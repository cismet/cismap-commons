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
package de.cismet.cismap.commons.gui.featureinfopanel;

import com.vividsolutions.jts.geom.Geometry;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.log4j.Logger;

import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.Font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.PermissionProvider;
import de.cismet.cismap.commons.features.WMSFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureLockerFactory;
import de.cismet.cismap.commons.gui.attributetable.FeatureLockingInterface;
import de.cismet.cismap.commons.gui.attributetable.LockAlreadyExistsException;
import de.cismet.cismap.commons.gui.featureinfowidget.FeatureInfoWidget;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.LayerCombobox;
import de.cismet.cismap.commons.gui.layerwidget.LayerFilter;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerWidget;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToFeaturesWorker;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.GetFeatureInfoClickDetectionListener;
import de.cismet.cismap.commons.interaction.GetFeatureInfoListener;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.interaction.events.GetFeatureInfoEvent;
import de.cismet.cismap.commons.interaction.events.MapClickedEvent;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.rasterservice.MapService;

import de.cismet.tools.gui.CellSpecificRenderedTable;
import de.cismet.tools.gui.DefaultPopupMenuListener;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FeatureInfoPanel extends javax.swing.JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FeatureInfoPanel.class);
    private static final String REPORT_URL = "/de/cismet/cismap/commons/gui/featureinfopanel/InfoTableTemplate.jasper";

    //~ Instance fields --------------------------------------------------------

    private ActiveLayerModel layerModel;
    private MappingComponent mappingComonent;
    private ThemeLayerWidget themeLayerWidget;
    private LayerFilterTreeModel model;
    private FeatureInfoWidget featureInfo;
    private DefaultPopupMenuListener popupMenuListener;
    private List<FeatureServiceFeature> changedFeatures = new ArrayList<FeatureServiceFeature>();
    private AttribueTableModel currentTableModel;
    private Map<Feature, Object> lockMap = new HashMap<Feature, Object>();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jtFeatures;
    private de.cismet.cismap.commons.gui.layerwidget.LayerCombobox layerCombobox1;
    private javax.swing.JMenuItem miEdit;
    private javax.swing.JMenuItem miPrint;
    private javax.swing.JMenuItem miZoom;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JScrollPane sbAttributes;
    private org.jdesktop.swingx.JXTable tabAttributes;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form FeatureInfoPanel.
     */
    public FeatureInfoPanel() {
        this(null, null);
    }

    /**
     * Creates new form FeatureInfoPanel.
     *
     * @param  mappingComonent   DOCUMENT ME!
     * @param  themeLayerWidget  DOCUMENT ME!
     */
    public FeatureInfoPanel(final MappingComponent mappingComonent, final ThemeLayerWidget themeLayerWidget) {
        this.layerModel = (ActiveLayerModel)mappingComonent.getMappingModel();
        this.mappingComonent = mappingComonent;
        this.themeLayerWidget = themeLayerWidget;
        initComponents();
        popupMenuListener = new DefaultPopupMenuListener(popupMenu);
        jtFeatures.addMouseListener(popupMenuListener);
        featureInfo = new FeatureInfoWidget();
        jtFeatures.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jtFeatures.setCellRenderer(new DefaultTreeCellRenderer() {

                @Override
                public Component getTreeCellRendererComponent(final JTree tree,
                        final Object value,
                        final boolean sel,
                        final boolean expanded,
                        final boolean leaf,
                        final int row,
                        final boolean hasFocus) {
                    final Component c = super.getTreeCellRendererComponent(
                            tree,
                            value,
                            sel,
                            expanded,
                            leaf,
                            row,
                            hasFocus);

                    if (c instanceof JLabel) {
                        final JLabel cl = (JLabel)c;
                        final JLabel lab = new JLabel(cl.getText(), cl.getIcon(), cl.getHorizontalAlignment());
                        lab.setBackground(cl.getBackground());
                        lab.setBorder(cl.getBorder());
                        lab.setForeground(cl.getForeground());

                        if (sel) {
                            lab.setBackground(backgroundSelectionColor);
                            lab.setOpaque(true);
                        }

                        if ((value instanceof MapService) || value.equals(jtFeatures.getModel().getRoot())) {
                            if (expanded) {
                                lab.setIcon(openIcon);
                            } else {
                                lab.setIcon(closedIcon);
                            }
                        } else if ((value instanceof WMSGetFeatureInfoDescription) || (value instanceof WMSFeature)) {
                            lab.setIcon(
                                new ImageIcon(
                                    getClass().getResource(
                                        "/de/cismet/cismap/commons/gui/layerwidget/res/layerOverlaywms.png")));
                        } else if (value instanceof DefaultFeatureServiceFeature) {
                            final DefaultFeatureServiceFeature f = (DefaultFeatureServiceFeature)value;
                            lab.setIcon(
                                f.getLayerProperties().getFeatureService().getLayerIcon(
                                    ServiceLayer.LAYER_ENABLED_VISIBLE));

                            if (f.isEditable()) {
                                final Font boldFont = lab.getFont().deriveFont(Font.BOLD);
                                lab.setFont(boldFont);
                            } else {
                                final Font plainFont = lab.getFont().deriveFont(Font.PLAIN);
                                lab.setFont(plainFont);
                            }
                        }

                        return lab;
                    } else {
                        c.setBackground(backgroundSelectionColor);
                    }

                    return c;
                }
            });
        model = new LayerFilterTreeModel(layerModel, mappingComonent);
        model.setLayerFilter((LayerFilter)layerCombobox1.getSelectedItem());
        layerCombobox1.getModel().addListDataListener(new ListDataListener() {

                @Override
                public void intervalAdded(final ListDataEvent e) {
                }

                @Override
                public void intervalRemoved(final ListDataEvent e) {
                }

                @Override
                public void contentsChanged(final ListDataEvent e) {
                    expandAll(new TreePath(model.getRoot()));
                }
            });
        jtFeatures.setModel(model);
        mappingComonent.addGetFeatureInfoListener(new GetFeatureInfoListener() {

                @Override
                public void getFeatureInfoRequest(final GetFeatureInfoEvent evt) {
                    final boolean successful = contentChanged();

                    if (successful) {
                        model.init(evt.getFeatures());
                        expandAll(new TreePath(model.getRoot()));
                    }
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  parent  DOCUMENT ME!
     */
    private void expandAll(final TreePath parent) {
        final Object lastComponent = parent.getLastPathComponent();

        jtFeatures.expandPath(parent);

        for (int i = 0; i < model.getChildCount(lastComponent); ++i) {
            final TreePath newPath = parent.pathByAddingChild(model.getChild(lastComponent, i));
            expandAll(newPath);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        popupMenu = new javax.swing.JPopupMenu();
        miZoom = new javax.swing.JMenuItem();
        miPrint = new javax.swing.JMenuItem();
        miEdit = new javax.swing.JMenuItem();
        layerCombobox1 = new LayerCombobox(layerModel, themeLayerWidget);
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtFeatures = new javax.swing.JTree();
        sbAttributes = new javax.swing.JScrollPane();
        tabAttributes = new CellSpecificRenderedTable();

        miZoom.setText(org.openide.util.NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.miZoom.text")); // NOI18N
        miZoom.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    miZoomActionPerformed(evt);
                }
            });
        popupMenu.add(miZoom);

        miPrint.setText(org.openide.util.NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.miPrint.text")); // NOI18N
        miPrint.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    miPrintActionPerformed(evt);
                }
            });
        popupMenu.add(miPrint);

        miEdit.setText(org.openide.util.NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.miEdit.text")); // NOI18N
        miEdit.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    miEditActionPerformed(evt);
                }
            });
        popupMenu.add(miEdit);

        setLayout(new java.awt.GridBagLayout());

        layerCombobox1.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    layerCombobox1ItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 5);
        add(layerCombobox1, gridBagConstraints);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 15);
        add(jLabel1, gridBagConstraints);

        jtFeatures.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {

                @Override
                public void valueChanged(final javax.swing.event.TreeSelectionEvent evt) {
                    jtFeaturesValueChanged(evt);
                }
            });
        jScrollPane1.setViewportView(jtFeatures);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.33;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jScrollPane1, gridBagConstraints);

        tabAttributes.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {},
                new String[] {}));
        sbAttributes.setViewportView(tabAttributes);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(sbAttributes, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void layerCombobox1ItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_layerCombobox1ItemStateChanged
        model.setLayerFilter((LayerFilter)evt.getItem());
        expandAll(new TreePath(model.getRoot()));
    }                                                                                 //GEN-LAST:event_layerCombobox1ItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jtFeaturesValueChanged(final javax.swing.event.TreeSelectionEvent evt) { //GEN-FIRST:event_jtFeaturesValueChanged
        final TreePath tp = jtFeatures.getSelectionPath();

        createPopupMenu();

        if (tp == null) {
            // there is nothing selected at the moment
            tabAttributes.setModel(new DefaultTableModel(0, 0));
            return;
        }

        final Object selectedComp = tp.getLastPathComponent();

        if (selectedComp instanceof DefaultFeatureServiceFeature) {
            final DefaultFeatureServiceFeature selectedFeature = (DefaultFeatureServiceFeature)selectedComp;

            currentTableModel = new AttribueTableModel(selectedFeature);

            tabAttributes.setModel(currentTableModel);
            enableAttributeTable(true);
            mappingComonent.highlightFeature(selectedFeature, 1500);
        } else if (selectedComp instanceof WMSGetFeatureInfoDescription) {
            // the default wms mechanism should be used
            enableAttributeTable(false);
            final WMSGetFeatureInfoDescription description = (WMSGetFeatureInfoDescription)selectedComp;
            final ActiveLayerEvent e = new ActiveLayerEvent();

            mappingComonent.highlightFeature(description, 1500);
            description.getLayer().setLayerQuerySelected(true);
            e.setLayer(description.getLayer());
            featureInfo.layerAdded(e);
            featureInfo.clickedOnMap(new MapClickedEvent(
                    GetFeatureInfoClickDetectionListener.FEATURE_INFO_MODE,
                    description.getpInputEvent()));
        } else {
            enableAttributeTable(true);
            tabAttributes.setModel(new DefaultTableModel(0, 0));
        }
    } //GEN-LAST:event_jtFeaturesValueChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void miZoomActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_miZoomActionPerformed
        final TreePath[] tps = jtFeatures.getSelectionPaths();
        final List<Feature> featureList = new ArrayList<Feature>();

        for (final TreePath tp : tps) {
            final Object o = tp.getLastPathComponent();

            if (o instanceof Feature) {
                final Feature fsf = (Feature)o;
                featureList.add(fsf);
            } else if (o instanceof MapService) {
                for (int i = 0; i < model.getChildCount(o); ++i) {
                    final Object featureObject = model.getChild(o, i);

                    if (featureObject instanceof Feature) {
                        featureList.add((Feature)featureObject);
                    }
                }
            }
        }

        final ZoomToFeaturesWorker worker = new ZoomToFeaturesWorker(featureList.toArray(
                    new Feature[featureList.size()]));
        worker.execute();
    } //GEN-LAST:event_miZoomActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void miEditActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_miEditActionPerformed
        final TreePath[] tps = jtFeatures.getSelectionPaths();

        for (final TreePath tp : tps) {
            final Object o = tp.getLastPathComponent();
            final List<DefaultFeatureServiceFeature> allFeatures = new ArrayList<DefaultFeatureServiceFeature>();
            boolean setEditable = true;

            if (o instanceof AbstractFeatureService) {
                for (int i = 0; i < model.getChildCount(o); ++i) {
                    final Object child = model.getChild(o, i);
                    if (child instanceof DefaultFeatureServiceFeature) {
                        allFeatures.add((DefaultFeatureServiceFeature)child);

                        if (((DefaultFeatureServiceFeature)child).isEditable()) {
                            setEditable = false;
                        }
                    }
                }
            } else if (o instanceof DefaultFeatureServiceFeature) {
                allFeatures.add((DefaultFeatureServiceFeature)o);
                setEditable = !((DefaultFeatureServiceFeature)o).isEditable();
            }

            for (final DefaultFeatureServiceFeature feature : allFeatures) {
                if ((feature != null) && !feature.isEditable() && setEditable) {
                    // start edit mode
                    final FeatureServiceFeature fsf = (FeatureServiceFeature)feature;
                    startEditMode(fsf);
                } else if ((feature != null) && feature.isEditable() && !setEditable) {
                    // stop edit mode
                    final DefaultFeatureServiceFeature f = (DefaultFeatureServiceFeature)feature;
                    stopEditMode(f);
                }
            }
        }

        createPopupMenu();
    } //GEN-LAST:event_miEditActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void miPrintActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_miPrintActionPerformed
        final TreePath tps = jtFeatures.getSelectionPath();

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
                    final Object o = tps.getLastPathComponent();
                    final DefaultFeatureServiceFeature feature = (DefaultFeatureServiceFeature)o;
                    final JRDataSource ds = new FeaturePanelAttributeSource(new AttribueTableModel(feature));
                    final Map<String, Object> map = new HashMap<String, Object>();
                    map.put("ds", ds);
                    map.put("title", feature.toString());
                    map.put(
                        "key",
                        NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.jtFeaturesValueChanged.name"));
                    map.put(
                        "value",
                        NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.jtFeaturesValueChanged.value"));
                    final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(getClass().getResourceAsStream(
                                REPORT_URL));
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
    } //GEN-LAST:event_miPrintActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  fsf  DOCUMENT ME!
     */
    private void startEditMode(final FeatureServiceFeature fsf) {
        if (fsf.getLayerProperties().getFeatureService().isEditable()) {
            if (fsf instanceof PermissionProvider) {
                if (!((PermissionProvider)fsf).hasWritePermissions()) {
                    JOptionPane.showMessageDialog(
                        this,
                        NbBundle.getMessage(
                            FeatureInfoPanel.class,
                            "FeatureInfoPanel.makeFeatureEditable.noPermissions.text"),
                        NbBundle.getMessage(
                            FeatureInfoPanel.class,
                            "FeatureInfoPanel.makeFeatureEditable.noPermissions.title"),
                        JOptionPane.ERROR_MESSAGE);

                    return;
                }
            }
            final FeatureLockingInterface locker = FeatureLockerFactory.getInstance()
                        .getLockerForFeatureService(fsf.getLayerProperties().getFeatureService());
            try {
                if (locker != null) {
                    lockMap.put(fsf, locker.lock(fsf));
                }
                fsf.setEditable(true);
                if (!changedFeatures.contains(fsf)) {
                    changedFeatures.add(fsf);
                    // ((DefaultFeatureServiceFeature)fsf).addPropertyChangeListener(model);
                }
            } catch (LockAlreadyExistsException ex) {
                JOptionPane.showMessageDialog(
                    FeatureInfoPanel.this,
                    NbBundle.getMessage(
                        FeatureInfoPanel.class,
                        "FeatureInfoPanel.miEditActionPerformed().lockexists.message",
                        fsf.getId(),
                        ex.getLockMessage()),
                    NbBundle.getMessage(
                        FeatureInfoPanel.class,
                        "FeatureInfoPanel.miEditActionPerformed().lockexists.title"),
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                LOG.error("Error while locking feature.", ex);
                JOptionPane.showMessageDialog(
                    FeatureInfoPanel.this,
                    NbBundle.getMessage(
                        FeatureInfoPanel.class,
                        "FeatureInfoPanel.miEditActionPerformed().exception.message",
                        ex.getMessage()),
                    NbBundle.getMessage(
                        FeatureInfoPanel.class,
                        "FeatureInfoPanel.miEditActionPerformed().exception.title"),
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    private void stopEditMode(final DefaultFeatureServiceFeature f) {
        try {
            final FeatureLockingInterface locker = FeatureLockerFactory.getInstance()
                        .getLockerForFeatureService(f.getLayerProperties().getFeatureService());
            final AttributeTableRuleSet tableRuleSet = f.getLayerProperties().getAttributeTableRuleSet();
            if (tableRuleSet != null) {
                tableRuleSet.beforeSave(f);
            }
            // stop the cell renerer, if it is active
            if ((tabAttributes.getEditingColumn() != -1) && (tabAttributes.getEditingRow() != -1)) {
                tabAttributes.getCellEditor(tabAttributes.getEditingRow(),
                    tabAttributes.getEditingColumn()).stopCellEditing();
            }
            f.saveChanges();
            f.setEditable(false);

            if (locker != null) {
                final Object lockingObject = lockMap.get(f);

                if (lockingObject != null) {
                    locker.unlock(lockingObject);
                    lockMap.remove(f);
                }
            }
            changedFeatures.remove(f);
        } catch (Exception e) {
            LOG.error("Error while saving feature", e);
        }
    }

    /**
     * Checks, if the FeatureInfoPanel contains unsaved changes and let the user decide, whether the content should be
     * saved or not.
     *
     * @return  false, if the close operation should be aborted
     */
    private boolean contentChanged() {
        if (!lockMap.isEmpty()) {
            final int ans = JOptionPane.showConfirmDialog(
                    FeatureInfoPanel.this,
                    NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.contentChanged().text"),
                    NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.contentChanged().title"),
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if (ans == JOptionPane.YES_OPTION) {
                saveAllChanges();
            } else if (ans == JOptionPane.NO_OPTION) {
                unlockAll();
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * Should be invoked, before the FeatureInfoPanel is closed. It checks, if there are unsaved changes.
     *
     * @return  false, if the close operation should be aborted
     */
    public boolean dispose() {
        final boolean successful = contentChanged();

        if (successful) {
            model.init(new ArrayList<Feature>());
        }

        return successful;
    }

    /**
     * Save all changes.
     */
    private void saveAllChanges() {
        for (final Feature f : lockMap.keySet()) {
            stopEditMode((DefaultFeatureServiceFeature)f);
        }

        lockMap.clear();
    }

    /**
     * unlocks all locked objects.
     */
    private void unlockAll() {
        boolean allLocksRemoved = true;

        for (final Feature f : lockMap.keySet()) {
            final FeatureLockingInterface locker = FeatureLockerFactory.getInstance()
                        .getLockerForFeatureService(((DefaultFeatureServiceFeature)f).getLayerProperties()
                            .getFeatureService());

            if (locker != null) {
                try {
                    locker.unlock(lockMap.get(f));
                } catch (Exception e) {
                    LOG.error("Locking object can't be removed.", e);
                    allLocksRemoved = false;
                }
            } else {
                LOG.error("No suitable locker object found");
                allLocksRemoved = false;
            }
        }

        if (!allLocksRemoved) {
            JOptionPane.showMessageDialog(
                FeatureInfoPanel.this,
                NbBundle.getMessage(
                    FeatureInfoPanel.class,
                    "FeatureInfoPanel.unlockAll().message"),
                NbBundle.getMessage(
                    FeatureInfoPanel.class,
                    "FeatureInfoPanel.unlockAll().title"),
                JOptionPane.ERROR_MESSAGE);
        }

        lockMap.clear();
    }

    /**
     * Creates the context menu of the FeatureInfoPanel.
     */
    private void createPopupMenu() {
        final TreePath tp = jtFeatures.getSelectionPath();
        if (tp == null) {
            // no element selected
            return;
        }
        final Object c = tp.getLastPathComponent();

        popupMenu.removeAll();

        popupMenu.add(miZoom);
        popupMenu.add(miPrint);

        if (c instanceof DefaultFeatureServiceFeature) {
            if ((((DefaultFeatureServiceFeature)c).getLayerProperties().getFeatureService() != null)
                        && ((DefaultFeatureServiceFeature)c).getLayerProperties().getFeatureService().isEditable()) {
                popupMenu.add(miEdit);
                if (((DefaultFeatureServiceFeature)c).isEditable()) {
                    miEdit.setText(NbBundle.getMessage(
                            FeatureInfoPanel.class,
                            "FeatureInfoPanel.miEdit.text.editable"));
                } else {
                    miEdit.setText(NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.miEdit.text"));
                }
            }
        } else if (c instanceof AbstractFeatureService) {
            final AbstractFeatureService service = (AbstractFeatureService)c;

            if (service.isEditable()) {
                boolean setEditable = true;
                for (int i = 0; i < model.getChildCount(service); ++i) {
                    final Object child = model.getChild(service, i);

                    if (child instanceof DefaultFeatureServiceFeature) {
                        if (((DefaultFeatureServiceFeature)child).isEditable()) {
                            setEditable = false;
                        }
                    }
                }

                popupMenu.add(miEdit);
                if (setEditable) {
                    miEdit.setText(NbBundle.getMessage(
                            FeatureInfoPanel.class,
                            "FeatureInfoPanel.miEdit.text.editable.all"));
                } else {
                    miEdit.setText(NbBundle.getMessage(
                            FeatureInfoPanel.class,
                            "FeatureInfoPanel.miEdit.text.noteditable.all"));
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  enable  DOCUMENT ME!
     */
    private void enableAttributeTable(final boolean enable) {
        sbAttributes.getViewport().remove(tabAttributes);
        sbAttributes.getViewport().remove(featureInfo);

        if (enable) {
            sbAttributes.getViewport().add(tabAttributes);
        } else {
            sbAttributes.getViewport().add(featureInfo);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureServiceFeature getSelectedFeature() {
        final AttribueTableModel tabModel = (AttribueTableModel)tabAttributes.getModel();

        if (tabModel != null) {
            return tabModel.getFeature();
        }

        return null;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class LayerFilterTreeModel implements TreeModel {

        //~ Instance fields ----------------------------------------------------

        List<Feature> lastFeatures;
        private final MappingComponent mappingComponent;
        private final ActiveLayerModel layerModel;
        private LayerFilter filter;
        private final String root = "Features";
        private final Map<MapService, List<Feature>> data = new HashMap<MapService, List<Feature>>();
        private final List<MapService> orderedDataKeys = new ArrayList<MapService>();
        private final List<TreeModelListener> listener = new ArrayList<TreeModelListener>();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LayerFilterTreeModel object.
         *
         * @param  layerModel        DOCUMENT ME!
         * @param  mappingComponent  DOCUMENT ME!
         */
        public LayerFilterTreeModel(final ActiveLayerModel layerModel, final MappingComponent mappingComponent) {
            this.layerModel = layerModel;
            this.mappingComponent = mappingComponent;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  filter  DOCUMENT ME!
         */
        public void setLayerFilter(final LayerFilter filter) {
            this.filter = filter;

            if (lastFeatures != null) {
                init(lastFeatures);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  features  DOCUMENT ME!
         */
        private void init(final List<Feature> features) {
            final TreeMap<Integer, MapService> treeMap = layerModel.getMapServices();
            final List<MapService> allowedMapServices = new ArrayList<MapService>();
            final List<AbstractFeatureService> allowedFeatureServices = new ArrayList<AbstractFeatureService>();
            lastFeatures = features;

            data.clear();
            orderedDataKeys.clear();

            for (final Integer serviceKey : treeMap.keySet()) {
                final MapService service = treeMap.get(serviceKey);

                if (filter.isLayerAllowed(service)) {
                    if (service instanceof AbstractFeatureService) {
                        allowedFeatureServices.add((AbstractFeatureService)service);
                    } else {
                        allowedMapServices.add(service);
                    }
                }
            }

            if ((features.size() == 1) && !(features.get(0) instanceof WMSFeature)) {
                final Feature f = features.get(0);
                final AbstractFeatureService service = ((FeatureServiceFeature)f).getLayerProperties()
                            .getFeatureService();
                if ((f instanceof FeatureServiceFeature) && !allowedMapServices.contains(service)) {
                    if (filter.isLayerAllowed(service)) {
                        allowedFeatureServices.add(service);
                    }
                }
            }

            for (final Feature feature : features) {
                MapService service = null;

                if (feature instanceof WMSGetFeatureInfoDescription) {
                    final WMSGetFeatureInfoDescription gfid = (WMSGetFeatureInfoDescription)feature;
                    service = getMapServiceOfWMSServiceLayer(allowedMapServices, gfid.getService());
                } else if (feature instanceof WMSFeature) {
                    final WMSFeature wmsFeature = (WMSFeature)feature;
                    service = getMapServiceOfWMSServiceLayer(allowedMapServices, wmsFeature.getWMSServiceLayer());
                } else if (feature instanceof FeatureServiceFeature) {
                    service = getFeatureServiceOfFeature(allowedFeatureServices, (FeatureServiceFeature)feature);
                }

                if (service != null) {
                    List<Feature> featureList = data.get(service);

                    if (featureList == null) {
                        featureList = new ArrayList<Feature>();
                        data.put(service, featureList);
                        orderedDataKeys.add(service);
                    }

                    featureList.add(feature);
                }
            }

            fireTreeStructureChanged();
        }

        /**
         * DOCUMENT ME!
         *
         * @param   serviceList  DOCUMENT ME!
         * @param   feature      DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private AbstractFeatureService getFeatureServiceOfFeature(final List<AbstractFeatureService> serviceList,
                final FeatureServiceFeature feature) {
            for (final AbstractFeatureService service : serviceList) {
                if (service.getLayerProperties() == feature.getLayerProperties()) {
                    return service;
                }
            }

            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   serviceList     DOCUMENT ME!
         * @param   featureService  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private MapService getMapServiceOfWMSServiceLayer(final List<MapService> serviceList,
                final WMSServiceLayer featureService) {
            for (final MapService service : serviceList) {
                if (service == featureService) {
                    return service;
                }
            }

            return null;
        }

        @Override
        public Object getRoot() {
            return root;
        }

        @Override
        public Object getChild(final Object parent, final int index) {
            if (parent == root) {
                // a MapService instance will be returned
                return orderedDataKeys.get(index);
            } else if (parent instanceof MapService) {
                // a feature instance will be returned
                return data.get((MapService)parent).get(index);
            } else {
                // should never happen
                return null;
            }
        }

        @Override
        public int getChildCount(final Object parent) {
            if (parent == root) {
                return orderedDataKeys.size();
            } else if (parent instanceof MapService) {
                return data.get((MapService)parent).size();
            } else {
                return 0;
            }
        }

        @Override
        public boolean isLeaf(final Object node) {
            if (node == root) {
                return orderedDataKeys.isEmpty();
            } else if (node instanceof MapService) {
                return data.get((MapService)node).isEmpty();
            } else {
                return true;
            }
        }

        @Override
        public void valueForPathChanged(final TreePath path, final Object newValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getIndexOfChild(final Object parent, final Object child) {
            if (parent == root) {
                // a MapService instance will be returned
                return orderedDataKeys.indexOf(child);
            } else if (parent instanceof MapService) {
                // a feature instance will be returned
                return data.get((MapService)parent).indexOf(child);
            } else {
                // should never happen
                LOG.error("parent is of type " + parent.getClass().getName() + ". This should never happen");
                return -1;
            }
        }

        @Override
        public void addTreeModelListener(final TreeModelListener l) {
            listener.add(l);
        }

        @Override
        public void removeTreeModelListener(final TreeModelListener l) {
            listener.remove(l);
        }

        /**
         * DOCUMENT ME!
         */
        private void fireTreeStructureChanged() {
            for (final TreeModelListener l : listener) {
                l.treeStructureChanged(new TreeModelEvent(this, new Object[] { root }));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class AttribueTableModel implements TableModel {

        //~ Instance fields ----------------------------------------------------

        private final DefaultFeatureServiceFeature feature;
        private final AttributeTableRuleSet tableRuleSet;
        private String[] attributeAlias;
        private String[] attributeNames;
        private Map<String, FeatureServiceAttribute> featureServiceAttributes;
        private String[] additionalAttributes = new String[0];
        private List<String> orderedFeatureServiceAttributes;
        private final List<TableModelListener> listener = new ArrayList<TableModelListener>();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AttribueTableModel object.
         *
         * @param  feature  DOCUMENT ME!
         */
        public AttribueTableModel(final DefaultFeatureServiceFeature feature) {
            this.feature = feature;
            tableRuleSet = feature.getLayerProperties().getAttributeTableRuleSet();
            initModel();
            initTable();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initModel() {
            if (feature instanceof WMSFeature) {
                final HashMap<String, Object> props = feature.getProperties();

                orderedFeatureServiceAttributes = new ArrayList<String>(props.keySet());
                featureServiceAttributes = new HashMap<String, FeatureServiceAttribute>();

                for (final String key : props.keySet()) {
                    final FeatureServiceAttribute attr = new FeatureServiceAttribute(key, "String", false);
                    featureServiceAttributes.put(key, attr);
                }
            } else {
                final AbstractFeatureService service = feature.getLayerProperties().getFeatureService();
                orderedFeatureServiceAttributes = service.getOrderedFeatureServiceAttributes();
                featureServiceAttributes = service.getFeatureServiceAttributes();
            }
            fillHeaderArrays();
        }

        /**
         * DOCUMENT ME!
         */
        private void initTable() {
            ((CellSpecificRenderedTable)tabAttributes).removeAllCellEditors();

            if (tableRuleSet != null) {
                for (int i = 0; i < getRowCount(); ++i) {
                    String columnName;
                    if (i >= attributeNames.length) {
                        columnName = additionalAttributes[i - attributeNames.length];
                    } else {
                        columnName = attributeNames[i];
                    }

                    final TableCellEditor editor = tableRuleSet.getCellEditor(columnName);
                    final TableCellRenderer renderer = tableRuleSet.getCellRenderer(columnName);

                    if (editor != null) {
                        ((CellSpecificRenderedTable)tabAttributes).addCellEditor(1, i, editor);
                    }

                    if (renderer != null) {
                        ((CellSpecificRenderedTable)tabAttributes).addCellRenderer(1, i, renderer);
                    }
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public FeatureServiceFeature getFeature() {
            return feature;
        }

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

        @Override
        public boolean isCellEditable(final int row, final int column) {
            if (column == 1) {
                if (row < attributeNames.length) {
                    if (tableRuleSet != null) {
                        return feature.isEditable() && tableRuleSet.isColumnEditable(attributeNames[row])
                                    && feature.getLayerProperties().getFeatureService().isEditable();
                    } else {
                        return feature.isEditable() && feature.getLayerProperties().getFeatureService().isEditable();
                    }
                }
            }

            return false;
        }

        @Override
        public int getRowCount() {
            return attributeNames.length + additionalAttributes.length;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(final int columnIndex) {
            if (columnIndex == 0) {
                return NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.jtFeaturesValueChanged.name");
            } else {
                return NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.jtFeaturesValueChanged.value");
            }
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
//            if (columnIndex < attributeAlias.length) {
//                final String key = attributeNames[columnIndex];
//                final FeatureServiceAttribute attr = featureServiceAttributes.get(key);
//
//                return FeatureTools.getClass(attr);
//            } else {
//                return tableRuleSet.getAdditionalFieldClass(columnIndex - attributeAlias.length);
//            }
//            return Double.class;
            return Object.class;
//            return String.class;
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            Object value;

            if (rowIndex < attributeNames.length) {
                if (columnIndex == 0) {
                    value = attributeAlias[rowIndex];
                } else {
                    value = feature.getProperty(attributeNames[rowIndex]);
                }
            } else {
                if (columnIndex == 0) {
                    value = additionalAttributes[rowIndex - attributeNames.length];
                } else {
                    value = tableRuleSet.getAdditionalFieldValue(rowIndex, feature);
                }
            }

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
            final String attrName = attributeNames[rowIndex];
            Object newObject = aValue;

            if (tableRuleSet != null) {
                newObject = tableRuleSet.afterEdit(attrName, -1, feature.getProperty(attrName), aValue);
            }
            feature.setProperty(attrName, newObject);
            if (!changedFeatures.contains(feature)) {
                changedFeatures.add(feature);
            }
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
        }
    }
}
