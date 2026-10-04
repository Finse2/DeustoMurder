package Networking.packets;

import Networking.Server;
import Networking.common.Packet;
import Networking.common.PacketType;

public final class GameStartPacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final boolean startGame;

    public GameStartPacket(boolean startGame) {
        super(PacketType.GAME_START);
        this.startGame = startGame;
    }

    public boolean getGameStart() {
        return startGame;
    }
}
