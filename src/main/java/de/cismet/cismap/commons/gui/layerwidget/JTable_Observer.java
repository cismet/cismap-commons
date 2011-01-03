/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.layerwidget;

/**
 *
 * @author thorsten.hell@cismet.de
 */
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Observable;
import java.util.Observer;

import javax.swing.*;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/*
 * Created on Apr 18, 2005
 * JTable_Observer.java
 */

/**
 * DOCUMENT ME!
 *
 * @author   Mayank Joshi
 * @version  $Revision$, $Date$
 */
public class JTable_Observer extends JFrame implements Observer {

    //~ Instance fields --------------------------------------------------------

    JTable table;
    MyDefaultTableModel dtm;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JTable_Observer object.
     */
    private JTable_Observer() {
        super(org.openide.util.NbBundle.getMessage(JTable_Observer.class, "JTable_Observer.title")); // NOI18N
        createUI();
        new ObservableChild().addObserver(this);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        new JTable_Observer();
    }

    /**
     * DOCUMENT ME!
     */
    private void createUI() {
        dtm = new MyDefaultTableModel(
                new Object[] {
                    org.openide.util.NbBundle.getMessage(JTable_Observer.class, "JTable_Observer.dtm.title1"), // NOI18N
                    org.openide.util.NbBundle.getMessage(JTable_Observer.class, "JTable_Observer.dtm.title2")
                },
                0);                                                                                            // NOI18N
        final CustomProgressBar progressBar = new CustomProgressBar();
        final CustomProgressBar progressBar2 = new CustomProgressBar();
        final CustomProgressBar progressBar3 = new CustomProgressBar();
        progressBar.setBorderPainted(true);
        progressBar.setIndeterminate(true);
        progressBar2.setBorderPainted(true);
        progressBar2.setIndeterminate(true);
        progressBar3.setBorderPainted(true);
        progressBar3.setIndeterminate(true);
        dtm.addRow(
            new Object[] {
                org.openide.util.NbBundle.getMessage(JTable_Observer.class, "JTable_Observer.row1"),
                progressBar
            });                                                                                                // NOI18N
        dtm.addRow(
            new Object[] {
                org.openide.util.NbBundle.getMessage(JTable_Observer.class, "JTable_Observer.row2"),
                progressBar2
            });                                                                                                // NOI18N
        dtm.addRow(
            new Object[] {
                org.openide.util.NbBundle.getMessage(JTable_Observer.class, "JTable_Observer.row3"),
                progressBar3
            });                                                                                                // NOI18N
        table = new JTable(dtm);
        table.getColumnModel().getColumn(1).setCellRenderer(new CustomProgressBar());
        final JScrollPane jsp = new JScrollPane(table);
        getContentPane().add(jsp);
        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void update(final Observable o, final Object arg) {
        ((DefaultTableModel)table.getModel()).fireTableCellUpdated(0, 1);
        ((DefaultTableModel)table.getModel()).fireTableCellUpdated(1, 1);
        ((DefaultTableModel)table.getModel()).fireTableCellUpdated(2, 1);
    }
}
/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class ObservableChild extends Observable implements ActionListener {

    //~ Instance fields --------------------------------------------------------

    Timer timer = new Timer(500, this);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ObservableChild object.
     */
    public ObservableChild() {
        super();
        timer.setRepeats(true);
        timer.start();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        setChanged();
        notifyObservers();
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class CustomProgressBar extends JProgressBar implements TableCellRenderer {

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getTableCellRendererComponent(final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {
        if (value instanceof JComponent) {
            return (JComponent)value;
        } else {
            return null;
        }
    }

    @Override
    public boolean isDisplayable() {
        return true;
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class MyDefaultTableModel extends DefaultTableModel {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MyDefaultTableModel object.
     *
     * @param  obj  DOCUMENT ME!
     * @param  i    DOCUMENT ME!
     */
    public MyDefaultTableModel(final Object[] obj, final int i) {
        super(obj, i);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isCellEditable(final int row, final int column) {
        if (column == 1) {
            return false;
        } else {
            return true;
        }
    }
}
