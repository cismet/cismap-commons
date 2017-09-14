/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * HandleRemoveListener.java
 *
 * Created on 15. April 2005, 12:02
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;

import de.cismet.cismap.commons.gui.piccolo.PHandle;
import de.cismet.cismap.commons.tools.PFeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class HandleRemoveListener extends PBasicInputEventHandler {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        log.info("remove Handle");     // NOI18N
        final Object o = PFeatureTools.getFirstValidObjectUnderPointer(
                pInputEvent,
                new Class[] { PHandle.class },
                true);
        if (o instanceof PHandle) {
            log.info("remove Handle"); // NOI18N
            ((PHandle)(o)).removeHandle();
        }
    }

    @Override
    public void mouseMoved(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[] { PHandle.class })
                    instanceof PHandle) {
            log.info("Over PHandle"); // NOI18N
        }
    }
}
