import java.awt.*;
import java.awt.image.BufferedImage;

public class TechJacket extends Fighter {

    private boolean overdriveActive = false;
    private int overdriveTimer = 0;

    // Laser beam state — tracked entirely within TechJacket, no Fighter changes needed
    private boolean laserActive = false;
    private int laserTimer = 0;
    private boolean inHeavy = false;
    private static final int LASER_DURATION = 22;
    private static final int LASER_RANGE    = 2400; // full world width — true ranged

    private BufferedImage imgSuper;

    public TechJacket(int x, boolean isPlayer1) {
        super("Tech Jacket", 290, x, isPlayer1);
        characterColor   = new Color(30, 90, 200);
        accentColor      = new Color(0, 220, 255);
        attackDamage     = 14;
        animSpeed        = 6;
        width            = 118;
        height           = 158;
        specialThreshold = 130;

        imgIdle       = loadImage("TechIdle.png");
        imgForward    = loadImage("TechForward.png");
        imgBackward   = loadImage("TechBackward.png");
        imgPunch      = loadImage("TechPunch.png");
        imgBlock      = loadImage("TechBlock.png");
        imgHit        = loadImage("TechHit.png");
        imgLevitating = loadImage("TechLevitating.png");
        imgSuper      = loadImage("TechSuper.png");
    }

    @Override
    public void update(Fighter nearest, java.util.List<Platform> platforms) {
        if (overdriveActive) {
            overdriveTimer--;
            if (overdriveTimer <= 0) overdriveActive = false;
        }

        // Heavy laser — blue, full range
        boolean wasHeavy = inHeavy;
        inHeavy = isAttacking && heavyStartup <= 0 && attackTimer > 0 && currentState == State.ATTACK_HEAVY;
        if (inHeavy && !wasHeavy) {
            laserActive = true;
            laserTimer  = LASER_DURATION;
        }
        if (laserActive) {
            laserTimer--;
            if (laserTimer <= 0) laserActive = false;
        }

        super.update(nearest, platforms);

        // Set laser hitbox AFTER super.update (which clears attackHitbox via updateAttackHitboxes)
        if (laserActive) {
            int lx = facingRight ? x + width : x - LASER_RANGE;
            attackHitbox = new Rectangle(lx, y + height / 4, LASER_RANGE, height / 2);
        }
    }

    @Override
    public void takeDamage(int damage) {
        if (overdriveActive) damage = damage / 3;
        super.takeDamage(damage);
    }

    @Override
    public void specialMove() {
        if (specialReady() && !isAttacking) {
            // Heal 60 HP (capped at max)
            currentHealth = Math.min(maxHealth, currentHealth + 60);
            consumeSpecial();
            // Brief animation — glow but no movement or damage
            isAttacking  = true;
            attackTimer  = 40;
            attackDamage = 0;
            currentState = State.SPECIAL;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        // Overdrive aura
        if (overdriveActive) {
            g.setColor(new Color(0, 180, 255, 55));
            g.fillOval(x - 18, y - 18, width + 36, height + 36);
            g.setColor(new Color(0, 220, 255, 20));
            g.fillOval(x - 35, y - 35, width + 70, height + 70);
        }

        // Draw laser before sprite
        if (laserActive) drawLaser(g);

        // Swap to TechSuper.png during heavy attack
        if (inHeavy && imgSuper != null) {
            drawWithImage(g, imgSuper);
        } else {
            super.draw(g);
        }

        if (overdriveActive) {
            g.setFont(new Font("Impact", Font.PLAIN, 14));
            g.setColor(new Color(0, 220, 255, 220));
            g.drawString("OVERDRIVE!", x + width / 2 - 35, y - 38);
        }

        // Healing glow during special
        if (currentState == State.SPECIAL && isAttacking) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            g2.setColor(new Color(0, 255, 80));
            g2.fillOval(x - 20, y - 20, width + 40, height + 40);
            g2.dispose();
            g.setFont(new Font("Impact", Font.PLAIN, 16));
            g.setColor(new Color(0, 255, 100, 230));
            g.drawString("+60 HP", x + width / 2 - 22, y - 42);
        }
    }

    private void drawWithImage(Graphics2D g, BufferedImage img) {
        Graphics2D g2 = (Graphics2D) g.create();
        if (!facingRight) {
            g2.translate(x + width, y);
            g2.scale(-1, 1);
            g2.drawImage(img, 0, 0, width, height, null);
        } else {
            g2.drawImage(img, x, y, width, height, null);
        }
        g2.dispose();
    }

    private void drawLaser(Graphics2D g) {
        float progress = (float) laserTimer / LASER_DURATION;
        int alpha = (int) (255 * progress);

        int beamX = facingRight ? x + width : x - LASER_RANGE;
        int beamY = y + height / 2 - 6;
        int beamW = LASER_RANGE;
        int beamH = 12;

        // Outer glow — blue
        g.setColor(new Color(0, 100, 255, Math.min(alpha / 2, 120)));
        g.fillRect(beamX, beamY - 8, beamW, beamH + 16);
        // Core beam — bright blue
        g.setColor(new Color(60, 160, 255, Math.min(alpha, 255)));
        g.fillRect(beamX, beamY, beamW, beamH);
        // Hot white-blue center
        g.setColor(new Color(200, 230, 255, Math.min(alpha, 255)));
        g.fillRect(beamX, beamY + 4, beamW, 4);
        // Muzzle flash
        int muzzleX = facingRight ? x + width - 10 : x - 20;
        g.setColor(new Color(0, 180, 255, Math.min(alpha, 200)));
        g.fillOval(muzzleX, beamY - 14, 30, 30);
        g.setColor(new Color(220, 240, 255, Math.min(alpha, 200)));
        g.fillOval(muzzleX + 8, beamY - 6, 14, 14);
        // Impact burst
        int impactX = facingRight ? beamX + beamW - 20 : beamX - 10;
        g.setColor(new Color(0, 120, 255, Math.min(alpha / 2, 100)));
        g.fillOval(impactX - 15, beamY - 20, 50, 40);
    }
}