package model.board;

import java.awt.Point;
import java.util.List;

/** Immutable blueprints for the room geometry and entrance locations. */
public final class BoardFactory {

    private BoardFactory() {
    }

    /** Returns a fresh blueprint array so each game can construct its own rooms. */
    public static RawRoom[] createRawRooms() {
        return new RawRoom[]{
                new RawRoom("Cry", new int[]{0, 0, 7, 4},
                        List.of(),
                        List.<Point[]>of(
                                new Point[]{
                                        new Point(7, 1),
                                        new Point(7, 2)
                                }
                        ),
                        List.of(
                                new Point(2, 1),
                                new Point(3, 1),
                                new Point(2, 2),
                                new Point(3, 2)
                        )
                ),

                new RawRoom("Baños", new int[]{9, 0, 5, 5},
                        List.of(),
                        List.<Point[]>of(
                                new Point[]{
                                        new Point(10, 5),
                                        new Point(11, 5)
                                },
                                new Point[]{
                                        new Point(14, 1),
                                        new Point(14, 2)
                                }
                        ),
                        List.of(
                                new Point(10, 2),
                                new Point(11, 2),
                                new Point(12, 2),
                                new Point(11, 3)
                        )
                ),

                new RawRoom("Cafeteria", new int[]{16, 0, 7, 5},
                        List.of(),
                        List.<Point[]>of(
                                new Point[]{
                                        new Point(16, 5),
                                        new Point(17, 5)
                                }
                        ),
                        List.of(
                                new Point(18, 2),
                                new Point(19, 2),
                                new Point(20, 2),
                                new Point(19, 3)
                        )
                ),

                new RawRoom("Maquinas", new int[]{0, 6, 7, 4},
                        List.of(
                                new Point(6, 6),
                                new Point(6, 9)
                        ),
                        List.<Point[]>of(
                                new Point[]{
                                        new Point(6, 6)
                                },
                                new Point[]{
                                        new Point(6, 9)
                                }
                        ),
                        List.of(
                                new Point(2, 7),
                                new Point(3, 7),
                                new Point(2, 8),
                                new Point(3, 8)
                        )
                ),

                new RawRoom("Laboratorio", new int[]{9, 7, 5, 6},
                        List.of(),
                        List.<Point[]>of(
                                new Point[]{
                                        new Point(8, 9),
                                        new Point(8, 10)
                                },
                                new Point[]{
                                        new Point(14, 9),
                                        new Point(14, 10)
                                }
                        ),
                        List.of(
                                new Point(10, 9),
                                new Point(11, 9),
                                new Point(12, 9),
                                new Point(11, 10)
                        )
                ),

                new RawRoom("DeustoTech", new int[]{16, 7, 7, 7},
                        List.of(
                                new Point(16, 13),
                                new Point(17, 13)
                        ),
                        List.<Point[]>of(
                                new Point[]{
                                        new Point(16, 13),
                                        new Point(17, 13)
                                }
                        ),
                        List.of(
                                new Point(18, 9),
                                new Point(19, 9),
                                new Point(20, 9),
                                new Point(19, 10)
                        )
                ),

                new RawRoom("Aula 1", new int[]{0, 11, 6, 4},
                        List.of(),
                        List.<Point[]>of(
                                new Point[]{
                                        new Point(6, 12),
                                        new Point(6, 13)
                                }
                        ),
                        List.of(
                                new Point(2, 12),
                                new Point(3, 12),
                                new Point(2, 13),
                                new Point(3, 13)
                        )
                ),

                new RawRoom("Aula 2", new int[]{0, 17, 6, 5},
                        List.of(
                                new Point(0, 17),
                                new Point(5, 17)
                        ),
                        List.<Point[]>of(
                                new Point[]{
                                        new Point(5, 17),
                                        new Point(6, 17),
                                        new Point(6, 18)
                                }
                        ),
                        List.of(
                                new Point(2, 18),
                                new Point(3, 18),
                                new Point(2, 19),
                                new Point(3, 19)
                        )
                ),

                new RawRoom("Room 9", new int[]{8, 15, 7, 7},
                        List.of(
                                new Point(8, 21),
                                new Point(14, 21)
                        ),
                        List.<Point[]>of(
                                new Point[]{
                                        new Point(10, 14),
                                        new Point(11, 14),
                                        new Point(12, 14)
                                }
                        ),
                        List.of(
                                new Point(10, 18),
                                new Point(11, 18),
                                new Point(12, 18),
                                new Point(11, 19)
                        )
                ),

                new RawRoom("Room 10", new int[]{17, 16, 6, 6},
                        List.of(),
                        List.<Point[]>of(
                                new Point[]{
                                        new Point(16, 16),
                                        new Point(16, 17)
                                }
                        ),
                        List.of(
                                new Point(19, 18),
                                new Point(20, 18),
                                new Point(19, 19),
                                new Point(20, 19)
                        )
                )
        };
    }
}
