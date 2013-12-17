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

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;

import de.cismet.tools.gui.downloadmanager.AbstractDownload;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ExportShapeDownload extends AbstractDownload implements Cancellable {

    //~ Instance fields --------------------------------------------------------

    private Feature[] features;
    private String filename;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportShapeDownload object.
     *
     * @param  filename   DOCUMENT ME!
     * @param  extension  DOCUMENT ME!
     * @param  features   DOCUMENT ME!
     */
    public ExportShapeDownload(final String filename,
            final String extension,
            final DefaultFeatureServiceFeature[] features) {
        this.features = features;
        this.filename = filename;
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
            try {
                final FeatureCollection fc = new SimpleFeatureCollection(
                        getId(),
                        (DefaultFeatureServiceFeature[])features);
                final ShapeFile shape = new ShapeFile(
                        fc,
                        fileToSaveTo.getAbsolutePath().substring(0, fileToSaveTo.getAbsolutePath().lastIndexOf(".")));
                final ShapeFileWriter writer = new ShapeFileWriter(shape);
                writer.write();
            } catch (Exception ex) {
                error(ex);
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
     * @return  DOCUMENT ME!
     */
    private String getId() {
        return String.valueOf(System.currentTimeMillis());
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
