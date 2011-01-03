/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * LayerControlSelectionChangedListener.java
 *
 * Created on 2. Oktober 2007, 15:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.simplelayerwidget;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public interface LayerControlSelectionChangedListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  lc  DOCUMENT ME!
     */
    void layerControlSelectionChanged(LayerControl lc);
    /**
     * DOCUMENT ME!
     *
     * @param  lc  DOCUMENT ME!
     */
    void layerWantsUp(LayerControl lc);
    /**
     * DOCUMENT ME!
     *
     * @param  lc  DOCUMENT ME!
     */
    void layerWantsDown(LayerControl lc);
}
