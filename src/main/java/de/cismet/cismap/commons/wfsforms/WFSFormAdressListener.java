/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.wfsforms;

import de.cismet.cismap.commons.BoundingBox;

/**
 *
 * @author spuhl
 */
public interface WFSFormAdressListener {

    void wfsFormAdressStreetSelected();
    void wfsFormAdressNrSelected();    
    void wfsFormAddressPositioned(BoundingBox addressBB);
    
}
