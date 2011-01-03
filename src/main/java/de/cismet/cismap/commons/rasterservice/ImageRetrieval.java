/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.rasterservice;

import org.apache.commons.httpclient.HttpClient;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.net.URL;

import javax.swing.JComponent;

import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

import de.cismet.security.AccessHandler.ACCESS_METHODS;

import de.cismet.security.WebAccessManager;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class ImageRetrieval extends Thread {

    //~ Instance fields --------------------------------------------------------

    Image image = null;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private String url;
    private ImageObserverInterceptor observer;
    private RetrievalListener listener = null;
    private ByteArrayOutputStream byteArrayOut = null;
//    private URLConnection uc = null;
//    private InputStream is = null;
    private WMSCapabilities cap;
    private HttpClient preferredHttpClient;
    private volatile boolean youngerCall = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ImageRetrieval.
     *
     * @param  listener  DOCUMENT ME!
     */
    public ImageRetrieval(final RetrievalListener listener) {
        this.listener = listener;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void youngerWMSCall() {
        youngerCall = true;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if (log.isDebugEnabled()) {
            log.debug("interrupt())"); // NOI18N
//            log.debug("interrupt())", new Exception());
        }
        releaseConnection();
    }
//    GetMethod getMethod = null;

    @Override
    public void run() {
        BufferedInputStream in = null;
        // new
        try {
            if (log.isDebugEnabled()) {
                log.debug("start of ImageRetrieval: " + url);                                                  // NOI18N
            }
            listener.retrievalStarted(new RetrievalEvent());
//            URL u = new URL(url.toString());
            if (cap != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieve: " + url.toString() + " WMSCapability: " + cap.getLayer().getTitle()); // NOI18N
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieve: " + url.toString());                                                  // NOI18N
                }
            }
            // !!! old retrieval !!!
            // uc=u.openConnection();
            // log.debug("contenttype: "+uc.getContentType());
            // uc.connect();
            // is=uc.getInputStream();
            // BufferedInputStream in = new BufferedInputStream(is);

//            if (getMethod==null) {
//                getMethod=new GetMethod();
//            }
//            else {
//                releaseConnection();
//                getMethod=new GetMethod();
//            }
            String urlBase = null;
            String requestParameter = null;
            int indexOfCharacter = 0;
            if ((indexOfCharacter = url.indexOf('?')) != -1) {
                urlBase = url.substring(0, indexOfCharacter);
                if ((indexOfCharacter + 1) < url.length()) {
                    requestParameter = url.substring(indexOfCharacter + 1, url.length());
                }
            } else {
                urlBase = url;
                requestParameter = ""; // NOI18N
            }

//            if(cap != null){
//                //ToDO!!! checken ob HTTP AUTH noch funktioniert
//                //in = new BufferedInputStream(HttpAuthentication.getBufferedInputStreamFromCapabilities(cap,u,getMethod));
//                in = new BufferedInputStream(WebAccessManager.getInstance().doRequest(new URL(urlBase), requestParameter, ACCESS_METHODS.GET_REQUEST));
//            } else {
//                //in = new BufferedInputStream(HttpAuthentication.getBufferedInputStreamFromURL(u,getMethod));
//                in = new BufferedInputStream(WebAccessManager.getInstance().doRequest(new URL(urlBase), requestParameter, ACCESS_METHODS.GET_REQUEST));
//            }
            in = new BufferedInputStream(WebAccessManager.getInstance().doRequest(
                        new URL(urlBase),
                        requestParameter,
                        ACCESS_METHODS.GET_REQUEST));

            byteArrayOut = new ByteArrayOutputStream();

            int c;
            // ToDo performanz
            while ((c = in.read()) != -1) {
                byteArrayOut.write(c);
                if (youngerCall || isInterrupted()) {
                    fireLoadingAborted();
                    if (log.isDebugEnabled()) {
                        log.debug("interrupt during retrieval"); // NOI18N
                    }
                    releaseConnection();
                    return;
                }
            }

            // Image image =observer.createImage( (ImageProducer) o);
            observer = new ImageObserverInterceptor();
            // Image image =Toolkit.getDefaultToolkit().getImage(is);
            image = Toolkit.getDefaultToolkit().createImage(byteArrayOut.toByteArray());
            observer.prepareImage(image, observer);
            while ((observer.checkImage(image, observer) & ImageObserver.ALLBITS) != ImageObserver.ALLBITS) {
                Thread.sleep(10);
                if (youngerCall || isInterrupted()) {
                    fireLoadingAborted();
                    if (log.isDebugEnabled()) {
                        log.debug("interrupt during assembling"); // NOI18N
                    }
                    releaseConnection();
                    return;
                }
            }
            final RetrievalEvent e = new RetrievalEvent();
            e.setIsComplete(true);
            e.setRetrievedObject(image);
            if (!youngerCall && !isInterrupted()) {
                listener.retrievalComplete(e);
                if (log.isDebugEnabled()) {
                    log.debug("Retrieval complete");              // NOI18N
                }
            } else {
                fireLoadingAborted();
            }
        } catch (Exception e) {
            log.error("Error in ImageRetrieval output=" + byteArrayOut); // NOI18N
            final RetrievalEvent re = new RetrievalEvent();
            re.setIsComplete(false);
            if ((e.getMessage() == null) || e.getMessage().equals("null")) { // NOI18N
                try {
                    final String cause = e.getCause().getMessage();
                    re.setRetrievedObject(cause);
                } catch (Exception ee) {
                }
            } else {
                re.setRetrievedObject(e.getMessage());
                re.setErrorType(RetrievalEvent.CLIENTERROR);
            }

            listener.retrievalError(re);
            log.error("Error while loading the image", e); // NOI18N
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    log.warn(ex, ex);
                }
            }
        }
        releaseConnection();
    }

    /**
     * DOCUMENT ME!
     */
    private void releaseConnection() {
//        if (getMethod != null) {
//            log.debug("Release Connection");
//            if (getMethod != null) {
//                getMethod.releaseConnection();
//            }
//            if (getMethod != null) {
//                getMethod.abort();
//            }
//            getMethod = null;
//        }
    }

    /**
     * DOCUMENT ME!
     */
    public void fireLoadingAborted() {
//        RetrievalEvent e=new RetrievalEvent();
//        listener.retrievalAborted(e);
        // TODO nochmal anschauen
        log.info("Retrieval interrupted"); // NOI18N
        image = null;
        observer = null;
//        if (is != null) {
//            try {
//                is.close();
//            } catch (IOException ioe) {
//                log.warn("Exception during premature closing of the inputstream", ioe);
//            }
//        }
//        System.gc();
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
     * new.
     *
     * @param  cap  DOCUMENT ME!
     */
    public void setWMSCapabilities(final WMSCapabilities cap) {
        this.cap = cap;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public HttpClient getPreferredHttpClient() {
        return preferredHttpClient;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  preferredHttpClient  DOCUMENT ME!
     */
    public void setPreferredHttpClient(final HttpClient preferredHttpClient) {
        this.preferredHttpClient = preferredHttpClient;
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
                log.error("error during image retrieval: '" + error + "'"); // NOI18N
                e.setRetrievedObject(error);
                e.setErrorType(RetrievalEvent.SERVERERROR);
                listener.retrievalError(e);
            }
            return ret;
        }
    }
}
