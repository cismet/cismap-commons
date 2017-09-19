/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * SimpleSingleSelectionListener.java
 *
 * Created on 8. M\u00E4rz 2005, 15:24
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolox.event.PNotificationCenter;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class SimpleSingleSelectionListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    public static final String SELECTION_CHANGED_NOTIFICATION = "SELECTION_CHANGED_NOTIFICATION"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    PFeature sel = null;
    MappingComponent mc = null;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private int clickCount = 0;

    //~ Methods ----------------------------------------------------------------

    // Selektiere einen PNode
    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (log.isDebugEnabled()) {
            log.debug("mouseClicked():" + pInputEvent.getPickedNode());   // NOI18N
        }
        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(
                pInputEvent,
                new Class[] { PFeature.class },
                true);
        clickCount = pInputEvent.getClickCount();
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            mc = (MappingComponent)pInputEvent.getComponent();
        }
        if (pInputEvent.getButton() == 3) {
            if (log.isDebugEnabled()) {
                log.debug("right mouseclick");                            // NOI18N
            }
            if ((o instanceof PFeature) && (((PFeature)o).getFeature() instanceof XStyledFeature)) {
                final XStyledFeature xf = (XStyledFeature)((PFeature)o).getFeature();
                if (log.isDebugEnabled()) {
                    log.debug("valid object under pointer");              // NOI18N
                }
                final JPopupMenu popup = new JPopupMenu(org.openide.util.NbBundle.getMessage(
                            SimpleSingleSelectionListener.class,
                            "SimpleSingleSelectionListener.popup.text")); // NOI18N
                final JMenuItem m = new JMenuItem(org.openide.util.NbBundle.getMessage(
                            SimpleSingleSelectionListener.class,
                            "SimpleSingleSelectionListener.m.text"));     // NOI18N
                m.setIcon(xf.getIconImage());
                popup.add(m);
                popup.show(
                    mc,
                    (int)pInputEvent.getCanvasPosition().getX(),
                    (int)pInputEvent.getCanvasPosition().getY());
            }
        } else {
            if (o instanceof PFeature) {
                super.mouseClicked(pInputEvent);
                sel = (PFeature)o;
                postSelectionChanged();
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
     * DOCUMENT ME!
     */
    private void postSelectionChanged() {
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(SimpleSingleSelectionListener.SELECTION_CHANGED_NOTIFICATION, this);
        if (mc != null) {
            if (log.isDebugEnabled()) {
                log.debug("unselectAll in postSelectionChanged()"); // NOI18N
            }
//            mc.getFeatureCollection().unselectAll(); //SINGLE SELECTION
            mc.getFeatureCollection().select(sel.getFeature());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getSelectedPFeature() {
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
