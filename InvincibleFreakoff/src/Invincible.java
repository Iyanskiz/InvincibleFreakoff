import java.awt.*;
import java.awt.geom.*;

public class Invincible extends Fighter {

    private int specialCooldown = 0;
    private boolean speedBoostActive = false;
    private int speedBoostTimer = 0;

    public Invincible(int x, boolean isPlayer1) {
        super("Invincible", 120, x, isPlayer1);
        characterColor = new Color(30, 90, 255);   // Blue
        accentColor    = new Color(255, 220, 0);   // Yellow
        attackDamage   = 10;
        animSpeed      = 5;
    }

    @Override
    public void update(Fighter opponent) {
        if (specialCooldown > 0) specialCooldown--;
        if (speedBoostActive) {
            speedBoostTimer--;
            if (speedBoostTimer <= 0) speedBoostActive = false;
        }
        super.update(opponent);
    }

    @Override
    public void specialMove() {
        if (specialCooldown == 0 && !isAttacking) {
            speedBoostActive = true;
            speedBoostTimer  = 120;
            specialCooldown  = 180;
            isAttacking  = true;
            attackTimer  = 25;
            attackDamage = 22;
            currentState = State.SPECIAL;
            // Dash forward
            velX = facingRight ? 14 : -14;
        }
    }

    @Override
    protected void drawCharacter(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int bx = x, by = y;
        Color col = characterColor;

        // Hurt flash
        if (isHurt && hurtTimer % 4 < 2) col = Color.WHITE;

        // Speed boost aura
        if (speedBoostActive) {
            g.setColor(new Color(255, 255, 100, 60));
            g.fillOval(bx - 10, by - 10, width + 20, height + 20);
        }

        // Legs
        g.setColor(col);
        g.fillRoundRect(bx + 18, by + 75, 18, 40, 6, 6);
        g.fillRoundRect(bx + 44, by + 75, 18, 40, 6, 6);

        // Boots
        g.setColor(accentColor);
        g.fillRoundRect(bx + 16, by + 105, 22, 12, 4, 4);
        g.fillRoundRect(bx + 42, by + 105, 22, 12, 4, 4);

        // Body
        g.setColor(col);
        g.fillRoundRect(bx + 12, by + 35, 56, 45, 10, 10);

        // Yellow chest stripe
        g.setColor(accentColor);
        g.fillRect(bx + 28, by + 38, 24, 5);
        g.setColor(new Color(255, 180, 0));
        int[] sx = {bx+28, bx+52, bx+50, bx+30};
        int[] sy = {by+43, by+43, by+78, by+78};
        g.fillPolygon(sx, sy, 4);

        // Cape
        g.setColor(new Color(200, 220, 255, 180));
        int[] cx = {bx+50, bx+75, bx+65, bx+55};
        int[] cy = {by+38, by+60, by+100, by+80};
        g.fillPolygon(cx, cy, 4);

        // Arms
        g.setColor(col);
        if (currentState == State.ATTACK_LIGHT || currentState == State.SPECIAL) {
            // Punch out
            g.fillRoundRect(bx + 60, by + 38, 30, 16, 8, 8);
            g.setColor(accentColor);
            g.fillOval(bx + 85, by + 35, 18, 18);
        } else if (currentState == State.ATTACK_HEAVY) {
            g.fillRoundRect(bx + 60, by + 30, 35, 16, 8, 8);
            g.setColor(accentColor);
            g.fillOval(bx + 90, by + 26, 20, 20);
        } else {
            g.fillRoundRect(bx + 60, by + 42, 18, 30, 8, 8);
            g.fillRoundRect(bx + 2,  by + 42, 18, 30, 8, 8);
        }

        // Head
        g.setColor(new Color(255, 210, 170));
        g.fillOval(bx + 20, by + 2, 40, 38);

        // Mask top
        g.setColor(col);
        g.fillArc(bx + 20, by + 2, 40, 24, 0, 180);

        // Mask eye band
        g.fillRect(bx + 20, by + 16, 40, 10);

        // Eyes
        g.setColor(accentColor);
        g.fillOval(bx + 24, by + 17, 10, 8);
        g.fillOval(bx + 46, by + 17, 10, 8);

        // Mouth
        g.setColor(new Color(180, 100, 80));
        g.drawArc(bx + 28, by + 27, 24, 10, 0, -180);

        // Special cooldown indicator
        if (specialCooldown > 0) {
            g.setColor(new Color(255, 255, 100, 120));
            float pct = 1f - (specialCooldown / 180f);
            g.fillArc(bx + 25, by - 18, 30, 14, 90, (int)(360 * pct));
        }
    }
}