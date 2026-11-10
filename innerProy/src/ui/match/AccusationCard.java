package ui.match;

import model.Actor;
import model.Room;
import model.Weapon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class AccusationCard extends JPanel {

    private static final Color PAPER = new Color(248, 246, 238);
    private static final Color INK = new Color(25, 25, 25);

    private Actor selectedActor;
    private Weapon selectedWeapon;
    private Room selectedRoom;

    private final JButton confirmButton;
    private final Consumer<AccusationSelection> onConfirm;

    public AccusationCard(
            List<Actor> actors,
            List<Weapon> weapons,
            List<Room> rooms,
            Consumer<AccusationSelection> onConfirm) {

        this.onConfirm = onConfirm;

        setLayout(new BorderLayout());
        setBackground(new Color(220, 220, 215));

        // PAPEL DE LA TARJETA
        JPanel paper = new JPanel();
        paper.setLayout(new BoxLayout(paper, BoxLayout.Y_AXIS));
        paper.setBackground(PAPER);

        paper.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(INK, 2),
                new EmptyBorder(18, 18, 18, 18)
        ));

        // TÍTULO
        JLabel title = new JLabel("Cluedo: Tarjeta de acusación");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 25));
        title.setForeground(INK);

        paper.add(title);
        paper.add(Box.createVerticalStrut(15));

        // JUGADOR
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

        // SOSPECHOSOS
        addSection(
                paper,
                "SOSPECHOSOS",
                actors,
                Actor::getName,
                actor -> selectedActor = actor
        );

        // ARMAS
        addSection(
                paper,
                "ARMAS",
                weapons,
                Weapon::getName,
                weapon -> selectedWeapon = weapon
        );

        // UBICACIONES
        addSection(
                paper,
                "UBICACIONES",
                rooms,
                Room::getName,
                room -> selectedRoom = room
        );

        // SCROLL
        JScrollPane scrollPane = new JScrollPane(paper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // BOTÓN CONFIRMAR
        confirmButton = new JButton("CONFIRMAR");
        confirmButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBackground(Color.GREEN);
        confirmButton.setFocusPainted(false);
        confirmButton.setEnabled(false);
        confirmButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        confirmButton.addActionListener(e -> {
            if (selectedActor != null
                    && selectedWeapon != null
                    && selectedRoom != null) {

                AccusationSelection selection = new AccusationSelection(
                        selectedActor,
                        selectedWeapon,
                        selectedRoom
                );

                if (this.onConfirm != null) {
                    this.onConfirm.accept(selection);
                }

                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.dispose();
                }
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(PAPER);
        bottomPanel.setBorder(new EmptyBorder(8, 0, 8, 0));
        bottomPanel.add(confirmButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Crea una sección con opciones de selección única.
     */
    private <T> void addSection(
            JPanel parent,
            String title,
            List<T> entries,
            Function<T, String> getName,
            Consumer<T> onSelection) {

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

        // Este grupo permite seleccionar solamente una opción.
        ButtonGroup group = new ButtonGroup();

        for (T entry : entries) {

            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(PAPER);
            row.setPreferredSize(new Dimension(0, 34));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

            JLabel name = new JLabel(" " + getName.apply(entry));
            name.setFont(new Font("SansSerif", Font.PLAIN, 14));
            name.setForeground(INK);

            JRadioButton option = new JRadioButton();
            option.setOpaque(false);
            option.setFocusPainted(false);

            group.add(option);

            option.addActionListener(e -> {
                onSelection.accept(entry);
                updateConfirmButton();
            });

            row.add(name, BorderLayout.CENTER);
            row.add(option, BorderLayout.EAST);

            // También permite seleccionar haciendo clic en el nombre.
            name.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            name.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    option.setSelected(true);
                    onSelection.accept(entry);
                    updateConfirmButton();
                }
            });

            rows.add(row);
        }

        parent.add(rows);
        parent.add(Box.createVerticalStrut(12));
    }

    /**
     * Activa Confirmar solamente cuando las tres categorías están seleccionadas.
     */
    private void updateConfirmButton() {
        confirmButton.setEnabled(
                selectedActor != null
                        && selectedWeapon != null
                        && selectedRoom != null
        );
    }

    /**
     * Devuelve la acusación seleccionada.
     */
    public record AccusationSelection(
            Actor actor,
            Weapon weapon,
            Room room
    ) {}

    /**
     * Abre la ventana de acusación.
     */
    public static void showCard(
            List<Actor> actors,
            List<Weapon> weapons,
            List<Room> rooms,
            Consumer<AccusationSelection> onConfirm) {

        JFrame frame = new JFrame("Cluedo - Tarjeta de acusación");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        AccusationCard card = new AccusationCard(
                actors,
                weapons,
                rooms,
                onConfirm
        );

        frame.setContentPane(card);
        frame.setSize(520, 800);
        frame.setMinimumSize(new Dimension(450, 600));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}