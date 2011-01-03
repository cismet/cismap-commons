/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

/**
 * A Feature is "something" that has a geometry.
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface Feature {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    com.vividsolutions.jts.geom.Geometry getGeometry();
    /**
     * DOCUMENT ME!
     *
     * @param  geom  DOCUMENT ME!
     */
    void setGeometry(com.vividsolutions.jts.geom.Geometry geom);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean canBeSelected();
    /**
     * DOCUMENT ME!
     *
     * @param  canBeSelected  DOCUMENT ME!
     */
    void setCanBeSelected(boolean canBeSelected);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isEditable();
    /**
     * DOCUMENT ME!
     *
     * @param  editable  DOCUMENT ME!
     */
    void setEditable(boolean editable);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isHidden();
    /**
     * DOCUMENT ME!
     *
     * @param  hiding  DOCUMENT ME!
     */
    void hide(boolean hiding);
}
