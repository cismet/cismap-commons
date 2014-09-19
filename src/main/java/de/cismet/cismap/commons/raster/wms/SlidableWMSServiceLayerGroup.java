/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.raster.wms;

import edu.umd.cs.piccolo.PNode;

import org.apache.log4j.Logger;

import org.jdom.DataConversionException;
import org.jdom.Element;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.LayerInfoProvider;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.XPImage;
import de.cismet.cismap.commons.interaction.ActiveLayerListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.preferences.CapabilityLink;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.rasterservice.RasterMapService;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.cismap.commons.wms.capabilities.Envelope;
import de.cismet.cismap.commons.wms.capabilities.Layer;
import de.cismet.cismap.commons.wms.capabilities.LayerBoundingBox;
import de.cismet.cismap.commons.wms.capabilities.Position;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public final class SlidableWMSServiceLayerGroup extends AbstractRetrievalService implements RetrievalServiceLayer,
    RasterMapService,
    ChangeListener,
    MapService,
    LayerInfoProvider,
    ActiveLayerListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(SlidableWMSServiceLayerGroup.class);

    public static final String XML_ELEMENT_NAME = "SlidableWMSServiceLayerGroup"; // NOI18N
    /** A suffix which makes it clear, if a layer name was loaded from the config file. */
    public static final String LAYERNAME_FROM_CONFIG_SUFFIX = "$fromConfig$";
    private static final String SLIDER_PREFIX = "Slider"; // NOI18N
    private static List<Integer> uniqueNumbers = new ArrayList<Integer>();
    private static String addedInternalWidget = null;

    private static boolean BOTTOM_UP;
    private static boolean RESOURCE_CONSERVING;
    private static int TIME_TILL_LOCKED;
    private static int INACTIVE_TIME_TILL_LOCKED;
    private static double VERTICAL_LABEL_WIDTH_THRESHOLD;

    static {
        final Properties prop = new Properties();
        try {
            prop.load(SlidableWMSServiceLayerGroup.class.getResourceAsStream(
                    "SlidableWMSServiceLayerGroup.properties"));
            BOTTOM_UP = prop.getProperty("bottomUp", "true").trim().equalsIgnoreCase("true");
            RESOURCE_CONSERVING = prop.getProperty("resourceConserving", "false").trim().equalsIgnoreCase("true");
            TIME_TILL_LOCKED = Math.abs(Integer.parseInt(prop.getProperty("timeTillLocked", "60")));
            INACTIVE_TIME_TILL_LOCKED = Math.abs(Integer.parseInt(prop.getProperty("inactiveTimeTillLocked", "10")));
            VERTICAL_LABEL_WIDTH_THRESHOLD = Math.abs(Double.parseDouble(
                        prop.getProperty("verticalLabelWidthThreshold", "0.5")));
        } catch (Exception ex) {
            LOG.error("Could not load the properties for the SlidableWMSServiceLayerGroup", ex);
            BOTTOM_UP = true;
            RESOURCE_CONSERVING = false;
            TIME_TILL_LOCKED = 60;
            INACTIVE_TIME_TILL_LOCKED = 10;
            VERTICAL_LABEL_WIDTH_THRESHOLD = 0.5;
        }
    }

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum LabelDirection {

        //~ Enum constants -----------------------------------------------------

        HORIZONTAL, VERTICAL;
    }

    //~ Instance fields --------------------------------------------------------

    SlidableWMSServiceLayerGroupInternalFrame internalFrame;

    private boolean printMode = false;

    private boolean resourceConserving;

    private final List<WMSServiceLayer> layers = new ArrayList<WMSServiceLayer>();
    private boolean layerQuerySelected = false;
    private final String sliderName;
    private PNode pnode = new XPImage();
    private int layerPosition;
    private String name;
    private String completePath = null;
    private Map<WMSServiceLayer, Integer> progressTable = new ConcurrentHashMap<WMSServiceLayer, Integer>();
    private AtomicInteger layerComplete = new AtomicInteger(0);
    private String preferredRasterFormat;
    private String preferredBGColor;
    private String preferredExceptionsFormat;
    private String capabilitiesUrl = null;
    private WMSCapabilities wmsCapabilities;
    private XBoundingBox boundingBox;
    private String customSLD;
    private boolean selected = false;
    private boolean locked;
    private boolean doNotDisableSlider;
    private Timer lockTimer;
    private boolean crossfadeEnabled;
    private boolean bottomUp;
    private boolean enableAllChildren;
    private int timeTillLocked;
    private int inactiveTimeTillLocked;
    private double verticalLabelWidthThreshold;
    private ActionListener btnLockListener = new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                btnLockResultsActionPerformed(evt);
            }
        };

    private ActionListener lockTimerListener = new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                doNotDisableSlider = !lockTimer.isRunning();
                SlidableWMSServiceLayerGroup.this.setLocked(!lockTimer.isRunning());
            }
        };

    private HashMap<WMSServiceLayer, RetrievalListener> layerRetrievalListeners =
        new HashMap<WMSServiceLayer, RetrievalListener>();

    private boolean enabled = true;
    private List originalTreePaths;
    private Element originalElement;
    private HashMap<String, WMSCapabilities> orginalCapabilities;

    private float translucency = 1.0f;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SlidableWMSServiceLayerGroup object.
     *
     * @param  treePaths  DOCUMENT ME!
     */
    public SlidableWMSServiceLayerGroup(final List treePaths) {
        originalTreePaths = treePaths;
        sliderName = SLIDER_PREFIX + getUniqueRandomNumber();
        final TreePath tp = ((TreePath)treePaths.get(0));
        final Layer selectedLayer = (de.cismet.cismap.commons.wms.capabilities.Layer)tp.getLastPathComponent();
        evaluateLayerKeywords(selectedLayer);
        final List<Layer> children = Arrays.asList(selectedLayer.getChildren());

        lockTimer = new Timer(timeTillLocked * 1000, lockTimerListener);
        lockTimer.setRepeats(false);

        setName(selectedLayer.getTitle());

        for (final Object path : tp.getPath()) {
            if (path instanceof Layer) {
                if (completePath == null) {
                    completePath = ((Layer)path).getName();
                } else {
                    completePath += "/" + ((Layer)path).getName();
                }
            }
        }

        double maxx = Double.NaN;
        double minx = Double.NaN;
        double maxy = Double.NaN;
        double miny = Double.NaN;
        String srsCode = null;
        boolean usesMultipleSrs = false;

        if (bottomUp) {
            Collections.reverse(children);
        }

        for (final Layer l : children) {
            boolean addLayer = false;
            if (enableAllChildren) {
                addLayer = true;
            } else {
                for (final String keyword : l.getKeywords()) {
                    if (keyword.equalsIgnoreCase("cismapSlidingLayerGroupMember")) {
                        addLayer = true;
                    }
                }
            }

            if (addLayer) {
                final WMSServiceLayer wsl = new WMSServiceLayer(l);
                layers.add(wsl);

                final Position min;
                final Position max;
                final LayerBoundingBox[] boundingBoxes = l.getBoundingBoxes();
                if (boundingBoxes.length > 0) {
                    min = boundingBoxes[0].getMin();
                    max = boundingBoxes[0].getMax();

                    if (srsCode == null) {
                        srsCode = boundingBoxes[0].getSRS();
                    } else if (!srsCode.equalsIgnoreCase(boundingBoxes[0].getSRS())) {
                        usesMultipleSrs = true;
                    }
                } else {
                    final Envelope envelope = l.getLatLonBoundingBoxes();
                    min = envelope.getMin();
                    max = envelope.getMax();

                    if (srsCode == null) {
                        srsCode = "EPSG:4326";
                    } else if (!srsCode.equalsIgnoreCase("EPSG:4326")) {
                        usesMultipleSrs = true;
                    }
                }
                if ((Double.isNaN(maxx)) || (maxx < max.getX())) {
                    maxx = max.getX();
                }
                if ((Double.isNaN(minx)) || (minx > min.getX())) {
                    minx = min.getX();
                }
                if ((Double.isNaN(maxy)) || (maxy < max.getY())) {
                    maxy = max.getY();
                }
                if ((Double.isNaN(miny)) || (miny > min.getY())) {
                    miny = min.getY();
                }
            }
        }

        if (!usesMultipleSrs && (maxx != Double.NaN) && (minx != Double.NaN) && (maxy != Double.NaN)
                    && (miny != Double.NaN)) {
            final Crs srs = CismapBroker.getInstance().crsFromCode(srsCode);

            if (srs != null) {
                this.boundingBox = new XBoundingBox(minx, miny, maxx, maxy, srs.getCode(), srs.isMetric());
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Layer's SRS code '" + srsCode + "' isn't available in cismap.");
                }
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("The children of '" + getName()
                            + "' whether use multiple srs or don't have valid bounding boxes.");
            }
        }

        init(0);
    }

    /**
     * Creates a new SlidableWMSServiceLayerGroup object.
     *
     * @param  element       treePaths DOCUMENT ME!
     * @param  capabilities  DOCUMENT ME!
     */
    public SlidableWMSServiceLayerGroup(final Element element, final HashMap<String, WMSCapabilities> capabilities) {
        orginalCapabilities = capabilities;
        originalElement = element;
        sliderName = SLIDER_PREFIX + getUniqueRandomNumber();
        setName(element.getAttributeValue("name"));

        lockTimer = new Timer(timeTillLocked * 1000, lockTimerListener);
        lockTimer.setRepeats(false);

        try {
            this.setEnabled(element.getAttribute("enabled").getBooleanValue());
        } catch (DataConversionException ex) {
            LOG.warn("Attribute enabled has wrong data type.", ex);
        } catch (final NullPointerException ex) {
            LOG.warn("Attribute enabled not found.", ex);
        }

        try {
            final boolean visible = element.getAttribute("visible").getBooleanValue();
            pnode.setVisible(visible);
        } catch (final DataConversionException e) {
            LOG.warn("Attribute visible has wrong data type.", e);
        } catch (final NullPointerException e) {
            LOG.warn("Attribute visible not found.", e);
        }

        try {
            pnode.setTransparency(element.getAttribute("translucency").getFloatValue());
        } catch (final DataConversionException e) {
            LOG.warn("Attribute translucency has wrong data type.", e);
        } catch (final NullPointerException e) {
            LOG.warn("Attribute translucency not found.", e);
        }

        try {
            completePath = element.getAttribute("completePath").getValue();
        } catch (final NullPointerException e) {
            LOG.warn("Attribute translucency not found.", e);
        }

        try {
            preferredBGColor = element.getAttribute("bgColor").getValue();
            preferredRasterFormat = element.getAttribute("imageFormat").getValue();
            preferredExceptionsFormat = element.getAttribute("exceptionFormat").getValue();
        } catch (final NullPointerException e) {
            LOG.warn("Attribute not found.", e);
        }

        try {
            boundingBox = new XBoundingBox(element);
        } catch (final Exception ex) {
            LOG.warn("Child element BoundingBox not found.", ex);
        }

        final Element layersElement = element.getChild("layers");
        final List layersList = layersElement.getChildren();

        evaluateElementKeywords(element);
        if (bottomUp) {
            Collections.reverse(layersList);
        }

        for (final Object o : layersList) {
            final WMSServiceLayer l = new WMSServiceLayer((Element)o, capabilities);
            layers.add(l);
        }

        try {
            final Element capElement = element.getChild("capabilities");
            final CapabilityLink cp = new CapabilityLink(capElement);
            setWmsCapabilities(capabilities.get(cp.getLink()));
            capabilitiesUrl = cp.getLink();
        } catch (final NullPointerException e) {
            LOG.warn("Child element capabilities not found.", e);
        }

        int sliderValue = 0;
        try {
            sliderValue = element.getAttribute("sliderValue").getIntValue();
        } catch (DataConversionException ex) {
            LOG.warn("Could not load attribute sliderValue.", ex);
        }
        init(sliderValue);
    }

    /**
     * Creates a new SlidableWMSServiceLayerGroup object.
     *
     * <p>Note: Deprecated - please test before using this constructor. If everything works as expected, the deprecated
     * tag can be removed.</p>
     *
     * @param       name             DOCUMENT ME!
     * @param       completePath     DOCUMENT ME!
     * @param       layers           DOCUMENT ME!
     * @param       wmsCapabilities  DOCUMENT ME!
     * @param       capabilitiesUrl  DOCUMENT ME!
     * @param       srs              DOCUMENT ME!
     *
     * @deprecated  DOCUMENT ME!
     */
    public SlidableWMSServiceLayerGroup(final String name,
            final String completePath,
            final Collection<Layer> layers,
            final WMSCapabilities wmsCapabilities,
            final String capabilitiesUrl,
            final Crs srs) {
        sliderName = SLIDER_PREFIX + getUniqueRandomNumber();
        setName(name);
        this.completePath = completePath;
        setWmsCapabilities(wmsCapabilities);

        double maxx = Double.NaN;
        double minx = Double.NaN;
        double maxy = Double.NaN;
        double miny = Double.NaN;

        evaluateLayerKeywords(null);

        for (final Layer l : layers) {
            this.layers.add(new WMSServiceLayer(l));

            final Position min;
            final Position max;
            final LayerBoundingBox[] boundingBoxes = l.getBoundingBoxes();
            if (boundingBoxes.length > 0) {
                min = boundingBoxes[0].getMin();
                max = boundingBoxes[0].getMax();
            } else {
                final Envelope envelope = l.getLatLonBoundingBoxes();
                min = envelope.getMin();
                max = envelope.getMax();
            }
            if ((Double.isNaN(maxx)) || (maxx < max.getX())) {
                maxx = max.getX();
            }
            if ((Double.isNaN(minx)) || (minx > min.getX())) {
                minx = min.getX();
            }
            if ((Double.isNaN(maxy)) || (maxy < max.getY())) {
                maxy = max.getY();
            }
            if ((Double.isNaN(miny)) || (miny > min.getY())) {
                miny = min.getY();
            }
        }

        if ((maxx != Double.NaN) && (minx != Double.NaN) && (maxy != Double.NaN) && (miny != Double.NaN)) {
            this.boundingBox = new XBoundingBox(minx, miny, maxx, maxy, srs.getCode(), srs.isMetric());
        }

        setWmsCapabilities(wmsCapabilities);
        setCapabilitiesUrl(capabilitiesUrl);

        init(0);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Print Mode has to be set true, if this class is used with the headlessMapProvider. If it is not set, the needed
     * retrievalCompleteEvent will not be fired. On the other side if the retrievalCompleteEvent is fired, the sliding
     * functionality will be destroyed, for some reason. Therefore the print mode has to be considered as a hack.
     *
     * @param  printMode  DOCUMENT ME!
     */
    public void setPrintMode(final boolean printMode) {
        this.printMode = printMode;
    }

    /**
     * See setPrintMode().
     *
     * @return  DOCUMENT ME!
     */
    public boolean isPrintMode() {
        return printMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static int getUniqueRandomNumber() {
        int number = 0;

        do {
            number = (new Random(System.currentTimeMillis())).nextInt();
        } while (uniqueNumbers.contains(number));

        uniqueNumbers.add(number);

        return number;
    }

    /**
     * initializes a new SlidableWMSServiceLayerGroup object.
     *
     * @param  sliderValue  the initial position of the slider
     */
    private void init(final int sliderValue) {
        setDefaults();

        for (final WMSServiceLayer wsl : layers) {
            if (capabilitiesUrl == null) {
                capabilitiesUrl = wsl.getCapabilitiesUrl();
            }

            wsl.setPNode(new XPImage());
            pnode.addChild(wsl.getPNode());

            final RetrievalListener retrievalListener = new RetrievalListener() {

                    @Override
                    public void retrievalStarted(final RetrievalEvent e) {
                        fireRetrievalStarted(e);
                    }

                    @Override
                    public void retrievalProgress(final RetrievalEvent e) {
                        final RetrievalEvent event = new RetrievalEvent();
                        progressTable.put(wsl, e.getPercentageDone());
                        int progress = 0;

                        for (final int i : progressTable.values()) {
                            progress += i;
                        }

                        if (!isLocked()) {
                            progress /= layers.size();
                        }

                        SlidableWMSServiceLayerGroup.this.progress = progress;
                        event.setPercentageDone(progress);
                        fireRetrievalProgress(event);
                    }

                    @Override
                    public void retrievalComplete(final RetrievalEvent e) {
                        final Image i = (Image)e.getRetrievedObject();
                        ((XPImage)wsl.getPNode()).setImage(i);
                        new Thread() {

                                @Override
                                public void run() {
                                    final Point2D localOrigin = CismapBroker.getInstance()
                                                .getMappingComponent()
                                                .getCamera()
                                                .getViewBounds()
                                                .getOrigin();
                                    final double localScale = CismapBroker.getInstance()
                                                .getMappingComponent()
                                                .getCamera()
                                                .getViewScale();
                                    wsl.getPNode().setScale(1 / localScale);
                                    wsl.getPNode().setOffset(localOrigin);
                                    layerComplete.incrementAndGet();

                                    if (layerComplete.get() == layers.size()) {
                                        CismapBroker.getInstance().getMappingComponent().repaint();
                                        final RetrievalEvent re = new RetrievalEvent();
                                        re.setIsComplete(true);
                                        re.setRetrievalService(SlidableWMSServiceLayerGroup.this);
                                        re.setHasErrors(false);

                                        re.setRetrievedObject(null);
                                        fireRetrievalComplete(re);
                                        stateChanged(new ChangeEvent(this));
                                        enableSliderAndRestartTimer();
                                        progressTable.clear();
                                    } else if (wsl == getSelectedLayer()) {
                                        CismapBroker.getInstance().getMappingComponent().repaint();
                                    }
                                    if (isPrintMode()) {
                                        fireRetrievalComplete(e);
                                    }
                                }
                            }.start();
                    }

                    @Override
                    public void retrievalAborted(final RetrievalEvent e) {
                        fireRetrievalAborted(e);
                    }

                    @Override
                    public void retrievalError(final RetrievalEvent e) {
                        fireRetrievalError(e);
                    }
                };

            layerRetrievalListeners.put(wsl, retrievalListener);
            wsl.addRetrievalListener(retrievalListener);
            if (wsl.getBackgroundColor() == null) {
                wsl.setBackgroundColor(preferredBGColor);
            }
            if (wsl.getExceptionsFormat() == null) {
                wsl.setExceptionsFormat(preferredExceptionsFormat);
            }
            if (wsl.getImageFormat() == null) {
                wsl.setImageFormat(preferredRasterFormat);
            }
        }

        layers.get(0).setVisible(true);
        initDialog(sliderValue);
        CismapBroker.getInstance().addActiveLayerListener(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  slidableLayerElement  DOCUMENT ME!
     */
    private void evaluateElementKeywords(final Element slidableLayerElement) {
        try {
            resourceConserving = slidableLayerElement.getAttribute("resourceConserving").getBooleanValue();
            timeTillLocked = slidableLayerElement.getAttribute("timeTillLocked").getIntValue();
            inactiveTimeTillLocked = slidableLayerElement.getAttribute("inactiveTimeTillLocked").getIntValue();
            bottomUp = slidableLayerElement.getAttribute("bottomUp").getBooleanValue();
            verticalLabelWidthThreshold = slidableLayerElement.getAttribute("verticalLabelWidthThreshold")
                        .getDoubleValue();
            crossfadeEnabled = slidableLayerElement.getAttribute("crossfadeEnabled").getBooleanValue();
        } catch (final NullPointerException e) {
            LOG.warn("Attribute not found.", e);
        } catch (DataConversionException ex) {
            LOG.warn("Attribute could not be converted.", ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectedLayer  DOCUMENT ME!
     */
    private void evaluateLayerKeywords(final Layer selectedLayer) {
        if (selectedLayer != null) {
            Boolean resourceConserving = null;
            Integer timeTillLocked = null;
            Integer inactiveTimeTillLocked = null;
            Boolean bottomUp = null;
            Double verticalLabelWidthThreshold = null;
            this.enableAllChildren = false;
            this.crossfadeEnabled = false;

            for (final String keyword : selectedLayer.getKeywords()) {
                if (keyword.equalsIgnoreCase("cismapSlidingLayerGroup.config.resourceConserving.enabled")) {
                    resourceConserving = true;
                } else if (keyword.equalsIgnoreCase("cismapSlidingLayerGroup.config.resourceConserving.disabled")) {
                    resourceConserving = false;
                }

                if (keyword.startsWith("cismapSlidingLayerGroup.config.resourceConserving.timeTillLocked")) {
                    try {
                        final String value = keyword.split(":")[1];
                        timeTillLocked = Integer.parseInt(value);
                    } catch (Exception ex) {
                        LOG.error("An error occured while parsing timeTillLocked. Use default value.", ex);
                    }
                }

                if (keyword.startsWith("cismapSlidingLayerGroup.config.resourceConserving.inactiveTimeTillLocked")) {
                    try {
                        final String value = keyword.split(":")[1];
                        inactiveTimeTillLocked = Integer.parseInt(value);
                    } catch (Exception ex) {
                        LOG.error("An error occured while parsing inactiveTimeTillLocked. Use default value.", ex);
                    }
                }

                if (keyword.equalsIgnoreCase("cismapSlidingLayerGroup.config.bottomUp")) {
                    bottomUp = true;
                } else if (keyword.equalsIgnoreCase("cismapSlidingLayerGroup.config.topDown")) {
                    bottomUp = false;
                }

                if (keyword.startsWith("cismapSlidingLayerGroup.config.verticalLabelWidthThreshold")) {
                    try {
                        final String value = keyword.split(":")[1];
                        verticalLabelWidthThreshold = Double.parseDouble(value);
                    } catch (Exception ex) {
                        LOG.error("An error occured while parsing inactiveTimeTillLocked. Use default value.", ex);
                    }
                }

                if (keyword.equalsIgnoreCase("cismapSlidingLayerGroup.config.enableAllChildren")) {
                    this.enableAllChildren = true;
                }

                if (keyword.equalsIgnoreCase("cismapSlidingLayerGroup.config.crossfadeEnabled")) {
                    this.crossfadeEnabled = true;
                }
            }

            if (resourceConserving == null) {
                this.resourceConserving = RESOURCE_CONSERVING;
            } else {
                this.resourceConserving = resourceConserving;
            }

            if (timeTillLocked == null) {
                this.timeTillLocked = TIME_TILL_LOCKED;
            } else {
                this.timeTillLocked = timeTillLocked;
            }

            if (inactiveTimeTillLocked == null) {
                this.inactiveTimeTillLocked = INACTIVE_TIME_TILL_LOCKED;
            } else {
                this.inactiveTimeTillLocked = inactiveTimeTillLocked;
            }

            if (bottomUp == null) {
                this.bottomUp = BOTTOM_UP;
            } else {
                this.bottomUp = bottomUp;
            }

            if (verticalLabelWidthThreshold == null) {
                this.verticalLabelWidthThreshold = VERTICAL_LABEL_WIDTH_THRESHOLD;
            } else {
                this.verticalLabelWidthThreshold = verticalLabelWidthThreshold;
            }
        } else {
            resourceConserving = RESOURCE_CONSERVING;
            timeTillLocked = TIME_TILL_LOCKED;
            inactiveTimeTillLocked = INACTIVE_TIME_TILL_LOCKED;
            bottomUp = BOTTOM_UP;
            verticalLabelWidthThreshold = VERTICAL_LABEL_WIDTH_THRESHOLD;
            enableAllChildren = false;
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void setDefaults() {
        preferredRasterFormat = "image/png";                      // NOI18N
        preferredBGColor = "0xF0F0F0";                            // NOI18N
        preferredExceptionsFormat = "application/vnd.ogc.se_xml"; // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wmsCapabilities  DOCUMENT ME!
     */
    public void setWmsCapabilities(final WMSCapabilities wmsCapabilities) {
        this.wmsCapabilities = wmsCapabilities;
        for (final WMSServiceLayer layer : layers) {
            layer.setWmsCapabilities(wmsCapabilities);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  capabilitiesUrl  DOCUMENT ME!
     */
    public void setCapabilitiesUrl(final String capabilitiesUrl) {
        this.capabilitiesUrl = capabilitiesUrl;
        for (final WMSServiceLayer layer : layers) {
            layer.setCapabilitiesUrl(capabilitiesUrl);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  srs  DOCUMENT ME!
     */
    public void setSrs(final String srs) {
        for (final WMSServiceLayer layer : layers) {
            layer.setSrs(srs);
        }
    }

    /**
     * Initializes the internal frame, which contains the slider.
     *
     * @param  sliderValue  the initial position of the slider
     */
    private void initDialog(final int sliderValue) {
        internalFrame = new SlidableWMSServiceLayerGroupInternalFrame(this, sliderValue);
        setLocked(resourceConserving);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnLockResultsActionPerformed(final ActionEvent evt) {
        setLocked(!isLocked());
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
        if (getPNode() == null) {
            return;
        }

        final int i = (internalFrame.getSliderValue() / 100);
        final int rest = internalFrame.getSliderValue() % 100;

        for (int j = 0; j < getPNode().getChildrenCount(); ++j) {
            if (i == j) {
                getPNode().getChild(i).setTransparency(1f);
            } else {
                getPNode().getChild(j).setTransparency(0f);
            }
        }
        if (internalFrame.isAllowCrossfade() && ((i + 1) < getPNode().getChildrenCount())) {
            getPNode().getChild(i + 1).setTransparency(((float)rest) / 100f);
        }

        if (lockTimer.isRunning()) {
            lockTimer.restart();
        }
    }

    @Override
    public PNode getPNode() {
        return pnode;
    }

    @Override
    public void setPNode(final PNode imageObject) {
        pnode = imageObject;
    }

    @Override
    public void retrieve(final boolean forced) {
        if (enabled || forced) {
            // these fields are needed to determine the progress of the retrieval
            progress = -1;
            layerComplete.set(0);
            progressTable.clear();

            // the slider is always disabled during the retrieval of the layers and might be enabled later on when all
            // the layers are completely loaded
            internalFrame.enableSlider(false);

            // stop the timer, otherwise it can happen that SlidableWMSServiceLayerGroup gets locked during the
            // retrieval.
            lockTimer.stop();

            if (isLocked()) {
                getSelectedLayer().retrieve(forced);
            } else {
                for (final WMSServiceLayer layer : layers) {
                    layer.retrieve(forced);
                }
            }
            setRefreshNeeded(false);
        }
    }

    @Override
    public boolean canBeDisabled() {
        return true;
    }

    @Override
    public int getLayerPosition() {
        return layerPosition;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Provides the bounding box of this layer. The bounding box represents the extent of the children's bounding boxes.
     *
     * @return  Extent of this layer.
     */
    public XBoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Returns the path of this layer. It's formatted by concatenating the names of parent layers with '/' as delimiter.
     * /[&lt;...&gt;/]&lt;Name of grand parent&gt;/&lt;Name of parent&gt;/&lt;Name of this layer&gt;
     *
     * @return  Extent of this layer.
     */
    public String getPath() {
        return completePath;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  customSLD  DOCUMENT ME!
     */
    public void setCustomSLD(final String customSLD) {
        this.customSLD = customSLD;

        for (final WMSServiceLayer layer : layers) {
            layer.setCustomSLD(this.customSLD);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCustomSLD() {
        return customSLD;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        for (final WMSServiceLayer layer : layers) {
            layer.setEnabled(enabled);
        }
    }

    @Override
    public void setLayerPosition(final int layerPosition) {
        this.layerPosition = layerPosition;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public float getTranslucency() {
        return translucency;
    }

    @Override
    public void setTranslucency(final float t) {
        translucency = t;
    }

    @Override
    public Object clone() {
        SlidableWMSServiceLayerGroup clonedLayer;
        if (originalTreePaths != null) {
            clonedLayer = new SlidableWMSServiceLayerGroup(originalTreePaths);
            clonedLayer.setWmsCapabilities(wmsCapabilities);
        } else if (originalElement != null) {
            clonedLayer = new SlidableWMSServiceLayerGroup(originalElement, orginalCapabilities);
        } else {
            LOG.error("Could not clone SlidableWMSServiceLayerGroup.", new Exception());
            return null;
        }

        clonedLayer.setBoundingBox(boundingBox);
        clonedLayer.setCapabilitiesUrl(capabilitiesUrl);
        clonedLayer.setCustomSLD(customSLD);
        clonedLayer.setEnabled(enabled);
        clonedLayer.setLayerPosition(layerPosition);
        clonedLayer.setLayerQuerySelected(layerQuerySelected);
        clonedLayer.setLocked(true);
        clonedLayer.setName(name);
        // The cloned service layer and the origin service layer should not use the same pnode,
        // because this would lead to problems, if the cloned layer and the origin layer are
        // used in 2 different MappingComponents
        // This has to be set afterwards.
        clonedLayer.setPNode(null);
        clonedLayer.setTranslucency(this.getTranslucency());
        clonedLayer.setSliderValue(internalFrame.getSliderValue());

        return clonedLayer;
    }

    @Override
    public boolean isVisible() {
        return pnode.getVisible();
    }

    @Override
    public void setBoundingBox(final BoundingBox bb) {
        for (final WMSServiceLayer layer : layers) {
            layer.setBoundingBox(bb);
        }
    }

    @Override
    public void setSize(final int height, final int width) {
        for (final WMSServiceLayer layer : layers) {
            layer.setSize(height, width);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getLayerURI() {
        return getName();
    }

    @Override
    public String getServerURI() {
        return capabilitiesUrl;
    }

    @Override
    public boolean isLayerQuerySelected() {
        return layerQuerySelected;
    }

    @Override
    public void setLayerQuerySelected(final boolean selected) {
        layerQuerySelected = selected;
    }

    @Override
    public boolean isQueryable() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getVerticalLabelWidthThreshold() {
        return verticalLabelWidthThreshold;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isCrossfadeEnabled() {
        return crossfadeEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isResourceConserving() {
        return resourceConserving;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the layer capabilities object of the current layer
     */
    @Override
    public Layer getLayerInformation() {
        if (wmsCapabilities != null) {
            return getLayerInformation(null, wmsCapabilities.getLayer().getChildren());
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   path        DOCUMENT ME!
     * @param   layerArray  DOCUMENT ME!
     *
     * @return  the layer capabilities object of the current layer
     */
    private Layer getLayerInformation(final String path, final Layer[] layerArray) {
        if (layerArray != null) {
            for (final Layer l : layerArray) {
                final String currentPath = ((path == null) ? l.getName() : (path + "/" + l.getName()));
                if (currentPath.equals(completePath)) {
                    return l;
                } else {
                    final Layer res = getLayerInformation(currentPath, l.getChildren());
                    if (res != null) {
                        return res;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof SlidableWMSServiceLayerGroup) {
            final SlidableWMSServiceLayerGroup other = (SlidableWMSServiceLayerGroup)obj;
            final Iterator<WMSServiceLayer> otherLayerIt = other.layers.iterator();

            for (final WMSServiceLayer l : layers) {
                if (otherLayerIt.hasNext()) {
                    final WMSServiceLayer lOther = otherLayerIt.next();
                    if (!lOther.propertyEquals(l)) {
                        return false;
                    }
                }
            }

            return other.layers.size() == layers.size();
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        for (final WMSServiceLayer l : layers) {
            hash += l.hashCode() % 71;
        }

        return hash;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Locks the SlidableWMSServiceLayerGroup, this is not possible if RESOURCE_CONSERVING is false. Locked means that
     * the slider is disabled (generally) and that only the currently selected layer gets loaded. Otherwise all the
     * layers get loaded every time.
     *
     * @param  locked  DOCUMENT ME!
     */
    public void setLocked(final boolean locked) {
        if (resourceConserving) {
            this.locked = locked;

            if (locked) {
                internalFrame.setLockIcon();
                internalFrame.enableSlider(false || doNotDisableSlider);
            } else {
                internalFrame.setUnlocIcon();
                doNotDisableSlider = false;
                // refresh the other layers
                this.retrieve(false);
            }
        } else {
            this.locked = false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ActionListener getLockListener() {
        return btnLockListener;
    }

    /**
     * DOCUMENT ME!
     */
    private void enableSliderAndRestartTimer() {
        final Runnable r = new Runnable() {

                @Override
                public void run() {
                    internalFrame.enableSlider(true);
                    lockTimer.restart();
                }
            };
        if (!isLocked()) {
            if (SwingUtilities.isEventDispatchThread()) {
                r.run();
            } else {
                SwingUtilities.invokeLater(r);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<WMSServiceLayer> getLayers() {
        return layers;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element toElement() {
        final Element element = new Element(XML_ELEMENT_NAME);
        element.setAttribute("name", getName());
        element.setAttribute("visible", (getPNode().getVisible() ? "true" : "false"));
        element.setAttribute("enabled", Boolean.toString(isEnabled()));
        element.setAttribute("translucency", "" + getPNode().getTransparency());
        element.setAttribute("bgColor", preferredBGColor);
        element.setAttribute("imageFormat", preferredRasterFormat);
        element.setAttribute("exceptionFormat", preferredExceptionsFormat);
        element.setAttribute("completePath", String.valueOf(completePath));

        // set the slidable layer keywords
        element.setAttribute("resourceConserving", Boolean.toString(resourceConserving));
        element.setAttribute("timeTillLocked", Integer.toString(timeTillLocked));
        element.setAttribute("inactiveTimeTillLocked", Integer.toString(inactiveTimeTillLocked));
        element.setAttribute("bottomUp", Boolean.toString(bottomUp));
        element.setAttribute("verticalLabelWidthThreshold", Double.toString(verticalLabelWidthThreshold));
        element.setAttribute("crossfadeEnabled", Boolean.toString(crossfadeEnabled));

        element.setAttribute("sliderValue", Integer.toString(internalFrame.getSliderValue()));

        if (boundingBox != null) {
            element.addContent(boundingBox.getJDOMElement());
        }

        final Element capElement = new Element("capabilities"); // NOI18N
        final CapabilityLink capLink = new CapabilityLink(CapabilityLink.OGC, capabilitiesUrl, false);
        capElement.addContent(capLink.getElement());

        element.addContent(capElement);
        final Element layersElement = new Element("layers"); // NOI18N
        for (final WMSServiceLayer l : layers) {
            final Element layerElement = l.getElement();
            layerElement.setAttribute("name", internalFrame.getTickTitle(l) + LAYERNAME_FROM_CONFIG_SUFFIX);
            layersElement.addContent(layerElement);
        }
        element.addContent(layersElement);
        return element;
    }

    @Override
    public void layerAdded(final ActiveLayerEvent e) {
    }

    @Override
    public void layerRemoved(final ActiveLayerEvent e) {
        if (e.getLayer() == this) {
            if ((addedInternalWidget != null) && addedInternalWidget.equals(sliderName)) {
                CismapBroker.getInstance().getMappingComponent().removeInternalWidget(sliderName);
                internalFrame.removeModel();
//                internalFrame.dispose();
                internalFrame = null;
                addedInternalWidget = null;
            }

            // use invoke later to avoid a java.util.ConcurrentModificationException
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        CismapBroker.getInstance().removeActiveLayerListener(SlidableWMSServiceLayerGroup.this);
                    }
                });
            try {
                uniqueNumbers.remove(Integer.valueOf(sliderName.substring(SLIDER_PREFIX.length())));
            } catch (final NumberFormatException ex) {
                LOG.error("The name of the internal slider widget is not valid.", ex);
            }
            lockTimer.removeActionListener(lockTimerListener);
            lockTimer.stop();

            for (final WMSServiceLayer wsl : layers) {
                wsl.removeRetrievalListener(layerRetrievalListeners.get(wsl));
            }
        }
    }

    @Override
    public void layerPositionChanged(final ActiveLayerEvent e) {
    }

    @Override
    public void layerVisibilityChanged(final ActiveLayerEvent e) {
        if ((e.getLayer() == this) && (getPNode() != null)) {
            boolean fadeOutOldWidget = false;
            boolean fadeInThisWidget = false;

            if (!getPNode().getVisible()) {
                fadeOutOldWidget = (addedInternalWidget != null) && addedInternalWidget.equals(sliderName);
            } else {
                fadeInThisWidget = selected;
            }

            if (fadeOutOldWidget) {
                CismapBroker.getInstance().getMappingComponent().removeInternalWidget(addedInternalWidget);
                addedInternalWidget = null;
            }

            if (fadeInThisWidget) {
                CismapBroker.getInstance()
                        .getMappingComponent()
                        .addInternalWidget(sliderName, MappingComponent.POSITION_NORTHEAST, internalFrame);
                addedInternalWidget = sliderName;
                CismapBroker.getInstance().getMappingComponent().showInternalWidget(sliderName, true, 800);
            }
        }
    }

    @Override
    public void layerAvailabilityChanged(final ActiveLayerEvent e) {
    }

    @Override
    public void layerInformationStatusChanged(final ActiveLayerEvent e) {
    }

    @Override
    public synchronized void layerSelectionChanged(final ActiveLayerEvent e) {
        selected = e.getLayer() == this;

        if (e.getLayer() == this) {
            if (addedInternalWidget != null) {
                CismapBroker.getInstance().getMappingComponent().removeInternalWidget(addedInternalWidget);
                CismapBroker.getInstance().getMappingComponent().repaint();
            }

            if ((getPNode() != null) && getPNode().getVisible()) {
                CismapBroker.getInstance()
                        .getMappingComponent()
                        .addInternalWidget(sliderName, MappingComponent.POSITION_NORTHEAST, internalFrame);
                addedInternalWidget = sliderName;
                CismapBroker.getInstance().getMappingComponent().showInternalWidget(sliderName, true, 800);
            }
        } else {
            if ((addedInternalWidget != null) && !(e.getLayer() instanceof SlidableWMSServiceLayerGroup)) {
                CismapBroker.getInstance().getMappingComponent().showInternalWidget(addedInternalWidget, false, 800);
            }
        }

        updateTimerDelay();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private WMSServiceLayer getSelectedLayer() {
        final int i = (internalFrame.getSliderValue() / 100);

        if (i < layers.size()) {
            return layers.get(i);
        } else {
            return layers.get(layers.size() - 1);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  value  DOCUMENT ME!
     */
    private void setSliderValue(final int value) {
        internalFrame.setSliderValue(value);
    }

    /**
     * DOCUMENT ME!
     */
    private void updateTimerDelay() {
        if (selected) {
            lockTimer.setDelay(timeTillLocked * 1000);
            lockTimer.setInitialDelay(timeTillLocked * 1000);
        } else {
            lockTimer.setDelay(inactiveTimeTillLocked * 1000);
            lockTimer.setInitialDelay(inactiveTimeTillLocked * 1000);
        }

        if (lockTimer.isRunning()) {
            lockTimer.restart();
        }
    }
}
