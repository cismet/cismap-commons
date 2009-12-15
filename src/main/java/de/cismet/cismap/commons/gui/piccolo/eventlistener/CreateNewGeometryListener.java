/*
 * CreateNewGeometryListener.java
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
 * Created on 29. Mai 2006, 11:55
 *
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import java.awt.Color;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class CreateNewGeometryListener extends CreateGeometryListener {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    /** Creates a new instance of CreateNewGeometryListener */
    private CreateNewGeometryListener(MappingComponent mc, Class geometryFeatureClass) {
        super(mc, geometryFeatureClass);
    }

    public CreateNewGeometryListener(MappingComponent mc) {
        super(mc, PureNewFeature.class);
    }

    @Override
    protected Color getFillingColor() {
        if (isInMode(POLYGON)) {
            return new Color(1f, 0f, 0f, 0.5f);
        } else {
            return null;
        }
    }

    @Override
    protected void finishGeometry(PureNewFeature newFeature) {
        super.finishGeometry(newFeature);

        newFeature.setEditable(true);
        mc.getFeatureCollection().holdFeature(newFeature);
    }

}
