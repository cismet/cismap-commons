/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * FeatureInfoWidget.java
 *
 * Created on 5. April 2006, 10:41
 */
package de.cismet.cismap.commons.gui.featureinfowidget;

import com.jgoodies.looks.Options;

import java.awt.Color;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JTabbedPane;

import de.cismet.cismap.commons.ChildrenProvider;
import de.cismet.cismap.commons.LayerInfoProvider;
import de.cismet.cismap.commons.gui.featureinfowidget.displays.OGCWMSGetFeatureInfoRequestHtmlDisplay;
import de.cismet.cismap.commons.gui.featureinfowidget.displays.TestFeatureInfoDisplay;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.GetFeatureInfoClickDetectionListener;
import de.cismet.cismap.commons.interaction.ActiveLayerListener;
import de.cismet.cismap.commons.interaction.MapClickListener;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.interaction.events.MapClickedEvent;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class FeatureInfoWidget extends javax.swing.JPanel implements ActiveLayerListener, MapClickListener {

    //~ Instance fields --------------------------------------------------------

    HashMap<Object, FeatureInfoDisplay> displays = new HashMap<Object, FeatureInfoDisplay>();
    HashMap<FeatureInfoDisplayKey, Class<? extends FeatureInfoDisplay>> displayRepository =
        new HashMap<FeatureInfoDisplayKey, Class<? extends FeatureInfoDisplay>>();

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JTabbedPane tbpFeatureInfos;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form FeatureInfoWidget.
     */
    public FeatureInfoWidget() {
        initComponents();
        tbpFeatureInfos.putClientProperty(Options.NO_CONTENT_BORDER_KEY, Boolean.FALSE);
        // tbpFeatureInfos.putClientProperty(Options.EMBEDDED_TABS_KEY, Boolean.TRUE);
        tbpFeatureInfos.setRequestFocusEnabled(true);
        tbpFeatureInfos.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // tbpFeatureInfos.setUI(new WindowsTabbedPaneUI());

        displayRepository.put(new FeatureInfoDisplayKey(
                "de.cismet.cismap.commons.raster.wms.WMSLayer",
                FeatureInfoDisplayKey.ANY,
                FeatureInfoDisplayKey.ANY),
            OGCWMSGetFeatureInfoRequestHtmlDisplay.class);
        displayRepository.put(new FeatureInfoDisplayKey(
                "de.cismet.cismap.commons.raster.wms.SlidableWMSServiceLayerGroup",
                FeatureInfoDisplayKey.ANY,
                FeatureInfoDisplayKey.ANY),
            TestFeatureInfoDisplay.class);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void layerVisibilityChanged(final ActiveLayerEvent e) {
    }

    @Override
    public void layerSelectionChanged(final ActiveLayerEvent e) {
        final Object o = e.getLayer();
        if ((o instanceof WMSLayer) && (displays.get(o) != null)) {
            tbpFeatureInfos.setSelectedComponent(displays.get(o));
        } else if ((o instanceof WMSServiceLayer) && (((WMSServiceLayer)o).getWMSLayers().size() == 1)) {
            tbpFeatureInfos.setSelectedComponent(displays.get(((WMSServiceLayer)o).getWMSLayers().get(0)));
        }
    }

    @Override
    public void layerRemoved(final ActiveLayerEvent e) {
        handleInformationStatusChanged(e, true);
    }

    @Override
    public void layerPositionChanged(final ActiveLayerEvent e) {
    }

    @Override
    public void layerInformationStatusChanged(final ActiveLayerEvent e) {
        handleInformationStatusChanged(e, false);
    }

    @Override
    public void layerAdded(final ActiveLayerEvent e) {
        handleInformationStatusChanged(e, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e       DOCUMENT ME!
     * @param  remove  DOCUMENT ME!
     */
    private void handleInformationStatusChanged(final ActiveLayerEvent e, final boolean remove) {
        final Object o = e.getLayer();
        if (o instanceof ChildrenProvider) {
            final Collection c = ((ChildrenProvider)o).getChildren();
            for (final Object childlayer : c) {
                final ActiveLayerEvent ale = new ActiveLayerEvent();
                ale.setLayer(childlayer);
                handleInformationStatusChanged(ale, remove);
            }
        } else if ((o instanceof LayerInfoProvider) && ((LayerInfoProvider)o).isQueryable()) {
            final LayerInfoProvider layer = (LayerInfoProvider)o;
            FeatureInfoDisplay d = displays.get(layer);
            if ((d != null) && ((layer.isLayerQuerySelected() == false) || remove)) {
                try {
                    tbpFeatureInfos.remove(d);
                    displays.remove(layer);
                } catch (Exception ex) {
                    log.warn("Workaround for style changes(there is no refresh, but only remove/add)", ex); // NOI18N I dont understand this
                }
            } else if ((d == null) && layer.isLayerQuerySelected()) {
                try {
                    final Class<? extends FeatureInfoDisplay> dc = getDisplayClass(layer.getClass().getCanonicalName(),
                            layer);
                    d = dc.getConstructor().newInstance();
                    d.init(layer, tbpFeatureInfos);
                    tbpFeatureInfos.add(layer.toString(), d);
                    displays.put(layer, d);
                } catch (Exception exception) {
                    log.error("Exception in creating featureInfoDisplay component", exception);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   layerclass  DOCUMENT ME!
     * @param   layerinfo   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Class<? extends FeatureInfoDisplay> getDisplayClass(final String layerclass,
            final LayerInfoProvider layerinfo) {
        final String server = layerinfo.getServerURI();
        final String layer = layerinfo.getLayerURI();

        Class<? extends FeatureInfoDisplay> c = null;
        c = displayRepository.get(new FeatureInfoDisplayKey(layerclass, server, layer));
        if (c == null) {
            c = displayRepository.get(new FeatureInfoDisplayKey(layerclass, server, FeatureInfoDisplayKey.ANY));
        }
        if (c == null) {
            c = displayRepository.get(new FeatureInfoDisplayKey(
                        layerclass,
                        FeatureInfoDisplayKey.ANY,
                        FeatureInfoDisplayKey.ANY));
        }

        if (c == null) {
            // Error INfoDisplay oder STandardInfoDisplay
        }
        return c;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jButton1 = new javax.swing.JButton();
        tbpFeatureInfos = new javax.swing.JTabbedPane();

        jButton1.setText(null);

        tbpFeatureInfos.addChangeListener(new javax.swing.event.ChangeListener() {

                @Override
                public void stateChanged(final javax.swing.event.ChangeEvent evt) {
                    tbpFeatureInfosStateChanged(evt);
                }
            });

        final org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                tbpFeatureInfos,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                400,
                Short.MAX_VALUE));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
                org.jdesktop.layout.GroupLayout.TRAILING,
                tbpFeatureInfos,
                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                300,
                Short.MAX_VALUE));
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void tbpFeatureInfosStateChanged(final javax.swing.event.ChangeEvent evt) { //GEN-FIRST:event_tbpFeatureInfosStateChanged
        for (int i = 0; i < tbpFeatureInfos.getTabCount(); ++i) {
            tbpFeatureInfos.setForegroundAt(i, null);
        }

        final int selectedindex = tbpFeatureInfos.getSelectedIndex();
        if ((selectedindex >= 0) && (tbpFeatureInfos.getTabCount() != 0)) {
            try {
                tbpFeatureInfos.setForegroundAt(selectedindex, Color.blue);
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Error. Feeling not blue.", ex);
                }
            }
        }
    } //GEN-LAST:event_tbpFeatureInfosStateChanged

    @Override
    public void clickedOnMap(final MapClickedEvent mce) {
        if (mce.getMode().equals(GetFeatureInfoClickDetectionListener.FEATURE_INFO_MODE)) {
            final Set layers = displays.keySet();
            for (final Object layer : layers) {
                displays.get(layer).showFeatureInfo(mce);
            }
        }
    }
}
