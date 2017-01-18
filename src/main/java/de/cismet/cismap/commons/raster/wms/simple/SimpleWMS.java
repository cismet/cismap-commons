/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.raster.wms.simple;

import edu.umd.cs.piccolo.PNode;

import org.apache.commons.httpclient.HttpClient;

import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Element;

import java.util.Vector;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.LayerInfoProvider;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.raster.wms.AbstractWMS;
import de.cismet.cismap.commons.rasterservice.ImageRetrieval;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.rasterservice.RasterMapService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.wms.capabilities.Layer;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class SimpleWMS extends AbstractWMS implements MapService,
    RasterMapService,
    RetrievalServiceLayer,
    LayerInfoProvider {    // implements RasterService,RetrievalListener,ServiceLayer {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private SimpleWmsGetMapUrl gmUrl;
    private ImageRetrieval ir;
    private PNode pNode;
    private String name = "SimpleWMS"; // NOI18N
    private HttpClient preferredClient = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SimpleWMS object.
     *
     * @param  s  DOCUMENT ME!
     */
    public SimpleWMS(final SimpleWMS s) {
        this(s.gmUrl);
        if ((BoundingBox)s.bb != null) {
            bb = (BoundingBox)s.bb.clone();
        }
        enabled = s.enabled;
        height = s.height;
        layerPosition = s.layerPosition;
        name = s.name;
        // The cloned wms and the origin wms should not use the same pnode,
        // because this would lead to problems, if the cloned layer and the origin layer are
        // used in 2 different MappingComponents
// pNode = s.pNode;
        translucency = s.translucency;
        width = s.width;
        ir = new ImageRetrieval(s);
        listeners = new Vector();
        listeners.addAll(s.listeners);
    }

    /**
     * Creates a new instance of SimpleWMS.
     *
     * <p>p@aram gmUrl DOCUMENT ME!</p>
     *
     * @param  gmUrl  DOCUMENT ME!
     */
    public SimpleWMS(final SimpleWmsGetMapUrl gmUrl) {
        this.gmUrl = gmUrl;
    }

    /**
     * Creates a new SimpleWMS object.
     *
     * @param   object  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public SimpleWMS(final Element object) throws Exception {
        final String urlTemplate = object.getTextTrim();
        final SimpleWmsGetMapUrl url = new SimpleWmsGetMapUrl(urlTemplate);
        gmUrl = url;
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
    }

    /**
     * Creates a new SimpleWMS object.
     *
     * @param  gmUrl          DOCUMENT ME!
     * @param  layerPosition  DOCUMENT ME!
     * @param  enabled        DOCUMENT ME!
     * @param  canbeDisabled  DOCUMENT ME!
     * @param  name           DOCUMENT ME!
     */
    public SimpleWMS(final SimpleWmsGetMapUrl gmUrl,
            final int layerPosition,
            final boolean enabled,
            final boolean canbeDisabled,
            final String name) {
        this.gmUrl = gmUrl;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getElement() {
        final Element element = new Element("simpleWms");                             // NOI18N
        element.setAttribute("layerPosition", new Integer(layerPosition).toString()); // NOI18N
        element.setAttribute("skip", "false");                                        // NOI18N
        element.setAttribute("enabled", Boolean.toString(enabled));
        element.setAttribute("visible", Boolean.toString(pNode.getVisible()));        // NOI18N
        element.setAttribute("name", name);                                           // NOI18N
        element.setAttribute("translucency", new Float(translucency).toString());     // NOI18N
        final CDATA data = new CDATA(gmUrl.getUrlTemplate());
        element.addContent(data);
        return element;
    }

    @Override
    public synchronized void retrieve(final boolean forced) {
        if (log.isDebugEnabled()) {
            log.debug("retrieve()"); // NOI18N
        }
        gmUrl.setHeight(height);
        gmUrl.setWidth(width);
        gmUrl.setX1(bb.getX1());
        gmUrl.setY1(bb.getY1());
        gmUrl.setX2(bb.getX2());
        gmUrl.setY2(bb.getY2());
        if ((ir != null) && ir.isAlive() && ir.getUrl().equals(gmUrl.toString()) && !forced) {
            if (log.isDebugEnabled()) {
                // mach nix
                // mehrfachaufruf mit der gleichen url = unsinn
                log.debug("multiple invocations with the same url = humbug"); // NOI18N
            }
        } else {
            if ((ir != null) && ir.isAlive()) {
                ir.youngerWMSCall();
                ir.interrupt();
                retrievalAborted(new RetrievalEvent());
            }

            ir = new ImageRetrieval(this);
            ir.setPreferredHttpClient(preferredClient);
            ir.setUrl(gmUrl.toString());
            ir.setPayload(gmUrl.createPayload());

            if (log.isDebugEnabled()) {
                log.debug("ir.start();"); // NOI18N
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
    public SimpleWmsGetMapUrl getGmUrl() {
        return gmUrl;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  gmUrl  DOCUMENT ME!
     */
    public void setGmUrl(final SimpleWmsGetMapUrl gmUrl) {
        this.gmUrl = gmUrl;
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
        return new SimpleWMS(this);
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
        return null;
    }

    @Override
    public String getServerURI() {
        return gmUrl.getUrlTemplate();
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
}
