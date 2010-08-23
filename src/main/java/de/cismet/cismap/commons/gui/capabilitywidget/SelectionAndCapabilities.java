/*
 * SelectionAndCapabilities.java
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
 * Created on 29. November 2005, 10:44
 *
 */

package de.cismet.cismap.commons.gui.capabilitywidget;

import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;
import javax.swing.tree.TreePath;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class SelectionAndCapabilities {
    public SelectionAndCapabilities(TreePath[] s,WMSCapabilities c,String url) {
        selection=s;
        capabilities=c;
        this.url=url;
    }
    private TreePath[] selection;
    private WMSCapabilities capabilities;
    private String url;
    public TreePath[] getSelection() {
        return selection;
    }

    public void setSelection(TreePath[] selection) {
        this.selection = selection;
    }

    public WMSCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(WMSCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
