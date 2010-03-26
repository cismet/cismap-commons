/*
 * WFSFormFactory.java
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
 * Created on 1. August 2006, 09:43
 *
 */
package de.cismet.cismap.commons.wfsforms;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.tools.configuration.Configurable;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;
import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class WFSFormFactory implements Configurable {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private LinkedHashMap<String, AbstractWFSForm> forms = new LinkedHashMap<String, AbstractWFSForm>();
    private static WFSFormFactory singletonInstance;
    /** Creates a new instance of WFSFormFactory */
    private Element configuration;
    private MappingComponent mappingComponent;
    private boolean problemDuringSorting = false;

    private WFSFormFactory(MappingComponent map) {
        mappingComponent = map;
    }

    public static WFSFormFactory getInstance(MappingComponent map) {
        if (singletonInstance == null) {
            singletonInstance = new WFSFormFactory(map);
        }
        return singletonInstance;
    }

    public static WFSFormFactory getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new WFSFormFactory(null);
        }
        return singletonInstance;
    }

    public Element getConfiguration() {
        Element ret = new Element("cismapWFSFormsPreferences");//NOI18N
//        Set<String> keySet=forms.keySet();
//        for (String key:keySet) {
//            Element form=forms.get(key).getElement();
//            ret.addContent(form);
//        }
        return ret;
    }

    public void masterConfigure(Element parent) {
        forms.clear();
        try {
            configuration = ((Element) parent.clone()).getChild("cismapWFSFormsPreferences").detach();//NOI18N
            List list = configuration.getChildren("wfsForm");//NOI18N
            for (Object o : list) {
                try {
                    Element e = (Element) o;
                    log.debug("Try to create WFSForm: " + e.getContent());//NOI18N
                    String className = e.getAttribute("className").getValue();//NOI18N
                    Class formClass = Class.forName(className);
                    Constructor constructor = formClass.getConstructor();
                    AbstractWFSForm form = (AbstractWFSForm) constructor.newInstance();
                    form.setClassName(className);
                    form.setId(e.getAttribute("id").getValue());//NOI18N
                    try {
                        form.setSorter(e.getAttribute("sorter").getValue());//NOI18N
                    } catch (Exception skip) {
                    }
                    form.setTitle(e.getAttribute("title").getValue());//NOI18N
                    form.setMenuString(e.getAttribute("menu").getValue());//NOI18N
                    form.setIconPath(e.getAttribute("icon").getValue());//NOI18N
                    form.setIcon(new javax.swing.ImageIcon(getClass().getResource(e.getAttribute("icon").getValue())));//NOI18N
                    Vector<WFSFormQuery> queryVector = new Vector<WFSFormQuery>();
                    List queries = e.getChildren("wfsFormQuery");//NOI18N
                    for (Object oq : queries) {
                        Element q = (Element) oq;
                        WFSFormQuery query = new WFSFormQuery();
                        query.setComponentName(q.getAttribute("componentName").getValue());//NOI18N
                        query.setDisplayTextProperty(q.getAttribute("displayTextProperty").getValue());//NOI18N
                        query.setExtentProperty(q.getAttribute("extentProperty").getValue());//NOI18N
                        query.setFilename(q.getAttribute("queryFile").getValue());//NOI18N
                        query.setWfsQueryString(readFileFromClassPathAsString(query.getFilename()));
                        query.setId(q.getAttribute("id").getValue());//NOI18N
                        query.setIdProperty(q.getAttribute("idProperty").getValue());//NOI18N
                        query.setServerUrl(q.getAttribute("server").getValue());//NOI18N
                        query.setTitle(q.getAttribute("title").getValue());//NOI18N
                        query.setType(q.getAttribute("type").getValue());//NOI18N
                        try {
                            query.setPropertyPrefix(q.getAttribute("propertyPrefix").getValue());//NOI18N
                        } catch (Exception skip) {
                            query.setPropertyPrefix(null);
                        }
                        try {
                            query.setPropertyNamespace(q.getAttribute("propertyNamespace").getValue());//NOI18N
                        } catch (Exception skip) {
                            query.setPropertyNamespace(null);
                        }
                        try {
                            query.setPositionProperty(q.getAttribute("positionProperty").getValue());//NOI18N
                        } catch (Exception skip) {
                            query.setPositionProperty(null);
                        }


                        //optional
                        if (q.getAttribute("queryPlaceholder") != null) {//NOI18N
                            query.setQueryPlaceholder(q.getAttribute("queryPlaceholder").getValue());//NOI18N
                        }
                        queryVector.add(query);
                    }
                    form.setQueries(queryVector);

                    forms.put(form.getId(), form);
                    log.debug("WFSForm " + form.getId() + " added");//NOI18N
                } catch (Throwable t) {
                    log.error("Could not create WFSForm", t);//NOI18N
                }
            }
            LinkedHashMap lhs = new LinkedHashMap(forms.size());


            List<String> keylistSorted = new Vector<String>(forms.keySet());

            Collections.sort(keylistSorted, new Comparator<String>() {

                @Override
                public int compare(String o1, String o2) {
                    try {
                        String sortO1=forms.get(o1).getSorter();
                        String sortO2=forms.get(o2).getSorter();
                        if (sortO1!=null&&sortO2!=null){
                            return sortO1.compareTo(sortO2);
                        }
                        else {
                            problemDuringSorting = true;
                        }
                    } catch (Exception e) {
                        problemDuringSorting = true;
                    }
                    return o1.compareTo(o2);
                }
            });

            if (!problemDuringSorting) {
                for (String key : keylistSorted) {
                    lhs.put(key, forms.get(key));
                }

                forms = lhs;
            }
            else {
                log.warn("Error while sorting the WFSForms. The order of the config file will be retained.");//NOI18N
            }
        } catch (Throwable t) {
            log.error("Could not create WFSForm", t);//NOI18N
        }
    }

    public void configure(Element parent) {
        //alle Infos kommen immer vom Server
    }

    public LinkedHashMap<String, AbstractWFSForm> getForms() {
        return forms;
    }

    private String readFileFromClassPathAsString(String filePath)
            throws java.io.IOException {
        InputStream is = getClass().getResourceAsStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer fileData = new StringBuffer(1000);
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
}
