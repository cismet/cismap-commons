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

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.text.DecimalFormat;

import java.util.*;
import java.util.logging.Level;

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
import de.cismet.cismap.commons.features.CommonMultiAndSingleFeatureAction;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.DoubleClickableFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureNameProvider;
import de.cismet.cismap.commons.features.FeaturesProvider;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.SelectFeature;
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

    // Now some property change event types.
    // We need to synchronize the mode and the last feature between AbstractCreateSelectGeometryListeners.
    public static final String PROPERTY_LAST_FEATURE = "PROPERTY_LAST_FEATURE";
    public static final String PROPERTY_MODE = "PROPERTY_MODE";

    //~ Instance fields --------------------------------------------------------

    Point doubleclickPoint = null;
    PFeature sel = null;
    Collection<PFeature> pfVector = new ArrayList<>();
    ArrayList<? extends CommonFeatureAction> allCommonFeatureActions = null;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private int clickCount = 0;
    private Map<Feature, PFeature> selectedFeatures = Collections.synchronizedMap(new HashMap<Feature, PFeature>());
    private boolean selectMultipleFeatures = false;
    private boolean featuresFromServicesSelectable = false;
    private boolean selectionInProgress = false;
    private boolean featureAdded = false;
    private List<Feature> lastUnselectedFeatures;
    private boolean holdGeometries = false;
    private SelectFeature lastFeature;
    private SelectFeature selectFeature;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private boolean showHandleNeighbourDistance = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionListener object.
     */
    public SelectionListener() {
        final Lookup.Result<CommonFeatureAction> result = Lookup.getDefault().lookupResult(CommonFeatureAction.class);
        allCommonFeatureActions = new ArrayList<>(result.allInstances());
        Collections.sort(allCommonFeatureActions, new Comparator<CommonFeatureAction>() {

                @Override
                public int compare(final CommonFeatureAction o1, final CommonFeatureAction o2) {
                    return Integer.valueOf(o1.getSorter()).compareTo(Integer.valueOf(o2.getSorter()));
                }
            });
        setGeometryFeatureClass(PureNewFeature.class);
        setMode(RECTANGLE);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isShowHandleNeighbourDistance() {
        return showHandleNeighbourDistance;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  showHandleNeighbourDistance  DOCUMENT ME!
     */
    public void setShowHandleNeighbourDistance(final boolean showHandleNeighbourDistance) {
        this.showHandleNeighbourDistance = showHandleNeighbourDistance;
    }

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
     * DOCUMENT ME!
     *
     * @param  pInputEvent  DOCUMENT ME!
     */
    private void handleRightClick(final PInputEvent pInputEvent) {
        if (log.isDebugEnabled()) {
            log.debug("right mouseclick"); // NOI18N
        }
        boolean clickOnSelection = false;
        final boolean multiFeaturePopupEnabled = CismapBroker.getInstance().isMultiFeaturePopupMenuEnabled();

        final PFeature clickedPFeature = (PFeature)PFeatureTools.getFirstValidObjectUnderPointer(
                pInputEvent,
                new Class[] { PFeature.class },
                true);

        // COLLECT ALL PFEATURES UNDER THE POINTER
        final Collection<PFeature> allClickedPFeatures = (List)PFeatureTools.getAllValidObjectsUnderPointer(
                pInputEvent,
                new Class[] { PFeature.class });

        // COLLECT ALL SELECTED PFEATURES that are SelectableServiceFeatures
        final Collection<PFeature> selectedPFeatures = new ArrayList<>(selectedFeatures.size());
        for (final Feature selectedFeature : selectedFeatures.keySet()) {
            final PFeature selectedPFeature = selectedFeatures.get(selectedFeature);
            if (allClickedPFeatures.contains(selectedPFeature)) {
                clickOnSelection = true;
            }
            selectedPFeatures.add(selectedPFeature);
        }

        // COLLECT ALL SELECTED PFEATURES from the DefaultFeatureCollectipon
        final DefaultFeatureCollection dfc = ((DefaultFeatureCollection)mappingComponent.getFeatureCollection());
        for (final Feature selectedFeature : ((LinkedHashSet<Feature>)dfc.getSelectedFeatures())) {
            final PFeature selectedPFeature = mappingComponent.getPFeatureHM().get(selectedFeature);
            if (selectedPFeature != null) {
                if (allClickedPFeatures.contains(selectedPFeature)) {
                    clickOnSelection = true;
                }
                if (!selectedPFeatures.contains(selectedPFeature)) {
                    // do not add duplicates
                    selectedPFeatures.add(selectedPFeature);
                }
            }
        }

        final List<PFeature> pFeatures = new ArrayList<>();
        // IS ONE OF THE SELECTED PFEATURES UNDER THE POINTER ?
        if (clickOnSelection) {
            pFeatures.addAll(selectedPFeatures);
        } else if (multiFeaturePopupEnabled) {
            pFeatures.addAll(allClickedPFeatures);
        } else if (clickedPFeature != null) {
            pFeatures.add((PFeature)clickedPFeature);
        }

        // we build a popup menu from all the registered generic point actions
        final Point point = createPointFromInput(pInputEvent);

        final Collection<? extends MapPopupAction> lookupResult = Lookup.getDefault().lookupAll(MapPopupAction.class);
        final ArrayList<MapPopupAction> allPopupActions = new ArrayList<MapPopupAction>(lookupResult);
        Collections.sort(allPopupActions);

        ////
        final MultiHashMap actionProviderMap = new MultiHashMap();
        final MultiHashMap commonFeatureActionsMap = new MultiHashMap();
        final MultiHashMap popupActionMap = new MultiHashMap();
        final MultiHashMap multipleCommonFeatureActionProvider = new MultiHashMap();

        for (final PFeature pFeature : pFeatures) {
            if ((pFeature instanceof ActionsProvider)
                        && (multiFeaturePopupEnabled || pFeature.equals(clickedPFeature))) {
                final Collection<? extends Action> actions = ((ActionsProvider)pFeature.getFeature()).getActions();
                actionProviderMap.putAll(pFeature, actions);
            }

            if (allCommonFeatureActions != null) {
                for (final CommonFeatureAction cfaTemplate : allCommonFeatureActions) {
                    final CommonFeatureAction cfa;
                    try {
                        cfa = (cfaTemplate instanceof FeaturesProvider) ? cfaTemplate
                                                                        : cfaTemplate.getClass().newInstance();
                    } catch (final Exception ex) {
                        break;
                    }
                    // if ((cfa != null) && (pFeature != null)) {
                    cfa.setSourceFeature(pFeature.getFeature());
                    // }

                    if (cfa.isActive()) {
                        if (cfa instanceof CommonFeaturePreciseAction) {
                            final Point2D pos = pInputEvent.getPosition();
                            final WorldToScreenTransform wtst = getMappingComponent().getWtst();
                            final Coordinate coord = new Coordinate(wtst.getSourceX(pos.getX()),
                                    wtst.getSourceY(pos.getY()));
                            final Collection<Feature> allFeatures = new ArrayList();
                            for (final PFeature feature : (Collection<PFeature>)pFeatures) {
                                allFeatures.add(feature.getFeature());
                            }
                            ((CommonFeaturePreciseAction)cfa).setActionCoordinate(coord);
                            ((CommonFeaturePreciseAction)cfa).setAllSourceFeatures(allFeatures);
                        }

                        if (cfa instanceof FeaturesProvider) {
                            if (((FeaturesProvider)cfa).isResponsibleFor(pFeature.getFeature())) {
                                if (!(cfa instanceof CommonMultiAndSingleFeatureAction) || (pFeatures.size() > 1)) {
                                    multipleCommonFeatureActionProvider.put(cfa, pFeature.getFeature());
                                }
                            }

                            if ((cfa instanceof CommonMultiAndSingleFeatureAction)
                                        && (multiFeaturePopupEnabled || pFeature.equals(clickedPFeature))) {
                                final CommonFeatureAction cfa2;
                                try {
                                    cfa2 = cfaTemplate.getClass().newInstance();
                                } catch (final Exception ex) {
                                    break;
                                }
                                cfa2.setSourceFeature(pFeature.getFeature());

                                commonFeatureActionsMap.put(pFeature, cfa2);
                            }
                        } else if (multiFeaturePopupEnabled || pFeature.equals(clickedPFeature)) {
                            commonFeatureActionsMap.put(pFeature, cfa);
                        }
                    }
                }
            }

            for (final MapPopupAction popupAction : allPopupActions) {
                popupAction.setPoint(point);

                if (popupAction.isActive(pFeature instanceof PFeature)
                            && (multiFeaturePopupEnabled || pFeature.equals(clickedPFeature))) {
                    final JMenu submenu = popupAction.getSubmenu();
                    popupActionMap.put(pFeature, (submenu != null) ? submenu : popupAction);
                }
            }
        }

        final MultiHashMap menuMap = new MultiHashMap();
        for (final PFeature pFeature : pFeatures) {
            final Collection<Action> actionProviders = actionProviderMap.getCollection(pFeature);
            final Collection<CommonFeatureAction> commonFeatureActions = commonFeatureActionsMap.getCollection(
                    pFeature);
            final Collection<Object> popupActions = popupActionMap.getCollection(pFeature);

            if (actionProviders != null) {
                if (menuMap.getCollection(pFeature) != null) {
                    menuMap.put(pFeature, new JSeparator());
                }
                menuMap.putAll(pFeature, actionProviders);
            }
            if (commonFeatureActions != null) {
                if (menuMap.getCollection(pFeature) != null) {
                    menuMap.put(pFeature, new JSeparator());
                }
                menuMap.putAll(pFeature, commonFeatureActions);
            }

            if ((popupActions != null) && (popupActions.size() > 0)) {
                if (menuMap.getCollection(pFeature) != null) {
                    menuMap.put(pFeature, new JSeparator());
                }
                menuMap.putAll(pFeature, popupActions);
            }
        }

        final JPopupMenu popup = new JPopupMenu("MapPopup");
        final Collection<PFeature> menuMapPFeatures = (Set<PFeature>)menuMap.keySet();
        if (menuMapPFeatures.size() > 1) {
            for (final PFeature pFeature : menuMapPFeatures) {
                final String featureName;
                if ((pFeature.getFeature() instanceof FeatureNameProvider)
                            && (((FeatureNameProvider)pFeature.getFeature()).getName() != null)) {
                    featureName = ((FeatureNameProvider)pFeature.getFeature()).getName();
                } else {
                    featureName = java.util.ResourceBundle.getBundle(
                                "de/cismet/cismap/commons/gui/piccolo/eventlistener/Bundle")
                                .getString("SelectionListener.unknown_featureName");
                }
                final JMenu featurePopup = new JMenu(featureName + " | "
                                + new DecimalFormat("#.##").format(
                                    pFeature.getFeature().getGeometry().getArea()) + " m²");
                featurePopup.addMouseListener(new MouseAdapter() {

                        @Override
                        public void mouseClicked(final MouseEvent e) {
                            ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselectAll();
                            ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).addToSelection(
                                pFeature.getFeature());
                        }
                    });
                for (final Object menuItemComponent : menuMap.getCollection(pFeature)) {
                    addItemToMenu(menuItemComponent, featurePopup);
                }
                if (featurePopup.getMenuComponentCount() > 0) {
                    popup.add(featurePopup);
                } else {
                    final JMenuItem mi = new JMenuItem(featurePopup.getText());
                    mi.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(final ActionEvent e) {
                                ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselectAll();
                                ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).addToSelection(
                                    pFeature.getFeature());
                            }
                        });
                    popup.add(mi);
                }
            }
        } else if (menuMapPFeatures.size() > 0) {
            final Object menuMapPFeature = menuMapPFeatures.iterator().next();
            for (final Object menuItemComponent : menuMap.getCollection(menuMapPFeature)) {
                addItemToMenu(menuItemComponent, popup);
            }
        }

        if (!multipleCommonFeatureActionProvider.isEmpty() && (popup.getComponentCount() > 0)) {
            popup.add(new JSeparator());
        }
        for (final FeaturesProvider action
                    : (Collection<FeaturesProvider>)multipleCommonFeatureActionProvider.keySet()) {
            final Collection<Feature> multipleCommonFeature = multipleCommonFeatureActionProvider.getCollection(action);
            if (!multipleCommonFeature.isEmpty()) {
                action.setSourceFeatures(new ArrayList<>(multipleCommonFeature));
                addItemToMenu(action, popup);
            }
        }

        if (popup.getComponentCount() > 0) {
            popup.show(
                mappingComponent,
                (int)pInputEvent.getCanvasPosition().getX(),
                (int)pInputEvent.getCanvasPosition().getY());
        }
    }

    /**
     * Selektiere einen PNode.
     *
     * @param  pInputEvent  DOCUMENT ME!
     */
    @Override
    public void mouseClicked(final PInputEvent pInputEvent) {
        setMappingComponent(pInputEvent);
        super.mouseClicked(pInputEvent);

        if (pInputEvent.isRightMouseButton()) {
            if (!isInProgress()) {
                try {
                    handleRightClick(pInputEvent);
                } catch (RuntimeException e) {
                    log.error("Problem while creating context menu", e);

                    final ErrorInfo errorInfo = new ErrorInfo(
                            NbBundle.getMessage(SelectionListener.class, "SelectionListener.mouseClicked().title"),
                            NbBundle.getMessage(SelectionListener.class, "SelectionListener.mouseClicked().message"),
                            null,
                            null,
                            e,
                            Level.ALL,
                            null);
                    JXErrorPane.showDialog(CismapBroker.getInstance().getMappingComponent(), errorInfo);
                }
            }
        } else if (mode.equals(RECTANGLE) || mode.equals(ELLIPSE)) {
            selectionInProgress = true;

            try {
                if (log.isDebugEnabled()) {
                    log.debug("mouseClicked():" + pInputEvent.getPickedNode()); // NOI18N
                }
                clickCount = pInputEvent.getClickCount();
                if ((clickCount == 2) && pInputEvent.isLeftMouseButton()) {
                    doubleclickPoint = createPointFromInput(pInputEvent);

                    final PNotificationCenter pn = PNotificationCenter.defaultCenter();
                    pn.postNotification(SelectionListener.DOUBLECLICK_POINT_NOTIFICATION, this);
                }

                if (pInputEvent.getComponent() instanceof MappingComponent) {
                    mappingComponent = (MappingComponent)pInputEvent.getComponent();
                }

                final PFeature clickedPFeature = (PFeature)PFeatureTools.getFirstValidObjectUnderPointer(
                        pInputEvent,
                        new Class[] { PFeature.class },
                        true);

                try {
                    final Point2D point = mappingComponent.isSnappingEnabled()
                        ? PFeatureTools.getNearestPointInArea(
                                    mappingComponent,
                                    pInputEvent.getCanvasPosition(),
                                    true,
                                    null).getPoint() : pInputEvent.getPosition();

                    final AbstractNewFeature.geomTypes geomType = AbstractNewFeature.geomTypes.POINT;

                    final int currentSrid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs()
                                    .getCode());
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

                if (clickedPFeature != null) {
                    sel = clickedPFeature;

                    if (clickCount == 2) {
                        final Feature feature = sel.getFeature();
                        if (feature instanceof DoubleClickableFeature) {
                            if (pInputEvent.isLeftMouseButton()) {
                                ((DoubleClickableFeature)feature).doubleClickPerformed(this);
                            }
                        }
                    }
                }
            } finally {
                selectionInProgress = false;
            }
        }
    }

    @Override
    public MappingComponent getMappingComponent() {
        return mappingComponent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  menuItemComponent  DOCUMENT ME!
     * @param  menuOrPopup        DOCUMENT ME!
     */
    private static void addItemToMenu(final Object menuItemComponent, final Object menuOrPopup) {
        if (menuOrPopup instanceof JMenu) {
            final JMenu menu = (JMenu)menuOrPopup;
            if (menuItemComponent instanceof Action) {
                menu.add((Action)menuItemComponent);
            } else if (menuItemComponent instanceof Component) {
                menu.add((Component)menuItemComponent);
            } else if (menuItemComponent instanceof JMenuItem) {
                menu.add((JMenuItem)menuItemComponent);
            } else if (menuItemComponent instanceof PopupMenu) {
                menu.add((PopupMenu)menuItemComponent);
            } else if (menuItemComponent instanceof String) {
                menu.add((String)menuItemComponent);
            }
        } else if (menuOrPopup instanceof JPopupMenu) {
            final JPopupMenu popup = (JPopupMenu)menuOrPopup;
            if (menuItemComponent instanceof Action) {
                popup.add((Action)menuItemComponent);
            } else if (menuItemComponent instanceof Component) {
                popup.add((Component)menuItemComponent);
            } else if (menuItemComponent instanceof JMenuItem) {
                popup.add((JMenuItem)menuItemComponent);
            } else if (menuItemComponent instanceof PopupMenu) {
                popup.add((PopupMenu)menuItemComponent);
            } else if (menuItemComponent instanceof String) {
                popup.add((String)menuItemComponent);
            }
        }
    }

    /**
     * unselect all currently selected features. If featuresFromServicesSelectable = false, this method does nothing
     */
    private void unselectAll() {
        final List<Feature> allKeys = new ArrayList(selectedFeatures.keySet());
        for (final Feature f : allKeys) {
            if (featuresFromServicesSelectable || (f instanceof SelectableServiceFeature)) {
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
        if (featuresFromServicesSelectable || ((f != null) && (f.getFeature() instanceof SelectableServiceFeature))) {
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
        if (featuresFromServicesSelectable || ((pf != null) && (pf.getFeature() instanceof SelectableServiceFeature))) {
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
        if (featuresFromServicesSelectable || ((pf != null) && (pf.getFeature() instanceof SelectableServiceFeature))) {
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

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Color getSelectColor() {
        return getFillingColor();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public float getSelectTransparency() {
        return getFillingColor().getTransparency();
    }

    @Override
    protected Color getFillingColor() {
        return new Color(20, 20, 20, 20);
    }

    /**
     * DOCUMENT ME!
     */
    public void redoLastSelect() {
        select(lastFeature);
    }

    /**
     * DOCUMENT ME!
     */
    public void showLastFeature() {
        showFeature(lastFeature);
        getMappingComponent().getFeatureCollection().holdFeature(lastFeature);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    protected void showFeature(final SelectFeature feature) {
        if (feature != null) {
            feature.setEditable(feature.getGeometryType() != AbstractNewFeature.geomTypes.MULTIPOLYGON);

            getMappingComponent().getFeatureCollection().addFeature(feature);
            if (isHoldingGeometries()) {
                getMappingComponent().getFeatureCollection().holdFeature(feature);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newValue  DOCUMENT ME!
     */
    protected void setLastFeature(final SelectFeature newValue) {
        final SelectFeature oldValue = this.lastFeature;
        this.lastFeature = newValue;

        // Notify other AbstractCreateSelectGeometryListeners about the change.
        propertyChangeSupport.firePropertyChange(PROPERTY_LAST_FEATURE, oldValue, newValue);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public SelectFeature getLastSelectFeature() {
        return lastFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectFeature  DOCUMENT ME!
     */
    public void select(final SelectFeature selectFeature) {
        if (selectFeature != null) {
            setSelectFeature(selectFeature);
            final boolean selectExecuted = performSelect(selectFeature);
            if (selectExecuted) {
                setLastFeature(selectFeature);
                showFeature(selectFeature);
                cleanup(selectFeature);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    protected void cleanup(final SelectFeature feature) {
        final PFeature pFeature = (PFeature)getMappingComponent().getPFeatureHM().get(feature);
        if (isHoldingGeometries()) {
            pFeature.moveToFront(); // funktioniert nicht?!
            feature.setEditable(true);
            getMappingComponent().getFeatureCollection().holdFeature(feature);
        } else {
            getMappingComponent().getTmpFeatureLayer().addChild(pFeature);

            // Transparenz animieren
            pFeature.animateToTransparency(0, 2500);
            // warten bis Animation zu Ende ist um Feature aus Liste zu entfernen
            new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while (pFeature.getTransparency() > 0) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                            }
                        }
                        getMappingComponent().getFeatureCollection().removeFeature(feature);
                    }
                }).start();
        }
    }

    @Override
    public void setMode(final String newValue) throws IllegalArgumentException {
        final String oldValue = getMode();
        super.setMode(newValue);

        propertyChangeSupport.firePropertyChange(PROPERTY_MODE, oldValue, newValue);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   selectFeature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected boolean performSelect(final SelectFeature selectFeature) {
        final Geometry geom = selectFeature.getGeometry();
        mappingComponent.getHandleLayer().removeAllChildren();
        if ((geom != null)) {
            if (log.isDebugEnabled()) {
                // Hole alle PFeatures die das Markierviereck schneiden
                // und Hinzuf\u00FCgen dieser PFeatures zur Selektion
                log.debug("Markiergeometrie = " + geom.toText()); // NOI18N
            }

            if (!finishingEvent.isControlDown()) {
                lastUnselectedFeatures = null;
                ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselectAll();
                unselectAll();
                featureAdded = false;
            } else {
                featureAdded = true;
            }
            PFeature[] pfArr = new PFeature[0];

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
                            new Class[] { PFeature.class },
                            true);
                    if (o != null) {
                        pfArr = new PFeature[] { (PFeature)o };
                    }
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
            final List<Feature> toBeSelected = new ArrayList<>();
            final List<Feature> toBeUnselected = new ArrayList<>();

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
                        }
                        changeSelection(pf);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Feature cannot be selected");      // NOI18N
                    }
                    if (mappingComponent.getFeatureCollection() instanceof DefaultFeatureCollection) {
                        toBeUnselected.add(pf.getFeature());
                    }
                }
            }

            // Hier passierts
            ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).addToSelection(toBeSelected);
            if (log.isDebugEnabled()) {
                log.debug("toBeSelected.size:" + toBeSelected.size());
            }
            lastUnselectedFeatures = toBeUnselected;
            ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselect(toBeUnselected);

            pfVector = new ArrayList(((DefaultFeatureCollection)mappingComponent.getFeatureCollection())
                            .getSelectedFeatures());
            postSelectionChanged();
        }

        setLastFeature(selectFeature);
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isHoldingGeometries() {
        return holdGeometries;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newValue  DOCUMENT ME!
     */
    public void setHoldGeometries(final boolean newValue) {
        this.holdGeometries = newValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public SelectFeature getSelectFeature() {
        return selectFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectFeature  DOCUMENT ME!
     */
    protected void setSelectFeature(final SelectFeature selectFeature) {
        this.selectFeature = selectFeature;
    }

    @Override
    protected void finishGeometry(final AbstractNewFeature feature) {
        super.finishGeometry(feature);

        selectionInProgress = true;
        try {
            final Geometry geom = feature.getGeometry();
            performSelect(new SelectFeature(geom, MappingComponent.SELECT));
        } finally {
            selectionInProgress = false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pInputEvent  DOCUMENT ME!
     */
    private void setMappingComponent(final PInputEvent pInputEvent) {
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
        final List<PFeature> featureList = new ArrayList<>();
        featureList.addAll(selectedFeatures.values());
        return featureList;
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

    /**
     * DOCUMENT ME!
     *
     * @return  the featureAdded
     */
    public boolean isFeatureAdded() {
        return featureAdded;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureAdded  the featureAdded to set
     */
    public void setFeatureAdded(final boolean featureAdded) {
        this.featureAdded = featureAdded;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the lastUnselectedFeatures
     */
    public List<Feature> getLastUnselectedFeatures() {
        return lastUnselectedFeatures;
    }
}
