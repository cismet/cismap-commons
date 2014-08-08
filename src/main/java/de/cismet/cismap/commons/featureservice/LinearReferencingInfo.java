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

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class LinearReferencingInfo {

    //~ Instance fields --------------------------------------------------------

    private String fromField;
    private String tillField;
    private String linRefReferenceName;
    private String geomField;
    private String srcLinRefJoinField;
    private String trgLinRefJoinField;
    private String domain;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the fromField
     */
    public String getFromField() {
        return fromField;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fromField  the fromField to set
     */
    public void setFromField(final String fromField) {
        this.fromField = fromField;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the tillField
     */
    public String getTillField() {
        return tillField;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  tillField  the tillField to set
     */
    public void setTillField(final String tillField) {
        this.tillField = tillField;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the routeField
     */
    public String getLinRefReferenceName() {
        return linRefReferenceName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  linRefReferenceName  routeField the routeField to set
     */
    public void setLinRefReferenceName(final String linRefReferenceName) {
        this.linRefReferenceName = linRefReferenceName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the routeGeometry
     */
    public String getGeomField() {
        return geomField;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geomField  routeGeometry the routeGeometry to set
     */
    public void setGeomField(final String geomField) {
        this.geomField = geomField;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the srcLinRefJoinField
     */
    public String getSrcLinRefJoinField() {
        return srcLinRefJoinField;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  srcLinRefJoinField  the srcLinRefJoinField to set
     */
    public void setSrcLinRefJoinField(final String srcLinRefJoinField) {
        this.srcLinRefJoinField = srcLinRefJoinField;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the trgLinRefJoinField
     */
    public String getTrgLinRefJoinField() {
        return trgLinRefJoinField;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  trgLinRefJoinField  the trgLinRefJoinField to set
     */
    public void setTrgLinRefJoinField(final String trgLinRefJoinField) {
        this.trgLinRefJoinField = trgLinRefJoinField;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  domain  the domain to set
     */
    public void setDomain(final String domain) {
        this.domain = domain;
    }
}
