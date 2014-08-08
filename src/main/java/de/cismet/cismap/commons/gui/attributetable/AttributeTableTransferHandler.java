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
package de.cismet.cismap.commons.gui.attributetable;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.util.ArrayList;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

/**
 * The TransferHandler that is used by the Attributetable. It does only support the drag operation (no drop support).
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AttributeTableTransferHandler extends TransferHandler {

    //~ Static fields/initializers ---------------------------------------------

    public static final DataFlavor rowFlavor = new ActivationDataFlavor(
            FeatureServiceFeature[].class,
            DataFlavor.javaJVMLocalObjectMimeType,
            "AttributeTableFeature");

    //~ Instance fields --------------------------------------------------------

    private AttributeTable table;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AttributeTableTransferHandler object.
     *
     * @param  table  DOCUMENT ME!
     */
    public AttributeTableTransferHandler(final AttributeTable table) {
        this.table = table;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected Transferable createTransferable(final JComponent c) {
        final JTable table = (JTable)c;
        final ArrayList<FeatureServiceFeature> list = new ArrayList<FeatureServiceFeature>();
        final SimpleAttributeTableModel model = (SimpleAttributeTableModel)table.getModel();

        for (final int i : table.getSelectedRows()) {
            list.add(model.getFeatureServiceFeature(table.convertRowIndexToModel(i)));
        }

        final FeatureServiceFeature[] transferedObjects = list.toArray(new FeatureServiceFeature[list.size()]);

        return new DataHandler(transferedObjects, rowFlavor.getMimeType());
    }

    @Override
    public boolean canImport(final TransferHandler.TransferSupport info) {
        return false;
    }

    @Override
    public int getSourceActions(final JComponent c) {
        return TransferHandler.COPY;
    }

    @Override
    public boolean importData(final TransferHandler.TransferSupport info) {
        return false;
    }

    @Override
    protected void exportDone(final JComponent c, final Transferable t, final int act) {
        c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
