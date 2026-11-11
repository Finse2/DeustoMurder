package mockClasses.testing;

import Networking.packets.GameOverPacket;
import game.statistics.LocalProfileStatistics;
import game.statistics.PlayerStatistics;
import game.statistics.ProfileMatchTracker;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Execute from the project with both source modules on the classpath.
 * No test framework required. The user's real profile is never modified.
 */
public final class ProfileStatisticsIntegrationTest {
    private ProfileStatisticsIntegrationTest() {}

    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("deusto-stats-test");
        Path file = directory.resolve("statistics.properties");
        try {
            LocalProfileStatistics stats = new LocalProfileStatistics(file);
            ProfileMatchTracker tracker = new ProfileMatchTracker(stats);

            tracker.onConnected(4, "Finse");
            tracker.onGameStarted();
            tracker.onGameOver(new GameOverPacket(4, "Finse", "Actor", "Weapon", "Room"));
            tracker.onGameOver(new GameOverPacket(4, "Finse", "Actor", "Weapon", "Room")); // Duplicate
            check(stats.snapshot(), 1, 1, 0, 1);

            // A subsequent match can assign the same client a different temporary ID.
            tracker.onConnected(9, "Finse");
            tracker.onGameStarted();
            tracker.onGameOver(new GameOverPacket(2, "Other", "Actor", "Weapon", "Room"));
            check(stats.snapshot(), 2, 1, 1, 1);

            LocalProfileStatistics reloaded = new LocalProfileStatistics(file);
            check(reloaded.snapshot(), 2, 1, 1, 1);
            if (!"Finse".equals(reloaded.snapshot().getPlayerName())) {
                throw new AssertionError("Saved profile name was not restored");
            }
            if (Math.abs(reloaded.snapshot().getWinRate() - 0.5) > 0.00001) {
                throw new AssertionError("Expected 50% win rate");
            }
            System.out.println("ProfileStatisticsIntegrationTest passed");
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(directory);
        }
    }

    private static void check(PlayerStatistics value, int games, int wins, int losses, int correct) {
        if (value.getGamesPlayed() != games || value.getWins() != wins
                || value.getLosses() != losses || value.getCorrectAccusations() != correct) {
            throw new AssertionError("Unexpected player stats: " + value);
        }
    }
}
