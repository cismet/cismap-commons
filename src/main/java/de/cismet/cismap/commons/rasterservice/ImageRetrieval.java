/*
 * ImageRetrieval.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 22. Juni 2005, 16:47
 *
 */
package de.cismet.cismap.commons.rasterservice;

import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.security.AccessHandler.ACCESS_METHODS;
import de.cismet.security.WebAccessManager;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.JComponent;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.deegree.services.wms.capabilities.WMSCapabilities;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class ImageRetrieval extends Thread {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private String url;
    private ImageObserverInterceptor observer;
    private RetrievalListener listener = null;
    private ByteArrayOutputStream byteArrayOut = null;
    private URLConnection uc = null;
    private InputStream is = null;
    private WMSCapabilities cap;
    private HttpClient preferredHttpClient;

    /** Creates a new instance of ImageRetrieval */
    public ImageRetrieval(RetrievalListener listener) {
        this.listener = listener;
    }
    private volatile boolean youngerCall = false;

    public void youngerWMSCall() {
        youngerCall = true;
    }
    Image image = null;

    @Override
    public void interrupt() {
        super.interrupt();
        if (log.isDebugEnabled()) {
            log.debug("interrupt())");
//            log.debug("interrupt())", new Exception());
        }
        releaseConnection();

    }
    GetMethod getMethod = null;

    @Override
    public void run() {
        try {
            //new
            log.debug("start of ImageRetrieval: " + url);
            listener.retrievalStarted(new RetrievalEvent());
            URL u = new URL(url.toString());
            if (cap != null) {
                log.debug("Retrieve: " + url.toString() + " WMSCapability: " + cap.getCapability().getLayer().getTitle());
            } else {
                log.debug("Retrieve: " + url.toString());
            }
            // !!! old retrieval !!!
            //uc=u.openConnection();
            //log.debug("contenttype: "+uc.getContentType());
            //uc.connect();
            //is=uc.getInputStream();
            //BufferedInputStream in = new BufferedInputStream(is);

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
                if (indexOfCharacter + 1 < url.length()) {
                    requestParameter = url.substring(indexOfCharacter + 1, url.length());
                }
            } else {
                urlBase = url;
                requestParameter = "";
            }

            BufferedInputStream in;
//            if(cap != null){
//                //ToDO!!! checken ob HTTP AUTH noch funktioniert
//                //in = new BufferedInputStream(HttpAuthentication.getBufferedInputStreamFromCapabilities(cap,u,getMethod));
//                in = new BufferedInputStream(WebAccessManager.getInstance().doRequest(new URL(urlBase), requestParameter, ACCESS_METHODS.GET_REQUEST));
//            } else {
//                //in = new BufferedInputStream(HttpAuthentication.getBufferedInputStreamFromURL(u,getMethod));
//                in = new BufferedInputStream(WebAccessManager.getInstance().doRequest(new URL(urlBase), requestParameter, ACCESS_METHODS.GET_REQUEST));
//            }
            in = new BufferedInputStream(WebAccessManager.getInstance().doRequest(new URL(urlBase), requestParameter, ACCESS_METHODS.GET_REQUEST));

            byteArrayOut = new ByteArrayOutputStream();

            int c;
            //ToDo performanz
            while ((c = in.read()) != -1) {
                byteArrayOut.write(c);
                if (youngerCall || isInterrupted()) {
                    fireLoadingAborted();
                    log.debug("interrupt during retrieval");
                    releaseConnection();
                    return;
                }
            }



            //Image image =observer.createImage( (ImageProducer) o);
            observer = new ImageObserverInterceptor();
            //Image image =Toolkit.getDefaultToolkit().getImage(is);
            image = Toolkit.getDefaultToolkit().createImage(byteArrayOut.toByteArray());
            observer.prepareImage(image, observer);
            while ((observer.checkImage(image, observer) & ImageObserver.ALLBITS) != ImageObserver.ALLBITS) {
                Thread.sleep(10);
                if (youngerCall || isInterrupted()) {
                    fireLoadingAborted();
                    log.debug("interrupt during assembling");
                    releaseConnection();
                    return;
                }
            }
            RetrievalEvent e = new RetrievalEvent();
            e.setIsComplete(true);
            e.setRetrievedObject(image);
            if (!youngerCall && !isInterrupted()) {
                listener.retrievalComplete(e);
                log.debug("Retrieval complete");
            } else {
                fireLoadingAborted();
            }

        } catch (Exception e) {


            log.error("Error in ImageRetrieval output=" + byteArrayOut);
            RetrievalEvent re = new RetrievalEvent();
            re.setIsComplete(false);
            if (e.getMessage() == null || e.getMessage().equals("null")) {
                try {
                    String cause = e.getCause().getMessage();
                    re.setRetrievedObject(cause);
                } catch (Exception ee) {
                }
            } else {
                re.setRetrievedObject(e.getMessage());
                re.setErrorType(RetrievalEvent.CLIENTERROR);
            }

            listener.retrievalError(re);
            log.error("Fehler beim Laden des Bildes ", e);
        }
        releaseConnection();
    }

    private void releaseConnection() {

        if (getMethod != null) {
            log.debug("Release Connection");
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
            if (getMethod != null) {
                getMethod.abort();
            }
            getMethod = null;
        }
    }

    public void fireLoadingAborted() {
//        RetrievalEvent e=new RetrievalEvent();
//        listener.retrievalAborted(e);
        //TODO nochmal anschauen
        log.info("Retrieval unterbrochen");
        image = null;
        observer = null;
        if (is != null) {
            try {
                is.close();
            } catch (IOException ioe) {
                log.warn("Exception during premature closing of the inputstream", ioe);
            }
        }
//        System.gc();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    //new
    public void setWMSCapabilities(WMSCapabilities cap) {
        this.cap = cap;
    }

    public HttpClient getPreferredHttpClient() {
        return preferredHttpClient;
    }

    public void setPreferredHttpClient(HttpClient preferredHttpClient) {
        this.preferredHttpClient = preferredHttpClient;
    }

    private class ImageObserverInterceptor extends JComponent {

        @Override
        public boolean imageUpdate(Image img,
                int infoflags,
                int x,
                int y,
                int width,
                int height) {
            boolean ret = super.imageUpdate(img, infoflags, x, y, width, height);
//            log.debug("ImageUpdate");
//            log.debug("y "+height);
//            log.debug("img.getHeight"+img.getHeight(this));


            if ((infoflags & ImageObserver.SOMEBITS) != 0) {
                RetrievalEvent e = new RetrievalEvent();
                e.setPercentageDone((int) (y / (img.getHeight(this) - 1.0) * 100));
                listener.retrievalProgress(e);
            } else if ((infoflags & ImageObserver.ABORT) != 0) {
            } else if ((infoflags & ImageObserver.ERROR) != 0) {
                RetrievalEvent e = new RetrievalEvent();
                e.setHasErrors(true);
                String error = new String(byteArrayOut.toByteArray());
                log.error("error during image retrieval: '" + error + "'");
                e.setRetrievedObject(error);
                e.setErrorType(RetrievalEvent.SERVERERROR);
                listener.retrievalError(e);
            }
            return ret;
        }
    }
}
