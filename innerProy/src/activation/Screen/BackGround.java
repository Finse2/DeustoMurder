package activation.Screen;

import javax.swing.*;
import java.awt.*;

public class BackGround extends JPanel {

    private Image backgroundImage;

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
