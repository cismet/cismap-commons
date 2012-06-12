/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui;

import com.vividsolutions.jts.geom.Point;

import javax.swing.AbstractAction;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class AbstractMapPopupAction extends AbstractAction implements MapPopupAction {

    //~ Instance fields --------------------------------------------------------

    /** The point where the action is invoked on. */
    private transient Point point;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractMapPopupAction object.
     */
    public AbstractMapPopupAction() {
        super();
    }

    /**
     * Creates a new AbstractMapPopupAction object.
     *
     * @param  name  DOCUMENT ME!
     */
    public AbstractMapPopupAction(final String name) {
        super(name);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Point getPoint() {
        return point;
    }

    @Override
    public void setPoint(final Point point) {
        this.point = point;
    }

    /**
     * {@inheritDoc}<br/>
     * <br/>
     * <b>NOTE:</b>By default this implementation returns <code>true</code> if there is no subjacent feature, <code>
     * false</code> otherwise.
     */
    @Override
    public boolean isActive(final boolean featuresSubjacent) {
        return !featuresSubjacent;
    }

    /**
     * Comparison based on the {@link #getPosition()} operation.
     *
     * @param   other  the other object
     *
     * @return  a negative integer if this object shall be above the given object, a positive integer if this object
     *          shall be below the given object or <code>0</code> if the relative position to each other cannot be
     *          determined/does not matter.
     */
    @Override
    public int compareTo(final MapPopupAction other) {
        return getPosition() - other.getPosition();
    }
}
