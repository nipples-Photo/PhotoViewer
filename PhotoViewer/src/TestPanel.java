import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class TestPanel extends JPanel {

	private static List<TestImg> bgList = new ArrayList<TestImg>();
	private static List<TestImg> il = Collections.synchronizedList(new ArrayList<TestImg>());


	private static long startTime = System.currentTimeMillis();
	private static int idx= 0;
	private static int idmx= 0;
	private int nextImg = 0;
	private int drawMax = 70;
	int showtimelength = 0;

	public void setShowtimelength(int tims){
		showtimelength = tims;
	}

	// canvasたち
	private static BufferedImage canvas  = null;
	private static BufferedImage bgCanvas  = null;

	private int interval = 1*5*1000;
	private int whiteinterval = 1500;

	private int lastIdx = 0;

	private List<ScrollImg> que = new ArrayList<ScrollImg>();
	public TestPanel(){
		setOpaque(false);
	}
	public void setInterval(int iv){
		this.interval = iv;
	}
	public void setMaxPix(int mP){
		this.drawMax = mP;
	}

	@Override
	public void paintComponent(Graphics g) {
		// 処理後の時刻を取得
        long now = System.currentTimeMillis();

		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;


		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_SPEED);

		if(now - startTime > interval){
	        	idx++;
	        	if(idx >= idmx){
	        		idx = 0;
	        	}
	        	startTime = now;
	    }
		if(bgList.size() != 0 ){

			int whitening = 0;
			if (now - startTime < whiteinterval ){
				//切り替わり直後
				whitening = (int)(whiteinterval - ( now - startTime ));
			}else if (now - startTime > interval - whiteinterval ){
				//切り替わり前
				whitening = (int) (now - startTime) - interval +  whiteinterval;
			}// interval

			TestImg  img = bgList.get(idx);
    		paintBg(img.img,whitening );
		}
		createImg();

		g2d.drawImage(bgCanvas, 0, 0,this);
		g2d.drawImage(canvas, 0, 0,this);

		time++;
	}
	private void paintBg(BufferedImage i , int whitening){

        bgCanvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2D = bgCanvas .createGraphics();
		Rectangle2D.Double rect = new Rectangle2D.Double(0,0,bgCanvas .getWidth(),bgCanvas .getHeight());
		g2D.setColor(Color.BLACK);
		g2D.fill(rect);
		g2D.setPaintMode();

		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_SPEED);

		int pnlw = getSize().width;
        int imgh = i.getHeight() * pnlw / i.getWidth();
        int x = getSize().width / 2 -  pnlw/2;
        int y = getSize().height / 2 -  imgh/2;
        g2D.drawImage(i, x, y, pnlw, imgh, null);
        if (whitening != 0){
        	g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT, (float)whitening/whiteinterval));
	     	Color clr2 = new Color((float)whitening/whiteinterval, 1f, 1f, 1f);
	     	g2D.setColor(clr2);
	     	g2D.fill(rect);
        }
		 g2D.dispose();

	}

	public void addImage(BufferedImage defaultImg,TestImg t) {

		que.add(new  ScrollImg(defaultImg,showtimelength,t));
	}

	public void removeByName(String name){
		TestImg rmvTgt = null;
		synchronized (il) {
			 for (TestImg i : il) {
				if(i.name.equals(name)){
					rmvTgt = i;
				}
			}

		}

		if (rmvTgt != null){
			il.remove(rmvTgt);
		}
	}
	private int time = 0;

	public void setBgLoadPath(String path,boolean isBg) {
		// ファイル名の一覧を取得する
		File file = new File(path);
		File files[] = file.listFiles();

		List<TestImg> tmpList = new ArrayList<TestImg>();

		// 取得した一覧を表示する
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().endsWith(".png") || files[i].getName().endsWith(".jpg") || files[i].getName().endsWith(".jpeg")) {
				try {
					BufferedImage img  = null;
					if(!isBg){
						Random ra = new Random();

						img = PhotoViewer.addBorder(PhotoViewer.scaleImage(files[i], (PhotoViewer.dispHeigt ) /3),PhotoViewer.cusatomScaleF);
						TestImg newImg =new TestImg(img,files[i].getName());
						//初回はx軸をばらけさせる
						newImg .position =  new Point(PhotoViewer.dispWidth + newImg.size.width +ra.nextInt(1000),ra.nextInt(PhotoViewer.dispHeigt-newImg.size.height) );
						//多すぎると重いので
						if(i > drawMax){newImg.isRenderable = false;}
						tmpList.add( newImg);


					}else{
						img = ImageIO.read(files[i]);
						tmpList.add( new TestImg(img,0));
					}
				} catch (Exception e) {/* nothing */
				}
			}
		}
		if (isBg){
			bgList.addAll(tmpList);
			idmx = bgList.size();
		}else{
			il.addAll( tmpList);
		}
	}

	public void createImg(){

		if(getWidth() == 0 || getHeight() == 0){
			//サイズが0なら描画しない
			return;
		}
			canvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2D = canvas .createGraphics();
		g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.5f));
		Rectangle2D.Double rect = new Rectangle2D.Double(0,0,canvas .getWidth(),canvas .getHeight());
		g2D.fill(rect);
		g2D.setPaintMode();

		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_SPEED);

		AffineTransform at = g2D.getTransform();
		int cnt = 0;
		synchronized (il) {

			 for (TestImg i : il) {
				 if(i.isRenderable){
					 i.tick(time);
					 // 回転角度と画像の中心点を指定する
					 at.setToRotation(Math.toRadians(i.angle),i.position.x + i.size.getWidth() / 2, i.position.y + i.size.getHeight() / 2);
					 g2D.setTransform(at);
					 g2D.drawImage(i.img, i.position.x, i.position.y,this);
					cnt++;
				 }
			}
		}

		if(il.size() < drawMax ){
			//全部スイッチオン
			synchronized (il) {
				 for (TestImg i : il) {
					 if(!i.isRenderable){
						 i.restart();
						 cnt++;
					 }
				 }
			 }
		}else {
			// lastIdxから始めてcnt>=100まで
			 for (int n =0 ; n < il.size() && cnt < drawMax ; n++){
				 // 画像削除されるとエラーになるため先にインクリメント
				 lastIdx++;
				 if (lastIdx >= il.size()){lastIdx = 0;}
				 TestImg t = il.get(lastIdx);
				 if(!t.isRenderable){
					 t.restart();
					 cnt++;
				 }
			 }
		}

		if(que.size() !=  0){
			ScrollImg smg = que.get(0);
			smg.tick(time);
			at.setToRotation(Math.toRadians(smg.angle),smg.pos.x + smg.img.getWidth() / 2, smg.pos.y + smg.img.getHeight() / 2);
			g2D.setTransform(at);
			g2D.drawImage(smg.img, smg.pos.x, smg.pos.y,this);
			if(smg.isFinished){
				il.add(que.get(0).test);
				que.remove(0);
			}
		}

		 g2D.dispose();
	}

	private List<TestImg> getNext30(int speed){
		int start = nextImg;
		List<TestImg> rtnrest = new ArrayList<TestImg>();
		int i = 0;
		int mIdx = 0;

		for (i = 0;  i < 100 ; i++){

			if (i > il.size()){
				break;
			}
			mIdx =  (start + i) % il.size();
			rtnrest.add(il.get(mIdx));
		}
		nextImg = mIdx;

		return rtnrest;
	}
}

