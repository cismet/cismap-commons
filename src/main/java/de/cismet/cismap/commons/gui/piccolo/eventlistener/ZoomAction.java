/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2010 srichter
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.cismet.cismap.commons.gui.MappingComponent;

/**
 * DOCUMENT ME!
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public final class ZoomAction implements ActionListener {

    //~ Instance fields --------------------------------------------------------

    private final PBounds bounds;
    private final MappingComponent map;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ZoomAction object.
     *
     * @param  bounds       DOCUMENT ME!
     * @param  pInputEvent  DOCUMENT ME!
     */
    public ZoomAction(final PBounds bounds, final PInputEvent pInputEvent) {
        this.bounds = bounds;
        this.map = (MappingComponent)pInputEvent.getComponent();
    }
    /**
     * Creates a new ZoomAction object.
     *
     * @param  bounds  DOCUMENT ME!
     * @param  map     DOCUMENT ME!
     */
    public ZoomAction(final PBounds bounds, final MappingComponent map) {
        this.bounds = bounds;
        this.map = map;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (map != null) {
            map.setNewViewBounds(bounds);
            map.queryServices();
        }
    }
}
