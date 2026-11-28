
package utils.JDBC.DAO;

import java.util.Objects;

public record AchievementDefinition(

        String code,
        String name,
        String description,

        boolean hidden,

        Long bronzeRequirement,
        Long goldRequirement,
        Long diamondRequirement,
        Long champRequirement,

        String iconPath

) {

    public AchievementDefinition {

        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");

        if (code.isBlank() || name.isBlank()) {

            throw new IllegalArgumentException(
                    "Achievement code and name cannot be empty."
            );
        }

        Long[] requirements = {

                bronzeRequirement,
                goldRequirement,
                diamondRequirement,
                champRequirement
        };

        long previous = 0;

        boolean hasRequirement = false;

        for (Long requirement : requirements) {

            if (requirement == null) {
                continue;
            }

            if (requirement <= previous) {

                throw new IllegalArgumentException(
                        "Achievement requirements must "
                                + "be positive and increasing."
                );
            }

            previous = requirement;
            hasRequirement = true;
        }

        if (!hasRequirement) {

            throw new IllegalArgumentException(
                    "Achievement must have at least one level."
            );
        }
    }

    // HIGHEST AVAILABLE LEVEL

    public String getHighestLevel() {

        if (champRequirement != null) {
            return "CHAMP";
        }

        if (diamondRequirement != null) {
            return "DIAMOND";
        }

        if (goldRequirement != null) {
            return "GOLD";
        }

        return "BRONZE";
    }

    // LEVEL REQUIREMENTS

    public Long getRequirement(String level) {

        return switch (level) {

            case "LOCKED" -> 0L;

            case "BRONZE" -> bronzeRequirement;

            case "GOLD" -> goldRequirement;

            case "DIAMOND" -> diamondRequirement;

            case "CHAMP" -> champRequirement;

            default -> throw new IllegalArgumentException(
                    "Unknown achievement level: " + level
            );
        };
    }

    // EVALUATE LEVEL

    public String evaluateLevel(long progress) {

        if (progress < 0) {

            throw new IllegalArgumentException(
                    "Progress cannot be negative."
            );
        }

        if (champRequirement != null
                && progress >= champRequirement) {

            return "CHAMP";
        }

        if (diamondRequirement != null
                && progress >= diamondRequirement) {

            return "DIAMOND";
        }

        if (goldRequirement != null
                && progress >= goldRequirement) {

            return "GOLD";
        }

        if (bronzeRequirement != null
                && progress >= bronzeRequirement) {

            return "BRONZE";
        }

        return "LOCKED";
    }

    // SINGLE LEVEL ACHIEVEMENTS

    public boolean isSingleLevel() {

        int levels = 0;

        if (bronzeRequirement != null) levels++;
        if (goldRequirement != null) levels++;
        if (diamondRequirement != null) levels++;
        if (champRequirement != null) levels++;

        return levels == 1;
    }
}
