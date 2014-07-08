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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.LayerInfoProvider;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.FloatingControlProvider;
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
    FloatingControlProvider,
    RasterMapService,
    ChangeListener,
    MapService,
    LayerInfoProvider,
    ActiveLayerListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(SlidableWMSServiceLayerGroup.class);

    public static final String XML_ELEMENT_NAME = "SlidableWMSServiceLayerGroup"; // NOI18N
    private static final String SLIDER_PREFIX = "Slider";                         // NOI18N
    private static List<Integer> uniqueNumbers = new ArrayList<Integer>();
    private static String addedInternalWidget = null;

    private static final ImageIcon LOCK_ICON = new javax.swing.ImageIcon(
            SlidableWMSServiceLayerGroup.class.getResource("/de/cismet/cismap/commons/raster/wms/res/lock.png"));        // NOI18N
    private static final ImageIcon UNLOCK_ICON = new javax.swing.ImageIcon(
            SlidableWMSServiceLayerGroup.class.getResource("/de/cismet/cismap/commons/raster/wms/res/lock-unlock.png")); // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final List<WMSServiceLayer> layers = new ArrayList<WMSServiceLayer>();
    private boolean layerQuerySelected = false;
    private final String sliderName;
    private PNode pnode = new XPImage();
    private JSlider slider = new JSlider();
    private JDialog dialog = new JDialog();
    private JInternalFrame internalFrame = new JInternalFrame();
    private int layerPosition;
    private String name;
    private String completePath = null;
    private Map<WMSServiceLayer, Integer> progressTable = new HashMap<WMSServiceLayer, Integer>();
    private int layerComplete = 0;
    private String preferredRasterFormat;
    private String preferredBGColor;
    private String preferredExceptionsFormat;
    private String capabilitiesUrl = null;
    private WMSCapabilities wmsCapabilities;
    private XBoundingBox boundingBox;
    private String customSLD;
    private boolean selected = false;
    private JButton btnLock = new JButton();
    private boolean locked;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SlidableWMSServiceLayerGroup object.
     *
     * @param  treePaths  DOCUMENT ME!
     */
    public SlidableWMSServiceLayerGroup(final List treePaths) {
        sliderName = SLIDER_PREFIX + getUniqueRandomNumber();
        final TreePath tp = ((TreePath)treePaths.get(0));
        final Layer selectedLayer = (de.cismet.cismap.commons.wms.capabilities.Layer)tp.getLastPathComponent();
        final Layer[] children = selectedLayer.getChildren();

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
        for (final Layer l : children) {
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

        init();
        setLocked(true);
    }

    /**
     * Creates a new SlidableWMSServiceLayerGroup object.
     *
     * @param  element       treePaths DOCUMENT ME!
     * @param  capabilities  DOCUMENT ME!
     */
    public SlidableWMSServiceLayerGroup(final Element element, final HashMap<String, WMSCapabilities> capabilities) {
        sliderName = SLIDER_PREFIX + getUniqueRandomNumber();
        setName(element.getAttributeValue("name"));

        try {
            pnode.setVisible(element.getAttribute("visible").getBooleanValue());
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
            final Element capElement = element.getChild("capabilities");
            final CapabilityLink cp = new CapabilityLink(capElement);
            wmsCapabilities = capabilities.get(cp.getLink());
            capabilitiesUrl = cp.getLink();
        } catch (final NullPointerException e) {
            LOG.warn("Child element capabilities not found.", e);
        }

        try {
            boundingBox = new XBoundingBox(element);
        } catch (final Exception ex) {
            LOG.warn("Child element BoundingBox not found.", ex);
        }

        final Element layersElement = element.getChild("layers");
        final List layersList = layersElement.getChildren();

        for (final Object o : layersList) {
            layers.add(new WMSServiceLayer((Element)o, capabilities));
        }

        init();
    }

    /**
     * Creates a new SlidableWMSServiceLayerGroup object.
     *
     * @param  name             DOCUMENT ME!
     * @param  completePath     DOCUMENT ME!
     * @param  layers           DOCUMENT ME!
     * @param  wmsCapabilities  DOCUMENT ME!
     * @param  capabilitiesUrl  DOCUMENT ME!
     * @param  srs              DOCUMENT ME!
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
        this.wmsCapabilities = wmsCapabilities;

        double maxx = Double.NaN;
        double minx = Double.NaN;
        double maxy = Double.NaN;
        double miny = Double.NaN;
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

        init();
    }

    //~ Methods ----------------------------------------------------------------

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
     * initialises a new SlidableWMSServiceLayerGroup object.
     */
    private void init() {
        setDefaults();

        for (final WMSServiceLayer wsl : layers) {
            if (capabilitiesUrl == null) {
                capabilitiesUrl = wsl.getCapabilitiesUrl();
            }

            wsl.setPNode(new XPImage());
            pnode.addChild(wsl.getPNode());
            wsl.addRetrievalListener(new RetrievalListener() {

                    @Override
                    public void retrievalStarted(final RetrievalEvent e) {
                        progress = -1;
                        layerComplete = 0;
                        progressTable.clear();
                        fireRetrievalStarted(e);
                    }

                    @Override
                    public void retrievalProgress(final RetrievalEvent e) {
                        final RetrievalEvent event = new RetrievalEvent();
                        progressTable.put(wsl, e.getPercentageDone());
                        progress = 0;

                        for (final int i : progressTable.values()) {
                            progress += i;
                        }

                        if (!isLocked()) {
                            progress /= layers.size();
                        }

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
                                CismapBroker.getInstance().getMappingComponent().repaint();
                                ++layerComplete;

                                if (layerComplete == layers.size()) {
                                    final RetrievalEvent re = new RetrievalEvent();
                                    re.setIsComplete(true);
                                    re.setRetrievalService(SlidableWMSServiceLayerGroup.this);
                                    re.setHasErrors(false);

                                    re.setRetrievedObject(null);
                                    fireRetrievalComplete(re);
                                    stateChanged(new ChangeEvent(this));
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
                });
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
        initDialog();
        CismapBroker.getInstance().addActiveLayerListener(this);
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
     * DOCUMENT ME!
     */
    private void initDialog() {
        dialog.getContentPane().setLayout(new BorderLayout());

        slider.setMinimum(0);
        slider.setMaximum((layers.size() - 1) * 100);
        slider.setValue(0);

        slider.setMinorTickSpacing(100);
        slider.addChangeListener(this);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        final Hashtable lableTable = new Hashtable();
        int x = 0;
        for (final WMSServiceLayer wsl : layers) {
            String layerTitle = wsl.getTitle();
            if (layerTitle == null) {
                layerTitle = wsl.getName();
            }

            if ((layerTitle != null) && (layerTitle.length() > 8)) {
                layerTitle = layerTitle.substring(0, 3) + "." + layerTitle.substring(layerTitle.length() - 4); // NOI18N
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No title found for WMSServiceLayer '" + wsl + "'.");
                }
            }

            final JLabel label = new JLabel(layerTitle);
            final Font font = label.getFont().deriveFont(10f);
            label.setFont(font);
            lableTable.put(x * 100, label);
            x++;
        }
        slider.setLabelTable(lableTable);
        internalFrame.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE); // NOI18N
        internalFrame.getContentPane().setLayout(new BorderLayout());
        internalFrame.getContentPane().add(slider, BorderLayout.CENTER);
        slider.setSnapToTicks(true);
        slider.repaint();

        btnLock.setText("");
        btnLock.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnLockResultsActionPerformed(evt);
                }
            });

        btnLock.setIcon(LOCK_ICON);
        btnLock.setBorder(null);
        btnLock.setContentAreaFilled(false);
        btnLock.setPreferredSize(new Dimension(32, (int)slider.getPreferredSize().getHeight()));
        internalFrame.getContentPane().add(btnLock, BorderLayout.WEST);

        internalFrame.setPreferredSize(new Dimension(
                (int)getSliderWidth(),
                (int)slider.getPreferredSize().getHeight()
                        + 15));
        internalFrame.pack();
        internalFrame.setResizable(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnLockResultsActionPerformed(final ActionEvent evt) {
        setLocked(!isLocked());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSliderWidth() {
        final Dictionary dic = slider.getLabelTable();
        final Enumeration e = dic.keys();
        final StringBuilder text = new StringBuilder();

        while (e.hasMoreElements()) {
            final Object o = dic.get(e.nextElement());
            if (o instanceof JLabel) {
                text.append(((JLabel)o).getText());
                text.append("  ");
            }
        }
        return slider.getFontMetrics(slider.getFont()).getStringBounds(text.toString(), slider.getGraphics())
                    .getWidth();
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
        final int i = (slider.getValue() / 100);
        final int rest = slider.getValue() % 100;

        for (int j = 0; j < getPNode().getChildrenCount(); ++j) {
            if (i == j) {
                getPNode().getChild(i).setTransparency(1f);
            } else {
                getPNode().getChild(j).setTransparency(0f);
            }
        }
        if ((i + 1) < getPNode().getChildrenCount()) {
            getPNode().getChild(i + 1).setTransparency(((float)rest) / 100f);
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
    public JDialog getFloatingControlComponent() {
        return dialog;
    }

    @Override
    public void retrieve(final boolean forced) {
        if (isLocked()) {
            getSelectedLayer().retrieve(forced);
        } else {
            for (final WMSServiceLayer layer : layers) {
                layer.retrieve(forced);
            }
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

    @Override
    public float getTranslucency() {
        return pnode.getTransparency();
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
     * /[<...>/]<Name of grand parent>/<Name of parent>/<Name of this layer>
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
        return true;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        // won't do anything
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
    public void setTranslucency(final float t) {
        pnode.setTransparency(t);
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isVisible() {
        return true;
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
     * DOCUMENT ME!
     *
     * @param  locked  DOCUMENT ME!
     */
    public void setLocked(final boolean locked) {
        this.locked = locked;
        slider.setEnabled(!locked);

        if (locked) {
            btnLock.setIcon(LOCK_ICON);
        } else {
            btnLock.setIcon(UNLOCK_ICON);

            // refresh the other layers
            this.retrieve(false);
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
        element.setAttribute("completePath", completePath);

        if (boundingBox != null) {
            element.addContent(boundingBox.getJDOMElement());
        }

        final Element capElement = new Element("capabilities"); // NOI18N
        final CapabilityLink capLink = new CapabilityLink(CapabilityLink.OGC, capabilitiesUrl, false);
        capElement.addContent(capLink.getElement());

        element.addContent(capElement);
        final Element layerElement = new Element("layers"); // NOI18N
        for (final WMSServiceLayer l : layers) {
            layerElement.addContent(l.getElement());
        }
        element.addContent(layerElement);
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
                addedInternalWidget = null;
            }

            // use invoke lateer to avoid a java.util.ConcurrentModificationException
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
                CismapBroker.getInstance().getMappingComponent().showInternalWidget(addedInternalWidget, false, 800);
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
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private WMSServiceLayer getSelectedLayer() {
        final int i = (slider.getValue() / 100);

        if (i < layers.size()) {
            return layers.get(i);
        } else {
            return layers.get(layers.size() - 1);
        }
    }
}
