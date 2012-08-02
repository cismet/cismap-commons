/*
 * BoundingBoxSearchListener.java
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
 * Created on 24. April 2006, 16:32
 *
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.MapSearchEvent;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import java.awt.geom.Point2D;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class BoundingBoxSearchListener extends RectangleRubberBandListener {

    /** Creates a new instance of BoundingBoxSearchListener */
    public BoundingBoxSearchListener() {
    }

    @Override
    public void mouseReleased(PInputEvent e) {
        super.mouseReleased(e);
        MapSearchEvent mse = new MapSearchEvent();
        
        PBounds pb=super.rectangle.getFullBounds();
        Point2D[] boundingBoxPoints=new Point2D[2];
        boundingBoxPoints[0] = new Point2D.Double(pb.getMinX(), pb.getMinY());
        boundingBoxPoints[1] = new Point2D.Double(pb.getMaxX(), pb.getMaxY());

        Point2D[] p = new Point2D[5];
            p[0] = (Point2D) boundingBoxPoints[0];
            p[2] = (Point2D) boundingBoxPoints[1];
            p[1] = new Point2D.Double(p[0].getX(), p[2].getY());
            p[3] = new Point2D.Double(p[2].getX(), p[0].getY());
            p[4] = p[0];


        PureNewFeature pnf=new PureNewFeature(p, CismapBroker.getInstance().getMappingComponent().getWtst());
        mse.setGeometry(pnf.getGeometry());
        mse.setBounds(super.rectangle.getFullBounds());
        CismapBroker.getInstance().fireMapSearchInited(mse);
    }
}
