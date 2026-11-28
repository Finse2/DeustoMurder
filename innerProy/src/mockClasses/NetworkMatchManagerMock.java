
package mockClasses;

import Networking.match.NetworkMatchManager;
import Networking.match.NetworkMatchResult;
import Networking.match.NetworkPlayerResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class NetworkMatchManagerMock {

    public static void main(String[] args)
            throws Exception {

        System.out.println(
                "=== NETWORK MATCH MANAGER TEST ==="
        );

        NetworkMatchManager manager =
                new NetworkMatchManager();

        UUID player1 = UUID.randomUUID();

        UUID player2 = UUID.randomUUID();

        UUID player3 = UUID.randomUUID();

        List<NetworkMatchManager.Participant> players =
                new ArrayList<>();

        players.add(

                new NetworkMatchManager.Participant(

                        1,

                        player1,

                        "Detective One",

                        "Character A"
                )
        );

        players.add(

                new NetworkMatchManager.Participant(

                        2,

                        player2,

                        "Detective Two",

                        "Character B"
                )
        );

        players.add(

                new NetworkMatchManager.Participant(

                        3,

                        player3,

                        "Detective Three",

                        "Character C"
                )
        );

        // TEST 1: INITIAL STATE

        check(

                manager.getState()
                        == NetworkMatchManager.State.IDLE,

                "Invalid initial state"
        );

        pass("Initial match state");

        // TEST 2: MINIMUM PARTICIPANTS

        expectFailure(() ->

                manager.startMatch(

                        player1,

                        List.of(players.getFirst())
                )
        );

        pass("Insufficient participants rejected");

        // TEST 3: UNKNOWN HOST

        expectFailure(() ->

                manager.startMatch(

                        UUID.randomUUID(),

                        players
                )
        );

        pass("Unknown host rejected");

        // TEST 4: DUPLICATE PARTICIPANTS

        expectFailure(() ->

                manager.startMatch(

                        player1,

                        List.of(

                                players.get(0),

                                players.get(0)
                        )
                )
        );

        pass("Duplicate participants rejected");

        // TEST 5: START MATCH

        UUID matchId =
                manager.startMatch(

                        player1,

                        players
                );

        check(

                matchId != null

                        && manager.isInProgress()

                        && manager.getPlayerCount() == 3,

                "Could not start match"
        );

        pass("Match started");

        // TEST 6: DUPLICATE START

        expectFailure(() ->

                manager.startMatch(

                        player1,

                        players
                )
        );

        pass("Duplicate match start rejected");

        // TEST 7: RECORD GAMEPLAY

        manager.recordTurn(1);

        manager.recordTurn(1);

        manager.recordDiceRoll(1);

        manager.recordDiceRoll(1);

        manager.recordMovement(1, 5);

        manager.recordMovement(1, 3);

        manager.recordSuggestion(1);

        manager.recordAccusation(1, true);

        manager.addPoints(1, 100);

        manager.recordTurn(2);

        manager.recordDiceRoll(2);

        manager.recordMovement(2, 4);

        manager.recordAccusation(2, false);

        manager.recordTurn(3);

        manager.setCharacter(

                3,

                "Character D"
        );

        pass("Gameplay statistics recorded");

        // TEST 8: INVALID PLAYER

        expectFailure(() ->

                manager.recordTurn(999)
        );

        pass("Unknown participant rejected");

        // TEST 9: NEGATIVE VALUES

        expectFailure(() ->

                manager.recordMovement(1, -5)
        );

        expectFailure(() ->

                manager.addPoints(1, -100)
        );

        pass("Negative statistics rejected");

        // TEST 10: INVALID WINNER

        expectFailure(() ->

                manager.completeMatch(

                        matchId,

                        999,

                        "Character A",

                        "Weapon A",

                        "Room A"
                )
        );

        check(

                manager.isInProgress(),

                "Invalid winner affected match state"
        );

        pass("Unknown winner rejected");

        // TEST 11: COMPLETE MATCH

        NetworkMatchResult result =
                manager.completeMatch(

                        matchId,

                        1,

                        "Character A",

                        "Weapon A",

                        "Room A"
                );

        check(

                result.matchId().equals(matchId)

                        && result.winnerId().equals(player1)

                        && result.getPlayerCount() == 3

                        && manager.getState()
                        == NetworkMatchManager
                        .State.COMPLETED,

                "Invalid completed match"
        );

        pass("Match completed successfully");

        // TEST 12: WINNER STATISTICS

        NetworkPlayerResult winner =
                result.getWinner();

        check(

                winner.hasWon()

                        && winner.turnsPlayed() == 2

                        && winner.diceRolls() == 2

                        && winner.movesMade() == 8

                        && winner.suggestionsMade() == 1

                        && winner.correctAccusations() == 1

                        && winner.incorrectAccusations() == 0

                        && winner.pointsEarned() == 100,

                "Incorrect winner statistics"
        );

        pass("Winner statistics verified");

        // TEST 13: OTHER PARTICIPANTS

        NetworkPlayerResult loser =
                result.getPlayer(player2)
                        .orElseThrow();

        check(

                loser.hasLost()

                        && loser.turnsPlayed() == 1

                        && loser.movesMade() == 4

                        && loser.incorrectAccusations() == 1,

                "Incorrect loser statistics"
        );

        check(

                result.getPlayer(player3)

                        .orElseThrow()

                        .characterName()

                        .equals("Character D"),

                "Character change not preserved"
        );

        pass("Other participant statistics verified");

        // TEST 14: DUPLICATE COMPLETION

        NetworkMatchResult duplicate =
                manager.completeMatch(

                        matchId,

                        1,

                        "Character A",

                        "Weapon A",

                        "Room A"
                );

        check(

                duplicate.equals(result),

                "Duplicate completion changed result"
        );

        pass("Duplicate completion is safe");

        // TEST 15: CONFLICTING COMPLETION

        expectFailure(() ->

                manager.completeMatch(

                        matchId,

                        2,

                        "Character A",

                        "Weapon A",

                        "Room A"
                )
        );

        pass("Conflicting completion rejected");

        // TEST 16: POST-MATCH MODIFICATION

        expectFailure(() ->

                manager.recordTurn(1)
        );

        pass("Post-match modification rejected");

        // TEST 17: IMMUTABLE RESULT

        expectFailure(() ->

                result.players().clear()
        );

        pass("Completed result is immutable");

        // TEST 18: SERIALIZATION

        ByteArrayOutputStream buffer =
                new ByteArrayOutputStream();

        try (ObjectOutputStream output =
                     new ObjectOutputStream(buffer)) {

            output.writeObject(result);
        }

        NetworkMatchResult restored;

        try (ObjectInputStream input =
                     new ObjectInputStream(

                             new ByteArrayInputStream(

                                     buffer.toByteArray()
                             )
                     )) {

            restored =
                    (NetworkMatchResult)
                            input.readObject();
        }

        check(

                result.equals(restored),

                "Serialization changed match result"
        );

        pass("Network serialization verified");

        // TEST 19: NEW MATCH

        UUID secondMatch =
                manager.startMatch(

                        player1,

                        players
                );

        check(

                !matchId.equals(secondMatch)

                        && manager.isInProgress()

                        && manager.getCompletedResult()
                        .isEmpty(),

                "New match did not reset state"
        );

        pass("New match has independent UUID");

        // TEST 20: STALE MATCH ID

        expectFailure(() ->

                manager.completeMatch(

                        matchId,

                        1,

                        null,

                        null,

                        null
                )
        );

        pass("Stale match completion rejected");

        // TEST 21: MATCH CANCELLATION

        check(

                manager.cancelMatch(secondMatch)

                        && manager.getState()
                        == NetworkMatchManager
                        .State.CANCELLED

                        && !manager.cancelMatch(secondMatch),

                "Match cancellation failed"
        );

        pass("Match cancelled safely");

        // TEST 22: CANCELLED MATCH COMPLETION

        expectFailure(() ->

                manager.completeMatch(

                        secondMatch,

                        1,

                        null,

                        null,

                        null
                )
        );

        pass("Cancelled match cannot complete");

        // TEST 23: RESET

        manager.reset();

        check(

                manager.getState()
                        == NetworkMatchManager.State.IDLE

                        && manager.getMatchId() == null

                        && manager.getPlayerCount() == 0,

                "Manager reset failed"
        );

        pass("Manager reset successful");

        System.out.println(
                "All assertions passed."
        );
    }

    private static void expectFailure(
            Runnable operation
    ) {

        try {

            operation.run();

        } catch (IllegalArgumentException
                 | IllegalStateException
                 | UnsupportedOperationException e) {

            return;
        }

        throw new AssertionError(
                "Expected operation to fail."
        );
    }

    private static void check(

            boolean condition,

            String message

    ) {

        if (!condition) {

            throw new AssertionError(message);
        }
    }

    private static void pass(String message) {

        System.out.println(
                "[PASS] " + message
        );
    }
}
