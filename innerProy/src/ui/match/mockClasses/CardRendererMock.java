package ui.match.mockClasses;

import utils.AllPaths.Resources.DefaultCardModels.ActorsData;
import utils.AllPaths.Resources.DefaultCardModels.RoomsData;
import utils.AllPaths.Resources.DefaultCardModels.WeaponsData;

import model.Actor;
import model.Card;
import model.Room;
import model.Weapon;
import ui.match.PlayersCardsRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**AI made*/
public final class CardRendererMock {

    private static final Color MEDAL_COLOR = new Color(204, 172, 124);
    private static final Color BACKDROP_COLOR = new Color(23, 29, 38);

    private CardRendererMock() {
    }

    private static List<Card> createRandomHand() {
        List<Card> cards = new ArrayList<>();

        List<ActorsData> actors = new ArrayList<>(Arrays.asList(ActorsData.values()));
        List<WeaponsData> weapons = new ArrayList<>(Arrays.asList(WeaponsData.values()));
        List<RoomsData> rooms = new ArrayList<>(Arrays.asList(RoomsData.values()));

        Collections.shuffle(actors);
        Collections.shuffle(weapons);
        Collections.shuffle(rooms);

        for (int i = 0; i < 2; i++) {
            ActorsData data = actors.get(i);
            Actor actor = new Actor(data.getName(), data.getColor());
            actor.setNameToRender(data.getName());
            actor.setRenderImages(data.getMainBackgroundImage(), data.getFrontCircleImage());
            actor.setCardBackGroundColor(data.getColor().darker().darker());
            actor.setCardFrontCircleColor(MEDAL_COLOR);
            cards.add(actor);
        }

        for (int i = 0; i < 2; i++) {
            WeaponsData data = weapons.get(i);
            Weapon weapon = new Weapon(data.getName());
            weapon.setNameToRender(data.getName());
            weapon.setRenderImages(data.getMainBackgroundImage(), data.getFrontCircleImage());
            weapon.setCardBackGroundColor(matchingIconColor(data.getFrontCircleImage()));
            weapon.setCardFrontCircleColor(MEDAL_COLOR);
            cards.add(weapon);
        }

        for (int i = 0; i < 2; i++) {
            RoomsData data = rooms.get(i);
            Room room = new Room(data.getName());
            room.setNameToRender(data.getName());
            room.setRenderImages(data.getMainBackgroundImage(), data.getFrontCircleImage());
            room.setCardBackGroundColor(matchingIconColor(data.getFrontCircleImage()));
            room.setCardFrontCircleColor(MEDAL_COLOR);
            cards.add(room);
        }

        Collections.shuffle(cards);
        return cards;
    }

    /** Sample a clear area inside the circular background, away from the 2D symbol. */
    private static Color matchingIconColor(BufferedImage badge) {
        int x = Math.max(0, badge.getWidth() / 8);
        int y = badge.getHeight() / 2;
        return new Color(badge.getRGB(x, y), true);
    }

    private static void showPreview() {
        List<Card> cards = createRandomHand();
        PlayersCardsRenderer hand = new PlayersCardsRenderer(cards, 300, 200, 20, 48, 1080);

        JFrame frame = new JFrame("DeustoMurder - Player Hand Mock");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel background = new JPanel(new BorderLayout(10, 8));
        background.setBackground(BACKDROP_COLOR);
        background.setBorder(BorderFactory.createEmptyBorder(16, 18, 12, 18));

        JLabel heading = new JLabel("Player's cards - 2 actors, 2 weapons, 2 rooms", SwingConstants.CENTER);
        heading.setForeground(new Color(235, 223, 204));
        heading.setFont(new Font("Serif", Font.BOLD, 21));
        background.add(heading, BorderLayout.NORTH);
        background.add(hand, BorderLayout.CENTER);

        JButton shuffleButton = new JButton("Shuffle a new hand");
        shuffleButton.addActionListener(event -> hand.setCards(createRandomHand()));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.add(shuffleButton);
        background.add(footer, BorderLayout.SOUTH);

        frame.setContentPane(background);
        frame.setSize(1170, 770);
        frame.setMinimumSize(new Dimension(690, 650));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CardRendererMock::showPreview);




    }
}
