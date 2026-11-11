package achievements;

import javax.swing.*;
import java.util.function.Consumer;

public class Achievement implements Consumer<AchievementType> {

    private final String name;
    private String stage;
    private final AchievementType type;
    private ImageIcon achievementIcon;
    private ImageIcon borderIcon;
    private int progress;

    public Achievement(AchievementType type, String name, boolean hidden) {

        this.type = type;
        this.name = name;
        this.progress = 0;
    }

    @Override
    public void accept(AchievementType event) {
        if (type == event) {
            progress++;
        }
    }

    public boolean isHidden() {
        return type.isHidden();
    }

    public String getName() {
        return name;
    }

    public AchievementType getType() {
        return type;
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

    public int getNextRequirement() {
        int [] requirements = type.getRequirements();

        for (int requirement : requirements) {
            if (progress < requirement) {
                return requirement;
            }
        }

        return -1;
    }

    public boolean isCompleted() {
        return getTier() == type.getRequirements().length;
    }

    // TODO: añadir los iconos de los achievements
    // TODO: añadir getters para achievements y borderIcon
    // TODO: mostrar una notificación al desbloquear un nuevo tier

}
