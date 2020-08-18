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

import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;

import org.openide.util.NbBundle;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.PersistentFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;

import de.cismet.tools.gui.downloadmanager.Download;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ExportTxtDownload extends ExportDownload {

    //~ Instance fields --------------------------------------------------------

    protected String separator = "\t";
    protected boolean writeHeader = true;
    protected String nullValue = "";
    protected String quotes = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportShapeDownload object. The init method must be invoked before the download can be started, if
     * this constructor is used.
     */
    public ExportTxtDownload() {
    }

    /**
     * Creates a new ExportShapeDownload object.
     *
     * @param  filename        DOCUMENT ME!
     * @param  extension       DOCUMENT ME!
     * @param  features        DOCUMENT ME!
     * @param  service         DOCUMENT ME!
     * @param  attributeNames  A list with string arrays. Every array should have 2 elements. The first element is the
     *                         alias of the column and the second element is the name of the attribute, that should be
     *                         shown in the column
     */
    public ExportTxtDownload(final String filename,
            final String extension,
            final FeatureServiceFeature[] features,
            final AbstractFeatureService service,
            final List<String[]> attributeNames) {
        init(filename, extension, features, service, attributeNames);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (status != Download.State.WAITING) {
            return;
        }
        try {
            status = Download.State.RUNNING;
            stateChanged();

            try {
                loadFeaturesIfRequired();
            } catch (Exception e) {
                log.error("Error while retrieving features", e);
                error(e);
                return;
            }

            if (features == null) {
                features = new FeatureServiceFeature[0];
            }

            final List<String> attributeList = toAttributeList(aliasAttributeList);
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(fileToSaveTo));

                if ((aliasAttributeList != null)) {
                    if (writeHeader) {
                        bw.write(toString(toAliasList(aliasAttributeList), true));
                        bw.newLine();
                    }
                }

                for (final FeatureServiceFeature feature : features) {
                    if (Thread.interrupted()) {
                        bw.close();
                        fileToSaveTo.delete();
                        bw = null;
                        break;
                    }
                    bw.write(toString(attributeList, feature));
                    bw.newLine();
                }
            } catch (final Exception ex) {
                error(ex);
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (final IOException e) {
                        log.error("Error while closing file", e);
                    }
                }
            }

            if ((features.length > 0) && (features[0] instanceof PersistentFeature)) {
                ((PersistentFeature)features[0]).getPersistenceManager().close();
            }

            if (status == Download.State.RUNNING) {
                status = Download.State.COMPLETED;
                stateChanged();
            }
        } finally {
            // without the following lines, a lot of memory will be used as long as
            // this download is in the download list
            features = null;
            service = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attributeNames  DOCUMENT ME!
     * @param   f               DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String toString(final List<String> attributeNames, final FeatureServiceFeature f) {
        final Map<String, Object> hm = (Map<String, Object>)f.getProperties();
        final List<Object> vals = new ArrayList<Object>();

        for (final String attrName : attributeNames) {
            final Object o = hm.get(attrName);

            vals.add(o);
        }

        return toString(vals, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   vals           DOCUMENT ME!
     * @param   withoutQuotes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String toString(final List<? extends Object> vals, final boolean withoutQuotes) {
        StringBuffer result = null;

        for (Object tmp : vals) {
            if (tmp == null) {
                tmp = nullValue;
            }

            if (tmp instanceof Geometry) {
                final org.deegree.model.spatialschema.Geometry geom = ((org.deegree.model.spatialschema.Geometry)tmp);
                tmp = "Geometry";
                try {
                    tmp = JTSAdapter.export(geom);
                } catch (GeometryException e) {
                    log.error("Error while transforming deegree geometry to jts geometry.", e);
                }
            }

            if (result == null) {
                result = new StringBuffer();
            } else {
                result.append(separator);
            }

            if (!withoutQuotes && (quotes != null)
                        && ((tmp instanceof String) || (tmp instanceof com.vividsolutions.jts.geom.Geometry))) {
                result.append(quotes).append(String.valueOf(tmp)).append(quotes);
            } else {
                if ((tmp instanceof Float) || (tmp instanceof Double)) {
                    result.append(FeatureTools.FORMATTER.format(tmp));
                } else {
                    result.append(String.valueOf(tmp));
                }
            }
        }

        return result.toString();
    }

    @Override
    public String getDefaultExtension() {
        return ".txt";
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(ExportShapeDownload.class, "ExportTxtDownload.toString");
    }
}
