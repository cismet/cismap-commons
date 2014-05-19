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

import com.vividsolutions.jts.geom.Geometry;

import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.feature.FeatureCollection;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ShapeInfo {

    //~ Instance fields --------------------------------------------------------

    // caches the last feature properties
    private Cache<LinkedHashMap<String, Object>> cache = new Cache<LinkedHashMap<String, Object>>(2);
    private Cache<Geometry> geoCache = new Cache<Geometry>(1);

    private String typename;
    private ShapeFile file;
    private int srid;
    private FeatureCollection fc;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeInfo object.
     *
     * @param  typename  DOCUMENT ME!
     * @param  file      DOCUMENT ME!
     * @param  srid      DOCUMENT ME!
     * @param  fc        DOCUMENT ME!
     */
    public ShapeInfo(final String typename, final ShapeFile file, final int srid, final FeatureCollection fc) {
        this.typename = typename;
        this.file = file;
        this.srid = srid;
        this.fc = fc;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the typename
     */
    public String getTypename() {
        return typename;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  typename  the typename to set
     */
    public void setTypename(final String typename) {
        this.typename = typename;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the file
     */
    public ShapeFile getFile() {
        return file;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  file  the file to set
     */
    public void setFile(final ShapeFile file) {
        this.file = file;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the srid
     */
    public int getSrid() {
        return srid;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  srid  the srid to set
     */
    public void setSrid(final int srid) {
        this.srid = srid;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the fc
     */
    public FeatureCollection getFc() {
        return fc;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fc  the fc to set
     */
    public void setFc(final FeatureCollection fc) {
        this.fc = fc;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public synchronized LinkedHashMap<String, Object> getPropertiesFromCache(final int id) {
        return cache.get(id);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  id         DOCUMENT ME!
     * @param  container  DOCUMENT ME!
     */
    public synchronized void addPropertiesToCache(final int id, final LinkedHashMap<String, Object> container) {
        cache.add(id, container);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public synchronized Geometry getGeometryFromCache(final int id) {
        return geoCache.get(id);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  id   DOCUMENT ME!
     * @param  geo  container DOCUMENT ME!
     */
    public synchronized void addGeometryToCache(final int id, final Geometry geo) {
        geoCache.add(id, geo);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class Cache<T> {

        //~ Instance fields ----------------------------------------------------

        private Integer[] ids;
        private int index = 0;
        private Map<Integer, T> dataMap;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Cache object.
         *
         * @param  size  DOCUMENT ME!
         */
        public Cache(final int size) {
            init(size);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  size  DOCUMENT ME!
         */
        private void init(final int size) {
            ids = new Integer[size];
            index = 0;
            dataMap = new HashMap<Integer, T>();
        }

        /**
         * DOCUMENT ME!
         *
         * @param  id    DOCUMENT ME!
         * @param  data  DOCUMENT ME!
         */
        public void add(final int id, final T data) {
            if (ids[index] != null) {
                dataMap.remove(ids[index]);
            }

            ids[index] = id;
            increaseIndex();

            dataMap.put(id, data);
        }

        /**
         * DOCUMENT ME!
         *
         * @param   id  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public T get(final int id) {
            final T res = dataMap.get(id);

            if (res != null) {
                while ((ids[index] == null) || (ids[index] != id)) {
                    increaseIndex();
                }
                increaseIndex();
            }

            return res;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  size  DOCUMENT ME!
         */
        private void setSize(final int size) {
            init(size);
        }

        /**
         * DOCUMENT ME!
         */
        private void increaseIndex() {
            ++index;
            if (index >= ids.length) {
                index = 0;
            }
        }
    }
}
