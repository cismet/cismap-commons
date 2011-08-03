/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import Sirius.navigator.plugin.PluginRegistry;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;

import org.apache.log4j.Logger;

import org.jfree.util.Log;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.InputStream;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.features.SignaturedFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.featureinfowidget.FeatureInfoDisplay;
import de.cismet.cismap.commons.gui.featureinfowidget.FeatureInfoWidget;
import de.cismet.cismap.commons.gui.featureinfowidget.MultipleFeatureInfoRequestsDisplay;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.gui.piccolo.FixedPImage;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.MapClickListener;
import de.cismet.cismap.commons.interaction.events.MapClickedEvent;

import de.cismet.cismap.navigatorplugin.CismapPlugin;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class GetFeatureInfoClickDetectionListener extends PBasicInputEventHandler implements HoldListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            "de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget"); // NOI18N
    public static final String FEATURE_INFO_MODE = "FEATURE_INFO_CLICK";       // NOI18N
    private static final transient Logger LOG = Logger.getLogger(GetFeatureInfoClickDetectionListener.class);

    //~ Instance fields --------------------------------------------------------

    private BufferedImage info;
    private FixedPImage pInfo;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of GetFeatureInfoClickDetectionListener.
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public GetFeatureInfoClickDetectionListener() {
        final InputStream is = this.getClass().getResourceAsStream("/de/cismet/cismap/commons/gui/res/featureInfo.png"); // NOI18N
        try {
            info = ImageIO.read(is);
        } catch (IOException ex) {
            LOG.warn("Could not load featureInfo icon", ex);                                                             // NO18N
        }
        if (info == null) {
            final String msg = "Could not load featureInfoIcon";                                                         // NOI18N
            LOG.error(msg);
            throw new IllegalStateException(msg);
        }
        final Graphics2D g2 = (Graphics2D)info.getGraphics();
//        g2.setPaint(new Color(255, 85, 85));
//        g2.setStroke(new BasicStroke(4));
//        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
//        g2.drawOval(6, 8, 26, 26);
        pInfo = new FixedPImage(new ImageIcon(info).getImage());
        getPInfo().setSweetSpotX(0.5d);
        getPInfo().setSweetSpotY(1d);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void mouseClicked(final edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
        if (pInputEvent.getComponent() instanceof MappingComponent) {
            boolean paintFeatureInfoIcon = true;
            final Vector<MapClickListener> v = CismapBroker.getInstance().getMapClickListeners();
            for (final MapClickListener listener : v) {
                if (listener instanceof FeatureInfoWidget) {
                    final FeatureInfoWidget infoWidget = (FeatureInfoWidget)listener;
                    final Map<Object, FeatureInfoDisplay> displays = infoWidget.getDisplays();
                    final Collection<FeatureInfoDisplay> c = displays.values();
                    for (final FeatureInfoDisplay d : c) {
                        if (d instanceof MultipleFeatureInfoRequestsDisplay) {
                            if (((MultipleFeatureInfoRequestsDisplay)d).isOnHold()) {
                                paintFeatureInfoIcon = false;
                            }
                            ((MultipleFeatureInfoRequestsDisplay)d).addHoldListener(this);
                        }
                    }
                }
            }
            if (paintFeatureInfoIcon) {
                final MappingComponent mc = (MappingComponent)pInputEvent.getComponent();
                mc.addStickyNode(getPInfo());
                mc.getRubberBandLayer().removeAllChildren();
                mc.getTmpFeatureLayer().removeAllChildren();
//                pInfo = new PImage(info.getImage());
                mc.getRubberBandLayer().addChild(getPInfo());
                getPInfo().setScale(1 / mc.getCamera().getViewScale());
                getPInfo().setOffset(pInputEvent.getPosition().getX(), pInputEvent.getPosition().getY());
                if (log.isDebugEnabled()) {
                    log.debug(getPInfo().getGlobalBounds().getWidth());
                }
                getPInfo().setVisible(true);
//                 mc.getCasmera().animateViewToCenterBounds(pInfo.getBounds(),true,1000);
                getPInfo().repaint();
                mc.repaint();
            }
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

    @Override
    public void holdFeautresChanged(final HoldFeatureChangeEvent evt) {
        showCustomFeatureInfo(evt.getHoldFeatures(), evt.getMultipleFeautureInfoRequestDisplay());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c        DOCUMENT ME!
     * @param   display  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private void showCustomFeatureInfo(final Collection<SignaturedFeature> c,
            final MultipleFeatureInfoRequestsDisplay display) {
        SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    display.removeHoldListener(GetFeatureInfoClickDetectionListener.this);
                }
            });

        final CismapPlugin cismapPl = (CismapPlugin)PluginRegistry.getRegistry().getPlugin("cismap"); // NOI18N
        final MappingComponent mc = cismapPl.getMappingComponent();

        // TODO if !display.isOnHold paint the standard icon...
        if (!display.isOnHold() || ((c == null) || c.isEmpty())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    "MultipleFeauteInfoRequestsDisplay is not on hold or holdFeautureCollection is null or empty.");   // NOI18N
            }
            return;
        } else {
            mc.getRubberBandLayer().removeAllChildren();
            mc.getTmpFeatureLayer().removeAllChildren();
            BufferedImage featureInfoIcon = null;
            BufferedImage lastFeatureInfoIcon = null;
            BufferedImage originalFeatureInfoIcon = null;
            InputStream is = this.getClass().getResourceAsStream("/de/cismet/cismap/commons/gui/res/featureInfo.png"); // NOI18N
            try {
                originalFeatureInfoIcon = ImageIO.read(is);
                is.close();
                is = this.getClass().getResourceAsStream("/de/cismet/cismap/commons/gui/res/featureInfo.png");
                featureInfoIcon = ImageIO.read(is);
                is.close();
                is = this.getClass().getResourceAsStream("/de/cismet/cismap/commons/gui/res/lastFeatureInfo.png");
                lastFeatureInfoIcon = ImageIO.read(is);
                is.close();
            } catch (IOException ex) {
                LOG.warn("Could not load featureInfo icon", ex);                                                       // NO18N
            }
            if (featureInfoIcon == null) {
                final String msg = "Could not load featureInfoIcon";                                                   // NOI18N
                LOG.error(msg);
                throw new IllegalStateException(msg);
            } else if (lastFeatureInfoIcon == null) {
                final String msg = "Could not load lastFeatureInfoIcon";                                               // NOI18N
                LOG.error(msg);
                throw new IllegalStateException(msg);
            }

            // set the overlay on the lower left edge of the icon as default..
            int width = 16;
            int height = 16;
            int xPos = featureInfoIcon.getWidth() - width;
            int yPos = featureInfoIcon.getHeight() - height;
            int bgR = 255;
            int bgG = 255;
            int bgB = 255;
            Color standardBG = new Color(bgR, bgG, bgB);
            int lastBGR = 255;
            int lastBGG = 255;
            int lastBGB = 255;
            Color lastBG = new Color(lastBGR, lastBGG, lastBGB);
            BufferedImage subimage = featureInfoIcon.getSubimage(xPos, yPos, width, height);
            Graphics2D g2d = (Graphics2D)subimage.getGraphics();

            // try to get metainformation for overlay position, width, color from properties file and override the
            // default values if succesfull
            final Properties iconProps = new Properties();
            try {
                final InputStream in = getClass().getResourceAsStream(
                        "/de/cismet/cismap/commons/gui/res/featureInfoIcon.properties");                             // NOI18N
                if (in != null) {
                    iconProps.load(in);
                    in.close();
                } else {
                    LOG.warn(
                        "Could not laod featureInfoIcon.properties file. Default values for overlay area are used"); // NOI18N
                }
            } catch (IOException ex) {
                LOG.error(
                    "Could not read featureInfoIcon.properties file. Default values for overlay area are used",
                    ex);                                                                                             // NOI18N
            }

            if (iconProps.isEmpty()
                        || !(iconProps.containsKey("overlayPositionX")
                            && iconProps.containsKey("overlayPositionY")                                       // NOI18N
                            && iconProps.containsKey("overlayWidth") && iconProps.containsKey("overlayHeigth") // NOI18N
                            && iconProps.containsKey("overlayBackgroundColorR")                                // NOI18N
                            && iconProps.containsKey("overlayBackgroundColorG")                                // NOI18N
                            && iconProps.containsKey("overlayBackgroundColorB")
                            && iconProps.containsKey("lastOverlayBackgroundColorR")                            // NOI18N
                            && iconProps.containsKey("lastOverlayBackgroundColorG")                            // NOI18N
                            && iconProps.containsKey("lastOverlayBackgroundColorB"))) {                        // NOI18N
                // TODO ERROR LOG EXCPETION
                LOG.warn(
                    "featureInfoIcon.properties file does not contain all needed keys. Default values for overlay area are used"); // NOI18N
            } else {
                try {
                    xPos = Integer.parseInt((String)iconProps.get("overlayPositionX"));                                            // NOI18N
                    yPos = Integer.parseInt((String)iconProps.get("overlayPositionY"));                                            // NOI18N
                    width = Integer.parseInt((String)iconProps.get("overlayWidth"));                                               // NOI18N
                    height = Integer.parseInt((String)iconProps.get("overlayHeigth"));                                             // NOI18N
                    bgR = Integer.parseInt((String)iconProps.get("overlayBackgroundColorR"));                                      // NOI18N
                    bgG = Integer.parseInt((String)iconProps.get("overlayBackgroundColorG"));                                      // NOI18N
                    bgB = Integer.parseInt((String)iconProps.get("overlayBackgroundColorB"));                                      // NOI18N
                    lastBGR = Integer.parseInt((String)iconProps.get("lastOverlayBackgroundColorR"));                              // NOI18N
                    lastBGG = Integer.parseInt((String)iconProps.get("lastOverlayBackgroundColorG"));                              // NOI18N
                    lastBGB = Integer.parseInt((String)iconProps.get("lastOverlayBackgroundColorB"));                              // NOI18N
                    standardBG = new Color(bgR, bgG, bgB);
                    lastBG = new Color(lastBGR, lastBGG, lastBGB);
                    subimage = featureInfoIcon.getSubimage(xPos, yPos, width, height);
                    g2d = (Graphics2D)subimage.getGraphics();
                } catch (NumberFormatException ex) {
                    Log.error(
                        "Error while retrieving properties for overlay area. Default values for overlay area are used",            // NOI18N
                        ex);
                }
            }

            // add a node for each SignaturedFeature to the map
            int nr = 0;
            for (final SignaturedFeature f : c) {
                final DefaultStyledFeature dsf = new DefaultStyledFeature();
                final FeatureAnnotationSymbol symb;
                if (f.getOverlayIcon() == null) {
                    // paint standard overlay, little circle and a number within or so...
                    symb = new FeatureAnnotationSymbol(originalFeatureInfoIcon);
                    Log.warn(
                        "OverlayIcon for MultipleFeautreInfoRequestDisplay is null, default FeatureInfoIcon is used"); // NOI18N
                } else {
                    if (nr == (c.size() - 1)) {
                        if (lastFeatureInfoIcon != null) {
                            g2d = (Graphics2D)lastFeatureInfoIcon.getSubimage(xPos, yPos, width, height).getGraphics();
                        }
                        g2d.drawImage(f.getOverlayIcon(), 0, 0, lastBG, cismapPl);
                        symb = new FeatureAnnotationSymbol(lastFeatureInfoIcon);
                    } else {
                        g2d.drawImage(f.getOverlayIcon(), 0, 0, standardBG, cismapPl);
                        symb = new FeatureAnnotationSymbol(featureInfoIcon);
                    }
                }

                symb.setSweetSpotX(0.5);
                symb.setSweetSpotY(0.9);
                dsf.setPointAnnotationSymbol(symb);
                dsf.setGeometry(f.getGeometry());

                final PFeature pf = new PFeature(dsf, mc);
                mc.addStickyNode(pf);
                mc.getTmpFeatureLayer().addChild(pf);
                nr++;
            }
            mc.rescaleStickyNodes();
            mc.repaint();
        }
    }
}
