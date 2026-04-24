import java.awt.*;

public class Conquest extends Fighter {

    private int specialCooldown = 0;
    private boolean blastActive = false;
    private int blastTimer = 0;
    private int blastX = 0, blastY = 0;

    public Conquest(int x, boolean isPlayer1) {
        super("Conquest", 170, x, isPlayer1);
        characterColor = new Color(60, 60, 80);
        accentColor    = new Color(200, 50, 50);
        attackDamage   = 15;
        animSpeed      = 6;
        width  = 140;
        height = 170;

        imgIdle       = loadImage("ConquestIdle.png");
        imgForward    = loadImage("ConquestForward.png");
        imgBackward   = loadImage("ConquestBackward.png");
        imgPunch      = loadImage("ConquestPunch.png");
        imgBlock      = loadImage("ConquestBlock.png");
        imgHit        = loadImage("ConquestHit.png");
        imgLevitating = loadImage("ConquestLevitating.png");
    }

    @Override
    public void update(Fighter opponent) {
        if (specialCooldown > 0) specialCooldown--;
        if (blastActive) {
            blastTimer--;
            if (blastTimer <= 0) blastActive = false;
        }
        super.update(opponent);
    }

    @Override
    public void specialMove() {
        // LORE: Conquest's signature is his ENERGY BLASTS —
        // Unlike other Viltrumites, Conquest can channel energy into
        // devastating concussive blasts from his hands, shown when he
        // nearly killed Invincible with point-blank energy attacks.
        // Medium range energy projectile with knockback.
        if (specialCooldown == 0 && !isAttacking) {
            blastActive     = true;
            blastTimer      = 30;
            specialCooldown = 190;
            isAttacking     = true;
            attackTimer     = 30;
            attackDamage    = 35;
            currentState    = State.SPECIAL;
            // Store blast origin
            blastX = facingRight ? x + width + 20 : x - 80;
            blastY = y + height / 3;
            // Slight push back from recoil
            velX = facingRight ? -3 : 3;
        }
    }

    public boolean isBlasting() { return blastActive; }
    public int getBlastX()      { return blastX; }
    public int getBlastY()      { return blastY; }
    public boolean isFacingRight() { return facingRight; }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);

        // Draw energy blast beam
        if (blastActive) {
            float alpha = blastTimer / 30f;
            // Outer glow
            g.setColor(new Color(255, 80, 50, (int)(alpha * 80)));
            int bx = facingRight ? x + width : x - 200;
            g.fillRoundRect(bx, y + height/3 - 15, 200, 30, 15, 15);
            // Core beam
            g.setColor(new Color(255, 200, 150, (int)(alpha * 200)));
            g.fillRoundRect(bx + 10, y + height/3 - 8, 180, 16, 8, 8);
            // Hot center
            g.setColor(new Color(255, 255, 220, (int)(alpha * 255)));
            g.fillRoundRect(bx + 20, y + height/3 - 4, 160, 8, 4, 4);
        }

        if (specialCooldown > 0) {
            g.setColor(new Color(200, 50, 50, 140));
            float pct = 1f - (specialCooldown / 190f);
            g.fillArc(x + width/2 - 15, y - 22, 30, 14, 90, (int)(360 * pct));
        }
        if (blastActive) {
            g.setFont(new Font("Impact", Font.PLAIN, 13));
            g.setColor(new Color(255, 120, 80, 220));
            g.drawString("ENERGY BLAST!", x + width/2 - 45, y - 26);
        }
    }
}