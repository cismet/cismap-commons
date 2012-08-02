/*
 * Legend.java
 *
 * Created on 16. Februar 2006, 13:59
 */
package de.cismet.cismap.commons.gui.infowidgets;

import de.cismet.cismap.commons.interaction.ActiveLayerListener;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.simple.SimpleLegendProvider;
import de.cismet.cismap.commons.rasterservice.ImageRetrieval;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.tools.gui.StaticSwingTools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
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
import org.deegree.services.wms.capabilities.LegendURL;
import org.deegree.services.wms.capabilities.WMSCapabilities;

/**
 *
 * @author  thorsten.hell@cismet.de
 */
public class Legend extends javax.swing.JPanel implements ActiveLayerListener {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private HashMap<String, WMSCapabilities> wmsCapabilities = new HashMap<String, WMSCapabilities>();
    private LegendModel tableModel = new LegendModel();

    /** Creates new form Legend */
    public Legend() {
        initComponents();
        this.setLayout(new BorderLayout());

        tblLegends.setModel(tableModel);
        tblLegends.setTableHeader(null);
        //tblLegends.getColumnModel().getColumn(0).setCellRenderer(new CustomCellRenderer());
        tblLegends.setDefaultRenderer(LegendPanel.class, new CustomCellRenderer());
        tblLegends.setShowHorizontalLines(false);
        tblLegends.setBorder(new EmptyBorder(0, 0, 0, 0));

        //scpLegends
        //scpLegends.setBorder(new EmptyBorder(0,0,0,0));
        tblLegends.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //scpLegends.setSize(this.getSize());
        tblLegends.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        //    tblLegends.setPreferredScrollableViewportSize(new Dimension(1000,1000));
//        tblLegends.revalidate();
        scpLegends = new JScrollPane(tblLegends);
        //scpLegends.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        StaticSwingTools.setNiftyScrollBars(scpLegends);
        add(scpLegends, BorderLayout.CENTER);
        //tblLegends.setPreferredSize(Width(600);


    }

    public void addLegend(String url, String layername) {
        //LegendPanel lp = new LegendPanel(url);
        tableModel.addLegend(url, layername);
    }

    public void removeLegendByName(String layername) {
//bizarr
//        tableModel.getAllUrls().remove(url);
//        if (!tableModel.getAllUrls().contains(url)) {
//            tableModel.getAllUrls().add(url);//wird nochmal entfernt
//            tableModel.removeLegend(url);
//        }
        tableModel.removeLegendByName(layername);
    }

    public void scrollToLegend(String url) {
        int pos = tableModel.getPosition(url);
        if (pos != -1) {
            StaticSwingTools.jTableScrollToVisible(tblLegends, pos, 0);
            log.debug(tblLegends.getSelectionModel().getValueIsAdjusting());
            if (!tblLegends.getSelectionModel().isSelectedIndex(pos)) {
                tblLegends.getSelectionModel().setSelectionInterval(pos, pos);
            }
        } else {
            tblLegends.getSelectionModel().clearSelection();
        }
    }

    public static void main(String[] args) {
        try {
            org.apache.log4j.PropertyConfigurator.configure(ClassLoader.getSystemResource("de/cismet/cismap/commons/demo/log4j.properties"));//NOI18N
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Legend l = new Legend();
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(l, BorderLayout.CENTER);
        l.addLegend("http://www.google.de/logos/olympics06_speedskating.gif", "1");//NOI18N
        l.addLegend("http://www.google.de/logos/olympics06_ski_jump.gif", "2");//NOI18N
        l.addLegend("http://www.google.de/logos/olympics06_curling.gif", "3");//NOI18N
        l.addLegend("http://www.google.de/logos/olympics06_speedskating.gif", "4");//NOI18N
        f.setSize(100, 400);
        f.setVisible(true);

    }

    public void layerVisibilityChanged(ActiveLayerEvent e) {
    }

    public void layerRemoved(ActiveLayerEvent e) {
        log.debug("layerRemoved() fired");//NOI18N
        if (e.getLayer() instanceof WMSServiceLayer) {
            WMSServiceLayer wmsLayer = (WMSServiceLayer) e.getLayer();
            Vector v = wmsLayer.getWMSLayers();
            Iterator it = v.iterator();
            while (it.hasNext()) {
                Object elem = (Object) it.next();
                if (elem instanceof WMSLayer) {
                    WMSLayer wl = (WMSLayer) elem;
                    removeWMSLayer(wl);
                }
            }
        } else if (e.getLayer() instanceof WMSLayer) {
            removeWMSLayer((WMSLayer) e.getLayer());
        } else if (e.getLayer() instanceof SimpleLegendProvider) {
            SimpleLegendProvider slp = (SimpleLegendProvider) e.getLayer();
            removeLegendByName(slp.getLegendIdentifier());

        } else {
            log.warn("For this type no legend can be created. " + e.getLayer());//NOI18N
        }
    }

    private void removeWMSLayer(WMSLayer wl) {
        String title = wl.getOgcCapabilitiesLayer().getTitle();
        String name = wl.getOgcCapabilitiesLayer().getName();
        String url = null;
        try {
            LegendURL[] lua = wl.getSelectedStyle().getLegendURL();
            url = lua[0].getOnlineResource().toString();
        } catch (Throwable t) {
            log.debug("Could not find a legend for " + title, t);//NOI18N
        }
        if (url != null) {
            this.removeLegendByName(name);
        }
    }

    public void layerSelectionChanged(ActiveLayerEvent e) {
        log.debug("layerSelectionChanged() fired");//NOI18N
        if (e.getLayer() instanceof WMSLayer || e.getLayer() instanceof WMSServiceLayer) {
            WMSLayer layer = null;
            if (e.getLayer() instanceof WMSLayer) {
                layer = (WMSLayer) e.getLayer();
            } else if (e.getLayer() instanceof WMSServiceLayer && ((WMSServiceLayer) e.getLayer()).getWMSLayers().size() == 1) {
                layer = (WMSLayer) ((WMSServiceLayer) e.getLayer()).getWMSLayers().get(0);
            }
            try {
                scrollToLegend(layer.getSelectedStyle().getLegendURL()[0].getOnlineResource().toString());
            } catch (Exception ex) {
                log.debug("Cannot scroll to legend of " + e.getLayer(), ex);//NOI18N
            }
        } else if (e.getLayer() instanceof SimpleLegendProvider) {

            SimpleLegendProvider slp = (SimpleLegendProvider) e.getLayer();
            scrollToLegend(slp.getLegendUrl());

        }
    }

    public void layerPositionChanged(ActiveLayerEvent e) {
        log.debug("layerPositionChanged() fired");//NOI18N
    }

    public void layerAdded(ActiveLayerEvent e) {
        log.debug("layerAdded() fired");//NOI18N

        if (e.getLayer() instanceof WMSServiceLayer) {
            WMSServiceLayer wmsLayer = (WMSServiceLayer) e.getLayer();
            Vector v = wmsLayer.getWMSLayers();
            Iterator it = v.iterator();
            while (it.hasNext()) {
                Object elem = (Object) it.next();
                if (elem instanceof WMSLayer) {
                    WMSLayer wl = (WMSLayer) elem;
                    String title = wl.getOgcCapabilitiesLayer().getTitle();
                    String name = wl.getOgcCapabilitiesLayer().getName();
                    String url = null;
                    try {
                        LegendURL[] lua = wl.getSelectedStyle().getLegendURL();
                        url = lua[0].getOnlineResource().toString();
                    } catch (Throwable t) {
                        log.debug("Could not find legend for " + title, t);//NOI18N
                    }
                    if (url != null) {
                        wmsCapabilities.put(url, wmsLayer.getWmsCapabilities());
                        this.addLegend(url, name);
                        log.debug("added legend:" + name + "=" + url);//NOI18N
                    }
                }
            }
        } else if (e.getLayer() instanceof SimpleLegendProvider) {
            SimpleLegendProvider slp = (SimpleLegendProvider) e.getLayer();
            this.addLegend(slp.getLegendUrl(), slp.getLegendIdentifier());

        } else {
            log.warn("For this type no legend can be created. " + e.getLayer());//NOI18N
        }
    }

    public void layerInformationStatusChanged(ActiveLayerEvent e) {


        if (e.getLayer() instanceof WMSServiceLayer) {
            WMSServiceLayer wmsLayer = (WMSServiceLayer) e.getLayer();
            Vector v = wmsLayer.getWMSLayers();
            Iterator it = v.iterator();
            while (it.hasNext()) {
                Object elem = (Object) it.next();
                if (elem instanceof WMSLayer) {
                    WMSLayer wl = (WMSLayer) elem;
                    String title = wl.getOgcCapabilitiesLayer().getTitle();
                    String name = wl.getOgcCapabilitiesLayer().getName();
                    String url = null;
                    try {
                        LegendURL[] lua = wl.getSelectedStyle().getLegendURL();
                        url = lua[0].getOnlineResource().toString();
                    } catch (Throwable t) {
                        log.debug("Could not find legend for " + title, t);//NOI18N
                    }
                    if (url != null) {
                        wmsCapabilities.put(url, wmsLayer.getWmsCapabilities());

                        tableModel.refreshLegend(url, name);

                    }
                }
            }
        } else if (e.getLayer() instanceof SimpleLegendProvider) {
            SimpleLegendProvider slp = (SimpleLegendProvider) e.getLayer();
            tableModel.refreshLegend(slp.getLegendUrl(), slp.getLegendIdentifier());

        } else {
            log.warn("For this type no legend can be created. " + e.getLayer());//NOI18N
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        scpLegends = new javax.swing.JScrollPane();
        tblLegends = new javax.swing.JTable();

        scpLegends.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scpLegends.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scpLegends.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scpLegends.setDoubleBuffered(true);
        tblLegends.setBackground(new java.awt.Color(236, 233, 216));
        tblLegends.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null}
            },
            new String [] {
                "Title 1"
            }
        ));

        setLayout(new java.awt.BorderLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 5, 1));
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scpLegends;
    private javax.swing.JTable tblLegends;
    // End of variables declaration//GEN-END:variables
    private int maxWidth = 0;

    private class LegendPanel extends JPanel implements RetrievalListener {

        private String url = "";//NOI18N
        JLabel lblImage = new JLabel();

        public LegendPanel() {
            super();
            setBorder(new EmptyBorder(2, 2, 2, 2));

            setLayout(new BorderLayout());

            add(lblImage, BorderLayout.CENTER);
            lblImage.setHorizontalAlignment(JLabel.LEADING);
            lblImage.setVerticalAlignment(JLabel.CENTER);
        }

        public LegendPanel(String url) {
            this();
            this.setUrl(url);
//            this.setMinimumSize(new Dimension(tblLegends.getColumnModel().getColumn(0).getPreferredWidth(),20));
            refresh();
        }

        public void refresh() {
            ImageRetrieval ir = new ImageRetrieval(this);
            ir.setUrl(url);
            ir.setWMSCapabilities(wmsCapabilities.get(url));
            ir.setPriority(Thread.NORM_PRIORITY);
            ir.start();
        }

        public void retrievalStarted(RetrievalEvent e) {
            lblImage.setIcon(null);
            lblImage.setText("...");//NOI18N
            if (maxWidth > 0) {
                tblLegends.getColumnModel().getColumn(0).setPreferredWidth(maxWidth);
            }
            tableModel.fireTableDataChanged();

        }

        public void retrievalProgress(RetrievalEvent e) {
        }

        public void retrievalError(RetrievalEvent e) {
            lblImage.setText("");//NOI18N
            log.error("Error while loading legend.");//NOI18N
            tableModel.fireTableDataChanged();

        }

        public void retrievalComplete(RetrievalEvent e) {
            if (e.getRetrievedObject() instanceof Image) {
                Image image = (Image) e.getRetrievedObject();
                ImageIcon ii = new ImageIcon(image);
                lblImage.setText("");//NOI18N
                lblImage.setIcon(ii);
                //setSize(new java.awt.Dimension(image.getWidth(null),image.getHeight(null)));

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

        public void retrievalAborted(RetrievalEvent e) {
            lblImage.setText("");//NOI18N
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean equals(Object tester) {
            if (tester instanceof LegendPanel) {
                LegendPanel t = (LegendPanel) tester;
                return t.url.equals(url);
            } else {
                return false;
            }
        }
    }

    private class CustomCellRenderer implements TableCellRenderer {

        /**
         *  Returns the component used for drawing the cell.  This method is
         *  used to configure the renderer appropriately before drawing.
         *
         *
         * @param table		the <code>JTable</code> that is asking the
         * 				renderer to draw; can be <code>null</code>
         * @param value		the value of the cell to be rendered.  It is
         * 				up to the specific renderer to interpret
         * 				and draw the value.  For example, if
         * 				<code>value</code>
         * 				is the string "true", it could be rendered as a
         * 				string or it could be rendered as a check
         * 				box that is checked.  <code>null</code> is a
         * 				valid value
         * @param isSelected	true if the cell is to be rendered with the
         * 				selection highlighted; otherwise false
         * @param hasFocus	if true, render cell appropriately.  For
         * 				example, put a special border on the cell, if
         * 				the cell can be edited, render in the color used
         * 				to indicate editing
         * @param row	        the row index of the cell being drawn.  When
         * 				drawing the header, the value of
         * 				<code>row</code> is -1
         * @param column	        the column index of the cell being drawn
         */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = (Component) value;
            if (isSelected) {
                component.setBackground(Color.white);//javax.swing.UIManager.getDefaults().getColor("Table.highlight"));
                component.setForeground(Legend.this.getForeground());
            } else {
                component.setBackground(Legend.this.getBackground());
                component.setForeground(Legend.this.getForeground());
            }

            if (table.getRowHeight(row) != (int) component.getPreferredSize().getHeight()) {
                table.setRowHeight(row, (int) component.getPreferredSize().getHeight());
            }

            return component;
        }
    }

    private class LegendModel extends AbstractTableModel {
        //private LinkedHashMap<String,LegendPanel> panels=new LinkedHashMap<String,LegendPanel>();

        private Vector<LegendPanel> panels = new Vector<LegendPanel>();
        private HashMap<String, LegendPanel> panelsByName = new HashMap<String, LegendPanel>();
        private HashMap<String, LegendPanel> panelsByUrl = new HashMap<String, LegendPanel>();
        private HashMap<String, String> urlsByName = new HashMap<String, String>();

        public Class<?> getColumnClass(int columnIndex) {
            return LegendPanel.class;
        }

        public int getRowCount() {
            return panels.size();
        }

        public Object getValueAt(int row, int column) {
            return panels.get(panels.size() - 1 - row);
        }

        public int getColumnCount() {
            return 1;
        }

        public void addLegend(String url, String name) {
            LegendPanel lp = new LegendPanel(url);
            urlsByName.put(name, url);
            if (!panels.contains(lp)) {
                panels.add(lp);
                panelsByName.put(name, lp);
                panelsByUrl.put(url, lp);
            }
            super.fireTableStructureChanged();
        }

        public void refreshLegend(String url, String layername) {
            String oldUrl = urlsByName.get(layername);
            if (!oldUrl.equals(url)) {
                urlsByName.put(layername, url);
                LegendPanel lp = panelsByName.get(layername);
                lp.setUrl(url);
                lp.refresh();
                panelsByUrl.remove(oldUrl);
                panelsByUrl.put(url, lp);
            } else {
                log.debug("no Refresh required. Same legend-urls");//NOI18N
            }

        }

        public void removeLegendByName(String layername) {
            String url = urlsByName.get(layername);
            urlsByName.remove(layername);
            if (!urlsByName.containsValue(url)) {
                LegendPanel lp = new LegendPanel(url);
                int index = panels.indexOf(lp);
                if (index != -1) {
                    panels.remove(new LegendPanel(url));
                    panelsByName.remove(layername);
                    panelsByUrl.remove(url);
                    super.fireTableRowsDeleted(index, index);
                }
            }
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public int getPosition(String url) {
            LegendPanel lp = panelsByUrl.get(url);

            return panels.size() - 1 - panels.indexOf(lp);
        }
//        public Vector<String> getAllUrls() {
//            return urlsByName.;
//        }
    }
}
