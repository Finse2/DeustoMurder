package Networking.packets;

import Networking.Server;
import Networking.common.Packet;
import Networking.common.PacketType;
/*
* This class represents a packet that is sent when the game starts.
* It contains a boolean indicating whether the game has started.
* */
public final class GameStartPacket extends Packet {
    private static final long serialVersionUID = Server.serialVersionUID;

    private final boolean startGame;

    // Constructor for the GameStartPacket class.
    public GameStartPacket(boolean startGame) {
        super(PacketType.GAME_START);
        this.startGame = startGame;
    }

    // Getter method for the startGame field.
    public boolean getGameStart() {
        return startGame;
    }
}
