/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.util.Vector;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.MapSearchEvent;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class CreateSearchGeometryListener extends CreateGeometryListener {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private boolean holdGeometries = false;
    private Color searchColor = Color.GREEN;
    private float searchTransparency = 0.5f;
    private PureNewFeature lastFeature;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CreateSearchGeometryListener object.
     *
     * @param  mc  DOCUMENT ME!
     */
    public CreateSearchGeometryListener(final MappingComponent mc) {
        super(mc, SearchFeature.class);

        this.mc = mc;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Color getFillingColor() {
        return new Color(searchColor.getRed(),
                searchColor.getGreen(),
                searchColor.getBlue(),
                255
                        - (int)(255f * searchTransparency));
    }

    @Override
    protected void finishGeometry(final PureNewFeature newFeature) {
        super.finishGeometry(newFeature);
        mc.getFeatureCollection().addFeature(newFeature);

        doSearch(newFeature);

        cleanup(newFeature);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    private void cleanup(final PureNewFeature feature) {
        final PFeature pFeature = (PFeature)mc.getPFeatureHM().get(feature);
        if (isHoldingGeometries()) {
            pFeature.moveToFront(); // funktioniert nicht?!
            feature.setEditable(true);
            mc.getFeatureCollection().holdFeature(feature);
        } else {
            mc.getTmpFeatureLayer().addChild(pFeature);

            // Transparenz animieren
            pFeature.animateToTransparency(0, 2500);
            // warten bis Animation zu Ende ist um Feature aus Liste zu entfernen
            new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while (pFeature.getTransparency() > 0) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                            }
                        }
                        mc.getFeatureCollection().removeFeature(feature);
                    }
                }).start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  searchFeature  DOCUMENT ME!
     */
    private void doSearch(final PureNewFeature searchFeature) {
        // Suche
        final MapSearchEvent mse = new MapSearchEvent();
        mse.setGeometry(searchFeature.getGeometry());
        CismapBroker.getInstance().fireMapSearchInited(mse);

        // letzte Suchgeometrie merken
        lastFeature = searchFeature;
    }

    /**
     * DOCUMENT ME!
     */
    public void redoLastSearch() {
        search(lastFeature);
    }

    /**
     * DOCUMENT ME!
     */
    public void showLastFeature() {
        showFeature(lastFeature);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    private void showFeature(final PureNewFeature feature) {
        if (feature != null) {
            feature.setEditable(feature.getGeometryType() != PureNewFeature.geomTypes.MULTIPOLYGON);

            mc.getFeatureCollection().addFeature(feature);
            if (isHoldingGeometries()) {
                mc.getFeatureCollection().holdFeature(feature);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isHoldingGeometries() {
        return holdGeometries;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  holdGeometries  DOCUMENT ME!
     */
    public void setHoldGeometries(final boolean holdGeometries) {
        this.holdGeometries = holdGeometries;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public float getSearchTransparency() {
        return searchTransparency;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  searchTransparency  DOCUMENT ME!
     */
    public void setSearchTransparency(final float searchTransparency) {
        this.searchTransparency = searchTransparency;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Color getSearchColor() {
        final Color filling = getFillingColor();
        return new Color(filling.getRed(), filling.getGreen(), filling.getBlue());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  color  DOCUMENT ME!
     */
    public void setSearchColor(final Color color) {
        this.searchColor = color;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PureNewFeature getLastSearchFeature() {
        return lastFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  searchFeature  DOCUMENT ME!
     */
    public void search(final PureNewFeature searchFeature) {
        if (searchFeature != null) {
            doSearch(searchFeature);
            showFeature(searchFeature);
            cleanup(searchFeature);
        }
    }

    @Override
    public void mousePressed(final PInputEvent pInputEvent) {
        final boolean progressBefore = inProgress;
        super.mousePressed(pInputEvent);

        if ((!inProgress || (!progressBefore && inProgress)) && (pInputEvent.getClickCount() == 2)) {
            final Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[] { PFeature.class });
            if (o instanceof PFeature) {
                final PFeature sel = (PFeature)o;
                if (sel.getFeature() instanceof SearchFeature) {
                    if (pInputEvent.isLeftMouseButton()) {
                        mc.getHandleLayer().removeAllChildren();
                        // neue Suche mit Geometry auslösen
                        ((CreateSearchGeometryListener)mc.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON))
                                .search((SearchFeature)sel.getFeature());
                    }
                }
            }
        }
    }
}
