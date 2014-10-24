/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.*;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.event.PNotificationCenter;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.util.Collection;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.*;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureDeleteAction;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureRemoveEntityAction;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureRemoveHoleAction;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class CreateNewGeometryListener extends CreateGeometryListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CreateNewGeometryListener.class);
    private static Collection<? extends GeometryCheckInterface> geometryChecks;

    static {
        geometryChecks = Lookup.getDefault().lookupAll(GeometryCheckInterface.class);
    }

    //~ Instance fields --------------------------------------------------------

    // delegate to enable zoom during creation.
    private final PBasicInputEventHandler zoomDelegate;
    private PFeature selectedPFeature = null;
    private int selectedEntityPosition = -1;
    private boolean creatingHole = false;
    private InvalidPolygonTooltip multiPolygonPointerAnnotation = new InvalidPolygonTooltip();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CreateNewGeometryListener object.
     *
     * @param  mc  DOCUMENT ME!
     */
    public CreateNewGeometryListener(final MappingComponent mc) {
        this(mc, PureNewFeature.class);
    }

    /**
     * Creates a new instance of CreateNewGeometryListener.
     *
     * @param  mc                    DOCUMENT ME!
     * @param  geometryFeatureClass  DOCUMENT ME!
     */
    private CreateNewGeometryListener(final MappingComponent mc, final Class geometryFeatureClass) {
        super(mc, geometryFeatureClass);
        zoomDelegate = new RubberBandZoomListener();
        mc.getCamera().addChild(multiPolygonPointerAnnotation);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Color getFillingColor() {
        return new Color(1f, 0f, 0f, 0.5f);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mousePosition  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Point getMousePoint(final Point2D mousePosition) {
        final WorldToScreenTransform wtst = getMappingComponent().getWtst();
        final double mouseCoordX = wtst.getSourceX(mousePosition.getX() - getMappingComponent().getClip_offset_x());
        final double mouseCoordY = wtst.getSourceY(mousePosition.getY() - getMappingComponent().getClip_offset_y());
        final Coordinate mouseCoord = new Coordinate(mouseCoordX, mouseCoordY);
        final int currentSrid = CrsTransformer.extractSridFromCrs(getMappingComponent().getMappingModel().getSrs()
                        .getCode());
        final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                currentSrid);
        final Point mousePoint = CrsTransformer.transformToGivenCrs(geometryFactory.createPoint(
                    mouseCoord),
                CrsTransformer.createCrsFromSrid(
                    selectedPFeature.getFeature().getGeometry().getSRID()));
        return mousePoint;
    }

    @Override
    public void mousePressed(final PInputEvent pInputEvent) {
        final AbstractNewFeature tempFeature = getCurrentNewFeature();

        if ((tempFeature != null) && (pInputEvent.getClickCount() == 1)) {
            final Geometry tempGeometry = tempFeature.getGeometry();

            if ((geometryChecks != null) && !geometryChecks.isEmpty()) {
                final Point2D lastPoint = pInputEvent.getPosition();
                final WorldToScreenTransform wtst = mappingComponent.getWtst();
                final Coordinate lastCoordinate = new Coordinate(wtst.getSourceX((float)lastPoint.getX()),
                        wtst.getSourceY((float)lastPoint.getY()));
                boolean ignoreLastGeometryCoordinate = false;

                if (points.size() < tempGeometry.getCoordinates().length) {
                    ignoreLastGeometryCoordinate = true;
                }

                for (final GeometryCheckInterface check : geometryChecks) {
                    if (!check.check(tempGeometry, lastCoordinate, ignoreLastGeometryCoordinate)) {
                        return;
                    }
                }
            }
        }
        multiPolygonPointerAnnotation.setVisible(false);

        if (pInputEvent.isLeftMouseButton()) {
            if (pInputEvent.getClickCount() == 1) {
                if (!isInProgress()) {
                    if (pInputEvent.isAltDown()
                                && (isInMode(POLYGON) || isInMode(ELLIPSE) || isInMode(RECTANGLE)
                                    || isInMode(RECTANGLE_FROM_LINE))) {
                        final Collection selectedFeatures = getMappingComponent().getFeatureCollection()
                                    .getSelectedFeatures();
                        if ((selectedPFeature != null) && (selectedFeatures.size() == 1)) {
                            final PFeature pFeature = getMappingComponent().getPFeatureHM()
                                        .get((Feature)selectedFeatures.toArray()[0]);
                            if ((pFeature != null)
                                        && ((pFeature.getFeature().getGeometry() instanceof MultiPolygon)
                                            || (pFeature.getFeature().getGeometry() instanceof Polygon))) {
                                final Point mousePoint = getMousePoint(pInputEvent.getPosition());
                                selectedEntityPosition = pFeature.getEntityPositionUnderPoint(mousePoint);
                                creatingHole = selectedEntityPosition != -1;
                                super.mousePressed(pInputEvent);
                            }
                        } else {
                            final PFeature pFeature = (PFeature)PFeatureTools.getFirstValidObjectUnderPointer(
                                    pInputEvent,
                                    new Class[] { PFeature.class });
                            if ((pFeature != null)
                                        && ((pFeature.getFeature().getGeometry() instanceof MultiPolygon)
                                            || (pFeature.getFeature().getGeometry() instanceof Polygon))) {
                                getMappingComponent().getFeatureCollection().select(pFeature.getFeature());
                                selectedPFeature = pFeature;
                            }
                        }
                    } else { // es wird ein normales polygon angefangen
                        selectedPFeature = null;
                        super.mousePressed(pInputEvent);
                    }
                } else {
                    super.mousePressed(pInputEvent);
                }
            } else if (pInputEvent.getClickCount() == 2) {
                if ((selectedPFeature == null) || isTempFeatureValid()) {
                    super.mousePressed(pInputEvent);
                }
            } else {
                super.mousePressed(pInputEvent);
            }
        } else {
            selectedPFeature = null;
            super.mousePressed(pInputEvent);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isTempFeatureValid() {
        final AbstractNewFeature tempFeature = getCurrentNewFeature();
        final Coordinate[] tempFeatureCoordinates = tempFeature.getGeometry().getCoordinates();

        if (tempFeatureCoordinates.length == 3) {
            final int currentSrid = selectedPFeature.getFeature().getGeometry().getSRID();
            final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                    currentSrid);
            final Point point = geometryFactory.createPoint(tempFeatureCoordinates[1]);
            if (creatingHole) {
                return selectedPFeature.getEntityPositionUnderPoint(point) == selectedEntityPosition;
            } else {
                return selectedPFeature.getEntityPositionUnderPoint(point) == -1;
            }
        }
        if (creatingHole) {
            return selectedPFeature.isValidWithThisNewHoleCoordinates(selectedEntityPosition, tempFeatureCoordinates);
        } else {
            return selectedPFeature.isValidWithThisNewEntityCoordinates(tempFeatureCoordinates);
        }
    }

    @Override
    protected void reset() {
        super.reset();
        selectedPFeature = null;
        selectedEntityPosition = -1;
        creatingHole = false;
    }

    @Override
    public void mouseMoved(final PInputEvent pInputEvent) {
        super.mouseMoved(pInputEvent);
        if (isInMode(POLYGON) || isInMode(ELLIPSE) || isInMode(RECTANGLE) || isInMode(RECTANGLE_FROM_LINE)) {
            multiPolygonPointerAnnotation.setOffset(
                pInputEvent.getCanvasPosition().getX()
                        + 20.0d,
                pInputEvent.getCanvasPosition().getY()
                        + 20.0d);

            final Collection selectedFeatures = getMappingComponent().getFeatureCollection().getSelectedFeatures();
            if ((selectedPFeature == null) || (selectedFeatures.size() != 1)) {
                if (pInputEvent.isAltDown()) {
                    multiPolygonPointerAnnotation.setMode(InvalidPolygonTooltip.Mode.SELECT_FEATURE);
                    multiPolygonPointerAnnotation.setVisible(true);
                } else {
                    multiPolygonPointerAnnotation.setVisible(false);
                }
            } else {
                if (isInProgress()) {
                    if (!isTempFeatureValid()) {
                        if (creatingHole) {
                            multiPolygonPointerAnnotation.setMode(InvalidPolygonTooltip.Mode.HOLE_ERROR);
                        } else {
                            multiPolygonPointerAnnotation.setMode(InvalidPolygonTooltip.Mode.ENTITY_ERROR);
                        }
                        multiPolygonPointerAnnotation.setVisible(true);
                    } else {
                        multiPolygonPointerAnnotation.setVisible(false);
                    }
                } else {
                    multiPolygonPointerAnnotation.setVisible(false);
                }
            }
        } else {
            multiPolygonPointerAnnotation.setVisible(false);
            final AbstractNewFeature tempFeature = getCurrentNewFeature();

            if (tempFeature != null) {
                final Geometry tempGeometry = tempFeature.getGeometry();
                boolean errorFound = false;

                if ((geometryChecks != null) && !geometryChecks.isEmpty()) {
                    final Point2D lastPoint = pInputEvent.getPosition();
                    final WorldToScreenTransform wtst = mappingComponent.getWtst();
                    final Coordinate lastCoordinate = new Coordinate(wtst.getSourceX(lastPoint.getX()),
                            wtst.getSourceY(lastPoint.getY()));

                    for (final GeometryCheckInterface check : geometryChecks) {
                        if (!check.check(tempGeometry, lastCoordinate, true)) {
                            multiPolygonPointerAnnotation.setCustomText("Geometrie ungültig", check.getErrorText());
                            errorFound = true;
                        }
                    }
                }
                multiPolygonPointerAnnotation.setVisible(errorFound);
            }
        }
    }

    @Override
    protected void finishGeometry(final AbstractNewFeature newFeature) {
        super.finishGeometry(newFeature);

        if (selectedPFeature == null) {
            newFeature.setEditable(true);
            getMappingComponent().getFeatureCollection().addFeature(newFeature);
            getMappingComponent().getFeatureCollection().holdFeature(newFeature);

            final PNotificationCenter pn = PNotificationCenter.defaultCenter();
            pn.postNotification(CreateGeometryListener.GEOMETRY_CREATED_NOTIFICATION, (AbstractNewFeature)newFeature);

            getMappingComponent().getMemUndo().addAction(new FeatureDeleteAction(getMappingComponent(), newFeature));
            getMappingComponent().getMemRedo().clear();
        } else {
            final Polygon polygon = (Polygon)newFeature.getGeometry();

            if (creatingHole) {
                selectedPFeature.addHoleToEntity(selectedEntityPosition, polygon.getExteriorRing());
                getMappingComponent().getMemUndo()
                        .addAction(new FeatureRemoveHoleAction(
                                getMappingComponent(),
                                selectedPFeature.getFeature(),
                                selectedEntityPosition,
                                polygon.getExteriorRing()));
            } else {
                selectedPFeature.addEntity(polygon);
                getMappingComponent().getMemUndo()
                        .addAction(new FeatureRemoveEntityAction(
                                mappingComponent,
                                selectedPFeature.getFeature(),
                                polygon));
            }

            getMappingComponent().getMemRedo().clear();
        }
    }

    @Override
    public void mouseWheelRotated(final PInputEvent pie) {
        // delegate zoom event
        zoomDelegate.mouseWheelRotated(pie);
        // trigger full repaint
        mouseMoved(pie);
    }
}
