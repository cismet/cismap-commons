/*
 * RaisePolygonListener.java
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
 * Created on 15. September 2005, 16:07
 *
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class RaisePolygonListener extends PBasicInputEventHandler {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private MappingComponent mc = null;

    /** Creates a new instance of RaisePolygonListener */
    public RaisePolygonListener(MappingComponent mc) {
        this.mc = mc;
    }

    @Override
    public void mouseClicked(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        log.debug("RaiseTry1");//NOI18N
        PFeature o = (PFeature) PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[]{PFeature.class});
        //if (o!=null&&o.getFeature() instanceof DefaultFeatureServiceFeature&& o.getVisible()==true && o.getParent()!=null && o.getParent().getVisible()==true) {
        if (o != null && o.getFeature() != null && o.getVisible() == true && o.getParent() != null && o.getParent().getVisible() == true) {
            log.debug("RaiseTry2");//NOI18N
            PureNewFeature pnf = new PureNewFeature((Geometry) (o.getFeature().getGeometry().clone()));
            pnf.setEditable(true);
            mc.getFeatureCollection().addFeature(pnf);
        }
    }
}
