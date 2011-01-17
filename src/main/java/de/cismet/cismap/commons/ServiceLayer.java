/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons;

import java.util.HashMap;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface ServiceLayer {

    //~ Instance fields --------------------------------------------------------

    int LAYER_ENABLED_VISIBLE = 0;
    int LAYER_DISABLED_VISIBLE = 1;
    int LAYER_ENABLED_INVISIBLE = 2;
    int LAYER_DISABLED_INVISIBLE = 3;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isEnabled();
    /**
     * DOCUMENT ME!
     *
     * @param  enabled  DOCUMENT ME!
     */
    void setEnabled(boolean enabled);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean canBeDisabled();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getLayerPosition();
    /**
     * DOCUMENT ME!
     *
     * @param  layerPosition  DOCUMENT ME!
     */
    void setLayerPosition(int layerPosition);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    float getTranslucency();
    /**
     * DOCUMENT ME!
     *
     * @param  t  DOCUMENT ME!
     */
    void setTranslucency(float t);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getName();
    /**
     * DOCUMENT ME!
     *
     * @param  name  DOCUMENT ME!
     */
    void setName(String name);
}
