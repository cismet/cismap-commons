package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.linearref.LengthLocationMap;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.CustomFixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.MarkPHandle;
import de.cismet.cismap.commons.gui.piccolo.MeasurementPHandle;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.SublinePHandle;
import de.cismet.math.geometry.StaticGeometryFunctions;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PLocator;
import java.awt.Color;
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
    public static enum modii {
        MARK_SELECTION,
        MARK_ADD,
        MEASUREMENT,
        SUBLINE
    };
    public static enum selectionTypes {
        NONE,
        MARK,
        SUBLINE
    }
    
    private static final Color COLOR_SUBLINE = new Color(255, 0, 255);

    private MappingComponent mc;
    private float cursorX = Float.MIN_VALUE;
    private float cursorY = Float.MIN_VALUE;
    private MeasurementPHandle cursorPHandle;

    private double startPosition;

    private Geometry geom;
    private Vector<Mark> marks = new Vector<Mark>();
    private Vector<Subline> sublines = new Vector<Subline>();
    private PFeature sublinePFeature;

    private modii modus;
    private JPopupMenu menu;
    private Mark selectedMark;
    private Subline selectedSubline;
    private selectionTypes selectionType;
    private boolean isDragging = false;

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
                return cursorX;
            }

            @Override
            public double locateY() {
                return cursorY;
            }
        };
        cursorPHandle = new MeasurementPHandle(l, mc);

        initContextMenu();
    }

    public selectionTypes getSelectionType() {
        return selectionType;
    }

    private void setSelectionType(selectionTypes selectionType) {
        this.selectionType = selectionType;
    }

    private Feature createSublineFeature() {
        Feature feature = new Feature() {

            private Geometry geom;

            @Override
            public Geometry getGeometry() {
                return geom;
            }

            @Override
            public void setGeometry(Geometry geom) {
                this.geom = geom;
            }

            @Override
            public boolean canBeSelected() {
                return false;
            }

            @Override
            public void setCanBeSelected(boolean canBeSelected) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isEditable() {
                return false;
            }

            @Override
            public void setEditable(boolean editable) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isHidden() {
                return false;
            }

            @Override
            public void hide(boolean hiding) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        return feature;
    }

    private void dragStart(PInputEvent event) {
        if (event.isShiftDown()) {
            startSubline();
        }

    }

    private void dragUpdate(PInputEvent event) {
        if (event.isShiftDown()) {
            updateSubline();
        }
    }

    private void dragEnd(PInputEvent event) {
        if (event.isShiftDown()) {
            finishSubline();
        }
    }

    private void startSubline() {
        //addMarkHandle(handleX, handleY);
        startPosition = getCurrentPosition();
        setModus(modii.SUBLINE);

        sublinePFeature = new PFeature(createSublineFeature(), mc);
        sublinePFeature.setStroke(new CustomFixedWidthStroke(5));
        sublinePFeature.setStrokePaint(COLOR_SUBLINE);

        PFeature pf = getSelectedPFeature();
        pf.addChild(sublinePFeature);
        pf.repaint();
    }

    private void updateSubline() {
        double endPosition = getCurrentPosition();
        LengthIndexedLine lil = new LengthIndexedLine(geom);
        LineString subline = (LineString)lil.extractLine(startPosition, endPosition);

        sublinePFeature.getFeature().setGeometry(subline);
        sublinePFeature.syncGeometry();
        sublinePFeature.visualize();
    }

    private void finishSubline() {
        //addMarkHandle(handleX, handleY);
        addSublinePHandle(startPosition, getCurrentPosition());
        setModus(modii.MARK_ADD);
    }

    @Override
    public void mouseDragged(PInputEvent event) {
        updateHandleCoords2(event.getPosition());
        if (!isDragging) {
            dragStart(event);
        }
        isDragging = true;
        dragUpdate(event);
    }

    @Override
    public void mouseReleased(PInputEvent event) {
        if (isDragging) {
            isDragging = false;
            dragEnd(event);
        }
    }

    @Override
    public void mouseClicked(PInputEvent event) {
        if (event.isLeftMouseButton()) {
            switch (modus) {
                case MARK_ADD:
                    PFeature selPFeature = getSelectedPFeature();
                    if (selPFeature != null) {
                        Point2D trigger = event.getPosition();
                        Point2D[] neighbours = getNearestNeighbours(trigger, selPFeature);
                        Point2D erg = StaticGeometryFunctions.createPointOnLine(neighbours[0], neighbours[1], trigger);

                        addMarkPHandle(erg.getX(), erg.getY());
                    }
                    break;
            }
        }
    }

    public Coordinate getCoordinateOfPosition(double position) {
        LengthIndexedLine lil = new LengthIndexedLine(geom);
        return lil.extractPoint(position);
    }

    public Coordinate getSelectedMarkCoordinate() {
        return getCoordinateOfPosition(getSelectedMarkPosition());
    }

    public double getSelectedSublineStart() {
        if (selectedSubline != null) {
            return selectedSubline.getPositionStart();
        } else {
            return 0;
        }
    }

    public double getSelectedSublineEnd() {
        if (selectedSubline != null) {
            return selectedSubline.getPositionEnd();
        } else {
            return 0;
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
                    setSelectionType(selectionTypes.MARK);
                    break;
                }
            }
        }
    }

    public void setSelectedSubline(SublinePHandle sublinePHandle) {
        if (sublinePHandle == null) {
            this.selectedSubline = null;
        } else {
            for (Subline subline : sublines) {
                if (subline != null && subline.getPHandle().equals(sublinePHandle)) {
                    this.selectedSubline = subline;
                    setSelectionType(selectionTypes.SUBLINE);
                    break;
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
                switch (selectionType) {
                    case MARK:
                        removeMark(selectedMark);
                        break;
                    case SUBLINE:
                        removeSubline(selectedSubline);
                        break;
                }
            }
        });
        cmdRemoveAllMarks.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                removeAllMarks();
                removeAllSublines();
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

    private void removeMark(Mark mark) {
        marks.remove(mark);
        getPLayer().removeChild(mark.getPHandle());
    }

    public void removeAllMarks() {
        for (Mark mark : marks) {
            getPLayer().removeChild(mark.getPHandle());
        }
        marks.removeAllElements();
    }

    private void removeSubline(Subline subline) {
        sublines.remove(subline);
        getSelectedPFeature().removeChild(subline.getPFeature());
        getPLayer().removeChild(subline.getPHandle());
    }

    public void removeAllSublines() {
        for (Subline subline : sublines) {
            getPLayer().removeChild(subline.getPHandle());
            getSelectedPFeature().removeChild(subline.getPFeature());
        }
        sublines.removeAllElements();
    }

    private double getCurrentPosition() {
        if (geom != null) {
            LocationIndexedLine lil = new LocationIndexedLine(geom);
            Coordinate c = new Coordinate(mc.getWtst().getSourceX(cursorX), mc.getWtst().getSourceY(cursorY));
            LinearLocation ll = lil.indexOf(c);
            LengthLocationMap llm = new LengthLocationMap(geom);
            return llm.getLength(ll);
        } else {
            return 0d;
        }
    }

    private void showMarks(boolean show) {
        for (Mark mark : marks) {
            showOnFather(getPLayer(), mark.getPHandle(), show);
        }
    }

    private void addMarkPHandle(final double x, final double y) {
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
        getPLayer().addChild(markHandle);
        
        setSelectedMark(markHandle);

        //measurementPHandle wieder nach oben holen
        getPLayer().removeChild(cursorPHandle);
        getPLayer().addChild(cursorPHandle);
    }

    private void showCursor(boolean show) {
        showOnFather(getPLayer(), cursorPHandle, show);
    }

    private void showOnFather(PNode father, PPath child, boolean show) {
        boolean found = false;
        for (Object o : father.getChildrenReference()) {
            if (o != null && o.equals(child)) {
                found = true;
                break;
            }
        }
        if (!found && show) {
            father.addChild(child);
        }
        if (found && !show) {
            father.removeChild(child);
        }
    }

    private void addSublinePHandle(final double startPosition, final double endPosition) {
        double mid = startPosition + (endPosition - startPosition) / 2;
        double length = Math.abs(endPosition - startPosition);
        
        final Coordinate midCoord = getCoordinateOfPosition(mid);
        log.debug("midCoord: " + midCoord);

        final double xTest = cursorX;
        final double yTest = cursorY;

        PLocator l = new PLocator() {

            @Override
            public double locateX() {
                return xTest;
            }

            @Override
            public double locateY() {
                return yTest;
            }
        };

        SublinePHandle pHandle = new SublinePHandle(l, this, mc);
        pHandle.setMarkPosition(length);

        sublines.add(new Subline(startPosition, endPosition, pHandle, sublinePFeature));
        getPLayer().addChild(pHandle);

        setSelectedSubline(pHandle);

        //measurementPHandle wieder nach oben holen
        getPLayer().removeChild(cursorPHandle);
        getPLayer().addChild(cursorPHandle);

    }

    private void showSublines(boolean show) {
        for (Subline subline : sublines) {
            log.debug(subline.getPHandle() + " -- " + subline.getPFeature());
            showOnFather(getPLayer(), subline.getPHandle(), show);
            showOnFather(getSelectedPFeature(), subline.getPFeature(), show);
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
                    showCursor(false);
                    showMarks(true);
                    showSublines(true);
                    break;
                case MARK_ADD:
                    showCursor(true);
                    showMarks(true);
                    showSublines(true);
                    break;
                case SUBLINE:
                    showCursor(true);
                    showMarks(false);
                    showSublines(true);
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
                        PFeature selPFeature = getSelectedPFeature();
                        if (selPFeature != null) {
                            updateHandleCoords2(event.getPosition());
                            if (event.isControlDown()) {
                                setModus(modii.MARK_SELECTION);
                            } else {
                                setModus(modii.MARK_ADD);                                
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

    private void updateHandleCoords2(Point2D trigger) {
        PFeature selPFeature = getSelectedPFeature();
        geom = selPFeature.getFeature().getGeometry();
        if (geom != null) {
            if (geom instanceof MultiLineString || geom instanceof LineString) {
                updateHandleCoords(trigger);
                cursorPHandle.setMarkPosition(getCurrentPosition());
            } else {
                log.debug("Wrong geometry type: " + geom.getGeometryType());
            }
        }
    }

    private void updateHandleCoords(Point2D trigger) {
        PFeature selPFeature = getSelectedPFeature();
        if (selPFeature != null) {
            Point2D[] neighbours = getNearestNeighbours(trigger, selPFeature);
            Point2D erg = StaticGeometryFunctions.createPointOnLine(neighbours[0], neighbours[1], trigger);

            cursorX = (float) erg.getX();
            cursorY = (float) erg.getY();
        }
    }

    public PFeature getSelectedPFeature() {
        //Collection holen
        FeatureCollection fc = mc.getFeatureCollection();
        //Selektierte Features holen
        Collection<Feature> sel = fc.getSelectedFeatures();

        //wenn genau 1 Objekt selektiert ist
        if (fc instanceof DefaultFeatureCollection && sel.size() == 1) {
            // selektiertes feature holen
            Feature[] sels = sel.toArray(new Feature[0]);
            //zugehöriges pfeature holen
            PFeature pf = mc.getPFeatureHM().get(sels[0]);
            //zugehörige geometrie holen
            return pf;
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

    class Mark {
        private double position;
        private MarkPHandle pHandle;

        Mark(double position, MarkPHandle handle) {
            this.pHandle = handle;
            this.position = position;
        }

        public double getPosition() {
            return position;
        }

        public MarkPHandle getPHandle() {
            return pHandle;
        }
    }

    class Subline {
        private double positionStart;
        private double positionEnd;
        private SublinePHandle pHandle;
        private PFeature pFeature;

        Subline(double positionStart, double positionEnd, SublinePHandle pHandle, PFeature pFeature) {
            this.pHandle = pHandle;
            this.pFeature = pFeature;
            this.positionStart = positionStart;
            this.positionEnd = positionEnd;
        }

        public double getPositionStart() {
            return positionStart;
        }

        public double getPositionEnd() {
            return positionEnd;
        }

        public SublinePHandle getPHandle() {
            return pHandle;
        }

        public PFeature getPFeature() {
            return pFeature;
        }
    }
}
