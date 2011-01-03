/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wfsforms;

import org.jdom.Element;

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

import de.cismet.cismap.commons.gui.MappingComponent;

import de.cismet.tools.configuration.Configurable;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class WFSFormFactory implements Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static WFSFormFactory singletonInstance;

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private LinkedHashMap<String, AbstractWFSForm> forms = new LinkedHashMap<String, AbstractWFSForm>();
    /** Creates a new instance of WFSFormFactory. */
    private Element configuration;
    private MappingComponent mappingComponent;
    private boolean problemDuringSorting = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WFSFormFactory object.
     *
     * @param  map  DOCUMENT ME!
     */
    private WFSFormFactory(final MappingComponent map) {
        mappingComponent = map;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   map  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static WFSFormFactory getInstance(final MappingComponent map) {
        if (singletonInstance == null) {
            singletonInstance = new WFSFormFactory(map);
        }
        return singletonInstance;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static WFSFormFactory getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new WFSFormFactory(null);
        }
        return singletonInstance;
    }

    @Override
    public Element getConfiguration() {
        final Element ret = new Element("cismapWFSFormsPreferences"); // NOI18N
//        Set<String> keySet=forms.keySet();
//        for (String key:keySet) {
//            Element form=forms.get(key).getElement();
//            ret.addContent(form);
//        }
        return ret;
    }

    @Override
    public void masterConfigure(final Element parent) {
        forms.clear();
        try {
            configuration = (Element)((Element)parent.clone()).getChild("cismapWFSFormsPreferences").detach();          // NOI18N
            final List list = configuration.getChildren("wfsForm");                                                     // NOI18N
            for (final Object o : list) {
                try {
                    final Element e = (Element)o;
                    if (log.isDebugEnabled()) {
                        log.debug("Try to create WFSForm: " + e.getContent());                                          // NOI18N
                    }
                    final String className = e.getAttribute("className").getValue();                                    // NOI18N
                    final Class formClass = Class.forName(className);
                    final Constructor constructor = formClass.getConstructor();
                    final AbstractWFSForm form = (AbstractWFSForm)constructor.newInstance();
                    form.setClassName(className);
                    form.setId(e.getAttribute("id").getValue());                                                        // NOI18N
                    try {
                        form.setSorter(e.getAttribute("sorter").getValue());                                            // NOI18N
                    } catch (Exception skip) {
                    }
                    form.setTitle(e.getAttribute("title").getValue());                                                  // NOI18N
                    form.setMenuString(e.getAttribute("menu").getValue());                                              // NOI18N
                    form.setIconPath(e.getAttribute("icon").getValue());                                                // NOI18N
                    form.setIcon(new javax.swing.ImageIcon(getClass().getResource(e.getAttribute("icon").getValue()))); // NOI18N
                    final Vector<WFSFormQuery> queryVector = new Vector<WFSFormQuery>();
                    final List queries = e.getChildren("wfsFormQuery");                                                 // NOI18N
                    for (final Object oq : queries) {
                        final Element q = (Element)oq;
                        final WFSFormQuery query = new WFSFormQuery();
                        query.setComponentName(q.getAttribute("componentName").getValue());                             // NOI18N
                        query.setDisplayTextProperty(q.getAttribute("displayTextProperty").getValue());                 // NOI18N
                        query.setExtentProperty(q.getAttribute("extentProperty").getValue());                           // NOI18N
                        query.setFilename(q.getAttribute("queryFile").getValue());                                      // NOI18N
                        query.setWfsQueryString(readFileFromClassPathAsString(query.getFilename()));
                        query.setId(q.getAttribute("id").getValue());                                                   // NOI18N
                        query.setIdProperty(q.getAttribute("idProperty").getValue());                                   // NOI18N
                        query.setServerUrl(q.getAttribute("server").getValue());                                        // NOI18N
                        query.setTitle(q.getAttribute("title").getValue());                                             // NOI18N
                        query.setType(q.getAttribute("type").getValue());                                               // NOI18N
                        try {
                            query.setPropertyPrefix(q.getAttribute("propertyPrefix").getValue());                       // NOI18N
                        } catch (Exception skip) {
                            query.setPropertyPrefix(null);
                        }
                        try {
                            query.setPropertyNamespace(q.getAttribute("propertyNamespace").getValue());                 // NOI18N
                        } catch (Exception skip) {
                            query.setPropertyNamespace(null);
                        }
                        try {
                            query.setPositionProperty(q.getAttribute("positionProperty").getValue());                   // NOI18N
                        } catch (Exception skip) {
                            query.setPositionProperty(null);
                        }

                        // optional
                        if (q.getAttribute("queryPlaceholder") != null) {                             // NOI18N
                            query.setQueryPlaceholder(q.getAttribute("queryPlaceholder").getValue()); // NOI18N
                        }
                        queryVector.add(query);
                    }
                    form.setQueries(queryVector);

                    forms.put(form.getId(), form);
                    if (log.isDebugEnabled()) {
                        log.debug("WFSForm " + form.getId() + " added"); // NOI18N
                    }
                } catch (Throwable t) {
                    log.error("Could not create WFSForm", t);            // NOI18N
                }
            }
            final LinkedHashMap lhs = new LinkedHashMap(forms.size());

            final List<String> keylistSorted = new Vector<String>(forms.keySet());

            Collections.sort(keylistSorted, new Comparator<String>() {

                    @Override
                    public int compare(final String o1, final String o2) {
                        try {
                            final String sortO1 = forms.get(o1).getSorter();
                            final String sortO2 = forms.get(o2).getSorter();
                            if ((sortO1 != null) && (sortO2 != null)) {
                                return sortO1.compareTo(sortO2);
                            } else {
                                problemDuringSorting = true;
                            }
                        } catch (Exception e) {
                            problemDuringSorting = true;
                        }
                        return o1.compareTo(o2);
                    }
                });

            if (!problemDuringSorting) {
                for (final String key : keylistSorted) {
                    lhs.put(key, forms.get(key));
                }

                forms = lhs;
            } else {
                log.warn("Error while sorting the WFSForms. The order of the config file will be retained."); // NOI18N
            }
        } catch (Throwable t) {
            log.error("Could not create WFSForm", t);                                                         // NOI18N
        }
    }

    @Override
    public void configure(final Element parent) {
        // alle Infos kommen immer vom Server
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LinkedHashMap<String, AbstractWFSForm> getForms() {
        return forms;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   filePath  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  java.io.IOException  DOCUMENT ME!
     */
    private String readFileFromClassPathAsString(final String filePath) throws java.io.IOException {
        final InputStream is = getClass().getResourceAsStream(filePath);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        final StringBuffer fileData = new StringBuffer(1000);
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            final String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
}
