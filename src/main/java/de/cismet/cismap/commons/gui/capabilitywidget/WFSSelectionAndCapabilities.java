/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.capabilitywidget;

import org.jdom.Element;

import java.util.Vector;

import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;

/**
 * TODO: Diese Klasse kann entfernt werden, da sie keine zusaetzliche Funktionalitaet zur Klasse FeatureType besitzt.
 *
 * @author   nh
 * @version  $Revision$, $Date$
 */
public class WFSSelectionAndCapabilities {

    //~ Instance fields --------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @deprecated  the features array should be used instead
     */
    private FeatureType feature;
    private FeatureType[] features;
    private boolean reverseAxisOrder = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WFSSelectionAndCapabilities object.
     *
     * @param       feature  DOCUMENT ME!
     *
     * @deprecated  the constructor with the FeatureType array should be used instead
     */
    public WFSSelectionAndCapabilities(final FeatureType feature) {
        this.feature = feature;
    }

    /**
     * Creates a new WFSSelectionAndCapabilities object.
     *
     * @param  features          DOCUMENT ME!
     * @param  reverseAxisOrder  DOCUMENT ME!
     */
    public WFSSelectionAndCapabilities(final FeatureType[] features, final boolean reverseAxisOrder) {
        this.features = features;
        this.reverseAxisOrder = reverseAxisOrder;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureType[] getFeatures() {
        return features;
    }

    /**
     * DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  getFeatures()[0].getPrefixedNameString() should be used
     */
    public String getName() {
        if (feature != null) {
            return feature.getPrefixedNameString();
        } else {
            return getFeatures()[0].getPrefixedNameString();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  getFeatures()[0].getWfsCapabilities().getURL().toString() should be used
     */
    public String getHost() {
        if (feature != null) {
            return feature.getWFSCapabilities().getURL().toString();
        } else {
            return getFeatures()[0].getWFSCapabilities().getURL().toString();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  getFeatures()[0].getWFSQuery() should be used
     */
    public Element getQuery() {
        if (feature != null) {
            return feature.getWFSQuery();
        } else {
            return getFeatures()[0].getWFSQuery();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getIdentifier() {
        return "";
    }

    /**
     * DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  getFeatures()[0] should be used
     */
    public FeatureType getFeature() {
        if (feature != null) {
            return feature;
        } else {
            return getFeatures()[0];
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  getFeatures()[0].getFeatureAttributes() should be used
     */
    public Vector<FeatureServiceAttribute> getAttributes() {
        if (feature != null) {
            return feature.getFeatureAttributes();
        } else {
            return getFeatures()[0].getFeatureAttributes();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the reversedAxisOrder
     */
    public boolean isReverseAxisOrder() {
        return reverseAxisOrder;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  reversedAxisOrder  the reversedAxisOrder to set
     */
    public void setReverseAxisOrder(final boolean reversedAxisOrder) {
        this.reverseAxisOrder = reversedAxisOrder;
    }
}
