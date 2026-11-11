package achievements;

import javax.swing.*;
import java.net.URL;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Achievement implements Consumer<AchievementType> {

    private final String name;
    private String stage;
    private final AchievementType type;
    private ImageIcon achievementIcon;
    private ImageIcon borderIcon;
    private int progress;

    private BiConsumer<Achievement, Integer> onTierUnlocked;

    public Achievement(AchievementType type, String name, boolean hidden) {

        this.type = type;
        this.name = name;
        this.progress = 0;

        String iconName = type.name().toLowerCase(Locale.ROOT);

        achievementIcon = loadIcon("resources/icons/" + iconName + ".png");

        if (achievementIcon == null) {
            achievementIcon = loadIcon ("/ressources/icons.default.png");
        }

        updateBorderIcon();
    }

    @Override
    public void accept(AchievementType event) {
        if (type == event) {
            addProgress(1);
        }
    }

    // LOAD ACHIEVEMENT AND BORDER ICONS

    private ImageIcon loadIcon(String path) {
        URL url = getClass().getResource(path);

        if (url == null) {
            return null;
        }

        return new ImageIcon(url);
    }

    private void updateBorderIcon() {
        borderIcon = loadIcon ("/resources/borders/tier_" + getTier() + ".png");
    }

    // DETECT WHEN A NEW TIER IS UNLOCKED

    public void addProgress(int amount) {
        if (amount < 0 ) {
            throw new IllegalArgumentException("Progress cannot be negative");
        }

        int previousTier = getTier();
        progress = Math.addExact(progress, amount);
        int currentTier = getTier();

        if (currentTier > previousTier) {
            updateBorderIcon();

            for (int tier = previousTier + 1; tier <= currentTier; tier++) {
                if (onTierUnlocked != null) {
                    onTierUnlocked.accept(this, tier);
                }
            }
        }
    }

    // REGISTER NOTIFICATION LISTENER

    public void setOnTierUnlocked(BiConsumer<Achievement, Integer> listener) {
        this.onTierUnlocked = listener;
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

    public int getRemainingProgress () {
        int next = getNextRequirement();
        return next == -1 ? 0 : next - progress;
    }

    public boolean isCompleted() {
        return getTier() == type.getRequirements().length;
    }

    public ImageIcon getAchievementIcon() {
        return achievementIcon;
    }

    public ImageIcon getBorderIcon() {
        return borderIcon;
    }

}
