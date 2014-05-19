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
package de.cismet.cismap.commons.util;

import java.util.HashMap;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SimpleCache<T> {

    //~ Instance fields --------------------------------------------------------

    private String[] ids;
    private int index = 0;
    private Map<String, T> dataMap;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Cache object.
     *
     * @param  size  DOCUMENT ME!
     */
    public SimpleCache(final int size) {
        init(size);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  size  DOCUMENT ME!
     */
    private void init(final int size) {
        ids = new String[size];
        index = 0;
        dataMap = new HashMap<String, T>();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  id    DOCUMENT ME!
     * @param  data  DOCUMENT ME!
     */
    public void add(final String id, final T data) {
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
     * @param  id    DOCUMENT ME!
     * @param  data  DOCUMENT ME!
     */
    public void add(final int id, final T data) {
        add(String.valueOf(id), data);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public T get(final String id) {
        final T res = dataMap.get(id);

        if (res != null) {
            while ((ids[index] == null) || (!ids[index].equals(id))) {
                increaseIndex();
            }
            increaseIndex();
        }

        return res;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   id  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public T get(final int id) {
        return get(String.valueOf(id));
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
