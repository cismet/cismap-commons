/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * HandleDeleteAction.java
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
 * Implementiert das CustomAction-Interface und wird von der Memento-Klasse verwendet, um ein dupliziertes Handle wieder
 * zu entfernen.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class HandleDeleteAction implements CustomAction {

    //~ Instance fields --------------------------------------------------------

    private final MappingComponent mc;
    private final Feature feature;
    private final int entityPosition;
    private final int ringPosition;
    private final int coordPosition;
    private final float x;
    private final float y; // wird nur f\u00FCr getInverse() ben\u00F6tigt

    //~ Constructors -----------------------------------------------------------

    /**
     * Erzeugt eine HandleDeleteAction-Instanz.
     *
     * @param  mc              DOCUMENT ME!
     * @param  feature         DOCUMENT ME!
     * @param  entityPosition  DOCUMENT ME!
     * @param  ringPosition    DOCUMENT ME!
     * @param  coordPosition   DOCUMENT ME!
     * @param  x               DOCUMENT ME!
     * @param  y               DOCUMENT ME!
     */
    public HandleDeleteAction(final MappingComponent mc,
            final Feature feature,
            final int entityPosition,
            final int ringPosition,
            final int coordPosition,
            final float x,
            final float y) {
        this.mc = mc;
        this.feature = feature;
        this.entityPosition = entityPosition;
        this.ringPosition = ringPosition;
        this.coordPosition = coordPosition;
        this.x = x;
        this.y = y;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * L\u00F6scht das PHandle und dessen Koordinate aus den Arrays.
     */
    @Override
    public void doAction() {
        final PFeature pf = (PFeature)mc.getPFeatureHM().get(feature);
        pf.removeCoordinate(entityPosition, ringPosition, coordPosition, false);
    }

    /**
     * Liefert eine Beschreibung der Aktion als String.
     *
     * @return  Beschreibungsstring
     */
    @Override
    public String info() {
        return org.openide.util.NbBundle.getMessage(
                HandleDeleteAction.class,
                "HandleDeleteAction.info().return",
                new Object[] { coordPosition, x, y }); // NOI18N
    }

    /**
     * Liefert als Gegenteil die Anlegeaktion des Handles.
     *
     * @return  HandleAddAction
     */
    @Override
    public CustomAction getInverse() {
        return new HandleAddAction(mc, feature, entityPosition, ringPosition, coordPosition, x, y);
    }

    @Override
    public boolean featureConcerned(final Feature feature) {
        return (feature != null) && feature.equals(feature);
    }
}
