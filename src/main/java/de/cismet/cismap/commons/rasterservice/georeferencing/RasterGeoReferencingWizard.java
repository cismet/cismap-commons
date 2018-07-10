/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.rasterservice.georeferencing;

import com.vividsolutions.jts.geom.Coordinate;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.PNodeProvider;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.WorldToScreenTransform;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.capabilitywidget.SelectionAndCapabilities;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.rasterservice.ImageRasterService;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class RasterGeoReferencingWizard implements PropertyChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            RasterGeoReferencingWizard.class);

    private static final DataFlavor CAPABILITIES_DATA_FLAVOR = new DataFlavor(
            DataFlavor.javaJVMLocalObjectMimeType,
            "SelectionAndCapabilities"); // NOI18N

    private static final int MAX_LAYER_COUNT = 3;
    private static final int DEFAULT_ZOOMVIEW_WIDTH = 200;
    private static final int DEFAULT_ZOOMVIEW_HEIGHT = 200;
    private static final int DEFAULT_ZOOMVIEW_FACTOR = 2;

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private enum SelectionMode {

        //~ Enum constants -----------------------------------------------------

        POINT, COORDINATE, NONE
    }

    //~ Instance fields --------------------------------------------------------

    private RetrievalServiceLayer singleLayer = null;

    private final ListenerHandler listenerHandler = new ListenerHandler();

    @Getter private RasterGeoReferencingHandler handler;

    @Getter private Point selectedPoint;

    @Getter private Coordinate selectedCoordinate;

    private SelectionMode selectionMode = SelectionMode.NONE;

    @Getter private int position = 0;

    private final MappingComponent pointZoomMap = new MappingComponent();
    private final MappingComponent coordinateZoomMap = new MappingComponent();

    @Getter @Setter private int zoomViewWidth = DEFAULT_ZOOMVIEW_WIDTH;
    @Getter @Setter private int zoomViewHeight = DEFAULT_ZOOMVIEW_HEIGHT;
    @Getter @Setter private double zoomViewFactor = DEFAULT_ZOOMVIEW_FACTOR;
    @Getter private final PCanvas pointZoomViewCanvas = new PCanvas();
    @Getter private final PCanvas coordinateZoomViewCanvas = new PCanvas();

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private boolean ignoreMapChange;

    @Getter(AccessLevel.PRIVATE)
    private final PropertyChangeListenerHandler propertyChangeListenerHandler = new PropertyChangeListenerHandler();

    @Getter(AccessLevel.PRIVATE)
    private final RetrievalListenerAdapter retrievalListenerAdapter = new RetrievalListenerAdapter();

    @Getter(AccessLevel.PRIVATE)
    private final Collection<PropertyChangeListener> propertyChangeListeners = new ArrayList<>();

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Collection<RetrievalServiceLayer> ignoreLayerList = new ArrayList<>();
    // IgnoreLayerList is needed because the main mapping component is firing layer events when layers are added or
    // removed from ANY mapping model (not only the main) That's why we use this list to ignore these events, if they
    // are concerning layers of one of the zoom mapping components.

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RasterGeoReferencingWizard object.
     */
    private RasterGeoReferencingWizard() {
        pointZoomMap.setAnimationDuration(0);
        pointZoomMap.setResizeEventActivated(false);
        pointZoomMap.setInternalLayerWidgetAvailable(false);
        refreshPointZoomMap();

        coordinateZoomMap.setAnimationDuration(0);
        coordinateZoomMap.setResizeEventActivated(false);
        coordinateZoomMap.setInternalLayerWidgetAvailable(false);
        refreshCoordinateZoomMap();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   propertyChangeListener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean addPropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
        return getPropertyChangeListeners().add(propertyChangeListener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   propertyChangeListener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean removePropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
        return getPropertyChangeListeners().remove(propertyChangeListener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pCanvas  DOCUMENT ME!
     * @param  zoomMap  pLayer DOCUMENT ME!
     */
    private void refreshZoomViewCanvas(final PCanvas pCanvas, final MappingComponent zoomMap) {
        final PCamera camera = new PCamera();
        camera.addLayer(zoomMap.getLayer());
        pCanvas.setCamera(camera);
        zoomMap.getRoot().addChild(camera);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  position  DOCUMENT ME!
     */
    public void updateZoom(final int position) {
        updateZoom(position, SelectionMode.POINT);
        updateZoom(position, SelectionMode.COORDINATE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  position  DOCUMENT ME!
     * @param  mode      DOCUMENT ME!
     */
    private void updateZoom(final int position, final SelectionMode mode) {
        final RasterGeoReferencingHandler handler = getHandler();
        final Coordinate coordinate = SelectionMode.POINT.equals(mode) ? handler.getPointCoordinate(position)
                                                                       : handler.getCoordinate(position);
        final WorldToScreenTransform wtst = SelectionMode.POINT.equals(mode) ? getPointZoomMap().getWtst()
                                                                             : getCoordinateZoomMap().getWtst();
        if (coordinate != null) {
            final Point2D screenPoint = getScreenPoint(coordinate, wtst);
            setZoom(screenPoint, mode);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   coordinate  DOCUMENT ME!
     * @param   wtst        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static Point2D getScreenPoint(final Coordinate coordinate, final WorldToScreenTransform wtst) {
        return new Point2D.Double(wtst.getScreenX(coordinate.x), wtst.getScreenY(coordinate.y));
    }

    /**
     * DOCUMENT ME!
     *
     * @param  coordinate  DOCUMENT ME!
     */
    public void setPointZoom(final Coordinate coordinate) {
        setZoom(coordinate, SelectionMode.POINT);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  coordinate  DOCUMENT ME!
     */
    public void setCoordinateZoom(final Coordinate coordinate) {
        setZoom(coordinate, SelectionMode.COORDINATE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  coordinate  DOCUMENT ME!
     * @param  mode        DOCUMENT ME!
     */
    private void setZoom(final Coordinate coordinate, final SelectionMode mode) {
        final Point2D screenPoint = getScreenPoint(coordinate, getZoomMap(mode).getWtst());
        setZoom(screenPoint, mode);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  screenPoint  DOCUMENT ME!
     * @param  mode         DOCUMENT ME!
     */
    private void setZoom(final Point2D screenPoint, final SelectionMode mode) {
        final double factor = getMainMap().getCamera().getViewScale() * getZoomViewFactor();
        final double width = getZoomViewWidth() / factor;
        final double height = getZoomViewHeight() / factor;

        final Rectangle2D viewBounds = new Rectangle2D.Double(screenPoint.getX()
                        - (width / 2d),
                screenPoint.getY()
                        - (height / 2d),
                width,
                height);
        final PCanvas pCanvas = SelectionMode.POINT.equals(mode) ? getPointZoomViewCanvas()
                                                                 : getCoordinateZoomViewCanvas();
        pCanvas.getCamera().setViewBounds(viewBounds);
        pCanvas.getCamera().setViewScale(factor);
        getPropertyChangeListenerHandler().propertyChange(null);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponent getPointZoomMap() {
        return getZoomMap(SelectionMode.POINT);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponent getCoordinateZoomMap() {
        return getZoomMap(SelectionMode.COORDINATE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mode  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private MappingComponent getZoomMap(final SelectionMode mode) {
        return SelectionMode.POINT.equals(mode) ? pointZoomMap : coordinateZoomMap;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static MappingComponent getMainMap() {
        return CismapBroker.getInstance().getMappingComponent();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   retrievalServiceLayer  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static RetrievalServiceLayer cloneRetrievalServiceLayer(
            final RetrievalServiceLayer retrievalServiceLayer) {
        if (!retrievalServiceLayer.isEnabled()) {
            return null;
        }
        if (!(retrievalServiceLayer instanceof PNodeProvider)) {
            return null;
        }

        final PNodeProvider pnodeProvider = (PNodeProvider)retrievalServiceLayer;
        final PNode pnode = pnodeProvider.getPNode();
        if ((pnode == null) || !pnode.getVisible()) {
            return null;
        }

        if (retrievalServiceLayer instanceof AbstractRetrievalService) {
            final AbstractRetrievalService ars = (AbstractRetrievalService)retrievalServiceLayer;
            final Object clone = ars.cloneWithoutRetrievalListeners();
            if (clone instanceof RetrievalServiceLayer) {
                return (RetrievalServiceLayer)clone;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     */
    public final void refreshPointZoomMap() {
        refreshZoomMap(SelectionMode.POINT);
    }

    /**
     * DOCUMENT ME!
     */
    public final void refreshCoordinateZoomMap() {
        refreshZoomMap(SelectionMode.COORDINATE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  singleLayer  DOCUMENT ME!
     */
    public void setSingleLayer(final RetrievalServiceLayer singleLayer) {
        this.singleLayer = singleLayer;
        refreshZoomMap(SelectionMode.COORDINATE);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public RetrievalServiceLayer getSingleLayer() {
        return singleLayer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   dtde  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean drop(final DropTargetDropEvent dtde) {
        if (dtde.getTransferable().isDataFlavorSupported(CAPABILITIES_DATA_FLAVOR)) {
            try {
                for (int i = 0; i < dtde.getTransferable().getTransferDataFlavors().length; ++i) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("DataFlavour" + i + ": " + dtde.getTransferable().getTransferDataFlavors()[i]); // NOI18N
                    }
                }
                final Object o = dtde.getTransferable().getTransferData(CAPABILITIES_DATA_FLAVOR);
                final List<TreePath> treePaths = new ArrayList<TreePath>();
                dtde.dropComplete(true);
                if (o instanceof SelectionAndCapabilities) {
                    final TreePath[] tpa = ((SelectionAndCapabilities)o).getSelection();
                    for (int i = 0; i < tpa.length; ++i) {
                        treePaths.add(tpa[i]);
                    }

                    final WMSServiceLayer layer;

                    if (((SelectionAndCapabilities)o).getUrl().contains("cismap.dont.touch.ordering=true")) {
                        layer = new WMSServiceLayer(treePaths, false, false);
                    } else {
                        layer = new WMSServiceLayer(treePaths, true, true);
                    }

                    layer.setWmsCapabilities(((SelectionAndCapabilities)o).getCapabilities());
                    layer.setCapabilitiesUrl(((SelectionAndCapabilities)o).getUrl());

                    setSingleLayer(layer);

                    return true;
                }
            } catch (final Exception e) {
                LOG.error(e, e);
            }
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mode  includingNotImageRasterService DOCUMENT ME!
     */
    private void refreshZoomMap(final SelectionMode mode) {
        final MappingComponent origMap = getMainMap();
        if (origMap != null) {
            final MappingComponent mappingComponent = SelectionMode.POINT.equals(mode) ? pointZoomMap
                                                                                       : coordinateZoomMap;

            final PCanvas pCanvas = SelectionMode.POINT.equals(mode) ? getPointZoomViewCanvas()
                                                                     : getCoordinateZoomViewCanvas();
            final MappingComponent zoomMap = SelectionMode.POINT.equals(mode) ? getPointZoomMap()
                                                                              : getCoordinateZoomMap();
            final WorldToScreenTransform wtst = zoomMap.getWtst();
            final Point2D zoomPoint = pCanvas.getCamera().getViewBounds().getCenter2D();
            final Coordinate zoomCoordinate = (wtst != null)
                ? new Coordinate(wtst.getWorldX(zoomPoint.getX()),
                    wtst.getWorldY(zoomPoint.getY())) : null;

            origMap.removePropertyChangeListener(this);
            origMap.addPropertyChangeListener(this);

            final XBoundingBox origBb = (XBoundingBox)origMap.getCurrentBoundingBoxFromCamera();
            final WorldToScreenTransform origWtst = origMap.getWtst();

            final double zoomWidth = getZoomViewWidth() / getZoomViewFactor();
            final double zoomHeight = getZoomViewHeight() / getZoomViewFactor();

            final Point2D topLeft = new Point2D.Double(
                    origWtst.getScreenX(origBb.getX1())
                            - (zoomWidth / 2),
                    origWtst.getScreenY(origBb.getY1())
                            - (zoomHeight / 2));
            final Point2D bottomRight = new Point2D.Double(origWtst.getScreenX(origBb.getX2()) + (zoomWidth / 2),
                    origWtst.getScreenY(origBb.getY2())
                            + (zoomHeight / 2));
            final Rectangle mapBounds = new Rectangle((int)(origMap.getWidth() + zoomWidth),
                    (int)(origMap.getHeight() + zoomHeight));

            final Dimension zoomMapDimension = mapBounds.getSize();
            final XBoundingBox bb = new XBoundingBox(origWtst.getWorldX(topLeft.getX()),
                    origWtst.getWorldY(topLeft.getY()),
                    origWtst.getWorldX(bottomRight.getX()),
                    origWtst.getWorldY(bottomRight.getY()),
                    origBb.getSrs(),
                    origBb.isMetric());

            // new mapping model
            final ActiveLayerModel mappingModel = new ActiveLayerModel();
            mappingModel.setSrs(origMap.getMappingModel().getSrs());
            mappingModel.addHome(bb);

            if (SelectionMode.COORDINATE.equals(mode) && (getSingleLayer() != null)) {
                mappingModel.addLayer(getSingleLayer());
            } else {
                int layerCount = 0;
                // Adding Layers
                for (final Object rasterService : origMap.getMappingModel().getRasterServices().values()) {
                    if (layerCount == MAX_LAYER_COUNT) {
                        break;
                    }
                    if (rasterService instanceof RetrievalServiceLayer) {
                        final RetrievalServiceLayer retrievalServiceLayer = (RetrievalServiceLayer)rasterService;
                        final boolean including = ((retrievalServiceLayer instanceof ImageRasterService)
                                        && SelectionMode.POINT.equals(mode))
                                    || (!(retrievalServiceLayer instanceof ImageRasterService)
                                        && SelectionMode.COORDINATE.equals(mode));
                        if (including) {
                            final RetrievalServiceLayer clone = cloneRetrievalServiceLayer(
                                    retrievalServiceLayer);
                            if (SelectionMode.POINT.equals(mode)) {
                                clone.setTranslucency(1f);
                            }
                            if (clone != null) {
                                clone.addRetrievalListener(getRetrievalListenerAdapter());
                                getIgnoreLayerList().add(clone);
                                mappingModel.addLayer(clone);
                                layerCount++;
                            }
                        }
                    }
                }
            }

            // remove old listener (preventling memory leak)
            // before setting new mapping model
            final ActiveLayerModel oldMappingModel = (ActiveLayerModel)mappingComponent.getMappingModel();
            if (oldMappingModel != null) {
                oldMappingModel.removeMappingModelListener(mappingComponent);
                final Collection oldLayers = oldMappingModel.getMapServices().values();
                for (final MapService mapService : (Collection<MapService>)oldMappingModel.getMapServices().values()) {
                    if (mapService instanceof RetrievalServiceLayer) {
                        final RetrievalServiceLayer retrievalServiceLayer = (RetrievalServiceLayer)mapService;
                        retrievalServiceLayer.removeRetrievalListener(getRetrievalListenerAdapter());
                    }
                }
                oldMappingModel.removeAllLayers();
                getIgnoreLayerList().removeAll(oldLayers);
            }

            if (SelectionMode.POINT.equals(mode)) {
                mappingComponent.getMapServiceLayer().removeAllChildren();
            }

            // lock, resize, replace mappingmodel, unlock
            mappingComponent.lock();
            mappingComponent.setSize(zoomMapDimension);
            mappingComponent.setMappingModel(mappingModel);
            mappingComponent.unlock();

            if (!mappingComponent.getInteractionMode().equals("MUTE")) {
                mappingComponent.setInteractionMode("MUTE");
            }

            refreshZoomViewCanvas(pCanvas, zoomMap);
            if (zoomCoordinate != null) {
                setZoom(zoomCoordinate, mode);
            }
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        // execute refresh only once a second
        // (scrolling & paning in map causes multiple fast propertychange events)
        synchronized (getMainMap()) {
            if (getMainMap().equals(evt.getSource()) && !isIgnoreMapChange()) {
                setIgnoreMapChange(true);
                new SwingWorker<Void, Void>() {

                        @Override
                        protected Void doInBackground() throws Exception {
                            Thread.sleep(1000);
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {
                                if (isPointSelected()) {
                                    refreshPointZoomMap();
                                }
                                if (isCoordinateSelected()) {
                                    refreshCoordinateZoomMap();
                                }
                            } finally {
                                setIgnoreMapChange(false);
                            }
                        }
                    }.execute();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean addListener(final RasterGeoReferencingWizardListener listener) {
        return listenerHandler.add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean removeListener(final RasterGeoReferencingWizardListener listener) {
        return listenerHandler.remove(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isPointSelected() {
        return SelectionMode.POINT.equals(selectionMode);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isCoordinateSelected() {
        return SelectionMode.COORDINATE.equals(selectionMode);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isPointSelectionMode() {
        return SelectionMode.POINT
                    == selectionMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isCoordinateSelectionMode() {
        return SelectionMode.COORDINATE
                    == selectionMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  position  DOCUMENT ME!
     */
    private void setPosition(final int position) {
        this.position = position;
    }

    /**
     * DOCUMENT ME!
     */
    public void forward() {
        final int position = getPosition();
        if (isPointSelected()) {
            selectCoordinate(position);
        } else if (isCoordinateSelected()) {
            if ((position + 1) == getHandler().getNumOfPairs()) {
                getHandler().addPair();
            }
            selectPoint(position + 1);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void backward() {
        if (isPointSelected()) {
            if (getPosition() > 0) {
                selectCoordinate(getPosition() - 1);
            } else {
                selectCoordinate(getHandler().getNumOfPairs() - 1);
            }
        } else if (isCoordinateSelected()) {
            selectPoint(getPosition());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newHandler  DOCUMENT ME!
     */
    public void setHandler(final RasterGeoReferencingHandler newHandler) {
        final RasterGeoReferencingHandler oldHandler = getHandler();

        final boolean handlerChanged = ((newHandler != null) && !newHandler.equals(oldHandler))
                    || ((newHandler == null) && (oldHandler != null));
        if (handlerChanged) {
            if (newHandler != null) {
                newHandler.addListener(listenerHandler);
            }

            this.handler = newHandler;
            listenerHandler.handlerChanged(newHandler);

            if ((handler != null) && (handler.getNumOfPairs() <= 0)) {
                handler.addPair();
            }

            if (oldHandler != null) {
                oldHandler.removeListener(listenerHandler);
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void clearSelection() {
        selectionMode = SelectionMode.NONE;
        selectedPoint = null;
        selectedCoordinate = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mode  DOCUMENT ME!
     */
    private void changeTransparency(final SelectionMode mode) {
        final ImageRasterService service = getHandler().getService();
        final float transparency;
        if (SelectionMode.COORDINATE.equals(mode)) {
            transparency = 0.2f;
        } else {
            transparency = 1f;
        }

        service.setTranslucency(transparency);
        final PNode pi = service.getPNode();
        if (pi != null) {
            pi.setTransparency(transparency);
            pi.repaint();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public void selectPoint(final int position) throws IndexOutOfBoundsException {
        final Point point = getHandler().getPoint(position);
        final boolean changed = (selectedPoint == null)
                    || ((point == null) && (selectedPoint != null))
                    || ((point != null) && !point.equals(selectedPoint));
        if (changed) {
            setPosition(position);
            selectionMode = SelectionMode.POINT;
            selectedPoint = point;
            selectedCoordinate = null;
            listenerHandler.pointSelected(position);
        }

        changeTransparency(SelectionMode.POINT);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   position  DOCUMENT ME!
     *
     * @throws  IndexOutOfBoundsException  DOCUMENT ME!
     */
    public void selectCoordinate(final int position) throws IndexOutOfBoundsException {
        final Coordinate coordinate = getHandler().getCoordinate(position);
        final boolean changed = (selectedCoordinate == null)
                    || ((coordinate == null) && (selectedCoordinate != null))
                    || ((coordinate != null) && !coordinate.equals(selectedCoordinate));
        if (changed) {
            setPosition(position);
            selectionMode = SelectionMode.COORDINATE;
            selectedPoint = null;
            selectedCoordinate = coordinate;
            listenerHandler.coordinateSelected(position);
        }

        changeTransparency(SelectionMode.COORDINATE);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static RasterGeoReferencingWizard getInstance() {
        return LazyInitialiser.INSTANCE;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitialiser {

        //~ Static fields/initializers -----------------------------------------

        private static final RasterGeoReferencingWizard INSTANCE = new RasterGeoReferencingWizard();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitialiser object.
         */
        private LazyInitialiser() {
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ListenerHandler implements RasterGeoReferencingWizardListener {

        //~ Instance fields ----------------------------------------------------

        private final Collection<RasterGeoReferencingWizardListener> listeners = new ArrayList<>();

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   listener  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean add(final RasterGeoReferencingWizardListener listener) {
            return listeners.add(listener);
        }

        /**
         * DOCUMENT ME!
         *
         * @param   listener  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean remove(final RasterGeoReferencingWizardListener listener) {
            synchronized (listeners) {
                return listeners.remove(listener);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private RasterGeoReferencingWizardListener[] getSyncedListeners() {
            synchronized (listeners) {
                return listeners.toArray(new RasterGeoReferencingWizardListener[0]);
            }
        }

        @Override
        public void pointSelected(final int position) {
            for (final RasterGeoReferencingWizardListener listener : getSyncedListeners()) {
                listener.pointSelected(position);
            }
        }

        @Override
        public void coordinateSelected(final int position) {
            for (final RasterGeoReferencingWizardListener listener : getSyncedListeners()) {
                listener.coordinateSelected(position);
            }
        }

        @Override
        public void handlerChanged(final RasterGeoReferencingHandler handler) {
            for (final RasterGeoReferencingWizardListener listener : getSyncedListeners()) {
                listener.handlerChanged(handler);
            }
        }

        @Override
        public void positionAdded(final int position) {
            for (final RasterGeoReferencingWizardListener listener : getSyncedListeners()) {
                listener.positionAdded(position);
            }
            SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (getHandler().getNumOfPairs() == 1) {
                            selectPoint(0);
                        }
                    }
                });
        }

        @Override
        public void positionRemoved(final int position) {
            for (final RasterGeoReferencingWizardListener listener : getSyncedListeners()) {
                listener.positionRemoved(position);
            }
        }

        @Override
        public void positionChanged(final int position) {
            for (final RasterGeoReferencingWizardListener listener : getSyncedListeners()) {
                listener.positionChanged(position);
            }
        }

        @Override
        public void transformationChanged() {
            for (final RasterGeoReferencingWizardListener listener : getSyncedListeners()) {
                listener.transformationChanged();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class PropertyChangeListenerHandler implements PropertyChangeListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            for (final PropertyChangeListener propertyChangeListener : propertyChangeListeners) {
                propertyChangeListener.propertyChange(evt);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class RetrievalListenerAdapter implements RetrievalListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void retrievalStarted(final RetrievalEvent e) {
        }

        @Override
        public void retrievalProgress(final RetrievalEvent e) {
        }

        @Override
        public void retrievalComplete(final RetrievalEvent e) {
            getPropertyChangeListenerHandler().propertyChange(null);
        }

        @Override
        public void retrievalAborted(final RetrievalEvent e) {
        }

        @Override
        public void retrievalError(final RetrievalEvent e) {
        }
    }
}
