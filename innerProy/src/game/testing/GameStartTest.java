
package game.testing;

import game.core.Game;
import game.core.GameStartResult;
import game.history.GameEvent;

import model.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class GameStartTest {

    private int passed = 0;
    private int failed = 0;

    // EJECUTAR TODOS LOS TESTS

    public static void main(String[] args) {

        GameStartTest test = new GameStartTest();

        test.runAllTests();
    }

    private void runAllTests() {

        System.out.println("\n===== GAME START TESTS =====\n");

        runTest("Start without players", this::testNoPlayers);

        runTest("Start with one player", this::testOnePlayer);

        runTest("Start with two players", this::testTwoPlayers);

        runTest("Card distribution", this::testCardDistribution);

        runTest("First player selection", this::testFirstPlayer);

        runTest("Game state initialization", this::testGameState);

        runTest("Round manager initialization", this::testRoundManager);

        runTest("Game started event", this::testGameStartedEvent);

        runTest("Start game twice", this::testStartTwice);

        runTest("Four players", this::testFourPlayers);

        runTest("Five players", this::testFivePlayers);

        runTest("Seven players", this::testSevenPlayers);

        runTest("Independent games", this::testIndependentGames);

        printResults();
    }


    // 1. NO INICIAR SIN JUGADORES

    private void testNoPlayers() {

        Game game = new Game();

        GameStartResult result = game.startGame();

        assertFalse(
                result.isSuccessful(),
                "A game should not start without players"
        );

        assertFalse(
                game.isInitialized(),
                "The game should not be initialized"
        );
    }


    // 2. NO INICIAR CON UN SOLO JUGADOR

    private void testOnePlayer() {

        Game game = new Game();

        addPlayer(game, "David");

        GameStartResult result = game.startGame();

        assertFalse(
                result.isSuccessful(),
                "A game should not start with one player"
        );
    }


    // 3. INICIAR CON DOS JUGADORES

    private void testTwoPlayers() {

        Game game = createGameWithPlayers(2);

        GameStartResult result = game.startGame();

        assertTrue(
                result.isSuccessful(),
                "The game should start with two players"
        );

        assertTrue(
                game.isInitialized(),
                "The game should be initialized"
        );
    }


    // 4. COMPROBAR REPARTO DE CARTAS

    private void testCardDistribution() {

        Game game = new Game();

        List<List<Card>> playerHands = new ArrayList<>();

        for (int i = 0; i < 2; i++) {

            List<Card> hand = new ArrayList<>();

            playerHands.add(hand);

            Player player = createPlayer(
                    "Player " + i,
                    hand
            );

            game.addPlayer(player);
        }

        GameStartResult result = game.startGame();

        assertTrue(
                result.isSuccessful(),
                "The game should start successfully"
        );

        for (int i = 0; i < playerHands.size(); i++) {

            assertEquals(
                    4,
                    playerHands.get(i).size(),
                    "Each player should receive 4 cards"
            );
        }

        assertEquals(
                11,
                game.getClues().size(),
                "There should be 11 remaining clue cards"
        );
    }


    // 5. COMPROBAR PRIMER JUGADOR

    private void testFirstPlayer() {

        Game game = createGameWithPlayers(2);

        Player expectedFirstPlayer = game.getPlayers().get(0);

        GameStartResult result = game.startGame();

        assertTrue(
                result.isSuccessful(),
                "The game should start"
        );

        assertTrue(
                game.getCurrentPlayer() == expectedFirstPlayer,
                "The first player should be the first player in the list"
        );
    }


    // 6. COMPROBAR GAME STATE

    private void testGameState() {

        Game game = createGameWithPlayers(2);

        game.startGame();

        assertTrue(
                game.getGameState() != null,
                "GameState should be created"
        );

        assertTrue(
                game.getGameState().isStarted(),
                "GameState should be running"
        );

        assertFalse(
                game.getGameState().isFinished(),
                "GameState should not be finished"
        );
    }


    // 7. COMPROBAR ROUND MANAGER

    private void testRoundManager() {

        Game game = createGameWithPlayers(2);

        game.startGame();

        assertTrue(
                game.getRoundManager() != null,
                "RoundManager should be created"
        );

        assertTrue(
                game.getRoundManager().areTurnsActive(),
                "Turns should be active"
        );

        assertTrue(
                game.getRoundManager().getCurrentPlayer()
                        == game.getPlayers().get(0),
                "The first player should have the first turn"
        );
    }


    // 8. COMPROBAR EVENTO GAME_STARTED

    private void testGameStartedEvent() {

        Game game = createGameWithPlayers(2);

        game.startGame();

        List<GameEvent> events =
                game.getGameHistory().getEventsOfType(
                        GameEvent.Type.GAME_STARTED
                );

        assertEquals(
                1,
                events.size(),
                "The history should contain one GAME_STARTED event"
        );
    }


    // 9. NO PERMITIR INICIAR DOS VECES

    private void testStartTwice() {

        Game game = createGameWithPlayers(2);

        GameStartResult firstResult = game.startGame();

        assertTrue(
                firstResult.isSuccessful(),
                "The first initialization should succeed"
        );

        GameStartResult secondResult = game.startGame();

        assertFalse(
                secondResult.isSuccessful(),
                "The second initialization should fail"
        );

        assertEquals(
                1,
                game.getGameHistory().getEventsOfType(
                        GameEvent.Type.GAME_STARTED
                ).size(),
                "There should only be one GAME_STARTED event"
        );
    }


    // 10. COMPROBAR CUATRO JUGADORES

    private void testFourPlayers() {

        Game game = createGameWithPlayers(4);

        GameStartResult result = game.startGame();

        assertTrue(
                result.isSuccessful(),
                "Four players should be able to start"
        );

        assertEquals(
                4,
                game.getPlayers().size(),
                "There should be four players"
        );

        assertEquals(
                3,
                game.getClues().size(),
                "There should be three remaining cards"
        );
    }


    // 11. COMPROBAR CINCO JUGADORES

    private void testFivePlayers() {

        Game game = createGameWithPlayers(5);

        GameStartResult result = game.startGame();

        assertFalse(
                result.isSuccessful(),
                "Five players cannot receive four cards each with the current deck"
        );
    }


    // 12. COMPROBAR SIETE JUGADORES

    private void testSevenPlayers() {

        Game game = createGameWithPlayers(7);

        GameStartResult result = game.startGame();

        assertFalse(
                result.isSuccessful(),
                "The game should not allow seven players"
        );
    }


    // 13. COMPROBAR INDEPENDENCIA ENTRE PARTIDAS

    private void testIndependentGames() {

        Game game1 = createGameWithPlayers(2);

        Game game2 = createGameWithPlayers(2);

        GameStartResult result1 = game1.startGame();

        GameStartResult result2 = game2.startGame();

        assertTrue(
                result1.isSuccessful(),
                "First game should start"
        );

        assertTrue(
                result2.isSuccessful(),
                "Second game should start"
        );

        assertEquals(
                2,
                game1.getPlayers().size(),
                "First game should have two players"
        );

        assertEquals(
                2,
                game2.getPlayers().size(),
                "Second game should have two players"
        );

        assertEquals(
                1,
                game1.getGameHistory().size(),
                "First game should have its own history"
        );

        assertEquals(
                1,
                game2.getGameHistory().size(),
                "Second game should have its own history"
        );
    }


    // MÉTODOS AUXILIARES


    // CREAR PARTIDA CON UN NÚMERO DE JUGADORES

    private Game createGameWithPlayers(int amount) {

        Game game = new Game();

        for (int i = 0; i < amount; i++) {

            addPlayer(game, "Player " + i);
        }

        return game;
    }


    // AÑADIR JUGADOR

    private void addPlayer(Game game, String name) {

        List<Card> cards = new ArrayList<>();

        Player player = createPlayer(name, cards);

        game.addPlayer(player);
    }


    // CREAR JUGADOR

    private Player createPlayer(
            String name,
            List<Card> cards
    ) {

        Actor actor = new Actor(name, Color.BLUE);

        return new Player(
                actor,
                cards,
                0,
                name
        );
    }


    // EJECUTAR TEST

    private void runTest(String name, Runnable test) {

        try {

            test.run();

            passed++;

            System.out.println("[PASS] " + name);

        } catch (Throwable exception) {

            failed++;

            System.out.println("[FAIL] " + name);

            System.out.println("       " + exception.getMessage());
        }
    }


    // ASSERT TRUE

    private void assertTrue(boolean condition, String message) {

        if (!condition) {

            throw new AssertionError(message);
        }
    }


    // ASSERT FALSE

    private void assertFalse(boolean condition, String message) {

        if (condition) {

            throw new AssertionError(message);
        }
    }


    // ASSERT EQUALS

    private void assertEquals(
            int expected,
            int actual,
            String message
    ) {

        if (expected != actual) {

            throw new AssertionError(
                    message +
                            " | Expected: " + expected +
                            " | Actual: " + actual
            );
        }
    }


    // RESULTADOS

    private void printResults() {

        System.out.println("\n===== RESULTS =====");

        System.out.println("Passed: " + passed);

        System.out.println("Failed: " + failed);

        System.out.println(
                "Total: " + (passed + failed)
        );
    }
}
