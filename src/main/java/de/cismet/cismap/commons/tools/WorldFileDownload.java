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

import org.openide.util.Exceptions;

import java.awt.Image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import de.cismet.cismap.commons.BoundingBox;

import de.cismet.tools.gui.downloadmanager.AbstractDownload;

/**
 * A Download, which creates a world file from a map image and a bounding box and saves it to a file.
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class WorldFileDownload extends AbstractDownload {

    //~ Instance fields --------------------------------------------------------

    private final Future<Image> futureImage;
    private BoundingBox boundingBoxFromMap;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WorldFileDownload object.
     *
     * @param  title               DOCUMENT ME!
     * @param  futureImage         DOCUMENT ME!
     * @param  boundingBoxFromMap  DOCUMENT ME!
     * @param  fileAbsolutPath     DOCUMENT ME!
     */
    public WorldFileDownload(final String title,
            final Future<Image> futureImage,
            final BoundingBox boundingBoxFromMap,
            final String fileAbsolutPath) {
        this.futureImage = futureImage;
        this.boundingBoxFromMap = boundingBoxFromMap;
        this.title = title;

        this.fileToSaveTo = new File(fileAbsolutPath);

        status = State.WAITING;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        try {
            if (status != State.WAITING) {
                return;
            }

            status = State.RUNNING;
            stateChanged();

            final Image image = futureImage.get();
            final String content = getWorldFileContent(image);

            if ((content == null) || (content.length() <= 0)) {
                log.info("Downloaded content seems to be empty..");

                if (status == State.RUNNING) {
                    status = State.COMPLETED;
                    stateChanged();
                }

                return;
            }

            PrintWriter out = null;
            try {
                out = new PrintWriter(fileToSaveTo);
                out.println(content);
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                out.close();
            }

            if (status == State.RUNNING) {
                status = State.COMPLETED;
                stateChanged();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   image  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getWorldFileContent(final Image image) {
        final BoundingBox boundingBox = boundingBoxFromMap;
        final DecimalFormat df = new DecimalFormat("#.");
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(dfs);
        df.setMaximumFractionDigits(32);
        final double widthPixel = image.getWidth(null);
        final double heightPixel = image.getWidth(null);

        // pixel size in the x-direction in map units/pixel
        final double xPixelSize = (boundingBox.getX2() - boundingBox.getX1()) / widthPixel;

        // rotation about y-axis
        final int yRotation = 0;
        // rotation about x-axis
        final int xRotation = 0;

        // pixel size in the y-direction in map units, almost always negative
        final double yPixelSize = (boundingBox.getY1() - boundingBox.getY2()) / heightPixel;

        // x-coordinate of the center of the upper left pixel
        final double xPixelCenter = boundingBox.getX1() + (xPixelSize / 2);

        // y-coordinate of the center of the upper left pixel
        final double yPixelCenter = boundingBox.getY1() + (yPixelSize / 2);

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(df.format(xPixelSize));
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(yRotation);
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(xRotation);
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(df.format(yPixelSize));
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(df.format(xPixelCenter));
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(df.format(yPixelCenter));

        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (31 * hash) + ((this.futureImage != null) ? this.futureImage.hashCode() : 0);
        hash = (31 * hash) + ((this.boundingBoxFromMap != null) ? this.boundingBoxFromMap.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WorldFileDownload other = (WorldFileDownload)obj;
        if ((this.futureImage != other.futureImage)
                    && ((this.futureImage == null) || !this.futureImage.equals(other.futureImage))) {
            return false;
        }
        if ((this.boundingBoxFromMap != other.boundingBoxFromMap)
                    && ((this.boundingBoxFromMap == null) || !this.boundingBoxFromMap.equals(
                            other.boundingBoxFromMap))) {
            return false;
        }
        return true;
    }
}
