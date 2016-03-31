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

import org.jdesktop.swingx.JXDatePicker;

import java.awt.Component;

import java.util.Date;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DateCellEditor extends AbstractCellEditor implements TableCellEditor {

    //~ Instance fields --------------------------------------------------------

    private JXDatePicker datePicker;
    private boolean useSqlDate = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DateCellEditor object.
     */
    public DateCellEditor() {
        datePicker = new JXDatePicker();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object getCellEditorValue() {
        if (useSqlDate) {
            return new java.sql.Date(datePicker.getDate().getTime());
        } else {
            return datePicker.getDate();
        }
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table,
            final Object value,
            final boolean isSelected,
            final int row,
            final int column) {
        if (value instanceof Date) {
            useSqlDate = false;
            datePicker.setDate((Date)value);
        }

        if (value instanceof java.sql.Date) {
            useSqlDate = true;
            final java.sql.Date date = (java.sql.Date)value;
            datePicker.setDate(new Date(date.getTime()));
        }

        return datePicker;
    }
}
