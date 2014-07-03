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
package de.cismet.cismap.actions;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.cismet.cismap.commons.features.CommonFeatureAction;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.UpdateablePostgisFeature;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CommonFeatureAction.class)
public class UpdateablePostgisFeatureAction extends AbstractAction implements CommonFeatureAction {

    //~ Instance fields --------------------------------------------------------

    Feature f = null;

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DuplicateGeometryFeatureAction object.
     */
    public UpdateablePostgisFeatureAction() {
        super(NbBundle.getMessage(
                UpdateablePostgisFeatureAction.class,
                "UpdateablePostgisFeatureAction.UpdateablePostgisFeatureAction()"));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getSorter() {
        return 10;
    }

    @Override
    public Feature getSourceFeature() {
        return f;
    }

    @Override
    public boolean isActive() {
        return (f instanceof UpdateablePostgisFeature);
    }

    @Override
    public void setSourceFeature(final Feature source) {
        f = source;
        // now change the icon and title according to the selected feature
        if (f instanceof UpdateablePostgisFeature) {
            putValue(Action.NAME, ((UpdateablePostgisFeature)f).getAction().getActionText());
            putValue(Action.SMALL_ICON, ((UpdateablePostgisFeature)f).getAction().getIcon());
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (f instanceof UpdateablePostgisFeature) {
            ((UpdateablePostgisFeature)f).doUpdate();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final WaitDialog w = new WaitDialog();
        w.setVisible(true);
    }
}
