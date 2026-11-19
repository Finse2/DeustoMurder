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

/** Starts a single-node rqlite server on Windows or macOS. Never used for SQLite files. */
final class JDBCDependencyHandler {

    private static final Path DIRECTORY = Path.of(
            System.getProperty("user.home"), ".deustomurder", "rqlite");
    private static final Path DATA = DIRECTORY.resolve("data");
    private static final Path WINDOWS_EXECUTABLE = DIRECTORY.resolve("rqlited.exe");

    private static final String VERSION = "10.6.5";
    private static final String DOWNLOAD_URL =
            "https://github.com/rqlite/rqlite/releases/download/v" + VERSION
                    + "/rqlite-v" + VERSION + "-win64.zip";

    private static final String SERVER_URL = "http://127.0.0.1:4001";
    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(30);
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static Process serverProcess;

    private JDBCDependencyHandler() {}

    public static synchronized Process startServer() throws Exception {
        if (isReady()) return serverProcess != null && serverProcess.isAlive() ? serverProcess : null;

        if (serverProcess != null && serverProcess.isAlive()) {
            waitUntilReady(serverProcess);
            return serverProcess;
        }

        if (isResponding()) {
            waitUntilReady(null);
            return null;
        }

        Files.createDirectories(DIRECTORY);
        Files.createDirectories(DATA);
        Path executable = resolveExecutable();

        ProcessBuilder builder = new ProcessBuilder(
                executable.toString(),
                "-node-id=1",
                "-http-addr=127.0.0.1:4001",
                "-raft-addr=127.0.0.1:4002",
                DATA.toString()
        );
        builder.directory(DIRECTORY.toFile());

        //this part takes out the error stream so the console thoesnt get spammed
        builder.redirectErrorStream(true);
        builder.redirectOutput(
                ProcessBuilder.Redirect.appendTo(
                        DIRECTORY.resolve("rqlite.log").toFile()
                )
        );
        serverProcess = builder.start();
        //----

        try {
            waitUntilReady(serverProcess);
            System.out.println("rqlite is ready at " + SERVER_URL);
            return serverProcess;
        } catch (Exception e) {
            if (serverProcess.isAlive()) serverProcess.destroy();
            serverProcess = null;
            throw e;
        }
    }

    private static Path resolveExecutable() throws Exception {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            if (Files.notExists(WINDOWS_EXECUTABLE)) installWindows();
            return WINDOWS_EXECUTABLE;
        }
        if (os.contains("mac")) {
            Path executable = findMacExecutable();
            if (executable != null) return executable;

            Path brew = findExecutable("brew");
            if (brew == null) {
                brew = firstExecutable(Path.of("/opt/homebrew/bin/brew"), Path.of("/usr/local/bin/brew"));
            }
            if (brew == null) {
                throw new IOException("Homebrew is not installed. Install it from https://brew.sh, "
                        + "then run: brew install rqlite");
            }

            System.out.println("Installing rqlite using Homebrew...");
            Process install = new ProcessBuilder(brew.toString(), "install", "rqlite")
                    .inheritIO().start();
            if (install.waitFor() != 0) {
                throw new IOException("Homebrew could not install rqlite. Try: brew install rqlite");
            }
            executable = findMacExecutable();
            if (executable == null) {
                throw new IOException("Homebrew finished but rqlited was not found.");
            }
            return executable;
        }
        throw new UnsupportedOperationException("Managed rqlite is supported on Windows and macOS only.");
    }

    private static Path findMacExecutable() {
        Path executable = findExecutable("rqlited");
        return executable != null ? executable : firstExecutable(
                Path.of("/opt/homebrew/bin/rqlited"), Path.of("/usr/local/bin/rqlited"));
    }

    private static Path firstExecutable(Path... paths) {
        for (Path path : paths) {
            if (Files.isRegularFile(path) && Files.isExecutable(path)) return path;
        }
        return null;
    }

    private static Path findExecutable(String name) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) return null;
        for (String path : pathEnv.split(java.io.File.pathSeparator)) {
            if (path.isBlank()) continue;
            Path candidate = Path.of(path, name);
            if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) return candidate;
        }
        return null;
    }

    private static boolean isReady() throws InterruptedException {
        return checkEndpoint("/readyz");
    }

    private static boolean isResponding() throws InterruptedException {
        return checkEndpoint("/readyz?noleader");
    }

    private static boolean checkEndpoint(String endpoint) throws InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + endpoint))
                .timeout(Duration.ofSeconds(2)).GET().build();
        try {
            return CLIENT.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }

    private static void waitUntilReady(Process process) throws Exception {
        long deadline = System.nanoTime() + STARTUP_TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            if (isReady()) return;
            if (process != null && !process.isAlive()) {
                throw new IOException("rqlited exited during startup (exit code "
                        + process.exitValue() + ")");
            }
            Thread.sleep(250);
        }
        throw new IOException("rqlite did not become ready at " + SERVER_URL
                + " within " + STARTUP_TIMEOUT.toSeconds() + " seconds");
    }

    private static void installWindows() throws Exception {
        System.out.println("Downloading rqlite " + VERSION + " for Windows...");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DOWNLOAD_URL))
                .timeout(Duration.ofMinutes(3)).GET().build();
        HttpResponse<InputStream> response = CLIENT.send(
                request, HttpResponse.BodyHandlers.ofInputStream());

        try (InputStream body = response.body()) {
            if (response.statusCode() != 200) {
                throw new IOException("Download failed: HTTP " + response.statusCode());
            }
            try (ZipInputStream zip = new ZipInputStream(body)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    String name = entry.getName().replace('\\', '/');
                    if (!entry.isDirectory()
                            && (name.equals("rqlited.exe") || name.endsWith("/rqlited.exe"))) {
                        Path temporary = DIRECTORY.resolve("rqlited.exe.part");
                        try {
                            Files.copy(zip, temporary, StandardCopyOption.REPLACE_EXISTING);
                            Files.move(temporary, WINDOWS_EXECUTABLE,
                                    StandardCopyOption.REPLACE_EXISTING);
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
