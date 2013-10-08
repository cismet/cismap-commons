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

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.featureservice.factory.ShapeFeatureFactory;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.SlidableWMSServiceLayerGroup;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.wms.capabilities.Envelope;
import de.cismet.cismap.commons.wms.capabilities.Layer;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ZoomToLayerWorker extends SwingWorker<Geometry, Geometry> {

    //~ Instance fields --------------------------------------------------------

    Logger LOG = Logger.getLogger(ZoomToLayerWorker.class);
    private TreePath[] tps;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ZoomToLayerWorker object.
     *
     * @param  tps  DOCUMENT ME!
     */
    public ZoomToLayerWorker(final TreePath[] tps) {
        final List<TreePath> tpl = new ArrayList<TreePath>();

        for (final TreePath tmp : tps) {
            final Object layer = tmp.getLastPathComponent();

            if (layer instanceof LayerCollection) {
                tpl.addAll(getAllServices(tmp.getParentPath(), (LayerCollection)layer));
            } else {
                tpl.add(tmp);
            }
        }

        this.tps = tpl.toArray(new TreePath[tpl.size()]);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   parent  DOCUMENT ME!
     * @param   lc      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<TreePath> getAllServices(final TreePath parent, final LayerCollection lc) {
        final List<TreePath> tpl = new ArrayList<TreePath>();
        final TreePath parentWithCollection = parent.pathByAddingChild(lc);

        for (final Object o : lc) {
            if (o instanceof LayerCollection) {
                tpl.addAll(getAllServices(parentWithCollection, (LayerCollection)o));
            } else if (o != null) {
                tpl.add(parentWithCollection.pathByAddingChild(o));
            }
        }

        return tpl;
    }

    @Override
    protected Geometry doInBackground() throws Exception {
        Geometry geom = null;

        for (final TreePath path : tps) {
            Geometry g = null;
            RetrievalServiceLayer rsl = null;

            if ((path != null) && (path.getLastPathComponent() instanceof RetrievalServiceLayer)) {
                rsl = (RetrievalServiceLayer)path.getLastPathComponent();
            } else if ((path != null)
                        && (path.getParentPath().getLastPathComponent() instanceof RetrievalServiceLayer)) {
                rsl = (RetrievalServiceLayer)path.getParentPath().getLastPathComponent();
            }

            if (rsl != null) {
                g = getServiceBounds(rsl);
            }

            if (g != null) {
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

    /**
     * DOCUMENT ME!
     *
     * @param   rsl  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry getServiceBounds(final RetrievalServiceLayer rsl) {
        Geometry g = null;

        if (rsl instanceof WMSServiceLayer) {
            final Layer l = ((WMSServiceLayer)rsl).getLayerInformation();
            Envelope envelope = null;

            if (l != null) {
                envelope = CapabilityWidget.getEnvelopeForWmsLayer(l);
            } else {
                final WMSCapabilities caps = ((WMSServiceLayer)rsl).getWmsCapabilities();

                if (caps != null) {
                    envelope = CapabilityWidget.getEnvelopeForWmsCaps(caps);
                }
            }

            if (envelope != null) {
                g = CapabilityWidget.createGeometryFromEnvelope(envelope);
            }
        } else if (rsl instanceof WebFeatureService) {
            final WebFeatureService l = ((WebFeatureService)rsl);
            final Envelope envelope = CapabilityWidget.getEnvelopeFromFeatureType(l.getFeature());

            if (envelope != null) {
                g = CapabilityWidget.createGeometryFromEnvelope(envelope);
            }
        } else if (rsl instanceof SlidableWMSServiceLayerGroup) {
            final Layer l = ((SlidableWMSServiceLayerGroup)rsl).getLayerInformation();

            if (l != null) {
                final Envelope envelope = CapabilityWidget.getEnvelopeForWmsLayer(l);

                if (envelope != null) {
                    g = CapabilityWidget.createGeometryFromEnvelope(envelope);
                }
            }
        } else if (rsl instanceof SimpleWMS) {
            final SimpleWMS wms = ((SimpleWMS)rsl);
            final Layer l = wms.getLayerInformation();

            if (l != null) {
                final Envelope envelope = CapabilityWidget.getEnvelopeForWmsLayer(l);

                if (envelope != null) {
                    g = CapabilityWidget.createGeometryFromEnvelope(envelope);
                }
            }
        } else if (rsl instanceof ShapeFileFeatureService) {
            final ShapeFileFeatureService sffs = (ShapeFileFeatureService)rsl;
            g = ((ShapeFeatureFactory)sffs.getFeatureFactory()).getEnvelope();
        }

        return g;
    }

    @Override
    protected void done() {
        try {
            final Geometry geom = get();
            CismapBroker.getInstance().getMappingComponent().gotoBoundingBoxWithHistory(new XBoundingBox(geom));
        } catch (Exception e) {
            LOG.error("Error while zooming to extend", e);
        }
    }
}
;