/*
 * GetFeatureInfoClickDetectionListener.java
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
 * Created on 7. April 2006, 11:07
 *
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FixedPImage;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.MapClickedEvent;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import javax.swing.ImageIcon;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class GetFeatureInfoClickDetectionListener extends PBasicInputEventHandler {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget");
    final public static String FEATURE_INFO_MODE = "FEATURE_INFO_CLICK";
    private ImageIcon info = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/featureInfo.png"));
    private FixedPImage pInfo = new FixedPImage(info.getImage());

    /** Creates a new instance of GetFeatureInfoClickDetectionListener */
    public GetFeatureInfoClickDetectionListener() {
        getPInfo().setSweetSpotX(0.5d);
        getPInfo().setSweetSpotY(1d);
    }

    @Override
    public void mouseClicked(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            MappingComponent mc = (MappingComponent) pInputEvent.getComponent();
            mc.addStickyNode(getPInfo());
            mc.getRubberBandLayer().removeAllChildren();
            //pInfo =new PImage(info.getImage());
            mc.getRubberBandLayer().addChild(getPInfo());
            getPInfo().setScale(1 / mc.getCamera().getViewScale());
            getPInfo().setOffset(pInputEvent.getPosition().getX(), pInputEvent.getPosition().getY());
            log.debug(getPInfo().getGlobalBounds().getWidth());
            getPInfo().setVisible(true);
            //mc.getCamera().animateViewToCenterBounds(pInfo.getBounds(),true,1000);
            getPInfo().repaint();
            mc.repaint();
        }
        CismapBroker.getInstance().fireClickOnMap(new MapClickedEvent(FEATURE_INFO_MODE, pInputEvent));
    }

    public FixedPImage getPInfo() {
        return pInfo;
    }
}
