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
    private String startMode = "ZOOM";//NOI18N
    private XBoundingBox initialBoundingBox = new XBoundingBox(0d, 0d, 0d, 0d, "",true);//NOI18N

    public GlobalPreferences(Element element) {
        try {
            animationDuration = element.getAttribute("animationDuration").getIntValue();//NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.animationDuration  ", e);//NOI18N
        }
        try {
            snappingEnabled = element.getAttribute("snappingEnabled").getBooleanValue();//NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.snappingEnabled  ", e);//NOI18N
        }
        try {
            snappingPreviewEnabled = element.getAttribute("snappingPreviewEnabled").getBooleanValue();//NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.animationDuration  ", e);//NOI18N
        }
        try {
            snappingRectSize = element.getAttribute("snappingRectSize").getIntValue();//NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.snappingPreviewEnabled  ", e);//NOI18N
        }
        try {
            startMode = element.getAttribute("startMode").getValue();//NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.startMode  ", e);//NOI18N
        }
        try {
            panPerformanceBooster = element.getAttribute("panPerformanceBooster").getBooleanValue();//NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.panPerformanceBooster  ", e);//NOI18N
        }
        try {
            errorAbolitionTime = element.getAttribute("errorAbolitionTime").getIntValue();//NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.errorAbolitionTime  ", e);//NOI18N
        }
        try {
            initialBoundingBox.setX1(element.getAttribute("initial_x1").getDoubleValue());//NOI18N
            initialBoundingBox.setY1(element.getAttribute("initial_y1").getDoubleValue());//NOI18N
            initialBoundingBox.setX2(element.getAttribute("initial_x2").getDoubleValue());//NOI18N
            initialBoundingBox.setY2(element.getAttribute("initial_y2").getDoubleValue());//NOI18N
            initialBoundingBox.setMetric(element.getAttribute("initial_metric").getBooleanValue());//NOI18N
            initialBoundingBox.setSrs(element.getAttributeValue("initial_srs"));//NOI18N
        } catch (Exception e) {
            log.warn("Read preferences. Error. GlobalPreferences.initialBoundingBox  ", e);//NOI18N
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
