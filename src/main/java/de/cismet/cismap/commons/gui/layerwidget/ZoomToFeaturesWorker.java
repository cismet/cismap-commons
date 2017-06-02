/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.layerwidget;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingWorker;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ZoomToFeaturesWorker extends SwingWorker<Geometry, Geometry> {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ZoomToFeaturesWorker.class);

    //~ Instance fields --------------------------------------------------------

    private Feature[] features;
    /** buffer in percent.* */
    private int buffer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ZoomToLayerWorker object.
     *
     * @param  features  tps DOCUMENT ME!
     */
    public ZoomToFeaturesWorker(final Feature[] features) {
        this(features, 0);
    }

    /**
     * Creates a new ZoomToLayerWorker object.
     *
     * @param  features  tps DOCUMENT ME!
     * @param  buffer    DOCUMENT ME!
     */
    public ZoomToFeaturesWorker(final Feature[] features, final int buffer) {
        this.features = features;
        this.buffer = buffer;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Geometry doInBackground() throws Exception {
        boolean first = true;
        int srid = 0;
        final List<Geometry> geomList = new ArrayList<Geometry>(features.length);

        for (final Feature f : features) {
            Geometry g = f.getGeometry();

            if (g != null) {
                g = g.getEnvelope();

                if (first) {
                    srid = g.getSRID();
                    first = false;
                } else {
                    if (g.getSRID() != srid) {
                        g = CrsTransformer.transformToGivenCrs(g, CrsTransformer.createCrsFromSrid(srid));
                    }
                }

                geomList.add(g);
            }
        }

        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);
        Geometry union = factory.buildGeometry(geomList);

        if (union instanceof GeometryCollection) {
            union = ((GeometryCollection)union).union();
        }

        return union;
    }

    @Override
    protected void done() {
        try {
            final Geometry geom = get();

            if ((geom != null) && !geom.isEmpty()) {
                final XBoundingBox boundingBox = new XBoundingBox(geom);

                if (buffer != 0) {
                    boundingBox.increase(buffer);
                }

                CismapBroker.getInstance().getMappingComponent().gotoBoundingBoxWithHistory(boundingBox);
            }
        } catch (Exception e) {
            LOG.error("Error while zooming to selected features", e);
        }
    }
}
