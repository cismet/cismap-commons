package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
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
import java.text.DecimalFormat;
import java.util.Collection;

public class MeasurementMoveListener extends PBasicInputEventHandler {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    public static final String COORDINATES_CHANGED = "COORDINATES_CHANGED";//NOI18N
    
    private MappingComponent mc;
    private float handleX = Float.MIN_VALUE;
    private float handleY = Float.MIN_VALUE;
    private PFeature pf = null;
    private PLocator l = null;
    private MeasurementPHandle measurementPHandle;

    /**
     * Creates a new instance of SimpleMoveListener
     */
    public MeasurementMoveListener(MappingComponent mc) {
        super();
        this.mc = mc;
    }

    @Override
    public void mouseMoved(final PInputEvent event) {
        Runnable t = new Runnable() {

            @Override
            public void run() {
                try {
                    if (mc.getInteractionMode().equals(MappingComponent.LINEMEASUREMENT)) {

                        FeatureCollection fc = mc.getFeatureCollection();
                        Collection<Feature> sel = fc.getSelectedFeatures();

                        if (fc instanceof DefaultFeatureCollection && sel.size() == 1) {

                            if (l == null || measurementPHandle == null) {
                                log.debug("create newPointHandle and Locator");//NOI18N
                                l = new PLocator() {
                                    @Override
                                    public double locateX() {return handleX;}
                                    @Override
                                    public double locateY() {return handleY;}
                                };
                                measurementPHandle = new MeasurementPHandle(l, mc);
                            }

                            Feature[] sels = sel.toArray(new Feature[0]);
                            pf = mc.getPFeatureHM().get(sels[0]);

                            Geometry geom = pf.getFeature().getGeometry();
                            if (geom instanceof LineString) {
                                Point2D trigger = event.getPosition();

                                Point2D[] neighbours = getNearestNeighbours(trigger, pf);
                                Point2D erg = StaticGeometryFunctions.createPointOnLine(neighbours[0], neighbours[1], trigger);

                                handleX = (float) erg.getX();
                                handleY = (float) erg.getY();

                                // measurementPHandle hinzufügen falls noch nicht geschehen
                                boolean found = false;
                                for (Object o : mc.getHandleLayer().getChildrenReference()) {
                                    if (o instanceof MeasurementPHandle && (MeasurementPHandle) o == measurementPHandle) {
                                        found = true;
                                    }
                                }
                                if (!found) {
                                    mc.getHandleLayer().addChild(measurementPHandle);
                                    log.info("tempor\u00E4res Handle eingef\u00FCgt");//NOI18N
                                }

                                measurementPHandle.relocateHandle();

                                LocationIndexedLine lil = new LocationIndexedLine(geom);
                                Coordinate c = new Coordinate(mc.getWtst().getSourceX(handleX), mc.getWtst().getSourceY(handleY));
                                LinearLocation ll = lil.indexOf(c);
                                LengthLocationMap llm = new LengthLocationMap(geom);
                                measurementPHandle.setDistanceInfo(new DecimalFormat("0.00").format(Math.round(llm.getLength(ll) * 100) / 100d));
                            }

                        }
                    }

                    postCoordinateChanged();
                } catch (Exception e) {
                    log.info("Fehler beim Moven \u00FCber die Karte ", e);//NOI18N
                }
            }
        };

        EventQueue.invokeLater(t);
    }

    private Point2D[] getNearestNeighbours(Point2D trigger, PFeature pfeature) {
        Point2D start = null;
        Point2D end = null;
        double dist = Double.POSITIVE_INFINITY;
        if (pfeature.getFeature().getGeometry() instanceof LineString) {
            for (int i = 0; i < pfeature.getXp().length-1; i++) {
                Point2D tmpStart = new Point2D.Double(pfeature.getXp()[i], pfeature.getYp()[i]);
                Point2D tmpEnd = new Point2D.Double(pfeature.getXp()[i+1], pfeature.getYp()[i+1]);
                double tmpDist = StaticGeometryFunctions.distanceToLine(tmpStart, tmpEnd, trigger);
                if (tmpDist < dist) {
                    dist = tmpDist;
                    start = tmpStart;
                    end = tmpEnd;
                }
            }
        }
        Point2D[] erg = {start, end};
        return erg;
    }

    private void postCoordinateChanged() {
        PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(COORDINATES_CHANGED, this);
    }

}
