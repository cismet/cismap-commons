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
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.geom.util.AffineTransformationBuilder;

import org.apache.log4j.Logger;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.gui.piccolo.eventlistener.RasterGeoRefFeature;
import de.cismet.cismap.commons.rasterservice.ImageFileMetaData;

import de.cismet.tools.gui.StaticSwingTools;

import static de.cismet.cismap.commons.rasterservice.georeferencing.RasterGeoReferencingDialog.getInstance;

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

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(RasterGeoReferencingHandler.class);

    //~ Instance fields --------------------------------------------------------

    private final List<PointCoordinatePair> pairs = new ArrayList<PointCoordinatePair>();
    private final ListenerHandler listenerHandler = new ListenerHandler();
    private final ImageFileMetaData metaData;
    private final Map<Integer, Boolean> positionStates = new HashMap<Integer, Boolean>();
    private final RasterGeoRefFeature feature;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RasterGeoReferencingHandler object.
     *
     * @param  metaData  DOCUMENT ME!
     */
    public RasterGeoReferencingHandler(final ImageFileMetaData metaData) {
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
        return Boolean.TRUE.equals(positionStates.get(position));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public RasterGeoRefFeature getFeature() {
        return feature;
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
        final boolean changed = !new Boolean(enabled).equals(positionStates.get(position));
        if (changed) {
            positionStates.put(position, enabled);
            updateTransformation();
            listenerHandler.positionChanged(position);
        }
    }

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
            positionStates.put(position, Boolean.TRUE);
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
            if (success) {
                positionStates.remove(position);
                listenerHandler.positionRemoved(position);
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
            final boolean include = (enabled == null) || enabled.equals(positionStates.get(position));
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
        final AffineTransform transform = updateTransformation();
        for (final PointCoordinatePair pair : getPairs()) {
            final Point2D correctedCoord = transform.transform(pair.getPoint(), null);
            pair.setCoordinate(new Coordinate(correctedCoord.getX(), correctedCoord.getY()));
        }
        updateTransformation();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private AffineTransform updateTransformation() {
        final List<AffineTransform> transforms = new ArrayList<>();
        final Envelope imageEnvelope;

        final AffineTransform avgTransform;
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

                final AffineTransformation t = builder.getTransformation();
                if (t != null) {
                    final double[] matrix = t.getMatrixEntries();
                    final AffineTransform transform = new AffineTransform(
                            matrix[0],
                            matrix[1],
                            matrix[3],
                            matrix[4],
                            matrix[2],
                            matrix[5]);
                    transforms.add(transform);
                    LOG.info(transform);
                }
            }

            avgTransform = createAverageTransformation(transforms);

            final Rectangle imageBounds = metaData.getImageBounds();
            final Rectangle transformedBounds = ((Path2D)avgTransform.createTransformedShape(imageBounds)).getBounds();
            imageEnvelope = new Envelope(
                    transformedBounds.getX(),
                    transformedBounds.getX()
                            + transformedBounds.getWidth(),
                    transformedBounds.getY(),
                    transformedBounds.getY()
                            + transformedBounds.getHeight());
        } else {
            avgTransform = null;
            imageEnvelope = null;
        }
        metaData.setTransform(avgTransform);
        metaData.setImageEnvelope(imageEnvelope);
        return avgTransform;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   transforms  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static AffineTransform createAverageTransformation(final List<AffineTransform> transforms) {
        final double[] avg = new double[6];
        for (final AffineTransform transform : transforms) {
            final double[] matrix = new double[6];
            transform.getMatrix(matrix);
            for (int i = 0; i < avg.length; i++) {
                avg[i] += matrix[i] / transforms.size();
            }
        }
        return new AffineTransform(avg);
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
        if ((metaData.getTransform() != null) && (pair != null) && (pair.getPoint() != null)
                    && (pair.getCoordinate() != null)) {
            final Point2D dest = metaData.getTransform().transform(pair.getPoint(), null);
            final Point2D checkDst = new Point.Double(pair.getCoordinate().x, pair.getCoordinate().y);
            return dest.distance(checkDst);
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
