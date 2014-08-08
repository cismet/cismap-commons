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
public interface AttributeTableRuleSet {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   column    DOCUMENT ME!
     * @param   row       DOCUMENT ME!
     * @param   oldValue  DOCUMENT ME!
     * @param   newValue  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Object afterEdit(final String column, final int row, final Object oldValue, final Object newValue);

    /**
     * DOCUMENT ME!
     *
     * @param  model  DOCUMENT ME!
     */
    void afterSave(final TableModel model);

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    void beforeSave(final FeatureServiceFeature feature);

    /**
     * DOCUMENT ME!
     *
     * @param   index  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Class getAdditionalFieldClass(final int index);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String[] getAdditionalFieldNames();

    /**
     * DOCUMENT ME!
     *
     * @param   index    DOCUMENT ME!
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Object getAdditionalFieldValue(final int index, final FeatureServiceFeature feature);

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
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    FeatureCreator getFeatureCreator();

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isColumnEditable(final String columnName);

    /**
     * DOCUMENT ME!
     *
     * @param   model  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean prepareForSave(final TableModel model);
}
