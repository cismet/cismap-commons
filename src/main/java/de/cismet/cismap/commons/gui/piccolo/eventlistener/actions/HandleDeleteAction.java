/*
 * HandleDeleteAction.java
 *
 * Created on 11. Dezember 2007, 09:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener.actions;

import com.vividsolutions.jts.geom.Coordinate;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import java.util.Vector;

/**
 * Implementiert das CustomAction-Interface und wird von der Memento-Klasse
 * verwendet, um ein dupliziertes Handle wieder zu entfernen.
 * @author nh
 */
public class HandleDeleteAction implements CustomAction {
    private MappingComponent mc;
    private Feature f;
    private int posInArray;
    private Coordinate c; // wird nur f\u00FCr getInverse() ben\u00F6tigt
    private float x,  y;  // wird nur f\u00FCr getInverse() ben\u00F6tigt

    /** 
     * Erzeugt eine HandleDeleteAction-Instanz.
     */
    public HandleDeleteAction(MappingComponent mc, Feature f, int pos, Coordinate c, float x, float y) {
        this.mc = mc;
        this.f = f;
        this.posInArray = pos;
        this.c = c;
        this.x = x;
        this.y = y;
    }

    /**
     * L\u00F6scht das PHandle und dessen Koordinate aus den Arrays.
     */
    public void doAction() {
        PFeature pf = (PFeature) mc.getPFeatureHM().get(f);
        pf.setXp(pf.removeCoordinateFromOutside(posInArray, pf.getXp()));
        pf.setYp(pf.removeCoordinateFromOutside(posInArray, pf.getYp()));
        pf.setCoordArr(pf.removeCoordinateFromOutside(posInArray, pf.getCoordArr()));
        pf.syncGeometry();
        pf.setPathToPolyline(pf.getXp(), pf.getYp());
        Vector v = new Vector();
        v.add(pf.getFeature());
        ((DefaultFeatureCollection) pf.getViewer().getFeatureCollection()).fireFeaturesChanged(v);
    }

    /**
     * Liefert eine Beschreibung der Aktion als String.
     * @return Beschreibungsstring
     */
    public String info() {
        return org.openide.util.NbBundle.getMessage(HandleDeleteAction.class, "HandleDeleteAction.info().return", new Object[]{posInArray, x, y});
    }

    /**
     * Liefert als Gegenteil die Anlegeaktion des Handles.
     * @return HandleAddAction
     */
    public CustomAction getInverse() {
        return new HandleAddAction(mc, f, posInArray, c, x, y);
    }
}
