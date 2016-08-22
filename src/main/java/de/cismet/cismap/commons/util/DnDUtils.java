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
package de.cismet.cismap.commons.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;

import javax.swing.TransferHandler;

/**
 * DOCUMENT ME!
 *
 * @author   spuhl
 * @version  $Revision$, $Date$
 */
public class DnDUtils {

    //~ Static fields/initializers ---------------------------------------------

    // TODO Best position for this code snippet ?
    public static DataFlavor URI_LIST_FLAVOR;

    static {
        try {
            URI_LIST_FLAVOR = new DataFlavor("text/uri-list;class=java.lang.String"); // NOI18N
        } catch (ClassNotFoundException e) {                                          // can't happen
            e.printStackTrace();
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   data  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List textURIListToFileList(final String data) {
        final java.util.List list = new java.util.ArrayList(1);
        for (final java.util.StringTokenizer st = new java.util.StringTokenizer(data, "\r\n"); // NOI18N
                    st.hasMoreTokens();) {
            final String s = st.nextToken();
            if (s.startsWith("#")) {                                                           // NOI18N
                // the line is a comment (as per the RFC 2483)
                continue;
            }
            try {
                final java.net.URI uri = new java.net.URI(s);
                final java.io.File file = new java.io.File(uri);
                list.add(file);
            } catch (java.net.URISyntaxException e) {
                // malformed URI
            } catch (IllegalArgumentException e) {
                // the URI is not a valid 'file:' URI
            }
        }
        return list;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   dtde  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static final boolean isFilesOrUriList(final DropTargetDropEvent dtde) {
        return isFilesOrUriList(new TransferSupportWrapper(dtde));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   tsw  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static final boolean isFilesOrUriList(final TransferSupportWrapper tsw) {
        if (tsw == null) {
            return false;
        }
        return tsw.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                    || tsw.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   dtde  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnsupportedFlavorException  DOCUMENT ME!
     * @throws  IOException                 DOCUMENT ME!
     */
    public static List<File> getFilesFrom(final DropTargetDropEvent dtde) throws UnsupportedFlavorException,
        IOException {
        return getFilesFrom(new TransferSupportWrapper(dtde));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   tsw  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnsupportedFlavorException  DOCUMENT ME!
     * @throws  IOException                 DOCUMENT ME!
     */
    public static List<File> getFilesFrom(final TransferSupportWrapper tsw) throws UnsupportedFlavorException,
        IOException {
        if (tsw == null) {
            return null;
        }
        List<File> data = null;
        final Transferable transferable = tsw.getTransferable();
        if (tsw.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
            try {
                data = (java.util.List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
            } catch (final Exception ex) {
                if (data == null) {
                    data = DnDUtils.textURIListToFileList((String)transferable.getTransferData(
                                DnDUtils.URI_LIST_FLAVOR));
                }
            }
        } else {
            data = (java.util.List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
        }
        return data;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * This class wraps a DropTargetDropEvent or TransferSupport.
     *
     * @version  $Revision$, $Date$
     */
    public static class TransferSupportWrapper {

        //~ Instance fields ----------------------------------------------------

        private DropTargetDropEvent event;
        private TransferHandler.TransferSupport transfer;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new TransferSupportWrapper object.
         *
         * @param  event  DOCUMENT ME!
         */
        public TransferSupportWrapper(final DropTargetDropEvent event) {
            this.event = event;
        }

        /**
         * Creates a new TransferSupportWrapper object.
         *
         * @param  transfer  DOCUMENT ME!
         */
        public TransferSupportWrapper(final TransferHandler.TransferSupport transfer) {
            this.transfer = transfer;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   df  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isDataFlavorSupported(final DataFlavor df) {
            if (event != null) {
                return event.isDataFlavorSupported(df);
            } else {
                return transfer.isDataFlavorSupported(df);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  action  DOCUMENT ME!
         */
        public void acceptDrop(final int action) {
            if (event != null) {
                event.acceptDrop(action);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  success  DOCUMENT ME!
         */
        public void dropComplete(final boolean success) {
            if (event != null) {
                event.dropComplete(success);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Transferable getTransferable() {
            if (event != null) {
                return event.getTransferable();
            } else {
                return transfer.getTransferable();
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public List<DataFlavor> getCurrentDataFlavorsAsList() {
            if (event != null) {
                return event.getCurrentDataFlavorsAsList();
            } else {
                return Arrays.asList(transfer.getDataFlavors());
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public DataFlavor[] getCurrentDataFlavors() {
            if (event != null) {
                return event.getCurrentDataFlavors();
            } else {
                return transfer.getDataFlavors();
            }
        }
    }
}
