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

import org.deegree.io.shpapi.shape_new.ShapeFile;
import org.deegree.io.shpapi.shape_new.ShapeFileWriter;
import org.deegree.model.feature.FeatureCollection;

import org.openide.util.Cancellable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;

import de.cismet.tools.gui.downloadmanager.AbstractDownload;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ExportXlsDownload extends AbstractDownload implements Cancellable {

    //~ Instance fields --------------------------------------------------------

    private DefaultFeatureServiceFeature[] features;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportShapeDownload object.
     *
     * @param  filename   DOCUMENT ME!
     * @param  extension  DOCUMENT ME!
     * @param  features   DOCUMENT ME!
     */
    public ExportXlsDownload(final String filename,
            final String extension,
            final DefaultFeatureServiceFeature[] features) {
        this.features = features;
        this.title = "Export " + features.length + " Features";

        status = State.WAITING;
        determineDestinationFile(filename, extension);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (status != State.WAITING) {
            return;
        }

        status = State.RUNNING;

        if ((features != null) && (features.length > 0)) {
            stateChanged();
            List<String> attributeNames = null;
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(fileToSaveTo));

                for (final DefaultFeatureServiceFeature feature : features) {
                    if (attributeNames == null) {
                        attributeNames = getAttributeNames(feature);
                    }
                    bw.write(toString(attributeNames, feature));
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
        } else {
            error(new Exception("No features found"));
        }

        if (status == State.RUNNING) {
            status = State.COMPLETED;
            stateChanged();
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
    private String toString(final List<String> attributeNames, final DefaultFeatureServiceFeature f) {
        final Map<String, Object> hm = (Map<String, Object>)f.getProperties();
        final List<Object> vals = new ArrayList<Object>();

        for (final String attrName : attributeNames) {
            final Object o = hm.get(attrName);

            vals.add(o);
        }

        return toString(vals);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   vals  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String toString(final List<Object> vals) {
        StringBuffer result = null;

        for (Object tmp : vals) {
            if (tmp == null) {
                tmp = "<null>";
            }

            if (result == null) {
                result = new StringBuffer(String.valueOf(tmp));
            } else {
                result.append('\t').append(String.valueOf(tmp));
            }
        }

        return result.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   f  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<String> getAttributeNames(final DefaultFeatureServiceFeature f) {
        final List<String> attributeNames = new ArrayList<String>();
        final Map<String, Object> hm = (Map<String, Object>)f.getProperties();

        for (final String attrName : hm.keySet()) {
            attributeNames.add(attrName);
        }

        return attributeNames;
    }

    @Override
    public boolean cancel() {
        boolean cancelled = true;
        if (downloadFuture != null) {
            cancelled = downloadFuture.cancel(true);
        }
        if (cancelled) {
            status = State.ABORTED;
            stateChanged();
        }
        return cancelled;
    }
}
