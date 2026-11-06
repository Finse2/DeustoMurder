package game.dice;

import game.core.Game;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameRandomizer {

    // IMPLEMENTACIONES

    /*
    Generar índices aleatorios para seleccionar elementos
    Seleccionar una carta de cada para la solución secreta
    Barajar una baraja de cartas
    Generar resultados aleatorios reproducibles durante las pruebas
    Permitir usar una seed para repetir una secuencia aleatoria
    Proporcionar una fuente de aleatoriedad compartida a otros componentes
     */

    private final Random random;

    public GameRandomizer() {
        this.random = new Random();
    }

    public GameRandomizer (long seed) {
        this.random = new Random(seed);
    }

    // GENERAR UN NÚMERO RANDOM

    public int nextInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum cannot be greater than maximum");
        }

        return random.nextInt(max - min + 1) + min;
    }

    //SLECCIONAR UN NÚMERO RANDOM

    public <T> T selectRandom (List<T> elements) {
        if (elements == null || elements.isEmpty()) {
            throw new IllegalArgumentException("Elements cannot be null or empty");
        }

        int index = random.nextInt(elements.size());

        return elements.get(index);
    }

    //BARAJAR UNA LISTA

    public <T> void shuffle (List <T> elements) {

        if (elements == null || elements.isEmpty()) {throw new IllegalArgumentException("Cannot shuffle elements");
        }
        Collections.shuffle(elements);
    }

}
