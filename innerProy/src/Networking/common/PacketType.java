package Networking.common;

public enum PacketType {
    JOIN,
    JOIN_ACCEPTED,
    PLAYER_JOINED, //here we can add a new packet type for when a player joins the game
    GAME_START,
    PLAYER_MOVE,
    CARD_SHOW,
    GAME_OVER
}
