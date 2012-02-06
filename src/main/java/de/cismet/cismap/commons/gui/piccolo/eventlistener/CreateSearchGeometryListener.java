/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PInputEvent;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.HeadlessException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.util.MissingResourceException;

import javax.swing.JOptionPane;

import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.MapSearchEvent;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class CreateSearchGeometryListener extends CreateGeometryListener implements PropertyChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROPERTY_LAST_FEATURE = "PROPERTY_LAST_FEATURE";
    public static final String PROPERTY_MODE = "PROPERTY_MODE";
    public static final String PROPERTY_NUM_OF_ELLIPSE_EDGES = "PROPERTY_NUM_OF_ELLIPSE_EDGES";
    public static final String PROPERTY_HOLD_GEOMETRIES = "PROPERTY_HOLD_GEOMETRIES";
    public static final String PROPERTY_SEARCH_COLOR = "PROPERTY_SEARCH_COLOR";
    public static final String PROPERTY_SEARCH_TRANSPARENCY = "PROPERTY_SEARCH_TRANSPARENCY";

    public static final String PROPERTY_FORGUI_LAST_FEATURE = "PROPERTY_FORGUI_LAST_FEATURE";
    public static final String PROPERTY_FORGUI_MODE = "PROPERTY_FORGUI_MODE";

    //~ Instance fields --------------------------------------------------------

    protected final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    protected boolean holdGeometries = false;
    protected Color searchColor = Color.GREEN;
    protected float searchTransparency = 0.5f;
    protected PureNewFeature lastFeature;
    protected PureNewFeature recentlyCreatedFeature;
    protected PropertyChangeSupport propertyChangeSupport;
    private MetaSearchFacade metaSearch;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CreateSearchGeometryListener object.
     *
     * @param  mc  DOCUMENT ME!
     */
    public CreateSearchGeometryListener(final MappingComponent mc) {
        super(mc, SearchFeature.class);

        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Creates a new CreateSearchGeometryListener object.
     *
     * @param  mc          DOCUMENT ME!
     * @param  metaSearch  DOCUMENT ME!
     */
    public CreateSearchGeometryListener(final MappingComponent mc, final MetaSearchFacade metaSearch) {
        this(mc);

        this.metaSearch = metaSearch;
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
     * @param  metaSearch  DOCUMENT ME!
     */
    public void setMetaSearch(final MetaSearchFacade metaSearch) {
        this.metaSearch = metaSearch;
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
     * @return  DOCUMENT ME!
     */
    private boolean isSearchTopicsSelected() {
        boolean result = false;

        if (metaSearch != null) {
            result = metaSearch.hasSelectedSearchTopics();
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  HeadlessException         DOCUMENT ME!
     * @throws  MissingResourceException  DOCUMENT ME!
     */
    private void notifyUserAboutMissingSearchTopics() throws HeadlessException, MissingResourceException {
        if (!isSearchTopicsSelected()) {
            if ((metaSearch != null) && metaSearch.hasSearchTopics()) {
                JOptionPane.showMessageDialog(
                    CismapBroker.getInstance().getMappingComponent(),
                    org.openide.util.NbBundle.getMessage(
                        CreateSearchGeometryListener.class,
                        "CreateSearchGeometryListener.mousePressed(PInputEvent).JOptionPane().noSearchTopicsChosen"),       // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CreateSearchGeometryListener.class,
                        "CreateSearchGeometryListener.mousePressed(PInputEvent).JOptionPane().noSearchTopicsChosen.title"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(
                    CismapBroker.getInstance().getMappingComponent(),
                    org.openide.util.NbBundle.getMessage(
                        CreateSearchGeometryListener.class,
                        "CreateSearchGeometryListener.mousePressed(PInputEvent).JOptionPane().notInitialized"),             // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        CreateSearchGeometryListener.class,
                        "CreateSearchGeometryListener.mousePressed(PInputEvent).JOptionPane().notInitialized.title"),       // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    protected void cleanup(final PureNewFeature feature) {
        final PFeature pFeature = (PFeature)mc.getPFeatureHM().get(feature);
        if (isHoldingGeometries()) {
            pFeature.moveToFront(); // funktioniert nicht?!
            feature.setEditable(true);
            mc.getFeatureCollection().holdFeature(feature);
        } else {
            mc.getTmpFeatureLayer().addChild(pFeature);

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
                        mc.getFeatureCollection().removeFeature(feature);
                    }
                }).start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  searchFeature  DOCUMENT ME!
     */
    protected void doSearch(final PureNewFeature searchFeature) {
        // Suche
        final MapSearchEvent mse = new MapSearchEvent();
        mse.setGeometry(searchFeature.getGeometry());
        CismapBroker.getInstance().fireMapSearchInited(mse);

        // letzte Suchgeometrie merken
        final PureNewFeature oldFeature = lastFeature;
        lastFeature = searchFeature;

        propertyChangeSupport.firePropertyChange(PROPERTY_LAST_FEATURE, oldFeature, searchFeature);
        propertyChangeSupport.firePropertyChange(PROPERTY_FORGUI_LAST_FEATURE, oldFeature, searchFeature);
    }

    /**
     * DOCUMENT ME!
     */
    public void redoLastSearch() {
        search(lastFeature);
    }

    /**
     * DOCUMENT ME!
     */
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

            mc.getFeatureCollection().addFeature(feature);
            if (isHoldingGeometries()) {
                mc.getFeatureCollection().holdFeature(feature);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isHoldingGeometries() {
        return holdGeometries;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  holdGeometries  DOCUMENT ME!
     */
    public void setHoldGeometries(final boolean holdGeometries) {
        final boolean oldValue = this.holdGeometries;
        this.holdGeometries = holdGeometries;

        propertyChangeSupport.firePropertyChange(PROPERTY_HOLD_GEOMETRIES, oldValue, this.holdGeometries);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public float getSearchTransparency() {
        return searchTransparency;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  searchTransparency  DOCUMENT ME!
     */
    public void setSearchTransparency(final float searchTransparency) {
        final float oldValue = this.searchTransparency;
        this.searchTransparency = searchTransparency;

        propertyChangeSupport.firePropertyChange(PROPERTY_SEARCH_TRANSPARENCY, oldValue, this.searchTransparency);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Color getSearchColor() {
        final Color filling = getFillingColor();
        return new Color(filling.getRed(), filling.getGreen(), filling.getBlue());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  color  DOCUMENT ME!
     */
    public void setSearchColor(final Color color) {
        final Color oldValue = this.searchColor;
        this.searchColor = color;

        propertyChangeSupport.firePropertyChange(PROPERTY_SEARCH_COLOR, oldValue, this.searchColor);
    }

    @Override
    public void setNumOfEllipseEdges(final int numOfEllipseEdges) {
        final int oldValue = getNumOfEllipseEdges();

        super.setNumOfEllipseEdges(numOfEllipseEdges);

        propertyChangeSupport.firePropertyChange(PROPERTY_NUM_OF_ELLIPSE_EDGES, oldValue, getNumOfEllipseEdges());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PureNewFeature getLastSearchFeature() {
        return lastFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  searchFeature  DOCUMENT ME!
     */
    public void search(final PureNewFeature searchFeature) {
        if (searchFeature != null) {
            doSearch(searchFeature);
            showFeature(searchFeature);
            cleanup(searchFeature);
        }
    }

    @Override
    public void mousePressed(final PInputEvent pInputEvent) {
        final boolean progressBefore = inProgress;
        super.mousePressed(pInputEvent);

        if (recentlyCreatedFeature != null) {
            // super.mousePressed(pInputEvent) called this.finishGeometry(Feature) since the user created a new search
            // geometry
            handleUserFinishedSearchGeometry(recentlyCreatedFeature);
            recentlyCreatedFeature = null;
        } else if ((!inProgress || (!progressBefore && inProgress)) && (pInputEvent.getClickCount() == 2)) {
            handleDoubleClickInMap(pInputEvent);
        }
    }

    @Override
    public void mouseReleased(final PInputEvent arg0) {
        super.mouseReleased(arg0);

        if (recentlyCreatedFeature != null) {
            // super.mousePressed(pInputEvent) called finishGeometry since the user created a new search geometry
            handleUserFinishedSearchGeometry(recentlyCreatedFeature);
            recentlyCreatedFeature = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    protected void handleUserFinishedSearchGeometry(final PureNewFeature feature) {
        if (!isSearchTopicsSelected()) {
            notifyUserAboutMissingSearchTopics();
            return;
        }

        mc.getFeatureCollection().addFeature(feature);

        doSearch(feature);

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

        if (!isSearchTopicsSelected()) {
            // finishGeometry is called before mousePressed. finishGeometry is not called if the user displayed the
            // last search feature. These conditions ensure that there is only one notification in any case.
            if (searchFeature.equals(lastFeature)) {
                notifyUserAboutMissingSearchTopics();
            }

            return;
        }

        if (pInputEvent.isLeftMouseButton()) {
            mc.getHandleLayer().removeAllChildren();
            // neue Suche mit Geometry auslösen
            ((CreateSearchGeometryListener)mc.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON)).search(
                searchFeature);
        }
    }

    @Override
    public void setMode(final String m) throws IllegalArgumentException {
        final String oldMode = getMode();
        super.setMode(m);

        propertyChangeSupport.firePropertyChange(PROPERTY_MODE, oldMode, m);

        generateAndShowPointerAnnotation();
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (MappingComponent.PROPERTY_MAP_INTERACTION_MODE.equals(evt.getPropertyName())) {
            if (MappingComponent.CREATE_SEARCH_POLYGON.equals(evt.getNewValue())) {
                generateAndShowPointerAnnotation();
            }
        } else if ((metaSearch != null) && metaSearch.isSearchTopicSelectedEvent(evt.getPropertyName())) {
            generateAndShowPointerAnnotation();
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
            mc.setPointerAnnotationVisibility(false);
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected void generateAndShowPointerAnnotation() {
        if (!MappingComponent.CREATE_SEARCH_POLYGON.equals(mc.getInteractionMode()) || (metaSearch == null)) {
            return;
        }

        final Runnable showPointerAnnotation = new Runnable() {

                @Override
                public void run() {
                    mc.setPointerAnnotation(metaSearch.generatePointerAnnotationForSelectedSearchTopics());
                    mc.setPointerAnnotationVisibility(true);
                }
            };

        if (EventQueue.isDispatchThread()) {
            showPointerAnnotation.run();
        } else {
            EventQueue.invokeLater(showPointerAnnotation);
        }
    }
}
