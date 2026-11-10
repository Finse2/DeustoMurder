
package mockClasses.testing;

import game.cards.DeckManager;
import model.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeckManagerTest {

    private int passed = 0;
    private int failed = 0;

    // EJECUTAR TODOS LOS TESTS

    public static void main(String[] args) {

        DeckManagerTest test = new DeckManagerTest();

        test.runAllTests();
    }

    private void runAllTests() {

        System.out.println("\n===== DECK MANAGER TESTS =====\n");

        runTest("Deck creation", this::testDeckCreation);

        runTest("Original list is copied", this::testOriginalListIsCopied);

        runTest("Shuffle deck", this::testShuffleDeck);

        runTest("Deal cards", this::testDealCards);

        runTest("Remaining cards", this::testRemainingCards);

        runTest("Not enough cards", this::testNotEnoughCards);

        runTest("Null deck", this::testNullDeck);

        runTest("Null players", this::testNullPlayers);

        printResults();
    }


    // 1. COMPROBAR CREACIÓN DEL MAZO

    private void testDeckCreation() {

        List<Card> cards = createCards(10);

        DeckManager manager = new DeckManager(cards);

        assertEquals(
                10,
                manager.getDeck().size(),
                "The deck should contain 10 cards"
        );
    }


    // 2. COMPROBAR QUE EL MAZO COPIA LA LISTA ORIGINAL

    private void testOriginalListIsCopied() {

        List<Card> cards = createCards(10);

        DeckManager manager = new DeckManager(cards);

        cards.clear();

        assertEquals(
                10,
                manager.getDeck().size(),
                "Changing the original list should not affect the deck"
        );
    }


    // 3. COMPROBAR EL BARAJADO

    private void testShuffleDeck() {

        List<Card> originalCards = createCards(10);

        DeckManager manager = new DeckManager(originalCards);

        manager.shuffleDeck();

        List<Card> shuffledCards = manager.getDeck();

        assertEquals(
                originalCards.size(),
                shuffledCards.size(),
                "The number of cards should remain the same"
        );

        for (Card card : originalCards) {

            assertTrue(
                    shuffledCards.contains(card),
                    "The shuffled deck must contain all original cards"
            );
        }
    }


    // 4. COMPROBAR EL REPARTO DE CARTAS

    private void testDealCards() {

        List<Card> cards = createCards(12);

        DeckManager manager = new DeckManager(cards);

        List<Card> player1Cards = new ArrayList<>();

        List<Card> player2Cards = new ArrayList<>();

        Player player1 = createPlayer("David", player1Cards);

        Player player2 = createPlayer("Andrada", player2Cards);

        List<Player> players = Arrays.asList(player1, player2);

        manager.shuffleDeck();

        manager.dealCards(players);

        assertEquals(
                4,
                player1Cards.size(),
                "Player 1 should receive 4 cards"
        );

        assertEquals(
                4,
                player2Cards.size(),
                "Player 2 should receive 4 cards"
        );

        assertEquals(
                4,
                manager.getDeck().size(),
                "The deck should contain 4 remaining cards"
        );
    }


    // 5. COMPROBAR LAS CARTAS SOBRANTES

    private void testRemainingCards() {

        List<Card> cards = createCards(10);

        DeckManager manager = new DeckManager(cards);

        List<Card> playerCards = new ArrayList<>();

        Player player = createPlayer("David", playerCards);

        manager.dealCards(List.of(player));

        List<Card> remainingCards = manager.getRemainingCards();

        assertEquals(
                6,
                remainingCards.size(),
                "There should be 6 remaining cards"
        );

        assertEquals(
                0,
                manager.getDeck().size(),
                "The deck should be empty after extracting remaining cards"
        );

        assertEquals(
                0,
                manager.getRemainingCards().size(),
                "The remaining cards should only be extracted once"
        );
    }


    // 6. COMPROBAR QUÉ PASA SI NO HAY SUFICIENTES CARTAS

    private void testNotEnoughCards() {

        List<Card> cards = createCards(7);

        DeckManager manager = new DeckManager(cards);

        List<Card> player1Cards = new ArrayList<>();

        List<Card> player2Cards = new ArrayList<>();

        Player player1 = createPlayer("David", player1Cards);

        Player player2 = createPlayer("Andrada", player2Cards);

        assertThrows(
                IllegalStateException.class,
                () -> manager.dealCards(
                        Arrays.asList(player1, player2)
                ),
                "An exception should occur when there are not enough cards"
        );

        assertEquals(
                0,
                player1Cards.size(),
                "No cards should be dealt when the deck is insufficient"
        );
    }


    // 7. COMPROBAR MAZO NULO

    private void testNullDeck() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new DeckManager(null),
                "Creating a deck with null should throw an exception"
        );
    }


    // 8. COMPROBAR LISTA DE JUGADORES NULA

    private void testNullPlayers() {

        DeckManager manager = new DeckManager(createCards(10));

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.dealCards(null),
                "The players list cannot be null"
        );
    }


    // MÉTODOS AUXILIARES


    // CREAR CARTAS DE PRUEBA

    private List<Card> createCards(int amount) {

        List<Card> cards = new ArrayList<>();

        for (int i = 0; i < amount; i++) {

            cards.add(new Room("Room " + i));
        }

        return cards;
    }


    // CREAR JUGADOR DE PRUEBA

    private Player createPlayer(
            String name,
            List<Card> playerCards
    ) {

        Actor actor = new Actor(name, Color.BLUE);

        return new Player(
                actor,
                playerCards,
                0,
                name
        );
    }


    // EJECUTAR UN TEST

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


    // COMPROBAR IGUALDAD

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


    // COMPROBAR TRUE

    private void assertTrue(boolean condition, String message) {

        if (!condition) {

            throw new AssertionError(message);
        }
    }


    // COMPROBAR EXCEPCIONES

    private void assertThrows(
            Class<? extends Throwable> expected,
            Runnable action,
            String message
    ) {

        try {

            action.run();

        } catch (Throwable exception) {

            if (expected.isInstance(exception)) {
                return;
            }

            throw new AssertionError(
                    message + " | Unexpected exception: " + exception
            );
        }

        throw new AssertionError(
                message + " | No exception was thrown"
        );
    }


    // MOSTRAR RESULTADOS

    private void printResults() {

        System.out.println("\n===== RESULTS =====");

        System.out.println("Passed: " + passed);

        System.out.println("Failed: " + failed);

        System.out.println(
                "Total: " + (passed + failed)
        );
    }
}
