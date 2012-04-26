/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.layerwidget;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import de.cismet.cismap.commons.Debug;
import de.cismet.cismap.commons.LayerInfoProvider;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.gui.simplelayerwidget.NewSimpleInternalLayerWidget;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.wms.capabilities.Style;

import de.cismet.tools.CurrentStackTrace;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.treetable.TreeTableModelAdapter;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class ActiveLayerTableCellRenderer extends DefaultTableCellRenderer {

    //~ Static fields/initializers ---------------------------------------------

    protected static final boolean DEBUG = Debug.DEBUG;
    private static final Dimension ZERO_DIMENSION = new Dimension(0, 0);

    //~ Instance fields --------------------------------------------------------

    private javax.swing.ImageIcon styleIcon;
    private javax.swing.ImageIcon unselectedStyleIcon;
    private LayerWidget layerWidgetParent = null;
    private NewSimpleInternalLayerWidget internalWidgetParent = null;
    private JButton moreButton = new JButton("..."); // NOI18N
    private JSlider slider = new JSlider(0, 100);
    private JSlider slider2 = new JSlider(0, 100);
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private ImageIcon layersIcon;
    private ImageIcon layerIcon;
    private ImageIcon layersInfoIcon;
    private ImageIcon layerInfoIcon;
    private ImageIcon okIcon;
    private ImageIcon errorIcon;
    private ImageIcon refreshIcon;
    private ImageIcon refreshNeededIcon;
    private ImageIcon inProgressIcon;
    // private Timer indeterminateProgressTimer = null;
    /** Liste mit update Timers. FIXME: bei 50 ist schloss. ArrayList? */
    private Timer[] indeterminateProgressTimers = new Timer[50];
    private StyleLabel styleLabel = new StyleLabel();
    private boolean isWidgetTable = false;
    private JProgressBar progressBar = new JProgressBar(0, 100) {

            @Override
            public boolean isDisplayable() {
                // This does the trick. It makes sure animation is always performed
                return true;
            }
        };

    private JProgressBar progressIndeterminate = new JProgressBar(0, 100) {

            @Override
            public boolean isDisplayable() {
                // This does the trick. It makes sure animation is always performed
                return true;
            }
        };

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ActiveLayerTableCellRenderer.
     */
    public ActiveLayerTableCellRenderer() {
//        progress.setUI(new MetalProgressBarUI());
//        progress.setString("");progress.setStringPainted(true);
//        progressIndeterminate.setUI(new MetalProgressBarUI());
//        progressIndeterminate.setString("");progressIndeterminate.setStringPainted(true);
        styleIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/style.png"));         // NOI18N
        unselectedStyleIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/disabledStyle.png")); // NOI18N
        moreButton.setEnabled(false);
        styleLabel.setOpaque(false);
        styleLabel.setBackground(Color.WHITE);
        // progressBar.setLayout(new BorderLayout(2, 2));
        progressBar.setLayout(new GridLayout(1, 1));
        slider.setOpaque(false);
        progressBar.add(slider); // , BorderLayout.CENTER);

        progressIndeterminate.setLayout(new BorderLayout(2, 2));
        slider2.setOpaque(false);
        progressIndeterminate.add(slider2, BorderLayout.CENTER);
        progressIndeterminate.setIndeterminate(true);

        layersIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layers.png"));   // NOI18N
        layerIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layer.png"));    // NOI18N
        layersInfoIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layers_i.png")); // NOI18N
        layerInfoIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layer_i.png"));  // NOI18N

        okIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/enable30.png"));   // NOI18N
        errorIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/error.png"));      // NOI18N
        refreshIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/refresh.png"));    // NOI18N
        refreshNeededIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/refresh30.png"));  // NOI18N
        inProgressIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/inProgress.png")); // NOI18N
    }

    /**
     * Creates a new ActiveLayerTableCellRenderer object.
     *
     * @param  widgetTable  DOCUMENT ME!
     */
    public ActiveLayerTableCellRenderer(final boolean widgetTable) {
        this();
        this.isWidgetTable = widgetTable;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the default table cell renderer.
     *
     * @param   table       the <code>JTable</code>
     * @param   value       the value to assign to the cell at <code>[row, column]</code>
     * @param   isSelected  true if cell is selected
     * @param   hasFocus    true if cell has focus
     * @param   row         the row of the cell to render
     * @param   column      the column of the cell to render
     *
     * @return  the default table cell renderer
     */
    @Override
    public Component getTableCellRendererComponent(final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column) {
        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug(value + ": column=" + column);
            }
        }

        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug(value + ": column=" + column + ", crw=" + row, new CurrentStackTrace());
            }
        }
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        final int realColumn = table.convertColumnIndexToModel(column);
        final int realRow = table.convertRowIndexToModel(row);
        final TableCellRenderer booleanRenderer = table.getDefaultRenderer(Boolean.class);
        final TableCellRenderer stringRenderer = table.getDefaultRenderer(String.class);

        if (DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug(value + ": realColumn=" + realColumn + ", realRow=" + realRow, new CurrentStackTrace());
            }
        }

        setToolTipText(null);
        if (!isWidgetTable) {
            if (layerWidgetParent == null) {
                layerWidgetParent = StaticSwingTools.findSpecificParentComponent(table, LayerWidget.class);
                layerWidgetParent.setErrorImage(null);
            } else {
                layerWidgetParent.setErrorImage(null);
            }
        } else {
            if (internalWidgetParent == null) {
                internalWidgetParent = StaticSwingTools.findSpecificParentComponent(
                        table,
                        NewSimpleInternalLayerWidget.class);
                internalWidgetParent.setErrorImage(null);
            } else {
                internalWidgetParent.setErrorImage(null);
            }
        }
        if (realColumn == 0) {
            setText(""); // NOI18N
            // label.setIconTextGap(4);
            setHorizontalAlignment(JLabel.CENTER);
            if (value instanceof RetrievalServiceLayer) {
                final int progress = ((RetrievalServiceLayer)value).getProgress();
                if (((RetrievalServiceLayer)value).isRefreshNeeded()) {
                    if (DEBUG) {
                        log.warn(value + " isRefreshNeeded");                 // NOI18N
                    }
                    setIcon(refreshNeededIcon);
                } else if ((progress > 0) && (progress < 100)) {
                    if (DEBUG) {
                        if (log.isDebugEnabled()) {
                            log.debug(value + " isInProgress");               // NOI18N
                        }
                    }
                    setIcon(inProgressIcon);
                } else if (((RetrievalServiceLayer)value).hasErrors()) {
                    if (DEBUG) {
                        log.warn(value + " has errors");                      // NOI18N
                    }
                    setIcon(errorIcon);
                    if (((RetrievalServiceLayer)value).getErrorObject() instanceof Image) {
                        if (!isWidgetTable) {
                            if (layerWidgetParent == null) {
                                layerWidgetParent = StaticSwingTools.findSpecificParentComponent(
                                        table,
                                        LayerWidget.class);
                                layerWidgetParent.setErrorImage((Image)((RetrievalServiceLayer)value).getErrorObject());
                            } else {
                                layerWidgetParent.setErrorImage((Image)((RetrievalServiceLayer)value).getErrorObject());
                            }
                        } else {
                            if (internalWidgetParent == null) {
                                internalWidgetParent = StaticSwingTools.findSpecificParentComponent(
                                        table,
                                        NewSimpleInternalLayerWidget.class);
                                internalWidgetParent.setErrorImage((Image)((RetrievalServiceLayer)value)
                                            .getErrorObject());
                            } else {
                                internalWidgetParent.setErrorImage((Image)((RetrievalServiceLayer)value)
                                            .getErrorObject());
                            }
                        }
                        setToolTipText(org.openide.util.NbBundle.getMessage(
                                ActiveLayerTableCellRenderer.class,
                                "ActiveLayerTableCellRenderer.toolTipText")); // NOI18N
                    } else if (((RetrievalServiceLayer)value).getErrorObject() instanceof String) {
                        setToolTipText(((RetrievalServiceLayer)value).getErrorObject().toString());
                    }
                } else {
                    if (DEBUG) {
                        if (log.isDebugEnabled()) {
                            log.debug(value + " isFinished");                 // NOI18N
                        }
                    }
                    setIcon(okIcon);
                }
            }
        } else if (realColumn == 2) {
            setText("");                                                      // NOI18N
            setIcon(null);
            if (value instanceof WMSServiceLayer) {
                if (((WMSServiceLayer)value).getWMSLayers().size() > 1) {
                    setText("");                                              // NOI18N
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                    setText("");                                              // NOI18N
                    // label.setIconTextGap(4);
                    Style selectedStyle = null;
                    String styleName = null;

                    int styleCount = 0;

                    // Kann nur ein WMSLayer haben (wegen Bedingung weiter oben)
                    final List<WMSLayer> wmsLayers = ((WMSServiceLayer)value).getWMSLayers();

                    if (!((WMSServiceLayer)value).isDummy() && !wmsLayers.isEmpty()) {
                        selectedStyle = wmsLayers.get(0).getSelectedStyle();
                        styleCount = wmsLayers.get(0).getOgcCapabilitiesLayer().getStyles().length;
                    } else if (!wmsLayers.isEmpty()) {
                        styleName = wmsLayers.get(0).getStyleName();
                    }

                    if (selectedStyle != null) {
                        styleName = selectedStyle.getTitle();
                    }
                    if (styleName != null) {
                        setText(styleName);
                    }

                    if (styleCount <= 1) {
                        setIcon(unselectedStyleIcon);
                    } else {
                        setIcon(styleIcon);
                    }
                }
            } else if (value instanceof AbstractFeatureService) {
                // final Color bg = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                // column).getBackground(); return temp;
                if (DEBUG) {
                    if (log.isDebugEnabled()) {
                        log.debug(value + " getStyleLabel (" + realRow + ", " + realColumn + ")"); // NOI18N
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("is it null? " + ((AbstractFeatureService)value).getLayerProperties());
                }

                if (((AbstractFeatureService)value).getLayerProperties() != null) {
                    styleLabel.style = ((AbstractFeatureService)value).getLayerProperties().getStyle();
                }

                styleLabel.repaint();
                return styleLabel;
            }
        } else if (realColumn == 3) {
            final WMSLayer wmsLayer = null;
            setText(""); // NOI18N
            setIcon(null);
            setHorizontalAlignment(JLabel.LEFT);
//            if (value instanceof WMSServiceLayer && ((WMSServiceLayer) value).getWMSLayers().size() > 1) {
//            } else {
//                if (value instanceof WMSServiceLayer && ((WMSServiceLayer) value).getWMSLayers().size() == 1) {
//                    wmsLayer = ((WMSLayer) ((WMSServiceLayer) value).getWMSLayers().get(0));
//                } else if (value instanceof WMSLayer) {
//                    wmsLayer = (WMSLayer) value;
//                }
//                if (wmsLayer != null && wmsLayer.getOgcCapabilitiesLayer().isQueryable()) {
//                    return booleanRenderer.getTableCellRendererComponent(table, new Boolean(wmsLayer.isQuerySelected()), isSelected, hasFocus, row, column);
//                } else {
//                    setIcon(null);
//                    setText("");//NOI18N
//                }
//            }
            if ((value instanceof LayerInfoProvider) && ((LayerInfoProvider)value).isQueryable()) {
                if (((LayerInfoProvider)value).isLayerQuerySelected()) {
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    if (value instanceof LayerInfoProvider) {
                                        ((LayerInfoProvider)value).setLayerQuerySelected(true);
                                        final ActiveLayerEvent ale = new ActiveLayerEvent();
                                        ale.setLayer(value);
                                        CismapBroker.getInstance().fireLayerInformationStatusChanged(ale);
                                    }
                                } catch (Exception ex) {
                                    log.error("Error in actionPerformed of the informationCheckBos", ex);
                                }
                            }
                        });
                }

                return booleanRenderer.getTableCellRendererComponent(
                        table,
                        ((LayerInfoProvider)value).isLayerQuerySelected(),
                        isSelected,
                        hasFocus,
                        row,
                        column);
            } else {
                setIcon(null);
                setText(""); // NOI18N
            }
        } else if (realColumn == 4) {
            // Component returnComp = null;
            if (value instanceof RetrievalServiceLayer) {
                slider.setValue((int)(((RetrievalServiceLayer)value).getTranslucency() * 100));
                slider2.setValue((int)(((RetrievalServiceLayer)value).getTranslucency() * 100));
                this.slider.updateUI();
                this.slider2.updateUI();

                final int currentProgress = ((RetrievalServiceLayer)value).getProgress();
                // -1: progress is indeterminate
                if ((currentProgress == -1) && ((RetrievalServiceLayer)value).isEnabled()
                            && !((RetrievalServiceLayer)value).isRefreshNeeded()) {
                    if (indeterminateProgressTimers[realRow] == null) {
                        if (DEBUG) {
                            if (log.isDebugEnabled()) {
                                log.debug("new indeterminateProgressTimers[" + realRow + "] created"); // NOI18N
                            }
                        }
                        indeterminateProgressTimers[realRow] = new Timer(30, new ActionListener() {

                                    @Override
                                    public void actionPerformed(final ActionEvent e) {
                                        final int currentProgress = ((RetrievalServiceLayer)value).getProgress();

                                        // redraw only progress bar column (indeterminate)
                                        ((TreeTableModelAdapter)(table.getModel())).fireTableCellUpdated(
                                            realRow,
                                            realColumn);

                                        if ((currentProgress == 0) || (currentProgress == 100)) {
                                            indeterminateProgressTimers[realRow].stop();
                                            indeterminateProgressTimers[realRow].setRepeats(false);
                                            indeterminateProgressTimers[realRow] = null;
                                            if (DEBUG) {
                                                if (log.isDebugEnabled()) {
                                                    log.debug(
                                                        value
                                                                + ": indeterminateProgressTimer["
                                                                + realRow
                                                                + "] stopped from Timer"); // NOI18N
                                                }
                                            }

                                            // redraw all columns (including status icon)
                                            final TableModelEvent evt = new TableModelEvent(
                                                    table.getModel(),
                                                    realRow,
                                                    realRow,
                                                    TableModelEvent.UPDATE);
                                            ((TreeTableModelAdapter)(table.getModel())).fireTableChanged(evt);
                                        }
                                    }
                                });
                    }

                    if (!indeterminateProgressTimers[realRow].isRunning()) {
                        // this.progressBar.invalidate();
                        indeterminateProgressTimers[realRow].start();
                        indeterminateProgressTimers[realRow].setRepeats(true);
                    }

                    // In this scenario of displaying a JSlider in a JProgressBar as renderer for a table cell, the
                    // JSlider component resizes sloppy. But only the JSlider in the firt row shows this effect. If you
                    // don't set the size of the JSlider you easily can reconstruct this effect by forcing the layer
                    // widget to resize with the help of the docking framework. See
                    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7092176 for more details.
                    this.slider2.setSize(ZERO_DIMENSION);
                    this.slider2.updateUI();
                    return progressIndeterminate;
                } else {
                    if (DEBUG) {
                        if (log.isDebugEnabled()) {
                            log.debug(value + ": getProgress(): " + currentProgress + " (realColumn=" + realColumn
                                        + ", realRow=" + realRow + ") (column=" + column + ", row=" + row + ")"); // NOI18N
                        }
                    }

                    this.progressBar.setValue(currentProgress);
                    if ((indeterminateProgressTimers[realRow] != null)
                                && ((currentProgress == 100) || (currentProgress == 0))) {
                        if (DEBUG) {
                            if (log.isDebugEnabled()) {
                                log.debug(value + ": indeterminateProgressTimer[" + realRow
                                            + "] stopped from Renderer"); // NOI18N
                            }
                        }
                        indeterminateProgressTimers[realRow].setRepeats(false);
                        indeterminateProgressTimers[realRow].stop();
                        indeterminateProgressTimers[realRow] = null;

                        if (DEBUG) {
                            if (log.isDebugEnabled()) {
                                log.debug(value + " indeterminateProgressTimer [" + realRow + "] stopped"); // NOI18N
                            }
                        }

                        // redraw all columns (including status icon)
                        final TableModelEvent evt = new TableModelEvent(table.getModel(),
                                realRow,
                                realRow,
                                TableModelEvent.UPDATE);
                        ((TreeTableModelAdapter)(table.getModel())).fireTableChanged(evt);
                    }
                }

                // TableModelEvent evt = new TableModelEvent(table.getModel(), realRow, realRow,
                // TableModelEvent.UPDATE); ((TreeTableModelAdapter) (table.getModel())).fireTableChanged(evt);

                // In this scenario of displaying a JSlider in a JProgressBar as renderer for a table cell, the JSlider
                // component resizes sloppy. But only the JSlider in the firt row shows this effect. If you don't set
                // the size of the JSlider you easily can reconstruct this effect by forcing the layer widget to resize
                // with the help of the docking framework.
                // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7092176 for more details.
                this.slider.setSize(ZERO_DIMENSION);

                this.slider.updateUI();

                return this.progressBar;
            } else {
                setIcon(null);
                setText(""); // NOI18N
            }
//            return new JLabel("turned off");
        } else if (realColumn == 5) {
            if (value instanceof WMSServiceLayer) {
                moreButton.setEnabled(false);
            } else if (value instanceof WebFeatureService) {
                moreButton.setEnabled(true);
            }
            return moreButton;
        } else {
            setIcon(null);
            setText("");     // NOI18N
        }

        return this;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class StyleLabel extends JLabel {

        //~ Instance fields ----------------------------------------------------

        protected de.cismet.cismap.commons.featureservice.style.Style style;

        //~ Methods ------------------------------------------------------------

        // paint style-rectangle inside the button that creates the StyleDialog
        @Override
        protected void paintComponent(final Graphics g) {
            try {
                if (style != null) {
                    final Graphics2D g2d = (Graphics2D)g;
                    g.setColor(ActiveLayerTableCellRenderer.this.getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                    if (style.isDrawFill() && (style.getFillColor() != null)) {
                        g2d.setColor((Color)style.getFillColor());
                        g2d.fillRect(10, 4, getWidth() - 20, getHeight() - 8);
                    }
                    if (style.isDrawLine() && (style.getLineColor() != null)) {
                        g2d.setColor((Color)style.getLineColor());
                        final float width = new Float(Math.min(3.0f, style.getLineWidth())).intValue();
                        g2d.setStroke(new BasicStroke(width));
                        g2d.drawRect(10, 4, getWidth() - 20, getHeight() - 8);
                    }
                }
            } catch (Exception ex) {
            }
        }
    }
}
