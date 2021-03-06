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
package de.cismet.cismap.commons.drophandler.builtin;

import com.vividsolutions.jts.geom.Point;

import lombok.Getter;

import java.awt.image.BufferedImage;

import java.io.File;

import javax.imageio.ImageIO;

import javax.swing.ImageIcon;

import de.cismet.cismap.commons.features.DefaultXStyledFeature;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;

import de.cismet.tools.gui.ImageUtil;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class MappingComponentGeoImageFileFeatureRenderer extends DefaultXStyledFeature {

    //~ Static fields/initializers ---------------------------------------------

    public static final BufferedImage ARROW;
    public static final BufferedImage ARROW_NULL;

    static {
        try {
            ARROW = ImageIO.read(MappingComponentGeoImageFileFeatureRenderer.class.getResource(
                        "/de/cismet/cismap/commons/drophandler/builtin/angle.png"));
            ARROW_NULL = ImageIO.read(MappingComponentGeoImageFileFeatureRenderer.class.getResource(
                        "/de/cismet/cismap/commons/drophandler/builtin/angle_null.png"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    //~ Instance fields --------------------------------------------------------

    @Getter private final Double winkel;
    @Getter private final File imageFile;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MappingComponentGeoImageFileFeatureRenderer object.
     *
     * @param  imageFile  DOCUMENT ME!
     * @param  geometry   DOCUMENT ME!
     * @param  winkel     DOCUMENT ME!
     */
    public MappingComponentGeoImageFileFeatureRenderer(final File imageFile,
            final Point geometry,
            final Double winkel) {
        super(
            new ImageIcon(ImageUtil.resizeOnScale(ARROW, 16, 16)),
            imageFile.getName(),
            "Bild-Datei",
            new MappingComponentGeoImageFileDropHandlerInfoComponent(imageFile),
            null);
        this.imageFile = imageFile;
        this.winkel = winkel;
        setGeometry(geometry);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public FeatureAnnotationSymbol getPointAnnotationSymbol() {
        final FeatureAnnotationSymbol symb;
        if (winkel == null) {
            symb = new FeatureAnnotationSymbol(ARROW_NULL);
        } else {
            symb = new FeatureAnnotationSymbol(ImageUtil.rotateImage(ARROW, -winkel));
        }
        symb.setSweetSpotX(0.5);
        symb.setSweetSpotY(0.5);
        return symb;
    }
}
