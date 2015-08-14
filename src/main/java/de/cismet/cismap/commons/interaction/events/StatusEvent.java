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
public class StatusEvent {

    //~ Static fields/initializers ---------------------------------------------

    public static final String ACTIVE_STATUS = "active_status";         // NOI18N
    public static final String COORDINATE_STRING = "coordinate_string"; // NOI18N
    public static final String ERROR_STATUS = "error_status";           // NOI18N
    public static final String MAPPING_MODE = "mode";                   // NOI18N
    public static final String MEASUREMENT_INFOS = "measurement";       // NOI18N
    public static final String OBJECT_INFOS = "object_infos";           // NOI18N
    public static final String SCALE = "scale";                         // NOI18N
    public static final String CRS = "crs";                             // NOI18N
    public static final String RETRIEVAL_STARTED = "retrieval.started";
    public static final String RETRIEVAL_COMPLETED = "retrieval.completed";
    public static final String RETRIEVAL_ABORTED = "retrieval.aborted";
    public static final String RETRIEVAL_ERROR = "retrieval.error";
    public static final String RETRIEVAL_REMOVED = "retrieval.removed";
    public static final String RETRIEVAL_RESET = "retrieval.reset";
    public static final String MAP_EXTEND_FIXED = "map.extent.fixed";
    public static final String MAP_SCALE_FIXED = "map.scale.fixed";
    public static final String AWAKED_FROM_DUMMY = "awaked.from.dummy";
    public static final String WINDOW_REMOVED = "awaked.from.dummy";

    //~ Instance fields --------------------------------------------------------

    private String name;
    private Object value;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of StatusEvent.
     *
     * @param  name   DOCUMENT ME!
     * @param  value  DOCUMENT ME!
     */
    public StatusEvent(final String name, final Object value) {
        this.name = name;
        this.setValue(value);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getValue() {
        return value;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  value  DOCUMENT ME!
     */
    public void setValue(final Object value) {
        this.value = value;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name  DOCUMENT ME!
     */
    public void setName(final String name) {
        this.name = name;
    }
}
