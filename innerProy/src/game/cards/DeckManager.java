
package game.cards;

import model.Card;
import model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeckManager {

    private static final int CARDS_PER_PLAYER = 4;

    private final List<Card> deck;
    private final List<Card> remainingCards;

    public DeckManager(List<Card> cards) {
        if (cards == null) {
            throw new IllegalArgumentException("The card list cannot be null");
        }

        this.deck = new ArrayList<>(cards);
        this.remainingCards = new ArrayList<>();
    }

    public void shuffleDeck() {
        Collections.shuffle(deck);
    }

    public void dealCards(List<Player> players) {
        if (players == null) {
            throw new IllegalArgumentException("The player list cannot be null");
        }

        for (Player player : players) {
            if (player == null) {
                throw new IllegalArgumentException("Players cannot be null");
            }

            for (int i = 0; i < CARDS_PER_PLAYER; i++) {
                if (deck.isEmpty()) {
                    throw new IllegalArgumentException(
                            "There are not enough cards to deal"
                    );
                }

                Card card = deck.remove(0);
                player.addCard(card);
            }
        }
    }

    public List<Card> getRemainingCards() {
        remainingCards.clear();
        remainingCards.addAll(deck);
        deck.clear();

        return new ArrayList<>(remainingCards);
    }

    public List<Card> getDeck() {
        return deck;
    }
}
