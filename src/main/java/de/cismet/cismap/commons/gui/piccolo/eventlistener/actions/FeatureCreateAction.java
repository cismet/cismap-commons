/*
 * FeatureCreateAction.java
 *
 * Created on 7. Dezember 2007, 11:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.gui.piccolo.eventlistener.actions;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;

/**
 * Implementiert das CustomAction-Interface und wird von der Memento-Klasse
 * verwendet, um gel\u00F6schte Features wiederherzustellen.
 * @author nh
 */
public class FeatureCreateAction implements CustomAction {
    private Feature f;
    private MappingComponent mc;
    
    /**
     * Erzeugt eine FeatureCreateAction-Instanz.
     * @param mc MappingComponent in dem das Feature angelegt werden soll
     * @param feature das zu erzeugende Feature
     */
    public FeatureCreateAction(MappingComponent mc, Feature f) {
        this.mc = mc;
        this.f = f;
    }

    /**
     * Erzeugt das gespeicherte Feature.
     */
    public void doAction() {
        f.setEditable(true);
        mc.getFeatureCollection().addFeature(f);
        mc.getFeatureCollection().holdFeature(f);
    }

    /**
     * Liefert eine Beschreibung der Aktion als String.
     * @return Beschreibungsstring
     */
    public String info() {
        return org.openide.util.NbBundle.getMessage(FeatureCreateAction.class, "FeatureCreateAction.info().return", new Object[] {f});
    }

    /**
     * Liefert als Gegenteil die Loeschaktion des Features.
     * @return Loeschaktion
     */
    public CustomAction getInverse() {
        return new FeatureDeleteAction(mc, f);
    }
}
