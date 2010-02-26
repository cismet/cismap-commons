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
import de.cismet.cismap.commons.BoundingBox;
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
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 * @author srichter
 */
public class MeasuringComponent extends javax.swing.JPanel {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final XBoundingBox initialBoundingBox;
    private RasterDocumentFeature mainRasterDocumentFeature;
    private final MessenGeometryListener mapListener;
    private static final String MY_MESSEN_MODE = "MY_MESSEN_MODE";

    /** Creates new form MeasuringComponent */
    public MeasuringComponent() {
        //Wupp home
        this(new XBoundingBox(2583621.251964098d, 5682507.032498134d, 2584022.9413952776d, 5682742.852810634d, "EPSG:31466", false));
    }

    public MeasuringComponent(XBoundingBox initialBBox) {
        this(initialBBox, initialBBox.getSrs());
    }

    public MessenGeometryListener getMessenInputListener() {
        return mapListener;
    }

    public MeasuringComponent(XBoundingBox initialBBox, String srs) {
        initComponents();
        this.mapListener = new MessenGeometryListener(map);
        this.initialBoundingBox = initialBBox;
        this.mainRasterDocumentFeature = null;
        ActiveLayerModel mappingModel = new ActiveLayerModel();
        mappingModel.addHome(initialBBox);
        mappingModel.setSrs(srs);
        map.setReadOnly(false);
        map.setMappingModel(mappingModel);
        //initial positioning of the map
        map.gotoInitialBoundingBox();
        //interaction mode
        map.setInteractionMode(MappingComponent.PAN);
        map.addInputListener(MY_MESSEN_MODE, mapListener);
        map.unlock();
    }

    public void reset() {
        removeAllFeatures();
        map.setInteractionMode(MappingComponent.PAN);
    }

    public void removeAllFeatures() {
        final FeatureCollection fc = map.getFeatureCollection();
        if (fc instanceof DefaultFeatureCollection) {
            ((DefaultFeatureCollection) fc).clear();
        } else {
            fc.removeAllFeatures();
        }
    }

    public void gotoInitialBoundingBox() {
        map.gotoInitialBoundingBox();
    }

    public void addFeature(Feature feature) {
        if (feature != null) {
            if (feature instanceof RasterDocumentFeature) {
                mainRasterDocumentFeature = (RasterDocumentFeature) feature;
            }
            getFeatureCollection().addFeature(feature);
        } else {
            log.warn("Feature is null!");
        }
    }

    public void addImage(BufferedImage bi) {
        addImage(bi, null);
    }

    public void addImage(BufferedImage bi, Geometry geometry) {
        final DefaultRasterDocumentFeature drdf;
        if (geometry != null) {
            drdf = new DefaultRasterDocumentFeature(bi, geometry);
        } else {
            drdf = new DefaultRasterDocumentFeature(bi, initialBoundingBox.getX1(), initialBoundingBox.getY1());
        }
        addFeature(drdf);
    }

    public MappingComponent getMap() {
        return map;
    }

    public FeatureCollection getFeatureCollection() {
        return map.getFeatureCollection();
    }

    public void zoomToFeatureCollection() {
        int aDuration = map.getAnimationDuration();
        map.setAnimationDuration(0);
        map.zoomToFeatureCollection();
        map.setAnimationDuration(aDuration);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
    }// </editor-fold>//GEN-END:initComponents

    private double calculateScaleFactor(double realDistance) {
        for (Feature f : map.getFeatureCollection().getAllFeatures()) {
            //Danger: Messlinie finden...unter der Annahme dass es nur ein PNF gibt!
            if (f instanceof PureNewFeature) {
                double virtualDistance = f.getGeometry().getLength();
                if (virtualDistance != 0) {
                    return realDistance / virtualDistance;
                }
            }
        }
        return 0;
    }

    private void applyScaling(double scalefactor) {
        //save camera position
        BoundingBox oldViewBounds = map.getCurrentBoundingBox();
        double vX1 = oldViewBounds.getX1();
        double vY1 = oldViewBounds.getY1();
        double vX2 = oldViewBounds.getX2();
        double vY2 = oldViewBounds.getY2();

        //unhold all features, so that they all can be scaled
        getFeatureCollection().setHoldAll(false);
        final List<Feature> backup = TypeSafeCollections.newArrayList();
        for (Feature f : map.getFeatureCollection().getAllFeatures()) {
            backup.add(f);
        }
        map.getFeatureCollection().removeAllFeatures();
        AffineTransformation trafo = new AffineTransformation();
        trafo = trafo.scale(scalefactor, scalefactor);
        Point centroid = mainRasterDocumentFeature.getGeometry().getCentroid();
        double oldX = centroid.getX();
        double oldY = centroid.getY();

        for (Feature f : backup) {
            f.getGeometry().apply(trafo);
        }
        centroid = mainRasterDocumentFeature.getGeometry().getCentroid();
        double transX = oldX - centroid.getX();
        double transY = oldY - centroid.getY();
        AffineTransformation backTranslation = AffineTransformation.translationInstance(transX, transY);
        for (Feature f : backup) {
            f.getGeometry().apply(backTranslation);
        }
        map.getFeatureCollection().addFeatures(backup);
        //apply trafo on camera position
        vX1 *= scalefactor;
        vY1 *= scalefactor;
        vX2 *= scalefactor;
        vY2 *= scalefactor;
        vX1 += transX;
        vY1 += transY;
        vX2 += transX;
        vY2 += transY;
        BoundingBox newBB = new BoundingBox(vX1, vY1, vX2, vY2);
        //adjust camera
        map.gotoBoundingBox(newBB, false, true, 0);

    }

    public void actionOverview() {
        map.zoomToFeatureCollection();
    }

    public void actionCalibrate(double measuredDistance) {
        if (measuredDistance != 0d && mainRasterDocumentFeature != null) {
            double scalefactor = calculateScaleFactor(measuredDistance);
            applyScaling(scalefactor);
        }
        map.setInteractionMode(MappingComponent.PAN);
    }

    public Geometry getMainDocumentGeometry() {
        if (mainRasterDocumentFeature != null) {
            return mainRasterDocumentFeature.getGeometry();
        }
        return null;
    }

    public void actionMeasurePolygon() {
        mapListener.setMode(MessenGeometryListener.POLYGON);
        map.setInteractionMode(MY_MESSEN_MODE);
        map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void actionMeasureLine() {
        mapListener.setMode(MessenGeometryListener.LINESTRING);
        map.setInteractionMode(MY_MESSEN_MODE);
        map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void actionPan() {
        map.setInteractionMode(MappingComponent.PAN);
    }

    public void actionZoom() {
        map.setInteractionMode(MappingComponent.ZOOM);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.cismet.cismap.commons.gui.MappingComponent map;
    private javax.swing.JPanel panCenter;
    // End of variables declaration//GEN-END:variables
}
