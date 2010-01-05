/*
 * LayerControlSelectionChangedListener.java
 *
 * Created on 2. Oktober 2007, 15:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.gui.simplelayerwidget;

/**
 *
 * @author hell
 */
public interface LayerControlSelectionChangedListener {
    public void layerControlSelectionChanged(LayerControl lc);
    public void layerWantsUp(LayerControl lc);
    public void layerWantsDown(LayerControl lc);
}
