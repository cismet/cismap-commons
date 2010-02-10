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

import de.cismet.cismap.commons.gui.MappingComponent;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author srichter
 */
public final class ZoomAction implements ActionListener {

    public ZoomAction(PBounds bounds, PInputEvent pInputEvent) {
        this.bounds = bounds;
        this.pInputEvent = pInputEvent;
    }
    private final PBounds bounds;
    private final PInputEvent pInputEvent;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            MappingComponent map = (MappingComponent) pInputEvent.getComponent();
            map.setNewViewBounds(bounds);
            map.queryServices();
        }
    }
}
