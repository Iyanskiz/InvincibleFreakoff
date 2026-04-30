import java.awt.*;

public class Thragg extends Fighter {

    private int specialCooldown = 0;
    private boolean graxActive = false;
    private int graxTimer = 0;

    public Thragg(int x, boolean isPlayer1) {
        super("Thragg", 320, x, isPlayer1);
        characterColor = new Color(100, 0, 0);
        accentColor    = new Color(180, 140, 0);
        attackDamage   = 16;
        animSpeed      = 7;
        width  = 150;
        height = 185;

        imgIdle       = loadImage("ThraggIdle.png");
        imgForward    = loadImage("ThraggForward.png");
        imgBackward   = loadImage("ThraggBackward.png");
        imgPunch      = loadImage("ThraggPunch.png");
        imgBlock      = loadImage("ThraggBlock.png");
        imgHit        = loadImage("ThraggHit.png");
        imgLevitating = loadImage("ThaggLevitating.png");
    }

    @Override
    public void update(Fighter opponent) {
        if (specialCooldown > 0) specialCooldown--;
        if (graxActive) {
            graxTimer--;
            if (graxTimer <= 0) graxActive = false;
        }
        super.update(opponent);
    }

    @Override
    public void takeDamage(int damage) {
        if (graxActive) damage = damage / 4; // near invincible during GRAX
        super.takeDamage(damage);
    }

    @Override
    public void specialMove() {
        // LORE: Thragg's signature is the GRAX —
        // The deadliest Viltrumite technique, he grabs the opponent,
        // tears into them with his teeth and bare hands simultaneously.
        // He used this to kill Omni-Man and nearly kill Invincible.
        // Short range but DEVASTATING damage + damage reduction while executing.
        if (specialCooldown == 0 && !isAttacking) {
            graxActive      = true;
            graxTimer       = 120;  // lasts 2 seconds
            specialCooldown = 280;  // long cooldown — it's a finishing move
            isAttacking     = true;
            attackTimer     = 50;
            attackDamage    = 55;   // highest damage in the game
            currentState    = State.SPECIAL;
            // Lunge forward to grab range
            velX = facingRight ? 12 : -12;
            velY = -3;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (graxActive) {
            // Blood-red aura — the GRAX is terrifying
            g.setColor(new Color(180, 0, 0, 60));
            g.fillOval(x - 18, y - 18, width + 36, height + 36);
            g.setColor(new Color(255, 0, 0, 20));
            g.fillOval(x - 35, y - 35, width + 70, height + 70);
        }
        super.draw(g);
        if (specialCooldown > 0) {
            g.setColor(new Color(200, 150, 0, 140));
            float pct = 1f - (specialCooldown / 280f);
            g.fillArc(x + width/2 - 15, y - 22, 30, 14, 90, (int)(360 * pct));
        }
        if (graxActive) {
            g.setFont(new Font("Impact", Font.PLAIN, 14));
            g.setColor(new Color(255, 0, 0, 220));
            g.drawString("THE GRAX!", x + width/2 - 35, y - 26);
        }
    }
}