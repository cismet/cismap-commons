/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons;
import java.util.TreeMap;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface MappingModel {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    TreeMap getRasterServices();
//    public void putRasterService(int position,RasterService rasterService);
//    public void moveRasterService(int step);
//    public void removeRasterService(RasterService rasterService);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    TreeMap getFeatureServices();
//    public void putFeatureService(int position,FeatureService featureService);
//    public void moveFeatureService(int step);
//    public void removeFeatureService(FeatureService featureService);

    /**
     * DOCUMENT ME!
     *
     * @param  mml  DOCUMENT ME!
     */
    void addMappingModelListener(MappingModelListener mml);
    /**
     * DOCUMENT ME!
     *
     * @param  mml  DOCUMENT ME!
     */
    void removeMappingModelListener(MappingModelListener mml);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    BoundingBox getInitialBoundingBox();
//    public void setInitialBoundingBox(BoundingBox bb);
    /**
     * DOCUMENT ME!
     *
     * @param  layer  DOCUMENT ME!
     */
    void addLayer(RetrievalServiceLayer layer);
    /**
     * DOCUMENT ME!
     *
     * @param  layer  DOCUMENT ME!
     */
    void removeLayer(RetrievalServiceLayer layer);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Crs getSrs();
}
