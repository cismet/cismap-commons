/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * CustomAction.java
 *
 * Created on 6. Dezember 2007, 13:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener.actions;

import de.cismet.cismap.commons.features.Feature;

/**
 * Definiert Methoden, die Veraenderungs-Aktionen von Features implementieren muessen. Diese Methoden sind wichtig fuer
 * die korrekte Arbeit der Undo/Redo- Funktion in Cismap.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public interface CustomAction {

    //~ Methods ----------------------------------------------------------------

    /**
     * Fuehrt die Aktion des Action-Objekts aus.
     */
    void doAction();

    /**
     * Liefert einen lesbaren String mit Informationen der Aktion. Auch fuer eine Undo/Redo-Combobox denkbar.
     *
     * @return  DOCUMENT ME!
     */
    String info();

    /**
     * Liefert das Gegenteil der eigenen Aktion zurueck. Wird fuer Redo-Funktion benoetigt.
     *
     * @return  invertiertes Action-Objekt
     */
    CustomAction getInverse();
    
    /**
     * Checks, if the given feature is concerned from this action
     * 
     * @param feature the feature that should be checked
     * @return true, iff the given feature is concerned from this action
     */
    boolean featureConcerned(Feature feature);
}
