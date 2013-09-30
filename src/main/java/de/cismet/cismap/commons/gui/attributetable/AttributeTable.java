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

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureWithId;
import de.cismet.cismap.commons.features.ShapeFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToLayerWorker;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.commons.concurrency.CismetConcurrency;

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
    private XBoundingBox bb;
    private int pageSize = 40;
    private int currentPage = 1;
    private int itemCount;
    private CustomTableModel model;
    private int popupColumn;
    private MappingComponent mappingComponent;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFirstPage;
    private javax.swing.JButton btnLastPage;
    private javax.swing.JButton btnNextPage;
    private javax.swing.JButton btnPrevPage;
    private javax.swing.JButton butAttrib;
    private javax.swing.JButton butClearSelection;
    private javax.swing.JButton butColWidth;
    private javax.swing.JButton butExport;
    private javax.swing.JButton butInvertSelection;
    private javax.swing.JButton butMoveSelectedRows;
    private javax.swing.JButton butPrint;
    private javax.swing.JButton butPrintPreview;
    private javax.swing.JButton butSearch;
    private javax.swing.JButton butSelectAll;
    private javax.swing.JButton butShowCols;
    private javax.swing.JButton butZoomToSelection;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel jpControl;
    private javax.swing.JLabel labWaitingImage;
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
        miFeldberechnung.setEnabled(false);
        miSortieren.setEnabled(false);
        miStatistik.setEnabled(false);

        if (featureService instanceof ShapeFileFeatureService) {
            pageSize = -1;
            jpControl.setVisible(false);
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
                    mouseProcessed(e);
                }

                private void mouseProcessed(final MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupColumn = table.getTableHeader().getColumnModel().getColumnIndexAtX(e.getX());
                        popupColumn = table.convertColumnIndexToModel(popupColumn);
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
                            selectedFeatureIds[i] = model.getFeatureServiceFeature(selectedFeatures[i]).getId();
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
                            }
                        }
                    }
                }
            });

        fillPopupMenu();
        txtCurrentPage.setText("1");
        final Geometry g = ZoomToLayerWorker.getServiceBounds(featureService);
        bb = new XBoundingBox(g);

        try {
            final CrsTransformer transformer = new CrsTransformer(CismapBroker.getInstance().getSrs().getCode());
            bb = transformer.transformBoundingBox(bb);
        } catch (Exception e) {
            LOG.error("Cannot transform CRS.", e);
        }

        loadModel(currentPage);

        final Highlighter alternateRowHighlighter = HighlighterFactory.createAlternateStriping(
                new Color(255, 255, 255),
                new Color(235, 235, 235));
        ((JXTable)table).setHighlighters(alternateRowHighlighter);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void fillPopupMenu() {
        final JMenuItem item = new JMenuItem("Sortieren");
    }

    /**
     * DOCUMENT ME!
     *
     * @param  page  DOCUMENT ME!
     */
    private void loadModel(final int page) {
        panWaiting.setVisible(true);

        final SwingWorker<List<FeatureServiceFeature>, Void> worker =
            new SwingWorker<List<FeatureServiceFeature>, Void>() {

                @Override
                protected List<FeatureServiceFeature> doInBackground() throws Exception {
                    final FeatureFactory factory = featureService.getFeatureFactory();

                    setItemCount(featureService.getFeatureCount(bb));
                    List<FeatureServiceFeature> featureList = null;

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
                                null);
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
                            model = new CustomTableModel(
                                    featureServiceAttributes,
                                    (List<FeatureServiceFeature>)featureList);
                            table.setModel(model);
                        } else {
                            model.setNewFeatureList(featureList);
                        }

                        setTableSize();
                        txtCurrentPage.setText(String.valueOf(page));
                    } catch (Exception e) {
                        LOG.error("Error while retrieving model", e);
                    } finally {
                        panWaiting.setVisible(false);
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
        panWaiting = new javax.swing.JPanel();
        labWaitingImage = new javax.swing.JLabel();
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

        tbProcessing.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.tbProcessing.text"));        // NOI18N
        tbProcessing.setToolTipText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.tbProcessing.toolTipText")); // NOI18N
        tbProcessing.setFocusable(false);
        tbProcessing.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbProcessing.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jToolBar1, gridBagConstraints);

        panWaiting.setBackground(new Color(255, 255, 255, 150));
        panWaiting.setLayout(new java.awt.GridBagLayout());

        labWaitingImage.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/ajax-loader.gif"))); // NOI18N
        labWaitingImage.setText(org.openide.util.NbBundle.getMessage(
                AttributeTable.class,
                "AttributeTable.labWaitingImage.text"));                                                  // NOI18N
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
        // TODO add your handling code here:
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
            final Geometry tmpGeo = model.getGeometryFromRow(row);

            if (geo == null) {
                geo = tmpGeo;
            } else {
                if (tmpGeo != null) {
                    geo = geo.union(tmpGeo);
                }
            }
        }

        if (mappingComponent != null) {
            mappingComponent.gotoBoundingBoxWithHistory(new XBoundingBox(geo));
        } else {
            LOG.error("MappingComponent is not set");
        }
    } //GEN-LAST:event_butZoomToSelectionActionPerformed

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

            if (model.getColumnClass(i).equals(String.class)) {
                for (int row = 0; row < model.getRowCount(); ++row) {
                    final int tmpSize = (int)fmetrics.getStringBounds(String.valueOf(model.getValueAt(row, i)),
                                table.getGraphics()).getWidth();

                    if ((tmpSize > size) && (tmpSize < MAX_COLUMN_SIZE)) {
                        size = tmpSize;
                    }
                }
            }

            if (i == (columnCount - 1)) {
                if ((totalSize + size + 30) < tableScrollPane.getSize().getWidth()) {
                    size = (int)tableScrollPane.getSize().getWidth() - 30 - totalSize;
                }
            }
            totalSize += size;
            columnModel.getColumn(i).setMinWidth(size + 30);
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

        mappingComponent.getFeatureCollection().addFeatureCollectionListener(new FeatureCollectionListener() {

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
                        final FeatureServiceFeature feature = tableFeatures.get(index);

                        if (selectedFeatures.contains(feature)) {
                            table.addRowSelectionInterval(index, index);
                        } else {
                            table.removeRowSelectionInterval(index, index);
                        }
                    }

                    table.getSelectionModel().setValueIsAdjusting(false);
                }

                @Override
                public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
                }

                @Override
                public void featureCollectionChanged() {
                }
            });
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CustomTableModel implements TableModel {

        //~ Instance fields ----------------------------------------------------

        private String[] attributeAlias;
        private String[] attributeNames;
        private Map<String, FeatureServiceAttribute> featureServiceAttributes;
        private List<FeatureServiceFeature> featureList;
        private List<TableModelListener> listener = new ArrayList<TableModelListener>();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CustomTableModel object.
         *
         * @param  featureServiceAttributes  DOCUMENT ME!
         * @param  propertyContainer         DOCUMENT ME!
         */
        public CustomTableModel(final Map<String, FeatureServiceAttribute> featureServiceAttributes,
                final List<FeatureServiceFeature> propertyContainer) {
            this.featureServiceAttributes = featureServiceAttributes;
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
            attributeNames = new String[featureServiceAttributes.size()];
            attributeAlias = new String[featureServiceAttributes.size()];

            for (final String attributeName : featureServiceAttributes.keySet()) {
                attributeNames[index] = attributeName;
                attributeAlias[index++] = attributeName;
            }
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

        @Override
        public int getRowCount() {
            if (featureList == null) {
                return 0;
            } else {
                return featureList.size();
            }
        }

        @Override
        public int getColumnCount() {
            if (attributeAlias == null) {
                return 0;
            } else {
                return attributeAlias.length;
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
            final List<String> geometryColumns = new ArrayList<String>();
            Geometry resultGeom = null;

            for (final String key : featureServiceAttributes.keySet()) {
                final FeatureServiceAttribute attr = featureServiceAttributes.get(key);

                if (attr.isGeometry()) {
                    geometryColumns.add(attr.getName());
                }
            }

            for (final String name : geometryColumns) {
                final Object value = featureList.get(row).getProperty(name);
                Geometry geo = null;

                if (value instanceof Geometry) {
                    geo = ((Geometry)value);
                } else if (value instanceof org.deegree.model.spatialschema.Geometry) {
                    final org.deegree.model.spatialschema.Geometry geom = ((org.deegree.model.spatialschema.Geometry)
                            value);
                    try {
                        geo = JTSAdapter.export(geom);
                    } catch (GeometryException e) {
                        LOG.error("Error while transforming deegree geometry to jts geometry.", e);
                    }
                }

                if (geo != null) {
                    if (resultGeom == null) {
                        resultGeom = geo;
                    } else {
                        resultGeom = resultGeom.union(geo);
                    }
                }
            }

            return resultGeom;
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

        @Override
        public String getColumnName(final int columnIndex) {
            return attributeAlias[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            Object value = featureList.get(rowIndex).getProperty(attributeNames[columnIndex]);

            if (value instanceof Geometry) {
                value = ((Geometry)value).getGeometryType();
            } else if (value instanceof org.deegree.model.spatialschema.Geometry) {
                final org.deegree.model.spatialschema.Geometry geom = ((org.deegree.model.spatialschema.Geometry)value);
                try {
                    value = JTSAdapter.export(geom).getGeometryType();
                } catch (GeometryException e) {
                    LOG.error("Error while transforming deegree geometry to jts geometry.", e);
                }
            }

            return value;
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addTableModelListener(final TableModelListener l) {
            listener.add(l);
        }

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
            if ((row > 0) && (row < attributeAlias.length)) {
                attributeAlias[row] = name;
                fireContentsChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
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
    }
}
