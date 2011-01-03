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
package de.cismet.cismap.commons.capabilities;

import javax.swing.tree.TreeModel;

/**
 * DOCUMENT ME!
 *
 * @author   spuhl
 * @version  $Revision$, $Date$
 */
public abstract class AbstractCapabilitiesTreeModel implements TreeModel {

    //~ Instance fields --------------------------------------------------------

    private String serviceName;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  serviceName  DOCUMENT ME!
     */
    public void setServiceName(final String serviceName) {
        this.serviceName = serviceName;
    }

// TODO MUST BE REFACTORED ONE DERIVATION IS USING DEEGREE 1 THE OTHER DEEGREE 2
//     /**
//     * Liefert das diesem Model zugeordnete WFSCapabilities-Objekt.
//     * @return WFSCapabilities-Objekt
//     */
//    public WFSCapabilities getCapabilities() {
//        return capabilities;
//    }
//
//    /**
//     * Setzt das diesem Model zugeordnete WFSCapabilities-Objekt neu.
//     * @param capabilities das neue WFSCapabilities-Objekt
//     */
//    public void setCapabilities(WFSCapabilities capabilities) {
//        this.capabilities = capabilities;
//    }
}
