package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.MapSearchEvent;
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
    private PureNewFeature lastSearchFeature;

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

        // neue Suche mit Geometrie auslösen
        MapSearchEvent mse = new MapSearchEvent();
        mse.setGeometry(newFeature.getGeometry());
        CismapBroker.getInstance().fireMapSearchInited(mse);

        lastSearchFeature = newFeature;

        PFeature pFeature = (PFeature)mc.getPFeatureHM().get(newFeature);

        if (isHoldingGeometries()) {
            pFeature.moveToFront();
            newFeature.setEditable(true);
            mc.getFeatureCollection().holdFeature(newFeature);
        } else {
            mc.getTmpFeatureLayer().addChild(pFeature);            
            pFeature.animateToTransparency(0, 2500);
        }
    }

    public void repeatLastSearch() {
        performSearch(lastSearchFeature);
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
        return lastSearchFeature;
    }

    public void performSearch(PureNewFeature searchFeature) {
        if (searchFeature != null) {
            mc.getTmpFeatureLayer().removeAllChildren();

            tempFeature = new PPath();
            tempFeature.setStroke(new FixedWidthStroke());
            if (isInMode(POLYGON) || isInMode(RECTANGLE) || isInMode(ELLIPSE)) {
                tempFeature.setPaint(getFillingColor());
            }
            Vector<Point2D> points = new Vector<Point2D>();
            for (Coordinate coord : searchFeature.getGeometry().getCoordinates()) {
                points.add(new Point2D.Double(coord.x, coord.y));
            }
            tempFeature.setPathToPolyline(points.toArray(new Point2D[0]));
            mc.getTmpFeatureLayer().addChild(tempFeature);

            finishGeometry(searchFeature);
        }
    }

}
