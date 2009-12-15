package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateSearchGeometryListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;

/**
 *
 * @author jruiz
 */
public class SearchFeature extends PureNewFeature {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    public SearchFeature(Geometry g) {
        super(g);
    }

    public SearchFeature(Point2D point, WorldToScreenTransform wtst) {
        super(point, wtst);
    }

    public SearchFeature(final Point2D[] canvasPoints, WorldToScreenTransform wtst) {
        super(canvasPoints, wtst);
    }

    public SearchFeature(Coordinate[] coordArr, WorldToScreenTransform wtst) {
        super(coordArr, wtst);
    }

    @Override
    public Paint getFillingPaint() {
        Color color = ((CreateSearchGeometryListener)CismapBroker.getInstance().getMappingComponent().getInputListener(MappingComponent.CREATE_SEARCH_POLYGON)).getSearchColor();
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 127);
    }

    @Override
    public String getName() {
        if (getGeometryType() != null) {
            switch (getGeometryType()) {
                case RECTANGLE:
                    return "Such-Rechteck";
                case LINESTRING:
                    return "Such-Linienzug";
                case ELLIPSE:
                    return "Such-Ellipse";
                case POINT:
                    return "Such-Punkt";
                case POLYGON:
                    return "Such-Polygon";
                default:
                    return "Error in getName()";
            }
        } else {
            return "Error in getName()";
        }
    }

    @Override
    public String getType() {
        return "Metasuche";
    }

}
