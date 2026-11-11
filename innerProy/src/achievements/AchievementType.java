package achievements;

public enum AchievementType {

    WIN_GAMES("Win games", false, 1, 10, 50, 100),
    FIRST_ROUND_WIN("Lucky as Hell", false, 1, 6, 11, 21),
    WIN_WITHIN_5_TURNS("Quick Thinker", false, 1, 5, 15, 30),
    CONSECUTIVE_WINS("On a Roll", false, 2, 3, 5, 10),

    TOTAL_MOVES("First Steps", false, 100, 500, 2000, 10000),
    DOUBLE_SIXES("Jackpot!", false, 1, 10, 25, 100),
    DOUBLE_ONES("Snake Eyes", false, 1, 10, 25, 100),

    TOTAL_MATCHES("Welcome to Deusto", false, 1, 10, 50, 200),
    TOTAL_SUGGESTIONS("Curious Mind", false, 10, 50, 200, 1000),

    MIND_READER("Mind Reader", true, 1, 2, 3, 5),
    THE_NUMBER_SEVEN("The Number Seven", true, 1, 2, 3, 5);

    private final String name;
    private final boolean hidden;
    private final int[] requirements;

    AchievementType(String name, boolean hidden, int... requirements) {
        this.name = name;
        this.hidden = hidden;
        this.requirements = requirements.clone();
    }

    public String getName() {
        return name;
    }

    public boolean isHidden() {
        return hidden;
    }

    public int[] getRequirements() {
        return requirements.clone();
    }
}