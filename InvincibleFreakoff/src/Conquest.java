import java.awt.*;


//iyans ssh key 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
public class Conquest extends Fighter {

    private int specialCooldown = 0;
    private boolean berserkMode = false;
    private int berserkTimer = 0;

    public Conquest(int x, boolean isPlayer1) {
        super("Conquest", 170, x, isPlayer1);
        characterColor = new Color(60, 60, 80);    // Dark gray/blue
        accentColor    = new Color(200, 50, 50);   // Red
        attackDamage   = 15;
        animSpeed      = 6;
        width  = 88;
        height = 125;
    }

    @Override
    public void update(Fighter opponent) {
        if (specialCooldown > 0) specialCooldown--;
        if (berserkMode) {
            berserkTimer--;
            if (berserkTimer <= 0) berserkMode = false;
        }
        super.update(opponent);
    }

    @Override
    public void specialMove() {
        if (specialCooldown == 0 && !isAttacking) {
            berserkMode     = true;
            berserkTimer    = 160;
            specialCooldown = 210;
            isAttacking  = true;
            attackTimer  = 38;
            attackDamage = 38;
            currentState = State.SPECIAL;
            velX = facingRight ? 12 : -12;
        }
    }

    @Override
    protected void drawCharacter(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int bx = x, by = y;
        Color col = berserkMode ? new Color(90, 30, 30) : characterColor;
        if (isHurt && hurtTimer % 4 < 2) col = Color.WHITE;

        // Berserk aura
        if (berserkMode) {
            g.setColor(new Color(200, 0, 0, 45));
            g.fillOval(bx - 15, by - 15, width + 30, height + 30);
        }

        // Legs
        g.setColor(col);
        g.fillRoundRect(bx + 16, by + 78, 22, 48, 6, 6);
        g.fillRoundRect(bx + 50, by + 78, 22, 48, 6, 6);

        // Armored boots
        g.setColor(new Color(40, 40, 50));
        g.fillRoundRect(bx + 14, by + 114, 26, 12, 4, 4);
        g.fillRoundRect(bx + 48, by + 114, 26, 12, 4, 4);

        // Body
        g.setColor(col);
        g.fillRoundRect(bx + 8, by + 30, 72, 54, 12, 12);

        // Armor detail lines
        g.setColor(new Color(80, 80, 100));
        g.setStroke(new BasicStroke(2));
        g.drawLine(bx + 20, by + 34, bx + 68, by + 34);
        g.drawLine(bx + 20, by + 50, bx + 68, by + 50);
        g.drawLine(bx + 20, by + 66, bx + 68, by + 66);
        g.setStroke(new BasicStroke(1));

        // Red chest symbol
        g.setColor(accentColor);
        g.fillOval(bx + 32, by + 38, 24, 20);

        // Cape
        g.setColor(new Color(50, 10, 10, 200));
        int[] cx = {bx+52, bx+90, bx+82, bx+60};
        int[] cy = {by+32, by+48, by+118, by+90};
        g.fillPolygon(cx, cy, 4);

        // Arms
        g.setColor(col);
        if (currentState == State.ATTACK_LIGHT || currentState == State.SPECIAL) {
            g.fillRoundRect(bx + 66, by + 32, 36, 18, 8, 8);
            g.setColor(new Color(40, 40, 50));
            g.fillOval(bx + 94, by + 26, 24, 24);
        } else if (currentState == State.ATTACK_HEAVY) {
            g.fillRoundRect(bx + 66, by + 22, 42, 18, 8, 8);
            g.setColor(new Color(40, 40, 50));
            g.fillOval(bx + 100, by + 16, 26, 26);
        } else {
            g.fillRoundRect(bx + 66, by + 38, 20, 34, 8, 8);
            g.fillRoundRect(bx - 4,  by + 38, 20, 34, 8, 8);
        }

        // Armored gloves
        g.setColor(new Color(40, 40, 50));
        if (currentState != State.ATTACK_LIGHT && currentState != State.ATTACK_HEAVY && currentState != State.SPECIAL) {
            g.fillOval(bx + 64, by + 66, 20, 18);
            g.fillOval(bx - 6, by + 66, 20, 18);
        }

        // Head with battle helmet
        g.setColor(new Color(50, 50, 65));
        g.fillOval(bx + 14, by - 6, 60, 50);

        // Helmet visor/eye slit
        g.setColor(accentColor);
        g.fillRect(bx + 18, by + 14, 52, 10);

        // Eyes glowing through visor
        g.setColor(new Color(255, 80, 80));
        g.fillOval(bx + 22, by + 15, 14, 8);
        g.fillOval(bx + 52, by + 15, 14, 8);

        // Helmet ridge
        g.setColor(new Color(30, 30, 45));
        g.fillRect(bx + 40, by - 6, 8, 20);

        // Battle scars / chin
        g.setColor(new Color(170, 130, 110));
        g.fillArc(bx + 20, by + 28, 48, 20, 0, -180);

        // Scar line
        g.setColor(new Color(140, 60, 60));
        g.setStroke(new BasicStroke(2));
        g.drawLine(bx + 50, by + 6, bx + 62, by + 24);
        g.setStroke(new BasicStroke(1));

        if (specialCooldown > 0) {
            g.setColor(new Color(200, 50, 50, 120));
            float pct = 1f - (specialCooldown / 210f);
            g.fillArc(bx + 25, by - 22, 30, 14, 90, (int)(360 * pct));
        }
    }
}