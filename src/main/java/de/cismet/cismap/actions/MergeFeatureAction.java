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
package de.cismet.cismap.actions;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.util.GeometryCombiner;

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.CheckedFeaturesProvider;
import de.cismet.cismap.commons.features.CommonFeatureAction;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeaturesProvider;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CommonFeatureAction.class)
public class MergeFeatureAction extends AbstractAction implements CommonFeatureAction, CheckedFeaturesProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MergeFeatureAction.class);

    //~ Instance fields --------------------------------------------------------

    Feature f = null;
    List<Feature> features = new ArrayList<>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DuplicateGeometryFeatureAction object.
     */
    public MergeFeatureAction() {
        super(NbBundle.getMessage(
                MergeFeatureAction.class,
                "MergeFeatureAction.MergeFeatureAction()"));
        super.putValue(
            Action.SMALL_ICON,
            new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/actions/arrow-join.png")));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getSorter() {
        return 1;
    }

    @Override
    public Feature getSourceFeature() {
        return f;
    }

    @Override
    public boolean isActive() {
        return CismapBroker.getInstance().getMappingComponent().getFeatureCollection().getSelectedFeatures().size() > 1;
    }

    @Override
    public void setSourceFeature(final Feature source) {
        f = source;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    Thread.currentThread().setName("MergeFeatureAction");

                    final ArrayList<Geometry> allGeoms = new ArrayList<Geometry>(features.size());

                    for (final Feature f : features) {
                        allGeoms.add(f.getGeometry());
                    }

                    final GeometryFactory gf = new GeometryFactory(
                            new PrecisionModel(PrecisionModel.FLOATING),
                            CrsTransformer.getCurrentSrid());
                    final Geometry geometryCollection = gf.buildGeometry(allGeoms);

                    final Geometry geom = geometryCollection.union();
                    final PureNewFeature pnf = new PureNewFeature(geom);
                    if ((geom instanceof LineString) || (geom instanceof MultiLineString)) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.LINESTRING);
                    } else if (geom instanceof Polygon) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.POLYGON);
                    } else if (geom instanceof MultiPolygon) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.MULTIPOLYGON);
                    } else if ((geom instanceof Point) || (geom instanceof MultiPoint)) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.POINT);
                    } else {
                        pnf.setGeometryType(PureNewFeature.geomTypes.UNKNOWN);
                    }

                    pnf.setEditable(true);
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addFeature(pnf);
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().holdFeature(pnf);
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeatures(features);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                    } catch (Exception e) {
                        LOG.error("Problem while merging geometries.", e);

                        final ErrorInfo errorInfo = new ErrorInfo(
                                NbBundle.getMessage(
                                    MergeFeatureAction.class,
                                    "MergeFeatureAction.actionPerformed().done().title"),
                                NbBundle.getMessage(
                                    MergeFeatureAction.class,
                                    "MergeFeatureAction.actionPerformed().done().message"),
                                null,
                                null,
                                e,
                                Level.ALL,
                                null);
                        JXErrorPane.showDialog(CismapBroker.getInstance().getMappingComponent(), errorInfo);
                    }
                }
            });
    }

    @Override
    public boolean isResponsibleFor(final Feature feature) {
        return (feature instanceof PureNewFeature);
    }

    @Override
    public void setSourceFeatures(final List<Feature> source) {
        features.clear();
        features.addAll(source);
    }

    @Override
    public List<Feature> getSourceFeatures() {
        return features;
    }

    @Override
    public boolean isActiveForFeatures(final List<Feature> source) {
        String type = null;

        for (final Feature tmp : source) {
            if ((tmp != null) && (tmp.getGeometry() != null)) {
                if (type == null) {
                    type = getGeometryType(tmp.getGeometry());
                } else {
                    if (!type.equals(getGeometryType(tmp.getGeometry()))) {
                        // different types will result in a geometry collection
                        return false;
                    }
                }

                if ((tmp.getGeometry() instanceof Point) || (tmp.getGeometry() instanceof MultiPoint)) {
                    // points will result in a MultiPoint, which cannot be handled properly by the
                    return false;
                }
            }
        }

        return source.size() > 1;
    }

    /**
     * Determine the geometry type of the given geometry. Make no difference between multi geoms and single geoms
     *
     * @param   geom  DOCUMENT ME!
     *
     * @return  the geometry type as string
     */
    private String getGeometryType(final Geometry geom) {
        if ((geom instanceof MultiPolygon) || (geom instanceof Polygon)) {
            return "polygon";
        } else if ((geom instanceof MultiLineString) || (geom instanceof LineString)) {
            return "linestring";
        } else if ((geom instanceof MultiPoint) || (geom instanceof Point)) {
            return "point";
        } else {
            return "unknown";
        }
    }
}
