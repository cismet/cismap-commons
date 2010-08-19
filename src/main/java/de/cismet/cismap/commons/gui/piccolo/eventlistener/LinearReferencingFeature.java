package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.linearref.LengthLocationMap;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.math.geometry.StaticGeometryFunctions;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author jruiz
 */
public class LinearReferencingFeature extends PureNewFeature implements AdditionalGeometriesFeature, SelfManipulatingFeature {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    public static final String PROPERTY_FEATURE_COORDINATE = "featureCoordinate";

    private Geometry lineGeom;
    private LinkedList<PropertyChangeListener> listeners = new LinkedList<PropertyChangeListener>();

    public LinearReferencingFeature(Point2D point, WorldToScreenTransform wtst, Geometry lineGeom) {
        super(point, wtst);
        setGeometryType(geomTypes.POINT);
        setEditable(true);        

        this.lineGeom = lineGeom;
    }

    @Override
    public Collection<Geometry> getAdditionalGeometries() {
        Collection<Geometry> coll = new ArrayList<Geometry>(1);
        coll.add(lineGeom);
        return coll;
    }

    @Override
    public String getType() {
        return "station";
    }

    public void addListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    private Coordinate getNearestCoordninateOnLine(Coordinate coord) {
        Coordinate[] neighbours = getNearestNeighbours(coord);
        Point2D point = StaticGeometryFunctions.createPointOnLine(
                new Point2D.Double(neighbours[0].x, neighbours[0].y),
                new Point2D.Double(neighbours[1].x, neighbours[1].y),
                new Point2D.Double(coord.x, coord.y));
        return new Coordinate(point.getX(), point.getY());
    }

    @Override
    public void moveTo(Coordinate coordinate) {
        Coordinate manipulatedCoordinate = getNearestCoordninateOnLine(coordinate);

        MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        PFeature pFeature = mc.getPFeatureHM().get(this);
        Coordinate oldCoord = pFeature.getCoordArr()[0];

        float[] xp = new float[] { (float) manipulatedCoordinate.x };
        float[] yp = new float[] { (float) manipulatedCoordinate.y };

        pFeature.setCoordArr(new Coordinate[] { (Coordinate) manipulatedCoordinate.clone() } );
        pFeature.setPathToPolyline(xp, yp);
        pFeature.syncGeometry();
        pFeature.resetInfoNodePosition();

        for(PropertyChangeListener listener : listeners) {
            listener.propertyChange(new PropertyChangeEvent(this, PROPERTY_FEATURE_COORDINATE, oldCoord, manipulatedCoordinate));
        }
    }

    public double getCurrentPosition() {
        Coordinate coord = getGeometry().getCoordinate();
        double cursorX = coord.x;
        double cursorY = coord.y;

        if (lineGeom != null) {
            LocationIndexedLine lil = new LocationIndexedLine(lineGeom);
            Coordinate c = new Coordinate(cursorX, cursorY);
            LinearLocation ll = lil.indexOf(c);
            LengthLocationMap llm = new LengthLocationMap(lineGeom);
            return llm.getLength(ll);
        } else {
            return 0d;
        }
    }

    private Coordinate[] getNearestNeighbours(Coordinate coord) {
        Coordinate start = null;
        Coordinate end = null;
        double dist = Double.POSITIVE_INFINITY;
        Coordinate[] coords = lineGeom.getCoordinates();
        for (int i = 0; i < coords.length - 1; i++) {
            Coordinate tmpStart = coords[i];
            Coordinate tmpEnd = coords[i + 1];
            double tmpDist = StaticGeometryFunctions.distanceToLine(
                    new Point2D.Double(tmpStart.x, tmpStart.y),
                    new Point2D.Double(tmpEnd.x, tmpEnd.y),
                    new Point2D.Double(coord.x, coord.y));
            if (tmpDist < dist) {
                dist = tmpDist;
                start = tmpStart;
                end = tmpEnd;
            }
        }
        return new Coordinate[] {start, end};
    }

    public static double getPositionOnLine(Point point, Geometry linestringOrMultilinestring) {
        Coordinate pointCoord = point.getCoordinate();

        LocationIndexedLine lineLIL = new LocationIndexedLine(linestringOrMultilinestring);
        LengthLocationMap lineLLM = new LengthLocationMap(linestringOrMultilinestring);
        LinearLocation pointLL = lineLIL.indexOf(pointCoord);
        double pointPosition = lineLLM.getLength(pointLL);

        return pointPosition;
    }

    public static Coordinate getCoordinateOnLine(double position, Geometry linestringOrMultilinestring) {
        LengthIndexedLine lil = new LengthIndexedLine(linestringOrMultilinestring);
        Coordinate coordinate = lil.extractPoint(position);

        return coordinate;
    }

}
