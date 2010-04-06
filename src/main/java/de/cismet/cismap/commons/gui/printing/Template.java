/*
 * Template.java
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
 * Created on 10. Juli 2006, 15:55
 *
 */

package de.cismet.cismap.commons.gui.printing;

import java.util.List;
import java.util.Vector;
import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class Template {
    private String title="";//NOI18N
    private String file="";//NOI18N
    private String className="";//NOI18N
    private String mapPlaceholder="";//NOI18N
    private int mapWidth=0;
    private int mapHeight=0;
    private String scaleDemoninatorPlaceholder="";//NOI18N
    private Vector<AdditionalTemplateParameter> additionalParameters=new Vector<AdditionalTemplateParameter>();
    
    /** Creates a new instance of Template */
    public Template(Element template) throws Exception{
        title=template.getAttribute("title").getValue();//NOI18N
        file=template.getAttribute("file").getValue();//NOI18N
        className=template.getAttribute("className").getValue();//NOI18N
        mapPlaceholder=template.getAttribute("mapPlaceholder").getValue();//NOI18N
        mapWidth=template.getAttribute("mapWidth").getIntValue();//NOI18N
        mapHeight=template.getAttribute("mapHeight").getIntValue();//NOI18N
        scaleDemoninatorPlaceholder=template.getAttribute("scaleDenominatorPlaceholder").getValue();//NOI18N
//        List additionalParameterList=template.getChildren("parameter");
//        for (Object elem : additionalParameterList) {
//            if (elem instanceof Element) {
//                AdditionalTemplateParameter p=new AdditionalTemplateParameter((Element)elem);
//                additionalParameters.add(p);
//            }
//        }
    }

    public String toString() {
        return getTitle();
    }
    
    public Element getElement(boolean selected) {
        Element e=new Element("template");//NOI18N
        e.setAttribute("selected",String.valueOf(selected));//NOI18N
        e.setAttribute("title",getTitle());//NOI18N
        e.setAttribute("file",getFile());//NOI18N
        e.setAttribute("className", getClassName());//NOI18N
        e.setAttribute("mapPlaceholder",getMapPlaceholder());//NOI18N
        e.setAttribute("mapWidth",getMapWidth()+"");//NOI18N
        e.setAttribute("mapHeight",getMapHeight()+"");//NOI18N
        e.setAttribute("scaleDenominatorPlaceholder",getScaleDemoninatorPlaceholder());//NOI18N
//        for (AdditionalTemplateParameter elem : additionalParameters) {
//            e.addContent(elem.getElement());
//        }
        return e;
    }
    public boolean equals(Object obj) {
        return obj instanceof Template && ((Template)obj).title==title 
                && ((Template)obj).file==file 
                && ((Template)obj).getClassName()==getClassName() 
                && ((Template)obj).mapPlaceholder==mapPlaceholder 
                && ((Template)obj).mapWidth==mapWidth 
                && ((Template)obj).mapHeight==mapHeight 
                && ((Template)obj).scaleDemoninatorPlaceholder==scaleDemoninatorPlaceholder ;
    }
    
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getMapPlaceholder() {
        return mapPlaceholder;
    }

    public void setMapPlaceholder(String mapPlaceholder) {
        this.mapPlaceholder = mapPlaceholder;
    }

    public String getScaleDemoninatorPlaceholder() {
        return scaleDemoninatorPlaceholder;
    }

    public void setScaleDemoninatorPlaceholder(String scaleDemoninatorPlaceholder) {
        this.scaleDemoninatorPlaceholder = scaleDemoninatorPlaceholder;
    }

    public Vector<AdditionalTemplateParameter> getAdditionalParameters() {
        return additionalParameters;
    }

    public void setAdditionalParameters(Vector<AdditionalTemplateParameter> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public void setMapHeight(int mapHeight) {
        this.mapHeight = mapHeight;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public void setMapWidth(int mapWidth) {
        this.mapWidth = mapWidth;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
