package mockClasses.testing;

import game.dice.Dice;
import game.dice.GameRandomizer;
import ui.match.inGameScreen.DiceRollPanel;

import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Comprueba pulsar/soltar, timeout sin interaccion y timeout manteniendo pulsado. */
public final class DiceRollAnimationTest {

    public static void main(String[] args) throws Exception {
        CountingDice model = new CountingDice(new GameRandomizer(456789L));
        DiceRollPanel[] holder = new DiceRollPanel[1];
        AtomicInteger callbacks = new AtomicInteger();
        CountDownLatch finished = new CountDownLatch(3);

        SwingUtilities.invokeAndWait(() -> {
            DiceRollPanel panel = new DiceRollPanel(model, new Random(12345L));
            holder[0] = panel;
            panel.setOnRollComplete((first, second) -> {
                check(first >= 1 && first <= 6 && second >= 1 && second <= 6, "Caras fuera de rango");
                check(panel.isSettled(), "Debe terminar la caida antes de publicar resultado");
                check(!panel.isVisible(), "La capa debe desaparecer al finalizar");
                check(panel.getFirstDie() == model.getFirstDie(), "Primer dado distinto al modelo");
                check(panel.getSecondDie() == model.getSecondDie(), "Segundo dado distinto al modelo");
                check(panel.getTopFace(0) == first && panel.getTopFace(1) == second,
                        "Caras 3D incoherentes con resultado");
                check(panel.getTotal() == model.getLastRoll(), "Suma erronea");
                System.out.println("OK tirada " + callbacks.incrementAndGet() + ": " + first + " + " + second);
                finished.countDown();
            });
            panel.prepareRoll();
            check(panel.isWaiting(), "La tirada debe empezar esperando al usuario");
            send(panel, MouseEvent.MOUSE_PRESSED);
            check(panel.isHolding(), "Pulsar debe iniciar la agitacion");
            send(panel, MouseEvent.MOUSE_RELEASED);
            check(panel.isRolling(), "Soltar debe iniciar la caida");
            send(panel, MouseEvent.MOUSE_RELEASED);
            check(model.rollCalls == 1, "Dos eventos no deben duplicar la tirada");
        });

        await(callbacks, 1, 5);
        SwingUtilities.invokeAndWait(() -> {
            holder[0].prepareRoll();
            check(holder[0].isWaiting(), "Debe permitir una nueva tirada");
            // Nadie toca los dados: se lanzan automaticamente a los 5 segundos.
        });
        await(callbacks, 2, 9);
        SwingUtilities.invokeAndWait(() -> {
            holder[0].prepareRoll();
            send(holder[0], MouseEvent.MOUSE_PRESSED);
            check(holder[0].isHolding(), "Debe agitarse durante la pulsacion");
            // Sin soltar: despues de 5 s se activa la caida automatica.
        });
        await(callbacks, 3, 9);
        check(model.rollCalls == 3, "Cada ronda debe tirar los dados exactamente una vez");
        check(finished.getCount() == 0, "Faltan callbacks");
        System.out.println("TODAS LAS PRUEBAS DE DADOS PASARON");
    }

    private static void send(DiceRollPanel panel, int type) {
        panel.dispatchEvent(new MouseEvent(panel, type, System.currentTimeMillis(), 0,
                120, 120, 1, false, MouseEvent.BUTTON1));
    }

    private static void await(AtomicInteger count, int expected, int timeoutSeconds) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds);
        while (count.get() < expected && System.nanoTime() < deadline) Thread.sleep(50);
        check(count.get() == expected, "Timeout en la tirada " + expected);
    }

    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }

    private static final class CountingDice extends Dice {
        private int rollCalls;
        CountingDice(GameRandomizer randomizer) { super(randomizer); }
        @Override public int roll() { rollCalls++; return super.roll(); }
    }
}
