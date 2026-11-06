
package ui.match;

import model.Card;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayersCardsRenderer extends JPanel {

    public record PlayersHandsRendererHeader(Class<? extends Card> cabecera, int maxRotationDegrees) {}

    private static final int GAP = 10;

    private final List<CardGroupRenderer> groups = new ArrayList<>();
    private final List<Card> cards = new ArrayList<>();
    private final PlayersHandsRendererHeader[] headers;

    private final int cardHeight;
    private final int cardWidth;
    private int minStep;
    private int maxHandWidth;

    public PlayersCardsRenderer(PlayersHandsRendererHeader[] headers, Object[] cards) {
        this(headers, cards, 165, 85, 40, 1450);
    }

    public PlayersCardsRenderer(PlayersHandsRendererHeader[] headers, Object[] cards,
                                int cardHeight, int cardWidth, int minStep, int maxHandWidth) {

        this.headers = headers.clone();
        this.cardHeight = cardHeight;
        this.cardWidth = cardWidth;
        this.minStep = minStep;
        this.maxHandWidth = maxHandWidth;

        setLayout(null);
        setOpaque(false);

        // Los subtipos deben aparecer antes que sus supertipos.
        for (int i = 0; i < this.headers.length; i++) {
            for (int j = i + 1; j < this.headers.length; j++) {
                Class<? extends Card> a = this.headers[i].cabecera();
                Class<? extends Card> b = this.headers[j].cabecera();

                if (a.isAssignableFrom(b)) {
                    System.err.println("Conflicto de tipos: " + a.getSimpleName()
                            + " y " + b.getSimpleName());

                    PlayersHandsRendererHeader temp = this.headers[i];
                    this.headers[i] = this.headers[j];
                    this.headers[j] = temp;
                } else if (b.isAssignableFrom(a)) {
                    System.err.println("Conflicto de tipos: " + a.getSimpleName()
                            + " y " + b.getSimpleName());
                }
            }
        }

        for (PlayersHandsRendererHeader header : this.headers) {
            int maxRotation = header.maxRotationDegrees();

            CardGroupRenderer group = new CardGroupRenderer(
                    List.of(), cardHeight, cardWidth, minStep) {

                @Override
                protected int rotation(int index, int total) {
                    if (total <= 1) return 0;
                    double center = (total - 1) / 2.0;
                    return (int) Math.round((index - center) / center * maxRotation);
                }

                @Override
                protected int verticalOffset(int index, int total) {
                    if (total <= 1 || maxRotation == 0) return 0;
                    double center = (total - 1) / 2.0;
                    double distance = (index - center) / center;
                    return (int) Math.round(15 * distance * distance);
                }
            };

            groups.add(group);
            add(group);
        }

        setCards(cards);
    }

    public void setCards(Object[] input) {
        cards.clear();

        List<List<Card>> classified = new ArrayList<>();
        for (int i = 0; i < headers.length; i++) {
            classified.add(new ArrayList<>());
        }

        for (Object object : input) {
            if (!(object instanceof Card card)) continue;

            for (int i = 0; i < headers.length; i++) {
                if (headers[i].cabecera().isInstance(card)) {
                    classified.get(i).add(card);
                    cards.add(card);
                    break;
                }
            }
        }

        for (int i = 0; i < groups.size(); i++) {
            groups.get(i).setCards(classified.get(i));
        }

        revalidate();
        repaint();
    }

    public void addCard(Card card) {
        List<Card> updated = getCards();
        updated.add(card);
        setCards(updated.toArray());
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    public void setMinStep(int minStep) {
        this.minStep = minStep;
        for (CardGroupRenderer group : groups) {
            group.setMinStep(minStep);
        }
        revalidate();
        repaint();
    }

    public int getMinStep() {
        return minStep;
    }

    public void setMaxHandWidth(int maxHandWidth) {
        this.maxHandWidth = maxHandWidth;
        revalidate();
        repaint();
    }

    public int getMaxHandWidth() {
        return maxHandWidth;
    }

    @Override
    public Dimension getPreferredSize() {
        int width = 0;
        int active = 0;

        for (CardGroupRenderer group : groups) {
            if (!group.getCards().isEmpty()) {
                width += group.getNaturalWidth();
                active++;
            }
        }

        width += Math.max(0, active - 1) * GAP;
        return new Dimension(Math.min(width, maxHandWidth), cardHeight + 80);
    }

    @Override
    public void doLayout() {
        int active = 0;
        int minTotal = 0;
        int naturalTotal = 0;

        for (CardGroupRenderer group : groups) {
            if (!group.getCards().isEmpty()) {
                active++;
                minTotal += group.getMinimumWidth();
                naturalTotal += group.getNaturalWidth();
            }
        }

        if (active == 0) return;

        int gapTotal = Math.max(0, active - 1) * GAP;
        int available = Math.max(0, Math.min(getWidth(), maxHandWidth) - gapTotal);
        int target = Math.min(available, naturalTotal);
        int extra = Math.max(0, target - minTotal);
        int flexible = Math.max(1, naturalTotal - minTotal);
        int x = (getWidth() - target - gapTotal) / 2;
        int remaining = target;

        int last = -1;
        for (int i = 0; i < groups.size(); i++) {
            if (!groups.get(i).getCards().isEmpty()) last = i;
        }

        for (int i = 0; i < groups.size(); i++) {
            CardGroupRenderer group = groups.get(i);

            if (group.getCards().isEmpty()) {
                group.setBounds(0, 0, 0, 0);
                continue;
            }

            int width = target >= minTotal
                    ? group.getMinimumWidth() + extra *
                    (group.getNaturalWidth() - group.getMinimumWidth()) / flexible
                    : group.getMinimumWidth() * target / Math.max(1, minTotal);

            width = Math.min(remaining, width);
            if (i == last) width = remaining;

            group.setBounds(x, 0, width, getHeight());
            x += width + GAP;
            remaining -= width;
        }
    }
}
