package ui;

import model.Actor;
import model.Room;
import model.Weapon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DetectiveCard extends JPanel {

    private static final Color PAPER = new Color(248, 246, 238);
    private static final Color INK = new Color(25, 25, 25);

    public DetectiveCard(List<Actor> actors, List<Weapon> weapons, List<Room> rooms) {
        setLayout(new BorderLayout());
        setBackground(new Color(220, 220, 215));

        JPanel paper = new JPanel();
        paper.setLayout(new BoxLayout(paper, BoxLayout.Y_AXIS));
        paper.setBackground(PAPER);
        paper.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(INK, 2),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel title = new JLabel("Cluedo: Tarjeta del detective");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 25));
        title.setForeground(INK);
        paper.add(title);
        paper.add(Box.createVerticalStrut(15));

        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        playerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        playerPanel.setBackground(Color.WHITE);
        playerPanel.setBorder(new LineBorder(INK, 1));

        JLabel playerLabel = new JLabel("JUGADOR");
        playerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        playerLabel.setPreferredSize(new Dimension(110, 38));
        playerLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        playerLabel.setForeground(Color.WHITE);
        playerLabel.setBackground(INK);
        playerLabel.setOpaque(true);

        JTextField playerName = new JTextField();
        playerName.setFont(new Font("SansSerif", Font.PLAIN, 14));
        playerName.setBorder(new EmptyBorder(4, 8, 4, 8));

        playerPanel.add(playerLabel, BorderLayout.WEST);
        playerPanel.add(playerName, BorderLayout.CENTER);
        paper.add(playerPanel);
        paper.add(Box.createVerticalStrut(15));

        String[] actorNames = new String[actors.size()];
        for (int i = 0; i < actors.size(); i++) {
            actorNames[i] = actors.get(i).getName();
        }
        addSection(paper, "SOSPECHOSOS", actorNames);

        String[] weaponNames = new String[weapons.size()];
        for (int i = 0; i < weapons.size(); i++) {
            weaponNames[i] = weapons.get(i).getName();
        }
        addSection(paper, "ARMAS", weaponNames);

        String[] roomNames = new String[rooms.size()];
        for (int i = 0; i < rooms.size(); i++) {
            roomNames[i] = rooms.get(i).getName();
        }
        addSection(paper, "UBICACIONES", roomNames);

        JScrollPane scrollPane = new JScrollPane(paper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addSection(JPanel parent, String title, String[] entries) {
        JLabel header = new JLabel(" " + title);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setOpaque(true);
        header.setBackground(INK);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 32));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        parent.add(header);

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(PAPER);
        rows.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String entry : entries) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(PAPER);
            row.setPreferredSize(new Dimension(0, 34));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

            JLabel name = new JLabel(" " + entry);
            name.setFont(new Font("SansSerif", Font.PLAIN, 14));
            name.setForeground(INK);

            JCheckBox check = new JCheckBox();
            check.setOpaque(false);
            check.setFocusPainted(false);

            row.add(name, BorderLayout.CENTER);
            row.add(check, BorderLayout.EAST);
            rows.add(row);
        }

        parent.add(rows);
        parent.add(Box.createVerticalStrut(12));
    }

    public static void showCard(List<Actor> actors, List<Weapon> weapons, List<Room> rooms) {
        JFrame frame = new JFrame("Cluedo - Tarjeta de detective");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(new DetectiveCard(actors, weapons, rooms));
        frame.setSize(520, 800);
        frame.setMinimumSize(new Dimension(450, 600));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        List<Actor> actors = new ArrayList<>();
        List<Weapon> weapons = new ArrayList<>();
        List<Room> rooms = new ArrayList<>();
        showCard(actors, weapons, rooms);
    }
}
