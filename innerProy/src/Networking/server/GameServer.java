
package Networking.server;


import Networking.common.Packet;
import Networking.common.PacketType;
import Networking.common.ChatChannel;

import Networking.packets.CardShowPacket;
import Networking.packets.ChatMessagePacket;
import Networking.packets.JoinAcceptedPacket;
import Networking.packets.JoinPacket;
import Networking.packets.PlayerJoinedPacket;
import Networking.packets.PlayerMovePacket;

import app.ApplicationContext;
import utils.JDBC.DAO.PlayerDAO;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import Networking.match.NetworkMatchManager;
import Networking.match.NetworkMatchResult;

import Networking.packets.GameStartPacket;
import Networking.packets.GameOverPacket;

import java.util.ArrayList;
import java.util.List;

public final class GameServer {

    // SERVER FIELDS

    private final NetworkMatchManager matchManager = new NetworkMatchManager();

    private volatile UUID hostProfileId;

    private final int port;

    private final Map<Integer, ServersClientHandler> clients =
            new ConcurrentHashMap<>();

    private final AtomicInteger nextPlayerID =
            new AtomicInteger(1);

    private final ExecutorService clientExecutor =
            Executors.newCachedThreadPool();

    private volatile boolean running;

    private volatile ServerSocket serverSocket;

    // CONSTRUCTOR

    public GameServer(int port) {

        this(
                port,
                ApplicationContext.getInstance()
                        .isInitialized()

                        ? ApplicationContext.getInstance()
                        .getLocalPlayerId()

                        : null
        );
    }

    public GameServer(
            int port,
            UUID hostProfileId
    ) {

        if (port < 1 || port > 65535) {

            throw new IllegalArgumentException(
                    "Port must be between 1 and 65535"
            );
        }

        this.port = port;

        this.hostProfileId = hostProfileId;
    }



    // START SERVER

    public void start() {

        if (running) {

            throw new IllegalStateException(
                    "Server is already running"
            );
        }

        try (ServerSocket listeningSocket =
                     new ServerSocket(port)) {

            serverSocket = listeningSocket;

            running = true;

            InetAddress localAddress =
                    InetAddress.getLocalHost();

            int listeningPort =
                    listeningSocket.getLocalPort();

            System.out.println("Server started:");

            System.out.println(
                    "  Host name: "
                            + localAddress.getHostName()
            );

            System.out.println(
                    "  IP address: "
                            + localAddress.getHostAddress()
            );

            System.out.println(
                    "  Port: " + listeningPort
            );

            System.out.println(
                    "Clients can connect using "
                            + localAddress.getHostName()
                            + ":" + listeningPort
            );

            while (running) {

                Socket clientSocket =
                        listeningSocket.accept();

                int playerID =
                        nextPlayerID.getAndIncrement();

                ServersClientHandler handler =
                        new ServersClientHandler(
                                clientSocket,
                                this,
                                playerID
                        );

                clients.put(playerID, handler);

                clientExecutor.execute(handler);
            }

        } catch (IOException e) {

            if (running) {

                System.err.println(
                        "Server stopped because it "
                                + "could not accept clients: "
                                + e.getMessage()
                );
            }

        } finally {

            running = false;

            serverSocket = null;

            disconnectClients();
        }
    }

    // STOP SERVER

    public void stop() {

        running = false;

        ServerSocket socket =
                serverSocket;

        if (socket != null) {

            try {

                socket.close();

            } catch (IOException e) {

                System.err.println(
                        "Error closing server socket: "
                                + e.getMessage()
                );
            }
        }

        disconnectClients();
    }

    // PACKET HANDLING

    void handlePacket(
            Packet packet,
            ServersClientHandler handler
    ) {

        if (packet == null || handler == null) {

            return;
        }

        PacketType type =
                packet.getPacketType();

        if (type == null) {

            return;
        }

        // Clients must complete JOIN before
        // sending any other packets.

        if (type != PacketType.JOIN
                && !handler.hasJoined()) {

            System.err.println(
                    "Ignoring packet from "
                            + "unregistered client."
            );

            return;
        }

        switch (type) {

            case JOIN ->
                    handleJoin(packet, handler);

            case CARD_SHOW ->
                    handleCardShow(packet, handler);


            case PLAYER_MOVE ->
                    handlePlayerMove(packet, handler);

            case GAME_START ->
                    handleGameStart(packet, handler);

            case GAME_OVER ->
                    handleGameOver(packet, handler);

            case CHAT_MESSAGE ->
                    handleChatMessage(packet, handler);

            case JOIN_ACCEPTED, PLAYER_JOINED ->

                    System.err.println(
                            "Ignoring server-only packet "
                                    + "from player "
                                    + handler.getPlayerID()
                    );
        }
    }

    // HANDLE PLAYER JOIN

    private synchronized void handleJoin(
            Packet packet,
            ServersClientHandler handler
    ) {

        if (!(packet instanceof JoinPacket joinPacket)) {

            return;
        }

        if (handler.hasJoined()) {
            return;
        }

        if (matchManager.isInProgress()) {

            sendSystemMessage(
                    handler,
                    "An investigation is already in progress."
            );

            handler.disconnect();

            return;
        }
        UUID profileId =
                joinPacket.getProfileId();

        // REJECT MISSING PROFILE UUID

        if (profileId == null) {

            System.err.println(
                    "Rejected player with missing profile ID."
            );

            handler.disconnect();

            return;
        }

        String userName = cleanName(

                joinPacket.getUserName(),

                "Player " + handler.getPlayerID()
        );

        String computerName = cleanName(

                joinPacket.getComputerName(),

                "Unknown computer"
        );

        if (userName.length() > 64) {

            System.err.println(
                    "Player name is too long."
            );

            handler.disconnect();

            return;
        }

        // CHECK FOR DUPLICATE IDENTITIES

        for (ServersClientHandler existing :
                clients.values()) {

            if (existing == handler) {

                continue;
            }

            if (profileId.equals(
                    existing.getProfileId()
            )) {

                System.err.println(
                        "Duplicate player profile: "
                                + profileId
                );

                handler.disconnect();

                return;
            }

            if (userName.equals(
                    existing.getUserName()
            )) {

                System.err.println(
                        "Duplicate player name: "
                                + userName
                );

                handler.disconnect();

                return;
            }
        }

        // REGISTER PROFILE IN HOST DATABASE

        ApplicationContext context =
                ApplicationContext.getInstance();

        if (context.isInitialized()) {

            try {

                PlayerDAO playerDAO =
                        new PlayerDAO(
                                context.getDatabase()
                        );

                playerDAO.getOrCreatePlayer(
                        profileId,
                        userName
                );

                // Preserve the host's local profile name.

                if (!profileId.equals(
                        context.getLocalPlayerId()
                )) {

                    playerDAO.updateName(
                            profileId,
                            userName
                    );
                }

                playerDAO.updateLastSeen(
                        profileId
                );

            } catch (Exception e) {

                System.err.println(
                        "Could not register player profile: "
                                + e.getMessage()
                );

                handler.disconnect();

                return;
            }
        }

        // STORE PLAYER INFORMATION

        handler.setProfileId(profileId);

        handler.setComputerName(computerName);

        handler.setUserName(userName);

        // CONFIRM PLAYER CONNECTION

        handler.sendPacket(
                new JoinAcceptedPacket(
                        handler.getPlayerID()
                )
        );

        // SEND EXISTING PLAYERS TO THE NEW CLIENT

        for (ServersClientHandler existing :
                clients.values()) {

            if (existing == handler
                    || !existing.hasJoined()) {

                continue;
            }

            handler.sendPacket(
                    new PlayerJoinedPacket(

                            existing.getPlayerID(),

                            existing.getUserName(),

                            existing.getComputerName(),

                            existing.getProfileId()
                    )
            );
        }

        // ANNOUNCE NEW PLAYER TO EXISTING CLIENTS

        broadcastExcept(

                new PlayerJoinedPacket(

                        handler.getPlayerID(),

                        userName,

                        computerName,

                        profileId
                ),

                handler
        );

        broadcastSystemMessage(

                userName
                        + " has joined the investigation."
        );

        System.out.println(

                "Player connected: "

                        + userName

                        + " | Session ID: "

                        + handler.getPlayerID()

                        + " | Profile ID: "

                        + profileId
        );
    }

    // HANDLE CHAT MESSAGES

    private void handleChatMessage(
            Packet packet,
            ServersClientHandler sender
    ) {

        if (!(packet instanceof ChatMessagePacket chatPacket)
                || !sender.hasJoined()) {

            return;
        }

        String message =
                chatPacket.getMessage();

        if (message == null
                || message.isBlank()
                || message.strip().length() > 500) {

            return;
        }

        ChatChannel channel =
                chatPacket.getChannel();

        if (channel == null
                || channel == ChatChannel.SYSTEM) {

            return;
        }

        switch (channel) {

            case GENERAL -> {

                ChatMessagePacket authoritative =
                        new ChatMessagePacket(

                                sender.getPlayerID(),

                                sender.getUserName(),

                                message.strip(),

                                ChatChannel.GENERAL,

                                null
                        );

                broadcastExcept(
                        authoritative,
                        sender
                );
            }

            case PRIVATE ->

                    handlePrivateMessage(

                            sender,

                            chatPacket.getRecipient(),

                            message.strip()
                    );

            case SYSTEM -> {

                // Only the server can generate
                // system messages.
            }
        }
    }

    // HANDLE PRIVATE MESSAGES

    private void handlePrivateMessage(
            ServersClientHandler sender,
            String recipient,
            String message
    ) {

        if (recipient == null
                || recipient.isBlank()) {

            sendSystemMessage(
                    sender,
                    "Invalid recipient."
            );

            return;
        }

        ServersClientHandler target = null;

        // FIND RECIPIENT

        for (ServersClientHandler client :
                clients.values()) {

            if (client.hasJoined()
                    && recipient.equals(
                    client.getUserName()
            )) {

                if (target != null) {

                    sendSystemMessage(
                            sender,
                            "Recipient name is ambiguous."
                    );

                    return;
                }

                target = client;
            }
        }

        if (target == null) {

            sendSystemMessage(
                    sender,
                    "The recipient is not connected."
            );

            return;
        }

        if (target == sender) {

            sendSystemMessage(
                    sender,
                    "You cannot send a private message "
                            + "to yourself."
            );

            return;
        }

        // CREATE AUTHORITATIVE MESSAGE

        ChatMessagePacket authoritative =
                new ChatMessagePacket(

                        sender.getPlayerID(),

                        sender.getUserName(),

                        message,

                        ChatChannel.PRIVATE,

                        target.getUserName()
                );

        // SEND TO RECIPIENT

        target.sendPacket(authoritative);

        // RETURN MESSAGE TO SENDER

        sender.sendPacket(authoritative);
    }

    // SEND INDIVIDUAL SYSTEM MESSAGE

    private void sendSystemMessage(
            ServersClientHandler target,
            String message
    ) {

        target.sendPacket(

                new ChatMessagePacket(

                        -1,

                        "System",

                        message,

                        ChatChannel.SYSTEM,

                        null
                )
        );
    }

    // HANDLE CARD REVELATION

    private void handleCardShow(
            Packet packet,
            ServersClientHandler handler
    ) {

        if (!(packet instanceof CardShowPacket cardShowPacket)) {

            return;
        }

        ServersClientHandler target =
                clients.get(
                        cardShowPacket.getTargetUserID()
                );

        if (target == null
                || !target.hasJoined()) {

            System.err.println(

                    "Target player not found: "

                            + cardShowPacket.getTargetUserID()
            );

            return;
        }

        target.sendPacket(

                new CardShowPacket(

                        cardShowPacket.getShowCard(),

                        handler.getUserName(),

                        target.getUserName(),

                        target.getPlayerID()
                )
        );
    }

    // HANDLE PLAYER MOVEMENT

    private void handlePlayerMove(
            Packet packet,
            ServersClientHandler handler
    ) {

        if (!(packet instanceof PlayerMovePacket movePacket)) {

            return;
        }

        PlayerMovePacket authoritativeMove =
                new PlayerMovePacket(

                        movePacket.getPlayerPosX(),

                        movePacket.getPlayerPosY(),

                        movePacket.getMoveDistance(),

                        handler.getPlayerID()
                );

        broadcastExcept(
                authoritativeMove,
                handler
        );
    }

    // BROADCAST TO ALL REGISTERED PLAYERS

    private void broadcast(Packet packet) {

        for (ServersClientHandler client :
                clients.values()) {

            if (client.hasJoined()) {

                client.sendPacket(packet);
            }
        }
    }

    // BROADCAST EXCEPT ONE PLAYER

    private void broadcastExcept(
            Packet packet,
            ServersClientHandler excludedClient
    ) {

        for (ServersClientHandler client :
                clients.values()) {

            if (client != excludedClient
                    && client.hasJoined()) {

                client.sendPacket(packet);
            }
        }
    }

    // DISCONNECT ALL CLIENTS

    private void disconnectClients() {

        for (ServersClientHandler client :
                clients.values()) {

            client.disconnect();
        }

        clientExecutor.shutdownNow();
    }

    // BROADCAST SYSTEM MESSAGE

    private void broadcastSystemMessage(
            String message
    ) {

        ChatMessagePacket packet =
                new ChatMessagePacket(

                        -1,

                        "System",

                        message,

                        ChatChannel.SYSTEM,

                        null
                );

        broadcast(packet);
    }

    // CLEAN PLAYER NAMES

    private String cleanName(
            String value,
            String fallback
    ) {

        if (value == null || value.isBlank()) {

            return fallback;
        }

        return value.trim();
    }

    // REMOVE DISCONNECTED CLIENT

    void removeClient(
            ServersClientHandler handler
    ) {

        boolean removed =
                clients.remove(

                        handler.getPlayerID(),

                        handler
                );

        if (removed
                && running
                && handler.hasJoined()) {

            broadcastSystemMessage(

                    handler.getUserName()

                            + " has left the investigation."
            );
        }
    }

    // GET PERMANENT PROFILE UUID BY SESSION ID

    public Optional<UUID> getProfileId(
            int playerID
    ) {

        ServersClientHandler handler =
                clients.get(playerID);

        if (handler == null
                || !handler.hasJoined()) {

            return Optional.empty();
        }

        return Optional.of(
                handler.getProfileId()
        );
    }

    // GET ALL CONNECTED PLAYER PROFILES

    public Map<Integer, UUID> getConnectedProfiles() {

        Map<Integer, UUID> result =
                new HashMap<>();

        for (ServersClientHandler handler :
                clients.values()) {

            if (handler.hasJoined()) {

                result.put(

                        handler.getPlayerID(),

                        handler.getProfileId()
                );
            }
        }

        return Map.copyOf(result);
    }

    // GET CLIENT COUNT

    public int getClientCount() {

        return clients.size();
    }

    // GET REGISTERED PLAYER COUNT

    public int getRegisteredPlayerCount() {

        return getConnectedProfiles().size();
    }

    // SERVER STATE

    public boolean isRunning() {

        return running;
    }

    private boolean isHost(
            ServersClientHandler handler
    ) {

        return handler != null

                && handler.hasJoined()

                && hostProfileId != null

                && hostProfileId.equals(
                handler.getProfileId()
        );
    }

    private synchronized void handleGameStart(
            Packet packet,
            ServersClientHandler handler
    ) {

        if (!(packet instanceof GameStartPacket request)) {

            return;
        }

        if (!request.getGameStart()
                || request.getMatchId() != null) {

            return;
        }

        // ONLY THE HOST CAN START A MATCH

        if (!isHost(handler)) {

            sendSystemMessage(
                    handler,
                    "Only the host can start the game."
            );

            return;
        }

        // PREVENT DUPLICATE MATCH STARTS

        if (matchManager.isInProgress()) {

            sendSystemMessage(
                    handler,
                    "A match is already in progress."
            );

            return;
        }

        // COLLECT REGISTERED PLAYERS

        List<NetworkMatchManager.Participant>
                participants = new ArrayList<>();

        for (ServersClientHandler client :
                clients.values()) {

            if (!client.hasJoined()) {

                continue;
            }

            participants.add(

                    new NetworkMatchManager.Participant(

                            client.getPlayerID(),

                            client.getProfileId(),

                            client.getUserName(),

                            null
                    )
            );
        }

        if (participants.size() < 2) {

            sendSystemMessage(
                    handler,
                    "At least two players are required."
            );

            return;
        }

        // CREATE MATCH

        UUID matchId;

        try {

            matchId = matchManager.startMatch(
                    hostProfileId,
                    participants
            );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            sendSystemMessage(
                    handler,
                    e.getMessage()
            );

            return;
        }

        // BROADCAST CONFIRMED START

        GameStartPacket confirmation =
                new GameStartPacket(

                        matchId,

                        hostProfileId,

                        matchManager.getStartedAt()
                );

        broadcast(confirmation);

        broadcastSystemMessage(
                "The investigation has started."
        );

        System.out.println(
                "Match started: " + matchId
        );

        System.out.println(
                "Participants: " + participants.size()
        );
    }


    private synchronized void handleGameOver(
            Packet packet,
            ServersClientHandler handler
    ) {

        if (!(packet instanceof GameOverPacket request)) {
            return;
        }

        if (!request.getGameOver()) {
            return;
        }

        // Clients cannot supply completed match results.

        if (request.hasMatchResult()) {

            sendSystemMessage(
                    handler,
                    "Clients cannot submit final match results."
            );

            return;
        }

        // Only the host can finalize a match.

        if (!isHost(handler)) {

            sendSystemMessage(
                    handler,
                    "Only the host can finalize the match."
            );

            return;
        }

        // Validate the match UUID.

        UUID matchId = request.getMatchId();

        if (matchId == null
                || !matchId.equals(matchManager.getMatchId())) {

            sendSystemMessage(
                    handler,
                    "Invalid or outdated match UUID."
            );

            return;
        }

        // Validate the winner.

        int winnerSessionId = request.getWinnerID();

        if (!matchManager.containsPlayer(winnerSessionId)) {

            sendSystemMessage(
                    handler,
                    "Invalid match winner."
            );

            return;
        }

        boolean alreadyCompleted =
                matchManager.getState()
                        == NetworkMatchManager.State.COMPLETED;

        NetworkMatchResult result;

        try {

            result = matchManager.completeMatch(
                    matchId,
                    winnerSessionId,
                    request.getMurderer(),
                    request.getWeapon(),
                    request.getRoom()
            );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {

            sendSystemMessage(
                    handler,
                    e.getMessage()
            );

            return;
        }

        // Return the existing result for duplicate requests.

        if (alreadyCompleted) {

            handler.sendPacket(
                    new GameOverPacket(result)
            );

            return;
        }

        // Broadcast identical results to all participants.

        broadcast(new GameOverPacket(result));

        broadcastSystemMessage(
                "The investigation has ended."
        );

        System.out.println(
                "Match completed: " + result.matchId()
        );

        System.out.println(
                "Winner: " + result.getWinnerName()
        );

        System.out.println(
                "Duration: " + result.durationMillis() + " ms"
        );
    }
    public Optional<NetworkMatchResult> getCompletedMatch() {

        return matchManager.getCompletedResult();
    }


}
