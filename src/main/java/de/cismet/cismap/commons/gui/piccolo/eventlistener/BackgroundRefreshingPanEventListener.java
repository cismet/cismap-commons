/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * BackgroundRefreshingPanEventListener.java
 *
 * Created on 15. M\u00E4rz 2005, 11:22
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.nodes.PImage;

import java.awt.Image;

import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.CurrentStackTrace;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class BackgroundRefreshingPanEventListener extends PPanEventHandler {

    //~ Instance fields --------------------------------------------------------

    PImage pi;
    boolean rasterServiceLayerVisible = true;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new BackgroundRefreshingPanEventListener object.
     */
    public BackgroundRefreshingPanEventListener() {
        setAutopan(false);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void dragActivityFinalStep(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        // TODO
        // 1. DragBild unsichtbar machen
        // 2. Alle FeatureLayer die sichtbar sein sollen wieder sichtbar machen
        super.dragActivityFinalStep(pInputEvent);
//        if (pInputEvent.getComponent() instanceof SimpleFeatureViewer) {
//            ((SimpleFeatureViewer)pInputEvent.getComponent()).refreshBackground();
//        }
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            final MappingComponent mc = (MappingComponent)pInputEvent.getComponent();
            // mc.showHandles(false);
            if ((mc.getCismapPrefs() != null) && (mc.getCismapPrefs().getGlobalPrefs() != null)
                        && mc.getCismapPrefs().getGlobalPrefs().isPanPerformanceBoosterEnabled()
                        && (mc.getMappingModel().getFeatureServices().size() > 0)) {
                mc.getRasterServiceLayer().setVisible(rasterServiceLayerVisible);
                mc.getDragPerformanceImproverLayer().setVisible(false);
                mc.getDragPerformanceImproverLayer().removeAllChildren();
                for (int i = 0; i < mc.getFeatureServiceLayer().getChildrenCount(); ++i) {
                    final Object o = mc.getFeatureServiceLayer().getChild(i).getClientProperty("serviceLayer");      // NOI18N
                    boolean enabled = true;
                    if ((o != null) && (o instanceof ServiceLayer)) {
                        enabled = ((ServiceLayer)o).isEnabled() && mc.isBackgroundEnabled();
                    } else {
                        log.warn("konnte nicht feststellen ob ServiceLayer enabled war, deswegen auf true gesetzt"); // NOI18N
                    }
                    mc.getFeatureServiceLayer().getChild(i).setVisible(enabled);
                }
                mc.getFeatureLayer().setVisible(true);
            }
            mc.setNewViewBounds(mc.getCamera().getViewBounds());
            mc.queryServices();
        }
    }

    /**
     * Override this method to get notified when the drag activity starts stepping.
     *
     * @param  aEvent  DOCUMENT ME!
     */
    @Override
    protected void dragActivityFirstStep(final edu.umd.cs.piccolo.event.PInputEvent aEvent) {
        // 1. Schritt ein Bild des aktuellen PCanvas schiessen
        // 2. Dieses Bild als obersten Layer einblenden und richtig positionieren
        // 3. Alle FeatureLayer unsichtbar machen
        if (aEvent.getComponent() instanceof MappingComponent) {
            final MappingComponent mc = (MappingComponent)aEvent.getComponent();
//            mc.getHandleLayer().removeAllChildren();
            if ((mc.getCismapPrefs() != null) && (mc.getCismapPrefs().getGlobalPrefs() != null)
                        && mc.getCismapPrefs().getGlobalPrefs().isPanPerformanceBoosterEnabled()
                        && (mc.getMappingModel().getFeatureServices().size() > 0)) {
                if (log.isDebugEnabled()) {
                    log.debug("isPanPerformanceBoosterEnabled"); // NOI18N
                }
                refreshImage(mc);
                mc.getDragPerformanceImproverLayer().setVisible(true);
                mc.getRasterServiceLayer().setVisible(false);
                for (int i = 0; i < mc.getFeatureServiceLayer().getChildrenCount(); ++i) {
                    mc.getFeatureServiceLayer().getChild(i).setVisible(false);
                }
                mc.getFeatureLayer().setVisible(false);
            }
        }
        super.dragActivityFirstStep(aEvent);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mc  DOCUMENT ME!
     */
    private void refreshImage(final MappingComponent mc) {
        // Hier gibts bei gro\u00DFen Bildern noch ein Performanceproblem
        // kann dadurch gel\u00F6st werden indem man diese Bild schon vorher erzeugt und
        // hier nur noch \u00FCberpr\u00FCft ob es aktualisiert werden muss.
        // evtl auch nur einen einfachen Layer nehmen. Vielleicht bringts das auch schon
        rasterServiceLayerVisible = mc.getRasterServiceLayer().getVisible();
        final Image i = mc.getCamera().toImage();
        pi = new PImage(i);
        mc.getDragPerformanceImproverLayer().removeAllChildren();
        mc.getDragPerformanceImproverLayer().addChild(pi);
//        Point2D p2d=
//                new Point(0,0));
        pi.scale(1 / mc.getCamera().getViewScale());
        pi.setOffset(mc.getCamera().getViewBounds().getOrigin());
        pi.setTransparency(0.5f);
    }

    @Override
    public void mouseDragged(final PInputEvent e) {
        super.mouseDragged(e);
        CismapBroker.getInstance().fireMapBoundsChanged();
//        if (e.getCanvasDelta().getHeight() > 100 || e.getCanvasDelta().getWidth() > 100) {
//            log.fatal(e.getCanvasDelta()+""+e.getSourceSwingEvent().getSource(), new CurrentStackTrace());
//        } else {
//            log.error(e.getCanvasDelta()+""+e.getSourceSwingEvent().getSource(), new CurrentStackTrace());
//
//        }
    }
}
