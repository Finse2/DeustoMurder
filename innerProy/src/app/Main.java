
package app;

import activation.Screen.MainScreen;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main {

    private static final Logger LOGGER =
            Logger.getLogger(Main.class.getName());

    private Main() {
    }

    public static void main(String[] args) {

        ApplicationContext context =
                ApplicationContext.getInstance();

        // INITIALIZE JDBC OUTSIDE THE SWING EDT

        try {

            LOGGER.info(
                    "Initializing DeustoMurder..."
            );

            context.initialize();

            LOGGER.info(
                    "Database initialization completed."
            );

            LOGGER.info(
                    "Local player: "
                            + context.getLocalPlayerId()
            );

        } catch (Exception e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Application initialization failed.",
                    e
            );

            context.shutdown();

            SwingUtilities.invokeLater(() ->

                    JOptionPane.showMessageDialog(
                            null,
                            "Could not initialize DeustoMurder.\n"
                                    + e.getMessage(),
                            "Startup Error",
                            JOptionPane.ERROR_MESSAGE
                    )
            );

            return;
        }

        // REGISTER APPLICATION SHUTDOWN

        Runtime.getRuntime().addShutdownHook(
                new Thread(
                        context::shutdown,
                        "Application-Shutdown"
                )
        );

        // CREATE THE MAIN MENU

        SwingUtilities.invokeLater(() -> {

            try {

                MainScreen menu = new MainScreen();

                menu.setVisible(true);

            } catch (RuntimeException e) {

                LOGGER.log(
                        Level.SEVERE,
                        "Could not open the main menu.",
                        e
                );

                context.shutdown();

                JOptionPane.showMessageDialog(
                        null,
                        "Could not open the main menu.\n"
                                + e.getMessage(),
                        "Application Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
