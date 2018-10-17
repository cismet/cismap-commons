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

import org.apache.commons.httpclient.Header;
import org.apache.log4j.Logger;

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
import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.measuring.MeasuringComponent;
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

    private static final String MAP_TEMPLATE =
        "<rasterfari:url>?REQUEST=GetMap&SERVICE=WMS&SRS=EPSG:25832&BBOX=<cismap:boundingBox>&WIDTH=<cismap:width>&HEIGHT=<cismap:height>&LAYERS=<rasterfari:document>";
    private static final String DOWNLOAD_TEMPLATE =
        "<rasterfari:url>?REQUEST=GetMap&SERVICE=WMS&customDocumentInfo=download&LAYERS=<rasterfari:document>";

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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHome;
    private javax.swing.JList lstPages;
    private de.cismet.cismap.commons.gui.measuring.MeasuringComponent measuringComponent;
    private javax.swing.JScrollPane scpPages;
    private javax.swing.JToggleButton togPan;
    private javax.swing.JToggleButton togZoom;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

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
        this.rasterfariUrl = rasterfariUrl;
        this.listener = listener;
        this.connectionContext = connectionContext;

        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

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
    public MeasuringComponent getMeasuringComponent() {
        return measuringComponent;
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
    public void dispose() {
        measuringComponent.dispose();
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
        measuringComponent = new MeasuringComponent(INITIAL_BOUNDINGBOX, CRS);

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

        final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 300, Short.MAX_VALUE));
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void lstPagesValueChanged(final javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstPagesValueChanged
        if (!evt.getValueIsAdjusting()) {
            final Object page = lstPages.getSelectedValue();

            if (page instanceof Integer) {
                loadPage(((Integer)page) - 1);
            }
        }
    }//GEN-LAST:event_lstPagesValueChanged

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
            final MappingModel mm = measuringComponent.getMap().getMappingModel();
            final String template = MAP_TEMPLATE.replace("<rasterfari:url>", rasterfariUrl)
                        .replace(
                            "<rasterfari:document>",
                            currentDocument
                            + ((lstPages.getModel().getSize() > 1) ? URLEncoder.encode(
                                    "["
                                    + currentPage
                                    + "]",
                                    "UTF-8") : ""));
//            mm.addLayer(new SimpleWMS(new SimpleWmsGetMapUrl(template), 0, true, false, "prefetching_Lageplan"));
            mm.addLayer(new SimpleWMS(new SimpleWmsGetMapUrl(template), 0, true, false, "Lageplan"));

            togPan.setSelected(true);
            measuringComponent.gotoInitialBoundingBox();
        } catch (final Exception ex) {
            LOG.error(ex, ex);
        } finally {
            listener.showMeasurePanel();
        }
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
    private void togPanActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togPanActionPerformed
        measuringComponent.actionPan();
    }//GEN-LAST:event_togPanActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void togZoomActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_togZoomActionPerformed
        measuringComponent.actionZoom();
    }//GEN-LAST:event_togZoomActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnHomeActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHomeActionPerformed
        measuringComponent.gotoInitialBoundingBox();
    }//GEN-LAST:event_btnHomeActionPerformed

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
                                .replace("<rasterfari:document>", currentDocument);
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
        measuringComponent.removeAllFeatures();
        measuringComponent.reset();
        final MappingModel mm = measuringComponent.getMap().getMappingModel();
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
