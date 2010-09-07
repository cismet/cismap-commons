/*
 * StatusBar.java
 *
 * Created on 23. März 2006, 14:23
 */
package de.cismet.cismap.commons.gui.statusbar;

import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.XStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.printing.Scale;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.StatusListener;
import de.cismet.cismap.commons.interaction.events.StatusEvent;
import de.cismet.tools.StaticDecimalTools;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;

/**
 *
 * @author  thorsten.hell@cismet.de
 */
public class StatusBar extends javax.swing.JPanel implements StatusListener, FeatureCollectionListener {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private GeoTransformer transformer = null;
    private DecimalFormat df = new DecimalFormat("0.000000");//NOI18N
    String mode;
    ImageIcon defaultIcon = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/map.png"));//NOI18N
    MappingComponent mappingComponent;

    /** Creates new form StatusBar */
    public StatusBar(MappingComponent mappingComponent) {
        initComponents();
        this.mappingComponent = mappingComponent;
        lblStatusImage.setText("");//NOI18N
        lblCoordinates.setText("");//NOI18N
        lblStatusImage.setIcon(defaultIcon);
        lblCrs.setText(CismapBroker.getInstance().getSrs().getCode());
        lblWgs84Coordinates.setToolTipText("WGS84");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(dfs);
        
        try {
            // initialises the geo transformer that transforms the coordinates from the current
            // coordinate system to EPSG:4326
            this.transformer = new GeoTransformer("EPSG:4326");
        } catch (Exception e) {
            log.error("cannot create a transformer for EPSG:4326.", e);
        }
    }


    public void addCrsPopups() {
        for (Crs c : mappingComponent.getCrsList()) {
            addCrsPopup(c);
        }
    }


    public void addScalePopups() {
        for (Scale s : mappingComponent.getScales()) {
            if (s.getDenominator() > 0) {
                addScalePopupMenu(s.getText(), s.getDenominator());
            }
        }
    }

    public void statusValueChanged(StatusEvent e) {
        if (e.getName().equals(StatusEvent.COORDINATE_STRING)) {
            lblCoordinates.setText( e.getValue().toString() );
            lblWgs84Coordinates.setText( transformToWGS84Coords( e.getValue().toString() ) );
        } else if (e.getName().equals(StatusEvent.MEASUREMENT_INFOS)) {
            lblStatus.setText(e.getValue().toString());
        } else if (e.getName().equals(StatusEvent.MAPPING_MODE)) {
            lblStatus.setText("");//NOI18N
            mode = ((String) e.getValue());
        } else if (e.getName().equals(StatusEvent.OBJECT_INFOS)) {
            if (e.getValue() != null && e.getValue() instanceof PFeature && ((PFeature) e.getValue()).getFeature() != null && ((PFeature) e.getValue()).getFeature() instanceof XStyledFeature) {
                lblStatus.setText(((XStyledFeature) ((PFeature) e.getValue()).getFeature()).getName());
                ImageIcon ico = ((XStyledFeature) ((PFeature) e.getValue()).getFeature()).getIconImage();
                if (ico != null && ico.getIconWidth() > 0 && ico.getIconHeight() > 0) {
                    lblStatusImage.setIcon(ico);
                } else {
                    lblStatusImage.setIcon(defaultIcon);
                }
            } else if (e.getValue() != null && e.getValue() instanceof PFeature && ((PFeature) e.getValue()).getFeature() != null && ((PFeature) e.getValue()).getFeature() instanceof DefaultFeatureServiceFeature) {
                if (((DefaultFeatureServiceFeature) ((PFeature) e.getValue()).getFeature()).getSecondaryAnnotation() != null) {
                    lblStatus.setText(((DefaultFeatureServiceFeature) ((PFeature) e.getValue()).getFeature()).getSecondaryAnnotation());
                } else {
                    lblStatus.setText("");//NOI18N
                }
            } else {
                lblStatus.setText("");//NOI18N
                lblStatusImage.setIcon(defaultIcon);
            }
        } else if (e.getName().equals(StatusEvent.SCALE)) {
            int sd = (int) (mappingComponent.getScaleDenominator() + 0.5);
            lblScale.setText("1:" + sd);//NOI18N
        } else if (e.getName().equals(StatusEvent.CRS)) {
            lblCrs.setText( ((Crs)e.getValue()).getShortname() );
            lblCoordinates.setToolTipText(e.getValue().toString());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        setLayout(new java.awt.GridBagLayout());

        lblCoordinates.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
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
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(jSeparator2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(lblStatus, gridBagConstraints);

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(jSeparator3, gridBagConstraints);

        lblScale.setComponentPopupMenu(pomScale);
        lblScale.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                lblScaleMousePressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
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
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        add(lblMeasurement, gridBagConstraints);

        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(jSeparator5, gridBagConstraints);

        lblWgs84Coordinates.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(lblWgs84Coordinates, gridBagConstraints);

        jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(jSeparator6, gridBagConstraints);

        lblCrs.setComponentPopupMenu(pomCrs);
        lblCrs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                lblCrsMousePressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        add(lblCrs, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void lblScaleMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblScaleMousePressed
        if (evt.isPopupTrigger()) {
            pomScale.setVisible(true);
        }
    }//GEN-LAST:event_lblScaleMousePressed

    private void lblCrsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCrsMousePressed
        if (evt.isPopupTrigger()) {
            pomCrs.setVisible(true);
        }
    }//GEN-LAST:event_lblCrsMousePressed


    private void addCrsPopup(final Crs crs) {
        JMenuItem jmi = new JMenuItem(crs.getShortname());
        jmi.setToolTipText(crs.getName());
        jmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CismapBroker.getInstance().setSrs(crs);
            }
        });
        pomCrs.add(jmi);
    }

    
    private void addScalePopupMenu(String text,final double scaleDenominator) {
        JMenuItem jmi=new JMenuItem(text);
        jmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mappingComponent.gotoBoundingBoxWithHistory(mappingComponent.getBoundingBoxFromScale(scaleDenominator));
            }
        });
        pomScale.add(jmi);
    }
    
    private  void refreshMeasurementsInStatus() {
        try{
        Collection<Feature> cf=mappingComponent.getFeatureCollection().getSelectedFeatures();
        refreshMeasurementsInStatus(cf);
        }catch(NullPointerException ex){
            log.error("Error while refreshing measurements",ex);//NOI18N
        }
    }
    
    private  void refreshMeasurementsInStatus(Collection<Feature> cf) {
        double umfang=0.0;
        double area=0.0;
        for (Feature f: cf) {
            if (f!=null && f.getGeometry() !=null) {
                area+=f.getGeometry().getArea();
                umfang+=f.getGeometry().getLength();
            }
        }
        if ((area==0.0 && umfang==0.0)||cf.size()==0){
            lblMeasurement.setText("");//NOI18N
        } else {
            lblMeasurement.setText(
                    org.openide.util.NbBundle.getMessage(StatusBar.class,"StatusBar.lblMeasurement.text", new Object[]{ StaticDecimalTools.round(area), StaticDecimalTools.round(umfang) }));//NOI18N
        }
    }

    public void featuresRemoved(FeatureCollectionEvent fce) {}
    
    public void featuresChanged(FeatureCollectionEvent fce) {
        log.debug("FeatureChanged");//NOI18N
        if (mappingComponent.getInteractionMode().equals(MappingComponent.NEW_POLYGON)) {
            refreshMeasurementsInStatus(fce.getEventFeatures());
        }
        else {
            refreshMeasurementsInStatus();
        }
    }
    
    public void featuresAdded(FeatureCollectionEvent fce) {}
    
    public void featureSelectionChanged(FeatureCollectionEvent fce) {
         refreshMeasurementsInStatus();
    }

    public void featureReconsiderationRequested(FeatureCollectionEvent fce) {}

    public void allFeaturesRemoved(FeatureCollectionEvent fce) {}

    public javax.swing.JPopupMenu getPomScale() {
        return pomScale;
    }

    public void featureCollectionChanged() {}

    private String transformToWGS84Coords(String coords) {
        String result = "";

        try {
            String tmp = coords.substring(1, coords.length() -1);
            int commaPosition = tmp.indexOf(",");

            if (commaPosition != -1 && transformer != null) {
                double xCoord = Double.parseDouble( tmp.substring(0, commaPosition) );
                double yCoord = Double.parseDouble( tmp.substring(commaPosition + 1) );

                CoordinateSystem coordSystem = CRSFactory.create(CismapBroker.getInstance().getSrs().getCode());
                Point currentPoint = GeometryFactory.createPoint(xCoord, yCoord, coordSystem);
                currentPoint = (Point)transformer.transform(currentPoint);
                result = "(" + df.format(currentPoint.getX()) + "," + df.format(currentPoint.getY()) + ")";//NOI18N
            } else {
                log.error("Cannot transform the current coordinates: " + coords);
            }
        } catch (Exception e) {
            log.error("Cannot transform the current coordinates: " + coords, e);
        }

        return result;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
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
    private javax.swing.JPopupMenu pomCrs;
    private javax.swing.JPopupMenu pomScale;
    // End of variables declaration//GEN-END:variables
    
    
    
}
