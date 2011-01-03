/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * MappingModelListener.java
 *
 * Created on 10. M�rz 2005, 11:25
 */
package de.cismet.cismap.commons;
import de.cismet.cismap.commons.rasterservice.MapService;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public interface MappingModelListener {

    //~ Methods ----------------------------------------------------------------

    /**
     * public void selectionChanged(MappingModelEvent mme);
     *
     * @param  mme  DOCUMENT ME!
     */
    void mapServiceLayerStructureChanged(MappingModelEvent mme);
    /**
     * DOCUMENT ME!
     *
     * @param  mapService  DOCUMENT ME!
     */
    void mapServiceAdded(MapService mapService);
    /**
     * DOCUMENT ME!
     *
     * @param  mapService  DOCUMENT ME!
     */
    void mapServiceRemoved(MapService mapService);
}
