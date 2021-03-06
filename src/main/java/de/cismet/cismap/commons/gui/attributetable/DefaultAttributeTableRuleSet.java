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
package de.cismet.cismap.commons.gui.attributetable;

import com.vividsolutions.jts.geom.Geometry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DefaultAttributeTableRuleSet implements AttributeTableRuleSet {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isColumnEditable(final String columnName) {
        return true;
    }

    @Override
    public Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue) {
        return newValue;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return null;
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        return null;
    }

    @Override
    public boolean prepareForSave(final List<FeatureServiceFeature> features) {
        return true;
    }

    @Override
    public void beforeSave(final FeatureServiceFeature feature) {
    }

    @Override
    public void afterSave(final TableModel model) {
    }

    @Override
    public String[] getAdditionalFieldNames() {
        return new String[0];
    }

    @Override
    public Object getAdditionalFieldValue(final java.lang.String propertyName, final FeatureServiceFeature feature) {
        return null;
    }

    @Override
    public String getAdditionalFieldFormula(final String propertyName) {
        return null;
    }

    @Override
    public Class getAdditionalFieldClass(final int index) {
        return String.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return null;
    }

    @Override
    public Map<String, Object> getDefaultValues() {
        return null;
    }

    @Override
    public void mouseClicked(final FeatureServiceFeature feature,
            final String columnName,
            final Object value,
            final int clickCount) {
    }

    @Override
    public int getIndexOfAdditionalFieldName(final String name) {
        final String[] fieldNames = getAdditionalFieldNames();

        if (fieldNames != null) {
            for (int i = 0; i < fieldNames.length; ++i) {
                if (fieldNames[i].equals(name)) {
                    return i;
                }
            }
        }

        return Integer.MIN_VALUE;
    }

    @Override
    public boolean isCatThree() {
        return false;
    }

    @Override
    public FeatureServiceFeature cloneFeature(final FeatureServiceFeature feature) {
        final DefaultFeatureServiceFeature newFeature = (DefaultFeatureServiceFeature)feature
                    .getLayerProperties().getFeatureService().getFeatureFactory().createNewFeature();

        final HashMap<String, Object> properties = feature.getProperties();

        for (final String propertyKey : properties.keySet()) {
            if (!propertyKey.equalsIgnoreCase("id") && !propertyKey.equals(feature.getIdExpression())) {
                newFeature.setProperty(propertyKey, properties.get(propertyKey));
            }
        }

        return newFeature;
    }

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol(final FeatureServiceFeature feature) {
        return null;
    }

    @Override
    public boolean hasCustomExportFeaturesMethod() {
        return false;
    }

    @Override
    public void exportFeatures() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean hasCustomPrintFeaturesMethod() {
        return false;
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void printFeatures() {
    }

    @Override
    public void copyProperties(final FeatureServiceFeature sourceFeature, final FeatureServiceFeature targetFeature) {
        // copy properties
        final Map<String, Object> defaultValues = getDefaultValues();

        if (defaultValues != null) {
            for (final String propName : defaultValues.keySet()) {
                targetFeature.setProperty(propName, defaultValues.get(propName));
            }
        }

        final boolean hasIdExpression = targetFeature.getLayerProperties().getIdExpressionType()
                    == LayerProperties.EXPRESSIONTYPE_PROPERTYNAME;
        final Map<String, FeatureServiceAttribute> attributeMap = targetFeature.getLayerProperties()
                    .getFeatureService()
                    .getFeatureServiceAttributes();

        for (final String attrKey : attributeMap.keySet()) {
            if (hasIdExpression
                        && targetFeature.getLayerProperties().getIdExpression().equalsIgnoreCase(attrKey)) {
                // do not change the id
                continue;
            }
            if (isColumnEditable(attrKey)) {
                final Object val = getFeaturePropertyIgnoreCase(sourceFeature, attrKey);
                if (val != null) {
                    // without this null check, the geometry will probably be overwritten
                    targetFeature.setProperty(attrKey, val);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     * @param   name     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Object getFeaturePropertyIgnoreCase(final FeatureServiceFeature feature, final String name) {
        for (final Object prop : feature.getProperties().keySet()) {
            if (prop instanceof String) {
                final String propName = (String)prop;
                if (propName.equalsIgnoreCase(name)) {
                    return feature.getProperty(propName);
                }
            }
        }

        return null;
    }

    @Override
    public Class<? extends FeatureServiceFeature> getFeatureClass() {
        return null;
    }

    @Override
    public FeatureServiceFeature[] prepareFeaturesForExport(final FeatureServiceFeature[] features) {
        return features;
    }
}
