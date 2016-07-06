import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Painter {

    public final int[] pixels;
    public final int width;
    public final int height;
    private final Rectangle rect;

    public Painter(int[] pixels, int width, int height) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.rect = new Rectangle(0, 0, width - 2, height - 2);
    }

    /**
     * Clipping algorithm from http://www.jfree.org/jfreechart/index.html
     */
    public void line(double x0, double y0, double x1, double y1, int rgb) {
        double minX = rect.getMinX();
        double maxX = rect.getMaxX();
        double minY = rect.getMinY();
        double maxY = rect.getMaxY();

        int f1 = rect.outcode(x0, y0);
        int f2 = rect.outcode(x1, y1);

        while ((f1 | f2) != 0) {
            if ((f1 & f2) == 0) {
                double dx = x1 - x0;
                double dy = y1 - y0;
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

    /**
     * https://en.wikipedia.org/wiki/Xiaolin_Wu%27s_line_algorithm
     */
    private void wuline(double x0, double y0, double x1, double y1, int rgb) {
        boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);

        double t;
        if (steep) {
            t = y0; y0 = x0; x0 = t;
            t = y1; y1 = x1; x1 = t;
        }

        if (x0 > x1) {
            t = x0; x0 = x1; x1 = t;
            t = y0; y0 = y1; y1 = t;
        }

        double dx = x1 - x0;
        double dy = y1 - y0;
        double gradient = dy / dx;

        // handle first endpoint
        int xend0 = round(x0);
        double yend0 = y0 + gradient * (xend0 - x0);
        wupixel(steep, xend0, yend0, rfpart(x0 + 0.5), rgb);

        // handle second endpoint
        int xend1 = round(x1);
        double yend1 = y1 + gradient * (xend1 - x1);
        wupixel(steep, xend1, yend1, fpart(x1 + 0.5), rgb);

        // main loop
        double intery = yend0 + gradient;
        for (int x = xend0 + 1; x <= xend1 - 1; ++x) {
            wupixel(steep, x, intery, 1.0, rgb);
            intery += gradient;
        }
    }

    private void wupixel(boolean steep, int x, double intery, double gap, int rgb) {
        if (steep) {
            plot(ipart(intery) + 0, x, rfpart(intery) * gap, rgb);
            plot(ipart(intery) + 1, x, fpart(intery) * gap, rgb);
        } else {
            plot(x, ipart(intery) + 0, rfpart(intery) * gap, rgb);
            plot(x, ipart(intery) + 1, fpart(intery) * gap, rgb);
        }
    }

    private void plot(int x, int y, double c, int rgb) {
        int c256 = (int)(256.0 * c);
        int offset = x + y * width;
        int pixel = pixels[offset];
        pixels[offset] = ((0xFF00FF & rgb) * c256 + (0xFF00FF & pixel) * (256 - c256) >> 8 & 0xFF00FF) |
                         ((0x00FF00 & rgb) * c256 + (0x00FF00 & pixel) * (256 - c256) >> 8 & 0x00FF00);
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
