package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.MapSearchEvent;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PPath;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Vector;

/**
 * @author jruiz
 */
public class CreateSearchGeometryListener extends CreateGeometryListener {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private boolean holdGeometries = false;
    private Color searchColor = Color.GREEN;
    private PureNewFeature lastFeature;

    public CreateSearchGeometryListener(MappingComponent mc) {
        super(mc, SearchFeature.class);

        this.mc = mc;
    }
    
    @Override
    protected Color getFillingColor() {
        return new Color(searchColor.getRed(), searchColor.getGreen(), searchColor.getBlue(), 127);
    }

    @Override
    protected void finishGeometry(PureNewFeature newFeature) {
        super.finishGeometry(newFeature);
        mc.getFeatureCollection().addFeature(newFeature);

        doSearch(newFeature);

        cleanup(newFeature);
    }

    private void cleanup(final PureNewFeature feature) {
        final PFeature pFeature = (PFeature)mc.getPFeatureHM().get(feature);
        if (isHoldingGeometries()) {
            pFeature.moveToFront(); //funktioniert nicht?!
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

    private void doSearch(PureNewFeature searchFeature) {

        // Suche
        MapSearchEvent mse = new MapSearchEvent();
        mse.setGeometry(searchFeature.getGeometry());
        CismapBroker.getInstance().fireMapSearchInited(mse);

        // letzte Suchgeometrie merken
        lastFeature = searchFeature;
    }

    public void redoLastSearch() {
        search(lastFeature);
    }

    public void showLastFeature() {
        showFeature(lastFeature);
    }

    private void showFeature(PureNewFeature feature) {
        if (feature != null) {
            PPath tmpFeature = new PPath();
            tmpFeature.setStroke(new FixedWidthStroke());
            // Alles außer Linie wird mit Farbe gefüllt
            if (!isInMode(LINESTRING)) {
                tmpFeature.setPaint(getFillingColor());
            }
            // Punkte abfragen und in neues Feature übertragen
            Vector<Point2D> points = new Vector<Point2D>();
            for (Coordinate coord : feature.getGeometry().getCoordinates()) {
                points.add(new Point2D.Double(coord.x, coord.y));
            }
            tmpFeature.setPathToPolyline(points.toArray(new Point2D[0]));

            feature.setEditable(true);
            mc.getFeatureCollection().addFeature(feature);
            if (isHoldingGeometries()) {
                mc.getFeatureCollection().holdFeature(feature);
            }
        }
    }

    public boolean isHoldingGeometries() {
        return holdGeometries;
    }

    public void setHoldGeometries(boolean holdGeometries) {
        this.holdGeometries = holdGeometries;
    }

    public Color getSearchColor() {
        Color filling = getFillingColor();
        return new Color(filling.getRed(), filling.getGreen(), filling.getBlue());
    }

    public void setSearchColor(Color color) {
        this.searchColor = color;
    }

    public PureNewFeature getLastSearchFeature() {
        return lastFeature;
    }

    public void search(PureNewFeature searchFeature) {
        if (searchFeature != null) {
            showFeature(searchFeature);
            doSearch(searchFeature);
            cleanup(searchFeature);
        }
    }

}
