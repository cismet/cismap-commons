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

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.event.ActionEvent;

import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.downloadmanager.Download;
import de.cismet.cismap.commons.gui.downloadmanager.DownloadManager;
import de.cismet.cismap.commons.gui.downloadmanager.DownloadManagerDialog;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * This action is responsible for the the steps to be done when the user wants to start a shape export.
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
        super();
        putValue(
            SMALL_ICON,
            new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/res/shapeexport_small.png")));
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(ShapeExportAction.class, "ShapeExportAction.tooltiptext"));
        putValue(NAME, NbBundle.getMessage(ShapeExportAction.class, "ShapeExportAction.name"));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        Collection<ExportWFS> wfsList = null;
        final Component parent = StaticSwingTools.getParentFrame(
                CismapBroker.getInstance().getMappingComponent());

        final ShapeExportDialog dialog = new ShapeExportDialog(CismapBroker.getInstance().getMappingComponent(),
                ShapeExport.getWFSList());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        if (dialog.isCancelled()) {
            return;
        }

        final XBoundingBox boundingBox = (XBoundingBox)CismapBroker.getInstance().getMappingComponent()
                    .getCurrentBoundingBox();
        wfsList = dialog.getSelectedWFSs();
        for (final ExportWFS wfs : wfsList) {
            wfs.setQuery(wfs.getQuery().replace(ShapeExport.getBboxToken(), boundingBox.toGml4WFS110String()));
        }

        DownloadManager.instance().add(convertToDownloads(wfsList));

        final JDialog downloadManager = DownloadManagerDialog.instance(StaticSwingTools.getParentFrame(
                    CismapBroker.getInstance().getMappingComponent()));
        if (!downloadManager.isVisible()) {
            downloadManager.setLocationRelativeTo(parent);
            downloadManager.setVisible(true);
            downloadManager.pack();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   wfss  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Download> convertToDownloads(final Collection<ExportWFS> wfss) {
        final Collection<Download> result = new LinkedList<Download>();

        final String directory = ShapeExport.getDestinationDirectory().getAbsolutePath();
        final String filenameFromShapeExport = ShapeExport.getDestinationFile();
        final String extension = ShapeExport.getDestinationFileExtension();

        for (final ExportWFS wfs : wfss) {
            Download download = null;
            final String filenameFromWFS = wfs.getFile();
            if ((filenameFromWFS == null) || (filenameFromWFS.trim().length() == 0)) {
                download = new Download(wfs.getUrl(),
                        wfs.getQuery(),
                        wfs.getTopic(),
                        directory,
                        filenameFromShapeExport,
                        extension);
            } else {
                download = new Download(wfs.getUrl(),
                        wfs.getQuery(),
                        wfs.getTopic(),
                        directory,
                        wfs.getFile(),
                        extension);
            }

            result.add(download);
        }

        return result;
    }
}
