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
package de.cismet.cismap.commons.internaldb;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.IOException;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DBTransferable implements Transferable {

    //~ Instance fields --------------------------------------------------------

    private DataFlavor TREEPATH_FLAVOR = new DataFlavor(
            DataFlavor.javaJVMLocalObjectMimeType,
            "SelectionAndCapabilities"); // NOI18N
    private DBTableInformation[] transferObjects;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DBTransferable object.
     *
     * @param  transferObjects  DOCUMENT ME!
     */
    public DBTransferable(final DBTableInformation[] transferObjects) {
        this.transferObjects = transferObjects;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { TREEPATH_FLAVOR };
    }

    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        return flavor.equals(TREEPATH_FLAVOR);
    }

    @Override
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            return transferObjects;
        }

        return null;
    }
}
