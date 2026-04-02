import java.awt.*;

public class OmniMan extends Fighter {

    private int specialCooldown = 0;
    private boolean rageMode = false;
    private int rageTimer = 0;

    public OmniMan(int x, boolean isPlayer1) {
        super("Omni-Man", 160, x, isPlayer1);
        characterColor = new Color(180, 0, 0);    // Red
        accentColor    = new Color(240, 240, 240); // White/silver
        attackDamage   = 14;
        animSpeed      = 6;
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
            rageMode    = true;
            rageTimer   = 150;
            specialCooldown = 200;
            isAttacking = true;
            attackTimer = 35;
            attackDamage = rageMode ? 35 : 28;
            currentState = State.SPECIAL;
            velX = facingRight ? 10 : -10;
        }
    }

    @Override
    protected void drawCharacter(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int bx = x, by = y;
        Color col = rageMode ? new Color(220, 40, 40) : characterColor;
        if (isHurt && hurtTimer % 4 < 2) col = Color.WHITE;

        // Rage aura
        if (rageMode) {
            g.setColor(new Color(255, 60, 0, 50));
            g.fillOval(bx - 15, by - 15, width + 30, height + 30);
        }

        // Legs
        g.setColor(col);
        g.fillRoundRect(bx + 18, by + 75, 20, 45, 6, 6);
        g.fillRoundRect(bx + 46, by + 75, 20, 45, 6, 6);

        // Boots
        g.setColor(accentColor);
        g.fillRoundRect(bx + 16, by + 108, 24, 12, 4, 4);
        g.fillRoundRect(bx + 44, by + 108, 24, 12, 4, 4);

        // Body - bigger/bulkier
        g.setColor(col);
        g.fillRoundRect(bx + 8, by + 32, 64, 48, 12, 12);

        // Cape (red, dramatic)
        g.setColor(new Color(140, 0, 0, 200));
        int[] cx = {bx+48, bx+85, bx+78, bx+58};
        int[] cy = {by+35, by+50, by+110, by+85};
        g.fillPolygon(cx, cy, 4);

        // Chest V design
        g.setColor(accentColor);
        int[] vx = {bx+20, bx+40, bx+60};
        int[] vy = {by+35, by+65, by+35};
        g.drawPolyline(vx, vy, 3);
        g.setStroke(new BasicStroke(3));
        g.drawPolyline(vx, vy, 3);
        g.setStroke(new BasicStroke(1));

        // Arms
        g.setColor(col);
        if (currentState == State.ATTACK_LIGHT || currentState == State.SPECIAL) {
            g.fillRoundRect(bx + 62, by + 35, 35, 18, 8, 8);
            g.setColor(accentColor);
            g.fillOval(bx + 90, by + 30, 22, 22);
        } else if (currentState == State.ATTACK_HEAVY) {
            g.fillRoundRect(bx + 62, by + 25, 40, 18, 8, 8);
            g.setColor(accentColor);
            g.fillOval(bx + 95, by + 20, 24, 24);
        } else {
            g.fillRoundRect(bx + 62, by + 40, 20, 32, 8, 8);
            g.fillRoundRect(bx - 2,  by + 40, 20, 32, 8, 8);
        }

        // Head (larger, mustache)
        g.setColor(new Color(220, 175, 140));
        g.fillOval(bx + 16, by, 48, 44);

        // Hair - black
        g.setColor(new Color(20, 20, 20));
        g.fillArc(bx + 16, by, 48, 28, 0, 180);

        // Mustache
        g.setColor(new Color(20, 20, 20));
        g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(bx + 22, by + 26, 14, 10, 0, -180);
        g.drawArc(bx + 44, by + 26, 14, 10, 0, -180);
        g.setStroke(new BasicStroke(1));

        // Eyes (intense)
        g.setColor(new Color(50, 50, 50));
        g.fillOval(bx + 22, by + 16, 12, 10);
        g.fillOval(bx + 46, by + 16, 12, 10);
        g.setColor(Color.WHITE);
        g.fillOval(bx + 24, by + 17, 5, 5);
        g.fillOval(bx + 48, by + 17, 5, 5);

        // Special cooldown
        if (specialCooldown > 0) {
            g.setColor(new Color(255, 100, 0, 120));
            float pct = 1f - (specialCooldown / 200f);
            g.fillArc(bx + 25, by - 18, 30, 14, 90, (int)(360 * pct));
        }
    }
}