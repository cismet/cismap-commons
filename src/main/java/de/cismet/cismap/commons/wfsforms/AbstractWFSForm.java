/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.wfsforms;

import com.vividsolutions.jts.geom.Point;

import org.jdom.Element;

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

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.FixedPImage;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class AbstractWFSForm extends JPanel {

    //~ Static fields/initializers ---------------------------------------------

    public static final int FEATURE_BORDER = 200;

    //~ Instance fields --------------------------------------------------------

    protected HashMap<String, JComponent> listComponents = new HashMap<String, JComponent>();
    protected HashMap<String, WFSFormQuery> queriesByComponentName = new HashMap<String, WFSFormQuery>();
    protected ImageIcon mark = new javax.swing.ImageIcon(getClass().getResource(
                "/de/cismet/cismap/commons/gui/res/markPoint.png")); // NOI18N
    protected FixedPImage pMark = new FixedPImage(mark.getImage());
    protected MappingComponent mappingComponent;
    Vector<ActionListener> actionListener = new Vector<ActionListener>();

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Vector<WFSFormQuery> queries = new Vector<WFSFormQuery>();
    private final String loadingMessage = org.openide.util.NbBundle.getMessage(
            AbstractWFSForm.class,
            "AbstractWFSForm.loadingMessage");            // NOI18N
    private final String errorMessage = org.openide.util.NbBundle.getMessage(
            WFSFormsListAndComboBoxModel.class,
            "WFSFormsListAndComboBoxModel.errorMessage"); // NOI18N
    private WFSFormFeature lastFeature = null;
    private String title;
    private String id;
    private String menuString;
    private Icon icon;
    private String iconPath;
    private String className;
    private boolean inited = false;
    private String sorter = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractWFSForm object.
     */
    public AbstractWFSForm() {
        addHierarchyListener(new HierarchyListener() {

                @Override
                public void hierarchyChanged(final HierarchyEvent e) {
                    if (!isInited() && isDisplayable()) {
                        initWFSForm();
                    }
                }
            });
        pMark.setVisible(false);
        pMark.setSweetSpotX(0.5d);
        pMark.setSweetSpotY(1d);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JComponent getListComponentByName(final String name) {
        return listComponents.get(name);
    }

    /**
     * DOCUMENT ME!
     */
    public void initWFSForm() {
        try {
            inited = true;
            // do the initial loading of all queries that are INITIAL
            for (final WFSFormQuery q : queries) {
                if (log.isDebugEnabled()) {
                    log.debug(title + "Components:" + listComponents); // NOI18N
                }
                queriesByComponentName.put(q.getComponentName(), q);
                if (q.getType().equals(WFSFormQuery.INITIAL) && listComponents.containsKey(q.getComponentName())) {
                    final JComponent c = listComponents.get(q.getComponentName());
                    if (log.isDebugEnabled()) {
                        log.debug("Comp: " + q.getComponentName());    // NOI18N
                    }
                    if (c instanceof JComboBox) {
                        try {
                            final JProgressBar bar = (JProgressBar)listComponents.get(q.getComponentName()
                                            + "Progress");             // NOI18N
                            final WFSFormsListAndComboBoxModel w = new WFSFormsListAndComboBoxModel(q, c, bar);
                            w.addActionListener(new ActionListener() {

                                    @Override
                                    public void actionPerformed(final ActionEvent e) {
                                        fireActionPerformed(e);
                                    }
                                });
                            ((JComboBox)c).setModel(w);
                        } catch (Exception ex) {
                            log.error("Error in initWFSForm", ex); // NOI18N
                        }
                    } else if (c instanceof JList) {
                        try {
                            final JProgressBar bar = (JProgressBar)listComponents.get(q.getComponentName()
                                            + "Progress");         // NOI18N
                            final WFSFormsListAndComboBoxModel w = new WFSFormsListAndComboBoxModel(q, c, bar);
                            w.addActionListener(new ActionListener() {

                                    @Override
                                    public void actionPerformed(final ActionEvent e) {
                                        fireActionPerformed(e);
                                    }
                                });
                            ((JList)c).setModel(w);
                        } catch (Exception ex) {
                            log.error("Error in initWFSForm", ex); // NOI18N
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error during initWFSForm", e);              // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  combo  DOCUMENT ME!
     */
    protected void checkCboCorrectness(final JComboBox combo) {
        final String itemString = String.valueOf(combo.getSelectedItem()).trim();
        if ((combo.getSelectedItem() != null) && !itemString.equals("") && !itemString.equals(loadingMessage)
                    && !itemString.equals(errorMessage)
                    && (combo.getSelectedIndex() == -1)) { // NOI18N
            combo.getEditor().getEditorComponent().setBackground(Color.red);
            garbageDuringAutoCompletion(combo);
        } else {
            combo.getEditor().getEditorComponent().setBackground(Color.white);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  box  DOCUMENT ME!
     */
    public abstract void garbageDuringAutoCompletion(JComboBox box);

    /**
     * DOCUMENT ME!
     *
     * @param  component        DOCUMENT ME!
     * @param  replacingValues  DOCUMENT ME!
     */
    public void requestRefresh(final String component, final HashMap<String, String> replacingValues) {
        if (log.isDebugEnabled()) {
            log.debug("requestRefresh: Queries=" + queries);                                                          // NOI18N
        }
        for (final WFSFormQuery q : queries) {
            if (component.equals(q.getComponentName()) && listComponents.containsKey(q.getComponentName())) {
                final JComponent c = listComponents.get(q.getComponentName());
                if (log.isDebugEnabled()) {
                    log.debug("requestRefresh JComponent=" + c);                                                      // NOI18N
                }
                if (c instanceof JComboBox) {
                    try {
                        final JProgressBar bar = (JProgressBar)listComponents.get(q.getComponentName() + "Progress"); // NOI18N
                        final WFSFormsListAndComboBoxModel model = new WFSFormsListAndComboBoxModel(
                                q,
                                replacingValues,
                                c,
                                bar);
                        model.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(final ActionEvent e) {
                                    fireActionPerformed(e);
                                }
                            });
                        ((JComboBox)c).setModel(model);
                    } catch (Exception ex) {
                        log.error("Error in requestRefresh", ex); // NOI18N
                    }
                } else if (c instanceof JList) {
                    try {
                        final JProgressBar bar = (JProgressBar)listComponents.get(q.getComponentName() + "Progress"); // NOI18N
                        final WFSFormsListAndComboBoxModel model = new WFSFormsListAndComboBoxModel(
                                q,
                                replacingValues,
                                c,
                                bar);
                        model.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(final ActionEvent e) {
                                    fireActionPerformed(e);
                                }
                            });
                        ((JList)c).setModel(model);
                    } catch (Exception ex) {
                        log.error("Error in requestRefresh", ex); // NOI18N
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Element getElement() {
        final Element ret = new Element("wfsForm");    // NOI18N
        ret.setAttribute("id", getId());               // NOI18N
        ret.setAttribute("title", getTitle());         // NOI18N
        ret.setAttribute("icon", getIconPath());       // NOI18N
        ret.setAttribute("className", getClassName()); // NOI18N
        ret.setAttribute("menu", getMenuString());     // NOI18N
        for (final WFSFormQuery query : queries) {
            ret.addContent(query.getElement());
        }
        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  component  DOCUMENT ME!
     * @param  value      DOCUMENT ME!
     */
    public void requestRefresh(final String component, final WFSFormFeature value) {
        if (log.isDebugEnabled()) {
            log.debug("requestRefresh(+" + component + "," + value + ")"); // NOI18N
        }
        if ((lastFeature == null) || !(value.getIdentifier().equals(lastFeature.getIdentifier()))) {
            lastFeature = value;
            final WFSFormQuery q = queriesByComponentName.get(component);
            if (q != null) {
                final HashMap<String, String> hm = new HashMap<String, String>();
                hm.put(q.getQueryPlaceholder(), value.getIdentifier());
                requestRefresh(component, hm);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature     DOCUMENT ME!
     * @param  showMarker  DOCUMENT ME!
     */
    public void visualizePosition(final WFSFormFeature feature, final boolean showMarker) {
        mappingComponent.getHighlightingLayer().removeAllChildren();
        mappingComponent.getHighlightingLayer().addChild(pMark);
        mappingComponent.addStickyNode(pMark);
        final Point p = feature.getPosition();
        final double x = mappingComponent.getWtst().getScreenX(p.getCoordinate().x);
        final double y = mappingComponent.getWtst().getScreenY(p.getCoordinate().y);
        pMark.setOffset(x, y);
        pMark.setVisible(showMarker);
        mappingComponent.rescaleStickyNodes();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Vector<WFSFormQuery> getQueries() {
        return queries;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  queries  DOCUMENT ME!
     */
    public void setQueries(final Vector<WFSFormQuery> queries) {
        this.queries = queries;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getTitle() {
        return title;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  title  DOCUMENT ME!
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getId() {
        return id;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  id  DOCUMENT ME!
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getMenuString() {
        return menuString;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  menuString  DOCUMENT ME!
     */
    public void setMenuString(final String menuString) {
        this.menuString = menuString;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  icon  DOCUMENT ME!
     */
    public void setIcon(final Icon icon) {
        this.icon = icon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isInited() {
        return inited;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getIconPath() {
        return iconPath;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  iconPath  DOCUMENT ME!
     */
    public void setIconPath(final String iconPath) {
        this.iconPath = iconPath;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getClassName() {
        return className;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  className  DOCUMENT ME!
     */
    public void setClassName(final String className) {
        this.className = className;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MappingComponent getMappingComponent() {
        return mappingComponent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  mappingComponent  DOCUMENT ME!
     */
    public void setMappingComponent(final MappingComponent mappingComponent) {
        this.mappingComponent = mappingComponent;
        mappingComponent.addStickyNode(pMark);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  a  DOCUMENT ME!
     */
    public void addActionListener(final ActionListener a) {
        actionListener.add(a);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  a  DOCUMENT ME!
     */
    public void removeActionListener(final ActionListener a) {
        actionListener.remove(a);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    public void fireActionPerformed(final ActionEvent e) {
        for (final ActionListener a : actionListener) {
            a.actionPerformed(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getSorter() {
        return sorter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sorter  DOCUMENT ME!
     */
    public void setSorter(final String sorter) {
        this.sorter = sorter;
    }
}
