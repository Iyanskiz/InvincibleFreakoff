import java.awt.*;

public class HealthBar {

    private int x, y, width, height;
    private boolean isPlayer1;
    private float displayedHealth = 1.0f;
    private String fighterName;

    public HealthBar(int x, int y, int width, int height, boolean isPlayer1) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isPlayer1 = isPlayer1;
    }

    public void setFighterName(String name) {
        this.fighterName = name;
    }

    public void update(int currentHealth, int maxHealth) {
        float target = (float) currentHealth / maxHealth;
        if (displayedHealth > target) {
            displayedHealth -= 0.015f;
            if (displayedHealth < target) displayedHealth = target;
        } else {
            displayedHealth = target;
        }
    }

    public void draw(Graphics2D g, int currentHealth, int maxHealth) {
        update(currentHealth, maxHealth);

        float pct = displayedHealth;

        g.setColor(new Color(20, 20, 20));
        g.fillRoundRect(x, y, width, height, height, height);

        g.setColor(new Color(100, 0, 0));
        g.fillRoundRect(x + 2, y + 2, width - 4, height - 4, height, height);

        Color healthColor;
        if (pct > 0.6f)      healthColor = new Color(60, 200, 60);
        else if (pct > 0.3f) healthColor = new Color(220, 180, 0);
        else                 healthColor = new Color(220, 40, 40);

        int fillW = (int)((width - 4) * pct);
        if (fillW < 0) fillW = 0;

        if (isPlayer1) {
            g.setColor(healthColor);
            g.fillRoundRect(x + 2, y + 2, fillW, height - 4, height, height);
            g.setColor(new Color(255, 255, 255, 40));
            g.fillRoundRect(x + 2, y + 2, fillW, (height - 4) / 2, height, height);
        } else {
            g.setColor(healthColor);
            g.fillRoundRect(x + 2 + (width - 4 - fillW), y + 2, fillW, height - 4, height, height);
            g.setColor(new Color(255, 255, 255, 40));
            g.fillRoundRect(x + 2 + (width - 4 - fillW), y + 2, fillW, (height - 4) / 2, height, height);
        }

        g.setColor(new Color(255, 255, 255, 80));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, width, height, height, height);
        g.setStroke(new BasicStroke(1));

        g.setFont(new Font("Arial", Font.BOLD, 11));
        String hpText = currentHealth + " / " + maxHealth;
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (width - fm.stringWidth(hpText)) / 2;
        g.setColor(new Color(0, 0, 0, 120));
        g.drawString(hpText, tx + 1, y + height - 4 + 1);
        g.setColor(Color.WHITE);
        g.drawString(hpText, tx, y + height - 4);

        if (fighterName != null) {
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(new Color(255, 220, 50));
            if (isPlayer1) {
                g.drawString(fighterName, x, y - 5);
            } else {
                FontMetrics fm2 = g.getFontMetrics();
                g.drawString(fighterName, x + width - fm2.stringWidth(fighterName), y - 5);
            }
        }
    }
}