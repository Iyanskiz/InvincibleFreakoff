import java.awt.*;

public class OmniMan extends Fighter {

    private int specialCooldown = 0;
    private boolean chargeActive = false;
    private int chargeTimer = 0;

    public OmniMan(int x, boolean isPlayer1) {
        super("Omni-Man", 300, x, isPlayer1);
        characterColor = new Color(180, 0, 0);
        accentColor    = new Color(240, 240, 240);
        attackDamage   = 14;
        animSpeed      = 6;
        width  = 125;
        height = 150;

        imgIdle       = loadImage("OmniLevitating.png");
        imgForward    = loadImage("OmniForward.png");
        imgBackward   = loadImage("OmniBackward.png");
        imgPunch      = loadImage("OmniPunch.png");
        imgBlock      = loadImage("OmniBlock.png");
        imgHit        = loadImage("OmniHit.png");
        imgLevitating = loadImage("OmniLevitating.png");
    }

    @Override
    public void update(Fighter opponent) {
        if (specialCooldown > 0) specialCooldown--;
        if (chargeActive) {
            chargeTimer--;
            if (chargeTimer <= 0) chargeActive = false;
        }
        super.update(opponent);
    }

    @Override
    public void specialMove() {
        // LORE: Omni-Man's signature move is the VILTRUMITE CHARGE —
        // He rockets through opponents like a bullet, the same way he
        // flew through Cecil's men and punched through Invincible repeatedly.
        // Incredibly fast horizontal charge with massive single hit damage.
        if (specialCooldown == 0 && !isAttacking) {
            chargeActive    = true;
            chargeTimer     = 40;
            specialCooldown = 220;
            isAttacking     = true;
            attackTimer     = 40;
            attackDamage    = 45; // one devastating hit
            currentState    = State.SPECIAL;
            // Rocket-fast charge — fastest in game
            velX = facingRight ? 22 : -22;
            velY = 0; // perfectly horizontal like a bullet
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (chargeActive) {
            // Speed trail effect
            g.setColor(new Color(255, 60, 0, 40));
            g.fillOval(x - 25, y - 10, width + 50, height + 20);
            g.setColor(new Color(255, 200, 50, 20));
            g.fillOval(x - 40, y - 5, width + 80, height + 10);
        }
        super.draw(g);
        if (specialCooldown > 0) {
            g.setColor(new Color(255, 100, 0, 140));
            float pct = 1f - (specialCooldown / 220f);
            g.fillArc(x + width/2 - 15, y - 22, 30, 14, 90, (int)(360 * pct));
        }
        if (chargeActive) {
            g.setFont(new Font("Impact", Font.PLAIN, 13));
            g.setColor(new Color(255, 120, 0, 220));
            g.drawString("VILTRUMITE!", x + width/2 - 35, y - 26);
        }
    }
}