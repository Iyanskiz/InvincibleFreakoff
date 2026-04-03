import java.awt.*;

public class Thragg extends Fighter {

    private int specialCooldown = 0;
    private boolean armorActive = false;
    private int armorTimer = 0;

    public Thragg(int x, boolean isPlayer1) {
        super("Thragg", 180, x, isPlayer1);
        characterColor = new Color(100, 0, 0);
        accentColor    = new Color(180, 140, 0);
        attackDamage   = 16;
        animSpeed      = 7;
        width  = 130;
        height = 160;

        // Load sprites — filenames must match exactly what's in your imgs/ folder
        imgIdle        = loadImage("ThaggLevitating.png");   // closest idle-like pose
        imgForward     = loadImage("ThraggForward.png");
        imgBackward    = loadImage("ThraggBackward.png");
        imgPunch       = loadImage("ThraggPunch.png");
        imgBlock       = loadImage("ThraggBlock.png");
        imgHit         = loadImage("ThraggHit.png");
        imgLevitating  = loadImage("ThaggLevitating.png"); // note: check spelling on your file
    }

    @Override
    public void update(Fighter opponent) {
        if (specialCooldown > 0) specialCooldown--;
        if (armorActive) {
            armorTimer--;
            if (armorTimer <= 0) armorActive = false;
        }
        super.update(opponent);
    }

    @Override
    public void takeDamage(int damage) {
        if (armorActive) damage = damage / 2;
        super.takeDamage(damage);
    }

    @Override
    public void specialMove() {
        if (specialCooldown == 0 && !isAttacking) {
            armorActive     = true;
            armorTimer      = 180;
            specialCooldown = 220;
            isAttacking     = true;
            attackTimer     = 40;
            attackDamage    = 40;
            currentState    = State.SPECIAL;
            velX = facingRight ? 8 : -8;
            velY = -10;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        // Armor glow aura behind sprite
        if (armorActive) {
            g.setColor(new Color(200, 160, 0, 50));
            g.fillOval(x - 18, y - 18, width + 36, height + 36);
        }
        super.draw(g);

        // Special cooldown indicator
        if (specialCooldown > 0) {
            g.setColor(new Color(200, 150, 0, 140));
            float pct = 1f - (specialCooldown / 220f);
            g.fillArc(x + width / 2 - 15, y - 22, 30, 14, 90, (int)(360 * pct));
        }
    }
}