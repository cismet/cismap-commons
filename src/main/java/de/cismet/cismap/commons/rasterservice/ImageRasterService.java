/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.rasterservice;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.umd.cs.piccolo.PNode;

import org.apache.commons.httpclient.HttpClient;

import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Element;

import java.io.File;

import java.util.ArrayList;
import java.util.Collections;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.LayerInfoProvider;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.cismap.commons.wms.capabilities.Layer;

import de.cismet.tools.CurrentStackTrace;

/**
 * This service can be used like a wms, but its data sources are image files.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ImageRasterService extends AbstractRetrievalService implements MapService,
    RasterMapService,
    RetrievalServiceLayer,
    LayerInfoProvider,
    RetrievalListener,
    ServiceLayer { // implements RasterService,RetrievalListener,ServiceLayer {

    //~ Instance fields --------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */

    private final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(this.getClass());
    private File imageFile;
    private ImageFileRetrieval ir;
    private PNode pNode;
    private String name = "ImageRasterService"; // NOI18N
    private HttpClient preferredClient = null;
    private BoundingBox bb;
    private boolean enabled = true;
    private int height = 0;
    private int width = 0;
    private int layerPosition = 0;
    private float translucency = 1.0f;
    private boolean visible = true;
    private Geometry envelope;
    private ImageFileUtils.Mode mode;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SimpleWMS object.
     *
     * @param  s  DOCUMENT ME!
     */
    public ImageRasterService(final ImageRasterService s) {
        this(s.imageFile, s.mode);
        if ((BoundingBox)s.bb != null) {
            bb = (BoundingBox)s.bb.clone();
        }
        enabled = s.enabled;
        height = s.height;
        layerPosition = s.layerPosition;
        name = s.name;
        // The cloned service and the origin service should not use the same pnode,
        // because this would lead to problems, if the cloned layer and the origin layer are
        // used in 2 different MappingComponents
// pNode = s.pNode;
        translucency = s.translucency;
        width = s.width;
        ir = new ImageFileRetrieval(s.imageFile, this, s.mode);
        listeners = new ArrayList<RetrievalListener>();
        listeners = Collections.synchronizedList(listeners);
        listeners.addAll(s.listeners);
    }

    /**
     * Creates a new SimpleWMS object.
     *
     * @param   object  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ImageRasterService(final Element object) throws Exception {
        final String filePath = object.getTextTrim();
        imageFile = new File(filePath);
        final Attribute layerPositionAttr = object.getAttribute("layerPosition"); // NOI18N
        if (layerPositionAttr != null) {
            try {
                layerPosition = layerPositionAttr.getIntValue();
            } catch (Exception e) {
            }
        }
        final Attribute enabledAttr = object.getAttribute("enabled");             // NOI18N
        if (enabledAttr != null) {
            try {
                enabled = enabledAttr.getBooleanValue();
            } catch (Exception e) {
            }
        }
        final Attribute nameAttr = object.getAttribute("name");                   // NOI18N
        if (nameAttr != null) {
            try {
                name = nameAttr.getValue();
            } catch (Exception e) {
            }
        }
        final Attribute visAttr = object.getAttribute("visible");                 // NOI18N
        if (visAttr != null) {
            try {
                visible = visAttr.getBooleanValue();
                pNode.setVisible(visible);
            } catch (Exception e) {
            }
        }
        final Attribute translucencyAttr = object.getAttribute("translucency");   // NOI18N
        if (translucencyAttr != null) {
            try {
                setTranslucency(translucencyAttr.getFloatValue());
            } catch (Exception e) {
            }
        }
        final File worldFile = ImageFileUtils.getWorldFile(imageFile);
        if (worldFile != null) {
            mode = ImageFileUtils.checkIfRasterGeoRef(worldFile) ? ImageFileUtils.Mode.GEO_REFERENCED
                                                                 : ImageFileUtils.Mode.WORLDFILE;
        }
    }

    /**
     * Creates a new instance of SimpleWMS.
     *
     * @param  imageFile  gmUrl DOCUMENT ME!
     * @param  mode       DOCUMENT ME!
     */
    public ImageRasterService(final File imageFile, final ImageFileUtils.Mode mode) {
        this.imageFile = imageFile;
        this.name = imageFile.getName();
        this.mode = mode;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ImageFileUtils.Mode getMode() {
        return mode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getElement() {
        final Element element = new Element("ImageRasterService");              // NOI18N
        element.setAttribute("layerPosition", Integer.toString(layerPosition)); // NOI18N
        element.setAttribute("type", getClass().getName());
        element.setAttribute("enabled", Boolean.toString(enabled));
        element.setAttribute("visible", Boolean.toString(pNode.getVisible()));  // NOI18N
        element.setAttribute("name", name);                                     // NOI18N
        element.setAttribute("translucency", Float.toString(translucency));     // NOI18N
        final CDATA data = new CDATA(imageFile.getAbsolutePath());
        element.addContent(data);
        return element;
    }

    @Override
    public synchronized void retrieve(final boolean forced) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrieve()"); // NOI18N
        }

        final ImageFileRetrieval ifr = new ImageFileRetrieval(imageFile, this, mode);
        ifr.setHeight(height);
        ifr.setWidth(width);
        ifr.setX1(bb.getX1());
        ifr.setY1(bb.getY1());
        ifr.setX2(bb.getX2());
        ifr.setY2(bb.getY2());

        if ((ir != null) && ir.isAlive() && ifr.equals(ir) && !forced) {
            if (LOG.isDebugEnabled()) {
                // mach nix
                // mehrfachaufruf mit der gleichen url = unsinn
                LOG.debug("multiple invocations with the same url = humbug"); // NOI18N
            }
        } else {
            if ((ir != null) && ir.isAlive()) {
                ir.youngerCall();
                ir.interrupt();
                retrievalAborted(new RetrievalEvent());
            }

            if (ir != null) {
                ifr.copyMetaData(ir);
            }
            ir = ifr;

            if (LOG.isDebugEnabled()) {
                LOG.debug("ir.start();"); // NOI18N
            }

            ir.setPriority(Thread.NORM_PRIORITY);
            ir.start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public File getImageFile() {
        return imageFile;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  imageFile  gmUrl DOCUMENT ME!
     */
    public void setImageFile(final File imageFile) {
        this.imageFile = imageFile;
    }

    @Override
    public void setPNode(final PNode imageObject) {
        pNode = imageObject;
    }

    @Override
    public PNode getPNode() {
        return pNode;
    }

    @Override
    public Object clone() {
        return new ImageRasterService(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public HttpClient getPreferredClient() {
        return preferredClient;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  preferredClient  DOCUMENT ME!
     */
    public void setPreferredClient(final HttpClient preferredClient) {
        this.preferredClient = preferredClient;
    }

    @Override
    public String getLayerURI() {
        return imageFile.getAbsolutePath();
    }

    @Override
    public String getServerURI() {
        return imageFile.getAbsolutePath();
    }

    @Override
    public boolean isLayerQuerySelected() {
        return false;
    }

    @Override
    public void setLayerQuerySelected(final boolean selected) {
    }

    @Override
    public boolean isQueryable() {
        return false;
    }

    @Override
    public Layer getLayerInformation() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry getEnvelope() {
        if (envelope == null) {
            final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING));
            final Envelope en = new ImageFileRetrieval(imageFile, this, mode).getEnvelope();

            if (en != null) {
                envelope = gf.toGeometry(en);
            }
        }

        return envelope;
    }

    @Override
    public void setBoundingBox(final de.cismet.cismap.commons.BoundingBox bb) {
        this.bb = bb;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void setSize(final int height, final int width) {
        this.height = height;
        this.width = width;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean canBeDisabled() {
        return true;
    }

    @Override
    public void retrievalStarted(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        this.fireRetrievalStarted(e);
    }

    @Override
    public void retrievalProgress(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        this.fireRetrievalProgress(e);
    }

    @Override
    public void retrievalError(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        LOG.warn("retrievalError", new CurrentStackTrace()); // NOI18N
        this.fireRetrievalError(e);
    }

    @Override
    public void retrievalComplete(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        if ((ir == null) || ir.isAlive()) {
            this.fireRetrievalComplete(e);
        }
    }

    @Override
    public void retrievalAborted(final de.cismet.cismap.commons.retrieval.RetrievalEvent e) {
        this.fireRetrievalAborted(e);
    }

    @Override
    public int getLayerPosition() {
        return layerPosition;
    }

    @Override
    public void setLayerPosition(final int layerPosition) {
        this.layerPosition = layerPosition;
    }

    @Override
    public float getTranslucency() {
        return translucency;
    }

    @Override
    public void setTranslucency(final float translucency) {
        this.translucency = translucency;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visible  DOCUMENT ME!
     */
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }
}
