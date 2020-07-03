import java.awt.Color;
import java.awt.Point;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

public class iLabel extends JLabel {

    public void setImage(String url) {
        ImageIcon i = new ImageIcon(url);
        setIcon(i);
        this.setSize(i.getIconWidth(), i.getIconHeight());
        LineBorder border = new LineBorder(Color.WHITE, 10, true);
        this.setBorder(border);

        Random r = new Random();
        this.speed = r.nextInt(3) + 1;

    }
    private int speed = 0;

    public void tick(int time) {
        if (time % speed == 0) {
            Point p = new Point();
            this.getLocation(p);
            this.setLocation(((int) p.getX()) - 1, ((int) p.getY()));

            System.out.println("tick...");
        }
    }

}
