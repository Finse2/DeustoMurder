package achievements;

public enum AchievementType {

    // DETECTIVE ACHIEVEMENTS

    WIN_GAMES(false, 1, 10, 50, 100),
    FIRST_ROUND_WIN(false, 1, 6, 11, 21),
    WIN_WITHIN_5_TURNS(false, 1, 5, 15, 30),
    WIN_WITH_NO_MORE_THAN_3_SUGGESTIONS(false, 1, 5, 15, 30),
    CONSECUTIVE_WINS(false, 2, 3, 5, 10),
    WIN_GAMES_LASTING_MORE_THAN_30_ROUNDS(false, 1, 5, 10, 20),

    // EXPLORATION ACHIEVEMENTS

    TOTAL_TILES_MOVES(false, 100, 500, 2000, 10000),
    VISIT_EVERY_ROOM_IN_A_MATCH(false, 100, 500, 2000, 10000),
    ENTER_THE_SAME_ROOM_5_TIMES_IN_ONE_MATCH(false, 1, 5, 10, 30),
    ROLL_DOUBLE_SIXES(false, 1, 10, 25, 100),
    ROLL_DOUBLE_ONES(false, 1, 10, 25, 100),
    ROLL_DOUBLE_ONES_3_TIMES_IN_ONE_MATCH(false, 1, 3, 5, 10),

    // MULTIPLAYER ACHIEVEMENTS

    HOST_COMPLETED_GAMES(false, 1, 10, 50, 100),
    PLAY_MATCHES_WITH_A_FULL_LOBBY(false, 1, 10, 25, 50),
    REVEAL_CARDS_TO_OPPONENTS (false, 10, 50, 200, 500),
    WIN_WITHOUT_REVEALING_ANY_CARDS (false, 1, 5, 15, 30),
    DEFEAT_THE_SAME_OPPONENT (false, 1, 5, 15, 30),
    DEFEAT_THE_PREVIOUS_MATCH_WINNER (false, 1, 5, 15, 30),

    // FUNNY AND UNLUCKY ACHIEVEMENTS

    MAKE_INCORRECT_FINAL_ACCUSATIONS (false, 1, 5, 20, 50),
    LOSE_CONSECUTIVE_GAMES (false, 3 ,5 ,10 ,20),
    MAKE_AN_ACCUSATION_WITH_EXACTLY_2_CORRECT_CARDS (false, 1, 5, 15, 30),
    MAKE_AN_ACCUSATION_WITH_ALL_3_CARDS_WRONG (false, 1, 5, 15, 30),
    INCORRECTLY_ACCUSE_IN_YOUR_FIRST_TURN (false, 1, 5, 10, 20);


    private final boolean hidden;
    private final int[] requirements;

    AchievementType(boolean hidden, int... requirements) {
        this.hidden = hidden;
        this.requirements = requirements.clone();
    }

    public boolean isHidden() {

        return hidden;
    }

    public int[] getRequirements() {
        return requirements.clone();
    }
}