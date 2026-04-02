
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
 
public class CharacterSelect extends JPanel implements KeyListener, ActionListener {
 
    // 0=Invincible, 1=OmniMan, 2=Thragg, 3=Conquest
    private int p1Selection = 0;
    private int p2Selection = 1;
    private boolean p1Confirmed = false;
    private boolean p2Confirmed = false;
 
    private javax.swing.Timer animTimer;
    private int tick = 0;
 
    private JFrame parentFrame;
 
    private final String[] names   = {"INVINCIBLE", "OMNI-MAN", "THRAGG", "CONQUEST"};
    private final String[] subtitles = {"Mark Grayson", "Nolan Grayson", "Grand Regent", "The Conqueror"};
    private final Color[]  colors  = {
        new Color(30, 90, 255),
        new Color(180, 0, 0),
        new Color(100, 0, 0),
        new Color(60, 60, 80)
    };
    private final Color[] accents = {
        new Color(255, 220, 0),
        new Color(240, 240, 240),
        new Color(180, 140, 0),
        new Color(200, 50, 50)
    };
    private final int[] statPower  = {80, 90, 95, 88};
    private final int[] statSpeed  = {90, 85, 70, 75};
    private final int[] statHealth = {75, 90, 95, 88};
 
    public CharacterSelect(JFrame frame) {
        this.parentFrame = frame;
        setPreferredSize(new Dimension(1100, 650));
        setFocusable(true);
        addKeyListener(this);
        animTimer = new javax.swing.Timer(16, this);
        animTimer.start();
    }
 
    @Override
    public void actionPerformed(ActionEvent e) {
        tick++;
        repaint();
    }
 
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 
        int W = getWidth(), H = getHeight();
 
        // ── Background ───────────────────────────────────
        g.setColor(new Color(8, 8, 14));
        g.fillRect(0, 0, W, H);
 
        // Grid lines
        g.setColor(new Color(255, 60, 60, 20));
        for (int i = 0; i < W; i += 50) g.drawLine(i, 0, i, H);
        for (int i = 0; i < H; i += 50) g.drawLine(0, i, W, i);
 
        // Center radial glow
        RadialGradientPaint radial = new RadialGradientPaint(
            W / 2f, H / 2f, W * 0.55f,
            new float[]{0f, 1f},
            new Color[]{new Color(160, 0, 0, 40), new Color(0, 0, 0, 0)}
        );
        g.setPaint(radial);
        g.fillRect(0, 0, W, H);
 
        // ── Title ────────────────────────────────────────
        drawTitle(g, W);
 
        // ── Instructions ─────────────────────────────────
        g.setFont(new Font("Arial", Font.BOLD, 13));
        g.setColor(new Color(255, 255, 255, 120));
        g.drawString("P1: A/D to select  |  F to confirm", 30, H - 50);
        g.drawString("P2: ◄/► to select  |  NUMPAD 1 to confirm", W - 330, H - 50);
 
        if (!p1Confirmed && !p2Confirmed) {
            drawBlinkingText(g, "PRESS CONFIRM TO LOCK IN", W/2, H - 30, new Color(255, 200, 50));
        }
 
        // ── Character Cards ───────────────────────────────
        int cardW = 210, cardH = 280;
        int totalW = 4 * cardW + 3 * 16;
        int startX = (W - totalW) / 2;
        int cardY = 130;
 
        for (int i = 0; i < 4; i++) {
            int cx = startX + i * (cardW + 16);
            drawCard(g, i, cx, cardY, cardW, cardH);
        }
 
        // ── VS divider ────────────────────────────────────
        drawVSDivider(g, W, H);
 
        // ── Bottom confirm panels ─────────────────────────
        drawConfirmPanel(g, p1Selection, 30, H - 110, 200, 50, 1, p1Confirmed);
        drawConfirmPanel(g, p2Selection, W - 230, H - 110, 200, 50, 2, p2Confirmed);
    }
 
    private void drawTitle(Graphics2D g, int W) {
        // Shadow
        g.setFont(new Font("Impact", Font.BOLD, 58));
        g.setColor(new Color(0, 0, 0, 180));
        g.drawString("INVINCIBLE  FREAKOFF", W/2 - 297, 75);
 
        // Gradient text
        GradientPaint gp = new GradientPaint(0, 20, new Color(255, 220, 50), 0, 72, new Color(255, 60, 0));
        g.setPaint(gp);
        g.drawString("INVINCIBLE  FREAKOFF", W/2 - 298, 74);
 
        // Subtitle
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(new Color(255, 255, 255, 80));
        String sub = "— SELECT YOUR FIGHTER —";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(sub, (W - fm.stringWidth(sub)) / 2, 100);
    }
 
    private void drawCard(Graphics2D g, int index, int cx, int cy, int cw, int ch) {
        boolean p1Here = (p1Selection == index);
        boolean p2Here = (p2Selection == index);
        boolean confirmed = (p1Here && p1Confirmed) || (p2Here && p2Confirmed);
 
        Color base  = colors[index];
        Color acc   = accents[index];
 
        // Hover/pulse animation
        float pulse = (float)(Math.sin(tick * 0.08) * 0.5 + 0.5);
 
        // Card background
        Color bg = new Color(15, 15, 25);
        g.setColor(bg);
        g.fillRoundRect(cx, cy, cw, ch, 12, 12);
 
        // Gradient overlay
        GradientPaint grad = new GradientPaint(cx, cy, new Color(base.getRed(), base.getGreen(), base.getBlue(), 60),
                                                cx, cy + ch, new Color(0, 0, 0, 0));
        g.setPaint(grad);
        g.fillRoundRect(cx, cy, cw, ch, 12, 12);
 
        // Border
        Color borderCol;
        if (confirmed) {
            borderCol = new Color(255, 255, 100);
        } else if (p1Here || p2Here) {
            int alpha = (int)(150 + 105 * pulse);
            borderCol = new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), alpha);
        } else {
            borderCol = new Color(60, 60, 80);
        }
        g.setColor(borderCol);
        g.setStroke(new BasicStroke(p1Here || p2Here ? 3 : 1.5f));
        g.drawRoundRect(cx, cy, cw, ch, 12, 12);
        g.setStroke(new BasicStroke(1));
 
        // Character silhouette / avatar area
        drawCharacterPreview(g, index, cx + cw/2, cy + 120, p1Here || p2Here);
 
        // Name
        g.setFont(new Font("Impact", Font.PLAIN, 26));
        GradientPaint nameGrad = new GradientPaint(cx, cy + 210, acc, cx, cy + 235, acc.darker());
        g.setPaint(nameGrad);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(names[index], cx + (cw - fm.stringWidth(names[index])) / 2, cy + 225);
 
        // Subtitle
        g.setFont(new Font("Arial", Font.ITALIC, 11));
        g.setColor(new Color(200, 200, 200, 160));
        fm = g.getFontMetrics();
        g.drawString(subtitles[index], cx + (cw - fm.stringWidth(subtitles[index])) / 2, cy + 242);
 
        // Stats
        drawStatBars(g, index, cx + 10, cy + 250, cw - 20);
 
        // P1 / P2 cursor
        if (p1Here) drawCursor(g, cx, cy, cw, ch, new Color(50, 150, 255), "P1", p1Confirmed);
        if (p2Here) drawCursor(g, cx + 3, cy + 3, cw - 6, ch - 6, new Color(255, 80, 80), "P2", p2Confirmed);
    }
 
    private void drawCharacterPreview(Graphics2D g, int index, int cx, int cy, boolean active) {
        float scale = active ? 1.1f : 1.0f;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(cx, cy);
        g2.scale(scale, scale);
        g2.translate(-cx, -cy);
 
        Color col = colors[index];
        Color acc = accents[index];
 
        // Simple silhouette matching each character
        switch (index) {
            case 0: drawInvinciblePreview(g2, cx, cy, col, acc); break;
            case 1: drawOmniManPreview(g2, cx, cy, col, acc); break;
            case 2: drawThraggPreview(g2, cx, cy, col, acc); break;
            case 3: drawConquestPreview(g2, cx, cy, col, acc); break;
        }
        g2.dispose();
    }
 
    private void drawInvinciblePreview(Graphics2D g, int cx, int cy, Color col, Color acc) {
        // Body
        g.setColor(col);
        g.fillRoundRect(cx - 22, cy - 60, 44, 50, 8, 8);
        // Head
        g.setColor(new Color(255, 210, 170));
        g.fillOval(cx - 16, cy - 95, 32, 32);
        g.setColor(col);
        g.fillArc(cx - 16, cy - 95, 32, 22, 0, 180);
        g.fillRect(cx - 16, cy - 78, 32, 10);
        g.setColor(acc);
        g.fillOval(cx - 12, cy - 77, 8, 6);
        g.fillOval(cx + 4, cy - 77, 8, 6);
        // Legs
        g.setColor(col);
        g.fillRoundRect(cx - 20, cy - 12, 16, 40, 4, 4);
        g.fillRoundRect(cx + 4, cy - 12, 16, 40, 4, 4);
        // Cape
        g.setColor(new Color(150, 180, 255, 150));
        int[] cx2 = {cx+14, cx+38, cx+30, cx+18};
        int[] cy2 = {cy-58, cy-38, cy+20, cy+0};
        g.fillPolygon(cx2, cy2, 4);
        // Chest stripe
        g.setColor(acc);
        g.fillRect(cx - 14, cy - 56, 8, 32);
    }
 
    private void drawOmniManPreview(Graphics2D g, int cx, int cy, Color col, Color acc) {
        g.setColor(col);
        g.fillRoundRect(cx - 26, cy - 62, 52, 54, 8, 8);
        g.setColor(new Color(220, 175, 140));
        g.fillOval(cx - 20, cy - 100, 40, 40);
        g.setColor(new Color(20, 20, 20));
        g.fillArc(cx - 20, cy - 100, 40, 26, 0, 180);
        // Mustache
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(cx - 14, cy - 72, 12, 8, 0, -180);
        g.drawArc(cx + 2, cy - 72, 12, 8, 0, -180);
        g.setStroke(new BasicStroke(1));
        // Legs
        g.setColor(col);
        g.fillRoundRect(cx - 24, cy - 10, 20, 42, 4, 4);
        g.fillRoundRect(cx + 4, cy - 10, 20, 42, 4, 4);
        // Cape
        g.setColor(new Color(140, 0, 0, 200));
        int[] cx2 = {cx+18, cx+44, cx+36, cx+22};
        int[] cy2 = {cy-60, cy-38, cy+28, cy+8};
        g.fillPolygon(cx2, cy2, 4);
        // Chest V
        g.setColor(acc);
        g.setStroke(new BasicStroke(3));
        g.drawLine(cx - 14, cy - 58, cx, cy - 34);
        g.drawLine(cx, cy - 34, cx + 14, cy - 58);
        g.setStroke(new BasicStroke(1));
    }
 
    private void drawThraggPreview(Graphics2D g, int cx, int cy, Color col, Color acc) {
        g.setColor(col);
        g.fillRoundRect(cx - 30, cy - 64, 60, 58, 10, 10);
        // Chest plate
        g.setColor(acc);
        g.fillRoundRect(cx - 20, cy - 58, 40, 36, 6, 6);
        g.setColor(new Color(195, 145, 110));
        g.fillOval(cx - 24, cy - 104, 48, 46);
        g.setColor(new Color(10, 10, 10));
        g.fillArc(cx - 24, cy - 104, 48, 28, 0, 180);
        // Red eyes
        g.setColor(new Color(200, 0, 0));
        g.fillOval(cx - 16, cy - 86, 12, 9);
        g.fillOval(cx + 4, cy - 86, 12, 9);
        // Legs
        g.setColor(col);
        g.fillRoundRect(cx - 26, cy - 8, 22, 44, 4, 4);
        g.fillRoundRect(cx + 4, cy - 8, 22, 44, 4, 4);
        // Cape
        g.setColor(new Color(60, 0, 0, 200));
        int[] cx2 = {cx+22, cx+52, cx+44, cx+28};
        int[] cy2 = {cy-62, cy-36, cy+32, cy+10};
        g.fillPolygon(cx2, cy2, 4);
    }
 
    private void drawConquestPreview(Graphics2D g, int cx, int cy, Color col, Color acc) {
        g.setColor(col);
        g.fillRoundRect(cx - 28, cy - 62, 56, 56, 10, 10);
        // Armor lines
        g.setColor(new Color(80, 80, 100));
        g.setStroke(new BasicStroke(2));
        g.drawLine(cx - 20, cy - 55, cx + 20, cy - 55);
        g.drawLine(cx - 20, cy - 40, cx + 20, cy - 40);
        g.setStroke(new BasicStroke(1));
        // Helmet
        g.setColor(new Color(50, 50, 65));
        g.fillOval(cx - 22, cy - 104, 44, 44);
        // Visor
        g.setColor(acc);
        g.fillRect(cx - 18, cy - 88, 36, 8);
        // Glowing eyes
        g.setColor(new Color(255, 80, 80));
        g.fillOval(cx - 14, cy - 87, 10, 6);
        g.fillOval(cx + 4, cy - 87, 10, 6);
        // Scar
        g.setColor(new Color(140, 60, 60));
        g.setStroke(new BasicStroke(2));
        g.drawLine(cx + 8, cy - 100, cx + 18, cy - 82);
        g.setStroke(new BasicStroke(1));
        // Legs
        g.setColor(col);
        g.fillRoundRect(cx - 24, cy - 8, 20, 42, 4, 4);
        g.fillRoundRect(cx + 4, cy - 8, 20, 42, 4, 4);
        // Cape
        g.setColor(new Color(50, 10, 10, 200));
        int[] cx2 = {cx+20, cx+48, cx+40, cx+24};
        int[] cy2 = {cy-60, cy-36, cy+30, cy+8};
        g.fillPolygon(cx2, cy2, 4);
    }
 
    private void drawStatBars(Graphics2D g, int index, int x, int y, int w) {
        String[] labels = {"PWR", "SPD", "HP"};
        int[]    vals   = {statPower[index], statSpeed[index], statHealth[index]};
        Color[]  cols   = {new Color(255, 60, 60), new Color(60, 180, 255), new Color(60, 200, 60)};
 
        g.setFont(new Font("Arial", Font.BOLD, 9));
        int bh = 5, gap = 9;
 
        for (int i = 0; i < 3; i++) {
            int by = y + i * gap;
            g.setColor(new Color(255, 255, 255, 100));
            g.drawString(labels[i], x, by + bh);
            int barX = x + 24;
            int barW = w - 26;
            g.setColor(new Color(30, 30, 40));
            g.fillRoundRect(barX, by, barW, bh, bh, bh);
            g.setColor(cols[i]);
            g.fillRoundRect(barX, by, (int)(barW * vals[i] / 100f), bh, bh, bh);
        }
    }
 
    private void drawCursor(Graphics2D g, int cx, int cy, int cw, int ch, Color col, String label, boolean confirmed) {
        // Corner brackets
        g.setColor(confirmed ? new Color(255, 255, 100) : col);
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int s = 18;
        // TL
        g.drawLine(cx, cy + s, cx, cy); g.drawLine(cx, cy, cx + s, cy);
        // TR
        g.drawLine(cx + cw - s, cy, cx + cw, cy); g.drawLine(cx + cw, cy, cx + cw, cy + s);
        // BL
        g.drawLine(cx, cy + ch - s, cx, cy + ch); g.drawLine(cx, cy + ch, cx + s, cy + ch);
        // BR
        g.drawLine(cx + cw - s, cy + ch, cx + cw, cy + ch); g.drawLine(cx + cw, cy + ch, cx + cw, cy + ch - s);
        g.setStroke(new BasicStroke(1));
 
        // Label tab
        g.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int lw = fm.stringWidth(label) + 8;
        g.setColor(confirmed ? new Color(255, 255, 100) : col);
        g.fillRoundRect(cx + 6, cy + 6, lw, 16, 4, 4);
        g.setColor(Color.BLACK);
        g.drawString(label, cx + 10, cy + 18);
 
        if (confirmed) {
            g.setFont(new Font("Arial", Font.BOLD, 13));
            g.setColor(new Color(255, 255, 100));
            g.drawString("LOCKED", cx + cw / 2 - 28, cy + ch / 2);
        }
    }
 
    private void drawVSDivider(Graphics2D g, int W, int H) {
        // VS text in center
        g.setFont(new Font("Impact", Font.BOLD, 42));
        GradientPaint vg = new GradientPaint(W/2f - 20, H/2f - 30, new Color(255,200,50), W/2f + 20, H/2f + 30, new Color(255,50,0));
        g.setPaint(vg);
        g.drawString("VS", W/2 - 26, 260);
    }
 
    private void drawConfirmPanel(Graphics2D g, int sel, int x, int y, int w, int h, int player, boolean confirmed) {
        Color col = (player == 1) ? new Color(50, 150, 255) : new Color(255, 80, 80);
        g.setColor(new Color(15, 15, 25, 200));
        g.fillRoundRect(x, y, w, h, 8, 8);
        g.setColor(confirmed ? new Color(255, 255, 100) : col);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, w, h, 8, 8);
        g.setStroke(new BasicStroke(1));
 
        g.setFont(new Font("Arial", Font.BOLD, 13));
        String txt = "P" + player + ": " + names[sel] + (confirmed ? " ✓" : "");
        g.setColor(confirmed ? new Color(255, 255, 100) : Color.WHITE);
        g.drawString(txt, x + 10, y + 32);
    }
 
    private void drawBlinkingText(Graphics2D g, String text, int cx, int cy, Color col) {
        if ((tick / 30) % 2 == 0) {
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(col);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, cx - fm.stringWidth(text) / 2, cy);
        }
    }
 
    // ── Input ──────────────────────────────────────────
 
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
 
        if (!p1Confirmed) {
            if (code == KeyEvent.VK_A) p1Selection = (p1Selection + 3) % 4;
            if (code == KeyEvent.VK_D) p1Selection = (p1Selection + 1) % 4;
            if (code == KeyEvent.VK_F) p1Confirmed = true;
        }
 
        if (!p2Confirmed) {
            if (code == KeyEvent.VK_LEFT)  p2Selection = (p2Selection + 3) % 4;
            if (code == KeyEvent.VK_RIGHT) p2Selection = (p2Selection + 1) % 4;
            if (code == KeyEvent.VK_NUMPAD1) p2Confirmed = true;
        }
 
        if (p1Confirmed && p2Confirmed) {
            startFight();
        }
    }
 
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
 
    private void startFight() {
        animTimer.stop();
        GamePanel gp = new GamePanel(p1Selection, p2Selection);
        parentFrame.setContentPane(gp);
        parentFrame.revalidate();
        gp.requestFocusInWindow();
        gp.startGame();
    }
}
 

