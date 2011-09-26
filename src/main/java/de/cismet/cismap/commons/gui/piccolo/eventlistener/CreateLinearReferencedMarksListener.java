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

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PLocator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.LinearReferencedPointMarkPHandle;
import de.cismet.cismap.commons.gui.piccolo.PFeature;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class CreateLinearReferencedMarksListener extends PBasicInputEventHandler {

    //~ Enums ------------------------------------------------------------------

    // private static final Color COLOR_SUBLINE = new Color(255, 91, 0);

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static enum Modus {

        //~ Enum constants -----------------------------------------------------

        MARK_SELECTION, MARK_ADD, MEASUREMENT
    }

    //~ Instance fields --------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
// public static enum SelectionType {
//
// //~ Enum constants -----------------------------------------------------
//
// NONE, MARK, SUBLINE
// }

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    private MappingComponent mc;

//    private double cursorPosition = -1;
    private float cursorX = Float.MIN_VALUE;
    private float cursorY = Float.MIN_VALUE;
    private final LinearReferencedPointMarkPHandle cursorPHandle;

//    private double lineStartPosition = -1;
//    private float lineStartX = Float.MIN_VALUE;
//    private float lineStartY = Float.MIN_VALUE;
//    private final MeasurementPHandle lineStartPHandle;

    private HashMap<PFeature, Collection<PointMark>> featurePointMarks = new HashMap<PFeature, Collection<PointMark>>();
//    private HashMap<PFeature, Collection<LineMark>> featureLineMarks = new HashMap<PFeature, Collection<LineMark>>();
    private PointMark selectedPointMark;
//    private LineMark selectedLineMark;

//    private PFeature currentLineMarkPFeature = null;

    private Modus modus = Modus.MARK_ADD;
    private JPopupMenu menu;
//    private SelectionType selectionType = SelectionType.NONE;

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
    public CreateLinearReferencedMarksListener(final MappingComponent mc) {
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
        cursorPHandle = new LinearReferencedPointMarkPHandle(l, this, mc);

//        final PLocator lsl = new PLocator() {
//
//                @Override
//                public double locateX() {
//                    return lineStartX;
//                }
//
//                @Override
//                public double locateY() {
//                    return lineStartY;
//                }
//            };
//        lineStartPHandle = new MeasurementPHandle(lsl, mc);

        initContextMenu();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
// public SelectionType getSelectionType() {
// return selectionType;
// }

    /**
     * DOCUMENT ME!
     *
     * @param  event  selectionType DOCUMENT ME!
     */
// private void setSelectionType(final SelectionType selectionType) {
// this.selectionType = selectionType;
// }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
// private Feature createSublineFeature() {
// final Feature feature = new Feature() {
//
// private Geometry geom;
//
// @Override
// public Geometry getGeometry() {
// return geom;
// }
//
// @Override
// public void setGeometry(final Geometry geom) {
// this.geom = geom;
// }
//
// @Override
// public boolean canBeSelected() {
// return false;
// }
//
// @Override
// public void setCanBeSelected(final boolean canBeSelected) {
// throw new UnsupportedOperationException("Not supported yet.");
// }
//
// @Override
// public boolean isEditable() {
// return false;
// }
//
// @Override
// public void setEditable(final boolean editable) {
// throw new UnsupportedOperationException("Not supported yet.");
// }
//
// @Override
// public boolean isHidden() {
// return false;
// }
//
// @Override
// public void hide(final boolean hiding) {
// throw new UnsupportedOperationException("Not supported yet.");
// }
// };
// return feature;
// }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
// private void startLineMark() {
// // addMarkHandle(handleX, handleY);
// lineStartPosition = getCurrentPosition();
// lineStartX = cursorX;
// lineStartY = cursorY;
////        setModus(Modus.DRAWING_SUBLINE);
//
//        currentLineMarkPFeature = new PFeature(createSublineFeature(), mc);
//        currentLineMarkPFeature.setStroke(new CustomFixedWidthStroke(5));
////        currentLineMarkPFeature.setStrokePaint(COLOR_SUBLINE);
//
//        currentLineMarkPFeature.addChild(lineStartPHandle);
//        currentLineMarkPFeature.addChild(cursorPHandle);
//
//        final PFeature pf = getSelectedLinePFeature();
//        pf.addChild(currentLineMarkPFeature);
//        pf.repaint();
//    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
//    private void updateLineMark() {
//        final LengthIndexedLine lil = new LengthIndexedLine(getSelectedLinePFeature().getFeature().getGeometry());
//        final LineString subline = (LineString)lil.extractLine(lineStartPosition, cursorPosition);
//
//        currentLineMarkPFeature.getFeature().setGeometry(subline);
//        currentLineMarkPFeature.syncGeometry();
//        currentLineMarkPFeature.visualize();
//    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
//    private void finishLineMark() {
//        // addMarkHandle(handleX, handleY);
//        addSublinePHandle(lineStartPosition, cursorPosition);
//        setModus(Modus.MARK_ADD);
//        currentLineMarkPFeature = null;
//    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    @Override
    public void mouseDragged(final PInputEvent event) {
//        if (!isDragging() && event.isShiftDown()) {
//            startLineMark();
//        }
//        updateCursor(event.getPosition());
//        cursorPHandle.setMarkPosition(getCurrentPosition());
//        if (isDragging()) {
//            updateLineMark();
//        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    @Override
    public void mouseReleased(final PInputEvent event) {
//        log.fatal("mouse released");
//        if (isDragging()) {
//            finishLineMark();
//        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
// private boolean isDragging() {
// log.fatal("isDragging: " + currentLineMarkPFeature + " = " + currentLineMarkPFeature != null);
// return currentLineMarkPFeature != null;
// }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    @Override
    public void mouseClicked(final PInputEvent event) {
        if (event.isLeftMouseButton()) {
            switch (modus) {
                case MARK_ADD: {
                    final PFeature selPFeature = getSelectedLinePFeature();
                    if (selPFeature != null) {
                        addMarkPHandle(new Coordinate(cursorX, cursorY));
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
        final LengthIndexedLine lil = new LengthIndexedLine(getSelectedLinePFeature().getFeature().getGeometry());
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
// public double getSelectedSublineStart() {
// if (selectedLineMark != null) {
// return selectedLineMark.getPositionStart();
// } else {
// return 0;
// }
// }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
// public double getSelectedSublineEnd() {
// if (selectedLineMark != null) {
// return selectedLineMark.getPositionEnd();
// } else {
// return 0;
// }
// }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getSelectedMarkPosition() {
        if (selectedPointMark != null) {
            return selectedPointMark.getPosition();
        } else {
            return 0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  markPHandle  DOCUMENT ME!
     */
    public void setSelectedMark(final LinearReferencedPointMarkPHandle markPHandle) {
        if (markPHandle == null) {
            this.selectedPointMark = null;
        } else {
            for (final PointMark mark : getPointMarks(getSelectedLinePFeature())) {
                if ((mark != null) && mark.getPHandle().equals(markPHandle)) {
                    this.selectedPointMark = mark;
//                    setSelectionType(SelectionType.MARK);
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
// public void setSelectedSubline(final MarkLinePHandle sublinePHandle) {
// if (sublinePHandle == null) {
// this.selectedLineMark = null;
// } else {
// for (final LineMark subline : getLineMarks(getSelectedLinePFeature())) {
// if ((subline != null) && subline.getPHandle().equals(sublinePHandle)) {
// this.selectedLineMark = subline;
// setSelectionType(SelectionType.SUBLINE);
// break;
// }
// }
// }
// }

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

        cmdRemoveMark = new JMenuItem("Markierung entfernen");            // NOI18N
        cmdRemoveAllMarks = new JMenuItem("alle Markierungen entfernen"); // NOI18N

        cmdRemoveMark.setIcon(icoMenRem);
        cmdRemoveAllMarks.setIcon(icoMenRemAll);

        cmdRemoveMark.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent ae) {
//                switch (selectionType) {
//                    case MARK: {
                    removeMark(selectedPointMark);
//                        break;
//                    }
//                    case SUBLINE: {
//                        // removeLineMark(selectedLineMark);
//                        break;
//                    }
//                 }
                }
            });
        cmdRemoveAllMarks.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent ae) {
                    removeAllMarks();
                    // removeAllLineMarks();
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
    private void removeMark(final PointMark mark) {
        if (getSelectedLinePFeature() != null) {
            getPointMarks(getSelectedLinePFeature()).remove(mark);
            getPLayer().removeChild(mark.getPHandle());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void removeAllMarks() {
        final Collection<PointMark> pointMarks = getPointMarks(getSelectedLinePFeature());
        for (final PointMark mark : pointMarks) {
            getPLayer().removeChild(mark.getPHandle());
        }
        pointMarks.clear();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
// private void removeLineMark(final LineMark subline) {
// getLineMarks(getSelectedLinePFeature()).remove(subline);
// getSelectedLinePFeature().removeChild(subline.getPFeature());
// getPLayer().removeChild(subline.getPHandle());
// }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
// public void removeAllLineMarks() {
// final Collection<LineMark> lineMarks = getLineMarks(getSelectedLinePFeature());
// for (final LineMark subline : lineMarks) {
// getPLayer().removeChild(subline.getPHandle());
// getSelectedLinePFeature().removeChild(subline.getPFeature());
// }
// lineMarks.clear();
// }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCurrentPosition() {
        if (getSelectedLinePFeature() != null) {
            return LinearReferencedPointFeature.getPositionOnLine(new Coordinate(
                        mc.getWtst().getSourceX(cursorX),
                        mc.getWtst().getSourceY(cursorY)),
                    getSelectedLinePFeature().getFeature().getGeometry());
        } else {
            return 0d;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  show  DOCUMENT ME!
     */
    private void showPointMarks(final boolean show) {
        for (final PointMark mark : getPointMarks(getSelectedLinePFeature())) {
            showOnFather(getPLayer(), mark.getPHandle(), show);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pFeature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<PointMark> getPointMarks(final PFeature pFeature) {
        Collection<PointMark> pointMarks = featurePointMarks.get(getSelectedLinePFeature());
        if (pointMarks == null) {
            pointMarks = new ArrayList<PointMark>();
            featurePointMarks.put(pFeature, new ArrayList<PointMark>());
        }
        return pointMarks;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  coordinate  pFeature DOCUMENT ME!
     */
// private Collection<LineMark> getLineMarks(final PFeature pFeature) {
// Collection<LineMark> lineMarks = featureLineMarks.get(getSelectedLinePFeature());
// if (lineMarks == null) {
// lineMarks = new ArrayList<LineMark>();
// featureLineMarks.put(pFeature, new ArrayList<LineMark>());
// }
// return lineMarks;
// }

    /**
     * DOCUMENT ME!
     *
     * @param  coordinate  x DOCUMENT ME!
     */
    private void addMarkPHandle(final Coordinate coordinate) {
        if (log.isDebugEnabled()) {
            log.debug("create newPointHandle and Locator"); // NOI18N
        }
        final PLocator l = new PLocator() {

                @Override
                public double locateX() {
                    return coordinate.x;
                }

                @Override
                public double locateY() {
                    return coordinate.y;
                }
            };

        final LinearReferencedPointMarkPHandle markHandle = new LinearReferencedPointMarkPHandle(l, this, mc);
        final double currentPosition = getCurrentPosition();
        markHandle.setMarkPosition(currentPosition);

        final PointMark pointMark = new PointMark(currentPosition, markHandle);
        getPointMarks(getSelectedLinePFeature()).add(pointMark);
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
     * @param  modus  startPosition DOCUMENT ME!
     */
// private void addSublinePHandle(final double startPosition, final double endPosition) {
// final double mid = startPosition + ((endPosition - startPosition) / 2);
// final double length = Math.abs(endPosition - startPosition);
//
// final Coordinate midCoord = getCoordinateOfPosition(mid);
// if (log.isDebugEnabled()) {
// log.debug("midCoord: " + midCoord);
// }
//
// final double xTest = cursorX;
// final double yTest = cursorY;
//
// final PLocator l = new PLocator() {
//
// @Override
// public double locateX() {
// return xTest;
// }
//
// @Override
// public double locateY() {
// return yTest;
// }
// };
//
// final MarkLinePHandle pHandle = new MarkLinePHandle(l, this, mc);
// pHandle.setPositions(startPosition, endPosition);
//
// final LineMark lineMark = new LineMark(startPosition, endPosition, pHandle, currentLineMarkPFeature);
//
// getLineMarks(getSelectedLinePFeature()).add(lineMark);
// getPLayer().addChild(pHandle);
//
// setSelectedSubline(pHandle);
//
// // measurementPHandle wieder nach oben holen
// getPLayer().removeChild(cursorPHandle);
// getPLayer().addChild(cursorPHandle);
// }

    /**
     * DOCUMENT ME!
     *
     * @param  modus  show DOCUMENT ME!
     */
// private void showSublines(final boolean show) {
// for (final LineMark subline : getLineMarks(getSelectedLinePFeature())) {
// if (log.isDebugEnabled()) {
// log.debug(subline.getPHandle() + " -- " + subline.getPFeature());
// }
// showOnFather(getPLayer(), subline.getPHandle(), show);
// showOnFather(getSelectedLinePFeature(), subline.getPFeature(), show);
// }
// }

    /**
     * DOCUMENT ME!
     *
     * @param  modus  DOCUMENT ME!
     */
    private void setModus(final Modus modus) {
        this.modus = modus;
        refreshHandles();
    }

    /**
     * DOCUMENT ME!
     */
    private void refreshHandles() {
        if (getSelectedLinePFeature() != null) {
            switch (modus) {
                case MARK_SELECTION: {
                    showCursor(false);
                    showPointMarks(true);
//                    showSublines(true);
                    break;
                }
                case MARK_ADD: {
                    showCursor(true);
                    showPointMarks(true);
//                    showSublines(true);
                    break;
                }
//                case DRAWING_SUBLINE: {
//                    showCursor(true);
//                    showMarks(false);
//                    showSublines(true);
//                    break;
//                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Modus getModus() {
        return modus;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    @Override
    public void mouseMoved(final PInputEvent event) {
        if (mc.getInteractionMode().equals(MappingComponent.LINEAR_REFERENCING)) {
            final PFeature selPFeature = getSelectedLinePFeature();
            if (selPFeature != null) {
                updateCursor(event.getPosition());
                cursorPHandle.setMarkPosition(getCurrentPosition());
                if (event.isControlDown()) {
                    setModus(Modus.MARK_SELECTION);
                } else {
                    setModus(Modus.MARK_ADD);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  trigger  DOCUMENT ME!
     */
    private void updateCursor(final Point2D trigger) {
        final PFeature selPFeature = getSelectedLinePFeature();
        if (selPFeature != null) {
            final Geometry geom = selPFeature.getFeature().getGeometry();
            if (selPFeature != null) {
                final Coordinate triggerCoordinate = new Coordinate(
                        mc.getWtst().getSourceX(trigger.getX()),
                        mc.getWtst().getSourceY(trigger.getY()));
                final Geometry lineGeometry = LinearReferencedPointFeature.getReducedLineGeometry(
                        geom,
                        new Coordinate(cursorX, cursorY),
                        triggerCoordinate);
                final Coordinate erg = LinearReferencedPointFeature.getNearestCoordninateOnLine(
                        triggerCoordinate,
                        lineGeometry);
                cursorX = (float)mc.getWtst().getDestX(erg.x);
                cursorY = (float)mc.getWtst().getDestY(erg.y);
//                cursorPosition = getCurrentPosition();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getSelectedLinePFeature() {
        // Collection holen
        final FeatureCollection fc = mc.getFeatureCollection();
        // Selektierte Features holen
        final Collection<Feature> sel = fc.getSelectedFeatures();

        // wenn genau 1 Objekt selektiert ist
        if ((fc instanceof DefaultFeatureCollection) && (sel.size() == 1)) {
            // selektiertes feature holen
            final Feature[] sels = sel.toArray(new Feature[0]);
            final Geometry geom = sels[0].getGeometry();
            if ((geom != null) || (geom instanceof MultiLineString) || (geom instanceof LineString)) {
                // zugehöriges pfeature holen
                final PFeature pf = mc.getPFeatureHM().get(sels[0]);
                // zugehörige geometrie holen
                return pf;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Wrong geometry type: " + geom.getGeometryType());
                }
                return null;
            }
        } else {
            return null;
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class PointMark {

        //~ Instance fields ----------------------------------------------------

        private double position;
        private LinearReferencedPointMarkPHandle pHandle;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PointMark object.
         *
         * @param  position  DOCUMENT ME!
         * @param  handle    DOCUMENT ME!
         */
        PointMark(final double position, final LinearReferencedPointMarkPHandle handle) {
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
        public LinearReferencedPointMarkPHandle getPHandle() {
            return pHandle;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
// class LineMark {
//
// //~ Instance fields ----------------------------------------------------
//
// private double positionStart;
// private double positionEnd;
// private MarkLinePHandle pHandle;
// private PFeature pFeature;
//
// //~ Constructors -------------------------------------------------------
//
// /**
// * Creates a new LineMark object.
// *
// * @param  positionStart  DOCUMENT ME!
// * @param  positionEnd    DOCUMENT ME!
// * @param  pHandle        DOCUMENT ME!
// * @param  pFeature       DOCUMENT ME!
// */
// LineMark(final double positionStart,
// final double positionEnd,
// final MarkLinePHandle pHandle,
// final PFeature pFeature) {
// this.pHandle = pHandle;
// this.pFeature = pFeature;
// this.positionStart = positionStart;
// this.positionEnd = positionEnd;
// }
//
// //~ Methods ------------------------------------------------------------
//
// /**
// * DOCUMENT ME!
// *
// * @return  DOCUMENT ME!
// */
// public double getPositionStart() {
// return positionStart;
// }
//
// /**
// * DOCUMENT ME!
// *
// * @return  DOCUMENT ME!
// */
// public double getPositionEnd() {
// return positionEnd;
// }
//
// /**
// * DOCUMENT ME!
// *
// * @return  DOCUMENT ME!
// */
// public MarkLinePHandle getPHandle() {
// return pHandle;
// }
//
// /**
// * DOCUMENT ME!
// *
// * @return  DOCUMENT ME!
// */
// public PFeature getPFeature() {
// return pFeature;
// }
// }
}
