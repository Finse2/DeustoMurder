package activation;

import model.Actor;
import model.Room;
import model.Weapon;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static List<Actor> actors = new ArrayList<>();
    static List<Room> rooms = new ArrayList<>();
    static List<Weapon> weapons = new ArrayList<>();

    static {resetListContents();}


    public static void main(String[] args) {


        resetListContents(); //we change the values of the lists to their preset value

    }
    /**
     * reset to preset values (names) for the tree lists which represent the three main types of cards encountered throught the game
     * */
    public static void resetListContents(){



        List<String> actorNames = List.of(
                "Garaizar",
                "Luka",
                "David",
                "Andrada",
                "Antal",
                "Bringas"
        );

        List<Color> actorColors = List.of(
                Color.PINK,
                Color.MAGENTA,
                Color.BLUE,
                Color.RED,
                Color.GREEN,
                Color.YELLOW
        );

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

        for(int i = 0; i < actorNames.size(); i++){
            actors.add(new Actor(actorNames.get(i), actorColors.get(i)));
        }

        for (String name : rawweapons) {
            weapons.add(new Weapon(name));
        }

        for (String name : rawrooms) {
            rooms.add(new Room(name));
        }
    }


    /**
     *
     * */
    public static void createDefaultLayout(){

    }


}