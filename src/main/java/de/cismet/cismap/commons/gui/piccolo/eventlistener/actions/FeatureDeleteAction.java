/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
 * Implementiert das CustomAction-Interface und wird von der Memento-Klasse verwendet, um das Erstellen von Features
 * wieder r\u00FCckg\u00E4ngig zu machen, in dem diese gel\u00F6scht werden.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class FeatureDeleteAction implements CustomAction {

    //~ Instance fields --------------------------------------------------------

    private Feature f;
    private MappingComponent mc;

    //~ Constructors -----------------------------------------------------------

    /**
     * Erzeugt eine FeatureDeleteAction-Instanz.
     *
     * @param  mc  MappingComponent, die das Feature beinhaltet
     * @param  f   das zu l\u00F6schende Feature
     */
    public FeatureDeleteAction(final MappingComponent mc, final Feature f) {
        this.f = f;
        this.mc = mc;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * L\u00F6scht das gespeicherte Feature.
     */
    @Override
    public void doAction() {
        // Feature l\u00F6schen
        mc.getFeatureCollection().removeFeature(f);
    }

    /**
     * Liefert eine Beschreibung der Aktion als String.
     *
     * @return  Beschreibungsstring
     */
    @Override
    public String info() {
        //
        return org.openide.util.NbBundle.getMessage(
                FeatureDeleteAction.class,
                "FeatureDeleteAction.info().return",
                new Object[] { f }); // NOI18N
    }

    /**
     * Liefert als Gegenteil die Aktion zum Anlegen des Features.
     *
     * @return  Erzeuge-Aktion
     */
    @Override
    public CustomAction getInverse() {
        return new FeatureCreateAction(mc, f);
    }
}
