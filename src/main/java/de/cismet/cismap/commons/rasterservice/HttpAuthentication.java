/*
 * HttpAuthentication.java
 *
 * Created on 18. Oktober 2006, 14:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.rasterservice;

import de.cismet.cismap.commons.exceptions.AuthenticationCanceledException;
import de.cismet.cismap.commons.exceptions.BadHttpStatusCodeException;
import de.cismet.cismap.commons.exceptions.CannotReadFromURLException;
import de.cismet.cismap.commons.interaction.CismapBroker;
import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.swing.JComponent;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.methods.GetMethod;
import org.deegree.services.wms.capabilities.WMSCapabilities;

/**
 *
 * @author Sebastian
 */
public class HttpAuthentication {
    
//    //private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
//    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("de.cismet.cismap.commons.rasterservice.HttpAuthentication");
//    
//    /** Creates a new instance of HttpAuthentication */
//    private HttpAuthentication() {
//    }
//    
//    /*@author Sebastian
//     * added 18.10.06 for http authentication support
//     */
//    
//    public static InputStreamReader getInputStreamReaderFromURL(Component parent,URL url,GetMethod method) throws Exception{
//        HttpClient client = new HttpClient();
//        //client.getHostConfiguration().setProxy(System.getProperty("proxyHost"), Integer.getInteger(System.getProperty("proxyPort")));
//        //client.getHostConfiguration().setProxy("www-proxy.htw-saarland.de", 3128);        
//        //client.getHostConfiguration().setProxy(System.getProperty("proxyHost"), Integer.getInteger(System.getProperty("proxyPort")));
//        
//        String proxySet = System.getProperty("proxySet");
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
//        log.debug("Trying to receive InputStreamReader from URL: "+url.toString());
//        CismapBroker broker = CismapBroker.getInstance();
//        //GetMethod method = new GetMethod(url.toString());
//        method.setURI(new URI(url.toString(),false,null));
//        
//        GUICredentialsProvider cp = broker.getHttpCredentialProviderURL(url);
//        log.debug("Retrieving Credential Provider for url: "+url.toString());
//        if(cp != null){
//            log.debug("Credential Provider available for ... " + url.toString());
//            client.getParams().setParameter(CredentialsProvider.PROVIDER,cp);
//        } else {
//            cp = broker.createSynchronizedCP(url,parent);
//            client.getParams().setParameter(CredentialsProvider.PROVIDER,cp);
//        }
//        
//        method.setDoAuthentication(true);
//        int statuscode;
//        boolean isRequesting = true;
//        
//        while(isRequesting){
//            log.debug("Executing GET for ... " + url.toString());
//            statuscode = client.executeMethod(method);
//            
//            if(cp.isAuthenticationCanceled()){
//                log.debug("User has canceled the Authorization for ... "+url.toString());
//                throw new AuthenticationCanceledException();
//            }
//            switch (statuscode){
//                case(HttpStatus.SC_UNAUTHORIZED):
//                    log.debug("Server response: credentials are not valid (statuscode 401) for ... "+url.toString());
//                    continue;
//                case(HttpStatus.SC_OK):
//                    log.debug("server response:  ok (statuscode 200) for ... "+url.toString());
//                    return new InputStreamReader(new BufferedInputStream(method.getResponseBodyAsStream()));
//                default:
//                    log.error("Statuscode is unknown and not implemented for ... "+url.toString() +" statuscode: "+statuscode);
//                    throw new BadHttpStatusCodeException("Bad statuscode from server",statuscode);
//            }
//        }
//        log.error("Unable to read form URL :"+url.toString());
//        throw new CannotReadFromURLException("Error during retrieving data from URL");
//    }
//    
//    public static InputStreamReader getInputStreamReaderFromURL(Component parent,URL url) throws Exception{
//        return getInputStreamReaderFromURL(parent,url,new GetMethod());
//    }
//    
//    
//    
//    public static BufferedInputStream getBufferedInputStreamFromCapabilities(Component parent,WMSCapabilities cap,URL url,GetMethod method) throws Exception{
//        HttpClient client = new HttpClient();
//        //client.getHostConfiguration().setProxy("www-proxy.htw-saarland.de", 3128);
//        //client.getHostConfiguration().setProxy(System.getProperty("proxyHost"), Integer.getInteger(System.getProperty("proxyPort")));
//        String proxySet = System.getProperty("proxySet");
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
//        log.debug("Trying to receive BufferedInputStream from URL: "+url.toString()+" with capabilities");
//        CismapBroker broker = CismapBroker.getInstance();
//        GUICredentialsProvider cp;
//        cp = broker.getHttpCredentialProviderCapabilities(cap);
//        log.debug("Retrieving Credential Provider for url: "+url.toString());
//        //GetMethod method = new GetMethod(url.toString());
//        method.setURI(new URI(url.toString(),false,null));
//        //if(cp.isAuthenticationCanceled())throw new AuthenticationCanceledException();
//        if(cp != null){
//            log.debug("Credential Provider available for ... " + url.toString());
//            client.getParams().setParameter(CredentialsProvider.PROVIDER,cp);
//        } else {
//            cp = broker.createSynchronizedCP(url,parent,cap);
//            client.getParams().setParameter(CredentialsProvider.PROVIDER,cp);
//        }
//        
//        
//        method.setDoAuthentication(true);
//        int statuscode;
//        boolean isRequesting = true;
//        
//        while(isRequesting){
//            log.debug("Executing GET for ... " + url.toString());
//            statuscode = client.executeMethod(method);
//            if(cp.isAuthenticationCanceled()){
//                log.debug("User has canceled the Authorization for ... "+url.toString());
//                throw new AuthenticationCanceledException();
//            }
//            switch (statuscode){
//                case(HttpStatus.SC_UNAUTHORIZED):
//                    log.debug("Server response: credentials are not valid (statuscode 401) for ... "+url.toString());
//                    continue;
//                case(HttpStatus.SC_OK):
//                    log.debug("Server response:  ok (statuscode 200) for ... "+url.toString());
//                    return new BufferedInputStream(method.getResponseBodyAsStream());
//                default:
//                    log.error("Statuscode is unknown and not implemented for ... "+url.toString() +" statuscode: "+statuscode);
//                    throw new BadHttpStatusCodeException("Bad statuscode from server",statuscode);
//            }
//        }
//        log.error("Unable to read form URL :"+url.toString());
//        throw new CannotReadFromURLException("Error during retrieving data from URL");
//        
//    }
//    
//    /*@author Sebastian
//     * added 18.10.06 for http authentication support
//     */
//    public static BufferedInputStream getBufferedInputStreamFromCapabilities(Component parent,WMSCapabilities cap,URL url) throws Exception{
//        return getBufferedInputStreamFromCapabilities(parent,cap,url,new GetMethod());
//    }
//    
//    public static BufferedInputStream getBufferedInputStreamFromCapabilities(WMSCapabilities cap,URL url,GetMethod method) throws Exception{
//        HttpClient client = new HttpClient();
//        String proxySet = System.getProperty("proxySet");
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
//        log.debug("Trying to receive BufferedInputStream from URL: "+url.toString()+" with capabilities");
//        
//        CismapBroker broker = CismapBroker.getInstance();
//        GUICredentialsProvider cp;
//        cp = broker.getHttpCredentialProviderCapabilities(cap);
//        log.debug("Retrieving Credential Provider for capability: "+cap.getCapability().getLayer().getTitle() +" (url: "+url.toString()+")");
//        //GetMethod method = new GetMethod(url.toString());
//        method.setURI(new URI(url.toString(),true,null));
//        //if(cp.isAuthenticationCanceled())throw new AuthenticationCanceledException();
//        if(cp != null){
//            log.debug("Credential Provider available for ... " + cap.getCapability().getLayer().getTitle());
//            client.getParams().setParameter(CredentialsProvider.PROVIDER,cp);
//        } else {
//            cp = broker.createSynchronizedCP(url,cap);
//            client.getParams().setParameter(CredentialsProvider.PROVIDER,cp);
//        }
//        
//        
//        method.setDoAuthentication(true);
//        int statuscode;
//        boolean isRequesting = true;
//        
//        while(isRequesting){
//            log.debug("Executing GET for ... " + url.toString());
//            statuscode = client.executeMethod(method);
//            if(cp.isAuthenticationCanceled()){
//                log.debug("User has canceled the Authorization for ... "+url.toString());
//                throw new AuthenticationCanceledException();
//            }
//            switch (statuscode){
//                case(HttpStatus.SC_UNAUTHORIZED):
//                    log.debug("Server response: credentials are not valid (statuscode 401) for ... "+url.toString());
//                    continue;
//                case(HttpStatus.SC_OK):
//                    log.debug("Server response:  ok (statuscode 200) for ... "+url.toString());
//                    return new BufferedInputStream(method.getResponseBodyAsStream());
//                default:
//                    log.error("Statuscode is unknown and not implemented for ... "+url.toString() +" statuscode: "+statuscode);
//                    throw new BadHttpStatusCodeException("Bad statuscode from server",statuscode);
//            }
//        }
//        log.error("Unable to read form URL :"+url.toString());
//        throw new CannotReadFromURLException("Error during retrieving data from URL");
//    }
//    
//    public static BufferedInputStream getBufferedInputStreamFromCapabilities(WMSCapabilities cap,URL url) throws Exception{
//        return getBufferedInputStreamFromCapabilities(cap,url,new GetMethod());
//    }
//    
//        public static BufferedInputStream getBufferedInputStreamFromURL(URL url,GetMethod method) throws Exception{
//        HttpClient client = new HttpClient();
//        String proxySet = System.getProperty("proxySet");
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
//            CismapBroker broker = CismapBroker.getInstance();
//        GUICredentialsProvider cp;
//        cp = broker.getHttpCredentialProviderURL(url);
//        //GetMethod method = new GetMethod(url.toString());
//        method.setURI(new URI(url.toString(),false,null));
//        //if(cp.isAuthenticationCanceled())throw new AuthenticationCanceledException();
//        if(cp != null){
//            client.getParams().setParameter(CredentialsProvider.PROVIDER,cp);
//        } else {
//            cp = broker.createSynchronizedCP(url);
//            client.getParams().setParameter(CredentialsProvider.PROVIDER,cp);
//        }
//        
//        method.setDoAuthentication(true);
//        int statuscode;
//        boolean isRequesting = true;
//        
//        //TODO ugly never used ?!
//        while(isRequesting){
//            statuscode = client.executeMethod(method);
//            if(cp.isAuthenticationCanceled())throw new AuthenticationCanceledException();
//            switch (statuscode){
//                case(HttpStatus.SC_UNAUTHORIZED):
//                    continue;
//                case(HttpStatus.SC_OK):
//                    return new BufferedInputStream(method.getResponseBodyAsStream());
//                default:
//                    throw new BadHttpStatusCodeException("Bad statuscode from server",statuscode);
//            }
//        }
//        throw new CannotReadFromURLException("Error during retrieving data from URL");
//        }
//    
//    public static BufferedInputStream getBufferedInputStreamFromURL(URL url) throws Exception{
//        return getBufferedInputStreamFromURL(url,new GetMethod());
//    }
}
