/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.attributetable;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.type.OrientationEnum;
import net.sf.jasperreports.view.JRSaveContributor;
import net.sf.jasperreports.view.JRViewer;

import org.apache.log4j.Logger;

import org.deegree.io.shpapi.shape_new.ShapeFile;
import org.deegree.io.shpapi.shape_new.ShapeFileWriter;
import org.deegree.model.feature.FeatureCollection;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;
import java.io.FileFilter;

import java.lang.reflect.Method;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.text.DateFormat;
import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureWithId;
import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.features.ModifiableFeature;
import de.cismet.cismap.commons.features.PermissionProvider;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.style.BasicStyle;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToFeaturesWorker;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToLayerWorker;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.retrieval.RepaintEvent;
import de.cismet.cismap.commons.retrieval.RepaintListener;
import de.cismet.cismap.commons.tools.ExportCsvDownload;
import de.cismet.cismap.commons.tools.ExportDbfDownload;
import de.cismet.cismap.commons.tools.ExportDownload;
import de.cismet.cismap.commons.tools.ExportShapeDownload;
import de.cismet.cismap.commons.tools.ExportTxtDownload;
import de.cismet.cismap.commons.tools.FeatureTools;
import de.cismet.cismap.commons.tools.GeometryUtils;
import de.cismet.cismap.commons.tools.SimpleFeatureCollection;
import de.cismet.cismap.commons.util.SelectionChangedEvent;
import de.cismet.cismap.commons.util.SelectionChangedListener;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.commons.concurrency.CismetConcurrency;

import de.cismet.math.geometry.StaticGeometryFunctions;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;
import de.cismet.tools.gui.downloadmanager.DownloadManager;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AttributeTable extends javax.swing.JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(AttributeTable.class);
    private static final int MAX_COLUMN_SIZE = 200;
    private static List<FeatureServiceFeature> clipboard;
    /** is used to refresh the paste button (butPaste). */
    private static final List<AttributeTable> instances = new ArrayList<AttributeTable>();

    //~ Instance fields --------------------------------------------------------

    boolean featureDeleted = false;

    private final AbstractFeatureService featureService;
    // bb will be null, if the featureService has no geometries
    private XBoundingBox bb;
    private int pageSize = 40;
    private int currentPage = 1;
    private int itemCount;
    private CustomTableModel model;
    private int popupColumn;
    private MappingComponent mappingComponent;
    private boolean selectionChangeFromMap = false;
//    private final FeatureCollectionListener featureCollectionListener;
    private final SelectionChangedListener featureSelectionChangedListener;
    private final ListSelectionListener listSelectionListener;
    private final RepaintListener repaintListener;
    private final HashSet<FeatureServiceFeature> lockedFeatures = new HashSet<FeatureServiceFeature>();
    private AttributeTableRuleSet tableRuleSet = new DefaultAttributeTableRuleSet();
    private final FeatureLockingInterface locker;
    private final Map<FeatureServiceFeature, Object> lockingObjects = new HashMap<FeatureServiceFeature, Object>();
    private AttributeTableSearchPanel searchPanel;
    private AttributeTableFieldCalculation calculationDialog;
    private Object query;
    private int[] lastRows;
    private final TreeSet<DefaultFeatureServiceFeature> modifiedFeatures = new TreeSet<DefaultFeatureServiceFeature>();
    private final TreeSet<FeatureServiceFeature> allFeaturesToDelete = new TreeSet<FeatureServiceFeature>();
    private final TreeSet<DefaultFeatureServiceFeature> newFeatures = new TreeSet<DefaultFeatureServiceFeature>();
    private final TreeSet<DefaultFeatureServiceFeature> rejectedNewFeatures =
        new TreeSet<DefaultFeatureServiceFeature>();
    private Object selectionEventSource = null;
    private List<ListSelectionListener> selectionListener = new ArrayList<ListSelectionListener>();
    private TreeSet<Feature> shownAsLocked = new TreeSet<Feature>();
    private String lastExportPath = null;
    private boolean tableLock = false;
    private boolean setNewModel = false;
    private boolean makeEditable = false;
    private boolean resetSelection = false;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgSortOrder1;
    private javax.swing.ButtonGroup bgSortOrder2;
    private javax.swing.ButtonGroup bgSortOrder3;
    private javax.swing.ButtonGroup bgSortOrder4;
    private javax.swing.JButton btnFirstPage;
    private javax.swing.JButton btnLastPage;
    private javax.swing.JButton btnNextPage;
    private javax.swing.JButton btnPrevPage;
    private javax.swing.JButton butAttrib;
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butCancel1;
    private javax.swing.JButton butClearSelection;
    private javax.swing.JButton butColWidth;
    private javax.swing.JButton butCopy;
    private javax.swing.JButton butDelete;
    private javax.swing.JButton butExpOk;
    private javax.swing.JButton butExpOk1;
    private javax.swing.JButton butExport;
    private javax.swing.JButton butInvertSelection;
    private javax.swing.JButton butMoveSelectedRows;
    private javax.swing.JButton butOk;
    private javax.swing.JButton butPaste;
    private javax.swing.JButton butPrint;
    private javax.swing.JButton butPrintPreview;
    private javax.swing.JButton butSearch;
    private javax.swing.JButton butSelectAll;
    private javax.swing.JButton butShowCols;
    private javax.swing.JButton butUndo;
    private javax.swing.JButton butZoomToSelection;
    private javax.swing.JComboBox cbCol1;
    private javax.swing.JComboBox cbCol2;
    private javax.swing.JComboBox cbCol3;
    private javax.swing.JComboBox cbCol4;
    private javax.swing.JDialog diaExport;
    private javax.swing.JDialog diaSort;
    private javax.swing.JDialog diaStatistic;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JComboBox jcFormat;
    private javax.swing.JPanel jpControl;
    private javax.swing.JLabel labSegHint;
    private javax.swing.JLabel labSortCol1;
    private javax.swing.JLabel labSortCol2;
    private javax.swing.JLabel labSortCol3;
    private javax.swing.JLabel labSortCol4;
    private javax.swing.JLabel labStat;
    private javax.swing.JLabel labStatCol;
    private javax.swing.JLabel labWaitingImage;
    private javax.swing.JLabel lblCountLab;
    private javax.swing.JLabel lblCountVal;
    private javax.swing.JLabel lblFormat;
    private javax.swing.JLabel lblMaxLab;
    private javax.swing.JLabel lblMaxVal;
    private javax.swing.JLabel lblMeanLab;
    private javax.swing.JLabel lblMeanVal;
    private javax.swing.JLabel lblMinLab;
    private javax.swing.JLabel lblMinVal;
    private javax.swing.JLabel lblNullLab;
    private javax.swing.JLabel lblNullVal;
    private javax.swing.JLabel lblStdDeviationLab;
    private javax.swing.JLabel lblStdDeviationVal;
    private javax.swing.JLabel lblSumLab;
    private javax.swing.JLabel lblSumVal;
    private javax.swing.JLabel lblTotalPages;
    private javax.swing.JMenuItem miFeldberechnung;
    private javax.swing.JMenuItem miSortieren;
    private javax.swing.JMenuItem miSpalteAusblenden;
    private javax.swing.JMenuItem miSpaltenUmbenennen;
    private javax.swing.JMenuItem miStatistik;
    private javax.swing.JMenuItem mniAdvancedSorting;
    private javax.swing.JPanel panHint;
    private javax.swing.JPanel panWaiting;
    private javax.swing.JRadioButton radOrderAsc1;
    private javax.swing.JRadioButton radOrderAsc2;
    private javax.swing.JRadioButton radOrderAsc3;
    private javax.swing.JRadioButton radOrderAsc4;
    private javax.swing.JRadioButton radOrderDesc1;
    private javax.swing.JRadioButton radOrderDesc2;
    private javax.swing.JRadioButton radOrderDesc3;
    private javax.swing.JRadioButton radOrderDesc4;
    private org.jdesktop.swingx.JXTable table;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JToggleButton tbProcessing;
    private javax.swing.JTextField txtCurrentPage;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form AttributeTable.
     *
     * @param  featureService  DOCUMENT ME!
     */
    public AttributeTable(final AbstractFeatureService featureService) {
        this.featureService = featureService;
        initComponents();
        miFeldberechnung.setVisible(false);
        miSortieren.setVisible(false);
        butAttrib.setVisible(false);
        tbProcessing.setEnabled(featureService.isEditable());
        butSearch.setVisible(false);
        butUndo.setVisible(false);
        butDelete.setVisible(featureService.isEditable());
        locker = FeatureLockerFactory.getInstance().getLockerForFeatureService(featureService);
        table.setTransferHandler(new AttributeTableTransferHandler(this));
        table.setDragEnabled(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // to consume the mouse events. Otherwise, the table behind it will handle the events
        panWaiting.addMouseListener(new MouseAdapter() {
            });
        tableRuleSet = featureService.getLayerProperties().getAttributeTableRuleSet();

        final Collection<? extends AttributeTableSearchPanel> panelList = Lookup.getDefault()
                    .lookupAll(AttributeTableSearchPanel.class);

        if ((panelList != null) && (panelList.size() > 0)) {
            searchPanel = panelList.toArray(new AttributeTableSearchPanel[panelList.size()])[0];
            butAttrib.setVisible(true);
        }

        final Collection<? extends AttributeTableFieldCalculation> calculatorList = Lookup.getDefault()
                    .lookupAll(AttributeTableFieldCalculation.class);

        if ((calculatorList != null) && (calculatorList.size() > 0)) {
            calculationDialog = calculatorList.toArray(new AttributeTableFieldCalculation[calculatorList.size()])[0];
            miFeldberechnung.setVisible(true);
        }

        jcFormat.setModel(new DefaultComboBoxModel(
                new Object[] {
                    new ExportTxtDownload(),
                    new ExportCsvDownload(),
                    new ExportShapeDownload(),
                    new ExportDbfDownload()
                }));

        setPartialTable(featureService.getMaxFeaturesPerPage() > 0);

        table.getTableHeader().addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(final MouseEvent e) {
                    mouseProcessed(e);
                }

                @Override
                public void mouseReleased(final MouseEvent e) {
                    mouseProcessed(e);
                }

                @Override
                public void mousePressed(final MouseEvent e) {
                    final TableColumn col = ((JTableHeader)e.getSource()).getResizingColumn();

                    if (col != null) {
                        butColWidth.setEnabled(true);
                    }
                    mouseProcessed(e);
                }

                private void mouseProcessed(final MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupColumn = table.getTableHeader().getColumnModel().getColumnIndexAtX(e.getX());
                        popupColumn = table.convertColumnIndexToModel(popupColumn);

                        miStatistik.setEnabled(model.isNumeric(popupColumn));
                        miFeldberechnung.setEnabled(isFieldCalculationAllowed());
                        jPopupMenu1.show((Component)e.getSource(), e.getX(), e.getY());
                    }
                }
            });
        listSelectionListener = new ListSelectionListener() {

                @Override
                public void valueChanged(final ListSelectionEvent e) {
                    final boolean rowsSelected = table.getSelectedRows().length > 0;
                    butCopy.setEnabled(rowsSelected);
                    butDelete.setEnabled(rowsSelected);
                    butClearSelection.setEnabled(rowsSelected);
                    butMoveSelectedRows.setEnabled(rowsSelected);
                    butZoomToSelection.setEnabled(rowsSelected);
                    butDelete.setEnabled(isDeleteButtonEnabled());
                    miFeldberechnung.setEnabled(isFieldCalculationAllowed());

                    if (!e.getValueIsAdjusting() && !setNewModel && !makeEditable) {
                        if (!selectionChangeFromMap) {
                            SelectionManager.getInstance()
                                    .setSelectedFeaturesForService(featureService, getSelectedFeatures());
//                            SelectionManager.getInstance().featureSelectionChanged(null);
                        }

                        if (tbProcessing.isSelected()) {
                            final int[] rows = table.getSelectedRows();

                            if (!Arrays.equals(lastRows, rows) && !resetSelection) {
                                final WaitingDialogThread wdt = new WaitingDialogThread(
                                        StaticSwingTools.getFirstParentFrame(AttributeTable.this),
                                        true,
                                        NbBundle.getMessage(
                                            AttributeTable.class,
                                            "AttributeTable.ListSelectionListener.text"),
                                        null,
                                        500,
                                        true) {

                                        @Override
                                        protected Object doInBackground() throws Exception {
                                            wd.setMax(rows.length);
                                            int progress = 0;

                                            try {
                                                CismapBroker.getInstance()
                                                        .getMappingComponent()
                                                        .setSelectionInProgress(true);

                                                for (final int row : rows) {
                                                    final FeatureServiceFeature feature =
                                                        model.getFeatureServiceFeature(
                                                            table.convertRowIndexToModel(row));

                                                    if (!((feature instanceof PermissionProvider)
                                                                    && !((PermissionProvider)feature)
                                                                    .hasWritePermissions())) {
                                                        makeEditable = true;
                                                        try {
                                                            makeFeatureEditable(feature, wd);
                                                        } finally {
                                                            makeEditable = false;
                                                        }
                                                    }
                                                    wd.setProgress(++progress);
                                                    if (canceled) {
                                                        return null;
                                                    }
                                                }
                                                CismapBroker.getInstance().getMappingComponent().showHandles(false);
                                            } finally {
                                                CismapBroker.getInstance()
                                                        .getMappingComponent()
                                                        .setSelectionInProgress(false);
                                                final List<FeatureServiceFeature> features = getSelectedFeatures();
                                                SelectionManager.getInstance().fireSelectionChangedEvent();
                                                // if a feature cannot be selected, because it is already locked, then
                                                // this feature will be removed from the selection during the
                                                // synchonisation with the map. To avoid this, the selection must be set
                                                // again
                                                resetSelection = true;
                                                try {
                                                    setSelection(features);
                                                } finally {
                                                    resetSelection = false;
                                                }
                                            }
                                            return null;
                                        }
                                    };
                                try {
//                                    EventQueue.invokeAndWait(new Thread() {
//
//                                            @Override
//                                            public void run() {
//                                                wdt.start();
//                                            }
//                                        });
                                    if (!EventQueue.isDispatchThread()) {
                                        final int progress = 0;

                                        for (final int row : rows) {
                                            final FeatureServiceFeature feature = model.getFeatureServiceFeature(
                                                    table.convertRowIndexToModel(row));

                                            if (!((feature instanceof PermissionProvider)
                                                            && !((PermissionProvider)feature).hasWritePermissions())) {
                                                makeEditable = true;
                                                try {
                                                    makeFeatureEditable(feature, null);
                                                } finally {
                                                    makeEditable = false;
                                                }
                                            }
                                        }
                                    } else {
                                        wdt.start();
                                    }
                                } catch (Exception ex) {
                                    LOG.error("Error while locking features", ex);
                                }
                            }
                            lastRows = rows;
                        }

                        table.repaint();

                        for (final ListSelectionListener l : selectionListener) {
                            if (l != selectionEventSource) {
                                l.valueChanged(e);
                            }
                        }
                    }
                }
            };

        table.getSelectionModel().addListSelectionListener(listSelectionListener);

        table.setDefaultRenderer(String.class, new AttributeTableCellRenderer());
        table.setDefaultRenderer(Boolean.class, new AttributeTableCellRenderer());
        table.setDefaultRenderer(Date.class, new AttributeTableCellRenderer());
        table.setDefaultRenderer(Number.class, new NumberCellRenderer());

        txtCurrentPage.setText("1");

        loadModel(currentPage);

        final ColorHighlighter base = new CustomColorHighlighter(
                HighlightPredicate.ALWAYS,
//                HighlightPredicate.EVEN,
                new Color(255, 255, 255),
                null);
//        final ColorHighlighter alternate = new CustomColorHighlighter(
//                HighlightPredicate.ODD,
//                new Color(235, 235, 235),
//                null);
//        final Highlighter alternateRowHighlighter = new CompoundHighlighter(base, alternate);

        ((JXTable)table).setHighlighters(base);
//        ((JXTable)table).setHighlighters(alternateRowHighlighter);

        featureSelectionChangedListener = new SelectionChangedListener() {

                @Override
                public void selectionChanged(final SelectionChangedEvent event) {
                    if (makeEditable) {
                        return;
                    }
                    selectionEventSource = event.getSource();
                    selectionChangeFromMap = true;
                    setSelection(SelectionManager.getInstance().getSelectedFeatures(featureService));
                    selectionChangeFromMap = false;
                    selectionEventSource = null;
                }
            };

        SelectionManager.getInstance().addSelectionChangedListener(featureSelectionChangedListener);

        repaintListener = new RepaintListener() {

                @Override
                public void repaintStart(final RepaintEvent e) {
                }

                @Override
                public void repaintComplete(final RepaintEvent e) {
                    if (e.getRetrievalEvent().getRetrievalService().equals(featureService)) {
                        synchronizeTableSeletionWithMap();
                    }
                }

                @Override
                public void repaintError(final RepaintEvent e) {
                }
            };

        table.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(final MouseEvent e) {
                    if ((tableRuleSet != null) && !tbProcessing.isSelected()) {
                        int row = table.rowAtPoint(e.getPoint());

                        if (row != -1) {
                            int col = table.getTableHeader().getColumnModel().getColumnIndexAtX(e.getX());
                            col = table.convertColumnIndexToModel(col);
                            final String columnName = model.getColumnAttributeName(col);
                            row = table.convertRowIndexToModel(row);
                            final Object value = model.getValueAt(row, col);
                            final FeatureServiceFeature f = model.getFeatureServiceFeature(row);

                            tableRuleSet.mouseClicked(f, columnName, value, e.getClickCount());
                        }
                    }
                }
            });
        instances.add(this);
        butPaste.setEnabled(isPasteButtonEnabled());
        final boolean rowsSelected = table.getSelectedRows().length > 0;
        butCopy.setEnabled(rowsSelected);
        butDelete.setEnabled(rowsSelected);
        butClearSelection.setEnabled(rowsSelected);
        butMoveSelectedRows.setEnabled(rowsSelected);
        butZoomToSelection.setEnabled(rowsSelected);
        butShowCols.setEnabled(false);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  true, iff the field calculation is allowed
     */
    private boolean isFieldCalculationAllowed() {
        boolean columnEditable = true;

        if (tableRuleSet != null) {
            columnEditable = tableRuleSet.isColumnEditable(model.getColumnAttributeName(popupColumn));
        }

        final int[] selectedRows = table.getSelectedRows();
        boolean editableColsSelected = (selectedRows == null) || (selectedRows.length == 0);

        if (selectedRows != null) {
            for (final int row : selectedRows) {
                final FeatureServiceFeature feature = model.getFeatureServiceFeature(table.convertRowIndexToModel(row));

                if (feature.isEditable()) {
                    editableColsSelected = true;
                    break;
                }
            }
        }

        return tbProcessing.isSelected() && columnEditable && editableColsSelected;
    }

    /**
     * A table is partial, if not the whole table will be shown at once, but in several pages.
     *
     * @param  partial  DOCUMENT ME!
     */
    private void setPartialTable(final boolean partial) {
        if (partial) {
            pageSize = featureService.getMaxFeaturesPerPage();
            butPrintPreview.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-preview_red.png")));
            butPrint.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-print_red.png")));
            butExport.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-export_red.png")));
            butAttrib.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-search_red.png")));
            butSelectAll.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource(
                        "/de/cismet/cismap/commons/gui/attributetable/res/icon-selectionadd_red.png")));
            butInvertSelection.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource(
                        "/de/cismet/cismap/commons/gui/attributetable/res/icon-selectionintersect_red.png")));
            butMoveSelectedRows.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource(
                        "/de/cismet/cismap/commons/gui/attributetable/res/icon-thissideup_red.png")));
            butZoomToSelection.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-resize_red.png")));
        } else {
            pageSize = -1;
            jpControl.setVisible(false);
            panHint.setVisible(false);
            butPrintPreview.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-preview.png")));
            butPrint.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-print.png")));
            butExport.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-export.png")));
            butAttrib.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-search.png")));
            butSelectAll.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource(
                        "/de/cismet/cismap/commons/gui/attributetable/res/icon-selectionadd.png")));
            butInvertSelection.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource(
                        "/de/cismet/cismap/commons/gui/attributetable/res/icon-selectionintersect.png")));
            butMoveSelectedRows.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource(
                        "/de/cismet/cismap/commons/gui/attributetable/res/icon-thissideup.png")));
            butZoomToSelection.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-resize.png")));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    public void addModifiedFeature(final DefaultFeatureServiceFeature feature) {
        modifiedFeatures.add(feature);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectedFeatures  DOCUMENT ME!
     */
    private void setSelection(final List<? extends Feature> selectedFeatures) {
        if (model == null) {
            return;
        }
        final List<FeatureServiceFeature> tableFeatures = model.getFeatureServiceFeatures();

        table.getSelectionModel().setValueIsAdjusting(true);
        final int[] selectedRows = table.getSelectedRows();
        Arrays.sort(selectedRows);
        final Comparator<Feature> featureComp = new Comparator<Feature>() {

                @Override
                public int compare(final Feature o1, final Feature o2) {
                    if ((o1 instanceof FeatureWithId) && (o2 instanceof FeatureWithId)) {
                        return ((FeatureWithId)o1).getId() - ((FeatureWithId)o2).getId();
                    } else if ((o1 == null) && (o2 == null)) {
                        return 0;
                    } else if ((o1 == null) && (o2 != null)) {
                        return -1;
                    } else if ((o1 != null) && (o2 == null)) {
                        return 1;
                    } else {
                        return o1.toString().compareTo(o2.toString());
                    }
                }
            };
        if (selectedFeatures != null) {
            Collections.sort(selectedFeatures, featureComp);
        }

        for (int index = 0; index < tableFeatures.size(); ++index) {
            final FeatureServiceFeature feature = tableFeatures.get(table.convertRowIndexToModel(index));
            final boolean contained = (selectedFeatures != null)
                        && (Collections.binarySearch(selectedFeatures, feature, featureComp) >= 0);
            final boolean selected = Arrays.binarySearch(selectedRows, index) >= 0;

            if (contained && !selected) {
                table.addRowSelectionInterval(index, index);
            } else if (!contained && selected) {
                table.removeRowSelectionInterval(index, index);
            }
        }

        table.getSelectionModel().setValueIsAdjusting(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  enabled  DOCUMENT ME!
     */
    public void setExportEnabled(final boolean enabled) {
        butExport.setVisible(enabled);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isPasteButtonEnabled() {
        boolean enabled = false;

        if ((clipboard != null) && tbProcessing.isSelected() && featureService.isEditable()) {
            for (final FeatureServiceFeature feature : clipboard) {
                final String geomType = featureService.getLayerProperties().getFeatureService().getGeometryType();
                if ((geomType != null) && !geomType.equals(AbstractFeatureService.UNKNOWN)) {
                    try {
                        final Class geomTypeClass = Class.forName("com.vividsolutions.jts.geom." + geomType);

                        if ((geomTypeClass != null)
                                    && ((feature.getGeometry() != null)
                                        && geomTypeClass.isInstance(feature.getGeometry()))) {
                            enabled = true;
                            break;
                        } else {
                            String compGeoType;

                            if (geomType.startsWith("Multi")) {
                                compGeoType = geomType.substring("Multi".length());
                            } else {
                                compGeoType = "Multi" + geomType;
                            }

                            try {
                                final Class otherGeomTypeClass = Class.forName("com.vividsolutions.jts.geom."
                                                + compGeoType);

                                if ((otherGeomTypeClass != null)
                                            && ((feature.getGeometry() != null)
                                                && otherGeomTypeClass.isInstance(feature.getGeometry()))) {
                                    enabled = true;
                                    break;
                                }
                            } catch (ClassNotFoundException e) {
                                enabled = false;
                            }
                        }
                    } catch (Exception e) {
                        if (geomType.equals(AbstractFeatureService.NONE) && (feature.getGeometry() == null)) {
                            enabled = true;
                        }
                    }
                } else if ((geomType == null) && (feature.getGeometry() == null)) {
                    enabled = true;
                    break;
                }
            }
        }

        return enabled;
    }

    /**
     * DOCUMENT ME!
     */
    private void enableDisableButtons() {
        butUndo.setEnabled(isUndoButtonEnabled());
        butPaste.setEnabled(tbProcessing.isSelected());
        butDelete.setEnabled(isDeleteButtonEnabled());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isUndoButtonEnabled() {
        refreshModifiedFeaturesSet();

        return tbProcessing.isSelected() && !modifiedFeatures.isEmpty();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isDeleteButtonEnabled() {
        if (!tbProcessing.isSelected()) {
            return false;
        }
        boolean hasWritePermission = false;
        final int[] selectedIndices = table.getSelectedRows();

        for (final int index : selectedIndices) {
            final int modelIndex = table.convertRowIndexToModel(index);

            final FeatureServiceFeature f = model.getFeatureServiceFeature(modelIndex);

            if (f != null) {
                if (f instanceof PermissionProvider) {
                    if (((PermissionProvider)f).hasWritePermissions()) {
                        hasWritePermission = true;
                        break;
                    }
                } else {
                    hasWritePermission = true;
                    break;
                }
            }
        }

        return hasWritePermission && (selectedIndices.length > 0);
    }

    /**
     * DOCUMENT ME!
     */
    private void refreshModifiedFeaturesSet() {
        for (FeatureServiceFeature feature : lockedFeatures) {
            if ((feature instanceof ModifiableFeature) && (feature instanceof DefaultFeatureServiceFeature)) {
                final FeatureServiceFeature featureFromModel = model.getFeatureServiceFeatureById(feature.getId());
                if (featureFromModel != null) {
                    feature = featureFromModel;
                }
                if (((ModifiableFeature)feature).isFeatureChanged()) {
                    modifiedFeatures.add((DefaultFeatureServiceFeature)feature);
                }
            }
        }
    }

    /**
     * synchronizes the table selection with the PFeatures in the map.
     */
    private void synchronizeTableSeletionWithMap() {
        final List<PFeature> features = new ArrayList<PFeature>();
        final SelectionListener sl = (SelectionListener)mappingComponent.getInputEventListener()
                    .get(MappingComponent.SELECT);
        features.addAll(featureService.getPNode().getChildrenReference());
        final int[] selectedFeatures = table.getSelectedRows();
        final int[] selectedFeatureIds = new int[selectedFeatures.length];

        for (int i = 0; i < selectedFeatures.length; ++i) {
            selectedFeatureIds[i] = model.getFeatureServiceFeature(
                    table.convertRowIndexToModel(selectedFeatures[i])).getId();
        }
        Arrays.sort(selectedFeatureIds);

        for (final PFeature pfeature : features) {
            final Feature feature = pfeature.getFeature();

            if (feature instanceof FeatureWithId) {
                final boolean selected = Arrays.binarySearch(
                        selectedFeatureIds,
                        ((FeatureWithId)feature).getId()) >= 0;

                if (selected != pfeature.isSelected()) {
                    pfeature.setSelected(selected);
                }
                if (selected) {
                    sl.addSelectedFeature(pfeature);
                } else {
                    sl.removeSelectedFeature(pfeature);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public AbstractFeatureService getFeatureService() {
        return this.featureService;
    }

    /**
     * Should be invoked, before the window with the AttributeTable is closed. This method checks, if there are unsaved
     * changes
     *
     * @return  DOCUMENT ME!
     */
    public boolean dispose() {
        refreshModifiedFeaturesSet();

        if (tbProcessing.isSelected() && !modifiedFeatures.isEmpty()) {
            final int ans = JOptionPane.showConfirmDialog(
                    AttributeTable.this,
                    NbBundle.getMessage(
                        AttributeTable.class,
                        "AttributeTable.addWindowListener().text",
                        featureService.getName()),
                    NbBundle.getMessage(AttributeTable.class, "AttributeTable.addWindowListener().title"),
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if (ans == JOptionPane.YES_OPTION) {
                if (!saveChangedRows(true, true)) {
                    return false;
                }
            } else if (ans == JOptionPane.NO_OPTION) {
                model.setEditable(false);
                AttributeTableFactory.getInstance().processingModeChanged(featureService, tbProcessing.isSelected());
            } else {
                return false;
            }
        } else if (tbProcessing.isSelected()) {
            model.setEditable(false);
            AttributeTableFactory.getInstance().processingModeChanged(featureService, tbProcessing.isSelected());
        }
        SelectionManager.getInstance().removeSelectionChangedListener(featureSelectionChangedListener);
//        mappingComponent.getFeatureCollection().removeFeatureCollectionListener(featureCollectionListener);
        mappingComponent.removeRepaintListener(repaintListener);
        instances.remove(this);
        table.getSelectionModel().removeListSelectionListener(listSelectionListener);
        selectionListener.clear();
        model.setNewFeatureList(new ArrayList<FeatureServiceFeature>());
        model = null;

        if (calculationDialog != null) {
            calculationDialog = null;
        }
        return true;
    }

    /**
     * Locks the given feature, if a corresponding locker exists and make the feature editable.
     *
     * @param  feature  the feature to make editable
     * @param  parent   DOCUMENT ME!
     */
    public void makeFeatureEditable(final FeatureServiceFeature feature, final Component parent) {
        if ((feature instanceof PermissionProvider) && (feature.getId() > 0)) {
            final PermissionProvider pp = (PermissionProvider)feature;

            if (!pp.hasWritePermissions()) {
                JOptionPane.showMessageDialog(
                    this,
                    NbBundle.getMessage(AttributeTable.class, "AttributeTable.makeFeatureEditable.noPermissions.text"),
                    NbBundle.getMessage(AttributeTable.class, "AttributeTable.makeFeatureEditable.noPermissions.title"),
                    JOptionPane.ERROR_MESSAGE);

                return;
            }
        }

        if ((feature != null) && !feature.isEditable()) {
            if (!shownAsLocked.contains(feature)) {
                try {
                    if ((locker != null) && !tableLock) {
                        if (!lockingObjects.containsKey(feature)) {
                            lockingObjects.put(feature, locker.lock(feature, false));
                        }
                    }
                    feature.setEditable(true);
                    if (!lockedFeatures.contains(feature)) {
                        lockedFeatures.add(feature);
                        ((DefaultFeatureServiceFeature)feature).addPropertyChangeListener(model);
                    }
                } catch (LockAlreadyExistsException ex) {
                    shownAsLocked.add(feature);
                    Component c = parent;
                    if (c == null) {
                        c = AttributeTable.this;
                    }

                    JOptionPane.showMessageDialog(
                        c,
                        NbBundle.getMessage(
                            AttributeTable.class,
                            "AttributeTable.ListSelectionListener.valueChanged().lockexists.message",
                            feature.getId(),
                            ex.getLockMessage()),
                        NbBundle.getMessage(
                            AttributeTable.class,
                            "AttributeTable.ListSelectionListener.valueChanged().lockexists.title"),
                        JOptionPane.ERROR_MESSAGE);
                    shownAsLocked.add(feature);
                } catch (Exception ex) {
                    LOG.error("Error while locking feature.", ex);
                    JOptionPane.showMessageDialog(
                        AttributeTable.this,
                        NbBundle.getMessage(
                            AttributeTable.class,
                            "AttributeTable.ListSelectionListener.valueChanged().exception.message",
                            ex.getMessage()),
                        NbBundle.getMessage(
                            AttributeTable.class,
                            "AttributeTable.ListSelectionListener.valueChanged().exception.title"),
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Locks the given feature, if a corresponding locker exists and make the feature editable.
     *
     * @param  features  the feature to make editable
     */
    public void makeFeaturesEditable(final List<FeatureServiceFeature> features) {
        boolean cannotLockAll = false;

        for (final FeatureServiceFeature f : new ArrayList<FeatureServiceFeature>(features)) {
            if ((f instanceof PermissionProvider) && (f.getId() > 0)) {
                final PermissionProvider pp = (PermissionProvider)f;

                if (!pp.hasWritePermissions()) {
                    cannotLockAll = true;
                    features.remove(f);
                }
            }
        }
        final List<FeatureServiceFeature> featuresToLock = new ArrayList<FeatureServiceFeature>();

        for (final FeatureServiceFeature feature : features) {
            if ((feature != null) && !feature.isEditable()) {
                if (!shownAsLocked.contains(feature)) {
                    featuresToLock.add(feature);
                }
            }
        }
    }

    /**
     * Adds the given feature to the attribute table. The feature must be of the same type as the other features in the
     * model
     *
     * @param  feature  FeatureServiceFeature the feature to add
     */
    public void addFeature(final FeatureServiceFeature feature) {
        if (model != null) {
            model.addFeature(feature);
            final int index = model.getRowCount() - 1;
            table.getSelectionModel().addSelectionInterval(index, index);
            modifiedFeatures.add((DefaultFeatureServiceFeature)feature);
            newFeatures.add((DefaultFeatureServiceFeature)feature);
            butMoveSelectedRowsActionPerformed(null);
        }
    }

    /**
     * Adds the given feature to the attribute table. The feature must be of the same type as the other features in the
     * model
     *
     * @param  features  FeatureServiceFeature the feature to add
     */
    public void addFeatures(final List<FeatureServiceFeature> features) {
        if (model != null) {
            final int index = model.getRowCount();
            model.addFeatures(features);

            for (final FeatureServiceFeature feature : features) {
                modifiedFeatures.add((DefaultFeatureServiceFeature)feature);
                newFeatures.add((DefaultFeatureServiceFeature)feature);
            }

            table.getSelectionModel().addSelectionInterval(index, model.getRowCount() - 1);
            butMoveSelectedRowsActionPerformed(null);
        }
    }

    /**
     * The given feature must exist in the attribute table and will be handled as new features (They will be removed, if
     * the table will not be saved).
     *
     * @param  feature  DOCUMENT ME!
     */
    public void markFeatureAsNewFeature(final DefaultFeatureServiceFeature feature) {
        newFeatures.add(feature);
    }

    /**
     * Load the model to show into the table.
     *
     * @param  page  the page to show. At the moment, all data will be displayed on one page
     */
    private void loadModel(final int page) {
        if (!lockingObjects.isEmpty()) {
            LOG.warn("Cannot reload the model of the AttributeTable, because there are unsaved objects.");
            return;
        }

        panWaiting.setVisible(true);
        ((JXBusyLabel)labWaitingImage).setBusy(true);
        setNewModel = true;

        final SwingWorker<List<FeatureServiceFeature>, Void> worker =
            new SwingWorker<List<FeatureServiceFeature>, Void>() {

                @Override
                protected List<FeatureServiceFeature> doInBackground() throws Exception {
                    Thread.currentThread().setName("AttributeTable loadModel");
                    final Object serviceQuery = ((query == null) ? featureService.getQuery() : query);

                    final Geometry g = ZoomToLayerWorker.getServiceBounds(featureService);

                    if (g != null) {
                        bb = new XBoundingBox(g);

                        try {
                            final CrsTransformer transformer = new CrsTransformer(CismapBroker.getInstance().getSrs()
                                            .getCode());
                            bb = transformer.transformBoundingBox(bb);

                            // the transformation from 4326 to the local crs is not precise
                            // enough (espacially for point layer). So add a 1 percent buffer
                            bb.increase(1);
                        } catch (Exception e) {
                            LOG.error("Cannot transform CRS.", e);
                        }
                    } else {
                        bb = null;
                    }

                    if ((pageSize != -1) && (itemCount == 0)) {
                        setItemCount(featureService.getFeatureCount(serviceQuery, bb));
                        if ((itemCount / pageSize) <= 1) {
                            pageSize = -1;
                            setPartialTable(false);
                        }
                    }

                    final FeatureFactory factory = featureService.getFeatureFactory();
                    List<FeatureServiceFeature> featureList;

                    if (pageSize != -1) {
                        List<FeatureServiceAttribute> orderBy = null;

                        List<? extends RowSorter.SortKey> keys = null;

                        if ((table != null) && (table.getRowSorter() != null)) {
                            keys = table.getRowSorter().getSortKeys();
                        }

                        if ((keys != null) && !keys.isEmpty()) {
                            orderBy = new ArrayList<FeatureServiceAttribute>();
                            for (final RowSorter.SortKey key : keys) {
                                final SortOrder order = key.getSortOrder();
                                final int colIndex = key.getColumn();

                                final String attributeName = model.getColumnAttributeName(
                                        table.convertColumnIndexToModel(colIndex));
                                final FeatureServiceAttribute attr = (FeatureServiceAttribute)
                                    featureService.getFeatureServiceAttributes().get(attributeName);
                                attr.setAscOrder(SortOrder.ASCENDING.equals(order));

                                if ((pageSize == -1) || orderBy.isEmpty()) { // only 1 sort criteria, when the table
                                                                             // is segmentised
                                    orderBy.add(attr);
                                }
                            }
                        }

                        featureList = factory.createFeatures(
                                serviceQuery,
                                bb,
                                null,
                                (page - 1)
                                        * pageSize,
                                pageSize,
                                ((orderBy == null) ? null
                                                   : orderBy.toArray(new FeatureServiceAttribute[orderBy.size()])));
                    } else {
                        featureList = factory.createFeatures(serviceQuery,
                                bb,
                                null, 0, 0, null);
                    }

                    return new ArrayList<FeatureServiceFeature>(featureList);
                }

                @Override
                protected void done() {
                    try {
                        final List<FeatureServiceFeature> featureList = get();

                        if (model == null) {
                            final Map<String, FeatureServiceAttribute> featureServiceAttributes =
                                featureService.getFeatureServiceAttributes();
                            final List<String> orderedFeatureServiceAttributes =
                                featureService.getOrderedFeatureServiceAttributes();
                            model = new CustomTableModel(
                                    orderedFeatureServiceAttributes,
                                    featureServiceAttributes,
                                    (List<FeatureServiceFeature>)featureList,
                                    tableRuleSet);
                            final List<String> columnList = new ArrayList<String>();
                            columnList.add(NbBundle.getMessage(
                                    AttributeTable.class,
                                    "AttributeTable.loadModel.column.none"));
                            columnList.addAll(model.getAllColumnNames());

                            // remove geometry attribute
                            for (final String key : featureServiceAttributes.keySet()) {
                                if (featureServiceAttributes.get(key).isGeometry()) {
                                    final String geomAlias = model.getColumnNameByAttributeName(key);
                                    columnList.remove(geomAlias);
                                }
                            }

                            cbCol1.setModel(new DefaultComboBoxModel(columnList.toArray(new String[0])));
                            cbCol2.setModel(new DefaultComboBoxModel(columnList.toArray(new String[0])));
                            cbCol3.setModel(new DefaultComboBoxModel(columnList.toArray(new String[0])));
                            cbCol4.setModel(new DefaultComboBoxModel(columnList.toArray(new String[0])));
                            table.setModel(model);
                            setTabMapping(table);
                            if (pageSize != -1) {
                                table.setRowSorter(new CustomRowSorter(model));
                            }
                            setTableSize();
                        } else {
                            model.setNewFeatureList(featureList);
                        }
                        setNewModel = false;

                        applySelection();
                        // add custom renderer and editors
                        if (tableRuleSet != null) {
                            for (int i = 0; i < table.getColumnCount(); ++i) {
                                final String columnName = model.getColumnAttributeName(i);
                                final TableCellEditor editor = tableRuleSet.getCellEditor(columnName);
                                final TableCellRenderer renderer = tableRuleSet.getCellRenderer(columnName);

                                if (editor != null) {
                                    table.getColumn(i).setCellEditor(editor);
                                } else {
                                    table.getColumn(i).setCellEditor(editor);
                                }

                                if (renderer != null) {
                                    table.getColumn(i).setCellRenderer(renderer);
                                }
                            }
                        }

                        txtCurrentPage.setText(String.valueOf(page));
                    } catch (Exception e) {
                        LOG.error("Error while retrieving model", e);
                    } finally {
                        panWaiting.setVisible(false);
                        ((JXBusyLabel)labWaitingImage).setBusy(false);
                        setNewModel = false;
                    }
                }
            };

        CismetConcurrency.getInstance("attributeTable").getDefaultExecutor().execute(worker);
    }

    /**
     * Configure the tab key.
     *
     * @param   theTable  the table to configure the tab key on
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public static void setTabMapping(final JTable theTable) {
        if (theTable == null) {
            throw new IllegalArgumentException("theTable is null");
        }

        final int endCol = theTable.getModel().getColumnCount();

        // Get Input and Action Map to set tabbing order on the JTable
        final InputMap im = theTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        final ActionMap am = theTable.getActionMap();

        // Get Tab Keystroke
        final KeyStroke tabKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        am.put(im.get(tabKey), new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final int row = theTable.getSelectedRow();
                    int col = theTable.getSelectedColumn();

                    if (theTable.getSelectedRowCount() != 1) {
                        return;
                    }

                    // Move cell selection
                    if ((theTable.getEditingRow() != -1) && (theTable.getEditingColumn() != -1)) {
                        theTable.getCellEditor(theTable.getEditingRow(), theTable.getEditingColumn()).stopCellEditing();
                    }
                    boolean editCell = false;
                    int attemptions = 0;

                    do {
                        ++attemptions;
                        ++col;

                        if (col >= endCol) {
                            col = 0;
                        }

                        editCell = theTable.editCellAt(row, col);
                    } while (!editCell && (attemptions < 150));
                    theTable.changeSelection(row, col, false, false);
                }
            });

        // Get SHIFT-Tab Keystroke
        final KeyStroke shiftTabKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK);
        am.put(im.get(shiftTabKey), new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final int row = theTable.getSelectedRow();
                    int col = theTable.getSelectedColumn();

                    if (theTable.getSelectedRowCount() != 1) {
                        return;
                    }

                    // Move cell selection
                    if ((theTable.getEditingRow() != -1) && (theTable.getEditingColumn() != -1)) {
                        theTable.getCellEditor(theTable.getEditingRow(), theTable.getEditingColumn()).stopCellEditing();
                    }
                    boolean editCell = false;
                    int attemptions = 0;

                    do {
                        ++attemptions;
                        --col;
                        if (col < 0) {
                            col = endCol - 1;
                        }
                        editCell = theTable.editCellAt(row, col);
                    } while (!editCell && (attemptions < 150));

                    theTable.changeSelection(row, col, false, false);
                }
            });
    }

    /**
     * Set a restriction for the displayed rows.
     *
     * @param  query  the query that should be used for the feature service
     */
    public void setQuery(final Object query) {
        this.query = query;

        currentPage = 1;
        itemCount = 0;
        loadModel(currentPage);

        if (query instanceof String) {
            String queryString = (String)query;
            final int newLinePosition = queryString.indexOf("\n");
            boolean addPoints = false;

            if (newLinePosition != -1) {
                queryString = queryString.substring(0, newLinePosition);
                addPoints = true;
            }

            if (queryString.length() > 20) {
                queryString = queryString.substring(0, 18);
                addPoints = true;
            }

            if (addPoints) {
                queryString += "...";
            }
            AttributeTableFactory.getInstance()
                    .changeAttributeTableName(
                        featureService,
                        NbBundle.getMessage(
                            AttributeTable.class,
                            "AttributeTable.setQuery().name",
                            featureService.getName(),
                            queryString));
        } else {
            AttributeTableFactory.getInstance()
                    .changeAttributeTableName(
                        featureService,
                        NbBundle.getMessage(
                            AttributeTableFactory.class,
                            "AttributeTableFactory.showAttributeTable().name",
                            featureService.getName()));
        }
    }

    /**
     * Toggles the processing mode.
     *
     * @param  forceSave  true, if the changed data should be saved without confirmation
     */
    public void changeProcessingMode(final boolean forceSave) {
        shownAsLocked.clear();
        tbProcessing.setSelected(!tbProcessing.isSelected());
        changeProcessingModeIntern(forceSave);
    }

    /**
     * Determines, if the processing mode of the given service is active.
     *
     * @return  true, if the processing mode is active
     */
    public boolean isProcessingModeActive() {
        return tbProcessing.isSelected();
    }

    /**
     * Checks, if the attribute table model is currently loading.
     *
     * @return  true, iff the attribute table model is currently loading
     */
    public boolean isLoading() {
        return panWaiting.isVisible();
    }

    /**
     * Toggles the processing mode.
     *
     * @param  forceSave  true, if the changed data should be saved without confirmation
     */
    private void changeProcessingModeIntern(final boolean forceSave) {
        lastRows = null;
        if (model == null) {
            // it is not possible to activate the processing mode, when the model is not created, yet.
            tbProcessing.setSelected(!tbProcessing.isSelected());
            return;
        }
        if (tbProcessing.isSelected()) {
            model.setEditable(tbProcessing.isSelected());
            AttributeTableFactory.getInstance().processingModeChanged(featureService, tbProcessing.isSelected());
            final ActiveLayerModel model = (ActiveLayerModel)CismapBroker.getInstance().getMappingComponent()
                        .getMappingModel();

            if (!featureService.isSelectable()) {
                featureService.setSelectable(true);
            }

            if (!featureService.isEnabled()) {
                featureService.setEnabled(true);
            }

            if (model.getMapServices().values().contains(featureService)
                        && !model.isVisible(new TreePath(featureService))) {
                model.handleVisibility(new TreePath(featureService));
                featureService.retrieve(true);
            }

            final List<FeatureServiceFeature> selectedFeatures = getSelectedFeatures();

            try {
                CismapBroker.getInstance().getMappingComponent().setSelectionInProgress(true);
                setSelection(null);
                setSelection(selectedFeatures);
            } finally {
                CismapBroker.getInstance().getMappingComponent().setSelectionInProgress(false);
            }

            CismapBroker.getInstance().getMappingComponent().showHandles(false);
        } else {
            if ((table.getEditingColumn() != -1) && (table.getEditingRow() != -1)) {
                table.getCellEditor(table.getEditingRow(), table.getEditingColumn()).stopCellEditing();
            }
            saveChangedRows(forceSave, true);
        }

        enableDisableButtons();
        table.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPopupMenu1 = new javax.swing.JPopupMenu();
        miSortieren = new javax.swing.JMenuItem();
        miStatistik = new javax.swing.JMenuItem();
        miSpalteAusblenden = new javax.swing.JMenuItem();
        miSpaltenUmbenennen = new javax.swing.JMenuItem();
        miFeldberechnung = new javax.swing.JMenuItem();
        mniAdvancedSorting = new javax.swing.JMenuItem();
        diaStatistic = new javax.swing.JDialog();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lblCountLab = new javax.swing.JLabel();
        lblMinLab = new javax.swing.JLabel();
        lblMaxLab = new javax.swing.JLabel();
        lblSumLab = new javax.swing.JLabel();
        lblMeanLab = new javax.swing.JLabel();
        lblStdDeviationLab = new javax.swing.JLabel();
        lblNullLab = new javax.swing.JLabel();
        lblCountVal = new javax.swing.JLabel();
        lblMaxVal = new javax.swing.JLabel();
        lblMinVal = new javax.swing.JLabel();
        lblSumVal = new javax.swing.JLabel();
        lblMeanVal = new javax.swing.JLabel();
        lblStdDeviationVal = new javax.swing.JLabel();
        lblNullVal = new javax.swing.JLabel();
        butOk = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        labStat = new javax.swing.JLabel();
        labStatCol = new javax.swing.JLabel();
        diaExport = new javax.swing.JDialog();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        lblFormat = new javax.swing.JLabel();
        jcFormat = new javax.swing.JComboBox();
        butExpOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        diaSort = new javax.swing.JDialog();
        jPanel6 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        labSortCol1 = new javax.swing.JLabel();
        cbCol1 = new javax.swing.JComboBox();
        radOrderAsc1 = new javax.swing.JRadioButton();
        radOrderDesc1 = new javax.swing.JRadioButton();
        jPanel9 = new javax.swing.JPanel();
        butExpOk1 = new javax.swing.JButton();
        butCancel1 = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        labSortCol2 = new javax.swing.JLabel();
        cbCol2 = new javax.swing.JComboBox();
        radOrderAsc2 = new javax.swing.JRadioButton();
        radOrderDesc2 = new javax.swing.JRadioButton();
        jPanel11 = new javax.swing.JPanel();
        labSortCol3 = new javax.swing.JLabel();
        cbCol3 = new javax.swing.JComboBox();
        radOrderAsc3 = new javax.swing.JRadioButton();
        radOrderDesc3 = new javax.swing.JRadioButton();
        jPanel12 = new javax.swing.JPanel();
        labSortCol4 = new javax.swing.JLabel();
        cbCol4 = new javax.swing.JComboBox();
        radOrderAsc4 = new javax.swing.JRadioButton();
        radOrderDesc4 = new javax.swing.JRadioButton();
        bgSortOrder1 = new javax.swing.ButtonGroup();
        bgSortOrder2 = new javax.swing.ButtonGroup();
        bgSortOrder3 = new javax.swing.ButtonGroup();
        bgSortOrder4 = new javax.swing.ButtonGroup();
        panHint = new javax.swing.JPanel();
        labSegHint = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        butPrintPreview = new javax.swing.JButton();
        butPrint = new javax.swing.JButton();
        butExport = new javax.swing.JButton();
        butAttrib = new javax.swing.JButton();
        butSelectAll = new javax.swing.JButton();
        butInvertSelection = new javax.swing.JButton();
        butClearSelection = new javax.swing.JButton();
        butMoveSelectedRows = new javax.swing.JButton();
        butZoomToSelection = new javax.swing.JButton();
        butColWidth = new javax.swing.JButton();
        butShowCols = new javax.swing.JButton();
        butSearch = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        tbProcessing = new javax.swing.JToggleButton();
        butUndo = new javax.swing.JButton();
        butCopy = new javax.swing.JButton();
        butPaste = new javax.swing.JButton();
        butDelete = new javax.swing.JButton();
        panWaiting = new javax.swing.JPanel();
        labWaitingImage = new org.jdesktop.swingx.JXBusyLabel();
        tableScrollPane = new javax.swing.JScrollPane();
        table = new org.jdesktop.swingx.JXTable();
        jpControl = new javax.swing.JPanel();
        btnFirstPage = new javax.swing.JButton();
        btnPrevPage = new javax.swing.JButton();
        txtCurrentPage = new javax.swing.JTextField();
        lblTotalPages = new javax.swing.JLabel();
        btnNextPage = new javax.swing.JButton();
        btnLastPage = new javax.swing.JButton();

        miSortieren.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.miSortieren.text")); // NOI18N
        jPopupMenu1.add(miSortieren);

        miStatistik.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.miStatistik.text")); // NOI18N
        miStatistik.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    miStatistikActionPerformed(evt);
                }
            });
        jPopupMenu1.add(miStatistik);

        miSpalteAusblenden.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.miSpalteAusblenden.text")); // NOI18N
        miSpalteAusblenden.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    miSpalteAusblendenActionPerformed(evt);
                }
            });
        jPopupMenu1.add(miSpalteAusblenden);

        miSpaltenUmbenennen.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.miSpaltenUmbenennen.text")); // NOI18N
        miSpaltenUmbenennen.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    miSpaltenUmbenennenActionPerformed(evt);
                }
            });
        jPopupMenu1.add(miSpaltenUmbenennen);

        miFeldberechnung.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.miFeldberechnung.text")); // NOI18N
        miFeldberechnung.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    miFeldberechnungActionPerformed(evt);
                }
            });
        jPopupMenu1.add(miFeldberechnung);

        mniAdvancedSorting.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.mniAdvancedSorting.text")); // NOI18N
        mniAdvancedSorting.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    mniAdvancedSortingActionPerformed(evt);
                }
            });
        jPopupMenu1.add(mniAdvancedSorting);

        diaStatistic.setTitle(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.diaStatistic.title")); // NOI18N

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new java.awt.GridBagLayout());

        lblCountLab.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.lblCountLab.text")); // NOI18N
        lblCountLab.setMaximumSize(new java.awt.Dimension(150, 20));
        lblCountLab.setMinimumSize(new java.awt.Dimension(150, 20));
        lblCountLab.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblCountLab, gridBagConstraints);

        lblMinLab.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.lblMinLab.text")); // NOI18N
        lblMinLab.setMaximumSize(new java.awt.Dimension(150, 20));
        lblMinLab.setMinimumSize(new java.awt.Dimension(150, 20));
        lblMinLab.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblMinLab, gridBagConstraints);

        lblMaxLab.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.lblMaxLab.text")); // NOI18N
        lblMaxLab.setMaximumSize(new java.awt.Dimension(150, 20));
        lblMaxLab.setMinimumSize(new java.awt.Dimension(150, 20));
        lblMaxLab.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblMaxLab, gridBagConstraints);

        lblSumLab.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.lblSumLab.text")); // NOI18N
        lblSumLab.setMaximumSize(new java.awt.Dimension(150, 20));
        lblSumLab.setMinimumSize(new java.awt.Dimension(150, 20));
        lblSumLab.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblSumLab, gridBagConstraints);

        lblMeanLab.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.lblMeanLab.text")); // NOI18N
        lblMeanLab.setMaximumSize(new java.awt.Dimension(150, 20));
        lblMeanLab.setMinimumSize(new java.awt.Dimension(150, 20));
        lblMeanLab.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblMeanLab, gridBagConstraints);

        lblStdDeviationLab.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.lblStdDeviationLab.text")); // NOI18N
        lblStdDeviationLab.setMaximumSize(new java.awt.Dimension(150, 20));
        lblStdDeviationLab.setMinimumSize(new java.awt.Dimension(150, 20));
        lblStdDeviationLab.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblStdDeviationLab, gridBagConstraints);

        lblNullLab.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.lblNullLab.text")); // NOI18N
        lblNullLab.setMaximumSize(new java.awt.Dimension(150, 20));
        lblNullLab.setMinimumSize(new java.awt.Dimension(150, 20));
        lblNullLab.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblNullLab, gridBagConstraints);

        lblCountVal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCountVal.setMaximumSize(new java.awt.Dimension(150, 20));
        lblCountVal.setMinimumSize(new java.awt.Dimension(150, 20));
        lblCountVal.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblCountVal, gridBagConstraints);

        lblMaxVal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblMaxVal.setMaximumSize(new java.awt.Dimension(150, 20));
        lblMaxVal.setMinimumSize(new java.awt.Dimension(150, 20));
        lblMaxVal.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblMaxVal, gridBagConstraints);

        lblMinVal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblMinVal.setMaximumSize(new java.awt.Dimension(150, 20));
        lblMinVal.setMinimumSize(new java.awt.Dimension(150, 20));
        lblMinVal.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblMinVal, gridBagConstraints);

        lblSumVal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSumVal.setMaximumSize(new java.awt.Dimension(150, 20));
        lblSumVal.setMinimumSize(new java.awt.Dimension(150, 20));
        lblSumVal.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblSumVal, gridBagConstraints);

        lblMeanVal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblMeanVal.setMaximumSize(new java.awt.Dimension(150, 20));
        lblMeanVal.setMinimumSize(new java.awt.Dimension(150, 20));
        lblMeanVal.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblMeanVal, gridBagConstraints);

        lblStdDeviationVal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblStdDeviationVal.setMaximumSize(new java.awt.Dimension(150, 20));
        lblStdDeviationVal.setMinimumSize(new java.awt.Dimension(150, 20));
        lblStdDeviationVal.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblStdDeviationVal, gridBagConstraints);

        lblNullVal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblNullVal.setMaximumSize(new java.awt.Dimension(150, 20));
        lblNullVal.setMinimumSize(new java.awt.Dimension(150, 20));
        lblNullVal.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(lblNullVal, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel1.add(jPanel2, gridBagConstraints);

        butOk.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butOk.text")); // NOI18N
        butOk.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butOkActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 10, 0);
        jPanel1.add(butOk, gridBagConstraints);

        labStat.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                        // NOI18N
        labStat.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.labStat.text")); // NOI18N
        jPanel3.add(labStat);

        labStatCol.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jPanel3.add(labStatCol);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        jPanel1.add(jPanel3, gridBagConstraints);

        diaStatistic.getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        diaExport.setTitle(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.diaExport.title")); // NOI18N

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jPanel5.setBorder(null);
        jPanel5.setLayout(new java.awt.GridBagLayout());

        lblFormat.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.lblFormat.text")); // NOI18N
        lblFormat.setMaximumSize(new java.awt.Dimension(100, 20));
        lblFormat.setMinimumSize(new java.awt.Dimension(100, 20));
        lblFormat.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel5.add(lblFormat, gridBagConstraints);

        jcFormat.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    jcFormatItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel5.add(jcFormat, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel4.add(jPanel5, gridBagConstraints);

        butExpOk.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butExpOk.text")); // NOI18N
        butExpOk.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butExpOkActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 10, 20);
        jPanel4.add(butExpOk, gridBagConstraints);

        butCancel.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butCancel.text")); // NOI18N
        butCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCancelActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 10, 20);
        jPanel4.add(butCancel, gridBagConstraints);

        diaExport.getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

        diaSort.setTitle(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.diaSort.title")); // NOI18N
        diaSort.setResizable(false);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        jPanel8.setLayout(new java.awt.GridBagLayout());

        labSortCol1.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.labSortCol1.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel8.add(labSortCol1, gridBagConstraints);

        cbCol1.setPreferredSize(new java.awt.Dimension(250, 27));
        cbCol1.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cbCol1ItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel8.add(cbCol1, gridBagConstraints);

        bgSortOrder1.add(radOrderAsc1);
        radOrderAsc1.setSelected(true);
        radOrderAsc1.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.radOrderAsc1.text",
                new Object[] {})); // NOI18N
        radOrderAsc1.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel8.add(radOrderAsc1, gridBagConstraints);

        bgSortOrder1.add(radOrderDesc1);
        radOrderDesc1.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.radOrderDesc1.text",
                new Object[] {})); // NOI18N
        radOrderDesc1.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel8.add(radOrderDesc1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        jPanel6.add(jPanel8, gridBagConstraints);

        jPanel9.setLayout(new java.awt.GridBagLayout());

        butExpOk1.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butExpOk1.text")); // NOI18N
        butExpOk1.setPreferredSize(new java.awt.Dimension(90, 29));
        butExpOk1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butExpOk1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
        jPanel9.add(butExpOk1, gridBagConstraints);

        butCancel1.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butCancel1.text")); // NOI18N
        butCancel1.setPreferredSize(new java.awt.Dimension(90, 29));
        butCancel1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCancel1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(butCancel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        jPanel6.add(jPanel9, gridBagConstraints);

        jPanel10.setLayout(new java.awt.GridBagLayout());

        labSortCol2.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.labSortCol2.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel10.add(labSortCol2, gridBagConstraints);

        cbCol2.setPreferredSize(new java.awt.Dimension(250, 27));
        cbCol2.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cbCol2ItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel10.add(cbCol2, gridBagConstraints);

        bgSortOrder2.add(radOrderAsc2);
        radOrderAsc2.setSelected(true);
        radOrderAsc2.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.radOrderAsc2.text",
                new Object[] {})); // NOI18N
        radOrderAsc2.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel10.add(radOrderAsc2, gridBagConstraints);

        bgSortOrder2.add(radOrderDesc2);
        radOrderDesc2.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.radOrderDesc2.text",
                new Object[] {})); // NOI18N
        radOrderDesc2.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel10.add(radOrderDesc2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        jPanel6.add(jPanel10, gridBagConstraints);

        jPanel11.setLayout(new java.awt.GridBagLayout());

        labSortCol3.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.labSortCol3.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel11.add(labSortCol3, gridBagConstraints);

        cbCol3.setPreferredSize(new java.awt.Dimension(250, 27));
        cbCol3.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cbCol3ItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel11.add(cbCol3, gridBagConstraints);

        bgSortOrder3.add(radOrderAsc3);
        radOrderAsc3.setSelected(true);
        radOrderAsc3.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.radOrderAsc3.text",
                new Object[] {})); // NOI18N
        radOrderAsc3.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel11.add(radOrderAsc3, gridBagConstraints);

        bgSortOrder3.add(radOrderDesc3);
        radOrderDesc3.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.radOrderDesc3.text",
                new Object[] {})); // NOI18N
        radOrderDesc3.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel11.add(radOrderDesc3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        jPanel6.add(jPanel11, gridBagConstraints);

        jPanel12.setLayout(new java.awt.GridBagLayout());

        labSortCol4.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.labSortCol4.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel12.add(labSortCol4, gridBagConstraints);

        cbCol4.setPreferredSize(new java.awt.Dimension(250, 27));
        cbCol4.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cbCol4ItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel12.add(cbCol4, gridBagConstraints);

        bgSortOrder4.add(radOrderAsc4);
        radOrderAsc4.setSelected(true);
        radOrderAsc4.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.radOrderAsc4.text",
                new Object[] {})); // NOI18N
        radOrderAsc4.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel12.add(radOrderAsc4, gridBagConstraints);

        bgSortOrder4.add(radOrderDesc4);
        radOrderDesc4.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.radOrderDesc4.text",
                new Object[] {})); // NOI18N
        radOrderDesc4.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel12.add(radOrderDesc4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        jPanel6.add(jPanel12, gridBagConstraints);

        diaSort.getContentPane().add(jPanel6, java.awt.BorderLayout.CENTER);

        setLayout(new java.awt.GridBagLayout());

        labSegHint.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        labSegHint.setForeground(new java.awt.Color(200, 16, 10));
        labSegHint.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.labSegHint.text",
                new Object[] {}));                              // NOI18N
        panHint.add(labSegHint);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(panHint, gridBagConstraints);

        jToolBar1.setRollover(true);

        butPrintPreview.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-preview.png"))); // NOI18N
        butPrintPreview.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butPrintPreview.text"));                                                       // NOI18N
        butPrintPreview.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butPrintPreview.toolTipText"));                                                // NOI18N
        butPrintPreview.setFocusable(false);
        butPrintPreview.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butPrintPreview.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butPrintPreview.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butPrintPreviewActionPerformed(evt);
                }
            });
        jToolBar1.add(butPrintPreview);

        butPrint.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-print.png")));          // NOI18N
        butPrint.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butPrint.text")); // NOI18N
        butPrint.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butPrint.toolTipText"));                                                              // NOI18N
        butPrint.setFocusable(false);
        butPrint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butPrint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butPrint.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butPrintActionPerformed(evt);
                }
            });
        jToolBar1.add(butPrint);

        butExport.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-export.png")));           // NOI18N
        butExport.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butExport.text")); // NOI18N
        butExport.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butExport.toolTipText"));                                                               // NOI18N
        butExport.setFocusable(false);
        butExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butExport.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butExportActionPerformed(evt);
                }
            });
        jToolBar1.add(butExport);

        butAttrib.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-search.png"))); // NOI18N
        butAttrib.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butAttrib.toolTipText"));                                                     // NOI18N
        butAttrib.setFocusable(false);
        butAttrib.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butAttrib.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butAttrib.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butAttribActionPerformed(evt);
                }
            });
        jToolBar1.add(butAttrib);

        butSelectAll.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-selectionadd.png"))); // NOI18N
        butSelectAll.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butSelectAll.text"));                                                               // NOI18N
        butSelectAll.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butSelectAll.toolTipText"));                                                        // NOI18N
        butSelectAll.setFocusable(false);
        butSelectAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butSelectAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butSelectAll.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butSelectAllActionPerformed(evt);
                }
            });
        jToolBar1.add(butSelectAll);

        butInvertSelection.setIcon(new javax.swing.ImageIcon(
                getClass().getResource(
                    "/de/cismet/cismap/commons/gui/attributetable/res/icon-selectionintersect.png"))); // NOI18N
        butInvertSelection.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butInvertSelection.text"));                                            // NOI18N
        butInvertSelection.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butInvertSelection.toolTipText"));                                     // NOI18N
        butInvertSelection.setFocusable(false);
        butInvertSelection.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butInvertSelection.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butInvertSelection.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butInvertSelectionActionPerformed(evt);
                }
            });
        jToolBar1.add(butInvertSelection);

        butClearSelection.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-selectionremove.png"))); // NOI18N
        butClearSelection.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butClearSelection.text"));                                                             // NOI18N
        butClearSelection.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butClearSelection.toolTipText"));                                                      // NOI18N
        butClearSelection.setFocusable(false);
        butClearSelection.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butClearSelection.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butClearSelection.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butClearSelectionActionPerformed(evt);
                }
            });
        jToolBar1.add(butClearSelection);

        butMoveSelectedRows.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-thissideup.png"))); // NOI18N
        butMoveSelectedRows.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butMoveSelectedRows.text"));                                                      // NOI18N
        butMoveSelectedRows.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butMoveSelectedRows.toolTipText"));                                               // NOI18N
        butMoveSelectedRows.setFocusable(false);
        butMoveSelectedRows.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butMoveSelectedRows.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butMoveSelectedRows.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butMoveSelectedRowsActionPerformed(evt);
                }
            });
        jToolBar1.add(butMoveSelectedRows);

        butZoomToSelection.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-resize.png"))); // NOI18N
        butZoomToSelection.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butZoomToSelection.text"));                                                   // NOI18N
        butZoomToSelection.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butZoomToSelection.toolTipText"));                                            // NOI18N
        butZoomToSelection.setFocusable(false);
        butZoomToSelection.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butZoomToSelection.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butZoomToSelection.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butZoomToSelectionActionPerformed(evt);
                }
            });
        jToolBar1.add(butZoomToSelection);

        butColWidth.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-text-width.png"))); // NOI18N
        butColWidth.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butColWidth.text"));                                                              // NOI18N
        butColWidth.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butColWidth.toolTipText"));                                                       // NOI18N
        butColWidth.setFocusable(false);
        butColWidth.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butColWidth.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butColWidth.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butColWidthActionPerformed(evt);
                }
            });
        jToolBar1.add(butColWidth);

        butShowCols.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-threecolumns.png"))); // NOI18N
        butShowCols.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butShowCols.text"));                                                                // NOI18N
        butShowCols.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butShowCols.toolTipText"));                                                         // NOI18N
        butShowCols.setFocusable(false);
        butShowCols.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butShowCols.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butShowCols.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butShowColsActionPerformed(evt);
                }
            });
        jToolBar1.add(butShowCols);

        butSearch.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-searchdocument.png")));   // NOI18N
        butSearch.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butSearch.text")); // NOI18N
        butSearch.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butSearch.toolTipText"));                                                               // NOI18N
        butSearch.setFocusable(false);
        butSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butSearch.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(butSearch);
        jToolBar1.add(jSeparator1);

        tbProcessing.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-edit.png"))); // NOI18N
        tbProcessing.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.tbProcessing.toolTipText"));                                                // NOI18N
        tbProcessing.setFocusable(false);
        tbProcessing.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbProcessing.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbProcessing.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    tbProcessingActionPerformed(evt);
                }
            });
        jToolBar1.add(tbProcessing);

        butUndo.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-undo.png")));         // NOI18N
        butUndo.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butUndo.text")); // NOI18N
        butUndo.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butUndo.toolTipText"));                                                             // NOI18N
        butUndo.setFocusable(false);
        butUndo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butUndo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butUndo.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butUndoActionPerformed(evt);
                }
            });
        jToolBar1.add(butUndo);

        butCopy.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-copy.png")));         // NOI18N
        butCopy.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butCopy.text")); // NOI18N
        butCopy.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butCopy.toolTipText"));                                                             // NOI18N
        butCopy.setFocusable(false);
        butCopy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butCopy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butCopy.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCopyActionPerformed(evt);
                }
            });
        jToolBar1.add(butCopy);

        butPaste.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-pastealt.png")));       // NOI18N
        butPaste.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butPaste.text")); // NOI18N
        butPaste.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butPaste.toolTipText"));                                                              // NOI18N
        butPaste.setFocusable(false);
        butPaste.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butPaste.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butPaste.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butPasteActionPerformed(evt);
                }
            });
        jToolBar1.add(butPaste);

        butDelete.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-remove-sign.png")));      // NOI18N
        butDelete.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butDelete.text")); // NOI18N
        butDelete.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butDelete.toolTipText"));                                                               // NOI18N
        butDelete.setFocusable(false);
        butDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butDelete.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butDeleteActionPerformed(evt);
                }
            });
        jToolBar1.add(butDelete);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jToolBar1, gridBagConstraints);

        panWaiting.setBackground(new Color(255, 255, 255, 150));
        panWaiting.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panWaiting.add(labWaitingImage, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(panWaiting, gridBagConstraints);

        table.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mouseClicked(final java.awt.event.MouseEvent evt) {
                    tableMouseClicked(evt);
                }
            });
        tableScrollPane.setViewportView(table);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tableScrollPane, gridBagConstraints);

        jpControl.setLayout(new java.awt.GridBagLayout());

        btnFirstPage.setForeground(new java.awt.Color(200, 16, 10));
        btnFirstPage.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.btnFirstPage.text"));        // NOI18N
        btnFirstPage.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.btnFirstPage.toolTipText")); // NOI18N
        btnFirstPage.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnFirstPageActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jpControl.add(btnFirstPage, gridBagConstraints);

        btnPrevPage.setForeground(new java.awt.Color(200, 16, 10));
        btnPrevPage.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.btnPrevPage.text"));        // NOI18N
        btnPrevPage.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.btnPrevPage.toolTipText")); // NOI18N
        btnPrevPage.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnPrevPageActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jpControl.add(btnPrevPage, gridBagConstraints);

        txtCurrentPage.setForeground(new java.awt.Color(200, 16, 10));
        txtCurrentPage.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.txtCurrentPage.text"));        // NOI18N
        txtCurrentPage.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.txtCurrentPage.toolTipText")); // NOI18N
        txtCurrentPage.setMinimumSize(new java.awt.Dimension(50, 27));
        txtCurrentPage.setPreferredSize(new java.awt.Dimension(50, 27));
        txtCurrentPage.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    txtCurrentPageActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jpControl.add(txtCurrentPage, gridBagConstraints);

        lblTotalPages.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.lblTotalPages.text"));        // NOI18N
        lblTotalPages.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.lblTotalPages.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jpControl.add(lblTotalPages, gridBagConstraints);

        btnNextPage.setForeground(new java.awt.Color(200, 16, 10));
        btnNextPage.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.btnNextPage.text"));        // NOI18N
        btnNextPage.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.btnNextPage.toolTipText")); // NOI18N
        btnNextPage.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnNextPageActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jpControl.add(btnNextPage, gridBagConstraints);

        btnLastPage.setForeground(new java.awt.Color(200, 16, 10));
        btnLastPage.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.btnLastPage.text"));        // NOI18N
        btnLastPage.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.btnLastPage.toolTipText")); // NOI18N
        btnLastPage.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnLastPageActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jpControl.add(btnLastPage, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        add(jpControl, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butPrintPreviewActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butPrintPreviewActionPerformed
        final int[] selectedRows = table.getSelectedRows();
        final boolean useSelectedRows = (selectedRows.length > 0);

        final WaitingDialogThread<JasperPrint> wdt = new WaitingDialogThread<JasperPrint>(StaticSwingTools
                        .getParentFrame(this),
                true,
                NbBundle.getMessage(
                    AttributeTable.class,
                    "AttributeTable.butPrintPreviewActionPerformed.WaitingDialogThread"),
                null,
                500) {

                @Override
                protected JasperPrint doInBackground() throws Exception {
                    final JRDataSource ds = (useSelectedRows ? new TableDataSource(table, selectedRows)
                                                             : new TableDataSource(table));
                    final Map<String, Object> map = new HashMap<String, Object>();
                    map.put(AttributeTableReportBuilder.DATASOURCE_NAME, ds);
                    final DynamicReport report =
                        new AttributeTableReportBuilder().buildReport(featureService.getName(), table);
                    final JasperReport jasperReport = DynamicJasperHelper.generateJasperReport(
                            report,
                            new ClassicLayoutManager(),
                            map);
                    return JasperFillManager.fillReport(jasperReport, map, ds);
                }

                @Override
                protected void done() {
                    try {
                        final JasperPrint jasperPrint = get();

                        final CustomJrViewer aViewer = new CustomJrViewer(jasperPrint);
                        final List<JRSaveContributor> contributors = new ArrayList<JRSaveContributor>();

                        for (final JRSaveContributor contributor : aViewer.getSaveContributors()) {
                            if (contributor.getDescription().toLowerCase().contains("pdf")) {
                                contributors.add(new ContributorWrapper(contributor, "PDF"));
                            } else if (contributor.getDescription().toLowerCase().contains("docx")) {
                                contributors.add(new ContributorWrapper(contributor, "DOCX"));
                            }
                        }

//                        contributors.add(new ShpSaveContributor());
//                        contributors.add(new DbfSaveContributor());
//                        contributors.add(new CsvSaveContributor());
//                        contributors.add(new TxtSaveContributor());

                        Collections.sort(contributors, new Comparator<JRSaveContributor>() {

                                @Override
                                public int compare(final JRSaveContributor o1, final JRSaveContributor o2) {
                                    if ((o1 != null) && (o2 != null)) {
                                        return o1.getDescription().compareTo(o2.getDescription());
                                    } else if ((o1 == null) && (o2 == null)) {
                                        return 0;
                                    } else if (o1 == null) {
                                        return 1;
                                    } else {
                                        return -1;
                                    }
                                }
                            });

                        aViewer.setSaveContributors(contributors.toArray(new JRSaveContributor[contributors.size()]));

                        final JFrame aFrame = new JFrame(org.openide.util.NbBundle.getMessage(
                                    AttributeTable.class,
                                    "AttributeTable.butPrintPreviewActionPerformed.aFrame.title")); // NOI18N
                        aFrame.getContentPane().add(aViewer);
                        final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                        aFrame.setSize(screenSize.width / 2, screenSize.height / 2);
                        final java.awt.Insets insets = aFrame.getInsets();
                        aFrame.setSize(aFrame.getWidth() + insets.left + insets.right,
                            aFrame.getHeight()
                                    + insets.top
                                    + insets.bottom
                                    + 20);
                        aFrame.setLocationRelativeTo(AttributeTable.this);
                        aFrame.setVisible(true);
                    } catch (Exception e) {
                        LOG.error("Error while creating report", e);
                    }
                }
            };

        wdt.start();
    } //GEN-LAST:event_butPrintPreviewActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnPrevPageActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnPrevPageActionPerformed
        if (currentPage > 1) {
            loadModel(--currentPage);
        }
    }                                                                               //GEN-LAST:event_btnPrevPageActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnFirstPageActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnFirstPageActionPerformed
        currentPage = 1;
        loadModel(currentPage);
    }                                                                                //GEN-LAST:event_btnFirstPageActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnNextPageActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnNextPageActionPerformed
        if ((pageSize != -1) && ((currentPage * pageSize) < itemCount)) {
            loadModel(++currentPage);
        }
    }                                                                               //GEN-LAST:event_btnNextPageActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnLastPageActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnLastPageActionPerformed
        currentPage = itemCount / pageSize;

        if ((pageSize != -1) && ((currentPage * pageSize) < itemCount)) {
            ++currentPage;
            loadModel(currentPage);
        }
    } //GEN-LAST:event_btnLastPageActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void miSpalteAusblendenActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_miSpalteAusblendenActionPerformed
        butShowCols.setEnabled(true);
        model.hideColumn(popupColumn);
    }                                                                                      //GEN-LAST:event_miSpalteAusblendenActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void miSpaltenUmbenennenActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_miSpaltenUmbenennenActionPerformed
        final String newName = (String)JOptionPane.showInputDialog(
                this,
                "Geben Sie den neuen Namen der Spalte ein.",
                "Spalte umbenennen",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                model.getColumnName(popupColumn));
        if (newName != null) {
            model.setColumnName(popupColumn, newName);
        }
    }                                                                                       //GEN-LAST:event_miSpaltenUmbenennenActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butShowColsActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butShowColsActionPerformed
        model.showColumns();
        butShowCols.setEnabled(false);
        setTableSize();
    }                                                                               //GEN-LAST:event_butShowColsActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butColWidthActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butColWidthActionPerformed
        setTableSize();
    }                                                                               //GEN-LAST:event_butColWidthActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butSelectAllActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butSelectAllActionPerformed
        selectAll();
    }                                                                                //GEN-LAST:event_butSelectAllActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butClearSelectionActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butClearSelectionActionPerformed
        table.getSelectionModel().clearSelection();
    }                                                                                     //GEN-LAST:event_butClearSelectionActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butInvertSelectionActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butInvertSelectionActionPerformed
        final int[] selectedIndices = table.getSelectedRows();
        table.clearSelection();
        Arrays.sort(selectedIndices);
        table.getSelectionModel().setValueIsAdjusting(true);

        for (int selectedIndex = 0; selectedIndex < table.getRowCount(); ++selectedIndex) {
            if (Arrays.binarySearch(selectedIndices, selectedIndex) < 0) {
                table.addRowSelectionInterval(selectedIndex, selectedIndex);
            }
        }
        table.getSelectionModel().setValueIsAdjusting(false);
    } //GEN-LAST:event_butInvertSelectionActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butMoveSelectedRowsActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butMoveSelectedRowsActionPerformed
        final int[] selectedRows = table.getSelectedRows();
        final FeatureWithIndex[] selectedFeatures = new FeatureWithIndex[selectedRows.length];
        final List<FeatureServiceFeature> allFeatures = new ArrayList<FeatureServiceFeature>(model.getRowCount());
        final int selectedRowCount = selectedRows.length;
        Arrays.sort(selectedRows);

        // keep the original sorts
        for (int i = 0; i < model.getRowCount(); ++i) {
            final int modelIndex = table.convertRowIndexToModel(i);
            allFeatures.add(model.getFeatureServiceFeature(modelIndex));
        }

        for (int i = 0; i < selectedRowCount; ++i) {
            final int modelIndex = table.convertRowIndexToModel(selectedRows[i]);
            selectedFeatures[i] = new FeatureWithIndex(model.getFeatureServiceFeature(modelIndex), selectedRows[i]);
        }

        for (int i = 0; i < model.getColumnCount(); ++i) {
            table.setSortOrder(i, SortOrder.UNSORTED);
        }

        final Comparator<Integer> reverseIntComparator = new Comparator<Integer>() {

                @Override
                public int compare(final Integer o1, final Integer o2) {
                    return -1 * Integer.compare(o1, o2);
                }
            };

        final WaitingDialogThread wdt = new WaitingDialogThread(StaticSwingTools.getFirstParentFrame(
                    CismapBroker.getInstance().getMappingComponent()),
                true,
                NbBundle.getMessage(AttributeTable.class, "AttributeTable.butMoveSelectedRows.waiting"),
                null,
                250) {

                @Override
                protected Object doInBackground() throws Exception {
                    wd.setMax(selectedRowCount);
                    final List<Integer> ts = new ArrayList<Integer>();

                    model.setNewFeatureList(allFeatures);

                    for (int i = (selectedRowCount - 1); i >= 0; --i) {
                        final int index = -1
                                    * (Collections.binarySearch(
                                            ts,
                                            selectedFeatures[i].getIndex(),
                                            reverseIntComparator)
                                        + 1);
                        model.moveRowUp(
                            selectedFeatures[i].getIndex()
                                    + (index),
                            (i == 0));
                        ts.add(index, selectedFeatures[i].getIndex());
                        final int count = (selectedRowCount - 1) - i;
                        if ((count % 10) == 1) {
                            wd.setProgress((int)(count / 0.9));
                        }
                    }

                    return null;
                }

                @Override
                protected void done() {
                    table.getSelectionModel().setSelectionInterval(0, selectedRowCount - 1);
                }
            };

        if (EventQueue.isDispatchThread()) {
            wdt.start();
        } else {
            final LinkedList<Integer> ts = new LinkedList<Integer>();

            for (int i = (selectedRowCount - 1); i >= 0; --i) {
                final int index = -1
                            * (Collections.binarySearch(ts, selectedFeatures[i].getIndex(), reverseIntComparator) + 1);
                model.moveRowUp(
                    selectedFeatures[i].getIndex()
                            + (index),
                    (i == 0));
                ts.add(index, selectedFeatures[i].getIndex());
            }
            table.getSelectionModel().setSelectionInterval(0, selectedRowCount - 1);
        }
    } //GEN-LAST:event_butMoveSelectedRowsActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butZoomToSelectionActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butZoomToSelectionActionPerformed
        final List<? extends Feature> featureList = getSelectedFeatures();

        final ZoomToFeaturesWorker worker = new ZoomToFeaturesWorker(featureList.toArray(
                    new Feature[featureList.size()]),
                10);
        worker.execute();
    } //GEN-LAST:event_butZoomToSelectionActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void miStatistikActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_miStatistikActionPerformed
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0;
        double mean = 0;
        double stdDeviation = 0;
        int nullCount = 0;
        List<FeatureServiceFeature> consideredFeatures = getSelectedFeatures();

        if ((consideredFeatures == null) || consideredFeatures.isEmpty()) {
            consideredFeatures = model.getFeatureServiceFeatures();
        }
        final Double[] values = new Double[consideredFeatures.size()];
        final int count = consideredFeatures.size();
        final String colName = model.getColumnName(popupColumn);

        for (int i = 0; i < consideredFeatures.size(); ++i) {
            Object val = consideredFeatures.get(i).getProperty(colName);

            if (val instanceof String) {
                try {
                    val = Double.parseDouble((String)val);
                } catch (NumberFormatException e) {
                    // nothing to do
                }
            }

            if (val instanceof Number) {
                final double doubleVal = ((Number)val).doubleValue();

                if (doubleVal < min) {
                    min = doubleVal;
                }

                if (doubleVal > max) {
                    max = doubleVal;
                }

                sum += doubleVal;
                values[i] = doubleVal;
            } else if (val == null) {
                ++nullCount;
                values[i] = null;
            }
        }

        mean = sum / (count - nullCount);

        for (int i = 0; i < values.length; ++i) {
            final Double val = values[i];

            if (val != null) {
                final double doubleVal = ((Number)val).doubleValue();
                stdDeviation += Math.pow(doubleVal - mean, 2);
            }
        }

        if (min == Double.POSITIVE_INFINITY) {
            min = 0;
        }
        if (max == Double.NEGATIVE_INFINITY) {
            max = 0;
        }

        // formula: sqrt(1/(n-1) * sum((Xi - Y)^2)), n: value count, Xi: ith values, Y: mean
        // see: http://en.wikipedia.org/wiki/Standard_deviation#Corrected_sample_standard_deviation
        stdDeviation = Math.sqrt(1.0 / (count - nullCount - 1) * stdDeviation);

        lblCountVal.setText(String.valueOf(count));
        lblMinVal.setText(trimNumberString(round(min, 6)));
        lblMaxVal.setText(trimNumberString(round(max, 6)));
        lblMeanVal.setText(trimNumberString(round(mean, 6)));
        lblNullVal.setText(String.valueOf(nullCount));
        lblStdDeviationVal.setText(trimNumberString(round(stdDeviation, 6)));
        lblSumVal.setText(trimNumberString(round(sum, 6)));

        diaStatistic.pack();
        diaStatistic.setResizable(false);
        labStatCol.setText(model.getColumnName(popupColumn));
        StaticSwingTools.showDialog(diaStatistic);
    } //GEN-LAST:event_miStatistikActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOkActionPerformed
        diaStatistic.setVisible(false);
    }                                                                         //GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butExportActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butExportActionPerformed
        if ((featureService.getLayerProperties().getAttributeTableRuleSet() != null)
                    && featureService.getLayerProperties().getAttributeTableRuleSet().hasCustomExportFeaturesMethod()) {
            featureService.getLayerProperties().getAttributeTableRuleSet().exportFeatures();
            return;
        }

//        diaExport.setSize(400, 130);
//        diaExport.pack();
//        diaExport.setResizable(false);
//        diaExport.setModal(true);
//        StaticSwingTools.showDialog(diaExport);
        startExport(null, null);
    } //GEN-LAST:event_butExportActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butExpOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butExpOkActionPerformed
        diaExport.setVisible(false);
        startExport((ExportDownload)jcFormat.getSelectedItem(), null);
    }                                                                            //GEN-LAST:event_butExpOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCancelActionPerformed
        diaExport.setVisible(false);
    }                                                                             //GEN-LAST:event_butCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jcFormatItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_jcFormatItemStateChanged
    }                                                                           //GEN-LAST:event_jcFormatItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butPrintActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butPrintActionPerformed
        if ((featureService.getLayerProperties().getAttributeTableRuleSet() != null)
                    && featureService.getLayerProperties().getAttributeTableRuleSet().hasCustomPrintFeaturesMethod()) {
            featureService.getLayerProperties().getAttributeTableRuleSet().printFeatures();
            return;
        }

        final int[] selectedRows = table.getSelectedRows();
        final boolean useSelectedRows = (selectedRows.length > 0);

        final WaitingDialogThread<JasperPrint> wdt = new WaitingDialogThread<JasperPrint>(StaticSwingTools
                        .getParentFrame(this),
                true,
                NbBundle.getMessage(
                    AttributeTable.class,
                    "AttributeTable.butPrintActionPerformed.WaitingDialogThread"),
                null,
                500) {

                @Override
                protected JasperPrint doInBackground() throws Exception {
                    final JRDataSource ds = (useSelectedRows ? new TableDataSource(table, selectedRows)
                                                             : new TableDataSource(table));
                    final Map<String, Object> map = new HashMap<String, Object>();
                    map.put(AttributeTableReportBuilder.DATASOURCE_NAME, ds);
                    final DynamicReport report =
                        new AttributeTableReportBuilder().buildReport(featureService.getName(), table);
                    final JasperReport jasperReport = DynamicJasperHelper.generateJasperReport(
                            report,
                            new ClassicLayoutManager(),
                            map);
                    return JasperFillManager.fillReport(jasperReport, map, ds);
                }

                @Override
                protected void done() {
                    try {
                        final JasperPrint jasperPrint = get();
                        jasperPrint.setOrientation(OrientationEnum.LANDSCAPE);
                        JasperPrintManager.printReport(jasperPrint, true);
                    } catch (Exception e) {
                        LOG.error("Error while creating report", e);
                    }
                }
            };

        wdt.start();
    } //GEN-LAST:event_butPrintActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void tbProcessingActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_tbProcessingActionPerformed
        shownAsLocked.clear();
        changeProcessingModeIntern(false);
        butPaste.setEnabled(isPasteButtonEnabled());
        featureDeleted = false;
    }                                                                                //GEN-LAST:event_tbProcessingActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butUndoActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butUndoActionPerformed
        final int ans = JOptionPane.showConfirmDialog(
                this,
                NbBundle.getMessage(AttributeTable.class, "AttributeTable.butUndoActionPerformed().text"),
                NbBundle.getMessage(AttributeTable.class, "AttributeTable.butUndoActionPerformed().title"),
                JOptionPane.YES_NO_OPTION);

        if (ans == JOptionPane.YES_OPTION) {
            for (final DefaultFeatureServiceFeature f : modifiedFeatures) {
                ((DefaultFeatureServiceFeature)f).undoAll();
            }

            for (final DefaultFeatureServiceFeature f : newFeatures) {
                if (f instanceof ModifiableFeature) {
                    try {
                        f.setEditable(false);
                        ((ModifiableFeature)f).delete();
                        model.removeFeatureServiceFeature(f);
                    } catch (Exception e) {
                        LOG.error("Cannot remove feature", e);
                    }
                }
            }
            newFeatures.clear();

            for (final FeatureServiceFeature f : allFeaturesToDelete) {
                if (f instanceof ModifiableFeature) {
                    try {
                        f.setEditable(false);
                        ((ModifiableFeature)f).restore();
                        model.addFeature(f);
                    } catch (Exception e) {
                        LOG.error("Cannot restore feature", e);
                    }
                }
            }
            allFeaturesToDelete.clear();
        }
    } //GEN-LAST:event_butUndoActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butAttribActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butAttribActionPerformed
        if (searchPanel != null) {
            searchPanel.openPanel(this, featureService);
        }
    }                                                                             //GEN-LAST:event_butAttribActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butDeleteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butDeleteActionPerformed
        deleteFeatures();
    }                                                                             //GEN-LAST:event_butDeleteActionPerformed

    /**
     * DOCUMENT ME!
     */
    public void deleteFeatures() {
        final int[] selectedRows = table.getSelectedRows();
        final List<FeatureServiceFeature> featuresToDelete = new ArrayList<FeatureServiceFeature>();
        final List<Integer> rowsToDeleteList = new ArrayList<Integer>();
        final List<FeatureServiceFeature> featuresToSelect = new ArrayList<FeatureServiceFeature>();

        for (final int row : selectedRows) {
            final FeatureServiceFeature featureToDelete = model.getFeatureServiceFeature(table.convertRowIndexToModel(
                        row));
            if (featureToDelete instanceof ModifiableFeature) {
                final ModifiableFeature dfsf = (ModifiableFeature)featureToDelete;

                if (!(dfsf instanceof PermissionProvider)
                            || ((PermissionProvider)dfsf).hasWritePermissions()) {
                    rowsToDeleteList.add(row);
                } else {
                    featuresToSelect.add(featureToDelete);
                }
            }
        }

        final Integer[] selectedRowsToDelete = rowsToDeleteList.toArray(new Integer[rowsToDeleteList.size()]);

        final int ans = JOptionPane.showConfirmDialog(
                AttributeTable.this,
                NbBundle.getMessage(
                    AttributeTable.class,
                    "AttributeTable.butDeleteActionPerformed().text",
                    selectedRowsToDelete.length),
                NbBundle.getMessage(AttributeTable.class, "AttributeTable.butDeleteActionPerformed().title"),
                JOptionPane.YES_NO_OPTION);

        if (ans != JOptionPane.YES_OPTION) {
            return;
        }

        final WaitingDialogThread<Map<Integer, String>> wdt = new WaitingDialogThread<Map<Integer, String>>(
                StaticSwingTools.getParentFrame(this),
                true,
                NbBundle.getMessage(
                    AttributeTable.class,
                    "AttributeTable.butDeleteActionPerformed.WaitingDialogThread"),
                null,
                500) {

                @Override
                protected Map<Integer, String> doInBackground() throws Exception {
                    int progress = 0;
                    wd.setMax(selectedRowsToDelete.length);
                    final Map<Integer, String> errorMap = new HashMap<Integer, String>();

                    for (final int row : selectedRowsToDelete) {
                        final FeatureServiceFeature featureToDelete = model.getFeatureServiceFeature(
                                table.convertRowIndexToModel(
                                    row));
                        if (featureToDelete instanceof ModifiableFeature) {
                            final ModifiableFeature dfsf = (ModifiableFeature)featureToDelete;
                            Object lockingObject = null;

                            if (dfsf != null) {
                                try {
                                    try {
                                        if (locker != null) {
                                            lockingObject = lockingObjects.get(featureToDelete);

                                            if (lockingObject == null) {
                                                lockingObject = locker.lock(dfsf, false);
                                            }
                                        }
                                        if (!(dfsf instanceof PermissionProvider)
                                                    || ((PermissionProvider)dfsf).hasWritePermissions()) {
                                            if (dfsf.isEditable()) {
                                                dfsf.setEditable(false);
                                            }
                                            dfsf.delete();
                                            featuresToDelete.add(featureToDelete);
                                            featureDeleted = true;
                                            lockingObjects.remove(featureToDelete);
                                        }
                                    } catch (LockAlreadyExistsException ex) {
                                        errorMap.put(featureToDelete.getId(), ex.getLockMessage());
                                        LOG.error("lock already exists.", ex);
                                        // show the error dialog within the done() method. Otherwise, the popup of the
                                        // error dialog and the popup of the waiting dialog block each other leave loop
                                        break;
                                    } catch (Exception ex) {
                                        LOG.error("Error while locking feature.", ex);
                                        errorMap.put(-1, ex.getMessage());
                                        // leave loop
                                        break;
                                    }
                                } finally {
                                    try {
                                        if (lockingObject != null) {
                                            locker.unlock(lockingObject);
                                        }
                                    } catch (Exception e) {
                                        LOG.error("An error during unlocking occured", e);
                                        // no user message required, because a locking object for an not existing object
                                        // does not matter.
                                    }
                                }
                            }
                        }
                        wd.setProgress(++progress);
                    }

                    return errorMap;
                }

                @Override
                protected void done() {
                    try {
                        final Map<Integer, String> errors = get();
                        allFeaturesToDelete.addAll(featuresToDelete);
                        for (final FeatureServiceFeature fsf : featuresToDelete) {
                            model.removeFeatureServiceFeature((FeatureServiceFeature)fsf);
                            modifiedFeatures.remove(fsf);
                        }
                        featureService.retrieve(true);

                        if (errors.isEmpty()) {
                            SelectionManager.getInstance().addSelectedFeatures(featuresToSelect);
                        }

                        for (final Integer id : errors.keySet()) {
                            if (id >= 0) {
                                JOptionPane.showMessageDialog(
                                    AttributeTable.this,
                                    NbBundle.getMessage(
                                        AttributeTable.class,
                                        "AttributeTable.ListSelectionListener.valueChanged().lockexists.message",
                                        id,
                                        errors.get(id)),
                                    NbBundle.getMessage(
                                        AttributeTable.class,
                                        "AttributeTable.ListSelectionListener.valueChanged().lockexists.title"),
                                    JOptionPane.ERROR_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(
                                    AttributeTable.this,
                                    NbBundle.getMessage(
                                        AttributeTable.class,
                                        "AttributeTable.ListSelectionListener.valueChanged().exception.message",
                                        errors.get(id)),
                                    NbBundle.getMessage(
                                        AttributeTable.class,
                                        "AttributeTable.ListSelectionListener.valueChanged().exception.title"),
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        }

//                        if (tableRuleSet != null) {
//                            tableRuleSet.afterSave(model);
//                        }
                    } catch (Exception e) {
                        LOG.error("Error while deleting objects", e);
                    }
                }
            };

        wdt.start();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void miFeldberechnungActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_miFeldberechnungActionPerformed
        final String attrName = model.getColumnAttributeName(popupColumn);
        final FeatureServiceAttribute attr = (FeatureServiceAttribute)featureService.getFeatureServiceAttributes()
                    .get(attrName);
        List<FeatureServiceFeature> featureList;
        final int[] selectedRow = table.getSelectedRows();

        if ((selectedRow != null) && (selectedRow.length > 0)) {
            featureList = new ArrayList<FeatureServiceFeature>();

            for (final int row : selectedRow) {
                final FeatureServiceFeature f = model.getFeatureServiceFeature(table.convertRowIndexToModel(row));
                if (f.isEditable()) {
                    featureList.add(f);
                }
            }
        } else {
            featureList = model.getFeatureServiceFeatures();
            final List<Feature> features = new ArrayList<Feature>();

            for (final FeatureServiceFeature f : featureList) {
                if ((f instanceof PermissionProvider) && ((PermissionProvider)f).hasWritePermissions()) {
                    features.add(f);
                } else {
                    features.add(f);
                }
            }

            try {
                if (locker != null) {
                    lockingObjects.put(null, locker.lock(features, true));
                    tableLock = true;
                }
            } catch (LockAlreadyExistsException ex) {
                featureList = null;
                JOptionPane.showMessageDialog(
                    AttributeTable.this,
                    NbBundle.getMessage(
                        AttributeTable.class,
                        "AttributeTable.ListSelectionListener.miFeldberechnungActionPerformed().lockexists.message",
                        ex.getLockMessage()),
                    NbBundle.getMessage(
                        AttributeTable.class,
                        "AttributeTable.ListSelectionListener.miFeldberechnungActionPerformed().lockexists.title"),
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                featureList = null;
                LOG.error("Error while locking feature.", ex);
                JOptionPane.showMessageDialog(
                    AttributeTable.this,
                    NbBundle.getMessage(
                        AttributeTable.class,
                        "AttributeTable.ListSelectionListener.valueChanged().exception.message",
                        ex.getMessage()),
                    NbBundle.getMessage(
                        AttributeTable.class,
                        "AttributeTable.ListSelectionListener.valueChanged().exception.title"),
                    JOptionPane.ERROR_MESSAGE);
            }
        }

        if (featureList != null) {
            List<FeatureServiceFeature> allFeatures = null;

            if (pageSize == -1) {
                allFeatures = model.getFeatureServiceFeatures();
            }

            final boolean changes = calculationDialog.openPanel(this, featureService, attr, featureList, allFeatures);

            if (changes) {
                for (final FeatureServiceFeature feature : featureList) {
//                    Object newObject = aValue;

//                    if (tableRuleSet != null) {
//                        newObject = tableRuleSet.afterEdit(attrName, rowIndex, feature.getProperty(attrName), aValue);
//                    }

                    if (!lockedFeatures.contains(feature)) {
                        lockedFeatures.add(feature);
                    }
                    modifiedFeatures.add((DefaultFeatureServiceFeature)feature);
                }
            }
        }
    } //GEN-LAST:event_miFeldberechnungActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCopyActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCopyActionPerformed
        copySelectedFeaturesToClipboard();
    }                                                                           //GEN-LAST:event_butCopyActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butPasteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butPasteActionPerformed
        pasteSelectedFeaturesfromClipboard();
    }                                                                            //GEN-LAST:event_butPasteActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtCurrentPageActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtCurrentPageActionPerformed
        try {
            final Integer page = Integer.parseInt(txtCurrentPage.getText());
            if ((page > 0) && (((page - 1) * pageSize) < itemCount)) {
                currentPage = page;
                loadModel(currentPage);
            }
        } catch (NumberFormatException e) {
        }
    }                                                                                  //GEN-LAST:event_txtCurrentPageActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void mniAdvancedSortingActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_mniAdvancedSortingActionPerformed
        diaSort.setSize(400, 400);
        diaSort.setResizable(false);
        diaSort.setModal(true);
        StaticSwingTools.showDialog(diaSort);
    }                                                                                      //GEN-LAST:event_mniAdvancedSortingActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbCol1ItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cbCol1ItemStateChanged
        final Object o = cbCol1.getSelectedItem();
        final boolean enableBoxes = (o != null) && model.getAllColumnNames().contains((String)o);
        radOrderAsc1.setEnabled(enableBoxes);
        radOrderDesc1.setEnabled(enableBoxes);
    }                                                                         //GEN-LAST:event_cbCol1ItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butExpOk1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butExpOk1ActionPerformed
        final List<String> cols = new ArrayList<String>();
        final List<Boolean> isAscOrder = new ArrayList<Boolean>();
        final List<String> colNames = model.getAllColumnNames();
        final JComboBox[] cbCols = new JComboBox[] { cbCol1, cbCol2, cbCol3, cbCol4 };
        final JRadioButton[] rdButtons = new JRadioButton[] { radOrderAsc1, radOrderAsc2, radOrderAsc3, radOrderAsc4 };

        for (int i = 0; i < cbCols.length; ++i) {
            final String tmp = (String)cbCols[i].getSelectedItem();

            if ((tmp != null) && colNames.contains(tmp)) {
                cols.add(tmp);
                isAscOrder.add(rdButtons[i].isSelected());
            }
        }

        if (cols.size() > 0) {
            model.sortOrder(cols.toArray(new String[cols.size()]), isAscOrder.toArray(new Boolean[isAscOrder.size()]));
        }

        diaSort.setVisible(false);
    } //GEN-LAST:event_butExpOk1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancel1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCancel1ActionPerformed
        diaSort.setVisible(false);
    }                                                                              //GEN-LAST:event_butCancel1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbCol2ItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cbCol2ItemStateChanged
        final Object o = cbCol2.getSelectedItem();
        final boolean enableBoxes = (o != null) && model.getAllColumnNames().contains((String)o);
        radOrderAsc2.setEnabled(enableBoxes);
        radOrderDesc2.setEnabled(enableBoxes);
    }                                                                         //GEN-LAST:event_cbCol2ItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbCol3ItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cbCol3ItemStateChanged
        final Object o = cbCol3.getSelectedItem();
        final boolean enableBoxes = (o != null) && model.getAllColumnNames().contains((String)o);
        radOrderAsc3.setEnabled(enableBoxes);
        radOrderDesc3.setEnabled(enableBoxes);
    }                                                                         //GEN-LAST:event_cbCol3ItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbCol4ItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cbCol4ItemStateChanged
        final Object o = cbCol4.getSelectedItem();
        final boolean enableBoxes = (o != null) && model.getAllColumnNames().contains((String)o);
        radOrderAsc4.setEnabled(enableBoxes);
        radOrderDesc4.setEnabled(enableBoxes);
    }                                                                         //GEN-LAST:event_cbCol4ItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void tableMouseClicked(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_tableMouseClicked
        if (evt.getClickCount() == 2) {
            final int selectedRow = table.getSelectedRow();
            final Geometry g = model.getGeometryFromRow(table.convertRowIndexToModel(selectedRow));

            if ((mappingComponent != null) && (g != null)) {
                final XBoundingBox bbox = new XBoundingBox(g);
                bbox.increase(10);
                mappingComponent.gotoBoundingBoxWithHistory(bbox);
            } else {
                LOG.error("MappingComponent is not set");
            }
        }
    } //GEN-LAST:event_tableMouseClicked

    /**
     * DOCUMENT ME!
     */
    private void copySelectedFeaturesToClipboard() {
        clipboard = getSelectedFeatures();

        for (final AttributeTable tab : instances) {
            tab.butPaste.setEnabled(tab.isPasteButtonEnabled());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  features  DOCUMENT ME!
     */
    public static void copySelectedFeaturesToClipboard(final List<FeatureServiceFeature> features) {
        clipboard = features;

        for (final AttributeTable tab : instances) {
            tab.butPaste.setEnabled(tab.isPasteButtonEnabled());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void pasteSelectedFeaturesfromClipboard() {
        if ((clipboard != null) && featureService.isEditable()) {
            for (final FeatureServiceFeature feature : clipboard) {
                if (feature.getGeometry() != null) {
                    boolean hasZCoordinate = false;

                    for (final Coordinate c : feature.getGeometry().getCoordinates()) {
                        if ((c.z != 0.0) && !Double.isNaN(c.z)) {
                            hasZCoordinate = true;
                            break;
                        }
                    }

                    if (hasZCoordinate) {
                        feature.setGeometry(GeometryUtils.force2d(feature.getGeometry()));
                    }
                }
            }
            final WaitingDialogThread<List<FeatureServiceFeature>> wdt =
                new WaitingDialogThread<List<FeatureServiceFeature>>(StaticSwingTools.getParentFrame(this),
                    true,
                    NbBundle.getMessage(AttributeTable.class, "AttributeTable.pasteSelectedFeaturesfromClipboard.text"),
                    null,
                    500) {

                    @Override
                    protected List<FeatureServiceFeature> doInBackground() throws Exception {
                        final List<FeatureServiceFeature> copiedFeatures = new ArrayList<FeatureServiceFeature>();

                        for (final FeatureServiceFeature feature : clipboard) {
                            final FeatureServiceFeature newFeature = featureService.getFeatureFactory()
                                        .createNewFeature();
                            final Map<String, FeatureServiceAttribute> attributeMap =
                                featureService.getFeatureServiceAttributes();
                            final Map<String, Object> defaultValues = tableRuleSet.getDefaultValues();
                            boolean geometryCompatible = false;
                            Geometry replacementGeometry = null;

                            // check, if the geometry types are compatible
                            final String geomType = featureService.getLayerProperties()
                                        .getFeatureService()
                                        .getGeometryType();
                            if ((geomType != null) && !geomType.equals(AbstractFeatureService.UNKNOWN)) {
                                try {
                                    final Class geomTypeClass = Class.forName("com.vividsolutions.jts.geom."
                                                    + geomType);

                                    if (((geomTypeClass == null) && (feature.getGeometry() == null))
                                                || ((geomTypeClass != null)
                                                    && ((feature.getGeometry() != null)
                                                        && geomTypeClass.isInstance(feature.getGeometry())))) {
                                        if (!((geomTypeClass == null) && (feature.getGeometry() == null))) {
                                            newFeature.setGeometry(feature.getGeometry());
                                        }
                                        geometryCompatible = true;
                                    } else {
                                        String compGeoType;
                                        boolean makeMulti = false;

                                        if (geomType.startsWith("Multi")) {
                                            compGeoType = geomType.substring("Multi".length());
                                            makeMulti = true;
                                        } else {
                                            compGeoType = "Multi" + geomType;
                                        }

                                        try {
                                            final Class otherGeomTypeClass = Class.forName(
                                                    "com.vividsolutions.jts.geom."
                                                            + compGeoType);

                                            if ((otherGeomTypeClass != null)
                                                        && ((feature.getGeometry() != null)
                                                            && otherGeomTypeClass.isInstance(feature.getGeometry()))) {
                                                if (makeMulti) {
                                                    replacementGeometry = StaticGeometryFunctions.toMultiGeometry(
                                                            feature.getGeometry());
                                                    geometryCompatible = true;
                                                } else {
                                                    replacementGeometry = StaticGeometryFunctions.toSimpleGeometry(
                                                            feature.getGeometry());
                                                    geometryCompatible = true;
                                                }
                                            }
                                        } catch (ClassNotFoundException e) {
                                            // nothing to do
                                        }
                                    }
                                } catch (Exception e) {
                                    if (geomType.equals(AbstractFeatureService.NONE)
                                                && (feature.getGeometry() == null)) {
                                        geometryCompatible = true;
                                    }
                                }
                            }

                            if (!geometryCompatible) {
                                continue;
                            }

                            if (tableRuleSet != null) {
                                tableRuleSet.copyProperties(feature, newFeature);
                            } else {
                                // copy properties
                                if (defaultValues != null) {
                                    for (final String propName : defaultValues.keySet()) {
                                        newFeature.setProperty(propName, defaultValues.get(propName));
                                    }
                                }

                                final boolean hasIdExpression =
                                    featureService.getLayerProperties().getIdExpressionType()
                                            == LayerProperties.EXPRESSIONTYPE_PROPERTYNAME;
                                for (final String attrKey : attributeMap.keySet()) {
                                    if (hasIdExpression
                                                && featureService.getLayerProperties().getIdExpression()
                                                .equalsIgnoreCase(
                                                    attrKey)) {
                                        // do not change the id
                                        continue;
                                    }
                                    if (tableRuleSet.isColumnEditable(attrKey)) {
                                        final Object val = getFeaturePropertyIgnoreCase(feature, attrKey);
                                        if (val != null) {
                                            // without this null check, the geometry will probably be overwritten
                                            newFeature.setProperty(attrKey, val);
                                        }
                                    }
                                }
                            }

                            if (replacementGeometry != null) {
                                newFeature.setGeometry(replacementGeometry);
                            }

                            copiedFeatures.add(newFeature);
                        }

                        return copiedFeatures;
                    }

                    @Override
                    protected void done() {
                        try {
                            final List<FeatureServiceFeature> copiedFeatures = get();

                            addFeatures(copiedFeatures);
                        } catch (Exception e) {
                            LOG.error("Error while paste features", e);
                        }
                    }
                };

            wdt.start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     * @param   name     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Object getFeaturePropertyIgnoreCase(final FeatureServiceFeature feature, final String name) {
        for (final Object prop : feature.getProperties().keySet()) {
            if (prop instanceof String) {
                final String propName = (String)prop;
                if (propName.equalsIgnoreCase(name)) {
                    return feature.getProperty(propName);
                }
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     */
    public void selectAll() {
        table.getSelectionModel().setSelectionInterval(0, model.getRowCount() - 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ed    DOCUMENT ME!
     * @param  file  DOCUMENT ME!
     */
    private void startExport(ExportDownload ed, final File file) {
        final List<FeatureServiceFeature> features = new ArrayList<FeatureServiceFeature>();
        final int[] selectedRows = table.getSelectedRows();

        if (lastExportPath == null) {
            lastExportPath = DownloadManager.instance().getDestinationDirectory().getAbsolutePath();
        }

        if (selectedRows != null) {
            for (final int row : selectedRows) {
                final FeatureServiceFeature feature = model.getFeatureServiceFeature(table.convertRowIndexToModel(row));

                if (feature != null) {
                    features.add(feature);
                }
            }
        }

        if (features.isEmpty()) {
            for (int i = 0; i < model.getRowCount(); ++i) {
                features.add(model.getFeatureServiceFeature(table.convertRowIndexToModel(i)));
            }
        }

        try {
            // every download needs its own instance of the Download class
            File outputFile = file;

            if ((outputFile == null) && (ed == null)) {
                final String geometryType = featureService.getLayerProperties().getFeatureService().getGeometryType();

                if ((geometryType == null) || geometryType.equals(AbstractFeatureService.UNKNOWN)
                            || geometryType.equals(AbstractFeatureService.NONE)) {
                    boolean first = true;
                    String featureGeometryType = null;

                    for (final Feature f : features) {
                        if (first) {
                            if (f.getGeometry() != null) {
                                featureGeometryType = f.getGeometry().getGeometryType();
                            } else {
                                break;
                            }
                            first = false;
                        } else {
                            if ((f.getGeometry() == null)
                                        || !f.getGeometry().getGeometryType().equals(featureGeometryType)) {
                                featureGeometryType = null;
                                break;
                            }
                        }
                    }

                    if (featureGeometryType == null) {
                        outputFile = StaticSwingTools.chooseFileWithMultipleFilters(
                                lastExportPath,
                                true,
                                new String[] { "dbf", "csv", "txt" },
                                new String[] { "dbf", "csv", "txt" },
                                this);
                    } else {
                        outputFile = StaticSwingTools.chooseFileWithMultipleFilters(
                                lastExportPath,
                                true,
                                new String[] { "shp", "dbf", "csv", "txt" },
                                new String[] { "shp", "dbf", "csv", "txt" },
                                this);
                    }
                } else {
                    outputFile = StaticSwingTools.chooseFileWithMultipleFilters(
                            lastExportPath,
                            true,
                            new String[] { "shp", "dbf", "csv", "txt" },
                            new String[] { "shp", "dbf", "csv", "txt" },
                            this);
                }

                if (outputFile != null) {
                    ExportDownload downloader;
                    final List<String[]> attributeNames;

                    if (!outputFile.getName().toLowerCase().endsWith("csv")
                                && !outputFile.getName().toLowerCase().endsWith("txt")) {
                        attributeNames = getAliasAttributeList(true);
                    } else {
                        attributeNames = getAliasAttributeList(false);
                    }

                    if (outputFile.getName().toLowerCase().endsWith("dbf")) {
                        downloader = new ExportDbfDownload();
                        downloader.init(outputFile.getAbsolutePath(),
                            "",
                            features.toArray(new FeatureServiceFeature[features.size()]),
                            featureService,
                            attributeNames,
                            null);
                    } else if (outputFile.getName().toLowerCase().endsWith("csv")) {
                        downloader = new ExportCsvDownload(outputFile.getAbsolutePath(),
                                "",
                                features.toArray(new FeatureServiceFeature[features.size()]),
                                featureService,
                                attributeNames);
                    } else if (outputFile.getName().toLowerCase().endsWith("txt")) {
                        downloader = new ExportTxtDownload(outputFile.getAbsolutePath(),
                                "",
                                features.toArray(new FeatureServiceFeature[features.size()]),
                                featureService,
                                attributeNames);
                    } else {
                        if (features.isEmpty()
                                    && featureService.getGeometryType().equals(AbstractFeatureService.UNKNOWN)) {
                            JOptionPane.showMessageDialog(
                                this,
                                NbBundle.getMessage(
                                    AttributeTable.class,
                                    "AttributeTable.butExportActionPerformed.noFeatures.text"),
                                NbBundle.getMessage(
                                    AttributeTable.class,
                                    "AttributeTable.butExportActionPerformed.noFeatures.title"),
                                JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                        downloader = new ExportShapeDownload();
                        downloader.init(outputFile.getAbsolutePath(),
                            "",
                            features.toArray(new FeatureServiceFeature[features.size()]),
                            featureService,
                            attributeNames,
                            null);
                    }

                    lastExportPath = outputFile.getParent();
                    DownloadManager.instance().add(downloader);
                }
            } else {
                ed = ed.getClass().newInstance();

                if (outputFile == null) {
                    outputFile = StaticSwingTools.chooseFile(
                            lastExportPath,
                            true,
                            new String[] { ed.getDefaultExtension().substring(1) },
                            ed.getDefaultExtension(),
                            this);
                }

                if (outputFile != null) {
                    lastExportPath = outputFile.getParent();
                    final List<String[]> attributeNames;

                    if (!ed.getClass().getName().toLowerCase().contains("csv")
                                && !ed.getClass().getName().toLowerCase().contains("txt")) {
                        attributeNames = getAliasAttributeList(true);
                    } else {
                        attributeNames = getAliasAttributeList(false);
                    }

                    ed.init(outputFile.getAbsolutePath(),
                        "",
                        features.toArray(new FeatureServiceFeature[features.size()]),
                        featureService,
                        attributeNames,
                        null);

                    DownloadManager.instance().add(ed);
                }
            }
        } catch (Exception e) {
            LOG.error("The ExportDownload class has possibly no public constructor without arguments.", e);
        }
    }

    /**
     * Reloads the model.
     */
    public void reload() {
        loadModel(currentPage);
    }

    /**
     * Refreshs the table. This should be invoked, if features of the model were changed.
     */
    public void refresh() {
        model.fireContentsChanged();
    }

    /**
     * Saves all changed rows.
     *
     * @param   forceSave             true, if the changed data should be saved without confirmation
     * @param   changeProcessingMode  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean saveChangedRows(final boolean forceSave, final boolean changeProcessingMode) {
        if ((table.getEditingRow() != -1) && (table.getEditingColumn() != -1)) {
            table.getCellEditor(table.getEditingRow(), table.getEditingColumn()).stopCellEditing();
        }

        boolean save = forceSave;
        refreshModifiedFeaturesSet();

        if (!save
                    && (!modifiedFeatures.isEmpty() || featureDeleted || !newFeatures.isEmpty()
                        || !lockedFeatures.isEmpty())) {
            final int ans = JOptionPane.showConfirmDialog(
                    AttributeTable.this,
                    NbBundle.getMessage(
                        AttributeTable.class,
                        "AttributeTable.addWindowListener().text",
                        featureService.getName()),
                    NbBundle.getMessage(AttributeTable.class, "AttributeTable.addWindowListener().title"),
                    JOptionPane.YES_NO_OPTION);

            if (ans == JOptionPane.YES_OPTION) {
                save = true;
            } else if (ans == JOptionPane.NO_OPTION) {
                save = false;
            } else {
                return false;
            }
        }

        if (save) {
            final List<FeatureServiceFeature> featuresPrepareForSave = new ArrayList<FeatureServiceFeature>(
                    modifiedFeatures);
            featuresPrepareForSave.removeAll(allFeaturesToDelete);

            if (tableRuleSet instanceof AttributeTableExtendedRuleSet) {
                final AttributeTableExtendedRuleSet exTableRuleSet = (AttributeTableExtendedRuleSet)tableRuleSet;
                final AttributeTableExtendedRuleSet.ErrorDetails details = exTableRuleSet.prepareForSaveWithDetails(
                        featuresPrepareForSave);
                if (details != null) {
                    if (details.getFeature() != null) {
                        final int featureIndex = table.convertRowIndexToView(model.getRowByFeature(
                                    details.getFeature()));

                        if (featureIndex != -1) {
                            table.getSelectionModel().setSelectionInterval(featureIndex, featureIndex);

                            if (details.getColumn() != null) {
                                try {
                                    final String columnName = model.getColumnNameByAttributeName(details.getColumn());
                                    final int col = table.getColumnModel().getColumnIndex(columnName);

                                    if (col != -1) {
                                        table.editCellAt(featureIndex, col);
                                    }
                                } catch (IllegalArgumentException e) {
                                    LOG.error("Cell not found.", e);
                                }
                            }
                        }
                    }
                    tbProcessing.setSelected(true);
                    return false;
                }
            } else if ((tableRuleSet != null)
                        && !tableRuleSet.prepareForSave(featuresPrepareForSave)) {
                tbProcessing.setSelected(true);
                return false;
            }

            final WaitingDialogThread<Void> wdt = new WaitingDialogThread<Void>(StaticSwingTools.getParentFrame(this),
                    true,
                    "Speichere Änderungen",
                    null,
                    500) {

                    @Override
                    protected Void doInBackground() throws Exception {
                        if (featureService instanceof ShapeFileFeatureService) {
                            final List<FeatureServiceFeature> features = new ArrayList<FeatureServiceFeature>();

                            for (int i = 0; i < model.getRowCount(); ++i) {
                                features.add(model.getFeatureServiceFeature(table.convertRowIndexToModel(i)));
                            }
                            model.setNewFeatureList(new ArrayList<FeatureServiceFeature>());

                            try {
                                if ((features.size() > 0)) {
                                    for (final FeatureServiceFeature fsf : modifiedFeatures) {
                                        if (fsf instanceof ModifiableFeature) {
                                            try {
                                                final ModifiableFeature feature = (ModifiableFeature)fsf;
                                                if (tableRuleSet != null) {
                                                    tableRuleSet.beforeSave(fsf);
                                                }
//                                                feature.saveChangesWithoutReload();
                                            } catch (Exception e) {
                                                LOG.error("Cannot save object", e);
                                            }
                                        }
                                    }

                                    // rewrite shape file
                                    final FeatureCollection fc = new SimpleFeatureCollection(
                                            String.valueOf(System.currentTimeMillis()),
                                            features.toArray(new FeatureServiceFeature[features.size()]),
                                            getAliasAttributeList(true));
                                    String filename = ((ShapeFileFeatureService)featureService).getDocumentURI()
                                                .getPath();
                                    final File shapeFile = new File(filename);

                                    if (shapeFile.exists()) {
                                        String file = shapeFile.getName();
                                        if (file.contains(".")) {
                                            file = file.substring(0, file.lastIndexOf("."));
                                        }
                                        final String nameStem = file;

                                        final File[] files = shapeFile.getParentFile().listFiles(new FileFilter() {

                                                    @Override
                                                    public boolean accept(final File pathname) {
                                                        return pathname.getName()
                                                                    .substring(0, nameStem.length())
                                                                    .equals(nameStem);
                                                    }
                                                });

                                        for (final File f : files) {
                                            if (f.getName().endsWith(".sbx") || f.getName().endsWith(".rti")) {
                                                f.delete();
                                            }
                                        }
                                    }
                                    if (filename.contains(".")) {
                                        filename = filename.substring(0, filename.lastIndexOf("."));
                                    }

                                    final ShapeFile shape = new ShapeFile(
                                            fc,
                                            filename);
                                    final ShapeFileWriter writer = new ShapeFileWriter(shape);
                                    writer.write();
                                }
                            } catch (Exception e) {
                                LOG.error("Error while refreshing shape file.", e);
                            }
                        } else {
                            if (modifiedFeatures.size() > 0) {
                                wd.setMax(modifiedFeatures.size());
                            }
                            int count = 0;

                            for (final FeatureServiceFeature fsf : modifiedFeatures) {
                                if (fsf instanceof ModifiableFeature) {
                                    try {
                                        final ModifiableFeature feature = (ModifiableFeature)fsf;
                                        if (tableRuleSet != null) {
                                            tableRuleSet.beforeSave(fsf);
                                        }
                                        feature.saveChangesWithoutReload();
                                    } catch (Exception e) {
                                        LOG.error("Cannot save object", e);
                                    }
                                }

                                wd.setProgress(++count);
                            }
                        }
                        if (changeProcessingMode) {
                            lockedFeatures.clear();
                        }
                        modifiedFeatures.clear();
                        for (final DefaultFeatureServiceFeature f : newFeatures) {
                            f.setEditable(false);
                        }
                        newFeatures.clear();

                        if (tableRuleSet != null) {
                            if ((allFeaturesToDelete != null) && !allFeaturesToDelete.isEmpty()) {
                                for (final FeatureServiceFeature f : allFeaturesToDelete) {
                                    model.addRemovedFeature(f);
                                }
                            }
                        }
                        tableRuleSet.afterSave(model);
                        model.clearRemovedFeatures();
                        allFeaturesToDelete.clear();

                        if (changeProcessingMode) {
                            model.setEditable(false);
                            AttributeTableFactory.getInstance()
                                    .processingModeChanged(featureService, tbProcessing.isSelected());
                        }

                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    selectionChangeFromMap = true;
                                    model.fireContentsChanged();
                                    selectionChangeFromMap = false;
                                }
                            });

                        // reload the layer
                        if (CismapBroker.getInstance().getMappingComponent() != null) {
                            CismapBroker.getInstance().getMappingComponent().refresh();
                        }
                        if (changeProcessingMode) {
                            if (featureService != null) {
                                loadModel(currentPage);
                            }
                        }
                        butUndo.setEnabled(isUndoButtonEnabled());

                        return null;
                    }
                };

            wdt.start();
        } else {
            for (final FeatureServiceFeature f : modifiedFeatures) {
                if (f instanceof DefaultFeatureServiceFeature) {
                    ((DefaultFeatureServiceFeature)f).undoAll();
                }
            }
            // features should not be restored, if they are new features
            allFeaturesToDelete.removeAll(newFeatures);
            for (final DefaultFeatureServiceFeature f : newFeatures) {
                if (f instanceof ModifiableFeature) {
                    try {
                        f.setEditable(false);
                        ((ModifiableFeature)f).delete();
                        model.removeFeatureServiceFeature(f);
                        SelectionManager.getInstance().removeSelectedFeatures(f);
                    } catch (Exception e) {
                        LOG.error("Cannot remove feature", e);
                    }
                }
            }
            rejectedNewFeatures.addAll(newFeatures);
            newFeatures.clear();

            for (final FeatureServiceFeature f : allFeaturesToDelete) {
                if (f instanceof ModifiableFeature) {
                    try {
                        f.setEditable(false);
                        ((ModifiableFeature)f).restore();
                        model.addFeature(f);
                    } catch (Exception e) {
                        LOG.error("Cannot restore feature", e);
                    }
                }
            }
            allFeaturesToDelete.clear();

            model.setEditable(false);
            AttributeTableFactory.getInstance().processingModeChanged(featureService, tbProcessing.isSelected());
            rejectedNewFeatures.clear();
            // reload the layer
            if (CismapBroker.getInstance().getMappingComponent() != null) {
                CismapBroker.getInstance().getMappingComponent().refresh();
            }
            if (featureService != null) {
                featureService.retrieve(true);
            }
        }

        butUndo.setEnabled(isUndoButtonEnabled());

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void addListSelectionListener(final ListSelectionListener listener) {
        selectionListener.add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void removeListSelectionListener(final ListSelectionListener listener) {
        selectionListener.remove(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    public void removeFeature(final FeatureServiceFeature f) {
        if (f instanceof ModifiableFeature) {
            try {
                f.setEditable(false);
                ((ModifiableFeature)f).delete();
                model.removeFeatureServiceFeature(f);
                modifiedFeatures.remove((DefaultFeatureServiceFeature)f);
                allFeaturesToDelete.add(f);
                featureDeleted = true;
            } catch (Exception e) {
                LOG.error("Cannot remove feature", e);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    public void removeFeatureFromModel(final DefaultFeatureServiceFeature f) {
        model.removeFeatureServiceFeature(f);
        newFeatures.remove(f);
        modifiedFeatures.remove(f);
        allFeaturesToDelete.remove(f);

        if (f.isEditable()) {
            f.setEditable(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<FeatureServiceFeature> getSelectedFeatures() {
        final int[] selectedFeatureRows = table.getSelectedRows();
        final List<FeatureServiceFeature> features = new ArrayList<FeatureServiceFeature>(selectedFeatureRows.length);

        for (int i = 0; i < selectedFeatureRows.length; ++i) {
            features.add(model.getFeatureServiceFeature(table.convertRowIndexToModel(selectedFeatureRows[i])));
        }

        return features;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getSelectedFeatureCount() {
        return table.getSelectedRows().length;
    }

    /**
     * Provides a list with the alias names of all attributes.
     *
     * @param   withGeometryColumn  DOCUMENT ME!
     *
     * @return  the list contains string arrays. Every array has 2 strings. The first string is the alias name and the
     *          second string is the original name
     */
    private List<String[]> getAliasAttributeList(final boolean withGeometryColumn) {
        final List<String[]> attrNames = new ArrayList<String[]>();
        final Map<String, FeatureServiceAttribute> attributeMap = featureService.getFeatureServiceAttributes();
        boolean addGeomField = true;

        for (int i = 0; i < table.getColumnCount(false); ++i) {
            final int modelCol = table.convertColumnIndexToModel(i);

            if (!withGeometryColumn) {
                final FeatureServiceAttribute attr = attributeMap.get(model.getColumnAttributeName(modelCol));
                if ((attr != null) && attr.isGeometry()) {
                    continue;
                }
            } else {
                final FeatureServiceAttribute attr = attributeMap.get(model.getColumnAttributeName(modelCol));
                if ((attr != null) && attr.isGeometry()) {
                    addGeomField = false;
                }
            }

            final String[] aliasAttr = new String[2];

            aliasAttr[0] = model.getColumnName(modelCol);
            aliasAttr[1] = model.getColumnAttributeName(modelCol);

            attrNames.add(aliasAttr);
        }

        if (withGeometryColumn && addGeomField) {
            for (final String name : attributeMap.keySet()) {
                final FeatureServiceAttribute attr = attributeMap.get(name);

                if ((attr != null) && attr.isGeometry()) {
                    final String[] aliasAttr = new String[2];

                    aliasAttr[0] = name;
                    aliasAttr[1] = name;
                    attrNames.add(aliasAttr);
                    break;
                }
            }
        }

        return attrNames;
    }

    /**
     * Removes all trailing zeros.
     *
     * @param   val  the string to trim
     *
     * @return  a new string without trailing zeros
     */
    private String trimNumberString(final String val) {
        String res = String.valueOf(val);

        if (res.indexOf(".") != -1) {
            // remove all ending points and zeros
            for (int i = res.length() - 1; i > 0; --i) {
                final char c = res.charAt(i);

                if ((c == '0') || (c == '.')) {
                    res = res.substring(0, i);
                    if (c == '.') {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return res.replace('.', ',');
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value   DOCUMENT ME!
     * @param   digits  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String round(final double value, final int digits) {
        if (Double.compare(value, Double.NaN) == 0) {
            return "";
        }
        final BigDecimal tmpValue = new BigDecimal(value);
        return tmpValue.setScale(digits, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * DOCUMENT ME!
     */
    private void setTableSize() {
        final TableColumnModel columnModel = table.getColumnModel();
        final FontMetrics fmetrics = table.getFontMetrics(table.getFont());
        final TableModel model = table.getModel();
        final int columnCount = model.getColumnCount();
        int totalSize = 0;

        for (int i = 0; i < columnCount; ++i) {
            int size = (int)fmetrics.getStringBounds(model.getColumnName(i), table.getGraphics()).getWidth();

            for (int row = 0; (row < model.getRowCount()) && (row < 50); ++row) {
                final int tmpSize = (int)fmetrics.getStringBounds(String.valueOf(model.getValueAt(row, i)),
                            table.getGraphics()).getWidth();

                if ((tmpSize > size) && (tmpSize < MAX_COLUMN_SIZE)) {
                    size = tmpSize;
                } else if ((tmpSize > size) && (tmpSize >= MAX_COLUMN_SIZE)) {
                    size = MAX_COLUMN_SIZE;
                }
            }

            totalSize += size;
            columnModel.getColumn(i).setPreferredWidth(size + 30);
        }

        table.setMinimumSize(new Dimension(totalSize + 20, 50));

        butColWidth.setEnabled(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  count  DOCUMENT ME!
     */
    public void setItemCount(final int count) {
        itemCount = count;
        int pageCount = itemCount
                    / pageSize;

        if ((pageCount * pageSize) < itemCount) {
            ++pageCount;
        }

        if (pageSize == -1) {
            pageCount = 1;
        }
        lblTotalPages.setText(" / " + pageCount);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the mappingComponent
     */
    public MappingComponent getMappingComponent() {
        return mappingComponent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mappingComponent  the mappingComponent to set
     */
    public void setMappingComponent(final MappingComponent mappingComponent) {
        this.mappingComponent = mappingComponent;
//        mappingComponent.getFeatureCollection().addFeatureCollectionListener(featureCollectionListener);
//        mappingComponent.addRepaintListener(repaintListener);

        if (model != null) {
            applySelection();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void applySelection() {
        applySelection(null, null, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  origin              DOCUMENT ME!
     * @param  selectedFeatures    DOCUMENT ME!
     * @param  removeOldSelection  DOCUMENT ME!
     */
    public void applySelection(final Object origin, List<Feature> selectedFeatures, final boolean removeOldSelection) {
        selectionChangeFromMap = true;
        selectionEventSource = origin;
        if (selectedFeatures == null) {
            selectedFeatures = SelectionManager.getInstance().getSelectedFeatures(featureService);
        }

        if (removeOldSelection) {
            table.getSelectionModel().clearSelection();
        }

        if (model != null) {
            final int[] selectedRows = table.getSelectedRows();

            for (final int i : selectedRows) {
                final Feature f = model.getFeatureServiceFeature(table.convertRowIndexToModel(i));

                if (!selectedFeatures.contains(f)) {
                    selectedFeatures.add(f);
                }
            }
        }

        if (selectedFeatures != null) {
            setSelection(selectedFeatures);
        }
        selectionEventSource = null;
        selectionChangeFromMap = false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   row  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureServiceFeature getFeatureByRow(final int row) {
        model.getFeatureServiceFeature(table.convertRowIndexToModel(row));
        return model.getFeatureServiceFeature(table.convertRowIndexToModel(row));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getFeatureCount() {
        return model.getRowCount();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id  row DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureServiceFeature getFeatureById(final int id) {
        if (model == null) {
            return null;
        }
        return model.getFeatureServiceFeatureById(id);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   col  DOCUMENT ME!
     *
     * @return  the column name (not the column alias)
     */
    public String getColumnName(final int col) {
        return model.getColumnAttributeName(col);
    }

    /**
     * unlocks all locked objects.
     */
    public void unlockAll() {
        boolean allLocksRemoved = true;

        for (final FeatureServiceFeature f : lockingObjects.keySet()) {
            try {
                final Object tmp = lockingObjects.get(f);
                if (tmp != null) {
                    locker.unlock(tmp);
                }
            } catch (Exception e) {
                LOG.error("Locking object can't be removed.", e);
                allLocksRemoved = false;
            }
        }

        if (!allLocksRemoved) {
            JOptionPane.showMessageDialog(
                AttributeTable.this,
                NbBundle.getMessage(
                    AttributeTable.class,
                    "AttributeTable.CustomTableModel.setEditable.message"),
                NbBundle.getMessage(
                    AttributeTable.class,
                    "AttributeTable.CustomTableModel.setEditable.title"),
                JOptionPane.ERROR_MESSAGE);
        }

        lockingObjects.clear();
        lockedFeatures.clear();
        modifiedFeatures.clear();
        allFeaturesToDelete.clear();
        newFeatures.clear();
        butUndo.setEnabled(isUndoButtonEnabled());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the rejectedNewFeatures
     */
    public TreeSet<DefaultFeatureServiceFeature> getRejectedNewFeatures() {
        return rejectedNewFeatures;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CustomTableModel extends SimpleAttributeTableModel implements PropertyChangeListener {

        //~ Instance fields ----------------------------------------------------

        protected List<FeatureServiceFeature> selectedFeatures;

        private boolean editable = false;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CustomTableModel object.
         *
         * @param  orderedFeatureServiceAttributes  DOCUMENT ME!
         * @param  featureServiceAttributes         DOCUMENT ME!
         * @param  propertyContainer                DOCUMENT ME!
         * @param  tableRuleSet                     DOCUMENT ME!
         */
        public CustomTableModel(final List<String> orderedFeatureServiceAttributes,
                final Map<String, FeatureServiceAttribute> featureServiceAttributes,
                final List<FeatureServiceFeature> propertyContainer,
                final AttributeTableRuleSet tableRuleSet) {
            super(orderedFeatureServiceAttributes, featureServiceAttributes, propertyContainer, tableRuleSet);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isEditable() {
            return editable;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  columns     DOCUMENT ME!
         * @param  isAscOrder  DOCUMENT ME!
         */
        public void sortOrder(final String[] columns, final Boolean[] isAscOrder) {
            for (int i = 0; i < model.getColumnCount(); ++i) {
                table.setSortOrder(i, SortOrder.UNSORTED);
            }

            Collections.sort(featureList, new Comparator<FeatureServiceFeature>() {

                    @Override
                    public int compare(final FeatureServiceFeature o1, final FeatureServiceFeature o2) {
                        for (int i = 0; i < columns.length; ++i) {
                            final int result = compareObjects(
                                    o1.getProperty(columns[i]),
                                    o2.getProperty(columns[i]),
                                    isAscOrder[i]);

                            if (result != 0) {
                                return result;
                            }
                        }

                        return 0;
                    }

                    private int compareObjects(final Object o1, final Object o2, final boolean isAscOrder) {
                        if ((o1 == null) && (o2 == null)) {
                            return 0;
                        } else if (o1 == null) {
                            if (isAscOrder) {
                                return -1;
                            } else {
                                return 1;
                            }
                        } else if (o2 == null) {
                            if (isAscOrder) {
                                return 1;
                            } else {
                                return -1;
                            }
                        } else {
                            if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
                                final Comparable c1 = (Comparable)o1;
                                final Comparable c2 = (Comparable)o2;

                                if (isAscOrder) {
                                    return c1.compareTo(c2);
                                } else {
                                    return c1.compareTo(c2) * -1;
                                }
                            } else {
                                LOG.error("Sort table with non comparable types");
                                return 0;
                            }
                        }
                    }
                });

            fireContentsChanged();
        }

        /**
         * DOCUMENT ME!
         *
         * @param  editable  DOCUMENT ME!
         */
        public void setEditable(final boolean editable) {
            if (this.editable && !editable) {
                // set all feature to editable = false
                for (final FeatureServiceFeature fsf : featureList) {
                    fsf.setEditable(false);
                }

                unlockAll();
            }

            this.editable = editable;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   rowIndex     DOCUMENT ME!
         * @param   columnIndex  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            if (columnIndex < attributeAlias.length) {
                if (tableRuleSet != null) {
                    return editable
                                && tableRuleSet.isColumnEditable(attributeNames[columnIndex])
                                && getFeatureServiceFeature(rowIndex).isEditable();
                } else {
                    return editable
                                && getFeatureServiceFeature(rowIndex).isEditable();
                }
            } else {
                return false;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public List<String> getAllColumnNames() {
            return Arrays.asList(attributeAlias);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  aValue       DOCUMENT ME!
         * @param  rowIndex     DOCUMENT ME!
         * @param  columnIndex  DOCUMENT ME!
         */
        @Override
        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
            final FeatureServiceFeature feature = featureList.get(rowIndex);
            final String attrName = attributeNames[columnIndex];
            Object newObject = (((aValue != null) && aValue.equals("")) ? null : aValue);

            if (tableRuleSet != null) {
                newObject = tableRuleSet.afterEdit(
                        feature,
                        attrName,
                        rowIndex,
                        feature.getProperty(attrName),
                        newObject);
            }
            feature.setProperty(attrName, newObject);
            modifiedFeatures.add((DefaultFeatureServiceFeature)feature);
            butUndo.setEnabled(isUndoButtonEnabled());
            table.repaint();
        }

        /**
         * DOCUMENT ME!
         *
         * @param  e  DOCUMENT ME!
         */
        @Override
        protected void fireContentsChanged(final TableModelEvent e) {
            for (final TableModelListener tmp : listener) {
                tmp.tableChanged(e);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  evt  DOCUMENT ME!
         */
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            final int eCol = table.getEditingColumn();
            final int eRow = table.getEditingRow();

            if ((eRow != -1) && (eCol != -1)) {
                for (int i = 0; i < getColumnCount(); ++i) {
                    if (i != eCol) {
                        final TableModelEvent e = new TableModelEvent(this, eRow, eRow, i);
                        selectionChangeFromMap = true;
                        fireContentsChanged(e);
                        selectionChangeFromMap = false;
                    }
                }
            } else {
                // use a repaint instead of the fireContentsChanged method, because fireContentsChanged removes the
                // selection selectionChangeFromMap = true; fireContentsChanged(); selectionChangeFromMap = false;
                table.repaint();
            }
        }
    }

    /**
     * This highlighter considers the editable attribute of the displayed features.
     *
     * @version  $Revision$, $Date$
     */
    private class CustomColorHighlighter extends org.jdesktop.swingx.decorator.ColorHighlighter {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CustomColorHighlighter object.
         *
         * @param  predicate       DOCUMENT ME!
         * @param  cellBackground  DOCUMENT ME!
         * @param  cellForeground  DOCUMENT ME!
         */
        public CustomColorHighlighter(final HighlightPredicate predicate,
                final Color cellBackground,
                final Color cellForeground) {
            super(predicate, cellBackground, cellForeground);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected void applyForeground(final Component renderer, final ComponentAdapter adapter) {
            super.applyForeground(renderer, adapter);

            if (tbProcessing.isSelected()) {
                // edit mode ist active, but the column is not editable
                if ((tableRuleSet != null)
                            && !tableRuleSet.isColumnEditable(
                                model.getColumnAttributeName(table.convertColumnIndexToModel(adapter.column)))) {
                    renderer.setForeground(new Color(96, 96, 96));
                } else {
                    renderer.setForeground(Color.BLACK);
                }

                final FeatureServiceFeature f = model.getFeatureServiceFeature(table.convertRowIndexToModel(
                            adapter.row));

                if (f instanceof PermissionProvider) {
                    if (!((PermissionProvider)f).hasWritePermissions()) {
                        renderer.setForeground(new Color(96, 96, 96));
                    }
                }
            } else {
                renderer.setForeground(Color.BLACK);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  renderer  DOCUMENT ME!
         * @param  adapter   DOCUMENT ME!
         */
        @Override
        protected void applyBackground(final Component renderer, final ComponentAdapter adapter) {
            super.applyBackground(renderer, adapter);

            final FeatureServiceFeature feature = model.getFeatureServiceFeature(table.convertRowIndexToModel(
                        adapter.row));

            if (feature.isEditable() && feature.getClass().getName().endsWith("CidsLayerFeature")) {
                try {
                    final Method m = feature.getClass().getMethod("getBackgroundColor");
                    final Color backgroundColor = (Color)m.invoke(feature);

                    if (backgroundColor != null) {
                        if (adapter.isSelected()) {
                            renderer.setBackground(backgroundColor);
                        } else {
                            renderer.setBackground(BasicStyle.lighten(backgroundColor));
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Cannot determine the background color.", e);
                }
            } else if (feature.isEditable() && (feature instanceof JDBCFeature)) {
                final Color backgroundColor = ((JDBCFeature)feature).getBackgroundColor();

                if (backgroundColor != null) {
                    if (adapter.isSelected()) {
                        renderer.setBackground(backgroundColor);
                    } else {
                        renderer.setBackground(BasicStyle.lighten(backgroundColor));
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class AttributeTableCellRenderer extends DefaultTableCellRenderer {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   table       DOCUMENT ME!
         * @param   value       DOCUMENT ME!
         * @param   isSelected  DOCUMENT ME!
         * @param   hasFocus    DOCUMENT ME!
         * @param   row         DOCUMENT ME!
         * @param   column      DOCUMENT ME!
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
            Object formattedValue = value;

            if (value instanceof java.sql.Date) {
                final long dateInMillis = ((java.sql.Date)value).getTime();
                formattedValue = DateFormat.getDateInstance().format(new Date(dateInMillis));
            }

            final Component c = super.getTableCellRendererComponent(
                    table,
                    formattedValue,
                    isSelected,
                    hasFocus,
                    row,
                    column);
            final FeatureServiceFeature feature = model.getFeatureServiceFeature(table.convertRowIndexToModel(row));

            if (feature.isEditable() && feature.getClass().getName().endsWith("CidsLayerFeature")) {
                try {
                    final Method m = feature.getClass().getMethod("getBackgroundColor");
                    final Color backgroundColor = (Color)m.invoke(feature);

                    if (backgroundColor != null) {
                        c.setBackground(backgroundColor);
                    }
                } catch (Exception e) {
                    LOG.error("Cannot determine the background color.", e);
                }
            } else if (feature.isEditable() && (feature instanceof JDBCFeature)) {
                final Color backgroundColor = ((JDBCFeature)feature).getBackgroundColor();

                if (backgroundColor != null) {
                    c.setBackground(backgroundColor);
                }
            }

            if (tbProcessing.isSelected()) {
                // edit mode ist active, but the column is not editable
                if ((tableRuleSet != null)
                            && !tableRuleSet.isColumnEditable(
                                model.getColumnAttributeName(table.convertColumnIndexToModel(column)))) {
                    final JLabel lab = new JLabel(((JLabel)c).getText(),
                            ((JLabel)c).getIcon(),
                            ((JLabel)c).getHorizontalAlignment());
                    lab.setBackground(((JLabel)c).getBackground());
                    lab.setForeground(new Color(96, 96, 96));
                    lab.setOpaque(true);
                    return lab;
                }
            }

            return c;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class NumberCellRenderer extends AttributeTableCellRenderer {

        //~ Instance fields ----------------------------------------------------

        private DecimalFormat format;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new NumberCellRenderer object.
         */
        public NumberCellRenderer() {
            format = new DecimalFormat();
            format.setGroupingUsed(false);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   table       DOCUMENT ME!
         * @param   value       DOCUMENT ME!
         * @param   isSelected  DOCUMENT ME!
         * @param   hasFocus    DOCUMENT ME!
         * @param   row         DOCUMENT ME!
         * @param   column      DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public Component getTableCellRendererComponent(final JTable table,
                Object value,
                final boolean isSelected,
                final boolean hasFocus,
                final int row,
                final int column) {
            if (value instanceof Number) {
                value = format.format(value);
            }

            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    /**
     * Wraps a table into a data source, that can be used within a jasper report.
     *
     * @version  $Revision$, $Date$
     */
    private static class TableDataSource implements JRDataSource {

        //~ Instance fields ----------------------------------------------------

        private int index = -1;
        private TableModel model;
        private JTable table;
        private int[] selectedRows = null;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new TableDataSource object.
         *
         * @param  table  DOCUMENT ME!
         */
        public TableDataSource(final JTable table) {
            this.model = table.getModel();
            this.table = table;
        }

        /**
         * Creates a new TableDataSource object.
         *
         * @param  table         DOCUMENT ME!
         * @param  selectedRows  DOCUMENT ME!
         */
        public TableDataSource(final JTable table, final int[] selectedRows) {
            this.model = table.getModel();
            this.table = table;
            this.selectedRows = selectedRows;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         *
         * @throws  JRException  DOCUMENT ME!
         */
        @Override
        public boolean next() throws JRException {
            if (selectedRows == null) {
                final boolean ret = ++index
                            < model.getRowCount();

                if (!ret) {
                    // Set the internal index to the first row, when the return value is false,
                    // so that the data source can used from multiple sub reports.
                    index = -1;
                }

                return ret;
            } else {
                final boolean ret = ++index
                            < selectedRows.length;

                if (!ret) {
                    // Set the internal index to the first row, when the return value is false,
                    // so that the data source can used from multiple sub reports.
                    index = -1;
                }

                return ret;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   jrField  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         *
         * @throws  JRException  DOCUMENT ME!
         */
        @Override
        public Object getFieldValue(final JRField jrField) throws JRException {
            int col = 0;

            try {
                col = Integer.parseInt(jrField.getName());
            } catch (NumberFormatException e) {
                LOG.error("Cannot parse column name", e);
            }

            Object result;

            if (selectedRows == null) {
                result = model.getValueAt(table.convertRowIndexToModel(index), col);
            } else {
                result = model.getValueAt(table.convertRowIndexToModel(selectedRows[index]), col);
            }

            if (result != null) {
                if ((result instanceof Float) || (result instanceof Double)) {
                    return FeatureTools.FORMATTER.format(result);
                } else {
                    return String.valueOf(result);
                }
            } else {
                return null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CustomRowSorter extends RowSorter<CustomTableModel> {

        //~ Static fields/initializers -----------------------------------------

        private static final int MAX_SORT_KEYS = 3;

        //~ Instance fields ----------------------------------------------------

        private CustomTableModel tableModel;
        private List<RowSorter.SortKey> sortKeys;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CustomRowSorter object.
         *
         * @param  model  DOCUMENT ME!
         */
        public CustomRowSorter(final CustomTableModel model) {
            sortKeys = Collections.emptyList();
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public CustomTableModel getModel() {
            return model;
        }

        @Override
        public void toggleSortOrder(final int column) {
            checkColumn(column);
            if (isSortable(column)) {
                List<RowSorter.SortKey> keys = new ArrayList<RowSorter.SortKey>(getSortKeys());
                final RowSorter.SortKey sortKey;
                int sortIndex;
                for (sortIndex = keys.size() - 1; sortIndex >= 0; sortIndex--) {
                    if (keys.get(sortIndex).getColumn() == column) {
                        break;
                    }
                }
                if (sortIndex == -1) {
                    // Key doesn't exist
                    sortKey = new RowSorter.SortKey(column, SortOrder.ASCENDING);
                    keys.add(0, sortKey);
                } else if (sortIndex == 0) {
                    // It's the primary sorting key, toggle it
                    keys.set(0, toggle(keys.get(0)));
                } else {
                    // It's not the first, but was sorted on, remove old
                    // entry, insert as first with ascending.
                    keys.remove(sortIndex);
                    keys.add(0, new RowSorter.SortKey(column, SortOrder.ASCENDING));
                }
                if (keys.size() > MAX_SORT_KEYS) {
                    keys = keys.subList(0, MAX_SORT_KEYS);
                }
                setSortKeys(keys);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   column  DOCUMENT ME!
         *
         * @throws  IndexOutOfBoundsException  DOCUMENT ME!
         */
        private void checkColumn(final int column) {
            if ((column < 0) || (column >= model.getColumnCount())) {
                throw new IndexOutOfBoundsException(
                    "column beyond range of TableModel");
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   key  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private RowSorter.SortKey toggle(final RowSorter.SortKey key) {
            if (key.getSortOrder() == SortOrder.ASCENDING) {
                return new RowSorter.SortKey(key.getColumn(), SortOrder.DESCENDING);
            }
            return new RowSorter.SortKey(key.getColumn(), SortOrder.ASCENDING);
        }

        /**
         * DOCUMENT ME!
         *
         * @param   column  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private boolean isSortable(final int column) {
            return true;
        }

        @Override
        public int convertRowIndexToModel(final int index) {
            return index;
        }

        @Override
        public int convertRowIndexToView(final int index) {
            return index;
        }

        @Override
        public void setSortKeys(final List<? extends RowSorter.SortKey> sortKeys) {
            final List<SortKey> old = this.sortKeys;
            if ((sortKeys != null) && (sortKeys.size() > 0)) {
                final int max = ((model != null) ? model.getColumnCount() : 0);
                for (final SortKey key : sortKeys) {
                    if ((key == null) || (key.getColumn() < 0)
                                || (key.getColumn() >= max)) {
                        throw new IllegalArgumentException("Invalid SortKey");
                    }
                }
                this.sortKeys = Collections.unmodifiableList(
                        new ArrayList<SortKey>(sortKeys));
            } else {
                this.sortKeys = Collections.emptyList();
            }
            if (!this.sortKeys.equals(old)) {
                fireSortOrderChanged();
                loadModel(currentPage);
            }
        }

        @Override
        public List<? extends RowSorter.SortKey> getSortKeys() {
            return sortKeys;
        }

        @Override
        public int getViewRowCount() {
            if (model != null) {
                return model.getRowCount();
            } else {
                return 0;
            }
        }

        @Override
        public int getModelRowCount() {
            if (model != null) {
                return model.getRowCount();
            } else {
                return 0;
            }
        }

        @Override
        public void modelStructureChanged() {
        }

        @Override
        public void allRowsChanged() {
//            setSortKeys(null);
        }

        @Override
        public void rowsInserted(final int firstRow, final int endRow) {
        }

        @Override
        public void rowsDeleted(final int firstRow, final int endRow) {
        }

        @Override
        public void rowsUpdated(final int firstRow, final int endRow) {
        }

        @Override
        public void rowsUpdated(final int firstRow, final int endRow, final int column) {
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CustomJrViewer extends JRViewer {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CustomJrViewer object.
         *
         * @param  jrPrint  DOCUMENT ME!
         */
        public CustomJrViewer(final JasperPrint jrPrint) {
            super(jrPrint);
            btnReload.setVisible(false);
            btnSave.setToolTipText(NbBundle.getMessage(AttributeTable.class, "AttributeTable.butExport.toolTipText"));
            btnSave.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-export.png")));
            btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(
                    AttributeTable.class,
                    "AttributeTable.butExport.toolTipText"));
            btnPrint.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-print.png")));
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected void loadReport(final JasperPrint jrPrint) {
            super.loadReport(jrPrint);
            btnReload.setVisible(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private abstract class ExportSaveContributor extends JRSaveContributor {

        //~ Instance fields ----------------------------------------------------

        protected boolean withGeometries;

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public abstract ExportDownload getExportDownload();

        @Override
        public void save(final JasperPrint jp, final File file) throws JRException {
            startExport(getExportDownload(), file);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ShpSaveContributor extends ExportSaveContributor {

        //~ Methods ------------------------------------------------------------

        @Override
        public ExportDownload getExportDownload() {
            return new ExportShapeDownload();
        }

        @Override
        public String getDescription() {
            return "Shape (*.shp)";
        }

        @Override
        public boolean accept(final File f) {
            return (f != null)
                        && f.getAbsolutePath().toLowerCase().endsWith(".shp");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ContributorWrapper extends JRSaveContributor {

        //~ Instance fields ----------------------------------------------------

        private final JRSaveContributor contributor;
        private final String description;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ContributorWrapper object.
         *
         * @param  contributor  DOCUMENT ME!
         * @param  description  DOCUMENT ME!
         */
        public ContributorWrapper(final JRSaveContributor contributor, final String description) {
            this.contributor = contributor;
            this.description = description;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof ContributorWrapper) {
                return contributor.equals(((ContributorWrapper)obj).contributor);
            } else {
                return contributor.equals(obj);
            }
        }

        @Override
        public void save(final JasperPrint jp, final File file) throws JRException {
            contributor.save(jp, file);
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public boolean accept(final File f) {
            return contributor.accept(f);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CsvSaveContributor extends ExportSaveContributor {

        //~ Methods ------------------------------------------------------------

        @Override
        public ExportDownload getExportDownload() {
            return new ExportCsvDownload();
        }

        @Override
        public String getDescription() {
            return "CSV (*.csv)";
        }

        @Override
        public boolean accept(final File f) {
            return (f != null)
                        && f.getAbsolutePath().toLowerCase().endsWith(".csv");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class DbfSaveContributor extends ExportSaveContributor {

        //~ Methods ------------------------------------------------------------

        @Override
        public ExportDownload getExportDownload() {
            return new ExportDbfDownload();
        }

        @Override
        public String getDescription() {
            return "DBF (*.dbf)";
        }

        @Override
        public boolean accept(final File f) {
            return (f != null)
                        && f.getAbsolutePath().toLowerCase().endsWith(".dbf");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class TxtSaveContributor extends ExportSaveContributor {

        //~ Methods ------------------------------------------------------------

        @Override
        public ExportDownload getExportDownload() {
            return new ExportTxtDownload();
        }

        @Override
        public String getDescription() {
            return "TXT (*.txt)";
        }

        @Override
        public boolean accept(final File f) {
            return (f != null)
                        && f.getAbsolutePath().toLowerCase().endsWith(".txt");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class FeatureWithIndex implements Comparable<FeatureWithIndex> {

        //~ Instance fields ----------------------------------------------------

        private FeatureServiceFeature feature;
        private int index;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FeatureWithIndex object.
         *
         * @param  feature  DOCUMENT ME!
         * @param  index    DOCUMENT ME!
         */
        public FeatureWithIndex(final FeatureServiceFeature feature, final int index) {
            this.feature = feature;
            this.index = index;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getIndex() {
            return index;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public FeatureServiceFeature getFeature() {
            return feature;
        }

        @Override
        public int compareTo(final FeatureWithIndex o) {
            return Integer.compare(getIndex(), o.getIndex());
        }
    }
}
