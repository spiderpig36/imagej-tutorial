import ij.*;
import ij.gui.*;
import ij.plugin.tool.PlugInTool;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;
import java.awt.event.*;

public class Extended_Line_Tool extends PlugInTool {

    @Override
    public void mousePressed(ImagePlus imp, MouseEvent e) {
        int sx = e.getX();
        int sy = e.getY();

        Roi roi = imp.getRoi();
        if (roi instanceof ExtendedLine && roi.isHandle(sx, sy) != -1) {
            ((ExtendedLine) roi).mouseDownInHandle(roi.isHandle(sx, sy), sx, sy);
        } else {
            Roi newRoi = new ExtendedLine(sx, sy, imp);
            imp.setRoi(newRoi);
        }
    }

    @Override
    public void mouseDragged(ImagePlus imp, MouseEvent e) {
        Roi roi = imp.getRoi();
        if (roi instanceof ExtendedLine) {
            roi.mouseDragged(e);
        }
    }

    @Override
    public void mouseReleased(ImagePlus imp, MouseEvent e) {
        Roi roi = imp.getRoi();
        if (roi instanceof ExtendedLine) {
            roi.mouseReleased(e);
        }
    }

    @Override
    public String getToolIcon() {
        return "C00fO22dd"; // blue circle
    }

    private static class ExtendedLine extends Line {
        public ExtendedLine(int sx, int sy, ImagePlus imp) {
            super(sx, sy, imp);
        }

        @Override
        public void draw(Graphics g) {
            super.draw(g);

            double x = getXBase();
            double y = getYBase();
            x1d=x+x1R; y1d=y+y1R; x2d=x+x2R; y2d=y+y2R;
            x1=(int)x1d; y1=(int)y1d; x2=(int)x2d; y2=(int)y2d;
            int sx1 = screenXD(x1d);
            int sy1 = screenYD(y1d);
            int sx2 = screenXD(x2d);
            int sy2 = screenYD(y2d);

            if (sx1 - sx2 == 0 && sy1 - sy2 == 0) {
                return;
            }

            Vector2D lineVector = new Vector2D(sx1 - sx2, sy1 - sy2).normalize();
            Vector2D helperVector = new Vector2D(lineVector.getY(), lineVector.getX() * -1).scalarMultiply(50);

            g.setColor(Color.RED);
            this.drawHelperLine(sx1, sy1, helperVector, g);
            this.drawHelperLine(sx2, sy2, helperVector, g);
        }

        private void drawHelperLine(int x, int y, Vector2D vector, Graphics g) {
            g.drawLine((int)(x + vector.getX()),
                    (int)(y + vector.getY()),
                    (int)(x + vector.getX() * -1),
                    (int)(y + vector.getY() * -1));
        }

        @Override
        protected void updateClipRect() {
            super.updateClipRect();

            clipX -= 101;
            clipY -= 101;
            clipHeight += 202;
            clipWidth += 202;
        }

        @Override
        protected void mouseDownInHandle(int handle, int sx, int sy) {
            super.mouseDownInHandle(handle, sx, sy);
        }
    }
}
