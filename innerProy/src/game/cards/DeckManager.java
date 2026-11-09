
package game.cards;

import game.core.GameConfig;
import model.Card;
import model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeckManager {

    private final List<Card> deck;
    private boolean dealt;

    public DeckManager(List<Card> cards) {
        if (cards == null || cards.contains(null)) {
            throw new IllegalArgumentException("The deck cannot be null or contain null cards");
        }

        deck = new ArrayList<>(cards);
        dealt = false;
    }

    // BARAJAR EL MAZO REAL (NO LA LISTA ORIGINAL)

    public void shuffleDeck() {
        if (dealt) {
            throw new IllegalStateException("Cannot shuffle after dealing cards");
        }
        Collections.shuffle(deck);
    }

    // REPARTIR CUATRO CARTAS POR JUGADOR, UNA POR RONDA

    public void dealCards(List<Player> players) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("The player list cannot be null or empty");
        }
        if (dealt) {
            throw new IllegalStateException("Cards have already been dealt");
        }

        Set<Player> uniquePlayers = new HashSet<>();
        for (Player player : players) {
            if (player == null || !uniquePlayers.add(player)) {
                throw new IllegalArgumentException("Players cannot be null or repeated");
            }
            if (!player.getCards().isEmpty()) {
                throw new IllegalStateException("Players must have empty hands before dealing");
            }
        }

        int requiredCards = players.size() * GameConfig.CARDS_PER_PLAYER;
        if (deck.size() < requiredCards) {
            throw new IllegalStateException(
                    "Not enough cards: required " + requiredCards + ", available " + deck.size()
            );
        }

        for (int i = 0; i < GameConfig.CARDS_PER_PLAYER; i++) {
            for (Player player : players) {
                player.addCard(deck.removeFirst());
            }
        }

        dealt = true;
    }

    // EXTRAER UNA ÚNICA VEZ LAS CARTAS QUE NO FUERON REPARTIDAS

    public List<Card> getRemainingCards() {
        List<Card> remaining = new ArrayList<>(deck);
        deck.clear();
        return remaining;
    }

    // DEVOLVER UNA COPIA DEL MAZO ACTUAL

    public List<Card> getDeck() {
        return new ArrayList<>(deck);
    }

    public int getRemainingDeckSize() {
        return deck.size();
    }
}
