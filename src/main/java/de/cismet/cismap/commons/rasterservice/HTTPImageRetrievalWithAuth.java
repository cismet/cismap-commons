/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.rasterservice;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.auth.NTLMScheme;
import org.apache.commons.httpclient.auth.RFC2617Scheme;
import org.apache.commons.httpclient.methods.GetMethod;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class HTTPImageRetrievalWithAuth extends Thread {

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
    public HTTPImageRetrievalWithAuth(final RetrievalListener listener) {
        this.listener = listener;
        client = new HttpClient();
        // client.getHostConfiguration().setProxy(System.getProperty("proxyHost"),
        // Integer.getInteger(System.getProperty("proxyPort")));
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
        }
        // new
        client.getParams().setParameter(
            CredentialsProvider.PROVIDER,
            new ConsoleAuthPrompter());
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (method != null) {
            method.abort();
        }
        method = new GetMethod(url);
        // new
        method.setDoAuthentication(true);
        if (!method.isAborted()) {
            try {
                final int statusCode = client.executeMethod(method);

                ///new
                if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    System.err.println("Unauthorized: " + method.getStatusLine()); // NOI18N
                    fireAuthenticationFailed();
                    method.releaseConnection();
                    final RetrievalEvent e = new RetrievalEvent();
                    e.setIsComplete(true);
                    listener.retrievalAborted(e);
                } else {
                    if (statusCode != -1) {
                        if (log.isDebugEnabled()) {
                            log.debug("reading: " + url);                          // NOI18N
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
                        while ((observer.checkImage(image, observer) & ImageObserver.ALLBITS)
                                    != ImageObserver.ALLBITS) {
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
    /**
     * new.
     */
    public void fireAuthenticationFailed() {
        log.info("AuthenticationFailed"); // NOI18N
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
    /**
     * /new.
     *
     * @version  $Revision$, $Date$
     */
    public class ConsoleAuthPrompter implements CredentialsProvider {

        //~ Instance fields ----------------------------------------------------

        private UsernamePasswordCredentials creds;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ConsoleAuthPrompter object.
         */
        public ConsoleAuthPrompter() {
            super();
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  creds  DOCUMENT ME!
         */
        public void setUsernamePassword(final UsernamePasswordCredentials creds) {
            this.creds = creds;
        }

        @Override
        public Credentials getCredentials(
                final AuthScheme authscheme,
                final String host,
                final int port,
                final boolean proxy) throws CredentialsNotAvailableException {
            if (authscheme == null) {
                return null;
            }
            try {
                if (authscheme instanceof NTLMScheme) {
                    requestUsernamePassword();
                    return creds;
                } else if (authscheme instanceof RFC2617Scheme) {
                    requestUsernamePassword();
                    return creds;
                } else {
                    throw new CredentialsNotAvailableException("Unna gsupported authentication scheme: " // NOI18N
                                + authscheme.getSchemeName());
                }
            } catch (IOException e) {
                throw new CredentialsNotAvailableException(e.getMessage(), e);
            }
        }

        /**
         * DOCUMENT ME!
         */
        private void requestUsernamePassword() {
//            try {
//                javax.swing.UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
//            } catch (UnsupportedLookAndFeelException ex) {
//                ex.printStackTrace();
//            }

            // TODO determine main frame and insert
            // TODO Cancel KEY
            final JFrame dummy = null;
            final JDialog requestDialog = new JDialog(
                    dummy,
                    org.openide.util.NbBundle.getMessage(
                        HTTPImageRetrievalWithAuth.class,
                        "HTTPImageRetrievalWithAuth.requestUsernamePassword().title"),
                    true);                                                                           // NOI18N
            requestDialog.setLayout(new GridLayout(0, 1));
            requestDialog.setPreferredSize(new Dimension(400, 200));
            final JLabel usernameLabel = new JLabel(org.openide.util.NbBundle.getMessage(
                        HTTPImageRetrievalWithAuth.class,
                        "HTTPImageRetrievalWithAuth.requestUsernamePassword().usernameLabel.text")); // NOI18N
            requestDialog.add(usernameLabel);

            final JTextField usernameField = new JTextField();
            usernameField.setBackground(Color.lightGray);
            requestDialog.add(usernameField);

            final JLabel passwordLabel = new JLabel(org.openide.util.NbBundle.getMessage(
                        HTTPImageRetrievalWithAuth.class,
                        "HTTPImageRetrievalWithAuth.requestUsernamePassword().passwordLabel.text")); // NOI18N
            requestDialog.add(passwordLabel);

            final JPasswordField passwordField = new JPasswordField();
            passwordField.setBackground(Color.lightGray);
            requestDialog.add(passwordField);

            final JButton okButton = new JButton(org.openide.util.NbBundle.getMessage(
                        HTTPImageRetrievalWithAuth.class,
                        "HTTPImageRetrievalWithAuth.requestUsernamePassword().okButton.text")); // NOI18N
            requestDialog.add(okButton);
            okButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        setUsernamePassword(
                            new UsernamePasswordCredentials(
                                usernameField.getText(),
                                new String(passwordField.getPassword())));
                        requestDialog.dispose();
                        // TODO security issue --> cleaning charArray
                    }
                });

            final JButton cancelButton = new JButton("Abbrechen");
            requestDialog.add(cancelButton);
            cancelButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        // setUsernamePassword(new UsernamePasswordCredentials(usernameField.getText(),new
                        // String(passwordField.getPassword()))); method.releaseConnection();
                        requestDialog.dispose();
                        // TODO security issue --> cleaning charArray
                    }
                });

            requestDialog.pack();
            StaticSwingTools.showDialog(requestDialog);
        }
    }
}
