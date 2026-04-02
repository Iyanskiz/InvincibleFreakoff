import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public abstract class Fighter {

    // Position & size
    public int x, y;
    public int width = 80, height = 120;

    // Physics
    public float velX = 0, velY = 0;
    public boolean onGround = true;
    public static final int GROUND_Y = 480;
    public static final float GRAVITY = 0.8f;
    public static final float JUMP_FORCE = -18f;

    // Stats
    public int maxHealth;
    public int currentHealth;
    public String name;
    public boolean facingRight = true;
    public boolean isPlayer1;

    // Combat
    public boolean isAttacking = false;
    public boolean isBlocking = false;
    public boolean isHurt = false;
    public int attackTimer = 0;
    public int hurtTimer = 0;
    public int attackDamage = 10;
    public Rectangle attackHitbox = null;

    // Animation
    public enum State { IDLE, WALK, JUMP, ATTACK_LIGHT, ATTACK_HEAVY, HURT, DEAD, BLOCK, SPECIAL }
    public State currentState = State.IDLE;
    public int animFrame = 0;
    public int animTimer = 0;
    public int animSpeed = 6;

    // Sprite
    protected BufferedImage spriteSheet;
    protected BufferedImage[] idleFrames;
    protected BufferedImage[] walkFrames;
    protected BufferedImage[] attackFrames;
    protected BufferedImage[] hurtFrames;
    protected Color characterColor;
    protected Color accentColor;

    public Fighter(String name, int health, int x, boolean isPlayer1) {
        this.name = name;
        this.maxHealth = health;
        this.currentHealth = health;
        this.x = x;
        this.y = GROUND_Y;
        this.isPlayer1 = isPlayer1;
        this.facingRight = isPlayer1;
    }

    public void update(Fighter opponent) {
        // Gravity
        if (!onGround) {
            velY += GRAVITY;
        }

        // Apply velocity
        x += velX;
        y += velY;

        // Ground collision
        if (y >= GROUND_Y) {
            y = GROUND_Y;
            velY = 0;
            onGround = true;
            if (currentState == State.JUMP) {
                currentState = State.IDLE;
            }
        }

        // Wall bounds
        if (x < 0) x = 0;
        if (x > 1100 - width) x = 1100 - width;

        // Auto-face opponent
        if (opponent != null) {
            facingRight = (opponent.x > this.x);
        }

        // Attack timer
        if (isAttacking) {
            attackTimer--;
            if (attackTimer <= 0) {
                isAttacking = false;
                attackHitbox = null;
                currentState = State.IDLE;
            }
        }

        // Hurt timer
        if (isHurt) {
            hurtTimer--;
            if (hurtTimer <= 0) {
                isHurt = false;
                currentState = State.IDLE;
            }
        }

        // Friction
        if (onGround) {
            velX *= 0.75f;
        }

        // Animate
        animTimer++;
        if (animTimer >= animSpeed) {
            animTimer = 0;
            animFrame++;
        }

        updateAttackHitbox();
    }

    public void lightAttack() {
        if (!isAttacking && !isHurt) {
            isAttacking = true;
            attackTimer = 20;
            attackDamage = 8;
            currentState = State.ATTACK_LIGHT;
            animFrame = 0;
        }
    }

    public void heavyAttack() {
        if (!isAttacking && !isHurt) {
            isAttacking = true;
            attackTimer = 30;
            attackDamage = 18;
            currentState = State.ATTACK_HEAVY;
            animFrame = 0;
        }
    }

    public void jump() {
        if (onGround) {
            velY = JUMP_FORCE;
            onGround = false;
            currentState = State.JUMP;
        }
    }

    public void moveLeft() {
        if (!isAttacking && !isHurt) {
            velX = -5;
            if (onGround) currentState = State.WALK;
        }
    }

    public void moveRight() {
        if (!isAttacking && !isHurt) {
            velX = 5;
            if (onGround) currentState = State.WALK;
        }
    }

    public void block() {
        if (!isAttacking) {
            isBlocking = true;
            currentState = State.BLOCK;
        }
    }

    public void stopBlock() {
        isBlocking = false;
        if (currentState == State.BLOCK) currentState = State.IDLE;
    }

    public void takeDamage(int damage) {
        if (isBlocking) damage = damage / 3;
        currentHealth -= damage;
        if (currentHealth < 0) currentHealth = 0;
        isHurt = true;
        hurtTimer = 15;
        currentState = State.HURT;
        // Knockback
        velX = facingRight ? -4 : 4;
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }

    private void updateAttackHitbox() {
        if (isAttacking) {
            int reach = (currentState == State.ATTACK_HEAVY) ? 100 : 70;
            if (facingRight) {
                attackHitbox = new Rectangle(x + width, y + 20, reach, 60);
            } else {
                attackHitbox = new Rectangle(x - reach, y + 20, reach, 60);
            }
        } else {
            attackHitbox = null;
        }
    }

    public Rectangle getHurtbox() {
        return new Rectangle(x + 10, y, width - 20, height);
    }

    public void draw(Graphics2D g) {
        // Shadow
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(x + 5, GROUND_Y + height - 10, width - 10, 16);

        // Flip if facing left
        Graphics2D g2 = (Graphics2D) g.create();
        if (!facingRight) {
            g2.translate(x + width, y);
            g2.scale(-1, 1);
            g2.translate(-x, -y);
        }

        drawCharacter(g2);
        g2.dispose();

        // Debug hitbox (comment out for release)
        // g.setColor(Color.RED);
        // g.draw(getHurtbox());
    }

    protected abstract void drawCharacter(Graphics2D g);
    public abstract void specialMove();
}