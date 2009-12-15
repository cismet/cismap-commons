/*
 * SelectionListener.java
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
 * Created on 22. August 2006, 16:32
 *
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.ParentNodeIsAPFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.MapSearchEvent;
import de.cismet.cismap.commons.tools.PFeatureTools;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import java.awt.event.InputEvent;
import java.util.Vector;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class SelectionListener extends RectangleRubberBandListener {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public static final String SELECTION_CHANGED_NOTIFICATION = "SELECTION_CHANGED_NOTIFICATION";
    PFeature sel = null;
    Vector<PFeature> pfVector = new Vector<PFeature>();
    private int clickCount = 0;
    MappingComponent mc = null;
    private static final double RECT_BUFFER = 0.1d;

    //Selektiere einen PNode
    @Override
    public void mouseClicked(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        log.debug("mouseClicked():" + pInputEvent.getPickedNode());
        Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[]{PFeature.class});
        clickCount = pInputEvent.getClickCount();
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            mc = (MappingComponent) pInputEvent.getComponent();
        }

        if (pInputEvent.isRightMouseButton()) {
            log.debug("right mouseclick");
//            if (o instanceof PFeature && ((PFeature)o).getFeature() instanceof XStyledFeature) {
//                XStyledFeature xf=(XStyledFeature)((PFeature)o).getFeature();
//                log.debug("valid object under pointer");
//                JPopupMenu popup=new JPopupMenu("Test");
//                JMenuItem m=new JMenuItem("TIM Merker anlegen");
//                m.setIcon(xf.getIconImage());
//                popup.add(m);
//                popup.show(mc,(int)pInputEvent.getCanvasPosition().getX(),(int)pInputEvent.getCanvasPosition().getY());
//            }
        } else {
            if (o instanceof PFeature) {
                super.mouseClicked(pInputEvent);
                sel = (PFeature) o;
                if (sel.getFeature().canBeSelected()) {
                    if (mc != null && pInputEvent.isLeftMouseButton() && pInputEvent.isControlDown()) {
                        if (mc.getFeatureCollection() instanceof DefaultFeatureCollection) {
                            if (!((DefaultFeatureCollection) mc.getFeatureCollection()).isSelected(sel.getFeature())) {
                                ((DefaultFeatureCollection) mc.getFeatureCollection()).addToSelection(sel.getFeature());
                            } else {
                                ((DefaultFeatureCollection) mc.getFeatureCollection()).unselect(sel.getFeature());
                            }

                        } else {
                            log.warn("mc.getFeatureCollection() instanceof DefaultFeatureCollection == false !!!!!!!");
                        }
                    } else {
                        mc.getFeatureCollection().select(sel.getFeature());
                    }
                } else {
                    log.debug("Feature cannot be selected");
                    if (mc.getFeatureCollection() instanceof DefaultFeatureCollection) {
                        ((DefaultFeatureCollection) mc.getFeatureCollection()).unselectAll();
                    }
                }
                postSelectionChanged();
                if (pInputEvent.getClickCount() == 2) {
                    if (sel.getFeature() instanceof SearchFeature) {
                        if (pInputEvent.isLeftMouseButton()) {
                            // neue Suche mit Geometry auslösen
                            ((CreateSearchGeometryListener)mc.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON)).performSearch((SearchFeature)sel.getFeature());
                        }
                    }
                }
            } else {
                if (mc.getFeatureCollection() instanceof DefaultFeatureCollection) {
                    ((DefaultFeatureCollection) mc.getFeatureCollection()).unselectAll();
                }
            }
//        else if(o instanceof ParentNodeIsAPFeature && o instanceof PNode) {
//            super.mouseClicked(pInputEvent);
//            Object test=o;
//            do {
//                test=((PNode)test).getParent();
//            }
//            while(!(test instanceof PFeature));
//            if (test instanceof PFeature) {
//                sel=(PFeature)test;
//            }
//            postSelectionChanged();
//        }
        }
    }

    /**
     * Wird gefeuert, wenn die Maustaste nach dem Ziehen des Markiervierecks
     * losgelassen wird.
     *
     * @param event das Mouseevent (als PInputEvent)
     */
    @Override
    public void mouseReleased(final PInputEvent event) {
        super.mouseReleased(event);
        if (event.getButton() == 1) { // linke Maustaste
            // Mouseevent muss von einer MappingComponent gefeuert werden
            if (event.getComponent() instanceof MappingComponent) {
                mc = (MappingComponent) event.getComponent();
                mc.getHandleLayer().removeAllChildren();
                // einfacher Klick ohne ziehen des Markiervierecks
                if ( rectangle != null && !(rectangle.getWidth() < RECT_BUFFER || rectangle.getHeight() < RECT_BUFFER)) {
                    // Hole alle PFeatures die das Markierviereck schneiden
                    // und Hinzuf\u00FCgen dieser PFeatures zur Selektion
                    log.debug("Markierviereck = (X=" + rectangle.getBounds().getX() + ",Y=" + rectangle.getBounds().getY() +
                            ",W=" + rectangle.getBounds().getWidth() + ",H=" + rectangle.getBounds().getHeight() + ")");
                    ((DefaultFeatureCollection) mc.getFeatureCollection()).unselectAll();
                    PFeature[] pfArr = PFeatureTools.getPFeaturesInArea(mc, rectangle.getBounds());
                    Vector<Feature> toBeSelected = new Vector<Feature>();
                    Vector<Feature> toBeUnselected = new Vector<Feature>();

                    for (PFeature pf : pfArr) {
                        if (pf.getFeature().canBeSelected()) {
                            if (mc.getFeatureCollection() instanceof DefaultFeatureCollection) {
                                if (!((DefaultFeatureCollection) mc.getFeatureCollection()).isSelected(pf.getFeature())) {
                                    log.debug("Feature markiert: " + pf);
                                    toBeSelected.add(pf.getFeature());

                                } else {
                                    toBeUnselected.add(pf.getFeature());
                                //mc.getFeatureCollection().unselect(pf.getFeature()); //war vorher unselectAll()
                                }
                            }
                        } else {
                            log.debug("Feature cannot be selected");
                            if (mc.getFeatureCollection() instanceof DefaultFeatureCollection) {
                                toBeUnselected.add(pf.getFeature());
                            //((DefaultFeatureCollection) mc.getFeatureCollection()).unselect(pf.getFeature());//war vorher unselectAll()
                            }
                        }
                    }

                    //Hier passierts
                    ((DefaultFeatureCollection) mc.getFeatureCollection()).addToSelection(toBeSelected);
                    ((DefaultFeatureCollection) mc.getFeatureCollection()).unselect(toBeUnselected);

                    pfVector = new Vector(((DefaultFeatureCollection) mc.getFeatureCollection()).getSelectedFeatures());
                    postSelectionChanged();
                }
            }
        }
    }

    private void postSelectionChanged() {
        log.debug("postSelectionChanged");
        PNotificationCenter pn = PNotificationCenter.defaultCenter();
        pn.postNotification(SelectionListener.SELECTION_CHANGED_NOTIFICATION, this);
    }

    public Vector<PFeature> getSelectedPFeatures() {
        return pfVector;
    }

    @Deprecated
    public PFeature getSelectedPFeature() {
        return sel;
    }

    public PFeature getAffectedPFeature() {
        return sel;
    }

    public int getClickCount() {
        return clickCount;
    }
}
