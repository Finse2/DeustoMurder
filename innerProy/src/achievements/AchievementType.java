
package achievements;

public enum AchievementType {

    // ------------------------
    // NORMAL ACHIEVEMENTS
    // ------------------------

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
    REVEAL_CARDS_TO_OPPONENTS(false, 10, 50, 200, 500),
    WIN_WITHOUT_REVEALING_ANY_CARDS(false, 1, 5, 15, 30),
    DEFEAT_THE_SAME_OPPONENT(false, 1, 5, 15, 30),
    DEFEAT_THE_PREVIOUS_MATCH_WINNER(false, 1, 5, 15, 30),

    // FUNNY AND UNLUCKY ACHIEVEMENTS

    MAKE_INCORRECT_FINAL_ACCUSATIONS(false, 1, 5, 20, 50),
    LOSE_CONSECUTIVE_GAMES(false, 3, 5, 10, 20),
    MAKE_AN_ACCUSATION_WITH_EXACTLY_2_CORRECT_CARDS(false, 1, 5, 15, 30),
    MAKE_AN_ACCUSATION_WITH_ALL_3_CARDS_WRONG(false, 1, 5, 15, 30),
    INCORRECTLY_ACCUSE_IN_YOUR_FIRST_TURN(false, 1, 5, 10, 20),

    // CAREER AND COMPLETION ACHIEVEMENTS

    TOTAL_COMPLETED_MATCHES(false, 1, 10, 50, 200),
    TOTAL_SUGGESTIONS_MADE(false, 10, 50, 200, 1000),
    TOTAL_FINAL_ACCUSATIONS(false, 5, 25, 100, 500),
    DISTINCT_OPPONENTS_PLAYED_AGAINST(false, 3, 10, 25, 50),
    TOTAL_ACHIEVEMENT_MILESTONES_UNLOCKED(false, 10, 30, 70, 100),

    // ------------------------
    // HIDDEN ACHIEVEMENTS
    // ------------------------

    // SECRET DETECTIVE ACHIEVEMENTS

    MIND_READER(true, 1, 2, 3, 5),
    THE_BROKEN_RECORD(true, 1, 3, 7, 15),
    ONE_ROOM_WONDER(true, 1, 3, 5, 10),
    THE_IMPOSTOR(true, 1, 2, 3, 5),

    // SECRET FUNNY ACHIEVEMENTS

    SELF_INCRIMINATION(true, 1, 5, 15, 30),
    THE_WORST_SUGGESTION(true, 1, 3, 7, 15),
    THE_PARROT(true, 1, 5, 15, 30),
    EVERYONE_SUSPECTS_ME(true, 1, 3, 7, 15),

    // SECRET LUCK ACHIEVEMENTS

    THE_NUMBER_SEVEN (true, 1, 2, 3, 5),
    PERFECTLY_BALANCED (true, 1, 5, 15, 30),
    AGAINST_ALL_ODDS (true, 1, 3, 7, 15),
    THE_UNSOLVED_CASE (true, 1, 2, 3, 5);

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
