/*
 * MappingModelListener.java
 *
 * Created on 10. M�rz 2005, 11:25
 */

package de.cismet.cismap.commons;
import de.cismet.cismap.commons.rasterservice.MapService;

/**
 *
 * @author hell
 */
public interface MappingModelListener {
    //public void selectionChanged(MappingModelEvent mme);
    public void mapServiceLayerStructureChanged(MappingModelEvent mme);
    public void mapServiceAdded(MapService mapService);
    public void mapServiceRemoved(MapService mapService);

}
