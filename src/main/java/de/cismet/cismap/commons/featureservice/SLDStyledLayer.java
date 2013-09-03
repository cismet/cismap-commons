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

import java.awt.Graphics2D;
import java.io.InputStream;
import java.io.Reader;
import org.deegree.commons.utils.Pair;

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
    
    Pair<Integer, Integer> getLegendSize();
    void getLegend(int width, int height, Graphics2D g2d);
}
