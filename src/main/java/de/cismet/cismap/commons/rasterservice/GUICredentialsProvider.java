/*
 * GuiCredentialProvider.java
 *
 * Created on 18. Oktober 2006, 11:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.rasterservice;

import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.tools.gui.StaticSwingTools;
import java.awt.Component;
import java.io.IOException;
import java.net.URL;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
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


/**
 *
 * @author Sebastian
 */
public class GUICredentialsProvider extends LoginService implements CredentialsProvider {
    private DefaultUserNameStore  usernames;
    private Preferences appPrefs=null;
    private UsernamePasswordCredentials creds;
    private Component parent;
    private JFrame parentFrame;
    private boolean isAuthenticationDone = false;
    private boolean isAuthenticationCanceled = false;
    private URL url;
    private Object dummy = new Object();
    private String username= null;
    private String title;
    private String prefTitle;
    private CismapBroker broker = CismapBroker.getInstance();
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("de.cismet.cismap.commons.rasterservice.GUICredentialsProvider");//NOI18N
    
    public String getUserName(){
        return creds.getUserName();
    }
    
    public UsernamePasswordCredentials getCredentials(){
        return creds;
    }
    
    public GUICredentialsProvider(URL url) {
        super();
        log.debug("Creating new Credential Provider Instance for URL: "+url.toString());//NOI18N//NOI18N
        this.url = url;
    }
    
    public GUICredentialsProvider(URL url, Component parentComponent) {
        this(url);
        if(parentComponent!=null) {
            this.parent = (StaticSwingTools.getParentFrame(parentComponent));
            if(this.parent==null) {
                this.parent = (StaticSwingTools.getFirstParentFrame(parentComponent));
            }
        }
        
        
    }
    
    
    public void setUsernamePassword(UsernamePasswordCredentials creds){
        this.creds = creds;
    }
    
    public Credentials getCredentials(
            final AuthScheme authscheme,
            final String host,
            int port,
            boolean proxy)
            throws CredentialsNotAvailableException {
        log.debug("Credentials requested for :" + url.toString() + " alias: "+title);//NOI18N
        usernames = new DefaultUserNameStore();
        appPrefs = Preferences.userNodeForPackage(this.getClass());
        usernames.setPreferences(appPrefs.node("loginURLHash"+Integer.toString(url.toString().hashCode())));//NOI18N
        if (creds != null){
            return creds;
        }
        
        synchronized(dummy){
            
            if (creds != null){
                return creds;
            }
            isAuthenticationCanceled=false;
            if (authscheme == null) {
                return null;
            }
            
            if (authscheme instanceof NTLMScheme) {
                requestUsernamePassword();
                
                return creds;
            } else
                if (authscheme instanceof RFC2617Scheme) {
                requestUsernamePassword();
                
                return creds;
                } else {
                throw new CredentialsNotAvailableException("Unsupported authentication scheme: " +//NOI18N
                        authscheme.getSchemeName());
                }
        }
    }
    
    public void requestUsernamePassword() throws CredentialsNotAvailableException{
        JXLoginPane login = new JXLoginPane(this,null,usernames);
        
        String[] names = usernames.getUserNames();
        if(names.length!=0){
            username = names[names.length-1];
        }
        
        login.setUserName(username);
        title = broker.getProperty(url.toString());
        if(title != null){
            //
            login.setMessage(org.openide.util.NbBundle.getMessage(GUICredentialsProvider.class, "GUICredentialsProvider.requestUsernamePassword().login.message", new Object[]{title}));
        } else {
            title = url.toString();
            if (title.startsWith("http://")&& title.length()>21) {//NOI18N
                title=title.substring(7,21)+"...";//NOI18N
            } else if (title.length()>14){
                title=title.substring(0,14)+"...";//NOI18N
            }
            
            login.setMessage(org.openide.util.NbBundle.getMessage(GUICredentialsProvider.class, "GUICredentialsProvider.requestUsernamePassword().login.message", new Object[]{title}));
        }
        log.debug("parentFrame in GUICredentialprovider:"+parent);//NOI18N
        JXLoginPane.JXLoginDialog dialog = new JXLoginPane.JXLoginDialog((JFrame)parent,login);
        
        try {
            ((JXPanel)((JXPanel)login.getComponent(1)).getComponent(1)).getComponent(3).requestFocus();
        } catch (Exception skip) {
            
        }
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        
        if(!isAuthenticationDone){
            isAuthenticationCanceled = true;
            throw new CredentialsNotAvailableException();
        }
    }
    
    public boolean authenticate(String name, char[] password, String server) throws Exception {
        log.debug("Authentication with username: " +name);//NOI18N
        if(testConnection(new UsernamePasswordCredentials(name,new String(password)))){
            log.debug("Credentials are valid for URL: " + url.toString());       //NOI18N
            usernames.removeUserName(name);
            usernames.saveUserNames();
            usernames.addUserName(name);
            usernames.saveUserNames();
            isAuthenticationDone=true;
            setUsernamePassword(new UsernamePasswordCredentials(name,new String(password)));
            return true;
        } else {
            log.debug("Credentials are not valid for URL: " + url.toString());//NOI18N
            return false;
        }
    }
    
    public boolean isAuthenticationCanceled(){
        return isAuthenticationCanceled;
    }
    
    public boolean testConnection(UsernamePasswordCredentials creds){
        HttpClient client = new HttpClient();
        String proxySet = System.getProperty("proxySet");//NOI18N
        if(proxySet != null && proxySet.equals("true")){//NOI18N
            log.debug("proxyIs Set");//NOI18N
            log.debug("ProxyHost:"+System.getProperty("http.proxyHost"));//NOI18N
            log.debug("ProxyPort:"+System.getProperty("http.proxyPort"));//NOI18N
            try {
            client.getHostConfiguration().setProxy(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")));    //NOI18N
            } catch(Exception e){
                log.error("Problem while setting proxy",e);//NOI18N
            }
        }
        GetMethod method = new GetMethod(url.toString());
        client.getState().setCredentials(new AuthScope(url.getHost(),AuthScope.ANY_PORT,AuthScope.ANY_REALM),creds);
        method.setDoAuthentication(true);
        int statuscode =0;
        try {
            statuscode = client.executeMethod(method);
        } catch (IOException ex) {
        }
        if(statuscode == HttpStatus.SC_OK){
            method.releaseConnection();
            return true;
        } else {
            method.releaseConnection();
            usernames.removeUserName(creds.getUserName());
            return false;
        }
    }
    
    public void setTitle(String title){
        this.title = title;
    }
}
