package activation;

import model.*;

import java.util.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static List<Actor> actors = new ArrayList<>();
    static List<Room> rooms = new ArrayList<>();
    static List<Weapon> weapons = new ArrayList<>();
    static List<Card> solution = new ArrayList<>();
    static List<Card> Playable_Cards = new ArrayList<>();
    static List<Card> visible_cards = new ArrayList<>();
    static List<Player> players = new ArrayList<>();


    public static void main(String[] args) {

        resetListContents(); //we change the values of the lists to their preset value

    }
    /**
     * reset to preset values (names) for the tree lists which represent the three main types of cards encountered throught the game
     * */
    public static void resetListContents() {

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
        solution.clear();
        Playable_Cards.clear();
        visible_cards.clear();
        players.clear();

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
    public static void gameStart() {

        Collections.shuffle(actors);
        Collections.shuffle(weapons);
        Collections.shuffle(rooms);

        Actor solution_actor = actors.getFirst();
        actors.removeFirst();

        Weapon solution_weapon = weapons.getFirst();
        weapons.removeFirst();

        Room solution_room = rooms.getFirst();
        rooms.removeFirst();

        solution.add(solution_actor);
        solution.add(solution_weapon);
        solution.add(solution_room);

        Playable_Cards.addAll(actors);
        Playable_Cards.addAll(weapons);
        Playable_Cards.addAll(rooms);

        Collections.shuffle(Playable_Cards);

        if ( players.size() * 3 > Playable_Cards.size()) {
            System.out.println("Error too many players!!!");
            return;
        }

        for (Player player : players) {
            for (int i = 0; i < 3; i++) {
                Card card = Playable_Cards.removeFirst();
                player.addCard(card);
            }
        }

        if (!Playable_Cards.isEmpty()) {
            visible_cards.addAll(Playable_Cards);
            Playable_Cards.clear();
        }










    }


    /**
     *
     * */
    public static void createDefaultLayout(){

    }


}