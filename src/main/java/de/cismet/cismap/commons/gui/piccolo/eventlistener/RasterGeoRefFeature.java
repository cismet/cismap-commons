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
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.umd.cs.piccolo.PNode;

import org.apache.log4j.Logger;

import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
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
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.georeferencing.PointCoordinatePair;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingDialog;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingHandler;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingHandlerListener;
import de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingWizard;

import de.cismet.tools.gui.StaticSwingTools;

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
    RasterGeoReferencingHandlerListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final BufferedImage GEOREF_DOT_IMAGE;
    private static final BufferedImage GEOREF_CROSS_IMAGE;

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
    }

    //~ Instance fields --------------------------------------------------------

    private final ArrayList<PNode> children = new ArrayList<>();
    private final RasterGeoReferencingHandler handler;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RasterGeoRefFeature object.
     *
     * @param  handler  DOCUMENT ME!
     */
    public RasterGeoRefFeature(final RasterGeoReferencingHandler handler) {
        this.handler = handler;
        handler.addListener(this);
        updateGeometry();
//        setTransparency(1);

        CismapBroker.getInstance()
                .getMappingComponent()
                .getFeatureCollection()
                .addFeatureCollectionListener(new FeatureCollectionListener() {

                        @Override
                        public void featuresAdded(final FeatureCollectionEvent fce) {
                            if (fce.getEventFeatures().contains(RasterGeoRefFeature.this)) {
                                RasterGeoReferencingWizard.getInstance().setHandler(handler);
                                StaticSwingTools.showDialog(RasterGeoReferencingDialog.getInstance());
                            }
                        }

                        @Override
                        public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
                        }

                        @Override
                        public void featuresRemoved(final FeatureCollectionEvent fce) {
                            if (fce.getEventFeatures().contains(RasterGeoRefFeature.this)) {
                                RasterGeoReferencingDialog.getInstance().setVisible(false);
                            }
                        }

                        @Override
                        public void featuresChanged(final FeatureCollectionEvent fce) {
                        }

                        @Override
                        public void featureSelectionChanged(final FeatureCollectionEvent fce) {
                            if (fce.getEventFeatures().contains(RasterGeoRefFeature.this)) {
                                if (fce.getFeatureCollection().getSelectedFeatures().contains(
                                        RasterGeoRefFeature.this)) {
                                    RasterGeoReferencingWizard.getInstance().setHandler(handler);

                                    if (!RasterGeoReferencingDialog.getInstance().isVisible()) {
                                        StaticSwingTools.showDialog(RasterGeoReferencingDialog.getInstance());
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
        final GeometryFactory factory = new GeometryFactory(
                new PrecisionModel(),
                CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode()));
        final Rectangle envelope = handler.getMetaData().getImageBounds();
        final AffineTransform transform = handler.getMetaData().getTransform();
        if ((envelope != null) && (transform != null)) {
            final Point2D upperLeftPoint = transform.transform(new Point2D.Double(
                        envelope.getMinX(),
                        envelope.getMinY()),
                    null);
            final Point2D upperRightPoint = transform.transform(new Point2D.Double(
                        envelope.getMaxX(),
                        envelope.getMinY()),
                    null);
            final Point2D lowerRightPoint = transform.transform(new Point2D.Double(
                        envelope.getMaxX(),
                        envelope.getMaxY()),
                    null);
            final Point2D lowerLeftPoint = transform.transform(new Point2D.Double(
                        envelope.getMinX(),
                        envelope.getMaxY()),
                    null);

            final Coordinate upperLeftCoordinate = new Coordinate(upperLeftPoint.getX(), upperLeftPoint.getY());
            final Coordinate upperRightCoordinate = new Coordinate(upperRightPoint.getX(), upperRightPoint.getY());
            final Coordinate lowerRightCoordinate = new Coordinate(lowerRightPoint.getX(), lowerRightPoint.getY());
            final Coordinate lowerLeftCoordinate = new Coordinate(lowerLeftPoint.getX(), lowerLeftPoint.getY());
            final Coordinate[] coordinates = new Coordinate[] {
                    upperLeftCoordinate,
                    upperRightCoordinate,
                    lowerRightCoordinate,
                    lowerLeftCoordinate,
                    upperLeftCoordinate
                };
            final LinearRing linear = new GeometryFactory().createLinearRing(coordinates);
            setGeometry(factory.createPolygon(linear, null));
        } else {
            setGeometry(null);
        }
    }

    @Override
    public ImageIcon getIconImage() {
        return null;
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
        return "RasterGeoRef";
    }

    /**
     * DOCUMENT ME!
     *
     * @param  parent  DOCUMENT ME!
     */
    private void init(final PFeature parent) {
        for (final PointCoordinatePair pair : handler.getPairs()) {
            if (pair.getCoordinate() != null) {
                children.add(createCoordinateChild(parent, pair));
            }
            if (pair.getPoint() != null) {
                children.add(createPointChild(parent, pair));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parent  DOCUMENT ME!
     * @param   pair    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private DerivedFixedPImage createCoordinateChild(final PFeature parent, final PointCoordinatePair pair) {
        final DerivedFixedPImage child = new DerivedFixedPImage(GEOREF_DOT_IMAGE, parent, new DeriveRule() {

                    @Override
                    public Geometry derive(final Geometry in) {
                        final GeometryFactory factory = new GeometryFactory(
                                new PrecisionModel(),
                                CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode()));

                        return factory.createPoint(pair.getCoordinate());
                    }
                });
        child.setMultiplier(0.25);
        child.setSweetSpotX(0.5);
        child.setSweetSpotY(0.5);
        return child;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parent  DOCUMENT ME!
     * @param   pair    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private DerivedFixedPImage createPointChild(final PFeature parent, final PointCoordinatePair pair) {
        final DerivedFixedPImage child = new DerivedFixedPImage(GEOREF_CROSS_IMAGE, parent, new DeriveRule() {

                    @Override
                    public Geometry derive(final Geometry in) {
                        final GeometryFactory factory = new GeometryFactory(
                                new PrecisionModel(),
                                CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode()));
                        final Point2D point = pair.getPoint();
                        final AffineTransform transform = handler.getMetaData().getTransform();
                        final Point2D transformedPoint = transform.transform(
                                new Point2D.Double(
                                    point.getX(),
                                    point.getY()),
                                null);

                        return factory.createPoint(new Coordinate(transformedPoint.getX(), transformedPoint.getY()));
                    }
                });
        child.setMultiplier(0.25);
        child.setSweetSpotX(0.5);
        child.setSweetSpotY(0.5);
        return child;
    }

    @Override
    public Collection<PNode> provideChildren(final PFeature parent) {
        if (children.isEmpty()) {
            init(parent);
        }
        return children;
    }

    /**
     * DOCUMENT ME!
     */
    private void refresh() {
        children.clear();
        updateGeometry();
        SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
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
}
