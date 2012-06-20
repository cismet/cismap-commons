/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;

import org.apache.log4j.Logger;

import java.awt.Color;

import java.util.Collection;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
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

    //~ Instance fields --------------------------------------------------------

    // delegate to enable zoom during creation.
    private final PBasicInputEventHandler zoomDelegate;
    private PFeature selectedPFeature = null;
    private int selectedEntityPosition = -1;
    private boolean creatingHole = false;

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
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Color getFillingColor() {
        return new Color(1f, 0f, 0f, 0.5f);
    }
    
    @Override
    public void mousePressed(final PInputEvent pInputEvent) {
        if (!isInProgress() && pInputEvent.isAltDown()) {
            if (selectedPFeature == null) {
                final Collection selectedFeatures = getMappingComponent().getFeatureCollection().getSelectedFeatures();
                if (selectedFeatures.size() == 1) {
                    final PFeature pFeature = getMappingComponent().getPFeatureHM()
                                .get((Feature)selectedFeatures.toArray()[0]);
                    if ((pFeature != null)
                                && (pFeature.getFeature().getGeometry() instanceof MultiPolygon)) {
                        selectedPFeature = pFeature;

                        selectedEntityPosition = pFeature.getEntityUnderCoordinate(
                                new Coordinate(
                                    getMappingComponent().getWtst().getSourceX(
                                        pInputEvent.getPosition().getX()
                                                - getMappingComponent().getClip_offset_x()),
                                    getMappingComponent().getWtst().getSourceY(
                                        pInputEvent.getPosition().getY()
                                                - getMappingComponent().getClip_offset_y())));
                        creatingHole = selectedEntityPosition != -1;
                        super.mousePressed(pInputEvent);
                    }
                } else {
                    final PFeature pFeature = (PFeature)PFeatureTools.getFirstValidObjectUnderPointer(
                            pInputEvent,
                            new Class[] { PFeature.class });
                    if ((pFeature != null)
                                && (pFeature.getFeature().getGeometry() instanceof MultiPolygon)) {
                        getMappingComponent().getFeatureCollection().select(pFeature.getFeature());
                    }
                }
            }
        } else {
            super.mousePressed(pInputEvent);
        }
    }

    @Override
    protected PPath createNewTempFeature() {
        final PPath newTempFeature = super.createNewTempFeature();
        final Color fillingColor = getFillingColor();
        newTempFeature.setStrokePaint(fillingColor.darker());
        newTempFeature.setPaint(fillingColor);
        return newTempFeature;
    }

    @Override
    protected void finishGeometry(final PureNewFeature newFeature) {
        super.finishGeometry(newFeature);
        if (selectedPFeature == null) {
            final int currentSrid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode());

            if (LOG.isDebugEnabled()) {
                LOG.debug("new geometry" + newFeature.getGeometry().toText() + " srid: " + currentSrid);
            }

            newFeature.getGeometry().setSRID(currentSrid);

            newFeature.setEditable(true);
            getMappingComponent().getFeatureCollection().addFeature(newFeature);
            getMappingComponent().getFeatureCollection().holdFeature(newFeature);
        } else {
            final Polygon polygon = (Polygon)newFeature.getGeometry();

            if (creatingHole) {
                selectedPFeature.addHoleToEntity(selectedEntityPosition, polygon.getExteriorRing());
            } else {
                selectedPFeature.addEntity(polygon);
            }
            selectedPFeature = null;
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
