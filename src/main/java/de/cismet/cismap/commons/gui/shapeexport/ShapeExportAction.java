/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2011 jweintraut
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
package de.cismet.cismap.commons.gui.shapeexport;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class ShapeExportAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ShapeExportAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeExportAction object.
     */
    public ShapeExportAction() {
        super("Shape-Export :)");
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        Collection<ExportWFS> wfsList = null;

        final ShapeExportDialog dialog = new ShapeExportDialog(CismapBroker.getInstance().getMappingComponent(),
                ShapeExport.getWFSList());
        findOptimalPositionOnScreen(dialog);
        dialog.setVisible(true);

        if (dialog.isCancelled()) {
            return;
        }

        final XBoundingBox boundingBox = (XBoundingBox)CismapBroker.getInstance().getMappingComponent()
                    .getCurrentBoundingBox();
        wfsList = dialog.getSelectedWFSs();
        for (final ExportWFS wfs : wfsList) {
            wfs.getQuery().replace("<cismap:BBOX/>", boundingBox.toGmlString());
            LOG.fatal(wfs + ": " + wfs.getQuery());
            System.out.println(wfs + ": " + wfs.getQuery());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  component  DOCUMENT ME!
     */
    public static void findOptimalPositionOnScreen(final JDialog component) {
        final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        // component.setSize(screenSize.width / 2, screenSize.height / 2);
        final java.awt.Insets insets = component.getInsets();
        /*component.setSize(component.getWidth() + insets.left + insets.right,
         *  component.getHeight()         + insets.top         + insets.bottom         + 20);*/
        component.setLocation((screenSize.width - component.getWidth()) / 2,
            (screenSize.height - component.getHeight())
                    / 2);
    }
}
