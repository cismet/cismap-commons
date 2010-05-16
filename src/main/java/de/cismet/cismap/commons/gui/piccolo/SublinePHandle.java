package de.cismet.cismap.commons.gui.piccolo;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.MeasurementMoveListener;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.util.PLocator;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import pswing.PSwing;
import pswing.PSwingCanvas;

public class SublinePHandle extends PPath {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    public static final double DEFAULT_HANDLE_SIZE = 8;
    public static final Shape DEFAULT_HANDLE_SHAPE = new Ellipse2D.Double(0f, 0f, DEFAULT_HANDLE_SIZE, DEFAULT_HANDLE_SIZE);
    public static final Color DEFAULT_COLOR = Color.GREEN;

    private PLocator locator;
    private MappingComponent mc = null;
    private SublinePanel panel;
    private MeasurementMoveListener measurementListener;
    private PSwing pswingComp;

    /**
     * Construct a new handle that will use the given locator
     * to locate itself on its parent node.
     */
    public SublinePHandle(PLocator locator, MeasurementMoveListener measurementListener, MappingComponent mc) {
        super(DEFAULT_HANDLE_SHAPE);

        this.mc = mc;
        this.locator = locator;
        this.measurementListener = measurementListener;

        installEventListener();

        setPaint(DEFAULT_COLOR);
        installHandleEventHandlers();
        startResizeBounds();

        initPanel();

        relocateHandle();
    }

    private void installEventListener() {
        PBasicInputEventHandler moveAndClickListener = new PBasicInputEventHandler() {

            @Override
            public void mouseClicked(PInputEvent pInputEvent) {
                handleClicked(pInputEvent);
            }

            @Override
            public void mouseEntered(PInputEvent pInputEvent) {
                switch (measurementListener.getModus()) {
                    case MARK_SELECTION:
                        measurementListener.getPLayer().removeChild(SublinePHandle.this);
                        measurementListener.getPLayer().addChild(SublinePHandle.this);
                        break;
                }
            }

        };

        addInputEventListener(moveAndClickListener);
    }

    private void handleClicked(PInputEvent pInputEvent) {
        switch (measurementListener.getModus()) {
            case MARK_SELECTION:
                log.debug("handle selected");
                if (pInputEvent.isLeftMouseButton()) {
                    MouseEvent swingEvent = ((MouseEvent)pInputEvent.getSourceSwingEvent());
                    measurementListener.setSelectedSubline(this);
                    measurementListener.getContextMenu().show(pswingComp.getComponent(), swingEvent.getX(), swingEvent.getY());
                }
                break;
        }
    }

    private void initPanel() {
        panel = new SublinePanel();

        pswingComp = new PSwing((PSwingCanvas) mc, panel);
        panel.setPNodeParent(pswingComp);
        addChild(pswingComp);
    }

    public void setPositions(double start, double end) {
        panel.setPositionStart(new DecimalFormat("0.00").format(start));
        panel.setPositionEnd(new DecimalFormat("0.00").format(end));
        relocateHandle();
        repaint();
    }

    protected void installHandleEventHandlers() {

        addPropertyChangeListener(PNode.PROPERTY_TRANSFORM, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                relocateHandle();
            }
        });

    }

    /**
     * Get the locator that this handle uses to position itself on its
     * parent node.
     */
    public PLocator getLocator() {
        return locator;
    }

    /**
     * Set the locator that this handle uses to position itself on its
     * parent node.
     */
    public void setLocator(PLocator locator) {
        this.locator = locator;
        invalidatePaint();
        relocateHandle();
    }

    @Override
    public void setParent(PNode newParent) {
        super.setParent(newParent);
        relocateHandle();
    }

    @Override
    public void parentBoundsChanged() {
        relocateHandle();
    }

    /**
     * Force this handle to relocate itself using its locator.
     */
    public void relocateHandle() {
        if (locator != null) {
            PBounds b = getBoundsReference();
            Point2D aPoint = locator.locatePoint(null);
            mc.getCamera().viewToLocal(aPoint);

            double newCenterX = aPoint.getX();
            double newCenterY = aPoint.getY();

            pswingComp.setOffset(newCenterX + DEFAULT_HANDLE_SIZE, newCenterY - pswingComp.getHeight() / 2);

            if (newCenterX != b.getCenterX() || newCenterY != b.getCenterY()) {
                this.setBounds(0, 0, DEFAULT_HANDLE_SIZE, DEFAULT_HANDLE_SIZE);
                centerBoundsOnPoint(newCenterX, newCenterY);
            }
        }
    }
}
