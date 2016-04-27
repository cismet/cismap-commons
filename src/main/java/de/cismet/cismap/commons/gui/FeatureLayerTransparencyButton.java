/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingConstants;

import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class FeatureLayerTransparencyButton extends JButton {

    //~ Static fields/initializers ---------------------------------------------

    public static final int POPUP_WIDTH = 35;

    //~ Instance fields --------------------------------------------------------

    private int prevValue = -1;

    private final JLabel labPercentage;
    private final JPanel panPopup;
    private final JPopupMenu popup;
    private final JSlider slider;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LTButton object.
     */
    public FeatureLayerTransparencyButton() {
        GridBagConstraints gridBagConstraints;

        popup = new JPopupMenu();
        panPopup = new JPanel();
        slider = new SliderMenuItem();
        labPercentage = new JLabel();

        panPopup.setLayout(new GridBagLayout());

        slider.setOrientation(JSlider.VERTICAL);
        slider.setValue(100);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        panPopup.add(slider, gridBagConstraints);

        labPercentage.setHorizontalAlignment(SwingConstants.CENTER);
        labPercentage.setText("100 %");
        labPercentage.setMaximumSize(new Dimension(POPUP_WIDTH, 17));
        labPercentage.setMinimumSize(new Dimension(POPUP_WIDTH, 17));
        labPercentage.setPreferredSize(new Dimension(POPUP_WIDTH, 17));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 2, 0);
        panPopup.add(labPercentage, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        setLayout(new GridBagLayout());

        addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent evt) {
                    if (CismapBroker.getInstance().getMappingComponent() != null) {
                        final float transp = CismapBroker.getInstance()
                                    .getMappingComponent()
                                    .getFeatureLayer()
                                    .getTransparency();
                        final int intValue = (int)(transp * 100);
                        slider.setValue(intValue);
                    }

                    updateLabel();
                    popup.show(
                        FeatureLayerTransparencyButton.this,
                        (getWidth() / 2)
                                - (POPUP_WIDTH / 2),
                        getHeight()
                                + 2);
                }
            });

        popup.add(panPopup);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void updateLabel() {
        final int intValue = (int)slider.getValue();
        if (prevValue != intValue) {
            labPercentage.setText(String.format("%d%%", slider.getValue()));
        }
        prevValue = intValue;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class SliderMenuItem extends JSlider implements MenuElement {

        //~ Instance fields ----------------------------------------------------

        private final SliderPopupListener popupHandler = new SliderPopupListener();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SliderMenuItem object.
         */
        public SliderMenuItem() {
            addMouseMotionListener(popupHandler);
            addMouseListener(popupHandler);
            addMouseWheelListener(popupHandler);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void processMouseEvent(final MouseEvent e,
                final MenuElement[] path,
                final MenuSelectionManager manager) {
        }

        @Override
        public void processKeyEvent(final KeyEvent e, final MenuElement[] path,
                final MenuSelectionManager manager) {
        }

        @Override
        public void menuSelectionChanged(final boolean isIncluded) {
        }

        @Override
        public MenuElement[] getSubElements() {
            return new MenuElement[0];
        }

        @Override
        public Component getComponent() {
            return this;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class SliderPopupListener extends MouseAdapter {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SliderPopupListener object.
         */
        public SliderPopupListener() {
            super();
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void mouseDragged(final MouseEvent me) {
            final int intValue = (int)slider.getValue();
            if (CismapBroker.getInstance().getMappingComponent() != null) {
                CismapBroker.getInstance()
                        .getMappingComponent()
                        .getFeatureLayer()
                        .setTransparency((float)intValue / 100f);
            }
            updateLabel();
        }

        @Override
        public void mouseReleased(final MouseEvent me) {
        }
    }
}
