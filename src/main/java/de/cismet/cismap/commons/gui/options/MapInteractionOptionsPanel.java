/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.options;

import org.jdom.Element;

import org.openide.util.lookup.ServiceProvider;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateNewGeometryListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.RubberBandZoomListener;
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
public class MapInteractionOptionsPanel extends AbstractOptionsPanel implements OptionsPanelController {

    //~ Static fields/initializers ---------------------------------------------

    private static final String OPTION_NAME = org.openide.util.NbBundle.getMessage(
            MapInteractionOptionsPanel.class,
            "MapInteractionOptionsPanel.jLabel3.text");
    private static final String CONFIGURATION = "MapInteractionOptionsPanel";
    private static final String CONF_INVERTSCROLLDIRECTION = "InvertScrollDirection";
    private static final String CONF_SHOW_LINE_LENGTH = "ShowLineLength";

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    private boolean stillConfigured = false;

    private boolean invertScrollDirection;
    private boolean showLineLength;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbShowLineLength;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MapInteractionOptionsPanel object.
     */
    public MapInteractionOptionsPanel() {
        super(OPTION_NAME, CismapOptionsCategory.class);
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void update() {
        final RubberBandZoomListener listener = getListener();
        final CreateNewGeometryListener geometryListener = getNewGeomertyListener();

        if (listener != null) {
            invertScrollDirection = listener.isInvertScrollDirection();
        }

        if (geometryListener != null) {
            showLineLength = geometryListener.isShowCurrentLength();
        }

        jCheckBox1.setSelected(invertScrollDirection);
        cbShowLineLength.setSelected(showLineLength);
    }

    @Override
    public void applyChanges() {
        invertScrollDirection = jCheckBox1.isSelected();
        showLineLength = cbShowLineLength.isSelected();

        final RubberBandZoomListener listener = getListener();
        final CreateNewGeometryListener geometryListener = getNewGeomertyListener();

        if (listener != null) {
            listener.setInvertScrollDirection(invertScrollDirection);
        }
        if (geometryListener != null) {
            geometryListener.setShowCurrentLength(showLineLength);
        }
    }

    @Override
    public boolean isChanged() {
        return (this.invertScrollDirection != jCheckBox1.isSelected())
                    || (this.showLineLength != cbShowLineLength.isSelected());
    }

    @Override
    public String getTooltip() {
        return org.openide.util.NbBundle.getMessage(
                MapInteractionOptionsPanel.class,
                "MapInteractionOptionsPanel.jLabel3.toolTipText"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private RubberBandZoomListener getListener() {
        RubberBandZoomListener result = null;
        if ((CismapBroker.getInstance() != null) && (CismapBroker.getInstance().getMappingComponent() != null)) {
            result = (RubberBandZoomListener)CismapBroker.getInstance().getMappingComponent()
                        .getInputListener(MappingComponent.ZOOM);
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CreateNewGeometryListener getNewGeomertyListener() {
        CreateNewGeometryListener result = null;

        if ((CismapBroker.getInstance() != null) && (CismapBroker.getInstance().getMappingComponent() != null)) {
            result = (CreateNewGeometryListener)CismapBroker.getInstance().getMappingComponent()
                        .getInputListener(MappingComponent.NEW_POLYGON);
        }

        return result;
    }

    @Override
    public void configure(final Element parent) {
        if (!stillConfigured) {
            try {
                String elementInvertScrollDirection = "";
                String elementShowLineLength = "";
                if (parent != null) {
                    final Element conf = parent.getChild(CONFIGURATION);
                    if (conf != null) {
                        elementInvertScrollDirection = conf.getChildText(CONF_INVERTSCROLLDIRECTION);
                        elementShowLineLength = conf.getChildText(CONF_SHOW_LINE_LENGTH);
                    }
                }
                invertScrollDirection = Boolean.valueOf(elementInvertScrollDirection);
                showLineLength = Boolean.valueOf(elementShowLineLength);
            } catch (Exception ex) {
                log.error("Fehler beim Konfigurieren des MapInteractionOptionsPanel", ex);
            }

            // hier werden die Werte in der GUI gesetzt
            jCheckBox1.setSelected(invertScrollDirection);
            cbShowLineLength.setSelected(showLineLength);

            stillConfigured = true;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("skip Configure MapInteractionOptionsPanel - still configured");
            }
        }

        // Änderungen anwenden
        applyChanges();
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        final Element conf = new Element(CONFIGURATION);
        final Element elementInvertScrollDirection = new Element(CONF_INVERTSCROLLDIRECTION);
        final Element elementShowLineLength = new Element(CONF_SHOW_LINE_LENGTH);

        elementInvertScrollDirection.addContent(Boolean.toString(invertScrollDirection));
        conf.addContent(elementInvertScrollDirection);

        elementShowLineLength.addContent(Boolean.toString(showLineLength));
        conf.addContent(elementShowLineLength);

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

        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        cbShowLineLength = new javax.swing.JCheckBox();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                new java.awt.Dimension(0, 0),
                new java.awt.Dimension(32767, 0));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                new java.awt.Dimension(0, 0),
                new java.awt.Dimension(0, 0));

        jLabel3.setText(org.openide.util.NbBundle.getMessage(
                MapInteractionOptionsPanel.class,
                "MapInteractionOptionsPanel.jLabel3.text"));        // NOI18N
        jLabel3.setToolTipText(org.openide.util.NbBundle.getMessage(
                MapInteractionOptionsPanel.class,
                "MapInteractionOptionsPanel.jLabel3.toolTipText")); // NOI18N

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(
                MapInteractionOptionsPanel.class,
                "MapInteractionOptionsPanel.jCheckBox1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel1.add(jCheckBox1, gridBagConstraints);

        cbShowLineLength.setText(org.openide.util.NbBundle.getMessage(
                MapInteractionOptionsPanel.class,
                "MapInteractionOptionsPanel.cbShowLineLength.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel1.add(cbShowLineLength, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(filler1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(filler2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 5, 5, 5);
        add(jPanel1, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents
}
