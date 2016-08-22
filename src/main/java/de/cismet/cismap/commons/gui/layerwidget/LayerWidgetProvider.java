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
package de.cismet.cismap.commons.gui.layerwidget;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface LayerWidgetProvider {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  layerWidget  DOCUMENT ME!
     */
    void setLayerWidget(final LayerWidget layerWidget);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    LayerWidget getLayerWidget();
}
