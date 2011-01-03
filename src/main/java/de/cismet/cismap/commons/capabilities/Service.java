/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.capabilities;

/**
 * The service interface provides the contact information of wms and wfs capabilities response documents. This interface
 * should be used to eliminate the deegree dependency for the capabilities parsing.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface Service {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String[] getKeywordList();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getAbstract();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getTitle();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getName();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getContactPerson();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getContactOrganization();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getFees();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getAccessConstraints();
}
