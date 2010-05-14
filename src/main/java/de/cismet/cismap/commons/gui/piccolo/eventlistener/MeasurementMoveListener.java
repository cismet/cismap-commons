package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.linearref.LengthLocationMap;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.MeasurementPHandle;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.math.geometry.StaticGeometryFunctions;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import edu.umd.cs.piccolox.util.PLocator;
import java.awt.EventQueue;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Vector;

public class MeasurementMoveListener extends PBasicInputEventHandler {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public static final String COORDINATES_CHANGED = "COORDINATES_CHANGED";//NOI18N
    private MappingComponent mc;
    private float handleX = Float.MIN_VALUE;
    private float handleY = Float.MIN_VALUE;
    private PFeature pf = null;
    private MeasurementPHandle measurementPHandle;

    private Geometry geom;
    private Vector<Mark> marks = new Vector<Mark>();

    /**
     * Creates a new instance of SimpleMoveListener
     */
    public MeasurementMoveListener(MappingComponent mc) {
        super();
        this.mc = mc;

        PLocator l = new PLocator() {

            @Override
            public double locateX() {
                return handleX;
            }

            @Override
            public double locateY() {
                return handleY;
            }
        };
        measurementPHandle = new MeasurementPHandle(l, mc);
    }

    @Override
    public void mouseClicked(PInputEvent event) {
        if (event.isLeftMouseButton()) {
            if (event.isControlDown()) {
                for (Mark mark : marks) {
                    mc.getHandleLayer().removeChild(mark.getHandle());
                }
                marks.removeAllElements();
            } else {
                Point2D trigger = event.getPosition();
                Point2D[] neighbours = getNearestNeighbours(trigger, pf);
                Point2D erg = StaticGeometryFunctions.createPointOnLine(neighbours[0], neighbours[1], trigger);

                addMarkHandle(erg.getX(), erg.getY());
            }
        }
    }

    private double getCurrentPosition() {
        if (geom != null) {
            LocationIndexedLine lil = new LocationIndexedLine(geom);
            Coordinate c = new Coordinate(mc.getWtst().getSourceX(handleX), mc.getWtst().getSourceY(handleY));
            LinearLocation ll = lil.indexOf(c);
            LengthLocationMap llm = new LengthLocationMap(geom);
            return llm.getLength(ll);
        } else {
            return 0d;
        }
    }

    @Override
    public void mouseDragged(PInputEvent event) {
        log.debug("mouse dragged");
    }

    @Override
    public void mouseReleased(PInputEvent event) {
        log.debug("mouse released");
    }

    private void addMarkHandles() {
        for (Mark mark : marks) {
            mc.getHandleLayer().addChild(mark.getHandle());
        }
    }

    private void addMarkHandle(final double x, final double y) {

        mc.getLayer().addChild(measurementPHandle);
        log.debug("create newPointHandle and Locator");//NOI18N
        PLocator l = new PLocator() {

            @Override
            public double locateX() {
                return x;
            }

            @Override
            public double locateY() {
                return y;
            }
        };        
        MeasurementPHandle stationPHandle = new MeasurementPHandle(l, mc);
        double currentPosition = getCurrentPosition();
        stationPHandle.setMarkPosition(currentPosition);
        mc.getHandleLayer().addChild(stationPHandle);

        marks.add(new Mark(currentPosition, stationPHandle));
    }

    @Override
    public void mouseMoved(final PInputEvent event) {
        Runnable t = new Runnable() {

            @Override
            public void run() {
                try {
                    if (mc.getInteractionMode().equals(MappingComponent.LINEMEASUREMENT)) {

                        addMarkHandles();
                        
                        geom = getSelectedGeometry();

                        if (geom != null) {
                            mc.getHandleLayer().addChild(measurementPHandle);

                            if (geom instanceof MultiLineString || geom instanceof LineString) {
                                updateHandleCoords(event.getPosition());
                                
                                measurementPHandle.setMarkPosition(getCurrentPosition());
                            } else {
                                log.debug("Wrong geometrytype:" +geom.getGeometryType());
                            }

                        }

                    }

                    postCoordinateChanged();
                } catch (Exception e) {
                    log.fatal("Fehler beim Moven \u00FCber die Karte ", e);//NOI18N
                }
            }
        };

        EventQueue.invokeLater(t);
    }

    private void updateHandleCoords(Point2D trigger) {
        Point2D[] neighbours = getNearestNeighbours(trigger, pf);
        Point2D erg = StaticGeometryFunctions.createPointOnLine(neighbours[0], neighbours[1], trigger);

        handleX = (float) erg.getX();
        handleY = (float) erg.getY();
    }

    private Geometry getSelectedGeometry() {
        //Collection holen
        FeatureCollection fc = mc.getFeatureCollection();
        //Selektierte Features holen
        Collection<Feature> sel = fc.getSelectedFeatures();

        //wenn genau 1 Objekt selektiert ist
        if (fc instanceof DefaultFeatureCollection && sel.size() == 1) {
            // selektiertes feature holen
            Feature[] sels = sel.toArray(new Feature[0]);
            //zugehöriges pfeature holen
            pf = mc.getPFeatureHM().get(sels[0]);
            //zugehörige geometrie holen
            return pf.getFeature().getGeometry();
         } else {
            return null;
         }
    }

    private Point2D[] getNearestNeighbours(Point2D trigger, PFeature pfeature) {
        Point2D start = null;
        Point2D end = null;
        double dist = Double.POSITIVE_INFINITY;
        if (geom != null || geom instanceof MultiLineString || geom instanceof LineString) {
            for (int i = 0; i < pfeature.getXp().length - 1; i++) {
                Point2D tmpStart = new Point2D.Double(pfeature.getXp()[i], pfeature.getYp()[i]);
                Point2D tmpEnd = new Point2D.Double(pfeature.getXp()[i + 1], pfeature.getYp()[i + 1]);
                double tmpDist = StaticGeometryFunctions.distanceToLine(tmpStart, tmpEnd, trigger);
                if (tmpDist < dist) {
                    dist = tmpDist;
                    start = tmpStart;
                    end = tmpEnd;
                }
            }
        }
        return new Point2D[] {start, end};
    }

    private void postCoordinateChanged() {
        PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(COORDINATES_CHANGED, this);
    }

    class Mark {
        private double position;
        private MeasurementPHandle handle;

        Mark(double position, MeasurementPHandle handle) {
            this.handle = handle;
            this.position = position;
        }

        public double getPosition() {
            return position;
        }

        public MeasurementPHandle getHandle() {
            return handle;
        }
    }

}
