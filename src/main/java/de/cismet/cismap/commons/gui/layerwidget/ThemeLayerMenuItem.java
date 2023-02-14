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
package de.cismet.cismap.commons.gui.layerwidget;

import java.awt.event.ActionListener;

import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.ServiceLayer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class ThemeLayerMenuItem extends JMenuItem implements ActionListener {

    //~ Static fields/initializers ---------------------------------------------

    public static final int ROOT = 1;
    public static final int NODE = 2;
    public static final int FOLDER = 4;
    public static final int MULTI = 8;
    public static final int FEATURE_SERVICE = 16;
    public static final int GEOMETRY = 32;
    public static final int FEATURE_SELECTED = 64;
    public static final int NO_GEOMETRY = 128;
    public static final int NO_FEATURE_SELECTED = 256;
    public static final int EVER = 511;

    //~ Instance fields --------------------------------------------------------

    protected int selectable = 0;
    protected boolean newSection = false;

    protected int visibility = 0;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ThemeLayerMenuItem object.
     *
     * @param  title       DOCUMENT ME!
     * @param  visibility  DOCUMENT ME!
     */
    public ThemeLayerMenuItem(final String title, final int visibility) {
        this(title, visibility, visibility);
    }

    /**
     * Creates a new ThemeLayerMenuItem object.
     *
     * @param  title       DOCUMENT ME!
     * @param  visibility  DOCUMENT ME!
     * @param  selectable  DOCUMENT ME!
     */
    public ThemeLayerMenuItem(final String title, final int visibility, final int selectable) {
        super(title);
        this.visibility = visibility;
        this.selectable = selectable;
        addActionListener(this);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   mask  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isVisible(final int mask) {
        return (visibility & mask) == mask;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serviceLayerList  mask DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isVisible(final List<ServiceLayer> serviceLayerList) {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   serviceLayerList  mask DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSelectable(final List<ServiceLayer> serviceLayerList) {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mask  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSelectable(final int mask) {
        return (selectable & mask) == mask;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isNewSection() {
        return newSection;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  paths  DOCUMENT ME!
     */
    public void refreshText(final TreePath[] paths) {
    }
}
