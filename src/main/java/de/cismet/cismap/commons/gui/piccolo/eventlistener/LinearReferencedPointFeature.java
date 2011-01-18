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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.linearref.LengthLocationMap;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import java.awt.Stroke;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

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

        setGeometry(new GeometryFactory().createPoint(getCoordinateOnLine(value, baseLineGeom)));
        setEditable(true);
        setPointAnnotationSymbol(FeatureAnnotationSymbol.newCenteredFeatureAnnotationSymbol(
                annotationIco.getImage(),
                annotationSelectedIco.getImage()));
    }

    //~ Methods ----------------------------------------------------------------

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
     * @param   coord  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Coordinate getNearestCoordninateOnLine(final Coordinate coord) {
        final Coordinate[] neighbours = getNearestNeighbours(coord);
        final Point2D point = StaticGeometryFunctions.createPointOnLine(
                new Point2D.Double(neighbours[0].x, neighbours[0].y),
                new Point2D.Double(neighbours[1].x, neighbours[1].y),
                new Point2D.Double(coord.x, coord.y));
        return new Coordinate(point.getX(), point.getY());
    }

    @Override
    public void moveTo(final Coordinate coordinate) {
        final Coordinate manipulatedCoordinate = getNearestCoordninateOnLine(coordinate);
        performMove(manipulatedCoordinate);
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
            final float[] xp = new float[] { (float)coordinate.x };
            final float[] yp = new float[] { (float)coordinate.y };

            pFeature.setCoordArr(new Coordinate[] { (Coordinate)coordinate.clone() });
            pFeature.setPathToPolyline(xp, yp);
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
     * @param   coord  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Coordinate[] getNearestNeighbours(final Coordinate coord) {
        Coordinate start = null;
        Coordinate end = null;
        double dist = Double.POSITIVE_INFINITY;

        // Kreisgeometrie errechnen um die Suche nach den nächsten Nachbarpunkten
        // auf einer Teillinie einzuschränken, statt auf der gesamten Linie.
        final GeometricShapeFactory gsf = new GeometricShapeFactory();
        // Zentrum auf der Koordinate von der aus gesucht werden soll.
        gsf.setCentre(coord);
        // Umfang des Kreises = doppelter Abstand zur jetzigen Koordinate des Punktes
        gsf.setSize(coord.distance(getGeometry().getCoordinate()) * 2);
        final Geometry circleGeom = gsf.createCircle();

        // Teillinie aus Verschnitt mit dem Kreis erstellen
        final Geometry cuttedGeom = baseLineGeom.intersection(circleGeom);

        // Suche auf Teillinie
        final Coordinate[] coords = cuttedGeom.getCoordinates();
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
        return new Coordinate[] { start, end };
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
    public static Coordinate getCoordinateOnLine(final double position, final Geometry linestringOrMultilinestring) {
        final LengthIndexedLine lil = new LengthIndexedLine(linestringOrMultilinestring);
        final Coordinate coordinate = lil.extractPoint(position);
        return coordinate;
    }

    @Override
    public ImageIcon getIconImage() {
        return ico;
    }

    @Override
    public String getName() {
        return "Station";
    }

    @Override
    public String getType() {
        return "Station";
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
