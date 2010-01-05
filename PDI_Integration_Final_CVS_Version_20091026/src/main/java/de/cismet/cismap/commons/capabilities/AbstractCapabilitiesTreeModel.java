/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.capabilities;

import javax.swing.tree.TreeModel;

/**
 *
 * @author spuhl
 */
public abstract class AbstractCapabilitiesTreeModel implements TreeModel {

private String serviceName;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
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
