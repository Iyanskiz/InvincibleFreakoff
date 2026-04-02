import java.awt.*;

public class Thragg extends Fighter {

    private int specialCooldown = 0;
    private boolean armorActive = false;
    private int armorTimer = 0;

    public Thragg(int x, boolean isPlayer1) {
        super("Thragg", 180, x, isPlayer1);
        characterColor = new Color(100, 0, 0);     // Dark red/maroon
        accentColor    = new Color(180, 140, 0);   // Gold
        attackDamage   = 16;
        animSpeed      = 7;
        width  = 90;
        height = 130;
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
            isAttacking  = true;
            attackTimer  = 40;
            attackDamage = 40;
            currentState = State.SPECIAL;
            velX = facingRight ? 8 : -8;
            velY = -10; // Leaping strike
        }
    }

    @Override
    protected void drawCharacter(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int bx = x, by = y;
        Color col = characterColor;
        if (isHurt && hurtTimer % 4 < 2) col = Color.WHITE;

        // Armor glow
        if (armorActive) {
            g.setColor(new Color(200, 160, 0, 60));
            g.fillOval(bx - 18, by - 18, width + 36, height + 36);
        }

        // Legs (thick)
        g.setColor(col);
        g.fillRoundRect(bx + 16, by + 80, 24, 50, 6, 6);
        g.fillRoundRect(bx + 50, by + 80, 24, 50, 6, 6);

        // Armored boots
        g.setColor(accentColor);
        g.fillRoundRect(bx + 14, by + 118, 28, 14, 4, 4);
        g.fillRoundRect(bx + 48, by + 118, 28, 14, 4, 4);

        // Body (massive)
        g.setColor(col);
        g.fillRoundRect(bx + 5, by + 30, 80, 56, 14, 14);

        // Chest armor plate
        g.setColor(accentColor);
        g.fillRoundRect(bx + 18, by + 34, 54, 40, 8, 8);
        g.setColor(new Color(140, 100, 0));
        g.drawRoundRect(bx + 18, by + 34, 54, 40, 8, 8);

        // Grand Regent symbol on chest (simplified)
        g.setColor(new Color(80, 40, 0));
        g.setStroke(new BasicStroke(2));
        g.drawLine(bx + 45, by + 38, bx + 45, by + 70);
        g.drawLine(bx + 30, by + 50, bx + 60, by + 50);
        g.setStroke(new BasicStroke(1));

        // Cape (dark, regal)
        g.setColor(new Color(60, 0, 0, 210));
        int[] cx = {bx+55, bx+95, bx+88, bx+65};
        int[] cy = {by+33, by+55, by+125, by+95};
        g.fillPolygon(cx, cy, 4);

        // Arms
        g.setColor(col);
        if (currentState == State.ATTACK_LIGHT || currentState == State.SPECIAL) {
            g.fillRoundRect(bx + 68, by + 34, 38, 20, 8, 8);
            g.setColor(accentColor);
            g.fillOval(bx + 98, by + 28, 26, 26);
        } else if (currentState == State.ATTACK_HEAVY) {
            g.fillRoundRect(bx + 68, by + 22, 44, 20, 8, 8);
            g.setColor(accentColor);
            g.fillOval(bx + 104, by + 16, 28, 28);
        } else {
            g.fillRoundRect(bx + 68, by + 40, 22, 36, 8, 8);
            g.fillRoundRect(bx - 6,  by + 40, 22, 36, 8, 8);
        }

        // Head (large, imposing)
        g.setColor(new Color(195, 145, 110));
        g.fillOval(bx + 14, by - 4, 62, 48);

        // Hair (dark, slicked)
        g.setColor(new Color(10, 10, 10));
        g.fillArc(bx + 14, by - 4, 62, 30, 0, 180);

        // Beard stubble
        g.setColor(new Color(30, 15, 5));
        g.fillArc(bx + 22, by + 24, 46, 18, 0, -180);

        // Eyes (cold, ruthless)
        g.setColor(new Color(180, 0, 0));
        g.fillOval(bx + 22, by + 12, 14, 10);
        g.fillOval(bx + 54, by + 12, 14, 10);
        g.setColor(Color.WHITE);
        g.fillOval(bx + 25, by + 14, 5, 5);
        g.fillOval(bx + 57, by + 14, 5, 5);

        // Scowl
        g.setColor(new Color(140, 80, 50));
        g.setStroke(new BasicStroke(2));
        g.drawLine(bx + 28, by + 32, bx + 62, by + 32);
        g.setStroke(new BasicStroke(1));

        if (specialCooldown > 0) {
            g.setColor(new Color(200, 150, 0, 120));
            float pct = 1f - (specialCooldown / 220f);
            g.fillArc(bx + 25, by - 20, 30, 14, 90, (int)(360 * pct));
        }
    }
}