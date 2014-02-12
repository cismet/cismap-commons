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

import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.feature.FeatureCollection;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ShapeInfo {

    //~ Instance fields --------------------------------------------------------

    private String typename;
    private ShapeFile file;
    private int srid;
    private FeatureCollection fc;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeInfo object.
     *
     * @param  typename  DOCUMENT ME!
     * @param  file      DOCUMENT ME!
     * @param  srid      DOCUMENT ME!
     * @param  fc        DOCUMENT ME!
     */
    public ShapeInfo(final String typename, final ShapeFile file, final int srid, final FeatureCollection fc) {
        this.typename = typename;
        this.file = file;
        this.srid = srid;
        this.fc = fc;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the typename
     */
    public String getTypename() {
        return typename;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  typename  the typename to set
     */
    public void setTypename(final String typename) {
        this.typename = typename;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the file
     */
    public ShapeFile getFile() {
        return file;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  file  the file to set
     */
    public void setFile(final ShapeFile file) {
        this.file = file;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the srid
     */
    public int getSrid() {
        return srid;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  srid  the srid to set
     */
    public void setSrid(final int srid) {
        this.srid = srid;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the fc
     */
    public FeatureCollection getFc() {
        return fc;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fc  the fc to set
     */
    public void setFc(final FeatureCollection fc) {
        this.fc = fc;
    }
}
