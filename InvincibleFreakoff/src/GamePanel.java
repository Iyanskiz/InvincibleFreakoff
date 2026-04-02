import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements ActionListener {

    private Fighter p1, p2;
    private HealthBar hb1, hb2;
    private InputHandler input;
    private javax.swing.Timer gameTimer;

    private int roundTimer = 99;
    private int timerTick  = 0;
    private int tick       = 0;

    private boolean gameOver  = false;
    private boolean paused    = false;
    private String  winnerText = "";
    private int     winnerTick = 0;

    // Hit effects
    private List<HitEffect> effects = new ArrayList<>();

    // Round state
    private int p1Wins = 0, p2Wins = 0;
    private boolean roundOver = false;
    private int     roundOverTimer = 0;

    private JFrame parentFrame;
    private int p1Index, p2Index;

    public GamePanel(int p1Index, int p2Index) {
        this.p1Index = p1Index;
        this.p2Index = p2Index;
        setPreferredSize(new Dimension(1100, 650));
        setFocusable(true);

        input = new InputHandler();
        addKeyListener(input);

        // Health bars
        hb1 = new HealthBar(30, 30, 430, 28, true);
        hb2 = new HealthBar(640, 30, 430, 28, false);

        gameTimer = new javax.swing.Timer(16, this);
    }

    public void startGame() {
        spawnFighters();
        gameTimer.start();
    }

    private void spawnFighters() {
        p1 = createFighter(p1Index, 150, true);
        p2 = createFighter(p2Index, 870, false);
        hb1.setFighterName(p1.name);
        hb2.setFighterName(p2.name);
        roundOver = false;
        roundTimer = 99;
        timerTick  = 0;
    }

    private Fighter createFighter(int index, int x, boolean isP1) {
        switch (index) {
            case 0: return new Invincible(x, isP1);
            case 1: return new OmniMan(x, isP1);
            case 2: return new Thragg(x, isP1);
            case 3: return new Conquest(x, isP1);
            default: return new Invincible(x, isP1);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused && !gameOver) {
            update();
        }
        tick++;
        repaint();
    }

    private void update() {
        if (roundOver) {
            roundOverTimer--;
            if (roundOverTimer <= 0) {
                if (p1Wins >= 2 || p2Wins >= 2) {
                    gameOver = true;
                    winnerText = (p1Wins >= 2) ? p1.name + " WINS!" : p2.name + " WINS!";
                } else {
                    spawnFighters();
                }
            }
            return;
        }

        // Round timer
        timerTick++;
        if (timerTick >= 60) {
            timerTick = 0;
            roundTimer--;
            if (roundTimer <= 0) {
                endRound(p1.currentHealth > p2.currentHealth ? p1 : p2);
            }
        }

        // ── P1 Input ──────────────────────────────────
        if (input.p1Left())    p1.moveLeft();
        if (input.p1Right())   p1.moveRight();
        if (input.p1Jump())    p1.jump();
        if (input.p1Block())   p1.block(); else p1.stopBlock();
        if (input.p1Light())   p1.lightAttack();
        if (input.p1Heavy())   p1.heavyAttack();
        if (input.p1Special()) p1.specialMove();

        // ── P2 Input ──────────────────────────────────
        if (input.p2Left())    p2.moveLeft();
        if (input.p2Right())   p2.moveRight();
        if (input.p2Jump())    p2.jump();
        if (input.p2Block())   p2.block(); else p2.stopBlock();
        if (input.p2Light())   p2.lightAttack();
        if (input.p2Heavy())   p2.heavyAttack();
        if (input.p2Special()) p2.specialMove();

        // Pause
        if (input.pausePressed()) paused = !paused;

        // ── Update fighters ───────────────────────────
        p1.update(p2);
        p2.update(p1);

        // ── Collision / hit detection ─────────────────
        checkHits();

        // ── Round end check ───────────────────────────
        if (p1.isDead()) endRound(p2);
        if (p2.isDead()) endRound(p1);

        // ── Update effects ────────────────────────────
        effects.removeIf(ef -> ef.life <= 0);
        for (HitEffect ef : effects) ef.update();
    }

    private void checkHits() {
        // P1 hits P2
        if (p1.isAttacking && p1.attackHitbox != null) {
            if (p1.attackHitbox.intersects(p2.getHurtbox())) {
                if (!p2.isHurt) {
                    p2.takeDamage(p1.attackDamage);
                    spawnHitEffect(p2.x + p2.width / 2, p2.y + p2.height / 2);
                }
            }
        }
        // P2 hits P1
        if (p2.isAttacking && p2.attackHitbox != null) {
            if (p2.attackHitbox.intersects(p1.getHurtbox())) {
                if (!p1.isHurt) {
                    p1.takeDamage(p2.attackDamage);
                    spawnHitEffect(p1.x + p1.width / 2, p1.y + p1.height / 2);
                }
            }
        }
    }

    private void endRound(Fighter winner) {
        if (roundOver) return;
        roundOver = true;
        roundOverTimer = 180;
        if (winner == p1) p1Wins++;
        else              p2Wins++;
        winnerTick = 0;
    }

    private void spawnHitEffect(int x, int y) {
        effects.add(new HitEffect(x, y));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();

        // ── Background ────────────────────────────────
        drawBackground(g, W, H);

        // ── Fighters ──────────────────────────────────
        if (p1 != null) p1.draw(g);
        if (p2 != null) p2.draw(g);

        // ── Hit effects ───────────────────────────────
        for (HitEffect ef : effects) ef.draw(g);

        // ── HUD ───────────────────────────────────────
        drawHUD(g, W);

        // ── Round/Win overlay ─────────────────────────
        if (roundOver) drawRoundOverlay(g, W, H);
        if (gameOver)  drawGameOverOverlay(g, W, H);
        if (paused)    drawPauseOverlay(g, W, H);
    }

    private void drawBackground(Graphics2D g, int W, int H) {
        // Sky gradient
        GradientPaint sky = new GradientPaint(0, 0, new Color(10, 10, 30), 0, H * 0.6f, new Color(20, 0, 50));
        g.setPaint(sky);
        g.fillRect(0, 0, W, H);

        // City silhouette (simple)
        g.setColor(new Color(5, 5, 15));
        int[] bx = {0, 0, 60, 60, 80, 80, 130, 130, 180, 180, 220, 220, 280, 280, 350, 350,
                    400, 400, 460, 460, 520, 520, 580, 580, 640, 640, 700, 700, 760, 760,
                    820, 820, 880, 880, 940, 940, 1000, 1000, 1060, 1060, 1100, 1100};
        int[] by = {H, 500, 500, 460, 460, 480, 480, 440, 440, 470, 470, 430, 430, 455, 455, 410,
                    410, 450, 450, 420, 420, 445, 445, 415, 415, 460, 460, 425, 425, 445, 445,
                    408, 408, 450, 450, 425, 425, 455, 455, 430, 430, H};
        g.fillPolygon(bx, by, bx.length);

        // Ground
        GradientPaint ground = new GradientPaint(0, Fighter.GROUND_Y + Fighter.GROUND_Y / 10,
                new Color(30, 20, 50), 0, H, new Color(10, 5, 20));
        g.setPaint(ground);
        g.fillRect(0, Fighter.GROUND_Y + 115, W, H - Fighter.GROUND_Y - 115);

        // Ground line
        g.setColor(new Color(100, 60, 160, 120));
        g.setStroke(new BasicStroke(2));
        g.drawLine(0, Fighter.GROUND_Y + 115, W, Fighter.GROUND_Y + 115);
        g.setStroke(new BasicStroke(1));

        // Stars
        g.setColor(new Color(255, 255, 255, 80));
        for (int i = 0; i < 60; i++) {
            int sx = (int)((i * 137 + 50) % W);
            int sy = (int)((i * 97 + 20) % 200);
            g.fillOval(sx, sy, 2, 2);
        }
    }

    private void drawHUD(Graphics2D g, int W) {
        // P1 health bar
        hb1.draw(g, p1.currentHealth, p1.maxHealth);
        // P2 health bar
        hb2.draw(g, p2.currentHealth, p2.maxHealth);

        // Round timer
        g.setFont(new Font("Impact", Font.PLAIN, 44));
        String timeStr = String.valueOf(roundTimer);
        FontMetrics fm = g.getFontMetrics();
        int tx = W / 2 - fm.stringWidth(timeStr) / 2;
        // Shadow
        g.setColor(Color.BLACK);
        g.drawString(timeStr, tx + 2, 58);
        // Color changes with urgency
        g.setColor(roundTimer > 30 ? Color.WHITE : new Color(255, 80, 80));
        g.drawString(timeStr, tx, 57);

        // Win pips
        drawWinPips(g, W);

        // Controls reminder (small)
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.setColor(new Color(255, 255, 255, 60));
        g.drawString("P1: WASD + F/G/H/ESC", 30, 75);
        g.drawString("P2: Arrows + NP1/NP2/NP3", W - 210, 75);
    }

    private void drawWinPips(Graphics2D g, int W) {
        // P1 wins left side
        for (int i = 0; i < 2; i++) {
            int px = 30 + i * 22;
            if (i < p1Wins) {
                g.setColor(new Color(255, 220, 50));
                g.fillOval(px, 68, 16, 16);
            } else {
                g.setColor(new Color(60, 60, 60));
                g.drawOval(px, 68, 16, 16);
            }
        }
        // P2 wins right side
        for (int i = 0; i < 2; i++) {
            int px = W - 68 + i * 22;
            if (i < p2Wins) {
                g.setColor(new Color(255, 220, 50));
                g.fillOval(px, 68, 16, 16);
            } else {
                g.setColor(new Color(60, 60, 60));
                g.drawOval(px, 68, 16, 16);
            }
        }
    }

    private void drawRoundOverlay(Graphics2D g, int W, int H) {
        // Flash overlay
        g.setColor(new Color(255, 255, 255, Math.min(40, roundOverTimer * 2)));
        g.fillRect(0, 0, W, H);

        String msg;
        if (gameOver) return;
        msg = (p1Wins > p2Wins) ? p1.name + " wins the round!" : p2.name + " wins the round!";

        g.setFont(new Font("Impact", Font.PLAIN, 54));
        FontMetrics fm = g.getFontMetrics();
        int tx = W / 2 - fm.stringWidth(msg) / 2;

        g.setColor(Color.BLACK);
        g.drawString(msg, tx + 3, H / 2 + 3);
        GradientPaint gp = new GradientPaint(0, H/2f - 40, new Color(255,220,50), 0, H/2f + 10, new Color(255, 80, 0));
        g.setPaint(gp);
        g.drawString(msg, tx, H / 2);
    }

    private void drawGameOverOverlay(Graphics2D g, int W, int H) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, W, H);

        g.setFont(new Font("Impact", Font.PLAIN, 72));
        FontMetrics fm = g.getFontMetrics();
        int tx = W / 2 - fm.stringWidth(winnerText) / 2;

        GradientPaint gp = new GradientPaint(0, H/2f - 50, new Color(255,220,50), 0, H/2f + 30, new Color(255,60,0));
        g.setPaint(gp);
        g.drawString(winnerText, tx, H / 2);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(255, 255, 255, 180));
        String restart = "Press ENTER to play again";
        fm = g.getFontMetrics();
        if ((tick / 30) % 2 == 0) {
            g.drawString(restart, W / 2 - fm.stringWidth(restart) / 2, H / 2 + 60);
        }

        // Check for restart
        // (handled in keyPressed via input)
    }

    private void drawPauseOverlay(Graphics2D g, int W, int H) {
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, W, H);
        g.setFont(new Font("Impact", Font.PLAIN, 64));
        g.setColor(Color.WHITE);
        g.drawString("PAUSED", W / 2 - 100, H / 2);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(new Color(200, 200, 200));
        g.drawString("Press ESC to resume", W / 2 - 80, H / 2 + 40);
    }

    // ── Hit Effect inner class ─────────────────────────

    private static class HitEffect {
        int x, y, life = 20;
        Color color;

        HitEffect(int x, int y) {
            this.x = x;
            this.y = y;
            // Random hit color
            Color[] cols = {new Color(255,200,50), new Color(255,100,50), new Color(255,255,150)};
            color = cols[(int)(Math.random() * cols.length)];
        }

        void update() { life--; }

        void draw(Graphics2D g) {
            float alpha = life / 20f;
            int size = (int)((20 - life) * 3 + 10);
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 200)));
            g.setStroke(new BasicStroke(3));
            // Star burst
            for (int i = 0; i < 6; i++) {
                double angle = Math.PI * 2 * i / 6;
                int x2 = (int)(x + Math.cos(angle) * size);
                int y2 = (int)(y + Math.sin(angle) * size);
                g.drawLine(x, y, x2, y2);
            }
            g.fillOval(x - 6, y - 6, 12, 12);
            g.setStroke(new BasicStroke(1));
        }
    }
}