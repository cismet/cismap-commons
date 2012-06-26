/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.EventQueue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public abstract class AbstractCreateSearchGeometryListener extends CreateGeometryListener
        implements CreateSearchGeometryListener,
            PropertyChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(AbstractCreateSearchGeometryListener.class);

    // Now some property change event types.
    // We need to synchronize the mode and the last feature between AbstractCreateSearchGeometryListeners.
    public static final String PROPERTY_LAST_FEATURE = "PROPERTY_LAST_FEATURE";
    public static final String PROPERTY_MODE = "PROPERTY_MODE";
    // It's not enough to synchronize the mode and the last feature between AbstractCreateSearchGeometryListeners.
    // Mostly they have a visualizing component (like the white m on a green ball button). They have to be notified of
    // changes regarding mode or last feature.
    public static final String PROPERTY_FORGUI_LAST_FEATURE = "PROPERTY_FORGUI_LAST_FEATURE";
    public static final String PROPERTY_FORGUI_MODE = "PROPERTY_FORGUI_MODE";

    // Additionally we want an AbstractCreateSearchGeometryListener to be the same color, ... as the
    // MetaSearchCreateSearchGeometryListener, since it's the only one which can be set by its own option dialog.
    public static final String PROPERTY_NUM_OF_ELLIPSE_EDGES = "PROPERTY_NUM_OF_ELLIPSE_EDGES";
    public static final String PROPERTY_HOLD_GEOMETRIES = "PROPERTY_HOLD_GEOMETRIES";
    public static final String PROPERTY_SEARCH_COLOR = "PROPERTY_SEARCH_COLOR";
    public static final String PROPERTY_SEARCH_TRANSPARENCY = "PROPERTY_SEARCH_TRANSPARENCY";

    //~ Instance fields --------------------------------------------------------

    private boolean holdGeometries = false;
    private Color searchColor = Color.GREEN;
    private float searchTransparency = 0.5f;
    private PureNewFeature lastFeature;
    private PureNewFeature recentlyCreatedFeature;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CreateSearchGeometryListener object.
     *
     * @param  mc  DOCUMENT ME!
     */
    public AbstractCreateSearchGeometryListener(final MappingComponent mc) {
        super(mc, SearchFeature.class);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  listener  DOCUMENT ME!
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    @Override
    protected Color getFillingColor() {
        return new Color(searchColor.getRed(),
                searchColor.getGreen(),
                searchColor.getBlue(),
                255
                        - (int)(255f * searchTransparency));
    }

    @Override
    protected void finishGeometry(final PureNewFeature newFeature) {
        super.finishGeometry(newFeature);

        recentlyCreatedFeature = newFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    protected void cleanup(final PureNewFeature feature) {
        final PFeature pFeature = (PFeature)getMappingComponent().getPFeatureHM().get(feature);
        if (isHoldingGeometries()) {
            pFeature.moveToFront(); // funktioniert nicht?!
            feature.setEditable(true);
            getMappingComponent().getFeatureCollection().holdFeature(feature);
        } else {
            getMappingComponent().getTmpFeatureLayer().addChild(pFeature);

            // Transparenz animieren
            pFeature.animateToTransparency(0, 2500);
            // warten bis Animation zu Ende ist um Feature aus Liste zu entfernen
            new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while (pFeature.getTransparency() > 0) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                            }
                        }
                        getMappingComponent().getFeatureCollection().removeFeature(feature);
                    }
                }).start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newValue  DOCUMENT ME!
     */
    protected void setLastFeature(final PureNewFeature newValue) {
        final PureNewFeature oldValue = this.lastFeature;
        this.lastFeature = newValue;

        // Notify other AbstractCreateSearchGeometryListeners about the change.
        propertyChangeSupport.firePropertyChange(PROPERTY_LAST_FEATURE, oldValue, newValue);
        // And notify the visualizing component of this AbstractCreateSearchGeometryListener about the change.
        // setLastFeature(PureNewFeature) is called by this AbstractCreateSearchGeometryListener itself so the
        // visualizing component doesn't know about the new last feature.
        propertyChangeSupport.firePropertyChange(PROPERTY_FORGUI_LAST_FEATURE, oldValue, newValue);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void redoLastSearch() {
        search(lastFeature);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void showLastFeature() {
        showFeature(lastFeature);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    protected void showFeature(final PureNewFeature feature) {
        if (feature != null) {
            feature.setEditable(feature.getGeometryType() != PureNewFeature.geomTypes.MULTIPOLYGON);

            getMappingComponent().getFeatureCollection().addFeature(feature);
            if (isHoldingGeometries()) {
                getMappingComponent().getFeatureCollection().holdFeature(feature);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isHoldingGeometries() {
        return holdGeometries;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newValue  DOCUMENT ME!
     */
    @Override
    public void setHoldGeometries(final boolean newValue) {
        final boolean oldValue = this.holdGeometries;
        this.holdGeometries = newValue;

        propertyChangeSupport.firePropertyChange(PROPERTY_HOLD_GEOMETRIES, oldValue, newValue);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public float getSearchTransparency() {
        return searchTransparency;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newValue  searchTransparency DOCUMENT ME!
     */
    @Override
    public void setSearchTransparency(final float newValue) {
        final float oldValue = this.searchTransparency;
        this.searchTransparency = newValue;

        propertyChangeSupport.firePropertyChange(PROPERTY_SEARCH_TRANSPARENCY, oldValue, newValue);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Color getSearchColor() {
        final Color filling = getFillingColor();
        return new Color(filling.getRed(), filling.getGreen(), filling.getBlue());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newValue  DOCUMENT ME!
     */
    @Override
    public void setSearchColor(final Color newValue) {
        final Color oldValue = this.searchColor;
        this.searchColor = newValue;

        propertyChangeSupport.firePropertyChange(PROPERTY_SEARCH_COLOR, oldValue, newValue);
    }

    @Override
    public void setNumOfEllipseEdges(final int newValue) {
        final int oldValue = getNumOfEllipseEdges();

        super.setNumOfEllipseEdges(newValue);

        propertyChangeSupport.firePropertyChange(PROPERTY_NUM_OF_ELLIPSE_EDGES, oldValue, newValue);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public PureNewFeature getLastSearchFeature() {
        return lastFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  searchFeature  DOCUMENT ME!
     */
    @Override
    public void search(final PureNewFeature searchFeature) {
        if (searchFeature != null) {
            final boolean searchExecuted = performSearch(searchFeature);
            if (searchExecuted) {
                setLastFeature(searchFeature);
                showFeature(searchFeature);
                cleanup(searchFeature);
            }
        }
    }

    @Override
    public void mousePressed(final PInputEvent pInputEvent) {
        final boolean progressBefore = isInProgress();
        super.mousePressed(pInputEvent);

        if (recentlyCreatedFeature != null) {
            // super.mousePressed(pInputEvent) called this.finishGeometry(Feature) since the user created a new search
            // geometry
            search(recentlyCreatedFeature);
            // handleUserFinishedSearchGeometry(recentlyCreatedFeature);
            recentlyCreatedFeature = null;
        } else if ((!isInProgress() || (!progressBefore && isInProgress())) && (pInputEvent.getClickCount() == 2)) {
            handleDoubleClickInMap(pInputEvent);
        }
    }

    @Override
    public void mouseReleased(final PInputEvent arg0) {
        super.mouseReleased(arg0);

        if (recentlyCreatedFeature != null) {
            // super.mousePressed(pInputEvent) called finishGeometry since the user created a new search geometry
            search(recentlyCreatedFeature);
            // handleUserFinishedSearchGeometry(recentlyCreatedFeature);
            recentlyCreatedFeature = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    protected void handleUserFinishedSearchGeometry(final PureNewFeature feature) {
        getMappingComponent().getFeatureCollection().addFeature(feature);
        setLastFeature(feature);
        performSearch(feature);
        cleanup(feature);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pInputEvent  DOCUMENT ME!
     */
    protected void handleDoubleClickInMap(final PInputEvent pInputEvent) {
        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[] { PFeature.class });

        if (!(o instanceof PFeature)) {
            return;
        }
        final PFeature sel = (PFeature)o;

        if (!(sel.getFeature() instanceof SearchFeature)) {
            return;
        }
        final SearchFeature searchFeature = (SearchFeature)sel.getFeature();

        if (pInputEvent.isLeftMouseButton()) {
            getMappingComponent().getHandleLayer().removeAllChildren();
            search(searchFeature);
        }
    }

    @Override
    public void setMode(final String newValue) throws IllegalArgumentException {
        final String oldValue = getMode();
        super.setMode(newValue);

        // Notify other AbstractCreateSearchGeometryListeners about the change.
        propertyChangeSupport.firePropertyChange(PROPERTY_MODE, oldValue, newValue);
        // But here we don't need to notify the visualizing component of this AbstractCreateSearchGeometryListener about
        // the change, since this method is invoked by it. It already knows about the change.

        generateAndShowPointerAnnotation();
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (MappingComponent.PROPERTY_MAP_INTERACTION_MODE.equals(evt.getPropertyName())) {
            if (MappingComponent.CREATE_SEARCH_POLYGON.equals(evt.getNewValue())) {
                generateAndShowPointerAnnotation();
            }
        } else if (PROPERTY_LAST_FEATURE.equals(evt.getPropertyName())) {
            lastFeature = (PureNewFeature)evt.getNewValue();
            propertyChangeSupport.firePropertyChange(
                PROPERTY_FORGUI_LAST_FEATURE,
                evt.getOldValue(),
                evt.getNewValue());
        } else if (PROPERTY_MODE.equals(evt.getPropertyName())) {
            super.setMode(evt.getNewValue().toString());
            propertyChangeSupport.firePropertyChange(PROPERTY_FORGUI_MODE, evt.getOldValue(), evt.getNewValue());
        }
    }

    @Override
    public void mouseEntered(final PInputEvent event) {
        super.mouseEntered(event);

        if (event.isMouseEnteredOrMouseExited()) {
            generateAndShowPointerAnnotation();
        }
    }

    @Override
    public void mouseExited(final PInputEvent event) {
        super.mouseExited(event);

        if (event.isMouseEnteredOrMouseExited()) {
            getMappingComponent().setPointerAnnotationVisibility(false);
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected void generateAndShowPointerAnnotation() {
        final PNode pointerAnnotation = getPointerAnnotation();
        if (pointerAnnotation == null) {
            return;
        }

        final Runnable showPointerAnnotation = new Runnable() {

                @Override
                public void run() {
                    getMappingComponent().setPointerAnnotation(pointerAnnotation);
                    getMappingComponent().setPointerAnnotationVisibility(true);
                }
            };

        if (EventQueue.isDispatchThread()) {
            showPointerAnnotation.run();
        } else {
            EventQueue.invokeLater(showPointerAnnotation);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   searchFeature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected abstract boolean performSearch(final PureNewFeature searchFeature);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected abstract PNode getPointerAnnotation();
}
