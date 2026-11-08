
package ui.match.cardDisplayer;

import model.Card;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Contenedor abstracto que organiza varias cartas, calcula sus posiciones y aplica sus rotaciones.
 */
abstract class CardGroupRenderer extends JPanel {

    private static final int SIDE_PADDING = 25;
    private static final int TOP_PADDING = 30;
    private static final int BOTTOM_PADDING = 30;

    private final int cardHeight;
    private final int cardWidth;
    private int minStep;
    private final List<Card> cards = new ArrayList<>();
    private final List<CardRenderer> renderers = new ArrayList<>();

    protected CardGroupRenderer(int cardHeight, int cardWidth, int minStep) {
        this.cardHeight = cardHeight;
        this.cardWidth = cardWidth;
        this.minStep = Math.max(1, Math.min(minStep, cardWidth));
        setLayout(null);
        setOpaque(false);
    }

    protected abstract int rotation(int index, int total);

    protected int verticalOffset(int index, int total) {
        return 0;
    }

    protected Point cardPosition(int index, int total, int step) {
        return new Point(index * step, verticalOffset(index, total));
    }

    protected int getCardWidth() {
        return cardWidth;
    }

    protected int getCardHeight() {
        return cardHeight;
    }

    public void setCards(List<Card> cards) {
        this.cards.clear();
        this.cards.addAll(cards);
        removeAll();
        renderers.clear();

        for (int i = 0; i < cards.size(); i++) {
            int degrees = rotation(i, cards.size());
            try {
                CardRenderer renderer = new CardRenderer(cards.get(i), cardHeight, cardWidth, degrees) {
                    @Override
                    public boolean contains(int x, int y) {
                        double angle = Math.toRadians(degrees);
                        double cx = 100 + cardWidth / 2.0;
                        double cy = 150 + cardHeight / 2.0;
                        double dx = x - cx;
                        double dy = y - cy;
                        double rx = cx + dx * Math.cos(angle) + dy * Math.sin(angle);
                        double ry = cy - dx * Math.sin(angle) + dy * Math.cos(angle);
                        return new RoundRectangle2D.Double(100, 150, cardWidth, cardHeight, 25, 25).contains(rx, ry);
                    }
                };
                renderers.add(renderer);
                add(renderer);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot render card " + cards.get(i).getNameToRender(), e);
            }
        }

        for (int i = 0; i < renderers.size(); i++) setComponentZOrder(renderers.get(i), 0);
        revalidate();
        repaint();
    }

    public void setMinStep(int minStep) {
        this.minStep = Math.max(1, Math.min(minStep, cardWidth));
        revalidate();
        repaint();
    }

    public int getMinStep() {
        return minStep;
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    public int getNaturalWidth() {
        return cards.isEmpty() ? 0 : cards.size() * cardWidth + SIDE_PADDING * 2;
    }

    public int getMinimumWidth() {
        return cards.isEmpty() ? 0 : cardWidth + (cards.size() - 1) * minStep + SIDE_PADDING * 2;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getNaturalWidth(), cardHeight + TOP_PADDING + BOTTOM_PADDING + 20);
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    @Override
    public void doLayout() {
        int total = renderers.size();
        if (total == 0) return;

        int step = total == 1 ? 0 : Math.max(1, Math.min(cardWidth, (getWidth() - SIDE_PADDING * 2 - cardWidth) / (total - 1)));

        int visibleWidth = cardWidth + (total - 1) * step;
        int startX = (getWidth() - visibleWidth) / 2;

        for (int i = 0; i < total; i++) {
            Point position = cardPosition(i, total, step);

            int x = startX + position.x;
            int y = TOP_PADDING + position.y;

            renderers.get(i).setBounds(x - 100, y - 150, cardWidth + 200, cardHeight + 300);
        }
    }
}
