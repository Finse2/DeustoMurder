
package game.core;

public class GameStartResult {

    private final boolean successful;
    private final String message;

    private GameStartResult(boolean successful, String message) {
        this.successful = successful;
        this.message = message;
    }

    public static GameStartResult success() {
        return new GameStartResult(
                true,
                "The game was created successfully"
        );
    }

    public static GameStartResult failure(String message) {
        if (message == null || message.isBlank()) {
            message = "The game couldn't be started";
        }

        return new GameStartResult(false, message);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "GameStartResult{" +
                "successful=" + successful +
                ", message='" + message + '\'' +
                '}';
    }
}

