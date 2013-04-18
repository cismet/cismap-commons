/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.commons.cismap.io;

import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.0
 */
public class AddGeometriesToMapEnterDataVisualPanel extends javax.swing.JPanel {

    //~ Instance fields --------------------------------------------------------

    private final transient AddGeometriesToMapEnterDataWizardPanel model;
    private final transient ChangeListener modelChangeL;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JButton btnOpenFile = new javax.swing.JButton();
    private final transient javax.swing.JEditorPane edpCoordinates = new javax.swing.JEditorPane();
    private final transient javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    private final transient javax.swing.JLabel lblCoordinates = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblFile = new javax.swing.JLabel();
    private final transient javax.swing.JTextField txtFile = new javax.swing.JTextField();
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form AddGeometriesToMapEnterDataVisualPanel.
     *
     * @param  model  DOCUMENT ME!
     */
    public AddGeometriesToMapEnterDataVisualPanel(final AddGeometriesToMapEnterDataWizardPanel model) {
        this.model = model;
        this.modelChangeL = new ModelChangeListener();

        initComponents();

        this.setName("Enter data");

        this.model.addChangeListener(WeakListeners.change(modelChangeL, model));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public AddGeometriesToMapEnterDataWizardPanel getModel() {
        return model;
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

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        lblCoordinates.setText(NbBundle.getMessage(
                AddGeometriesToMapEnterDataVisualPanel.class,
                "AddGeometriesToMapEnterDataVisualPanel.lblCoordinates.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(lblCoordinates, gridBagConstraints);

        final org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${model.coordinateData}"),
                edpCoordinates,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jScrollPane1.setViewportView(edpCoordinates);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jScrollPane1, gridBagConstraints);

        lblFile.setText(NbBundle.getMessage(
                AddGeometriesToMapEnterDataVisualPanel.class,
                "AddGeometriesToMapEnterDataVisualPanel.lblFile.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(lblFile, gridBagConstraints);

        txtFile.setText(NbBundle.getMessage(
                AddGeometriesToMapEnterDataVisualPanel.class,
                "AddGeometriesToMapEnterDataVisualPanel.txtFile.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(txtFile, gridBagConstraints);

        btnOpenFile.setText(NbBundle.getMessage(
                AddGeometriesToMapEnterDataVisualPanel.class,
                "AddGeometriesToMapEnterDataVisualPanel.btnOpenFile.text")); // NOI18N
        btnOpenFile.setMaximumSize(new java.awt.Dimension(50, 29));
        btnOpenFile.setMinimumSize(new java.awt.Dimension(50, 29));
        btnOpenFile.setPreferredSize(new java.awt.Dimension(50, 29));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(btnOpenFile, gridBagConstraints);

        bindingGroup.bind();
    } // </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class ModelChangeListener implements ChangeListener {

        //~ Methods ------------------------------------------------------------

        // subobtimal, propertychange would be better
        @Override
        public void stateChanged(final ChangeEvent e) {
            if (e.getSource() instanceof AddGeometriesToMapEnterDataWizardPanel) {
                lblCoordinates.setText(NbBundle.getMessage(
                        AddGeometriesToMapEnterDataVisualPanel.class,
                        "AddGeometriesToMapEnterDataVisualPanel.lblCoordinates.text", // NOI18N
                        model.getEpsgCode()));
                edpCoordinates.setText(model.getCoordinateData());
            }
        }
    }
}
