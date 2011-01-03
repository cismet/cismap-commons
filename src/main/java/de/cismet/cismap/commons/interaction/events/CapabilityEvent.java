/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.interaction.events;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class CapabilityEvent {

    //~ Instance fields --------------------------------------------------------

    private Object capabilityObject = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of CapabilityEvent.
     *
     * @param  capabilityObject  DOCUMENT ME!
     */
    public CapabilityEvent(final Object capabilityObject) {
        this.capabilityObject = capabilityObject;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getCapabilityObject() {
        return capabilityObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  capabilityObject  DOCUMENT ME!
     */
    public void setCapabilityObject(final Object capabilityObject) {
        this.capabilityObject = capabilityObject;
    }
}
