package game.statistics;

import Networking.packets.GameOverPacket;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Tracks one client connection's ongoing match. This prevents duplicate
 * GAME_OVER packets from incrementing profile counters more than once.
 */
public final class ProfileMatchTracker {
    private final LocalProfileStatistics store;
    private int localPlayerId = -1;
    private String localPlayerName = "Local Player";
    private boolean matchStarted;
    private long startNanos;

    public ProfileMatchTracker() {
        this(LocalProfileStatistics.getInstance());
    }

    public ProfileMatchTracker(LocalProfileStatistics store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    public synchronized void onConnected(int playerId, String playerName) {
        localPlayerId = playerId;
        localPlayerName = playerName == null || playerName.isBlank()
                ? "Local Player" : playerName.trim();
        matchStarted = false;
    }

    public synchronized void onGameStarted() {
        if (localPlayerId < 0 || matchStarted) {
            return;
        }
        matchStarted = true;
        startNanos = System.nanoTime();
    }

    public void onGameOver(GameOverPacket result) {
        if (result == null || !result.getGameOver()) {
            return;
        }

        final String name;
        final boolean won;
        final long durationMillis;
        synchronized (this) {
            if (!matchStarted || localPlayerId < 0) {
                return;
            }
            if (result.getWinnerID() < 0
                    && (result.getWinnerName() == null || result.getWinnerName().isBlank())) {
                return; // No confirmed winner; do not guess that the player lost.
            }
            won = result.getWinnerID() >= 0
                    ? result.getWinnerID() == localPlayerId
                    : localPlayerName.equalsIgnoreCase(result.getWinnerName().trim());
            durationMillis = Math.max(0L, TimeUnit.NANOSECONDS.toMillis(
                    System.nanoTime() - startNanos));
            name = localPlayerName;
            matchStarted = false; // Mark finished before writing; duplicates are ignored.
        }
        store.recordFinishedMatch(name, won, durationMillis);
    }
}
