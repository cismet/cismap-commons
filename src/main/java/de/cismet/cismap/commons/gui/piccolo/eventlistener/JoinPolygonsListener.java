/*
 * JoinPolygonsListener.java
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
 * Created on 6. September 2005, 11:55
 *
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolox.event.PNotificationCenter;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class JoinPolygonsListener extends PBasicInputEventHandler {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public static final String FEATURE_JOIN_REQUEST_NOTIFICATION = "FEATURE_JOIN_REQUEST_NOTIFICATION";//NOI18N
    PFeature featureRequestedForJoin = null;
    int modifier = -1;

    @Override
    public void mouseClicked(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[]{PFeature.class});
        modifier = pInputEvent.getModifiers();
        if (o instanceof PFeature) {
            super.mouseClicked(pInputEvent);
            featureRequestedForJoin = (PFeature) (o);
            postFeatureJoinRequest();
        } else {
            featureRequestedForJoin = null;
        }
    }

    private void postFeatureJoinRequest() {
        PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(FEATURE_JOIN_REQUEST_NOTIFICATION, this);
    }

    public PFeature getFeatureRequestedForJoin() {
        return featureRequestedForJoin;
    }

    public int getModifier() {
        return modifier;
    }
}
