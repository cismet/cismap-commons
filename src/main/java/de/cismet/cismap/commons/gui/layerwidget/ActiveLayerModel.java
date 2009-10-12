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
import de.cismet.cismap.commons.features.DefaultWFSFeature;
import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.SimpleUpdateablePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.WFSOperator;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupportingRasterLayer;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.security.AccessHandler;
import de.cismet.security.WebAccessManager;
import de.cismet.tools.CismetThreadPool;
import de.cismet.tools.StaticXMLTools;
import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;
import de.cismet.tools.gui.PointSymbolCreator;
import de.cismet.tools.gui.treetable.TreeTableModel;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import javax.swing.tree.TreePath;
import org.deegree.services.wms.capabilities.WMSCapabilities;
import org.deegree_impl.services.wms.capabilities.OGCWMSCapabilitiesFactory;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class ActiveLayerModel extends AbstractTreeTableModel implements MappingModel, Configurable {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ActiveLayerModel.class);
    Vector layers = new Vector();
    Vector mappingModelListeners = new Vector();
    BoundingBox initialBoundingBox;
    private HashMap<String, XBoundingBox> homes = new HashMap<String, XBoundingBox>();
    private String srs;
    private String preferredRasterFormat;
    private String preferredTransparentPref;
    private String preferredBGColor;
    private String preferredExceptionsFormat;
    private CyclicBarrier currentBarrier = null;

    /**
     * Erstellt eine neue ActiveLayerModel-Instanz.
     */
    public ActiveLayerModel() {
        super("Root");
        setDefaults();
    }

    private void setDefaults() {
        //srs="EPSG:4326";
        preferredRasterFormat = "image/png";
        preferredBGColor = "0xF0F0F0";
        //preferredExceptionsFormat="application/vnd.ogc.se_inimage";
        preferredExceptionsFormat = "application/vnd.ogc.se_xml";
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
    public synchronized void addLayer(RetrievalServiceLayer layer) {
        log.debug("addLayer");
        if (layers.contains(layer)) {
            throw new IllegalArgumentException("schon vorhanden");
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
            ((WMSServiceLayer) layer).setSrs(srs);
        }

        layer.addRetrievalListener(new RetrievalListener() {

            public void retrievalStarted(RetrievalEvent e) {
                currentLayer.setProgress(0);
                fireProgressChanged(currentLayer);
                log.debug("retrievalStarted");
            }

            public void retrievalProgress(RetrievalEvent e) {
                currentLayer.setProgress((int) (e.getPercentageDone() * 100));
                fireProgressChanged(currentLayer);
            }

            public void retrievalComplete(RetrievalEvent e) {
                currentLayer.setProgress(100);
                fireProgressChanged(currentLayer);
                log.debug("retrievalComplete");
                if (e.isHasErrors()) {
                    retrievalError(e);
                } else {
                    currentLayer.setErrorObject(null);
                }
            }

            public void retrievalAborted(RetrievalEvent e) {
                currentLayer.setProgress(100);
                fireProgressChanged(currentLayer);
            }

            public void retrievalError(RetrievalEvent e) {
                log.warn("retrievalError");
                currentLayer.setProgress(100);
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
                            errorObject = "<html><table width=310 border=\"0\"><tr><th align=\"left\"><b>Der Server lieferte folgende Fehlermeldung zur\u00FCck:</th></tr><tr><td>" + message + "</td></tr></table></html>";
                        } else {
                            errorObject = "<html><table width=310 border=\"0\"><tr><th align=\"left\"><b>Beim Laden des Bildes ist ein Fehler aufgetreten:</th></tr><tr><td>" + message + "</td></tr></table></html>";
                        }
                    }// Hier kommt jetzt HTML Fehlermeldung, Internal und XML. Das muss reichen
                    //else if ()

                    currentLayer.setErrorObject(errorObject);
                }
            }
        });

        if (layer instanceof MapService) {
            fireMapServiceAdded(((MapService) layer));
        }
        log.debug("RetrievalListener added");
        // Das eigentliche Hinzufuegen des neuen Layers
        layers.add(layer);
        log.debug("layer added");
        fireTreeStructureChanged(this, new Object[]{root}, null, null);
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
            fireTreeStructureChanged(this, new Object[]{root, (WMSServiceLayer) parentPath.getLastPathComponent()}, null, null);
            ActiveLayerEvent ale = new ActiveLayerEvent();
            ale.setLayer((WMSLayer) layer);
            CismapBroker.getInstance().fireLayerRemoved(ale);
        }
    }

    public void removeLayer(RetrievalServiceLayer layer) {
        RetrievalServiceLayer wmsServiceLayer = ((RetrievalServiceLayer) layer);
        layers.remove(wmsServiceLayer);
        ActiveLayerEvent ale = new ActiveLayerEvent();
        ale.setLayer(wmsServiceLayer);
        CismapBroker.getInstance().fireLayerRemoved(ale);
        fireTreeStructureChanged(this, new Object[]{root}, null, null);
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
            fireTreeNodesChanged(this, new Object[]{root}, null, null);
        } else if (layer instanceof WMSLayer) {//Kinderlayer

            TreePath parentPath = treePath.getParentPath();
            ((WMSLayer) layer).setEnabled(!((WMSLayer) layer).isEnabled());
            ((WMSServiceLayer) parentPath.getLastPathComponent()).setRefreshNeeded(true);
            fireTreeNodesChanged(this, new Object[]{root, (WMSServiceLayer) parentPath.getLastPathComponent()}, null, null);
        }
    }

    public void handleVisibility(TreePath treePath) {
        Object layer = treePath.getLastPathComponent();
        if (layer instanceof RetrievalServiceLayer) {
            RetrievalServiceLayer wmsServiceLayer = ((RetrievalServiceLayer) layer);
            wmsServiceLayer.getPNode().setVisible(!wmsServiceLayer.getPNode().getVisible());
            fireTreeNodesChanged(this, new Object[]{root}, null, null);
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
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
            }
        } else if (layer instanceof WMSLayer) {
            WMSLayer l = (WMSLayer) layer;
            WMSServiceLayer parent = (WMSServiceLayer) treePath.getParentPath().getLastPathComponent();
            int pos = parent.getWMSLayers().indexOf(l);
            if (pos + 1 != parent.getWMSLayers().size()) {
                parent.getWMSLayers().remove(l);
                parent.getWMSLayers().add(pos + 1, l);
                parent.setRefreshNeeded(true);
                fireTreeStructureChanged(this, new Object[]{root, parent}, null, null);
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
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
            }
        } else if (layer instanceof WMSLayer) {
            WMSLayer l = (WMSLayer) layer;
            WMSServiceLayer parent = (WMSServiceLayer) treePath.getParentPath().getLastPathComponent();
            int pos = parent.getWMSLayers().indexOf(l);
            if (pos != 0) {
                parent.getWMSLayers().remove(l);
                parent.getWMSLayers().add(pos - 1, l);
                parent.setRefreshNeeded(true);
                fireTreeStructureChanged(this, new Object[]{root, parent}, null, null);
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
    public Object getValueAt(Object node, int column) {
        if (node instanceof RetrievalServiceLayer) {
            return ((RetrievalServiceLayer) node);
        } else if (node instanceof WMSLayer) {
            return ((WMSLayer) node);
        } else {
            return "ROOT 0";
        }
    }

    /**
     * Returns the name for column number <code>column</code>.
     */
    public String getColumnName(int column) {
        switch (column) {
            case (0):
                return " ";
            case (1):
                return java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("ActiveLayerModel.Layer");
            case (2):
                return java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("ActiveLayerModel.Style");
            case (3):
                return java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("ActiveLayerModel.Info");
            case (4):
                return java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("ActiveLayerModel.Fortschritt_Transparenz");
            case (5):
                return "";
            default:
                return "";
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
    public Object getChild(Object parent, int index) {
        //Hier wird die Reihenfolge
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
                } else if (node instanceof WebFeatureService) {
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
            log.debug("node:" + node);
            log.debug("aValue:" + aValue);
            ((WMSServiceLayer) node).setName(aValue.toString());
            this.fireTreeNodesChanged(this, new Object[]{root, node}, null, null);
        } else if (column == 3) {
            //if (aValue instanceof WMSLayer)
        }
        super.setValueAt(aValue, node, column);
    }

    public void removeMappingModelListener(de.cismet.cismap.commons.MappingModelListener mml) {
        mappingModelListeners.remove(mml);
    }

    public void addMappingModelListener(de.cismet.cismap.commons.MappingModelListener mml) {
        mappingModelListeners.add(mml);
    }

    public java.util.TreeMap getMapServices() {
        Iterator it = layers.iterator();
        TreeMap tm = new TreeMap();
        int counter = 0;
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof MapService) {
                tm.put(new Integer(counter++), o);
            }
        }
        return tm;
    }

    /**
     * 
     * @return
     * @deprecated
     */
    public java.util.TreeMap getRasterServices() {
        return getMapServices();
    }

    public HashMap getHomeBoundingBoxes() {
        return homes;
    }

    public void addHome(XBoundingBox xbb) {
        homes.put(xbb.getSrs(), xbb);
    }

//    public void setInitialBoundingBox(BoundingBox initialBoundingBox)  {
//        this.initialBoundingBox=initialBoundingBox;
//    }
    public de.cismet.cismap.commons.BoundingBox getInitialBoundingBox() {
        return homes.get(srs);
    }

    public java.util.TreeMap getFeatureServices() {
        return new TreeMap();
    }

    public String getSrs() {
        return srs;
    }

    public void setSrs(String srs) {
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
    public Element getConfiguration() throws NoWriteError {
        Element conf = new Element("cismapActiveLayerConfiguration");
        //Zuerst alle RasterLayer
        Iterator it = getMapServices().keySet().iterator();
        Element allLayerConf = new Element("Layers"); //Sollte irgendwann zu "Layers" umgewandelt werden (TODO)

        int counter = 0;
        while (it.hasNext()) {
            Object service = getMapServices().get(it.next());
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
                Element layerConf = ((WebFeatureService) service).getElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof SimplePostgisFeatureService) {
                Element layerConf = ((SimplePostgisFeatureService) service).getElement();
                allLayerConf.addContent(layerConf);
                counter++;
            } else if (service instanceof SimpleUpdateablePostgisFeatureService) {
                Element layerConf = ((SimpleUpdateablePostgisFeatureService) service).getElement();
                allLayerConf.addContent(layerConf);
                counter++;
            }
        }
        if (counter == 0) {
            throw new NoWriteError();
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
    synchronized public void configure(final Element e) {

        try {
            final Element conf = e.getChild("cismapActiveLayerConfiguration");
            final Vector<String> links = LayerWidget.getCapabilities(conf, new Vector<String>());
            log.debug("Capabilties links: " + links);
            //Laden der Capabilities vom Server und Speichern in einer HashMap<String url,Capabilities>;
            final HashMap<String, WMSCapabilities> capabilities = new HashMap<String, WMSCapabilities>();

            log.debug("vor CyclicBarrier");

            if (links.size() > 0) {
                //Das Runnable Objekt wird ausgef\u00FChrt wenn alle Capabilities geladen worden sind oder ein Fehler aufgetreten ist
                if (currentBarrier != null) {
                    log.debug("reseting cyclicBarrier");
                    currentBarrier.reset();
                }
                log.debug("currentBarrier == null creating new Barrier");
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
                    Runnable retrieval = new Runnable() {

                        @Override
                        public void run() {
                            URL getCapURL = null;
                            try {
//                                InputStreamReader reader = null;
                                getCapURL = new URL(link);
                                URL finalPostUrl = new URL(link.substring(0, link.indexOf('?')));
//                                OGCWMSCapabilitiesFactory capFact = new OGCWMSCapabilitiesFactory();
                                CismapBroker broker = CismapBroker.getInstance();
//                                try {
//                                    log.debug("Layer Widget: Creating WMScapabilities for URL: " + getCapURL.toString());
//                                    reader = HttpAuthentication.getInputStreamReaderFromURL(CismapBroker.getInstance().getMappingComponent(), getCapURL);
//                                } catch (AuthenticationCanceledException ex) {
//                                    log.warn(ex);
//                                    String title = CismapBroker.getInstance().getProperty(getCapURL.toString());
//
//                                    if (title != null) {
//                                        JXErrorDialog.showDialog(CismapBroker.getInstance().getMappingComponent(),
//                                                java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("LayerWidget.AuthenticationCanceled.Title"),
//                                                java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("LayerWidget.AuthenticationCanceled.Text1") +
//                                                "\"" +
//                                                title +
//                                                "\" " +
//                                                java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("LayerWidget.AuthenticationCanceled.Text2"));
//                                    } else {
//                                        title = getCapURL.toString();
//                                        if (title.startsWith("http://") && title.length() > 21) {
//                                            title = title.substring(7, 21) + "...";
//                                        } else if (title.length() > 14) {
//                                            title = title.substring(0, 14) + "...";
//                                        }
//                                        JXErrorDialog.showDialog(CismapBroker.getInstance().getMappingComponent(),
//                                                java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("LayerWidget.AuthenticationCanceled.Title"),
//                                                java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("LayerWidget.AuthenticationCanceled.Text1") +
//                                                "\"" + title + "\" " +
//                                                java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("LayerWidget.AuthenticationCanceled.Text2"));
//                                    }
//                                }
                                //ToDo Probleme mit WFS wird aber denke ich nicht gebraucht
                                log.debug("rufe WMSCapabilities für " + finalPostUrl + " ab");
                                OGCWMSCapabilitiesFactory capFact = new OGCWMSCapabilitiesFactory();
                                if (link.toLowerCase().contains("service=wss")) {
                                    try {
                                        log.debug("WSS Capabilties Link hinzugefügt");
                                        final URL url = new URL(link.substring(0, link.indexOf('?')));
                                        log.debug("URL des WSS: " + url.toString());
                                        if (!WebAccessManager.getInstance().isHandlerForURLRegistered(url)) {
                                            WebAccessManager.getInstance().registerAccessHandler(url, AccessHandler.ACCESS_HANDLER_TYPES.WSS);
                                        } else {
                                            log.debug("Handler ist bereits registriert");
                                        }
                                    } catch (MalformedURLException ex) {
                                        log.error("Url is not wellformed no wss authentication possible", ex);
                                    }
                                }
                                InputStream result = WebAccessManager.getInstance().doRequest(new URL(link));
                                //ToDO Langsam                                
                                WMSCapabilities cap = capFact.createCapabilities(new BufferedReader(new InputStreamReader(result)));
//ToDo funktionalität abgeschaltet steckt zur zeit in CismetGUICommons --> refactoring
//                                broker.addHttpCredentialProviderCapabilities(cap, broker.getHttpCredentialProviderURL(getCapURL));
//                                if (broker.isServerSecuredByPassword(cap)) {
//                                    broker.addProperty(getCapURL.toString(), cap.getCapability().getLayer().getTitle());
//                                }
                                capabilities.put(link, cap);
                            } catch (Exception ex) {
                                log.debug("Exception für URL: " + link, ex);
                                log.warn(java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("LayerWidget.RetrievingCapabilitiesExceptions") + ":", ex);
                            }
                            try {
                                currentBarrier.await();
                            } catch (InterruptedException ex) {
                                log.warn(java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("LayerWidget.log.Interrupted") + ":", ex);
                            } catch (BrokenBarrierException ex) {
                                log.warn(java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("LayerWidget.log.BrokenBarrier") + ":", ex);
                            }
                        }
                    };
                    CismetThreadPool.execute(retrieval);
                }
//</editor-fold>
            } else {
                log.debug("No Barrier");
                createLayers(conf, capabilities);
            }


        } catch (Throwable ex) {
            log.error("Fehler beim Konfigurieren des ActiveLayerModells", ex);
        }
    }

    private void createLayers(final Element conf, final HashMap<String, WMSCapabilities> capabilities) {
        log.debug("Trigger started");
        log.debug("removing all existing layers");
        removeAllLayers();
        Element layerElement = conf.getChild("Layers");
        if (layerElement == null) {
            log.debug("LayerElement not found! Check for old version child \"RasterLayers\"");
            layerElement = conf.getChild("RasterLayers");
            // wenn == null, dann Exception ? 
            log.debug("OK");
        }

        Iterator<Element> layerIt = layerElement.getChildren().iterator();
        log.debug("Es gibt " + layerElement.getChildren().size() + " Layer");
        while (layerIt.hasNext()) {
            try {
                Element element = layerIt.next();

                // <editor-fold defaultstate="collapsed" desc="WMSServiceLayer">
                if (element.getName().equals("WMSServiceLayer")) {
                    final WMSServiceLayer l = new WMSServiceLayer(element, capabilities);
                    if (l.getWMSLayers().size() > 0) {
                        try {
                            EventQueue.invokeLater(new Runnable() {

                                public void run() {
                                    log.info("addLayer(" + l.getName() + ")");
                                    addLayer(l);
                                }
                            });
                        } catch (IllegalArgumentException schonVorhanden) {
                            log.debug("Layer already existed. Do not add the Layer");
                        }
                    }
                } //</editor-fold>
                // <editor-fold defaultstate="collapsed" desc="WebFeatureServiceLayer">
                else if (element.getName().equals("WebFeatureServiceLayer")) {
                    final String name = element.getAttributeValue("name");
                    List<Element> attributes = element.getChild("Attributes").getChildren();
                    Element query = element.getChild(WFSOperator.GET_FEATURE, WFSOperator.WFS);

                    DefaultWFSFeature wfsFeature = new DefaultWFSFeature();
                    Element fe = element.getChild("renderingFeature").getChild("DefaultWFSFeature");
//                            f.setId(Integer.parseInt(fe.getAttributeValue("id")));
                    wfsFeature.setIdExpression(fe.getAttributeValue("idExpression"));
                    int lineWidth = Integer.parseInt(fe.getAttributeValue("lineWidth"));
                    wfsFeature.setLineWidth(lineWidth);
                    wfsFeature.setTransparency(Float.parseFloat(fe.getAttributeValue("transparency")));
                    wfsFeature.setPrimaryAnnotation(fe.getAttributeValue("primaryAnnotation"));
                    wfsFeature.setSecondaryAnnotation(fe.getAttributeValue("secondaryAnnotation"));
                    wfsFeature.setPrimaryAnnotationScaling(Double.parseDouble(fe.getAttributeValue("primaryAnnotationScaling")));
                    wfsFeature.setPrimaryAnnotationJustification(Float.parseFloat(fe.getAttributeValue("primaryAnnotationJustification")));
                    wfsFeature.setMaxScaleDenominator(Integer.parseInt(fe.getAttributeValue("maxScaleDenominator")));
                    wfsFeature.setMinScaleDenominator(Integer.parseInt(fe.getAttributeValue("minScaleDenominator")));
                    wfsFeature.setAutoScale(Boolean.parseBoolean(fe.getAttributeValue("autoscale")));
                    Color fillColor = StaticXMLTools.convertXMLElementToColor(fe.getChild("fillingColor").getChild("Color"));
                    wfsFeature.setFillingPaint(fillColor);
                    Color lineColor = StaticXMLTools.convertXMLElementToColor(fe.getChild("lineColor").getChild("Color"));
                    wfsFeature.setLinePaint(lineColor);
                    wfsFeature.setPrimaryAnnotationFont(StaticXMLTools.convertXMLElementToFont(fe.getChild("primaryAnnotationFont").getChild("Font")));
                    wfsFeature.setPrimaryAnnotationPaint(StaticXMLTools.convertXMLElementToColor(fe.getChild("primaryAnnotationColor").getChild("Color")));
                    wfsFeature.setHighlightingEnabled(Boolean.parseBoolean(fe.getAttributeValue("highlightingEnabled")));
                    FeatureAnnotationSymbol fas = new FeatureAnnotationSymbol(PointSymbolCreator.createPointSymbol((lineColor != null), (fillColor != null), 10, lineWidth, fillColor, lineColor));
                    fas.setSweetSpotX(0.5d);
                    fas.setSweetSpotY(0.5d);
                    wfsFeature.setPointAnnotationSymbol(fas);

                    final WebFeatureService wfs = new WebFeatureService(element);
                    wfs.setWfsQuery(query);
                    wfs.setAttributes(attributes);
                    wfs.setRenderingFeature(wfsFeature);
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            log.info("addLayer(" + name + ")");
                            addLayer(wfs);
                        }
                    });
                } //</editor-fold>
                // <editor-fold defaultstate="collapsed" desc="SimpleWMS">
                else if (element.getName().equals("simpleWms")) {
                    final SimpleWMS l = new SimpleWMS(element);
                    try {
                        EventQueue.invokeLater(new Runnable() {

                            public void run() {
                                log.info("addLayer(" + l.getName() + ")");
                                addLayer(l);
                            }
                        });
                    } catch (IllegalArgumentException schonVorhanden) {
                        log.debug("Layer already existed. Do not add the Layer");
                    }
                } //</editor-fold>
                // <editor-fold defaultstate="collapsed" desc="SimplePostgisFeatureService und SimpleUpdateablePostgisFeatureService">
                else if (element.getName().equals("simplePostgisFeatureService")) {
                    try {
                        SimplePostgisFeatureService spfs;
                        if (element.getAttributeValue("updateable") != null && element.getAttributeValue("updateable").equals("true")) {
                            spfs = new SimpleUpdateablePostgisFeatureService(element);
                        } else {
                            spfs = new SimplePostgisFeatureService(element);
                        }

                        final SimplePostgisFeatureService l = spfs;

                        EventQueue.invokeLater(new Runnable() {

                            public void run() {
                                log.info("addLayer(" + l.getName() + ")");
                                addLayer(l);
                            }
                        });
                    } catch (IllegalArgumentException schonVorhanden) {
                        log.debug("Layer already existed. Do not add the Layer");
                    }


//</editor-fold>
                }
            } catch (Throwable t) {
            }
        }
    }
}
