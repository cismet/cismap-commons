/*
 * MappingComponent.java
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
 * but WITHOUT ANY WARRANTY; without even the implied warranty ofjo
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
 * Created on 22. Juni 2005, 12:03
 *
 */
package de.cismet.cismap.commons.gui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.MappingModelListener;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.Bufferable;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.DefaultWFSFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureWithId;
import de.cismet.cismap.commons.features.RasterLayerSupportedFeature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.StyledFeature;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.PBoundsWithCleverToString;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.PFeatureCoordinatePosition;
import de.cismet.cismap.commons.gui.piccolo.PNodeFactory;
import de.cismet.cismap.commons.gui.piccolo.PSticky;
import de.cismet.cismap.commons.gui.piccolo.XPImage;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.AttachFeatureListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.BackgroundRefreshingPanEventListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.BoundingBoxSearchListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CustomFeatureActionListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CustomFeatureInfoListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.DeleteFeatureListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.FeatureMoveListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.GetFeatureInfoClickDetectionListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.JoinPolygonsListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.KeyboardListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MeasurementListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.OverviewModeListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.PrintingFrameListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.RaisePolygonListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.RubberBandZoomListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SplitPolygonListener;
import de.cismet.cismap.commons.gui.printing.PrintingSettingsWidget;
import de.cismet.cismap.commons.gui.printing.PrintingWidget;
import de.cismet.cismap.commons.gui.printing.Scale;
import de.cismet.cismap.commons.gui.simplelayerwidget.LayerControl;
import de.cismet.cismap.commons.gui.simplelayerwidget.NewSimpleInternalLayerWidget;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.MapDnDEvent;
import de.cismet.cismap.commons.interaction.events.StatusEvent;
import de.cismet.cismap.commons.interaction.memento.Memento;
import de.cismet.cismap.commons.interaction.memento.MementoInterface;
import de.cismet.cismap.commons.preferences.CismapPreferences;
import de.cismet.cismap.commons.preferences.GlobalPreferences;
import de.cismet.cismap.commons.preferences.LayersPreferences;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;
import de.cismet.cismap.commons.rasterservice.FeatureAwareRasterService;
import de.cismet.cismap.commons.rasterservice.RasterMapService;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.tools.CismetThreadPool;
import de.cismet.tools.CurrentStackTrace;
import de.cismet.tools.StaticDebuggingTools;
import de.cismet.tools.collections.MultiMap;
import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.gui.historybutton.DefaultHistoryModel;
import de.cismet.tools.gui.historybutton.HistoryModel;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.PRoot;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.jdom.DataConversionException;
import org.jdom.Element;
import pswing.PSwingCanvas;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class MappingComponent extends PSwingCanvas implements MappingModelListener, FeatureCollectionListener, HistoryModel, Configurable, DropTargetListener {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public static final String MOTION = "MOTION";
    public static final String SELECT = "SELECT";
    public static final String ZOOM = "ZOOM";
    public static final String PAN = "PAN";
    public static final String FEATURE_INFO = "FEATURE_INFO";
    public static final String BOUNDING_BOX_SEARCH = "BOUNDING_BOX_SEARCH";
    public static final String MOVE_POLYGON = "MOVE_POLYGON";
    public static final String REMOVE_POLYGON = "REMOVE_POLYGON";
    public static final String NEW_POLYGON = "NEW_POLYGON";
    public static final String SPLIT_POLYGON = "SPLIT_POLYGON";
    public static final String JOIN_POLYGONS = "JOIN_POLYGONS";
    public static final String RAISE_POLYGON = "RAISE_POLYGON";
    public static final String ROTATE_POLYGON = "ROTATE_POLYGON";
    public static final String ATTACH_POLYGON_TO_ALPHADATA = "ATTACH_POLYGON_TO_ALPHADATA";
    public static final String MOVE_HANDLE = "MOVE_HANDLE";
    public static final String REMOVE_HANDLE = "REMOVE_HANDLE";
    public static final String ADD_HANDLE = "ADD_HANDLE";
    public static final String MEASUREMENT = "MEASUREMENT";
    public static final String PRINTING_AREA_SELECTION = "PRINTING_AREA_SELECTION";
    public static final String CUSTOM_FEATUREACTION = "CUSTOM_FEATUREACTION";
    public static final String CUSTOM_FEATUREINFO = "CUSTOM_FEATUREINFO";
    public static final String OVERVIEW = "OVERVIEW";
    private boolean gridEnabled = true;
    private Feature[] currentlyShownFeatures = null;
    private com.vividsolutions.jts.geom.Envelope currentFeatureEnvelope = null;
    private MappingModel mappingModel;
    private ConcurrentHashMap pFeatureHM = new ConcurrentHashMap();
    private MultiMap pFeatureHMbyCoordinate = new MultiMap();
    //Attribute die zum selektieren von PNodes gebraucht werden
    //private PFeature selectedFeature=null;
    private Paint paint = null;
    private WorldToScreenTransform wtst = null;
    private double clip_offset_x;
    private double clip_offset_y;
    private double printingResolution = 0d;
    double viewScale;
    private PImage imageBackground = new XPImage();
    private SimpleWmsGetMapUrl wmsBackgroundUrl;
    private boolean backgroundEnabled = true;
    private ConcurrentHashMap<String, PLayer> featureLayers = new ConcurrentHashMap<String, PLayer>();
    private PLayer featureLayer = new PLayer();
    private PLayer tmpFeatureLayer = new PLayer();
    private PLayer mapServicelayer = new PLayer();
    private PLayer featureServiceLayer = new PLayer();
    private PLayer handleLayer = new PLayer();
    private PLayer snapHandleLayer = new PLayer();
    private PLayer rubberBandLayer = new PLayer();
    private PLayer highlightingLayer = new PLayer();
    private PLayer crosshairLayer = new PLayer();
    private PLayer stickyLayer = new PLayer();
    private PLayer printingFrameLayer = new PLayer();
    private PLayer dragPerformanceImproverLayer = new PLayer();
    private boolean readOnly = true;
    private boolean snappingEnabled = true;
    private boolean visualizeSnappingEnabled = true;
    private boolean visualizeSnappingRectEnabled = false;
    private int snappingRectSize = 20;
    private HashMap cursors = new HashMap();
    private HashMap<String, PBasicInputEventHandler> inputEventListener = new HashMap<String, PBasicInputEventHandler>();
    private Action backAction;
    private Action forwardAction;
    private Action homeAction;
    private Action refreshAction;
    private Action snappingAction;
    private Action backgroundAction;
    private Action zoomAction;
    private Action panAction;
    private Action selectAction;
    private int acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
    private DragSource dragSource;
    private DragGestureListener dgListener;
    private DragSourceListener dsListener;
    private FeatureCollection featureCollection;
    private boolean internalLayerWidgetAvailable = false;
    private boolean infoNodesVisible = false;
    private boolean fixedMapExtent = false;
    private boolean fixedMapScale = false;
    private boolean inGlueIdenticalPointsMode = true;
    /**
     * Holds value of property interactionMode.
     */
    private String interactionMode;
    /**
     * Holds value of property handleInteractionMode.
     */
    private String handleInteractionMode;
    // "Phantom PCanvas" der nie selbst dargestellt wird
    // wird nur dazu benutzt das Graphics Objekt up to date
    // zu halten und dann als Hintergrund von z.B. einem
    // Panel zu fungieren
    // coooooooool, was ? ;-)
    private PCanvas selectedObjectPresenter = new PCanvas();
    private BoundingBox currentBoundingBox = null;
    private HashMap rasterServiceImages = new HashMap();
    static private MappingComponent THIS;
    private Rectangle2D newViewBounds;
    private int animationDuration = 500;
    private int taskCounter = 0;
    private CismapPreferences cismapPrefs;
    private NewSimpleInternalLayerWidget internalLayerWidget = null;//new NewSimpleInternalLayerWidget(this);
    boolean featureServiceLayerVisible = true;
    Vector layerControls = new Vector();
    private DefaultHistoryModel historyModel = new DefaultHistoryModel();
    private HashSet<Feature> holdFeatures = new HashSet<Feature>();
    //Scales
    private Vector<Scale> scales = new Vector<Scale>();
    //Printing
    private PrintingSettingsWidget printingSettingsDialog;
    private PrintingWidget printingDialog;
    //Scalebar
    private double screenResolution = 100.0;
    private boolean locked = true;
    private Vector<PNode> stickyPNodes = new Vector<PNode>();
    //Undo- & Redo-Stacks
    private MementoInterface memUndo = new Memento();
    private MementoInterface memRedo = new Memento();
    private boolean featureDebugging = false;
    private BoundingBox fixedBoundingBox = null;
    Object handleFeatureServiceBlocker = new Object();
    private ArrayList<MapListener> mapListeners = new ArrayList();

    public void addMapListener(MapListener mapListener) {
        if (mapListener != null) {
            mapListeners.add(mapListener);
        }
    }

    public void removeMapListener(MapListener mapListener) {
        if (mapListener != null) {
            mapListeners.remove(mapListener);
        }
    }

    /**
     * Creates a new instance of MappingComponent
     */
    public MappingComponent() {
        super();
        locked = true;
        THIS = this;
        //wird in der Regel wieder ueberschrieben
        setSnappingRectSize(20);
        setSnappingEnabled(false);
        setVisualizeSnappingEnabled(false);
        setAnimationDuration(500);
        setInteractionMode(ZOOM);

        featureDebugging = StaticDebuggingTools.checkHomeForFile("cismetTurnOnFeatureDebugging");

        setFeatureCollection(new DefaultFeatureCollection());

        addMapListener((DefaultFeatureCollection) getFeatureCollection());
        DropTarget dt = new DropTarget(this, acceptableActions, this);

//        setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
//        setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        setDefaultRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
        setAnimatingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);

        removeInputEventListener(getPanEventHandler());
        removeInputEventListener(getZoomEventHandler());
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        PRoot root = getRoot();

        PCamera otherCamera = new PCamera();
        otherCamera.addLayer(featureLayer);
        selectedObjectPresenter.setCamera(otherCamera);

        root.addChild(otherCamera);

        getLayer().addChild(mapServicelayer);
        getLayer().addChild(featureServiceLayer);
        getLayer().addChild(featureLayer);
        getLayer().addChild(tmpFeatureLayer);
        //getLayer().addChild(handleLayer);
        getLayer().addChild(rubberBandLayer);
        getLayer().addChild(highlightingLayer);
        getLayer().addChild(crosshairLayer);
        getLayer().addChild(dragPerformanceImproverLayer);
        getLayer().addChild(printingFrameLayer);

        getCamera().addLayer(mapServicelayer);
//        getCamera().addLayer(1, featureServiceLayer);
        getCamera().addLayer(featureLayer);
        getCamera().addLayer(tmpFeatureLayer);
        //getCamera().addLayer(5,snapHandleLayer);
        //getCamera().addLayer(5,handleLayer);
        getCamera().addLayer(rubberBandLayer);
        getCamera().addLayer(highlightingLayer);
        getCamera().addLayer(crosshairLayer);
        getCamera().addLayer(dragPerformanceImproverLayer);
        getCamera().addLayer(printingFrameLayer);

        getCamera().addChild(snapHandleLayer);
        getCamera().addChild(handleLayer);
        getCamera().addChild(stickyLayer);
        handleLayer.moveToFront();

        otherCamera.setTransparency(0.05f);

        initInputListener();
        initCursors();

        addInputEventListener(getInputListener(MOTION));
        addInputEventListener(getInputListener(CUSTOM_FEATUREACTION));

        KeyboardListener k = new KeyboardListener(this);
        addInputEventListener(k);
        getRoot().getDefaultInputManager().setKeyboardFocus(k);
        setInteractionMode(ZOOM);
        setHandleInteractionMode(MOVE_HANDLE);

        dragPerformanceImproverLayer.setVisible(false);
        historyModel.setMaximumPossibilities(30);

        zoomAction = new AbstractAction() {

            {
                putValue(Action.NAME, "Zoom");
                putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/layers.png")));
                putValue(Action.SHORT_DESCRIPTION, "Zoom");
                putValue(Action.LONG_DESCRIPTION, "Wechsel des Modus auf Zoom");
                putValue(Action.MNEMONIC_KEY, new Integer('Z'));
                putValue(Action.ACTION_COMMAND_KEY, "zoom.action");
            }

            public void actionPerformed(ActionEvent event) {
                zoomAction.putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/server.png")));
                setInteractionMode(MappingComponent.ZOOM);
            }
        };

        this.getCamera().addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
//                log.debug("getCamera().getViewTransform():"+getCamera().getViewTransform());
//                log.debug("getCamera().getViewTransform().getScaleY():"+getCamera().getViewTransform().getScaleY());
//                double[] matrix=new double[9];
//                getCamera().getViewTransform().getMatrix(matrix);
//                boolean nan=false;
//                for (double d:matrix) {
//                    if (d==Double.NaN) {
//                        nan=true;
//                        break;
//                    }
//                }
//                if (nan) {
//                    log.warn("getCamera().getViewTransform() has at least one NaN");
//                    getCamera().getViewTransformReference().setToIdentity();
//
//                }
//                if (getCamera().getViewTransform().getScaleY()<=0) {
//                    log.warn("getCamera().getViewTransform().getScaleY()<=0");
//                }
                checkAndFixErroneousTransformation();
//                log.debug("Camera().PropertyChangeListener()");
                rescaleStickyNodes();
                CismapBroker.getInstance().fireStatusValueChanged(new StatusEvent(StatusEvent.SCALE, interactionMode));
            }
        });
    }

    /**
     * @return true, if debug-messages are logged.
     */
    public boolean isFeatureDebugging() {
        return featureDebugging;
    }

    /**
     * Creates printingDialog and printingSettingsDialog.
     */
    public void initPrintingDialogs() {
        printingSettingsDialog = new PrintingSettingsWidget(true, this);
        printingDialog = new PrintingWidget(true, this);
    }

    /**
     * Returns the momentary image of the PCamera of this MappingComponent.
     * @return  Image
     */
    public Image getImage() {
        // this.getCamera().print();
        return this.getCamera().toImage(this.getWidth(), this.getHeight(), Color.white);
    }

    /**
     * Creates an image with given width and height from all features in the given 
     * featurecollection. The image will be used for printing.
     * @param fc FeatureCollection
     * @param width desired width of the resulting image
     * @param height desired height of the resulting image
     * @return Image of the featurecollection
     */
    public Image getImageOfFeatures(Collection<Feature> fc, int width, int height) {
        try {
            log.debug("getImageOffFeatures" + width + "," + height);
            PrintingFrameListener pfl = ((PrintingFrameListener) getInputListener(PRINTING_AREA_SELECTION));
            final PCanvas pc = new PCanvas();
            //c.addLayer(featureLayer);
            pc.setSize(width, height);
            Vector<PFeature> v = new Vector<PFeature>();
            Iterator it = fc.iterator();
            while (it.hasNext()) {
                Feature f = (Feature) it.next();
                final PFeature p = new PFeature(f, wtst, clip_offset_x, clip_offset_y, MappingComponent.this);
                if (p.getFullBounds().intersects(pfl.getPrintingRectangle().getBounds())) {
                    v.add(p);
                }
            }
            pc.getCamera().animateViewToCenterBounds(pfl.getPrintingRectangle().getBounds(), true, 0);
            final double scale = 1 / pc.getCamera().getViewScale();
            log.debug("subPCscale:" + scale);

            //TODO Sorge dafür dass die PSwingKomponente richtig gedruckt wird und dass die Karte nicht mehr "zittert"

            for (final PNode p : v) {
                if (p instanceof PFeature) {
                    PFeature original = ((PFeature) p);
                    original.setInfoNodeExpanded(false);

                    if (original.getFeature() instanceof DefaultWFSFeature) {
                        ((DefaultWFSFeature) original.getFeature()).setLineWidth((int) Math.round(((DefaultWFSFeature) original.getFeature()).getLineWidth() * (getPrintingResolution() * 2)));
                    }
                    PFeature copy = new PFeature(original.getFeature(), getWtst(), 0, 0, MappingComponent.this, true);
                    pc.getLayer().addChild(copy);

                    copy.setTransparency(original.getTransparency());
                    copy.setStrokePaint(original.getStrokePaint());
                    boolean expanded = original.isInfoNodeExpanded();
                    copy.addInfoNode();
                    copy.setInfoNodeExpanded(false);
//                    original.setInfoNodeExpanded(true);
                    copy.refreshInfoNode();

//                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection(original.toImage()), null);
                    original.refreshInfoNode();

                    removeStickyNode(copy.getStickyChild());

                    //Wenn mal irgendwas wegen Querformat kommt :
                    //pf.getStickyChild().setRotation(0.5);
                    PNode stickyChild = copy.getStickyChild();
                    if (stickyChild != null) {
                        stickyChild.setScale(scale * getPrintingResolution());
                        if (copy.hasSecondStickyChild()) {
                            copy.getSecondStickyChild().setScale(scale * getPrintingResolution());
                        }
                    }
                }
            }
            Image ret = pc.getCamera().toImage(width, height, new Color(255, 255, 255, 0));
            log.debug(ret);
            return ret;
        } catch (Exception exception) {
            log.error("Fehler beim Erzeugen eines bildes aus Features", exception);
            return null;
        }
    }

    /**
     * Creates an image with given width and height from all features that intersects
     * the printingframe.
     * @param width desired width of the resulting image
     * @param height desired height of the resulting image
     * @return Image of intersecting features
     */
    public Image getFeatureImage(int width, int height) {
        log.debug("getFeatureImage" + width + "," + height);
        PrintingFrameListener pfl = ((PrintingFrameListener) getInputListener(PRINTING_AREA_SELECTION));
        log.debug("printing rectangle bounds: " + pfl.getPrintingRectangle().getBounds());
        final PCanvas pc = new PCanvas();
        //c.addLayer(featureLayer);
        pc.setSize(width, height);
        Vector<PNode> v = new Vector<PNode>();
        Iterator it = featureLayer.getChildrenIterator();
        while (it.hasNext()) {
            PNode p = (PNode) it.next();
            if (p.getFullBounds().intersects(pfl.getPrintingRectangle().getBounds())) {
                v.add(p);
            }
        }
        log.debug("intersecting feature count: " + v.size());
        pc.getCamera().animateViewToCenterBounds(pfl.getPrintingRectangle().getBounds(), true, 0);
        final double scale = 1 / pc.getCamera().getViewScale();
        log.debug("subPCscale:" + scale);

        //TODO Sorge dafür dass die PSwingKomponente richtig gedruckt wird und dass die Karte nicht mehr "zittert"

        for (final PNode p : v) {

            if (p instanceof PFeature) {
                final PFeature original = ((PFeature) p);
                try {
                    EventQueue.invokeAndWait(new Runnable() {

                        public void run() {

                            try {
                                original.setInfoNodeExpanded(false);

                                final PFeature copy = new PFeature(original.getFeature(), getWtst(), 0, 0, MappingComponent.this, true);
                                pc.getLayer().addChild(copy);

                                copy.setTransparency(original.getTransparency());
                                copy.setStrokePaint(original.getStrokePaint());

                                copy.addInfoNode();
                                copy.setInfoNodeExpanded(false);

                                //original.refreshInfoNode();

                                //Wenn mal irgendwas wegen Querformat kommt :
                                //pf.getStickyChild().setRotation(0.5);
                                if (copy.getStickyChild() != null) {
                                    copy.getStickyChild().setScale(scale * getPrintingResolution());
                                }
                            } catch (Throwable t) {
                                log.error("Fehler beim erstellen des Featureabbildes", t);
                            }
                        }
                    });
                } catch (Throwable t) {
                    log.fatal("Fehler beim erstellen des Featureabbildes", t);
                    return null;
                }
                // Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection(original.toImage()), null);

//                log.debug("StcikyChild:"+pf.getStickyChild().);
            }
        }
        return pc.getCamera().toImage(width, height, new Color(255, 255, 255, 0));
    }

    /**
     * Adds the given PCamera to the PRoot of this MappingComponent.
     * @param cam PCamera-object
     */
    public void addToPRoot(PCamera cam) {
        getRoot().addChild(cam);
    }

    /**
     * Adds a PNode to the StickyNode-vector.
     * @param pn PNode-object
     */
    public void addStickyNode(PNode pn) {
        // log.debug("addStickyNode:" + pn);
        stickyPNodes.add(pn);
    }

    /**
     * Removes a specific PNode from the StickyNode-vector.
     * @param pn PNode that should be removed
     */
    public void removeStickyNode(PNode pn) {
        stickyPNodes.remove(pn);
    }

    /**
     * @return Vector<PNode> with all sticky PNodes
     */
    public Vector<PNode> getStickyNodes() {
        return stickyPNodes;
    }

    /**
     * Calls private method rescaleStickyNodeWork(node) to rescale the sticky PNode. Forces the
     * execution to the EDT.
     * @param n PNode to rescale
     */
    public void rescaleStickyNode(final PNode n) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    rescaleStickyNodeWork(n);
                }
            });
        } else {
            rescaleStickyNodeWork(n);
        }
    }

    private double getPrintingResolution() {
        return this.printingResolution;
    }

    public void setPrintingResolution(double printingResolution) {
        this.printingResolution = printingResolution;
    }

    /**
     * Sets the scale of the given PNode to the value of the camera scale.
     * @param n PNode to rescale
     */
    private void rescaleStickyNodeWork(PNode n) {
        double s = MappingComponent.this.getCamera().getViewScale();
        n.setScale(1 / s);
    }

    /**
     * Rescales all nodes inside the StickyNode-vector.
     */
    public void rescaleStickyNodes() {
        Vector<PNode> stickyNodeCopy = new Vector<PNode>(getStickyNodes());

        for (Iterator<PNode> i = stickyNodeCopy.iterator(); i.hasNext();) {
            final PNode each = i.next();
            if (each instanceof PSticky && each.getVisible()) {
                rescaleStickyNode(each);
            } else {
                if (each instanceof PSticky && each.getParent() == null) {
                    removeStickyNode(each);
                }
            }
        }
    }

    /**
     * Returns the custom created Action zoomAction.
     * @return Action-object
     */
    public Action getZoomAction() {
        return zoomAction;
    }

    /**
     * Pans to the given bounds without creating a historyaction to undo the action.
     * @param bounds new bounds of the camera
     */
    public void gotoBoundsWithoutHistory(PBounds bounds) {
        log.debug("gotoBoundsWithoutHistory(PBounds: " + bounds, new CurrentStackTrace());
        try {
            handleLayer.removeAllChildren();
            if (bounds.getWidth() < 0) {
                bounds.setSize(bounds.getWidth() * (-1), bounds.getHeight());
            }
            if (bounds.getHeight() < 0) {
                bounds.setSize(bounds.getWidth(), bounds.getHeight() * (-1));
            }
            log.debug("vor animateView");
            getCamera().animateViewToCenterBounds(((PBounds) bounds), true, animationDuration);
            log.debug("nach animateView");

            queryServicesWithoutHistory();

            showHandles(true);
        } catch (NullPointerException npe) {
            log.warn("NPE in gotoBoundsWithoutHistory(" + bounds + ")", npe);
        }
    }

    /**
     * Checks out the y-camerascales for negative value and fixes it by negating
     * both x- and y-scales.
     */
    private void checkAndFixErroneousTransformation() {
        if (getCamera().getViewTransform().getScaleY() < 0) {
            double y = getCamera().getViewTransform().getScaleY();
            double x = getCamera().getViewTransform().getScaleX();
            log.warn("Erroneous ViewTransform: getViewTransform (scaleY=" + y + " scaleX=" + x + "). Try to fix it.");
            getCamera().getViewTransformReference().setToScale(getCamera().getViewTransform().getScaleX() * (-1), y * (-1));
        }
    }

    /**
     * Re-adds the default layers in a given order.
     */
    private void adjustLayers() {
        //getCamera().removeAllChildren();
        int counter = 0;
        getCamera().addLayer(counter++, mapServicelayer);
        for (int i = 0; i < featureServiceLayer.getChildrenCount(); ++i) {
            getCamera().addLayer(counter++, (PLayer) featureServiceLayer.getChild(i));
        }
        getCamera().addLayer(counter++, featureLayer);
        getCamera().addLayer(counter++, tmpFeatureLayer);
        //getCamera().addLayer(counter++,snapHandleLayer);
        //getCamera().addLayer(counter++,handleLayer);
        getCamera().addLayer(counter++, rubberBandLayer);
        getCamera().addLayer(counter++, dragPerformanceImproverLayer);
        getCamera().addLayer(counter++, printingFrameLayer);
    }

    /**
     * Assigns the listeners to the according interactionmodes.
     */
    public void initInputListener() {
        inputEventListener.put(MOTION, new SimpleMoveListener(this));
        inputEventListener.put(CUSTOM_FEATUREACTION, new CustomFeatureActionListener(this));
        inputEventListener.put(ZOOM, new RubberBandZoomListener());
        inputEventListener.put(PAN, new BackgroundRefreshingPanEventListener());
        inputEventListener.put(SELECT, new SelectionListener());

        inputEventListener.put(FEATURE_INFO, new GetFeatureInfoClickDetectionListener());
        inputEventListener.put(BOUNDING_BOX_SEARCH, new BoundingBoxSearchListener());

        inputEventListener.put(MOVE_POLYGON, new FeatureMoveListener(this));
        inputEventListener.put(NEW_POLYGON, new CreateGeometryListener(this));
        inputEventListener.put(RAISE_POLYGON, new RaisePolygonListener(this));
        inputEventListener.put(REMOVE_POLYGON, new DeleteFeatureListener());
        inputEventListener.put(ATTACH_POLYGON_TO_ALPHADATA, new AttachFeatureListener());
        inputEventListener.put(JOIN_POLYGONS, new JoinPolygonsListener());
        inputEventListener.put(SPLIT_POLYGON, new SplitPolygonListener(this));
        inputEventListener.put(MEASUREMENT, new MeasurementListener(this));
        inputEventListener.put(PRINTING_AREA_SELECTION, new PrintingFrameListener(this));
        inputEventListener.put(CUSTOM_FEATUREINFO, new CustomFeatureInfoListener());
        inputEventListener.put(OVERVIEW, new OverviewModeListener());
    }

    /**
     * Assigns a custom interactionmode with an own PBasicInputEventHandler.
     * @param key interactionmode as String
     * @param listener new PBasicInputEventHandler
     */
    public void addCustomInputListener(String key, PBasicInputEventHandler listener) {
        inputEventListener.put(key, listener);
    }

    /**
     * Assigns the cursors to the according interactionmodes.
     */
    public void initCursors() {
        putCursor(SELECT, new Cursor(Cursor.DEFAULT_CURSOR));
        putCursor(ZOOM, new Cursor(Cursor.CROSSHAIR_CURSOR));
        putCursor(PAN, new Cursor(Cursor.HAND_CURSOR));
        putCursor(FEATURE_INFO, new Cursor(Cursor.DEFAULT_CURSOR));
        putCursor(BOUNDING_BOX_SEARCH, new Cursor(Cursor.CROSSHAIR_CURSOR));

        putCursor(MOVE_POLYGON, new Cursor(Cursor.HAND_CURSOR));
        putCursor(ROTATE_POLYGON, new Cursor(Cursor.DEFAULT_CURSOR));
        putCursor(NEW_POLYGON, new Cursor(Cursor.CROSSHAIR_CURSOR));
        putCursor(RAISE_POLYGON, new Cursor(Cursor.DEFAULT_CURSOR));
        putCursor(REMOVE_POLYGON, new Cursor(Cursor.DEFAULT_CURSOR));
        putCursor(ATTACH_POLYGON_TO_ALPHADATA, new Cursor(Cursor.DEFAULT_CURSOR));
        putCursor(JOIN_POLYGONS, new Cursor(Cursor.DEFAULT_CURSOR));
        putCursor(SPLIT_POLYGON, new Cursor(Cursor.CROSSHAIR_CURSOR));
        putCursor(MEASUREMENT, new Cursor(Cursor.CROSSHAIR_CURSOR));

        putCursor(MOVE_HANDLE, new Cursor(Cursor.CROSSHAIR_CURSOR));
        putCursor(REMOVE_HANDLE, new Cursor(Cursor.CROSSHAIR_CURSOR));
        putCursor(ADD_HANDLE, new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * Shows the printingsetting-dialog that resets the interactionmode after printing.
     * @param oldInteractionMode String-object
     */
    public void showPrintingSettingsDialog(String oldInteractionMode) {
        if (!(printingSettingsDialog.getParent() instanceof JFrame)) {
            printingSettingsDialog = printingSettingsDialog.cloneWithNewParent(true, this);
        }
        printingSettingsDialog.setInteractionModeAfterPrinting(oldInteractionMode);
        printingSettingsDialog.setLocationRelativeTo(this);
        printingSettingsDialog.setVisible(true);
    }

    /**
     * Shows the printing-dialog that resets the interactionmode after printing.
     * @param oldInteractionMode String-object
     */
    public void showPrintingDialog(String oldInteractionMode) {
        setPointerAnnotationVisibility(false);
        if (!(printingDialog.getParent() instanceof JFrame)) {
            printingDialog = printingDialog.cloneWithNewParent(true, this);
        }
        try {
            printingDialog.setInteractionModeAfterPrinting(oldInteractionMode);
            printingDialog.startLoading();
            printingDialog.setLocationRelativeTo(this);
            printingDialog.setVisible(true);
        } catch (Exception e) {
            log.error("Fehler beim Anzeigen des Printing Dialogs", e);
        }
    }

    /**
     * Getter for property interactionMode.
     * @return Value of property interactionMode.
     */
    public String getInteractionMode() {
        return this.interactionMode;
    }

    /**
     * Changes the interactionmode.
     * @param interactionMode new interactionmode as String
     */
    public void setInteractionMode(String interactionMode) {
        try {
            log.debug("setInteractionMode(" + interactionMode + ")\nAlter InteractionMode:" + this.interactionMode + "");

            
            try {
                handleLayer.removeAllChildren();
            }
            catch (Exception e){
                log.warn("Fehler bei removeAllCHildren",e);

            }
            
            setPointerAnnotationVisibility(false);
            if (getPrintingFrameLayer().getChildrenCount() > 1) {
                getPrintingFrameLayer().removeAllChildren();
            }
            if (this.interactionMode != null) {
                if (interactionMode.equals(FEATURE_INFO)) {
                    ((GetFeatureInfoClickDetectionListener) this.getInputListener(interactionMode)).getPInfo().setVisible(true);
                } else {
                    ((GetFeatureInfoClickDetectionListener) this.getInputListener(FEATURE_INFO)).getPInfo().setVisible(false);
                }

                if (isReadOnly()) {
                    ((DefaultFeatureCollection) (getFeatureCollection())).removeFeaturesByInstance(PureNewFeature.class);
                }

                PInputEventListener pivl = this.getInputListener(this.interactionMode);
                if (pivl != null) {
                    removeInputEventListener(pivl);
                } else {
                    log.warn("this.getInputListener(this.interactionMode)==null");
                }
                if (interactionMode.equals(NEW_POLYGON)) {//||interactionMode==SELECT) {
//                if (selectedFeature!=null) {
//                    selectPFeatureManually(null);
//                }

                    featureCollection.unselectAll();
                }
                if ((interactionMode.equals(SELECT) || interactionMode.equals(SPLIT_POLYGON)) && this.readOnly == false) {
//                if (selectedFeature!=null) {
//                    selectPFeatureManually(selectedFeature);
//                }
                    featureSelectionChanged(null);
                }
                if (interactionMode.equals(JOIN_POLYGONS)) {
                    handleLayer.removeAllChildren();
                }
            }

            this.interactionMode = interactionMode;
            PInputEventListener pivl = this.getInputListener(interactionMode);
            if (pivl != null) {
                addInputEventListener(pivl);
                CismapBroker.getInstance().fireStatusValueChanged(new StatusEvent(StatusEvent.MAPPING_MODE, interactionMode));
            } else {
                log.warn("this.getInputListener(this.interactionMode)==null bei interactionMode=" + interactionMode);
            }
        } catch (Exception e) {
            log.error("Fehler beim Ändern des InteractionModes", e);
        }
    }

    /**
     * Resizes the component.
     * @param evt resizeevent as ComponentEvent
     */
    public void formComponentResized(ComponentEvent evt) {
        if (!this.isLocked()) {
            try {
                log.debug("formComponentResized " + MappingComponent.this.getSize());
                if (MappingComponent.this.getSize().height >= 0 && MappingComponent.this.getSize().width >= 0) {
                    if (mappingModel != null) {
                        log.debug("BB:" + MappingComponent.this.currentBoundingBox);
                        if (MappingComponent.this.currentBoundingBox == null) {
                            log.error("currentBoundingBox is null");
                            currentBoundingBox = getCurrentBoundingBox();
                        }
                        gotoBoundsWithoutHistory((PBounds) historyModel.getCurrentElement());
//                        }
//                        if (getCurrentElement()!=null) {
//                            gotoBoundsWithoutHistory((PBounds)(getCurrentElement()));
//                        } else {
//                            log.debug("getCurrentElement()==null) ");
//                        }
                        if (internalLayerWidget != null && internalLayerWidget.isVisible()) {
                            internalLayerWidget.setVisible(false);
                        }
                    }
                }
            } catch (Throwable t) {
                log.error("Fehler in formComponentResized()", t);
            }
        }
    }

    /**
     * syncSelectedObjectPresenter(int i)
     * @param i
     */
    public void syncSelectedObjectPresenter(int i) {
        selectedObjectPresenter.setVisible(true);
        if (featureCollection.getSelectedFeatures().size() > 0) {
            if (featureCollection.getSelectedFeatures().size() == 1) {
                PFeature selectedFeature = (PFeature) pFeatureHM.get(featureCollection.getSelectedFeatures().toArray()[0]);
                if (selectedFeature != null) {
                    selectedObjectPresenter.getCamera().animateViewToCenterBounds(selectedFeature.getBounds(), true, getAnimationDuration() * 2);
                }
            } else {
                //todo
            }
        } else {
            log.warn("in syncSelectedObjectPresenter(" + i + "): selectedFeature==null");
        }
    }

//    public void selectPFeatureManually(PFeature feature) {
//        if (feature==null) {
//            handleLayer.removeAllChildren();
//            if (selectedFeature!=null) {
//                selectedFeature.setSelected(false);
//            }
//        } else {
//            if (selectedFeature!=null) {
//                selectedFeature.setSelected(false);
//            }
//            feature.setSelected(true);
//            selectedFeature=feature;
//
//
//            //Fuer den selectedObjectPresenter (Eigener PCanvas)
//            syncSelectedObjectPresenter(1000);
//
//            handleLayer.removeAllChildren();
//            if (  this.isReadOnly()==false
//                    &&(
//                    getInteractionMode().equals(SELECT)
//                    ||
//                    getInteractionMode().equals(PAN)
//                    ||
//                    getInteractionMode().equals(ZOOM)
//                    ||
//                    getInteractionMode().equals(SPLIT_POLYGON)
//                    )
//                    ) {
//                selectedFeature.addHandles(handleLayer);
//            } else {
//                handleLayer.removeAllChildren();
//            }
//        }
//
//    }
//    public void selectionChanged(de.cismet.cismap.commons.MappingModelEvent mme) {
//        Feature f=mme.getFeature();
//        if (f==null) {
//            selectPFeatureManually(null);
//        } else {
//            PFeature fp=((PFeature)pFeatureHM.get(f));
//            if (fp!=null&&fp.getFeature()!=null&&fp.getFeature().getGeometry()!=null) {
////                PNode p=fp.getChild(0);
//                selectPFeatureManually(fp);
//            } else {
//                selectPFeatureManually(null);
//            }
//        }
//    }
    /**
     * Returns the current featureCollection.
     */
    public FeatureCollection getFeatureCollection() {
        return featureCollection;
    }

    /**
     * Replaces the old featureCollection with a new one.
     * @param featureCollection the new featureCollection
     */
    public void setFeatureCollection(FeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
        featureCollection.addFeatureCollectionListener(this);
    }

    public void setFeatureCollectionVisibility(boolean visibility) {
        featureLayer.setVisible(visibility);
    }

    public boolean isFeatureCollectionVisible() {
        return featureLayer.getVisible();
    }

    /**
     * Adds a new mapservice at a specific place of the layercontrols.
     * @param mapService the new mapservice
     * @param position the index where to position the mapservice
     */
    public void addMapService(MapService mapService, int position) {
        try {
            PNode p = new PNode();
            if (mapService instanceof RasterMapService) {
                p = new XPImage();
                mapService.addRetrievalListener(new MappingComponentRasterServiceListener(position, p, (ServiceLayer) mapService));
                mapService.setPNode(p);
            } else {
                p = new PLayer();
                mapService.setPNode(p);
                mapService.addRetrievalListener(new MappingComponentFeatureServiceListener((ServiceLayer) mapService, (PLayer) mapService.getPNode()));
            }

            p.setTransparency(mapService.getTranslucency());
            p.setVisible(mapService.isVisible());
            mapServicelayer.addChild(p);
//        if (internalLayerWidgetAvailable) {
////          LayerControl lc=internalLayerWidget.addRasterService(rs.size()-rsi,(ServiceLayer)o,cismapPrefs.getGlobalPrefs().getErrorAbolitionTime());
            // LayerControl lc = internalLayerWidget.addRasterService(position, (ServiceLayer) mapService, 500);
//            mapService.addRetrievalListener(lc);
//            lc.setTransparentable(p);
//            layerControls.add(lc);
//        }
        } catch (Throwable t) {
            log.warn("Fehler beim hinzufuegen eines Layers", t);
        }
    }

    public void preparationSetMappingModel(MappingModel mm) {
        mappingModel = mm;
    }

    /**
     * Sets a new mappingmodel in this MappingComponent.
     * @param mm the new mappingmodel
     */
    public void setMappingModel(MappingModel mm) {
        log.info("setMappingModel");
        mappingModel = mm;
        currentBoundingBox = mm.getInitialBoundingBox();
        Runnable t = new Runnable() {

            @Override
            public void run() {
                mappingModel.addMappingModelListener(MappingComponent.this);
                //currentBoundingBox=mm.getInitialBoundingBox();
                TreeMap rs = mappingModel.getRasterServices();
                //reCalcWtstAndBoundingBox();

                //Rückwärts wegen der Reihenfolge der Layer im Layer Widget
                Iterator it = rs.keySet().iterator();
                while (it.hasNext()) {
                    Object key = it.next();
                    int rsi = ((Integer) key).intValue();
                    Object o = rs.get(key);
                    if (o instanceof MapService) {
                        addMapService(((MapService) o), rsi);
                    }
                }

                //Es gibt nur noch MapServices


//                TreeMap fs = mappingModel.getFeatureServices();
//                //Rueckwaerts wegen der Reihenfolge der Layer im Layer Widget
//                it = fs.keySet().iterator();
//                while (it.hasNext()) {
//                    Object key = it.next();
//                    int fsi = ((Integer) key).intValue();
//                    Object o = fs.get(key);
//                    if (o instanceof MapService) {
//                        log.debug("neuer Featureservice: " + o);
//                        PLayer pn = new PLayer();
//                        //pn.setVisible(true);
//                        //pn.setBounds(this.getRoot().getFullBounds());
//                        pn.setTransparency(((MapService) o).getTranslucency());
//                        //((FeatureService)o).setPNode(pn);
//                        featureServiceLayer.addChild(pn);
//                        pn.addClientProperty("serviceLayer", (ServiceLayer) o);
//                        //getCamera().addLayer(pn);
//                        ((MapService) o).addRetrievalListener(new MappingComponentFeatureServiceListener((ServiceLayer) o, pn));
//                        log.debug("add FeatureService");
//
//                        //if (internalLayerWidgetAvailable) {
//                        //LayerControl lc = internalLayerWidget.addFeatureService(fs.size() - fsi, (ServiceLayer) o, 3000);
//                        //LayerControl lc=internalLayerWidget.addFeatureService(fs.size()-fsi,(ServiceLayer)o,cismapPrefs.getGlobalPrefs().getErrorAbolitionTime());
////                        ((MapService) o).addRetrievalListener(lc);
////                        lc.setTransparentable(pn);
////                        layerControls.add(lc);
//                    //}
//                    }
//                }
                adjustLayers();
                // TODO MappingModel im InternalLayerWidget setzen, da es  bei
                // der Initialisierung des Widgets NULL ist
                internalLayerWidget = new NewSimpleInternalLayerWidget(MappingComponent.this);
                //internalLayerWidget.setMappingModel(mappingModel);
                add(internalLayerWidget);
                internalLayerWidget.pack();
                //gotoInitialBoundingBox();
                log.debug("Set Mapping Modell done");
            }
        };
       CismetThreadPool.execute(t);
    }

    /**
     * Returns the current mappingmodel.
     * @return current mappingmodel
     */
    public MappingModel getMappingModel() {
        return mappingModel;
    }

    /**
     * Animates a component to a given x/y-coordinate in a given time.
     * @param c the component to animate
     * @param toX final x-position
     * @param toY final y-position
     * @param animationDuration duration of the animation
     * @param hideAfterAnimation should the component be hidden after animation?
     */
    private void animateComponent(final JComponent c, final int toX, final int toY, int animationDuration, final boolean hideAfterAnimation) {
        int x = (int) c.getBounds().getX() - toX;
        int y = (int) c.getBounds().getY() - toY;
        int sx, sy;
        if (x > 0) {
            sx = -1;
        } else {
            sx = 1;
        }
        if (y > 0) {
            sy = -1;
        } else {
            sy = 1;
        }
        int big;
        if (Math.abs(x) > Math.abs(y)) {
            big = Math.abs(x);
        } else {
            big = Math.abs(y);
        }
        final int sleepy = (int) (animationDuration / big);
        final int directionY = sy;
        final int directionX = sx;

        Thread timer = new Thread() {

            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        sleep(sleepy);
                    } catch (Exception iex) {
                    }
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            int currentY = (int) c.getBounds().getY();
                            int currentX = (int) c.getBounds().getX();
                            if (currentY != toY) {
                                currentY = currentY + directionY;
                            }
                            if (currentX != toX) {
                                currentX = currentX + directionX;
                            }
                            c.setBounds(currentX, currentY, c.getWidth(), c.getHeight());
                        }
                    });

                    if (c.getBounds().getY() == toY && c.getBounds().getX() == toX) {
                        if (hideAfterAnimation) {
                            EventQueue.invokeLater(new Runnable() {

                                public void run() {
                                    internalLayerWidget.setVisible(false);
                                    internalLayerWidget.hide();
                                }
                            });
                        }
                        break;
                    }
                }
            }
        };
        timer.setPriority(Thread.NORM_PRIORITY);
        timer.start();
    }

    public NewSimpleInternalLayerWidget getInternalLayerWidget() {
        return internalLayerWidget;
    }

    /**
     * Shows the InternalLayerWidget by sliding it into the mappingcomponent on the 
     * bottom left corner.
     * @param b should the widget be visible after the animation?
     * @param animationDuration duration of the animation
     */
    public void showInternalLayerWidget(boolean b, int animationDuration) {
//        //NORTH WEST
//        int positionX = 1;
//        int positionY = 1;
//
//        //SOUTH WEST
//        positionX = 1;
//        positionY = getHeight() - getInternalLayerWidget().getHeight() - 1;
//
//        //NORTH EAST
//        positionX = getWidth() - getInternalLayerWidget().getWidth() - 1;
//        positionY = 1;
//
        //SOUTH EAST
        int positionX = getWidth() - internalLayerWidget.getWidth() - 1;
        int positionY = getHeight() - internalLayerWidget.getHeight() - 1;

        if (b) {
            internalLayerWidget.setVisible(true);
            internalLayerWidget.show();
            internalLayerWidget.setBounds(positionX, positionY + internalLayerWidget.getHeight() + 1, internalLayerWidget.getWidth(), internalLayerWidget.getHeight());
            animateComponent(internalLayerWidget, positionX, positionY, animationDuration, false);

        } else {
            internalLayerWidget.setBounds(positionX, positionY, internalLayerWidget.getWidth(), internalLayerWidget.getHeight());
            animateComponent(internalLayerWidget, positionX, positionY + internalLayerWidget.getHeight() + 1, animationDuration, true);
        }
    }

    /**
     * Returns a boolean, if the InternalLayerWidget is visible.
     * @return true, if visible, else false
     */
    public boolean isInternalLayerWidgetVisible() {
        return internalLayerWidget.isVisible();
    }

    /**
     * Moves the camera to the initial bounding box (e.g. if the home-button is pressed).
     */
    public void gotoInitialBoundingBox() {
        double x1, y1, x2, y2, w, h;
        x1 = getWtst().getScreenX(mappingModel.getInitialBoundingBox().getX1());
        y1 = getWtst().getScreenY(mappingModel.getInitialBoundingBox().getY1());
        x2 = getWtst().getScreenX(mappingModel.getInitialBoundingBox().getX2());
        y2 = getWtst().getScreenY(mappingModel.getInitialBoundingBox().getY2());
        Rectangle2D home = new Rectangle2D.Double();
        home.setRect(x1, y2, x2 - x1, y1 - y2);
        getCamera().animateViewToCenterBounds(home, true, animationDuration);
        if (getCamera().getViewTransform().getScaleY() < 0) {
            log.fatal("gotoInitialBoundingBox: Problem :-( mit getViewTransform");
        }
        setNewViewBounds(home);
        queryServices();
    }

    /**
     * Refreshs all registered services.
     */
    public void queryServices() {
        if (newViewBounds != null) {
            addToHistory(new PBoundsWithCleverToString(new PBounds(newViewBounds), wtst));
            queryServicesWithoutHistory();
            log.debug("queryServices()");
            rescaleStickyNodes();
        }
    }

    /**
     * Forces all services to refresh themselves.
     */
    public void refresh() {
        forceQueryServicesWithoutHistory();
    }

    /**
     * Forces all services to refresh themselves.
     */
    private void forceQueryServicesWithoutHistory() {
        queryServicesWithoutHistory(true);
    }

    /**
     * Refreshs all services, but not forced.
     */
    private void queryServicesWithoutHistory() {
        queryServicesWithoutHistory(false);
    }

    /**
     * Waits until all animations are done, then iterates through all registered
     * services and calls handleMapService() for each.
     * @param forced forces the refresh
     */
    private void queryServicesWithoutHistory(final boolean forced) {
        if (!locked) {
            Runnable t = new Runnable() {

                @Override
                public void run() {
                    while (getAnimating()) {
                        try {
                            Thread.sleep(50);
                        } catch (Exception doNothing) {
                        }
                    }
                    CismapBroker.getInstance().fireMapBoundsChanged();

                    if (MappingComponent.this.isBackgroundEnabled()) {
                        TreeMap rs = mappingModel.getRasterServices();
                        TreeMap fs = mappingModel.getFeatureServices();

                        for (Iterator it = rs.keySet().iterator(); it.hasNext();) {
                            Object key = it.next();
                            int rsi = ((Integer) key).intValue();
                            Object o = rs.get(key);
                            if (o instanceof MapService) {
                                handleMapService(rsi, (MapService) o, forced);
                            }
                        }
                        for (Iterator it = fs.keySet().iterator(); it.hasNext();) {
                            Object key = it.next();
                            int fsi = ((Integer) key).intValue();
                            Object o = fs.get(key);
                            if (o instanceof MapService) {
                                handleMapService(fsi, (MapService) o, forced);
                            }
                        }
                    }
                }
            };
            CismetThreadPool.execute(t);
        }
    }

    /**
     * queryServicesIndependentFromMap
     * @param width
     * @param height
     * @param bb
     * @param rl
     */
    public void queryServicesIndependentFromMap(final int width, final int height, final BoundingBox bb, final RetrievalListener rl) {
        Runnable t = new Runnable() {

            @Override
            public void run() {
                while (getAnimating()) {
                    try {
                        Thread.sleep(50);
                    } catch (Exception doNothing) {
                    }
                }
                if (MappingComponent.this.isBackgroundEnabled()) {
                    TreeMap rs = mappingModel.getRasterServices();
                    TreeMap fs = mappingModel.getFeatureServices();
                    for (Iterator it = rs.keySet().iterator(); it.hasNext();) {
                        Object key = it.next();
                        int rsi = ((Integer) key).intValue();
                        Object o = rs.get(key);
                        if (o instanceof AbstractRetrievalService && o instanceof ServiceLayer && ((ServiceLayer) o).isEnabled() && o instanceof RetrievalServiceLayer && ((RetrievalServiceLayer) o).getPNode().getVisible()) {
                            // AbstractRetrievalService r = ((AbstractRetrievalService) o).cloneWithoutRetrievalListeners();
                            AbstractRetrievalService r;
                            if (o instanceof WebFeatureService) {
                                WebFeatureService wfsClone = (WebFeatureService) ((WebFeatureService) o).clone();
                                wfsClone.removeAllListeners();
                                r = wfsClone;
                            } else {
                                r = ((AbstractRetrievalService) o).cloneWithoutRetrievalListeners();
                            }
                            r.addRetrievalListener(rl);
                            ((ServiceLayer) r).setLayerPosition(rsi);
                            handleMapService(rsi, (MapService) r, width, height, bb, true);
                        }
                    }
                    for (Iterator it = fs.keySet().iterator(); it.hasNext();) {
                        Object key = it.next();
                        int fsi = ((Integer) key).intValue();
                        Object o = fs.get(key);
                        if (o instanceof AbstractRetrievalService) {
                            AbstractRetrievalService r;
                            if (o instanceof WebFeatureService) {
                                WebFeatureService wfsClone = (WebFeatureService) ((WebFeatureService) o).clone();
                                wfsClone.removeAllListeners();
                                r = (AbstractRetrievalService) o;
                            } else {
                                r = ((AbstractRetrievalService) o).cloneWithoutRetrievalListeners();
                            }
                            r.addRetrievalListener(rl);
                            ((ServiceLayer) r).setLayerPosition(fsi);
                            handleMapService(fsi, (MapService) r, 0, 0, bb, true);
                        }
                    }
                }
            }
        };
        CismetThreadPool.execute(t);
    }

    /**
     * 
     * former synchronized method
     * @param position
     * @param rs
     * @param forced
     */
    public void handleMapService(int position, final MapService service, boolean forced) {
        log.debug("in handleRasterService:" + service + "(" + Integer.toHexString(System.identityHashCode(service)) + ")(" + service.hashCode() + ")");

        PBounds bounds = getCamera().getViewBounds();
        BoundingBox bb = new BoundingBox();
        double x1 = getWtst().getWorldX(bounds.getMinX());
        double y1 = getWtst().getWorldY(bounds.getMaxY());
        double x2 = getWtst().getWorldX(bounds.getMaxX());
        double y2 = getWtst().getWorldY(bounds.getMinY());

        log.debug("Bounds=" + bounds);
        log.debug("handleRasterService BoundingBox(" + x1 + " " + y1 + "," + x2 + " " + y2 + ")");

        if (((ServiceLayer) service).getName().startsWith("prefetching")) {
            bb.setX1(x1 - (x2 - x1));
            bb.setY1(y1 - (y2 - y1));
            bb.setX2(x2 + (x2 - x1));
            bb.setY2(y2 + (y2 - y1));
        } else {
            bb.setX1(x1);
            bb.setY1(y1);
            bb.setX2(x2);
            bb.setY2(y2);
        }
        handleMapService(position, service, getWidth(), getHeight(), bb, forced);
    }

    /**
     * 
     * former synchronized method
     * @param position
     * @param rs
     * @param width
     * @param height
     * @param bb
     * @param forced
     */
    private void handleMapService(int position, final MapService rs, int width, int height, final BoundingBox bb, final boolean forced) {
        log.debug("handleMapService ", new CurrentStackTrace());
        if (((ServiceLayer) rs).isEnabled()) {
            rs.setSize(height, width);
            //log.debug("this.currentBoundingBox:"+this.currentBoundingBox);
            //If the PCanvas is in animation state, there should be a pre information about the
            //aimed new bounds
            Runnable handle = new Runnable() {

                @Override
                public void run() {
                    while (getAnimating()) {
                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {
                        }
                    }
                    rs.setBoundingBox(bb);
                    if (rs instanceof FeatureAwareRasterService) {
                        ((FeatureAwareRasterService) rs).setFeatureCollection(featureCollection);
                    }
                    rs.retrieve(forced);
                }
            };
            CismetThreadPool.execute(handle);
        }
    }
//former synchronized method

//    private void handleFeatureService(int position, final FeatureService fs, boolean forced) {
//        synchronized (handleFeatureServiceBlocker) {
//            PBounds bounds = getCamera().getViewBounds();
//
//            BoundingBox bb = new BoundingBox();
//            double x1 = getWtst().getSourceX(bounds.getMinX());
//            double y1 = getWtst().getSourceY(bounds.getMaxY());
//            double x2 = getWtst().getSourceX(bounds.getMaxX());
//            double y2 = getWtst().getSourceY(bounds.getMinY());
//
//            log.debug("handleFeatureService BoundingBox(" + x1 + " " + y1 + "," + x2 + " " + y2 + ")");
//
//            bb.setX1(x1);
//            bb.setY1(y1);
//            bb.setX2(x2);
//            bb.setY2(y2);
//            handleFeatureService(position, fs, bb, forced);
//        }
//    }
////former synchronized method
//    Object handleFeatureServiceBlocker2 = new Object();
//
//    private void handleFeatureService(int position, final FeatureService fs, final BoundingBox bb, final boolean forced) {
//        synchronized (handleFeatureServiceBlocker2) {
//            log.debug("handleFeatureService");
//            if (fs instanceof ServiceLayer && ((ServiceLayer) fs).isEnabled()) {
//                Thread handle = new Thread() {
//
//                    @Override
//                    public void run() {
//                        while (getAnimating()) {
//                            try {
//                                sleep(50);
//                            } catch (Exception e) {
//                                log.error("Fehler im handle FeatureServicethread");
//                            }
//                        }
//                        fs.setBoundingBox(bb);
//                        fs.retrieve(forced);
//                    }
//                };
//                handle.setPriority(Thread.NORM_PRIORITY);
//                handle.start();
//            }
//        }
//    }
    /**
     * Creates a new WorldToScreenTransform for the current screensize (boundingbox) 
     * and returns it.
     * @return new WorldToScreenTransform or null
     */
    public WorldToScreenTransform getWtst() {
        try {
            if (wtst == null) {
                double y_real = mappingModel.getInitialBoundingBox().getY2() - mappingModel.getInitialBoundingBox().getY1();
                double x_real = mappingModel.getInitialBoundingBox().getX2() - mappingModel.getInitialBoundingBox().getX1();

                double clip_height;
                double clip_width;

                double x_screen = getWidth();
                double y_screen = getHeight();

                if (x_screen == 0) {
                    x_screen = 100;
                }
                if (y_screen == 0) {
                    y_screen = 100;
                }

                if (x_real / x_screen >= y_real / y_screen) { //X ist Bestimmer d.h. x wird nicht verändert
                    clip_height = x_screen * y_real / x_real;
                    clip_width = x_screen;
                    clip_offset_y = 0;//(y_screen-clip_height)/2;
                    clip_offset_x = 0;
                } else { // Y ist Bestimmer
                    clip_height = y_screen;
                    clip_width = y_screen * x_real / y_real;
                    clip_offset_y = 0;
                    clip_offset_x = 0;//(x_screen-clip_width)/2;
                }
                //wtst= new WorldToScreenTransform(mappingModel.getInitialBoundingBox().getX1(),mappingModel.getInitialBoundingBox().getY1(),mappingModel.getInitialBoundingBox().getX2(),mappingModel.getInitialBoundingBox().getY2(),0,0,clip_width,clip_height);
                //wtst= new WorldToScreenTransform(mappingModel.getInitialBoundingBox().getX1(),mappingModel.getInitialBoundingBox().getY1(),mappingModel.getInitialBoundingBox().getX2(),mappingModel.getInitialBoundingBox().getY2(),0,0, x_real,y_real);
                //wtst= new WorldToScreenTransform(2566470,5673088,2566470+100,5673088+100,0,0,100,100);
                //             wtst= new WorldToScreenTransform(-180,-90,180,90,0,0,180,90);
                // wtst= new WorldToScreenTransform(mappingModel.getInitialBoundingBox().getX1(),mappingModel.getInitialBoundingBox().getY1(),mappingModel.getInitialBoundingBox().getX1()+100,mappingModel.getInitialBoundingBox().getY1()+100,0,0,100,100);
                //wtst= new WorldToScreenTransform(0,0, 1000, 1000, 0,0, 1000,1000);
                wtst = new WorldToScreenTransform(mappingModel.getInitialBoundingBox().getX1(), mappingModel.getInitialBoundingBox().getY2());
            }
            return wtst;
        } catch (Throwable t) {
            log.error("Fehler in getWtst()", t);
            return null;
        }
    }

    /**
     * Resets the current WorldToScreenTransformation.
     */
    public void resetWtst() {
        wtst = null;
    }

    /**
     * Returns 0.
     */
    public double getClip_offset_x() {
        return 0;//clip_offset_x;
    }

    /**
     * Assigns a new value to the x-clip-offset.
     * @param clip_offset_x new clipoffset
     */
    public void setClip_offset_x(double clip_offset_x) {
        this.clip_offset_x = clip_offset_x;
    }

    /**
     * Returns 0.
     */
    public double getClip_offset_y() {
        return 0;//clip_offset_y;
    }

    /**
     * Assigns a new value to the y-clip-offset.
     * @param clip_offset_y new clipoffset
     */
    public void setClip_offset_y(double clip_offset_y) {
        this.clip_offset_y = clip_offset_y;
    }

    /**
     * Returns the rubberband-PLayer.
     */
    public PLayer getRubberBandLayer() {
        return rubberBandLayer;
    }

    /**
     * Assigns a given PLayer to the variable rubberBandLayer.
     * @param rubberBandLayer a PLayer
     */
    public void setRubberBandLayer(PLayer rubberBandLayer) {
        this.rubberBandLayer = rubberBandLayer;
    }

    /**
     * Sets new viewbounds.
     * @param r2d Rectangle2D
     */
    public void setNewViewBounds(Rectangle2D r2d) {
        newViewBounds = r2d;
    }

    /**
     * Will be called if the selection of features changes. It selects the PFeatures
     * connected to the selected features of the featurecollectionevent and moves them
     * to the front. Also repaints handles at the end.
     * @param fce featurecollectionevent with selected features
     */
    public void featureSelectionChanged(FeatureCollectionEvent fce) {
        Collection<PFeature> all = featureLayer.getChildrenReference();
        for (PFeature f : all) {
            f.setSelected(false);
        }
        Collection<Feature> c;
        if (fce != null) {
            c = fce.getFeatureCollection().getSelectedFeatures();
        } else {
            c = featureCollection.getSelectedFeatures();
        }
        for (Feature f : c) {
            if (f != null) {
                PFeature feature = (PFeature) getPFeatureHM().get(f);

                if (feature != null) {
                    feature.setSelected(true);
                    feature.moveToFront();
                    //Fuer den selectedObjectPresenter (Eigener PCanvas)
                    syncSelectedObjectPresenter(1000);
                } else {
                    handleLayer.removeAllChildren();
                }
            }
        }
        showHandles(false);
    }

    /**
     * Will be called if one or more features are changed somehow (handles moved/rotated).
     * Calls reconsiderFeature() on each feature of the given featurecollectionevent.
     * Repaints handles at the end.
     * @param fce featurecollectionevent with changed features
     */
    public void featuresChanged(FeatureCollectionEvent fce) {
        log.debug("featuresChanged");
        Vector<Feature> v = new Vector<Feature>();
        v.addAll(fce.getEventFeatures());
        for (Feature elem : v) {
            reconsiderFeature(elem);
        }
        showHandles(false);
    }

    /**
     * Does a complete reconciliation of the PFeature assigned to a feature from the
     * FeatureCollectionEvent. Calls following PFeature-methods:
     * syncGeometry(), visualize(), resetInfoNodePosition() and refreshInfoNode()
     * @param fce featurecollectionevent with features to reconsile
     */
    public void featureReconsiderationRequested(FeatureCollectionEvent fce) {
        for (Feature f : fce.getEventFeatures()) {
            if (f != null) {
                PFeature node = ((PFeature) pFeatureHM.get(f));
                if (node != null) {
                    node.syncGeometry();
                    node.visualize();
                    node.resetInfoNodePosition();
                    node.refreshInfoNode();
                    repaint();
                    // für Mehrfachzeichnung der Handles verantworlich ??
//                    showHandles(false);
                }
            }
        }
    }

    /**
     * Adds all features from the FeatureCollectionEvent to the mappingcomponent.
     * Paints handles at the end. Former synchronized method.
     * @param fce FeatureCollectionEvent with features to add
     */
    public void featuresAdded(FeatureCollectionEvent fce) {
        log.debug("firefeaturesAdded (old disabled)");
        //Attention: Bug-Gefahr !!! TODO
        //addFeaturesToMap(fce.getEventFeatures().toArray(new Feature[0]));
        //log.debug("featuresAdded()");
    }

    /**
     * Method is deprecated and deactivated. Does nothing!!
     * @deprecated
     */
    public void featureCollectionChanged() {
//        if (getFeatureCollection() instanceof DefaultFeatureCollection) {
//            DefaultFeatureCollection coll=((DefaultFeatureCollection)getFeatureCollection());
//            Vector<String> layers=coll.getAllLayers();
//            for (String layer :layers) {
//                if (!featureLayers.keySet().contains(layer)) {
//                    PLayer pLayer=new PLayer();
//                    featureLayer.addChild(pLayer);
//                    featureLayers.put(layer,pLayer);
//                }
//            }
//        }
    }

    /**
     * Clears the PFeatureHashmap and removes all PFeatures from the featurelayer.
     * Does a checkFeatureSupportingRasterServiceAfterFeatureRemoval() on all features
     * from the given FeatureCollectionEvent.
     * @param fce FeatureCollectionEvent with features to check for a remaining
     * supporting rasterservice
     */
    public void allFeaturesRemoved(FeatureCollectionEvent fce) {
        pFeatureHM.clear();
        featureLayer.removeAllChildren();
        checkFeatureSupportingRasterServiceAfterFeatureRemoval(fce);
    }

    /**
     * Removes all features of the given FeatureCollectionEvent from the mappingcomponent.
     * Checks for remaining supporting rasterservices and paints handles at the end.
     * @param fce FeatureCollectionEvent with features to remove
     */
    public void featuresRemoved(FeatureCollectionEvent fce) {
        log.debug("featuresRemoved");
        removeFeatures(fce.getEventFeatures());
        checkFeatureSupportingRasterServiceAfterFeatureRemoval(fce);
        showHandles(false);
    }

    /**
     * Checks after removing one or more features from the mappingcomponent which
     * rasterservices became unnecessary and which need a refresh.
     * @param fce FeatureCollectionEvent with removed features
     */
    private void checkFeatureSupportingRasterServiceAfterFeatureRemoval(FeatureCollectionEvent fce) {
        HashSet<FeatureAwareRasterService> rasterServicesWhichShouldBeRemoved = new HashSet<FeatureAwareRasterService>();
        HashSet<FeatureAwareRasterService> rasterServicesWhichShouldBeRefreshed = new HashSet<FeatureAwareRasterService>();
        HashSet<FeatureAwareRasterService> rasterServices = new HashSet<FeatureAwareRasterService>();

        for (Feature f : getFeatureCollection().getAllFeatures()) {
            if (f instanceof RasterLayerSupportedFeature && ((RasterLayerSupportedFeature) f).getSupportingRasterService() != null) {
                FeatureAwareRasterService rs = ((RasterLayerSupportedFeature) f).getSupportingRasterService();
                log.debug("getAllFeatures() Feature:SupportingRasterService:" + f + ":" + rs);
                rasterServices.add(rs); //DANGER
            }
        }

        for (Feature f : fce.getEventFeatures()) {
            if (f instanceof RasterLayerSupportedFeature && ((RasterLayerSupportedFeature) f).getSupportingRasterService() != null) {
                FeatureAwareRasterService rs = ((RasterLayerSupportedFeature) f).getSupportingRasterService();
                log.debug("getEventFeatures() Feature:SupportingRasterService:" + f + ":" + rs);
                if (rasterServices.contains(rs)) {
                    for (Object o : getMappingModel().getRasterServices().values()) {
                        MapService r = (MapService) o;
                        if (r.equals(rs)) {
                            rasterServicesWhichShouldBeRefreshed.add((FeatureAwareRasterService) r);
                        }
                    }
                } else {
                    for (Object o : getMappingModel().getRasterServices().values()) {
                        MapService r = (MapService) o;
                        if (r.equals(rs)) {
                            rasterServicesWhichShouldBeRemoved.add((FeatureAwareRasterService) r);
                        }
                    }
                }
            }
        }
        for (FeatureAwareRasterService frs : rasterServicesWhichShouldBeRemoved) {
            getMappingModel().removeLayer(frs);
        }
        for (FeatureAwareRasterService frs : rasterServicesWhichShouldBeRefreshed) {
            handleMapService(0, frs, true);
        }
    }

//    public void showFeatureCollection(Feature[] features) {
//        com.vividsolutions.jts.geom.Envelope env=computeFeatureEnvelope(features);
//        showFeatureCollection(features,env);
//    }
//    public void showFeatureCollection(Feature[] f,com.vividsolutions.jts.geom.Envelope featureEnvelope) {
//        selectedFeature=null;
//        handleLayer.removeAllChildren();
//        //setRasterServiceLayerImagesVisibility(false);
//        Envelope eSquare=null;
//        HashSet<Feature> featureSet=new HashSet<Feature>();
//        featureSet.addAll(holdFeatures);
//        featureSet.addAll(java.util.Arrays.asList(f));
//        Feature[] features=featureSet.toArray(new Feature[0]);
//
//        pFeatureHM.clear();
//        addFeaturesToMap(features);
//        zoomToFullFeatureCollectionBounds();
//
//    }
    /**
     * Creates new PFeatures for all features in the given array and adds them to the
     * PFeatureHashmap. Then adds the PFeature to the featurelayer.
     * 
     * DANGER: there's a bug risk here because the method runs in an own thread! It is 
     * possible that a PFeature of a feature is demanded but not yet added to the 
     * hashmap which causes in most cases a NullPointerException!
     * @param features array with features to add
     */
    public void addFeaturesToMap(final Feature[] features) {
        Runnable t = new Runnable() {

            @Override
            public void run() {
                double local_clip_offset_y = clip_offset_y;
                double local_clip_offset_x = clip_offset_x;

                /// Hier muss der layer bestimmt werdenn
                for (int i = 0; i < features.length; ++i) {
                    final PFeature p = new PFeature(features[i], getWtst(), local_clip_offset_x, local_clip_offset_y, MappingComponent.this);
                    try {
                        if (features[i] instanceof StyledFeature) {
                            p.setTransparency(((StyledFeature) (features[i])).getTransparency());
                        } else {
                            p.setTransparency(cismapPrefs.getLayersPrefs().getAppFeatureLayerTranslucency());
                        }
                    } catch (Exception e) {
                        p.setTransparency(0.8f);
                        log.info("Fehler beim Setzen der Transparenzeinstellungen", e);
                    }
                    // So kann man es Piccolo überlassen (müsste nur noch ein transformation machen, die die y achse spiegelt)
                    // PFeature p=new PFeature(features[i],(WorldToScreenTransform)null,(double)0,(double)0);
                    // p.setViewer(this);
                    // ((PPath)p).setStroke(new BasicStroke(0.5f));
                    if (features[i].getGeometry() != null) {
                        pFeatureHM.put(p.getFeature(), p);
                        for (int j = 0; j < p.getCoordArr().length; ++j) {
                            pFeatureHMbyCoordinate.put(p.getCoordArr()[j], new PFeatureCoordinatePosition(p, j));
                        }
                        final int ii = i;
                        EventQueue.invokeLater(new Runnable() {

                            public void run() {
                                featureLayer.addChild(p);
                                if (!(features[ii].getGeometry() instanceof com.vividsolutions.jts.geom.Point)) {
                                    //p.moveToBack();
                                    p.moveToFront();
                                }
                            }
                        });
                    }
                }
                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        rescaleStickyNodes();
                        repaint();
                        fireFeaturesAddedToMap(Arrays.asList(features));
                    }
                });
            }
        };
        CismetThreadPool.execute(t);

        //check whether the feature has a rasterSupportLayer or not
        for (Feature f : features) {
            if (f instanceof RasterLayerSupportedFeature && ((RasterLayerSupportedFeature) f).getSupportingRasterService() != null) {
                FeatureAwareRasterService rs = ((RasterLayerSupportedFeature) f).getSupportingRasterService();
                if (!getMappingModel().getRasterServices().containsValue(rs)) {
                    log.debug("FeatureAwareRasterServiceAdded");
                    rs.setFeatureCollection(getFeatureCollection());
                    getMappingModel().addLayer(rs);
                }
            }
        }

        showHandles(false);
    }

    private void fireFeaturesAddedToMap(Collection<Feature> cf) {
        for (MapListener curMapListener : mapListeners) {
            curMapListener.featuresAddedToMap(cf);
        }
    }

    /**
     * Returns a list of PFeatureCoordinatePositions which are located at the given 
     * coordinate.
     * @param c Coordinate
     * @return list of PFeatureCoordinatePositions
     */
    public List<PFeatureCoordinatePosition> getPFeaturesByCoordinates(Coordinate c) {
        List<PFeatureCoordinatePosition> l = (List<PFeatureCoordinatePosition>) pFeatureHMbyCoordinate.get(c);
        return l;
    }

    /**
     * Creates an envelope around all features from the given array.
     * @param features features to create the envelope around
     * @return Envelope com.vividsolutions.jts.geom.Envelope
     */
    private com.vividsolutions.jts.geom.Envelope computeFeatureEnvelope(Feature[] features) {
        PNode root = new PNode();
        for (int i = 0; i < features.length; ++i) {
            PNode p = PNodeFactory.createPFeature(features[i], this);
            if (p != null) {
                root.addChild(p);
            }
        }
        PBounds ext = root.getFullBounds();
        com.vividsolutions.jts.geom.Envelope env = new com.vividsolutions.jts.geom.Envelope(ext.x, ext.x + ext.width, ext.y, ext.y + ext.height);
        return env;
    }

    /**
     * Zooms in / out to match the bounds of the featurecollection.
     */
    public void zoomToFullFeatureCollectionBounds() {
        zoomToFeatureCollection();
    }

    /**
     * Adds a new cursor to the cursor-hashmap.
     * @param mode interactionmode as string
     * @param cursor cursor-object
     */
    public void putCursor(String mode, Cursor cursor) {
        cursors.put(mode, cursor);
    }

    /**
     * Returns the cursor assigned to the given mode.
     * @param mode mode as String
     * @return Cursor-object or null
     */
    public Cursor getCursor(String mode) {
        Object o = cursors.get(mode);
        if (o instanceof Cursor) {
            return (Cursor) o;
        } else {
            return null;
        }
    }

    /**
     * Adds a new PBasicInputEventHandler for a specific interactionmode.
     * @param mode interactionmode as string
     * @param listener new PBasicInputEventHandler
     */
    public void addInputListener(String mode, PBasicInputEventHandler listener) {
        inputEventListener.put(mode, listener);
    }

    /**
     * Returns the PBasicInputEventHandler assigned to the committed interactionmode.
     * @param mode interactionmode as string
     * @return PBasicInputEventHandler-object or null
     */
    public PInputEventListener getInputListener(String mode) {
        Object o = inputEventListener.get(mode);
        if (o instanceof PInputEventListener) {
            return (PInputEventListener) o;
        } else {
            return null;
        }
    }

    /**
     * Returns whether the features are editable or not.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     *Sets all Features ReadOnly use Feature.setEditable(boolean) instead
     */
    public void setReadOnly(boolean readOnly) {
        for (Object f : featureCollection.getAllFeatures()) {
            ((Feature) f).setEditable(!readOnly);
        }
        this.readOnly = readOnly;
        handleLayer.removeAllChildren();
        handleLayer.repaint();

        //       if (readOnly==false) {
        handleLayer.removeAllChildren();
        snapHandleLayer.removeAllChildren();
//        }
    }

    /**
     * Returns the current HandleInteractionMode.
     */
    public String getHandleInteractionMode() {
        return handleInteractionMode;
    }

    /**
     * Changes the HandleInteractionMode. Repaints handles.
     * @param handleInteractionMode the new HandleInteractionMode
     */
    public void setHandleInteractionMode(String handleInteractionMode) {
        this.handleInteractionMode = handleInteractionMode;
        showHandles(false);
    }

    /**
     * Returns whether the background is enabled or not. 
     */
    public boolean isBackgroundEnabled() {
        return backgroundEnabled;
    }

    /**
     * TODO
     * @param backgroundEnabled
     */
    public void setBackgroundEnabled(boolean backgroundEnabled) {
        if (backgroundEnabled == false && isBackgroundEnabled() == true) {
            featureServiceLayerVisible = featureServiceLayer.getVisible();
        }

        this.mapServicelayer.setVisible(backgroundEnabled);
        this.featureServiceLayer.setVisible(backgroundEnabled && featureServiceLayerVisible);
        for (int i = 0; i < featureServiceLayer.getChildrenCount(); ++i) {
            featureServiceLayer.getChild(i).setVisible(backgroundEnabled && featureServiceLayerVisible);
        }

        if (backgroundEnabled != isBackgroundEnabled() && isBackgroundEnabled() == false) {
            this.queryServices();
        }
        this.backgroundEnabled = backgroundEnabled;
    }

    /**
     * Returns the featurelayer.
     */
    public PLayer getFeatureLayer() {
        return featureLayer;
    }

    /**
     * Adds a PFeature to the PFeatureHashmap.
     * @param p PFeature to add
     */
    public void refreshHM(PFeature p) {
        pFeatureHM.put(p.getFeature(), p);
    }

    /**
     * Returns the selectedObjectPresenter (PCanvas).
     * @return
     */
    public PCanvas getSelectedObjectPresenter() {
        return selectedObjectPresenter;
    }

    /**
     * If f != null it calls the reconsiderFeature()-method of the featurecollection.
     * @param f feature to reconcile
     */
    public void reconsiderFeature(Feature f) {
        if (f != null) {
            featureCollection.reconsiderFeature(f);
        }
    }

    /**
     * Removes all features of the collection from the hashmap.
     * @param fc collection of features to remove
     */
    public void removeFeatures(Collection<Feature> fc) {
        featureLayer.setVisible(false);
        for (Feature elem : fc) {
            removeFromHM(elem);
        }
        featureLayer.setVisible(true);
    }

    /**
     * Removes a Feature from the PFeatureHashmap. Uses the delivered feature as 
     * hashmap-key.
     * @param f feature of the Pfeature that should be deleted
     */
    public void removeFromHM(Feature f) {
        log.debug("pFeatureHM" + pFeatureHM);
        PFeature pf = (PFeature) pFeatureHM.get(f);

        if (pf != null) {
            pf.cleanup();
            pFeatureHM.remove(f);
            try {
                log.info("Entferne Feature " + f);
                featureLayer.removeChild(pf);
            } catch (Exception ex) {
                log.debug("Remove Child ging Schief. Ist beim Splitten aber normal.", ex);
            }
        } else {
            log.warn("Feature war nicht in pFeatureHM");
        }
    }

    /**
     * Zooms to the current selected node.
     * @deprecated
     */
    public void zoomToSelectedNode() {
        zoomToSelection();
    }

    /**
     * Zooms to the current selected features.
     */
    public void zoomToSelection() {
        Collection<Feature> selection = featureCollection.getSelectedFeatures();
        zoomToAFeatureCollection(selection, true, false);
    }

    /**
     * Zooms to a specific featurecollection.
     * @param collection the featurecolltion
     * @param withHistory should the zoomaction be undoable
     * @param fixedScale fixedScale
     */
    public void zoomToAFeatureCollection(Collection<Feature> collection, boolean withHistory, boolean fixedScale) {
        log.debug("zoomToAFeatureCollection");
        handleLayer.removeAllChildren();
        boolean first = true;
        Geometry g = null;

        for (Feature f : collection) {
            if (first) {
                g = f.getGeometry().getEnvelope();
                if (f instanceof Bufferable) {
                    g = g.buffer(((Bufferable) f).getBuffer());
                }
                first = false;
            } else {
                if (f.getGeometry() != null) {
                    Geometry geometry = f.getGeometry().getEnvelope();
                    if (f instanceof Bufferable) {
                        geometry = geometry.buffer(((Bufferable) f).getBuffer());
                    }
                    g = g.getEnvelope().union(geometry);
                }
            }
        }

        if (g != null) {
            //dreisatz.de
            double hBuff = g.getEnvelopeInternal().getHeight() / ((double) getHeight()) * 10;
            double vBuff = g.getEnvelopeInternal().getWidth() / ((double) getWidth()) * 10;
            if (getHeight() == 0 || getWidth() == 0) {
                log.fatal("DIVISION BY ZERO");
            }

            double buff = 0;
            if (hBuff > vBuff) {
                buff = hBuff;
            } else {
                buff = vBuff;
            }
            g = g.buffer(buff);
            BoundingBox bb = new BoundingBox(g);
            gotoBoundingBox(bb, withHistory, !fixedScale, animationDuration);
        }
    }

    /**
     * Deletes all present handles from the handlelayer. Tells all selected features
     * in the featurecollection to create their handles and to add them to the
     * handlelayer. 
     * @param waitTillAllAnimationsAreComplete wait until all animations are completed 
     * before create the handles
     */
    public void showHandles(final boolean waitTillAllAnimationsAreComplete) {
        // are there features selected?
        if (featureCollection.getSelectedFeatures().size() > 0) {
            // DANGER Mehrfachzeichnen von Handles durch parallelen Aufruf
            Runnable handle = new Runnable() {

                @Override
                public void run() {
                    // alle bisherigen Handles entfernen
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            handleLayer.removeAllChildren();
                        }
                    });
                    while (getAnimating() && waitTillAllAnimationsAreComplete) {
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {
                            log.warn("Unterbrechung bei getAnimating()", e);
                        }
                    }

                    if (featureCollection.areFeaturesEditable() && (getInteractionMode().equals(SELECT) ||
                            getInteractionMode().equals(PAN) ||
                            getInteractionMode().equals(ZOOM) ||
                            getInteractionMode().equals(SPLIT_POLYGON))) {
                        // Handles für alle selektierten Features der Collection hinzufügen
                        if (getHandleInteractionMode().equals(ROTATE_POLYGON)) {
                            LinkedHashSet<Feature> copy = new LinkedHashSet(featureCollection.getSelectedFeatures());
                            for (final Object selectedFeature : copy) {
                                if (selectedFeature instanceof Feature && ((Feature) selectedFeature).isEditable()) {
                                    if (pFeatureHM.get(selectedFeature) != null) {
                                        ((PFeature) pFeatureHM.get(selectedFeature)).addRotationHandles(handleLayer);
                                    } else {
                                        log.warn("pFeatureHM.get(" + selectedFeature + ")==null");
                                    }
                                }
                            }
                        } else {
                            LinkedHashSet<Feature> copy = new LinkedHashSet(featureCollection.getSelectedFeatures());
                            for (Object selectedFeature : copy) {
                                if (selectedFeature instanceof Feature && ((Feature) selectedFeature).isEditable()) {
                                    if (pFeatureHM.get(selectedFeature) != null) {
                                        ((PFeature) pFeatureHM.get(selectedFeature)).addHandles(handleLayer);
                                    } else {
                                        log.warn("pFeatureHM.get(" + selectedFeature + ")==null");
                                    }
                                    // DANGER mit break werden nur die Handles EINES slektierten Features angezeigt
                                    // wird break auskommentiert werden jedoch zu viele Handles angezeigt
//                                break;
                                }
                            }
                        }
                    }
                }
            };
            log.debug("showHandles", new CurrentStackTrace());
            CismetThreadPool.execute(handle);
        } else {
            // alle bisherigen Handles entfernen
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    handleLayer.removeAllChildren();
                }
            });
        }
    }

    /**
     * Will return a PureNewFeature if there is only one in the featurecollection
     * else null.
     */
    public PFeature getSolePureNewFeature() {
        int counter = 0;
        PFeature sole = null;
        for (Iterator it = featureLayer.getChildrenIterator(); it.hasNext();) {
            Object o = it.next();
            if (o instanceof PFeature) {
                if (((PFeature) o).getFeature() instanceof PureNewFeature) {
                    ++counter;
                    sole = ((PFeature) o);
                }
            }
        }
        if (counter == 1) {
            return sole;
        } else {
            return null;
        }
    }

    /**
     * Sets the visibility of the children of the rasterservicelayer.
     * @param visible true sets visible
     */
    private void setRasterServiceLayerImagesVisibility(boolean visible) {
        Iterator it = mapServicelayer.getChildrenIterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof XPImage) {
                ((XPImage) o).setVisible(visible);
            }
        }
    }

    /**
     * Returns the temporary featurelayer.
     */
    public PLayer getTmpFeatureLayer() {
        return tmpFeatureLayer;
    }

    /**
     * Assigns a new temporary featurelayer.
     * @param tmpFeatureLayer PLayer
     */
    public void setTmpFeatureLayer(PLayer tmpFeatureLayer) {
        this.tmpFeatureLayer = tmpFeatureLayer;
    }

    /**
     * Returns whether the grid is enabled or not.
     */
    public boolean isGridEnabled() {
        return gridEnabled;
    }

    /**
     * Enables or disables the grid.
     * @param gridEnabled true, to enable the grid
     */
    public void setGridEnabled(boolean gridEnabled) {
        this.gridEnabled = gridEnabled;
    }

    /**
     * Returns a String from two double-values. Serves the visualization.
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return a Strin-object like "(X,Y)"
     */
    public static String getCoordinateString(double x, double y) {
        DecimalFormat df = new DecimalFormat("0.00");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(dfs);
        return "(" + df.format(x) + "," + df.format(y) + ")";
    }

    public PLayer getHandleLayer() {
        return handleLayer;
    }

    public void setHandleLayer(PLayer handleLayer) {
        this.handleLayer = handleLayer;
    }

    public boolean isVisualizeSnappingEnabled() {
        return visualizeSnappingEnabled;
    }

    public void setVisualizeSnappingEnabled(boolean visualizeSnappingEnabled) {
        this.visualizeSnappingEnabled = visualizeSnappingEnabled;
    }

    public boolean isVisualizeSnappingRectEnabled() {
        return visualizeSnappingRectEnabled;
    }

    public void setVisualizeSnappingRectEnabled(boolean visualizeSnappingRectEnabled) {
        this.visualizeSnappingRectEnabled = visualizeSnappingRectEnabled;
    }

    public int getSnappingRectSize() {
        return snappingRectSize;
    }

    public void setSnappingRectSize(int snappingRectSize) {
        this.snappingRectSize = snappingRectSize;
    }

    public PLayer getSnapHandleLayer() {
        return snapHandleLayer;
    }

    public void setSnapHandleLayer(PLayer snapHandleLayer) {
        this.snapHandleLayer = snapHandleLayer;
    }

    public boolean isSnappingEnabled() {
        return snappingEnabled;
    }

    public void setSnappingEnabled(boolean snappingEnabled) {
        this.snappingEnabled = snappingEnabled;
        setVisualizeSnappingEnabled(snappingEnabled);
    }

    public PLayer getFeatureServiceLayer() {
        return featureServiceLayer;
    }

    public void setFeatureServiceLayer(PLayer featureServiceLayer) {
        this.featureServiceLayer = featureServiceLayer;
    }

    public int getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    @Deprecated
    public void setPreferences(CismapPreferences prefs) {
        cismapPrefs = prefs;
        //DefaultMappingModel mm = new DefaultMappingModel();
        ActiveLayerModel mm = new ActiveLayerModel();
        LayersPreferences layersPrefs = prefs.getLayersPrefs();
        GlobalPreferences globalPrefs = prefs.getGlobalPrefs();

        setSnappingRectSize(globalPrefs.getSnappingRectSize());
        setSnappingEnabled(globalPrefs.isSnappingEnabled());
        setVisualizeSnappingEnabled(globalPrefs.isSnappingPreviewEnabled());
        setAnimationDuration(globalPrefs.getAnimationDuration());
        setInteractionMode(globalPrefs.getStartMode());
        // mm.setInitialBoundingBox(globalPrefs.getInitialBoundingBox());
        mm.addHome(globalPrefs.getInitialBoundingBox());
        mm.setSrs(globalPrefs.getInitialBoundingBox().getSrs());

        TreeMap raster = layersPrefs.getRasterServices();
        if (raster != null) {
            Iterator it = raster.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                Object o = raster.get(key);
                if (o instanceof MapService) {
                    // mm.putMapService(((Integer) key).intValue(), (MapService) o);
                    mm.addLayer((RetrievalServiceLayer) o);
                }
            }
        }
        TreeMap features = layersPrefs.getFeatureServices();
        if (features != null) {
            Iterator it = features.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                Object o = features.get(key);
                if (o instanceof MapService) {
                    //TODO
                    //mm.putMapService(((Integer) key).intValue(), (MapService) o);
                    mm.addLayer((RetrievalServiceLayer) o);
                }
            }
        }
        setMappingModel(mm);
    }

    public CismapPreferences getCismapPrefs() {
        return cismapPrefs;
    }

    public void flash(int duration, int animationDuration, int what, int number) {
    }

    public PLayer getDragPerformanceImproverLayer() {
        return dragPerformanceImproverLayer;
    }

    public void setDragPerformanceImproverLayer(PLayer dragPerformanceImproverLayer) {
        this.dragPerformanceImproverLayer = dragPerformanceImproverLayer;
    }

    @Deprecated
    public PLayer getRasterServiceLayer() {
        return mapServicelayer;
    }

    public PLayer getMapServiceLayer() {
        return mapServicelayer;
    }

    public void setRasterServiceLayer(PLayer rasterServiceLayer) {
        this.mapServicelayer = rasterServiceLayer;
    }

    public void showGeometryInfoPanel(Feature f) {
    }
    /**
     * Utility field used by bound properties.
     */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Setter for property taskCounter.
     * former synchronized method
     * @param taskCounter New value of property taskCounter.
     */
    public void fireActivityChanged() {
        propertyChangeSupport.firePropertyChange("activityChanged", null, null);
    }

    /**
     * Returns true, if there's still one layercontrol running. Else false;
     * former synchronized method
     */
    public boolean isRunning() {
        for (Iterator it = layerControls.iterator(); it.hasNext();) {
            if (((LayerControl) (it.next())).isRunning()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the visibility of all infonodes
     * @param visible true, if infonodes should be visible
     */
    public void setInfoNodesVisible(boolean visible) {
        infoNodesVisible = visible;
        for (Iterator it = featureLayer.getChildrenIterator(); it.hasNext();) {
            Object elem = it.next();
            if (elem instanceof PFeature) {
                ((PFeature) elem).setInfoNodeVisible(visible);
            }
        }
        log.debug("setInfoNodesVisible()");
        rescaleStickyNodes();
    }

    /**
     * Adds an object to the historymodel.
     * @param o Object to add
     */
    public void addToHistory(Object o) {
        log.debug("addToHistory:" + o.toString());
        historyModel.addToHistory(o);
    }

    /**
     * Removes a specific HistoryModelListener from the historymodel.
     * @param hml HistoryModelListener
     */
    public void removeHistoryModelListener(de.cismet.tools.gui.historybutton.HistoryModelListener hml) {
        historyModel.removeHistoryModelListener(hml);
    }

    /**
     * Adds aHistoryModelListener to the historymodel.
     * @param hml HistoryModelListener
     */
    public void addHistoryModelListener(de.cismet.tools.gui.historybutton.HistoryModelListener hml) {
        historyModel.addHistoryModelListener(hml);
    }

    /**
     * Sets the maximum value of saved historyactions.
     * @param max new integer value 
     */
    public void setMaximumPossibilities(int max) {
        historyModel.setMaximumPossibilities(max);
    }

    /**
     * Redos the last undone historyaction.
     * @param external true, if fireHistoryChanged-action should be fired
     * @return PBounds of the forward-action
     */
    public Object forward(boolean external) {
        PBounds fwd = (PBounds) historyModel.forward(external);
        log.debug("HistoryModel.forward():" + fwd);
        if (external) {
            this.gotoBoundsWithoutHistory(fwd);
        }
        return fwd;
    }

    /**
     * Undos the last action.
     * @param external true, if fireHistoryChanged-action should be fired
     * @return PBounds of the back-action
     */
    public Object back(boolean external) {
        PBounds back = (PBounds) historyModel.back(external);
        log.debug("HistoryModel.back():" + back);
        if (external) {
            this.gotoBoundsWithoutHistory(back);
        }
        return back;
    }

    /**
     * Returns true, if it's possible to redo an action.
     */
    public boolean isForwardPossible() {
        return historyModel.isForwardPossible();
    }

    /**
     * Returns true, if it's possible to undo an action.
     */
    public boolean isBackPossible() {
        return historyModel.isBackPossible();
    }

    /**
     * Returns a vector with all redo-possibilities.
     */
    public Vector getForwardPossibilities() {
        return historyModel.getForwardPossibilities();
    }

    /**
     * Returns the current element of the historymodel.
     */
    public Object getCurrentElement() {
        return historyModel.getCurrentElement();
    }

    /**
     * Returns a vector with all undo-possibilities.
     */
    public Vector getBackPossibilities() {
        return historyModel.getBackPossibilities();
    }

    /**
     * Returns whether an internallayerwidget is available.
     */
    public boolean isInternalLayerWidgetAvailable() {
        return internalLayerWidgetAvailable;
    }

    /**
     * Sets the variable, if an internallayerwidget is available or not.
     * @param internalLayerWidgetAvailable true, if available
     */
    public void setInternalLayerWidgetAvailable(boolean internalLayerWidgetAvailable) {
        this.internalLayerWidgetAvailable = internalLayerWidgetAvailable;
    }

    public void mapServiceLayerStructureChanged(de.cismet.cismap.commons.MappingModelEvent mme) {
    }

    /**
     * Removes the mapservice from the rasterservicelayer.
     * @param rasterService the removing mapservice
     */
    public void mapServiceRemoved(MapService rasterService) {
        try {
        mapServicelayer.removeChild(rasterService.getPNode());
        }
        catch (Exception e) {
            log.warn("erro rin mapServiceRemoved ",e );
        }
    }

    /**
     * Adds the commited mapservice on the last position to the rasterservicelayer.
     * @param mapService the new mapservice
     */
    public void mapServiceAdded(MapService mapService) {
        addMapService(mapService, mapServicelayer.getChildrenCount());
        if (mapService instanceof FeatureAwareRasterService) {
            ((FeatureAwareRasterService) mapService).setFeatureCollection(getFeatureCollection());
        }
        if (mapService instanceof ServiceLayer && ((ServiceLayer) mapService).isEnabled()) {
            handleMapService(0, mapService, false);
        }
    }

    /**
     * Returns the current OGC scale.
     */
    public double getCurrentOGCScale() {
        //funktioniert nur bei metrischen SRS's
        double h = getCamera().getViewBounds().getHeight() / getHeight();
        double w = getCamera().getViewBounds().getWidth() / getWidth();
//        log.debug("H�he:"+getHeight()+" Breite:"+getWidth());
//        log.debug("H�heReal:"+getCamera().getViewBounds().getHeight()+" BreiteReal:"+getCamera().getViewBounds().getWidth());
        return Math.sqrt(h * h + w * w);//Math.sqrt((getWidth()*getWidth())*2);
    }

    /**
     * Returns the current BoundingBox.
     */
    public BoundingBox getCurrentBoundingBox() {
        if (fixedBoundingBox != null) {
            return fixedBoundingBox;
        } else {
            try {
                PBounds bounds = getCamera().getViewBounds();
                double x1 = wtst.getWorldX(bounds.getX());
                double y1 = wtst.getWorldY(bounds.getY());
                double x2 = x1 + bounds.width;
                double y2 = y1 - bounds.height;
                currentBoundingBox = new BoundingBox(x1, y1, x2, y2);
                //log.debug("getCurrentBoundingBox()" + currentBoundingBox + "(" + (y2 - y1) + "," + (x2 - x1) + ")", new CurrentStackTrace());
                return currentBoundingBox;
            } catch (Throwable t) {
                log.error("Fehler in getCurrentBoundingBox()", t);
                return null;
            }
        }
    }

    /**
     * Returns a BoundingBox with a fixed size.
     */
    public BoundingBox getFixedBoundingBox() {
        return fixedBoundingBox;
    }

    /**
     * Assigns fixedBoundingBox a new value.
     * @param fixedBoundingBox new boundingbox
     */
    public void setFixedBoundingBox(BoundingBox fixedBoundingBox) {
        this.fixedBoundingBox = fixedBoundingBox;
    }

    /**
     * Paints the outline of the forwarded BoundingBox.
     * @param bb BoundingBox
     */
    public void outlineArea(BoundingBox bb) {
        outlineArea(bb, null);
    }

    /**
     * Paints the outline of the forwarded PBounds.
     * @param b PBounds
     */
    public void outlineArea(PBounds b) {
        outlineArea(b, null);
    }

    /**
     * Paints a filled rectangle of the area of the forwarded BoundingBox.
     * @param bb BoundingBox
     * @param fillingPaint Color to fill the rectangle
     */
    public void outlineArea(BoundingBox bb, Paint fillingPaint) {
        PBounds pb = null;
        if (bb != null) {
            pb = new PBounds(wtst.getScreenX(bb.getX1()), wtst.getScreenY(bb.getY2()), bb.getX2() - bb.getX1(), bb.getY2() - bb.getY1());
        }
        outlineArea(pb, fillingPaint);
    }

    /**
     * Paints a filled rectangle of the area of the forwarded PBounds.
     * @param b PBounds to paint
     * @param fillingColor Color to fill the rectangle
     */
    public void outlineArea(PBounds b, Paint fillingColor) {
        if (b == null) {
            if (highlightingLayer.getChildrenCount() > 0) {
                highlightingLayer.removeAllChildren();
            }
        } else {
            highlightingLayer.removeAllChildren();
            highlightingLayer.setTransparency(1);
            PPath rectangle = new PPath();
            rectangle.setPaint(fillingColor);
            rectangle.setStroke(new FixedWidthStroke());
            rectangle.setStrokePaint(new Color(100, 100, 100, 255));
            rectangle.setPathTo(b);
            highlightingLayer.addChild(rectangle);
        }
    }

    /**
     * Highlights the delivered BoundingBox. Calls highlightArea(PBounds b) internally.
     * @param bb BoundingBox to highlight
     */
    public void highlightArea(BoundingBox bb) {
        PBounds pb = null;
        if (bb != null) {
            pb = new PBounds(wtst.getScreenX(bb.getX1()), wtst.getScreenY(bb.getY2()), bb.getX2() - bb.getX1(), bb.getY2() - bb.getY1());
        }
        highlightArea(pb);
    }

    /**
     * Highlights the delivered PBounds by painting over with a transparent white.
     * @param b PBounds to hightlight
     */
    private void highlightArea(PBounds b) {
        if (b == null) {
            if (highlightingLayer.getChildrenCount() > 0) {
            }
            highlightingLayer.animateToTransparency(0, animationDuration);
            highlightingLayer.removeAllChildren();
        } else {
            highlightingLayer.removeAllChildren();
            highlightingLayer.setTransparency(1);
            PPath rectangle = new PPath();
            rectangle.setPaint(new Color(255, 255, 255, 100));
            rectangle.setStroke(null);
//            rectangle.setStroke(new BasicStroke((float)(1/ getCamera().getViewScale())));
//            rectangle.setStrokePaint(new Color(255,255,255,20));
            rectangle.setPathTo(this.getCamera().getViewBounds());
            highlightingLayer.addChild(rectangle);
            rectangle.animateToBounds(b.x, b.y, b.width, b.height, this.animationDuration);
        }
    }

    /**
     * Paints a crosshair at the delivered coordinate. Calculates a Point from the 
     * coordinate and calls crossHairPoint(Point p) internally.
     * @param c coordinate of the crosshair's venue
     */
    public void crossHairPoint(Coordinate c) {
        Point p = null;
        if (c != null) {
            p = new Point((int) wtst.getScreenX(c.x), (int) wtst.getScreenY(c.y));
        }
        crossHairPoint(p);
    }

    /**
     * Paints a crosshair at the delivered point.
     * @param p point of the crosshair's venue
     */
    public void crossHairPoint(Point p) {
        if (p == null) {
            if (crosshairLayer.getChildrenCount() > 0) {
                crosshairLayer.removeAllChildren();
            }
        } else {
            crosshairLayer.removeAllChildren();
            crosshairLayer.setTransparency(1);
            PPath lineX = new PPath();
            PPath lineY = new PPath();
            lineX.setStroke(new FixedWidthStroke());
            lineX.setStrokePaint(new Color(100, 100, 100, 255));
            lineY.setStroke(new FixedWidthStroke());
            lineY.setStrokePaint(new Color(100, 100, 100, 255));

            //PBounds current=getCurrentBoundingBox().getPBounds(getWtst());
            PBounds current = getCamera().getViewBounds();
            PBounds x = new PBounds(PBounds.OUT_LEFT - current.width, p.y, 2 * current.width, 1);
            PBounds y = new PBounds(p.x, PBounds.OUT_TOP - current.height, 1, current.height * 2);
            lineX.setPathTo(x);
            crosshairLayer.addChild(lineX);
            lineY.setPathTo(y);
            crosshairLayer.addChild(lineY);

        }
    }

    public Element getConfiguration() {
        Element ret = new Element("cismapMappingPreferences");
        ret.setAttribute("interactionMode", getInteractionMode());
        ret.setAttribute("creationMode", ((CreateGeometryListener) getInputListener(MappingComponent.NEW_POLYGON)).getMode());
        ret.setAttribute("handleInteractionMode", getHandleInteractionMode());
        ret.setAttribute("snapping", new Boolean(isSnappingEnabled()).toString());

        //Position
        Element currentPosition = new Element("Position");
//        if (Double.isNaN(getCurrentBoundingBox().getX1())||
//                Double.isNaN(getCurrentBoundingBox().getX2())||
//                Double.isNaN(getCurrentBoundingBox().getY1())||
//                Double.isNaN(getCurrentBoundingBox().getY2()))        {
//            log.warn("BUGFINDER:Es war ein Wert in der BoundingBox NaN. Setze auf HOME");
//            gotoInitialBoundingBox();
//        }
        currentPosition.addContent(currentBoundingBox.getJDOMElement());
        //currentPosition.addContent(getCurrentBoundingBox().getJDOMElement());
        ret.addContent(currentPosition);
        if (printingSettingsDialog != null) {
            ret.addContent(printingSettingsDialog.getConfiguration());
        }
        return ret;
    }

    public void masterConfigure(Element e) {
        Element prefs = e.getChild("cismapMappingPreferences");
        // HOME
        try {
            if (getMappingModel() instanceof ActiveLayerModel) {
                ActiveLayerModel alm = (ActiveLayerModel) getMappingModel();
                Iterator<Element> it = prefs.getChildren("home").iterator();
                log.debug("Es gibt " + prefs.getChildren("home").size() + " Home Einstellungen");
                while (it.hasNext()) {
                    Element elem = it.next();
                    String srs = elem.getAttribute("srs").getValue();
                    boolean metric = false;
                    try {
                        metric = elem.getAttribute("metric").getBooleanValue();
                    } catch (DataConversionException dce) {
                        log.warn("Metric hat falschen Syntax", dce);
                    }
                    boolean defaultVal = false;
                    try {
                        defaultVal = elem.getAttribute("default").getBooleanValue();
                    } catch (DataConversionException dce) {
                        log.warn("default hat falschen Syntax", dce);
                    }
                    XBoundingBox xbox = new XBoundingBox(elem, srs, metric);

                    alm.addHome(xbox);
                    if (defaultVal) {
                        alm.setSrs(srs);
                        wtst = null;
                        getWtst();
                    }
                }
            }
        } catch (Throwable t) {
            log.error("Fehler beim MasterConfigure der MappingComponent", t);
        }

        try {
            List scalesList = prefs.getChild("printing").getChildren("scale");//NOI18N
            scales.removeAllElements();

            for (Object elem : scalesList) {
                if (elem instanceof Element) {
                    Scale s = new Scale((Element) elem);
                    scales.add(s);
                }
            }
        } catch (Exception skip) {
            log.error("Fehler beim Lesen von Scale", skip);
        }

        // Und jetzt noch die PriningEinstellungen
        initPrintingDialogs();
        printingSettingsDialog.masterConfigure(prefs);
    }

    /**
     * Configurates this MappingComponent
     * @param e JDOM-Element with configuration
     */
    public void configure(Element e) {
        Element prefs = e.getChild("cismapMappingPreferences");
        // InteractionMode
        try {
            String interactMode = prefs.getAttribute("interactionMode").getValue();
            setInteractionMode(interactMode);
            if (interactMode.equals(MappingComponent.NEW_POLYGON)) {
                try {
                    String creationMode = prefs.getAttribute("creationMode").getValue();
                    ((CreateGeometryListener) getInputListener(MappingComponent.NEW_POLYGON)).setMode(creationMode);
                } catch (Throwable t) {
                    log.warn("Fehler beim Setzen des CreationInteractionMode", t);
                }
            }
        } catch (Throwable t) {
            log.warn("Fehler beim Setzen des InteractionMode", t);
        }
        try {
            String handleInterMode = prefs.getAttribute("handleInteractionMode").getValue();
            setHandleInteractionMode(handleInterMode);
        } catch (Throwable t) {
            log.warn("Fehler beim Setzen des HandleInteractionMode", t);
        }
        try {
            boolean snapping = prefs.getAttribute("snapping").getBooleanValue();
            log.info("snapping=" + snapping);

            setSnappingEnabled(snapping);
            setVisualizeSnappingEnabled(snapping);
            setInGlueIdenticalPointsMode(snapping);

        } catch (Throwable t) {
            log.warn("Fehler beim setzen von snapping und Konsorten", t);
        }

        // aktuelle Position
        try {
            Element pos = prefs.getChild("Position");
            BoundingBox b = new BoundingBox(pos);
            log.debug("Position:" + b);
            PBounds pb = b.getPBounds(getWtst());
            log.debug("PositionPb:" + pb);
            if (Double.isNaN(b.getX1()) ||
                    Double.isNaN(b.getX2()) ||
                    Double.isNaN(b.getY1()) ||
                    Double.isNaN(b.getY2())) {
                log.fatal("BUGFINDER:Es war ein Wert in der BoundingBox NaN. Setze auf HOME");
//                gotoInitialBoundingBox();
                this.currentBoundingBox = getMappingModel().getInitialBoundingBox();
                addToHistory(new PBoundsWithCleverToString(new PBounds(currentBoundingBox.getPBounds(wtst)), wtst));

            } else {
                this.currentBoundingBox = b;
                log.debug("added to History" + b);
                addToHistory(new PBoundsWithCleverToString(new PBounds(b.getPBounds(wtst)), wtst));
//                final BoundingBox bb=b;
//                EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        gotoBoundingBoxWithHistory(bb);
//                    }
//                });
            }
        } catch (Throwable t) {
            log.error("Fehler beim lesen der aktuellen Position", t);
//            EventQueue.invokeLater(new Runnable() {
//                public void run() {
//                    gotoBoundingBoxWithHistory(getMappingModel().getInitialBoundingBox());
            this.currentBoundingBox = getMappingModel().getInitialBoundingBox();
//                }
//            });
        }
        if (printingSettingsDialog != null) {
            printingSettingsDialog.configure(prefs);
        }
    }

    /**
     * Zooms to all features of the mappingcomponents featurecollection. If fixedScale is
     * true, the mappingcomponent will only pan to the featurecollection and not zoom.
     * @param fixedScale true, if zoom is not allowed
     */
    public void zoomToFeatureCollection(boolean fixedScale) {
        zoomToAFeatureCollection(featureCollection.getAllFeatures(), true, fixedScale);
    }

    /**
     * Zooms to all features of the mappingcomponents featurecollection. 
     */
    public void zoomToFeatureCollection() {
        log.debug("zoomToFeatureCollection");
        zoomToAFeatureCollection(featureCollection.getAllFeatures(), true, false);
    }

    /**
     * Moves the view to the target Boundingbox.
     * @param bb target BoundingBox
     * @param history true, if the action sould be saved in the history
     * @param scaleToFit true, to zoom
     * @param animationDuration duration of the animation
     */
    public void gotoBoundingBox(BoundingBox bb, boolean history, boolean scaleToFit, int animationDuration) {
        gotoBoundingBox(bb, history, scaleToFit, animationDuration, true);
    }

    /**
     * Moves the view to the target Boundingbox.
     * @param bb target BoundingBox
     * @param history true, if the action sould be saved in the history
     * @param scaleToFit true, to zoom
     * @param animationDuration duration of the animation
     * @param queryServices true, if the services should be refreshed after animation
     */
    public void gotoBoundingBox(BoundingBox bb, final boolean history, final boolean scaleToFit, int animationDuration, final boolean queryServices) {
        if (bb != null) {
            log.debug("gotoBoundingBox:" + bb, new CurrentStackTrace());
            handleLayer.removeAllChildren();
            final double x1 = getWtst().getScreenX(bb.getX1());
            final double y1 = getWtst().getScreenY(bb.getY1());
            final double x2 = getWtst().getScreenX(bb.getX2());
            final double y2 = getWtst().getScreenY(bb.getY2());
            final double w;
            final double h;

            final Rectangle2D pos = new Rectangle2D.Double();
            pos.setRect(x1, y2, x2 - x1, y1 - y2);
            getCamera().animateViewToCenterBounds(pos, x1 != x2 && y1 != y2 && scaleToFit, animationDuration);
            if (getCamera().getViewTransform().getScaleY() < 0) {
                log.fatal("gotoBoundingBox: Problem :-( mit getViewTransform");
            }
            showHandles(true);
            Runnable handle = new Runnable() {

                @Override
                public void run() {
                    while (getAnimating()) {
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {
                            log.warn("Unterbrechung bei getAnimating()", e);
                        }
                    }
                    if (history) {
                        if (x1 == x2 || y1 == y2 || !scaleToFit) {
                            setNewViewBounds(getCamera().getViewBounds());
                        } else {
                            setNewViewBounds(pos);
                        }
                        if (queryServices) {
                            queryServices();
                        }
                    } else {
                        if (queryServices) {
                            queryServicesWithoutHistory();
                        }
                    }

                }
            };
            CismetThreadPool.execute(handle);
        } else {
            log.warn("Seltsam: die BoundingBox war null", new CurrentStackTrace());
        }
    }

    /**
     * Moves the view to the target Boundingbox without saving the action in the history.
     * @param bb target BoundingBox
     */
    public void gotoBoundingBoxWithoutHistory(BoundingBox bb) {
        gotoBoundingBox(bb, false, true, animationDuration);
    }

    /**
     * Moves the view to the target Boundingbox and saves the action in the history.
     * @param bb target BoundingBox
     */
    public void gotoBoundingBoxWithHistory(BoundingBox bb) {
        gotoBoundingBox(bb, true, true, animationDuration);
    }

    /**
     * Returns a BoundingBox of the current view in another scale.
     * @param scaleDenominator specific target scale
     */
    public BoundingBox getBoundingBoxFromScale(double scaleDenominator) {
        return getScaledBoundingBox(scaleDenominator, getCurrentBoundingBox());
    }

    /**
     * Returns the BoundingBox of the delivered BoundingBox in another scale.
     * @param scaleDenominator specific target scale
     * @param bb source BoundingBox
     */
    public BoundingBox getScaledBoundingBox(double scaleDenominator, BoundingBox bb) {
        double screenWidthInInch = getWidth() / screenResolution;
        double screenWidthInMeter = screenWidthInInch * 0.0254;
        double screenHeightInInch = getHeight() / screenResolution;
        double screenHeightInMeter = screenHeightInInch * 0.0254;

        double realWorldWidthInMeter = screenWidthInMeter * scaleDenominator;
        double realWorldHeightInMeter = screenHeightInMeter * scaleDenominator;
        double midX = bb.getX1() + (bb.getX2() - bb.getX1()) / 2;
        double midY = bb.getY1() + (bb.getY2() - bb.getY1()) / 2;
        return new BoundingBox(midX - realWorldWidthInMeter / 2, midY - realWorldHeightInMeter / 2, midX + realWorldWidthInMeter / 2, midY + realWorldHeightInMeter / 2);
    }

    /**
     * Calculate the current scaledenominator.
     */
    public double getScaleDenominator() {
        double screenWidthInInch = getWidth() / screenResolution;
        double screenWidthInMeter = screenWidthInInch * 0.0254;
        double screenHeightInInch = getHeight() / screenResolution;
        double screenHeightInMeter = screenHeightInInch * 0.0254;

        double realWorldWidthInMeter = getCurrentBoundingBox().getWidth();
        double realWorldHeightInMeter = getCurrentBoundingBox().getHeight();

        return realWorldWidthInMeter / screenWidthInMeter;
    }

//    public static Image getFeatureImage(Feature f){
//        MappingComponent mc=new MappingComponent();
//        mc.setSize(100,100);
//        pc.setInfoNodesVisible(true);
//            pc.setSize(100,100);
//            PFeature pf=new PFeature(pnf,new WorldToScreenTransform(0,0),0,0,null);
//            pc.getLayer().addChild(pf);
//            pc.getCamera().animateViewToCenterBounds(pf.getBounds(),true,0);
//            i=new ImageIcon(pc.getCamera().toImage(100,100,getBackground()));
//    }
    /**
     * Called when the drag operation has terminated with a drop on
     * the operable part of the drop site for the <code>DropTarget</code>
     * registered with this listener.
     * <p>
     * This method is responsible for undertaking
     * the transfer of the data associated with the
     * gesture. The <code>DropTargetDropEvent</code>
     * provides a means to obtain a <code>Transferable</code>
     * object that represents the data object(s) to
     * be transfered.<P>
     * From this method, the <code>DropTargetListener</code>
     * shall accept or reject the drop via the
     * acceptDrop(int dropAction) or rejectDrop() methods of the
     * <code>DropTargetDropEvent</code> parameter.
     * <P>
     * Subsequent to acceptDrop(), but not before,
     * <code>DropTargetDropEvent</code>'s getTransferable()
     * method may be invoked, and data transfer may be
     * performed via the returned <code>Transferable</code>'s
     * getTransferData() method.
     * <P>
     * At the completion of a drop, an implementation
     * of this method is required to signal the success/failure
     * of the drop by passing an appropriate
     * <code>boolean</code> to the <code>DropTargetDropEvent</code>'s
     * dropComplete(boolean success) method.
     * <P>
     * Note: The data transfer should be completed before the call  to the
     * <code>DropTargetDropEvent</code>'s dropComplete(boolean success) method.
     * After that, a call to the getTransferData() method of the
     * <code>Transferable</code> returned by
     * <code>DropTargetDropEvent.getTransferable()</code> is guaranteed to
     * succeed only if the data transfer is local; that is, only if
     * <code>DropTargetDropEvent.isLocalTransfer()</code> returns
     * <code>true</code>. Otherwise, the behavior of the call is
     * implementation-dependent.
     * <P>
     *
     * @param dtde the <code>DropTargetDropEvent</code>
     */
    public void drop(DropTargetDropEvent dtde) {
        MapDnDEvent mde = new MapDnDEvent();
        mde.setDte(dtde);
        Point p = dtde.getLocation();
        mde.setXPos(getWtst().getWorldX(p.getX()));
        mde.setYPos(getWtst().getWorldY(p.getY()));
        CismapBroker.getInstance().fireDropOnMap(mde);
    }

    /**
     * Called while a drag operation is ongoing, when the mouse pointer has
     * exited the operable part of the drop site for the
     * <code>DropTarget</code> registered with this listener.
     *
     * @param dte the <code>DropTargetEvent</code>
     */
    public void dragExit(DropTargetEvent dte) {
    }

    /**
     * Called if the user has modified
     * the current drop gesture.
     *
     * @param dtde the <code>DropTargetDragEvent</code>
     */
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    /**
     * Called when a drag operation is ongoing, while the mouse pointer is still
     * over the operable part of the drop site for the <code>DropTarget</code>
     * registered with this listener.
     *
     * @param dtde the <code>DropTargetDragEvent</code>
     */
    public void dragOver(DropTargetDragEvent dtde) {
        MapDnDEvent mde = new MapDnDEvent();
        mde.setDte(dtde);
        Point p = dtde.getLocation();
        mde.setXPos(getWtst().getWorldX(p.getX()));
        mde.setYPos(getWtst().getWorldY(p.getY()));
        CismapBroker.getInstance().fireDragOverMap(mde);
    }

    /**
     * Called while a drag operation is ongoing, when the mouse pointer enters
     * the operable part of the drop site for the <code>DropTarget</code>
     * registered with this listener.
     *
     * @param dtde the <code>DropTargetDragEvent</code>
     */
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    /**
     * Returns the PfeatureHashmap which assigns a Feature to a PFeature.
     */
    public ConcurrentHashMap getPFeatureHM() {
        return pFeatureHM;
    }

    public boolean isFixedMapExtent() {
        return fixedMapExtent;
    }

    public void setFixedMapExtent(boolean fixedMapExtent) {
        this.fixedMapExtent = fixedMapExtent;
    }

    public boolean isFixedMapScale() {
        return fixedMapScale;
    }

    public void setFixedMapScale(boolean fixedMapScale) {
        this.fixedMapScale = fixedMapScale;
    }

    /**
     *@deprecated
     */
    public void selectPFeatureManually(PFeature one) {
        //throw new UnsupportedOperationException("Not yet implemented");
        if (one != null) {
            featureCollection.select(one.getFeature());
        }
    }

    /**
     *@deprecated
     */
    public PFeature getSelectedNode() {
        //gehe mal davon aus dass das nur aufgerufen wird wenn sowieso nur ein node selected ist
        //deshalb gebe ich mal nur das erste zur�ck
        if (featureCollection.getSelectedFeatures().size() > 0) {
            Feature selF = (Feature) featureCollection.getSelectedFeatures().toArray()[0];
            if (selF == null) {
                return null;
            }
            return (PFeature) pFeatureHM.get(selF);
        } else {
            return null;
        }
    }

    public boolean isInfoNodesVisible() {
        return infoNodesVisible;
    }

    public PLayer getPrintingFrameLayer() {
        return printingFrameLayer;
    }

    public PrintingSettingsWidget getPrintingSettingsDialog() {
        return printingSettingsDialog;
    }

    public boolean isInGlueIdenticalPointsMode() {
        return inGlueIdenticalPointsMode;
    }

    public void setInGlueIdenticalPointsMode(boolean inGlueIdenticalPointsMode) {
        this.inGlueIdenticalPointsMode = inGlueIdenticalPointsMode;
    }

    public PLayer getHighlightingLayer() {
        return highlightingLayer;
    }

    public void setPointerAnnotation(PNode anno) {
        ((SimpleMoveListener) getInputListener(MOTION)).setPointerAnnotation(anno);

    }

    public void setPointerAnnotationVisibility(boolean visib) {
        if (getInputListener(MOTION) != null) {
            ((SimpleMoveListener) getInputListener(MOTION)).setAnnotationNodeVisible(visib);
        }
    }

    /**
     * Returns a boolean whether the annotationnode is visible or not. Returns false if
     * the interactionmode doesn't equal MOTION.
     */
    public boolean isPointerAnnotationVisible() {
        if (getInputListener(MOTION) != null) {
            return ((SimpleMoveListener) getInputListener(MOTION)).isAnnotationNodeVisible();
        } else {
            return false;
        }
    }

    /**
     * Returns a vector with different scales.
     */
    public Vector<Scale> getScales() {
        return scales;
    }

    /**
     * Locks the MappingComponent.
     */
    public void lock() {
        locked = true;
    }

    /**
     * Unlocks the MappingComponent.
     */
    public void unlock() {
        log.debug("unlock");
        locked = false;
        log.debug("currentBoundingBox:" + currentBoundingBox);
        gotoBoundingBoxWithHistory(currentBoundingBox);
    }

    /**
     * Returns whether the MappingComponent is locked or not.
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Returns the MementoInterface for redo-actions.
     */
    public MementoInterface getMemRedo() {
        return memRedo;
    }

    /**
     * Returns the MementoInterface for undo-actions.
     */
    public MementoInterface getMemUndo() {
        return memUndo;
    }

    public HashMap<String, PBasicInputEventHandler> getInputEventListener() {
        return inputEventListener;
    }

    public void setInputEventListener(HashMap<String, PBasicInputEventHandler> inputEventListener) {
        this.inputEventListener = inputEventListener;
    }

    /////////////////////////////////////////////////
    // CLASS MappingComponentRasterServiceListener //
    /////////////////////////////////////////////////
    private class MappingComponentRasterServiceListener implements RetrievalListener {

        private int position = -1;
        private XPImage pi = null;
        private ServiceLayer rasterService = null;

        public MappingComponentRasterServiceListener(int position, PNode pn, ServiceLayer rasterService) {
            this.position = position;
            if (pn instanceof XPImage) {
                this.pi = (XPImage) pn;
            }
            this.rasterService = rasterService;
        }

        public void retrievalStarted(RetrievalEvent e) {
            fireActivityChanged();
            log.debug("TaskCounter:" + taskCounter);
        }

        public void retrievalProgress(RetrievalEvent e) {
        }

        public void retrievalError(RetrievalEvent e) {
            log.error("Fehler beim Laden des Bildes!");
            fireActivityChanged();
            log.debug("TaskCounter:" + taskCounter);
        }

        public void retrievalComplete(RetrievalEvent e) {
            final Object o = e.getRetrievedObject();
            fireActivityChanged();
            log.debug("TaskCounter:" + taskCounter);

            if (o instanceof Image && e.isHasErrors() == false) {
                // TODO Hier ist noch ein Fehler die Sichtbarkeit muss vom Layer erfragt werden
                if (isBackgroundEnabled()) {
                    //Image i=Static2DTools.toCompatibleImage((Image)o);
                    Image i = (Image) o;
                    if (rasterService.getName().startsWith("prefetching")) {
                        pi.setImage(i, 0);
                        pi.setScale(3 / getCamera().getViewScale());
                        pi.setOffset(getCamera().getViewBounds().getOrigin().getX() - getCamera().getViewBounds().getWidth(),
                                getCamera().getViewBounds().getOrigin().getY() - getCamera().getViewBounds().getHeight());
                    } else {
                        pi.setImage(i, 1000);
                        pi.setScale(1 / getCamera().getViewScale());
                        pi.setOffset(getCamera().getViewBounds().getOrigin());
                        MappingComponent.this.repaint();
                    }
                }
            }
        }

        public void retrievalAborted(RetrievalEvent e) {
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }

    //////////////////////////////////////////////////
    // CLASS MappingComponentFeatureServiceListener //
    //////////////////////////////////////////////////
    private class MappingComponentFeatureServiceListener implements RetrievalListener {

        ServiceLayer featureService;
        PLayer parent;
        long requestIdentifier;
        Thread completionThread = null;
        private Vector deletionCandidates = new Vector();
        private Vector twins = new Vector();

        /**
         * Creates a new MappingComponentFeatureServiceListener.
         * @param featureService the featureretrievalservice
         * @param parent the featurelayer (PNode) connected with the servicelayer
         */
        public MappingComponentFeatureServiceListener(ServiceLayer featureService, PLayer parent) {
            this.featureService = featureService;
            this.parent = parent;
        }

        public void retrievalStarted(RetrievalEvent e) {
            requestIdentifier = e.getRequestIdentifier();
            fireActivityChanged();
        }

        public void retrievalProgress(RetrievalEvent e) {
            //TODO Hier besteht auch die Möglichkeit jedes einzelne Polygon hinzuzufügen. ausprobieren, ob das flüssiger ist
        }

        public void retrievalError(RetrievalEvent e) {
            fireActivityChanged();
            log.error("Fehler im FeatureService");
        }

        public void retrievalComplete(final RetrievalEvent e) {
            log.debug("retrievalComplete");
            final Vector newFeatures = new Vector();
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    ((RetrievalServiceLayer) featureService).setProgress(0);
                    parent.setVisible(isBackgroundEnabled() && (featureService).isEnabled() && parent.getVisible());
//                    parent.removeAllChildren();
                }
            });
            // clear all old data to delete twins
            deletionCandidates.removeAllElements();
            twins.removeAllElements();

            // if it's a refresh, add all old features which should be deleted in the
            // newly acquired featurecollection
            if (!e.isRefreshExisting()) {
                deletionCandidates.addAll(parent.getChildrenReference());
            }
            log.debug("deletionCandidates (" + deletionCandidates.size() + "):" + deletionCandidates);

            // only start parsing the features if there are no errors and a correct collection
            if (e.isHasErrors() == false && e.getRetrievedObject() instanceof Collection) {
                completionThread = new Thread() {

                    @Override
                    public void run() {
                        // this is the collection with the retrieved features
                        Collection c = ((Collection) e.getRetrievedObject());
                        final int size = c.size();
                        int counter = 0;
                        Iterator it = c.iterator();
                        log.debug("Anzahl Features: " + size);

                        while (it.hasNext() && requestIdentifier == e.getRequestIdentifier() && !isInterrupted()) {
                            counter++;
                            Object o = it.next();
                            if (o instanceof Feature) {
                                final PFeature p = new PFeature(((Feature) o), wtst, clip_offset_x, clip_offset_y, MappingComponent.this);
                                PFeature twin = null;
                                for (Object tester : deletionCandidates) {
                                    // if tester and PFeature are FeatureWithId-objects
                                    if (((PFeature) tester).getFeature() instanceof FeatureWithId && p.getFeature() instanceof FeatureWithId) {
                                        int id1 = ((FeatureWithId) ((PFeature) tester).getFeature()).getId();
                                        int id2 = ((FeatureWithId) (p.getFeature())).getId();
                                        if (id1 != -1 && id2 != -1) { // check if they've got the same id
                                            if (id1 == id2) {
                                                twin = ((PFeature) tester);
                                                break;
                                            }
                                        } else { // else test the geometry for equality
                                            if (((PFeature) tester).getFeature().getGeometry().equals(p.getFeature().getGeometry())) {
                                                twin = ((PFeature) tester);
                                                break;
                                            }
                                        }
                                    } else { // no FeatureWithId, test geometries for equality
                                        if (((PFeature) tester).getFeature().getGeometry().equals(p.getFeature().getGeometry())) {
                                            twin = ((PFeature) tester);
                                            break;
                                        }
                                    }
                                }

                                // if a twin is found remove him from the deletion candidates
                                // and add him to the twins
                                if (twin != null) {
                                    deletionCandidates.remove(twin);
                                    twins.add(twin);
                                } else { // else add the PFeature to the new features
                                    newFeatures.add(p);
                                }

                                // calculate the advance of the progressbar
                                int prog = (int) (((double) counter / (double) size) * 100d);
                                ((RetrievalServiceLayer) featureService).setProgress(prog);
                                fireActivityChanged();
                            }
                        }

                        if (requestIdentifier == e.getRequestIdentifier() && !isInterrupted()) {
                            // after all features are computed do stuff on the EDT
                            EventQueue.invokeLater(new Runnable() {

                                public void run() {
                                    try {
                                        log.debug("MappingComponentFeaturelistener.retrievalComplete()");

                                        // if it's a refresh, delete all previous features
                                        if (e.isRefreshExisting()) {
                                            parent.removeAllChildren();
                                        }
                                        Vector deleteFeatures = new Vector();
                                        for (Object o : newFeatures) {
                                            parent.addChild((PNode) o);
                                        }
//                                    for (Object o : twins) { // TODO only nesseccary if style has changed
//                                        ((PFeature) o).refreshDesign();
//                                        log.debug("twin refresh");
//                                    }

                                        // set the prograssbar to full
                                        ((RetrievalServiceLayer) featureService).setProgress(100);
                                        fireActivityChanged();

                                        // repaint the featurelayer
                                        parent.repaint();

                                        // remove stickyNode from all deletionCandidates and add
                                        // each to the new deletefeature-collection
                                        for (Object o : deletionCandidates) {
                                            if (o instanceof PFeature) {
                                                PNode p = ((PFeature) o).getPrimaryAnnotationNode();
                                                if (p != null) {
                                                    removeStickyNode(p);
                                                }
                                                deleteFeatures.add(o);
                                            }
                                        }
                                        log.debug("parentCount before:" + parent.getChildrenCount());
                                        log.debug("deleteFeatures=" + deleteFeatures.size() + " :" + deleteFeatures);

                                        // delete the features marked for deletion
                                        parent.removeChildren(deleteFeatures);
                                        log.debug("parentCount after:" + parent.getChildrenCount());
                                        rescaleStickyNodes();

                                    } catch (Exception exception) {
                                        log.warn("Fehler beim Aufr\u00E4umen", exception);
                                    }
                                }
                            });
                        }
                        else {
                            log.info("parallel call");
                        }
                    }
                };
                completionThread.setPriority(Thread.NORM_PRIORITY);
                if (requestIdentifier == e.getRequestIdentifier()){
                    CismetThreadPool.execute(completionThread);
                }
            }
            fireActivityChanged();
        }

        public void retrievalAborted(RetrievalEvent e) {
            if (completionThread != null) {
                completionThread.interrupt();
            }
            fireActivityChanged();
            log.debug("TaskCounter:" + taskCounter);
        }
    }
}

class ImageSelection implements Transferable {

    private Image image;

    public ImageSelection(Image image) {
        this.image = image;
    }

    // Returns supported flavors
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    // Returns true if flavor is supported
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return DataFlavor.imageFlavor.equals(flavor);
    }

    // Returns image
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!DataFlavor.imageFlavor.equals(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return image;
    }
}
