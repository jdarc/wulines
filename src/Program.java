import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

class Program {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            var mainFrame = new JFrame();
            mainFrame.setTitle("Anti-aliased Lines - Xiaolin Wu's Algorithm");
            mainFrame.setSize(new Dimension(1440, 900));
            mainFrame.setResizable(false);
            mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainFrame.setLocationRelativeTo(null);

            var visibleWidth = mainFrame.getWidth() - mainFrame.getInsets().left - mainFrame.getInsets().right;
            var visibleHeight = mainFrame.getHeight() - mainFrame.getInsets().top - mainFrame.getInsets().bottom;
            var image = mainFrame.getGraphicsConfiguration().createCompatibleImage(visibleWidth, visibleHeight);
            var painter = new Painter(image);

            var spinners = new Box[256];
            for (var i = 0; i < spinners.length; i++) {
                var x = (Math.random() * visibleWidth);
                var y = (Math.random() * visibleHeight);
                var w = Math.max(2.0, Math.random() * 100.0);
                var h = Math.max(2.0, Math.random() * 100.0);
                var rgb = new Color((int)(Math.random() * 0xAAAAAA)).brighter().getRGB();
                var angle = (Math.random() * Math.PI);
                var delta = (Math.random() - 0.5) / 10.0;
                spinners[i] = new Box(x, y, w, h, rgb, angle, delta);
            }

            new Timer(8, e -> {
                painter.clear(0x000000);

                for (var spinner : spinners) {
                    spinner.update();
                    spinner.render(painter);
                }

                var graphics = (Graphics2D)mainFrame.getGraphics();
                graphics.drawImage(image, mainFrame.getInsets().left, mainFrame.getInsets().top, mainFrame);
                graphics.dispose();
            }).start();

            mainFrame.setVisible(true);
        });
    }

    private static class Box {
        private final double x;
        private final double y;
        private final double width;
        private final double height;
        private final int rgb;
        private final double delta;
        private double angle;

        public Box(double x, double y, double width, double height, int rgb, double angle, double delta) {
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
            var cos = Math.cos(angle);
            var sin = Math.sin(angle);
            var hw = width * 0.5;
            var hh = height * 0.5;
            var x0 = cos * -hw - sin * -hh;
            var y0 = sin * -hw + cos * -hh;
            var x1 = cos * hw - sin * -hh;
            var y1 = sin * hw + cos * -hh;
            var x2 = cos * hw - sin * hh;
            var y2 = sin * hw + cos * hh;
            var x3 = cos * -hw - sin * hh;
            var y3 = sin * -hw + cos * hh;
            painter.line(x + x0, y + y0, x + x1, y + y1, rgb);
            painter.line(x + x1, y + y1, x + x2, y + y2, rgb);
            painter.line(x + x2, y + y2, x + x3, y + y3, rgb);
            painter.line(x + x3, y + y3, x + x0, y + y0, rgb);
        }
    }

    private static class Painter {
        public final int width;
        public final int height;
        public final int[] pixels;
        private final Rectangle rect;

        public Painter(BufferedImage image) {
            width = image.getWidth();
            height = image.getHeight();
            pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
            rect = new Rectangle(0, 0, width - 2, height - 2);
        }

        public void clear(int rgb) {
            Arrays.fill(pixels, rgb);
        }

        // Clipping algorithm from http://www.jfree.org/jfreechart/index.html
        public void line(double x0, double y0, double x1, double y1, int rgb) {
            var minX = rect.getMinX();
            var maxX = rect.getMaxX();
            var minY = rect.getMinY();
            var maxY = rect.getMaxY();

            var f1 = rect.outcode(x0, y0);
            var f2 = rect.outcode(x1, y1);

            while ((f1 | f2) != 0) {
                if ((f1 & f2) == 0) {
                    var dx = x1 - x0;
                    var dy = y1 - y0;
                    // update (x1, y1), (x2, y2) and f1 and f2 using intersections then recheck
                    if (f1 != 0) {
                        // first point is outside, so we update it against one of the four sides then continue
                        if ((f1 & Rectangle2D.OUT_LEFT) == Rectangle2D.OUT_LEFT && dx != 0.0) {
                            y0 = y0 + (minX - x0) * dy / dx;
                            x0 = minX;
                        } else if ((f1 & Rectangle2D.OUT_RIGHT) == Rectangle2D.OUT_RIGHT && dx != 0.0) {
                            y0 = y0 + (maxX - x0) * dy / dx;
                            x0 = maxX;
                        } else if ((f1 & Rectangle2D.OUT_BOTTOM) == Rectangle2D.OUT_BOTTOM && dy != 0.0) {
                            x0 = x0 + (maxY - y0) * dx / dy;
                            y0 = maxY;
                        } else if ((f1 & Rectangle2D.OUT_TOP) == Rectangle2D.OUT_TOP && dy != 0.0) {
                            x0 = x0 + (minY - y0) * dx / dy;
                            y0 = minY;
                        }
                        f1 = rect.outcode(x0, y0);
                    } else if (f2 != 0) {
                        // second point is outside, so we update it against one of the four sides then continue
                        if ((f2 & Rectangle2D.OUT_LEFT) == Rectangle2D.OUT_LEFT && dx != 0.0) {
                            y1 = y1 + (minX - x1) * dy / dx;
                            x1 = minX;
                        } else if ((f2 & Rectangle2D.OUT_RIGHT) == Rectangle2D.OUT_RIGHT && dx != 0.0) {
                            y1 = y1 + (maxX - x1) * dy / dx;
                            x1 = maxX;
                        } else if ((f2 & Rectangle2D.OUT_BOTTOM) == Rectangle2D.OUT_BOTTOM && dy != 0.0) {
                            x1 = x1 + (maxY - y1) * dx / dy;
                            y1 = maxY;
                        } else if ((f2 & Rectangle2D.OUT_TOP) == Rectangle2D.OUT_TOP && dy != 0.0) {
                            x1 = x1 + (minY - y1) * dx / dy;
                            y1 = minY;
                        }
                        f2 = rect.outcode(x1, y1);
                    }
                } else {
                    return;
                }
            }

            wuline(x0, y0, x1, y1, rgb);
        }

        // https://en.wikipedia.org/wiki/Xiaolin_Wu%27s_line_algorithm
        private void wuline(double x0, double y0, double x1, double y1, int rgb) {
            var steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);

            double t;
            if (steep) {
                t = y0; y0 = x0; x0 = t;
                t = y1; y1 = x1; x1 = t;
            }

            if (x0 > x1) {
                t = x0; x0 = x1; x1 = t;
                t = y0; y0 = y1; y1 = t;
            }

            var dx = x1 - x0;
            var dy = y1 - y0;
            var gradient = dy / dx;

            // handle first endpoint
            var xend0 = round(x0);
            var yend0 = y0 + gradient * (xend0 - x0);
            wupixel(steep, xend0, yend0, rfpart(x0 + 0.5), rgb);

            // handle second endpoint
            var xend1 = round(x1);
            var yend1 = y1 + gradient * (xend1 - x1);
            wupixel(steep, xend1, yend1, fpart(x1 + 0.5), rgb);

            // main loop
            var intery = yend0 + gradient;
            for (var x = xend0 + 1; x <= xend1 - 1; ++x) {
                wupixel(steep, x, intery, 1.0, rgb);
                intery += gradient;
            }
        }

        private void wupixel(boolean steep, int x, double intery, double gap, int rgb) {
            if (steep) {
                plot(ipart(intery), x, rfpart(intery) * gap, rgb);
                plot(ipart(intery) + 1, x, fpart(intery) * gap, rgb);
            } else {
                plot(x, ipart(intery), rfpart(intery) * gap, rgb);
                plot(x, ipart(intery) + 1, fpart(intery) * gap, rgb);
            }
        }

        private void plot(int x, int y, double c, int rgb) {
            var c256 = (int)(256.0 * c);
            var offset = x + y * width;
            var pixel = pixels[offset];
            var rb = (0xFF00FF & rgb) * c256 + (0xFF00FF & pixel) * (256 - c256);
            var ag = (0x00FF00 & rgb) * c256 + (0x00FF00 & pixel) * (256 - c256);
            pixels[offset] = rb >> 8 & 0xFF00FF | ag >> 8 & 0x00FF00;
        }

        private static int ipart(double x) {
            return (int)x;
        }

        private static int round(double x) {
            return ipart(x + 0.5);
        }

        private static double fpart(double x) {
            return x < 0.0 ? (int)x - x : x - (int)x;
        }

        private static double rfpart(double x) {
            return 1.0 - fpart(x);
        }
    }
}
