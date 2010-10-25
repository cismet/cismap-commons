/*
 * CismapBroker.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 20. Februar 2006, 10:33
 *
 */
package de.cismet.cismap.commons.interaction;

import de.cismet.cismap.commons.security.AbstractCredentialsProvider;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;
import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.MappingModelListener;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.LayerWidget;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MeasurementListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.interaction.events.CapabilityEvent;
import de.cismet.cismap.commons.interaction.events.CrsChangedEvent;
import de.cismet.cismap.commons.interaction.events.MapClickedEvent;
import de.cismet.cismap.commons.interaction.events.MapDnDEvent;
import de.cismet.cismap.commons.interaction.events.MapSearchEvent;
import de.cismet.cismap.commons.interaction.events.StatusEvent;
import de.cismet.tools.CurrentStackTrace;
import de.cismet.tools.StaticDecimalTools;
import de.cismet.tools.gui.historybutton.HistoryModelListener;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.SwingWorker;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class CismapBroker {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private static final String FS = System.getProperty("file.separator");//NOI18N
    private static final String USER_HOME_DIRECTORY = System.getProperty("user.home");//NOI18N
    private static final String SERVERALIAS_FILE_NAME = "serverAliases.properties";//NOI18N
    private static final String DEFAULT_CISMAP_FOLDER = ".cismap";//NOI18N
    private static final String DEFAULT_ALIAS_FILE_PATH = "appLib" + FS + SERVERALIAS_FILE_NAME;//NOI18N
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
    //private Hashtable<WMSCapabilities, GUICredentialsProvider> httpCredentialsForCapabilities = new Hashtable<WMSCapabilities, GUICredentialsProvider>();
    private Crs srs;
    private String preferredRasterFormat;
    private String preferredTransparentPref;
    private String preferredBGColor;
    private String preferredExceptionsFormat;
    private MappingComponent mappingComponent = null;
    private LayerWidget layerWidget = null;
    private BoundingBox initialBoundingBox;
    private static CismapBroker instance = null;
    PFeature oldPfeature = null;
    private ExecutorService execService = null;
    private boolean serverAliasesInited = false;

    private CismapBroker() {
        execService = Executors.newCachedThreadPool();
    }

    public static CismapBroker getInstance() {
        if (instance == null) {
            instance = new CismapBroker();
        }
        return instance;
    }

    public void addCapabilityListener(CapabilityListener cl) {
        capabilityListeners.add(cl);
    }

    public void removeCapabilityListener(CapabilityListener cl) {
        capabilityListeners.remove(cl);
    }

    public void addMappingModelListener(MappingModelListener ml) {
        mappingModelListeners.add(ml);
    }

    public void removeMappingModelListener(MappingModelListener ml) {
        mappingModelListeners.remove(ml);
    }

    public void addStatusListener(StatusListener sl) {
        statusListeners.add(sl);
    }

    public void removeStatusListener(StatusListener sl) {
        statusListeners.remove(sl);
    }

    public void addHistoryModelListener(HistoryModelListener hml) {
        historyModelListeners.add(hml);
    }

    public void removeHistoryModelListener(HistoryModelListener hml) {
        historyModelListeners.remove(hml);
    }

    public void addActiveLayerListener(ActiveLayerListener all) {
        activeLayerListeners.add(all);
    }

    public void removeActiveLayerListener(ActiveLayerListener all) {
        activeLayerListeners.remove(all);
    }

    public void addMapClickListener(MapClickListener mcl) {
        mapClickListeners.add(mcl);
    }

    public void removeMapClickListener(MapClickListener mcl) {
        mapClickListeners.remove(mcl);
    }

    public void addMapSearchListener(MapSearchListener msl) {
        mapSearchListeners.add(msl);
    }

    public void removeMapSearchListener(MapSearchListener msl) {
        mapSearchListeners.remove(msl);
    }

    public void addMapDnDListener(MapDnDListener mdl) {
        mapDnDListeners.add(mdl);
    }

    public void removeMapDnDListener(MapDnDListener mdl) {
        mapDnDListeners.remove(mdl);
    }

    public void addFeatureCollectionListener(FeatureCollectionListener fcl) {
        featureCollectionListeners.add(fcl);
    }

    public void removeFeatureCollectionListener(FeatureCollectionListener fcl) {
        featureCollectionListeners.remove(fcl);
    }

    public void addMapBoundsListener(MapBoundsListener mbl) {
        mapBoundsListeners.add(mbl);
    }

    public void removeFMapBoundsListener(MapBoundsListener mbl) {
        mapBoundsListeners.remove(mbl);
    }

    public void addCrsChangeListener(CrsChangeListener mbl) {
        crsChangeListeners.add(mbl);
    }

    public void removeCrsChangeListener(CrsChangeListener mbl) {
        crsChangeListeners.remove(mbl);
    }

    public void fireCapabilityServerChanged(CapabilityEvent ce) {
        for (Iterator<CapabilityListener> it = capabilityListeners.iterator(); it.hasNext();) {
            CapabilityListener listener = it.next();
            listener.serverChanged(ce);
        }
    }

    public void fireCapabilityLayerChanged(CapabilityEvent ce) {
        for (Iterator<CapabilityListener> it = capabilityListeners.iterator(); it.hasNext();) {
            CapabilityListener listener = it.next();
            listener.layerChanged(ce);
        }
    }

    public void fireLayerAdded(ActiveLayerEvent ale) {
        for (Iterator<ActiveLayerListener> it = activeLayerListeners.iterator(); it.hasNext();) {
            it.next().layerAdded(ale);
        }
    }

    public void fireLayerRemoved(ActiveLayerEvent ale) {
        for (Iterator<ActiveLayerListener> it = activeLayerListeners.iterator(); it.hasNext();) {
            it.next().layerRemoved(ale);
        }
    }

    public void fireLayerPositionChanged(ActiveLayerEvent ale) {
        for (Iterator<ActiveLayerListener> it = activeLayerListeners.iterator(); it.hasNext();) {
            it.next().layerPositionChanged(ale);
        }
    }

    public void fireLayerVisibilityChanged(ActiveLayerEvent ale) {
        for (Iterator<ActiveLayerListener> it = activeLayerListeners.iterator(); it.hasNext();) {
            it.next().layerVisibilityChanged(ale);
        }
    }

    public void fireLayerInformationStatusChanged(ActiveLayerEvent ale) {
        for (Iterator<ActiveLayerListener> it = activeLayerListeners.iterator(); it.hasNext();) {
            it.next().layerInformationStatusChanged(ale);
        }
    }

    public void fireLayerSelectionChanged(ActiveLayerEvent ale) {
        for (Iterator<ActiveLayerListener> it = activeLayerListeners.iterator(); it.hasNext();) {
            it.next().layerSelectionChanged(ale);
        }
    }

    public void fireStatusValueChanged(StatusEvent se) {
        for (Iterator<StatusListener> it = statusListeners.iterator(); it.hasNext();) {
            it.next().statusValueChanged(se);
        }
    }

    public void fireClickOnMap(MapClickedEvent mce) {
        for (Iterator<MapClickListener> it = mapClickListeners.iterator(); it.hasNext();) {
            it.next().clickedOnMap(mce);
        }
    }

    public void fireMapSearchInited(MapSearchEvent mse) {
        for (Iterator<MapSearchListener> it = mapSearchListeners.iterator(); it.hasNext();) {
            it.next().mapSearchStarted(mse);
        }
    }

    public void fireDropOnMap(MapDnDEvent mde) {
        for (Iterator<MapDnDListener> it = mapDnDListeners.iterator(); it.hasNext();) {
            it.next().dropOnMap(mde);
        }
    }

    public void fireDragOverMap(MapDnDEvent mde) {
        for (Iterator<MapDnDListener> it = mapDnDListeners.iterator(); it.hasNext();) {
            it.next().dragOverMap(mde);
        }
    }

//    public void fireFeatureCollectionChanged(MappingModelEvent mme) {
//        for (Iterator<FeatureCollectionListener> it = featureCollectionListeners.iterator(); it.hasNext();) {
//            it.next().featureCollectionChanged(mme);
//        }
//    }
//    public void fireFeatureSelectionChanged(MappingModelEvent mme) {
//        for (Iterator<FeatureCollectionListener> it = featureCollectionListeners.iterator(); it.hasNext();) {
//            it.next().selectionChanged(mme);
//        }
//    }
    public void fireMapBoundsChanged() {
        for (Iterator<MapBoundsListener> it = mapBoundsListeners.iterator(); it.hasNext();) {
            it.next().shownMapBoundsChanged();
        }
    }


    public void fireCrsChanged(CrsChangedEvent event) {
        for (Iterator<CrsChangeListener> it = crsChangeListeners.iterator(); it.hasNext();) {
            it.next().crsChanged(event);
        }
    }

    public Crs getSrs() {
        if (srs == null) {
            log.error("srs is not set. Use EPSG:31466 ", new CurrentStackTrace());
            Crs crs = new Crs("EPSG:31466", "EPSG:31466", "EPSG:31466", true, false);
            return crs;
        }
        return srs;
    }

    public void setSrs(Crs srs) {
        if (this.srs == null || !this.srs.equals( srs ) ) {
            StatusEvent event = new StatusEvent(StatusEvent.CRS, srs);
            CrsChangedEvent ce = new CrsChangedEvent(this.srs, srs);
            this.srs = srs;
            fireCrsChanged(ce);
            fireStatusValueChanged(event);
        }
    }

    public String getPreferredRasterFormat() {
        return preferredRasterFormat;
    }

    public void setPreferredRasterFormat(String preferredRasterFormat) {
        this.preferredRasterFormat = preferredRasterFormat;
    }

    public String getPreferredTransparentPref() {
        return preferredTransparentPref;
    }

    public void setPreferredTransparentPref(String preferredTransparentPref) {
        this.preferredTransparentPref = preferredTransparentPref;
    }

    public String getPreferredBGColor() {
        return preferredBGColor;
    }

    public void setPreferredBGColor(String preferredBGColor) {
        this.preferredBGColor = preferredBGColor;
    }

    public String getPreferredExceptionsFormat() {
        return preferredExceptionsFormat;
    }

    public void setPreferredExceptionsFormat(String preferredExceptionsFormat) {
        this.preferredExceptionsFormat = preferredExceptionsFormat;
    }

//    public MappingComponent getMappingComponent() {
//        return mappingComponent;
//    }
//
    public void setMappingComponent(MappingComponent mappingComponent) {
        this.mappingComponent = mappingComponent;
        PNotificationCenter.defaultCenter().addListener(this,
                "coordinatesChanged",                               //NOI18N
                SimpleMoveListener.COORDINATES_CHANGED,
                mappingComponent.getInputListener(MappingComponent.MOTION));
        PNotificationCenter.defaultCenter().addListener(this,
                "lengthChanged",                                    //NOI18N
                MeasurementListener.LENGTH_CHANGED,
                mappingComponent.getInputListener(MappingComponent.MEASUREMENT));
        PNotificationCenter.defaultCenter().addListener(this,
                "selectionChanged",                                 //NOI18N
                PSelectionEventHandler.SELECTION_CHANGED_NOTIFICATION,
                mappingComponent.getInputListener(MappingComponent.SELECT));
    }

    public void coordinatesChanged(edu.umd.cs.piccolox.event.PNotification notification) {
        Object o = notification.getObject();
        if (o instanceof SimpleMoveListener) {
            double x = ((SimpleMoveListener) o).getXCoord();
            double y = ((SimpleMoveListener) o).getYCoord();
            fireStatusValueChanged(new StatusEvent(StatusEvent.COORDINATE_STRING, MappingComponent.getCoordinateString(x, y)));
            PFeature pf = ((SimpleMoveListener) o).getUnderlyingPFeature();
            if (pf != oldPfeature) {
                fireStatusValueChanged(new StatusEvent(StatusEvent.OBJECT_INFOS, pf));
                oldPfeature = pf;
            }
        }
    }

    public void lengthChanged(edu.umd.cs.piccolox.event.PNotification notification) {
        Object o = notification.getObject();
        if (o instanceof MeasurementListener) {
            double length = ((MeasurementListener) o).getMeasuredLength();
            fireStatusValueChanged(new StatusEvent(StatusEvent.MEASUREMENT_INFOS, StaticDecimalTools.round("0.00", length) + " m"));//NOI18N
        }
    }

    public void selectionChanged(edu.umd.cs.piccolox.event.PNotification notification) {
    }

//    public LayerWidget getLayerWidget() {
//        return layerWidget;
//    }
//
//    public void setLayerWidget(LayerWidget layerWidget) {
//        this.layerWidget = layerWidget;
//    }
//
//    public BoundingBox getInitialBoundingBox() {
//        return initialBoundingBox;
//    }
//
//    public void setInitialBoundingBox(BoundingBox initialBoundingBox) {
//        this.initialBoundingBox = initialBoundingBox;
//    }
    public MappingComponent getMappingComponent() {
        return mappingComponent;
    }

    private void initAliases() {
        log.debug("initializing server aliases property");//NOI18N
        try {
            userAliasFile = new File(getCismapFolderPath() + FS + SERVERALIAS_FILE_NAME);
            File cismapFolder = new File(getCismapFolderPath());

            if (!cismapFolder.exists()) {
                cismapFolder.mkdir();
            }

            if (userAliasFile.exists()) {
                FileInputStream in = new FileInputStream(userAliasFile);
                userProperties.load(in);
                in.close();
            } else {
                userAliasFile.createNewFile();
            }

            defaultAliasFile = new File(DEFAULT_ALIAS_FILE_PATH);
            defaultProperties = new Properties(userProperties);
            if (defaultAliasFile.exists()) {
                FileInputStream in = new FileInputStream(defaultAliasFile);
                defaultProperties.load(in);
                in.close();
            }
        } catch (IOException ex) {
            log.error("Error during reading the server aliases from file", ex);//NOI18N
        }
        serverAliasesInited = true;
    }

    public void cleanUpSystemRegistry() {
        Preferences appPrefs = Preferences.userNodeForPackage(AbstractCredentialsProvider.class);
        try {
            log.debug("Try to delete preferences of the password dialog");//NOI18N
            appPrefs.removeNode();
            log.debug("deletion of the preferences successfully");//NOI18N
        } catch (BackingStoreException ex) {
            log.debug("Error during the deletion of the preferences");//NOI18N
            ex.printStackTrace();
        }
    }

    public void writePropertyFile() {
        log.debug("writing server Aliases to File");//NOI18N
        if (!serverAliasesInited) {
            initAliases();
        }
        try {
            if (userAliasFile.exists()) {
                FileOutputStream out = new FileOutputStream(userAliasFile);
                userProperties.store(out, "Server Aliases URL <---> Alias");//NOI18N
            }
        } catch (IOException ex) {
            log.error("Error during writing the server aliases to file", ex);//NOI18N
        }
        log.debug("Server Aliases wrote to File");//NOI18N
    }

    public void addProperty(String key, String value) {
        if (!serverAliasesInited) {
            initAliases();
        }
        userProperties.setProperty(key, value);
        log.debug("Server alias added  key: " + key + " value: " + value);//NOI18N
    }

    public String getProperty(String key) {
        if (!serverAliasesInited) {
            initAliases();
        }
        return defaultProperties.getProperty(key);
    }

    public void execute(SwingWorker workerThread) {
        try {
            execService.submit(workerThread);
            log.debug("SwingWorker submitted to Threadpool");//NOI18N
        } catch (Exception ex) {
            log.fatal("SwingWorker Error", ex);//NOI18N
        }
    }

    public String getCismapFolderPath() {
        return cismapFolderPath;
    }

    public void setCismapFolderPath(String cismapFolderPath) {
        try {
            File cismapFolder = new File(cismapFolderPath);
            if (!cismapFolder.exists()) {
                cismapFolder.mkdir();
            }
        } catch (Exception e) {
            log.fatal("Error during the creation of "+cismapFolderPath,e);//NOI18N
        }
        this.cismapFolderPath = cismapFolderPath;
    }
    
}
