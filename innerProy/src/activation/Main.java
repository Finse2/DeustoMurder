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
                "Miss Scarlett",
                "Colonel Mustard",
                "Mrs White",
                "Reverend Green",
                "Mrs Peacock",
                "Professor Plum"
        };

        String[] rawweapons = {
                "Candlestick",
                "Dagger",
                "Lead Pipe",
                "Revolver",
                "Rope",
                "Spanner"
        };

        String[] rawrooms = {
                "Kitchen",
                "Ballroom",
                "Conservatory",
                "Dining Room",
                "Billiard Room",
                "Library",
                "Lounge",
                "Hall",
                "Study"
        };

        actors.clear();
        weapons.clear();
        rooms.clear();

        for (String name : rawactors) {
            actors.add(new Actor(name, null));
        }

        for (String name : rawweapons) {
            weapons.add(new Weapon(name));
        }

        for (String name : rawrooms) {
            rooms.add(new Room(name));
        }
    }


}