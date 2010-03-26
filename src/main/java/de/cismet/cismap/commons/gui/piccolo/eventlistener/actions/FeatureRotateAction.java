/*
 * CustomAction.java
 *
 * Created on 6. Dezember 2007, 13:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener.actions;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Vector;

/**
 * Implementiert das CustomAction-Interface und wird von der Memento-Klasse
 * verwendet, um das Drehen von Features wieder r\u00FCckg\u00E4ngig zu machen, in dem
 * in die entgegengesetzte Richtung gedreht wird.
 * @author nh
 */
public class FeatureRotateAction implements CustomAction {
    private MappingComponent mc;
    private double rot;
    private Point2D pivot;
    private Collection arr;

    /**
     * Erzeugt eine neue FeatureRotateAction.
     * @param mc die Mappingcomponent
     * @param pfArr ArrayList mit zu drehenden PFeatures
     * @param pivot Kopie des Angelpunkts der Drehung
     * @param rot Drehwinkel im Bogenma\u00DF
     */
    public FeatureRotateAction(MappingComponent mc, Collection arr, Point2D pivot, double rot) {
        this.mc = mc;
        this.arr = arr;
        this.pivot = pivot;
        this.rot = rot;
    }

    /**
     * Dreht die in der ArrayList enthaltenen PFeatures.
     */
    public void doAction() {
        Vector v = new Vector();
        for (Object o : arr) {
            PFeature pf = ((PFeature) mc.getPFeatureHM().get(o));
            v.add(pf.getFeature());
            pf.rotateAllPoints(rot, pivot);
            pf.syncGeometry();
        }
        ((DefaultFeatureCollection) mc.getFeatureCollection()).fireFeaturesChanged(v);
        mc.showHandles(false);
    }

    /**
     * Liefert eine Beschreibung der Aktion als String.
     * @return Beschreibungsstring
     */
    public String info() {
        StringBuffer sb = new StringBuffer();
        for (Object o : arr) {
            sb.append(o.hashCode() + ", ");//NOI18N
        }
        sb.delete(sb.length() - 2, sb.length());
        return org.openide.util.NbBundle.getMessage(FeatureRotateAction.class, "FeatureRotateAction.info().return", new Object[]{sb.toString(), Math.toDegrees(rot)});
    }

    /**
     * Liefert als Gegenteil die FeatureRotateAktion in die umgekehrte Richtung.
     * @return FeatureRotateAction
     */
    public FeatureRotateAction getInverse() {
        return new FeatureRotateAction(mc, arr, pivot, rot * (-1));
    }
}
