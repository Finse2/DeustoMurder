package game.history;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** Historial de eventos con búsqueda, filtros y CSV reversible. */
public class GameHistory {

    private final List<GameEvent> events = new ArrayList<>();
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();

    public void addChangeListener(Runnable listener) {
        changeListeners.add(Objects.requireNonNull(listener));
    }

    public void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    private void notifyChange() {
        for (Runnable listener : changeListeners) listener.run();
    }

    public void addEvent(String playerName, String eventType, String description) {
        GameEvent.Type type = GameEvent.Type.valueOf(eventType.toUpperCase(Locale.ROOT));
        record(new GameEvent(type, null, playerName, description));
    }

    public void record(GameEvent event) {
        synchronized (this) {
            events.add(Objects.requireNonNull(event));
        }
        notifyChange(); // Nunca invocar listeners mientras se mantiene el bloqueo.
    }

    public void record(GameEvent.Type type, Integer playerId, String playerName, String description) {
        record(new GameEvent(type, playerId, playerName, description));
    }

    public synchronized List<GameEvent> getAllEvents() {
        return List.copyOf(events);
    }

    public List<GameEvent> getEventsForPlayer(int playerId) {
        return filterEvents(null, null, playerId, null, null, null);
    }

    public List<GameEvent> getEventsOfType(GameEvent.Type type) {
        return filterEvents(null, Objects.requireNonNull(type), null, null, null, null);
    }

    /** Fechas inclusivas; los parámetros null no aplican filtro. */
    public List<GameEvent> filterEvents(String query, GameEvent.Type type, Integer playerId,
                                        String playerName, Instant from, Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from cannot be after to");
        }
        String text = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        List<GameEvent> result = new ArrayList<>();
        for (GameEvent event : getAllEvents()) {
            if (type != null && event.getType() != type) continue;
            if (playerId != null && !playerId.equals(event.getPlayerId())) continue;
            if (playerName != null && !playerName.equals(event.getPlayerName())) continue;
            if (from != null && event.getTimestamp().isBefore(from)) continue;
            if (to != null && event.getTimestamp().isAfter(to)) continue;
            if (!text.isEmpty()) {
                String searchable = (event.getType() + " " + event.getPlayerName() + " "
                        + event.getDescription()).toLowerCase(Locale.ROOT);
                if (!searchable.contains(text)) continue;
            }
            result.add(event);
        }
        return List.copyOf(result);
    }

    public List<GameEvent> getLastEvents(int limit) {
        if (limit < 0) throw new IllegalArgumentException("limit cannot be negative");
        List<GameEvent> snapshot = getAllEvents();
        return snapshot.subList(Math.max(0, snapshot.size() - limit), snapshot.size());
    }

    public Map<GameEvent.Type, Long> countByType() {
        Map<GameEvent.Type, Long> counts = new EnumMap<>(GameEvent.Type.class);
        for (GameEvent event : getAllEvents()) counts.merge(event.getType(), 1L, Long::sum);
        return counts;
    }

    public synchronized int size() { return events.size(); }
    public synchronized boolean isEmpty() { return events.isEmpty(); }

    public void clear() {
        synchronized (this) { events.clear(); }
        notifyChange();
    }

    public void exportCSV(Path destination) throws IOException {
        Objects.requireNonNull(destination);
        Path parent = destination.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        List<GameEvent> snapshot = getAllEvents();
        try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
            writer.write("timestamp,type,playerId,playerName,description");
            writer.newLine();
            for (GameEvent event : snapshot) {
                writer.write(csv(event.getTimestamp().toString())); writer.write(',');
                writer.write(csv(event.getType().name())); writer.write(',');
                writer.write(csv(event.getPlayerId() == null ? "" : event.getPlayerId().toString()));
                writer.write(','); writer.write(csv(event.getPlayerName())); writer.write(',');
                writer.write(csv(event.getDescription())); writer.newLine();
            }
        }
    }

    /** Sustituye el historial actual. Ante un CSV inválido no modifica ningún evento. */
    public void importCSV(Path source) throws IOException {
        importCSV(source, false);
    }

    /** @param append true para añadir eventos; false para sustituir los existentes. */
    public void importCSV(Path source, boolean append) throws IOException {
        Objects.requireNonNull(source);
        List<GameEvent> imported = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
            List<List<String>> rows = parseCSV(reader);
            if (rows.isEmpty() || rows.getFirst().size() != 5 ||
                    !rows.getFirst().get(0).replace("\uFEFF", "").strip().equalsIgnoreCase("timestamp") ||
                    !rows.getFirst().get(1).strip().equalsIgnoreCase("type") ||
                    !rows.getFirst().get(2).strip().equalsIgnoreCase("playerId") ||
                    !rows.getFirst().get(3).strip().equalsIgnoreCase("playerName") ||
                    !rows.getFirst().get(4).strip().equalsIgnoreCase("description")) {
                throw new IOException("Invalid CSV header: expected timestamp,type,playerId,playerName,description");
            }
            for (int i = 1; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                if (row.size() != 5) throw new IOException("Row " + (i + 1) + " must have 5 columns");
                try {
                    Instant when = Instant.parse(row.get(0).strip());
                    GameEvent.Type type = GameEvent.Type.valueOf(row.get(1).strip());
                    Integer id = row.get(2).isBlank() ? null : Integer.valueOf(row.get(2).strip());
                    imported.add(new GameEvent(when, type, id, row.get(3), row.get(4)));
                } catch (DateTimeParseException | IllegalArgumentException error) {
                    throw new IOException("Invalid event in CSV row " + (i + 1) + ": " + error.getMessage(), error);
                }
            }
        }
        synchronized (this) {
            if (!append) events.clear();
            events.addAll(imported);
        }
        notifyChange();
    }

    private static String csv(String text) {
        return '"' + (text == null ? "" : text.replace("\"", "\"\"")) + '"';
    }

    /** Lee campos con comas, comillas dobles y saltos de línea dentro de comillas. */
    private static List<List<String>> parseCSV(Reader reader) throws IOException {
        PushbackReader input = new PushbackReader(reader, 1);
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean quoteClosed = false;
        boolean fieldStarted = false;
        int code;
        while ((code = input.read()) != -1) {
            char character = (char) code;
            if (quoted) {
                if (character == '"') {
                    int next = input.read();
                    if (next == '"') field.append('"');
                    else {
                        quoted = false;
                        quoteClosed = true;
                        if (next != -1) input.unread(next);
                    }
                } else field.append(character);
                continue;
            }
            if (character == '"' && !fieldStarted) {
                quoted = true;
                fieldStarted = true;
            } else if (character == ',') {
                row.add(field.toString());
                field.setLength(0);
                fieldStarted = false;
                quoteClosed = false;
            } else if (character == '\n' || character == '\r') {
                if (character == '\r') {
                    int next = input.read();
                    if (next != '\n' && next != -1) input.unread(next);
                }
                row.add(field.toString());
                rows.add(row);
                row = new ArrayList<>();
                field.setLength(0);
                fieldStarted = false;
                quoteClosed = false;
            } else if (character == '"' || quoteClosed) {
                throw new IOException("Invalid CSV quoting");
            } else {
                field.append(character);
                fieldStarted = true;
            }
        }
        if (quoted) throw new IOException("CSV ended inside a quoted field");
        if (fieldStarted || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }
}
