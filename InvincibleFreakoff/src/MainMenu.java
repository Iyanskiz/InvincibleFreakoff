import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.net.URL;
import javax.sound.sampled.*; // Added for audio support

public class MainMenu extends JPanel implements ActionListener, KeyListener {

    private JFrame parentFrame;
    private javax.swing.Timer animTimer;
    private int tick = 0;
    private int selectedOption = 0;
    private Random rand = new Random();
    private List<MenuParticle> particles = new ArrayList<>();
    private List<float[]> stars = new ArrayList<>();

    // --- TITLE SCREEN & AUDIO ---
    private static boolean firstTimeLaunch = true; 
    private boolean showingTitleScreen;
    private Image titleGif;
    private Clip titleMusic; // Audio handler
    // ----------------------------

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
        
        this.showingTitleScreen = firstTimeLaunch;

        // Load Title GIF
        URL imgUrl = getClass().getResource("/imgs/titlecard.gif");
        if (imgUrl != null) {
            titleGif = new ImageIcon(imgUrl).getImage();
        }

        // Initialize Audio (Only if it's the first launch)
        if (showingTitleScreen) {
            playTitleMusic("/sounds/title.wav"); // Recommend converting mp3 to wav
        }

        for (int i = 0; i < 300; i++)
            stars.add(new float[]{rand.nextInt(2000), rand.nextInt(1000), rand.nextFloat()*2.5f+0.5f});
            
        animTimer = new javax.swing.Timer(16, this);
        animTimer.start();
    }

    private void playTitleMusic(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                titleMusic = AudioSystem.getClip();
                titleMusic.open(audioIn);
                titleMusic.loop(Clip.LOOP_CONTINUOUSLY); // Loops the music
                titleMusic.start();
            }
        } catch (Exception e) {
            System.err.println("Music failed to load: " + e.getMessage());
        }
    }

    private void stopTitleMusic() {
        if (titleMusic != null && titleMusic.isRunning()) {
            titleMusic.stop();
            titleMusic.close();
        }
    }

    @Override public void actionPerformed(ActionEvent e) {
        tick++;
        if (!showingTitleScreen) {
            if (tick % 5 == 0 && getWidth() > 0)
                particles.add(new MenuParticle(rand.nextInt(getWidth()), getHeight() + 10));
            particles.removeIf(p -> p.y < -20 || p.life <= 0);
            for (MenuParticle p : particles) p.update();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        int W = getWidth(), H = getHeight();

        if (showingTitleScreen) {
            if (titleGif != null) {
                g.drawImage(titleGif, 0, 0, W, H, this);
            } else {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, W, H);
            }
            return; 
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        drawBackground(g, W, H);
        drawTitle(g, W, H);
        drawOptions(g, W, H);
        drawFooter(g, W, H);
    }

    // --- RENDER METHODS ---

    private void drawBackground(Graphics2D g, int W, int H) {
        if (W <= 0 || H <= 0) return;
        GradientPaint bg = new GradientPaint(0,0,new Color(4,4,18),W,H,new Color(12,0,28));
        g.setPaint(bg); g.fillRect(0,0,W,H);
        for (float[] star : stars) {
            float tw = (float)(Math.sin(tick*0.04+star[0]*0.01)*0.3+0.7);
            int br = Math.min(255,(int)(tw*(100+star[2]*55)));
            g.setColor(new Color(br,br,Math.min(255,br+30)));
            int sz = star[2] > 2 ? 2 : 1;
            g.fillOval((int)(star[0] % W),(int)(star[1] % H), sz, sz);
        }
        for (MenuParticle p : particles) p.draw(g);
        
        g.setColor(new Color(5,4,12));
        int bCount = Math.max(2, W / 50 + 2);
        int[] bx = new int[bCount * 2 + 2]; int[] by = new int[bCount * 2 + 2];
        bx[0] = 0; by[0] = H;
        for (int i = 0; i < bCount; i++) {
            int wx  = i * W / (bCount - 1);
            int ht  = (int)(H * 0.62f - Math.sin(i * 0.9 + 0.3) * H * 0.08f - Math.sin(i * 2.3) * H * 0.03f);
            int wx2 = Math.min((i + 1) * W / (bCount - 1), W);
            bx[i*2+1] = wx;  by[i*2+1] = ht; bx[i*2+2] = wx2; by[i*2+2] = ht;
        }
        bx[bCount*2+1] = W; by[bCount*2+1] = H;
        g.fillPolygon(bx, by, bCount*2+2);
    }

    private void drawTitle(Graphics2D g, int W, int H) {
        int titleSize = Math.min(120, W / 10);
        g.setFont(new Font("Impact", Font.PLAIN, titleSize));
        FontMetrics fm = g.getFontMetrics();
        String l1 = "INVINCIBLE", l2 = "SHOWDOWNS";
        int l1w = fm.stringWidth(l1), l2w = fm.stringWidth(l2);
        int titleY1 = (int)(H * 0.18f), titleY2 = (int)(H * 0.18f) + titleSize + 4;

        g.setColor(new Color(0,0,0,200));
        g.drawString(l1, W/2 - l1w/2 + 4, titleY1 + 4);
        g.drawString(l2, W/2 - l2w/2 + 4, titleY2 + 4);

        g.setPaint(new GradientPaint(0, titleY1-titleSize, new Color(255,240,80), 0, titleY1, new Color(255,140,0)));
        g.drawString(l1, W/2 - l1w/2, titleY1);
        g.setPaint(new GradientPaint(0, titleY2-titleSize, new Color(255,80,50), 0, titleY2, new Color(200,0,0)));
        g.drawString(l2, W/2 - l2w/2, titleY2);
    }

    private void drawOptions(Graphics2D g, int W, int H) {
        int hintSize = Math.max(11, W/120);
        g.setFont(new Font("Arial", Font.BOLD, hintSize));
        g.setColor(new Color(255,255,255,90));
        FontMetrics fm = g.getFontMetrics();
        String nav = "W/S  or  ↑↓  to navigate    ENTER or F to select";
        g.drawString(nav, W/2 - fm.stringWidth(nav)/2, (int)(H * 0.46f));

        int margin = (int)(W * 0.04f), gap = (int)(W * 0.015f);
        int cardW = (W - margin*2 - gap*3) / 4, cardH = (int)(H * 0.30f), cardY = (int)(H * 0.49f);
        for (int i = 0; i < 4; i++) drawCard(g, i, margin + i*(cardW+gap), cardY, cardW, cardH);

        g.setFont(new Font("Arial", Font.ITALIC, Math.max(13, W/100)));
        g.setColor(new Color(200,200,200,200));
        fm = g.getFontMetrics();
        g.drawString(DESCRIPTIONS[selectedOption], W/2 - fm.stringWidth(DESCRIPTIONS[selectedOption])/2, cardY+cardH+30);
        drawControlsReminder(g, W, cardY+cardH+55);
    }

    private void drawCard(Graphics2D g, int index, int cx, int cy, int cw, int ch) {
        boolean sel = (index == selectedOption); Color col = OPTION_COLORS[index];
        float pulse = (float)(Math.sin(tick*0.08)*0.5+0.5);
        
        g.setColor(new Color(0,0,0,100)); g.fillRoundRect(cx+4, cy+4, cw, ch, 18, 18);
        g.setPaint(new GradientPaint(cx, cy, new Color(col.getRed()/7, col.getGreen()/7, col.getBlue()/7, 220), cx, cy+ch, new Color(4,4,12,220)));
        g.fillRoundRect(cx, cy, cw, ch, 18, 18);
        
        if (sel) {
            g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), (int)(140+115*pulse)));
            g.setStroke(new BasicStroke(3));
        } else {
            g.setColor(new Color(55,55,75,190)); g.setStroke(new BasicStroke(1.5f));
        }
        g.drawRoundRect(cx, cy, cw, ch, 18, 18);

        int titleSize = Math.max(18, cw / 7);
        g.setFont(new Font("Impact", Font.PLAIN, sel ? titleSize+4 : titleSize));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(sel ? col : new Color(150,150,160));
        g.drawString(OPTIONS[index], cx + (cw-fm.stringWidth(OPTIONS[index]))/2, cy + ch/2);
    }

    private void drawControlsReminder(Graphics2D g, int W, int y) {
        int sz = Math.max(10, W/140); g.setFont(new Font("Arial", Font.PLAIN, sz));
        g.setColor(new Color(255,255,255,50)); FontMetrics fm = g.getFontMetrics();
        String[] lines = {"P1: WASD + F/G/H   |   P2: Arrow Keys + NP1/NP2/NP3", "P3: IJKL + U/O/P   |   P4: NP4/NP6/NP8 + NP7/NP9/NP0"};
        for (int i = 0; i < lines.length; i++) 
            g.drawString(lines[i], W/2 - fm.stringWidth(lines[i])/2, y + i*(sz+4));
    }

    private void drawFooter(Graphics2D g, int W, int H) {
        g.setFont(new Font("Arial", Font.PLAIN, Math.max(10, W/140)));
        g.setColor(new Color(255,255,255,35));
        String f = "Invincible Showdowns v3.0";
        g.drawString(f, W/2 - g.getFontMetrics().stringWidth(f)/2, H - 14);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int c = e.getKeyCode();
        if (showingTitleScreen) {
            if (c == KeyEvent.VK_ENTER) {
                stopTitleMusic(); // Kill music when Enter is pressed
                showingTitleScreen = false;
                firstTimeLaunch = false;
                repaint();
            }
            return; 
        }
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
        float x, y, vx, vy, size; Color color; int life, maxLife;
        MenuParticle(float x, float y) {
            Random r = new Random(); this.x = x; this.y = y;
            vx = (r.nextFloat()-0.5f)*1.8f; vy = -(r.nextFloat()*2.5f+0.5f); size = r.nextFloat()*4+2;
            Color[] cols = {new Color(255,150,0),new Color(255,220,50),new Color(200,50,50)};
            color = cols[r.nextInt(cols.length)]; maxLife = 90+r.nextInt(70); life = maxLife;
        }
        void update() { x+=vx; y+=vy; life--; size*=0.995f; }
        void draw(Graphics2D g) {
            int alpha = Math.max(0, (int)(((float)life/maxLife)*160));
            g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),alpha));
            g.fillOval((int)(x-size/2),(int)(y-size/2),(int)size,(int)size);
        }
    }
}