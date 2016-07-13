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
package de.cismet.cismap.commons.features;

import edu.umd.cs.piccolo.PNode;

import java.util.Collection;

import de.cismet.cismap.commons.gui.piccolo.PFeature;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public interface ChildNodesProvider {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   parent  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection<PNode> provideChildren(PFeature parent);
}
