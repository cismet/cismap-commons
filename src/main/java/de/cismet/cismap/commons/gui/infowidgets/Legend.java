/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.infowidgets;

import org.openide.util.NbBundle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Image;

import java.net.URL;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import de.cismet.cismap.commons.interaction.ActiveLayerListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.StatusListener;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.interaction.events.StatusEvent;
import de.cismet.cismap.commons.raster.wms.SlidableWMSServiceLayerGroup;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.simple.SimpleLegendProvider;
import de.cismet.cismap.commons.rasterservice.ImageRetrieval;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

import de.cismet.commons.wms.capabilities.WMSCapabilities;

import de.cismet.tools.Static2DTools;

import de.cismet.tools.gui.GUIWindow;
import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = GUIWindow.class)
public class Legend extends javax.swing.JPanel implements ActiveLayerListener, StatusListener, GUIWindow {

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
        tblLegends.setDefaultRenderer(LegendPanel.class, new CustomCellRenderer());
        tblLegends.setShowHorizontalLines(false);
        tblLegends.setBorder(new EmptyBorder(0, 0, 0, 0));

        tblLegends.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblLegends.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        scpLegends = new JScrollPane(tblLegends);
        StaticSwingTools.setNiftyScrollBars(scpLegends);
        add(scpLegends, BorderLayout.CENTER);
        CismapBroker.getInstance().addActiveLayerListener(this);
        CismapBroker.getInstance().addStatusListener(this);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  url        DOCUMENT ME!
     * @param  layername  DOCUMENT ME!
     */
    public void addLegend(final String url, final String layername) {
        tableModel.addLegend(url, layername);
    }

    /*public void addLegend(final BufferedImage img, final String layername) {
     *  tableModel.addLegend(img, layername);}*/

    /*public void addLegend(final BufferedImage img, final String layername) {
     *  tableModel.addLegend(img, layername);}*/

    /*public void addLegend(final BufferedImage img, final String layername) {
     *  tableModel.addLegend(img, layername);}*/

    /*public void addLegend(final BufferedImage img, final String layername) {
     *  tableModel.addLegend(img, layername);}*/

    /*public void addLegend(final BufferedImage img, final String layername) {
     *  tableModel.addLegend(img, layername);}*/

    /*public void addLegend(final BufferedImage img, final String layername) {
     *  tableModel.addLegend(img, layername);}*/

    /*public void addLegend(final BufferedImage img, final String layername) {
     *  tableModel.addLegend(img, layername);}*/

    /*public void addLegend(final BufferedImage img, final String layername) {
     *  tableModel.addLegend(img, layername);}*/

    /**
     * DOCUMENT ME!
     *
     * @param  layerurl  DOCUMENT ME!
     * @param  name      DOCUMENT ME!
     */
    public void removeLegendByUrl(final String layerurl, final String name) {
        tableModel.removeLegendByUrl(layerurl, name);
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

    @Override
    public void layerVisibilityChanged(final ActiveLayerEvent e) {
    }

    @Override
    public void layerAvailabilityChanged(final ActiveLayerEvent e) {
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
            removeLegendByUrl(slp.getLegendUrl(), slp.getLegendIdentifier());
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
        }                                                                        /*else if(e.getLayer() instanceof
                                                                                  * ServiceLayer) {
                                                                                  * removeLegendByName(((ServiceLayer)e.getLayer()).getName());}*/
        else {
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
        assert (wl != null);
        if (wl.getOgcCapabilitiesLayer() == null) {
            if (log.isDebugEnabled()) {
                log.debug(
                    "in removeWMSLayer waren die capabilities null. kann die Legende nicht entferenen. Wahrscheinlich war deshalb auch gar keine drin. ");
            }
        } else {
            final String title = wl.getOgcCapabilitiesLayer().getTitle();
            final String name = wl.getOgcCapabilitiesLayer().getName();
            String url = null;
            try {
                final URL[] lua = wl.getSelectedStyle().getLegendURL();
                url = getValidUrlString(lua[0]);
            } catch (final Exception t) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not find a legend for " + title, t); // NOI18N
                }
            }
            if (url != null) {
                this.removeLegendByUrl(url, name);
            }
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
                if (!layer.isDummy()) {
                    scrollToLegend(getValidUrlString(layer.getSelectedStyle().getLegendURL()[0]));
                }
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
        }                                                                        /*else if (e.getLayer() instanceof
                                                                                  * SLDStyledLayer) { final
                                                                                  * SLDStyledLayer sldLayer =
                                                                                  * (SLDStyledLayer) e.getLayer();
                                                                                  * Pair<Integer, Integer> size =
                                                                                  * sldLayer.getLegendSize();
                                                                                  * BufferedImage legendImage = new
                                                                                  * BufferedImage(size.first,
                                                                                  * size.second,
                                                                                  * BufferedImage.TYPE_4BYTE_ABGR);
                                                                                  * sldLayer.getLegend(legendImage.getWidth(),
                                                                                  * legendImage.getHeight(),
                                                                                  * legendImage.createGraphics());
                                                                                  * addLegend(legendImage,
                                                                                  * ((ServiceLayer)e.getLayer()).getName());}
                                                                                  */
        else {
            log.warn("For this type no legend can be created. " + e.getLayer()); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wmsLayer  DOCUMENT ME!
     */
    private void addWmsServiceLayer(final WMSServiceLayer wmsLayer) {
        if (!wmsLayer.isDummy()) {
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
                        url = getValidUrlString(lua[0]);
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
    }

    /**
     * The legend url can contains umlaute and colons, which cannot be handled by the ImageRetrieval class. So this
     * characters should be encoded
     *
     * @param   url  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private String getValidUrlString(final URL url) throws Exception {
        String urlString = null;

        try {
            urlString = url.toURI().toASCIIString();
        } catch (final Exception t) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot convert legend url to ascii string", t); // NOI18N
            }
            urlString = url.toString();

            if (urlString.contains("?")) {
                final String param = urlString.substring(urlString.indexOf("?") + 1);
                urlString = urlString.substring(0, urlString.indexOf("?")) + "?";
                final StringTokenizer stParam = new StringTokenizer(param, "&");

                while (stParam.hasMoreTokens()) {
                    final StringTokenizer stKeyVal = new StringTokenizer(stParam.nextToken(), "=");

                    if (stKeyVal.countTokens() == 2) {
                        urlString += "&" + stKeyVal.nextToken() + "="
                                    + URLEncoder.encode(stKeyVal.nextToken(), "UTF-8");
                    }
                }
            }
        }

        return urlString;
    }

    @Override
    public void layerInformationStatusChanged(final ActiveLayerEvent e) {
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

    @Override
    public void statusValueChanged(final StatusEvent e) {
        if (e.getName().equals(StatusEvent.AWAKED_FROM_DUMMY)) {
            if (e.getValue() instanceof WMSServiceLayer) {
                addWmsServiceLayer((WMSServiceLayer)e.getValue());
            }
        }
    }

    @Override
    public JComponent getGuiComponent() {
        return this;
    }

    @Override
    public String getPermissionString() {
        return GUIWindow.NO_PERMISSION;
    }

    @Override
    public String getViewTitle() {
        return NbBundle.getMessage(Legend.class, "Legend.getViewTitle");
    }

    @Override
    public Icon getViewIcon() {
        final Icon icoMap = new ImageIcon(getClass().getResource("/de/cismet/cismap/navigatorplugin/res/legend.png"));
        return Static2DTools.borderIcon(icoMap, 0, 3, 0, 1);
    }

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

        /*public LegendPanel(final BufferedImage img) {
         *  this(); lblImage.setText(""); // NOI18N lblImage.setIcon(new ImageIcon(img));
         * tableModel.fireTableDataChanged(); int newWidth = img.getWidth(null); if (newWidth <
         * tblLegends.getPreferredSize().width) {     newWidth = tblLegends.getPreferredSize().width; } else {
         * maxWidth = newWidth; } tblLegends.getColumnModel().getColumn(0).setPreferredWidth(newWidth);}*/

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  r  DOCUMENT ME!
         */
        private void dispatch(final Runnable r) {
            if (EventQueue.isDispatchThread()) {
                r.run();
            } else {
                EventQueue.invokeLater(r);
            }
        }

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
            final Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        lblImage.setIcon(null);
                        lblImage.setText("..."); // NOI18N
                        if (maxWidth > 0) {
                            tblLegends.getColumnModel().getColumn(0).setPreferredWidth(maxWidth);
                        }
                        tableModel.fireTableDataChanged();
                    }
                };

            dispatch(r);
        }

        @Override
        public void retrievalProgress(final RetrievalEvent e) {
        }

        @Override
        public void retrievalError(final RetrievalEvent e) {
            final Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        lblImage.setText("");                     // NOI18N
                        log.error("Error while loading legend."); // NOI18N
                        tableModel.fireTableDataChanged();
                    }
                };

            dispatch(r);
        }

        @Override
        public void retrievalComplete(final RetrievalEvent e) {
            if (e.getRetrievedObject() instanceof Image) {
                final Image image = (Image)e.getRetrievedObject();
                final ImageIcon ii = new ImageIcon(image);

                final Runnable r = new Runnable() {

                        @Override
                        public void run() {
                            lblImage.setText(""); // NOI18N
                            lblImage.setIcon(ii);

                            tableModel.fireTableDataChanged();
                            int newWidth = image.getWidth(null) + 10; // because of the border and stuff of the JTable
                            if (newWidth < tblLegends.getPreferredSize().width) {
                                newWidth = tblLegends.getPreferredSize().width;
                            } else {
                                maxWidth = newWidth;
                            }
                            tblLegends.getColumnModel().getColumn(0).setPreferredWidth(newWidth);
                        }
                    };

                dispatch(r);
            }
        }

        @Override
        public void retrievalAborted(final RetrievalEvent e) {
            final Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        lblImage.setText(""); // NOI18N
                    }
                };

            dispatch(r);
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

        private List<LegendPanel> panels = new ArrayList<LegendPanel>();
        private HashMap<String, LegendPanel> panelsByUrl = new HashMap<String, LegendPanel>();
        private HashMap<String, TreeSet<String>> urlByLayername = new HashMap<>();

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
            if (!panels.contains(lp)) {
                panels.add(lp);
                panelsByUrl.put(url, lp);
            }

            final TreeSet<String> allLayers = urlByLayername.get(url);

            if (allLayers == null) {
                final TreeSet<String> names = new TreeSet<>();
                names.add(name);
                urlByLayername.put(url, names);
            } else {
                allLayers.add(name);
            }

            if (EventQueue.isDispatchThread()) {
                fireTableStructureChanged();
            } else {
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            LegendModel.super.fireTableStructureChanged();
                        }
                    });
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  url   layerurl DOCUMENT ME!
         * @param  name  DOCUMENT ME!
         */
        public void removeLegendByUrl(final String url, final String name) {
            if (panelsByUrl.containsKey(url)) {
                final LegendPanel lp = new LegendPanel(url);
                if (panels.contains(lp)) {
                    final TreeSet<String> names = urlByLayername.get(url);
                    boolean removeUrl = true;

                    if (names != null) {
                        names.remove(name);

                        if (names.size() > 0) {
                            removeUrl = false;
                        } else {
                            urlByLayername.remove(url);
                        }
                    }

                    if (removeUrl) {
                        final int index = panels.indexOf(lp);
                        panels.remove(lp);
                        panelsByUrl.remove(url);
                        super.fireTableRowsDeleted(index, index);
                    }
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
    }
}
