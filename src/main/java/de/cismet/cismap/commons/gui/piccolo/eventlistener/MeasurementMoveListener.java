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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.linearref.LengthLocationMap;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

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

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class MeasurementMoveListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final Color COLOR_SUBLINE = new Color(255, 91, 0);

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static enum modii {

        //~ Enum constants -----------------------------------------------------

        MARK_SELECTION, MARK_ADD, MEASUREMENT, SUBLINE
    }
    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static enum selectionTypes {

        //~ Enum constants -----------------------------------------------------

        NONE, MARK, SUBLINE
    }

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

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

    private ImageIcon icoMenRem = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/res/marker--minus.png")); // NOI18N
    private ImageIcon icoMenRemAll = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/res/marker--minus.png")); // NOI18N

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of SimpleMoveListener.
     *
     * @param  mc  DOCUMENT ME!
     */
    public MeasurementMoveListener(final MappingComponent mc) {
        super();
        this.mc = mc;

        final PLocator l = new PLocator() {

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

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public selectionTypes getSelectionType() {
        return selectionType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectionType  DOCUMENT ME!
     */
    private void setSelectionType(final selectionTypes selectionType) {
        this.selectionType = selectionType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    private Feature createSublineFeature() {
        final Feature feature = new Feature() {

                private Geometry geom;

                @Override
                public Geometry getGeometry() {
                    return geom;
                }

                @Override
                public void setGeometry(final Geometry geom) {
                    this.geom = geom;
                }

                @Override
                public boolean canBeSelected() {
                    return false;
                }

                @Override
                public void setCanBeSelected(final boolean canBeSelected) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean isEditable() {
                    return false;
                }

                @Override
                public void setEditable(final boolean editable) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean isHidden() {
                    return false;
                }

                @Override
                public void hide(final boolean hiding) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        return feature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    private void dragStart(final PInputEvent event) {
        if (event.isShiftDown()) {
            startSubline();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    private void dragUpdate(final PInputEvent event) {
        if (event.isShiftDown()) {
            updateSubline();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    private void dragEnd(final PInputEvent event) {
        if (event.isShiftDown()) {
            finishSubline();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void startSubline() {
        // addMarkHandle(handleX, handleY);
        startPosition = getCurrentPosition();
        setModus(modii.SUBLINE);

        sublinePFeature = new PFeature(createSublineFeature(), mc);
        sublinePFeature.setStroke(new CustomFixedWidthStroke(5));
        sublinePFeature.setStrokePaint(COLOR_SUBLINE);

        final PFeature pf = getSelectedPFeature();
        pf.addChild(sublinePFeature);
        pf.repaint();
    }

    /**
     * DOCUMENT ME!
     */
    private void updateSubline() {
        final double endPosition = getCurrentPosition();
        final LengthIndexedLine lil = new LengthIndexedLine(geom);
        final LineString subline = (LineString)lil.extractLine(startPosition, endPosition);

        sublinePFeature.getFeature().setGeometry(subline);
        sublinePFeature.syncGeometry();
        sublinePFeature.visualize();
    }

    /**
     * DOCUMENT ME!
     */
    private void finishSubline() {
        // addMarkHandle(handleX, handleY);
        addSublinePHandle(startPosition, getCurrentPosition());
        setModus(modii.MARK_ADD);
    }

    @Override
    public void mouseDragged(final PInputEvent event) {
        updateHandleCoords2(event.getPosition());
        if (!isDragging) {
            dragStart(event);
        }
        isDragging = true;
        dragUpdate(event);
    }

    @Override
    public void mouseReleased(final PInputEvent event) {
        if (isDragging) {
            isDragging = false;
            dragEnd(event);
        }
    }

    @Override
    public void mouseClicked(final PInputEvent event) {
        if (event.isLeftMouseButton()) {
            switch (modus) {
                case MARK_ADD: {
                    final PFeature selPFeature = getSelectedPFeature();
                    if (selPFeature != null) {
                        final Point2D trigger = event.getPosition();
                        final Point2D[] neighbours = getNearestNeighbours(trigger, selPFeature);
                        final Point2D erg = StaticGeometryFunctions.createPointOnLine(
                                neighbours[0],
                                neighbours[1],
                                trigger);

                        addMarkPHandle(erg.getX(), erg.getY());
                    }
                    break;
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Coordinate getCoordinateOfPosition(final double position) {
        final LengthIndexedLine lil = new LengthIndexedLine(geom);
        return lil.extractPoint(position);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Coordinate getSelectedMarkCoordinate() {
        return getCoordinateOfPosition(getSelectedMarkPosition());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getSelectedSublineStart() {
        if (selectedSubline != null) {
            return selectedSubline.getPositionStart();
        } else {
            return 0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getSelectedSublineEnd() {
        if (selectedSubline != null) {
            return selectedSubline.getPositionEnd();
        } else {
            return 0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getSelectedMarkPosition() {
        if (selectedMark != null) {
            return selectedMark.getPosition();
        } else {
            return 0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  markPHandle  DOCUMENT ME!
     */
    public void setSelectedMark(final MarkPHandle markPHandle) {
        if (markPHandle == null) {
            this.selectedMark = null;
        } else {
            for (final Mark mark : marks) {
                if ((mark != null) && mark.getPHandle().equals(markPHandle)) {
                    this.selectedMark = mark;
                    setSelectionType(selectionTypes.MARK);
                    break;
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sublinePHandle  DOCUMENT ME!
     */
    public void setSelectedSubline(final SublinePHandle sublinePHandle) {
        if (sublinePHandle == null) {
            this.selectedSubline = null;
        } else {
            for (final Subline subline : sublines) {
                if ((subline != null) && subline.getPHandle().equals(sublinePHandle)) {
                    this.selectedSubline = subline;
                    setSelectionType(selectionTypes.SUBLINE);
                    break;
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JPopupMenu getContextMenu() {
        return menu;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  item  DOCUMENT ME!
     */
    public void removeContextMenuItem(final JMenuItem item) {
        menu.remove(item);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  item  DOCUMENT ME!
     */
    public void addContextMenuItem(final JMenuItem item) {
        menu.remove(cmdRemoveMark);
        menu.remove(cmdRemoveAllMarks);
        menu.add(item);
        menu.add(cmdRemoveMark);
        menu.add(cmdRemoveAllMarks);
    }

    /**
     * DOCUMENT ME!
     */
    private void initContextMenu() {
        menu = new JPopupMenu();

        cmdRemoveMark = new JMenuItem("entfernen");          // NOI18N
        cmdRemoveAllMarks = new JMenuItem("alle entfernen"); // NOI18N

        cmdRemoveMark.setIcon(icoMenRem);
        cmdRemoveAllMarks.setIcon(icoMenRemAll);

        cmdRemoveMark.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent ae) {
                    switch (selectionType) {
                        case MARK: {
                            removeMark(selectedMark);
                            break;
                        }
                        case SUBLINE: {
                            removeSubline(selectedSubline);
                            break;
                        }
                    }
                }
            });
        cmdRemoveAllMarks.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent ae) {
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

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PLayer getPLayer() {
        return mc.getHandleLayer();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mark  DOCUMENT ME!
     */
    private void removeMark(final Mark mark) {
        marks.remove(mark);
        getPLayer().removeChild(mark.getPHandle());
    }

    /**
     * DOCUMENT ME!
     */
    public void removeAllMarks() {
        for (final Mark mark : marks) {
            getPLayer().removeChild(mark.getPHandle());
        }
        marks.removeAllElements();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  subline  DOCUMENT ME!
     */
    private void removeSubline(final Subline subline) {
        sublines.remove(subline);
        getSelectedPFeature().removeChild(subline.getPFeature());
        getPLayer().removeChild(subline.getPHandle());
    }

    /**
     * DOCUMENT ME!
     */
    public void removeAllSublines() {
        for (final Subline subline : sublines) {
            getPLayer().removeChild(subline.getPHandle());
            getSelectedPFeature().removeChild(subline.getPFeature());
        }
        sublines.removeAllElements();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCurrentPosition() {
        if (geom != null) {
            return LinearReferencedPointFeature.getPositionOnLine(new Coordinate(
                        mc.getWtst().getSourceX(cursorX),
                        mc.getWtst().getSourceY(cursorY)),
                    geom);
        } else {
            return 0d;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  show  DOCUMENT ME!
     */
    private void showMarks(final boolean show) {
        for (final Mark mark : marks) {
            showOnFather(getPLayer(), mark.getPHandle(), show);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  x  DOCUMENT ME!
     * @param  y  DOCUMENT ME!
     */
    private void addMarkPHandle(final double x, final double y) {
        if (log.isDebugEnabled()) {
            log.debug("create newPointHandle and Locator"); // NOI18N
        }
        final PLocator l = new PLocator() {

                @Override
                public double locateX() {
                    return x;
                }

                @Override
                public double locateY() {
                    return y;
                }
            };

        final MarkPHandle markHandle = new MarkPHandle(l, this, mc);
        final double currentPosition = getCurrentPosition();
        markHandle.setMarkPosition(currentPosition);

        marks.add(new Mark(currentPosition, markHandle));
        getPLayer().addChild(markHandle);

        setSelectedMark(markHandle);

        // measurementPHandle wieder nach oben holen
        getPLayer().removeChild(cursorPHandle);
        getPLayer().addChild(cursorPHandle);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  show  DOCUMENT ME!
     */
    private void showCursor(final boolean show) {
        showOnFather(getPLayer(), cursorPHandle, show);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  father  DOCUMENT ME!
     * @param  child   DOCUMENT ME!
     * @param  show    DOCUMENT ME!
     */
    private void showOnFather(final PNode father, final PPath child, final boolean show) {
        boolean found = false;
        for (final Object o : father.getChildrenReference()) {
            if ((o != null) && o.equals(child)) {
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

    /**
     * DOCUMENT ME!
     *
     * @param  startPosition  DOCUMENT ME!
     * @param  endPosition    DOCUMENT ME!
     */
    private void addSublinePHandle(final double startPosition, final double endPosition) {
        final double mid = startPosition + ((endPosition - startPosition) / 2);
        final double length = Math.abs(endPosition - startPosition);

        final Coordinate midCoord = getCoordinateOfPosition(mid);
        if (log.isDebugEnabled()) {
            log.debug("midCoord: " + midCoord);
        }

        final double xTest = cursorX;
        final double yTest = cursorY;

        final PLocator l = new PLocator() {

                @Override
                public double locateX() {
                    return xTest;
                }

                @Override
                public double locateY() {
                    return yTest;
                }
            };

        final SublinePHandle pHandle = new SublinePHandle(l, this, mc);
        pHandle.setPositions(startPosition, endPosition);

        sublines.add(new Subline(startPosition, endPosition, pHandle, sublinePFeature));
        getPLayer().addChild(pHandle);

        setSelectedSubline(pHandle);

        // measurementPHandle wieder nach oben holen
        getPLayer().removeChild(cursorPHandle);
        getPLayer().addChild(cursorPHandle);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  show  DOCUMENT ME!
     */
    private void showSublines(final boolean show) {
        for (final Subline subline : sublines) {
            if (log.isDebugEnabled()) {
                log.debug(subline.getPHandle() + " -- " + subline.getPFeature());
            }
            showOnFather(getPLayer(), subline.getPHandle(), show);
            showOnFather(getSelectedPFeature(), subline.getPFeature(), show);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  modus  DOCUMENT ME!
     */
    private void setModus(final modii modus) {
        this.modus = modus;
        refreshHandles();
    }

    /**
     * DOCUMENT ME!
     */
    private void refreshHandles() {
        if (geom != null) {
            switch (modus) {
                case MARK_SELECTION: {
                    showCursor(false);
                    showMarks(true);
                    showSublines(true);
                    break;
                }
                case MARK_ADD: {
                    showCursor(true);
                    showMarks(true);
                    showSublines(true);
                    break;
                }
                case SUBLINE: {
                    showCursor(true);
                    showMarks(false);
                    showSublines(true);
                    break;
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public modii getModus() {
        return modus;
    }

    @Override
    public void mouseMoved(final PInputEvent event) {
        final Runnable t = new Runnable() {

                @Override
                public void run() {
                    try {
                        if (mc.getInteractionMode().equals(MappingComponent.LINEMEASUREMENT)) {
                            final PFeature selPFeature = getSelectedPFeature();
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
                        log.fatal("Fehler in mouseMoved", t); // NOI18N
                    }
                }
            };

        EventQueue.invokeLater(t);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  trigger  DOCUMENT ME!
     */
    private void updateHandleCoords2(final Point2D trigger) {
        final PFeature selPFeature = getSelectedPFeature();
        geom = selPFeature.getFeature().getGeometry();
        if (geom != null) {
            if ((geom instanceof MultiLineString) || (geom instanceof LineString)) {
                updateHandleCoords(trigger);
                cursorPHandle.setMarkPosition(getCurrentPosition());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Wrong geometry type: " + geom.getGeometryType());
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  trigger  DOCUMENT ME!
     */
    private void updateHandleCoords(final Point2D trigger) {
        final PFeature selPFeature = getSelectedPFeature();
        if (selPFeature != null) {
            final Point2D[] neighbours = getNearestNeighbours(trigger, selPFeature);
            final Point2D erg = StaticGeometryFunctions.createPointOnLine(neighbours[0], neighbours[1], trigger);

            cursorX = (float)erg.getX();
            cursorY = (float)erg.getY();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getSelectedPFeature() {
        // Collection holen
        final FeatureCollection fc = mc.getFeatureCollection();
        // Selektierte Features holen
        final Collection<Feature> sel = fc.getSelectedFeatures();

        // wenn genau 1 Objekt selektiert ist
        if ((fc instanceof DefaultFeatureCollection) && (sel.size() == 1)) {
            // selektiertes feature holen
            final Feature[] sels = sel.toArray(new Feature[0]);
            // zugehöriges pfeature holen
            final PFeature pf = mc.getPFeatureHM().get(sels[0]);
            // zugehörige geometrie holen
            return pf;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   trigger   DOCUMENT ME!
     * @param   pfeature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Point2D[] getNearestNeighbours(final Point2D trigger, final PFeature pfeature) {
        Point2D start = null;
        Point2D end = null;
        double dist = Double.POSITIVE_INFINITY;
        if ((geom != null) || (geom instanceof MultiLineString) || (geom instanceof LineString)) {
            for (int i = 0; i < (pfeature.getXp().length - 1); i++) {
                final Point2D tmpStart = new Point2D.Double(pfeature.getXp()[i], pfeature.getYp()[i]);
                final Point2D tmpEnd = new Point2D.Double(pfeature.getXp()[i + 1], pfeature.getYp()[i + 1]);
                final double tmpDist = StaticGeometryFunctions.distanceToLine(tmpStart, tmpEnd, trigger);
                if (tmpDist < dist) {
                    dist = tmpDist;
                    start = tmpStart;
                    end = tmpEnd;
                }
            }
        }
        return new Point2D[] { start, end };
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class Mark {

        //~ Instance fields ----------------------------------------------------

        private double position;
        private MarkPHandle pHandle;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Mark object.
         *
         * @param  position  DOCUMENT ME!
         * @param  handle    DOCUMENT ME!
         */
        Mark(final double position, final MarkPHandle handle) {
            this.pHandle = handle;
            this.position = position;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public double getPosition() {
            return position;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public MarkPHandle getPHandle() {
            return pHandle;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class Subline {

        //~ Instance fields ----------------------------------------------------

        private double positionStart;
        private double positionEnd;
        private SublinePHandle pHandle;
        private PFeature pFeature;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Subline object.
         *
         * @param  positionStart  DOCUMENT ME!
         * @param  positionEnd    DOCUMENT ME!
         * @param  pHandle        DOCUMENT ME!
         * @param  pFeature       DOCUMENT ME!
         */
        Subline(final double positionStart,
                final double positionEnd,
                final SublinePHandle pHandle,
                final PFeature pFeature) {
            this.pHandle = pHandle;
            this.pFeature = pFeature;
            this.positionStart = positionStart;
            this.positionEnd = positionEnd;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public double getPositionStart() {
            return positionStart;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public double getPositionEnd() {
            return positionEnd;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public SublinePHandle getPHandle() {
            return pHandle;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public PFeature getPFeature() {
            return pFeature;
        }
    }
}
