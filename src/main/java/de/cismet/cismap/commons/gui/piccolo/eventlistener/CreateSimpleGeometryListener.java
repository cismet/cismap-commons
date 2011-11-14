/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.event.PNotificationCenter;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.util.Vector;

import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureDeleteAction;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class CreateSimpleGeometryListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    public static final String GEOMETRY_CREATED_NOTIFICATION = "GEOMETRY_CREATED_NOTIFICATION"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    protected Point2D startPoint;
    protected PPath tempFeature;
    protected MappingComponent mc;
    protected boolean inProgress;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Vector<Point2D> points;
    private PureNewFeature newFeature = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of CreateGeometryListener.
     *
     * @param  mc  DOCUMENT ME!
     */
    public CreateSimpleGeometryListener(final MappingComponent mc) {
        this.mc = mc;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseMoved(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        super.mouseMoved(pInputEvent);
    }

    @Override
    public void mousePressed(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        super.mouseClicked(pInputEvent);

        // draggen beginnen
        if (!inProgress) {
            initTempFeature(true);
            startPoint = pInputEvent.getPosition();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geomType  DOCUMENT ME!
     */
    private void createPureNewFeature(final PureNewFeature.geomTypes geomType) {
        try {
            final Point2D[] p = points.toArray(new Point2D[0]);
            final PureNewFeature pnf = new PureNewFeature(p, mc.getWtst());
            pnf.setGeometryType(geomType);
            finishGeometry(pnf);
        } catch (Throwable t) {
            log.error("Error during the creation of the geometry", t); // NOI18N
        }
        inProgress = false;
    }

    @Override
    public void mouseReleased(final PInputEvent pInputEvent) {
        super.mouseReleased(pInputEvent);

        if (pInputEvent.isLeftMouseButton()) {
            if (inProgress) {
                // rechteck erzeugen
                createPureNewFeature(PureNewFeature.geomTypes.RECTANGLE);
            } else {
                if (pInputEvent.getClickCount() == 1) {
                    // punkt erzeugen
                    final Point2D point = pInputEvent.getPosition();
                    points = new Vector<Point2D>();
                    points.add(point);
                    createPureNewFeature(PureNewFeature.geomTypes.POINT);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  m  DOCUMENT ME!
     * @param  f  DOCUMENT ME!
     */
    private void createAction(final MappingComponent m, final PureNewFeature f) {
        mc.getMemUndo().addAction(new FeatureDeleteAction(m, f));
        mc.getMemRedo().clear();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Color getFillingColor() {
        return new Color(1f, 1f, 1f, 0.4f);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lastPoint  DOCUMENT ME!
     */
    protected void updatePolygon(final Point2D lastPoint) {
        final Point2D[] p = points.toArray(new Point2D[0]);
        tempFeature.setPathToPolyline(p);
        tempFeature.repaint();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newFeature  DOCUMENT ME!
     */
    private void postGeometryCreatedNotificaton(final PureNewFeature newFeature) {
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(GEOMETRY_CREATED_NOTIFICATION, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newFeature  DOCUMENT ME!
     */
    protected void finishGeometry(final PureNewFeature newFeature) {
        mc.getTmpFeatureLayer().removeAllChildren();
        this.newFeature = newFeature;
        postGeometryCreatedNotificaton(newFeature);
        createAction(mc, newFeature);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  filled  DOCUMENT ME!
     */
    private void initTempFeature(final boolean filled) {
        tempFeature = new PPath();
        tempFeature.setStroke(new FixedWidthStroke());
        if (filled) {
            final Color fillingColor = getFillingColor();
            tempFeature.setStrokePaint(fillingColor.darker());
            tempFeature.setPaint(fillingColor);
        }
        mc.getTmpFeatureLayer().addChild(tempFeature);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PureNewFeature getNewFeature() {
        return this.newFeature;
    }

    @Override
    public void mouseDragged(final PInputEvent pInputEvent) {
        super.mouseDragged(pInputEvent);

        if (startPoint == null) {
            startPoint = pInputEvent.getPosition();
        }

        inProgress = true;

        // 4 Punkte, und der erste Punkt nochmal als letzter Punkt
        points = new Vector<Point2D>(5);
        points.add(startPoint);
        points.add(new Point2D.Double(startPoint.getX(), pInputEvent.getPosition().getY()));
        points.add(pInputEvent.getPosition());
        points.add(new Point2D.Double(pInputEvent.getPosition().getX(), startPoint.getY()));
        points.add(startPoint);

        updatePolygon(null);
    }
}
