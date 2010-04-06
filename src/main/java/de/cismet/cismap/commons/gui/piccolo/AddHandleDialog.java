/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AddHandleDialog.java
 *
 * Created on 05.11.2009, 15:38:03
 */
package de.cismet.cismap.commons.gui.piccolo;

import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author jruiz
 */
public class AddHandleDialog extends javax.swing.JDialog {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    public static final int STATUS_NONE = -1;
    public static final int STATUS_OK = 0;
    public static final int STATUS_CANCELED = 1;

    private static final double PRECISION = 100; // => "1/PRECISION"

    private int returnStatus = STATUS_NONE;

    private boolean updateLeft = true;
    private boolean updateRight = true;

    /** Creates new form AddHandleDialog */
    public AddHandleDialog(java.awt.Frame parent, boolean modal, double distanceTotal) {
        super(parent, modal);

        initComponents();
        getRootPane().setDefaultButton(btnOK);

        setDistanceTotal(distanceTotal);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        txtLeft = new javax.swing.JTextField();
        txtRight = new javax.swing.JTextField();
        lblNew = new javax.swing.JLabel();
        sliDistance = new javax.swing.JSlider();
        lblLeftNeighbour = new javax.swing.JLabel();
        lblRightNeighbour = new javax.swing.JLabel();
        lblDistanceLeft = new javax.swing.JLabel();
        lblRightDistance = new javax.swing.JLabel();
        lblLeftPoint = new javax.swing.JLabel();
        lblRightPoint = new javax.swing.JLabel();
        lblNewPoint = new javax.swing.JLabel();
        panFooter = new javax.swing.JPanel();
        lblDescriptionImage = new javax.swing.JLabel();
        lblDescription = new javax.swing.JLabel();
        panButtons = new javax.swing.JPanel();
        btnOK = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        lblLeftSpacer = new javax.swing.JLabel();
        lblTopLeftSpacer = new javax.swing.JLabel();
        lblRightSpacer = new javax.swing.JLabel();
        lblTopRightSpacer = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.title")); // NOI18N
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        txtLeft.getDocument().addDocumentListener(new LeftDocumentListener());
        txtLeft.setText(String.valueOf(getDistanceToLeft()));
        txtLeft.setPreferredSize(new java.awt.Dimension(50, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(txtLeft, gridBagConstraints);

        txtRight.getDocument().addDocumentListener(new RightDocumentListener());
        txtRight.setText(String.valueOf(getDistanceToRight()));
        txtRight.setPreferredSize(new java.awt.Dimension(50, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(txtRight, gridBagConstraints);

        lblNew.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNew.setText(org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.lblNew.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(lblNew, gridBagConstraints);

        sliDistance.setMajorTickSpacing((int) PRECISION);
        sliDistance.setPaintTicks(true);
        sliDistance.setValue(0);
        sliDistance.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliDistanceStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(sliDistance, gridBagConstraints);

        lblLeftNeighbour.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLeftNeighbour.setText(org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.lblLeftNeighbour.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(lblLeftNeighbour, gridBagConstraints);

        lblRightNeighbour.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRightNeighbour.setText(org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.lblRightNeighbour.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(lblRightNeighbour, gridBagConstraints);

        lblDistanceLeft.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDistanceLeft.setText(org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.lblDistanceLeft.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(lblDistanceLeft, gridBagConstraints);

        lblRightDistance.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblRightDistance.setText(org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.lblDistanceRight.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(lblRightDistance, gridBagConstraints);

        lblLeftPoint.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLeftPoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/piccolo/neighbourPoint.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(lblLeftPoint, gridBagConstraints);

        lblRightPoint.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRightPoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/piccolo/neighbourPoint.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(lblRightPoint, gridBagConstraints);

        lblNewPoint.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNewPoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/piccolo/newPoint.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(lblNewPoint, gridBagConstraints);

        panFooter.setLayout(new java.awt.GridBagLayout());

        lblDescriptionImage.setText(org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.lblDescriptionImage.text")); // NOI18N
        lblDescriptionImage.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        lblDescriptionImage.setPreferredSize(new java.awt.Dimension(250, 120));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panFooter.add(lblDescriptionImage, gridBagConstraints);

        lblDescription.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/piccolo/addHandle.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panFooter.add(lblDescription, gridBagConstraints);

        btnOK.setText(org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.btnOK.text")); // NOI18N
        btnOK.setPreferredSize(new java.awt.Dimension(80, 29));
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });
        panButtons.add(btnOK);

        btnCancel.setText(org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.btnCancel.text")); // NOI18N
        btnCancel.setPreferredSize(new java.awt.Dimension(80, 29));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        panButtons.add(btnCancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        panFooter.add(panButtons, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(panFooter, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.38;
        getContentPane().add(lblLeftSpacer, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(lblTopLeftSpacer, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.38;
        getContentPane().add(lblRightSpacer, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(lblTopRightSpacer, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        if (checkTextFields()) {
            returnStatus = STATUS_OK;
            dispose();
        }
    }//GEN-LAST:event_btnOKActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        returnStatus = STATUS_CANCELED;
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void sliDistanceStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliDistanceStateChanged
        sliderValueChanged();
    }//GEN-LAST:event_sliDistanceStateChanged

    private double cut(double value) {
        return Math.round(value * PRECISION) / PRECISION;
    }

    private void sliderValueChanged() {
        if (updateLeft) { // Endlos-Schleife vermeiden
            txtLeft.setText(String.valueOf(getDistanceToLeft()));
        }
        if (updateRight) { // Endlos-Schleife vermeiden
            txtRight.setText(String.valueOf(getDistanceToRight()));
        }
    }

    private void leftTextChanged() {
        double distanceToLeft = 0;
        try {
            // Abstand vom linken Punkt anhand des linken Feldes setzen
            distanceToLeft = Double.valueOf(txtLeft.getText());
            // nur was tun wenn der Abstand innerhalb der gültigen Grenzen liegt
            if (distanceToLeft >= 0 && distanceToLeft <= getDistanceTotal()) {
                // update für das linke Feld kurzfristig deaktivieren (sonst endlos-Schleife)
                updateLeft = false;
                // Slider auf den Wert des linken Feldes setzen
                setDistanceToLeft(distanceToLeft);
                // update für das linke Feld wieder aktivieren
                updateLeft = true;
            }
        } catch (NumberFormatException ex) {
            // keine Nummer eingegeben => nichts tun
        }
    }

    private void rightTextChanged() {
        double distanceToRight = 0;
        try {
            // Abstand vom linken Punkt anhand des Wertes im linken Feld berechnen
            distanceToRight = Double.valueOf(txtRight.getText());
            // nur was tun wenn der Abstand innerhalb der gültigen Grenzen liegt
            if (distanceToRight >= 0 && distanceToRight <= getDistanceTotal()) {
                // update für das rechte Feld kurzfristig deaktivieren (sonst endlos-Schleife)
                updateRight = false;
                // Slider auf den Wert des rechten Feldes setzen
                setDistanceToRight(distanceToRight);
                // update für das rechte Feld wieder aktivieren
                updateRight = true;
            }
        } catch (NumberFormatException ex) {
            // keine Nummer eingegeben => nichts tun
        }
    }

    private boolean checkTextFields() {
        double distanceToLeft;
        double distanceToRight;

        // LINKS
        // - ist eine Zahl?
        try {
            distanceToLeft = Double.valueOf(txtLeft.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, 
                    org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.checkTextFields().JOptionPane1.message"),//NOI18N
                    org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.checkTextFields().JOptionPane1.title"), //NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // - ist kleiner Gesamt-Abstand
        if (distanceToLeft > getDistanceTotal()) {
            JOptionPane.showMessageDialog(null, 
                    org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.checkTextFields().JOptionPane2.message"),//NOI18N
                    org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.checkTextFields().JOptionPane2.title"),//NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // - ist größer 0
        if (distanceToLeft < 0) {
            JOptionPane.showMessageDialog(null, 
                    org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.checkTextFields().JOptionPane3.message"),//NOI18N
                    org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.checkTextFields().JOptionPane3.title"),//NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // RECHTS
        // - ist eine Zahl?
        try {
            distanceToRight = Double.valueOf(txtRight.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, 
                    org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.checkTextFields().JOptionPane4.message"),//NOI18N
                    org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.checkTextFields().JOptionPane4.title"),//NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // - ist kleiner Gesamt-Abstand
        if (distanceToRight > getDistanceTotal()) {
            JOptionPane.showMessageDialog(null, 
                    org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.checkTextFields().JOptionPane5.message"),//NOI18N
                    org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.checkTextFields().JOptionPane5.title"),//NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // - ist größer 0
        if (distanceToRight < 0) {
            JOptionPane.showMessageDialog(null, 
                    org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.checkTextFields().JOptionPane6.message"),//NOI18N
                    org.openide.util.NbBundle.getMessage(AddHandleDialog.class, "AddHandleDialog.checkTextFields().JOptionPane6.title"),//NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // keine Fehler
        return true;
    }

    public int getReturnStatus() {
        return returnStatus;
    }

    public double getDistanceTotal() {
        return sliDistance.getMaximum() / PRECISION;
    }

    public double getDistanceToLeft() {
        return sliDistance.getValue() / PRECISION;
    }

    public double getDistanceToRight() {
        return cut(getDistanceTotal() - getDistanceToLeft());
    }

    private void setDistanceTotal(double distanceTotal) {
        sliDistance.setMaximum((int) (cut(distanceTotal) * PRECISION));
    }

    public void setDistanceToLeft(double distanceToLeft) {
        sliDistance.setValue((int) (cut(distanceToLeft) * PRECISION));
    }

    public void setDistanceToRight(double distanceToRight) {
        sliDistance.setValue((int) ((getDistanceTotal() - cut(distanceToRight)) * PRECISION));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                AddHandleDialog dialog = new AddHandleDialog(new javax.swing.JFrame(), true, 23d);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblDescriptionImage;
    private javax.swing.JLabel lblDistanceLeft;
    private javax.swing.JLabel lblLeftNeighbour;
    private javax.swing.JLabel lblLeftPoint;
    private javax.swing.JLabel lblLeftSpacer;
    private javax.swing.JLabel lblNew;
    private javax.swing.JLabel lblNewPoint;
    private javax.swing.JLabel lblRightDistance;
    private javax.swing.JLabel lblRightNeighbour;
    private javax.swing.JLabel lblRightPoint;
    private javax.swing.JLabel lblRightSpacer;
    private javax.swing.JLabel lblTopLeftSpacer;
    private javax.swing.JLabel lblTopRightSpacer;
    private javax.swing.JPanel panButtons;
    private javax.swing.JPanel panFooter;
    private javax.swing.JSlider sliDistance;
    private javax.swing.JTextField txtLeft;
    private javax.swing.JTextField txtRight;
    // End of variables declaration//GEN-END:variables

    class LeftDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            leftTextChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            leftTextChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            leftTextChanged();
        }
    }

    class RightDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            rightTextChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            rightTextChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            rightTextChanged();
        }
    }

}
