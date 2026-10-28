package ui.match;

import model.Card;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayersCardsRenderer extends JPanel {

    private List<Card> cards;
    private final List<CardRenderer> renderers = new ArrayList<>();

    private final int cardHeight;
    private final int cardWidth;
    private final int maxRotationDegrees;

    private int minStep;
    private int maxHandWidth;

    private static final int ARC_DEPTH = 65;

    public PlayersCardsRenderer(List<Card> cards,
                                int cardHeight,
                                int cardWidth,
                                int maxRotationDegrees,
                                int minStep,
                                int maxHandWidth) {

        this.cards = new ArrayList<>(cards);
        this.cardHeight = cardHeight;
        this.cardWidth = cardWidth;
        this.maxRotationDegrees = maxRotationDegrees;

        this.minStep = Math.max(1, Math.min(minStep, cardWidth));
        this.maxHandWidth = Math.max(rendererWidth(), maxHandWidth);

        setLayout(null);
        setOpaque(false);

        refreshCards();
    }

    public PlayersCardsRenderer(List<Card> cards) {
        this(cards, 300, 200, 22, 40, 1100);
    }

    public PlayersCardsRenderer() {
        this(new ArrayList<>());
    }

    private int rendererWidth() {
        return cardWidth + 220;
    }

    private int rendererHeight() {
        return cardHeight + 260;
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    @Override
    public Dimension getPreferredSize() {

        int n = cards.size();

        int width = rendererWidth();

        if (n > 1) {
            int naturalWidth = rendererWidth() + (n - 1) * cardWidth;
            int minimumWidth = rendererWidth() + (n - 1) * minStep;

            width = Math.max(minimumWidth, Math.min(naturalWidth, maxHandWidth));
        }

        return new Dimension(
                width,
                rendererHeight() + ARC_DEPTH
        );
    }

    public void setCards(List<Card> cards) {
        this.cards = new ArrayList<>(cards);
        refreshCards();
    }

    public void addCard(Card card) {
        cards.add(card);
        refreshCards();
    }

    public void add(Card card) {
        addCard(card);
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    public void setMinStep(int minStep) {
        this.minStep = Math.max(1, Math.min(minStep, cardWidth));
        revalidate();
        doLayout();
        repaint();
    }

    public void setMaxHandWidth(int maxHandWidth) {
        this.maxHandWidth = Math.max(rendererWidth(), maxHandWidth);
        revalidate();
        doLayout();
        repaint();
    }

    public int getMinStep() {
        return minStep;
    }

    public int getMaxHandWidth() {
        return maxHandWidth;
    }

    private int computeRotation(int index, int totalCards) {

        if (totalCards <= 1) {
            return 0;
        }

        double center = (totalCards - 1) / 2.0;
        double normalized = (index - center) / center;

        return (int) Math.round(normalized * maxRotationDegrees);
    }

    private int computeVerticalOffset(int index, int totalCards) {

        if (totalCards <= 1) {return 0;}

        double center = (totalCards - 1) / 2.0;
        double normalized = (index - center) / center;

        return (int) Math.round(ARC_DEPTH * normalized * normalized);
    }

    private void refreshCards() {

        removeAll();
        renderers.clear();

        int n = cards.size();

        for (int i = 0; i < n; i++) {

            Card card = cards.get(i);
            int rotation = computeRotation(i, n);

            try {
                CardRenderer renderer = new CardRenderer(
                        card,
                        cardHeight,
                        cardWidth,
                        rotation
                ) {
                    @Override
                    public boolean contains(int x, int y) {

                        double angle = Math.toRadians(rotation);

                        double cx = 100 + cardWidth / 2.0;
                        double cy = 150 + cardHeight / 2.0;

                        double dx = x - cx;
                        double dy = y - cy;

                        double originalX = cx + dx * Math.cos(angle) + dy * Math.sin(angle);

                        double originalY = cy - dx * Math.sin(angle) + dy * Math.cos(angle);

                        Shape shape = new RoundRectangle2D.Float(100, 150, cardWidth, cardHeight, 25, 25);

                        return shape.contains(originalX, originalY);
                    }
                };

                renderer.setOpaque(false);

                renderers.add(renderer);
                add(renderer);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        updateStackingOrder();

        revalidate();
        doLayout();
        repaint();
    }

    private void updateStackingOrder() {

        int n = renderers.size();

        double center = (n - 1) / 2.0;

        List<Integer> order = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            order.add(i);
        }

        order.sort(Comparator.comparingDouble(i -> Math.abs(i - center)));

        // Center cards appear in front of outer cards.
        for (int i = order.size() - 1; i >= 0; i--) {
            setComponentZOrder(renderers.get(order.get(i)), 0);
        }
    }

    @Override
    public void doLayout() {

        int n = renderers.size();

        if (n == 0) {
            return;
        }

        int step = 0;

        if (n > 1) {

            int naturalWidth = rendererWidth() + (n - 1) * cardWidth;

            int availableWidth = Math.min(Math.min(getWidth(), maxHandWidth), naturalWidth);

            step = (availableWidth - rendererWidth()) / (n - 1);

            step = Math.max(minStep, step);
            step = Math.min(cardWidth, step);
        }

        int handWidth = rendererWidth() + (n - 1) * step;

        int startX = (getWidth() - handWidth) / 2;

        for (int i = 0; i < n; i++) {

            int x = startX + i * step;
            int y = computeVerticalOffset(i, n);

            renderers.get(i).setBounds(x, y, rendererWidth(), rendererHeight());
        }
    }
}