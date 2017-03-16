/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import java.awt.Point;
import java.awt.geom.Point2D;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.ImageRasterService;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingHandler;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingWizard;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */

public class RasterGeoReferencingInputListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(RasterGeoReferencingInputListener.class);

    public static final String NAME = "RasterGeoRefInputListener";

    //~ Instance fields --------------------------------------------------------

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private float oldTransparency;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final PInputEvent pie) {
        super.mouseClicked(pie);

        if (pie.isLeftMouseButton()) {
            if (pie.getClickCount() < 2) {
                final Point2D mapPoint = pie.getPosition();
                final WorldToScreenTransform wtst = CismapBroker.getInstance().getMappingComponent().getWtst();
                final Coordinate coordinate = new Coordinate(wtst.getWorldX(mapPoint.getX()),
                        wtst.getWorldY(mapPoint.getY()));

                if (!getWizard().isPointSelected() && !getWizard().isCoordinateSelected()) {
                    getHandler().addPair();
                    getWizard().selectPoint(getWizard().getHandler().getNumOfPairs() - 1);
                }

                final int position = getWizard().getPosition();

                try {
                    final Coordinate imageCoordinate = getHandler().getMetaData()
                                .getTransform()
                                .getInverse()
                                .transform(coordinate, new Coordinate());
                    final Point point = new Point((int)imageCoordinate.x, (int)imageCoordinate.y);

                    if (getWizard().isPointSelected()) {
                        getHandler().setPoint(position, point);
                    } else if (getWizard().isCoordinateSelected()) {
                        getHandler().setCoordinate(position, coordinate);
                    } else {
                        return;
                    }
                } catch (final Exception ex) {
                }

                if (getWizard().isCoordinateSelectionMode()) {
                    getHandler().setPositionEnabled(position, true);
                }
                getWizard().forward();
            }
        } else if (pie.isRightMouseButton()) {
            getWizard().backward();
        }

        final ImageRasterService service = getHandler().getService();
        final float transparency;
        if (getWizard().isCoordinateSelected()) {
            setOldTransparency(service.getTranslucency());
            transparency = 0f;
        } else {
            transparency = getOldTransparency();
        }

        service.setTranslucency(transparency);
        final PNode pi = service.getPNode();
        if (pi != null) {
            pi.setTransparency(transparency);
            pi.repaint();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private RasterGeoReferencingWizard getWizard() {
        return RasterGeoReferencingWizard.getInstance();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private RasterGeoReferencingHandler getHandler() {
        return getWizard().getHandler();
    }
}
