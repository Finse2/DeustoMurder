
package game.core;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public final class GameConfig {

    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 6;
    public static final int CARDS_PER_PLAYER = 4;
    public static final int DICE_MIN = 1;
    public static final int DICE_MAX = 6;
    public static final int NUMBER_OF_DICES = 2;


    public static final List<String> ACTOR_NAMES = List.of(
            "Garaizar",
            "Luka",
            "David",
            "Andrada",
            "Antal",
            "Bringas"
    );

    public static final List<Color> PLAYER_COLORS = List.of(
            Color.PINK,
            Color.MAGENTA,
            Color.BLUE,
            Color.RED,
            Color.GREEN,
            Color.YELLOW
    );

    public static final List<String> WEAPON_NAMES = List.of(
            "Machete",
            "Tiza",
            "Teclado",
            "Cable",
            "Silla",
            "Destornillador"
    );

    private GameConfig() {
    }
}
