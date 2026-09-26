package activation;

import model.*;

import javax.swing.*;

import java.awt.Color;
import java.awt.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {

    static List<Actor> actors =
            new ArrayList<>();

    static List<Room> rooms =
            new ArrayList<>();

    static List<Weapon> weapons =
            new ArrayList<>();

    static List<Card> solution =
            new ArrayList<>();

    static List<Card> Playable_Cards =
            new ArrayList<>();

    static List<Card> visible_cards =
            new ArrayList<>();

    static List<Player> players =
            new ArrayList<>();

    public static void main(String[] args) {

        resetListContents();

        SwingUtilities.invokeLater(
                () -> new Window(rooms)
        );
    }

    /**
     * Resets the lists to the preset
     * actors, weapons and rooms.
     */
    public static void resetListContents() {

        List<String> actorNames =
                List.of(
                        "Garaizar",
                        "Luka",
                        "David",
                        "Andrada",
                        "Antal",
                        "Bringas"
                );

        List<Color> actorColors =
                List.of(
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

        record RawRoom(
                String name,
                int[] area,
                List<Point> ignoredPoints
        ) {
        }

        List<RawRoom> rawrooms =
                List.of(

                        new RawRoom(
                                "Cry",
                                new int[]{
                                        0,
                                        0,
                                        7,
                                        4
                                },
                                List.of()
                        ),

                        new RawRoom(
                                "Baños",
                                new int[]{
                                        9,
                                        0,
                                        5,
                                        6
                                },
                                List.of()
                        ),

                        new RawRoom(
                                "Cafeteria",
                                new int[]{
                                        16,
                                        0,
                                        7,
                                        5
                                },
                                List.of()
                        ),

                        new RawRoom(
                                "Maquinas",
                                new int[]{
                                        0,
                                        6,
                                        7,
                                        4
                                },
                                List.of(
                                        new Point(
                                                6,
                                                6
                                        ),
                                        new Point(
                                                6,
                                                9
                                        )
                                )
                        ),

                        new RawRoom(
                                "Laboratorio",
                                new int[]{
                                        9,
                                        7,
                                        5,
                                        6
                                },
                                List.of()
                        ),

                        new RawRoom(
                                "DeustoTech",
                                new int[]{
                                        16,
                                        7,
                                        7,
                                        7
                                },
                                List.of(
                                        new Point(
                                                16,
                                                13
                                        ),
                                        new Point(
                                                17,
                                                13
                                        )
                                )
                        ),

                        new RawRoom(
                                "Aula 1",
                                new int[]{
                                        0,
                                        11,
                                        6,
                                        4
                                },
                                List.of()
                        ),

                        new RawRoom(
                                "Aula 2",
                                new int[]{
                                        0,
                                        17,
                                        6,
                                        5
                                },
                                List.of(
                                        new Point(
                                                0,
                                                17
                                        ),
                                        new Point(
                                                5,
                                                17
                                        )
                                )
                        ),

                        new RawRoom(
                                "Room 9",
                                new int[]{
                                        8,
                                        15,
                                        7,
                                        7
                                },
                                List.of(
                                        new Point(
                                                8,
                                                21
                                        ),
                                        new Point(
                                                14,
                                                21
                                        )
                                )
                        ),

                        new RawRoom(
                                "Room 10",
                                new int[]{
                                        17,
                                        16,
                                        6,
                                        6
                                },
                                List.of()
                        )
                );

        actors.clear();
        weapons.clear();
        rooms.clear();

        solution.clear();
        Playable_Cards.clear();

        visible_cards.clear();
        players.clear();

        for (
                int i = 0;
                i < actorNames.size();
                i++
        ) {

            actors.add(
                    new Actor(
                            actorNames.get(i),
                            actorColors.get(i)
                    )
            );
        }

        for (String name : rawweapons) {

            weapons.add(
                    new Weapon(name)
            );
        }

        for (RawRoom rawRoom : rawrooms) {

            int[] area =
                    rawRoom.area();

            Room room =
                    new Room(
                            rawRoom.name()
                    )
                            .occupyRectangle(
                                    area[0],
                                    area[1],
                                    area[2],
                                    area[3]
                            );

            for (
                    Point ignoredPoint
                    : rawRoom.ignoredPoints()
            ) {

                room.free(
                        ignoredPoint.x,
                        ignoredPoint.y
                );
            }

            rooms.add(room);
        }
    }

    public static void gameStart() {

        Collections.shuffle(actors);
        Collections.shuffle(weapons);
        Collections.shuffle(rooms);

        Actor solution_actor =
                actors.getFirst();

        actors.removeFirst();

        Weapon solution_weapon =
                weapons.getFirst();

        weapons.removeFirst();

        Room solution_room =
                rooms.getFirst();

        rooms.removeFirst();

        solution.add(solution_actor);
        solution.add(solution_weapon);
        solution.add(solution_room);

        Playable_Cards.addAll(actors);
        Playable_Cards.addAll(weapons);
        Playable_Cards.addAll(rooms);

        Collections.shuffle(
                Playable_Cards
        );

        if (
                players.size() * 3
                        > Playable_Cards.size()
        ) {

            System.out.println(
                    "Error too many players!!!"
            );

            return;
        }

        for (Player player : players) {

            for (int i = 0; i < 3; i++) {

                Card card =
                        Playable_Cards.removeFirst();

                player.addCard(card);
            }
        }

        if (!Playable_Cards.isEmpty()) {

            visible_cards.addAll(
                    Playable_Cards
            );

            Playable_Cards.clear();
        }
    }

    public static void createDefaultLayout() {
        resetListContents();
    }
}