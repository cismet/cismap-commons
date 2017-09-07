/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.rasterservice.georeferencing;

import com.vividsolutions.jts.geom.Coordinate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;

import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.gui.SimpleBackgroundedJPanel;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class RasterGeoReferencingPanel extends javax.swing.JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(RasterGeoReferencingPanel.class);

    private static final String[] COLUMN_NAMES = {
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.colName.position"),
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.colName.point"),
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.colName.coord"),
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.colName.error"),
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.colName.enabled")
        };

    private static final Class[] COLUMN_CLASSES = {
            Integer.class,
            String.class,
            String.class,
            String.class,
            Boolean.class
        };

    //~ Instance fields --------------------------------------------------------

    @Getter(AccessLevel.PRIVATE)
    private final CellSelectionListener selectionListener = new CellSelectionListener();

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private boolean wizardRefreshing = false;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JDialog jDialog2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private org.jdesktop.swingx.JXTable jXTable1;
    private javax.swing.JPanel panContent;
    private javax.swing.JPanel panInstructions;
    private javax.swing.JPanel panMapOverview;
    private javax.swing.JPanel panTable;
    private de.cismet.cismap.commons.gui.SimpleBackgroundedJPanel simpleBackgroundedJPanel1;
    private de.cismet.cismap.commons.gui.SimpleBackgroundedJPanel simpleBackgroundedJPanel2;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form RasterGeoReferencingPanel.
     */
    public RasterGeoReferencingPanel() {
        initComponents();

        if (getWizard() != null) {
            getWizard().addListener(new WizardListener());
        }

        simpleBackgroundedJPanel1.setPCanvas(getWizard().getPointZoomViewCanvas());
        getWizard().addPropertyChangeListener(simpleBackgroundedJPanel1);

        simpleBackgroundedJPanel2.setPCanvas(getWizard().getCoordinateZoomViewCanvas());
        getWizard().addPropertyChangeListener(simpleBackgroundedJPanel2);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final RasterGeoReferencingWizard getWizard() {
        return RasterGeoReferencingWizard.getInstance();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public RasterGeoReferencingHandler getHandler() {
        return getWizard().getHandler();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        jDialog1 = new javax.swing.JDialog();
        jDialog2 = new javax.swing.JDialog();
        jLabel12 = new javax.swing.JLabel();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        panContent = new javax.swing.JPanel();
        panInstructions = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        panTable = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jXTable1 = new org.jdesktop.swingx.JXTable();
        jPanel4 = new javax.swing.JPanel();
        jButton8 = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                new java.awt.Dimension(0, 0),
                new java.awt.Dimension(32767, 0));
        jButton1 = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                new java.awt.Dimension(0, 0),
                new java.awt.Dimension(32767, 0));
        panMapOverview = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        simpleBackgroundedJPanel1 = new de.cismet.cismap.commons.gui.SimpleBackgroundedJPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        simpleBackgroundedJPanel2 = new DnDTargetSimpleBackgroundedJPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                new java.awt.Dimension(0, 0),
                new java.awt.Dimension(32767, 0));

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel12,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jLabel12.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(
            jMenuItem2,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jMenuItem2.text")); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jMenuItem2ActionPerformed(evt);
                }
            });
        jPopupMenu1.add(jMenuItem2);

        setMinimumSize(new java.awt.Dimension(420, 500));
        setPreferredSize(new java.awt.Dimension(420, 520));
        setLayout(new java.awt.GridBagLayout());

        panContent.setLayout(new java.awt.GridBagLayout());

        panInstructions.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel7,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jLabel7.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        panInstructions.add(jLabel7, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel5,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jLabel5.text")); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${wizard.pointSelected}"),
                jLabel5,
                org.jdesktop.beansbinding.BeanProperty.create("enabled"),
                "wizPointEnable");
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        panInstructions.add(jLabel5, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel6,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jLabel6.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${wizard.coordinateSelected}"),
                jLabel6,
                org.jdesktop.beansbinding.BeanProperty.create("enabled"),
                "wizCoordinateEnable");
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        panInstructions.add(jLabel6, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridLayout(1, 2, 5, 0));

        jButton6.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/control-180.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jButton6,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jButton6.text"));                                   // NOI18N
        jButton6.setBorderPainted(false);
        jButton6.setContentAreaFilled(false);
        jButton6.setFocusPainted(false);
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${backwardPossible}"),
                jButton6,
                org.jdesktop.beansbinding.BeanProperty.create("enabled"),
                "wizBackwardEnable");
        bindingGroup.addBinding(binding);

        jButton6.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jButton6ActionPerformed(evt);
                }
            });
        jPanel1.add(jButton6);

        jButton7.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/control.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jButton7,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jButton7.text"));                               // NOI18N
        jButton7.setBorderPainted(false);
        jButton7.setContentAreaFilled(false);
        jButton7.setFocusPainted(false);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${forwardPossible}"),
                jButton7,
                org.jdesktop.beansbinding.BeanProperty.create("enabled"),
                "wizForwardEnable");
        bindingGroup.addBinding(binding);

        jButton7.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jButton7ActionPerformed(evt);
                }
            });
        jPanel1.add(jButton7);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        panInstructions.add(jPanel1, gridBagConstraints);

        jLabel8.setIcon(new javax.swing.ImageIcon(
                getClass().getResource(
                    "/de/cismet/cismap/commons/rasterservice/georeferencing/georef_wizard_icon.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel8,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jLabel8.text"));                                             // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 20);
        panInstructions.add(jLabel8, gridBagConstraints);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                jXTable1,
                org.jdesktop.beansbinding.ELProperty.create("<html><b>${selectedRow + 1}"),
                jLabel9,
                org.jdesktop.beansbinding.BeanProperty.create("text"),
                "wizPosition");
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        panInstructions.add(jLabel9, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panContent.add(panInstructions, gridBagConstraints);

        panTable.setMinimumSize(new java.awt.Dimension(400, 60));
        panTable.setPreferredSize(new java.awt.Dimension(400, 382));
        panTable.setLayout(new java.awt.GridBagLayout());

        jXTable1.setModel(new PairTableModel());
        jXTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
        jXTable1.setCellSelectionEnabled(true);
        jXTable1.setSortable(false);
        jXTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jXTable1);
        jXTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        jXTable1.getColumnModel().getSelectionModel().addListSelectionListener(getSelectionListener());
        jXTable1.getSelectionModel().addListSelectionListener(getSelectionListener());

        final DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();

        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        jXTable1.getColumnModel().getColumn(0).setMinWidth(15);
        jXTable1.getColumnModel().getColumn(0).setMaxWidth(25);
        jXTable1.getColumnModel().getColumn(0).setPreferredWidth(20);

        jXTable1.getColumnModel().getColumn(1).setMinWidth(60);
        jXTable1.getColumnModel().getColumn(1).setMaxWidth(120);
        jXTable1.getColumnModel().getColumn(1).setPreferredWidth(80);

        jXTable1.getColumnModel().getColumn(2).setMinWidth(150);
        jXTable1.getColumnModel().getColumn(2).setPreferredWidth(180);

        jXTable1.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        jXTable1.getColumnModel().getColumn(3).setMinWidth(40);
        jXTable1.getColumnModel().getColumn(3).setMaxWidth(60);
        jXTable1.getColumnModel().getColumn(3).setPreferredWidth(50);

        jXTable1.getColumnModel().getColumn(4).setMinWidth(25);
        jXTable1.getColumnModel().getColumn(4).setMaxWidth(25);
        jXTable1.getColumnModel().getColumn(4).setPreferredWidth(25);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        panTable.add(jScrollPane1, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jButton8.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/plus.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jButton8,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jButton8.text"));                            // NOI18N
        jButton8.setToolTipText(org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jButton8.toolTipText"));                     // NOI18N
        jButton8.setBorderPainted(false);
        jButton8.setContentAreaFilled(false);
        jButton8.setFocusPainted(false);
        jButton8.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jButton8ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        jPanel4.add(jButton8, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        jPanel4.add(filler2, gridBagConstraints);

        jButton1.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/minus.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jButton1,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jButton1.text"));                             // NOI18N
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setFocusPainted(false);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                jXTable1,
                org.jdesktop.beansbinding.ELProperty.create("${selectedElement != null}"),
                jButton1,
                org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jButton1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel4.add(jButton1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel4.add(filler3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        panTable.add(jPanel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panContent.add(panTable, gridBagConstraints);

        panMapOverview.setLayout(new java.awt.GridBagLayout());

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel3,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 2);
        panMapOverview.add(jLabel3, gridBagConstraints);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel4,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jLabel4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 5, 0);
        panMapOverview.add(jLabel4, gridBagConstraints);

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane2.setMaximumSize(new java.awt.Dimension(200, 200));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(200, 200));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(200, 200));

        simpleBackgroundedJPanel1.setBackground(new java.awt.Color(255, 255, 255));
        simpleBackgroundedJPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(
                javax.swing.border.BevelBorder.LOWERED));
        simpleBackgroundedJPanel1.setMaximumSize(new java.awt.Dimension(200, 200));
        simpleBackgroundedJPanel1.setMinimumSize(new java.awt.Dimension(200, 200));
        simpleBackgroundedJPanel1.setPreferredSize(new java.awt.Dimension(200, 200));
        simpleBackgroundedJPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/rasterservice/georeferencing/georef_dot.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel1,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jLabel1.text"));                                                        // NOI18N
        simpleBackgroundedJPanel1.add(jLabel1, new java.awt.GridBagConstraints());

        jScrollPane2.setViewportView(simpleBackgroundedJPanel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        panMapOverview.add(jScrollPane2, gridBagConstraints);

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane3.setMaximumSize(new java.awt.Dimension(200, 200));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(200, 200));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(200, 200));

        simpleBackgroundedJPanel2.setBackground(new java.awt.Color(255, 255, 255));
        simpleBackgroundedJPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(
                javax.swing.border.BevelBorder.LOWERED));
        simpleBackgroundedJPanel2.setMaximumSize(new java.awt.Dimension(200, 200));
        simpleBackgroundedJPanel2.setMinimumSize(new java.awt.Dimension(200, 200));
        simpleBackgroundedJPanel2.setPreferredSize(new java.awt.Dimension(200, 200));
        simpleBackgroundedJPanel2.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mousePressed(final java.awt.event.MouseEvent evt) {
                    simpleBackgroundedJPanel2MousePressed(evt);
                }
            });
        simpleBackgroundedJPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel11.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/featureservice/res/pointsymbols/info.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel11,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jLabel11.text"));                                                    // NOI18N
        jLabel11.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mousePressed(final java.awt.event.MouseEvent evt) {
                    jLabel11MousePressed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 0);
        simpleBackgroundedJPanel2.add(jLabel11, gridBagConstraints);
        jLabel11.setVisible(false);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/rasterservice/georeferencing/georef_cross.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel10,
            org.openide.util.NbBundle.getMessage(
                RasterGeoReferencingPanel.class,
                "RasterGeoReferencingPanel.jLabel10.text"));                                                         // NOI18N
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mousePressed(final java.awt.event.MouseEvent evt) {
                    jLabel10MousePressed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        simpleBackgroundedJPanel2.add(jLabel10, gridBagConstraints);

        jScrollPane3.setViewportView(simpleBackgroundedJPanel2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        panMapOverview.add(jScrollPane3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        panMapOverview.add(filler1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panContent.add(panMapOverview, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(panContent, gridBagConstraints);

        bindingGroup.bind();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jButton1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton1ActionPerformed
        final int position = jXTable1.getSelectedRow();
        new SwingWorker<Boolean, Object>() {

                @Override
                protected Boolean doInBackground() throws Exception {
                    final boolean success = getHandler().removePair(position);
                    return success;
                }

                @Override
                protected void done() {
                    try {
                        final Boolean success = get();
                        if (Boolean.TRUE.equals(success)) {
                            refreshModel();
                            if (success && (getHandler().getNumOfPairs() != 0)) {
                                getWizard().selectCoordinate(position - 1);
                            }
                        }
                    } catch (final Exception ex) {
                        LOG.info(ex, ex);
                    }
                }
            }.execute();
        ;
    } //GEN-LAST:event_jButton1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jButton8ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton8ActionPerformed
        new SwingWorker<Integer, Object>() {

                @Override
                protected Integer doInBackground() throws Exception {
                    final int position = getHandler().addPair();
                    return position;
                }

                @Override
                protected void done() {
                    try {
                        final int position = (Integer)get();
                        refreshModel();
                        getWizard().selectPoint(position);
                    } catch (final Exception ex) {
                        LOG.info(ex, ex);
                    }
                }
            }.execute();
    } //GEN-LAST:event_jButton8ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jButton7ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton7ActionPerformed
        new SwingWorker<Void, Object>() {

                @Override
                protected Void doInBackground() throws Exception {
                    getWizard().forward();
                    return null;
                }
            }.execute();
    } //GEN-LAST:event_jButton7ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jButton6ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton6ActionPerformed
        new SwingWorker<Void, Object>() {

                @Override
                protected Void doInBackground() throws Exception {
                    getWizard().backward();
                    return null;
                }
            }.execute();
    } //GEN-LAST:event_jButton6ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jMenuItem2ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jMenuItem2ActionPerformed
        resetLayer();
    }                                                                              //GEN-LAST:event_jMenuItem2ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jLabel11MousePressed(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_jLabel11MousePressed
        showResetLayerPopup(evt);
    }                                                                        //GEN-LAST:event_jLabel11MousePressed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jLabel10MousePressed(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_jLabel10MousePressed
        showResetLayerPopup(evt);
    }                                                                        //GEN-LAST:event_jLabel10MousePressed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void simpleBackgroundedJPanel2MousePressed(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_simpleBackgroundedJPanel2MousePressed
        showResetLayerPopup(evt);
    }                                                                                         //GEN-LAST:event_simpleBackgroundedJPanel2MousePressed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void showResetLayerPopup(final java.awt.event.MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            jPopupMenu1.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void resetLayer() {
        getWizard().setSingleLayer(null);
        jLabel10.setToolTipText(null);
        jLabel11.setVisible(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isBackwardPossible() {
        return (getHandler() != null) && (getHandler().getNumOfPairs() > 0)
                    && ((jXTable1.getSelectedRow() > 0) || getWizard().isCoordinateSelected());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isForwardPossible() {
        return (getHandler() != null) && (getHandler().getNumOfPairs() > 0)
                    && ((jXTable1.getSelectedRow() < (getHandler().getNumOfPairs() - 1))
                        || getWizard().isPointSelected());
    }

    /**
     * DOCUMENT ME!
     */
    private void refreshWizardBinding() {
        if (!isWizardRefreshing()) {
            setWizardRefreshing(true);
            SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            bindingGroup.getBinding("wizPosition").unbind();
                            bindingGroup.getBinding("wizBackwardEnable").unbind();
                            bindingGroup.getBinding("wizForwardEnable").unbind();
                            bindingGroup.getBinding("wizPointEnable").unbind();
                            bindingGroup.getBinding("wizCoordinateEnable").unbind();

                            bindingGroup.getBinding("wizPosition").bind();
                            bindingGroup.getBinding("wizBackwardEnable").bind();
                            bindingGroup.getBinding("wizForwardEnable").bind();
                            bindingGroup.getBinding("wizPointEnable").bind();
                            bindingGroup.getBinding("wizCoordinateEnable").bind();
                        } finally {
                            setWizardRefreshing(false);
                        }
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void refreshModel() {
        ((PairTableModel)jXTable1.getModel()).fireTableDataChanged();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class PairTableModel extends AbstractTableModel {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int getRowCount() {
            return ((getHandler() == null) || (getHandler() == null)) ? 0 : getHandler().getNumOfPairs();
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int getColumnCount() {
            return COLUMN_CLASSES.length;
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
            final PointCoordinatePair pair = getHandler().getPair(rowIndex);

            final Point point = pair.getPoint();
            final Coordinate coordinate = pair.getCoordinate();

            final DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
            otherSymbols.setDecimalSeparator(',');
            final DecimalFormat format = new DecimalFormat("#0.00", otherSymbols);

            switch (columnIndex) {
                case 0: {
                    return rowIndex + 1;
                }
                case 1: {
                    return (point != null) ? ("[" + (int)point.getX() + ";" + (int)point.getY() + "]") : null;
                }
                case 2: {
                    return (coordinate != null)
                        ? ("[" + format.format(coordinate.x) + ";" + format.format(coordinate.y) + "]") : null;
                }
                case 3: {
                    if (getHandler().isComplete()) {
                        return format.format(getHandler().getError(rowIndex));
                    } else {
                        return "-";
                    }
                }
                case 4: {
                    return getHandler().isPositionEnabled(rowIndex);
                }
                default: {
                    return null;
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  position  DOCUMENT ME!
         * @param  point     DOCUMENT ME!
         */
        public void setPoint(final int position, final Point point) {
            new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() throws Exception {
                        getHandler().setPoint(position, point);
                        return null;
                    }
                }.execute();
        }

        /**
         * DOCUMENT ME!
         *
         * @param  position    DOCUMENT ME!
         * @param  coordinate  DOCUMENT ME!
         */
        public void setCoordinate(final int position, final Coordinate coordinate) {
            new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() throws Exception {
                        getHandler().setCoordinate(position, coordinate);
                        return null;
                    }
                }.execute();
        }

        /**
         * DOCUMENT ME!
         *
         * @param  position  DOCUMENT ME!
         * @param  enabled   DOCUMENT ME!
         */
        public void setPostitionEnabled(final int position, final boolean enabled) {
            new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() throws Exception {
                        getHandler().setPositionEnabled(position, enabled);
                        return null;
                    }
                }.execute();
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
            if ((columnIndex == 1) || (columnIndex == 2)) {
                final String value = (aValue != null) ? ((String)aValue).trim() : null;
                if (value != null) {
                    final String[] split = value.replaceAll("\\(|\\)|\\[|\\]| ", "")
                                .replaceAll("\\.", "")
                                .replaceAll("\\||/", ";")
                                .replaceAll(",", ".")
                                .split(";");
                    if (split.length == 2) {
                        try {
                            if (columnIndex == 1) {
                                final int x = Integer.parseInt(split[0].trim());
                                final int y = Integer.parseInt(split[1].trim());
                                setPoint(rowIndex, new Point(x, y));
                            } else {
                                final double x = Double.parseDouble(split[0].trim());
                                final double y = Double.parseDouble(split[1].trim());
                                setCoordinate(rowIndex, new Coordinate(x, y));
                            }
                        } catch (final Exception ex) {
                            // ignoring parsing exceptions
                        }

                        return;
                    }
                }

                if (columnIndex == 1) {
                    setPoint(rowIndex, null);
                } else {
                    setCoordinate(rowIndex, null);
                }
            } else {
                if (columnIndex == 4) {
                    final Boolean value = (Boolean)aValue;
                    setPostitionEnabled(rowIndex, Boolean.TRUE.equals(value));
                }
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
        public String getColumnName(final int columnIndex) {
            return COLUMN_NAMES[columnIndex];
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
            return COLUMN_CLASSES[columnIndex];
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return (columnIndex == 1) || (columnIndex == 2) || (columnIndex == 4);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class WizardListener implements RasterGeoReferencingWizardListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void positionAdded(final int position) {
            refreshModel();
            refreshWizardBinding();

            if (getHandler().getNumOfPairs() == 1) {
                getWizard().selectPoint(0);
            }
        }

        @Override
        public void positionRemoved(final int position) {
            refreshModel();
            refreshWizardBinding();
        }

        @Override
        public void positionChanged(final int position) {
            if (getWizard().isPointSelected()) {
                refreshModel();
                pointSelected(position);
            } else if (getWizard().isCoordinateSelected()) {
                refreshModel();
                coordinateSelected(position);
            }
            refreshWizardBinding();
        }

        @Override
        public void transformationChanged() {
        }

        @Override
        public void pointSelected(final int position) {
            jXTable1.changeSelection(position, 1, false, false);
            refreshWizardBinding();
        }

        @Override
        public void coordinateSelected(final int position) {
            jXTable1.changeSelection(position, 2, false, false);
            refreshWizardBinding();
        }

        @Override
        public void handlerChanged(final RasterGeoReferencingHandler handler) {
            refreshModel();
            refreshWizardBinding();
            if ((getHandler() != null) && (getHandler().getNumOfPairs() > 0)) {
                getWizard().selectPoint(0);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CellSelectionListener implements ListSelectionListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                refreshWizardBinding();

                final int position = jXTable1.getSelectedRow();
                if (position < 0) {
                    getWizard().clearSelection();
                } else {
                    if (jXTable1.getSelectedColumn() == 1) {
                        getWizard().selectPoint(position);
                    } else if (jXTable1.getSelectedColumn() == 2) {
                        getWizard().selectCoordinate(position);
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
    class DnDTargetSimpleBackgroundedJPanel extends SimpleBackgroundedJPanel implements DropTargetListener {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DnDTargetSimpleBackgroundedJPanel object.
         */
        public DnDTargetSimpleBackgroundedJPanel() {
            final DropTarget dt = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void dragEnter(final DropTargetDragEvent dtde) {
        }

        @Override
        public void dragOver(final DropTargetDragEvent dtde) {
        }

        @Override
        public void dropActionChanged(final DropTargetDragEvent dtde) {
        }

        @Override
        public void dragExit(final DropTargetEvent dte) {
        }

        @Override
        public void drop(final DropTargetDropEvent dtde) {
            try {
                getWizard().drop(dtde);
                final RetrievalServiceLayer layer = getWizard().getSingleLayer();
                final String tooltip = (layer != null) ? ("<html>" + jLabel12.getText() + ": " + layer.getName())
                                                       : null;
                jLabel10.setToolTipText(tooltip);
                jLabel11.setToolTipText(tooltip);
                jLabel11.setVisible(layer != null);
            } catch (final Exception ex) {
                LOG.error("Error in drop", ex); // NOI18N
            }
        }
    }
}
