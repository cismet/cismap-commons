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

import org.apache.log4j.Logger;

import java.awt.Color;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import de.cismet.cismap.commons.ChildrenProvider;
import de.cismet.cismap.commons.LayerInfoProvider;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.GetFeatureInfoClickDetectionListener;
import de.cismet.cismap.commons.interaction.ActiveLayerListener;
import de.cismet.cismap.commons.interaction.MapClickListener;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.interaction.events.MapClickedEvent;
import de.cismet.cismap.commons.raster.wms.SlidableWMSServiceLayerGroup;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class FeatureInfoWidget extends JPanel implements ActiveLayerListener, MapClickListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(FeatureInfoWidget.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Map<Object, FeatureInfoDisplay> displays;
    private final transient FeatureInfoDisplayRepository displayRepo;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JTabbedPane tbpFeatureInfos;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form FeatureInfoWidget.
     */
    public FeatureInfoWidget() {
        displays = new HashMap<Object, FeatureInfoDisplay>();
        displayRepo = new FeatureInfoDisplayRepository();

        initComponents();

        tbpFeatureInfos.putClientProperty(Options.NO_CONTENT_BORDER_KEY, Boolean.FALSE);
        tbpFeatureInfos.setRequestFocusEnabled(true);
        tbpFeatureInfos.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void layerVisibilityChanged(final ActiveLayerEvent e) {
        // noop
    }

    @Override
    public void layerSelectionChanged(final ActiveLayerEvent e) {
        final Object o = e.getLayer();
        if ((o instanceof WMSLayer) && (displays.get(o) != null)) {
            tbpFeatureInfos.setSelectedComponent(displays.get(o).getDisplayComponent());
        } else if ((o instanceof WMSServiceLayer) && (((WMSServiceLayer)o).getWMSLayers().size() == 1)) {
            final FeatureInfoDisplay displ = displays.get(((WMSServiceLayer)o).getWMSLayers().get(0));

            if (displ != null) {
                tbpFeatureInfos.setSelectedComponent(displ.getDisplayComponent());
            }
        } else if ((o instanceof SlidableWMSServiceLayerGroup) && (displays.get(o) != null)) {
            tbpFeatureInfos.setSelectedComponent(displays.get(o).getDisplayComponent());
        }
    }

    @Override
    public void layerRemoved(final ActiveLayerEvent e) {
        handleInformationStatusChanged(e, true);
    }

    @Override
    public void layerPositionChanged(final ActiveLayerEvent e) {
        // noop
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
            FeatureInfoDisplay display = displays.get(layer);
            if ((display != null) && ((layer.isLayerQuerySelected() == false) || remove)) {
                try {
                    tbpFeatureInfos.remove(display.getDisplayComponent());
                    displays.remove(layer);
                } catch (Exception ex) {
                    LOG.warn("Workaround for style changes(there is no refresh, but only remove/add)", ex); // NOI18N I dont understand this
                }
            } else if ((display == null) && layer.isLayerQuerySelected()) {
                try {
                    display = displayRepo.getDisplayClass(layer.getClass(), layer);
                    if (display == null) {
                        // TODO: use default display? or should even a default be delivered by the repo?
                        throw new IllegalStateException("dispay info for layer is null: " + layer); // NOI18N
                    }
                    display.init(layer, tbpFeatureInfos);
                    tbpFeatureInfos.add(layer.toString(), display.getDisplayComponent());
                    displays.put(layer, display);
                } catch (final Exception exception) {
                    LOG.error("Exception in creating featureInfoDisplay component", exception);     // NOI18N
                    layer.setLayerQuerySelected(false);
                }
            }
        }
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
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Error. Feeling not blue.", ex); // NOI18N
                }
            }
        }
    }                                                          //GEN-LAST:event_tbpFeatureInfosStateChanged

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
