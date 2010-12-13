package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.FeatureCollectionAdapter;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import java.awt.Color;
import java.awt.Stroke;
import java.util.Collection;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 *
 * @author jruiz
 */
public class LinearReferencedLineFeature extends DefaultStyledFeature implements  DrawSelectionFeature, /*FeatureGroup,*/ XStyledFeature {

    public final static Color DEFAULT_COLOR = new Color(255, 91, 0);
    public final static boolean FROM = true;
    public final static boolean TO = false;

    private final static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(LinearReferencedLineFeature.class);
    private final static ImageIcon ico = new javax.swing.ImageIcon(LinearReferencedLineFeature.class.getResource("/de/cismet/cismap/commons/gui/res/linRefPointIcon.png"));//NOI18N

    private LinearReferencedPointFeature fromFeature;
    private LinearReferencedPointFeature toFeature;
    private Geometry baseLineGeom;
    private FeatureCollectionListener featureCollectionListener;
    private boolean featCollLock = false;
    private StationListener fromStationListener;
    private StationListener toStationListener;

    public LinearReferencedLineFeature(final LinearReferencedPointFeature fromFeature, final LinearReferencedPointFeature toFeature) {
        this.baseLineGeom = fromFeature.getLineGeometry();

        setLineWidth(4);
        setLinePaint(DEFAULT_COLOR);
        setPrimaryAnnotationVisible(false);

        fromStationListener = new StationListener(FROM);
        toStationListener = new StationListener(TO);

        setPointFeature(fromFeature, FROM);
        setPointFeature(toFeature, TO);

        initFeatureCollectionListener();

        updateGeometry();
    }

    public final void setPointFeature(LinearReferencedPointFeature feature, boolean isFrom) {
        LinearReferencedPointFeature oldFeature = (isFrom) ? fromFeature : toFeature;
        StationListener listener = (isFrom) ? fromStationListener : toStationListener;
        if (oldFeature != null) {
            oldFeature.removeListener(listener);
        }
        feature.addListener(listener);
        if (isFrom)  {
            this.fromFeature = feature;
        } else {
            this.toFeature = feature;
        }
    }

    public LinearReferencedPointFeature getStationFeature(boolean isFrom) {
        return (isFrom) ? fromFeature : toFeature;
    }

    private void initFeatureCollectionListener() {
        featureCollectionListener = new FeatureCollectionAdapter() {

            @Override
            public void featureSelectionChanged(FeatureCollectionEvent fce) {
                Collection<Feature> features = fce.getEventFeatures();

                FeatureCollection collection = CismapBroker.getInstance().getMappingComponent().getFeatureCollection();
                if (!featCollLock) {
                    featCollLock = true;
                    try {
                        boolean addFeaturesToCollection = false;
                        for (Feature feature : features) {
                            if (collection.isSelected(feature) && feature instanceof LinearReferencedLineFeature && ((LinearReferencedLineFeature) feature) == LinearReferencedLineFeature.this) {
                                addFeaturesToCollection = true;
                            }
                        }
                        if (addFeaturesToCollection) {
                            collection.addToSelection(fromFeature);
                            collection.addToSelection(toFeature);
                        }
                    } finally {
                        featCollLock = false;
                    }
                }
            }
        };

        CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addFeatureCollectionListener(featureCollectionListener);
    }

    private static Geometry createSubline(double von, double bis, Geometry auf) {
        LengthIndexedLine lil = new LengthIndexedLine(auf);
        return lil.extractLine(von, bis);
    }

    public final void updateGeometry() {
        LOG.debug("update Geometry");
        Geometry sublineGeom;
        if (fromFeature != toFeature) {
            sublineGeom = createSubline(fromFeature.getCurrentPosition(), toFeature.getCurrentPosition(), baseLineGeom);
        } else {
            sublineGeom = fromFeature.getGeometry();
        }
        setGeometry(sublineGeom);

        Coordinate[] coordinates = sublineGeom.getCoordinates();

        float[] xp = new float[coordinates.length];
        float[] yp = new float[coordinates.length];        
        for (int i = 0; i < coordinates.length; i++) {
            xp[i] = (float) coordinates[i].x;
            yp[i] = (float) coordinates[i].y;
        }

        MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        PFeature pFeature = mc.getPFeatureHM().get(this);

        if (pFeature != null) {
            pFeature.setCoordArr(coordinates);
            pFeature.setPathToPolyline(xp, yp);
            pFeature.syncGeometry();
            pFeature.visualize();
//            pFeature.resetInfoNodePosition();
        }
    }

    @Override
    public ImageIcon getIconImage() {
        return ico;
    }

    @Override
    public String getName() {
        return "Stationierte Linie";
    }

    @Override
    public String getType() {
        return "Stationierte Linie";
    }

    @Override
    public JComponent getInfoComponent(Refreshable refresh) {
        return null;
    }

    @Override
    public Stroke getLineStyle() {
        return null;
    }

    @Override
    public boolean isDrawingSelection() {
        return false;
    }

    class StationListener implements LinearReferencedPointFeatureListener {

        private boolean isFrom;

        StationListener(boolean isFrom) {
            this.isFrom = isFrom;
        }
        
        @Override
        public void featureMerged(LinearReferencedPointFeature mergePoint, LinearReferencedPointFeature withPoint) {
            setPointFeature(withPoint, isFrom);
            updateGeometry();
        }

        @Override
        public void featureMoved(LinearReferencedPointFeature pointFeature) {
            updateGeometry();
        }

    }
}
