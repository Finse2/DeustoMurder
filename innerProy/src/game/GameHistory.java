package game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GameHistory {

    private final List<GameEvent> events = new ArrayList<>();

    public synchronized void addEvent (String playerName, String eventType, String description) {
        GameEvent.Type type = GameEvent.Type.valueOf(eventType.toUpperCase());

        record(new GameEvent(type,null, playerName, description));
    }

    public synchronized void record (GameEvent event) {
        events.add(Objects.requireNonNull(event));
    }

    public void record (GameEvent.Type type, Integer playerId, String playerName, String description) {
        record (new GameEvent(type, playerId, playerName, description));
    }

    public synchronized List <GameEvent> getAllEvents () {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }

    public synchronized List <GameEvent> getEventsForPlayer(int playerId) {
        return events.stream().filter (event -> event.getPlayerId() != null
                && event.getPlayerId() == playerId).collect(Collectors.toList());
    }

    public synchronized List <GameEvent> getEventsOfType(GameEvent.Type type) {
        Objects.requireNonNull(type);

        return events.stream().filter(event -> event.getType() == type).collect (Collectors.toList());
    }

    public synchronized int size () {
        return events.size();
    }

    public synchronized boolean isEmpty () {
        return events.isEmpty();
    }

    public synchronized void clear () {
        events.clear();
    }

    public synchronized void exportCSV (Path destination) throws IOException {
        Objects.requireNonNull(destination);

        Path parent = destination.toAbsolutePath().getParent();

        if (parent != null) {

            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
            writer.write("timestamp, type, playerId, playerName, description");
            writer.newLine();

            for (GameEvent event : events) {
                writer.write(csv(event.getTimestamp().toString()));
                writer.write(",");
                writer.write(csv(event.getType().name()));
                writer.write(",");
                writer.write(csv(
                        event.getPlayerId() == null
                                ? ""
                                : event.getPlayerId().toString()));
                writer.write(",");
                writer.write(csv(event.getPlayerName()));
                writer.write(",");
                writer.write(csv(event.getDescription()));
                writer.newLine();
            }
        }
    }

    private static String csv (String value) {
        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }



    }
