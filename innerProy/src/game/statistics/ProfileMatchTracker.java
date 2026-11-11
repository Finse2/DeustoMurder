package game.statistics;

import Networking.packets.GameOverPacket;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class ProfileMatchTracker {

    private final LocalProfileStatistics store;

    private int localPlayerId = -1;
    private String localPlayerName = "Local Player";

    private boolean matchStarted = false;
    private long startNanos;

    public ProfileMatchTracker() {
        this(LocalProfileStatistics.getInstance());
    }

    public ProfileMatchTracker(LocalProfileStatistics store) {
        this.store = Objects.requireNonNull(store);
    }

    public synchronized void onConnected(
            int playerId,
            String playerName
    ) {
        localPlayerId = playerId;

        localPlayerName = playerName == null ||
                playerName.isBlank()
                ? "Local Player"
                : playerName.trim();

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

        String playerName;
        boolean won;
        long durationMillis;

        synchronized (this) {

            if (!matchStarted || localPlayerId < 0) {
                return;
            }

            // A valid winner must be known before recording a loss.
            if (result.getWinnerID() < 0 &&
                    (result.getWinnerName() == null ||
                            result.getWinnerName().isBlank())) {
                return;
            }

            if (result.getWinnerID() >= 0) {

                won = result.getWinnerID() == localPlayerId;

            } else {

                won = localPlayerName.equalsIgnoreCase(
                        result.getWinnerName().trim()
                );
            }

            durationMillis = Math.max(
                    0,
                    TimeUnit.NANOSECONDS.toMillis(
                            System.nanoTime() - startNanos
                    )
            );

            playerName = localPlayerName;

            // Prevent duplicate GAME_OVER packets.
            matchStarted = false;
        }

        store.recordFinishedMatch(
                playerName,
                won,
                durationMillis
        );
    }
}
