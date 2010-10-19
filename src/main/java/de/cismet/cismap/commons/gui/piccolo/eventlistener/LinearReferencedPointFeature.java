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
import de.cismet.cismap.commons.Refreshable;
import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.math.geometry.StaticGeometryFunctions;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 *
 * @author jruiz
 */
public class LinearReferencedPointFeature extends DefaultStyledFeature implements XStyledFeature, SelfManipulatingFeature {

    private final static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(LinearReferencedPointFeature.class);

    public static final String PROPERTY_FEATURE_COORDINATE = "featureCoordinate";

    private Geometry baseLineGeom;
    private Collection<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    private Collection<Feature> subFeatures = new ArrayList<Feature>(1);
    private ImageIcon ico = new javax.swing.ImageIcon(LinearReferencedPointFeature.class.getResource("/de/cismet/cismap/commons/gui/res/linRefPointIcon.png"));//NOI18N
    private ImageIcon annotationIco = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/linRefPoint.png"));//NOI18N
    private ImageIcon annotationSelectedIco = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/linRefPointSelected.png"));//NOI18N

    public LinearReferencedPointFeature(final double value, final Geometry baseLineGeom) {
        this(value, baseLineGeom, true);
    }

    public void setIconImage(ImageIcon ico) {
        this.ico = ico;

        MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        PFeature pFeature = mc.getPFeatureHM().get(this);
        if (pFeature != null) {
            pFeature.refresh();
        }
    }

    public LinearReferencedPointFeature(final double value, final Geometry baseLineGeom, boolean showSubLine) {
        this.baseLineGeom = baseLineGeom;

        setGeometry(new GeometryFactory().createPoint(getCoordinateOnLine(value, baseLineGeom)));
        setEditable(true);
        setPointAnnotationSymbol(FeatureAnnotationSymbol.newCenteredFeatureAnnotationSymbol(annotationIco.getImage(), annotationSelectedIco.getImage()));
    }

    public Geometry getLineGeometry() {
        return baseLineGeom;
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
        performMove(manipulatedCoordinate);
    }

    private void performMove(Coordinate coordinate) {
        MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        PFeature pFeature = mc.getPFeatureHM().get(this);

        if (pFeature != null) {
            Coordinate oldCoord = pFeature.getCoordArr()[0];

            float[] xp = new float[] { (float) coordinate.x };
            float[] yp = new float[] { (float) coordinate.y };

            pFeature.setCoordArr(new Coordinate[] { (Coordinate) coordinate.clone() } );
            pFeature.setPathToPolyline(xp, yp);
            pFeature.syncGeometry();
            pFeature.resetInfoNodePosition();
            pFeature.visualize();

            for(PropertyChangeListener listener : listeners) {
                listener.propertyChange(new PropertyChangeEvent(this, PROPERTY_FEATURE_COORDINATE, oldCoord, coordinate));
            }
        }
    }

    public void moveToPosition(double position) {
        Coordinate coordinate = getCoordinateOnLine(position, baseLineGeom);
        performMove(coordinate);
    }

    public double getCurrentPosition() {
        Coordinate coord = getGeometry().getCoordinate();
        double cursorX = coord.x;
        double cursorY = coord.y;

        if (baseLineGeom != null) {
            LocationIndexedLine lil = new LocationIndexedLine(baseLineGeom);
            Coordinate c = new Coordinate(cursorX, cursorY);
            LinearLocation ll = lil.indexOf(c);
            LengthLocationMap llm = new LengthLocationMap(baseLineGeom);
            return llm.getLength(ll);
        } else {
            return 0d;
        }
    }

    /*
     * Sucht die Koordinaten der 2 nächsten Punkten der Linie von der
     * Koordinate eines bestimmten Punktes aus.
     */
    private Coordinate[] getNearestNeighbours(Coordinate coord) {
        Coordinate start = null;
        Coordinate end = null;
        double dist = Double.POSITIVE_INFINITY;

        // Kreisgeometrie errechnen um die Suche nach den nächsten Nachbarpunkten
        // auf einer Teillinie einzuschränken, statt auf der gesamten Linie.
        GeometricShapeFactory gsf = new GeometricShapeFactory();
        // Zentrum auf der Koordinate von der aus gesucht werden soll.
        gsf.setCentre(coord);
        // Umfang des Kreises = doppelter Abstand zur jetzigen Koordinate des Punktes
        gsf.setSize(coord.distance(getGeometry().getCoordinate()) * 2);
        Geometry circleGeom = gsf.createCircle();

        // Teillinie aus Verschnitt mit dem Kreis erstellen
        Geometry cuttedGeom = baseLineGeom.intersection(circleGeom);

        // Suche auf Teillinie
        Coordinate[] coords = cuttedGeom.getCoordinates();
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
    public JComponent getInfoComponent(Refreshable refresh) {
        return null;
    }

    @Override
    public Stroke getLineStyle() {
        return null;
    }

}
