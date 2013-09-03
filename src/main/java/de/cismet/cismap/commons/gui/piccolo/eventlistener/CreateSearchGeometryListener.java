/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import java.awt.Color;

import de.cismet.cismap.commons.features.SearchFeature;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface CreateSearchGeometryListener extends CreateGeometryListenerInterface {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    SearchFeature getLastSearchFeature();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Color getSearchColor();

    /**
     * DOCUMENT ME!
     *
     * @param  newValue  DOCUMENT ME!
     */
    void setSearchColor(final Color newValue);

    /**
     * DOCUMENT ME!
     *
     * @param  newValue  DOCUMENT ME!
     */
    void setSearchTransparency(final float newValue);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    float getSearchTransparency();

    /**
     * DOCUMENT ME!
     *
     * @param  newValue  DOCUMENT ME!
     */
    void setHoldGeometries(final boolean newValue);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isHoldingGeometries();

    /**
     * DOCUMENT ME!
     */
    void showLastFeature();

    /**
     * DOCUMENT ME!
     */
    void redoLastSearch();

    /**
     * DOCUMENT ME!
     *
     * @param  searchFeature  DOCUMENT ME!
     */
    void search(final SearchFeature searchFeature);
}
