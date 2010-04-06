/*
 * AdditionalTemplateParameter.java
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
 * Created on 10. Juli 2006, 15:54
 *
 */

package de.cismet.cismap.commons.gui.printing;

import org.jdom.Element;


/**
 *
 * @author thorsten.hell@cismet.de
 */
public class AdditionalTemplateParameter {
    private String placeholder="";//NOI18N
    private String title="";//NOI18N
    public AdditionalTemplateParameter(Element parameter) {
        placeholder=parameter.getAttribute("placeholder").getValue();//NOI18N
        title=parameter.getAttribute("title").getValue();//NOI18N
        
    }
    
    Element getElement() {
        Element e=new Element("parameter");//NOI18N
        e.setAttribute("placeholder",getPlaceholder());//NOI18N
        e.setAttribute("title",getTitle());//NOI18N
        return e;
    }
    
    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
