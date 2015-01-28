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

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

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
    public boolean prepareForSave(final List<FeatureServiceFeature> features, final TableModel model) {
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
    public Class getAdditionalFieldClass(final int index) {
        return String.class;
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        return null;
    }

    @Override
    public void mouseClicked(final JTable table, final String columnName, final Object value, final int clickCount) {
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

        return -1;
    }

    @Override
    public boolean isCatThree() {
        return false;
    }
}
