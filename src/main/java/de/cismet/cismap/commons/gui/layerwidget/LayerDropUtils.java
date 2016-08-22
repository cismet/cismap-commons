/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.layerwidget;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.LayerConfig;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.DocumentFeatureServiceFactory;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.capabilitywidget.SelectionAndCapabilities;
import de.cismet.cismap.commons.gui.capabilitywidget.WFSSelectionAndCapabilities;
import de.cismet.cismap.commons.internaldb.DBTableInformation;
import de.cismet.cismap.commons.raster.wms.SlidableWMSServiceLayerGroup;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.rasterservice.ImageRasterService;
import de.cismet.cismap.commons.util.DnDUtils;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class LayerDropUtils {

    //~ Static fields/initializers ---------------------------------------------

    public static final Logger LOG = Logger.getLogger(LayerDropUtils.class);
    public static final String[] SUPPORTED_IMAGE_FORMATS = { "png", "jpg", "jpeg", "tif", "tiff", "gif" };

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   data              DOCUMENT ME!
     * @param   activeLayerModel  DOCUMENT ME!
     * @param   parent            DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean drop(final Collection<File> data,
            final ActiveLayerModel activeLayerModel,
            final JComponent parent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Drag&Drop File List: " + data); // NOI18N
        }
        if (data != null) {
            if (handleFiles(data, activeLayerModel, -1, parent)) {
                return true;
            }
        } else {
            LOG.warn("No files available");            // NOI18N
        }
        return false;
    }
    /**
     * Handles a layer drop event.
     *
     * @param  dtde              the data of the drop event
     * @param  activeLayerModel  the model to add
     * @param  parent            a component that is used to message dialogs, if required
     */
    public static void drop(final java.awt.dnd.DropTargetDropEvent dtde,
            final ActiveLayerModel activeLayerModel,
            final JComponent parent) {
        drop(new DnDUtils.TransferSupportWrapper(dtde), activeLayerModel, parent, -1);
    }

    /**
     * Handles a layer drop event.
     *
     * @param   support           the data of the drop event
     * @param   activeLayerModel  the model to add
     * @param   index             the index, the dropped element should be added
     * @param   parent            a component that is used to message dialogs, if required
     *
     * @return  true, if the given element was added
     */
    public static boolean drop(final TransferHandler.TransferSupport support,
            final ActiveLayerModel activeLayerModel,
            final int index,
            final JComponent parent) {
        return drop(new DnDUtils.TransferSupportWrapper(support), activeLayerModel, parent, index);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   dtde              the data of the drop event
     * @param   activeLayerModel  the model to add
     * @param   parent            a component that is used to message dialogs, if required
     * @param   index             the index, the dropped element should be added or -1 if it should be added on top
     *
     * @return  DOCUMENT ME!
     */
    private static boolean drop(final DnDUtils.TransferSupportWrapper dtde,
            final ActiveLayerModel activeLayerModel,
            final JComponent parent,
            final int index) {
        final DataFlavor TREEPATH_FLAVOR = new DataFlavor(
                DataFlavor.javaJVMLocalObjectMimeType,
                "SelectionAndCapabilities");                                                                            // NOI18N
        if (LOG.isDebugEnabled()) {
            LOG.debug("Drop with this flavors:" + dtde.getCurrentDataFlavorsAsList());                                  // NOI18N
        }
        if (DnDUtils.isFilesOrUriList(dtde)) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            try {
                final List<File> data = DnDUtils.getFilesFrom(dtde);
                drop(data, activeLayerModel, parent);
            } catch (final Exception ex) {
                LOG.error("Failure during drag & drop opertation", ex);                                                 // NOI18N
            }
        } else if (dtde.isDataFlavorSupported(TREEPATH_FLAVOR)) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("There are " + dtde.getTransferable().getTransferDataFlavors().length + " DataFlavours"); // NOI18N
                }
                for (int i = 0; i < dtde.getTransferable().getTransferDataFlavors().length; ++i) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("DataFlavour" + i + ": " + dtde.getTransferable().getTransferDataFlavors()[i]);       // NOI18N
                    }
                }
                final Object o = dtde.getTransferable().getTransferData(TREEPATH_FLAVOR);
                final List<TreePath> v = new ArrayList<TreePath>();
                dtde.dropComplete(true);
                if (o instanceof SelectionAndCapabilities) {
                    final TreePath[] tpa = ((SelectionAndCapabilities)o).getSelection();
                    for (int i = 0; i < tpa.length; ++i) {
                        v.add(tpa[i]);
                    }

                    if (isSlidableWMSServiceLayerGroup(v.get(0).getLastPathComponent())) {
                        final SlidableWMSServiceLayerGroup l = new SlidableWMSServiceLayerGroup(v);
                        l.setWmsCapabilities(((SelectionAndCapabilities)o).getCapabilities());
                        l.setCapabilitiesUrl(((SelectionAndCapabilities)o).getUrl());
                        if (index != -1) {
                            activeLayerModel.addLayer(l, activeLayerModel.layers.size() - index);
                        } else {
                            activeLayerModel.addLayer(l);
                        }

                        return true;
                    } else {
                        WMSServiceLayer l;

                        if (((SelectionAndCapabilities)o).getUrl().contains("cismap.dont.touch.ordering=true")) {
                            l = new WMSServiceLayer(v, false, false);
                        } else {
                            l = new WMSServiceLayer(v, true, true);
                        }

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("((SelectionAndCapabilities)o).getUrl()"
                                        + ((SelectionAndCapabilities)o).getUrl()); // NOI18N
                        }

                        l.setWmsCapabilities(((SelectionAndCapabilities)o).getCapabilities());
                        l.setCapabilitiesUrl(((SelectionAndCapabilities)o).getUrl());

                        if (index != -1) {
                            activeLayerModel.addLayer(l, activeLayerModel.layers.size() - index);
                        } else {
                            activeLayerModel.addLayer(l);
                        }

                        return true;
                    }
                } else if (o instanceof WFSSelectionAndCapabilities) { // Drop-Objekt war ein WFS-Element
                    final WFSSelectionAndCapabilities sac = (WFSSelectionAndCapabilities)o;

                    for (final FeatureType feature : sac.getFeatures()) {
                        try {
                            final WebFeatureService wfs = new WebFeatureService(feature.getPrefixedNameString(),
                                    feature.getWFSCapabilities().getURL().toString(),
                                    feature.getWFSQuery(),
                                    feature.getFeatureAttributes(),
                                    feature,
                                    sac.isReverseAxisOrder());
                            if ((sac.getIdentifier() != null) && (sac.getIdentifier().length() > 0)) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("setting PrimaryAnnotationExpression of WFS Layer to '"
                                                + sac.getIdentifier()
                                                + "' (EXPRESSIONTYPE_PROPERTYNAME)");        // NOI18N
                                }
                                wfs.getLayerProperties()
                                        .setPrimaryAnnotationExpression(sac.getIdentifier(),
                                            LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
                            } else {
                                LOG.warn("could not determine PrimaryAnnotationExpression"); // NOI18N
                            }

                            if (index != -1) {
                                activeLayerModel.addLayer(wfs, activeLayerModel.layers.size() - index);
                            } else {
                                activeLayerModel.addLayer(wfs);
                            }
                        } catch (final IllegalArgumentException schonVorhanden) {
                            JOptionPane.showMessageDialog(
                                parent,
                                org.openide.util.NbBundle.getMessage(
                                    LayerWidget.class,
                                    "LayerWidget.drop(DropTargetDropEvent).JOptionPane.message"), // NOI18N
                                org.openide.util.NbBundle.getMessage(
                                    LayerWidget.class,
                                    "LayerWidget.drop(DropTargetDropEvent).JOptionPane.title"), // NOI18N
                                JOptionPane.ERROR_MESSAGE);

                            return false;
                        }
                    }

                    return true;
                } else if (o instanceof LayerConfig[]) {
                    for (final LayerConfig config : (LayerConfig[])o) {
                        if (index != -1) {
                            activeLayerModel.addLayer((config).createConfiguredLayer(),
                                activeLayerModel.layers.size()
                                        - index);
                        } else {
                            activeLayerModel.addLayer((config).createConfiguredLayer());
                        }
                    }

                    return true;
                } else if (o instanceof File) {
                    final List<File> list = new ArrayList<File>(1);
                    list.add((File)o);

                    if (handleFiles(list, activeLayerModel, index, parent)) {
                        return true;
                    }
                } else if (o instanceof DBTableInformation[]) {
                    final DBTableInformation[] infos = (DBTableInformation[])o;

                    for (final DBTableInformation i : infos) {
                        if (i.isFolder()) {
                            final LayerCollection lc = createH2Folder(i);
                            lc.setModel(activeLayerModel);

                            if (index != -1) {
                                activeLayerModel.addLayer(lc, activeLayerModel.layers.size() - index);
                            } else {
                                activeLayerModel.addLayer(lc, 0);
                            }
                        } else {
                            final H2FeatureService layer = new H2FeatureService(i.getName(),
                                    i.getDatabasePath(),
                                    i.getDatabaseTable(),
                                    null);

                            if (index != -1) {
                                activeLayerModel.addLayer(layer, activeLayerModel.layers.size() - index);
                            } else {
                                activeLayerModel.addLayer(layer);
                            }
                        }
                    }
                }
            } catch (final IllegalArgumentException schonVorhanden) {
                JOptionPane.showMessageDialog(
                    parent,
                    org.openide.util.NbBundle.getMessage(
                        LayerWidget.class,
                        "LayerWidget.drop(DropTargetDropEvent).JOptionPane.message"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        LayerWidget.class,
                        "LayerWidget.drop(DropTargetDropEvent).JOptionPane.title"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            } catch (final Exception e) {
                LOG.error(e, e);
            }
        } else {
            LOG.warn("No Matching dataFlavour: " + dtde.getCurrentDataFlavorsAsList()); // NOI18N
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   i  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static LayerCollection createH2Folder(final DBTableInformation i) throws Exception {
        final LayerCollection lc = new LayerCollection();
        lc.setName(i.getName());

        for (final DBTableInformation tmp : i.getChildren()) {
            if (tmp.isFolder()) {
                lc.add(createH2Folder(tmp));
            } else {
                final H2FeatureService layer = new H2FeatureService(tmp.getName(),
                        tmp.getDatabasePath(),
                        tmp.getDatabaseTable(),
                        null);

                lc.add(layer);
            }
        }

        return lc;
    }

    /**
     * Handel the given file. Every file, that is recognised by the DocumentFeatureServiceFactory can be handled.
     *
     * @param   data              DOCUMENT ME!
     * @param   activeLayerModel  DOCUMENT ME!
     * @param   index             DOCUMENT ME!
     * @param   parent            DOCUMENT ME!
     *
     * @return  true, iff a service was added
     */
    public static boolean handleFiles(final Collection<File> data,
            final ActiveLayerModel activeLayerModel,
            final int index,
            final Component parent) {
        for (final File currentFile : data) {
            // NO HARDCODING
            try {
                LOG.info("DocumentUri: " + currentFile.toURI()); // NOI18N

                if (isGeoImage(currentFile.getName())) {
                    final ImageRasterService irs = new ImageRasterService(currentFile);

                    if (index != -1) {
                        activeLayerModel.addLayer(irs, activeLayerModel.layers.size() - index);
                    } else {
                        activeLayerModel.addLayer(irs);
                    }
                } else {
                    final AbstractFeatureService dfs = DocumentFeatureServiceFactory.createDocumentFeatureService(
                            currentFile);
                    if (index != -1) {
                        activeLayerModel.addLayer(dfs, activeLayerModel.layers.size() - index);
                    } else {
                        activeLayerModel.addLayer(dfs);
                    }

                    if (dfs instanceof ShapeFileFeatureService) {
                        new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    do {
                                        try {
                                            Thread.sleep(500);
                                        } catch (final InterruptedException e) {
                                            // nothing to do
                                        }
                                    } while (!dfs.isInitialized());

                                    if (((ShapeFileFeatureService)dfs).isErrorInGeometryFound()) {
                                        JOptionPane.showMessageDialog(
                                            StaticSwingTools.getParentFrame(parent),
                                            NbBundle.getMessage(
                                                LayerWidget.class,
                                                "LayerWidget.drop().errorInShapeGeometryFoundMessage"),
                                            NbBundle.getMessage(
                                                LayerWidget.class,
                                                "LayerWidget.drop().errorInShapeGeometryFoundTitle"),
                                            JOptionPane.ERROR_MESSAGE);
                                    } else if (((ShapeFileFeatureService)dfs).isNoGeometryRecognised()) {
                                        JOptionPane.showMessageDialog(
                                            StaticSwingTools.getParentFrame(parent),
                                            NbBundle.getMessage(
                                                LayerWidget.class,
                                                "LayerWidget.drop().noGeometryFoundInShapeMessage"),
                                            NbBundle.getMessage(
                                                LayerWidget.class,
                                                "LayerWidget.drop().noGeometryFoundInShapeTitle"),
                                            JOptionPane.WARNING_MESSAGE);
                                    }
                                }
                            }).start();

                        return true;
                    }
                }
            } catch (final Exception ex) {
                LOG.error("Error during creation of a FeatureServices", ex); // NOI18N
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fileName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean isGeoImage(final String fileName) {
        for (final String ending : SUPPORTED_IMAGE_FORMATS) {
            if (fileName.endsWith(ending)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks, if the given object is a SlidableWMSServiceLayerGroup.
     *
     * @param   lastPathComponent  the object to check
     *
     * @return  true, if the given object is a SlidableWMSServiceLayerGroup
     */
    private static boolean isSlidableWMSServiceLayerGroup(final Object lastPathComponent) {
        de.cismet.cismap.commons.wms.capabilities.deegree.DeegreeLayer layer = null;

        if (lastPathComponent instanceof de.cismet.cismap.commons.wms.capabilities.deegree.DeegreeLayer) {
            layer = (de.cismet.cismap.commons.wms.capabilities.deegree.DeegreeLayer)lastPathComponent;
        } else {
            return false;
        }

        final List<String> keywords = Arrays.asList(layer.getKeywords());

        return keywords.contains("cismapSlidingLayerGroup");
    }
}
