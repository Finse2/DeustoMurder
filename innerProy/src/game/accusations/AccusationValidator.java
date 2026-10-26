package game.accusations;

import model.Card;

import java.util.List;

public class AccusationValidator {

    public boolean isCorrectAccusation (Card room, Card actor, Card weapon, List<Card> solution) {
        if (room == null || actor == null || weapon == null || solution == null || solution.size() != 3) {
            return false;
        }

        return solution.contains(room) && solution.contains(actor) && solution.contains(weapon);
    }

    // FALTA POR AÑADIR:

    /*
    1. Comprobar las categorías de las cartas: una habitación, un personaje y un arma (no tres cartas cualquiera)
    2. Comprobar que la solución sea coherente
     */
}
