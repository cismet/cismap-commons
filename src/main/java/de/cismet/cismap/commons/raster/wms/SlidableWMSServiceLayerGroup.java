/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2010 thorsten
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cismap.commons.raster.wms;

import edu.umd.cs.piccolo.PNode;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.geom.Point2D;

import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.LayerInfoProvider;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.gui.FloatingControlProvider;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.XPImage;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.rasterservice.RasterMapService;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.cismap.commons.wms.capabilities.Layer;
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class SlidableWMSServiceLayerGroup extends AbstractRetrievalService implements RetrievalServiceLayer,
    FloatingControlProvider,
    RasterMapService,
    ChangeListener,
    MapService,
    LayerInfoProvider {

    //~ Instance fields --------------------------------------------------------

    ArrayList<WMSServiceLayer> layers = new ArrayList<WMSServiceLayer>();
    ArrayList<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();
    ArrayList<RetrievalListener> retrievalListeners = new ArrayList<RetrievalListener>();
    boolean layerQuerySelected = false;

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private PNode pnode = new XPImage();
    private JSlider slider = new JSlider();
    private JDialog dialog = new JDialog();
    private JInternalFrame internalFrame = new JInternalFrame();
    private int layerPosition;
    private boolean enabled;
    private String name;
    private int progress;
    private String preferredRasterFormat;
    private String preferredTransparentPref;
    private String preferredBGColor;
    private String preferredExceptionsFormat;
    private int selectedLayer = 0;
    private String capabilitiesUrl = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SlidableWMSServiceLayerGroup object.
     *
     * @param  treePaths  DOCUMENT ME!
     */
    public SlidableWMSServiceLayerGroup(final Vector treePaths) {
        setDefaults();
        final TreePath tp = ((TreePath)treePaths.get(0));
        final Layer[] children =
            ((de.cismet.cismap.commons.wms.capabilities.deegree.DeegreeLayer)tp.getLastPathComponent()).getChildren();
        final Collection<Layer> c = new ArrayList<Layer>(Arrays.asList(children));
        setName(((de.cismet.cismap.commons.wms.capabilities.deegree.DeegreeLayer)tp.getLastPathComponent()).getName());

        for (final Layer l : children) {
            final WMSServiceLayer wsl = new WMSServiceLayer(l);
            if (capabilitiesUrl == null) {
                capabilitiesUrl = wsl.getCapabilitiesUrl();
            }

            wsl.setPNode(new XPImage());
            layers.add(wsl);
//            final Point2D localOrigin = CismapBroker.getInstance().getMappingComponent().getCamera().getViewBounds().getOrigin();
//            final double localScale = CismapBroker.getInstance().getMappingComponent().getCamera().getViewScale();
            pnode.addChild(wsl.getPNode());
//            wsl.getPNode().setScale(1 / localScale);
//            wsl.getPNode().setOffset(localOrigin);
            wsl.addRetrievalListener(new RetrievalListener() {

                    @Override
                    public void retrievalStarted(final RetrievalEvent e) {
                        log.fatal("go " + e);
                    }

                    @Override
                    public void retrievalProgress(final RetrievalEvent e) {
                    }

                    @Override
                    public void retrievalComplete(final RetrievalEvent e) {
                        log.fatal("fertich " + e);

                        final Image i = (Image)e.getRetrievedObject();
                        ((XPImage)wsl.getPNode()).setImage(i);
                        new Thread() {

                            @Override
                            public void run() {
                                final Point2D localOrigin = CismapBroker.getInstance()
                                            .getMappingComponent()
                                            .getCamera()
                                            .getViewBounds()
                                            .getOrigin();
                                final double localScale = CismapBroker.getInstance()
                                            .getMappingComponent()
                                            .getCamera()
                                            .getViewScale();
                                wsl.getPNode().setScale(1 / localScale);
                                wsl.getPNode().setOffset(localOrigin);
                                CismapBroker.getInstance().getMappingComponent().repaint();
                                if (wsl == layers.get(0)) {
                                    final RetrievalEvent re = new RetrievalEvent();
                                    re.setIsComplete(true);
                                    re.setRetrievalService(SlidableWMSServiceLayerGroup.this);
                                    re.setHasErrors(false);

//                                re.setRetrievedObject(((XPImage) layers.get(0).getPNode()).getImage());
                                    re.setRetrievedObject(null);
                                    fireRetrievalComplete(re);
                                }
                            }
                        }.start();
                    }

                    @Override
                    public void retrievalAborted(final RetrievalEvent e) {
                    }

                    @Override
                    public void retrievalError(final RetrievalEvent e) {
                    }
                });
            if (wsl.getBackgroundColor() == null) {
                wsl.setBackgroundColor(preferredBGColor);
            }
            if (wsl.getExceptionsFormat() == null) {
                wsl.setExceptionsFormat(preferredExceptionsFormat);
            }
            if (wsl.getImageFormat() == null) {
                wsl.setImageFormat(preferredRasterFormat);
            }
        }

        layers.get(0).setVisible(true);
        initDialog();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void setDefaults() {
        // srs="EPSG:4326";
        preferredRasterFormat = "image/png"; // NOI18N
        preferredBGColor = "0xF0F0F0";       // NOI18N
        // preferredExceptionsFormat="application/vnd.ogc.se_inimage";
        preferredExceptionsFormat = "application/vnd.ogc.se_xml"; // NOI18N

//        srs="EPSG:31466";
//        preferredRasterFormat="image/png";
//        preferredBGColor="0xF0F0F0";
//        preferredExceptionsFormat="application/vnd.ogc.se_inimage";
//        initialBoundingBox=new BoundingBox(2569442.79,5668858.33,2593744.91,5688416.22);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wmsCapabilities  DOCUMENT ME!
     */
    public void setWmsCapabilities(final WMSCapabilities wmsCapabilities) {
        for (final WMSServiceLayer layer : layers) {
            layer.setWmsCapabilities(wmsCapabilities);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  capabilitiesUrl  DOCUMENT ME!
     */
    public void setCapabilitiesUrl(final String capabilitiesUrl) {
        for (final WMSServiceLayer layer : layers) {
            layer.setCapabilitiesUrl(capabilitiesUrl);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  srs  DOCUMENT ME!
     */
    public void setSrs(final String srs) {
        for (final WMSServiceLayer layer : layers) {
            layer.setSrs(srs);
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void initDialog() {
        dialog.getContentPane().setLayout(new BorderLayout());

        slider.setMinimum(0);
        slider.setMaximum((layers.size() - 1) * 100);
        slider.setValue(0);

        slider.setMinorTickSpacing(100);
        slider.addChangeListener(this);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        final Dimension d = slider.getPreferredSize();
        d.width = (int)(((float)d.width) * 1.5);
        slider.setPreferredSize(new Dimension(d));
        final Hashtable lableTable = new Hashtable();
        int x = 0;
        for (final WMSServiceLayer wsl : layers) {
            final JLabel label = new JLabel(wsl.getName());
            // label.setBorder(new EmptyBorder(1,5,1,5));
            lableTable.put(new Integer(x * 100), label);
            x++;
        }
        slider.setLabelTable(lableTable);

//        dialog.getContentPane().add(slider, BorderLayout.CENTER);
//
//        dialog.pack();
//        dialog.setVisible(true);
        internalFrame.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE); // NOI18N
        internalFrame.getContentPane().add(slider);
        CismapBroker.getInstance()
                .getMappingComponent()
                .addInternalWidget("Slider", MappingComponent.POSITION_NORTHEAST, internalFrame);
        CismapBroker.getInstance().getMappingComponent().showInternalWidget("Slider", true, 800);
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
        final int i = (slider.getValue() / 100);
        final int rest = slider.getValue() % 100;

        // ((XPImage) getPNode()).setImage(((XPImage) layers.get(i).getPNode()).getImage());
        for (int j = 0; j < getPNode().getChildrenCount(); ++j) {
            if (i == j) {
                getPNode().getChild(i).setTransparency(1f);
            } else {
                getPNode().getChild(j).setTransparency(0f);
            }
        }
        if ((i + 1) < getPNode().getChildrenCount()) {
            getPNode().getChild(i + 1).setTransparency(((float)rest) / 100f);
        }
    }

    @Override
    public PNode getPNode() {
        return pnode;
    }

    @Override
    public void setPNode(final PNode imageObject) {
        pnode = imageObject;
    }

    @Override
    public JDialog getFloatingControlComponent() {
        return dialog;
    }

    @Override
    public void retrieve(final boolean forced) {
        for (final WMSServiceLayer layer : layers) {
            layer.retrieve(forced);
        }
    }

    @Override
    public boolean canBeDisabled() {
        return true;
    }

    @Override
    public int getLayerPosition() {
        return layerPosition;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getTranslucency() {
        return pnode.getTransparency();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void setLayerPosition(final int layerPosition) {
        this.layerPosition = layerPosition;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public void setTranslucency(final float t) {
        pnode.setTransparency(t);
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void setBoundingBox(final BoundingBox bb) {
        for (final WMSServiceLayer layer : layers) {
            layer.setBoundingBox(bb);
        }
    }

    @Override
    public void setSize(final int height, final int width) {
        log.fatal("setSize");
        for (final WMSServiceLayer layer : layers) {
            layer.setSize(height, width);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getLayerURI() {
        return getName();
    }

    @Override
    public String getServerURI() {
        return capabilitiesUrl;
    }

    @Override
    public boolean isLayerQuerySelected() {
        return layerQuerySelected;
    }

    @Override
    public void setLayerQuerySelected(final boolean selected) {
        layerQuerySelected = selected;
    }

    @Override
    public boolean isQueryable() {
        return true;
    }
}
