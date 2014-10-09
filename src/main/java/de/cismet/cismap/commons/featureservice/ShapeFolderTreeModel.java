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
package de.cismet.cismap.commons.featureservice;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;

import java.net.URI;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ShapeFolderTreeModel implements TreeModel {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ShapeFolderTreeModel.class);

    //~ Instance fields --------------------------------------------------------

    File folder;
    ShapeFileFilter filter = new ShapeFileFilter();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeFolderTreeModel object.
     *
     * @param  link  DOCUMENT ME!
     */
    public ShapeFolderTreeModel(final String link) {
        try {
            String uri = link;
            if (uri.endsWith("\r")) {
                uri = uri.substring(0, uri.length() - 1);
            }

            this.folder = new File(uri.substring(7).trim());
        } catch (Exception e) {
            LOG.error("Illegale shape folder path found.", e);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object getRoot() {
        return folder;
    }

    @Override
    public Object getChild(final Object parent, final int index) {
        if (parent instanceof File) {
            final File parentFile = (File)parent;

            if (parentFile.isDirectory()) {
                return parentFile.listFiles(filter)[index];
            }
        }

        return null;
    }

    @Override
    public int getChildCount(final Object parent) {
        if (parent instanceof File) {
            final File parentFile = (File)parent;

            if (parentFile.isDirectory()) {
                return parentFile.listFiles(filter).length;
            }
        }

        return 0;
    }

    @Override
    public boolean isLeaf(final Object node) {
        if (node instanceof File) {
            final File parentFile = (File)node;

            if (parentFile.isDirectory()) {
                return parentFile.listFiles(filter).length == 0;
            }
        }

        return true;
    }

    @Override
    public void valueForPathChanged(final TreePath path, final Object newValue) {
    }

    @Override
    public int getIndexOfChild(final Object parent, final Object child) {
        if ((parent != null) && (child != null)) {
            if (parent instanceof File) {
                final File parentFile = (File)parent;
                final File[] list = parentFile.listFiles(filter);

                for (int i = 0; i < list.length; ++i) {
                    if (list[i].equals(child)) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    @Override
    public void addTreeModelListener(final TreeModelListener l) {
    }

    @Override
    public void removeTreeModelListener(final TreeModelListener l) {
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ShapeFileFilter implements FileFilter {

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean accept(final File pathname) {
            if (pathname.isDirectory()) {
                return true;
            } else {
                return (pathname.getName().endsWith(".shp") || pathname.getName().endsWith(".gml"));
            }
        }
    }
}
