import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class CharacterSelect extends JPanel implements KeyListener, ActionListener {

    // gameMode: 0=1v1, 1=2v2, 2=1vBot, 3=2v2Bot
    private int gameMode;
    private int[] p1Selections = {0, 1};
    private int[] p2Selections = {2, 3};
    private boolean[] p1Confirmed, p2Confirmed;
    private int p1Slot=0, p2Slot=0;
    private int p1Cursor=0, p2Cursor=2;

    // Bot settings
    private int   botDifficulty   = 1; // 0=Easy,1=Medium,2=Hard,3=Nightmare
    private int[] botSelections   = {2, 3};
    private boolean[] botConfirmed;
    private int botSlot=0, botCursor=2;
    private boolean selectingDifficulty = false;
    private boolean difficultyDone = false;

    // Phase: 0=picking difficulty (bot modes), 1=picking characters
    private int phase = 0;

    private javax.swing.Timer animTimer;
    private int tick=0;
    private JFrame parentFrame;

    private static final String[] NAMES     = {"INVINCIBLE","OMNI-MAN","THRAGG","CONQUEST","ANISSA"};
    private static final String[] SUBTITLES = {"Mark Grayson","Nolan Grayson","Grand Regent","The Conqueror","Viltrumite Warrior"};
    private static final Color[]  COLORS    = {new Color(20,160,80),new Color(180,0,0),new Color(100,0,0),new Color(60,60,80),new Color(140,30,180)};
    private static final Color[]  ACCENTS   = {new Color(180,255,120),new Color(240,240,240),new Color(180,140,0),new Color(200,50,50),new Color(220,180,255)};
    private static final int[]    STAT_PWR  = {100,90,95,88,92};
    private static final int[]    STAT_SPD  = {95,85,70,75,82};
    private static final int[]    STAT_HP   = {100,90,95,88,85};
    private static final String[] DIFF_NAMES  = {"EASY","MEDIUM","HARD","NIGHTMARE"};
    private static final Color[]  DIFF_COLORS = {new Color(80,200,80),new Color(255,200,50),new Color(255,100,50),new Color(200,0,255)};
    private static final String[] DIFF_DESC   = {
        "Bot moves slowly, rarely attacks",
        "Balanced challenge — attacks and defends",
        "Aggressive AI — fast combos and blocks",
        "Relentless — near-perfect reactions"
    };

    private BufferedImage[] previewImages = new BufferedImage[5];

    public CharacterSelect(JFrame frame, int gameMode) {
        this.parentFrame = frame;
        this.gameMode    = gameMode;
        int slots = (gameMode==1||gameMode==3)?2:1;
        p1Confirmed  = new boolean[slots];
        p2Confirmed  = new boolean[slots];
        botConfirmed = new boolean[slots];
        // Bot modes start at difficulty phase
        phase = (gameMode==2||gameMode==3) ? 0 : 1;
        // Size set by Frame
        setFocusable(true);
        addKeyListener(this);
        loadPreviews();
        animTimer=new javax.swing.Timer(16,this);
        animTimer.start();
    }

    private void loadPreviews(){
        String[] files={"InvincibleLevitating.png","OmniLevitating.png","ThaggLevitating.png","ConquestLevitating.png","AnissaLevitating.png"};
        String[] paths={"imgs/","src/imgs/","../imgs/",""};
        for(int i=0;i<files.length;i++) for(String b:paths){
            File f=new File(b+files[i]);
            if(f.exists()){try{previewImages[i]=ImageIO.read(f);}catch(IOException e){}break;}
        }
    }

    @Override public void actionPerformed(ActionEvent e){tick++;repaint();}

    @Override
    protected void paintComponent(Graphics g0){
        super.paintComponent(g0);
        Graphics2D g=(Graphics2D)g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int W=getWidth(),H=getHeight();
        // Background
        g.setColor(new Color(6,6,14)); g.fillRect(0,0,W,H);
        g.setColor(new Color(255,60,60,12));
        for(int i=0;i<W;i+=55) g.drawLine(i,0,i,H);
        for(int i=0;i<H;i+=55) g.drawLine(0,i,W,i);
        RadialGradientPaint r=new RadialGradientPaint(W/2f,H/2f,W*0.55f,
            new float[]{0f,1f},new Color[]{new Color(160,0,0,30),new Color(0,0,0,0)});
        g.setPaint(r); g.fillRect(0,0,W,H);

        if(phase==0) drawDifficultyPhase(g,W,H);
        else         drawCharacterPhase(g,W,H);
    }

    // ── Phase 0: Difficulty selection ─────────────────────────────────────────

    private void drawDifficultyPhase(Graphics2D g, int W, int H){
        // Title
        g.setFont(new Font("Impact",Font.PLAIN,48));
        FontMetrics fm=g.getFontMetrics();
        String t="SELECT DIFFICULTY";
        g.setColor(new Color(0,0,0,180)); g.drawString(t,W/2-fm.stringWidth(t)/2+3,63);
        GradientPaint gp=new GradientPaint(0,25,new Color(255,220,50),0,63,new Color(255,60,0));
        g.setPaint(gp); g.drawString(t,W/2-fm.stringWidth(t)/2,61);

        // Mode badge
        String badge=gameMode==2?"1 VS BOT":"2 VS 2 BOT";
        g.setFont(new Font("Arial",Font.BOLD,13)); fm=g.getFontMetrics();
        g.setColor(new Color(200,80,255,60)); g.fillRoundRect(W/2-fm.stringWidth(badge)/2-10,72,fm.stringWidth(badge)+20,20,8,8);
        g.setColor(new Color(200,80,255)); g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(W/2-fm.stringWidth(badge)/2-10,72,fm.stringWidth(badge)+20,20,8,8);
        g.setStroke(new BasicStroke(1));
        g.drawString(badge,W/2-fm.stringWidth(badge)/2,86);

        // Nav hint
        g.setFont(new Font("Arial",Font.BOLD,13)); fm=g.getFontMetrics();
        g.setColor(new Color(255,255,255,80));
        String nav="← → or A/D to select   |   ENTER or F to confirm";
        g.drawString(nav,W/2-fm.stringWidth(nav)/2,108);

        // Difficulty cards
        int cw=240,ch=160,gap=28;
        int totalW=4*cw+3*gap;
        int sx=(W-totalW)/2, sy=130;
        for(int i=0;i<4;i++) drawDiffCard(g,i,sx+i*(cw+gap),sy,cw,ch);

        // Description
        g.setFont(new Font("Arial",Font.ITALIC,16)); fm=g.getFontMetrics();
        g.setColor(new Color(220,220,220,200));
        g.drawString(DIFF_DESC[botDifficulty],W/2-fm.stringWidth(DIFF_DESC[botDifficulty])/2,sy+ch+35);

        // Bot character selection (below difficulty)
        drawBotCharSelect(g,W,sy+ch+65,H);
    }

    private void drawDiffCard(Graphics2D g,int index,int cx,int cy,int cw,int ch){
        boolean sel=(index==botDifficulty);
        Color col=DIFF_COLORS[index];
        float pulse=(float)(Math.sin(tick*0.08)*0.5+0.5);
        g.setColor(new Color(0,0,0,100)); g.fillRoundRect(cx+3,cy+3,cw,ch,14,14);
        GradientPaint bg=new GradientPaint(cx,cy,
            new Color(col.getRed()/7,col.getGreen()/7,col.getBlue()/7,210),cx,cy+ch,new Color(4,4,12,210));
        g.setPaint(bg); g.fillRoundRect(cx,cy,cw,ch,14,14);
        if(sel){
            int a=Math.max(0,Math.min(255,(int)(135+120*pulse)));
            g.setColor(new Color(col.getRed(),col.getGreen(),col.getBlue(),a));
            g.setStroke(new BasicStroke(3));
        } else {g.setColor(new Color(55,55,75,180)); g.setStroke(new BasicStroke(1.5f));}
        g.drawRoundRect(cx,cy,cw,ch,14,14); g.setStroke(new BasicStroke(1));
        // Skull / icon per difficulty
        String[] icons={"😊","⚔","🔥","💀","👊"};
        g.setFont(new Font("Segoe UI Emoji",Font.PLAIN,sel?32:26));
        FontMetrics fm=g.getFontMetrics();
        g.setColor(col); g.drawString(icons[index],cx+(cw-fm.stringWidth(icons[index]))/2,cy+46);
        g.setFont(new Font("Impact",Font.PLAIN,sel?28:22)); fm=g.getFontMetrics();
        if(sel){GradientPaint tp=new GradientPaint(cx,cy+50,col,cx,cy+80,col.darker());g.setPaint(tp);}
        else g.setColor(new Color(160,160,160));
        g.drawString(DIFF_NAMES[index],cx+(cw-fm.stringWidth(DIFF_NAMES[index]))/2,cy+80);
        if(sel){g.setFont(new Font("Arial",Font.BOLD,10));fm=g.getFontMetrics();
            g.setColor(new Color(col.getRed(),col.getGreen(),col.getBlue(),160));
            g.drawString("SELECTED",cx+(cw-fm.stringWidth("SELECTED"))/2,cy+100);}
        if(sel){g.setColor(col);g.fillPolygon(new int[]{cx+cw/2-7,cx+cw/2+7,cx+cw/2},new int[]{cy+ch+6,cy+ch+6,cy+ch+13},3);}
    }

    private void drawBotCharSelect(Graphics2D g,int W,int y,int H){
        int slots=(gameMode==3)?2:1;
        // Header
        g.setFont(new Font("Impact",Font.PLAIN,22)); FontMetrics fm=g.getFontMetrics();
        g.setColor(new Color(255,255,255,150));
        String hdr="SELECT BOT CHARACTER"+(slots>1?"S":"");
        g.drawString(hdr,W/2-fm.stringWidth(hdr)/2,y+22);

        int cw=180,ch=90,gap=14,totalW=5*cw+4*gap,sx=(W-totalW)/2;
        for(int i=0;i<5;i++){
            int cx=sx+i*(cw+gap),cy=y+30;
            boolean sel=(botCursor==i);
            boolean conf0=(slots>=1&&botSelections[0]==i&&botConfirmed[0]);
            boolean conf1=(slots>=2&&botSelections[1]==i&&botConfirmed[1]);
            Color col=COLORS[i],acc=ACCENTS[i];
            float pulse=(float)(Math.sin(tick*0.08)*0.5+0.5);
            g.setColor(new Color(0,0,0,90)); g.fillRoundRect(cx+2,cy+2,cw,ch,10,10);
            GradientPaint bg=new GradientPaint(cx,cy,new Color(col.getRed()/7,col.getGreen()/7,col.getBlue()/7,200),cx,cy+ch,new Color(4,4,12,200));
            g.setPaint(bg); g.fillRoundRect(cx,cy,cw,ch,10,10);
            Color bc=sel?new Color(acc.getRed(),acc.getGreen(),acc.getBlue(),Math.max(0,Math.min(255,(int)(130+125*pulse)))):new Color(50,50,68,170);
            g.setColor(bc); g.setStroke(new BasicStroke(sel?2.5f:1.2f));
            g.drawRoundRect(cx,cy,cw,ch,10,10); g.setStroke(new BasicStroke(1));
            // Mini preview
            if(previewImages[i]!=null){
                int ih=Math.min(70,(int)(previewImages[i].getHeight()*(60f/previewImages[i].getWidth())));
                int iw=(int)(previewImages[i].getWidth()*((float)ih/previewImages[i].getHeight()));
                g.drawImage(previewImages[i],cx+(cw-iw)/2,cy+4,iw,Math.min(ih,60),null);
            }
            g.setFont(new Font("Impact",Font.PLAIN,sel?15:13)); fm=g.getFontMetrics();
            g.setColor(sel?acc:new Color(160,160,160));
            g.drawString(NAMES[i],cx+(cw-fm.stringWidth(NAMES[i]))/2,cy+ch-8);
            // Slot tags
            if(conf0){g.setFont(new Font("Arial",Font.BOLD,8));fm=g.getFontMetrics();
                g.setColor(new Color(200,80,255,200));g.fillRoundRect(cx+2,cy+2,fm.stringWidth("BOT"+(slots>1?"-1":""))+6,12,3,3);
                g.setColor(Color.BLACK);g.drawString("BOT"+(slots>1?"-1":""),cx+5,cy+11);}
            if(conf1){g.setFont(new Font("Arial",Font.BOLD,8));fm=g.getFontMetrics();
                g.setColor(new Color(200,80,255,200));g.fillRoundRect(cx+cw-fm.stringWidth("BOT-2")-8,cy+2,fm.stringWidth("BOT-2")+6,12,3,3);
                g.setColor(Color.BLACK);g.drawString("BOT-2",cx+cw-fm.stringWidth("BOT-2")-5,cy+11);}
        }

        // Confirm prompt
        boolean allBotDone=true; for(boolean b:botConfirmed) if(!b) allBotDone=false;
        if(allBotDone&&difficultyDone){
            if((tick/30)%2==0){
                g.setFont(new Font("Impact",Font.PLAIN,20)); fm=g.getFontMetrics();
                g.setColor(new Color(200,80,255));
                String s="PRESS ENTER TO CONTINUE";
                g.drawString(s,W/2-fm.stringWidth(s)/2,H-20);
            }
        } else {
            g.setFont(new Font("Arial",Font.BOLD,12)); fm=g.getFontMetrics();
            g.setColor(new Color(255,255,255,70));
            String hint=!difficultyDone?"ENTER = confirm difficulty   |   ←/→ = choose difficulty":
                "←/→ = choose bot character   |   ENTER = confirm bot";
            g.drawString(hint,W/2-fm.stringWidth(hint)/2,H-20);
        }
    }

    // ── Phase 1: Character selection ──────────────────────────────────────────

    private void drawCharacterPhase(Graphics2D g, int W, int H){
        // Title
        g.setFont(new Font("Impact",Font.PLAIN,46));
        FontMetrics fm=g.getFontMetrics();
        String t="SELECT YOUR FIGHTER";
        g.setColor(new Color(0,0,0,180)); g.drawString(t,W/2-fm.stringWidth(t)/2+3,63);
        GradientPaint gp=new GradientPaint(0,25,new Color(255,220,50),0,63,new Color(255,60,0));
        g.setPaint(gp); g.drawString(t,W/2-fm.stringWidth(t)/2,61);

        drawModeTag(g,W);

        int cw=Math.min(230,(W-80)/5),ch=320,gap=14,totalW=5*cw+4*gap,sx=(W-totalW)/2,sy=120;
        for(int i=0;i<5;i++) drawCharCard(g,i,sx+i*(cw+gap),sy,cw,ch);

        drawBottomPanels(g,W,H);
        drawCharInstructions(g,W,H);
    }

    private void drawModeTag(Graphics2D g,int W){
        String[] tags={"1V1","2V2","VS BOT","2V2 BOT"};
        Color[]  tc  ={new Color(255,200,50),new Color(50,180,255),new Color(255,80,80),new Color(200,80,255)};
        String badge=tags[gameMode]; Color bc=tc[gameMode];
        g.setFont(new Font("Arial",Font.BOLD,13)); FontMetrics fm=g.getFontMetrics();
        int bw=fm.stringWidth(badge)+20;
        g.setColor(new Color(bc.getRed(),bc.getGreen(),bc.getBlue(),40));
        g.fillRoundRect(W/2-bw/2,70,bw,20,8,8);
        g.setColor(bc); g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(W/2-bw/2,70,bw,20,8,8); g.setStroke(new BasicStroke(1));
        g.drawString(badge,W/2-fm.stringWidth(badge)/2,84);
        // Show selected difficulty for bot modes
        if(gameMode==2||gameMode==3){
            g.setFont(new Font("Arial",Font.BOLD,11)); fm=g.getFontMetrics();
            g.setColor(DIFF_COLORS[botDifficulty]);
            String ds="Difficulty: "+DIFF_NAMES[botDifficulty];
            g.drawString(ds,W/2-fm.stringWidth(ds)/2,99);
        }
    }

    private void drawCharCard(Graphics2D g,int index,int cx,int cy,int cw,int ch){
        boolean p1h=(p1Cursor==index), p2h=(gameMode!=2&&gameMode!=3&&p2Cursor==index);
        Color base=COLORS[index],acc=ACCENTS[index];
        float pulse=(float)(Math.sin(tick*0.08)*0.5+0.5);

        g.setColor(new Color(0,0,0,100)); g.fillRoundRect(cx+4,cy+4,cw,ch,14,14);
        GradientPaint bg=new GradientPaint(cx,cy,new Color(base.getRed()/6,base.getGreen()/6,base.getBlue()/6,220),cx,cy+ch,new Color(5,5,12,220));
        g.setPaint(bg); g.fillRoundRect(cx,cy,cw,ch,14,14);

        Color bc;
        if(p1h||p2h){int a=Math.max(0,Math.min(255,(int)(140+115*pulse)));bc=new Color(acc.getRed(),acc.getGreen(),acc.getBlue(),a);g.setStroke(new BasicStroke(3));}
        else{bc=new Color(55,55,75);g.setStroke(new BasicStroke(1.5f));}
        g.setColor(bc); g.drawRoundRect(cx,cy,cw,ch,14,14); g.setStroke(new BasicStroke(1));

        // Preview
        int ph=205; float bob=(float)(Math.sin(tick*0.06+index*1.2)*5);
        if(previewImages[index]!=null){
            BufferedImage img=previewImages[index];
            float scale=Math.min((float)(cw-20)/img.getWidth(),(float)ph/img.getHeight())*0.9f;
            int dw=(int)(img.getWidth()*scale),dh=(int)(img.getHeight()*scale);
            RadialGradientPaint gl=new RadialGradientPaint(cx+cw/2f,cy+ph/2f+8,ph*0.55f,new float[]{0f,1f},
                new Color[]{new Color(base.getRed(),base.getGreen(),base.getBlue(),32),new Color(0,0,0,0)});
            g.setPaint(gl); g.fillRect(cx,cy,cw,ph+16);
            g.drawImage(img,cx+(cw-dw)/2,(int)(cy+8+(ph-dh)/2+bob),dw,dh,null);
        }
        // Divider
        g.setColor(new Color(acc.getRed(),acc.getGreen(),acc.getBlue(),55));
        g.drawLine(cx+14,cy+ph+16,cx+cw-14,cy+ph+16);
        // Name
        g.setFont(new Font("Impact",Font.PLAIN,20));
        GradientPaint ng=new GradientPaint(cx,cy+ph+20,acc,cx,cy+ph+38,acc.darker());
        g.setPaint(ng); FontMetrics fm=g.getFontMetrics();
        g.drawString(NAMES[index],cx+(cw-fm.stringWidth(NAMES[index]))/2,cy+ph+34);
        // Subtitle
        g.setFont(new Font("Arial",Font.ITALIC,10)); g.setColor(new Color(170,170,170,150)); fm=g.getFontMetrics();
        g.drawString(SUBTITLES[index],cx+(cw-fm.stringWidth(SUBTITLES[index]))/2,cy+ph+48);
        // Stats
        drawStats(g,index,cx+12,cy+ph+55,cw-24);
        // Slot tags
        int slots=(gameMode==1||gameMode==3)?2:1;
        for(int s=0;s<slots;s++){
            if(p1Selections[s]==index&&p1Confirmed[s]){
                g.setFont(new Font("Arial",Font.BOLD,8));fm=g.getFontMetrics();
                String tag="P1"+(slots>1?"-"+(s+1):"");
                g.setColor(new Color(50,150,255,200));g.fillRoundRect(cx+3+s*28,cy+3,fm.stringWidth(tag)+6,12,3,3);
                g.setColor(Color.BLACK);g.drawString(tag,cx+6+s*28,cy+12);}
            if(gameMode!=2&&gameMode!=3&&p2Selections[s]==index&&p2Confirmed[s]){
                g.setFont(new Font("Arial",Font.BOLD,8));fm=g.getFontMetrics();
                String tag="P2"+(slots>1?"-"+(s+1):"");
                g.setColor(new Color(255,80,80,200));g.fillRoundRect(cx+cw-3-(s+1)*28,cy+3,fm.stringWidth(tag)+6,12,3,3);
                g.setColor(Color.BLACK);g.drawString(tag,cx+cw-25-s*28,cy+12);}
        }
        if(p1h) drawCursor(g,cx,cy,cw,ch,new Color(50,150,255),"P1",allP1Done());
        if(p2h) drawCursor(g,cx+3,cy+3,cw-6,ch-6,new Color(255,80,80),"P2",allP2Done());
    }

    private void drawStats(Graphics2D g,int idx,int x,int y,int w){
        String[] lbl={"PWR","SPD","HP"}; int[] vals={STAT_PWR[idx],STAT_SPD[idx],STAT_HP[idx]};
        Color[] cols={new Color(255,60,60),new Color(60,180,255),new Color(60,200,60)};
        g.setFont(new Font("Arial",Font.BOLD,9));
        for(int i=0;i<3;i++){int by=y+i*11;
            g.setColor(new Color(255,255,255,85));g.drawString(lbl[i],x,by+6);
            int bx=x+24,bw=w-26,bh=5;
            g.setColor(new Color(22,22,32));g.fillRoundRect(bx,by,bw,bh,bh,bh);
            g.setColor(cols[i]);g.fillRoundRect(bx,by,(int)(bw*vals[i]/100f),bh,bh,bh);}
    }

    private void drawCursor(Graphics2D g,int cx,int cy,int cw,int ch,Color col,String label,boolean conf){
        g.setColor(conf?new Color(255,255,100):col);
        g.setStroke(new BasicStroke(3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        int s=15;
        g.drawLine(cx,cy+s,cx,cy);g.drawLine(cx,cy,cx+s,cy);
        g.drawLine(cx+cw-s,cy,cx+cw,cy);g.drawLine(cx+cw,cy,cx+cw,cy+s);
        g.drawLine(cx,cy+ch-s,cx,cy+ch);g.drawLine(cx,cy+ch,cx+s,cy+ch);
        g.drawLine(cx+cw-s,cy+ch,cx+cw,cy+ch);g.drawLine(cx+cw,cy+ch,cx+cw,cy+ch-s);
        g.setStroke(new BasicStroke(1));
        g.setFont(new Font("Arial",Font.BOLD,10)); FontMetrics fm=g.getFontMetrics();
        int lw=fm.stringWidth(label)+8;
        g.setColor(conf?new Color(255,255,100):col); g.fillRoundRect(cx+5,cy+5,lw,14,4,4);
        g.setColor(Color.BLACK); g.drawString(label,cx+9,cy+15);
        if(conf){g.setFont(new Font("Impact",Font.PLAIN,13));g.setColor(new Color(255,255,100));g.drawString("LOCKED",cx+cw/2-24,cy+ch/2);}
    }

    private void drawBottomPanels(Graphics2D g,int W,int H){
        int slots=(gameMode==1||gameMode==3)?2:1;
        drawTeamPanel(g,20,H-85,290,65,1,slots);
        g.setFont(new Font("Impact",Font.BOLD,34));
        GradientPaint vg=new GradientPaint(W/2f-16,H-75,new Color(255,200,50),W/2f+16,H-40,new Color(255,50,0));
        g.setPaint(vg); g.drawString("VS",W/2-20,H-40);
        if(gameMode==0||gameMode==1) drawTeamPanel(g,W-310,H-85,290,65,2,slots);
        else drawBotInfoPanel(g,W-310,H-85,290,65,slots);
    }

    private void drawTeamPanel(Graphics2D g,int x,int y,int w,int h,int player,int slots){
        Color col=player==1?new Color(50,150,255):new Color(255,80,80);
        g.setColor(new Color(10,10,20,200));g.fillRoundRect(x,y,w,h,10,10);
        g.setColor(allDone(player)?new Color(255,255,100):col);
        g.setStroke(new BasicStroke(2));g.drawRoundRect(x,y,w,h,10,10);g.setStroke(new BasicStroke(1));
        g.setFont(new Font("Impact",Font.PLAIN,14));
        int[] sels=player==1?p1Selections:p2Selections;
        boolean[] conf=player==1?p1Confirmed:p2Confirmed;
        for(int s=0;s<slots;s++){
            g.setColor(conf[s]?new Color(255,255,100):col);
            g.drawString("P"+player+(slots>1?"-"+(s+1):"")+": "+NAMES[sels[s]]+(conf[s]?" ✓":""),x+10,y+22+s*24);}
    }

    private void drawBotInfoPanel(Graphics2D g,int x,int y,int w,int h,int slots){
        g.setColor(new Color(10,10,20,200));g.fillRoundRect(x,y,w,h,10,10);
        g.setColor(DIFF_COLORS[botDifficulty]);g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x,y,w,h,10,10);g.setStroke(new BasicStroke(1));
        g.setFont(new Font("Impact",Font.PLAIN,13));
        for(int s=0;s<slots;s++){
            g.setColor(DIFF_COLORS[botDifficulty]);
            g.drawString("BOT"+(slots>1?"-"+(s+1):"")+": "+NAMES[botSelections[s]]+" ["+DIFF_NAMES[botDifficulty]+"]",x+8,y+20+s*24);}
    }

    private void drawCharInstructions(Graphics2D g,int W,int H){
        g.setFont(new Font("Arial",Font.PLAIN,11)); g.setColor(new Color(255,255,255,65));
        FontMetrics fm=g.getFontMetrics();
        String i1="P1: A/D select  |  F confirm";
        String i2=(gameMode==0||gameMode==1)?"P2: ←/→ select  |  NP1 confirm":"Bot auto-assigned";
        g.drawString(i1,20,H-92); g.drawString(i2,W-220,H-92);
        boolean rd=allP1Done()&&(gameMode==2||gameMode==3||allP2Done());
        if(rd&&(tick/30)%2==0){
            g.setFont(new Font("Impact",Font.PLAIN,20));g.setColor(new Color(255,220,50));
            String s="PRESS ENTER TO FIGHT!";fm=g.getFontMetrics();
            g.drawString(s,W/2-fm.stringWidth(s)/2,H-12);}
    }

    private boolean allP1Done(){for(boolean b:p1Confirmed)if(!b)return false;return true;}
    private boolean allP2Done(){for(boolean b:p2Confirmed)if(!b)return false;return true;}
    private boolean allBotDone(){for(boolean b:botConfirmed)if(!b)return false;return true;}
    private boolean allDone(int player){return player==1?allP1Done():allP2Done();}

    @Override
    public void keyPressed(KeyEvent e){
        int code=e.getKeyCode();
        int slots=(gameMode==1||gameMode==3)?2:1;

        if(phase==0){
            // Phase 0: difficulty then bot char
            if(!difficultyDone){
                if(code==KeyEvent.VK_A||code==KeyEvent.VK_LEFT)  botDifficulty=(botDifficulty+3)%5;
                if(code==KeyEvent.VK_D||code==KeyEvent.VK_RIGHT) botDifficulty=(botDifficulty+1)%5;
                if(code==KeyEvent.VK_ENTER||code==KeyEvent.VK_F) difficultyDone=true;
            } else if(!allBotDone()){
                if(code==KeyEvent.VK_A||code==KeyEvent.VK_LEFT)  botCursor=(botCursor+3)%5;
                if(code==KeyEvent.VK_D||code==KeyEvent.VK_RIGHT) botCursor=(botCursor+1)%5;
                if(code==KeyEvent.VK_ENTER||code==KeyEvent.VK_F){
                    botSelections[botSlot]=botCursor;
                    botConfirmed[botSlot]=true;
                    if(botSlot<slots-1) botSlot++;
                }
            } else if(code==KeyEvent.VK_ENTER||code==KeyEvent.VK_F){
                phase=1; // advance to character select
            }
        } else {
            // Phase 1: player character select
            if(!p1Confirmed[p1Slot]){
                if(code==KeyEvent.VK_A) p1Cursor=(p1Cursor+3)%5;
                if(code==KeyEvent.VK_D) p1Cursor=(p1Cursor+1)%5;
                if(code==KeyEvent.VK_F){p1Selections[p1Slot]=p1Cursor;p1Confirmed[p1Slot]=true;if(p1Slot<slots-1)p1Slot++;}
            }
            if(gameMode!=2&&gameMode!=3&&!p2Confirmed[p2Slot]){
                if(code==KeyEvent.VK_LEFT)    p2Cursor=(p2Cursor+3)%5;
                if(code==KeyEvent.VK_RIGHT)   p2Cursor=(p2Cursor+1)%5;
                if(code==KeyEvent.VK_NUMPAD1){p2Selections[p2Slot]=p2Cursor;p2Confirmed[p2Slot]=true;if(p2Slot<slots-1)p2Slot++;}
            }
            boolean ready=allP1Done()&&(gameMode==2||gameMode==3||allP2Done());
            if((code==KeyEvent.VK_ENTER)&&ready) startFight();
        }
        repaint();
    }
    @Override public void keyReleased(KeyEvent e){}
    @Override public void keyTyped(KeyEvent e){}

    private void startFight(){
        animTimer.stop();
        GamePanel gp=new GamePanel(p1Selections,p2Selections,gameMode,botSelections,botDifficulty);
        parentFrame.setContentPane(gp);
        parentFrame.revalidate();
        gp.requestFocusInWindow();
        gp.startGame();
    }
}