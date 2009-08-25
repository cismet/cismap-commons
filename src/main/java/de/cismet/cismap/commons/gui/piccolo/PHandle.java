/*
 * Copyright (c) 2002-@year@, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Piccolo was written at the Human-Computer Interaction Laboratory www.cs.umd.edu/hcil by Jesse Grosjean
 * under the supervision of Ben Bederson. The Piccolo website is www.cs.umd.edu/hcil/piccolo.
 */
package de.cismet.cismap.commons.gui.piccolo;

import de.cismet.cismap.commons.gui.MappingComponent;
import java.awt.Color;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.util.PLocator;

/**
 * <b>PHandle</b> is used to modify some aspect of Piccolo when it
 * is dragged. Each handle has a PLocator that it uses to automatically position
 * itself. See PBoundsHandle for an example of a handle that resizes the bounds
 * of another node.
 * <P>
 * @version 1.0
 * @author Jesse Grosjean
 */
public class PHandle extends PPath {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public static double DEFAULT_HANDLE_SIZE = 8;
    public static Shape DEFAULT_HANDLE_SHAPE = new Ellipse2D.Double(0f, 0f, DEFAULT_HANDLE_SIZE, DEFAULT_HANDLE_SIZE);
    //public static Shape DEFAULT_HANDLE_SHAPE = new Rectangle.Double(0f, 0f, DEFAULT_HANDLE_SIZE, DEFAULT_HANDLE_SIZE);
    public static Color DEFAULT_COLOR = new Color(1f, 1f, 1f, 0.4f);//Color.white;
    public static Color DEFAULT_SELECTED_COLOR = Color.red;
    private static PAffineTransform TEMP_TRANSFORM = new PAffineTransform();
    private PLocator locator;
    private PDragSequenceEventHandler handleDragger;
    private boolean selected = false;
    private MappingComponent mc = null;
    boolean inDragOperation = false;

    /**
     * Construct a new handle that will use the given locator
     * to locate itself on its parent node.
     */
    public PHandle(PLocator aLocator, MappingComponent mc) {
//                super(new Rectangle2D.Double(0f, 0f, 1f,1f));
        super(DEFAULT_HANDLE_SHAPE);
        this.mc = mc;
        //setStroke(new FixedWidthStroke());
        //log.fatal("Scale:"+((double)(DEFAULT_HANDLE_SIZE/PPaintContext.CURRENT_PAINT_CONTEXT.getScale())));
        locator = aLocator;
        setPaint(DEFAULT_COLOR);
        installHandleEventHandlers();
        startResizeBounds();
        relocateHandle();
    }

    //zum \u00FCberschreiben
    public void removeHandle() {
    }

    public void duplicateHandle() {
    }

    protected void installHandleEventHandlers() {
        handleDragger = new PDragSequenceEventHandler() {

            @Override
            protected void startDrag(PInputEvent event) {
                log.debug("Handle Start Drag");
                super.startDrag(event);
                inDragOperation = true;
                startHandleDrag(event.getPositionRelativeTo(PHandle.this), event);
            }

            @Override
            protected void drag(PInputEvent event) {
                log.debug("Handle Drag");
                super.drag(event);
                PDimension aDelta = event.getDeltaRelativeTo(PHandle.this);
                if (aDelta.getWidth() != 0 || aDelta.getHeight() != 0) {
                    dragHandle(aDelta, event);
                }
            }

            @Override
            protected void endDrag(PInputEvent event) {
                log.debug("Handle End Drag");
                super.endDrag(event);
                inDragOperation = false;
                endHandleDrag(event.getPositionRelativeTo(PHandle.this), event);
            }
        };

        addPropertyChangeListener(PNode.PROPERTY_TRANSFORM, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                relocateHandle();
            }
        });

        handleDragger.setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK));
        handleDragger.getEventFilter().setMarksAcceptedEventsAsHandled(true);
        handleDragger.getEventFilter().setAcceptsMouseEntered(false);
        handleDragger.getEventFilter().setAcceptsMouseExited(false);
        handleDragger.getEventFilter().setAcceptsMouseMoved(false); 	// no need for moved events for handle interaction,
        // so reject them so we don't consume them
        addInputEventListener(handleDragger);

        //Test
        PBasicInputEventHandler moveAndClickListener = new PBasicInputEventHandler() {

            @Override
            public void mouseClicked(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
                log.debug("Handle Mouse Clicked");
                handleClicked(pInputEvent);
            }

            @Override
            public void mouseMoved(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
                //log.debug("Handle Mouse Moved");
                if (!inDragOperation) {
                    mouseMovedNotInDragOperation(pInputEvent);
                }
            }
        };
        addInputEventListener(moveAndClickListener);
    }

    /**
     * Return the event handler that is responsible for the drag handle
     * interaction.
     */
    public PDragSequenceEventHandler getHandleDraggerHandler() {
        return handleDragger;
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
    public void setLocator(PLocator aLocator) {
        locator = aLocator;
        invalidatePaint();
        relocateHandle();
    }

    //****************************************************************
    // Handle Dragging - These are the methods the subclasses should
    // normally override to give a handle unique behavior.
    //****************************************************************
    /**
     * Override this method to get notified when the handle starts to get dragged.
     */
    public void startHandleDrag(Point2D aLocalPoint, PInputEvent aEvent) {
    }

    /**
     * Override this method to get notified as the handle is dragged.
     */
    public void dragHandle(PDimension aLocalDimension, PInputEvent aEvent) {
    }

    /**
     * Override this method to get notified when the handle stops getting dragged.
     */
    public void endHandleDrag(Point2D aLocalPoint, PInputEvent aEvent) {
    }

    public void handleClicked(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
    }

    public void mouseMovedNotInDragOperation(edu.umd.cs.piccolo.event.PInputEvent pInputEvent) {
    }

    //****************************************************************
    // Layout - When a handle's parent's layout changes the handle
    // invalidates its own layout and then repositions itself on its
    // parents bounds using its locator to determine that new
    // position.
    //****************************************************************
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
            //log.fatal("*vorher:" +  aPoint);
            mc.getCamera().viewToLocal(aPoint);
            //log.fatal("*nachher:" +  aPoint);

//			if (locator instanceof PNodeLocator) {
//				PNode located = ((PNodeLocator)locator).getNode();
//				PNode parent = getParent();
//
//				located.localToGlobal(aPoint);
//				globalToLocal(aPoint);
//
//				if (parent != located && parent instanceof PCamera) {
//					((PCamera)parent).viewToLocal(aPoint);
//				}
//			}

            double newCenterX = aPoint.getX();
            double newCenterY = aPoint.getY();

            if (newCenterX != b.getCenterX() ||
                    newCenterY != b.getCenterY()) {
                this.setBounds(0, 0, DEFAULT_HANDLE_SIZE, DEFAULT_HANDLE_SIZE);
                centerBoundsOnPoint(newCenterX, newCenterY);
            }
        }
    }

    //****************************************************************
    // Serialization
    //****************************************************************
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        installHandleEventHandlers();
    }
    // double scale=-1;
//        public void paint(PPaintContext aPaintContext) {
//            super.paint(aPaintContext);
//////            double newscale=aPaintContext.getScale();
//////            if (scale!=newscale) {
//////                double xxx=(double)(DEFAULT_HANDLE_SIZE/newscale);
//////                this.scale(1/aPaintContext.getScale());
//////            }
////
////
////
////            double newscale=aPaintContext.getScale();
////            if (scale!=newscale) {
////
////
////                double xxx=(double)(DEFAULT_HANDLE_SIZE/newscale);
////log.fatal("Breite:"+xxx+ "(x,y): ("+(double)locator.locateX()+","+(double)locator.locateY()+")");
////                setBounds((double)locator.locateX()-(xxx/2),(double)locator.locateY()+(xxx/2), xxx,xxx);
////                //setPathToEllipse((double)0, (double)0, xxx,xxx);
////
////                //centerBoundsOnPoint((double)locator.locateX(), (double)locator.locateY());
////                scale=newscale;
////            }
//        }
//
//      	public void setPathToEllipse(double x, double y, double width, double height) {
//		TEMP_ELLIPSE.setFrame(x, y, width, height);
//		setPathTo(TEMP_ELLIPSE);
//	}
//	//private static final Ellipse2D.Double TEMP_ELLIPSE = new Ellipse2D.Double();
//	private static final Rectangle2D.Double TEMP_ELLIPSE = new Rectangle2D.Double();
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            setPaint(DEFAULT_SELECTED_COLOR);
        } else {
            setPaint(DEFAULT_COLOR);
        }
        repaint();
    }
}
