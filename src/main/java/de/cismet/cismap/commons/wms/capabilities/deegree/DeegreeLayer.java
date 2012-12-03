/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wms.capabilities.deegree;

import java.util.ArrayList;
import java.util.List;

import de.cismet.cismap.commons.wms.capabilities.Envelope;
import de.cismet.cismap.commons.wms.capabilities.Layer;
import de.cismet.cismap.commons.wms.capabilities.LayerBoundingBox;
import de.cismet.cismap.commons.wms.capabilities.Style;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeLayer implements Layer {

    //~ Instance fields --------------------------------------------------------

    private org.deegree.ogcwebservices.wms.capabilities.Layer layer;
    private String filterString;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreeLayer object.
     *
     * @param  layer  DOCUMENT ME!
     */
    public DeegreeLayer(final org.deegree.ogcwebservices.wms.capabilities.Layer layer) {
        this.layer = layer;
    }

    //~ Methods ----------------------------------------------------------------

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
    public String[] getKeywords() {
        return layer.getKeywordList();
    }

    @Override
    public boolean isQueryable() {
        return layer.isQueryable();
    }

    @Override
    public boolean isSrsSupported(final String srs) {
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
    public Style getStyleResource(final String name) {
        return new DeegreeStyle(layer.getStyleResource(name));
    }

    @Override
    public Style[] getStyles() {
        if (layer.getStyles() == null) {
            return null;
        }
        final org.deegree.ogcwebservices.wms.capabilities.Style[] deegreeStyles = layer.getStyles();
        final Style[] result = new Style[deegreeStyles.length];

        for (int i = 0; i < deegreeStyles.length; ++i) {
            result[i] = new DeegreeStyle(deegreeStyles[i]);
        }

        return result;
    }

    @Override
    public Layer[] getChildren() {
        if (layer.getLayer() == null) {
            return null;
        }
        final org.deegree.ogcwebservices.wms.capabilities.Layer[] deegreeLayer = layer.getLayer();
        final List<Layer> result = new ArrayList<Layer>();

        for (int i = 0; i < deegreeLayer.length; ++i) {
            if ((filterString == null) || fulfilFilterRequirements(deegreeLayer[i])) {
                final Layer l = new DeegreeLayer(deegreeLayer[i]);
                l.setFilterString(filterString);
                result.add(l);
            }
        }

        return result.toArray(new Layer[result.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   l  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean fulfilFilterRequirements(final org.deegree.ogcwebservices.wms.capabilities.Layer l) {
        if ((l.getTitle().toLowerCase().indexOf(filterString.toLowerCase()) != -1) && (l.getLayer().length == 0)) {
            return true;
        } else {
            final org.deegree.ogcwebservices.wms.capabilities.Layer[] children = l.getLayer();
            for (int i = 0; i < children.length; ++i) {
                if (fulfilFilterRequirements(children[i])) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public LayerBoundingBox[] getBoundingBoxes() {
        if (this.layer.getBoundingBoxes() == null) {
            return null;
        }

        final org.deegree.ogcwebservices.wms.capabilities.LayerBoundingBox[] origBoxes = layer.getBoundingBoxes();
        final LayerBoundingBox[] boxes = new LayerBoundingBox[origBoxes.length];

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
        return new DeegreeEnvelope(this.layer.getLatLonBoundingBox());
    }

    @Override
    public void setFilterString(final String filterString) {
        this.filterString = filterString;
    }
}
