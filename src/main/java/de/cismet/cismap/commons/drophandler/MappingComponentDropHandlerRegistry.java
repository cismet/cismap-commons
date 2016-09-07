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

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class MappingComponentDropHandlerRegistry {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(MappingComponentDropHandlerRegistry.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Collection<MappingComponentDropHandler> repo = new ArrayList<>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MappingComponentDropHandlerRegistry object.
     */
    private MappingComponentDropHandlerRegistry() {
        final Lookup.Result<MappingComponentDropHandler> result = Lookup.getDefault()
                    .lookupResult(MappingComponentDropHandler.class);
        final Collection<? extends MappingComponentDropHandler> instances = result.allInstances();
        if (LOG.isDebugEnabled()) {
            LOG.debug("found " + instances.size() + " instances of MappingComponentDropHandler"); // NOI18N
        }

        for (final MappingComponentDropHandler dropHandler : instances) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("found entry: " + dropHandler.getFileMatcher() + " -> " + dropHandler.getClass()); // NOI18N
            }
            repo.add(dropHandler);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static MappingComponentDropHandlerRegistry getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   file  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponentDropHandler getDropHandler(final File file) {
        final Map<Integer, MappingComponentDropHandler> matchingDropHandlers = new HashMap<>();
        for (final MappingComponentDropHandler dropHandler : repo) {
            final MappingComponentDropHandlerFileMatcher fileMatcher = dropHandler.getFileMatcher();

            if (fileMatcher.isMatching(file)) {
                matchingDropHandlers.put(dropHandler.getPriority(), dropHandler);
            }
        }

        if (matchingDropHandlers.isEmpty()) {
            return null;
        } else {
            final List<Integer> priorityList = new ArrayList<>(matchingDropHandlers.keySet());
            Collections.sort(priorityList);

            final Integer highestPriority = priorityList.get(priorityList.size() - 1);
            return matchingDropHandlers.get(highestPriority);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final MappingComponentDropHandlerRegistry INSTANCE = new MappingComponentDropHandlerRegistry();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
