import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Anissa — Viltrumite warrior, sent to Earth to mate with Invincible.
 * 
 * LORE: Anissa is one of the most powerful Viltrumites encountered on Earth.
 * She combines raw Viltrumite brutality with tactical precision — she waited,
 * studied, then struck when Invincible was at his weakest. Her special move
 * is the VILTRUMITE SLAM: she grabs the opponent, rockets them into the ground
 * pile-driver style — the same move she used to brutalize Mark repeatedly.
 *
 * STATS: High health, high damage, moderate speed. Tanky brawler.
 * Special: GROUND SLAM — charges forward, grabs, pile-drives into the ground.
 *          Charges from dealing damage (threshold=110).
 */
public class Anissa extends Fighter {

    private boolean slamActive = false;
    private int slamTimer = 0;

    public Anissa(int x, boolean isPlayer1) {
        super("Anissa", 290, x, isPlayer1);
        characterColor   = new Color(140, 30, 180);   // purple — her suit color
        accentColor      = new Color(220, 180, 255);
        attackDamage     = 13;
        animSpeed        = 6;
        width            = 118;
        height           = 155;
        specialThreshold = 110;  // mid-range charge — balanced

        imgIdle       = loadImage("AnissaIdle.png");
        imgForward    = loadImage("AnissaForward.png");
        imgBackward   = loadImage("AnissaBackward.png");
        imgPunch      = loadImage("AnissaPunch.png");
        imgBlock      = loadImage("AnissaBlock.png");
        imgHit        = loadImage("AnissaHit.png");
        imgLevitating = loadImage("AnissaLevitating.png");
    }

    @Override
    public void update(Fighter nearest, java.util.List<Platform> platforms) {
        if (slamActive) {
            slamTimer--;
            if (slamTimer <= 0) slamActive = false;
        }
        super.update(nearest, platforms);
    }

    @Override
    public void specialMove() {
        // VILTRUMITE SLAM — rockets forward low and fast, massive single hit
        if (specialReady() && !isAttacking) {
            slamActive    = true;
            slamTimer     = 45;
            consumeSpecial();
            isAttacking   = true;
            attackTimer   = 45;
            attackDamage  = 40;
            currentState  = State.SPECIAL;
            // Fast low charge — she dives at the opponent
            velX = facingRight ? 18 : -18;
            velY = 4f;  // slight downward arc, pile-driver feel
        }
    }

    @Override
    protected BufferedImage getCurrentImage() {
        return super.getCurrentImage();
    }

    /**
     * Override draw to render Anissa's sprite at its natural aspect ratio
     * so she looks slim, not stretched wide.
     * NOTE: do NOT call super.draw(g) — we handle everything here ourselves.
     */
    @Override
    public void draw(Graphics2D g) {
        // Draw shadow
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(x + 10, y + height, width - 20, 10);

        BufferedImage img = getCurrentImage();
        if (img != null) {
            // Calculate draw size preserving the image's natural aspect ratio
            float imgAspect = (float) img.getWidth() / img.getHeight();
            float boxAspect = (float) width / height;

            int drawW, drawH;
            if (imgAspect < boxAspect) {
                // Image is taller relative to width — constrain by height
                drawH = height;
                drawW = (int)(height * imgAspect);
            } else {
                // Image is wider — constrain by width
                drawW = width;
                drawH = (int)(width / imgAspect);
            }

            // Center horizontally within the hitbox, pin feet to bottom
            int drawX = x + (width - drawW) / 2;
            int drawY = y + (height - drawH);  // feet at bottom of hitbox

            if (isHurt && hurtTimer % 4 < 2) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
                g2.setColor(Color.WHITE);
                if (!facingRight) g2.drawImage(img, drawX + drawW, drawY, -drawW, drawH, null);
                else              g2.drawImage(img, drawX, drawY, drawW, drawH, null);
                g2.dispose();
            } else {
                if (!facingRight) g.drawImage(img, drawX + drawW, drawY, -drawW, drawH, null);
                else              g.drawImage(img, drawX, drawY, drawW, drawH, null);
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

        drawSpecialMeter(g);

        // Slam aura and shockwave — drawn after sprite, not doubled
        if (slamActive) {
            g.setColor(new Color(160, 0, 220, 55));
            g.fillOval(x - 20, y - 10, width + 40, height + 20);
            g.setColor(new Color(220, 100, 255, 25));
            g.fillOval(x - 35, y - 20, width + 70, height + 40);
            if (y + height > GROUND_Y - 60) {
                g.setColor(new Color(180, 0, 255, 60));
                int shockW = (int)(180 * ((float)slamTimer / 45f));
                g.fillOval(x + width/2 - shockW/2, GROUND_Y - 8, shockW, 20);
            }
            g.setFont(new Font("Impact", Font.PLAIN, 13));
            g.setColor(new Color(200, 80, 255, 220));
            g.drawString("VILTRUMITE SLAM!", x + width / 2 - 52, y - 38);
        }
    }
}