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
package de.cismet.cismap.commons.gui.featureinfopanel;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataSourceProvider;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignField;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FeaturePanelAttributeSource implements JRDataSource, JRDataSourceProvider {

    //~ Instance fields --------------------------------------------------------

    private int index = -1;
    private TableModel model;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PrintSource object.
     *
     * @deprecated  this constructor is only for the usage in ireport
     */
    public FeaturePanelAttributeSource() {
        // sample data to test this JRDataSourceProvider in ireport
        model = new DefaultTableModel(
                new String[][] {
                    { "a", "b" },
                    { "c", "d" }
                },
                new String[] { "name", "value" });
    }

    /**
     * Creates a new PrintSource object.
     *
     * @param  model  DOCUMENT ME!
     */
    public FeaturePanelAttributeSource(final TableModel model) {
        this.model = model;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean next() throws JRException {
        final boolean ret = ++index < model.getRowCount();

        if (!ret) {
            // Set the internal index to the first row, when the return value is false,
            // so that the data source can used from multiple sub reports.
            index = -1;
        }

        return ret;
    }

    @Override
    public Object getFieldValue(final JRField jrField) throws JRException {
        final String colName = jrField.getName();

        if (colName.equalsIgnoreCase("value")) {
            return String.valueOf(model.getValueAt(index, 1));
        } else {
            return String.valueOf(model.getValueAt(index, 0));
        }
    }

    @Override
    public boolean supportsGetFieldsOperation() {
        return false;
    }

    @Override
    public JRField[] getFields(final JasperReport report) throws JRException, UnsupportedOperationException {
        final JRDesignField keyField = new JRDesignField();
        keyField.setName("key");
        keyField.setValueClass(String.class);
        keyField.setValueClassName("java.lang.String");
        final JRDesignField valueField = new JRDesignField();
        valueField.setName("value");
        valueField.setValueClass(String.class);
        valueField.setValueClassName("java.lang.String");

        return new JRField[] { keyField, valueField };
    }

    @Override
    public JRDataSource create(final JasperReport report) throws JRException {
        return this;
    }

    @Override
    public void dispose(final JRDataSource dataSource) throws JRException {
    }
}
