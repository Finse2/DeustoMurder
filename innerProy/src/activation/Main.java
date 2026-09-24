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

    static {
        resetListContents();
    }

    public static void main(String[] args) {
        new Window(rooms);
    }

    private record rawActor(
            String actorName,
            Color actorsColor
    ) {}

    private record rawRoom(
            String roomName,
            int[] area,
            List<Point> ignoredPoints
    ) {}

    public static void resetListContents() {

        List<rawActor> rawActors = List.of(
                new rawActor("Garaizar", Color.PINK),
                new rawActor("Luka", Color.MAGENTA),
                new rawActor("David", Color.BLUE),
                new rawActor("Andrada", Color.RED),
                new rawActor("Antal", Color.GREEN),
                new rawActor("Bringas", Color.YELLOW)
        );

        String[] rawWeapons = {
                "Machete",
                "Tiza",
                "Teclado",
                "Cable",
                "Silla",
                "Destornillador"
        };

        List<rawRoom> rawRooms = List.of(

                new rawRoom("Cry", new int[]{0, 0, 7, 4},
                        List.of()),

                new rawRoom("Baños", new int[]{9, 0, 5, 6},
                        List.of()
                ),

                new rawRoom("Cafeteria", new int[]{16, 0, 7, 5},
                        List.of()
                ),

                new rawRoom("Maquinas", new int[]{0, 6, 7, 4},
                        List.of(
                                new Point(0, 6),
                                new Point(0, 9),
                                new Point(6, 6),
                                new Point(6, 9)
                        )
                ),

                new rawRoom("Laboratorio", new int[]{9, 7, 5, 6},
                        List.of()
                ),

                new rawRoom("DeustoTech", new int[]{16, 7, 7, 7},
                        List.of(
                                new Point(16, 13),
                                new Point(17, 13)
                        )
                ),

                new rawRoom("Aula 1", new int[]{0, 11, 6, 4},
                        List.of()
                ),

                new rawRoom("Aula 2", new int[]{0, 17, 6, 5},
                        List.of(
                                new Point(0, 17),
                                new Point(5, 17)
                        )
                ),

                new rawRoom("Room 9", new int[]{8, 15, 7, 7},
                        List.of(
                                new Point(8, 21),
                                new Point(14, 21)
                        )
                ),

                new rawRoom("Room 10", new int[]{17, 16, 6, 6},
                        List.of()
                )
        );

        actors.clear();
        weapons.clear();
        rooms.clear();

        for (rawActor rawActor : rawActors) {

            actors.add(new Actor(rawActor.actorName(), rawActor.actorsColor()));
        }

        for (String weaponName : rawWeapons) {

            weapons.add(new Weapon(weaponName));
        }

        for (rawRoom rawRoom : rawRooms) {

            int[] area = rawRoom.area();

            Room room = new Room(rawRoom.roomName());

            room.occupyRectangle(area[0], area[1], area[2], area[3]);

            for (Point ignoredPoint : rawRoom.ignoredPoints()) {
                room.free(ignoredPoint.x, ignoredPoint.y);
            }

            rooms.add(room);
        }
    }
}