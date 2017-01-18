/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.gui.printing;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class RotatedPrintingUtils {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   biggerImage  DOCUMENT ME!
     * @param   angle        DOCUMENT ME!
     * @param   width        DOCUMENT ME!
     * @param   height       DOCUMENT ME!
     * @param   baseDpi      DOCUMENT ME!
     * @param   targetDpi    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static BufferedImage rotateAndCrop(final Image biggerImage,
            final double angle,
            final double width,
            final double height,
            final int baseDpi,
            final int targetDpi) {
//        double printingResolution = targetDpi / CismapBroker.getInstance().getMappingComponent().getFeaturePrintingDpi();
        final int imageWidth = (int)((double)width / (double)baseDpi
                        * (double)targetDpi);
        final int imageHeight = (int)((double)height / (double)baseDpi
                        * (double)targetDpi);
        final BufferedImage bufferedBiggerImage = toBufferedImage(biggerImage);
        final BufferedImage off_Image = new BufferedImage((int)imageWidth,
                (int)imageHeight,
                BufferedImage.TYPE_INT_ARGB);
        final double rotationRequired = Math.toRadians(angle);
        final AffineTransform at = new AffineTransform();

        // translate it to the center of the component
        at.translate(imageWidth / 2, imageHeight / 2);
        // spin it back
        at.rotate(-rotationRequired);
        // put it on the right spot
        at.translate(-bufferedBiggerImage.getWidth() / 2, -bufferedBiggerImage.getHeight() / 2);
        final AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        final Graphics2D g2d = (Graphics2D)off_Image.getGraphics();
        g2d.drawImage(op.filter(bufferedBiggerImage, null), 0, 0, null);
        return off_Image;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   i                DOCUMENT ME!
     * @param   angleInDeegrees  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static BufferedImage rotate(final BufferedImage i, final double angleInDeegrees) {
        final BufferedImage off_Image = new BufferedImage(i.getWidth(),
                i.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        final double rotationRequired = Math.toRadians(angleInDeegrees);
        final AffineTransform at = new AffineTransform();

        // translate it to the center of the component
        at.translate(i.getWidth() / 2, i.getHeight() / 2);
        // spin it back
        at.rotate(rotationRequired);

        // put it on the right spot
        at.translate(-i.getWidth() / 2, -i.getHeight() / 2);
        final AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        final Graphics2D g2d = (Graphics2D)off_Image.getGraphics();
        g2d.drawImage(op.filter(i, null), 0, 0, null);
        return off_Image;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   width   DOCUMENT ME!
     * @param   height  DOCUMENT ME!
     * @param   angle   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Dimension calculateNewImageDimensionToFitRotatedBoundingBox(final double width,
            final double height,
            final double angle) {
        final Dimension ret = new Dimension();
        double a = angle;
        if (a < 0) {
            a = 180 + a;
        }
        if (a > 90) {
            a = a - 90;

            final double rotation1 = Math.toRadians(a);
            final double rotation2 = Math.toRadians(90 - a);
            ret.setSize((Math.cos(rotation1) * height)
                        + (Math.cos(rotation2) * width),
                (Math.cos(rotation1) * width)
                        + (Math.cos(rotation2) * height));
            return ret;
        } else {
            final double rotation1 = Math.toRadians(a);
            final double rotation2 = Math.toRadians(90 - a);
            ret.setSize((Math.cos(rotation1) * width) + (Math.cos(rotation2) * height),
                (Math.cos(rotation1) * height)
                        + (Math.cos(rotation2) * width));
            return ret;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   img  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static BufferedImage toBufferedImage(final Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage)img;
        }

        // Create a buffered image with transparency
        final BufferedImage bimage = new BufferedImage(img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        final Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
}
