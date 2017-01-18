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

    private final MappingComponent mc;
    private final Feature feature;
    private final int entityPosition;
    private final int ringPosition;
    private final int coordPosition;
    private final float x;
    private final float y;

    //~ Constructors -----------------------------------------------------------

    /**
     * Erzeugt eine HandleAddAction-Instanz.
     *
     * @param  mc              h das Handle selbst
     * @param  f               PFeature dem das Handle zugeordnet ist
     * @param  entityPosition  DOCUMENT ME!
     * @param  ringPosition    DOCUMENT ME!
     * @param  coordPosition   Position der HandleKoordinaten im Koordinatenarray des PFeatures
     * @param  x               DOCUMENT ME!
     * @param  y               DOCUMENT ME!
     */
    public HandleAddAction(
            final MappingComponent mc,
            final Feature f,
            final int entityPosition,
            final int ringPosition,
            final int coordPosition,
            final float x,
            final float y) {
        this.mc = mc;
        this.feature = f;
        this.entityPosition = entityPosition;
        this.ringPosition = ringPosition;
        this.coordPosition = coordPosition;
        this.x = x;
        this.y = y;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Legt das gespeicherte PHandle neu an.
     */
    @Override
    public void doAction() {
        final PFeature pf = (PFeature)mc.getPFeatureHM().get(feature);
        pf.insertCoordinate(entityPosition, ringPosition, coordPosition, x, y, false);
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
                new Object[] { coordPosition, x, y }); // NOI18N
    }

    /**
     * Liefert als Gegenteil die L\u00F6schaktion des Handles.
     *
     * @return  HandleDeleteAction
     */
    @Override
    public CustomAction getInverse() {
        return new HandleDeleteAction(mc, feature, entityPosition, ringPosition, coordPosition, x, y);
    }

    @Override
    public boolean featureConcerned(final Feature feature) {
        return (feature != null) && feature.equals(feature);
    }
}
