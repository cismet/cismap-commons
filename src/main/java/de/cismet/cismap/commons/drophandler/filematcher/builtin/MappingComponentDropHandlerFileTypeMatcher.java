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

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.cismet.cismap.commons.drophandler.MappingComponentDropHandlerFileMatcher;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class MappingComponentDropHandlerFileTypeMatcher implements MappingComponentDropHandlerFileMatcher {

    //~ Static fields/initializers ---------------------------------------------

    public static String[] VIDEO_TYPES = new String[] { "mov", "mpeg", "mpg", "avi", "mkv" };

    public static String[] IMAGE_TYPES = new String[] { "jpg", "jpeg", "bmp", "tif", "tiff", "png" };

    public static String[] TEXT_TYPES = new String[] { "txt", "csv" };

    public static String[] BINARY_TYPES = new String[] { "exe" };

    public static Map<FileType, String> typeExtensionMap = new HashMap<>();

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum FileType {

        //~ Enum constants -----------------------------------------------------

        ANY, VIDEO, IMAGE, TEXT, BINARY
    }

    //~ Instance fields --------------------------------------------------------

    private final MappingComponentDropHandlerFileExtensionMatcher extensionMatcher;

    private final Collection<FileType> types;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MappingComponentDropHandlerFileTypeMatcher object.
     *
     * @param  type  DOCUMENT ME!
     */
    public MappingComponentDropHandlerFileTypeMatcher(final FileType type) {
        this(Arrays.asList(type));
    }

    /**
     * Creates a new MappingComponentDropHandlerFileTypeMatcher object.
     *
     * @param  types  DOCUMENT ME!
     */
    public MappingComponentDropHandlerFileTypeMatcher(final Collection<FileType> types) {
        final Collection<String> extensions = new ArrayList<>();
        for (final FileType type : types) {
            switch (type) {
                case VIDEO: {
                    extensions.addAll(Arrays.asList(VIDEO_TYPES));
                }
                break;
                case IMAGE: {
                    extensions.addAll(Arrays.asList(IMAGE_TYPES));
                }
                break;
                case TEXT: {
                    extensions.addAll(Arrays.asList(TEXT_TYPES));
                }
                break;
                case BINARY: {
                    extensions.addAll(Arrays.asList(BINARY_TYPES));
                }
                break;
            }
        }
        this.types = types;
        this.extensionMatcher = new MappingComponentDropHandlerFileExtensionMatcher(extensions);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isMatching(final File file) {
        if (types.contains(FileType.ANY)) {
            return true;
        } else {
            return extensionMatcher.isMatching(file);
        }
    }
}

//layerwidgetprovider
