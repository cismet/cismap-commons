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

import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface AttributeTableRuleSet {

    //~ Methods ----------------------------------------------------------------

    /**
     * This method will be invoked, after a cell was edited.
     *
     * @param   feature   the modified feature
     * @param   column    the modified attribute name
     * @param   row       the row of the feature in the model
     * @param   oldValue  the old attribute value
     * @param   newValue  the new atribute value
     *
     * @return  the new value of the cell. Usually, the new value, if it was valid.
     */
    Object afterEdit(final FeatureServiceFeature feature,
            final String column,
            final int row,
            final Object oldValue,
            final Object newValue);

    /**
     * DOCUMENT ME!
     *
     * @param  model  DOCUMENT ME!
     */
    void afterSave(final TableModel model);

    /**
     * Will be invoked before a the given feature will be saved.
     *
     * @param  feature  DOCUMENT ME!
     */
    void beforeSave(final FeatureServiceFeature feature);

    /**
     * DOCUMENT ME!
     *
     * @param   index  the index of the additional field
     *
     * @return  the class of the additional field with the given index
     */
    Class getAdditionalFieldClass(final int index);

    /**
     * The names of the additional fields. Additional fields are fields, which does not exists in the objects, but which
     * should be automatically added.
     *
     * @return  DOCUMENT ME!
     */
    String[] getAdditionalFieldNames();

    /**
     * The index of the attribute in the attribute list of the service. A negative value n means that the attribute
     * should be inserted at the nth last position.
     *
     * @param   name  The name of the attribute. See {@link getAdditionalFieldNames()}
     *
     * @return  The index of the attribute in the attribute list of the service or Integer.MIN_VALUE, if no additional
     *          property with the given name exists
     */
    int getIndexOfAdditionalFieldName(String name);

    /**
     * Determines the value of the additional field with the given index for the given feature.
     *
     * @param   propertyName  the index of the additional field
     * @param   feature       the feature, the value should be calculated for
     *
     * @return  the java.lang.Object
     */
    Object getAdditionalFieldValue(final String propertyName, final FeatureServiceFeature feature);

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    TableCellEditor getCellEditor(final String columnName);

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    TableCellRenderer getCellRenderer(final String columnName);

    /**
     * Provides a creator to create new objects of the represented service.
     *
     * @return  a creator to create new objects of the represented service
     */
    FeatureCreator getFeatureCreator();

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  true, if the given column should be editable
     */
    boolean isColumnEditable(final String columnName);

    /**
     * Will be invoked before the service will be saved.
     *
     * @param   features  the modified features
     * @param   model     A model with all features of the represented service
     *
     * @return  true, if the save operation should be executed
     */
    boolean prepareForSave(final List<FeatureServiceFeature> features, final TableModel model);

    /**
     * DOCUMENT ME!
     *
     * @param  feature     DOCUMENT ME!
     * @param  columnName  DOCUMENT ME!
     * @param  value       DOCUMENT ME!
     * @param  clickCount  DOCUMENT ME!
     */
    void mouseClicked(FeatureServiceFeature feature, String columnName, Object value, int clickCount);

    /**
     * todo: change method name
     *
     * @return  DOCUMENT ME!
     */
    boolean isCatThree();

    /**
     * Get all attribute values, which should be set, when a new object is created.
     *
     * @return  A Hashmap with the attribute names and the attribute values
     */
    Map<String, Object> getDefaultValues();

    /**
     * Clones the given feature.
     *
     * @param   feature  the feature to clone
     *
     * @return  the cloned feature
     */
    FeatureServiceFeature cloneFeature(FeatureServiceFeature feature);

    /**
     * Get the point annotation symbol, that should be used, when no style is defined.
     *
     * @param   feature  the feature, that should use the point annotation symbol
     *
     * @return  DOCUMENT ME!
     */
    FeatureAnnotationSymbol getPointAnnotationSymbol(FeatureServiceFeature feature);

    /**
     * True, if the service has its own export tool.
     *
     * @return  True, iff the service has its own export tool
     */
    boolean hasCustomExportFeaturesMethod();

    /**
     * start the feature export.
     */
    void exportFeatures();

    /**
     * True, if the service has its own print tool.
     *
     * @return  True, iff the service has its own print tool
     */
    boolean hasCustomPrintFeaturesMethod();

    /**
     * start the feature print.
     */
    void printFeatures();

    /**
     * Copies the feature properties from the source feature to the target feature.
     *
     * @param  sourceFeature  the feature with the source values
     * @param  targetFeature  the values from the source feature will be copied in this feature
     */
    void copyProperties(FeatureServiceFeature sourceFeature, FeatureServiceFeature targetFeature);
}
