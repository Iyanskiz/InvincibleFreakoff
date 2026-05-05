import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class Fighter {

    public int x, y;
    public int width = 120, height = 150;

    public float velX = 0, velY = 0;
    public boolean onGround = true;
    public static final int GROUND_Y  = 620;   // lower floor — more vertical space
    public static final int WORLD_W   = 1600;  // wider arena
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
    public Rectangle attackHitbox = null;

    // Platform drop-through
    public boolean dropThrough = false;
    public int     dropTimer   = 0;

    public enum State { IDLE, WALK_FORWARD, WALK_BACKWARD, JUMP, ATTACK_LIGHT, ATTACK_HEAVY, HURT, DEAD, BLOCK, SPECIAL }
    public State currentState = State.IDLE;
    public int animFrame = 0;
    public int animTimer = 0;
    public int animSpeed = 6;

    protected Color characterColor = Color.GRAY;
    protected Color accentColor    = Color.WHITE;

    protected BufferedImage imgIdle;
    protected BufferedImage imgForward;
    protected BufferedImage imgBackward;
    protected BufferedImage imgPunch;
    protected BufferedImage imgBlock;
    protected BufferedImage imgHit;
    protected BufferedImage imgLevitating;

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
            String[] paths = { "imgs/" + filename, "src/imgs/" + filename, "../imgs/" + filename, filename };
            for (String path : paths) {
                File f = new File(path);
                if (f.exists()) return ImageIO.read(f);
            }
            System.err.println("Image not found: " + filename);
        } catch (IOException e) {
            System.err.println("Error loading: " + filename);
        }
        return null;
    }

    public void setBlockHeld(boolean held) {
        blockKeyHeld = held;
        if (held && !isAttacking && !isHurt) {
            isBlocking   = true;
            currentState = State.BLOCK;
        } else if (!held) {
            isBlocking = false;
            if (currentState == State.BLOCK) currentState = State.IDLE;
        }
    }

    /** Full update with platform list */
    public void update(Fighter opponent, List<Platform> platforms) {
        if (dropTimer > 0) { dropThrough = true; dropTimer--; }
        else dropThrough = false;

        if (!onGround) velY += GRAVITY;
        x += (int)velX;
        y += (int)velY;

        // Main floor
        if (y >= GROUND_Y) {
            y = GROUND_Y; velY = 0; onGround = true;
            if (currentState == State.JUMP) currentState = State.IDLE;
        }

        // Platform collision (one-way top)
        if (!dropThrough && velY >= 0) {
            for (Platform p : platforms) {
                int feetY    = y + height;
                int prevFeet = feetY - (int)velY;
                if (prevFeet <= p.y && feetY >= p.y
                        && x + width - 20 > p.x
                        && x + 20 < p.x + p.w) {
                    y = p.y - height; velY = 0; onGround = true;
                    if (currentState == State.JUMP) currentState = State.IDLE;
                    break;
                }
            }
        }

        if (x < 0)               x = 0;
        if (x > WORLD_W - width) x = WORLD_W - width;

        if (opponent != null) facingRight = (opponent.x > this.x);

        if (blockKeyHeld && !isAttacking && !isHurt) {
            isBlocking = true; currentState = State.BLOCK;
        }

        if (isAttacking) {
            attackTimer--;
            if (attackTimer <= 0) {
                isAttacking = false; attackHitbox = null;
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
            (currentState == State.WALK_FORWARD || currentState == State.WALK_BACKWARD)) {
            currentState = blockKeyHeld ? State.BLOCK : State.IDLE;
        }

        animTimer++;
        if (animTimer >= animSpeed) { animTimer = 0; animFrame++; }
        updateAttackHitbox();
    }

    /** Legacy no-platform update */
    public void update(Fighter opponent) {
        update(opponent, java.util.Collections.emptyList());
    }

    public void lightAttack() {
        if (!isAttacking && !isHurt && !isBlocking) {
            isAttacking = true; attackTimer = 20; attackDamage = 8;
            currentState = State.ATTACK_LIGHT; animFrame = 0;
        }
    }

    public void heavyAttack() {
        if (!isAttacking && !isHurt && !isBlocking) {
            isAttacking = true; attackTimer = 30; attackDamage = 18;
            currentState = State.ATTACK_HEAVY; animFrame = 0;
        }
    }

    public void jump() {
        if (onGround && !isBlocking) {
            velY = JUMP_FORCE; onGround = false; currentState = State.JUMP;
        }
    }

    /** Drop through platform (DOWN + JUMP) */
    public void dropDown() {
        if (onGround) {
            dropThrough = true; dropTimer = 18;
            onGround = false; velY = 3f;
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
            damage = damage / 3;
            currentHealth -= damage;
            if (currentHealth < 0) currentHealth = 0;
            isHurt = true; hurtTimer = 6;
            return;
        }
        currentHealth -= damage;
        if (currentHealth < 0) currentHealth = 0;
        isHurt = true; hurtTimer = 15;
        currentState = State.HURT;
        velX = facingRight ? -4 : 4;
    }

    public boolean isDead() { return currentHealth <= 0; }

    private void updateAttackHitbox() {
        if (isAttacking) {
            int reach = (currentState == State.ATTACK_HEAVY || currentState == State.SPECIAL) ? 58 : 42;
            int hitH  = 42;
            int hitY  = y + height/2 - hitH/2;
            if (facingRight) attackHitbox = new Rectangle(x + width - 15, hitY, reach, hitH);
            else             attackHitbox = new Rectangle(x - reach + 15,  hitY, reach, hitH);
        } else {
            attackHitbox = null;
        }
    }

    public Rectangle getHurtbox() {
        return new Rectangle(x + 28, y + 18, width - 56, height - 30);
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

    public void draw(Graphics2D g) {
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
    }

    public abstract void specialMove();
}