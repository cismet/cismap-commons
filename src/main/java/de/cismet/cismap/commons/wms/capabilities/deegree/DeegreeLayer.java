package de.cismet.cismap.commons.wms.capabilities.deegree;

import de.cismet.cismap.commons.wms.capabilities.Envelope;
import de.cismet.cismap.commons.wms.capabilities.Extent;
import de.cismet.cismap.commons.wms.capabilities.Layer;
import de.cismet.cismap.commons.wms.capabilities.LayerBoundingBox;
import de.cismet.cismap.commons.wms.capabilities.Style;


/**
 *
 * @author therter
 */
public class DeegreeLayer implements Layer {
    private org.deegree.ogcwebservices.wms.capabilities.Layer layer;

    public DeegreeLayer(org.deegree.ogcwebservices.wms.capabilities.Layer layer) {
        this.layer = layer;
    }

    @Override
    public String getTitle() {
        return layer.getTitle();
    }

    @Override
    public String getName() {
        return layer.getName();
    }

    @Override
    public String getAbstract() {
        return layer.getAbstract();
    }

    @Override
    public boolean isQueryable() {
        return layer.isQueryable();
    }

    @Override
    public boolean isSrsSupported(String srs) {
        return layer.isSrsSupported(srs);
    }

    @Override
    public String[] getSrs() {
        return layer.getSrs();
    }

    @Override
    public double getScaleDenominationMax() {
        if (layer.getScaleHint() != null) {
            return layer.getScaleHint().getMax();
        } else {
            return 0;
        }
    }

    @Override
    public double getScaleDenominationMin() {
        if (layer.getScaleHint() != null) {
            return layer.getScaleHint().getMin();
        } else {
            return 0;
        }
    }

    @Override
    public Style getStyleResource(String name) {
        return new DeegreeStyle(layer.getStyleResource(name));
    }

    @Override
    public Style[] getStyles() {
        if (layer.getStyles() == null) {
            return null;
        }
        org.deegree.ogcwebservices.wms.capabilities.Style[] deegreeStyles = layer.getStyles();
        Style[] result = new Style[deegreeStyles.length];

        for ( int i = 0; i < deegreeStyles.length; ++i ) {
            result[i] = new DeegreeStyle(deegreeStyles[i]);
        }

        return result;
    }

    @Override
    public Layer[] getChildren() {
        if (layer.getLayer() == null) {
            return null;
        }
        org.deegree.ogcwebservices.wms.capabilities.Layer[] deegreeLayer = layer.getLayer();
        Layer[] result = new Layer[deegreeLayer.length];

        for ( int i = 0; i < deegreeLayer.length; ++i ) {
            result[i] = new DeegreeLayer( deegreeLayer[i] );
        }

        return result;
    }

    @Override
    public LayerBoundingBox[] getBoundingBoxes() {
        if (this.layer.getBoundingBoxes() == null) {
            return null;
        }
        
        org.deegree.ogcwebservices.wms.capabilities.LayerBoundingBox[] origBoxes = layer.getBoundingBoxes();
        LayerBoundingBox[] boxes = new LayerBoundingBox[origBoxes.length];

        for (int i = 0; i < origBoxes.length; ++i) {
            boxes[i] = new DeegreeLayerBoundingBox(origBoxes[i]);
        }

        return boxes;
    }

    @Override
    public Envelope getLatLonBoundingBoxes() {
        if (this.layer.getLatLonBoundingBox() == null) {
            return null;
        }
        return new DeegreeEnvelope( this.layer.getLatLonBoundingBox() );
    }
}
