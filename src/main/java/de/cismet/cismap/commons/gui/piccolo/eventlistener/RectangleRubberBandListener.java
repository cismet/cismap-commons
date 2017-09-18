/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * RectangleRubberBandListener.java
 *
 * Created on 5. M\u00E4rz 2005, 14:47
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.PBounds;

import java.awt.*;
import java.awt.geom.Point2D;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   HP
 * @version  $Revision$, $Date$
 */
public class RectangleRubberBandListener extends PBasicInputEventHandler {

    //~ Instance fields --------------------------------------------------------

    /** The rectangle that is currently getting created. */
    protected PPath rectangle;

    /** The mouse press location for the current pressed, drag and release sequence. */
    protected Point2D pressPoint;

    /** The current drag location. */
    protected Point2D dragPoint;
    protected boolean rectangleEmpty = true;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of RectangleRubberBandListener.
     */
    public RectangleRubberBandListener() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mousePressed(final PInputEvent e) {
        super.mousePressed(e);
        try {
            if (e.isLeftMouseButton() && !isMouseOverStickyNode(e)) {
                final PLayer layer = ((MappingComponent)(e.getComponent())).getRubberBandLayer();
                // Initialize the locations.
                pressPoint = e.getPosition();
                dragPoint = pressPoint;

                // create a new rectangle and add it to the canvas layer so
                // that we can see it.
                rectangle = new PPath();
                rectangle.setPaint(new java.awt.Color(20, 20, 20, 20));
                rectangle.setStroke(new BasicStroke((float)(1 / e.getCamera().getViewScale())));
                layer.addChild(rectangle);
                rectangle.moveToFront();
                updateRectangle();
                rectangleEmpty = true;
            }
        } catch (ClassCastException cce) {
            log.error("PCanvas muss vom Typ SimpleFeatureViewer sein", cce); // NOI18N
        }
    }

    @Override
    public void mouseDragged(final PInputEvent e) {
        super.mouseDragged(e);
        dragPoint = e.getPosition();
        updateRectangle();
        rectangleEmpty = false;
    }

    @Override
    public void mouseReleased(final PInputEvent e) {
        super.mouseReleased(e);
        final PBounds b = new PBounds();
        if (pressPoint != null) {
            b.add(pressPoint);
            b.add(dragPoint);
        }
        if (e.getButton() == 1) { // Linke Maustaste: TODO: konnte die piccolo Konstanten nicht inden
            // update the rectangle shape.
            updateRectangle();
            if ((rectangle != null) && (rectangle.getParent() != null)) {
                rectangle.removeFromParent();
            }
            // rectangle = null;
        }
    }

    /**
     * Updates the rectangle shape.
     */
    private void updateRectangle() {
        // create a new bounds that contains both the press and
        // current drag point.
        if (rectangle != null) {
            PBounds b = new PBounds();
            b.add(pressPoint);
            b.add(dragPoint);

            // Set the rectangles bounds.
            // rectangle.setPathToRectangle((float)pressPoint.getX(),(float)pressPoint.getY(),(float)(pressPoint.getX()-dragPoint.getX()),(float)(pressPoint.getY()-dragPoint.getY()));
            rectangle.setPathTo(b);
            b = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pInputEvent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isMouseOverStickyNode(final PInputEvent pInputEvent) {
        final Object firstPFeatureUnderMouse = PFeatureTools.getFirstValidObjectUnderPointer(
                pInputEvent,
                new Class[] { PFeature.class },
                true);

        if ((firstPFeatureUnderMouse != null) && (firstPFeatureUnderMouse instanceof PFeature)) {
            final PFeature pFeature = (PFeature)firstPFeatureUnderMouse;
            final PNode stickyChild = pFeature.getStickyChild();
            if ((stickyChild != null) && stickyChild.getVisible()) {
                if (stickyChild.getFullBounds().contains(pInputEvent.getPosition())) {
                    return true;
                }
            }
        }
        return false;
    }
}
