/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.rasterservice.java_iio;

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

import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class ImageRetrieval extends Thread implements IIOReadProgressListener {

    //~ Instance fields --------------------------------------------------------

    Image image = null;
    ImageReader ir = null;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private String url;
    private RetrievalListener listener = null;
    // private ByteArrayOutputStream byteArrayOut=null;
    private boolean youngerCall = false;

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
        if (ir != null) {
            if (log.isDebugEnabled()) {
                log.debug("ir.abort();"); // NOI18N
            }
            ir.abort();
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

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("IR Thread started"); // NOI18N
        }
        image = null;
        ImageInputStream iis = null;
        try {
            final URL u = new URL(url.toString());
            final URLConnection uc = u.openConnection();
            final String mimeType = uc.getContentType();
            uc.connect();
            final InputStream is = uc.getInputStream();
            iis = ImageIO.createImageInputStream(is);
            final Iterator it = ImageIO.getImageReadersByMIMEType(mimeType);
            // TODO: Hier kucken ob es \u00FCberhaupt einen Reader gibt
            if (it.hasNext()) {
                ir = (ImageReader)it.next();
                ir.setInput(iis, true);
                ir.addIIOReadProgressListener(this);

                image = ir.read(0);
                if (!youngerCall) {
                    final RetrievalEvent e = new RetrievalEvent();
                    e.setIsComplete(true);
                    e.setRetrievedObject(image);
                    listener.retrievalComplete(e);
                    if (log.isDebugEnabled()) {
                        log.debug("RetrievalComplete"); // NOI18N
                    }
                }
            } else {
                // Fehler
                final BufferedInputStream in = new BufferedInputStream(is);
                final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                int c;
                while ((c = in.read()) != -1) {
                    byteArrayOut.write(c);
                    if (youngerCall) {
                        // fireLoadingAborted();
                        return;
                    }
                }
                final RetrievalEvent e = new RetrievalEvent();
                e.setHasErrors(true);
                final String error = new String(byteArrayOut.toByteArray());
                e.setRetrievedObject(error);
                listener.retrievalError(e);
            }
        } catch (Exception e) {
            log.error("Error while loading the image.", e); // NOI18N
        }
    }

    @Override
    public void imageComplete(final ImageReader source) {
    }

    @Override
    public void imageProgress(final ImageReader source, final float percentageDone) {
        if (!youngerCall) {
            final RetrievalEvent e = new RetrievalEvent();
            e.setPercentageDone((int)percentageDone);
            listener.retrievalProgress(e);
        }
    }

    @Override
    public void imageStarted(final ImageReader source, final int imageIndex) {
    }

    @Override
    public void readAborted(final ImageReader source) {
    }

    @Override
    public void sequenceComplete(final ImageReader source) {
    }

    @Override
    public void sequenceStarted(final ImageReader source, final int minIndex) {
    }

    @Override
    public void thumbnailComplete(final ImageReader source) {
    }

    @Override
    public void thumbnailProgress(final ImageReader source, final float percentageDone) {
    }

    @Override
    public void thumbnailStarted(final ImageReader source, final int imageIndex, final int thumbnailIndex) {
    }
}
