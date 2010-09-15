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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 *
 * @author jruiz
 */
public class LinearReferencedLineFeature extends DefaultStyledFeature implements  DrawSelectionFeature, /*FeatureGroup,*/ XStyledFeature {

    public final static Color DEFAULT_COLOR = new Color(255, 91, 0);

    private final static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(LinearReferencedLineFeature.class);
    private final static ImageIcon ico = new javax.swing.ImageIcon(LinearReferencedLineFeature.class.getResource("/de/cismet/cismap/commons/gui/res/linRefPointIcon.png"));//NOI18N

    private LinearReferencedPointFeature fromFeature;
    private LinearReferencedPointFeature toFeature;
    private Geometry baseLineGeom;
    private FeatureCollectionListener featureCollectionListener;
    private boolean featCollLock = false;

    public LinearReferencedLineFeature(final LinearReferencedPointFeature from, final LinearReferencedPointFeature to) {
        this.fromFeature = from;
        this.toFeature = to;

        this.baseLineGeom = from.getLineGeometry();

        setLineWidth(4);
        setLinePaint(DEFAULT_COLOR);

        from.addListener(new StationListener());
        to.addListener(new StationListener());

        initFeatureCollectionListener();

        updateGeometry();
    }

    private void initFeatureCollectionListener() {
        featureCollectionListener = new FeatureCollectionAdapter() {

            @Override
            public void featureSelectionChanged(FeatureCollectionEvent fce) {
                Collection<Feature> features = fce.getEventFeatures();

                if (!featCollLock) {
                    featCollLock = true;
                    boolean addFeaturesToCollection = false;
                    for (Feature feature : features) {
                        if (feature instanceof LinearReferencedLineFeature && ((LinearReferencedLineFeature) feature) == LinearReferencedLineFeature.this) {
                            addFeaturesToCollection = true;
                        }
                    }
                    if (addFeaturesToCollection) {
                        features.remove(LinearReferencedLineFeature.this);
                        features.add(fromFeature);
                        features.add(toFeature);
                        FeatureCollection collection = CismapBroker.getInstance().getMappingComponent().getFeatureCollection();
                        collection.select(features);
                    }
                    featCollLock = false;
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
        Geometry sublineGeom = createSubline(fromFeature.getCurrentPosition(), toFeature.getCurrentPosition(), baseLineGeom);
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
            pFeature.resetInfoNodePosition();
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

    class StationListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            updateGeometry();
        }

    }
}
