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

/**
 * Features produced by a Web Feature Service. Currently identical to DefaultFeatureServiceFeatures.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class WFSFeature extends DefaultFeatureServiceFeature {

    //~ Instance fields --------------------------------------------------------

    private String name;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  name  DOCUMENT ME!
     */
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getProperty(String propertyName) {
        final Object ret = super.getProperty(propertyName);

        if ((ret == null) && (propertyName.indexOf(":") != -1)) {
            propertyName = propertyName.substring(propertyName.indexOf(":") + 1);
            return super.getProperty("app:" + propertyName);
        }

        return ret;
    }
}
