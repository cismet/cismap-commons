/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.rasterservice;

import org.apache.commons.httpclient.HttpClient;

import org.openide.util.Exceptions;

import java.awt.Image;

import java.io.BufferedInputStream;
import java.io.IOException;

import java.net.URL;

import javax.imageio.ImageIO;

import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

import de.cismet.commons.security.AccessHandler.ACCESS_METHODS;

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
    BufferedInputStream in = null;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private String url;
    private RetrievalListener listener = null;
    private WMSCapabilities cap;
    private HttpClient preferredHttpClient;
    private volatile boolean youngerCall = false;
    private String payload;

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
            log.debug("interrupt()", new Exception("interrupt")); // NOI18N
        }
        if (in != null) {
            if (log.isDebugEnabled()) {
                log.info("in!=null");                             // NOI18N
                try {
                    in.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("start of ImageRetrieval: " + url); // NOI18N
            }
            listener.retrievalStarted(new RetrievalEvent());

            if (cap != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieve: " + url.toString() + " WMSCapability: " + cap.getLayer().getTitle()); // NOI18N
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieve: " + url.toString());                                                  // NOI18N
                }
            }

            final String urlBase;
            final String requestParameter;
            final ACCESS_METHODS method;

            if (payload == null) {
                // assume GET request
                int indexOfCharacter = 0;
                if ((indexOfCharacter = url.indexOf('?')) != -1) {
                    urlBase = url.substring(0, indexOfCharacter);
                    if ((indexOfCharacter + 1) < url.length()) {
                        requestParameter = url.substring(indexOfCharacter + 1, url.length());
                    } else {
                        requestParameter = ""; // NOI18N
                    }
                } else {
                    urlBase = url;
                    requestParameter = "";     // NOI18N
                }

                method = ACCESS_METHODS.GET_REQUEST;
            } else {
                // assume POST request
                urlBase = url;
                requestParameter = payload;
                method = ACCESS_METHODS.POST_REQUEST;

                if (log.isDebugEnabled()) {
                    log.debug("POST payload: " + payload); // NOI18N
                }
            }

            in = new BufferedInputStream(WebAccessManager.getInstance().doRequest(
                        new URL(urlBase),
                        requestParameter,
                        method));

            if (!youngerCall && !isInterrupted()) {
                image = ImageIO.read(in);
            }

            final RetrievalEvent e = new RetrievalEvent();
            e.setIsComplete(true);
            e.setRetrievedObject(image);
            if (!youngerCall && !isInterrupted()) {
                listener.retrievalComplete(e);
                if (log.isDebugEnabled()) {
                    log.debug("Retrieval complete of " + e.getRetrievalService()); // NOI18N
                }
            } else {
                fireLoadingAborted();
            }
        } catch (Exception e) {
            log.error("Error in ImageRetrieval", e);                               // NOI18N
            final RetrievalEvent re = new RetrievalEvent();
            re.setIsComplete(false);
            if ((e.getMessage() == null) || e.getMessage().equals("null")) {       // NOI18N
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
    }

    /**
     * DOCUMENT ME!
     */
    public void fireLoadingAborted() {
        // TODO nochmal anschauen
        log.info("Retrieval interrupted"); // NOI18N
        image = null;
        if (listener != null) {
            final RetrievalEvent e = new RetrievalEvent();
            e.setIsComplete(false);
            listener.retrievalAborted(e);
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
     *
     * @return  DOCUMENT ME!
     */
    public String getPayload() {
        return payload;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  payload  DOCUMENT ME!
     */
    public void setPayload(final String payload) {
        this.payload = payload;
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
}
