/*
 * ActiveLayerModel.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 8. November 2005, 15:34
 *
 */
package de.cismet.cismap.commons.gui.layerwidget;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.ConvertableToXML;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.Debug;
import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.MappingModelListener;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.tools.gui.Static2DTools;
import de.cismet.tools.gui.treetable.AbstractTreeTableModel;
import java.util.TreeMap;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.XMLObjectFactory;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.DocumentFeatureService;
import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.SimpleUpdateablePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupportingRasterLayer;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.security.AccessHandler;
import de.cismet.security.WebAccessManager;
import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;
import de.cismet.tools.gui.treetable.TreeTableModel;
import de.cismet.tools.gui.treetable.TreeTableModelAdapter;
import java.awt.EventQueue;
import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilitiesFactory;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class ActiveLayerModel extends AbstractTreeTableModel implements MappingModel, Configurable {

    protected final static boolean DEBUG = Debug.DEBUG;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    Vector layers = new Vector();
    Vector<MappingModelListener> mappingModelListeners = new Vector();
    BoundingBox initialBoundingBox;
    private HashMap<String, XBoundingBox> homes = new HashMap<String, XBoundingBox>();
    private Crs srs;
    private String preferredRasterFormat;
    private String preferredTransparentPref;
    private String preferredBGColor;
    private String preferredExceptionsFormat;
    private CyclicBarrier currentBarrier = null;
    private TreeTableModelAdapter tableModel;
    private Crs defaultSrs;

    /**
     * Erstellt eine neue ActiveLayerModel-Instanz.
     */
    public ActiveLayerModel() {
        super("Root");//NOI18N
        setDefaults();
        this.tableModel = new TreeTableModelAdapter(this, new JTree());
    }

    private void setDefaults() {
        //srs="EPSG:4326";
        preferredRasterFormat = "image/png";//NOI18N
        preferredBGColor = "0xF0F0F0";//NOI18N
        //preferredExceptionsFormat="application/vnd.ogc.se_inimage";
        preferredExceptionsFormat = "application/vnd.ogc.se_xml";//NOI18N
        initialBoundingBox = new BoundingBox(-180, -90, 180, 90);
//        srs="EPSG:31466";
//        preferredRasterFormat="image/png";
//        preferredBGColor="0xF0F0F0";
//        preferredExceptionsFormat="application/vnd.ogc.se_inimage";
//        initialBoundingBox=new BoundingBox(2569442.79,5668858.33,2593744.91,5688416.22);
    }

    /**
     * Fuegt dem Layer-Vektor einen neuen RetrievalServiceLayer hinzu.
     * @param layer neuer RetrievalServiceLayer
     */
    @Override
    public synchronized void addLayer(RetrievalServiceLayer layer) {
        if (DEBUG) {
            log.debug("addLayer: " + layer.getName());//NOI18N
        }
        if (layers.contains(layer)) {
            throw new IllegalArgumentException("Layer '" + layer.getName() + "' already exists");//NOI18N
        }
        final RetrievalServiceLayer currentLayer = layer;
        ActiveLayerEvent ale = new ActiveLayerEvent();
        ale.setLayer(currentLayer);
        CismapBroker.getInstance().fireLayerAdded(ale);
        if (layer instanceof WMSServiceLayer) {
            WMSServiceLayer wmsLayer = ((WMSServiceLayer) layer);
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

        layer.addRetrievalListener(new RetrievalListener() {

            @Override
            public void retrievalStarted(RetrievalEvent e) {
                if (DEBUG) {
                    log.debug(currentLayer.getName() + "[" + e.getRequestIdentifier() + "]: retrievalStarted");//NOI18N
                }
                //currentLayer.setProgress(-1);
                fireProgressChanged(currentLayer);
            }

            @Override
            public void retrievalProgress(RetrievalEvent e) {
                
                //currentLayer.setProgress((int) (e.getPercentageDone() * 100));
                fireProgressChanged(currentLayer);
            }

            @Override
            public void retrievalComplete(RetrievalEvent e) {
                if (DEBUG) {
                    log.debug(currentLayer.getName() + "[" + e.getRequestIdentifier() + "]: retrievalComplete");//NOI18N
                }
                //currentLayer.setProgress(100);
                fireProgressChanged(currentLayer);
                if (e.isHasErrors()) {
                    retrievalError(e);
                } else {
                    currentLayer.setErrorObject(null);
                }
            }

            @Override
            public void retrievalAborted(RetrievalEvent e) {
                if (DEBUG) {
                    log.debug(currentLayer.getName() + "[" + e.getRequestIdentifier() + "]: retrievalAborted");//NOI18N
                }
                //currentLayer.setProgress(0);
                fireProgressChanged(currentLayer);
            }

            @Override
            public void retrievalError(RetrievalEvent e) {
                if (DEBUG) {
                    log.warn(currentLayer.getName() + "[" + e.getRequestIdentifier() + "]: retrievalError: " + e.getErrorType() + " (hasErrors=" + currentLayer.hasErrors() + ")");//NOI18N
                }
                //currentLayer.setProgress(0);

                fireProgressChanged(currentLayer);

                if (e.getRetrievedObject() != null) {
                    Object errorObject = e.getRetrievedObject();
                    if (errorObject instanceof Image) {
                        //Static2DTools.scaleImage((Image)errorObject,0.7);
                        Image i = Static2DTools.removeUnusedBorder((Image) errorObject, 5, 0.7);
                        errorObject = i;
                    } else if (e.getRetrievedObject() instanceof String) {
                        String message = (String) e.getRetrievedObject();
//                        message=message.replaceAll("<.*>","");
                        if (e.getErrorType().equals(RetrievalEvent.SERVERERROR)) {
                            errorObject = org.openide.util.NbBundle.getMessage(ActiveLayerModel.class, "ActiveLayerModel.retrievalError(RetrievalEvent).errorObject.servererror", new Object[] {message});//NOI18N
                        } else {
                            errorObject = org.openide.util.NbBundle.getMessage(ActiveLayerModel.class, "ActiveLayerModel.retrievalError(RetrievalEvent).errorObject.noServererror", new Object[] {});//NOI18N
                        }
                    }// Hier kommt jetzt HTML Fehlermeldung, Internal und XML. Das muss reichen
                    //else if ()

                    currentLayer.setErrorObject(errorObject);
                } else if (DEBUG) {
                    log.warn("no error object supplied");//NOI18N
                }
            }
        });

        if (layer instanceof MapService) {
            fireMapServiceAdded(((MapService) layer));
        } else {
            log.warn("fireMapServiceAdded event not fired, layer is no MapService:" + layer);//NOI18N
        }

        if (DEBUG) {
            log.debug("RetrievalListener added on layer '" + currentLayer.getName() + "'");//NOI18N
        }
        // Das eigentliche Hinzufuegen des neuen Layers
        layers.add(layer);
        if (DEBUG) {
            log.debug("layer '" + currentLayer.getName() + "' added");//NOI18N
        }
        fireTreeStructureChanged(this, new Object[]{
                    root
                }, null, null);
    }

    public void removeAllLayers() {
        Object[] oa = layers.toArray();
        for (int i = 0; i < oa.length; i++) {
            Object elem = oa[i];
            removeLayer(elem, null);
        }
    }

    public void removeLayer(TreePath treePath) {
        Object layer = treePath.getLastPathComponent();
        removeLayer(layer, treePath);
    }

    public void removeLayer(Object layer, TreePath treePath) {
        if (layer instanceof RetrievalServiceLayer) {
            removeLayer((RetrievalServiceLayer) layer);
        } else if (treePath != null && layer instanceof WMSLayer) { //Kinderlayer

            TreePath parentPath = treePath.getParentPath();
            if (parentPath.getLastPathComponent() instanceof WMSServiceLayer) {
                ((WMSServiceLayer) parentPath.getLastPathComponent()).removeLayer((WMSLayer) layer);
            }
            fireTreeStructureChanged(this, new Object[]{
                        root, (WMSServiceLayer) parentPath.getLastPathComponent()
                    }, null, null);
            ActiveLayerEvent ale = new ActiveLayerEvent();
            ale.setLayer((WMSLayer) layer);
            CismapBroker.getInstance().fireLayerRemoved(ale);
        }
    }

    @Override
    public void removeLayer(RetrievalServiceLayer layer) {
        RetrievalServiceLayer wmsServiceLayer = ((RetrievalServiceLayer) layer);
        layers.remove(wmsServiceLayer);
        ActiveLayerEvent ale = new ActiveLayerEvent();
        ale.setLayer(wmsServiceLayer);
        CismapBroker.getInstance().fireLayerRemoved(ale);
        fireTreeStructureChanged(this, new Object[]{
                    root
                }, null, null);
        fireMapServiceRemoved((MapService) wmsServiceLayer);
    }

    public void disableLayer(TreePath treePath) {
        Object layer = treePath.getLastPathComponent();
        if (layer instanceof RetrievalServiceLayer) {
            RetrievalServiceLayer wmsServiceLayer = ((RetrievalServiceLayer) layer);
            wmsServiceLayer.setEnabled(!wmsServiceLayer.isEnabled());
            if (wmsServiceLayer.isEnabled()) {
                wmsServiceLayer.setRefreshNeeded(true);
            }
            fireTreeNodesChanged(this, new Object[]{
                        root
                    }, null, null);
        } else if (layer instanceof WMSLayer) {//Kinderlayer

            TreePath parentPath = treePath.getParentPath();
            ((WMSLayer) layer).setEnabled(!((WMSLayer) layer).isEnabled());
            ((WMSServiceLayer) parentPath.getLastPathComponent()).setRefreshNeeded(true);
            fireTreeNodesChanged(this, new Object[]{
                        root, (WMSServiceLayer) parentPath.getLastPathComponent()
                    }, null, null);
        }
    }

    public void handleVisibility(TreePath treePath) {
        Object layer = treePath.getLastPathComponent();
        if (layer instanceof RetrievalServiceLayer) {
            RetrievalServiceLayer wmsServiceLayer = ((RetrievalServiceLayer) layer);
            wmsServiceLayer.getPNode().setVisible(!wmsServiceLayer.getPNode().getVisible());
            fireTreeNodesChanged(this, new Object[]{
                        root
                    }, null, null);
        }
    }

    public void moveLayerUp(TreePath treePath) {
        Object layer = treePath.getLastPathComponent();
        if (layer instanceof RetrievalServiceLayer) {
            MapService l = (MapService) layer;
            int pos = layers.indexOf(l);
            if (pos + 1 != layers.size()) {
                layers.remove(l);
                layers.add(pos + 1, l);
                l.getPNode().moveInFrontOf(((MapService) layers.get(pos)).getPNode());
                fireTreeStructureChanged(this, new Object[]{
                            root
                        }, new int[]{pos, pos + 1}, new Object[]{layers.get(pos), l});
            }
        } else if (layer instanceof WMSLayer) {
            WMSLayer l = (WMSLayer) layer;
            WMSServiceLayer parent = (WMSServiceLayer) treePath.getParentPath().getLastPathComponent();
            int pos = parent.getWMSLayers().indexOf(l);
            if (pos + 1 != parent.getWMSLayers().size()) {
                parent.getWMSLayers().remove(l);
                parent.getWMSLayers().add(pos + 1, l);
                parent.setRefreshNeeded(true);
                fireTreeStructureChanged(this, new Object[]{
                            root, parent
                        }, null, null);
            }
        }
    }

    public void moveLayerDown(TreePath treePath) {
        Object layer = treePath.getLastPathComponent();
        if (layer instanceof MapService) {
            MapService l = (MapService) layer;
            int pos = layers.indexOf(l);
            if (pos != 0) {
                layers.remove(l);
                layers.add(pos - 1, l);

                l.getPNode().moveInBackOf(((MapService) layers.get(pos)).getPNode());
                fireTreeStructureChanged(this, new Object[]{
                            root
                        }, new int[]{pos - 1, pos}, new Object[]{l, layers.get(pos)});
            }
        } else if (layer instanceof WMSLayer) {
            WMSLayer l = (WMSLayer) layer;
            WMSServiceLayer parent = (WMSServiceLayer) treePath.getParentPath().getLastPathComponent();
            int pos = parent.getWMSLayers().indexOf(l);
            if (pos != 0) {
                parent.getWMSLayers().remove(l);
                parent.getWMSLayers().add(pos - 1, l);
                parent.setRefreshNeeded(true);
                fireTreeStructureChanged(this, new Object[]{
                            root, parent
                        }, null, null);
            }
        }
    }

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 1:
                return TreeTableModel.class;
//            case 2:
//                return Boolean.class;
            default:
                return Object.class;
        }
    }

    /**
     * Returns the number of children of <code>parent</code>.
     * Returns 0 if the node
     * is a leaf or if it has no children.  <code>parent</code> must be a node
     * previously obtained from this data source.
     *
     * @param   parent  a node in the tree, obtained from this data source
     * @return  the number of children of the node <code>parent</code>
     */
    @Override
    public int getChildCount(Object parent) {
        if (parent == super.getRoot()) {
            return layers.size();
        }
        if (parent instanceof WMSServiceLayer) {
            WMSServiceLayer wmsServiceLayer = (WMSServiceLayer) parent;
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
     * Returns the value to be displayed for node <code>node</code>,
     * at column number <code>column</code>.
     */
    @Override
    public Object getValueAt(Object node, int column) {
        if (node instanceof RetrievalServiceLayer) {
            return ((RetrievalServiceLayer) node);
        } else if (node instanceof WMSLayer) {
            return ((WMSLayer) node);
        } else {
            return "ROOT 0";//NOI18N
        }
    }

    /**
     * Returns the name for column number <code>column</code>.
     */
    @Override
    public String getColumnName(int column) {
        switch (column) {
            case (0):
                return " ";//NOI18N
            case (1):
                return org.openide.util.NbBundle.getMessage(ActiveLayerModel.class, "ActiveLayerModel.getColumnName(int).return.layer");//NOI18N
            case (2):
                return org.openide.util.NbBundle.getMessage(ActiveLayerModel.class, "ActiveLayerModel.getColumnName(int).return.style");//NOI18N
            case (3):
                return org.openide.util.NbBundle.getMessage(ActiveLayerModel.class, "ActiveLayerModel.getColumnName(int).return.info");//NOI18N
            case (4):
                return org.openide.util.NbBundle.getMessage(ActiveLayerModel.class, "ActiveLayerModel.getColumnName(int).return.fortschrittTransparent");//NOI18N
            case (5):
                return "";//NOI18N
            default:
                return "";//NOI18N
        }
    }

    /**
     * Returns the child of <code>parent</code> at index <code>index</code>
     * in the parent's
     * child array.  <code>parent</code> must be a node previously obtained
     * from this data source. This should not return <code>null</code>
     * if <code>index</code>
     * is a valid index for <code>parent</code> (that is <code>index >= 0 &&
     * index < getChildCount(parent</code>)).
     *
     * @param   parent  a node in the tree, obtained from this data source
     * @return  the child of <code>parent</code> at index <code>index</code>
     */
    @Override
    public Object getChild(Object parent, int index) {
        //Hier wird die Reihenfolge festgelegt
        if (parent == root) {
            return layers.get(layers.size() - 1 - index);
        } else if (parent instanceof WMSServiceLayer) {
            return ((WMSServiceLayer) parent).getWMSLayers().get(((WMSServiceLayer) parent).getWMSLayers().size() - 1 - index);
        } else {
            return null;
        }
    }

    /**
     * Returns the number ofs availible column.
     */
    @Override
    public int getColumnCount() {
        return 6;
    }

    /**
     * By default, make the column with the Tree in it the only editable one.
     *  Making this column editable causes the JTable to forward mouse
     *  and keyboard events in the Tree column to the underlying JTree.
     */
    @Override
    public boolean isCellEditable(Object node, int column) {
        switch (column) {
            case 0:
                return true;
            case 1:
                if (node instanceof WMSServiceLayer && ((WMSServiceLayer) node).getWMSLayers().size() > 1) {
                    return true;
                } else {
                    return false;
                }
            case 2:
                if (node instanceof WMSServiceLayer && ((WMSServiceLayer) node).getWMSLayers().size() > 1) {
                    return false;
                } else if (node instanceof WMSServiceLayer && ((WMSServiceLayer) node).getWMSLayers().size() == 1 &&
                        ((WMSLayer) ((WMSServiceLayer) node).getWMSLayers().get(0)).getOgcCapabilitiesLayer().getStyles().length > 1) {
                    return true;
                } else if (node instanceof WMSLayer && ((WMSLayer) node).getOgcCapabilitiesLayer().getStyles().length > 1) {
                    return true;
                } else if (node instanceof AbstractFeatureService) {
                    return true;
                } else {
                    return false;
                }
            case 3:
                if (node instanceof WMSServiceLayer && ((WMSServiceLayer) node).getWMSLayers().size() > 1) {
                    return false;
                } else {
                    if (node instanceof WMSLayer) {
                        return ((WMSLayer) node).getOgcCapabilitiesLayer().isQueryable();
                    } else if (node instanceof WMSServiceLayer && ((WMSServiceLayer) node).getWMSLayers().get(0) instanceof WMSLayer) {
                        return ((WMSLayer) (((WMSServiceLayer) node).getWMSLayers().get(0))).getOgcCapabilitiesLayer().isQueryable();
                    } else {
                        return false;
                    }
                }
            case 4:
                if (node instanceof RetrievalServiceLayer) {
                    return true;
                } else {
                    return false;
                }
            case 5:
                return true;
        }
        boolean retValue;

        retValue = super.isCellEditable(node, column);

        //return retValue;
        return true;
    }

    @Override
    public void setValueAt(Object aValue, Object node, int column) {
        if (column == 1) {
            if (DEBUG) {
                log.debug("node:" + node);//NOI18N
            }
            if (DEBUG) {
                log.debug("aValue:" + aValue);//NOI18N
            }
            ((WMSServiceLayer) node).setName(aValue.toString());
            this.fireTreeNodesChanged(this, new Object[]{
                        root, node
                    }, null, null);
        } else if (column == 3) {
            //if (aValue instanceof WMSLayer)
        }
        super.setValueAt(aValue, node, column);
    }

    @Override
    public void removeMappingModelListener(de.cismet.cismap.commons.MappingModelListener mml) {
        mappingModelListeners.remove(mml);
    }

    @Override
    public void addMappingModelListener(de.cismet.cismap.commons.MappingModelListener mml) {
        mappingModelListeners.add(mml);
    }

    public java.util.TreeMap<Integer, MapService> getMapServices() {
        Iterator it = layers.iterator();
        TreeMap<Integer, MapService> tm = new TreeMap();
        int counter = 0;
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof MapService) {
                tm.put(new Integer(counter++), (MapService) o);
            } else {
                log.warn("service is not of type MapService: " + o);//NOI18N
            }
        }
        return tm;
    }

    public HashMap getHomeBoundingBoxes() {
        return homes;
    }

    public void addHome(XBoundingBox xbb) {
        homes.put(xbb.getSrs(), xbb);
    }

    @Override
    public de.cismet.cismap.commons.BoundingBox getInitialBoundingBox() {
        if (srs == null || defaultSrs == null) {
            log.warn("SRS is not set or default SRS is not set, yet");
            return null;
        }
        XBoundingBox homeBox = homes.get(srs.getCode());

        if (homeBox == null) {
            log.debug("No home found for srs " + srs.getCode());

            XBoundingBox defaultBox = homes.get(defaultSrs.getCode());

            if (defaultBox != null) {
                try {
                    CrsTransformer transformer = new CrsTransformer(srs.getCode());
                    homeBox = transformer.transformBoundingBox(defaultBox);
                    homes.put(srs.getCode(), homeBox);
                } catch (Exception e) {
                    log.error("Error while transforming coordinates from " + defaultBox.getSrs() + " to " + srs);
                }
            } else {
                log.debug("No default bunding box (home) found. ");
            }
        }

        if (homeBox == null) {
            log.error("home bounding box == null for srs " + srs.getCode());
        }
        
        return homeBox;
    }

    public Crs getSrs() {
        return srs;
    }

    public void setSrs(Crs srs) {
        if (this.defaultSrs == null) {
            this.defaultSrs = srs;
        }

        for (Object layer : this.layers) {
            if (layer instanceof WMSServiceLayer) {
                ((WMSServiceLayer)layer).setSrs(srs.getCode());
            }
        }

        this.srs = srs;
    }

    public String getPreferredRasterFormat() {
        return preferredRasterFormat;
    }

    public void setPreferredRasterFormat(String preferredRasterFormat) {
        this.preferredRasterFormat = preferredRasterFormat;
    }

    public String getPreferredTransparentPref() {
        return preferredTransparentPref;
    }

    public void setPreferredTransparentPref(String preferredTransparentPref) {
        this.preferredTransparentPref = preferredTransparentPref;
    }

    public String getPreferredBGColor() {
        return preferredBGColor;
    }

    public void setPreferredBGColor(String preferredBGColor) {
        this.preferredBGColor = preferredBGColor;
    }

    public String getPreferredExceptionsFormat() {
        return preferredExceptionsFormat;
    }

    public void setPreferredExceptionsFormat(String preferredExceptionsFormat) {
        this.preferredExceptionsFormat = preferredExceptionsFormat;
    }

    public void fireProgressChanged(ServiceLayer sl) {


        int pos = layers.indexOf(sl);
        if (pos >= 0) {
            this.fireTreeNodesChanged(this, new Object[]{root, sl}, null, null);
            fireTableChanged(null);
        }
    }

    public void fireTableChanged(TableModelEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TableModelListener.class) {
                ((TableModelListener) listeners[i + 1]).tableChanged(e);
            }
        }
    }

    protected void fireMapServiceAdded(MapService rasterService) {
        Vector v = new Vector(mappingModelListeners);
        Iterator it = v.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof MappingModelListener) {
                MappingModelListener mml = (MappingModelListener) o;
                mml.mapServiceAdded(rasterService);
            }
        }
    }

    protected void fireMapServiceRemoved(MapService rasterService) {
        Iterator it = mappingModelListeners.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof MappingModelListener) {
                MappingModelListener mml = (MappingModelListener) o;
                mml.mapServiceRemoved(rasterService);
            }
        }
    }

    //Configurable
    @Override
    public Element getConfiguration() throws NoWriteError {
        Element conf = new Element("cismapActiveLayerConfiguration");//NOI18N
        //Zuerst alle RasterLayer
        Iterator<Integer> it = getMapServices().keySet().iterator();
        Element allLayerConf = new Element("Layers"); //Sollte irgendwann zu "Layers" umgewandelt werden (TODO)//NOI18N

        int counter = 0;
        while (it.hasNext()) {
            MapService service = getMapServices().get(it.next());
            if (DEBUG) {
                log.debug("saving configuration of service: '" + service + "'");//NOI18N
            }

            if (service instanceof ServiceLayer) {
                // es reicht völlig aus, die Layer Position erst beim Speichern der
                // Konfiugration zu setzten und nicht bei jedem Aufruf von moveÖayerUp/Down.
                ((ServiceLayer) service).setLayerPosition(counter);
            }

            if (service instanceof SimpleFeatureSupportingRasterLayer) {
            } else if (service instanceof WMSServiceLayer) {
                Element layerConf = ((WMSServiceLayer) service).getElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof SimpleWMS) {
                Element layerConf = ((SimpleWMS) service).getElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof WebFeatureService) {
                Element layerConf = ((WebFeatureService) service).toElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof DocumentFeatureService) {
                Element layerConf = ((DocumentFeatureService) service).toElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof SimplePostgisFeatureService) {
                Element layerConf = ((SimplePostgisFeatureService) service).toElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof SimpleUpdateablePostgisFeatureService) {
                Element layerConf = ((SimpleUpdateablePostgisFeatureService) service).toElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof ConvertableToXML) {
                Element layerConf = ((ConvertableToXML) service).toElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else {
                log.warn("saving configuration not supported by service: " + service);//NOI18N
            }
        }
        if (counter == 0) {
            //ToDo Why ?
            //throw new NoWriteError();
        }
        conf.addContent(allLayerConf);
        //Alle FeatureService Layer

        //AppFeatureLayer

        return conf;
    }

    @Override
    public void masterConfigure(Element e) {
        //wird alles lokal gespeichert und auch wieder abgerufen
    }

    @Override
    synchronized public void configure(Element e) {
        if (DEBUG) {
            log.debug("ActiveLayerModel configure(" + e.getName() + ")");//NOI18N
        }
        try {
            final Element conf = e.getChild("cismapActiveLayerConfiguration");//NOI18N
            final Vector<String> links = LayerWidget.getCapabilities(conf, new Vector<String>());
            if (DEBUG) {
                log.debug("Capabilties links: " + links);//NOI18N
            }
            //Laden der Capabilities vom Server und Speichern in einer HashMap<String url,Capabilities>;
            final HashMap<String, WMSCapabilities> capabilities = new HashMap<String, WMSCapabilities>();

            if (DEBUG) {
                log.debug("vor CyclicBarrier");//NOI18N
            }

            if (links.size() > 0) {
                //Das Runnable Objekt wird ausgef\u00FChrt wenn alle Capabilities geladen worden sind oder ein Fehler aufgetreten ist
                if (currentBarrier != null) {
                    if (DEBUG) {
                        log.debug("reseting cyclicBarrier");//NOI18N
                    }
                    currentBarrier.reset();
                }

                currentBarrier = new CyclicBarrier(links.size(), new Runnable() {

                    @Override
                    public void run() {
                        createLayers(conf, capabilities);
                    }
                });



                // <editor-fold defaultstate="collapsed" desc="Laden der Capabilities">

                //Zuerst werden alle Capabilities geladen und in eine HashMap gesteckt
                //Ist das Laden beednet wird das dem Barrier durch ein await() gesagt
                for (final String link : links) {
                    Thread retrieval = new Thread() {

                        @Override
                        public void run() {
                            URL getCapURL = null;
                            try {
//                                InputStreamReader reader = null;
                                getCapURL = new URL(link);
                                URL finalPostUrl = link.indexOf('?') != -1 ? new URL(link.substring(0, link.indexOf('?'))) : new URL(link);



//              OGCWMSCapabilitiesFactory capFact = new OGCWMSCapabilitiesFactory();
                                CismapBroker broker = CismapBroker.getInstance();
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
                                //ToDo Probleme mit WFS wird aber denke ich nicht gebraucht
                                if (DEBUG) {
                                    log.debug("read WMSCapabilities for " + finalPostUrl);//NOI18N
                                }
                                WMSCapabilitiesFactory capFact = new WMSCapabilitiesFactory();
                                if (link.toLowerCase().contains("service=wss")) {//NOI18N
                                    try {
                                        if (DEBUG) {
                                            log.debug("WSS Capabilties Link added");//NOI18N
                                        }
                                        final URL url = new URL(link.substring(0, link.indexOf('?')));
                                        if (DEBUG) {
                                            log.debug("URL of the WSS: " + url.toString());//NOI18N
                                        }
                                        if (!WebAccessManager.getInstance().isHandlerForURLRegistered(url)) {
                                            WebAccessManager.getInstance().registerAccessHandler(url, AccessHandler.ACCESS_HANDLER_TYPES.WSS);
                                        } else {
                                            if (DEBUG) {
                                                log.debug("Handler is already registered");//NOI18N
                                            }
                                        }
                                    } catch (MalformedURLException ex) {
                                        log.error("Url is not wellformed no wss authentication possible", ex);//NOI18N
                                    }
                                }
                                //ToDO Langsam
                                WMSCapabilities cap = capFact.createCapabilities(link);
//ToDo funktionalität abgeschaltet steckt zur zeit in CismetGUICommons --> refactoring
//                                broker.addHttpCredentialProviderCapabilities(cap, broker.getHttpCredentialProviderURL(getCapURL));
//                                if (broker.isServerSecuredByPassword(cap)) {
//                                    broker.addProperty(getCapURL.toString(), cap.getCapability().getLayer().getTitle());
//                                }
                                capabilities.put(link, cap);
                            } catch (Exception ex) {
                                if (DEBUG) {
                                    log.debug("Exception for URL: " + link, ex);//NOI18N
                                }
                                log.warn("Error while retrieving Capabilities" + ":", ex);//NOI18N
                            }
                            try {
                                currentBarrier.await();
                            } catch (InterruptedException ex) {
                                log.warn("Thread was interrupted TODO CUSTOMIZE TEXT" + ":", ex);//NOI18N
                            } catch (BrokenBarrierException ex) {
                                log.warn("No layers available TODO CUSTOMIZE TEXT" + ":", ex);//NOI18N
                            }
                        }
                    };
                    retrieval.setPriority(Thread.NORM_PRIORITY);
                    retrieval.start();
                }
                //</editor-fold>

            } else {
                if (DEBUG) {
                    log.debug("No Barrier");//NOI18N
                }
                createLayers(conf, capabilities);
            }


        } catch (Throwable ex) {
            log.error("Error during the configuration of the ActiveLayerModell", ex);//NOI18N
        }
    }

    /**
     * Layer neu anordnene entweder nach dem layerPosition Attribut (wenn vorhanden)
     * oder nach der Tag-Reihenfolge in der XML Config.
     *
     * @return sortierte Liste
     */
    private Element[] orderLayers(Element layersElement) {
        List<Element> layerElements = layersElement.getChildren();
        Element[] orderedLayerElements = new Element[layerElements.size()];

        int i = 0;
        for (Element layerElement : layerElements) {
            int layerPosition = -1;
            Attribute layerPositionAttr = layerElement.getAttribute("layerPosition");//NOI18N
            if (layerPositionAttr != null) {
                try {
                    layerPosition = layerPositionAttr.getIntValue();
                } catch (Exception e) {
                }
            }

            if (layerPosition < 0 || layerPosition >= orderedLayerElements.length) {
                log.warn("layer position of layer #" + i + " (" + layerElement.getName() + ") not set or invalid, setting to " + i);//NOI18N
                layerPosition = i;
            }

            if (orderedLayerElements[layerPosition] != null) {
                log.warn("conflicting layer position " + layerPosition + ": '" + layerElement.getName() + "' vs '" + orderedLayerElements[layerPosition].getName() + "'");//NOI18N
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
                log.debug(i + " layer '" + layerElement.getName() + "' set to position " + layerPosition);//NOI18N
            }
            i++;
        }

        return orderedLayerElements;
    }

    private void createLayers(final Element conf, final HashMap<String, WMSCapabilities> capabilities) {
        if (DEBUG) {
            log.debug("removing all existing layers");//NOI18N
        }
        removeAllLayers();
        Element layerElement = conf.getChild("Layers");//NOI18N
        if (layerElement == null) {
            log.warn("LayerElement not found! Check for old version child \"RasterLayers\"");//NOI18N
            layerElement = conf.getChild("RasterLayers");//NOI18N
            if (layerElement == null) {
                log.error("no vlaid layers element found");//NOI18N
                return;
            }
        }

        log.info("restoring " + layerElement.getChildren().size() + " layers from xml configuration");//NOI18N
        Element[] orderedLayers = orderLayers(layerElement);

        for (final Element element : orderedLayers) {
            if (DEBUG) {
                log.debug("trying to add Layer '" + element.getName() + "'");//NOI18N
            }
            try {
                // <editor-fold defaultstate="collapsed" desc="WMSServiceLayer">
                if (element.getName().equals("WMSServiceLayer")) {//NOI18N
                    final WMSServiceLayer wmsServiceLayer = new WMSServiceLayer(element, capabilities);
                    if (wmsServiceLayer.getWMSLayers().size() > 0) {
                        if (EventQueue.isDispatchThread()) {
                            log.fatal("InvokeLater in EDT");//NOI18N
                        }
                        EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    log.info("addLayer WMSServiceLayer (" + wmsServiceLayer.getName() + ")");//NOI18N
                                    addLayer(wmsServiceLayer);
                                } catch (IllegalArgumentException schonVorhanden) {
                                    log.warn("Layer WMSServiceLayer '" + wmsServiceLayer.getName() + "' already existed. Do not add the Layer. \n" + schonVorhanden.getMessage());//NOI18N
                                }
                            }
                        });

                    }
                } //</editor-fold>
                // <editor-fold defaultstate="collapsed" desc="WebFeatureServiceLayer">
                else if (element.getName().equals(WebFeatureService.WFS_FEATURELAYER_TYPE)) {
                    final WebFeatureService wfs = new WebFeatureService(element);
                    if (EventQueue.isDispatchThread()) {
                        log.fatal("InvokeLater in EDT");//NOI18N
                    }
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                log.info("addLayer " + WebFeatureService.WFS_FEATURELAYER_TYPE + " (" + wfs.getName() + ")");//NOI18N
                                addLayer(wfs);
                            } catch (IllegalArgumentException schonVorhanden) {
                                log.warn("Layer " + WebFeatureService.WFS_FEATURELAYER_TYPE + " '" + wfs.getName() + "' already existed. Do not add the Layer. \n" + schonVorhanden.getMessage());//NOI18N
                            }
                        }
                    });
                } //</editor-fold>
                // <editor-fold defaultstate="collapsed" desc="DocumentFeatureServiceLayer">
                else if (element.getName().equals("DocumentFeatureServiceLayer")) {//NOI18N
                    log.error("DocumentFeatureServiceLayer not supported");//NOI18N
                    //throw new UnsupportedOperationException("DocumentFeatureServiceLayer not supported");
//          if(DEBUG)log.debug("DocumentFeatureLayer von ConfigFile wird hinzugefügt");
//          URI documentURI = new URI(element.getChildText("documentURI").trim());
//          File testFile = new File(documentURI);
//          if (!testFile.exists())
//          {
//            log.warn("Das Angebene Document(" + testFile.getAbsolutePath() + ") exisitiert nicht ---> abbruch, es wird kein Layer angelegt");
//            continue;
//          }
//
//          //final GMLFeatureService gfs = new GMLFeatureService(element);
//          //langsam sollte nicht im EDT ausgeführt werden
//          final DocumentFeatureService dfs = DocumentFeatureServiceFactory.createDocumentFeatureService(element);
//          //final ShapeFileFeatureService sfs = new ShapeFileFeatureService(element);
//          EventQueue.invokeLater(new Runnable()
//          {
//
//            @Override
//            public void run()
//            {
//              try
//              {
//                log.info("addLayer DocumentFeatureServiceLayer (" + dfs.getName() + ")");
//                addLayer(dfs);
//              } catch (IllegalArgumentException schonVorhanden)
//              {
//                log.warn("Layer DocumentFeatureServiceLayer '" + dfs.getName() + "' already existed. Do not add the Layer. \n" + schonVorhanden.getMessage());
//              }
//            }
//          });
                } //</editor-fold>
                // <editor-fold defaultstate="collapsed" desc="SimpleWMS">
                else if (element.getName().equals("simpleWms")) {//NOI18N
                    final SimpleWMS simpleWMS = new SimpleWMS(element);
                    if (EventQueue.isDispatchThread()) {
                        log.fatal("InvokeLater in EDT");//NOI18N
                    }
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            log.info("addLayer SimpleWMS (" + simpleWMS.getName() + ")");//NOI18N
                            try {
                                addLayer(simpleWMS);
                            } catch (IllegalArgumentException schonVorhanden) {
                                log.warn("Layer SimpleWMS '" + simpleWMS.getName() + "' already existed. Do not add the Layer. \n" + schonVorhanden.getMessage());//NOI18N
                            }
                        }
                    });

                } //</editor-fold>
                // <editor-fold defaultstate="collapsed" desc="SimplePostgisFeatureService und SimpleUpdateablePostgisFeatureService">
                else if (element.getName().equals("simplePostgisFeatureService")) {//NOI18N
                    SimplePostgisFeatureService spfs;
                    if (element.getAttributeValue("updateable") != null && element.getAttributeValue("updateable").equals("true")) {//NOI18N
                        spfs = new SimpleUpdateablePostgisFeatureService(element);
                    } else {
                        spfs = new SimplePostgisFeatureService(element);
                    }

                    final SimplePostgisFeatureService simplePostgisFeatureService = spfs;
                    if (EventQueue.isDispatchThread()) {
                        log.fatal("InvokeLater in EDT");//NOI18N
                    }
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                log.info("addLayer SimplePostgisFeatureService (" + simplePostgisFeatureService.getName() + ")");//NOI18N
                                addLayer(simplePostgisFeatureService);
                            } catch (IllegalArgumentException schonVorhanden) {
                                log.warn("Layer SimplePostgisFeatureService '" + simplePostgisFeatureService.getName() + "' already existed. Do not add the Layer. \n" + schonVorhanden.getMessage());//NOI18N
                            } //</editor-fold>
                        }
                    });
                } else {
                    try {
                        if (DEBUG) {
                            log.debug("restoring generic layer configuration from xml element '" + element.getName() + "'");//NOI18N
                        }
                        final RetrievalServiceLayer layer = (RetrievalServiceLayer) XMLObjectFactory.restoreObjectfromElement(element);

                        if (EventQueue.isDispatchThread()) {
                            log.fatal("InvokeLater in EDT");//NOI18N
                        }
                        EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    log.info("addLayer generic layer configuration (" + layer.getName() + ")");//NOI18N
                                    addLayer(layer);
                                } catch (IllegalArgumentException schonVorhanden) {
                                    log.warn("Layer SimplePostgisFeatureService '" + layer.getName() + "' already existed. Do not add the Layer. \n" + schonVorhanden.getMessage());//NOI18N
                                } //</editor-fold>
                            }
                        });
                    } catch (Throwable t) {
                        log.error("unsupported xml configuration, layer '" + element.getName() + "' could not be created: \n" + t.getLocalizedMessage(), t);//NOI18N
                    }
                }
            } catch (Throwable t) {
                log.error("Layer layer '" + element.getName() + "' could not be created: \n" + t.getMessage(), t);//NOI18N
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
     * @return the defaultSrs
     */
    public Crs getDefaultSrs() {
        return defaultSrs;
    }

    /**
     * @param defaultSrs the defaultSrs to set
     */
    public void setDefaultSrs(Crs defaultSrs) {
        this.defaultSrs = defaultSrs;
    }
}
