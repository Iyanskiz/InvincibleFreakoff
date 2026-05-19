import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class Fighter {

    public int x, y;
    public int width = 120, height = 155;   // uniform base size

    public float velX = 0, velY = 0;
    public boolean onGround = true;
    public static final int GROUND_Y  = 650;
    public static final int WORLD_W   = 2400; // wide scrolling world
    public static final float GRAVITY    = 0.9f;
    public static final float JUMP_FORCE = -20f;

    public int maxHealth;
    public int currentHealth;
    public String name;
    public boolean facingRight = true;
    public boolean isPlayer1;

    public boolean isAttacking = false;
    public boolean isBlocking  = false;
    public boolean isHurt      = false;
    public int attackTimer  = 0;
    public int hurtTimer    = 0;
    public int attackDamage = 10;
    public int  heavyStartup = 0;   // frames before heavy attack hitbox activates
    public Rectangle attackHitbox  = null;
    public Rectangle attackHitbox2 = null; // second hitbox when enemies are on both sides

    // ── Special meter — charged by dealing damage ─────────────────────────────
    public int  specialMeter     = 0;
    public int  specialThreshold = 100;  // subclass overrides
    public boolean specialReady()  { return specialMeter >= specialThreshold; }
    public void chargeSpecial(int dmg) {
        if (!specialReady()) {
            specialMeter = Math.min(specialThreshold, specialMeter + dmg);
        }
    }
    public void consumeSpecial() { specialMeter = 0; }

    // Platform drop-through
    public boolean dropThrough = false;
    public int     dropTimer   = 0;
    public Platform standingPlatform = null; // platform currently stood on (null = floor/air)

    public enum State { IDLE, WALK_FORWARD, WALK_BACKWARD, JUMP,
                        ATTACK_LIGHT, ATTACK_HEAVY, HURT, DEAD, BLOCK, SPECIAL }
    public State currentState = State.IDLE;
    public int animFrame = 0;
    public int animTimer = 0;
    public int animSpeed = 6;

    protected Color characterColor = Color.GRAY;
    protected Color accentColor    = Color.WHITE;

    protected BufferedImage imgIdle, imgForward, imgBackward,
                             imgPunch, imgBlock, imgHit, imgLevitating;

    private boolean blockKeyHeld = false;

    public Fighter(String name, int health, int x, boolean isPlayer1) {
        this.name          = name;
        this.maxHealth     = health;
        this.currentHealth = health;
        this.x             = x;
        this.y             = GROUND_Y;
        this.isPlayer1     = isPlayer1;
        this.facingRight   = isPlayer1;
    }

    protected BufferedImage loadImage(String filename) {
        try {
            String[] paths = { "imgs/"+filename, "src/imgs/"+filename, "../imgs/"+filename, filename };
            for (String path : paths) {
                File f = new File(path);
                if (f.exists()) return ImageIO.read(f);
            }
        } catch (IOException e) {}
        return null;
    }

    public void setBlockHeld(boolean held) {
        blockKeyHeld = held;
        if (held && !isAttacking && !isHurt) {
            isBlocking = true; currentState = State.BLOCK;
        } else if (!held) {
            isBlocking = false;
            if (currentState == State.BLOCK) currentState = State.IDLE;
        }
    }

    /**
     * Full update.
     * @param nearestEnemy  the closest living enemy — used for facing direction
     * @param platforms     current stage platforms
     */
    public void update(Fighter nearestEnemy, List<Platform> platforms) {
        if (dropTimer > 0) { dropThrough = true; dropTimer--; }
        else dropThrough = false;

        // Track whether we were on a platform last frame
        boolean wasOnPlatform = standingPlatform != null;

        if (!onGround) velY += GRAVITY;
        x += (int) velX;
        y += (int) velY;

        // Main floor
        if (y >= GROUND_Y) {
            y = GROUND_Y; velY = 0; onGround = true;
            standingPlatform = null;
            if (currentState == State.JUMP) currentState = State.IDLE;
        }

        // Platform collision — land from above AND stay on while walking
        standingPlatform = null;
        if (!dropThrough) {
            for (Platform p : platforms) {
                int feetY    = y + height;
                int prevFeet = feetY - (int) velY;
                boolean overPlatform = x + width - 18 > p.x && x + 18 < p.x + p.w;

                if (overPlatform && prevFeet <= p.y + 4 && feetY >= p.y) {
                    // Landing or staying on platform
                    y = p.y - height; velY = 0; onGround = true;
                    standingPlatform = p;
                    if (currentState == State.JUMP) currentState = State.IDLE;
                    break;
                }
            }
            // If we were on a platform but walked off the edge — start falling
            if (wasOnPlatform && standingPlatform == null && y + height <= GROUND_Y && onGround) {
                // Check we're not over ANY platform
                boolean stillOver = false;
                for (Platform p : platforms) {
                    if (x + width - 18 > p.x && x + 18 < p.x + p.w
                            && y + height >= p.y && y + height <= p.y + 20) {
                        stillOver = true; break;
                    }
                }
                if (!stillOver) onGround = false; // walked off edge — gravity kicks in
            }
        }

        if (x < 0)               x = 0;
        if (x > WORLD_W - width) x = WORLD_W - width;

        // Face the nearest enemy
        if (nearestEnemy != null) facingRight = (nearestEnemy.x > this.x);

        if (blockKeyHeld && !isAttacking && !isHurt) {
            isBlocking = true; currentState = State.BLOCK;
        }

        if (isAttacking) {
            attackTimer--;
            if (heavyStartup > 0) {
                heavyStartup--;
                // During windup: suppress movement so it feels committed
                if (currentState == State.ATTACK_HEAVY) velX *= 0.5f;
            }
            if (attackTimer <= 0) {
                isAttacking = false; attackHitbox = null; attackHitbox2 = null;
                heavyStartup = 0;
                currentState = blockKeyHeld ? State.BLOCK : State.IDLE;
            }
        }

        if (isHurt) {
            hurtTimer--;
            if (hurtTimer <= 0) {
                isHurt = false;
                currentState = blockKeyHeld ? State.BLOCK : State.IDLE;
            }
        }

        if (onGround) velX *= 0.75f;
        if (Math.abs(velX) < 0.5f && onGround &&
            (currentState == State.WALK_FORWARD || currentState == State.WALK_BACKWARD))
            currentState = blockKeyHeld ? State.BLOCK : State.IDLE;

        animTimer++;
        if (animTimer >= animSpeed) { animTimer = 0; animFrame++; }

        updateAttackHitboxes();
    }

    /** Convenience: no platforms */
    public void update(Fighter nearestEnemy) {
        update(nearestEnemy, java.util.Collections.emptyList());
    }

    public void lightAttack() {
        if (!isAttacking && !isHurt && !isBlocking) {
            isAttacking = true; attackTimer = 20; attackDamage = 8;
            currentState = State.ATTACK_LIGHT; animFrame = 0;
        }
    }

    public void heavyAttack() {
        if (!isAttacking && !isHurt && !isBlocking) {
            isAttacking   = true;
            heavyStartup  = 18;  // 18 frames of windup (~300ms) before the hit lands
            attackTimer   = 50;  // total duration: 18 startup + 32 active/recovery
            attackDamage  = 22;  // slightly more damage than before
            currentState  = State.ATTACK_HEAVY;
            animFrame     = 0;
            // Slight root during windup — slow the fighter down
            velX *= 0.3f;
        }
    }

    public void jump() {
        if (onGround && !isBlocking) {
            velY = JUMP_FORCE; onGround = false; currentState = State.JUMP;
        }
    }

    public void dropDown() {
        if (onGround) {
            dropThrough = true; dropTimer = 18; onGround = false; velY = 3f;
        }
    }

    public void moveLeft() {
        if (!isAttacking && !isHurt && !isBlocking) {
            velX = -5;
            if (onGround) currentState = facingRight ? State.WALK_BACKWARD : State.WALK_FORWARD;
        }
    }

    public void moveRight() {
        if (!isAttacking && !isHurt && !isBlocking) {
            velX = 5;
            if (onGround) currentState = facingRight ? State.WALK_FORWARD : State.WALK_BACKWARD;
        }
    }

    public void block()     { setBlockHeld(true);  }
    public void stopBlock() { setBlockHeld(false); }

    public void takeDamage(int damage) {
        if (isBlocking) {
            currentHealth -= damage / 3;
            if (currentHealth < 0) currentHealth = 0;
            isHurt = true; hurtTimer = 6;
            velX = facingRight ? -3 : 3;
            return;
        }
        currentHealth -= damage;
        if (currentHealth < 0) currentHealth = 0;
        isHurt = true; hurtTimer = 15;
        currentState = State.HURT;
        velX = facingRight ? -7 : 7;
        velY = -5;
        onGround = false;
    }

    public void applyKnockback(boolean attackerFacingRight, boolean isHeavy) {
        if (isBlocking) return;
        float kx = attackerFacingRight ? 10 : -10;
        float ky = -6;
        if (isHeavy) { kx *= 1.8f; ky = -10; hurtTimer = 28; }
        velX = kx;
        velY = ky;
        onGround = false;
    }

    public boolean isDead() { return currentHealth <= 0; }

    /**
     * Build up to TWO hitboxes so a fighter can hit enemies on both sides.
     * Primary hitbox always points toward nearestEnemy (facingRight).
     * Secondary hitbox fires the other direction at reduced reach if an enemy is back there.
     */
    private void updateAttackHitboxes() {
        attackHitbox  = null;
        attackHitbox2 = null;
        if (!isAttacking) return;

        // During heavy startup — fighter is winding up, no hitbox yet
        if (heavyStartup > 0) return;

        boolean heavy = (currentState == State.ATTACK_HEAVY || currentState == State.SPECIAL);
        int reach = heavy ? 58 : 42;
        int hitH  = 42;
        int hitY  = y + height / 2 - hitH / 2;

        // Primary — toward faced direction
        if (facingRight)
            attackHitbox = new Rectangle(x + width - 15, hitY, reach, hitH);
        else
            attackHitbox = new Rectangle(x - reach + 15, hitY, reach, hitH);

        // Secondary — opposite side at half reach (wide swing)
        int reach2 = reach / 2;
        if (facingRight)
            attackHitbox2 = new Rectangle(x - reach2 + 15, hitY, reach2, hitH);
        else
            attackHitbox2 = new Rectangle(x + width - 15, hitY, reach2, hitH);
    }

    public Rectangle getHurtbox() {
        return new Rectangle(x + 20, y + 14, width - 40, height - 24);
    }

    public BufferedImage getLevitatingImage() { return imgLevitating; }

    protected BufferedImage getCurrentImage() {
        switch (currentState) {
            case WALK_FORWARD:  return imgForward    != null ? imgForward    : imgIdle;
            case WALK_BACKWARD: return imgBackward   != null ? imgBackward   : imgIdle;
            case ATTACK_LIGHT:
            case ATTACK_HEAVY:
            case SPECIAL:       return imgPunch      != null ? imgPunch      : imgIdle;
            case BLOCK:         return imgBlock      != null ? imgBlock      : imgIdle;
            case HURT:          return imgHit        != null ? imgHit        : imgIdle;
            case JUMP:          return imgLevitating != null ? imgLevitating : imgIdle;
            default:            return imgIdle;
        }
    }

    // ── Draw the special meter bar above the fighter ──────────────────────────
    protected void drawSpecialMeter(Graphics2D g) {
        int bw = width;
        int bh = 5;
        int bx = x;
        int by = y - 32;

        // Background
        g.setColor(new Color(20, 20, 20, 180));
        g.fillRoundRect(bx, by, bw, bh, bh, bh);

        if (specialMeter > 0) {
            float pct = (float) specialMeter / specialThreshold;
            int fillW = (int)(bw * pct);
            Color barCol = specialReady()
                ? new Color(255, 215, 0)   // gold when ready
                : new Color(80, 120, 255); // blue while charging
            g.setColor(barCol);
            g.fillRoundRect(bx, by, fillW, bh, bh, bh);
            // Shine
            g.setColor(new Color(255, 255, 255, 60));
            g.fillRoundRect(bx, by, fillW, bh / 2, bh, bh);
        }

        // Outline
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(bx, by, bw, bh, bh, bh);
        g.setStroke(new BasicStroke(1));

        // "SPECIAL READY" flash
        if (specialReady()) {
            g.setFont(new Font("Impact", Font.PLAIN, 11));
            g.setColor(new Color(255, 215, 0, 200));
            g.drawString("SPECIAL READY", bx, by - 4);
        }
    }

    public void draw(Graphics2D g) {
        // Ground shadow
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(x + 10, y + height, width - 20, 10);

        BufferedImage img = getCurrentImage();
        if (img != null) {
            if (isHurt && hurtTimer % 4 < 2) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
                g2.setColor(Color.WHITE);
                if (!facingRight) g2.drawImage(img, x + width, y, -width, height, null);
                else              g2.drawImage(img, x, y, width, height, null);
                g2.dispose();
            } else {
                if (!facingRight) g.drawImage(img, x + width, y, -width, height, null);
                else              g.drawImage(img, x, y, width, height, null);
            }
            if (isBlocking) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
                g2.setColor(new Color(80, 160, 255));
                g2.fillRoundRect(x, y, width, height, 20, 20);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2.setColor(new Color(80, 160, 255));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(x, y, width, height, 20, 20);
                g2.dispose();
            }
        } else {
            g.setColor(characterColor);
            g.fillRoundRect(x + 10, y, width - 20, height, 10, 10);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(name, x + 15, y + height / 2);
        }

        // Heavy attack windup glow — red aura builds during startup
        if (currentState == State.ATTACK_HEAVY && heavyStartup > 0) {
            float charge = 1f - (heavyStartup / 18f); // 0→1 as startup counts down
            int alpha = (int)(charge * 160);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, charge * 0.45f));
            g2.setColor(new Color(255, 60, 0));
            g2.fillOval(x - 12, y - 8, width + 24, height + 16);
            g2.dispose();
            // "CHARGING" text
            g.setFont(new Font("Impact", Font.PLAIN, 11));
            g.setColor(new Color(255, 140, 0, Math.min(255, alpha + 60)));
            g.drawString("CHARGING...", x + width / 2 - 32, y - 36);
        }
        // Flash white on the frame the heavy hit activates
        if (currentState == State.ATTACK_HEAVY && heavyStartup == 0 && attackTimer >= 46) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2.setColor(Color.WHITE);
            g2.fillOval(x - 10, y - 6, width + 20, height + 12);
            g2.dispose();
        }
        drawSpecialMeter(g);
    }

    public abstract void specialMove();
}