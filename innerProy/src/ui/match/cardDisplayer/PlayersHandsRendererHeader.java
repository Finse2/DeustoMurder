package ui.match.cardDisplayer;

import model.Card;

/**
 Short explanation: it specifies what type of card is part of a certain group and its max rotation <br>
 <br>
Record containing two primary parameters, a child Class reference that must extend Card. <br>
An Integer that specifies weather the system should:<br>
 <br>
    -> null    = automatically find the best rotation<br>
    -> int val = should instead use a degree of rotation provided by the user<br>
 */
public record PlayersHandsRendererHeader(Class<? extends Card> header, Integer maxRotationDegrees) {

    // Manual rotation  - by manually passing a concrete number
    public PlayersHandsRendererHeader(Class<? extends Card> header, int maxRotationDegrees) {
        this(header, Integer.valueOf(maxRotationDegrees));
    }

    // Automatic rotation
    public PlayersHandsRendererHeader(Class<? extends Card> header) {
        this(header, 0);
    }

    // Default
    public PlayersHandsRendererHeader() {
        this(Card.class);
    }

    public boolean autoRotate() {
        return maxRotationDegrees == null;
    }
}