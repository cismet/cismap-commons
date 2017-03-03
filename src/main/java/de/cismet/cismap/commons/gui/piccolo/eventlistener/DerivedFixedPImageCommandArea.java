/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

import java.awt.Image;

import java.beans.PropertyChangeEvent;

import de.cismet.cismap.commons.gui.piccolo.PFeature;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class DerivedFixedPImageCommandArea extends DerivedFixedPImage {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private PBasicInputEventHandler inputHandler = new PBasicInputEventHandler() {

            @Override
            public void keyboardFocusLost(final PInputEvent event) {
                super.keyboardFocusLost(event);
                DerivedFixedPImageCommandArea.this.keyboardFocusLost(event);
                event.setHandled(true);
            }

            @Override
            public void keyboardFocusGained(final PInputEvent event) {
                super.keyboardFocusGained(event);
                DerivedFixedPImageCommandArea.this.keyboardFocusGained(event);
                event.setHandled(true);
            }

            @Override
            public void mouseWheelRotatedByBlock(final PInputEvent event) {
                super.mouseWheelRotatedByBlock(event);
                DerivedFixedPImageCommandArea.this.mouseWheelRotatedByBlock(event);
                event.setHandled(true);
            }

            @Override
            public void mouseWheelRotated(final PInputEvent event) {
                super.mouseWheelRotated(event);
                DerivedFixedPImageCommandArea.this.mouseWheelRotated(event);
                event.setHandled(true);
            }

            @Override
            public void mouseReleased(final PInputEvent event) {
                super.mouseReleased(event);
                DerivedFixedPImageCommandArea.this.mouseReleased(event);
                event.setHandled(true);
            }

            @Override
            public void mouseMoved(final PInputEvent event) {
                super.mouseMoved(event);
                DerivedFixedPImageCommandArea.this.mouseMoved(event);
                event.setHandled(true);
            }

            @Override
            public void mouseExited(final PInputEvent event) {
                super.mouseExited(event);
                DerivedFixedPImageCommandArea.this.mouseExited(event);
                event.setHandled(true);
            }

            @Override
            public void mouseEntered(final PInputEvent event) {
                super.mouseEntered(event);
                DerivedFixedPImageCommandArea.this.mouseEntered(event);
                event.setHandled(true);
            }

            @Override
            public void mouseDragged(final PInputEvent event) {
                super.mouseDragged(event);
                DerivedFixedPImageCommandArea.this.mouseDragged(event);
                event.setHandled(true);
            }

            @Override
            public void mousePressed(final PInputEvent event) {
                super.mousePressed(event);
                DerivedFixedPImageCommandArea.this.mousePressed(event);
                event.setHandled(true);
            }

            @Override
            public void mouseClicked(final PInputEvent event) {
                super.mouseClicked(event);
                DerivedFixedPImageCommandArea.this.mouseClicked(event);
                event.setHandled(true);
            }

            @Override
            public void keyTyped(final PInputEvent event) {
                super.keyTyped(event);
                DerivedFixedPImageCommandArea.this.keyTyped(event);
                event.setHandled(true);
            }

            @Override
            public void keyReleased(final PInputEvent event) {
                super.keyReleased(event);
                DerivedFixedPImageCommandArea.this.keyReleased(event);
                event.setHandled(true);
            }

            @Override
            public void keyPressed(final PInputEvent event) {
                super.keyPressed(event);
                DerivedFixedPImageCommandArea.this.keyPressed(event);
                event.setHandled(true);
            }
        };

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DerivedFixedPImage object.
     *
     * @param  image   DOCUMENT ME!
     * @param  parent  DOCUMENT ME!
     * @param  rule    DOCUMENT ME!
     */
    public DerivedFixedPImageCommandArea(final Image image, final PFeature parent, final DeriveRule rule) {
        super(image, parent, rule);
        this.addInputEventListener(inputHandler);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        super.propertyChange(evt);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void keyPressed(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void keyReleased(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void keyTyped(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void mouseClicked(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void mousePressed(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void mouseDragged(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void mouseEntered(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void mouseExited(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void mouseMoved(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void mouseReleased(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void mouseWheelRotated(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void mouseWheelRotatedByBlock(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void keyboardFocusGained(final PInputEvent event) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public void keyboardFocusLost(final PInputEvent event) {
    }
}
