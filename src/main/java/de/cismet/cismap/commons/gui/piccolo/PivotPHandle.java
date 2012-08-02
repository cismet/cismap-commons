/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.util.PLocator;
import java.awt.geom.Point2D;
import java.util.Collection;

/**
 *
 * @author jruiz
 */
public class PivotPHandle extends PHandle {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private PFeature pfeature;
    private Point2D mid;

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
        }
        , pfeature.getViewer());
        
        this.pfeature = pfeature;
        this.mid = mid;
    }

    @Override
    public void dragHandle(PDimension aLocalDimension, PInputEvent aEvent) {
        double scale = pfeature.getViewer().getCamera().getViewScale();
        SimpleMoveListener moveListener = (SimpleMoveListener) pfeature.getViewer().getInputListener(MappingComponent.MOTION);
        if (moveListener != null) {
            moveListener.mouseMoved(aEvent);
        } else {
            log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");
        }
        mid.setLocation(mid.getX() + (aLocalDimension.width / scale), mid.getY() + (aLocalDimension.height / scale));
        relocateHandle();
    }

    @Override
    public void endHandleDrag(java.awt.geom.Point2D aLocalPoint, PInputEvent aEvent) {
        if (pfeature.getViewer().getFeatureCollection() instanceof DefaultFeatureCollection) {
            Collection selArr = pfeature.getViewer().getFeatureCollection().getSelectedFeatures();
            for (Object o : selArr) {
                PFeature pf = (PFeature) (pfeature.getViewer().getPFeatureHM().get(o));
                pf.setPivotPoint(mid);
            }
        }
        if (pfeature.getViewer().isFeatureDebugging()) {
            log.debug("neuer PivotPunkt=(" + mid.getX() + ", " + mid.getY() + ")");
        }
        super.endHandleDrag(aLocalPoint, aEvent);
    }

    @Override
    public void mouseMovedNotInDragOperation(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        SimpleMoveListener moveListener = (SimpleMoveListener) pfeature.getViewer().getInputListener(MappingComponent.MOTION);
        if (moveListener != null) {
            moveListener.mouseMoved(pInputEvent);
        } else {
            log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");
        }
    }
}
