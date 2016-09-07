/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.drophandler;

import java.io.File;

import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface MappingComponentDropHandler {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    MappingComponentDropHandlerFileMatcher getFileMatcher();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getPriority();

    /**
     * DOCUMENT ME!
     *
     * @param  files  DOCUMENT ME!
     */
    void dropFiles(final Collection<File> files);
}
