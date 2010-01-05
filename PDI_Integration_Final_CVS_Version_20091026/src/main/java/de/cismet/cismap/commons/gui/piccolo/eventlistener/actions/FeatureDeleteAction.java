/*
 * FeatureDeleteAction.java
 *
 * Created on 6. Dezember 2007, 11:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.gui.piccolo.eventlistener.actions;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;

/**
 * Implementiert das CustomAction-Interface und wird von der Memento-Klasse
 * verwendet, um das Erstellen von Features wieder r\u00FCckg\u00E4ngig zu machen, in dem
 * diese gel\u00F6scht werden.
 * @author nh
 */
public class FeatureDeleteAction implements CustomAction {
    private Feature f;
    private MappingComponent mc;
    
    /**
     * Erzeugt eine FeatureDeleteAction-Instanz.
     * @param mc MappingComponent, die das Feature beinhaltet
     * @param f das zu l\u00F6schende Feature
     */
    public FeatureDeleteAction(MappingComponent mc, Feature f) {
        this.f = f;
        this.mc = mc;
    }
    
    /**
     * L\u00F6scht das gespeicherte Feature.
     */
    public void doAction() {
        // Feature l\u00F6schen
        mc.getFeatureCollection().removeFeature(f);
    }
    
    /**
     * Liefert eine Beschreibung der Aktion als String.
     * @return Beschreibungsstring
     */
    public String info() {
        return "L\u00F6sche Feature: " + f;
    }
    
    /**
     * Liefert als Gegenteil die Aktion zum Anlegen des Features.
     * @return Erzeuge-Aktion
     */
    public CustomAction getInverse() {
        return new FeatureCreateAction(mc, f);
    }
}
