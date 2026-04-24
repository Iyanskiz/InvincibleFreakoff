import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private Fighter p1, p2;
    private HealthBar hb1, hb2;
    private InputHandler input;
    private javax.swing.Timer gameTimer;
    private Random rand = new Random();

    private int roundTimer = 99;
    private int timerTick  = 0;
    private int tick       = 0;

    private boolean gameOver = false;
    private boolean paused   = false;
    private String winnerText = "";

    private List<HitEffect>    effects  = new ArrayList<>();
    private List<Particle>     particles = new ArrayList<>();
    private List<Meteor>       meteors   = new ArrayList<>();
    private List<Shockwave>    shockwaves = new ArrayList<>();
    private List<ScreenFlash>  flashes   = new ArrayList<>();

    private int p1Wins = 0, p2Wins = 0;
    private boolean roundOver = false;
    private int roundOverTimer = 0;

    private int p1Index, p2Index;

    // ── Stage system ───────────────────────────────────────────────────────────
    private int currentStage = 0;
    private static final int NUM_STAGES = 4;

    // Stage-specific state
    private float[] stageScrollX = new float[NUM_STAGES];
    private List<BuildingPiece> buildingDebris = new ArrayList<>();
    private int meteorTimer = 0;
    private int meteorInterval = 180; // frames between meteors
    private float thunderTimer = 0;
    private boolean thunderFlash = false;
    private List<LightningBolt> lightning = new ArrayList<>();
    private int lightningTimer = 0;
    private List<SpaceDebris> spaceDebris = new ArrayList<>();
    private List<EnergyOrb> energyOrbs = new ArrayList<>();

    public GamePanel(int p1Index, int p2Index) {
        this.p1Index = p1Index;
        this.p2Index = p2Index;

        // Pick random stage
        currentStage = rand.nextInt(NUM_STAGES);

        setPreferredSize(new Dimension(1100, 650));
        setFocusable(true);

        input = new InputHandler();
        addKeyListener(input);
        addKeyListener(this);

        hb1 = new HealthBar(30,  30, 430, 28, true);
        hb2 = new HealthBar(640, 30, 430, 28, false);

        // Init space debris for space stage
        for (int i = 0; i < 20; i++) {
            spaceDebris.add(new SpaceDebris(rand.nextInt(1100), rand.nextInt(400)));
        }
        // Init energy orbs for void stage
        for (int i = 0; i < 8; i++) {
            energyOrbs.add(new EnergyOrb(rand.nextInt(1100), 200 + rand.nextInt(300)));
        }

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
        roundOver  = false;
        roundTimer = 99;
        timerTick  = 0;
        meteorTimer    = 0;
        meteorInterval = 180;
        meteors.clear();
        buildingDebris.clear();
        particles.clear();
        shockwaves.clear();
        flashes.clear();
        effects.clear();
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
        if (!paused && !gameOver) update();
        tick++;
        repaint();
    }

    private void update() {
        if (roundOver) {
            roundOverTimer--;
            // Keep stage effects running during round over
            updateStageEvents();
            for (Particle  p  : particles) p.update();
            for (Meteor    m  : meteors) m.update(this);
            for (Shockwave s  : shockwaves) s.update();
            for (ScreenFlash f : flashes) f.update();
            for (BuildingPiece b : buildingDebris) b.update();
            meteors.removeIf(m -> m.dead);
            particles.removeIf(p -> p.life <= 0);
            shockwaves.removeIf(s -> s.life <= 0);
            flashes.removeIf(f -> f.life <= 0);
            buildingDebris.removeIf(b -> b.y > 700);
            if (roundOverTimer <= 0) {
                if (p1Wins >= 2 || p2Wins >= 2) {
                    gameOver   = true;
                    winnerText = (p1Wins >= 2) ? p1.name + " WINS!" : p2.name + " WINS!";
                } else {
                    currentStage = rand.nextInt(NUM_STAGES); // new stage each round
                    spawnFighters();
                }
            }
            return;
        }

        timerTick++;
        if (timerTick >= 60) {
            timerTick = 0;
            roundTimer--;
            if (roundTimer <= 0) endRound(p1.currentHealth > p2.currentHealth ? p1 : p2);
        }

        // Input
        if (input.p1Left())  p1.moveLeft();
        if (input.p1Right()) p1.moveRight();
        if (input.p1Jump())  p1.jump();
        p1.setBlockHeld(input.p1Block());
        if (input.p1Light())   p1.lightAttack();
        if (input.p1Heavy())   p1.heavyAttack();
        if (input.p1Special()) p1.specialMove();

        if (input.p2Left())  p2.moveLeft();
        if (input.p2Right()) p2.moveRight();
        if (input.p2Jump())  p2.jump();
        p2.setBlockHeld(input.p2Block());
        if (input.p2Light())   p2.lightAttack();
        if (input.p2Heavy())   p2.heavyAttack();
        if (input.p2Special()) p2.specialMove();

        if (input.pausePressed()) paused = !paused;

        p1.update(p2);
        p2.update(p1);
        checkHits();

        if (p1.isDead()) endRound(p2);
        if (p2.isDead()) endRound(p1);

        // Update effects
        effects.removeIf(ef -> ef.life <= 0);
        particles.removeIf(p -> p.life <= 0);
        meteors.removeIf(m -> m.dead);
        shockwaves.removeIf(s -> s.life <= 0);
        flashes.removeIf(f -> f.life <= 0);
        lightning.removeIf(l -> l.life <= 0);
        buildingDebris.removeIf(b -> b.y > 700);
        spaceDebris.removeIf(s -> s.x > 1200);
        energyOrbs.removeIf(o -> o.dead);

        for (HitEffect ef : effects) ef.update();
        for (Particle  p  : particles) p.update();
        for (Meteor    m  : meteors) m.update(this);
        for (Shockwave s  : shockwaves) s.update();
        for (ScreenFlash f : flashes) f.update();
        for (LightningBolt l : lightning) l.update();
        for (BuildingPiece b : buildingDebris) b.update();
        for (SpaceDebris   s : spaceDebris) s.update();
        for (EnergyOrb     o : energyOrbs) o.update();

        // Stage-specific events
        updateStageEvents();
    }

    private void updateStageEvents() {
        switch (currentStage) {
            case 0: // City destruction - meteors
                meteorTimer++;
                if (meteorTimer >= meteorInterval) {
                    meteorTimer = 0;
                    meteorInterval = 120 + rand.nextInt(180);
                    int mx = 100 + rand.nextInt(900);
                    meteors.add(new Meteor(mx));
                }
                // Ambient fire particles
                if (tick % 3 == 0) {
                    int fx = rand.nextInt(1100);
                    particles.add(new Particle(fx, Fighter.GROUND_Y + 115,
                        (rand.nextFloat()-0.5f)*2, -rand.nextFloat()*3-1,
                        new Color(255, 80+rand.nextInt(100), 0, 200), 30+rand.nextInt(20)));
                }
                break;
            case 1: // Space/orbit
                // Scroll space debris
                for (SpaceDebris s : spaceDebris) s.x -= 0.5f;
                if (rand.nextInt(60) == 0) spaceDebris.add(new SpaceDebris(1110, rand.nextInt(400)));
                break;
            case 2: // Lightning storm
                thunderTimer++;
                lightningTimer++;
                if (lightningTimer > 60 + rand.nextInt(120)) {
                    lightningTimer = 0;
                    thunderFlash = true;
                    int lx = rand.nextInt(1100);
                    lightning.add(new LightningBolt(lx, 0, lx + (rand.nextInt(100)-50), Fighter.GROUND_Y + 115));
                    flashes.add(new ScreenFlash(new Color(200, 220, 255, 60), 8));
                }
                break;
            case 3: // Void/energy
                for (EnergyOrb o : energyOrbs) o.update();
                if (rand.nextInt(120) == 0) energyOrbs.add(new EnergyOrb(rand.nextInt(1100), 200+rand.nextInt(250)));
                // Void particles
                if (tick % 5 == 0) {
                    particles.add(new Particle(
                        rand.nextInt(1100), rand.nextInt(600),
                        (rand.nextFloat()-0.5f), (rand.nextFloat()-0.5f),
                        new Color(150, 0, 255, 100+rand.nextInt(100)), 40+rand.nextInt(30)));
                }
                break;
        }
    }

    void spawnMeteorImpact(int x, int y) {
        // Big shockwave
        shockwaves.add(new Shockwave(x, y, new Color(255, 150, 50)));
        // Explosion particles
        for (int i = 0; i < 40; i++) {
            float angle = (float)(Math.PI * 2 * i / 40);
            float speed = 3 + rand.nextFloat() * 6;
            particles.add(new Particle(x, y,
                (float)Math.cos(angle) * speed,
                (float)Math.sin(angle) * speed - 2,
                i % 3 == 0 ? new Color(255, 60, 0, 220)
                           : i % 3 == 1 ? new Color(255, 200, 50, 200)
                                        : new Color(180, 180, 180, 180),
                40 + rand.nextInt(30)));
        }
        // Building chunks
        for (int i = 0; i < 8; i++) {
            buildingDebris.add(new BuildingPiece(x + (rand.nextInt(80)-40), y));
        }
        flashes.add(new ScreenFlash(new Color(255, 150, 50, 80), 12));

        // Damage fighters near impact
        damageFighterNearImpact(p1, x, y);
        damageFighterNearImpact(p2, x, y);
    }

    private void damageFighterNearImpact(Fighter f, int mx, int my) {
        if (f == null) return;
        int fx = f.x + f.width/2, fy = f.y + f.height/2;
        double dist = Math.sqrt(Math.pow(fx-mx,2) + Math.pow(fy-my,2));
        if (dist < 120) {
            f.takeDamage((int)(15 * (1 - dist/120)));
            f.velX = (fx > mx) ? 8 : -8;
            f.velY = -10;
        }
    }

    private void checkHits() {
        if (p1.isAttacking && p1.attackHitbox != null) {
            if (p1.attackHitbox.intersects(p2.getHurtbox()) && !p2.isHurt) {
                p2.takeDamage(p1.attackDamage);
                spawnHitEffect(p2.x + p2.width/2, p2.y + p2.height/2, p2.isBlocking);
                if (!p2.isBlocking) spawnHitParticles(p2.x + p2.width/2, p2.y + p2.height/2);
            }
        }
        if (p2.isAttacking && p2.attackHitbox != null) {
            if (p2.attackHitbox.intersects(p1.getHurtbox()) && !p1.isHurt) {
                p1.takeDamage(p2.attackDamage);
                spawnHitEffect(p1.x + p1.width/2, p1.y + p1.height/2, p1.isBlocking);
                if (!p1.isBlocking) spawnHitParticles(p1.x + p1.width/2, p1.y + p1.height/2);
            }
        }
    }

    private void endRound(Fighter winner) {
        if (roundOver) return;
        roundOver = true;
        roundOverTimer = 180;
        if (winner == p1) p1Wins++;
        else p2Wins++;
        shockwaves.add(new Shockwave(winner.x + winner.width/2, winner.y + winner.height/2, new Color(255,220,50)));
    }

    private void spawnHitEffect(int x, int y, boolean blocked) {
        effects.add(new HitEffect(x, y, blocked));
        shockwaves.add(new Shockwave(x, y, blocked ? new Color(80,160,255) : new Color(255,200,50)));
    }

    private void spawnHitParticles(int x, int y) {
        for (int i = 0; i < 12; i++) {
            float angle = (float)(Math.PI * 2 * i / 12);
            float speed = 2 + rand.nextFloat() * 4;
            Color[] cols = {new Color(255,200,50,200), new Color(255,100,0,200), new Color(255,255,150,200)};
            particles.add(new Particle(x, y,
                (float)Math.cos(angle)*speed, (float)Math.sin(angle)*speed-1,
                cols[i%3], 15+rand.nextInt(10)));
        }
    }

    // ── Paint ──────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();

        drawStageBackground(g, W, H);

        // Draw particles behind fighters
        for (Particle p : particles) p.draw(g);
        for (SpaceDebris s : spaceDebris) s.draw(g);
        for (EnergyOrb  o : energyOrbs) o.draw(g);
        for (LightningBolt l : lightning) l.draw(g);
        for (BuildingPiece b : buildingDebris) b.draw(g);

        // Draw meteors
        for (Meteor m : meteors) m.draw(g);

        // Draw fighters
        if (p1 != null) p1.draw(g);
        if (p2 != null) p2.draw(g);

        // Draw effects on top
        for (Shockwave s : shockwaves) s.draw(g);
        for (HitEffect ef : effects) ef.draw(g);

        // Screen flashes
        for (ScreenFlash f : flashes) f.draw(g, W, H);

        // HUD
        if (p1 != null && p2 != null) drawHUD(g, W, H);

        if (roundOver) drawRoundOverlay(g, W, H);
        if (gameOver)  drawGameOverOverlay(g, W, H);
        if (paused)    drawPauseOverlay(g, W, H);
    }

    // ── Stage backgrounds ──────────────────────────────────────────────────────

    private void drawStageBackground(Graphics2D g, int W, int H) {
        switch (currentStage) {
            case 0: drawCityStage(g, W, H);   break;
            case 1: drawSpaceStage(g, W, H);  break;
            case 2: drawStormStage(g, W, H);  break;
            case 3: drawVoidStage(g, W, H);   break;
        }
    }

    private void drawCityStage(Graphics2D g, int W, int H) {
        // Fiery sky - city under attack
        GradientPaint sky = new GradientPaint(0,0, new Color(15,5,5), 0, H*0.5f, new Color(60,15,5));
        g.setPaint(sky);
        g.fillRect(0, 0, W, H);

        // Distant fire glow on horizon
        GradientPaint glow = new GradientPaint(0, H*0.4f, new Color(200,60,0,80), 0, H*0.7f, new Color(0,0,0,0));
        g.setPaint(glow);
        g.fillRect(0, 0, W, H);

        // Stars
        g.setColor(new Color(255,255,200,100));
        for (int i = 0; i < 50; i++) {
            int sx = (i*173+30) % W, sy = (i*97+10) % 150;
            g.fillOval(sx, sy, 2, 2);
        }

        // Far buildings (dark silhouette)
        g.setColor(new Color(8, 4, 4));
        int[] farX = {0,0,40,40,70,70,110,110,160,160,210,210,270,270,330,330,380,380,
                      440,440,500,500,560,560,620,620,680,680,730,730,790,790,850,850,
                      910,910,970,970,1030,1030,1100,1100};
        int[] farY = {H,400,400,370,370,390,390,355,355,380,380,345,345,372,372,335,335,365,
                      365,340,340,360,360,332,332,358,358,345,345,368,368,338,338,362,362,
                      348,348,370,370,340,340,H};
        g.fillPolygon(farX, farY, farX.length);

        // Closer buildings with fire damage
        g.setColor(new Color(12, 6, 4));
        int[] nearX = {0,0,80,80,150,150,230,230,320,320,420,420,510,510,600,600,690,690,780,780,870,870,950,950,1040,1040,1100,1100};
        int[] nearY = {H,460,460,420,420,450,450,400,400,440,440,415,415,445,445,410,410,438,438,418,418,448,448,405,405,435,435,H};
        g.fillPolygon(nearX, nearY, nearX.length);

        // Windows (some lit orange from fire)
        for (int bx = 0; bx < W; bx += 30) {
            for (int by = 420; by < 580; by += 18) {
                if (rand.nextInt(5) == 0) {
                    Color winCol = rand.nextInt(3)==0 ? new Color(255,120,0,160) : new Color(255,200,80,80);
                    g.setColor(winCol);
                    g.fillRect(bx + rand.nextInt(20), by, 6, 8);
                }
            }
        }

        // Ground - cracked asphalt
        GradientPaint ground = new GradientPaint(0, Fighter.GROUND_Y+115, new Color(35,15,10), 0, H, new Color(10,4,3));
        g.setPaint(ground);
        g.fillRect(0, Fighter.GROUND_Y+115, W, H-Fighter.GROUND_Y-115);
        // Ground cracks
        g.setColor(new Color(80,30,10,120));
        g.setStroke(new BasicStroke(2));
        for (int i = 0; i < 8; i++) {
            int cx = (i*150+50)%W;
            g.drawLine(cx, Fighter.GROUND_Y+115, cx+rand.nextInt(80)-40, Fighter.GROUND_Y+160);
        }
        g.setStroke(new BasicStroke(1));

        // Ground line glow (fire-lit)
        g.setColor(new Color(200, 80, 20, 100));
        g.setStroke(new BasicStroke(3));
        g.drawLine(0, Fighter.GROUND_Y+115, W, Fighter.GROUND_Y+115);
        g.setStroke(new BasicStroke(1));

        // Smoke puffs rising from ground
        for (int i = 0; i < 6; i++) {
            int sx = (i*180+60)%W;
            float alpha = 0.3f + (float)Math.sin(tick*0.02 + i)*0.1f;
            g.setColor(new Color(40,20,10,(int)(alpha*180)));
            int smokeY = Fighter.GROUND_Y + 80 - (tick*2 + i*30) % 200;
            g.fillOval(sx-15, smokeY, 40+i*5, 30+i*3);
        }
    }

    private void drawSpaceStage(Graphics2D g, int W, int H) {
        // Deep space gradient
        GradientPaint sky = new GradientPaint(0,0, new Color(2,4,20), 0, H, new Color(5,0,30));
        g.setPaint(sky);
        g.fillRect(0, 0, W, H);

        // Nebula clouds
        float[] nColors = {0.3f, 0.5f, 0.7f};
        Color[] nCols = {new Color(60,0,120,30), new Color(0,40,120,25), new Color(80,0,80,20)};
        for (int i = 0; i < 3; i++) {
            RadialGradientPaint neb = new RadialGradientPaint(
                W*nColors[i], H*0.3f, 300,
                new float[]{0f,1f}, new Color[]{nCols[i], new Color(0,0,0,0)});
            g.setPaint(neb);
            g.fillRect(0,0,W,H);
        }

        // Stars (parallax layers)
        for (int i = 0; i < 200; i++) {
            int sx = (i*173 + tick/3) % W;
            int sy = (i*97 + 10) % (H-200);
            int brightness = 100 + (i%5)*30;
            g.setColor(new Color(brightness, brightness, Math.min(255,brightness+50), 200));
            int size = (i%4==0) ? 2 : 1;
            g.fillOval(sx, sy, size, size);
        }

        // Planet in background
        RadialGradientPaint planet = new RadialGradientPaint(850, 200, 120,
            new float[]{0f, 0.6f, 1f},
            new Color[]{new Color(60,100,180), new Color(30,60,140), new Color(10,20,80)});
        g.setPaint(planet);
        g.fillOval(730, 80, 240, 240);
        // Planet rings
        g.setColor(new Color(80,120,200,60));
        g.setStroke(new BasicStroke(8));
        g.drawOval(680, 160, 340, 80);
        g.setColor(new Color(100,140,220,40));
        g.setStroke(new BasicStroke(4));
        g.drawOval(700, 170, 300, 60);
        g.setStroke(new BasicStroke(1));

        // Ground - space platform
        GradientPaint ground = new GradientPaint(0, Fighter.GROUND_Y+115, new Color(15,20,50), 0, H, new Color(5,8,25));
        g.setPaint(ground);
        g.fillRect(0, Fighter.GROUND_Y+115, W, H-Fighter.GROUND_Y-115);
        // Platform edge glow
        g.setColor(new Color(60,120,255,150));
        g.setStroke(new BasicStroke(3));
        g.drawLine(0, Fighter.GROUND_Y+115, W, Fighter.GROUND_Y+115);
        // Platform grid lines
        g.setColor(new Color(40,80,180,40));
        g.setStroke(new BasicStroke(1));
        for (int gx = 0; gx < W; gx += 60) g.drawLine(gx, Fighter.GROUND_Y+115, gx, H);
        for (int gy = Fighter.GROUND_Y+115; gy < H; gy += 40) g.drawLine(0, gy, W, gy);
    }

    private void drawStormStage(Graphics2D g, int W, int H) {
        // Stormy sky
        boolean flash = thunderFlash && (tick%4 < 2);
        Color skyTop = flash ? new Color(80,90,120) : new Color(8,10,18);
        Color skyBot = flash ? new Color(40,50,80)  : new Color(15,18,35);
        GradientPaint sky = new GradientPaint(0,0,skyTop, 0,H*0.6f,skyBot);
        g.setPaint(sky);
        g.fillRect(0,0,W,H);
        thunderFlash = false;

        // Storm clouds (layered)
        for (int layer = 0; layer < 3; layer++) {
            int alpha = 80 + layer*40;
            g.setColor(new Color(15+layer*5, 18+layer*5, 30+layer*8, alpha));
            int offset = (int)(tick * (0.3f + layer*0.15f)) % W;
            for (int cx = -200; cx < W+200; cx += 250) {
                int cloudX = (cx + offset) % (W+400) - 200;
                int cloudY = 40 + layer*30;
                g.fillOval(cloudX, cloudY, 280, 80);
                g.fillOval(cloudX+60, cloudY-25, 180, 70);
                g.fillOval(cloudX+140, cloudY+10, 200, 60);
            }
        }

        // Ruins/destroyed buildings
        g.setColor(new Color(10,12,20));
        int[] rX={0,0,60,60,90,110,110,150,150,180,180,230,250,250,300,300,340,340,400,400,450,440,
                  460,460,530,530,580,580,640,640,700,700,740,740,810,810,860,860,920,930,950,950,1020,1020,1100,1100};
        int[] rY={H,420,420,395,395,420,415,415,385,385,410,400,400,375,375,400,400,368,368,395,380,380,
                  370,395,395,362,362,388,388,358,358,385,385,365,365,390,378,378,360,350,360,385,385,362,362,H};
        g.fillPolygon(rX, rY, Math.min(rX.length, rY.length));

        // Rain streaks
        g.setColor(new Color(150,180,220,60));
        g.setStroke(new BasicStroke(1));
        for (int i = 0; i < 80; i++) {
            int rx = (i*47 + tick*6) % W;
            int ry = (i*113 + tick*10) % H;
            g.drawLine(rx, ry, rx-4, ry+18);
        }
        g.setStroke(new BasicStroke(1));

        // Ground
        GradientPaint ground = new GradientPaint(0, Fighter.GROUND_Y+115, new Color(12,14,25), 0, H, new Color(5,6,12));
        g.setPaint(ground);
        g.fillRect(0, Fighter.GROUND_Y+115, W, H-Fighter.GROUND_Y-115);
        // Puddle reflections
        g.setColor(new Color(30,50,100,80));
        for (int i = 0; i < 5; i++) {
            g.fillOval(i*220+30, Fighter.GROUND_Y+120, 150, 20);
        }
        g.setColor(new Color(80,140,255,120));
        g.setStroke(new BasicStroke(2));
        g.drawLine(0, Fighter.GROUND_Y+115, W, Fighter.GROUND_Y+115);
        g.setStroke(new BasicStroke(1));
    }

    private void drawVoidStage(Graphics2D g, int W, int H) {
        // Void - swirling purple/black
        GradientPaint sky = new GradientPaint(0,0, new Color(5,0,15), 0, H, new Color(15,0,35));
        g.setPaint(sky);
        g.fillRect(0,0,W,H);

        // Swirling void rings
        for (int i = 0; i < 5; i++) {
            float angle = (float)(tick * 0.008f + i * Math.PI*2/5);
            int rx = (int)(W/2 + Math.cos(angle) * (150+i*60));
            int ry = (int)(300 + Math.sin(angle*0.7f) * (80+i*30));
            int size = 200 + i*80;
            RadialGradientPaint vp = new RadialGradientPaint(rx, ry, size,
                new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(100,0,200,15+i*5), new Color(60,0,120,10), new Color(0,0,0,0)});
            g.setPaint(vp);
            g.fillOval(rx-size, ry-size, size*2, size*2);
        }

        // Void portal in background
        RadialGradientPaint portal = new RadialGradientPaint(W/2, 200, 150,
            new float[]{0f, 0.4f, 0.8f, 1f},
            new Color[]{new Color(200,0,255,100), new Color(100,0,180,80), new Color(40,0,80,40), new Color(0,0,0,0)});
        g.setPaint(portal);
        g.fillOval(W/2-150, 50, 300, 300);
        // Portal ring
        g.setColor(new Color(180,0,255,150));
        g.setStroke(new BasicStroke(4));
        g.drawOval(W/2-120, 80, 240, 240);
        g.setColor(new Color(220,100,255,100));
        g.setStroke(new BasicStroke(2));
        g.drawOval(W/2-100, 100, 200, 200);
        g.setStroke(new BasicStroke(1));

        // Ground - void platform with cracks
        GradientPaint ground = new GradientPaint(0, Fighter.GROUND_Y+115, new Color(20,0,40), 0, H, new Color(5,0,15));
        g.setPaint(ground);
        g.fillRect(0, Fighter.GROUND_Y+115, W, H-Fighter.GROUND_Y-115);
        // Cracks glowing purple
        g.setColor(new Color(150,0,255,120));
        g.setStroke(new BasicStroke(2));
        for (int i = 0; i < 6; i++) {
            int cx = (i*190+50)%W;
            g.drawLine(cx, Fighter.GROUND_Y+115, cx+(rand.nextInt(60)-30), Fighter.GROUND_Y+170);
        }
        g.setColor(new Color(180,0,255,200));
        g.setStroke(new BasicStroke(3));
        g.drawLine(0, Fighter.GROUND_Y+115, W, Fighter.GROUND_Y+115);
        g.setStroke(new BasicStroke(1));
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    private void drawHUD(Graphics2D g, int W, int H) {
        hb1.draw(g, p1.currentHealth, p1.maxHealth);
        hb2.draw(g, p2.currentHealth, p2.maxHealth);

        // Timer with glow
        g.setFont(new Font("Impact", Font.PLAIN, 48));
        String timeStr = String.valueOf(roundTimer);
        FontMetrics fm = g.getFontMetrics();
        int tx = W/2 - fm.stringWidth(timeStr)/2;
        Color timerCol = roundTimer > 30 ? Color.WHITE : new Color(255,60,60);
        if (roundTimer <= 10) {
            // Pulsing red when low
            int alpha = 150 + (int)(Math.sin(tick*0.3)*105);
            timerCol = new Color(255, 0, 0, Math.max(0, Math.min(255, alpha)));
        }
        g.setColor(new Color(0,0,0,150));
        g.drawString(timeStr, tx+2, 60);
        g.setColor(timerCol);
        g.drawString(timeStr, tx, 59);

        // Stage name banner (fades after 3 seconds)
        if (tick < 180) {
            float alpha = Math.min(1f, (180-tick)/60f);
            String[] stageNames = {"CITY UNDER SIEGE", "THE VOID OF SPACE", "STORM'S WRATH", "THE DARK DIMENSION"};
            g.setFont(new Font("Impact", Font.PLAIN, 28));
            g.setColor(new Color(255,220,50,(int)(alpha*200)));
            fm = g.getFontMetrics();
            String sn = stageNames[currentStage];
            g.drawString(sn, W/2 - fm.stringWidth(sn)/2, H/2);
        }

        // Win pips
        for (int i = 0; i < 2; i++) {
            int px = 30 + i*22;
            if (i < p1Wins) { g.setColor(new Color(255,220,50)); g.fillOval(px,68,16,16); }
            else             { g.setColor(new Color(60,60,60));   g.drawOval(px,68,16,16); }
        }
        for (int i = 0; i < 2; i++) {
            int px = W-68+i*22;
            if (i < p2Wins) { g.setColor(new Color(255,220,50)); g.fillOval(px,68,16,16); }
            else             { g.setColor(new Color(60,60,60));   g.drawOval(px,68,16,16); }
        }

        // Block indicators
        if (p1 != null && p1.isBlocking) {
            g.setFont(new Font("Arial", Font.BOLD, 13));
            g.setColor(new Color(80,160,255)); g.drawString("BLOCKING", 30, 92);
        }
        if (p2 != null && p2.isBlocking) {
            g.setFont(new Font("Arial", Font.BOLD, 13));
            FontMetrics fm2 = g.getFontMetrics();
            g.setColor(new Color(80,160,255)); g.drawString("BLOCKING", W-30-fm2.stringWidth("BLOCKING"), 92);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.setColor(new Color(255,255,255,60));
        g.drawString("P1: WASD+F/G/H", 30, 75);
        g.drawString("P2: Arrows+NP1/2/3", W-185, 75);
    }

    private void drawRoundOverlay(Graphics2D g, int W, int H) {
        g.setColor(new Color(255,255,255, Math.min(40, roundOverTimer*2)));
        g.fillRect(0,0,W,H);
        if (gameOver) return;
        String msg = (p1Wins > p2Wins) ? p1.name+" wins the round!" : p2.name+" wins the round!";
        g.setFont(new Font("Impact", Font.PLAIN, 58));
        FontMetrics fm = g.getFontMetrics();
        int tx = W/2 - fm.stringWidth(msg)/2;
        g.setColor(Color.BLACK); g.drawString(msg, tx+3, H/2+3);
        GradientPaint gp = new GradientPaint(0,H/2f-40, new Color(255,220,50), 0,H/2f+10, new Color(255,80,0));
        g.setPaint(gp); g.drawString(msg, tx, H/2);
    }

    private void drawGameOverOverlay(Graphics2D g, int W, int H) {
        g.setColor(new Color(0,0,0,190)); g.fillRect(0,0,W,H);
        g.setFont(new Font("Impact", Font.PLAIN, 78));
        FontMetrics fm = g.getFontMetrics();
        int tx = W/2 - fm.stringWidth(winnerText)/2;
        GradientPaint gp = new GradientPaint(0,H/2f-55, new Color(255,220,50), 0,H/2f+35, new Color(255,60,0));
        g.setPaint(gp); g.drawString(winnerText, tx, H/2);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(255,255,255,180));
        String restart = "Press ENTER to play again";
        fm = g.getFontMetrics();
        if ((tick/30)%2==0) g.drawString(restart, W/2-fm.stringWidth(restart)/2, H/2+65);
    }

    private void drawPauseOverlay(Graphics2D g, int W, int H) {
        g.setColor(new Color(0,0,0,150)); g.fillRect(0,0,W,H);
        g.setFont(new Font("Impact", Font.PLAIN, 70));
        g.setColor(Color.WHITE); g.drawString("PAUSED", W/2-110, H/2);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(new Color(200,200,200)); g.drawString("Press ESC to resume", W/2-80, H/2+40);
    }

    // ── KeyListener ────────────────────────────────────────────────────────────

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && gameOver) {
            gameTimer.stop();
            input.clear();
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame != null) {
                CharacterSelect cs = new CharacterSelect(frame);
                frame.setContentPane(cs);
                frame.revalidate();
                cs.requestFocusInWindow();
            }
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // ══════════════════════════════════════════════════════════════════════════
    // ── Inner effect classes ──────────────────────────────────────────────────
    // ══════════════════════════════════════════════════════════════════════════

    static class Meteor {
        float x, y, vx, vy;
        int size;
        boolean dead = false;
        List<Particle> trail = new ArrayList<>();
        Random r = new Random();

        Meteor(int startX) {
            x = startX; y = -60;
            vx = (r.nextFloat()-0.5f)*4;
            vy = 8 + r.nextFloat()*5;
            size = 20 + r.nextInt(25);
        }

        void update(GamePanel gp) {
            x += vx; y += vy;
            // Trail particles
            for (int i = 0; i < 3; i++) {
                trail.add(new Particle((int)x, (int)y,
                    (r.nextFloat()-0.5f)*2, -r.nextFloat()*2,
                    r.nextInt(3)==0 ? new Color(255,80,0,200) : new Color(255,200,50,180), 15+r.nextInt(10)));
            }
            trail.removeIf(p -> p.life<=0);
            for (Particle p : trail) p.update();

            if (y >= Fighter.GROUND_Y + 115) {
                dead = true;
                gp.spawnMeteorImpact((int)x, (int)y);
            }
        }

        void draw(Graphics2D g) {
            for (Particle p : trail) p.draw(g);
            // Meteor body
            g.setColor(new Color(40,20,10));
            g.fillOval((int)x-size, (int)y-size, size*2, size*2);
            // Hot glow
            RadialGradientPaint glow = new RadialGradientPaint(x, y, size*1.5f,
                new float[]{0f,0.5f,1f},
                new Color[]{new Color(255,200,50,200), new Color(255,80,0,100), new Color(255,0,0,0)});
            g.setPaint(glow);
            g.fillOval((int)(x-size*1.5f), (int)(y-size*1.5f), (int)(size*3), (int)(size*3));
            // Rock detail
            g.setColor(new Color(60,30,10));
            g.fillOval((int)x-size+5, (int)y-size+5, size/2, size/2);
            g.setColor(new Color(80,40,15));
            g.fillOval((int)x+2, (int)y-size+8, size/3, size/3);
        }
    }

    static class Particle {
        float x, y, vx, vy;
        Color color;
        int life, maxLife;

        Particle(int x, int y, float vx, float vy, Color c, int life) {
            this.x=x; this.y=y; this.vx=vx; this.vy=vy;
            this.color=c; this.life=life; this.maxLife=life;
        }
        void update() { x+=vx; y+=vy; vy+=0.15f; life--; vx*=0.96f; }
        void draw(Graphics2D g) {
            float alpha = (float)life/maxLife;
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha*color.getAlpha())));
            int size = Math.max(1, (int)(3*alpha));
            g.fillOval((int)x-size/2, (int)y-size/2, size, size);
        }
    }

    static class Shockwave {
        int x, y, life = 25;
        Color color;
        Shockwave(int x, int y, Color c) { this.x=x; this.y=y; this.color=c; }
        void update() { life--; }
        void draw(Graphics2D g) {
            float pct = 1f - (float)life/25f;
            int r = (int)(pct*120);
            int alpha = (int)((1-pct)*200);
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0,alpha)));
            g.setStroke(new BasicStroke(3*(1-pct)+1));
            g.drawOval(x-r, y-r, r*2, r*2);
            g.setStroke(new BasicStroke(1));
        }
    }

    static class ScreenFlash {
        Color color; int life, maxLife;
        ScreenFlash(Color c, int life) { this.color=c; this.life=life; this.maxLife=life; }
        void update() { life--; }
        void draw(Graphics2D g, int W, int H) {
            float alpha = (float)life/maxLife;
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha*color.getAlpha())));
            g.fillRect(0,0,W,H);
        }
    }

    static class LightningBolt {
        int x1,y1,x2,y2; int life=12;
        List<int[]> segments = new ArrayList<>();
        LightningBolt(int x1, int y1, int x2, int y2) {
            this.x1=x1; this.y1=y1; this.x2=x2; this.y2=y2;
            // Generate jagged path
            int steps=12; Random r=new Random();
            int px=x1,py=y1;
            for(int i=0;i<steps;i++){
                int nx=(int)(x1+(x2-x1)*(i+1f)/steps)+r.nextInt(60)-30;
                int ny=(int)(y1+(y2-y1)*(i+1f)/steps);
                segments.add(new int[]{px,py,nx,ny});
                px=nx; py=ny;
            }
        }
        void update() { life--; }
        void draw(Graphics2D g) {
            float alpha=(float)life/12f;
            // Glow
            g.setColor(new Color(150,200,255,(int)(alpha*80)));
            g.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for(int[] s:segments) g.drawLine(s[0],s[1],s[2],s[3]);
            // Core
            g.setColor(new Color(220,240,255,(int)(alpha*255)));
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for(int[] s:segments) g.drawLine(s[0],s[1],s[2],s[3]);
            g.setStroke(new BasicStroke(1));
        }
    }

    static class BuildingPiece {
        float x,y,vx,vy,rot,rotV;
        int w,h; Color color;
        BuildingPiece(int x, int y) {
            Random r=new Random();
            this.x=x; this.y=y;
            vx=(r.nextFloat()-0.5f)*8; vy=-r.nextFloat()*10-5;
            rot=r.nextFloat()*360; rotV=(r.nextFloat()-0.5f)*8;
            w=10+r.nextInt(30); h=10+r.nextInt(30);
            color=new Color(30+r.nextInt(30), 15+r.nextInt(15), 10+r.nextInt(10));
        }
        void update() { x+=vx; y+=vy; vy+=0.4f; vx*=0.98f; rot+=rotV; }
        void draw(Graphics2D g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.translate(x,y); g2.rotate(Math.toRadians(rot));
            g2.setColor(color); g2.fillRect(-w/2,-h/2,w,h);
            g2.setColor(new Color(50,25,15)); g2.drawRect(-w/2,-h/2,w,h);
            g2.dispose();
        }
    }

    static class SpaceDebris {
        float x,y,speed,rot,rotV; int size; Color color;
        SpaceDebris(float x, float y) {
            Random r=new Random();
            this.x=x; this.y=y;
            speed=0.5f+r.nextFloat()*1.5f; rot=r.nextFloat()*360; rotV=(r.nextFloat()-0.5f)*2;
            size=5+r.nextInt(20);
            color=new Color(80+r.nextInt(80), 70+r.nextInt(60), 60+r.nextInt(50), 180);
        }
        void update() { x-=speed; rot+=rotV; }
        void draw(Graphics2D g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.translate(x,y); g2.rotate(Math.toRadians(rot));
            g2.setColor(color); g2.fillOval(-size/2,-size/2,size,size);
            g2.setColor(new Color(120,110,100,100)); g2.drawOval(-size/2,-size/2,size,size);
            g2.dispose();
        }
    }

    static class EnergyOrb {
        float x,y,vx,vy; int size; float phase; boolean dead=false;
        EnergyOrb(float x, float y) {
            Random r=new Random();
            this.x=x; this.y=y;
            vx=(r.nextFloat()-0.5f)*1.5f; vy=(r.nextFloat()-0.5f)*1.5f;
            size=8+r.nextInt(16); phase=r.nextFloat()*6.28f;
        }
        void update() {
            x+=vx; y+=vy; phase+=0.05f;
            if(x<0||x>1100||y<0||y>Fighter.GROUND_Y+115) dead=true;
        }
        void draw(Graphics2D g) {
            float pulse=(float)(Math.sin(phase)*0.3f+0.7f);
            int r2=(int)(size*pulse);
            RadialGradientPaint gp=new RadialGradientPaint(x,y,r2,
                new float[]{0f,0.5f,1f},
                new Color[]{new Color(200,100,255,200),new Color(120,0,200,120),new Color(60,0,120,0)});
            g.setPaint(gp);
            g.fillOval((int)(x-r2),(int)(y-r2),r2*2,r2*2);
        }
    }

    static class HitEffect {
        int x,y,life=22; Color color; boolean blocked;
        HitEffect(int x,int y,boolean blocked) {
            this.x=x; this.y=y; this.blocked=blocked;
            color=blocked?new Color(80,160,255):new Color(255,200,50);
        }
        void update(){life--;}
        void draw(Graphics2D g){
            float alpha=life/22f;
            int size=(22-life)*4+8;
            g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),(int)(alpha*200)));
            g.setStroke(new BasicStroke(3));
            int lines=blocked?4:8;
            for(int i=0;i<lines;i++){
                double angle=Math.PI*2*i/lines+(blocked?Math.PI/4:0);
                g.drawLine(x,y,(int)(x+Math.cos(angle)*size),(int)(y+Math.sin(angle)*size));
            }
            g.fillOval(x-5,y-5,10,10);
            g.setStroke(new BasicStroke(1));
            if(blocked){
                g.setFont(new Font("Impact",Font.PLAIN,16));
                g.setColor(new Color(80,160,255,(int)(alpha*255)));
                g.drawString("BLOCKED!",x-35,y-22);
            }
        }
    }
}