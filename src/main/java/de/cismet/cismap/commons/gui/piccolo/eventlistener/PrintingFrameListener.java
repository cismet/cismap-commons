/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

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

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.printing.PrintingToolTip;

import de.cismet.tools.CismetThreadPool;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class PrintingFrameListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    public static final double DEFAULT_JAVA_RESOLUTION_IN_DPI = 72d;
    public static final double MILLIMETER_OF_AN_INCH = 25.4d;
    public static final double INCH_OF_A_MILLIMETER = 0.039d;
    public static final double MILLIMETER_OF_A_METER = 1000d;
    public static final String WIDTH = "WIDTH";   // NOI18N
    public static final String HEIGHT = "HEIGHT"; // NOI18N
    public static final Color BORDER_COLOR = new Color(255, 255, 255, 200);

    //~ Instance fields --------------------------------------------------------

    protected PPath north = new PPath();
    protected PPath south = new PPath();
    protected PPath east = new PPath();
    protected PPath west = new PPath();
    MappingComponent mappingComponent = null;
    Point2D startDragPosition;
    double widthToHeightRatio = 1.0d;
    int scaleDenominator = 0;
    int placeholderWidth = 0;
    boolean inDragOperation = false;
    int placeholderHeight = 0;
    double realWorldWidth = 0;
    double realWorldHeight = 0;
    PLayer layer;
    Thread zoomThread;
    long zoomTime;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private String bestimmerDimension = WIDTH;
    private String oldInteractionMode = ""; // NOI18N
    private PPath printingRectangle = new PPath() {

            @Override
            protected void paint(final PPaintContext paintContext) {
                super.paint(paintContext);
                final PBounds vb = mappingComponent.getCamera().getViewBounds();
                final PBounds rb = getPrintingRectangle().getBounds();
                final GeneralPath newNorthBounds = new GeneralPath(new Rectangle2D.Double(
                            vb.getMinX(),
                            vb.getMinY(),
                            vb.getWidth(),
                            rb.getMinY()
                                    - vb.getMinY()));
                final GeneralPath newSouthBounds = new GeneralPath(new Rectangle2D.Double(
                            vb.getMinX(),
                            rb.getMaxY(),
                            vb.getWidth(),
                            vb.getMaxY()
                                    - rb.getMaxY()));
                final GeneralPath newEastBounds = new GeneralPath(new Rectangle2D.Double(
                            vb.getMinX(),
                            rb.getMinY(),
                            rb.getMinX()
                                    - vb.getMinX(),
                            rb.getHeight()));
                final GeneralPath newWestBounds = new GeneralPath(new Rectangle2D.Double(
                            rb.getMaxX(),
                            rb.getMinY(),
                            vb.getMaxX()
                                    - rb.getMaxX(),
                            rb.getHeight()));
                // to prevent painting loops check before setPathTo
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

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of PrintingFrameListener.
     *
     * @param  mappingComponent  DOCUMENT ME!
     */
    public PrintingFrameListener(final MappingComponent mappingComponent) {
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

        // printingRectangle.setStroke(new FixedWidthStroke());
        getPrintingRectangle().setStroke(null);
        getPrintingRectangle().setPaint(Color.yellow);
        getPrintingRectangle().setPaint(new Color(20, 20, 20, 1));
        layer = mappingComponent.getPrintingFrameLayer();
        // layer=mappingComponent.getTmpFeatureLayer();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  scaleDenominator    DOCUMENT ME!
     * @param  placeholderWidth    DOCUMENT ME!
     * @param  placeholderHeight   DOCUMENT ME!
     * @param  oldInteractionMode  DOCUMENT ME!
     */
    public void init(final int scaleDenominator,
            final int placeholderWidth,
            final int placeholderHeight,
            final String oldInteractionMode) {
//        log.fatal(scaleDenominator+ " "+placeholderWidth+" "+placeholderHeight);
        this.oldInteractionMode = oldInteractionMode;
        this.widthToHeightRatio = (double)placeholderWidth / (double)placeholderHeight;
        this.scaleDenominator = scaleDenominator;
        this.placeholderHeight = placeholderHeight;
        this.placeholderWidth = placeholderWidth;
        final double mapWidth = mappingComponent.getCamera().getViewBounds().getWidth();
        final double mapHeight = mappingComponent.getCamera().getViewBounds().getHeight();

        // calculate realworldsize
        if (this.scaleDenominator == -1) {
            final String s = JOptionPane.showInputDialog(
                    mappingComponent,
                    org.openide.util.NbBundle.getMessage(
                        PrintingFrameListener.class,
                        "PrintingFrameListener.init(double,int,int,String).message"),
                    ""); // NOI18N
            try {
                this.scaleDenominator = Integer.parseInt(s);
            } catch (Exception skip) {
                log.warn(
                    "Could not determine the given scale denominator. It will be set to '0.0' to enable free scaling.",
                    skip);
                this.scaleDenominator = 0;
            }
        }

        if (this.scaleDenominator == 0) {
            // no fixed scale
            if ((widthToHeightRatio / (mapWidth / mapHeight)) < 1) {
                // height is the critical value and must be shrinked. in german: bestimmer ;-)
                realWorldHeight = mapHeight * 0.75;
                realWorldWidth = realWorldHeight * widthToHeightRatio;
                bestimmerDimension = PrintingFrameListener.HEIGHT;
            } else {
                // width is the critical value and must be shrinked. in german: bestimmer ;-)
                realWorldWidth = mapWidth * 0.75;
                realWorldHeight = (double)realWorldWidth / (double)widthToHeightRatio;
                bestimmerDimension = PrintingFrameListener.WIDTH;
            }
        } else {
            realWorldWidth = placeholderWidth / DEFAULT_JAVA_RESOLUTION_IN_DPI * MILLIMETER_OF_AN_INCH
                        / MILLIMETER_OF_A_METER * this.scaleDenominator;
            realWorldHeight = placeholderHeight / DEFAULT_JAVA_RESOLUTION_IN_DPI * MILLIMETER_OF_AN_INCH
                        / MILLIMETER_OF_A_METER * this.scaleDenominator;
        }
        final Point2D center = mappingComponent.getCamera().getViewBounds().getCenter2D();
        final PBounds pb = new PBounds(0, 0, realWorldWidth, realWorldHeight);
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

        if ((this.scaleDenominator != 0)) {
            final PBounds b = getPrintingRectangle().getBounds();
            final PBounds mover = new PBounds(b.getX() - (b.getWidth() * (0.25 / 2.0)),
                    b.getY()
                            - (b.getHeight() * (0.25 / 2.0)),
                    b.getWidth()
                            * 1.25,
                    b.getHeight()
                            * 1.25);
            mappingComponent.getCamera()
                    .animateViewToCenterBounds(mover, true, mappingComponent.getAnimationDuration());
            mappingComponent.queryServices();
        }
        mappingComponent.setPointerAnnotation(new PrintingToolTip(new Color(255, 255, 222, 200)));
        mappingComponent.setPointerAnnotationVisibility(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
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
    public void mouseReleased(final PInputEvent event) {
        super.mouseReleased(event);
        if (inDragOperation && (startDragPosition.distance(event.getCanvasPosition()) > 2)) {
            mappingComponent.getCamera().animateViewToCenterBounds(getPrintingRectangle().getBounds(), false, 500);
            mappingComponent.queryServices();
        }
        inDragOperation = false;
    }

    @Override
    public void mousePressed(final PInputEvent event) {
        super.mousePressed(event);
        if (log.isDebugEnabled()) {
            log.debug("mousePressed:" + event); // NOI18N
        }
        if ((event.getPickedNode() == getPrintingRectangle()) && (event.getClickCount() < 2)) {
            inDragOperation = true;
            startDragPosition = event.getCanvasPosition();
        }
    }

    @Override
    public void mouseMoved(final PInputEvent event) {
        super.mouseMoved(event);
        if (event.getPickedNode() == getPrintingRectangle()) {
            mappingComponent.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            mappingComponent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    @Override
    public void mouseDragged(final PInputEvent event) {
        super.mouseDragged(event);
        if (inDragOperation) {
            final PBounds b = getPrintingRectangle().getBounds();
            b.setOrigin(b.getOrigin().getX() + event.getDelta().getWidth(),
                b.getOrigin().getY()
                        + event.getDelta().getHeight());
            getPrintingRectangle().setPathTo(b);
            // printingRectangle.translate(event.getDelta().getWidth(),event.getDelta().getHeight());
            // printingRectangle.moveTo((float)event.getPosition().getX(),(float)event.getPosition().getY());
        }
    }

    @Override
    public void mouseWheelRotated(final PInputEvent event) {
        super.mouseWheelRotatedByBlock(event);
        if (scaleDenominator == 0) {
            if (log.isDebugEnabled()) {
                log.debug((event.getWheelRotation()));
            }
            if (event.getWheelRotation() < 0) {
                zoom(0.9d);
                adjustMap();
            } else {
                zoom(1.1d);
                adjustMap();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  scale  DOCUMENT ME!
     */
    private void zoom(final double scale) {
        final PBounds b = getPrintingRectangle().getBounds();
        final double oldW = b.getWidth();
        final double oldH = b.getHeight();
        double w = b.getWidth();
        double h = b.getHeight();
        if (bestimmerDimension.equals(WIDTH)) {
            w = w * scale;
            h = w / widthToHeightRatio;
        } else {
            h = h * scale;
            w = h * widthToHeightRatio;
        }
        final double diffW = oldW - w;
        final double diffH = oldH - h;

        b.setOrigin(b.getOrigin().getX() + (diffW / 2), b.getOrigin().getY() + (diffH / 2));
        b.setSize(w, h);
        getPrintingRectangle().setPathTo(b);
    }

    @Override
    public void mouseClicked(final PInputEvent event) {
        super.mouseClicked(event);
//        log.debug(event.getPickedNode());
//        log.debug(event.getPickedNode()==getPrintingRectangle());
//        log.debug(event.getClickCount());
        if ((event.getPickedNode() == getPrintingRectangle()) && (event.getClickCount() == 2)) {
//             log.debug("mappingComponent.showPrintingDialog(getOldInteractionMode());");
            mappingComponent.showPrintingDialog(getOldInteractionMode());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getOldInteractionMode() {
        return oldInteractionMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  oldInteractionMode  DOCUMENT ME!
     */
    public void setOldInteractionMode(final String oldInteractionMode) {
        this.oldInteractionMode = oldInteractionMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public BoundingBox getPrintingBoundingBox() {
        final WorldToScreenTransform wtst = mappingComponent.getWtst();
        final double x1 = wtst.getWorldX(getPrintingRectangle().getBounds().getMinX());
        final double y1 = wtst.getWorldY(getPrintingRectangle().getBounds().getMinY());
        final double x2 = wtst.getWorldX(getPrintingRectangle().getBounds().getMaxX());
        final double y2 = wtst.getWorldY(getPrintingRectangle().getBounds().getMaxY());
        return new BoundingBox(x1, y1, x2, y2);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PPath getPrintingRectangle() {
        return printingRectangle;
    }

    /**
     * DOCUMENT ME!
     */
    public void adjustMap() {
        final int delayTime = 800;
        zoomTime = System.currentTimeMillis() + delayTime;
        if ((zoomThread == null) || !zoomThread.isAlive()) {
            zoomThread = new Thread() {

                    @Override
                    public void run() {
                        while (System.currentTimeMillis() < zoomTime) {
                            try {
                                sleep(100);
                                // log.debug("WAIT");
                            } catch (InterruptedException iex) {
                            }
                        }
                        // log.debug("ZOOOOOOOOOOOOOOOOOOOOOOOOOOOM");
                        final PBounds b = getPrintingRectangle().getBounds();
                        final PBounds mover = new PBounds(b.getX() - (b.getWidth() * (0.25 / 2.0)),
                                b.getY()
                                        - (b.getHeight() * (0.25 / 2.0)),
                                b.getWidth()
                                        * 1.25,
                                b.getHeight()
                                        * 1.25);
                        mappingComponent.getCamera()
                                .animateViewToCenterBounds(mover, true, mappingComponent.getAnimationDuration());
                        // TODO Hier muss noch die momentane BoundingBox in die History gesetzt werden
                        mappingComponent.queryServices();
                    }
                };
            zoomThread.setPriority(Thread.NORM_PRIORITY);
            CismetThreadPool.execute(zoomThread);
        }
    }
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
