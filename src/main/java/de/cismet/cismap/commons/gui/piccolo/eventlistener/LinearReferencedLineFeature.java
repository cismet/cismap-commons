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
import java.awt.Paint;
import java.awt.Stroke;

import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.features.*;
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
    private LinearReferencedPointFeatureHandler fromPointListener;
    private LinearReferencedPointFeatureHandler toPointListener;

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

        fromPointListener = new LinearReferencedPointFeatureHandler(FROM);
        toPointListener = new LinearReferencedPointFeatureHandler(TO);

        initFeatureCollectionListener();

        setPointFeature(fromFeature, FROM);
        setPointFeature(toFeature, TO);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  pointFeature  DOCUMENT ME!
     * @param  isFrom        DOCUMENT ME!
     */
    public final void setPointFeature(final LinearReferencedPointFeature pointFeature, final boolean isFrom) {
        final LinearReferencedPointFeature oldFeature = (isFrom) ? fromFeature : toFeature;
        final LinearReferencedPointFeatureHandler listener = (isFrom) ? fromPointListener : toPointListener;
        if (oldFeature != null) {
            oldFeature.removeListener(listener);
        }
        if (pointFeature != null) {
            pointFeature.addListener(listener);
        }
        if (isFrom) {
            this.fromFeature = pointFeature;
        } else {
            this.toFeature = pointFeature;
        }

        updateGeometry();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isFrom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LinearReferencedPointFeature getPointFeature(final boolean isFrom) {
        return (isFrom) ? fromFeature : toFeature;
    }

    @Override
    public void setLinePaint(final Paint linePaint) {
        super.setLinePaint(linePaint);
        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        final PFeature pFeature = mc.getPFeatureHM().get(this);
        if (pFeature != null) {
            pFeature.visualize();
        }
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
                            if (features != null) {
                                for (final Feature feature : features) {
                                    if (collection.isSelected(feature)
                                                && (feature instanceof LinearReferencedLineFeature)
                                                && (((LinearReferencedLineFeature)feature)
                                                    == LinearReferencedLineFeature.this)) {
                                        addFeaturesToCollection = true;
                                    }
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
        if ((fromFeature != null) && (toFeature != null)) {
            Geometry sublineGeom;
            if (fromFeature != toFeature) {
                sublineGeom = createSubline(fromFeature.getCurrentPosition(),
                        toFeature.getCurrentPosition(),
                        baseLineGeom);
            } else {
                sublineGeom = fromFeature.getGeometry();
            }
            setGeometry(sublineGeom);

            final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
            final PFeature pFeature = mc.getPFeatureHM().get(this);

            if (pFeature != null) {
                pFeature.setCoordArr(0, 0, sublineGeom.getCoordinates());
                pFeature.updatePath();
                pFeature.syncGeometry();
                pFeature.visualize();
            }
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
    class LinearReferencedPointFeatureHandler implements LinearReferencedPointFeatureListener {

        //~ Instance fields ----------------------------------------------------

        private boolean isFrom;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LinearReferencedPointFeatureHandler object.
         *
         * @param  isFrom  DOCUMENT ME!
         */
        LinearReferencedPointFeatureHandler(final boolean isFrom) {
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
