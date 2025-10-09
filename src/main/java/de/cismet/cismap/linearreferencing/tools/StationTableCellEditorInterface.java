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
package de.cismet.cismap.linearreferencing.tools;

import de.cismet.cismap.commons.featureservice.LinearReferencingInfo;
import java.util.List;
import javax.swing.table.TableCellEditor;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface StationTableCellEditorInterface extends TableCellEditor {
    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  columnName  the columnName to set
     */
    void setColumnName(String columnName);

    /**
     * DOCUMENT ME!
     *
     * @param  linRefInfos  DOCUMENT ME!
     */
    void setLinRefInfos(List<LinearReferencingInfo> linRefInfos);
}
