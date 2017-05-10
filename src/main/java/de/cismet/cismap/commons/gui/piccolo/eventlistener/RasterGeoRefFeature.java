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
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.util.AffineTransformation;

import edu.umd.cs.piccolo.PNode;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.features.ChildNodesProvider;
import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.RequestForHidingHandles;
import de.cismet.cismap.commons.features.RequestForNonreflectingFeature;
import de.cismet.cismap.commons.features.RequestForRotatingPivotLock;
import de.cismet.cismap.commons.features.RequestForUnaddableHandles;
import de.cismet.cismap.commons.features.RequestForUnmoveableHandles;
import de.cismet.cismap.commons.features.RequestForUnremovableHandles;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.georeferencing.PointCoordinatePair;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingHandler;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingHandlerListener;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingWizard;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingWizardListener;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class RasterGeoRefFeature extends DefaultStyledFeature implements XStyledFeature,
    ChildNodesProvider,
    RequestForUnaddableHandles,
    RequestForUnmoveableHandles,
    RequestForUnremovableHandles,
    RequestForRotatingPivotLock,
    RequestForNonreflectingFeature,
    RequestForHidingHandles,
    RasterGeoReferencingHandlerListener,
    RasterGeoReferencingWizardListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final BufferedImage GEOREF_DOT_IMAGE;
    private static final BufferedImage GEOREF_CROSS_IMAGE;
    private static final ImageIcon GEOREF_ICON;

    private static final transient Logger LOG = Logger.getLogger(RasterGeoRefFeature.class);

    static {
        BufferedImage geoRefDotImage = null;
        try {
            geoRefDotImage = ImageIO.read(RasterGeoRefFeature.class.getResource(
                        "/de/cismet/cismap/commons/rasterservice/georeferencing/georef_dot.png"));
        } catch (final IOException ex) {
            LOG.error("could not load the georref_dot image from resources");
        }
        GEOREF_DOT_IMAGE = geoRefDotImage;
        BufferedImage geoRefCrossImage = null;
        try {
            geoRefCrossImage = ImageIO.read(RasterGeoRefFeature.class.getResource(
                        "/de/cismet/cismap/commons/rasterservice/georeferencing/georef_cross.png"));
        } catch (final IOException ex) {
            LOG.error("could not load the georref_cross image from resources");
        }
        GEOREF_CROSS_IMAGE = geoRefCrossImage;
        BufferedImage geoRefIconImage = null;
        try {
            geoRefIconImage = ImageIO.read(RasterGeoRefFeature.class.getResource(
                        "/de/cismet/cismap/commons/rasterservice/georeferencing/georef.png"));
        } catch (final IOException ex) {
            LOG.error("could not load the georref_cross image from resources");
        }
        GEOREF_ICON = new ImageIcon(geoRefIconImage.getScaledInstance(13, 13, java.awt.Image.SCALE_SMOOTH));
    }

    //~ Instance fields --------------------------------------------------------

    @Getter(AccessLevel.PRIVATE)
    private final ArrayList<PNode> children = new ArrayList<>();

    @Getter(AccessLevel.PRIVATE)
    private final RasterGeoReferencingHandler handler;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private boolean refreshing;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RasterGeoRefFeature object.
     *
     * @param  handler  DOCUMENT ME!
     */
    public RasterGeoRefFeature(final RasterGeoReferencingHandler handler) {
        this.handler = handler;
        getHandler().addListener(this);
        RasterGeoReferencingWizard.getInstance().addListener(this);
        updateGeometry();

        CismapBroker.getInstance()
                .getMappingComponent()
                .getFeatureCollection()
                .addFeatureCollectionListener(new FeatureCollectionListener() {

                        boolean ignoreSelection = false;

                        @Override
                        public void featuresAdded(final FeatureCollectionEvent fce) {
                            // if (!isRefreshing() && fce.getEventFeatures().contains(RasterGeoRefFeature.this)) {
                            // CismapBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.GEO_REF);
                            // }
                        }

                        @Override
                        public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
                        }

                        @Override
                        public void featuresRemoved(final FeatureCollectionEvent fce) {
                            // if (!isRefreshing()) { if (fce.getEventFeatures().contains(RasterGeoRefFeature.this)) {
                            // CismapBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.SELECT);
                            // } }
                        }

                        @Override
                        public void featuresChanged(final FeatureCollectionEvent fce) {
                        }

                        @Override
                        public void featureSelectionChanged(final FeatureCollectionEvent fce) {
                            if (fce.getEventFeatures().contains(RasterGeoRefFeature.this)) {
                                if (fce.getFeatureCollection().getSelectedFeatures().contains(
                                        RasterGeoRefFeature.this)) {
                                    if (!ignoreSelection) {
                                        ignoreSelection = true;
                                        try {
                                            RasterGeoReferencingWizard.getInstance().setHandler(handler);
                                            CismapBroker.getInstance()
                                            .getMappingComponent()
                                            .getFeatureCollection()
                                            .select(handler.getFeature());
                                            CismapBroker.getInstance()
                                            .getMappingComponent()
                                            .setInteractionMode(MappingComponent.GEO_REF);
                                        } finally {
                                            ignoreSelection = false;
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
                        }

                        @Override
                        public void featureCollectionChanged() {
                        }
                    });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void updateGeometry() {
        final Rectangle envelope = getHandler().getMetaData().getImageBounds();
        final AffineTransformation transform = getHandler().getMetaData().getTransform();
        if ((envelope != null) && (transform != null)) {
            final Coordinate upperLeftCoordinate = transform.transform(new Coordinate(
                        envelope.getMinX(),
                        envelope.getMinY()),
                    new Coordinate());
            final Coordinate upperRightCoordinate = transform.transform(new Coordinate(
                        envelope.getMaxX(),
                        envelope.getMinY()),
                    new Coordinate());
            final Coordinate lowerRightCoordinate = transform.transform(new Coordinate(
                        envelope.getMaxX(),
                        envelope.getMaxY()),
                    new Coordinate());
            final Coordinate lowerLeftCoordinate = transform.transform(new Coordinate(
                        envelope.getMinX(),
                        envelope.getMaxY()),
                    new Coordinate());

            final Coordinate[] coordinates = new Coordinate[] {
                    upperLeftCoordinate,
                    upperRightCoordinate,
                    lowerRightCoordinate,
                    lowerLeftCoordinate,
                    upperLeftCoordinate
                };
            final GeometryFactory factory = new GeometryFactory(
                    new PrecisionModel(),
                    CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode()));
            final LinearRing linear = factory.createLinearRing(coordinates);
            setGeometry(factory.createPolygon(linear, null));
        }
    }

    @Override
    public ImageIcon getIconImage() {
        return GEOREF_ICON;
    }

    @Override
    public String getType() {
        return "RasterGeoRef";
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
    public String getName() {
        return getHandler().getService().getName();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  parent  DOCUMENT ME!
     */
    private void init(final PFeature parent) {
        final int numOfPairs = getHandler().getPairs().length;
        for (int position = 0; position < numOfPairs; position++) {
            addPointChild(parent, position);
            addCoordinateChild(parent, position);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  parent    DOCUMENT ME!
     * @param  position  pair DOCUMENT ME!
     */
    private void addCoordinateChild(final PFeature parent, final int position) {
        final PointCoordinatePair pair = getHandler().getPair(position);
        if ((pair != null) && (pair.getCoordinate() != null)) {
            final DerivedFixedPImage coordinateDFP = new DerivedFixedPImage(
                    GEOREF_CROSS_IMAGE,
                    parent,
                    new DeriveRule() {

                        @Override
                        public Geometry derive(final Geometry in) {
                            final GeometryFactory factory = new GeometryFactory(
                                    new PrecisionModel(),
                                    CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode()));

                            return factory.createPoint(pair.getCoordinate());
                        }
                    });
            coordinateDFP.setMultiplier(0.25);
            coordinateDFP.setSweetSpotX(0.5);
            coordinateDFP.setSweetSpotY(0.5);
            getChildren().add(coordinateDFP);

            final DerivedFixedPImage textDFP = new DerivedFixedPImage(createImageFromText(
                        Integer.toString(position + 1),
                        30,
                        30),
                    parent,
                    new DeriveRule() {

                        @Override
                        public Geometry derive(final Geometry in) {
                            final GeometryFactory factory = new GeometryFactory(
                                    new PrecisionModel(),
                                    CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode()));

                            final Point center = factory.createPoint(pair.getCoordinate());
                            return center;
                        }
                    });

            textDFP.setSweetSpotX(1);
            textDFP.setSweetSpotY(1);
            getChildren().add(textDFP);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  parent    DOCUMENT ME!
     * @param  position  pair DOCUMENT ME!
     */
    private void addPointChild(final PFeature parent, final int position) {
        final PointCoordinatePair pair = getHandler().getPair(position);
        if ((pair != null) && (pair.getPoint() != null)) {
            final AffineTransformation transform = getHandler().getMetaData().getTransform();
            if (transform != null) {
                final DerivedFixedPImage pointDFP = new DerivedFixedPImage(
                        GEOREF_DOT_IMAGE,
                        parent,
                        new DeriveRule() {

                            @Override
                            public Geometry derive(final Geometry in) {
                                final GeometryFactory factory = new GeometryFactory(
                                        new PrecisionModel(),
                                        CrsTransformer.extractSridFromCrs(
                                            CismapBroker.getInstance().getSrs().getCode()));
                                return transform.transform(
                                        factory.createPoint(
                                            new Coordinate(
                                                pair.getPoint().getX(),
                                                pair.getPoint().getY())));
                            }
                        });
                pointDFP.setMultiplier(0.25);
                pointDFP.setSweetSpotX(0.5);
                pointDFP.setSweetSpotY(0.5);
                getChildren().add(pointDFP);

                final DerivedFixedPImage textDFP = new DerivedFixedPImage(createImageFromText(
                            Integer.toString(position + 1),
                            30,
                            30),
                        parent,
                        new DeriveRule() {

                            @Override
                            public Geometry derive(final Geometry in) {
                                final GeometryFactory factory = new GeometryFactory(
                                        new PrecisionModel(),
                                        CrsTransformer.extractSridFromCrs(
                                            CismapBroker.getInstance().getSrs().getCode()));
                                return transform.transform(
                                        factory.createPoint(
                                            new Coordinate(
                                                pair.getPoint().getX(),
                                                pair.getPoint().getY())));
                            }
                        });
                textDFP.setSweetSpotX(0);
                textDFP.setSweetSpotY(0);
                getChildren().add(textDFP);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   text    DOCUMENT ME!
     * @param   width   DOCUMENT ME!
     * @param   height  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private BufferedImage createImageFromText(final String text, final int width, final int height) {
        final Font font = new Font("Arial", Font.BOLD, 12);

        final BufferedImage image = new BufferedImage(width,
                height,
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = image.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, width, height);
        g2d.setComposite(AlphaComposite.Src);
        g2d.setColor(Color.BLACK);
        g2d.setFont(font);

        final FontMetrics metrics = g2d.getFontMetrics(font);
        g2d.drawString(
            text,
            (float)((width / 2f) - (metrics.stringWidth(text) / 2f)),
            (float)((height / 2f) - (metrics.getHeight() / 2f) + metrics.getAscent()));
        g2d.dispose();

        return image;
    }

    @Override
    public Collection<PNode> provideChildren(final PFeature parent) {
        if (getChildren().isEmpty()) {
            init(parent);
        }
        return getChildren();
    }

    /**
     * DOCUMENT ME!
     */
    private void refresh() {
        getChildren().clear();
        updateGeometry();
        SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        setRefreshing(true);
                        CismapBroker.getInstance()
                                .getMappingComponent()
                                .getFeatureCollection()
                                .removeFeature(RasterGeoRefFeature.this);
                        if (getGeometry() != null) {
                            CismapBroker.getInstance()
                                    .getMappingComponent()
                                    .getFeatureCollection()
                                    .addFeature(RasterGeoRefFeature.this);
                        }
                    } finally {
                        setRefreshing(false);
                    }
                }
            });
    }

    @Override
    public void positionAdded(final int position) {
        refresh();
    }

    @Override
    public void positionRemoved(final int position) {
        refresh();
    }

    @Override
    public void positionChanged(final int position) {
        refresh();
    }

    @Override
    public void transformationChanged() {
        refresh();
    }

    @Override
    public void pointSelected(final int position) {
    }

    @Override
    public void coordinateSelected(final int position) {
    }

    @Override
    public void handlerChanged(final RasterGeoReferencingHandler handler) {
        if ((handler != null) && handler.equals(getHandler())) {
            refresh();
        }
    }
}
