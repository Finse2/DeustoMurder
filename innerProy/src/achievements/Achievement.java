package achievements;

import java.util.function.Consumer;

public class Achievement implements Consumer<AchievementType> {

    private final AchievementType type;
    private int progress;

    public Achievement(AchievementType type) {
        this.type = type;
    }

    @Override
    public void accept(AchievementType event) {
        if (type == event) {
            progress++;
        }
    }

    public int getProgress() {
        return progress;
    }

    public int getTier() {
        int tier = 0;

        for (int requirement : type.getRequirements()) {
            if (progress >= requirement) {
                tier++;
            }
        }

        return tier;
    }


}
