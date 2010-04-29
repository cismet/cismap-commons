/*
 * HTTPImageRetrieval.java
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
 * Created on 7. August 2006, 15:39
 *
 */

package de.cismet.cismap.commons.rasterservice;

import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
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

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class HTTPImageRetrievalWithAuth extends Thread{
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private RetrievalListener listener=null;
    private ImageObserverInterceptor observer;
    Image image=null;
    HttpClient client;
    GetMethod method;
    String url;
    private ByteArrayOutputStream byteArrayOut=null;
    private boolean youngerCall=false;
    /** Creates a new instance of HTTPImageRetrieval */
    public HTTPImageRetrievalWithAuth(RetrievalListener listener) {
        this.listener=listener;
        client = new HttpClient();
        //client.getHostConfiguration().setProxy(System.getProperty("proxyHost"), Integer.getInteger(System.getProperty("proxyPort")));
        String proxySet = System.getProperty("proxySet");//NOI18N
        if(proxySet != null && proxySet.equals("true")){//NOI18N
            log.debug("proxyIs Set");//NOI18N
            log.debug("ProxyHost:"+System.getProperty("http.proxyHost"));//NOI18N
            log.debug("ProxyPort:"+System.getProperty("http.proxyPort"));//NOI18N
            try {
            client.getHostConfiguration().setProxy(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort"))); //NOI18N
            } catch(Exception e){
                log.error("Problem while setting proxy",e);//NOI18N
            }
        }
        //new
        client.getParams().setParameter(
            CredentialsProvider.PROVIDER, new ConsoleAuthPrompter());
        
    }

    @Override
    public void run() {
        if (method!=null) {
            method.abort();            
        }
        method = new GetMethod( url );
        //new
        method.setDoAuthentication(true);
        if (!method.isAborted()) {
            try {
                int statusCode = client.executeMethod( method );
                
                ///new 
                if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    System.err.println("Unauthorized: " + method.getStatusLine());//NOI18N
                    fireAuthenticationFailed();
                    method.releaseConnection();
                    RetrievalEvent e = new RetrievalEvent();
                    e.setIsComplete(true);                    
                    listener.retrievalAborted(e);
                } else {
                    
                if( statusCode != -1 ) {
                    log.debug("reading: "+url);//NOI18N
                    InputStream is = method.getResponseBodyAsStream();
                    BufferedInputStream in = new BufferedInputStream( is );
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
                    
                    log.debug("creating image");//NOI18N
                    //Image image =observer.createImage( (ImageProducer) o);
                    observer=new ImageObserverInterceptor();
                    //Image image =Toolkit.getDefaultToolkit().getImage(is);
                    image=Toolkit.getDefaultToolkit().createImage(byteArrayOut.toByteArray());
                    observer.prepareImage(image, observer);
                    while ((observer.checkImage(image, observer) & ImageObserver.ALLBITS)!= ImageObserver.ALLBITS) {
                        Thread.sleep(10);
                        if (youngerCall) {
                            fireLoadingAborted();
                            log.debug("interrupt during assembling");//NOI18N
                            return;
                        }
                    }
                    
                    RetrievalEvent e=new RetrievalEvent();
                    e.setIsComplete(true);
                    e.setRetrievedObject(image);
                    if (!youngerCall) {
                        listener.retrievalComplete(e);
                        log.debug("Retrieval complete");//NOI18N
                    } else {
                        fireLoadingAborted();
                    }
                    method.releaseConnection();
                    
                }
            }
                
                
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
        
    }
    public void endRetrieval() {
        if (method!=null) {
            method.abort();
        }
        youngerCall=true;
    }
    
    
    
    public void fireLoadingAborted(){
        log.info("Retrieval interrupted");//NOI18N
        if (method!=null&&!method.isAborted()) {
            method.abort();
        }
        image=null;
        observer=null;
        
        System.gc();
    }
    
    //new
    public void fireAuthenticationFailed(){
        log.info("AuthenticationFailed");//NOI18N
        if (method!=null&&!method.isAborted()) {
            method.abort();            
        }
        image=null;
        observer=null;        
        System.gc();
    }
    private class ImageObserverInterceptor extends JComponent {
    @Override
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
    ///new
    public class ConsoleAuthPrompter implements CredentialsProvider {

    
        private UsernamePasswordCredentials creds;
        
        public ConsoleAuthPrompter() {
            super();              
        }
        
        
        public void setUsernamePassword(UsernamePasswordCredentials creds){
            this.creds = creds;
        }
        
    @Override
        public Credentials getCredentials(
            final AuthScheme authscheme, 
            final String host, 
            int port, 
            boolean proxy)
            throws CredentialsNotAvailableException 
        {
            if (authscheme == null) {
                return null;
            }
            try{
                if (authscheme instanceof NTLMScheme) {
                    requestUsernamePassword();
                    return creds;    
                } else
                if (authscheme instanceof RFC2617Scheme) {
                    requestUsernamePassword();
                    return creds;
                } else {
                    throw new CredentialsNotAvailableException("Unna gsupported authentication scheme: " +//NOI18N
                        authscheme.getSchemeName());
                }
            } catch (IOException e) {
                throw new CredentialsNotAvailableException(e.getMessage(), e);
            }
        }
        
        private void requestUsernamePassword(){
        
//            try {
//                javax.swing.UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
//            } catch (UnsupportedLookAndFeelException ex) {
//                ex.printStackTrace();
//            }
            
            // TODO determine main frame and insert
            // TODO Cancel KEY
            JFrame dummy = null;            
            final JDialog requestDialog = new JDialog (dummy,org.openide.util.NbBundle.getMessage(HTTPImageRetrievalWithAuth.class, "HTTPImageRetrievalWithAuth.requestUsernamePassword().title"),true);//NOI18N
            requestDialog.setLayout (new GridLayout (0, 1));
            requestDialog.setPreferredSize(new Dimension(400,200));
            JLabel usernameLabel = new JLabel (org.openide.util.NbBundle.getMessage(HTTPImageRetrievalWithAuth.class, "HTTPImageRetrievalWithAuth.requestUsernamePassword().usernameLabel.text"));//NOI18N
            requestDialog.add (usernameLabel);
            
            final JTextField usernameField = new JTextField();
            usernameField.setBackground (Color.lightGray);
            requestDialog.add (usernameField);
            
            JLabel passwordLabel = new JLabel (org.openide.util.NbBundle.getMessage(HTTPImageRetrievalWithAuth.class, "HTTPImageRetrievalWithAuth.requestUsernamePassword().passwordLabel.text"));//NOI18N
            requestDialog.add (passwordLabel);      
            
            final JPasswordField passwordField = new JPasswordField();    
            passwordField.setBackground(Color.lightGray);
            requestDialog.add (passwordField);
            
            JButton okButton = new JButton (org.openide.util.NbBundle.getMessage(HTTPImageRetrievalWithAuth.class, "HTTPImageRetrievalWithAuth.requestUsernamePassword().okButton.text"));//NOI18N
            requestDialog.add (okButton);
            okButton.addActionListener(new ActionListener() 
                                  {
        @Override
                                    public void actionPerformed (ActionEvent e) {
                                    setUsernamePassword(new UsernamePasswordCredentials(usernameField.getText(),new String(passwordField.getPassword())));          
                                    requestDialog.dispose();
                                    //TODO security issue --> cleaning charArray
                                  }
            });
            
            JButton cancelButton = new JButton ("Abbrechen");
            requestDialog.add (cancelButton);
            cancelButton.addActionListener(new ActionListener() 
                                  {
        @Override
                                    public void actionPerformed (ActionEvent e) {
                                    //setUsernamePassword(new UsernamePasswordCredentials(usernameField.getText(),new String(passwordField.getPassword())));          
                                    //method.releaseConnection();
                                    requestDialog.dispose();
                                    //TODO security issue --> cleaning charArray
                                  }
            });
            
            
            requestDialog.pack();
            requestDialog.setVisible(true);
        }   
    }
    
}
