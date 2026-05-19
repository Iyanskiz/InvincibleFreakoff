import java.awt.*;
import java.awt.image.BufferedImage;

public class Invincible extends Fighter {

    private boolean flyingThrough = false;
    private int flyTimer = 0;

    public Invincible(int x, boolean isPlayer1) {
        super("Invincible", 180, x, isPlayer1);
        characterColor   = new Color(30, 90, 255);
        accentColor      = new Color(255, 220, 0);
        attackDamage     = 10;
        animSpeed        = 5;
        width            = 118;  // same frame as Anissa — younger/smaller hero
        height           = 155;
        specialThreshold = 80;  // fastest to charge — he's scrappy

        imgIdle       = loadImage("InvincibleIdle.png");
        imgForward    = loadImage("InvincibleForward.png");
        imgBackward   = loadImage("InvincibleBackward.png");
        imgPunch      = loadImage("InvinciblePunch.png");
        imgBlock      = loadImage("InvincibleBlock.png");
        imgHit        = loadImage("InvincibleHit.png");
        imgLevitating = loadImage("InvincibleLevitating.png");
    }

    @Override
    public void update(Fighter nearest, java.util.List<Platform> platforms) {
        if (flyingThrough) { flyTimer--; if (flyTimer <= 0) flyingThrough = false; }
        super.update(nearest, platforms);
    }

    @Override
    public void specialMove() {
        if (specialReady() && !isAttacking) {
            flyingThrough = true;
            flyTimer      = 40;
            consumeSpecial();
            isAttacking   = true;
            attackTimer   = 40;
            attackDamage  = 28;
            currentState  = State.SPECIAL;
            velY = -20;
            velX = facingRight ? 12 : -12;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (flyingThrough) {
            g.setColor(new Color(50, 120, 255, 45));
            g.fillOval(x - 15, y - 15, width + 30, height + 30);
            g.setColor(new Color(255, 220, 50, 22));
            g.fillOval(x - 28, y - 28, width + 56, height + 56);
        }
        super.draw(g);
        if (flyingThrough) {
            g.setFont(new Font("Impact", Font.PLAIN, 13));
            g.setColor(new Color(100, 180, 255, 215));
            g.drawString("AERIAL SLAM!", x + width / 2 - 42, y - 38);
        }
    }
}