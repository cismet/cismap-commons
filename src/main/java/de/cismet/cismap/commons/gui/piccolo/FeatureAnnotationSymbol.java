/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo;

import java.awt.Image;

import java.net.URL;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class FeatureAnnotationSymbol extends FixedPImage implements ParentNodeIsAPFeature {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FeatureAnnotationSymbol.class);

    //~ Instance fields --------------------------------------------------------

    private FeatureAnnotationSymbol selectedFeatureAnnotationSymbol = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of FeatureAnnotationSymbol.
     */
    public FeatureAnnotationSymbol() {
        super();
    }

    /**
     * Creates a new FeatureAnnotationSymbol object.
     *
     * @param  newImage  DOCUMENT ME!
     */
    public FeatureAnnotationSymbol(final Image newImage) {
        super(newImage);
    }

    /**
     * Creates a new FeatureAnnotationSymbol object.
     *
     * @param  fileName  DOCUMENT ME!
     */
    public FeatureAnnotationSymbol(final String fileName) {
        super(fileName);
    }

    /**
     * Creates a new FeatureAnnotationSymbol object.
     *
     * @param  url  DOCUMENT ME!
     */
    public FeatureAnnotationSymbol(final URL url) {
        super(url);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureAnnotationSymbol getSelectedFeatureAnnotationSymbol() {
        return selectedFeatureAnnotationSymbol;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selectedFeatureAnnotationSymbol  DOCUMENT ME!
     */
    public void setSelectedFeatureAnnotationSymbol(final Image selectedFeatureAnnotationSymbol) {
        if (selectedFeatureAnnotationSymbol != null) {
            this.selectedFeatureAnnotationSymbol = new FeatureAnnotationSymbol(selectedFeatureAnnotationSymbol);
            this.selectedFeatureAnnotationSymbol.setSweetSpotX(this.getSweetSpotX());
            this.selectedFeatureAnnotationSymbol.setSweetSpotY(this.getSweetSpotY());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fas  DOCUMENT ME!
     */
    public void setSelectedFeatureAnnotationSymbol(final FeatureAnnotationSymbol fas) {
        this.selectedFeatureAnnotationSymbol = fas;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   unselected  DOCUMENT ME!
     * @param   selected    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static FeatureAnnotationSymbol newCenteredFeatureAnnotationSymbol(final Image unselected,
            final Image selected) {
        final FeatureAnnotationSymbol tmpSymbol = new FeatureAnnotationSymbol(unselected);
        tmpSymbol.setSelectedFeatureAnnotationSymbol(selected);
        tmpSymbol.setSweetSpotX(0.5d);
        tmpSymbol.setSweetSpotY(0.5d);
        return tmpSymbol;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   unselected  DOCUMENT ME!
     * @param   selected    DOCUMENT ME!
     * @param   sweetSpotX  DOCUMENT ME!
     * @param   sweetSpotY  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static FeatureAnnotationSymbol newCustomSweetSpotFeatureAnnotationSymbol(final Image unselected,
            final Image selected,
            final double sweetSpotX,
            final double sweetSpotY) {
        final FeatureAnnotationSymbol tmpSymbol = new FeatureAnnotationSymbol(unselected);
        tmpSymbol.setSelectedFeatureAnnotationSymbol(selected);
        tmpSymbol.setSweetSpotX(sweetSpotX);
        tmpSymbol.setSweetSpotY(sweetSpotY);
        return tmpSymbol;
    }
}
