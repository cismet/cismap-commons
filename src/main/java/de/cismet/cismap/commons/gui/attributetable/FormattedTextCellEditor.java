/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.attributetable;


import java.awt.Component;

import java.text.Format;
import java.text.ParseException;


import javax.swing.AbstractCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FormattedTextCellEditor extends AbstractCellEditor implements TableCellEditor {

    //~ Instance fields --------------------------------------------------------

    private JFormattedTextField textField;
    private boolean useSqlDate = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DateCellEditor object.
     *
     * @param  formatter  DOCUMENT ME!
     */
    public FormattedTextCellEditor(final Format formatter) {
        textField = new JFormattedTextField(formatter);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object getCellEditorValue() {
//        return textField.getText();
        try {
            return textField.getFormatter().valueToString(textField.getValue());
        } catch (ParseException ex) {
            return null;
        }
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table,
            final Object value,
            final boolean isSelected,
            final int row,
            final int column) {
        if (value instanceof String) {
            textField.setText((String)value);
        }

        return textField;
    }
}
