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

    private FeatureType feature;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WFSSelectionAndCapabilities object.
     *
     * @param  feature  DOCUMENT ME!
     */
    public WFSSelectionAndCapabilities(final FeatureType feature) {
        this.feature = feature;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getName() {
        return feature.getPrefixedNameString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getHost() {
        return feature.getWFSCapabilities().getURL().toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getQuery() {
        return feature.getWFSQuery();
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
     * @return  DOCUMENT ME!
     */
    public FeatureType getFeature() {
        return feature;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Vector<FeatureServiceAttribute> getAttributes() {
        return feature.getFeatureAttributes();
    }
}
