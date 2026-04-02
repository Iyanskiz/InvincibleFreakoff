import javax.swing.*;
import java.awt.*;

public class Frame extends JFrame {

    public Frame() {
        setTitle("Invincible Freakoff");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Start with character select screen
        CharacterSelect charSelect = new CharacterSelect(this);
        setContentPane(charSelect);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        charSelect.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Frame());
    }
}