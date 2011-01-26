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
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface FeatureInfoDisplay {

    //~ Methods ----------------------------------------------------------------

    /**
     * This operation shall be called before the application sends {@link MapClickedEvent}s to the <code>
     * FeatureInfoDisplay</code>. It shall be used to provide necessary information to the <code>
     * FeatureInfoDisplay</code> so that it can serve its purpose in displaying feature information for certain points.
     *
     * @param   layer             The layer object that will use this <code>FeatureInfoDisplay</code>
     * @param   parentTabbedPane  the parent component of the display
     *
     * @throws  InitialisationException  if any error occurs during initialisation (e.g. the <code>
     *                                   FeatureInfoDisplay</code> does not accept the given layer implementation)
     *
     * @see     #acceptLayer(java.lang.Class)
     */
    void init(Object layer, JTabbedPane parentTabbedPane) throws InitialisationException;

    /**
     * Processes the {@link MapClickedEvent} and most likely somehow display information within the display component.
     *
     * @param  mce  the <code>MapClickedEvent</code> to process
     *
     * @see    #getDisplayComponent()
     */
    void showFeatureInfo(MapClickedEvent mce);

    /**
     * Retrieves the component that is responsible for displaying any results of calls to <code>showFeatureInfo.</code>
     *
     * @return  the displaying component
     *
     * @see     #showFeatureInfo(de.cismet.cismap.commons.interaction.events.MapClickedEvent)
     */
    JComponent getDisplayComponent();

    /**
     * Retrieves the {@link FeatureInfoDisplayKey} information that determines which layer implementation class this
     * instance accepts and which server and layer it will be responsible for.
     *
     * @return  the <code>FeatureInfoDisplayKey</code>
     *
     * @see     #acceptLayer(java.lang.Class)
     */
    FeatureInfoDisplayKey getDisplayKey();

    /**
     * Determines whether this <code>FeatureInfoDisplay</code> will accept the given layer <code>Class</code>. If a
     * certain class is accepted by the display it can in general be used to show a layer's feature information.
     *
     * @param   layerClass  the <code>Class</code> of the layer to be checked for acceptance
     *
     * @return  true if this <code>FeatureInfoDisplay</code> accepts the given layer, false otherwise
     */
    boolean acceptLayer(Class layerClass);
}
