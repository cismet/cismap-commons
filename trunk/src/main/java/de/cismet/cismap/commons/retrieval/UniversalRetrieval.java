/*
 * UniversalRetrieval.java
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
 * Created on 19. April 2006, 12:16
 *
 */

package de.cismet.cismap.commons.retrieval;

import de.cismet.tools.CismetThreadPool;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import javax.swing.JComponent;

/**
 *
 * @author thorsten.hell@cismet.de
 */
@Deprecated
public class UniversalRetrieval extends AbstractRetrievalService implements  RetrievalListener{
    String url=null;
    
    /** Creates a new instance of UniversalRetrieval */
     public static void main(String[] args) {
           UniversalRetrieval ur=new UniversalRetrieval("http://www.google.de/intl/de_de/images/logo.gif");//NOI18N
           //UniversalRetrieval ur=new UniversalRetrieval("http://www.google.de");
           //UniversalRetrieval ur=new UniversalRetrieval("http://www2.demis.nl/WMS/wms.asp?WMS=WorldMap&WMTVER=1.0.0&request=capabilities");
           ur.retrieve(false);
        }
    public UniversalRetrieval(String url) {
        this.url=url;
    }
    
    public void retrieve(boolean forced) {
        RetrievalThread rt=new RetrievalThread();
        CismetThreadPool.execute(rt);
    }

    public void retrievalStarted(RetrievalEvent e) {
        fireRetrievalStarted(e);
    }

    public void retrievalProgress(RetrievalEvent e) {
        fireRetrievalProgress(e);
    }

    public void retrievalError(RetrievalEvent e) {
        fireRetrievalProgress(e);
    }

    public void retrievalComplete(RetrievalEvent e) {
        fireRetrievalComplete(e);
    }

    public void retrievalAborted(RetrievalEvent e) {
        fireRetrievalAborted(e);
    }

    public Object clone() {
        UniversalRetrieval u=new UniversalRetrieval(url);
        u.listeners=new Vector(listeners);
        return u;
    }
    
    class RetrievalThread extends Thread {
        private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
        private ImageObserverInterceptor observer;
        private RetrievalListener listener=null;
        private ByteArrayOutputStream byteArrayOut=null;
        private URLConnection uc=null;
        private InputStream is=null;
        private Image image=null;
        private boolean youngerCall=false;
        private String contentType="";//NOI18N
        public RetrievalThread() {
            listener=UniversalRetrieval.this;
        }
        public void youngerCall() {
            youngerCall=true;
        }
       
        
        public void run() {
            try {
                log.debug("start of ImageRetrieval");//NOI18N
                listener.retrievalStarted(new RetrievalEvent());
                URL u=new URL(url.toString());
                log.debug("Retrieve: "+url.toString());//NOI18N
                uc=u.openConnection();
                log.debug("contenttype: "+uc.getContentType());//NOI18N
                contentType=uc.getContentType();
                uc.connect();
                
                is=uc.getInputStream();
                
                BufferedInputStream in = new BufferedInputStream(is);
                byteArrayOut = new ByteArrayOutputStream();
                int c;
                while ((c = in.read()) != -1) {
                    byteArrayOut.write(c);
                    if (youngerCall) {
                        fireLoadingAborted();
                        log.debug("interrupt during retrieval");//NOI18N
                        return;
                    }
                }
                if (uc.getContentType().indexOf("image")!=-1) {//NOI18N
                    observer=new ImageObserverInterceptor();
                    //Image image =Toolkit.getDefaultToolkit().getImage(is);
                    image=Toolkit.getDefaultToolkit().createImage(byteArrayOut.toByteArray());
                    observer.prepareImage(image, observer);
                    while ((observer.checkImage(image, observer) & observer.ALLBITS)!= observer.ALLBITS) {
                        Thread.sleep(10);
                        if (youngerCall) {
                            fireLoadingAborted();
                            log.debug("interrupt during assembling");//NOI18N
                            return;
                        }
                    }
                    RetrievalEvent e=new RetrievalEvent();
                    e.setIsComplete(true);
                    e.setContentType(contentType);
                    e.setRetrievedObject(image);
                    if (!youngerCall) {
                        listener.retrievalComplete(e);
                        log.debug("Retrieval complete");//NOI18N
                    } else {
                        fireLoadingAborted();
                    }
                } else if (uc.getContentType().indexOf("text")!=-1) {//NOI18N

                    RetrievalEvent e=new RetrievalEvent();
                    e.setContentType(contentType);
                    e.setIsComplete(true);
                    e.setRetrievedObject(byteArrayOut.toString());
                    listener.retrievalComplete(e);
                }
                else {
                    RetrievalEvent e=new RetrievalEvent();
                    e.setIsComplete(true);
                    e.setContentType(contentType);
                    e.setRetrievedObject(byteArrayOut);
                    listener.retrievalComplete(e);
                }
            } catch ( Exception  e ) {
                log.error(byteArrayOut);
                RetrievalEvent re=new RetrievalEvent();
                re.setIsComplete(false);
                re.setContentType(contentType);
                if (e.getMessage()==null||e.getMessage().equals("null")) {//NOI18N
                    try {
                        String cause=e.getCause().getMessage();
                        re.setRetrievedObject(cause);
                    } catch (Exception ee) {}
                } else {
                    re.setRetrievedObject(e.getMessage());
                }
                listener.retrievalError(re);
                log.error("Fehler beim Laden des Bildes ",e);//NOI18N
            }
        }
        public void fireLoadingAborted(){
//        RetrievalEvent e=new RetrievalEvent();
//        listener.retrievalAborted(e);
            //TODO nochmal anschauen
            log.info("Retrieval interrupted");//NOI18N
            image=null;
            observer=null;
            if (is!=null){
                try {
                    is.close();
                } catch (IOException ioe) {
                    log.warn("Exception during premature closing of the inputstream",ioe);//NOI18N
                }
            }
            System.gc();
        }
        private class ImageObserverInterceptor extends JComponent {
            public boolean imageUpdate(Image img,
                    int infoflags,
                    int x,
                    int y,
                    int width,
                    int height) {
                boolean ret=super.imageUpdate(img,infoflags,x,y,width,height);
//            log.debug("ImageUpdate");
//            log.debug("y "+height);
//            log.debug("img.getHeight"+img.getHeight(this));
                
                
                if ((infoflags&ImageObserver.SOMEBITS) !=0) {
                    RetrievalEvent e=new RetrievalEvent();
                    e.setPercentageDone((int) (y / (img.getHeight(this) - 1.0) * 100));
                    listener.retrievalProgress(e);
                } else if ((infoflags&ImageObserver.ABORT)!=0) {
                    
                } else if ((infoflags&ImageObserver.ERROR)!=0) {
                    RetrievalEvent e=new RetrievalEvent();
                    e.setHasErrors(true);
                    String error=new String(byteArrayOut.toByteArray());
                    e.setRetrievedObject(error);
                    listener.retrievalError(e);
                }
                return ret;
            }
        }
    }
}
