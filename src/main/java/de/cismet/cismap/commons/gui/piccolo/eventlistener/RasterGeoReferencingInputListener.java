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

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PPanEventHandler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingDialog;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingHandler;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingWizard;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingWizardListener;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */

public class RasterGeoReferencingInputListener extends PPanEventHandler implements RasterGeoReferencingWizardListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(RasterGeoReferencingInputListener.class);

    public static final String NAME = "RasterGeoRefInputListener";

    //~ Instance fields --------------------------------------------------------

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private boolean ignoreTransformationChanged = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RasterGeoReferencingInputListener object.
     */
    private RasterGeoReferencingInputListener() {
        getWizard().addListener(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseMoved(final PInputEvent pie) {
        super.mouseMoved(pie);

        if (RasterGeoReferencingDialog.getInstance().isVisible() && (getHandler() != null)) {
            final Point2D mouseScreenPoint = pie.getPosition();
            final WorldToScreenTransform wtst = getMainMap().getWtst();
            final Coordinate mouseCoordinate = new Coordinate(wtst.getWorldX(mouseScreenPoint.getX()),
                    wtst.getWorldY(mouseScreenPoint.getY()));

            final int position = getWizard().getPosition();
            final Coordinate pointCoordinate = getHandler().getPointCoordinate(position);

            if (!getWizard().isCoordinateSelected() || (pointCoordinate != null)) {
                final Coordinate coordinate = (!getWizard().isCoordinateSelected()) ? mouseCoordinate : pointCoordinate;
                getWizard().setPointZoom(coordinate);
            }

            final Coordinate coordinate = getWizard().isCoordinateSelected() ? mouseCoordinate
                                                                             : getWizard().getSelectedCoordinate();
            if (getWizard().isCoordinateSelected() || (coordinate != null)) {
                getWizard().setCoordinateZoom(coordinate);
            }
        }
    }

    @Override
    public void mouseDragged(final PInputEvent pie) {
        super.mouseDragged(pie);
        getPanAndMousewheelZoomListener().mouseDragged(pie);
    }

    @Override
    protected void dragActivityFirstStep(final PInputEvent pie) {
        super.dragActivityFirstStep(pie);
        getPanAndMousewheelZoomListener().dragActivityFirstStep(pie);
    }

    @Override
    protected void dragActivityFinalStep(final PInputEvent pie) {
        super.dragActivityFinalStep(pie);
        getPanAndMousewheelZoomListener().dragActivityFinalStep(pie);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private PanAndMousewheelZoomListener getPanAndMousewheelZoomListener() {
        return (PanAndMousewheelZoomListener)getMainMap().getInputListener(MappingComponent.PAN);
    }

    @Override
    public void mouseWheelRotated(final PInputEvent pie) {
        getPanAndMousewheelZoomListener().mouseWheelRotated(pie);
    }

    @Override
    public void mouseClicked(final PInputEvent pie) {
        super.mouseClicked(pie);

        if (RasterGeoReferencingDialog.getInstance().isVisible() && pie.isLeftMouseButton()) {
            if (pie.getClickCount() < 2) {
                final Point2D mapPoint = pie.getPosition();
                final WorldToScreenTransform wtst = getMainMap().getWtst();
                final Coordinate coordinate = new Coordinate(wtst.getWorldX(mapPoint.getX()),
                        wtst.getWorldY(mapPoint.getY()));

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
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static MappingComponent getMainMap() {
        return CismapBroker.getInstance().getMappingComponent();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static RasterGeoReferencingWizard getWizard() {
        return RasterGeoReferencingWizard.getInstance();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static RasterGeoReferencingHandler getHandler() {
        return getWizard().getHandler();
    }

    @Override
    public void pointSelected(final int position) {
        getWizard().updateZoom(position);
    }

    @Override
    public void coordinateSelected(final int position) {
        getWizard().updateZoom(position);
    }

    @Override
    public void handlerChanged(final RasterGeoReferencingHandler handler) {
        SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    getWizard().refreshPointZoomMap();
                }
            });
    }

    @Override
    public void positionAdded(final int position) {
    }

    @Override
    public void positionRemoved(final int position) {
    }

    @Override
    public void positionChanged(final int position) {
    }

    @Override
    public void transformationChanged() {
        SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    getWizard().refreshPointZoomMap();
                    getWizard().updateZoom(getWizard().getPosition());
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static RasterGeoReferencingInputListener getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final RasterGeoReferencingInputListener INSTANCE = new RasterGeoReferencingInputListener();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
