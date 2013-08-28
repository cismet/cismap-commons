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
import de.cismet.cismap.commons.CrsTransformer;

import org.apache.log4j.Logger;

import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.PropertyContainer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToLayerWorker;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.commons.concurrency.CismetConcurrency;
import de.cismet.tools.CismetThreadPool;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AttributeTable extends javax.swing.JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(AttributeTable.class);
    private final static int MAX_COLUMN_SIZE = 200;
    private AbstractFeatureService featureService;
    private XBoundingBox bb;
    private int pageSize = 40;
    private int currentPage = 1;
    private int itemCount;
    private CustomTableModel model;
    private int popupColumn;

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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JToolBar jToolBar1;
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
     * @param  featureServiceAttributes  DOCUMENT ME!
     * @param  propertyContainer         DOCUMENT ME!
     */
    public AttributeTable(AbstractFeatureService featureService) {
        this.featureService = featureService;
        initComponents();
        miFeldberechnung.setEnabled(false);
        miSortieren.setEnabled(false);
        miStatistik.setEnabled(false);

        table.getTableHeader().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                mouseProcessed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseProcessed(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseProcessed(e);
            }
            
                        
            private void mouseProcessed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupColumn = table.getTableHeader().getColumnModel().getColumnIndexAtX(e.getX());
                    popupColumn = table.convertColumnIndexToModel(popupColumn);
                    jPopupMenu1.show((Component)e.getSource(), e.getX(), e.getY());
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

    private void fillPopupMenu() {
        JMenuItem item = new JMenuItem("Sortieren");
    }
    
    private void loadModel(final int page) {
        panWaiting.setVisible(true);
        
        SwingWorker<List<PropertyContainer>, Void> worker = new SwingWorker<List<PropertyContainer>, Void>() {

            @Override
            protected List<PropertyContainer> doInBackground() throws Exception {
                final FeatureFactory factory = featureService.getFeatureFactory();

                setItemCount(featureService.getFeatureCount(bb));
                final List<PropertyContainer> featureList = factory.createFeatures(featureService.getQuery(), bb, null, (page - 1) * pageSize, pageSize, null);

                return featureList;
            }

            @Override
            protected void done() {
                try {
                    List<PropertyContainer> featureList = get();
                    
                    if (model == null) {
                        final Map<String, FeatureServiceAttribute> featureServiceAttributes =
                            featureService.getFeatureServiceAttributes();                        
                        model = new CustomTableModel(featureServiceAttributes, (List<PropertyContainer>)featureList);
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
        jPanel1 = new javax.swing.JPanel();
        btnFirstPage = new javax.swing.JButton();
        btnPrevPage = new javax.swing.JButton();
        txtCurrentPage = new javax.swing.JTextField();
        lblTotalPages = new javax.swing.JLabel();
        btnNextPage = new javax.swing.JButton();
        btnLastPage = new javax.swing.JButton();

        miSortieren.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.miSortieren.text")); // NOI18N
        jPopupMenu1.add(miSortieren);

        miStatistik.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.miStatistik.text")); // NOI18N
        jPopupMenu1.add(miStatistik);

        miSpalteAusblenden.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.miSpalteAusblenden.text")); // NOI18N
        miSpalteAusblenden.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSpalteAusblendenActionPerformed(evt);
            }
        });
        jPopupMenu1.add(miSpalteAusblenden);

        miSpaltenUmbenennen.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.miSpaltenUmbenennen.text")); // NOI18N
        miSpaltenUmbenennen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSpaltenUmbenennenActionPerformed(evt);
            }
        });
        jPopupMenu1.add(miSpaltenUmbenennen);

        miFeldberechnung.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.miFeldberechnung.text")); // NOI18N
        jPopupMenu1.add(miFeldberechnung);

        setLayout(new java.awt.GridBagLayout());

        jToolBar1.setRollover(true);

        butPrintPreview.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butPrintPreview.text")); // NOI18N
        butPrintPreview.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butPrintPreview.toolTipText")); // NOI18N
        butPrintPreview.setFocusable(false);
        butPrintPreview.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butPrintPreview.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butPrintPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPrintPreviewActionPerformed(evt);
            }
        });
        jToolBar1.add(butPrintPreview);

        butPrint.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butPrint.text")); // NOI18N
        butPrint.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butPrint.toolTipText")); // NOI18N
        butPrint.setFocusable(false);
        butPrint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butPrint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(butPrint);

        butExport.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butExport.text")); // NOI18N
        butExport.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butExport.toolTipText")); // NOI18N
        butExport.setFocusable(false);
        butExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(butExport);

        butAttrib.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butAttrib.text")); // NOI18N
        butAttrib.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butAttrib.toolTipText")); // NOI18N
        butAttrib.setFocusable(false);
        butAttrib.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butAttrib.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(butAttrib);

        butSearch.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butSearch.text")); // NOI18N
        butSearch.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butSearch.toolTipText")); // NOI18N
        butSearch.setFocusable(false);
        butSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butSearch.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(butSearch);

        tbLookup.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.tbLookup.text")); // NOI18N
        tbLookup.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.tbLookup.toolTipText")); // NOI18N
        tbLookup.setFocusable(false);
        tbLookup.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbLookup.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(tbLookup);

        tbAlias.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.tbAlias.text")); // NOI18N
        tbAlias.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.tbAlias.toolTipText")); // NOI18N
        tbAlias.setFocusable(false);
        tbAlias.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbAlias.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(tbAlias);

        tbProcessing.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.tbProcessing.text")); // NOI18N
        tbProcessing.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.tbProcessing.toolTipText")); // NOI18N
        tbProcessing.setFocusable(false);
        tbProcessing.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbProcessing.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(tbProcessing);

        butMoveSelectedRows.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butMoveSelectedRows.text")); // NOI18N
        butMoveSelectedRows.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butMoveSelectedRows.toolTipText")); // NOI18N
        butMoveSelectedRows.setFocusable(false);
        butMoveSelectedRows.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butMoveSelectedRows.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(butMoveSelectedRows);

        butSelectAll.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butSelectAll.text")); // NOI18N
        butSelectAll.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butSelectAll.toolTipText")); // NOI18N
        butSelectAll.setFocusable(false);
        butSelectAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butSelectAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSelectAllActionPerformed(evt);
            }
        });
        jToolBar1.add(butSelectAll);

        butInvertSelection.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butInvertSelection.text")); // NOI18N
        butInvertSelection.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butInvertSelection.toolTipText")); // NOI18N
        butInvertSelection.setFocusable(false);
        butInvertSelection.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butInvertSelection.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(butInvertSelection);

        butClearSelection.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butClearSelection.text")); // NOI18N
        butClearSelection.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butClearSelection.toolTipText")); // NOI18N
        butClearSelection.setFocusable(false);
        butClearSelection.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butClearSelection.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butClearSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butClearSelectionActionPerformed(evt);
            }
        });
        jToolBar1.add(butClearSelection);

        butZoomToSelection.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butZoomToSelection.text")); // NOI18N
        butZoomToSelection.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butZoomToSelection.toolTipText")); // NOI18N
        butZoomToSelection.setFocusable(false);
        butZoomToSelection.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butZoomToSelection.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(butZoomToSelection);

        butColWidth.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butColWidth.text")); // NOI18N
        butColWidth.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butColWidth.toolTipText")); // NOI18N
        butColWidth.setFocusable(false);
        butColWidth.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butColWidth.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butColWidth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butColWidthActionPerformed(evt);
            }
        });
        jToolBar1.add(butColWidth);

        butShowCols.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butShowCols.text")); // NOI18N
        butShowCols.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.butShowCols.toolTipText")); // NOI18N
        butShowCols.setFocusable(false);
        butShowCols.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butShowCols.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butShowCols.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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

        panWaiting.setBackground(new Color(255,255,255, 150));
        panWaiting.setLayout(new java.awt.GridBagLayout());

        labWaitingImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/ajax-loader.gif"))); // NOI18N
        labWaitingImage.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.labWaitingImage.text")); // NOI18N
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
            new Object [][] {

            },
            new String [] {

            }
        ));
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tableScrollPane.setViewportView(table);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tableScrollPane, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        btnFirstPage.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.btnFirstPage.text")); // NOI18N
        btnFirstPage.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.btnFirstPage.toolTipText")); // NOI18N
        btnFirstPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFirstPageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(btnFirstPage, gridBagConstraints);

        btnPrevPage.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.btnPrevPage.text")); // NOI18N
        btnPrevPage.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.btnPrevPage.toolTipText")); // NOI18N
        btnPrevPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevPageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(btnPrevPage, gridBagConstraints);

        txtCurrentPage.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.txtCurrentPage.text")); // NOI18N
        txtCurrentPage.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.txtCurrentPage.toolTipText")); // NOI18N
        txtCurrentPage.setMinimumSize(new java.awt.Dimension(50, 27));
        txtCurrentPage.setPreferredSize(new java.awt.Dimension(50, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(txtCurrentPage, gridBagConstraints);

        lblTotalPages.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.lblTotalPages.text")); // NOI18N
        lblTotalPages.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.lblTotalPages.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(lblTotalPages, gridBagConstraints);

        btnNextPage.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.btnNextPage.text")); // NOI18N
        btnNextPage.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.btnNextPage.toolTipText")); // NOI18N
        btnNextPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextPageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(btnNextPage, gridBagConstraints);

        btnLastPage.setText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.btnLastPage.text")); // NOI18N
        btnLastPage.setToolTipText(org.openide.util.NbBundle.getMessage(AttributeTable.class, "AttributeTable.btnLastPage.toolTipText")); // NOI18N
        btnLastPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLastPageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(btnLastPage, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butPrintPreviewActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPrintPreviewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_butPrintPreviewActionPerformed

    private void btnPrevPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevPageActionPerformed
        if (currentPage > 1) {
            loadModel(--currentPage);
        }
    }//GEN-LAST:event_btnPrevPageActionPerformed

    private void btnFirstPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFirstPageActionPerformed
        currentPage = 1;
        loadModel(currentPage);
    }//GEN-LAST:event_btnFirstPageActionPerformed

    private void btnNextPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextPageActionPerformed
        if (currentPage * pageSize < itemCount) {
            loadModel(++currentPage);
        }
    }//GEN-LAST:event_btnNextPageActionPerformed

    private void btnLastPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLastPageActionPerformed
        currentPage = itemCount / pageSize;
        
        if (currentPage * pageSize < itemCount) {
            ++currentPage;
        }
        loadModel(currentPage);
    }//GEN-LAST:event_btnLastPageActionPerformed

    private void miSpalteAusblendenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSpalteAusblendenActionPerformed
        model.hideColumn(popupColumn);
    }//GEN-LAST:event_miSpalteAusblendenActionPerformed

    private void miSpaltenUmbenennenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSpaltenUmbenennenActionPerformed
        String newName = (String)JOptionPane.showInputDialog(this, "Geben Sie den neuen Namen der Spalte ein.", "Spalte umbenennen", JOptionPane.QUESTION_MESSAGE, null, null, model.getColumnName(popupColumn));
        if (newName != null) {
            model.setColumnName(popupColumn, newName);
        }
    }//GEN-LAST:event_miSpaltenUmbenennenActionPerformed

    private void butShowColsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butShowColsActionPerformed
        model.showColumns();
    }//GEN-LAST:event_butShowColsActionPerformed

    private void butColWidthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butColWidthActionPerformed
        setTableSize();
    }//GEN-LAST:event_butColWidthActionPerformed

    private void butSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSelectAllActionPerformed
        table.getSelectionModel().setSelectionInterval(0, model.getRowCount() - 1);
    }//GEN-LAST:event_butSelectAllActionPerformed

    private void butClearSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butClearSelectionActionPerformed
        table.getSelectionModel().clearSelection();
    }//GEN-LAST:event_butClearSelectionActionPerformed

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
                    int tmpSize = (int)fmetrics.getStringBounds(String.valueOf( model.getValueAt(row, i) ), table.getGraphics()).getWidth();
                    
                    if (tmpSize > size && tmpSize < MAX_COLUMN_SIZE) {
                        size = tmpSize;
                    }
                }
            }
            
            if (i == (columnCount - 1)) {
                if (totalSize + size + 30 < tableScrollPane.getSize().getWidth()) {
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
        
        if (pageCount * pageSize < itemCount) {
            ++pageCount;
        }        
        lblTotalPages.setText(" / " + pageCount);
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
        private List<PropertyContainer> featureList;
        private List<TableModelListener> listener = new ArrayList<TableModelListener>();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CustomTableModel object.
         *
         * @param  featureServiceAttributes  DOCUMENT ME!
         * @param  propertyContainer         DOCUMENT ME!
         */
        public CustomTableModel(final Map<String, FeatureServiceAttribute> featureServiceAttributes,
                final List<PropertyContainer> propertyContainer) {
            this.featureServiceAttributes = featureServiceAttributes;
            this.featureList = propertyContainer;

            fillHeaderArrays();
            fireContentsChanged();
        }

        //~ Methods ------------------------------------------------------------
        
        private void fillHeaderArrays() {
            int index = 0;
            attributeNames = new String[featureServiceAttributes.size()];
            attributeAlias = new String[featureServiceAttributes.size()];

            for (final String attributeName : featureServiceAttributes.keySet()) {
                attributeNames[index] = attributeName;
                attributeAlias[index++] = attributeName;
            }
        }

        public void setNewFeatureList(final List<PropertyContainer> propertyContainer) {
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
        
        public void hideColumn(int col) {
            this.attributeNames = remove(this.attributeNames, col);
            this.attributeAlias = remove(this.attributeAlias, col);
            fireContentsChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
        }
        
        public void showColumns() {
            fillHeaderArrays();
            fireContentsChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
        }
        
        public void setColumnName(int row, String name) {
            if (row > 0 && row < attributeAlias.length) {
                attributeAlias[row] = name;
                fireContentsChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
            }
        }
        
        private String[] remove(String[] array, int index) {
            if (index >= 0 && index < array.length) {
                String[] resultArray = new String[array.length - 1];
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
         */
        private void fireContentsChanged(final TableModelEvent e) {
            for (final TableModelListener tmp : listener) {
                tmp.tableChanged(e);
            }
            
            AttributeTable.this.setTableSize();
        }
    }
}
