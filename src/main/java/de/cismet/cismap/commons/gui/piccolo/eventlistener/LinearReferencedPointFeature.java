/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.linearref.LengthLocationMap;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import edu.umd.cs.piccolo.util.PDimension;

import java.awt.Stroke;
import java.awt.geom.Point2D;

import java.text.Format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.math.geometry.StaticGeometryFunctions;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class LinearReferencedPointFeature extends DefaultStyledFeature implements XStyledFeature,
    SelfManipulatingFeature {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            LinearReferencedPointFeature.class);

    public static final String PROPERTY_FEATURE_COORDINATE = "featureCoordinate";

    //~ Instance fields --------------------------------------------------------

    private Geometry baseLineGeom;
    private Collection<LinearReferencedPointFeatureListener> listeners =
        new ArrayList<LinearReferencedPointFeatureListener>();
    private ImageIcon ico = new javax.swing.ImageIcon(LinearReferencedPointFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/linRefPointIcon.png"));     // NOI18N
    private ImageIcon annotationIco = new javax.swing.ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/res/linRefPoint.png"));         // NOI18N
    private ImageIcon annotationSelectedIco = new javax.swing.ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/res/linRefPointSelected.png")); // NOI18N

    private Format infoFormat;
    private boolean isMovable = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LinearReferencedPointFeature object.
     *
     * @param  value         DOCUMENT ME!
     * @param  baseLineGeom  DOCUMENT ME!
     */
    public LinearReferencedPointFeature(final double value, final Geometry baseLineGeom) {
        this(value, baseLineGeom, true);
    }

    /**
     * Creates a new LinearReferencedPointFeature object.
     *
     * @param  value         DOCUMENT ME!
     * @param  baseLineGeom  DOCUMENT ME!
     * @param  showSubLine   DOCUMENT ME!
     */
    public LinearReferencedPointFeature(final double value, final Geometry baseLineGeom, final boolean showSubLine) {
        this.baseLineGeom = baseLineGeom;
        setGeometry(getPointOnLine(value, baseLineGeom));
        setPointAnnotationSymbol(FeatureAnnotationSymbol.newCenteredFeatureAnnotationSymbol(
                annotationIco.getImage(),
                annotationSelectedIco.getImage()));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  isMovable  DOCUMENT ME!
     */
    public void setMovable(final boolean isMovable) {
        this.isMovable = isMovable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isMovable() {
        return isMovable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  infoFormat  DOCUMENT ME!
     */
    public void setInfoFormat(final Format infoFormat) {
        this.infoFormat = infoFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Format getInfoFormat() {
        return infoFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ico  DOCUMENT ME!
     */
    public void setIconImage(final ImageIcon ico) {
        this.ico = ico;

        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        final PFeature pFeature = mc.getPFeatureHM().get(this);
        if (pFeature != null) {
            pFeature.refresh();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry getLineGeometry() {
        return baseLineGeom;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void addListener(final LinearReferencedPointFeatureListener listener) {
        listeners.add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void removeListener(final LinearReferencedPointFeatureListener listener) {
        listeners.remove(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   coord     DOCUMENT ME!
     * @param   lineGeom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Coordinate getNearestCoordninateOnLine(final Coordinate coord, final Geometry lineGeom) {
        final Coordinate[] neighbours = getNearestNeighbours(coord, lineGeom);
        if (neighbours != null) {
            final Point2D point = StaticGeometryFunctions.createPointOnLine(
                    new Point2D.Double(neighbours[0].x, neighbours[0].y),
                    new Point2D.Double(neighbours[1].x, neighbours[1].y),
                    new Point2D.Double(coord.x, coord.y));
            return new Coordinate(point.getX(), point.getY());
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   coord     DOCUMENT ME!
     * @param   lineGeom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static double getDistanceOfCoordToLine(final Coordinate coord, final Geometry lineGeom) {
        final Coordinate[] neighbours = getNearestNeighbours(coord, lineGeom);
        if (neighbours != null) {
            final double distance = StaticGeometryFunctions.distanceToLine(
                    new Point2D.Double(neighbours[0].x, neighbours[0].y),
                    new Point2D.Double(neighbours[1].x, neighbours[1].y),
                    new Point2D.Double(coord.x, coord.y));
            return distance;
        } else {
            return -1d;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  coordinate  DOCUMENT ME!
     * @param  delta       DOCUMENT ME!
     */
    @Override
    public void moveTo(final Coordinate coordinate, final PDimension delta) {
        if (isMovable()) {
//        // mauskoordinaten ins selbe coordsys umwandeln wie das der route
//            coordinate = transformToRouteSrid(coordinate);

            final Geometry cuttedLineGeom = getReducedLineGeometry(
                    baseLineGeom,
                    getGeometry().getCoordinate(),
                    coordinate);
            final Coordinate manipulatedCoordinate = getNearestCoordninateOnLine(coordinate, cuttedLineGeom);
            if (manipulatedCoordinate != null) {
                performMove(manipulatedCoordinate);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lineGeom        DOCUMENT ME!
     * @param   lastCoordinate  DOCUMENT ME!
     * @param   newCoordinate   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry getReducedLineGeometry(final Geometry lineGeom,
            final Coordinate lastCoordinate,
            final Coordinate newCoordinate) {
        // Kreisgeometrie errechnen um die Suche nach den nächsten Nachbarpunkten
        // auf einer Teillinie einzuschränken, statt auf der gesamten Linie.
        final GeometricShapeFactory gsf = new GeometricShapeFactory();
        // Zentrum auf der Koordinate von der aus gesucht werden soll.
        gsf.setCentre(newCoordinate);
        // Umfang des Kreises = doppelter Abstand zur jetzigen Koordinate des Punktes
        gsf.setSize(newCoordinate.distance(lastCoordinate) * 2);
        final Geometry circleGeom = gsf.createCircle();

        // Teillinie aus Verschnitt mit dem Kreis erstellen
        return lineGeom.intersection(circleGeom);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   coord  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Coordinate transformToRouteSrid(final Coordinate coord) {
        try {
            final CrsTransformer crsT = new CrsTransformer(CrsTransformer.createCrsFromSrid(
                        getLineGeometry().getSRID()));

            final CoordinateSequence coordSeq = new CoordinateArraySequence(new Coordinate[] { coord });
            final Point point = new Point(coordSeq, getLineGeometry().getFactory());
            final Point transformedPoint = crsT.transformGeometry(point, CismapBroker.getInstance().getSrs().getCode());
            return transformedPoint.getCoordinate();
        } catch (Exception ex) {
            LOG.error("Fehler beim Umrechnen des CRS", ex);
        }
        return coord;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  coordinate  DOCUMENT ME!
     */
    private void performMove(final Coordinate coordinate) {
        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        final PFeature pFeature = mc.getPFeatureHM().get(this);

        if (pFeature != null) {
            pFeature.setCoordArr(0, 0, new Coordinate[] { (Coordinate)coordinate.clone() });
            pFeature.updatePath();
            pFeature.syncGeometry();
            pFeature.resetInfoNodePosition();
            pFeature.visualize();
            fireFeatureMoved();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void fireFeatureMoved() {
        final Collection<LinearReferencedPointFeatureListener> listenersCopy =
            new CopyOnWriteArrayList<LinearReferencedPointFeatureListener>(listeners);
        for (final LinearReferencedPointFeatureListener listener : listenersCopy) {
            listener.featureMoved(this);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mergePoint  DOCUMENT ME!
     * @param  withPoint   DOCUMENT ME!
     */
    private void fireFeatureMerged(final LinearReferencedPointFeature mergePoint,
            final LinearReferencedPointFeature withPoint) {
        final Collection<LinearReferencedPointFeatureListener> listenersCopy =
            new CopyOnWriteArrayList<LinearReferencedPointFeatureListener>(listeners);
        for (final LinearReferencedPointFeatureListener listener : listenersCopy) {
            listener.featureMerged(mergePoint, withPoint);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  position  DOCUMENT ME!
     */
    public void moveToPosition(final double position) {
        final Coordinate coordinate = getCoordinateOnLine(position, baseLineGeom);
        performMove(coordinate);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getCurrentPosition() {
        final Coordinate coord = getGeometry().getCoordinate();
        final double cursorX = coord.x;
        final double cursorY = coord.y;

        if (baseLineGeom != null) {
            final LocationIndexedLine lil = new LocationIndexedLine(baseLineGeom);
            final Coordinate c = new Coordinate(cursorX, cursorY);
            final LinearLocation ll = lil.indexOf(c);
            final LengthLocationMap llm = new LengthLocationMap(baseLineGeom);
            return llm.getLength(ll);
        } else {
            return 0d;
        }
    }
    /**
     * Sucht die Koordinaten der 2 nächsten Punkten der Linie von der Koordinate eines bestimmten Punktes aus.
     *
     * @param   coord     DOCUMENT ME!
     * @param   lineGeom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Coordinate[] getNearestNeighbours(final Coordinate coord, final Geometry lineGeom) {
        Coordinate start = null;
        Coordinate end = null;
        double dist = Double.POSITIVE_INFINITY;

        // Suche auf Teillinie
        final Coordinate[] coords = lineGeom.getCoordinates();
        for (int i = 0; i < (coords.length - 1); i++) {
            final Coordinate tmpStart = coords[i];
            final Coordinate tmpEnd = coords[i + 1];
            final double tmpDist = StaticGeometryFunctions.distanceToLine(
                    new Point2D.Double(tmpStart.x, tmpStart.y),
                    new Point2D.Double(tmpEnd.x, tmpEnd.y),
                    new Point2D.Double(coord.x, coord.y));
            if (tmpDist < dist) {
                dist = tmpDist;
                start = tmpStart;
                end = tmpEnd;
            }
        }
        if ((start != null) && (end != null)) {
            return new Coordinate[] { start, end };
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pointCoord                   DOCUMENT ME!
     * @param   linestringOrMultilinestring  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static double getPositionOnLine(final Coordinate pointCoord, final Geometry linestringOrMultilinestring) {
        final LocationIndexedLine lineLIL = new LocationIndexedLine(linestringOrMultilinestring);
        final LengthLocationMap lineLLM = new LengthLocationMap(linestringOrMultilinestring);
        final LinearLocation pointLL = lineLIL.indexOf(pointCoord);
        final double pointPosition = lineLLM.getLength(pointLL);

        return pointPosition;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position                     DOCUMENT ME!
     * @param   linestringOrMultilinestring  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static Coordinate getCoordinateOnLine(final double position, final Geometry linestringOrMultilinestring) {
        final LengthIndexedLine lil = new LengthIndexedLine(linestringOrMultilinestring);
        final Coordinate coordinate = lil.extractPoint(position);
        return coordinate;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position                     DOCUMENT ME!
     * @param   linestringOrMultilinestring  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry getPointOnLine(final double position, final Geometry linestringOrMultilinestring) {
        final Coordinate coordinate = getCoordinateOnLine(position, linestringOrMultilinestring);
        return new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), linestringOrMultilinestring.getSRID())
                    .createPoint(coordinate);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public ImageIcon getIconImage() {
        return ico;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getName() {
        return "Station";
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getType() {
        return "Station";
    }

    /**
     * DOCUMENT ME!
     *
     * @param   refresh  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public JComponent getInfoComponent(final Refreshable refresh) {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Stroke getLineStyle() {
        return null;
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void moveFinished() {
        final LinearReferencedPointFeature snappingPoint = getSnappingPoint();
        if (snappingPoint != null) {
            fireFeatureMerged(this, snappingPoint);
            snappingPoint.fireFeatureMerged(this, snappingPoint);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LinearReferencedPointFeature getSnappingPoint() {
        final boolean snapping = CismapBroker.getInstance().getMappingComponent().isSnappingEnabled();
        if (snapping) {
            final FeatureCollection fc = CismapBroker.getInstance().getMappingComponent().getFeatureCollection();
            final Feature[] features = fc.getAllFeatures().toArray(new Feature[0]); // nicht die originalcollection,
                                                                                    // weil diese sich in der
                                                                                    // schleife verändern kann
            final LinearReferencedPointFeature mergePoint = this;
            for (final Feature feature : features) {
                if ((feature instanceof LinearReferencedPointFeature) && (feature != mergePoint)) {
                    final LinearReferencedPointFeature withPoint = (LinearReferencedPointFeature)feature;
                    final boolean isInSnappingDistance = Math.abs(withPoint.getCurrentPosition()
                                    - mergePoint.getCurrentPosition())
                                < (0.002 * CismapBroker.getInstance().getMappingComponent().getScaleDenominator());
                    if (isInSnappingDistance) {
                        return withPoint;
                    }
                }
            }
        }
        return null;
    }
}
