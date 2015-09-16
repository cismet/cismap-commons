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
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.event.PNotificationCenter;

import org.apache.commons.collections.MultiHashMap;

import org.openide.util.Lookup;

import java.awt.Color;
import java.awt.Component;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import java.text.DecimalFormat;

import java.util.*;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.AbstractNewFeature;
import de.cismet.cismap.commons.features.CommonFeatureAction;
import de.cismet.cismap.commons.features.CommonFeaturePreciseAction;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureNameProvider;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.MapPopupAction;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.PFeatureTools;

import de.cismet.tools.gui.ActionsProvider;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class SelectionListener extends CreateGeometryListener {

    //~ Static fields/initializers ---------------------------------------------

    public static final String SELECTION_CHANGED_NOTIFICATION = "SELECTION_CHANGED_NOTIFICATION"; // NOI18N
    public static final String DOUBLECLICK_POINT_NOTIFICATION = "DOUBLECLICK_POINT_NOTIFICATION"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    Point doubleclickPoint = null;
    PFeature sel = null;
    Vector<PFeature> pfVector = new Vector<PFeature>();
    ArrayList<? extends CommonFeatureAction> commonFeatureActions = null;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private int clickCount = 0;
    private Map<Feature, PFeature> selectedFeatures = new HashMap<Feature, PFeature>();
    private boolean selectMultipleFeatures = false;
    private boolean featuresFromServicesSelectable = false;
    private boolean selectionInProgress = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionListener object.
     */
    public SelectionListener() {
        final Lookup.Result<CommonFeatureAction> result = Lookup.getDefault().lookupResult(CommonFeatureAction.class);
        commonFeatureActions = new ArrayList<CommonFeatureAction>(result.allInstances());
        Collections.sort(commonFeatureActions, new Comparator<CommonFeatureAction>() {

                @Override
                public int compare(final CommonFeatureAction o1, final CommonFeatureAction o2) {
                    return Integer.valueOf(o1.getSorter()).compareTo(Integer.valueOf(o2.getSorter()));
                }
            });
        setGeometryFeatureClass(PureNewFeature.class);
        setMode(RECTANGLE);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseMoved(final PInputEvent pInputEvent) {
        setMappingComponent(pInputEvent);
        super.mouseMoved(pInputEvent);
    }

    @Override
    public void mousePressed(final PInputEvent pInputEvent) {
        setMappingComponent(pInputEvent);
        super.mousePressed(pInputEvent);
    }

    /**
     * Selektiere einen PNode.
     *
     * @param  pInputEvent  DOCUMENT ME!
     */
    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        setMappingComponent(pInputEvent);
        super.mouseClicked(pInputEvent);

        if (mode.equals(RECTANGLE) || mode.equals(ELLIPSE)) {
            selectionInProgress = true;

            try {
                if (log.isDebugEnabled()) {
                    log.debug("mouseClicked():" + pInputEvent.getPickedNode()); // NOI18N
                }
                clickCount = pInputEvent.getClickCount();
                if (pInputEvent.getComponent() instanceof MappingComponent) {
                    mappingComponent = (MappingComponent)pInputEvent.getComponent();
                }

                if ((clickCount == 2) && pInputEvent.isLeftMouseButton()) {
                    doubleclickPoint = createPointFromInput(pInputEvent);

                    final PNotificationCenter pn = PNotificationCenter.defaultCenter();
                    pn.postNotification(SelectionListener.DOUBLECLICK_POINT_NOTIFICATION, this);
                }

                final List ouptemp = PFeatureTools.getAllValidObjectsUnderPointer(
                        pInputEvent,
                        new Class[] { PFeature.class });

                final List oup = new ArrayList();
                if (CismapBroker.getInstance().isMultiFeaturePopupMenuEnabled()) {
                    oup.addAll(ouptemp);
                } else {
                    oup.add(((ouptemp == null) || ouptemp.isEmpty()) ? null : ouptemp.get(0));
                }
                if (pInputEvent.isRightMouseButton()) {
                    if (log.isDebugEnabled()) {
                        log.debug("right mouseclick"); // NOI18N
                    }
                    final MultiHashMap menuMap = new MultiHashMap();
                    for (final Object o : oup) {
                        menuMap.put(o, null);
                        if (o instanceof PFeature) {
                            final PFeature pf = (PFeature)o;
                            if (pf.getFeature() instanceof ActionsProvider) {
                                final ActionsProvider ap = (ActionsProvider)((PFeature)o).getFeature();
                                final Collection<? extends Action> ac = ap.getActions();
                                for (final Action a : ac) {
                                    menuMap.put(o, a);
                                }
                            }

                            final JSeparator sep = new JSeparator();

                            if ((menuMap.getCollection(o) != null) && (menuMap.getCollection(o).size() > 1)) {
                                menuMap.put(o, sep);
                            }

                            int commonActionCounter = 0;
                            if (commonFeatureActions != null) {
                                for (final CommonFeatureAction cfaTemplate : commonFeatureActions) {
                                    CommonFeatureAction cfa = null;
                                    try {
                                        cfa = cfaTemplate.getClass().newInstance();
                                    } catch (final Exception ex) {
                                        break;
                                    }
                                    cfa.setSourceFeature(pf.getFeature());
                                    if (cfa instanceof CommonFeaturePreciseAction) {
                                        final Point2D pos = pInputEvent.getPosition();
                                        final WorldToScreenTransform wtst = getMappingComponent().getWtst();
                                        final Coordinate coord = new Coordinate(wtst.getSourceX(pos.getX()),
                                                wtst.getSourceY(pos.getY()));
                                        final Collection<Feature> allFeatures = new ArrayList();
                                        for (final PFeature feature : (Collection<PFeature>)oup) {
                                            allFeatures.add(feature.getFeature());
                                        }
                                        ((CommonFeaturePreciseAction)cfa).setActionCoordinate(coord);
                                        ((CommonFeaturePreciseAction)cfa).setAllSourceFeatures(allFeatures);
                                    }
                                    if (cfa.isActive()) {
                                        pf.getFeature().getGeometry().getArea();
                                        menuMap.put(o, cfa);
                                        commonActionCounter++;
                                    }
                                }
                            }
                            if ((commonActionCounter == 0) && (menuMap.getCollection(o) != null)) {
                                menuMap.getCollection(o).remove(sep);
                            }
                        }

                        // we build a popup menu from all the registered generic point actions
                        final Point point = createPointFromInput(pInputEvent);

                        final Collection<? extends MapPopupAction> lookupResult = Lookup.getDefault()
                                    .lookupAll(MapPopupAction.class);
                        final ArrayList<MapPopupAction> popupActions = new ArrayList<MapPopupAction>(lookupResult);
                        Collections.sort(popupActions);

                        boolean first = true;
                        for (final MapPopupAction action : popupActions) {
                            action.setPoint(point);

                            if (action.isActive(o instanceof PFeature)) {
                                if (first
                                            && ((menuMap.getCollection(o) != null)
                                                && (menuMap.getCollection(o).size() > 0))) {
                                    menuMap.put(o, new JSeparator());
                                    first = false;
                                }

                                final JMenu submenu = action.getSubmenu();

                                if (submenu != null) {
                                    menuMap.put(o, submenu);
                                } else {
                                    menuMap.put(o, action);
                                }
                            }
                        }
                    }

                    final JPopupMenu popup = new JPopupMenu("MapPopup");
                    if (menuMap.keySet().size() > 1) {
                        for (final Object o : menuMap.keySet()) {
                            final String featureName;
                            if ((o instanceof PFeature)
                                        && (((PFeature)o).getFeature() instanceof FeatureNameProvider)
                                        && (((FeatureNameProvider)((PFeature)o).getFeature()).getName() != null)) {
                                featureName = ((FeatureNameProvider)((PFeature)o).getFeature()).getName();
                            } else {
                                featureName = java.util.ResourceBundle.getBundle(
                                            "de/cismet/cismap/commons/gui/piccolo/eventlistener/Bundle")
                                            .getString("SelectionListener.unknown_featureName");
                            }
                            final JMenu featurePopup = new JMenu(featureName + " | "
                                            + new DecimalFormat("#.##").format(
                                                ((PFeature)o).getFeature().getGeometry().getArea()) + " m²");
                            featurePopup.addMouseListener(new MouseAdapter() {

                                    @Override
                                    public void mouseClicked(final MouseEvent e) {
                                        ((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
                                                .unselectAll();
                                        ((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
                                                .addToSelection(((PFeature)o).getFeature());
                                    }
                                });
                            for (final Object i : menuMap.getCollection(o)) {
                                if (i instanceof Action) {
                                    featurePopup.add((Action)i);
                                } else if (i instanceof Component) {
                                    featurePopup.add((Component)i);
                                } else if (i instanceof JMenuItem) {
                                    featurePopup.add((JMenuItem)i);
                                } else if (i instanceof PopupMenu) {
                                    featurePopup.add((PopupMenu)i);
                                } else if (i instanceof String) {
                                    featurePopup.add((String)i);
                                }
                            }
                            if (featurePopup.getMenuComponentCount() > 0) {
                                popup.add(featurePopup);
                            } else {
                                final JMenuItem mi = new JMenuItem(featurePopup.getText());
                                mi.addActionListener(new ActionListener() {

                                        @Override
                                        public void actionPerformed(final ActionEvent e) {
                                            ((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
                                                    .unselectAll();
                                            ((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
                                                    .addToSelection(((PFeature)o).getFeature());
                                        }
                                    });
                                popup.add(mi);
                            }
                        }
                    } else if (menuMap.keySet().size() > 0) {
                        final Object o = menuMap.keySet().iterator().next();
                        for (final Object i : menuMap.getCollection(o)) {
                            if (i instanceof Action) {
                                popup.add((Action)i);
                            } else if (i instanceof Component) {
                                popup.add((Component)i);
                            } else if (i instanceof JMenuItem) {
                                popup.add((JMenuItem)i);
                            } else if (i instanceof PopupMenu) {
                                popup.add((PopupMenu)i);
                            } else if (i instanceof String) {
                                popup.add((String)i);
                            }
                        }
                    }

                    if (popup.getComponentCount() > 0) {
                        popup.show(
                            mappingComponent,
                            (int)pInputEvent.getCanvasPosition().getX(),
                            (int)pInputEvent.getCanvasPosition().getY());
                    }
                } else {
                    final Object o = oup.isEmpty() ? null : oup.get(0);
                    if (o instanceof PFeature) {
                        super.mouseClicked(pInputEvent);
                        sel = (PFeature)o;

                        try {
                            Point2D point = null;
                            if (mappingComponent.isSnappingEnabled()) {
                                final boolean vertexRequired = mappingComponent.isSnappingOnLineEnabled();
                                point = PFeatureTools.getNearestPointInArea(
                                        mappingComponent,
                                        pInputEvent.getCanvasPosition(),
                                        vertexRequired,
                                        true);
                            }
                            if (point == null) {
                                point = pInputEvent.getPosition();
                            }

                            final AbstractNewFeature.geomTypes geomType = AbstractNewFeature.geomTypes.POINT;

                            final int currentSrid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance()
                                            .getSrs().getCode());
                            final AbstractNewFeature newFeature = new PureNewFeature(point, mappingComponent.getWtst());
                            newFeature.setGeometryType(geomType);
                            newFeature.getGeometry().setSRID(currentSrid);
                            final Geometry geom = CrsTransformer.transformToGivenCrs(newFeature.getGeometry(),
                                    mappingComponent.getMappingModel().getSrs().getCode());
                            newFeature.setGeometry(geom);

                            finishingEvent = pInputEvent;
                            finishGeometry(newFeature);
                        } catch (Throwable throwable) {
                            log.error("Error during the creation of the geometry", throwable); // NOI18N
                        }

                        if (pInputEvent.getClickCount() == 2) {
                            if (sel.getFeature() instanceof SearchFeature) {
                                final SearchFeature searchFeature = (SearchFeature)sel.getFeature();
                                if (pInputEvent.isLeftMouseButton()) {
                                    ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselectAll();
                                    mappingComponent.getHandleLayer().removeAllChildren();
                                    // neue Suche mit Geometry auslösen
                                    ((AbstractCreateSearchGeometryListener)mappingComponent.getInputListener(
                                            searchFeature.getInteractionMode())).search(searchFeature);
                                }
                            }
                        }
                    } else {
                        if (mappingComponent.getFeatureCollection() instanceof DefaultFeatureCollection) {
                            ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselectAll();
                        }
                        unselectAll();
                    }
                }
            } finally {
                selectionInProgress = false;
            }
        }
    }

    /**
     * unselect all currently selected features. If featuresFromServicesSelectable = false, this method does nothing
     */
    private void unselectAll() {
        if (featuresFromServicesSelectable) {
            final List<Feature> allKeys = new ArrayList(selectedFeatures.keySet());
            for (final Feature f : allKeys) {
                final PFeature pf = selectedFeatures.remove(f);
                if (pf != null) {
                    pf.setSelected(false);
                }
            }
        }
    }

    /**
     * changes the selection of the given pfeature If featuresFromServicesSelectable = false, this method does nothing.
     *
     * @param  f  the feature that should be change its selection
     */
    private void changeSelection(final PFeature f) {
        if (featuresFromServicesSelectable) {
            f.setSelected(!isSelected(f));

            if (f.isSelected()) {
                selectedFeatures.put(f.getFeature(), f);
            } else {
                selectedFeatures.remove(f.getFeature());
            }
        }
    }

    /**
     * checks, if the given feature is currently selected.
     *
     * @param   f  the feature to check
     *
     * @return  true, if the given feature is currently selected
     */
    private boolean isSelected(final PFeature f) {
        return selectedFeatures.get(f.getFeature()) != null;
    }

    /**
     * Adds the given feature to the selected features. The select flag of the pfeature will not be changed from this
     * method.
     *
     * @param  pf  the feature to add
     */
    public void addSelectedFeature(final PFeature pf) {
        if (featuresFromServicesSelectable) {
            selectedFeatures.put(pf.getFeature(), pf);
        }
    }

    /**
     * Removes the given feature from the selected features. The select flag of the pfeature will not be changed from
     * this method.
     *
     * @param  pf  the feature to remove
     */
    public void removeSelectedFeature(final PFeature pf) {
        if (featuresFromServicesSelectable) {
            selectedFeatures.remove(pf.getFeature());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featuresFromServicesSelectable  DOCUMENT ME!
     */
    public void setFeaturesFromServicesSelectable(final boolean featuresFromServicesSelectable) {
        this.featuresFromServicesSelectable = featuresFromServicesSelectable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   event  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Point createPointFromInput(final PInputEvent event) {
        final Point2D pos = event.getPosition();
        final WorldToScreenTransform wtst = getMappingComponent().getWtst();
        final Coordinate coord = new Coordinate(wtst.getSourceX(pos.getX()), wtst.getSourceY(pos.getY()));
        final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                CrsTransformer.getCurrentSrid());

        return gf.createPoint(coord);
    }

    @Override
    public void mouseDragged(final PInputEvent e) {
        setMappingComponent(e);
        super.mouseDragged(e); // To change body of generated methods, choose Tools | Templates.

        clickCount = e.getClickCount();
    }

    /**
     * Wird gefeuert, wenn die Maustaste nach dem Ziehen des Markiervierecks losgelassen wird.
     *
     * @param  event  das Mouseevent (als PInputEvent)
     */
    @Override
    public void mouseReleased(final PInputEvent event) {
        setMappingComponent(event);
        super.mouseReleased(event);
    }

    @Override
    protected Color getFillingColor() {
        return new Color(20, 20, 20, 20);
    }

    @Override
    protected void finishGeometry(final AbstractNewFeature feature) {
        super.finishGeometry(feature);
        selectionInProgress = true;
        final Geometry geom = feature.getGeometry();
        try {
            mappingComponent.getHandleLayer().removeAllChildren();
            if ((geom != null)) {
                if (log.isDebugEnabled()) {
                    // Hole alle PFeatures die das Markierviereck schneiden
                    // und Hinzuf\u00FCgen dieser PFeatures zur Selektion
                    log.debug("Markiergeometrie = " + geom.toText()); // NOI18N
                }

                if (!finishingEvent.isControlDown()) {
                    ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselectAll();
                    unselectAll();
                }
                PFeature[] pfArr;

                if (geom.getGeometryType().equalsIgnoreCase("point")) {
                    // getAllValidObjectsUnderPointer: Uses the pnodes to check, if a PFeature intersects the given
                    // point
                    if (isSelectMultipleFeatures()) {
                        pfArr = (PFeature[])PFeatureTools.getAllValidObjectsUnderPointer(
                                    finishingEvent,
                                    new Class[] { PFeature.class }).toArray(new PFeature[0]);
                    } else {
                        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(
                                finishingEvent,
                                new Class[] { PFeature.class });
                        pfArr = new PFeature[] { (PFeature)o };
                    }
                } else {
                    // getPFeaturesInArea: Uses the geometry of the underlying features to check, if a PFeature
                    // intersects the given area. So it is almost impossible to match a point feature. Even if it is
                    // displayed by a big symbol. For this reason, getPFeaturesInArea should only be used, if geom is an
                    // area and not just a point.
                    pfArr = PFeatureTools.getPFeaturesInArea(
                            mappingComponent,
                            geom);
                }
                final Vector<Feature> toBeSelected = new Vector<Feature>();
                final Vector<Feature> toBeUnselected = new Vector<Feature>();

                for (final PFeature pf : pfArr) {
                    if (pf.getFeature().canBeSelected()) {
                        if (mappingComponent.getFeatureCollection() instanceof DefaultFeatureCollection) {
                            if (
                                !((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).isSelected(
                                            pf.getFeature())) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Feature markiert: " + pf); // NOI18N
                                }
                                toBeSelected.add(pf.getFeature());
                            } else {
                                toBeUnselected.add(pf.getFeature());
                                // mappingComponent.getFeatureCollection().unselect(pf.getFeature()); //war vorher
                                // unselectAll()
                            }
                            changeSelection(pf);
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Feature cannot be selected");      // NOI18N
                        }
                        if (mappingComponent.getFeatureCollection() instanceof DefaultFeatureCollection) {
                            toBeUnselected.add(pf.getFeature());
                            // ((DefaultFeatureCollection)
                            // mappingComponent.getFeatureCollection()).unselect(pf.getFeature());//war vorher
                            // unselectAll()
                        }
                    }
                }

                // Hier passierts
                ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).addToSelection(
                    toBeSelected);
                ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselect(toBeUnselected);

                pfVector = new Vector(((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
                                .getSelectedFeatures());
                postSelectionChanged();
            }
        } finally {
            selectionInProgress = false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pInputEvent  DOCUMENT ME!
     */
    private void setMappingComponent(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (getMappingComponent() == null) {
            super.setMappingComponent((MappingComponent)pInputEvent.getComponent());
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void postSelectionChanged() {
        if (log.isDebugEnabled()) {
            log.debug("postSelectionChanged"); // NOI18N
        }
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(SelectionListener.SELECTION_CHANGED_NOTIFICATION, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  all currently selected PFeature object
     */
    public List<PFeature> getAllSelectedPFeatures() {
        final List<PFeature> featureList = new ArrayList<PFeature>();
        featureList.addAll(selectedFeatures.values());
        return featureList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  the returned vector does not only contains pfeatures. Use getAllSelectedPFeatures instead
     */
    public Vector<PFeature> getSelectedPFeatures() {
        return pfVector;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Deprecated
    public PFeature getSelectedPFeature() {
        return sel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getAffectedPFeature() {
        return sel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getClickCount() {
        return clickCount;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Point getDoubleclickPoint() {
        return doubleclickPoint;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the selectionInProgress
     */
    public boolean isSelectionInProgress() {
        return selectionInProgress;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectionInProgress  the selectionInProgress to set
     */
    public void setSelectionInProgress(final boolean selectionInProgress) {
        this.selectionInProgress = selectionInProgress;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the selectMultipleFeatures
     */
    public boolean isSelectMultipleFeatures() {
        return selectMultipleFeatures;
    }

    /**
     * If this is false, a simgle click on a feature selects only the first feature.
     *
     * @param  selectMultipleFeatures  the selectMultipleFeatures to set
     */
    public void setSelectMultipleFeatures(final boolean selectMultipleFeatures) {
        this.selectMultipleFeatures = selectMultipleFeatures;
    }
}
