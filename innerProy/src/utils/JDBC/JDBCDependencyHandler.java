
package utils.JDBC;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**managed the installed SQL dependency, the jar itself still has to be imported aside into the Library sections*/
class JDBCDependencyHandler {

    /**define where rqlite is installed, where its executable is located, and where its database files are stored
     * the dot is there as part of a convention regarding application data*/
    private static final Path DIRECTORY = Path.of(
            System.getProperty("user.home"), ".deustomurder", "rqlite");

    /**full path to the rqlited.exe file inside that folder.*/
    private static final Path EXECUTABLE = DIRECTORY.resolve("rqlited.exe");

    /**folder where rqlite is installed*/
    private static final Path DATA = DIRECTORY.resolve("data"); //resolve finds the path to destination

    /**package version that will get installed*/
    private static final String VERSION = "10.6.5";

    /**link from whitch content is downloaded in case the usser thoesnt have the required dependencies*/
    private static final String DOWNLOAD_URL = "https://github.com/rqlite/rqlite/releases/download/v" + VERSION + "/rqlite-v" + VERSION + "-win64.zip";

    private static final String SERVER_URL = "http://127.0.0.1:4001";
    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(30);

    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).followRedirects(HttpClient.Redirect.NORMAL).build();

    private static Process serverProcess;

    public static synchronized Process startServer() throws Exception {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (!os.contains("win")) {
            throw new UnsupportedOperationException("The automatic rqlite installer currently supports Windows only.");
        }

        if (isReady()) {
            System.out.println("rqlite is already running and ready on port 4001.");
            return serverProcess != null && serverProcess.isAlive() ? serverProcess : null;
        }

        if (serverProcess != null && serverProcess.isAlive()) {
            waitUntilReady(serverProcess);
            return serverProcess;
        }

        if (isResponding()) {
            waitUntilReady(null);
            System.out.println("Existing rqlite server is ready on port 4001.");
            return null;
        }

        Files.createDirectories(DIRECTORY); //we create directory at a certain path if that directory thoesnt already exist
        Files.createDirectories(DATA);

        if (Files.notExists(EXECUTABLE)) {
            install();
        }

        ProcessBuilder builder = new ProcessBuilder(
                EXECUTABLE.toString(), "-node-id=1", DATA.toString());
        builder.directory(DIRECTORY.toFile());
        builder.inheritIO();

        serverProcess = builder.start();
        try {
            waitUntilReady(serverProcess);
            System.out.println("rqlite is ready on port 4001.");
            return serverProcess;
        } catch (Exception e) {
            if (serverProcess.isAlive()) {
                serverProcess.destroy();
            }
            serverProcess = null;
            throw e;
        }
    }

    private static boolean isReady() throws InterruptedException {
        return checkEndpoint("/readyz");
    }

    private static boolean isResponding() throws InterruptedException {
        return checkEndpoint("/readyz?noleader");
    }

    private static boolean checkEndpoint(String endpoint) throws InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + endpoint)).timeout(Duration.ofSeconds(2)).GET().build();

        try {
            return CLIENT.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }

    private static void waitUntilReady(Process process) throws Exception {
        long deadline = System.nanoTime() + STARTUP_TIMEOUT.toNanos();

        while (System.nanoTime() < deadline) {
            if (isReady()) {
                return;
            }
            if (process != null && !process.isAlive()) {
                throw new IOException("rqlited exited before becoming ready (exit code " + process.exitValue() + ").");
            }
            Thread.sleep(250);
        }

        throw new IOException("rqlite did not become ready at " + SERVER_URL + " within " + STARTUP_TIMEOUT.toSeconds() + " seconds.");
    }

    /**install the required dependency, however it test check weather it is already installed or not, reason of why checking must come before running it*/
    private static void install() throws Exception {
        System.out.println("Downloading rqlite...");

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(DOWNLOAD_URL)).timeout(Duration.ofMinutes(3)).GET().build();

        HttpResponse<InputStream> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        try (InputStream body = response.body()) {
            if (response.statusCode() != 200) {
                throw new IOException("rqlite download failed: HTTP " + response.statusCode());
            }

            try (ZipInputStream zip = new ZipInputStream(body)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    String name = entry.getName().replace('\\', '/');
                    if (!entry.isDirectory() && (name.equals("rqlited.exe") || name.endsWith("/rqlited.exe"))) {

                        Path temporary = DIRECTORY.resolve("rqlited.exe.part");
                        try {
                            Files.copy(zip, temporary, StandardCopyOption.REPLACE_EXISTING);
                            Files.move(temporary, EXECUTABLE, StandardCopyOption.REPLACE_EXISTING);
                        } finally {
                            Files.deleteIfExists(temporary);
                        }

                        System.out.println("rqlite installed successfully.");
                        return;
                    }
                    zip.closeEntry();
                }
            }
        }

        throw new IOException("rqlited.exe was not found in the downloaded ZIP.");
    }
}
