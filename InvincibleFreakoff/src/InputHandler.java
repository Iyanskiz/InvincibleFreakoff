import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public class InputHandler implements KeyListener {

    private Set<Integer> pressedKeys = new HashSet<>();

    // ── Player 1 (WASD + F/G/H) ───────────────────────────────────────────────
    public static final int P1_LEFT    = KeyEvent.VK_A;
    public static final int P1_RIGHT   = KeyEvent.VK_D;
    public static final int P1_JUMP    = KeyEvent.VK_W;
    public static final int P1_BLOCK   = KeyEvent.VK_S;
    public static final int P1_LIGHT   = KeyEvent.VK_F;
    public static final int P1_HEAVY   = KeyEvent.VK_G;
    public static final int P1_SPECIAL = KeyEvent.VK_H;

    // ── Player 2 (Arrow keys + Numpad 1/2/3) ──────────────────────────────────
    public static final int P2_LEFT    = KeyEvent.VK_LEFT;
    public static final int P2_RIGHT   = KeyEvent.VK_RIGHT;
    public static final int P2_JUMP    = KeyEvent.VK_UP;
    public static final int P2_BLOCK   = KeyEvent.VK_DOWN;
    public static final int P2_LIGHT   = KeyEvent.VK_NUMPAD1;
    public static final int P2_HEAVY   = KeyEvent.VK_NUMPAD2;
    public static final int P2_SPECIAL = KeyEvent.VK_NUMPAD3;

    // ── Player 3 (IJKL + U/O/P) ───────────────────────────────────────────────
    public static final int P3_LEFT    = KeyEvent.VK_J;
    public static final int P3_RIGHT   = KeyEvent.VK_L;
    public static final int P3_JUMP    = KeyEvent.VK_I;
    public static final int P3_BLOCK   = KeyEvent.VK_K;
    public static final int P3_LIGHT   = KeyEvent.VK_U;
    public static final int P3_HEAVY   = KeyEvent.VK_O;
    public static final int P3_SPECIAL = KeyEvent.VK_P;

    // ── Player 4 (Numpad 4568 + Numpad 7/9/0) ─────────────────────────────────
    public static final int P4_LEFT    = KeyEvent.VK_NUMPAD4;
    public static final int P4_RIGHT   = KeyEvent.VK_NUMPAD6;
    public static final int P4_JUMP    = KeyEvent.VK_NUMPAD8;
    public static final int P4_BLOCK   = KeyEvent.VK_NUMPAD5;
    public static final int P4_LIGHT   = KeyEvent.VK_NUMPAD7;
    public static final int P4_HEAVY   = KeyEvent.VK_NUMPAD9;
    public static final int P4_SPECIAL = KeyEvent.VK_NUMPAD0;

    public static final int PAUSE = KeyEvent.VK_ESCAPE;

    @Override public void keyPressed(KeyEvent e)  { pressedKeys.add(e.getKeyCode()); }
    @Override public void keyReleased(KeyEvent e) { pressedKeys.remove(e.getKeyCode()); }
    @Override public void keyTyped(KeyEvent e)    {}

    public boolean isPressed(int keyCode) { return pressedKeys.contains(keyCode); }
    public void clear() { pressedKeys.clear(); }

    // P1
    public boolean p1Left()    { return isPressed(P1_LEFT);    }
    public boolean p1Right()   { return isPressed(P1_RIGHT);   }
    public boolean p1Jump()    { return isPressed(P1_JUMP);    }
    public boolean p1Block()   { return isPressed(P1_BLOCK);   }
    public boolean p1Light()   { return isPressed(P1_LIGHT);   }
    public boolean p1Heavy()   { return isPressed(P1_HEAVY);   }
    public boolean p1Special() { return isPressed(P1_SPECIAL); }

    // P2
    public boolean p2Left()    { return isPressed(P2_LEFT);    }
    public boolean p2Right()   { return isPressed(P2_RIGHT);   }
    public boolean p2Jump()    { return isPressed(P2_JUMP);    }
    public boolean p2Block()   { return isPressed(P2_BLOCK);   }
    public boolean p2Light()   { return isPressed(P2_LIGHT);   }
    public boolean p2Heavy()   { return isPressed(P2_HEAVY);   }
    public boolean p2Special() { return isPressed(P2_SPECIAL); }

    // P3
    public boolean p3Left()    { return isPressed(P3_LEFT);    }
    public boolean p3Right()   { return isPressed(P3_RIGHT);   }
    public boolean p3Jump()    { return isPressed(P3_JUMP);    }
    public boolean p3Block()   { return isPressed(P3_BLOCK);   }
    public boolean p3Light()   { return isPressed(P3_LIGHT);   }
    public boolean p3Heavy()   { return isPressed(P3_HEAVY);   }
    public boolean p3Special() { return isPressed(P3_SPECIAL); }

    // P4
    public boolean p4Left()    { return isPressed(P4_LEFT);    }
    public boolean p4Right()   { return isPressed(P4_RIGHT);   }
    public boolean p4Jump()    { return isPressed(P4_JUMP);    }
    public boolean p4Block()   { return isPressed(P4_BLOCK);   }
    public boolean p4Light()   { return isPressed(P4_LIGHT);   }
    public boolean p4Heavy()   { return isPressed(P4_HEAVY);   }
    public boolean p4Special() { return isPressed(P4_SPECIAL); }

    public boolean pausePressed() { return isPressed(PAUSE); }
}