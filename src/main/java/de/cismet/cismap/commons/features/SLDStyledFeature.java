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
package de.cismet.cismap.commons.features;

import org.deegree.style.se.unevaluated.Style;

import java.util.List;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.piccolo.PFeature;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public interface SLDStyledFeature extends Feature {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum UOM {

        //~ Enum constants -----------------------------------------------------

        metre, foot, pixel, mm
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  pFeature  DOCUMENT ME!
     * @param  wtst      DOCUMENT ME!
     */
    void applyStyle(PFeature pFeature, WorldToScreenTransform wtst);
    /**
     * DOCUMENT ME!
     *
     * @param  style  DOCUMENT ME!
     */
    void setSLDStyles(List<Style> style);
}
