/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.tools;

/**
 * PixelDPICalculator calculates the right resolution if one value was changed. E.g. if the height is changed the width
 * and the dpi has to be recalculated.
 *
 * @version  $Revision$, $Date$
 */
public class PixelDPICalculator {

    //~ Instance fields --------------------------------------------------------

    private int widthPixel;
    private int heightPixel;
    private int dpi;
    private final double aspectRatio; // width / height
    // needed to calculate the dpi. May seem unnecessary, but using the equation newdpi = newWidth * olddpi /
    // oldWidth has the problem that newdpi can become 0.
    private final double widthInches;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PixelDPICalculator object.
     *
     * @param  widthPixel   DOCUMENT ME!
     * @param  heightPixel  DOCUMENT ME!
     * @param  dpi          DOCUMENT ME!
     */
    public PixelDPICalculator(final int widthPixel, final int heightPixel, final int dpi) {
        this.widthPixel = widthPixel;
        this.heightPixel = heightPixel;
        this.dpi = dpi;

        this.aspectRatio = widthPixel * 1d / heightPixel;
        this.widthInches = widthPixel * 1d / dpi;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getHeightPixel() {
        return heightPixel;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newHeightPixel  DOCUMENT ME!
     */
    public void setHeightPixel(final int newHeightPixel) {
        this.widthPixel = (int)Math.round(newHeightPixel * aspectRatio);
        final double newDpi = widthPixel / widthInches;

        this.dpi = (int)Math.round(newDpi);
        this.heightPixel = newHeightPixel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getDPI() {
        return dpi;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newDpi  DPI DOCUMENT ME!
     */
    public void setDPI(final int newDpi) {
        final double newWidthPixel = widthPixel * 1d / dpi * newDpi;
        this.widthPixel = (int)Math.round(newWidthPixel);

        this.heightPixel = (int)Math.round(widthPixel * 1d / aspectRatio);

        this.dpi = newDpi;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getWidthPixel() {
        return widthPixel;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newWidthPixel  DOCUMENT ME!
     */
    public void setWidthPixel(final int newWidthPixel) {
        this.heightPixel = (int)Math.round(newWidthPixel * 1d / aspectRatio);
        this.widthPixel = newWidthPixel;

        final double newDpi = widthPixel / widthInches;
        this.dpi = (int)Math.round(newDpi);
    }
}
