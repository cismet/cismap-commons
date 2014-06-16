/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * FeatureMoveListener.java
 *
 * Created on 18. April 2005, 14:57
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import edu.umd.cs.piccolox.util.PLocator;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;

import java.util.Iterator;
import java.util.Vector;

import de.cismet.cismap.commons.features.AbstractNewFeature;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.PHandle;
import de.cismet.cismap.commons.gui.piccolo.PivotPHandle;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureMoveAction;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class FeatureMoveListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    public static final String SELECTION_CHANGED_NOTIFICATION = "SELECTION_CHANGED_NOTIFICATION_FEATUREMOVE"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    protected Point2D pressPoint;
    protected Point2D dragPoint;
    protected PDimension dragDim;
    protected PFeature feature;
    protected MappingComponent mc;
    protected Vector features = new Vector();

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private boolean ctrlPressed = false;
    private boolean drag = false;
    private final PLayer handleLayer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of FeatureMoveListener.
     *
     * @param  mc  DOCUMENT ME!
     */
    public FeatureMoveListener(final MappingComponent mc) {
        super();
        this.mc = mc;
        this.handleLayer = mc.getHandleLayer();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mousePressed(final PInputEvent e) {
        super.mousePressed(e);
        if (!ctrlPressed(e)) {
            unmarkFeatures();
        }
        if (e.isLeftMouseButton()) {
            // Initialize the locations.
            pressPoint = e.getPosition();
            dragDim = e.getCanvasDelta();
            dragPoint = pressPoint;
            final Object o = PFeatureTools.getFirstValidObjectUnderPointer(e, new Class[] { PFeature.class });

            if ((o instanceof PFeature) && ((PFeature)o).getFeature().isEditable()
                        && ((PFeature)o).getFeature().canBeSelected()) {
                feature = (PFeature)(o);
                feature.setStrokePaint(Color.red);
                if (features.contains(feature)) {
//                    features.remove(feature);
//                    mc.reconsiderFeature(feature.getFeature());
                } else {
                    features.add(feature);
                    feature.moveToFront();
                }
                if (!feature.isSelected() || (mc.getFeatureCollection().getSelectedFeatures().size() != 1)) {
                    mc.getFeatureCollection().unselectAll();
                    mc.getFeatureCollection().select(feature.getFeature());
                    postSelectionChanged();
                }
            } else {
                feature = null;
            }
        }
    }

    @Override
    public void mouseDragged(final PInputEvent e) {
        if (handleLayer.getChildrenCount() > 0) {
            drag = true;
            final SimpleMoveListener moveListener = (SimpleMoveListener)mc.getInputListener(MappingComponent.MOTION);
            if (moveListener != null) {
                moveListener.mouseMoved(e);
            } else {
                log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden."); // NOI18N
            }
            super.mouseDragged(e);
            if (feature != null) {
                dragPoint = e.getPosition();
                final Feature feat = feature.getFeature();
                // bestimmt selbst wie es bewegt wird?
                if (feat instanceof SelfManipulatingFeature) {
                    final SelfManipulatingFeature smFeature = (SelfManipulatingFeature)feat;
                    final Coordinate coord = new Coordinate(
                            mc.getWtst().getSourceX(dragPoint.getX()),
                            mc.getWtst().getSourceY(dragPoint.getY()));
                    smFeature.moveTo(coord);
                } else {
                    // PDimension delta = e.getDeltaRelativeTo(pressPoint);
                    final PDimension delta = e.getCanvasDelta();
                    dragDim.setSize((dragDim.getWidth() - e.getCanvasDelta().getWidth()),
                        (dragDim.getHeight() - e.getCanvasDelta().getHeight()));
                    final Iterator it = features.iterator();
                    while (it.hasNext()) {
                        final Object o = it.next();
                        if (o instanceof PFeature) {
                            final PFeature f = (PFeature)o;
                            f.moveFeature(delta);
                        }
                    }
                    final double scale = mc.getCamera().getViewScale();
                    for (int i = 0; i < handleLayer.getChildrenCount(); i++) {
                        final PNode child = handleLayer.getChild(i);
                        if (child instanceof PivotPHandle) {
                            final PivotPHandle pivotHandle = (PivotPHandle)child;
                            final PLocator pLocator = pivotHandle.getLocator();
                            final Point2D newMid = new Point2D.Double(pLocator.locateX() + (delta.getWidth() / scale),
                                    pLocator.locateY()
                                            + (delta.getHeight() / scale));
                            pivotHandle.getMid().setLocation(newMid);
                        }
                        if (child instanceof PHandle) {
                            final PHandle pHandle = (PHandle)child;
                            pHandle.relocateHandle();
                        }
                    }
                }
                mc.syncSelectedObjectPresenter(0);
            }
        }
    }

    @Override
    public void mouseReleased(final PInputEvent e) {
        super.mouseReleased(e);
        // endDrag
        if (drag) {
            drag = false;

            final Feature feat = feature.getFeature();
            if (feat instanceof SelfManipulatingFeature) {
                ((SelfManipulatingFeature)feat).moveFinished();
            }

            mc.getMemUndo().addAction(new FeatureMoveAction(mc, features, dragDim, true));
            mc.getMemRedo().clear();
            final Iterator it = features.iterator();
            while (it.hasNext()) {
                final Object o = it.next();
                if (o instanceof PFeature) {
                    final PFeature f = (PFeature)o;
                    if (mc.getFeatureCollection() instanceof DefaultFeatureCollection) {
                        final Vector v = new Vector();
                        v.add(f.getFeature());
                        ((DefaultFeatureCollection)mc.getFeatureCollection()).fireFeaturesChanged(v);
                        // DANGER
                        // viewer.getFeatureCollection().reconsiderFeature(getFeature());
                    } else {
                        mc.getFeatureCollection().reconsiderFeature(f.getFeature());
                    }
                }
            }
            mc.showHandles(false);
        }
        if (!ctrlPressed(e)) {
            unmarkFeatures();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void postSelectionChanged() {
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(SELECTION_CHANGED_NOTIFICATION, this);
    }

    @Override
    public void mouseMoved(final PInputEvent event) {
        super.mouseMoved(event);
        if (!ctrlPressed(event)) {
            unmarkFeatures();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   event  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean ctrlPressed(final PInputEvent event) {
        return (event.getModifiers() & InputEvent.CTRL_MASK) != 0;
    }

    /**
     * DOCUMENT ME!
     */
    private void unmarkFeatures() {
        final Iterator it = features.iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if (o instanceof PFeature) {
                final PFeature f = (PFeature)o;
                if (f.getFeature() instanceof AbstractNewFeature) {
                    f.setStrokePaint(Color.black);
                }
//                else {
//                    mc.reconsiderFeature(f.getFeature());
//                }
            }
        }
        features = new Vector();
    }
}
