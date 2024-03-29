/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import com.vividsolutions.jts.operation.buffer.OffsetCurveBuilder;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;

import org.apache.log4j.Logger;
import org.apache.xerces.xs.XSElementDeclaration;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.GMLObjectCategory;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Triple;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.feature.Feature;
import org.deegree.feature.property.ExtraProps;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.style.styling.LineStyling;
import org.deegree.style.styling.PointStyling;
import org.deegree.style.styling.PolygonStyling;
import org.deegree.style.styling.Styling;
import org.deegree.style.styling.TextStyling;
import org.deegree.style.styling.components.Fill;
import org.deegree.style.styling.components.Graphic;
import org.deegree.style.styling.components.Halo;
import org.deegree.style.styling.components.Mark;
import org.deegree.style.styling.components.Stroke;

import org.jfree.util.Log;

import sun.awt.image.ToolkitImage;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.awt.image.RescaleOp;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.File;

import java.lang.reflect.Constructor;

import java.math.BigDecimal;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.ImageIcon;

import javax.xml.namespace.QName;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.style.Style;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.piccolo.CustomFixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.gui.piccolo.FixedPImage;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.PSticky;
import de.cismet.cismap.commons.gui.piccolo.SelectionAwareTexturePaint;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.styling.CustomStyle;
import de.cismet.cismap.commons.styling.EndPointStyle;
import de.cismet.cismap.commons.styling.EndPointStyleDescription;

/**
 * Default implementation of a FeatureServiceFeature.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class DefaultFeatureServiceFeature implements FeatureServiceFeature, Comparable<DefaultFeatureServiceFeature> {

    //~ Static fields/initializers ---------------------------------------------

    protected static final String CLASS_ID = "class_id";
    protected static final String GEOMETRIE = "geo_field";
    protected static final String OBJECT_ID = "object_id";
    protected static Map<BufferedImage, BufferedImage> selectedImage = new HashMap<BufferedImage, BufferedImage>();
    private static AbstractDefaultGeometry defaultGeom = new DefaultPoint(null, null, null, new double[] { 0.0, 0.0 });

    //~ Instance fields --------------------------------------------------------

    protected Logger logger = Logger.getLogger(this.getClass());
    protected XPathEvaluator<Feature> evaluator = new DeegreeEvaluator();
    protected List<org.deegree.style.se.unevaluated.Style> styles;
    protected LinkedList<Triple<Styling, LinkedList<org.deegree.geometry.Geometry>, String>> stylings;
    // private final static org.apache.log4j.Logger logger =
    // org.apache.log4j.Logger.getLogger(DefaultFeatureServiceFeature.class);
    private int id = -1;
    private String primaryAnnotation;
    private String secondaryAnnotation;
    private boolean hiding = false;
    private boolean editable = false;
    private Boolean canBeSelected = null;
    private LinkedHashMap<String, Object> container = new LinkedHashMap<String, Object>();
    private Geometry geometry = null;
    private LayerProperties layerProperties;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private Paint customFillingStyle;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new uninitialised instance of DefaultFeatureServiceFeature. The id is set to -1, editable is set to
     * false, canBeSelected is set to true, hiding is set to false, any other properties set to null.
     */
    public DefaultFeatureServiceFeature() {
    }

    /**
     * Initialises a new DefaultFeatureServiceFeature instance from an existing FeatureServiceFeature object. The
     * properties of the FeatureServiceFeature will be cloned.
     *
     * @param  feature  layerProperties LayerProperties to be used for initialisation
     *
     * @see    FeatureServiceFeature#clone(Object)
     */
    public DefaultFeatureServiceFeature(final FeatureServiceFeature feature) {
        this.setId(feature.getId());
        this.setPrimaryAnnotation(feature.getPrimaryAnnotation());
        this.setSecondaryAnnotation(feature.getSecondaryAnnotation());
        this.hide(feature.isHidden());
        this.setEditable(feature.isEditable());
        this.setCanBeSelected(feature.canBeSelected());
        if (feature instanceof DefaultFeatureServiceFeature) {
            styles = ((DefaultFeatureServiceFeature)feature).styles;
        }

        if ((feature.getProperties() != null) && (feature.getProperties().size() > 0)) {
            // TODO: deep cloning of hashmap?
            this.container = new LinkedHashMap(feature.getProperties());
        }

        if (feature.getLayerProperties() != null) {
            this.setLayerProperties((LayerProperties)feature.getLayerProperties().clone());
        }

        if (feature.getGeometry() != null) {
            this.setGeometry((Geometry)feature.getGeometry().clone());
        }
    }

    /**
     * Creates a new initialised instance of DefaultFeatureServiceFeature. Editable is set to false, canBeSelected is
     * set to true, hiding is set to false.
     *
     * @param  id               the unique (within the layer or the feature collection) id of the feature
     * @param  geometry         the geometry of the feature
     * @param  layerProperties  (shared) layer properties object
     */
    public DefaultFeatureServiceFeature(final int id, final Geometry geometry, final LayerProperties layerProperties) {
        this.setId(id);
        this.setGeometry(geometry);
        this.setLayerProperties(layerProperties);
    }

    /**
     * Creates a new DefaultFeatureServiceFeature object.
     *
     * @param  id               DOCUMENT ME!
     * @param  geometry         DOCUMENT ME!
     * @param  layerProperties  DOCUMENT ME!
     * @param  styles           DOCUMENT ME!
     */
    public DefaultFeatureServiceFeature(final int id,
            final Geometry geometry,
            final LayerProperties layerProperties,
            final List<org.deegree.style.se.unevaluated.Style> styles) {
        this(id, geometry, layerProperties);
        this.styles = styles;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object clone() {
        return new DefaultFeatureServiceFeature(this);
    }

    /**
     * Fügt das übergebene Objekt dem PropertyContainer unter gegebenem Namen ein.
     *
     * @param  propertyName  Name und gleichzeitig Schlüssel
     * @param  property      einzufügendes Objekt
     */
    @Override
    public void addProperty(final String propertyName, final Object property) {
        container.put(propertyName, property);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  map  DOCUMENT ME!
     */
    public void addProperties(final Map<String, Object> map) {
        container.putAll(map);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  HashMap mit Properties
     */
    @Override
    public HashMap getProperties() {
        if (hasAdditionalProperties()) {
            final AttributeTableRuleSet ruleSet = layerProperties.getAttributeTableRuleSet();
            final String[] names = ruleSet.getAdditionalFieldNames();

            for (final String tmpName : names) {
                final Object value = ruleSet.getAdditionalFieldValue(tmpName,
                        this);
                container.put(tmpName, value);
            }
        }

        return container;
    }

    /**
     * Ersetzt den alten PropertieContainer mit einer neuen HashMap.
     *
     * @param  properties  neue Hashmap
     */
    @Override
    public void setProperties(final HashMap properties) {
        container = new LinkedHashMap(properties);
    }

    /**
     * Liefert die dem Namen zugeordnete Property.
     *
     * @param   propertyName  Name des gesuchten Objekts
     *
     * @return  Objekt aus der Hashmap
     */
    @Override
    public Object getProperty(final String propertyName) {
        Object o = container.get(propertyName);

        if (hasAdditionalProperties()) {
            final int index = layerProperties.getAttributeTableRuleSet().getIndexOfAdditionalFieldName(propertyName);

            if (index != Integer.MIN_VALUE) {
                o = layerProperties.getAttributeTableRuleSet().getAdditionalFieldValue(propertyName, this);
            }
        }

        return o;
    }

    /**
     * Entfernt die dem Namen zugeordnete Property aus der Hashmap.
     *
     * @param  propertyName  Name des zu löschenden Objekts
     */
    @Override
    public void removeProperty(final String propertyName) {
        container.remove(propertyName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  true, iff the service of this feature has additional properties, which are defined by a
     *          AttributeTableRuleSet
     */
    protected boolean hasAdditionalProperties() {
        if (layerProperties != null) {
            final AttributeTableRuleSet ruleSet = layerProperties.getAttributeTableRuleSet();

            if (ruleSet == null) {
                return false;
            } else {
                final String[] fieldNames = ruleSet.getAdditionalFieldNames();
                return (fieldNames != null) && (fieldNames.length > 0);
            }
        } else {
            return false;
        }
    }

    /**
     * Liefert die ID des DefaultWFSFeatures.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * Setzt die ID des DefaultWFSFeatures neu.
     *
     * @param  id  neue ID
     */
    @Override
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Liefert den ID-Ausdruck des DefaultWFSFeatures.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getIdExpression() {
        return this.layerProperties.getIdExpression();
    }

    /**
     * Setzt den ID-Ausdruck des DefaultWFSFeatures neu.
     *
     * @param  idExpression  neuer ID-Ausdruck
     */
    @Override
    public void setIdExpression(final String idExpression) {
        this.layerProperties.setIdExpression(idExpression, layerProperties.getIdExpressionType());
    }
    /**
     * /** * Erzeugt ein JDOM-Element, das das DefaultFeatureServiceFeature und dessen Attribute * widerspiegelt. *
     * &#064;return JDOM-Element
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public LayerProperties getLayerProperties() {
        return this.layerProperties;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layerProperties  DOCUMENT ME!
     */
    @Override
    public void setLayerProperties(final LayerProperties layerProperties) {
        this.layerProperties = layerProperties;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Paint getLinePaint() {
        if ((styles == null) || styles.isEmpty()) {
            return this.getStyle().isDrawLine() ? this.getStyle().getLineColor() : null;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  linePaint  DOCUMENT ME!
     */
    @Override
    public void setLinePaint(final Paint linePaint) {
        this.getStyle().setLineColor((Color)linePaint);
        this.getStyle().setDrawLine(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getLineWidth() {
        if ((styles == null) || styles.isEmpty()) {
            return this.getStyle().getLineWidth();
        } else {
            return 0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  width  DOCUMENT ME!
     */
    @Override
    public void setLineWidth(final int width) {
        this.getStyle().setLineWidth(width);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Paint getFillingPaint() {
        if (customFillingStyle != null) {
            return customFillingStyle;
        } else {
            if ((styles == null) || styles.isEmpty()) {
                return this.getStyle().isDrawFill() ? this.getStyle().getFillColor() : null;
            } else {
                return null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fillingStyle  DOCUMENT ME!
     */
    @Override
    public void setFillingPaint(final Paint fillingStyle) {
        customFillingStyle = fillingStyle;
//        this.getStyle().setFillColor((Color)fillingStyle);
//        this.getStyle().setDrawFill(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public float getTransparency() {
        if ((styles == null) || styles.isEmpty()) {
            return this.getStyle().getAlpha();
        } else {
            // if 0 will be returned, the handles of editable features will not be connected (the lines between the
            // handles are transparent)
            return 0.5f;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  transparrency  DOCUMENT ME!
     */
    @Override
    public void setTransparency(final float transparrency) {
        this.getStyle().setAlpha(transparrency);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        if ((styles == null) || styles.isEmpty()) {
            return this.getStyle().getPointSymbol();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureAnnotationSymbol  DOCUMENT ME!
     */
    @Override
    public void setPointAnnotationSymbol(final FeatureAnnotationSymbol featureAnnotationSymbol) {
        this.getStyle().setPointSymbol(featureAnnotationSymbol);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isHighlightingEnabled() {
        if ((styles == null) || styles.isEmpty()) {
            return this.getStyle().isHighlightFeature();
        } else {
            return true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  enabled  DOCUMENT ME!
     */
    @Override
    public void setHighlightingEnabled(final boolean enabled) {
        this.getStyle().setHighlightFeature(enabled);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Geometry getGeometry() {
        return this.geometry;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geom  DOCUMENT ME!
     */
    @Override
    public void setGeometry(Geometry geom) {
        if ((getLayerProperties() != null) && (getLayerProperties().getAttributeTableRuleSet() != null)) {
            geom = (Geometry)getLayerProperties().getAttributeTableRuleSet()
                        .afterEdit(this, "", -1, this.geometry, geom);
        }
        this.geometry = geom;
        setProperty(getGeometryFieldName(), geom);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGeometryFieldName() {
        if ((getLayerProperties() != null) && (getLayerProperties().getFeatureService() != null)
                    && (getLayerProperties().getFeatureService().getFeatureServiceAttributes() != null)) {
            final Map<String, FeatureServiceAttribute> attributes = getLayerProperties().getFeatureService()
                        .getFeatureServiceAttributes();

            for (final String key : attributes.keySet()) {
                final FeatureServiceAttribute attr = attributes.get(key);

                if (attr.isGeometry()) {
                    return attr.getName();
                }
            }
        }
        return "geom";
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean canBeSelected() {
        if ((layerProperties != null) && (layerProperties.getFeatureService() != null)) {
            if ((layerProperties.getFeatureService().getPNode() != null)
                        && !layerProperties.getFeatureService().getPNode().getVisible()) {
                // A feature should not be selectable, if its layer is not visible
                return false;
            }
        }
        if (canBeSelected == null) {
            if ((layerProperties != null) && (layerProperties.getFeatureService() != null)) {
                return layerProperties.getFeatureService().isSelectable();
            } else {
                return true;
            }
        } else {
            return canBeSelected.booleanValue();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isEditable() {
        return this.editable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  editable  DOCUMENT ME!
     */
    @Override
    public void setEditable(final boolean editable) {
        this.editable = editable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isHidden() {
        return this.hiding;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  hiding  DOCUMENT ME!
     */
    @Override
    public void hide(final boolean hiding) {
        this.hiding = hiding;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getPrimaryAnnotation() {
        if ((styles == null) || styles.isEmpty()) {
            return this.primaryAnnotation;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isPrimaryAnnotationVisible() {
        if ((styles == null) || styles.isEmpty()) {
            return this.getStyle().isDrawLabel();
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     */
    @Override
    public void setPrimaryAnnotationVisible(final boolean visible) {
        this.getStyle().setDrawLabel(visible);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Font getPrimaryAnnotationFont() {
        return this.getStyle().getFont();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Paint getPrimaryAnnotationPaint() {
        return this.getStyle().getFontColor();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public double getPrimaryAnnotationScaling() {
        return this.getStyle().getMultiplier();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public float getPrimaryAnnotationJustification() {
        return this.getStyle().getAlignment();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  just  DOCUMENT ME!
     */
    @Override
    public void setPrimaryAnnotationJustification(final float just) {
        this.getStyle().setAlignment(just);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getSecondaryAnnotation() {
        return this.secondaryAnnotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  primaryAnnotation  DOCUMENT ME!
     */
    @Override
    public void setPrimaryAnnotation(final String primaryAnnotation) {
        this.primaryAnnotation = primaryAnnotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  primaryAnnotationFont  DOCUMENT ME!
     */
    @Override
    public void setPrimaryAnnotationFont(final Font primaryAnnotationFont) {
        this.getStyle().setFont(primaryAnnotationFont);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  primaryAnnotationPaint  DOCUMENT ME!
     */
    @Override
    public void setPrimaryAnnotationPaint(final Paint primaryAnnotationPaint) {
        this.getStyle().setFontColor((Color)primaryAnnotationPaint);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  primaryAnnotationScaling  DOCUMENT ME!
     */
    @Override
    public void setPrimaryAnnotationScaling(final double primaryAnnotationScaling) {
        this.getStyle().setMultiplier(primaryAnnotationScaling);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  secondaryAnnotation  DOCUMENT ME!
     */
    @Override
    public void setSecondaryAnnotation(final String secondaryAnnotation) {
        this.secondaryAnnotation = secondaryAnnotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isAutoscale() {
        return this.getStyle().isAutoscale();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  autoScale  DOCUMENT ME!
     */
    @Override
    public void setAutoScale(final boolean autoScale) {
        this.getStyle().setAutoscale(autoScale);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Integer getMinScaleDenominator() {
        return this.getStyle().getMinScale();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Integer getMaxScaleDenominator() {
        return this.getStyle().getMaxScale();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  min  DOCUMENT ME!
     */
    @Override
    public void setMinScaleDenominator(final Integer min) {
        this.getStyle().setMinScale(min);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  max  DOCUMENT ME!
     */
    @Override
    public void setMaxScaleDenominator(final Integer max) {
        this.getStyle().setMaxScale(max);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  halo  DOCUMENT ME!
     */
    @Override
    public void setPrimaryAnnotationHalo(final Color halo) {
        this.getStyle().setHalo(halo);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Color getPrimaryAnnotationHalo() {
        return this.getStyle().getHalo();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Style getStyle() {
        return this.layerProperties.getStyle();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  canBeSelected  DOCUMENT ME!
     */
    @Override
    public void setCanBeSelected(final boolean canBeSelected) {
        this.canBeSelected = canBeSelected;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   obj  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj.getClass().getName().equals(getClass().getName())) {
            final DefaultFeatureServiceFeature other = (DefaultFeatureServiceFeature)obj;

            if (getId() != -1) {
                return getId() == other.getId();
            }
        } else {
            return false;
        }

        return super.equals(obj);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = (79 * hash) + this.id;
        hash = (79 * hash) + ((this.getClass().getName() != null) ? this.getClass().getName().hashCode() : 0);
        return hash;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  uom       DOCUMENT ME!
     * @param  fill      DOCUMENT ME!
     * @param  pfeature  DOCUMENT ME!
     * @param  map       DOCUMENT ME!
     */
    protected void applyFill(final org.deegree.style.styling.components.UOM uom,
            final Fill fill,
            final PPath pfeature,
            final MappingComponent map) {
        pfeature.setPaint(getPaintFromDeegree(fill.graphic, fill.color, uom, pfeature, map));
        // applyGraphic(fill.graphic, pfeature);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pfeature  DOCUMENT ME!
     * @param  styling   DOCUMENT ME!
     * @param  map       DOCUMENT ME!
     */
    protected void applyLineStyling(final PPath pfeature, final LineStyling styling, final MappingComponent map) {
        if (styling.stroke != null) {
            applyStroke(styling.uom, styling.stroke, pfeature, map);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  image     DOCUMENT ME!
     * @param  styling   DOCUMENT ME!
     * @param  wtst      DOCUMENT ME!
     * @param  x         DOCUMENT ME!
     * @param  y         DOCUMENT ME!
     * @param  camera    DOCUMENT ME!
     * @param  selected  DOCUMENT ME!
     */
    protected void applyPointStyling(final PImage image,
            final PointStyling styling,
            final WorldToScreenTransform wtst,
            final double x,
            final double y,
            final PCamera camera,
            final boolean selected) {
        final BufferedImage buffImage = getImageFromDeegree(styling.graphic, selected);

        image.setImage(buffImage);
        if (getUOMFromDeegree(styling.uom) == UOM.pixel) {
            ((FixedPImage)image).setMultiplier(1 / (buffImage.getHeight() / styling.graphic.size));
            ((FixedPImage)image).setSweetSpotX(-1 * styling.graphic.anchorPointX);
            ((FixedPImage)image).setSweetSpotY(-1 * styling.graphic.anchorPointY);
            image.setOffset(wtst.getScreenX(x),
                wtst.getScreenY(y));
//            image.setOffset(wtst.getScreenX(x) - (buffImage.getWidth() / 2),
//                wtst.getScreenY(y)
//                        - (buffImage.getHeight() / 2));
        } else {
            // ((PImageWithDisplacement)image).setUOM(getUOMFromDeegree(styling.uom));
            final double multiplier = getMultiplierFromDeegreeUOM(styling.uom);
            final double sizeMulti = styling.graphic.size / (double)(buffImage.getHeight());
            image.setScale(multiplier * sizeMulti);
            image.setOffset(wtst.getScreenX(
                    x
                            + ((styling.graphic.displacementX
                                    + ((-styling.graphic.anchorPointX) * buffImage.getWidth() * sizeMulti))
                                * multiplier)),
                wtst.getScreenY(
                    y
                            + ((styling.graphic.displacementY
                                    + ((styling.graphic.anchorPointY) * styling.graphic.size)) * multiplier)));
        }
        // image.setRotation(Math.toRadians(styling.graphic.rotation)); For Demo only
        image.setTransparency((float)styling.graphic.opacity);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pfeature  DOCUMENT ME!
     * @param  styling   DOCUMENT ME!
     * @param  map       DOCUMENT ME!
     */
    protected void applyPolygonStyling(final PPath pfeature, final PolygonStyling styling, final MappingComponent map) {
        if (styling.fill != null) {
            applyFill(styling.uom, styling.fill, pfeature, map);
        }
        if (styling.stroke != null) {
            applyStroke(styling.uom, styling.stroke, pfeature, map);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  uom       DOCUMENT ME!
     * @param  stroke    DOCUMENT ME!
     * @param  pfeature  DOCUMENT ME!
     * @param  map       DOCUMENT ME!
     */
    protected void applyStroke(final org.deegree.style.styling.components.UOM uom,
            final Stroke stroke,
            final PPath pfeature,
            final MappingComponent map) {
        final double multiplier = getMultiplierFromDeegreeUOM(uom);
        int linecap = BasicStroke.CAP_ROUND;

        if (stroke.linecap == Stroke.LineCap.BUTT) {
            linecap = BasicStroke.CAP_BUTT;
        } else if (stroke.linecap == Stroke.LineCap.ROUND) {
            linecap = BasicStroke.CAP_ROUND;
        } else if (stroke.linecap == Stroke.LineCap.SQUARE) {
            linecap = BasicStroke.CAP_SQUARE;
        }

        int lineJoin = BasicStroke.JOIN_ROUND;

        if (stroke.linejoin == Stroke.LineJoin.BEVEL) {
            lineJoin = BasicStroke.JOIN_BEVEL;
        } else if (stroke.linejoin == Stroke.LineJoin.MITRE) {
            lineJoin = BasicStroke.JOIN_MITER;
        } else if (stroke.linejoin == Stroke.LineJoin.ROUND) {
            lineJoin = BasicStroke.JOIN_ROUND;
        }

        float[] dash_array = null;

        if ((stroke.dasharray != null) && (stroke.dasharray.length != 0)) {
            dash_array = new float[stroke.dasharray.length];
            for (int i = 0; i < stroke.dasharray.length; i++) {
                dash_array[i] = (float)(stroke.dasharray[i] * multiplier);
            }
        }

        java.awt.Stroke newStroke;

        if (uom == org.deegree.style.styling.components.UOM.Pixel) {
            newStroke = new CustomFixedWidthStroke((float)(stroke.width),
                    linecap,
                    lineJoin,
                    1.0F,
                    dash_array,
                    (float)(stroke.dashoffset),
                    map);
        } else {
            newStroke = new BasicStroke((float)(stroke.width * multiplier),
                    linecap,
                    lineJoin,
                    1.0F,
                    dash_array,
                    (float)(stroke.dashoffset * multiplier));
        }

        pfeature.setStroke(newStroke);
        pfeature.setStrokePaint(getPaintFromDeegree(stroke.fill, stroke.color, uom, pfeature, map));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected org.deegree.feature.Feature getDeegreeFeature() {
        return new DeegreeFeature();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pfeature  DOCUMENT ME!
     * @param  wtst      DOCUMENT ME!
     */
    @Override
    public void applyStyle(final PFeature pfeature, final WorldToScreenTransform wtst) {
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

        final ListIterator it = pfeature.getChildrenIterator();
        while (it.hasNext()) {
            final Object child = it.next();
            if (child instanceof PSticky) {
                pfeature.getMappingComponent().removeStickyNode((PSticky)child);
            }
        }
        pfeature.removeAllChildren();
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

        /**
         * First clear the screen and then check, if there is a styling active.
         * Otherwise, the scale properties will not work.
         */
        if ((stylings == null) || (stylings.size() == 0)) {
            return;
        }

        final Geometry geom = pfeature.getFeature().getGeometry();
        int polygonNr = -1;
        int textNr = 0;
        int imageNr = 0;
        final List<Triple<Styling, LinkedList<org.deegree.geometry.Geometry>, String>> reverseList =
            new ArrayList<Triple<Styling, LinkedList<org.deegree.geometry.Geometry>, String>>(stylings);
        if ((geom != null) && !geom.getGeometryType().equalsIgnoreCase("Point")) {
            Collections.reverse(reverseList);
        }
        for (final Triple<Styling, LinkedList<org.deegree.geometry.Geometry>, String> styling : reverseList) {
            if ((styling.first instanceof PolygonStyling)
                        && ((geom instanceof Polygon) || (geom instanceof MultiPolygon))) {
                final PolygonStyling polygonStyle = (PolygonStyling)styling.first;
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
                    path.setPathTo((GeneralPath)pfeature.getPathReference().clone());
                }

                if ((polygonStyle.displacementX != 0.0) || (polygonStyle.displacementY != 0.0)) {
                    if (polygonStyle.uom == org.deegree.style.styling.components.UOM.Pixel) {
                        final MappingComponent mc = pfeature.getViewer();
                        final double metrePerPixel = mc.getCamera().getViewBounds().getHeight() / mc.getHeight();
                        final Geometry newGeom = (Geometry)geom.clone();
                        newGeom.apply(new CoordinateFilter() {

                                @Override
                                public void filter(final Coordinate coord) {
                                    coord.x = coord.x + (polygonStyle.displacementX * metrePerPixel);
                                    coord.y = coord.y + (polygonStyle.displacementY * metrePerPixel);
                                }
                            });
                        newGeom.geometryChanged();
                        setPath(path, newGeom, wtst);
                    } else {
                        final double multiplier = getMultiplierFromDeegreeUOM(polygonStyle.uom);
                        final Geometry newGeom = (Geometry)geom.clone();
                        newGeom.apply(new CoordinateFilter() {

                                @Override
                                public void filter(final Coordinate coord) {
                                    coord.x = coord.x + (polygonStyle.displacementX * multiplier);
                                    coord.y = coord.y + (polygonStyle.displacementY * multiplier);
                                }
                            });
                        newGeom.geometryChanged();
                        setPath(path, newGeom, wtst);
                    }
                }

                if (polygonStyle.perpendicularOffset != 0.0) {
                    if (polygonStyle.uom == org.deegree.style.styling.components.UOM.Pixel) {
                        final MappingComponent mc = pfeature.getViewer();
                        final double metrePerPixel = mc.getCamera().getViewBounds().getHeight() / mc.getHeight();
                        final Geometry newGeom = createOffsetCurve(
                                geom,
                                polygonStyle.perpendicularOffset
                                        * metrePerPixel);
                        setPath(path, newGeom, wtst);
                    } else {
                        final double multiplier = getMultiplierFromDeegreeUOM(polygonStyle.uom);
                        final Geometry newGeom = createOffsetCurve(geom, polygonStyle.perpendicularOffset
                                        * multiplier);
                        setPath(path, newGeom, wtst);
                    }
                }
                applyPolygonStyling(path, (PolygonStyling)styling.first, pfeature.getMappingComponent());
                polygonNr++;
            } else if ((styling.first instanceof LineStyling)
                        && ((geom instanceof LineString) || (geom instanceof MultiLineString))) {
                final LineStyling lineStyle = (LineStyling)styling.first;
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
                    path.setPathTo((GeneralPath)pfeature.getPathReference().clone());
                }
                Geometry offsetGeom = geom;

                if (lineStyle.perpendicularOffset != 0.0) {
                    if (lineStyle.uom == org.deegree.style.styling.components.UOM.Pixel) {
                        final MappingComponent mc = pfeature.getViewer();
                        final double metrePerPixel = mc.getCamera().getViewBounds().getHeight() / mc.getHeight();
                        offsetGeom = createOffsetCurve(geom, lineStyle.perpendicularOffset
                                        * metrePerPixel);
                        setPath(path, offsetGeom, wtst);
                    } else {
                        final double multiplier = getMultiplierFromDeegreeUOM(lineStyle.uom);
                        offsetGeom = createOffsetCurve(geom, lineStyle.perpendicularOffset
                                        * multiplier);
                        setPath(path, offsetGeom, wtst);
                    }
                }

                final List<EndPointStyle> epStyles = getEndPointStyles();

                applyLineStyling(path, (LineStyling)styling.first, pfeature.getMappingComponent());
                polygonNr++;

                if (!epStyles.isEmpty()) {
                    for (final EndPointStyle tmpStyle : epStyles) {
                        final MappingComponent mc = pfeature.getViewer();
                        final double metrePerPixel = mc.getCamera().getViewBounds().getHeight() / mc.getHeight();

                        path = new PPath();
                        final GeneralPath newPath = tmpStyle.arrowhead(
                                transformCoordinateArr(offsetGeom.getCoordinates(), wtst),
                                metrePerPixel);
                        pfeature.sldStyledPolygon.add(path);
                        pfeature.addChild(path);
                        path.setPathTo(newPath);
                        applyLineStyling(path, (LineStyling)styling.first, pfeature.getMappingComponent());
                        polygonNr++;
                    }
                }
            } else if ((styling.first instanceof TextStyling) && (styling.third != null)
                        && !styling.third.equals("null")) {
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
                String value = styling.third;
                try {
                    final double val = Double.parseDouble(styling.third);

                    if (val == (long)val) {
                        value = String.valueOf((long)val);
                    }
                } catch (NumberFormatException e) {
                }
                applyTextStyling(
                    text,
                    value,
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
    private List<EndPointStyle> getEndPointStyles() {
        final List<EndPointStyle> epStyles = new ArrayList<EndPointStyle>();

        for (final org.deegree.style.se.unevaluated.Style tempStyle : styles) {
            if (tempStyle instanceof CustomStyle) {
                final CustomStyle cs = (CustomStyle)tempStyle;
                if (!cs.getEndPointStyles().isEmpty()) {
                    for (final EndPointStyleDescription epStyle : cs.getEndPointStyles()) {
                        try {
                            final Class c = Class.forName(epStyle.getClassName());
                            final Constructor constructor = c.getConstructor();
                            final Object o = constructor.newInstance();

                            if (o instanceof EndPointStyle) {
                                epStyles.add((EndPointStyle)o);
                            }
                        } catch (Exception ex) {
                            logger.error("Cannot find end point style class", ex);
                        }
                    }
                }
            }
        }

        return epStyles;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geom    DOCUMENT ME!
     * @param   offset  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Geometry createOffsetCurve(final Geometry geom, final double offset) {
        final BufferParameters bufParams = new BufferParameters();
        bufParams.setSingleSided(true);
        final OffsetCurveBuilder curveBuilder = new OffsetCurveBuilder(geom.getPrecisionModel(),
                bufParams);
        final GeometryFactory factory = geom.getFactory();

        if (geom.getNumGeometries() > 1) {
            final List<Geometry> geomList = new ArrayList<Geometry>();

            for (int i = 0; i < geom.getNumGeometries(); ++i) {
                final Coordinate[] newCoords = curveBuilder.getOffsetCurve(geom.getGeometryN(i).getCoordinates(),
                        offset);
                final Geometry newGeom = factory.createLineString(newCoords);

                geomList.add(newGeom);
            }

            return factory.buildGeometry(geomList);
        } else {
            final Coordinate[] newCoords = curveBuilder.getOffsetCurve(geom.getCoordinates(), offset);
            return factory.createLineString(newCoords);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  path  DOCUMENT ME!
     * @param  geom  DOCUMENT ME!
     * @param  wtst  DOCUMENT ME!
     */
    private void setPath(final PPath path, final Geometry geom, final WorldToScreenTransform wtst) {
        final Coordinate[][][] entityRingCoordArr = getCoordinateArray(geom);

        final float[][][] entityRingXArr = new float[entityRingCoordArr.length][][];
        final float[][][] entityRingYArr = new float[entityRingCoordArr.length][][];

        for (int entityIndex = 0; entityIndex < entityRingCoordArr.length; entityIndex++) {
            entityRingXArr[entityIndex] = new float[entityRingCoordArr[entityIndex].length][];
            entityRingYArr[entityIndex] = new float[entityRingCoordArr[entityIndex].length][];

            for (int ringIndex = 0; ringIndex < entityRingCoordArr[entityIndex].length; ringIndex++) {
                final Coordinate[] transformedCoordArr = transformCoordinateArr(
                        entityRingCoordArr[entityIndex][ringIndex],
                        wtst);
                final int length = transformedCoordArr.length;
                entityRingXArr[entityIndex][ringIndex] = new float[length];
                entityRingYArr[entityIndex][ringIndex] = new float[length];

                for (int coordIndex = 0; coordIndex < length; coordIndex++) {
                    entityRingXArr[entityIndex][ringIndex][coordIndex] = (float)transformedCoordArr[coordIndex].x;
                    entityRingYArr[entityIndex][ringIndex][coordIndex] = (float)transformedCoordArr[coordIndex].y;
                }
            }
        }

        path.getPathReference().reset();

        if (geom instanceof Point) {
            path.setPathToPolyline(
                new float[] { entityRingXArr[0][0][0], entityRingXArr[0][0][0] },
                new float[] { entityRingYArr[0][0][0], entityRingYArr[0][0][0] });
        } else if ((geom instanceof LineString) || (geom instanceof MultiPoint)) {
            path.setPathToPolyline(entityRingXArr[0][0], entityRingYArr[0][0]);
        } else if ((geom instanceof Polygon) || (geom instanceof MultiPolygon)) {
            path.getPathReference().setWindingRule(GeneralPath.WIND_EVEN_ODD);
            for (int entityIndex = 0; entityIndex < entityRingCoordArr.length; entityIndex++) {
                for (int ringIndex = 0; ringIndex < entityRingCoordArr[entityIndex].length; ringIndex++) {
                    final Coordinate[] coordArr = entityRingCoordArr[entityIndex][ringIndex];
                    addLinearRing(path, coordArr, wtst);
                }
            }
            path.updateBoundsFromPath();
            path.invalidatePaint();
        } else if (geom instanceof MultiLineString) {
            for (int entityIndex = 0; entityIndex < entityRingCoordArr.length; entityIndex++) {
                for (int ringIndex = 0; ringIndex < entityRingCoordArr[entityIndex].length; ringIndex++) {
                    final Coordinate[] coordArr = entityRingCoordArr[entityIndex][ringIndex];
                    addLinearRing(path, coordArr, wtst);
                }
            }
            path.updateBoundsFromPath();
            path.invalidatePaint();
        }
    }

    /**
     * F\u00FCgt dem PFeature ein weiteres Coordinate-Array hinzu. Dadurch entstehen Multipolygone und Polygone mit
     * L\u00F6chern, je nachdem, ob der neue LinearRing ausserhalb oder innerhalb des PFeatures liegt.
     *
     * @param  path           DOCUMENT ME!
     * @param  coordinateArr  die Koordinaten des hinzuzuf\u00FCgenden Rings als Coordinate-Array
     * @param  wtst           DOCUMENT ME!
     */
    private void addLinearRing(final PPath path, final Coordinate[] coordinateArr, final WorldToScreenTransform wtst) {
        final Coordinate[] points = transformCoordinateArr(coordinateArr, wtst);
        final GeneralPath gp = new GeneralPath();
        gp.reset();

        if (points.length > 0) {
            gp.moveTo((float)points[0].x, (float)points[0].y);
            for (int i = 1; i < points.length; i++) {
                gp.lineTo((float)points[i].x, (float)points[i].y);
            }
        }

        path.getPathReference().append(gp, false);
    }

    /**
     * Erzeugt PCanvas-Koordinaten-Punktarrays aus Realworldkoordinaten.
     *
     * @param   coordinateArr  Array mit Realworld-Koordinaten
     * @param   wtst           DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Coordinate[] transformCoordinateArr(final Coordinate[] coordinateArr, final WorldToScreenTransform wtst) {
        final Coordinate[] points = new Coordinate[coordinateArr.length];

        for (int i = 0; i < coordinateArr.length; ++i) {
            points[i] = new Coordinate();
            if (wtst == null) {
                points[i].x = (float)(coordinateArr[i].x);
                points[i].y = (float)(coordinateArr[i].y);
            } else {
                points[i].x = (float)(wtst.getDestX(coordinateArr[i].x));
                points[i].y = (float)(wtst.getDestY(coordinateArr[i].y));
            }
        }

        return points;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Coordinate[][][] getCoordinateArray(final Geometry geom) {
        Coordinate[][][] otherCoords = null;

        if (geom instanceof Point) {
            final Point point = (Point)geom;
            otherCoords = new Coordinate[][][] {
                    {
                        { point.getCoordinate() }
                    }
                };
        } else if (geom instanceof LineString) {
            final LineString lineString = (LineString)geom;
            otherCoords = new Coordinate[][][] {
                    { lineString.getCoordinates() }
                };
        } else if (geom instanceof Polygon) {
            final Polygon polygon = (Polygon)geom;
            final int numOfHoles = polygon.getNumInteriorRing();
            otherCoords = new Coordinate[1][1 + numOfHoles][];
            otherCoords[0][0] = polygon.getExteriorRing().getCoordinates();
            for (int ringIndex = 1; ringIndex < otherCoords[0].length; ++ringIndex) {
                otherCoords[0][ringIndex] = polygon.getInteriorRingN(ringIndex - 1).getCoordinates();
            }
        } else if (geom instanceof LinearRing) {
            // doPolygon((Polygon)geom);
        } else if (geom instanceof MultiPoint) {
            otherCoords = new Coordinate[][][] {
                    { ((MultiPoint)geom).getCoordinates() }
                };
        } else if (geom instanceof MultiLineString) {
            final MultiLineString multiLineString = (MultiLineString)geom;
            final int numOfGeoms = multiLineString.getNumGeometries();
            otherCoords = new Coordinate[numOfGeoms][][];
            for (int entityIndex = 0; entityIndex < numOfGeoms; ++entityIndex) {
                final Coordinate[] coordSubArr = ((LineString)multiLineString.getGeometryN(entityIndex))
                            .getCoordinates();
                otherCoords[entityIndex] = new Coordinate[][] { coordSubArr };
            }
        } else if (geom instanceof MultiPolygon) {
            final MultiPolygon multiPolygon = (MultiPolygon)geom;
            final int numOfEntities = multiPolygon.getNumGeometries();
            otherCoords = new Coordinate[numOfEntities][][];
            for (int entityIndex = 0; entityIndex < numOfEntities; ++entityIndex) {
                final Polygon polygon = (Polygon)multiPolygon.getGeometryN(entityIndex);
                final int numOfHoles = polygon.getNumInteriorRing();
                otherCoords[entityIndex] = new Coordinate[1 + numOfHoles][];
                otherCoords[entityIndex][0] = polygon.getExteriorRing().getCoordinates();
                for (int ringIndex = 1; ringIndex < otherCoords[entityIndex].length; ++ringIndex) {
                    otherCoords[entityIndex][ringIndex] = polygon.getInteriorRingN(ringIndex - 1).getCoordinates();
                }
            }
        } else if (geom instanceof GeometryCollection) {
            final GeometryCollection gc = (GeometryCollection)geom;
            final int numOfGeoms = gc.getNumGeometries();
            otherCoords = new Coordinate[numOfGeoms][][];
            for (int entityIndex = 0; entityIndex < numOfGeoms; ++entityIndex) {
                final Coordinate[][][] coordSubArr = getCoordinateArray(gc.getGeometryN(entityIndex));
                otherCoords[entityIndex] = coordSubArr[0];
            }
        }

        return otherCoords;
    }

    /**
     * does not have the limitations from the rescaleStickyNode(PSticky) method of the MappingsComponent.
     *
     * @param  pfeature  DOCUMENT ME!
     * @param  sticky    DOCUMENT ME!
     */
    protected void rescaleStickyNode(final PFeature pfeature, final PSticky sticky) {
        final double s = pfeature.getMappingComponent().getCamera().getViewScale();
        sticky.setScale(1 / s);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ptext        DOCUMENT ME!
     * @param  value        DOCUMENT ME!
     * @param  textStyling  DOCUMENT ME!
     * @param  wtst         DOCUMENT ME!
     * @param  x            DOCUMENT ME!
     * @param  y            DOCUMENT ME!
     */
    protected void applyTextStyling(final PFeature.PTextWithDisplacement ptext,
            final String value,
            final TextStyling textStyling,
            final WorldToScreenTransform wtst,
            final double x,
            final double y) {
        ptext.setText(value);
        ptext.setOffset(wtst.getScreenX(x), wtst.getScreenY(y));
        /*ptext.setDisplacement(getUOMFromDeegree(textStyling.uom),
         *  textStyling.displacementX, textStyling.displacementY, textStyling.anchorPointX, textStyling.anchorPointY,
         * wtst);*/
        /*double multiplier = getMultiplierFromDeegreeUOM(textStyling.uom);
         * ptext.setOffset(wtst.getScreenX(x + ((textStyling.displacementX)*multiplier)), wtst.getScreenY(y +
         * ((textStyling.displacementY)*multiplier)));*/
        /*ptext.setOffset(wtst.getScreenX(x + ((styling.graphic.displacementX + (1.0d - styling.graphic.anchorPointX) *
         * styling.graphic.image.getWidth() * sizeMulti)* multiplier)),         wtst.getScreenY(y +
         * ((styling.graphic.displacementY + (1.0d - styling.graphic.anchorPointY) *
         * styling.graphic.size)*multiplier)));
         */
        ptext.setTextPaint(textStyling.fill.color);
        Font font = null;
        try {
            for (final String fontName : textStyling.font.fontFamily) {
                font = new Font(fontName, getFontStyling(textStyling.font), (int)textStyling.font.fontSize);
            }
        } catch (Exception ex) {
        }
        ptext.setFont(font);
        ptext.setRotation(Math.toRadians(textStyling.rotation));

        if (textStyling.halo != null) {
            final Halo halo = textStyling.halo;

            if ((halo.fill != null) && (halo.fill.color != null)) {
                ptext.setPaint(halo.fill.color);
            }

            if (halo.radius != 0.0) {
                final PBounds bound = ptext.getBounds();
                ptext.setBounds(bound.x, bound.y, bound.width + halo.radius, bound.height + halo.radius);
            }
        }

        if (textStyling.uom == org.deegree.style.styling.components.UOM.Pixel) {
//            final MappingComponent mc = pfeature.getViewer();
//            final double metrePerPixel = mc.getCamera().getViewBounds().getHeight() / mc.getHeight();
            ptext.setDisplacement(getUOMFromDeegree(textStyling.uom),
                textStyling.displacementX,
                textStyling.displacementY,
                textStyling.anchorPointX,
                textStyling.anchorPointY,
                wtst);
        } else {
            final double multiplier = getMultiplierFromDeegreeUOM(textStyling.uom);
            ptext.setDisplacement(getUOMFromDeegree(textStyling.uom),
                textStyling.displacementX
                        * multiplier,
                textStyling.displacementY
                        * multiplier,
                textStyling.anchorPointX,
                textStyling.anchorPointY,
                wtst);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   font  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected int getFontStyling(final org.deegree.style.styling.components.Font font) {
        final int bolt = font.bold ? 1 : 0;
        switch (font.fontStyle) {
            case OBLIQUE:
            case ITALIC: {
                return bolt + 2;
            }
            case NORMAL: {
                return bolt;
            }
        }
        return bolt;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   graphic   DOCUMENT ME!
     * @param   selected  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected BufferedImage getImageFromDeegree(final Graphic graphic, final boolean selected) {
        if (graphic.image != null) {
            if (selected) {
                return getSelectedImageFromImage(graphic.image);
            } else {
                return graphic.image;
            }
        } else {
            BufferedImage temp = getImageFromWellKnownName(graphic.mark.wellKnown);
            if ((graphic.mark.fill != null) && (graphic.mark.fill.color != null)) {
                final BufferedImage coloredVerion = new BufferedImage(temp.getWidth(),
                        temp.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                final Graphics2D g = (Graphics2D)coloredVerion.getGraphics();
                if (selected) {
                    g.setColor(PFeature.getHighlightingColorFromColor(graphic.mark.fill.color));
                } else {
                    g.setColor(graphic.mark.fill.color);
                }
                g.fillRect(0, 0, temp.getWidth(), temp.getHeight());
                g.setComposite(AlphaComposite.DstIn);
                g.drawImage(
                    temp,
                    0,
                    0,
                    temp.getWidth(),
                    temp.getHeight(),
                    0,
                    0,
                    temp.getWidth(),
                    temp.getHeight(),
                    null);
                temp = coloredVerion;
            }
            return temp;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mark  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RuntimeException  DOCUMENT ME!
     */
    protected BufferedImage getImageFromWellKnownName(final Mark.SimpleMark mark) {
        URL url = null;
        switch (mark) {
            case CIRCLE: {
                url = getClass().getResource("/icon-circlerecord.png");
                break;
            }
            case CROSS: {
                url = getClass().getResource("/icon-plus.png");
                break;
            }
            case SQUARE: {
                url = getClass().getResource("/icon-squareapp.png");
                break;
            }
            case STAR: {
                url = getClass().getResource("/icon-star.png");
                break;
            }
            case TRIANGLE: {
                url = getClass().getResource("/icon-play.png");
                break;
            }
            case X: {
                url = getClass().getResource("/icon-remove.png");
                break;
            }
        }
        if (url == null) {
            throw new RuntimeException("could not load Resource" + mark);
        }
        final ImageIcon icon = new ImageIcon(url);
        final Image image = icon.getImage();
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        } else if (image instanceof ToolkitImage) {
            logger.warn(
                "sun.awt.image.ToolkitImage is internal proprietary API and may be removed in a future release ("
                        + mark
                        + ")");
            return ((ToolkitImage)image).getBufferedImage();
        } else {
            throw new RuntimeException("No BufferedImage" + mark);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   uom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RuntimeException  DOCUMENT ME!
     */
    protected double getMultiplierFromDeegreeUOM(final org.deegree.style.styling.components.UOM uom) {
        switch (uom) {
            case Foot: {
                return 0.3048;
            }
            case Metre: {
                return 1.0;
            }
            case Pixel: {
                return 1.0;
            }
            case mm: {
                return 0.001;
            }
        }
        throw new RuntimeException("unknown UOM" + uom.toString());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   graphic  DOCUMENT ME!
     * @param   color    DOCUMENT ME!
     * @param   uom      DOCUMENT ME!
     * @param   parent   DOCUMENT ME!
     * @param   map      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Paint getPaintFromDeegree(final Graphic graphic,
            final Color color,
            final org.deegree.style.styling.components.UOM uom,
            final PNode parent,
            final MappingComponent map) {
        if (graphic == null) {
            return color;
        } else {
            final double multiplier = getMultiplierFromDeegreeUOM(uom);
            final BufferedImage image = getImageFromDeegree(graphic, false);
            Paint texture;
            if (uom != org.deegree.style.styling.components.UOM.Pixel) {
                texture = new TexturePaint(
                        image,
                        new Rectangle2D.Double(
                            0,
                            0,
                            multiplier
                                    * graphic.size
                                    * image.getWidth()
                                    / image.getHeight(),
                            graphic.size
                                    * multiplier));
            } else {
                final RescaleOp rescaleOp = new RescaleOp(0.25f, 0f, null);

                texture = new SelectionAwareTexturePaint(
                        image,
                        rescaleOp.filter(image, null),
                        rescaleOp.filter(image, null),
                        new Rectangle2D.Double(
                            0,
                            0,
                            multiplier
                                    * graphic.size
                                    * image.getWidth(),
                            multiplier
                                    * graphic.size
                                    * image.getHeight()));
//                texture = new PFixedTexturePaint(
//                        image,
//                        new Rectangle2D.Double(
//                            0,
//                            0,
//                            multiplier
//                                    * graphic.size
//                                    * image.getWidth()
//                                    / image.getHeight(),
//                            graphic.size
//                                    * multiplier),
//                        parent);
//                map.addStickyNode((PFixedTexturePaint)texture);
            }
            return texture;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   uom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RuntimeException  DOCUMENT ME!
     */
    protected UOM getUOMFromDeegree(final org.deegree.style.styling.components.UOM uom) {
        switch (uom) {
            case Foot: {
                return UOM.foot;
            }
            case Metre: {
                return UOM.metre;
            }
            case Pixel: {
                return UOM.pixel;
            }
            case mm: {
                return UOM.mm;
            }
        }
        throw new RuntimeException("unknown UOM" + uom.toString());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureStyles  DOCUMENT ME!
     */
    @Override
    public void setSLDStyles(final List<org.deegree.style.se.unevaluated.Style> featureStyles) {
        if ((styles == null) || !styles.equals(featureStyles)) {
            this.styles = featureStyles;
            stylings = null;
            resetCustomStylesAdjustments();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void resetCustomStylesAdjustments() {
        customFillingStyle = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   unselectedImage  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private BufferedImage getSelectedImageFromImage(final BufferedImage unselectedImage) {
        BufferedImage bImage = selectedImage.get(unselectedImage);

        if (bImage == null) {
            Image image = ensureRGBAImage(unselectedImage);
            image = Toolkit.getDefaultToolkit()
                        .createImage(new FilteredImageSource(image.getSource(),
                                    new SelectedFilter()));
            final int width = image.getWidth(null);
            final int height = image.getHeight(null);
            bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            bImage.getGraphics().drawImage(image, 0, 0, null);

            selectedImage.put(unselectedImage, bImage);
        }

        return bImage;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   image  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private BufferedImage ensureRGBAImage(BufferedImage image) {
        if ((image != null) && (image.getType() != BufferedImage.TYPE_INT_ARGB)) {
            final BufferedImage tmpImg = new BufferedImage(image.getWidth(),
                    image.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            final Graphics g = tmpImg.getGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = tmpImg;
        }

        return image;
    }

    /**
     * Ändert das dem Namen zugeordnete Property.
     *
     * @param  propertyName   Name des gesuchten Objekts
     * @param  propertyValue  neuer Wert des Properties
     */
    @Override
    public void setProperty(final String propertyName, final Object propertyValue) {
        container.put(propertyName, propertyValue);
    }

    /**
     * Saves the feature.
     *
     * @return  the reloaded feature
     *
     * @throws  Exception                      DOCUMENT ME!
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public FeatureServiceFeature saveChanges() throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public void undoAll() {
        throw new UnsupportedOperationException();
    }

    /**
     * Add a new PropertyChangeListener.
     *
     * @param  l  DOCUMENT ME!
     */
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Remove the given PropertyChangeListener.
     *
     * @param  l  DOCUMENT ME!
     */
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * fires a propertyChange event.
     *
     * @param  propertyName  the name of the changed property
     * @param  oldValue      the old value
     * @param  newValue      the new value
     */
    protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public String toString() {
        final AbstractFeatureService service = layerProperties.getFeatureService();
        final List<String> nameParts = new ArrayList<String>();

        if (service != null) {
            final Map<String, FeatureServiceAttribute> attributes = service.getFeatureServiceAttributes();
            final List<String> attributeNames = service.getOrderedFeatureServiceAttributes();

            for (final String key : attributeNames) {
                final FeatureServiceAttribute attr = attributes.get(key);

                if (attr.isNameElement()) {
                    nameParts.add(String.valueOf(getProperty(key)));
                }
            }
        }

        if (!nameParts.isEmpty()) {
            StringBuilder sb = null;

            for (final String part : nameParts) {
                if (sb == null) {
                    sb = new StringBuilder();
                    sb.append(part);
                } else {
                    sb.append(" - ").append(part);
                }
            }

            return sb.toString();
        } else {
            final String[] prefferedKeys = { "ID", "id", "Id", "app:ID", "app:id", "app:Id" };
            final HashMap propertyMap = getProperties();

            for (final String key : prefferedKeys) {
                if (propertyMap.containsKey(key)) {
                    final Object id = propertyMap.get(key);

                    if (id != null) {
                        return id.toString();
                    }
                }
            }

            // no ID key found. Return a random attribute
            final Iterator it = propertyMap.keySet().iterator();

            if (it.hasNext()) {
                return String.valueOf(propertyMap.get(it.next()));
            } else {
                return super.toString();
            }
        }
    }

    @Override
    public int compareTo(final DefaultFeatureServiceFeature o) {
        return Integer.compare(id, o.id);
    }

    @Override
    public String getName() {
        return null;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected class DeegreeEvaluator implements org.deegree.filter.XPathEvaluator<org.deegree.feature.Feature> {

        //~ Methods ------------------------------------------------------------

        @Override
        public TypedObjectNode[] eval(final Feature t, final ValueReference vr) throws FilterEvaluationException {
            final List<org.deegree.commons.tom.gml.property.Property> properties = t.getProperties(vr.getAsQName());
            final TypedObjectNode[] ret = properties.toArray(new TypedObjectNode[properties.size()]);
            return ret;
        }

        @Override
        public String getId(final Feature t) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected class DeegreeFeature implements Feature {

        //~ Methods ------------------------------------------------------------

        @Override
        public List<org.deegree.commons.tom.gml.property.Property> getGeometryProperties() {
            return new ArrayList<org.deegree.commons.tom.gml.property.Property>();
        }

        @Override
        public org.deegree.feature.types.FeatureType getType() {
            return new DeegreeFeatureType();
        }

        @Override
        public List<org.deegree.commons.tom.gml.property.Property> getProperties(final QName qname) {
            final List<Property> deegreeProperties = new LinkedList();
            if (qname == null) {
                return deegreeProperties;
            }
            final Object value;
            String key;
            if ((qname.getPrefix() != null) && !qname.getPrefix().isEmpty()) {
                key = qname.getPrefix() + ":" + qname.getLocalPart();
            } else {
                key = qname.getLocalPart();
            }
            if (DefaultFeatureServiceFeature.this.getProperties().containsKey(key)) {
                value = DefaultFeatureServiceFeature.this.getProperty(key);
                if (value == null) {
                    deegreeProperties.add(null);
                } else {
                    if (value instanceof BigDecimal) {
                        deegreeProperties.add(new DeegreeProperty(qname, ((BigDecimal)value).doubleValue()));
                    } else {
                        deegreeProperties.add(new DeegreeProperty(qname, value));
                    }
                }
            } else if (DefaultFeatureServiceFeature.this.getProperties().containsKey("app:" + qname.getLocalPart())) {
                value = DefaultFeatureServiceFeature.this.getProperty("app:" + qname.getLocalPart());
                if (value == null) {
                    deegreeProperties.add(null);
                } else {
                    deegreeProperties.add(new DeegreeProperty(qname, value));
                }
            }
            return deegreeProperties;
        }

        @Override
        public void setId(final String string) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public QName getName() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public Envelope getEnvelope() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public void setEnvelope(final Envelope envlp) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public Envelope calcEnvelope() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public void setPropertyValue(final QName qname, final int i, final TypedObjectNode ton) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public void setProperties(final List<Property> list) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public ExtraProps getExtraProperties() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public void setExtraProperties(final ExtraProps ep) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public String getId() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public List<Property> getProperties() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected class DeegreeFeatureType implements FeatureType {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DeegreeFeatureType object.
         */
        public DeegreeFeatureType() {
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public GeometryPropertyType getDefaultGeometryPropertyDeclaration() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public Feature newFeature(final String string, final List<Property> list, final ExtraProps ep) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public AppSchema getSchema() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public GMLObjectCategory getCategory() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public QName getName() {
            return new QName("");
        }

        @Override
        public boolean isAbstract() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public PropertyType getPropertyDeclaration(final QName qname) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public List<PropertyType> getPropertyDeclarations() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected class DeegreeProperty implements Property {

        //~ Instance fields ----------------------------------------------------

        private QName name;
        private Object value;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DeegreeProperty object.
         *
         * @param  name   DOCUMENT ME!
         * @param  value  DOCUMENT ME!
         */
        public DeegreeProperty(final QName name, final Object value) {
            this.name = name;
            this.value = value;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public QName getName() {
            return name;
        }

        @Override
        public TypedObjectNode getValue() {
            if (value == null) {
                return new org.deegree.commons.tom.primitive.PrimitiveValue("null");
            } else if (value instanceof String) {
                return new org.deegree.commons.tom.primitive.PrimitiveValue((String)value,
                        new org.deegree.commons.tom.primitive.PrimitiveType(
                            org.deegree.commons.tom.primitive.BaseType.STRING));
            } else if (value instanceof Float) {
                return new org.deegree.commons.tom.primitive.PrimitiveValue(new Double((Float)value),
                        new org.deegree.commons.tom.primitive.PrimitiveType(
                            org.deegree.commons.tom.primitive.BaseType.DOUBLE));
            } else if (value instanceof Boolean) {
                return new org.deegree.commons.tom.primitive.PrimitiveValue((Boolean)value,
                        new org.deegree.commons.tom.primitive.PrimitiveType(
                            org.deegree.commons.tom.primitive.BaseType.BOOLEAN));
            } else if (value instanceof Double) {
                return new org.deegree.commons.tom.primitive.PrimitiveValue((Double)value,
                        new org.deegree.commons.tom.primitive.PrimitiveType(
                            org.deegree.commons.tom.primitive.BaseType.DOUBLE));
            } else if (value instanceof Integer) {
                return new org.deegree.commons.tom.primitive.PrimitiveValue((Integer)value,
                        new org.deegree.commons.tom.primitive.PrimitiveType(
                            org.deegree.commons.tom.primitive.BaseType.INTEGER));
            } else if (value instanceof Long) {
                return new org.deegree.commons.tom.primitive.PrimitiveValue((Long)value,
                        new org.deegree.commons.tom.primitive.PrimitiveType(
                            org.deegree.commons.tom.primitive.BaseType.INTEGER));
            } else if (value instanceof org.deegree.geometry.Geometry) {
                return ((org.deegree.geometry.Geometry)value);
            } else if (value instanceof org.deegree.model.spatialschema.Geometry) {
                try {
                    final org.deegree.model.spatialschema.Geometry geo = ((org.deegree.model.spatialschema.Geometry)
                            value);
                    final Geometry g = JTSAdapter.export(geo);
                    if (geo.getCoordinateSystem() != null) {
                        final int srid = CrsTransformer.extractSridFromCrs(geo.getCoordinateSystem().getIdentifier());
                        g.setSRID(srid);
                    }
                    return defaultGeom.createFromJTS(g, null);
                } catch (final Exception e) {
                    logger.error("Cannot create deegree3 from deegree3 geometry.", e);
                    return new org.deegree.commons.tom.primitive.PrimitiveValue("null");
                }
            } else if (value instanceof Geometry) {
                return defaultGeom.createFromJTS((Geometry)value, null);
//                return new org.deegree.geometry.Geometry() {
//
//                        @Override
//                        public org.deegree.geometry.Geometry.GeometryType getGeometryType() {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public void setId(final String string) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public void setType(final org.deegree.commons.tom.gml.GMLObjectType gmlot) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public org.deegree.geometry.precision.PrecisionModel getPrecision() {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public void setPrecision(final org.deegree.geometry.precision.PrecisionModel pm) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public org.deegree.cs.coordinatesystems.ICRS getCoordinateSystem() {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public void setCoordinateSystem(final org.deegree.cs.coordinatesystems.ICRS icrs) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public void setProperties(final List<Property> list) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public boolean isSFSCompliant() {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public int getCoordinateDimension() {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public boolean contains(final org.deegree.geometry.Geometry gmtr) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public boolean crosses(final org.deegree.geometry.Geometry gmtr) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public boolean equals(final org.deegree.geometry.Geometry gmtr) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public boolean intersects(final org.deegree.geometry.Geometry gmtr) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public boolean isBeyond(final org.deegree.geometry.Geometry gmtr,
//                                final org.deegree.commons.uom.Measure msr) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public boolean isDisjoint(final org.deegree.geometry.Geometry gmtr) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public boolean isWithin(final org.deegree.geometry.Geometry gmtr) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public boolean isWithinDistance(final org.deegree.geometry.Geometry gmtr,
//                                final org.deegree.commons.uom.Measure msr) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public boolean overlaps(final org.deegree.geometry.Geometry gmtr) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public boolean touches(final org.deegree.geometry.Geometry gmtr) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public org.deegree.geometry.Geometry getBuffer(final org.deegree.commons.uom.Measure msr) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public org.deegree.geometry.primitive.Point getCentroid() {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public Envelope getEnvelope() {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public org.deegree.geometry.Geometry getDifference(
//                                final org.deegree.geometry.Geometry gmtr) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public org.deegree.geometry.Geometry getIntersection(
//                                final org.deegree.geometry.Geometry gmtr) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public org.deegree.geometry.Geometry getUnion(final org.deegree.geometry.Geometry gmtr) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public org.deegree.geometry.Geometry getConvexHull() {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public org.deegree.commons.uom.Measure getDistance(final org.deegree.geometry.Geometry gmtr,
//                                final org.deegree.commons.uom.Unit unit) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public String getId() {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public org.deegree.commons.tom.gml.GMLObjectType getType() {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public List<Property> getProperties() {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public List<Property> getProperties(final QName qname) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//                    };
            } else {
                return new org.deegree.commons.tom.primitive.PrimitiveValue("null");
            }
        }

        @Override
        public String toString() {
            return value.toString();
        }

        @Override
        public PropertyType getType() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public void setValue(final TypedObjectNode ton) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public void setChildren(final List<TypedObjectNode> list) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public Map<QName, PrimitiveValue> getAttributes() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public List<TypedObjectNode> getChildren() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }

        @Override
        public XSElementDeclaration getXSType() {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
            // choose Tools | Templates.
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class SelectedFilter extends RGBImageFilter {

        //~ Methods ------------------------------------------------------------

        @Override
        public int filterRGB(final int x, final int y, final int argb) {
            final int r = (argb & 0x00ff0000) >> 0x10;
            final int g = (argb & 0x0000ff00) >> 0x08;
            final int b = (argb & 0x000000ff);

            final Color c = new Color(r, g, b);

            final Color newColor = PFeature.getHighlightingColorFromColor(c);

            return (argb & 0xff000000) | (newColor.getRed() << 0x10) | (newColor.getGreen() << 0x08)
                        | (newColor.getBlue());
        }
    }
}
