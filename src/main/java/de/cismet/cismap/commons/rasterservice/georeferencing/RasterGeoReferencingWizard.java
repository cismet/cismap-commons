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

import lombok.Getter;

import java.awt.Point;

import java.util.ArrayList;
import java.util.Collection;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

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

    @Getter private RasterGeoReferencingHandler handler;

    @Getter private Point selectedPoint;

    @Getter private Coordinate selectedCoordinate;

    private SelectionMode selectionMode = SelectionMode.NONE;

    @Getter private int position = 0;

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
    public boolean isPointSelectionMode() {
        return SelectionMode.POINT == selectionMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isCoordinateSelectionMode() {
        return SelectionMode.COORDINATE == selectionMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  position  DOCUMENT ME!
     */
    private void setPosition(final int position) {
        this.position = position;
    }

    /**
     * DOCUMENT ME!
     */
    public void forward() {
        final int position = getPosition();
        if (isPointSelected()) {
            selectCoordinate(position);
        } else if (isCoordinateSelected()) {
            if ((position + 1) == getHandler().getNumOfPairs()) {
                getHandler().addPair();
            }
            selectPoint(position + 1);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void backward() {
        if (isPointSelected()) {
            if (getPosition() > 0) {
                selectCoordinate(getPosition() - 1);
            } else {
                selectCoordinate(getHandler().getNumOfPairs() - 1);
            }
        } else if (isCoordinateSelected()) {
            selectPoint(getPosition());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newHandler  DOCUMENT ME!
     */
    public void setHandler(final RasterGeoReferencingHandler newHandler) {
        final RasterGeoReferencingHandler oldHandler = getHandler();

        final boolean handlerChanged = ((newHandler != null) && !newHandler.equals(oldHandler))
                    || ((newHandler == null) && (oldHandler != null));
        if (handlerChanged) {
            if (newHandler != null) {
                newHandler.addListener(listenerHandler);
            }

            this.handler = newHandler;
            listenerHandler.handlerChanged(newHandler);

            if (oldHandler != null) {
                oldHandler.removeListener(listenerHandler);
            }
        }
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
        final Point point = getHandler().getPoint(position);
        final boolean changed = (selectedPoint == null) || ((point == null) && (selectedPoint != null))
                    || ((point != null) && !point.equals(selectedPoint));
        if (changed) {
            setPosition(position);
            selectionMode = SelectionMode.POINT;
            selectedPoint = point;
            selectedCoordinate = null;
            listenerHandler.pointSelected(position);
        }
        CismapBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.GEO_REF);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public void selectCoordinate(final int position) throws IndexOutOfBoundsException {
        final Coordinate coordinate = getHandler().getCoordinate(position);
        final boolean changed = (selectedCoordinate == null) || ((coordinate == null) && (selectedCoordinate != null))
                    || ((coordinate != null) && !coordinate.equals(selectedCoordinate));
        if (changed) {
            setPosition(position);
            selectionMode = SelectionMode.COORDINATE;
            selectedPoint = null;
            selectedCoordinate = coordinate;
            listenerHandler.coordinateSelected(position);
        }
        CismapBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.GEO_REF);
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
            if (getHandler().getNumOfPairs() == 1) {
                selectPoint(0);
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
