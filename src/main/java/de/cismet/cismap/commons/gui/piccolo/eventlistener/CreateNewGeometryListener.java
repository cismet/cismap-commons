/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

import org.apache.log4j.Logger;

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

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CreateNewGeometryListener.class);

    //~ Instance fields --------------------------------------------------------

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
        final int currentSrid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode());

        if (LOG.isDebugEnabled()) {
            LOG.debug("new geometry" + newFeature.getGeometry().toText() + " srid: " + currentSrid);
        }

        newFeature.getGeometry().setSRID(currentSrid);
        super.finishGeometry(newFeature);

        newFeature.setEditable(true);
        getMappingComponent().getFeatureCollection().addFeature(newFeature);
        getMappingComponent().getFeatureCollection().holdFeature(newFeature);
    }

    @Override
    public void mouseWheelRotated(final PInputEvent pie) {
        // delegate zoom event
        zoomDelegate.mouseWheelRotated(pie);
        // trigger full repaint
        mouseMoved(pie);
    }
}
