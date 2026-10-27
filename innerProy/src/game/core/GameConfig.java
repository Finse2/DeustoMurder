
package game.core;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public final class GameConfig {

    public static final int CARDS_PER_PLAYER = 4;

    public static final List<String> ACTOR_NAMES = Arrays.asList(
            "Garaizar",
            "Luka",
            "David",
            "Andrada",
            "Antal",
            "Bringas"
    );

    public static final List<Color> PLAYER_COLORS = Arrays.asList(
            Color.PINK,
            Color.MAGENTA,
            Color.BLUE,
            Color.RED,
            Color.GREEN,
            Color.YELLOW
    );

    public static final List<String> WEAPON_NAMES = Arrays.asList(
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
