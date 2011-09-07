/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.raster.wms;
import de.cismet.cismap.commons.LayerInfoProvider;
import de.cismet.cismap.commons.wms.capabilities.Layer;
import de.cismet.cismap.commons.wms.capabilities.Style;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class WMSLayer implements LayerInfoProvider {

    //~ Instance fields --------------------------------------------------------

    private boolean enabled;
    private Style selectedStyle;
    private boolean querySelected;
    private Layer ogcCapabilitiesLayer;
    private WMSServiceLayer parentServiceLayer = null;
    private String name;
    private String styleName;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of WMSLayer.
     *
     * @param  ogcCapabilitiesLayer  DOCUMENT ME!
     * @param  selectedStyle         DOCUMENT ME!
     */
    public WMSLayer(final Layer ogcCapabilitiesLayer, final Style selectedStyle) {
        this.ogcCapabilitiesLayer = ogcCapabilitiesLayer;
        this.selectedStyle = selectedStyle;
        enabled = true;
        querySelected = false;
        this.selectedStyle = selectedStyle;
    }

    /**
     * Creates a new instance of WMSLayer.
     *
     * @param  name           ogcCapabilitiesLayer DOCUMENT ME!
     * @param  styleName      DOCUMENT ME!
     * @param  enabled        DOCUMENT ME!
     * @param  querySelected  DOCUMENT ME!
     */
    public WMSLayer(final String name, final String styleName, final boolean enabled, final boolean querySelected) {
        this.name = name;
        this.styleName = styleName;
        this.enabled = enabled;
        this.querySelected = querySelected;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        if (ogcCapabilitiesLayer != null) {
            return ogcCapabilitiesLayer.getTitle();
        } else {
            return name;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  enabled  DOCUMENT ME!
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Style getSelectedStyle() {
        return selectedStyle;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectedStyle  DOCUMENT ME!
     */
    public void setSelectedStyle(final Style selectedStyle) {
        this.selectedStyle = selectedStyle;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isQuerySelected() {
        return querySelected;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  querySelected  DOCUMENT ME!
     */
    public void setQuerySelected(final boolean querySelected) {
        this.querySelected = querySelected;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Layer getOgcCapabilitiesLayer() {
        return ogcCapabilitiesLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ogcCapabilitiesLayer  DOCUMENT ME!
     */
    public void setOgcCapabilitiesLayer(final Layer ogcCapabilitiesLayer) {
        this.ogcCapabilitiesLayer = ogcCapabilitiesLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   srs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSrsSupported(final String srs) {
        return ogcCapabilitiesLayer.isSrsSupported(srs);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public WMSServiceLayer getParentServiceLayer() {
        return parentServiceLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  parentServiceLayer  DOCUMENT ME!
     */
    public void setParentServiceLayer(final WMSServiceLayer parentServiceLayer) {
        this.parentServiceLayer = parentServiceLayer;
    }

    @Override
    public String getLayerURI() {
        return ogcCapabilitiesLayer.getName();
    }

    @Override
    public String getServerURI() {
        return parentServiceLayer.getCapabilitiesUrl();
    }

    @Override
    public boolean isLayerQuerySelected() {
        return isQuerySelected();
    }

    @Override
    public void setLayerQuerySelected(final boolean selected) {
        setQuerySelected(selected);
    }

    @Override
    public boolean isQueryable() {
        if (isDummy()) {
            return querySelected;
        } else {
            return getOgcCapabilitiesLayer().isQueryable();
        }
    }

    @Override
    public Layer getLayerInformation() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isDummy() {
        return ogcCapabilitiesLayer == null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the styleName
     */
    public String getStyleName() {
        return styleName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  styleName  the styleName to set
     */
    public void setStyleName(final String styleName) {
        this.styleName = styleName;
    }
}
