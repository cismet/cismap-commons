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

import javax.swing.ImageIcon;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

/**
 * This feature will be used for the drawing mode.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DrawingFeature extends PureNewFeature implements FeatureWithId, DrawingFeatureInterface {

    //~ Static fields/initializers ---------------------------------------------

    private static final ImageIcon textAnnotationSymbol = new javax.swing.ImageIcon(DrawingFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/transparentPoint.png"));

    //~ Instance fields --------------------------------------------------------

    private String name;
    private FeatureAnnotationSymbol featureAnnotationSymbol = null;
    private int id = -1;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DrawingFeature object.
     *
     * @param  g  DOCUMENT ME!
     */
    public DrawingFeature(final Geometry g) {
        super(g);
    }

    /**
     * Creates a new DrawingFeature object.
     *
     * @param  coordArr  DOCUMENT ME!
     * @param  wtst      DOCUMENT ME!
     */
    public DrawingFeature(final Coordinate[] coordArr, final WorldToScreenTransform wtst) {
        super(coordArr, wtst);
    }

    /**
     * Creates a new DrawingFeature object.
     *
     * @param  point  DOCUMENT ME!
     * @param  wtst   DOCUMENT ME!
     */
    public DrawingFeature(final Point2D point, final WorldToScreenTransform wtst) {
        super(point, wtst);
    }

    /**
     * Creates a new DrawingFeature object.
     *
     * @param  canvasPoints  DOCUMENT ME!
     * @param  wtst          DOCUMENT ME!
     */
    public DrawingFeature(final Point2D[] canvasPoints, final WorldToScreenTransform wtst) {
        super(canvasPoints, wtst);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Paint getFillingPaint() {
        return new Color(1f, 0f, 0f);
    }

    @Override
    public void setName(final String name) {
        this.name = name;
        setPrimaryAnnotation(name);
    }

    @Override
    public void setGeometryType(final geomTypes geomType) {
        super.setGeometryType(geomType);

        if (AbstractNewFeature.geomTypes.TEXT.equals(geomType)) {
            setPrimaryAnnotationVisible(true);
//            setAutoScale(true);
            final FeatureAnnotationSymbol symbol = new FeatureAnnotationSymbol(textAnnotationSymbol.getImage());
            symbol.setSweetSpotX(0);
            symbol.setSweetSpotY(0);
            symbol.setOffset(0, 0);
            setPointAnnotationSymbol(symbol);
        }
    }

    @Override
    public void setPrimaryAnnotationJustification(final float just) {
        super.setPrimaryAnnotationJustification(just);
    }

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        return featureAnnotationSymbol;
    }

    @Override
    public void setPointAnnotationSymbol(final FeatureAnnotationSymbol featureAnnotationSymbol) {
        this.featureAnnotationSymbol = featureAnnotationSymbol;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getTypeOrder() {
        return TYPE_ORDER.indexOf(getGeometryType());
    }

    @Override
    public float getTransparency() {
        return 0.0f;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static int getTypeOrderCount() {
        return TYPE_ORDER.size();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(final int id) {
        this.id = id;
    }

    @Override
    public String getIdExpression() {
        return null;
    }

    @Override
    public void setIdExpression(final String idExpression) {
    }
}
