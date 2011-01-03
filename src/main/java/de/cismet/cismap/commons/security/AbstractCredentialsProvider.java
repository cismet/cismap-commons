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
package de.cismet.cismap.commons.security;

import org.jdesktop.swingx.auth.LoginService;

/**
 * DOCUMENT ME!
 *
 * @author   Sebastian
 * @version  $Revision$, $Date$
 */
public abstract class AbstractCredentialsProvider extends LoginService {

//    protected DefaultUserNameStore  usernames;
//    protected Preferences appPrefs=null;
//    protected UsernamePasswordCredentials creds;
//    private Component parent;
//    private JFrame parentFrame;
//    private boolean isAuthenticationDone = false;
//    protected  boolean isAuthenticationCanceled = false;
//    protected URL url;
//    protected Object dummy = new Object();
//    private String username= null;
//    protected  String title;
//    private String prefTitle;
//    private CismapBroker broker = CismapBroker.getInstance();
//    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("de.cismet.cismap.commons.rasterservice.GUICredentialsProvider");
//
//
//    public String getUserName(){
//        return creds.getUserName();
//    }
//
//    public boolean areCredentialsAvailable(){
//        return creds != null;
//    }
//
//    public AbstractCredentialsProvider(URL url) {
//        super();
//        log.debug("Creating new Credential Provider Instance for URL: "+url.toString());
//        this.url = url;
//    }
//
//    public AbstractCredentialsProvider(URL url, Component parentComponent) {
//        this(url);
//        if(parentComponent!=null) {
//            this.parent = (StaticSwingTools.getParentFrame(parentComponent));
//            if(this.parent==null) {
//                this.parent = (StaticSwingTools.getFirstParentFrame(parentComponent));
//            }
//        }
//
//
//    }
//
//
//    public void setUsernamePassword(UsernamePasswordCredentials creds){
//        this.creds = creds;
//    }
//
//
//    private void requestUsernamePassword() throws CredentialsNotAvailableException{
//        JXLoginPanel login = new JXLoginPanel(this,null,usernames);
//
//        String[] names = usernames.getUserNames();
//        if(names.length!=0){
//            username = names[names.length-1];
//        }
//
//        login.setUserName(username);
//        title = broker.getProperty(url.toString());
//        if(title != null){
//            login.setMessage(java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("GUICredentialProvider.HttpAuthentication.Messagetext_1")+
//                    " \""+ title +"\" "
//                    );
//        } else {
//            title = url.toString();
//            if (title.startsWith("http://")&& title.length()>21) {
//                title=title.substring(7,21)+"...";
//            } else if (title.length()>14){
//                title=title.substring(0,14)+"...";
//            }
//
//            login.setMessage(java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("GUICredentialProvider.HttpAuthentication.Messagetext_1")+
//                    "\n"+
//                    " \""+ title +"\" "
//                    );
//        }
//        log.debug("parentFrame in GUICredentialprovider:"+parent);
//        JXLoginPanel.JXLoginDialog dialog = new JXLoginPanel.JXLoginDialog((JFrame)parent,login);
//
//        try {
//            ((JXPanel)((JXPanel)login.getComponent(1)).getComponent(1)).getComponent(3).requestFocus();
//        } catch (Exception skip) {
//
//        }
//        dialog.setAlwaysOnTop(true);
//        dialog.setVisible(true);
//
//        if(!isAuthenticationDone){
//            isAuthenticationCanceled = true;
//            throw new CredentialsNotAvailableException();
//        }
//    }
//
//    public boolean authenticate(String name, char[] password, String server) throws Exception {
//        log.debug("Authentication with username: " +name);
//        if(testConnection(new UsernamePasswordCredentials(name,new String(password)))){
//            log.debug("Credentials are valid for URL: " + url.toString());
//            usernames.removeUserName(name);
//            usernames.saveUserNames();
//            usernames.addUserName(name);
//            usernames.saveUserNames();
//            isAuthenticationDone=true;
//            setUsernamePassword(new UsernamePasswordCredentials(name,new String(password)));
//            return true;
//        } else {
//            log.debug("Credentials are not valid for URL: " + url.toString());
//            return false;
//        }
//    }
//
//    public boolean isAuthenticationCanceled(){
//        return isAuthenticationCanceled;
//    }
//
//    public abstract boolean testConnection(UsernamePasswordCredentials creds);
//
//    public void setTitle(String title){
//        this.title = title;
//    }
//
//    public Credentials getCredentials() throws CredentialsNotAvailableException{
//        log.debug("Credentials requested for :" + url.toString() + " alias: "+title);
//        usernames = new DefaultUserNameStore();
//        appPrefs = Preferences.userNodeForPackage(this.getClass());
//        usernames.setPreferences(appPrefs.node("loginURLHash"+Integer.toString(url.toString().hashCode())));
//        requestUsernamePassword();
//        return creds;
//    }
}
