/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo;

import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.CustomAction;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureRotateAction;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.util.PLocator;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Vector;
import javax.swing.JOptionPane;

/**
 *
 * @author jruiz
 */
public class RotationPHandle extends PHandle {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private PFeature pfeature;
    private static final String DIALOG_TEXT = "Aktion r\u00FCckg\u00E4ngig machen?";
    private static final String DIALOG_TITLE = "\u00DCberschneidung entdeckt";
    private double rotation = 0.0d;
    private PHandle pivotHandle;
    private Point2D mid;
    private int position;

    public RotationPHandle(final PFeature pfeature, Point2D mid, PHandle pivotHandle, final int position) {
        super(new PLocator() {

            @Override
            public double locateX() {
                return pfeature.getXp()[position];
            }

            @Override
            public double locateY() {
                return pfeature.getYp()[position];
            }
        }, pfeature.getViewer());
        
        this.mid = mid;
        this.pfeature = pfeature;
        this.position = position;
        this.pivotHandle = pivotHandle;
    }

    @Override
    public void dragHandle(PDimension aLocalDimension, PInputEvent aEvent) {
        SimpleMoveListener moveListener = (SimpleMoveListener) pfeature.getViewer().getInputListener(MappingComponent.MOTION);
        if (moveListener != null) {
            moveListener.mouseMoved(aEvent);
        } else {
            log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");
        }
        pfeature.getViewer().getCamera().localToView(aLocalDimension);
        double dragRot = pfeature.calculateDrag(aEvent, pfeature.getXp()[position], pfeature.getYp()[position]);
        if (pfeature.getViewer().getFeatureCollection() instanceof DefaultFeatureCollection &&
                ((DefaultFeatureCollection) pfeature.getViewer().getFeatureCollection()).getSelectedFeatures().size() > 1) {
            for (Object o : pfeature.getViewer().getFeatureCollection().getSelectedFeatures()) {
                PFeature pf = (PFeature) pfeature.getViewer().getPFeatureHM().get(o);
                if (pf.getFeature().isEditable()) {
                    pf.rotateAllPoints(dragRot, null);
                    relocateHandle();
                }
            }
        } else {
            pfeature.rotateAllPoints(dragRot, null);
            relocateHandle();
        }
        rotation -= dragRot;
    }

    /**
     * Override this method to get notified when the handle starts to get dragged.
     */
    @Override
    public void startHandleDrag(Point2D aLocalPoint, PInputEvent aEvent) {
        if (pfeature.getViewer().isFeatureDebugging()) {
            log.debug("startHandleDrag");
        }
        rotation = 0.0d;

        // InfoNode entfernen, da sie sonst mitdreht
        Collection selArr = pfeature.getViewer().getFeatureCollection().getSelectedFeatures();
        for (Object o : selArr) {
            PFeature pf = (PFeature) (pfeature.getViewer().getPFeatureHM().get(o));
            if (pf != null && pf.getInfoNode() != null) {
                pf.getInfoNode().setVisible(false);
            }
        }

        pfeature.getViewer().getHandleLayer().removeAllChildren();
        pfeature.getViewer().getHandleLayer().addChild(this);
        if (pivotHandle != null) {
            pfeature.getViewer().getHandleLayer().addChild(pivotHandle);
        }
        super.startHandleDrag(aLocalPoint, aEvent);
    }

    @Override
    public void endHandleDrag(java.awt.geom.Point2D aLocalPoint, PInputEvent aEvent) {
        try {
            if (pfeature.getViewer().isFeatureDebugging()) {
                log.debug("endHandleDrag");
            }

            LinkedHashSet<Feature> temp = (LinkedHashSet<Feature>) pfeature.getViewer().getFeatureCollection().getSelectedFeatures();
            LinkedHashSet<Feature> selArr = (LinkedHashSet<Feature>) temp.clone();
            Vector<Feature> all = pfeature.getViewer().getFeatureCollection().getAllFeatures();
            temp = null;

            // FeatureChangedEvents werfen und aktuell halten
            if (pfeature.getViewer().getFeatureCollection() instanceof DefaultFeatureCollection) {
                Vector v = new Vector();
                for (Object f : selArr) {
                    ((PFeature) pfeature.getViewer().getPFeatureHM().get(f)).setPivotPoint(mid);
                    v.add(((PFeature) pfeature.getViewer().getPFeatureHM().get(f)).getFeature());
                }
                ((DefaultFeatureCollection) pfeature.getViewer().getFeatureCollection()).fireFeaturesChanged(v);
            } else {
                pfeature.getViewer().getFeatureCollection().reconsiderFeature(pfeature.getFeature());
            }
            log.debug("Nach fireFeaturesChanged");

            boolean overlap = false;

            if (! (pfeature.getFeature() instanceof SearchFeature)) {
                // Ewig aufwändiger Check nach Überschneidungen
                for (Object o : selArr) {
                    Geometry g = ((PFeature) pfeature.getViewer().getPFeatureHM().get(o)).getFeature().getGeometry();
                    if (!overlap) {
                        for (Feature f : all) {
                            if (!(g.equals(f.getGeometry())) && g.overlaps(f.getGeometry())) {
                                overlap = true;
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
            }

            // Falls ja, dann Abfrage "Sind Sie sicher?"
            log.debug("Nach Overlap-Check");
            if (overlap) {
                log.debug("Überlappt");
                if (pfeature.getViewer().isFeatureDebugging()) {
                    log.debug("\u00DCberschneidungen nach Drehung der PFeatures");
                }
                int answer = JOptionPane.showConfirmDialog(pfeature.getViewer(), DIALOG_TEXT, DIALOG_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (pfeature.getViewer().isFeatureDebugging()) {
                    log.debug("Drehung durchf\u00FChren: " + (answer == JOptionPane.YES_OPTION ? "JA" : "NEIN"));
                }
                if (answer == JOptionPane.YES_OPTION) {
                    CustomAction a = new FeatureRotateAction(pfeature.getViewer(), selArr, (Point2D) mid.clone(), rotation);
                    a.doAction();
                } else {
                    if (rotation != 0.0d) {
                        pfeature.getViewer().getMemUndo().addAction(new FeatureRotateAction(pfeature.getViewer(), selArr, (Point2D) mid.clone(), rotation));
                        pfeature.getViewer().getMemRedo().clear();
                    }
                }
            } else {
                log.debug("Überlappt nicht");
                if (rotation != 0.0d) {
                    log.debug("arr=" + selArr);
                    log.debug("mid=" + mid);
                    Point2D actionMid = new Point2D.Double(mid.getX(), mid.getY());
                    pfeature.getViewer().getMemUndo().addAction(new FeatureRotateAction(pfeature.getViewer(), selArr, actionMid, rotation));
                    pfeature.getViewer().getMemRedo().clear();
                }
            }
            super.endHandleDrag(aLocalPoint, aEvent);

        } catch (Throwable ex) {
            log.error("Boooooom", ex);
        }
    }

    @Override
    public void mouseMovedNotInDragOperation(PInputEvent pInputEvent) {
        SimpleMoveListener moveListener = (SimpleMoveListener) pfeature.getViewer().getInputListener(MappingComponent.MOTION);
        if (moveListener != null) {
            moveListener.mouseMoved(pInputEvent);
        } else {
            log.warn("Movelistener zur Abstimmung der Mauszeiger nicht gefunden.");
        }
    }

}
