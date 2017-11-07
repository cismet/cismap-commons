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
package de.cismet.cismap.commons.tools;

import com.vividsolutions.jts.geom.Geometry;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.PersistentFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToLayerWorker;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.util.FilePersistenceManager;

import de.cismet.tools.gui.downloadmanager.AbstractCancellableDownload;
import de.cismet.tools.gui.downloadmanager.Download;

/**
 * Every ExportDownload class needs a public constructor without arguments.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class ExportDownload extends AbstractCancellableDownload {

    //~ Static fields/initializers ---------------------------------------------

    private static int currentId = 0;

    //~ Instance fields --------------------------------------------------------

    protected FeatureServiceFeature[] features;
    protected AbstractFeatureService service;
    protected List<String[]> aliasAttributeList;
    protected String extension;
    private boolean absoluteFileName;
    private final int id = getId();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static synchronized int getId() {
        return ++currentId;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  filename            DOCUMENT ME!
     * @param  extension           DOCUMENT ME!
     * @param  features            DOCUMENT ME!
     * @param  service             DOCUMENT ME!
     * @param  aliasAttributeList  A list with string arrays. Every array should have 2 elements. The first element is
     *                             the alias of the column and the second element is the name of the attribute, that
     *                             should be shown in the column
     */
    public void init(final String filename,
            final String extension,
            final FeatureServiceFeature[] features,
            final AbstractFeatureService service,
            final List<String[]> aliasAttributeList) {
        this.features = features;
        this.service = service;
        this.aliasAttributeList = aliasAttributeList;
        this.title = "Export " + ((features != null) ? features.length : "") + " Features";
        this.extension = extension;

        if (aliasAttributeList == null) {
            if ((features != null) && (features.length > 0)) {
                this.aliasAttributeList = getAttributeNames(features[0]);
            }
        }

        status = Download.State.WAITING;
        String filenameWithoutExt = filename;

        if (filename.contains(".") && (filename.charAt(filename.length() - 4) == '.')) {
            filenameWithoutExt = filename.substring(0, filename.length() - 4);
        }

        if (absoluteFileName || new File(filename).isAbsolute()) {
            fileToSaveTo = new File(filename + extension);
            int index = filename.lastIndexOf("/");

            if (index == -1) {
                index = filename.lastIndexOf("\\");
            }

            final File dir = new File(filename.substring(0, index));
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } else {
            determineDestinationFile(filenameWithoutExt, extension);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected void loadFeaturesIfRequired() throws Exception {
        if (features == null) {
            service.initAndWait();
            final int pageSize = service.getMaxFeaturesPerPage() * 2;
            List<FeatureServiceFeature> featureList;

            if ((pageSize < 0)
                        || ((service.getFeatureServiceAttributes() == null)
                            || (service.getFeatureServiceAttributes().get("id") == null))) {
                featureList = service.getFeatureFactory().createFeatures(service.getQuery(), null, null, 0, 0, null);
                features = featureList.toArray(new FeatureServiceFeature[featureList.size()]);
            } else {
                final List<FeatureServiceFeature> tmpFeatureList = new ArrayList<FeatureServiceFeature>();
                final Geometry g = ZoomToLayerWorker.getServiceBounds(service);
                XBoundingBox bb;
                final FeatureServiceAttribute[] idAttr = new FeatureServiceAttribute[] {
                        (FeatureServiceAttribute)service.getFeatureServiceAttributes().get("id")
                    };

                if (g != null) {
                    bb = new XBoundingBox(g);

                    try {
                        final CrsTransformer transformer = new CrsTransformer(CismapBroker.getInstance().getSrs()
                                        .getCode());
                        bb = transformer.transformBoundingBox(bb);
                    } catch (Exception e) {
                        error(e);
                    }
                } else {
                    bb = null;
                }

                final int count = service.getFeatureCount(service.getQuery(), bb);
                int index = 0;
                final FilePersistenceManager pm = new FilePersistenceManager(fileToSaveTo.getParentFile());

                do {
                    if (Thread.interrupted()) {
                        features = null;
                        pm.close();
                        return;
                    }
                    featureList = service.getFeatureFactory()
                                .createFeatures(service.getQuery(), null, null, index, pageSize, idAttr);
                    index += featureList.size();

                    for (final FeatureServiceFeature f : featureList) {
                        tmpFeatureList.add(new PersistentFeature(f, pm));
                    }
                } while (index < count);
                features = tmpFeatureList.toArray(new FeatureServiceFeature[tmpFeatureList.size()]);
            }

            if (aliasAttributeList == null) {
                if ((features != null) && (features.length > 0)) {
                    this.aliasAttributeList = getAttributeNames(features[0]);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  absoluteFileName  DOCUMENT ME!
     */
    public void setAbsoluteFileName(final boolean absoluteFileName) {
        this.absoluteFileName = absoluteFileName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract String getDefaultExtension();

    /**
     * DOCUMENT ME!
     *
     * @param   f  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<String[]> getAttributeNames(final FeatureServiceFeature f) {
        final List<String[]> attrNames = new ArrayList<String[]>();
        final Map<String, Object> hm = (Map<String, Object>)f.getProperties();

        for (final String attrName : hm.keySet()) {
            final String[] aliasName = new String[2];
            aliasName[0] = aliasName[1] = attrName;
            attrNames.add(aliasName);
        }

        return attrNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aliasAttributeList  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected List<String> toAliasList(final List<String[]> aliasAttributeList) {
        return toAliasOrAttributeList(aliasAttributeList, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aliasAttributeList  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected List<String> toAttributeList(final List<String[]> aliasAttributeList) {
        return toAliasOrAttributeList(aliasAttributeList, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aliasAttributeList  DOCUMENT ME!
     * @param   aliasList           DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<String> toAliasOrAttributeList(final List<String[]> aliasAttributeList, final boolean aliasList) {
        final List<String> attrList = new ArrayList<String>(aliasAttributeList.size());

        for (final String[] aliasAttr : aliasAttributeList) {
            attrList.add((aliasList ? aliasAttr[0] : aliasAttr[1]));
        }

        return attrList;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ExportDownload) {
            if ((((ExportDownload)obj).id == id)) {
                return (obj.getClass().getName().equals(this.getClass().getName()));
            }
        }

        return super.equals(obj);
    }
}
