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

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PPanEventHandler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.ImageRasterService;
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

    private static final int DEFAULT_VIEW_WIDTH = 50;
    private static final int DEFAULT_VIEW_HEIGHT = 50;

    //~ Instance fields --------------------------------------------------------

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private float oldTransparency;

    @Getter private final PCanvas pointZoomViewCanvas = new PCanvas();
    @Getter private final PCanvas coordinateZoomViewCanvas = new PCanvas();

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private boolean init = false;

    @Getter(AccessLevel.PRIVATE)
    private final Collection<PropertyChangeListener> propertyChangeListeners = new ArrayList<>();

    @Getter(AccessLevel.PRIVATE)
    private final PropertyChangeListenerHandler propertyChangeListenerHandler = new PropertyChangeListenerHandler();

    @Getter @Setter private int viewWidth = DEFAULT_VIEW_WIDTH;
    @Getter @Setter private int viewHeight = DEFAULT_VIEW_HEIGHT;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RasterGeoReferencingInputListener object.
     */
    public RasterGeoReferencingInputListener() {
        RasterGeoReferencingWizard.getInstance().addListener(this);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   propertyChangeListener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean addPropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
        return getPropertyChangeListeners().add(propertyChangeListener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   propertyChangeListener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean removePropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
        return getPropertyChangeListeners().remove(propertyChangeListener);
    }

    /**
     * DOCUMENT ME!
     */
    private void initIfNeeded() {
        if (!isInit()) {
            initZoomViewCanvas(getPointZoomViewCanvas(), getMappingComponent().getLayer());
            initZoomViewCanvas(getCoordinateZoomViewCanvas(), getMappingComponent().getLayer());
            setInit(true);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pCanvas  DOCUMENT ME!
     * @param  pLayer   DOCUMENT ME!
     */
    private void initZoomViewCanvas(final PCanvas pCanvas, final PLayer pLayer) {
        final PCamera camera = new PCamera();
        camera.addLayer(pLayer);
        pCanvas.setCamera(camera);
        getMappingComponent().getRoot().addChild(camera);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static MappingComponent getMappingComponent() {
        return CismapBroker.getInstance().getMappingComponent();
    }

    @Override
    public void mouseMoved(final PInputEvent pie) {
        super.mouseMoved(pie);
        initIfNeeded();

        final Point2D mouseScreenPoint = pie.getPosition();

        final int position = getWizard().getPosition();
        final Coordinate pointCoordinate = getHandler().getPointCoordinate(position);

        if (!getWizard().isCoordinateSelected() || (pointCoordinate != null)) {
            final Point2D pointScreenPoint = (!getWizard().isCoordinateSelected()) ? mouseScreenPoint
                                                                                   : getScreenPoint(pointCoordinate);
            setPointZoom(pointScreenPoint);
        }

        final Coordinate coordinate = getWizard().getSelectedCoordinate();
        if (getWizard().isCoordinateSelected() || (coordinate != null)) {
            final Point2D coordinateScreenPoint = getWizard().isCoordinateSelected() ? mouseScreenPoint
                                                                                     : getScreenPoint(coordinate);
            setCoordinateZoom(coordinateScreenPoint);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  screenPoint  DOCUMENT ME!
     */
    private void setPointZoom(final Point2D screenPoint) {
        final Rectangle2D viewBounds = calculateBounds(screenPoint);
        getPointZoomViewCanvas().getCamera().setViewBounds(viewBounds);
        getPropertyChangeListenerHandler().propertyChange(null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  screenPoint  DOCUMENT ME!
     */
    private void setCoordinateZoom(final Point2D screenPoint) {
        final Rectangle2D pointViewBounds = calculateBounds(screenPoint);
        getCoordinateZoomViewCanvas().getCamera().setViewBounds(pointViewBounds);
        getPropertyChangeListenerHandler().propertyChange(null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   coordinate  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static Point2D getScreenPoint(final Coordinate coordinate) {
        return new Point2D.Double(getWtst().getScreenX(coordinate.x), getWtst().getScreenY(coordinate.y));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   point  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Rectangle2D calculateBounds(final Point2D point) {
        final int width = getViewWidth();
        final int height = getViewHeight();

        return new Rectangle2D.Double(
                point.getX()
                        - (width / 2),
                point.getY()
                        - (height / 2),
                width,
                height);
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
        return (PanAndMousewheelZoomListener)CismapBroker.getInstance().getMappingComponent()
                    .getInputListener(MappingComponent.PAN);
    }

    @Override
    public void mouseWheelRotated(final PInputEvent pie) {
        getPanAndMousewheelZoomListener().mouseWheelRotated(pie);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static WorldToScreenTransform getWtst() {
        return CismapBroker.getInstance().getMappingComponent().getWtst();
    }

    @Override
    public void mouseClicked(final PInputEvent pie) {
        super.mouseClicked(pie);

        if (pie.isLeftMouseButton()) {
            if (pie.getClickCount() < 2) {
                final Point2D mapPoint = pie.getPosition();
                final WorldToScreenTransform wtst = getWtst();
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

    /**
     * DOCUMENT ME!
     *
     * @param  position  DOCUMENT ME!
     */
    public void updatePointZoom(final int position) {
        final Coordinate pointCoordinate = getHandler().getPointCoordinate(position);
        if (pointCoordinate != null) {
            final Point2D screenPoint = getScreenPoint(pointCoordinate);
            setPointZoom(screenPoint);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  position  DOCUMENT ME!
     */
    public void updateCoordinateZoom(final int position) {
        final Coordinate coordinate = getHandler().getCoordinate(position);
        if (coordinate != null) {
            final Point2D screenPoint = getScreenPoint(coordinate);
            setCoordinateZoom(screenPoint);
        }
    }

    @Override
    public void pointSelected(final int position) {
        updatePointZoom(position);
        updateCoordinateZoom(position);
    }

    @Override
    public void coordinateSelected(final int position) {
        updatePointZoom(position);
        updateCoordinateZoom(position);
    }

    @Override
    public void handlerChanged(final RasterGeoReferencingHandler handler) {
    }

    @Override
    public void positionAdded(final int position) {
    }

    @Override
    public void positionRemoved(final int position) {
    }

    @Override
    public void positionChanged(final int position) {
        updatePointZoom(position);
        updateCoordinateZoom(position);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class PropertyChangeListenerHandler implements PropertyChangeListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            for (final PropertyChangeListener propertyChangeListener : propertyChangeListeners) {
                propertyChangeListener.propertyChange(evt);
            }
        }
    }
}
