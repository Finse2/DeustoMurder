
package game.profile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.Objects;
import java.util.UUID;

public final class PlayerIdentityManager {

    private static final Path DEFAULT_PATH = Path.of(
            System.getProperty("user.home"),
            ".deustoCluedo",
            "player.id"
    );

    private final Path identityFile;

    private UUID cachedIdentity;

    public PlayerIdentityManager() {
        this(DEFAULT_PATH);
    }

    public PlayerIdentityManager(Path identityFile) {

        this.identityFile = Objects.requireNonNull(
                identityFile
        ).toAbsolutePath().normalize();
    }

    // GET OR CREATE PERMANENT IDENTITY

    public synchronized UUID getOrCreateIdentity()
            throws IOException {

        if (cachedIdentity != null) {
            return cachedIdentity;
        }

        Files.createDirectories(
                identityFile.getParent()
        );

        if (Files.notExists(identityFile)) {

            UUID generated = UUID.randomUUID();

            try {

                Files.writeString(
                        identityFile,
                        generated.toString(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE
                );

            } catch (
                    java.nio.file.FileAlreadyExistsException e
            ) {
                // Another instance created the file.
                // Read the existing identity below.
            }
        }

        cachedIdentity = readIdentity();

        return cachedIdentity;
    }

    // READ EXISTING IDENTITY

    private UUID readIdentity()
            throws IOException {

        String value = Files.readString(
                identityFile,
                StandardCharsets.UTF_8
        ).trim();

        try {

            UUID identity = UUID.fromString(value);

            if (!identity.toString()
                    .equalsIgnoreCase(value)) {

                throw new IllegalArgumentException(
                        "Invalid UUID representation."
                );
            }

            return identity;

        } catch (IllegalArgumentException e) {

            throw new IOException(
                    "Invalid persistent player identity: "
                            + identityFile,
                    e
            );
        }
    }

    public boolean identityExists() {

        return Files.isRegularFile(identityFile);
    }

    public Path getIdentityFile() {

        return identityFile;
    }
}
