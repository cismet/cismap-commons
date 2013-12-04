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

import org.apache.log4j.Logger;

import javax.swing.SwingWorker;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.RetrievalServiceLayer;
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

    //~ Instance fields --------------------------------------------------------

    private Logger LOG = Logger.getLogger(ZoomToFeaturesWorker.class);
    private Feature[] features;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ZoomToLayerWorker object.
     *
     * @param  features  tps DOCUMENT ME!
     */
    public ZoomToFeaturesWorker(final Feature[] features) {
        this.features = features;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Geometry doInBackground() throws Exception {
        Geometry geom = null;

        for (final Feature f : features) {
            Geometry g = f.getGeometry();

            if (g != null) {
                g = g.getEnvelope();

                if (geom == null) {
                    geom = g.getEnvelope();
                    geom.setSRID(g.getSRID());
                } else {
                    if (geom.getSRID() != g.getSRID()) {
                        g = CrsTransformer.transformToGivenCrs(
                                g,
                                CrsTransformer.createCrsFromSrid(geom.getSRID()));
                    }
                    final Geometry ge = g.getEnvelope();
                    ge.setSRID(geom.getSRID());
                    geom = geom.union(g);
                }
            }
        }
        return geom;
    }

    @Override
    protected void done() {
        try {
            final Geometry geom = get();

            if (geom != null) {
                CismapBroker.getInstance().getMappingComponent().gotoBoundingBoxWithHistory(new XBoundingBox(geom));
            }
        } catch (Exception e) {
            LOG.error("Error while zooming to selected features", e);
        }
    }
}
;
