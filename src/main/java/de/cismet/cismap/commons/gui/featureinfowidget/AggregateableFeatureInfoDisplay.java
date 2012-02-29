/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.featureinfowidget;

import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   dmeiers
 * @version  $Revision$, $Date$
 *
 *           <p>An AggregateableFeatureInfoDisplay manages a list of similiar feature info displays in order to combine the information of them. The similiarity is expressed by an String.</p>
 */
public interface AggregateableFeatureInfoDisplay {

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the ID of this AggregateableFeatureInfoDisplay.
     *
     * @return  the id
     */
    String getAggregateTypeID();

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    void setAggregatableDisplayList(List<AggregateableFeatureInfoDisplay> l);
}
