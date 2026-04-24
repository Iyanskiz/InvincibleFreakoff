import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class CharacterSelect extends JPanel implements KeyListener, ActionListener {

    private int p1Selection = 0;
    private int p2Selection = 1;
    private boolean p1Confirmed = false;
    private boolean p2Confirmed = false;

    private javax.swing.Timer animTimer;
    private int tick = 0;
    private JFrame parentFrame;

    private final String[] names     = {"MUT. INVINCIBLE", "OMNI-MAN", "THRAGG", "CONQUEST"};
    private final String[] subtitles = {"Mutated Mark Grayson", "Nolan Grayson", "Grand Regent", "The Conqueror"};
    private final Color[]  colors    = {
        new Color(20, 160, 80),
        new Color(180, 0, 0),
        new Color(100, 0, 0),
        new Color(60, 60, 80)
    };
    private final Color[] accents = {
        new Color(180, 255, 120),
        new Color(240, 240, 240),
        new Color(180, 140, 0),
        new Color(200, 50, 50)
    };
    private final int[] statPower  = {100, 90, 95, 88};
    private final int[] statSpeed  = {95, 85, 70, 75};
    private final int[] statHealth = {100, 90, 95, 88};

    // Levitating preview images per character
    private BufferedImage[] previewImages = new BufferedImage[4];

    public CharacterSelect(JFrame frame) {
        this.parentFrame = frame;
        setPreferredSize(new Dimension(1100, 650));
        setFocusable(true);
        addKeyListener(this);

        loadPreviewImages();

        animTimer = new javax.swing.Timer(16, this);
        animTimer.start();
    }

    private void loadPreviewImages() {
        // Index matches: 0=Invincible, 1=OmniMan, 2=Thragg, 3=Conquest
        String[] filenames = {
            "InvincibleLevitating.png",  // Invincible
            "OmniLevitating.png",        // OmniMan
            "ThaggLevitating.png",       // Thragg  (check exact filename spelling)
            "ConquestLevitating.png"     // Conquest
        };

        String[] paths = {"imgs/", "src/imgs/", "../imgs/", ""};

        for (int i = 0; i < filenames.length; i++) {
            if (filenames[i] == null) continue;
            for (String base : paths) {
                File f = new File(base + filenames[i]);
                if (f.exists()) {
                    try {
                        previewImages[i] = ImageIO.read(f);
                        System.out.println("Loaded preview: " + f.getPath());
                    } catch (IOException e) {
                        System.err.println("Failed to load: " + f.getPath());
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) { tick++; repaint(); }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int W = getWidth(), H = getHeight();

        // Background
        g.setColor(new Color(8, 8, 14));
        g.fillRect(0, 0, W, H);
        g.setColor(new Color(255, 60, 60, 18));
        for (int i = 0; i < W; i += 50) g.drawLine(i, 0, i, H);
        for (int i = 0; i < H; i += 50) g.drawLine(0, i, W, i);

        RadialGradientPaint radial = new RadialGradientPaint(
            W / 2f, H / 2f, W * 0.55f,
            new float[]{0f, 1f},
            new Color[]{new Color(160, 0, 0, 40), new Color(0, 0, 0, 0)}
        );
        g.setPaint(radial);
        g.fillRect(0, 0, W, H);

        drawTitle(g, W);

        g.setFont(new Font("Arial", Font.BOLD, 13));
        g.setColor(new Color(255, 255, 255, 120));
        g.drawString("P1: A/D to select  |  F to confirm", 30, H - 50);
        g.drawString("P2: ◄/► to select  |  NUMPAD 1 to confirm", W - 330, H - 50);

        if (!p1Confirmed || !p2Confirmed)
            drawBlinkingText(g, "PRESS CONFIRM TO LOCK IN", W / 2, H - 30, new Color(255, 200, 50));

        int cardW = 210, cardH = 310;
        int totalW = 4 * cardW + 3 * 16;
        int startX = (W - totalW) / 2;
        int cardY  = 120;

        for (int i = 0; i < 4; i++) {
            drawCard(g, i, startX + i * (cardW + 16), cardY, cardW, cardH);
        }

        drawVSDivider(g, W);
        drawConfirmPanel(g, p1Selection, 30, H - 110, 200, 50, 1, p1Confirmed);
        drawConfirmPanel(g, p2Selection, W - 230, H - 110, 200, 50, 2, p2Confirmed);
    }

    private void drawTitle(Graphics2D g, int W) {
        g.setFont(new Font("Impact", Font.BOLD, 58));
        g.setColor(new Color(0, 0, 0, 180));
        g.drawString("INVINCIBLE  FREAKOFF", W / 2 - 297, 75);
        GradientPaint gp = new GradientPaint(0, 20, new Color(255, 220, 50), 0, 72, new Color(255, 60, 0));
        g.setPaint(gp);
        g.drawString("INVINCIBLE  FREAKOFF", W / 2 - 298, 74);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(new Color(255, 255, 255, 80));
        String sub = "— SELECT YOUR FIGHTER —";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(sub, (W - fm.stringWidth(sub)) / 2, 100);
    }

    private void drawCard(Graphics2D g, int index, int cx, int cy, int cw, int ch) {
        boolean p1Here   = (p1Selection == index);
        boolean p2Here   = (p2Selection == index);
        Color base = colors[index];
        Color acc  = accents[index];
        float pulse = (float)(Math.sin(tick * 0.08) * 0.5 + 0.5);

        // Card bg
        g.setColor(new Color(15, 15, 25));
        g.fillRoundRect(cx, cy, cw, ch, 12, 12);
        GradientPaint grad = new GradientPaint(cx, cy,
            new Color(base.getRed(), base.getGreen(), base.getBlue(), 60),
            cx, cy + ch, new Color(0, 0, 0, 0));
        g.setPaint(grad);
        g.fillRoundRect(cx, cy, cw, ch, 12, 12);

        // Border
        Color borderCol;
        if ((p1Here && p1Confirmed) || (p2Here && p2Confirmed)) borderCol = new Color(255, 255, 100);
        else if (p1Here || p2Here) {
            int alpha = (int)(150 + 105 * pulse);
            borderCol = new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), alpha);
        } else borderCol = new Color(60, 60, 80);

        g.setColor(borderCol);
        g.setStroke(new BasicStroke((p1Here || p2Here) ? 3 : 1.5f));
        g.drawRoundRect(cx, cy, cw, ch, 12, 12);
        g.setStroke(new BasicStroke(1));

        // ── Character preview area ─────────────────────────
        int previewW = cw - 20;
        int previewH = 200;
        int previewX = cx + 10;
        int previewY = cy + 8;

        // Subtle glow behind character
        RadialGradientPaint glow = new RadialGradientPaint(
            cx + cw / 2f, previewY + previewH / 2f, previewH * 0.6f,
            new float[]{0f, 1f},
            new Color[]{new Color(base.getRed(), base.getGreen(), base.getBlue(), 40), new Color(0,0,0,0)}
        );
        g.setPaint(glow);
        g.fillRect(previewX, previewY, previewW, previewH);

        // Float bob animation
        float bob = (float)(Math.sin(tick * 0.07 + index * 1.2) * 5);

        if (previewImages[index] != null) {
            // Draw real image centered in preview area
            int iw = previewImages[index].getWidth();
            int ih = previewImages[index].getHeight();
            float scale = Math.min((float)previewW / iw, (float)previewH / ih) * 0.95f;
            int dw = (int)(iw * scale);
            int dh = (int)(ih * scale);
            int dx = previewX + (previewW - dw) / 2;
            int dy = (int)(previewY + (previewH - dh) / 2 + bob);
            g.drawImage(previewImages[index], dx, dy, dw, dh, null);
        } else {
            // Shape fallback for characters without images yet
            drawShapePreview(g, index, cx + cw / 2, (int)(previewY + previewH / 2 + bob), p1Here || p2Here);
        }

        // Name
        g.setFont(new Font("Impact", Font.PLAIN, 24));
        GradientPaint nameGrad = new GradientPaint(cx, cy + 218, acc, cx, cy + 240, acc.darker());
        g.setPaint(nameGrad);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(names[index], cx + (cw - fm.stringWidth(names[index])) / 2, cy + 228);

        // Subtitle
        g.setFont(new Font("Arial", Font.ITALIC, 11));
        g.setColor(new Color(200, 200, 200, 160));
        fm = g.getFontMetrics();
        g.drawString(subtitles[index], cx + (cw - fm.stringWidth(subtitles[index])) / 2, cy + 244);

        // Stats
        drawStatBars(g, index, cx + 10, cy + 255, cw - 20);

        // Cursors
        if (p1Here) drawCursor(g, cx,     cy,     cw,     ch,     new Color(50,150,255), "P1", p1Confirmed);
        if (p2Here) drawCursor(g, cx + 3, cy + 3, cw - 6, ch - 6, new Color(255,80,80),  "P2", p2Confirmed);
    }

    private void drawShapePreview(Graphics2D g, int index, int cx, int cy, boolean active) {
        float scale = active ? 1.1f : 1.0f;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(cx, cy);
        g2.scale(scale, scale);
        g2.translate(-cx, -cy);
        Color col = colors[index];
        Color acc = accents[index];
        switch (index) {
            case 0: drawInvincibleShape(g2, cx, cy, col, acc); break;
            case 1: drawOmniManShape(g2, cx, cy, col, acc);    break;
        }
        g2.dispose();
    }

    private void drawInvincibleShape(Graphics2D g, int cx, int cy, Color col, Color acc) {
        g.setColor(col);
        g.fillRoundRect(cx - 22, cy - 30, 44, 50, 8, 8);
        g.setColor(new Color(255, 210, 170));
        g.fillOval(cx - 16, cy - 65, 32, 32);
        g.setColor(col);
        g.fillArc(cx - 16, cy - 65, 32, 22, 0, 180);
        g.fillRect(cx - 16, cy - 48, 32, 10);
        g.setColor(acc);
        g.fillOval(cx - 12, cy - 47, 8, 6);
        g.fillOval(cx + 4, cy - 47, 8, 6);
        g.setColor(col);
        g.fillRoundRect(cx - 20, cy + 18, 16, 38, 4, 4);
        g.fillRoundRect(cx + 4,  cy + 18, 16, 38, 4, 4);
        g.setColor(new Color(200, 220, 255, 150));
        int[] cxp = {cx+14, cx+38, cx+30, cx+18};
        int[] cyp = {cy-28, cy-8, cy+50, cy+28};
        g.fillPolygon(cxp, cyp, 4);
        g.setColor(acc);
        g.fillRect(cx - 14, cy - 26, 8, 30);
    }

    private void drawOmniManShape(Graphics2D g, int cx, int cy, Color col, Color acc) {
        g.setColor(col);
        g.fillRoundRect(cx - 26, cy - 32, 52, 54, 8, 8);
        g.setColor(new Color(220, 175, 140));
        g.fillOval(cx - 20, cy - 70, 40, 40);
        g.setColor(new Color(20, 20, 20));
        g.fillArc(cx - 20, cy - 70, 40, 26, 0, 180);
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(cx - 14, cy - 42, 12, 8, 0, -180);
        g.drawArc(cx + 2,  cy - 42, 12, 8, 0, -180);
        g.setStroke(new BasicStroke(1));
        g.setColor(col);
        g.fillRoundRect(cx - 24, cy + 20, 20, 40, 4, 4);
        g.fillRoundRect(cx + 4,  cy + 20, 20, 40, 4, 4);
        g.setColor(new Color(140, 0, 0, 200));
        int[] cxp = {cx+18, cx+44, cx+36, cx+22};
        int[] cyp = {cy-30, cy-8, cy+58, cy+36};
        g.fillPolygon(cxp, cyp, 4);
        g.setColor(acc);
        g.setStroke(new BasicStroke(3));
        g.drawLine(cx - 14, cy - 28, cx, cy - 4);
        g.drawLine(cx, cy - 4, cx + 14, cy - 28);
        g.setStroke(new BasicStroke(1));
    }

    private void drawStatBars(Graphics2D g, int index, int x, int y, int w) {
        String[] labels = {"PWR", "SPD", "HP"};
        int[]    vals   = {statPower[index], statSpeed[index], statHealth[index]};
        Color[]  cols   = {new Color(255,60,60), new Color(60,180,255), new Color(60,200,60)};
        g.setFont(new Font("Arial", Font.BOLD, 9));
        int bh = 5, gap = 10;
        for (int i = 0; i < 3; i++) {
            int by = y + i * gap;
            g.setColor(new Color(255, 255, 255, 100));
            g.drawString(labels[i], x, by + bh);
            int barX = x + 26, barW = w - 28;
            g.setColor(new Color(30, 30, 40));
            g.fillRoundRect(barX, by, barW, bh, bh, bh);
            g.setColor(cols[i]);
            g.fillRoundRect(barX, by, (int)(barW * vals[i] / 100f), bh, bh, bh);
        }
    }

    private void drawCursor(Graphics2D g, int cx, int cy, int cw, int ch, Color col, String label, boolean confirmed) {
        g.setColor(confirmed ? new Color(255,255,100) : col);
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int s = 18;
        g.drawLine(cx, cy+s, cx, cy); g.drawLine(cx, cy, cx+s, cy);
        g.drawLine(cx+cw-s, cy, cx+cw, cy); g.drawLine(cx+cw, cy, cx+cw, cy+s);
        g.drawLine(cx, cy+ch-s, cx, cy+ch); g.drawLine(cx, cy+ch, cx+s, cy+ch);
        g.drawLine(cx+cw-s, cy+ch, cx+cw, cy+ch); g.drawLine(cx+cw, cy+ch, cx+cw, cy+ch-s);
        g.setStroke(new BasicStroke(1));
        g.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int lw = fm.stringWidth(label) + 8;
        g.setColor(confirmed ? new Color(255,255,100) : col);
        g.fillRoundRect(cx+6, cy+6, lw, 16, 4, 4);
        g.setColor(Color.BLACK);
        g.drawString(label, cx+10, cy+18);
        if (confirmed) {
            g.setFont(new Font("Arial", Font.BOLD, 13));
            g.setColor(new Color(255,255,100));
            g.drawString("LOCKED", cx + cw/2 - 28, cy + ch/2);
        }
    }

    private void drawVSDivider(Graphics2D g, int W) {
        g.setFont(new Font("Impact", Font.BOLD, 42));
        GradientPaint vg = new GradientPaint(W/2f-20, 220, new Color(255,200,50), W/2f+20, 260, new Color(255,50,0));
        g.setPaint(vg);
        g.drawString("VS", W/2 - 26, 260);
    }

    private void drawConfirmPanel(Graphics2D g, int sel, int x, int y, int w, int h, int player, boolean confirmed) {
        Color col = (player == 1) ? new Color(50,150,255) : new Color(255,80,80);
        g.setColor(new Color(15,15,25,200));
        g.fillRoundRect(x, y, w, h, 8, 8);
        g.setColor(confirmed ? new Color(255,255,100) : col);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, w, h, 8, 8);
        g.setStroke(new BasicStroke(1));
        g.setFont(new Font("Arial", Font.BOLD, 13));
        g.setColor(confirmed ? new Color(255,255,100) : Color.WHITE);
        g.drawString("P" + player + ": " + names[sel] + (confirmed ? " ✓" : ""), x+10, y+32);
    }

    private void drawBlinkingText(Graphics2D g, String text, int cx, int cy, Color col) {
        if ((tick / 30) % 2 == 0) {
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(col);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, cx - fm.stringWidth(text)/2, cy);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (!p1Confirmed) {
            if (code == KeyEvent.VK_A) p1Selection = (p1Selection + 3) % 4;
            if (code == KeyEvent.VK_D) p1Selection = (p1Selection + 1) % 4;
            if (code == KeyEvent.VK_F) p1Confirmed = true;
        }
        if (!p2Confirmed) {
            if (code == KeyEvent.VK_LEFT)    p2Selection = (p2Selection + 3) % 4;
            if (code == KeyEvent.VK_RIGHT)   p2Selection = (p2Selection + 1) % 4;
            if (code == KeyEvent.VK_NUMPAD1) p2Confirmed = true;
        }
        if (p1Confirmed && p2Confirmed) startFight();
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