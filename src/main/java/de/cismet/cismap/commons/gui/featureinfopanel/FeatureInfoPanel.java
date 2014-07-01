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

import org.apache.log4j.Logger;

import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;

import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPopupMenu;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.WMSFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.attributetable.DefaultAttributeTableRuleSet;
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
import de.cismet.cismap.commons.tools.FeatureTools;

import de.cismet.tools.gui.DefaultPopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FeatureInfoPanel extends javax.swing.JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FeatureInfoPanel.class);

    //~ Instance fields --------------------------------------------------------

    private ActiveLayerModel layerModel;
    private MappingComponent mappingComonent;
    private ThemeLayerWidget themeLayerWidget;
    private LayerFilterTreeModel model;
    private FeatureInfoWidget featureInfo;
    private DefaultPopupMenuListener popupMenuListener;
    private List<FeatureServiceFeature> changedFeatures = new ArrayList<FeatureServiceFeature>();
    private AttribueTableModel currentTableModel;

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
                    model.init(evt.getFeatures());
                    expandAll(new TreePath(model.getRoot()));
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
        tabAttributes = new org.jdesktop.swingx.JXTable();

        miZoom.setText(org.openide.util.NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.miZoom.text")); // NOI18N
        miZoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miZoomActionPerformed(evt);
            }
        });
        popupMenu.add(miZoom);

        miPrint.setText(org.openide.util.NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.miPrint.text")); // NOI18N
        miPrint.setEnabled(false);
        popupMenu.add(miPrint);

        miEdit.setText(org.openide.util.NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.miEdit.text")); // NOI18N
        miEdit.setEnabled(false);
        miEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miEditActionPerformed(evt);
            }
        });
        popupMenu.add(miEdit);

        setLayout(new java.awt.GridBagLayout());

        layerCombobox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
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
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
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
            new Object [][] {

            },
            new String [] {

            }
        ));
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
    }// </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void layerCombobox1ItemStateChanged(final java.awt.event.ItemEvent evt) {//GEN-FIRST:event_layerCombobox1ItemStateChanged
        model.setLayerFilter((LayerFilter)evt.getItem());
        expandAll(new TreePath(model.getRoot()));
    } 										//GEN-LAST:event_layerCombobox1ItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jtFeaturesValueChanged(final javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jtFeaturesValueChanged
        final TreePath tp = jtFeatures.getSelectionPath();

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
    }//GEN-LAST:event_jtFeaturesValueChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void miZoomActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miZoomActionPerformed
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
    }//GEN-LAST:event_miZoomActionPerformed

    private void miEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miEditActionPerformed
        final TreePath[] tps = jtFeatures.getSelectionPaths();

        for (final TreePath tp : tps) {
            final Object o = tp.getLastPathComponent();

            if (o instanceof Feature) {
                final Feature feature = (Feature)o;
                if ((feature != null) && !feature.isEditable()) {
                    if (feature instanceof FeatureServiceFeature) {
                        FeatureServiceFeature fsf = (FeatureServiceFeature)feature;
                        if (fsf.getLayerProperties().getFeatureService().isEditable()) {
                            feature.setEditable(true);
                            if (!changedFeatures.contains(fsf)) {
                                changedFeatures.add(fsf);
//                                ((DefaultFeatureServiceFeature)fsf).addPropertyChangeListener(model);
                            }
                        }
                    }
                }
                
            }
        }
    }//GEN-LAST:event_miEditActionPerformed

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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class LayerFilterTreeModel implements TreeModel {

        //~ Instance fields ----------------------------------------------------

        List<Feature> lastFeatures;
        private MappingComponent mappingComponent;
        private ActiveLayerModel layerModel;
        private LayerFilter filter;
        private String root = "Features";
        private Map<MapService, List<Feature>> data = new HashMap<MapService, List<Feature>>();
        private List<MapService> orderedDataKeys = new ArrayList<MapService>();
        private List<TreeModelListener> listener = new ArrayList<TreeModelListener>();

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
    
    
    class AttribueTableModel implements TableModel {
        private DefaultFeatureServiceFeature feature;
        private DefaultAttributeTableRuleSet tableRuleSet;
        private String[] attributeAlias;
        private String[] attributeNames;
        private Map<String, FeatureServiceAttribute> featureServiceAttributes;
        private String[] additionalAttributes = new String[0];
        private List<String> orderedFeatureServiceAttributes;
        private List<TableModelListener> listener = new ArrayList<TableModelListener>();
        

        public AttribueTableModel(DefaultFeatureServiceFeature feature) {
            this.feature = feature;
            tableRuleSet = AttributeTable.getTableRuleSetForFeatureService(feature.getLayerProperties().getFeatureService());
            initTable();
        }

        
        private void initTable() {
            AbstractFeatureService service = feature.getLayerProperties().getFeatureService();
            orderedFeatureServiceAttributes = service.getOrderedFeatureServiceAttributes();
            featureServiceAttributes = service.getFeatureServiceAttributes();
            fillHeaderArrays();
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
        
        @Override
        public boolean isCellEditable(int row, int column) {
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
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.jtFeaturesValueChanged.name");
            } else {
                return NbBundle.getMessage(FeatureInfoPanel.class, "FeatureInfoPanel.jtFeaturesValueChanged.value");
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
//            if (columnIndex < attributeAlias.length) {
//                final String key = attributeNames[columnIndex];
//                final FeatureServiceAttribute attr = featureServiceAttributes.get(key);
//                
//                return FeatureTools.getClass(attr);
//            } else {
//                return tableRuleSet.getAdditionalFieldClass(columnIndex - attributeAlias.length);
//            }
            return String.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < attributeNames.length) {
                if (columnIndex == 0) {
                    return attributeAlias[rowIndex];
                } else {
                    return feature.getProperty(attributeNames[rowIndex]);
                }
            } else {
                return additionalAttributes[rowIndex - attributeNames.length];
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
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
