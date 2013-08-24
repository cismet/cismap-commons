/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.features.AbstractNewFeature;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface CreateGeometryListenerInterface {

    //~ Instance fields --------------------------------------------------------

    String LINESTRING = "LINESTRING";                   // NOI18N
    String POINT = "POINT";                             // NOI18N
    String POLYGON = "POLYGON";                         // NOI18N
    String RECTANGLE = "BOUNDING_BOX";                  // NOI18N
    String RECTANGLE_FROM_LINE = "RECTANGLE_FROM_LINE"; // NOI18N
    String ELLIPSE = "ELLIPSE";                         // NOI18N

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getMode();

    /**
     * DOCUMENT ME!
     *
     * @param   m  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    void setMode(final String m) throws IllegalArgumentException;

    /**
     * DOCUMENT ME!
     *
     * @param   mode  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isInMode(final String mode);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getNumOfEllipseEdges();

    /**
     * DOCUMENT ME!
     *
     * @param  numOfEllipseEdges  DOCUMENT ME!
     */
    void setNumOfEllipseEdges(int numOfEllipseEdges);

    /**
     * DOCUMENT ME!
     *
     * @param  geometryFeatureClass  DOCUMENT ME!
     */
    void setGeometryFeatureClass(final Class<? extends AbstractNewFeature> geometryFeatureClass);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Class getGeometryFeatureClass();
}
