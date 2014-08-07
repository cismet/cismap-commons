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

import org.openide.util.NbBundle;

import java.awt.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.rasterservice.MapService;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class LayerCombobox extends JComboBox {

    //~ Instance fields --------------------------------------------------------

    private LayerComboboxModel model;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LayerCombobox object.
     */
    public LayerCombobox() {
    }

    /**
     * Creates a new LayerCombobox object.
     *
     * @param  layerModel  DOCUMENT ME!
     */
    public LayerCombobox(final ActiveLayerModel layerModel) {
        this(layerModel, null);
    }

    /**
     * Creates a new LayerCombobox object.
     *
     * @param  layerModel        DOCUMENT ME!
     * @param  themeLayerWidget  DOCUMENT ME!
     */
    public LayerCombobox(final ActiveLayerModel layerModel, final ThemeLayerWidget themeLayerWidget) {
        super();
        model = new LayerComboboxModel(layerModel, themeLayerWidget);
        super.setModel(model);
        super.setRenderer(new CustomRenderer());
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CustomRenderer extends DefaultListCellRenderer {

        //~ Instance fields ----------------------------------------------------

        private int indent = UIManager.getInt("Tree.leftChildIndent");

        //~ Methods ------------------------------------------------------------

        @Override
        public Component getListCellRendererComponent(final JList<?> list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            final Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (c instanceof JLabel) {
                final JLabel lbl = (JLabel)c;

                if (value instanceof DefaultLayerFilter) {
                    final DefaultLayerFilter lf = (DefaultLayerFilter)value;

                    if (lf.getIcon() != null) {
                        lbl.setIcon(lf.getIcon());
                    }
                }

                if (value instanceof SingleLayerFilter) {
                    final SingleLayerFilter lf = (SingleLayerFilter)value;

                    if (lf.getDepth() > 0) {
                        lbl.setBorder(new EmptyBorder(0, lf.getDepth() * indent, 0, 0));
                    }
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
    private class LayerComboboxModel extends DefaultComboBoxModel {

        //~ Instance fields ----------------------------------------------------

        private ActiveLayerModel layerModel;
        private ThemeLayerWidget themeLayerWidget;
        private List<LayerFilter> filter = new ArrayList<LayerFilter>();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LayerComboboxModel object.
         *
         * @param  layerModel        DOCUMENT ME!
         * @param  themeLayerWidget  DOCUMENT ME!
         */
        public LayerComboboxModel(final ActiveLayerModel layerModel, final ThemeLayerWidget themeLayerWidget) {
            this.layerModel = layerModel;
            this.themeLayerWidget = themeLayerWidget;

            layerModel.addTreeModelListener(new TreeModelListener() {

                    @Override
                    public void treeNodesChanged(final TreeModelEvent e) {
                        // This means, that the layer was disabled, the visibility was changed, the name was changed or
                        // the progress was changed
                    }

                    @Override
                    public void treeNodesInserted(final TreeModelEvent e) {
                        initModel();
                    }

                    @Override
                    public void treeNodesRemoved(final TreeModelEvent e) {
                        initModel();
                    }

                    @Override
                    public void treeStructureChanged(final TreeModelEvent e) {
                        initModel();
                    }
                });

            initModel();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void initModel() {
            final List<LayerFilter> entryList = new ArrayList<LayerFilter>();

            final Object selectedObject = getSelectedItem();

            if (filter.isEmpty()) {
                if (themeLayerWidget != null) {
                    filter.add(new SelectedLayerFilter(themeLayerWidget));
                }
                filter.add(new TopMostLayerFilter(layerModel));
                filter.add(new VisibleLayersFilter(layerModel));
                filter.add(new AllLayersFilter());
            }

            for (final LayerFilter lf : filter) {
                entryList.add(lf);
            }

            final TreeMap<Integer, Object> map = layerModel.getMapServicesAndCollections();

            final Integer[] keys = new Integer[map.keySet().size()];
            int i = 0;

            for (final Integer key : map.keySet()) {
                keys[i++] = key;
            }
            Arrays.sort(keys, Collections.reverseOrder());

            for (final Integer key : keys) {
                final Object layer = map.get(key);

                if (layer instanceof Collection) {
                    entryList.addAll(getLayersFromCollection((Collection)layer, 1));
                } else {
                    entryList.add(new SingleLayerFilter(layer, 0));
                }
            }

            this.removeAllElements();
            for (final Object entry : entryList) {
                this.addElement(entry);
            }

            if (entryList.contains(selectedObject)) {
                setSelectedItem(selectedObject);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   layer  DOCUMENT ME!
         * @param   depth  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private List<LayerFilter> getLayersFromCollection(final Collection layer, final int depth) {
            final List<LayerFilter> entryList = new ArrayList<LayerFilter>();

            entryList.add(new SingleLayerFilter(layer, depth - 1));

            for (final Object subLayer : layer) {
                if (subLayer instanceof Collection) {
                    entryList.addAll(getLayersFromCollection((Collection)subLayer, depth + 1));
                } else {
                    entryList.add(new SingleLayerFilter(subLayer, depth));
                }
            }

            return entryList;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private abstract class DefaultLayerFilter implements LayerFilter {

        //~ Instance fields ----------------------------------------------------

        private String name;
        private Icon icon;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DefaultLayerFilter object.
         *
         * @param  name  DOCUMENT ME!
         */
        public DefaultLayerFilter(final String name) {
            this.name = name;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  icon  DOCUMENT ME!
         */
        public void setIcon(final Icon icon) {
            this.icon = icon;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Icon getIcon() {
            return icon;
        }

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
    private class TopMostLayerFilter extends DefaultLayerFilter {

        //~ Instance fields ----------------------------------------------------

        private MapService topMostLayer;
        private ActiveLayerModel model;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new TopMostLayerFilter object.
         *
         * @param  model  DOCUMENT ME!
         */
        public TopMostLayerFilter(final ActiveLayerModel model) {
            super(NbBundle.getMessage(TopMostLayerFilter.class, "LayerCombobox.TopMostLayerFilter"));
            this.model = model;
            setTopMostLayer();

            model.addTreeModelListener(new TreeModelListener() {

                    @Override
                    public void treeNodesChanged(final TreeModelEvent e) {
                        setTopMostLayer();
                    }

                    @Override
                    public void treeNodesInserted(final TreeModelEvent e) {
                        setTopMostLayer();
                    }

                    @Override
                    public void treeNodesRemoved(final TreeModelEvent e) {
                        setTopMostLayer();
                    }

                    @Override
                    public void treeStructureChanged(final TreeModelEvent e) {
                        setTopMostLayer();
                    }
                });
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void setTopMostLayer() {
            final TreeMap<Integer, MapService> map = model.getMapServices();

            if (!map.keySet().isEmpty()) {
                final Integer[] keys = new Integer[map.keySet().size()];
                int i = 0;

                for (final Integer key : map.keySet()) {
                    keys[i++] = key;
                }

                Arrays.sort(keys, Collections.reverseOrder());

                topMostLayer = map.get(keys[0]);
            } else {
                topMostLayer = null;
            }
        }

        @Override
        public boolean isLayerAllowed(final MapService layer) {
            if (topMostLayer != null) {
                return layer.equals(topMostLayer);
            } else {
                return false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class VisibleLayersFilter extends DefaultLayerFilter {

        //~ Instance fields ----------------------------------------------------

        private List<MapService> visibleLayer = new ArrayList<MapService>();
        private ActiveLayerModel model;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new VisibleLayersFilter object.
         *
         * @param  model  DOCUMENT ME!
         */
        public VisibleLayersFilter(final ActiveLayerModel model) {
            super(NbBundle.getMessage(VisibleLayersFilter.class, "LayerCombobox.VisibleLayersFilter"));
            this.model = model;
            setVisibleLayerFilterLayer();

            model.addTreeModelListener(new TreeModelListener() {

                    @Override
                    public void treeNodesChanged(final TreeModelEvent e) {
                        setVisibleLayerFilterLayer();
                    }

                    @Override
                    public void treeNodesInserted(final TreeModelEvent e) {
                        setVisibleLayerFilterLayer();
                    }

                    @Override
                    public void treeNodesRemoved(final TreeModelEvent e) {
                        setVisibleLayerFilterLayer();
                    }

                    @Override
                    public void treeStructureChanged(final TreeModelEvent e) {
                        setVisibleLayerFilterLayer();
                    }
                });
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void setVisibleLayerFilterLayer() {
            final TreeMap<Integer, MapService> map = model.getMapServices();
            visibleLayer.clear();

            for (final Integer key : map.keySet()) {
                final MapService service = map.get(key);
                if (service.getPNode().getVisible()) {
                    visibleLayer.add(service);
                }
            }
        }

        @Override
        public boolean isLayerAllowed(final MapService layer) {
            return visibleLayer.contains(layer);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class SelectedLayerFilter extends DefaultLayerFilter {

        //~ Instance fields ----------------------------------------------------

        private List<MapService> selectedLayer = new ArrayList<MapService>();
        private ThemeLayerWidget themeLayer;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SelectedLayerFilter object.
         *
         * @param  themeLayer  DOCUMENT ME!
         */
        public SelectedLayerFilter(final ThemeLayerWidget themeLayer) {
            super(NbBundle.getMessage(SelectedLayerFilter.class, "LayerCombobox.SelectedLayerFilter"));
            this.themeLayer = themeLayer;
            setSelectedLayers();

            themeLayer.addTreeSelectionListener(new TreeSelectionListener() {

                    @Override
                    public void valueChanged(final TreeSelectionEvent e) {
                        setSelectedLayers();
                        model.initModel();
                    }
                });
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        private void setSelectedLayers() {
            final TreePath[] selectionPath = themeLayer.getSelectionPath();
            selectedLayer.clear();

            if (selectionPath != null) {
                for (final TreePath path : selectionPath) {
                    final Object o = path.getLastPathComponent();

                    if (o instanceof MapService) {
                        selectedLayer.add((MapService)o);
                    }
                }
            }
        }

        @Override
        public boolean isLayerAllowed(final MapService layer) {
            return selectedLayer.contains(layer);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class AllLayersFilter extends DefaultLayerFilter {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new AllLayersFilter object.
         */
        public AllLayersFilter() {
            super(NbBundle.getMessage(AllLayersFilter.class, "LayerCombobox.AllLayersFilter"));
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isLayerAllowed(final MapService layer) {
            return true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class SingleLayerFilter extends DefaultLayerFilter {

        //~ Instance fields ----------------------------------------------------

        private Object layer;
        private int depth;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SingleLayerFilter object.
         *
         * @param  layer  DOCUMENT ME!
         * @param  depth  DOCUMENT ME!
         */
        public SingleLayerFilter(final Object layer, final int depth) {
            super(layer.toString());
            super.setIcon(getIcon(layer));
            this.layer = layer;
            this.depth = depth;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean isLayerAllowed(final MapService layer) {
            return this.layer.equals(layer);
        }

        /**
         * DOCUMENT ME!
         *
         * @param   layer  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private Icon getIcon(final Object layer) {
            return new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layer.png"));
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getDepth() {
            return depth;
        }
    }
}
