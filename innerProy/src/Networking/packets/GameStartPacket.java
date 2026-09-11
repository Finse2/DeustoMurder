package Networking.packets;

import Networking.common.Packet;
import Networking.common.PacketType;

public class GameStartPacket extends Packet {
    private static final long serialVersionUID = 1L;

    private final boolean startGame;

    public GameStartPacket(boolean StartGame) {
        super(PacketType.GAME_START);
        this.startGame = StartGame;
    }

    public boolean getGameStart() {
        return startGame;
    }
}
