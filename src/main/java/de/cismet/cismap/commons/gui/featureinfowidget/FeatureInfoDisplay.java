/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.featureinfowidget;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import de.cismet.cismap.commons.interaction.events.MapClickedEvent;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface FeatureInfoDisplay {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   layer             DOCUMENT ME!
     * @param   parentTabbedPane  DOCUMENT ME!
     *
     * @throws  InitialisationException  DOCUMENT ME!
     */
    void init(Object layer, JTabbedPane parentTabbedPane) throws InitialisationException;

    /**
     * DOCUMENT ME!
     *
     * @param  mce  DOCUMENT ME!
     */
    void showFeatureInfo(MapClickedEvent mce);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    JComponent getDisplayComponent();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    FeatureInfoDisplayKey getDisplayKey();
}
