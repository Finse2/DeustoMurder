
package model;

import java.util.Objects;
import java.util.UUID;

public final class PlayerProfile {

    private final UUID playerId;

    private final String displayName;

    private final String profileImage;

    private final long createdAt;

    private final long lastSeen;

    // CONSTRUCTOR

    public PlayerProfile(
            UUID playerId,
            String displayName,
            String profileImage,
            long createdAt,
            long lastSeen
    ) {

        this.playerId = Objects.requireNonNull(
                playerId, "playerId"
        );

        Objects.requireNonNull(
                displayName, "displayName"
        );

        if (displayName.isBlank()) {

            throw new IllegalArgumentException(
                    "Display name cannot be empty."
            );
        }

        if (createdAt < 0 || lastSeen < createdAt) {

            throw new IllegalArgumentException(
                    "Invalid profile timestamps."
            );
        }

        this.displayName = displayName.trim();
        this.profileImage = profileImage;
        this.createdAt = createdAt;
        this.lastSeen = lastSeen;
    }

    // GETTERS

    public UUID getPlayerId() {
        return playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public boolean hasProfileImage() {

        return profileImage != null
                && !profileImage.isBlank();
    }

    // OBJECT METHODS

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof PlayerProfile other)) {
            return false;
        }

        return playerId.equals(other.playerId);
    }

    @Override
    public int hashCode() {
        return playerId.hashCode();
    }

    @Override
    public String toString() {

        return "PlayerProfile{" +
                "playerId=" + playerId +
                ", displayName='" + displayName + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", createdAt=" + createdAt +
                ", lastSeen=" + lastSeen +
                '}';
    }
}
