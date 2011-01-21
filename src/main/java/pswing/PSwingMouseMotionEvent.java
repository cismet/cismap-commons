/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package pswing;

import edu.umd.cs.piccolo.event.PInputEvent;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * <b>ZMouseMotionEvent</b> is an event which indicates that a mouse motion action occurred in a node.
 *
 * <P>This low-level event is generated by a node object for:</P>
 *
 * <ul>
 *   <li>Mouse Motion Events
 *
 *     <ul>
 *       <li>the mouse is moved</li>
 *       <li>the mouse is dragged</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <P>A ZMouseEvent object is passed to every <code>ZMouseMotionListener</code> or <code>ZMouseMotionAdapter</code>
 * object which registered to receive mouse motion events using the component's <code>addMouseMotionListener</code>
 * method. (<code>ZMouseMotionAdapter</code> objects implement the <code>ZMouseMotionListener</code> interface.) Each
 * such listener object gets a <code>ZMouseEvent</code> containing the mouse motion event.</p>
 *
 * <P><b>Warning:</b> Serialized and ZSerialized objects of this class will not be compatible with future Jazz releases.
 * The current serialization support is appropriate for short term storage or RMI between applications running the same
 * version of Jazz. A future release of Jazz will provide support for long term persistence.</P>
 *
 * @version  $Revision$, $Date$
 */
public class PSwingMouseMotionEvent extends PSwingMouseEvent {

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructs a new ZMouse event from a Java MouseEvent.
     *
     * @param  id     The event type (MOUSE_MOVED, MOUSE_DRAGGED)
     * @param  e      The original Java mouse event when in MOUSE_DRAGGED events.
     * @param  event  DOCUMENT ME!
     */
    protected PSwingMouseMotionEvent(final int id, final MouseEvent e, final PInputEvent event) {
        super(id, e, event);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Calls appropriate method on the listener based on this events ID.
     *
     * @param   listener  DOCUMENT ME!
     *
     * @throws  RuntimeException  DOCUMENT ME!
     */
    @Override
    public void dispatchTo(final Object listener) {
        final MouseMotionListener mouseMotionListener = (MouseMotionListener)listener;
        switch (getID()) {
            case PSwingMouseEvent.MOUSE_DRAGGED: {
                mouseMotionListener.mouseDragged(this);
                break;
            }
            case PSwingMouseEvent.MOUSE_MOVED: {
                mouseMotionListener.mouseMoved(this);
                break;
            }
            default: {
                throw new RuntimeException("ZMouseMotionEvent with bad ID"); // NOI18N
            }
        }
    }
}
