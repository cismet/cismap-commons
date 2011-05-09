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

import org.openide.util.lookup.ServiceProvider;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
    private static final String XML_WFS = "exportWFS";
    private static final String XML_WFS_TITLE = "title";
    private static final String XML_WFS_URL = "url";
    private static final String XML_WFS_QUERY = "query";

    private static Set<ExportWFS> wfsList = new TreeSet<ExportWFS>();

    //~ Instance fields --------------------------------------------------------

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

    @Override
    public void configure(final Element parent) {
        // TODO if necessary
    }

    @Override
    public void masterConfigure(final Element parent) {
        final Element cismapShapeExport = parent.getChild(XML_CONF_ROOT);
        if (cismapShapeExport == null) {
            LOG.warn("The shape export isn't configured. The export functionality will not be available.");
            return;
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
                                    + "' is invalid. This WFS will bee skipped for shape export.",
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
        }
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        // throw new UnsupportedOperationException("Not supported yet.");
        // TODO if neccessary
        return null;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public Collection<ToolbarComponentDescription> getToolbarComponents() {
        if (toolbarComponents == null) {
            final List<ToolbarComponentDescription> preparationList = TypeSafeCollections.newArrayList();
            final ToolbarComponentDescription description = new ToolbarComponentDescription(
                    "tlbMain",
                    new JButton(new ShapeExportAction()),
                    ToolbarPositionHint.AFTER,
                    "cmdShapeExport");
            preparationList.add(description);
            toolbarComponents = Collections.unmodifiableList(preparationList);
        }

        return toolbarComponents;
    }
}
