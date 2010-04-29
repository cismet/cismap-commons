/*
 * Resolution.java
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

import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class Resolution {
    private int resolution=72;
    private String text;
    public Resolution(int resolution,String text) {
        this.resolution=resolution;
        this.text=text;
    }
    public Resolution(Element e) throws Exception{
        resolution=e.getAttribute("dpi").getIntValue();//NOI18N
        text=e.getText();
    }
    public String toString(){
        return text;
    }
    public boolean equals(Object obj) {
        return obj instanceof Resolution && ((Resolution)obj).resolution==resolution;
    }
    public Element getElement(boolean selected) {
        Element e=new Element("resolution");//NOI18N
        e.setAttribute("selected",String.valueOf(selected));//NOI18N
        e.setAttribute("dpi",resolution+"");//NOI18N
        e.setText(text);
        return e;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
