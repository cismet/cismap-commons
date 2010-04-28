/*
 * FeatureMoveListener.java
 *
 * Created on 18. April 2005, 14:57
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureMoveAction;
import de.cismet.cismap.commons.tools.PFeatureTools;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author hell
 */
public class FeatureMoveListener extends PBasicInputEventHandler {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public static final String SELECTION_CHANGED_NOTIFICATION = "SELECTION_CHANGED_NOTIFICATION_FEATUREMOVE";//NOI18N
    protected Point2D pressPoint;
    protected Point2D dragPoint;
    protected PDimension dragDim;
    protected PFeature feature;
    protected MappingComponent mc;
    protected Vector features = new Vector();
    private boolean ctrlPressed = false;
    private boolean drag = false;
    private final PLayer handleLayer;

    /** Creates a new instance of FeatureMoveListener */
    public FeatureMoveListener(MappingComponent mc) {
        super();
        this.mc = mc;
        this.handleLayer = mc.getHandleLayer();
    }

    @Override
    public void mousePressed(PInputEvent e) {
        super.mousePressed(e);
        if (!ctrlPressed(e)) {
            unmarkFeatures();
        }
        if (e.getButton() == 1) { //Linke Maustaste: TODO: konnte die piccolo Konstanten nicht inden
            // Initialize the locations.
            pressPoint = e.getPosition();
            dragDim = e.getCanvasDelta();
            dragPoint = pressPoint;
            handleLayer.removeAllChildren();
            Object o = PFeatureTools.getFirstValidObjectUnderPointer(e, new Class[]{PFeature.class});

            if (o instanceof PFeature && ((PFeature) o).getFeature().isEditable() && ((PFeature) o).getFeature().canBeSelected()) {
                feature = (PFeature) (o);
                feature.setStrokePaint(Color.red);
                if (features.contains(feature)) {
//                    features.remove(feature);
//                    mc.reconsiderFeature(feature.getFeature());
                } else {
                    features.add(feature);
                    feature.moveToFront();
                }
                postSelectionChanged();
            } else {
                feature = null;
            }
        }
    }

    @Override
    public void mouseDragged(PInputEvent e) {
        drag = true;
        SimpleMoveListener moveListener = (SimpleMoveListener) mc.getInputListener(MappingComponent.MOTION);
        if (moveListener != null) {
            moveListener.mouseMoved(e);
        } else {
            log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");//NOI18N
        }
        super.mouseDragged(e);
        if (feature != null) {
            dragPoint = e.getPosition();
            //PDimension delta=e.getDeltaRelativeTo(pressPoint);
            PDimension delta = e.getCanvasDelta();
            dragDim.setSize((dragDim.getWidth() - e.getCanvasDelta().getWidth()), (dragDim.getHeight() - e.getCanvasDelta().getHeight()));
            Iterator it = features.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof PFeature) {
                    PFeature f = (PFeature) o;
                    f.moveFeature(delta);
                }
            }
            if (handleLayer.getChildrenCount() > 0) {
                //to avoid problem if featur is dragged, released and dragged again very fast.
                handleLayer.removeAllChildren();
            }
            mc.syncSelectedObjectPresenter(0);
        }
    }

    @Override
    public void mouseReleased(PInputEvent e) {
        super.mouseReleased(e);
        //endDrag
        if (drag) {
            drag = false;
            mc.getMemUndo().addAction(new FeatureMoveAction(mc, features, dragDim, true));
            mc.getMemRedo().clear();
            Iterator it = features.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof PFeature) {
                    PFeature f = (PFeature) o;
                    if (mc.getFeatureCollection() instanceof DefaultFeatureCollection) {
                        Vector v = new Vector();
                        v.add(f.getFeature());
                        ((DefaultFeatureCollection) mc.getFeatureCollection()).fireFeaturesChanged(v);
                        //DANGER
                        //viewer.getFeatureCollection().reconsiderFeature(getFeature());
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

    private void postSelectionChanged() {
        PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(SELECTION_CHANGED_NOTIFICATION, this);
    }

    @Override
    public void mouseMoved(PInputEvent event) {
        super.mouseMoved(event);
        if (!ctrlPressed(event)) {
            unmarkFeatures();
        }
    }

    private boolean ctrlPressed(PInputEvent event) {
        return (event.getModifiers() & InputEvent.CTRL_MASK) != 0;
    }

    private void unmarkFeatures() {
        Iterator it = features.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof PFeature) {
                PFeature f = (PFeature) o;
                if (f.getFeature() instanceof PureNewFeature) {
                    f.setStrokePaint(Color.black);
                } else {
                    mc.reconsiderFeature(f.getFeature());
                }
            }
        }
        features = new Vector();
    }
}
