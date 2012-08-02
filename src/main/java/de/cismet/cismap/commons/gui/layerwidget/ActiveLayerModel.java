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

import java.net.MalformedURLException;
import java.net.URL;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import javax.swing.JTree;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.ConvertableToXML;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.Debug;
import de.cismet.cismap.commons.LayerInfoProvider;
import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.MappingModelListener;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.XMLObjectFactory;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.DocumentFeatureService;
import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.SimpleUpdateablePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.raster.wms.SlidableWMSServiceLayerGroup;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupportingRasterLayer;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilitiesFactory;

import de.cismet.security.AccessHandler;
import de.cismet.security.WebAccessManager;

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
    private CyclicBarrier currentBarrier = null;
    private TreeTableModelAdapter tableModel;
    private boolean initalLayerConfigurationFromServer = false;
    private HashMap<String, Element> masterLayerHashmap = new HashMap<String, Element>();
    private Crs defaultHomeSrs;

    //~ Constructors -----------------------------------------------------------

    /**
     * Erstellt eine neue ActiveLayerModel-Instanz.
     */
    public ActiveLayerModel() {
        super("Root"); // NOI18N
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
     * Fuegt dem Layer-Vektor einen neuen RetrievalServiceLayer hinzu.
     *
     * @param   layer  neuer RetrievalServiceLayer
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    @Override
    public synchronized void addLayer(final RetrievalServiceLayer layer) {
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
        final RetrievalServiceLayer currentLayer = layer;
        final ActiveLayerEvent ale = new ActiveLayerEvent();
        ale.setLayer(currentLayer);
        CismapBroker.getInstance().fireLayerAdded(ale);
        if (layer instanceof WMSServiceLayer) {
            final WMSServiceLayer wmsLayer = ((WMSServiceLayer)layer);
            ale.setCapabilities(wmsLayer.getWmsCapabilities());
            if (wmsLayer.getBackgroundColor() == null) {
                wmsLayer.setBackgroundColor(preferredBGColor);
            }
            if (wmsLayer.getExceptionsFormat() == null) {
                wmsLayer.setExceptionsFormat(preferredExceptionsFormat);
            }
            if (wmsLayer.getImageFormat() == null) {
                wmsLayer.setImageFormat(preferredRasterFormat);
            }
            wmsLayer.setSrs(srs.getCode());
        }
        if (layer instanceof SlidableWMSServiceLayerGroup) {
            ((SlidableWMSServiceLayerGroup)layer).setSrs(srs.getCode());
        }

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

        if (layer instanceof MapService) {
            fireMapServiceAdded(((MapService)layer));
        } else {
            log.warn("fireMapServiceAdded event not fired, layer is no MapService:" + layer); // NOI18N
        }

        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("RetrievalListener added on layer '" + currentLayer.getName() + "'"); // NOI18N
            }
        }
        // Das eigentliche Hinzufuegen des neuen Layers
        layers.add(layer);
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("layer '" + currentLayer.getName() + "' added"); // NOI18N
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
        if (layer instanceof RetrievalServiceLayer) {
            removeLayer((RetrievalServiceLayer)layer);
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
     * @param  treePath  DOCUMENT ME!
     */
    public void disableLayer(final TreePath treePath) {
        final Object layer = treePath.getLastPathComponent();
        if (layer instanceof RetrievalServiceLayer) {
            final RetrievalServiceLayer wmsServiceLayer = ((RetrievalServiceLayer)layer);
            wmsServiceLayer.setEnabled(!wmsServiceLayer.isEnabled());
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
                l.getPNode().moveInFrontOf(((MapService)layers.get(pos)).getPNode());
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

                l.getPNode().moveInBackOf(((MapService)layers.get(pos)).getPNode());
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
        } else {
            return 0;
        }
    }

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
        } else {
            return null;
        }
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

    @Override
    public void removeMappingModelListener(final de.cismet.cismap.commons.MappingModelListener mml) {
        mappingModelListeners.remove(mml);
    }

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
            if (layer instanceof WMSServiceLayer) {
                ((WMSServiceLayer)layer).setSrs(srs.getCode());
            }
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
            this.fireTreeNodesChanged(this, new Object[] { root, sl }, null, null);
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
    protected void fireMapServiceAdded(final MapService rasterService) {
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
    protected void fireMapServiceRemoved(final MapService rasterService) {
        final Iterator it = mappingModelListeners.iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if (o instanceof MappingModelListener) {
                final MappingModelListener mml = (MappingModelListener)o;
                mml.mapServiceRemoved(rasterService);
            }
        }
    }

    // Configurable
    @Override
    public Element getConfiguration() throws NoWriteError {
        final Element conf = new Element("cismapActiveLayerConfiguration"); // NOI18N
        // Zuerst alle RasterLayer
        final Iterator<Integer> it = getMapServices().keySet().iterator();
        final Element allLayerConf = new Element("Layers"); // Sollte irgendwann zu "Layers" umgewandelt werden
                                                            // (TODO)//NOI18N

        int counter = 0;
        while (it.hasNext()) {
            final MapService service = getMapServices().get(it.next());
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("saving configuration of service: '" + service + "'"); // NOI18N
                }
            }

            if (service instanceof ServiceLayer) {
                // es reicht völlig aus, die Layer Position erst beim Speichern der
                // Konfiugration zu setzten und nicht bei jedem Aufruf von moveÖayerUp/Down.
                ((ServiceLayer)service).setLayerPosition(counter);
            }

            if (service instanceof SimpleFeatureSupportingRasterLayer) {
            } else if (service instanceof WMSServiceLayer) {
                final Element layerConf = ((WMSServiceLayer)service).getElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof SimpleWMS) {
                final Element layerConf = ((SimpleWMS)service).getElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof WebFeatureService) {
                final Element layerConf = ((WebFeatureService)service).toElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof DocumentFeatureService) {
                final Element layerConf = ((DocumentFeatureService)service).toElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof SimplePostgisFeatureService) {
                final Element layerConf = ((SimplePostgisFeatureService)service).toElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof SimpleUpdateablePostgisFeatureService) {
                final Element layerConf = ((SimpleUpdateablePostgisFeatureService)service).toElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof SlidableWMSServiceLayerGroup) {
                final Element layerConf = ((SlidableWMSServiceLayerGroup)service).toElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof ConvertableToXML) {
                final Element layerConf = ((ConvertableToXML)service).toElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else {
                log.warn("saving configuration not supported by service: " + service); // NOI18N
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
     * @param   layerelement  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getKeyforLayerElement(final Element layerelement) {
        final String keyString = null;
        if (layerelement != null) {
            try {
                if (layerelement.getName().equals("WMSServiceLayer")) {                                            // NOI18N
                    final WMSServiceLayer wmsServiceLayer = new WMSServiceLayer(
                            layerelement,
                            new HashMap<String, WMSCapabilities>());
                    return getKeyForRetrievalService(wmsServiceLayer);
                } else if (layerelement.getName().equals(WebFeatureService.WFS_FEATURELAYER_TYPE)) {
                    final WebFeatureService wfs = new WebFeatureService(layerelement);
                    return getKeyForRetrievalService(wfs);
                } else if (layerelement.getName().equals("DocumentFeatureServiceLayer")) {                         // NOI18N
                    log.warn("Sollte nicht vorkommen. Die sollten alle von der XMLObjectFactory geladen werden."); // NOI18N
                } else if (layerelement.getName().equals("simpleWms")) {                                           // NOI18N
                    final SimpleWMS simpleWMS = new SimpleWMS(layerelement);
                    return getKeyForRetrievalService(simpleWMS);
                } else if (layerelement.getName().equals("simplePostgisFeatureService")) {                         // NOI18N
                    SimplePostgisFeatureService spfs;
                    if ((layerelement.getAttributeValue("updateable") != null)
                                && layerelement.getAttributeValue("updateable").equals("true")) {                  // NOI18N
                        spfs = new SimpleUpdateablePostgisFeatureService(layerelement);
                    } else {
                        spfs = new SimplePostgisFeatureService(layerelement);
                    }
                    return getKeyForRetrievalService(spfs);
                } else if (layerelement.getName().equals(SlidableWMSServiceLayerGroup.XML_ELEMENT_NAME)) {         // NOI18N
                    final SlidableWMSServiceLayerGroup slidableWms = new SlidableWMSServiceLayerGroup(
                            layerelement,
                            new HashMap<String, WMSCapabilities>());

                    // the listener and the internal widget should be removed by the slidable wms object
                    final ActiveLayerEvent event = new ActiveLayerEvent();
                    event.setLayer(slidableWms);
                    slidableWms.layerRemoved(event);

                    return getKeyForRetrievalService(slidableWms);
                } else {
                    final RetrievalServiceLayer layer = (RetrievalServiceLayer)XMLObjectFactory
                                .restoreObjectfromElement(layerelement);
                    return getKeyForRetrievalService(layer);
                }
            } catch (Exception ex) {
                log.error("Konnte keinen Key für das layerelement erstellen", ex);
            }
        }
        return null;
    }
    /**
     * Same as above if this is done directly by the retrievalservicelayer no instanceof is needed.
     *
     * @param   layer  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getKeyForRetrievalService(final RetrievalServiceLayer layer) {
        final String keyString = null;
        if (layer != null) {
            try {
                if (layer instanceof WMSServiceLayer) {                     // NOI18N
                    final WMSServiceLayer wmsServiceLayer = (WMSServiceLayer)layer;
                    return wmsServiceLayer.getName() + "#" + wmsServiceLayer.getCapabilitiesUrl();
                } else if (layer instanceof WebFeatureService) {
                    final WebFeatureService wfs = (WebFeatureService)layer;
                    return wfs.getName() + "#" + wfs.getHostname();
                } else if (layer instanceof DocumentFeatureService) {       // NOI18N
                    final DocumentFeatureService dfs = (DocumentFeatureService)layer;
                    return dfs.getName() + dfs.getDocumentURI();
                } else if (layer instanceof SimpleWMS) {                    // NOI18N
                    final SimpleWMS simpleWMS = (SimpleWMS)layer;
                    return simpleWMS.getName() + "#" + simpleWMS.getGmUrl().getUrlTemplate();
                } else if (layer instanceof SimplePostgisFeatureService) {  // NOI18N
                    final SimplePostgisFeatureService spfs = (SimplePostgisFeatureService)layer;
                    return spfs.getName() + "#" + spfs.getConnectionInfo().getUrl();
                } else if (layer instanceof SlidableWMSServiceLayerGroup) { // NOI18N
                    final SlidableWMSServiceLayerGroup wms = (SlidableWMSServiceLayerGroup)layer;
                    return wms.getName() + "#" + wms.getName();
                } else {
                    final RetrievalServiceLayer rsl = (RetrievalServiceLayer)layer;
                    return rsl.getName() + "#" + rsl.getClass();
                }
            } catch (Exception ex) {
                log.error("Konnte keinen Key für das layerelement erstellen", ex);
            }
        }
        return null;
    }

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
                final Element[] orderedLayers = orderLayers(layersElement);
                for (final Element curLayerElement : orderedLayers) {
                    final String curKeyString = getKeyforLayerElement(curLayerElement);
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

    @Override
    public synchronized void configure(final Element e) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("ActiveLayerModel configure(" + e.getName() + ")");  // NOI18N
            }
        }
        try {
            final Element conf = e.getChild("cismapActiveLayerConfiguration"); // NOI18N
            final Vector<String> links = LayerWidget.getCapabilities(conf, new Vector<String>());
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("Capabilties links: " + links);                  // NOI18N
                }
            }
            // Laden der Capabilities vom Server und Speichern in einer HashMap<String url,Capabilities>;
            final HashMap<String, WMSCapabilities> capabilities = new HashMap<String, WMSCapabilities>();

            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("vor CyclicBarrier"); // NOI18N
                }
            }

            if (links.size() > 0) {
                // Das Runnable Objekt wird ausgef\u00FChrt wenn alle Capabilities geladen worden sind oder ein Fehler
                // aufgetreten ist
                if (currentBarrier != null) {
                    if (DEBUG) {
                        if (log.isDebugEnabled()) {
                            log.debug("reseting cyclicBarrier"); // NOI18N
                        }
                    }
                    currentBarrier.reset();
                }

                currentBarrier = new CyclicBarrier(links.size(), new Runnable() {

                            @Override
                            public void run() {
                                createLayers(conf, capabilities);
                            }
                        });

                // Zuerst werden alle Capabilities geladen und in eine HashMap gesteckt
                // Ist das Laden beednet wird das dem Barrier durch ein await() gesagt
                for (final String link : links) {
                    final Thread retrieval = new Thread() {

                            @Override
                            public void run() {
                                URL getCapURL = null;
                                try {
//                                InputStreamReader reader = null;
                                    getCapURL = new URL(link);
                                    final URL finalPostUrl = (link.indexOf('?') != -1)
                                        ? new URL(link.substring(0, link.indexOf('?'))) : new URL(link);

//              OGCWMSCapabilitiesFactory capFact = new OGCWMSCapabilitiesFactory();
                                    final CismapBroker broker = CismapBroker.getInstance();
//                                try {
//                                    if(DEBUG)log.debug("Layer Widget: Creating WMScapabilities for URL: " + getCapURL.toString());
//                                    reader = HttpAuthentication.getInputStreamReaderFromURL(CismapBroker.getInstance().getMappingComponent(), getCapURL);
//                                } catch (AuthenticationCanceledException ex) {
//                                    log.warn(ex);
//                                    String title = CismapBroker.getInstance().getProperty(getCapURL.toString());
//
//                                    if (title != null) {
//                                        JXErrorDialog.showDialog(CismapBroker.getInstance().getMappingComponent(),
//                                                "Authenfication failed!"),
//                                                "The authentication was canceled.\nAll current layers from server\n") +
//                                                "\"" +
//                                                title +
//                                                "\" " +
//                                                "were removed"));//NOI18N
//                                    } else {
//                                        title = getCapURL.toString();
//                                        if (title.startsWith("http://") && title.length() > 21) {
//                                            title = title.substring(7, 21) + "...";
//                                        } else if (title.length() > 14) {
//                                            title = title.substring(0, 14) + "...";
//                                        }
//                                        JXErrorDialog.showDialog(CismapBroker.getInstance().getMappingComponent(),
//                                                "Authenfication failed!"),
//                                                "The authentication was canceled.\nAll current layers from server\n") +
//                                                "\"" + title + "\" " +
//                                                "were removed"));
//                                    }
//                                }
                                    // ToDo Probleme mit WFS wird aber denke ich nicht gebraucht
                                    if (DEBUG) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("read WMSCapabilities for " + finalPostUrl);                 // NOI18N
                                        }
                                    }
                                    final WMSCapabilitiesFactory capFact = new WMSCapabilitiesFactory();
                                    if (link.toLowerCase().contains("service=wss")) {                              // NOI18N
                                        try {
                                            if (DEBUG) {
                                                if (log.isDebugEnabled()) {
                                                    log.debug("WSS Capabilties Link added");                       // NOI18N
                                                }
                                            }
                                            final URL url = new URL(link.substring(0, link.indexOf('?')));
                                            if (DEBUG) {
                                                if (log.isDebugEnabled()) {
                                                    log.debug("URL of the WSS: " + url.toString());                // NOI18N
                                                }
                                            }
                                            if (!WebAccessManager.getInstance().isHandlerForURLRegistered(url)) {
                                                WebAccessManager.getInstance()
                                                        .registerAccessHandler(
                                                            url,
                                                            AccessHandler.ACCESS_HANDLER_TYPES.WSS);
                                            } else {
                                                if (DEBUG) {
                                                    if (log.isDebugEnabled()) {
                                                        log.debug("Handler is already registered");                // NOI18N
                                                    }
                                                }
                                            }
                                        } catch (MalformedURLException ex) {
                                            log.error("Url is not wellformed no wss authentication possible", ex); // NOI18N
                                        }
                                    }
                                    // ToDO Langsam
                                    final WMSCapabilities cap = capFact.createCapabilities(link);
//ToDo funktionalität abgeschaltet steckt zur zeit in CismetGUICommons --> refactoring
//                                broker.addHttpCredentialProviderCapabilities(cap, broker.getHttpCredentialProviderURL(getCapURL));
//                                if (broker.isServerSecuredByPassword(cap)) {
//                                    broker.addProperty(getCapURL.toString(), cap.getCapability().getLayer().getTitle());
//                                }
                                    capabilities.put(link, cap);
                                } catch (Exception ex) {
                                    if (DEBUG) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("Exception for URL: " + link, ex);       // NOI18N
                                        }
                                    }
                                    log.warn("Error while retrieving Capabilities" + ":", ex); // NOI18N
                                }
                                try {
                                    currentBarrier.await();
                                } catch (InterruptedException ex) {
                                    log.warn("Thread was interrupted TODO CUSTOMIZE TEXT" + ":", ex); // NOI18N
                                } catch (BrokenBarrierException ex) {
                                    log.warn("No layers available TODO CUSTOMIZE TEXT" + ":", ex); // NOI18N
                                }
                            }
                        };
                    retrieval.setPriority(Thread.NORM_PRIORITY);
                    retrieval.start();
                }
            } else {
                if (DEBUG) {
                    if (log.isDebugEnabled()) {
                        log.debug("No Barrier");                                               // NOI18N
                    }
                }
                createLayers(conf, capabilities);
            }
        } catch (Throwable ex) {
            log.error("Error during the configuration of the ActiveLayerModell", ex);          // NOI18N
        }
    }

    /**
     * Layer neu anordnene entweder nach dem layerPosition Attribut (wenn vorhanden) oder nach der Tag-Reihenfolge in
     * der XML Config.
     *
     * @param   layersElement  DOCUMENT ME!
     *
     * @return  sortierte Liste
     */
    private Element[] orderLayers(final Element layersElement) {
        final List<Element> layerElements = layersElement.getChildren();
        final Element[] orderedLayerElements = new Element[layerElements.size()];

        int i = 0;
        for (final Element layerElement : layerElements) {
            int layerPosition = -1;
            final Attribute layerPositionAttr = layerElement.getAttribute("layerPosition"); // NOI18N
            if (layerPositionAttr != null) {
                try {
                    layerPosition = layerPositionAttr.getIntValue();
                } catch (Exception e) {
                }
            }

            if ((layerPosition < 0) || (layerPosition >= orderedLayerElements.length)) {
                log.warn("layer position of layer #" + i + " (" + layerElement.getName()
                            + ") not set or invalid, setting to " + i); // NOI18N
                layerPosition = i;
            }

            if (orderedLayerElements[layerPosition] != null) {
                log.warn("conflicting layer position " + layerPosition + ": '" + layerElement.getName() + "' vs '"
                            + orderedLayerElements[layerPosition].getName() + "'");                            // NOI18N
                for (int j = 0; j < orderedLayerElements.length; j++) {
                    if (orderedLayerElements[j] == null) {
                        orderedLayerElements[j] = layerElement;
                        break;
                    }
                }
            } else {
                orderedLayerElements[layerPosition] = layerElement;
            }
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug(i + " layer '" + layerElement.getName() + "' set to position " + layerPosition); // NOI18N
                }
            }
            i++;
        }

        return orderedLayerElements;
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
                log.error("no vlaid layers element found");                                            // NOI18N
                return;
            }
        }
        log.info("restoring " + layerElement.getChildren().size() + " layers from xml configuration"); // NOI18N
        final Element[] orderedLayers = orderLayers(layerElement);
        for (final Element element : orderedLayers) {
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("trying to add Layer '" + element.getName() + "'");                      // NOI18N
                }
            }
            final String currentKeyString = getKeyforLayerElement(element);
            if (isInitalLayerConfigurationFromServer()
                        && !masterLayerHashmap.containsKey(currentKeyString)) {
                log.info("Layer in Serverkonfiguration nicht vorhanden, wird nicht hinzugefügt KeyString: "
                            + currentKeyString);
                continue;
            }
            try {
                if (element.getName().equals("WMSServiceLayer")) {                                     // NOI18N
                    final WMSServiceLayer wmsServiceLayer = new WMSServiceLayer(element, capabilities);
                    if (wmsServiceLayer.getWMSLayers().size() > 0) {
                        if (EventQueue.isDispatchThread()) {
                            log.fatal("InvokeLater in EDT");                                           // NOI18N
                        }
                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        log.info("addLayer WMSServiceLayer (" + wmsServiceLayer.getName() + ")"); // NOI18N
                                        addLayer(wmsServiceLayer);
                                    } catch (IllegalArgumentException schonVorhanden) {
                                        log.warn(
                                            "Layer WMSServiceLayer '"
                                                    + wmsServiceLayer.getName()
                                                    + "' already existed. Do not add the Layer. \n"
                                                    + schonVorhanden.getMessage());                               // NOI18N
                                    }
                                }
                            });
                    }
                } else if (element.getName().equals(WebFeatureService.WFS_FEATURELAYER_TYPE)) {
                    final WebFeatureService wfs = new WebFeatureService(element);
                    if (EventQueue.isDispatchThread()) {
                        log.fatal("InvokeLater in EDT");                                                          // NOI18N
                    }
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    log.info(
                                        "addLayer "
                                                + WebFeatureService.WFS_FEATURELAYER_TYPE
                                                + " ("
                                                + wfs.getName()
                                                + ")");                         // NOI18N
                                    addLayer(wfs);
                                } catch (IllegalArgumentException schonVorhanden) {
                                    log.warn(
                                        "Layer "
                                                + WebFeatureService.WFS_FEATURELAYER_TYPE
                                                + " '"
                                                + wfs.getName()
                                                + "' already existed. Do not add the Layer. \n"
                                                + schonVorhanden.getMessage()); // NOI18N
                                }
                            }
                        });
                } else if (element.getName().equals("DocumentFeatureServiceLayer")) { // NOI18N
                    log.error("DocumentFeatureServiceLayer not supported");     // NOI18N
                    // throw new UnsupportedOperationException("DocumentFeatureServiceLayer not supported");
                    // if(DEBUG)log.debug("DocumentFeatureLayer von ConfigFile wird hinzugefügt"); URI documentURI =
                    // new URI(element.getChildText("documentURI").trim()); File testFile = new File(documentURI); if
                    // (!testFile.exists()) { log.warn("Das Angebene Document(" + testFile.getAbsolutePath() + ")
                    // exisitiert nicht ---> abbruch, es wird kein Layer angelegt"); continue; }
                    //
                    // final GMLFeatureService gfs = new GMLFeatureService(element); //langsam sollte nicht im EDT
                    // ausgeführt werden final DocumentFeatureService dfs =
                    // DocumentFeatureServiceFactory.createDocumentFeatureService(element); //final
                    // ShapeFileFeatureService sfs = new ShapeFileFeatureService(element); EventQueue.invokeLater(new
                    // Runnable() {
                    //
                    // @Override public void run() { try { log.info("addLayer DocumentFeatureServiceLayer (" +
                    // dfs.getName() + ")"); addLayer(dfs); } catch (IllegalArgumentException schonVorhanden) {
                    // log.warn("Layer DocumentFeatureServiceLayer '" + dfs.getName() + "' already existed. Do not
                    // add the Layer. \n" + schonVorhanden.getMessage()); } } });
                } else if (element.getName().equals("simpleWms")) { // NOI18N
                    final SimpleWMS simpleWMS = new SimpleWMS(element);
                    if (EventQueue.isDispatchThread()) {
                        log.fatal("InvokeLater in EDT");            // NOI18N
                    }
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                log.info("addLayer SimpleWMS (" + simpleWMS.getName() + ")"); // NOI18N
                                try {
                                    addLayer(simpleWMS);
                                } catch (IllegalArgumentException schonVorhanden) {
                                    log.warn(
                                        "Layer SimpleWMS '"
                                                + simpleWMS.getName()
                                                + "' already existed. Do not add the Layer. \n"
                                                + schonVorhanden.getMessage());               // NOI18N
                                }
                            }
                        });
                } else if (element.getName().equals(SlidableWMSServiceLayerGroup.XML_ELEMENT_NAME)) { // NOI18N
                    final SlidableWMSServiceLayerGroup wms = new SlidableWMSServiceLayerGroup(element, capabilities);
                    if (EventQueue.isDispatchThread()) {
                        log.fatal("InvokeLater in EDT");                                      // NOI18N
                    }
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                log.info("addLayer SlidableWMSServiceLayerGroup (" + wms.getName() + ")"); // NOI18N
                                try {
                                    addLayer(wms);
                                } catch (IllegalArgumentException schonVorhanden) {
                                    log.warn(
                                        "Layer SimpleWMS '"
                                                + wms.getName()
                                                + "' already existed. Do not add the Layer. \n"
                                                + schonVorhanden.getMessage());                            // NOI18N
                                }
                            }
                        });
                } else if (element.getName().equals("simplePostgisFeatureService")) {                      // NOI18N
                    SimplePostgisFeatureService spfs;
                    if ((element.getAttributeValue("updateable") != null)
                                && element.getAttributeValue("updateable").equals("true")) {               // NOI18N
                        spfs = new SimpleUpdateablePostgisFeatureService(element);
                    } else {
                        spfs = new SimplePostgisFeatureService(element);
                    }

                    final SimplePostgisFeatureService simplePostgisFeatureService = spfs;
                    if (EventQueue.isDispatchThread()) {
                        log.fatal("InvokeLater in EDT"); // NOI18N
                    }
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    log.info(
                                        "addLayer SimplePostgisFeatureService ("
                                                + simplePostgisFeatureService.getName()
                                                + ")");                         // NOI18N
                                    addLayer(simplePostgisFeatureService);
                                } catch (IllegalArgumentException schonVorhanden) {
                                    log.warn(
                                        "Layer SimplePostgisFeatureService '"
                                                + simplePostgisFeatureService.getName()
                                                + "' already existed. Do not add the Layer. \n"
                                                + schonVorhanden.getMessage()); // NOI18N
                                }
                            }
                        });
                } else {
                    try {
                        if (DEBUG) {
                            if (log.isDebugEnabled()) {
                                log.debug("restoring generic layer configuration from xml element '" + element
                                            .getName() + "'");                  // NOI18N
                            }
                        }
                        final RetrievalServiceLayer layer = (RetrievalServiceLayer)XMLObjectFactory
                                    .restoreObjectfromElement(element);

                        if (EventQueue.isDispatchThread()) {
                            log.fatal("InvokeLater in EDT"); // NOI18N
                        }
                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        log.info("addLayer generic layer configuration (" + layer.getName() + ")"); // NOI18N
                                        addLayer(layer);
                                    } catch (IllegalArgumentException schonVorhanden) {
                                        log.warn(
                                            "Layer SimplePostgisFeatureService '"
                                                    + layer.getName()
                                                    + "' already existed. Do not add the Layer. \n"
                                                    + schonVorhanden.getMessage());                                 // NOI18N
                                    }
                                }
                            });
                    } catch (Throwable t) {
                        log.error("unsupported xml configuration, layer '" + element.getName()
                                    + "' could not be created: \n" + t.getLocalizedMessage(),
                            t);                                                                                     // NOI18N
                    }
                }
            } catch (Throwable t) {
                log.error("Layer layer '" + element.getName() + "' could not be created: \n" + t.getMessage(), t);  // NOI18N
            }
        }
    }

    @Deprecated
    @Override
    public java.util.TreeMap getRasterServices() {
        return getMapServices();
    }

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
