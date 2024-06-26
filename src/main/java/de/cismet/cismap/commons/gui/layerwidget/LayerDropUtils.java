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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import org.apache.log4j.Logger;

import org.deegree.ogcwebservices.wms.capabilities.Layer;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.LayerConfig;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.DocumentFeatureServiceFactory;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.LayerAlreadyAddedException;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.WFSCapabilitiesTreeCellRenderer;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.capabilitywidget.CidsLayerTransferable;
import de.cismet.cismap.commons.gui.capabilitywidget.SelectionAndCapabilities;
import de.cismet.cismap.commons.gui.capabilitywidget.TreeFolder;
import de.cismet.cismap.commons.gui.capabilitywidget.WFSSelectionAndCapabilities;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.internaldb.DBTableInformation;
import de.cismet.cismap.commons.raster.wms.SlidableWMSServiceLayerGroup;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.rasterservice.ImageFileUtils;
import de.cismet.cismap.commons.rasterservice.ImageRasterService;
import de.cismet.cismap.commons.tools.FeatureTools;
import de.cismet.cismap.commons.util.DnDUtils;
import de.cismet.cismap.commons.wfs.capabilities.FeatureType;

import de.cismet.commons.cismap.io.converters.GeometriesFromGPXConverter;
import de.cismet.commons.cismap.io.converters.MultiGeometriesProvider;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class LayerDropUtils {

    //~ Static fields/initializers ---------------------------------------------

    public static final Logger LOG = Logger.getLogger(LayerDropUtils.class);

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

                        if (CismapBroker.getInstance().isWMSLayerNamesWithPath() && (v.size() == 1)) {
                            final List<de.cismet.commons.wms.capabilities.Layer> parents = new ArrayList<>();
                            TreePath tp = v.get(0);

                            if ((tp.getLastPathComponent() instanceof de.cismet.commons.wms.capabilities.Layer)) {
                                parents.add((de.cismet.commons.wms.capabilities.Layer)tp.getLastPathComponent());
                            }

                            while ((tp.getParentPath() != null)
                                        && (tp.getParentPath().getLastPathComponent()
                                            instanceof de.cismet.commons.wms.capabilities.Layer)) {
                                parents.add((de.cismet.commons.wms.capabilities.Layer)tp.getParentPath()
                                            .getLastPathComponent());
                                tp = tp.getParentPath();
                            }

                            Collections.reverse(parents);
                            StringBuffer parentsString = null;

                            for (final de.cismet.commons.wms.capabilities.Layer p : parents) {
                                if (parentsString == null) {
                                    parentsString = new StringBuffer(p.getTitle());
                                } else {
                                    parentsString.append("->").append(p.getTitle());
                                }
                            }

                            l.setName(parentsString.toString());
                        }

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
//                            final WebFeatureService wfs = new WebFeatureService(feature.getName().getLocalPart(),
//                                    feature.getWFSCapabilities().getURL().toString(),
//                                    feature.getWFSQuery(),
//                                    feature.getFeatureAttributes(),
//                                    feature,
//                                    sac.isReverseAxisOrder());
                            final String hostname = ((feature.getWFSCapabilities().getOriginalLink() != null)
                                    ? feature.getWFSCapabilities().getOriginalLink()
                                    : feature.getWFSCapabilities().getURL().toString());
                            final WebFeatureService wfs = new WebFeatureService(
                                    ((WFSCapabilitiesTreeCellRenderer.showTitle && (feature.getTitle() != null))
                                        ? feature.getTitle() : feature.getPrefixedNameString()),
                                    hostname,
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
                } else if (o instanceof CidsLayerTransferable[]) {
                    for (int i = ((CidsLayerTransferable[])o).length - 1; i >= 0; --i) {
                        final CidsLayerTransferable transferable = ((CidsLayerTransferable[])o)[i];

                        if (transferable.isFolder()) {
                            final TreeFolder folder = transferable.getFolder();

                            final LayerCollection lc = createTreeFolder(folder);
                            lc.setModel(activeLayerModel);

                            if (index != -1) {
                                activeLayerModel.addLayer(lc, activeLayerModel.layers.size() - index);
                            } else {
                                activeLayerModel.addLayer(lc, 0);
                            }
                        } else {
                            final LayerConfig config = transferable.getLayerConfig();
                            if (index != -1) {
                                activeLayerModel.addLayer((config).createConfiguredLayer(),
                                    activeLayerModel.layers.size()
                                            - index);
                            } else {
                                activeLayerModel.addLayer((config).createConfiguredLayer());
                            }
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

                    for (int n = infos.length - 1; n >= 0; --n) {
                        final DBTableInformation i = infos[n];
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
     * DOCUMENT ME!
     *
     * @param   folder  i DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static LayerCollection createTreeFolder(final TreeFolder folder) throws Exception {
        final LayerCollection lc = new LayerCollection();
        lc.setName(folder.getName());

        for (final Object tmp : folder) {
            if (tmp instanceof TreeFolder) {
                lc.add(createTreeFolder((TreeFolder)tmp));
            } else {
                lc.add(((LayerConfig)tmp).createConfiguredLayer());
            }
        }

        return lc;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   data              DOCUMENT ME!
     * @param   activeLayerModel  DOCUMENT ME!
     * @param   index             DOCUMENT ME!
     * @param   parent            DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean handleFiles(final Collection<File> data,
            final ActiveLayerModel activeLayerModel,
            final int index,
            final Component parent) {
        boolean success = false;
        for (final File currentFile : data) {
            if (handleFile(currentFile, activeLayerModel, index, parent)) {
                success = true;
            }
        }
        return success;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   currentFile       DOCUMENT ME!
     * @param   activeLayerModel  DOCUMENT ME!
     * @param   index             DOCUMENT ME!
     * @param   parent            DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean handleFile(final File currentFile,
            final ActiveLayerModel activeLayerModel,
            final int index,
            final Component parent) {
        LOG.info("DocumentUri: " + currentFile.toURI()); // NOI18N

        if (ImageFileUtils.isImageFileEnding(currentFile.getName())) {
            return handleImageFile(
                    currentFile,
                    activeLayerModel,
                    index,
                    parent,
                    ImageFileUtils.determineMode(currentFile));
        } else {
            return handleFeatureServiceFile(currentFile, activeLayerModel, index, parent);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   currentFile       DOCUMENT ME!
     * @param   activeLayerModel  DOCUMENT ME!
     * @param   index             DOCUMENT ME!
     * @param   parent            DOCUMENT ME!
     * @param   imageFileMode     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean handleImageFile(final File currentFile,
            final ActiveLayerModel activeLayerModel,
            final int index,
            final Component parent,
            final ImageFileUtils.Mode imageFileMode) {
        final ImageRasterService irs = new ImageRasterService(currentFile, imageFileMode);

        if (index != -1) {
            activeLayerModel.addLayer(irs, activeLayerModel.layers.size() - index);
        } else {
            activeLayerModel.addLayer(irs);
        }
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   currentFile       DOCUMENT ME!
     * @param   activeLayerModel  DOCUMENT ME!
     * @param   index             DOCUMENT ME!
     * @param   parent            DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean handleGPXFile(final File currentFile,
            final ActiveLayerModel activeLayerModel,
            final int index,
            final Component parent) {
        final GeometriesFromGPXConverter converter = new GeometriesFromGPXConverter();

        final WaitingDialogThread<Geometry> wdt = new WaitingDialogThread<Geometry>(
                StaticSwingTools.getFirstParentFrame(parent),
                true,
                NbBundle.getMessage(
                    LayerDropUtils.class,
                    "LayerDropUtils.handleGPXFile().waitingDialogThread.message"), // NOI18N
                null,
                50) {

                @Override
                protected Geometry doInBackground() throws Exception {
                    final BufferedReader fileReader = new BufferedReader(new FileReader(currentFile));
                    final StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = fileReader.readLine()) != null) {
                        sb.append(line).append('\n');
                    }

                    final Geometry geom = converter.convertForward(sb.toString(),
                            CismapBroker.getInstance().getSrs().getCode());

                    return geom;
                }

                @Override
                protected void done() {
                    try {
                        final Geometry geom = get();
                        final MappingComponent map = CismapBroker.getInstance().getMappingComponent();

                        final List<PureNewFeature> featureList = new ArrayList<PureNewFeature>();
                        String featureName = currentFile.getName();

                        if (featureName.contains(".")) {
                            featureName = featureName.substring(0, currentFile.getName().indexOf("."));
                        }

                        if ((converter instanceof MultiGeometriesProvider)
                                    && (geom instanceof GeometryCollection)) {
                            final GeometryCollection gc = (GeometryCollection)geom;

                            for (int i = 0; i < gc.getNumGeometries(); ++i) {
                                final PureNewFeature feature = new PureNewFeature(gc.getGeometryN(i));
                                feature.setGeometryType(FeatureTools.getGeomType(gc.getGeometryN(i)));
                                feature.setEditable(true);
                                feature.setName(featureName + "-" + i);
                                featureList.add(feature);
                            }
                        } else {
                            final PureNewFeature feature = new PureNewFeature(geom);
                            feature.setGeometryType(FeatureTools.getGeomType(geom));
                            feature.setEditable(true);
                            feature.setName(featureName);
                            featureList.add(feature);
                        }

                        map.getFeatureCollection().addFeatures(featureList);

                        for (final PureNewFeature feature : featureList) {
                            map.getFeatureCollection().holdFeature(feature);
                        }

                        // fixed extent means, don't move map at all
                        if (!map.isFixedMapExtent()) {
                            map.zoomToAFeatureCollection(featureList,
                                true,
                                map.isFixedMapScale());
                        }
                    } catch (Exception e) {
                        LOG.error("Error whiel parsing gpx file");
                    }
                }
            };

        if (EventQueue.isDispatchThread()) {
            wdt.start();
        } else {
            EventQueue.invokeLater(new Thread("GPXParser-Thread") {

                    @Override
                    public void run() {
                        wdt.start();
                    }
                });
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   currentFile       DOCUMENT ME!
     * @param   activeLayerModel  DOCUMENT ME!
     * @param   index             DOCUMENT ME!
     * @param   parent            DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean handleFeatureServiceFile(final File currentFile,
            final ActiveLayerModel activeLayerModel,
            final int index,
            final Component parent) {
        try {
            final AbstractFeatureService dfs = DocumentFeatureServiceFactory.createDocumentFeatureService(
                    currentFile);

            try {
                if (index != -1) {
                    activeLayerModel.addLayer(dfs, activeLayerModel.layers.size() - index);
                } else {
                    activeLayerModel.addLayer(dfs);
                }
            } catch (final IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(
                    parent,
                    org.openide.util.NbBundle.getMessage(
                        LayerWidget.class,
                        "LayerWidget.drop(DropTargetDropEvent).JOptionPane.message"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        LayerWidget.class,
                        "LayerWidget.drop(DropTargetDropEvent).JOptionPane.title"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
                throw ex;
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
            }
        } catch (LayerAlreadyAddedException e) {
            // nothing to do. This exception means that the layer should not be added to the layer model, because it is
            // already there
        } catch (final Exception ex) {
            LOG.error("Error during creation of a FeatureServices", ex); // NOI18N
            return false;
        }
        return true;
    }

    /**
     * Checks, if the given object is a SlidableWMSServiceLayerGroup.
     *
     * @param   lastPathComponent  the object to check
     *
     * @return  true, if the given object is a SlidableWMSServiceLayerGroup
     */
    private static boolean isSlidableWMSServiceLayerGroup(final Object lastPathComponent) {
        de.cismet.commons.wms.capabilities.deegree.DeegreeLayer layer = null;

        if (lastPathComponent instanceof de.cismet.commons.wms.capabilities.deegree.DeegreeLayer) {
            layer = (de.cismet.commons.wms.capabilities.deegree.DeegreeLayer)lastPathComponent;
        } else {
            return false;
        }

        final List<String> keywords = Arrays.asList(layer.getKeywords());

        return keywords.contains("cismapSlidingLayerGroup");
    }
}
