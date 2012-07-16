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

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.event.ActionEvent;

import java.io.File;

import java.util.Collection;
import java.util.LinkedList;

import javax.swing.AbstractAction;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.downloadmanager.Download;
import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
import de.cismet.tools.gui.downloadmanager.HttpDownload;
import de.cismet.tools.gui.downloadmanager.MultipleDownload;

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

        XBoundingBox boundingBox = (XBoundingBox)CismapBroker.getInstance().getMappingComponent()
                    .getCurrentBoundingBox();

        wfsList = dialog.getSelectedWFSs();
        for (final ExportWFS wfs : wfsList) {
            if (wfs.getTargetCRS() != null) {
                final Geometry g = CrsTransformer.transformToGivenCrs(boundingBox.getGeometry(), wfs.getTargetCRS());
                boundingBox = new XBoundingBox(g);
            }

            wfs.setQuery(wfs.getQuery().replace(ShapeExport.getBboxToken(), boundingBox.toGml4WFS110String()));
        }

        if (!DownloadManagerDialog.showAskingForUserTitle(CismapBroker.getInstance().getMappingComponent())) {
            return;
        }
        final String jobname = DownloadManagerDialog.getJobname();

        DownloadManager.instance().add(convertToDownloads(wfsList, jobname));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   wfss     DOCUMENT ME!
     * @param   jobname  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Download convertToDownloads(final Collection<ExportWFS> wfss, final String jobname) {
        final Collection<HttpDownload> downloads = new LinkedList<HttpDownload>();

        final String filenameFromShapeExport = ShapeExport.getDestinationFile();
        final String extension = ShapeExport.getDestinationFileExtension();
        String destinationDirectory = ShapeExport.getDestinationDirectory();
        if ((jobname != null) && (jobname.trim().length() > 0)) {
            destinationDirectory = destinationDirectory.concat(File.separator).concat(jobname);
        }

        for (final ExportWFS wfs : wfss) {
            String filenameForDownload = wfs.getFile();
            if ((filenameForDownload == null) || (filenameForDownload.trim().length() == 0)) {
                filenameForDownload = filenameFromShapeExport;
            }

            final HttpDownload download = new HttpDownload(wfs.getUrl(),
                    wfs.getQuery(),
                    destinationDirectory,
                    wfs.getTopic(),
                    filenameForDownload,
                    extension);

            downloads.add(download);
        }

        if (downloads.size() == 1) {
            return downloads.iterator().next();
        } else {
            return new MultipleDownload(downloads, jobname);
        }
    }
}
