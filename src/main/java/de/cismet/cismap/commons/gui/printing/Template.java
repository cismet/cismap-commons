/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.printing;

import org.jdom.Element;

import java.util.List;
import java.util.Vector;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class Template {

    //~ Instance fields --------------------------------------------------------

    private String title = "";                       // NOI18N
    private String file = "";                        // NOI18N
    private String className = "";                   // NOI18N
    private String mapPlaceholder = "";              // NOI18N
    private int mapWidth = 0;
    private int mapHeight = 0;
    private String scaleDemoninatorPlaceholder = ""; // NOI18N
    private Vector<AdditionalTemplateParameter> additionalParameters = new Vector<AdditionalTemplateParameter>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of Template.
     *
     * @param   template  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Template(final Element template) throws Exception {
        title = template.getAttribute("title").getValue();                                             // NOI18N
        file = template.getAttribute("file").getValue();                                               // NOI18N
        className = template.getAttribute("className").getValue();                                     // NOI18N
        mapPlaceholder = template.getAttribute("mapPlaceholder").getValue();                           // NOI18N
        mapWidth = template.getAttribute("mapWidth").getIntValue();                                    // NOI18N
        mapHeight = template.getAttribute("mapHeight").getIntValue();                                  // NOI18N
        scaleDemoninatorPlaceholder = template.getAttribute("scaleDenominatorPlaceholder").getValue(); // NOI18N
//        List additionalParameterList=template.getChildren("parameter");
//        for (Object elem : additionalParameterList) {
//            if (elem instanceof Element) {
//                AdditionalTemplateParameter p=new AdditionalTemplateParameter((Element)elem);
//                additionalParameters.add(p);
//            }
//        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        return getTitle();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   selected  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getElement(final boolean selected) {
        final Element e = new Element("template");                                       // NOI18N
        e.setAttribute("selected", String.valueOf(selected));                            // NOI18N
        e.setAttribute("title", getTitle());                                             // NOI18N
        e.setAttribute("file", getFile());                                               // NOI18N
        e.setAttribute("className", getClassName());                                     // NOI18N
        e.setAttribute("mapPlaceholder", getMapPlaceholder());                           // NOI18N
        e.setAttribute("mapWidth", getMapWidth() + "");                                  // NOI18N
        e.setAttribute("mapHeight", getMapHeight() + "");                                // NOI18N
        e.setAttribute("scaleDenominatorPlaceholder", getScaleDemoninatorPlaceholder()); // NOI18N
//        for (AdditionalTemplateParameter elem : additionalParameters) {
//            e.addContent(elem.getElement());
//        }
        return e;
    }
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof Template) && (((Template)obj).title == title)
                    && (((Template)obj).file == file)
                    && (((Template)obj).getClassName() == getClassName())
                    && (((Template)obj).mapPlaceholder == mapPlaceholder)
                    && (((Template)obj).mapWidth == mapWidth)
                    && (((Template)obj).mapHeight == mapHeight)
                    && (((Template)obj).scaleDemoninatorPlaceholder == scaleDemoninatorPlaceholder);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getTitle() {
        return title;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  title  DOCUMENT ME!
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getFile() {
        return file;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  file  DOCUMENT ME!
     */
    public void setFile(final String file) {
        this.file = file;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getMapPlaceholder() {
        return mapPlaceholder;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mapPlaceholder  DOCUMENT ME!
     */
    public void setMapPlaceholder(final String mapPlaceholder) {
        this.mapPlaceholder = mapPlaceholder;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getScaleDemoninatorPlaceholder() {
        return scaleDemoninatorPlaceholder;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  scaleDemoninatorPlaceholder  DOCUMENT ME!
     */
    public void setScaleDemoninatorPlaceholder(final String scaleDemoninatorPlaceholder) {
        this.scaleDemoninatorPlaceholder = scaleDemoninatorPlaceholder;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Vector<AdditionalTemplateParameter> getAdditionalParameters() {
        return additionalParameters;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  additionalParameters  DOCUMENT ME!
     */
    public void setAdditionalParameters(final Vector<AdditionalTemplateParameter> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getMapHeight() {
        return mapHeight;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mapHeight  DOCUMENT ME!
     */
    public void setMapHeight(final int mapHeight) {
        this.mapHeight = mapHeight;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getMapWidth() {
        return mapWidth;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mapWidth  DOCUMENT ME!
     */
    public void setMapWidth(final int mapWidth) {
        this.mapWidth = mapWidth;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getClassName() {
        return className;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  className  DOCUMENT ME!
     */
    public void setClassName(final String className) {
        this.className = className;
    }
}
