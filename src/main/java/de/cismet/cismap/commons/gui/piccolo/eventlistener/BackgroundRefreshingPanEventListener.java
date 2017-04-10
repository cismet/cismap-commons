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

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.nodes.PImage;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JComponent;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.StaticDebuggingTools;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class BackgroundRefreshingPanEventListener extends PPanEventHandler implements PropertyChangeListener {

    //~ Instance fields --------------------------------------------------------

    PImage pi;
    boolean rasterServiceLayerVisible = true;

    private boolean imageBoosterActive = false;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private List<PNode> nodesToEnable = new ArrayList<PNode>();
    private MappingComponent mappingComponent;
    private volatile Image image = null;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private boolean panStarted = false;

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
        panStarted = false;
//        if (pInputEvent.getComponent() instanceof SimpleFeatureViewer) {
//            ((SimpleFeatureViewer)pInputEvent.getComponent()).refreshBackground();
//        }
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            final MappingComponent mc = (MappingComponent)pInputEvent.getComponent();

            // mc.showHandles(false);
            if (imageBoosterActive) {
//                if (mappingComponent == null) {
//                    mappingComponent = mc;
//                    mc.getCamera().addPropertyChangeListener(this);
//                }
                mc.getRasterServiceLayer().setVisible(rasterServiceLayerVisible);
                mc.getDragPerformanceImproverLayer().setVisible(false);
                mc.getDragPerformanceImproverLayer().removeAllChildren();
                for (final PNode node : nodesToEnable) {
                    node.setVisible(true);
                }
                mc.getFeatureLayer().setVisible(true);
            }
            final Rectangle2D oldBounds = mc.getViewBounds();
            final Rectangle2D newBounds = mc.getCamera().getViewBounds();
            if (!newBounds.equals(oldBounds)) {
                mc.setNewViewBounds(newBounds);
                mc.queryServices();
            }
        }
//        propertyChange(null);
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
        panStarted = true;
        if (aEvent.getComponent() instanceof MappingComponent) {
            final MappingComponent mc = (MappingComponent)aEvent.getComponent();
//            mc.getHandleLayer().removeAllChildren();
            imageBoosterActive = StaticDebuggingTools.checkHomeForFile("panPerformanceBooster");

            if (imageBoosterActive) {
                if (log.isDebugEnabled()) {
                    log.debug("isPanPerformanceBoosterEnabled"); // NOI18N
                }
                refreshImage(mc);
                mc.getDragPerformanceImproverLayer().setVisible(true);
                mc.getRasterServiceLayer().setVisible(false);
                for (int i = 0; i < mc.getMapServiceLayer().getChildrenCount(); ++i) {
                    final PNode tmp = mc.getMapServiceLayer().getChild(i);
                    if (tmp.getVisible()) {
                        tmp.setVisible(false);
                        nodesToEnable.add(tmp);
                    }
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
//        lock.readLock().lock();
//        try {
//            if (image == null) {
        image = mc.getCamera().toImage();
//            }
        pi = new PImage(image);
//        } finally {
//            lock.readLock().unlock();
//        }
        mc.getDragPerformanceImproverLayer().removeAllChildren();
        mc.getDragPerformanceImproverLayer().addChild(pi);
//        Point2D p2d=
//                new Point(0,0));
        pi.scale(1 / mc.getCamera().getViewScale());
        pi.setOffset(mc.getCamera().getViewBounds().getOrigin());
        pi.setTransparency(0.5f);
    }

    /**
     * Draws an image from a component.
     *
     * @param   component  DOCUMENT ME!
     *
     * @return  the given component as image
     */
    private BufferedImage componentToImage(final JComponent component) {
        final BufferedImage img = new BufferedImage(component.getWidth(),
                component.getHeight(),
                BufferedImage.TYPE_INT_ARGB_PRE);
        final Graphics g = img.getGraphics();
        g.setColor(component.getForeground());
        g.setFont(component.getFont());
        component.paintAll(g);

        return img.getSubimage(0, 0, img.getWidth(), img.getHeight());
    }

    @Override
    public void mouseDragged(final PInputEvent e) {
        super.mouseDragged(e);
        CismapBroker.getInstance().fireMapBoundsChanged();
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (!panStarted) {
            image = null;
            new Thread(new Runnable() {

                    @Override
                    public void run() {
                        lock.writeLock().lock();

                        try {
                            image = mappingComponent.getCamera().toImage();
//                            image = componentToImage(mappingComponent);
                        } finally {
                            lock.writeLock().unlock();
                        }
                    }
                }).start();
        }
    }
}
