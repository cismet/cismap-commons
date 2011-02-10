/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.event.PNotificationCenter;

import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import de.cismet.cismap.commons.features.CommonFeatureAction;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;

import de.cismet.tools.gui.ActionsProvider;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class SelectionListener extends RectangleRubberBandListener {

    //~ Static fields/initializers ---------------------------------------------

    public static final String SELECTION_CHANGED_NOTIFICATION = "SELECTION_CHANGED_NOTIFICATION"; // NOI18N
    private static final double RECT_BUFFER = 0.1d;

    //~ Instance fields --------------------------------------------------------

    PFeature sel = null;
    Vector<PFeature> pfVector = new Vector<PFeature>();
    MappingComponent mc = null;
    ArrayList<? extends CommonFeatureAction> commonFeatureActions = null;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private int clickCount = 0;

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
    }

    //~ Methods ----------------------------------------------------------------

    // Selektiere einen PNode
    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (log.isDebugEnabled()) {
            log.debug("mouseClicked():" + pInputEvent.getPickedNode()); // NOI18N
        }
        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[] { PFeature.class });
        clickCount = pInputEvent.getClickCount();
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            mc = (MappingComponent)pInputEvent.getComponent();
        }

        if (pInputEvent.isRightMouseButton()) {
            if (log.isDebugEnabled()) {
                log.debug("right mouseclick"); // NOI18N
            }
            final JPopupMenu popup = new JPopupMenu("Test");

            if ((o instanceof PFeature)) {
                final PFeature pf = ((PFeature)o);
                if (pf.getFeature() instanceof ActionsProvider) {
                    final ActionsProvider ap = (ActionsProvider)((PFeature)o).getFeature();
                    final Collection<? extends Action> ac = ap.getActions();
                    for (final Action a : ac) {
                        popup.add(a);
                    }
                }

                final JSeparator sep = new JSeparator();

                if (popup.getComponentCount() > 0) {
                    popup.add(sep);
                }

                int commonActionCounter = 0;
                if (commonFeatureActions != null) {
                    for (final CommonFeatureAction cfa : commonFeatureActions) {
                        cfa.setSourceFeature(pf.getFeature());
                        if (cfa.isActive()) {
                            popup.add(cfa);
                            commonActionCounter++;
                        }
                    }
                }
                if (commonActionCounter == 0) {
                    popup.remove(sep);
                }
                if (popup.getComponentCount() > 0) {
                    popup.show(
                        mc,
                        (int)pInputEvent.getCanvasPosition().getX(),
                        (int)pInputEvent.getCanvasPosition().getY());
                }
            }

//            if (o instanceof PFeature && ((PFeature)o).getFeature() instanceof XStyledFeature) {
//                XStyledFeature xf=(XStyledFeature)((PFeature)o).getFeature();
//                log.debug("valid object under pointer");
//                JPopupMenu popup=new JPopupMenu("Test");
//                JMenuItem m=new JMenuItem("TIM Merker anlegen");
//                m.setIcon(xf.getIconImage());
//                popup.add(m);
//                popup.show(mc,(int)pInputEvent.getCanvasPosition().getX(),(int)pInputEvent.getCanvasPosition().getY());
//            }
        } else {
            if (o instanceof PFeature) {
                super.mouseClicked(pInputEvent);
                sel = (PFeature)o;
                if (sel.getFeature().canBeSelected()) {
                    if ((mc != null) && pInputEvent.isLeftMouseButton() && pInputEvent.isControlDown()) {
                        if (mc.getFeatureCollection() instanceof DefaultFeatureCollection) {
                            if (!((DefaultFeatureCollection)mc.getFeatureCollection()).isSelected(sel.getFeature())) {
                                ((DefaultFeatureCollection)mc.getFeatureCollection()).addToSelection(sel.getFeature());
                            } else {
                                ((DefaultFeatureCollection)mc.getFeatureCollection()).unselect(sel.getFeature());
                            }
                        } else {
                            log.warn("mc.getFeatureCollection() instanceof DefaultFeatureCollection == false !!!!!!!"); // NOI18N
                        }
                    } else {
                        mc.getFeatureCollection().select(sel.getFeature());
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Feature cannot be selected");                                                        // NOI18N
                    }
                    if (mc.getFeatureCollection() instanceof DefaultFeatureCollection) {
                        ((DefaultFeatureCollection)mc.getFeatureCollection()).unselectAll();
                    }
                }
                postSelectionChanged();
                if (pInputEvent.getClickCount() == 2) {
                    if (sel.getFeature() instanceof SearchFeature) {
                        if (pInputEvent.isLeftMouseButton()) {
                            ((DefaultFeatureCollection)mc.getFeatureCollection()).unselectAll();
                            mc.getHandleLayer().removeAllChildren();
                            // neue Suche mit Geometry auslösen
                            ((CreateSearchGeometryListener)mc.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON))
                                    .search((SearchFeature)sel.getFeature());
                        }
                    }
                }
            } else {
                if (mc.getFeatureCollection() instanceof DefaultFeatureCollection) {
                    ((DefaultFeatureCollection)mc.getFeatureCollection()).unselectAll();
                }
            }
//        else if(o instanceof ParentNodeIsAPFeature && o instanceof PNode) {
//            super.mouseClicked(pInputEvent);
//            Object test=o;
//            do {
//                test=((PNode)test).getParent();
//            }
//            while(!(test instanceof PFeature));
//            if (test instanceof PFeature) {
//                sel=(PFeature)test;
//            }
//            postSelectionChanged();
//        }
        }
    }

    /**
     * Wird gefeuert, wenn die Maustaste nach dem Ziehen des Markiervierecks losgelassen wird.
     *
     * @param  event  das Mouseevent (als PInputEvent)
     */
    @Override
    public void mouseReleased(final PInputEvent event) {
        super.mouseReleased(event);
        if (event.getButton() == 1) { // linke Maustaste
            // Mouseevent muss von einer MappingComponent gefeuert werden
            if (event.getComponent() instanceof MappingComponent) {
                mc = (MappingComponent)event.getComponent();
                mc.getHandleLayer().removeAllChildren();
                // einfacher Klick ohne ziehen des Markiervierecks
                if ((rectangle != null)
                            && !((rectangle.getWidth() < RECT_BUFFER) || (rectangle.getHeight() < RECT_BUFFER))) {
                    if (log.isDebugEnabled()) {
                        // Hole alle PFeatures die das Markierviereck schneiden
                        // und Hinzuf\u00FCgen dieser PFeatures zur Selektion
                        log.debug("Markierviereck = (X=" + rectangle.getBounds().getX() + ",Y="
                                    + rectangle.getBounds().getY() // NOI18N
                                    + ",W=" + rectangle.getBounds().getWidth() + ",H="
                                    + rectangle.getBounds().getHeight() + ")"); // NOI18N
                    }
                    ((DefaultFeatureCollection)mc.getFeatureCollection()).unselectAll();
                    final PFeature[] pfArr = PFeatureTools.getPFeaturesInArea(mc, rectangle.getBounds());
                    final Vector<Feature> toBeSelected = new Vector<Feature>();
                    final Vector<Feature> toBeUnselected = new Vector<Feature>();

                    for (final PFeature pf : pfArr) {
                        if (pf.getFeature().canBeSelected()) {
                            if (mc.getFeatureCollection() instanceof DefaultFeatureCollection) {
                                if (!((DefaultFeatureCollection)mc.getFeatureCollection()).isSelected(
                                                pf.getFeature())) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Feature markiert: " + pf); // NOI18N
                                    }
                                    toBeSelected.add(pf.getFeature());
                                } else {
                                    toBeUnselected.add(pf.getFeature());
                                    // mc.getFeatureCollection().unselect(pf.getFeature()); //war vorher unselectAll()
                                }
                            }
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Feature cannot be selected");      // NOI18N
                            }
                            if (mc.getFeatureCollection() instanceof DefaultFeatureCollection) {
                                toBeUnselected.add(pf.getFeature());
                                // ((DefaultFeatureCollection)
                                // mc.getFeatureCollection()).unselect(pf.getFeature());//war vorher unselectAll()
                            }
                        }
                    }

                    // Hier passierts
                    ((DefaultFeatureCollection)mc.getFeatureCollection()).addToSelection(toBeSelected);
                    ((DefaultFeatureCollection)mc.getFeatureCollection()).unselect(toBeUnselected);

                    pfVector = new Vector(((DefaultFeatureCollection)mc.getFeatureCollection()).getSelectedFeatures());
                    postSelectionChanged();
                }
            }
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
     * @return  DOCUMENT ME!
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
}
