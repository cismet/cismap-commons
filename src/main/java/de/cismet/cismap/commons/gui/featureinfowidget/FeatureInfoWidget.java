/*
 * FeatureInfoWidget.java
 *
 * Created on 5. April 2006, 10:41
 */
package de.cismet.cismap.commons.gui.featureinfowidget;

import com.jgoodies.looks.Options;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.GetFeatureInfoClickDetectionListener;
import de.cismet.cismap.commons.interaction.ActiveLayerListener;
import de.cismet.cismap.commons.interaction.MapClickListener;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.interaction.events.MapClickedEvent;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.tools.CurrentStackTrace;
import java.awt.Color;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import javax.swing.JTabbedPane;

/**
 *
 * @author  thorsten.hell@cismet.de
 */
public class FeatureInfoWidget extends javax.swing.JPanel implements ActiveLayerListener, MapClickListener {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    HashMap<WMSLayer, FeatureInfoDisplay> displays = new HashMap<WMSLayer, FeatureInfoDisplay>();

    /** Creates new form FeatureInfoWidget */
    public FeatureInfoWidget() {

        initComponents();
        tbpFeatureInfos.putClientProperty(Options.NO_CONTENT_BORDER_KEY, Boolean.FALSE);
        //tbpFeatureInfos.putClientProperty(Options.EMBEDDED_TABS_KEY, Boolean.TRUE);
        tbpFeatureInfos.setRequestFocusEnabled(true);
        tbpFeatureInfos.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        //tbpFeatureInfos.setUI(new WindowsTabbedPaneUI());

    }

    public void layerVisibilityChanged(ActiveLayerEvent e) {
    }

    public void layerSelectionChanged(ActiveLayerEvent e) {
        Object o = e.getLayer();
        if (o instanceof WMSLayer && displays.get(o) != null) {
            tbpFeatureInfos.setSelectedComponent(displays.get(o));

        } else if (o instanceof WMSServiceLayer && ((WMSServiceLayer) o).getWMSLayers().size() == 1) {
            tbpFeatureInfos.setSelectedComponent(displays.get(((WMSServiceLayer) o).getWMSLayers().get(0)));
        }


    }

    public void layerRemoved(ActiveLayerEvent e) {
        handleInformationStatusChanged(e, true);

    }

    public void layerPositionChanged(ActiveLayerEvent e) {
    }

    public void layerInformationStatusChanged(ActiveLayerEvent e) {
        handleInformationStatusChanged(e, false);
    }

    public void layerAdded(ActiveLayerEvent e) {
        handleInformationStatusChanged(e, false);
    }

    private void handleInformationStatusChanged(ActiveLayerEvent e, final boolean remove) {
        final Object o = e.getLayer();
        if (o instanceof WMSLayer) {
            WMSLayer layer = (WMSLayer) o;
            FeatureInfoDisplay d = displays.get(layer);
            if (d != null && (layer.isQuerySelected() == false || remove)) {
                try {
                    tbpFeatureInfos.remove(d);
                    displays.remove(layer);
                } catch (Exception ex) {
                    log.warn("Workaround for style changes(there is no refresh, but only remove/add)", ex);//NOI18N I dont understand this
                }
            } else if (d == null && layer.isQuerySelected()) {
                d = new FeatureInfoDisplay(layer, tbpFeatureInfos);
                tbpFeatureInfos.add(layer.toString(), d);
                displays.put(layer, d);
            }

        } else if (o instanceof WMSServiceLayer) {
            WMSServiceLayer serviceLayer = (WMSServiceLayer) o;
            Vector<WMSLayer> v = serviceLayer.getWMSLayers();
            for (WMSLayer elem : v) {
                ActiveLayerEvent ale = new ActiveLayerEvent();
                ale.setLayer(elem);
                handleInformationStatusChanged(ale, remove);
            }
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jButton1 = new javax.swing.JButton();
        tbpFeatureInfos = new javax.swing.JTabbedPane();

        jButton1.setText(null);

        tbpFeatureInfos.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tbpFeatureInfosStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tbpFeatureInfos, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, tbpFeatureInfos, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tbpFeatureInfosStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tbpFeatureInfosStateChanged
        for (int i = 0; i < tbpFeatureInfos.getTabCount(); ++i) {
            tbpFeatureInfos.setForegroundAt(i, null);
        }

        int selectedindex = tbpFeatureInfos.getSelectedIndex();
        try {
            tbpFeatureInfos.setForegroundAt(selectedindex, Color.blue);
        } catch (Exception ex) {
            log.debug("Error. Feeling not blue.", ex);
        }



    }//GEN-LAST:event_tbpFeatureInfosStateChanged

    public void getFeatureInfo(int x, int y) {
        Set<WMSLayer> layers = displays.keySet();
        for (WMSLayer l : layers) {
            displays.get(l).showContent(x, y);

        }

    }

    public void clickedOnMap(MapClickedEvent mce) {
        if (mce.getMode().equals(GetFeatureInfoClickDetectionListener.FEATURE_INFO_MODE)) {
            getFeatureInfo((int) mce.getX(), (int) mce.getY());
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JTabbedPane tbpFeatureInfos;
    // End of variables declaration//GEN-END:variables
}
