/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
 * Implementiert das CustomAction-Interface und wird von der Memento-Klasse verwendet, um gel\u00F6schte Features
 * wiederherzustellen.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class FeatureCreateAction implements CustomAction {

    //~ Instance fields --------------------------------------------------------

    private Feature f;
    private MappingComponent mc;

    //~ Constructors -----------------------------------------------------------

    /**
     * Erzeugt eine FeatureCreateAction-Instanz.
     *
     * @param  mc  MappingComponent in dem das Feature angelegt werden soll
     * @param  f   feature das zu erzeugende Feature
     */
    public FeatureCreateAction(final MappingComponent mc, final Feature f) {
        this.mc = mc;
        this.f = f;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Erzeugt das gespeicherte Feature.
     */
    @Override
    public void doAction() {
        f.setEditable(true);
        mc.getFeatureCollection().addFeature(f);
        mc.getFeatureCollection().holdFeature(f);
    }

    /**
     * Liefert eine Beschreibung der Aktion als String.
     *
     * @return  Beschreibungsstring
     */
    @Override
    public String info() {
        return org.openide.util.NbBundle.getMessage(
                FeatureCreateAction.class,
                "FeatureCreateAction.info().return",
                new Object[] { f }); // NOI18N
    }

    /**
     * Liefert als Gegenteil die Loeschaktion des Features.
     *
     * @return  Loeschaktion
     */
    @Override
    public CustomAction getInverse() {
        return new FeatureDeleteAction(mc, f);
    }
}
