   /*
 * SimpleUpdateablePostgisFeatureService.java
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
 * Created on 4. September 2006, 16:55
 *
 */
package de.cismet.cismap.commons.featureservice;

import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.features.InputEventAwareFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import edu.umd.cs.piccolo.event.PInputEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdom.CDATA;
import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class SimpleUpdateablePostgisFeatureService extends SimplePostgisFeatureService implements RetrievalServiceLayer {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    String action;
    String actionText;
    ImageIcon icon;
    String iconPath;
    public final static String ID_TOKEN = "<cismap::update::id>";

    /** Creates a new instance of SimpleUpdateablePostgisFeatureService */
    public SimpleUpdateablePostgisFeatureService(Element object) {
        super(object);
        Element actionElement = null;
        try {
            actionElement = object.getChild("action");
            action = actionElement.getText();
        } catch (Exception e) {
            log.warn("No action in updateable Service", e);
        }
        try {
            actionText = actionElement.getAttribute("text").getValue();
            iconPath = actionElement.getAttribute("icon").getValue();
            icon = new ImageIcon(getClass().getResource(iconPath));
        } catch (Exception e) {
            log.warn("No actiontext in updateable Service", e);
            actionText = "Aktion ausf\u00FChren";
        }
    }

    public Element getElement() {
        Element e = super.getElement();
        e.setAttribute("updateable", "true");
        Element actionElement = new Element("action");
        actionElement.setAttribute("text", actionText);
        actionElement.setAttribute("icon", iconPath);
        actionElement.addContent(new CDATA(action));
        e.addContent(actionElement);
        return e;
    }

    public void doAction(String id) throws Exception {
        if (action != null) {
            java.sql.Statement s = connection.createStatement();
            String sql = action.replaceAll(ID_TOKEN, id);
            log.debug("execute: " + sql);
            s.execute(sql);
            s.close();
        }
    }

    public DefaultFeatureServiceFeature getNewFeatureServiceFeature() {
        log.debug("getNewFeatureServiceFeature");
        return new UpdateableFeature();
    }

    private class UpdateableFeature extends DefaultFeatureServiceFeature implements InputEventAwareFeature {

        public boolean noFurtherEventProcessing(PInputEvent event) {
            return true;
        }

        public void mouseClicked(PInputEvent event) {
        }

        public void mouseEntered(PInputEvent event) {
        }

        public void mouseExited(PInputEvent event) {
        }

        public void mousePressed(PInputEvent event) {
            final MappingComponent mappingComponent = (MappingComponent) event.getComponent();
            if (!mappingComponent.isReadOnly() && event.getModifiers() == InputEvent.BUTTON3_MASK) {
                log.debug("try to show menu");
                JPopupMenu pop = new JPopupMenu();
                JMenuItem mni = new JMenuItem(actionText, icon);
                mni.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        try {
                            doAction("" + getId());
                            getFeatureService().retrieve(true);
                        } catch (Exception ex) {
                            log.error("Error during doAction()", ex);
                            ErrorInfo ei = new ErrorInfo("Fehler", "Fehler beim Zugriff auf den FeatureService", null,null, ex, Level.ALL, null);
                            JXErrorPane.showDialog(mappingComponent, ei);

//                            JXErrorDialog.showDialog(mappingComponent,"Fehler","Fehler beim Zugriff auf den FeatureService",ex);
                        }
                    }
                });
                pop.add(mni);
                pop.show(mappingComponent, (int) event.getCanvasPosition().getX(), (int) event.getCanvasPosition().getY());

            }
        }

        public void mouseWheelRotated(PInputEvent event) {
        }

        public void mouseReleased(PInputEvent event) {
        }

        public void mouseMoved(PInputEvent event) {
        }

        public void mouseDragged(PInputEvent event) {
        }
    }
}
