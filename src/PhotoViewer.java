import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PhotoViewer extends JFrame implements MouseListener {

    private static PhotoViewer j;
    private static TestPanel p = null;
    static int cnt = 0;
    static int last_posX = 0;
    private static String s = "";
    private static JLabel label = new JLabel();
    private static String sPath = "";
    private static String tPath = "";
    private static int maxPix = 0;
    private static int interval = 0;
    private static boolean ud = false;
    private static int initWidth = 1280;
    private static int initHeight = 720;
    static int dispWidth = 0;
    static int dispHeigt = 0;

    static int showtimelength = 0;
    static int cusatomScaleFB = 0;
    static int cusatomScaleF = 0;

    public static void main(String[] args) throws IOException, InterruptedException {

        ResourceBundle rb = ResourceBundle.getBundle("Photo");

        sPath = rb.getString("synchPath");
        tPath = rb.getString("tempPath");
        maxPix = Integer.parseInt(rb.getString("maxPix"));
        interval = Integer.parseInt(rb.getString("interval"));
        ud = (rb.getString("undecorate").equals("true"));
        showtimelength = Integer.parseInt(rb.getString("showtimelength"));
        String customX = rb.getString("customScaleX");
        String customY = rb.getString("customScaleY");
        cusatomScaleFB = Integer.parseInt(rb.getString("customScaleFrameBold"));
        cusatomScaleF = Integer.parseInt(rb.getString("customScaleFrame"));
        java.awt.GraphicsEnvironment env = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        java.awt.DisplayMode displayMode = env.getDefaultScreenDevice().getDisplayMode();
        // 変数widthとheightに画面の解像度の幅と高さを代入
        dispWidth = displayMode.getWidth();
        dispHeigt = displayMode.getHeight();

        // 設定された場合、カスタムスケールで起動（解像度対策）
        if (!customX.equals("") && !customY.equals("")) {
            dispWidth = Integer.parseInt(customX);
            dispHeigt = Integer.parseInt(customY);
        }

        // 枠なしだとサイズ変更できないので、モニタのサイズでinitする
        // カスタムスケール指定の場合は、そのサイズで起動
        if (ud || (!customX.equals("") && !customY.equals(""))) {
            initWidth = dispWidth;
            initHeight = dispHeigt;
        }

        j = new PhotoViewer();
        j.setDefaultCloseOperation(EXIT_ON_CLOSE);

        Path dir = Paths.get(sPath);
        WatchService watcher = FileSystems.getDefault().newWatchService();
        dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        TestPanel nj = new TestPanel();
        nj.setMaxPix(maxPix);
        nj.setInterval(interval);
        nj.setShowtimelength(showtimelength);
        j.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        j.setLayout(new BoxLayout(j.getContentPane(), BoxLayout.PAGE_AXIS));
        j.getContentPane().add(nj);
        nj.setLayout(null);
        nj.setBgLoadPath(tPath, true);
        nj.setBgLoadPath(sPath, false);
        nj.addMouseListener(j);
        nj.setFocusable(true);

        p = nj;
        Runnable r = new Runnable() {
            private JPanel pane = null;

            public Runnable setP(JPanel p) {
                this.pane = p;
                return this;
            }

            public void run() {
                for (;;) {
                    try {
                        Thread.sleep(65);
                    } catch (InterruptedException e) {
                        // TODO 自動生成された catch ブロック
                        e.printStackTrace();
                    }
                    p.repaint();
                }
            }
        }.setP(nj);

        Thread t = new Thread(r);
        t.start();

        for (;;) {
            WatchKey watchKey = watcher.take();
            for (WatchEvent<?> event : watchKey.pollEvents()) {
                if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
                System.out.format("%s: %s\n", event.kind().name(), child);
                if (StandardWatchEventKinds.ENTRY_CREATE == event.kind()) {
                    //変更のあったファイルのパス
                    String path = child.toString();
                    File file = new File(path);

                    // TEST ファイルの書き込み可否≒ロック状態を知りたい
                    while (!file.canWrite()) {
                        System.out.println(path + " : can not write");
                    }

                    Thread.sleep(1000);

                    BufferedImage i = addBorder(scaleImage(file, (dispHeigt) / 3), cusatomScaleF);

                    p.addImage(addBorder(scaleImage(file, dispHeigt - 180), cusatomScaleFB), new TestImg(i, file.getName()));

                } else if (StandardWatchEventKinds.ENTRY_DELETE == event.kind()) {
                    //変更のあったファイルのパス
                    String path = child.toString();
                    File file = new File(path);
                    p.removeByName(file.getName());
                }
            }
            watchKey.reset();
        }
    }

    public static BufferedImage addBorder(BufferedImage image, int Lwidth) {

        BufferedImage result = new BufferedImage(image.getWidth() + Lwidth * 2 + 4, image.getHeight() + Lwidth * 2 + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.drawImage(image, Lwidth + 2, Lwidth + 2, null);

        Stroke stroke1 = new BasicStroke(Lwidth + 1f);

        g.setColor(Color.WHITE);
        g.setStroke(stroke1);
        g.drawRect(Lwidth / 2 + 1, Lwidth / 2 + 1, image.getWidth() + Lwidth + 1, image.getHeight() + Lwidth + 1);

        g.dispose();
        return result;
    }

    public PhotoViewer() {
        super();
        setSize(new Dimension(initWidth, initHeight));
        getContentPane().setBackground(Color.WHITE);

        if (ud) {
            this.setUndecorated(true);
            System.out.println("undecorated");
        }

        getContentPane().setLayout(null);
        this.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    Image img;

    public static void setImage(String url) {
        ImageIcon i = new ImageIcon(url);
        label.setIcon(i);
        j.validate();
        j.repaint();
    }

    private static boolean isFull = false;

    public static BufferedImage scaleImage(File in, int target) throws IOException {

        BufferedImage org = ImageIO.read(in);
        int height = org.getHeight();
        int width = org.getWidth();
        int base = 0;
        if (height > width) {
            base = height;
        } else {
            base = width;
        }
        double scale = (double) target / base;
        ImageFilter filter = new AreaAveragingScaleFilter((int) (org.getWidth() * scale), (int) (org.getHeight() * scale));
        ImageProducer p = new FilteredImageSource(org.getSource(), filter);
        java.awt.Image dstImage = Toolkit.getDefaultToolkit().createImage(p);
        BufferedImage dst = new BufferedImage(dstImage.getWidth(null), dstImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.drawImage(dstImage, 0, 0, null);
        g.dispose();
        return dst;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() >= 2) {

            if (!isFull) {
                /* フルスクリーンにするウィンドウを登録する */
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);
                isFull = true;
            } else {
                /* フルスクリーンにするウィンドウを登録する */
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
                isFull = false;
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
