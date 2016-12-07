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
import com.vividsolutions.jts.geom.util.LineStringExtracter;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

import org.openide.util.lookup.ServiceProvider;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.cismet.cismap.commons.features.CommonFeatureAction;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CommonFeatureAction.class)
public class SplitFeatureAction extends AbstractAction implements CommonFeatureAction {

    //~ Instance fields --------------------------------------------------------

    Feature currentFeature = null;

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DuplicateGeometryFeatureAction object.
     */
    public SplitFeatureAction() {
//        super(NbBundle.getMessage(
//                DuplicateGeometryFeatureAction.class,
//                "DuplicateGeometryFeatureAction.DuplicateGeometryFeatureAction()"));
        super("Split");
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
        return currentFeature;
    }

    @Override
    public boolean isActive() {
        return ((currentFeature instanceof PureNewFeature) && (currentFeature.getGeometry() instanceof Polygon)
                        && (getIntersectingLineFeatureIfUnambigous(currentFeature) != null));
    }

    @Override
    public void setSourceFeature(final Feature source) {
        currentFeature = source;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geometry  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry polygonize(final Geometry geometry) {
        final List lines = LineStringExtracter.getLines(geometry);
        final Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(lines);
        final Collection polys = polygonizer.getPolygons();
        final Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);
        return geometry.getFactory().createGeometryCollection(polyArray);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   poly  DOCUMENT ME!
     * @param   line  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry splitPolygon(final Geometry poly, final Geometry line) {
        final Geometry nodedLinework = poly.getBoundary().union(line);
        final Geometry polys = polygonize(nodedLinework);

        // Only keep polygons which are inside the input
        final List output = new ArrayList();
        for (int i = 0; i < polys.getNumGeometries(); i++) {
            final Polygon candpoly = (Polygon)polys.getGeometryN(i);
            if (poly.contains(candpoly.getInteriorPoint())) {
                output.add(candpoly);
            }
        }
        return poly.getFactory().createGeometryCollection(GeometryFactory.toGeometryArray(output));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   polygonfeature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Feature getIntersectingLineFeatureIfUnambigous(final Feature polygonfeature) {
        Feature lineFeature = null;
        for (final Feature f : CismapBroker.getInstance().getMappingComponent().getFeatureCollection().getAllFeatures()) {
            if ((f != polygonfeature) && (f.getGeometry() instanceof LineString)
                        && polygonfeature.getGeometry().intersects(f.getGeometry())) {
                if (lineFeature == null) {
                    lineFeature = f;
                } else {
                    return null;
                }
            }
        }
        return lineFeature;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final WaitDialog wd = new WaitDialog();
//        EventQueue.invokeLater(new Runnable() {
//
//                @Override
//                public void run() {
//                    StaticSwingTools.showDialog(wd);
//                }
//            });
        de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    Thread.currentThread().setName("SplitGeometryFeatureAction");

                    final Feature lineFeature = getIntersectingLineFeatureIfUnambigous(currentFeature);

                    final Geometry splitResult = splitPolygon(currentFeature.getGeometry(), lineFeature.getGeometry());

                    if (splitResult instanceof GeometryCollection) {
                        for (int i = 0; i < ((GeometryCollection)splitResult).getNumGeometries(); i++) {
                            final Geometry geom = ((GeometryCollection)splitResult).getGeometryN(i);
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
                        }
                        CismapBroker.getInstance()
                                .getMappingComponent()
                                .getFeatureCollection()
                                .removeFeature(lineFeature);
                        CismapBroker.getInstance()
                                .getMappingComponent()
                                .getFeatureCollection()
                                .removeFeature(currentFeature);
                    }

                    return null;
                }

                @Override
                protected void done() {
//                    wd.setVisible(false);
//                    wd.dispose();
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
}
/**
 * DOCUMENT ME!
 *
 * @version $Revision$, $Date$
 */
//class WaitDialog extends JDialog {
//
//    //~ Constructors -----------------------------------------------------------
//
//    /**
//     * Creates a new WaitDialog object.
//     */
//    public WaitDialog() {
//        super(StaticSwingTools.getParentFrame(CismapBroker.getInstance().getMappingComponent()), true);
//        setLayout(new FlowLayout());
//        getContentPane().add(new JLabel(
//                new javax.swing.ImageIcon(
//                    getClass().getResource("/de/cismet/cismap/actions/raiseProgress.png"))));
//        final JProgressBar prb = new JProgressBar();
//        prb.setForeground(new Color(51, 153, 204));
//        prb.setBorderPainted(false);
//        prb.setIndeterminate(true);
//        getContentPane().add(prb);
//        setUndecorated(true);
//        final JComponent c = CismapBroker.getInstance().getMappingComponent();
//        pack();
//        setLocationRelativeTo(c);
//    }
//}
//
//}
