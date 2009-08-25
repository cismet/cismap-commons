/*
 * HandleAddAction.java
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
 * verwendet, um ein gel\u00F6schtes Handle eines Features wiederherzustellen.
 * @author nh
 */
public class HandleAddAction implements CustomAction {
    private MappingComponent mc;
    private Feature f;
    private int posInArray;
    private float x,  y;
    private Coordinate c;

    /**
     * Erzeugt eine HandleAddAction-Instanz.
     * @param pf PFeature dem das Handle zugeordnet ist
     * @param h das Handle selbst
     * @param pos Position der HandleKoordinaten im Koordinatenarray des PFeatures
     * @param c Coordinate-Instanz der Handle-Koordinaten
     */
    public HandleAddAction(MappingComponent mc, Feature f, int pos, Coordinate c, float x, float y) {
        this.mc = mc;
        this.f = f;
        this.posInArray = pos;
        this.c = c;
        this.x = x;
        this.y = y;
    }

    /**
     * Legt das gespeicherte PHandle neu an.
     */
    public void doAction() {
        PFeature pf = (PFeature) mc.getPFeatureHM().get(f);
        pf.setXp(pf.insertCoordinate(posInArray, pf.getXp(), x));
        pf.setYp(pf.insertCoordinate(posInArray, pf.getYp(), y));
        pf.setCoordArr(pf.insertCoordinate(posInArray, pf.getCoordArr(), c));
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
        return "Erstelle Handle an " + posInArray + ". Koord.: (" + x + ", " + y + ")";
    }

    /**
     * Liefert als Gegenteil die L\u00F6schaktion des Handles.
     * @return HandleDeleteAction
     */
    public CustomAction getInverse() {
        return new HandleDeleteAction(mc, f, posInArray, c, x, y);
    }
}
