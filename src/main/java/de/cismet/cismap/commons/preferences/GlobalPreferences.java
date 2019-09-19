/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.preferences;

import org.apache.log4j.Logger;

import org.jdom.Element;

import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class GlobalPreferences {

    //~ Instance fields --------------------------------------------------------

    final Logger log = Logger.getLogger(this.getClass());
    private int animationDuration = 500;
    /**
     * DOCUMENT ME!
     *
     * @deprecated  use snappingMode instead
     */
    private boolean snappingEnabled = false;
    private MappingComponent.SnappingMode snappingMode = null;
    private boolean snappingPreviewEnabled = false;
    private boolean panPerformanceBooster = false;
    private int errorAbolitionTime = 2000;
    private int snappingRectSize = 20;
    private String startMode = "ZOOM";                                                    // NOI18N
    private XBoundingBox initialBoundingBox = new XBoundingBox(0d, 0d, 0d, 0d, "", true); // NOI18N

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GlobalPreferences object.
     *
     * @param  element  DOCUMENT ME!
     */
    public GlobalPreferences(final Element element) {
        try {
            animationDuration = element.getAttribute("animationDuration").getIntValue();               // NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.animationDuration  ", e);             // NOI18N
        }
        try {
            final String snappingModeValue = element.getAttribute("snappingMode").getValue();
            for (final MappingComponent.SnappingMode snappingMode : MappingComponent.SnappingMode.values()) {
                if (snappingMode.name().equalsIgnoreCase(snappingModeValue)) {
                    this.snappingMode = snappingMode;
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.snappingMode  ", e);                  // NOI18N
        }
        if (this.snappingMode == null) {
            try {
                snappingEnabled = element.getAttribute("snappingEnabled").getBooleanValue();           // NOI18N
            } catch (Exception e) {
                log.warn("Read preferences. Error. GlobalPreferences.snappingEnabled  ", e);           // NOI18N
            }
        }
        try {
            snappingPreviewEnabled = element.getAttribute("snappingPreviewEnabled").getBooleanValue(); // NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.animationDuration  ", e);             // NOI18N
        }
        try {
            snappingRectSize = element.getAttribute("snappingRectSize").getIntValue();                 // NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.snappingPreviewEnabled  ", e);        // NOI18N
        }
        try {
            startMode = element.getAttribute("startMode").getValue();                                  // NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.startMode  ", e);                     // NOI18N
        }
        try {
            panPerformanceBooster = element.getAttribute("panPerformanceBooster").getBooleanValue();   // NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.panPerformanceBooster  ", e);         // NOI18N
        }
        try {
            errorAbolitionTime = element.getAttribute("errorAbolitionTime").getIntValue();             // NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.errorAbolitionTime  ", e);            // NOI18N
        }
        try {
            initialBoundingBox.setX1(element.getAttribute("initial_x1").getDoubleValue());             // NOI18N
            initialBoundingBox.setY1(element.getAttribute("initial_y1").getDoubleValue());             // NOI18N
            initialBoundingBox.setX2(element.getAttribute("initial_x2").getDoubleValue());             // NOI18N
            initialBoundingBox.setY2(element.getAttribute("initial_y2").getDoubleValue());             // NOI18N
            initialBoundingBox.setMetric(element.getAttribute("initial_metric").getBooleanValue());    // NOI18N
            initialBoundingBox.setSrs(element.getAttributeValue("initial_srs"));                       // NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.initialBoundingBox  ", e);            // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getAnimationDuration() {
        return animationDuration;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  animationDuration  DOCUMENT ME!
     */
    public void setAnimationDuration(final int animationDuration) {
        this.animationDuration = animationDuration;
    }

    /**
     * DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  use snappingMode instead
     */
    public boolean isSnappingEnabled() {
        return snappingEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponent.SnappingMode getSnappingMode() {
        return snappingMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param       snappingEnabled  DOCUMENT ME!
     *
     * @deprecated  use snappingMode instead
     */
    public void setSnappingEnabled(final boolean snappingEnabled) {
        this.snappingEnabled = snappingEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSnappingPreviewEnabled() {
        return snappingPreviewEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  snappingPreviewEnabled  DOCUMENT ME!
     */
    public void setSnappingPreviewEnabled(final boolean snappingPreviewEnabled) {
        this.snappingPreviewEnabled = snappingPreviewEnabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getSnappingRectSize() {
        return snappingRectSize;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  snappingRectSize  DOCUMENT ME!
     */
    public void setSnappingRectSize(final int snappingRectSize) {
        this.snappingRectSize = snappingRectSize;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getStartMode() {
        return startMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  startMode  DOCUMENT ME!
     */
    public void setStartMode(final String startMode) {
        this.startMode = startMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public XBoundingBox getInitialBoundingBox() {
        return initialBoundingBox;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  initialBoundingBox  DOCUMENT ME!
     */
    public void setInitialBoundingBox(final XBoundingBox initialBoundingBox) {
        this.initialBoundingBox = initialBoundingBox;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isPanPerformanceBoosterEnabled() {
        return panPerformanceBooster;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  panPerformanceBooster  DOCUMENT ME!
     */
    public void setPanPerformanceBoosterEnabled(final boolean panPerformanceBooster) {
        this.panPerformanceBooster = panPerformanceBooster;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getErrorAbolitionTime() {
        return errorAbolitionTime;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  errorAbolitionTime  DOCUMENT ME!
     */
    public void setErrorAbolitionTime(final int errorAbolitionTime) {
        this.errorAbolitionTime = errorAbolitionTime;
    }
}
