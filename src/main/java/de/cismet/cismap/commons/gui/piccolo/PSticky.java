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
    public boolean getVisible();
    public PNode getParent();
    public void setScale(final double scale);
}
