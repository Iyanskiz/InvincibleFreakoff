import java.awt.*;
import java.awt.image.BufferedImage;

public class Invincible extends Fighter {

    private int specialCooldown = 0;
    private boolean rageActive = false;
    private int rageTimer = 0;
    // Flurry combo tracking
    private int flurryCount = 0;
    private int flurryTimer = 0;

    public Invincible(int x, boolean isPlayer1) {
        super("Invincible", 350, x, isPlayer1);
        characterColor = new Color(20, 160, 80);
        accentColor    = new Color(180, 255, 120);
        attackDamage   = 20;
        animSpeed      = 4;
        width  = 150;
        height = 185;

        imgIdle       = loadImage("InvincibleIdle.png");
        imgForward    = loadImage("InvincibleForward.png");
        imgBackward   = loadImage("InvincibleBackward.png");
        imgPunch      = loadImage("InvinciblePunch.png");
        imgBlock      = loadImage("InvincibleBlock.png");
        imgHit        = loadImage("InvincibleHit.png");
        imgLevitating = loadImage("InvincibleLevitating.png");
    }

    @Override
    public void update(Fighter opponent) {
        if (specialCooldown > 0) specialCooldown--;
        if (flurryTimer > 0) flurryTimer--;
        if (rageActive) {
            rageTimer--;
            if (rageTimer <= 0) {
                rageActive   = false;
                attackDamage = 20;
            }
        }
        super.update(opponent);
    }

    @Override
    public void specialMove() {
        // LORE: Invincible's signature is an ULTRA FLURRY of punches at superhuman speed
        // He flies at the opponent and unleashes a barrage — like his fight vs Omni-Man
        if (specialCooldown == 0 && !isAttacking) {
            rageActive      = true;
            rageTimer       = 180;
            specialCooldown = 200;
            isAttacking     = true;
            attackTimer     = 60;   // long attack — full flurry
            attackDamage    = 8;    // each hit is moderate but hits MANY times
            flurryCount     = 6;    // 6 rapid hits
            flurryTimer     = 60;
            currentState    = State.SPECIAL;
            // Fly straight at opponent fast
            velX = facingRight ? 18 : -18;
            velY = -4;
        }
    }

    // Called from GamePanel to do multi-hit flurry
    public boolean doFlurryHit() {
        if (flurryCount > 0 && flurryTimer > 0 && (flurryTimer % 10 == 0)) {
            flurryCount--;
            return true;
        }
        return false;
    }

    @Override
    public void takeDamage(int damage) {
        damage = (int)(damage * 0.75f); // mutant durability
        super.takeDamage(damage);
    }

    @Override
    public void draw(Graphics2D g) {
        if (rageActive) {
            g.setColor(new Color(80, 255, 120, 55));
            g.fillOval(x - 20, y - 20, width + 40, height + 40);
            g.setColor(new Color(120, 255, 80, 25));
            g.fillOval(x - 35, y - 35, width + 70, height + 70);
        }
        super.draw(g);
        if (specialCooldown > 0) {
            g.setColor(new Color(80, 255, 120, 140));
            float pct = 1f - (specialCooldown / 200f);
            g.fillArc(x + width/2 - 15, y - 22, 30, 14, 90, (int)(360 * pct));
        }
        if (rageActive) {
            g.setFont(new Font("Impact", Font.PLAIN, 13));
            g.setColor(new Color(80, 255, 80, 200));
            g.drawString("FLURRY!", x + width/2 - 25, y - 26);
        }
    }
}