import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Random;


public class TestImg {
	BufferedImage img = null;
	int angle = 0;
	Point position = null;
	Dimension size = null;
	int speed = 0;
	double scale = 1.0;
	private static Random ra = new Random();

	boolean isRenderable = true;

	int height = 0;
	int width = 0;

	String name = "";

	public TestImg(BufferedImage b,int a){
		//背景画像用
		this.img = b;
		speed = 0;
		position = new Point(0,0);
		size = new Dimension(0, 0);
		angle= 0;
	}
	public TestImg(BufferedImage b,String n){
		this.img = b;
		this.name = n;
		size = new Dimension(b.getWidth(), b.getHeight());
		// 勝手に貰っておく
		this.height = PhotoViewer.dispHeigt;
		this.width = PhotoViewer.dispWidth;
		restart();
	}

	public void restart(){

		isRenderable = true;
		// 座標再設定

		speed = ra.nextInt(6) + 3;
		if(speed == 1 || speed == 2){
			speed = ra.nextInt(6) + 1;
		}
		angle = ra.nextInt(60) -30;
		position = new Point((int) (width + size.getWidth() * 1.5),(int) (ra.nextInt((int) (height  - size.getHeight()/2 )) - size.getHeight()/4)   );
	}


	public void tick(int time){
		if(isRenderable){
			this.position.x -= speed;
			if (position.x <= -(size.width * 1.5) ){
				// 移動の停止
				isRenderable = false;
			}
		}
	}


}
