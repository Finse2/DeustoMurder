package game.achievements;

public class AchievementEvent {

    private String playerID;
    private AchievementType type;

    public AchievementEvent(String playerID, AchievementType type) {
        this.playerID = playerID;
        this.type = type;
    }

    public String getPlayerID() {
        return playerID;
    }

    public void setPlayerID(String playerID) {
        this.playerID = playerID;
    }

    public AchievementType getType() {
        return type;
    }

    public void setType(AchievementType type) {
        this.type = type;
    }



}
