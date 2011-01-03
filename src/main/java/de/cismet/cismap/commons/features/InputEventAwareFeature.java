/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public interface InputEventAwareFeature extends Feature {

    //~ Methods ----------------------------------------------------------------

    /**
     * public void keyPressed(PInputEvent event); public void keyReleased(PInputEvent event); public void
     * keyTyped(PInputEvent event); public void keyboardFocusGained(PInputEvent event); public void
     * keyboardFocusLost(PInputEvent event);
     *
     * @param  event  DOCUMENT ME!
     */
    void mouseClicked(PInputEvent event);
    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    void mouseDragged(PInputEvent event);
    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    void mouseEntered(PInputEvent event);
    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    void mouseExited(PInputEvent event);
    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    void mouseMoved(PInputEvent event);
    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    void mousePressed(PInputEvent event);
    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    void mouseReleased(PInputEvent event);
    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    void mouseWheelRotated(PInputEvent event);

    /**
     * DOCUMENT ME!
     *
     * @param   event  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean noFurtherEventProcessing(PInputEvent event);
}
