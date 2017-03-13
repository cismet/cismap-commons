/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.rasterservice.georeferencing;

import com.vividsolutions.jts.geom.Coordinate;

import java.awt.Point;

import java.util.ArrayList;
import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class RasterGeoReferencingWizard {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private enum SelectionMode {

        //~ Enum constants -----------------------------------------------------

        POINT, COORDINATE, NONE
    }

    //~ Instance fields --------------------------------------------------------

    private final ListenerHandler listenerHandler = new ListenerHandler();

    private RasterGeoReferencingHandler handler;
    private Point selectedPoint;
    private Coordinate selectedCoordinate;

    private SelectionMode selectionMode = SelectionMode.NONE;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RasterGeoReferencingWizard object.
     */
    private RasterGeoReferencingWizard() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean addListener(final RasterGeoReferencingWizardListener listener) {
        return listenerHandler.add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean removeListener(final RasterGeoReferencingWizardListener listener) {
        return listenerHandler.remove(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isPointSelected() {
        return SelectionMode.POINT.equals(selectionMode);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isCoordinateSelected() {
        return SelectionMode.COORDINATE.equals(selectionMode);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public RasterGeoReferencingHandler getHandler() {
        return handler;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  handler  DOCUMENT ME!
     */
    public void setHandler(final RasterGeoReferencingHandler handler) {
        final boolean handlerChanged = ((handler != null) && !handler.equals(this.handler))
                    || ((handler == null) && (this.handler != null));
        if (handlerChanged) {
            final RasterGeoReferencingHandler oldHandler = this.handler;
            if (handler != null) {
                handler.addListener(listenerHandler);
            }

            this.handler = handler;
            listenerHandler.handlerChanged(handler);

            if (oldHandler != null) {
                oldHandler.removeListener(listenerHandler);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Point getSelectedPoint() {
        return selectedPoint;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Coordinate getSelectedCoordinate() {
        return selectedCoordinate;
    }

    /**
     * DOCUMENT ME!
     */
    public void clearSelection() {
        selectionMode = SelectionMode.NONE;
        selectedPoint = null;
        selectedCoordinate = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public void selectPoint(final int position) throws IndexOutOfBoundsException {
        final Point point = handler.getPoint(position);
        final boolean changed = ((point == null) && (selectedPoint != null))
                    || ((point != null) && !point.equals(selectedPoint));
        if (changed) {
            selectionMode = SelectionMode.POINT;
            selectedPoint = point;
            selectedCoordinate = null;
            listenerHandler.pointSelected(position);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public void selectCoordinate(final int position) throws IndexOutOfBoundsException {
        final Coordinate coordinate = handler.getCoordinate(position);
        final boolean changed = ((coordinate == null) && (selectedCoordinate != null))
                    || ((coordinate != null) && !coordinate.equals(selectedCoordinate));
        if (changed) {
            selectionMode = SelectionMode.COORDINATE;
            selectedPoint = null;
            selectedCoordinate = coordinate;
            listenerHandler.coordinateSelected(position);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static RasterGeoReferencingWizard getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final RasterGeoReferencingWizard INSTANCE = new RasterGeoReferencingWizard();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ListenerHandler implements RasterGeoReferencingWizardListener {

        //~ Instance fields ----------------------------------------------------

        private final Collection<RasterGeoReferencingWizardListener> listeners =
            new ArrayList<RasterGeoReferencingWizardListener>();

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   listener  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean add(final RasterGeoReferencingWizardListener listener) {
            return listeners.add(listener);
        }

        /**
         * DOCUMENT ME!
         *
         * @param   listener  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean remove(final RasterGeoReferencingWizardListener listener) {
            return listeners.remove(listener);
        }

        @Override
        public void pointSelected(final int position) {
            for (final RasterGeoReferencingWizardListener listener : listeners) {
                listener.pointSelected(position);
            }
        }

        @Override
        public void coordinateSelected(final int position) {
            for (final RasterGeoReferencingWizardListener listener : listeners) {
                listener.coordinateSelected(position);
            }
        }

        @Override
        public void handlerChanged(final RasterGeoReferencingHandler handler) {
            for (final RasterGeoReferencingWizardListener listener : listeners) {
                listener.handlerChanged(handler);
            }
        }

        @Override
        public void positionAdded(final int position) {
            for (final RasterGeoReferencingWizardListener listener : listeners) {
                listener.positionAdded(position);
            }
        }

        @Override
        public void positionRemoved(final int position) {
            for (final RasterGeoReferencingWizardListener listener : listeners) {
                listener.positionRemoved(position);
            }
        }

        @Override
        public void positionChanged(final int position) {
            for (final RasterGeoReferencingWizardListener listener : listeners) {
                listener.positionChanged(position);
            }
        }
    }
}
