/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MetaSearchCreateSearchGeometryListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class SearchFeature extends PureNewFeature {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SearchFeature.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SearchFeature object.
     *
     * @param  g  DOCUMENT ME!
     */
    public SearchFeature(final Geometry g) {
        super(g);
    }

    /**
     * Creates a new SearchFeature object.
     *
     * @param  point  DOCUMENT ME!
     * @param  wtst   DOCUMENT ME!
     */
    public SearchFeature(final Point2D point, final WorldToScreenTransform wtst) {
        super(point, wtst);
    }

    /**
     * Creates a new SearchFeature object.
     *
     * @param  canvasPoints  DOCUMENT ME!
     * @param  wtst          DOCUMENT ME!
     */
    public SearchFeature(final Point2D[] canvasPoints, final WorldToScreenTransform wtst) {
        super(canvasPoints, wtst);
    }

    /**
     * Creates a new SearchFeature object.
     *
     * @param  coordArr  DOCUMENT ME!
     * @param  wtst      DOCUMENT ME!
     */
    public SearchFeature(final Coordinate[] coordArr, final WorldToScreenTransform wtst) {
        super(coordArr, wtst);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Paint getFillingPaint() {
        final MetaSearchCreateSearchGeometryListener searchListener = ((MetaSearchCreateSearchGeometryListener)
                CismapBroker.getInstance().getMappingComponent().getInputListener(
                    MappingComponent.CREATE_SEARCH_POLYGON));
        final Color color = searchListener.getSearchColor();
        return new Color(color.getRed(),
                color.getGreen(),
                color.getBlue(),
                255
                        - (int)(255f * searchListener.getSearchTransparency()));
    }

    @Override
    public Paint getLinePaint() {
        final Color color = (Color)getFillingPaint();
        return color.darker();
    }

    @Override
    public String getName() {
        if (getGeometryType() != null) {
            switch (getGeometryType()) {
                case RECTANGLE: {
                    return org.openide.util.NbBundle.getMessage(
                            PureNewFeature.class,
                            "SearchFeature.getName().searchRectangle");    // NOI18N
                }
                case LINESTRING: {
                    return org.openide.util.NbBundle.getMessage(
                            PureNewFeature.class,
                            "SearchFeature.getName().searchPolyline");     // NOI18N
                }
                case ELLIPSE: {
                    return org.openide.util.NbBundle.getMessage(
                            PureNewFeature.class,
                            "SearchFeature.getName().searchEllipse");      // NOI18N
                }
                case POINT: {
                    return org.openide.util.NbBundle.getMessage(
                            PureNewFeature.class,
                            "SearchFeature.getName().searchPoint");        // NOI18N
                }
                case POLYGON: {
                    return org.openide.util.NbBundle.getMessage(
                            PureNewFeature.class,
                            "SearchFeature.getName().searchPOLYGON");      // NOI18N
                }
                case MULTIPOLYGON: {
                    return org.openide.util.NbBundle.getMessage(
                            PureNewFeature.class,
                            "SearchFeature.getName().searchMULTIPOLYGON"); // NOI18N
                }
                default: {
                    if (super.getName() != null) {
                        return super.getName();
                    } else {
                        return "---";
                    }
                }
            }
        } else {
            return "--";
        }
    }

    @Override
    public String getType() {
        return "Suche";
    }
}
