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
import de.cismet.cismap.commons.wms.capabilities.WMSCapabilities;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeegreeLayer implements Layer {

    //~ Instance fields --------------------------------------------------------

    private org.deegree.ogcwebservices.wms.capabilities.Layer layer;
    private WMSCapabilities capabilities;
    private String filterString;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeegreeLayer object.
     *
     * @param  layer         DOCUMENT ME!
     * @param  capabilities  DOCUMENT ME!
     */
    public DeegreeLayer(final org.deegree.ogcwebservices.wms.capabilities.Layer layer,
            final WMSCapabilities capabilities) {
        this.layer = layer;
        this.capabilities = capabilities;
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

    /**
     * DOCUMENT ME!
     *
     * @return  the scale hint not the scale denominator regardless of the service version
     */
    @Override
    public double getScaleDenominationMax() {
        if (layer.getScaleHint() != null) {
            final double maxScaleHint = layer.getScaleHint().getMax();

            if (capabilities != null) {
                final String version = capabilities.getVersion();

                if (((version != null) && version.trim().equals("1.3")) || version.trim().equals("1.3.0")) {
                    // version 1.3 uses a scaleDenominator instead of a scaleHint
                    // See http://wiki.deegree.org/deegreeWiki/HowToUseScaleHintAndScaleDenominator
                    final double pixelwidth = maxScaleHint * 0.00028;
                    return Math.sqrt(pixelwidth * pixelwidth * 2);
                }
            }

            return maxScaleHint;
        } else {
            return 0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the scale hint not the scale denominator regardless of the service version
     */
    @Override
    public double getScaleDenominationMin() {
        if (layer.getScaleHint() != null) {
            final double minScaleHint = layer.getScaleHint().getMin();

            if (capabilities != null) {
                final String version = capabilities.getVersion();

                if (((version != null) && version.trim().equals("1.3")) || version.trim().equals("1.3.0")) {
                    // version 1.3 uses a scaleDenominator instead of a scaleHint.
                    // See http://wiki.deegree.org/deegreeWiki/HowToUseScaleHintAndScaleDenominator
                    final double pixelwidth = minScaleHint * 0.00028;
                    return Math.sqrt(pixelwidth * pixelwidth * 2);
                }
            }

            return minScaleHint;
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
                final Layer l = new DeegreeLayer(deegreeLayer[i], capabilities);
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
        if (((l.getTitle().toLowerCase().indexOf(filterString.toLowerCase()) != -1)
                        || containsFilterString(l.getKeywordList()))
                    && (l.getLayer().length == 0)) {
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

    /**
     * DOCUMENT ME!
     *
     * @param   keywords  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean containsFilterString(final String[] keywords) {
        if (keywords != null) {
            for (final String tmp : keywords) {
                if ((tmp != null) && (tmp.toLowerCase().indexOf(filterString.toLowerCase()) != -1)) {
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

    @Override
    public WMSCapabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public void setCapabilities(final WMSCapabilities capabilities) {
        this.capabilities = capabilities;
    }
}
