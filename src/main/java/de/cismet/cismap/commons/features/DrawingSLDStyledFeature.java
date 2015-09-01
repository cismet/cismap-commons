/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;

import org.deegree.commons.utils.Triple;
import org.deegree.style.styling.LineStyling;
import org.deegree.style.styling.PointStyling;
import org.deegree.style.styling.PolygonStyling;
import org.deegree.style.styling.Styling;
import org.deegree.style.styling.TextStyling;

import org.jfree.util.Log;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.ImageIcon;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.featureservice.DefaultLayerProperties;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.gui.piccolo.FixedPImage;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.PSticky;

/**
 * This feature class is used within the drawing mode.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DrawingSLDStyledFeature extends DefaultFeatureServiceFeature implements DrawingFeatureInterface,
    AnnotatedFeature {

    //~ Static fields/initializers ---------------------------------------------

    private static final ImageIcon textAnnotationSymbol = new javax.swing.ImageIcon(DrawingFeature.class.getResource(
                "/de/cismet/cismap/commons/gui/res/transparentPoint.png"));

    //~ Instance fields --------------------------------------------------------

    private AbstractNewFeature.geomTypes geomType = AbstractNewFeature.geomTypes.UNKNOWN;
    private String text;
    private boolean autoscale = false;

    //~ Instance initializers --------------------------------------------------

    {
        final DefaultLayerProperties layerProps = new DefaultLayerProperties();
        layerProps.getStyle().setMaxScale(Integer.MAX_VALUE);
        setLayerProperties(layerProps);
    }

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DrawingSLDStyledFeature object.
     */
    public DrawingSLDStyledFeature() {
    }

    /**
     * Creates a new DrawingSLDStyledFeature object.
     *
     * @param  feature  DOCUMENT ME!
     */
    public DrawingSLDStyledFeature(final DrawingFeature feature) {
        if (feature.getGeometryType() != null) {
            setGeometryType(feature.getGeometryType());
        }
        text = feature.getName();
        setId(feature.getId());
        setGeometry(feature.getGeometry());
        if (feature.getGeometryType().equals(geomType)) {
            setAutoScale(feature.isAutoscale());
            if (feature.getPrimaryAnnotationHalo() != null) {
                setPrimaryAnnotationHalo(feature.getPrimaryAnnotationHalo());
            }
            setPrimaryAnnotationFont(feature.getPrimaryAnnotationFont());
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getTypeOrder() {
        return TYPE_ORDER.indexOf(getGeometryType());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the geomType
     */
    @Override
    public AbstractNewFeature.geomTypes getGeometryType() {
        return geomType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pfeature  DOCUMENT ME!
     * @param  wtst      DOCUMENT ME!
     */
    @Override
    public void applyStyle(final PFeature pfeature, final WorldToScreenTransform wtst) {
        if (geomType.equals(AbstractNewFeature.geomTypes.TEXT)) {
            return;
        }
        if (styles == null) {
            return;
        }
        stylings = null;
        pfeature.setStrokePaint(null);
        pfeature.setStroke(null);
        pfeature.setPaintOnAllFeatures(null);
        pfeature.setPaint(null);
        for (final org.deegree.style.se.unevaluated.Style tempStyle : styles) {
            final org.deegree.style.se.unevaluated.Style filteredStyle = tempStyle.filter(
                    pfeature.getMappingComponent().getScaleDenominator());
            final LinkedList<Triple<Styling, LinkedList<org.deegree.geometry.Geometry>, String>> tempStylings =
                filteredStyle.evaluate(getDeegreeFeature(), evaluator);
            if (stylings == null) {
                stylings = tempStylings;
            } else {
                stylings.addAll(tempStylings);
            }
        }

        /*
         * if (stylings == null) { if (style == null) {     return; } this.stylings =
         * style.evaluate(getDeegreeFeature(), evaluator);}*/
        if ((stylings == null) || (stylings.size() == 0)) {
            return;
        }

        if (geomType.equals(AbstractNewFeature.geomTypes.POINT) && !hasPointStyling()) {
            return;
        }

        if (!geomType.equals(AbstractNewFeature.geomTypes.TEXT)) {
            final ListIterator it = pfeature.getChildrenIterator();
            while (it.hasNext()) {
                final Object child = it.next();
                if (child instanceof PSticky) {
                    pfeature.getMappingComponent().removeStickyNode((PSticky)child);
                }
            }
            pfeature.removeAllChildren();
        }
        pfeature.sldStyledPolygon.clear();

        for (final PImage image : pfeature.sldStyledImage) {
            if (image instanceof PSticky) {
                pfeature.getMappingComponent().removeStickyNode((PSticky)image);
            }
        }
        pfeature.sldStyledImage.clear();

        for (final PFeature.PTextWithDisplacement text : pfeature.sldStyledText) {
            pfeature.getMappingComponent().removeStickyNode(text);
        }
        pfeature.sldStyledText.clear();

        final Geometry geom = pfeature.getFeature().getGeometry();
        int polygonNr = -1;
        int textNr = 0;
        int imageNr = 0;
        for (final Triple<Styling, LinkedList<org.deegree.geometry.Geometry>, String> styling : stylings) {
            if ((styling.first instanceof PolygonStyling)
                        && ((geom instanceof Polygon) || (geom instanceof MultiPolygon))) {
                PPath path;
                if (polygonNr < 0) {
                    path = pfeature;
                } else {
                    try {
                        path = pfeature.sldStyledPolygon.get(polygonNr);
                    } catch (IndexOutOfBoundsException ex) {
                        path = new PPath();
                        pfeature.sldStyledPolygon.add(path);
                        pfeature.addChild(path);
                    }
                    path.setPathTo(pfeature.getPathReference());
                }
                applyPolygonStyling(path, (PolygonStyling)styling.first, pfeature.getMappingComponent());
                polygonNr++;
            } else if ((styling.first instanceof LineStyling)
                        && ((geom instanceof LineString) || (geom instanceof MultiLineString))) {
                PPath path;
                if (polygonNr < 0) {
                    path = pfeature;
                } else {
                    try {
                        path = pfeature.sldStyledPolygon.get(polygonNr);
                    } catch (IndexOutOfBoundsException ex) {
                        path = new PPath();
                        pfeature.sldStyledPolygon.add(path);
                        pfeature.addChild(path);
                    }
                    path.setPathTo(pfeature.getPathReference());
                }
                applyLineStyling(path, (LineStyling)styling.first, pfeature.getMappingComponent());
                polygonNr++;
            } else if (styling.first instanceof TextStyling) {
                PFeature.PTextWithDisplacement text;
                try {
                    text = pfeature.sldStyledText.get(textNr++);
                } catch (IndexOutOfBoundsException ex) {
                    text = pfeature.new PTextWithDisplacement();
                    pfeature.sldStyledText.add(text);
                    pfeature.addChild(text);
                    pfeature.getMappingComponent().addStickyNode(text);
                }
                final Point intPoint = CrsTransformer.transformToGivenCrs(
                            getGeometry(),
                            pfeature.getMappingComponent().getMappingModel().getSrs().getCode())
                            .getInteriorPoint();
                applyTextStyling(
                    text,
                    styling.third,
                    (TextStyling)styling.first,
                    wtst,
                    intPoint.getX(),
                    intPoint.getY());
                rescaleStickyNode(pfeature, text);
            } else if ((styling.first instanceof PointStyling)
                        && ((geom instanceof Point) || (geom instanceof MultiPoint))) {
                PImage image;
                PImage selectedImage;
                try {
                    image = pfeature.sldStyledImage.get(imageNr);
                    selectedImage = pfeature.sldStyledSelectedImage.get(imageNr++);
                } catch (IndexOutOfBoundsException ex) {
                    if (((PointStyling)styling.first).uom == org.deegree.style.styling.components.UOM.Pixel) {
                        image = new FixedPImage();
                        pfeature.getMappingComponent().addStickyNode((PSticky)image);
                        selectedImage = new FixedPImage();
                        pfeature.getMappingComponent().addStickyNode((PSticky)selectedImage);
                    } else {
                        image = new PImage();
                        selectedImage = new PImage();
                    }
                    // image = new PImageWithDisplacement();
                    pfeature.sldStyledImage.add(image);
                    pfeature.sldStyledSelectedImage.add(selectedImage);
                    pfeature.addChild(image);
                }
                if (((PointStyling)styling.first).uom == org.deegree.style.styling.components.UOM.Pixel) {
                    if (!(image instanceof FixedPImage)) {
                        pfeature.removeChild(image);
                        pfeature.sldStyledImage.remove(image);
                        pfeature.sldStyledSelectedImage.remove(selectedImage);
                        image = new FixedPImage();
                        pfeature.sldStyledImage.add(image);
                        pfeature.sldStyledSelectedImage.add(selectedImage);
                        pfeature.addChild(image);
                        pfeature.addChild(selectedImage);
                        pfeature.getMappingComponent().addStickyNode((PSticky)image);
                        pfeature.getMappingComponent().addStickyNode((PSticky)selectedImage);
                    }
                } else {
                    if (image instanceof FixedPImage) {
                        pfeature.getMappingComponent().removeStickyNode((PSticky)image);
                        pfeature.sldStyledImage.remove(image);
                        pfeature.sldStyledSelectedImage.remove(selectedImage);
                        pfeature.removeChild(image);
                        image = new PImage();
                        pfeature.sldStyledImage.add(image);
                        pfeature.sldStyledSelectedImage.add(selectedImage);
                        pfeature.addChild(image);
                    }
                }
                final Point intPoint = CrsTransformer.transformToGivenCrs(
                            getGeometry(),
                            pfeature.getMappingComponent().getMappingModel().getSrs().getCode())
                            .getInteriorPoint();
                applyPointStyling(
                    image,
                    (PointStyling)styling.first,
                    wtst,
                    intPoint.getX(),
                    intPoint.getY(),
                    pfeature.getMappingComponent().getCamera(),
                    false);
                applyPointStyling(
                    selectedImage,
                    (PointStyling)styling.first,
                    wtst,
                    intPoint.getX(),
                    intPoint.getY(),
                    pfeature.getMappingComponent().getCamera(),
                    true);
                if (((PointStyling)styling.first).uom == org.deegree.style.styling.components.UOM.Pixel) {
                    rescaleStickyNode(pfeature, (PSticky)image);
                    rescaleStickyNode(pfeature, (PSticky)selectedImage);
                }
            }
        }
        if ((polygonNr == -1) && (imageNr == 0) && (textNr == 0)) {
            Log.warn("Es wurde kein passender Symbolizer für das Feature gefunden, Darstellung unmöglich.");
        }
        /*
         * //if (stylings.getFirst().first instanceof PolygonStyling) { applyStyling(pfeature,
         * stylings.getFirst().first); //}
         *
         * while (pfeature.sldStyledPolygon.size() < (stylings.size() - 1)) { final PPath child = new PPath();
         * pfeature.sldStyledPolygon.add(child); pfeature.addChild(child); }
         *
         * for (int i = 0; i < pfeature.sldStyledPolygon.size(); i++) { //
         * pfeature.sldStyled.get(i).getPathReference().reset();
         * pfeature.sldStyledPolygon.get(i).setPathTo(pfeature.getPathReference());
         * applyStyling(pfeature.sldStyledPolygon.get(i), stylings.get(i + 1).first);}*/
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean hasPointStyling() {
        int styles = 0;
        for (final Triple<Styling, LinkedList<org.deegree.geometry.Geometry>, String> styling : stylings) {
            if (styling.first instanceof PointStyling) {
                ++styles;

                if (styles > 1) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String getPrimaryAnnotation() {
        if (AbstractNewFeature.geomTypes.TEXT.equals(geomType)) {
            return getText();
        } else {
            return super.getPrimaryAnnotation();
        }
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isAutoscale() {
        return autoscale;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  autoScale  DOCUMENT ME!
     */
    @Override
    public void setAutoScale(final boolean autoScale) {
        this.autoscale = autoScale;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geomType  the geomType to set
     */
    public void setGeometryType(final AbstractNewFeature.geomTypes geomType) {
        if (geomType == null) {
            this.geomType = AbstractNewFeature.geomTypes.UNKNOWN;
        } else {
            this.geomType = geomType;
        }
    }

    @Override
    public boolean isPrimaryAnnotationVisible() {
        if (AbstractNewFeature.geomTypes.TEXT.equals(geomType)) {
            return true;
        } else {
            return super.isPrimaryAnnotationVisible();
        }
    }

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        if (AbstractNewFeature.geomTypes.TEXT.equals(geomType)) {
            final FeatureAnnotationSymbol symbol = new FeatureAnnotationSymbol(textAnnotationSymbol.getImage());
            symbol.setSweetSpotX(0);
            symbol.setSweetSpotY(0);
            symbol.setOffset(0, 0);
            return symbol;
        } else {
            return super.getPointAnnotationSymbol();
        }
    }

    @Override
    public float getTransparency() {
        if ((styles == null) || styles.isEmpty()) {
            return this.getStyle().getAlpha();
        } else {
            return 1;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the text
     */
    public String getText() {
        return text;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  text  the text to set
     */
    public void setText(final String text) {
        this.text = text;
    }

    @Override
    public String getName() {
        return getText();
    }
}
