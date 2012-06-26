/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo;

import com.vividsolutions.jts.geom.Coordinate;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.util.PLocator;

import pswing.PSwing;
import pswing.PSwingCanvas;

import java.awt.geom.Point2D;

import java.text.Format;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeatureListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class LinearReferencedPointPHandle extends PHandle {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            LinearReferencedPointPHandle.class);

    //~ Instance fields --------------------------------------------------------

    private PFeature pfeature;
    private LinearReferencedPointInfoPanel infoPanel;
    private PSwing pswingComp;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LinearReferencedPointPHandle object.
     *
     * @param  pfeature  DOCUMENT ME!
     */
    public LinearReferencedPointPHandle(final PFeature pfeature) {
        super(new PLocator() {

                @Override
                public double locateX() {
                    try {
                        return pfeature.getXp(0, 0)[0];
                    } catch (Exception ex) {
                        return -1;
                    }
                }

                @Override
                public double locateY() {
                    try {
                        return pfeature.getYp(0, 0)[0];
                    } catch (Exception ex) {
                        return -1;
                    }
                }
            }, pfeature.getViewer());

        this.pfeature = pfeature;

        initPanel();

        ((LinearReferencedPointFeature)pfeature.getFeature()).addListener(new LinearReferencedPointFeatureListener() {

                @Override
                public void featureMoved(final LinearReferencedPointFeature pointFeature) {
                    relocateHandle();
                }

                @Override
                public void featureMerged(final LinearReferencedPointFeature withPoint,
                        final LinearReferencedPointFeature mergePoint) {
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getPFeature() {
        return pfeature;
    }

    /**
     * DOCUMENT ME!
     */
    private void initPanel() {
        infoPanel = new LinearReferencedPointInfoPanel();

        pswingComp = new PSwing((PSwingCanvas)pfeature.getViewer(), infoPanel);
        infoPanel.setPNodeParent(pswingComp);
        addChild(pswingComp);
    }

    @Override
    public void dragHandle(final PDimension aLocalDimension, final PInputEvent pInputEvent) {
        try {
            final SimpleMoveListener moveListener = (SimpleMoveListener)pfeature.getViewer()
                        .getInputListener(MappingComponent.MOTION);
            if (moveListener != null) {
                moveListener.mouseMoved(pInputEvent);
            } else {
                LOG.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");
            }

            if (pfeature.getViewer().getHandleInteractionMode().equals(MappingComponent.MOVE_HANDLE)) {
                pfeature.getViewer().getCamera().localToView(aLocalDimension);

                final WorldToScreenTransform wtst = pfeature.getViewer().getWtst();

                final LinearReferencedPointFeature linref = (LinearReferencedPointFeature)pfeature.getFeature();

                final Point2D dragPoint = pInputEvent.getPosition();
                final Coordinate coord = new Coordinate(
                        wtst.getSourceX(dragPoint.getX()),
                        wtst.getSourceY(dragPoint.getY()));

                linref.moveTo(coord);
                relocateHandle();
            }
        } catch (Throwable t) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error in dragHandle.", t);
            }
        }
    }

    @Override
    public void endHandleDrag(final Point2D aLocalPoint, final PInputEvent aEvent) {
        super.endHandleDrag(aLocalPoint, aEvent);
        final LinearReferencedPointFeature linref = (LinearReferencedPointFeature)pfeature.getFeature();
        linref.moveFinished();
    }

    @Override
    public void relocateHandle() {
        super.relocateHandle();

        if (pfeature != null) {
            final LinearReferencedPointFeature linref = (LinearReferencedPointFeature)pfeature.getFeature();

            String info = "";

            final Format infoFormat = ((LinearReferencedPointFeature)pfeature.getFeature()).getInfoFormat();
            if (infoFormat != null) {
                info = infoFormat.format(linref.getCurrentPosition());
            } else {
                info = String.valueOf(linref.getCurrentPosition());
            }
            infoPanel.setLengthInfo(info);

            final PBounds b = getBoundsReference();
            final Point2D aPoint = getLocator().locatePoint(null);
            pfeature.getViewer().getCamera().viewToLocal(aPoint);

            final double newCenterX = aPoint.getX();
            final double newCenterY = aPoint.getY();

            pswingComp.setOffset(newCenterX + DEFAULT_HANDLE_SIZE, newCenterY - (pswingComp.getHeight() / 2));

            if ((newCenterX != b.getCenterX()) || (newCenterY != b.getCenterY())) {
                this.setBounds(0, 0, DEFAULT_HANDLE_SIZE, DEFAULT_HANDLE_SIZE);
                centerBoundsOnPoint(newCenterX, newCenterY);
            }
        }
    }
}
