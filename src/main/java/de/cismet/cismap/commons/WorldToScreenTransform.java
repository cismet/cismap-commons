/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class WorldToScreenTransform {

    //~ Instance fields --------------------------------------------------------

    double xHome;
    double yHome;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of WorldToScreenTransform.
     *
     * @param  x  DOCUMENT ME!
     * @param  y  DOCUMENT ME!
     */
    public WorldToScreenTransform(final double x, final double y) {
        xHome = x;
        yHome = y;
        if (log.isDebugEnabled()) {
            log.debug("WorldToScreenTransform(x=" + xHome + ",y=" + yHome + ")"); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   x  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getSourceX(final double x) {
        return getWorldX(x);
    }
    /**
     * DOCUMENT ME!
     *
     * @param   y  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getSourceY(final double y) {
        return getWorldY(y);
    }
    /**
     * DOCUMENT ME!
     *
     * @param   x  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getDestX(final double x) {
        return getScreenX(x);
    }
    /**
     * DOCUMENT ME!
     *
     * @param   y  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getDestY(final double y) {
        return getScreenY(y);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   screenX  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getWorldX(final double screenX) {
        return screenX + xHome;
    }
    /**
     * DOCUMENT ME!
     *
     * @param   screenY  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getWorldY(final double screenY) {
        return yHome - screenY;
    }
    /**
     * DOCUMENT ME!
     *
     * @param   worldX  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getScreenX(final double worldX) {
        return worldX - xHome;
    }
    /**
     * DOCUMENT ME!
     *
     * @param   worldY  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getScreenY(final double worldY) {
        return (-1.0) * (worldY - yHome);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   wtst  DOCUMENT ME!
     * @param   x     DOCUMENT ME!
     * @param   y     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String xyToScreen(final WorldToScreenTransform wtst, final double x, final double y) {
        return wtst.getScreenX(x) + "," + wtst.getScreenY(y); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final WorldToScreenTransform wtst = new WorldToScreenTransform(-180, 90);
        System.out.println(xyToScreen(wtst, -180, 90));
        System.out.println(xyToScreen(wtst, -180, -90));
        System.out.println(xyToScreen(wtst, 180, -90));
        System.out.println(xyToScreen(wtst, 180, 90));

//        System.out.println(wtst.getWorldX(wtst.getScreenX(0)));
//        System.out.println(wtst.getWorldY(wtst.getScreenY(0)));

    }

    @Override
    public String toString() {
        return "de.cismet.cismap.commons.WorldToScreenTransform: xHome:" + xHome + " yHome:" + yHome; // NOI18N
    }
}
