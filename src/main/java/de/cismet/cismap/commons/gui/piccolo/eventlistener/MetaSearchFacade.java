/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.PNode;

import javax.swing.JDialog;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public interface MetaSearchFacade {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean hasSearchTopics();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean hasSelectedSearchTopics();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    PNode generatePointerAnnotationForSelectedSearchTopics();
    /**
     * DOCUMENT ME!
     *
     * @param   changedPropertyName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isSearchTopicSelectedEvent(final String changedPropertyName);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    JDialog getSearchDialog();
}
