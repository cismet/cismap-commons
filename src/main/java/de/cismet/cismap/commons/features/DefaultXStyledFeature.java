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
package de.cismet.cismap.commons.features;

import java.awt.Stroke;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import de.cismet.cismap.commons.Refreshable;

/**
 * DOCUMENT ME!
 *
 * @author   daniel meiers
 * @version  $Revision$, $Date$
 */
public class DefaultXStyledFeature extends DefaultStyledFeature implements XStyledFeature {

    //~ Instance fields --------------------------------------------------------

    private ImageIcon iconImage;
    private String name;
    private String type;
    private JComponent infoComponent;
    private Stroke lineStyle;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DefaultXStyledFeature object.
     *
     * @param  iconImage      DOCUMENT ME!
     * @param  name           DOCUMENT ME!
     * @param  type           DOCUMENT ME!
     * @param  infoComponent  DOCUMENT ME!
     * @param  lineStyle      DOCUMENT ME!
     */
    public DefaultXStyledFeature(final ImageIcon iconImage,
            final String name,
            final String type,
            final JComponent infoComponent,
            final Stroke lineStyle) {
        this.iconImage = iconImage;
        this.name = name;
        this.type = type;
        this.infoComponent = infoComponent;
        this.lineStyle = lineStyle;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  iconImage  DOCUMENT ME!
     */
    public void setIconImage(final ImageIcon iconImage) {
        this.iconImage = iconImage;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  name  DOCUMENT ME!
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  type  DOCUMENT ME!
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  infoComponent  DOCUMENT ME!
     */
    public void setInfoComponent(final JComponent infoComponent) {
        this.infoComponent = infoComponent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  lineSytle  DOCUMENT ME!
     */
    public void setLineSytle(final Stroke lineSytle) {
        this.lineStyle = lineSytle;
    }

    @Override
    public ImageIcon getIconImage() {
        return iconImage;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public JComponent getInfoComponent(final Refreshable refresh) {
        return infoComponent;
    }

    @Override
    public Stroke getLineStyle() {
        return lineStyle;
    }
}
