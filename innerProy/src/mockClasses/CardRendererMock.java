
package mockClasses;

import model.Actor;
import model.Card;
import model.Room;
import model.Weapon;
import ui.match.cardDisplayer.PlayersCardsRenderer;
import ui.match.cardDisplayer.PlayersHandsRendererHeader;
import utils.AllPaths.DefaultCardModels.ActorsData;
import utils.AllPaths.DefaultCardModels.RoomsData;
import utils.AllPaths.DefaultCardModels.WeaponsData;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class CardRendererMock {

    static Object[] createHand() {
        List<Card> cards = new ArrayList<>();

        for (ActorsData data : ActorsData.values()) {
            Actor card = new Actor(data.getName(), data.getColor());
            card.setNameToRender(data.getName());
            card.setRenderImages(data.getMainBackgroundImage(), data.getFrontCircleImage());
            card.setCardBackGroundColor(data.getColor().darker().darker());
            cards.add(card);
        }

        for (WeaponsData data : WeaponsData.values()) {
            Weapon card = new Weapon(data.getName());
            card.setNameToRender(data.getName());
            card.setRenderImages(data.getMainBackgroundImage(), data.getFrontCircleImage());
            card.setCardBackGroundColor(new Color(60, 35, 25));
            cards.add(card);
        }

        for (RoomsData data : new RoomsData[]{
                RoomsData.AULA_E208,
                RoomsData.POLIDEPORTIVO,
                RoomsData.DECANATO
        }) {
            Room card = new Room(data.getName());
            card.setNameToRender(data.getName());
            card.setRenderImages(data.getMainBackgroundImage(), data.getFrontCircleImage());
            card.setCardBackGroundColor(new Color(20, 50, 70));
            cards.add(card);
        }

        return cards.toArray();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            PlayersHandsRendererHeader[] headers = {
                    new PlayersHandsRendererHeader(Actor.class, 15),
                    new PlayersHandsRendererHeader(Room.class, 0),
                    new PlayersHandsRendererHeader(Weapon.class, 0)
            };

            JFrame frame = new JFrame("DeustoMurder - Player Cards");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1550, 335);
            frame.setMinimumSize(new Dimension(900, 300));
            frame.setLocationRelativeTo(null);

            JPanel background = new JPanel(new BorderLayout());
            background.setBackground(new Color(10, 25, 40));
            background.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            background.add(
                    new PlayersCardsRenderer(headers, createHand()),
                    BorderLayout.CENTER
            );

            frame.setContentPane(background);
            frame.setVisible(true);
        });
    }
}
