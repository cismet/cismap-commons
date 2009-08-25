/*
 * CustomAction.java
 *
 * Created on 6. Dezember 2007, 13:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.gui.piccolo.eventlistener.actions;

/**
 * Definiert Methoden, die Veraenderungs-Aktionen von Features implementieren
 * muessen. Diese Methoden sind wichtig fuer die korrekte Arbeit der Undo/Redo-
 * Funktion in Cismap.
 * @author nh
 */
public interface CustomAction {
    /**
     * Fuehrt die Aktion des Action-Objekts aus.
     */
    public void doAction();
    
    /**
     * Liefert einen lesbaren String mit Informationen der Aktion.
     * Auch fuer eine Undo/Redo-Combobox denkbar.
     */
    public String info();
    
    /**
     * Liefert das Gegenteil der eigenen Aktion zurueck. Wird fuer Redo-Funktion
     * benoetigt.
     * @return  invertiertes Action-Objekt
     */
    public CustomAction getInverse();
}
