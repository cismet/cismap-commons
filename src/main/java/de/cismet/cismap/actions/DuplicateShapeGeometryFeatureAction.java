/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2011 thorsten
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cismap.actions;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import de.cismet.cismap.commons.features.CommonFeatureAction;
import de.cismet.cismap.commons.features.CommonMultiAndSingleFeatureAction;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeaturesProvider;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.ShapeFeature;
import de.cismet.cismap.commons.featureservice.factory.ShapeFeatureFactory;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.GeometryUtils;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CommonFeatureAction.class)
public class DuplicateShapeGeometryFeatureAction extends AbstractAction implements CommonFeatureAction,
    FeaturesProvider,
    CommonMultiAndSingleFeatureAction {

    //~ Instance fields --------------------------------------------------------

    Feature f = null;
    List<Feature> featureList;

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DuplicateGeometryFeatureAction object.
     */
    public DuplicateShapeGeometryFeatureAction() {
        super(NbBundle.getMessage(
                DuplicateShapeGeometryFeatureAction.class,
                "DuplicateShapeGeometryFeatureAction.DuplicateShapeGeometryFeatureAction()"));
        super.putValue(
            Action.SMALL_ICON,
            new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/actions/raisePoly.png")));
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
        return (f instanceof ShapeFeature) && (f.getGeometry() != null)
                    && ((f.getGeometry() instanceof Polygon) || (f.getGeometry() instanceof MultiPolygon));
    }

    @Override
    public void setSourceFeature(final Feature source) {
        f = source;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final WaitDialog wd = new WaitDialog();
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    StaticSwingTools.showDialog(wd);
                }
            });
        de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    Thread.currentThread().setName("DuplicateShapeGeometryFeatureAction");

                    if (featureList != null) {
                        final List<ShapeFeatureFactory> shapes = new ArrayList<>();

                        for (final Feature feature : featureList) {
                            if (feature instanceof ShapeFeature) {
                                final ShapeFeature sFeature = (ShapeFeature)feature;
                                final ShapeFeatureFactory factory = (ShapeFeatureFactory)sFeature.getLayerProperties()
                                            .getFeatureService()
                                            .getFeatureFactory();

                                if ((factory != null) && !shapes.contains(factory)) {
                                    shapes.add(factory);
                                    addShapeFileAsFeature(factory);
                                }
                            }
                        }
                    } else {
                        if (f instanceof ShapeFeature) {
                            final ShapeFeature sFeature = (ShapeFeature)f;
                            final ShapeFeatureFactory factory = (ShapeFeatureFactory)sFeature.getLayerProperties()
                                        .getFeatureService()
                                        .getFeatureFactory();

                            if (factory != null) {
                                addShapeFileAsFeature(factory);
                            }
                        }
                    }
                    return null;
                }

                private void addShapeFileAsFeature(final ShapeFeatureFactory factory) throws Exception {
                    Geometry totalGeom = null;
                    final List<ShapeFeature> featureList = factory.createFeatures(null, null, null);
                    final List<Geometry> geometryList = new ArrayList<>();

                    for (final ShapeFeature f : featureList) {
                        geometryList.add(f.getGeometry());
                    }

                    try {
                        totalGeom = GeometryUtils.unionGeometries(geometryList);
                    } catch (Exception e) {
                        // reduce the precision toi make the union more stable
                        final PrecisionModel pm = new PrecisionModel(1000); // Millimeter
                        final GeometryPrecisionReducer reducer = new GeometryPrecisionReducer(pm);

                        final List<Geometry> reducedGeometryList = new ArrayList<>();

                        for (final Geometry g : geometryList) {
                            reducedGeometryList.add(reducer.reduce(g));
                        }

                        try {
                            totalGeom = GeometryUtils.unionGeometries(reducedGeometryList);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(
                                wd,
                                ex.getMessage(),
                                NbBundle.getMessage(
                                    DuplicateShapeGeometryFeatureAction.class,
                                    "DuplicateShapeGeometryFeatureAction.addShapeFileAsFeature().error_title"),
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    final PureNewFeature pnf = new PureNewFeature(totalGeom);

                    if ((totalGeom instanceof LineString) || (totalGeom instanceof MultiLineString)) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.LINESTRING);
                    } else if (totalGeom instanceof Polygon) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.POLYGON);
                    } else if (totalGeom instanceof MultiPolygon) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.MULTIPOLYGON);
                    } else if ((totalGeom instanceof Point) || (totalGeom instanceof MultiPoint)) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.POINT);
                    } else {
                        pnf.setGeometryType(PureNewFeature.geomTypes.UNKNOWN);
                    }

                    pnf.setEditable(true);
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addFeature(pnf);
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().holdFeature(pnf);
                }

                @Override
                protected void done() {
                    wd.setVisible(false);
                    wd.dispose();
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final WaitDialog w = new WaitDialog();
        w.setVisible(true);
    }

    @Override
    public void setSourceFeatures(final List<Feature> sourceList) {
        this.featureList = sourceList;

        final List<ShapeFeatureFactory> shapes = new ArrayList<>();

        for (final Feature feature : featureList) {
            if (feature instanceof ShapeFeature) {
                final ShapeFeature sFeature = (ShapeFeature)feature;
                final ShapeFeatureFactory factory = (ShapeFeatureFactory)sFeature.getLayerProperties()
                            .getFeatureService()
                            .getFeatureFactory();

                if ((factory != null) && !shapes.contains(factory)) {
                    shapes.add(factory);
                }
            }
        }

        super.putValue(
            Action.NAME,
            NbBundle.getMessage(
                DuplicateShapeGeometryFeatureAction.class,
                "DuplicateShapeGeometryFeatureAction.DuplicateShapeGeometriesFeatureAction()",
                new Object[] { shapes.size() }));
    }

    @Override
    public List<Feature> getSourceFeatures() {
        return featureList;
    }

    @Override
    public boolean isResponsibleFor(final Feature feature) {
        return true;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class WaitDialog extends JDialog {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new WaitDialog object.
         */
        public WaitDialog() {
            super(StaticSwingTools.getParentFrame(CismapBroker.getInstance().getMappingComponent()), true);
            setLayout(new FlowLayout());
            getContentPane().add(new JLabel(
                    new javax.swing.ImageIcon(
                        getClass().getResource("/de/cismet/cismap/actions/raiseProgress.png"))));
            final JProgressBar prb = new JProgressBar();
            prb.setForeground(new Color(51, 153, 204));
            prb.setBorderPainted(false);
            prb.setIndeterminate(true);
            getContentPane().add(prb);
            setUndecorated(true);
            final JComponent c = CismapBroker.getInstance().getMappingComponent();
            pack();
            setLocationRelativeTo(c);
        }
    }
}
