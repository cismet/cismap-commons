/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * AbstractFeatureInfoDisplay.java
 *
 * Created on 5. April 2006, 15:42
 */
package de.cismet.cismap.commons.gui.featureinfowidget.displays;

import org.apache.log4j.Logger;

import org.openide.util.lookup.ServiceProvider;

import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.LinkListener;

import java.applet.AppletContext;

import java.awt.ComponentOrientation;
import java.awt.EventQueue;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import de.cismet.cismap.commons.gui.featureinfowidget.AbstractFeatureInfoDisplay;
import de.cismet.cismap.commons.gui.featureinfowidget.FeatureInfoDisplay;
import de.cismet.cismap.commons.gui.featureinfowidget.FeatureInfoDisplayKey;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.MapClickedEvent;
import de.cismet.cismap.commons.raster.wms.WMSLayer;

import de.cismet.security.AccessHandler;

import de.cismet.security.AccessHandler.ACCESS_METHODS;

import de.cismet.security.WebAccessManager;

import de.cismet.security.handler.WSSAccessHandler;

import de.cismet.tools.gui.xhtmlrenderer.WebAccessManagerUserAgent;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = FeatureInfoDisplay.class)
public class OGCWMSGetFeatureInfoRequestHtmlFSDisplay extends AbstractFeatureInfoDisplay<WMSLayer> {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(OGCWMSGetFeatureInfoRequestHtmlFSDisplay.class);

    //~ Instance fields --------------------------------------------------------

    private final Icon icoProgress = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/featureinfowidget/res/progress.png"));   // NOI18N
    private final Icon icoProgress64 = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/featureinfowidget/res/progress64.png")); // NOI18N
    private final Icon icoInfo = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/featureinfowidget/res/info.png"));       // NOI18N

    private WMSLayer wmsLayer = null;
    private AppletContext appletContext;
    private JTabbedPane tabbedparent;
    private String currentUrl;
    private SwingWorker currentWorker;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdOpenExternal;
    private org.xhtmlrenderer.simple.FSScrollPane fSScrollPane;
    private javax.swing.JToolBar tbRight;
    private org.xhtmlrenderer.simple.XHTMLPanel xhtmlPanel;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form AbstractFeatureInfoDisplay.
     */
    public OGCWMSGetFeatureInfoRequestHtmlFSDisplay() {
        super(new FeatureInfoDisplayKey(
                WMSLayer.class,
                "",
                ""));

        System.setProperty("xr.load.xml-reader", "org.ccil.cowan.tagsoup.Parser");
        System.setProperty("xr.load.string-interning", "true");
        System.setProperty("xr.use.listeners", "true");

        initComponents();

        for (final Object o : xhtmlPanel.getMouseTrackingListeners()) {
            if (o instanceof LinkListener) {
                xhtmlPanel.removeMouseTrackingListener((LinkListener)o);
                xhtmlPanel.addMouseTrackingListener(new ShowLoadingStateLinkListener());
            }
        }

        xhtmlPanel.getSharedContext().setUserAgentCallback(new WebAccessManagerUserAgent());
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void init(final WMSLayer layer, final JTabbedPane parentTabbedPane) {
        this.wmsLayer = layer;
        this.tabbedparent = parentTabbedPane;
    }

    @Override
    public void showFeatureInfo(final MapClickedEvent mapClickedEvent) {
        showContent((int)mapClickedEvent.getX(), (int)mapClickedEvent.getY());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  x  DOCUMENT ME!
     * @param  y  DOCUMENT ME!
     */
    private void showContent(final int x, final int y) {
        final String url = wmsLayer.getParentServiceLayer().getGetFeatureInfoUrl(x, y, wmsLayer);
        currentUrl = url;

        if (LOG.isDebugEnabled()) {
            LOG.debug("showContent(" + x + ", " + y + ") called. Corresponding url: '" + url + "'."); // NOI18N
        }

        if ((currentWorker != null) && !currentWorker.isCancelled()) {
            currentWorker.cancel(true);
        }

        currentWorker = new FeatureInfoRetriever(url);
        CismapBroker.getInstance().execute(currentWorker);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  url  DOCUMENT ME!
     */
    private void openUrlInExternalBrowser(final String url) {
        if (appletContext == null) {
            try {
                de.cismet.tools.BrowserLauncher.openURL(url);
            } catch (Exception ex) {
                LOG.error("Error while opening '" + url + "' in external browser.", ex);

                try {
                    de.cismet.tools.BrowserLauncher.openURL("file://" + url);                  // NOI18N
                } catch (final Exception ex2) {
                    LOG.error("Couldn't open 'file://" + url + "' in external browser.", ex2); // NOI18N
                }
            }
        } else {
            try {
                appletContext.showDocument(new java.net.URL(url), "cismetBrowser");            // NOI18N
            } catch (MalformedURLException ex) {
                LOG.error("Couldn't create an URL object from '" + url + "'.", ex);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        tbRight = new javax.swing.JToolBar();
        tbRight.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        cmdOpenExternal = new javax.swing.JButton();
        fSScrollPane = new org.xhtmlrenderer.simple.FSScrollPane();
        xhtmlPanel = new org.xhtmlrenderer.simple.XHTMLPanel();

        setLayout(new java.awt.BorderLayout());

        tbRight.setFloatable(false);

        cmdOpenExternal.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/featureinfowidget/res/extWindow.png"))); // NOI18N
        cmdOpenExternal.setText(org.openide.util.NbBundle.getMessage(
                OGCWMSGetFeatureInfoRequestHtmlFSDisplay.class,
                "OGCWMSGetFeatureInfoRequestHtmlDisplay.cmdOpenExternal.text"));                               // NOI18N
        cmdOpenExternal.setToolTipText(org.openide.util.NbBundle.getMessage(
                OGCWMSGetFeatureInfoRequestHtmlFSDisplay.class,
                "OGCWMSGetFeatureInfoRequestHtmlDisplay.cmdOpenExternal.toolTipText"));                        // NOI18N
        cmdOpenExternal.setBorderPainted(false);
        cmdOpenExternal.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdOpenExternalActionPerformed(evt);
                }
            });
        tbRight.add(cmdOpenExternal);

        add(tbRight, java.awt.BorderLayout.NORTH);

        fSScrollPane.setViewportView(xhtmlPanel);

        add(fSScrollPane, java.awt.BorderLayout.CENTER);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdOpenExternalActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdOpenExternalActionPerformed

        if (currentUrl != null) {
            try {
                // ToDo muss in WebAccessManger
                final AccessHandler handler = WebAccessManager.getInstance().getHandlerForURL(new URL(currentUrl));
                if (handler != null) {
                    if (handler instanceof WSSAccessHandler) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Handler is WSS handler. Create WSS get request."); // NOI18N
                        }

                        final String wssRequest = ((WSSAccessHandler)handler).createGetRequest(currentUrl);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Created WSS request: " + wssRequest); // NOI18N
                        }
                        openUrlInExternalBrowser(wssRequest);

                        return;
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("No special handler. Default access via open URL"); // NOI18N
                        }
                    }
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("No handler available for given URL. Default access via open URL."); // NOI18N
                    }
                }

                openUrlInExternalBrowser(currentUrl);
            } catch (final Exception ex) {
                LOG.error("Error while creating URL for FeatureInfo", ex); // NOI18N
            }
        } else {
            openUrlInExternalBrowser("http://www.cismet.de");              // NOI18N
        }
    }                                                                      //GEN-LAST:event_cmdOpenExternalActionPerformed

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class FeatureInfoRetriever extends SwingWorker<String, Void> {

        //~ Instance fields ----------------------------------------------------

        private String url;
        private URL baseUrl;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FeatureInfoRetriever object.
         *
         * @param  url  DOCUMENT ME!
         */
        FeatureInfoRetriever(final String url) {
            this.url = url;
            try {
                if (url.indexOf('?') != -1) {
                    baseUrl = new URL(url.substring(0, url.indexOf('?')));
                } else {
                    baseUrl = new URL(url);
                }
            } catch (MalformedURLException ex) {
                LOG.error("Extraction of the base URL from '" + url + "' wasn't possible.");
            }
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected String doInBackground() throws Exception {
            if (LOG.isDebugEnabled()) {
                LOG.debug("FeatureInfoRetriever started"); // NOI18N
            }
            try {
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            if (tabbedparent != null) {
                                tabbedparent.setIconAt(
                                    tabbedparent.indexOfComponent(OGCWMSGetFeatureInfoRequestHtmlFSDisplay.this),
                                    icoProgress);
                            }
                        }
                    });

                if (isCancelled()) {
                    return null;
                }
                final BufferedInputStream in = new BufferedInputStream(WebAccessManager.getInstance().doRequest(
                            baseUrl,
                            url,
                            ACCESS_METHODS.GET_REQUEST));
                if (isCancelled()) {
                    return null;
                }
                final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream(4096);
                final byte[] c = new byte[4096];
                int count = -1;
                while ((count = in.read(c)) != -1) {
                    if (isCancelled()) {
                        return null;
                    }
                    byteArrayOut.write(c);
                }
                return byteArrayOut.toString();
            } catch (final Exception ex) {
                LOG.error("Error while fetching FeatureInfos", ex); // NOI18N
                return null;
            }
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                LOG.warn("FeatureInfoRetriever was cancelled."); // NOI18N
                return;
            }

            try {
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            if (tabbedparent != null) {
                                tabbedparent.setIconAt(
                                    tabbedparent.indexOfComponent(OGCWMSGetFeatureInfoRequestHtmlFSDisplay.this),
                                    icoInfo);
                            }
                        }
                    });

                String urlForFurtherRequests = url;
                if (baseUrl != null) {
                    urlForFurtherRequests = baseUrl.toString();
                }
                xhtmlPanel.setDocumentFromString(get(), urlForFurtherRequests, new XhtmlNamespaceHandler());
            } catch (final Exception ex) {
                LOG.error("Error while processing data of FeatureInfoRetriever", ex); // NOI18N
                return;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ShowLoadingStateLinkListener extends LinkListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void linkClicked(final BasicPanel panel, final String uri) {
            if ((currentWorker != null) && !currentWorker.isCancelled()) {
                currentWorker.cancel(true);
            }
            currentWorker = new FeatureInfoRetriever(uri);
            CismapBroker.getInstance().execute(currentWorker);
        }
    }
}
