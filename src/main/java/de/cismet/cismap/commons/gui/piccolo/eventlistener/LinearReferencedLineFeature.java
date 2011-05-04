/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import java.awt.Color;
import java.awt.Stroke;

import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

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

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class LinearReferencedLineFeature extends DefaultStyledFeature implements DrawSelectionFeature, XStyledFeature {

    //~ Static fields/initializers ---------------------------------------------

    public static final Color DEFAULT_COLOR = new Color(255, 91, 0);
    public static final boolean FROM = true;
    public static final boolean TO = false;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            LinearReferencedLineFeature.class);
    private static final ImageIcon ico = new javax.swing.ImageIcon(LinearReferencedLineFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/linRefPointIcon.png")); // NOI18N

    //~ Instance fields --------------------------------------------------------

    private LinearReferencedPointFeature fromFeature;
    private LinearReferencedPointFeature toFeature;
    private Geometry baseLineGeom;
    private FeatureCollectionListener featureCollectionListener;
    private boolean featCollLock = false;
    private StationListener fromStationListener;
    private StationListener toStationListener;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LinearReferencedLineFeature object.
     *
     * @param  fromFeature  DOCUMENT ME!
     * @param  toFeature    DOCUMENT ME!
     */
    public LinearReferencedLineFeature(final LinearReferencedPointFeature fromFeature,
            final LinearReferencedPointFeature toFeature) {
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

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     * @param  isFrom   DOCUMENT ME!
     */
    public final void setPointFeature(final LinearReferencedPointFeature feature, final boolean isFrom) {
        final LinearReferencedPointFeature oldFeature = (isFrom) ? fromFeature : toFeature;
        final StationListener listener = (isFrom) ? fromStationListener : toStationListener;
        if (oldFeature != null) {
            oldFeature.removeListener(listener);
        }
        feature.addListener(listener);
        if (isFrom) {
            this.fromFeature = feature;
        } else {
            this.toFeature = feature;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LinearReferencedPointFeature getStationFeature(final boolean isFrom) {
        return (isFrom) ? fromFeature : toFeature;
    }

    /**
     * DOCUMENT ME!
     */
    private void initFeatureCollectionListener() {
        featureCollectionListener = new FeatureCollectionAdapter() {

                @Override
                public void featureSelectionChanged(final FeatureCollectionEvent fce) {
                    final Collection<Feature> features = fce.getEventFeatures();

                    final FeatureCollection collection = CismapBroker.getInstance()
                                .getMappingComponent()
                                .getFeatureCollection();
                    if (!featCollLock) {
                        featCollLock = true;
                        try {
                            boolean addFeaturesToCollection = false;
                            for (final Feature feature : features) {
                                if (collection.isSelected(feature) && (feature instanceof LinearReferencedLineFeature)
                                            && (((LinearReferencedLineFeature)feature)
                                                == LinearReferencedLineFeature.this)) {
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

        CismapBroker.getInstance()
                .getMappingComponent()
                .getFeatureCollection()
                .addFeatureCollectionListener(featureCollectionListener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   von  DOCUMENT ME!
     * @param   bis  DOCUMENT ME!
     * @param   auf  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry createSubline(final double von, final double bis, final Geometry auf) {
        final LengthIndexedLine lil = new LengthIndexedLine(auf);
        return lil.extractLine(von, bis);
    }

    /**
     * DOCUMENT ME!
     */
    public final void updateGeometry() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("update Geometry");
        }
        Geometry sublineGeom;
        if (fromFeature != toFeature) {
            sublineGeom = createSubline(fromFeature.getCurrentPosition(), toFeature.getCurrentPosition(), baseLineGeom);
        } else {
            sublineGeom = fromFeature.getGeometry();
        }
        setGeometry(sublineGeom);

        final Coordinate[] coordinates = sublineGeom.getCoordinates();

        final float[] xp = new float[coordinates.length];
        final float[] yp = new float[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            xp[i] = (float)coordinates[i].x;
            yp[i] = (float)coordinates[i].y;
        }

        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        final PFeature pFeature = mc.getPFeatureHM().get(this);

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
    public JComponent getInfoComponent(final Refreshable refresh) {
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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class StationListener implements LinearReferencedPointFeatureListener {

        //~ Instance fields ----------------------------------------------------

        private boolean isFrom;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new StationListener object.
         *
         * @param  isFrom  DOCUMENT ME!
         */
        StationListener(final boolean isFrom) {
            this.isFrom = isFrom;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void featureMerged(final LinearReferencedPointFeature mergePoint,
                final LinearReferencedPointFeature withPoint) {
            setPointFeature(withPoint, isFrom);
            updateGeometry();
        }

        @Override
        public void featureMoved(final LinearReferencedPointFeature pointFeature) {
            updateGeometry();
        }
    }
}
