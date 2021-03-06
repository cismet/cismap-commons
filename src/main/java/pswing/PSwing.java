/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 */
package pswing;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import javax.swing.*;

/*
This message was sent to Sun on August 27, 1999

-----------------------------------------------

We are currently developing Jazz, a "scenegraph" for use in 2D graphics.
One of our ultimate goals is to support Swing lightweight components
within Jazz, whose graphical space supports arbitray affine transforms.
The challenge in this pursuit is getting the components to respond and
render properly though not actually displayed in a standard Java component
hierarchy.


The first issues involved making the Swing components focusable and
showing.  This was accomplished by adding the Swing components to a 0x0
JComponent which was in turn added to our main Jazz application component.
To our good fortune, a Java component is showing merely if it and its
ancestors are showing and not based on whether it is ACTUALLY visible.
Likewise, focus in a JComponent depends merely on the component's
containing window having focus.


The second issue involved capturing the repaint calls on a Swing
component.  Normally, for a repaint and the consequent call to
paintImmediately, a Swing component obtains the Graphics object necessary
to render itself through the Java component heirarchy.  However, for Jazz
we would like the component to render using a Graphics object that Jazz
may have arbitrarily transformed in some way.  By capturing in the
RepaintManager the repaint calls made on our special Swing components, we
are able to redirect the repaint requests through the Jazz architecture to
put the Graphics in its proper context.  Unfortunately, this means that
if the Swing component contains other Swing components, then any repaint
requests made by one of these nested components must go through
the Jazz architecture then through the top level Swing component
down to the nested Swing component.  This normally doesn't cause a
problem.  However, if calling paint on one of these nested
children causes a call to repaint then an infinite loop ensues.  This does
in fact happen in the Swing components that use cell renderers.  Before
the cell renderer is painted, it is invalidated and consequently
repainted.  We solved this problem by putting a lock on repaint calls for
a component while that component is painting.  (A similar problem faced
the Swing team over this same issue.  They solved it by inserting a
CellRendererPane to capture the renderer's invalidate calls.)


Another issue arose over the forwarding of mouse events to the Swing
components.  Since our Swing components are not actually displayed on
screen in the standard manner, we must manually dispatch any MouseEvents
we want the component to receive.  Hence, we needed to find the deepest
visible component at a particular location that accepts MouseEvents.
Finding the deepest visible component at a point was achieved with the
"findComponentAt" method in java.awt.Container.  With the
"getListeners(Class listenerType)" method added in JDK1.3 Beta we are able
to determine if the component has any Mouse Listeners. However, we haven't
yet found a way to determine if MouseEvents have been specifically enabled
for a component. The package private method "eventEnabled" in
java.awt.Component does exactly what we want but is, of course,
inaccessible.  In order to dispatch events correctly we would need a
public accessor to the method "boolean eventEnabled(AWTEvent)" in
java.awt.Component.


Still another issue involves the management of cursors when the mouse is
over a Swing component in our application.  To the Java mechanisms, the
mouse never appears to enter the bounds of the Swing components since they
are contained by a 0x0 JComponent.  Hence, we must manually change the
cursor when the mouse enters one of the Swing components in our
application. This generally works but becomes a problem if the Swing
component's cursor changes while we are over that Swing component (for
instance, if you resize a Table Column).  In order to manage cursors
properly, we would need setCursor to fire property change events.


With the above fixes, most Swing components work.  The only Swing
components that are definitely broken are ToolTips and those that rely on
JPopupMenu. In order to implement ToolTips properly, we would need to have
a method in ToolTipManager that allows us to set the current manager, as
is possible with RepaintManager.  In order to implement JPopupMenu, we
will likely need to reimplement JPopupMenu to function in Jazz with
a transformed Graphics and to insert itself in the proper place in the
Jazz scenegraph.

 */
/**
 * <b>ZSwing</b> is a Visual Component wrapper used to add Swing Components to a Jazz ZCanvas.
 *
 * <P>Example: adding a swing JButton to a ZCanvas:</P>
 *
 * <pre>
ZCanvas canvas = new ZCanvas();
JButton button = new JButton("Button");
swing = new ZSwing(canvas, button);
leaf = new ZVisualLeaf(swing);
canvas.getLayer().addChild(leaf);


NOTE: ZSwing has the current limitation that it does not listen for
Container events.  This is only an issue if you create a ZSwing
and later add Swing components to the ZSwing's component hierarchy
that do not have double buffering turned off or have a smaller font
size than the minimum font size of the original ZSwing's component
hierarchy.

For instance, the following bit of code will give unexpected
results:

JPanel panel = new JPanel();
ZSwing swing = new ZSwing(panel);
JPanel newChild = new JPanel();
newChild.setDoubleBuffered(true);
panel.add(newChild);
 *       </pre>
 *
 * <p>NOTE: ZSwing is not properly ZSerializable, but it is java.io.Serializable.</p>
 *
 * <P><b>Warning:</b> Serialized and ZSerialized objects of this class will not be compatible with future Jazz releases.
 * The current serialization support is appropriate for short term storage or RMI between applications running the same
 * version of Jazz. A future release of Jazz will provide support for long term persistence.</P>
 *
 * @author   Benjamin B. Bederson
 * @author   Lance E. Good
 * @version  $Revision$, $Date$
 */
public class PSwing extends PNode implements Serializable, PropertyChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    /** Used as a hashtable key for this object in the Swing component's client properties. */
    public static final String VISUAL_COMPONENT_KEY = "ZSwing";                            // NOI18N
    private static final AffineTransform IDENTITY = new AffineTransform();
    private static PBounds TEMP_REPAINT_BOUNDS2 = new PBounds();

    //~ Instance fields --------------------------------------------------------

    /** The cutoff at which the Swing component is rendered greek. */
    protected double renderCutoff = 0.3;
    /** The Swing component that this Visual Component wraps. */
    protected JComponent component = null;
    /** The minimum font size in the Swing hierarchy rooted at the component. */
    protected double minFontSize = Double.MAX_VALUE;
    /** The default stroke. */
    protected transient Stroke defaultStroke = new BasicStroke();
    /** The default font. */
    protected Font defaultFont = new Font("Serif", Font.PLAIN, 12); // NOI18N

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private BufferedImage buffer;
    private PSwingCanvas PSwingCanvas;

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructs a new visual component wrapper for the Swing component and adds the Swing component to the
     * SwingWrapper component of the ZCanvas.
     *
     * @param  zbc        The ZCanvas to which the Swing component will be added
     * @param  component  The swing component to be wrapped
     */
    public PSwing(final PSwingCanvas zbc, final JComponent component) {
        this.PSwingCanvas = zbc;
        this.component = component;
        component.putClientProperty(VISUAL_COMPONENT_KEY, this);
        init(component);
        zbc.getSwingWrapper().add(component);
        component.revalidate();
        reshape();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void reshape() {
        component.setBounds(0, 0, component.getPreferredSize().width, component.getPreferredSize().height);
        setBounds(0, 0, component.getPreferredSize().width, component.getPreferredSize().height);
    }

    /**
     * Determines if the Swing component should be rendered normally or as a filled rectangle.
     *
     * <p>The transform, clip, and composite will be set appropriately when this object is rendered. It is up to this
     * object to restore the transform, clip, and composite of the Graphics2D if this node changes any of them. However,
     * the color, font, and stroke are unspecified by Jazz. This object should set those things if they are used, but
     * they do not need to be restored.</p>
     *
     * @param  renderContext  Contains information about current render.
     */
    @Override
    public void paint(final PPaintContext renderContext) {
        final Graphics2D g2 = renderContext.getGraphics();

        if (defaultStroke == null) {
            defaultStroke = new BasicStroke();
        }
        g2.setStroke(defaultStroke);

        if (defaultFont == null) {
            defaultFont = new Font("Serif", Font.PLAIN, 12); // NOI18N
        }

        g2.setFont(defaultFont);

        if (component.getParent() == null) {
            PSwingCanvas.getSwingWrapper().add(component);
            component.revalidate();
        }

        if (((getCompositeMagnification(renderContext) < renderCutoff) && PSwingCanvas.getInteracting())
                    || ((minFontSize * getCompositeMagnification(renderContext)) < 0.5)) {
            paintAsGreek(g2);
        } else {
            paint(g2);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pPaintContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getCompositeMagnification(final PPaintContext pPaintContext) {
        // renderContext.getCompositeMagnification()
        return pPaintContext.getScale();
    }

    /**
     * Paints the Swing component as greek.
     *
     * @param  g2  The graphics used to render the filled rectangle
     */
    public void paintAsGreek(final Graphics2D g2) {
        final Color background = component.getBackground();
        final Color foreground = component.getForeground();
        final Rectangle2D rect = getBounds();

        if (background != null) {
            g2.setColor(background);
        }
        g2.fill(rect);

        if (foreground != null) {
            g2.setColor(foreground);
        }
        g2.draw(rect);
    }

    /**
     * Forwards the paint request to the Swing component to paint normally.
     *
     * @param  g2  The graphics this visual component should pass to the Swing component
     */
    public void paint(final Graphics2D g2) {
        boolean wrongRepaintManager = false;
        PSwingCanvas.ZBasicRepaintManager manager = null;
        try {
            manager = (PSwingCanvas.ZBasicRepaintManager)RepaintManager.currentManager(component);
        } catch (Exception e) {
            log.warn("Repaint Problem", e);
            wrongRepaintManager = true;
        }
        if ((buffer == null) || (buffer.getWidth() != component.getWidth())
                    || (buffer.getHeight() != component.getHeight())) {
            buffer = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);
        } else {
            final Graphics2D bufferedGraphics = buffer.createGraphics();
            bufferedGraphics.setBackground(Color.black);
            bufferedGraphics.clipRect(0, 0, component.getWidth(), component.getHeight());
        }
        final Graphics2D bufferedGraphics = buffer.createGraphics();
//        bufferedGraphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        component.paint(bufferedGraphics);
//        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        final Object origHint = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawRenderedImage(buffer, IDENTITY);
        if (origHint != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, origHint);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        }
        if (!wrongRepaintManager) {
            manager.unlockRepaint(component);
        }
    }

    // todo enable region repainting.
    /**
     * Repaints the specified portion of this visual component Note that the input parameter may be modified as a result
     * of this call.
     *
     * @param  repaintBounds  DOCUMENT ME!
     */
    public void repaint(final PBounds repaintBounds) {
//        AffineTransform viewTransform = zCanvas.getCamera().getViewTransform();
//        AffineTransform totalTransform=getTransform();
//        System.out.println( "viewTransform = " + viewTransform );
        final Shape sh = getTransform().createTransformedShape(repaintBounds);
        TEMP_REPAINT_BOUNDS2.setRect(sh.getBounds2D());
        repaintFrom(TEMP_REPAINT_BOUNDS2, this);
        // repaint();
// PNode parent = getParent();
// if( parent != null ) {
////            parent.repaintFrom( repaintBounds, this );
////            parent.repaintFrom( repaintBounds, parent );
//            repaint();
//        }
//        else {
//            repaint();
//        }

        // original code
// PNode[] parentsRef = getParentsReference();
// int numParents = getNumParents();
//
// for( int i = 0; i < numParents; i++ ) {
// if( i == numParents - 1 ) {
// parentsRef[i].repaint( repaintBounds );
// }
// else {
// parentsRef[i].repaint( (ZBounds)repaintBounds.clone() );
// }
// }
    }

    /**
     * Sets the Swing component's bounds to its preferred bounds unless it already is set to its preferred size. Also
     * updates the visual components copy of these bounds
     */
    public void computeBounds() {
        final Dimension d = component.getPreferredSize();
        getBoundsReference().setRect(0, 0, d.getWidth(), d.getHeight());
        if (!component.getSize().equals(d)) {
            component.setBounds(0, 0, (int)d.getWidth(), (int)d.getHeight());
        }
    }

    /**
     * Returns the Swing component that this visual component wraps.
     *
     * @return  The Swing component that this visual component wraps
     */
    public JComponent getComponent() {
        return component;
    }

    /**
     * We need to turn off double buffering of Swing components within Jazz since all components contained within a
     * native container use the same buffer for double buffering. With normal Swing widgets this is fine, but for Swing
     * components within Jazz this causes problems. This function recurses the component tree rooted at c, and turns off
     * any double buffering in use. It also updates the minimum font size based on the font size of c and adds a
     * property change listener to listen for changes to the font.
     *
     * @param  c  The Component to be recursively unDoubleBuffered
     */
    void init(final Component c) {
        Component[] children = null;
        if (c instanceof Container) {
            children = ((Container)c).getComponents();
        }

        if (c.getFont() != null) {
            minFontSize = Math.min(minFontSize, c.getFont().getSize());
        }

        if (children != null) {
            for (int j = 0; j < children.length; j++) {
                init(children[j]);
            }
        }

        if (c instanceof JComponent) {
            ((JComponent)c).setDoubleBuffered(false);
            c.addPropertyChangeListener("font", this); // NOI18N
        }
    }

    /**
     * Listens for changes in font on components rooted at this ZSwing.
     *
     * @param  evt  DOCUMENT ME!
     */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (component.isAncestorOf((Component)evt.getSource())
                    && (((Component)evt.getSource()).getFont() != null)) {
            minFontSize = Math.min(minFontSize, ((Component)evt.getSource()).getFont().getSize());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   in  DOCUMENT ME!
     *
     * @throws  IOException             DOCUMENT ME!
     * @throws  ClassNotFoundException  DOCUMENT ME!
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init(component);
    }
}
