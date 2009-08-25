/*
 * WFSFormsListAndComboBoxModel.java
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
 * version 2.1 of the License, or (at your option) any later version.KA
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
 * Created on 27. Juli 2006, 14:11
 *
 */

package de.cismet.cismap.commons.wfsforms;


import de.cismet.tools.StaticHtmlTools;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeListener;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Priority;
import org.deegree2.model.feature.FeatureCollection;
import org.deegree2.model.feature.FeatureProgressListener;
import org.deegree2.model.feature.GMLFeatureCollectionDocument;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class WFSFormsListAndComboBoxModel extends AbstractListModel implements ComboBoxModel,FeatureProgressListener{
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Vector<WFSFormFeature> features=new Vector<WFSFormFeature>();
    
    FeatureCollection fc = null;
    private Vector<ChangeListener> changeListeners=new Vector<ChangeListener>();
    private Object selectedValue;
    private String loadingMessage=java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("WFSFormListAndComboBoxModel.laden");
    private boolean started=false;
    private boolean finished=false;
    private int estimatedFeatureCount=-1;
    private WFSFormQuery query;
    private JProgressBar progressBar;
    private JComponent comp;
    private int count=0;
    private int max=0;
    private Vector<ActionListener> actionListener=new Vector<ActionListener>();
    /** Creates a new instance of WFSFormsListAndComboBoxModel */
    public WFSFormsListAndComboBoxModel(WFSFormQuery query,JComponent comp,JProgressBar progressBar) throws Exception{
        this(query,null,comp,progressBar);
    }
    
    public WFSFormsListAndComboBoxModel(WFSFormQuery query,final HashMap replacingValues,JComponent comp,JProgressBar progressBar) throws Exception{
        this.progressBar=progressBar;
        this.comp=comp;
        this.query=query;
        
        Thread t=new Thread() {
            public void run() {
                refresh(replacingValues);
            }
        };
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
    
    public void refresh(HashMap replacingValues)  {
//        log.fatal("in refresh() --> EventQueue.isDispatchThread():"+EventQueue.isDispatchThread());
        GMLFeatureCollectionDocument gmlDocument = new GMLFeatureCollectionDocument();
        try {
            if (!started) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        WFSFormsListAndComboBoxModel.this.comp.setEnabled(false);
                    }
                });
                if (WFSFormsListAndComboBoxModel.this.progressBar!=null) {
                    Color visible=WFSFormsListAndComboBoxModel.this.progressBar.getForeground();
                    visible=new Color(visible.getRed(),visible.getGreen(),visible.getBlue(),255);
                    final Color visibleCopy=visible;
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            WFSFormsListAndComboBoxModel.this.progressBar.setForeground(visibleCopy);
                            WFSFormsListAndComboBoxModel.this.progressBar.setVisible(true);
                            WFSFormsListAndComboBoxModel.this.progressBar.setIndeterminate(true);
                        }
                    });
                }
                started=false;
                finished=false;
                HttpClient client = new HttpClient();
                String proxySet = System.getProperty("proxySet");
                if(proxySet != null && proxySet.equals("true")){
                    log.debug("proxyIs Set");
                    log.debug("ProxyHost:"+System.getProperty("http.proxyHost"));
                    log.debug("ProxyPort:"+System.getProperty("http.proxyPort"));
                    try {
                        
                        client.getHostConfiguration().setProxy(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")));
                    } catch(Exception e){
                        log.error("Problem while setting proxy",e);
                    }
                }
                
                PostMethod httppost = new PostMethod(query.getServerUrl());
                //PostMethod httppost = new PostMethod("http://www.heise.de");
                
                String postString=query.getWfsQueryString();
                
                if (replacingValues!=null) {
                    Set keys=replacingValues.keySet();
                    log.debug("replacingValues.keySet()"+replacingValues.keySet());
                    for (Object key:keys) {
                        postString=postString.replaceAll((String)key,(String)replacingValues.get(key));
                    }
                }
                
                log.info("WFS Query:"+StaticHtmlTools.stringToHTMLString(postString));
                String modifiedString= new String(postString.getBytes("UTF-8"), "ISO-8859-1");
                httppost.setRequestEntity(new StringRequestEntity(modifiedString));
                
                try {
                    log.debug("in EDT:" +EventQueue.isDispatchThread());
                    client.executeMethod(httppost);
                    if (httppost.getStatusCode() == HttpStatus.SC_OK) {
                        
                        if (WFSFormsListAndComboBoxModel.this.progressBar!=null) {
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    WFSFormsListAndComboBoxModel.this.progressBar.setIndeterminate(true);
                                }
                            });
                            
                        }
                        
                        log.debug("Start parsing of "+WFSFormsListAndComboBoxModel.this.query.getId());
                        started=true;
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                WFSFormsListAndComboBoxModel.this.fireContentsChanged(WFSFormsListAndComboBoxModel.this,0,0);
                            }
                        });
                        
                        long start = System.currentTimeMillis();
                        
                        //FileReader reader = new FileReader("request");
                        
                        gmlDocument.load(new InputStreamReader(httppost.getResponseBodyAsStream(),Charset.forName("UTF-8")),"http://dummyURL");
                        //gmlDocument.load(new InputStreamReader(new FileInputStream("request"),Charset.forName("iso-8859-1")),"http://dummyURL");
                        gmlDocument.addFeatureProgressListener(this);
                        max = gmlDocument.getFeatureCount();
                        count=0;
                        if (WFSFormsListAndComboBoxModel.this.progressBar!=null) {
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    WFSFormsListAndComboBoxModel.this.progressBar.setIndeterminate(false);
                                    WFSFormsListAndComboBoxModel.this.progressBar.setMaximum(max);
                                    log.debug("Anzahl Feature: "+max);
                                }
                            });
                        }
                        
                        fc = gmlDocument.parse();
                        gmlDocument.removeFeatureProgressListener(this);
                        log.debug("Featurecollection "+fc);
                        for (int i=0; i<fc.size();++i) {
                            features.add(new WFSFormFeature(fc.getFeature(i),query));
                            log.debug(i+":"+features.get(i));
                        }
                        
                        long stop = System.currentTimeMillis();
                        if(log.isEnabledFor(Priority.INFO)) log.info(((stop-start)/1000.0)+" Sekunden dauerte das Parsen");
                        
                        log.debug("Ended parsing of "+WFSFormsListAndComboBoxModel.this.query.getId());
                        finished=true;
                        selectedValue=null;
                        WFSFormsListAndComboBoxModel.this.fireContentsChanged(WFSFormsListAndComboBoxModel.this,0,fc.size()-1);
                        fireActionPerformed(null);
                        if (WFSFormsListAndComboBoxModel.this.progressBar!=null) {
                            Color invisible=WFSFormsListAndComboBoxModel.this.progressBar.getForeground();
                            invisible=new Color(invisible.getRed(),invisible.getGreen(),invisible.getBlue(),0);
                            final Color invisibleCopy=invisible;
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    WFSFormsListAndComboBoxModel.this.progressBar.setForeground(invisibleCopy);
                                }
                            });
                            
                        }
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                WFSFormsListAndComboBoxModel.this.comp.setEnabled(true);
                            }
                        });
                        
                        
                    } else {
                        log.error("Unexpected failure: " + httppost.getStatusLine().toString());
                    }
                } catch (Throwable t) {
                    log.error("Fehler bei POST",t);
                    gmlDocument.removeFeatureProgressListener(this);
                } finally {
                    httppost.releaseConnection();
                }
            }
        } catch (Exception e) {
            log.error("Fehler beim Laden der Features.",e);
            gmlDocument.removeFeatureProgressListener(this);
        }
    }
    
    
    /**
     * Returns the value at the specified index.
     *
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    public Object getElementAt(int index) {
        if (!finished) {
            return loadingMessage;
        } else {
            return features.get(index);
        }
    }
    
    /**
     *
     * Returns the length of the list.
     *
     * @return the length of the list
     */
    public int getSize() {
        if (!finished) {
            //log.debug("Size=0");
            return 0;
        } else {
            //log.debug("Size="+features.size());
            return features.size();
        }
    }
    
    
    
    /**
     *
     * Set the selected item. The implementation of this  method should notify
     * all registered <code>ListDataListener</code>s that the contents
     * have changed.
     *
     *
     * @param anItem the list object to select or <code>null</code>
     *        to clear the selection
     */
    public void setSelectedItem(Object anItem) {
        log.debug("setSelectedItem:"+anItem.getClass() + "::"+anItem);
        selectedValue=anItem;
    }
    
    /**
     *
     * Returns the selected item
     *
     * @return The selected item or <code>null</code> if there is no selection
     */
    public Object getSelectedItem() {
        if (!finished) {
            return java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("WFSFormListAndComboBoxModel.laden");
        } else {
            return selectedValue;
        }
    }
    
    
    private int countFeatures(String s) {
        //do this with jdom, because I don't know how to do it with the sax stuff
        try {
            SAXBuilder builder = new SAXBuilder(false);
            Document doc=builder.build(new StringReader(s));
            Element rootObject=doc.getRootElement();
            return rootObject.getChildren("featureMember", Namespace.getNamespace("http://www.opengis.net/gml")).size();
        } catch (Exception jex) {
            log.warn("error during featurecounting",jex);
            return -1;
        }
        
    }
    
    public int getEstimatedFeatureCount() {
        return estimatedFeatureCount;
    }
    
    public void setEstimatedFeatureCount(int estimatedFeatureCount) {
        this.estimatedFeatureCount = estimatedFeatureCount;
    }
    
    public void featureProgress(int progress) {
        //count +=GMLFeatureCollectionDocument.PROGRESS_CONSTANT;
        WFSFormsListAndComboBoxModel.this.progressBar.setValue(progress);
    }
    
    public void featureLoadingFinished() {
        //WFSFormsListAndComboBoxModel.this.progressBar.setValue(max);
    }
    
    public void featureProgress() {
    }
    
    public void addActionListener(ActionListener a) {
        actionListener.add(a);
    }
    public void removeActionListener(ActionListener a) {
        actionListener.remove(a);
    }
    
    public void fireActionPerformed(ActionEvent e) {
        for (ActionListener a:actionListener) {
            a.actionPerformed(e);
        }
    }
    
    
}
