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
package de.cismet.cismap.commons.drophandler.filematcher.builtin;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

import java.util.Arrays;
import java.util.Collection;

import de.cismet.cismap.commons.drophandler.MappingComponentDropHandlerFileMatcher;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class MappingComponentDropHandlerFileExtensionMatcher implements MappingComponentDropHandlerFileMatcher {

    //~ Instance fields --------------------------------------------------------

    private final Collection<String> extensions;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MappingComponentDropHandlerFileExtensionMatcher object.
     *
     * @param  extension  DOCUMENT ME!
     */
    public MappingComponentDropHandlerFileExtensionMatcher(final String extension) {
        this(Arrays.asList(extension));
    }

    /**
     * Creates a new MappingComponentDropHandlerFileExtensionMatcher object.
     *
     * @param  extensions  DOCUMENT ME!
     */
    public MappingComponentDropHandlerFileExtensionMatcher(final Collection<String> extensions) {
        this.extensions = extensions;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isMatching(final File file) {
        if (file != null) {
            final String matchingExtension = FilenameUtils.getExtension(file.getName());

            for (final String extension : extensions) {
                if ((extension != null) && extension.equals(matchingExtension)) {
                    return true;
                }
            }
        }
        return false;
    }
}
