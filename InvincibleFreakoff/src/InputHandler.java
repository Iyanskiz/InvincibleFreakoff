import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public class InputHandler implements KeyListener {

    private Set<Integer> pressedKeys = new HashSet<>();

    // ── Player 1 keys (WASD + F/G/H) ──────────────────
    public static final int P1_LEFT    = KeyEvent.VK_A;
    public static final int P1_RIGHT   = KeyEvent.VK_D;
    public static final int P1_JUMP    = KeyEvent.VK_W;
    public static final int P1_BLOCK   = KeyEvent.VK_S;
    public static final int P1_LIGHT   = KeyEvent.VK_F;
    public static final int P1_HEAVY   = KeyEvent.VK_G;
    public static final int P1_SPECIAL = KeyEvent.VK_H;

    // ── Player 2 keys (Arrow keys + numpad 1/2/3) ─────
    public static final int P2_LEFT    = KeyEvent.VK_LEFT;
    public static final int P2_RIGHT   = KeyEvent.VK_RIGHT;
    public static final int P2_JUMP    = KeyEvent.VK_UP;
    public static final int P2_BLOCK   = KeyEvent.VK_DOWN;
    public static final int P2_LIGHT   = KeyEvent.VK_NUMPAD1;
    public static final int P2_HEAVY   = KeyEvent.VK_NUMPAD2;
    public static final int P2_SPECIAL = KeyEvent.VK_NUMPAD3;

    // Pause
    public static final int PAUSE = KeyEvent.VK_ESCAPE;

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public boolean isPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public void clear() {
        pressedKeys.clear();
    }

    // ── Convenience helpers ────────────────────────────

    // Player 1
    public boolean p1Left()    { return isPressed(P1_LEFT); }
    public boolean p1Right()   { return isPressed(P1_RIGHT); }
    public boolean p1Jump()    { return isPressed(P1_JUMP); }
    public boolean p1Block()   { return isPressed(P1_BLOCK); }
    public boolean p1Light()   { return isPressed(P1_LIGHT); }
    public boolean p1Heavy()   { return isPressed(P1_HEAVY); }
    public boolean p1Special() { return isPressed(P1_SPECIAL); }

    // Player 2
    public boolean p2Left()    { return isPressed(P2_LEFT); }
    public boolean p2Right()   { return isPressed(P2_RIGHT); }
    public boolean p2Jump()    { return isPressed(P2_JUMP); }
    public boolean p2Block()   { return isPressed(P2_BLOCK); }
    public boolean p2Light()   { return isPressed(P2_LIGHT); }
    public boolean p2Heavy()   { return isPressed(P2_HEAVY); }
    public boolean p2Special() { return isPressed(P2_SPECIAL); }

    public boolean pausePressed() { return isPressed(PAUSE); }
}