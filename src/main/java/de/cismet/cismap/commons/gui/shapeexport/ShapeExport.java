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
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.collections.TypeSafeCollections;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerAction;

/**
 * This class configures the shape export functionality in cismap. Therefore it reads the corresponding part of
 * defaultCismapProperties.xml and provides the buttons for cismap's toolbar.
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
    private static final String XML_WFS_FILE = "file";
    private static final String XML_EXTENSION = "extension";

    private static Set<ExportWFS> wfsList = new LinkedHashSet<ExportWFS>();
    private static String bboxToken = "<cismap:BBOX/>";
    private static File destinationDirectory = new File(System.getProperty("user.dir"));
    private static String destinationFile = "export";
    private static String destinationFileExtension = ".zip";

    private static boolean enableShapeExport = true;

    //~ Instance fields --------------------------------------------------------

    private List<ToolbarComponentDescription> toolbarComponents;

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns a Set of all configured export topics. The Set is ordered by the appearance of the topics in the config
     * file.
     *
     * @return  A Set of available topics.
     */
    public static Set<ExportWFS> getWFSList() {
        return wfsList;
    }

    /**
     * Returns the string which serves as a replacement token to put a bounding box in the configured WFS queries. This
     * token is later replaced by a XBoundingBox's GML string.
     *
     * @return  The replacement token for a bounding box in a WFS query.
     */
    public static String getBboxToken() {
        return bboxToken;
    }

    /**
     * Shape exports shall be saved in a directory somewhere in user's home directory. This method will return a file
     * object pointing to that directory. This directory is already tested for existence and, if necessary, created.
     *
     * @return  A file object pointing to the directory where to place exports.
     */
    public static File getDestinationDirectory() {
        return destinationDirectory;
    }

    /**
     * Returns a String object which denotes the file name which is to be used for shape exports.
     *
     * @return  The file name for shape exports.
     */
    public static String getDestinationFile() {
        return destinationFile;
    }

    /**
     * Returns the configured extension for shape exports. Usually it is set to ".zip".
     *
     * @return  The file extension for shape exports.
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

        final Element bbox = cismapShapeExport.getChild(XML_BBOX_TOKEN);
        if ((bbox == null) || (bbox.getText() == null) || (bbox.getText().trim().length() == 0)) {
            LOG.warn("There is no replacement token configured for shape export. Using default replacement token '"
                        + bboxToken + "'.");
        } else {
            bboxToken = bbox.getText();
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

        final List<Element> exportWFSs = cismapShapeExport.getChildren(XML_WFS);

        for (final Element exportWFS : exportWFSs) {
            final Element title = exportWFS.getChild(XML_WFS_TITLE);
            final Element wfsFile = exportWFS.getChild(XML_WFS_FILE);
            final Element url = exportWFS.getChild(XML_WFS_URL);
            final Element query = exportWFS.getChild(XML_WFS_QUERY);

            if ((title != null) && (url != null) && (query != null)) {
                final String contentOfTitle = title.getText();
                final String contentOfUrl = url.getText();
                final String contentOfQuery = query.getText();
                String contentOfWfsFile = destinationFile;
                if ((wfsFile != null) && (wfsFile.getText() != null) && (wfsFile.getText().trim().length() > 0)) {
                    contentOfWfsFile = wfsFile.getText();
                }

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
                        wfsList.add(new ExportWFS(contentOfTitle, contentOfWfsFile, contentOfQuery, convertedUrl));
                    }
                }
            }
        }

        if (wfsList.isEmpty()) {
            LOG.warn(
                "Could not read the list of WFSs for shape export. The export functionality will not be available.");
            enableShapeExport = false;
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
        if ((toolbarComponents == null) && enableShapeExport && DownloadManager.instance().isEnabled()) {
            final JButton btnShapeExport = new JButton(new ShapeExportAction());
            btnShapeExport.setText(null);
            btnShapeExport.setName(NbBundle.getMessage(ShapeExportAction.class, "ShapeExportAction.name"));
            btnShapeExport.setBorderPainted(false);
            btnShapeExport.setFocusable(false);
            btnShapeExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            btnShapeExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
            final JButton btnDownloadManager = new JButton(new DownloadManagerAction(
                        CismapBroker.getInstance().getMappingComponent()));
            btnDownloadManager.setText(null);
            btnDownloadManager.setName(NbBundle.getMessage(DownloadManagerAction.class, "DownloadManagerAction.name"));
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
                    NbBundle.getMessage(ShapeExportAction.class, "ShapeExportAction.name"));
            preparationList.add(shapeExport);
            preparationList.add(downloadManager);
            toolbarComponents = Collections.unmodifiableList(preparationList);
        }

        return toolbarComponents;
    }
}
