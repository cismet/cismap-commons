/*
 * SimpleSingleSelectionListener.java
 *
 * Created on 8. M\u00E4rz 2005, 15:24
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author hell
 */
public class SimpleSingleSelectionListener extends PBasicInputEventHandler {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public static final String SELECTION_CHANGED_NOTIFICATION = "SELECTION_CHANGED_NOTIFICATION";
    PFeature sel = null;
    private int clickCount = 0;
    MappingComponent mc = null;
    
    //Selektiere einen PNode
    @Override
    public void mouseClicked(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        log.debug("mouseClicked():" + pInputEvent.getPickedNode());
        Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[]{PFeature.class});
        clickCount = pInputEvent.getClickCount();
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            mc = (MappingComponent) pInputEvent.getComponent();
        }
        if (pInputEvent.getButton() == 3) {
            log.debug("right mouseclick");
            if (o instanceof PFeature && ((PFeature) o).getFeature() instanceof XStyledFeature) {
                XStyledFeature xf = (XStyledFeature) ((PFeature) o).getFeature();
                log.debug("valid object under pointer");
                JPopupMenu popup = new JPopupMenu("Test");
                JMenuItem m = new JMenuItem("TIM Merker anlegen");
                m.setIcon(xf.getIconImage());
                popup.add(m);
                popup.show(mc, (int) pInputEvent.getCanvasPosition().getX(), (int) pInputEvent.getCanvasPosition().getY());
            }
        } else {
            if (o instanceof PFeature) {
                super.mouseClicked(pInputEvent);
                sel = (PFeature) o;
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

    private void postSelectionChanged() {
        PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(SimpleSingleSelectionListener.SELECTION_CHANGED_NOTIFICATION, this);
        if (mc != null) {
            log.debug("unselectAll in postSelectionChanged()");
//            mc.getFeatureCollection().unselectAll(); //SINGLE SELECTION
            mc.getFeatureCollection().select(sel.getFeature());
        }
    }

    public PFeature getSelectedPFeature() {
        return sel;
    }

    public int getClickCount() {
        return clickCount;
    }
}
