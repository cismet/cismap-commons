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
        CreateSearchGeometryListener searchListener = ((CreateSearchGeometryListener)CismapBroker.getInstance().getMappingComponent().getInputListener(MappingComponent.CREATE_SEARCH_POLYGON));
        Color color = searchListener.getSearchColor();
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 255 - (int)(255f * searchListener.getSearchTransparency()));
    }

    @Override
    public Paint getLinePaint() {
        Color color = (Color)getFillingPaint();
        return color.darker();
    }

    @Override
    public String getName() {
        if (getGeometryType() != null) {
            switch (getGeometryType()) {
                case RECTANGLE:
                    return org.openide.util.NbBundle.getMessage(SearchFeature.class, "SearchFeature.name.searchRectangle") ;
                case LINESTRING:
                    return org.openide.util.NbBundle.getMessage(SearchFeature.class, "SearchFeature.name.searchPolyline") ;
                case ELLIPSE:
                    return org.openide.util.NbBundle.getMessage(SearchFeature.class, "SearchFeature.name.searchEllipse") ;
                case POINT:
                    return org.openide.util.NbBundle.getMessage(SearchFeature.class, "SearchFeature.name.searchPoint") ;
                case POLYGON:
                    return org.openide.util.NbBundle.getMessage(SearchFeature.class, "SearchFeature.name.searchPOLYGON") ;
                default:
                    return org.openide.util.NbBundle.getMessage(SearchFeature.class, "SearchFeature.name.errorInGetName") ;
            }
        } else {
            return org.openide.util.NbBundle.getMessage(SearchFeature.class, "SearchFeature.name.errorInGetName") ;
        }
    }

    @Override
    public String getType() {
        return "Metasuche";
    }

}
