/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.retrieval;

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

import de.cismet.tools.CismetThreadPool;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
@Deprecated
public class UniversalRetrieval extends AbstractRetrievalService implements RetrievalListener {

    //~ Instance fields --------------------------------------------------------

    String url = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UniversalRetrieval object.
     *
     * @param  url  DOCUMENT ME!
     */
    public UniversalRetrieval(final String url) {
        this.url = url;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates a new instance of UniversalRetrieval.
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final UniversalRetrieval ur = new UniversalRetrieval("http://www.google.de/intl/de_de/images/logo.gif"); // NOI18N
        // UniversalRetrieval ur=new UniversalRetrieval("http://www.google.de"); UniversalRetrieval ur=new
        // UniversalRetrieval("http://www2.demis.nl/WMS/wms.asp?WMS=WorldMap&WMTVER=1.0.0&request=capabilities");
        ur.retrieve(false);
    }

    @Override
    public void retrieve(final boolean forced) {
        final RetrievalThread rt = new RetrievalThread();
        CismetThreadPool.execute(rt);
    }

    @Override
    public void retrievalStarted(final RetrievalEvent e) {
        fireRetrievalStarted(e);
    }

    @Override
    public void retrievalProgress(final RetrievalEvent e) {
        fireRetrievalProgress(e);
    }

    @Override
    public void retrievalError(final RetrievalEvent e) {
        fireRetrievalProgress(e);
    }

    @Override
    public void retrievalComplete(final RetrievalEvent e) {
        fireRetrievalComplete(e);
    }

    @Override
    public void retrievalAborted(final RetrievalEvent e) {
        fireRetrievalAborted(e);
    }

    @Override
    public Object clone() {
        final UniversalRetrieval u = new UniversalRetrieval(url);
        u.listeners = new Vector(listeners);
        return u;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class RetrievalThread extends Thread {

        //~ Instance fields ----------------------------------------------------

        private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
        private ImageObserverInterceptor observer;
        private RetrievalListener listener = null;
        private ByteArrayOutputStream byteArrayOut = null;
        private URLConnection uc = null;
        private InputStream is = null;
        private Image image = null;
        private boolean youngerCall = false;
        private String contentType = ""; // NOI18N

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RetrievalThread object.
         */
        public RetrievalThread() {
            super("RetrievalThread");
            listener = UniversalRetrieval.this;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        public void youngerCall() {
            youngerCall = true;
        }

        @Override
        public void run() {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("start of ImageRetrieval");             // NOI18N
                }
                listener.retrievalStarted(new RetrievalEvent());
                final URL u = new URL(url.toString());
                if (log.isDebugEnabled()) {
                    log.debug("Retrieve: " + url.toString());         // NOI18N
                }
                uc = u.openConnection();
                if (log.isDebugEnabled()) {
                    log.debug("contenttype: " + uc.getContentType()); // NOI18N
                }
                contentType = uc.getContentType();
                uc.connect();

                is = uc.getInputStream();

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
                if (uc.getContentType().indexOf("image") != -1) {    // NOI18N
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
                    e.setContentType(contentType);
                    e.setRetrievedObject(image);
                    if (!youngerCall) {
                        listener.retrievalComplete(e);
                        if (log.isDebugEnabled()) {
                            log.debug("Retrieval complete");              // NOI18N
                        }
                    } else {
                        fireLoadingAborted();
                    }
                } else if (uc.getContentType().indexOf("text") != -1) {   // NOI18N

                    final RetrievalEvent e = new RetrievalEvent();
                    e.setContentType(contentType);
                    e.setIsComplete(true);
                    e.setRetrievedObject(byteArrayOut.toString());
                    listener.retrievalComplete(e);
                } else {
                    final RetrievalEvent e = new RetrievalEvent();
                    e.setIsComplete(true);
                    e.setContentType(contentType);
                    e.setRetrievedObject(byteArrayOut);
                    listener.retrievalComplete(e);
                }
            } catch (Exception e) {
                log.error(byteArrayOut);
                final RetrievalEvent re = new RetrievalEvent();
                re.setIsComplete(false);
                re.setContentType(contentType);
                if ((e.getMessage() == null) || e.getMessage().equals("null")) { // NOI18N
                    try {
                        final String cause = e.getCause().getMessage();
                        re.setRetrievedObject(cause);
                    } catch (Exception ee) {
                    }
                } else {
                    re.setRetrievedObject(e.getMessage());
                }
                listener.retrievalError(re);
                log.error("Fehler beim Laden des Bildes ", e);                   // NOI18N
            }
        }
        /**
         * DOCUMENT ME!
         */
        public void fireLoadingAborted() {
//        RetrievalEvent e=new RetrievalEvent();
//        listener.retrievalAborted(e);
            // TODO nochmal anschauen
            log.info("Retrieval interrupted");                                              // NOI18N
            image = null;
            observer = null;
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    log.warn("Exception during premature closing of the inputstream", ioe); // NOI18N
                }
            }
            System.gc();
        }

        //~ Inner Classes ------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @version  $Revision$, $Date$
         */
        private class ImageObserverInterceptor extends JComponent {

            //~ Methods --------------------------------------------------------

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
}
