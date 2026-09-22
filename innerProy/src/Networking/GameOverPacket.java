package Networking;

final class GameOverPacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final boolean gameOver;
    private final String winner;

    GameOverPacket(boolean gameOver, String winner) {
        super(PacketType.GAME_OVER);
        this.gameOver = gameOver;
        this.winner = winner;
    }

    boolean getGameOver() {
        return gameOver;
    }

    String getWinner() {
        return winner;
    }
}
