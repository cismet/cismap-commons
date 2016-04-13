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
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;

import java.lang.reflect.Method;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.text.DateFormat;
import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.FocusManager;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.Timer;
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
import de.cismet.cismap.commons.tools.SimpleFeatureCollection;
import de.cismet.cismap.commons.util.SelectionChangedEvent;
import de.cismet.cismap.commons.util.SelectionChangedListener;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.commons.concurrency.CismetConcurrency;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;
import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;

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
    private final RepaintListener repaintListener;
    private final List<FeatureServiceFeature> lockedFeatures = new ArrayList<FeatureServiceFeature>();
    private AttributeTableRuleSet tableRuleSet = new DefaultAttributeTableRuleSet();
    private final FeatureLockingInterface locker;
    private final List<Object> lockingObjects = new ArrayList<Object>();
    private AttributeTableSearchPanel searchPanel;
    private AttributeTableFieldCalculation calculationDialog;
    private Object query;
    private int[] lastRows;
    private final TreeSet<DefaultFeatureServiceFeature> modifiedFeatures = new TreeSet<DefaultFeatureServiceFeature>();
    private Object selectionEventSource = null;
    private List<ListSelectionListener> selectionListener = new ArrayList<ListSelectionListener>();
    private TreeSet<Feature> shownAsLocked = new TreeSet<Feature>();
    private String lastExportPath = DownloadManager.instance().getDestinationDirectory().getAbsolutePath();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFirstPage;
    private javax.swing.JButton btnLastPage;
    private javax.swing.JButton btnNextPage;
    private javax.swing.JButton btnPrevPage;
    private javax.swing.JButton butAttrib;
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butClearSelection;
    private javax.swing.JButton butColWidth;
    private javax.swing.JButton butCopy;
    private javax.swing.JButton butDelete;
    private javax.swing.JButton butExpOk;
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
    private javax.swing.JDialog diaExport;
    private javax.swing.JDialog diaStatistic;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JComboBox jcFormat;
    private javax.swing.JPanel jpControl;
    private javax.swing.JLabel labSegHint;
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
    private javax.swing.JPanel panHint;
    private javax.swing.JPanel panWaiting;
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

        if (featureService.getMaxFeaturesPerPage() <= 0) {
            pageSize = -1;
            jpControl.setVisible(false);
            panHint.setVisible(false);
        } else {
            pageSize = featureService.getMaxFeaturesPerPage();
        }

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
                        boolean columnEditable = true;

                        if (tableRuleSet != null) {
                            columnEditable = tableRuleSet.isColumnEditable(model.getColumnAttributeName(popupColumn));
                        }
                        miFeldberechnung.setEnabled(tbProcessing.isSelected() && columnEditable);

                        jPopupMenu1.show((Component)e.getSource(), e.getX(), e.getY());
                    }
                }
            });

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(final ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        final boolean rowsSelected = table.getSelectedRows().length > 0;
                        butCopy.setEnabled(rowsSelected);
                        butDelete.setEnabled(rowsSelected);
                        butClearSelection.setEnabled(rowsSelected);
                        butMoveSelectedRows.setEnabled(rowsSelected);
                        butZoomToSelection.setEnabled(rowsSelected);
                        butDelete.setEnabled(isDeleteButtonEnabled());

                        if (!selectionChangeFromMap) {
                            SelectionManager.getInstance()
                                    .setSelectedFeaturesForService(featureService, getSelectedFeatures());
//                            SelectionManager.getInstance().featureSelectionChanged(null);
                        }

                        if (tbProcessing.isSelected()) {
                            final int[] rows = table.getSelectedRows();

                            if (!Arrays.equals(lastRows, rows)) {
                                for (final int row : rows) {
                                    final FeatureServiceFeature feature = model.getFeatureServiceFeature(
                                            table.convertRowIndexToModel(row));
                                    makeFeatureEditable(feature);
                                }
                            }
                            lastRows = rows;
                        }
                    }

                    table.repaint();

                    for (final ListSelectionListener l : selectionListener) {
                        if (l != selectionEventSource) {
                            l.valueChanged(e);
                        }
                    }
                }
            });

        table.setDefaultRenderer(String.class, new AttributeTableCellRenderer());
        table.setDefaultRenderer(Boolean.class, new AttributeTableCellRenderer());
        table.setDefaultRenderer(Date.class, new AttributeTableCellRenderer());
        table.setDefaultRenderer(Number.class, new NumberCellRenderer());

        txtCurrentPage.setText("1");

        loadModel(currentPage);

        final ColorHighlighter base = new CustomColorHighlighter(
                HighlightPredicate.EVEN,
                new Color(255, 255, 255),
                null);
        final ColorHighlighter alternate = new CustomColorHighlighter(
                HighlightPredicate.ODD,
                new Color(235, 235, 235),
                null);
        final Highlighter alternateRowHighlighter = new CompoundHighlighter(base, alternate);

        ((JXTable)table).setHighlighters(alternateRowHighlighter);

        featureSelectionChangedListener = new SelectionChangedListener() {

                @Override
                public void selectionChanged(final SelectionChangedEvent event) {
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
     * @param  feature  DOCUMENT ME!
     */
    public void addModifiedFeature(final DefaultFeatureServiceFeature feature) {
        if (!modifiedFeatures.contains(feature)) {
            modifiedFeatures.add(feature);
        }
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

        for (int index = 0; index < tableFeatures.size(); ++index) {
            final FeatureServiceFeature feature = tableFeatures.get(table.convertRowIndexToModel(index));
            final boolean contained = (selectedFeatures != null) && selectedFeatures.contains(feature);
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
    private boolean isPasteButtonEnabled() {
        boolean enabled = false;

        if ((clipboard != null) && tbProcessing.isSelected() && featureService.isEditable()) {
            for (final FeatureServiceFeature feature : clipboard) {
                final String geomType = featureService.getLayerProperties().getFeatureService().getGeometryType();
                if ((geomType != null) && !geomType.equals(AbstractFeatureService.UNKNOWN)) {
                    try {
                        final Class geomTypeClass = Class.forName("com.vividsolutions.jts.geom." + geomType);

                        if ((geomTypeClass != null)
                                    && ((feature.getGeometry() == null)
                                        || geomTypeClass.isInstance(feature.getGeometry()))) {
                            enabled = true;
                            break;
                        }
                    } catch (Exception e) {
                        // nothing to do
                    }
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
                }
            }
        }

        return hasWritePermission && (selectedIndices.length > 0);
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
                saveChangedRows(true);
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
        return true;
    }

    /**
     * Locks the given feature, if a corresponding locker exists and make the feature editable.
     *
     * @param  feature  the feature to make editable
     */
    public void makeFeatureEditable(final FeatureServiceFeature feature) {
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
                    if (locker != null) {
                        lockingObjects.add(locker.lock(feature, false));
                    }
                    feature.setEditable(true);
                    if (!lockedFeatures.contains(feature)) {
                        lockedFeatures.add(feature);
                        ((DefaultFeatureServiceFeature)feature).addPropertyChangeListener(model);
                    }
                } catch (LockAlreadyExistsException ex) {
                    shownAsLocked.add(feature);
                    JOptionPane.showMessageDialog(
                        AttributeTable.this,
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

                    final Timer t = new Timer(500, new ActionListener() {

                                @Override
                                public void actionPerformed(final ActionEvent e) {
                                    shownAsLocked.clear();
                                }
                            });
                    t.setRepeats(false);
                    t.start();
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
        }
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

        final SwingWorker<List<FeatureServiceFeature>, Void> worker =
            new SwingWorker<List<FeatureServiceFeature>, Void>() {

                @Override
                protected List<FeatureServiceFeature> doInBackground() throws Exception {
                    Thread.currentThread().setName("AttributeTable loadModel");
                    final Object serviceQuery = ((query == null) ? featureService.getQuery() : query);

                    if (bb == null) {
                        final Geometry g = ZoomToLayerWorker.getServiceBounds(featureService);

                        if (g != null) {
                            bb = new XBoundingBox(g);

                            try {
                                final CrsTransformer transformer = new CrsTransformer(CismapBroker.getInstance()
                                                .getSrs().getCode());
                                bb = transformer.transformBoundingBox(bb);
                            } catch (Exception e) {
                                LOG.error("Cannot transform CRS.", e);
                            }
                        } else {
                            bb = null;
                        }
                    }

                    if ((pageSize != -1) && (itemCount == 0)) {
                        setItemCount(featureService.getFeatureCount(query, bb));
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

                    return featureList;
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
                            table.setModel(model);
                            if (pageSize != -1) {
                                table.setRowSorter(new CustomRowSorter(model));
                            }
                            setTableSize();
                        } else {
                            model.setNewFeatureList(featureList);
                        }

                        applySelection();
                        // add custom renderer and editors
                        if (tableRuleSet != null) {
                            for (int i = 0; i < table.getColumnCount(); ++i) {
                                final String columnName = model.getColumnAttributeName(i);
                                final TableCellEditor editor = tableRuleSet.getCellEditor(columnName);
                                final TableCellRenderer renderer = tableRuleSet.getCellRenderer(columnName);

                                if (editor != null) {
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
                    }
                }
            };

        CismetConcurrency.getInstance("attributeTable").getDefaultExecutor().execute(worker);
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
            }
        } else {
            if ((table.getEditingColumn() != -1) && (table.getEditingRow() != -1)) {
                table.getCellEditor(table.getEditingRow(), table.getEditingColumn()).stopCellEditing();
            }
            saveChangedRows(forceSave);
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
                    final JRDataSource ds = new TableDataSource(table);
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
                            if (!contributor.getDescription().toLowerCase().contains("csv")
                                        && !contributor.getDescription().toLowerCase().contains("multiple sheets")) {
                                contributors.add(contributor);
                            }
                        }

                        contributors.add(new ShpSaveContributor());
                        contributors.add(new DbfSaveContributor());
                        contributors.add(new CsvSaveContributor());
                        contributors.add(new TxtSaveContributor());

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
        table.getSelectionModel().setSelectionInterval(0, model.getRowCount() - 1);
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
        table.selectAll();
        table.getSelectionModel().setValueIsAdjusting(true);

        for (final int selectedIndex : selectedIndices) {
            table.removeRowSelectionInterval(selectedIndex, selectedIndex);
        }
        table.getSelectionModel().setValueIsAdjusting(false);
    } //GEN-LAST:event_butInvertSelectionActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butMoveSelectedRowsActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butMoveSelectedRowsActionPerformed
        for (int i = 0; i < model.getColumnCount(); ++i) {
            table.setSortOrder(i, SortOrder.UNSORTED);
        }

        final int[] selectedRows = table.getSelectedRows();
        final int selectedRowCount = table.getSelectedRowCount();
        int count = 0;

        Arrays.sort(selectedRows);

        for (int i = (selectedRowCount - 1); i >= 0; --i) {
            model.moveRowUp(selectedRows[i] + (count++));
        }

        table.getSelectionModel().setSelectionInterval(0, selectedRowCount - 1);
    } //GEN-LAST:event_butMoveSelectedRowsActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butZoomToSelectionActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butZoomToSelectionActionPerformed
        final int[] selectedRows = table.getSelectedRows();
        boolean first = true;
        int srid = 0;
        final List<Geometry> geomList = new ArrayList<Geometry>(selectedRows.length);

        for (final int row : selectedRows) {
            Geometry g = model.getGeometryFromRow(table.convertRowIndexToModel(row));

            if (g != null) {
                g = g.getEnvelope();

                if (first) {
                    srid = g.getSRID();
                    first = false;
                } else {
                    if (g.getSRID() != srid) {
                        g = CrsTransformer.transformToGivenCrs(g, CrsTransformer.createCrsFromSrid(srid));
                    }
                }

                geomList.add(g);
            }
        }

        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);
        Geometry union = factory.buildGeometry(geomList);

        if (union instanceof GeometryCollection) {
            union = ((GeometryCollection)union).union();
        }

        if (mappingComponent != null) {
            final XBoundingBox bbox = new XBoundingBox(union);
            bbox.increase(10);
            mappingComponent.gotoBoundingBoxWithHistory(bbox);
        } else {
            LOG.error("MappingComponent is not set");
        }
    } //GEN-LAST:event_butZoomToSelectionActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void miStatistikActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_miStatistikActionPerformed
        final int count = model.getRowCount();
        final Double[] values = new Double[model.getRowCount()];
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0;
        double mean = 0;
        double stdDeviation = 0;
        int nullCount = 0;

        for (int i = 0; i < model.getRowCount(); ++i) {
            Object val = model.getValueAt(i, popupColumn);

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

        diaExport.setSize(400, 130);
        diaExport.pack();
        diaExport.setResizable(false);
        diaExport.setModal(true);
        StaticSwingTools.showDialog(diaExport);
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
                    final JRDataSource ds = new TableDataSource(table);
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
        changeProcessingModeIntern(false);
        butPaste.setEnabled(isPasteButtonEnabled());
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
            for (final FeatureServiceFeature f : lockedFeatures) {
                if (f instanceof DefaultFeatureServiceFeature) {
                    ((DefaultFeatureServiceFeature)f).undoAll();
                }
            }
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
        final int[] selectedRows = table.getSelectedRows();
        final List<ModifiableFeature> featuresToDelete = new ArrayList<ModifiableFeature>();

        final int ans = JOptionPane.showConfirmDialog(
                AttributeTable.this,
                NbBundle.getMessage(
                    AttributeTable.class,
                    "AttributeTable.butDeleteActionPerformed().text",
                    selectedRows.length),
                NbBundle.getMessage(AttributeTable.class, "AttributeTable.butDeleteActionPerformed().title"),
                JOptionPane.YES_NO_OPTION);

        if (ans != JOptionPane.YES_OPTION) {
            return;
        }

        final WaitingDialogThread<Void> wdt = new WaitingDialogThread<Void>(StaticSwingTools.getParentFrame(this),
                true,
                NbBundle.getMessage(
                    AttributeTable.class,
                    "AttributeTable.butDeleteActionPerformed.WaitingDialogThread"),
                null,
                500) {

                @Override
                protected Void doInBackground() throws Exception {
                    int progress = 0;
                    wd.setMax(selectedRows.length);

                    for (final int row : selectedRows) {
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
                                            lockingObject = locker.lock(dfsf, false);
                                        }
                                        if (!(dfsf instanceof PermissionProvider)
                                                    || ((PermissionProvider)dfsf).hasWritePermissions()) {
                                            dfsf.delete();
                                            featuresToDelete.add(dfsf);
                                        }
                                    } catch (LockAlreadyExistsException ex) {
                                        JOptionPane.showMessageDialog(
                                            AttributeTable.this,
                                            NbBundle.getMessage(
                                                AttributeTable.class,
                                                "AttributeTable.ListSelectionListener.valueChanged().lockexists.message",
                                                featureToDelete.getId(),
                                                ex.getLockMessage()),
                                            NbBundle.getMessage(
                                                AttributeTable.class,
                                                "AttributeTable.ListSelectionListener.valueChanged().lockexists.title"),
                                            JOptionPane.ERROR_MESSAGE);
                                        // leave loop
                                        break;
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

                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        for (final ModifiableFeature fsf : featuresToDelete) {
                            model.removeFeatureServiceFeature((FeatureServiceFeature)fsf);
                        }
                        featureService.retrieve(true);

                        if (tableRuleSet != null) {
                            tableRuleSet.afterSave(model);
                        }
                    } catch (Exception e) {
                        LOG.error("Error while deleting objects", e);
                    }
                }
            };

        wdt.start();
    } //GEN-LAST:event_butDeleteActionPerformed

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
                featureList.add(f);
            }
        } else {
            featureList = model.getFeatureServiceFeatures();

            try {
                if (locker != null) {
                    locker.lock(featureService, false);
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
            final boolean changes = calculationDialog.openPanel(this, featureService, attr, featureList);

            if (changes) {
                for (final FeatureServiceFeature feature : featureList) {
//                    Object newObject = aValue;

//                    if (tableRuleSet != null) {
//                        newObject = tableRuleSet.afterEdit(attrName, rowIndex, feature.getProperty(attrName), aValue);
//                    }

                    if (!lockedFeatures.contains(feature)) {
                        lockedFeatures.add(feature);
                    }
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

        for (final AttributeTable tab : instances) {
            tab.butPaste.setEnabled(isPasteButtonEnabled());
        }
    } //GEN-LAST:event_butCopyActionPerformed

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
     */
    private void copySelectedFeaturesToClipboard() {
        clipboard = getSelectedFeatures();
    }

    /**
     * DOCUMENT ME!
     */
    private void pasteSelectedFeaturesfromClipboard() {
        if ((clipboard != null) && featureService.isEditable()) {
            for (final FeatureServiceFeature feature : clipboard) {
                final FeatureServiceFeature newFeature = featureService.getFeatureFactory().createNewFeature();
                final Map<String, FeatureServiceAttribute> attributeMap = featureService.getFeatureServiceAttributes();
                final Map<String, Object> defaultValues = tableRuleSet.getDefaultValues();
                boolean geometryCompatible = false;

                // check, if the geometry types are compatible
                final String geomType = featureService.getLayerProperties().getFeatureService().getGeometryType();
                if ((geomType != null) && !geomType.equals(AbstractFeatureService.UNKNOWN)) {
                    try {
                        final Class geomTypeClass = Class.forName("com.vividsolutions.jts.geom." + geomType);

                        if ((geomTypeClass != null)
                                    && ((feature.getGeometry() == null)
                                        || geomTypeClass.isInstance(feature.getGeometry()))) {
                            newFeature.setGeometry(feature.getGeometry());
                            geometryCompatible = true;
                        }
                    } catch (Exception e) {
                        // nothing to do
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

                    final boolean hasIdExpression = featureService.getLayerProperties().getIdExpressionType()
                                == LayerProperties.EXPRESSIONTYPE_PROPERTYNAME;
                    for (final String attrKey : attributeMap.keySet()) {
                        if (hasIdExpression
                                    && featureService.getLayerProperties().getIdExpression().equalsIgnoreCase(
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

                addFeature(newFeature);
                modifiedFeatures.add((DefaultFeatureServiceFeature)newFeature);
            }
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
     *
     * @param  ed    DOCUMENT ME!
     * @param  file  DOCUMENT ME!
     */
    private void startExport(ExportDownload ed, final File file) {
        final List<FeatureServiceFeature> features = new ArrayList<FeatureServiceFeature>();
        final int[] selectedRows = table.getSelectedRows();
        int option = 0;

        if (selectedRows != null) {
            for (final int row : selectedRows) {
                final FeatureServiceFeature feature = model.getFeatureServiceFeature(table.convertRowIndexToModel(row));

                if (feature != null) {
                    features.add(feature);
                }
            }
        }

        if (!features.isEmpty()) {
            option = JOptionPane.showOptionDialog(
                    AttributeTable.this,
                    "Alle Features exportieren oder nur die ausgewählten?",
                    "Features exportieren",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[] { "alle Features", "selektierte Features" },
                    "alle Features");
        }

        if (option == -1) {
            return;
        } else if (option == 0) {
            // export all features
            for (int i = 0; i < model.getRowCount(); ++i) {
                features.add(model.getFeatureServiceFeature(table.convertRowIndexToModel(i)));
            }
        }

        if (features.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                NbBundle.getMessage(AttributeTable.class, "AttributeTable.butExportActionPerformed.noFeatures.text"),
                NbBundle.getMessage(AttributeTable.class, "AttributeTable.butExportActionPerformed.noFeatures.title"),
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            // every download needs its own instance of the Download class
            ed = ed.getClass().newInstance();
            File outputFile = file;

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
                    attributeNames);

                DownloadManager.instance().add(ed);
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
     * @param  forceSave  true, if the changed data should be saved without confirmation
     */
    private void saveChangedRows(final boolean forceSave) {
        boolean save = forceSave;

        if (!save && (!modifiedFeatures.isEmpty())) {
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
                return;
            }
        }

        if (save) {
            if ((tableRuleSet != null) && !tableRuleSet.prepareForSave(lockedFeatures, model)) {
                tbProcessing.setSelected(true);
                return;
            }

            final WaitingDialogThread<Void> wdt = new WaitingDialogThread<Void>(StaticSwingTools.getParentFrame(this),
                    true,
                    "Speichere Änderungen",
                    null,
                    200) {

                    @Override
                    protected Void doInBackground() throws Exception {
                        if (featureService instanceof ShapeFileFeatureService) {
                            final List<FeatureServiceFeature> features = new ArrayList<FeatureServiceFeature>();

                            for (int i = 0; i < model.getRowCount(); ++i) {
                                features.add(model.getFeatureServiceFeature(table.convertRowIndexToModel(i)));
                            }

                            try {
                                if ((features.size() > 0)) {
                                    final FeatureCollection fc = new SimpleFeatureCollection(
                                            String.valueOf(System.currentTimeMillis()),
                                            features.toArray(new FeatureServiceFeature[features.size()]),
                                            getAliasAttributeList(true));
                                    String filename = ((ShapeFileFeatureService)featureService).getDocumentURI()
                                                .getPath();
                                    if (filename.contains(".")) {
                                        filename = filename.substring(0, filename.lastIndexOf("."));
                                    }

                                    for (final FeatureServiceFeature fsf : lockedFeatures) {
                                        if (fsf instanceof ModifiableFeature) {
                                            try {
                                                final ModifiableFeature feature = (ModifiableFeature)fsf;
                                                if (tableRuleSet != null) {
                                                    tableRuleSet.beforeSave(fsf);
                                                }
                                                feature.saveChanges();
                                            } catch (Exception e) {
                                                LOG.error("Cannot save object", e);
                                            }
                                        }
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
                            wd.setMax(lockedFeatures.size());
                            int count = 0;

                            for (final FeatureServiceFeature fsf : lockedFeatures) {
                                if (fsf instanceof ModifiableFeature) {
                                    try {
                                        final ModifiableFeature feature = (ModifiableFeature)fsf;
                                        if (tableRuleSet != null) {
                                            tableRuleSet.beforeSave(fsf);
                                        }
                                        feature.saveChanges();
                                    } catch (Exception e) {
                                        LOG.error("Cannot save object", e);
                                    }
                                }

                                wd.setProgress(++count);
                            }
                        }
                        lockedFeatures.clear();
                        modifiedFeatures.clear();

                        if (tableRuleSet != null) {
                            tableRuleSet.afterSave(model);
                        }

                        model.setEditable(false);
                        AttributeTableFactory.getInstance()
                                .processingModeChanged(featureService, tbProcessing.isSelected());

                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    model.fireContentsChanged();
                                }
                            });

                        // reload the layer
                        if (featureService != null) {
                            featureService.retrieve(true);
                        }

                        butUndo.setEnabled(isUndoButtonEnabled());

                        return null;
                    }
                };

            wdt.start();
        } else {
            for (final FeatureServiceFeature f : lockedFeatures) {
                if (f instanceof DefaultFeatureServiceFeature) {
                    ((DefaultFeatureServiceFeature)f).undoAll();
                }
            }
            model.setEditable(false);
            AttributeTableFactory.getInstance().processingModeChanged(featureService, tbProcessing.isSelected());
        }

        butUndo.setEnabled(isUndoButtonEnabled());
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

        for (int i = 0; i < table.getColumnCount(false); ++i) {
            final int modelCol = table.convertColumnIndexToModel(i);

            if (!withGeometryColumn) {
                final FeatureServiceAttribute attr = attributeMap.get(model.getColumnAttributeName(modelCol));
                if ((attr != null) && attr.isGeometry()) {
                    continue;
                }
            }

            final String[] aliasAttr = new String[2];

            aliasAttr[0] = model.getColumnName(modelCol);
            aliasAttr[1] = model.getColumnAttributeName(modelCol);

            attrNames.add(aliasAttr);
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
        int pageCount = itemCount / pageSize;

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

        for (final Object tmp : lockingObjects) {
            try {
                locker.unlock(tmp);
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
        modifiedFeatures.clear();
        butUndo.setEnabled(isUndoButtonEnabled());
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CustomTableModel extends SimpleAttributeTableModel implements PropertyChangeListener {

        //~ Instance fields ----------------------------------------------------

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
                    return editable && tableRuleSet.isColumnEditable(attributeNames[columnIndex])
                                && getFeatureServiceFeature(rowIndex).isEditable();
                } else {
                    return editable && getFeatureServiceFeature(rowIndex).isEditable();
                }
            } else {
                return false;
            }
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
            Object newObject = aValue;

            if (tableRuleSet != null) {
                newObject = tableRuleSet.afterEdit(feature, attrName, rowIndex, feature.getProperty(attrName), aValue);
            }
            feature.setProperty(attrName, newObject);
            modifiedFeatures.add((DefaultFeatureServiceFeature)feature);
            butUndo.setEnabled(isUndoButtonEnabled());
            if (!lockedFeatures.contains(feature)) {
                lockedFeatures.add(feature);
            }
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

            AttributeTable.this.setTableSize();
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
                        fireContentsChanged(e);
                    }
                }
            } else {
                fireContentsChanged();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class FeatureComboItem {

        //~ Instance fields ----------------------------------------------------

        private int id;
        private String name;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FeatureComboItem object.
         *
         * @param  id    DOCUMENT ME!
         * @param  name  DOCUMENT ME!
         */
        public FeatureComboItem(final int id, final String name) {
            this.id = id;
            this.name = name;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getId() {
            return id;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public String toString() {
            return name;
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
                    lab.setForeground(Color.LIGHT_GRAY);
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
            final boolean ret = ++index < model.getRowCount();

            if (!ret) {
                // Set the internal index to the first row, when the return value is false,
                // so that the data source can used from multiple sub reports.
                index = -1;
            }

            return ret;
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

            final Object result = model.getValueAt(table.convertRowIndexToModel(index), col);

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
            return (f != null) && f.getAbsolutePath().toLowerCase().endsWith(".shp");
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
            return (f != null) && f.getAbsolutePath().toLowerCase().endsWith(".csv");
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
            return (f != null) && f.getAbsolutePath().toLowerCase().endsWith(".dbf");
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
            return (f != null) && f.getAbsolutePath().toLowerCase().endsWith(".txt");
        }
    }
}
