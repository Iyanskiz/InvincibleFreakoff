import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private Fighter p1, p2, p1b, p2b;
    private HealthBar hb1, hb2, hb1b, hb2b;
    private InputHandler input;
    private javax.swing.Timer gameTimer;
    private Random rand = new Random();

    private int roundTimer = 99, timerTick = 0, tick = 0;
    private boolean gameOver = false, paused = false;
    private String winnerText = "";
    private boolean roundOver = false;
    private int roundOverTimer = 0;
    private int p1Wins = 0, p2Wins = 0;

    private int   gameMode;
    private int[] p1Indices, p2Indices, botIndices;
    private int   botDifficulty = 1;
    private int   p1Index, p2Index;
    private int   botActionTimer = 0, bot2ActionTimer = 0;

    // World / view constants
    public static final int VIEW_W  = 1600;
    public static final int VIEW_H  = 800;
    // Mirror Fighter constants here so GamePanel has no stale cross-class bytecode dependency
    public static final int WORLD_W  = 2400;
    public static final int GROUND_Y = 650;
    private float camTX   = 0f;   // camera translateX (screen pixels)
    private float camTY   = 0f;   // camera translateY (screen pixels)
    private float camScale = 1.0f; // camera zoom — always keeps all fighters visible
    private int   camX     = 0;    // legacy — used only in spawnFighters init

    // Platforms for current stage
    private List<Platform> platforms = new ArrayList<>();

    // Stage
    private int currentStage = 0;
    private static final int NUM_STAGES = 7;
    private int meteorTimer = 0, meteorInterval = 180;
    private int lightningTimer = 0;
    private boolean thunderFlash = false;

    // Effects
    private List<HitEffect>    effects      = new ArrayList<>();
    private List<Particle>     particles    = new ArrayList<>();
    private List<Meteor>       meteors      = new ArrayList<>();
    private List<Shockwave>    shockwaves   = new ArrayList<>();
    private List<ScreenFlash>  flashes      = new ArrayList<>();
    private List<LightningBolt> lightning   = new ArrayList<>();
    private List<BuildingPiece> buildingDebris = new ArrayList<>();
    private List<SpaceDebris>  spaceDebris  = new ArrayList<>();
    private List<EnergyOrb>    energyOrbs   = new ArrayList<>();

    public GamePanel(int[] p1Indices, int[] p2Indices, int gameMode, int[] botIndices, int botDifficulty) {
        this.p1Indices     = p1Indices;
        this.p2Indices     = p2Indices;
        this.gameMode      = gameMode;
        this.botIndices    = botIndices;
        this.botDifficulty = botDifficulty;
        this.p1Index       = p1Indices[0];
        this.p2Index       = (gameMode==2||gameMode==3) ? botIndices[0] : p2Indices[0];

        currentStage = rand.nextInt(NUM_STAGES);
        // Size set by Frame
        setFocusable(true);

        input = new InputHandler();
        addKeyListener(input);
        addKeyListener(this);

        hb1  = new HealthBar(30,  30, 600, 28, true);
        hb2  = new HealthBar(970, 30, 600, 28, false);
        hb1b = new HealthBar(30,  64, 600, 20, true);
        hb2b = new HealthBar(970, 64, 600, 20, false);

        for (int i = 0; i < 25; i++) spaceDebris.add(new SpaceDebris(rand.nextInt(VIEW_W), rand.nextInt(450)));
        for (int i = 0; i < 10; i++) energyOrbs.add(new EnergyOrb(rand.nextInt(VIEW_W), 200 + rand.nextInt(350)));

        gameTimer = new javax.swing.Timer(16, this);
    }

    public void startGame() { spawnFighters(); gameTimer.start(); }

    private void spawnFighters() {
        int mid = WORLD_W / 2;
        p1  = createFighter(p1Index, mid - 300, true);
        p2  = createFighter((gameMode==2||gameMode==3)?botIndices[0]:p2Indices[0], mid + 180, false);
        hb1.setFighterName(p1.name);
        hb2.setFighterName(p2.name);
        p1b = null; p2b = null;
        if (gameMode == 1 || gameMode == 3) {
            int p1bIdx = p1Indices.length > 1 ? p1Indices[1] : p1Indices[0];
            int p2bIdx = (gameMode == 3) ? (botIndices.length > 1 ? botIndices[1] : botIndices[0])
                                         : (p2Indices.length > 1 ? p2Indices[1] : p2Indices[0]);
            p1b = createFighter(p1bIdx, mid - 180, true);
            p2b = createFighter(p2bIdx, mid + 300, false);
            hb1b.setFighterName(p1b.name);
            hb2b.setFighterName(p2b.name);
        }
        camX = Math.max(0, mid - VIEW_W / 2);
        // Reset bot state so it doesn't carry stale velocity from last round
        bot1VelX = 0; bot2VelX = 0;
        bot1AttackCooldown = 0; bot2AttackCooldown = 0;
        bot1JumpCooldown   = 0; bot2JumpCooldown   = 0;
        bot1SpecCooldown   = 0; bot2SpecCooldown   = 0;
        bot1BlockTimer     = 0; bot2BlockTimer     = 0;
        bot1TargetX = mid + 180; bot2TargetX = mid + 300;
        platforms = buildPlatforms(currentStage);
        roundOver = false; roundTimer = 99; timerTick = 0;
        meteorTimer = 0; meteorInterval = 180;
        meteors.clear(); buildingDebris.clear(); particles.clear();
        shockwaves.clear(); flashes.clear(); effects.clear();
    }

    /** Build platforms across the full 2400px world */
    private List<Platform> buildPlatforms(int stage) {
        List<Platform> list = new ArrayList<>();
        Platform.PlatformStyle sty;
        switch(stage) {
            case 0:  sty = Platform.PlatformStyle.CITY;    break;
            case 1:  sty = Platform.PlatformStyle.SPACE;   break;
            case 2:  sty = Platform.PlatformStyle.STORM;   break;
            case 3:  sty = Platform.PlatformStyle.VOID;    break;
            case 4:  sty = Platform.PlatformStyle.VOLCANO; break;
            case 5:  sty = Platform.PlatformStyle.THRONE;  break;
            default: sty = Platform.PlatformStyle.ARCTIC;  break;
        }
        // World is 2400px wide; platforms scattered across the full length
        switch(stage) {
            case 0: // City — staircase ledges across the skyline
                list.add(new Platform(200,  490, 220, sty));
                list.add(new Platform(500,  400, 200, sty));
                list.add(new Platform(800,  310, 240, sty));
                list.add(new Platform(1100, 400, 200, sty));
                list.add(new Platform(1400, 490, 220, sty));
                list.add(new Platform(1700, 390, 200, sty));
                list.add(new Platform(2000, 480, 220, sty));
                list.add(new Platform(350,  540, 150, sty));
                list.add(new Platform(950,  540, 150, sty));
                list.add(new Platform(1550, 540, 150, sty));
                break;
            case 1: // Space — floating islands
                list.add(new Platform(100,  430, 200, sty));
                list.add(new Platform(450,  330, 180, sty));
                list.add(new Platform(800,  240, 200, sty));
                list.add(new Platform(1150, 330, 180, sty));
                list.add(new Platform(1500, 430, 200, sty));
                list.add(new Platform(1850, 320, 180, sty));
                list.add(new Platform(2150, 430, 200, sty));
                list.add(new Platform(625,  510, 150, sty));
                list.add(new Platform(1300, 510, 150, sty));
                list.add(new Platform(1975, 510, 150, sty));
                break;
            case 2: // Storm — uneven cliffs
                list.add(new Platform(80,   450, 260, sty));
                list.add(new Platform(440,  350, 180, sty));
                list.add(new Platform(760,  260, 260, sty));
                list.add(new Platform(1100, 350, 200, sty));
                list.add(new Platform(1420, 260, 260, sty));
                list.add(new Platform(1750, 350, 180, sty));
                list.add(new Platform(2060, 450, 260, sty));
                list.add(new Platform(290,  540, 140, sty));
                list.add(new Platform(930,  540, 140, sty));
                list.add(new Platform(1610, 540, 140, sty));
                break;
            case 3: // Void — wide gaps, dramatic drops
                list.add(new Platform(60,   410, 180, sty));
                list.add(new Platform(380,  290, 160, sty));
                list.add(new Platform(720,  210, 280, sty));
                list.add(new Platform(1100, 290, 160, sty));
                list.add(new Platform(1440, 210, 280, sty));
                list.add(new Platform(1800, 290, 160, sty));
                list.add(new Platform(2120, 410, 180, sty));
                list.add(new Platform(240,  510, 130, sty));
                list.add(new Platform(960,  510, 130, sty));
                list.add(new Platform(1700, 510, 130, sty));
                list.add(new Platform(860,  440, 160, sty));
                list.add(new Platform(1600, 440, 160, sty));
                break;
            case 4: // Volcano — lava rocks
                list.add(new Platform(120,  470, 190, sty));
                list.add(new Platform(420,  360, 170, sty));
                list.add(new Platform(740,  270, 280, sty));
                list.add(new Platform(1100, 360, 170, sty));
                list.add(new Platform(1440, 270, 260, sty));
                list.add(new Platform(1780, 360, 170, sty));
                list.add(new Platform(2080, 470, 190, sty));
                list.add(new Platform(300,  540, 130, sty));
                list.add(new Platform(980,  540, 130, sty));
                list.add(new Platform(1660, 540, 130, sty));
                list.add(new Platform(620,  460, 120, sty));
                list.add(new Platform(1300, 460, 120, sty));
                break;
            case 5: // Throne — grand symmetrical terraces
                list.add(new Platform(150,  490, 210, sty));
                list.add(new Platform(480,  380, 200, sty));
                list.add(new Platform(820,  270, 240, sty));
                list.add(new Platform(1180, 380, 200, sty));
                list.add(new Platform(1520, 270, 240, sty));
                list.add(new Platform(1860, 380, 200, sty));
                list.add(new Platform(2150, 490, 210, sty));
                list.add(new Platform(340,  550, 150, sty));
                list.add(new Platform(1000, 550, 150, sty));
                list.add(new Platform(1680, 550, 150, sty));
                break;
            case 6: // Arctic — ice shelves
                list.add(new Platform(80,   460, 200, sty));
                list.add(new Platform(420,  340, 200, sty));
                list.add(new Platform(780,  250, 260, sty));
                list.add(new Platform(1140, 340, 200, sty));
                list.add(new Platform(1500, 250, 260, sty));
                list.add(new Platform(1860, 340, 200, sty));
                list.add(new Platform(2160, 460, 200, sty));
                list.add(new Platform(270,  520, 130, sty));
                list.add(new Platform(960,  520, 130, sty));
                list.add(new Platform(1660, 520, 130, sty));
                list.add(new Platform(1230, 440, 130, sty));
                break;
        }
        return list;
    }

    private Fighter createFighter(int index, int x, boolean isP1) {
        switch (index) {
            case 0: return new Invincible(x, isP1);
            case 1: return new OmniMan(x, isP1);
            case 2: return new Thragg(x, isP1);
            case 3: return new Conquest(x, isP1);
            case 4: return new Anissa(x, isP1);
            default: return new Invincible(x, isP1);
        }
    }

    @Override public void actionPerformed(ActionEvent e) {
        if (!paused && !gameOver) update();
        tick++;
        repaint();
    }

    private void update() {
        if (roundOver) {
            roundOverTimer--;
            updateStageEvents();
            for (Meteor m : meteors) m.update(this);
            for (Particle p : particles) p.update();
            for (Shockwave s : shockwaves) s.update();
            for (ScreenFlash f : flashes) f.update();
            for (BuildingPiece b : buildingDebris) b.update();
            meteors.removeIf(m -> m.dead);
            particles.removeIf(p -> p.life <= 0);
            shockwaves.removeIf(s -> s.life <= 0);
            flashes.removeIf(f -> f.life <= 0);
            buildingDebris.removeIf(b -> b.y > 750);
            if (roundOverTimer <= 0) {
                if (p1Wins >= 2 || p2Wins >= 2) {
                    gameOver   = true;
                    winnerText = (p1Wins >= 2) ? p1.name + " WINS!" : p2.name + " WINS!";
                } else {
                    currentStage = rand.nextInt(NUM_STAGES);
                    spawnFighters();
                }
            }
            return;
        }

        timerTick++;
        if (timerTick >= 60) { timerTick = 0; roundTimer--; if (roundTimer <= 0) endRound(p1.currentHealth > p2.currentHealth ? p1 : p2); }

        // P1 input
        if (input.p1Left())  p1.moveLeft();
        if (input.p1Right()) p1.moveRight();
        if (input.p1Block() && input.p1Jump()) p1.dropDown();
        else if (input.p1Jump())  p1.jump();
        p1.setBlockHeld(input.p1Block() && !input.p1Jump());
        if (input.p1Light())   p1.lightAttack();
        if (input.p1Heavy())   p1.heavyAttack();
        if (input.p1Special()) p1.specialMove();

        // P2 input (human) or bot
        if (gameMode == 0 || gameMode == 1) {
            if (input.p2Left())  p2.moveLeft();
            if (input.p2Right()) p2.moveRight();
            if (input.p2Block() && input.p2Jump()) p2.dropDown();
            else if (input.p2Jump())  p2.jump();
            p2.setBlockHeld(input.p2Block() && !input.p2Jump());
            if (input.p2Light())   p2.lightAttack();
            if (input.p2Heavy())   p2.heavyAttack();
            if (input.p2Special()) p2.specialMove();
        } else {
            Fighter botTarget = nearestLiving(p2, p1, p1b);
            if (botTarget != null) runBotAI(p2, botTarget, botActionTimer++);
        }

        // 2v2 players 3 & 4
        if (gameMode == 1) {
            if (p1b != null) {
                if (input.p3Left())  p1b.moveLeft();
                if (input.p3Right()) p1b.moveRight();
                if (input.p3Block() && input.p3Jump()) p1b.dropDown();
                else if (input.p3Jump())  p1b.jump();
                p1b.setBlockHeld(input.p3Block() && !input.p3Jump());
                if (input.p3Light())   p1b.lightAttack();
                if (input.p3Heavy())   p1b.heavyAttack();
                if (input.p3Special()) p1b.specialMove();
            }
            if (p2b != null) {
                if (input.p4Left())  p2b.moveLeft();
                if (input.p4Right()) p2b.moveRight();
                if (input.p4Block() && input.p4Jump()) p2b.dropDown();
                else if (input.p4Jump())  p2b.jump();
                p2b.setBlockHeld(input.p4Block() && !input.p4Jump());
                if (input.p4Light())   p2b.lightAttack();
                if (input.p4Heavy())   p2b.heavyAttack();
                if (input.p4Special()) p2b.specialMove();
            }
        }
        // 2v2 bot: p1b human, p2b bot
        if (gameMode == 3) {
            if (p1b != null) {
                if (input.p3Left())  p1b.moveLeft();
                if (input.p3Right()) p1b.moveRight();
                if (input.p3Block() && input.p3Jump()) p1b.dropDown();
                else if (input.p3Jump())  p1b.jump();
                p1b.setBlockHeld(input.p3Block() && !input.p3Jump());
                if (input.p3Light())   p1b.lightAttack();
                if (input.p3Heavy())   p1b.heavyAttack();
                if (input.p3Special()) p1b.specialMove();
            }
            // p2 bot targets nearest living team-1 fighter
            if (p2 != null && !p2.isDead()) {
                Fighter t2 = nearestLiving(p2, p1, p1b);
                if (t2 != null) runBotAI(p2, t2, botActionTimer++);
            }
            // p2b bot targets nearest living team-1 fighter
            if (p2b != null && !p2b.isDead()) {
                Fighter t2b = nearestLiving(p2b, p1, p1b);
                if (t2b != null) runBotAI(p2b, t2b, bot2ActionTimer++);
            }
        }

        if (input.pausePressed()) paused = !paused;

        // Nearest living enemy for each fighter (used for facing direction)
        Fighter p1Near  = nearestLiving(p1,  p2, p2b);
        Fighter p2Near  = nearestLiving(p2,  p1, p1b);
        Fighter p1bNear = (p1b != null) ? nearestLiving(p1b, p2, p2b) : null;
        Fighter p2bNear = (p2b != null) ? nearestLiving(p2b, p1, p1b) : null;

        p1.update(p1Near, platforms);
        p2.update(p2Near, platforms);
        if (p1b != null) p1b.update(p1bNear, platforms);
        if (p2b != null) p2b.update(p2bNear, platforms);

        // Camera: track the midpoint of all living fighters
        updateCamera();

        checkHits();

        boolean p1TeamDead = p1.isDead() && (p1b == null || p1b.isDead());
        boolean p2TeamDead = p2.isDead() && (p2b == null || p2b.isDead());
        if (p1TeamDead) endRound(p2);
        if (p2TeamDead) endRound(p1);

        effects.removeIf(ef -> ef.life <= 0);
        particles.removeIf(p -> p.life <= 0);
        meteors.removeIf(m -> m.dead);
        shockwaves.removeIf(s -> s.life <= 0);
        flashes.removeIf(f -> f.life <= 0);
        lightning.removeIf(l -> l.life <= 0);
        buildingDebris.removeIf(b -> b.y > 750);
        spaceDebris.removeIf(s -> s.x > VIEW_W + 20);
        energyOrbs.removeIf(o -> o.dead);

        for (HitEffect ef : effects) ef.update();
        for (Particle p : particles) p.update();
        for (Meteor m : meteors) m.update(this);
        for (Shockwave s : shockwaves) s.update();
        for (ScreenFlash f : flashes) f.update();
        for (LightningBolt l : lightning) l.update();
        for (BuildingPiece b : buildingDebris) b.update();
        for (SpaceDebris s : spaceDebris) s.update();
        for (EnergyOrb o : energyOrbs) o.update();

        updateStageEvents();
    }

    // ── Camera ────────────────────────────────────────────────────────────────

    private void updateCamera() {
        int viewW = getWidth() > 0 ? getWidth() : VIEW_W;
        int viewH = getHeight() > 0 ? getHeight() : VIEW_H;

        // ── Find bounding box of all living fighters ──────────────────────────
        Fighter[] all = {p1, p2, p1b, p2b};
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        int count = 0;
        for (Fighter f : all) {
            if (f == null || f.isDead()) continue;
            minX = Math.min(minX, f.x);
            maxX = Math.max(maxX, f.x + f.width);
            minY = Math.min(minY, f.y);
            maxY = Math.max(maxY, f.y + f.height);
            count++;
        }
        if (count == 0) return;

        // Centre of fighters in world space
        float worldCX = (minX + maxX) * 0.5f;
        float worldCY = (minY + maxY) * 0.5f;

        // Padding around fighters so they're never right at the edge
        float padX = 250f, padY = 180f;
        float spanX = Math.max(600f, (maxX - minX) + padX * 2f);
        float spanY = Math.max(500f, (maxY - minY) + padY * 2f);

        // Scale to fit both X and Y span — use whichever requires more zoom-out
        float scaleX = viewW / spanX;
        float scaleY = viewH / spanY;
        float targetScale = Math.min(scaleX, scaleY);
        targetScale = Math.max(0.5f, Math.min(1.6f, targetScale));

        // Translation so world centre maps to screen centre
        float targetTX = viewW  * 0.5f - worldCX * targetScale;
        float targetTY = viewH  * 0.5f - worldCY * targetScale;

        // Smooth lerp toward targets
        float lerp = 0.07f;
        camScale += (targetScale - camScale) * lerp;
        camTX    += (targetTX    - camTX)    * lerp;
        camTY    += (targetTY    - camTY)    * lerp;
    }

    // ── Hit detection (both hitboxes, charges special on hit) ─────────────────

    private void checkHits() {
        checkPair(p1,  p2);   checkPair(p1,  p2b);
        checkPair(p1b, p2);   checkPair(p1b, p2b);
        checkPair(p2,  p1);   checkPair(p2,  p1b);
        checkPair(p2b, p1);   checkPair(p2b, p1b);
    }

    private void checkPair(Fighter a, Fighter d) {
        if (a == null || d == null || a.isDead() || d.isDead()) return;
        if (!a.isAttacking) return;
        Rectangle hurtbox = d.getHurtbox();
        boolean hit = (a.attackHitbox  != null && a.attackHitbox.intersects(hurtbox))
                   || (a.attackHitbox2 != null && a.attackHitbox2.intersects(hurtbox));
        if (hit && !d.isHurt) {
            d.takeDamage(a.attackDamage);
            a.chargeSpecial(a.attackDamage); // attacker earns meter
            spawnHitEffect(d.x + d.width/2, d.y + d.height/2, d.isBlocking);
            if (!d.isBlocking) spawnHitParticles(d.x + d.width/2, d.y + d.height/2);
        }
    }

    private Fighter nearestLiving(Fighter bot, Fighter a, Fighter b) {
        boolean aOk = a != null && !a.isDead();
        boolean bOk = b != null && !b.isDead();
        if (!aOk && !bOk) return null;
        if (!aOk) return b;
        if (!bOk) return a;
        int da = Math.abs(bot.x - a.x);
        int db = Math.abs(bot.x - b.x);
        return da <= db ? a : b;
    }

    // Per-bot state for smooth movement
    private float bot1VelX = 0, bot2VelX = 0;
    private int   bot1AttackCooldown = 0, bot2AttackCooldown = 0;
    private int   bot1JumpCooldown   = 0, bot2JumpCooldown   = 0;
    private int   bot1SpecCooldown   = 0, bot2SpecCooldown   = 0;
    private int   bot1BlockTimer     = 0, bot2BlockTimer     = 0;
    private float bot1TargetX = 0,        bot2TargetX = 0;

    private void runBotAI(Fighter bot, Fighter target, int timer) {
        if (bot == null || bot.isDead() || target == null || target.isDead()) return;
        boolean isBot1 = (bot == p2);

        // ── Per-bot persistent state ──────────────────────────────────────
        int   atkCool  = isBot1 ? bot1AttackCooldown : bot2AttackCooldown;
        int   jmpCool  = isBot1 ? bot1JumpCooldown   : bot2JumpCooldown;
        int   specCool = isBot1 ? bot1SpecCooldown   : bot2SpecCooldown;
        int   blkTimer = isBot1 ? bot1BlockTimer     : bot2BlockTimer;

        if (atkCool  > 0) atkCool--;
        if (jmpCool  > 0) jmpCool--;
        if (specCool > 0) specCool--;
        if (blkTimer > 0) blkTimer--;

        // ── Difficulty tables ─────────────────────────────────────────────
        // attackRange: how close bot wants to be before swinging
        int[]   attackRange        = {140, 160, 175, 185};
        int[]   attackCooldownBase = { 45,  28,  14,   6};
        int[]   jumpCooldownBase   = {180, 110,  65,  35};
        int[]   specCooldownBase   = {200, 120,  70,  30};
        int[]   blockChance        = {  5,  18,  35,  65};
        // movement speed directly applied each frame
        float[] moveSpd            = {3.0f, 4.2f, 5.2f, 6.0f};

        int   range = attackRange[botDifficulty];
        int   botCX = bot.x    + bot.width  / 2;
        int   tarCX = target.x + target.width / 2;
        int   dist  = Math.abs(botCX - tarCX);

        // ── MOVEMENT: always close the gap directly, no lerp ─────────────
        // Ideal standing position: just inside attack range
        int idealDist = (int)(range * 0.72f);
        int deadzone  = 20; // don't fidget when already in position

        if (!bot.isAttacking && !bot.isHurt) {
            if (dist > idealDist + deadzone) {
                // Too far — charge directly toward target
                if (botCX < tarCX) bot.moveRight();
                else                bot.moveLeft();
            } else if (dist < idealDist - deadzone && botDifficulty >= 1) {
                // Too close — back up slightly (only medium+ bots back up)
                if (botCX < tarCX) bot.moveLeft();
                else                bot.moveRight();
            }
        }

        // ── BLOCKING ─────────────────────────────────────────────────────
        boolean incomingAttack = target.isAttacking && dist < 220;
        if (incomingAttack && blkTimer == 0 && rand.nextInt(100) < blockChance[botDifficulty]) {
            blkTimer = 14 + rand.nextInt(10);
        }
        // Only block when actively being attacked, not as a sustained stance
        bot.setBlockHeld(blkTimer > 0 && incomingAttack);

        // ── ATTACKING ────────────────────────────────────────────────────
        if (dist <= range && atkCool == 0 && !bot.isBlocking) {
            int r = rand.nextInt(10);
            if (r < 7) {
                // Light is safe and fast — bots use it most
                bot.lightAttack();
                atkCool = attackCooldownBase[botDifficulty] + rand.nextInt(12);
            } else if (r < 9) {
                // Heavy has startup — bot only uses it when close and safe
                // Harder bots use it more accurately
                if (dist < range * 0.7f || botDifficulty >= 2) {
                    bot.heavyAttack();
                    // Extra cooldown to account for the startup + recovery
                    atkCool = attackCooldownBase[botDifficulty] + 25 + rand.nextInt(15);
                } else {
                    bot.lightAttack();
                    atkCool = attackCooldownBase[botDifficulty] + rand.nextInt(12);
                }
            }
        }

        // ── SPECIAL ──────────────────────────────────────────────────────
        if (bot.specialReady() && dist <= range + 80 && specCool == 0 && !bot.isBlocking) {
            bot.specialMove();
            specCool = specCooldownBase[botDifficulty] + rand.nextInt(20);
        }

        // ── JUMPING (to reach platforms / mix up) ────────────────────────
        int heightDiff = bot.y - target.y;
        // Needs to jump: target is above, or random pressure
        int jumpBias = heightDiff > 100 ? 40 : (botDifficulty == 0 ? 2 : botDifficulty == 1 ? 5 : botDifficulty == 2 ? 10 : 18);
        if (jmpCool == 0 && rand.nextInt(100) < jumpBias) {
            bot.jump();
            jmpCool = jumpCooldownBase[botDifficulty] + rand.nextInt(30);
        }

        // ── NIGHTMARE: extra aggression layer ────────────────────────────
        if (botDifficulty == 3) {
            // Constant pressure — never stop moving toward target
            if (dist > 60 && !bot.isAttacking && !bot.isHurt) {
                if (botCX < tarCX) bot.moveRight();
                else                bot.moveLeft();
            }
            // Instant counter-attack on opponent's attack frames
            if (target.isAttacking && dist < 250 && atkCool == 0) {
                bot.heavyAttack(); atkCool = 5;
            }
            // Spam light at close range
            if (dist < 165 && atkCool == 0) { bot.lightAttack(); atkCool = 6; }
            // Perfect block
            if (target.isAttacking && dist < 230) blkTimer = 14;
            // Aggressive jump pressure
            if (jmpCool == 0 && rand.nextInt(100) < 22) { bot.jump(); jmpCool = 35; }
        }

        // ── Write back state ──────────────────────────────────────────────
        if (isBot1) {
            bot1AttackCooldown = atkCool; bot1JumpCooldown = jmpCool;
            bot1SpecCooldown   = specCool; bot1BlockTimer  = blkTimer;
        } else {
            bot2AttackCooldown = atkCool; bot2JumpCooldown = jmpCool;
            bot2SpecCooldown   = specCool; bot2BlockTimer  = blkTimer;
        }
    }

        // ── Hit detection ─────────────────────────────────────────────────────────

    void spawnMeteorImpact(int x, int y) {
        shockwaves.add(new Shockwave(x, y, new Color(255,150,50)));
        for (int i = 0; i < 40; i++) {
            float angle=(float)(Math.PI*2*i/40), speed=3+rand.nextFloat()*6;
            Color c=i%3==0?new Color(255,60,0,220):i%3==1?new Color(255,200,50,200):new Color(180,180,180,180);
            particles.add(new Particle(x,y,(float)Math.cos(angle)*speed,(float)Math.sin(angle)*speed-2,c,40+rand.nextInt(30)));
        }
        for (int i=0;i<8;i++) buildingDebris.add(new BuildingPiece(x+(rand.nextInt(80)-40),y));
        flashes.add(new ScreenFlash(new Color(255,150,50,80),12));
        damageFighterNear(p1,x,y); damageFighterNear(p2,x,y);
        if(p1b!=null) damageFighterNear(p1b,x,y);
        if(p2b!=null) damageFighterNear(p2b,x,y);
    }

    private void damageFighterNear(Fighter f,int mx,int my){
        if(f==null||f.isDead()) return;
        double d=Math.sqrt(Math.pow(f.x+f.width/2-mx,2)+Math.pow(f.y+f.height/2-my,2));
        if(d<130){f.takeDamage((int)(18*(1-d/130)));f.velX=f.x>mx?8:-8;f.velY=-10;}
    }

    private void endRound(Fighter winner) {
        if (roundOver) return;
        roundOver=true; roundOverTimer=180;
        if(winner==p1||winner==p1b) p1Wins++; else p2Wins++;
        shockwaves.add(new Shockwave(winner.x+winner.width/2,winner.y+winner.height/2,new Color(255,220,50)));
    }

    private void spawnHitEffect(int x,int y,boolean blocked){
        effects.add(new HitEffect(x,y,blocked));
        shockwaves.add(new Shockwave(x,y,blocked?new Color(80,160,255):new Color(255,200,50)));
    }
    private void spawnHitParticles(int x,int y){
        for(int i=0;i<12;i++){
            float angle=(float)(Math.PI*2*i/12),speed=2+rand.nextFloat()*4;
            Color[] cols={new Color(255,200,50,200),new Color(255,100,0,200),new Color(255,255,150,200)};
            particles.add(new Particle(x,y,(float)Math.cos(angle)*speed,(float)Math.sin(angle)*speed-1,cols[i%3],15+rand.nextInt(10)));
        }
    }

    // ── Stage events ──────────────────────────────────────────────────────────

    private void updateStageEvents() {
        switch(currentStage) {
            case 0:
                meteorTimer++;
                if(meteorTimer>=meteorInterval){meteorTimer=0;meteorInterval=120+rand.nextInt(180);meteors.add(new Meteor(100+rand.nextInt(VIEW_W-200)));}
                if(tick%3==0) particles.add(new Particle(rand.nextInt(VIEW_W),GROUND_Y+150,(rand.nextFloat()-0.5f)*2,-rand.nextFloat()*3-1,new Color(255,80+rand.nextInt(100),0,200),30+rand.nextInt(20)));
                break;
            case 1:
                for(SpaceDebris s:spaceDebris) s.x-=0.5f;
                if(rand.nextInt(60)==0) spaceDebris.add(new SpaceDebris(VIEW_W + 20,rand.nextInt(450)));
                break;
            case 2:
                lightningTimer++;
                if(lightningTimer>60+rand.nextInt(120)){
                    lightningTimer=0; thunderFlash=true;
                    int lx=rand.nextInt(VIEW_W);
                    lightning.add(new LightningBolt(lx,0,lx+(rand.nextInt(100)-50),GROUND_Y+150));
                    flashes.add(new ScreenFlash(new Color(200,220,255,55),8));
                }
                break;
            case 3:
                if(rand.nextInt(120)==0) energyOrbs.add(new EnergyOrb(rand.nextInt(VIEW_W),200+rand.nextInt(350)));
                if(tick%5==0) particles.add(new Particle(rand.nextInt(VIEW_W),rand.nextInt(VIEW_H),(rand.nextFloat()-0.5f),(rand.nextFloat()-0.5f),new Color(150,0,255,Math.max(0,Math.min(255,100+rand.nextInt(100)))),40+rand.nextInt(30)));
                break;
            case 4: // Volcano — lava embers constantly rising
                if(tick%2==0){
                    int lx=rand.nextInt(VIEW_W);
                    particles.add(new Particle(lx,GROUND_Y+150,(rand.nextFloat()-0.5f)*3,-rand.nextFloat()*5-2,
                        rand.nextInt(2)==0?new Color(255,50,0,220):new Color(255,180,0,200),25+rand.nextInt(20)));
                }
                // Lava meteor eruptions
                meteorTimer++;
                if(meteorTimer>=meteorInterval){meteorTimer=0;meteorInterval=90+rand.nextInt(120);
                    meteors.add(new Meteor(rand.nextInt(200)));
                    meteors.add(new Meteor(1080+rand.nextInt(200)));
                }
                break;
            case 5: // Viltrumite Throne Room — crackling energy orbs
                if(rand.nextInt(80)==0) energyOrbs.add(new EnergyOrb(rand.nextInt(VIEW_W),200+rand.nextInt(350)));
                if(tick%8==0){
                    Color gc=rand.nextInt(2)==0?new Color(255,180,0,120):new Color(200,0,0,100);
                    particles.add(new Particle(rand.nextInt(VIEW_W),GROUND_Y+150,(rand.nextFloat()-0.5f)*1.5f,-rand.nextFloat()*2-0.5f,gc,50+rand.nextInt(30)));
                }
                break;
            case 6: // Antarctic — blizzard snow particles
                for(int i=0;i<4;i++){
                    particles.add(new Particle(rand.nextInt(VIEW_W),-5,
                        (rand.nextFloat()-0.3f)*2, rand.nextFloat()*3+1,
                        new Color(200,220,255,160+rand.nextInt(80)), 60+rand.nextInt(40)));
                }
                // Occasional lightning (blizzard crackle)
                lightningTimer++;
                if(lightningTimer>120+rand.nextInt(200)){
                    lightningTimer=0;
                    int lx=rand.nextInt(VIEW_W);
                    lightning.add(new LightningBolt(lx,0,lx+(rand.nextInt(60)-30),GROUND_Y+150));
                    flashes.add(new ScreenFlash(new Color(180,210,255,30),5));
                }
                break;
        }
    }

    // ── Paint ─────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int W = getWidth(), H = getHeight();

        // Always fill the entire panel first — prevents any white gaps
        g.setColor(new Color(4, 4, 18));
        g.fillRect(0, 0, W, H);

        // ── Background + world — both use the same camera transform ────────────
        drawBackground(g, W, H);

        // Camera transform: translate then scale so fighters are always centred
        java.awt.geom.AffineTransform screenBase = g.getTransform();
        g.translate(camTX, camTY);
        g.scale(camScale, camScale);

        for (Platform p   : platforms)    p.draw(g, tick);
        for (Particle p   : particles)    p.draw(g);
        for (SpaceDebris s: spaceDebris)  s.draw(g);
        for (EnergyOrb o  : energyOrbs)   o.draw(g);
        for (LightningBolt l: lightning)  l.draw(g);
        for (BuildingPiece b: buildingDebris) b.draw(g);
        for (Meteor m     : meteors)      m.draw(g);

        if (p1  != null) { if (!p1.isDead())  p1.draw(g);  else if (gameMode==1||gameMode==3) drawTombstone(g, p1.x,  p1.y+p1.height,   p1.name); }
        if (p2  != null) { if (!p2.isDead())  p2.draw(g);  else if (gameMode==1||gameMode==3) drawTombstone(g, p2.x,  p2.y+p2.height,   p2.name); }
        if (p1b != null) { if (!p1b.isDead()) p1b.draw(g); else drawTombstone(g, p1b.x, p1b.y+p1b.height, p1b.name); }
        if (p2b != null) { if (!p2b.isDead()) p2b.draw(g); else drawTombstone(g, p2b.x, p2b.y+p2b.height, p2b.name); }

        for (Shockwave s  : shockwaves)   s.draw(g);
        for (HitEffect ef : effects)      ef.draw(g);

        // ── Restore screen space — HUD/overlays are never zoomed ─────────────
        g.setTransform(screenBase);

        for (ScreenFlash f: flashes) f.draw(g, W, H);
        if (p1 != null && p2 != null) drawHUD(g, VIEW_W, VIEW_H);
        if (roundOver) drawRoundOverlay(g, W, H);
        if (gameOver)  drawGameOverOverlay(g, W, H);
        if (paused)    drawPauseOverlay(g, W, H);
    }

    // ── Backgrounds ───────────────────────────────────────────────────────────

    private void drawBackground(Graphics2D g, int W, int H) {
        int worldW = WORLD_W;
        // Same camera transform as world objects
        java.awt.geom.AffineTransform bgBase = g.getTransform();
        g.translate(camTX, camTY);
        g.scale(camScale, camScale);
        switch(currentStage){
            case 0: drawCity(g, worldW, H);       break;
            case 1: drawSpace(g, worldW, H);      break;
            case 2: drawStorm(g, worldW, H);      break;
            case 3: drawVoid(g, worldW, H);       break;
            case 4: drawVolcano(g, worldW, H);    break;
            case 5: drawThroneRoom(g, worldW, H); break;
            case 6: drawAntarctic(g, worldW, H);  break;
        }
        g.setTransform(bgBase);
    }

    private void drawCity(Graphics2D g,int W,int H){
        GradientPaint sky=new GradientPaint(0,0,new Color(15,5,5),0,H*0.5f,new Color(60,15,5));
        g.setPaint(sky);g.fillRect(0,0,W,H);
        g.setColor(new Color(200,60,0,70));g.fillRect(0,(int)(H*0.35f),W,(int)(H*0.3f));
        g.setColor(new Color(255,255,200,90));
        for(int i=0;i<70;i++){int sx=(i*173+30)%W,sy=(i*97+10)%150;g.fillOval(sx,sy,2,2);}
        // Far buildings
        g.setColor(new Color(8,4,4));
        int[]fbx={0,0,60,60,110,110,170,170,240,240,310,310,390,390,470,470,550,550,630,630,710,710,800,800,880,880,960,960,1050,1050,1130,1130,1210,1210,1300,1300,1380,1380,1460,1460,1530,1530,1600,1600};
        int[]fby={H,410,410,378,378,400,400,362,362,385,385,350,350,375,375,345,345,368,368,338,338,362,362,335,335,358,358,345,345,368,368,340,340,360,360,348,348,372,372,340,340,360,360,H};
        g.fillPolygon(fbx,fby,fbx.length);
        // Near buildings
        g.setColor(new Color(12,6,4));
        int[]nbx={0,0,100,100,195,195,300,300,410,410,520,520,630,630,730,730,840,840,940,940,1040,1040,1140,1140,1250,1250,1360,1360,1460,1460,1560,1560,1600,1600};
        int[]nby={H,470,470,432,432,458,458,416,416,452,452,415,415,445,445,412,412,442,442,418,418,448,448,410,410,440,440,415,415,442,442,416,416,H};
        g.fillPolygon(nbx,nby,nbx.length);
        // Window lights
        for(int bx2=0;bx2<W;bx2+=32)for(int by2=430;by2<590;by2+=18){
            if(rand.nextInt(6)==0){g.setColor(rand.nextInt(3)==0?new Color(255,120,0,150):new Color(255,200,80,80));g.fillRect(bx2+rand.nextInt(22),by2,6,8);}
        }
        GradientPaint gr=new GradientPaint(0,GROUND_Y+150,new Color(35,15,10),0,H,new Color(10,4,3));
        g.setPaint(gr);g.fillRect(0,GROUND_Y+150,W,H-GROUND_Y-150);
        g.setColor(new Color(200,80,20,100));g.setStroke(new BasicStroke(3));
        g.drawLine(0,GROUND_Y+150,W,GROUND_Y+150);g.setStroke(new BasicStroke(1));
        for(int i=0;i<8;i++){int sx2=(i*200+80)%W;float a=0.28f+(float)Math.sin(tick*0.02+i)*0.08f;g.setColor(new Color(40,20,10,Math.max(0,Math.min(255,(int)(a*180)))));int sy2=GROUND_Y+100-(tick*2+i*30)%200;g.fillOval(sx2-15,sy2,42+i*4,28+i*3);}
    }

    private void drawSpace(Graphics2D g,int W,int H){
        GradientPaint sky=new GradientPaint(0,0,new Color(2,4,20),0,H,new Color(5,0,30));
        g.setPaint(sky);g.fillRect(0,0,W,H);
        g.setColor(new Color(60,0,120,28));g.fillOval(100,50,500,400);
        g.setColor(new Color(0,40,120,22));g.fillOval(700,100,400,350);
        for(int i=0;i<220;i++){int sx=(i*173+tick/3)%W,sy=(i*97+10)%(H-200);int br=Math.min(255,100+(i%5)*28);g.setColor(new Color(br,br,Math.min(255,br+40),190));g.fillOval(sx,sy,i%4==0?2:1,i%4==0?2:1);}
        RadialGradientPaint pl=new RadialGradientPaint(950,200,130,new float[]{0f,0.6f,1f},new Color[]{new Color(60,100,180),new Color(30,60,140),new Color(10,20,80)});
        g.setPaint(pl);g.fillOval(820,70,260,260);
        g.setColor(new Color(80,120,200,55));g.setStroke(new BasicStroke(9));g.drawOval(760,160,380,80);
        g.setColor(new Color(100,140,220,38));g.setStroke(new BasicStroke(4));g.drawOval(785,172,330,58);
        g.setStroke(new BasicStroke(1));
        GradientPaint gr=new GradientPaint(0,GROUND_Y+150,new Color(15,20,50),0,H,new Color(5,8,25));
        g.setPaint(gr);g.fillRect(0,GROUND_Y+150,W,H-GROUND_Y-130);
        g.setColor(new Color(60,120,255,150));g.setStroke(new BasicStroke(3));g.drawLine(0,GROUND_Y+150,W,GROUND_Y+150);
        g.setColor(new Color(40,80,180,38));g.setStroke(new BasicStroke(1));
        for(int gx=0;gx<W;gx+=65)g.drawLine(gx,GROUND_Y+150,gx,H);
        for(int gy2=GROUND_Y+150;gy2<H;gy2+=42)g.drawLine(0,gy2,W,gy2);
    }

    private void drawStorm(Graphics2D g,int W,int H){
        boolean fl=thunderFlash&&(tick%4<2);
        g.setColor(fl?new Color(75,85,115):new Color(8,10,18));g.fillRect(0,0,W,H);
        thunderFlash=false;
        for(int layer=0;layer<3;layer++){
            int alpha=78+layer*38;g.setColor(new Color(14+layer*4,17+layer*4,28+layer*7,alpha));
            int offset=(int)(tick*(0.3f+layer*0.15f))%W;
            for(int cx=-200;cx<W+200;cx+=260){int cloudX=(cx+offset)%(W+400)-200,cloudY=38+layer*28;g.fillOval(cloudX,cloudY,290,82);g.fillOval(cloudX+65,cloudY-22,185,68);g.fillOval(cloudX+145,cloudY+12,205,58);}
        }
        g.setColor(new Color(10,12,20));
        int[]rx={0,0,80,80,130,130,190,190,260,260,330,330,410,410,490,490,570,570,650,650,730,730,820,820,900,900,990,990,1070,1070,1150,1150,1240,1240,1330,1330,1420,1420,1510,1510,1600,1600};
        int[]ry={H,430,430,396,396,420,420,385,385,410,410,375,375,402,402,368,368,394,394,360,360,386,386,356,356,382,382,365,365,390,390,355,355,380,380,365,365,392,392,358,358,H};
        g.fillPolygon(rx,ry,rx.length);
        g.setColor(new Color(148,178,218,55));g.setStroke(new BasicStroke(1));
        for(int i=0;i<120;i++){int rx2=(i*47+tick*6)%W,ry2=(i*113+tick*10)%H;g.drawLine(rx2,ry2,rx2-4,ry2+18);}
        GradientPaint gr=new GradientPaint(0,GROUND_Y+150,new Color(12,14,25),0,H,new Color(5,6,12));
        g.setPaint(gr);g.fillRect(0,GROUND_Y+150,W,H-GROUND_Y-130);
        g.setColor(new Color(30,50,100,75));for(int i=0;i<7;i++)g.fillOval(i*230+30,GROUND_Y+152,158,20);
        g.setColor(new Color(80,140,255,120));g.setStroke(new BasicStroke(2));g.drawLine(0,GROUND_Y+150,W,GROUND_Y+150);g.setStroke(new BasicStroke(1));
    }

    private void drawVoid(Graphics2D g,int W,int H){
        GradientPaint sky=new GradientPaint(0,0,new Color(5,0,15),0,H,new Color(15,0,35));
        g.setPaint(sky);g.fillRect(0,0,W,H);
        for(int i=0;i<5;i++){float angle=(float)(tick*0.008f+i*Math.PI*2/5);int rx=(int)(W/2+Math.cos(angle)*(160+i*62)),ry=(int)(300+Math.sin(angle*0.7f)*(82+i*28)),size=210+i*78;
            RadialGradientPaint vp=new RadialGradientPaint(rx,ry,size,new float[]{0f,1f},new Color[]{new Color(100,0,200,14+i*4),new Color(0,0,0,0)});
            g.setPaint(vp);g.fillOval(rx-size,ry-size,size*2,size*2);}
        RadialGradientPaint portal=new RadialGradientPaint(W/2,200,155,new float[]{0f,0.4f,0.8f,1f},new Color[]{new Color(200,0,255,95),new Color(100,0,180,75),new Color(40,0,80,38),new Color(0,0,0,0)});
        g.setPaint(portal);g.fillOval(W/2-155,48,310,310);
        g.setColor(new Color(180,0,255,145));g.setStroke(new BasicStroke(4));g.drawOval(W/2-125,78,250,250);
        g.setColor(new Color(220,100,255,95));g.setStroke(new BasicStroke(2));g.drawOval(W/2-105,98,210,210);g.setStroke(new BasicStroke(1));
        GradientPaint gr=new GradientPaint(0,GROUND_Y+150,new Color(20,0,40),0,H,new Color(5,0,15));
        g.setPaint(gr);g.fillRect(0,GROUND_Y+150,W,H-GROUND_Y-130);
        g.setColor(new Color(150,0,255,115));g.setStroke(new BasicStroke(2));
        for(int i=0;i<6;i++){int cx=(i*200+55)%W;g.drawLine(cx,GROUND_Y+150,cx+(rand.nextInt(62)-30),GROUND_Y+178);}
        g.setColor(new Color(180,0,255,195));g.setStroke(new BasicStroke(3));g.drawLine(0,GROUND_Y+150,W,GROUND_Y+150);g.setStroke(new BasicStroke(1));
    }

    private void drawVolcano(Graphics2D g, int W, int H) {
        // Deep red/orange hellscape sky
        GradientPaint sky = new GradientPaint(0,0,new Color(20,2,2),0,H*0.6f,new Color(80,20,0));
        g.setPaint(sky); g.fillRect(0,0,W,H);
        // Ash clouds
        for(int i=0;i<5;i++){
            int offset=(int)(tick*(0.4f+i*0.12f))%W;
            g.setColor(new Color(28+i*4,12+i*2,8+i*2,70+i*15));
            for(int cx=-200;cx<W+200;cx+=280){
                int cloudX=(cx+offset)%(W+400)-200, cloudY=20+i*30;
                g.fillOval(cloudX,cloudY,310,90); g.fillOval(cloudX+70,cloudY-24,190,70);
            }
        }
        // Volcano cones on horizon
        g.setColor(new Color(12,5,3));
        int[] vx={0,0,140,200,330,400,530,620,760,840,980,1060,1190,1260,1390,1460,1600,1600};
        int[] vy={H,460,460,385,385,428,428,365,365,412,412,360,360,398,398,438,438,H};
        g.fillPolygon(vx,vy,vx.length);
        int[] ventX={200,400,620,840,1060,1260,1460};
        for(int vnt:ventX){
            RadialGradientPaint lava=new RadialGradientPaint(vnt,390,40,
                new float[]{0f,0.5f,1f},new Color[]{new Color(255,120,0,200),new Color(255,50,0,80),new Color(0,0,0,0)});
            g.setPaint(lava); g.fillOval(vnt-40,350,80,80);
        }
        // Lava river floor
        GradientPaint lavaPaint=new GradientPaint(0,GROUND_Y+150,new Color(180,40,0),0,H,new Color(60,10,0));
        g.setPaint(lavaPaint); g.fillRect(0,GROUND_Y+150,W,H-GROUND_Y-130);
        // Lava glow ripple
        float lavaGlow=(float)(Math.sin(tick*0.04)*0.3+0.7);
        g.setColor(new Color(255,100,0,(int)(lavaGlow*90)));
        g.setStroke(new BasicStroke(4));
        g.drawLine(0,GROUND_Y+150,W,GROUND_Y+150);
        g.setStroke(new BasicStroke(1));
        // Lava pools
        for(int i=0;i<9;i++){
            int lx=80+i*170; float ph=(float)(Math.sin(tick*0.05+i)*0.25+0.75);
            g.setColor(new Color(255,80,0,(int)(ph*130)));
            g.fillOval(lx,GROUND_Y+138,100,22);
            g.setColor(new Color(255,200,50,(int)(ph*80)));
            g.fillOval(lx+20,GROUND_Y+140,60,12);
        }
    }

    private void drawThroneRoom(Graphics2D g, int W, int H) {
        // Dark regal chamber
        GradientPaint sky=new GradientPaint(0,0,new Color(5,0,8),0,H,new Color(18,3,0));
        g.setPaint(sky); g.fillRect(0,0,W,H);
        // Stone wall texture — vertical columns
        for(int i=0;i<13;i++){
            int cx=i*130;
            g.setColor(new Color(22,8,6,200));
            g.fillRect(cx,0,10,GROUND_Y+150);
            g.setColor(new Color(35,12,8,100));
            g.fillRect(cx+2,0,6,GROUND_Y+150);
        }
        // Banners/flags
        Color[] bannerColors={new Color(180,0,0),new Color(140,100,0),new Color(100,0,0)};
        for(int i=0;i<7;i++){
            int bx=80+i*220, by=40;
            g.setColor(bannerColors[i%3]);
            int[]bpx={bx,bx+40,bx+40,bx+20,bx};
            int[]bpy={by,by,by+90,by+110,by+90};
            g.fillPolygon(bpx,bpy,5);
            g.setColor(new Color(220,170,0));
            g.setStroke(new BasicStroke(2));
            g.drawLine(bx-2,by-10,bx-2,by+110);
            g.setStroke(new BasicStroke(1));
            // Viltrumite symbol (V shape)
            g.setColor(new Color(255,210,0,200));
            g.drawLine(bx+10,by+30,bx+20,by+55);
            g.drawLine(bx+20,by+55,bx+30,by+30);
        }
        // Grand throne at center
        g.setColor(new Color(30,10,8));
        g.fillRect(550,280,180,220);
        g.setColor(new Color(50,18,12));
        g.fillRect(560,240,160,240);
        g.setColor(new Color(200,150,0));
        g.setStroke(new BasicStroke(3));
        g.drawRect(560,240,160,240);
        g.drawRect(566,246,148,228);
        g.setStroke(new BasicStroke(1));
        // Gold trim on throne
        g.setColor(new Color(255,200,0,80));
        g.fillRect(560,240,160,15);
        // Glowing energy torches on walls
        for(int i=0;i<8;i++){
            int tx=60+i*210, ty=200;
            float ph=(float)(Math.sin(tick*0.07+i)*0.4+0.6);
            RadialGradientPaint torch=new RadialGradientPaint(tx,ty,50,
                new float[]{0f,0.5f,1f},new Color[]{
                    new Color(255,150,0,(int)(ph*200)),new Color(255,60,0,(int)(ph*80)),new Color(0,0,0,0)});
            g.setPaint(torch); g.fillOval(tx-50,ty-50,100,100);
            g.setColor(new Color(255,180,0)); g.fillOval(tx-4,ty-4,8,8);
        }
        // Stone floor
        GradientPaint floor=new GradientPaint(0,GROUND_Y+150,new Color(25,8,5),0,H,new Color(10,3,2));
        g.setPaint(floor); g.fillRect(0,GROUND_Y+150,W,H-GROUND_Y-130);
        // Floor tiles
        g.setColor(new Color(35,12,8,100));
        for(int fx=0;fx<W;fx+=80) g.drawLine(fx,GROUND_Y+150,fx,H);
        for(int fy=GROUND_Y+150;fy<H;fy+=50) g.drawLine(0,fy,W,fy);
        // Gold ground line
        g.setColor(new Color(200,140,0,180));
        g.setStroke(new BasicStroke(3));
        g.drawLine(0,GROUND_Y+150,W,GROUND_Y+150);
        g.setStroke(new BasicStroke(1));
    }

    private void drawAntarctic(Graphics2D g, int W, int H) {
        // Icy blue-white blizzard sky
        GradientPaint sky=new GradientPaint(0,0,new Color(8,14,28),0,H*0.6f,new Color(20,35,60));
        g.setPaint(sky); g.fillRect(0,0,W,H);
        // Aurora borealis bands
        for(int i=0;i<4;i++){
            float phase=(float)(Math.sin(tick*0.015+i*1.1)*40);
            int ay=(int)(80+i*45+phase);
            Color aurora=i%2==0?new Color(0,200,160,28):new Color(60,120,255,22);
            g.setColor(aurora);
            g.setStroke(new BasicStroke(22+i*8));
            g.drawLine(0,ay,W,ay+(int)(Math.sin(tick*0.01+i)*30));
            g.setStroke(new BasicStroke(1));
        }
        // Blizzard fog
        g.setColor(new Color(180,200,230,18)); g.fillRect(0,0,W,H);
        // Ice cliff silhouettes
        g.setColor(new Color(12,22,38));
        int[]icx={0,0,80,80,160,160,240,240,340,340,440,440,540,540,640,640,740,740,840,840,950,950,1060,1060,1160,1160,1270,1270,1380,1380,1490,1490,1600,1600};
        int[]icy={H,400,400,368,368,390,390,352,352,378,378,345,345,372,372,338,338,362,362,345,345,368,368,338,338,360,360,348,348,370,370,342,342,H};
        g.fillPolygon(icx,icy,icx.length);
        // Snow/ice highlights on cliffs
        g.setColor(new Color(160,200,240,80));
        for(int i=0;i<icx.length-2;i+=2){
            if(icy[i]<420) g.fillRect(icx[i],icy[i],Math.max(1,icx[i+1]-icx[i]),4);
        }
        // Icy frozen ground
        GradientPaint icePaint=new GradientPaint(0,GROUND_Y+150,new Color(30,55,90),0,H,new Color(10,20,40));
        g.setPaint(icePaint); g.fillRect(0,GROUND_Y+150,W,H-GROUND_Y-130);
        // Ice crack patterns
        g.setColor(new Color(80,140,200,60));
        g.setStroke(new BasicStroke(1));
        for(int i=0;i<11;i++){
            int cx=50+i*145;
            g.drawLine(cx,GROUND_Y+150,cx+40,GROUND_Y+160);
            g.drawLine(cx+40,GROUND_Y+160,cx+10,GROUND_Y+180);
            g.drawLine(cx+40,GROUND_Y+160,cx+70,GROUND_Y+185);
        }
        // Frozen surface line
        g.setColor(new Color(140,200,255,200));
        g.setStroke(new BasicStroke(3));
        g.drawLine(0,GROUND_Y+150,W,GROUND_Y+150);
        // Glassy highlight
        g.setColor(new Color(200,230,255,50));
        g.setStroke(new BasicStroke(1));
        g.drawLine(0,GROUND_Y+131,W,GROUND_Y+131);
        g.setStroke(new BasicStroke(1));
    }



    private void drawHUD(Graphics2D g,int W,int H){
        hb1.draw(g,p1.currentHealth,p1.maxHealth);
        hb2.draw(g,p2.currentHealth,p2.maxHealth);
        if((gameMode==1||gameMode==3)&&p1b!=null&&p2b!=null){
            hb1b.draw(g,p1b.currentHealth,p1b.maxHealth);
            hb2b.draw(g,p2b.currentHealth,p2b.maxHealth);
        }
        // Timer
        int topY = (gameMode==1||gameMode==3)?65:45;
        g.setFont(new Font("Impact",Font.PLAIN,46));
        String ts=String.valueOf(roundTimer);
        FontMetrics fm=g.getFontMetrics();
        int tx=W/2-fm.stringWidth(ts)/2;
        Color tc=roundTimer>30?Color.WHITE:new Color(255,60,60);
        if(roundTimer<=10){int a=Math.max(0,Math.min(255,150+(int)(Math.sin(tick*0.3)*105)));tc=new Color(255,0,0,a);}
        g.setColor(new Color(0,0,0,150));g.drawString(ts,tx+2,topY+2);
        g.setColor(tc);g.drawString(ts,tx,topY);
        // Stage name
        if(tick<180){float a=Math.min(1f,(180-tick)/60f);String[]sn={"CITY UNDER SIEGE","THE VOID OF SPACE","STORM'S WRATH","THE DARK DIMENSION","VOLCANO'S FURY","VILTRUMITE THRONE","FROZEN WASTELAND"};
            g.setFont(new Font("Impact",Font.PLAIN,26));g.setColor(new Color(255,220,50,Math.max(0,Math.min(255,(int)(a*190)))));fm=g.getFontMetrics();g.drawString(sn[currentStage],W/2-fm.stringWidth(sn[currentStage])/2,H/2);}
        // Win pips
        for(int i=0;i<2;i++){int px=30+i*22;if(i<p1Wins){g.setColor(new Color(255,220,50));g.fillOval(px,topY+8,16,16);}else{g.setColor(new Color(60,60,60));g.drawOval(px,topY+8,16,16);}}
        for(int i=0;i<2;i++){int px=W-88+i*22;if(i<p2Wins){g.setColor(new Color(255,220,50));g.fillOval(px,topY+8,16,16);}else{g.setColor(new Color(60,60,60));g.drawOval(px,topY+8,16,16);}}
        // Block labels
        if(p1!=null&&p1.isBlocking){g.setFont(new Font("Arial",Font.BOLD,12));g.setColor(new Color(80,160,255));g.drawString("BLOCKING",30,topY+30);}
        if(p2!=null&&p2.isBlocking){g.setFont(new Font("Arial",Font.BOLD,12));FontMetrics fm2=g.getFontMetrics();g.setColor(new Color(80,160,255));g.drawString("BLOCKING",W-30-fm2.stringWidth("BLOCKING"),topY+30);}
        // Controls HUD
        g.setFont(new Font("Arial",Font.PLAIN,10));g.setColor(new Color(255,255,255,55));
        if(gameMode==0||gameMode==2){
            g.drawString("P1: WASD+F/G/H  |  DOWN+JUMP=Drop",30,topY+46);
            g.drawString("P2: Arrows+NP1/2/3  |  DOWN+JUMP=Drop",W-260,topY+46);
        } else {
            g.setColor(new Color(50,150,255,150));
            g.drawString("TEAM 1",30,topY+36);
            g.setColor(new Color(255,255,255,55));
            g.drawString("P1: WASD+FGH",30,topY+47);
            g.drawString("P3: IJKL+UOP",30,topY+58);
            g.setColor(new Color(255,80,80,150));
            g.drawString("TEAM 2",W-135,topY+36);
            g.setColor(new Color(255,255,255,55));
            g.drawString("P2: Arrows+NP123",W-185,topY+47);
            g.drawString("P4: NP4568+790",W-170,topY+58);
        }
        if(gameMode==2||gameMode==3){String[]dn={"EASY","MEDIUM","HARD","NIGHTMARE"};Color[]dc={new Color(80,200,80),new Color(255,200,50),new Color(255,100,50),new Color(200,0,255)};
            g.setFont(new Font("Impact",Font.PLAIN,12));fm=g.getFontMetrics();String dt="BOT: "+dn[botDifficulty];g.setColor(dc[botDifficulty]);g.drawString(dt,W-140,topY+46);}
    }

    private void drawRoundOverlay(Graphics2D g,int W,int H){
        g.setColor(new Color(255,255,255,Math.max(0,Math.min(40,roundOverTimer*2))));g.fillRect(0,0,W,H);
        if(gameOver) return;
        String msg=(p1Wins>p2Wins)?p1.name+" wins the round!":p2.name+" wins the round!";
        g.setFont(new Font("Impact",Font.PLAIN,58));FontMetrics fm=g.getFontMetrics();int tx=W/2-fm.stringWidth(msg)/2;
        g.setColor(Color.BLACK);g.drawString(msg,tx+3,H/2+3);
        GradientPaint gp=new GradientPaint(0,H/2f-40,new Color(255,220,50),0,H/2f+10,new Color(255,80,0));
        g.setPaint(gp);g.drawString(msg,tx,H/2);
    }

    private void drawGameOverOverlay(Graphics2D g,int W,int H){
        g.setColor(new Color(0,0,0,188));g.fillRect(0,0,W,H);
        g.setFont(new Font("Impact",Font.PLAIN,76));FontMetrics fm=g.getFontMetrics();int tx=W/2-fm.stringWidth(winnerText)/2;
        GradientPaint gp=new GradientPaint(0,H/2f-52,new Color(255,220,50),0,H/2f+34,new Color(255,60,0));
        g.setPaint(gp);g.drawString(winnerText,tx,H/2);
        g.setFont(new Font("Arial",Font.BOLD,20));g.setColor(new Color(255,255,255,175));
        String r="Press ENTER to return to menu";fm=g.getFontMetrics();
        if((tick/30)%2==0) g.drawString(r,W/2-fm.stringWidth(r)/2,H/2+65);
    }

    private void drawPauseOverlay(Graphics2D g,int W,int H){
        g.setColor(new Color(0,0,0,148));g.fillRect(0,0,W,H);
        g.setFont(new Font("Impact",Font.PLAIN,68));g.setColor(Color.WHITE);g.drawString("PAUSED",W/2-108,H/2);
        g.setFont(new Font("Arial",Font.PLAIN,16));g.setColor(new Color(200,200,200));g.drawString("Press ESC to resume",W/2-80,H/2+40);
    }

    @Override
    public void keyPressed(KeyEvent e){
        if(e.getKeyCode()==KeyEvent.VK_ENTER&&gameOver){
            gameTimer.stop(); input.clear();
            JFrame frame=(JFrame)SwingUtilities.getWindowAncestor(this);
            if(frame!=null){MainMenu m=new MainMenu(frame);frame.setContentPane(m);frame.revalidate();m.requestFocusInWindow();}
        }
        if(e.getKeyCode()==KeyEvent.VK_ESCAPE&&!gameOver) paused=!paused;
    }
    @Override public void keyReleased(KeyEvent e){}
    @Override public void keyTyped(KeyEvent e){}

    // ── Draw tombstone for dead 2v2 fighter ──────────────────────────────────

    private void drawTombstone(Graphics2D g, int x, int groundY, String name) {
        int tx = x, ty = groundY - 90;
        // Stone base
        g.setColor(new Color(30, 30, 30));
        g.fillRoundRect(tx + 15, ty + 70, 60, 20, 4, 4);
        // Stone slab
        g.setColor(new Color(80, 80, 88));
        g.fillRoundRect(tx + 20, ty + 10, 52, 65, 10, 10);
        g.setColor(new Color(100, 100, 110));
        g.fillRoundRect(tx + 22, ty + 12, 48, 62, 9, 9);
        // Arch top
        g.setColor(new Color(90, 90, 98));
        g.fillArc(tx + 20, ty, 52, 30, 0, 180);
        g.setColor(new Color(105, 105, 115));
        g.fillArc(tx + 22, ty + 2, 48, 26, 0, 180);
        // Cross
        g.setColor(new Color(55, 55, 62));
        g.fillRect(tx + 43, ty + 20, 6, 30);
        g.fillRect(tx + 33, ty + 28, 26, 6);
        // RIP text
        g.setFont(new Font("Impact", Font.PLAIN, 10));
        g.setColor(new Color(40, 40, 44));
        FontMetrics fm = g.getFontMetrics();
        String rip = "R.I.P";
        g.drawString(rip, tx + 46 - fm.stringWidth(rip)/2, ty + 55);
        // Name (truncated)
        g.setFont(new Font("Arial", Font.BOLD, 8));
        fm = g.getFontMetrics();
        String shortName = name.length() > 8 ? name.substring(0,8) : name;
        g.setColor(new Color(50, 50, 55));
        g.drawString(shortName, tx + 46 - fm.stringWidth(shortName)/2, ty + 66);
        // Shadow
        g.setColor(new Color(0, 0, 0, 55));
        g.fillOval(tx + 10, groundY + 5, 72, 12);
    }

    // ══ Inner effect classes ══════════════════════════════════════════════════

    static class Meteor {
        float x,y,vx,vy; int size; boolean dead=false;
        List<Particle> trail=new ArrayList<>(); Random r=new Random();
        Meteor(int sx){x=sx;y=-60;vx=(r.nextFloat()-0.5f)*4;vy=8+r.nextFloat()*5;size=20+r.nextInt(25);}
        void update(GamePanel gp){
            x+=vx;y+=vy;
            for(int i=0;i<3;i++) trail.add(new Particle((int)x,(int)y,(r.nextFloat()-0.5f)*2,-r.nextFloat()*2,r.nextInt(3)==0?new Color(255,80,0,200):new Color(255,200,50,180),15+r.nextInt(10)));
            trail.removeIf(p->p.life<=0); for(Particle p:trail) p.update();
            if(y>=GROUND_Y+150){dead=true;gp.spawnMeteorImpact((int)x,(int)y);}
        }
        void draw(Graphics2D g){
            for(Particle p:trail) p.draw(g);
            g.setColor(new Color(40,20,10));g.fillOval((int)x-size,(int)y-size,size*2,size*2);
            RadialGradientPaint gl=new RadialGradientPaint(x,y,size*1.5f,new float[]{0f,0.5f,1f},new Color[]{new Color(255,200,50,200),new Color(255,80,0,100),new Color(255,0,0,0)});
            g.setPaint(gl);g.fillOval((int)(x-size*1.5f),(int)(y-size*1.5f),(int)(size*3),(int)(size*3));
        }
    }

    static class Particle {
        float x,y,vx,vy; Color color; int life,maxLife;
        Particle(int x,int y,float vx,float vy,Color c,int life){this.x=x;this.y=y;this.vx=vx;this.vy=vy;color=c;this.life=life;maxLife=life;}
        void update(){x+=vx;y+=vy;vy+=0.15f;life--;vx*=0.96f;}
        void draw(Graphics2D g){float a=(float)life/maxLife;int al=Math.max(0,Math.min(255,(int)(a*color.getAlpha())));g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),al));int s=Math.max(1,(int)(3*a));g.fillOval((int)x-s/2,(int)y-s/2,s,s);}
    }

    static class Shockwave {
        int x,y,life=25; Color color;
        Shockwave(int x,int y,Color c){this.x=x;this.y=y;color=c;}
        void update(){life--;}
        void draw(Graphics2D g){float pct=1f-(float)life/25f;int r=(int)(pct*125);int a=Math.max(0,(int)((1-pct)*200));g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),a));g.setStroke(new BasicStroke(3*(1-pct)+1));g.drawOval(x-r,y-r,r*2,r*2);g.setStroke(new BasicStroke(1));}
    }

    static class ScreenFlash {
        Color color; int life,maxLife;
        ScreenFlash(Color c,int l){color=c;life=l;maxLife=l;}
        void update(){life--;}
        void draw(Graphics2D g,int W,int H){float a=(float)life/maxLife;int al=Math.max(0,Math.min(255,(int)(a*color.getAlpha())));g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),al));g.fillRect(0,0,W,H);}
    }

    static class LightningBolt {
        List<int[]> segs=new ArrayList<>(); int life=12;
        LightningBolt(int x1,int y1,int x2,int y2){
            Random r=new Random();int px=x1,py=y1;
            for(int i=0;i<12;i++){int nx=(int)(x1+(x2-x1)*(i+1f)/12)+r.nextInt(62)-30,ny=(int)(y1+(y2-y1)*(i+1f)/12);segs.add(new int[]{px,py,nx,ny});px=nx;py=ny;}
        }
        void update(){life--;}
        void draw(Graphics2D g){float a=(float)life/12f;g.setColor(new Color(150,200,255,Math.max(0,(int)(a*75))));g.setStroke(new BasicStroke(8,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));for(int[]s:segs)g.drawLine(s[0],s[1],s[2],s[3]);g.setColor(new Color(220,240,255,Math.max(0,(int)(a*255))));g.setStroke(new BasicStroke(2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));for(int[]s:segs)g.drawLine(s[0],s[1],s[2],s[3]);g.setStroke(new BasicStroke(1));}
    }

    static class BuildingPiece {
        float x,y,vx,vy,rot,rotV; int w,h; Color color;
        BuildingPiece(int x,int y){Random r=new Random();this.x=x;this.y=y;vx=(r.nextFloat()-0.5f)*8;vy=-r.nextFloat()*10-5;rot=r.nextFloat()*360;rotV=(r.nextFloat()-0.5f)*8;w=10+r.nextInt(30);h=10+r.nextInt(30);color=new Color(30+r.nextInt(28),14+r.nextInt(14),10+r.nextInt(10));}
        void update(){x+=vx;y+=vy;vy+=0.4f;vx*=0.98f;rot+=rotV;}
        void draw(Graphics2D g){Graphics2D g2=(Graphics2D)g.create();g2.translate(x,y);g2.rotate(Math.toRadians(rot));g2.setColor(color);g2.fillRect(-w/2,-h/2,w,h);g2.setColor(new Color(50,25,15));g2.drawRect(-w/2,-h/2,w,h);g2.dispose();}
    }

    static class SpaceDebris {
        float x,y,speed,rot,rotV; int size; Color color;
        SpaceDebris(float x,float y){Random r=new Random();this.x=x;this.y=y;speed=0.5f+r.nextFloat()*1.5f;rot=r.nextFloat()*360;rotV=(r.nextFloat()-0.5f)*2;size=5+r.nextInt(20);color=new Color(78+r.nextInt(78),68+r.nextInt(58),58+r.nextInt(48),175);}
        void update(){x-=speed;rot+=rotV;}
        void draw(Graphics2D g){Graphics2D g2=(Graphics2D)g.create();g2.translate(x,y);g2.rotate(Math.toRadians(rot));g2.setColor(color);g2.fillOval(-size/2,-size/2,size,size);g2.setColor(new Color(120,110,100,95));g2.drawOval(-size/2,-size/2,size,size);g2.dispose();}
    }

    static class EnergyOrb {
        float x,y,vx,vy; int size; float phase; boolean dead=false;
        EnergyOrb(float x,float y){Random r=new Random();this.x=x;this.y=y;vx=(r.nextFloat()-0.5f)*1.5f;vy=(r.nextFloat()-0.5f)*1.5f;size=8+r.nextInt(16);phase=r.nextFloat()*6.28f;}
        void update(){x+=vx;y+=vy;phase+=0.05f;if(x<0||x>1600||y<0||y>GROUND_Y+150)dead=true;}
        void draw(Graphics2D g){float pulse=(float)(Math.sin(phase)*0.3f+0.7f);int r2=(int)(size*pulse);RadialGradientPaint gp=new RadialGradientPaint(x,y,r2,new float[]{0f,0.5f,1f},new Color[]{new Color(200,100,255,200),new Color(120,0,200,120),new Color(60,0,120,0)});g.setPaint(gp);g.fillOval((int)(x-r2),(int)(y-r2),r2*2,r2*2);}
    }

    static class HitEffect {
        int x,y,life=22; Color color; boolean blocked;
        HitEffect(int x,int y,boolean blocked){this.x=x;this.y=y;this.blocked=blocked;color=blocked?new Color(80,160,255):new Color(255,200,50);}
        void update(){life--;}
        void draw(Graphics2D g){float a=(float)life/22f;int size=(22-life)*4+8;g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),Math.max(0,(int)(a*200))));g.setStroke(new BasicStroke(3));int lines=blocked?4:8;for(int i=0;i<lines;i++){double angle=Math.PI*2*i/lines+(blocked?Math.PI/4:0);g.drawLine(x,y,(int)(x+Math.cos(angle)*size),(int)(y+Math.sin(angle)*size));}g.fillOval(x-5,y-5,10,10);g.setStroke(new BasicStroke(1));if(blocked){g.setFont(new Font("Impact",Font.PLAIN,16));g.setColor(new Color(80,160,255,Math.max(0,(int)(a*255))));g.drawString("BLOCKED!",x-35,y-22);}}
    }
}