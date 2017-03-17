/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.rasterservice.georeferencing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.geom.util.AffineTransformationBuilder;

import lombok.AccessLevel;
import lombok.Getter;

import java.awt.Point;
import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.RasterGeoRefFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.ImageFileMetaData;
import de.cismet.cismap.commons.rasterservice.ImageRasterService;

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

    private final List<PointCoordinatePair> pairs = new ArrayList<>();
    @Getter(AccessLevel.PRIVATE)
    private final ListenerHandler listenerHandler = new ListenerHandler();
    @Getter(AccessLevel.PRIVATE)
    private final Map<Integer, Boolean> positionStates = new HashMap<>();
    @Getter private final ImageFileMetaData metaData;
    @Getter private final RasterGeoRefFeature feature;
    @Getter private final ImageRasterService service;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RasterGeoReferencingHandler object.
     *
     * @param  service   DOCUMENT ME!
     * @param  metaData  DOCUMENT ME!
     */
    public RasterGeoReferencingHandler(final ImageRasterService service, final ImageFileMetaData metaData) {
        this.service = service;
        this.metaData = metaData;
        this.feature = new RasterGeoRefFeature(this);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ArrayIndexOutOfBoundsException  DOCUMENT ME!
     */
    public boolean isPositionEnabled(final int position) throws ArrayIndexOutOfBoundsException {
        checḱPosition(position);
        return Boolean.TRUE.equals(getPositionStates().get(position));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     * @param   enabled   DOCUMENT ME!
     *
     * @throws  ArrayIndexOutOfBoundsException  DOCUMENT ME!
     */
    public void setPositionEnabled(final int position, final boolean enabled) throws ArrayIndexOutOfBoundsException {
        checḱPosition(position);
        final boolean changed = !new Boolean(enabled).equals(getPositionStates().get(position));
        if (changed) {
            getPositionStates().put(position, enabled);
            updateTransformation();
            getListenerHandler().positionChanged(position);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isComplete() {
        return getCompletePairs().length >= 3;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean addListener(final RasterGeoReferencingHandlerListener listener) {
        return getListenerHandler().add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean removeListener(final RasterGeoReferencingHandlerListener listener) {
        return getListenerHandler().remove(listener);
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
            getPositionStates().put(position, (pair.getPoint() != null) && (pair.getCoordinate() != null));
        }
        getListenerHandler().positionAdded(position);
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
            getListenerHandler().positionChanged(position);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void removeAllPairs() {
        while (getNumOfPairs() > 0) {
            removePair(getNumOfPairs() - 1);
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
            if (success) {
                getPositionStates().remove(position);
                getListenerHandler().positionRemoved(position);
            }
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
    public PointCoordinatePair[] getCompletePairs() {
        final List<PointCoordinatePair> pairs = Arrays.asList(getPairs(Boolean.TRUE));
        final List<PointCoordinatePair> completePairs = new ArrayList<PointCoordinatePair>(pairs.size());
        for (int position = 0; position < pairs.size(); position++) {
            final PointCoordinatePair pair = pairs.get(position);
            if ((pair != null) && (pair.getPoint() != null) && (pair.getCoordinate() != null)) {
                completePairs.add(pair);
            }
        }
        return completePairs.toArray(new PointCoordinatePair[0]);
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
     * @param   position  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public Coordinate getPointCoordinate(final int position) throws IndexOutOfBoundsException {
        final Point point = getPoint(position);
        if (point != null) {
            final AffineTransformation transform = getMetaData().getTransform();
            if (transform != null) {
                return transform.transform(new Coordinate(point.getX(), point.getY()), new Coordinate());
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     * @param   point     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public boolean setPoint(final int position, final Point point) throws IndexOutOfBoundsException {
        if (position == pairs.size()) {
            return addPair(point) >= 0;
        } else {
            final PointCoordinatePair pair = getPair(position);
            if (pair != null) {
                pair.setPoint(point);
                getListenerHandler().positionChanged(position);
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
     * @param   position    DOCUMENT ME!
     * @param   coordinate  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public boolean setCoordinate(final int position, final Coordinate coordinate) throws IndexOutOfBoundsException {
        if (position == pairs.size()) {
            return addPair(coordinate) >= 0;
        } else {
            final PointCoordinatePair pair = getPair(position);
            if (pair != null) {
                pair.setCoordinate(coordinate);
                getListenerHandler().positionChanged(position);
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
        return getPairs(null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   enabled  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PointCoordinatePair[] getPairs(final Boolean enabled) {
        final List<PointCoordinatePair> pairs = new ArrayList<PointCoordinatePair>(this.pairs);
        final PointCoordinatePair[] array = new PointCoordinatePair[pairs.size()];
        for (int position = 0; position < pairs.size(); position++) {
            final boolean include = (enabled == null) || enabled.equals(getPositionStates().get(position));
            if (include) {
                final PointCoordinatePair pair = pairs.get(position);
                final Point point = pair.getPoint();
                final Coordinate coordinate = pair.getCoordinate();
                array[position] = new PointCoordinatePair(point, coordinate);
            }
        }
        return array;
    }

    /**
     * DOCUMENT ME!
     */
    public void doCoordinateCorrection() {
        final AffineTransformation transform = updateTransformation();
        if (transform != null) {
            for (final PointCoordinatePair pair : getPairs()) {
                final Coordinate point = new Coordinate(pair.getPoint().getX(), pair.getPoint().getY());
                pair.setCoordinate(transform.transform(point, new Coordinate()));
            }
        }
        updateTransformation();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public AffineTransformation updateTransformation() {
        final List<AffineTransformation> transforms = new ArrayList<>();
        final PointCoordinatePair[] completePairs = getCompletePairs();
        if (completePairs.length >= 3) {
            for (final Object[] arr : RasterGeoReferencingHandler.getCombinations(completePairs, 3)) {
                final PointCoordinatePair pair0 = (PointCoordinatePair)arr[0];
                final PointCoordinatePair pair1 = (PointCoordinatePair)arr[1];
                final PointCoordinatePair pair2 = (PointCoordinatePair)arr[2];

                final AffineTransformationBuilder builder = new AffineTransformationBuilder(
                        new Coordinate(pair0.getPoint().getX(), pair0.getPoint().getY()),
                        new Coordinate(pair1.getPoint().getX(), pair1.getPoint().getY()),
                        new Coordinate(pair2.getPoint().getX(), pair2.getPoint().getY()),
                        pair0.getCoordinate(),
                        pair1.getCoordinate(),
                        pair2.getCoordinate());

                final AffineTransformation transform = builder.getTransformation();
                if (transform != null) {
                    transforms.add(transform);
                }
            }

            final AffineTransformation avgTransform = createAverageTransformation(transforms);

            final Rectangle imageBounds = getMetaData().getImageBounds();

            final GeometryFactory factory = new GeometryFactory(
                    new PrecisionModel(),
                    CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode()));
            final LinearRing linear = factory.createLinearRing(
                    new Coordinate[] {
                        new Coordinate(imageBounds.getMinX(), imageBounds.getMinY()),
                        new Coordinate(imageBounds.getMaxX(), imageBounds.getMinY()),
                        new Coordinate(imageBounds.getMaxX(), imageBounds.getMaxY()),
                        new Coordinate(imageBounds.getMinX(), imageBounds.getMaxY()),
                        new Coordinate(imageBounds.getMinX(), imageBounds.getMinY())
                    });

            final Envelope imageEnvelope = avgTransform.transform(factory.createPolygon(linear)).getEnvelopeInternal();
            getMetaData().setTransform(avgTransform);
            getMetaData().setImageEnvelope(imageEnvelope);
            return avgTransform;
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   transforms  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static AffineTransformation createAverageTransformation(final List<AffineTransformation> transforms) {
        final double[] avg = new double[6];
        for (final AffineTransformation transform : transforms) {
            final double[] matrix = transform.getMatrixEntries();
            for (int i = 0; i < avg.length; i++) {
                avg[i] += matrix[i] / transforms.size();
            }
        }
        return new AffineTransformation(avg);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getError(final int position) {
        final PointCoordinatePair pair = pairs.get(position);
        if ((getMetaData().getTransform() != null) && (pair != null) && (pair.getPoint() != null)
                    && (pair.getCoordinate() != null)) {
            final Coordinate point = new Coordinate(pair.getPoint().getX(), pair.getPoint().getY());
            final Coordinate transformedPoint = getMetaData().getTransform().transform(point, new Coordinate());
            return transformedPoint.distance(pair.getCoordinate());
        } else {
            return 0;
        }
    }

    /**
     * public static List<Object[]> comb(final Object[] input, final int k) { final List<int[]> indices = comb(input,
     * k); }.
     *
     * @param   input    DOCUMENT ME!
     * @param   setSize  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List<Object[]> getCombinations(final Object[] input, final int setSize) {
        final List<Object[]> subsets = new ArrayList<>();

        final int[] indices = new int[setSize]; // here we'll keep indices
        // pointing to elements in input array

        if (setSize <= input.length) {
            // store first 'setSize' number of indices
            for (int index = 0; index < setSize; index++) {
                indices[index] = index;
            }
            subsets.add(getSubset(input, indices));

            int index;
            do {
                // find position of item that can be incremented
                index = setSize - 1;
                while ((index >= 0) && (indices[index] == (input.length - setSize + index))) {
                    index--;
                }
                if (index >= 0) {
                    indices[index]++;                         // increment this item
                    for (++index; index < setSize; index++) { // fill up remaining items
                        indices[index] = indices[index - 1] + 1;
                    }
                    subsets.add(getSubset(input, indices));
                }
            } while (index >= 0);
        }
        return subsets;
    }

    /**
     * generate actual subset by index sequence.
     *
     * @param   input    DOCUMENT ME!
     * @param   indices  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Object[] getSubset(final Object[] input, final int[] indices) {
        final Object[] result = new Object[indices.length];
        for (int index = 0; index < indices.length; index++) {
            result[index] = input[indices[index]];
        }
        return result;
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
            updateTransformation();
            for (final RasterGeoReferencingHandlerListener listener : listeners) {
                listener.positionAdded(position);
            }
        }

        @Override
        public void positionRemoved(final int position) {
            updateTransformation();
            for (final RasterGeoReferencingHandlerListener listener : listeners) {
                listener.positionRemoved(position);
            }
        }

        @Override
        public void positionChanged(final int position) {
            updateTransformation();
            for (final RasterGeoReferencingHandlerListener listener : listeners) {
                listener.positionChanged(position);
            }
        }
    }
}
