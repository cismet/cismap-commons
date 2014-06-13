/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * FeatureMoveAction.java
 *
 * Created on 6. Dezember 2007, 12:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener.actions;

import edu.umd.cs.piccolo.util.PDimension;

import java.util.Iterator;
import java.util.Vector;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import java.util.List;

/**
 * Implementiert das CustomAction-Interface und wird von der Memento-Klasse verwendet, um ein (oder mehrere) vom
 * Benutzer verschobenes Feature an den Ursprungsort zu verschieben.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class FeatureMoveAction implements CustomAction {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private MappingComponent mc;
    private List<PFeature> features;
    private PDimension dim;

    //~ Constructors -----------------------------------------------------------

    /**
     * Erzeugt eine FeatureMoveAction-Instanz.
     *
     * @param  mc                MappingComponent auf dem die PFeatures bewegt werden
     * @param  features          Vektor mit allen bewegten Features
     * @param  dim               PDimension-Objekt das die Bewegung darstellt
     * @param  dimensionInPixel  DOCUMENT ME!
     */
    public FeatureMoveAction(final MappingComponent mc,
            final List<PFeature> features,
            final PDimension dim,
            final boolean dimensionInPixel) {
        this.mc = mc;
        this.features = features;
        if (dimensionInPixel) {
            this.dim = createDimension(dim, false);
        } else {
            this.dim = dim;
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Bewegt das Feature.
     */
    @Override
    public void doAction() {
        if (log.isDebugEnabled()) {
            log.debug("X=" + dim.getWidth());  // NOI18N
        }
        if (log.isDebugEnabled()) {
            log.debug("Y=" + dim.getHeight()); // NOI18N
        }
        final Iterator<PFeature> it = features.iterator();
        while (it.hasNext()) {
            final PFeature o = it.next();
            if (((PFeature)o).getFeature().isEditable()
                        && ((PFeature)o).getFeature().canBeSelected()) {
                PFeature f = (PFeature)o;

                // the pfeature from the map should be used. Otherwise, the undo/redo buttons does not
                // work properly (only one polygon of a multi polygon is moved),
                // if the featureMoveAction is used in a sequence with the featureAddEntityAction
                f = mc.getPFeatureHM().get(f.getFeature());

                if (f != null) {
                    f.moveFeature(createDimension(dim, true));
                    if (mc.getFeatureCollection() instanceof DefaultFeatureCollection) {
                        final Vector v = new Vector();
                        v.add(f.getFeature());
                        ((DefaultFeatureCollection)mc.getFeatureCollection()).fireFeaturesChanged(v);
                    }
                }
            }
        }
    }

    /**
     * Liefert eine Beschreibung der Aktion als String.
     *
     * @return  Beschreibungsstring
     */
    @Override
    public String info() {
        return org.openide.util.NbBundle.getMessage(
                FeatureMoveAction.class,
                "FeatureMoveAction.info().return",
                new Object[] { dim.getWidth(), dim.getHeight() }); // NOI18N
    }

    /**
     * Liefert die gegenteilige Bewegungsaktion.
     *
     * @return  gegenteilige Bewegungsaktion
     */
    @Override
    public CustomAction getInverse() {
        final PDimension inverseDim = new PDimension(dim.getWidth() * (-1), dim.getHeight() * (-1));
        return new FeatureMoveAction(mc, features, inverseDim, false);
    }

    /**
     * Berechnet Pixelwerte zu PCanvas-Koordinaten und umgekehrt.
     *
     * @param   dim      umzurechnendes PDimension-Objekt
     * @param   toPixel  boolean-Variable zur Angabe der Berechnungsrichtung
     *
     * @return  PDimension-Objekt mit umgerechneten Werten
     */
    private PDimension createDimension(final PDimension dim, final boolean toPixel) {
        final double scale = mc.getCamera().getViewScale();
        PDimension newDim;
        if (toPixel) {
            newDim = new PDimension(dim.getWidth() * scale, dim.getHeight() * scale);
        } else {
            newDim = new PDimension(dim.getWidth() / scale, dim.getHeight() / scale);
        }
        return newDim;
    }


    @Override
    public boolean featureConcerned(Feature feature) {
        for (PFeature f : features) {
            if (f.getFeature().equals(feature)) {
                return true;
            }
        }
        
        return false;
    }
}
