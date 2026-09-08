package activation;

import model.Actor;
import model.Room;
import model.Weapon;

import java.util.ArrayList;
import java.util.List;

public class Main {

    static List<Actor> actors = new ArrayList<>();
    static List<Room> rooms = new ArrayList<>();
    static List<Weapon> weapons = new ArrayList<>();

    static {resetListContents();}


    public static void main(String[] args) {


    }
    /**
     * reset to preset values (names) for the tree lists which represent the three main types of cards encountered throught the game
     * */
    public static void resetListContents(){
        String[] rawactors = {
                "Garaizar", // rosa
                "Luka", // morado
                "David", // azul
                "Andrada", // rojo
                "Antal", // verde
                "Bringas" // amarillo
        };

        String[] rawweapons = {
                "Machete",
                "Tiza",
                "Teclado",
                "Cable",
                "Silla",
                "Destornillador"
        };

        String[] rawrooms = {
                "Cry",
                "Baños",
                "Cafeteria",
                "Maquinas",
                "Laboratorio",
                "DeustoTech",
                "Aula 1",
                "Aula 2"
        };

        actors.clear();
        weapons.clear();
        rooms.clear();

        for (String name : rawactors) {
            actors.add(new Actor(name));
        }

        for (String name : rawweapons) {
            weapons.add(new Weapon(name));
        }

        for (String name : rawrooms) {
            rooms.add(new Room(name));
        }
    }


}