/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2011 thorsten
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
package de.cismet.cismap.commons.features;

import org.openide.util.lookup.ServiceProvider;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CommonFeatureAction.class)
public class CommonDemoAction extends AbstractAction implements CommonFeatureAction {

    //~ Instance fields --------------------------------------------------------

    Feature source = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CommonDemoAction object.
     */
    public CommonDemoAction() {
        super("Demo");
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        JOptionPane.showMessageDialog(CismapBroker.getInstance().getMappingComponent(),
            source.getGeometry().getGeometryType());
    }

    @Override
    public Feature getSourceFeature() {
        return source;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void setSourceFeature(final Feature source) {
        this.source = source;
    }

    @Override
    public int getSorter() {
        return 10;
    }
}
