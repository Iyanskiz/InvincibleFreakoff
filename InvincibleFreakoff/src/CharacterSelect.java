import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class CharacterSelect extends JPanel implements KeyListener, ActionListener {

    // gameMode: 0=1v1, 1=2v2, 2=1vBot, 3=2v2Bot
    private int gameMode;
    private int[] p1Selections = {0, 1};
    private int[] p2Selections = {2, 3};
    private boolean[] p1Confirmed, p2Confirmed;
    private int p1Slot = 0, p2Slot = 0;
    private int p1Cursor = 0, p2Cursor = 2;

    // Bot settings
    private int botDifficulty = 1;
    private int[] botSelections = {2, 3};
    private boolean[] botConfirmed;
    private int botSlot = 0, botCursor = 2;
    private boolean selectingDifficulty = false;
    private boolean difficultyDone = false;

    private int phase = 0;

    private javax.swing.Timer animTimer;
    private int tick = 0;
    private JFrame parentFrame;

    private static final int NUM_CHARS = 6;

    private static final String[] NAMES = {"INVINCIBLE", "OMNI-MAN", "THRAGG", "CONQUEST", "ANISSA", "TECH JACKET"};
    private static final String[] SUBTITLES = {"Mark Grayson", "Nolan Grayson", "Grand Regent", "The Conqueror", "Viltrumite Warrior", "Zachary Thompson"};
    private static final Color[] COLORS = {
            new Color(20, 160, 80),
            new Color(180, 0, 0),
            new Color(100, 0, 0),
            new Color(60, 60, 80),
            new Color(140, 30, 180),
            new Color(30, 90, 200)
    };
    private static final Color[] ACCENTS = {
            new Color(180, 255, 120),
            new Color(240, 240, 240),
            new Color(180, 140, 0),
            new Color(200, 50, 50),
            new Color(220, 180, 255),
            new Color(0, 220, 255)
    };
    private static final int[] STAT_PWR = {100, 90, 95, 88, 92, 85};
    private static final int[] STAT_SPD = {95, 85, 70, 75, 82, 80};
    private static final int[] STAT_HP = {100, 90, 95, 88, 85, 90};
    private static final String[] DIFF_NAMES = {"EASY", "MEDIUM", "HARD", "NIGHTMARE"};
    private static final Color[] DIFF_COLORS = {new Color(80, 200, 80), new Color(255, 200, 50), new Color(255, 100, 50), new Color(200, 0, 255)};
    private static final String[] DIFF_DESC = {
            "Smarter spacing — blocks and punishes openings",
            "Aggressive — combos, jumps, and special usage",
            "Fast pressure — counters, heavies, and platform play",
            "Relentless — blocks windups, spams offense, reads you"
    };

    private BufferedImage[] previewImages = new BufferedImage[NUM_CHARS];

    public CharacterSelect(JFrame frame, int gameMode) {
        this.parentFrame = frame;
        this.gameMode = gameMode;
        int slots = (gameMode == 1 || gameMode == 3) ? 2 : 1;
        p1Confirmed = new boolean[slots];
        p2Confirmed = new boolean[slots];
        botConfirmed = new boolean[slots];
        phase = (gameMode == 2 || gameMode == 3) ? 0 : 1;
        setFocusable(true);
        addKeyListener(this);
        loadPreviews();
        animTimer = new javax.swing.Timer(16, this);
        animTimer.start();
    }

    private void loadPreviews() {
        String[] files = {
                "InvincibleLevitating.png", "OmniLevitating.png", "ThaggLevitating.png",
                "ConquestLevitating.png", "AnissaLevitating.png", "TechLevitating.png"
        };
        String[] paths = {"imgs/", "src/imgs/", "../imgs/", ""};
        for (int i = 0; i < files.length; i++) {
            for (String b : paths) {
                File f = new File(b + files[i]);
                if (f.exists()) {
                    try {
                        previewImages[i] = ImageIO.read(f);
                    } catch (IOException e) {
                    }
                    break;
                }
            }
        }
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
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int W = getWidth(), H = getHeight();

        g.setColor(new Color(4, 4, 10));
        g.fillRect(0, 0, W, H);
        g.setColor(new Color(255, 80, 80, 7));
        for (int i = 0; i < W; i += 70) g.drawLine(i, 0, i, H);
        for (int i = 0; i < H; i += 70) g.drawLine(0, i, W, i);
        RadialGradientPaint r = new RadialGradientPaint(W / 2f, H / 2f, W * 0.55f,
                new float[]{0f, 1f}, new Color[]{new Color(180, 0, 0, 22), new Color(0, 0, 0, 0)});
        g.setPaint(r);
        g.fillRect(0, 0, W, H);

        if (phase == 0) drawDifficultyPhase(g, W, H);
        else drawCharacterPhase(g, W, H);
    }

    // ── Phase 0: Difficulty ───────────────────────────────────────────────────

    private void drawDifficultyPhase(Graphics2D g, int W, int H) {
        botDifficulty = Math.min(3, Math.max(0, botDifficulty));
        g.setFont(new Font("Impact", Font.PLAIN, 52));
        FontMetrics fm = g.getFontMetrics();
        String t = "SELECT DIFFICULTY";
        g.setColor(new Color(0, 0, 0, 200));
        g.drawString(t, W / 2 - fm.stringWidth(t) / 2 + 3, 68);
        GradientPaint gp = new GradientPaint(0, 28, new Color(255, 225, 60), 0, 68, new Color(255, 55, 0));
        g.setPaint(gp);
        g.drawString(t, W / 2 - fm.stringWidth(t) / 2, 66);

        String badge = gameMode == 2 ? "1 VS BOT" : "2 VS 2 BOT";
        g.setFont(new Font("Arial", Font.BOLD, 14));
        fm = g.getFontMetrics();
        int bw = fm.stringWidth(badge) + 24, bh = 24, bx = W / 2 - bw / 2, by = 78;
        g.setColor(new Color(180, 60, 255, 50));
        g.fillRoundRect(bx, by, bw, bh, 10, 10);
        g.setColor(new Color(200, 100, 255, 200));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(bx, by, bw, bh, 10, 10);
        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(230, 180, 255));
        g.drawString(badge, W / 2 - fm.stringWidth(badge) / 2, by + 16);

        g.setFont(new Font("Arial", Font.BOLD, 13));
        fm = g.getFontMetrics();
        g.setColor(new Color(255, 255, 255, 120));
        String nav = "← → or A/D to select   |   ENTER or F to confirm";
        g.drawString(nav, W / 2 - fm.stringWidth(nav) / 2, 117);

        int cw = 248, ch = 172, gap = 32, totalW = 4 * cw + 3 * gap, sx = (W - totalW) / 2, sy = 136;
        for (int i = 0; i < 4; i++) drawDiffCard(g, i, sx + i * (cw + gap), sy, cw, ch);

        g.setFont(new Font("Arial", Font.ITALIC, 15));
        fm = g.getFontMetrics();
        g.setColor(new Color(240, 240, 240, 210));
        String desc = DIFF_DESC[botDifficulty];
        g.drawString(desc, W / 2 - fm.stringWidth(desc) / 2, sy + ch + 42);
        drawBotCharSelect(g, W, sy + ch + 62, H);
    }

    private void drawDiffCard(Graphics2D g, int index, int cx, int cy, int cw, int ch) {
        boolean sel = (index == botDifficulty);
        Color col = DIFF_COLORS[index];
        float pulse = (float) (Math.sin(tick * 0.08) * 0.5 + 0.5);
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(cx + 4, cy + 4, cw, ch, 14, 14);
        GradientPaint bg = new GradientPaint(cx, cy,
                new Color(col.getRed() / 6, col.getGreen() / 6, col.getBlue() / 6, 230),
                cx, cy + ch, new Color(6, 6, 16, 230));
        g.setPaint(bg);
        g.fillRoundRect(cx, cy, cw, ch, 14, 14);
        if (sel) {
            int a = Math.max(0, Math.min(255, (int) (150 + 105 * pulse)));
            g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), a));
            g.setStroke(new BasicStroke(3));
        } else {
            g.setColor(new Color(70, 70, 90, 200));
            g.setStroke(new BasicStroke(1.5f));
        }
        g.drawRoundRect(cx, cy, cw, ch, 14, 14);
        g.setStroke(new BasicStroke(1));
        String[] icons = {"😊", "⚔", "🔥", "💀"};
        g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, sel ? 36 : 28));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(col);
        g.drawString(icons[index], cx + (cw - fm.stringWidth(icons[index])) / 2, cy + 50);
        g.setFont(new Font("Impact", Font.PLAIN, sel ? 30 : 24));
        fm = g.getFontMetrics();
        if (sel) {
            GradientPaint tp = new GradientPaint(cx, cy + 56, col, cx, cy + 88, col.darker());
            g.setPaint(tp);
        } else g.setColor(new Color(180, 180, 180));
        g.drawString(DIFF_NAMES[index], cx + (cw - fm.stringWidth(DIFF_NAMES[index])) / 2, cy + 88);
        if (sel) {
            g.setFont(new Font("Arial", Font.BOLD, 11));
            fm = g.getFontMetrics();
            g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 200));
            g.drawString("SELECTED", cx + (cw - fm.stringWidth("SELECTED")) / 2, cy + 108);
            g.setColor(col);
            g.fillPolygon(new int[]{cx + cw / 2 - 8, cx + cw / 2 + 8, cx + cw / 2}, new int[]{cy + ch + 7, cy + ch + 7, cy + ch + 16}, 3);
        }
    }

    private void drawBotCharSelect(Graphics2D g, int W, int y, int H) {
        int slots = (gameMode == 3) ? 2 : 1;
        g.setFont(new Font("Impact", Font.PLAIN, 24));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(255, 255, 255, 200));
        String hdr = "SELECT BOT CHARACTER" + (slots > 1 ? "S" : "");
        g.drawString(hdr, W / 2 - fm.stringWidth(hdr) / 2, y + 26);
        
        int cw = 170, ch = 100, gap = 14, slant = 45; // Increased slant
        int totalW = NUM_CHARS * cw + slant + (NUM_CHARS - 1) * gap;
        int sx = (W - totalW) / 2;

        for (int i = 0; i < NUM_CHARS; i++) {
            int cx = sx + i * (cw + gap), cy = y + 36;
            boolean sel = (botCursor == i);
            boolean conf0 = (slots >= 1 && botSelections[0] == i && botConfirmed[0]);
            boolean conf1 = (slots >= 2 && botSelections[1] == i && botConfirmed[1]);
            Color col = COLORS[i], acc = ACCENTS[i];
            float pulse = (float) (Math.sin(tick * 0.08) * 0.5 + 0.5);

            int[] px = {cx + slant, cx + cw + slant, cx + cw, cx};
            int[] py = {cy, cy, cy + ch, cy + ch};
            Polygon cardPoly = new Polygon(px, py, 4);

            g.setColor(new Color(0, 0, 0, 100));
            g.translate(3, 3);
            g.fillPolygon(cardPoly);
            g.translate(-3, -3);

            GradientPaint bg = new GradientPaint(cx, cy,
                    new Color(col.getRed() / 5, col.getGreen() / 5, col.getBlue() / 5, 220),
                    cx, cy + ch, new Color(6, 6, 16, 220));
            g.setPaint(bg);
            g.fillPolygon(cardPoly);

            int borderAlpha = sel ? Math.max(0, Math.min(255, (int) (145 + 110 * pulse))) : 80;
            g.setColor(sel ? new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), borderAlpha) : new Color(70, 70, 90, 180));
            g.setStroke(new BasicStroke(sel ? 2.5f : 1.2f));
            g.drawPolygon(cardPoly);
            g.setStroke(new BasicStroke(1));

            if (previewImages[i] != null) {
                int ih = Math.min(75, (int) (previewImages[i].getHeight() * (62f / previewImages[i].getWidth())));
                int iw = (int) (previewImages[i].getWidth() * ((float) ih / previewImages[i].getHeight()));
                
                float tY = (float) (cy + 4 + ih / 2f - cy) / ch;
                int imgCenter = (int) (cx + cw / 2.0 + slant * (1.0 - tY));
                g.drawImage(previewImages[i], imgCenter - iw / 2, cy + 4, iw, Math.min(ih, 66), null);
            }

            g.setFont(new Font("Impact", Font.PLAIN, sel ? 15 : 13));
            fm = g.getFontMetrics();
            g.setColor(sel ? acc : new Color(190, 190, 190));
            int textY = cy + ch - 8;
            float textT = (float) (textY - cy) / ch;
            int textCenter = (int) (cx + cw / 2.0 + slant * (1.0 - textT));
            g.drawString(NAMES[i], textCenter - fm.stringWidth(NAMES[i]) / 2, textY);

            if (conf0) {
                g.setFont(new Font("Arial", Font.BOLD, 8));
                fm = g.getFontMetrics();
                String tag = "BOT" + (slots > 1 ? "-1" : "");
                g.setColor(new Color(210, 90, 255, 220));
                g.fillRoundRect(cx + slant + 2, cy + 2, fm.stringWidth(tag) + 6, 13, 3, 3);
                g.setColor(Color.WHITE);
                g.drawString(tag, cx + slant + 5, cy + 12);
            }
            if (conf1) {
                g.setFont(new Font("Arial", Font.BOLD, 8));
                fm = g.getFontMetrics();
                g.setColor(new Color(210, 90, 255, 220));
                g.fillRoundRect(cx + cw + slant - fm.stringWidth("BOT-2") - 8, cy + 2, fm.stringWidth("BOT-2") + 6, 13, 3, 3);
                g.setColor(Color.WHITE);
                g.drawString("BOT-2", cx + cw + slant - fm.stringWidth("BOT-2") - 5, cy + 12);
            }
        }

        boolean allBotDone = true;
        for (boolean b : botConfirmed) if (!b) allBotDone = false;
        int hintY = H - 28;
        
        if (allBotDone && difficultyDone) {
            if ((tick / 30) % 2 == 0) {
                g.setFont(new Font("Impact", Font.PLAIN, 22));
                fm = g.getFontMetrics();
                GradientPaint pg = new GradientPaint(0, hintY - 18, new Color(255, 220, 50), 0, hintY, new Color(255, 80, 0));
                g.setPaint(pg);
                String s = "PRESS ENTER TO CONTINUE";
                g.drawString(s, W / 2 - fm.stringWidth(s) / 2, hintY);
            }
        } else {
            g.setFont(new Font("Arial", Font.BOLD, 13));
            fm = g.getFontMetrics();
            g.setColor(new Color(255, 255, 255, 120));
            String hint = !difficultyDone
                    ? "ENTER = confirm difficulty   |   ← / → = choose difficulty"
                    : "← / → = choose bot character   |   ENTER = confirm bot";
            g.drawString(hint, W / 2 - fm.stringWidth(hint) / 2, hintY);
        }
    }

    // ── Phase 1: Character selection ──────────────────────────────────────────

    private void drawCharacterPhase(Graphics2D g, int W, int H) {
        // Title
        g.setFont(new Font("Impact", Font.PLAIN, 50));
        FontMetrics fm = g.getFontMetrics();
        String t = "SELECT YOUR FIGHTER";
        g.setColor(new Color(0, 0, 0, 200));
        g.drawString(t, W / 2 - fm.stringWidth(t) / 2 + 3, 58);
        GradientPaint gp = new GradientPaint(0, 22, new Color(255, 225, 60), 0, 58, new Color(255, 55, 0));
        g.setPaint(gp);
        g.drawString(t, W / 2 - fm.stringWidth(t) / 2, 56);

        drawModeTag(g, W);

        // Cards fill nearly the whole screen now (no bottom boxes)
        int cardTopY = 115;
        int cardBotY = H - 70; // Adjusted upwards to leave space for bottom text
        int ch = Math.max(300, cardBotY - cardTopY); 

        // Wider gap, wider cards, bigger slant
        int gap = 20; 
        int slant = 85; // Increased slant drastically
        int totalAvail = W - 60; 
        int cw = Math.min(320, (totalAvail - slant - (NUM_CHARS - 1) * gap) / NUM_CHARS);
        
        // Accurate total width accounting for slant
        int totalW = NUM_CHARS * cw + slant + (NUM_CHARS - 1) * gap;
        int sx = (W - totalW) / 2;

        for (int i = 0; i < NUM_CHARS; i++) {
            drawCharCard(g, i, sx + i * (cw + gap), cardTopY, cw, ch, slant);
        }

        // Keep the fight prompt and keys floating cleanly at the bottom without boxes
        boolean ready = allP1Done() && (gameMode == 2 || gameMode == 3 || allP2Done());
        if (ready) {
            if ((tick / 30) % 2 == 0) {
                g.setFont(new Font("Impact", Font.PLAIN, 32));
                fm = g.getFontMetrics();
                GradientPaint pg = new GradientPaint(0, H - 55, new Color(255, 220, 50), 0, H - 23, new Color(255, 80, 0));
                g.setPaint(pg);
                String s = "PRESS ENTER TO FIGHT!";
                g.drawString(s, W / 2 - fm.stringWidth(s) / 2, H - 25);
            }
        } else {
            // Player controls shown here while selecting
            g.setFont(new Font("Arial", Font.BOLD, 13));
            fm = g.getFontMetrics();
            g.setColor(new Color(255, 255, 255, 180));
            
            String p1Ctrl = "P1: [ A / D ] Move   [ F ] Select";
            String p2Ctrl = "P2: [ ← / → ] Move   [ NUMPAD 1 ] Select";
            String infoText = (gameMode == 2 || gameMode == 3) ? p1Ctrl : p1Ctrl + "      |      " + p2Ctrl;
            
            g.drawString(infoText, W / 2 - fm.stringWidth(infoText) / 2, H - 25);
        }
    }

    private void drawModeTag(Graphics2D g, int W) {
        String[] tags = {"1V1", "2V2", "VS BOT", "2V2 BOT"};
        Color[] tc = {new Color(255, 200, 50), new Color(50, 200, 255), new Color(255, 80, 80), new Color(210, 90, 255)};
        String badge = tags[gameMode];
        Color bc = tc[gameMode];
        g.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int bw = fm.stringWidth(badge) + 26, bh = 24, bx = W / 2 - bw / 2, by = 64;
        g.setColor(new Color(bc.getRed(), bc.getGreen(), bc.getBlue(), 45));
        g.fillRoundRect(bx, by, bw, bh, 10, 10);
        g.setColor(new Color(bc.getRed(), bc.getGreen(), bc.getBlue(), 200));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(bx, by, bw, bh, 10, 10);
        g.setStroke(new BasicStroke(1));
        g.setColor(bc);
        g.drawString(badge, W / 2 - fm.stringWidth(badge) / 2, by + 17);
        if (gameMode == 2 || gameMode == 3) {
            g.setFont(new Font("Arial", Font.BOLD, 12));
            fm = g.getFontMetrics();
            g.setColor(DIFF_COLORS[botDifficulty]);
            String ds = "Difficulty: " + DIFF_NAMES[botDifficulty];
            g.drawString(ds, W / 2 - fm.stringWidth(ds) / 2, 96);
        }
    }

    private void drawCharCard(Graphics2D g, int index, int cx, int cy, int cw, int ch, int slant) {
        boolean p1h = (p1Cursor == index), p2h = (gameMode != 2 && gameMode != 3 && p2Cursor == index);
        Color base = COLORS[index], acc = ACCENTS[index];
        float pulse = (float) (Math.sin(tick * 0.08) * 0.5 + 0.5);

        // ── Shape definition (Slanted Parallelogram) ──
        int[] px = {cx + slant, cx + cw + slant, cx + cw, cx};
        int[] py = {cy, cy, cy + ch, cy + ch};
        Polygon cardPoly = new Polygon(px, py, 4);

        // Shadow
        g.setColor(new Color(0, 0, 0, 110));
        g.translate(5, 5);
        g.fillPolygon(cardPoly);
        g.translate(-5, -5);

        // Background gradient
        GradientPaint bg = new GradientPaint(cx, cy,
                new Color(base.getRed() / 5, base.getGreen() / 5, base.getBlue() / 5, 230),
                cx, cy + ch, new Color(7, 7, 18, 230));
        g.setPaint(bg);
        g.fillPolygon(cardPoly);

        // Diagonal stripes (clipped to card shape)
        Shape oldClip = g.getClip();
        g.setClip(cardPoly);

        g.setColor(new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), 14));
        g.setStroke(new BasicStroke(1.2f));
        int spacing = 22;
        for (int d = -ch; d < cw + ch + slant * 2; d += spacing) {
            g.drawLine(cx - slant + d, cy, cx - slant + d + ch, cy + ch);
        }
        g.setColor(new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), 7));
        for (int d = -ch; d < cw + ch + slant * 2; d += spacing * 2) {
            g.drawLine(cx + cw + slant * 2 - d, cy, cx + cw + slant * 2 - d - ch, cy + ch);
        }

        g.setStroke(new BasicStroke(1));
        g.setClip(oldClip);

        // Border
        Color bc;
        if (p1h || p2h) {
            int a = Math.max(0, Math.min(255, (int) (160 + 95 * pulse)));
            bc = new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), a);
            g.setStroke(new BasicStroke(3));
        } else {
            bc = new Color(75, 75, 95);
            g.setStroke(new BasicStroke(1.5f));
        }
        g.setColor(bc);
        g.drawPolygon(cardPoly);
        g.setStroke(new BasicStroke(1));

        // Preview image
        int previewH = Math.max(120, ch - 100);
        float bob = (float) (Math.sin(tick * 0.06 + index * 1.2) * 5);

        if (previewImages[index] != null) {
            BufferedImage img = previewImages[index];
            float scale = Math.min((float) (cw - 16) / img.getWidth(), (float) (previewH) / img.getHeight()) * 0.92f;
            int dw = (int) (img.getWidth() * scale), dh = (int) (img.getHeight() * scale);

            float tImg = (float) (previewH / 2f + 8) / ch;
            int centerImgX = (int) (cx + cw / 2.0 + slant * (1.0 - tImg));

            RadialGradientPaint gl = new RadialGradientPaint(centerImgX, cy + previewH / 2f + 8, previewH * 0.55f,
                    new float[]{0f, 1f}, new Color[]{new Color(base.getRed(), base.getGreen(), base.getBlue(), 28), new Color(0, 0, 0, 0)});
            g.setPaint(gl);
            
            Shape oldGlowClip = g.getClip();
            g.setClip(cardPoly);
            g.fillRect(cx - slant, cy, cw + slant * 2, previewH + 16);
            g.setClip(oldGlowClip);

            int imgY = (int) (cy + 8 + (previewH - dh) / 2 + bob);
            float tY = (float) (imgY + dh / 2 - cy) / ch;
            int imgCenter = (int) (cx + cw / 2.0 + slant * (1.0 - tY));
            g.drawImage(img, imgCenter - dw / 2, imgY, dw, dh, null);
        }

        // Divider
        int divY = cy + previewH + 16;
        float divT = (float) (divY - cy) / ch;
        int divCenter = (int) (cx + cw / 2.0 + slant * (1.0 - divT));
        g.setColor(new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), 70));
        g.drawLine(divCenter - cw / 2 + 14, divY, divCenter + cw / 2 - 14, divY);

        // Name
        g.setFont(new Font("Impact", Font.PLAIN, cw < 160 ? 18 : 22));
        GradientPaint ng = new GradientPaint(cx, divY + 4, acc, cx, divY + 26, acc.darker());
        g.setPaint(ng);
        FontMetrics fm = g.getFontMetrics();
        int nameY = divY + 22;
        float nameT = (float) (nameY - cy) / ch;
        int nameCenter = (int) (cx + cw / 2.0 + slant * (1.0 - nameT));
        g.drawString(NAMES[index], nameCenter - fm.stringWidth(NAMES[index]) / 2, nameY);

        // Subtitle
        g.setFont(new Font("Arial", Font.ITALIC, cw < 160 ? 9 : 11));
        g.setColor(new Color(195, 195, 195, 180));
        fm = g.getFontMetrics();
        int subY = divY + 37;
        float subT = (float) (subY - cy) / ch;
        int subCenter = (int) (cx + cw / 2.0 + slant * (1.0 - subT));
        g.drawString(SUBTITLES[index], subCenter - fm.stringWidth(SUBTITLES[index]) / 2, subY);

        // Stats
        int statY = divY + 44;
        float statT = (float) (statY - cy) / ch;
        int statCenter = (int) (cx + cw / 2.0 + slant * (1.0 - statT));
        drawStats(g, index, statCenter - (cw - 20) / 2, statY, cw - 20);

        // Slot tags
        int slots = (gameMode == 1 || gameMode == 3) ? 2 : 1;
        for (int s = 0; s < slots; s++) {
            if (p1Selections[s] == index && p1Confirmed[s]) {
                g.setFont(new Font("Arial", Font.BOLD, 8));
                fm = g.getFontMetrics();
                String tag = "P1" + (slots > 1 ? "-" + (s + 1) : "");
                int tagX = cx + slant + 3 + s * 28;
                g.setColor(new Color(50, 150, 255, 220));
                g.fillRoundRect(tagX, cy + 3, fm.stringWidth(tag) + 6, 13, 3, 3);
                g.setColor(Color.WHITE);
                g.drawString(tag, tagX + 3, cy + 13);
            }
            if (gameMode != 2 && gameMode != 3 && p2Selections[s] == index && p2Confirmed[s]) {
                g.setFont(new Font("Arial", Font.BOLD, 8));
                fm = g.getFontMetrics();
                String tag = "P2" + (slots > 1 ? "-" + (s + 1) : "");
                int tagX = cx + cw + slant - 3 - (s + 1) * 28;
                g.setColor(new Color(255, 80, 80, 220));
                g.fillRoundRect(tagX, cy + 3, fm.stringWidth(tag) + 6, 13, 3, 3);
                g.setColor(Color.WHITE);
                g.drawString(tag, tagX + 3, cy + 13);
            }
        }
        
        // Cursors 
        if (p1h) drawCursor(g, cx, cy, cw, ch, slant, new Color(50, 150, 255), "P1", allP1Done());
        if (p2h) drawCursor(g, cx + 3, cy + 3, cw - 6, ch - 6, slant, new Color(255, 80, 80), "P2", allP2Done());
    }

    private void drawStats(Graphics2D g, int idx, int x, int y, int w) {
        String[] lbl = {"PWR", "SPD", "HP"};
        int[] vals = {STAT_PWR[idx], STAT_SPD[idx], STAT_HP[idx]};
        Color[] cols = {new Color(255, 80, 80), new Color(80, 190, 255), new Color(80, 210, 80)};
        g.setFont(new Font("Arial", Font.BOLD, 10));
        for (int i = 0; i < 3; i++) {
            int by = y + i * 13;
            g.setColor(new Color(220, 220, 220, 160));
            g.drawString(lbl[i], x, by + 8);
            int bx = x + 26, bw = w - 28, bh = 6;
            g.setColor(new Color(28, 28, 42));
            g.fillRoundRect(bx, by, bw, bh, bh, bh);
            g.setColor(cols[i]);
            g.fillRoundRect(bx, by, (int) (bw * vals[i] / 100f), bh, bh, bh);
        }
    }

    private void drawCursor(Graphics2D g, int cx, int cy, int cw, int ch, int slant, Color col, String label, boolean conf) {
        g.setColor(conf ? new Color(255, 255, 100) : col);
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        int[] px = {cx + slant, cx + cw + slant, cx + cw, cx};
        int[] py = {cy, cy, cy + ch, cy + ch};
        g.drawPolygon(px, py, 4);
        
        g.setStroke(new BasicStroke(1));
        g.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        int lw = fm.stringWidth(label) + 10;
        
        g.setColor(conf ? new Color(255, 255, 100) : col); 
        g.fillRoundRect(cx + slant + 6, cy + 6, lw, 15, 4, 4);
        g.setColor(Color.BLACK); 
        g.drawString(label, cx + slant + 11, cy + 17);
        
        if (conf) {
            g.setFont(new Font("Impact", Font.PLAIN, 14));
            g.setColor(new Color(255, 255, 100));
            g.drawString("LOCKED", cx + cw / 2 - 27 + slant / 2, cy + ch / 2);
        }
    }

    private boolean allP1Done() {
        for (boolean b : p1Confirmed) if (!b) return false;
        return true;
    }

    private boolean allP2Done() {
        for (boolean b : p2Confirmed) if (!b) return false;
        return true;
    }

    private boolean allBotDone() {
        for (boolean b : botConfirmed) if (!b) return false;
        return true;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        int slots = (gameMode == 1 || gameMode == 3) ? 2 : 1;
        
        if (phase == 0) {
            if (!difficultyDone) {
                if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) botDifficulty = (botDifficulty + 3) % 4;
                if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) botDifficulty = (botDifficulty + 1) % 4;
                if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_F) difficultyDone = true;
            } else if (!allBotDone()) {
                if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) botCursor = (botCursor + NUM_CHARS - 1) % NUM_CHARS;
                if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) botCursor = (botCursor + 1) % NUM_CHARS;
                if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_F) {
                    botSelections[botSlot] = botCursor;
                    botConfirmed[botSlot] = true;
                    if (botSlot < slots - 1) botSlot++;
                }
            } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_F) {
                phase = 1;
            }
        } else {
            if (!p1Confirmed[p1Slot]) {
                if (code == KeyEvent.VK_A) p1Cursor = (p1Cursor + NUM_CHARS - 1) % NUM_CHARS;
                if (code == KeyEvent.VK_D) p1Cursor = (p1Cursor + 1) % NUM_CHARS;
                if (code == KeyEvent.VK_F) {
                    p1Selections[p1Slot] = p1Cursor;
                    p1Confirmed[p1Slot] = true;
                    if (p1Slot < slots - 1) p1Slot++;
                }
            }
            if (gameMode != 2 && gameMode != 3 && !p2Confirmed[p2Slot]) {
                if (code == KeyEvent.VK_LEFT) p2Cursor = (p2Cursor + NUM_CHARS - 1) % NUM_CHARS;
                if (code == KeyEvent.VK_RIGHT) p2Cursor = (p2Cursor + 1) % NUM_CHARS;
                if (code == KeyEvent.VK_NUMPAD1) {
                    p2Selections[p2Slot] = p2Cursor;
                    p2Confirmed[p2Slot] = true;
                    if (p2Slot < slots - 1) p2Slot++;
                }
            }
            
            boolean ready = allP1Done() && (gameMode == 2 || gameMode == 3 || allP2Done());
            if (ready && (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_F)) {
                // Trigger game start logic here
                System.out.println("Starting match...");
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}