/*
 * FeatureControl.java
 *
 * Created on 3. Mai 2006, 11:31
 */
package de.cismet.cismap.commons.gui.featurecontrolwidget;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.MapBoundsListener;
import de.cismet.tools.CurrentStackTrace;
import de.cismet.tools.StaticDecimalTools;
import de.cismet.tools.configuration.Configurable;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Filter;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 *
 * @author  thorsten.hell@cismet.de
 */
public class FeatureControl extends javax.swing.JPanel implements FeatureCollectionListener, MapBoundsListener, Configurable {

    private static final java.util.ResourceBundle I18N =
            java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle");

    final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private FeatureCollectionFilter filter;
    private ImageIcon icoGreenled = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/greenled.png"));
    boolean wizardMode = false;
    private MappingComponent mappingComponent = null;
    private ListSelectionListener theListSelectionListener = new ListSelectionListener() {

        public void valueChanged(ListSelectionEvent e) {
            log.debug("ValueChanged of SelectionListener from jxtFeatures: " + e);
            int[] rows = jxtFeatures.getSelectedRows();
//            mappingComponent.getFeatureCollection().unselectAll(false);
            List<Feature> list = new LinkedList<Feature>();
            for (int i = 0; i < rows.length; i++) {
                int mappedRow = mapRowToModel(rows[i]);
                list.add(getFeatureCollection().getFeature(mappedRow));
//                if (!(mappingComponent.getFeatureCollection().getSelectedFeatures().contains(getFeatureCollection().getFeature(mappedRow)))) {
//                    mappingComponent.getFeatureCollection().removeFeatureCollectionListener(FeatureControl.this);
//                    mappingComponent.getFeatureCollection().addToSelection(getFeatureCollection().getFeature(mappedRow));
//                }
            }
            // Hinter die Schleife gestellt, damit nicht f\u00FCr alle selektierten Features ein Event gefeuert wird
            mappingComponent.getFeatureCollection().removeFeatureCollectionListener(FeatureControl.this);
            mappingComponent.getFeatureCollection().select(list);
            mappingComponent.getFeatureCollection().addFeatureCollectionListener(FeatureControl.this);

        }
    };


    /** Creates new form FeatureControl */
    public FeatureControl(de.cismet.cismap.commons.gui.MappingComponent mappingComponent) {
        initComponents();
        jxtFeatures.setModel(new FeatureCollectionTableModel());
        this.mappingComponent = mappingComponent;
//        Enumeration en = jxtFeatures.getColumnModel().getColumns();
//        while ( en.hasMoreElements() ) {
//            TableColumn tc = (TableColumn)en.nextElement();
//            tc.setIdentifier(tc.getIdentifier());
//        }

        //Vorerst: SingleSelection
        //jxtFeatures.setSelectionMode(jxtFeatures.getSelectionModel().SINGLE_SELECTION);
        //jxtFeatures.setAutoCreateColumnsFromModel(true);

        jxtFeatures.getColumnModel().getColumn(0).setCellRenderer(jxtFeatures.getDefaultRenderer(Icon.class));
        jxtFeatures.getColumnModel().getColumn(4).setCellRenderer(jxtFeatures.getDefaultRenderer(Number.class));
        jxtFeatures.getColumnModel().getColumn(5).setCellRenderer(jxtFeatures.getDefaultRenderer(Number.class));
        jxtFeatures.getColumnModel().getColumn(7).setCellRenderer(jxtFeatures.getDefaultRenderer(Icon.class));
        //jxtFeatures.getColumnModel().getColumn(7).setCellEditor(new JXTable.BooleanEditor());

        filter = new FeatureCollectionFilter(false);
        Filter[] filterArray = {filter};
        FilterPipeline filters = new FilterPipeline(filterArray);
        jxtFeatures.setFilters(filters);
        //jxtFeatures.setHighlighters(new HighlighterPipeline(new Highlighter[]{ AlternateRowHighlighter.classicLinePrinter }));
        jxtFeatures.getSelectionModel().addListSelectionListener(theListSelectionListener);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        togSelectionWizard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/wizard.png"))); // NOI18N
        togSelectionWizard.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/wizard.png"))); // NOI18N
        togSelectionWizard.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/wizard.png"))); // NOI18N
        togSelectionWizard.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/wizard.png"))); // NOI18N
        togSelectionWizard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                togSelectionWizardActionPerformed(evt);
            }
        });

        setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        setLayout(new java.awt.BorderLayout());

        jToolBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        cmdZoomToAllFeatures.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/zoomToAll.png"))); // NOI18N
        cmdZoomToAllFeatures.setToolTipText(I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.cmdZoomToAllFeatures.toolTipText")); // NOI18N
        cmdZoomToAllFeatures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdZoomToAllFeaturesActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdZoomToAllFeatures);

        cmdZoomToFeatures.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/zoomToSelection.png"))); // NOI18N
        cmdZoomToFeatures.setToolTipText(I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.cmdZoomToFeatures.toolTipText")); // NOI18N
        cmdZoomToFeatures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdZoomToFeaturesActionPerformed(evt);
            }
        });
        cmdZoomToFeatures.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cmdZoomToFeaturesMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cmdZoomToFeaturesMouseExited(evt);
            }
        });
        jToolBar1.add(cmdZoomToFeatures);

        cmdRemoveFeatures.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/removerow.png"))); // NOI18N
        cmdRemoveFeatures.setToolTipText(I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.cmdRemoveFeatures.toolTipText")); // NOI18N
        cmdRemoveFeatures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRemoveFeaturesActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdRemoveFeatures);

        cmdRemoveAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/removeAll.png"))); // NOI18N
        cmdRemoveAll.setToolTipText(I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.cmdRemoveAll.toolTipText")); // NOI18N
        cmdRemoveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRemoveAllActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdRemoveAll);

        togShowOnlyVisible.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/showOnlyVisible_disabled.png"))); // NOI18N
        togShowOnlyVisible.setToolTipText(I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.togShowOnlyVisible.toolTipText")); // NOI18N
        togShowOnlyVisible.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/showOnlyVisible_disabled.png"))); // NOI18N
        togShowOnlyVisible.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/showOnlyVisible.png"))); // NOI18N
        togShowOnlyVisible.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/showOnlyVisible.png"))); // NOI18N
        togShowOnlyVisible.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                togShowOnlyVisibleActionPerformed(evt);
            }
        });
        jToolBar1.add(togShowOnlyVisible);

        togFixMapExtent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapExtent_disabled.png"))); // NOI18N
        togFixMapExtent.setToolTipText(I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.togFixMapExtent.toolTipText")); // NOI18N
        togFixMapExtent.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapExtent_disabled.png"))); // NOI18N
        togFixMapExtent.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapExtent.png"))); // NOI18N
        togFixMapExtent.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapExtent.png"))); // NOI18N
        togFixMapExtent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                togFixMapExtentActionPerformed(evt);
            }
        });
        jToolBar1.add(togFixMapExtent);

        togFixMapScale.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapScale_disabled.png"))); // NOI18N
        togFixMapScale.setToolTipText(I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.togFixMapScale.toolTipText")); // NOI18N
        togFixMapScale.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapScale_disabled.png"))); // NOI18N
        togFixMapScale.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapScale.png"))); // NOI18N
        togFixMapScale.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/fixMapScale.png"))); // NOI18N
        togFixMapScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                togFixMapScaleActionPerformed(evt);
            }
        });
        jToolBar1.add(togFixMapScale);

        togDisplayObjectInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/infoLabel_disabled.png"))); // NOI18N
        togDisplayObjectInfo.setToolTipText(I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.togDisplayObjectInfo.toolTipText")); // NOI18N
        togDisplayObjectInfo.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/infoLabel_disabled.png"))); // NOI18N
        togDisplayObjectInfo.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/infoLabel.png"))); // NOI18N
        togDisplayObjectInfo.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/infoLabel.png"))); // NOI18N
        togDisplayObjectInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                togDisplayObjectInfoActionPerformed(evt);
            }
        });
        jToolBar1.add(togDisplayObjectInfo);

        togHoldAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/holdAll_disabled.png"))); // NOI18N
        togHoldAll.setToolTipText(I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.togHoldAll.toolTipText")); // NOI18N
        togHoldAll.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/holdAll_disabled.png"))); // NOI18N
        togHoldAll.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/holdAll.png"))); // NOI18N
        togHoldAll.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/holdAll.png"))); // NOI18N
        togHoldAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                togHoldAllActionPerformed(evt);
            }
        });
        jToolBar1.add(togHoldAll);

        cmdHold.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/holdSelection.png"))); // NOI18N
        cmdHold.setToolTipText(I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.cmdHold.toolTipText")); // NOI18N
        cmdHold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdHoldActionPerformed(evt);
            }
        });
        jToolBar1.add(cmdHold);

        add(jToolBar1, java.awt.BorderLayout.NORTH);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jScrollPane2.setViewportView(jxtFeatures);

        add(jScrollPane2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    private void togHoldAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togHoldAllActionPerformed
        mappingComponent.getFeatureCollection().setHoldAll(togHoldAll.isSelected());
    }//GEN-LAST:event_togHoldAllActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        tester();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void cmdRemoveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemoveAllActionPerformed
        mappingComponent.getFeatureCollection().removeAllFeatures();
        mappingComponent.getMemUndo().clear();
        mappingComponent.getMemRedo().clear();
    }//GEN-LAST:event_cmdRemoveAllActionPerformed

    private void togSelectionWizardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togSelectionWizardActionPerformed
        wizardMode = togSelectionWizard.isSelected();
    }//GEN-LAST:event_togSelectionWizardActionPerformed

    private void togFixMapScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togFixMapScaleActionPerformed
        mappingComponent.setFixedMapScale(togFixMapScale.isSelected());
    }//GEN-LAST:event_togFixMapScaleActionPerformed

    private void togFixMapExtentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togFixMapExtentActionPerformed
        mappingComponent.setFixedMapExtent(togFixMapExtent.isSelected());
    }//GEN-LAST:event_togFixMapExtentActionPerformed

    private void togDisplayObjectInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togDisplayObjectInfoActionPerformed
        mappingComponent.setInfoNodesVisible(togDisplayObjectInfo.isSelected());
    }//GEN-LAST:event_togDisplayObjectInfoActionPerformed

    private void cmdHoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdHoldActionPerformed
        int[] rows = jxtFeatures.getSelectedRows();
        for (int i = 0; i < rows.length; i++) {
            int mappedRow = mapRowToModel(rows[i]);
            Feature f = getFeatureCollection().getFeature(mappedRow);
            if (getFeatureCollection().isHoldFeature(f)) {
                getFeatureCollection().unholdFeature(f);
            } else {
                getFeatureCollection().holdFeature(f);
            }
        }
    }//GEN-LAST:event_cmdHoldActionPerformed

    private FeatureCollection getFeatureCollection() {
        try {
            return mappingComponent.getFeatureCollection();
        } catch (Exception e) {
            log.warn("Bei getFeatureCollection() geht was schief", e);
            return new DefaultFeatureCollection();
        }
    }

    private int mapRowToModel(int displayedRow) {
        return jxtFeatures.getFilters().convertRowIndexToModel(displayedRow);
        //return jxtFeatures.getFilters().convertRowIndexToView(displayedRow);
    }

    public Vector<Feature> getAllFeaturesSorted() {
        Vector<Feature> v = new Vector<Feature>();
        FeatureCollection fc = getFeatureCollection();
        if (fc.getFeatureCount() > 0) {
            for (int i = 0; i < jxtFeatures.getRowCount(); ++i) {
                try {
                    v.add((Feature) fc.getAllFeatures().get(mapRowToModel(i)));
                } catch (Throwable t) {
                    log.error("Fehler in getAllFeaturesSorted() alleFeatures=" + fc.getAllFeatures() + ", Zugriffsversuch an " + mapRowToModel(i) + ".te Stelle", t);
                }
            }
        }
        return v;
    }

    private void cmdRemoveFeaturesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemoveFeaturesActionPerformed
        mappingComponent.getMemUndo().clear();
        mappingComponent.getMemRedo().clear();
        int[] rows = jxtFeatures.getSelectedRows();
        Vector<Feature> remove = new Vector<Feature>();
        int firstSelectedRow = -1;
        for (int i = 0; i < rows.length; i++) {
            if (firstSelectedRow == -1) {
                firstSelectedRow = rows[i];
            }
            int mappedRow = mapRowToModel(rows[i]);
            remove.add(getFeatureCollection().getFeature(mappedRow));
        }
        getFeatureCollection().removeFeatures(remove);
        if (jxtFeatures.getRowCount() == firstSelectedRow) {
            firstSelectedRow--;
        }
        if (firstSelectedRow >= 0) {
            jxtFeatures.getSelectionModel().setSelectionInterval(firstSelectedRow, firstSelectedRow);
        }
    }//GEN-LAST:event_cmdRemoveFeaturesActionPerformed

    private void cmdZoomToFeaturesMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cmdZoomToFeaturesMouseExited
//        mappingComponent.outlineArea((BoundingBox)null);
    }//GEN-LAST:event_cmdZoomToFeaturesMouseExited

    private void cmdZoomToFeaturesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdZoomToFeaturesActionPerformed
        mappingComponent.zoomToSelection();
    }//GEN-LAST:event_cmdZoomToFeaturesActionPerformed

    private void cmdZoomToAllFeaturesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdZoomToAllFeaturesActionPerformed
        mappingComponent.zoomToFeatureCollection();
    }//GEN-LAST:event_cmdZoomToAllFeaturesActionPerformed

    private void cmdZoomToFeaturesMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cmdZoomToFeaturesMouseEntered
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
    }//GEN-LAST:event_cmdZoomToFeaturesMouseEntered

    private void togShowOnlyVisibleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togShowOnlyVisibleActionPerformed
        filter.setArmed(togShowOnlyVisible.isSelected());
        ((FeatureCollectionTableModel) jxtFeatures.getModel()).fireTableDataChanged();
    }//GEN-LAST:event_togShowOnlyVisibleActionPerformed
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

    public void shownMapBoundsChanged() {
        //refreshTableAndTryToKeepTheFuckingSelection();
        if (filter.isArmed()) {
            ((FeatureCollectionTableModel) jxtFeatures.getModel()).fireTableDataChanged();
        }
        JXTable t = new JXTable();
        t.getSortedColumn();
    }

//    private void refreshTableAndTryToKeepTheFuckingSelection() {
//        int[] i=jxtFeatures.getSelectedRows();
//        ((FeatureCollectionTableModel)jxtFeatures.getModel()).fireTableDataChanged();
//        for (int j = 0; j < i.length; j++) {
//            jxtFeatures.getSelectionModel().setSelectionInterval(i[j],i[j]);
//        }
//
//    }
    public void featuresRemoved(FeatureCollectionEvent fce) {
        ((FeatureCollectionTableModel) jxtFeatures.getModel()).fireTableDataChanged();
    }

    public void featuresChanged(FeatureCollectionEvent fce) {
        Feature f = null;
        Collection<Feature> fc = fce.getFeatureCollection().getSelectedFeatures();
        log.debug("in featuresChanged: Selectedfeatures (" + fc.size() + ")" + fc, new CurrentStackTrace());
        ((FeatureCollectionTableModel) jxtFeatures.getModel()).fireTableDataChanged();
        for (Feature feat : fc) {
            int index = getFeatureCollection().getAllFeatures().indexOf(feat);
            int viewIndex = jxtFeatures.convertRowIndexToView(index);
            jxtFeatures.getSelectionModel().addSelectionInterval(viewIndex, viewIndex);
            log.debug("SelectionIntervall added " + viewIndex);
        }
    }

    public void featuresAdded(FeatureCollectionEvent fce) {
        ((FeatureCollectionTableModel) jxtFeatures.getModel()).fireTableDataChanged();
    }

    public void featureSelectionChanged(FeatureCollectionEvent fce) {
        try {
            if (fce.getFeatureCollection().getSelectedFeatures().size() == 1) {
                Feature f = (Feature) fce.getFeatureCollection().getSelectedFeatures().toArray()[0];
                int index = getFeatureCollection().getAllFeatures().indexOf(f);
                int viewIndex = jxtFeatures.convertRowIndexToView(index);
                jxtFeatures.getSelectionModel().removeListSelectionListener(theListSelectionListener);
                jxtFeatures.getSelectionModel().setSelectionInterval(viewIndex, viewIndex);
                jxtFeatures.getSelectionModel().addListSelectionListener(theListSelectionListener);
                jxtFeatures.scrollRowToVisible(viewIndex);
            } else if (fce.getFeatureCollection().getSelectedFeatures().size() == 0) {
                jxtFeatures.getSelectionModel().removeListSelectionListener(theListSelectionListener);
                jxtFeatures.getSelectionModel().clearSelection();
                jxtFeatures.getSelectionModel().addListSelectionListener(theListSelectionListener);
                jxtFeatures.scrollRowToVisible(0);
            } else // if (fce.getFeatureCollection().getSelectedFeatures().size() > 1) 
            {
                Object[] fs = fce.getFeatureCollection().getSelectedFeatures().toArray();
                jxtFeatures.getSelectionModel().removeListSelectionListener(theListSelectionListener);
                jxtFeatures.getSelectionModel().clearSelection();
                for (Object o : fs) {
                    if (o instanceof Feature) {
                        Feature f = (Feature) o;
                        int index = getFeatureCollection().getAllFeatures().indexOf(f);
                        int viewIndex = jxtFeatures.convertRowIndexToView(index);
                        jxtFeatures.getSelectionModel().addSelectionInterval(viewIndex, viewIndex);
                    }
                }
                jxtFeatures.getSelectionModel().addListSelectionListener(theListSelectionListener);
            }
        } catch (Exception e) {
            log.error("Fehler in featureSelectionChanged", e);
        }
    }

    class FeatureCollectionTableModel extends AbstractTableModel {

        /**
         * Sets the value in the cell at <code>columnIndex</code> and
         * <code>rowIndex</code> to <code>aValue</code>.
         *
         *
         * @param aValue		 the new
         * public void selectionChanged(FeatureCollectionEvent fce) {
         * }
         *
         * public void featuresRemoved(FeatureCollectionEvent fce) {
         * }
         *
         * public void featuresChanged(FeatureCollectionEvent fce) {
         * }
         *
         * public void featuresAdded(FeatureCollectionEvent fce) {
         * }
         * value
         * @param rowIndex	 the row whose value is to be changed
         * @param columnIndex 	 the column whose value is to be changed
         * @see #getValueAt
         * @see #isCellEditable
         */
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1 && (Feature) getFeatureCollection().getFeature(rowIndex) instanceof PureNewFeature) {
                ((PureNewFeature) (getFeatureCollection().getFeature(rowIndex))).setName(aValue.toString());
                Vector v = new Vector();
                v.add(getFeatureCollection().getFeature(rowIndex));
                ((DefaultFeatureCollection) mappingComponent.getFeatureCollection()).fireFeaturesChanged(v);
            }
        }

        /**
         * Returns the name of the column at <code>columnIndex</code>.  This is used
         * to initialize the table's column header name.  Note: this name does
         * not need to be unique; two columns in a table can have the same name.
         * @param columnIndex	the index of the column
         * @return the name of the column
         */
        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0: //Icon
                    return I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.getColumnName().return.ico");
                case 1: //Name
                    return I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.getColumnName().return.name");
                case 2: //Type
                    return I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.getColumnName().return.typ");
                case 3: //Geometrietyp
                    return I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.getColumnName().return.geometrie");
                case 4: //Gr\u00F6\u00DFe
                    return I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.getColumnName().return.groesse");
                case 5: //L\u00E4nge
                    return I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.getColumnName().return.laenge");
                case 6: //Zentrum
                    return I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.getColumnName().return.zentrum");
                case 7: //Markierung
                    return I18N.getString("de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl.getColumnName().return.Halten");
                default:
                    return "";
            }
        }

        /**
         * Returns true if the cell at <code>rowIndex</code> and
         * <code>columnIndex</code>
         * is editable.  Otherwise, <code>setValueAt</code> on the cell will not
         * change the value of that cell.
         * @param rowIndex	the row whose value to be queried
         * @param columnIndex	the column whose value to be queried
         * @return true if the cell is editable
         * @see #setValueAt
         */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 1 && (Feature) getFeatureCollection().getFeature(rowIndex) instanceof PureNewFeature) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Returns the value for the cell at <code>columnIndex</code> and
         * <code>rowIndex</code>.
         * @param rowIndex	the row whose value is to be queried
         * @param columnIndex 	the column whose value is to be queried
         * @return the value Object at the specified cell
         */
        public Object getValueAt(int rowIndex, int columnIndex) {
            try {
                Feature f = (Feature) getFeatureCollection().getFeature(rowIndex);
                switch (columnIndex) {
                    case 0: //Icon
                        if (f instanceof XStyledFeature) {
                            return (Icon) (((XStyledFeature) f).getIconImage());
                        } else {
                            return null;
                        }
                    case 1: //Name
                        if (f instanceof XStyledFeature) {
                            return ((XStyledFeature) f).getName();
                        } else {
                            return f;
                        }
                    case 2: //Type
                        if (f instanceof XStyledFeature) {
                            return ((XStyledFeature) f).getType();
                        } else {
                            return "";
                        }
                    case 3: //Geometrietyp
                        if (f.getGeometry() != null) {
                            return f.getGeometry().getGeometryType();
                        } else {
                            return "";
                        }
                    case 4: //Gr\u00F6\u00DFe
                        if (f.getGeometry() != null) {
                            return StaticDecimalTools.round(f.getGeometry().getArea());
                        } else {
                            return 0.0;
                        }
                    case 5: //L\u00E4nge
                        if (f.getGeometry() != null) {
                            return StaticDecimalTools.round(f.getGeometry().getLength());
                        } else {
                            return 0.0;
                        }
                    case 6: //Zentrum
                        if (f.getGeometry() != null) {
                            return "(" + StaticDecimalTools.round(f.getGeometry().getCentroid().getX()) + "," +
                                    StaticDecimalTools.round(f.getGeometry().getCentroid().getY()) + ")";
                        } else {
                            return 0.0;
                        }
                    case 7: //Markierung
                        if (getFeatureCollection().isHoldFeature(f)) {
                            return icoGreenled;
                        } else {
                            return null;
                        }
                    default:
                        return null;
                }
            } catch (Throwable t) {
                log.error("Error in table.", t);
                return null;
            }
        }

        /**
         * Returns the number of rows in the model. A
         * <code>JTable</code> uses this method to determine how many rows it
         * should display.  This method should be quick, as it
         * is called frequently during rendering.
         * @return the number of rows in the model
         * @see #getColumnCount
         */
        public int getRowCount() {
            return getFeatureCollection().getFeatureCount();
        }

        /**
         * Returns the number of columns in the model. A
         * <code>JTable</code> uses this method to determine how many columns it
         * should create and display by default.
         * @return the number of columns in the model
         * @see #getRowCount
         */
        public int getColumnCount() {
            return 8;
        }
    }

    class FeatureCollectionFilter extends Filter {

        private ArrayList<Integer> toPrevious;
        private boolean armed = false;

        public FeatureCollectionFilter(boolean armed) {
            super(0);
            init();
            reset();
            this.armed = armed;
            log.debug("Filter initialised.");
        }

        protected void reset() {
            toPrevious.clear();
            int inputSize = getInputSize();
            fromPrevious = new int[inputSize];  // fromPrevious is inherited protected
            for (int i = 0; i < inputSize; i++) {
                fromPrevious[i] = -1;
            }
        }

        protected void init() {
            toPrevious = new ArrayList<Integer>();
        }

        public int getSize() {
            return toPrevious.size();
        }

        protected int mapTowardModel(int row) {
            return toPrevious.get(row);
        }

        public boolean test(int row) {
            if (!armed) {
                return true;
            } else {
                try {
                    Object value = getFeatureCollection().getFeature(row);
                    PFeature pf = (PFeature) (mappingComponent.getPFeatureHM().get(value));
                    PBounds all = mappingComponent.getCamera().getViewBounds();
                    PDimension delta = all.deltaRequiredToContain(pf.getFullBounds());
                    mappingComponent.getCamera().viewToLocal(delta);
                    PDimension size = new PDimension(pf.getFullBounds().getSize());
                    mappingComponent.getCamera().viewToLocal(size);
                    //boolean test=(Math.abs(delta.width)<pf.getFullBounds().getWidth()*0.75 && Math.abs(delta.height)<pf.getFullBounds().getHeight()*0.75);
                    //boolean test=(Math.abs(delta.width)<=pf.getFullBounds().getWidth()-10 && Math.abs(delta.height)<=pf.getFullBounds().getHeight()-10);
                    boolean test = (Math.abs(delta.width) <= size.width && Math.abs(delta.height) <= size.height);
                    return test;
                } catch (Throwable t) {
                    log.error("Error while testing in filter.", t);
                    return true;
                }
            }
        }

        protected void filter() {
            int inputSize = getInputSize();
            int current = 0;
            for (int i = 0; i < inputSize; i++) {
                if (test(i)) {
                    toPrevious.add(new Integer(i));
                    // generate inverse map entry while we are here
                    fromPrevious[i] = current++;
                }
            }
        }

        public boolean isArmed() {
            return armed;
        }

        public void setArmed(boolean armed) {
            this.armed = armed;
            refresh();
        }
    }

    public boolean isWizardMode() {
        return wizardMode;
    }

    /**
     *
     * @return Element
     */
    public Element getConfiguration() {
        try {
            Element ret = new Element("cismapFeatureControl");
            ret.setAttribute("showOnlyObjectsInMap", new Boolean(togShowOnlyVisible.isSelected()).toString());
            ret.setAttribute("fixedMapExtent", new Boolean(togFixMapExtent.isSelected()).toString());
            ret.setAttribute("fixedMapScale", new Boolean(togFixMapScale.isSelected()).toString());
            ret.setAttribute("displayObjectInfo", new Boolean(togDisplayObjectInfo.isSelected()).toString());
            ret.setAttribute("holdAll", new Boolean(togHoldAll.isSelected()).toString());
            if (jxtFeatures.getSortedColumn() != null) {
                ret.setAttribute("sortedColumn", jxtFeatures.getSortedColumn().getIdentifier().toString());
                int viewIndex = jxtFeatures.convertColumnIndexToView(jxtFeatures.getSortedColumn().getModelIndex());
                ret.setAttribute("ascendingSortOrder", new Boolean(jxtFeatures.getSortOrder(viewIndex).isAscending()).toString());
            }
            Element columnSequence = new Element("columnSequence");
            for (Object tce : jxtFeatures.getColumns()) {
                columnSequence.addContent(new Element("id").addContent(((TableColumnExt) tce).getIdentifier().toString()));
            }
            ret.addContent(columnSequence);
            for (Object o : jxtFeatures.getColumns(true)) {
                TableColumnExt tce = (TableColumnExt) o;
                Element columnElement = new Element("column");
                columnElement.setAttribute("title", tce.getTitle());
                columnElement.setAttribute("identifier", tce.getIdentifier().toString());
                columnElement.setAttribute("visible", new Boolean(tce.isVisible()).toString());
                columnElement.setAttribute("width", "" + tce.getWidth());
                //columnElement.setAttribute("sorterClass",tce.getSorter().);
                ret.addContent(columnElement);
            }
            return ret;
        } catch (Throwable t) {
            log.error("Error while creating configuration (application exit)", t);
            return new Element("cismapFeatureControl");
        }
    }

    public void masterConfigure(Element e) {
        //wird alles lokal gespeichert und auch wieder abgerufen
    }

    public void configure(Element e) {
        try {
            Element conf = e.getChild("cismapFeatureControl");
            if (conf != null) {
                try {
                    togShowOnlyVisible.setSelected(conf.getAttribute("showOnlyObjectsInMap").getBooleanValue());
                } catch (Exception ex) {
                    log.warn("Error while reading configs", ex);
                }
                try {
                    togFixMapExtent.setSelected(conf.getAttribute("fixedMapExtent").getBooleanValue());
                } catch (Exception ex) {
                    log.warn("Error while reading configs", ex);
                }
                try {
                    togFixMapScale.setSelected(conf.getAttribute("fixedMapScale").getBooleanValue());
                } catch (Exception ex) {
                    log.warn("Error while reading configs", ex);
                }
                try {
                    togDisplayObjectInfo.setSelected(conf.getAttribute("displayObjectInfo").getBooleanValue());
                } catch (Exception ex) {
                    log.warn("Error while reading configs", ex);
                }
                try {
                    togHoldAll.setSelected(conf.getAttribute("holdAll").getBooleanValue());
                } catch (Exception ex) {
                    log.warn("Error while reading configs", ex);
                }
                try {
                    List seq = conf.getChild("columnSequence").getChildren("id");
                    Object[] oa = new Object[seq.size()];
                    int i = 0;
                    for (Object elem : seq) {
                        oa[i++] = ((Element) elem).getText();
                        log.debug(oa[i - 1]);
                    }
                    jxtFeatures.setColumnSequence(oa);
                } catch (Exception ex) {
                    log.warn("Error while reading configs", ex);
                }
                try {
                    String columnId = conf.getAttribute("sortedColumn").getValue();
                    boolean ascending = conf.getAttribute("ascendingSortOrder").getBooleanValue();

                    int viewIndex = jxtFeatures.convertColumnIndexToView(jxtFeatures.getColumn(columnId).getModelIndex());
                    jxtFeatures.toggleSortOrder(viewIndex);
                    if (!ascending) {
                        jxtFeatures.toggleSortOrder(viewIndex);
                    }
                } catch (Exception ex) {
                    log.warn("Error while reading configs", ex);
                }

                List lst = conf.getChildren("column");
                try {
                    for (Object elem : lst) {
                        Element col = (Element) elem;
                        Object id = col.getAttribute("identifier").getValue();
                        try {
                            jxtFeatures.getColumnExt(id).setVisible(col.getAttribute("visible").getBooleanValue());
                        } catch (Exception ex) {
                            log.warn("Error while reading configs", ex);
                        }
                        try {
                            jxtFeatures.getColumnExt(id).setPreferredWidth(col.getAttribute("width").getIntValue());
                            //jxtFeatures.getColumnExt(id).setWidth();
                        } catch (Exception ex) {
                            log.warn("Error while reading configs", ex);
                        }
                    }
                } catch (Throwable t) {
                    log.warn("Error while configuring featureControlComponent:", t);
                }
                filter.setArmed(togShowOnlyVisible.isSelected());
                mappingComponent.setInfoNodesVisible(togDisplayObjectInfo.isSelected());
                mappingComponent.setFixedMapExtent(togFixMapExtent.isSelected());
                mappingComponent.setFixedMapScale(togFixMapScale.isSelected());
                mappingComponent.getFeatureCollection().setHoldAll(togHoldAll.isSelected());
            } else {
                log.warn("No configurarion data for FeatureControl available.");
            }
        } catch (Throwable t) {
            log.error("Error while loading configuration (application start)", t);
        }
    }

    private void tester() {
        for (Object tce : jxtFeatures.getColumns()) {
            //log.debug(((TableColumnExt)tce).getSorter().getComparator());
        }
    }

    public void featureReconsiderationRequested(FeatureCollectionEvent fce) {
    }

    public void allFeaturesRemoved(FeatureCollectionEvent fce) {
        ((FeatureCollectionTableModel) jxtFeatures.getModel()).fireTableDataChanged();
    }

    public void featureCollectionChanged() {
    }


}
