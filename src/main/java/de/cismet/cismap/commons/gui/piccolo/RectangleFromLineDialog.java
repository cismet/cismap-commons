/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2010 jruiz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * RectangleFromLineDialog.java
 *
 * Created on 04.08.2010, 13:53:09
 */
package de.cismet.cismap.commons.gui.piccolo;

import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.Converter;

import java.awt.event.KeyEvent;

import java.text.NumberFormat;

import java.util.LinkedList;

import javax.swing.JFormattedTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class RectangleFromLineDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    public static final int STATUS_NONE = -1;
    public static final int STATUS_OK = 0;
    public static final int STATUS_CANCELED = 1;

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    private double length = 0;
    private NumberFormat format = NumberFormat.getInstance();
    private int returnStatus = STATUS_NONE;

    private LinkedList<ChangeListener> widthChangedListeners = new LinkedList<ChangeListener>();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblLength;
    private javax.swing.JPanel panButtons;
    private javax.swing.JPanel panParams;
    private javax.swing.JPanel panSide;
    private javax.swing.JTextField txtSurface;
    private javax.swing.JTextField txtWidth;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form RectangleFromLineDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     * @param  length  DOCUMENT ME!
     */
    public RectangleFromLineDialog(final java.awt.Frame parent, final boolean modal, final double length) {
        super(parent, modal);
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        initComponents();
        lblLength.setText(format.format(length));
        this.length = length;

        bindingGroup.addBindingListener(new AbstractBindingListener() {

                @Override
                public void synced(final Binding bndng) {
                    fireStateChanged();
                }
            });

        jRadioButton1.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(final ChangeEvent ce) {
                    fireStateChanged();
                }
            });

        getRootPane().setDefaultButton(btnOK);
        StaticSwingTools.doClickButtonOnKeyStroke(btnOK, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), getRootPane());
        StaticSwingTools.doClickButtonOnKeyStroke(
            btnCancel,
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            getRootPane());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void fireStateChanged() {
        for (final ChangeListener cl : widthChangedListeners) {
            cl.stateChanged(new ChangeEvent(this));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        buttonGroup1 = new javax.swing.ButtonGroup();
        panParams = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtSurface = new JFormattedTextField(format);
        txtWidth = new JFormattedTextField(format);
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        panSide = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblLength = new javax.swing.JLabel();
        panButtons = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        btnCancel = new javax.swing.JButton();
        btnOK = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(RectangleFromLineDialog.class, "RectangleFromLineDialog.title")); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        panParams.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(org.openide.util.NbBundle.getMessage(
                RectangleFromLineDialog.class,
                "RectangleFromLineDialog.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.05;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(jLabel1, gridBagConstraints);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(
                RectangleFromLineDialog.class,
                "RectangleFromLineDialog.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(jLabel2, gridBagConstraints);

        txtSurface.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        txtSurface.setText(org.openide.util.NbBundle.getMessage(
                RectangleFromLineDialog.class,
                "RectangleFromLineDialog.txtSurface.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(txtSurface, gridBagConstraints);

        txtWidth.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        final org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                txtSurface,
                org.jdesktop.beansbinding.ELProperty.create("${text}"),
                txtWidth,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceNullValue("0,00");
        binding.setSourceUnreadableValue("0,00");
        binding.setConverter(new WidthToSurfaceConverter());
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(txtWidth, gridBagConstraints);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(
                RectangleFromLineDialog.class,
                "RectangleFromLineDialog.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.025;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(jLabel3, gridBagConstraints);

        jLabel4.setText(org.openide.util.NbBundle.getMessage(
                RectangleFromLineDialog.class,
                "RectangleFromLineDialog.jLabel4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(jLabel4, gridBagConstraints);

        panSide.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 25, 0));

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText(org.openide.util.NbBundle.getMessage(
                RectangleFromLineDialog.class,
                "RectangleFromLineDialog.jRadioButton1.text")); // NOI18N
        panSide.add(jRadioButton1);

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText(org.openide.util.NbBundle.getMessage(
                RectangleFromLineDialog.class,
                "RectangleFromLineDialog.jRadioButton2.text_3")); // NOI18N
        panSide.add(jRadioButton2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(panSide, gridBagConstraints);

        jLabel5.setText(org.openide.util.NbBundle.getMessage(
                RectangleFromLineDialog.class,
                "RectangleFromLineDialog.jLabel5.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(jLabel5, gridBagConstraints);

        jLabel6.setText(org.openide.util.NbBundle.getMessage(
                RectangleFromLineDialog.class,
                "RectangleFromLineDialog.jLabel6.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(jLabel6, gridBagConstraints);

        lblLength.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblLength.setText(org.openide.util.NbBundle.getMessage(
                RectangleFromLineDialog.class,
                "RectangleFromLineDialog.lblLength.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panParams.add(lblLength, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(panParams, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        btnCancel.setText(org.openide.util.NbBundle.getMessage(
                RectangleFromLineDialog.class,
                "RectangleFromLineDialog.btnCancel.text_2")); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnCancelActionPerformed(evt);
                }
            });
        jPanel1.add(btnCancel);

        btnOK.setText(org.openide.util.NbBundle.getMessage(
                RectangleFromLineDialog.class,
                "RectangleFromLineDialog.btnOK.text_2")); // NOI18N
        btnOK.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnOKActionPerformed(evt);
                }
            });
        jPanel1.add(btnOK);

        panButtons.add(jPanel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(panButtons, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jSeparator2, gridBagConstraints);

        bindingGroup.bind();

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  cl  DOCUMENT ME!
     */
    public void addWidthChangedListener(final ChangeListener cl) {
        widthChangedListeners.add(cl);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getRectangleWidth() {
        return convertStringToDouble(txtWidth.getText());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isLefty() {
        return jRadioButton1.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   width  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double convertWidthToSurface(final double width) {
        return width * length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   surface  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double convertSurfaceToWidth(final double surface) {
        if (length == 0) {
            return 0d;
        } else {
            return surface / length;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   surfaceString  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static double convertStringToDouble(final String surfaceString) {
        try {
            return Double.parseDouble(surfaceString.replace(',', '.'));
        } catch (NumberFormatException ex) {
            return 0d;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getReturnStatus() {
        return returnStatus;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnOKActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnOKActionPerformed
        returnStatus = STATUS_OK;
        dispose();
    }                                                                         //GEN-LAST:event_btnOKActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
        returnStatus = STATUS_CANCELED;
        dispose();
    }                                                                             //GEN-LAST:event_btnCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final RectangleFromLineDialog dialog = new RectangleFromLineDialog(
                            new javax.swing.JFrame(),
                            true,
                            0);
                    dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                            @Override
                            public void windowClosing(final java.awt.event.WindowEvent e) {
                                System.exit(0);
                            }
                        });
                    dialog.setVisible(true);
                }
            });
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class WidthToSurfaceConverter extends Converter<String, String> {

        //~ Methods ------------------------------------------------------------

        @Override
        public String convertForward(final String surfaceString) {
            final double surface = convertStringToDouble(surfaceString);
            return format.format(convertSurfaceToWidth(surface));
        }

        @Override
        public String convertReverse(final String widthString) {
            final double width = convertStringToDouble(widthString);
            return format.format(convertWidthToSurface(width));
        }
    }
}
