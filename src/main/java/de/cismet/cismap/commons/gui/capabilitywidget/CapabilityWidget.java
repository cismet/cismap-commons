/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.capabilitywidget;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import org.jdom.Element;

import org.openide.util.NbBundle;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Level;

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

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.capabilities.AbstractCapabilitiesTreeModel;
import de.cismet.cismap.commons.exceptions.ConvertException;
import de.cismet.cismap.commons.featureservice.FeatureServiceUtilities;
import de.cismet.cismap.commons.featureservice.WFSCapabilitiesTreeCellRenderer;
import de.cismet.cismap.commons.featureservice.WFSCapabilitiesTreeModel;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.MapBoundsListener;
import de.cismet.cismap.commons.interaction.events.CapabilityEvent;
import de.cismet.cismap.commons.preferences.CapabilitiesListTreeNode;
import de.cismet.cismap.commons.preferences.CapabilitiesPreferences;
import de.cismet.cismap.commons.preferences.CapabilityLink;
import de.cismet.cismap.commons.raster.wms.WMSCapabilitiesTreeCellRenderer;
import de.cismet.cismap.commons.raster.wms.WMSCapabilitiesTreeModel;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilities;
import de.cismet.cismap.commons.wfs.capabilities.WFSCapabilitiesFactory;
import de.cismet.cismap.commons.wms.capabilities.Envelope;
import de.cismet.cismap.commons.wms.capabilities.Layer;
import de.cismet.cismap.commons.wms.capabilities.LayerBoundingBox;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilitiesFactory;

import de.cismet.security.AccessHandler;
import de.cismet.security.WebAccessManager;

import de.cismet.security.exceptions.RequestFailedException;

import de.cismet.security.handler.WSSAccessHandler;

import de.cismet.tools.CismetThreadPool;

import de.cismet.tools.configuration.Configurable;

import de.cismet.tools.gui.DefaultPopupMenuListener;
import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class CapabilityWidget extends JPanel implements DropTargetListener,
    ChangeListener,
    ActionListener,
    Configurable,
    MapBoundsListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            "de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget"); // NOI18N

    //~ Instance fields --------------------------------------------------------

    int selectedIndex = -1;
    private int maxServerNameLength = 14;
    private ImageIcon icoConnect = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/capabilitywidget/res/connect.png")); // NOI18N
    private ImageIcon icoConnected = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/raster/wms/res/server.png"));            // NOI18N
    private ImageIcon icoError = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/capabilitywidget/res/error.png"));   // NOI18N
    private LinkedHashMap<LinkWithSubparent, JComponent> capabilityUrls =
        new LinkedHashMap<LinkWithSubparent, JComponent>();
    private LinkedHashMap<JComponent, LinkWithSubparent> capabilityUrlsReverse =
        new LinkedHashMap<JComponent, LinkWithSubparent>();
    private int acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
    private HashMap<Component, WMSCapabilities> wmsCapabilities = new HashMap<Component, WMSCapabilities>();
    private HashMap<Component, JTree> wmsCapabilitiesTrees = new HashMap<Component, JTree>();
    private HashMap<Component, WFSCapabilities> wfsCapabilities = new HashMap<Component, WFSCapabilities>();
    private HashMap<Component, JTree> wfsCapabilitiesTrees = new HashMap<Component, JTree>();
    private CapabilitiesPreferences preferences = new CapabilitiesPreferences();
    private JPopupMenu capabilityList = new JPopupMenu();
    private CapabilityWidget thisWidget = null;
    private Element serverElement;
    private JPopupMenu treePopMenu = new JPopupMenu();

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

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form CapabilityWidget.
     */
    public CapabilityWidget() {
        thisWidget = this;
        initComponents();
        tbpCapabilities.addChangeListener(this);
        tbpCapabilities.putClientProperty(Options.NO_CONTENT_BORDER_KEY, Boolean.FALSE);
        tbpCapabilities.setRequestFocusEnabled(false);
        cmdAddFromList.setComponentPopupMenu(capabilityList);
        final DropTarget dt = new DropTarget(this, acceptableActions, this);
        // tbpCapabilities.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT); tbpCapabilities.setUI(new
        // WindowsTabbedPaneUI());

        final JMenuItem pmenuItem = new JMenuItem(NbBundle.getMessage(
                    CapabilityWidget.class,
                    "CapabilityWidget.CapabilityWidget().pmenuItem.text"));
        pmenuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    JTree tree = wmsCapabilitiesTrees.get(tbpCapabilities.getSelectedComponent());
                    if (tree == null) {
                        tree = wfsCapabilitiesTrees.get(tbpCapabilities.getSelectedComponent());
                    }
                    if (tree instanceof DragTree) {
                        zoomToExtent((DragTree)tree);
                    }
                }
            });
        treePopMenu.add(pmenuItem);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Erzeugt ein neues Tab in der TabbedPane und stoesst das parsen der Capabilities-XML an, die ueber den Link
     * ansprechbar ist.
     *
     * @param  link         URL zur Capabilities-XML-Datei
     * @param  subparent    DOCUMENT ME!
     * @param  interactive  true, falls per Drag&Drop, sonst false
     */
    private void processUrl(final String link, final String subparent, final boolean interactive) {
        if (Thread.getDefaultUncaughtExceptionHandler() == null) {
            if (log.isDebugEnabled()) {
                log.debug("uncaught exception handler registered");
            }
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                    @Override
                    public void uncaughtException(final Thread t, final Throwable e) {
                        log.error("uncaughtException: ", e);
                    }
                });
        }
        log.info("processURL: " + link); // NOI18N
        // Gibts diese URL schon?
        // Text im Tab der L\u00E4nge der URL anpassen
        String tabText;
        if ((subparent != null) && (subparent.trim().length() > 0)) {
            tabText = subparent;
        } else {
            if (link.startsWith("http://") && (link.length() > 21)) { // NOI18N
                tabText = link.substring(7, 21) + "...";              // NOI18N
            } else if (link.length() > 14) {
                tabText = link.substring(0, 14) + "...";              // NOI18N
            } else {
                tabText = link;
            }
        }

        final JPanel load = getNewWaitingPanel(tabText);
        final Object test = capabilityUrls.get(new LinkWithSubparent(link, subparent));
        final String tabTextCopy = tabText;
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if ((test != null) && (test instanceof JComponent)) {
                        synchronized (this) {
                            tbpCapabilities.setComponentAt(tbpCapabilities.indexOfComponent((JComponent)test), load);
                        }
                    }
                    capabilityUrls.put(new LinkWithSubparent(link, subparent), load);
                    capabilityUrlsReverse.put(load, new LinkWithSubparent(link, subparent));
                    synchronized (this) {
                        StaticSwingTools.jTabbedPaneWithVerticalTextAddTab(
                            tbpCapabilities,
                            tabTextCopy,
                            icoConnect,
                            load);
                    }
                    tbpCapabilities.setSelectedComponent(load);
                    // setOGCWMSCapabilitiesTree(link, tbpCapabilities.getComponentCount()-1);
                    if (log.isDebugEnabled()) {
                        log.debug(
                            "link.toLowerCase().contains(service=wms)"
                                    + link.toLowerCase().contains("service=wms")
                                    + " link: "
                                    + link.toLowerCase());
                    }
                    // TODO
                    // should be refactored --> coomon parts like capabilities s
                    if (link.toLowerCase().contains("service=wfs")) {                              // NOI18N
                        addOGCWFSCapabilitiesTree(link, load, interactive);
                    } else if (link.toLowerCase().contains("service=wms")) {                       // NOI18N
                        addOGCWMSCapabilitiesTree(link, load, interactive, subparent);
                    } else if (link.toLowerCase().contains("service=wss")) {                       // NOI18N
                        try {
                            if (log.isDebugEnabled()) {
                                log.debug("WSS Capabilties Link hinzugefügt");                     // NOI18N
                            }
                            final URL url = new URL(link.substring(0, link.indexOf('?')));
                            if (log.isDebugEnabled()) {
                                log.debug("URL des WSS: " + url.toString());                       // NOI18N
                            }
                            if (!WebAccessManager.getInstance().isHandlerForURLRegistered(url)) {
                                WebAccessManager.getInstance()
                                        .registerAccessHandler(url, AccessHandler.ACCESS_HANDLER_TYPES.WSS);
                            }
                            addOGCCapabilitiesTree(link, load, interactive);
                        } catch (MalformedURLException ex) {
                            log.error("Url is not wellformed no wss authentication possible", ex); // NOI18N
                        }
                    } else {
                        // ToDo cleveres Probieren wenn z.B. nur die service URL angebenen wurde -->
                        // getCapabiltiesrequest aufbauen und probieren
                        log.info("service nicht spezifizierbar");                                      // NOI18N
                        final Object[] alternatives = {
                                "OGC-Web Mapping Service",
                                "OGC-Web Feature Service",
                                "OGC-Web Security Service"
                            };                                                                         // NOI18N
                        final Object selectedValue = JOptionPane.showInputDialog(
                                StaticSwingTools.getParentFrame(CapabilityWidget.this),
                                org.openide.util.NbBundle.getMessage(
                                    CapabilityWidget.class,
                                    "CapabilityWidget.processUrl(String,String,boolean).JOptionPane.message",
                                    new Object[] { link }),                                            // NOI18N
                                org.openide.util.NbBundle.getMessage(
                                    CapabilityWidget.class,
                                    "CapabilityWidget.processUrl(String,String,boolean).JOptionPane.title"),
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                alternatives,
                                alternatives[0]);                                                      // NOI18N
                        if (selectedValue == alternatives[0]) {
                            addOGCWMSCapabilitiesTree(link, load, interactive, subparent);
                        } else if (selectedValue == alternatives[1]) {
                            addOGCWFSCapabilitiesTree(link, load, interactive);
                        } else if (selectedValue == alternatives[2]) {
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debug("WSS Capabilties Link hinzugefügt");                     // NOI18N
                                }
                                URL url;
                                if (link.indexOf('?') != -1) {
                                    url = new URL(link.substring(0, link.indexOf('?')));
                                } else {
                                    url = new URL(link);
                                }
                                if (log.isDebugEnabled()) {
                                    log.debug("URL des WSS: " + url.toString());                       // NOI18N
                                }
                                if (!WebAccessManager.getInstance().isHandlerForURLRegistered(url)) {
                                    WebAccessManager.getInstance()
                                            .registerAccessHandler(url, AccessHandler.ACCESS_HANDLER_TYPES.WSS);
                                }
                                addOGCCapabilitiesTree(link, load, interactive);
                            } catch (MalformedURLException ex) {
                                log.error("Url is not wellformed no wss authentication possible", ex); // NOI18N
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
     * Erzeugt ein neues Tab in der TabbedPane und st\u00F6\u00DFt das parsen der Capabilities-XML an, die \u00FCber den
     * Link ansprechbar ist. Ruft processUrl(link, true) auf.
     *
     * @param  link       URL zur Capabilities-XML-Datei
     * @param  subparent  DOCUMENT ME!
     */
    public void processUrl(final String link, final String subparent) {
        processUrl(link, subparent, true);
    }

    /**
     * Called when the drag operation has terminated with a drop on the operable part of the drop site for the <code>
     * DropTarget</code> registered with this listener.
     *
     * <p>This method is responsible for undertaking the transfer of the data associated with the gesture. The <code>
     * DropTargetDropEvent</code> provides a means to obtain a <code>Transferable</code> object that represents the data
     * object(s) to be transfered.</p>
     *
     * <P>From this method, the <code>DropTargetListener</code> shall accept or reject the drop via the acceptDrop(int
     * dropAction) or rejectDrop() methods of the <code>DropTargetDropEvent</code> parameter.</P>
     *
     * <P>Subsequent to acceptDrop(), but not before, <code>DropTargetDropEvent</code>'s getTransferable() method may be
     * invoked, and data transfer may be performed via the returned <code>Transferable</code>'s getTransferData()
     * method.</P>
     *
     * <P>At the completion of a drop, an implementation of this method is required to signal the success/failure of the
     * drop by passing an appropriate <code>boolean</code> to the <code>DropTargetDropEvent</code>'s
     * dropComplete(boolean success) method.</P>
     *
     * <P>Note: The data transfer should be completed before the call to the <code>DropTargetDropEvent</code>'s
     * dropComplete(boolean success) method. After that, a call to the getTransferData() method of the <code>
     * Transferable</code> returned by <code>DropTargetDropEvent.getTransferable()</code> is guaranteed to succeed only
     * if the data transfer is local; that is, only if <code>DropTargetDropEvent.isLocalTransfer()</code> returns <code>
     * true</code>. Otherwise, the behavior of the call is implementation-dependent.</P>
     *
     * @param  dtde  the <code>DropTargetDropEvent</code>
     */
    @Override
    public void drop(final DropTargetDropEvent dtde) {
        if (isDropOk(dtde)) {
            final String link = getLink(dtde);
            processUrl(link, null);
        }
    }
    /**
     * restliche unbenutzte DnD-Methoden.
     *
     * @param  dte  DOCUMENT ME!
     */
    @Override
    public void dragExit(final DropTargetEvent dte) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dtde  DOCUMENT ME!
     */
    @Override
    public void dropActionChanged(final DropTargetDragEvent dtde) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dtde  DOCUMENT ME!
     */
    @Override
    public void dragOver(final DropTargetDragEvent dtde) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dtde  DOCUMENT ME!
     */
    @Override
    public void dragEnter(final DropTargetDragEvent dtde) {
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        cmdCollapse = new javax.swing.JButton();
        cmdAddFromList = new javax.swing.JButton();
        cmdAddByUrl = new javax.swing.JButton();
        cmdRemove = new javax.swing.JButton();
        cmdRefresh = new javax.swing.JButton();
        tbpCapabilities = StaticSwingTools.jTabbedPaneWithVerticalTextCreator(
                JTabbedPane.LEFT,
                JTabbedPane.SCROLL_TAB_LAYOUT);

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setPreferredSize(new java.awt.Dimension(200, 250));
        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        jToolBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        cmdCollapse.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/collapseTree.png"))); // NOI18N
        cmdCollapse.setToolTipText(org.openide.util.NbBundle.getMessage(
                CapabilityWidget.class,
                "CapabilityWidget.cmdCollapse.toolTipText"));                                                    // NOI18N
        cmdCollapse.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdCollapse.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdCollapseActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdCollapse);

        cmdAddFromList.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/addServerFromList.png"))); // NOI18N
        cmdAddFromList.setToolTipText(org.openide.util.NbBundle.getMessage(
                CapabilityWidget.class,
                "CapabilityWidget.cmdAddFromList.toolTipText"));                                                      // NOI18N
        cmdAddFromList.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdAddFromList.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdAddFromListActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdAddFromList);

        cmdAddByUrl.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/addServerFromUrl.png"))); // NOI18N
        cmdAddByUrl.setToolTipText(org.openide.util.NbBundle.getMessage(
                CapabilityWidget.class,
                "CapabilityWidget.cmdAddByUrl.toolTipText"));                                                        // NOI18N
        cmdAddByUrl.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdAddByUrl.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdAddByUrlActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdAddByUrl);

        cmdRemove.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/tab_remove.png"))); // NOI18N
        cmdRemove.setToolTipText(org.openide.util.NbBundle.getMessage(
                CapabilityWidget.class,
                "CapabilityWidget.cmdRemove.toolTipText"));                                                    // NOI18N
        cmdRemove.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdRemove.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdRemoveActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdRemove);

        cmdRefresh.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/refresh.png"))); // NOI18N
        cmdRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(
                CapabilityWidget.class,
                "CapabilityWidget.cmdRefresh.toolTipText"));                                                // NOI18N
        cmdRefresh.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdRefresh.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
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
    } // </editor-fold>//GEN-END:initComponents
    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdAddFromListActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdAddFromListActionPerformed
        capabilityList.show(cmdAddFromList, 0, cmdAddFromList.getHeight());
        capabilityList.setVisible(true);
    }                                                                                  //GEN-LAST:event_cmdAddFromListActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdRefreshActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdRefreshActionPerformed
        final JTree active = getActiveTree();
        if (active != null) {
            final LinkWithSubparent link = capabilityUrlsReverse.get(tbpCapabilities.getSelectedComponent());
            addLinkManually(link);
        }
    }                                                                              //GEN-LAST:event_cmdRefreshActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdAddByUrlActionPerformed(final java.awt.event.ActionEvent evt) {       //GEN-FIRST:event_cmdAddByUrlActionPerformed
        final String input = JOptionPane.showInputDialog(
                this,
                org.openide.util.NbBundle.getMessage(
                    CapabilityWidget.class,
                    "CapabilityWidget.cmdAddByUrlActionPerformed().JOptionPane.message"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    CapabilityWidget.class,
                    "CapabilityWidget.cmdAddByUrlActionPerformed().JOptionPane.title"),   // NOI18N
                JOptionPane.INFORMATION_MESSAGE);
        if (input != null) {
            processUrl(input, null, true);
        }
    }                                                                                     //GEN-LAST:event_cmdAddByUrlActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdRemoveActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdRemoveActionPerformed
        removeActiveCapabilityTree();
    }                                                                             //GEN-LAST:event_cmdRemoveActionPerformed

    /**
     * Entfernt einen Capability-Baum aus der TabbedPane.
     */
    private void removeActiveCapabilityTree() {
        final JTree active = getActiveTree();
        if (active != null) {
            if (log.isDebugEnabled()) {
                log.debug("active = " + active); // NOI18N
            }
            // ToDo wenn das hier nur bei einem aktivierten Tree gemacht wird kann der link nicht mehr hinzugefügt
            // werden
            final LinkWithSubparent link = capabilityUrlsReverse.get(tbpCapabilities.getSelectedComponent());
            capabilityUrls.remove(link);
            capabilityUrlsReverse.remove(tbpCapabilities.getSelectedComponent());
            if (wmsCapabilities.get(tbpCapabilities.getSelectedComponent()) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Entferne WMSCapabilities-Tree");  // NOI18N
                }
                wmsCapabilities.remove(tbpCapabilities.getSelectedComponent());
                wmsCapabilitiesTrees.remove(tbpCapabilities.getSelectedComponent());
                tbpCapabilities.remove(tbpCapabilities.indexOfComponent(tbpCapabilities.getSelectedComponent()));
            } else if (wfsCapabilities.get(tbpCapabilities.getSelectedComponent()) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Entferne WFSCapabilities-Tree");  // NOI18N
                }
                wfsCapabilities.remove(tbpCapabilities.getSelectedComponent());
                wfsCapabilitiesTrees.remove(tbpCapabilities.getSelectedComponent());
                tbpCapabilities.remove(tbpCapabilities.indexOfComponent(tbpCapabilities.getSelectedComponent()));
            } else {
                log.warn("Keine Component zum entfernen aktiv"); // NOI18N
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("kein Baum aktiv, entferne selektierten Reiter"); // NOI18N
            }
            if (tbpCapabilities.getSelectedComponent() != null) {
                final LinkWithSubparent link = capabilityUrlsReverse.get(tbpCapabilities.getSelectedComponent());
                capabilityUrls.remove(link);
                capabilityUrlsReverse.remove(tbpCapabilities.getSelectedComponent());
                wmsCapabilities.remove(tbpCapabilities.getSelectedComponent());
                wfsCapabilities.remove(tbpCapabilities.getSelectedComponent());
                tbpCapabilities.remove(tbpCapabilities.getSelectedComponent());
            } else {
                log.warn("The link was not removed from the capabilitiyURLs");
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdCollapseActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdCollapseActionPerformed
        final JTree active = getActiveTree();
        if (active != null) {
            int row = active.getRowCount() - 1;
            while (row > 0) {
                active.collapseRow(row);
                row--;
            }
        }
    }                                                                               //GEN-LAST:event_cmdCollapseActionPerformed

    /**
     * Liefert den momentan selektierten Capabilties-Baum.
     *
     * @return  selektierter Capabilties-Baum
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
     *
     * @param  args  Parameter
     */
    public static void main(final String[] args) {
        try {
            org.apache.log4j.PropertyConfigurator.configure(ClassLoader.getSystemResource(
                    "/de/cismet/cismap/commons/demo/log4j.properties")); // NOI18N
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
        } catch (Exception e) {
            log.warn("Error while setting LookAndFeel", e);              // NOI18N
        }
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final JFrame f = new JFrame();
                    f.getContentPane().add(new CapabilityWidget());
                    f.pack();
                    f.setVisible(true);
                    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                }
            });
    }

    /**
     * Testet, ob ein g\u00FCltiges Objekt in das CapabilityWidget gezogen wurde.
     *
     * @param   e  DropEvent
     *
     * @return  true, falls g\u00FCltiges Objekt, sonst false
     */
    private boolean isDropOk(final DropTargetDropEvent e) {
        if (e.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Extrahiert die URL aus dem auf das Widget gezogenen Objekt.
     *
     * @param   dtde  DropEvent
     *
     * @return  URL als String-Objekt
     */
    private String getLink(final DropTargetDropEvent dtde) {
        String link = null;
        try {
            dtde.acceptDrop(acceptableActions);
            final Object data = dtde.getTransferable().getTransferData(DataFlavor.getTextPlainUnicodeFlavor());
            if (data instanceof InputStream) {
                final InputStream input = (InputStream)data;
                final InputStreamReader isr = new InputStreamReader(input);

                final StringBuffer str = new StringBuffer();
                int in = -1;
                try {
                    while ((in = isr.read()) >= 0) {
                        if (in != 0) {
                            str.append((char)in);
                        }
                    }
                    link = str.toString();
                } catch (IOException ioe) {
                    /*
                     * bug #4094987 sun.io.MalformedInputException: Missing byte-order mark e.g. if dragging from MS
                     * Word 97 to Java still a bug in 1.2 final
                     */
                    System.err.println("cannot read" + ioe);                        // NOI18N
                    dtde.dropComplete(false);
                    final String message = org.openide.util.NbBundle.getMessage(
                            CapabilityWidget.class,
                            "CapabilityWidget.getLink(DropTargetDropEvent).message",
                            new Object[] { ioe.getMessage() });                     // NOI18N
                    JOptionPane.showMessageDialog(
                        this,
                        message,
                        org.openide.util.NbBundle.getMessage(
                            CapabilityWidget.class,
                            "CapabilityWidget.getLink(DropTargetDropEvent).title"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);

                    return null;
                }
            }
            // Wir gehen davon aus, dass der Link Title immer in der 2ten Zeile steht
            try {
                link = link.substring(0, link.indexOf("\n")); // NOI18N
            } catch (Exception e) {
            }
            return link;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return link;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   tabTitle  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private JPanel getNewWaitingPanel(final String tabTitle) {
        final JPanel panLoad = new JPanel();
        final JLabel lblWorld = new JLabel();
        final JLabel lblLoading = new JLabel();
        final JPanel panFillTop = new JPanel();
        final JPanel panFillBottom = new JPanel();
        panLoad.setLayout(new GridBagLayout());
        panLoad.putClientProperty("tabTitle", tabTitle);                                                 // NOI18N
        lblWorld.setHorizontalAlignment(SwingConstants.CENTER);
        lblWorld.setIcon(new ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/load.png"))); // NOI18N
        lblWorld.setVerticalAlignment(SwingConstants.TOP);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        panLoad.add(lblWorld, gridBagConstraints);

        lblLoading.setText(org.openide.util.NbBundle.getMessage(
                CapabilityWidget.class,
                "CapabilityWidget.lblLoading.text")); // NOI18N
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
     * Erzeugt einen WMS-Baum aus der geparsten Capabilities-XML und f\u00FCgt ihn der TabbedPane hinzu.
     *
     * @param  link       Capabilites-URL
     * @param  comp       Component
     * @param  subparent  DOCUMENT ME!
     */
    private void addOGCWMSCapabilitiesTree(final String link, final JComponent comp, final String subparent) {
        addOGCWMSCapabilitiesTree(link, comp, true, subparent);
    }

    /**
     * Falls ein Link manuell \u00FCber einen Dialog eingegeben wurde.
     *
     * @param  link  Capability-Link
     */
    private void addLinkManually(final LinkWithSubparent link) {
        processUrl(link.getLink(), link.getSubparent(), false);
    }

    /**
     * Erzeugt den Baum aus der geparsten Capabilities-XML und f\u00FCgt ihn der TabbedPane hinzu.
     *
     * @param  link         Capabilites-URL
     * @param  comp         Component
     * @param  interactive  true, falls per Drag&Drop, sonst false
     * @param  subparent    DOCUMENT ME!
     */
    private void addOGCWMSCapabilitiesTree(final String link,
            final JComponent comp,
            final boolean interactive,
            final String subparent) {
        if (log.isDebugEnabled()) {
            log.debug("addOGCWMSCapabilitiesTree()"); // NOI18N
        }
        final Runnable t = new Runnable() {

                @Override
                public void run() {
                    try {
                        final DragTree trvCap = new DragTree();
                        final WMSCapabilitiesFactory capFact = new WMSCapabilitiesFactory();
                        final CismapBroker broker = CismapBroker.getInstance();
                        if (log.isDebugEnabled()) {
                            log.debug("Capability Widget: Creating WMScapabilities for URL: " + link); // NOI18N
                        }
                        // final WMSCapabilities cap =
                        // capFact.createCapabilities(HttpAuthentication.getInputStreamReaderFromURL(CapabilityWidget.this,
                        // getCapURL));
                        final WMSCapabilities cap = capFact.createCapabilities(link);
                        if (log.isDebugEnabled()) {
                            log.debug("finished creating Capabilties"); // NOI18N
                        }
                        // TODO for WFS ToDo funktionalität abgeschaltet steckt zur zeit in CismetGUICommons -->
                        // refactoring broker.addHttpCredentialProviderCapabilities(cap,
                        // broker.getHttpCredentialProviderURL(getCapURL)); if (broker.isServerSecuredByPassword(cap)) {
                        // broker.addProperty(getCapURL.toString(), cap.getCapability().getLayer().getTitle()); }
                        trvCap.setWmsCapabilities(cap);
                        final WMSCapabilitiesTreeModel tm = new WMSCapabilitiesTreeModel(cap, subparent);
                        final DropTarget dt = new DropTarget(trvCap, acceptableActions, thisWidget);
                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    addPopupMenu(trvCap);
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
                                    String title = cap.getLayer().getTitle().trim();
                                    if (subparent != null) {
                                        title = subparent;
                                    }
                                    final String titleOrig = title;
                                    if (title.length() > 0) {
                                        if (title.length() > maxServerNameLength) {
                                            title = title.substring(0, maxServerNameLength - 3) + "..."; // NOI18N
                                        }
                                        sPane.putClientProperty("tabTitle", title);                      // NOI18N
                                        synchronized (this) {
                                            StaticSwingTools.jTabbedPaneWithVerticalTextSetNewText(
                                                tbpCapabilities,
                                                title,
                                                icoConnected,
                                                Color.black,
                                                sPane);
                                        }
                                        synchronized (this) {
                                            tbpCapabilities.setToolTipTextAt(
                                                tbpCapabilities.indexOfComponent(sPane),
                                                titleOrig);
                                        }
                                        stateChanged(null);
                                    }
                                }
                            });
                    } catch (Throwable e) {
                        log.error("Fehler während dem Erstellen des WMSCapabilties Baums", e);           // NOI18N
                        String message = "";                                                             // NOI18N

                        tbpCapabilities.setIconAt(tbpCapabilities.indexOfComponent(comp), icoError);
                        if ((e instanceof RequestFailedException) || (e.getMessage() == null)
                                    || e.getMessage().equals("null")) { // NOI18N
                            message = e.getCause().getMessage();
                        } else {
                            message = e.getMessage();
                        }

                        if (interactive) {
                            final ErrorInfo ei = new ErrorInfo(org.openide.util.NbBundle.getMessage(
                                        CapabilityWidget.class,
                                        "CapabilityWidget.addOGCWMSCapabilitiesTree.JOptionPane.title"),   // NOI18N
                                    org.openide.util.NbBundle.getMessage(
                                        CapabilityWidget.class,
                                        "CapabilityWidget.addOGCWMSCapabilitiesTree.JOptionPane.message"), // NOI18N
                                    null,
                                    null,
                                    e,
                                    Level.SEVERE,
                                    null);
                            JXErrorPane.showDialog(thisWidget, ei);
                        }
                        // TODO: Error \u00FCber die Statuszeile bekanntgeben
                        log.error("Error while loading server capabilities: " + message, e); // NOI18N
                        tbpCapabilities.remove(tbpCapabilities.indexOfComponent(comp));

                        final JComponent jc = capabilityUrls.get(new LinkWithSubparent(link, null));
                        capabilityUrls.remove(new LinkWithSubparent(link, null));
                        capabilityUrlsReverse.remove(jc);
                    }
                }
            };
        CismetThreadPool.execute(t);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  link         DOCUMENT ME!
     * @param  comp         DOCUMENT ME!
     * @param  interactive  DOCUMENT ME!
     */
    public void addOGCCapabilitiesTree(final String link, final JComponent comp, final boolean interactive) {
        if (log.isDebugEnabled()) {
            log.debug("addOGCCapabilitiesTree()"); // NOI18N
        }
        final Runnable r = new Runnable() {

                @Override
                public void run() {
                    try {
                        final DragTree trvCap = new DragTree();
                        // ToDo outsource/generalise method
                        // URL-String als URL speichern
                        final URL getCapURL = new URL(link);
                        URL postURL;

                        // WFSCapabilities aus dem \u00FCbergebenen Link (liefert XML-Dok) parsen
                        // log.debug("Versuche WFSCapabilities zu parsen");
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
                        // final AbstractCapabilitiesTreeModel capTreeModel = passwordDialog.getCapabilitiesTree();
                        final DropTarget dt = new DropTarget(trvCap, acceptableActions, thisWidget);

                        final AbstractCapabilitiesTreeModel capTreeModel;
                        // TODO!!! Wenn beim abrufen der Capabillities der neue Server entfernt wird --> kann er nicht
                        // mehr hinzugefügt werden kann

                        final AccessHandler handler = WebAccessManager.getInstance().getHandlerForURL(finalPostUrl);
                        final String securedServiceType = ((WSSAccessHandler)handler).getSecuredServiceTypeForURL(
                                finalPostUrl);
                        if (securedServiceType != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("SecuredServiceType des WSS konnte bestimmt werden: " + securedServiceType); // NOI18N
                            }
                            if (securedServiceType.equals(WSSAccessHandler.SECURED_SERVICE_TYPE.WFS.toString())) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Gesicheter Service ist ein: "
                                                + WSSAccessHandler.SECURED_SERVICE_TYPE.WFS);                          // NOI18N
                                    log.debug("Capability Widget: Creating WFScapabilities for URL: "
                                                + finalPostUrl.toString());                                            // NOI18N
                                }

//                            InputStream result = WebAccessManager.getInstance().doRequest(finalPostUrl, new StringReader("?REQUEST=GetCapabilities&service=WFS"), AccessHandler.ACCESS_METHODS.GET_REQUEST);
//                            final WFSOperator op = new WFSOperator();
//                            final WFSCapabilities capWFS = op.parseWFSCapabilites(new BufferedReader(new InputStreamReader(result)));
//                            log.debug("Erstelle WFSCapabilitiesTreeModel");
                                // !!!ToDo WebAccessMananger testen
                                final WFSCapabilitiesFactory capFact = new WFSCapabilitiesFactory();

                                final WFSCapabilities cap = capFact.createCapabilities(link);
                                final String name = FeatureServiceUtilities.getServiceName(cap);

                                capTreeModel = new WFSCapabilitiesTreeModel(cap);
                                capTreeModel.setServiceName(name);
                                // ((WFSCapabilitiesTreeModel)
                                // capTreeModel).setFeatureTypes(op.getElements(finalPostUrl,
                                // capWFS.getFeatureTypeList()));
                            } else if (securedServiceType.equals(
                                            WSSAccessHandler.SECURED_SERVICE_TYPE.WMS.toString())) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Gesicheter Service ist ein: "
                                                + WSSAccessHandler.SECURED_SERVICE_TYPE.WMS); // NOI18N
                                }
                                try {
                                    final WMSCapabilitiesFactory capFact = new WMSCapabilitiesFactory();
                                    if (log.isDebugEnabled()) {
                                        log.debug("Capability Widget: Creating WMScapabilities for URL: "
                                                    + link);                                  // NOI18N
                                    }
                                    // ToDO Langsam
                                    final WMSCapabilities capWMS = capFact.createCapabilities(link);
                                    if (log.isDebugEnabled()) {
                                        log.debug("Erstelle WMSCapabilitiesTreeModel");  // NOI18N
                                    }
                                    capTreeModel = new WMSCapabilitiesTreeModel(capWMS);
                                    capTreeModel.setServiceName(capWMS.getLayer().getTitle().trim());
                                } catch (Exception ex) {
                                    log.error("Exception during doRequest cause: ", ex); // NOI18N
                                    return;
                                }
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("Gesicherter Service ist von unbekanntem Typ."); // NOI18N
                                }
                                return;
                            }
                        } else {
                            log.warn("SecuredServiceType des WSS konnte nicht bestimmt werden"); // NOI18N
                            return;
                        }

                        // ToDo Listener oder sonstwas damit das retrieval auch abgebrochen wird
                        if (tbpCapabilities.indexOfComponent(comp) == -1) {
                            log.info("Ladepanel ist nicht mehr in TabbedPane --> retrieval wird abgebrochen"); // NOI18N
                            final LinkWithSubparent link = capabilityUrlsReverse.get(comp);
                            capabilityUrls.remove(link);
                            capabilityUrlsReverse.remove(comp);
                            if (wmsCapabilities.get(comp) != null) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Entferne WMSCapabilities-Tree");                                // NOI18N
                                }
                                wmsCapabilities.remove(comp);
                                wmsCapabilitiesTrees.remove(comp);
                                tbpCapabilities.remove(tbpCapabilities.indexOfComponent(comp));
                            } else if (wfsCapabilities.get(comp) != null) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Entferne WFSCapabilities-Tree");                                // NOI18N
                                }
                                wfsCapabilities.remove(comp);
                                wfsCapabilitiesTrees.remove(comp);
                                tbpCapabilities.remove(comp);
                            } else {
                                log.warn("Keine Component zum entfernen aktiv");                               // NOI18N
                            }
                            return;
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Capabilitespanel noch vorhanden --> stelle baum dar");              // NOI18N
                            }
                        }
                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    // broker.addHttpCredentialProviderCapabilities(cap,
                                    // broker.getHttpCredentialProviderURL(url)); ToDO subparent

                                    final String name = capTreeModel.getServiceName();
                                    if (log.isDebugEnabled()) {
                                        log.debug("ServiceName: " + name); // NOI18N
                                    }
                                    trvCap.setModel(capTreeModel);

                                    trvCap.setBorder(new EmptyBorder(
                                            1,
                                            1,
                                            1,
                                            1));
                                    final JScrollPane sPane = new JScrollPane();
                                    sPane.setViewportView(trvCap);
                                    sPane.setBorder(
                                        new EmptyBorder(1, 1, 1, 1));
                                    StaticSwingTools.setNiftyScrollBars(sPane);
                                    synchronized (this) {
                                        tbpCapabilities.setComponentAt(tbpCapabilities.indexOfComponent(comp), sPane);
                                    }
                                    // ToDo generalize --> getCapabilities of AbstractCapabilitiesTreeModel
                                    if (capTreeModel instanceof WMSCapabilitiesTreeModel) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("WMSTree"); // NOI18N
                                        }
                                        wmsCapabilities.put(
                                            sPane,
                                            ((WMSCapabilitiesTreeModel)capTreeModel).getCapabilities());
                                        wmsCapabilitiesTrees.put(sPane, trvCap);
                                        trvCap.setWmsCapabilities(
                                            ((WMSCapabilitiesTreeModel)capTreeModel).getCapabilities());
                                        trvCap.setCellRenderer(new WMSCapabilitiesTreeCellRenderer());
                                        stateChanged(null);
                                    } else if (capTreeModel instanceof WFSCapabilitiesTreeModel) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("WFSTree"); // NOI18N
                                        }
                                        wfsCapabilities.put(
                                            sPane,
                                            ((WFSCapabilitiesTreeModel)capTreeModel).getCapabilities());
                                        wfsCapabilitiesTrees.put(sPane, trvCap);
                                        trvCap.setCellRenderer(new WFSCapabilitiesTreeCellRenderer(name));
                                        stateChanged(null);
                                    } else {
                                        // Throw exception
                                    }

                                    capabilityUrls.put(new LinkWithSubparent(link, null), sPane);
                                    capabilityUrlsReverse.put(sPane,
                                        new LinkWithSubparent(link, null));
                                    String title = name;
                                    final String titleOrig = title;

                                    if (title.length()
                                                > 0) {
                                        if (title.length() > maxServerNameLength) {
                                            title = title.substring(0, maxServerNameLength - 3) + "..."; // NOI18N
                                        }
                                        sPane.putClientProperty("tabTitle", title);                      // NOI18N
                                        synchronized (this) {
                                            StaticSwingTools.jTabbedPaneWithVerticalTextSetNewText(
                                                tbpCapabilities,
                                                title,
                                                icoConnected,
                                                Color.black,
                                                sPane);
                                        }
                                        synchronized (this) {
                                            tbpCapabilities.setToolTipTextAt(
                                                tbpCapabilities.indexOfComponent(sPane),
                                                titleOrig);
                                        }
                                        stateChanged(null);
                                    }
                                }
                            });
                    } catch (Throwable e) {
                        String message = "";                                                             // NOI18N

                        tbpCapabilities.setIconAt(tbpCapabilities.indexOfComponent(comp), icoError);
                        if ((e.getMessage() == null) || e.getMessage().equals("null")) { // NOI18N
                            message = e.getCause().getMessage();
                        } else {
                            message = e.getMessage();
                        }

                        if (interactive) {
                            JOptionPane.showMessageDialog(
                                thisWidget,
                                org.openide.util.NbBundle.getMessage(
                                    CapabilityWidget.class,
                                    "CapabilityWidget.addOGCCapabilitiesTree(String,JComponent,boolean).JOptionPane.message",
                                    new Object[] { message }),                                                               // NOI18N
                                org.openide.util.NbBundle.getMessage(
                                    CapabilityWidget.class,
                                    "CapabilityWidget.addOGCCapabilitiesTree(String,JComponent,boolean).JOptionPane.title"), // NOI18N
                                JOptionPane.ERROR_MESSAGE);
                        }
                        log.error("Error during the loading of the capabilities of the server. " + message, e);              // NOI18N
                        tbpCapabilities.remove(tbpCapabilities.indexOfComponent(comp));

                        final JComponent jc = capabilityUrls.get(new LinkWithSubparent(link, null));
                        capabilityUrls.remove(new LinkWithSubparent(link, null));
                        capabilityUrlsReverse.remove(jc);
                    }
                }
            };

        CismetThreadPool.execute(r);
    }

    /**
     * Erzeugt den Baum aus der geparsten Capabilities-XML und f\u00FCgt ihn der TabbedPane hinzu.
     *
     * @param  link         Capabilites-URL
     * @param  comp         Component
     * @param  interactive  true, falls per Drag&Drop, sonst false
     */
    private void addOGCWFSCapabilitiesTree(final String link,
            final JComponent comp,
            final boolean interactive) {
        if (log.isDebugEnabled()) {
            log.debug("addOGCWFSCapabilitiesTree()"); // NOI18N
        }
        final Runnable t = new Runnable() {

                @Override
                public void run() {
                    try {
                        final DragTree trvCap = new DragTree();
                        // URL-String als URL speichern
                        final URL getCapURL = new URL(link);
                        URL postURL;
                        if (log.isDebugEnabled()) {
// WFSCapabilities aus dem \u00FCbergebenen Link (liefert XML-Dok) parsen
                            log.debug("try to parse WFSCapabilities"); // NOI18N
                        }
                        if (link.indexOf('?') > 0) {
                            postURL = new URL(link.substring(0, link.indexOf('?')));
                        } else {
                            postURL = getCapURL;
                        }

                        final URL finalPostUrl = postURL;
                        final WFSCapabilitiesFactory capFact = new WFSCapabilitiesFactory();
                        final WFSCapabilities cap = capFact.createCapabilities(link);
                        trvCap.setWfsCapabilities(cap);
                        final String name = FeatureServiceUtilities.getServiceName(cap);
                        if (log.isDebugEnabled()) {
                            // Hashmap mit den FeatureLayer-Attributen erzeugen
                            log.debug("create WFSCapabilitiesTreeModel"); // NOI18N
                        }
                        final WFSCapabilitiesTreeModel tm = new WFSCapabilitiesTreeModel(cap);

                        // Den WFSTree als DropTarget spezifizieren
                        final DropTarget dt = new DropTarget(trvCap, acceptableActions, thisWidget);
                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    addPopupMenu(trvCap);
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
                                    stateChanged(null);

                                    capabilityUrls.put(new LinkWithSubparent(link, null), sPane);
                                    capabilityUrlsReverse.put(sPane, new LinkWithSubparent(link, null));
                                    String title = name;
                                    final String titleOrig = title;
                                    if (title.length() > 0) {
                                        if (title.length() > maxServerNameLength) {
                                            title = title.substring(0, maxServerNameLength - 3) + "..."; // NOI18N
                                        }

                                        sPane.putClientProperty("tabTitle", title); // NOI18N
                                        synchronized (this) {
                                            StaticSwingTools.jTabbedPaneWithVerticalTextSetNewText(
                                                tbpCapabilities,
                                                title,
                                                icoConnected,
                                                Color.black,
                                                sPane);
                                        }

                                        synchronized (this) {
                                            tbpCapabilities.setToolTipTextAt(
                                                tbpCapabilities.indexOfComponent(sPane),
                                                titleOrig);
                                        }

                                        stateChanged(null);
                                    }
                                }
                            });
                    } catch (Throwable e) {
                        String message = ""; // NOI18N

                        tbpCapabilities.setIconAt(tbpCapabilities.indexOfComponent(comp), icoError);
                        if ((e.getMessage() == null) || e.getMessage().equals("null")) { // NOI18N
                            if (e.getCause() != null) {
                                message = e.getCause().getMessage();
                            }
                        } else {
                            message = e.getMessage();
                        }

                        if (interactive) {
                            JOptionPane.showMessageDialog(
                                thisWidget,
                                org.openide.util.NbBundle.getMessage(
                                    CapabilityWidget.class,
                                    "CapabilityWidget.addOGCWFSCapabilitiesTree(String,JComponent,boolean).JOptionPane.message",
                                    new Object[] { message }),                                                                  // NOI18N
                                org.openide.util.NbBundle.getMessage(
                                    CapabilityWidget.class,
                                    "CapabilityWidget.addOGCWFSCapabilitiesTree(String,JComponent,boolean).JOptionPane.title"), // NOI18N
                                JOptionPane.ERROR_MESSAGE);
                        }

                        log.error("Loading of the server capabilities failed. " + message, e); // NOI18N
                        tbpCapabilities.remove(tbpCapabilities.indexOfComponent(comp));

                        final JComponent jc = capabilityUrls.get(new LinkWithSubparent(link, null));
                        capabilityUrls.remove(new LinkWithSubparent(link, null));
                        capabilityUrlsReverse.remove(jc);
                    }
                }
            };
        CismetThreadPool.execute(t);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void stateChanged(final ChangeEvent e) {
        if (selectedIndex > -1) {
            try {
                tbpCapabilities.setForegroundAt(selectedIndex, null);
                String t;

                t = (String)((JComponent)tbpCapabilities.getComponentAt(selectedIndex)).getClientProperty("tabTitle"); // NOI18N
                if (t == null) {
                    t = "";                                                                                            // NOI18N
                }

                StaticSwingTools.jTabbedPaneWithVerticalTextSetNewText(
                    tbpCapabilities,
                    t,
                    icoConnected,
                    Color.black,
                    (JComponent)tbpCapabilities.getComponentAt(selectedIndex));
            } catch (Throwable skip) {
                // do nothing
            }
        }
        selectedIndex = tbpCapabilities.getSelectedIndex();
        if (selectedIndex > -1) {
            String t = (String)((JComponent)tbpCapabilities.getComponentAt(selectedIndex)).getClientProperty(
                    "tabTitle"); // NOI18N
            if (t == null) {
                t = "";          // NOI18N
            }

            tbpCapabilities.setForegroundAt(selectedIndex, Color.blue);
            StaticSwingTools.jTabbedPaneWithVerticalTextSetNewText(
                tbpCapabilities,
                t,
                icoConnected,
                Color.blue,
                (JComponent)tbpCapabilities.getComponentAt(selectedIndex));
        }

        if (wmsCapabilities.get(tbpCapabilities.getSelectedComponent()) != null) {
            CismapBroker.getInstance()
                    .fireCapabilityServerChanged(new CapabilityEvent(
                            wmsCapabilities.get(tbpCapabilities.getSelectedComponent())));
        } else if (wfsCapabilities.get(tbpCapabilities.getSelectedComponent()) != null) {
            CismapBroker.getInstance()
                    .fireCapabilityServerChanged(new CapabilityEvent(
                            wfsCapabilities.get(tbpCapabilities.getSelectedComponent())));
        } else {
            if (log.isDebugEnabled()) {
                log.debug(wmsCapabilities); // NOI18N
                log.debug(wfsCapabilities); // NOI18N
            }
        }
    }

    /**
     * Invoked when an action occurs. Loggt das ActionEvent als ERROR.
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        log.error(e);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Element getConfiguration() {
        // TODO Im Moment gibts nur OGC-WMS Links. Da faul ....
        final Element ret = new Element("cismapCapabilitiesPreferences"); // NOI18N
        {
            final Set wmsCapabilitiesSet = capabilityUrls.keySet();
            final Iterator<LinkWithSubparent> it = wmsCapabilitiesSet.iterator();
            final LinkWithSubparent selectedLink = capabilityUrlsReverse.get(tbpCapabilities.getSelectedComponent());
            while (it.hasNext()) {
                final LinkWithSubparent link = it.next();
                final CapabilityLink cl = new CapabilityLink(
                        CapabilityLink.OGC,
                        link.getLink(),
                        link.equals(selectedLink),
                        link.getSubparent());
                ret.addContent(cl.getElement());
            }
        }
        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void masterConfigure(final Element e) {
        serverElement = e;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void configure(final Element e) {
        preferences = new CapabilitiesPreferences(serverElement, e);
        configure(preferences);
    }

    /**
     * Konfiguriert das Widget und bestimmt, welche Capabilities verstanden werden.
     *
     * @param   node  cp CapabilitiesPreferences
     *
     * @return  DOCUMENT ME!
     */
    //J-
    public void configure(final CapabilitiesPreferences cp) {
        removeAllServer();
        JComponent activeComponent = null;
        final Iterator<Integer> it = cp.getCapabilities().keySet().iterator();
        while (it.hasNext()) {
            final Integer i = it.next();
            final CapabilityLink cl = cp.getCapabilities().get(i);
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
        final JMenu menu = createCapabilitiesListSubmenu(cp.getCapabilitiesListTree());
        for (final Component component : menu.getMenuComponents()) {
            capabilityList.add(component);
        }
    }
    //J+

    /**
     * Erzeugt rekursiv aus einem CapabilitiesListTreeNode ein JMenu mit Untermenues und CapabilityLink-Einträgen.
     *
     * @param   node  Der Knoten aus dem ein JMenu erzeugt werden soll
     *
     * @return  JMenu mit den Menu-Einträgen und Untermenues des Knoten
     */
    private JMenu createCapabilitiesListSubmenu(final CapabilitiesListTreeNode node) {
        final JMenu menu = new JMenu(node.getTitle());

        // Untermenues rekursiv erzeugen
        for (final CapabilitiesListTreeNode subnode : node.getSubnodes()) {
            menu.add(createCapabilitiesListSubmenu(subnode));
        }

        // CapabilityLink-Einträge erzeugen
        for (final CapabilityLink cl : node.getCapabilitiesList().values()) {
            if (cl.getType().equals(CapabilityLink.OGC) || cl.getType().equals(CapabilityLink.OGC_DEPRECATED)) {
                final ListMenuItem lmi = new ListMenuItem("test", cl); // NOI18N
                lmi.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(final ActionEvent e) {
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
        final int mx = tbpCapabilities.getTabCount();
        for (int i = 0; i
                    < mx; ++i) {
            removeActiveCapabilityTree();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cl  DOCUMENT ME!
     */
    private void addSubmenuToMenu(final CapabilityLink cl) {
        final ListMenuItem lmi = new ListMenuItem("test", cl); // NOI18N
        lmi.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    addLinkManually(new LinkWithSubparent(cl.getLink(), cl.getSubparent()));
                }
            });
        capabilityList.add(lmi);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void shownMapBoundsChanged() {
        final JTree t = getActiveTree();
        if (t != null) {
            t.repaint();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  trvCap  DOCUMENT ME!
     */
    private void addPopupMenu(final DragTree trvCap) {
        trvCap.addMouseListener(new DefaultPopupMenuListener(treePopMenu));
    }

    /**
     * DOCUMENT ME!
     *
     * @param  currentTrvCap  DOCUMENT ME!
     */
    private void zoomToExtent(final DragTree currentTrvCap) {
        if (log.isDebugEnabled()) {
            log.debug("zoom to extent.");
        }

        if (currentTrvCap != null) {
            final String currentCrs = CismapBroker.getInstance().getSrs().getCode();
            final WMSCapabilities wmsCap = currentTrvCap.getWmsCapabilities();
            final LayerBoundingBox[] boxes = null;
            Envelope bestEnvelope = null;

            // search for a bounding box with the right CRS
            if (wmsCap != null) {
                final TreePath currentPath = currentTrvCap.getSelectionPath();

                if ((currentPath != null) && (currentPath.getLastPathComponent() instanceof Layer)) {
                    bestEnvelope = getEnvelopeForWmsLayer((Layer)currentPath.getLastPathComponent());
                }

                if (bestEnvelope == null) {
                    bestEnvelope = getEnvelopeForWmsCaps(wmsCap);
                }
            } else if (currentTrvCap.getWfsCapabilities() != null) {
                // the selected server is a wfs
                final TreePath currentPath = currentTrvCap.getSelectionPath();
                FeatureType selectedFeature = null;

                if ((currentPath != null) && (currentPath.getLastPathComponent() instanceof FeatureType)) {
                    selectedFeature = (FeatureType)currentPath.getLastPathComponent();
                } else {
                    try {
                        final Iterator<FeatureType> it = currentTrvCap.getWfsCapabilities()
                                    .getFeatureTypeList()
                                    .iterator();

                        if (it.hasNext()) {
                            selectedFeature = it.next();
                        }
                    } catch (final Exception e) {
                        log.error("Cannot receive the feature type list from the capabilities document", e);
                    }
                }

                if (selectedFeature != null) {
                    bestEnvelope = getEnvelopeFromFeatureType(selectedFeature);
                }
            }

            if (bestEnvelope == null) {
                log.warn("no envelope found in the capabilities document");
                JOptionPane.showMessageDialog(
                    null,
                    NbBundle.getMessage(CapabilityWidget.class, "CapabilityWidget.zoomToExtent().JOptionPane.msg"),
                    NbBundle.getMessage(
                        CapabilityWidget.class,
                        "CapabilityWidget.zoomToExtent().JOptionPane.title"),
                    JOptionPane.ERROR_MESSAGE);
            }

            try {
                if (bestEnvelope != null) {
                    BoundingBox bb = null;
                    if (bestEnvelope instanceof LayerBoundingBox) {
                        if (((LayerBoundingBox)bestEnvelope).getSRS().equals(currentCrs)) {
                            bb = new BoundingBox(bestEnvelope.getMin().getX(),
                                    bestEnvelope.getMin().getY(),
                                    bestEnvelope.getMax().getX(),
                                    bestEnvelope.getMax().getY());
                        } else {
                            final Envelope env = bestEnvelope.transform(
                                    currentCrs,
                                    ((LayerBoundingBox)bestEnvelope).getSRS());
                            bb = new BoundingBox(env.getMin().getX(),
                                    env.getMin().getY(),
                                    env.getMax().getX(),
                                    env.getMax().getY());
                        }
                    } else {
                        final Envelope env = bestEnvelope.transform(currentCrs, "EPSG:4326");
                        bb = new BoundingBox(env.getMin().getX(),
                                env.getMin().getY(),
                                env.getMax().getX(),
                                env.getMax().getY());
                    }

                    if (bb != null) {
                        CismapBroker.getInstance().getMappingComponent().gotoBoundingBoxWithHistory(bb);
                    } else {
                        log.warn("no valid bounding box found.");
                    }
                }
            } catch (ConvertException e) {
                log.error("Cannot transform coordinates", e);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   l  currentTrvCap DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Envelope getEnvelopeForWmsLayer(final Layer l) {
        Envelope bestEnvelope = null;
        final LayerBoundingBox[] boxes = l.getBoundingBoxes();
        final String currentCrs = CismapBroker.getInstance().getSrs().getCode();

        // search for a bounding box with the right CRS
        if (boxes != null) {
            for (final LayerBoundingBox tmp : boxes) {
                if (tmp.getSRS().equals(currentCrs)) {
                    bestEnvelope = tmp;
                    break;
                }
            }
        }

        if (bestEnvelope == null) {
            if (l.getLatLonBoundingBoxes() != null) {
                bestEnvelope = l.getLatLonBoundingBoxes();
            } else {
                if ((boxes != null) && (boxes.length > 0)) {
                    bestEnvelope = boxes[0];
                }
            }
        }

        return bestEnvelope;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   caps  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Envelope getEnvelopeForWmsCaps(final WMSCapabilities caps) {
        Envelope bestEnvelope = null;

        if (caps != null) {
            final LayerBoundingBox[] boxes = caps.getLayer().getBoundingBoxes();
            final String currentCrs = CismapBroker.getInstance().getSrs().getCode();

            // search for a bounding box with the right CRS
            if (boxes != null) {
                for (final LayerBoundingBox tmp : boxes) {
                    if (tmp.getSRS().equals(currentCrs)) {
                        bestEnvelope = tmp;
                    }
                }
            }

            if (bestEnvelope == null) {
                if (caps.getLayer().getLatLonBoundingBoxes() != null) {
                    bestEnvelope = caps.getLayer().getLatLonBoundingBoxes();
                } else {
                    if ((boxes != null) && (boxes.length > 0)) {
                        bestEnvelope = boxes[0];
                    }
                }
            }
        }

        return bestEnvelope;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  currentTrvCap DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Envelope getEnvelopeFromFeatureType(final FeatureType feature) {
        Envelope bestEnvelope = null;

        if (feature != null) {
            final Envelope[] envs = feature.getWgs84BoundingBoxes();
            if ((envs != null) && (envs.length > 0)) {
                bestEnvelope = envs[0];
            }
        }

        return bestEnvelope;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class ListMenuItem extends JMenuItem {

        //~ Instance fields ----------------------------------------------------

        private CapabilityLink capabilityLink;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ListMenuItem object.
         *
         * @param  label  DOCUMENT ME!
         * @param  cl     DOCUMENT ME!
         */
        public ListMenuItem(final String label, final CapabilityLink cl) {
            super(cl.getTitle());
            this.capabilityLink = cl;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public CapabilityLink getCapabilityLink() {
            return capabilityLink;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  capabilityLink  DOCUMENT ME!
         */
        public void setCapabilityLink(final CapabilityLink capabilityLink) {
            this.capabilityLink = capabilityLink;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class DragTree extends JTree implements DragGestureListener, DragSourceListener {

        //~ Instance fields ----------------------------------------------------

        DragSource dragSource = null;
        TreePath[] cachedTreePaths; // DND Fehlverhalten Workaround
        private WMSCapabilities wmsCapabilities;
        private WFSCapabilities wfsCapabilities;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DragTree object.
         */
        public DragTree() {
            dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(
                this,                             // component where drag originates
                DnDConstants.ACTION_COPY_OR_MOVE, // actions
                this);                            // drag gesture recognizer

            addMouseListener(new MouseAdapter() { // DND Fehlverhalten Workaround

                    @Override
                    public void mouseReleased(final MouseEvent e) { // DND Fehlverhalten Workaround

                        cachedTreePaths = getSelectionModel().getSelectionPaths(); // DND Fehlverhalten Workaround
                    }
                });

            getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

                    @Override
                    public void valueChanged(final TreeSelectionEvent e) {
                        if ((getSelectionPath() != null)
                                    && ((getSelectionPath().getLastPathComponent() instanceof Layer)
                                        || (getSelectionPath().getLastPathComponent() instanceof Element)
                                        || (getSelectionPath().getLastPathComponent() instanceof FeatureType))) {
                            CismapBroker.getInstance()
                                    .fireCapabilityLayerChanged(
                                        new CapabilityEvent(getSelectionPath().getLastPathComponent()));
                        } else {
                            if (getSelectionPath() != null) {
                                // FIXME: WTF? Warum wan?
                                log.warn(
                                    "getSelectionPath().getLastPathComponent()="
                                            + getSelectionPath().getLastPathComponent()); // NOI18N
                            }
                        }
                    }
                });
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  e  DOCUMENT ME!
         */
        @Override
        public void dragGestureRecognized(final DragGestureEvent e) {
            getSelectionModel().setSelectionPaths(cachedTreePaths); // DND Fehlverhalten Workaround

            final TreePath selPath = getPathForLocation((int)e.getDragOrigin().getX(), (int)e.getDragOrigin().getY()); // DND Fehlverhalten Workaround

            if ((e.getTriggerEvent().getModifiers() & (InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)) != 0) { // DND Fehlverhalten Workaround

                getSelectionModel().setSelectionPaths(cachedTreePaths); // DND Fehlverhalten Workaround /

                getSelectionModel().addSelectionPath(selPath); // DND Fehlverhalten Workaround

                cachedTreePaths = getSelectionModel().getSelectionPaths(); // DND Fehlverhalten Workaround
            } else {
                getSelectionModel().setSelectionPath(selPath);             // DND Fehlverhalten Workaround
            }

            Transferable trans = null;
            if (this.getModel() instanceof WMSCapabilitiesTreeModel) {
                trans = new DefaultTransferable(new SelectionAndCapabilities(
                            getSelectionModel().getSelectionPaths(),
                            wmsCapabilities,
                            capabilityUrlsReverse.get(tbpCapabilities.getSelectedComponent()).getLink()));
            } else if (this.getModel() instanceof WFSCapabilitiesTreeModel) {
                final WFSCapabilitiesTreeModel model = (WFSCapabilitiesTreeModel)this.getModel();
                if (log.isDebugEnabled()) {
                    log.debug("create Transferable for WFS"); // NOI18N
                }
                // TODO ein Transferable zum Testen erstellen
                if (getSelectionModel().getSelectionPath().getLastPathComponent() instanceof FeatureType) {
                    final TreePath[] paths = getSelectionModel().getSelectionPaths();
                    final FeatureType[] features = new FeatureType[paths.length];

                    for (int i = 0; i < paths.length; ++i) {
                        features[i] = (FeatureType)paths[i].getLastPathComponent();
                    }

                    trans = new DefaultTransferable(new WFSSelectionAndCapabilities(features));
                }
            }
            dragSource.startDrag(e, DragSource.DefaultCopyDrop, trans, this);
        }

        /**
         * unbenutzte DnD-Methoden.
         *
         * @param  e  DOCUMENT ME!
         */
        @Override
        public void dragDropEnd(final DragSourceDropEvent e) {
        }

        /**
         * DOCUMENT ME!
         *
         * @param  e  DOCUMENT ME!
         */
        @Override
        public void dragEnter(final DragSourceDragEvent e) {
        }

        /**
         * DOCUMENT ME!
         *
         * @param  e  DOCUMENT ME!
         */
        @Override
        public void dragExit(final DragSourceEvent e) {
        }

        /**
         * DOCUMENT ME!
         *
         * @param  e  DOCUMENT ME!
         */
        @Override
        public void dragOver(final DragSourceDragEvent e) {
        }

        /**
         * DOCUMENT ME!
         *
         * @param  e  DOCUMENT ME!
         */
        @Override
        public void dropActionChanged(final DragSourceDragEvent e) {
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public WMSCapabilities getWmsCapabilities() {
            return wmsCapabilities;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  wmsCapabilities  DOCUMENT ME!
         */
        public void setWmsCapabilities(final WMSCapabilities wmsCapabilities) {
            this.wmsCapabilities = wmsCapabilities;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public WFSCapabilities getWfsCapabilities() {
            return wfsCapabilities;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  wfsCapabilities  DOCUMENT ME!
         */
        public void setWfsCapabilities(final WFSCapabilities wfsCapabilities) {
            this.wfsCapabilities = wfsCapabilities;
        }
    }

    /**
     * Klasse, die Transferable implementiert und den Datentransfer beim Drag&Drop sichert.
     *
     * @version  $Revision$, $Date$
     */
    class DefaultTransferable implements Transferable {

        //~ Instance fields ----------------------------------------------------

        private DataFlavor TREEPATH_FLAVOR = new DataFlavor(
                DataFlavor.javaJVMLocalObjectMimeType,
                "SelectionAndCapabilities"); // NOI18N
        private Object o;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DefaultTransferable object.
         *
         * @param  o  DOCUMENT ME!
         */
        public DefaultTransferable(final Object o) {
            this.o = o;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * Returns whether or not the specified data flavor is supported for this object.
         *
         * @param   flavor  the requested flavor for the data
         *
         * @return  boolean indicating whether or not the data flavor is supported
         */
        @Override
        public boolean isDataFlavorSupported(final DataFlavor flavor) {
            if (flavor.match(TREEPATH_FLAVOR)) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Returns an object which represents the data to be transferred. The class of the object returned is defined by
         * the representation class of the flavor.
         *
         * @param      flavor  the requested flavor for the data
         *
         * @return     DOCUMENT ME!
         *
         * @exception  UnsupportedFlavorException  if the requested data flavor is not supported.
         * @exception  IOException                 if the data is no longer available in the requested flavor.
         *
         * @see        DataFlavor#getRepresentationClass
         */
        @Override
        public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.match(TREEPATH_FLAVOR)) {
                return o;
            } else {
                return null;
            }
        }

        /**
         * Returns an array of DataFlavor objects indicating the flavors the data can be provided in. The array should
         * be ordered according to preference for providing the data (from most richly descriptive to least
         * descriptive).
         *
         * @return  an array of data flavors in which this data can be transferred
         */
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            final DataFlavor[] ar = new DataFlavor[1];
            ar[0] = TREEPATH_FLAVOR;
            return ar;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class LinkWithSubparent {

        //~ Instance fields ----------------------------------------------------

        String link = null;
        String subparent = null;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LinkWithSubparent object.
         *
         * @param  link       DOCUMENT ME!
         * @param  subparent  DOCUMENT ME!
         */
        public LinkWithSubparent(final String link, final String subparent) {
            this.link = link;
            this.subparent = subparent;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getLink() {
            return link;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  link  DOCUMENT ME!
         */
        public void setLink(final String link) {
            this.link = link;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getSubparent() {
            return subparent;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  subparent  DOCUMENT ME!
         */
        public void setSubparent(final String subparent) {
            this.subparent = subparent;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   obj  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof LinkWithSubparent) {
                final LinkWithSubparent tester = (LinkWithSubparent)obj;
                final String t = tester.link + tester.subparent;
                final String thisT = link + subparent;
                return t.equals(thisT);
            } else {
                return false;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int hashCode() {
            final String t = link + subparent;
            return t.hashCode();
        }
    }
}
