/*
 * AbstractWFSForm.java
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
 * Created on 25. Juli 2006, 17:26
 *
 *
 *
 * 
 */
package de.cismet.cismap.commons.wfsforms;

import com.vividsolutions.jts.geom.Point;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FixedPImage;
import de.cismet.cismap.commons.interaction.CismapBroker;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public abstract class AbstractWFSForm extends JPanel {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Vector<WFSFormQuery> queries = new Vector<WFSFormQuery>();
    protected HashMap<String, JComponent> listComponents = new HashMap<String, JComponent>();
    protected HashMap<String, WFSFormQuery> queriesByComponentName = new HashMap<String, WFSFormQuery>();
    private String loadingMessage = java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("WFSFormListAndComboBoxModel.laden");
    private WFSFormFeature lastFeature = null;
    protected ImageIcon mark = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/markPoint.png"));
    protected FixedPImage pMark = new FixedPImage(mark.getImage());
    private String title;
    private String id;
    private String menuString;
    private Icon icon;
    private String iconPath;
    private String className;
    private boolean inited = false;
    private String sorter=null;
    protected MappingComponent mappingComponent;
    Vector<ActionListener> actionListener = new Vector<ActionListener>();
    public static final int FEATURE_BORDER = 200;

    public AbstractWFSForm() {
        addHierarchyListener(new HierarchyListener() {

            public void hierarchyChanged(HierarchyEvent e) {
                if (!isInited() && isDisplayable()) {
                    initWFSForm();
                }
            }
        });
        pMark.setVisible(false);
        pMark.setSweetSpotX(0.5d);
        pMark.setSweetSpotY(1d);



    }

    public JComponent getListComponentByName(String name) {
        return listComponents.get(name);
    }

    public void initWFSForm() {
        try {
            inited = true;
            //do the initial loading of all queries that are INITIAL
            for (final WFSFormQuery q : queries) {
                log.debug(title+"Components:"+ listComponents);
                queriesByComponentName.put(q.getComponentName(), q);
                if (q.getType().equals(WFSFormQuery.INITIAL) && listComponents.containsKey(q.getComponentName())) {
                    final JComponent c = listComponents.get(q.getComponentName());
                    log.debug("Comp: "+q.getComponentName());
                    if (c instanceof JComboBox) {
                        try {
                            JProgressBar bar = (JProgressBar) listComponents.get(q.getComponentName() + "Progress");
                            WFSFormsListAndComboBoxModel w = new WFSFormsListAndComboBoxModel(q, c, bar);
                            w.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent e) {
                                    fireActionPerformed(e);
                                }
                            });
                            ((JComboBox) c).setModel(w);
                        } catch (Exception ex) {
                            log.error("Fehler in initWFSForm", ex);
                        }
                    } else if (c instanceof JList) {
                        try {
                            JProgressBar bar = (JProgressBar) listComponents.get(q.getComponentName() + "Progress");
                            WFSFormsListAndComboBoxModel w = new WFSFormsListAndComboBoxModel(q, c, bar);
                            w.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent e) {
                                    fireActionPerformed(e);
                                }
                            });
                            ((JList) c).setModel(w);
                        } catch (Exception ex) {
                            log.error("Fehler in initWFSForm", ex);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error during initWFSForm", e);
        }
    }

    protected void checkCboCorrectness(JComboBox combo) {
        if (combo.getSelectedItem() != null && !combo.getSelectedItem().toString().trim().equals("") && !combo.getSelectedItem().toString().trim().equals(loadingMessage)&& combo.getSelectedIndex() == -1) {
            combo.getEditor().getEditorComponent().setBackground(Color.red);
            garbageDuringAutoCompletion(combo);
        } else {
            combo.getEditor().getEditorComponent().setBackground(Color.white);
        }
    }

    public abstract void garbageDuringAutoCompletion(JComboBox box);

    public void requestRefresh(String component, HashMap<String, String> replacingValues) {
        log.debug("requestRefresh: Queries=" + queries);
        for (final WFSFormQuery q : queries) {
            if (component.equals(q.getComponentName()) && listComponents.containsKey(q.getComponentName())) {
                final JComponent c = listComponents.get(q.getComponentName());
                log.debug("requestRefresh JComponent=" + c);
                if (c instanceof JComboBox) {
                    try {
                        JProgressBar bar = (JProgressBar) listComponents.get(q.getComponentName() + "Progress");
                        WFSFormsListAndComboBoxModel model = new WFSFormsListAndComboBoxModel(q, replacingValues, c, bar);
                        model.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                fireActionPerformed(e);
                            }
                        });
                        ((JComboBox) c).setModel(model);
                    } catch (Exception ex) {
                        log.error("Fehler in requestRefresh", ex);
                    }
                } else if (c instanceof JList) {
                    try {
                        JProgressBar bar = (JProgressBar) listComponents.get(q.getComponentName() + "Progress");
                        WFSFormsListAndComboBoxModel model = new WFSFormsListAndComboBoxModel(q, replacingValues, c, bar);
                        model.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                fireActionPerformed(e);
                            }
                        });
                        ((JList) c).setModel(model);
                    } catch (Exception ex) {
                        log.error("Fehler in requestRefresh", ex);
                    }
                }
            }
        }
    }

    public Element getElement() {
        Element ret = new Element("wfsForm");
        ret.setAttribute("id", getId());
        ret.setAttribute("title", getTitle());
        ret.setAttribute("icon", getIconPath());
        ret.setAttribute("className", getClassName());
        ret.setAttribute("menu", getMenuString());
        for (WFSFormQuery query : queries) {
            ret.addContent(query.getElement());
        }
        return ret;
    }

    public void requestRefresh(String component, WFSFormFeature value) {
        log.debug("requestRefresh(+" + component + "," + value + ")");
        if (lastFeature == null || !(value.getIdentifier().equals(lastFeature.getIdentifier()))) {
            lastFeature = value;
            WFSFormQuery q = queriesByComponentName.get(component);
            if (q != null) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put(q.getQueryPlaceholder(), value.getIdentifier());
                requestRefresh(component, hm);
            }
        }
    }

    public void visualizePosition(WFSFormFeature feature, boolean showMarker) {
        mappingComponent.getHighlightingLayer().removeAllChildren();
        mappingComponent.getHighlightingLayer().addChild(pMark);
        mappingComponent.addStickyNode(pMark);
        Point p = feature.getPosition();
        double x = mappingComponent.getWtst().getScreenX(p.getCoordinate().x);
        double y = mappingComponent.getWtst().getScreenY(p.getCoordinate().y);
        pMark.setOffset(x, y);
        pMark.setVisible(showMarker);
        mappingComponent.rescaleStickyNodes();
    }

    public Vector<WFSFormQuery> getQueries() {
        return queries;
    }

    public void setQueries(Vector<WFSFormQuery> queries) {
        this.queries = queries;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMenuString() {
        return menuString;
    }

    public void setMenuString(String menuString) {
        this.menuString = menuString;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public boolean isInited() {
        return inited;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public MappingComponent getMappingComponent() {
        return mappingComponent;
    }

    public void setMappingComponent(MappingComponent mappingComponent) {
        this.mappingComponent = mappingComponent;
        mappingComponent.addStickyNode(pMark);
    }

    public void addActionListener(ActionListener a) {
        actionListener.add(a);
    }

    public void removeActionListener(ActionListener a) {
        actionListener.remove(a);
    }

    public void fireActionPerformed(ActionEvent e) {
        for (ActionListener a : actionListener) {
            a.actionPerformed(e);
        }
    }

    public String getSorter() {
        return sorter;
    }

    public void setSorter(String sorter) {
        this.sorter = sorter;
    }
    
}