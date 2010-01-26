/*
 * FeatureMoveAction.java
 *
 * Created on 6. Dezember 2007, 12:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener.actions;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import edu.umd.cs.piccolo.util.PDimension;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Implementiert das CustomAction-Interface und wird von der Memento-Klasse
 * verwendet, um ein (oder mehrere) vom Benutzer verschobenes Feature an den
 * Ursprungsort zu verschieben.
 * @author nh
 */
public class FeatureMoveAction implements CustomAction {
    private static final ResourceBundle I18N = ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle");
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private MappingComponent mc;
    private Vector features;
    private PDimension dim;

    /**
     * Erzeugt eine FeatureMoveAction-Instanz.
     * @param mc MappingComponent auf dem die PFeatures bewegt werden
     * @param features Vektor mit allen bewegten Features
     * @param dim PDimension-Objekt das die Bewegung darstellt
     */
    public FeatureMoveAction(MappingComponent mc, Vector features, PDimension dim, boolean dimensionInPixel) {
        this.mc = mc;
        this.features = features;
        if (dimensionInPixel) {
            this.dim = createDimension(dim, false);
        } else {
            this.dim = dim;
        }
    }

    /**
     * Bewegt das Feature.
     */
    public void doAction() {
        log.debug("X=" + dim.getWidth());
        log.debug("Y=" + dim.getHeight());
        Iterator it = features.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof PFeature && ((PFeature) o).getFeature().isEditable() && ((PFeature) o).getFeature().canBeSelected()) {
                PFeature f = (PFeature) o;
                f.moveFeature(createDimension(dim, true));
                if (mc.getFeatureCollection() instanceof DefaultFeatureCollection) {
                    Vector v = new Vector();
                    v.add(f.getFeature());
                    ((DefaultFeatureCollection) mc.getFeatureCollection()).fireFeaturesChanged(v);
                }
            }
        }
    }

    /**
     * Liefert eine Beschreibung der Aktion als String.
     * @return  Beschreibungsstring
     */
    public String info() {
        return I18N.getString("de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.FeatureMoveAction.info().return")
            + " " + "(" + dim.getWidth() + ", " + dim.getHeight() + ")";
    }

    /**
     * Liefert die gegenteilige Bewegungsaktion.
     * @return gegenteilige Bewegungsaktion
     */
    public CustomAction getInverse() {
        PDimension inverseDim = new PDimension(dim.getWidth() * (-1), dim.getHeight() * (-1));
        return new FeatureMoveAction(mc, features, inverseDim, false);
    }

    /**
     * Berechnet Pixelwerte zu PCanvas-Koordinaten und umgekehrt.
     * @param dim umzurechnendes PDimension-Objekt
     * @param toPixel boolean-Variable zur Angabe der Berechnungsrichtung
     * @return PDimension-Objekt mit umgerechneten Werten
     */
    private PDimension createDimension(PDimension dim, boolean toPixel) {
        double scale = mc.getCamera().getViewScale();
        PDimension newDim;
        if (toPixel) {
            newDim = new PDimension(dim.getWidth() * scale, dim.getHeight() * scale);
        } else {
            newDim = new PDimension(dim.getWidth() / scale, dim.getHeight() / scale);
        }
        return newDim;
    }
}
