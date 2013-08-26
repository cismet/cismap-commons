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
package de.cismet.cismap.commons;

import edu.umd.cs.piccolo.PNode;

import org.jdom.Element;

import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModelStore;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class ModeLayer implements RetrievalServiceLayer, MapService, ActiveLayerModelStore {

    //~ Instance fields --------------------------------------------------------

    protected HashMap<String, RetrievalServiceLayer> modeLayers = new HashMap<String, RetrievalServiceLayer>();
    private String mode = null;
    private RetrievalServiceLayer currentModeLayer = null;
    private boolean enabled = true;
    private int layerPosition = -1;
    private float translucency = 0;
    private ArrayList<RetrievalListener> retrievalListeners = new ArrayList<RetrievalListener>();
    private ArrayList<PropertyChangeListener> pcListeners = new ArrayList<PropertyChangeListener>();
    private ActiveLayerModel mappingModel;
    private String layerKey = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ModeLayer object.
     */
    public ModeLayer() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setActiveLayerModel(final ActiveLayerModel alm) {
        mappingModel = alm;
    }

    @Override
    public ActiveLayerModel getActiveLayerModel() {
        return mappingModel;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mode   DOCUMENT ME!
     * @param   layer  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public RetrievalServiceLayer putModeLayer(final String mode, final RetrievalServiceLayer layer) {
        if (layer.getPNode() == null) {
            layer.setPNode(new PNode()); // add this PNode to avoid NPEs when the layer
        }
        return modeLayers.put(mode, layer);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mode  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public RetrievalServiceLayer getModeLayer(final String mode) {
        return modeLayers.get(mode);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Set<String> getModes() {
        return modeLayers.keySet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mode  DOCUMENT ME!
     */
    public void forceMode(final String mode) {
        setMode(mode, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mode  DOCUMENT ME!
     */
    public void setMode(final String mode) {
        setMode(mode, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mode    DOCUMENT ME!
     * @param  forced  DOCUMENT ME!
     */
    public void setMode(final String mode, final boolean forced) {
        if ((this.mode == null) || !this.mode.equals(mode) || forced) {
            final RetrievalServiceLayer oldLayer = currentModeLayer;
            currentModeLayer = modeLayers.get(mode);
            if (currentModeLayer != null) {
                this.mode = mode;
                for (final RetrievalListener rl : retrievalListeners) {
                    if (oldLayer != null) {
                        oldLayer.removeRetrievalListener(rl);
                        setTranslucency(oldLayer.getTranslucency());
                    }
                    currentModeLayer.addRetrievalListener(rl);
                }
                for (final PropertyChangeListener pcl : pcListeners) {
                    if (oldLayer != null) {
                        oldLayer.removePropertyChangeListener(pcl);
                    }
                    currentModeLayer.addPropertyChangeListener(pcl);
                }
                final ActiveLayerEvent ale = new ActiveLayerEvent();
                if (oldLayer != null) {
                    ale.setLayer(oldLayer);
                    CismapBroker.getInstance().fireLayerRemoved(ale);
                }
                if (mappingModel != null) {
                    if (oldLayer != null) {
                        mappingModel.fireMapServiceRemoved((MapService)oldLayer);
                    }
                    mappingModel.fireMapServiceAdded((MapService)currentModeLayer);
                }
                ale.setLayer(currentModeLayer);
                CismapBroker.getInstance().fireLayerAdded(ale);
            } else {
                currentModeLayer = oldLayer;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private void checkCurrentModeLayer() {
        if (currentModeLayer == null) {
            throw new IllegalStateException(
                "A modeLayer without a mode is not a good idea. It should be configured with at least two different modes/layers and initialized with a start mode");
        }
    }

    @Override
    public void setErrorObject(final Object error) {
        checkCurrentModeLayer();
        currentModeLayer.setErrorObject(error);
    }

    @Override
    public Object getErrorObject() {
        checkCurrentModeLayer();
        return currentModeLayer.getErrorObject();
    }

    @Override
    public boolean hasErrors() {
        return currentModeLayer.hasErrors();
    }

    @Override
    public PNode getPNode() {
        return currentModeLayer.getPNode();
    }

    @Override
    public void setPNode(final PNode pNode) {
        currentModeLayer.setPNode(pNode);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        for (final RetrievalServiceLayer rsl : modeLayers.values()) {
            rsl.setEnabled(enabled);
        }
    }

    @Override
    public boolean canBeDisabled() {
        return true;
    }

    @Override
    public int getLayerPosition() {
        return layerPosition;
    }

    @Override
    public void setLayerPosition(final int layerPosition) {
        this.layerPosition = layerPosition;
    }

    @Override
    public float getTranslucency() {
        return translucency;
    }

    @Override
    public void setTranslucency(final float translucency) {
        this.translucency = translucency;
        for (final RetrievalServiceLayer rsl : modeLayers.values()) {
            rsl.setTranslucency(translucency);
        }
    }

    @Override
    public String getName() {
        return currentModeLayer.getName();
    }

    @Override
    public void setName(final String name) {
        currentModeLayer.setName(name);
    }

    @Override
    public void addRetrievalListener(final RetrievalListener rl) {
        retrievalListeners.add(rl);
        currentModeLayer.addRetrievalListener(rl);
    }

    @Override
    public void removeRetrievalListener(final RetrievalListener irl) {
        retrievalListeners.remove(irl);
        currentModeLayer.removeRetrievalListener(irl);
    }

    @Override
    public void retrieve(final boolean forced) {
        currentModeLayer.retrieve(forced);
    }

    @Override
    public void setRefreshNeeded(final boolean refreshNeeded) {
        for (final RetrievalServiceLayer rsl : modeLayers.values()) {
            rsl.setRefreshNeeded(refreshNeeded);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  crs  DOCUMENT ME!
     */
    public void setCrs(final Crs crs) {
        for (final RetrievalServiceLayer rsl : modeLayers.values()) {
            CidsLayerFactory.setLayerToCrs(crs, rsl);
        }
    }

    @Override
    public boolean isRefreshNeeded() {
        return currentModeLayer.isRefreshNeeded();
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        pcListeners.add(l);
        currentModeLayer.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        pcListeners.remove(l);
        currentModeLayer.removePropertyChangeListener(l);
    }

    @Override
    public int getProgress() {
        return currentModeLayer.getProgress();
    }

    @Override
    public void setProgress(final int progress) {
        currentModeLayer.setProgress(progress);
    }

    @Override
    public void setSize(final int height, final int width) {
        for (final RetrievalServiceLayer rsl : modeLayers.values()) {
            if (rsl instanceof MapService) {
                ((MapService)rsl).setSize(height, width);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public RetrievalServiceLayer getCurrentLayer() {
        return currentModeLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCurrentMode() {
        return mode;
    }

    @Override
    public void setBoundingBox(final BoundingBox bb) {
        for (final RetrievalServiceLayer rsl : modeLayers.values()) {
            if (rsl instanceof MapService) {
                ((MapService)rsl).setBoundingBox(bb);
            }
        }
    }

    @Override
    public boolean isVisible() {
        return ((MapService)currentModeLayer).isVisible();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element toElement() {
        final Element element = new Element("ModeLayer"); // NOI18N
        element.setAttribute("mode", getCurrentMode());   // NOI18N
        element.setAttribute("key", layerKey);            // NOI18N
        for (final String m : getModes()) {
            final Element modeElement = new Element("Mode");
            modeElement.setAttribute("key", m);
            modeElement.addContent(CidsLayerFactory.getElement((MapService)getModeLayer(m)));
            element.addContent(modeElement);
        }
        return element;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getLayerKey() {
        return layerKey;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layerKey  DOCUMENT ME!
     */
    public void setLayerKey(final String layerKey) {
        this.layerKey = layerKey;
    }
}
