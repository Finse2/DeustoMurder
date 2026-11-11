package game.statistics;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Statistics of the profile installed on this computer. The server's temporary
 * player IDs are deliberately not used as persistent profile identities.
 */
public final class LocalProfileStatistics {
    private static final int LOCAL_PROFILE_ID = 0;
    private static final Path DEFAULT_PATH = Path.of(
            System.getProperty("user.home"), ".deustoCluedo", "statistics.properties");
    private static final LocalProfileStatistics INSTANCE = new LocalProfileStatistics(DEFAULT_PATH);

    private final Path file;
    private final StatisticsManager manager = new StatisticsManager();
    private final CopyOnWriteArrayList<Runnable> listeners = new CopyOnWriteArrayList<>();

    public static LocalProfileStatistics getInstance() {
        return INSTANCE;
    }

    /** Public for testing with a temporary file instead of the real user profile. */
    public LocalProfileStatistics(Path file) {
        this.file = Objects.requireNonNull(file, "file");
        load();
    }

    /** A defensive copy: callers cannot change stored counters through the UI. */
    public synchronized PlayerStatistics snapshot() {
        PlayerStatistics value = manager.getOrCreate(LOCAL_PROFILE_ID, "Local Player");
        return PlayerStatistics.fromPersisted(
                value.getPlayerId(), value.getPlayerName(),
                value.getGamesPlayed(), value.getWins(), value.getLosses(),
                value.getAccusationsMade(), value.getCorrectAccusations(),
                value.getIncorrectAccusations(), value.getTotalGameDurationMillis());
    }

    /**
     * Called when this client's own match has ended, not when any opponent ends
     * a match. A Cluedo victory necessarily means a correct final accusation.
     * Wrong accusations are not inferred from losses.
     */
    public void recordFinishedMatch(String playerName, boolean won, long durationMillis) {
        synchronized (this) {
            String name = playerName == null || playerName.isBlank() ? "Local Player" : playerName.trim();
            manager.recordGameFinished(LOCAL_PROFILE_ID, name, won, durationMillis);
            if (won) {
                manager.recordAccusation(LOCAL_PROFILE_ID, name, true);
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
        Properties p = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            p.load(input);
            PlayerStatistics restored = PlayerStatistics.fromPersisted(
                    LOCAL_PROFILE_ID,
                    p.getProperty("playerName", "Local Player"),
                    Integer.parseInt(p.getProperty("gamesPlayed", "0")),
                    Integer.parseInt(p.getProperty("wins", "0")),
                    Integer.parseInt(p.getProperty("losses", "0")),
                    Integer.parseInt(p.getProperty("accusationsMade", "0")),
                    Integer.parseInt(p.getProperty("correctAccusations", "0")),
                    Integer.parseInt(p.getProperty("incorrectAccusations", "0")),
                    Long.parseLong(p.getProperty("totalGameDurationMillis", "0")));
            manager.restoreStatistics(restored);
        } catch (IOException | IllegalArgumentException ex) {
            System.err.println("Could not load player statistics from " + file + ": " + ex.getMessage());
        }
    }

    private synchronized void save() {
        PlayerStatistics value = manager.getOrCreate(LOCAL_PROFILE_ID, "Local Player");
        Properties p = new Properties();
        p.setProperty("playerName", value.getPlayerName());
        p.setProperty("gamesPlayed", Integer.toString(value.getGamesPlayed()));
        p.setProperty("wins", Integer.toString(value.getWins()));
        p.setProperty("losses", Integer.toString(value.getLosses()));
        p.setProperty("accusationsMade", Integer.toString(value.getAccusationsMade()));
        p.setProperty("correctAccusations", Integer.toString(value.getCorrectAccusations()));
        p.setProperty("incorrectAccusations", Integer.toString(value.getIncorrectAccusations()));
        p.setProperty("totalGameDurationMillis", Long.toString(value.getTotalGameDurationMillis()));

        Path temp = null;
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            temp = Files.createTempFile(parent, "deusto-stats-", ".tmp");
            try (OutputStream output = Files.newOutputStream(temp)) {
                p.store(output, "Deusto Cluedo local profile statistics");
            }
            try {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            System.err.println("Could not save player statistics to " + file + ": " + ex.getMessage());
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // Best-effort cleanup of the temporary file.
                }
            }
        }
    }
}
