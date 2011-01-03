/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;

import javax.swing.ImageIcon;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FixedPImage;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.MapClickedEvent;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class GetFeatureInfoClickDetectionListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            "de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget"); // NOI18N
    public static final String FEATURE_INFO_MODE = "FEATURE_INFO_CLICK";       // NOI18N

    //~ Instance fields --------------------------------------------------------

    private ImageIcon info = new javax.swing.ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/res/featureInfo.png")); // NOI18N
    private FixedPImage pInfo = new FixedPImage(info.getImage());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of GetFeatureInfoClickDetectionListener.
     */
    public GetFeatureInfoClickDetectionListener() {
        getPInfo().setSweetSpotX(0.5d);
        getPInfo().setSweetSpotY(1d);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            final MappingComponent mc = (MappingComponent)pInputEvent.getComponent();
            mc.addStickyNode(getPInfo());
            mc.getRubberBandLayer().removeAllChildren();
            // pInfo =new PImage(info.getImage());
            mc.getRubberBandLayer().addChild(getPInfo());
            getPInfo().setScale(1 / mc.getCamera().getViewScale());
            getPInfo().setOffset(pInputEvent.getPosition().getX(), pInputEvent.getPosition().getY());
            if (log.isDebugEnabled()) {
                log.debug(getPInfo().getGlobalBounds().getWidth());
            }
            getPInfo().setVisible(true);
            // mc.getCamera().animateViewToCenterBounds(pInfo.getBounds(),true,1000);
            getPInfo().repaint();
            mc.repaint();
        }
        CismapBroker.getInstance().fireClickOnMap(new MapClickedEvent(FEATURE_INFO_MODE, pInputEvent));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FixedPImage getPInfo() {
        return pInfo;
    }
}
