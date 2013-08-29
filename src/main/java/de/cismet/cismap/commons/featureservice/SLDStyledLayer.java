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
package de.cismet.cismap.commons.featureservice;

import java.io.InputStream;
import java.io.Reader;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public interface SLDStyledLayer {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  inputStream  DOCUMENT ME!
     */
    void setSLDInputStream(String inputStream);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Reader getSLDDefiniton();
}
