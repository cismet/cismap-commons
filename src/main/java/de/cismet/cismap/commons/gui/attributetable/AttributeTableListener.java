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
package de.cismet.cismap.commons.gui.attributetable;

import javax.swing.JPanel;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface AttributeTableListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  panel    DOCUMENT ME!
     * @param  id       DOCUMENT ME!
     * @param  name     DOCUMENT ME!
     * @param  tooltip  DOCUMENT ME!
     */
    void showPanel(JPanel panel, String id, String name, String tooltip);
}