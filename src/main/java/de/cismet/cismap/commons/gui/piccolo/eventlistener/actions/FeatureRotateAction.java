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

import java.awt.geom.Point2D;

import java.util.Collection;
import java.util.Vector;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;

/**
 * Implementiert das CustomAction-Interface und wird von der Memento-Klasse verwendet, um das Drehen von Features wieder
 * r\u00FCckg\u00E4ngig zu machen, in dem in die entgegengesetzte Richtung gedreht wird.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class FeatureRotateAction implements CustomAction {

    //~ Instance fields --------------------------------------------------------

    private MappingComponent mc;
    private double rot;
    private Point2D pivot;
    private Collection<Feature> arr;

    //~ Constructors -----------------------------------------------------------

    /**
     * Erzeugt eine neue FeatureRotateAction.
     *
     * @param  mc     die Mappingcomponent
     * @param  arr    pfArr ArrayList mit zu drehenden PFeatures
     * @param  pivot  Kopie des Angelpunkts der Drehung
     * @param  rot    Drehwinkel im Bogenma\u00DF
     */
    public FeatureRotateAction(final MappingComponent mc,
            final Collection<Feature> arr,
            final Point2D pivot,
            final double rot) {
        this.mc = mc;
        this.arr = arr;
        this.pivot = pivot;
        this.rot = rot;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Dreht die in der ArrayList enthaltenen PFeatures.
     */
    @Override
    public void doAction() {
        final Vector v = new Vector();
        for (final Feature o : arr) {
            final PFeature pf = ((PFeature)mc.getPFeatureHM().get(o));
            v.add(pf.getFeature());
            pf.rotateAllPoints(rot, pivot);
            pf.syncGeometry();
        }
        ((DefaultFeatureCollection)mc.getFeatureCollection()).fireFeaturesChanged(v);
        mc.showHandles(false);
    }

    /**
     * Liefert eine Beschreibung der Aktion als String.
     *
     * @return  Beschreibungsstring
     */
    @Override
    public String info() {
        final StringBuffer sb = new StringBuffer();
        for (final Object o : arr) {
            sb.append(o.hashCode() + ", ");                           // NOI18N
        }
        sb.delete(sb.length() - 2, sb.length());
        return org.openide.util.NbBundle.getMessage(
                FeatureRotateAction.class,
                "FeatureRotateAction.info().return",
                new Object[] { sb.toString(), Math.toDegrees(rot) }); // NOI18N
    }

    /**
     * Liefert als Gegenteil die FeatureRotateAktion in die umgekehrte Richtung.
     *
     * @return  FeatureRotateAction
     */
    @Override
    public FeatureRotateAction getInverse() {
        return new FeatureRotateAction(mc, arr, pivot, rot * (-1));
    }

    @Override
    public boolean featureConcerned(final Feature feature) {
        for (final Feature o : arr) {
            if (o.equals(feature)) {
                return true;
            }
        }
        return false;
    }
}
