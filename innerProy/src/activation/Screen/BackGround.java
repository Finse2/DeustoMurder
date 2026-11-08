package activation.Screen;

import javax.swing.*;
import java.awt.*;
/**
 * The BackGround class is a custom JPanel that displays a background image.
 * It extends JPanel and overrides the paintComponent method to draw the specified image as the background.
 * The image is scaled to fit the size of the panel.
 */
public class
BackGround extends JPanel {

    private Image backgroundImage;
/**
     * Constructor for the BackGround class.
     *
     * @param imagePath The path to the image file to be used as the background.
     */
    public BackGround(String imagePath) {
        backgroundImage = new ImageIcon(imagePath).getImage();
        setLayout(new BorderLayout());
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(backgroundImage,0,0,getWidth(), getHeight(),this );
    }

}
