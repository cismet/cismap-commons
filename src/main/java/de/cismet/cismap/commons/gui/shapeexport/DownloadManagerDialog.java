/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * DownloadManagerDialog.java
 *
 * Created on 13.05.2011, 13:33:00
 */
package de.cismet.cismap.commons.gui.shapeexport;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class DownloadManagerDialog extends javax.swing.JDialog implements WindowListener {

    //~ Static fields/initializers ---------------------------------------------

    private static DownloadManagerDialog instance;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.cismet.cismap.commons.gui.shapeexport.DownloadManagerPanel pnlDownloadManagerPanel;
    private javax.swing.JScrollPane scpDownloadManagerPanel;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form DownloadManagerDialog.
     *
     * @param  parent  DOCUMENT ME!
     */
    private DownloadManagerDialog(final java.awt.Frame parent) {
        super(parent, false);
        initComponents();
        addWindowListener(pnlDownloadManagerPanel);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   parent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static DownloadManagerDialog instance(final Frame parent) {
        if (instance == null) {
            instance = new DownloadManagerDialog(parent);
            instance.addWindowListener(instance);
            instance.setPreferredSize(new Dimension(650, 200));
            instance.setLocationRelativeTo(parent);
        }

        return instance;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        scpDownloadManagerPanel = new javax.swing.JScrollPane();
        pnlDownloadManagerPanel = new de.cismet.cismap.commons.gui.shapeexport.DownloadManagerPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(DownloadManagerDialog.class, "DownloadManagerDialog.title")); // NOI18N

        scpDownloadManagerPanel.setBorder(null);
        scpDownloadManagerPanel.setViewportView(pnlDownloadManagerPanel);

        getContentPane().add(scpDownloadManagerPanel, java.awt.BorderLayout.CENTER);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    @Override
    public void windowOpened(final WindowEvent e) {
    }

    @Override
    public void windowClosing(final WindowEvent e) {
        instance = null;
    }

    @Override
    public void windowClosed(final WindowEvent e) {
    }

    @Override
    public void windowIconified(final WindowEvent e) {
    }

    @Override
    public void windowDeiconified(final WindowEvent e) {
    }

    @Override
    public void windowActivated(final WindowEvent e) {
    }

    @Override
    public void windowDeactivated(final WindowEvent e) {
    }
}
