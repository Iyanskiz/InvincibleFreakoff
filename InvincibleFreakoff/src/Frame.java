import javax.swing.*;
import java.awt.*;

public class Frame extends JFrame {

    public Frame() {
        setTitle("Invincible Showdowns");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Remove window decorations for true full-screen feel
        // Use undecorated only if you want no title bar — keep decorated for now
        // but fill the entire usable screen area
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle usable = ge.getMaximumWindowBounds(); // excludes taskbar

        // Make the content panel exactly the usable size
        int W = usable.width;
        int H = usable.height;

        MainMenu menu = new MainMenu(this);
        menu.setPreferredSize(new Dimension(W, H));
        setContentPane(menu);

        pack();                                      // sizes frame around content
        setLocation(usable.x, usable.y);             // top-left of usable area
        setResizable(false);
        setVisible(true);
        menu.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Frame());
    }
}