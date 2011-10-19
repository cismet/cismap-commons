/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.options;

import org.jdom.Element;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import java.awt.Color;

import javax.swing.JColorChooser;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateSearchGeometryListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.lookupoptions.*;

import de.cismet.tools.configuration.NoWriteError;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = OptionsPanelController.class)
public class SearchOptionsPanel extends AbstractOptionsPanel implements OptionsPanelController {

    //~ Static fields/initializers ---------------------------------------------

    private static final String OPTION_NAME = NbBundle.getMessage(
            SearchOptionsPanel.class,
            "SearchOptionsPanel.OPTION_NAME");
    private static final String CONFIGURATION = "SearchOptionsPanel";
    private static final String CONF_HOLD_GEOMETRIES = "HoldGeometries";
    private static final String CONF_GEOMETRY_COLOR = "GeometryColor";
    private static final String CONF_GEOMETRY_TRANSPARENCY = "GeometryTransparency";

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    private boolean stillConfigured = false;
    private boolean holdGeometries;
    private float geometryTransparency;
    private Color geometryColor;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSlider jSlider1;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SearchOptionsPanel object.
     */
    public SearchOptionsPanel() {
        super(OPTION_NAME, CismapOptionsCategory.class);
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public void update() {
        final CreateSearchGeometryListener listener = getListener();
        if (listener != null) {
            holdGeometries = listener.isHoldingGeometries();
            geometryColor = listener.getSearchColor();
            geometryTransparency = listener.getSearchTransparency();
        }

        jCheckBox1.setSelected(holdGeometries);
        jPanel1.setBackground(geometryColor);
        jSlider1.setValue((int)(geometryTransparency * 100f));
    }

    @Override
    public void applyChanges() {
        holdGeometries = jCheckBox1.isSelected();
        geometryColor = jPanel1.getBackground();
        geometryTransparency = jSlider1.getValue() / 100f;

        final CreateSearchGeometryListener listener = getListener();
        if (listener != null) {
            listener.setHoldGeometries(holdGeometries);
            listener.setSearchColor(geometryColor);
            listener.setSearchTransparency(geometryTransparency);
        }
    }

    @Override
    public boolean isChanged() {
        return (holdGeometries != jCheckBox1.isSelected())
                    || (geometryColor.getRGB() != jPanel1.getBackground().getRGB())
                    || (geometryTransparency != (jSlider1.getValue() / 100f));
    }

    @Override
    public String getTooltip() {
        return "";
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CreateSearchGeometryListener getListener() {
        CreateSearchGeometryListener result = null;
        if ((CismapBroker.getInstance() != null)
                    && (CismapBroker.getInstance().getMappingComponent() != null)
                    && (CismapBroker.getInstance().getMappingComponent().getInputListener(
                            MappingComponent.CREATE_SEARCH_POLYGON) != null)) {
            result = (CreateSearchGeometryListener)CismapBroker.getInstance().getMappingComponent()
                        .getInputListener(MappingComponent.CREATE_SEARCH_POLYGON);
        }

        return result;
    }

    @Override
    public void configure(final Element parent) {
        if (!stillConfigured) {
            if (log.isDebugEnabled()) {
                log.debug("Configure SearchOptionsPanel");
            }
            try {
                String elementHoldGeometries = "";
                String elementGeometryColor = "";
                String elementGeometryTransparency = "";
                if (parent != null) {
                    final Element conf = parent.getChild(CONFIGURATION);
                    if (conf != null) {
                        elementHoldGeometries = conf.getChildText(CONF_HOLD_GEOMETRIES);
                        elementGeometryColor = conf.getChildText(CONF_GEOMETRY_COLOR);
                        elementGeometryTransparency = conf.getChildText(CONF_GEOMETRY_TRANSPARENCY);
                    }
                }
                holdGeometries = new Boolean(elementHoldGeometries);
                try {
                    geometryColor = new Color(Integer.valueOf(elementGeometryColor));
                } catch (NumberFormatException ex) {
                    geometryColor = Color.GREEN;
                }
                try {
                    geometryTransparency = Float.valueOf(elementGeometryTransparency);
                } catch (NumberFormatException ex) {
                    geometryTransparency = 0.5f;
                }
            } catch (Exception ex) {
                log.error("Fehler beim Konfigurieren des SearchOptionsPanel", ex);
            }

            // hier werden die Werte in der GUI gesetzt
            jCheckBox1.setSelected(holdGeometries);
            jPanel1.setBackground(geometryColor);
            jSlider1.setValue((int)(geometryTransparency * 100f));

            stillConfigured = true;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("skip Configure SearchOptionsPanel - still configured");
            }
        }

        // Änderungen anwenden
        applyChanges();
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        final Element conf = new Element(CONFIGURATION);

        final Element holdSearchGeometriesElement = new Element(CONF_HOLD_GEOMETRIES);
        final Element searchGeometryColorElement = new Element(CONF_GEOMETRY_COLOR);
        final Element searchGeometryTransparencyElement = new Element(CONF_GEOMETRY_TRANSPARENCY);

        holdSearchGeometriesElement.addContent(Boolean.toString(holdGeometries));
        searchGeometryColorElement.addContent(String.valueOf(geometryColor.getRGB()));
        searchGeometryTransparencyElement.addContent(String.valueOf(geometryTransparency));

        conf.addContent(holdSearchGeometriesElement);
        conf.addContent(searchGeometryColorElement);
        conf.addContent(searchGeometryTransparencyElement);

        return conf;
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

        jCheckBox1 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setAlignmentY(0.0F);
        setLayout(new java.awt.GridBagLayout());

        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(
                SearchOptionsPanel.class,
                "SearchOptionsPanel.jCheckBox1.text")); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jCheckBox1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 5, 3, 5);
        add(jCheckBox1, gridBagConstraints);

        jButton1.setText(org.openide.util.NbBundle.getMessage(
                SearchOptionsPanel.class,
                "SearchOptionsPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        add(jButton1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jSeparator1, gridBagConstraints);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(
                SearchOptionsPanel.class,
                "SearchOptionsPanel.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 5);
        add(jSlider1, gridBagConstraints);

        jPanel1.setPreferredSize(new java.awt.Dimension(58, 29));

        final javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                58,
                Short.MAX_VALUE));
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(
                0,
                29,
                Short.MAX_VALUE));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(jPanel1, gridBagConstraints);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(
                SearchOptionsPanel.class,
                "SearchOptionsPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 5);
        add(jLabel1, gridBagConstraints);

        jLabel3.setPreferredSize(new java.awt.Dimension(50, 17));

        final org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                jSlider1,
                org.jdesktop.beansbinding.ELProperty.create("${value}%"),
                jLabel3,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
        add(jLabel3, gridBagConstraints);

        jLabel4.setText(org.openide.util.NbBundle.getMessage(
                SearchOptionsPanel.class,
                "SearchOptionsPanel.jLabel4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(jLabel4, gridBagConstraints);

        bindingGroup.bind();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jCheckBox1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_jCheckBox1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jButton1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton1ActionPerformed
        final Color color = JColorChooser.showDialog(
                this,
                "Farbe der Such-Geometrien wählen.",
                getColor());
        if (color != null) {
            setColor(color);
        }
    }                                                                            //GEN-LAST:event_jButton1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Color getColor() {
        return jPanel1.getBackground();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newColor  DOCUMENT ME!
     */
    private void setColor(final Color newColor) {
        if (log.isDebugEnabled()) {
            log.debug("newColor: " + newColor);
        }
        jPanel1.setBackground(newColor);
    }
}
