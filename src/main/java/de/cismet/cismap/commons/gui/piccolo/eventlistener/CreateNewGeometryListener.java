/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Geometry;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

import java.awt.Color;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class CreateNewGeometryListener extends CreateGeometryListener {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    // delegate to enable zoom during creation.
    private final PBasicInputEventHandler zoomDelegate;

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
    protected void finishGeometry(final PureNewFeature newFeature) {
        // the coordinates of the newly created feature should must the default srs, because
        // every feature uses internally the default srs.
        if (log.isDebugEnabled()) {
            log.debug("original geometry" + newFeature.getGeometry().toText());
        }
        Geometry defaultGeom = newFeature.getGeometry();
        final int currentSrid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode());
        defaultGeom.setSRID(currentSrid);
        defaultGeom = CrsTransformer.transformToDefaultCrs(defaultGeom);
        newFeature.setGeometry(defaultGeom);
        if (log.isDebugEnabled()) {
            log.debug("geometry after transformation to default crs " + newFeature.getGeometry().toText());
        }
        super.finishGeometry(newFeature);

        newFeature.setEditable(true);
        mc.getFeatureCollection().addFeature(newFeature);
        mc.getFeatureCollection().holdFeature(newFeature);
    }

    @Override
    public void mouseWheelRotated(final PInputEvent pie) {
        // delegate zoom event
        zoomDelegate.mouseWheelRotated(pie);
        // trigger full repaint
        mouseMoved(pie);
    }
}
