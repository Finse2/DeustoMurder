
package utils.JDBC;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;

public final class DatabaseConfig {

    // DATABASE BACKENDS

    public enum DatabaseType {
        SQLITE,
        RQLITE_MANAGED,
        RQLITE_REMOTE
    }

    // DEFAULT VALUES

    public static final String RQLITE_VERSION = "10.6.5";

    public static final int DEFAULT_HTTP_PORT = 4001;
    public static final int DEFAULT_RAFT_PORT = 4002;

    private static final Path DEFAULT_DIRECTORY =
            Path.of(
                    System.getProperty("user.home"),
                    ".deustomurder"
            );

    // CONFIGURATION FIELDS

    private final DatabaseType databaseType;

    private final Path sqliteFile;

    private final Path rqliteDirectory;
    private final Path rqliteExecutable;

    private final int httpPort;
    private final int raftPort;

    private final Duration startupTimeout;

    private final String remoteUrl;
    private final String downloadChecksum;

    // SINGLETON

    private static class Holder {

        private static final DatabaseConfig INSTANCE =
                new DatabaseConfig();
    }

    public static DatabaseConfig getInstance() {
        return Holder.INSTANCE;
    }

    // CONSTRUCTOR

    private DatabaseConfig() {

        databaseType = DatabaseType.valueOf(
                System.getProperty(
                        "database.type",
                        "RQLITE_MANAGED"
                ).trim().toUpperCase(Locale.ROOT)
        );

        sqliteFile = readPath(
                "database.sqlite.file",
                DEFAULT_DIRECTORY.resolve("database.db")
        );

        rqliteDirectory = readPath(
                "rqlite.directory",
                DEFAULT_DIRECTORY.resolve("rqlite")
        );

        httpPort = readInteger(
                "rqlite.http.port",
                DEFAULT_HTTP_PORT
        );

        raftPort = readInteger(
                "rqlite.raft.port",
                DEFAULT_RAFT_PORT
        );

        startupTimeout = Duration.ofSeconds(
                readInteger(
                        "rqlite.startup.timeout",
                        30
                )
        );

        remoteUrl = System.getProperty(
                "rqlite.remote.url",
                ""
        ).trim();

        downloadChecksum = System.getProperty(
                "rqlite.download.sha256",
                ""
        ).trim();

        String executable = System.getProperty(
                "rqlite.executable",
                ""
        ).trim();

        rqliteExecutable = executable.isBlank()
                ? null
                : Path.of(executable)
                .toAbsolutePath()
                .normalize();

        validate();
    }

    // CONFIGURATION VALIDATION

    private void validate() {

        validatePort(httpPort, "HTTP");
        validatePort(raftPort, "Raft");

        if (httpPort == raftPort) {

            throw new IllegalArgumentException(
                    "HTTP and Raft ports must be different."
            );
        }

        if (startupTimeout.isZero()
                || startupTimeout.isNegative()) {

            throw new IllegalArgumentException(
                    "Startup timeout must be positive."
            );
        }

        if (!downloadChecksum.isBlank()
                && !downloadChecksum.matches(
                "(?i)[0-9a-f]{64}"
        )) {

            throw new IllegalArgumentException(
                    "Invalid SHA-256 checksum."
            );
        }

        if (databaseType == DatabaseType.RQLITE_REMOTE) {

            if (remoteUrl.isBlank()) {

                throw new IllegalArgumentException(
                        "Remote rqlite URL is required."
                );
            }

            URI uri = URI.create(remoteUrl);

            boolean validScheme =
                    "http".equalsIgnoreCase(uri.getScheme())
                            || "https".equalsIgnoreCase(uri.getScheme());

            if (!validScheme
                    || uri.getHost() == null
                    || uri.getUserInfo() != null
                    || uri.getPort() > 65535
                    || uri.getPort() == 0) {

                throw new IllegalArgumentException(
                        "Invalid remote rqlite URL."
                );
            }
        }
    }

    private static void validatePort(
            int port,
            String name
    ) {

        if (port < 1 || port > 65535) {

            throw new IllegalArgumentException(
                    "Invalid " + name + " port: " + port
            );
        }
    }

    // CONFIGURATION UTILITIES

    private static Path readPath(
            String property,
            Path defaultPath
    ) {

        return Path.of(
                System.getProperty(
                        property,
                        defaultPath.toString()
                )
        ).toAbsolutePath().normalize();
    }

    private static int readInteger(
            String property,
            int defaultValue
    ) {

        String value = System.getProperty(property);

        if (value == null) {
            return defaultValue;
        }

        try {

            return Integer.parseInt(value.trim());

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Invalid integer for " + property,
                    e
            );
        }
    }

    // CONNECTION URLS

    public String getJdbcUrl() {

        return switch (databaseType) {

            case SQLITE ->
                    "jdbc:sqlite:" + sqliteFile;

            case RQLITE_MANAGED ->
                    getManagedJdbcUrl();

            case RQLITE_REMOTE ->
                    "jdbc:rqlite:" + remoteUrl;
        };
    }

    public String getManagedJdbcUrl() {

        return "jdbc:rqlite:" + getManagedServerUrl();
    }

    public String getManagedServerUrl() {

        return "http://127.0.0.1:" + httpPort;
    }

    // DATABASE TYPE

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public boolean isSQLite() {
        return databaseType == DatabaseType.SQLITE;
    }

    public boolean isRqlite() {
        return databaseType != DatabaseType.SQLITE;
    }

    public boolean isManagedRqlite() {
        return databaseType == DatabaseType.RQLITE_MANAGED;
    }

    public boolean isRemoteRqlite() {
        return databaseType == DatabaseType.RQLITE_REMOTE;
    }

    // PATHS

    public Path getSQLiteFile() {
        return sqliteFile;
    }

    public Path getRqliteDirectory() {
        return rqliteDirectory;
    }

    public Path getRqliteDataDirectory() {
        return rqliteDirectory.resolve("data");
    }

    public Path getRqliteLogFile() {
        return rqliteDirectory.resolve("rqlite.log");
    }

    public Path getWindowsExecutable() {
        return rqliteDirectory.resolve("rqlited.exe");
    }

    public Path getRqliteExecutable() {
        return rqliteExecutable;
    }

    // SERVER CONFIGURATION

    public int getHttpPort() {
        return httpPort;
    }

    public int getRaftPort() {
        return raftPort;
    }

    public Duration getStartupTimeout() {
        return startupTimeout;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public String getDownloadChecksum() {
        return downloadChecksum;
    }

    public String getRqliteVersion() {
        return RQLITE_VERSION;
    }

    public String getWindowsDownloadUrl() {

        return "https://github.com/rqlite/rqlite/releases/download/v"
                + RQLITE_VERSION
                + "/rqlite-v"
                + RQLITE_VERSION
                + "-win64.zip";
    }

    // DEBUGGING

    @Override
    public String toString() {

        return "DatabaseConfig{" +
                "databaseType=" + databaseType +
                ", sqliteFile=" + sqliteFile +
                ", rqliteDirectory=" + rqliteDirectory +
                ", httpPort=" + httpPort +
                ", raftPort=" + raftPort +
                ", startupTimeout=" + startupTimeout +
                ", rqliteVersion=" + RQLITE_VERSION +
                ", checksumConfigured=" +
                !downloadChecksum.isBlank() +
                '}';
    }
}
