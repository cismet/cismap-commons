/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.tools;

import org.apache.log4j.Logger;

import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import javax.swing.JOptionPane;

import de.cismet.cismap.commons.ErroneousRetrievalServiceProvider;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.retrieval.RetrievalService;

import de.cismet.tools.gui.downloadmanager.AbstractDownload;

/**
 * A Download which can be added to the DownloadManager and saves an image from a Future&lt;Image&gt; to a file. If the
 * image should be saved as jpeg then the transparency of the image gets removed.
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class FutureImageDownload extends AbstractDownload implements Cancellable {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FutureImageDownload.class);

    //~ Instance fields --------------------------------------------------------

    String extension;
    Future<Image> futureImage;
    int futureImageHash;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ImageDownload object.
     *
     * @param  filename      DOCUMENT ME!
     * @param  extension     DOCUMENT ME!
     * @param  title         DOCUMENT ME!
     * @param  fileToSaveTo  DOCUMENT ME!
     * @param  futureImage   DOCUMENT ME!
     */
    public FutureImageDownload(
            final String filename,
            final String extension,
            final String title,
            final File fileToSaveTo,
            final Future<Image> futureImage) {
        this.extension = extension;
        this.futureImage = futureImage;
        this.futureImageHash = futureImage.hashCode();

        this.title = title;

        status = State.WAITING;
        this.fileToSaveTo = fileToSaveTo;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void run() {
        if (status != State.WAITING) {
            releaseMemory();
            return;
        }
        status = State.RUNNING;

        stateChanged();

        Image image = null;
        if (futureImage != null) {
            try {
                if (!Thread.interrupted()) {
                    image = (Image)futureImage.get();

                    if (futureImage instanceof ErroneousRetrievalServiceProvider) {
                        final ErroneousRetrievalServiceProvider p = (ErroneousRetrievalServiceProvider)futureImage;
                        final HashSet<RetrievalService> layers = p.getErroneousLayer();

                        if ((layers != null) && !layers.isEmpty()) {
                            String layersWithErrors = null;

                            for (final RetrievalService service : layers) {
                                if (layersWithErrors == null) {
                                    layersWithErrors = service.toString();
                                } else {
                                    layersWithErrors += "\n" + service.toString();
                                }
                            }

                            final Component parent = CismapBroker.getInstance().getMappingComponent();

                            JOptionPane.showMessageDialog(
                                parent,
                                NbBundle.getMessage(
                                    FutureImageDownload.class,
                                    "FutureImageDownload.run().cannotLoadLayer.message",
                                    layersWithErrors),
                                NbBundle.getMessage(
                                    FutureImageDownload.class,
                                    "FutureImageDownload.run().cannotLoadLayer.title"),
                                JOptionPane.WARNING_MESSAGE);
                        }
                    }
                } else {
                    deleteFile();
                }
            } catch (InterruptedException ex) {
                deleteFile();
                releaseMemory();
                return;
            } catch (ExecutionException ex) {
                LOG.error("Error while getting the image.", ex);
                status = State.COMPLETED_WITH_ERROR;
                stateChanged();
                deleteFile();
                releaseMemory();
                return;
            }
        }

        if ((image != null) && !Thread.interrupted()) {
            try {
                ImageIO.write(prepareImage(image), extension, fileToSaveTo);
            } catch (IOException ex) {
                LOG.error("Error while saving the image", ex);
                status = State.COMPLETED_WITH_ERROR;
                stateChanged();
                deleteFile();
                releaseMemory();
                return;
            }
        } else {
            status = State.COMPLETED_WITH_ERROR;
            stateChanged();
            deleteFile();
            releaseMemory();
            return;
        }

        if (status == State.RUNNING) {
            status = State.COMPLETED;
            stateChanged();
        }
        releaseMemory();
    }

    /**
     * Set the futureImage to null. Otherwise, the FutureImageDownload will take much memory until it is removed from
     * the DownloadManager.
     */
    private void releaseMemory() {
        futureImage = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   image  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private BufferedImage prepareImage(final Image image) {
        if (extension.endsWith("jpg") || extension.endsWith("jpeg")) {
            return removeTransparency(image);
        } else if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        } else {
            // Convert the image to a buffered image
            // Create a buffered image with transparency
            final BufferedImage bimage = new BufferedImage(image.getWidth(null),
                    image.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);

            // Draw the image on to the buffered image
            final Graphics2D bGr = bimage.createGraphics();
            bGr.drawImage(image, 0, 0, null);
            bGr.dispose();

            // Return the buffered image
            return bimage;
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

    @Override
    public boolean cancel() {
        boolean cancelled = true;
        if (downloadFuture != null) {
            cancelled = downloadFuture.cancel(true);
        }
        if (cancelled) {
            status = State.ABORTED;
            stateChanged();
            releaseMemory();
        }
        return cancelled;
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
        hash = (37 * hash) + this.futureImageHash;
        hash = (37 * hash) + ((this.title != null) ? this.title.hashCode() : 0);
        hash = (37 * hash) + ((this.fileToSaveTo != null) ? this.fileToSaveTo.hashCode() : 0);
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
        final FutureImageDownload other = (FutureImageDownload)obj;
        if (this.futureImageHash != other.futureImageHash) {
            return false;
        }
        if ((this.title == null) ? (other.title != null) : (!this.title.equals(other.title))) {
            return false;
        }
        if ((this.fileToSaveTo != other.fileToSaveTo)
                    && ((this.fileToSaveTo == null) || !this.fileToSaveTo.equals(other.fileToSaveTo))) {
            return false;
        }
        return true;
    }
}
