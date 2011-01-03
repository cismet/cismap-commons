/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.overviewwidget;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

import java.awt.Color;
import java.awt.Dimension;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FixedWidthStroke;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class OverviewWidget extends PCanvas {

    //~ Instance fields --------------------------------------------------------

    PLayer highlightingLayer = new PLayer();
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new OverviewWidget object.
     *
     * @param  mapC  DOCUMENT ME!
     */
    public OverviewWidget(final MappingComponent mapC) {
        super();
        if (log.isDebugEnabled()) {
            log.debug("Start");                      // NOI18N
        }
        setBackground(Color.YELLOW);
        setPreferredSize(new Dimension(100, 100));
        try {
            getLayer().addChild(highlightingLayer);
            getLayer().addChild(new PText("Start")); // NOI18N

//        PBounds pb = new BoundingBox(2568580.612400579, 2568580.612400579, 2568580.612400579, 5687929.518337978).getPBounds(mapC.getWtst());
//        getCamera().animateViewToCenterBounds(pb,true,0);

            mapC.getCamera().addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener() {

                    @Override
                    public void propertyChange(final PropertyChangeEvent evt) {
//                PBounds pb = new BoundingBox(2568580.612400579, 2568580.612400579, 2568580.612400579, 5687929.518337978).getPBounds(mapC.getWtst());
//                getCamera().animateViewToCenterBounds(pb,true,0);

                        outlineArea(mapC.getCamera().getViewBounds());
                    }
                });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  b  DOCUMENT ME!
     */
    public void outlineArea(final PBounds b) {
        if (b == null) {
            if (highlightingLayer.getChildrenCount() > 0) {
                highlightingLayer.removeAllChildren();
            }
        } else {
            highlightingLayer.removeAllChildren();
            highlightingLayer.setTransparency(1);
            final PPath rectangle = new PPath();
            rectangle.setPaint(Color.BLUE);
            rectangle.setStroke(new FixedWidthStroke());
            rectangle.setStrokePaint(Color.RED);
            rectangle.setPathTo(b);
            highlightingLayer.addChild(rectangle);
        }
    }
}
