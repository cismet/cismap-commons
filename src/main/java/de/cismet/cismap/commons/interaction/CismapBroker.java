/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.interaction;

import edu.umd.cs.piccolox.event.PNotificationCenter;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.SwingWorker;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.MappingModelListener;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.featureservice.style.BasicFeatureStyleDialogueFactory;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.LayerWidget;
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
    private static CismapBroker instance = null;

    //~ Instance fields --------------------------------------------------------

    PFeature oldPfeature = null;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Properties userProperties = new Properties();
    private Properties defaultProperties;
    private String cismapFolderPath = USER_HOME_DIRECTORY + FS + DEFAULT_CISMAP_FOLDER;
    private File defaultAliasFile;
    private File userAliasFile;
    private Vector<CapabilityListener> capabilityListeners = new Vector<CapabilityListener>();
    private Vector<MappingModelListener> mappingModelListeners = new Vector<MappingModelListener>();
    private Vector<StatusListener> statusListeners = new Vector<StatusListener>();
    private Vector<HistoryModelListener> historyModelListeners = new Vector<HistoryModelListener>();
    private Vector<ActiveLayerListener> activeLayerListeners = new Vector<ActiveLayerListener>();
    private Vector<MapClickListener> mapClickListeners = new Vector<MapClickListener>();
    private Vector<MapSearchListener> mapSearchListeners = new Vector<MapSearchListener>();
    private Vector<MapDnDListener> mapDnDListeners = new Vector<MapDnDListener>();
    private Vector<FeatureCollectionListener> featureCollectionListeners = new Vector<FeatureCollectionListener>();
    private Vector<MapBoundsListener> mapBoundsListeners = new Vector<MapBoundsListener>();
    private Vector<CrsChangeListener> crsChangeListeners = new Vector<CrsChangeListener>();
    // private Hashtable<WMSCapabilities, GUICredentialsProvider> httpCredentialsForCapabilities = new
    // Hashtable<WMSCapabilities, GUICredentialsProvider>();
    private Crs srs;
    private String preferredRasterFormat;
    private String preferredTransparentPref;
    private String preferredBGColor;
    private String preferredExceptionsFormat;
    private MappingComponent mappingComponent = null;
    private LayerWidget layerWidget = null;
    private BoundingBox initialBoundingBox;
    private ExecutorService execService = null;
    private boolean serverAliasesInited = false;
    private String defaultCrs = "EPSG:31466";
    private int DefaultCrsAlias = -1;
    private MetaSearchFacade metaSearch;
    private boolean useInternalDb = false;
    private boolean checkForOverlappingGeometriesAfterFeatureRotation = true;
    private String featureStylingComponentKey = BasicFeatureStyleDialogueFactory.KEY;

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
     * @return  DOCUMENT ME!
     */
    public static CismapBroker getInstance() {
        if (instance == null) {
            instance = new CismapBroker();
        }
        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cl  DOCUMENT ME!
     */
    public void addCapabilityListener(final CapabilityListener cl) {
        capabilityListeners.add(cl);
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
        mappingModelListeners.add(ml);
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
        statusListeners.add(sl);
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
        historyModelListeners.add(hml);
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
        activeLayerListeners.add(all);
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
        mapClickListeners.add(mcl);
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
        mapSearchListeners.add(msl);
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
        mapDnDListeners.add(mdl);
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
        featureCollectionListeners.add(fcl);
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
        mapBoundsListeners.add(mbl);
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
        crsChangeListeners.add(mbl);
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
        for (final Iterator<ActiveLayerListener> it = activeLayerListeners.iterator(); it.hasNext();) {
            it.next().layerRemoved(ale);
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
        for (final Iterator<StatusListener> it = statusListeners.iterator(); it.hasNext();) {
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
                    MappingComponent.getCoordinateString(x, y)));
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
}
