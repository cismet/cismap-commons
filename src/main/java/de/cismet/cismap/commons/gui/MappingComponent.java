/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.PRoot;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

import org.apache.log4j.Logger;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

import pswing.PSwingCanvas;

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
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.Debug;
import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.MappingModelListener;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.Bufferable;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureGroup;
import de.cismet.cismap.commons.features.FeatureGroups;
import de.cismet.cismap.commons.features.FeatureWithId;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.RasterLayerSupportedFeature;
import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.features.StyledFeature;
import de.cismet.cismap.commons.featureservice.DocumentFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;
import de.cismet.cismap.commons.gui.piccolo.PBoundsWithCleverToString;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.PNodeFactory;
import de.cismet.cismap.commons.gui.piccolo.PSticky;
import de.cismet.cismap.commons.gui.piccolo.XPImage;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.AttachFeatureListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateNewGeometryListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateSearchGeometryListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CustomFeatureActionListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CustomFeatureInfoListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.DeleteFeatureListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.FeatureMoveListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.GetFeatureInfoClickDetectionListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.JoinPolygonsListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.KeyboardListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MeasurementListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MeasurementMoveListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.OverviewModeListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.PanAndMousewheelZoomListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.PrintingFrameListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.RaisePolygonListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.RubberBandZoomListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SplitPolygonListener;
import de.cismet.cismap.commons.gui.printing.PrintingSettingsWidget;
import de.cismet.cismap.commons.gui.printing.PrintingWidget;
import de.cismet.cismap.commons.gui.printing.Scale;
import de.cismet.cismap.commons.gui.progresswidgets.DocumentProgressWidget;
import de.cismet.cismap.commons.gui.simplelayerwidget.LayerControl;
import de.cismet.cismap.commons.gui.simplelayerwidget.NewSimpleInternalLayerWidget;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.CrsChangeListener;
import de.cismet.cismap.commons.interaction.events.CrsChangedEvent;
import de.cismet.cismap.commons.interaction.events.MapDnDEvent;
import de.cismet.cismap.commons.interaction.events.StatusEvent;
import de.cismet.cismap.commons.interaction.memento.Memento;
import de.cismet.cismap.commons.interaction.memento.MementoInterface;
import de.cismet.cismap.commons.preferences.CismapPreferences;
import de.cismet.cismap.commons.preferences.GlobalPreferences;
import de.cismet.cismap.commons.preferences.LayersPreferences;
import de.cismet.cismap.commons.rasterservice.FeatureAwareRasterService;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.rasterservice.RasterMapService;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

import de.cismet.tools.CismetThreadPool;
import de.cismet.tools.CurrentStackTrace;
import de.cismet.tools.StaticDebuggingTools;

import de.cismet.tools.collections.TypeSafeCollections;

import de.cismet.tools.configuration.Configurable;

import de.cismet.tools.gui.historybutton.DefaultHistoryModel;
import de.cismet.tools.gui.historybutton.HistoryModel;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class MappingComponent extends PSwingCanvas implements MappingModelListener,
    FeatureCollectionListener,
    HistoryModel,
    Configurable,
    DropTargetListener,
    CrsChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    /** Wenn false, werden alle debug statements vom compiler wegoptimiert. */
    private static final boolean DEBUG = Debug.DEBUG;
    public static final String PROPERTY_MAP_INTERACTION_MODE = "INTERACTION_MODE";          // NOI18N
    public static final String MOTION = "MOTION";                                           // NOI18N
    public static final String SELECT = "SELECT";                                           // NOI18N
    public static final String ZOOM = "ZOOM";                                               // NOI18N
    public static final String PAN = "PAN";                                                 // NOI18N
    public static final String ALKIS_PRINT = "ALKIS_PRINT";                                 // NOI18N
    public static final String FEATURE_INFO = "FEATURE_INFO";                               // NOI18N
    public static final String CREATE_SEARCH_POLYGON = "SEARCH_POLYGON";                    // NOI18N
    public static final String MOVE_POLYGON = "MOVE_POLYGON";                               // NOI18N
    public static final String REMOVE_POLYGON = "REMOVE_POLYGON";                           // NOI18N
    public static final String NEW_POLYGON = "NEW_POLYGON";                                 // NOI18N
    public static final String SPLIT_POLYGON = "SPLIT_POLYGON";                             // NOI18N
    public static final String JOIN_POLYGONS = "JOIN_POLYGONS";                             // NOI18N
    public static final String RAISE_POLYGON = "RAISE_POLYGON";                             // NOI18N
    public static final String ROTATE_POLYGON = "ROTATE_POLYGON";                           // NOI18N
    public static final String ATTACH_POLYGON_TO_ALPHADATA = "ATTACH_POLYGON_TO_ALPHADATA"; // NOI18N
    public static final String MOVE_HANDLE = "MOVE_HANDLE";                                 // NOI18N
    public static final String REMOVE_HANDLE = "REMOVE_HANDLE";                             // NOI18N
    public static final String ADD_HANDLE = "ADD_HANDLE";                                   // NOI18N
    public static final String MEASUREMENT = "MEASUREMENT";                                 // NOI18N
    public static final String LINEMEASUREMENT = "LINEMEASUREMENT";                         // NOI18N
    public static final String PRINTING_AREA_SELECTION = "PRINTING_AREA_SELECTION";         // NOI18N
    public static final String CUSTOM_FEATUREACTION = "CUSTOM_FEATUREACTION";               // NOI18N
    public static final String CUSTOM_FEATUREINFO = "CUSTOM_FEATUREINFO";                   // NOI18N
    public static final String OVERVIEW = "OVERVIEW";                                       // NOI18N
    private static MappingComponent THIS;
    /** Name of the internal Simple Layer Widget. */
    public static final String LAYERWIDGET = "SimpleInternalLayerWidget"; // NOI18N
    /** Name of the internal Document Progress Widget. */
    public static final String PROGRESSWIDGET = "DocumentProgressWidget"; // NOI18N
    /** Internat Widget at position north west. */
    public static final int POSITION_NORTHWEST = 1;
    /** Internat Widget at position south west. */
    public static final int POSITION_SOUTHWEST = 2;
    /** Internat Widget at position north east. */
    public static final int POSITION_NORTHEAST = 4;
    /** Internat Widget at position south east. */
    public static final int POSITION_SOUTHEAST = 8;
    /** Delay after a compoent resize event triggers a service reload request. */
    private static final int RESIZE_DELAY = 500;
    /** If a document exceeds the criticalDocumentSize, the document progress widget is displayed. */
    private static final long criticalDocumentSize = 10000000;                                    // 10MB

    //~ Instance fields --------------------------------------------------------

    // private
    // NewSimpleInternalLayerWidget
    // internalLayerWidget
    // = null;//new
    // NewSimpleInternalLayerWidget(this);
    // 10MB
    // private NewSimpleInternalLayerWidget internalLayerWidget = null;//new NewSimpleInternalLayerWidget(this);
    boolean featureServiceLayerVisible = true;
    final List<LayerControl> layerControls = new ArrayList<LayerControl>();
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private boolean gridEnabled = true;
//    private Feature[] currentlyShownFeatures = null;
//    private com.vividsolutions.jts.geom.Envelope currentFeatureEnvelope = null;
    private MappingModel mappingModel;
    private ConcurrentHashMap<Feature, PFeature> pFeatureHM = TypeSafeCollections.newConcurrentHashMap();
//    private MultiMap pFeatureHMbyCoordinate = new MultiMap();
    // Attribute die zum selektieren von PNodes gebraucht werden
    // private PFeature selectedFeature=null;
//    private Paint paint = null;
    private WorldToScreenTransform wtst = null;
    private double clip_offset_x;
    private double clip_offset_y;
    private double printingResolution = 0d;
//    double viewScale;
//    private PImage imageBackground = new XPImage();
//    private SimpleWmsGetMapUrl wmsBackgroundUrl;
    private boolean backgroundEnabled = true;
//    private ConcurrentHashMap<String, PLayer> featureLayers = new ConcurrentHashMap<String, PLayer>();
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
    private final Map<String, Cursor> cursors = TypeSafeCollections.newHashMap();
    private final HashMap<String, PBasicInputEventHandler> inputEventListener = TypeSafeCollections.newHashMap();
//    private Action backAction;
//    private Action forwardAction;
//    private Action homeAction;
//    private Action refreshAction;
//    private Action snappingAction;
//    private Action backgroundAction;
    private final Action zoomAction;
//    private Action panAction;
//    private Action selectAction;
    private int acceptableActions = DnDConstants.ACTION_COPY_OR_MOVE;
//    private DragSource dragSource;
//    private DragGestureListener dgListener;
//    private DragSourceListener dsListener;
    private FeatureCollection featureCollection;
    // private boolean internalLayerWidgetAvailable = false;
    private boolean infoNodesVisible = false;
    private boolean fixedMapExtent = false;
    private boolean fixedMapScale = false;
    private boolean inGlueIdenticalPointsMode = true;
    /** Holds value of property interactionMode. */
    private String interactionMode;
    /** Holds value of property handleInteractionMode. */
    private String handleInteractionMode;
    // "Phantom PCanvas" der nie selbst dargestellt wird
    // wird nur dazu benutzt das Graphics Objekt up to date
    // zu halten und dann als Hintergrund von z.B. einem
    // Panel zu fungieren
    // coooooooool, was ? ;-)
    private final PCanvas selectedObjectPresenter = new PCanvas();
    private BoundingBox currentBoundingBox = null;
//    private HashMap rasterServiceImages = new HashMap();
    private Rectangle2D newViewBounds;
    private int animationDuration = 500;
    private int taskCounter = 0;
    private CismapPreferences cismapPrefs;
    private DefaultHistoryModel historyModel = new DefaultHistoryModel();
//    private Set<Feature> holdFeatures = new HashSet<Feature>();
    // Scales
    private final List<Scale> scales = new ArrayList<Scale>();
    // Printing
    private PrintingSettingsWidget printingSettingsDialog;
    private PrintingWidget printingDialog;
    // Scalebar
    private double screenResolution = 100.0;
    private volatile boolean locked = true;
    private final List<PNode> stickyPNodes = new ArrayList<PNode>();
    // Undo- & Redo-Stacks
    private final MementoInterface memUndo = new Memento();
    private final MementoInterface memRedo = new Memento();
    private boolean featureDebugging = false;
    private BoundingBox fixedBoundingBox = null;
//    Object handleFeatureServiceBlocker = new Object();
    private final List<MapListener> mapListeners = new ArrayList<MapListener>();
    /** Contains the internal widgets. */
    private final Map<String, JInternalFrame> internalWidgets = TypeSafeCollections.newHashMap();
    /** Contains the positions of the internal widgets. */
    private final Map<String, Integer> internalWidgetPositions = TypeSafeCollections.newHashMap();
    /** The timer that delays the reload requests. */
    private Timer delayedResizeEventTimer = null;
    private DocumentProgressListener documentProgressListener = null;
    private List<Crs> crsList = new ArrayList<Crs>();
    private CrsTransformer transformer;
    private boolean resetCrs = false;
    private final Timer showHandleDelay;
    private final Map<MapService, Future<?>> serviceFuturesMap = TypeSafeCollections.newHashMap();
    /** Utility field used by bound properties. */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private ButtonGroup interactionButtonGroup;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of MappingComponent.
     */
    public MappingComponent() {
        super();
        locked = true;
        THIS = this;
        // wird in der Regel wieder ueberschrieben
        setSnappingRectSize(20);
        setSnappingEnabled(false);
        setVisualizeSnappingEnabled(false);
        setAnimationDuration(500);
        setInteractionMode(ZOOM);
        showHandleDelay = new Timer(500, new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        showHandles(false);
                    }
                });
        showHandleDelay.setRepeats(false);
        featureDebugging = StaticDebuggingTools.checkHomeForFile("cismetTurnOnFeatureDebugging"); // NOI18N

        setFeatureCollection(new DefaultFeatureCollection());

        addMapListener((DefaultFeatureCollection)getFeatureCollection());
        final DropTarget dt = new DropTarget(this, acceptableActions, this);

//        setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
//        setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        setDefaultRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
        setAnimatingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);

        removeInputEventListener(getPanEventHandler());
        removeInputEventListener(getZoomEventHandler());
        addComponentListener(new ComponentAdapter() {

                @Override
                public void componentResized(final ComponentEvent evt) {
                    if (MappingComponent.this.delayedResizeEventTimer == null) {
                        delayedResizeEventTimer = new Timer(RESIZE_DELAY, new ActionListener() {

                                    @Override
                                    public void actionPerformed(final ActionEvent e) {
                                        delayedResizeEventTimer.stop();
                                        delayedResizeEventTimer = null;

                                        // perform delayed resize:
                                        // rescape map + move widgets + reload services
                                        componentResizedDelayed();
                                    }
                                });
                        delayedResizeEventTimer.start();
                    } else {
                        // perform intermediate resize:
                        // rescape map + move widgets
                        componentResizedIntermediate();
                        delayedResizeEventTimer.restart();
                    }
                }
            });

        final PRoot root = getRoot();

        final PCamera otherCamera = new PCamera();
        otherCamera.addLayer(featureLayer);
        selectedObjectPresenter.setCamera(otherCamera);

        root.addChild(otherCamera);

        getLayer().addChild(mapServicelayer);
        getLayer().addChild(featureServiceLayer);
        getLayer().addChild(featureLayer);
        getLayer().addChild(tmpFeatureLayer);
        // getLayer().addChild(handleLayer);
        getLayer().addChild(rubberBandLayer);
        getLayer().addChild(highlightingLayer);
        getLayer().addChild(crosshairLayer);
        getLayer().addChild(dragPerformanceImproverLayer);
        getLayer().addChild(printingFrameLayer);

        getCamera().addLayer(mapServicelayer);
//        getCamera().addLayer(1, featureServiceLayer);
        getCamera().addLayer(featureLayer);
        getCamera().addLayer(tmpFeatureLayer);
        // getCamera().addLayer(5,snapHandleLayer);
        // getCamera().addLayer(5,handleLayer);
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

        final KeyboardListener k = new KeyboardListener(this);
        addInputEventListener(k);
        getRoot().getDefaultInputManager().setKeyboardFocus(k);
        setInteractionMode(ZOOM);
        setHandleInteractionMode(MOVE_HANDLE);

        dragPerformanceImproverLayer.setVisible(false);
        historyModel.setMaximumPossibilities(30);

        zoomAction = new AbstractAction() {

                {
                    putValue(
                        Action.NAME,
                        org.openide.util.NbBundle.getMessage(
                            MappingComponent.class,
                            "MappingComponent.zoomAction.NAME"));                                                      // NOI18N
                    putValue(
                        Action.SMALL_ICON,
                        new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/layers.png"))); // NOI18N
                    putValue(
                        Action.SHORT_DESCRIPTION,
                        org.openide.util.NbBundle.getMessage(
                            MappingComponent.class,
                            "MappingComponent.zoomAction.SHORT_DESCRIPTION"));                                         // NOI18N
                    putValue(
                        Action.LONG_DESCRIPTION,
                        org.openide.util.NbBundle.getMessage(
                            MappingComponent.class,
                            "MappingComponent.zoomAction.LONG_DESCRIPTION"));                                          // NOI18N
                    putValue(Action.MNEMONIC_KEY, Integer.valueOf('Z'));                                               // NOI18N
                    putValue(Action.ACTION_COMMAND_KEY, "zoom.action");                                                // NOI18N
                }

                @Override
                public void actionPerformed(final ActionEvent event) {
                    zoomAction.putValue(
                        Action.SMALL_ICON,
                        new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/server.png"))); // NOI18N
                    setInteractionMode(MappingComponent.ZOOM);
                }
            };

        this.getCamera().addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener() {

                @Override
                public void propertyChange(final PropertyChangeEvent evt) {
//                if(DEBUG)log.debug("getCamera().getViewTransform():"+getCamera().getViewTransform());
//                if(DEBUG)log.debug("getCamera().getViewTransform().getScaleY():"+getCamera().getViewTransform().getScaleY());
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
                    handleLayer.removeAllChildren();
                    showHandleDelay.restart();
                    rescaleStickyNodes();
//                log.fatal(evt.getPropertyName()+" "+evt.getOldValue()+" "+evt.getNewValue());
                    CismapBroker.getInstance()
                            .fireStatusValueChanged(new StatusEvent(StatusEvent.SCALE, interactionMode));
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ButtonGroup getInteractionButtonGroup() {
        return interactionButtonGroup;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  interactionButtonGroup  DOCUMENT ME!
     */
    public void setInteractionButtonGroup(final ButtonGroup interactionButtonGroup) {
        this.interactionButtonGroup = interactionButtonGroup;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mapListener  DOCUMENT ME!
     */
    public void addMapListener(final MapListener mapListener) {
        if (mapListener != null) {
            mapListeners.add(mapListener);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mapListener  DOCUMENT ME!
     */
    public void removeMapListener(final MapListener mapListener) {
        if (mapListener != null) {
            mapListeners.remove(mapListener);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void dispose() {
        CismapBroker.getInstance().removeCrsChangeListener(this);
        getFeatureCollection().removeAllFeatures();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  true, if debug-messages are logged.
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
     *
     * @return  Image
     */
    public Image getImage() {
        // this.getCamera().print();
        return this.getCamera().toImage(this.getWidth(), this.getHeight(), Color.white);
    }

    /**
     * Creates an image with given width and height from all features in the given featurecollection. The image will be
     * used for printing.
     *
     * @param   fc      FeatureCollection
     * @param   width   desired width of the resulting image
     * @param   height  desired height of the resulting image
     *
     * @return  Image of the featurecollection
     */
    public Image getImageOfFeatures(final Collection<Feature> fc, final int width, final int height) {
        try {
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("getImageOffFeatures (" + width + "x" + height + ")"); // NOI18N
                }
            }

            final PrintingFrameListener pfl = ((PrintingFrameListener)getInputListener(PRINTING_AREA_SELECTION));
            final PCanvas pc = new PCanvas();
            // c.addLayer(featureLayer);
            pc.setSize(width, height);
            final List<PFeature> list = new ArrayList<PFeature>();
            final Iterator it = fc.iterator();
            while (it.hasNext()) {
                final Feature f = (Feature)it.next();
                final PFeature p = new PFeature(f, wtst, clip_offset_x, clip_offset_y, MappingComponent.this);
                if (p.getFullBounds().intersects(pfl.getPrintingRectangle().getBounds())) {
                    list.add(p);
                }
            }
            pc.getCamera().animateViewToCenterBounds(pfl.getPrintingRectangle().getBounds(), true, 0);
            final double scale = 1 / pc.getCamera().getViewScale();
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("subPCscale:" + scale); // NOI18N
                }
            }

            // TODO Sorge dafür dass die PSwingKomponente richtig gedruckt wird und dass die Karte nicht mehr "zittert"

            int printingLineWidth = -1;
            for (final PNode p : list) {
                if (p instanceof PFeature) {
                    final PFeature original = ((PFeature)p);
                    original.setInfoNodeExpanded(false);

                    if (printingLineWidth > 0) {
                        ((StyledFeature)original.getFeature()).setLineWidth(printingLineWidth);
                    } else if (StyledFeature.class.isAssignableFrom(original.getFeature().getClass())) {
                        final int orginalLineWidth = ((StyledFeature)original.getFeature()).getLineWidth();
                        printingLineWidth = (int)Math.round(orginalLineWidth * (getPrintingResolution() * 2));
                        if (DEBUG) {
                            if (log.isDebugEnabled()) {
                                log.debug("getImageOfFeatures: changed printingLineWidth from " + orginalLineWidth
                                            + " to " + printingLineWidth + " (resolution=" + getPrintingResolution()
                                            + ")"); // NOI18N
                            }
                        }
                        ((StyledFeature)original.getFeature()).setLineWidth(printingLineWidth);
                    }

                    final PFeature copy = new PFeature(original.getFeature(),
                            getWtst(),
                            0,
                            0,
                            MappingComponent.this,
                            true);
                    pc.getLayer().addChild(copy);

                    copy.setTransparency(original.getTransparency());
                    copy.setStrokePaint(original.getStrokePaint());
                    final boolean expanded = original.isInfoNodeExpanded();
                    copy.addInfoNode();
                    copy.setInfoNodeExpanded(false);
//                    original.setInfoNodeExpanded(true);
                    copy.refreshInfoNode();

//                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection(original.toImage()), null);
                    original.refreshInfoNode();

                    removeStickyNode(copy.getStickyChild());

                    // Wenn mal irgendwas wegen Querformat kommt :
                    // pf.getStickyChild().setRotation(0.5);
                    final PNode stickyChild = copy.getStickyChild();
                    if (stickyChild != null) {
                        stickyChild.setScale(scale * getPrintingResolution());
                        if (copy.hasSecondStickyChild()) {
                            copy.getSecondStickyChild().setScale(scale * getPrintingResolution());
                        }
                    }
                }
            }
            final Image ret = pc.getCamera().toImage(width, height, new Color(255, 255, 255, 0));
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug(ret);
                }
            }
            return ret;
        } catch (Exception exception) {
            log.error("Error during the creation of an image from features", exception); // NOI18N
            return null;
        }
    }

    /**
     * Creates an image with given width and height from all features that intersects the printingframe.
     *
     * @param   width   desired width of the resulting image
     * @param   height  desired height of the resulting image
     *
     * @return  Image of intersecting features
     */
    public Image getFeatureImage(final int width, final int height) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("getFeatureImage " + width + "x" + height);                              // NOI18N
            }
        }
        final PrintingFrameListener pfl = ((PrintingFrameListener)getInputListener(PRINTING_AREA_SELECTION));
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("printing rectangle bounds: " + pfl.getPrintingRectangle().getBounds()); // NOI18N
            }
        }
        final PCanvas pc = new PCanvas();
        // c.addLayer(featureLayer);
        pc.setSize(width, height);
        final List<PNode> list = new ArrayList<PNode>();
        final Iterator it = featureLayer.getChildrenIterator();
        while (it.hasNext()) {
            final PNode p = (PNode)it.next();
            if (p.getFullBounds().intersects(pfl.getPrintingRectangle().getBounds())) {
                list.add(p);
            }
        }
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("intersecting feature count: " + list.size()); // NOI18N
            }
        }
        pc.getCamera().animateViewToCenterBounds(pfl.getPrintingRectangle().getBounds(), true, 0);
        final double scale = 1 / pc.getCamera().getViewScale();
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("subPCscale:" + scale);                        // NOI18N
            }
        }

        // TODO Sorge dafür dass die PSwingKomponente richtig gedruckt wird und dass die Karte nicht mehr "zittert"

        for (final PNode p : list) {
            if (p instanceof PFeature) {
                final PFeature original = ((PFeature)p);
                try {
                    EventQueue.invokeAndWait(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    original.setInfoNodeExpanded(false);

                                    final PFeature copy = new PFeature(
                                            original.getFeature(),
                                            getWtst(),
                                            0,
                                            0,
                                            MappingComponent.this,
                                            true);
                                    pc.getLayer().addChild(copy);

                                    copy.setTransparency(original.getTransparency());
                                    copy.setStrokePaint(original.getStrokePaint());

                                    copy.addInfoNode();
                                    copy.setInfoNodeExpanded(false);

                                    // original.refreshInfoNode();

                                    // Wenn mal irgendwas wegen Querformat kommt :
                                    // pf.getStickyChild().setRotation(0.5);
                                    if (copy.getStickyChild() != null) {
                                        copy.getStickyChild().setScale(scale * getPrintingResolution());
                                    }
                                } catch (Throwable t) {
                                    log.error("Fehler beim erstellen des Featureabbildes", t); // NOI18N
                                }
                            }
                        });
                } catch (Throwable t) {
                    log.fatal("Fehler beim erstellen des Featureabbildes", t);                 // NOI18N
                    return null;
                }
                // Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection(original.toImage()),
                // null);

//                if(DEBUG)log.debug("StcikyChild:"+pf.getStickyChild().);
            }
        }
        return pc.getCamera().toImage(width, height, new Color(255, 255, 255, 0));
    }

    /**
     * Adds the given PCamera to the PRoot of this MappingComponent.
     *
     * @param  cam  PCamera-object
     */
    public void addToPRoot(final PCamera cam) {
        getRoot().addChild(cam);
    }

    /**
     * Adds a PNode to the StickyNode-vector.
     *
     * @param  pn  PNode-object
     */
    public void addStickyNode(final PNode pn) {
        // if(DEBUG)log.debug("addStickyNode:" + pn);
        stickyPNodes.add(pn);
    }

    /**
     * Removes a specific PNode from the StickyNode-vector.
     *
     * @param  pn  PNode that should be removed
     */
    public void removeStickyNode(final PNode pn) {
        stickyPNodes.remove(pn);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  Vector<PNode> with all sticky PNodes
     */
    public List<PNode> getStickyNodes() {
        return stickyPNodes;
    }

    /**
     * Calls private method rescaleStickyNodeWork(node) to rescale the sticky PNode. Forces the execution to the EDT.
     *
     * @param  n  PNode to rescale
     */
    public void rescaleStickyNode(final PNode n) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        rescaleStickyNodeWork(n);
                    }
                });
        } else {
            rescaleStickyNodeWork(n);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getPrintingResolution() {
        return this.printingResolution;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  printingResolution  DOCUMENT ME!
     */
    public void setPrintingResolution(final double printingResolution) {
        this.printingResolution = printingResolution;
    }

    /**
     * Sets the scale of the given PNode to the value of the camera scale.
     *
     * @param  n  PNode to rescale
     */
    private void rescaleStickyNodeWork(final PNode n) {
        final double s = MappingComponent.this.getCamera().getViewScale();
        n.setScale(1 / s);
    }

    /**
     * Rescales all nodes inside the StickyNode-vector.
     */
    public void rescaleStickyNodes() {
        final List<PNode> stickyNodeCopy = new ArrayList<PNode>(getStickyNodes());
        for (final PNode each : stickyNodeCopy) {
            if ((each instanceof PSticky) && each.getVisible()) {
                rescaleStickyNode(each);
            } else {
                if ((each instanceof PSticky) && (each.getParent() == null)) {
                    removeStickyNode(each);
                }
            }
        }
    }

    /**
     * Returns the custom created Action zoomAction.
     *
     * @return  Action-object
     */
    public Action getZoomAction() {
        return zoomAction;
    }

    /**
     * Pans to the given bounds without creating a historyaction to undo the action.
     *
     * @param  bounds  new bounds of the camera
     */
    public void gotoBoundsWithoutHistory(PBounds bounds) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("gotoBoundsWithoutHistory(PBounds: " + bounds, new CurrentStackTrace()); // NOI18N
            }
        }
        try {
            try {
                handleLayer.removeAllChildren();
            } catch (Exception e) {
                log.warn("error during removeAllCHildren", e);                                     // NOI18N
            }
            if (bounds.getWidth() < 0) {
                bounds.setSize(bounds.getWidth() * (-1), bounds.getHeight());
            }
            if (bounds.getHeight() < 0) {
                bounds.setSize(bounds.getWidth(), bounds.getHeight() * (-1));
            }

            if (bounds instanceof PBoundsWithCleverToString) {
                final PBoundsWithCleverToString boundWCTS = (PBoundsWithCleverToString)bounds;
                if (!boundWCTS.getCrsCode().equals(mappingModel.getSrs().getCode())) {
                    try {
                        final Rectangle2D pos = new Rectangle2D.Double();
                        XBoundingBox bbox = boundWCTS.getWorldCoordinates();
                        final CrsTransformer trans = new CrsTransformer(mappingModel.getSrs().getCode());
                        bbox = trans.transformBoundingBox(bbox);
                        bounds = bbox.getPBounds(getWtst());
                    } catch (Exception e) {
                        log.error("Cannot transform the bounding box from " + boundWCTS.getCrsCode() + " to "
                                    + mappingModel.getSrs().getCode());
                    }
                }
            }

            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("before animateView"); // NOI18N
                }
            }
            getCamera().animateViewToCenterBounds(((PBounds)bounds), true, animationDuration);
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("after animateView");  // NOI18N
                }
            }

            queryServicesWithoutHistory();

            showHandles(true);
        } catch (NullPointerException npe) {
            log.warn("NPE in gotoBoundsWithoutHistory(" + bounds + ")", npe); // NOI18N
        }
    }

    /**
     * Checks out the y-camerascales for negative value and fixes it by negating both x- and y-scales.
     */
    private void checkAndFixErroneousTransformation() {
        if (getCamera().getViewTransform().getScaleY() < 0) {
            final double y = getCamera().getViewTransform().getScaleY();
            final double x = getCamera().getViewTransform().getScaleX();
            log.warn("Erroneous ViewTransform: getViewTransform (scaleY=" + y + " scaleX=" + x + "). Try to fix it."); // NOI18N
            getCamera().getViewTransformReference()
                    .setToScale(getCamera().getViewTransform().getScaleX() * (-1), y * (-1));
        }
    }

    /**
     * Re-adds the default layers in a given order.
     */
    private void adjustLayers() {
        // getCamera().removeAllChildren();
        int counter = 0;
        getCamera().addLayer(counter++, mapServicelayer);
        for (int i = 0; i < featureServiceLayer.getChildrenCount(); ++i) {
            getCamera().addLayer(counter++, (PLayer)featureServiceLayer.getChild(i));
        }
        getCamera().addLayer(counter++, featureLayer);
        getCamera().addLayer(counter++, tmpFeatureLayer);
        // getCamera().addLayer(counter++,snapHandleLayer);
        // getCamera().addLayer(counter++,handleLayer);
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
        inputEventListener.put(PAN, new PanAndMousewheelZoomListener());
//        inputEventListener.put(PAN, new BackgroundRefreshingPanEventListener());
        inputEventListener.put(SELECT, new SelectionListener());

        inputEventListener.put(FEATURE_INFO, new GetFeatureInfoClickDetectionListener());
        inputEventListener.put(CREATE_SEARCH_POLYGON, new CreateSearchGeometryListener(this));

        inputEventListener.put(MOVE_POLYGON, new FeatureMoveListener(this));
        inputEventListener.put(NEW_POLYGON, new CreateNewGeometryListener(this));
        inputEventListener.put(RAISE_POLYGON, new RaisePolygonListener(this));
        inputEventListener.put(REMOVE_POLYGON, new DeleteFeatureListener());
        inputEventListener.put(ATTACH_POLYGON_TO_ALPHADATA, new AttachFeatureListener());
        inputEventListener.put(JOIN_POLYGONS, new JoinPolygonsListener());
        inputEventListener.put(SPLIT_POLYGON, new SplitPolygonListener(this));
        inputEventListener.put(LINEMEASUREMENT, new MeasurementMoveListener(this));
        inputEventListener.put(MEASUREMENT, new MeasurementListener(this));
        inputEventListener.put(PRINTING_AREA_SELECTION, new PrintingFrameListener(this));
        inputEventListener.put(CUSTOM_FEATUREINFO, new CustomFeatureInfoListener());
        inputEventListener.put(OVERVIEW, new OverviewModeListener());
    }

    /**
     * Assigns a custom interactionmode with an own PBasicInputEventHandler.
     *
     * @param  key       interactionmode as String
     * @param  listener  new PBasicInputEventHandler
     */
    public void addCustomInputListener(final String key, final PBasicInputEventHandler listener) {
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
        putCursor(CREATE_SEARCH_POLYGON, new Cursor(Cursor.CROSSHAIR_CURSOR));

        putCursor(MOVE_POLYGON, new Cursor(Cursor.HAND_CURSOR));
        putCursor(ROTATE_POLYGON, new Cursor(Cursor.DEFAULT_CURSOR));
        putCursor(NEW_POLYGON, new Cursor(Cursor.CROSSHAIR_CURSOR));
        putCursor(RAISE_POLYGON, new Cursor(Cursor.DEFAULT_CURSOR));
        putCursor(REMOVE_POLYGON, new Cursor(Cursor.DEFAULT_CURSOR));
        putCursor(ATTACH_POLYGON_TO_ALPHADATA, new Cursor(Cursor.DEFAULT_CURSOR));
        putCursor(JOIN_POLYGONS, new Cursor(Cursor.DEFAULT_CURSOR));
        putCursor(SPLIT_POLYGON, new Cursor(Cursor.CROSSHAIR_CURSOR));
        putCursor(MEASUREMENT, new Cursor(Cursor.CROSSHAIR_CURSOR));
        putCursor(LINEMEASUREMENT, new Cursor(Cursor.DEFAULT_CURSOR));

        putCursor(MOVE_HANDLE, new Cursor(Cursor.CROSSHAIR_CURSOR));
        putCursor(REMOVE_HANDLE, new Cursor(Cursor.CROSSHAIR_CURSOR));
        putCursor(ADD_HANDLE, new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * Shows the printingsetting-dialog that resets the interactionmode after printing.
     *
     * @param  oldInteractionMode  String-object
     */
    public void showPrintingSettingsDialog(final String oldInteractionMode) {
        if (!(printingSettingsDialog.getParent() instanceof JFrame)) {
            printingSettingsDialog = printingSettingsDialog.cloneWithNewParent(true, this);
        }
        printingSettingsDialog.setInteractionModeAfterPrinting(oldInteractionMode);
        printingSettingsDialog.setLocationRelativeTo(this);
        printingSettingsDialog.setVisible(true);
    }

    /**
     * Shows the printing-dialog that resets the interactionmode after printing.
     *
     * @param  oldInteractionMode  String-object
     */
    public void showPrintingDialog(final String oldInteractionMode) {
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
            log.error("Fehler beim Anzeigen des Printing Dialogs", e); // NOI18N
        }
    }

    /**
     * Getter for property interactionMode.
     *
     * @return  Value of property interactionMode.
     */
    public String getInteractionMode() {
        return this.interactionMode;
    }

    /**
     * Changes the interactionmode.
     *
     * @param  interactionMode  new interactionmode as String
     */
    public void setInteractionMode(final String interactionMode) {
        try {
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("setInteractionMode(" + interactionMode + ")\nAlter InteractionMode:"
                                + this.interactionMode + "",
                        new Exception()); // NOI18N
                }
            }

            try {
                handleLayer.removeAllChildren();
            } catch (Exception e) {
                log.warn("Fehler bei removeAllCHildren", e); // NOI18N
            }
            setPointerAnnotationVisibility(false);
            if (getPrintingFrameLayer().getChildrenCount() > 1) {
                getPrintingFrameLayer().removeAllChildren();
            }
            if (this.interactionMode != null) {
                if (interactionMode.equals(FEATURE_INFO)) {
                    ((GetFeatureInfoClickDetectionListener)this.getInputListener(interactionMode)).getPInfo()
                            .setVisible(true);
                } else {
                    ((GetFeatureInfoClickDetectionListener)this.getInputListener(FEATURE_INFO)).getPInfo()
                            .setVisible(false);
                }

                if (isReadOnly()) {
                    ((DefaultFeatureCollection)(getFeatureCollection())).removeFeaturesByInstance(PureNewFeature.class);
                }

                final PInputEventListener pivl = this.getInputListener(this.interactionMode);
                if (pivl != null) {
                    removeInputEventListener(pivl);
                } else {
                    log.warn("this.getInputListener(this.interactionMode)==null");                          // NOI18N
                }
                if (interactionMode.equals(NEW_POLYGON) || interactionMode.equals(CREATE_SEARCH_POLYGON)) { // ||interactionMode==SELECT) {
//                if (selectedFeature!=null) {
//                    selectPFeatureManually(null);
//                }

                    featureCollection.unselectAll();
                }
                if ((interactionMode.equals(SELECT) || interactionMode.equals(LINEMEASUREMENT)
                                || interactionMode.equals(SPLIT_POLYGON))
                            && (this.readOnly == false)) {
//                if (selectedFeature!=null) {
//                    selectPFeatureManually(selectedFeature);
//                }
                    featureSelectionChanged(null);
                }
                if (interactionMode.equals(JOIN_POLYGONS)) {
                    try {
                        handleLayer.removeAllChildren();
                    } catch (Exception e) {
                        log.warn("Fehler bei removeAllCHildren", e); // NOI18N
                    }
                }
            }
            final PropertyChangeEvent interactionModeChangedEvent = new PropertyChangeEvent(
                    this,
                    PROPERTY_MAP_INTERACTION_MODE,
                    this.interactionMode,
                    interactionMode);
            this.interactionMode = interactionMode;
            final PInputEventListener pivl = getInputListener(interactionMode);
            if (pivl != null) {
                addInputEventListener(pivl);
                propertyChangeSupport.firePropertyChange(interactionModeChangedEvent);
                CismapBroker.getInstance()
                        .fireStatusValueChanged(new StatusEvent(StatusEvent.MAPPING_MODE, interactionMode));
            } else {
                log.warn("this.getInputListener(this.interactionMode)==null bei interactionMode=" + interactionMode); // NOI18N
            }
        } catch (Exception e) {
            log.error("Fehler beim Ändern des InteractionModes", e); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    @Deprecated
    public void formComponentResized(final ComponentEvent evt) {
        this.componentResizedDelayed();
    }

    /**
     * Resizes the map and does not reload all services.
     *
     * @see  #componentResizedDelayed()
     */
    public void componentResizedIntermediate() {
        if (!this.isLocked()) {
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("componentResizedIntermediate " + MappingComponent.this.getSize()); // NOI18N
                }
            }

            if ((MappingComponent.this.getSize().height >= 0) && (MappingComponent.this.getSize().width >= 0)) {
                if (mappingModel != null) {
                    if (DEBUG) {
                        if (log.isDebugEnabled()) {
                            log.debug("BB:" + MappingComponent.this.currentBoundingBox); // NOI18N
                        }
                    }
                    if (MappingComponent.this.currentBoundingBox == null) {
                        log.error("currentBoundingBox is null");                         // NOI18N
                        currentBoundingBox = getCurrentBoundingBox();
                    }

                    // rescale map
                    if (historyModel.getCurrentElement() != null) {
                        final PBounds bounds = (PBounds)historyModel.getCurrentElement();
                        if (bounds.getWidth() < 0) {
                            bounds.setSize(bounds.getWidth() * (-1), bounds.getHeight());
                        }
                        if (bounds.getHeight() < 0) {
                            bounds.setSize(bounds.getWidth(), bounds.getHeight() * (-1));
                        }
                        getCamera().animateViewToCenterBounds(bounds, true, animationDuration);
                    }
                }
            }

            // move internal widgets
            for (final String internalWidget : this.internalWidgets.keySet()) {
                if (this.getInternalWidget(internalWidget).isVisible()) {
                    showInternalWidget(internalWidget, true, 0);
                }
            }
        }
    }

    /**
     * Resizes the map and reloads all services.
     *
     * @see  #componentResizedIntermediate()
     */
    public void componentResizedDelayed() {
        if (!this.isLocked()) {
            try {
                if (DEBUG) {
                    if (log.isDebugEnabled()) {
                        log.debug("componentResizedDelayed " + MappingComponent.this.getSize()); // NOI18N
                    }
                }
                if ((MappingComponent.this.getSize().height >= 0) && (MappingComponent.this.getSize().width >= 0)) {
                    if (mappingModel != null) {
                        if (DEBUG) {
                            if (log.isDebugEnabled()) {
                                log.debug("BB:" + MappingComponent.this.currentBoundingBox);     // NOI18N
                            }
                        }
                        if (MappingComponent.this.currentBoundingBox == null) {
                            log.error("currentBoundingBox is null");                             // NOI18N
                            currentBoundingBox = getCurrentBoundingBox();
                        }
                        gotoBoundsWithoutHistory((PBounds)historyModel.getCurrentElement());
//                        }
//                        if (getCurrentElement()!=null) {
//                            gotoBoundsWithoutHistory((PBounds)(getCurrentElement()));
//                        } else {
//                            if(DEBUG)log.debug("getCurrentElement()==null) ");
//                        }

//            for (JComponent internalWiget : this.internalWidgets.values())
//            {
//              if (internalWiget.isVisible())
//              {
//                internalWiget.setVisible(false);
//              }
//            }

                        // move internal widgets
                        for (final String internalWidget : this.internalWidgets.keySet()) {
                            if (this.getInternalWidget(internalWidget).isVisible()) {
                                showInternalWidget(internalWidget, true, 0);
                            }
                        }

//            if (internalLayerWidget != null && internalLayerWidget.isVisible())
//            {
//              internalLayerWidget.setVisible(false);
//            }
                    }
                }
            } catch (Throwable t) {
                log.error("Fehler in formComponentResized()", t); // NOI18N
            }
        }
    }

    /**
     * syncSelectedObjectPresenter(int i).
     *
     * @param  i  DOCUMENT ME!
     */
    public void syncSelectedObjectPresenter(final int i) {
        selectedObjectPresenter.setVisible(true);
        if (featureCollection.getSelectedFeatures().size() > 0) {
            if (featureCollection.getSelectedFeatures().size() == 1) {
                final PFeature selectedFeature = (PFeature)pFeatureHM.get(
                        featureCollection.getSelectedFeatures().toArray()[0]);
                if (selectedFeature != null) {
                    selectedObjectPresenter.getCamera()
                            .animateViewToCenterBounds(selectedFeature.getBounds(), true, getAnimationDuration() * 2);
                }
            } else {
                // todo
            }
        } else {
            log.warn("in syncSelectedObjectPresenter(" + i + "): selectedFeature==null"); // NOI18N
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
     *
     * @return  DOCUMENT ME!
     */
    public FeatureCollection getFeatureCollection() {
        return featureCollection;
    }

    /**
     * Replaces the old featureCollection with a new one.
     *
     * @param  featureCollection  the new featureCollection
     */
    public void setFeatureCollection(final FeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
        featureCollection.addFeatureCollectionListener(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visibility  DOCUMENT ME!
     */
    public void setFeatureCollectionVisibility(final boolean visibility) {
        featureLayer.setVisible(visibility);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isFeatureCollectionVisible() {
        return featureLayer.getVisible();
    }

    /**
     * Adds a new mapservice at a specific place of the layercontrols.
     *
     * @param  mapService  the new mapservice
     * @param  position    the index where to position the mapservice
     */
    public void addMapService(final MapService mapService, final int position) {
        try {
            PNode p = new PNode();
            if (mapService instanceof RasterMapService) {
                log.info("adding RasterMapService '" + mapService + "' " + mapService.getClass().getSimpleName() + ")"); // NOI18N
                if (mapService.getPNode() instanceof XPImage) {
                    p = (XPImage)mapService.getPNode();
                } else {
                    p = new XPImage();
                    mapService.setPNode(p);
                }
                mapService.addRetrievalListener(new MappingComponentRasterServiceListener(
                        position,
                        p,
                        (ServiceLayer)mapService));
            } else {
                log.info("adding FeatureMapService '" + mapService + "' (" + mapService.getClass().getSimpleName()
                            + ")");                                                                                      // NOI18N
                p = new PLayer();
                mapService.setPNode(p);

                if (DocumentFeatureService.class.isAssignableFrom(mapService.getClass())) {
                    if (DEBUG) {
                        if (log.isDebugEnabled()) {
                            log.debug("FeatureMapService(" + mapService
                                        + "): isDocumentFeatureService, checking document size");       // NOI18N
                        }
                    }
                    final DocumentFeatureService documentFeatureService = (DocumentFeatureService)mapService;
                    if (documentFeatureService.getDocumentSize() > this.criticalDocumentSize) {
                        log.warn("FeatureMapService(" + mapService + "): DocumentFeatureService '"
                                    + documentFeatureService.getName() + "' size of "
                                    + (documentFeatureService.getDocumentSize() / 1000000)
                                    + "MB exceeds critical document size (" + (this.criticalDocumentSize / 1000000)
                                    + "MB)");                                                           // NOI18N
                        if (this.documentProgressListener == null) {
                            if (DEBUG) {
                                if (log.isDebugEnabled()) {
                                    log.debug("FeatureMapService(" + mapService
                                                + "): lazy instantiation of documentProgressListener"); // NOI18N
                                }
                            }
                            this.documentProgressListener = new DocumentProgressListener();
                        }

                        if (this.documentProgressListener.getRequestId() != -1) {
                            log.error("FeatureMapService(" + mapService
                                        + "): The documentProgressListener is already in use by request '"
                                        + this.documentProgressListener.getRequestId()
                                        + ", document progress cannot be tracked");                                      // NOI18N
                        } else {
                            if (DEBUG) {
                                if (log.isDebugEnabled()) {
                                    log.debug("FeatureMapService(" + mapService + "): adding documentProgressListener"); // NOI18N
                                }
                            }
                            documentFeatureService.addRetrievalListener(this.documentProgressListener);
                        }
                    }
                }

                mapService.addRetrievalListener(new MappingComponentFeatureServiceListener(
                        (ServiceLayer)mapService,
                        (PLayer)mapService.getPNode()));
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
            log.error("addMapService(" + mapService + "): Fehler beim hinzufuegen eines Layers: " + t.getMessage(), t); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mm  DOCUMENT ME!
     */
    public void preparationSetMappingModel(final MappingModel mm) {
        mappingModel = mm;
    }

    /**
     * Sets a new mappingmodel in this MappingComponent.
     *
     * @param  mm  the new mappingmodel
     */
    public void setMappingModel(final MappingModel mm) {
        log.info("setMappingModel");                        // NOI18N
        if (Thread.getDefaultUncaughtExceptionHandler() == null) {
            log.info("setDefaultUncaughtExceptionHandler"); // NOI18N
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                    @Override
                    public void uncaughtException(final Thread t, final Throwable e) {
                        log.error("Error", e);
                    }
                });
        }
        mappingModel = mm;
        currentBoundingBox = mm.getInitialBoundingBox();
        final Runnable r = new Runnable() {

                @Override
                public void run() {
                    mappingModel.addMappingModelListener(MappingComponent.this);
                    // currentBoundingBox=mm.getInitialBoundingBox();
                    final TreeMap rs = mappingModel.getRasterServices();
                    // reCalcWtstAndBoundingBox();

                    // Rückwärts wegen der Reihenfolge der Layer im Layer Widget
                    final Iterator it = rs.keySet().iterator();
                    while (it.hasNext()) {
                        final Object key = it.next();
                        final int rsi = ((Integer)key).intValue();
                        final Object o = rs.get(key);
                        if (o instanceof MapService) {
                            addMapService(((MapService)o), rsi);
                        }
                    }

                    // Es gibt nur noch MapServices

//                TreeMap fs = mappingModel.getFeatureServices();
//                //Rueckwaerts wegen der Reihenfolge der Layer im Layer Widget
//                it = fs.keySet().iterator();
//                while (it.hasNext()) {
//                    Object key = it.next();
//                    int fsi = ((Integer) key).intValue();
//                    Object o = fs.get(key);
//                    if (o instanceof MapService) {
//                        if(DEBUG)log.debug("neuer Featureservice: " + o);
//                        PLayer pn = new PLayer();
//                        //pn.setVisible(true);
//                        //pn.setBounds(this.getRoot().getFullBounds());
//                        pn.setTransparency(((MapService) o).getTranslucency());
//                        //((FeatureService)o).setPNode(pn);
//                        featureServiceLayer.addChild(pn);
//                        pn.addClientProperty("serviceLayer", (ServiceLayer) o);
//                        //getCamera().addLayer(pn);
//                        ((MapService) o).addRetrievalListener(new MappingComponentFeatureServiceListener((ServiceLayer) o, pn));
//                        if(DEBUG)log.debug("add FeatureService");
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
                    // internalLayerWidget = new NewSimpleInternalLayerWidget(MappingComponent.this);
                    // internalLayerWidget.setMappingModel(mappingModel);

                    // gotoInitialBoundingBox();
                    final NewSimpleInternalLayerWidget simpleInternalLayerWidget = new NewSimpleInternalLayerWidget(
                            MappingComponent.this);
                    MappingComponent.this.addInternalWidget(
                        LAYERWIDGET,
                        MappingComponent.POSITION_SOUTHEAST,
                        simpleInternalLayerWidget);

                    if (DEBUG) {
                        if (log.isDebugEnabled()) {
                            log.debug("Set Mapping Modell done"); // NOI18N
                        }
                    }
                }
            };
        CismetThreadPool.execute(r);
    }

    /**
     * Returns the current mappingmodel.
     *
     * @return  current mappingmodel
     */
    public MappingModel getMappingModel() {
        return mappingModel;
    }

    /**
     * Animates a component to a given x/y-coordinate in a given time.
     *
     * @param  c                   the component to animate
     * @param  toX                 final x-position
     * @param  toY                 final y-position
     * @param  animationDuration   duration of the animation
     * @param  hideAfterAnimation  should the component be hidden after animation?
     */
    private void animateComponent(final JComponent c,
            final int toX,
            final int toY,
            final int animationDuration,
            final boolean hideAfterAnimation) {
        if (animationDuration > 0) {
            final int x = (int)c.getBounds().getX() - toX;
            final int y = (int)c.getBounds().getY() - toY;
            int sx;
            int sy;
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

            final int sleepy;
            if ((animationDuration / big) < 1) {
                sleepy = 1;
            } else {
                sleepy = (int)(animationDuration / big);
            }

            final int directionY = sy;
            final int directionX = sx;

            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("animateComponent: directionX=" + directionX + ", directionY=" + directionY
                                + ", currentX=" + c.getBounds().getX() + ", currentY=" + c.getBounds().getY() + ", toX="
                                + toX + ", toY=" + toY); // NOI18N
                }
            }
            final Thread timer = new Thread() {

                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            try {
                                sleep(sleepy);
                            } catch (Exception iex) {
                            }
                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        int currentY = (int)c.getBounds().getY();
                                        int currentX = (int)c.getBounds().getX();
                                        if (currentY != toY) {
                                            currentY = currentY + directionY;
                                        }
                                        if (currentX != toX) {
                                            currentX = currentX + directionX;
                                        }
                                        c.setBounds(currentX, currentY, c.getWidth(), c.getHeight());
                                    }
                                });

                            if ((c.getBounds().getY() == toY) && (c.getBounds().getX() == toX)) {
                                if (hideAfterAnimation) {
                                    EventQueue.invokeLater(new Runnable() {

                                            @Override
                                            public void run() {
                                                c.setVisible(false);
                                                c.hide();
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
        } else {
            c.setBounds(toX, toY, c.getWidth(), c.getHeight());
            if (hideAfterAnimation) {
                c.setVisible(false);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @depreacted  DOCUMENT ME!
     */
    @Deprecated
    public NewSimpleInternalLayerWidget getInternalLayerWidget() {
        return (NewSimpleInternalLayerWidget)this.getInternalWidget(LAYERWIDGET);
    }

    /**
     * Adds a new internal widget to the map.<br/>
     * If a {@code widget} with the same {@code name} already exisits, the old widget will be removed and the new widget
     * will be added. If a widget with a different name already exisit at the same {@code position} the new widget will
     * not be added and the operation returns {@code false}.
     *
     * @param   name      unique name of the widget
     * @param   position  position of the widget
     * @param   widget    the widget
     *
     * @return  {@code true} if the widget could be added, {@code false} otherwise
     *
     * @see     #POSITION_NORTHEAST
     * @see     #POSITION_NORTHWEST
     * @see     #POSITION_SOUTHEAST
     * @see     #POSITION_SOUTHWEST
     */
    public boolean addInternalWidget(final String name, final int position, final JInternalFrame widget) {
        if (log.isDebugEnabled()) {
            log.debug("adding internal widget '" + name + "' to position '" + position + "'"); // NOI18N
        }
        if (this.internalWidgets.containsKey(name)) {
            log.warn("widget '" + name + "' already added, removing old widget");              // NOI18N
            this.remove(this.getInternalWidget(name));
        } else if (this.internalWidgetPositions.containsValue(position)) {
            log.warn("widget position '" + position + "' already taken");                      // NOI18N
            return false;
        }

        this.internalWidgets.put(name, widget);
        this.internalWidgetPositions.put(name, position);

        widget.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE); // NOI18N
        this.add(widget);
        widget.pack();

        return true;
    }

    /**
     * Removes an existing internal widget from the map.
     *
     * @param   name  name of the widget to be removed
     *
     * @return  {@code true} id the widget was found and removed, {@code false} otherwise
     */
    public boolean removeInternalWidget(final String name) {
        if (log.isDebugEnabled()) {
            log.debug("removing internal widget '" + name + "'"); // NOI18N
        }
        if (!this.internalWidgets.containsKey(name)) {
            log.warn("widget '" + name + "' not found");          // NOI18N
            return false;
        }

        this.remove(this.getInternalWidget(name));
        this.internalWidgets.remove(name);
        this.internalWidgetPositions.remove(name);
        return true;
    }

    /**
     * Shows an InternalWidget by sliding it into the mappingcomponent.
     *
     * @param   name               name of the internl component to show
     * @param   visible            should the widget be visible after the animation?
     * @param   animationDuration  duration of the animation
     *
     * @return  {@code true} if the operation was successful, {@code false} otherwise
     */
    public boolean showInternalWidget(final String name, final boolean visible, final int animationDuration) {
        // log.info("showing internal widget '" + name + "': " + visible);
        final JInternalFrame internalWidget = this.getInternalWidget(name);
        if (internalWidget == null) {
            return false;
        }

        int positionX;
        int positionY;
        final int widgetPosition = this.getInternalWidgetPosition(name);
        switch (widgetPosition) {
            case POSITION_NORTHWEST: {
                positionX = 1;
                positionY = 1;
                break;
            }
            case POSITION_SOUTHWEST: {
                positionX = 1;
                positionY = getHeight() - internalWidget.getHeight() - 1;
                break;
            }
            case POSITION_NORTHEAST: {
                positionX = getWidth() - internalWidget.getWidth() - 1;
                positionY = 1;
                break;
            }
            case POSITION_SOUTHEAST: {
                positionX = getWidth() - internalWidget.getWidth() - 1;
                positionY = getHeight() - internalWidget.getHeight() - 1;
                break;
            }
            default: {
                log.warn("unkown widget position?!"); // NOI18N
                return false;
            }
        }

        if (visible) {
            int toY = positionY + internalWidget.getHeight() + 1;
            if ((widgetPosition == POSITION_NORTHWEST) || (widgetPosition == POSITION_NORTHEAST)) {
                toY = positionY - internalWidget.getHeight() - 1;
            }
            internalWidget.setBounds(positionX, toY, internalWidget.getWidth(), internalWidget.getHeight());
            internalWidget.setVisible(true);
            internalWidget.show();

            animateComponent(internalWidget, positionX, positionY, animationDuration, false);
        } else {
            internalWidget.setBounds(positionX, positionY, internalWidget.getWidth(), internalWidget.getHeight());
            int toY = positionY + internalWidget.getHeight() + 1;
            if ((widgetPosition == POSITION_NORTHWEST) || (widgetPosition == POSITION_NORTHEAST)) {
                toY = positionY - internalWidget.getHeight() - 1;
            }

            animateComponent(internalWidget, positionX, toY, animationDuration, true);
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visible            DOCUMENT ME!
     * @param  animationDuration  DOCUMENT ME!
     */
    @Deprecated
    public void showInternalLayerWidget(final boolean visible, final int animationDuration) {
        this.showInternalWidget(LAYERWIDGET, visible, animationDuration);

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
        // SOUTH EAST
//    int positionX = getWidth() - internalLayerWidget.getWidth() - 1;
//    int positionY = getHeight() - internalLayerWidget.getHeight() - 1;
//
//    if (visible)
//    {
//      internalLayerWidget.setVisible(true);
//      internalLayerWidget.show();
//      internalLayerWidget.setBounds(positionX, positionY + internalLayerWidget.getHeight() + 1, internalLayerWidget.getWidth(), internalLayerWidget.getHeight());
//      animateComponent(internalLayerWidget, positionX, positionY, animationDuration, false);
//
//    } else
//    {
//      internalLayerWidget.setBounds(positionX, positionY, internalLayerWidget.getWidth(), internalLayerWidget.getHeight());
//      animateComponent(internalLayerWidget, positionX, positionY + internalLayerWidget.getHeight() + 1, animationDuration, true);
//    }
    }

    /**
     * Returns a boolean, if the InternalLayerWidget is visible.
     *
     * @return  true, if visible, else false
     */
    @Deprecated
    public boolean isInternalLayerWidgetVisible() {
        return this.getInternalLayerWidget().isVisible();
    }

    /**
     * Returns a boolean, if the InternalWidget is visible.
     *
     * @param   name  name of the widget
     *
     * @return  true, if visible, else false
     */
    public boolean isInternalWidgetVisible(final String name) {
        final JInternalFrame widget = this.getInternalWidget(name);
        if (widget != null) {
            return widget.isVisible();
        }

        return false;
    }

    /**
     * Moves the camera to the initial bounding box (e.g. if the home-button is pressed).
     */
    public void gotoInitialBoundingBox() {
        final double x1;
        final double y1;
        final double x2;
        final double y2;
        final double w;
        final double h;
        x1 = getWtst().getScreenX(mappingModel.getInitialBoundingBox().getX1());
        y1 = getWtst().getScreenY(mappingModel.getInitialBoundingBox().getY1());
        x2 = getWtst().getScreenX(mappingModel.getInitialBoundingBox().getX2());
        y2 = getWtst().getScreenY(mappingModel.getInitialBoundingBox().getY2());
        final Rectangle2D home = new Rectangle2D.Double();
        home.setRect(x1, y2, x2 - x1, y1 - y2);
        getCamera().animateViewToCenterBounds(home, true, animationDuration);
        if (getCamera().getViewTransform().getScaleY() < 0) {
            log.fatal("gotoInitialBoundingBox: Problem :-( mit getViewTransform"); // NOI18N
        }
        setNewViewBounds(home);
        queryServices();
    }

    /**
     * Refreshs all registered services.
     */
    public void queryServices() {
        if (newViewBounds != null) {
            addToHistory(new PBoundsWithCleverToString(
                    new PBounds(newViewBounds),
                    wtst,
                    mappingModel.getSrs().getCode()));
            queryServicesWithoutHistory();
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("queryServices()"); // NOI18N
                }
            }
            rescaleStickyNodes();
        }
//        showHandles(false);
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
     * Waits until all animations are done, then iterates through all registered services and calls handleMapService()
     * for each.
     *
     * @param  forced  forces the refresh
     */
    private void queryServicesWithoutHistory(final boolean forced) {
        if (!locked) {
            final Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        while (getAnimating()) {
                            try {
                                Thread.currentThread().sleep(50);
                            } catch (Exception doNothing) {
                            }
                        }
                        CismapBroker.getInstance().fireMapBoundsChanged();

                        if (MappingComponent.this.isBackgroundEnabled()) {
                            final TreeMap rs = mappingModel.getRasterServices();
                            final TreeMap fs = mappingModel.getFeatureServices();

                            for (final Iterator it = rs.keySet().iterator(); it.hasNext();) {
                                final Object key = it.next();
                                final int rsi = ((Integer)key).intValue();
                                final Object o = rs.get(key);
                                if (o instanceof MapService) {
                                    if (DEBUG) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("queryServicesWithoutHistory (RasterServices): " + o); // NOI18N
                                        }
                                    }
                                    handleMapService(rsi, (MapService)o, forced);
                                } else {
                                    log.warn("service is not of type MapService:" + o);                      // NOI18N
                                }
                            }

                            for (final Iterator it = fs.keySet().iterator(); it.hasNext();) {
                                final Object key = it.next();
                                final int fsi = ((Integer)key).intValue();
                                final Object o = fs.get(key);
                                if (o instanceof MapService) {
                                    if (DEBUG) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("queryServicesWithoutHistory (FeatureServices): " + o); // NOI18N
                                        }
                                    }
                                    handleMapService(fsi, (MapService)o, forced);
                                } else {
                                    log.warn("service is not of type MapService:" + o);                       // NOI18N
                                }
                            }
                        }
                    }
                };
            CismetThreadPool.execute(r);
        }
    }

    /**
     * queryServicesIndependentFromMap.
     *
     * @param  width   DOCUMENT ME!
     * @param  height  DOCUMENT ME!
     * @param  bb      DOCUMENT ME!
     * @param  rl      DOCUMENT ME!
     */
    public void queryServicesIndependentFromMap(final int width,
            final int height,
            final BoundingBox bb,
            final RetrievalListener rl) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("queryServicesIndependentFromMap (" + width + "x" + height + ")"); // NOI18N
            }
        }
        final Runnable r = new Runnable() {

                @Override
                public void run() {
                    while (getAnimating()) {
                        try {
                            Thread.currentThread().sleep(50);
                        } catch (Exception doNothing) {
                        }
                    }
                    if (MappingComponent.this.isBackgroundEnabled()) {
                        final TreeMap rs = mappingModel.getRasterServices();
                        final TreeMap fs = mappingModel.getFeatureServices();

                        for (final Iterator it = rs.keySet().iterator(); it.hasNext();) {
                            final Object key = it.next();
                            final int rsi = ((Integer)key).intValue();
                            final Object o = rs.get(key);
                            if ((o instanceof AbstractRetrievalService) && (o instanceof ServiceLayer)
                                        && ((ServiceLayer)o).isEnabled()
                                        && (o instanceof RetrievalServiceLayer)
                                        && ((RetrievalServiceLayer)o).getPNode().getVisible()) {
                                try {
                                    // AbstractRetrievalService r = ((AbstractRetrievalService)
                                    // o).cloneWithoutRetrievalListeners();
                                    if (DEBUG) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("queryServicesIndependentFromMap: cloning '"
                                                        + o.getClass().getSimpleName() + "': '" + o + "'"); // NOI18N
                                        }
                                    }
                                    AbstractRetrievalService r;
                                    if (o instanceof WebFeatureService) {
                                        final WebFeatureService wfsClone = (WebFeatureService)((WebFeatureService)o)
                                                    .clone();
                                        wfsClone.removeAllListeners();
                                        r = wfsClone;
                                    } else {
                                        r = ((AbstractRetrievalService)o).cloneWithoutRetrievalListeners();
                                    }
                                    r.addRetrievalListener(rl);
                                    ((ServiceLayer)r).setLayerPosition(rsi);
                                    handleMapService(rsi, (MapService)r, width, height, bb, true);
                                } catch (Throwable t) {
                                    log.error("could not clone service '" + o + "' for printing: " + t.getMessage(), t); // NOI18N
                                }
                            } else {
                                log.warn("ignoring service '" + o + "' for printing");                      // NOI18N
                            }
                        }

                        for (final Iterator it = fs.keySet().iterator(); it.hasNext();) {
                            final Object key = it.next();
                            final int fsi = ((Integer)key).intValue();
                            final Object o = fs.get(key);
                            if (o instanceof AbstractRetrievalService) {
                                if (DEBUG) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("queryServicesIndependentFromMap: cloning '"
                                                    + o.getClass().getSimpleName() + "': '" + o + "'"); // NOI18N
                                    }
                                }
                                AbstractRetrievalService r;
                                if (o instanceof WebFeatureService) {
                                    final WebFeatureService wfsClone = (WebFeatureService)((WebFeatureService)o)
                                                .clone();
                                    wfsClone.removeAllListeners();
                                    r = (AbstractRetrievalService)o;
                                } else {
                                    r = ((AbstractRetrievalService)o).cloneWithoutRetrievalListeners();
                                }
                                r.addRetrievalListener(rl);
                                ((ServiceLayer)r).setLayerPosition(fsi);
                                handleMapService(fsi, (MapService)r, 0, 0, bb, true);
                            }
                        }
                    }
                }
            };
        CismetThreadPool.execute(r);
    }

    /**
     * former synchronized method.
     *
     * @param  position  DOCUMENT ME!
     * @param  service   rs
     * @param  forced    DOCUMENT ME!
     */
    public void handleMapService(final int position, final MapService service, final boolean forced) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("in handleRasterService: " + service + "("
                            + Integer.toHexString(System.identityHashCode(service)) + ")(" + service.hashCode() + ")"); // NOI18N
            }
        }

        final PBounds bounds = getCamera().getViewBounds();
        final BoundingBox bb = new BoundingBox();
        final double x1 = getWtst().getWorldX(bounds.getMinX());
        final double y1 = getWtst().getWorldY(bounds.getMaxY());
        final double x2 = getWtst().getWorldX(bounds.getMaxX());
        final double y2 = getWtst().getWorldY(bounds.getMinY());

        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("Bounds=" + bounds);                                                             // NOI18N
            }
        }
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("handleRasterService BoundingBox(" + x1 + " " + y1 + "," + x2 + " " + y2 + ")"); // NOI18N
            }
        }

        if (((ServiceLayer)service).getName().startsWith("prefetching")) { // NOI18N
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
     * former synchronized method.
     *
     * @param  position  DOCUMENT ME!
     * @param  rs        DOCUMENT ME!
     * @param  width     DOCUMENT ME!
     * @param  height    DOCUMENT ME!
     * @param  bb        DOCUMENT ME!
     * @param  forced    DOCUMENT ME!
     */
    private void handleMapService(final int position,
            final MapService rs,
            final int width,
            final int height,
            final BoundingBox bb,
            final boolean forced) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("handleMapService: " + rs); // NOI18N
            }
        }
        if (((ServiceLayer)rs).isEnabled()) {
            synchronized (serviceFuturesMap) {
                final Future<?> sf = serviceFuturesMap.get(rs);
                if ((sf == null) || sf.isDone()) {
                    rs.setSize(height, width);
                    final Runnable serviceCall = new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    while (getAnimating()) {
                                        try {
                                            Thread.currentThread().sleep(50);
                                        } catch (Exception e) {
                                        }
                                    }
                                    rs.setBoundingBox(bb);
                                    if (rs instanceof FeatureAwareRasterService) {
                                        ((FeatureAwareRasterService)rs).setFeatureCollection(featureCollection);
                                    }
                                    rs.retrieve(forced);
                                } finally {
                                    serviceFuturesMap.remove(rs);
                                }
                            }
                        };
                    synchronized (serviceFuturesMap) {
                        serviceFuturesMap.put(rs, CismetThreadPool.submit(serviceCall));
                    }
                }
            }
        }
    }

//    private void handleMapService(int position, final MapService rs, int width, int height, final BoundingBox bb, final boolean forced) {
//        if (DEBUG) {
//            log.debug("handleMapService: " + rs);
//        }
//
//        if (((ServiceLayer) rs).isEnabled()) {
//            rs.setSize(height, width);
//            //if(DEBUG)log.debug("this.currentBoundingBox:"+this.currentBoundingBox);
//            //If the PCanvas is in animation state, there should be a pre information about the
//            //aimed new bounds
//            Runnable handle = new Runnable() {
//
//                @Override
//                public void run() {
//                    while (getAnimating()) {
//                        try {
//                            Thread.currentThread().sleep(50);
//                        } catch (Exception e) {
//                        }
//                    }
//                    rs.setBoundingBox(bb);
//                    if (rs instanceof FeatureAwareRasterService) {
//                        ((FeatureAwareRasterService) rs).setFeatureCollection(featureCollection);
//                    }
//                    rs.retrieve(forced);
//                }
//            };
//            CismetThreadPool.execute(handle);
//        }
//    }
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
//            if(DEBUG)log.debug("handleFeatureService BoundingBox(" + x1 + " " + y1 + "," + x2 + " " + y2 + ")");
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
//            if(DEBUG)log.debug("handleFeatureService");
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
     * Creates a new WorldToScreenTransform for the current screensize (boundingbox) and returns it.
     *
     * @return  new WorldToScreenTransform or null
     */
    public WorldToScreenTransform getWtst() {
        try {
            if (wtst == null) {
                final double y_real = mappingModel.getInitialBoundingBox().getY2()
                            - mappingModel.getInitialBoundingBox().getY1();
                final double x_real = mappingModel.getInitialBoundingBox().getX2()
                            - mappingModel.getInitialBoundingBox().getX1();

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

                if ((x_real / x_screen) >= (y_real / y_screen)) { // X ist Bestimmer d.h. x wird nicht verändert
                    clip_height = x_screen * y_real / x_real;
                    clip_width = x_screen;
                    clip_offset_y = 0;                            // (y_screen-clip_height)/2;
                    clip_offset_x = 0;
                } else {                                          // Y ist Bestimmer
                    clip_height = y_screen;
                    clip_width = y_screen * x_real / y_real;
                    clip_offset_y = 0;
                    clip_offset_x = 0;                            // (x_screen-clip_width)/2;
                }
                // wtst= new
                // WorldToScreenTransform(mappingModel.getInitialBoundingBox().getX1(),mappingModel.getInitialBoundingBox().getY1(),mappingModel.getInitialBoundingBox().getX2(),mappingModel.getInitialBoundingBox().getY2(),0,0,clip_width,clip_height);
                // wtst= new
                // WorldToScreenTransform(mappingModel.getInitialBoundingBox().getX1(),mappingModel.getInitialBoundingBox().getY1(),mappingModel.getInitialBoundingBox().getX2(),mappingModel.getInitialBoundingBox().getY2(),0,0,
                // x_real,y_real); wtst= new WorldToScreenTransform(2566470,5673088,2566470+100,5673088+100,0,0,100,100);
                // wtst= new WorldToScreenTransform(-180,-90,180,90,0,0,180,90); wtst= new
                // WorldToScreenTransform(mappingModel.getInitialBoundingBox().getX1(),mappingModel.getInitialBoundingBox().getY1(),mappingModel.getInitialBoundingBox().getX1()+100,mappingModel.getInitialBoundingBox().getY1()+100,0,0,100,100);
                // wtst= new WorldToScreenTransform(0,0, 1000, 1000, 0,0, 1000,1000);
                wtst = new WorldToScreenTransform(mappingModel.getInitialBoundingBox().getX1(),
                        mappingModel.getInitialBoundingBox().getY2());
            }
            return wtst;
        } catch (Throwable t) {
            log.error("Fehler in getWtst()", t); // NOI18N
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
     *
     * @return  DOCUMENT ME!
     */
    public double getClip_offset_x() {
        return 0; // clip_offset_x;
    }

    /**
     * Assigns a new value to the x-clip-offset.
     *
     * @param  clip_offset_x  new clipoffset
     */
    public void setClip_offset_x(final double clip_offset_x) {
        this.clip_offset_x = clip_offset_x;
    }

    /**
     * Returns 0.
     *
     * @return  DOCUMENT ME!
     */
    public double getClip_offset_y() {
        return 0; // clip_offset_y;
    }

    /**
     * Assigns a new value to the y-clip-offset.
     *
     * @param  clip_offset_y  new clipoffset
     */
    public void setClip_offset_y(final double clip_offset_y) {
        this.clip_offset_y = clip_offset_y;
    }

    /**
     * Returns the rubberband-PLayer.
     *
     * @return  DOCUMENT ME!
     */
    public PLayer getRubberBandLayer() {
        return rubberBandLayer;
    }

    /**
     * Assigns a given PLayer to the variable rubberBandLayer.
     *
     * @param  rubberBandLayer  a PLayer
     */
    public void setRubberBandLayer(final PLayer rubberBandLayer) {
        this.rubberBandLayer = rubberBandLayer;
    }

    /**
     * Sets new viewbounds.
     *
     * @param  r2d  Rectangle2D
     */
    public void setNewViewBounds(final Rectangle2D r2d) {
        newViewBounds = r2d;
    }

    /**
     * Will be called if the selection of features changes. It selects the PFeatures connected to the selected features
     * of the featurecollectionevent and moves them to the front. Also repaints handles at the end.
     *
     * @param  fce  featurecollectionevent with selected features
     */
    @Override
    public void featureSelectionChanged(final FeatureCollectionEvent fce) {
        final Collection<PFeature> all = featureLayer.getChildrenReference();
        for (final PFeature f : all) {
            f.setSelected(false);
        }
        Collection<Feature> c;
        if (fce != null) {
            c = fce.getFeatureCollection().getSelectedFeatures();
        } else {
            c = featureCollection.getSelectedFeatures();
        }

        ////handle featuregroup select-delegation////
        final Set<Feature> selectionResult = TypeSafeCollections.newHashSet();
        for (final Feature current : c) {
            if (current instanceof FeatureGroup) {
                selectionResult.addAll(FeatureGroups.expandToLeafs((FeatureGroup)current));
            } else {
                selectionResult.add(current);
            }
        }
        /////////////////////////////////////////////
        c = selectionResult;
        for (final Feature f : c) {
            if (f != null) {
                final PFeature feature = (PFeature)getPFeatureHM().get(f);

                if (feature != null) {
                    feature.setSelected(true);
                    feature.moveToFront();
                    // Fuer den selectedObjectPresenter (Eigener PCanvas)
                    syncSelectedObjectPresenter(1000);
                } else {
                    try {
                        handleLayer.removeAllChildren();
                    } catch (Exception e) {
                        log.warn("Fehler bei removeAllCHildren", e); // NOI18N
                    }
                }
            }
        }
        showHandles(false);
    }

    /**
     * Will be called if one or more features are changed somehow (handles moved/rotated). Calls reconsiderFeature() on
     * each feature of the given featurecollectionevent. Repaints handles at the end.
     *
     * @param  fce  featurecollectionevent with changed features
     */
    @Override
    public void featuresChanged(final FeatureCollectionEvent fce) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("featuresChanged"); // NOI18N
            }
        }
        final List<Feature> list = new ArrayList<Feature>();
        list.addAll(fce.getEventFeatures());
        for (final Feature elem : list) {
            reconsiderFeature(elem);
        }
        showHandles(false);
    }

    /**
     * Does a complete reconciliation of the PFeature assigned to a feature from the FeatureCollectionEvent. Calls
     * following PFeature-methods: syncGeometry(), visualize(), resetInfoNodePosition() and refreshInfoNode()
     *
     * @param  fce  featurecollectionevent with features to reconsile
     */
    @Override
    public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
        for (final Feature f : fce.getEventFeatures()) {
            if (f != null) {
                final PFeature node = pFeatureHM.get(f);
                if (node != null) {
                    node.syncGeometry();
                    node.visualize();
                    node.resetInfoNodePosition();
                    node.refreshInfoNode();
                    repaint();
                    // für Mehrfachzeichnung der Handles verantworlich ??
// showHandles(false);
                }
            }
        }
    }

    /**
     * Adds all features from the FeatureCollectionEvent to the mappingcomponent. Paints handles at the end. Former
     * synchronized method.
     *
     * @param  fce  FeatureCollectionEvent with features to add
     */
    @Override
    public void featuresAdded(final FeatureCollectionEvent fce) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("firefeaturesAdded (old disabled)"); // NOI18N
            }
        }
        // Attention: Bug-Gefahr !!! TODO
        // addFeaturesToMap(fce.getEventFeatures().toArray(new Feature[0]));
        // if(DEBUG)log.debug("featuresAdded()");
    }

    /**
     * Method is deprecated and deactivated. Does nothing!!
     *
     * @deprecated  DOCUMENT ME!
     */
    @Override
    @Deprecated
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
     * Clears the PFeatureHashmap and removes all PFeatures from the featurelayer. Does a
     * checkFeatureSupportingRasterServiceAfterFeatureRemoval() on all features from the given FeatureCollectionEvent.
     *
     * @param  fce  FeatureCollectionEvent with features to check for a remaining supporting rasterservice
     */
    @Override
    public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
        for (final PFeature feature : pFeatureHM.values()) {
            feature.cleanup();
        }
        stickyPNodes.clear();
        pFeatureHM.clear();
        featureLayer.removeAllChildren();
        checkFeatureSupportingRasterServiceAfterFeatureRemoval(fce);
    }

    /**
     * Removes all features of the given FeatureCollectionEvent from the mappingcomponent. Checks for remaining
     * supporting rasterservices and paints handles at the end.
     *
     * @param  fce  FeatureCollectionEvent with features to remove
     */
    @Override
    public void featuresRemoved(final FeatureCollectionEvent fce) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("featuresRemoved"); // NOI18N
            }
        }
        removeFeatures(fce.getEventFeatures());
        checkFeatureSupportingRasterServiceAfterFeatureRemoval(fce);
        showHandles(false);
    }

    /**
     * Checks after removing one or more features from the mappingcomponent which rasterservices became unnecessary and
     * which need a refresh.
     *
     * @param  fce  FeatureCollectionEvent with removed features
     */
    private void checkFeatureSupportingRasterServiceAfterFeatureRemoval(final FeatureCollectionEvent fce) {
        final HashSet<FeatureAwareRasterService> rasterServicesWhichShouldBeRemoved =
            new HashSet<FeatureAwareRasterService>();
        final HashSet<FeatureAwareRasterService> rasterServicesWhichShouldBeRefreshed =
            new HashSet<FeatureAwareRasterService>();
        final HashSet<FeatureAwareRasterService> rasterServices = new HashSet<FeatureAwareRasterService>();

        for (final Feature f : getFeatureCollection().getAllFeatures()) {
            if ((f instanceof RasterLayerSupportedFeature)
                        && (((RasterLayerSupportedFeature)f).getSupportingRasterService() != null)) {
                final FeatureAwareRasterService rs = ((RasterLayerSupportedFeature)f).getSupportingRasterService();
                if (DEBUG) {
                    if (log.isDebugEnabled()) {
                        log.debug("getAllFeatures() Feature:SupportingRasterService:" + f + ":" + rs); // NOI18N
                    }
                }
                rasterServices.add(rs);                                                                // DANGER
            }
        }

        for (final Feature f : fce.getEventFeatures()) {
            if ((f instanceof RasterLayerSupportedFeature)
                        && (((RasterLayerSupportedFeature)f).getSupportingRasterService() != null)) {
                final FeatureAwareRasterService rs = ((RasterLayerSupportedFeature)f).getSupportingRasterService();
                if (DEBUG) {
                    if (log.isDebugEnabled()) {
                        log.debug("getEventFeatures() Feature:SupportingRasterService:" + f + ":" + rs); // NOI18N
                    }
                }
                if (rasterServices.contains(rs)) {
                    for (final Object o : getMappingModel().getRasterServices().values()) {
                        final MapService r = (MapService)o;
                        if (r.equals(rs)) {
                            rasterServicesWhichShouldBeRefreshed.add((FeatureAwareRasterService)r);
                        }
                    }
                } else {
                    for (final Object o : getMappingModel().getRasterServices().values()) {
                        final MapService r = (MapService)o;
                        if (r.equals(rs)) {
                            rasterServicesWhichShouldBeRemoved.add((FeatureAwareRasterService)r);
                        }
                    }
                }
            }
        }
        for (final FeatureAwareRasterService frs : rasterServicesWhichShouldBeRemoved) {
            getMappingModel().removeLayer(frs);
        }
        for (final FeatureAwareRasterService frs : rasterServicesWhichShouldBeRefreshed) {
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
     * Creates new PFeatures for all features in the given array and adds them to the PFeatureHashmap. Then adds the
     * PFeature to the featurelayer.
     *
     * <p>DANGER: there's a bug risk here because the method runs in an own thread! It is possible that a PFeature of a
     * feature is demanded but not yet added to the hashmap which causes in most cases a NullPointerException!</p>
     *
     * @param  features  array with features to add
     */
    public void addFeaturesToMap(final Feature[] features) {
//        Runnable r = new Runnable() {
//
//            @Override
//            public void run() {
        final double local_clip_offset_y = clip_offset_y;
        final double local_clip_offset_x = clip_offset_x;

        /// Hier muss der layer bestimmt werdenn
        for (int i = 0; i < features.length; ++i) {
            final PFeature p = new PFeature(
                    features[i],
                    getWtst(),
                    local_clip_offset_x,
                    local_clip_offset_y,
                    MappingComponent.this);
            try {
                if (features[i] instanceof StyledFeature) {
                    p.setTransparency(((StyledFeature)(features[i])).getTransparency());
                } else {
                    p.setTransparency(cismapPrefs.getLayersPrefs().getAppFeatureLayerTranslucency());
                }
            } catch (Exception e) {
                p.setTransparency(0.8f);
                log.info("Fehler beim Setzen der Transparenzeinstellungen", e); // NOI18N
            }
            // So kann man es Piccolo überlassen (müsste nur noch ein transformation machen, die die y achse spiegelt)
            // PFeature p=new PFeature(features[i],(WorldToScreenTransform)null,(double)0,(double)0); p.setViewer(this);
            // ((PPath)p).setStroke(new BasicStroke(0.5f));
            if (features[i].getGeometry() != null) {
                pFeatureHM.put(p.getFeature(), p);
//                for (int j = 0; j < p.getCoordArr().length; ++j) {
//                    pFeatureHMbyCoordinate.put(p.getCoordArr()[j], new PFeatureCoordinatePosition(p, j));
//                }
                final int ii = i;
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            featureLayer.addChild(p);
                            if (!(features[ii].getGeometry() instanceof com.vividsolutions.jts.geom.Point)) {
                                // p.moveToBack();
                                p.moveToFront();
                            }
                        }
                    });
            }
        }
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    rescaleStickyNodes();
                    repaint();
                    fireFeaturesAddedToMap(Arrays.asList(features));

                    // SuchFeatures in den Vordergrund stellen
                    for (final Feature feature : featureCollection.getAllFeatures()) {
                        if (feature instanceof SearchFeature) {
                            final PFeature pFeature = pFeatureHM.get(feature);
                            pFeature.moveToFront();
                        }
                    }
                }
            });
//            }
//        };
//        CismetThreadPool.execute(r);

        // check whether the feature has a rasterSupportLayer or not
        for (final Feature f : features) {
            if ((f instanceof RasterLayerSupportedFeature)
                        && (((RasterLayerSupportedFeature)f).getSupportingRasterService() != null)) {
                final FeatureAwareRasterService rs = ((RasterLayerSupportedFeature)f).getSupportingRasterService();
                if (!getMappingModel().getRasterServices().containsValue(rs)) {
                    if (DEBUG) {
                        if (log.isDebugEnabled()) {
                            log.debug("FeatureAwareRasterServiceAdded"); // NOI18N
                        }
                    }
                    rs.setFeatureCollection(getFeatureCollection());
                    getMappingModel().addLayer(rs);
                }
            }
        }

        showHandles(false);
    }
//    public void addFeaturesToMap(final Feature[] features) {
//        Runnable r = new Runnable() {
//
//            @Override
//            public void run() {
//                double local_clip_offset_y = clip_offset_y;
//                double local_clip_offset_x = clip_offset_x;
//
//                /// Hier muss der layer bestimmt werdenn
//                for (int i = 0; i < features.length; ++i) {
//                    final PFeature p = new PFeature(features[i], getWtst(), local_clip_offset_x, local_clip_offset_y, MappingComponent.this);
//                    try {
//                        if (features[i] instanceof StyledFeature) {
//                            p.setTransparency(((StyledFeature) (features[i])).getTransparency());
//                        } else {
//                            p.setTransparency(cismapPrefs.getLayersPrefs().getAppFeatureLayerTranslucency());
//                        }
//                    } catch (Exception e) {
//                        p.setTransparency(0.8f);
//                        log.info("Fehler beim Setzen der Transparenzeinstellungen", e);
//                    }
//                    // So kann man es Piccolo überlassen (müsste nur noch ein transformation machen, die die y achse spiegelt)
//                    // PFeature p=new PFeature(features[i],(WorldToScreenTransform)null,(double)0,(double)0);
//                    // p.setViewer(this);
//                    // ((PPath)p).setStroke(new BasicStroke(0.5f));
//                    if (features[i].getGeometry() != null) {
//                        pFeatureHM.put(p.getFeature(), p);
//                        for (int j = 0; j < p.getCoordArr().length; ++j) {
//                            pFeatureHMbyCoordinate.put(p.getCoordArr()[j], new PFeatureCoordinatePosition(p, j));
//                        }
//                        final int ii = i;
//                        EventQueue.invokeLater(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                featureLayer.addChild(p);
//                                if (!(features[ii].getGeometry() instanceof com.vividsolutions.jts.geom.Point)) {
//                                    //p.moveToBack();
//                                    p.moveToFront();
//                                }
//                            }
//                        });
//                    }
//                }
//                EventQueue.invokeLater(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        rescaleStickyNodes();
//                        repaint();
//                        fireFeaturesAddedToMap(Arrays.asList(features));
//
//                        // SuchFeatures in den Vordergrund stellen
//                        for (Feature feature : featureCollection.getAllFeatures()) {
//                            if (feature instanceof SearchFeature) {
//                                PFeature pFeature = (PFeature)pFeatureHM.get(feature);
//                                pFeature.moveToFront();
//                            }
//                        }
//                    }
//                });
//            }
//        };
//        CismetThreadPool.execute(r);
//
//        //check whether the feature has a rasterSupportLayer or not
//        for (Feature f : features) {
//            if (f instanceof RasterLayerSupportedFeature && ((RasterLayerSupportedFeature) f).getSupportingRasterService() != null) {
//                FeatureAwareRasterService rs = ((RasterLayerSupportedFeature) f).getSupportingRasterService();
//                if (!getMappingModel().getRasterServices().containsValue(rs)) {
//                    if (DEBUG) {
//                        log.debug("FeatureAwareRasterServiceAdded");
//                    }
//                    rs.setFeatureCollection(getFeatureCollection());
//                    getMappingModel().addLayer(rs);
//                }
//            }
//        }
//
//        showHandles(false);
//    }

    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    private void fireFeaturesAddedToMap(final Collection<Feature> cf) {
        for (final MapListener curMapListener : mapListeners) {
            curMapListener.featuresAddedToMap(cf);
        }
    }

    /**
     * Returns a list of PFeatureCoordinatePositions which are located at the given coordinate.
     *
     * @param   features  c Coordinate
     *
     * @return  list of PFeatureCoordinatePositions
     */
// public List<PFeatureCoordinatePosition> getPFeaturesByCoordinates(Coordinate c) {
// List<PFeatureCoordinatePosition> l = (List<PFeatureCoordinatePosition>) pFeatureHMbyCoordinate.get(c);
// return l;
// }
    /**
     * Creates an envelope around all features from the given array.
     *
     * @param   features  features to create the envelope around
     *
     * @return  Envelope com.vividsolutions.jts.geom.Envelope
     */
    private com.vividsolutions.jts.geom.Envelope computeFeatureEnvelope(final Feature[] features) {
        final PNode root = new PNode();
        for (int i = 0; i < features.length; ++i) {
            final PNode p = PNodeFactory.createPFeature(features[i], this);
            if (p != null) {
                root.addChild(p);
            }
        }
        final PBounds ext = root.getFullBounds();
        final com.vividsolutions.jts.geom.Envelope env = new com.vividsolutions.jts.geom.Envelope(
                ext.x,
                ext.x
                        + ext.width,
                ext.y,
                ext.y
                        + ext.height);
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
     *
     * @param  mode    interactionmode as string
     * @param  cursor  cursor-object
     */
    public void putCursor(final String mode, final Cursor cursor) {
        cursors.put(mode, cursor);
    }

    /**
     * Returns the cursor assigned to the given mode.
     *
     * @param   mode  mode as String
     *
     * @return  Cursor-object or null
     */
    public Cursor getCursor(final String mode) {
        return cursors.get(mode);
    }

    /**
     * Adds a new PBasicInputEventHandler for a specific interactionmode.
     *
     * @param  mode      interactionmode as string
     * @param  listener  new PBasicInputEventHandler
     */
    public void addInputListener(final String mode, final PBasicInputEventHandler listener) {
        inputEventListener.put(mode, listener);
    }

    /**
     * Returns the PBasicInputEventHandler assigned to the committed interactionmode.
     *
     * @param   mode  interactionmode as string
     *
     * @return  PBasicInputEventHandler-object or null
     */
    public PInputEventListener getInputListener(final String mode) {
        final Object o = inputEventListener.get(mode);
        if (o instanceof PInputEventListener) {
            return (PInputEventListener)o;
        } else {
            return null;
        }
    }

    /**
     * Returns whether the features are editable or not.
     *
     * @return  DOCUMENT ME!
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets all Features ReadOnly use Feature.setEditable(boolean) instead.
     *
     * @param  readOnly  DOCUMENT ME!
     */
    public void setReadOnly(final boolean readOnly) {
        for (final Object f : featureCollection.getAllFeatures()) {
            ((Feature)f).setEditable(!readOnly);
        }
        this.readOnly = readOnly;
        handleLayer.repaint();

        // if (readOnly==false) {
        try {
            handleLayer.removeAllChildren();
        } catch (Exception e) {
            log.warn("Fehler bei removeAllCHildren", e); // NOI18N
        }
        snapHandleLayer.removeAllChildren();
//        }
    }

    /**
     * Returns the current HandleInteractionMode.
     *
     * @return  DOCUMENT ME!
     */
    public String getHandleInteractionMode() {
        return handleInteractionMode;
    }

    /**
     * Changes the HandleInteractionMode. Repaints handles.
     *
     * @param  handleInteractionMode  the new HandleInteractionMode
     */
    public void setHandleInteractionMode(final String handleInteractionMode) {
        this.handleInteractionMode = handleInteractionMode;
        showHandles(false);
    }

    /**
     * Returns whether the background is enabled or not.
     *
     * @return  DOCUMENT ME!
     */
    public boolean isBackgroundEnabled() {
        return backgroundEnabled;
    }

    /**
     * TODO.
     *
     * @param  backgroundEnabled  DOCUMENT ME!
     */
    public void setBackgroundEnabled(final boolean backgroundEnabled) {
        if ((backgroundEnabled == false) && (isBackgroundEnabled() == true)) {
            featureServiceLayerVisible = featureServiceLayer.getVisible();
        }

        this.mapServicelayer.setVisible(backgroundEnabled);
        this.featureServiceLayer.setVisible(backgroundEnabled && featureServiceLayerVisible);
        for (int i = 0; i < featureServiceLayer.getChildrenCount(); ++i) {
            featureServiceLayer.getChild(i).setVisible(backgroundEnabled && featureServiceLayerVisible);
        }

        if ((backgroundEnabled != isBackgroundEnabled()) && (isBackgroundEnabled() == false)) {
            this.queryServices();
        }
        this.backgroundEnabled = backgroundEnabled;
    }

    /**
     * Returns the featurelayer.
     *
     * @return  DOCUMENT ME!
     */
    public PLayer getFeatureLayer() {
        return featureLayer;
    }

    /**
     * Adds a PFeature to the PFeatureHashmap.
     *
     * @param  p  PFeature to add
     */
    public void refreshHM(final PFeature p) {
        pFeatureHM.put(p.getFeature(), p);
    }

    /**
     * Returns the selectedObjectPresenter (PCanvas).
     *
     * @return  DOCUMENT ME!
     */
    public PCanvas getSelectedObjectPresenter() {
        return selectedObjectPresenter;
    }

    /**
     * If f != null it calls the reconsiderFeature()-method of the featurecollection.
     *
     * @param  f  feature to reconcile
     */
    public void reconsiderFeature(final Feature f) {
        if (f != null) {
            featureCollection.reconsiderFeature(f);
        }
    }

    /**
     * Removes all features of the collection from the hashmap.
     *
     * @param  fc  collection of features to remove
     */
    public void removeFeatures(final Collection<Feature> fc) {
        featureLayer.setVisible(false);
        for (final Feature elem : fc) {
            removeFromHM(elem);
        }
        featureLayer.setVisible(true);
    }

    /**
     * Removes a Feature from the PFeatureHashmap. Uses the delivered feature as hashmap-key.
     *
     * @param  f  feature of the Pfeature that should be deleted
     */
    public void removeFromHM(final Feature f) {
        final PFeature pf = pFeatureHM.get(f);

        if (pf != null) {
            pf.cleanup();
            pFeatureHM.remove(f);
            stickyPNodes.remove(pf);
            try {
                log.info("Entferne Feature " + f);                                                 // NOI18N
                featureLayer.removeChild(pf);
            } catch (Exception ex) {
                if (DEBUG) {
                    if (log.isDebugEnabled()) {
                        log.debug("Remove Child ging Schief. Ist beim Splitten aber normal.", ex); // NOI18N
                    }
                }
            }
        } else {
            log.warn("Feature war nicht in pFeatureHM");                                           // NOI18N
        }
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("pFeatureHM" + pFeatureHM);                                              // NOI18N
            }
        }
    }

    /**
     * Zooms to the current selected node.
     *
     * @deprecated  DOCUMENT ME!
     */
    public void zoomToSelectedNode() {
        zoomToSelection();
    }

    /**
     * Zooms to the current selected features.
     */
    public void zoomToSelection() {
        final Collection<Feature> selection = featureCollection.getSelectedFeatures();
        zoomToAFeatureCollection(selection, true, false);
    }

    /**
     * Zooms to a specific featurecollection.
     *
     * @param  collection   the featurecolltion
     * @param  withHistory  should the zoomaction be undoable
     * @param  fixedScale   fixedScale
     */
    public void zoomToAFeatureCollection(final Collection<Feature> collection,
            final boolean withHistory,
            final boolean fixedScale) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("zoomToAFeatureCollection");   // NOI18N
            }
        }
        try {
            handleLayer.removeAllChildren();
        } catch (Exception e) {
            log.warn("Fehler bei removeAllCHildren", e); // NOI18N
        }
        boolean first = true;
        Geometry g = null;

        for (final Feature f : collection) {
            if (first) {
                if (f.getGeometry() != null) {
                    g = CrsTransformer.transformToGivenCrs(f.getGeometry(), mappingModel.getSrs().getCode())
                                .getEnvelope();
                    if ((f instanceof Bufferable) && mappingModel.getSrs().isMetric()) {
                        g = g.buffer(((Bufferable)f).getBuffer());
                    }
                    first = false;
                }
            } else {
                if (f.getGeometry() != null) {
                    Geometry geometry = CrsTransformer.transformToGivenCrs(f.getGeometry(),
                                mappingModel.getSrs().getCode())
                                .getEnvelope();
                    if ((f instanceof Bufferable) && mappingModel.getSrs().isMetric()) {
                        geometry = geometry.buffer(((Bufferable)f).getBuffer());
                    }
                    g = g.getEnvelope().union(geometry);
                }
            }
        }

        if (g != null) {
            // dreisatz.de
            final double hBuff = g.getEnvelopeInternal().getHeight() / ((double)getHeight()) * 10;
            final double vBuff = g.getEnvelopeInternal().getWidth() / ((double)getWidth()) * 10;
            if ((getHeight() == 0) || (getWidth() == 0)) {
                log.fatal("DIVISION BY ZERO"); // NOI18N
            }

            double buff = 0;
            if (hBuff > vBuff) {
                buff = hBuff;
            } else {
                buff = vBuff;
            }

            if (buff == 0.0) {
                if (mappingModel.getSrs().isMetric()) {
                    buff = 1.0;
                } else {
                    buff = 0.01;
                }
            }

            g = g.buffer(buff);
            final BoundingBox bb = new BoundingBox(g);
            gotoBoundingBox(bb, withHistory, !fixedScale, animationDuration);
        }
    }

    /**
     * Deletes all present handles from the handlelayer. Tells all selected features in the featurecollection to create
     * their handles and to add them to the handlelayer.
     *
     * @param  waitTillAllAnimationsAreComplete  wait until all animations are completed before create the handles
     */
    public void showHandles(final boolean waitTillAllAnimationsAreComplete) {
        // are there features selected?
        if (featureCollection.getSelectedFeatures().size() > 0) {
            // DANGER Mehrfachzeichnen von Handles durch parallelen Aufruf
            final Runnable handle = new Runnable() {

                    @Override
                    public void run() {
                        // alle bisherigen Handles entfernen
                        EventQueue.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        handleLayer.removeAllChildren();
                                    } catch (Exception e) {
                                        log.warn("Fehler bei removeAllCHildren", e); // NOI18N
                                    }
                                }
                            });
                        while (getAnimating() && waitTillAllAnimationsAreComplete) {
                            try {
                                Thread.currentThread().sleep(10);
                            } catch (Exception e) {
                                log.warn("Unterbrechung bei getAnimating()", e);     // NOI18N
                            }
                        }
                        if (featureCollection.areFeaturesEditable()
                                    && (getInteractionMode().equals(SELECT)
                                        || getInteractionMode().equals(LINEMEASUREMENT)
                                        || getInteractionMode().equals(PAN)
                                        || getInteractionMode().equals(ZOOM)
                                        || getInteractionMode().equals(ALKIS_PRINT)
                                        || getInteractionMode().equals(SPLIT_POLYGON))) {
                            // Handles für alle selektierten Features der Collection hinzufügen
                            if (getHandleInteractionMode().equals(ROTATE_POLYGON)) {
                                final LinkedHashSet<Feature> copy = new LinkedHashSet(
                                        featureCollection.getSelectedFeatures());
                                for (final Feature selectedFeature : copy) {
                                    if ((selectedFeature instanceof Feature)
                                                && ((Feature)selectedFeature).isEditable()) {
                                        if (pFeatureHM.get(selectedFeature) != null) {
                                            // manipulates gui -> edt
                                            EventQueue.invokeLater(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        ((PFeature)pFeatureHM.get(selectedFeature)).addRotationHandles(
                                                            handleLayer);
                                                    }
                                                });
                                        } else {
                                            log.warn("pFeatureHM.get(" + selectedFeature + ")==null"); // NOI18N
                                        }
                                    }
                                }
                            } else {
                                final LinkedHashSet<Feature> copy = new LinkedHashSet(
                                        featureCollection.getSelectedFeatures());
                                for (final Feature selectedFeature : copy) {
                                    if ((selectedFeature != null) && selectedFeature.isEditable()) {
                                        if (pFeatureHM.get(selectedFeature) != null) {
                                            // manipulates gui -> edt
                                            EventQueue.invokeLater(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        try {
                                                            ((PFeature)pFeatureHM.get(selectedFeature)).addHandles(
                                                                handleLayer);
                                                        } catch (Exception e) {
                                                            log.error("Error bei addHandles: ", e); // NOI18N
                                                        }
                                                    }
                                                });
                                        } else {
                                            log.warn("pFeatureHM.get(" + selectedFeature + ")==null"); // NOI18N
                                        }
                                        // DANGER mit break werden nur die Handles EINES slektierten Features angezeigt
                                        // wird break auskommentiert werden jedoch zu viele Handles angezeigt break;
                                    }
                                }
                            }
                        }
                    }
                };

            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("showHandles", new CurrentStackTrace()); // NOI18N
                }
            }
            CismetThreadPool.execute(handle);
        } else {
            // alle bisherigen Handles entfernen
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            handleLayer.removeAllChildren();
                        } catch (Exception e) {
                            log.warn("Fehler bei removeAllCHildren", e); // NOI18N
                        }
                    }
                });
        }
    }

    /**
     * Will return a PureNewFeature if there is only one in the featurecollection else null.
     *
     * @return  DOCUMENT ME!
     */
    public PFeature getSolePureNewFeature() {
        int counter = 0;
        PFeature sole = null;
        for (final Iterator it = featureLayer.getChildrenIterator(); it.hasNext();) {
            final Object o = it.next();
            if (o instanceof PFeature) {
                if (((PFeature)o).getFeature() instanceof PureNewFeature) {
                    ++counter;
                    sole = ((PFeature)o);
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
     *
     * @param  visible  true sets visible
     */
    private void setRasterServiceLayerImagesVisibility(final boolean visible) {
        final Iterator it = mapServicelayer.getChildrenIterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if (o instanceof XPImage) {
                ((XPImage)o).setVisible(visible);
            }
        }
    }

    /**
     * Returns the temporary featurelayer.
     *
     * @return  DOCUMENT ME!
     */
    public PLayer getTmpFeatureLayer() {
        return tmpFeatureLayer;
    }

    /**
     * Assigns a new temporary featurelayer.
     *
     * @param  tmpFeatureLayer  PLayer
     */
    public void setTmpFeatureLayer(final PLayer tmpFeatureLayer) {
        this.tmpFeatureLayer = tmpFeatureLayer;
    }

    /**
     * Returns whether the grid is enabled or not.
     *
     * @return  DOCUMENT ME!
     */
    public boolean isGridEnabled() {
        return gridEnabled;
    }

    /**
     * Enables or disables the grid.
     *
     * @param  gridEnabled  true, to enable the grid
     */
    public void setGridEnabled(final boolean gridEnabled) {
        this.gridEnabled = gridEnabled;
    }

    /**
     * Returns a String from two double-values. Serves the visualization.
     *
     * @param   x  X-coordinate
     * @param   y  Y-coordinate
     *
     * @return  a Strin-object like "(X,Y)"
     */
    public static String getCoordinateString(final double x, final double y) {
        final DecimalFormat df = new DecimalFormat("0.00");   // NOI18N
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(dfs);
        return "(" + df.format(x) + "," + df.format(y) + ")"; // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   event  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public com.vividsolutions.jts.geom.Point getPointGeometryFromPInputEvent(final PInputEvent event) {
        final double xCoord = getWtst().getSourceX(event.getPosition().getX() - getClip_offset_x());
        final double yCoord = getWtst().getSourceY(event.getPosition().getY() - getClip_offset_y());
        final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                CrsTransformer.extractSridFromCrs(getMappingModel().getSrs().getCode()));
        return gf.createPoint(new Coordinate(xCoord, yCoord));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PLayer getHandleLayer() {
        return handleLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  handleLayer  DOCUMENT ME!
     */
    public void setHandleLayer(final PLayer handleLayer) {
        this.handleLayer = handleLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isVisualizeSnappingEnabled() {
        return visualizeSnappingEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visualizeSnappingEnabled  DOCUMENT ME!
     */
    public void setVisualizeSnappingEnabled(final boolean visualizeSnappingEnabled) {
        this.visualizeSnappingEnabled = visualizeSnappingEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isVisualizeSnappingRectEnabled() {
        return visualizeSnappingRectEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visualizeSnappingRectEnabled  DOCUMENT ME!
     */
    public void setVisualizeSnappingRectEnabled(final boolean visualizeSnappingRectEnabled) {
        this.visualizeSnappingRectEnabled = visualizeSnappingRectEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getSnappingRectSize() {
        return snappingRectSize;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  snappingRectSize  DOCUMENT ME!
     */
    public void setSnappingRectSize(final int snappingRectSize) {
        this.snappingRectSize = snappingRectSize;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PLayer getSnapHandleLayer() {
        return snapHandleLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  snapHandleLayer  DOCUMENT ME!
     */
    public void setSnapHandleLayer(final PLayer snapHandleLayer) {
        this.snapHandleLayer = snapHandleLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSnappingEnabled() {
        return snappingEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  snappingEnabled  DOCUMENT ME!
     */
    public void setSnappingEnabled(final boolean snappingEnabled) {
        this.snappingEnabled = snappingEnabled;
        setVisualizeSnappingEnabled(snappingEnabled);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PLayer getFeatureServiceLayer() {
        return featureServiceLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  featureServiceLayer  DOCUMENT ME!
     */
    public void setFeatureServiceLayer(final PLayer featureServiceLayer) {
        this.featureServiceLayer = featureServiceLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getAnimationDuration() {
        return animationDuration;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  animationDuration  DOCUMENT ME!
     */
    public void setAnimationDuration(final int animationDuration) {
        this.animationDuration = animationDuration;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  prefs  DOCUMENT ME!
     */
    @Deprecated
    public void setPreferences(final CismapPreferences prefs) {
        log.warn("involing deprecated operation setPreferences()"); // NOI18N
        cismapPrefs = prefs;
        // DefaultMappingModel mm = new DefaultMappingModel();
        final ActiveLayerModel mm = new ActiveLayerModel();
        final LayersPreferences layersPrefs = prefs.getLayersPrefs();
        final GlobalPreferences globalPrefs = prefs.getGlobalPrefs();

        setSnappingRectSize(globalPrefs.getSnappingRectSize());
        setSnappingEnabled(globalPrefs.isSnappingEnabled());
        setVisualizeSnappingEnabled(globalPrefs.isSnappingPreviewEnabled());
        setAnimationDuration(globalPrefs.getAnimationDuration());
        setInteractionMode(globalPrefs.getStartMode());
        // mm.setInitialBoundingBox(globalPrefs.getInitialBoundingBox());
        mm.addHome(globalPrefs.getInitialBoundingBox());
        final Crs crs = new Crs();
        crs.setCode(globalPrefs.getInitialBoundingBox().getSrs());
        crs.setName(globalPrefs.getInitialBoundingBox().getSrs());
        crs.setShortname(globalPrefs.getInitialBoundingBox().getSrs());
        mm.setSrs(crs);

        final TreeMap raster = layersPrefs.getRasterServices();
        if (raster != null) {
            final Iterator it = raster.keySet().iterator();
            while (it.hasNext()) {
                final Object key = it.next();
                final Object o = raster.get(key);
                if (o instanceof MapService) {
                    // mm.putMapService(((Integer) key).intValue(), (MapService) o);
                    mm.addLayer((RetrievalServiceLayer)o);
                }
            }
        }
        final TreeMap features = layersPrefs.getFeatureServices();
        if (features != null) {
            final Iterator it = features.keySet().iterator();
            while (it.hasNext()) {
                final Object key = it.next();
                final Object o = features.get(key);
                if (o instanceof MapService) {
                    // TODO
                    // mm.putMapService(((Integer) key).intValue(), (MapService) o);
                    mm.addLayer((RetrievalServiceLayer)o);
                }
            }
        }
        setMappingModel(mm);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CismapPreferences getCismapPrefs() {
        return cismapPrefs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  duration           DOCUMENT ME!
     * @param  animationDuration  DOCUMENT ME!
     * @param  what               DOCUMENT ME!
     * @param  number             DOCUMENT ME!
     */
    public void flash(final int duration, final int animationDuration, final int what, final int number) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PLayer getDragPerformanceImproverLayer() {
        return dragPerformanceImproverLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dragPerformanceImproverLayer  DOCUMENT ME!
     */
    public void setDragPerformanceImproverLayer(final PLayer dragPerformanceImproverLayer) {
        this.dragPerformanceImproverLayer = dragPerformanceImproverLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Deprecated
    public PLayer getRasterServiceLayer() {
        return mapServicelayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PLayer getMapServiceLayer() {
        return mapServicelayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  rasterServiceLayer  DOCUMENT ME!
     */
    public void setRasterServiceLayer(final PLayer rasterServiceLayer) {
        this.mapServicelayer = rasterServiceLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  f  DOCUMENT ME!
     */
    public void showGeometryInfoPanel(final Feature f) {
    }

    /**
     * Adds a PropertyChangeListener to the listener list.
     *
     * @param  l  The listener to add.
     */
    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param  l  The listener to remove.
     */
    @Override
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Setter for property taskCounter. former synchronized method
     */
    public void fireActivityChanged() {
        propertyChangeSupport.firePropertyChange("activityChanged", null, null); // NOI18N
    }

    /**
     * Returns true, if there's still one layercontrol running. Else false; former synchronized method
     *
     * @return  DOCUMENT ME!
     */
    public boolean isRunning() {
        for (final LayerControl lc : layerControls) {
            if (lc.isRunning()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the visibility of all infonodes.
     *
     * @param  visible  true, if infonodes should be visible
     */
    public void setInfoNodesVisible(final boolean visible) {
        infoNodesVisible = visible;
        for (final Iterator it = featureLayer.getChildrenIterator(); it.hasNext();) {
            final Object elem = it.next();
            if (elem instanceof PFeature) {
                ((PFeature)elem).setInfoNodeVisible(visible);
            }
        }
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("setInfoNodesVisible()"); // NOI18N
            }
        }
        rescaleStickyNodes();
    }

    /**
     * Adds an object to the historymodel.
     *
     * @param  o  Object to add
     */
    @Override
    public void addToHistory(final Object o) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("addToHistory:" + o.toString()); // NOI18N
            }
        }
        historyModel.addToHistory(o);
    }

    /**
     * Removes a specific HistoryModelListener from the historymodel.
     *
     * @param  hml  HistoryModelListener
     */
    @Override
    public void removeHistoryModelListener(final de.cismet.tools.gui.historybutton.HistoryModelListener hml) {
        historyModel.removeHistoryModelListener(hml);
    }

    /**
     * Adds aHistoryModelListener to the historymodel.
     *
     * @param  hml  HistoryModelListener
     */
    @Override
    public void addHistoryModelListener(final de.cismet.tools.gui.historybutton.HistoryModelListener hml) {
        historyModel.addHistoryModelListener(hml);
    }

    /**
     * Sets the maximum value of saved historyactions.
     *
     * @param  max  new integer value
     */
    @Override
    public void setMaximumPossibilities(final int max) {
        historyModel.setMaximumPossibilities(max);
    }

    /**
     * Redos the last undone historyaction.
     *
     * @param   external  true, if fireHistoryChanged-action should be fired
     *
     * @return  PBounds of the forward-action
     */
    @Override
    public Object forward(final boolean external) {
        final PBounds fwd = (PBounds)historyModel.forward(external);
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("HistoryModel.forward():" + fwd); // NOI18N
            }
        }
        if (external) {
            this.gotoBoundsWithoutHistory(fwd);
        }
        return fwd;
    }

    /**
     * Undos the last action.
     *
     * @param   external  true, if fireHistoryChanged-action should be fired
     *
     * @return  PBounds of the back-action
     */
    @Override
    public Object back(final boolean external) {
        final PBounds back = (PBounds)historyModel.back(external);
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("HistoryModel.back():" + back); // NOI18N
            }
        }
        if (external) {
            this.gotoBoundsWithoutHistory(back);
        }
        return back;
    }

    /**
     * Returns true, if it's possible to redo an action.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isForwardPossible() {
        return historyModel.isForwardPossible();
    }

    /**
     * Returns true, if it's possible to undo an action.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isBackPossible() {
        return historyModel.isBackPossible();
    }

    /**
     * Returns a vector with all redo-possibilities.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Vector getForwardPossibilities() {
        return historyModel.getForwardPossibilities();
    }

    /**
     * Returns the current element of the historymodel.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object getCurrentElement() {
        return historyModel.getCurrentElement();
    }

    /**
     * Returns a vector with all undo-possibilities.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Vector getBackPossibilities() {
        return historyModel.getBackPossibilities();
    }

    /**
     * Returns whether an internallayerwidget is available.
     *
     * @return  DOCUMENT ME!
     */
    @Deprecated
    public boolean isInternalLayerWidgetAvailable() {
        return this.getInternalWidget(LAYERWIDGET) != null;
    }

    /**
     * Sets the variable, if an internallayerwidget is available or not.
     *
     * @param  internalLayerWidgetAvailable  true, if available
     */
    @Deprecated
    public void setInternalLayerWidgetAvailable(final boolean internalLayerWidgetAvailable) {
        if (!internalLayerWidgetAvailable && (this.getInternalWidget(LAYERWIDGET) != null)) {
            this.removeInternalWidget(LAYERWIDGET);
        } else if (internalLayerWidgetAvailable && (this.getInternalWidget(LAYERWIDGET) == null)) {
            final NewSimpleInternalLayerWidget simpleInternalLayerWidget = new NewSimpleInternalLayerWidget(
                    MappingComponent.this);
            MappingComponent.this.addInternalWidget(
                LAYERWIDGET,
                MappingComponent.POSITION_SOUTHEAST,
                simpleInternalLayerWidget);
        }
    }

    @Override
    public void mapServiceLayerStructureChanged(final de.cismet.cismap.commons.MappingModelEvent mme) {
    }

    /**
     * Removes the mapservice from the rasterservicelayer.
     *
     * @param  rasterService  the removing mapservice
     */
    @Override
    public void mapServiceRemoved(final MapService rasterService) {
        try {
            mapServicelayer.removeChild(rasterService.getPNode());
            System.gc();
        } catch (Exception e) {
            log.warn("Fehler bei mapServiceRemoved", e); // NOI18N
        }
    }

    /**
     * Adds the commited mapservice on the last position to the rasterservicelayer.
     *
     * @param  mapService  the new mapservice
     */
    @Override
    public void mapServiceAdded(final MapService mapService) {
        addMapService(mapService, mapServicelayer.getChildrenCount());
        if (mapService instanceof FeatureAwareRasterService) {
            ((FeatureAwareRasterService)mapService).setFeatureCollection(getFeatureCollection());
        }
        if ((mapService instanceof ServiceLayer) && ((ServiceLayer)mapService).isEnabled()) {
            handleMapService(0, mapService, false);
        }
    }

    /**
     * Returns the current OGC scale.
     *
     * @return  DOCUMENT ME!
     */
    public double getCurrentOGCScale() {
        // funktioniert nur bei metrischen SRS's
        final double h = getCamera().getViewBounds().getHeight() / getHeight();
        final double w = getCamera().getViewBounds().getWidth() / getWidth();
//        if(DEBUG)log.debug("H�he:"+getHeight()+" Breite:"+getWidth());
//        if(DEBUG)log.debug("H�heReal:"+getCamera().getViewBounds().getHeight()+" BreiteReal:"+getCamera().getViewBounds().getWidth());
        return Math.sqrt((h * h) + (w * w)); // Math.sqrt((getWidth()*getWidth())*2);
    }

    /**
     * Returns the current BoundingBox.
     *
     * @return  DOCUMENT ME!
     */
    public BoundingBox getCurrentBoundingBox() {
        if (fixedBoundingBox != null) {
            return fixedBoundingBox;
        } else {
            try {
                final PBounds bounds = getCamera().getViewBounds();
                final double x1 = wtst.getWorldX(bounds.getX());
                final double y1 = wtst.getWorldY(bounds.getY());
                final double x2 = x1 + bounds.width;
                final double y2 = y1 - bounds.height;

                final Crs currentCrs = CismapBroker.getInstance().getSrs();
                final boolean metric;
                // FIXME: this is a hack to overcome the "metric" issue for 4326 default srs
                if (CrsTransformer.getCurrentSrid() == 4326) {
                    metric = false;
                } else {
                    metric = currentCrs.isMetric();
                }

                currentBoundingBox = new XBoundingBox(x1, y1, x2, y2, currentCrs.getCode(), metric);

                return currentBoundingBox;
            } catch (Exception e) {
                log.error("cannot create bounding box from current view, return null", e); // NOI18N

                return null;
            }
        }
    }

    /**
     * Returns a BoundingBox with a fixed size.
     *
     * @return  DOCUMENT ME!
     */
    public BoundingBox getFixedBoundingBox() {
        return fixedBoundingBox;
    }

    /**
     * Assigns fixedBoundingBox a new value.
     *
     * @param  fixedBoundingBox  new boundingbox
     */
    public void setFixedBoundingBox(final BoundingBox fixedBoundingBox) {
        this.fixedBoundingBox = fixedBoundingBox;
    }

    /**
     * Paints the outline of the forwarded BoundingBox.
     *
     * @param  bb  BoundingBox
     */
    public void outlineArea(final BoundingBox bb) {
        outlineArea(bb, null);
    }

    /**
     * Paints the outline of the forwarded PBounds.
     *
     * @param  b  PBounds
     */
    public void outlineArea(final PBounds b) {
        outlineArea(b, null);
    }

    /**
     * Paints a filled rectangle of the area of the forwarded BoundingBox.
     *
     * @param  bb            BoundingBox
     * @param  fillingPaint  Color to fill the rectangle
     */
    public void outlineArea(final BoundingBox bb, final Paint fillingPaint) {
        PBounds pb = null;
        if (bb != null) {
            pb = new PBounds(wtst.getScreenX(bb.getX1()),
                    wtst.getScreenY(bb.getY2()),
                    bb.getX2()
                            - bb.getX1(),
                    bb.getY2()
                            - bb.getY1());
        }
        outlineArea(pb, fillingPaint);
    }

    /**
     * Paints a filled rectangle of the area of the forwarded PBounds.
     *
     * @param  b             PBounds to paint
     * @param  fillingColor  Color to fill the rectangle
     */
    public void outlineArea(final PBounds b, final Paint fillingColor) {
        if (b == null) {
            if (highlightingLayer.getChildrenCount() > 0) {
                highlightingLayer.removeAllChildren();
            }
        } else {
            highlightingLayer.removeAllChildren();
            highlightingLayer.setTransparency(1);
            final PPath rectangle = new PPath();
            rectangle.setPaint(fillingColor);
            rectangle.setStroke(new FixedWidthStroke());
            rectangle.setStrokePaint(new Color(100, 100, 100, 255));
            rectangle.setPathTo(b);
            highlightingLayer.addChild(rectangle);
        }
    }

    /**
     * Highlights the delivered BoundingBox. Calls highlightArea(PBounds b) internally.
     *
     * @param  bb  BoundingBox to highlight
     */
    public void highlightArea(final BoundingBox bb) {
        PBounds pb = null;
        if (bb != null) {
            pb = new PBounds(wtst.getScreenX(bb.getX1()),
                    wtst.getScreenY(bb.getY2()),
                    bb.getX2()
                            - bb.getX1(),
                    bb.getY2()
                            - bb.getY1());
        }
        highlightArea(pb);
    }

    /**
     * Highlights the delivered PBounds by painting over with a transparent white.
     *
     * @param  b  PBounds to hightlight
     */
    private void highlightArea(final PBounds b) {
        if (b == null) {
            if (highlightingLayer.getChildrenCount() > 0) {
            }
            highlightingLayer.animateToTransparency(0, animationDuration);
            highlightingLayer.removeAllChildren();
        } else {
            highlightingLayer.removeAllChildren();
            highlightingLayer.setTransparency(1);
            final PPath rectangle = new PPath();
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
     * Paints a crosshair at the delivered coordinate. Calculates a Point from the coordinate and calls
     * crossHairPoint(Point p) internally.
     *
     * @param  c  coordinate of the crosshair's venue
     */
    public void crossHairPoint(final Coordinate c) {
        Point p = null;
        if (c != null) {
            p = new Point((int)wtst.getScreenX(c.x), (int)wtst.getScreenY(c.y));
        }
        crossHairPoint(p);
    }

    /**
     * Paints a crosshair at the delivered point.
     *
     * @param  p  point of the crosshair's venue
     */
    public void crossHairPoint(final Point p) {
        if (p == null) {
            if (crosshairLayer.getChildrenCount() > 0) {
                crosshairLayer.removeAllChildren();
            }
        } else {
            crosshairLayer.removeAllChildren();
            crosshairLayer.setTransparency(1);
            final PPath lineX = new PPath();
            final PPath lineY = new PPath();
            lineX.setStroke(new FixedWidthStroke());
            lineX.setStrokePaint(new Color(100, 100, 100, 255));
            lineY.setStroke(new FixedWidthStroke());
            lineY.setStrokePaint(new Color(100, 100, 100, 255));

            // PBounds current=getCurrentBoundingBox().getPBounds(getWtst());
            final PBounds current = getCamera().getViewBounds();
            final PBounds x = new PBounds(PBounds.OUT_LEFT - current.width, p.y, 2 * current.width, 1);
            final PBounds y = new PBounds(p.x, PBounds.OUT_TOP - current.height, 1, current.height * 2);
            lineX.setPathTo(x);
            crosshairLayer.addChild(lineX);
            lineY.setPathTo(y);
            crosshairLayer.addChild(lineY);
        }
    }

    @Override
    public Element getConfiguration() {
        if (log.isDebugEnabled()) {
            log.debug("writing configuration <cismapMappingPreferences>");                          // NOI18N
        }
        final Element ret = new Element("cismapMappingPreferences");                                // NOI18N
        ret.setAttribute("interactionMode", getInteractionMode());                                  // NOI18N
        ret.setAttribute(
            "creationMode",
            ((CreateNewGeometryListener)getInputListener(MappingComponent.NEW_POLYGON)).getMode()); // NOI18N
        ret.setAttribute("handleInteractionMode", getHandleInteractionMode());                      // NOI18N
        ret.setAttribute("snapping", new Boolean(isSnappingEnabled()).toString());                  // NOI18N

        // Position
        final Element currentPosition = new Element("Position"); // NOI18N
//        if (Double.isNaN(getCurrentBoundingBox().getX1())||
//                Double.isNaN(getCurrentBoundingBox().getX2())||
//                Double.isNaN(getCurrentBoundingBox().getY1())||
//                Double.isNaN(getCurrentBoundingBox().getY2()))        {
//            log.warn("BUGFINDER:Es war ein Wert in der BoundingBox NaN. Setze auf HOME");
//            gotoInitialBoundingBox();
//        }
        currentPosition.addContent(currentBoundingBox.getJDOMElement());
        currentPosition.setAttribute("CRS", mappingModel.getSrs().getCode());
        // currentPosition.addContent(getCurrentBoundingBox().getJDOMElement());
        ret.addContent(currentPosition);

        // Crs
        final Element crsListElement = new Element("crsList");

        for (final Crs tmp : crsList) {
            crsListElement.addContent(tmp.getJDOMElement());
        }

        ret.addContent(crsListElement);
        if (printingSettingsDialog != null) {
            ret.addContent(printingSettingsDialog.getConfiguration());
        }

        // save internal widgets status
        final Element widgets = new Element("InternalWidgets");                                       // NOI18N
        for (final String name : this.internalWidgets.keySet()) {
            final Element widget = new Element("Widget");                                             // NOI18N
            widget.setAttribute("name", name);                                                        // NOI18N
            widget.setAttribute("position", String.valueOf(this.internalWidgetPositions.get(name)));  // NOI18N
            widget.setAttribute("visible", String.valueOf(this.getInternalWidget(name).isVisible())); // NOI18N
            widgets.addContent(widget);
        }

        ret.addContent(widgets);
        return ret;
    }

    @Override
    public void masterConfigure(final Element e) {
        final Element prefs = e.getChild("cismapMappingPreferences"); // NOI18N
        // CRS List
        try {
            final List crsElements = prefs.getChild("crsList").getChildren("crs"); // NOI18N
            boolean defaultCrsFound = false;
            crsList.clear();

            for (final Object elem : crsElements) {
                if (elem instanceof Element) {
                    final Crs s = new Crs((Element)elem);
                    crsList.add(s);

                    if (s.isSelected() && s.isMetric()) {
                        try {
                            if (defaultCrsFound) {
                                log.warn("More than one default CRS is set. "
                                            + "Please check your master configuration file."); // NOI18N
                            }
                            CismapBroker.getInstance().setDefaultCrs(s.getCode());
                            defaultCrsFound = true;
                            transformer = new CrsTransformer(s.getCode());
                        } catch (Exception ex) {
                            log.error("Cannot create a GeoTransformer for the crs " + s.getCode(), ex);
                        }
                    }
                }
            }
        } catch (Exception skip) {
            log.error("Error while reading the crs list", skip);                               // NOI18N
        }

        if (CismapBroker.getInstance().getDefaultCrs() == null) {
            log.fatal("The default CRS is not set. This can lead to almost irreparable data errors. "
                        + "Keep in mind: The default CRS must be metric"); // NOI18N
        }

        if (transformer == null) {
            log.error("No metric default crs found. Use EPSG:31466 as default crs"); // NOI18N

            try {
                transformer = new CrsTransformer("EPSG:31466");                         // NOI18N
                CismapBroker.getInstance().setDefaultCrs("EPSG:31466");                 // NOI18N
            } catch (Exception ex) {
                log.error("Cannot create a GeoTransformer for the crs EPSG:31466", ex); // NOI18N
            }
        }

        // HOME
        try {
            if (getMappingModel() instanceof ActiveLayerModel) {
                final ActiveLayerModel alm = (ActiveLayerModel)getMappingModel();
                final Iterator<Element> it = prefs.getChildren("home").iterator();                        // NOI18N
                if (DEBUG) {
                    if (log.isDebugEnabled()) {
                        log.debug("Es gibt " + prefs.getChildren("home").size() + " Home Einstellungen"); // NOI18N
                    }
                }
                while (it.hasNext()) {
                    final Element elem = it.next();
                    final String srs = elem.getAttribute("srs").getValue();                               // NOI18N
                    boolean metric = false;
                    try {
                        metric = elem.getAttribute("metric").getBooleanValue();                           // NOI18N
                    } catch (DataConversionException dce) {
                        log.warn("Metric hat falschen Syntax", dce);                                      // NOI18N
                    }
                    boolean defaultVal = false;
                    try {
                        defaultVal = elem.getAttribute("default").getBooleanValue();                      // NOI18N
                    } catch (DataConversionException dce) {
                        log.warn("default hat falschen Syntax", dce);                                     // NOI18N
                    }
                    final XBoundingBox xbox = new XBoundingBox(elem, srs, metric);

                    alm.addHome(xbox);
                    if (defaultVal) {
                        Crs crsObject = null;

                        for (final Crs tmp : crsList) {
                            if (tmp.getCode().equals(srs)) {
                                crsObject = tmp;
                                break;
                            }
                        }
                        if (crsObject == null) {
                            log.error("CRS " + srs + " from the default home is not found in the crs list");
                            crsObject = new Crs(srs, srs, srs, true, false);
                            crsList.add(crsObject);
                        }

                        alm.setSrs(crsObject);
                        alm.setDefaultHomeSrs(crsObject);
                        CismapBroker.getInstance().setSrs(crsObject);
                        wtst = null;
                        getWtst();
                    }
                }
            }
        } catch (Throwable t) {
            log.error("Fehler beim MasterConfigure der MappingComponent", t); // NOI18N
        }

        try {
            final Element defaultCrs = prefs.getChild("defaultCrs");
            final int defaultCrsInt = Integer.parseInt(defaultCrs.getAttributeValue("geometrySridAlias"));
            CismapBroker.getInstance().setDefaultCrsAlias(defaultCrsInt);
        } catch (Exception ex) {
            log.error("Error while reading the default crs alias from the master configuration file.", ex);
        }

        try {
            final List scalesList = prefs.getChild("printing").getChildren("scale"); // NOI18N
            scales.clear();

            for (final Object elem : scalesList) {
                if (elem instanceof Element) {
                    final Scale s = new Scale((Element)elem);
                    scales.add(s);
                }
            }
        } catch (Exception skip) {
            log.error("Fehler beim Lesen von Scale", skip); // NOI18N
        }

        // Und jetzt noch die PriningEinstellungen
        initPrintingDialogs();
        printingSettingsDialog.masterConfigure(prefs);
    }

    /**
     * Configurates this MappingComponent.
     *
     * @param  e  JDOM-Element with configuration
     */
    @Override
    public void configure(final Element e) {
        final Element prefs = e.getChild("cismapMappingPreferences"); // NOI18N

        try {
            final List crsElements = prefs.getChild("crsList").getChildren("crs"); // NOI18N

            for (final Object elem : crsElements) {
                if (elem instanceof Element) {
                    final Crs s = new Crs((Element)elem);
                    // the crs is equals to an other crs, if the code is equal. If a crs has in the
                    // local configuration file an other name than in the master configuration file,
                    // the old crs will be removed and the local one should be added to use the
                    // local name and short name of the crs.
                    if (crsList.contains(s)) {
                        crsList.remove(s);
                    }
                    crsList.add(s);
                }
            }
        } catch (Exception skip) {
            log.error("Error while reading the crs list", skip); // NOI18N
        }

        // InteractionMode
        try {
            final String interactMode = prefs.getAttribute("interactionMode").getValue();          // NOI18N
            setInteractionMode(interactMode);
            if (interactMode.equals(MappingComponent.NEW_POLYGON)) {
                try {
                    final String creationMode = prefs.getAttribute("creationMode").getValue();     // NOI18N
                    ((CreateNewGeometryListener)getInputListener(MappingComponent.NEW_POLYGON)).setMode(creationMode);
                } catch (Throwable t) {
                    log.warn("Fehler beim Setzen des CreationInteractionMode", t);                 // NOI18N
                }
            }
        } catch (Throwable t) {
            log.warn("Fehler beim Setzen des InteractionMode", t);                                 // NOI18N
        }
        try {
            final String handleInterMode = prefs.getAttribute("handleInteractionMode").getValue(); // NOI18N
            setHandleInteractionMode(handleInterMode);
        } catch (Throwable t) {
            log.warn("Fehler beim Setzen des HandleInteractionMode", t);                           // NOI18N
        }
        try {
            final boolean snapping = prefs.getAttribute("snapping").getBooleanValue();             // NOI18N
            log.info("snapping=" + snapping);                                                      // NOI18N

            setSnappingEnabled(snapping);
            setVisualizeSnappingEnabled(snapping);
            setInGlueIdenticalPointsMode(snapping);
        } catch (Throwable t) {
            log.warn("Fehler beim setzen von snapping und Konsorten", t); // NOI18N
        }

        // aktuelle Position
        try {
            final Element pos = prefs.getChild("Position");                                    // NOI18N
            final BoundingBox b = new BoundingBox(pos);
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("Position:" + b);                                                // NOI18N
                }
            }
            final PBounds pb = b.getPBounds(getWtst());
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("PositionPb:" + pb);                                             // NOI18N
                }
            }
            if (Double.isNaN(b.getX1())
                        || Double.isNaN(b.getX2())
                        || Double.isNaN(b.getY1())
                        || Double.isNaN(b.getY2())) {
                log.fatal("BUGFINDER:Es war ein Wert in der BoundingBox NaN. Setze auf HOME"); // NOI18N
//                gotoInitialBoundingBox();
                this.currentBoundingBox = getMappingModel().getInitialBoundingBox();
                final String crsCode = ((pos.getAttribute("CRS") != null) ? pos.getAttribute("CRS").getValue() : null);
                addToHistory(new PBoundsWithCleverToString(
                        new PBounds(currentBoundingBox.getPBounds(wtst)),
                        wtst,
                        crsCode));
            } else {
                // set the current crs
                final Attribute crsAtt = pos.getAttribute("CRS");
                if (crsAtt != null) {
                    final String currentCrs = crsAtt.getValue();
                    Crs crsObject = null;

                    for (final Crs tmp : crsList) {
                        if (tmp.getCode().equals(currentCrs)) {
                            crsObject = tmp;
                            break;
                        }
                    }

                    if (crsObject == null) {
                        log.error("CRS " + currentCrs + " from the position element is not found in the crs list");
                    }

                    final ActiveLayerModel alm = (ActiveLayerModel)getMappingModel();
                    if (alm instanceof ActiveLayerModel) {
                        alm.setSrs(crsObject);
                    }
                    CismapBroker.getInstance().setSrs(crsObject);
                    wtst = null;
                    getWtst();
                }

                this.currentBoundingBox = b;
                if (DEBUG) {
                    if (log.isDebugEnabled()) {
                        log.debug("added to History" + b); // NOI18N
                    }
                }

                final String crsCode = ((pos.getAttribute("CRS") != null) ? pos.getAttribute("CRS").getValue() : null);
//                addToHistory(new PBoundsWithCleverToString(new PBounds(currentBoundingBox.getPBounds(wtst)), wtst, crsCode ));
//                final BoundingBox bb=b;
//                EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//                        gotoBoundingBoxWithHistory(bb);
//                    }
//                });
            }
        } catch (Throwable t) {
            log.error("Fehler beim lesen der aktuellen Position", t); // NOI18N
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

        try {
            final Element widgets = prefs.getChild("InternalWidgets");                                   // NOI18N
            if (widgets != null) {
                for (final Object widget : widgets.getChildren()) {
                    final String name = ((Element)widget).getAttribute("name").getValue();               // NOI18N
                    final boolean visible = ((Element)widget).getAttribute("visible").getBooleanValue(); // NOI18N
                    this.showInternalWidget(name, visible, 0);
                }
            }
        } catch (Throwable t) {
            log.warn("could not enable internal widgets: " + t, t);                                      // NOI18N
        }
    }

    /**
     * Zooms to all features of the mappingcomponents featurecollection. If fixedScale is true, the mappingcomponent
     * will only pan to the featurecollection and not zoom.
     *
     * @param  fixedScale  true, if zoom is not allowed
     */
    public void zoomToFeatureCollection(final boolean fixedScale) {
        zoomToAFeatureCollection(featureCollection.getAllFeatures(), true, fixedScale);
    }

    /**
     * Zooms to all features of the mappingcomponents featurecollection.
     */
    public void zoomToFeatureCollection() {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("zoomToFeatureCollection"); // NOI18N
            }
        }
        zoomToAFeatureCollection(featureCollection.getAllFeatures(), true, false);
    }

    /**
     * Moves the view to the target Boundingbox.
     *
     * @param  bb                 target BoundingBox
     * @param  history            true, if the action sould be saved in the history
     * @param  scaleToFit         true, to zoom
     * @param  animationDuration  duration of the animation
     */
    public void gotoBoundingBox(final BoundingBox bb,
            final boolean history,
            final boolean scaleToFit,
            final int animationDuration) {
        gotoBoundingBox(bb, history, scaleToFit, animationDuration, true);
    }

    /**
     * Moves the view to the target Boundingbox.
     *
     * @param  bb                 target BoundingBox
     * @param  history            true, if the action sould be saved in the history
     * @param  scaleToFit         true, to zoom
     * @param  animationDuration  duration of the animation
     * @param  queryServices      true, if the services should be refreshed after animation
     */
    public void gotoBoundingBox(BoundingBox bb,
            final boolean history,
            final boolean scaleToFit,
            final int animationDuration,
            final boolean queryServices) {
        if (bb != null) {
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug("gotoBoundingBox:" + bb, new CurrentStackTrace()); // NOI18N
                }
            }
            try {
                handleLayer.removeAllChildren();
            } catch (Exception e) {
                log.warn("Fehler bei removeAllCHildren", e);                     // NOI18N
            }

            if (bb instanceof XBoundingBox) {
                if (!((XBoundingBox)bb).getSrs().equals(mappingModel.getSrs().getCode())) {
                    try {
                        final CrsTransformer trans = new CrsTransformer(mappingModel.getSrs().getCode());
                        bb = trans.transformBoundingBox((XBoundingBox)bb);
                    } catch (Exception e) {
                        log.error("Cannot transform the bounding box", e);
                    }
                }
            }

            final double x1 = getWtst().getScreenX(bb.getX1());
            final double y1 = getWtst().getScreenY(bb.getY1());
            final double x2 = getWtst().getScreenX(bb.getX2());
            final double y2 = getWtst().getScreenY(bb.getY2());
            final double w;
            final double h;

            final Rectangle2D pos = new Rectangle2D.Double();
            pos.setRect(x1, y2, x2 - x1, y1 - y2);
            getCamera().animateViewToCenterBounds(pos, (x1 != x2) && (y1 != y2) && scaleToFit, animationDuration);
            if (getCamera().getViewTransform().getScaleY() < 0) {
                log.fatal("gotoBoundingBox: Problem :-( mit getViewTransform"); // NOI18N
            }
            showHandles(true);
            final Runnable handle = new Runnable() {

                    @Override
                    public void run() {
                        while (getAnimating()) {
                            try {
                                Thread.currentThread().sleep(10);
                            } catch (Exception e) {
                                log.warn("Unterbrechung bei getAnimating()", e); // NOI18N
                            }
                        }
                        if (history) {
                            if ((x1 == x2) || (y1 == y2) || !scaleToFit) {
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
            log.warn("Seltsam: die BoundingBox war null", new CurrentStackTrace()); // NOI18N
        }
    }

    /**
     * Moves the view to the target Boundingbox without saving the action in the history.
     *
     * @param  bb  target BoundingBox
     */
    public void gotoBoundingBoxWithoutHistory(final BoundingBox bb) {
        gotoBoundingBoxWithoutHistory(bb, animationDuration);
    }

    /**
     * Moves the view to the target Boundingbox without saving the action in the history.
     *
     * @param  bb                 target BoundingBox
     * @param  animationDuration  the animation duration
     */
    public void gotoBoundingBoxWithoutHistory(final BoundingBox bb, final int animationDuration) {
        gotoBoundingBox(bb, false, true, animationDuration);
    }

    /**
     * Moves the view to the target Boundingbox and saves the action in the history.
     *
     * @param  bb  target BoundingBox
     */
    public void gotoBoundingBoxWithHistory(final BoundingBox bb) {
        gotoBoundingBox(bb, true, true, animationDuration);
    }

    /**
     * Returns a BoundingBox of the current view in another scale.
     *
     * @param   scaleDenominator  specific target scale
     *
     * @return  DOCUMENT ME!
     */
    public BoundingBox getBoundingBoxFromScale(final double scaleDenominator) {
        return getScaledBoundingBox(scaleDenominator, getCurrentBoundingBox());
    }

    /**
     * Returns the BoundingBox of the delivered BoundingBox in another scale.
     *
     * @param   scaleDenominator  specific target scale
     * @param   bb                source BoundingBox
     *
     * @return  DOCUMENT ME!
     */
    public BoundingBox getScaledBoundingBox(final double scaleDenominator, BoundingBox bb) {
        final double screenWidthInInch = getWidth() / screenResolution;
        final double screenWidthInMeter = screenWidthInInch * 0.0254;
        final double screenHeightInInch = getHeight() / screenResolution;
        final double screenHeightInMeter = screenHeightInInch * 0.0254;

        final double realWorldWidthInMeter = screenWidthInMeter * scaleDenominator;
        final double realWorldHeightInMeter = screenHeightInMeter * scaleDenominator;

        if (!mappingModel.getSrs().isMetric() && (transformer != null)) {
            try {
                // transform the given bounding box to a metric coordinate system
                bb = transformer.transformBoundingBox(bb, mappingModel.getSrs().getCode());
            } catch (Exception e) {
                log.error("Cannot transform the current bounding box.", e);
            }
        }

        final double midX = bb.getX1() + ((bb.getX2() - bb.getX1()) / 2);
        final double midY = bb.getY1() + ((bb.getY2() - bb.getY1()) / 2);
        BoundingBox scaledBox = new BoundingBox(midX - (realWorldWidthInMeter / 2),
                midY
                        - (realWorldHeightInMeter / 2),
                midX
                        + (realWorldWidthInMeter / 2),
                midY
                        + (realWorldHeightInMeter / 2));

        if (!mappingModel.getSrs().isMetric() && (transformer != null)) {
            try {
                // transform the scaled bounding box to the current coordinate system
                final CrsTransformer trans = new CrsTransformer(mappingModel.getSrs().getCode());
                scaledBox = trans.transformBoundingBox(scaledBox, transformer.getDestinationCrs());
            } catch (Exception e) {
                log.error("Cannot transform the current bounding box.", e);
            }
        }

        return scaledBox;
    }

    /**
     * Calculate the current scaledenominator.
     *
     * @return  DOCUMENT ME!
     */
    public double getScaleDenominator() {
        BoundingBox boundingBox = getCurrentBoundingBox();
        final double screenWidthInInch = getWidth() / screenResolution;
        final double screenWidthInMeter = screenWidthInInch * 0.0254;
        final double screenHeightInInch = getHeight() / screenResolution;
        final double screenHeightInMeter = screenHeightInInch * 0.0254;

        if (!mappingModel.getSrs().isMetric() && (transformer != null)) {
            try {
                boundingBox = transformer.transformBoundingBox(
                        boundingBox,
                        mappingModel.getSrs().getCode());
            } catch (Exception e) {
                log.error("Cannot transform the current bounding box.", e);
            }
        }

        final double realWorldWidthInMeter = boundingBox.getWidth();
        final double realWorldHeightInMeter = boundingBox.getHeight();

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
     * Called when the drag operation has terminated with a drop on the operable part of the drop site for the <code>
     * DropTarget</code> registered with this listener.
     *
     * <p>This method is responsible for undertaking the transfer of the data associated with the gesture. The <code>
     * DropTargetDropEvent</code> provides a means to obtain a <code>Transferable</code> object that represents the data
     * object(s) to be transfered.</p>
     *
     * <P>From this method, the <code>DropTargetListener</code> shall accept or reject the drop via the acceptDrop(int
     * dropAction) or rejectDrop() methods of the <code>DropTargetDropEvent</code> parameter.</P>
     *
     * <P>Subsequent to acceptDrop(), but not before, <code>DropTargetDropEvent</code>'s getTransferable() method may be
     * invoked, and data transfer may be performed via the returned <code>Transferable</code>'s getTransferData()
     * method.</P>
     *
     * <P>At the completion of a drop, an implementation of this method is required to signal the success/failure of the
     * drop by passing an appropriate <code>boolean</code> to the <code>DropTargetDropEvent</code>'s
     * dropComplete(boolean success) method.</P>
     *
     * <P>Note: The data transfer should be completed before the call to the <code>DropTargetDropEvent</code>'s
     * dropComplete(boolean success) method. After that, a call to the getTransferData() method of the <code>
     * Transferable</code> returned by <code>DropTargetDropEvent.getTransferable()</code> is guaranteed to succeed only
     * if the data transfer is local; that is, only if <code>DropTargetDropEvent.isLocalTransfer()</code> returns <code>
     * true</code>. Otherwise, the behavior of the call is implementation-dependent.</P>
     *
     * @param  dtde  the <code>DropTargetDropEvent</code>
     */
    @Override
    public void drop(final DropTargetDropEvent dtde) {
        if (isDropEnabled(dtde)) {
            try {
                final MapDnDEvent mde = new MapDnDEvent();
                mde.setDte(dtde);
                final Point p = dtde.getLocation();
                getCamera().getViewTransform().inverseTransform(p, p);
                mde.setXPos(getWtst().getWorldX(p.getX()));
                mde.setYPos(getWtst().getWorldY(p.getY()));
                CismapBroker.getInstance().fireDropOnMap(mde);
            } catch (Exception ex) {
                log.error("Error in drop", ex); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   dtde  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isDropEnabled(final DropTargetDropEvent dtde) {
        if (ALKIS_PRINT.equals(getInteractionMode())) {
            for (final DataFlavor flavour : dtde.getTransferable().getTransferDataFlavors()) {
                // necessary evil, because we have no dependecy to DefaultMetaTreeNode frome here
                if (String.valueOf(flavour.getRepresentationClass()).endsWith(".DefaultMetaTreeNode")) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Called while a drag operation is ongoing, when the mouse pointer has exited the operable part of the drop site
     * for the <code>DropTarget</code> registered with this listener.
     *
     * @param  dte  the <code>DropTargetEvent</code>
     */
    @Override
    public void dragExit(final DropTargetEvent dte) {
    }

    /**
     * Called if the user has modified the current drop gesture.
     *
     * @param  dtde  the <code>DropTargetDragEvent</code>
     */
    @Override
    public void dropActionChanged(final DropTargetDragEvent dtde) {
    }

    /**
     * Called when a drag operation is ongoing, while the mouse pointer is still over the operable part of the dro9p
     * site for the <code>DropTarget</code> registered with this listener.
     *
     * @param  dtde  the <code>DropTargetDragEvent</code>
     */
    @Override
    public void dragOver(final DropTargetDragEvent dtde) {
        try {
            final MapDnDEvent mde = new MapDnDEvent();
            mde.setDte(dtde);
            // TODO: this seems to be buggy!
            final Point p = dtde.getLocation();
            // Point2D p2d;
// double scale = 1 / getCamera().getViewScale();
            getCamera().getViewTransform().inverseTransform(p, p);
            mde.setXPos(getWtst().getWorldX(p.getX()));
            mde.setYPos(getWtst().getWorldY(p.getY()));
            CismapBroker.getInstance().fireDragOverMap(mde);
        } catch (Exception ex) {
            log.error("Error in dragOver", ex); // NOI18N
        }
//        MapDnDEvent mde = new MapDnDEvent();
//        mde.setDte(dtde);
//        Point p = dtde.getLocation();
//        double scale = 1/getCamera().getViewScale();
//        mde.setXPos(getWtst().getWorldX(p.getX() * scale));
//        mde.setYPos(getWtst().getWorldY(p.getY() * scale));
//        CismapBroker.getInstance().fireDragOverMap(mde);
    }

    /**
     * Called while a drag operation is ongoing, when the mouse pointer enters the operable part of the drop site for
     * the <code>DropTarget</code> registered with this listener.
     *
     * @param  dtde  the <code>DropTargetDragEvent</code>
     */
    @Override
    public void dragEnter(final DropTargetDragEvent dtde) {
    }

    /**
     * Returns the PfeatureHashmap which assigns a Feature to a PFeature.
     *
     * @return  DOCUMENT ME!
     */
    public ConcurrentHashMap<Feature, PFeature> getPFeatureHM() {
        return pFeatureHM;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isFixedMapExtent() {
        return fixedMapExtent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fixedMapExtent  DOCUMENT ME!
     */
    public void setFixedMapExtent(final boolean fixedMapExtent) {
        this.fixedMapExtent = fixedMapExtent;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isFixedMapScale() {
        return fixedMapScale;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fixedMapScale  DOCUMENT ME!
     */
    public void setFixedMapScale(final boolean fixedMapScale) {
        this.fixedMapScale = fixedMapScale;
    }

    /**
     * DOCUMENT ME!
     *
     * @param       one  DOCUMENT ME!
     *
     * @deprecated  DOCUMENT ME!
     */
    public void selectPFeatureManually(final PFeature one) {
        // throw new UnsupportedOperationException("Not yet implemented");
        if (one != null) {
            featureCollection.select(one.getFeature());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  DOCUMENT ME!
     */
    public PFeature getSelectedNode() {
        // gehe mal davon aus dass das nur aufgerufen wird wenn sowieso nur ein node selected ist
        // deshalb gebe ich mal nur das erste zur�ck
        if (featureCollection.getSelectedFeatures().size() > 0) {
            final Feature selF = (Feature)featureCollection.getSelectedFeatures().toArray()[0];
            if (selF == null) {
                return null;
            }
            return pFeatureHM.get(selF);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isInfoNodesVisible() {
        return infoNodesVisible;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PLayer getPrintingFrameLayer() {
        return printingFrameLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PrintingSettingsWidget getPrintingSettingsDialog() {
        return printingSettingsDialog;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isInGlueIdenticalPointsMode() {
        return inGlueIdenticalPointsMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  inGlueIdenticalPointsMode  DOCUMENT ME!
     */
    public void setInGlueIdenticalPointsMode(final boolean inGlueIdenticalPointsMode) {
        this.inGlueIdenticalPointsMode = inGlueIdenticalPointsMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PLayer getHighlightingLayer() {
        return highlightingLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  anno  DOCUMENT ME!
     */
    public void setPointerAnnotation(final PNode anno) {
        ((SimpleMoveListener)getInputListener(MOTION)).setPointerAnnotation(anno);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visib  DOCUMENT ME!
     */
    public void setPointerAnnotationVisibility(final boolean visib) {
        if (getInputListener(MOTION) != null) {
            ((SimpleMoveListener)getInputListener(MOTION)).setAnnotationNodeVisible(visib);
        }
    }

    /**
     * Returns a boolean whether the annotationnode is visible or not. Returns false if the interactionmode doesn't
     * equal MOTION.
     *
     * @return  DOCUMENT ME!
     */
    public boolean isPointerAnnotationVisible() {
        if (getInputListener(MOTION) != null) {
            return ((SimpleMoveListener)getInputListener(MOTION)).isAnnotationNodeVisible();
        } else {
            return false;
        }
    }

    /**
     * Returns a vector with different scales.
     *
     * @return  DOCUMENT ME!
     */
    public List<Scale> getScales() {
        return scales;
    }

    /**
     * Returns a list with different crs.
     *
     * @return  DOCUMENT ME!
     */
    public List<Crs> getCrsList() {
        return crsList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  a transformer with the default crs as destination crs. The default crs is the first crs in the
     *          configuration file that has set the selected attribut on true). This crs must be metric.
     */
    public CrsTransformer getMetricTransformer() {
        return transformer;
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
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("unlock");                                   // NOI18N
            }
        }
        locked = false;
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug("currentBoundingBox:" + currentBoundingBox); // NOI18N
            }
        }
        gotoBoundingBoxWithHistory(currentBoundingBox);
    }

    /**
     * Returns whether the MappingComponent is locked or not.
     *
     * @return  DOCUMENT ME!
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Returns the MementoInterface for redo-actions.
     *
     * @return  DOCUMENT ME!
     */
    public MementoInterface getMemRedo() {
        return memRedo;
    }

    /**
     * Returns the MementoInterface for undo-actions.
     *
     * @return  DOCUMENT ME!
     */
    public MementoInterface getMemUndo() {
        return memUndo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public HashMap<String, PBasicInputEventHandler> getInputEventListener() {
        return inputEventListener;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  inputEventListener  DOCUMENT ME!
     */
    public void setInputEventListener(final HashMap<String, PBasicInputEventHandler> inputEventListener) {
        this.inputEventListener.clear();
        this.inputEventListener.putAll(inputEventListener);
    }

    @Override
    public synchronized void crsChanged(final CrsChangedEvent event) {
        if ((event.getFormerCrs() != null) && (fixedBoundingBox == null) && !resetCrs) {
            if (locked) {
                return;
            }

            try {
                // the wtst object should not be null, so the getWtst method will be invoked
                final WorldToScreenTransform oldWtst = getWtst();
                final BoundingBox bbox = getCurrentBoundingBox(); // getCurrentBoundingBox();
                final CrsTransformer crsTransformer = new CrsTransformer(event.getCurrentCrs().getCode());
                final BoundingBox newBbox = crsTransformer.transformBoundingBox(bbox, event.getFormerCrs().getCode());

                if (getMappingModel() instanceof ActiveLayerModel) {
                    final ActiveLayerModel alm = (ActiveLayerModel)getMappingModel();
                    alm.setSrs(event.getCurrentCrs());
                }
                wtst = null;
                getWtst();
                gotoBoundingBoxWithoutHistory(newBbox, 0);

                final ArrayList<Feature> list = new ArrayList<Feature>(featureCollection.getAllFeatures());
                removeFeatures(list);
                addFeaturesToMap(list.toArray(new Feature[list.size()]));

                // refresh all wfs layer
                if (getMappingModel() instanceof ActiveLayerModel) {
                    final ActiveLayerModel alm = (ActiveLayerModel)getMappingModel();
                    alm.refreshWebFeatureServices();
                }

                // transform the highlighting layer
                for (int i = 0; i < highlightingLayer.getChildrenCount(); ++i) {
                    final PNode node = highlightingLayer.getChild(i);
                    CrsTransformer.transformPNodeToGivenCrs(
                        node,
                        event.getFormerCrs().getCode(),
                        event.getCurrentCrs().getCode(),
                        oldWtst,
                        getWtst());
                    rescaleStickyNode(node);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    org.openide.util.NbBundle.getMessage(
                        MappingComponent.class,
                        "MappingComponent.crsChanged(CrsChangedEvent).JOptionPane.message"),
                    org.openide.util.NbBundle.getMessage(
                        MappingComponent.class,
                        "MappingComponent.crsChanged(CrsChangedEvent).JOptionPane.title"),
                    JOptionPane.ERROR_MESSAGE);
                log.error("Cannot transform the current bounding box to the CRS " + event.getCurrentCrs(), e);
                resetCrs = true;
                final ActiveLayerModel alm = (ActiveLayerModel)getMappingModel();
                alm.setSrs(event.getCurrentCrs());
                CismapBroker.getInstance().setSrs(event.getFormerCrs());
            }
        } else {
            resetCrs = false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JInternalFrame getInternalWidget(final String name) {
        if (this.internalWidgets.containsKey(name)) {
            return this.internalWidgets.get(name);
        } else {
            log.warn("unknown internal widget '" + name + "'"); // NOI18N
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getInternalWidgetPosition(final String name) {
        if (this.internalWidgetPositions.containsKey(name)) {
            return this.internalWidgetPositions.get(name);
        } else {
            log.warn("unknown position for '" + name + "'"); // NOI18N
            return -1;
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * /////////////////////////////////////////////// CLASS MappingComponentRasterServiceListener //
     * ///////////////////////////////////////////////.
     *
     * @version  $Revision$, $Date$
     */
    private class MappingComponentRasterServiceListener implements RetrievalListener {

        //~ Instance fields ----------------------------------------------------

        private Logger logger = Logger.getLogger(this.getClass());
        private int position = -1;
        private XPImage pi = null;
        private ServiceLayer rasterService = null;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new MappingComponentRasterServiceListener object.
         *
         * @param  position       DOCUMENT ME!
         * @param  pn             DOCUMENT ME!
         * @param  rasterService  DOCUMENT ME!
         */
        public MappingComponentRasterServiceListener(final int position,
                final PNode pn,
                final ServiceLayer rasterService) {
            this.position = position;
            if (pn instanceof XPImage) {
                this.pi = (XPImage)pn;
            }
            this.rasterService = rasterService;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void retrievalStarted(final RetrievalEvent e) {
            fireActivityChanged();
            if (DEBUG) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(rasterService + ": TaskCounter:" + taskCounter); // NOI18N
                }
            }
        }

        @Override
        public void retrievalProgress(final RetrievalEvent e) {
        }

        @Override
        public void retrievalError(final RetrievalEvent e) {
            this.logger.error(rasterService + ": Fehler beim Laden des Bildes! " + e.getErrorType() + " Errors: "
                        + e.getErrors() + " Cause: " + e.getRetrievedObject()); // NOI18N
            fireActivityChanged();
            if (DEBUG) {
                if (log.isDebugEnabled()) {
                    log.debug(rasterService + ": TaskCounter:" + taskCounter);  // NOI18N
                }
            }
        }

        @Override
        public void retrievalComplete(final RetrievalEvent e) {
            final Point2D localOrigin = getCamera().getViewBounds().getOrigin();
            final double localScale = getCamera().getViewScale();
            final PBounds localBounds = getCamera().getViewBounds();
            final Object o = e.getRetrievedObject();
//            log.fatal(localBounds+ " "+localScale);
            if (DEBUG) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(rasterService + ": TaskCounter:" + taskCounter); // NOI18N
                }
            }
            final Runnable paintImageOnMap = new Runnable() {

                    @Override
                    public void run() {
                        fireActivityChanged();
                        if ((o instanceof Image) && (e.isHasErrors() == false)) {
                            // TODO Hier ist noch ein Fehler die Sichtbarkeit muss vom Layer erfragt werden
                            if (isBackgroundEnabled()) {
                                // Image i=Static2DTools.toCompatibleImage((Image)o);
                                final Image i = (Image)o;
                                if (rasterService.getName().startsWith("prefetching")) { // NOI18N
                                    final double x = localOrigin.getX() - localBounds.getWidth();
                                    final double y = localOrigin.getY() - localBounds.getHeight();
                                    pi.setImage(i, 0);
                                    pi.setScale(3 / localScale);
                                    pi.setOffset(x, y);
                                } else {
                                    pi.setImage(i, 1000);
                                    pi.setScale(1 / localScale);
                                    pi.setOffset(localOrigin);
                                    MappingComponent.this.repaint();
                                }
                            }
                        }
                    }
                };
            if (EventQueue.isDispatchThread()) {
                paintImageOnMap.run();
            } else {
                EventQueue.invokeLater(paintImageOnMap);
            }
        }

        @Override
        public void retrievalAborted(final RetrievalEvent e) {
            this.logger.warn(rasterService + ": retrievalAborted: " + e.getRequestIdentifier()); // NOI18N
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getPosition() {
            return position;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  position  DOCUMENT ME!
         */
        public void setPosition(final int position) {
            this.position = position;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class DocumentProgressListener implements RetrievalListener {

        //~ Instance fields ----------------------------------------------------

        private Logger logger = Logger.getLogger(this.getClass());
        private long requestId = -1;
        /** Displays the loading progress of Documents, e.g. SHP Files */
        private final DocumentProgressWidget documentProgressWidget = new DocumentProgressWidget();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DocumentProgressListener object.
         */
        public DocumentProgressListener() {
            documentProgressWidget.setVisible(false);
            if (MappingComponent.this.getInternalWidget(MappingComponent.PROGRESSWIDGET) == null) {
                MappingComponent.this.addInternalWidget(
                    MappingComponent.PROGRESSWIDGET,
                    MappingComponent.POSITION_SOUTHWEST,
                    documentProgressWidget);
            }
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void retrievalStarted(final RetrievalEvent e) {
            if (!e.isInitialisationEvent()) {
                logger.warn(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalStarted aborted, no initialisation event"); // NOI18N
                return;
            }

            if (this.requestId != -1) {
                logger.warn(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalStarted: another initialisation thread is still running: " + requestId); // NOI18N
            }

            this.requestId = e.getRequestIdentifier();
            this.documentProgressWidget.setServiceName(e.getRetrievalService().toString());
            this.documentProgressWidget.setProgress(-1);
            MappingComponent.this.showInternalWidget(MappingComponent.PROGRESSWIDGET, true, 100);

            // MappingComponent.this.showInternalWidget(ZOOM, DEBUG, animationDuration);
            // MappingComponent.this.isInternalWidgetVisible(ZOOM);
        }

        @Override
        public void retrievalProgress(final RetrievalEvent e) {
            if (!e.isInitialisationEvent()) {
                logger.warn(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalProgress, no initialisation event"); // NOI18N
                return;
            }

            if (this.requestId != e.getRequestIdentifier()) {
                logger.warn(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalProgress: another initialisation thread is still running: " + requestId); // NOI18N
            }

            if (DEBUG) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                                + "]: initialisation progress: " + e.getPercentageDone()); // NOI18N
                }
            }
            this.documentProgressWidget.setProgress(e.getPercentageDone());
        }

        @Override
        public void retrievalComplete(final RetrievalEvent e) {
            if (!e.isInitialisationEvent()) {
                logger.warn(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalComplete, no initialisation event"); // NOI18N
                return;
            }

            if (this.requestId != e.getRequestIdentifier()) {
                logger.warn(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalComplete: another initialisation thread is still running: " + requestId); // NOI18N
            }

            e.getRetrievalService().removeRetrievalListener(this);
            this.requestId = -1;
            this.documentProgressWidget.setProgress(100);
            MappingComponent.this.showInternalWidget(MappingComponent.PROGRESSWIDGET, false, 200);
        }

        @Override
        public void retrievalAborted(final RetrievalEvent e) {
            if (!e.isInitialisationEvent()) {
                logger.warn(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalAborted aborted, no initialisation event"); // NOI18N
                return;
            }

            if (this.requestId != e.getRequestIdentifier()) {
                logger.warn(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalAborted: another initialisation thread is still running: " + requestId); // NOI18N
            }

            // e.getRetrievalService().removeRetrievalListener(this);
            this.requestId = -1;
            this.documentProgressWidget.setProgress(0);
            MappingComponent.this.showInternalWidget(MappingComponent.PROGRESSWIDGET, false, 25);
        }

        @Override
        public void retrievalError(final RetrievalEvent e) {
            if (!e.isInitialisationEvent()) {
                logger.warn(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalError aborted, no initialisation event"); // NOI18N
                return;
            }

            if (this.requestId != e.getRequestIdentifier()) {
                logger.warn(e.getRetrievalService() + "[" + e.getRequestIdentifier()
                            + "]: retrievalError: another initialisation thread is still running: " + requestId); // NOI18N
            }

            this.requestId = -1;
            e.getRetrievalService().removeRetrievalListener(this);
            this.documentProgressWidget.setProgress(0);
            MappingComponent.this.showInternalWidget(MappingComponent.PROGRESSWIDGET, false, 25);
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public long getRequestId() {
            return this.requestId;
        }
    }

    /**
     * //////////////////////////////////////////////// CLASS MappingComponentFeatureServiceListener //
     * ////////////////////////////////////////////////.
     *
     * @version  $Revision$, $Date$
     */
    private class MappingComponentFeatureServiceListener implements RetrievalListener {

        //~ Instance fields ----------------------------------------------------

        ServiceLayer featureService;
        PLayer parent;
        long requestIdentifier;
        Thread completionThread = null;
        private Logger logger = Logger.getLogger(this.getClass());
        private Vector deletionCandidates = new Vector();
        private Vector twins = new Vector();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new MappingComponentFeatureServiceListener.
         *
         * @param  featureService  the featureretrievalservice
         * @param  parent          the featurelayer (PNode) connected with the servicelayer
         */
        public MappingComponentFeatureServiceListener(final ServiceLayer featureService, final PLayer parent) {
            this.featureService = featureService;
            this.parent = parent;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void retrievalStarted(final RetrievalEvent e) {
            if (!e.isInitialisationEvent()) {
                requestIdentifier = e.getRequestIdentifier();
            }

            if (DEBUG) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(featureService + "[" + e.getRequestIdentifier() + " (" + requestIdentifier
                                + ")]: " + (e.isInitialisationEvent() ? "initialisation" : "retrieval") + " started"); // NOI18N
                }
            }
            fireActivityChanged();
        }

        @Override
        public void retrievalProgress(final RetrievalEvent e) {
            if (DEBUG) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(featureService + "[" + e.getRequestIdentifier() + " (" + this.requestIdentifier
                                + ")]: " + (e.isInitialisationEvent() ? "initialisation" : "retrieval") + " Progress: "
                                + e.getPercentageDone() + " (" + ((RetrievalServiceLayer)featureService).getProgress()
                                + ")"); // NOI18N
                }
            }
            fireActivityChanged();
            // TODO Hier besteht auch die Möglichkeit jedes einzelne Polygon hinzuzufügen. ausprobieren, ob das
            // flüssiger ist
        }

        @Override
        public void retrievalError(final RetrievalEvent e) {
            this.logger.error(featureService + "[" + e.getRequestIdentifier() + " (" + this.requestIdentifier + ")]: "
                        + (e.isInitialisationEvent() ? "initialisation" : "retrieval") + " error"); // NOI18N
            fireActivityChanged();
        }

        @Override
        public void retrievalComplete(final RetrievalEvent e) {
            if (DEBUG) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(featureService + "[" + e.getRequestIdentifier() + " (" + this.requestIdentifier
                                + ")]: " + (e.isInitialisationEvent() ? "initialisation" : "retrieval") + " complete"); // NOI18N
                }
            }

            if (e.isInitialisationEvent()) {
                this.logger.info(featureService + "[" + e.getRequestIdentifier() + " (" + this.requestIdentifier
                            + ")]: initialisation complete"); // NOI18N
                fireActivityChanged();
                return;
            }

            if ((completionThread != null) && completionThread.isAlive() && !completionThread.isInterrupted()) {
                this.logger.warn(featureService + "[" + e.getRequestIdentifier() + " (" + this.requestIdentifier
                            + ")]: retrievalComplete: old completion thread still running, trying to interrupt thread"); // NOI18N
                completionThread.interrupt();
            }

            if (e.getRequestIdentifier() < requestIdentifier) {
                if (DEBUG) {
                    this.logger.warn(featureService + "[" + e.getRequestIdentifier() + " (" + requestIdentifier
                                + ")]: retrievalComplete: another retrieval process is still running, aborting retrievalComplete"); // NOI18N
                }
                ((RetrievalServiceLayer)featureService).setProgress(-1);
                fireActivityChanged();
                return;
            }

            final Vector newFeatures = new Vector();
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        ((RetrievalServiceLayer)featureService).setProgress(-1);
                        parent.setVisible(isBackgroundEnabled() && featureService.isEnabled() && parent.getVisible());
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

            if (DEBUG) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(featureService + "[" + e.getRequestIdentifier() + " (" + this.requestIdentifier
                                + ")]: deletionCandidates (" + deletionCandidates.size() + ")");    // + deletionCandidates);//NOI18N
                }
            }
            // only start parsing the features if there are no errors and a correct collection
            if ((e.isHasErrors() == false) && (e.getRetrievedObject() instanceof Collection)) {
                completionThread = new Thread() {

                        @Override
                        public void run() {
                            // this is the collection with the retrieved features
                            // Collection c = ((Collection) e.getRetrievedObject());
                            final List features = new ArrayList((Collection)e.getRetrievedObject());
                            final int size = features.size();
                            int counter = 0;
                            final Iterator it = features.iterator();
                            if (DEBUG) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(featureService + "[" + e.getRequestIdentifier() + " ("
                                                + requestIdentifier + ")]: Anzahl Features: " + size); // NOI18N
                                }
                            }

                            while ((requestIdentifier == e.getRequestIdentifier()) && !isInterrupted()
                                        && it.hasNext()) {
                                counter++;
                                final Object o = it.next();
                                if (o instanceof Feature) {
                                    final PFeature p = new PFeature(((Feature)o),
                                            wtst,
                                            clip_offset_x,
                                            clip_offset_y,
                                            MappingComponent.this);
                                    PFeature twin = null;
                                    for (final Object tester : deletionCandidates) {
                                        // if tester and PFeature are FeatureWithId-objects
                                        if ((((PFeature)tester).getFeature() instanceof FeatureWithId)
                                                    && (p.getFeature() instanceof FeatureWithId)) {
                                            final int id1 = ((FeatureWithId)((PFeature)tester).getFeature()).getId();
                                            final int id2 = ((FeatureWithId)(p.getFeature())).getId();
                                            if ((id1 != -1) && (id2 != -1)) { // check if they've got the same id
                                                if (id1 == id2) {
                                                    twin = ((PFeature)tester);
                                                    break;
                                                }
                                            } else {                          // else test the geometry for equality
                                                if (((PFeature)tester).getFeature().getGeometry().equals(
                                                                p.getFeature().getGeometry())) {
                                                    twin = ((PFeature)tester);
                                                    break;
                                                }
                                            }
                                        } else {                              // no FeatureWithId, test geometries for
                                            // equality
                                            if (((PFeature)tester).getFeature().getGeometry().equals(
                                                            p.getFeature().getGeometry())) {
                                                twin = ((PFeature)tester);
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
                                    // fire event only wheen needed
                                    final int currentProgress = (int)((double)counter / (double)size * 100d);
                                    /*if(currentProgress % 10 == 0 && currentProgress > lastUpdateProgress)
                                     * { lastUpdateProgress = currentProgress; if(DEBUG)log.debug("fire progress changed
                                     * "+currentProgress); fireActivityChanged();}*/

                                    if ((currentProgress >= 10) && ((currentProgress % 10) == 0)) {
                                        ((RetrievalServiceLayer)featureService).setProgress(currentProgress);
                                        fireActivityChanged();
                                    }
                                }
                            }

                            if ((requestIdentifier == e.getRequestIdentifier()) && !isInterrupted()) {
                                // after all features are computed do stuff on the EDT
                                EventQueue.invokeLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            try {
                                                if (DEBUG) {
                                                    if (logger.isDebugEnabled()) {
                                                        logger.debug(
                                                            featureService
                                                                    + "["
                                                                    + e.getRequestIdentifier()
                                                                    + " ("
                                                                    + requestIdentifier
                                                                    + ")]: MappingComponentFeaturelistener.retrievalComplete()"); // NOI18N
                                                    }
                                                }

                                                // if it's a refresh, delete all previous features
                                                if (e.isRefreshExisting()) {
                                                    parent.removeAllChildren();
                                                }
                                                final Vector deleteFeatures = new Vector();
                                                for (final Object o : newFeatures) {
                                                    parent.addChild((PNode)o);
                                                }
//                                    for (Object o : twins) { // TODO only nesseccary if style has changed
//                                        ((PFeature) o).refreshDesign();
//                                        if(DEBUG)log.debug("twin refresh");
//                                    }

                                                // set the prograssbar to full
                                                if (DEBUG) {
                                                    if (logger.isDebugEnabled()) {
                                                        logger.debug(
                                                            featureService
                                                                    + "["
                                                                    + e.getRequestIdentifier()
                                                                    + " ("
                                                                    + requestIdentifier
                                                                    + ")]: set progress to 100"); // NOI18N
                                                    }
                                                }
                                                ((RetrievalServiceLayer)featureService).setProgress(100);
                                                fireActivityChanged();

                                                // repaint the featurelayer
                                                parent.repaint();

                                                // remove stickyNode from all deletionCandidates and add
                                                // each to the new deletefeature-collection
                                                for (final Object o : deletionCandidates) {
                                                    if (o instanceof PFeature) {
                                                        final PNode p = ((PFeature)o).getPrimaryAnnotationNode();
                                                        if (p != null) {
                                                            removeStickyNode(p);
                                                        }
                                                        deleteFeatures.add(o);
                                                    }
                                                }
                                                if (DEBUG) {
                                                    if (logger.isDebugEnabled()) {
                                                        logger.debug(
                                                            featureService
                                                                    + "["
                                                                    + e.getRequestIdentifier()
                                                                    + " ("
                                                                    + requestIdentifier
                                                                    + ")]: parentCount before:"
                                                                    + parent.getChildrenCount()); // NOI18N
                                                    }
                                                }
                                                if (DEBUG) {
                                                    if (logger.isDebugEnabled()) {
                                                        logger.debug(
                                                            featureService
                                                                    + "["
                                                                    + e.getRequestIdentifier()
                                                                    + " ("
                                                                    + requestIdentifier
                                                                    + ")]: deleteFeatures="
                                                                    + deleteFeatures.size());     // + " :" +
                                                        // deleteFeatures);//NOI18N
                                                    }
                                                }
                                                parent.removeChildren(deleteFeatures);

                                                if (DEBUG) {
                                                    if (logger.isDebugEnabled()) {
                                                        logger.debug(
                                                            featureService
                                                                    + "["
                                                                    + e.getRequestIdentifier()
                                                                    + " ("
                                                                    + requestIdentifier
                                                                    + ")]: parentCount after:"
                                                                    + parent.getChildrenCount()); // NOI18N
                                                    }
                                                }
                                                log.info(
                                                    featureService
                                                            + "["
                                                            + e.getRequestIdentifier()
                                                            + " ("
                                                            + requestIdentifier
                                                            + ")]: "
                                                            + parent.getChildrenCount()
                                                            + " features retrieved or updated");  // NOI18N
                                                rescaleStickyNodes();
                                            } catch (Exception exception) {
                                                logger.warn(
                                                    featureService
                                                            + "["
                                                            + e.getRequestIdentifier()
                                                            + " ("
                                                            + requestIdentifier
                                                            + ")]: Fehler beim Aufr\u00E4umen",
                                                    exception);                                   // NOI18N
                                            }
                                        }
                                    });
                            } else {
                                if (DEBUG) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(featureService + "[" + e.getRequestIdentifier() + " ("
                                                    + requestIdentifier
                                                    + ")]: completion thread Interrupted or synchronisation lost"); // NOI18N
                                    }
                                }
                            }
                        }
                    };
                completionThread.setPriority(Thread.NORM_PRIORITY);
                if (requestIdentifier == e.getRequestIdentifier()) {
                    completionThread.start();
                } else {
                    if (DEBUG) {
                        if (this.logger.isDebugEnabled()) {
                            this.logger.debug(featureService + "[" + e.getRequestIdentifier() + " (" + requestIdentifier
                                        + ")]: completion thread Interrupted or synchronisation lost"); // NOI18N
                        }
                    }
                }
            }

            fireActivityChanged();
        }

        @Override
        public void retrievalAborted(final RetrievalEvent e) {
            this.logger.warn(featureService + "[" + e.getRequestIdentifier() + " (" + requestIdentifier
                        + ")]: aborted, TaskCounter:" + taskCounter); // NOI18N
            if (completionThread != null) {
                completionThread.interrupt();
            }

            if (e.getRequestIdentifier() < requestIdentifier) {
                if (DEBUG) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug(featureService + "[" + e.getRequestIdentifier() + " (" + requestIdentifier
                                    + ")]: another retrieval process is still running, setting the retrieval progress to indeterminate"); // NOI18N
                    }
                }
                ((RetrievalServiceLayer)featureService).setProgress(-1);
            } else {
                if (DEBUG) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug(featureService + "[" + e.getRequestIdentifier() + " (" + requestIdentifier
                                    + ")]: this is the last retrieval process, settign the retrieval progress to 0 (aborted)");           // NOI18N
                    }
                }
                ((RetrievalServiceLayer)featureService).setProgress(0);
            }

            fireActivityChanged();
        }
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class ImageSelection implements Transferable {

    //~ Instance fields --------------------------------------------------------

    private Image image;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ImageSelection object.
     *
     * @param  image  DOCUMENT ME!
     */
    public ImageSelection(final Image image) {
        this.image = image;
    }

    //~ Methods ----------------------------------------------------------------

    // Returns supported flavors
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { DataFlavor.imageFlavor };
    }

    // Returns true if flavor is supported
    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        return DataFlavor.imageFlavor.equals(flavor);
    }

    // Returns image
    @Override
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!DataFlavor.imageFlavor.equals(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return image;
    }
}
