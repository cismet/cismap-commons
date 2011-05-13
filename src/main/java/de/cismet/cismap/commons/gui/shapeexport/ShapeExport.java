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

import org.jdom.Element;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;

import de.cismet.cismap.commons.gui.ToolbarComponentDescription;
import de.cismet.cismap.commons.gui.ToolbarComponentsProvider;

import de.cismet.tools.collections.TypeSafeCollections;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = ToolbarComponentsProvider.class)
public class ShapeExport implements Configurable, ToolbarComponentsProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ShapeExport.class);

    private static final String PLUGIN_NAME = "SHAPE_EXPORT";
    private static final String XML_CONF_ROOT = "cismapShapeExport";
    private static final String XML_BBOX_TOKEN = "bboxToken";
    private static final String XML_WFS = "wfs";
    private static final String XML_WFS_TITLE = "title";
    private static final String XML_WFS_URL = "url";
    private static final String XML_WFS_QUERY = "query";
    private static final String XML_DESTINATION = "destination";
    private static final String XML_DIRECTORY = "directory";
    private static final String XML_FILE = "file";
    private static final String XML_EXTENSION = "extension";

    private static Set<ExportWFS> wfsList = new LinkedHashSet<ExportWFS>();
    private static String bboxToken = "<cismap:BBOX/>";
    private static File destinationDirectory;
    private static String destinationFile = "export";
    private static String destinationFileExtension = ".zip";

    //~ Instance fields --------------------------------------------------------

    private boolean enableShapeExport = true;

    private List<ToolbarComponentDescription> toolbarComponents;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Set<ExportWFS> getWFSList() {
        return wfsList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getBboxToken() {
        return bboxToken;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static File getDestinationDirectory() {
        return destinationDirectory;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getDestinationFile() {
        return destinationFile;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getDestinationFileExtension() {
        return destinationFileExtension;
    }

    @Override
    public void configure(final Element parent) {
        // TODO if necessary
    }

    @Override
    public void masterConfigure(final Element parent) {
        final Element cismapShapeExport = parent.getChild(XML_CONF_ROOT);
        if (cismapShapeExport == null) {
            LOG.warn("The shape export isn't configured. The export functionality will not be available.");
            enableShapeExport = false;
            return;
        }

        final Element bboxToken = cismapShapeExport.getChild(XML_BBOX_TOKEN);
        if ((bboxToken == null) || (bboxToken.getText() == null) || (bboxToken.getText().trim().length() == 0)) {
            LOG.warn("There is no replacement token configured for shape export. Using default replacement token '"
                        + this.bboxToken + "'.");
        } else {
            this.bboxToken = bboxToken.getText();
        }

        final List<Element> exportWFSs = cismapShapeExport.getChildren(XML_WFS);

        for (final Element exportWFS : exportWFSs) {
            final Element title = exportWFS.getChild(XML_WFS_TITLE);
            final Element url = exportWFS.getChild(XML_WFS_URL);
            final Element query = exportWFS.getChild(XML_WFS_QUERY);

            if ((title != null) && (url != null)) {
                final String contentOfTitle = title.getText();
                final String contentOfUrl = url.getText();
                final String contentOfQuery = query.getText();

                if ((contentOfTitle != null) && (contentOfTitle.trim().length() > 0)
                            && (contentOfUrl != null)
                            && (contentOfUrl.trim().length() > 0)
                            && (contentOfQuery != null)
                            && (contentOfQuery.trim().length() > 0)) {
                    URL convertedUrl = null;

                    try {
                        convertedUrl = new URL(contentOfUrl);
                    } catch (MalformedURLException e) {
                        LOG.error("The given URL for WFS '" + contentOfTitle
                                    + "' is invalid. This WFS will be skipped for shape export.",
                            e);
                    }

                    if (convertedUrl != null) {
                        wfsList.add(new ExportWFS(contentOfTitle, contentOfQuery, convertedUrl));
                    }
                }
            }
        }

        if (wfsList.isEmpty()) {
            LOG.warn(
                "Could not read the list of WFSs for shape export. The export functionality will not be available.");
            enableShapeExport = false;
        }

        final Element destination = cismapShapeExport.getChild(XML_DESTINATION);
        if (destination == null) {
            LOG.warn(
                "There is no destination directory and file name configured for shape export. Using default directory and default file name.");
        }

        if (destination != null) {
            final Element directory = destination.getChild(XML_DIRECTORY);
            if ((directory == null) || (directory.getText() == null) || (directory.getText().trim().length() == 0)) {
                LOG.warn("There is no destination directory configured for shape export. Using default directory.");
            } else {
                destinationDirectory = new File(System.getProperty("user.home"), directory.getText());
                if (!destinationDirectory.exists()) {
                    destinationDirectory.mkdirs();
                }
            }
        }

        if ((destinationDirectory == null) || !destinationDirectory.exists() || !destinationDirectory.isDirectory()
                    || !destinationDirectory.canWrite()) {
            if (destinationDirectory != null) {
                LOG.warn("The destination directory for shape export '" + destinationDirectory.getAbsolutePath()
                            + "' is invalid. Using '" + System.getProperty("user.home") + File.separatorChar
                            + "cismap' instead.");
            }

            destinationDirectory = new File(System.getProperty("user.home"), "cismap");
            if (!destinationDirectory.exists()) {
                destinationDirectory.mkdirs();
            }
        }

        if (!destinationDirectory.exists() || !destinationDirectory.isDirectory() || !destinationDirectory.canWrite()) {
            LOG.error("The destination directory for shape export '" + destinationDirectory.getAbsolutePath()
                        + "' is invalid. Shape export will be disabled.");
            enableShapeExport = false;
        }

        final Element file = destination.getChild(XML_FILE);
        if ((file == null) || (file.getText() == null) || (file.getText().trim().length() == 0)) {
            LOG.warn("There is no destination file name configured for shape export. Using default file name '"
                        + destinationFile + "'.");
        } else {
            destinationFile = file.getText();
        }

        final Element extension = destination.getChild(XML_EXTENSION);
        if ((extension == null) || (extension.getText() == null) || (extension.getText().trim().length() == 0)) {
            LOG.warn(
                "There is no destination file extension configured for shape export. Using default file extension '"
                        + destinationFileExtension
                        + "'.");
        } else {
            destinationFileExtension = extension.getText();
        }
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        // TODO if neccessary
        return null;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public Collection<ToolbarComponentDescription> getToolbarComponents() {
        if ((toolbarComponents == null) && enableShapeExport) {
            final JButton btnShapeExport = new JButton(new ShapeExportAction());
            btnShapeExport.setText(null);
            btnShapeExport.setName(NbBundle.getMessage(ShapeExport.class, "ShapeExportAction.name"));
            btnShapeExport.setBorderPainted(false);
            btnShapeExport.setFocusable(false);
            btnShapeExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            btnShapeExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            final JButton btnDownloadManager = new JButton(new DownloadManagerAction());
            btnDownloadManager.setText(null);
            btnDownloadManager.setName(NbBundle.getMessage(ShapeExport.class, "DownloadManagerAction.name"));
            btnDownloadManager.setBorderPainted(false);
            btnDownloadManager.setFocusable(false);
            btnDownloadManager.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            btnDownloadManager.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            final List<ToolbarComponentDescription> preparationList = TypeSafeCollections.newArrayList();
            final ToolbarComponentDescription shapeExport = new ToolbarComponentDescription(
                    "tlbMain",
                    btnShapeExport,
                    ToolbarPositionHint.AFTER,
                    "cmdClipboard");
            final ToolbarComponentDescription downloadManager = new ToolbarComponentDescription(
                    "tlbMain",
                    btnDownloadManager,
                    ToolbarPositionHint.AFTER,
                    NbBundle.getMessage(ShapeExport.class, "ShapeExportAction.name"));
            preparationList.add(shapeExport);
            preparationList.add(downloadManager);
            toolbarComponents = Collections.unmodifiableList(preparationList);
        }

        return toolbarComponents;
    }
}
