package model;

import java.awt.*;
import java.io.Serializable;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

public sealed abstract class Card implements Serializable permits Room, Actor, Weapon{

    private static final long serialVersionUID = 1L;

    private Path aspectToRender;
    private Path circleImageToRender;
    private String nameToRender;

    // Images are cached by AllPaths; do not serialize the pixel data.
    private transient BufferedImage mainBackgroundImage;
    private transient BufferedImage frontCircleImage;

    private Color CardBackGroundColor = Color.BLACK;
    private Color CardFrontCircleColor = Color.RED;

    public Card(Path aspectToRender, String nameToRender, Color cardBackGroundColor, Color cardFrontCircleColor) {
        this.aspectToRender = aspectToRender;
        this.nameToRender = nameToRender;
        CardBackGroundColor = cardBackGroundColor;
        CardFrontCircleColor = cardFrontCircleColor;
    }

    public Card(Path aspectToRender, String nameToRender, Path circleImageToRender,
                Color cardBackGroundColor, Color cardFrontCircleColor) {
        this(aspectToRender, nameToRender, cardBackGroundColor, cardFrontCircleColor);
        this.circleImageToRender = circleImageToRender;
    }

    public Card(Path aspectToRender, String nameToRender) {
        this.aspectToRender = aspectToRender;
        this.nameToRender = nameToRender;
    }

    public Card(Path aspectToRender, String nameToRender, Path circleImageToRender) {
        this(aspectToRender, nameToRender);
        this.circleImageToRender = circleImageToRender;
    }

    public Card(){}

    /** Return the already-loaded main image, when assigned. */
    public BufferedImage getMainBackgroundImage() {
        return mainBackgroundImage;
    }

    public void setMainBackgroundImage(BufferedImage mainBackgroundImage) {
        this.mainBackgroundImage = mainBackgroundImage;
    }

    /** Return the already-loaded circular badge image, when assigned. */
    public BufferedImage getFrontCircleImage() {
        return frontCircleImage;
    }

    public void setFrontCircleImage(BufferedImage frontCircleImage) {
        this.frontCircleImage = frontCircleImage;
    }

    public void setRenderImages(BufferedImage mainBackgroundImage, BufferedImage frontCircleImage) {
        this.mainBackgroundImage = mainBackgroundImage;
        this.frontCircleImage = frontCircleImage;
    }

    public Color getCardBackGroundColor() {
        return CardBackGroundColor;
    }

    public void setCardBackGroundColor(Color cardBackGroundColor) {
        CardBackGroundColor = cardBackGroundColor;
    }

    public Color getCardFrontCircleColor() {
        return CardFrontCircleColor;
    }

    public void setCardFrontCircleColor(Color cardFrontCircleColor) {
        CardFrontCircleColor = cardFrontCircleColor;
    }

    /**get the main aspect of the image whitch shows as the image in the center*/
    public Path getAspectToRender() {
        return aspectToRender;
    }

    /**set the main aspect of the image whitch shows as the image in the center*/
    public void setAspectToRender(Path aspectToRender) {
        this.aspectToRender = aspectToRender;
    }

    /**
     * get the aspect of the image inside the circle at the lower part of the card
     */
    public Path getCircleImageToRender() {
        return circleImageToRender;
    }

    /**
     *set the aspect of the image inside the circle at the lower part of the card
     */
    public void setCircleImageToRender(Path circleImageToRender) {
        this.circleImageToRender = circleImageToRender;
    }

    /**
     *returns the unique name of the card<br>
     * Warning: this String should not be confused with the name of the Card Type (which would be either Room, Actor or Weapon)
     */
    public String getNameToRender() {
        return nameToRender;
    }

    /**
     *sets the unique name of the card<br>
     * Warning : this String should not be confused with the name of the Card Type (which would be either Room, Actor or Weapon)
     */
    public void setNameToRender(String nameToRender) {
        this.nameToRender = nameToRender;
    }

    /**
     * gets the name of the class (therefore the name of the type of Card) of this object (whitch is a class)
     */
    public String getCardType(){
        return getClass().getSimpleName();
    }
}
