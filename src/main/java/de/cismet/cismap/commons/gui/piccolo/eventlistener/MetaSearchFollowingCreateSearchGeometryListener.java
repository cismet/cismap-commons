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
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import java.awt.Color;
import java.awt.Cursor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.MappingComponent;

import static de.cismet.cismap.commons.gui.piccolo.eventlistener.AbstractCreateSearchGeometryListener.PROPERTY_HOLD_GEOMETRIES;
import static de.cismet.cismap.commons.gui.piccolo.eventlistener.AbstractCreateSearchGeometryListener.PROPERTY_LAST_FEATURE;
import static de.cismet.cismap.commons.gui.piccolo.eventlistener.AbstractCreateSearchGeometryListener.PROPERTY_MODE;
import static de.cismet.cismap.commons.gui.piccolo.eventlistener.AbstractCreateSearchGeometryListener.PROPERTY_NUM_OF_ELLIPSE_EDGES;
import static de.cismet.cismap.commons.gui.piccolo.eventlistener.AbstractCreateSearchGeometryListener.PROPERTY_SEARCH_COLOR;
import static de.cismet.cismap.commons.gui.piccolo.eventlistener.AbstractCreateSearchGeometryListener.PROPERTY_SEARCH_TRANSPARENCY;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public abstract class MetaSearchFollowingCreateSearchGeometryListener extends AbstractCreateSearchGeometryListener
        implements PropertyChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    public static final String ACTION_SEARCH_STARTED = "ACTION_SEARCH_STARTED";

    //~ Instance fields --------------------------------------------------------

    private final MetaSearchCreateSearchGeometryListener metaSearchListener;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MetaSearchFollowingCreateSearchGeometryListener object.
     *
     * @param  mappingComponent  DOCUMENT ME!
     * @param  listenerName      DOCUMENT ME!
     */
    public MetaSearchFollowingCreateSearchGeometryListener(final MappingComponent mappingComponent,
            final String listenerName) {
        super(mappingComponent, listenerName);

        metaSearchListener = (MetaSearchCreateSearchGeometryListener)mappingComponent.getInputListener(
                MappingComponent.CREATE_SEARCH_POLYGON);
        metaSearchListener.addPropertyChangeListener(this);

        mappingComponent.addCustomInputListener(listenerName, this);
        mappingComponent.putCursor(listenerName, new Cursor(Cursor.CROSSHAIR_CURSOR));

        setMode(metaSearchListener.getMode());
        setLastFeature(metaSearchListener.getLastSearchFeature());
        setNumOfEllipseEdges(metaSearchListener.getNumOfEllipseEdges());
        setHoldGeometries(metaSearchListener.isHoldingGeometries());
        setSearchColor(metaSearchListener.getSearchColor());
        setSearchTransparency(metaSearchListener.getSearchTransparency());
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        final String propertyName = evt.getPropertyName();
        final Object newValue = evt.getNewValue();

        if (MappingComponent.PROPERTY_MAP_INTERACTION_MODE.equals(propertyName)) {
            if (getInputListenerName().equals(newValue)) {
                generateAndShowPointerAnnotation();
            }
        } else if (PROPERTY_LAST_FEATURE.equals(propertyName)) {
            super.setLastFeature((SearchFeature)newValue);
        } else if (PROPERTY_MODE.equals(propertyName)) {
            super.setMode(newValue.toString());
        } else if (PROPERTY_HOLD_GEOMETRIES.equals(propertyName) && (newValue instanceof Boolean)) {
            super.setHoldGeometries((Boolean)newValue);
        } else if (PROPERTY_NUM_OF_ELLIPSE_EDGES.equals(propertyName) && (newValue instanceof Integer)) {
            super.setNumOfEllipseEdges((Integer)newValue);
        } else if (PROPERTY_SEARCH_COLOR.equals(propertyName) && (newValue instanceof Color)) {
            super.setSearchColor((Color)newValue);
        } else if (PROPERTY_SEARCH_TRANSPARENCY.equals(propertyName) && (newValue instanceof Float)) {
            super.setSearchTransparency((Float)newValue);
        }
    }

    @Override
    public final void setMode(final String newValue) throws IllegalArgumentException {
        super.setMode(newValue);
        metaSearchListener.setMode(newValue);
    }

    @Override
    public final void setLastFeature(final SearchFeature newValue) {
        super.setLastFeature(newValue);
        metaSearchListener.setLastFeature(newValue);
    }

    @Override
    public final void setNumOfEllipseEdges(final int newValue) {
        super.setNumOfEllipseEdges(newValue);
        metaSearchListener.setNumOfEllipseEdges(newValue);
    }

    @Override
    public final void setHoldGeometries(final boolean newValue) {
        super.setHoldGeometries(newValue);
        metaSearchListener.setHoldGeometries(newValue);
    }

    @Override
    public final void setSearchColor(final Color newValue) {
        super.setSearchColor(newValue);
        metaSearchListener.setSearchColor(newValue);
    }

    @Override
    public final void setSearchTransparency(final float newValue) {
        super.setSearchTransparency(newValue);
        metaSearchListener.setSearchTransparency(newValue);
    }

    @Override
    protected boolean performSearch(final SearchFeature searchFeature) {
        final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();

        setLastFeature(searchFeature);
        propertyChangeSupport.firePropertyChange(ACTION_SEARCH_STARTED, null, searchFeature.getGeometry());

        return true;
    }
}
