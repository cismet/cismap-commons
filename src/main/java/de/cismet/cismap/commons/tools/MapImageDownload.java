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

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.cismet.cismap.commons.gui.MappingComponent;

import de.cismet.tools.gui.downloadmanager.AbstractCancellableDownload;

/**
 * A Download which can be added to the DownloadManager and saves an image to a file. The transparency of the files gets
 * removed.
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class MapImageDownload extends AbstractCancellableDownload {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(MapImageDownload.class);

    //~ Instance fields --------------------------------------------------------

    String extension;
    MappingComponent map;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ImageDownload object.
     *
     * @param  filename      DOCUMENT ME!
     * @param  extension     DOCUMENT ME!
     * @param  fileToSaveTo  DOCUMENT ME!
     * @param  map           image futureImage DOCUMENT ME!
     */
    public MapImageDownload(
            final String filename,
            final String extension,
            final File fileToSaveTo,
            final MappingComponent map) {
        this.extension = extension;
        this.map = map;

        title = org.openide.util.NbBundle.getMessage(
                MapImageDownload.class,
                "MapImageDownload.title");

        status = State.WAITING;
        this.fileToSaveTo = fileToSaveTo;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (status != State.WAITING) {
            return;
        }
        status = State.RUNNING;

        stateChanged();
        final Image image = map.getImage();

        if ((image != null) && !Thread.interrupted()) {
            try {
                ImageIO.write(removeTransparency(image), extension, fileToSaveTo);
            } catch (IOException ex) {
                LOG.error("Error while saving the image", ex);
                status = State.COMPLETED_WITH_ERROR;
                stateChanged();
                deleteFile();
                return;
            }
        } else {
            status = State.COMPLETED_WITH_ERROR;
            stateChanged();
            deleteFile();
            return;
        }

        if (status == State.RUNNING) {
            status = State.COMPLETED;
            stateChanged();
        }
    }

    /**
     * Removes the transparency from an image and returns an opaque image. This method is needed as the image should be
     * saved as jpg, which is unable to handle transparency. The transparent image is copied to another opaque image
     * with a white background, which is returned.
     *
     * @param   transparentImage  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private BufferedImage removeTransparency(final Image transparentImage) {
        final BufferedImage whiteBackgroundImage = new BufferedImage(transparentImage.getWidth(null),
                transparentImage.getHeight(null),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = null;
        try {
            g2 = whiteBackgroundImage.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, whiteBackgroundImage.getWidth(), whiteBackgroundImage.getHeight());

            g2.drawImage(
                transparentImage,
                0,
                0,
                whiteBackgroundImage.getWidth(),
                whiteBackgroundImage.getHeight(),
                null);
        } finally {
            if (g2 != null) {
                g2.dispose();
            }
        }

        return whiteBackgroundImage;
    }

    /**
     * DOCUMENT ME!
     */
    private void deleteFile() {
        if (fileToSaveTo.exists() && fileToSaveTo.isFile()) {
            fileToSaveTo.delete();
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = (37 * hash)
                    + ((this.fileToSaveTo.getAbsolutePath() != null) ? this.fileToSaveTo.getAbsolutePath().hashCode()
                                                                     : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj;
    }
}
