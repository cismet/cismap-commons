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
 * Created on 27. Juni 2005, 14:20
 *
 */

package de.cismet.cismap.commons.rasterservice.java_iio;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class ImageRetrieval extends Thread implements IIOReadProgressListener{
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private String url;
    private RetrievalListener listener=null;
    private ByteArrayOutputStream byteArrayOut=null;
    private boolean youngerCall=false;
    Image image=null;
    ImageReader ir=null;
    /**
     * Creates a new instance of ImageRetrieval 
     */
    public ImageRetrieval(RetrievalListener listener) {
        this.listener=listener;
    }

    public void youngerWMSCall() {
        youngerCall=true;
        if (ir!=null) {
            log.debug("ir.abort();");
            ir.abort();
        }
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public void run() {
        log.debug("IR Thread started");
        image=null;
        ImageInputStream iis=null;
        try {
            URL u=new URL(url.toString()); 
            URLConnection uc=u.openConnection(); 
            String mimeType=uc.getContentType();
            uc.connect();
            InputStream is=uc.getInputStream();
            iis = ImageIO.createImageInputStream(is);
            Iterator it=ImageIO.getImageReadersByMIMEType(mimeType);
            //TODO: Hier kucken ob es \u00FCberhaupt einen Reader gibt
            if (it.hasNext()){
                ir=(ImageReader)it.next();
                ir.setInput(iis, true);
                ir.addIIOReadProgressListener(this);

                image=ir.read(0);
                if (!youngerCall) {
                    RetrievalEvent e=new RetrievalEvent();
                    e.setIsComplete(true);
                    e.setRetrievedObject(image);
                    listener.retrievalComplete(e);
                    log.debug("RetrievalComplete");
                }
            }
            else {
                //Fehler
                BufferedInputStream in = new BufferedInputStream(is);
                ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                int c;
                while ((c = in.read()) != -1) {
                    byteArrayOut.write(c);
                    if (youngerCall) {
                        //fireLoadingAborted();
                        return;
                    }
                }
                RetrievalEvent e=new RetrievalEvent();
                e.setHasErrors(true);
                String error=new String(byteArrayOut.toByteArray());
                e.setRetrievedObject(error);
                listener.retrievalError(e);
                 
                
            }
            
        } catch (Exception e) {
            log.error("Fehler beim Laden des Bildes",e);
        }
            
        
    }
    
    
    
    
    
    
    public void imageComplete(ImageReader source) {
    }
     
    public void imageProgress(ImageReader source, float percentageDone) {
        if (!youngerCall) {
            RetrievalEvent e=new RetrievalEvent();
            e.setPercentageDone(percentageDone);
            listener.retrievalProgress(e);
        }
    }
    
    public void imageStarted(ImageReader source, int imageIndex) {
    }
    
    public void readAborted(ImageReader source) {
    }

    public void sequenceComplete(ImageReader source) {
    }
    
    public void sequenceStarted(ImageReader source, int minIndex) {
    }
    
    public void thumbnailComplete(ImageReader source) {
    }
    
    public void thumbnailProgress(ImageReader source, float percentageDone) {
    }
    
    public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {
    }    
    
    
}
