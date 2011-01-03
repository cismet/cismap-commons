/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.rasterservice;

/**
 * DOCUMENT ME!
 *
 * @author   spuhl
 * @version  $Revision$, $Date$
 */
//public class HTTPCredentialProvider extends AbstractCredentialsProvider implements CredentialsProvider{
public class HTTPCredentialProvider {

//    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("de.cismet.cismap.commons.rasterservice.GUICredentialsProvider");
//
//    public HTTPCredentialProvider(URL url, Component parentComponent) {
//        super(url, parentComponent);
//    }
//
//    public HTTPCredentialProvider(URL url) {
//        super(url);
//    }
//
//    public Credentials getCredentials(
//            final AuthScheme authscheme,
//            final String host,
//            int port,
//            boolean proxy)
//            throws CredentialsNotAvailableException {
//
//        if (creds != null){
//            return creds;
//        }
//
//        synchronized(dummy){
//
//            if (creds != null){
//                return creds;
//            }
//            isAuthenticationCanceled=false;
//            if (authscheme == null) {
//                return null;
//            }
//
//            if (authscheme instanceof NTLMScheme) {
//
//
//                return getCredentials();
//            } else
//                if (authscheme instanceof RFC2617Scheme) {
//                return getCredentials();
//                } else {
//                throw new CredentialsNotAvailableException("Unsupported authentication scheme: " +
//                        authscheme.getSchemeName());
//                }
//        }
//    }
//
//    public boolean testConnection(UsernamePasswordCredentials creds){
//        HttpClient client = new HttpClient();
//        String proxySet = System.getProperty("proxySet");
//        //ToDo proxyauslagern siehe HTTPCredentialProvider
//        if(proxySet != null && proxySet.equals("true")){
//            log.debug("proxyIs Set");
//            log.debug("ProxyHost:"+System.getProperty("http.proxyHost"));
//            log.debug("ProxyPort:"+System.getProperty("http.proxyPort"));
//            try {
//            client.getHostConfiguration().setProxy(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")));
//            } catch(Exception e){
//                log.error("Problem while setting proxy",e);
//            }
//        }
//        GetMethod method = new GetMethod(url.toString());
//        client.getState().setCredentials(new AuthScope(url.getHost(),AuthScope.ANY_PORT,AuthScope.ANY_REALM),creds);
//        method.setDoAuthentication(true);
//        int statuscode =0;
//        try {
//            statuscode = client.executeMethod(method);
//        } catch (IOException ex) {
//        }
//        if(statuscode == HttpStatus.SC_OK){
//            method.releaseConnection();
//            return true;
//        } else {
//            method.releaseConnection();
//            usernames.removeUserName(creds.getUserName());
//            return false;
//        }
//    }

}
