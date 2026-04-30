import javax.swing.*;
import java.awt.*;

public class Frame extends JFrame {

    public static final int WIDTH  = 1280;
    public static final int HEIGHT = 720;

    public Frame() {
        setTitle("Invincible Showdown");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        MainMenu menu = new MainMenu(this);
        setContentPane(menu);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        menu.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Frame());
    }
}