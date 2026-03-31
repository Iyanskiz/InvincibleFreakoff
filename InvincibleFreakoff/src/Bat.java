import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.net.URL;

public class Bat{
	private Image forward, backward, left, right; 	
	private AffineTransform tx;
	
	int dir = 0; 					//0-forward, 1-backward, 2-left, 3-right
	int width, height;
	int x, y;						//position of the object
	int vx, vy;						//movement variables
	double scaleWidth = .1;		//change to scale image
	double scaleHeight = .1; 		//change to scale image

	public Bat() {
		forward 	= getImage("/imgs/"+"batFlying.gif"); //load the image for Tree
		backward 	= getImage("/imgs/"+"batFlying.gif"); //load the image for Tree
		left 		= getImage("/imgs/"+"batFlying.gif"); //load the image for Tree
		right 		= getImage("/imgs/"+"batFlying.gif"); //load the image for Tree

		//alter these
		width = 0;
		height = 0;
		x = 0;
		y = 0;
		vx = -5;
		vy = 0;
		
		tx = AffineTransform.getTranslateInstance(0, 0);
		
		init(x, y); 				//initialize the location of the image
									//use your variables
		
	}
	
	
	//2nd constructor to allow init of x, y
	public Bat(int x, int y) {
		this(); //call def constructor for other work
		this.x = x;
		this.y = y;
		init(x,y);//makes sure image is where the x,y values are
	}
	
	public void update() {
		
		x+=vx;
		y+=vy;	
		
		if(x < -100) {
			//teleport to the right side of the screen
			x = 800;
		}
		
	}
	
	

	public void paint(Graphics g) {
		//these are the 2 lines of code needed draw an image on the screen
		Graphics2D g2 = (Graphics2D) g;
		
		
		
		update();
		
		init(x,y);
        
	       if (vx <= 0) {
	           // Flip around the duck's center
	           tx.scale(-1, 1); //<--- will not change size just flip it
	           // Move it back so it stays in the right place after flipping
	           tx.translate(-forward.getWidth(null), 0); //img being the image obj
	       }
		switch(dir) {
		case 0:
			g2.drawImage(forward, tx, null);
			break;
		case 1:
			g2.drawImage(backward, tx, null);

			break;
		case 2:
			g2.drawImage(left, tx, null);

			break;
		case 3:
			g2.drawImage(right, tx, null);
			break;
		}

	}
	
	private void init(double a, double b) {
		tx.setToTranslation(a, b);
		tx.scale(scaleWidth, scaleHeight);
	}

	private Image getImage(String path) {
		Image tempImage = null;
		try {
			URL imageURL = Bat.class.getResource(path);
			tempImage = Toolkit.getDefaultToolkit().getImage(imageURL);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tempImage;
	}

}
