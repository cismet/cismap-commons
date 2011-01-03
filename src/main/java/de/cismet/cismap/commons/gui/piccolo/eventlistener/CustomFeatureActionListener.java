/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

import java.util.LinkedList;

import de.cismet.cismap.commons.features.InputEventAwareFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;

import de.cismet.tools.CismetThreadPool;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class CustomFeatureActionListener extends PBasicInputEventHandler {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            CustomFeatureActionListener.class); // NOI18N

    //~ Instance fields --------------------------------------------------------

    MappingComponent mappingComponent;
    Class[] validClasses = { PFeature.class };

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of CustomFeatureActionListener.
     *
     * @param  mappingComponent  DOCUMENT ME!
     */
    public CustomFeatureActionListener(final MappingComponent mappingComponent) {
        this.mappingComponent = mappingComponent;
    }

    //~ Methods ----------------------------------------------------------------

// public void mouseWheelRotatedByBlock(PInputEvent event) {
// super.mouseWheelRotatedByBlock(event);
// LinkedList l=PFeatureTools.getAllValidObjectsUnderPointer(event,validClasses);
// for (Object o:l) {
// if (o instanceof PFeature && ((PFeature)o).getFeature() instanceof InputEventAwareFeature) {
// ((InputEventAwareFeature)((PFeature)o).getFeature()).mouseWheelRotatedByBlock(event);
// if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
// break;
// }
// }
// }
// }
// public void keyPressed(PInputEvent event) {
// super.keyPressed(event);
// LinkedList l=PFeatureTools.getAllValidObjectsUnderPointer(event,validClasses);
// for (Object o:l) {
// if (o instanceof PFeature && ((PFeature)o).getFeature() instanceof InputEventAwareFeature) {
// ((InputEventAwareFeature)((PFeature)o).getFeature()).keyPressed(event);
// if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
// break;
// }
// }
// }
// }
//
// public void keyReleased(PInputEvent event) {
// super.keyReleased(event);
// LinkedList l=PFeatureTools.getAllValidObjectsUnderPointer(event,validClasses);
// for (Object o:l) {
// if (o instanceof PFeature && ((PFeature)o).getFeature() instanceof InputEventAwareFeature) {
// ((InputEventAwareFeature)((PFeature)o).getFeature()).keyReleased(event);
// if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
// break;
// }
// }
// }
// }
//
// public void keyTyped(PInputEvent event) {
// super.keyTyped(event);
// LinkedList l=PFeatureTools.getAllValidObjectsUnderPointer(event,validClasses);
// for (Object o:l) {
// if (o instanceof PFeature && ((PFeature)o).getFeature() instanceof InputEventAwareFeature) {
// ((InputEventAwareFeature)((PFeature)o).getFeature()).keyTyped(event);
// if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
// break;
// }
// }
// }
// }
//
// public void keyboardFocusGained(PInputEvent event) {
// super.keyboardFocusGained(event);
// LinkedList l=PFeatureTools.getAllValidObjectsUnderPointer(event,validClasses);
// for (Object o:l) {
// if (o instanceof PFeature && ((PFeature)o).getFeature() instanceof InputEventAwareFeature) {
// ((InputEventAwareFeature)((PFeature)o).getFeature()).keyboardFocusGained(event);
// if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
// break;
// }
// }
// }
// }
//
// public void keyboardFocusLost(PInputEvent event) {
// super.keyboardFocusLost(event);
// LinkedList l=PFeatureTools.getAllValidObjectsUnderPointer(event,validClasses);
// for (Object o:l) {
// if (o instanceof PFeature && ((PFeature)o).getFeature() instanceof InputEventAwareFeature) {
// ((InputEventAwareFeature)((PFeature)o).getFeature()).keyboardFocusLost(event);
// if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
// break;
// }
// }
// }
// }

    @Override
    public void mouseClicked(final PInputEvent event) {
        super.mouseClicked(event);
        final Runnable t = new Runnable() {

                @Override
                public void run() {
                    final LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                    for (final Object o : l) {
                        if ((o instanceof PFeature) && (((PFeature)o).getFeature() instanceof InputEventAwareFeature)) {
                            ((InputEventAwareFeature)((PFeature)o).getFeature()).mouseClicked(event);
                            if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
                                break;
                            }
                        }
                    }
                }
            };
        CismetThreadPool.execute(t);
    }

    @Override
    public void mouseDragged(final PInputEvent event) {
        super.mouseDragged(event);
        final Runnable t = new Runnable() {

                @Override
                public void run() {
                    final LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                    for (final Object o : l) {
                        if ((o instanceof PFeature) && (((PFeature)o).getFeature() instanceof InputEventAwareFeature)) {
                            ((InputEventAwareFeature)((PFeature)o).getFeature()).mouseDragged(event);
                            if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
                                break;
                            }
                        }
                    }
                }
            };
        CismetThreadPool.execute(t);
    }

    @Override
    public void mouseEntered(final PInputEvent event) {
        super.mouseEntered(event);
        final Runnable t = new Runnable() {

                @Override
                public void run() {
                    final LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                    for (final Object o : l) {
                        if ((o instanceof PFeature) && (((PFeature)o).getFeature() instanceof InputEventAwareFeature)) {
                            ((InputEventAwareFeature)((PFeature)o).getFeature()).mouseEntered(event);
                            if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
                                break;
                            }
                        }
                    }
                }
            };
        CismetThreadPool.execute(t);
    }

    @Override
    public void mouseExited(final PInputEvent event) {
        super.mouseExited(event);
        final Runnable t = new Runnable() {

                @Override
                public void run() {
                    final LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                    for (final Object o : l) {
                        if ((o instanceof PFeature) && (((PFeature)o).getFeature() instanceof InputEventAwareFeature)) {
                            ((InputEventAwareFeature)((PFeature)o).getFeature()).mouseExited(event);
                            if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
                                break;
                            }
                        }
                    }
                }
            };
        CismetThreadPool.execute(t);
    }

    @Override
    public void mouseMoved(final PInputEvent event) {
        super.mouseMoved(event);
        final Runnable t = new Runnable() {

                @Override
                public void run() {
                    final LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                    for (final Object o : l) {
                        if ((o instanceof PFeature) && (((PFeature)o).getFeature() instanceof InputEventAwareFeature)) {
                            ((InputEventAwareFeature)((PFeature)o).getFeature()).mouseMoved(event);
                            if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
                                break;
                            }
                        }
                    }
                }
            };
        CismetThreadPool.execute(t);
    }

    @Override
    public void mousePressed(final PInputEvent event) {
        super.mousePressed(event);
        final Runnable t = new Runnable() {

                @Override
                public void run() {
                    final LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                    for (final Object o : l) {
                        if ((o instanceof PFeature) && (((PFeature)o).getFeature() instanceof InputEventAwareFeature)) {
                            ((InputEventAwareFeature)((PFeature)o).getFeature()).mousePressed(event);
                            if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
                                break;
                            }
                        }
                    }
                }
            };
        CismetThreadPool.execute(t);
    }

    @Override
    public void mouseReleased(final PInputEvent event) {
        super.mouseReleased(event);
        final Runnable t = new Runnable() {

                @Override
                public void run() {
                    final LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                    for (final Object o : l) {
                        if ((o instanceof PFeature) && (((PFeature)o).getFeature() instanceof InputEventAwareFeature)) {
                            ((InputEventAwareFeature)((PFeature)o).getFeature()).mouseReleased(event);
                            if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
                                break;
                            }
                        }
                    }
                }
            };
        CismetThreadPool.execute(t);
    }

    @Override
    public void mouseWheelRotated(final PInputEvent event) {
        super.mouseWheelRotated(event);
        final Runnable t = new Runnable() {

                @Override
                public void run() {
                    final LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                    for (final Object o : l) {
                        if ((o instanceof PFeature) && (((PFeature)o).getFeature() instanceof InputEventAwareFeature)) {
                            ((InputEventAwareFeature)((PFeature)o).getFeature()).mouseWheelRotated(event);
                            if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
                                break;
                            }
                        }
                    }
                }
            };
        CismetThreadPool.execute(t);
    }
}
