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

import com.vividsolutions.jts.geom.Coordinate;

import java.util.Vector;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
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

    private MappingComponent mc;
    private Feature f;
    private int posInArray;
    private Coordinate c; // wird nur f\u00FCr getInverse() ben\u00F6tigt
    private float x;
    private float y;      // wird nur f\u00FCr getInverse() ben\u00F6tigt

    //~ Constructors -----------------------------------------------------------

    /**
     * Erzeugt eine HandleDeleteAction-Instanz.
     *
     * @param  mc   DOCUMENT ME!
     * @param  f    DOCUMENT ME!
     * @param  pos  DOCUMENT ME!
     * @param  c    DOCUMENT ME!
     * @param  x    DOCUMENT ME!
     * @param  y    DOCUMENT ME!
     */
    public HandleDeleteAction(final MappingComponent mc,
            final Feature f,
            final int pos,
            final Coordinate c,
            final float x,
            final float y) {
        this.mc = mc;
        this.f = f;
        this.posInArray = pos;
        this.c = c;
        this.x = x;
        this.y = y;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * L\u00F6scht das PHandle und dessen Koordinate aus den Arrays.
     */
    @Override
    public void doAction() {
        final PFeature pf = (PFeature)mc.getPFeatureHM().get(f);
        pf.setXp(pf.removeCoordinateFromOutside(posInArray, pf.getXp()));
        pf.setYp(pf.removeCoordinateFromOutside(posInArray, pf.getYp()));
        pf.setCoordArr(pf.removeCoordinateFromOutside(posInArray, pf.getCoordArr()));
        pf.syncGeometry();
        pf.setPathToPolyline(pf.getXp(), pf.getYp());
        final Vector v = new Vector();
        v.add(pf.getFeature());
        ((DefaultFeatureCollection)pf.getViewer().getFeatureCollection()).fireFeaturesChanged(v);
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
                new Object[] { posInArray, x, y }); // NOI18N
    }

    /**
     * Liefert als Gegenteil die Anlegeaktion des Handles.
     *
     * @return  HandleAddAction
     */
    @Override
    public CustomAction getInverse() {
        return new HandleAddAction(mc, f, posInArray, c, x, y);
    }
}
