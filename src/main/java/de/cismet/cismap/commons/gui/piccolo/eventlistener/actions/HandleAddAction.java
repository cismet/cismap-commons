/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * HandleAddAction.java
 *
 * Created on 11. Dezember 2007, 09:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener.actions;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;

/**
 * Implementiert das CustomAction-Interface und wird von der Memento-Klasse verwendet, um ein gel\u00F6schtes Handle
 * eines Features wiederherzustellen.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class HandleAddAction implements CustomAction {

    //~ Instance fields --------------------------------------------------------

    private MappingComponent mc;
    private Feature f;
    private int coordEntityIndex;
    private int posInArray;
    private float x;
    private float y;

    //~ Constructors -----------------------------------------------------------

    /**
     * Erzeugt eine HandleAddAction-Instanz.
     *
     * @param  mc                h das Handle selbst
     * @param  f                 PFeature dem das Handle zugeordnet ist
     * @param  coordEntityIndex  DOCUMENT ME!
     * @param  pos               Position der HandleKoordinaten im Koordinatenarray des PFeatures
     * @param  x                 DOCUMENT ME!
     * @param  y                 DOCUMENT ME!
     */
    public HandleAddAction(final MappingComponent mc,
            final Feature f,
            final int coordEntityIndex,
            final int pos,
            final float x,
            final float y) {
        this.mc = mc;
        this.f = f;
        this.coordEntityIndex = coordEntityIndex;
        this.posInArray = pos;
        this.x = x;
        this.y = y;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Legt das gespeicherte PHandle neu an.
     */
    @Override
    public void doAction() {
        final PFeature pf = (PFeature)mc.getPFeatureHM().get(f);
        pf.insertCoordinate(coordEntityIndex, posInArray, x, y);
    }

    /**
     * Liefert eine Beschreibung der Aktion als String.
     *
     * @return  Beschreibungsstring
     */
    @Override
    public String info() {
        return org.openide.util.NbBundle.getMessage(
                HandleAddAction.class,
                "HandleAddAction.info().return",
                new Object[] { posInArray, x, y }); // NOI18N
    }

    /**
     * Liefert als Gegenteil die L\u00F6schaktion des Handles.
     *
     * @return  HandleDeleteAction
     */
    @Override
    public CustomAction getInverse() {
        return new HandleDeleteAction(mc, f, coordEntityIndex, posInArray, x, y);
    }
}
