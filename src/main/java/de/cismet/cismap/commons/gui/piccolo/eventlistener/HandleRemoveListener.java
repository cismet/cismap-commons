/*
 * HandleRemoveListener.java
 *
 * Created on 15. April 2005, 12:02
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.gui.piccolo.PHandle;
import de.cismet.cismap.commons.tools.PFeatureTools;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;

/**
 *
 * @author hell
 */
public class HandleRemoveListener extends PBasicInputEventHandler {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    @Override
    public void mouseClicked(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        log.info("Entferne Handle");
        Object o = PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[]{PHandle.class});
        if (o instanceof PHandle) {
            log.info("Entferne Handle");
            ((PHandle) (o)).removeHandle();
        }
    }

    @Override
    public void mouseMoved(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (PFeatureTools.getFirstValidObjectUnderPointer(pInputEvent, new Class[]{PHandle.class}) instanceof PHandle) {
            log.info("\u00DCber PHandle");
        }
    }
}
