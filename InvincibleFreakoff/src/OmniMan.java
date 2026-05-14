import java.awt.*;

public class OmniMan extends Fighter {

    private boolean chargeActive = false;
    private int chargeTimer = 0;

    public OmniMan(int x, boolean isPlayer1) {
        super("Omni-Man", 300, x, isPlayer1);
        characterColor   = new Color(180, 0, 0);
        accentColor      = new Color(240, 240, 240);
        attackDamage     = 14;
        animSpeed        = 6;
        width            = 124;   // wider than everyone except Thragg (128)
        height           = 160;   // taller than everyone except Thragg (162)
        specialThreshold = 120;

        imgIdle       = loadImage("OmniLevitating.png");
        imgForward    = loadImage("OmniForward.png");
        imgBackward   = loadImage("OmniBackward.png");
        imgPunch      = loadImage("OmniPunch.png");
        imgBlock      = loadImage("OmniBlock.png");
        imgHit        = loadImage("OmniHit.png");
        imgLevitating = loadImage("OmniLevitating.png");
    }

    @Override
    public void update(Fighter nearest, java.util.List<Platform> platforms) {
        if (chargeActive) { chargeTimer--; if (chargeTimer <= 0) chargeActive = false; }
        super.update(nearest, platforms);
    }

    @Override
    public void specialMove() {
        if (specialReady() && !isAttacking) {
            chargeActive  = true;
            chargeTimer   = 40;
            consumeSpecial();
            isAttacking   = true;
            attackTimer   = 40;
            attackDamage  = 45;
            currentState  = State.SPECIAL;
            velX = facingRight ? 22 : -22;
            velY = 0;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (chargeActive) {
            g.setColor(new Color(255, 60, 0, 40));
            g.fillOval(x - 25, y - 10, width + 50, height + 20);
            g.setColor(new Color(255, 200, 50, 20));
            g.fillOval(x - 40, y - 5, width + 80, height + 10);
        }
        super.draw(g);
        if (chargeActive) {
            g.setFont(new Font("Impact", Font.PLAIN, 13));
            g.setColor(new Color(255, 120, 0, 220));
            g.drawString("VILTRUMITE!", x + width / 2 - 35, y - 38);
        }
    }
}