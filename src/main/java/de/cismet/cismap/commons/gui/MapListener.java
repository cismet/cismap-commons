/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.gui;

import de.cismet.cismap.commons.features.Feature;
import java.util.Collection;

/**
 *
 * @author spuhl
 */
public interface MapListener {
    void featuresAddedToMap(Collection<Feature> cf);
}
