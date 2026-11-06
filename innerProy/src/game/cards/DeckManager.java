
package game.cards;

import model.Card;
import model.Player;
import game.core.GameConfig;

import java.util.ArrayList;
import java.util.List;

public class DeckManager {

    private final List<Card> remainingCards;
    private final List<Card> deck;

    public DeckManager(List<Card> cards) {
        if (cards == null) {
            throw new IllegalArgumentException("The card list cannot be null");
        }

        this.deck = new ArrayList<>(cards);
        this.remainingCards = new ArrayList<>();
    }

    // BARAJAR CARTAS

    public void shuffleDeck() {
        java.util.Collections.shuffle(deck);
    }

    // REPARTIR CARTAS

    public void dealCards(List<Player> players) {
        if (players == null) {
            throw new IllegalArgumentException("The player list cannot be null");
        }

        int requiredCards = players.size() * GameConfig.CARDS_PER_PLAYER;

        if (deck.size() < requiredCards) {
            throw new IllegalArgumentException("The card list cannot be empty");
        }

        for (Player player : players) {
            if (player == null) {
                throw new IllegalArgumentException("The player list cannot be null");
            }

            for (int i = 0; i < GameConfig.CARDS_PER_PLAYER; i++) {
                Card card = deck.removeFirst();
                remainingCards.add(card);
            }
        }
    }

    // OBTENER LAS CARTAS SOBRANTES

    public List<Card> getRemainingCards() {
        remainingCards.clear();
        remainingCards.addAll(deck);
        deck.clear();

        return new ArrayList<>(remainingCards);
    }

    // CONSULTAR EL MAZO

    public List<Card> getDeck() {

        return new ArrayList<>(deck);
    }

    public int getRemainingDeckSize() {
        return deck.size();
    }
}
