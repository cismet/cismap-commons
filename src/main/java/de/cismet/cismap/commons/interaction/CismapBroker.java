/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.interaction;

import com.vividsolutions.jts.geom.Coordinate;

import edu.umd.cs.piccolox.event.PNotificationCenter;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;

import org.openide.util.Exceptions;

import java.awt.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.SwingWorker;

import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.MappingModelListener;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.featureservice.style.BasicFeatureStyleDialogFactory;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.LayerWidget;
import de.cismet.cismap.commons.gui.options.GPSDirectionOptions;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MeasurementListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MetaSearchFacade;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.interaction.events.CapabilityEvent;
import de.cismet.cismap.commons.interaction.events.CrsChangedEvent;
import de.cismet.cismap.commons.interaction.events.MapClickedEvent;
import de.cismet.cismap.commons.interaction.events.MapDnDEvent;
import de.cismet.cismap.commons.interaction.events.MapSearchEvent;
import de.cismet.cismap.commons.interaction.events.StatusEvent;
import de.cismet.cismap.commons.security.AbstractCredentialsProvider;

import de.cismet.tools.CurrentStackTrace;
import de.cismet.tools.StaticDecimalTools;

import de.cismet.tools.gui.historybutton.HistoryModelListener;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class CismapBroker {

    //~ Static fields/initializers ---------------------------------------------

    private static final String FS = System.getProperty("file.separator");                       // NOI18N
    private static final String USER_HOME_DIRECTORY = System.getProperty("user.home");           // NOI18N
    private static final String SERVERALIAS_FILE_NAME = "serverAliases.properties";              // NOI18N
    private static final String DEFAULT_CISMAP_FOLDER = ".cismap";                               // NOI18N
    private static final String DEFAULT_ALIAS_FILE_PATH = "appLib" + FS + SERVERALIAS_FILE_NAME; // NOI18N

    //~ Instance fields --------------------------------------------------------

    PFeature oldPfeature = null;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private final Properties userProperties = new Properties();
    private Properties defaultProperties;
    private String cismapFolderPath = USER_HOME_DIRECTORY + FS + DEFAULT_CISMAP_FOLDER;
    private File defaultAliasFile;
    private File userAliasFile;
    private Vector<CapabilityListener> capabilityListeners = new Vector<>();
    private Vector<MappingModelListener> mappingModelListeners = new Vector<>();
    private Vector<StatusListener> statusListeners = new Vector<>();
    private Vector<HistoryModelListener> historyModelListeners = new Vector<>();
    private Vector<ActiveLayerListener> activeLayerListeners = new Vector<>();
    private Vector<MapClickListener> mapClickListeners = new Vector<>();
    private Vector<MapSearchListener> mapSearchListeners = new Vector<>();
    private Vector<MapDnDListener> mapDnDListeners = new Vector<>();
    private Vector<FeatureCollectionListener> featureCollectionListeners = new Vector<>();
    private Vector<MapBoundsListener> mapBoundsListeners = new Vector<>();
    private Vector<CrsChangeListener> crsChangeListeners = new Vector<>();
    // private Hashtable<WMSCapabilities, GUICredentialsProvider> httpCredentialsForCapabilities = new
    // Hashtable<WMSCapabilities, GUICredentialsProvider>();
    private Crs srs;
    private String preferredRasterFormat;
    private String preferredTransparentPref;
    private String preferredBGColor;
    private String preferredExceptionsFormat;
    private MappingComponent mappingComponent = null;
    private LayerWidget layerWidget = null;
    private ExecutorService execService = null;
    private boolean serverAliasesInited = false;
    private String defaultCrs = "EPSG:31466";
    private int DefaultCrsAlias = -1;
    private MetaSearchFacade metaSearch;
    private boolean useInternalDb = false;
    private boolean checkForOverlappingGeometriesAfterFeatureRotation = true;
    private String featureStylingComponentKey = BasicFeatureStyleDialogFactory.KEY;
    private PFeature snappingVetoFeature;
    private Float minOpacityToStayEnabled = null;
    private boolean multiFeaturePopupMenuEnabled = false;
    private float defaultTranslucency = 0.2f;
    private boolean highlightFeatureOnMouseOver = true;
    private boolean enableDummyLayerWhenAvailable = true;
    private boolean enableRasterGeoReferencingToolbar = true;
    private Color measurementFillingColor;
    private Color measurementLineColor;
    private Integer measurementLineWidth;
    private GPSDirectionOptions.GPSDirection gpsAngleDirection = GPSDirectionOptions.GPSDirection.AUTO;
    private boolean WMSLayerNamesWithPath = false;
    private Map<String, String> urlAliasMapping = new HashMap<>();
    private Map<String, String> variableMapping = new HashMap<>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CismapBroker object.
     */
    private CismapBroker() {
        execService = Executors.newCachedThreadPool();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the urlAliasMapping
     */
    public Map<String, String> getUrlAliasMapping() {
        return urlAliasMapping;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  urlAliasMapping  the urlAliasMapping to set
     */
    public void setUrlAliasMapping(final Map<String, String> urlAliasMapping) {
        this.urlAliasMapping = urlAliasMapping;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the urlAliasMapping
     */
    public Map<String, String> getVariableMapping() {
        return variableMapping;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  variableMapping  the urlAliasMapping to set
     */
    public void setVariableMapping(final Map<String, String> variableMapping) {
        this.variableMapping = variableMapping;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the WMSLayerNamesWithPath
     */
    public boolean isWMSLayerNamesWithPath() {
        return WMSLayerNamesWithPath;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  WMSLayerNamesWithPath  the WMSLayerNamesWithPath to set
     */
    public void setWMSLayerNamesWithPath(final boolean WMSLayerNamesWithPath) {
        this.WMSLayerNamesWithPath = WMSLayerNamesWithPath;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the gpsAngleDirection
     */
    public GPSDirectionOptions.GPSDirection getGpsAngleDirection() {
        return gpsAngleDirection;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  gpsAngleDirection  the gpsAngleDirection to set
     */
    public void setGpsAngleDirection(final GPSDirectionOptions.GPSDirection gpsAngleDirection) {
        this.gpsAngleDirection = gpsAngleDirection;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the enableRasterGeoReferencingToolbar
     */
    public boolean isEnableRasterGeoReferencingToolbar() {
        return enableRasterGeoReferencingToolbar;
    }

    /**
     * enables or disables the RasterGeoReferencingToolbarComponentProvider. The default value is true
     *
     * @param  enableRasterGeoReferencingToolbar  the enableRasterGeoReferencingToolbar to set
     */
    public void setEnableRasterGeoReferencingToolbar(final boolean enableRasterGeoReferencingToolbar) {
        this.enableRasterGeoReferencingToolbar = enableRasterGeoReferencingToolbar;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the enableDummyLayerWhenAvailable
     */
    public boolean isEnableDummyLayerWhenAvailable() {
        return enableDummyLayerWhenAvailable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  enableDummyLayerWhenAvailable  the enableDummyLayerWhenAvailable to set
     */
    public void setEnableDummyLayerWhenAvailable(final boolean enableDummyLayerWhenAvailable) {
        this.enableDummyLayerWhenAvailable = enableDummyLayerWhenAvailable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the defaultTranslucency
     */
    public float getDefaultTranslucency() {
        return defaultTranslucency;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  defaultTranslucency  the defaultTranslucency to set
     */
    public void setDefaultTranslucency(final float defaultTranslucency) {
        this.defaultTranslucency = defaultTranslucency;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static CismapBroker getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cl  DOCUMENT ME!
     */
    public void addCapabilityListener(final CapabilityListener cl) {
        if (!capabilityListeners.contains(cl)) {
            capabilityListeners.add(cl);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cl  DOCUMENT ME!
     */
    public void removeCapabilityListener(final CapabilityListener cl) {
        capabilityListeners.remove(cl);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ml  DOCUMENT ME!
     */
    public void addMappingModelListener(final MappingModelListener ml) {
        if (!mappingModelListeners.contains(ml)) {
            mappingModelListeners.add(ml);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ml  DOCUMENT ME!
     */
    public void removeMappingModelListener(final MappingModelListener ml) {
        mappingModelListeners.remove(ml);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sl  DOCUMENT ME!
     */
    public void addStatusListener(final StatusListener sl) {
        if (!statusListeners.contains(sl)) {
            statusListeners.add(sl);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sl  DOCUMENT ME!
     */
    public void removeStatusListener(final StatusListener sl) {
        statusListeners.remove(sl);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  hml  DOCUMENT ME!
     */
    public void addHistoryModelListener(final HistoryModelListener hml) {
        if (!historyModelListeners.contains(hml)) {
            historyModelListeners.add(hml);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  hml  DOCUMENT ME!
     */
    public void removeHistoryModelListener(final HistoryModelListener hml) {
        historyModelListeners.remove(hml);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  all  DOCUMENT ME!
     */
    public void addActiveLayerListener(final ActiveLayerListener all) {
        if (!activeLayerListeners.contains(all)) {
            activeLayerListeners.add(all);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  all  DOCUMENT ME!
     */
    public void removeActiveLayerListener(final ActiveLayerListener all) {
        activeLayerListeners.remove(all);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mcl  DOCUMENT ME!
     */
    public void addMapClickListener(final MapClickListener mcl) {
        if (!mapClickListeners.contains(mcl)) {
            mapClickListeners.add(mcl);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mcl  DOCUMENT ME!
     */
    public void removeMapClickListener(final MapClickListener mcl) {
        mapClickListeners.remove(mcl);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  msl  DOCUMENT ME!
     */
    public void addMapSearchListener(final MapSearchListener msl) {
        if (!mapSearchListeners.contains(msl)) {
            mapSearchListeners.add(msl);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  msl  DOCUMENT ME!
     */
    public void removeMapSearchListener(final MapSearchListener msl) {
        mapSearchListeners.remove(msl);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mdl  DOCUMENT ME!
     */
    public void addMapDnDListener(final MapDnDListener mdl) {
        if (!mapDnDListeners.contains(mdl)) {
            mapDnDListeners.add(mdl);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mdl  DOCUMENT ME!
     */
    public void removeMapDnDListener(final MapDnDListener mdl) {
        mapDnDListeners.remove(mdl);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fcl  DOCUMENT ME!
     */
    public void addFeatureCollectionListener(final FeatureCollectionListener fcl) {
        if (!featureCollectionListeners.contains(fcl)) {
            featureCollectionListeners.add(fcl);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fcl  DOCUMENT ME!
     */
    public void removeFeatureCollectionListener(final FeatureCollectionListener fcl) {
        featureCollectionListeners.remove(fcl);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mbl  DOCUMENT ME!
     */
    public void addMapBoundsListener(final MapBoundsListener mbl) {
        if (!mapBoundsListeners.contains(mbl)) {
            mapBoundsListeners.add(mbl);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mbl  DOCUMENT ME!
     */
    public void removeFMapBoundsListener(final MapBoundsListener mbl) {
        mapBoundsListeners.remove(mbl);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mbl  DOCUMENT ME!
     */
    public void addCrsChangeListener(final CrsChangeListener mbl) {
        if (!crsChangeListeners.contains(mbl)) {
            crsChangeListeners.add(mbl);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mbl  DOCUMENT ME!
     */
    public void removeCrsChangeListener(final CrsChangeListener mbl) {
        crsChangeListeners.remove(mbl);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ce  DOCUMENT ME!
     */
    public void fireCapabilityServerChanged(final CapabilityEvent ce) {
        for (final Iterator<CapabilityListener> it = capabilityListeners.iterator(); it.hasNext();) {
            final CapabilityListener listener = it.next();
            listener.serverChanged(ce);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ce  DOCUMENT ME!
     */
    public void fireCapabilityLayerChanged(final CapabilityEvent ce) {
        for (final Iterator<CapabilityListener> it = capabilityListeners.iterator(); it.hasNext();) {
            final CapabilityListener listener = it.next();
            listener.layerChanged(ce);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ale  DOCUMENT ME!
     */
    public void fireLayerAdded(final ActiveLayerEvent ale) {
        for (final Iterator<ActiveLayerListener> it = activeLayerListeners.iterator(); it.hasNext();) {
            it.next().layerAdded(ale);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ale  DOCUMENT ME!
     */
    public void fireLayerRemoved(final ActiveLayerEvent ale) {
        for (final ActiveLayerListener listener : new ArrayList<ActiveLayerListener>(activeLayerListeners)) {
            listener.layerRemoved(ale);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ale  DOCUMENT ME!
     */
    public void fireLayerPositionChanged(final ActiveLayerEvent ale) {
        for (final Iterator<ActiveLayerListener> it = activeLayerListeners.iterator(); it.hasNext();) {
            it.next().layerPositionChanged(ale);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ale  DOCUMENT ME!
     */
    public void fireLayerVisibilityChanged(final ActiveLayerEvent ale) {
        for (final Iterator<ActiveLayerListener> it = activeLayerListeners.iterator(); it.hasNext();) {
            it.next().layerVisibilityChanged(ale);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ale  DOCUMENT ME!
     */
    public void fireLayerAvailabilityChanged(final ActiveLayerEvent ale) {
        for (final Iterator<ActiveLayerListener> it = activeLayerListeners.iterator(); it.hasNext();) {
            it.next().layerAvailabilityChanged(ale);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ale  DOCUMENT ME!
     */
    public void fireLayerInformationStatusChanged(final ActiveLayerEvent ale) {
        for (final Iterator<ActiveLayerListener> it = activeLayerListeners.iterator(); it.hasNext();) {
            it.next().layerInformationStatusChanged(ale);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ale  DOCUMENT ME!
     */
    public void fireLayerSelectionChanged(final ActiveLayerEvent ale) {
        for (final Iterator<ActiveLayerListener> it = activeLayerListeners.iterator(); it.hasNext();) {
            it.next().layerSelectionChanged(ale);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  se  DOCUMENT ME!
     */
    public void fireStatusValueChanged(final StatusEvent se) {
        final List<StatusListener> listenerCopy = new ArrayList<>(statusListeners);

        for (final Iterator<StatusListener> it = listenerCopy.iterator(); it.hasNext();) {
            it.next().statusValueChanged(se);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mce  DOCUMENT ME!
     */
    public void fireClickOnMap(final MapClickedEvent mce) {
        for (final Iterator<MapClickListener> it = mapClickListeners.iterator(); it.hasNext();) {
            it.next().clickedOnMap(mce);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mse  DOCUMENT ME!
     */
    public void fireMapSearchInited(final MapSearchEvent mse) {
        for (final Iterator<MapSearchListener> it = mapSearchListeners.iterator(); it.hasNext();) {
            it.next().mapSearchStarted(mse);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mde  DOCUMENT ME!
     */
    public void fireDropOnMap(final MapDnDEvent mde) {
        for (final Iterator<MapDnDListener> it = mapDnDListeners.iterator(); it.hasNext();) {
            it.next().dropOnMap(mde);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mde  DOCUMENT ME!
     */
    public void fireDragOverMap(final MapDnDEvent mde) {
        for (final Iterator<MapDnDListener> it = mapDnDListeners.iterator(); it.hasNext();) {
            it.next().dragOverMap(mde);
        }
    }

    /**
     * public void fireFeatureCollectionChanged(MappingModelEvent mme) { for (Iterator<FeatureCollectionListener> it =
     * featureCollectionListeners.iterator(); it.hasNext();) { it.next().featureCollectionChanged(mme); } } public void
     * fireFeatureSelectionChanged(MappingModelEvent mme) { for (Iterator<FeatureCollectionListener> it =
     * featureCollectionListeners.iterator(); it.hasNext();) { it.next().selectionChanged(mme); } }.
     */
    public void fireMapBoundsChanged() {
        for (final Iterator<MapBoundsListener> it = mapBoundsListeners.iterator(); it.hasNext();) {
            it.next().shownMapBoundsChanged();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void fireCrsChanged(final CrsChangedEvent event) {
        for (final Iterator<CrsChangeListener> it = crsChangeListeners.iterator(); it.hasNext();) {
            it.next().crsChanged(event);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Vector<MapClickListener> getMapClickListeners() {
        return mapClickListeners;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Crs getSrs() {
        if (srs == null) {
            if ((mappingComponent == null) || mappingComponent.isLocked()) {
                // the getSrs() method is called before the mapping component has configured it.
                // So a dummy will be returned. This should not happen after the startup phase.
                final Crs crs = new Crs("dummy", "dummy", "EPSG:31466", true, false);
                return crs;
            } else {
                log.error("srs is not set. Use EPSG:31466 ", new CurrentStackTrace());
                final Crs crs = new Crs("EPSG:31466", "EPSG:31466", "EPSG:31466", true, false);
                return crs;
            }
        }
        return srs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  srs  DOCUMENT ME!
     */
    public void setSrs(final Crs srs) {
        if ((this.srs == null) || !this.srs.equals(srs)) {
            final StatusEvent event = new StatusEvent(StatusEvent.CRS, srs);
            final CrsChangedEvent ce = new CrsChangedEvent(this.srs, srs);
            this.srs = srs;
            fireCrsChanged(ce);
            fireStatusValueChanged(event);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPreferredRasterFormat() {
        return preferredRasterFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  preferredRasterFormat  DOCUMENT ME!
     */
    public void setPreferredRasterFormat(final String preferredRasterFormat) {
        this.preferredRasterFormat = preferredRasterFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPreferredTransparentPref() {
        return preferredTransparentPref;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  preferredTransparentPref  DOCUMENT ME!
     */
    public void setPreferredTransparentPref(final String preferredTransparentPref) {
        this.preferredTransparentPref = preferredTransparentPref;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPreferredBGColor() {
        return preferredBGColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  preferredBGColor  DOCUMENT ME!
     */
    public void setPreferredBGColor(final String preferredBGColor) {
        this.preferredBGColor = preferredBGColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPreferredExceptionsFormat() {
        return preferredExceptionsFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  preferredExceptionsFormat  DOCUMENT ME!
     */
    public void setPreferredExceptionsFormat(final String preferredExceptionsFormat) {
        this.preferredExceptionsFormat = preferredExceptionsFormat;
    }

    /**
     * public MappingComponent getMappingComponent() { return mappingComponent; }.
     *
     * @param  mappingComponent  DOCUMENT ME!
     */
    public void setMappingComponent(final MappingComponent mappingComponent) {
        this.mappingComponent = mappingComponent;
        PNotificationCenter.defaultCenter()
                .addListener(
                    this,
                    "coordinatesChanged", // NOI18N
                    SimpleMoveListener.COORDINATES_CHANGED,
                    mappingComponent.getInputListener(MappingComponent.MOTION));
        PNotificationCenter.defaultCenter()
                .addListener(
                    this,
                    "lengthChanged", // NOI18N
                    MeasurementListener.LENGTH_CHANGED,
                    mappingComponent.getInputListener(MappingComponent.MEASUREMENT));
        PNotificationCenter.defaultCenter()
                .addListener(
                    this,
                    "selectionChanged", // NOI18N
                    PSelectionEventHandler.SELECTION_CHANGED_NOTIFICATION,
                    mappingComponent.getInputListener(MappingComponent.SELECT));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MetaSearchFacade getMetaSearch() {
        return metaSearch;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  metaSearch  DOCUMENT ME!
     */
    public void setMetaSearch(final MetaSearchFacade metaSearch) {
        this.metaSearch = metaSearch;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  notification  DOCUMENT ME!
     */
    public void coordinatesChanged(final edu.umd.cs.piccolox.event.PNotification notification) {
        final Object o = notification.getObject();
        if (o instanceof SimpleMoveListener) {
            final double x = ((SimpleMoveListener)o).getXCoord();
            final double y = ((SimpleMoveListener)o).getYCoord();

            fireStatusValueChanged(new StatusEvent(
                    StatusEvent.COORDINATE_STRING,
                    new Coordinate(x, y)));
            final PFeature pf = ((SimpleMoveListener)o).getUnderlyingPFeature();
            if (pf != oldPfeature) {
                fireStatusValueChanged(new StatusEvent(StatusEvent.OBJECT_INFOS, pf));
                oldPfeature = pf;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  notification  DOCUMENT ME!
     */
    public void lengthChanged(final edu.umd.cs.piccolox.event.PNotification notification) {
        final Object o = notification.getObject();
        if (o instanceof MeasurementListener) {
            final double length = ((MeasurementListener)o).getMeasuredLength();
            fireStatusValueChanged(new StatusEvent(
                    StatusEvent.MEASUREMENT_INFOS,
                    StaticDecimalTools.round("0.00", length)
                            + " m")); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  notification  DOCUMENT ME!
     */
    public void selectionChanged(final edu.umd.cs.piccolox.event.PNotification notification) {
    }

    /**
     * public LayerWidget getLayerWidget() { return layerWidget; } public void setLayerWidget(LayerWidget layerWidget) {
     * this.layerWidget = layerWidget; } public BoundingBox getInitialBoundingBox() { return initialBoundingBox; }
     * public void setInitialBoundingBox(BoundingBox initialBoundingBox) { this.initialBoundingBox = initialBoundingBox;
     * }.
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponent getMappingComponent() {
        return mappingComponent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   url  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String aliasToUrl(final String url) {
        if (url == null) {
            return null;
        }

        for (final String alias : urlAliasMapping.keySet()) {
            if (url.equals(alias)) {
                return replaceVariableInAlias(urlAliasMapping.get(alias));
            }
        }

        return url;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   url  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String urlToAlias(final String url) {
        final String alias = toAliasIfExists(url);

        if (alias != null) {
            return alias;
        } else {
            return url;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   url  DOCUMENT ME!
     *
     * @return  null, if the given string does not contain an alias
     */
    private String toAliasIfExists(String url) {
        if (url == null) {
            return null;
        }

        for (final String alias : urlAliasMapping.keySet()) {
            String urlPart = urlAliasMapping.get(alias);

            if (urlPart.endsWith("&")) {
                urlPart = urlPart.substring(0, urlPart.length() - 1);
            }
            if (url.endsWith("&")) {
                url = url.substring(0, url.length() - 1);
            }

            if (url.equals(urlPart) || url.equals(replaceVariableInAlias(urlPart))) {
                return alias;
            } else {
                final String aliasUrl = replaceVariableInAlias(urlPart);

                if (url.contains("?") && (aliasUrl != null) && aliasUrl.contains("?")) {
                    if (url.substring(0, url.indexOf("?")).equalsIgnoreCase(
                                    aliasUrl.substring(0, aliasUrl.indexOf("?")))) {
                        final Map<String, String> urlMap = getParameterMap(url);
                        final Map<String, String> urlAliasMap = getParameterMap(aliasUrl);

                        if (urlMap.equals(urlAliasMap)) {
                            return alias;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   url  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Map<String, String> getParameterMap(final String url) {
        final Map<String, String> map = new HashMap<>();

        if ((url != null) && url.contains("?") && (url.length() > (url.indexOf("?") + 1))) {
            final String parameters = url.substring(url.indexOf("?") + 1);
            final StringTokenizer st = new StringTokenizer(parameters, "&");

            while (st.hasMoreTokens()) {
                final String param = st.nextToken();

                if (param.contains("=")) {
                    map.put(param.substring(0, param.indexOf("=")), param.substring(param.indexOf("=") + 1));
                } else {
                    map.put(param, "");
                }
            }
        }

        return map;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   url  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String replaceVariableInAlias(final String url) {
        String result = url;

        if (result == null) {
            return null;
        }

        for (final String alias : variableMapping.keySet()) {
            if (result.contains("${{" + alias + "}}")) {
                result = result.replace("${{" + alias + "}}", variableMapping.get(alias));
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   link  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isAlias(final String link) {
        return urlAliasMapping.containsKey(link);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   url  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isAliasDefinedForUrl(final String url) {
        final String alias = toAliasIfExists(url);

        if (alias != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void initAliases() {
        if (log.isDebugEnabled()) {
            log.debug("initializing server aliases property"); // NOI18N
        }
        try {
            userAliasFile = new File(getCismapFolderPath() + FS + SERVERALIAS_FILE_NAME);
            final File cismapFolder = new File(getCismapFolderPath());

            if (!cismapFolder.exists()) {
                cismapFolder.mkdir();
            }

            if (userAliasFile.exists()) {
                final FileInputStream in = new FileInputStream(userAliasFile);
                userProperties.load(in);
                in.close();
            } else {
                userAliasFile.createNewFile();
            }

            defaultAliasFile = new File(DEFAULT_ALIAS_FILE_PATH);
            defaultProperties = new Properties(userProperties);
            if (defaultAliasFile.exists()) {
                final FileInputStream in = new FileInputStream(defaultAliasFile);
                defaultProperties.load(in);
                in.close();
            }
        } catch (IOException ex) {
            log.error("Error during reading the server aliases from file", ex); // NOI18N
        }
        serverAliasesInited = true;
    }

    /**
     * DOCUMENT ME!
     */
    public void cleanUpSystemRegistry() {
        final Preferences appPrefs = Preferences.userNodeForPackage(AbstractCredentialsProvider.class);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Try to delete preferences of the password dialog"); // NOI18N
            }
            appPrefs.removeNode();
            if (log.isDebugEnabled()) {
                log.debug("deletion of the preferences successfully");         // NOI18N
            }
        } catch (BackingStoreException ex) {
            if (log.isDebugEnabled()) {
                log.debug("Error during the deletion of the preferences");     // NOI18N
            }
            ex.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void writePropertyFile() {
        if (log.isDebugEnabled()) {
            log.debug("writing server Aliases to File");                      // NOI18N
        }
        if (!serverAliasesInited) {
            initAliases();
        }
        try {
            if (userAliasFile.exists()) {
                final FileOutputStream out = new FileOutputStream(userAliasFile);
                userProperties.store(out, "Server Aliases URL <---> Alias");  // NOI18N
            }
        } catch (IOException ex) {
            log.error("Error during writing the server aliases to file", ex); // NOI18N
        }
        if (log.isDebugEnabled()) {
            log.debug("Server Aliases wrote to File");                        // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key    DOCUMENT ME!
     * @param  value  DOCUMENT ME!
     */
    public void addProperty(final String key, final String value) {
        if (!serverAliasesInited) {
            initAliases();
        }
        userProperties.setProperty(key, value);
        if (log.isDebugEnabled()) {
            log.debug("Server alias added  key: " + key + " value: " + value); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getProperty(final String key) {
        if (!serverAliasesInited) {
            initAliases();
        }
        return defaultProperties.getProperty(key);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  workerThread  DOCUMENT ME!
     */
    public void execute(final SwingWorker workerThread) {
        try {
            execService.submit(workerThread);
            if (log.isDebugEnabled()) {
                log.debug("SwingWorker submitted to Threadpool"); // NOI18N
            }
        } catch (Exception ex) {
            log.fatal("SwingWorker Error", ex);                   // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCismapFolderPath() {
        return cismapFolderPath;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cismapFolderPath  DOCUMENT ME!
     */
    public void setCismapFolderPath(final String cismapFolderPath) {
        try {
            final File cismapFolder = new File(cismapFolderPath);
            if (!cismapFolder.exists()) {
                cismapFolder.mkdir();
            }
        } catch (Exception e) {
            log.fatal("Error during the creation of " + cismapFolderPath, e); // NOI18N
        }
        this.cismapFolderPath = cismapFolderPath;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the defaultCrs
     */
    public String getDefaultCrs() {
        return defaultCrs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  defaultCrs  the defaultCrs to set
     */
    public void setDefaultCrs(final String defaultCrs) {
        this.defaultCrs = defaultCrs;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the DefaultCrsAlias
     */
    public int getDefaultCrsAlias() {
        return DefaultCrsAlias;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  DefaultCrsAlias  the DefaultCrsAlias to set
     */
    public void setDefaultCrsAlias(final int DefaultCrsAlias) {
        this.DefaultCrsAlias = DefaultCrsAlias;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   code  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Crs crsFromCode(final String code) {
        Crs result = null;

        if ((code == null) || code.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("The given code is null or empty. Can't find a Crs object without a code.");
            }

            return result;
        }

        if (mappingComponent == null) {
            if (log.isDebugEnabled()) {
                log.debug(
                    "CismapBroker didn't provide a mapping component. So it's impossible to retrieve the crs list.");
            }

            return result;
        }

        final List<Crs> crsList = mappingComponent.getCrsList();
        if ((crsList == null) || crsList.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug(
                    "The crs list of the mapping component is empty. So it's impossible to find a matching Crs object.");
            }

            return result;
        }

        final String matchCode;
        if (!code.toUpperCase().startsWith("EPSG:")) {
            matchCode = "EPSG:".concat(code);
        } else {
            matchCode = code;
        }

        for (final Crs crs : crsList) {
            if ((crs != null) && (crs.getCode() != null) && crs.getCode().equalsIgnoreCase(matchCode)) {
                result = crs;
            }
        }

        if (result == null) {
            if (log.isDebugEnabled()) {
                log.debug("Couldn't find a crs for code '" + code + "' in crs list '" + crsList + "'.");
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the useInternalDb
     */
    public boolean isUseInternalDb() {
        return useInternalDb;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  useInternalDb  the useInternalDb to set
     */
    public void setUseInternalDb(final boolean useInternalDb) {
        this.useInternalDb = useInternalDb;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isCheckForOverlappingGeometriesAfterFeatureRotation() {
        return checkForOverlappingGeometriesAfterFeatureRotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  checkForOverlappingGeometriesAfterFeatureRotation  DOCUMENT ME!
     */
    public void setCheckForOverlappingGeometriesAfterFeatureRotation(
            final boolean checkForOverlappingGeometriesAfterFeatureRotation) {
        this.checkForOverlappingGeometriesAfterFeatureRotation = checkForOverlappingGeometriesAfterFeatureRotation;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getFeatureStylingComponentKey() {
        return featureStylingComponentKey;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureStylingComponentKey  DOCUMENT ME!
     */
    public void setFeatureStylingComponentKey(final String featureStylingComponentKey) {
        this.featureStylingComponentKey = featureStylingComponentKey;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the snappingVetoFeature
     */
    public PFeature getSnappingVetoFeature() {
        return snappingVetoFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  snappingVetoFeature  the snappingVetoFeature to set
     */
    public void setSnappingVetoFeature(final PFeature snappingVetoFeature) {
        this.snappingVetoFeature = snappingVetoFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the minOpacityToStayEnabled
     */
    public Float getMinOpacityToStayEnabled() {
        return minOpacityToStayEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  minOpacityToStayEnabled  the minOpacityToStayEnabled to set
     */
    public void setMinOpacityToStayEnabled(final Float minOpacityToStayEnabled) {
        this.minOpacityToStayEnabled = minOpacityToStayEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isMultiFeaturePopupMenuEnabled() {
        return multiFeaturePopupMenuEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  multiFeaturePopupMenuEnabled  DOCUMENT ME!
     */
    public void setMultiFeaturePopupMenuEnabled(final boolean multiFeaturePopupMenuEnabled) {
        this.multiFeaturePopupMenuEnabled = multiFeaturePopupMenuEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the highlightFeatureOnMouseOver
     */
    public boolean isHighlightFeatureOnMouseOver() {
        return highlightFeatureOnMouseOver;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  highlightFeatureOnMouseOver  the highlightFeatureOnMouseOver to set
     */
    public void setHighlightFeatureOnMouseOver(final boolean highlightFeatureOnMouseOver) {
        this.highlightFeatureOnMouseOver = highlightFeatureOnMouseOver;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Color getMeasurementFillingColor() {
        return measurementFillingColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  measurementFillingColor  DOCUMENT ME!
     */
    public void setMeasurementFillingColor(final Color measurementFillingColor) {
        this.measurementFillingColor = measurementFillingColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Color getMeasurementLineColor() {
        return measurementLineColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  measurementLineColor  DOCUMENT ME!
     */
    public void setMeasurementLineColor(final Color measurementLineColor) {
        this.measurementLineColor = measurementLineColor;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Integer getMeasurementLineWidth() {
        return measurementLineWidth;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  measurementLineWidth  DOCUMENT ME!
     */
    public void setMeasurementLineWidth(final Integer measurementLineWidth) {
        this.measurementLineWidth = measurementLineWidth;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final CismapBroker INSTANCE = new CismapBroker();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }
}
