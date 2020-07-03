import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Random;

public class ScrollImg {

    BufferedImage img = null;
    int targetAngle = 0;
    int startAngle = 0;
    double angle = 0;
    Point pos = null;
    Point targetPosition = null;
    Point startPosition = null;
    double radian = 0.0f;
    double rotate = 0.0d;
    boolean isFinished = false;
    boolean isReach = false;
    long reachTime = 0;
    int showtimelength = 0;
    TestImg test = null;

    int dWidth = 0;
    int dHeight = 0;
    private int speed = 50;

    public ScrollImg(BufferedImage b, int times, TestImg t) {
        this.img = b;
        test = t;
        showtimelength = times;

        // 座標設定
        Random ra = new Random();
        // 1/-1のランダム
        int powX = 1 - ra.nextInt(2) * 2;
        int powY = 1 - ra.nextInt(2) * 2;
        int startY = 0;
        int startX = 0;
        System.out.println("vector-gen x:" + powX + "  y:" + powY);

        dWidth = PhotoViewer.dispWidth;
        dHeight = PhotoViewer.dispHeigt;

        if (ra.nextBoolean()) {
            //Y方向に飛ばす
            startY = (int) ((dHeight / 2 + b.getHeight() * 1.5 + ra.nextInt(200)) * powY);
            //Xは-2200～2200で自由な値をとる
            startX = ra.nextInt((int) (dWidth / 2 + b.getWidth() * 1.5)) * powX;
            System.out.println("Y方向(true) x:" + startX + "  y:" + startY);
        } else {
            //X方向に飛ばす
            startX = (int) ((dWidth / 2 + b.getWidth() * 1.5 + ra.nextInt(200)) * powX);
            //Yは-1150～1150で自由な値をとる
            startY = ra.nextInt((int) (dHeight / 2 + b.getHeight() * 1.5)) * powY;
            System.out.println("X方向(false) x:" + startX + "  y:" + startY);
        }

        //初期・現在位置
        startPosition = new Point(startX - img.getWidth() / 2, startY - img.getHeight() / 2);
        pos = new Point(startX - img.getWidth() / 2, startY - img.getHeight() / 2);

        // ちょっと真ん中を通り過ぎる
        targetPosition = new Point(dWidth / 2 - img.getWidth() / 2, dHeight / 2 - img.getHeight() / 2);

        startAngle = ra.nextInt(60) - 30;
        targetAngle = ra.nextInt(60) - 30;

        // 40フレーム想定
        rotate = (targetAngle - startAngle) / ((float) showtimelength / 50);

    }

    public void tick(int time) {

        if (!isReach) {
            // 進む角度（ラジアン）
            radian = Math.atan2(targetPosition.y - pos.y, targetPosition.x - pos.x);
        }

        pos.x += speed * Math.cos(radian);
        pos.y += speed * Math.sin(radian);
        float x = pos.x - targetPosition.x;
        float y = pos.y - targetPosition.y;
        double dist = Math.sqrt(x * x + y * y);

        if (isReach) {
            if (System.currentTimeMillis() - reachTime > showtimelength) {
                if (dist > 300) {
                    speed = 30;
                } else if (dist > 50) {
                    speed = 20;
                } else if (dist > 30) {
                    speed = 10;
                }
            }
        } else {

            if (dist < 30) {
                speed = 3;
                isReach = true;
                reachTime = System.currentTimeMillis();
            } else if (dist < 50) {
                speed = 10;
            } else if (dist < 100) {
                speed = 20;
            } else if (dist < 500) {
                speed = 30;
            }
        }

        if (!isReach && dist < 1200) {
            int vectlA = (targetAngle > startAngle) ? 1 : -1;
            if (angle * vectlA < targetAngle * vectlA) {
                angle += rotate;
            }
        } else if (isReach && dist > 300) {
            angle += rotate;
        }

        if (isReach && dist > dWidth + img.getWidth() + img.getHeight()) {
            isFinished = true;
        }
    }

}
