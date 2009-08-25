/*
 * Action.java
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
 * Created on 10. Juli 2006, 17:35
 *
 */

package de.cismet.cismap.commons.gui.printing;

import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class Action {
    private String id;
    private String title;
    
    public static final String PRINTPREVIEW="PRINTPREVIEW";
    public static final String PDF="PDF";
    public static final String PRINT="PRINT";
    /** Creates a new instance of Action */
    public Action(Element e) throws Exception{
        id=e.getAttribute("id").getValue();
        title=e.getText();
    }
    public String toString() {
        return getTitle();
    }
    public Element getElement(boolean selected) {
        Element e=new Element("action");
        e.setAttribute("selected",new Boolean(selected).toString());
        e.setAttribute("id",id);
        e.setText(getTitle());
        return e;
             
    }
    public boolean equals(Object obj) {
        return obj instanceof Action && ((Action)obj).id.equals(id);
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
}
