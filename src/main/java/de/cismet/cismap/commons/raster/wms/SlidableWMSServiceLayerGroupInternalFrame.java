/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.raster.wms;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;

import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.wms.capabilities.Layer;

import de.cismet.tools.gui.VerticalTextIcon;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class SlidableWMSServiceLayerGroupInternalFrame extends JInternalFrame {

    //~ Static fields/initializers ---------------------------------------------

    private static final ImageIcon LOCK_ICON = new javax.swing.ImageIcon(
            SlidableWMSServiceLayerGroupInternalFrame.class.getResource(
                "/de/cismet/cismap/commons/raster/wms/res/lock.png"));        // NOI18N
    private static final ImageIcon UNLOCK_ICON = new javax.swing.ImageIcon(
            SlidableWMSServiceLayerGroupInternalFrame.class.getResource(
                "/de/cismet/cismap/commons/raster/wms/res/lock-unlock.png")); // NOI18N

    private static final transient Logger LOG = Logger.getLogger(SlidableWMSServiceLayerGroupInternalFrame.class);

    //~ Instance fields --------------------------------------------------------

    private final SlidableWMSServiceLayerGroupJSlider slider = new SlidableWMSServiceLayerGroupJSlider();
    private final JButton btnLock = new JButton();

    private SlidableWMSServiceLayerGroup model;
    private boolean allowCrossfade;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SlidableWMSServiceLayerGroupInternalFrame object.
     *
     * @param  model  DOCUMENT ME!
     */
    public SlidableWMSServiceLayerGroupInternalFrame(final SlidableWMSServiceLayerGroup model) {
        this.model = model;
        slider.setMinimum(0);
        slider.setMaximum((model.getLayers().size() - 1) * 100);
        slider.setValue(0);

        slider.setMinorTickSpacing(100);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBorder(new EmptyBorder(3, 3, 3, 3));

        final int mapCWidth = CismapBroker.getInstance().getMappingComponent().getWidth();
        double sliderWidth = slider.estimateSliderWidthHorizontalLabels();
        if ((sliderWidth / mapCWidth) < model.getVerticalLabelWidthThreshold()) {
            slider.drawLabels(SlidableWMSServiceLayerGroup.LabelDirection.HORIZONTAL);
        } else {
            slider.drawLabels(SlidableWMSServiceLayerGroup.LabelDirection.VERTICAL);
            sliderWidth = slider.estimateSliderWidthVerticalLabels();
        }

        this.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE); // NOI18N
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(slider, BorderLayout.CENTER);
        slider.setSnapToTicks(true);
        slider.repaint();

        btnLock.setText("");

        btnLock.setIcon(LOCK_ICON);
        btnLock.setBorder(null);
        btnLock.setContentAreaFilled(false);
        btnLock.setPreferredSize(new Dimension(32, (int)slider.getPreferredSize().getHeight()));
        btnLock.setFocusPainted(false);
        btnLock.setToolTipText(NbBundle.getMessage(
                SlidableWMSServiceLayerGroup.class,
                "SlidableWMSServiceLayerGroup.initDialog().btnLock.tooltip"));
        btnLock.setVisible(model.isResourceConserving());
        this.getContentPane().add(btnLock, BorderLayout.WEST);

        this.setPreferredSize(new Dimension((int)sliderWidth, (int)slider.getPreferredSize().getHeight()
                        + 15));
        this.pack();
        this.setResizable(true);
        addListenersFromModel();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getSliderValue() {
        return slider.getValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  enable  DOCUMENT ME!
     */
    public void enableSlider(final boolean enable) {
        slider.setEnabled(enable);
    }

    /**
     * DOCUMENT ME!
     */
    public void setLockIcon() {
        btnLock.setIcon(LOCK_ICON);
    }
    /**
     * DOCUMENT ME!
     */
    public void setUnlocIcon() {
        btnLock.setIcon(UNLOCK_ICON);
    }

    /**
     * DOCUMENT ME!
     */
    private void addListenersFromModel() {
        slider.addChangeListener(model);
        btnLock.addActionListener(model.getLockListener());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public SlidableWMSServiceLayerGroup getModel() {
        return model;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  model  DOCUMENT ME!
     */
    public void setModel(final SlidableWMSServiceLayerGroup model) {
        this.model = model;
        addListenersFromModel();
    }

    /**
     * DOCUMENT ME!
     */
    public void removeModel() {
        slider.removeChangeListener(model);
        btnLock.removeActionListener(model.getLockListener());
        model = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isAllowCrossfade() {
        return allowCrossfade;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class SlidableWMSServiceLayerGroupJSlider extends JSlider implements ComponentListener {

        //~ Instance fields ----------------------------------------------------

        private SlidableWMSServiceLayerGroup.LabelDirection labelDirection;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SlidableWMSServiceLayerGroupJSlider object.
         */
        public SlidableWMSServiceLayerGroupJSlider() {
            this.addComponentListener(this);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  direction  DOCUMENT ME!
         */
        public void drawLabels(final SlidableWMSServiceLayerGroup.LabelDirection direction) {
            labelDirection = direction;
            switch (direction) {
                case HORIZONTAL: {
                    drawLabelsHorizontally();
                    break;
                }
                case VERTICAL: {
                    drawLabelsVertically();
                    break;
                }
            }
        }

        /**
         * DOCUMENT ME!
         */
        private void drawLabelsHorizontally() {
            this.setLabelTable(null);
            final Hashtable lableTable = new Hashtable();
            int x = 0;
            for (final WMSServiceLayer wsl : model.getLayers()) {
                final String layerTitle = getLayerTitle(wsl);

                final JLabel label = new JLabel(layerTitle);
                final Font font = label.getFont().deriveFont(10f);
                label.setFont(font);
                lableTable.put(x * 100, label);
                x++;
            }
            this.setLabelTable(lableTable);
        }

        /**
         * DOCUMENT ME!
         */
        private void drawLabelsVertically() {
            this.setLabelTable(null);
            final Hashtable lableTable = new Hashtable();
            int x = 0;
            for (final WMSServiceLayer wsl : model.getLayers()) {
                final String layerTitle = getLayerTitle(wsl);

                final JLabel label = new JLabel();
                label.setIcon(new VerticalTextIcon(layerTitle, false));
                lableTable.put(x * 100, label);
                x++;
            }
            this.setLabelTable(lableTable);
        }

        /**
         * DOCUMENT ME!
         */
        private void drawDisabledLabelsVertically() {
            this.setLabelTable(null);
            final Hashtable lableTable = new Hashtable();
            int x = 0;
            for (final WMSServiceLayer wsl : model.getLayers()) {
                final String layerTitle = getLayerTitle(wsl);
                final JLabel label = new JLabel();
                label.setIcon(new VerticalTextIcon(layerTitle, false, Color.DARK_GRAY));
                lableTable.put(x * 100, label);
                x++;
            }
            this.setLabelTable(lableTable);
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public double estimateSliderWidthHorizontalLabels() {
            final StringBuilder text = new StringBuilder();
            for (final WMSServiceLayer wsl : model.getLayers()) {
                final String layerTitle = getLayerTitle(wsl);
                text.append(layerTitle);
                text.append("  ");
            }

            return this.getFontMetrics(this.getFont()).getStringBounds(text.toString(), this.getGraphics()).getWidth();
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public double estimateSliderWidthVerticalLabels() {
            double sliderWidth = 0;
            final Icon icon = new VerticalTextIcon(getLayerTitle(model.getLayers().get(0)), false);
            final int iconWidth = icon.getIconWidth();
            final int gap = 5;
            for (final WMSServiceLayer wsl : model.getLayers()) {
                sliderWidth += iconWidth + gap;
            }

            return sliderWidth;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   layer  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String getLayerTitle(final WMSServiceLayer layer) {
            String layerTitle = null;
            try {
                final String[] keywords = ((Layer)layer.ogcLayers.get(0)).getKeywords();
                for (final String keyword : keywords) {
                    if (keyword.startsWith("cismapSlidingLayerGroupMember.tickTitle")) {
                        layerTitle = keyword.split(":")[1];
                    }
                }
            } catch (Exception ex) {
                LOG.error("An error occured while parsing tickTitle. Use layer title or name.", ex);
            }

            if (layerTitle == null) {
                layerTitle = layer.getTitle();
                if (layerTitle == null) {
                    layerTitle = layer.getName();
                }

                if ((layerTitle != null) && (layerTitle.length() > 8)) {
                    layerTitle = layerTitle.substring(0, 3) + "." + layerTitle.substring(layerTitle.length() - 4); // NOI18N
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("No title found for WMSServiceLayer '" + layer + "'.");
                    }
                }
            }
            return layerTitle;
        }

        @Override
        public void setEnabled(final boolean enabled) {
            super.setEnabled(enabled);
            if ((labelDirection != null)
                        && labelDirection.equals(SlidableWMSServiceLayerGroup.LabelDirection.VERTICAL)) {
                if (enabled) {
                    drawLabelsVertically();
                } else {
                    drawDisabledLabelsVertically();
                }
            }
        }

        @Override
        public void componentResized(final ComponentEvent e) {
            allowCrossfade = ((this.getWidth() * 1. / model.getLayers().size()) > 30) || model.isCrossfadeEnabled();
        }

        @Override
        public void componentMoved(final ComponentEvent e) {
        }

        @Override
        public void componentShown(final ComponentEvent e) {
            allowCrossfade = ((this.getWidth() * 1. / model.getLayers().size()) > 30) || model.isCrossfadeEnabled();
        }

        @Override
        public void componentHidden(final ComponentEvent e) {
        }
    }
}
