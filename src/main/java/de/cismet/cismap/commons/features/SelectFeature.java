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

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class SelectFeature extends AbstractNewFeature implements DoubleClickableFeature {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SelectFeature.class);

    //~ Instance fields --------------------------------------------------------

    private String inputListenerName;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectFeature object.
     *
     * @param  g                  DOCUMENT ME!
     * @param  inputListenerName  DOCUMENT ME!
     */
    public SelectFeature(final Geometry g, final String inputListenerName) {
        super(g);

        this.inputListenerName = inputListenerName;
    }

    /**
     * Creates a new SelectFeature object.
     *
     * @param  point  DOCUMENT ME!
     * @param  wtst   DOCUMENT ME!
     */
    public SelectFeature(final Point2D point, final WorldToScreenTransform wtst) {
        super(point, wtst);
    }

    /**
     * Creates a new SelectFeature object.
     *
     * @param  canvasPoints  DOCUMENT ME!
     * @param  wtst          DOCUMENT ME!
     */
    public SelectFeature(final Point2D[] canvasPoints,
            final WorldToScreenTransform wtst) {
        super(canvasPoints, wtst);
    }

    /**
     * Creates a new SelectFeature object.
     *
     * @param  coordArr  DOCUMENT ME!
     * @param  wtst      DOCUMENT ME!
     */
    public SelectFeature(final Coordinate[] coordArr, final WorldToScreenTransform wtst) {
        super(coordArr, wtst);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getInputListenerName() {
        return inputListenerName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  inputListenerName  DOCUMENT ME!
     */
    public void setInputListenerName(final String inputListenerName) {
        this.inputListenerName = inputListenerName;
    }

    @Override
    public Paint getFillingPaint() {
        final SelectionListener selectListener = ((SelectionListener)CismapBroker.getInstance().getMappingComponent()
                        .getInputListener(
                            MappingComponent.SELECT));
        return selectListener.getSelectColor();
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
                            SelectFeature.class,
                            "SelectFeature.etName().selectRectangle");     // NOI18N
                }
                case LINESTRING: {
                    return org.openide.util.NbBundle.getMessage(
                            SelectFeature.class,
                            "SelectFeature.getName().selectPolyline");     // NOI18N
                }
                case ELLIPSE: {
                    return org.openide.util.NbBundle.getMessage(
                            SelectFeature.class,
                            "SelectFeature.getName().selectEllipse");      // NOI18N
                }
                case POINT: {
                    return org.openide.util.NbBundle.getMessage(
                            SelectFeature.class,
                            "SelectFeature.getName().selectPoint");        // NOI18N
                }
                case POLYGON: {
                    return org.openide.util.NbBundle.getMessage(
                            SelectFeature.class,
                            "SelectFeature.getName().selectPOLYGON");      // NOI18N
                }
                case MULTIPOLYGON: {
                    return org.openide.util.NbBundle.getMessage(
                            SelectFeature.class,
                            "SelectFeature.getName().selectMULTIPOLYGON"); // NOI18N
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
        return "Auswahl";
    }

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        final int strokeWidth = 4;
        final int imageSize = 24;
        final int circleSize = imageSize - (strokeWidth * 2);
        // 0 | strokeWidth | circleSize | strokeWidth | imageSize

        final Color color = (Color)getFillingPaint();
        final BufferedImage bufferedImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = (Graphics2D)bufferedImage.getGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(strokeWidth));
        graphics.drawOval(strokeWidth, strokeWidth, circleSize, circleSize);

        final FeatureAnnotationSymbol pointAnnotationSymbol = FeatureAnnotationSymbol
                    .newCustomSweetSpotFeatureAnnotationSymbol(bufferedImage, null, 0.5, 0.5);

        return pointAnnotationSymbol;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getInteractionMode() {
        return inputListenerName;
    }

    @Override
    public void doubleClickPerformed(final SelectionListener selectionListener) {
        final MappingComponent mappingComponent = selectionListener.getMappingComponent();
        ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).unselectAll();
        mappingComponent.getHandleLayer().removeAllChildren();
        selectionListener.select(this);
    }
}
