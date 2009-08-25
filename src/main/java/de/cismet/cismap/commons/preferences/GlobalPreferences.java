package de.cismet.cismap.commons.preferences;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.XBoundingBox;
import org.apache.log4j.Logger;
import org.jdom.Element;

public class GlobalPreferences {

    final Logger log = Logger.getLogger(this.getClass());
    private int animationDuration = 500;
    private boolean snappingEnabled = false;
    private boolean snappingPreviewEnabled = false;
    private boolean panPerformanceBooster = false;
    private int errorAbolitionTime = 2000;
    private int snappingRectSize = 20;
    private String startMode = "ZOOM";
    private XBoundingBox initialBoundingBox = new XBoundingBox(0d, 0d, 0d, 0d, "",true);

    public GlobalPreferences(Element element) {
        try {
            animationDuration = element.getAttribute("animationDuration").getIntValue();
        } catch (Exception e) {
            log.warn("Preferences Auslesen. Fehler. GlobalPreferences.animationDuration  ", e);
        }
        try {
            snappingEnabled = element.getAttribute("snappingEnabled").getBooleanValue();
        } catch (Exception e) {
            log.warn("Preferences Auslesen. Fehler. GlobalPreferences.snappingEnabled  ", e);
        }
        try {
            snappingPreviewEnabled = element.getAttribute("snappingPreviewEnabled").getBooleanValue();
        } catch (Exception e) {
            log.warn("Preferences Auslesen. Fehler. GlobalPreferences.animationDuration  ", e);
        }
        try {
            snappingRectSize = element.getAttribute("snappingRectSize").getIntValue();
        } catch (Exception e) {
            log.warn("Preferences Auslesen. Fehler. GlobalPreferences.snappingPreviewEnabled  ", e);
        }
        try {
            startMode = element.getAttribute("startMode").getValue();
        } catch (Exception e) {
            log.warn("Preferences Auslesen. Fehler. GlobalPreferences.startMode  ", e);
        }
        try {
            panPerformanceBooster = element.getAttribute("panPerformanceBooster").getBooleanValue();
        } catch (Exception e) {
            log.warn("Preferences Auslesen. Fehler. GlobalPreferences.panPerformanceBooster  ", e);
        }
        try {
            errorAbolitionTime = element.getAttribute("errorAbolitionTime").getIntValue();
        } catch (Exception e) {
            log.warn("Preferences Auslesen. Fehler. GlobalPreferences.errorAbolitionTime  ", e);
        }
        try {
            initialBoundingBox.setX1(element.getAttribute("initial_x1").getDoubleValue());
            initialBoundingBox.setY1(element.getAttribute("initial_y1").getDoubleValue());
            initialBoundingBox.setX2(element.getAttribute("initial_x2").getDoubleValue());
            initialBoundingBox.setY2(element.getAttribute("initial_y2").getDoubleValue());
            initialBoundingBox.setMetric(element.getAttribute("initial_metric").getBooleanValue());
            initialBoundingBox.setSrs(element.getAttributeValue("initial_srs"));
        } catch (Exception e) {
            log.warn("Preferences Auslesen. Fehler. GlobalPreferences.initialBoundingBox  ", e);
        }
    }

    public int getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    public boolean isSnappingEnabled() {
        return snappingEnabled;
    }

    public void setSnappingEnabled(boolean snappingEnabled) {
        this.snappingEnabled = snappingEnabled;
    }

    public boolean isSnappingPreviewEnabled() {
        return snappingPreviewEnabled;
    }

    public void setSnappingPreviewEnabled(boolean snappingPreviewEnabled) {
        this.snappingPreviewEnabled = snappingPreviewEnabled;
    }

    public int getSnappingRectSize() {
        return snappingRectSize;
    }

    public void setSnappingRectSize(int snappingRectSize) {
        this.snappingRectSize = snappingRectSize;
    }

    public String getStartMode() {
        return startMode;
    }

    public void setStartMode(String startMode) {
        this.startMode = startMode;
    }

    public XBoundingBox getInitialBoundingBox() {
        return initialBoundingBox;
    }

    public void setInitialBoundingBox(XBoundingBox initialBoundingBox) {
        this.initialBoundingBox = initialBoundingBox;
    }

    public boolean isPanPerformanceBoosterEnabled() {
        return panPerformanceBooster;
    }

    public void setPanPerformanceBoosterEnabled(boolean panPerformanceBooster) {
        this.panPerformanceBooster = panPerformanceBooster;
    }

    public int getErrorAbolitionTime() {
        return errorAbolitionTime;
    }

    public void setErrorAbolitionTime(int errorAbolitionTime) {
        this.errorAbolitionTime = errorAbolitionTime;
    }
}
