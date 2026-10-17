package app;

import ui.match.InGameScreen;
import ui.menu.MainScreen;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * central entry point
 * game data and Swing window layout are handled elsewhere.
 */
public final class Main {

    private Main() {
        // utility entry-point class: not instantiated.
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::showMainMenu);
    }

    private static void showMainMenu() {
        MainScreen menu = new MainScreen(
                Main::openLocalGame,
                Main::showJoinNotImplemented
        );
        menu.setVisible(true);
    }

    private static void openLocalGame() {
        InGameScreen gameScreen = new InGameScreen();
        gameScreen.setVisible(true);
    }

    private static void showJoinNotImplemented() {
        JOptionPane.showMessageDialog(
                null,
                "Joining a network game has not been connected to the menu yet.",
                "Join Game",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
