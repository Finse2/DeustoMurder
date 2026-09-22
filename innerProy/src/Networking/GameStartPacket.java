package Networking;

final class GameStartPacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final boolean startGame;

    GameStartPacket(boolean startGame) {
        super(PacketType.GAME_START);
        this.startGame = startGame;
    }

    boolean getGameStart() {
        return startGame;
    }
}
