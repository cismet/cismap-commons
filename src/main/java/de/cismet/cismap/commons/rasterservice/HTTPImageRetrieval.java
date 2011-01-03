/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.rasterservice;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JComponent;

import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class HTTPImageRetrieval extends Thread {

    //~ Instance fields --------------------------------------------------------

    Image image = null;
    HttpClient client;
    GetMethod method;
    String url;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private RetrievalListener listener = null;
    private ImageObserverInterceptor observer;
    private ByteArrayOutputStream byteArrayOut = null;
    private boolean youngerCall = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of HTTPImageRetrieval.
     *
     * @param  listener  DOCUMENT ME!
     */
    public HTTPImageRetrieval(final RetrievalListener listener) {
        this.listener = listener;
        client = new HttpClient();
        // client.getHostConfiguration().setProxy("www-proxy.htw-saarland.de", 3128);
        if (log.isDebugEnabled()) {
            log.debug("proxySet:" + System.getProperty("http.proxyHost"));  // NOI18N
        }
        if (log.isDebugEnabled()) {
            log.debug("ProxyHost:" + System.getProperty("http.proxyHost")); // NOI18N
        }
        if (log.isDebugEnabled()) {
            log.debug("ProxyPort:" + System.getProperty("http.proxyPort")); // NOI18N
        }

        final String proxySet = System.getProperty("proxySet");                      // NOI18N
        if ((proxySet != null) && proxySet.equals("true")) {                         // NOI18N
            if (log.isDebugEnabled()) {
                log.debug("proxyIs Set");                                            // NOI18N
                log.debug("ProxyHost:" + System.getProperty("http.proxyHost"));      // NOI18N
            }
            if (log.isDebugEnabled()) {
                log.debug("ProxyPort:" + System.getProperty("http.proxyPort"));      // NOI18N
            }
            try {
                client.getHostConfiguration()
                        .setProxy(System.getProperty("http.proxyHost"),
                            Integer.parseInt(System.getProperty("http.proxyPort"))); // NOI18N
            } catch (Exception e) {
                log.error("Problem while setting proxy", e);                         // NOI18N
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("no proxyIs Set");                                         // NOI18N
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (method != null) {
            method.abort();
        }
        method = new GetMethod(url);
        if (!method.isAborted()) {
            try {
                final int statusCode = client.executeMethod(method);

                if (statusCode != -1) {
                    if (log.isDebugEnabled()) {
                        log.debug("reading: " + url); // NOI18N
                    }
                    final InputStream is = method.getResponseBodyAsStream();
                    final BufferedInputStream in = new BufferedInputStream(is);
                    byteArrayOut = new ByteArrayOutputStream();
                    int c;

                    while ((c = in.read()) != -1) {
                        byteArrayOut.write(c);
                        if (youngerCall) {
                            fireLoadingAborted();
                            if (log.isDebugEnabled()) {
                                log.debug("interrupt during retrieval"); // NOI18N
                            }
                            return;
                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("creating image");                     // NOI18N
                    }
                    // Image image =observer.createImage( (ImageProducer) o);
                    observer = new ImageObserverInterceptor();
                    // Image image =Toolkit.getDefaultToolkit().getImage(is);
                    image = Toolkit.getDefaultToolkit().createImage(byteArrayOut.toByteArray());
                    observer.prepareImage(image, observer);
                    while ((observer.checkImage(image, observer) & observer.ALLBITS) != observer.ALLBITS) {
                        Thread.sleep(10);
                        if (youngerCall) {
                            fireLoadingAborted();
                            if (log.isDebugEnabled()) {
                                log.debug("interrupt during assembling"); // NOI18N
                            }
                            return;
                        }
                    }

                    final RetrievalEvent e = new RetrievalEvent();
                    e.setIsComplete(true);
                    e.setRetrievedObject(image);
                    if (!youngerCall) {
                        listener.retrievalComplete(e);
                        if (log.isDebugEnabled()) {
                            log.debug("Retrieval complete"); // NOI18N
                        }
                    } else {
                        fireLoadingAborted();
                    }
                    method.releaseConnection();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUrl() {
        return url;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  url  DOCUMENT ME!
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * DOCUMENT ME!
     */
    public void endRetrieval() {
        if (method != null) {
            method.abort();
        }
        youngerCall = true;
    }

    /**
     * DOCUMENT ME!
     */
    public void fireLoadingAborted() {
        log.info("Retrieval interrupted"); // NOI18N
        if ((method != null) && !method.isAborted()) {
            method.abort();
        }
        image = null;
        observer = null;

        System.gc();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ImageObserverInterceptor extends JComponent {

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean imageUpdate(final Image img,
                final int infoflags,
                final int x,
                final int y,
                final int width,
                final int height) {
            final boolean ret = super.imageUpdate(img, infoflags, x, y, width, height);
//            log.debug("ImageUpdate");
//            log.debug("y "+height);
//            log.debug("img.getHeight"+img.getHeight(this));

            if ((infoflags & ImageObserver.SOMEBITS) != 0) {
                final RetrievalEvent e = new RetrievalEvent();
                e.setPercentageDone((int)(y / (img.getHeight(this) - 1.0) * 100));
                listener.retrievalProgress(e);
            } else if ((infoflags & ImageObserver.ABORT) != 0) {
            } else if ((infoflags & ImageObserver.ERROR) != 0) {
                final RetrievalEvent e = new RetrievalEvent();
                e.setHasErrors(true);
                final String error = new String(byteArrayOut.toByteArray());
                e.setRetrievedObject(error);
                listener.retrievalError(e);
            }
            return ret;
        }
    }
}
