/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.util.PLocator;

import pswing.PSwing;
import pswing.PSwingCanvas;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.text.DecimalFormat;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateLinearReferencedMarksListener;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class LinearReferencedLineMarkPHandle extends PPath {

    //~ Static fields/initializers ---------------------------------------------

    public static final double DEFAULT_HANDLE_SIZE = 8;
    public static final Shape DEFAULT_HANDLE_SHAPE = new Ellipse2D.Double(
            0f,
            0f,
            DEFAULT_HANDLE_SIZE,
            DEFAULT_HANDLE_SIZE);
    public static final Color DEFAULT_COLOR = Color.GREEN;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            LinearReferencedLineMarkPHandle.class);

    //~ Instance fields --------------------------------------------------------

    private PLocator locator;
    private MappingComponent mc = null;
    private SublinePanel panel;
    private CreateLinearReferencedMarksListener measurementListener;
    private PSwing pswingComp;

    //~ Constructors -----------------------------------------------------------

    /**
     * Construct a new handle that will use the given locator to locate itself on its parent node.
     *
     * @param  locator              DOCUMENT ME!
     * @param  measurementListener  DOCUMENT ME!
     * @param  mc                   DOCUMENT ME!
     */
    public LinearReferencedLineMarkPHandle(final PLocator locator,
            final CreateLinearReferencedMarksListener measurementListener,
            final MappingComponent mc) {
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

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void installEventListener() {
        final PBasicInputEventHandler moveAndClickListener = new PBasicInputEventHandler() {

                @Override
                public void mouseClicked(final PInputEvent pInputEvent) {
                    handleClicked(pInputEvent);
                }

                @Override
                public void mouseEntered(final PInputEvent pInputEvent) {
//                    switch (measurementListener.getModus()) {
//                        case MARK_SELECTION: {
                    measurementListener.getPLayer().removeChild(LinearReferencedLineMarkPHandle.this);
                    measurementListener.getPLayer().addChild(LinearReferencedLineMarkPHandle.this);
//                            break;
//                        }
//                    }
                }
            };

        addInputEventListener(moveAndClickListener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pInputEvent  DOCUMENT ME!
     */
    private void handleClicked(final PInputEvent pInputEvent) {
//        switch (measurementListener.getModus()) {
//            case MARK_SELECTION: {
        if (LOG.isDebugEnabled()) {
            LOG.debug("handle selected");
        }
        if (pInputEvent.isRightMouseButton()) {
            final MouseEvent swingEvent = ((MouseEvent)pInputEvent.getSourceSwingEvent());
//                    measurementListener.setSelectedSubline(this);
            measurementListener.getContextMenu().show(pswingComp.getComponent(), swingEvent.getX(), swingEvent.getY());
        }
//                break;
//            }
//        }
    }

    /**
     * DOCUMENT ME!
     */
    private void initPanel() {
        panel = new SublinePanel();

        pswingComp = new PSwing((PSwingCanvas)mc, panel);
        panel.setPNodeParent(pswingComp);
        addChild(pswingComp);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  start  DOCUMENT ME!
     * @param  end    DOCUMENT ME!
     */
    public void setPositions(final double start, final double end) {
        panel.setPositionStart(new DecimalFormat("0.00").format(start));
        panel.setPositionEnd(new DecimalFormat("0.00").format(end));
        relocateHandle();
        repaint();
    }

    /**
     * DOCUMENT ME!
     */
    protected void installHandleEventHandlers() {
        addPropertyChangeListener(PNode.PROPERTY_TRANSFORM, new PropertyChangeListener() {

                @Override
                public void propertyChange(final PropertyChangeEvent evt) {
                    relocateHandle();
                }
            });
    }

    /**
     * Get the locator that this handle uses to position itself on its parent node.
     *
     * @return  DOCUMENT ME!
     */
    public PLocator getLocator() {
        return locator;
    }

    /**
     * Set the locator that this handle uses to position itself on its parent node.
     *
     * @param  locator  DOCUMENT ME!
     */
    public void setLocator(final PLocator locator) {
        this.locator = locator;
        invalidatePaint();
        relocateHandle();
    }

    @Override
    public void setParent(final PNode newParent) {
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
            final PBounds b = getBoundsReference();
            final Point2D aPoint = locator.locatePoint(null);
            mc.getCamera().viewToLocal(aPoint);

            final double newCenterX = aPoint.getX();
            final double newCenterY = aPoint.getY();

            pswingComp.setOffset(newCenterX + DEFAULT_HANDLE_SIZE, newCenterY - (pswingComp.getHeight() / 2));

            if ((newCenterX != b.getCenterX()) || (newCenterY != b.getCenterY())) {
                this.setBounds(0, 0, DEFAULT_HANDLE_SIZE, DEFAULT_HANDLE_SIZE);
                centerBoundsOnPoint(newCenterX, newCenterY);
            }
        }
    }
}
