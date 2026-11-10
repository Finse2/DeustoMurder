package mockClasses;

import game.core.Game;
import game.core.GameSetup;
import game.core.GameStartResult;
import game.history.GameEvent;
import game.history.GameHistory;
import game.history.GameHistoryWriterThread;
import mockClasses.testing.GameStartTest;
import game.turns.TurnTimerThread;
import model.Actor;
import model.Player;
import ui.match.inGameScreen.GameHistoryPanel;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Mock único para verificar:
 * 1. TurnTimerThread            2. Game
 * 3. GameHistory                4. GameHistoryPanel
 * 5. GameEvent                  6. GameHistoryWriterThread
 * 7. GameStartTest
 *
 * Ejecutar mockClasses.TurnHistoryIntegrationMock para abrir la ventana.
 * Ejecutar con --self-test para verificar los 7 componentes en consola.
 */
public final class TurnHistoryIntegrationMock {

    private final JFrame frame = new JFrame("DeustoMurder - Mock de turnos e historial");
    private final JPanel historyHost = new JPanel(new BorderLayout());
    private final JTextArea checks = new JTextArea();
    private final JLabel playerLabel = new JLabel("Sin partida", SwingConstants.CENTER);
    private final JLabel timeLabel = new JLabel("00:00", SwingConstants.CENTER);
    private final JLabel stateLabel = new JLabel("Pulsa Iniciar partida", SwingConstants.CENTER);
    private final JSpinner seconds = new JSpinner(new SpinnerNumberModel(5, 1, 300, 1));
    private final JButton startButton = new JButton("Iniciar partida");
    private final JButton pauseButton = new JButton("Pausar");
    private final JButton resumeButton = new JButton("Reanudar");
    private final JButton nextButton = new JButton("Siguiente turno");
    private final JButton finishButton = new JButton("Finalizar");
    private final JButton testButton = new JButton("Ejecutar 7 pruebas");
    private final Path csvFile = Path.of("logs", "mock-turn-history.csv");

    private Game game;
    private GameHistoryPanel historyPanel;
    private boolean paused;
    private int createdEvents;

    private TurnHistoryIntegrationMock() {
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(1160, 760);
        frame.setLocationRelativeTo(null);

        playerLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 54));
        stateLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        checks.setEditable(false);
        checks.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        checks.setLineWrap(true);
        checks.setWrapStyleWord(true);
        checks.setText("Mock unificado de 7 componentes.\n"
                + "1) Inicia la partida.\n"
                + "2) Pausa, reanuda y cambia turnos.\n"
                + "3) Genera eventos y prueba los filtros del historial.\n"
                + "4) Exporta/importa CSV o ejecuta las pruebas automáticas.\n\n");

        JPanel timerLabels = new JPanel(new GridLayout(3, 1, 0, 3));
        timerLabels.add(playerLabel);
        timerLabels.add(timeLabel);
        timerLabels.add(stateLabel);

        JPanel gameControls = new JPanel(new GridLayout(0, 2, 6, 6));
        gameControls.add(new JLabel("Segundos por turno:"));
        gameControls.add(seconds);
        gameControls.add(startButton);
        gameControls.add(finishButton);
        gameControls.add(pauseButton);
        gameControls.add(resumeButton);
        gameControls.add(nextButton);
        gameControls.add(new JLabel(""));

        JButton addEvent = new JButton("Añadir movimiento");
        JButton addTurnEvent = new JButton("Añadir sospecha");
        JButton export = new JButton("Exportar CSV");
        JButton importFile = new JButton("Importar CSV");
        JButton addMany = new JButton("+250 eventos");
        JButton clear = new JButton("Limpiar historial");

        JPanel historyControls = new JPanel(new GridLayout(0, 2, 6, 6));
        historyControls.setBorder(BorderFactory.createTitledBorder("Eventos y persistencia"));
        historyControls.add(addEvent);
        historyControls.add(addTurnEvent);
        historyControls.add(export);
        historyControls.add(importFile);
        historyControls.add(addMany);
        historyControls.add(clear);

        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel leftTop = new JPanel(new BorderLayout(0, 8));
        leftTop.add(timerLabels, BorderLayout.NORTH);
        leftTop.add(gameControls, BorderLayout.CENTER);
        left.add(leftTop, BorderLayout.NORTH);

        JPanel leftBottom = new JPanel(new BorderLayout(0, 6));
        leftBottom.add(historyControls, BorderLayout.NORTH);
        leftBottom.add(testButton, BorderLayout.CENTER);
        leftBottom.add(new JScrollPane(checks), BorderLayout.SOUTH);
        ((JScrollPane) leftBottom.getComponent(2)).setPreferredSize(new Dimension(360, 220));
        left.add(leftBottom, BorderLayout.CENTER);

        historyHost.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, historyHost);
        split.setResizeWeight(0.35);
        split.setDividerLocation(410);
        frame.add(split);

        startButton.addActionListener(e -> startGame());
        pauseButton.addActionListener(e -> {
            if (game != null) { game.pauseTurnTimer(); paused = true; updateState(); }
        });
        resumeButton.addActionListener(e -> {
            if (game != null) { game.resumeTurnTimer(); paused = false; updateState(); }
        });
        nextButton.addActionListener(e -> {
            if (game != null) { game.nextTurn(); paused = false; updateState(); }
        });
        finishButton.addActionListener(e -> {
            if (game != null) game.closeGame();
            paused = false;
            updateState();
        });
        addEvent.addActionListener(e -> addEvent(GameEvent.Type.PLAYER_MOVED));
        addTurnEvent.addActionListener(e -> addEvent(GameEvent.Type.SUGGESTION_MADE));
        addMany.addActionListener(e -> {
            for (int i = 0; i < 250; i++) addEvent(GameEvent.Type.CUSTOM);
        });
        clear.addActionListener(e -> {
            if (game != null) game.getGameHistory().clear();
        });
        export.addActionListener(e -> fileAction(true));
        importFile.addActionListener(e -> fileAction(false));
        testButton.addActionListener(e -> runTestsInBackground());
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                if (historyPanel != null) historyPanel.bindTimer(null);
                if (game != null) game.closeGame();
            }
        });
        startGame();
    }

    private static Game createGame(int turnSeconds) {
        Game created = new Game();
        created.setTurnDurationSeconds(turnSeconds);
        List<Actor> actors = GameSetup.createActors();
        for (int i = 0; i < 3; i++)
            created.addPlayer(new Player(actors.get(i), new ArrayList<>(), i));
        return created;
    }

    private void startGame() {
        if (historyPanel != null) historyPanel.bindTimer(null);
        if (game != null) game.closeGame();
        Game created = createGame((Integer) seconds.getValue());
        game = created;
        paused = false;
        createdEvents = 0;
        historyPanel = new GameHistoryPanel(created.getGameHistory(), GameSetup.createActors());
        historyPanel.bindTimer(created);
        historyHost.removeAll();
        historyHost.add(historyPanel, BorderLayout.CENTER);
        historyHost.revalidate();
        historyHost.repaint();

        created.addTurnTimeListener(t -> SwingUtilities.invokeLater(() -> {
            if (game == created) {
                timeLabel.setText(String.format("%02d:%02d", t / 60, t % 60));
                updateState();
            }
        }));
        created.getGameHistory().addChangeListener(() -> SwingUtilities.invokeLater(() -> {
            if (game == created) updateState();
        }));

        GameStartResult result = created.startGame();
        if (!result.isSuccessful()) {
            checks.append("[ERROR] No se inicia la partida: " + result.getMessage() + "\n");
            created.closeGame();
        }
        updateState();
    }

    private void addEvent(GameEvent.Type type) {
        if (game == null) return;
        createdEvents++;
        Player player = game.getCurrentPlayer();
        game.getGameHistory().record(type,
                player == null ? null : player.getPlayerID(),
                player == null ? "" : player.getName(),
                "Evento mock #" + createdEvents + ": buscando texto, CSV y filtros");
    }

    private void fileAction(boolean export) {
        if (game == null) return;
        try {
            if (export) game.getGameHistory().exportCSV(csvFile);
            else game.getGameHistory().importCSV(csvFile);
            checks.append((export ? "Exportado" : "Importado") + ": " + csvFile + "\n");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "CSV", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateState() {
        boolean active = game != null && game.isInitialized() && !game.isGameOver();
        Player current = active ? game.getCurrentPlayer() : null;
        playerLabel.setText(current == null ? "Sin turno activo" : "Turno de " + current.getName());
        stateLabel.setText(!active ? "Partida finalizada" : paused ? "PAUSADO" : "Cuenta atrás activa");
        if (!active) timeLabel.setText("00:00");
        seconds.setEnabled(!active);
        pauseButton.setEnabled(active && !paused);
        resumeButton.setEnabled(active && paused);
        nextButton.setEnabled(active);
        finishButton.setEnabled(active);
    }

    @FunctionalInterface
    private interface CheckedTest { void run() throws Exception; }

    private static void ensure(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static boolean until(long milliseconds, java.util.function.BooleanSupplier condition)
            throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(milliseconds);
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) return true;
            Thread.sleep(25);
        }
        return condition.getAsBoolean();
    }

    private static void testGameEvent() {
        Instant timestamp = Instant.parse("2025-02-03T04:05:06Z");
        GameEvent a = new GameEvent(GameEvent.Type.CUSTOM, 1, "Ana", "Original");
        GameEvent b = new GameEvent(timestamp, GameEvent.Type.CUSTOM, 1, "Ana", "Importado");
        ensure(a.getTimestamp() != null, "Constructor original sin fecha");
        ensure(b.getTimestamp().equals(timestamp), "Constructor con Instant no conserva fecha");
        ensure(b.getDescription().equals("Importado"), "Descripción incorrecta");
    }

    private static void testGameHistory() throws Exception {
        GameHistory history = new GameHistory();
        Instant date = Instant.parse("2025-02-03T04:05:06Z");
        String description = "Texto, con \"comillas\" y\nsalto de línea";
        history.record(new GameEvent(date, GameEvent.Type.CUSTOM, 4, "Ana, María", description));
        history.record(GameEvent.Type.TURN_STARTED, 5, "Luka", "Empieza turno");
        ensure(history.filterEvents("COMILLAS", null, null, null, null, null).size() == 1,
                "La búsqueda no encuentra la descripción");
        ensure(history.filterEvents(null, GameEvent.Type.TURN_STARTED, 5, "Luka", null, null).size() == 1,
                "El filtro tipo/jugador no funciona");
        ensure(history.filterEvents(null, null, null, null, date.plusSeconds(1), null).size() == 1,
                "El filtro por fecha no funciona");

        Path file = Files.createTempFile("deustomurder-mock-history-", ".csv");
        try {
            history.exportCSV(file);
            history.clear();
            history.importCSV(file);
            ensure(history.size() == 2, "CSV no recupera los 2 eventos");
            GameEvent recovered = history.getAllEvents().getFirst();
            ensure(recovered.getTimestamp().equals(date), "CSV no conserva fecha");
            ensure(recovered.getPlayerName().equals("Ana, María"), "CSV no conserva las comas");
            ensure(recovered.getDescription().equals(description), "CSV no conserva comillas/saltos");
            history.importCSV(file, true);
            ensure(history.size() == 4, "La importación append no añade los eventos");
            Files.writeString(file, "invalid-header\n", StandardCharsets.UTF_8);
            try {
                history.importCSV(file);
                throw new AssertionError("Un CSV inválido se aceptó sin error");
            } catch (IOException expected) {
                ensure(history.size() == 4, "Un CSV inválido modificó el historial");
            }
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void testPanel() throws Exception {
        GameHistory history = new GameHistory();
        GameHistoryPanel[] panels = new GameHistoryPanel[1];
        SwingUtilities.invokeAndWait(() -> panels[0] = new GameHistoryPanel(history, List.of()));
        history.record(GameEvent.Type.GAME_STARTED, null, "", "Comienzo");
        history.record(GameEvent.Type.CUSTOM, 1, "Ana", "Prueba de panel");
        SwingUtilities.invokeAndWait(() -> { });
        ensure(panels[0].getVisibleEventCount() == 2, "El panel no muestra dos eventos");
        history.clear();
        SwingUtilities.invokeAndWait(() -> { });
        ensure(panels[0].getVisibleEventCount() == 0, "El panel no se actualiza al limpiar");
    }

    private static void testTimer() throws Exception {
        AtomicInteger ticks = new AtomicInteger();
        AtomicInteger expired = new AtomicInteger();
        TurnTimerThread timer = new TurnTimerThread(2, ticks::set, expired::incrementAndGet);
        timer.start();
        try {
            Thread.sleep(100);
            timer.pauseTimer();
            int paused = timer.getRemainingSeconds();
            Thread.sleep(1200);
            ensure(timer.getRemainingSeconds() == paused, "El temporizador corre en pausa");
            timer.resumeTimer();
            ensure(until(3500, () -> expired.get() == 1), "No expira tras reanudar");
            ensure(timer.getRemainingSeconds() == 0, "El contador no acaba en 0");
            ensure(ticks.get() == 0, "Falta el tick final");
        } finally {
            timer.stopTimer();
            timer.join(2000);
        }
        ensure(expired.get() == 1, "El timeout se ejecutó más de una vez");
        AtomicInteger cancelled = new AtomicInteger();
        TurnTimerThread stopped = new TurnTimerThread(1, s -> {}, cancelled::incrementAndGet);
        stopped.start();
        stopped.stopTimer();
        stopped.join(2000);
        ensure(cancelled.get() == 0, "Un temporizador cancelado ejecutó el timeout");
    }

    private static void testGame() throws Exception {
        Game game = createGame(2);
        try {
            GameStartResult result = game.startGame();
            ensure(result.isSuccessful(), "No inicia Game: " + result.getMessage());
            ensure(game.getGameHistory().getEventsOfType(GameEvent.Type.GAME_STARTED).size() == 1,
                    "Game no registra GAME_STARTED");
            ensure(game.getGameHistory().getEventsOfType(GameEvent.Type.TURN_STARTED).size() == 1,
                    "Game no registra el primer turno");
            Thread.sleep(100);
            game.pauseTurnTimer();
            int frozen = game.getTurnRemainingSeconds();
            Thread.sleep(1200);
            ensure(game.getTurnRemainingSeconds() == frozen, "Game no pausa el temporizador");
            game.resumeTurnTimer();
            ensure(until(3500, () -> game.getGameHistory()
                    .getEventsOfType(GameEvent.Type.TURN_STARTED).size() >= 2),
                    "Game no pasa de turno automáticamente");
            int previous = game.getGameHistory().getEventsOfType(GameEvent.Type.TURN_STARTED).size();
            game.nextTurn();
            ensure(game.getGameHistory().getEventsOfType(GameEvent.Type.TURN_STARTED).size() == previous + 1,
                    "Game no pasa el turno manualmente");
        } finally {
            game.closeGame();
        }
        ensure(game.getTurnRemainingSeconds() == 0, "Game no detiene el reloj al cerrar");
        ensure(game.getGameHistory().getEventsOfType(GameEvent.Type.GAME_FINISHED).size() == 1,
                "Game no registra el fin de partida");
    }

    private static void testWriter() throws Exception {
        GameHistory history = new GameHistory();
        Path file = Files.createTempFile("deustomurder-writer-", ".csv");
        try {
            history.record(GameEvent.Type.GAME_STARTED, null, "", "Inicio");
            GameHistoryWriterThread writer = new GameHistoryWriterThread(history, file);
            writer.start();
            history.record(GameEvent.Type.GAME_FINISHED, null, "", "Último evento");
            writer.stopWriter();
            writer.join(3000);
            ensure(!writer.isAlive(), "El hilo writer sigue activo");
            GameHistory imported = new GameHistory();
            imported.importCSV(file);
            ensure(imported.size() == 2, "El writer perdió el último evento");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void testGameStartTest() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream previous = System.out;
        try (PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            try {
                GameStartTest.main(new String[0]);
            } finally {
                System.setOut(previous);
            }
        }
        String output = buffer.toString(StandardCharsets.UTF_8);
        ensure(output.contains("Passed: 13") && output.contains("Failed: 0"),
                "GameStartTest no supera sus 13 casos:\n" + output);
    }

    private static int runAll(Consumer<String> output) {
        int passed = 0;
        passed += record(output, "GameEvent (constructores y fecha)", TurnHistoryIntegrationMock::testGameEvent);
        passed += record(output, "GameHistory (búsqueda, filtros, CSV)", TurnHistoryIntegrationMock::testGameHistory);
        passed += record(output, "GameHistoryPanel (actualización Swing)", TurnHistoryIntegrationMock::testPanel);
        passed += record(output, "TurnTimerThread (pausa y timeout)", TurnHistoryIntegrationMock::testTimer);
        passed += record(output, "Game (turnos automáticos y cierre)", TurnHistoryIntegrationMock::testGame);
        passed += record(output, "GameHistoryWriterThread (guardado final)", TurnHistoryIntegrationMock::testWriter);
        passed += record(output, "GameStartTest (13 pruebas originales)", TurnHistoryIntegrationMock::testGameStartTest);
        output.accept("Resultado: " + passed + "/7 componentes correctos");
        return passed;
    }

    private static int record(Consumer<String> output, String name, CheckedTest test) {
        try {
            test.run();
            output.accept("[PASS] " + name);
            return 1;
        } catch (Throwable error) {
            output.accept("[FAIL] " + name + " -> " + error);
            return 0;
        }
    }

    private void runTestsInBackground() {
        testButton.setEnabled(false);
        checks.append("\n======= PRUEBAS AUTOMÁTICAS =======\n");
        new SwingWorker<Void, String>() {
            @Override protected Void doInBackground() {
                int passed = runAll(this::publish);
                publish(passed == 7 ? "[OK] 7/7 componentes verificados"
                        : "[ERROR] " + (7 - passed) + " componentes fallaron");
                return null;
            }
            @Override protected void process(List<String> lines) {
                for (String line : lines) checks.append(line + "\n");
                checks.setCaretPosition(checks.getDocument().getLength());
            }
            @Override protected void done() { testButton.setEnabled(true); }
        }.execute();
    }

    public static void main(String[] args) {
        if (args.length > 0 && "--self-test".equals(args[0])) {
            System.setProperty("java.awt.headless", "true");
            int passed = runAll(System.out::println);
            if (passed != 7) throw new AssertionError((7 - passed) + " componentes fallaron");
            System.out.println("[OK] 7/7 componentes verificados");
        } else {
            SwingUtilities.invokeLater(() -> new TurnHistoryIntegrationMock().frame.setVisible(true));
        }
    }
}
