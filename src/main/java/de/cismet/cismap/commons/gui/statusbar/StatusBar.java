/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * StatusBar.java
 *
 * Created on 23. März 2006, 14:23
 */
package de.cismet.cismap.commons.gui.statusbar;

import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.Collection;
import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.printing.Scale;
import de.cismet.cismap.commons.interaction.ActiveLayerListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.StatusListener;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.interaction.events.StatusEvent;

import de.cismet.tools.StaticDecimalTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class StatusBar extends javax.swing.JPanel implements StatusListener,
    FeatureCollectionListener,
    ActiveLayerListener {

    //~ Instance fields --------------------------------------------------------

    String mode;
    ImageIcon defaultIcon = new javax.swing.ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/res/map.png")); // NOI18N
    MappingComponent mappingComponent;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private GeoTransformer transformer = null;
    private DecimalFormat df = new DecimalFormat("0.00");      // NOI18N
    private int servicesCounter = 0;
    private int servicesErroneousCounter = 0;
    private Collection<ServiceLayer> services = new HashSet<ServiceLayer>();
    private Collection<ServiceLayer> erroneousServices = new HashSet<ServiceLayer>();
    private JPanel servicesBusyPanel = new ServicesBusyPanel();
    private JPanel servicesRetrievedPanel = new ServicesRetrievedPanel();
    private JPanel servicesErrorPanel = new ServicesErrorPanel();
    private JPanel mapExtentFixedPanel = new MapExtentFixedPanel();
    private JPanel mapExtentUnfixedPanel = new MapExtentUnfixedPanel();
    private JPanel mapScaleFixedPanel = new MapScaleFixedPanel();
    private JPanel mapScaleUnfixedPanel = new MapScaleUnfixedPanel();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler gluFiller;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JLabel lblCoordinates;
    private javax.swing.JLabel lblCrs;
    private javax.swing.JLabel lblMeasurement;
    private javax.swing.JLabel lblScale;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblStatusImage;
    private javax.swing.JLabel lblWgs84Coordinates;
    private javax.swing.JPanel pnlFixMapExtent;
    private javax.swing.JPanel pnlFixMapScale;
    private javax.swing.JPanel pnlServicesStatus;
    private javax.swing.JPopupMenu pomCrs;
    private javax.swing.JPopupMenu pomScale;
    private de.cismet.cismap.commons.gui.statusbar.ServicesRetrievedPanel servicesRetrievedPanel1;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form StatusBar.
     *
     * @param  mappingComponent  DOCUMENT ME!
     */
    public StatusBar(final MappingComponent mappingComponent) {
        initComponents();
        this.mappingComponent = mappingComponent;
        lblStatusImage.setText(""); // NOI18N
        lblCoordinates.setText(""); // NOI18N
        lblStatusImage.setIcon(defaultIcon);
        lblCrs.setText(CismapBroker.getInstance().getSrs().getCode());
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(dfs);

        pnlFixMapExtent.add(mapExtentUnfixedPanel, BorderLayout.CENTER);
        pnlFixMapScale.add(mapScaleUnfixedPanel, BorderLayout.CENTER);

        try {
            // initialises the geo transformer that transforms the coordinates from the current
            // coordinate system to EPSG:4326
            this.transformer = new GeoTransformer("EPSG:4326");
        } catch (Exception e) {
            log.error("cannot create a transformer for EPSG:4326.", e);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void addCrsPopups() {
        for (final Crs c : mappingComponent.getCrsList()) {
            addCrsPopup(c);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void addScalePopups() {
        for (final Scale s : mappingComponent.getScales()) {
            if (s.getDenominator() > 0) {
                addScalePopupMenu(s.getText(), s.getDenominator());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    @Override
    public void statusValueChanged(final StatusEvent e) {
        final Runnable modifyControls = new Runnable() {

                @Override
                public void run() {
                    if (e.getName().equals(StatusEvent.COORDINATE_STRING)) {
                        lblCoordinates.setText(e.getValue().toString());
                        lblWgs84Coordinates.setText(transformToWGS84Coords(e.getValue().toString()));
                    } else if (e.getName().equals(StatusEvent.MEASUREMENT_INFOS)) {
                        lblStatus.setText(e.getValue().toString());
                    } else if (e.getName().equals(StatusEvent.MAPPING_MODE)) {
                        lblStatus.setText("");         // NOI18N
                    } else if (e.getName().equals(StatusEvent.OBJECT_INFOS)) {
                        if ((e.getValue() != null) && (e.getValue() instanceof PFeature)
                                    && (((PFeature)e.getValue()).getFeature() != null)
                                    && (((PFeature)e.getValue()).getFeature() instanceof XStyledFeature)) {
                            lblStatus.setText(((XStyledFeature)((PFeature)e.getValue()).getFeature()).getName());
                            final ImageIcon ico = ((XStyledFeature)((PFeature)e.getValue()).getFeature())
                                        .getIconImage();
                            if ((ico != null) && (ico.getIconWidth() > 0) && (ico.getIconHeight() > 0)) {
                                lblStatusImage.setIcon(ico);
                            } else {
                                lblStatusImage.setIcon(defaultIcon);
                            }
                        } else if ((e.getValue() != null) && (e.getValue() instanceof PFeature)
                                    && (((PFeature)e.getValue()).getFeature() != null)
                                    && (((PFeature)e.getValue()).getFeature() instanceof DefaultFeatureServiceFeature)) {
                            if (
                                ((DefaultFeatureServiceFeature)((PFeature)e.getValue()).getFeature())
                                        .getSecondaryAnnotation()
                                        != null) {
                                lblStatus.setText(((DefaultFeatureServiceFeature)((PFeature)e.getValue()).getFeature())
                                            .getSecondaryAnnotation());
                            } else {
                                lblStatus.setText(""); // NOI18N
                            }
                        } else {
                            lblStatus.setText("");     // NOI18N
                            lblStatusImage.setIcon(defaultIcon);
                        }
                    } else if (e.getName().equals(StatusEvent.SCALE)) {
                        final int sd = (int)(mappingComponent.getScaleDenominator() + 0.5);
                        lblScale.setText("1:" + sd);   // NOI18N
                    } else if (e.getName().equals(StatusEvent.CRS)) {
                        lblCrs.setText(((Crs)e.getValue()).getShortname());
                        lblCoordinates.setToolTipText(((Crs)e.getValue()).getShortname());
                    } else if (e.getName().equals(StatusEvent.RETRIEVAL_STARTED)) {
                        if ((pnlServicesStatus.getComponentCount() > 0)
                                    && !pnlServicesStatus.getComponent(0).equals(servicesBusyPanel)) {
                            pnlServicesStatus.removeAll();
                            pnlServicesStatus.add(servicesBusyPanel, BorderLayout.CENTER);
                            pnlServicesStatus.revalidate();
                            pnlServicesStatus.repaint();
                        }
                    } else if (e.getName().equals(StatusEvent.RETRIEVAL_COMPLETED)
                                || e.getName().equals(StatusEvent.RETRIEVAL_ABORTED)
                                || e.getName().equals(StatusEvent.RETRIEVAL_REMOVED)) {
                        if (servicesCounter == 0) {
                            pnlServicesStatus.removeAll();
                            if (servicesErroneousCounter == 0) {
                                pnlServicesStatus.add(servicesRetrievedPanel, BorderLayout.CENTER);
                            } else {
                                pnlServicesStatus.add(servicesErrorPanel, BorderLayout.CENTER);
                            }
                            pnlServicesStatus.revalidate();
                            pnlServicesStatus.repaint();
                        }
                    } else if (e.getName().equals(StatusEvent.RETRIEVAL_ERROR)) {
                        if ((pnlServicesStatus.getComponentCount() > 0)
                                    && !pnlServicesStatus.getComponent(0).equals(servicesErrorPanel)) {
                            pnlServicesStatus.removeAll();
                            pnlServicesStatus.add(servicesErrorPanel, BorderLayout.CENTER);
                            pnlServicesStatus.revalidate();
                            pnlServicesStatus.repaint();
                        }
                    } else if (e.getName().equals(StatusEvent.MAP_EXTEND_FIXED)) {
                        if (e.getValue() instanceof Boolean) {
                            pnlFixMapExtent.removeAll();
                            if ((Boolean)e.getValue()) {
                                pnlFixMapExtent.add(mapExtentFixedPanel, BorderLayout.CENTER);
                            } else {
                                pnlFixMapExtent.add(mapExtentUnfixedPanel, BorderLayout.CENTER);
                            }
                            pnlFixMapExtent.revalidate();
                            pnlFixMapExtent.repaint();
                        }
                    } else if (e.getName().equals(StatusEvent.MAP_SCALE_FIXED)) {
                        if (e.getValue() instanceof Boolean) {
                            pnlFixMapScale.removeAll();
                            if ((Boolean)e.getValue()) {
                                pnlFixMapScale.add(mapScaleFixedPanel, BorderLayout.CENTER);
                            } else {
                                pnlFixMapScale.add(mapScaleUnfixedPanel, BorderLayout.CENTER);
                            }
                            pnlFixMapScale.revalidate();
                            pnlFixMapScale.repaint();
                        }
                    }
                }
            };

        if (e.getName().equals(StatusEvent.MAPPING_MODE)) {
            mode = ((String)e.getValue());
        } else if (e.getName().equals(StatusEvent.RETRIEVAL_STARTED)) {
            if (e.getValue() instanceof ServiceLayer) {
                final ServiceLayer service = (ServiceLayer)e.getValue();
                log.fatal("STARTED: " + service);
                if (!services.contains(service)) {
                    services.add(service);
                    servicesCounter++;
                }
                log.fatal("SERVICES: " + servicesCounter + ", ERRONEOUS SERVICES: " + servicesErroneousCounter);
            }
        } else if (e.getName().equals(StatusEvent.RETRIEVAL_COMPLETED)) {
            if (e.getValue() instanceof ServiceLayer) {
                final ServiceLayer service = (ServiceLayer)e.getValue();
                log.fatal("COMPLETED: " + service);
                if (services.contains(service)) {
                    services.remove(service);
                    servicesCounter--;
                }
                log.fatal("SERVICES: " + servicesCounter + ", ERRONEOUS SERVICES: " + servicesErroneousCounter);
            }
        } else if (e.getName().equals(StatusEvent.RETRIEVAL_ABORTED)) {
            if (e.getValue() instanceof ServiceLayer) {
                final ServiceLayer service = (ServiceLayer)e.getValue();
                log.fatal("ABORTED: " + service);
                if (services.contains(service)) {
                    services.remove(service);
                    servicesCounter--;
                }
                log.fatal("SERVICES: " + servicesCounter + ", ERRONEOUS SERVICES: " + servicesErroneousCounter);
            }
        } else if (e.getName().equals(StatusEvent.RETRIEVAL_ERROR)) {
            if (e.getValue() instanceof ServiceLayer) {
                final ServiceLayer service = (ServiceLayer)e.getValue();
                log.fatal("ERROR: " + service);
                if (services.contains(service)) {
                    services.remove(service);
                    servicesCounter--;
                    erroneousServices.add(service);
                    servicesErroneousCounter++;
                }
                log.fatal("SERVICES: " + servicesCounter + ", ERRONEOUS SERVICES: " + servicesErroneousCounter);
            }
        } else if (e.getName().equals(StatusEvent.RETRIEVAL_REMOVED)) {
            if (e.getValue() instanceof ServiceLayer) {
                final ServiceLayer service = (ServiceLayer)e.getValue();
                log.fatal("REMOVED: " + service);
                if (services.contains(service)) {
                    services.remove(service);
                    servicesCounter--;
                }
                if (erroneousServices.contains(service)) {
                    erroneousServices.remove(service);
                    servicesErroneousCounter--;
                }
                log.fatal("SERVICES: " + servicesCounter + ", ERRONEOUS SERVICES: " + servicesErroneousCounter);
            }
        }

        if (EventQueue.isDispatchThread()) {
            modifyControls.run();
        } else {
            EventQueue.invokeLater(modifyControls);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSeparator1 = new javax.swing.JSeparator();
        pomScale = new javax.swing.JPopupMenu();
        pomCrs = new javax.swing.JPopupMenu();
        lblCoordinates = new javax.swing.JLabel();
        lblStatusImage = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        lblStatus = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        lblScale = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        lblMeasurement = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        lblWgs84Coordinates = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        lblCrs = new javax.swing.JLabel();
        pnlServicesStatus = new javax.swing.JPanel();
        servicesRetrievedPanel1 = new de.cismet.cismap.commons.gui.statusbar.ServicesRetrievedPanel();
        pnlFixMapExtent = new javax.swing.JPanel();
        pnlFixMapScale = new javax.swing.JPanel();
        gluFiller = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
                new java.awt.Dimension(0, 0),
                new java.awt.Dimension(0, 0));

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        setLayout(new java.awt.GridBagLayout());

        lblCoordinates.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(lblCoordinates, gridBagConstraints);

        lblStatusImage.setMaximumSize(new java.awt.Dimension(17, 17));
        lblStatusImage.setMinimumSize(new java.awt.Dimension(17, 17));
        lblStatusImage.setPreferredSize(new java.awt.Dimension(17, 17));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(lblStatusImage, gridBagConstraints);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(jSeparator2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(lblStatus, gridBagConstraints);

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(jSeparator3, gridBagConstraints);

        lblScale.setComponentPopupMenu(pomScale);
        lblScale.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mousePressed(final java.awt.event.MouseEvent evt) {
                    lblScaleMousePressed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        add(lblScale, gridBagConstraints);

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(jSeparator4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        add(lblMeasurement, gridBagConstraints);

        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(jSeparator5, gridBagConstraints);

        lblWgs84Coordinates.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblWgs84Coordinates.setToolTipText(org.openide.util.NbBundle.getMessage(
                StatusBar.class,
                "StatusBar.lblWgs84Coordinates.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(lblWgs84Coordinates, gridBagConstraints);

        jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(jSeparator6, gridBagConstraints);

        lblCrs.setComponentPopupMenu(pomCrs);
        lblCrs.addMouseListener(new java.awt.event.MouseAdapter() {

                @Override
                public void mousePressed(final java.awt.event.MouseEvent evt) {
                    lblCrsMousePressed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        add(lblCrs, gridBagConstraints);

        pnlServicesStatus.setLayout(new java.awt.BorderLayout());
        pnlServicesStatus.add(servicesRetrievedPanel1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        add(pnlServicesStatus, gridBagConstraints);

        pnlFixMapExtent.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(pnlFixMapExtent, gridBagConstraints);

        pnlFixMapScale.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        add(pnlFixMapScale, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(gluFiller, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void lblScaleMousePressed(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_lblScaleMousePressed
        if (evt.isPopupTrigger()) {
            pomScale.setVisible(true);
        }
    }                                                                        //GEN-LAST:event_lblScaleMousePressed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void lblCrsMousePressed(final java.awt.event.MouseEvent evt) { //GEN-FIRST:event_lblCrsMousePressed
        if (evt.isPopupTrigger()) {
            pomCrs.setVisible(true);
        }
    }                                                                      //GEN-LAST:event_lblCrsMousePressed

    /**
     * DOCUMENT ME!
     *
     * @param  crs  DOCUMENT ME!
     */
    private void addCrsPopup(final Crs crs) {
        final JMenuItem jmi = new JMenuItem(crs.getShortname());
        jmi.setToolTipText(crs.getName());
        jmi.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    CismapBroker.getInstance().setSrs(crs);
                }
            });
        pomCrs.add(jmi);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  text              DOCUMENT ME!
     * @param  scaleDenominator  DOCUMENT ME!
     */
    private void addScalePopupMenu(final String text, final double scaleDenominator) {
        final JMenuItem jmi = new JMenuItem(text);
        jmi.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    mappingComponent.gotoBoundingBoxWithHistory(
                        mappingComponent.getBoundingBoxFromScale(scaleDenominator));
                }
            });
        pomScale.add(jmi);
    }

    /**
     * DOCUMENT ME!
     */
    private void refreshMeasurementsInStatus() {
        try {
            final Collection<Feature> cf = mappingComponent.getFeatureCollection().getSelectedFeatures();
            refreshMeasurementsInStatus(cf);
        } catch (NullPointerException ex) {
            log.error("Error while refreshing measurements", ex); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cf  DOCUMENT ME!
     */
    private void refreshMeasurementsInStatus(final Collection<Feature> cf) {
        double umfang = 0.0;
        double area = 0.0;
        for (final Feature f : cf) {
            if ((f != null) && (f.getGeometry() != null)) {
                area += f.getGeometry().getArea();
                umfang += f.getGeometry().getLength();
            }
        }
        if (((area == 0.0) && (umfang == 0.0)) || (cf.size() == 0)) {
            lblMeasurement.setText("");                                                                  // NOI18N
        } else {
            lblMeasurement.setText(
                org.openide.util.NbBundle.getMessage(
                    StatusBar.class,
                    "StatusBar.lblMeasurement.text",
                    new Object[] { StaticDecimalTools.round(area), StaticDecimalTools.round(umfang) })); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    @Override
    public void featuresRemoved(final FeatureCollectionEvent fce) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    @Override
    public void featuresChanged(final FeatureCollectionEvent fce) {
        if (log.isDebugEnabled()) {
            log.debug("FeatureChanged"); // NOI18N
        }
        if (mappingComponent.getInteractionMode().equals(MappingComponent.NEW_POLYGON)) {
            refreshMeasurementsInStatus(fce.getEventFeatures());
        } else {
            refreshMeasurementsInStatus();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    @Override
    public void featuresAdded(final FeatureCollectionEvent fce) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    @Override
    public void featureSelectionChanged(final FeatureCollectionEvent fce) {
        refreshMeasurementsInStatus();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    @Override
    public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fce  DOCUMENT ME!
     */
    @Override
    public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public javax.swing.JPopupMenu getPomScale() {
        return pomScale;
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void featureCollectionChanged() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param   coords  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String transformToWGS84Coords(final String coords) {
        String result = "";

        try {
            final String tmp = coords.substring(1, coords.length() - 1);
            final int commaPosition = tmp.indexOf(",");

            if ((commaPosition != -1) && (transformer != null)) {
                final double xCoord = Double.parseDouble(tmp.substring(0, commaPosition));
                final double yCoord = Double.parseDouble(tmp.substring(commaPosition + 1));

                final CoordinateSystem coordSystem = CRSFactory.create(CismapBroker.getInstance().getSrs().getCode());
                Point currentPoint = GeometryFactory.createPoint(xCoord, yCoord, coordSystem);
                currentPoint = (Point)transformer.transform(currentPoint);
                result = "(" + df.format(currentPoint.getX()) + "," + df.format(currentPoint.getY()) + ")"; // NOI18N
            } else {
                log.error("Cannot transform the current coordinates: " + coords);
            }
        } catch (Exception e) {
            log.error("Cannot transform the current coordinates: " + coords, e);
        }

        return result;
    }

    @Override
    public void layerAdded(final ActiveLayerEvent e) {
        // TODO: Use this for counting starting retrievals?
    }

    @Override
    public void layerRemoved(final ActiveLayerEvent e) {
        if (e.getLayer() instanceof ServiceLayer) {
            final ServiceLayer service = (ServiceLayer)e.getLayer();
            if (services.contains(service)) {
//                services.remove(service);
//                servicesCounter--;

//                if(service instanceof RetrievalServiceLayer && ((RetrievalServiceLayer)service).hasErrors()) {
//                    servicesErroneousCounter--;
//                }

                statusValueChanged(new StatusEvent(StatusEvent.RETRIEVAL_ABORTED, service));
            }
        }
    }

    @Override
    public void layerPositionChanged(final ActiveLayerEvent e) {
        // NOP
    }

    @Override
    public void layerVisibilityChanged(final ActiveLayerEvent e) {
        // NOP
    }

    @Override
    public void layerInformationStatusChanged(final ActiveLayerEvent e) {
        // NOP
    }

    @Override
    public void layerSelectionChanged(final ActiveLayerEvent e) {
        // NOP
    }
}
