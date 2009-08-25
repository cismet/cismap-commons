/*
 * CustomFeatureActionListener.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 4. September 2006, 16:03
 *
 */
package de.cismet.cismap.commons.gui.piccolo.eventlistener;

import de.cismet.cismap.commons.features.InputEventAwareFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.tools.PFeatureTools;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import java.util.LinkedList;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class CustomFeatureActionListener extends PBasicInputEventHandler {
    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("de.cismet.cismap.commons.tools.PFeatureTools");
    MappingComponent mappingComponent;
    Class[] validClasses = {PFeature.class};

    /**
     * Creates a new instance of CustomFeatureActionListener
     */
    public CustomFeatureActionListener(MappingComponent mappingComponent) {
        this.mappingComponent = mappingComponent;
    }

//    public void mouseWheelRotatedByBlock(PInputEvent event) {
//        super.mouseWheelRotatedByBlock(event);
//        LinkedList l=PFeatureTools.getAllValidObjectsUnderPointer(event,validClasses);
//        for (Object o:l) {
//            if (o instanceof PFeature && ((PFeature)o).getFeature() instanceof InputEventAwareFeature) {
//                ((InputEventAwareFeature)((PFeature)o).getFeature()).mouseWheelRotatedByBlock(event);
//                if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
//                    break;
//                }
//            }
//        }
//    }
//    public void keyPressed(PInputEvent event) {
//        super.keyPressed(event);
//        LinkedList l=PFeatureTools.getAllValidObjectsUnderPointer(event,validClasses);
//        for (Object o:l) {
//            if (o instanceof PFeature && ((PFeature)o).getFeature() instanceof InputEventAwareFeature) {
//                ((InputEventAwareFeature)((PFeature)o).getFeature()).keyPressed(event);
//                if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
//                    break;
//                }
//            }
//        }
//    }
//
//    public void keyReleased(PInputEvent event) {
//        super.keyReleased(event);
//        LinkedList l=PFeatureTools.getAllValidObjectsUnderPointer(event,validClasses);
//        for (Object o:l) {
//            if (o instanceof PFeature && ((PFeature)o).getFeature() instanceof InputEventAwareFeature) {
//                ((InputEventAwareFeature)((PFeature)o).getFeature()).keyReleased(event);
//                if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
//                    break;
//                }
//            }
//        }
//    }
//
//    public void keyTyped(PInputEvent event) {
//        super.keyTyped(event);
//        LinkedList l=PFeatureTools.getAllValidObjectsUnderPointer(event,validClasses);
//        for (Object o:l) {
//            if (o instanceof PFeature && ((PFeature)o).getFeature() instanceof InputEventAwareFeature) {
//                ((InputEventAwareFeature)((PFeature)o).getFeature()).keyTyped(event);
//                if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
//                    break;
//                }
//            }
//        }
//    }
//
//    public void keyboardFocusGained(PInputEvent event) {
//        super.keyboardFocusGained(event);
//        LinkedList l=PFeatureTools.getAllValidObjectsUnderPointer(event,validClasses);
//        for (Object o:l) {
//            if (o instanceof PFeature && ((PFeature)o).getFeature() instanceof InputEventAwareFeature) {
//                ((InputEventAwareFeature)((PFeature)o).getFeature()).keyboardFocusGained(event);
//                if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
//                    break;
//                }
//            }
//        }
//    }
//
//    public void keyboardFocusLost(PInputEvent event) {
//        super.keyboardFocusLost(event);
//        LinkedList l=PFeatureTools.getAllValidObjectsUnderPointer(event,validClasses);
//        for (Object o:l) {
//            if (o instanceof PFeature && ((PFeature)o).getFeature() instanceof InputEventAwareFeature) {
//                ((InputEventAwareFeature)((PFeature)o).getFeature()).keyboardFocusLost(event);
//                if (((InputEventAwareFeature)((PFeature)o).getFeature()).noFurtherEventProcessing(event)) {
//                    break;
//                }
//            }
//        }
//    }
    
    @Override
    public void mouseClicked(final PInputEvent event) {
        super.mouseClicked(event);
        Thread t = new Thread() {
            public void run() {
                LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                for (Object o : l) {
                    if (o instanceof PFeature && ((PFeature) o).getFeature() instanceof InputEventAwareFeature) {
                        ((InputEventAwareFeature) ((PFeature) o).getFeature()).mouseClicked(event);
                        if (((InputEventAwareFeature) ((PFeature) o).getFeature()).noFurtherEventProcessing(event)) {
                            break;
                        }
                    }
                }
            }
        };
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    @Override
    public void mouseDragged(final PInputEvent event) {
        super.mouseDragged(event);
        Thread t = new Thread() {
            public void run() {
                LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                for (Object o : l) {
                    if (o instanceof PFeature && ((PFeature) o).getFeature() instanceof InputEventAwareFeature) {
                        ((InputEventAwareFeature) ((PFeature) o).getFeature()).mouseDragged(event);
                        if (((InputEventAwareFeature) ((PFeature) o).getFeature()).noFurtherEventProcessing(event)) {
                            break;
                        }
                    }
                }
            }
        };
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    @Override
    public void mouseEntered(final PInputEvent event) {
        super.mouseEntered(event);
        Thread t = new Thread() {
            public void run() {
                LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                for (Object o : l) {
                    if (o instanceof PFeature && ((PFeature) o).getFeature() instanceof InputEventAwareFeature) {
                        ((InputEventAwareFeature) ((PFeature) o).getFeature()).mouseEntered(event);
                        if (((InputEventAwareFeature) ((PFeature) o).getFeature()).noFurtherEventProcessing(event)) {
                            break;
                        }
                    }
                }
            }
        };
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    @Override
    public void mouseExited(final PInputEvent event) {
        super.mouseExited(event);
        Thread t = new Thread() {
            public void run() {
                LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                for (Object o : l) {
                    if (o instanceof PFeature && ((PFeature) o).getFeature() instanceof InputEventAwareFeature) {
                        ((InputEventAwareFeature) ((PFeature) o).getFeature()).mouseExited(event);
                        if (((InputEventAwareFeature) ((PFeature) o).getFeature()).noFurtherEventProcessing(event)) {
                            break;
                        }
                    }
                }
            }
        };
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    @Override
    public void mouseMoved(final PInputEvent event) {
        super.mouseMoved(event);
        Thread t = new Thread() {
            public void run() {
                LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                for (Object o : l) {
                    if (o instanceof PFeature && ((PFeature) o).getFeature() instanceof InputEventAwareFeature) {
                        ((InputEventAwareFeature) ((PFeature) o).getFeature()).mouseMoved(event);
                        if (((InputEventAwareFeature) ((PFeature) o).getFeature()).noFurtherEventProcessing(event)) {
                            break;
                        }
                    }
                }
            }
        };
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    @Override
    public void mousePressed(final PInputEvent event) {
        super.mousePressed(event);
        Thread t = new Thread() {
            public void run() {
                LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                for (Object o : l) {
                    if (o instanceof PFeature && ((PFeature) o).getFeature() instanceof InputEventAwareFeature) {
                        ((InputEventAwareFeature) ((PFeature) o).getFeature()).mousePressed(event);
                        if (((InputEventAwareFeature) ((PFeature) o).getFeature()).noFurtherEventProcessing(event)) {
                            break;
                        }
                    }
                }
            }
        };
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    @Override
    public void mouseReleased(final PInputEvent event) {
        super.mouseReleased(event);
        Thread t = new Thread() {
            public void run() {
                LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                for (Object o : l) {
                    if (o instanceof PFeature && ((PFeature) o).getFeature() instanceof InputEventAwareFeature) {
                        ((InputEventAwareFeature) ((PFeature) o).getFeature()).mouseReleased(event);
                        if (((InputEventAwareFeature) ((PFeature) o).getFeature()).noFurtherEventProcessing(event)) {
                            break;
                        }
                    }
                }
            }
        };
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    @Override
    public void mouseWheelRotated(final PInputEvent event) {
        super.mouseWheelRotated(event);
        Thread t = new Thread() {
            public void run() {
                LinkedList l = PFeatureTools.getAllValidObjectsUnderPointer(event, validClasses);
                for (Object o : l) {
                    if (o instanceof PFeature && ((PFeature) o).getFeature() instanceof InputEventAwareFeature) {
                        ((InputEventAwareFeature) ((PFeature) o).getFeature()).mouseWheelRotated(event);
                        if (((InputEventAwareFeature) ((PFeature) o).getFeature()).noFurtherEventProcessing(event)) {
                            break;
                        }
                    }
                }
            }
        };
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
}
