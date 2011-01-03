/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.preferences;

import org.jdom.Element;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.SimpleUpdateablePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class LayersPreferences {

    //~ Instance fields --------------------------------------------------------

    final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final CismapPreferences cismapPreferences;
    private TreeMap rasterServices = new TreeMap();
    private TreeMap featureServices = new TreeMap();
    private boolean appFeatureLayerEnabled = true;
    private float appFeatureLayerTranslucency = 0.9f;
    private String appFeatureLayerName = ""; // NOI18N

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LayersPreferences object.
     *
     * @param  cismapPreferences  DOCUMENT ME!
     * @param  parent             DOCUMENT ME!
     */
    public LayersPreferences(final CismapPreferences cismapPreferences, final Element parent) {
        this.cismapPreferences = cismapPreferences;

        try {
            appFeatureLayerEnabled = parent.getChild("appFeatureLayer").getAttribute("enabled").getBooleanValue();
        } catch (Exception e) {
            this.cismapPreferences.log.warn("Read preferences. Error. appFeatureLayer.enabled  ", e);
        } // NOI18N
        try {
            appFeatureLayerTranslucency = parent.getChild("appFeatureLayer").getAttribute("translucency")
                        .getFloatValue();
        } catch (Exception e) {
            this.cismapPreferences.log.warn("Read preferences. Error. appFeatureLayer.translucency  ", e);
        } // NOI18N
        try {
            appFeatureLayerName = parent.getChild("appFeatureLayer").getAttribute("name").getValue();
        } catch (Exception e) {
            this.cismapPreferences.log.warn("Read preferences. Error. appFeatureLayer.name  ", e);
        } // NOI18N

        final List simpleWmsList = parent.getChild("rasterLayers").getChildren("simpleWms"); // NOI18N
        Iterator it = simpleWmsList.iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if (o instanceof Element) {
                final Element el = (Element)o;
                try {
                    boolean skip = false;
                    try {
                        skip = el.getAttribute("skip").getBooleanValue();
                    } catch (Exception skipException) {
                    }                                                                        // NOI18N
                    if (!skip) {
                        final SimpleWMS swms = new SimpleWMS(el);
                        rasterServices.put(new Integer(swms.getLayerPosition()), swms);
                    }
                } catch (Exception ex) {
                    log.warn("Read preferences. Error. SimpleWMS erzeugen  ", ex);           // NOI18N
                }
            }
        }
        final List simplePostgisFeatureServiceList = parent.getChild("featureLayers")
                    .getChildren("simplePostgisFeatureService");                             // NOI18N
        it = simplePostgisFeatureServiceList.iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if (o instanceof Element) {
                final Element el = (Element)o;
                if (log.isDebugEnabled()) {
                    log.debug("parsing '" + el.getName() + "' layer preferences");           // NOI18N
                }
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("SimplePostgisFeatureService added");                      // NOI18N
                    }
                    boolean skip = false;
                    boolean updateable = false;
                    try {
                        skip = el.getAttribute("skip").getBooleanValue();
                    } catch (Exception skipException) {
                    }                                                                        // NOI18N
                    try {
                        updateable = el.getAttribute("updateable").getBooleanValue();
                    } catch (Exception skipException) {
                    }                                                                        // NOI18N
                    if (!skip) {
                        SimplePostgisFeatureService spfs = null;
                        if (updateable) {
                            spfs = new SimpleUpdateablePostgisFeatureService(el);
                        } else {
                            spfs = new SimplePostgisFeatureService(el);
                        }

                        featureServices.put(new Integer(spfs.getLayerPosition()), spfs);
                    }
                } catch (Exception ex) {
                    log.warn("Read preferences. Error. Create SimplePostgisFeatureService", ex); // NOI18N
                }
            }
        }

        final List simplePostgisWebServiceList = parent.getChild("featureLayers")
                    .getChildren("simpleWebFeatureService");                                     // NOI18N
        it = simplePostgisWebServiceList.iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if (o instanceof Element) {
                final Element el = (Element)o;
                try {
                    boolean skip = false;
                    boolean updateable = false;
                    try {
                        skip = el.getAttribute("skip").getBooleanValue();
                    } catch (Exception skipException) {
                    }                                                                            // NOI18N
                    try {
                        updateable = el.getAttribute("updateable").getBooleanValue();
                    } catch (Exception skipException) {
                    }                                                                            // NOI18N
                    if (!skip) {
                        WebFeatureService swfs = null;
                        if (updateable) {
                            // TODO IMPLEMENT ?
                            // spfs = new SimpleUpdateablePostgisFeatureService(el);
                        } else {
                            swfs = new WebFeatureService(el);
                        }
                        featureServices.put(new Integer(swfs.getLayerPosition()), swfs);
                        if (log.isDebugEnabled()) {
                            log.debug("SimpleWebFeatureService added");                          // NOI18N
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Read preferences. Error. SimpleWebFeatureService erzeugen  ", ex); // NOI18N
                }
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TreeMap getRasterServices() {
        return rasterServices;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  rasterServices  DOCUMENT ME!
     */
    public void setRasterServices(final TreeMap rasterServices) {
        this.rasterServices = rasterServices;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TreeMap getFeatureServices() {
        return featureServices;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureServices  DOCUMENT ME!
     */
    public void setFeatureServices(final TreeMap featureServices) {
        this.featureServices = featureServices;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isAppFeatureLayerEnabled() {
        return appFeatureLayerEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  appFeatureLayerEnabled  DOCUMENT ME!
     */
    public void setAppFeatureLayerEnabled(final boolean appFeatureLayerEnabled) {
        this.appFeatureLayerEnabled = appFeatureLayerEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public float getAppFeatureLayerTranslucency() {
        return appFeatureLayerTranslucency;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  appFeatureLayerTranslucency  DOCUMENT ME!
     */
    public void setAppFeatureLayerTranslucency(final float appFeatureLayerTranslucency) {
        this.appFeatureLayerTranslucency = appFeatureLayerTranslucency;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getAppFeatureLayerName() {
        return appFeatureLayerName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  appFeatureLayerName  DOCUMENT ME!
     */
    public void setAppFeatureLayerName(final String appFeatureLayerName) {
        this.appFeatureLayerName = appFeatureLayerName;
    }
}
