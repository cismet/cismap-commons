/*
 * HTTPImageRetrieval.java
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
 * Created on 7. August 2006, 15:39
 *
 */
package de.cismet.cismap.commons.rasterservice;

import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JComponent;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class HTTPImageRetrieval extends Thread {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private RetrievalListener listener = null;
    private ImageObserverInterceptor observer;
    Image image = null;
    HttpClient client;
    GetMethod method;
    String url;
    private ByteArrayOutputStream byteArrayOut = null;
    private boolean youngerCall = false;

    /** Creates a new instance of HTTPImageRetrieval */
    public HTTPImageRetrieval(RetrievalListener listener) {
        this.listener = listener;
        client = new HttpClient();
        //client.getHostConfiguration().setProxy("www-proxy.htw-saarland.de", 3128);

        log.debug("proxySet:" + System.getProperty("http.proxyHost"));
        log.debug("ProxyHost:" + System.getProperty("http.proxyHost"));
        log.debug("ProxyPort:" + System.getProperty("http.proxyPort"));



        String proxySet = System.getProperty("proxySet");
        if (proxySet != null && proxySet.equals("true")) {
            log.debug("proxyIs Set");
            log.debug("ProxyHost:" + System.getProperty("http.proxyHost"));
            log.debug("ProxyPort:" + System.getProperty("http.proxyPort"));
            try {
                client.getHostConfiguration().setProxy(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")));
            } catch (Exception e) {
                log.error("Problem while setting proxy", e);
            }
        }
        else {
            log.debug("no proxyIs Set");
        }

    }

    public void run() {
        if (method != null) {
            method.abort();
        }
        method = new GetMethod(url);
        if (!method.isAborted()) {
            try {
                int statusCode = client.executeMethod(method);

                if (statusCode != -1) {
                    log.debug("reading: " + url);
                    InputStream is = method.getResponseBodyAsStream();
                    BufferedInputStream in = new BufferedInputStream(is);
                    byteArrayOut = new ByteArrayOutputStream();
                    int c;

                    while ((c = in.read()) != -1) {
                        byteArrayOut.write(c);
                        if (youngerCall) {
                            fireLoadingAborted();
                            log.debug("interrupt during retrieval");
                            return;
                        }
                    }

                    log.debug("creating image");
                    //Image image =observer.createImage( (ImageProducer) o);
                    observer = new ImageObserverInterceptor();
                    //Image image =Toolkit.getDefaultToolkit().getImage(is);
                    image = Toolkit.getDefaultToolkit().createImage(byteArrayOut.toByteArray());
                    observer.prepareImage(image, observer);
                    while ((observer.checkImage(image, observer) & observer.ALLBITS) != observer.ALLBITS) {
                        Thread.sleep(10);
                        if (youngerCall) {
                            fireLoadingAborted();
                            log.debug("interrupt during assembling");
                            return;
                        }
                    }

                    RetrievalEvent e = new RetrievalEvent();
                    e.setIsComplete(true);
                    e.setRetrievedObject(image);
                    if (!youngerCall) {
                        listener.retrievalComplete(e);
                        log.debug("Retrieval complete");
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;

    }

    public void endRetrieval() {
        if (method != null) {
            method.abort();
        }
        youngerCall = true;
    }

    public void fireLoadingAborted() {
        log.info("Retrieval unterbrochen");
        if (method != null && !method.isAborted()) {
            method.abort();
        }
        image = null;
        observer = null;

        System.gc();
    }

    private class ImageObserverInterceptor extends JComponent {

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
                e.setPercentageDone( (int) (y / (img.getHeight(this) - 1.0) * 100));
                listener.retrievalProgress(e);
            } else if ((infoflags & ImageObserver.ABORT) != 0) {
            } else if ((infoflags & ImageObserver.ERROR) != 0) {
                RetrievalEvent e = new RetrievalEvent();
                e.setHasErrors(true);
                String error = new String(byteArrayOut.toByteArray());
                e.setRetrievedObject(error);
                listener.retrievalError(e);
            }
            return ret;
        }
    }
}
