/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Legend.java
 *
 * Created on 16. Februar 2006, 13:59
 */
package de.cismet.cismap.commons.gui.infowidgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import de.cismet.cismap.commons.interaction.ActiveLayerListener;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.raster.wms.SlidableWMSServiceLayerGroup;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.simple.SimpleLegendProvider;
import de.cismet.cismap.commons.rasterservice.ImageRetrieval;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class Legend extends javax.swing.JPanel implements ActiveLayerListener {

    //~ Instance fields --------------------------------------------------------

    private int maxWidth = 0;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private HashMap<String, WMSCapabilities> wmsCapabilities = new HashMap<String, WMSCapabilities>();
    private LegendModel tableModel = new LegendModel();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scpLegends;
    private javax.swing.JTable tblLegends;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form Legend.
     */
    public Legend() {
        initComponents();
        this.setLayout(new BorderLayout());

        tblLegends.setModel(tableModel);
        tblLegends.setTableHeader(null);
        // tblLegends.getColumnModel().getColumn(0).setCellRenderer(new CustomCellRenderer());
        tblLegends.setDefaultRenderer(LegendPanel.class, new CustomCellRenderer());
        tblLegends.setShowHorizontalLines(false);
        tblLegends.setBorder(new EmptyBorder(0, 0, 0, 0));

        // scpLegends
        // scpLegends.setBorder(new EmptyBorder(0,0,0,0));
        tblLegends.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // scpLegends.setSize(this.getSize());
        tblLegends.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // tblLegends.setPreferredScrollableViewportSize(new Dimension(1000,1000));
// tblLegends.revalidate();
        scpLegends = new JScrollPane(tblLegends);
        // scpLegends.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        StaticSwingTools.setNiftyScrollBars(scpLegends);
        add(scpLegends, BorderLayout.CENTER);
        // tblLegends.setPreferredSize(Width(600);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  url        DOCUMENT ME!
     * @param  layername  DOCUMENT ME!
     */
    public void addLegend(final String url, final String layername) {
        // LegendPanel lp = new LegendPanel(url);
        tableModel.addLegend(url, layername);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layername  DOCUMENT ME!
     */
    public void removeLegendByName(final String layername) {
//bizarr
//        tableModel.getAllUrls().remove(url);
//        if (!tableModel.getAllUrls().contains(url)) {
//            tableModel.getAllUrls().add(url);//wird nochmal entfernt
//            tableModel.removeLegend(url);
//        }
        tableModel.removeLegendByName(layername);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  url  DOCUMENT ME!
     */
    public void scrollToLegend(final String url) {
        final int pos = tableModel.getPosition(url);
        if (pos != -1) {
            StaticSwingTools.jTableScrollToVisible(tblLegends, pos, 0);
            if (log.isDebugEnabled()) {
                log.debug(tblLegends.getSelectionModel().getValueIsAdjusting());
            }
            if (!tblLegends.getSelectionModel().isSelectedIndex(pos)) {
                tblLegends.getSelectionModel().setSelectionInterval(pos, pos);
            }
        } else {
            tblLegends.getSelectionModel().clearSelection();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        try {
            org.apache.log4j.PropertyConfigurator.configure(ClassLoader.getSystemResource(
                    "de/cismet/cismap/commons/demo/log4j.properties")); // NOI18N
        } catch (Exception e) {
            e.printStackTrace();
        }

        final JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final Legend l = new Legend();
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(l, BorderLayout.CENTER);
        l.addLegend("http://www.google.de/logos/olympics06_speedskating.gif", "1"); // NOI18N
        l.addLegend("http://www.google.de/logos/olympics06_ski_jump.gif", "2");     // NOI18N
        l.addLegend("http://www.google.de/logos/olympics06_curling.gif", "3");      // NOI18N
        l.addLegend("http://www.google.de/logos/olympics06_speedskating.gif", "4"); // NOI18N
        f.setSize(100, 400);
        f.setVisible(true);
    }

    @Override
    public void layerVisibilityChanged(final ActiveLayerEvent e) {
    }

    @Override
    public void layerRemoved(final ActiveLayerEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("layerRemoved() fired");                                   // NOI18N
        }
        if (e.getLayer() instanceof WMSServiceLayer) {
            removeWmsServiceLayer((WMSServiceLayer)e.getLayer());
        } else if (e.getLayer() instanceof WMSLayer) {
            removeWMSLayer((WMSLayer)e.getLayer());
        } else if (e.getLayer() instanceof SimpleLegendProvider) {
            final SimpleLegendProvider slp = (SimpleLegendProvider)e.getLayer();
            removeLegendByName(slp.getLegendIdentifier());
        } else if (e.getLayer() instanceof SlidableWMSServiceLayerGroup) {
            final SlidableWMSServiceLayerGroup wmsLayer = (SlidableWMSServiceLayerGroup)e.getLayer();
            final List v = wmsLayer.getLayers();
            final Iterator it = v.iterator();
            if (it.hasNext()) {
                final Object elem = it.next();
                if (elem instanceof WMSServiceLayer) {
                    removeWmsServiceLayer((WMSServiceLayer)elem);
                }
            }
        } else {
            log.warn("For this type no legend can be created. " + e.getLayer()); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wmsLayer  DOCUMENT ME!
     */
    private void removeWmsServiceLayer(final WMSServiceLayer wmsLayer) {
        final List v = wmsLayer.getWMSLayers();
        final Iterator it = v.iterator();
        while (it.hasNext()) {
            final Object elem = it.next();
            if (elem instanceof WMSLayer) {
                final WMSLayer wl = (WMSLayer)elem;
                removeWMSLayer(wl);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wl  DOCUMENT ME!
     */
    private void removeWMSLayer(final WMSLayer wl) {
        final String title = wl.getOgcCapabilitiesLayer().getTitle();
        final String name = wl.getOgcCapabilitiesLayer().getName();
        String url = null;
        try {
            final URL[] lua = wl.getSelectedStyle().getLegendURL();
            url = lua[0].toString();
        } catch (final Exception t) {
            if (log.isDebugEnabled()) {
                log.debug("Could not find a legend for " + title, t); // NOI18N
            }
        }
        if (url != null) {
            this.removeLegendByName(name);
        }
    }

    @Override
    public void layerSelectionChanged(final ActiveLayerEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("layerSelectionChanged() fired");                          // NOI18N
        }
        if ((e.getLayer() instanceof WMSLayer) || (e.getLayer() instanceof WMSServiceLayer)
                    || (e.getLayer() instanceof SlidableWMSServiceLayerGroup)) {
            WMSLayer layer = null;
            if (e.getLayer() instanceof WMSLayer) {
                layer = (WMSLayer)e.getLayer();
            } else if ((e.getLayer() instanceof WMSServiceLayer)
                        && (((WMSServiceLayer)e.getLayer()).getWMSLayers().size() == 1)) {
                layer = (WMSLayer)((WMSServiceLayer)e.getLayer()).getWMSLayers().get(0);
            } else if ((e.getLayer() instanceof SlidableWMSServiceLayerGroup)
                        && (((SlidableWMSServiceLayerGroup)e.getLayer()).getLayers().size() > 0)) {
                final WMSServiceLayer sLayer = ((SlidableWMSServiceLayerGroup)e.getLayer()).getLayers().get(0);
                if (sLayer.getWMSLayers().size() == 1) {
                    layer = (WMSLayer)sLayer.getWMSLayers().get(0);
                }
            }
            try {
                scrollToLegend(layer.getSelectedStyle().getLegendURL()[0].toString());
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot scroll to legend of " + e.getLayer(), ex); // NOI18N
                }
            }
        } else if (e.getLayer() instanceof SimpleLegendProvider) {
            final SimpleLegendProvider slp = (SimpleLegendProvider)e.getLayer();
            scrollToLegend(slp.getLegendUrl());
        }
    }

    @Override
    public void layerPositionChanged(final ActiveLayerEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("layerPositionChanged() fired"); // NOI18N
        }
    }

    @Override
    public void layerAdded(final ActiveLayerEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("layerAdded() fired"); // NOI18N
        }

        if (e.getLayer() instanceof WMSServiceLayer) {
            addWmsServiceLayer((WMSServiceLayer)e.getLayer());
        } else if (e.getLayer() instanceof SimpleLegendProvider) {
            final SimpleLegendProvider slp = (SimpleLegendProvider)e.getLayer();
            this.addLegend(slp.getLegendUrl(), slp.getLegendIdentifier());
        } else if (e.getLayer() instanceof SlidableWMSServiceLayerGroup) {
            final SlidableWMSServiceLayerGroup wmsLayer = (SlidableWMSServiceLayerGroup)e.getLayer();
            final List v = wmsLayer.getLayers();
            final Iterator it = v.iterator();
            if (it.hasNext()) {
                final Object elem = it.next();
                if (elem instanceof WMSServiceLayer) {
                    addWmsServiceLayer((WMSServiceLayer)elem);
                }
            }
        } else {
            log.warn("For this type no legend can be created. " + e.getLayer()); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wmsLayer  DOCUMENT ME!
     */
    private void addWmsServiceLayer(final WMSServiceLayer wmsLayer) {
        final List v = wmsLayer.getWMSLayers();
        final Iterator it = v.iterator();
        while (it.hasNext()) {
            final Object elem = it.next();
            if (elem instanceof WMSLayer) {
                final WMSLayer wl = (WMSLayer)elem;
                final String title = wl.getOgcCapabilitiesLayer().getTitle();
                final String name = wl.getOgcCapabilitiesLayer().getName();
                String url = null;
                try {
                    final URL[] lua = wl.getSelectedStyle().getLegendURL();
                    url = lua[0].toString();
                } catch (final Exception t) {
                    if (log.isDebugEnabled()) {
                        log.debug("Could not find legend for " + title, t); // NOI18N
                    }
                }
                if (url != null) {
                    wmsCapabilities.put(url, wmsLayer.getWmsCapabilities());
                    this.addLegend(url, name);
                    if (log.isDebugEnabled()) {
                        log.debug("added legend:" + name + "=" + url);      // NOI18N
                    }
                }
            }
        }
    }

    @Override
    public void layerInformationStatusChanged(final ActiveLayerEvent e) {
        if (e.getLayer() instanceof WMSServiceLayer) {
            refreshWMSServiceLayerInformation((WMSServiceLayer)e.getLayer());
        } else if (e.getLayer() instanceof SlidableWMSServiceLayerGroup) {
            final List<WMSServiceLayer> layer = ((SlidableWMSServiceLayerGroup)e.getLayer()).getLayers();
            final Iterator<WMSServiceLayer> it = layer.iterator();

            if (it.hasNext()) {
                refreshWMSServiceLayerInformation(it.next());
            }
        } else if (e.getLayer() instanceof SimpleLegendProvider) {
            final SimpleLegendProvider slp = (SimpleLegendProvider)e.getLayer();
            tableModel.refreshLegend(slp.getLegendUrl(), slp.getLegendIdentifier());
        } else {
            log.warn("For this type no legend can be created. " + e.getLayer()); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wmsLayer  DOCUMENT ME!
     */
    private void refreshWMSServiceLayerInformation(final WMSServiceLayer wmsLayer) {
        final List v = wmsLayer.getWMSLayers();
        final Iterator it = v.iterator();
        while (it.hasNext()) {
            final Object elem = it.next();
            if (elem instanceof WMSLayer) {
                final WMSLayer wl = (WMSLayer)elem;
                final String title = wl.getOgcCapabilitiesLayer().getTitle();
                final String name = wl.getOgcCapabilitiesLayer().getName();
                String url = null;
                try {
                    final URL[] lua = wl.getSelectedStyle().getLegendURL();
                    url = lua[0].toString();
                } catch (final Exception t) {
                    if (log.isDebugEnabled()) {
                        log.debug("Could not find legend for " + title, t); // NOI18N
                    }
                }
                if (url != null) {
                    wmsCapabilities.put(url, wmsLayer.getWmsCapabilities());

                    tableModel.refreshLegend(url, name);
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        scpLegends = new javax.swing.JScrollPane();
        tblLegends = new javax.swing.JTable();

        scpLegends.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scpLegends.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scpLegends.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scpLegends.setDoubleBuffered(true);
        tblLegends.setBackground(new java.awt.Color(236, 233, 216));
        tblLegends.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                    { null }
                },
                new String[] { "Title 1" }));

        setLayout(new java.awt.BorderLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 5, 1));
    } // </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class LegendPanel extends JPanel implements RetrievalListener {

        //~ Instance fields ----------------------------------------------------

        JLabel lblImage = new JLabel();

        private String url = ""; // NOI18N

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LegendPanel object.
         */
        public LegendPanel() {
            super();
            setBorder(new EmptyBorder(2, 2, 2, 2));

            setLayout(new BorderLayout());

            add(lblImage, BorderLayout.CENTER);
            lblImage.setHorizontalAlignment(JLabel.LEADING);
            lblImage.setVerticalAlignment(JLabel.CENTER);
        }

        /**
         * Creates a new LegendPanel object.
         *
         * @param  url  DOCUMENT ME!
         */
        public LegendPanel(final String url) {
            this();
            this.url = url;
            refresh();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        public final void refresh() {
            final ImageRetrieval ir = new ImageRetrieval(this);
            ir.setUrl(url);
            ir.setWMSCapabilities(wmsCapabilities.get(url));
            ir.setPriority(Thread.NORM_PRIORITY);
            ir.start();
        }

        @Override
        public void retrievalStarted(final RetrievalEvent e) {
            lblImage.setIcon(null);
            lblImage.setText("..."); // NOI18N
            if (maxWidth > 0) {
                tblLegends.getColumnModel().getColumn(0).setPreferredWidth(maxWidth);
            }
            tableModel.fireTableDataChanged();
        }

        @Override
        public void retrievalProgress(final RetrievalEvent e) {
        }

        @Override
        public void retrievalError(final RetrievalEvent e) {
            lblImage.setText("");                     // NOI18N
            log.error("Error while loading legend."); // NOI18N
            tableModel.fireTableDataChanged();
        }

        @Override
        public void retrievalComplete(final RetrievalEvent e) {
            if (e.getRetrievedObject() instanceof Image) {
                final Image image = (Image)e.getRetrievedObject();
                final ImageIcon ii = new ImageIcon(image);
                lblImage.setText(""); // NOI18N
                lblImage.setIcon(ii);
                // setSize(new java.awt.Dimension(image.getWidth(null),image.getHeight(null)));

                tableModel.fireTableDataChanged();
                int newWidth = image.getWidth(null);
                if (newWidth < tblLegends.getPreferredSize().width) {
                    newWidth = tblLegends.getPreferredSize().width;
                } else {
                    maxWidth = newWidth;
                }
                tblLegends.getColumnModel().getColumn(0).setPreferredWidth(newWidth);
//                tblLegends.setSize(newWidth,1000);
//                tblLegends.setPreferredSize(new Dimension(newWidth,(int)tblLegends.getSize().getHeight()+image.getHeight(null)));
//                tblLegends.setPreferredSize(new Dimension(image.getWidth(null),(int)scpLegends.getSize().getHeight()));
//
            }
        }

        @Override
        public void retrievalAborted(final RetrievalEvent e) {
            lblImage.setText(""); // NOI18N
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getUrl() {
            return url;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  url  DOCUMENT ME!
         */
        public void setUrl(final String url) {
            this.url = url;
        }

        @Override
        public boolean equals(final Object tester) {
            if (tester instanceof LegendPanel) {
                final LegendPanel t = (LegendPanel)tester;
                return t.url.equals(url);
            } else {
                return false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CustomCellRenderer implements TableCellRenderer {

        //~ Methods ------------------------------------------------------------

        /**
         * Returns the component used for drawing the cell. This method is used to configure the renderer appropriately
         * before drawing.
         *
         * @param   table       the <code>JTable</code> that is asking the renderer to draw; can be <code>null</code>
         * @param   value       the value of the cell to be rendered. It is up to the specific renderer to interpret and
         *                      draw the value. For example, if <code>value</code> is the string "true", it could be
         *                      rendered as a string or it could be rendered as a check box that is checked. <code>
         *                      null</code> is a valid value
         * @param   isSelected  true if the cell is to be rendered with the selection highlighted; otherwise false
         * @param   hasFocus    if true, render cell appropriately. For example, put a special border on the cell, if
         *                      the cell can be edited, render in the color used to indicate editing
         * @param   row         the row index of the cell being drawn. When drawing the header, the value of <code>
         *                      row</code> is -1
         * @param   column      the column index of the cell being drawn
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public Component getTableCellRendererComponent(final JTable table,
                final Object value,
                final boolean isSelected,
                final boolean hasFocus,
                final int row,
                final int column) {
            final Component component = (Component)value;
            if (isSelected) {
                component.setBackground(Color.white); // javax.swing.UIManager.getDefaults().getColor("Table.highlight"));
                component.setForeground(Legend.this.getForeground());
            } else {
                component.setBackground(Legend.this.getBackground());
                component.setForeground(Legend.this.getForeground());
            }

            if (table.getRowHeight(row) != (int)component.getPreferredSize().getHeight()) {
                table.setRowHeight(row, (int)component.getPreferredSize().getHeight());
            }

            return component;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class LegendModel extends AbstractTableModel {

        //~ Instance fields ----------------------------------------------------

        // private LinkedHashMap<String,LegendPanel> panels=new LinkedHashMap<String,LegendPanel>();

        private List<LegendPanel> panels = new ArrayList<LegendPanel>();
        private HashMap<String, LegendPanel> panelsByName = new HashMap<String, LegendPanel>();
        private HashMap<String, LegendPanel> panelsByUrl = new HashMap<String, LegendPanel>();
        private HashMap<String, String> urlsByName = new HashMap<String, String>();

        //~ Methods ------------------------------------------------------------

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            return LegendPanel.class;
        }

        @Override
        public int getRowCount() {
            return panels.size();
        }

        @Override
        public Object getValueAt(final int row, final int column) {
            return panels.get(panels.size() - 1 - row);
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  url   DOCUMENT ME!
         * @param  name  DOCUMENT ME!
         */
        public void addLegend(final String url, final String name) {
            final LegendPanel lp = new LegendPanel(url);
            urlsByName.put(name, url);
            if (!panels.contains(lp)) {
                panels.add(lp);
                panelsByName.put(name, lp);
                panelsByUrl.put(url, lp);
            }
            super.fireTableStructureChanged();
        }

        /**
         * DOCUMENT ME!
         *
         * @param  url        DOCUMENT ME!
         * @param  layername  DOCUMENT ME!
         */
        public void refreshLegend(final String url, final String layername) {
            final String oldUrl = urlsByName.get(layername);
            if (!oldUrl.equals(url)) {
                urlsByName.put(layername, url);
                final LegendPanel lp = panelsByName.get(layername);
                lp.setUrl(url);
                lp.refresh();
                panelsByUrl.remove(oldUrl);
                panelsByUrl.put(url, lp);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("no Refresh required. Same legend-urls"); // NOI18N
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  layername  DOCUMENT ME!
         */
        public void removeLegendByName(final String layername) {
            final String url = urlsByName.get(layername);
            urlsByName.remove(layername);
            if (!urlsByName.containsValue(url)) {
                final LegendPanel lp = new LegendPanel(url);
                final int index = panels.indexOf(lp);
                if (index != -1) {
                    panels.remove(new LegendPanel(url));
                    panelsByName.remove(layername);
                    panelsByUrl.remove(url);
                    super.fireTableRowsDeleted(index, index);
                }
            }
        }

        @Override
        public boolean isCellEditable(final int row, final int column) {
            return false;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   url  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getPosition(final String url) {
            final LegendPanel lp = panelsByUrl.get(url);

            return panels.size() - 1 - panels.indexOf(lp);
        }
//        public Vector<String> getAllUrls() {
//            return urlsByName.;
//        }
    }
}
