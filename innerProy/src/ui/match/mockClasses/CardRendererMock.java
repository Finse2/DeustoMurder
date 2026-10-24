package ui.match.mockClasses;

import model.Card;
import ui.match.PlayersCardsRenderer;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CardRendererMock {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            Path image = Path.of(
                    "/Users/nistaltothantal/Desktop/world/programacion/Java/DeustoMurder/innerProy/ressources/cardAspects/librería.png"
            );

            List<Card> cards = new ArrayList<>();

            cards.add(new Card(image, "Librería") {});
            cards.add(new Card(image, "Comedor") {});
            cards.add(new Card(image, "Jardín") {});
            cards.add(new Card(image, "Salón") {});
            cards.add(new Card(image, "Biblioteca") {});
            cards.add(new Card(image, "Pasillo") {});
            cards.add(new Card(image, "Cocina") {});

            JFrame frame = new JFrame("Player Hand Test");

            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            frame.getContentPane().setBackground(
                    new Color(30, 110, 30)
            );

            PlayersCardsRenderer handRenderer =
                    new PlayersCardsRenderer(
                            cards,
                            300,
                            200,
                            22,
                            40,
                            1100
                    );

            frame.add(handRenderer, BorderLayout.CENTER);

            frame.setSize(1300, 750);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}