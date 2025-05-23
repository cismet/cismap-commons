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

import calpa.html.CalCons;
import calpa.html.CalHTMLPane;
import calpa.html.CalHTMLPreferences;
import calpa.html.DefaultCalHTMLObserver;

import org.apache.log4j.Logger;

import org.openide.util.lookup.ServiceProvider;

import java.applet.AppletContext;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.MouseEvent;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import java.net.URL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.cismet.cismap.commons.gui.featureinfowidget.AbstractFeatureInfoDisplay;
import de.cismet.cismap.commons.gui.featureinfowidget.FeatureInfoDisplay;
import de.cismet.cismap.commons.gui.featureinfowidget.FeatureInfoDisplayKey;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.MapClickedEvent;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.cismap.commons.retrieval.UniversalRetrieval;

import de.cismet.commons.security.AccessHandler;
import de.cismet.commons.security.AccessHandler.ACCESS_METHODS;

import de.cismet.security.WebAccessManager;

import de.cismet.security.handler.WSSAccessHandler;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = FeatureInfoDisplay.class)
public class OGCWMSGetFeatureInfoRequestHtmlDisplay extends AbstractFeatureInfoDisplay<WMSLayer>
        implements RetrievalListener,
            HyperlinkListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(OGCWMSGetFeatureInfoRequestHtmlDisplay.class);

    //~ Instance fields --------------------------------------------------------

    WMSLayer wmsLayer = null;
    UniversalRetrieval ur = null;
    private final Icon icoProgress = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/featureinfowidget/res/progress.png"));   // NOI18N
    private final Icon icoProgress64 = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/featureinfowidget/res/progress64.png")); // NOI18N
    private final Icon icoInfo = new ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/featureinfowidget/res/info.png"));       // NOI18N

    private final DefaultCalHTMLObserver htmlObserver = new DefaultCalHTMLObserver() {

            @Override
            public void statusUpdate(final CalHTMLPane calHTMLPane,
                    final int i,
                    final URL uRL,
                    final int i0,
                    final String string) {
                super.statusUpdate(calHTMLPane, i, uRL, i0, string);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("StatusUpdate" + i + uRL); // NOI18N
                }
            }

            @Override
            public void linkActivatedUpdate(final CalHTMLPane calHTMLPane,
                    final URL uRL,
                    final String string,
                    final String string0) {
                super.linkActivatedUpdate(calHTMLPane, uRL, string, string0);
            }

            @Override
            public void linkFocusedUpdate(final CalHTMLPane calHTMLPane, final URL uRL) {
                super.linkFocusedUpdate(calHTMLPane, uRL);
            }
        };

    private calpa.html.CalHTMLPane calpaHtmlPane;
    private final CalHTMLPreferences htmlPrefs;
    private AppletContext appletContext;
    private boolean shiftDown;
    private JTabbedPane tabbedparent;
    private String urlBuffer;
    private SwingWorker currentWorker;
    private FXPanelWrapper fxBrowserPanel;
    private boolean fxIniterror = false;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdOpenExternal;
    private javax.swing.JButton cmdRefresh;
    private javax.swing.JTextPane htmlPane_;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pnlWebView;
    private javax.swing.JToolBar tbLeft;
    private javax.swing.JToolBar tbRight;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form AbstractFeatureInfoDisplay.
     */
    public OGCWMSGetFeatureInfoRequestHtmlDisplay() {
        super(new FeatureInfoDisplayKey(
                WMSLayer.class,
                FeatureInfoDisplayKey.ANY_SERVER,
                FeatureInfoDisplayKey.ANY_LAYER));

        htmlPrefs = new CalHTMLPreferences();
        htmlPrefs.setAutomaticallyFollowHyperlinks(false);
        htmlPrefs.setHandleFormSubmission(false);
        htmlPrefs.setOptimizeDisplay(CalCons.OPTIMIZE_ALL);
        htmlPrefs.setDisplayErrorDialogs(false);
        htmlPrefs.setLoadImages(true);

        initComponents();
        tbLeft.setVisible(false);
        /*
         *If something wents wrong with the initialistion of the JavaFX WebView we fall back to calpa to ensure backward
         * compatibilty
         */
        try {
            if (System.getProperty("java.version").startsWith("1.6")) {
                fxIniterror = true;
                initCalpaAsFallback();
            } else {
                fxBrowserPanel = new FXPanelWrapper();
                pnlWebView.add(fxBrowserPanel, BorderLayout.CENTER);
            }
        } catch (Error e) {
            fxIniterror = true;
            LOG.warn("Error initialising JavaFX WebView. Using Calpa as Fallback", e);
            initCalpaAsFallback();
        } catch (Exception ex) {
            fxIniterror = true;
            LOG.warn("Excpetion initialising JavaFX WebView. Using Calpa as Fallback", ex);
            initCalpaAsFallback();
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void initCalpaAsFallback() {
        calpaHtmlPane = new CalHTMLPane(htmlPrefs, htmlObserver, "cismap");
        pnlWebView.removeAll();
        pnlWebView.add(calpaHtmlPane, BorderLayout.CENTER);
    }

    @Override
    public void init(final WMSLayer layer, final JTabbedPane parentTabbedPane) {
        this.wmsLayer = layer;
        this.tabbedparent = parentTabbedPane;
    }

    @Override
    public void showFeatureInfo(final MapClickedEvent mce) {
        showContent((int)mce.getX(), (int)mce.getY());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  x  DOCUMENT ME!
     * @param  y  DOCUMENT ME!
     */
    private void showContent(final int x, final int y) {
        final String url = wmsLayer.getParentServiceLayer().getGetFeatureInfoUrl(x, y, wmsLayer);
        if (LOG.isDebugEnabled()) {
            LOG.debug("showContet of " + url);                 // NOI18N
        }
        urlBuffer = url;
        if ((currentWorker != null) && !currentWorker.isCancelled()) {
            currentWorker.cancel(true);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("before FeatureInfoRetriever creation"); // NOI18N
        }
        currentWorker = new FeatureInfoRetriever(url);
        if (LOG.isDebugEnabled()) {
            LOG.debug("afterFeatureInfoCreation");             // NOI18N
        }
        CismapBroker.getInstance().execute(currentWorker);
    }

    @Override
    public void retrievalStarted(final RetrievalEvent e) {
    }

    @Override
    public void retrievalProgress(final RetrievalEvent e) {
    }

    @Override
    public void retrievalError(final RetrievalEvent e) {
    }

    @Override
    public void retrievalComplete(final RetrievalEvent e) {
        if (tabbedparent != null) {
            tabbedparent.setIconAt(tabbedparent.indexOfComponent(this), icoInfo);
        }
        if (e.getRetrievedObject() instanceof String) {
            if (fxIniterror) {
                calpaHtmlPane.showHTMLDocument(e.getRetrievedObject().toString());
            } else {
                try {
                    fxBrowserPanel.getJfxPanel().loadContent(e.getRetrievedObject().toString());
                } catch (final Exception loadContentEx) {
                    LOG.error("Problem during loadContent of fxBrowserPanel.getJfxPanel():" + loadContentEx);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("String:" + e.getRetrievedObject().toString()); // NOI18N
            }
        } else if (e.getRetrievedObject() instanceof Image) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Image:" + e.getRetrievedObject());             // NOI18N
            }
        }
    }

    @Override
    public void retrievalAborted(final RetrievalEvent e) {
    }

    /**
     * Called when a hypertext link is updated.
     *
     * @param  event  e the event responsible for the update
     */
    @Override
    public void hyperlinkUpdate(final HyperlinkEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hyperlinkUpdate: " + event); // NOI18N
        }
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (shiftDown) {
                openUrlInExternalBrowser(event.getURL().toExternalForm());
            } else {
                final UniversalRetrieval ur = new UniversalRetrieval(event.getURL().toExternalForm());
                ur.addRetrievalListener(this);
                ur.retrieve(false);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  url  DOCUMENT ME!
     */
    private void openUrlInExternalBrowser(final String url) {
        try {
            if (appletContext == null) {
                de.cismet.tools.BrowserLauncher.openURL(url);
            } else {
                final java.net.URL u = new java.net.URL(url);
                appletContext.showDocument(u, "cismetBrowser");         // NOI18N
            }
        } catch (final Exception e) {
            LOG.warn("Error while opening: " + url + ".\nNew try.", e); // NOI18N
            // Nochmal zur Sicherheit mit dem BrowserLauncher probieren
            try {
                de.cismet.tools.BrowserLauncher.openURL(url);
            } catch (final Exception e2) {
                LOG.warn("Second try also failed. Error while opening: " + url + "\nLast try.", e2); // NOI18N
                try {
                    de.cismet.tools.BrowserLauncher.openURL("file://" + url);                        // NOI18N
                } catch (final Exception e3) {
                    LOG.error("Third try also failed. Error while opening: file://" + url, e3);      // NOI18N
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        htmlPane_ = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        tbLeft = new javax.swing.JToolBar();
        tbLeft.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        cmdRefresh = new javax.swing.JButton();
        tbRight = new javax.swing.JToolBar();
        tbRight.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        cmdOpenExternal = new javax.swing.JButton();
        pnlWebView = new javax.swing.JPanel();

        htmlPane_.setEditable(false);
        htmlPane_.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

                @Override
                public void mouseMoved(final java.awt.event.MouseEvent evt) {
                    htmlPane_MouseMoved(evt);
                }
            });
        htmlPane_.addKeyListener(new java.awt.event.KeyAdapter() {

                @Override
                public void keyPressed(final java.awt.event.KeyEvent evt) {
                    htmlPane_KeyPressed(evt);
                }
            });

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        tbLeft.setFloatable(false);

        cmdRefresh.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/featureinfowidget/res/reload16.gif"))); // NOI18N
        cmdRefresh.setText(org.openide.util.NbBundle.getMessage(
                OGCWMSGetFeatureInfoRequestHtmlDisplay.class,
                "OGCWMSGetFeatureInfoRequestHtmlDisplay.cmdRefresh.text"));                                   // NOI18N
        cmdRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(
                OGCWMSGetFeatureInfoRequestHtmlDisplay.class,
                "OGCWMSGetFeatureInfoRequestHtmlDisplay.cmdRefresh.toolTipText"));                            // NOI18N
        cmdRefresh.setBorderPainted(false);
        cmdRefresh.setFocusable(false);
        cmdRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cmdRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cmdRefresh.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdRefreshActionPerformed(evt);
                }
            });
        tbLeft.add(cmdRefresh);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(tbLeft, gridBagConstraints);

        tbRight.setFloatable(false);

        cmdOpenExternal.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/featureinfowidget/res/extWindow.png"))); // NOI18N
        cmdOpenExternal.setText(org.openide.util.NbBundle.getMessage(
                OGCWMSGetFeatureInfoRequestHtmlDisplay.class,
                "OGCWMSGetFeatureInfoRequestHtmlDisplay.cmdOpenExternal.text"));                               // NOI18N
        cmdOpenExternal.setToolTipText(org.openide.util.NbBundle.getMessage(
                OGCWMSGetFeatureInfoRequestHtmlDisplay.class,
                "OGCWMSGetFeatureInfoRequestHtmlDisplay.cmdOpenExternal.toolTipText"));                        // NOI18N
        cmdOpenExternal.setBorderPainted(false);
        cmdOpenExternal.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdOpenExternalActionPerformed(evt);
                }
            });
        tbRight.add(cmdOpenExternal);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(tbRight, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        pnlWebView.setLayout(new java.awt.BorderLayout());
        add(pnlWebView, java.awt.BorderLayout.CENTER);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdOpenExternalActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdOpenExternalActionPerformed

        if (urlBuffer != null) {
            try {
                // ToDo muss in WebAccessManger
                final AccessHandler handler = WebAccessManager.getInstance().getHandlerForURL(new URL(urlBuffer));
                if (handler != null) {
                    if (handler instanceof WSSAccessHandler) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("handler is wss handler --> creating wss get request"); // NOI18N
                        }

                        final String wssRequest = ((WSSAccessHandler)handler).createGetRequest(urlBuffer);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("created wss request: " + wssRequest); // NOI18N
                        }
                        openUrlInExternalBrowser(wssRequest);

                        return;
                    } else if (LOG.isDebugEnabled()) {
                        LOG.debug("No special handler --> default access via open URL"); // NOI18N
                    }
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug("no handler available for given url default access via openURL"); // NOI18N
                }
                openUrlInExternalBrowser(urlBuffer);
            } catch (final Exception ex) {
                LOG.error("Error while creating url for featureinfo", ex);               // NOI18N
            }
        } else {
            openUrlInExternalBrowser("http://www.cismet.de");                            // NOI18N
        }
    }                                                                                    //GEN-LAST:event_cmdOpenExternalActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void htmlPane_MouseMoved(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_htmlPane_MouseMoved
        shiftDown = (evt.getModifiers() & MouseEvent.SHIFT_MASK) == MouseEvent.SHIFT_MASK;
    }                                                                       //GEN-LAST:event_htmlPane_MouseMoved

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void htmlPane_KeyPressed(final java.awt.event.KeyEvent evt) { //GEN-FIRST:event_htmlPane_KeyPressed
    }                                                                     //GEN-LAST:event_htmlPane_KeyPressed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdRefreshActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmdRefreshActionPerformed
        fxBrowserPanel.refresh();
    }                                                                              //GEN-LAST:event_cmdRefreshActionPerformed

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class FeatureInfoRetriever extends SwingWorker<String, Void> {

        //~ Instance fields ----------------------------------------------------

        private String url;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FeatureInfoRetriever object.
         *
         * @param  url  DOCUMENT ME!
         */
        FeatureInfoRetriever(final String url) {
            this.url = url;
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
                                    tabbedparent.indexOfComponent(OGCWMSGetFeatureInfoRequestHtmlDisplay.this),
                                    icoProgress);
                            }
                        }
                    });

                final URL baseUrl;
                final String parameter;
                if (url.indexOf('?') != -1) {
                    baseUrl = new URL(url.substring(0, url.indexOf('?')));
                    parameter = url.substring(url.indexOf('?') + 1, url.length());
                } else {
                    baseUrl = new URL(url);
                    parameter = "";
                }
                if (isCancelled()) {
                    return null;
                }
                final BufferedInputStream in = new BufferedInputStream(WebAccessManager.getInstance().doRequest(
                            baseUrl,
                            parameter,
                            ACCESS_METHODS.GET_REQUEST));
                if (isCancelled()) {
                    return null;
                }
                final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                int c;
                while ((c = in.read()) != -1) {
                    if (isCancelled()) {
                        return null;
                    }
                    byteArrayOut.write(c);
                }

                String s = byteArrayOut.toString();
                final String charset = getCharset(s);

                if (charset != null) {
                    try {
                        // the charset from the html code should be used. Otherwise, special
                        // characters will be wrong
                        s = byteArrayOut.toString(charset);
                    } catch (UnsupportedEncodingException e) {
                        LOG.error("Unsupported charset used", e);
                    }
                }

                return s;
            } catch (final Exception ex) {
                LOG.error("Error while fetching FeatureInfos", ex); // NOI18N
                return null;
            }
        }

        /**
         * Try to odetermine the charsert of the given html code.
         *
         * @param   s  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String getCharset(final String s) {
            // The regular expression is an easy way to find out the characterset.
            // Not 100 percent accurate, but it should be good enought
            final Pattern p = Pattern.compile(
                    "<meta(?!\\s*(?:name|value)\\s*=)[^>]*?charset\\s*=[\\s\"']*([^\\s\"'/>]*)");
            final Matcher m = p.matcher(s);

            if (m.find()) {
                return m.group(1);
            } else {
                return null;
            }
        }

        @Override
        protected void done() {
            super.done();
            if (isCancelled()) {
                LOG.warn("FeatureInfoRetriever was canceled"); // NOI18N
                return;
            }
            try {
                if (tabbedparent != null) {
                    tabbedparent.setIconAt(tabbedparent.indexOfComponent(OGCWMSGetFeatureInfoRequestHtmlDisplay.this),
                        icoInfo);
                }
                final String result = get();
                // ToDo more generic it should be possible to display images
                if (fxIniterror) {
                    calpaHtmlPane.showHTMLDocument(result);
                } else {
                    fxBrowserPanel.getJfxPanel().loadContent(result);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("String:" + result);                                    // NOI18N
                }
            } catch (final Exception ex) {
                LOG.error("Error while processing data of FeatureInfoRetriever", ex); // NOI18N
                return;
            }
        }
    }
}
