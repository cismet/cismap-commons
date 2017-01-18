/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo;

import edu.umd.cs.piccolo.PNode;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface PSticky {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean getVisible();
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    PNode getParent();
    /**
     * DOCUMENT ME!
     *
     * @param  scale  DOCUMENT ME!
     */
    void setScale(final double scale);
}
