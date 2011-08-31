/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * MapScaleFixedPanel.java
 *
 * Created on 29.08.2011, 12:01:26
 */
package de.cismet.cismap.commons.gui.statusbar;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class MapScaleFixedPanel extends javax.swing.JPanel {

    //~ Instance fields --------------------------------------------------------

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblMapScaleFixedIcon;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form MapScaleFixedPanel.
     */
    public MapScaleFixedPanel() {
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblMapScaleFixedIcon = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        lblMapScaleFixedIcon.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/statusbar/fixMapScale.png"))); // NOI18N
        lblMapScaleFixedIcon.setText(org.openide.util.NbBundle.getMessage(
                MapScaleFixedPanel.class,
                "MapScaleFixedPanel.lblMapScaleFixedIcon.text"));                                    // NOI18N
        lblMapScaleFixedIcon.setToolTipText(org.openide.util.NbBundle.getMessage(
                MapScaleFixedPanel.class,
                "MapScaleFixedPanel.lblMapScaleFixedIcon.toolTipText"));                             // NOI18N
        add(lblMapScaleFixedIcon, java.awt.BorderLayout.CENTER);
    }                                                                                                // </editor-fold>//GEN-END:initComponents
}
