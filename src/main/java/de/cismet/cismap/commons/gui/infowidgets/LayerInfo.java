/*
 * LayerInfo.java
 *
 * Created on 16. Februar 2006, 13:58
 */
package de.cismet.cismap.commons.gui.infowidgets;

import de.cismet.cismap.commons.interaction.ActiveLayerListener;
import de.cismet.cismap.commons.interaction.CapabilityListener;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.interaction.events.CapabilityEvent;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.tools.gui.StaticSwingTools;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import org.deegree.services.wms.capabilities.Layer;

/**
 *
 * @author  thorsten.hell@cismet.de
 */
public class LayerInfo extends javax.swing.JPanel implements CapabilityListener, ActiveLayerListener {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private boolean splitPaneInitialized=false;
    /** Creates new form LayerInfo */
    public LayerInfo() {
        initComponents();
        sppMain.setDividerLocation(0.75d);

    }

    public void serverChanged(CapabilityEvent e) {
    }

    public void initDividerLocation() {
        sppMain.setDividerLocation(0.75d);
    }
    
    public void layerChanged(CapabilityEvent e) {
        if (e.getCapabilityObject() instanceof Layer) {
            setValues((Layer) e.getCapabilityObject());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        panMain = new javax.swing.JPanel();
        sppMain = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        scpAbstract = new javax.swing.JScrollPane();
        txtAbstract = new javax.swing.JTextArea();
        lblName = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        lblFeatureInfo = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstSrs = new javax.swing.JList();

        jLabel1.setText(null);

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());

        panMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panMain.setLayout(new java.awt.BorderLayout());

        sppMain.setDividerLocation(200);
        sppMain.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        sppMain.setResizeWeight(0.5);

        jPanel1.setLayout(new java.awt.BorderLayout());

        scpAbstract.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        scpAbstract.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        txtAbstract.setBackground(panMain.getBackground());
        txtAbstract.setColumns(20);
        txtAbstract.setLineWrap(true);
        txtAbstract.setRows(3);
        txtAbstract.setWrapStyleWord(true);
        scpAbstract.setViewportView(txtAbstract);

        jPanel1.add(scpAbstract, java.awt.BorderLayout.CENTER);

        lblName.setFont(new java.awt.Font("Tahoma", 1, 14));
        lblName.setText(null);
        jPanel1.add(lblName, java.awt.BorderLayout.PAGE_START);

        sppMain.setTopComponent(jPanel1);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel3.setText(null);

        lblFeatureInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/layerInfo.png"))); // NOI18N
        lblFeatureInfo.setText(null);

        lstSrs.setBackground(panMain.getBackground());
        jScrollPane2.setViewportView(lstSrs);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 418, Short.MAX_VALUE)
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                        .add(jPanel2Layout.createSequentialGroup()
                            .add(lblFeatureInfo)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1582, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE))
                    .addContainerGap()))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 226, Short.MAX_VALUE)
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(lblFeatureInfo)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(jLabel3)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        sppMain.setRightComponent(jPanel2);

        panMain.add(sppMain, java.awt.BorderLayout.CENTER);

        add(panMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
           
    }//GEN-LAST:event_formComponentShown

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized

    }//GEN-LAST:event_formComponentResized

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblFeatureInfo;
    private javax.swing.JLabel lblName;
    private javax.swing.JList lstSrs;
    private javax.swing.JPanel panMain;
    private javax.swing.JScrollPane scpAbstract;
    private javax.swing.JSplitPane sppMain;
    private javax.swing.JTextArea txtAbstract;
    // End of variables declaration//GEN-END:variables
    private void setValues(Layer layer) {
        Values v = new Values();
        try {
            v.title = layer.getTitle();
        } catch (Exception e) {
        }
        try {
            v.name = layer.getName();
        } catch (Exception e) {
        }

        try {
            v.description = layer.getAbstract();
        } catch (Exception e) {
        }

        try {
            v.featureInfo = layer.isQueryable();
        } catch (Exception e) {
        }

        try {
            v.srs = layer.getSrs();
        } catch (Exception e) {
        }

        setValues(v);
    }

    private void setValues(Values v) {
        if (v.title != null) {
            lblName.setText(v.title);
        } else {
            lblName.setText("-");//NOI18N
        }
        if (v.name != null) {
            lblName.setToolTipText(v.name);
        } else {
            lblName.setToolTipText(null);
        }

        if (v.description != null && v.description.length() > 0) {
            scpAbstract.setVisible(true);
            String s=v.description;
            txtAbstract.setText(getGDILanguageString(v.description));
        } else {
            scpAbstract.setVisible(false);
        }

        lblFeatureInfo.setEnabled(v.featureInfo);

        if (v.srs != null && v.srs.length > 0) {
            lstSrs.setVisible(true);
            lstSrs.setModel(new DefaultComboBoxModel(v.srs));
        } else {
            lstSrs.setVisible(false);
        }
    }

    public void layerVisibilityChanged(ActiveLayerEvent e) {
    }

    public void layerSelectionChanged(ActiveLayerEvent e) {

        if (e.getLayer() instanceof WMSServiceLayer) {

            Vector v = ((WMSServiceLayer) e.getLayer()).getWMSLayers();
            if (v.size() == 1) {
                Object o = v.get(0);
                if (o instanceof WMSLayer) {
                    setValues(((WMSLayer) o).getOgcCapabilitiesLayer());
                }
            }

        }
    }

    public void layerRemoved(ActiveLayerEvent e) {
    }

    public void layerPositionChanged(ActiveLayerEvent e) {
    }

    public void layerInformationStatusChanged(ActiveLayerEvent e) {
    }

    public void layerAdded(ActiveLayerEvent e) {
    }

    class Values {

        String title;
        String name;
        String description;
        boolean featureInfo;
        String[] srs;
    }
    private static final String DE = "--DE";//NOI18N
    private static final String EN = "--EN";//NOI18N
    
    private static final String[] LANGUAGES={DE,EN};

    public String getGDILanguageString(String text) {
        String ret="";//NOI18N
        try {
            for (String lan:LANGUAGES){
                if (text.contains(lan)){
                    int from=text.indexOf(lan);
                    int to=text.indexOf("--", from+lan.length());//NOI18N
                    if (from<0) {
                        break;
                    }
                    if (to<0) {
                        to=text.length();
                    }
                    ret=text.substring(from+lan.length(),to);
                    return ret.trim();
                }
            }
            return text;
        } catch (Exception e) {
            log.warn("Error while checking the GDI String", e);//NOI18N
            return text;
        
        }
    }
    
//    public static void main(String[] args) {
//        String s="--DE Inhalt: Orthofotomosaik fuer das Stadtgebiet von Wuppertal, Bildflug vom 13.03.2007, hergestellt durch Hansa Luftbild AG / Muenster. Sichtbarkeit: oeffentlich, Detailzooms nur fuer interne und autorisierte externe Nutzer. Nutzung: Detailzooms fuer externe Nutzer i. A. kostenpflichtig nach Abschluss eines schriftlichen Nutzungsrechtsvertrages.--EN This is english";
//        System.out.println(new LayerInfo().getGDILanguageString(s));
//    }

}
