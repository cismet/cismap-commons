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
package de.cismet.cismap.linearreferencing.tools;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface LinearReferencedGeomProvider {
    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    List<AbstractFeatureService> getLinearReferencedGeomServices();

    /**
     * DOCUMENT ME!
     *
     * @param   service  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getServiceDomain(AbstractFeatureService service);

    /**
     * DOCUMENT ME!
     *
     * @param   service  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getInternalServiceName(AbstractFeatureService service);
}
