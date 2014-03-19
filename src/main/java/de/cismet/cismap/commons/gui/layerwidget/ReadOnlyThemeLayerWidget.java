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
package de.cismet.cismap.commons.gui.layerwidget;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.MapService;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ReadOnlyThemeLayerWidget extends javax.swing.JPanel { // implements

    //~ Instance fields --------------------------------------------------------

    private Logger log = Logger.getLogger(ThemeLayerWidget.class);
    private ActiveLayerModel layerModel;
    private TreeModel model;
    private Map<Object, Boolean> serviceStateMap = new HashMap<Object, Boolean>();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree tree;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form ThemeLayerWidget.
     */
    public ReadOnlyThemeLayerWidget() {
        initComponents();
        tree.setCellRenderer(new CheckBoxNodeRenderer());
        tree.setEditable(false);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  mappingModel  DOCUMENT ME!
     */
    public void setMappingModel(final ActiveLayerModel mappingModel) {
        setMappingModel(mappingModel, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mappingModel  DOCUMENT ME!
     * @param  classes       DOCUMENT ME!
     */
    public void setMappingModel(final ActiveLayerModel mappingModel, final Class[] classes) {
        layerModel = mappingModel;
        model = new ActiveLayerModelWrapperWithoutProgress(layerModel);

        if (classes != null) {
            model = new FilterTreeModelWrapper(model, classes);
        }

        tree.setModel(model);

        tree.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (!e.isPopupTrigger() && (e.getClickCount() == 1)) {
                        final int x = e.getX();
                        final int y = e.getY();

                        // is click over the combobox?
                        final TreePath tp = tree.getPathForLocation(x, y);

                        if (tp != null) {
                            final int pathCount = tp.getPathCount() - 1;
                            final int minX = pathCount * 20;
                            final int maxX = minX + 15;

                            if ((x >= minX) && (x <= maxX)) {
                                // click is over the checkbox
                                changeState(tp.getLastPathComponent(), !getState(tp.getLastPathComponent()));
                                tree.repaint();
                            }
                        }
                    }
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<Object> getSelectedServices() {
        final List<Object> result = new ArrayList<Object>();

        for (final Object key : serviceStateMap.keySet()) {
            final Boolean selected = serviceStateMap.get(key);
            if (selected.booleanValue()) {
                result.add(key);
            }
        }

        return result;
    }

    /**
     * changes the visibility of the given object.
     *
     * @param  objectToChange  either the root layer, a LayerCollection or a ServiceLayer
     * @param  newState        DOCUMENT ME!
     */
    private void changeState(final Object objectToChange, final boolean newState) {
        if (objectToChange.equals(model.getRoot())) {
            for (int i = 0; i < model.getChildCount(model.getRoot()); ++i) {
                changeState(model.getChild(model.getRoot(), i), newState);
            }
        } else if (objectToChange instanceof LayerCollection) {
            final LayerCollection lc = (LayerCollection)objectToChange;

            for (int i = 0; i < lc.size(); ++i) {
                changeState(lc.get(i), newState);
            }
        } else if (objectToChange instanceof ServiceLayer) {
            final Boolean currentState = serviceStateMap.get(objectToChange);

            if (currentState != null) {
                serviceStateMap.put(objectToChange, newState);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean getState(final Object value) {
        if (value.equals(model.getRoot())) {
            boolean isSelected = true;

            for (int i = 0; i < model.getChildCount(model.getRoot()); ++i) {
                if (!getState(model.getChild(model.getRoot(), i))) {
                    isSelected = false;
                }
            }

            return isSelected;
        } else if (value instanceof LayerCollection) {
            boolean isSelected = true;

            final LayerCollection lc = (LayerCollection)value;

            for (int i = 0; i < lc.size(); ++i) {
                if (!getState(lc.get(i))) {
                    isSelected = false;
                }
            }

            return isSelected;
        } else if (value instanceof ServiceLayer) {
            Boolean state = serviceStateMap.get(value);

            if (state == null) {
                state = Boolean.FALSE;
                serviceStateMap.put(value, state);
            }

            return state.booleanValue();
        }

        return false;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        final java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();

        setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setViewportView(tree);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class CheckBoxNodeRenderer extends ActiveLayerTreeCellRenderer {

        //~ Instance fields ----------------------------------------------------

        protected Color selectionBorderColor;
        protected Color selectionForeground;
        protected Color selectionBackground;
        protected Color textForeground;
        protected Color textBackground;
        protected Boolean drawsFocusBorderAroundIcon;
        protected Font fontValue;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CheckBoxNodeRenderer object.
         */
        public CheckBoxNodeRenderer() {
            fontValue = UIManager.getFont("Tree.font");
            drawsFocusBorderAroundIcon = (Boolean)UIManager.get("Tree.drawsFocusBorderAroundIcon");
            selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
            selectionForeground = UIManager.getColor("Tree.selectionForeground");
            selectionBackground = UIManager.getColor("Tree.selectionBackground");
            textForeground = UIManager.getColor("Tree.textForeground");
            textBackground = UIManager.getColor("Tree.textBackground");
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getTreeCellRendererComponent(final JTree tree,
                final Object value,
                final boolean selected,
                final boolean expanded,
                final boolean leaf,
                final int row,
                final boolean hasFocus) {
            final JLabel lab;
            synchronized (ReadOnlyThemeLayerWidget.this.getTreeLock()) {
                final Component ret = super.getTreeCellRendererComponent(
                        tree,
                        value,
                        selected,
                        expanded,
                        leaf,
                        row,
                        hasFocus);
                final JLabel retLab = (JLabel)ret;
                lab = new JLabel(retLab.getText(), retLab.getIcon(), retLab.getHorizontalAlignment());
            }
            final JPanel pan = new JPanel();
            final JCheckBox leafRenderer = new JCheckBox();
            pan.setLayout(new GridBagLayout());

            if (fontValue != null) {
                leafRenderer.setFont(fontValue);
            }
            leafRenderer.setFocusPainted((drawsFocusBorderAroundIcon != null)
                        && (drawsFocusBorderAroundIcon.booleanValue()));

            leafRenderer.setEnabled(tree.isEnabled());

            if (selected) {
                leafRenderer.setForeground(selectionForeground);
                leafRenderer.setBackground(selectionBackground);
                pan.setForeground(selectionForeground);
                pan.setBackground(selectionBackground);
            } else {
                leafRenderer.setForeground(textForeground);
                leafRenderer.setBackground(textBackground);
                pan.setForeground(textForeground);
                pan.setBackground(textBackground);
            }

            if ((value instanceof ShapeFileFeatureService) && ((ShapeFileFeatureService)value).isFileNotFound()) {
                lab.setForeground(Color.GRAY);
            }

            leafRenderer.setSelected(getState(value));

            pan.add(leafRenderer);
            pan.add(lab);
            pan.doLayout();
            pan.repaint();
            return pan;
        }
    }

    /**
     * Wraps an TreeModel and shows only child nodes with the type of the supported classes.
     *
     * @version  $Revision$, $Date$
     */
    private class FilterTreeModelWrapper implements TreeModel {

        //~ Instance fields ----------------------------------------------------

        private TreeModel model;
        private Class[] supportedClasses;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FilterTreeModelWrapper object.
         *
         * @param  model             DOCUMENT ME!
         * @param  supportedClasses  DOCUMENT ME!
         */
        public FilterTreeModelWrapper(final TreeModel model, final Class[] supportedClasses) {
            this.model = model;
            this.supportedClasses = supportedClasses;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public Object getRoot() {
            return model.getRoot();
        }

        @Override
        public Object getChild(final Object parent, final int index) {
            int counter = 0;

            for (int i = 0; i < model.getChildCount(parent); ++i) {
                final Object child = model.getChild(parent, i);
                if (isInstanceOfSupportedClass(child)) {
                    if (counter == index) {
                        return child;
                    } else {
                        ++counter;
                    }
                }
            }

            // If this code is reached, either this method is wrong or the getChildCount(Object) method
            log.error("No child found. This should never happen.");
            return null;
        }

        @Override
        public int getChildCount(final Object parent) {
            int counter = 0;

            for (int i = 0; i < model.getChildCount(parent); ++i) {
                if (isInstanceOfSupportedClass(model.getChild(parent, i))) {
                    ++counter;
                }
            }

            return counter;
        }

        @Override
        public boolean isLeaf(final Object node) {
            return model.isLeaf(node);
        }

        @Override
        public void valueForPathChanged(final TreePath path, final Object newValue) {
            model.valueForPathChanged(path, newValue);
        }

        @Override
        public int getIndexOfChild(final Object parent, final Object child) {
            int counter = 0;

            if ((parent == null) || (child == null)) {
                return -1;
            }

            for (int i = 0; i < model.getChildCount(parent); ++i) {
                final Object originChild = model.getChild(parent, i);
                if (isInstanceOfSupportedClass(originChild)) {
                    if (child == originChild) {
                        return counter;
                    } else {
                        ++counter;
                    }
                }
            }

            return -1;
        }

        @Override
        public void addTreeModelListener(final TreeModelListener l) {
            model.addTreeModelListener(l);
        }

        @Override
        public void removeTreeModelListener(final TreeModelListener l) {
            model.removeTreeModelListener(l);
        }

        /**
         * DOCUMENT ME!
         *
         * @param   o  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private boolean isInstanceOfSupportedClass(final Object o) {
            for (final Class c : supportedClasses) {
                if (c.isAssignableFrom(o.getClass())) {
                    return true;
                }
            }

            return false;
        }
    }
}
