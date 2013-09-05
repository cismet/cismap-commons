/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.layerwidget;

import org.jdom.Attribute;
import org.jdom.Element;

import java.awt.EventQueue;
import java.awt.Image;

import java.util.*;

import javax.swing.JTree;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.*;
import de.cismet.cismap.commons.featureservice.*;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupportingRasterLayer;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

import de.cismet.tools.CismetThreadPool;
import de.cismet.tools.PropertyEqualsProvider;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.Static2DTools;
import de.cismet.tools.gui.treetable.AbstractTreeTableModel;
import de.cismet.tools.gui.treetable.TreeTableModel;
import de.cismet.tools.gui.treetable.TreeTableModelAdapter;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class ActiveLayerModel extends AbstractTreeTableModel implements MappingModel, Configurable {

    //~ Static fields/initializers ---------------------------------------------

    protected static final boolean DEBUG = Debug.DEBUG;

    //~ Instance fields --------------------------------------------------------

    Vector layers = new Vector();
    Vector<MappingModelListener> mappingModelListeners = new Vector();
    BoundingBox initialBoundingBox;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private HashMap<String, XBoundingBox> homes = new HashMap<String, XBoundingBox>();
    private Crs srs;
    private String preferredRasterFormat;
    private String preferredTransparentPref;
    private String preferredBGColor;
    private String preferredExceptionsFormat;
    private TreeTableModelAdapter tableModel;
    private boolean initalLayerConfigurationFromServer = false;
    private HashMap<String, Element> masterLayerHashmap = new HashMap<String, Element>();
    private Crs defaultHomeSrs;

    //~ Constructors -----------------------------------------------------------

    /**
     * Erstellt eine neue ActiveLayerModel-Instanz.
     */
    public ActiveLayerModel() {
        super("Layer"); // NOI18N
        setDefaults();
        this.tableModel = new TreeTableModelAdapter(this, new JTree());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void setDefaults() {
        // srs="EPSG:4326";
        preferredRasterFormat = "image/png"; // NOI18N
        preferredBGColor = "0xF0F0F0";       // NOI18N
        // preferredExceptionsFormat="application/vnd.ogc.se_inimage";
        preferredExceptionsFormat = "application/vnd.ogc.se_xml"; // NOI18N
        initialBoundingBox = new BoundingBox(-180, -90, 180, 90);
//        srs="EPSG:31466";
//        preferredRasterFormat="image/png";
//        preferredBGColor="0xF0F0F0";
//        preferredExceptionsFormat="application/vnd.ogc.se_inimage";
//        initialBoundingBox=new BoundingBox(2569442.79,5668858.33,2593744.91,5688416.22);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layer  DOCUMENT ME!
     */
    public synchronized void addEmptyLayerCollection(final LayerCollection layer) {
        layers.add(layer);
        layer.setModel(this);
        fireTreeStructureChanged(
            this,
            new Object[] { root },
            null,
            new Object[] { layer });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  path   DOCUMENT ME!
     * @param  layer  DOCUMENT ME!
     */
    public synchronized void addEmptyLayerCollection(final TreePath path, final LayerCollection layer) {
        final Object parentCollection = path.getLastPathComponent();

        if (parentCollection instanceof LayerCollection) {
            final LayerCollection collection = (LayerCollection)parentCollection;
            collection.add(layer);
            layer.setModel(this);
            fireTreeStructureChanged(
                this,
                path.getPath(),
                null,
                new Object[] { layer });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layer  DOCUMENT ME!
     * @param  index  DOCUMENT ME!
     */
    public synchronized void addLayerCollection(final LayerCollection layer, final int index) {
        layers.add(index, layer);
        fireTreeStructureChanged(
            this,
            new Object[] { root },
            null,
            null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layer  DOCUMENT ME!
     * @param  index  DOCUMENT ME!
     */
    public synchronized void addLayer(final Object layer, final int index) {
        if (layer instanceof LayerCollection) {
            addLayerCollection((LayerCollection)layer, index);
        } else if (layer instanceof RetrievalServiceLayer) {
            addLayer((RetrievalServiceLayer)layer, index);
        }
    }

    /**
     * Fuegt dem Layer-Vektor einen neuen RetrievalServiceLayer hinzu.
     *
     * @param  layer  neuer RetrievalServiceLayer
     */
    @Override
    public synchronized void addLayer(final RetrievalServiceLayer layer) {
        addLayer(layer, layers.size());
    }

    /**
     * Fuegt dem Layer-Vektor einen neuen RetrievalServiceLayer hinzu.
     *
     * @param   layer  neuer RetrievalServiceLayer
     * @param   index  the index of the layer in the layer list
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public synchronized void addLayer(final RetrievalServiceLayer layer, final int index) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("addLayer: " + layer.getName()); // NOI18N
            }
        }

        if (layers.contains(layer)) {
            throw new IllegalArgumentException("Layer '" + layer.getName() + "' already exists");         // NOI18N
        } else if (layer instanceof PropertyEqualsProvider) {
            for (final Object o : layers) {
                if ((o instanceof PropertyEqualsProvider) && ((PropertyEqualsProvider)o).propertyEquals(layer)) {
                    throw new IllegalArgumentException("Layer '" + layer.getName() + "' already exists"); // NOI18N
                }
            }
        }

        registerRetrievalServiceLayer(layer);

        // Das eigentliche Hinzufuegen des neuen Layers
        layers.add(index, layer);
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("layer '" + layer.getName() + "' added"); // NOI18N
            }
        }
        fireTreeStructureChanged(
            this,
            new Object[] { root },
            null,
            null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layer  DOCUMENT ME!
     */
    public void registerRetrievalServiceLayer(final RetrievalServiceLayer layer) {
        final RetrievalServiceLayer currentLayer = layer;
        final ActiveLayerEvent ale = new ActiveLayerEvent();
        ale.setLayer(currentLayer);
        CismapBroker.getInstance().fireLayerAdded(ale);
//            ale.setCapabilities(wmsLayer.getWmsCapabilities());
        CidsLayerFactory.wmsSpecificConfiguration(
            layer,
            preferredBGColor,
            preferredExceptionsFormat,
            preferredRasterFormat,
            srs);
        layer.addRetrievalListener(new RetrievalListener() {

                @Override
                public void retrievalStarted(final RetrievalEvent e) {
                    if (DEBUG) {
                        if (log.isDebugEnabled()) {
                            log.debug(currentLayer.getName() + "[" + e.getRequestIdentifier() + "]: retrievalStarted"); // NOI18N
                        }
                    }
                    // currentLayer.setProgress(-1);
                    fireProgressChanged(currentLayer);
                }

                @Override
                public void retrievalProgress(final RetrievalEvent e) {
                    // currentLayer.setProgress((int) (e.getPercentageDone() * 100));
                    fireProgressChanged(currentLayer);
                }

                @Override
                public void retrievalComplete(final RetrievalEvent e) {
                    if (DEBUG) {
                        if (log.isDebugEnabled()) {
                            log.debug(currentLayer.getName() + "[" + e.getRequestIdentifier() + "]: retrievalComplete"); // NOI18N
                        }
                    }
                    // currentLayer.setProgress(100);
                    fireProgressChanged(currentLayer);
                    if (e.isHasErrors()) {
                        retrievalError(e);
                    } else {
                        currentLayer.setErrorObject(null);
                    }
                }

                @Override
                public void retrievalAborted(final RetrievalEvent e) {
                    if (DEBUG) {
                        if (log.isDebugEnabled()) {
                            log.debug(currentLayer.getName() + "[" + e.getRequestIdentifier() + "]: retrievalAborted"); // NOI18N
                        }
                    }
                    // currentLayer.setProgress(0);
                    fireProgressChanged(currentLayer);
                }

                @Override
                public void retrievalError(final RetrievalEvent e) {
                    if (DEBUG) {
                        log.warn(
                            currentLayer.getName()
                                    + "["
                                    + e.getRequestIdentifier()
                                    + "]: retrievalError: "
                                    + e.getErrorType()
                                    + " (hasErrors="
                                    + currentLayer.hasErrors()
                                    + ")"); // NOI18N
                    }
                    // currentLayer.setProgress(0);

                    fireProgressChanged(currentLayer);

                    if (e.getRetrievedObject() != null) {
                        Object errorObject = e.getRetrievedObject();
                        if (errorObject instanceof Image) {
                            // Static2DTools.scaleImage((Image)errorObject,0.7);
                            final Image i = Static2DTools.removeUnusedBorder((Image)errorObject, 5, 0.7);
                            errorObject = i;
                        } else if (e.getRetrievedObject() instanceof String) {
                            final String message = (String)e.getRetrievedObject();
//                        message=message.replaceAll("<.*>","");
                            if (e.getErrorType().equals(RetrievalEvent.SERVERERROR)) {
                                errorObject = org.openide.util.NbBundle.getMessage(
                                        ActiveLayerModel.class,
                                        "ActiveLayerModel.retrievalError(RetrievalEvent).errorObject.servererror",
                                        new Object[] { message });     // NOI18N
                            } else {
                                if (message != null) {
                                    errorObject = org.openide.util.NbBundle.getMessage(
                                            ActiveLayerModel.class,
                                            "ActiveLayerModel.retrievalError(RetrievalEvent).errorObject.noServererror",
                                            new Object[] { message }); // NOI18N
                                } else {
                                    errorObject = org.openide.util.NbBundle.getMessage(
                                            ActiveLayerModel.class,
                                            "ActiveLayerModel.retrievalError(RetrievalEvent).errorObject.noServererror",
                                            new Object[] {});          // NOI18N
                                }
                            }
                        }                                              // Hier kommt jetzt HTML Fehlermeldung, Internal
                        // und XML. Das muss reichen
                        // else if ()

                        currentLayer.setErrorObject(errorObject);
                    } else if (DEBUG) {
                        log.warn("no error object supplied"); // NOI18N
                    }
                }
            });

        if (layer instanceof ModeLayer) {
            fireMapServiceAdded((MapService)((ModeLayer)layer).getCurrentLayer());
        } else if (layer instanceof MapService) {
            fireMapServiceAdded(((MapService)layer));
        } else {
            log.warn("fireMapServiceAdded event not fired, layer is no MapService:" + layer); // NOI18N
        }

        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("RetrievalListener added on layer '" + currentLayer.getName() + "'"); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshWebFeatureServices() {
        final Object[] oa = layers.toArray();
        final Vector<WebFeatureService> removedLayer = new Vector<WebFeatureService>();

        for (int i = 0; i < oa.length; i++) {
            if (oa[i] instanceof WebFeatureService) {
                removedLayer.add((WebFeatureService)oa[i]);
                removeLayer(oa[i], null);
            }
        }

        for (final WebFeatureService tmp : removedLayer) {
            addLayer((WebFeatureService)tmp.clone());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshShapeFileLayer() {
        final Object[] oa = layers.toArray();

        for (int i = 0; i < oa.length; i++) {
            if (oa[i] instanceof ShapeFileFeatureService) {
                ((ShapeFileFeatureService)oa[i]).getPNode().removeAllChildren();
                ((ShapeFileFeatureService)oa[i]).setCrs(srs);
                ((ShapeFileFeatureService)oa[i]).retrieve(true);
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void removeAllLayers() {
        final Object[] oa = layers.toArray();
        for (int i = 0; i < oa.length; i++) {
            final Object elem = oa[i];
            removeLayer(elem, null);
        }
    }

    //J-
    public void removeLayer(final TreePath treePath) {
        final Object layer = treePath.getLastPathComponent();
        removeLayer(layer, treePath);
    }
    //J+

    /**
     * DOCUMENT ME!
     *
     * @param  layer     DOCUMENT ME!
     * @param  treePath  DOCUMENT ME!
     */
    public void removeLayer(final Object layer, final TreePath treePath) {
        if ((treePath != null) && !treePath.getParentPath().getLastPathComponent().equals(getRoot())) {
            final Object parent = treePath.getParentPath().getLastPathComponent();

            if (parent instanceof LayerCollection) {
                ((LayerCollection)parent).remove(layer);
                fireTreeStructureChanged(this, new Object[] { layer }, null, null);
            }
        } else {
            if (layer instanceof RetrievalServiceLayer) {
                removeLayer((RetrievalServiceLayer)layer);
            } else if (layer instanceof LayerCollection) {
                layers.remove(layer);
                fireTreeStructureChanged(this, new Object[] { layer }, null, null);
            } else if ((treePath != null) && (layer instanceof WMSLayer)) { // Kinderlayer

                final TreePath parentPath = treePath.getParentPath();
                if (parentPath.getLastPathComponent() instanceof WMSServiceLayer) {
                    ((WMSServiceLayer)parentPath.getLastPathComponent()).removeLayer((WMSLayer)layer);
                }
                fireTreeStructureChanged(
                    this,
                    new Object[] { root, (WMSServiceLayer)parentPath.getLastPathComponent() },
                    null,
                    null);
                final ActiveLayerEvent ale = new ActiveLayerEvent();
                ale.setLayer((WMSLayer)layer);
                CismapBroker.getInstance().fireLayerRemoved(ale);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layer  DOCUMENT ME!
     */
    @Override
    public void removeLayer(final RetrievalServiceLayer layer) {
        final RetrievalServiceLayer wmsServiceLayer = ((RetrievalServiceLayer)layer);
        layers.remove(wmsServiceLayer);
        final ActiveLayerEvent ale = new ActiveLayerEvent();
        ale.setLayer(wmsServiceLayer);
        CismapBroker.getInstance().fireLayerRemoved(ale);
        fireTreeStructureChanged(
            this,
            new Object[] { root },
            null,
            null);
        fireMapServiceRemoved((MapService)wmsServiceLayer);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layer  DOCUMENT ME!
     */
    public void removeLayer(final Object layer) {
        if (layer instanceof LayerCollection) {
            removeLayerCollection((LayerCollection)layer);
        } else if (layer instanceof RetrievalServiceLayer) {
            removeLayer((RetrievalServiceLayer)layer);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layer  DOCUMENT ME!
     */
    public void removeLayerCollection(final LayerCollection layer) {
        layers.remove(layer);
        final ActiveLayerEvent ale = new ActiveLayerEvent();
        ale.setLayer(layer);
        CismapBroker.getInstance().fireLayerRemoved(ale);
        fireTreeStructureChanged(
            this,
            new Object[] { root },
            null,
            null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  treePath  DOCUMENT ME!
     */
    public void disableLayer(final TreePath treePath) {
        final Object layer = treePath.getLastPathComponent();
        final ActiveLayerEvent activeLayerEvent = new ActiveLayerEvent();
        if (layer instanceof RetrievalServiceLayer) {
            final RetrievalServiceLayer wmsServiceLayer = ((RetrievalServiceLayer)layer);
            wmsServiceLayer.setEnabled(!wmsServiceLayer.isEnabled());

            activeLayerEvent.setLayer(layer);
            CismapBroker.getInstance().fireLayerAvailabilityChanged(activeLayerEvent);

            if (wmsServiceLayer.isEnabled()) {
                wmsServiceLayer.setRefreshNeeded(true);
            }
            fireTreeNodesChanged(
                this,
                new Object[] { root },
                null,
                null);
        } else if (layer instanceof WMSLayer) { // Kinderlayer

            final TreePath parentPath = treePath.getParentPath();
            ((WMSLayer)layer).setEnabled(!((WMSLayer)layer).isEnabled());

            activeLayerEvent.setLayer(layer);
            CismapBroker.getInstance().fireLayerAvailabilityChanged(activeLayerEvent);

            ((WMSServiceLayer)parentPath.getLastPathComponent()).setRefreshNeeded(true);
            fireTreeNodesChanged(
                this,
                new Object[] { root, (WMSServiceLayer)parentPath.getLastPathComponent() },
                null,
                null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  treePath  DOCUMENT ME!
     */
    public void handleVisibility(final TreePath treePath) {
        final Object layer = treePath.getLastPathComponent();
        if (layer instanceof RetrievalServiceLayer) {
            final RetrievalServiceLayer wmsServiceLayer = ((RetrievalServiceLayer)layer);
            wmsServiceLayer.getPNode().setVisible(!wmsServiceLayer.getPNode().getVisible());

            fireTreeNodesChanged(
                this,
                new Object[] { root },
                null,
                null);

            final ActiveLayerEvent ale = new ActiveLayerEvent();
            ale.setLayer(wmsServiceLayer);
            CismapBroker.getInstance().fireLayerVisibilityChanged(ale);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   treePath  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isVisible(final TreePath treePath) {
        final Object layer = treePath.getLastPathComponent();

        if (layer instanceof RetrievalServiceLayer) {
            final RetrievalServiceLayer wmsServiceLayer = ((RetrievalServiceLayer)layer);
            return wmsServiceLayer.getPNode().getVisible();
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  source       DOCUMENT ME!
     * @param  destination  DOCUMENT ME!
     * @param  index        DOCUMENT ME!
     * @param  layer        DOCUMENT ME!
     */
    public void moveLayer(final TreePath source, final TreePath destination, final int index, final Object layer) {
        int indexOfRemovedObject = 0;

        if (source.getLastPathComponent().equals(getRoot())) {
            indexOfRemovedObject = layers.indexOf(layer);
            layers.remove(layer);
        } else if (source.getLastPathComponent() instanceof LayerCollection) {
            indexOfRemovedObject = ((LayerCollection)source.getLastPathComponent()).indexOf(layer);
            ((LayerCollection)source.getLastPathComponent()).remove(layer);
        }

        if (destination.getLastPathComponent().equals(getRoot())) {
            layers.add(layers.size() - index, layer);
        } else if (destination.getLastPathComponent() instanceof LayerCollection) {
            final LayerCollection collection = ((LayerCollection)destination.getLastPathComponent());
            collection.add(collection.size() - index, layer);
        }

//        fireTreeStructureChanged(
//            this,
//            new Object[] { root },
//            null,
//            null);
        fireTreeStructureChanged(
            this,
            source.getPath(),
            new int[] { indexOfRemovedObject },
            new Object[] { layer });
        fireTreeStructureChanged(
            this,
            destination.getPath(),
            null,
            new Object[] { layer });
        reorderLayer();
    }

    /**
     * DOCUMENT ME!
     */
    private void reorderLayer() {
        final TreeMap<Integer, MapService> map = getMapServices();
        MapService lastService = null;

        for (final Integer key : map.keySet()) {
            final MapService s = map.get(key);

            if (lastService == null) {
                lastService = s;
            } else {
                s.getPNode().moveInFrontOf(((MapService)lastService).getPNode());
                lastService = s;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  treePath  DOCUMENT ME!
     */
    public void moveLayerUp(final TreePath treePath) {
        final Object layer = treePath.getLastPathComponent();
        if (layer instanceof RetrievalServiceLayer) {
            final MapService l = (MapService)layer;
            final int pos = layers.indexOf(l);
            if ((pos + 1) != layers.size()) {
                layers.remove(l);
                layers.add(pos + 1, l);
                if (layers.get(pos) instanceof MapService) {
                    l.getPNode().moveInFrontOf(((MapService)layers.get(pos)).getPNode());
                }
                fireTreeStructureChanged(
                    this,
                    new Object[] { root },
                    new int[] { pos, pos + 1 },
                    new Object[] { layers.get(pos), l });
            }
        } else if (layer instanceof WMSLayer) {
            final WMSLayer l = (WMSLayer)layer;
            final WMSServiceLayer parent = (WMSServiceLayer)treePath.getParentPath().getLastPathComponent();
            final int pos = parent.getWMSLayers().indexOf(l);
            if ((pos + 1) != parent.getWMSLayers().size()) {
                parent.getWMSLayers().remove(l);
                parent.getWMSLayers().add(pos + 1, l);
                parent.setRefreshNeeded(true);
                fireTreeStructureChanged(
                    this,
                    new Object[] { root, parent },
                    null,
                    null);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  treePath  DOCUMENT ME!
     */
    public void moveLayerDown(final TreePath treePath) {
        final Object layer = treePath.getLastPathComponent();
        if (layer instanceof MapService) {
            final MapService l = (MapService)layer;
            final int pos = layers.indexOf(l);
            if (pos != 0) {
                layers.remove(l);
                layers.add(pos - 1, l);

                if (layers.get(pos) instanceof MapService) {
                    l.getPNode().moveInBackOf(((MapService)layers.get(pos)).getPNode());
                }
                fireTreeStructureChanged(
                    this,
                    new Object[] { root },
                    new int[] { pos - 1, pos },
                    new Object[] { l, layers.get(pos) });
            }
        } else if (layer instanceof WMSLayer) {
            final WMSLayer l = (WMSLayer)layer;
            final WMSServiceLayer parent = (WMSServiceLayer)treePath.getParentPath().getLastPathComponent();
            final int pos = parent.getWMSLayers().indexOf(l);
            if (pos != 0) {
                parent.getWMSLayers().remove(l);
                parent.getWMSLayers().add(pos - 1, l);
                parent.setRefreshNeeded(true);
                fireTreeStructureChanged(
                    this,
                    new Object[] { root, parent },
                    null,
                    null);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   treePath  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getLayerPosition(final TreePath treePath) {
        final Object layer = treePath.getLastPathComponent();

        if (layer instanceof MapService) {
            final MapService l = (MapService)layer;
            final int pos = layers.indexOf(l);
            return pos;
        } else if (layer instanceof WMSLayer) {
            final WMSLayer l = (WMSLayer)layer;
            final WMSServiceLayer parent = (WMSServiceLayer)treePath.getParentPath().getLastPathComponent();
            final int pos = parent.getWMSLayers().indexOf(l);
            return pos;
        }

        return 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   column  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Class getColumnClass(final int column) {
        switch (column) {
            case 1: {
                return TreeTableModel.class;
            }
//            case 2:
//                return Boolean.class;
            default: {
                return Object.class;
            }
        }
    }

    /**
     * Returns the number of children of <code>parent</code>. Returns 0 if the node is a leaf or if it has no children.
     * <code>parent</code> must be a node previously obtained from this data source.
     *
     * @param   parent  a node in the tree, obtained from this data source
     *
     * @return  the number of children of the node <code>parent</code>
     */
    @Override
    public int getChildCount(final Object parent) {
        if (parent == super.getRoot()) {
            return layers.size();
        }

        if (parent instanceof WMSServiceLayer) {
            final WMSServiceLayer wmsServiceLayer = (WMSServiceLayer)parent;
            if (wmsServiceLayer.getWMSLayers().size() > 1) {
                return wmsServiceLayer.getWMSLayers().size();
            } else {
                return 0;
            }
        } else if (parent instanceof LayerCollection) {
            return ((LayerCollection)parent).size();
        } else {
            return 0;
        }
    }

    @Override
    public boolean isLeaf(final Object node) {
        return getChildCount(node) == 0;
    }

//    public int getChildCount(final Object parent, boolean layerCollectionsSupported) {
//        if (parent == super.getRoot()) {
//            if (layerCollectionsSupported) {
//                return layers.size();
//            } else {
//                int count = 0;
//
//                for (Object o : layers) {
//                    if (o instanceof LayerCollection) {
//                        count += getLayerCount((LayerCollection)o);
//                    } else {
//                        ++count;
//                    }
//                }
//            }
//        }
//
//        if (parent instanceof WMSServiceLayer) {
//            final WMSServiceLayer wmsServiceLayer = (WMSServiceLayer)parent;
//            if (wmsServiceLayer.getWMSLayers().size() > 1) {
//                return wmsServiceLayer.getWMSLayers().size();
//            } else {
//                return 0;
//            }
//        } else if (parent instanceof LayerCollection) {
//            if (layerCollectionsSupported) {
//                return ((LayerCollection)parent).size();
//            } else {
//                return getLayerCount((LayerCollection)parent);
//            }
//        } else {
//            return 0;
//        }
//    }
//
//    private int getLayerCount(LayerCollection l) {
//        int count = 0;
//
//        for (Object o : l) {
//            if (o instanceof LayerCollection) {
//                count += getLayerCount((LayerCollection)o);
//            } else {
//                ++count;
//            }
//        }
//
//        return count;
//    }
//

    /**
     * Returns the value to be displayed for node <code>node</code>, at column number <code>column</code>.
     *
     * @param   node    DOCUMENT ME!
     * @param   column  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object getValueAt(final Object node, final int column) {
        if (node instanceof RetrievalServiceLayer) {
            return ((RetrievalServiceLayer)node);
        } else if (node instanceof WMSLayer) {
            return ((WMSLayer)node);
        } else {
            return "ROOT 0"; // NOI18N
        }
    }

    /**
     * Returns the name for column number <code>column</code>.
     *
     * @param   column  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getColumnName(final int column) {
        switch (column) {
            case (0): {
                return " ";                                                                   // NOI18N
            }
            case (1): {
                return org.openide.util.NbBundle.getMessage(
                        ActiveLayerModel.class,
                        "ActiveLayerModel.getColumnName(int).return.layer");                  // NOI18N
            }
            case (2): {
                return org.openide.util.NbBundle.getMessage(
                        ActiveLayerModel.class,
                        "ActiveLayerModel.getColumnName(int).return.style");                  // NOI18N
            }
            case (3): {
                return org.openide.util.NbBundle.getMessage(
                        ActiveLayerModel.class,
                        "ActiveLayerModel.getColumnName(int).return.info");                   // NOI18N
            }
            case (4): {
                return org.openide.util.NbBundle.getMessage(
                        ActiveLayerModel.class,
                        "ActiveLayerModel.getColumnName(int).return.fortschrittTransparent"); // NOI18N
            }
            case (5): {
                return "";                                                                    // NOI18N
            }
            default: {
                return "";                                                                    // NOI18N
            }
        }
    }

    /**
     * Returns the child of <code>parent</code> at index <code>index</code> in the parent's child array. <code>
     * parent</code> must be a node previously obtained from this data source. This should not return <code>null</code>
     * if <code>index</code> is a valid index for <code>parent</code> (that is <code>index >= 0 && index <
     * getChildCount(parent</code>)).
     *
     * @param   parent  a node in the tree, obtained from this data source
     * @param   index   DOCUMENT ME!
     *
     * @return  the child of <code>parent</code> at index <code>index</code>
     */
    @Override
    public Object getChild(final Object parent, final int index) {
        // Hier wird die Reihenfolge festgelegt
        if (parent == root) {
            return layers.get(layers.size() - 1 - index);
        } else if (parent instanceof WMSServiceLayer) {
            return ((WMSServiceLayer)parent).getWMSLayers()
                        .get(((WMSServiceLayer)parent).getWMSLayers().size() - 1 - index);
        } else if (parent instanceof LayerCollection) {
            return ((LayerCollection)parent).get(((LayerCollection)parent).size() - 1 - index);
        } else {
            return null;
        }
    }

    /**
     * public Object getChild(final Object parent, final int index, boolean supportLayerCollection) { // Hier wird die
     * Reihenfolge festgelegt if (parent == root) { return layers.get(layers.size() - 1 - index); } else if (parent
     * instanceof WMSServiceLayer) { return ((WMSServiceLayer)parent).getWMSLayers()
     * .get(((WMSServiceLayer)parent).getWMSLayers().size() - 1 - index); } else if (parent instanceof LayerCollection)
     * { return ((LayerCollection)parent).get(((LayerCollection)parent).size() - 1 - index); } else { return null; } }.
     *
     * @param   parent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getLayerCollectionCount(final Object parent) {
        int count = 0;
        Collection col;

        if (parent == root) {
            col = layers;
        } else if (parent instanceof LayerCollection) {
            col = (LayerCollection)parent;
        } else {
            return 0;
        }

        for (final Object o : col) {
            if (!(o instanceof LayerCollection)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * Returns the number ofs availible column.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getColumnCount() {
        return 6;
    }

    /**
     * By default, make the column with the Tree in it the only editable one. Making this column editable causes the
     * JTable to forward mouse and keyboard events in the Tree column to the underlying JTree.
     *
     * @param   node    DOCUMENT ME!
     * @param   column  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isCellEditable(final Object node, final int column) {
        switch (column) {
            case 0: {
                return true;
            }
            case 1: {
                if ((node instanceof WMSServiceLayer) && (((WMSServiceLayer)node).getWMSLayers().size() > 1)) {
                    return true;
                } else {
                    return false;
                }
            }
            case 2: {
                if ((node instanceof WMSServiceLayer) && (((WMSServiceLayer)node).getWMSLayers().size() > 1)) {
                    return false;
                } else if ((node instanceof WMSServiceLayer) && (((WMSServiceLayer)node).getWMSLayers().size() == 1)
                            && (((WMSLayer)((WMSServiceLayer)node).getWMSLayers().get(0)).getOgcCapabilitiesLayer()
                                .getStyles().length > 1)) {
                    return true;
                } else if ((node instanceof WMSLayer)
                            && (((WMSLayer)node).getOgcCapabilitiesLayer().getStyles().length > 1)) {
                    return true;
                } else if (node instanceof AbstractFeatureService) {
                    return true;
                } else {
                    return false;
                }
            }
            case 3: {
                if (node instanceof LayerInfoProvider) {
                    return ((LayerInfoProvider)node).isQueryable();
                } else {
                    return false;
                }
            }

//                if (node instanceof WMSServiceLayer && ((WMSServiceLayer) node).getWMSLayers().size() > 1) {
//                    return false;
//                } else {
//                    if (node instanceof WMSLayer) {
//                        return ((WMSLayer) node).getOgcCapabilitiesLayer().isQueryable();
//                    } else if (node instanceof WMSServiceLayer && ((WMSServiceLayer) node).getWMSLayers().get(0) instanceof WMSLayer) {
//                        return ((WMSLayer) (((WMSServiceLayer) node).getWMSLayers().get(0))).getOgcCapabilitiesLayer().isQueryable();
//                    } else {
//                        return false;
//                    }
//                }
            case 4: {
                if (node instanceof RetrievalServiceLayer) {
                    return true;
                } else {
                    return false;
                }
            }
            case 5: {
                return true;
            }
        }
        final boolean retValue;

        retValue = super.isCellEditable(node, column);

        // return retValue;
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  aValue  DOCUMENT ME!
     * @param  node    DOCUMENT ME!
     * @param  column  DOCUMENT ME!
     */
    @Override
    public void setValueAt(final Object aValue, final Object node, final int column) {
        if (column == 1) {
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("node:" + node);     // NOI18N
                }
            }
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("aValue:" + aValue); // NOI18N
                }
            }
            ((WMSServiceLayer)node).setName(aValue.toString());
            this.fireTreeNodesChanged(
                this,
                new Object[] { root, node },
                null,
                null);
        } else if (column == 3) {
            // if (aValue instanceof WMSLayer)
        }
        super.setValueAt(aValue, node, column);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mml  DOCUMENT ME!
     */
    @Override
    public void removeMappingModelListener(final de.cismet.cismap.commons.MappingModelListener mml) {
        mappingModelListeners.remove(mml);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mml  DOCUMENT ME!
     */
    @Override
    public void addMappingModelListener(final de.cismet.cismap.commons.MappingModelListener mml) {
        mappingModelListeners.add(mml);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public java.util.TreeMap<Integer, MapService> getMapServices() {
        final Iterator it = layers.iterator();
        final TreeMap<Integer, MapService> tm = new TreeMap();
        int counter = 0;
        while (it.hasNext()) {
            final Object o = it.next();
            if (o instanceof MapService) {
                tm.put(new Integer(counter++), (MapService)o);
            } else if (o instanceof LayerCollection) {
                for (final MapService ms : getMapServicesFromLayerCollection((LayerCollection)o)) {
                    tm.put(new Integer(counter++), ms);
                }
            } else {
                log.warn("service is not of type MapService: " + o); // NOI18N
            }
        }
        return tm;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public java.util.TreeMap<Integer, Object> getMapServicesAndCollections() {
        final Iterator it = layers.iterator();
        final TreeMap<Integer, Object> tm = new TreeMap();
        int counter = 0;
        while (it.hasNext()) {
            final Object o = it.next();
            if (o instanceof MapService) {
                tm.put(new Integer(counter++), (MapService)o);
            } else if (o instanceof LayerCollection) {
                tm.put(new Integer(counter++), (LayerCollection)o);
            } else {
                log.warn("service is not of type MapService: " + o); // NOI18N
            }
        }
        return tm;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   col  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<MapService> getMapServicesFromLayerCollection(final LayerCollection col) {
        final List<MapService> resultList = new ArrayList<MapService>();

        for (final Object o : col) {
            if (o instanceof MapService) {
                resultList.add((MapService)o);
            } else if (o instanceof LayerCollection) {
                resultList.addAll(getMapServicesFromLayerCollection((LayerCollection)o));
            }
        }

        return resultList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public HashMap getHomeBoundingBoxes() {
        return homes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  xbb  DOCUMENT ME!
     */
    public void addHome(final XBoundingBox xbb) {
        homes.put(xbb.getSrs(), xbb);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public de.cismet.cismap.commons.BoundingBox getInitialBoundingBox() {
        if ((srs == null) && (defaultHomeSrs == null)) {
            log.warn("SRS and default SRS are not set, yet");
            return null;
        }
        XBoundingBox homeBox = homes.get(srs.getCode());

        if (homeBox == null) {
            if (log.isDebugEnabled()) {
                log.debug("No home found for srs " + srs.getCode());
            }

            final XBoundingBox defaultBox = homes.get(defaultHomeSrs.getCode());

            if (defaultBox != null) {
                try {
                    final CrsTransformer transformer = new CrsTransformer(srs.getCode());
                    homeBox = transformer.transformBoundingBox(defaultBox);
                } catch (Exception e) {
                    log.error("Error while transforming coordinates from " + defaultBox.getSrs() + " to " + srs, e);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No default bunding box (home) found. ");
                }
            }
        }

        if (homeBox == null) {
            log.error("home bounding box == null for srs " + srs.getCode());
        }

        return homeBox;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Crs getSrs() {
        return srs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param       srs  DOCUMENT ME!
     *
     * @deprecated  the method setSrs(Crs srs) should be used instead. This method only exists for compatibility with
     *              cids_custom_wuppertal
     */
    public void setSrs(final String srs) {
        setSrs(new Crs(srs, srs, srs, true, false));
    }

    /**
     * DOCUMENT ME!
     *
     * @param  srs  DOCUMENT ME!
     */
    public void setSrs(final Crs srs) {
        if (this.defaultHomeSrs == null) {
            this.defaultHomeSrs = srs;
        }

        for (final Object layer : this.layers) {
            CidsLayerFactory.setLayerToCrs(srs, layer);
        }

        this.srs = srs;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPreferredRasterFormat() {
        return preferredRasterFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  preferredRasterFormat  DOCUMENT ME!
     */
    public void setPreferredRasterFormat(final String preferredRasterFormat) {
        this.preferredRasterFormat = preferredRasterFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPreferredTransparentPref() {
        return preferredTransparentPref;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  preferredTransparentPref  DOCUMENT ME!
     */
    public void setPreferredTransparentPref(final String preferredTransparentPref) {
        this.preferredTransparentPref = preferredTransparentPref;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPreferredBGColor() {
        return preferredBGColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  preferredBGColor  DOCUMENT ME!
     */
    public void setPreferredBGColor(final String preferredBGColor) {
        this.preferredBGColor = preferredBGColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPreferredExceptionsFormat() {
        return preferredExceptionsFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  preferredExceptionsFormat  DOCUMENT ME!
     */
    public void setPreferredExceptionsFormat(final String preferredExceptionsFormat) {
        this.preferredExceptionsFormat = preferredExceptionsFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sl  DOCUMENT ME!
     */
    public void fireProgressChanged(final ServiceLayer sl) {
        final int pos = layers.indexOf(sl);
        if (pos >= 0) {
//            this.fireTreeNodesChanged(this, new Object[] { root, sl }, null, null);
            fireTableChanged(null);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    public void fireTableChanged(final TableModelEvent e) {
        // Guaranteed to return a non-null array
        final Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TableModelListener.class) {
                ((TableModelListener)listeners[i + 1]).tableChanged(e);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  rasterService  DOCUMENT ME!
     */
    public void fireMapServiceAdded(final MapService rasterService) {
        final Vector v = new Vector(mappingModelListeners);
        final Iterator it = v.iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if (o instanceof MappingModelListener) {
                final MappingModelListener mml = (MappingModelListener)o;
                mml.mapServiceAdded(rasterService);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  rasterService  DOCUMENT ME!
     */
    public void fireMapServiceRemoved(final MapService rasterService) {
        final Iterator it = mappingModelListeners.iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if (o instanceof MappingModelListener) {
                final MappingModelListener mml = (MappingModelListener)o;
                mml.mapServiceRemoved(rasterService);
            }
        }
    }

    /**
     * Configurable.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  NoWriteError  DOCUMENT ME!
     */
    @Override
    public Element getConfiguration() throws NoWriteError {
        final Element conf = new Element("cismapActiveLayerConfiguration"); // NOI18N
        // Zuerst alle RasterLayer
        final Iterator<Integer> it = getMapServicesAndCollections().keySet().iterator();
        final Element allLayerConf = new Element("Layers"); // Sollte irgendwann zu "Layers" umgewandelt werden
        // (TODO)//NOI18N

        int counter = 0;
        while (it.hasNext()) {
            final Object service = layers.get(it.next());
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("saving configuration of service: '" + service + "'"); // NOI18N
                }
            }

            if (service instanceof ServiceLayer) {
                // es reicht völlig aus, die Layer Position erst beim Speichern der
                // Konfiugration zu setzten und nicht bei jedem Aufruf von moveLayerUp/Down.
                ((ServiceLayer)service).setLayerPosition(counter);
            }

            if (service instanceof SimpleFeatureSupportingRasterLayer) {
                // wird nicht gespeichert
            } else {
                final Element layerConf = CidsLayerFactory.getElement(service);
                allLayerConf.addContent(layerConf);
                counter++;
            }
        }
        if (counter == 0) {
            // ToDo Why ?
            // throw new NoWriteError();
        }
        conf.addContent(allLayerConf);
        // Alle FeatureService Layer

        // AppFeatureLayer

        return conf;
    }

    /**
     * ToDo abstract class or interface for all layer should implement a key string method then every layer developer
     * has to define a string which uniquely identifies the developed layer.
     *
     * @param  e  layerelement DOCUMENT ME!
     */
    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void masterConfigure(final Element e) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("MasterConfigure(): " + this.getClass().getName());
            }
        }
        masterLayerHashmap.clear();
        if (initalLayerConfigurationFromServer) {
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("Layerkonfiguration vom Server wird geladen");
                }
            }
            try {
                Element layersElement = e.getChild("cismapActiveLayerConfiguration").getChild("Layers");   // NOI18N
                if (layersElement == null) {
                    log.warn("LayerElement nicht gefunden! Suche nach altem Kind \"RasterLayers\"");       // NOI18N
                    layersElement = e.getChild("cismapActiveLayerConfiguration").getChild("RasterLayers"); // NOI18N
                    if (layersElement == null) {
                        log.error("Kein valides Layerelement gefunden.");                                  // NOI18N
                        return;
                    }
                }
                final Element[] orderedLayers = CidsLayerFactory.orderLayers(layersElement);
                for (final Element curLayerElement : orderedLayers) {
                    final String curKeyString = CidsLayerFactory.getKeyforLayerElement(curLayerElement);
                    if (curKeyString != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Adding element: " + curLayerElement + " with key: " + curKeyString);
                        }
                        masterLayerHashmap.put(curKeyString, curLayerElement);
                    } else {
                        log.warn("Es war nicht möglich einen Keystring für das Element: " + curLayerElement
                                    + " zu erzeugen");
                    }
                }
            } catch (Exception ex) {
                log.warn("Kann die Layerkonfiguration des Servers nicht laden", ex);
            }
        } else {
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("Es wird keine Layerkonfiguration vom Server geladen ");
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public synchronized void configure(final Element e) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("ActiveLayerModel configure(" + e.getName() + ")");  // NOI18N
            }
        }
        try {
            final Element conf = e.getChild("cismapActiveLayerConfiguration"); // NOI18N
            final List<String> links = LayerWidget.getCapabilities(conf, new ArrayList<String>());
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("Capabilties links: " + links);                  // NOI18N
                }
            }
            final HashMap<String, WMSCapabilities> capabilities = new HashMap<String, WMSCapabilities>();

            if (links.size() > 0) {
                createLayers(conf, capabilities);
            } else {
                if (DEBUG) {
                    if (log.isDebugEnabled()) {
                        log.debug("No Capabilities links"); // NOI18N
                    }
                }
                createLayers(conf, capabilities);
            }
        } catch (Throwable ex) {
            log.error("Error during the configuration of the ActiveLayerModell", ex); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  conf          DOCUMENT ME!
     * @param  capabilities  DOCUMENT ME!
     */
    private void createLayers(final Element conf, final HashMap<String, WMSCapabilities> capabilities) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("removing all existing layers");                                             // NOI18N
            }
        }
        removeAllLayers();
        Element layerElement = conf.getChild("Layers");                                                // NOI18N
        if (layerElement == null) {
            log.warn("LayerElement not found! Check for old version child \"RasterLayers\"");          // NOI18N
            layerElement = conf.getChild("RasterLayers");                                              // NOI18N
            if (layerElement == null) {
                log.error("no valid layers element found");                                            // NOI18N
                return;
            }
        }
        log.info("restoring " + layerElement.getChildren().size() + " layers from xml configuration"); // NOI18N
        final Element[] orderedLayers = CidsLayerFactory.orderLayers(layerElement);
        for (final Element element : orderedLayers) {
            createLayer(element, capabilities);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  element       DOCUMENT ME!
     * @param  capabilities  DOCUMENT ME!
     */
    private void createLayer(final Element element, final HashMap<String, WMSCapabilities> capabilities) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("trying to add Layer '" + element.getName() + "'"); // NOI18N
            }
        }
        final String currentKeyString = CidsLayerFactory.getKeyforLayerElement(element);
        if (isInitalLayerConfigurationFromServer()
                    && !masterLayerHashmap.containsKey(currentKeyString)) {
            log.info("Layer in Serverkonfiguration nicht vorhanden, wird nicht hinzugefügt KeyString: "
                        + currentKeyString);
            return;
        }
        try {
            final ServiceLayer layer = CidsLayerFactory.createLayer(element, capabilities, this);

            if (layer != null) {
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                try {
                                    log.info(
                                        "addLayer  ("
                                                + layer.getName()
                                                + ")");                         // NOI18N
                                    if (layer instanceof ActiveLayerModelStore) {
                                        ((ActiveLayerModelStore)layer).setActiveLayerModel(ActiveLayerModel.this);
                                    }
                                    addLayer(layer, 0);
                                } catch (IllegalArgumentException schonVorhanden) {
                                    log.warn(
                                        "Layer '"
                                                + layer.getName()
                                                + "' already existed. Do not add the Layer. \n"
                                                + schonVorhanden.getMessage()); // NOI18N
                                }
                            } catch (Exception e) {
                                log.error("Error while initialising WMS", e);
                            }
                        }
                    });
            }
        } catch (Throwable t) {
            log.error("Layer layer '" + element.getName() + "' could not be created: \n" + t.getMessage(), t); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Deprecated
    @Override
    public java.util.TreeMap getRasterServices() {
        return getMapServices();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public java.util.TreeMap getFeatureServices() {
        return new TreeMap();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isInitalLayerConfigurationFromServer() {
        return initalLayerConfigurationFromServer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  initalLayerConfigurationFromServer  DOCUMENT ME!
     */
    public void setInitalLayerConfigurationFromServer(final boolean initalLayerConfigurationFromServer) {
        this.initalLayerConfigurationFromServer = initalLayerConfigurationFromServer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the defaultSrs
     */
    public Crs getDefaultHomeSrs() {
        return defaultHomeSrs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  defaultSrs  the defaultSrs to set
     */
    public void setDefaultHomeSrs(final Crs defaultSrs) {
        this.defaultHomeSrs = defaultSrs;
    }
}
