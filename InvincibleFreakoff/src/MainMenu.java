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

    // 0=1v1, 1=2v2, 2=1vBot, 3=2v2Bot
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
        setPreferredSize(new Dimension(1600, 800));
        setFocusable(true);
        addKeyListener(this);
        for (int i = 0; i < 220; i++)
            stars.add(new float[]{rand.nextInt(1600), rand.nextInt(800), rand.nextFloat()*2+0.5f});
        animTimer = new javax.swing.Timer(16, this);
        animTimer.start();
    }

    @Override public void actionPerformed(ActionEvent e) {
        tick++;
        if (tick % 6 == 0) particles.add(new MenuParticle(rand.nextInt(1600), 730));
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
        drawTitle(g, W);
        drawOptions(g, W, H);
        drawFooter(g, W, H);
    }

    private void drawBackground(Graphics2D g, int W, int H) {
        GradientPaint bg = new GradientPaint(0,0,new Color(4,4,16),W,H,new Color(10,0,25));
        g.setPaint(bg); g.fillRect(0,0,W,H);
        for (float[] star : stars) {
            float tw = (float)(Math.sin(tick*0.04+star[0])*0.3+0.7);
            int br = Math.min(255,(int)(tw*(100+star[2]*55)));
            g.setColor(new Color(br,br,Math.min(255,br+30)));
            g.fillOval((int)star[0],(int)star[1],star[2]>2?2:1,star[2]>2?2:1);
        }
        RadialGradientPaint n1=new RadialGradientPaint(200,300,350,new float[]{0f,1f},
            new Color[]{new Color(80,0,160,22),new Color(0,0,0,0)});
        g.setPaint(n1); g.fillRect(0,0,W,H);
        RadialGradientPaint n2=new RadialGradientPaint(1100,400,300,new float[]{0f,1f},
            new Color[]{new Color(200,50,0,18),new Color(0,0,0,0)});
        g.setPaint(n2); g.fillRect(0,0,W,H);
        for (MenuParticle p : particles) p.draw(g);
        // City silhouette
        g.setColor(new Color(5,4,10));
        int[] bx={0,0,80,80,140,140,210,210,280,280,350,350,420,420,500,500,580,580,650,650,
                  720,720,800,800,870,870,940,940,1010,1010,1080,1080,1150,1150,1220,1220,1300,1300,1380,1380,1460,1460,1540,1540,1600,1600};
        int[] by={H,590,590,558,558,578,578,545,545,570,570,540,540,563,563,535,535,558,558,532,
                  532,555,555,528,528,552,552,538,538,562,562,533,533,555,555,540,540,558,558,530,530,550,550,535,535,H};
        g.fillPolygon(bx,by,bx.length);
        GradientPaint hor=new GradientPaint(0,520,new Color(180,40,0,35),0,600,new Color(0,0,0,0));
        g.setPaint(hor); g.fillRect(0,520,W,80);
    }

    private void drawTitle(Graphics2D g, int W) {
        g.setFont(new Font("Impact",Font.PLAIN,96));
        FontMetrics fm=g.getFontMetrics();
        String l1="INVINCIBLE", l2="FREAKOFF";
        g.setColor(new Color(0,0,0,200));
        g.drawString(l1,W/2-fm.stringWidth(l1)/2+4,148);
        g.drawString(l2,W/2-fm.stringWidth(l2)/2+4,230);
        GradientPaint gp1=new GradientPaint(0,65,new Color(255,240,80),0,148,new Color(255,140,0));
        g.setPaint(gp1); g.drawString(l1,W/2-fm.stringWidth(l1)/2,146);
        GradientPaint gp2=new GradientPaint(0,155,new Color(255,80,50),0,230,new Color(200,0,0));
        g.setPaint(gp2); g.drawString(l2,W/2-fm.stringWidth(l2)/2,228);
        g.setFont(new Font("Arial",Font.BOLD,14));
        fm=g.getFontMetrics();
        String sub="— A VILTRUMITE FIGHTING EXPERIENCE —";
        g.setColor(new Color(255,255,255,55));
        g.drawString(sub,W/2-fm.stringWidth(sub)/2,253);
        float pulse=(float)(Math.sin(tick*0.06)*0.4+0.6);
        g.setColor(new Color(255,200,50,Math.max(0,Math.min(255,(int)(pulse*140)))));
        g.setStroke(new BasicStroke(2));
        g.drawLine(W/2-200,263,W/2+200,263);
        g.setStroke(new BasicStroke(1));
    }

    private void drawOptions(Graphics2D g, int W, int H) {
        g.setFont(new Font("Arial",Font.BOLD,13));
        g.setColor(new Color(255,255,255,90));
        FontMetrics fm=g.getFontMetrics();
        String nav="W/S  or  ↑↓  to navigate    ENTER or F to select";
        g.drawString(nav,W/2-fm.stringWidth(nav)/2,292);

        int cardW=270, cardH=140, gap=22;
        int totalW=4*cardW+3*gap;
        int startX=(W-totalW)/2;
        int cardY=308;
        for(int i=0;i<4;i++) drawCard(g,i,startX+i*(cardW+gap),cardY,cardW,cardH);

        // Description
        g.setFont(new Font("Arial",Font.ITALIC,15));
        g.setColor(new Color(200,200,200,200));
        fm=g.getFontMetrics();
        g.drawString(DESCRIPTIONS[selectedOption],W/2-fm.stringWidth(DESCRIPTIONS[selectedOption])/2,cardY+cardH+36);

        // Controls reminder
        drawControlsReminder(g, W, cardY+cardH+70);

        if((tick/35)%2==0){
            g.setFont(new Font("Impact",Font.PLAIN,24));
            g.setColor(OPTION_COLORS[selectedOption]);
            fm=g.getFontMetrics();
            String press="PRESS ENTER TO START";
            g.drawString(press,W/2-fm.stringWidth(press)/2,cardY+cardH+68);
        }
    }

    private void drawCard(Graphics2D g, int index, int cx, int cy, int cw, int ch) {
        boolean sel=(index==selectedOption);
        Color col=OPTION_COLORS[index];
        float pulse=(float)(Math.sin(tick*0.08)*0.5+0.5);

        g.setColor(new Color(0,0,0,110)); g.fillRoundRect(cx+4,cy+4,cw,ch,16,16);
        GradientPaint bg=new GradientPaint(cx,cy,
            new Color(col.getRed()/8,col.getGreen()/8,col.getBlue()/8,210),cx,cy+ch,new Color(4,4,10,210));
        g.setPaint(bg); g.fillRoundRect(cx,cy,cw,ch,16,16);

        if(sel){
            int a=Math.max(0,Math.min(255,(int)(140+115*pulse)));
            g.setColor(new Color(col.getRed(),col.getGreen(),col.getBlue(),a));
            g.setStroke(new BasicStroke(3));
        } else {
            g.setColor(new Color(55,55,75,190)); g.setStroke(new BasicStroke(1.5f));
        }
        g.drawRoundRect(cx,cy,cw,ch,16,16); g.setStroke(new BasicStroke(1));

        if(sel){
            RadialGradientPaint glow=new RadialGradientPaint(cx+cw/2f,cy+ch/2f,cw*0.65f,
                new float[]{0f,1f},new Color[]{
                    new Color(col.getRed(),col.getGreen(),col.getBlue(),Math.max(0,Math.min(255,(int)(pulse*38)))),
                    new Color(0,0,0,0)});
            g.setPaint(glow); g.fillRoundRect(cx-20,cy-20,cw+40,ch+40,20,20);
        }

        // Title
        g.setFont(new Font("Impact",Font.PLAIN,sel?30:24));
        FontMetrics fm=g.getFontMetrics();
        if(sel){
            GradientPaint tp=new GradientPaint(cx,cy+30,col,cx,cy+65,col.darker());
            g.setPaint(tp);
        } else g.setColor(new Color(160,160,160));
        g.drawString(OPTIONS[index],cx+(cw-fm.stringWidth(OPTIONS[index]))/2,cy+(sel?55:52));

        // Tag
        g.setFont(new Font("Arial",Font.BOLD,11));
        fm=g.getFontMetrics();
        g.setColor(sel?new Color(col.getRed(),col.getGreen(),col.getBlue(),170):new Color(90,90,110));
        g.drawString(TAGS[index],cx+(cw-fm.stringWidth(TAGS[index]))/2,cy+82);

        // Mode indicator icons
        String icon = index==0?"👤 VS 👤":index==1?"👥 VS 👥":index==2?"👤 VS 🤖":"👥 VS 🤖🤖";
        g.setFont(new Font("Segoe UI Emoji",Font.PLAIN,14));
        fm=g.getFontMetrics();
        g.setColor(sel?col:new Color(120,120,140));
        g.drawString(icon,cx+(cw-fm.stringWidth(icon))/2,cy+106);

        if(sel){
            g.setColor(col);
            int ax=cx+cw/2;
            int ay=cy+ch+8;
            g.fillPolygon(new int[]{ax-7,ax+7,ax},new int[]{ay,ay,ay+7},3);
        }
    }

    private void drawControlsReminder(Graphics2D g, int W, int y) {
        g.setFont(new Font("Arial",Font.PLAIN,11));
        g.setColor(new Color(255,255,255,50));
        FontMetrics fm=g.getFontMetrics();
        String[] lines = {
            "P1: WASD + F/G/H   |   P2: Arrow Keys + NP1/NP2/NP3",
            "P3: IJKL + U/O/P   |   P4: NP4/NP6/NP8 + NP7/NP9/NP0"
        };
        for(int i=0;i<lines.length;i++)
            g.drawString(lines[i],W/2-fm.stringWidth(lines[i])/2,y+i*16);
    }

    private void drawFooter(Graphics2D g, int W, int H) {
        g.setFont(new Font("Arial",Font.PLAIN,11));
        g.setColor(new Color(255,255,255,35));
        FontMetrics fm=g.getFontMetrics();
        String f="Invincible Showdown v2.0  |  Built with Java Swing";
        g.drawString(f,W/2-fm.stringWidth(f)/2,H-14);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int c=e.getKeyCode();
        if(c==KeyEvent.VK_UP||c==KeyEvent.VK_W)      selectedOption=(selectedOption+3)%4;
        if(c==KeyEvent.VK_DOWN||c==KeyEvent.VK_S)     selectedOption=(selectedOption+1)%4;
        if(c==KeyEvent.VK_LEFT||c==KeyEvent.VK_A)     selectedOption=(selectedOption+3)%4;
        if(c==KeyEvent.VK_RIGHT||c==KeyEvent.VK_D)    selectedOption=(selectedOption+1)%4;
        if(c==KeyEvent.VK_ENTER||c==KeyEvent.VK_F)    launchMode();
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
        float x,y,vx,vy,size; Color color; int life,maxLife;
        MenuParticle(float x,float y){
            Random r=new Random();
            this.x=x; this.y=y;
            vx=(r.nextFloat()-0.5f)*1.5f; vy=-(r.nextFloat()*2+0.5f);
            size=r.nextFloat()*3+2;
            Color[] cols={new Color(255,150,0),new Color(255,220,50),new Color(200,50,50),new Color(100,150,255)};
            color=cols[r.nextInt(cols.length)];
            maxLife=100+r.nextInt(60); life=maxLife;
        }
        void update(){x+=vx;y+=vy;life--;size*=0.996f;}
        void draw(Graphics2D g){
            float a=(float)life/maxLife;
            int alpha=Math.max(0,Math.min(255,(int)(a*150)));
            g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),alpha));
            g.fillOval((int)(x-size/2),(int)(y-size/2),(int)size,(int)size);
        }
    }
}