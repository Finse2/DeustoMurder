package game.dice;

import game.core.Game;
import game.core.GameConfig;

public class Dice {

    // IMPLEMENTACIONES:

    /*
    Generar resultado de una tirada
    Simular dos dados
    Devolver el resultado de cada dado
    Calcular la suma de los resultados
    Permitir consultar el resultado de la última tirada
    Respetar el rango de valores permitido para cada dado
     */

    private final GameRandomizer randomizer;

    private int lastRoll;

    private int firstDie;
    private int secondDie;

    public Dice () {
        this(new GameRandomizer());
    }

    public Dice (GameRandomizer randomizer) {

        if (randomizer == null) {
            throw new IllegalArgumentException("Randomizer cannot be null");
        }

        this.randomizer = randomizer;
    }

    // LANZAR LOS DOS DADOS

    public int roll() {
        firstDie = randomizer.nextInt(GameConfig.DICE_MIN, GameConfig.DICE_MAX);
        secondDie =  randomizer.nextInt(GameConfig.DICE_MIN, GameConfig.DICE_MAX);

        lastRoll = firstDie + secondDie;

        return lastRoll;
    }

    public int getFirstDie () {
        return firstDie;
    }

    public int getSecondDie () {
        return secondDie;
    }

    public int getLastRoll () {
        return lastRoll;
    }
}
