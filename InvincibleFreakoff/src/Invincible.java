import java.awt.*;
import java.awt.image.BufferedImage;

public class Invincible extends Fighter {

    private int specialCooldown = 0;
    private boolean flyingThrough = false;
    private int flyTimer = 0;

    public Invincible(int x, boolean isPlayer1) {
        super("Invincible", 180, x, isPlayer1);
        characterColor = new Color(30, 90, 255);
        accentColor    = new Color(255, 220, 0);
        attackDamage   = 10;
        animSpeed      = 5;
        width  = 130;
        height = 160;

        imgIdle       = loadImage("InvincibleIdle.png");
        imgForward    = loadImage("InvincibleForward.png");
        imgBackward   = loadImage("InvincibleBackward.png");
        imgPunch      = loadImage("InvinciblePunch.png");
        imgBlock      = loadImage("InvincibleBlock.png");
        imgHit        = loadImage("InvincibleIdle.png");
        imgLevitating = loadImage("InvincibleLevitating.png");
    }

    @Override
    public void update(Fighter opponent) {
        if (specialCooldown > 0) specialCooldown--;
        if (flyingThrough) {
            flyTimer--;
            if (flyTimer <= 0) flyingThrough = false;
        }
        super.update(opponent);
    }

    @Override
    public void specialMove() {
        // LORE: Invincible's signature — the AERIAL SLAM
        // He rockets straight up then dive-bombs the opponent
        // like his classic move against Conquest and Omni-Man
        if (specialCooldown == 0 && !isAttacking) {
            flyingThrough   = true;
            flyTimer        = 40;
            specialCooldown = 160;
            isAttacking     = true;
            attackTimer     = 40;
            attackDamage    = 28;
            currentState    = State.SPECIAL;
            // Rocket upward then forward
            velY = -20;
            velX = facingRight ? 12 : -12;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        // Blue speed trail when doing special
        if (flyingThrough) {
            g.setColor(new Color(50, 120, 255, 45));
            g.fillOval(x - 15, y - 15, width + 30, height + 30);
            g.setColor(new Color(255, 220, 50, 22));
            g.fillOval(x - 28, y - 28, width + 56, height + 56);
        }
        super.draw(g);
        if (specialCooldown > 0) {
            g.setColor(new Color(50, 120, 255, 135));
            float pct = 1f - (specialCooldown / 160f);
            g.fillArc(x + width/2 - 15, y - 22, 30, 14, 90, (int)(360 * pct));
        }
        if (flyingThrough) {
            g.setFont(new Font("Impact", Font.PLAIN, 13));
            g.setColor(new Color(100, 180, 255, 215));
            g.drawString("AERIAL SLAM!", x + width/2 - 42, y - 26);
        }
    }
}