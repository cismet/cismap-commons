/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.layerwidget;

import org.jdom.Element;

import org.openide.util.NbBundle;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import java.util.*;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.*;
import de.cismet.cismap.commons.featureservice.GMLFeatureService;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.preferences.CapabilityLink;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.rasterservice.MapService;

import de.cismet.tools.Static2DTools;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.GUIWindow;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.imagetooltip.ImageToolTip;
import de.cismet.tools.gui.treetable.JTreeTable;
import de.cismet.tools.gui.treetable.TreeTableCellEditor;
import de.cismet.tools.gui.treetable.TreeTableModel;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = GUIWindow.class)
public class LayerWidget extends JPanel implements DropTargetListener, Configurable, GUIWindow {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(LayerWidget.class);
    private static final Map<String, String[]> URL_TRANSFORMATION_RULES = new HashMap<>();
    private static final String SEARCH_STRING = "_search";
    private static final String EXTRACTION_STRING = "_extraction";
    private static final String PREFIX_STRING = "_prefix";
    private static final String POSTFIX_STRING = "_postfix";
    private static final String CONCAT_STRING = "_concat";
    private static final List<String> URL_TRANSFORMATION_RULES_ORDER = new ArrayList<>();

    static {
        try {
            final ResourceBundle transformkRules = ResourceBundle.getBundle("UrlDisplayTransform", Locale.getDefault());

            for (final String key : transformkRules.keySet()) {
                String transformKey = null;
                int index = -1;

                if (key.endsWith(SEARCH_STRING)) {
                    transformKey = key.substring(0, key.length() - SEARCH_STRING.length());
                    index = 0;
                } else if (key.endsWith(EXTRACTION_STRING)) {
                    transformKey = key.substring(0, key.length() - EXTRACTION_STRING.length());
                    index = 1;
                } else if (key.endsWith(PREFIX_STRING)) {
                    transformKey = key.substring(0, key.length() - PREFIX_STRING.length());
                    index = 2;
                } else if (key.endsWith(POSTFIX_STRING)) {
                    transformKey = key.substring(0, key.length() - POSTFIX_STRING.length());
                    index = 3;
                } else if (key.endsWith(CONCAT_STRING)) {
                    transformKey = key.substring(0, key.length() - CONCAT_STRING.length());
                    index = 4;
                }

                if (transformKey != null) {
                    String[] rule = URL_TRANSFORMATION_RULES.get(transformKey);

                    if (rule == null) {
                        rule = new String[5];

                        URL_TRANSFORMATION_RULES.put(transformKey, rule);
                        URL_TRANSFORMATION_RULES_ORDER.add(transformKey);
                    }

                    rule[index] = transformkRules.getString(key);
                }
            }

            Collections.sort(URL_TRANSFORMATION_RULES_ORDER);
        } catch (Exception e) {
            LOG.warn("No rules found to transform urls", e);
        }
    }

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final ActiveLayerModel activeLayerModel = new ActiveLayerModel();
    private JTreeTable treeTable;
    private final int acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
    private Image errorImage;
    private MappingComponent mapC = null;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdCopyToClipboard;
    private javax.swing.JButton cmdDisable;
    private javax.swing.JButton cmdDown;
    private javax.swing.JButton cmdMakeInvisible;
    private javax.swing.JButton cmdRefreshSingleLayer;
    private javax.swing.JButton cmdRemove;
    private javax.swing.JButton cmdTreeCollapse;
    private javax.swing.JButton cmdUp;
    private javax.swing.JButton cmdZoomToFullExtent;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JScrollPane scpMain;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * This constructor should only be used by the Lookup and the init(MappingComponent) should be invoked, when this
     * constructor was used.
     */
    public LayerWidget() {
    }

    /**
     * Creates new form LayerWidget.
     *
     * @param  mapC  DOCUMENT ME!
     */
    public LayerWidget(final MappingComponent mapC) {
        init(mapC);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * To initialise this component, when the default constructor was used.
     *
     * @param  mapC  DOCUMENT ME!
     */
    public void init(final MappingComponent mapC) {
        hackDragAndDropDataFlavors();
        initComponents();
        this.mapC = mapC;
        log.info("LayerWidget: " + activeLayerModel); // NOI18N
        final DropTarget dt = new DropTarget(this, acceptableActions, this);

        treeTable = new JTreeTable(activeLayerModel) {

                @Override
                public JToolTip createToolTip() {
                    if (log.isDebugEnabled()) {
                        log.debug("Tooltip"); // NOI18N
                    }
                    if (getErrorImage() != null) {
                        return new ImageToolTip(getErrorImage());
                    } else {
                        return super.createToolTip();
                    }
                }

                @Override
                public String getToolTipText(final MouseEvent event) {
                    final int row = rowAtPoint(event.getPoint());
                    final int column = columnAtPoint(event.getPoint());

                    if ((row == -1) || (column == -1)) {
                        return null;
                    }

                    // Assume the tree is in the first column
                    if (column == 1) {
                        // Convert table coordinates to tree coordinates
                        final JTree tree = getTree();

                        final Rectangle cellRect = getCellRect(row, column, false);

                        final MouseEvent treeEvent = new MouseEvent(
                                tree,
                                event.getID(),
                                event.getWhen(),
                                event.getModifiersEx(),
                                event.getX()
                                        - cellRect.x,
                                event.getY()
                                        - cellRect.y,
                                event.getClickCount(),
                                event.isPopupTrigger(),
                                event.getButton());

                        final TreePath path = tree.getPathForLocation(event.getX(), event.getY());
                        if (path != null) {
                            final Object node = path.getLastPathComponent();
                            String url = null;

                            url = extractUrlFromServie(node);

                            if (url != null) {
                                return transformURL(url);
                            }
                        }

                        return null;
                    } else {
                        return super.getToolTipText(event);
                    }
                }
            };

        treeTable.setAutoCreateColumnsFromModel(true);
        treeTable.setShowGrid(true);
        treeTable.getTableHeader().setReorderingAllowed(true);
        treeTable.getTree().setShowsRootHandles(true);
        treeTable.getTree().setRootVisible(false);
        treeTable.getTree().setCellRenderer(new ActiveLayerTreeCellRenderer());
        final ActiveLayerTableCellEditor cellEditor = new ActiveLayerTableCellEditor();
        // treeTable.getTree().setCellEditor(cellEditor);
// treeTable.getTree().setEditable(true);
        final TreeTableCellEditor treeTableCellEditor = new TreeTableCellEditor(treeTable, treeTable.getTree());
        treeTableCellEditor.setClickCountToStart(2);
        treeTable.setDefaultEditor(TreeTableModel.class, treeTableCellEditor);
        treeTable.getColumnModel().getColumn(0).setMaxWidth(20);
        treeTable.getColumnModel().getColumn(3).setMaxWidth(50);
        treeTable.getColumnModel().getColumn(5).setMaxWidth(50);
        treeTable.getColumnModel().getColumn(0).setCellEditor(cellEditor);
        treeTable.getColumnModel().getColumn(2).setCellEditor(cellEditor);
        treeTable.getColumnModel().getColumn(3).setCellEditor(cellEditor);
        treeTable.getColumnModel().getColumn(4).setCellEditor(cellEditor);
        treeTable.getColumnModel().getColumn(5).setCellEditor(cellEditor);
        final ActiveLayerTableCellRenderer tableCellRenderer = new ActiveLayerTableCellRenderer();
        treeTable.getColumnModel().getColumn(0).setCellRenderer(tableCellRenderer);
        treeTable.getColumnModel().getColumn(2).setCellRenderer(tableCellRenderer);
        treeTable.getColumnModel().getColumn(3).setCellRenderer(tableCellRenderer);
        treeTable.getColumnModel().getColumn(4).setCellRenderer(tableCellRenderer);
        treeTable.getColumnModel().getColumn(5).setCellRenderer(tableCellRenderer);

//        treeTable.setShowHorizontaLines(true);
        treeTable.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        scpMain.setViewportView(treeTable);

        treeTable.getTree().getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

                @Override
                public void valueChanged(final TreeSelectionEvent e) {
                    if (treeTable.getTree().getSelectionPath() != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("ActiveLayerWidget: selectionChanged()\n" + e); // NOI18N
                        }
                        try {
                            final ActiveLayerEvent ale = new ActiveLayerEvent();
                            ale.setLayer(treeTable.getTree().getSelectionPath().getLastPathComponent());
                            if (ale.getLayer() instanceof WMSServiceLayer) {
                                ale.setCapabilities(((WMSServiceLayer)ale.getLayer()).getWmsCapabilities());
                            }
                            CismapBroker.getInstance().fireLayerSelectionChanged(ale);
                        } catch (Exception ex) {
                            log.error("error while changing the selected layer", ex); // NOI18N
                        }
                    }
                }
            });

        treeTable.addKeyListener(new KeyListener() {

                @Override
                public void keyTyped(final KeyEvent e) {
                }

                @Override
                public void keyPressed(final KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                        cmdRemoveActionPerformed(null);
                        e.consume();
                    }
                }

                @Override
                public void keyReleased(final KeyEvent e) {
                }
            });

        treeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(final ListSelectionEvent e) {
                    final TreePath[] tps = treeTable.getTree().getSelectionPaths();
                    boolean zoomToLayerAllowd = false;

                    if (tps != null) {
                        for (final TreePath tp : tps) {
                            if (tp.getLastPathComponent() instanceof WMSServiceLayer) {
                                final WMSServiceLayer rsl = (WMSServiceLayer)tp.getLastPathComponent();

                                if ((rsl.getLayerInformation() == null)
                                            || (rsl.getLayerInformation().getAbstract() == null)
                                            || !rsl.getLayerInformation().getAbstract().contains(
                                                CapabilityWidget.MASSSTABSBEGRENZUNG)) {
                                    zoomToLayerAllowd = true;
                                    break;
                                } else {
                                    try {
                                        final String abstr = rsl.getLayerInformation().getAbstract();
                                        final String relevantSubString = abstr.substring(
                                                abstr.indexOf(CapabilityWidget.MASSSTABSBEGRENZUNG)
                                                        + CapabilityWidget.MASSSTABSBEGRENZUNG.length());
                                        final StringTokenizer st = new StringTokenizer(relevantSubString, ",");
                                        final List<String> relevantLayerNames = new ArrayList<>();

                                        while (st.hasMoreTokens()) {
                                            final String token = st.nextToken();

                                            if ((token.contains("\""))
                                                        && (token.indexOf("\"") != token.lastIndexOf("\""))) {
                                                relevantLayerNames.add(
                                                    token.substring(token.indexOf("\"") + 1, token.lastIndexOf("\""))
                                                                .toLowerCase());
                                            }
                                        }

                                        final List<WMSLayer> layers = rsl.getWMSLayers();

                                        if (layers != null) {
                                            for (final WMSLayer l : layers) {
                                                if (!relevantLayerNames.contains(l.getStyleName().toLowerCase())) {
                                                    zoomToLayerAllowd = true;
                                                    break;
                                                }
                                            }
                                        }
                                    } catch (Throwable t) {
                                        // do nothing
                                    }
                                }
                            } else {
                                zoomToLayerAllowd = true;
                                break;
                            }
                        }
                    }

                    cmdZoomToFullExtent.setEnabled(zoomToLayerAllowd);
                }
            });

        treeTable.setGridColor(this.getBackground());
        addComponentListener(new ComponentListener() {

                @Override
                public void componentHidden(final ComponentEvent e) {
                }

                @Override
                public void componentMoved(final ComponentEvent e) {
                }

                @Override
                public void componentResized(final ComponentEvent e) {
//                treeTable.repaint();
                }

                @Override
                public void componentShown(final ComponentEvent e) {
                }
            });
        activeLayerModel.addMappingModelListener(new MappingModelListener() {

                @Override
                public void mapServiceLayerStructureChanged(final MappingModelEvent mme) {
                    treeTable.getColumnModel().getColumn(3).getCellEditor().stopCellEditing();
                }

                @Override
                public void mapServiceAdded(final MapService mapService) {
                    treeTable.getColumnModel().getColumn(3).getCellEditor().stopCellEditing();
                }

                @Override
                public void mapServiceRemoved(final MapService mapService) {
                    treeTable.getColumnModel().getColumn(3).getCellEditor().stopCellEditing();
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String extractUrlFromServie(final Object node) {
        String url = null;

        if (node instanceof RetrievalServiceLayer) {
            final RetrievalServiceLayer layer = (RetrievalServiceLayer)node;

            if (node instanceof WMSServiceLayer) {
                final WMSServiceLayer wmsLayer = (WMSServiceLayer)node;
                url = wmsLayer.getServerURI();
            } else if (node instanceof SimpleWMS) {
                url = ((SimpleWMS)layer).getServerURI();
            } else if (node instanceof WebFeatureService) {
                url = ((WebFeatureService)layer).getServerURI();
            } else if (node instanceof GMLFeatureService) {
                url = ((GMLFeatureService)layer).getServerURI();
            } else if (node instanceof ShapeFileFeatureService) {
                url = ((ShapeFileFeatureService)layer).getDocumentURI().toString();
            } else if (node instanceof SimplePostgisFeatureService) {
            } else if (node instanceof H2FeatureService) {
            } else {
            }
        } else if (node instanceof WMSLayer) {
            url = ((WMSLayer)node).getServerURI();
        } else if (node instanceof SimpleWMS) {
            url = ((SimpleWMS)node).getServerURI();
        }

        return url;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   url  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String transformURL(final String url) {
        String result = url;

        for (final String key : URL_TRANSFORMATION_RULES_ORDER) {
            final String[] rule = URL_TRANSFORMATION_RULES.get(key);

            if ((rule[0] != null) && (rule[1] != null)) {
                final Pattern searchPattern = Pattern.compile(rule[0]);
                final Matcher searchMatcher = searchPattern.matcher(url);

                if (searchMatcher.find()) {
                    final Pattern pattern = Pattern.compile(rule[1]);
                    final Matcher matcher = pattern.matcher(result);

                    if (matcher.find()) {
                        result = "";

                        for (int i = 1; i <= matcher.groupCount(); ++i) {
                            result += ((!result.equals("") && (rule[4] != null)) ? rule[4] : "") + matcher.group(i);
                        }

                        return result;
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Kein Match gefunden für: " + url);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        scpMain = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        cmdTreeCollapse = new javax.swing.JButton();
        cmdRefreshSingleLayer = new javax.swing.JButton();
        cmdDown = new javax.swing.JButton();
        cmdUp = new javax.swing.JButton();
        cmdZoomToFullExtent = new javax.swing.JButton();
        cmdDisable = new javax.swing.JButton();
        cmdRemove = new javax.swing.JButton();
        cmdMakeInvisible = new javax.swing.JButton();
        cmdCopyToClipboard = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEtchedBorder(),
                javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        setPreferredSize(new java.awt.Dimension(211, 114));
        setLayout(new java.awt.BorderLayout());

        scpMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scpMain.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        add(scpMain, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jToolBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jToolBar1.setRollover(true);

        cmdTreeCollapse.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/collapseTree.png"))); // NOI18N
        cmdTreeCollapse.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdTreeCollapse.toolTipText"));                                                // NOI18N
        cmdTreeCollapse.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdTreeCollapse.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdTreeCollapseActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdTreeCollapse);

        cmdRefreshSingleLayer.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/refresh.png"))); // NOI18N
        cmdRefreshSingleLayer.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdRefreshSingleLayer.toolTipText"));                                     // NOI18N
        cmdRefreshSingleLayer.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdRefreshSingleLayer.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdRefreshSingleLayerActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdRefreshSingleLayer);

        cmdDown.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/down.png"))); // NOI18N
        cmdDown.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdDown.toolTipText"));                                                // NOI18N
        cmdDown.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdDown.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdDownActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdDown);

        cmdUp.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/up.png")));                       // NOI18N
        cmdUp.setToolTipText(org.openide.util.NbBundle.getMessage(LayerWidget.class, "LayerWidget.cmdUp.toolTipText")); // NOI18N
        cmdUp.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdUp.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdUpActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdUp);

        cmdZoomToFullExtent.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerZoom.png"))); // NOI18N
        cmdZoomToFullExtent.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdZoomToFullExtent.toolTipText"));                                         // NOI18N
        cmdZoomToFullExtent.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdZoomToFullExtent.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdZoomToFullExtentActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdZoomToFullExtent);

        cmdDisable.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disable.png"))); // NOI18N
        cmdDisable.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdDisable.toolTipText"));                                                // NOI18N
        cmdDisable.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdDisable.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdDisableActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdDisable);

        cmdRemove.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/removeLayer.png"))); // NOI18N
        cmdRemove.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdRemove.toolTipText"));                                                     // NOI18N
        cmdRemove.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdRemove.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdRemoveActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdRemove);

        cmdMakeInvisible.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerDLinvisible.png"))); // NOI18N
        cmdMakeInvisible.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdMakeInvisible.toolTipText"));                                                   // NOI18N
        cmdMakeInvisible.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdMakeInvisible.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdMakeInvisibleActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdMakeInvisible);

        cmdCopyToClipboard.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/clipboard16.png"))); // NOI18N
        cmdCopyToClipboard.setToolTipText(org.openide.util.NbBundle.getMessage(
                LayerWidget.class,
                "LayerWidget.cmdCopyToClipboard.toolTipText"));                                            // NOI18N
        cmdCopyToClipboard.setFocusable(false);
        cmdCopyToClipboard.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdCopyToClipboard.setMargin(new java.awt.Insets(2, 1, 2, 1));
        cmdCopyToClipboard.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdCopyToClipboard.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdCopyToClipboardActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdCopyToClipboard);

        jPanel1.add(jToolBar1, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.NORTH);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     */
    public void removeAllLayers() {
        activeLayerModel.removeAllLayers();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdRefreshSingleLayerActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdRefreshSingleLayerActionPerformed
        final TreePath[] tps = treeTable.getTree().getSelectionPaths();
        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    if (tps != null) {
                        for (final TreePath tp : tps) {
                            if ((tp != null) && (tp.getLastPathComponent() instanceof RetrievalServiceLayer)) {
                                ((MapService)tp.getLastPathComponent()).setBoundingBox(
                                    mapC.getCurrentBoundingBoxFromCamera());
                                ((RetrievalServiceLayer)tp.getLastPathComponent()).retrieve(true);
                            } else if ((tp != null)
                                        && (tp.getParentPath().getLastPathComponent() instanceof RetrievalServiceLayer)) {
                                ((RetrievalServiceLayer)tp.getParentPath().getLastPathComponent()).retrieve(true);
                            }
                        }
                    }
                    return null;
                }

                @Override
                protected void done() {
                    treeTable.getTree().setSelectionPaths(tps);
                    StaticSwingTools.jTableScrollToVisible(treeTable, treeTable.getSelectedRow(), 0);
                    treeTable.getTree().setSelectionPaths(tps);
                }
            };

        worker.execute();
    } //GEN-LAST:event_cmdRefreshSingleLayerActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdDownActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdDownActionPerformed
        final TreePath[] tps = treeTable.getTree().getSelectionPaths();

        if (tps != null) {
            Arrays.sort(tps, new Comparator<TreePath>() {

                    @Override
                    public int compare(final TreePath o1, final TreePath o2) {
                        final Integer pos = activeLayerModel.getLayerPosition(o1);

                        return pos.compareTo(activeLayerModel.getLayerPosition(o2));
                    }
                });

            for (final TreePath tp : tps) {
                if (tp != null) {
                    activeLayerModel.moveLayerDown(tp);
                }
                if (EventQueue.isDispatchThread()) {
                    log.warn("InvokeLater in EDT");
                } // NOI18N
            }
        }
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    treeTable.getTree().setSelectionPaths(tps);
                    StaticSwingTools.jTableScrollToVisible(treeTable, treeTable.getSelectedRow(), 0);
                }
            });
    } //GEN-LAST:event_cmdDownActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdUpActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdUpActionPerformed
        final TreePath[] tps = treeTable.getTree().getSelectionPaths();

        if (tps != null) {
            Arrays.sort(tps, new Comparator<TreePath>() {

                    @Override
                    public int compare(final TreePath o1, final TreePath o2) {
                        final Integer pos = activeLayerModel.getLayerPosition(o1);

                        return pos.compareTo(activeLayerModel.getLayerPosition(o2)) * -1;
                    }
                });

            for (final TreePath tp : tps) {
                if (tp != null) {
                    activeLayerModel.moveLayerUp(tp);
                }
                if (EventQueue.isDispatchThread()) {
                    log.warn("InvokeLater in EDT");
                } // NOI18N
            }
        }

        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    treeTable.getTree().setSelectionPaths(tps);
                    StaticSwingTools.jTableScrollToVisible(treeTable, treeTable.getSelectedRow(), 0);
                }
            });
    } //GEN-LAST:event_cmdUpActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdMakeInvisibleActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdMakeInvisibleActionPerformed
        final TreePath[] tps = treeTable.getTree().getSelectionPaths();

        if (tps != null) {
            for (final TreePath tp : tps) {
                if (tp != null) {
                    activeLayerModel.handleVisibility(tp);
                }
            }
        }

        if (EventQueue.isDispatchThread()) {
            log.warn("InvokeLater in EDT");
        } // NOI18N
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    treeTable.getTree().setSelectionPaths(tps);
                }
            });
    } //GEN-LAST:event_cmdMakeInvisibleActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdDisableActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdDisableActionPerformed
        final TreePath[] tps = treeTable.getTree().getSelectionPaths();

        if (tps != null) {
            for (final TreePath tp : tps) {
                if (tp != null) {
                    activeLayerModel.disableLayer(tp);
                }
                if (EventQueue.isDispatchThread()) {
                    log.warn("InvokeLater in EDT");
                } // NOI18N
            }
        }
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    treeTable.getTree().setSelectionPaths(tps);
                }
            });
    } //GEN-LAST:event_cmdDisableActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdTreeCollapseActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdTreeCollapseActionPerformed
        // StaticSwingTools.jTreeCollapseAllNodes(treeTable.getTree());
// int sel = treeTable.getSelectionModel().getMinSelectionIndex();
// if (treeTable.getRowCount() > 0) {
// treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
// treeTable.getSelectionModel().setSelectionInterval(0, treeTable.getRowCount() - 1);
// if (sel == -1) {
// treeTable.getSelectionModel().setSelectionInterval(0, 0);
// } else {
// treeTable.getSelectionModel().setSelectionInterval(sel, sel);
// }
// treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
// }

        treeTable.getColumnModel().getColumn(3).getCellEditor().stopCellEditing();
    } //GEN-LAST:event_cmdTreeCollapseActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdRemoveActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdRemoveActionPerformed
        try {
            final TreePath[] tps = treeTable.getTree().getSelectionPaths();

            if (tps != null) {
                int row = -1;
                for (final TreePath tp : tps) {
                    row = treeTable.getSelectedRow();

                    if (tp != null) {
                        activeLayerModel.removeLayer(tp);
                    }
                }

                final int lastSelectedRow = row;

                final Runnable r = new Runnable() {

                        @Override
                        public void run() {
                            int selectedRow = lastSelectedRow;
                            if (selectedRow >= treeTable.getRowCount()) {
                                selectedRow = treeTable.getRowCount() - 1;
                            }
                            if (selectedRow != -1) {
                                treeTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
                            }
                        }
                    };

                // do invoke later in EDT is required, because the selection should be adopted
                // after the layer is removed
                EventQueue.invokeLater(r);
            }
        } catch (final Exception e) {
            log.error("Error during removal of layer", e);
        }
    } //GEN-LAST:event_cmdRemoveActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdZoomToFullExtentActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdZoomToFullExtentActionPerformed
        final TreePath[] tps = treeTable.getTree().getSelectionPaths();

        final ZoomToLayerWorker worker = new ZoomToLayerWorker(tps, 10);
        worker.execute();
    } //GEN-LAST:event_cmdZoomToFullExtentActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jButton1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton1ActionPerformed
        try {
            final Class classInfo = ClassLoader.getSystemClassLoader()
                        .loadClass("de.cismet.cismap.cidslayer.CidsLayer");
            activeLayerModel.addLayer((RetrievalServiceLayer)classInfo.newInstance());
        } catch (ClassNotFoundException ex) {
            log.error("ClassNotFound", ex);
        } catch (InstantiationException ex) {
            log.error("InstantiationException", ex);
        } catch (IllegalAccessException ex) {
            log.error("IllegalAccessException", ex);
        }
    }                                                                            //GEN-LAST:event_jButton1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdCopyToClipboardActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdCopyToClipboardActionPerformed
        final TreePath tp = treeTable.getTree().getSelectionPath();

        if (tp != null) {
            final String url = extractUrlFromServie(tp.getLastPathComponent());

            if (url != null) {
                final StringSelection selection = new StringSelection(url);
                final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);
            }
        }
    } //GEN-LAST:event_cmdCopyToClipboardActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ActiveLayerModel getMappingModel() {
        return activeLayerModel;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    new Tester().setVisible(true);
                }
            });
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
    public void drop(final java.awt.dnd.DropTargetDropEvent dtde) {
        LayerDropUtils.drop(dtde, activeLayerModel, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dte  DOCUMENT ME!
     */
    @Override
    public void dragExit(final java.awt.dnd.DropTargetEvent dte) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dtde  DOCUMENT ME!
     */
    @Override
    public void dropActionChanged(final java.awt.dnd.DropTargetDragEvent dtde) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dtde  DOCUMENT ME!
     */
    @Override
    public void dragOver(final java.awt.dnd.DropTargetDragEvent dtde) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dtde  DOCUMENT ME!
     */
    @Override
    public void dragEnter(final java.awt.dnd.DropTargetDragEvent dtde) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getScale() {
        return 2.0;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Image getErrorImage() {
        return errorImage;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  errorImage  DOCUMENT ME!
     */
    public void setErrorImage(final Image errorImage) {
        this.errorImage = errorImage;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  NoWriteError  DOCUMENT ME!
     */
    @Override
    @Deprecated
    public Element getConfiguration() throws NoWriteError {
        return activeLayerModel.getConfiguration();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    @Deprecated
    public void masterConfigure(final Element e) {
        activeLayerModel.masterConfigure(e);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    @Deprecated
    public void configure(final Element e) {
        activeLayerModel.configure(e);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e      DOCUMENT ME!
     * @param  merge  DOCUMENT ME!
     */
    @Override
    public void configure(final Element e, final boolean merge) {
        activeLayerModel.configure(e, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   e  DOCUMENT ME!
     * @param   v  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List<String> getCapabilities(final Element e, final List<String> v) {
        try {
            if (e.getName().equals("capabilities") && (e.getAttribute("type") != null)
                        && (e.getAttribute("type").getValue().equals(CapabilityLink.OGC)
                            || e.getAttribute("type").getValue().equals(CapabilityLink.OGC_DEPRECATED))) { // NOI18N
                final String url = e.getTextTrim();
                if (!v.contains(url)) {
                    v.add(url);
                    return v;
                }
            } else {
                final Iterator it = e.getChildren().iterator();
                while (it.hasNext()) {
                    final Object elem = (Object)it.next();
                    if (elem instanceof Element) {
                        getCapabilities((Element)elem, v);
                    }
                }
            }
            return v;
        } catch (Exception ex) {
            return new ArrayList<>(0);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JTreeTable getTreeTable() {
        return treeTable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lastPathComponent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isSlidableWMSServiceLayerGroup(final Object lastPathComponent) {
        final de.cismet.commons.wms.capabilities.deegree.DeegreeLayer layer;

        if (lastPathComponent instanceof de.cismet.commons.wms.capabilities.deegree.DeegreeLayer) {
            layer = (de.cismet.commons.wms.capabilities.deegree.DeegreeLayer)lastPathComponent;
        } else {
            return false;
        }

        final List<String> keywords = Arrays.asList(layer.getKeywords());

        return keywords.contains("cismapSlidingLayerGroup");
    }

    /**
     * This is required to prevent a bug on Macs that causes the first drop operation to fail with an exception,
     * "java.awt.datatransfer.UnsupportedFlavorException: application/x-java-file-list". This bug is related to
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4746177 but only occurs during the first Mac OS drop operation
     * in a Java application.
     */
    private static void hackDragAndDropDataFlavors() {
        final SystemFlavorMap sfm = (SystemFlavorMap)SystemFlavorMap.getDefaultFlavorMap();
        final String nativeValue = "application/x-java-file-list"; // NOI18N
        final DataFlavor dataFlavor = new DataFlavor(
                "application/x-java-file-list; charset=ASCII; class=java.util.List",
                "File List");                                      // NOI18N
        sfm.addUnencodedNativeForFlavor(dataFlavor, nativeValue);
        sfm.addFlavorForUnencodedNative(nativeValue, dataFlavor);
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
        return NbBundle.getMessage(LayerWidget.class, "LayerWidget.getViewTitle");
    }

    @Override
    public Icon getViewIcon() {
        final Icon icoMap = new ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layers.png"));
        return Static2DTools.borderIcon(icoMap, 0, 3, 0, 1);
    }
}
