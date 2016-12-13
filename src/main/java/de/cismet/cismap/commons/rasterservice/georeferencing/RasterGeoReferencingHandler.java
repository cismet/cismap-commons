/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.rasterservice.georeferencing;

import com.vividsolutions.jts.geom.Coordinate;

import java.awt.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.cismet.cismap.commons.rasterservice.ImageFileMetaData;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class RasterGeoReferencingHandler {

    //~ Instance fields --------------------------------------------------------

    private final List<PointCoordinatePair> pairs = new ArrayList<PointCoordinatePair>();
    private ListenerHandler listenerHandler = new ListenerHandler();
    private final ImageFileMetaData metaData;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RasterGeoReferencingHandler object.
     *
     * @param  metaData  DOCUMENT ME!
     */
    public RasterGeoReferencingHandler(final ImageFileMetaData metaData) {
        this.metaData = metaData;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ImageFileMetaData getMetaData() {
        return metaData;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean addListener(final RasterGeoReferencingHandlerListener listener) {
        return listenerHandler.add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean removeListener(final RasterGeoReferencingHandlerListener listener) {
        return listenerHandler.remove(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int addPair() {
        return addPair(null, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   point  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int addPair(final Point point) {
        return addPair(point, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   coordinate  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int addPair(final Coordinate coordinate) {
        return addPair(null, coordinate);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   point       DOCUMENT ME!
     * @param   coordinate  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int addPair(final Point point, final Coordinate coordinate) {
        final PointCoordinatePair pair = new PointCoordinatePair(point, coordinate);
        return addPair(pair);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pair  point DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public int addPair(final PointCoordinatePair pair) throws IllegalArgumentException {
        if (pair == null) {
            throw new IllegalArgumentException("the given pair is null");
        }
        final int position;

        synchronized (pairs) {
            position = pairs.size();
            pairs.add(position, (PointCoordinatePair)pair.clone());
        }
        listenerHandler.positionAdded(position);
        return position;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pair      DOCUMENT ME!
     * @param   position  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     * @throws  IllegalArgumentException   DOCUMENT ME!
     */
    public void setPair(final PointCoordinatePair pair, final int position) throws IndexOutOfBoundsException,
        IllegalArgumentException {
        if (pair == null) {
            throw new IllegalArgumentException("the given pair is null");
        }
        synchronized (pairs) {
            checḱPosition(position);
            pairs.set(position, (PointCoordinatePair)pair.clone());
            listenerHandler.positionChanged(position);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public boolean removePair(final int position) throws IndexOutOfBoundsException {
        synchronized (pairs) {
            checḱPosition(position);
            final boolean success = pairs.remove(position) != null;
            listenerHandler.positionRemoved(position);
            return success;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getNumOfPairs() {
        return pairs.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int[] getIncompletePairPositions() {
        final List<PointCoordinatePair> pairs = new ArrayList<PointCoordinatePair>(this.pairs);
        final List<Integer> incompletePairs = new ArrayList<Integer>(pairs.size());
        for (int position = 0; position < pairs.size(); position++) {
            final PointCoordinatePair pair = pairs.get(position);
            if ((pair == null) || (pair.getPoint() == null) || (pair.getCoordinate() == null)) {
                incompletePairs.add(position);
            }
        }
        final int[] array = new int[incompletePairs.size()];
        System.arraycopy(incompletePairs.toArray(new Integer[0]), 0, array, 0, array.length);
        return array;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public PointCoordinatePair getPair(final int position) throws IndexOutOfBoundsException {
        synchronized (pairs) {
            checḱPosition(position);
            return pairs.get(position);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public Point getPoint(final int position) throws IndexOutOfBoundsException {
        return getPair(position).getPoint();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   point     DOCUMENT ME!
     * @param   position  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public boolean setPoint(final Point point, final int position) throws IndexOutOfBoundsException {
        if (position == pairs.size()) {
            return addPair(point) >= 0;
        } else {
            final PointCoordinatePair pair = getPair(position);
            if (pair != null) {
                pair.setPoint(point);
                listenerHandler.positionChanged(position);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public Coordinate getCoordinate(final int position) throws IndexOutOfBoundsException {
        return getPair(position).getCoordinate();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   coordinate  DOCUMENT ME!
     * @param   position    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public boolean setCoordinate(final Coordinate coordinate, final int position) throws IndexOutOfBoundsException {
        if (position == pairs.size()) {
            return addPair(coordinate) >= 0;
        } else {
            final PointCoordinatePair pair = getPair(position);
            if (pair != null) {
                pair.setCoordinate(coordinate);
                listenerHandler.positionChanged(position);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public void checḱPosition(final int position) throws IndexOutOfBoundsException {
        if ((position < 0) || (position >= pairs.size())) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PointCoordinatePair[] getPairs() {
        final List<PointCoordinatePair> pairs = new ArrayList<PointCoordinatePair>(this.pairs);
        final PointCoordinatePair[] array = new PointCoordinatePair[pairs.size()];
        for (int position = 0; position < pairs.size(); position++) {
            final PointCoordinatePair pair = pairs.get(position);
            final Point point = pair.getPoint();
            final Coordinate coordinate = pair.getCoordinate();
            array[position] = new PointCoordinatePair(point, coordinate);
        }
        return array;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ListenerHandler implements RasterGeoReferencingHandlerListener {

        //~ Instance fields ----------------------------------------------------

        private final Collection<RasterGeoReferencingHandlerListener> listeners =
            new ArrayList<RasterGeoReferencingHandlerListener>();

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   listener  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean add(final RasterGeoReferencingHandlerListener listener) {
            return listeners.add(listener);
        }

        /**
         * DOCUMENT ME!
         *
         * @param   listener  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean remove(final RasterGeoReferencingHandlerListener listener) {
            return listeners.remove(listener);
        }

        @Override
        public void positionAdded(final int position) {
            for (final RasterGeoReferencingHandlerListener listener : listeners) {
                listener.positionAdded(position);
            }
        }

        @Override
        public void positionRemoved(final int position) {
            for (final RasterGeoReferencingHandlerListener listener : listeners) {
                listener.positionRemoved(position);
            }
        }

        @Override
        public void positionChanged(final int position) {
            for (final RasterGeoReferencingHandlerListener listener : listeners) {
                listener.positionChanged(position);
            }
        }

        @Override
        public void pointSelected(final int position) {
            for (final RasterGeoReferencingHandlerListener listener : listeners) {
                listener.pointSelected(position);
            }
        }

        @Override
        public void coordinateSelected(final int position) {
            for (final RasterGeoReferencingHandlerListener listener : listeners) {
                listener.coordinateSelected(position);
            }
        }
    }
}
