
package utils.JDBC;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class JDBCDependencyHandler {

    // LOGGING

    private static final Logger LOGGER =
            Logger.getLogger(
                    JDBCDependencyHandler.class.getName()
            );


// CONFIGURATION

    private static final DatabaseConfig CONFIG =
            DatabaseConfig.getInstance();

    private static final String VERSION =
            CONFIG.getRqliteVersion();

    private static final int HTTP_PORT =
            CONFIG.getHttpPort();

    private static final int RAFT_PORT =
            CONFIG.getRaftPort();

    private static final Path DIRECTORY =
            CONFIG.getRqliteDirectory();

    private static final Path DATA =
            CONFIG.getRqliteDataDirectory();

    private static final Path LOG_FILE =
            CONFIG.getRqliteLogFile();

    private static final Path WINDOWS_EXECUTABLE =
            CONFIG.getWindowsExecutable();

    private static final String DOWNLOAD_URL =
            CONFIG.getWindowsDownloadUrl();

    private static final String EXPECTED_SHA256 =
            CONFIG.getDownloadChecksum();

    private static final String SERVER_URL =
            CONFIG.getManagedServerUrl();

    private static final Duration STARTUP_TIMEOUT =
            CONFIG.getStartupTimeout();

    private static final HttpClient CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();


    // PROCESS MANAGEMENT

    private static Process serverProcess;
    private static boolean ownsProcess;
    private static boolean shutdownHookRegistered;

    private JDBCDependencyHandler() {}

    // SERVER STARTUP

    public static synchronized Process startServer()
            throws Exception {

        if (serverProcess != null) {

            if (serverProcess.isAlive()) {

                waitUntilReady(serverProcess);

                return serverProcess;
            }

            serverProcess = null;
            ownsProcess = false;
        }

        // Reuse an existing external server.

        if (isReady()) {

            LOGGER.info(
                    "Using existing rqlite server at "
                            + SERVER_URL
            );

            return null;
        }

        if (isResponding()) {

            LOGGER.info(
                    "Waiting for existing rqlite server."
            );

            waitUntilReady(null);

            return null;
        }

        Files.createDirectories(DIRECTORY);
        Files.createDirectories(DATA);

        Path executable = resolveExecutable();

        registerShutdownHook();

        ProcessBuilder builder = new ProcessBuilder(

                executable.toString(),

                "-node-id=1",

                "-http-addr=127.0.0.1:" + HTTP_PORT,

                "-raft-addr=127.0.0.1:" + RAFT_PORT,

                DATA.toString()
        );

        builder.directory(DIRECTORY.toFile());

        builder.redirectErrorStream(true);

        builder.redirectOutput(
                ProcessBuilder.Redirect.appendTo(
                        LOG_FILE.toFile()
                )
        );

        LOGGER.info("Starting rqlite server...");

        serverProcess = builder.start();
        ownsProcess = true;

        try {

            waitUntilReady(serverProcess);

            LOGGER.info(
                    "rqlite is ready at " + SERVER_URL
            );

            return serverProcess;

        } catch (Exception e) {

            stopServer();

            throw e;
        }
    }

    // SERVER SHUTDOWN

    public static synchronized void stopServer() {

        if (!ownsProcess || serverProcess == null) {
            return;
        }

        Process process = serverProcess;

        try {

            if (process.isAlive()) {

                LOGGER.info(
                        "Stopping managed rqlite server..."
                );

                process.destroy();

                if (!process.waitFor(
                        5, TimeUnit.SECONDS
                )) {

                    LOGGER.warning(
                            "Graceful shutdown timed out."
                    );

                    process.destroyForcibly();

                    process.waitFor(
                            5, TimeUnit.SECONDS
                    );
                }
            }

        } catch (InterruptedException e) {

            process.destroyForcibly();

            Thread.currentThread().interrupt();

        } finally {

            if (!process.isAlive()) {

                serverProcess = null;
                ownsProcess = false;

                LOGGER.info("rqlite server stopped.");

            } else {

                LOGGER.warning(
                        "rqlite process has not exited."
                );
            }
        }
    }

    private static synchronized void registerShutdownHook() {

        if (shutdownHookRegistered) {
            return;
        }

        Runtime.getRuntime().addShutdownHook(
                new Thread(
                        JDBCDependencyHandler::stopServer,
                        "Rqlite-Shutdown"
                )
        );

        shutdownHookRegistered = true;
    }

    // SERVER STATUS

    public static synchronized boolean isManagedServer() {

        return ownsProcess
                && serverProcess != null
                && serverProcess.isAlive();
    }

    public static boolean isRunning() {

        try {

            return isReady();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            return false;
        }
    }

    public static synchronized long getProcessId() {

        if (serverProcess == null
                || !serverProcess.isAlive()) {

            return -1;
        }

        return serverProcess.pid();
    }

    public static String getServerUrl() {
        return SERVER_URL;
    }

    public static String getJdbcUrl() {
        return "jdbc:rqlite:" + SERVER_URL;
    }

    public static Path getDataDirectory() {
        return DATA;
    }

    public static Path getLogFile() {
        return LOG_FILE;
    }

    // CONFIGURATION VALIDATION

    private static void validateConfiguration() {

        if (HTTP_PORT < 1 || HTTP_PORT > 65535
                || RAFT_PORT < 1 || RAFT_PORT > 65535) {

            throw new IllegalArgumentException(
                    "Invalid rqlite port configuration."
            );
        }

        if (HTTP_PORT == RAFT_PORT) {

            throw new IllegalArgumentException(
                    "HTTP and Raft ports must be different."
            );
        }

        if (STARTUP_TIMEOUT.isZero()
                || STARTUP_TIMEOUT.isNegative()) {

            throw new IllegalArgumentException(
                    "Startup timeout must be positive."
            );
        }
    }

    // OPERATING SYSTEM HANDLING

    private static Path resolveExecutable() throws Exception {
        Path executableOverride =
                CONFIG.getRqliteExecutable();

        if (executableOverride != null) {

            if (!Files.isRegularFile(executableOverride)
                    || !Files.isExecutable(executableOverride)) {

                throw new IOException(
                        "Invalid rqlite executable: "
                                + executableOverride
                );
            }

            return executableOverride;
        }

        String os = System.getProperty("os.name")
                .toLowerCase(Locale.ROOT);

        if (os.startsWith("windows")) {

            Path installed =
                    findExecutable("rqlited.exe");

            if (installed != null) {
                return installed;
            }

            if (Files.notExists(WINDOWS_EXECUTABLE)) {
                installWindows();
            }

            return WINDOWS_EXECUTABLE;
        }

        if (os.contains("mac") || os.contains("darwin")) {
            return resolveMacExecutable();
        }

        if (os.contains("linux")) {
            return resolveLinuxExecutable();
        }

        throw new UnsupportedOperationException(
                "Unsupported operating system: " + os
        );
    }

    // MACOS

    private static Path resolveMacExecutable()
            throws Exception {

        Path executable = findMacExecutable();

        if (executable != null) {
            return executable;
        }

        Path brew = findExecutable("brew");

        if (brew == null) {

            brew = firstExecutable(
                    Path.of("/opt/homebrew/bin/brew"),
                    Path.of("/usr/local/bin/brew")
            );
        }

        if (brew == null) {

            throw new IOException(
                    "Homebrew is not installed. "
                            + "Install Homebrew from "
                            + "https://brew.sh and run: "
                            + "brew install rqlite"
            );
        }

        LOGGER.info(
                "Installing rqlite using Homebrew..."
        );

        Process install = new ProcessBuilder(
                brew.toString(),
                "install",
                "rqlite"
        ).inheritIO().start();

        if (install.waitFor() != 0) {

            throw new IOException(
                    "Homebrew could not install rqlite."
            );
        }

        executable = findMacExecutable();

        if (executable == null) {

            throw new IOException(
                    "rqlited was not found after installation."
            );
        }

        return executable;
    }

    private static Path findMacExecutable() {

        Path executable = findExecutable("rqlited");

        if (executable != null) {
            return executable;
        }

        return firstExecutable(
                Path.of("/opt/homebrew/bin/rqlited"),
                Path.of("/usr/local/bin/rqlited")
        );
    }

    // LINUX

    private static Path resolveLinuxExecutable()
            throws IOException {

        Path executable = findExecutable("rqlited");

        if (executable != null) {
            return executable;
        }

        executable = firstExecutable(
                DIRECTORY.resolve("rqlited"),
                Path.of("/usr/local/bin/rqlited"),
                Path.of("/usr/bin/rqlited")
        );

        if (executable != null) {
            return executable;
        }

        throw new IOException(
                "rqlited was not found. "
                        + "Install rqlite or configure "
                        + "-Drqlite.executable=/path/to/rqlited"
        );
    }

    // EXECUTABLE DISCOVERY

    private static Path firstExecutable(Path... paths) {

        for (Path path : paths) {

            if (Files.isRegularFile(path)
                    && Files.isExecutable(path)) {

                return path.toAbsolutePath();
            }
        }

        return null;
    }

    private static Path findExecutable(String name) {

        String pathEnvironment = System.getenv("PATH");

        if (pathEnvironment == null) {
            return null;
        }

        for (String path : pathEnvironment.split(
                java.io.File.pathSeparator
        )) {

            if (path.isBlank()) {
                continue;
            }

            Path candidate = Path.of(path, name);

            if (Files.isRegularFile(candidate)
                    && Files.isExecutable(candidate)) {

                return candidate.toAbsolutePath();
            }
        }

        return null;
    }

    // HEALTH CHECKS

    private static boolean isReady()
            throws InterruptedException {

        return checkEndpoint("/readyz");
    }

    private static boolean isResponding()
            throws InterruptedException {

        return checkEndpoint("/readyz?noleader");
    }

    private static boolean checkEndpoint(String endpoint)
            throws InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()

                .uri(URI.create(SERVER_URL + endpoint))

                .timeout(Duration.ofSeconds(2))

                .GET()

                .build();

        try {

            HttpResponse<Void> response = CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.discarding()
            );

            return response.statusCode() == 200;

        } catch (IOException e) {

            return false;
        }
    }

    private static void waitUntilReady(Process process)
            throws Exception {

        long deadline = System.nanoTime()
                + STARTUP_TIMEOUT.toNanos();

        while (System.nanoTime() < deadline) {

            if (process != null && !process.isAlive()) {

                throw new IOException(
                        "rqlited exited with code "
                                + process.exitValue()
                                + ". Check " + LOG_FILE
                );
            }

            if (isReady()) {
                return;
            }

            Thread.sleep(250);
        }

        throw new IOException(
                "rqlite did not become ready within "
                        + STARTUP_TIMEOUT.toSeconds()
                        + " seconds. Check " + LOG_FILE
        );
    }

    // WINDOWS INSTALLATION

    private static void installWindows()
            throws Exception {

        LOGGER.info(
                "Downloading rqlite " + VERSION
        );

        Path archive = Files.createTempFile(
                DIRECTORY,
                "rqlite-",
                ".zip"
        );

        Path temporaryExecutable =
                Files.createTempFile(
                        DIRECTORY,
                        "rqlited-",
                        ".part"
                );

        try {

            HttpRequest request =
                    HttpRequest.newBuilder()

                            .uri(URI.create(DOWNLOAD_URL))

                            .timeout(Duration.ofMinutes(3))

                            .GET()

                            .build();

            HttpResponse<Path> response = CLIENT.send(

                    request,

                    HttpResponse.BodyHandlers.ofFile(
                            archive,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.TRUNCATE_EXISTING
                    )
            );

            if (response.statusCode() != 200) {

                throw new IOException(
                        "Download failed: HTTP "
                                + response.statusCode()
                );
            }

            verifyChecksum(archive);

            extractExecutable(
                    archive,
                    temporaryExecutable
            );

            if (Files.size(temporaryExecutable) == 0) {

                throw new IOException(
                        "Extracted executable is empty."
                );
            }

            try {

                Files.move(
                        temporaryExecutable,
                        WINDOWS_EXECUTABLE,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );

            } catch (
                    AtomicMoveNotSupportedException e
            ) {

                Files.move(
                        temporaryExecutable,
                        WINDOWS_EXECUTABLE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            LOGGER.info(
                    "rqlite installed successfully."
            );

        } finally {

            Files.deleteIfExists(archive);
            Files.deleteIfExists(temporaryExecutable);
        }
    }

    private static void extractExecutable(
            Path archive,
            Path destination
    ) throws IOException {

        try (ZipInputStream zip = new ZipInputStream(
                Files.newInputStream(archive)
        )) {

            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {

                String name = entry.getName()
                        .replace('\\', '/');

                if (!entry.isDirectory()
                        && (
                        name.equals("rqlited.exe")
                                || name.endsWith("/rqlited.exe")
                )) {

                    Files.copy(
                            zip,
                            destination,
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    return;
                }

                zip.closeEntry();
            }
        }

        throw new IOException(
                "rqlited.exe was not found in the ZIP."
        );
    }

    // DOWNLOAD INTEGRITY

    private static void verifyChecksum(Path archive)
            throws Exception {

        if (EXPECTED_SHA256.isBlank()) {

            LOGGER.warning(
                    "No trusted SHA-256 checksum configured. "
                            + "Download integrity was not verified."
            );

            return;
        }

        if (!EXPECTED_SHA256.matches(
                "(?i)[0-9a-f]{64}"
        )) {

            throw new IllegalArgumentException(
                    "Invalid SHA-256 checksum."
            );
        }

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        try (InputStream input =
                     Files.newInputStream(archive)) {

            byte[] buffer = new byte[8192];

            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {

                digest.update(
                        buffer,
                        0,
                        bytesRead
                );
            }
        }

        byte[] expected =
                HexFormat.of().parseHex(EXPECTED_SHA256);

        if (!MessageDigest.isEqual(
                expected,
                digest.digest()
        )) {

            throw new SecurityException(
                    "rqlite download checksum mismatch."
            );
        }

        LOGGER.info(
                "Download checksum verified successfully."
        );
    }

    public static int getHttpPort() {
        return HTTP_PORT;
    }
}
