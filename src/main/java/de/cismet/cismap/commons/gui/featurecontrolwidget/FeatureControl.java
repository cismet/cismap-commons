/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * FeatureControl.java
 *
 * Created on 3. Mai 2006, 11:31
 */
package de.cismet.cismap.commons.gui.featurecontrolwidget;

import com.vividsolutions.jts.geom.Geometry;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;

import org.apache.log4j.Logger;

import org.jdesktop.swingx.table.TableColumnExt;

import org.jdom.Element;

import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.AbstractNewFeature;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureGroup;
import de.cismet.cismap.commons.features.FeatureGroups;
import de.cismet.cismap.commons.features.FeatureRenderer;
import de.cismet.cismap.commons.features.FeatureRendererAwareFeature;
import de.cismet.cismap.commons.features.SubFeature;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.MapBoundsListener;

import de.cismet.tools.CurrentStackTrace;
import de.cismet.tools.StaticDecimalTools;

import de.cismet.tools.collections.TypeSafeCollections;

import de.cismet.tools.configuration.Configurable;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class FeatureControl extends javax.swing.JPanel implements FeatureCollectionListener,
    MapBoundsListener,
    Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(FeatureControl.class);

    //~ Instance fields --------------------------------------------------------

    boolean wizardMode = false;
    private FeatureCollectionFilter featureCollectionFilter;
    private ImageIcon icoGreenled = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/res/greenled.png")); // NOI18N
    private MappingComponent mappingComponent = null;
    private ListSelectionListener theListSelectionListener = new ListSelectionListener() {

            @Override
            public void valueChanged(final ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("invocation of the method SelectionListener.valueChanged from jxtFeatures"); // NOI18N
                    }
                    updateSelection();
                }
            }
        };

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdHold;
    private javax.swing.JButton cmdRemoveAll;
    private javax.swing.JButton cmdRemoveFeatures;
    private javax.swing.JButton cmdZoomToAllFeatures;
    private javax.swing.JButton cmdZoomToFeatures;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar jToolBar1;
    private org.jdesktop.swingx.JXTable jxtFeatures;
    private javax.swing.JToggleButton togDisplayObjectInfo;
    private javax.swing.JToggleButton togFixMapExtent;
    private javax.swing.JToggleButton togFixMapScale;
    private javax.swing.JToggleButton togHoldAll;
    private javax.swing.JToggleButton togSelectionWizard;
    private javax.swing.JToggleButton togShowOnlyVisible;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form FeatureControl.
     *
     * @param  mappingComponent  DOCUMENT ME!
     */
    public FeatureControl(final de.cismet.cismap.commons.gui.MappingComponent mappingComponent) {
        initComponents();
        this.mappingComponent = mappingComponent;
        final FeatureCollectionTableModel model = new FeatureCollectionTableModel();
        jxtFeatures.setModel(model);

        jxtFeatures.getColumnModel().getColumn(0).setCellRenderer(jxtFeatures.getDefaultRenderer(Icon.class));
        jxtFeatures.getColumnModel().getColumn(4).setCellRenderer(jxtFeatures.getDefaultRenderer(Number.class));
        jxtFeatures.getColumnModel().getColumn(5).setCellRenderer(jxtFeatures.getDefaultRenderer(Number.class));
        jxtFeatures.getColumnModel().getColumn(7).setCellRenderer(jxtFeatures.getDefaultRenderer(Icon.class));

        featureCollectionFilter = new FeatureCollectionFilter(false, model);
        final ArrayList<RowFilter<AbstractTableModel, Integer>> usedFilters =
            new ArrayList<RowFilter<AbstractTableModel, Integer>>();
        if (FeatureGroups.SHOW_GROUPING_ENABLED) {
            usedFilters.add(featureCollectionFilter);
        } else {
            final SubFeatureFilter subFeatureFilter = new SubFeatureFilter();
            usedFilters.add(featureCollectionFilter);
            usedFilters.add(subFeatureFilter);
        }

        final TableRowSorter<FeatureCollectionTableModel> sorter = new TableRowSorter<FeatureCollectionTableModel>(
                model);
        sorter.setRowFilter(RowFilter.andFilter(usedFilters));
        jxtFeatures.setRowSorter(sorter);

        jxtFeatures.getSelectionModel().addListSelectionListener(theListSelectionListener);
        jxtFeatures.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (e.getClickCount() > 1) {
                        updateSelection();
                    }
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void updateSelection() {
        final int[] rows = jxtFeatures.getSelectedRows();

        final List<Feature> tableSelection = TypeSafeCollections.newArrayList();
        for (int i = 0; i < rows.length; i++) {
            final int mappedRow = mapRowToModel(rows[i]);
            tableSelection.add(getFeatureCollection().getFeature(mappedRow));
        }

        // Hinter die Schleife gestellt, damit nicht f\u00FCr alle selektierten Features ein Event gefeuert wird
        mappingComponent.getFeatureCollection().removeFeatureCollectionListener(FeatureControl.this);
        mappingComponent.getFeatureCollection().select(tableSelection);
        mappingComponent.getFeatureCollection().addFeatureCollectionListener(FeatureControl.this);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        togSelectionWizard = new javax.swing.JToggleButton();
        jToolBar1 = new javax.swing.JToolBar();
        cmdZoomToAllFeatures = new javax.swing.JButton();
        cmdZoomToFeatures = new javax.swing.JButton();
        cmdRemoveFeatures = new javax.swing.JButton();
        cmdRemoveAll = new javax.swing.JButton();
        togShowOnlyVisible = new javax.swing.JToggleButton();
        togFixMapExtent = new javax.swing.JToggleButton();
        togFixMapScale = new javax.swing.JToggleButton();
        togDisplayObjectInfo = new javax.swing.JToggleButton();
        togHoldAll = new javax.swing.JToggleButton();
        cmdHold = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jxtFeatures = new org.jdesktop.swingx.JXTable();

        jButton1.setText(null);

        jButton2.setText(null);

        jButton3.setForeground(javax.swing.UIManager.getDefaults().getColor("Table.selectionBackground"));
        jButton3.setText(null);
        jButton3.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jButton3ActionPerformed(evt);
                }
            });

        togSelectionWizard.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/wizard.png"))); // NOI18N
        togSelectionWizard.setRolloverIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/wizard.png"))); // NOI18N
        togSelectionWizard.setRolloverSelectedIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/wizard.png"))); // NOI18N
        togSelectionWizard.setSelectedIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/wizard.png"))); // NOI18N
        togSelectionWizard.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    togSelectionWizardActionPerformed(evt);
                }
            });

        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEtchedBorder(),
                javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        setLayout(new java.awt.BorderLayout());

        jToolBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        cmdZoomToAllFeatures.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/zoomToAll.png"))); // NOI18N
        cmdZoomToAllFeatures.setToolTipText(org.openide.util.NbBundle.getMessage(
                FeatureControl.class,
                "FeatureControl.cmdZoomToAllFeatures.toolTipText"));                         // NOI18N
        cmdZoomToAllFeatures.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdZoomToAllFeaturesActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdZoomToAllFeatures);

        cmdZoomToFeatures.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/zoomToSelection.png"))); // NOI18N
        cmdZoomToFeatures.setToolTipText(org.openide.util.NbBundle.getMessage(
                FeatureControl.class,
                "FeatureControl.cmdZoomToFeatures.toolTipText"));                                  // NOI18N
        cmdZoomToFeatures.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdZoomToFeaturesActionPerformed(evt);
                }
            });
        cmdZoomToFeatures.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mouseEntered(final java.awt.event.MouseEvent evt) {
                    cmdZoomToFeaturesMouseEntered(evt);
                }
                @Override
                public void mouseExited(final java.awt.event.MouseEvent evt) {
                    cmdZoomToFeaturesMouseExited(evt);
                }
            });
        jToolBar1.add(cmdZoomToFeatures);

        cmdRemoveFeatures.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/removerow.png"))); // NOI18N
        cmdRemoveFeatures.setToolTipText(org.openide.util.NbBundle.getMessage(
                FeatureControl.class,
                "FeatureControl.cmdRemoveFeatures.toolTipText"));                            // NOI18N
        cmdRemoveFeatures.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdRemoveFeaturesActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdRemoveFeatures);

        cmdRemoveAll.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/removeAll.png"))); // NOI18N
        cmdRemoveAll.setToolTipText(org.openide.util.NbBundle.getMessage(
                FeatureControl.class,
                "FeatureControl.cmdRemoveAll.toolTipText"));                                 // NOI18N
        cmdRemoveAll.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdRemoveAllActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdRemoveAll);

        togShowOnlyVisible.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/showOnlyVisible_disabled.png"))); // NOI18N
        togShowOnlyVisible.setToolTipText(org.openide.util.NbBundle.getMessage(
                FeatureControl.class,
                "FeatureControl.togShowOnlyVisible.toolTipText"));                                          // NOI18N
        togShowOnlyVisible.setRolloverIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/showOnlyVisible_disabled.png"))); // NOI18N
        togShowOnlyVisible.setRolloverSelectedIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/showOnlyVisible.png")));          // NOI18N
        togShowOnlyVisible.setSelectedIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/showOnlyVisible.png")));          // NOI18N
        togShowOnlyVisible.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    togShowOnlyVisibleActionPerformed(evt);
                }
            });
        jToolBar1.add(togShowOnlyVisible);

        togFixMapExtent.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapExtent_disabled.png"))); // NOI18N
        togFixMapExtent.setToolTipText(org.openide.util.NbBundle.getMessage(
                FeatureControl.class,
                "FeatureControl.togFixMapExtent.toolTipText"));                                          // NOI18N
        togFixMapExtent.setRolloverIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapExtent_disabled.png"))); // NOI18N
        togFixMapExtent.setRolloverSelectedIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapExtent.png")));          // NOI18N
        togFixMapExtent.setSelectedIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapExtent.png")));          // NOI18N
        togFixMapExtent.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    togFixMapExtentActionPerformed(evt);
                }
            });
        jToolBar1.add(togFixMapExtent);

        togFixMapScale.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapScale_disabled.png"))); // NOI18N
        togFixMapScale.setToolTipText(org.openide.util.NbBundle.getMessage(
                FeatureControl.class,
                "FeatureControl.togFixMapScale.toolTipText"));                                          // NOI18N
        togFixMapScale.setRolloverIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapScale_disabled.png"))); // NOI18N
        togFixMapScale.setRolloverSelectedIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapScale.png")));          // NOI18N
        togFixMapScale.setSelectedIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapScale.png")));          // NOI18N
        togFixMapScale.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    togFixMapScaleActionPerformed(evt);
                }
            });
        jToolBar1.add(togFixMapScale);

        togDisplayObjectInfo.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/infoLabel_disabled.png"))); // NOI18N
        togDisplayObjectInfo.setToolTipText(org.openide.util.NbBundle.getMessage(
                FeatureControl.class,
                "FeatureControl.togDisplayObjectInfo.toolTipText"));                                  // NOI18N
        togDisplayObjectInfo.setRolloverIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/infoLabel_disabled.png"))); // NOI18N
        togDisplayObjectInfo.setRolloverSelectedIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/infoLabel.png")));          // NOI18N
        togDisplayObjectInfo.setSelectedIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/infoLabel.png")));          // NOI18N
        togDisplayObjectInfo.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    togDisplayObjectInfoActionPerformed(evt);
                }
            });
        jToolBar1.add(togDisplayObjectInfo);

        togHoldAll.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/holdAll_disabled.png"))); // NOI18N
        togHoldAll.setToolTipText(org.openide.util.NbBundle.getMessage(
                FeatureControl.class,
                "FeatureControl.togHoldAll.toolTipText"));                                          // NOI18N
        togHoldAll.setRolloverIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/holdAll_disabled.png"))); // NOI18N
        togHoldAll.setRolloverSelectedIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/holdAll.png")));          // NOI18N
        togHoldAll.setSelectedIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/holdAll.png")));          // NOI18N
        togHoldAll.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    togHoldAllActionPerformed(evt);
                }
            });
        jToolBar1.add(togHoldAll);

        cmdHold.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/holdSelection.png"))); // NOI18N
        cmdHold.setToolTipText(org.openide.util.NbBundle.getMessage(
                FeatureControl.class,
                "FeatureControl.cmdHold.toolTipText"));                                          // NOI18N
        cmdHold.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdHoldActionPerformed(evt);
                }
            });
        jToolBar1.add(cmdHold);

        add(jToolBar1, java.awt.BorderLayout.NORTH);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jScrollPane2.setViewportView(jxtFeatures);

        add(jScrollPane2, java.awt.BorderLayout.CENTER);
    } // </editor-fold>//GEN-END:initComponents
    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void togHoldAllActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togHoldAllActionPerformed
        mappingComponent.getFeatureCollection().setHoldAll(togHoldAll.isSelected());
    }                                                                              //GEN-LAST:event_togHoldAllActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jButton3ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jButton3ActionPerformed
        tester();
    }                                                                            //GEN-LAST:event_jButton3ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdRemoveAllActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdRemoveAllActionPerformed
        mappingComponent.getFeatureCollection().removeAllFeatures();
        mappingComponent.getMemUndo().clear();
        mappingComponent.getMemRedo().clear();
    }                                                                                //GEN-LAST:event_cmdRemoveAllActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void togSelectionWizardActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togSelectionWizardActionPerformed
        wizardMode = togSelectionWizard.isSelected();
    }                                                                                      //GEN-LAST:event_togSelectionWizardActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void togFixMapScaleActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togFixMapScaleActionPerformed
        mappingComponent.setFixedMapScale(togFixMapScale.isSelected());
    }                                                                                  //GEN-LAST:event_togFixMapScaleActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void togFixMapExtentActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togFixMapExtentActionPerformed
        mappingComponent.setFixedMapExtent(togFixMapExtent.isSelected());
    }                                                                                   //GEN-LAST:event_togFixMapExtentActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void togDisplayObjectInfoActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togDisplayObjectInfoActionPerformed
        mappingComponent.setInfoNodesVisible(togDisplayObjectInfo.isSelected());
    }                                                                                        //GEN-LAST:event_togDisplayObjectInfoActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdHoldActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdHoldActionPerformed
        final int[] rows = jxtFeatures.getSelectedRows();
        for (int i = 0; i < rows.length; i++) {
            final int mappedRow = mapRowToModel(rows[i]);
            final Feature f = getFeatureCollection().getFeature(mappedRow);
            if (getFeatureCollection().isHoldFeature(f)) {
                getFeatureCollection().unholdFeature(f);
            } else {
                getFeatureCollection().holdFeature(f);
            }
        }
    }                                                                           //GEN-LAST:event_cmdHoldActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private FeatureCollection getFeatureCollection() {
        try {
            return mappingComponent.getFeatureCollection();
        } catch (Exception e) {
            LOG.warn("Problem with method getFeatureCollection().", e); // NOI18N
            return new DefaultFeatureCollection();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   displayedRow  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int mapRowToModel(final int displayedRow) {
        return jxtFeatures.convertRowIndexToModel(displayedRow);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Vector<Feature> getAllFeaturesSorted() {
        final Vector<Feature> v = new Vector<Feature>();
        final FeatureCollection fc = getFeatureCollection();
        if (fc.getFeatureCount() > 0) {
            for (int i = 0; i < jxtFeatures.getRowCount(); ++i) {
                try {
                    v.add((Feature)fc.getAllFeatures().get(mapRowToModel(i)));
                } catch (Throwable t) {
                    LOG.error("Error in getAllFeaturesSorted() allFeatures=" + fc.getAllFeatures() + ", try to access "
                                + mapRowToModel(i) + ". position",
                        t); // NOI18N
                }
            }
        }
        return v;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdRemoveFeaturesActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdRemoveFeaturesActionPerformed
        mappingComponent.getMemUndo().clear();
        mappingComponent.getMemRedo().clear();
        final int[] rows = jxtFeatures.getSelectedRows();
        final Set<Feature> toRemove = TypeSafeCollections.newHashSet();
        int firstSelectedRow = -1;
        for (int i = 0; i < rows.length; i++) {
            if (firstSelectedRow == -1) {
                firstSelectedRow = rows[i];
            }
            final int mappedRow = mapRowToModel(rows[i]);
            final Feature currentFeature = getFeatureCollection().getFeature(mappedRow);
            // ENABLE THIS CODE TO HAVE THE SUBFEATURES DELETE FROM THEIR PARENT ON REMOVE FROM MAP
// if (currentFeature instanceof SubFeature) {
// SubFeature currentSubFeature = (SubFeature) currentFeature;
// FeatureGroup parent = currentSubFeature.getParentFeature();
// if (parent != null) {
// currentSubFeature.setParentFeature(null);
// parent.removeFeature(currentFeature);
// }
// }
            if (!toRemove.contains(currentFeature)) {
                if (currentFeature instanceof FeatureGroup) {
                    // delete group with all contained features
                    toRemove.addAll(FeatureGroups.expandAll((FeatureGroup)currentFeature));
                } else {
                    toRemove.add(currentFeature);
                }
            }
        }
        getFeatureCollection().removeFeatures(toRemove);
        if (jxtFeatures.getRowCount() == firstSelectedRow) {
            firstSelectedRow--;
        }
        if (firstSelectedRow >= 0) {
            jxtFeatures.getSelectionModel().setSelectionInterval(firstSelectedRow, firstSelectedRow);
        }
    } //GEN-LAST:event_cmdRemoveFeaturesActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdZoomToFeaturesMouseExited(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_cmdZoomToFeaturesMouseExited
//        mappingComponent.outlineArea((BoundingBox)null);
    } //GEN-LAST:event_cmdZoomToFeaturesMouseExited

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdZoomToFeaturesActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdZoomToFeaturesActionPerformed
        mappingComponent.zoomToSelection();
    }                                                                                     //GEN-LAST:event_cmdZoomToFeaturesActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdZoomToAllFeaturesActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdZoomToAllFeaturesActionPerformed
        mappingComponent.zoomToFeatureCollection();
    }                                                                                        //GEN-LAST:event_cmdZoomToAllFeaturesActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdZoomToFeaturesMouseEntered(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_cmdZoomToFeaturesMouseEntered
//        int[] rows=jxtFeatures.getSelectedRows();
//        Geometry g=null;
//        for (int i = 0; i < rows.length; i++) {
//            log.debug("rows["+i+"]="+rows[i]+"  ,filter.mapTowardModel(rows[i])="+filter.mapTowardModel(rows[i]));
//            int mappedRow=mapRowToModel(rows[i]);
//            if (i==0) {
//                g=getFeatureCollection().getFeature(mappedRow).getGeometry().getEnvelope();
//            } else {
//                g=g.getEnvelope().union(getFeatureCollection().getFeature(mappedRow).getGeometry().getEnvelope());
//            }
//        }
//        if (g!=null) {
//            BoundingBox bb=new BoundingBox(g);
//            mappingComponent.outlineArea(bb);
//        }
    } //GEN-LAST:event_cmdZoomToFeaturesMouseEntered

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void togShowOnlyVisibleActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togShowOnlyVisibleActionPerformed
        featureCollectionFilter.setArmed(togShowOnlyVisible.isSelected());
        fireTableDataChanged();
    }                                                                                      //GEN-LAST:event_togShowOnlyVisibleActionPerformed

    @Override
    public void shownMapBoundsChanged() {
        if (featureCollectionFilter.isArmed()) {
            fireTableDataChanged();
        }
    }

    @Override
    public void featuresRemoved(final FeatureCollectionEvent fce) {
        fireTableDataChanged();
    }

    @Override
    public void featuresChanged(final FeatureCollectionEvent fce) {
        final Collection<Feature> fc = fce.getFeatureCollection().getSelectedFeatures();
        if (LOG.isDebugEnabled()) {
            LOG.debug("in featuresChanged: Selectedfeatures (" + fc.size() + ")" + fc, new CurrentStackTrace()); // NOI18N
        }
        fireTableDataChanged();
        for (final Feature feat : fc) {
            final int index = getFeatureCollection().getAllFeatures().indexOf(feat);
            if (index != -1) {
                final int viewIndex = jxtFeatures.convertRowIndexToView(index);
                jxtFeatures.getSelectionModel().addSelectionInterval(viewIndex, viewIndex);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("SelectionIntervall added " + viewIndex);                                          // NOI18N
                }
            }
        }
    }

    @Override
    public void featuresAdded(final FeatureCollectionEvent fce) {
        fireTableDataChanged();
    }

    /**
     * DOCUMENT ME!
     */
    private void fireTableDataChanged() {
        if (EventQueue.isDispatchThread()) {
            ((FeatureCollectionTableModel)jxtFeatures.getModel()).fireTableDataChanged();
        } else {
            LOG.warn("fireTableDataChanged not in edt", new Exception());

            EventQueue.invokeLater(new Thread("fireTableDatachanged in FeatureControl") {

                    @Override
                    public void run() {
                        ((FeatureCollectionTableModel)FeatureControl.this.jxtFeatures.getModel())
                                .fireTableDataChanged();
                    }
                });
        }
    }

    @Override
    public void featureSelectionChanged(final FeatureCollectionEvent fce) {
        try {
            addFeatureToSelection(fce.getFeatureCollection());
        } catch (Exception e) {
            LOG.error("Error in featureSelectionChanged", e); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fc  DOCUMENT ME!
     */
    private void addFeatureToSelection(final FeatureCollection fc) {
        try {
//            ((FeatureCollectionTableModel)jxtFeatures.getModel()).fireTableDataChanged();
            final Collection<Feature> features = fc.getSelectedFeatures();
            jxtFeatures.getSelectionModel().removeListSelectionListener(theListSelectionListener);
            jxtFeatures.getSelectionModel().clearSelection();
            if ((features != null) && (features.size() > 0)) {
                final Iterator<Feature> featureIt = features.iterator();
                for (int i = 0; i < features.size(); ++i) {
                    Feature current = featureIt.next();
                    if (!FeatureGroups.SHOW_GROUPING_ENABLED && (current instanceof SubFeature)) {
                        final SubFeature sf = (SubFeature)current;
                        current = FeatureGroups.getRootFeature(sf);
                    }
                    final int collectionIndex = fc.getAllFeatures().indexOf(current);
                    if (collectionIndex != -1) {
                        final int viewIndex = jxtFeatures.convertRowIndexToView(collectionIndex);
                        jxtFeatures.getSelectionModel().addSelectionInterval(viewIndex, viewIndex);
                    }
                }
            } else {
                jxtFeatures.scrollRowToVisible(0);
            }
            jxtFeatures.getSelectionModel().addListSelectionListener(theListSelectionListener);
        } catch (Exception e) {
            // TODO
            LOG.error("Error in addFeatureToSelection", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isWizardMode() {
        return wizardMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  Element
     */
    @Override
    public Element getConfiguration() {
        try {
            final Element ret = new Element("cismapFeatureControl");                                           // NOI18N
            ret.setAttribute("showOnlyObjectsInMap", new Boolean(togShowOnlyVisible.isSelected()).toString()); // NOI18N
            ret.setAttribute("fixedMapExtent", new Boolean(togFixMapExtent.isSelected()).toString());          // NOI18N
            ret.setAttribute("fixedMapScale", new Boolean(togFixMapScale.isSelected()).toString());            // NOI18N
            ret.setAttribute("displayObjectInfo", new Boolean(togDisplayObjectInfo.isSelected()).toString());  // NOI18N
            ret.setAttribute("holdAll", new Boolean(togHoldAll.isSelected()).toString());                      // NOI18N
            if (jxtFeatures.getSortedColumn() != null) {
                ret.setAttribute("sortedColumn", jxtFeatures.getSortedColumn().getIdentifier().toString());    // NOI18N
                final int viewIndex = jxtFeatures.convertColumnIndexToView(jxtFeatures.getSortedColumn()
                                .getModelIndex());
                ret.setAttribute(
                    "ascendingSortOrder",
                    new Boolean(jxtFeatures.getSortOrder(viewIndex).equals(SortOrder.ASCENDING)).toString());  // NOI18N
            }
            final Element columnSequence = new Element("columnSequence");                                      // NOI18N
            for (final Object tce : jxtFeatures.getColumns()) {
                columnSequence.addContent(new Element("id").addContent(
                        ((TableColumnExt)tce).getIdentifier().toString()));                                    // NOI18N
            }
            ret.addContent(columnSequence);
            for (final Object o : jxtFeatures.getColumns(true)) {
                final TableColumnExt tce = (TableColumnExt)o;
                final Element columnElement = new Element("column");                                           // NOI18N
                columnElement.setAttribute("title", tce.getTitle());                                           // NOI18N
                columnElement.setAttribute("identifier", tce.getIdentifier().toString());                      // NOI18N
                columnElement.setAttribute("visible", new Boolean(tce.isVisible()).toString());                // NOI18N
                columnElement.setAttribute("width", "" + tce.getWidth());                                      // NOI18N
                // columnElement.setAttribute("sorterClass",tce.getSorter().);
                ret.addContent(columnElement);
            }
            return ret;
        } catch (Throwable t) {
            LOG.error("Error while creating configuration (application exit)", t); // NOI18N
            return new Element("cismapFeatureControl");                            // NOI18N
        }
    }

    @Override
    public void masterConfigure(final Element e) {
        // wird alles lokal gespeichert und auch wieder abgerufen
    }

    @Override
    public void configure(final Element e) {
        try {
            final Element conf = e.getChild("cismapFeatureControl");                                             // NOI18N
            if (conf != null) {
                try {
                    togShowOnlyVisible.setSelected(conf.getAttribute("showOnlyObjectsInMap").getBooleanValue()); // NOI18N
                } catch (Exception ex) {
                    LOG.warn("Error while reading configs", ex);                                                 // NOI18N
                }
                try {
                    togFixMapExtent.setSelected(conf.getAttribute("fixedMapExtent").getBooleanValue());          // NOI18N
                } catch (Exception ex) {
                    LOG.warn("Error while reading configs", ex);                                                 // NOI18N
                }
                try {
                    togFixMapScale.setSelected(conf.getAttribute("fixedMapScale").getBooleanValue());            // NOI18N
                } catch (Exception ex) {
                    LOG.warn("Error while reading configs", ex);                                                 // NOI18N
                }
                try {
                    togDisplayObjectInfo.setSelected(conf.getAttribute("displayObjectInfo").getBooleanValue());  // NOI18N
                } catch (Exception ex) {
                    LOG.warn("Error while reading configs", ex);                                                 // NOI18N
                }
                try {
                    togHoldAll.setSelected(conf.getAttribute("holdAll").getBooleanValue());                      // NOI18N
                } catch (Exception ex) {
                    LOG.warn("Error while reading configs", ex);                                                 // NOI18N
                }
                try {
                    final List seq = conf.getChild("columnSequence").getChildren("id");                          // NOI18N
                    final Object[] oa = new Object[seq.size()];
                    int i = 0;
                    for (final Object elem : seq) {
                        oa[i++] = ((Element)elem).getText();
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(oa[i - 1]);
                        }
                    }
                    jxtFeatures.setColumnSequence(oa);
                } catch (Exception ex) {
                    LOG.warn("Error while reading configs", ex);                                                 // NOI18N
                }
                try {
                    final String columnId = conf.getAttribute("sortedColumn").getValue();                        // NOI18N
                    final boolean ascending = conf.getAttribute("ascendingSortOrder").getBooleanValue();         // NOI18N

                    final int viewIndex = jxtFeatures.convertColumnIndexToView(jxtFeatures.getColumn(columnId)
                                    .getModelIndex());
                    jxtFeatures.toggleSortOrder(viewIndex);
                    if (!ascending) {
                        jxtFeatures.toggleSortOrder(viewIndex);
                    }
                } catch (Exception ex) {
                    LOG.warn("Error while reading configs", ex); // NOI18N
                }

                final List lst = conf.getChildren("column");                                                         // NOI18N
                try {
                    for (final Object elem : lst) {
                        final Element col = (Element)elem;
                        final Object id = col.getAttribute("identifier").getValue();                                 // NOI18N
                        try {
                            jxtFeatures.getColumnExt(id).setVisible(col.getAttribute("visible").getBooleanValue());  // NOI18N
                        } catch (Exception ex) {
                            LOG.warn("Error while reading configs", ex);                                             // NOI18N
                        }
                        try {
                            jxtFeatures.getColumnExt(id).setPreferredWidth(col.getAttribute("width").getIntValue()); // NOI18N
                        } catch (Exception ex) {
                            LOG.warn("Error while reading configs", ex);                                             // NOI18N
                        }
                    }
                } catch (Throwable t) {
                    LOG.warn("Error while configuring featureControlComponent:", t);                                 // NOI18N
                }
                featureCollectionFilter.setArmed(togShowOnlyVisible.isSelected());
                mappingComponent.setInfoNodesVisible(togDisplayObjectInfo.isSelected());
                mappingComponent.setFixedMapExtent(togFixMapExtent.isSelected());
                mappingComponent.setFixedMapScale(togFixMapScale.isSelected());
                mappingComponent.getFeatureCollection().setHoldAll(togHoldAll.isSelected());
            } else {
                LOG.warn("No configurarion data for FeatureControl available.");                                     // NOI18N
            }
        } catch (Throwable t) {
            LOG.error("Error while loading configuration (application start)", t);                                   // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void tester() {
        for (final Object tce : jxtFeatures.getColumns()) {
            // log.debug(((TableColumnExt)tce).getSorter().getComparator());
        }
    }

    @Override
    public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
    }

    @Override
    public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
        fireTableDataChanged();
    }

    @Override
    public void featureCollectionChanged() {
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class FeatureCollectionTableModel extends AbstractTableModel {

        //~ Methods ------------------------------------------------------------

        /**
         * Sets the value in the cell at <code>columnIndex</code> and <code>rowIndex</code> to <code>aValue</code>.
         *
         * @param  aValue       the new public void selectionChanged(FeatureCollectionEvent fce) { }
         *
         *                      <p>public void featuresRemoved(FeatureCollectionEvent fce) { }</p>
         *
         *                      <p>public void featuresChanged(FeatureCollectionEvent fce) { }</p>
         *
         *                      <p>public void featuresAdded(FeatureCollectionEvent fce) { } value</p>
         * @param  rowIndex     the row whose value is to be changed
         * @param  columnIndex  the column whose value is to be changed
         *
         * @see    #getValueAt
         * @see    #isCellEditable
         */
        @Override
        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
            if ((columnIndex == 1)
                        && ((Feature)getFeatureCollection().getFeature(rowIndex) instanceof AbstractNewFeature)) {
                ((AbstractNewFeature)(getFeatureCollection().getFeature(rowIndex))).setName(aValue.toString());
                final Vector v = new Vector();
                v.add(getFeatureCollection().getFeature(rowIndex));
                ((DefaultFeatureCollection)mappingComponent.getFeatureCollection()).fireFeaturesChanged(v);
            }
        }

        /**
         * Returns the name of the column at <code>columnIndex</code>. This is used to initialize the table's column
         * header name. Note: this name does not need to be unique; two columns in a table can have the same name.
         *
         * @param   columnIndex  the index of the column
         *
         * @return  the name of the column
         */
        @Override
        public String getColumnName(final int columnIndex) {
            switch (columnIndex) {
                case 0: {
                    // Icon
                    return org.openide.util.NbBundle.getMessage(
                            FeatureControl.class,
                            "FeatureControl.getColumnName(int).return.ico"); // NOI18N
                }
                case 1: {
                    // Name
                    return org.openide.util.NbBundle.getMessage(
                            FeatureControl.class,
                            "FeatureControl.getColumnName(int).return.name"); // NOI18N
                }
                case 2: {
                    // Type
                    return org.openide.util.NbBundle.getMessage(
                            FeatureControl.class,
                            "FeatureControl.getColumnName(int).return.typ"); // NOI18N
                }
                case 3: {
                    // Geometrietyp
                    return org.openide.util.NbBundle.getMessage(
                            FeatureControl.class,
                            "FeatureControl.getColumnName(int).return.geometrie"); // NOI18N
                }
                case 4: {
                    // Gr\u00F6\u00DFe
                    return org.openide.util.NbBundle.getMessage(
                            FeatureControl.class,
                            "FeatureControl.getColumnName(int).return.groesse"); // NOI18N
                }
                case 5: {
                    // L\u00E4nge
                    return org.openide.util.NbBundle.getMessage(
                            FeatureControl.class,
                            "FeatureControl.getColumnName(int).return.laenge"); // NOI18N
                }
                case 6: {
                    // Zentrum
                    return org.openide.util.NbBundle.getMessage(
                            FeatureControl.class,
                            "FeatureControl.getColumnName(int).return.zentrum"); // NOI18N
                }
                case 7: {
                    // Markierung
                    return org.openide.util.NbBundle.getMessage(
                            FeatureControl.class,
                            "FeatureControl.getColumnName(int).return.Halten"); // NOI18N
                }
                default: {
                    return "";                                                  // NOI18N
                }
            }
        }

        /**
         * Returns true if the cell at <code>rowIndex</code> and <code>columnIndex</code> is editable. Otherwise, <code>
         * setValueAt</code> on the cell will not change the value of that cell.
         *
         * @param   rowIndex     the row whose value to be queried
         * @param   columnIndex  the column whose value to be queried
         *
         * @return  true if the cell is editable
         *
         * @see     #setValueAt
         */
        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            if ((columnIndex == 1)
                        && ((Feature)getFeatureCollection().getFeature(rowIndex) instanceof AbstractNewFeature)) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Returns the value for the cell at <code>columnIndex</code> and <code>rowIndex</code>.
         *
         * @param   rowIndex     the row whose value is to be queried
         * @param   columnIndex  the column whose value is to be queried
         *
         * @return  the value Object at the specified cell
         */
        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            try {
                final Feature f = (Feature)getFeatureCollection().getFeature(rowIndex);
                switch (columnIndex) {
                    case 0: {
                        // Icon
                        if (f instanceof XStyledFeature) {
                            return (Icon)(((XStyledFeature)f).getIconImage());
                        } else {
                            return null;
                        }
                    }
                    case 1: {
                        // Name
                        if (f instanceof XStyledFeature) {
                            return ((XStyledFeature)f).getName();
                        } else {
                            return f;
                        }
                    }
                    case 2: {
                        // Type
                        if (f instanceof XStyledFeature) {
                            return ((XStyledFeature)f).getType();
                        } else if (f instanceof FeatureGroup) {
                            return "GRUPPE";
                        } else {
                            return ""; // NOI18N
                        }
                    }
                    case 3: {
                        // Geometrietyp
                        if (f.getGeometry() != null) {
                            return f.getGeometry().getGeometryType();
                        } else {
                            return ""; // NOI18N
                        }
                    }
                    case 4: {
                        // Gr\u00F6\u00DFe
                        if (f.getGeometry() != null) {
                            final Geometry geom = CrsTransformer.transformToMetricCrs(f.getGeometry(),
                                    mappingComponent.getCrsList());

                            return StaticDecimalTools.round(geom.getArea());
                        } else {
                            return 0.0;
                        }
                    }
                    case 5: {
                        // L\u00E4nge
                        if (f.getGeometry() != null) {
                            final Geometry geom = CrsTransformer.transformToMetricCrs(f.getGeometry(),
                                    mappingComponent.getCrsList());

                            return StaticDecimalTools.round(geom.getLength());
                        } else {
                            return 0.0;
                        }
                    }
                    case 6: {
                        // Zentrum
                        if (f instanceof FeatureRendererAwareFeature) {
                            final FeatureRenderer renderer = ((FeatureRendererAwareFeature)f).getFeatureRenderer();

                            if (renderer instanceof CoordHider) {
                                // The coords should not be shown, if the feature renderer implements the CoordHider
                                // interface
                                return "";
                            }
                        }

                        if (f.getGeometry() != null) {
                            final Geometry geom = CrsTransformer.transformToCurrentCrs(f.getGeometry());
                            final String pattern = (CismapBroker.getInstance().getSrs().isMetric() ? "0.00"
                                                                                                   : "0.000000");

                            return "(" + StaticDecimalTools.round(pattern, geom.getCentroid().getX()) + ","   // NOI18N
                                        + StaticDecimalTools.round(pattern, geom.getCentroid().getY()) + ")"; // NOI18N
                        } else {
                            return 0.0;
                        }
                    }
                    case 7: {
                        // Markierung
                        if (getFeatureCollection().isHoldFeature(f)) {
                            return icoGreenled;
                        } else {
                            return null;
                        }
                    }
                    default: {
                        return null;
                    }
                }
            } catch (Throwable t) {
                LOG.error("Error in table.", t); // NOI18N
                return null;
            }
        }

        /**
         * Returns the number of rows in the model. A <code>JTable</code> uses this method to determine how many rows it
         * should display. This method should be quick, as it is called frequently during rendering.
         *
         * @return  the number of rows in the model
         *
         * @see     #getColumnCount
         */
        @Override
        public int getRowCount() {
            return (getFeatureCollection() != null) ? getFeatureCollection().getFeatureCount() : 0;
        }

        /**
         * Returns the number of columns in the model. A <code>JTable</code> uses this method to determine how many
         * columns it should create and display by default.
         *
         * @return  the number of columns in the model
         *
         * @see     #getRowCount
         */
        @Override
        public int getColumnCount() {
            return 8;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class SubFeatureFilter extends RowFilter<AbstractTableModel, Integer> {

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean include(final RowFilter.Entry entry) {
            final int modelRow = (Integer)entry.getIdentifier();
            try {
                final Feature currentTestFeature = getFeatureCollection().getFeature(modelRow);
                if (currentTestFeature instanceof SubFeature) {
                    return ((SubFeature)currentTestFeature).getParentFeature() == null;
                }
                return true;
            } catch (Throwable t) {
                LOG.error("Error while testing in filter", t); // NOI18N
                return true;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class FeatureCollectionFilter extends RowFilter<AbstractTableModel, Integer> {

        //~ Instance fields ----------------------------------------------------

        private FeatureCollectionTableModel model;
        private boolean armed = false;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FeatureCollectionFilter object.
         *
         * @param  armed  DOCUMENT ME!
         * @param  model  DOCUMENT ME!
         */
        public FeatureCollectionFilter(final boolean armed, final FeatureCollectionTableModel model) {
            this.armed = armed;
            this.model = model;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean include(final RowFilter.Entry entry) {
            if (!armed) {
                return true;
            } else {
                final int modelRow = (Integer)entry.getIdentifier();

                try {
                    final Object value = getFeatureCollection().getFeature(modelRow);
                    final PFeature pf = (PFeature)(mappingComponent.getPFeatureHM().get(value));
                    if (pf != null) {
                        final PBounds all = mappingComponent.getCamera().getViewBounds();
                        final PDimension delta = all.deltaRequiredToContain(pf.getFullBounds());
                        mappingComponent.getCamera().viewToLocal(delta);
                        final PDimension size = new PDimension(pf.getFullBounds().getSize());
                        mappingComponent.getCamera().viewToLocal(size);
                        final boolean test = ((Math.abs(delta.width) <= size.width)
                                        && (Math.abs(delta.height) <= size.height));
                        return test;
                    } else {
                        return false;
                    }
                } catch (Throwable t) {
                    LOG.error("Error while testing in filter.", t); // NOI18N
                    return true;
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isArmed() {
            return armed;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  armed  DOCUMENT ME!
         */
        public void setArmed(final boolean armed) {
            this.armed = armed;
            model.fireTableDataChanged();
        }
    }
}
