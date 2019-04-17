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
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.util.AffineTransformation;

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

import de.cismet.tools.transformations.PointCoordinatePair;
import de.cismet.tools.transformations.TransformationTools;

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
    @Getter(AccessLevel.PRIVATE)
    private final AffineTransformation initialTransform;

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
        this.initialTransform = metaData.getTransform();
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
        updateTransformation();
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
            updateTransformation();
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
                updateTransformation();
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
                updateTransformation();
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
    public void updateTransformation() {
        final AffineTransformation oldTransformation = getMetaData().getTransform();
        final Polygon imageBoundsGeometry = createPolygon(getMetaData().getImageBounds());

        final AffineTransformation avgTransform = TransformationTools.calculateAvgTransformation(
                getCompletePairs());
        final AffineTransformation transform = (avgTransform != null) ? avgTransform : getInitialTransform();
        if (!transform.equals(oldTransformation)) {
            final Envelope imageEnvelope = transform.transform(imageBoundsGeometry).getEnvelopeInternal();

            getMetaData().setTransform(transform);
            getMetaData().setImageEnvelope(imageEnvelope);
            getListenerHandler().transformationChanged();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   bounds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Polygon createPolygon(final Rectangle bounds) {
        final GeometryFactory factory = new GeometryFactory(
                new PrecisionModel(),
                CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode()));

        final LinearRing linear = factory.createLinearRing(
                new Coordinate[] {
                    new Coordinate(bounds.getMinX(), bounds.getMinY()),
                    new Coordinate(bounds.getMaxX(), bounds.getMinY()),
                    new Coordinate(bounds.getMaxX(), bounds.getMaxY()),
                    new Coordinate(bounds.getMinX(), bounds.getMaxY()),
                    new Coordinate(bounds.getMinX(), bounds.getMinY())
                });
        return factory.createPolygon(linear);
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
        public void transformationChanged() {
            for (final RasterGeoReferencingHandlerListener listener : listeners) {
                listener.transformationChanged();
            }
        }
    }
}
