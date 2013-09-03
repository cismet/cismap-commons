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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;

import org.apache.log4j.Logger;
import org.apache.xerces.xs.XSElementDeclaration;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.GMLObjectCategory;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Triple;
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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.LinkedList;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;

import javax.swing.ImageIcon;

import javax.xml.namespace.QName;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.style.Style;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.CustomFixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.gui.piccolo.FixedPImage;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.PFixedTexturePaint;
import de.cismet.cismap.commons.gui.piccolo.PSticky;
import edu.umd.cs.piccolo.PNode;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.util.ListIterator;

/**
 * Default implementation of a FeatureServiceFeature.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class DefaultFeatureServiceFeature implements FeatureServiceFeature {

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
        this.setId(id);
        this.setPrimaryAnnotation(new String(feature.getPrimaryAnnotation()));
        this.setSecondaryAnnotation(new String(feature.getSecondaryAnnotation()));
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

        if (feature.getGeometry() != null) {
            this.setGeometry((Geometry)feature.getGeometry().clone());
        }

        if (feature.getLayerProperties() != null) {
            this.setLayerProperties((LayerProperties)feature.getLayerProperties().clone());
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
        return container.get(propertyName);
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
        if ((styles == null) || styles.isEmpty()) {
            return this.getStyle().isDrawFill() ? this.getStyle().getFillColor() : null;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fillingStyle  DOCUMENT ME!
     */
    @Override
    public void setFillingPaint(final Paint fillingStyle) {
        this.getStyle().setFillColor((Color)fillingStyle);
        this.getStyle().setDrawFill(true);
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
            return 0;
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
    public void setGeometry(final Geometry geom) {
        this.geometry = geom;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean canBeSelected() {
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
     * does not have the limitations from the rescaleStickyNode(PSticky) method of the MappingsComponent.
     *
     * @param  pfeature  DOCUMENT ME!
     * @param  sticky    DOCUMENT ME!
     */
    private void rescaleStickyNode(final PFeature pfeature, final PSticky sticky) {
        final double s = pfeature.getMappingComponent().getCamera().getViewScale();
        sticky.setScale(1 / s);
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
        }

        return super.equals(obj);
     * @param  uom       DOCUMENT ME!
     * @param  fill      DOCUMENT ME!
     * @param  pfeature  DOCUMENT ME!
     * @param  map       DOCUMENT ME!
    }

    /**
     * Ändert das dem Namen zugeordnete Property.
     *
     * @param  propertyName   Name des gesuchten Objekts
     * @param  propertyValue  neuer Wert des Properties
