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

import org.jdom.Element;

import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.CidsLayerFactory;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.raster.wms.SlidableWMSServiceLayerGroup;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupportingRasterLayer;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class LayerCollection extends ArrayList<Object> implements ServiceLayer {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(LayerCollection.class);
    public static final String XML_ELEMENT_NAME = "LayerCollection"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private String name = "unbenannt";
    private ActiveLayerModel model = null;
    private int layerPosition;
    private float translucency;
    private Element initElement = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LayerCollection object.
     */
    public LayerCollection() {
    }

    /**
     * Creates a new LayerCollection object.
     *
     * @param  e             DOCUMENT ME!
     * @param  capabilities  DOCUMENT ME!
     * @param  model         DOCUMENT ME!
     */
    public LayerCollection(final Element e,
            final HashMap<String, WMSCapabilities> capabilities,
            final ActiveLayerModel model) {
        this.model = model;

        try {
            LOG.info("creating new FeatureService instance from xml element '" + e.getName() + "'"); // NOI18N

            if (e.getName().equals(XML_ELEMENT_NAME)) {
                this.initFromElement(e, capabilities);
            } else {
                LOG.error("LayerCollection could not be initailised from xml: unsupported element '" + e.getName()
                            + "'"); // NOI18N
                throw new ClassNotFoundException(
                    "LayerCollection could not be initailised from xml: unsupported element '"
                            + e.getName()
                            + "'"); // NOI18N
            }
        } catch (Exception ex) {
            LOG.error("Exception while creating LayerCollection", ex);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   element       DOCUMENT ME!
     * @param   capabilities  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void initFromElement(Element element, final HashMap<String, WMSCapabilities> capabilities)
            throws Exception {
        if (element == null) {
            element = this.getInitElement();
        } else {
            this.setInitElement((Element)element.clone());
        }

        if (element.getAttributeValue("name") != null)                                  // NOI18N
        {
            this.setName(element.getAttributeValue("name"));                            // NOI18N
        }
        if (element.getAttributeValue("enabled") != null)                               // NOI18N
        {
            this.setEnabled(Boolean.valueOf(element.getAttributeValue("enabled")));     // NOI18N
        }
        if (element.getAttributeValue("translucency") != null)                          // NOI18N
        {
            this.setTranslucency(element.getAttribute("translucency").getFloatValue()); // NOI18N
        }
        if (element.getAttributeValue("layerPosition") != null)                         // NOI18N
        {
            this.setLayerPosition(element.getAttribute("layerPosition").getIntValue()); // NOI18N
        }

        createLayers(element, capabilities);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  conf          DOCUMENT ME!
     * @param  capabilities  DOCUMENT ME!
     */
    private void createLayers(final Element conf, final HashMap<String, WMSCapabilities> capabilities) {
        final Element layerElement = conf.getChild("Layers"); // NOI18N

        if (layerElement == null) {
            LOG.error("no valid layers element found"); // NOI18N
            return;
        }

        LOG.info("restoring " + layerElement.getChildren().size() + " layers from xml configuration"); // NOI18N

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
        try {
            final ServiceLayer layer = CidsLayerFactory.createLayer(element, capabilities, model);

            if (layer != null) {
                add(layer);

                if (layer instanceof RetrievalServiceLayer) {
                    model.registerRetrievalServiceLayer((RetrievalServiceLayer)layer);
                }
            }
        } catch (Throwable t) {
            LOG.error("Layer layer '" + element.getName() + "' could not be created: \n" + t.getMessage(), t); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  srs  DOCUMENT ME!
     */
    public void setCrs(final Crs srs) {
        for (final Object layer : this) {
            if (layer instanceof WMSServiceLayer) {
                ((WMSServiceLayer)layer).setSrs(srs.getCode());
            } else if (layer instanceof SlidableWMSServiceLayerGroup) {
                ((SlidableWMSServiceLayerGroup)layer).setSrs(srs.getCode());
            } else if (layer instanceof WebFeatureService) {
                ((WebFeatureService)layer).setCrs(srs);
            } else if (layer instanceof ShapeFileFeatureService) {
                ((ShapeFileFeatureService)layer).setCrs(srs);
            } else if (layer instanceof LayerCollection) {
                ((LayerCollection)layer).setCrs(srs);
            } else {
                LOG.error("The SRS of a layer cannot be changed. Layer is of type  " + layer.getClass().getName());
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element toElement() {
        final Element element = new Element(XML_ELEMENT_NAME);
        element.setAttribute("name", getName());                                                // NOI18N
        element.setAttribute("enabled", Boolean.valueOf(isEnabled()).toString());               // NOI18N
        element.setAttribute("translucency", new Float(getTranslucency()).toString());          // NOI18N
        element.setAttribute("layerPosition", new Integer(this.getLayerPosition()).toString()); // NOI18N

        // Zuerst alle RasterLayer
        final Iterator<Object> it = iterator();
        final Element allLayerConf = new Element("Layers"); // Sollte irgendwann zu "Layers" umgewandelt werden
        // (TODO)//NOI18N

        int counter = 0;
        while (it.hasNext()) {
            final Object service = it.next();

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
        element.addContent(allLayerConf);
        // Alle FeatureService Layer

        // AppFeatureLayer

        return element;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = (79 * hash) + ((this.name != null) ? this.name.hashCode() : 0);
        return hash;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  enabled  DOCUMENT ME!
     */
    @Override
    public void setEnabled(final boolean enabled) {
        for (final Object tmp : this) {
            if (tmp instanceof LayerCollection) {
                ((LayerCollection)tmp).setEnabled(enabled);
            } else if (tmp instanceof ServiceLayer) {
                ((ServiceLayer)tmp).setEnabled(enabled);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isEnabled() {
        boolean enabled = true;

        for (final Object tmp : this) {
            if (tmp instanceof LayerCollection) {
                if (!((LayerCollection)tmp).isEnabled()) {
                    enabled = false;
                }
            } else if (tmp instanceof ServiceLayer) {
                if (!((ServiceLayer)tmp).isEnabled()) {
                    enabled = false;
                }

                if (model != null) {
                    // only the last component of the tree path will be considered within
                    // the methods isVisible(TreePath) and handleVisibiliy(TreePath)
                    final TreePath tp = new TreePath(new Object[] { this, tmp });

                    if (model.isVisible(tp) != enabled) {
                        model.handleVisibility(tp);
                    }
                }
            }
        }

        return enabled;
    }

    @Override
    public boolean add(final Object e) {
        if (e instanceof LayerCollection) {
            ((LayerCollection)e).setModel(model);
        }

        return super.add(e);
    }

    @Override
    public void add(final int index, final Object element) {
        if (element instanceof LayerCollection) {
            ((LayerCollection)element).setModel(model);
        }

        super.add(index, element);
    }

    @Override
    public boolean addAll(final Collection<? extends Object> c) {
        for (final Object element : c) {
            if (element instanceof LayerCollection) {
                ((LayerCollection)element).setModel(model);
            }
        }

        return super.addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends Object> c) {
        for (final Object element : c) {
            if (element instanceof LayerCollection) {
                ((LayerCollection)element).setModel(model);
            }
        }

        return super.addAll(index, c);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name  DOCUMENT ME!
     */
    @Override
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the model
     */
    public ActiveLayerModel getModel() {
        return model;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  model  the model to set
     */
    public void setModel(final ActiveLayerModel model) {
        this.model = model;

        for (final Object tmp : this) {
            if (tmp instanceof LayerCollection) {
                ((LayerCollection)tmp).setModel(model);
            }
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
    public void setTranslucency(final float t) {
        this.translucency = translucency;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the initElement
     */
    public Element getInitElement() {
        return initElement;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  initElement  the initElement to set
     */
    public void setInitElement(final Element initElement) {
        this.initElement = initElement;
    }
}
