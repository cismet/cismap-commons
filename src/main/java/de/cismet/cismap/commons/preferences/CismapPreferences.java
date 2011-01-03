/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.preferences;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.net.URL;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class CismapPreferences {

    //~ Instance fields --------------------------------------------------------

    final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private LayersPreferences layersPrefs;
    private GlobalPreferences globalPrefs;
    private CapabilitiesPreferences capabilityPrefs;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of CismapPreferences.
     *
     * @param  url  DOCUMENT ME!
     */
    public CismapPreferences(final URL url) {
        try {
            final SAXBuilder builder = new SAXBuilder(false);
            final Document doc = builder.build(url);
            final Element prefs = doc.getRootElement();
            readFromCismapPreferences(prefs);
        } catch (Exception e) {
        }
    }

    /**
     * Creates a new CismapPreferences object.
     *
     * @param  cismapPreferences  DOCUMENT ME!
     */
    public CismapPreferences(final Element cismapPreferences) {
        readFromCismapPreferences(cismapPreferences);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  cismapPreferences  DOCUMENT ME!
     */
    private void readFromCismapPreferences(final Element cismapPreferences) {
        try {
            layersPrefs = new LayersPreferences(this, cismapPreferences.getChild("cismapLayersPreferences"));
        } // NOI18N
        catch (Exception e) {
            log.warn("Error while loading the LayersPreferences", e);
        } // NOI18N
        try {
            globalPrefs = new GlobalPreferences(cismapPreferences.getChild("cismapGlobalPreferences"));
        } // NOI18N
        catch (Exception e) {
            log.warn("Error while loading the GlobalPreferences", e);
        } // NOI18N
        try {
            capabilityPrefs = new CapabilitiesPreferences(cismapPreferences.getChild("cismapCapabilitiesPreferences"),
                    cismapPreferences.getChild("cismapCapabilitiesPreferences"));
        } // NOI18N
        catch (Exception e) {
            log.warn("Error while loading the CapabilitiesPreferences", e);
        } // NOI18N
    }
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LayersPreferences getLayersPrefs() {
        return layersPrefs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layersPrefs  DOCUMENT ME!
     */
    public void setLayersPrefs(final LayersPreferences layersPrefs) {
        this.layersPrefs = layersPrefs;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public GlobalPreferences getGlobalPrefs() {
        return globalPrefs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  globalPrefs  DOCUMENT ME!
     */
    public void setGlobalPrefs(final GlobalPreferences globalPrefs) {
        this.globalPrefs = globalPrefs;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CapabilitiesPreferences getCapabilityPrefs() {
        return capabilityPrefs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  capabilityPrefs  DOCUMENT ME!
     */
    public void setCapabilityPrefs(final CapabilitiesPreferences capabilityPrefs) {
        this.capabilityPrefs = capabilityPrefs;
    }
}
