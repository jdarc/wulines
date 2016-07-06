import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class Program {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame mainFrame = new JFrame();

            mainFrame.setTitle("Xiaolin Wu's line algorithm - now in colour!");
            mainFrame.setSize(new Dimension(1280, 800));
            mainFrame.setResizable(false);
            mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainFrame.setLocationRelativeTo(null);

            GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsConfiguration graphicsConfiguration = graphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration();

            int visibleWidth = mainFrame.getWidth() - mainFrame.getInsets().left - mainFrame.getInsets().right;
            int visibleHeight = mainFrame.getHeight() - mainFrame.getInsets().top - mainFrame.getInsets().bottom;
            BufferedImage image = graphicsConfiguration.createCompatibleImage(visibleWidth, visibleHeight);

            int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
            Painter painter = new Painter(pixels, visibleWidth, visibleHeight);

            SpinningBox[] spinners = new SpinningBox[256];
            for (int i = 0; i < spinners.length; i++) {
                float x = (float)(Math.random() * visibleWidth);
                float y = (float)(Math.random() * visibleHeight);
                float w = (float)Math.max(2.0, Math.random() * 100.0);
                float h = (float)Math.max(2.0, Math.random() * 100.0);
                int rgb = new Color((int)(Math.random() * 0xFFFFFF)).brighter().getRGB();
                float angle = (float)(Math.random() * Math.PI);
                float delta = (float)(Math.random() - 0.5) * 0.1f;
                spinners[i] = new SpinningBox(x, y, w, h, rgb, angle, delta);
            }

            new Timer(8, e -> {
                Arrays.fill(pixels, 0);

                for (SpinningBox spinner : spinners) {
                    spinner.update();
                    spinner.render(painter);
                }

                Graphics2D graphics = (Graphics2D)mainFrame.getGraphics();
                graphics.drawImage(image, mainFrame.getInsets().left, mainFrame.getInsets().top, mainFrame);
                graphics.dispose();
            }).start();

            mainFrame.setVisible(true);
        });
    }

    private static class SpinningBox {

        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final int rgb;
        private float angle;
        private final float delta;

        public SpinningBox(float x, float y, float width, float height, int rgb, float angle, float delta) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.rgb = rgb;
            this.angle = angle;
            this.delta = delta;
        }

        public void update() {
            angle += delta;
        }

        public void render(Painter painter) {
            float hw = width * 0.5f;
            float hh = height * 0.5f;
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);
            float x0 = cos * -hw - sin * -hh;
            float y0 = sin * -hw + cos * -hh;
            float x1 = cos * hw - sin * -hh;
            float y1 = sin * hw + cos * -hh;
            float x2 = cos * hw - sin * hh;
            float y2 = sin * hw + cos * hh;
            float x3 = cos * -hw - sin * hh;
            float y3 = sin * -hw + cos * hh;
            painter.line(x + x0, y + y0, x + x1, y + y1, rgb);
            painter.line(x + x1, y + y1, x + x2, y + y2, rgb);
            painter.line(x + x2, y + y2, x + x3, y + y3, rgb);
            painter.line(x + x3, y + y3, x + x0, y + y0, rgb);
        }
    }
}
