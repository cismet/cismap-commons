/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * DragFeatureListener.java
 *
 * Created on 20. April 2005, 14:43
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.event.PNotificationCenter;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.features.Attachable;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.PFeatureTools;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class AttachFeatureListener extends RectangleRubberBandListener {

    //~ Static fields/initializers ---------------------------------------------

    public static final String ATTACH_FEATURE_NOTIFICATION = "ATTACH_FEATURE_NOTIFICATION"; // NOI18N

    private static final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            AttachFeatureListener.class);

    //~ Instance fields --------------------------------------------------------

    private PFeature featureToAttach = null;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  pInputEvent  DOCUMENT ME!
     */
    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(
                pInputEvent,
                new Class[] { PFeature.class },
                30.5d,
                true);
        if (o instanceof PFeature) {
            super.mouseClicked(pInputEvent);
            featureToAttach = (PFeature)(o);
            postFeatureAttachRequest();
        } else {
            featureToAttach = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Geometry createGeometryFromRectangle() {
        final WorldToScreenTransform wtst = CismapBroker.getInstance().getMappingComponent().getWtst();

        final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode()));

        // ab einer bestimmten Zoomstufe ist aus ungeklärten Gründen getBounds = EMPTY
        // damit intersects funktioniert, wird hier im Falle von isEmpty ein Punkt statt
        // eines Polygons erzeugt. Dann klappt's auch mit dem Nachbarn...
        if (rectangle.getBounds().isEmpty()) {
            final double x = wtst.getWorldX(rectangle.getBounds().getX());
            final double y = wtst.getWorldY(rectangle.getBounds().getY());

            final Coordinate coord = new Coordinate(x, y);

            return geomFactory.createPoint(coord);
        } else {
            final double x1 = wtst.getWorldX(rectangle.getBounds().getMinX());
            final double x2 = wtst.getWorldX(rectangle.getBounds().getMaxX());
            final double y1 = wtst.getWorldY(rectangle.getBounds().getMinY());
            final double y2 = wtst.getWorldY(rectangle.getBounds().getMaxY());

            final Coordinate[] polyCords = new Coordinate[] {
                    new Coordinate(x1, y1),
                    new Coordinate(x2, y1),
                    new Coordinate(x2, y2),
                    new Coordinate(x1, y2),
                    new Coordinate(x1, y1)
                };

            return geomFactory.createPolygon(geomFactory.createLinearRing(polyCords), null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    @Override
    public void mouseReleased(final PInputEvent event) {
        super.mouseReleased(event);
        if (event.getButton() == 1) { // linke Maustaste
            // Mouseevent muss von einer MappingComponent gefeuert werden
            if (event.getComponent() instanceof MappingComponent) {
                final MappingComponent mc = (MappingComponent)event.getComponent();
                mc.getHandleLayer().removeAllChildren();

                if (log.isDebugEnabled()) {
                    // Hole alle PFeatures die das Markierviereck schneiden
                    // und Hinzuf\u00FCgen dieser PFeatures zur Selektion
                    log.debug("Markierviereck = " + rectangle.getBounds() + event); // NOI18N
                }

                // geometrie der aufgezogenen BoundingBox erzeugen
                final Geometry geom = createGeometryFromRectangle();

                // Feature zur Liste der markierten Features hinzufügen, falls dessen
                // Geometrie sich mit der Geometrie der BoundingBox überschneidet
                final List<Feature> markedFeatures = new ArrayList<>();
                for (final Feature feature : mc.getFeatureCollection().getAllFeatures()) {
                    if ((feature instanceof Attachable) && feature.getGeometry().intersects(geom)) {
                        markedFeatures.add(feature);
                    }
                }

                if (markedFeatures.isEmpty()) { // kein Objekt ausgewählt
                    // nichts tun
                } else if (markedFeatures.size() == 1) { // genau 1 Feature markiert
                    featureToAttach = mc.getPFeatureHM().get(markedFeatures.get(0));
                    postFeatureAttachRequest();
                } else {                                 // mehr als ein Feature markiert
                    JOptionPane.showMessageDialog(
                        StaticSwingTools.getParentFrame(mc),
                        "Bitte nur ein Objekt zum Zuordnen auswählen.",
                        "Mehr als ein Objekt markiert.",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void postFeatureAttachRequest() {
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(ATTACH_FEATURE_NOTIFICATION, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getFeatureToAttach() {
        return featureToAttach;
    }
}
