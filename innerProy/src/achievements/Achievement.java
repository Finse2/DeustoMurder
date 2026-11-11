package achievements;

import javax.swing.*;
import java.util.function.Consumer;

public class Achievement implements Consumer<AchievementType> {

    private final String name;
    private final boolean hidden;
    private String stage;
    private final AchievementType type;
    private ImageIcon achievementIcon;
    private ImageIcon borderIcon;
    private int progress;

    public Achievement(AchievementType type, String name, boolean hidden) {

        this.type = type;
        this.name = name;
        this.hidden = false;
    }

    @Override
    public void accept(AchievementType event) {
        if (type == event) {
            progress++;
        }
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getName() {
        return name;
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

    // TODO: añadir los iconos de los achievements

}
