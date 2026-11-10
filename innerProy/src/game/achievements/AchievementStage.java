package game.achievements;

public class AchievementStage {

    private int stageNumber;
    private int requirement;
    private int points;
    private String description;

    public AchievementStage(int stageNumber, int requirement, int points, String description) {
        this.stageNumber = stageNumber;
        this.requirement = requirement;
        this.points = points;
        this.description = description;
    }

    public int getStageNumber() {
        return stageNumber;
    }

    public void setStageNumber(int stageNumber) {
        this.stageNumber = stageNumber;
    }

    public int getRequirement() {
        return requirement;
    }

    public void setRequirement(int requirement) {
        this.requirement = requirement;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
