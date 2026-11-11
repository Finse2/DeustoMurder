
package mockClasses.testing;

import Networking.packets.GameOverPacket;
import game.statistics.LocalProfileStatistics;
import game.statistics.PlayerStatistics;
import game.statistics.ProfileMatchTracker;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ProfileStatisticsIntegrationTest {

    public static void main(String[] args) throws Exception {

        Path directory = Files.createTempDirectory("deusto-stats-test");
        Path file = directory.resolve("statistics.properties");

        try {

            LocalProfileStatistics store =
                    new LocalProfileStatistics(file);

            ProfileMatchTracker tracker =
                    new ProfileMatchTracker(store);

            // First match: local player wins.
            tracker.onConnected(4, "Finse");
            tracker.onGameStarted();

            GameOverPacket victory = new GameOverPacket(
                    4,
                    "Finse",
                    "Actor",
                    "Weapon",
                    "Room"
            );

            tracker.onGameOver(victory);
            tracker.onGameOver(victory); // Should not count twice.

            // Second match: local player loses.
            tracker.onConnected(9, "Finse");
            tracker.onGameStarted();

            tracker.onGameOver(new GameOverPacket(
                    2,
                    "Other",
                    "Actor",
                    "Weapon",
                    "Room"
            ));

            // Load the statistics again from disk.
            LocalProfileStatistics reloaded =
                    new LocalProfileStatistics(file);

            PlayerStatistics stats = reloaded.snapshot();

            if (stats.getGamesPlayed() != 2 ||
                    stats.getWins() != 1 ||
                    stats.getLosses() != 1 ||
                    stats.getCorrectAccusations() != 1 ||
                    Math.abs(stats.getWinRate() - 0.5) > 0.00001) {

                throw new AssertionError(
                        "Statistics did not persist correctly: " + stats
                );
            }

            System.out.println("Profile statistics test passed");
            System.out.println(stats);

        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(directory);
        }
    }
}
