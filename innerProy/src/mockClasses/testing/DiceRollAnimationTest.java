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

/** Test automático, compatible con modo headless (sin abrir ventanas). */
public final class DiceRollAnimationTest {

    public static void main(String[] args) throws Exception {
        CountingDice model = new CountingDice(new GameRandomizer(456789L));
        DiceRollPanel[] panel = new DiceRollPanel[1];
        CountDownLatch completed = new CountDownLatch(2);
        AtomicInteger callbacks = new AtomicInteger();

        SwingUtilities.invokeAndWait(() -> {
            panel[0] = new DiceRollPanel(model, new Random(12345L));
            panel[0].setOnRollComplete((first, second) -> {
                assertCondition(first >= 1 && first <= 6, "Dado 1 fuera de rango");
                assertCondition(second >= 1 && second <= 6, "Dado 2 fuera de rango");
                assertCondition(panel[0].isSettled(), "Debe haber terminado la animación");
                assertCondition(panel[0].getFirstDie() == model.getFirstDie(), "Resultado dado 1 distinto del modelo");
                assertCondition(panel[0].getSecondDie() == model.getSecondDie(), "Resultado dado 2 distinto del modelo");
                assertCondition(panel[0].getTotal() == model.getLastRoll(), "Suma distinta de la lógica");
                System.out.println("OK tirada " + (callbacks.incrementAndGet()) + ": "
                        + first + " + " + second + " = " + panel[0].getTotal());
                completed.countDown();
            });
            assertCondition(!panel[0].isSettled(), "Debe comenzar agitándose");
            assertCondition(panel[0].getTotal() == 0, "Todavía no hay resultado");
            click(panel[0]);
            assertCondition(panel[0].isRolling(), "El clic debe iniciar la caída");
            assertCondition(model.rollCalls == 1, "Un clic debe generar una tirada real");
            click(panel[0]);
            assertCondition(panel[0].isRolling(), "Un segundo clic no debe interrumpirla");
            assertCondition(model.rollCalls == 1, "No se debe lanzar de nuevo mientras cae");
        });

        waitForCallbacks(completed, callbacks, 1);
        SwingUtilities.invokeAndWait(() -> {
            assertCondition(panel[0].isSettled(), "La primera tirada debe estar detenida");
            click(panel[0]);
            assertCondition(panel[0].isRolling(), "Debe permitir volver a lanzar");
            assertCondition(model.rollCalls == 2, "La segunda pulsación debe lanzar otra tirada");
            assertCondition(panel[0].getTotal() == 0, "El resultado antiguo debe ocultarse");
        });
        waitForCallbacks(completed, callbacks, 2);
        assertCondition(callbacks.get() == 2, "Deben recibirse exactamente 2 callbacks");
        System.out.println("TODAS LAS PRUEBAS PASARON");
    }

    private static void click(DiceRollPanel panel) {
        panel.dispatchEvent(new MouseEvent(panel, MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, 120, 120, 1, false, MouseEvent.BUTTON1));
    }

    private static final class CountingDice extends Dice {
        private int rollCalls;
        private CountingDice(GameRandomizer randomizer) { super(randomizer); }
        @Override public int roll() { rollCalls++; return super.roll(); }
    }

    private static void waitForCallbacks(CountDownLatch completed, AtomicInteger callbacks, int expected) throws Exception {
        long limit = System.nanoTime() + TimeUnit.SECONDS.toNanos(6);
        while (callbacks.get() < expected && System.nanoTime() < limit) {
            if (completed.await(50, TimeUnit.MILLISECONDS)) break;
        }
        assertCondition(callbacks.get() >= expected, "Timeout: tirada " + expected + " no ha terminado");
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
