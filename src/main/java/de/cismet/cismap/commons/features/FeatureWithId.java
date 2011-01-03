/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.features;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten hell
 * @version  $Revision$, $Date$
 */
public interface FeatureWithId {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getId();

    /**
     * DOCUMENT ME!
     *
     * @param  id  DOCUMENT ME!
     */
    void setId(int id);

    /**
     * DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  The id expression should be optained from LayerProperties
     */
    @Deprecated
    String getIdExpression();

    /**
     * DOCUMENT ME!
     *
     * @param       idExpression  DOCUMENT ME!
     *
     * @deprecated  The id expression should be set on the LayerProperties
     */
    @Deprecated
    void setIdExpression(String idExpression);
}
