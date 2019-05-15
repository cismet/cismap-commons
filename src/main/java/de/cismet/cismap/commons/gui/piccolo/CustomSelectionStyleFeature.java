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
package de.cismet.cismap.commons.gui.piccolo;

import java.awt.Paint;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public interface CustomSelectionStyleFeature {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Paint getSelectionLinePaint();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getSelectionLineWidth();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Paint getSelectionFillingPaint();
}
