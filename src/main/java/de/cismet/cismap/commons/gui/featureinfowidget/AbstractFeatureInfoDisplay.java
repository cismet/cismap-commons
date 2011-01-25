/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.featureinfowidget;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public abstract class AbstractFeatureInfoDisplay extends JPanel implements FeatureInfoDisplay {

    //~ Instance fields --------------------------------------------------------

    private final FeatureInfoDisplayKey displayKey;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractFeatureInfoDisplay object.
     *
     * @param   displayKey  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public AbstractFeatureInfoDisplay(final FeatureInfoDisplayKey displayKey) {
        if (displayKey == null) {
            throw new IllegalArgumentException("not allowed to use null display key"); // NOI18N
        }

        this.displayKey = displayKey;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public final JComponent getDisplayComponent() {
        return this;
    }

    @Override
    public FeatureInfoDisplayKey getDisplayKey() {
        return displayKey;
    }
}
