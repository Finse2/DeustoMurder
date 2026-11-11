
package game.statistics;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class LocalProfileStatistics {

    private static final int LOCAL_PROFILE_ID = 0;

    private static final Path DEFAULT_PATH = Path.of(
            System.getProperty("user.home"),
            ".deustoCluedo",
            "statistics.properties"
    );

    private static final LocalProfileStatistics INSTANCE =
            new LocalProfileStatistics(DEFAULT_PATH);

    private final StatisticsManager manager = new StatisticsManager();
    private final Path file;

    private final List<Runnable> listeners =
            new CopyOnWriteArrayList<>();

    public static LocalProfileStatistics getInstance() {
        return INSTANCE;
    }

    public LocalProfileStatistics(Path file) {
        this.file = Objects.requireNonNull(file);
        load();
    }

    public synchronized PlayerStatistics snapshot() {

        PlayerStatistics stats =
                manager.getOrCreate(LOCAL_PROFILE_ID, null);

        return PlayerStatistics.fromPersisted(
                stats.getPlayerId(),
                stats.getPlayerName(),
                stats.getGamesPlayed(),
                stats.getWins(),
                stats.getLosses(),
                stats.getAccusationsMade(),
                stats.getCorrectAccusations(),
                stats.getIncorrectAccusations(),
                stats.getTotalGameDurationMillis()
        );
    }

    public void recordFinishedMatch(
            String playerName,
            boolean won,
            long durationMillis
    ) {
        synchronized (this) {

            String name = playerName == null ||
                    playerName.isBlank()
                    ? "Local Player"
                    : playerName.trim();

            manager.recordGameFinished(
                    LOCAL_PROFILE_ID,
                    name,
                    won,
                    durationMillis
            );

            // Winning a Cluedo game means a correct accusation.
            if (won) {
                manager.recordAccusation(
                        LOCAL_PROFILE_ID,
                        name,
                        true
                );
            }

            save();
        }

        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    public void addChangeListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public void removeChangeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private synchronized void load() {

        if (!Files.isRegularFile(file)) {
            return;
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(file)) {

            properties.load(input);

            PlayerStatistics stats = PlayerStatistics.fromPersisted(
                    LOCAL_PROFILE_ID,
                    properties.getProperty("playerName", "Local Player"),
                    Integer.parseInt(properties.getProperty("gamesPlayed", "0")),
                    Integer.parseInt(properties.getProperty("wins", "0")),
                    Integer.parseInt(properties.getProperty("losses", "0")),
                    Integer.parseInt(properties.getProperty("accusationsMade", "0")),
                    Integer.parseInt(properties.getProperty("correctAccusations", "0")),
                    Integer.parseInt(properties.getProperty("incorrectAccusations", "0")),
                    Long.parseLong(properties.getProperty("totalGameDurationMillis", "0"))
            );

            manager.restoreStatistics(stats);

        } catch (IOException | IllegalArgumentException exception) {
            System.err.println(
                    "Cannot load profile statistics: " +
                            exception.getMessage()
            );
        }
    }

    private synchronized void save() {

        PlayerStatistics stats =
                manager.getOrCreate(LOCAL_PROFILE_ID, null);

        Properties properties = new Properties();

        properties.setProperty("playerName", stats.getPlayerName());
        properties.setProperty("gamesPlayed",
                Integer.toString(stats.getGamesPlayed()));
        properties.setProperty("wins",
                Integer.toString(stats.getWins()));
        properties.setProperty("losses",
                Integer.toString(stats.getLosses()));
        properties.setProperty("accusationsMade",
                Integer.toString(stats.getAccusationsMade()));
        properties.setProperty("correctAccusations",
                Integer.toString(stats.getCorrectAccusations()));
        properties.setProperty("incorrectAccusations",
                Integer.toString(stats.getIncorrectAccusations()));
        properties.setProperty("totalGameDurationMillis",
                Long.toString(stats.getTotalGameDurationMillis()));

        Path temp = null;

        try {
            Path parent = file.toAbsolutePath().getParent();
            Files.createDirectories(parent);

            temp = Files.createTempFile(
                    parent,
                    "deusto-stats-",
                    ".tmp"
            );

            try (OutputStream output = Files.newOutputStream(temp)) {
                properties.store(
                        output,
                        "Deusto Cluedo player statistics"
                );
            }

            try {
                Files.move(
                        temp,
                        file,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(
                        temp,
                        file,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (IOException exception) {
            System.err.println(
                    "Cannot save profile statistics: " +
                            exception.getMessage()
            );
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                }
            }
        }
    }
}
