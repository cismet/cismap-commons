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

import java.net.URL;

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
    public static final FeatureAnnotationSymbol ARROW_NULL;

    static {
        try {
            final URL arrowUrl = MappingComponentGeoImageFileFeatureRenderer.class.getResource(
                    "/de/cismet/cismap/commons/drophandler/builtin/angle.png");
            ARROW = ImageIO.read(arrowUrl);
            ARROW_NULL = new FeatureAnnotationSymbol(new ImageIcon(
                        "/de/cismet/cismap/commons/drophandler/builtin/angle_null.png").getImage());
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
            null,
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
        if (winkel == null) {
            return new FeatureAnnotationSymbol(ARROW_NULL.getImage());
        } else {
            final BufferedImage rotatedArrow = ImageUtil.rotateImage(ARROW, -winkel);
            final FeatureAnnotationSymbol symb = new FeatureAnnotationSymbol(rotatedArrow);
            symb.setSweetSpotX(0.5);
            symb.setSweetSpotY(0.5);
            return symb;
        }
    }
}
