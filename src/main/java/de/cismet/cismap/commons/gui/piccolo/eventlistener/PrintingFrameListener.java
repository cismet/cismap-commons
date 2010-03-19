/*
 * PrintingFrameListener.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 5. Juli 2006, 16:09
 *
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.printing.PrintingToolTip;

import de.cismet.tools.CismetThreadPool;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JOptionPane;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class PrintingFrameListener extends PBasicInputEventHandler {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    MappingComponent mappingComponent = null;
    Point2D startDragPosition;
    double widthToHeightRatio = 1.0d;
    double scaleDenominator = 0d;
    int placeholderWidth = 0;
    boolean inDragOperation = false;
    int placeholderHeight = 0;
    double realWorldWidth = 0;
    double realWorldHeight = 0;
    public static final double DEFAULT_JAVA_RESOLUTION_IN_DPI = 72d;
    public static final double MILLIMETER_OF_AN_INCH = 25.4d;
    public static final double INCH_OF_A_MILLIMETER = 0.039d;
    public static final double MILLIMETER_OF_A_METER = 1000d;
    PLayer layer;
    protected PPath north = new PPath();
    protected PPath south = new PPath();
    protected PPath east = new PPath();
    protected PPath west = new PPath();
    private String bestimmerDimension=WIDTH;
    public static final String WIDTH = "WIDTH";
    public static final String HEIGHT = "HEIGHT";
    public static final Color BORDER_COLOR = new Color(255, 255, 255, 200);
    private String oldInteractionMode = "";
    private PPath printingRectangle = new PPath() {
        protected void paint(PPaintContext paintContext) {
            super.paint(paintContext);
            PBounds vb = mappingComponent.getCamera().getViewBounds();
            PBounds rb = getPrintingRectangle().getBounds();
            GeneralPath newNorthBounds = new GeneralPath(new Rectangle2D.Double(vb.getMinX(), vb.getMinY(), vb.getWidth(), rb.getMinY() - vb.getMinY()));
            GeneralPath newSouthBounds = new GeneralPath(new Rectangle2D.Double(vb.getMinX(), rb.getMaxY(), vb.getWidth(), vb.getMaxY() - rb.getMaxY()));
            GeneralPath newEastBounds = new GeneralPath(new Rectangle2D.Double(vb.getMinX(), rb.getMinY(), rb.getMinX() - vb.getMinX(), rb.getHeight()));
            GeneralPath newWestBounds = new GeneralPath(new Rectangle2D.Double(rb.getMaxX(), rb.getMinY(), vb.getMaxX() - rb.getMaxX(), rb.getHeight()));
            //to prevent painting loops check before setPathTo
            if (!(north.getPathReference().getBounds2D().equals(newNorthBounds.getBounds2D()))) {
                north.setPathTo(newNorthBounds);
            }
            if (!(south.getPathReference().getBounds2D().equals(newSouthBounds.getBounds2D()))) {
                south.setPathTo(newSouthBounds);
            }
            if (!(east.getPathReference().getBounds2D().equals(newEastBounds.getBounds2D()))) {
                east.setPathTo(newEastBounds);
            }
            if (!(west.getPathReference().getBounds2D().equals(newWestBounds.getBounds2D()))) {
                west.setPathTo(newWestBounds);
            }
        }
    };

    /** Creates a new instance of PrintingFrameListener */
    public PrintingFrameListener(MappingComponent mappingComponent) {
        this.mappingComponent = mappingComponent;
        north.setPaint(BORDER_COLOR);
        north.setStroke(null);
        north.setStrokePaint(null);
        south.setPaint(BORDER_COLOR);
        south.setStroke(null);
        south.setStrokePaint(null);
        east.setPaint(BORDER_COLOR);
        east.setStroke(null);
        east.setStrokePaint(null);
        west.setPaint(BORDER_COLOR);
        west.setStroke(null);
        west.setStrokePaint(null);
        
        //printingRectangle.setStroke(new FixedWidthStroke());
        getPrintingRectangle().setStroke(null);
        getPrintingRectangle().setPaint(Color.yellow);
        getPrintingRectangle().setPaint(new Color(20, 20, 20, 1));
        layer = mappingComponent.getPrintingFrameLayer();
    //layer=mappingComponent.getTmpFeatureLayer();
    }

    public void init(double scaleDenominator,
            int placeholderWidth,
            int placeholderHeight, String oldInteractionMode) {
//        log.fatal(scaleDenominator+ " "+placeholderWidth+" "+placeholderHeight);
        this.oldInteractionMode = oldInteractionMode;
        this.widthToHeightRatio = (double) placeholderWidth / (double) placeholderHeight;
        this.scaleDenominator = scaleDenominator;
        this.placeholderHeight = placeholderHeight;
        this.placeholderWidth = placeholderWidth;
        double mapWidth = mappingComponent.getCamera().getViewBounds().getWidth();
        double mapHeight = mappingComponent.getCamera().getViewBounds().getHeight();

        //calculate realworldsize
        if (scaleDenominator == -1) {
            String s = JOptionPane.showInputDialog(mappingComponent, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("Massstab_manuell_auswaehlen"), "");
            try {
                Double d = new Double(s);
                scaleDenominator = d;
                realWorldWidth = placeholderWidth / DEFAULT_JAVA_RESOLUTION_IN_DPI * MILLIMETER_OF_AN_INCH / MILLIMETER_OF_A_METER * scaleDenominator;
                realWorldHeight = placeholderHeight / DEFAULT_JAVA_RESOLUTION_IN_DPI * MILLIMETER_OF_AN_INCH / MILLIMETER_OF_A_METER * scaleDenominator;
            } catch (Exception skip) {
                scaleDenominator = 0;
            }
        } else if (scaleDenominator == 0) {
            //no fixed scale
            if (widthToHeightRatio / (mapWidth / mapHeight) < 1) {
                //height is the critical value and must be shrinked. in german: bestimmer ;-)
                realWorldHeight = mapHeight * 0.75;
                realWorldWidth = realWorldHeight * widthToHeightRatio;
                bestimmerDimension = PrintingFrameListener.HEIGHT;
            } else {
                //width is the critical value and must be shrinked. in german: bestimmer ;-)
                realWorldWidth = mapWidth * 0.75;
                realWorldHeight = (double) realWorldWidth / (double) widthToHeightRatio;
                bestimmerDimension = PrintingFrameListener.WIDTH;
            }

        } else {
            realWorldWidth = placeholderWidth / DEFAULT_JAVA_RESOLUTION_IN_DPI * MILLIMETER_OF_AN_INCH / MILLIMETER_OF_A_METER * scaleDenominator;
            realWorldHeight = placeholderHeight / DEFAULT_JAVA_RESOLUTION_IN_DPI * MILLIMETER_OF_AN_INCH / MILLIMETER_OF_A_METER * scaleDenominator;
        }
        Point2D center = mappingComponent.getCamera().getViewBounds().getCenter2D();
        PBounds pb = new PBounds(0, 0, realWorldWidth, realWorldHeight);
        getPrintingRectangle().setPathTo(pb);
        getPrintingRectangle().centerBoundsOnPoint(center.getX(), center.getY());
        layer.removeAllChildren();
        layer.addChild(getPrintingRectangle());
        layer.addChild(north);
        layer.addChild(south);
        layer.addChild(east);
        layer.addChild(west);
        getPrintingRectangle().moveToFront();
        getPrintingRectangle().repaint();
        
        if (scaleDenominator != 0 && !mappingComponent.isFixedMapScale()) {
            PBounds b = getPrintingRectangle().getBounds();
            PBounds mover = new PBounds(b.getX() - b.getWidth() * (0.25 / 2.0), b.getY() - b.getHeight() * (0.25 / 2.0), b.getWidth() * 1.25, b.getHeight() * 1.25);
            mappingComponent.getCamera().animateViewToCenterBounds(mover, true, mappingComponent.getAnimationDuration());
            mappingComponent.queryServices();
        }
        mappingComponent.setPointerAnnotation(new PrintingToolTip(new Color(255, 255, 222, 200)));
        mappingComponent.setPointerAnnotationVisibility(true);
    }

    public double getScaleDenominator() {
        double real;
        double paper;
        if (bestimmerDimension.equals(PrintingFrameListener.HEIGHT)) {
            real = getPrintingBoundingBox().getHeight();
            paper = placeholderHeight / DEFAULT_JAVA_RESOLUTION_IN_DPI * MILLIMETER_OF_AN_INCH / MILLIMETER_OF_A_METER;
        } else {
            real = getPrintingBoundingBox().getWidth();
            paper = placeholderWidth / DEFAULT_JAVA_RESOLUTION_IN_DPI * MILLIMETER_OF_AN_INCH / MILLIMETER_OF_A_METER;
        }
        return real / paper;
    }
    

    @Override
    public void mouseReleased(PInputEvent event) {
        super.mouseReleased(event);
        if (inDragOperation && startDragPosition.distance(event.getCanvasPosition()) > 2) {
            mappingComponent.getCamera().animateViewToCenterBounds(getPrintingRectangle().getBounds(), false, 500);
            mappingComponent.queryServices();
        }
        inDragOperation = false;
    }

    @Override
    public void mousePressed(PInputEvent event) {
        super.mousePressed(event);
        log.debug("mousePressed:" + event);
        if (event.getPickedNode() == getPrintingRectangle() && event.getClickCount() < 2) {
            inDragOperation = true;
            startDragPosition = event.getCanvasPosition();
        }
    }

    @Override
    public void mouseMoved(PInputEvent event) {
        super.mouseMoved(event);
        if (event.getPickedNode() == getPrintingRectangle()) {
            mappingComponent.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            mappingComponent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    @Override
    public void mouseDragged(PInputEvent event) {
        super.mouseDragged(event);
        if (inDragOperation) {
            PBounds b = getPrintingRectangle().getBounds();
            b.setOrigin(b.getOrigin().getX() + event.getDelta().getWidth(), b.getOrigin().getY() + event.getDelta().getHeight());
            getPrintingRectangle().setPathTo(b);
        //printingRectangle.translate(event.getDelta().getWidth(),event.getDelta().getHeight());
        //printingRectangle.moveTo((float)event.getPosition().getX(),(float)event.getPosition().getY());
        }
    }

    @Override
    public void mouseWheelRotated(PInputEvent event) {
        super.mouseWheelRotatedByBlock(event);
        if (scaleDenominator == 0) {
            log.debug((event.getWheelRotation()));
            if (event.getWheelRotation() < 0) {
                zoom(0.9d);
                adjustMap();
            } else {
                zoom(1.1d);
                adjustMap();
            }
        }
    }

    private void zoom(double scale) {
        PBounds b = getPrintingRectangle().getBounds();
        double oldW = b.getWidth();
        double oldH = b.getHeight();
        double w = b.getWidth();
        double h = b.getHeight();
        if (bestimmerDimension.equals(WIDTH)) {
            w = w * scale;
            h = w / widthToHeightRatio;
        } else {
            h = h * scale;
            w = h * widthToHeightRatio;
        }
        double diffW = oldW - w;
        double diffH = oldH - h;

        b.setOrigin(b.getOrigin().getX() + diffW / 2, b.getOrigin().getY() + diffH / 2);
        b.setSize(w, h);
        getPrintingRectangle().setPathTo(b);
    }

    @Override
    public void mouseClicked(PInputEvent event) {
        super.mouseClicked(event);
//        log.debug(event.getPickedNode());
//        log.debug(event.getPickedNode()==getPrintingRectangle());
//        log.debug(event.getClickCount());
        if (event.getPickedNode() == getPrintingRectangle() && event.getClickCount() == 2) {
//             log.debug("mappingComponent.showPrintingDialog(getOldInteractionMode());");
            mappingComponent.showPrintingDialog(getOldInteractionMode());
        }
    }

    public String getOldInteractionMode() {
        return oldInteractionMode;
    }

    public void setOldInteractionMode(String oldInteractionMode) {
        this.oldInteractionMode = oldInteractionMode;
    }

    public BoundingBox getPrintingBoundingBox() {
        WorldToScreenTransform wtst = mappingComponent.getWtst();
        double x1 = wtst.getWorldX(getPrintingRectangle().getBounds().getMinX());
        double y1 = wtst.getWorldY(getPrintingRectangle().getBounds().getMinY());
        double x2 = wtst.getWorldX(getPrintingRectangle().getBounds().getMaxX());
        double y2 = wtst.getWorldY(getPrintingRectangle().getBounds().getMaxY());
        return new BoundingBox(x1, y1, x2, y2);
    }

    public PPath getPrintingRectangle() {
        return printingRectangle;
    }

    public void adjustMap() {
        int delayTime = 800;
        zoomTime = System.currentTimeMillis() + delayTime;
        if (zoomThread == null || !zoomThread.isAlive()) {
            zoomThread = new Thread() {
                public void run() {
                    while (System.currentTimeMillis() < zoomTime) {
                        try {
                            sleep(100);
                        //log.debug("WAIT");
                        } catch (InterruptedException iex) {
                        }
                    }
                    //log.debug("ZOOOOOOOOOOOOOOOOOOOOOOOOOOOM");
                    PBounds b = getPrintingRectangle().getBounds();
                    PBounds mover = new PBounds(b.getX() - b.getWidth() * (0.25 / 2.0), b.getY() - b.getHeight() * (0.25 / 2.0), b.getWidth() * 1.25, b.getHeight() * 1.25);
                    mappingComponent.getCamera().animateViewToCenterBounds(mover, true, mappingComponent.getAnimationDuration());
                    //TODO Hier muss noch die momentane BoundingBox in die History gesetzt werden
                    mappingComponent.queryServices();
                }
            };
            zoomThread.setPriority(Thread.NORM_PRIORITY);
            CismetThreadPool.execute(zoomThread);
        }
    }
    Thread zoomThread;
    long zoomTime;
}

// funzt nicht
//class BlurredPPath extends PPath{
//    protected void paint(PPaintContext paintContext) {
//        BufferedImage buf=new BufferedImage((int)getWidth(),(int)getHeight(),BufferedImage.TYPE_INT_RGB);
//        super.paint(new PPaintContext((Graphics2D)buf.getGraphics()));
//        paintContext.getGraphics().drawImage(buf,null,0,0);
//    }
//
//}
