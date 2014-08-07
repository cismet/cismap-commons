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

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JRViewer;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;
import org.deegree.io.shpapi.shape_new.ShapeFile;
import org.deegree.io.shpapi.shape_new.ShapeFileWriter;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.lang.reflect.Method;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureWithId;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.style.BasicStyle;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToLayerWorker;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.ExportCsvDownload;
import de.cismet.cismap.commons.tools.ExportDownload;
import de.cismet.cismap.commons.tools.ExportShapeDownload;
import de.cismet.cismap.commons.tools.ExportTxtDownload;
import de.cismet.cismap.commons.tools.FeatureTools;
import de.cismet.cismap.commons.tools.SimpleFeatureCollection;

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

    //~ Instance fields --------------------------------------------------------

    private AbstractFeatureService featureService;
    // bb will be null, if the featureService has no geometries
    private XBoundingBox bb;
    private int pageSize = 40;
    private int currentPage = 1;
    private int itemCount;
    private CustomTableModel model;
    private int popupColumn;
    private MappingComponent mappingComponent;
    private boolean selectionChangeFromMap = false;
    private FeatureCollectionListener featureCollectionListener;
    private List<FeatureServiceFeature> changedFeatures = new ArrayList<FeatureServiceFeature>();
    private DefaultAttributeTableRuleSet tableRuleSet = new DefaultAttributeTableRuleSet();
    private FeatureLockingInterface locker;
    private List<Object> lockingObjects = new ArrayList<Object>();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFirstPage;
    private javax.swing.JButton btnLastPage;
    private javax.swing.JButton btnNextPage;
    private javax.swing.JButton btnPrevPage;
    private javax.swing.JButton butAttrib;
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butClearSelection;
    private javax.swing.JButton butColWidth;
    private javax.swing.JButton butExpOk;
    private javax.swing.JButton butExport;
    private javax.swing.JButton butInvertSelection;
    private javax.swing.JButton butMoveSelectedRows;
    private javax.swing.JButton butOk;
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
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JComboBox jcFeatures;
    private javax.swing.JComboBox jcFormat;
    private javax.swing.JPanel jpControl;
    private javax.swing.JLabel labStat;
    private javax.swing.JLabel labStatCol;
    private javax.swing.JLabel labWaitingImage;
    private javax.swing.JLabel lblCountLab;
    private javax.swing.JLabel lblCountVal;
    private javax.swing.JLabel lblFeature;
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
    private javax.swing.JPanel panWaiting;
    private org.jdesktop.swingx.JXTable table;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JToggleButton tbAlias;
    private javax.swing.JToggleButton tbLookup;
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
        tbAlias.setVisible(false);
        tbProcessing.setEnabled(featureService.isEditable());
        butSearch.setVisible(false);
        tbLookup.setVisible(false);
        butUndo.setVisible(false);
        locker = FeatureLockerFactory.getInstance().getLockerForFeatureService(featureService);

        tableRuleSet = getTableRuleSetForFeatureService(featureService);

        jcFeatures.setModel(new DefaultComboBoxModel(
                new Object[] {
                    new FeatureComboItem(
                        1,
                        NbBundle.getMessage(AttributeTable.class, "AttributeTable.FeatureComboItem.allFeatures")),
                    new FeatureComboItem(
                        2,
                        NbBundle.getMessage(AttributeTable.class, "AttributeTable.FeatureComboItem.selectedFeatures"))
                }));
        jcFormat.setModel(new DefaultComboBoxModel(
                new Object[] { new ExportTxtDownload(), new ExportCsvDownload(), new ExportShapeDownload() }));

//        if ((featureService instanceof ShapeFileFeatureService) || (featureService instanceof H2FeatureService)) {
        pageSize = -1;
        jpControl.setVisible(false);
//        }

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
                    mouseProcessed(e);
                }

                private void mouseProcessed(final MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupColumn = table.getTableHeader().getColumnModel().getColumnIndexAtX(e.getX());
                        popupColumn = table.convertColumnIndexToModel(popupColumn);

                        miStatistik.setEnabled(model.isNumeric(popupColumn));

                        jPopupMenu1.show((Component)e.getSource(), e.getX(), e.getY());
                    }
                }
            });

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(final ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        final List<PFeature> features = new ArrayList<PFeature>();
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

                                if (!selectionChangeFromMap) {
                                    if (selected != pfeature.isSelected()) {
                                        pfeature.setSelected(selected);
                                    }
                                    if (selected) {
                                        final SelectionListener sl = (SelectionListener)
                                            mappingComponent.getInputEventListener().get(MappingComponent.SELECT);
                                        sl.addSelectedFeature(pfeature);
                                    } else {
                                        final SelectionListener sl = (SelectionListener)
                                            mappingComponent.getInputEventListener().get(MappingComponent.SELECT);
                                        sl.removeSelectedFeature(pfeature);
                                    }
                                }
                            }
                        }

                        if (tbProcessing.isSelected() && !selectionChangeFromMap) {
                            final int[] rows = table.getSelectedRows();

                            for (final int row : rows) {
                                final FeatureServiceFeature feature = model.getFeatureServiceFeature(
                                        table.convertRowIndexToModel(row));
                                makeFeatureEditable(feature);
                            }
                        }
                    }
                }
            });

        table.setDefaultRenderer(String.class, new AttributeTableCellRenderer());
        table.setDefaultRenderer(Boolean.class, new AttributeTableCellRenderer());
        table.setDefaultRenderer(Date.class, new AttributeTableCellRenderer());
        table.setDefaultRenderer(Number.class, new NumberCellRenderer());

        txtCurrentPage.setText("1");
        final Geometry g = ZoomToLayerWorker.getServiceBounds(featureService);

        if (g != null) {
            bb = new XBoundingBox(g);

            try {
                final CrsTransformer transformer = new CrsTransformer(CismapBroker.getInstance().getSrs().getCode());
                bb = transformer.transformBoundingBox(bb);
            } catch (Exception e) {
                LOG.error("Cannot transform CRS.", e);
            }
        } else {
            bb = null;
        }

        loadModel(currentPage);

//        final Highlighter alternateRowHighlighter = HighlighterFactory.createAlternateStriping(
//                new Color(255, 255, 255),
//                new Color(235, 235, 235));
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

        featureCollectionListener = new FeatureCollectionListener() {

                @Override
                public void featuresAdded(final FeatureCollectionEvent fce) {
                }

                @Override
                public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
                }

                @Override
                public void featuresRemoved(final FeatureCollectionEvent fce) {
                }

                @Override
                public void featuresChanged(final FeatureCollectionEvent fce) {
                }

                @Override
                public void featureSelectionChanged(final FeatureCollectionEvent fce) {
                    final Collection<Feature> features = fce.getFeatureCollection().getSelectedFeatures();
                    final List<FeatureServiceFeature> selectedFeatures = new ArrayList<FeatureServiceFeature>();
                    final List<FeatureServiceFeature> tableFeatures = model.getFeatureServiceFeatures();
                    final LayerProperties layerProperties = featureService.getLayerProperties();

                    for (final Feature feature : features) {
                        if (feature instanceof FeatureServiceFeature) {
                            if (((FeatureServiceFeature)feature).getLayerProperties() == layerProperties) {
                                selectedFeatures.add((FeatureServiceFeature)feature);
                            }
                        }
                    }

                    table.getSelectionModel().setValueIsAdjusting(true);
                    for (int index = 0; index < tableFeatures.size(); ++index) {
                        final FeatureServiceFeature feature = tableFeatures.get(table.convertRowIndexToModel(index));

                        if (selectedFeatures.contains(feature)) {
                            table.addRowSelectionInterval(index, index);
                        } else {
                            table.removeRowSelectionInterval(index, index);
                        }
                    }

                    selectionChangeFromMap = true;
                    table.getSelectionModel().setValueIsAdjusting(false);
                    selectionChangeFromMap = false;
                }

                @Override
                public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
                }

                @Override
                public void featureCollectionChanged() {
                }
            };
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   featureService  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static DefaultAttributeTableRuleSet getTableRuleSetForFeatureService(
            final AbstractFeatureService featureService) {
        final String ruleSetName = camelize(featureService.getName()) + "RuleSet";

        try {
            final Class ruleSetClass = Class.forName("de.cismet.cismap.custom.attributerule." + ruleSetName);
            final Object o = ruleSetClass.newInstance();
            if (o instanceof DefaultAttributeTableRuleSet) {
                return (DefaultAttributeTableRuleSet)o;
            }
        } catch (Exception e) {
            // nothing to do
        }

        return null;
    }

    /**
     * Locks the given feature, if a corresponding locker exists and make the feature editable.
     *
     * @param  feature  the feature to make editable
     */
    private void makeFeatureEditable(final FeatureServiceFeature feature) {
        if ((feature != null) && !feature.isEditable()) {
            try {
                if (locker != null) {
                    lockingObjects.add(locker.lock(feature));
                }
                feature.setEditable(true);
                if (!changedFeatures.contains(feature)) {
                    changedFeatures.add(feature);
                    ((DefaultFeatureServiceFeature)feature).addPropertyChangeListener(model);
                }
            } catch (LockAlreadyExistsException ex) {
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

    /**
     * camelizes the given string.
     *
     * @param   toCamelize  string to camalize
     *
     * @return  the camalized string
     */
    public static String camelize(final String toCamelize) {
        boolean upperCase = true;
        final char[] result = new char[toCamelize.length()];
        int resultPosition = 0;
        for (int i = 0; i < toCamelize.length(); ++i) {
            char current = toCamelize.charAt(i);
            if (Character.isLetterOrDigit(current)) {
                if (upperCase) {
                    current = Character.toUpperCase(current);
                    upperCase = false;
                } else {
                    current = Character.toLowerCase(current);
                }
                result[resultPosition++] = current;
            } else {
                upperCase = true;
            }
        }
        return String.valueOf(result, 0, resultPosition);
    }

    /**
     * Load the model to show into the table.
     *
     * @param  page  the page to show. At the moment, all data will be displayed on one page
     */
    private void loadModel(final int page) {
        panWaiting.setVisible(true);
        ((JXBusyLabel)labWaitingImage).setBusy(true);

        final SwingWorker<List<FeatureServiceFeature>, Void> worker =
            new SwingWorker<List<FeatureServiceFeature>, Void>() {

                @Override
                protected List<FeatureServiceFeature> doInBackground() throws Exception {
                    final FeatureFactory factory = featureService.getFeatureFactory();

                    setItemCount(featureService.getFeatureCount(bb));
                    List<FeatureServiceFeature> featureList;

                    if (pageSize != -1) {
                        featureList = factory.createFeatures(featureService.getQuery(),
                                bb,
                                null,
                                (page - 1)
                                        * pageSize,
                                pageSize,
                                null);
                    } else {
                        featureList = factory.createFeatures(featureService.getQuery(),
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
                                    (List<FeatureServiceFeature>)featureList);
                            table.setModel(model);
                        } else {
                            model.setNewFeatureList(featureList);
                        }

                        applySelection();
                        setTableSize();
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
        lblFeature = new javax.swing.JLabel();
        lblFormat = new javax.swing.JLabel();
        jcFeatures = new javax.swing.JComboBox();
        jcFormat = new javax.swing.JComboBox();
        butExpOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        jToolBar1 = new javax.swing.JToolBar();
        butPrintPreview = new javax.swing.JButton();
        butPrint = new javax.swing.JButton();
        butExport = new javax.swing.JButton();
        butAttrib = new javax.swing.JButton();
        butSearch = new javax.swing.JButton();
        tbLookup = new javax.swing.JToggleButton();
        tbAlias = new javax.swing.JToggleButton();
        tbProcessing = new javax.swing.JToggleButton();
        butMoveSelectedRows = new javax.swing.JButton();
        butSelectAll = new javax.swing.JButton();
        butInvertSelection = new javax.swing.JButton();
        butClearSelection = new javax.swing.JButton();
        butZoomToSelection = new javax.swing.JButton();
        butColWidth = new javax.swing.JButton();
        butShowCols = new javax.swing.JButton();
        butUndo = new javax.swing.JButton();
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

        lblFeature.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.lblFeature.text")); // NOI18N
        lblFeature.setMaximumSize(new java.awt.Dimension(100, 20));
        lblFeature.setMinimumSize(new java.awt.Dimension(100, 20));
        lblFeature.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel5.add(lblFeature, gridBagConstraints);

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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel5.add(jcFeatures, gridBagConstraints);

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

        butAttrib.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butAttrib.text")); // NOI18N
        butAttrib.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.butAttrib.toolTipText"));                                                               // NOI18N
        butAttrib.setFocusable(false);
        butAttrib.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butAttrib.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(butAttrib);

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

        tbLookup.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-brokenlink.png")));     // NOI18N
        tbLookup.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.tbLookup.text")); // NOI18N
        tbLookup.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.tbLookup.toolTipText"));                                                              // NOI18N
        tbLookup.setFocusable(false);
        tbLookup.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbLookup.setSelectedIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-link.png")));           // NOI18N
        tbLookup.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(tbLookup);

        tbAlias.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.tbAlias.text")); // NOI18N
        tbAlias.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.tbAlias.toolTipText"));                                                             // NOI18N
        tbAlias.setFocusable(false);
        tbAlias.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbAlias.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(tbAlias);

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
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
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(panWaiting, gridBagConstraints);

        table.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {},
                new String[] {}));
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tableScrollPane.setViewportView(table);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tableScrollPane, gridBagConstraints);

        jpControl.setLayout(new java.awt.GridBagLayout());

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
        gridBagConstraints.gridy = 2;
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

                        final JRViewer aViewer = new JRViewer(jasperPrint);
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
        Geometry geo = null;

        for (final int row : selectedRows) {
            final Geometry tmpGeo = model.getGeometryFromRow(table.convertRowIndexToModel(row));

            if (geo == null) {
                geo = tmpGeo;
            } else {
                if (tmpGeo != null) {
                    geo = geo.union(tmpGeo);
                }
            }
        }

        if (mappingComponent != null) {
            final XBoundingBox bbox = new XBoundingBox(geo);
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
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
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

        diaStatistic.setSize(400, 320);
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
        diaExport.setSize(400, 150);
        diaExport.setResizable(false);
        diaExport.setModal(true);
        StaticSwingTools.showDialog(diaExport);
    }                                                                             //GEN-LAST:event_butExportActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butExpOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butExpOkActionPerformed
        diaExport.setVisible(false);

        final List<FeatureServiceFeature> features = new ArrayList<FeatureServiceFeature>();
        final FeatureComboItem featureComboItem = (FeatureComboItem)jcFeatures.getSelectedItem();

        if (featureComboItem.getId() == 2) {
            // export all selected features
            final int[] selectedRows = table.getSelectedRows();

            for (final int row : selectedRows) {
                final FeatureServiceFeature feature = model.getFeatureServiceFeature(table.convertRowIndexToModel(row));

                if (feature != null) {
                    features.add(feature);
                }
            }
        } else if (featureComboItem.getId() == 1) {
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

        if (DownloadManagerDialog.showAskingForUserTitle(this)) {
            try {
                ExportDownload ed = (ExportDownload)jcFormat.getSelectedItem();
                // every download needs its own instance of the Download class
                ed = ed.getClass().newInstance();
                final List<String[]> attributeNames = getAliasAttributeList();
                ed.init(featureService.getName(),
                    ed.getDefaultExtension(),
                    features.toArray(new FeatureServiceFeature[features.size()]),
                    featureService,
                    attributeNames);

                DownloadManager.instance().add(ed);
            } catch (Exception e) {
                LOG.error("The ExportDownload class has possibly no public constructor without arguments.", e);
            }
        }
    } //GEN-LAST:event_butExpOkActionPerformed

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
        model.setEditable(tbProcessing.isSelected());

        if (!tbProcessing.isSelected()) {
            saveChangedRows();
        }
        butUndo.setVisible(tbProcessing.isSelected());
    } //GEN-LAST:event_tbProcessingActionPerformed

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
            for (final FeatureServiceFeature f : changedFeatures) {
                if (f instanceof DefaultFeatureServiceFeature) {
                    ((DefaultFeatureServiceFeature)f).undoAll();
                }
            }
        }
    } //GEN-LAST:event_butUndoActionPerformed

    /**
     * DOCUMENT ME!
     */
    private void saveChangedRows() {
        if ((tableRuleSet != null) && !tableRuleSet.prepareForSave(model)) {
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
                            if ((features != null) && (features.size() > 0)) {
                                final FeatureCollection fc = new SimpleFeatureCollection(
                                        String.valueOf(System.currentTimeMillis()),
                                        features.toArray(new FeatureServiceFeature[features.size()]),
                                        getAliasAttributeList());
                                String filename = ((ShapeFileFeatureService)featureService).getDocumentURI().getPath();
                                if (filename.indexOf(".") != -1) {
                                    filename = filename.substring(0, filename.lastIndexOf("."));
                                }

                                for (final FeatureServiceFeature fsf : changedFeatures) {
                                    if (fsf instanceof DefaultFeatureServiceFeature) {
                                        try {
                                            final DefaultFeatureServiceFeature feature = (DefaultFeatureServiceFeature)
                                                fsf;
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
                        wd.setMax(changedFeatures.size());
                        int count = 0;

                        for (final FeatureServiceFeature fsf : changedFeatures) {
                            if (fsf instanceof DefaultFeatureServiceFeature) {
                                try {
                                    final DefaultFeatureServiceFeature feature = (DefaultFeatureServiceFeature)fsf;
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
                    changedFeatures.clear();

                    if (tableRuleSet != null) {
                        tableRuleSet.afterSave(model);
                    }

                    model.setEditable(false);

                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                model.fireContentsChanged();
                            }
                        });
                    return null;
                }
            };

        wdt.start();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<String[]> getAliasAttributeList() {
        final List<String[]> attrNames = new ArrayList<String[]>();

        for (int i = 0; i < table.getColumnCount(false); ++i) {
            final int modelCol = table.convertColumnIndexToModel(i);
            final String[] aliasAttr = new String[2];

            aliasAttr[0] = model.getColumnName(modelCol);
            aliasAttr[1] = model.getColumnAttributeName(modelCol);

            attrNames.add(aliasAttr);
        }

        return attrNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   val  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String trimNumberString(final String val) {
        String res = String.valueOf(val);

        if (res.indexOf(".") != -1) {
            // remove all leading points and zeros
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
                }
            }

            totalSize += size;
            columnModel.getColumn(i).setPreferredWidth(size + 30);
        }

        table.setMinimumSize(new Dimension(totalSize + 20, 50));
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
        mappingComponent.getFeatureCollection().addFeatureCollectionListener(featureCollectionListener);

        if (model != null) {
            applySelection();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void applySelection() {
        final SelectionListener sl = (SelectionListener)mappingComponent.getInputEventListener()
                    .get(MappingComponent.SELECT);
        final Collection<PFeature> selectedPFeatures = sl.getAllSelectedPFeatures();
        final Collection<Feature> selectedFeatures = new ArrayList<Feature>();

        for (final PFeature f : selectedPFeatures) {
            selectedFeatures.add(f.getFeature());
        }

        final DefaultFeatureCollection dfc = new DefaultFeatureCollection();
        dfc.addToSelection(selectedFeatures);
        final FeatureCollectionEvent e = new FeatureCollectionEvent(dfc, selectedFeatures);
        featureCollectionListener.featureSelectionChanged(e);
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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CustomTableModel implements TableModel, PropertyChangeListener {

        //~ Instance fields ----------------------------------------------------

        private String[] attributeAlias;
        private String[] attributeNames;
        private Map<String, FeatureServiceAttribute> featureServiceAttributes;
        private String[] additionalAttributes = new String[0];
        private List<String> orderedFeatureServiceAttributes;
        private List<FeatureServiceFeature> featureList;
        private List<TableModelListener> listener = new ArrayList<TableModelListener>();
        private boolean editable = false;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CustomTableModel object.
         *
         * @param  orderedFeatureServiceAttributes  DOCUMENT ME!
         * @param  featureServiceAttributes         DOCUMENT ME!
         * @param  propertyContainer                DOCUMENT ME!
         */
        public CustomTableModel(final List<String> orderedFeatureServiceAttributes,
                final Map<String, FeatureServiceAttribute> featureServiceAttributes,
                final List<FeatureServiceFeature> propertyContainer) {
            this.featureServiceAttributes = featureServiceAttributes;
            this.orderedFeatureServiceAttributes = orderedFeatureServiceAttributes;
            this.featureList = propertyContainer;

            fillHeaderArrays();
            fireContentsChanged();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void fillHeaderArrays() {
            int index = 0;
            attributeNames = new String[attributeCount()];
            attributeAlias = new String[attributeCount()];

            for (final String attributeName : orderedFeatureServiceAttributes) {
                final FeatureServiceAttribute fsa = featureServiceAttributes.get(attributeName);

                if ((fsa == null) || fsa.isVisible()) {
                    attributeNames[index] = attributeName;
                    String aliasName = attributeName;

                    if ((fsa != null) && !fsa.getAlias().equals("")) {
                        final String alias = fsa.getAlias();

                        if (alias != null) {
                            aliasName = alias;
                        }
                    }

                    if (aliasName.startsWith("app:")) {
                        attributeAlias[index++] = aliasName.substring(4);
                    } else {
                        attributeAlias[index++] = aliasName;
                    }
                }
            }

            if (tableRuleSet != null) {
                final String[] fields = tableRuleSet.getAdditionalFieldNames();

                if (fields != null) {
                    additionalAttributes = fields;
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private int attributeCount() {
            int count = 0;

            for (final String key : orderedFeatureServiceAttributes) {
                final FeatureServiceAttribute fsa = featureServiceAttributes.get(key);
                if (fsa.isVisible()) {
                    ++count;
                }
            }

            return count;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  propertyContainer  DOCUMENT ME!
         */
        public void setNewFeatureList(final List<FeatureServiceFeature> propertyContainer) {
            this.featureList = propertyContainer;
            fireContentsChanged();
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int getRowCount() {
            if (featureList == null) {
                return 0;
            } else {
                return featureList.size();
            }
        }

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
            boolean allLocksRemoved = true;

            if (this.editable && !editable) {
                // set all feature to editable = false
                for (final FeatureServiceFeature fsf : featureList) {
                    fsf.setEditable(false);
                }

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
                            CustomTableModel.class,
                            "AttributeTable.CustomTableModel.setEditable.message"),
                        NbBundle.getMessage(
                            CustomTableModel.class,
                            "AttributeTable.CustomTableModel.setEditable.message"),
                        JOptionPane.ERROR_MESSAGE);
                }

                lockingObjects.clear();
            }

            this.editable = editable;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int getColumnCount() {
            if (attributeAlias == null) {
                return 0;
            } else {
                return attributeAlias.length + additionalAttributes.length;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  index  DOCUMENT ME!
         */
        public void moveRowUp(final int index) {
            final FeatureServiceFeature propToMove = featureList.get(index);

            for (int i = index; i > 0; --i) {
                featureList.set(i, featureList.get(i - 1));
            }

            featureList.set(0, propToMove);

            fireContentsChanged();
        }

        /**
         * DOCUMENT ME!
         *
         * @param   row  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Geometry getGeometryFromRow(final int row) {
            // The geometries from the attributes has no crs. At least, if they come from a shape file final
            // List<String> geometryColumns = new ArrayList<String>(); Geometry resultGeom = null;
            //
            // for (final String key : featureServiceAttributes.keySet()) { final FeatureServiceAttribute attr =
            // featureServiceAttributes.get(key);
            //
            // if (attr.isGeometry()) { geometryColumns.add(attr.getName()); } }
            //
            // for (final String name : geometryColumns) { final Object value = featureList.get(row).getProperty(name);
            // Geometry geo = null;
            //
            // if (value instanceof Geometry) { geo = ((Geometry)value); } else if (value instanceof
            // org.deegree.model.spatialschema.Geometry) { final org.deegree.model.spatialschema.Geometry geom =
            // ((org.deegree.model.spatialschema.Geometry) value); try { geo = JTSAdapter.export(geom); } catch
            // (GeometryException e) { LOG.error("Error while transforming deegree geometry to jts geometry.", e); }
            // }
            //
            // if (geo != null) { if (resultGeom == null) { resultGeom = geo; } else { resultGeom =
            // resultGeom.union(geo); } } }
            //
            // resultGeom.setSRID(featureList.get(row).getGeometry().getSRID());

            // the same geometry, that is shown on the map, should be returned
            return featureList.get(row).getGeometry();
        }

        /**
         * DOCUMENT ME!
         *
         * @param   col  row DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isNumeric(final int col) {
            final String key = attributeNames[col];
            final FeatureServiceAttribute attr = featureServiceAttributes.get(key);

            if ((attr != null)
                        && (attr.getType().equals(String.valueOf(Types.INTEGER))
                            || attr.getType().equals(String.valueOf(Types.BIGINT))
                            || attr.getType().equals(String.valueOf(Types.SMALLINT))
                            || attr.getType().equals(String.valueOf(Types.TINYINT))
                            || attr.getType().equals(String.valueOf(Types.DOUBLE))
                            || attr.getType().equals(String.valueOf(Types.FLOAT))
                            || attr.getType().equals(String.valueOf(Types.DECIMAL))
                            || attr.getType().equals("xsd:float")
                            || attr.getType().equals("xsd:decimal")
                            || attr.getType().equals("xsd:double")
                            || attr.getType().equals("xsd:integer"))) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   row  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public FeatureServiceFeature getFeatureServiceFeature(final int row) {
            return featureList.get(row);
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public List<FeatureServiceFeature> getFeatureServiceFeatures() {
            return featureList;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   columnIndex  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public String getColumnName(final int columnIndex) {
            if (columnIndex < attributeAlias.length) {
                return attributeAlias[columnIndex];
            } else {
                return additionalAttributes[columnIndex - attributeAlias.length];
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   columnIndex  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getColumnAttributeName(final int columnIndex) {
            if (columnIndex < attributeAlias.length) {
                return attributeNames[columnIndex];
            } else {
                return additionalAttributes[columnIndex - attributeAlias.length];
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   columnIndex  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            if (columnIndex < attributeAlias.length) {
                final String key = attributeNames[columnIndex];
                final FeatureServiceAttribute attr = featureServiceAttributes.get(key);

                return FeatureTools.getClass(attr);
            } else {
                return tableRuleSet.getAdditionalFieldClass(columnIndex - attributeAlias.length);
            }
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
         * @param   rowIndex     DOCUMENT ME!
         * @param   columnIndex  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            if (columnIndex < attributeAlias.length) {
                Object value = featureList.get(rowIndex).getProperty(attributeNames[columnIndex]);

                if (value instanceof Geometry) {
                    value = ((Geometry)value).getGeometryType();
                } else if (value instanceof org.deegree.model.spatialschema.Geometry) {
                    final org.deegree.model.spatialschema.Geometry geom = ((org.deegree.model.spatialschema.Geometry)
                            value);
                    try {
                        value = JTSAdapter.export(geom).getGeometryType();
                    } catch (GeometryException e) {
                        LOG.error("Error while transforming deegree geometry to jts geometry.", e);
                    }
                }

                return value;
            } else {
                return tableRuleSet.getAdditionalFieldValue(columnIndex - attributeAlias.length,
                        featureList.get(rowIndex));
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
                newObject = tableRuleSet.afterEdit(attrName, rowIndex, feature.getProperty(attrName), aValue);
            }
            feature.setProperty(attrName, newObject);
            if (!changedFeatures.contains(feature)) {
                changedFeatures.add(feature);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  l  DOCUMENT ME!
         */
        @Override
        public void addTableModelListener(final TableModelListener l) {
            listener.add(l);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  l  DOCUMENT ME!
         */
        @Override
        public void removeTableModelListener(final TableModelListener l) {
            listener.remove(l);
        }

        /**
         * DOCUMENT ME!
         *
         * @param  col  DOCUMENT ME!
         */
        public void hideColumn(final int col) {
            // todo fuer virtuelle Spalten
            this.attributeNames = remove(this.attributeNames, col);
            this.attributeAlias = remove(this.attributeAlias, col);
            fireContentsChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
        }

        /**
         * DOCUMENT ME!
         */
        public void showColumns() {
            fillHeaderArrays();
            fireContentsChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
        }

        /**
         * DOCUMENT ME!
         *
         * @param  row   DOCUMENT ME!
         * @param  name  DOCUMENT ME!
         */
        public void setColumnName(final int row, final String name) {
            if ((row >= 0) && (row < attributeAlias.length)) {
                attributeAlias[row] = name;
                fireContentsChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
            } else if ((row - attributeAlias.length) >= 0) {
                additionalAttributes[row - attributeAlias.length] = name;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   array  DOCUMENT ME!
         * @param   index  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String[] remove(final String[] array, final int index) {
            if ((index >= 0) && (index < array.length)) {
                final String[] resultArray = new String[array.length - 1];
                int indexResArray = 0;

                for (int i = 0; i < array.length; ++i) {
                    if (i != index) {
                        resultArray[indexResArray++] = array[i];
                    }
                }

                return resultArray;
            } else {
                return array;
            }
        }

        /**
         * DOCUMENT ME!
         */
        private void fireContentsChanged() {
            final TableModelEvent e = new TableModelEvent(this);

            for (final TableModelListener tmp : listener) {
                tmp.tableChanged(e);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  e  DOCUMENT ME!
         */
        private void fireContentsChanged(final TableModelEvent e) {
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
     * This highlighter considers the editable attribute of the displayed features.
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
     * DOCUMENT ME!
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
            final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
                return String.valueOf(result);
            } else {
                return null;
            }
        }
    }
}
