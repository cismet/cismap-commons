/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.util.PLocator;

import java.awt.geom.Point2D;

import java.util.Collection;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class PivotPHandle extends PHandle {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private PFeature pfeature;
    private Point2D mid;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PivotPHandle object.
     *
     * @param  pfeature  DOCUMENT ME!
     * @param  mid       DOCUMENT ME!
     */
    public PivotPHandle(final PFeature pfeature, final Point2D mid) {
        super(new PLocator() {

                @Override
                public double locateX() {
                    if (mid == null) {
                        return pfeature.getBounds().getCenter2D().getX();
                    } else {
                        return mid.getX();
                    }
                }

                @Override
                public double locateY() {
                    if (mid == null) {
                        return pfeature.getBounds().getCenter2D().getY();
                    } else {
                        return mid.getY();
                    }
                }
            }, pfeature.getViewer());

        this.pfeature = pfeature;
        this.mid = mid;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void dragHandle(final PDimension aLocalDimension, final PInputEvent aEvent) {
        final double scale = pfeature.getViewer().getCamera().getViewScale();
        final SimpleMoveListener moveListener = (SimpleMoveListener)pfeature.getViewer()
                    .getInputListener(MappingComponent.MOTION);
        if (moveListener != null) {
            moveListener.mouseMoved(aEvent);
        } else {
            log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");
        }
        mid.setLocation(mid.getX() + (aLocalDimension.width / scale), mid.getY() + (aLocalDimension.height / scale));
        relocateHandle();
    }

    @Override
    public void endHandleDrag(final java.awt.geom.Point2D aLocalPoint, final PInputEvent aEvent) {
        if (pfeature.getViewer().getFeatureCollection() instanceof DefaultFeatureCollection) {
            final Collection selArr = pfeature.getViewer().getFeatureCollection().getSelectedFeatures();
            for (final Object o : selArr) {
                final PFeature pf = (PFeature)(pfeature.getViewer().getPFeatureHM().get(o));
                pf.setPivotPoint(mid);
            }
        }
        if (pfeature.getViewer().isFeatureDebugging()) {
            if (log.isDebugEnabled()) {
                log.debug("neuer PivotPunkt=(" + mid.getX() + ", " + mid.getY() + ")");
            }
        }
        super.endHandleDrag(aLocalPoint, aEvent);
    }

    @Override
    public void mouseMovedNotInDragOperation(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        final SimpleMoveListener moveListener = (SimpleMoveListener)pfeature.getViewer()
                    .getInputListener(MappingComponent.MOTION);
        if (moveListener != null) {
            moveListener.mouseMoved(pInputEvent);
        } else {
            log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");
        }
    }
}
