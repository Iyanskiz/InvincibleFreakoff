import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public abstract class Fighter {

    public int x, y;
    public int width = 120, height = 150;

    public float velX = 0, velY = 0;
    public boolean onGround = true;
    public static final int GROUND_Y = 430;
    public static final float GRAVITY = 0.8f;
    public static final float JUMP_FORCE = -18f;

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

    public enum State { IDLE, WALK_FORWARD, WALK_BACKWARD, JUMP, ATTACK_LIGHT, ATTACK_HEAVY, HURT, DEAD, BLOCK, SPECIAL }
    public State currentState = State.IDLE;
    public int animFrame = 0;
    public int animTimer = 0;
    public int animSpeed = 6;

    protected Color characterColor = Color.GRAY;
    protected Color accentColor    = Color.WHITE;

    // Sprite images — subclasses populate these
    protected BufferedImage imgIdle;
    protected BufferedImage imgForward;
    protected BufferedImage imgBackward;
    protected BufferedImage imgPunch;
    protected BufferedImage imgBlock;
    protected BufferedImage imgHit;
    protected BufferedImage imgLevitating; // used for jump

    public Fighter(String name, int health, int x, boolean isPlayer1) {
        this.name      = name;
        this.maxHealth = health;
        this.currentHealth = health;
        this.x         = x;
        this.y         = GROUND_Y;
        this.isPlayer1 = isPlayer1;
        this.facingRight = isPlayer1;
    }

    /** Helper — loads an image from the imgs/ folder next to src */
    protected BufferedImage loadImage(String filename) {
        try {
            // Try several common paths so it works in Eclipse and from jar
            String[] paths = {
                "imgs/" + filename,
                "src/imgs/" + filename,
                "../imgs/" + filename,
                filename
            };
            for (String path : paths) {
                File f = new File(path);
                if (f.exists()) return ImageIO.read(f);
            }
            System.err.println("Image not found: " + filename);
        } catch (IOException e) {
            System.err.println("Error loading image: " + filename + " — " + e.getMessage());
        }
        return null;
    }

    public void update(Fighter opponent) {
        if (!onGround) velY += GRAVITY;

        x += velX;
        y += velY;

        if (y >= GROUND_Y) {
            y = GROUND_Y;
            velY = 0;
            onGround = true;
            if (currentState == State.JUMP) currentState = State.IDLE;
        }

        if (x < 0) x = 0;
        if (x > 1100 - width) x = 1100 - width;

        if (opponent != null) facingRight = (opponent.x > this.x);

        if (isAttacking) {
            attackTimer--;
            if (attackTimer <= 0) {
                isAttacking   = false;
                attackHitbox  = null;
                currentState  = State.IDLE;
            }
        }

        if (isHurt) {
            hurtTimer--;
            if (hurtTimer <= 0) {
                isHurt       = false;
                currentState = State.IDLE;
            }
        }

        if (onGround) velX *= 0.75f;
        if (Math.abs(velX) < 0.5f && onGround && currentState == State.WALK_FORWARD) currentState = State.IDLE;
        if (Math.abs(velX) < 0.5f && onGround && currentState == State.WALK_BACKWARD) currentState = State.IDLE;

        animTimer++;
        if (animTimer >= animSpeed) { animTimer = 0; animFrame++; }

        updateAttackHitbox();
    }

    public void lightAttack() {
        if (!isAttacking && !isHurt) {
            isAttacking  = true;
            attackTimer  = 20;
            attackDamage = 8;
            currentState = State.ATTACK_LIGHT;
            animFrame    = 0;
        }
    }

    public void heavyAttack() {
        if (!isAttacking && !isHurt) {
            isAttacking  = true;
            attackTimer  = 30;
            attackDamage = 18;
            currentState = State.ATTACK_HEAVY;
            animFrame    = 0;
        }
    }

    public void jump() {
        if (onGround) {
            velY     = JUMP_FORCE;
            onGround = false;
            currentState = State.JUMP;
        }
    }

    public void moveLeft() {
        if (!isAttacking && !isHurt) {
            velX = -5;
            if (onGround) currentState = facingRight ? State.WALK_BACKWARD : State.WALK_FORWARD;
        }
    }

    public void moveRight() {
        if (!isAttacking && !isHurt) {
            velX = 5;
            if (onGround) currentState = facingRight ? State.WALK_FORWARD : State.WALK_BACKWARD;
        }
    }

    public void block() {
        if (!isAttacking) { isBlocking = true; currentState = State.BLOCK; }
    }

    public void stopBlock() {
        isBlocking = false;
        if (currentState == State.BLOCK) currentState = State.IDLE;
    }

    public void takeDamage(int damage) {
        if (isBlocking) damage = damage / 3;
        currentHealth -= damage;
        if (currentHealth < 0) currentHealth = 0;
        isHurt       = true;
        hurtTimer    = 15;
        currentState = State.HURT;
        velX = facingRight ? -4 : 4;
    }

    public boolean isDead() { return currentHealth <= 0; }

    private void updateAttackHitbox() {
        if (isAttacking) {
            int reach = (currentState == State.ATTACK_HEAVY) ? 110 : 80;
            if (facingRight) attackHitbox = new Rectangle(x + width,       y + 20, reach, 80);
            else             attackHitbox = new Rectangle(x - reach,        y + 20, reach, 80);
        } else {
            attackHitbox = null;
        }
    }

    public Rectangle getHurtbox() {
        return new Rectangle(x + 10, y, width - 20, height);
    }

    /** Returns the correct image for the current state */
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
        // Shadow
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(x + 10, GROUND_Y + height - 5, width - 20, 14);

        BufferedImage img = getCurrentImage();

        if (img != null) {
            // Hurt flash
            if (isHurt && hurtTimer % 4 < 2) {
                // Draw with white tint by using AlphaComposite trick
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2.setColor(Color.WHITE);
                if (!facingRight) {
                    g2.drawImage(img, x + width, y, -width, height, null);
                } else {
                    g2.drawImage(img, x, y, width, height, null);
                }
                g2.dispose();
            } else {
                if (!facingRight) {
                    g.drawImage(img, x + width, y, -width, height, null);
                } else {
                    g.drawImage(img, x, y, width, height, null);
                }
            }
        } else {
            // Fallback shape if image missing
            g.setColor(characterColor);
            g.fillRoundRect(x + 10, y, width - 20, height, 10, 10);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(name, x + 15, y + height / 2);
        }
    }

    public abstract void specialMove();
}