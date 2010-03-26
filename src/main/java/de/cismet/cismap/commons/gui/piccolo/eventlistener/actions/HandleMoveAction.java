/*
 * HandleMoveAction.java
 *
 * Created on 11. Dezember 2007, 09:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener.actions;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.tools.collections.MultiMap;
import edu.umd.cs.piccolo.PLayer;
import java.util.Collection;
import java.util.Set;
import java.util.Vector;

/**
 * Implementiert das CustomAction-Interface und wird von der Memento-Klasse
 * verwendet, um ein Handle, das vom Benutzer bewegt wurde wieder an den
 * Uersprungsort zur\u00FCckverschoben wird.
 * @author nh
 */
public class HandleMoveAction implements CustomAction {
    private MultiMap gluedCoordinates;
    private PFeature pf;
    private int posInArray;
    private float startX,  startY,  endX,  endY;
    private boolean isGluedAction;

    /**
     * Erzeugt eine HandleMoveAction-Instanz.
     * @param position Position der HandleKoordinaten im Koordinatenarray des PFeatures
     * @param pf PFeature dem das Handle zugeordnet ist
     * @param handle das Handle selbst
     * @param startX X-Koordinate des Anfangspunkts
     * @param startY Y-Koordinate des Anfangspunkts
     * @param endX X-Koordinate des Endpunkts
     * @param endY Y-Koordinate des Endpunkts
     * @param isGlued Waren beim Verschieben mehrere Handles gekoppelt?
     */
    public HandleMoveAction(int position, PFeature pf, float startX, float startY,
            float endX, float endY, boolean isGlued) {
        this.gluedCoordinates = null;
        this.posInArray = position;
        this.pf = pf;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.isGluedAction = isGlued;
    }

    /**
     * Bewegt das gespeicherte PHandle von der Start- zur Zielkoordinate.
     */
    public void doAction() {
        if (isGluedAction) { //werden mehrere Punkte bewegt?
            gluedCoordinates = pf.checkforGlueCoords(posInArray);
        }
        // Bewege das Handle
        pf.moveCoordinateToNewPiccoloPosition(posInArray, startX, startY);

        // Falls zusammengeh\u00F6rige Punkte gefunden wurden, bewege diese ebenfalls
        if (gluedCoordinates != null) {
            Set<PFeature> pFeatureSet = gluedCoordinates.keySet();
            for (PFeature gluePFeature : pFeatureSet) {
                if (gluePFeature.getFeature().isEditable()) {
                    Collection coordinates = (Collection) gluedCoordinates.get(gluePFeature);
                    if (coordinates != null) {
                        for (Object o : coordinates) {
                            int oIndex = (Integer) o;
                            gluePFeature.moveCoordinateToNewPiccoloPosition(oIndex, startX, startY);
                        }
                    }
                }
            }
        }
        // Aktualisiere Handles durch entfernen und neu erstellen
        // (nur wenn das PFeature auch selektiert ist)
        if (pf.isSelected()) {
            PLayer handleLayer = pf.getViewer().getHandleLayer();
            handleLayer.removeAllChildren();
            pf.addHandles(handleLayer);
        }
        pf.syncGeometry();
        Vector v = new Vector();
        v.add(pf.getFeature());
        ((DefaultFeatureCollection) pf.getViewer().getFeatureCollection()).fireFeaturesChanged(v);
    }

    /**
     * Liefert eine Beschreibung der Aktion als String.
     * @return Beschreibungsstring
     */
    public String info() {
        return org.openide.util.NbBundle.getMessage(HandleMoveAction.class, "HandleMoveAction.info().return", new Object[]{new Float(startX).intValue(), new Float(startY).intValue()});
    }

    /**
     * Liefert als Gegenteil die Bewegung des Handles in die andere Richtung.
     * @return gegenteilige HandleMoveAction
     */
    public CustomAction getInverse() {
        return new HandleMoveAction(posInArray, pf, endX, endY, startX, startY, isGluedAction);
    }
}
