import java.awt.*;

public class OmniMan extends Fighter {

    private int specialCooldown = 0;
    private boolean rageMode = false;
    private int rageTimer = 0;

    public OmniMan(int x, boolean isPlayer1) {
        super("Omni-Man", 160, x, isPlayer1);
        characterColor = new Color(180, 0, 0);
        accentColor    = new Color(240, 240, 240);
        attackDamage   = 14;
        animSpeed      = 6;
        width  = 125;
        height = 150;

        imgIdle       = loadImage("OmniBlock.png");
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
        if (rageMode) {
            rageTimer--;
            if (rageTimer <= 0) rageMode = false;
        }
        super.update(opponent);
    }

    @Override
    public void specialMove() {
        if (specialCooldown == 0 && !isAttacking) {
            rageMode        = true;
            rageTimer       = 150;
            specialCooldown = 200;
            isAttacking     = true;
            attackTimer     = 35;
            attackDamage    = 35;
            currentState    = State.SPECIAL;
            velX = facingRight ? 10 : -10;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        // Rage aura behind sprite
        if (rageMode) {
            g.setColor(new Color(255, 60, 0, 50));
            g.fillOval(x - 15, y - 15, width + 30, height + 30);
        }
        super.draw(g);

        // Special cooldown arc
        if (specialCooldown > 0) {
            g.setColor(new Color(255, 100, 0, 140));
            float pct = 1f - (specialCooldown / 200f);
            g.fillArc(x + width / 2 - 15, y - 22, 30, 14, 90, (int)(360 * pct));
        }
    }
}