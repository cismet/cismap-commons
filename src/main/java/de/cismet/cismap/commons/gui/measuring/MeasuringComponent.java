/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2010 srichter
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
/*
 * MeasuringComponent.java
 *
 * Created on 10.02.2010, 11:00:03
 */
package de.cismet.cismap.commons.gui.measuring;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.AffineTransformation;

import java.awt.Cursor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

import java.util.List;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.DefaultRasterDocumentFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.RasterDocumentFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MessenGeometryListener;

import de.cismet.tools.collections.TypeSafeCollections;

/**
 * DOCUMENT ME!
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public class MeasuringComponent extends javax.swing.JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final String MY_MESSEN_MODE = "MY_MESSEN_MODE";

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final XBoundingBox initialBoundingBox;
    private RasterDocumentFeature mainRasterDocumentFeature;
    private final MessenGeometryListener mapListener;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.cismet.cismap.commons.gui.MappingComponent map;
    private javax.swing.JPanel panCenter;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form MeasuringComponent.
     */
    public MeasuringComponent() {
        // Wupp home
        this(new XBoundingBox(
                2583621.251964098d,
                5682507.032498134d,
                2584022.9413952776d,
                5682742.852810634d,
                "EPSG:31466",
                false),
            new Crs("EPSG:31466", "EPSG:31466", "EPSG:31466", false, true));
    }

    /**
     * Creates a new MeasuringComponent object.
     *
     * @param  initialBBox  DOCUMENT ME!
     * @param  srs          DOCUMENT ME!
     */
    public MeasuringComponent(final XBoundingBox initialBBox, final Crs srs) {
        initComponents();
        this.mapListener = new MessenGeometryListener(map);
        this.initialBoundingBox = initialBBox;
        this.mainRasterDocumentFeature = null;
        final ActiveLayerModel mappingModel = new ActiveLayerModel();
        mappingModel.addHome(initialBBox);
        mappingModel.setSrs(srs);
        mappingModel.setDefaultHomeSrs(srs);
        map.setAnimationDuration(0);
        map.setReadOnly(false);
        map.setMappingModel(mappingModel);
        // initial positioning of the map
        map.gotoInitialBoundingBox();
        // interaction mode
        map.setInteractionMode(MappingComponent.PAN);
        map.addInputListener(MY_MESSEN_MODE, mapListener);
        map.unlock();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MessenGeometryListener getMessenInputListener() {
        return mapListener;
    }

    /**
     * DOCUMENT ME!
     */
    public void dispose() {
        // TODO:
        // this is a quick fix for the memory leak that some mapping components can not be garbage collected
        panCenter.remove(map);
        map.removeInputEventListener(mapListener);
        map.dispose();
        map = null;
    }

    /**
     * DOCUMENT ME!
     */
    public void reset() {
        removeAllFeatures();
        map.setInteractionMode(MappingComponent.PAN);
    }

    /**
     * DOCUMENT ME!
     */
    public void removeAllFeatures() {
        final FeatureCollection fc = map.getFeatureCollection();
        if (fc instanceof DefaultFeatureCollection) {
            ((DefaultFeatureCollection)fc).clear();
        } else {
            fc.removeAllFeatures();
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void gotoInitialBoundingBox() {
        map.gotoInitialBoundingBox();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    public void addFeature(final Feature feature) {
        if (feature != null) {
            if (feature instanceof RasterDocumentFeature) {
                mainRasterDocumentFeature = (RasterDocumentFeature)feature;
            }
            getFeatureCollection().addFeature(feature);
        } else {
            log.warn("Feature is null!");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bi  DOCUMENT ME!
     */
    public void addImage(final BufferedImage bi) {
        addImage(bi, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bi        DOCUMENT ME!
     * @param  geometry  DOCUMENT ME!
     */
    public void addImage(final BufferedImage bi, final Geometry geometry) {
        final DefaultRasterDocumentFeature drdf;
        if (geometry != null) {
            drdf = new DefaultRasterDocumentFeature(bi, geometry);
        } else {
            drdf = new DefaultRasterDocumentFeature(bi, initialBoundingBox.getX1(), initialBoundingBox.getY1());
        }
        addFeature(drdf);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponent getMap() {
        return map;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureCollection getFeatureCollection() {
        return map.getFeatureCollection();
    }

    /**
     * DOCUMENT ME!
     */
    public void zoomToFeatureCollection() {
        if ((map.getWidth() > 0) && (map.getHeight() > 0)) {
            map.zoomToFeatureCollection();
        } else {
            // lazy zoom if map is hidden and has size zero.
            map.addComponentListener(new ComponentAdapter() {

                    @Override
                    public void componentResized(final ComponentEvent e) {
                        if ((map.getWidth() > 0) && (map.getHeight() > 0)) {
                            map.zoomToFeatureCollection();
                            map.removeComponentListener(this);
                        }
                    }
                });
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        panCenter = new javax.swing.JPanel();
        map = new de.cismet.cismap.commons.gui.MappingComponent();

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        panCenter.setOpaque(false);
        panCenter.setLayout(new java.awt.BorderLayout());

        map.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panCenter.add(map, java.awt.BorderLayout.CENTER);

        add(panCenter, java.awt.BorderLayout.CENTER);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param   realDistance  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double calculateScaleFactor(final double realDistance) {
        for (final Feature f : map.getFeatureCollection().getAllFeatures()) {
            // Danger: Messlinie finden...unter der Annahme dass es nur ein PNF gibt!
            if (f instanceof PureNewFeature) {
                final double virtualDistance = f.getGeometry().getLength();
                if (virtualDistance != 0) {
                    return realDistance / virtualDistance;
                }
            }
        }
        return 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  scalefactor  DOCUMENT ME!
     */
    private void applyScaling(final double scalefactor) {
        // save camera position
        final BoundingBox oldViewBounds = map.getCurrentBoundingBox();
        double vX1 = oldViewBounds.getX1();
        double vY1 = oldViewBounds.getY1();
        double vX2 = oldViewBounds.getX2();
        double vY2 = oldViewBounds.getY2();

        // unhold all features, so that they all can be scaled
        getFeatureCollection().setHoldAll(false);
        final List<Feature> backup = TypeSafeCollections.newArrayList();
        for (final Feature f : map.getFeatureCollection().getAllFeatures()) {
            backup.add(f);
        }
        map.getFeatureCollection().removeAllFeatures();
        AffineTransformation trafo = new AffineTransformation();
        trafo = trafo.scale(scalefactor, scalefactor);
        Point centroid = mainRasterDocumentFeature.getGeometry().getCentroid();
        final double oldX = centroid.getX();
        final double oldY = centroid.getY();

        for (final Feature f : backup) {
            f.getGeometry().apply(trafo);
        }
        centroid = mainRasterDocumentFeature.getGeometry().getCentroid();
        final double transX = oldX - centroid.getX();
        final double transY = oldY - centroid.getY();
        final AffineTransformation backTranslation = AffineTransformation.translationInstance(transX, transY);
        for (final Feature f : backup) {
            f.getGeometry().apply(backTranslation);
        }
        map.getFeatureCollection().addFeatures(backup);
        // apply trafo on camera position
        vX1 *= scalefactor;
        vY1 *= scalefactor;
        vX2 *= scalefactor;
        vY2 *= scalefactor;
        vX1 += transX;
        vY1 += transY;
        vX2 += transX;
        vY2 += transY;
        final BoundingBox newBB = new BoundingBox(vX1, vY1, vX2, vY2);
        // adjust camera
        map.gotoBoundingBox(newBB, false, true, 0);
    }

    /**
     * DOCUMENT ME!
     */
    public void actionOverview() {
        map.zoomToFeatureCollection();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  measuredDistance  DOCUMENT ME!
     */
    public void actionCalibrate(final double measuredDistance) {
        if ((measuredDistance != 0d) && (mainRasterDocumentFeature != null)) {
            final double scalefactor = calculateScaleFactor(measuredDistance);
            applyScaling(scalefactor);
        }
        map.setInteractionMode(MappingComponent.PAN);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry getMainDocumentGeometry() {
        if (mainRasterDocumentFeature != null) {
            return mainRasterDocumentFeature.getGeometry();
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     */
    public void actionMeasurePolygon() {
        mapListener.setMode(MessenGeometryListener.POLYGON);
        map.setInteractionMode(MY_MESSEN_MODE);
        map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * DOCUMENT ME!
     */
    public void actionMeasureLine() {
        mapListener.setMode(MessenGeometryListener.LINESTRING);
        map.setInteractionMode(MY_MESSEN_MODE);
        map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * DOCUMENT ME!
     */
    public void actionPan() {
        map.setInteractionMode(MappingComponent.PAN);
    }

    /**
     * DOCUMENT ME!
     */
    public void actionZoom() {
        map.setInteractionMode(MappingComponent.ZOOM);
    }
}
