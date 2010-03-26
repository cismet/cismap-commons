package de.cismet.cismap.commons.gui.options;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.lookupoptions.*;
import de.cismet.tools.configuration.NoWriteError;
import org.jdom.Element;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author jruiz
 */
@ServiceProvider(service = OptionsPanelController.class)
public class GeometryOptionsPanel extends AbstractOptionsPanel implements OptionsPanelController {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    
    private static final String OPTION_NAME = org.openide.util.NbBundle.getMessage(GeometryOptionsPanel.class, "GeometryOptionsPanel.OPTION_NAME");
    private static final String CONFIGURATION = "GeometryOptionsPanel";//NOI18N
    private static final String CONF_HOLD_GEOMETRIES = "EllipseEgdes";//NOI18N

    private boolean stillConfigured = false;
    
    private int numOfEllipseEdges;

    private CreateGeometryListener listener = ((CreateGeometryListener)CismapBroker.getInstance().getMappingComponent().getInputListener(MappingComponent.CREATE_SEARCH_POLYGON));

    public GeometryOptionsPanel() {
        super(OPTION_NAME, CismapOptionsCategory.class);
        initComponents();
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public void update() {
        numOfEllipseEdges = listener.getNumOfEllipseEdges();

        jTextField1.setText(String.valueOf(numOfEllipseEdges));
    }

    @Override
    public void applyChanges() {
        numOfEllipseEdges = Integer.valueOf(jTextField1.getText());

        listener.setNumOfEllipseEdges(numOfEllipseEdges);
    }

    @Override
    public boolean isChanged() {
        int intEdges;
        try {
            intEdges = Integer.valueOf(jTextField1.getText());
        } catch (NumberFormatException ex) {
            intEdges = 0;
        }
        
        return numOfEllipseEdges != intEdges;
    }

    @Override
    public String getTooltip() {
        return "";//NOI18N
    }

    @Override
    public void configure(Element parent) {
        if (!stillConfigured) {
            log.debug("Configure GeometryOptionsPanel");//NOI18N
            try {
                String elementNumOfEllipseEdges = "";//NOI18N
                if (parent != null) {
                    final Element conf = parent.getChild(CONFIGURATION);
                    if (conf != null) {
                        elementNumOfEllipseEdges = conf.getChildText(CONF_HOLD_GEOMETRIES);
                    }
                }
                numOfEllipseEdges = new Integer(elementNumOfEllipseEdges);

           } catch (Exception ex) {
                log.error("Erring during the configuration of the GeometryOptionsPanel", ex);//NOI18N
            }

            // hier werden die Werte in der GUI gesetzt
            jTextField1.setText(String.valueOf(numOfEllipseEdges));

            stillConfigured = true;
        } else {
            log.debug("skip Configure GeometryOptionsPanel - still configured");//NOI18N
        }

        // Änderungen anwenden
        applyChanges();
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        Element conf = new Element(CONFIGURATION);

        Element holdSearchGeometriesElement = new Element(CONF_HOLD_GEOMETRIES);

        holdSearchGeometriesElement.addContent(Integer.toString(numOfEllipseEdges));

        conf.addContent(holdSearchGeometriesElement);

        return conf;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(GeometryOptionsPanel.class, "GeometryOptionsPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(GeometryOptionsPanel.class, "GeometryOptionsPanel.jLabel2.text")); // NOI18N

        jTextField1.setText(org.openide.util.NbBundle.getMessage(GeometryOptionsPanel.class, "GeometryOptionsPanel.jTextField1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1))
                .addContainerGap(83, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(36, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
