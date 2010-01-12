/*
 * CapabilityWidget.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 14. Oktober 2005, 15:52
 *
 */
package de.cismet.cismap.commons.gui.capabilitywidget;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import de.cismet.cismap.commons.capabilities.AbstractCapabilitiesTreeModel;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.MapBoundsListener;
import de.cismet.cismap.commons.interaction.events.CapabilityEvent;
import de.cismet.cismap.commons.preferences.CapabilitiesPreferences;
import de.cismet.cismap.commons.preferences.CapabilityLink;

import de.cismet.cismap.commons.featureservice.WFSCapabilitiesTreeCellRenderer;
import de.cismet.cismap.commons.featureservice.WFSCapabilitiesTreeModel;
import de.cismet.cismap.commons.featureservice.FeatureServiceUtilities;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.WFSOperator;
import de.cismet.cismap.commons.preferences.CapabilitiesListTreeNode;
import de.cismet.cismap.commons.raster.wms.WMSCapabilitiesTreeCellRenderer;
import de.cismet.cismap.commons.raster.wms.WMSCapabilitiesTreeModel;
import de.cismet.security.AccessHandler;
import de.cismet.security.WebAccessManager;
import de.cismet.security.exceptions.RequestFailedException;
import de.cismet.security.handler.WSSAccessHandler;
import de.cismet.tools.CismetThreadPool;
import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.gui.StaticSwingTools;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import org.deegree.services.capabilities.OGCWebServiceCapabilities;
import org.deegree.services.wms.capabilities.Layer;
import org.deegree.services.wms.capabilities.WMSCapabilities;
import org.deegree2.framework.xml.schema.ElementDeclaration;
import org.deegree2.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree2.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree_impl.services.wms.capabilities.OGCWMSCapabilitiesFactory;
import org.jdom.Element;

/**
 *
 * @author  thorsten.hell@cismet.de
 */
public class CapabilityWidget extends JPanel implements DropTargetListener, ChangeListener, ActionListener, Configurable, MapBoundsListener {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget");
    private int maxServerNameLength = 14;
    private ImageIcon icoConnect = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/connect.png"));
    private ImageIcon icoConnected = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/server.png"));
    private ImageIcon icoError = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/error.png"));
    private LinkedHashMap<LinkWithSubparent, JComponent> capabilityUrls = new LinkedHashMap<LinkWithSubparent, JComponent>();
    private LinkedHashMap<JComponent, LinkWithSubparent> capabilityUrlsReverse = new LinkedHashMap<JComponent, LinkWithSubparent>();
    private int acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
    private HashMap<Component, WMSCapabilities> wmsCapabilities = new HashMap<Component, WMSCapabilities>();
    private HashMap<Component, JTree> wmsCapabilitiesTrees = new HashMap<Component, JTree>();
    private HashMap<Component, WFSCapabilities> wfsCapabilities = new HashMap<Component, WFSCapabilities>();
    private HashMap<Component, JTree> wfsCapabilitiesTrees = new HashMap<Component, JTree>();
    private HashMap<Component, URL> wfsPostUrls = new HashMap<Component, URL>();
    private CapabilitiesPreferences preferences = new CapabilitiesPreferences();
    private JPopupMenu capabilityList = new JPopupMenu();
    private CapabilityWidget thisWidget = null;
//    private URL postURL;
    private Element serverElement;

    /**
     * Creates new form CapabilityWidget
     */
    public CapabilityWidget() {
        thisWidget = this;
        initComponents();
        tbpCapabilities.addChangeListener(this);
        tbpCapabilities.putClientProperty(Options.NO_CONTENT_BORDER_KEY, Boolean.FALSE);
        tbpCapabilities.setRequestFocusEnabled(false);
        cmdAddFromList.setComponentPopupMenu(capabilityList);
        DropTarget dt = new DropTarget(this, acceptableActions, this);
    //tbpCapabilities.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    //tbpCapabilities.setUI(new WindowsTabbedPaneUI());
    }

    /**
     * Erzeugt ein neues Tab in der TabbedPane und stoesst das parsen der 
     * Capabilities-XML an, die ueber den Link ansprechbar ist.
     * @param link URL zur Capabilities-XML-Datei
     * @param interactive true, falls per Drag&Drop, sonst false
     */
    private void processUrl(final String link, final String subparent, final boolean interactive) {
        log.info("processURL: " + link);
        // Gibts diese URL schon?
        // Text im Tab der L\u00E4nge der URL anpassen
        String tabText;
        if (subparent != null && subparent.trim().length() > 0) {
            tabText = subparent;
        } else {
            if (link.startsWith("http://") && link.length() > 21) {
                tabText = link.substring(7, 21) + "...";
            } else if (link.length() > 14) {
                tabText = link.substring(0, 14) + "...";
            } else {
                tabText = link;
            }
        }

        final JPanel load = getNewWaitingPanel(tabText);
        final Object test = capabilityUrls.get(new LinkWithSubparent(link, subparent));
        final String tabTextCopy = tabText;
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (test != null && test instanceof JComponent) {
                    synchronized (this) {
                        tbpCapabilities.setComponentAt(tbpCapabilities.indexOfComponent((JComponent) test), load);
                    }
                }
                capabilityUrls.put(new LinkWithSubparent(link, subparent), load);
                capabilityUrlsReverse.put(load, new LinkWithSubparent(link, subparent));
                synchronized (this) {
                    StaticSwingTools.jTabbedPaneWithVerticalTextAddTab(tbpCapabilities, tabTextCopy, icoConnect, load);
                }
                tbpCapabilities.setSelectedComponent(load);
                // setOGCWMSCapabilitiesTree(link, tbpCapabilities.getComponentCount()-1);

                //TODO
                //should be refactored --> coomon parts like capabilities s
                if (link.toLowerCase().contains("service=wfs")) {
                    addOGCWFSCapabilitiesTree(link, load, interactive);
                } else if (link.toLowerCase().contains("service=wms")) {
                    addOGCWMSCapabilitiesTree(link, load, interactive, subparent);
                } else if (link.toLowerCase().contains("service=wss")) {
                    try {
                        log.debug("WSS Capabilties Link hinzugefügt");
                        final URL url = new URL(link.substring(0, link.indexOf('?')));
                        log.debug("URL des WSS: " + url.toString());
                        if (!WebAccessManager.getInstance().isHandlerForURLRegistered(url)) {
                            WebAccessManager.getInstance().registerAccessHandler(url, AccessHandler.ACCESS_HANDLER_TYPES.WSS);
                        }
                        addOGCCapabilitiesTree(link, load, interactive);
                    } catch (MalformedURLException ex) {
                        log.error("Url is not wellformed no wss authentication possible", ex);
                    }
                } else {
                    //ToDo cleveres Probieren wenn z.B. nur die service URL angebenen wurde --> getCapabiltiesrequest aufbauen und probieren
                    log.info("service nicht spezifizierbar");
                    Object[] alternatives = {"OGC-Web Mapping Service", "OGC-Web Feature Service", "OGC-Web Security Service"};
                    Object selectedValue = JOptionPane.showInputDialog(CapabilityWidget.this,
                            "<html>Aus der URL:<br><pre>" + link + "</pre><br>kann der Servicetyp nicht ermittelt werden.<br><br>Um welchen Service handelt es sich?</html>",
                            "Uups", JOptionPane.INFORMATION_MESSAGE, null, alternatives, alternatives[0]);
                    if (selectedValue == alternatives[0]) {
                        addOGCWMSCapabilitiesTree(link, load, interactive, subparent);
                    } else if (selectedValue == alternatives[1]) {
                        addOGCWFSCapabilitiesTree(link, load, interactive);
                    } else if (selectedValue == alternatives[2]) {
                        try {
                            log.debug("WSS Capabilties Link hinzugefügt");
                            URL url;
                            if (link.indexOf('?') != -1) {
                                url = new URL(link.substring(0, link.indexOf('?')));
                            } else {
                                url = new URL(link);
                            }
                            log.debug("URL des WSS: " + url.toString());
                            if (!WebAccessManager.getInstance().isHandlerForURLRegistered(url)) {
                                WebAccessManager.getInstance().registerAccessHandler(url, AccessHandler.ACCESS_HANDLER_TYPES.WSS);
                            }
                            addOGCCapabilitiesTree(link, load, interactive);
                        } catch (MalformedURLException ex) {
                            log.error("Url is not wellformed no wss authentication possible", ex);
                        }
                    } else if (selectedValue == null) {
                        tbpCapabilities.remove(load);
                        capabilityUrls.remove(capabilityUrlsReverse.get(load));
                        capabilityUrlsReverse.remove(load);
                    } else {
                        addOGCWMSCapabilitiesTree(null, load, interactive, null);
                    }
                }
            }
        });
    }

    /**
     * Erzeugt ein neues Tab in der TabbedPane und st\u00F6\u00DFt das parsen der 
     * Capabilities-XML an, die \u00FCber den Link ansprechbar ist. 
     * Ruft processUrl(link, true) auf.
     * @param link URL zur Capabilities-XML-Datei
     */
    public void processUrl(String link, String subparent) {
        processUrl(link, subparent, true);
    }

    /**
     * Called when the drag operation has terminated with a drop on
     * the operable part of the drop site for the <code>DropTarget</code>
     * registered with this listener.
     * <p>
     * This method is responsible for undertaking
     * the transfer of the data associated with the
     * gesture. The <code>DropTargetDropEvent</code>
     * provides a means to obtain a <code>Transferable</code>
     * object that represents the data object(s) to
     * be transfered.<P>
     * From this method, the <code>DropTargetListener</code>
     * shall accept or reject the drop via the
     * acceptDrop(int dropAction) or rejectDrop() methods of the
     * <code>DropTargetDropEvent</code> parameter.
     * <P>
     * Subsequent to acceptDrop(), but not before,
     * <code>DropTargetDropEvent</code>'s getTransferable()
     * method may be invoked, and data transfer may be
     * performed via the returned <code>Transferable</code>'s
     * getTransferData() method.
     * <P>
     * At the completion of a drop, an implementation
     * of this method is required to signal the success/failure
     * of the drop by passing an appropriate
     * <code>boolean</code> to the <code>DropTargetDropEvent</code>'s
     * dropComplete(boolean success) method.
     * <P>
     * Note: The data transfer should be completed before the call  to the
     * <code>DropTargetDropEvent</code>'s dropComplete(boolean success) method.
     * After that, a call to the getTransferData() method of the
     * <code>Transferable</code> returned by
     * <code>DropTargetDropEvent.getTransferable()</code> is guaranteed to
     * succeed only if the data transfer is local; that is, only if
     * <code>DropTargetDropEvent.isLocalTransfer()</code> returns
     * <code>true</code>. Otherwise, the behavior of the call is
     * implementation-dependent.
     * <P>
     * @param dtde the <code>DropTargetDropEvent</code>
     */
    public void drop(DropTargetDropEvent dtde) {
        if (isDropOk(dtde)) {
            String link = getLink(dtde);
            processUrl(link, null);
        }
    }

    // restliche unbenutzte DnD-Methoden
    public void dragExit(DropTargetEvent dte) {
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void dragOver(DropTargetDragEvent dtde) {
    }

    public void dragEnter(DropTargetDragEvent dtde) {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        cmdCollapse = new javax.swing.JButton();
        cmdAddFromList = new javax.swing.JButton();
        cmdAddByUrl = new javax.swing.JButton();
        cmdRemove = new javax.swing.JButton();
        cmdRefresh = new javax.swing.JButton();
        tbpCapabilities = StaticSwingTools.jTabbedPaneWithVerticalTextCreator(JTabbedPane.LEFT,JTabbedPane.SCROLL_TAB_LAYOUT);

        setLayout(new java.awt.BorderLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setPreferredSize(new java.awt.Dimension(200, 250));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jToolBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        cmdCollapse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/collapseTree.png")));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle"); // NOI18N
        cmdCollapse.setToolTipText(bundle.getString("CapabilityWidget.cmdCollapse.toolTipText")); // NOI18N
        cmdCollapse.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdCollapse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCollapseActionPerformed(evt);
            }
        });

        jToolBar1.add(cmdCollapse);

        cmdAddFromList.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/addServerFromList.png")));
        cmdAddFromList.setToolTipText(bundle.getString("CapabilityWidget.cmdAddFromList.toolTipText")); // NOI18N
        cmdAddFromList.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdAddFromList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdAddFromListActionPerformed(evt);
            }
        });

        jToolBar1.add(cmdAddFromList);

        cmdAddByUrl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/addServerFromUrl.png")));
        cmdAddByUrl.setToolTipText(bundle.getString("CapabilityWidget.cmdAddByUrl.toolTipText")); // NOI18N
        cmdAddByUrl.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdAddByUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdAddByUrlActionPerformed(evt);
            }
        });

        jToolBar1.add(cmdAddByUrl);

        cmdRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/tab_remove.png")));
        cmdRemove.setToolTipText(bundle.getString("CapabilityWidget.cmdRemove.toolTipText")); // NOI18N
        cmdRemove.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRemoveActionPerformed(evt);
            }
        });

        jToolBar1.add(cmdRemove);

        cmdRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/refresh.png")));
        cmdRefresh.setToolTipText(bundle.getString("CapabilityWidget.cmdRefresh.toolTipText")); // NOI18N
        cmdRefresh.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRefreshActionPerformed(evt);
            }
        });

        jToolBar1.add(cmdRefresh);

        jPanel1.add(jToolBar1, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        tbpCapabilities.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        tbpCapabilities.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tbpCapabilities.setPreferredSize(new java.awt.Dimension(180, 400));
        add(tbpCapabilities, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents
    private void cmdAddFromListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAddFromListActionPerformed
        capabilityList.show(cmdAddFromList, 0, cmdAddFromList.getHeight());
        capabilityList.setVisible(true);
    }//GEN-LAST:event_cmdAddFromListActionPerformed

    private void cmdRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRefreshActionPerformed
        JTree active = getActiveTree();
        if (active != null) {
            LinkWithSubparent link = capabilityUrlsReverse.get(tbpCapabilities.getSelectedComponent());
            addLinkManually(link);
        }
    }//GEN-LAST:event_cmdRefreshActionPerformed

    private void cmdAddByUrlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAddByUrlActionPerformed
        String input = JOptionPane.showInputDialog(this, ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("CapabilityWidget.Bitte_geben_Sie_den_Link_zum_Server_ein."),
                ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("CapabilityWidget.Url_eingeben"), JOptionPane.INFORMATION_MESSAGE);
        if (input != null) {
            processUrl(input, null, true);
        }
    }//GEN-LAST:event_cmdAddByUrlActionPerformed

    private void cmdRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemoveActionPerformed
        removeActiveCapabilityTree();
    }//GEN-LAST:event_cmdRemoveActionPerformed

    /**
     * Entfernt einen Capability-Baum aus der TabbedPane.
     */
    private void removeActiveCapabilityTree() {
        JTree active = getActiveTree();
        if (active != null) {
            log.debug("active = " + active);
            //ToDo wenn das hier nur bei einem aktivierten Tree gemacht wird kann der link nicht mehr hinzugefügt werden
            LinkWithSubparent link = capabilityUrlsReverse.get(tbpCapabilities.getSelectedComponent());
            capabilityUrls.remove(link);
            capabilityUrlsReverse.remove(tbpCapabilities.getSelectedComponent());
            if (wmsCapabilities.get(tbpCapabilities.getSelectedComponent()) != null) {
                log.debug("Entferne WMSCapabilities-Tree");
                wmsCapabilities.remove(tbpCapabilities.getSelectedComponent());
                wmsCapabilitiesTrees.remove(tbpCapabilities.getSelectedComponent());
                tbpCapabilities.remove(tbpCapabilities.indexOfComponent(tbpCapabilities.getSelectedComponent()));
            } else if (wfsCapabilities.get(tbpCapabilities.getSelectedComponent()) != null) {
                log.debug("Entferne WFSCapabilities-Tree");
                wfsCapabilities.remove(tbpCapabilities.getSelectedComponent());
                wfsCapabilitiesTrees.remove(tbpCapabilities.getSelectedComponent());
                wfsPostUrls.remove(tbpCapabilities.getSelectedComponent());
                tbpCapabilities.remove(tbpCapabilities.indexOfComponent(tbpCapabilities.getSelectedComponent()));
            } else {
                log.warn("Keine Component zum entfernen aktiv");
            }
        } else {
            log.debug("kein Baum aktiv, entferne selektierten Reiter");
            if (tbpCapabilities.getSelectedComponent() != null) {
                wmsCapabilities.remove(tbpCapabilities.getSelectedComponent());
                wfsCapabilities.remove(tbpCapabilities.getSelectedComponent());
                wfsPostUrls.remove(tbpCapabilities.getSelectedComponent());
                tbpCapabilities.remove(tbpCapabilities.getSelectedComponent());
            }
        }
    }

    private void cmdCollapseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCollapseActionPerformed
        JTree active = getActiveTree();
        if (active != null) {
            int row = active.getRowCount() - 1;
            while (row > 0) {
                active.collapseRow(row);
                row--;
            }
        }
    }//GEN-LAST:event_cmdCollapseActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdAddByUrl;
    private javax.swing.JButton cmdAddFromList;
    private javax.swing.JButton cmdCollapse;
    private javax.swing.JButton cmdRefresh;
    private javax.swing.JButton cmdRemove;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTabbedPane tbpCapabilities;
    // End of variables declaration//GEN-END:variables

    /**
     * Liefert den momentan selektierten Capabilties-Baum.
     * @return selektierter Capabilties-Baum
     */
    private JTree getActiveTree() {
        if (wmsCapabilitiesTrees.get(tbpCapabilities.getSelectedComponent()) != null) {
            return wmsCapabilitiesTrees.get(tbpCapabilities.getSelectedComponent());
        } else if (wfsCapabilitiesTrees.get(tbpCapabilities.getSelectedComponent()) != null) {
            return wfsCapabilitiesTrees.get(tbpCapabilities.getSelectedComponent());
        } else {
            return null;
        }
    }

    /**
     * Testmethode, um das Widget Standalone zu testen.
     * @param args Parameter
     */
    public static void main(String args[]) {
        try {
            org.apache.log4j.PropertyConfigurator.configure(ClassLoader.getSystemResource("/de/cismet/cismap/commons/demo/log4j.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
        } catch (Exception e) {
            log.warn(ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("CapabilityWidget.Fehler_beim_Einstellen_des_Look_Feels_s"), e);
        }
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                JFrame f = new JFrame();
                f.getContentPane().add(new CapabilityWidget());
                f.pack();
                f.setVisible(true);
                f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            }
        });
    }

    /**
     * Testet, ob ein g\u00FCltiges Objekt in das CapabilityWidget gezogen wurde.
     * @param e DropEvent
     * @return true, falls g\u00FCltiges Objekt, sonst false
     */
    private boolean isDropOk(DropTargetDropEvent e) {
        if (e.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Extrahiert die URL aus dem auf das Widget gezogenen Objekt.
     * @param dtde DropEvent
     * @return  URL als String-Objekt
     */
    private String getLink(DropTargetDropEvent dtde) {
        String link = null;
        try {
            dtde.acceptDrop(acceptableActions);
            Object data = dtde.getTransferable().getTransferData(DataFlavor.getTextPlainUnicodeFlavor());
            if (data instanceof InputStream) {
                InputStream input = (InputStream) data;
                InputStreamReader isr = new InputStreamReader(input);

                StringBuffer str = new StringBuffer();
                int in = -1;
                try {
                    while ((in = isr.read()) >= 0) {
                        if (in != 0) {
                            str.append((char) in);
                        }
                    }
                    link = str.toString();
                } catch (IOException ioe) {
                    /*
                    bug #4094987
                    sun.io.MalformedInputException: Missing byte-order mark
                    e.g. if dragging from MS Word 97 to Java
                    still a bug in 1.2 final
                     */
                    System.err.println("cannot read" + ioe);
                    dtde.dropComplete(false);
                    String message = "Bad drop\n" + ioe.getMessage();
                    JOptionPane.showMessageDialog(this,
                            message,
                            "Error",
                            JOptionPane.ERROR_MESSAGE); //NOI18N

                    return null;
                }
            }
            //Wir gehen davon aus, dass der Link Title immer in der 2ten Zeile steht
            try {
                link = link.substring(0, link.indexOf("\n"));
            } catch (Exception e) {
            }
            return link;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return link;
    }

    /**
     * 
     * @param tabTitle
     * @return
     */
    private JPanel getNewWaitingPanel(String tabTitle) {
        JPanel panLoad = new JPanel();
        JLabel lblWorld = new JLabel();
        JLabel lblLoading = new JLabel();
        JPanel panFillTop = new JPanel();
        JPanel panFillBottom = new JPanel();
        panLoad.setLayout(new GridBagLayout());
        panLoad.putClientProperty("tabTitle", tabTitle);
        lblWorld.setHorizontalAlignment(SwingConstants.CENTER);
        lblWorld.setIcon(new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/load.png")));
        lblWorld.setVerticalAlignment(SwingConstants.TOP);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panLoad.add(lblWorld, gridBagConstraints);

        lblLoading.setText(ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("CapabilityWidget.loading"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        panLoad.add(lblLoading, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        panLoad.add(panFillTop, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        panLoad.add(panFillBottom, gridBagConstraints);

        return panLoad;
    }

    /**
     * Erzeugt einen WMS-Baum aus der geparsten Capabilities-XML und f\u00FCgt ihn der 
     * TabbedPane hinzu.
     * @param link Capabilites-URL
     * @param comp Component
     */
    private void addOGCWMSCapabilitiesTree(final String link, final JComponent comp, String subparent) {
        addOGCWMSCapabilitiesTree(link, comp, true, subparent);
    }

    /**
     * Falls ein Link manuell \u00FCber einen Dialog eingegeben wurde.
     * @param link Capability-Link
     */
    private void addLinkManually(LinkWithSubparent link) {
        processUrl(link.getLink(), link.getSubparent(), false);
    }

    /**
     * Erzeugt den Baum aus der geparsten Capabilities-XML und f\u00FCgt ihn der 
     * TabbedPane hinzu.
     * @param link Capabilites-URL
     * @param comp Component
     * @param interactive true, falls per Drag&Drop, sonst false
     */
    private void addOGCWMSCapabilitiesTree(final String link, final JComponent comp, final boolean interactive, final String subparent) {
        log.debug("addOGCWMSCapabilitiesTree()");
        Runnable t = new Runnable() {

            @Override
            public void run() {
                try {
                    final DragTree trvCap = new DragTree();
                    URL getCapURL = new URL(link);
                    OGCWMSCapabilitiesFactory capFact = new OGCWMSCapabilitiesFactory();
                    CismapBroker broker = CismapBroker.getInstance();
                    log.debug("Capability Widget: Creating WMScapabilities for URL: " + getCapURL.toString());
                    //final WMSCapabilities cap = capFact.createCapabilities(HttpAuthentication.getInputStreamReaderFromURL(CapabilityWidget.this, getCapURL));
                    final WMSCapabilities cap = capFact.createCapabilities(new InputStreamReader(WebAccessManager.getInstance().doRequest(getCapURL)));
                    log.debug("finished creating Capabilties");
                    //TODO for WFS
                    //ToDo funktionalität abgeschaltet steckt zur zeit in CismetGUICommons --> refactoring
//                    broker.addHttpCredentialProviderCapabilities(cap, broker.getHttpCredentialProviderURL(getCapURL));
//                    if (broker.isServerSecuredByPassword(cap)) {
//                        broker.addProperty(getCapURL.toString(), cap.getCapability().getLayer().getTitle());
//                    }
                    trvCap.setWmsCapabilities(cap);
                    final WMSCapabilitiesTreeModel tm = new WMSCapabilitiesTreeModel(cap, subparent);
                    DropTarget dt = new DropTarget(trvCap, acceptableActions, thisWidget);
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            trvCap.setModel(tm);
                            trvCap.setBorder(new EmptyBorder(1, 1, 1, 1));
                            trvCap.setCellRenderer(new WMSCapabilitiesTreeCellRenderer());
                            final JScrollPane sPane = new JScrollPane();
                            sPane.setViewportView(trvCap);
                            sPane.setBorder(new EmptyBorder(1, 1, 1, 1));
                            StaticSwingTools.setNiftyScrollBars(sPane);
                            synchronized (this) {
                                tbpCapabilities.setComponentAt(tbpCapabilities.indexOfComponent(comp), sPane);
                            }
                            wmsCapabilities.put(sPane, cap);
                            wmsCapabilitiesTrees.put(sPane, trvCap);
                            stateChanged(null);

                            capabilityUrls.put(new LinkWithSubparent(link, subparent), sPane);
                            capabilityUrlsReverse.put(sPane, new LinkWithSubparent(link, subparent));
                            String title = cap.getCapability().getLayer().getTitle().trim();
                            if (subparent != null) {
                                title = subparent;
                            }
                            String titleOrig = title;
                            if (title.length() > 0) {
                                if (title.length() > maxServerNameLength) {
                                    title = title.substring(0, maxServerNameLength - 3) + "...";
                                }
                                sPane.putClientProperty("tabTitle", title);
                                synchronized (this) {
                                    StaticSwingTools.jTabbedPaneWithVerticalTextSetNewText(tbpCapabilities, title, icoConnected, Color.black, sPane);
                                }
                                synchronized (this) {
                                    tbpCapabilities.setToolTipTextAt(tbpCapabilities.indexOfComponent(sPane), titleOrig);
                                }
                                stateChanged(null);
                            }
                        }
                    });
                } catch (Exception e) {
                    log.error("Fehler währened dem erstellen des WMSCapabilties Baums", e);
                    String message = "";

                    tbpCapabilities.setIconAt(tbpCapabilities.indexOfComponent(comp), icoError);
                    if (e instanceof RequestFailedException || e.getMessage() == null || e.getMessage().equals("null")) {
                        message = e.getCause().getMessage();
                    } else {
                        message = e.getMessage();
                    }

                    if (interactive) {
                        JOptionPane.showMessageDialog(thisWidget,
                                ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("CapabilityWidget.Fehler_beim_Laden_der_Capabilities_des_Servers") + message,
                                ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("CapabilityWidget.Error"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                    //TODO: Error \u00FCber die Statuszeile bekanntgeben
                    log.error(ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("CapabilityWidget.Fehler_beim_Laden_der_Capabilities_des_Servers_log") + message, e);
                    tbpCapabilities.remove(tbpCapabilities.indexOfComponent(comp));

                    JComponent jc = capabilityUrls.get(link);
                    capabilityUrls.remove(new LinkWithSubparent(link, subparent));
                    capabilityUrlsReverse.remove(jc);
                }
            }
        };
        CismetThreadPool.execute(t);
    }

    public void addOGCCapabilitiesTree(final String link, final JComponent comp, final boolean interactive) {
        log.debug("addOGCCapabilitiesTree()");
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    final DragTree trvCap = new DragTree();
                    //ToDo outsource/generalise method
                    // URL-String als URL speichern
                    URL getCapURL = new URL(link);
                    URL postURL;

                    // WFSCapabilities aus dem \u00FCbergebenen Link (liefert XML-Dok) parsen
                    //log.debug("Versuche WFSCapabilities zu parsen");
                    if (link.indexOf('?') > 0) {
                        postURL = new URL(link.substring(0, link.indexOf('?')));
                    } else {
                        postURL = getCapURL;
                    }

                    final URL finalPostUrl = postURL;
//                    final WFSOperator op = new WFSOperator();
//                    final WFSCapabilities cap = op.parseWFSCapabilites(postURL);
//
//                    // Hashmap mit den FeatureLayer-Attributen erzeugen
//                    log.debug("Erzeuge WFSCapabilitiesTreeModel");
//                    final WFSCapabilitiesTreeModel tm = new WFSCapabilitiesTreeModel(cap);
//                    tm.setFeatureTypes(op.getElements(postURL, cap.getFeatureTypeList()));

                    // Den WFSTree als DropTarget spezifizieren
                    //final AbstractCapabilitiesTreeModel capTreeModel = passwordDialog.getCapabilitiesTree();
                    DropTarget dt = new DropTarget(trvCap, acceptableActions, thisWidget);


                    final AbstractCapabilitiesTreeModel capTreeModel;
                    //TODO!!! Wenn beim abrufen der Capabillities der neue Server entfernt wird --> kann er nicht mehr hinzugefügt werden kann

                    AccessHandler handler = WebAccessManager.getInstance().getHandlerForURL(finalPostUrl);
                    final String securedServiceType = ((WSSAccessHandler) handler).getSecuredServiceTypeForURL(finalPostUrl);
                    if (securedServiceType != null) {
                        log.debug("SecuredServiceType des WSS konnte bestimmt werden: " + securedServiceType);
                        if (securedServiceType.equals(WSSAccessHandler.SECURED_SERVICE_TYPE.WFS.toString())) {
                            log.debug("Gesicheter Service ist ein: " + WSSAccessHandler.SECURED_SERVICE_TYPE.WFS);
                            log.debug("Capability Widget: Creating WFScapabilities for URL: " + finalPostUrl.toString());

//                            InputStream result = WebAccessManager.getInstance().doRequest(finalPostUrl, new StringReader("?REQUEST=GetCapabilities&service=WFS"), AccessHandler.ACCESS_METHODS.GET_REQUEST);
//                            final WFSOperator op = new WFSOperator();
//                            final WFSCapabilities capWFS = op.parseWFSCapabilites(new BufferedReader(new InputStreamReader(result)));
//                            log.debug("Erstelle WFSCapabilitiesTreeModel");
                             // !!!ToDo WebAccessMananger testen
                            final FeatureServiceUtilities utilities = new FeatureServiceUtilities();
                            WFSCapabilitiesDocument wfsDoc = utilities.getWFSCapabilitesDocument(postURL);
                            final WFSCapabilities cap = (WFSCapabilities) wfsDoc.parseCapabilities();
                            final String name = FeatureServiceUtilities.getServiceName(wfsDoc);

                            capTreeModel = new WFSCapabilitiesTreeModel(cap, utilities.getElementDeclarations(finalPostUrl, cap.getFeatureTypeList()));
                            capTreeModel.setServiceName(name);
                            //((WFSCapabilitiesTreeModel) capTreeModel).setFeatureTypes(op.getElements(finalPostUrl, capWFS.getFeatureTypeList()));
                        } else if (securedServiceType.equals(WSSAccessHandler.SECURED_SERVICE_TYPE.WMS.toString())) {
                            log.debug("Gesicheter Service ist ein: " + WSSAccessHandler.SECURED_SERVICE_TYPE.WMS);
                            try {
                                OGCWMSCapabilitiesFactory capFact = new OGCWMSCapabilitiesFactory();
                                log.debug("Capability Widget: Creating WMScapabilities for URL: " + finalPostUrl.toString());
                                InputStream result = WebAccessManager.getInstance().doRequest(finalPostUrl, new StringReader("REQUEST=GetCapabilities&service=WMS"), AccessHandler.ACCESS_METHODS.GET_REQUEST);
                                log.debug("WMS Capabilties erstellt");
                                //ToDO Langsam
                                final WMSCapabilities capWMS = capFact.createCapabilities(new BufferedReader(new InputStreamReader(result)));
                                log.debug("Erstelle WMSCapabilitiesTreeModel");
                                capTreeModel = new WMSCapabilitiesTreeModel(capWMS);
                                capTreeModel.setServiceName(capWMS.getCapability().getLayer().getTitle().trim());
                            } catch (Exception ex) {
                                log.error("Exception during doRequest cause: ", ex);
                                return;
                            }
                        } else {
                            log.debug("Gesicherter Service ist von unbekanntem Typ.");
                            return;
                        }
                    } else {
                        log.warn("SecuredServiceType des WSS konnte nicht bestimmt werden");
                        return;
                    }

                    // ToDo Listener oder sonstwas damit das retrieval auch abgebrochen wird
                    if (tbpCapabilities.indexOfComponent(comp) == -1) {
                        log.info("Ladepanel ist nicht mehr in TabbedPane --> retrieval wird abgebrochen");
                        LinkWithSubparent link = capabilityUrlsReverse.get(comp);
                        capabilityUrls.remove(link);
                        capabilityUrlsReverse.remove(comp);
                        if (wmsCapabilities.get(comp) != null) {
                            log.debug("Entferne WMSCapabilities-Tree");
                            wmsCapabilities.remove(comp);
                            wmsCapabilitiesTrees.remove(comp);
                            tbpCapabilities.remove(tbpCapabilities.indexOfComponent(comp));
                        } else if (wfsCapabilities.get(comp) != null) {
                            log.debug("Entferne WFSCapabilities-Tree");
                            wfsCapabilities.remove(comp);
                            wfsCapabilitiesTrees.remove(comp);
                            wfsPostUrls.remove(comp);
                            tbpCapabilities.remove(comp);
                        } else {
                            log.warn("Keine Component zum entfernen aktiv");
                        }
                        return;
                    } else {
                        log.debug("Capabilitespanel noch vorhanden --> stelle baum dar");
                    }
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            //broker.addHttpCredentialProviderCapabilities(cap, broker.getHttpCredentialProviderURL(url));
                            //ToDO subparent

                            String name = capTreeModel.getServiceName();
                            log.debug("ServiceName: " + name);
                            trvCap.setModel(capTreeModel);

                            trvCap.setBorder(new EmptyBorder(
                                    1, 1, 1, 1));
                            final JScrollPane sPane = new JScrollPane();
                            sPane.setViewportView(trvCap);
                            sPane.setBorder(
                                    new EmptyBorder(1, 1, 1, 1));
                            StaticSwingTools.setNiftyScrollBars(sPane);
                            synchronized (this) {
                                tbpCapabilities.setComponentAt(tbpCapabilities.indexOfComponent(comp), sPane);
                            }
                            //ToDo generalize --> getCapabilities of AbstractCapabilitiesTreeModel
                            if (capTreeModel instanceof WMSCapabilitiesTreeModel) {
                                log.debug("WMSTree");
                                wmsCapabilities.put(sPane, ((WMSCapabilitiesTreeModel) capTreeModel).getCapabilities());
                                wmsCapabilitiesTrees.put(sPane, trvCap);
                                trvCap.setWmsCapabilities(((WMSCapabilitiesTreeModel) capTreeModel).getCapabilities());
                                trvCap.setCellRenderer(new WMSCapabilitiesTreeCellRenderer());
                                stateChanged(null);
                            } else if (capTreeModel instanceof WFSCapabilitiesTreeModel) {
                                log.debug("WFSTree");
                                wfsCapabilities.put(sPane, ((WFSCapabilitiesTreeModel) capTreeModel).getCapabilities());
                                wfsCapabilitiesTrees.put(sPane, trvCap);
                                wfsPostUrls.put(sPane, finalPostUrl);
                                trvCap.setCellRenderer(new WFSCapabilitiesTreeCellRenderer(name));
                                stateChanged(null);
                            } else {
                                //Throw exception
                            }

                            capabilityUrls.put(new LinkWithSubparent(link, null), sPane);
                            capabilityUrlsReverse.put(sPane,
                                    new LinkWithSubparent(link, null));
                            String title = name;
                            String titleOrig = title;

                            if (title.length() >
                                    0) {
                                if (title.length() > maxServerNameLength) {
                                    title = title.substring(0, maxServerNameLength - 3) + "...";
                                }
                                sPane.putClientProperty("tabTitle", title);
                                synchronized (this) {
                                    StaticSwingTools.jTabbedPaneWithVerticalTextSetNewText(tbpCapabilities, title, icoConnected, Color.black, sPane);
                                }
                                synchronized (this) {
                                    tbpCapabilities.setToolTipTextAt(tbpCapabilities.indexOfComponent(sPane), titleOrig);
                                }
                                stateChanged(null);
                            }
                        }
                    });
                } catch (Exception e) {
                    String message = "";

                    tbpCapabilities.setIconAt(tbpCapabilities.indexOfComponent(comp), icoError);
                    if (e.getMessage() == null || e.getMessage().equals("null")) {
                        message = e.getCause().getMessage();
                    } else {
                        message = e.getMessage();
                    }

                    if (interactive) {
                        JOptionPane.showMessageDialog(thisWidget,
                                ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("CapabilityWidget.Fehler_beim_Laden_der_Capabilities_des_Servers") + " " + message,
                                ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("CapabilityWidget.Error"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                    log.error(ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("CapabilityWidget.Fehler_beim_Laden_der_Capabilities_des_Servers_log") + " " + message, e);
                    tbpCapabilities.remove(tbpCapabilities.indexOfComponent(comp));

                    JComponent jc = capabilityUrls.get(link);
                    capabilityUrls.remove(link);
                    capabilityUrlsReverse.remove(jc);
                }
            }
        };

        CismetThreadPool.execute(r);
    }

    /**
     * Erzeugt den Baum aus der geparsten Capabilities-XML und f\u00FCgt ihn der 
     * TabbedPane hinzu.
     * @param link Capabilites-URL
     * @param comp Component
     * @param interactive true, falls per Drag&Drop, sonst false
     */
    private void addOGCWFSCapabilitiesTree(final String link,
            final JComponent comp,
            final boolean interactive) {
        log.debug("addOGCWFSCapabilitiesTree()");
        Runnable t = new Runnable() {

            @Override
            public void run() {
                try {
                    final DragTree trvCap = new DragTree();
                    // URL-String als URL speichern
                    URL getCapURL = new URL(link);
                    URL postURL;

// WFSCapabilities aus dem \u00FCbergebenen Link (liefert XML-Dok) parsen
                    log.debug("Versuche WFSCapabilities zu parsen");
                    if (link.indexOf('?') > 0) {
                        postURL = new URL(link.substring(0, link.indexOf('?')));
                    } else {
                        postURL = getCapURL;
                    }

                    final URL finalPostUrl = postURL;
                    final FeatureServiceUtilities utilities = new FeatureServiceUtilities();
                    WFSCapabilitiesDocument wfsDoc = utilities.getWFSCapabilitesDocument(postURL);
                    final WFSCapabilities cap = (WFSCapabilities) wfsDoc.parseCapabilities();
                    final String name = FeatureServiceUtilities.getServiceName(wfsDoc);

                    // Hashmap mit den FeatureLayer-Attributen erzeugen
                    log.debug("Erzeuge WFSCapabilitiesTreeModel");
                    final WFSCapabilitiesTreeModel tm = new WFSCapabilitiesTreeModel(cap, utilities.getElementDeclarations(postURL, cap.getFeatureTypeList()));

                    // Den WFSTree als DropTarget spezifizieren
                    DropTarget dt = new DropTarget(trvCap, acceptableActions, thisWidget);
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            trvCap.setModel(tm);
                            trvCap.setBorder(new EmptyBorder(1, 1, 1, 1));
                            trvCap.setCellRenderer(new WFSCapabilitiesTreeCellRenderer(name));
                            final JScrollPane sPane = new JScrollPane();
                            sPane.setViewportView(trvCap);
                            sPane.setBorder(new EmptyBorder(1, 1, 1, 1));
                            StaticSwingTools.setNiftyScrollBars(sPane);
                            synchronized (this) {
                                tbpCapabilities.setComponentAt(tbpCapabilities.indexOfComponent(comp), sPane);
                            }

                            wfsCapabilities.put(sPane, cap);
                            wfsCapabilitiesTrees.put(sPane, trvCap);
                            wfsPostUrls.put(sPane, finalPostUrl);
                            stateChanged(null);

                            capabilityUrls.put(new LinkWithSubparent(link, null), sPane);
                            capabilityUrlsReverse.put(sPane, new LinkWithSubparent(link, null));
                            String title = name;
                            String titleOrig = title;
                            if (title.length() > 0) {
                                if (title.length() > maxServerNameLength) {
                                    title = title.substring(0, maxServerNameLength - 3) + "...";
                                }

                                sPane.putClientProperty("tabTitle", title);
                                synchronized (this) {
                                    StaticSwingTools.jTabbedPaneWithVerticalTextSetNewText(tbpCapabilities, title, icoConnected, Color.black, sPane);
                                }

                                synchronized (this) {
                                    tbpCapabilities.setToolTipTextAt(tbpCapabilities.indexOfComponent(sPane), titleOrig);
                                }

                                stateChanged(null);
                            }

                        }
                    });
                } catch (Exception e) {
                    String message = "";

                    tbpCapabilities.setIconAt(tbpCapabilities.indexOfComponent(comp), icoError);
                    if (e.getMessage() == null || e.getMessage().equals("null")) {
                        message = e.getCause().getMessage();
                    } else {
                        message = e.getMessage();
                    }

                    if (interactive) {
                        JOptionPane.showMessageDialog(thisWidget,
                                ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("CapabilityWidget.Fehler_beim_Laden_der_Capabilities_des_Servers") + " " + message,
                                ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("CapabilityWidget.Error"),
                                JOptionPane.ERROR_MESSAGE);
                    }

                    log.error(ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("CapabilityWidget.Fehler_beim_Laden_der_Capabilities_des_Servers_log") + " " + message, e);
                    tbpCapabilities.remove(tbpCapabilities.indexOfComponent(comp));

                    JComponent jc = capabilityUrls.get(link);
                    capabilityUrls.remove(link);
                    capabilityUrlsReverse.remove(jc);
                }

            }
        };
        CismetThreadPool.execute(t);
    }
    /**
     * Invoked when the target of the listener has changed its state.
     * @param e  a ChangeEvent object
     */
    int selectedIndex = -1;

    public void stateChanged(ChangeEvent e) {
        if (selectedIndex > -1) {
            try {
                tbpCapabilities.setForegroundAt(selectedIndex, null);
                String t;

                t =
                        (String) ((JComponent) tbpCapabilities.getComponentAt(selectedIndex)).getClientProperty("tabTitle");
                if (t == null) {
                    t = "";
                }

                StaticSwingTools.jTabbedPaneWithVerticalTextSetNewText(tbpCapabilities, t, icoConnected, Color.black, (JComponent) tbpCapabilities.getComponentAt(selectedIndex));
            } catch (Throwable skip) {
                // do nothing
            }
        }
        selectedIndex = tbpCapabilities.getSelectedIndex();
        if (selectedIndex > -1) {
            String t = (String) ((JComponent) tbpCapabilities.getComponentAt(selectedIndex)).getClientProperty("tabTitle");
            if (t == null) {
                t = "";
            }

            tbpCapabilities.setForegroundAt(selectedIndex, Color.blue);
            StaticSwingTools.jTabbedPaneWithVerticalTextSetNewText(tbpCapabilities, t, icoConnected, Color.blue, (JComponent) tbpCapabilities.getComponentAt(selectedIndex));
        }

        if (wmsCapabilities.get(tbpCapabilities.getSelectedComponent()) != null) {
            CismapBroker.getInstance().fireCapabilityServerChanged(new CapabilityEvent(wmsCapabilities.get(tbpCapabilities.getSelectedComponent())));
        } else if (wfsCapabilities.get(tbpCapabilities.getSelectedComponent()) != null) {
            CismapBroker.getInstance().fireCapabilityServerChanged(new CapabilityEvent(wfsCapabilities.get(tbpCapabilities.getSelectedComponent())));
        } else {
            log.debug(wmsCapabilities);
            log.debug(wfsCapabilities);
        }

    }

    /**
     * Invoked when an action occurs. Loggt das ActionEvent als ERROR.
     */
    public void actionPerformed(ActionEvent e) {
        log.error(e);
    }

    /**
     * 
     * @return
     */
    public Element getConfiguration() {
        //TODO Im Moment gibts nur OGC-WMS Links. Da faul ....
        Element ret = new Element("cismapCapabilitiesPreferences");
        {

            Set wmsCapabilitiesSet = capabilityUrls.keySet();
            Iterator<LinkWithSubparent> it = wmsCapabilitiesSet.iterator();
            LinkWithSubparent selectedLink = capabilityUrlsReverse.get(tbpCapabilities.getSelectedComponent());
            while (it.hasNext()) {
                LinkWithSubparent link = it.next();
                CapabilityLink cl = new CapabilityLink(CapabilityLink.OGC, link.getLink(), link.equals(selectedLink), link.getSubparent());
                ret.addContent(cl.getElement());
            }

        }
        return ret;
    }

    /**
     * 
     * @param e
     */
    public void masterConfigure(Element e) {
        serverElement = e;
    }

    /**
     * 
     * @param e
     */
    public void configure(Element e) {
        preferences = new CapabilitiesPreferences(serverElement, e);
        configure(preferences);
    }

    /**
     * Konfiguriert das Widget und bestimmt, welche Capabilities verstanden werden.
     * @param cp CapabilitiesPreferences
     */
    public void configure(CapabilitiesPreferences cp) {
        removeAllServer();
        JComponent activeComponent = null;
        Iterator<Integer> it = cp.getCapabilities().keySet().iterator();
        while (it.hasNext()) {
            Integer i = it.next();
            CapabilityLink cl = cp.getCapabilities().get(i);
            if (cl.getType().equals(CapabilityLink.OGC) || cl.getType().equals(CapabilityLink.OGC_DEPRECATED)) {
                addLinkManually(new LinkWithSubparent(cl.getLink(), cl.getSubparent()));
            }

            if (cl.isActive()) {
                activeComponent = capabilityUrls.get(cl.getLink());
            }
// TODO Hier WFS, ESRI, Google, ...

        }
        if (activeComponent != null) {
            tbpCapabilities.setSelectedComponent(activeComponent);
        }

        // CapabilityList-Baum neu aufbauen
        capabilityList.removeAll();
        JMenu menu = createCapabilitiesListSubmenu(cp.getCapabilitiesListTree());
        for (Component component : menu.getMenuComponents()) {
            capabilityList.add(component);
        }
    }

    /**
     * Erzeugt rekursiv aus einem CapabilitiesListTreeNode ein JMenu mit
     * Untermenues und CapabilityLink-Einträgen.
     *
     * @param node Der Knoten aus dem ein JMenu erzeugt werden soll
     * @return JMenu mit den Menu-Einträgen und Untermenues des Knoten
     */
    private JMenu createCapabilitiesListSubmenu(CapabilitiesListTreeNode node) {
        JMenu menu = new JMenu(node.getTitle());

        // Untermenues rekursiv erzeugen
        for (CapabilitiesListTreeNode subnode : node.getSubnodes()) {
            menu.add(createCapabilitiesListSubmenu(subnode));
        }

        // CapabilityLink-Einträge erzeugen
        for (final CapabilityLink cl : node.getCapabilitiesList().values()) {
            if (cl.getType().equals(CapabilityLink.OGC) || cl.getType().equals(CapabilityLink.OGC_DEPRECATED)) {
                ListMenuItem lmi = new ListMenuItem("test", cl);
                lmi.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addLinkManually(new LinkWithSubparent(cl.getLink(), cl.getSubparent()));
                    }
                });
                menu.add(lmi);
            } else if (cl.getType().equals(CapabilityLink.SEPARATOR)) {
                menu.addSeparator();
            }
            // TODO Hier WFS, ESRI, Google, ...

        }

        // fertig
        return menu;
    }

    /**
     * Entfernt alle vorhandenen Capabilities-B\u00E4ume.
     */
    public void removeAllServer() {
        int mx = tbpCapabilities.getTabCount();
        for (int i = 0; i <
                mx; ++i) {
            removeActiveCapabilityTree();
        }

    }

    /**
     *
     * @param cl
     */
    private void addSubmenuToMenu(final CapabilityLink cl) {
        ListMenuItem lmi = new ListMenuItem("test", cl);
        lmi.addActionListener(new ActionListener() {

            public void actionPerformed(
                    ActionEvent e) {
                addLinkManually(new LinkWithSubparent(cl.getLink(), cl.getSubparent()));
            }
        });
        capabilityList.add(lmi);
    }

    /**
     * 
     */
    public void shownMapBoundsChanged() {
        JTree t = getActiveTree();
        if (t != null) {
            t.repaint();
        }








    }

    class ListMenuItem
            extends JMenuItem {

        private CapabilityLink capabilityLink;

        public ListMenuItem(String label, CapabilityLink cl) {
            super(cl.getTitle());
            this.capabilityLink = cl;
        }

        public CapabilityLink getCapabilityLink() {
            return capabilityLink;
        }

        public void setCapabilityLink(CapabilityLink capabilityLink) {
            this.capabilityLink = capabilityLink;
        }
    }

    class DragTree extends JTree implements DragGestureListener, DragSourceListener {

        DragSource dragSource = null;
        TreePath[] cachedTreePaths; //DND Fehlverhalten Workaround
        private WMSCapabilities wmsCapabilities;

        public DragTree() {
            dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(
                    this, // component where drag originates
                    DnDConstants.ACTION_COPY_OR_MOVE, // actions
                    this); // drag gesture recognizer

            addMouseListener(new MouseAdapter() {                                       //DND Fehlverhalten Workaround

                @Override
                public void mouseReleased(MouseEvent e) {                           //DND Fehlverhalten Workaround

                    cachedTreePaths = getSelectionModel().getSelectionPaths();       //DND Fehlverhalten Workaround

                }
            });

            getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

                public void valueChanged(TreeSelectionEvent e) {
                    if (getSelectionPath() != null && (getSelectionPath().getLastPathComponent() instanceof Layer || getSelectionPath().getLastPathComponent() instanceof Element)) {
                        CismapBroker.getInstance().fireCapabilityLayerChanged(new CapabilityEvent(getSelectionPath().getLastPathComponent()));
                    } else {
                        if (getSelectionPath() != null) {
                          //FIXME: WTF? Warum wan?
                          log.warn("getSelectionPath().getLastPathComponent()=" + getSelectionPath().getLastPathComponent());
                        }
                    }
                }
            });
        }

        public void dragGestureRecognized(DragGestureEvent e) {
            getSelectionModel().setSelectionPaths(cachedTreePaths); //DND Fehlverhalten Workaround

            TreePath selPath = getPathForLocation((int) e.getDragOrigin().getX(), (int) e.getDragOrigin().getY());//DND Fehlverhalten Workaround

            if ((e.getTriggerEvent().getModifiers() & (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)) != 0) {//DND Fehlverhalten Workaround

                getSelectionModel().setSelectionPaths(cachedTreePaths); //DND Fehlverhalten Workaround /

                getSelectionModel().addSelectionPath(selPath);          //DND Fehlverhalten Workaround

                cachedTreePaths = getSelectionModel().getSelectionPaths();//DND Fehlverhalten Workaround

            } else {
                getSelectionModel().setSelectionPath(selPath);//DND Fehlverhalten Workaround

            }

            Transferable trans = null;
            if (this.getModel() instanceof WMSCapabilitiesTreeModel) {
                trans = new DefaultTransferable(new SelectionAndCapabilities(getSelectionModel().getSelectionPaths(), wmsCapabilities, capabilityUrlsReverse.get(tbpCapabilities.getSelectedComponent()).getLink()));
            } else if (this.getModel() instanceof WFSCapabilitiesTreeModel) {
                WFSCapabilitiesTreeModel model = (WFSCapabilitiesTreeModel) this.getModel();
                log.debug("Erstelle Transferable f\u00FCr WFS");
                // TODO ein Transferable zum Testen erstellen
                if (getSelectionModel().getSelectionPath().getLastPathComponent() instanceof ElementDeclaration) {
                    ElementDeclaration element = (ElementDeclaration) getSelectionModel().getSelectionPath().getLastPathComponent();
                    FeatureServiceUtilities utilities = new FeatureServiceUtilities(element.getName().getAsString());

                    //TODO Heuristic HIER zur Bestimmung der Geometrie
                    utilities.setGeometry(FeatureServiceUtilities.getFirstGeometryName(model.getChildren(element)));
                    Vector<String> names = new Vector<String>();
                    for (FeatureServiceAttribute fsa : model.getChildren(element)) {
                        names.add(fsa.getName());
                    }
                    FeatureServiceUtilities.changePropertyNames(utilities.getQuery(), names);
                    trans = new DefaultTransferable(new WFSSelectionAndCapabilities(
                            element.getName().getAsString(),
                            wfsPostUrls.get(tbpCapabilities.getSelectedComponent()).toString(),
                            utilities.getQuery(),
                            "",
                            model.getChildren(element)));
                }
            }
            dragSource.startDrag(e, DragSource.DefaultCopyDrop, trans, this);
        }

        // unbenutzte DnD-Methoden
        public void dragDropEnd(DragSourceDropEvent e) {
        }

        public void dragEnter(DragSourceDragEvent e) {
        }

        public void dragExit(DragSourceEvent e) {
        }

        public void dragOver(DragSourceDragEvent e) {
        }

        public void dropActionChanged(DragSourceDragEvent e) {
        }

        public OGCWebServiceCapabilities getWmsCapabilities() {
            return wmsCapabilities;
        }

        public void setWmsCapabilities(WMSCapabilities wmsCapabilities) {
            this.wmsCapabilities = wmsCapabilities;
        }
    }

    /**
     * Klasse, die Transferable implementiert und den Datentransfer beim
     * Drag&Drop sichert.
     */
    class DefaultTransferable implements Transferable {

        private DataFlavor TREEPATH_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "SelectionAndCapabilities");
        private Object o;

        public DefaultTransferable(Object o) {
            this.o = o;
        }

        /**
         * Returns whether or not the specified data flavor is supported for
         * this object.
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (flavor.match(TREEPATH_FLAVOR)) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Returns an object which represents the data to be transferred.  The class
         * of the object returned is defined by the representation class of the flavor.
         * @param flavor the requested flavor for the data
         * @see DataFlavor#getRepresentationClass
         * @exception IOException                if the data is no longer available
         *              in the requested flavor.
         * @exception UnsupportedFlavorException if the requested data flavor is
         *              not supported.
         */
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.match(TREEPATH_FLAVOR)) {
                return o;
            } else {
                return null;
            }
        }

        /**
         * Returns an array of DataFlavor objects indicating the flavors the data
         * can be provided in.  The array should be ordered according to preference
         * for providing the data (from most richly descriptive to least descriptive).
         * @return an array of data flavors in which this data can be transferred
         */
        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] ar = new DataFlavor[1];
            ar[0] = TREEPATH_FLAVOR;
            return ar;
        }
    }

    class LinkWithSubparent {

        String link = null;
        String subparent = null;

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getSubparent() {
            return subparent;
        }

        public void setSubparent(String subparent) {
            this.subparent = subparent;
        }

        public LinkWithSubparent(String link, String subparent) {
            this.link = link;
            this.subparent = subparent;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LinkWithSubparent) {
                LinkWithSubparent tester = (LinkWithSubparent) obj;
                String t = tester.link + tester.subparent;
                String thisT = link + subparent;
                return t.equals(thisT);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            String t = link + subparent;
            return t.hashCode();
        }
    }
}
