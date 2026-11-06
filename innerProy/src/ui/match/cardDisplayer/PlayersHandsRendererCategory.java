
package ui.match.cardDisplayer;

import model.Card;
import java.awt.Point;
import java.util.List;

public class PlayersHandsRendererCategory extends CardGroupRenderer {

    private final PlayersHandsRendererHeader header;

    public PlayersHandsRendererCategory(PlayersHandsRendererHeader header, List<Card> cards,
                                        int cardHeight, int cardWidth, int minStep) {
        super(cardHeight, cardWidth, minStep);
        this.header = header;
        setCards(cards);
    }

    private int getMaxRotation(int total) {
        if (!header.autoRotate()) return header.maxRotationDegrees();
        if (total <= 1) return 0;

        double overlap = getCardWidth() - getMinStep();
        double stepAngle = Math.toDegrees(
                Math.atan2(overlap, getCardHeight() * 2.5)
        );

        return (int) Math.round(Math.min(20, stepAngle * (total - 1) / 2.0));
    }

    @Override
    protected int rotation(int index, int total) {
        if (total <= 1) return 0;

        double center = (total - 1) / 2.0;
        return (int) Math.round(
                (index - center) / center * getMaxRotation(total)
        );
    }

    @Override
    protected Point cardPosition(int index, int total, int step) {
        int maxRotation = getMaxRotation(total);

        if (total <= 1 || maxRotation == 0)
            return super.cardPosition(index, total, step);

        double maxAngle = Math.toRadians(maxRotation);
        double angle = maxAngle * (2.0 * index / (total - 1) - 1.0);

        double radius = (total - 1) * step / (2.0 * Math.sin(maxAngle));

        int x = (int) Math.round(
                radius * (Math.sin(angle) + Math.sin(maxAngle))
        );

        int y = (int) Math.round(
                radius * (1.0 - Math.cos(angle))
        );

        return new Point(x, y);
    }

    @Override
    public int getNaturalWidth() {
        if (getMaxRotation(getCards().size()) == 0)
            return super.getNaturalWidth();

        return getMinimumWidth();
    }

    @Override
    public void setMinStep(int minStep) {
        super.setMinStep(minStep);
        setCards(getCards());
    }

    public PlayersHandsRendererHeader getHeader() {
        return header;
    }
}
