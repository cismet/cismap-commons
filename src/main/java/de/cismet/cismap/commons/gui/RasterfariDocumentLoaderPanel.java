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
package de.cismet.cismap.commons.gui;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.AffineTransformation;

import org.apache.commons.httpclient.Header;
import org.apache.log4j.Logger;

import java.awt.Cursor;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StringReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JToggleButton;
import javax.swing.ListModel;
import javax.swing.SwingWorker;

import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MessenGeometryListener;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;

import de.cismet.commons.security.AccessHandler;
import de.cismet.commons.security.handler.SimpleHttpAccessHandler;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class RasterfariDocumentLoaderPanel extends javax.swing.JPanel implements ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final String MAP_TEMPLATE = "<rasterfari:url>"
                + "?REQUEST=GetMap"
                + "&SERVICE=WMS"
                + "&SRS=EPSG:25832"
                + "&BBOX=<cismap:boundingBox>"
                + "&WIDTH=<cismap:width>"
                + "&HEIGHT=<cismap:height>"
                + "&LAYERS=<rasterfari:document>"
                + "&CUSTOMSCALE=<rasterfari:scale>"
                + "&CUSTOMOFFSETX=<rasterfari:offsetX>"
                + "&CUSTOMOFFSETY=<rasterfari:offsetY>";
    private static final String DOWNLOAD_TEMPLATE =
        "<rasterfari:url>?REQUEST=GetMap&SERVICE=WMS&customDocumentInfo=download&LAYERS=<rasterfari:document>";
    private static final String MY_MESSEN_MODE = "MY_MESSEN_MODE";

    private static final Logger LOG = Logger.getLogger(RasterfariDocumentLoaderPanel.class);
    private static final String SRS = "EPSG:25832";
    private static final Crs CRS = new Crs(SRS, SRS, SRS, true, true);
    private static final XBoundingBox INITIAL_BOUNDINGBOX = new XBoundingBox(-0.5d, -0.5d, 0.5d, 0.5d, SRS, true);
    private static final int NO_SELECTION = -1;
    private static final ListModel MODEL_LOAD = new DefaultListModel() {

            {
                add(
                    0,
                    org.openide.util.NbBundle.getMessage(
                        RasterfariDocumentLoaderPanel.class,
                        "RasterfariDocumentLoaderPanel.lstPages.loading"));
            }
        };

    private static final ListModel FEHLER_MODEL = new DefaultListModel() {

            {
                add(
                    0,
                    org.openide.util.NbBundle.getMessage(
                        RasterfariDocumentLoaderPanel.class,
                        "RasterfariDocumentLoaderPanel.lstPages.loadingError"));
            }
        };

    //~ Instance fields --------------------------------------------------------

    private final String rasterfariUrl;
    private String currentDocument = "";
    private int currentPage = NO_SELECTION;
    private final ConnectionContext connectionContext;
    private final Listener listener;
    private double scale = 1;
    private double offsetX = 0;
    private double offsetY = 0;
    private final Crs crs;
    private Geometry mainDocumentGeometry;
    private final MessenGeometryListener mapListener;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHome;
    private javax.swing.JList lstPages;
    private de.cismet.cismap.commons.gui.MappingComponent map;
    private javax.swing.JPanel panCenter;
    private javax.swing.JScrollPane scpPages;
    private javax.swing.JToggleButton togPan;
    private javax.swing.JToggleButton togZoom;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RasterfariDocumentLoaderPanel object.
     */
    public RasterfariDocumentLoaderPanel() {
        this(null, null, INITIAL_BOUNDINGBOX, CRS, ConnectionContext.createDeprecated());
    }

    /**
     * Creates a new PictureLoaderPanel object.
     *
     * @param  rasterfariUrl      DOCUMENT ME!
     * @param  listener           DOCUMENT ME!
     * @param  connectionContext  DOCUMENT ME!
     */
    public RasterfariDocumentLoaderPanel(final String rasterfariUrl,
            final Listener listener,
            final ConnectionContext connectionContext) {
        this(rasterfariUrl, listener, INITIAL_BOUNDINGBOX, CRS, connectionContext);
    }

    /**
     * Creates a new RasterfariDocumentLoaderPanel object.
     *
     * @param  rasterfariUrl       DOCUMENT ME!
     * @param  listener            DOCUMENT ME!
     * @param  initialBoundingBox  DOCUMENT ME!
     * @param  crs                 DOCUMENT ME!
     * @param  connectionContext   DOCUMENT ME!
     */
    public RasterfariDocumentLoaderPanel(final String rasterfariUrl,
            final Listener listener,
            final XBoundingBox initialBoundingBox,
            final Crs crs,
            final ConnectionContext connectionContext) {
        this.rasterfariUrl = rasterfariUrl;
        this.listener = listener;
        this.crs = crs;
        this.connectionContext = connectionContext;

        initComponents();

        this.mapListener = new MessenGeometryListener(map);
        this.mainDocumentGeometry = null;
        final ActiveLayerModel mappingModel = new ActiveLayerModel();
        mappingModel.addHome(initialBoundingBox);
        mappingModel.setSrs(crs);
        mappingModel.setDefaultHomeSrs(crs);
        map.setAnimationDuration(0);
        map.setReadOnly(false);
        map.setMappingModel(mappingModel);
        // initial positioning of the map
        map.gotoInitialBoundingBox();
        // interaction mode
        map.setInteractionMode(MappingComponent.PAN);
        map.addInputListener(MY_MESSEN_MODE, mapListener);
        map.unlock();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MessenGeometryListener getMessenInputListener() {
        return mapListener;
    }

    /**
     * DOCUMENT ME!
     */
    public void dispose() {
        // TODO:
        // this is a quick fix for the memory leak that some mapping components can not be garbage collected
        panCenter.remove(map);
        map.removeInputEventListener(mapListener);
        map.dispose();
        map = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JToggleButton getTogPan() {
        return togPan;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JToggleButton getTogZoom() {
        return togZoom;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JButton getBtnHome() {
        return btnHome;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * DOCUMENT ME!
     */
    public void actionOverview() {
        showFullDocument();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry getMainDocumentGeometry() {
        return mainDocumentGeometry;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mainDocumentGeometry  DOCUMENT ME!
     */
    public void setMainDocumentGeometry(final Geometry mainDocumentGeometry) {
        this.mainDocumentGeometry = mainDocumentGeometry;
    }

    /**
     * DOCUMENT ME!
     */
    public void actionMeasurePolygon() {
        mapListener.setMode(MessenGeometryListener.POLYGON);
        map.setInteractionMode(MY_MESSEN_MODE);
        map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * DOCUMENT ME!
     */
    public void actionMeasureLine() {
        mapListener.setMode(MessenGeometryListener.LINESTRING);
        map.setInteractionMode(MY_MESSEN_MODE);
        map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * DOCUMENT ME!
     */
    public void actionPan() {
        map.setInteractionMode(MappingComponent.PAN);
    }

    /**
     * DOCUMENT ME!
     */
    public void actionZoom() {
        map.setInteractionMode(MappingComponent.ZOOM);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   realDistance  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double calculateScaleFactor(final double realDistance) {
        for (final Feature f : map.getFeatureCollection().getAllFeatures()) {
            // Danger: Messlinie finden...unter der Annahme dass es nur ein PNF gibt!
            if (f instanceof PureNewFeature) {
                final double virtualDistance = f.getGeometry().getLength();
                if (virtualDistance != 0) {
                    return realDistance / virtualDistance;
                }
            }
        }
        return 0;
    }

    /**
     * DOCUMENT ME!
     */
    public void removeAllFeatures() {
        if (map != null) {
            final FeatureCollection fc = map.getFeatureCollection();
            if (fc instanceof DefaultFeatureCollection) {
                ((DefaultFeatureCollection)fc).clear();
            } else {
                fc.removeAllFeatures();
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void showFullDocument() {
        final Geometry geometry;
        if (mainDocumentGeometry != null) {
            geometry = mainDocumentGeometry;
        } else {
            geometry = getMap().getInitialBoundingBox()
                        .getGeometry(CrsTransformer.extractSridFromCrs(getMap().getMappingModel().getSrs().getCode()));
        }
        getMap().gotoBoundingBoxWithoutHistory(new XBoundingBox(geometry.buffer(scale / 10)));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getScale() {
        return scale;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponent getMap() {
        return map;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureCollection getFeatureCollection() {
        return map.getFeatureCollection();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        scpPages = new javax.swing.JScrollPane();
        lstPages = new javax.swing.JList();
        togPan = new javax.swing.JToggleButton();
        togZoom = new javax.swing.JToggleButton();
        btnHome = new javax.swing.JButton();
        panCenter = new javax.swing.JPanel();
        map = new de.cismet.cismap.commons.gui.MappingComponent();

        scpPages.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scpPages.setMinimumSize(new java.awt.Dimension(31, 75));
        scpPages.setOpaque(false);
        scpPages.setPreferredSize(new java.awt.Dimension(85, 75));

        lstPages.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstPages.setFixedCellWidth(75);
        lstPages.addListSelectionListener(new javax.swing.event.ListSelectionListener() {

                @Override
                public void valueChanged(final javax.swing.event.ListSelectionEvent evt) {
                    lstPagesValueChanged(evt);
                }
            });
        scpPages.setViewportView(lstPages);

        togPan.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/pan.gif"))); // NOI18N
        togPan.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            togPan,
            org.openide.util.NbBundle.getMessage(
                RasterfariDocumentLoaderPanel.class,
                "RasterfariDocumentLoaderPanel.togPan.text"));                             // NOI18N
        togPan.setToolTipText(org.openide.util.NbBundle.getMessage(
                RasterfariDocumentLoaderPanel.class,
                "RasterfariDocumentLoaderPanel.togPan.toolTipText"));                      // NOI18N
        togPan.setFocusPainted(false);
        togPan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        togPan.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    togPanActionPerformed(evt);
                }
            });

        togZoom.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/zoom.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            togZoom,
            org.openide.util.NbBundle.getMessage(
                RasterfariDocumentLoaderPanel.class,
                "RasterfariDocumentLoaderPanel.togZoom.text"));                             // NOI18N
        togZoom.setToolTipText(org.openide.util.NbBundle.getMessage(
                RasterfariDocumentLoaderPanel.class,
                "RasterfariDocumentLoaderPanel.togZoom.toolTipText"));                      // NOI18N
        togZoom.setFocusPainted(false);
        togZoom.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        togZoom.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    togZoomActionPerformed(evt);
                }
            });

        btnHome.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cids/custom/wunda_blau/res/home.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            btnHome,
            org.openide.util.NbBundle.getMessage(
                RasterfariDocumentLoaderPanel.class,
                "RasterfariDocumentLoaderPanel.btnHome.text"));                             // NOI18N
        btnHome.setToolTipText(org.openide.util.NbBundle.getMessage(
                RasterfariDocumentLoaderPanel.class,
                "RasterfariDocumentLoaderPanel.btnHome.toolTipText"));                      // NOI18N
        btnHome.setFocusPainted(false);
        btnHome.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnHome.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnHomeActionPerformed(evt);
                }
            });

        setLayout(new java.awt.BorderLayout());

        panCenter.setOpaque(false);
        panCenter.setLayout(new java.awt.BorderLayout());

        map.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panCenter.add(map, java.awt.BorderLayout.CENTER);

        add(panCenter, java.awt.BorderLayout.CENTER);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void lstPagesValueChanged(final javax.swing.event.ListSelectionEvent evt) { //GEN-FIRST:event_lstPagesValueChanged
        if (!evt.getValueIsAdjusting()) {
            final Object page = lstPages.getSelectedValue();

            if (page instanceof Integer) {
                loadPage(((Integer)page) - 1);
            }
        }
    } //GEN-LAST:event_lstPagesValueChanged

    /**
     * DOCUMENT ME!
     *
     * @param  page  DOCUMENT ME!
     */
    private void loadPage(final int page) {
        currentPage = page;
        reset();
        listener.showMeasureIsLoading();
        try {
            final MappingModel mm = map.getMappingModel();
            final String template = MAP_TEMPLATE.replace("<rasterfari:url>", rasterfariUrl)
                        .replace("<rasterfari:scale>", Double.toString(scale))
                        .replace("<rasterfari:offsetX>", Double.toString(offsetX))
                        .replace("<rasterfari:offsetY>", Double.toString(offsetY))
                        .replace(
                            "<rasterfari:document>",
                            currentDocument
                            + ((lstPages.getModel().getSize() > 1) ? URLEncoder.encode(
                                    "["
                                    + currentPage
                                    + "]",
                                    "UTF-8") : ""));
            mm.addLayer(new SimpleWMS(new SimpleWmsGetMapUrl(template), 1, true, false, "prefetching_Lageplan"));
            mm.addLayer(new SimpleWMS(new SimpleWmsGetMapUrl(template), 0, true, false, "Lageplan"));

            togPan.setSelected(true);
            showFullDocument();
        } catch (final Exception ex) {
            LOG.error(ex, ex);
        } finally {
            listener.showMeasurePanel();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  distance  DOCUMENT ME!
     */
    public void calibrate(final double distance) {
        final double scalefactor = calculateScaleFactor(distance);
        doScale(scalefactor);
        loadPage(currentPage);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  scalefactor  DOCUMENT ME!
     */
    private void doScale(final double scalefactor) {
        setScale(scale * scalefactor);
        final Geometry documentGeom = (Geometry)getMainDocumentGeometry().clone();
        final Point oldCentroid = documentGeom.getCentroid();
        documentGeom.apply(AffineTransformation.scaleInstance(scalefactor, scalefactor));
        final Point newCentroid = documentGeom.getCentroid();
        documentGeom.apply(AffineTransformation.translationInstance(
                oldCentroid.getX()
                        - newCentroid.getX(),
                oldCentroid.getY()
                        - newCentroid.getY()));
        setMainDocumentGeometry(documentGeom);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  scale  DOCUMENT ME!
     */
    public void setScale(final double scale) {
        this.scale = scale;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  offsetX  DOCUMENT ME!
     */
    public void setOffsetX(final double offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  offsetY  DOCUMENT ME!
     */
    public void setOffsetY(final double offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  scale    DOCUMENT ME!
     * @param  offsetX  DOCUMENT ME!
     * @param  offsetY  DOCUMENT ME!
     */
    public void setScaleAndOffsets(final double scale, final double offsetX, final double offsetY) {
        setScale(scale);
        setOffsetX(offsetX);
        setOffsetY(offsetY);
    }

    /**
     * DOCUMENT ME!
     */
    public void reload() {
        loadPage(currentPage);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public URL getDocumentUrl() {
        return getDocumentUrl(currentDocument);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   document  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public URL getDocumentUrl(final String document) {
        try {
            return new URL(DOWNLOAD_TEMPLATE.replace("<rasterfari:url>", rasterfariUrl).replace(
                        "<rasterfari:document>",
                        currentDocument));
        } catch (final MalformedURLException ex) {
            LOG.error(ex, ex);
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void togPanActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togPanActionPerformed
        actionPan();
    }                                                                          //GEN-LAST:event_togPanActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void togZoomActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_togZoomActionPerformed
        actionZoom();
    }                                                                           //GEN-LAST:event_togZoomActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnHomeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnHomeActionPerformed
        showFullDocument();
    }                                                                           //GEN-LAST:event_btnHomeActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JList getLstPages() {
        return lstPages;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCurrentDocument() {
        return currentDocument;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  document  DOCUMENT ME!
     */
    public void setDocument(final String document) {
        this.currentDocument = document;
        lstPages.setModel(MODEL_LOAD);
        listener.showMeasureIsLoading();

        new SwingWorker<Integer, Void>() {

                @Override
                protected Integer doInBackground() throws Exception {
                    // We just want the header.
                    final String template = MAP_TEMPLATE.replace("<rasterfari:url>", rasterfariUrl)
                                .replace("<cismap:boundingBox>", "-0.5,-0.5,0.5,0.5")
                                .replace("<cismap:width>", "10")
                                .replace("<cismap:height>", "10")
                                .replace("<rasterfari:document>", currentDocument)
                                .replace("<rasterfari:scale>", Double.toString(1d))
                                .replace("<rasterfari:offsetX>", Double.toString(0d))
                                .replace("<rasterfari:offsetY>", Double.toString(0d));
                    final URL url = new URL(template);
                    final InputStream is =
                        new SimpleHttpAccessHandler().doRequest(
                            url,
                            new StringReader(""),
                            AccessHandler.ACCESS_METHODS.HEAD_REQUEST);
                    try(final ObjectInputStream ois = new ObjectInputStream(is)) {
                        final Object object = ois.readObject();
                        final Header[] headers = (Header[])object;
                        for (final Header header : headers) {
                            if ("X-Rasterfari-numOfPages".equals(header.getName())) {
                                return Integer.parseInt(header.getValue());
                            }
                        }
                    }
                    return -1;
                }

                @Override
                protected void done() {
                    try {
                        final Integer pages = get();
                        final DefaultListModel dlm = new DefaultListModel();
                        for (int i = 0; i < pages; i++) {
                            dlm.addElement(i + 1);
                        }
                        lstPages.setModel(dlm);
                        lstPages.setEnabled(true);
                        lstPages.setSelectedIndex(0);
                    } catch (final Exception ex) {
                        lstPages.setModel(FEHLER_MODEL);
                        LOG.error(ex, ex);
                        setCurrentPageNull();
                    }
                }
            }.execute();
    }

    /**
     * DOCUMENT ME!
     */
    public void setCurrentPageNull() {
        currentPage = NO_SELECTION;
        lstPages.setEnabled(false);
        reset();
    }

    /**
     * DOCUMENT ME!
     */
    public void reset() {
        removeAllFeatures();
        getMap().setInteractionMode(MappingComponent.PAN);
        final MappingModel mm = getMap().getMappingModel();
        for (final RetrievalServiceLayer rsl : (Collection<RetrievalServiceLayer>)mm.getRasterServices().values()) {
            mm.removeLayer(rsl);
        }
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    //~ Inner Interfaces -------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public interface Listener {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        void showMeasureIsLoading();
        /**
         * DOCUMENT ME!
         */
        void showMeasurePanel();
    }
}
