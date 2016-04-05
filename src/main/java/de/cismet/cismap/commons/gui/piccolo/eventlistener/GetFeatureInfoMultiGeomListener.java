/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.event.PNotificationCenter;

import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.spatialschema.JTSAdapter;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import java.net.URL;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.AbstractNewFeature;
import de.cismet.cismap.commons.features.CommonFeatureAction;
import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.WMSFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.DefaultLayerProperties;
import de.cismet.cismap.commons.featureservice.GeometryHeuristics;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.featureinfopanel.WMSGetFeatureInfoDescription;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.GetFeatureInfoListener;
import de.cismet.cismap.commons.interaction.events.GetFeatureInfoEvent;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.tools.PFeatureTools;
import de.cismet.cismap.commons.wms.capabilities.Parameter;

import de.cismet.commons.concurrency.CismetExecutors;

import de.cismet.security.WebAccessManager;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GetFeatureInfoMultiGeomListener extends CreateGeometryListener {

    //~ Static fields/initializers ---------------------------------------------

    public static final String GET_FEATURE_INFO_MULTI_GEOM_NOTIFICATION = "GET_FEATURE_INFO_MULTI_GEOM_NOTIFICATION"; // NOI18N
    private static final String WMS_GML_FORMAT = "application/vnd.ogc.gml";
    private static final String ENCODING_ATTR = "encoding=\"";

    //~ Instance fields --------------------------------------------------------

    Vector<PFeature> pfVector = new Vector<PFeature>();
    ArrayList<? extends CommonFeatureAction> commonFeatureActions = null;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private int clickCount = 0;
    private boolean selectionInProgress = false;
    private List<GetFeatureInfoListener> listener = new ArrayList<GetFeatureInfoListener>();
    private ImageIcon pointIcon = new javax.swing.ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/res/linRefPoint.png"));

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionListener object.
     */
    public GetFeatureInfoMultiGeomListener() {
        final Lookup.Result<CommonFeatureAction> result = Lookup.getDefault().lookupResult(CommonFeatureAction.class);
        commonFeatureActions = new ArrayList<CommonFeatureAction>(result.allInstances());
        Collections.sort(commonFeatureActions, new Comparator<CommonFeatureAction>() {

                @Override
                public int compare(final CommonFeatureAction o1, final CommonFeatureAction o2) {
                    return Integer.valueOf(o1.getSorter()).compareTo(Integer.valueOf(o2.getSorter()));
                }
            });
        setGeometryFeatureClass(PureNewFeature.class);
        setMode(RECTANGLE);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseMoved(final PInputEvent pInputEvent) {
        setMappingComponent(pInputEvent);
        super.mouseMoved(pInputEvent);
    }

    @Override
    public void mousePressed(final PInputEvent pInputEvent) {
        setMappingComponent(pInputEvent);
        super.mousePressed(pInputEvent);
    }

    /**
     * Selektiere einen PNode.
     *
     * @param  pInputEvent  DOCUMENT ME!
     */
    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        setMappingComponent(pInputEvent);
        super.mouseClicked(pInputEvent);

        if (mode.equals(RECTANGLE) || mode.equals(ELLIPSE)) {
            selectionInProgress = true;

            try {
                if (log.isDebugEnabled()) {
                    log.debug("mouseClicked():" + pInputEvent.getPickedNode()); // NOI18N
                }
                clickCount = pInputEvent.getClickCount();
                if (pInputEvent.getComponent() instanceof MappingComponent) {
                    mappingComponent = (MappingComponent)pInputEvent.getComponent();
                }

                final int currentSrid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs()
                                .getCode());
                final PureNewFeature feature = new PureNewFeature(createPointFromInput(pInputEvent));
                feature.setGeometryType(AbstractNewFeature.geomTypes.POINT);
                feature.getGeometry().setSRID(currentSrid);

                // show the point on the map
                final DefaultStyledFeature styledFeature = new DefaultStyledFeature();
                styledFeature.setGeometry(createPointFromInput(pInputEvent));
                styledFeature.getGeometry().setSRID(currentSrid);
                final FeatureAnnotationSymbol fas = new FeatureAnnotationSymbol(pointIcon.getImage());
                fas.setSweetSpotX(0.5);
                fas.setSweetSpotY(0.5);
                styledFeature.setPointAnnotationSymbol(fas);
                mappingComponent.highlightFeature(styledFeature, 1500);

                finishingEvent = pInputEvent;
                finishGeometry(feature);
            } finally {
                selectionInProgress = false;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  featuresFromServicesSelectable DOCUMENT ME!
     */
    public void addGetFeatureInfoListener(final GetFeatureInfoListener l) {
        listener.add(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  featuresFromServicesSelectable DOCUMENT ME!
     */
    public void removeGetFeatureInfoListener(final GetFeatureInfoListener l) {
        listener.remove(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   event  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Point createPointFromInput(final PInputEvent event) {
        final Point2D pos = event.getPosition();
        final WorldToScreenTransform wtst = getMappingComponent().getWtst();
        final Coordinate coord = new Coordinate(wtst.getSourceX(pos.getX()), wtst.getSourceY(pos.getY()));
        final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                CrsTransformer.getCurrentSrid());

        return gf.createPoint(coord);
    }

    @Override
    public void mouseDragged(final PInputEvent e) {
        setMappingComponent(e);
        super.mouseDragged(e); // To change body of generated methods, choose Tools | Templates.

        clickCount = e.getClickCount();
    }

    /**
     * Wird gefeuert, wenn die Maustaste nach dem Ziehen des Markiervierecks losgelassen wird.
     *
     * @param  event  das Mouseevent (als PInputEvent)
     */
    @Override
    public void mouseReleased(final PInputEvent event) {
        setMappingComponent(event);
        super.mouseReleased(event);
    }

    @Override
    protected Color getFillingColor() {
        return new Color(20, 20, 20, 20);
    }

    @Override
    protected void finishGeometry(final AbstractNewFeature feature) {
        super.finishGeometry(feature);
        selectionInProgress = true;
        mappingComponent.getHandleLayer().removeAllChildren();
        final Geometry geom;

        if (feature.getGeometryType().equals(AbstractNewFeature.geomTypes.POINT)) {
            // 0.000001 this is even in geographic crs less than 1m
            geom = feature.getGeometry().buffer(0.000001);
        } else {
            geom = feature.getGeometry();
        }

        final WaitingDialogThread<List<Feature>> t = new WaitingDialogThread<List<Feature>>(
                StaticSwingTools.getParentFrame(
                    mappingComponent),
                true,
                NbBundle.getMessage(
                    GetFeatureInfoMultiGeomListener.class,
                    "GetFeatureInfoMultiGeomListener.finishGeometry.WaitingDialogThread"),
                null,
                200,
                true) {

                @Override
                protected List<Feature> doInBackground() throws Exception {
                    final List<Feature> toBeSelected = Collections.synchronizedList(new ArrayList<Feature>());

                    if ((geom != null)) {
                        if (log.isDebugEnabled()) {
                            // Hole alle PFeatures die das Markierviereck schneiden
                            // und Hinzuf\u00FCgen dieser PFeatures zur Selektion
                            log.debug("Markiergeometrie = " + geom.toText()); // NOI18N
                        }

                        final TreeMap<Integer, MapService> serviceTree =
                            ((ActiveLayerModel)mappingComponent.getMappingModel()).getMapServices();

                        final ExecutorService executor = CismetExecutors.newFixedThreadPool(8);
                        Map<MapService, List<Feature>> featureMap = new HashMap<MapService, List<Feature>>(
                                serviceTree.size());
                        featureMap = Collections.synchronizedMap(featureMap);

                        for (final Integer key : serviceTree.keySet()) {
                            final MapService service = serviceTree.get(key);

                            final FeatureRetriever fr = new FeatureRetriever(featureMap, service, geom, feature);
                            executor.submit(fr);
                        }

                        executor.shutdown();
                        executor.awaitTermination(1, TimeUnit.HOURS);

                        for (final MapService service : featureMap.keySet()) {
                            toBeSelected.addAll(featureMap.get(service));
                        }
                    }

                    return toBeSelected;
                }

                @Override
                protected void done() {
                    try {
                        final List<Feature> toBeSelected = get();
                        final GetFeatureInfoEvent evt = new GetFeatureInfoEvent(mappingComponent, geom);
                        evt.setFeatures(toBeSelected);
                        fireGetFeatureInfoEvent(evt);

                        postSelectionChanged();
                    } catch (Exception e) {
                        log.error("Error while trying to receiving features.", e);
                    } finally {
                        selectionInProgress = false;
                        mappingComponent.getFeatureCollection().removeFeature(feature);
                    }
                }
            };

        t.start();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   url              DOCUMENT ME!
     * @param   wmsServiceLayer  DOCUMENT ME!
     * @param   clickPoint       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<Feature> getWMSFeatures(final String url,
            final WMSServiceLayer wmsServiceLayer,
            final Geometry clickPoint) throws Exception {
        final InputStream respIs = WebAccessManager.getInstance().doRequest(new URL(url));
        final GMLFeatureCollectionDocument featureCollectionDocument = new GMLFeatureCollectionDocument();
        final FeatureCollection featureCollection;

        String res = readInputStream(respIs, null);
        String encodingString = null;

        if (res.contains(ENCODING_ATTR)) {
            encodingString = res.substring(res.indexOf(ENCODING_ATTR) + ENCODING_ATTR.length());

            encodingString = encodingString.substring(0, encodingString.indexOf("\""));
        }

        if (encodingString != null) {
            res = readInputStream(new ByteArrayInputStream(res.getBytes()), encodingString);
        }

        final StringReader re = new StringReader(res);

        featureCollectionDocument.load(re, "http://dummyID");

        if (featureCollectionDocument.getFeatureCount() == 0) {
            return null;
        }

        featureCollection = featureCollectionDocument.parse();

        if ((featureCollection.size() == 1) && (featureCollection.getFeature(0).getName() != null)
                    && featureCollection.getFeature(0).getName().getLocalName().equals("ExceptionText")) {
            try {
                final String errorMessage = featureCollectionDocument.getRootElement()
                            .getFirstChild()
                            .getFirstChild()
                            .getTextContent();

                throw new Exception(errorMessage);
            } catch (NullPointerException e) {
                throw new Exception("The wfs replies with an Exception, but the error text cannot be extracted.");
            }
        }

        if (featureCollection.size() > 0) {
            return processFeatureCollection(
                    featureCollection.toArray(),
                    true,
                    wmsServiceLayer,
                    clickPoint);
        } else {
            return new ArrayList<Feature>();
        }
    }

    /**
     * Reads the given input stream.
     *
     * @param   respIs   the inputstream to read
     * @param   charset  the charset to be use. The default charset will be used, if it is null
     *
     * @return  The inputstream as string
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private String readInputStream(final InputStream respIs, final String charset) throws IOException {
        final InputStreamReader reader;

        if (charset != null) {
            reader = new InputStreamReader(new BufferedInputStream(respIs), charset);
        } else {
            reader = new InputStreamReader(new BufferedInputStream(respIs));
        }
        final StringBuilder res = new StringBuilder();
        String tmp;
        final BufferedReader br = new BufferedReader(reader);

        while ((tmp = br.readLine()) != null) {
            res.append(tmp);
        }

        return res.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   featureCollection    DOCUMENT ME!
     * @param   evaluateExpressions  DOCUMENT ME!
     * @param   layer                DOCUMENT ME!
     * @param   clickPoint           DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected List<Feature> processFeatureCollection(final org.deegree.model.feature.Feature[] featureCollection,
            final boolean evaluateExpressions,
            final WMSServiceLayer layer,
            final Geometry clickPoint) throws Exception {
        int i = 0;
        final int geometryIndex = GeometryHeuristics.findBestGeometryIndex(featureCollection[0]);

        final Vector<Feature> featureVector = new Vector(featureCollection.length);

        for (final org.deegree.model.feature.Feature degreeFeature : featureCollection) {
            final WMSFeature featureServiceFeature = new WMSFeature(layer);
            final int srid = CrsTransformer.getCurrentSrid();
            this.initialiseFeature(
                featureServiceFeature,
                degreeFeature,
                evaluateExpressions,
                i,
                geometryIndex,
                srid,
                clickPoint);
            featureVector.add(featureServiceFeature);
            i++;
        }

        return featureVector;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   featureServiceFeature  DOCUMENT ME!
     * @param   degreeFeature          DOCUMENT ME!
     * @param   evaluateExpressions    DOCUMENT ME!
     * @param   index                  DOCUMENT ME!
     * @param   geometryIndex          DOCUMENT ME!
     * @param   featureSrid            DOCUMENT ME!
     * @param   clickPoint             DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected void initialiseFeature(final WMSFeature featureServiceFeature,
            final org.deegree.model.feature.Feature degreeFeature,
            final boolean evaluateExpressions,
            final int index,
            final int geometryIndex,
            final int featureSrid,
            final Geometry clickPoint) throws Exception {
        // perform standard initilaisation
        featureServiceFeature.setLayerProperties(new DefaultLayerProperties());

        // creating geometry
        if (featureServiceFeature.getGeometry() == null) {
            try {
                featureServiceFeature.setGeometry(JTSAdapter.export(
                        degreeFeature.getGeometryPropertyValues()[geometryIndex]));
            } catch (Exception e) {
                featureServiceFeature.setGeometry(clickPoint);
            }
        }

        if ((featureServiceFeature.getGeometry() != null)) {
            featureServiceFeature.getGeometry().setSRID(featureSrid);
        }

        // adding properties
        if ((featureServiceFeature.getProperties() == null) || featureServiceFeature.getProperties().isEmpty()) {
            // set the properties
            final FeatureProperty[] featureProperties = degreeFeature.getProperties();

            for (final FeatureProperty fp : featureProperties) {
                featureServiceFeature.addProperty(fp.getName().getAsString(), fp.getValue());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    public void fireGetFeatureInfoEvent(final GetFeatureInfoEvent evt) {
        for (final GetFeatureInfoListener l : listener) {
            l.getFeatureInfoRequest(evt);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pInputEvent  DOCUMENT ME!
     */
    private void setMappingComponent(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (getMappingComponent() == null) {
            super.setMappingComponent((MappingComponent)pInputEvent.getComponent());
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void postSelectionChanged() {
        if (log.isDebugEnabled()) {
            log.debug("postSelectionChanged"); // NOI18N
        }
        final PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(GetFeatureInfoMultiGeomListener.GET_FEATURE_INFO_MULTI_GEOM_NOTIFICATION, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getClickCount() {
        return clickCount;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the selectionInProgress
     */
    public boolean isSelectionInProgress() {
        return selectionInProgress;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectionInProgress  the selectionInProgress to set
     */
    public void setSelectionInProgress(final boolean selectionInProgress) {
        this.selectionInProgress = selectionInProgress;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class FeatureRetriever implements Runnable {

        //~ Instance fields ----------------------------------------------------

        private final Map<MapService, List<Feature>> featureMap;
        private final MapService service;
        private final Geometry geom;
        private final AbstractNewFeature feature;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FeatureRetriever object.
         *
         * @param  featureMap  DOCUMENT ME!
         * @param  service     DOCUMENT ME!
         * @param  geom        DOCUMENT ME!
         * @param  feature     DOCUMENT ME!
         */
        public FeatureRetriever(final Map<MapService, List<Feature>> featureMap,
                final MapService service,
                final Geometry geom,
                final AbstractNewFeature feature) {
            this.featureMap = featureMap;
            this.service = service;
            this.geom = geom;
            this.feature = feature;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            List<Feature> featuresFromService = new ArrayList<Feature>();

            if (service instanceof AbstractFeatureService) {
                try {
                    final AbstractFeatureService featureService = (AbstractFeatureService)service;
                    if (!featureService.isInitialized()) {
                        featureService.initAndWait();
                    }
                    featuresFromService = featureService.getFeatureFactory()
                                .createFeatures(featureService.getQuery(),
                                        new XBoundingBox(geom),
                                        null,
                                        0,
                                        Integer.MAX_VALUE,
                                        null);
                } catch (Exception e) {
                    log.error("Error while receiving features", e);
                }
            } else if (feature.getGeometryType().equals(AbstractNewFeature.geomTypes.POINT)
                        && (service instanceof WMSServiceLayer)) {
                final WMSServiceLayer wmsService = (WMSServiceLayer)service;

                if (wmsService.isQueryable()) {
                    final Parameter p = wmsService.getWmsCapabilities()
                                .getRequest()
                                .getFeatureInfoOperation()
                                .getParameter("Format");
                    final boolean gmlResponsePossible = (p != null) && (p.getAllowedValues() != null)
                                && p.getAllowedValues().contains(WMS_GML_FORMAT);
                    for (final WMSLayer layer : (List<WMSLayer>)wmsService.getWMSLayers()) {
                        if (gmlResponsePossible) {
                            try {
                                featuresFromService.addAll(getWMSFeatures(
                                        wmsService.getGetFeatureInfoUrl(
                                            (int)finishingEvent.getCanvasPosition().getX(),
                                            (int)finishingEvent.getCanvasPosition().getY(),
                                            layer,
                                            WMS_GML_FORMAT),
                                        wmsService,
                                        feature.getGeometry()));
                            } catch (Exception e) {
                                log.error("Error while retrieving features from wms", e);
                            }
                        } else {
                            featuresFromService.add(new WMSGetFeatureInfoDescription(
                                    feature.getGeometry(),
                                    finishingEvent,
                                    layer,
                                    wmsService));
                        }
                    }
                }
            }

            featureMap.put(service, featuresFromService);
        }
    }
}
