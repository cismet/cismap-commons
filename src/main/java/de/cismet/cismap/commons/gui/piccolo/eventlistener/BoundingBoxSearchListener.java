/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

import java.awt.geom.Point2D;

import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.MapSearchEvent;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class BoundingBoxSearchListener extends RectangleRubberBandListener {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of BoundingBoxSearchListener.
     */
    public BoundingBoxSearchListener() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseReleased(final PInputEvent e) {
        super.mouseReleased(e);
        final MapSearchEvent mse = new MapSearchEvent();

        final PBounds pb = super.rectangle.getFullBounds();
        final Point2D[] boundingBoxPoints = new Point2D[2];
        boundingBoxPoints[0] = new Point2D.Double(pb.getMinX(), pb.getMinY());
        boundingBoxPoints[1] = new Point2D.Double(pb.getMaxX(), pb.getMaxY());

        final Point2D[] p = new Point2D[5];
        p[0] = (Point2D)boundingBoxPoints[0];
        p[2] = (Point2D)boundingBoxPoints[1];
        p[1] = new Point2D.Double(p[0].getX(), p[2].getY());
        p[3] = new Point2D.Double(p[2].getX(), p[0].getY());
        p[4] = p[0];

        final PureNewFeature pnf = new PureNewFeature(p, CismapBroker.getInstance().getMappingComponent().getWtst());
        mse.setGeometry(pnf.getGeometry());
        mse.setBounds(super.rectangle.getFullBounds());
        CismapBroker.getInstance().fireMapSearchInited(mse);
    }
}
