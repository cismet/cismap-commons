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

import com.vividsolutions.jts.geom.Coordinate;

import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.gui.piccolo.PFeature;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class SplittedNewFeature extends PureNewFeature {

    //~ Instance fields --------------------------------------------------------

    private final PFeature splittedFromPFeature;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SplittedNewFeature object.
     *
     * @param  coordArr              DOCUMENT ME!
     * @param  wtst                  DOCUMENT ME!
     * @param  splittedFromPFeature  DOCUMENT ME!
     */
    public SplittedNewFeature(final Coordinate[] coordArr,
            final WorldToScreenTransform wtst,
            final PFeature splittedFromPFeature) {
        super(coordArr, wtst);

        this.splittedFromPFeature = splittedFromPFeature;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getSplittedFromPFeature() {
        return splittedFromPFeature;
    }
}
