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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.LayerConfig;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.DocumentFeatureService;
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
    private static final String[] SUPPORTED_IMAGE_FORMATS = { "png", "jpg", "tif", "tiff", "gif" };

    //~ Methods ----------------------------------------------------------------

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
        drop(new TransferSupportWrapper(dtde), activeLayerModel, parent, -1);
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
        return drop(new TransferSupportWrapper(support), activeLayerModel, parent, index);
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
    private static boolean drop(final TransferSupportWrapper dtde,
            final ActiveLayerModel activeLayerModel,
            final JComponent parent,
            final int index) {
        final DataFlavor TREEPATH_FLAVOR = new DataFlavor(
                DataFlavor.javaJVMLocalObjectMimeType,
                "SelectionAndCapabilities");                                           // NOI18N
        if (LOG.isDebugEnabled()) {
            LOG.debug("Drop with this flavors:" + dtde.getCurrentDataFlavorsAsList()); // NOI18N
        }
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                    || dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            try {
                List<File> data = null;
                final Transferable transferable = dtde.getTransferable();
                if (dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Drop is unix drop");                                // NOI18N
                    }

                    try {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Drop is Mac drop xxx"
                                        + transferable.getTransferData(DataFlavor.javaFileListFlavor)); // NOI18N
                        }

                        data = (java.util.List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    } catch (Exception e) {
                        // transferable.getTransferData(DataFlavor.javaFileListFlavor) will throw an
                        // UnsupportedFlavorException on Linux
                        if (data == null) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Drop is Linux drop"); // NOI18N
                            }
                            data = DnDUtils.textURIListToFileList((String)transferable.getTransferData(
                                        DnDUtils.URI_LIST_FLAVOR));
                        }
                    }
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Drop is windows drop");       // NOI18N
                    }
                    data = (java.util.List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Drag&Drop File List: " + data);          // NOI18N
                }
                if (data != null) {
                    if (handleFiles(data, activeLayerModel, index, parent)) {
                        return true;
                    }
                } else {
                    LOG.warn("No files available");                     // NOI18N
                }
            } catch (final Exception ex) {
                LOG.error("Failure during drag & drop opertation", ex); // NOI18N
            }
        } else if (dtde.isDataFlavorSupported(TREEPATH_FLAVOR)) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("There are " + dtde.getTransferable().getTransferDataFlavors().length + " DataFlavours"); // NOI18N
                }
                for (int i = 0; i < dtde.getTransferable().getTransferDataFlavors().length; ++i) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("DataFlavour" + i + ": " + dtde.getTransferable().getTransferDataFlavors()[i]); // NOI18N
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
                        final WMSServiceLayer l = new WMSServiceLayer(v);

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
                                    feature);
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

                            return true;
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
                        }
                    }
                } else if (o instanceof LayerConfig) {
                    if (index != -1) {
                        activeLayerModel.addLayer(((LayerConfig)o).createConfiguredLayer(),
                            activeLayerModel.layers.size()
                                    - index);
                    } else {
                        activeLayerModel.addLayer(((LayerConfig)o).createConfiguredLayer());
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
     * Handel the given file. Every file, that is recognised by the DocumentFeatureServiceFactory can be handled.
     *
     * @param   data              DOCUMENT ME!
     * @param   activeLayerModel  DOCUMENT ME!
     * @param   index             DOCUMENT ME!
     * @param   parent            DOCUMENT ME!
     *
     * @return  true, iff a service was added
     */
    private static boolean handleFiles(final List<File> data,
            final ActiveLayerModel activeLayerModel,
            final int index,
            final JComponent parent) {
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
    private static boolean isGeoImage(final String fileName) {
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
        de.cismet.cismap.commons.wms.capabilities.Layer layer = null;

        if (lastPathComponent instanceof de.cismet.cismap.commons.wms.capabilities.Layer) {
            layer = (de.cismet.cismap.commons.wms.capabilities.Layer)lastPathComponent;
        } else {
            return false;
        }

        String titleOrName = layer.getTitle();

        if (titleOrName == null) {
            titleOrName = layer.getName();
        }

        return (titleOrName != null) && titleOrName.endsWith("[]");
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * This class wraps a DropTargetDropEvent or TransferSupport.
     *
     * @version  $Revision$, $Date$
     */
    private static class TransferSupportWrapper {

        //~ Instance fields ----------------------------------------------------

        private DropTargetDropEvent event;
        private TransferHandler.TransferSupport transfer;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new TransferSupportWrapper object.
         *
         * @param  event  DOCUMENT ME!
         */
        public TransferSupportWrapper(final DropTargetDropEvent event) {
            this.event = event;
        }

        /**
         * Creates a new TransferSupportWrapper object.
         *
         * @param  transfer  DOCUMENT ME!
         */
        public TransferSupportWrapper(final TransferSupport transfer) {
            this.transfer = transfer;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   df  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isDataFlavorSupported(final DataFlavor df) {
            if (event != null) {
                return event.isDataFlavorSupported(df);
            } else {
                return transfer.isDataFlavorSupported(df);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  action  DOCUMENT ME!
         */
        public void acceptDrop(final int action) {
            if (event != null) {
                event.acceptDrop(action);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  success  DOCUMENT ME!
         */
        public void dropComplete(final boolean success) {
            if (event != null) {
                event.dropComplete(success);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Transferable getTransferable() {
            if (event != null) {
                return event.getTransferable();
            } else {
                return transfer.getTransferable();
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public List<DataFlavor> getCurrentDataFlavorsAsList() {
            if (event != null) {
                return event.getCurrentDataFlavorsAsList();
            } else {
                return Arrays.asList(transfer.getDataFlavors());
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public DataFlavor[] getCurrentDataFlavors() {
            if (event != null) {
                return event.getCurrentDataFlavors();
            } else {
                return transfer.getDataFlavors();
            }
        }
    }
}
