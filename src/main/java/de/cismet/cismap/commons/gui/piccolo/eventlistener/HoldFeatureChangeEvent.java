/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import java.util.Collection;

import de.cismet.cismap.commons.features.SignaturedFeature;
import de.cismet.cismap.commons.gui.featureinfowidget.MultipleFeatureInfoRequestsDisplay;

/**
 * DOCUMENT ME!
 *
 * @author   dmeiers
 * @version  $Revision$, $Date$
 */
public class HoldFeatureChangeEvent {

    //~ Instance fields --------------------------------------------------------

    private Collection<SignaturedFeature> holdFeatures;
    private MultipleFeatureInfoRequestsDisplay multipleFeautureInfoRequestDisplay;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HoldFeatureChangeEvent object.
     *
     * @param   c        DOCUMENT ME!
     * @param   display  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public HoldFeatureChangeEvent(final Collection<SignaturedFeature> c,
            final MultipleFeatureInfoRequestsDisplay display) {
        this.holdFeatures = c;
        if (display == null) {
            throw new IllegalStateException("display can not be null"); // NOI18N
        }
        this.multipleFeautureInfoRequestDisplay = display;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<SignaturedFeature> getHoldFeatures() {
        return holdFeatures;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MultipleFeatureInfoRequestsDisplay getMultipleFeautureInfoRequestDisplay() {
        return multipleFeautureInfoRequestDisplay;
    }
}
