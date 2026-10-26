package game;

import model.Card;

import java.util.List;

public class AccusationValidator {

    // MÉTODO QUE COMPRUEBA SI LA ACUSACIÓN ES CORRECTA O NO
    public boolean isCorrectAccusation (Card room, Card actor, Card weapon, List<Card> solution) {
        if (room == null || actor == null || weapon == null || solution == null || solution.size() != 3) {
            return false;
        }

        return solution.contains(room) && solution.contains(actor) && solution.contains(weapon);
    }
}
