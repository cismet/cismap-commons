/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons;

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
     * Getter for the translucency of this layer. It returns a value between 0.0 and 1.0 (0.0 &lt;= translucency &lt;=
     * 1.0) which represents the translucency percentage. However, this percentage effectively represents the <b>
     * opacity</b> of the layer, e.g. if this operation returns <code>1.0</code> the layer is fully opaque and not
     * transparent.
     *
     * @return  the opacity percentage (0.0 &lt;= translucency &lt;= 1.0)
     */
    float getTranslucency();

    /**
     * Setter for the translucency of this layer. The value shall be between 0.0 and 1.0 (0.0 &lt;= translucency &lt;=
     * 1.0) which represents the translucency percentage. However, this percentage effectively is interpreted as the <b>
     * opacity</b> of the layer, e.g. if this operation receives <code>1.0</code> as a parameter the layer will become
     * fully opaque and not transparent.
     *
     * @param  t  the opacity percentage (0.0 &lt;= translucency &lt;= 1.0)
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
