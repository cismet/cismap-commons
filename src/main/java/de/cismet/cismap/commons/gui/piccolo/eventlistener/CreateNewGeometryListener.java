package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import java.awt.Color;

/**
 *
 * @author jruiz
 */
public class CreateNewGeometryListener extends CreateGeometryListener {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    //delegate to enable zoom during creation.
    private final PBasicInputEventHandler zoomDelegate;

    /** Creates a new instance of CreateNewGeometryListener */
    private CreateNewGeometryListener(MappingComponent mc, Class geometryFeatureClass) {
        super(mc, geometryFeatureClass);
        zoomDelegate = new RubberBandZoomListener();
    }

    public CreateNewGeometryListener(MappingComponent mc) {
        this(mc, PureNewFeature.class);
    }

    @Override
    protected Color getFillingColor() {
        if (isInMode(POLYGON)) {
            return new Color(1f, 0f, 0f, 0.5f);
        } else {
            return null;
        }
    }

    @Override
    protected void finishGeometry(PureNewFeature newFeature) {
        super.finishGeometry(newFeature);

        newFeature.setEditable(true);
        mc.getFeatureCollection().addFeature(newFeature);
        mc.getFeatureCollection().holdFeature(newFeature);
    }

    @Override
    public void mouseWheelRotated(PInputEvent pie) {
        //delegate zoom event
        zoomDelegate.mouseWheelRotated(pie);
        //trigger full repaint
        mouseMoved(pie);
    }



}
