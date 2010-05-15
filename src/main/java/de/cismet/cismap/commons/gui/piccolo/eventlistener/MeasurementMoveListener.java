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
import de.cismet.cismap.commons.gui.piccolo.MarkPHandle;
import de.cismet.cismap.commons.gui.piccolo.MeasurementPHandle;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.math.geometry.StaticGeometryFunctions;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import edu.umd.cs.piccolox.util.PLocator;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class MeasurementMoveListener extends PBasicInputEventHandler {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public static final String COORDINATES_CHANGED = "COORDINATES_CHANGED";//NOI18N
    public static enum modii {
        MARK_SELECTION,
        MARK_ADD,
        MEASUREMENT,
        SUBLINE
    };


    private MappingComponent mc;
    private float handleX = Float.MIN_VALUE;
    private float handleY = Float.MIN_VALUE;
    private PFeature pf = null;
    private MeasurementPHandle measurementPHandle;

    private Geometry geom;
    private Vector<Mark> marks = new Vector<Mark>();

    private modii modus;
    private JPopupMenu menu;
    private Mark selectedMark;

    private JMenuItem cmdRemoveMark;
    private JMenuItem cmdRemoveAllMarks;

    private ImageIcon icoMenRem = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/marker--minus.png"));//NOI18N
    private ImageIcon icoMenRemAll = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/marker--minus.png"));//NOI18N

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

        initContextMenu();
    }

    @Override
    public void mouseClicked(PInputEvent event) {
        if (event.isLeftMouseButton()) {
            switch (modus) {
                case MARK_ADD:
                    Point2D trigger = event.getPosition();
                    Point2D[] neighbours = getNearestNeighbours(trigger, pf);
                    Point2D erg = StaticGeometryFunctions.createPointOnLine(neighbours[0], neighbours[1], trigger);

                    addMarkHandle(erg.getX(), erg.getY());
                    break;
                case SUBLINE:
                    //todo
                    break;
            }
        }
    }

    public double getSelectedMarkPosition() {
        if (selectedMark != null) {
            return selectedMark.getPosition();
        } else {
            return 0;
        }
    }

    public void setSelectedMark(MarkPHandle markPHandle) {
        if (markPHandle == null) {
            this.selectedMark = null;
        } else {
            for (Mark mark : marks) {
                if (mark != null && mark.getPHandle().equals(markPHandle)) {
                    this.selectedMark = mark;
                }
            }
        }
    }

    public JPopupMenu getContextMenu() {
        return menu;
    }

    public void removeContextMenuItem(JMenuItem item) {
        menu.remove(item);
    }

    public void addContextMenuItem(JMenuItem item) {
        menu.remove(cmdRemoveMark);
        menu.remove(cmdRemoveAllMarks);
        menu.add(item);
        menu.add(cmdRemoveMark);
        menu.add(cmdRemoveAllMarks);
    }

    private void initContextMenu() {
        menu = new JPopupMenu();

        cmdRemoveMark = new JMenuItem("entfernen");//NOI18N
        cmdRemoveAllMarks = new JMenuItem("alle entfernen");//NOI18N

        cmdRemoveMark.setIcon(icoMenRem);
        cmdRemoveAllMarks.setIcon(icoMenRemAll);

        cmdRemoveMark.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                removeMark(selectedMark.getPHandle());
            }
        });
        cmdRemoveAllMarks.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                removeAllMarks();
            }
        });

        if (menu.getComponentCount() > 0) {
            menu.addSeparator();
        }
        menu.add(cmdRemoveMark);
        menu.add(cmdRemoveAllMarks);
    }

    public PLayer getPLayer() {
        return mc.getHandleLayer();
    }

    public void removeMark(MarkPHandle markHandle) {
        if (markHandle != null) {
            Mark toRemove = null;
            for (Mark mark : marks) {
                if (mark.getPHandle().equals(markHandle)) {
                    toRemove = mark;
                    break;
                }
            }
            if (toRemove != null) {
                marks.remove(toRemove);
                getPLayer().removeChild(toRemove.getPHandle());
            }
        }
    }

    public void removeAllMarks() {
        for (Mark mark : marks) {
            getPLayer().removeChild(mark.getPHandle());
        }
        marks.removeAllElements();
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

    private void showMarkHandles(boolean show) {
        for (Mark mark : marks) {
            showHandle(mark.getPHandle(), show);
        }
    }

    private void addMarkHandle(final double x, final double y) {
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
        MarkPHandle markHandle = new MarkPHandle(l, this, mc);
        double currentPosition = getCurrentPosition();
        markHandle.setMarkPosition(currentPosition);

        marks.add(new Mark(currentPosition, markHandle));
        getPLayer().removeChild(measurementPHandle);
        getPLayer().addChild(markHandle);
        getPLayer().addChild(measurementPHandle);
    }

    private void showMeasurementHandle(boolean show) {
        log.debug("showMeasurementHandle " + show);
        showHandle(measurementPHandle, show);
    }

    private void showHandle(PPath handle, boolean show) {
        boolean found = false;
        for (Object o : getPLayer().getChildrenReference()) {
            if (o != null && o.equals(handle)) {
                found = true;
                break;
            }
        }
        if (!found && show) {
            getPLayer().addChild(handle);
        }
        if (found && !show) {
            getPLayer().removeChild(handle);
        }
    }

    private void setModus(modii modus) {
        this.modus = modus;
        refreshHandles();
    }

    private void refreshHandles() {
        if (geom != null) {
            switch (modus) {
                case MARK_SELECTION:
                    showMarkHandles(true);
                    showMeasurementHandle(false);
                    break;
                case MARK_ADD:
                    showMarkHandles(true);
                    showMeasurementHandle(true);
                    break;
            }
        }
    }

    public modii getModus() {
        return modus;
    }

    @Override
    public void mouseMoved(final PInputEvent event) {
        Runnable t = new Runnable() {

            @Override
            public void run() {
                try {
                    if (mc.getInteractionMode().equals(MappingComponent.LINEMEASUREMENT)) {
                        geom = getSelectedGeometry();

                        if (event.isControlDown()) {
                            setModus(modii.MARK_SELECTION);
                        } else {
                            setModus(modii.MARK_ADD);

                            if (geom != null) {
                                if (geom instanceof MultiLineString || geom instanceof LineString) {
                                    updateHandleCoords(event.getPosition());
                                    measurementPHandle.setMarkPosition(getCurrentPosition());
                                    postCoordinateChanged();
                                } else {
                                    log.debug("Wrong geometry type: " + geom.getGeometryType());
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    log.fatal("Fehler in mouseMoved", t);//NOI18N
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
        private MarkPHandle handle;

        Mark(double position, MarkPHandle handle) {
            this.handle = handle;
            this.position = position;
        }

        public double getPosition() {
            return position;
        }

        public MarkPHandle getPHandle() {
            return handle;
        }
    }

}
