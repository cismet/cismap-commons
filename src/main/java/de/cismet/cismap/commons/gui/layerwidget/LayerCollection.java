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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.ConvertableToXML;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.featureservice.DocumentFeatureService;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.SimpleUpdateablePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.preferences.CapabilityLink;
import de.cismet.cismap.commons.raster.wms.SlidableWMSServiceLayerGroup;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.raster.wms.featuresupportlayer.SimpleFeatureSupportingRasterLayer;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class LayerCollection extends ArrayList<Object> {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger log = Logger.getLogger(LayerCollection.class);
    public static final String XML_ELEMENT_NAME = "LayerCollection"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private String name = "unbenannt";
    private ActiveLayerModel model = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LayerCollection object.
     */
    public LayerCollection() {
    }

    //~ Methods ----------------------------------------------------------------

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
                log.error("The SRS of a layer cannot be changed. Layer is of type  " + layer.getClass().getName());
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
        element.setAttribute("name", name);

        final Element layerElement = new Element("layers"); // NOI18N
        int counter = 0;

        for (final Object service : this) {
            if (service instanceof SimpleFeatureSupportingRasterLayer) {
            } else if (service instanceof WMSServiceLayer) {
                final Element layerConf = ((WMSServiceLayer)service).getElement();
                layerElement.addContent(layerConf);
                counter++;
            } else if (service instanceof SimpleWMS) {
                final Element layerConf = ((SimpleWMS)service).getElement();
                layerElement.addContent(layerConf);
                counter++;
            } else if (service instanceof WebFeatureService) {
                final Element layerConf = ((WebFeatureService)service).toElement();
                layerElement.addContent(layerConf);
                counter++;
            } else if (service instanceof DocumentFeatureService) {
                final Element layerConf = ((DocumentFeatureService)service).toElement();
                layerElement.addContent(layerConf);
                counter++;
            } else if (service instanceof SimplePostgisFeatureService) {
                final Element layerConf = ((SimplePostgisFeatureService)service).toElement();
                layerElement.addContent(layerConf);
                counter++;
            } else if (service instanceof SimpleUpdateablePostgisFeatureService) {
                final Element layerConf = ((SimpleUpdateablePostgisFeatureService)service).toElement();
                layerElement.addContent(layerConf);
                counter++;
            } else if (service instanceof SlidableWMSServiceLayerGroup) {
                final Element layerConf = ((SlidableWMSServiceLayerGroup)service).toElement();
                layerElement.addContent(layerConf);
                counter++;
            } else if (service instanceof LayerCollection) {
                final Element layerConf = ((LayerCollection)service).toElement();
                layerElement.addContent(layerConf);
                counter++;
            } else if (service instanceof ConvertableToXML) {
                final Element layerConf = ((ConvertableToXML)service).toElement();
                layerElement.addContent(layerConf);
                counter++;
            } else {
                log.warn("saving configuration not supported by service: " + service); // NOI18N
            }
        }

        element.addContent(layerElement);
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
}
