import java.awt.*;

public class Conquest extends Fighter {

    private int specialCooldown = 0;
    private boolean berserkMode = false;
    private int berserkTimer = 0;

    public Conquest(int x, boolean isPlayer1) {
        super("Conquest", 170, x, isPlayer1);
        characterColor = new Color(60, 60, 80);
        accentColor    = new Color(200, 50, 50);
        attackDamage   = 15;
        animSpeed      = 6;
        width  = 130;
        height = 160;

        // Load sprites — filenames must match exactly what's in your imgs/ folder
        imgIdle        = loadImage("ConquestLevitating.png");  // closest idle-like pose
        imgForward     = loadImage("ConquestForward.png");
        imgBackward    = loadImage("ConquestBackward.png");
        imgPunch       = loadImage("ConquestPunch.png");
        imgBlock       = loadImage("ConquestBlock.png");
        imgHit         = loadImage("ConquestHit.png");
        imgLevitating  = loadImage("ConquestLevitating.png");
    }

    @Override
    public void update(Fighter opponent) {
        if (specialCooldown > 0) specialCooldown--;
        if (berserkMode) {
            berserkTimer--;
            if (berserkTimer <= 0) berserkMode = false;
        }
        super.update(opponent);
    }

    @Override
    public void specialMove() {
        if (specialCooldown == 0 && !isAttacking) {
            berserkMode     = true;
            berserkTimer    = 160;
            specialCooldown = 210;
            isAttacking     = true;
            attackTimer     = 38;
            attackDamage    = 38;
            currentState    = State.SPECIAL;
            velX = facingRight ? 12 : -12;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        // Berserk aura behind sprite
        if (berserkMode) {
            g.setColor(new Color(200, 0, 0, 45));
            g.fillOval(x - 15, y - 15, width + 30, height + 30);
        }
        super.draw(g);

        // Special cooldown indicator
        if (specialCooldown > 0) {
            g.setColor(new Color(200, 50, 50, 140));
            float pct = 1f - (specialCooldown / 210f);
            g.fillArc(x + width / 2 - 15, y - 22, 30, 14, 90, (int)(360 * pct));
        }
    }
}