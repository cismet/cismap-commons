/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * GuiCredentialProvider.java
 *
 * Created on 18. Oktober 2006, 11:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.rasterservice;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.auth.NTLMScheme;
import org.apache.commons.httpclient.auth.RFC2617Scheme;
import org.apache.commons.httpclient.methods.GetMethod;

import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.auth.DefaultUserNameStore;
import org.jdesktop.swingx.auth.LoginService;

import java.awt.Component;

import java.io.IOException;

import java.net.URL;

import java.util.prefs.Preferences;

import javax.swing.JFrame;

import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   Sebastian
 * @version  $Revision$, $Date$
 */
public class GUICredentialsProvider extends LoginService implements CredentialsProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            "de.cismet.cismap.commons.rasterservice.GUICredentialsProvider"); // NOI18N

    //~ Instance fields --------------------------------------------------------

    private DefaultUserNameStore usernames;
    private Preferences appPrefs = null;
    private UsernamePasswordCredentials creds;
    private Component parent;
    private JFrame parentFrame;
    private boolean isAuthenticationDone = false;
    private boolean isAuthenticationCanceled = false;
    private URL url;
    private Object dummy = new Object();
    private String username = null;
    private String title;
    private String prefTitle;
    private CismapBroker broker = CismapBroker.getInstance();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GUICredentialsProvider object.
     *
     * @param  url  DOCUMENT ME!
     */
    public GUICredentialsProvider(final URL url) {
        super();
        if (log.isDebugEnabled()) {
            log.debug("Creating new Credential Provider Instance for URL: " + url.toString()); // NOI18N//NOI18N
        }
        this.url = url;
    }

    /**
     * Creates a new GUICredentialsProvider object.
     *
     * @param  url              DOCUMENT ME!
     * @param  parentComponent  DOCUMENT ME!
     */
    public GUICredentialsProvider(final URL url, final Component parentComponent) {
        this(url);
        if (parentComponent != null) {
            this.parent = (StaticSwingTools.getParentFrame(parentComponent));
            if (this.parent == null) {
                this.parent = (StaticSwingTools.getFirstParentFrame(parentComponent));
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getUserName() {
        return creds.getUserName();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public UsernamePasswordCredentials getCredentials() {
        return creds;
    }

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
        if (log.isDebugEnabled()) {
            log.debug("Credentials requested for :" + url.toString() + " alias: " + title);                    // NOI18N
        }
        usernames = new DefaultUserNameStore();
        appPrefs = Preferences.userNodeForPackage(this.getClass());
        usernames.setPreferences(appPrefs.node("loginURLHash" + Integer.toString(url.toString().hashCode()))); // NOI18N
        if (creds != null) {
            return creds;
        }

        synchronized (dummy) {
            if (creds != null) {
                return creds;
            }
            isAuthenticationCanceled = false;
            if (authscheme == null) {
                return null;
            }

            if (authscheme instanceof NTLMScheme) {
                requestUsernamePassword();

                return creds;
            } else if (authscheme instanceof RFC2617Scheme) {
                requestUsernamePassword();

                return creds;
            } else {
                throw new CredentialsNotAvailableException("Unsupported authentication scheme: " // NOI18N
                            + authscheme.getSchemeName());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  CredentialsNotAvailableException  DOCUMENT ME!
     */
    public void requestUsernamePassword() throws CredentialsNotAvailableException {
        final JXLoginPane login = new JXLoginPane(this, null, usernames);

        final String[] names = usernames.getUserNames();
        if (names.length != 0) {
            username = names[names.length - 1];
        }

        login.setUserName(username);
        title = broker.getProperty(url.toString());
        if (title != null) {
            //
            login.setMessage(org.openide.util.NbBundle.getMessage(
                    GUICredentialsProvider.class,
                    "GUICredentialsProvider.requestUsernamePassword().login.message",
                    new Object[] { title }));                           // NOI18N
        } else {
            title = url.toString();
            if (title.startsWith("http://") && (title.length() > 21)) { // NOI18N
                title = title.substring(7, 21) + "...";                 // NOI18N
            } else if (title.length() > 14) {
                title = title.substring(0, 14) + "...";                 // NOI18N
            }

            login.setMessage(org.openide.util.NbBundle.getMessage(
                    GUICredentialsProvider.class,
                    "GUICredentialsProvider.requestUsernamePassword().login.message",
                    new Object[] { title }));                            // NOI18N
        }
        if (log.isDebugEnabled()) {
            log.debug("parentFrame in GUICredentialprovider:" + parent); // NOI18N
        }
        final JXLoginPane.JXLoginDialog dialog = new JXLoginPane.JXLoginDialog((JFrame)parent, login);

        try {
            ((JXPanel)((JXPanel)login.getComponent(1)).getComponent(1)).getComponent(3).requestFocus();
        } catch (Exception skip) {
        }
        dialog.setAlwaysOnTop(true);
        StaticSwingTools.showDialog(dialog);

        if (!isAuthenticationDone) {
            isAuthenticationCanceled = true;
            throw new CredentialsNotAvailableException();
        }
    }

    @Override
    public boolean authenticate(final String name, final char[] password, final String server) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Authentication with username: " + name);                    // NOI18N
        }
        if (testConnection(new UsernamePasswordCredentials(name, new String(password)))) {
            if (log.isDebugEnabled()) {
                log.debug("Credentials are valid for URL: " + url.toString());     // NOI18N
            }
            usernames.removeUserName(name);
            usernames.saveUserNames();
            usernames.addUserName(name);
            usernames.saveUserNames();
            isAuthenticationDone = true;
            setUsernamePassword(new UsernamePasswordCredentials(name, new String(password)));
            return true;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Credentials are not valid for URL: " + url.toString()); // NOI18N
            }
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isAuthenticationCanceled() {
        return isAuthenticationCanceled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   creds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean testConnection(final UsernamePasswordCredentials creds) {
        final HttpClient client = new HttpClient();
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
        final GetMethod method = new GetMethod(url.toString());
        client.getState().setCredentials(new AuthScope(url.getHost(), AuthScope.ANY_PORT, AuthScope.ANY_REALM), creds);
        method.setDoAuthentication(true);
        int statuscode = 0;
        try {
            statuscode = client.executeMethod(method);
        } catch (IOException ex) {
        }
        if (statuscode == HttpStatus.SC_OK) {
            method.releaseConnection();
            return true;
        } else {
            method.releaseConnection();
            usernames.removeUserName(creds.getUserName());
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  title  DOCUMENT ME!
     */
    public void setTitle(final String title) {
        this.title = title;
    }
}
