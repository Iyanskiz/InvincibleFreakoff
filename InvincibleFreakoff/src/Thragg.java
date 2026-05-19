import java.awt.*;

public class Thragg extends Fighter {

    private boolean graxActive = false;
    private int graxTimer = 0;

    public Thragg(int x, boolean isPlayer1) {
        super("Thragg", 320, x, isPlayer1);
        characterColor   = new Color(100, 0, 0);
        accentColor      = new Color(180, 140, 0);
        attackDamage     = 16;
        animSpeed        = 7;
        width            = 128;  // slightly bigger — he IS the Grand Regent
        height           = 162;
        specialThreshold = 150; // longest to charge — most devastating payoff

        imgIdle       = loadImage("ThraggIdle.png");
        imgForward    = loadImage("ThraggForward.png");
        imgBackward   = loadImage("ThraggBackward.png");
        imgPunch      = loadImage("ThraggPunch.png");
        imgBlock      = loadImage("ThraggBlock.png");
        imgHit        = loadImage("ThraggHit.png");
        imgLevitating = loadImage("ThaggLevitating.png");
    }

    @Override
    public void update(Fighter nearest, java.util.List<Platform> platforms) {
        if (graxActive) { graxTimer--; if (graxTimer <= 0) graxActive = false; }
        super.update(nearest, platforms);
    }

    @Override
    public void takeDamage(int damage) {
        if (graxActive) damage = damage / 4;
        super.takeDamage(damage);
    }

    @Override
    public void specialMove() {
        if (specialReady() && !isAttacking) {
            graxActive    = true;
            graxTimer     = 120;
            consumeSpecial();
            isAttacking   = true;
            attackTimer   = 50;
            attackDamage  = 55;
            currentState  = State.SPECIAL;
            velX = facingRight ? 12 : -12;
            velY = -3;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (graxActive) {
            g.setColor(new Color(180, 0, 0, 60));
            g.fillOval(x - 18, y - 18, width + 36, height + 36);
            g.setColor(new Color(255, 0, 0, 20));
            g.fillOval(x - 35, y - 35, width + 70, height + 70);
        }
        super.draw(g);
        if (graxActive) {
            g.setFont(new Font("Impact", Font.PLAIN, 14));
            g.setColor(new Color(255, 0, 0, 220));
            g.drawString("THE GRAX!", x + width / 2 - 35, y - 38);
        }
    }
}