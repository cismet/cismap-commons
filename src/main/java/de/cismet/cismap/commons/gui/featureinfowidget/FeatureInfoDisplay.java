/*
 * FeatureInfoDisplay.java
 *
 * Created on 5. April 2006, 15:42
 */
package de.cismet.cismap.commons.gui.featureinfowidget;

import calpa.html.CalCons;
import calpa.html.CalHTMLPane;
import calpa.html.CalHTMLPreferences;
import calpa.html.DefaultCalHTMLObserver;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.cismap.commons.retrieval.UniversalRetrieval;
import de.cismet.security.AccessHandler;
import de.cismet.security.AccessHandler.ACCESS_METHODS;
import de.cismet.security.WebAccessManager;
import de.cismet.security.handler.WSSAccessHandler;
import java.awt.ComponentOrientation;

import java.awt.EventQueue;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 *
 * @author  thorsten.hell@cismet.de
 */
public class FeatureInfoDisplay extends javax.swing.JPanel implements RetrievalListener, HyperlinkListener {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private java.applet.AppletContext appletContext = null;
    WMSLayer wmsLayer = null;
    UniversalRetrieval ur = null;
    private boolean shiftDown;
    private int x = -1;
    private int y = -1;
    private JTabbedPane tabbedparent = null;
    Icon icoProgress = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/featureinfowidget/res/progress.png"));//NOI18N
    Icon icoProgress64 = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/featureinfowidget/res/progress64.png"));//NOI18N
    Icon icoInfo = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/featureinfowidget/res/info.png"));//NOI18N
    private String urlBuffer = null;
    private SwingWorker currentWorker = null;
    DefaultCalHTMLObserver htmlObserver = new DefaultCalHTMLObserver() {

        public void statusUpdate(CalHTMLPane calHTMLPane, int i, URL uRL, int i0, String string) {
            super.statusUpdate(calHTMLPane, i, uRL, i0, string);
            log.debug("StatusUpdate" + i + uRL);//NOI18N
        }

        public void linkActivatedUpdate(CalHTMLPane calHTMLPane, URL uRL, String string, String string0) {
            super.linkActivatedUpdate(calHTMLPane, uRL, string, string0);
        }

        public void linkFocusedUpdate(CalHTMLPane calHTMLPane, URL uRL) {
            super.linkFocusedUpdate(calHTMLPane, uRL);
        }
    };
    CalHTMLPreferences htmlPrefs = new CalHTMLPreferences();

    /** Creates new form FeatureInfoDisplay */
    public FeatureInfoDisplay() {
        htmlPrefs.setAutomaticallyFollowHyperlinks(false);
        htmlPrefs.setHandleFormSubmission(false);
        htmlPrefs.setOptimizeDisplay(CalCons.OPTIMIZE_ALL);
        htmlPrefs.setDisplayErrorDialogs(false);
        htmlPrefs.setLoadImages(true);


        initComponents();
    //htmlPane.addHyperlinkListener(this);

    }

    public FeatureInfoDisplay(WMSLayer l, JTabbedPane tabbedparent) {
        this();
        wmsLayer = l;
        this.tabbedparent = tabbedparent;

    }

    public void showContent(int x, int y) {
        this.x = x;
        this.y = y;
        String url = wmsLayer.getParentServiceLayer().getGetFeatureInfoUrl(x, y, wmsLayer);
        log.debug("showContet of " + url);//NOI18N
        urlBuffer=url;
        if (currentWorker != null && !currentWorker.isCancelled()) {
            currentWorker.cancel(true);
        }
        log.debug("before FeatureInfoRetriever creation");//NOI18N
        currentWorker = new FeatureInfoRetriever(url);
        log.debug("afterFeatureInfoCreation");//NOI18N
        CismapBroker.getInstance().execute(currentWorker);
//        urlBuffer=url;
//        UniversalRetrieval ur=new UniversalRetrieval(url);
//        ur.addRetrievalListener(this);
//        ur.retrieve(false);

//        try {
//            log.debug("FeatureInfoUrl:"+wmsLayer.getParentServiceLayer().getGetFeatureInfoUrl(x,y,wmsLayer));
//            htmlPane.showHTMLDocument(new URL(wmsLayer.getParentServiceLayer().getGetFeatureInfoUrl(x,y,wmsLayer)));
//
//        }
//        catch (Exception e) {
//            log.error("Fehler beim Anzeigen der FeatureInfo",e);
//        }
    }

    public void retrievalStarted(RetrievalEvent e) {
//        StyledDocument doc = (StyledDocument)htmlPane.getDocument();
//
//        htmlPane.setText("");
//        htmlPane.setContentType("text/plain");
//        htmlPane.setEditable(true);
//        // The image must first be wrapped in a style
//        Style style = doc.addStyle("StyleName", null);
//        StyleConstants.setIcon(style, icoProgress64);
//        try {
//
//            // Insert the image at the end of the text
//            doc.insertString(doc.getLength(), "ignored text", style);
//        } catch (BadLocationException ex) {
//            ex.printStackTrace();
//        }
//        htmlPane.setEditable(false);
        //htmlPane.setText("");
    }

    public void retrievalProgress(RetrievalEvent e) {
    }

    public void retrievalError(RetrievalEvent e) {
    }

    public void retrievalComplete(RetrievalEvent e) {
        if (tabbedparent != null) {
            tabbedparent.setIconAt(tabbedparent.indexOfComponent(this), icoInfo);
        }
        if (e.getRetrievedObject() instanceof String) {
//            htmlPane.setContentType("text/html");
//            htmlPane.setText(StaticHtmlTools.convertHTTPReferences(StaticHtmlTools.stripMetaTag(e.getRetrievedObject().toString())));
            htmlPane.showHTMLDocument(e.getRetrievedObject().toString());
            log.debug("String:" + e.getRetrievedObject().toString());//NOI18N
        } else if (e.getRetrievedObject() instanceof Image) {

//            htmlPane.setText("");
//            htmlPane.select(0,1);
//            htmlPane.insertIcon(new ImageIcon((Image)e.getRetrievedObject()));
            log.debug("Bild:" + e.getRetrievedObject());//NOI18N
        }
    }

    public void retrievalAborted(RetrievalEvent e) {
    }

    /**
     * Called when a hypertext link is updated.
     *
     *
     * @param e the event responsible for the update
     */
    public void hyperlinkUpdate(HyperlinkEvent event) {
        log.debug("hyperlinkUpdate: " + event);//NOI18N
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (shiftDown) {
                openUrlInExternalBrowser(event.getURL().toExternalForm());
            } else {
                UniversalRetrieval ur = new UniversalRetrieval(event.getURL().toExternalForm());
                ur.addRetrievalListener(this);
                ur.retrieve(false);
            }
        }
    }

    private void openUrlInExternalBrowser(String url) {
        try {
            if (appletContext == null) {
                de.cismet.tools.BrowserLauncher.openURL(url);
            } else {
                java.net.URL u = new java.net.URL(url);
                appletContext.showDocument(u, "cismetBrowser");//NOI18N

            }
        } catch (Exception e) {
            log.warn("Error while opening: " + url + ".\nNew try.", e);//NOI18N
            //Nochmal zur Sicherheit mit dem BrowserLauncher probieren
            try {
                de.cismet.tools.BrowserLauncher.openURL(url);
            } catch (Exception e2) {
                log.warn("Second try also failed. Error while opening: " + url + "\nLast try.", e2);//NOI18N
                try {
                    de.cismet.tools.BrowserLauncher.openURL("file://" + url);//NOI18N
                } catch (Exception e3) {
                    log.error("Third try also failed. Error while opening: file://" + url, e3);//NOI18N
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        htmlPane_ = new javax.swing.JTextPane();
        tbRight = new javax.swing.JToolBar();
        tbRight.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        cmdOpenExternal = new javax.swing.JButton();
        htmlPane = new CalHTMLPane(htmlPrefs,htmlObserver,"cismap");

        htmlPane_.setEditable(false);
        htmlPane_.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                htmlPane_MouseMoved(evt);
            }
        });
        htmlPane_.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                htmlPane_KeyPressed(evt);
            }
        });

        setLayout(new java.awt.BorderLayout());

        tbRight.setFloatable(false);

        cmdOpenExternal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/featureinfowidget/res/extWindow.png"))); // NOI18N
        cmdOpenExternal.setText(org.openide.util.NbBundle.getMessage(FeatureInfoDisplay.class, "FeatureInfoDisplay.cmdOpenExternal.text")); // NOI18N
        cmdOpenExternal.setToolTipText(org.openide.util.NbBundle.getMessage(FeatureInfoDisplay.class, "FeatureInfoDisplay.cmdOpenExternal.toolTipText")); // NOI18N
        cmdOpenExternal.setBorderPainted(false);
        cmdOpenExternal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdOpenExternalActionPerformed(evt);
            }
        });
        tbRight.add(cmdOpenExternal);

        add(tbRight, java.awt.BorderLayout.NORTH);

        htmlPane.setDoubleBuffered(true);
        add(htmlPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void cmdOpenExternalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdOpenExternalActionPerformed
        
        if (urlBuffer != null) {
            try{
                //ToDo muss in WebAccessManger
            AccessHandler handler = WebAccessManager.getInstance().getHandlerForURL(new URL(urlBuffer));
            if(handler != null){
                if(handler instanceof WSSAccessHandler){
                    log.debug("handler is wss handler --> creating wss get request");//NOI18N
                    String wssRequest = ((WSSAccessHandler)handler).createGetRequest(urlBuffer);
                    log.debug("created wss request: "+wssRequest);//NOI18N
                    openUrlInExternalBrowser(wssRequest);
                    return;                   
                } else {
                    log.debug("No special handler --> default access via open URL");//NOI18N
                }                
            } else {
                log.debug("no handler available for given url default access via openURL");//NOI18N
            }            
            openUrlInExternalBrowser(urlBuffer);
            }catch(Exception ex){
                log.error("Error while creating url for featureinfo");//NOI18N
            }
        } else {
            openUrlInExternalBrowser("http://www.cismet.de");//NOI18N
        }
    }//GEN-LAST:event_cmdOpenExternalActionPerformed

    private void htmlPane_MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_htmlPane_MouseMoved
        shiftDown = (evt.getModifiers() & evt.SHIFT_MASK) == evt.SHIFT_MASK;
    }//GEN-LAST:event_htmlPane_MouseMoved

    private void htmlPane_KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_htmlPane_KeyPressed
    }//GEN-LAST:event_htmlPane_KeyPressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdOpenExternal;
    private calpa.html.CalHTMLPane htmlPane;
    private javax.swing.JTextPane htmlPane_;
    private javax.swing.JToolBar tbRight;
    // End of variables declaration//GEN-END:variables

    class FeatureInfoRetriever extends SwingWorker<String, Void> {

        private String url;

        FeatureInfoRetriever(String url) {
            this.url = url;
        }

        protected String doInBackground() throws Exception {
            log.debug("FeatureInfoRetriever started");//NOI18N
            try {

                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        if (tabbedparent != null) {
                            tabbedparent.setIconAt(tabbedparent.indexOfComponent(FeatureInfoDisplay.this), icoProgress);
                        }
                    }
                });

                URL baseUrl = null;
                if (url.indexOf('?') != -1) {
                    baseUrl = new URL(url.substring(0, url.indexOf('?')));
                } else {
                    baseUrl = new URL(url);
                }
                if (isCancelled()) {
                    return null;
                }
                final BufferedInputStream in = new BufferedInputStream(WebAccessManager.getInstance().doRequest(baseUrl, url, ACCESS_METHODS.GET_REQUEST));
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
                return byteArrayOut.toString();
            } catch (Exception ex) {
                log.error("Error while fetching FeatureInfos", ex);//NOI18N
                return null;
            }
        }

        protected void done() {
            super.done();
            if (isCancelled()) {
                log.warn("FeatureInfoRetriever was canceled");//NOI18N
                return;
            }
            try {
                if (tabbedparent != null) {
                    tabbedparent.setIconAt(tabbedparent.indexOfComponent(FeatureInfoDisplay.this), icoInfo);
                }
                String result = get();
                //ToDo more generic it should be possible to display images
                //if (e.getRetrievedObject() instanceof String) {
//            htmlPane.setContentType("text/html");
//            htmlPane.setText(StaticHtmlTools.convertHTTPReferences(StaticHtmlTools.stripMetaTag(e.getRetrievedObject().toString())));
                htmlPane.showHTMLDocument(result);
                log.debug("String:" + result);//NOI18N

            //        } else if (e.getRetrievedObject() instanceof Image) {
//
////            htmlPane.setText("");
////            htmlPane.select(0,1);
////            htmlPane.insertIcon(new ImageIcon((Image)e.getRetrievedObject()));
//            log.debug("Bild:" + e.getRetrievedObject());
//        }
            } catch (Exception ex) {
                log.error("Error while processing data of FeatureInfoRetriever", ex);//NOI18N
                return;
            }
        }
    }
}
