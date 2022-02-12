package me.kolterdyx.neat.utils.graph;

import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.util.Camera;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

public class MouseListener implements java.awt.event.MouseListener {

    private static MouseEvent lastMouseEvent=null;

    public static MouseWheelListener wheelListener(View view){
        return (e -> {
            e.consume();
            int i = e.getWheelRotation();
            double factor = Math.pow(1.25, i);
            Camera cam = view.getCamera();
            double zoom = cam.getViewPercent() * factor;
            Point2 pxCenter  = cam.transformGuToPx(cam.getViewCenter().x, cam.getViewCenter().y, 0);
            Point3 guClicked = cam.transformPxToGu(e.getX(), e.getY());
            double newRatioPx2Gu = cam.getMetrics().ratioPx2Gu/factor;
            double x = guClicked.x + (pxCenter.x - e.getX())/newRatioPx2Gu;
            double y = guClicked.y - (pxCenter.y - e.getY())/newRatioPx2Gu;
            cam.setViewCenter(x, y, 0);
            cam.setViewPercent(zoom);
        });
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        MouseMotionListener.resetDrag();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }


    public static class MouseMotionListener implements java.awt.event.MouseMotionListener {
        private final View view;
        private final Camera camera;
        private static MouseEvent last;

        public MouseMotionListener(View view){
            this.view = view;
            camera = view.getCamera();
        }

        @Override
        public void mouseDragged(MouseEvent mouseEvent) {
            if (SwingUtilities.isLeftMouseButton(mouseEvent)) processDrag(mouseEvent);
        }

        @Override
        public void mouseMoved(MouseEvent mouseEvent) {

        }

        public void processDrag(MouseEvent event)
        {
            if(last!=null) {
                Point3 p1 = camera.getViewCenter();
                Point3 p2=camera.transformGuToPx(p1.x,p1.y,0);
                int xdelta=event.getX()-last.getX();//determine direction
                int ydelta=event.getY()-last.getY();//determine direction
                p2.x-=xdelta;
                p2.y-=ydelta;
                Point3 p3=camera.transformPxToGu(p2.x,p2.y);
                camera.setViewCenter(p3.x,p3.y, 0);
            }
            last=event;
        }

        public static void resetDrag(){
            last=null;
        }
    }
}
