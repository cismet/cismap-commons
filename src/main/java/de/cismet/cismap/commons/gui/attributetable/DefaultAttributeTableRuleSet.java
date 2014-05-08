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

import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;

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
public class DefaultAttributeTableRuleSet {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isColumnEditable(final String columnName) {
        return true;
    }

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
    public Object afterEdit(final String column, final int row, final Object oldValue, final Object newValue) {
        return newValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TableCellRenderer getCellRenderer(final String columnName) {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TableCellEditor getCellEditor(final String columnName) {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   model  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean prepareForSave(final TableModel model) {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    public void beforeSave(final FeatureServiceFeature feature) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  model  DOCUMENT ME!
     */
    public void afterSave(final TableModel model) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String[] getAdditionalFieldNames() {
        return new String[0];
    }

    /**
     * DOCUMENT ME!
     *
     * @param   index    DOCUMENT ME!
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getAdditionalFieldValue(final int index, final FeatureServiceFeature feature) {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   index  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Class getAdditionalFieldClass(final int index) {
        return String.class;
    }
}
