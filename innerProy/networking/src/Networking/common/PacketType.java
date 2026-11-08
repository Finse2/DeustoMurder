package Networking.common;
/**
 * This enum represents the different types of packets that can be sent between the client and server in a networking environment.
 * It is used to facilitate the communication between the client and server by indicating the type of packet being sent.
 */
public enum PacketType {
    JOIN,
    JOIN_ACCEPTED,
    PLAYER_JOINED,
    GAME_START,
    PLAYER_MOVE,
    CARD_SHOW,
    GAME_OVER
}
