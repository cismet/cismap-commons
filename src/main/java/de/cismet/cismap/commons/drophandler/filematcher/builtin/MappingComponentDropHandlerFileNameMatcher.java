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

import lombok.Getter;

import java.io.File;

import java.util.regex.Pattern;

import de.cismet.cismap.commons.drophandler.MappingComponentDropHandlerFileMatcher;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class MappingComponentDropHandlerFileNameMatcher implements MappingComponentDropHandlerFileMatcher {

    //~ Instance fields --------------------------------------------------------

    @Getter private final String matchString;

    @Getter private final boolean regex;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MappingComponentDropHandlerFileNameMatcher object.
     *
     * @param  matchString  DOCUMENT ME!
     * @param  regex        DOCUMENT ME!
     */
    public MappingComponentDropHandlerFileNameMatcher(final String matchString, final boolean regex) {
        this.regex = regex;
        this.matchString = matchString;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isMatching(final File file) {
        if (file == null) {
            return false;
        } else if (regex) {
            return Pattern.matches(matchString, file.getName());
        } else {
            return file.getName().equals(matchString);
        }
    }
}
