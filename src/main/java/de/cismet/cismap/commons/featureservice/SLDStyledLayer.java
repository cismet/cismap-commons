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
package de.cismet.cismap.commons.featureservice;

import org.deegree.commons.utils.Pair;

import java.awt.Graphics2D;

import java.io.InputStream;
import java.io.Reader;
import org.deegree.commons.utils.Pair;

import java.util.List;

import java.util.List;

import java.util.List;

import java.util.List;

import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public interface SLDStyledLayer {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  inputStream  DOCUMENT ME!
     */
    void setSLDInputStream(String inputStream);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Reader getSLDDefiniton();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Pair<Integer, Integer> getLegendSize();
    /**
     * DOCUMENT ME!
     *
     * @param   nr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Pair<Integer, Integer> getLegendSize(int nr);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    List<Pair<Integer, Integer>> getLegendSizes();
    /**
     * DOCUMENT ME!
     *
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     * @param  g2d     DOCUMENT ME!
     */
    void getLegend(int width, int height, Graphics2D g2d);
    /**
     * DOCUMENT ME!
     *
     * @param  nr      DOCUMENT ME!
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     * @param  g2d     DOCUMENT ME!
     */
    void getLegend(int nr, final int width, final int height, final Graphics2D g2d);
    /**
     * DOCUMENT ME!
     *
     * @param  sizes  DOCUMENT ME!
     * @param  g2ds   DOCUMENT ME!
     */
    void getLegends(final List<Pair<Integer, Integer>> sizes, final Graphics2D[] g2ds);
}
