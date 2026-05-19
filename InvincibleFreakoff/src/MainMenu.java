import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainMenu extends JPanel implements ActionListener, KeyListener {

    private JFrame parentFrame;
    private javax.swing.Timer animTimer;
    private int tick = 0;
    private int selectedOption = 0;
    private Random rand = new Random();
    private List<MenuParticle> particles = new ArrayList<>();
    private List<float[]> stars = new ArrayList<>();

    private static final String[] OPTIONS      = {"1  VS  1", "2  VS  2", "1  VS  BOT", "2  VS  2 BOT"};
    private static final String[] DESCRIPTIONS = {
        "Classic one-on-one Viltrumite combat",
        "Team up — two fighters per side",
        "Challenge the AI — pick your difficulty",
        "2v2 Team battle against AI opponents"
    };
    private static final Color[] OPTION_COLORS = {
        new Color(255, 200, 50),
        new Color(50, 180, 255),
        new Color(255, 80, 80),
        new Color(180, 80, 255)
    };
    private static final String[] TAGS = {"CLASSIC", "TEAM BATTLE", "VS AI", "TEAM VS AI"};

    public MainMenu(JFrame frame) {
        this.parentFrame = frame;
        setFocusable(true);
        addKeyListener(this);
        for (int i = 0; i < 300; i++)
            stars.add(new float[]{rand.nextInt(2000), rand.nextInt(1000), rand.nextFloat()*2.5f+0.5f});
        animTimer = new javax.swing.Timer(16, this);
        animTimer.start();
    }

    @Override public void actionPerformed(ActionEvent e) {
        tick++;
        if (tick % 5 == 0 && getWidth() > 0)
            particles.add(new MenuParticle(rand.nextInt(getWidth()), getHeight() + 10));
        particles.removeIf(p -> p.y < -20 || p.life <= 0);
        for (MenuParticle p : particles) p.update();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int W = getWidth(), H = getHeight();
        drawBackground(g, W, H);
        drawTitle(g, W, H);
        drawOptions(g, W, H);
        drawFooter(g, W, H);
    }

    private void drawBackground(Graphics2D g, int W, int H) {
        if (W <= 0 || H <= 0) return;  // not yet laid out
        GradientPaint bg = new GradientPaint(0,0,new Color(4,4,18),W,H,new Color(12,0,28));
        g.setPaint(bg); g.fillRect(0,0,W,H);

        // Stars
        for (float[] star : stars) {
            float tw = (float)(Math.sin(tick*0.04+star[0]*0.01)*0.3+0.7);
            int br = Math.min(255,(int)(tw*(100+star[2]*55)));
            g.setColor(new Color(br,br,Math.min(255,br+30)));
            int sz = star[2] > 2 ? 2 : 1;
            g.fillOval((int)(star[0] % W),(int)(star[1] % H), sz, sz);
        }

        // Nebula glows
        RadialGradientPaint n1 = new RadialGradientPaint(W*0.15f, H*0.4f, W*0.3f,
            new float[]{0f,1f}, new Color[]{new Color(80,0,160,25),new Color(0,0,0,0)});
        g.setPaint(n1); g.fillRect(0,0,W,H);
        RadialGradientPaint n2 = new RadialGradientPaint(W*0.85f, H*0.5f, W*0.28f,
            new float[]{0f,1f}, new Color[]{new Color(200,50,0,20),new Color(0,0,0,0)});
        g.setPaint(n2); g.fillRect(0,0,W,H);

        // Particles
        for (MenuParticle p : particles) p.draw(g);

        // City silhouette — use pre-computed sine curve, no random calls during paint
        g.setColor(new Color(5,4,12));
        int bCount = Math.max(2, W / 50 + 2);
        int[] bx = new int[bCount * 2 + 2];
        int[] by = new int[bCount * 2 + 2];
        bx[0] = 0; by[0] = H;
        for (int i = 0; i < bCount; i++) {
            int wx  = i * W / (bCount - 1);
            int ht  = (int)(H * 0.62f - Math.sin(i * 0.9 + 0.3) * H * 0.08f - Math.sin(i * 2.3) * H * 0.03f);
            int wx2 = Math.min((i + 1) * W / (bCount - 1), W);
            bx[i*2+1] = wx;  by[i*2+1] = ht;
            bx[i*2+2] = wx2; by[i*2+2] = ht;
        }
        bx[bCount*2+1] = W; by[bCount*2+1] = H;
        g.fillPolygon(bx, by, bCount*2+2);

        // Horizon glow
        GradientPaint hor = new GradientPaint(0,(int)(H*0.55f),new Color(180,40,0,40),0,(int)(H*0.68f),new Color(0,0,0,0));
        g.setPaint(hor); g.fillRect(0,(int)(H*0.55f),W,(int)(H*0.15f));
    }

    private void drawTitle(Graphics2D g, int W, int H) {
        // Scale font to panel width
        int titleSize = Math.min(120, W / 10);
        g.setFont(new Font("Impact", Font.PLAIN, titleSize));
        FontMetrics fm = g.getFontMetrics();
        String l1 = "INVINCIBLE", l2 = "SHOWDOWNS";

        int l1w = fm.stringWidth(l1), l2w = fm.stringWidth(l2);
        int titleY1 = (int)(H * 0.18f);
        int titleY2 = (int)(H * 0.18f) + titleSize + 4;

        // Shadows
        g.setColor(new Color(0,0,0,200));
        g.drawString(l1, W/2 - l1w/2 + 4, titleY1 + 4);
        g.drawString(l2, W/2 - l2w/2 + 4, titleY2 + 4);

        // Gradient fills
        GradientPaint gp1 = new GradientPaint(0, titleY1-titleSize, new Color(255,240,80), 0, titleY1, new Color(255,140,0));
        g.setPaint(gp1); g.drawString(l1, W/2 - l1w/2, titleY1);
        GradientPaint gp2 = new GradientPaint(0, titleY2-titleSize, new Color(255,80,50), 0, titleY2, new Color(200,0,0));
        g.setPaint(gp2); g.drawString(l2, W/2 - l2w/2, titleY2);

        // Subtitle
        int subSize = Math.max(12, W / 80);
        g.setFont(new Font("Arial", Font.BOLD, subSize));
        fm = g.getFontMetrics();
        String sub = "— A VILTRUMITE FIGHTING EXPERIENCE —";
        g.setColor(new Color(255,255,255,55));
        g.drawString(sub, W/2 - fm.stringWidth(sub)/2, titleY2 + subSize + 10);

        // Divider line
        float pulse = (float)(Math.sin(tick*0.06)*0.4+0.6);
        g.setColor(new Color(255,200,50, Math.max(0,Math.min(255,(int)(pulse*140)))));
        g.setStroke(new BasicStroke(2));
        int divW = Math.min(400, W/3);
        g.drawLine(W/2-divW, titleY2+subSize+18, W/2+divW, titleY2+subSize+18);
        g.setStroke(new BasicStroke(1));
    }

    private void drawOptions(Graphics2D g, int W, int H) {
        // Navigation hint
        int hintSize = Math.max(11, W/120);
        g.setFont(new Font("Arial", Font.BOLD, hintSize));
        g.setColor(new Color(255,255,255,90));
        FontMetrics fm = g.getFontMetrics();
        String nav = "W/S  or  ↑↓  to navigate    ENTER or F to select";
        int navY = (int)(H * 0.46f);
        g.drawString(nav, W/2 - fm.stringWidth(nav)/2, navY);

        // Cards — fill ~80% of width, scale height to fill space nicely
        int margin  = (int)(W * 0.04f);
        int totalW  = W - margin * 2;
        int gap     = (int)(W * 0.015f);
        int cardW   = (totalW - gap * 3) / 4;
        int cardH   = (int)(H * 0.30f);   // 30% of screen height
        int cardY   = (int)(H * 0.49f);
        int startX  = margin;

        for (int i = 0; i < 4; i++)
            drawCard(g, i, startX + i*(cardW+gap), cardY, cardW, cardH);

        // Description
        int descSize = Math.max(13, W/100);
        g.setFont(new Font("Arial", Font.ITALIC, descSize));
        g.setColor(new Color(200,200,200,200));
        fm = g.getFontMetrics();
        g.drawString(DESCRIPTIONS[selectedOption], W/2 - fm.stringWidth(DESCRIPTIONS[selectedOption])/2, cardY+cardH+30);

        // Controls reminder
        drawControlsReminder(g, W, cardY+cardH+55);

        // Press enter blink
        if ((tick/35)%2 == 0) {
            int pressSize = Math.max(18, W/70);
            g.setFont(new Font("Impact", Font.PLAIN, pressSize));
            g.setColor(OPTION_COLORS[selectedOption]);
            fm = g.getFontMetrics();
            String press = "PRESS ENTER TO START";
            g.drawString(press, W/2 - fm.stringWidth(press)/2, cardY+cardH+55+pressSize+10);
        }
    }

    private void drawCard(Graphics2D g, int index, int cx, int cy, int cw, int ch) {
        boolean sel = (index == selectedOption);
        Color col = OPTION_COLORS[index];
        float pulse = (float)(Math.sin(tick*0.08)*0.5+0.5);

        // Shadow
        g.setColor(new Color(0,0,0,100));
        g.fillRoundRect(cx+4, cy+4, cw, ch, 18, 18);

        // Background gradient
        GradientPaint bg = new GradientPaint(cx, cy,
            new Color(col.getRed()/7, col.getGreen()/7, col.getBlue()/7, 220),
            cx, cy+ch, new Color(4,4,12,220));
        g.setPaint(bg);
        g.fillRoundRect(cx, cy, cw, ch, 18, 18);

        // Border
        if (sel) {
            int a = Math.max(0, Math.min(255, (int)(140+115*pulse)));
            g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), a));
            g.setStroke(new BasicStroke(3));
        } else {
            g.setColor(new Color(55,55,75,190));
            g.setStroke(new BasicStroke(1.5f));
        }
        g.drawRoundRect(cx, cy, cw, ch, 18, 18);
        g.setStroke(new BasicStroke(1));

        // Selection glow
        if (sel) {
            RadialGradientPaint glow = new RadialGradientPaint(cx+cw/2f, cy+ch/2f, cw*0.7f,
                new float[]{0f,1f}, new Color[]{
                    new Color(col.getRed(), col.getGreen(), col.getBlue(), Math.max(0,Math.min(255,(int)(pulse*45)))),
                    new Color(0,0,0,0)});
            g.setPaint(glow);
            g.fillRoundRect(cx-20, cy-20, cw+40, ch+40, 22, 22);
        }

        // Scale fonts to card width
        int titleSize = Math.max(18, cw / 7);
        int tagSize   = Math.max(10, cw / 18);
        int iconSize  = Math.max(13, cw / 16);

        // Main label
        g.setFont(new Font("Impact", Font.PLAIN, sel ? titleSize+4 : titleSize));
        FontMetrics fm = g.getFontMetrics();
        if (sel) {
            GradientPaint tp = new GradientPaint(cx, cy+ch/3, col, cx, cy+ch*2/3, col.darker());
            g.setPaint(tp);
        } else {
            g.setColor(new Color(150, 150, 160));
        }
        int lw = fm.stringWidth(OPTIONS[index]);
        g.drawString(OPTIONS[index], cx + (cw-lw)/2, cy + ch/2 - titleSize/4);

        // Tag
        g.setFont(new Font("Arial", Font.BOLD, tagSize));
        fm = g.getFontMetrics();
        g.setColor(sel ? new Color(col.getRed(), col.getGreen(), col.getBlue(), 180) : new Color(80,80,100));
        int tw = fm.stringWidth(TAGS[index]);
        g.drawString(TAGS[index], cx + (cw-tw)/2, cy + ch/2 + titleSize/2 + 8);

        // Icon
        String icon = index==0?"👤 VS 👤":index==1?"👥 VS 👥":index==2?"👤 VS 🤖":"👥 VS 🤖🤖";
        g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, iconSize));
        fm = g.getFontMetrics();
        int iw = fm.stringWidth(icon);
        g.setColor(sel ? col : new Color(110,110,130));
        g.drawString(icon, cx + (cw-iw)/2, cy + ch/2 + titleSize/2 + tagSize + iconSize + 4);

        // Arrow below selected card
        if (sel) {
            g.setColor(col);
            int ax = cx+cw/2, ay = cy+ch+10;
            g.fillPolygon(new int[]{ax-9,ax+9,ax}, new int[]{ay,ay,ay+9}, 3);
        }
    }

    private void drawControlsReminder(Graphics2D g, int W, int y) {
        int sz = Math.max(10, W/140);
        g.setFont(new Font("Arial", Font.PLAIN, sz));
        g.setColor(new Color(255,255,255,50));
        FontMetrics fm = g.getFontMetrics();
        String[] lines = {
            "P1: WASD + F/G/H   |   P2: Arrow Keys + NP1/NP2/NP3",
            "P3: IJKL + U/O/P   |   P4: NP4/NP6/NP8 + NP7/NP9/NP0   |   DOWN+JUMP = Drop through platform"
        };
        for (int i = 0; i < lines.length; i++)
            g.drawString(lines[i], W/2 - fm.stringWidth(lines[i])/2, y + i*(sz+4));
    }

    private void drawFooter(Graphics2D g, int W, int H) {
        int sz = Math.max(10, W/140);
        g.setFont(new Font("Arial", Font.PLAIN, sz));
        g.setColor(new Color(255,255,255,35));
        FontMetrics fm = g.getFontMetrics();
        String f = "Invincible Showdowns v3.0  |  Built with Java Swing";
        g.drawString(f, W/2 - fm.stringWidth(f)/2, H - 14);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int c = e.getKeyCode();
        if (c==KeyEvent.VK_UP   ||c==KeyEvent.VK_W||c==KeyEvent.VK_LEFT ||c==KeyEvent.VK_A) selectedOption=(selectedOption+3)%4;
        if (c==KeyEvent.VK_DOWN ||c==KeyEvent.VK_S||c==KeyEvent.VK_RIGHT||c==KeyEvent.VK_D) selectedOption=(selectedOption+1)%4;
        if (c==KeyEvent.VK_ENTER||c==KeyEvent.VK_F) launchMode();
        repaint();
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)    {}

    private void launchMode() {
        animTimer.stop();
        CharacterSelect cs = new CharacterSelect(parentFrame, selectedOption);
        parentFrame.setContentPane(cs);
        parentFrame.revalidate();
        cs.requestFocusInWindow();
    }

    static class MenuParticle {
        float x, y, vx, vy, size;
        Color color;
        int life, maxLife;
        MenuParticle(float x, float y) {
            Random r = new Random();
            this.x = x; this.y = y;
            vx = (r.nextFloat()-0.5f)*1.8f;
            vy = -(r.nextFloat()*2.5f+0.5f);
            size = r.nextFloat()*4+2;
            Color[] cols = {new Color(255,150,0),new Color(255,220,50),new Color(200,50,50),new Color(100,150,255)};
            color = cols[r.nextInt(cols.length)];
            maxLife = 90+r.nextInt(70); life = maxLife;
        }
        void update() { x+=vx; y+=vy; life--; size*=0.995f; }
        void draw(Graphics2D g) {
            float a = (float)life/maxLife;
            int alpha = Math.max(0, Math.min(255,(int)(a*160)));
            g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),alpha));
            g.fillOval((int)(x-size/2),(int)(y-size/2),(int)size,(int)size);
        }
    }
}