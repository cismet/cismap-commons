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

import org.deegree.feature.Feature;
import org.deegree.feature.types.FeatureType;
import org.deegree.style.se.unevaluated.Style;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Features read from a SHP File. Currently identical to DefaultFeatureServiceFeature.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class ShapeFeature extends DefaultFeatureServiceFeature {

    //~ Instance fields --------------------------------------------------------

    private final String typename;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeFeature object.
     *
     * @param  typename  DOCUMENT ME!
     */

    public ShapeFeature(final String typename) {
        this.typename = typename;
    }

    /**
     * Creates a new ShapeFeature object.
     *
     * @param  typename  DOCUMENT ME!
     * @param  styles    DOCUMENT ME!
     */
    public ShapeFeature(final String typename, final List<org.deegree.style.se.unevaluated.Style> styles) {
        setSLDStyles(styles); // super.style = styles;
        this.typename = typename;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Feature getDeegreeFeature() {
        return new ShapeFileLayerDeegreeFeature();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected class ShapeFileLayerDeegreeFeature extends DeegreeFeature {

        //~ Methods ------------------------------------------------------------

        @Override
        public FeatureType getType() {
            return new DeegreeFeatureType() {

                    @Override
                    public QName getName() {
                        return new QName("Feature");// for demo only
                    }
                };
        }
    }
}
