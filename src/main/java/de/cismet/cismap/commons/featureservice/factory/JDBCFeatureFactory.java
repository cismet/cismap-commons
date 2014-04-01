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
package de.cismet.cismap.commons.featureservice.factory;

import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class JDBCFeatureFactory<FT extends FeatureServiceFeature, QT> extends AbstractFeatureFactory<FT, QT> {

    //~ Instance fields --------------------------------------------------------

    protected Crs crs = CismapBroker.getInstance().getSrs();
    protected Geometry envelope; 

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DegreeFeatureFactory object.
     */
    public JDBCFeatureFactory() {
        super();
    }

    /**
     * Creates a new DegreeFeatureFactory object.
     *
     * @param  dff  DOCUMENT ME!
     */
    protected JDBCFeatureFactory(final JDBCFeatureFactory dff) {
        super(dff);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the crs
     */
    public Crs getCrs() {
        return crs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  crs  the crs to set
     */
    public void setCrs(final Crs crs) {
        this.crs = crs;
    }

    /**
     * @return the envelope
     */
    public Geometry getEnvelope() {
        return envelope;
    }
}
