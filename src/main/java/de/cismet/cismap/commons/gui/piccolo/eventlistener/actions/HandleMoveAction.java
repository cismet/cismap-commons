/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * HandleMoveAction.java
 *
 * Created on 11. Dezember 2007, 09:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener.actions;

import edu.umd.cs.piccolo.PLayer;

import java.util.Collection;
import java.util.Set;
import java.util.Vector;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.gui.piccolo.PFeature;

import de.cismet.tools.collections.MultiMap;

/**
 * Implementiert das CustomAction-Interface und wird von der Memento-Klasse verwendet, um ein Handle, das vom Benutzer
 * bewegt wurde wieder an den Uersprungsort zur\u00FCckverschoben wird.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class HandleMoveAction implements CustomAction {

    //~ Instance fields --------------------------------------------------------

    private MultiMap gluedCoordinates;
    private PFeature pf;
    private final int entityPosition;
    private final int ringPosition;
    private final int coordPosition;
    private float startX;
    private float startY;
    private float endX;
    private float endY;
    private boolean isGluedAction;

    //~ Constructors -----------------------------------------------------------

    /**
     * Erzeugt eine HandleMoveAction-Instanz.
     *
     * @param  entityPosition  DOCUMENT ME!
     * @param  ringPosition    DOCUMENT ME!
     * @param  coordPosition   Position der HandleKoordinaten im Koordinatenarray des PFeatures
     * @param  pf              PFeature dem das Handle zugeordnet ist
     * @param  startX          X-Koordinate des Anfangspunkts
     * @param  startY          Y-Koordinate des Anfangspunkts
     * @param  endX            X-Koordinate des Endpunkts
     * @param  endY            Y-Koordinate des Endpunkts
     * @param  isGlued         Waren beim Verschieben mehrere Handles gekoppelt?
     */
    public HandleMoveAction(final int entityPosition,
            final int ringPosition,
            final int coordPosition,
            final PFeature pf,
            final float startX,
            final float startY,
            final float endX,
            final float endY,
            final boolean isGlued) {
        this.gluedCoordinates = null;
        this.entityPosition = entityPosition;
        this.ringPosition = ringPosition;
        this.coordPosition = coordPosition;
        this.pf = pf;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.isGluedAction = isGlued;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Bewegt das gespeicherte PHandle von der Start- zur Zielkoordinate.
     */
    @Override
    public void doAction() {
        if (isGluedAction) { // werden mehrere Punkte bewegt?
            gluedCoordinates = pf.checkforGlueCoords(entityPosition, ringPosition, coordPosition);
        }
        // Bewege das Handle
        pf.moveCoordinateToNewPiccoloPosition(entityPosition, ringPosition, coordPosition, startX, startY);

        // Falls zusammengeh\u00F6rige Punkte gefunden wurden, bewege diese ebenfalls
        if (gluedCoordinates != null) {
            final Set<PFeature> pFeatureSet = gluedCoordinates.keySet();
            for (final PFeature gluePFeature : pFeatureSet) {
                if (gluePFeature.getFeature().isEditable()) {
                    final Collection coordinates = (Collection)gluedCoordinates.get(gluePFeature);
                    if (coordinates != null) {
                        for (final Object o : coordinates) {
                            final int oIndex = (Integer)o;
                            gluePFeature.moveCoordinateToNewPiccoloPosition(
                                entityPosition,
                                ringPosition,
                                oIndex,
                                startX,
                                startY);
                        }
                    }
                }
            }
        }
        // Aktualisiere Handles durch entfernen und neu erstellen
        // (nur wenn das PFeature auch selektiert ist)
        if (pf.isSelected()) {
            final PLayer handleLayer = pf.getViewer().getHandleLayer();
            handleLayer.removeAllChildren();
            pf.addHandles(handleLayer);
        }
        pf.syncGeometry();
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
                HandleMoveAction.class,
                "HandleMoveAction.info().return",
                new Object[] { new Float(startX).intValue(), new Float(startY).intValue() }); // NOI18N
    }

    /**
     * Liefert als Gegenteil die Bewegung des Handles in die andere Richtung.
     *
     * @return  gegenteilige HandleMoveAction
     */
    @Override
    public CustomAction getInverse() {
        return new HandleMoveAction(
                entityPosition,
                ringPosition,
                coordPosition,
                pf,
                endX,
                endY,
                startX,
                startY,
                isGluedAction);
    }
}
