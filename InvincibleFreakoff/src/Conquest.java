import java.awt.*;

public class Conquest extends Fighter {

    private boolean blastActive = false;
    private int blastTimer = 0;

    public Conquest(int x, boolean isPlayer1) {
        super("Conquest", 310, x, isPlayer1);
        characterColor   = new Color(60, 60, 80);
        accentColor      = new Color(200, 50, 50);
        attackDamage     = 15;
        animSpeed        = 6;
        width            = 120;
        height           = 155;
        specialThreshold = 100;

        imgIdle       = loadImage("ConquestIdle.png");
        imgForward    = loadImage("ConquestForward.png");
        imgBackward   = loadImage("ConquestBackward.png");
        imgPunch      = loadImage("ConquestPunch.png");
        imgBlock      = loadImage("ConquestBlock.png");
        imgHit        = loadImage("ConquestHit.png");
        imgLevitating = loadImage("ConquestLevitating.png");
    }

    @Override
    public void update(Fighter nearest, java.util.List<Platform> platforms) {
        if (blastActive) { blastTimer--; if (blastTimer <= 0) blastActive = false; }
        super.update(nearest, platforms);
    }

    @Override
    public void specialMove() {
        if (specialReady() && !isAttacking) {
            blastActive   = true;
            blastTimer    = 30;
            consumeSpecial();
            isAttacking   = true;
            attackTimer   = 30;
            attackDamage  = 35;
            currentState  = State.SPECIAL;
            velX = facingRight ? -3 : 3;
        }
    }

    public boolean isBlasting()    { return blastActive; }
    public boolean isFacingRight() { return facingRight; }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        if (blastActive) {
            float alpha = blastTimer / 30f;
            int bx = facingRight ? x + width : x - 200;
            g.setColor(new Color(255, 80, 50, (int)(alpha * 80)));
            g.fillRoundRect(bx, y + height / 3 - 15, 200, 30, 15, 15);
            g.setColor(new Color(255, 200, 150, (int)(alpha * 200)));
            g.fillRoundRect(bx + 10, y + height / 3 - 8, 180, 16, 8, 8);
            g.setColor(new Color(255, 255, 220, (int)(alpha * 255)));
            g.fillRoundRect(bx + 20, y + height / 3 - 4, 160, 8, 4, 4);
            g.setFont(new Font("Impact", Font.PLAIN, 13));
            g.setColor(new Color(255, 120, 80, 220));
            g.drawString("ENERGY BLAST!", x + width / 2 - 45, y - 38);
        }
    }
}