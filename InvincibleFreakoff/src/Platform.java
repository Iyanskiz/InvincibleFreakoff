import java.awt.*;

/**
 * A one-way platform — fighters land on top, can drop through by pressing DOWN+JUMP.
 * Each platform has a visual style matched to its stage.
 */
public class Platform {
    public int x, y, w, h;
    public PlatformStyle style;

    public enum PlatformStyle {
        CITY, SPACE, STORM, VOID, VOLCANO, THRONE, ARCTIC, GCI, MOON, BATTLEFIELD
    }

    public Platform(int x, int y, int w, PlatformStyle style) {
        this.x = x; this.y = y; this.w = w; this.h = 18; this.style = style;
    }

    public void draw(Graphics2D g, int tick) {
        switch (style) {
            case CITY:    drawCity(g, tick);   break;
            case SPACE:   drawSpace(g, tick);  break;
            case STORM:   drawStorm(g, tick);  break;
            case VOID:    drawVoid(g, tick);   break;
            case VOLCANO: drawVolcano(g, tick);break;
            case THRONE:  drawThrone(g, tick); break;
            case ARCTIC:      drawArctic(g, tick);      break;
            case GCI:         drawGci(g, tick);         break;
            case MOON:        drawMoon(g, tick);        break;
            case BATTLEFIELD: drawBattlefield(g, tick); break;
        }
        // Drop-hint text drawn from GamePanel; nothing here
    }

    // ── City: concrete ledge with steel edge ─────────────────────────────────
    private void drawCity(Graphics2D g, int tick) {
        g.setColor(new Color(35, 32, 28));
        g.fillRoundRect(x, y, w, h, 4, 4);
        g.setColor(new Color(55, 50, 42));
        g.fillRoundRect(x + 2, y + 2, w - 4, h - 8, 3, 3);
        // Steel lip
        g.setColor(new Color(90, 90, 100));
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + 4, y + 1, x + w - 4, y + 1);
        g.setStroke(new BasicStroke(1));
        // Rivets
        for (int rx = x + 12; rx < x + w - 10; rx += 40) {
            g.setColor(new Color(70, 70, 80)); g.fillOval(rx, y + 4, 5, 5);
        }
    }

    // ── Space: glowing energy platform ───────────────────────────────────────
    private void drawSpace(Graphics2D g, int tick) {
        float pulse = (float)(Math.sin(tick * 0.06 + x * 0.01) * 0.3 + 0.7);
        int alpha = (int)(pulse * 180);
        // Base
        g.setColor(new Color(20, 40, 90, 200));
        g.fillRoundRect(x, y, w, h, 6, 6);
        // Glow top edge
        g.setColor(new Color(80, 160, 255, alpha));
        g.setStroke(new BasicStroke(3));
        g.drawLine(x + 4, y + 1, x + w - 4, y + 1);
        g.setStroke(new BasicStroke(1));
        // Energy nodes
        for (int nx = x + 15; nx < x + w - 10; nx += 50) {
            g.setColor(new Color(120, 200, 255, alpha));
            g.fillOval(nx - 4, y + 4, 8, 8);
        }
    }

    // ── Storm: wet stone ledge ────────────────────────────────────────────────
    private void drawStorm(Graphics2D g, int tick) {
        g.setColor(new Color(28, 32, 42));
        g.fillRoundRect(x, y, w, h, 4, 4);
        g.setColor(new Color(40, 46, 60));
        g.fillRoundRect(x + 2, y + 2, w - 4, h - 8, 3, 3);
        // Wet shimmer
        float shim = (float)(Math.sin(tick * 0.04 + x * 0.02) * 0.2 + 0.3);
        g.setColor(new Color(100, 140, 200, (int)(shim * 120)));
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + 2, y + 1, x + w - 2, y + 1);
        g.setStroke(new BasicStroke(1));
    }

    // ── Void: dark floating slab with purple glow ─────────────────────────────
    private void drawVoid(Graphics2D g, int tick) {
        float pulse = (float)(Math.sin(tick * 0.05 + x * 0.008) * 0.4 + 0.6);
        g.setColor(new Color(15, 0, 30, 230));
        g.fillRoundRect(x, y, w, h, 6, 6);
        // Purple edge
        int a = (int)(pulse * 220);
        g.setColor(new Color(160, 0, 255, a));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, w, h, 6, 6);
        g.setStroke(new BasicStroke(1));
        // Glow drip underneath
        g.setColor(new Color(120, 0, 200, (int)(pulse * 60)));
        g.fillRect(x + 10, y + h, w - 20, 6);
    }

    // ── Volcano: scorched rock slab ───────────────────────────────────────────
    private void drawVolcano(Graphics2D g, int tick) {
        g.setColor(new Color(30, 10, 5));
        g.fillRoundRect(x, y, w, h, 3, 3);
        g.setColor(new Color(55, 20, 8));
        g.fillRoundRect(x + 2, y + 2, w - 4, h - 8, 2, 2);
        // Lava crack glow
        float glow = (float)(Math.sin(tick * 0.07 + x * 0.015) * 0.3 + 0.7);
        g.setColor(new Color(255, 60, 0, (int)(glow * 100)));
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + 8, y + 1, x + w / 3, y + 1);
        g.drawLine(x + w * 2 / 3, y + 1, x + w - 8, y + 1);
        g.setStroke(new BasicStroke(1));
        // Ember drip
        g.setColor(new Color(255, 100, 0, (int)(glow * 80)));
        g.fillRect(x + 15, y + h, w - 30, 4);
    }

    // ── Throne: carved stone with gold trim ───────────────────────────────────
    private void drawThrone(Graphics2D g, int tick) {
        g.setColor(new Color(25, 8, 5));
        g.fillRoundRect(x, y, w, h, 4, 4);
        g.setColor(new Color(40, 14, 8));
        g.fillRoundRect(x + 2, y + 2, w - 4, h - 8, 3, 3);
        // Gold top trim
        g.setColor(new Color(200, 150, 0, 200));
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + 4, y + 1, x + w - 4, y + 1);
        g.setStroke(new BasicStroke(1));
        // Gold studs
        for (int sx = x + 16; sx < x + w - 10; sx += 36) {
            g.setColor(new Color(220, 170, 0)); g.fillOval(sx, y + 5, 5, 5);
        }
    }

    // ── Arctic: ice shelf ─────────────────────────────────────────────────────
    private void drawArctic(Graphics2D g, int tick) {
        g.setColor(new Color(30, 55, 90, 220));
        g.fillRoundRect(x, y, w, h, 5, 5);
        g.setColor(new Color(80, 130, 180, 160));
        g.fillRoundRect(x + 2, y + 2, w - 4, h / 2, 4, 4);
        // Ice highlight
        float shim = (float)(Math.sin(tick * 0.03 + x * 0.02) * 0.2 + 0.6);
        g.setColor(new Color(180, 220, 255, (int)(shim * 160)));
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + 4, y + 1, x + w - 4, y + 1);
        g.setStroke(new BasicStroke(1));
    }

    private void drawGci(Graphics2D g, int tick) {
        float pulse = (float)(Math.sin(tick * 0.08 + x * 0.02) * 0.3 + 0.7);
        g.setColor(new Color(18, 28, 38));
        g.fillRoundRect(x, y, w, h, 4, 4);
        g.setColor(new Color(30, 45, 58));
        g.fillRoundRect(x + 2, y + 2, w - 4, h - 8, 3, 3);
        g.setColor(new Color(0, 220, 255, (int)(pulse * 200)));
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + 4, y + 1, x + w - 4, y + 1);
        g.setStroke(new BasicStroke(1));
        for (int lx = x + 10; lx < x + w - 8; lx += 28) {
            g.setColor(new Color(0, 180, 220, (int)(pulse * 140)));
            g.fillRect(lx, y + 5, 12, 3);
        }
    }

    private void drawMoon(Graphics2D g, int tick) {
        g.setColor(new Color(72, 72, 82));
        g.fillRoundRect(x, y, w, h, 5, 5);
        g.setColor(new Color(95, 95, 108));
        g.fillRoundRect(x + 2, y + 2, w - 4, h - 7, 4, 4);
        for (int c = 0; c < 3; c++) {
            int cx = x + 18 + c * (w / 3);
            g.setColor(new Color(55, 55, 65));
            g.fillOval(cx, y + 4, 10 + c * 2, 6);
        }
        g.setColor(new Color(200, 210, 230, 90));
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + 3, y + 1, x + w - 3, y + 1);
        g.setStroke(new BasicStroke(1));
    }

    private void drawBattlefield(Graphics2D g, int tick) {
        float glow = (float)(Math.sin(tick * 0.06 + x * 0.01) * 0.3 + 0.7);
        g.setColor(new Color(35, 8, 8));
        g.fillRoundRect(x, y, w, h, 3, 3);
        g.setColor(new Color(55, 15, 12));
        g.fillRoundRect(x + 2, y + 2, w - 4, h - 8, 2, 2);
        g.setColor(new Color(180, 20, 20, (int)(glow * 120)));
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + 6, y + 1, x + w - 6, y + 1);
        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(120, 0, 0, (int)(glow * 70)));
        g.fillRect(x + 8, y + h, w - 16, 5);
    }

    /** Returns a hint rect for the drop-through indicator */
    public Rectangle getBounds() { return new Rectangle(x, y, w, h); }
}