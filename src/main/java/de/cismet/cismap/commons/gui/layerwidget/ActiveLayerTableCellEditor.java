/*
 * ActiveLayerTableCellEditor.java
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
 * Created on 24. November 2005, 10:43
 *
 */
package de.cismet.cismap.commons.gui.layerwidget;

import de.cismet.cismap.commons.Debug;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.wms.capabilities.Style;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.QueryEditorDialog;
import de.cismet.cismap.commons.featureservice.FeatureServiceUtilities;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.featureservice.style.StyleDialog;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.events.ActiveLayerEvent;
import de.cismet.cismap.commons.raster.wms.WMSLayer;
import de.cismet.cismap.commons.raster.wms.WMSServiceLayer;
import de.cismet.cismap.commons.wfs.WFSFacade;
import de.cismet.tools.CismetThreadPool;
import de.cismet.tools.gui.StaticSwingTools;
import edu.umd.cs.piccolo.PNode;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellEditor;
import org.jdom.Element;


/**
 *
 * @author thorsten.hell@cismet.de
 */
public class ActiveLayerTableCellEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor, PropertyChangeListener
{
  protected final static boolean DEBUG = Debug.DEBUG;
  private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
  private JCheckBox informationBox;
  private Object value;
  private JTable table;
  private JComboBox cbbStyleChooser;
  private StyleDialog styleDialog;// = new StyleDialog(new JFrame("XXX"), true);
  private JButton moreButton = new JButton(". .");//NOI18N
  private JButton wfsStyleButton = new JButton()
  {
    // paints the rectangle inside the button that creates the StyleDialog
    @Override
    protected void paintComponent(Graphics g)
    {
      de.cismet.cismap.commons.featureservice.style.Style style = ((AbstractFeatureService) value).getLayerProperties().getStyle();
      try
      {
        Graphics2D g2d = (Graphics2D) g;
        if (style.isDrawFill() && style.getFillColor() != null)
        {
          g2d.setColor((Color) style.getFillColor());
          g2d.fillRect(10, 4, getWidth() - 20, getHeight() - 8);
        }
        if (style.isDrawLine() && style.getLineColor() != null)
        {
          g2d.setColor((Color) style.getLineColor());
          float width = new Float(Math.min(3.0f, style.getLineWidth())).intValue();
          g2d.setStroke(new BasicStroke(width));
          g2d.drawRect(10, 4, getWidth() - 20, getHeight() - 8);
        }
      } catch (Exception ex)
      {
      }
    }
  };
  private DefaultCellEditor informationCellEditor;
  private DefaultCellEditor stylesCellEditor;
  private JProgressBar progress = new JProgressBar(0, 100);
  private JSlider slider = new JSlider(0, 100);
  private JLabel visibilityLabel = new JLabel("", JLabel.CENTER);//NOI18N
  private JLabel emptyLabel = new JLabel();
  private RetrievalServiceLayer wmsServiceLayerThatFiresPropertyChangeEvents = null;

  /** Creates a new instance of ActiveLayerTableCellEditor */
  public ActiveLayerTableCellEditor()
  {
    //progress.setUI(new MetalProgressBarUI());
    progress.setString("");//NOI18N
    progress.setStringPainted(true);

    visibilityLabel.setOpaque(false);
    visibilityLabel.addMouseListener(new MouseAdapter()
    {
      // deactivate & hide layer on doubleclick or
      // if already hidden activate, show and do a new retrieve

      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() == 2)
        {
          if (value instanceof RetrievalServiceLayer)
          {
            RetrievalServiceLayer layer = ((RetrievalServiceLayer) value);
            boolean flag = layer.getPNode().getVisible();
            layer.getPNode().setVisible(!flag);
            layer.setEnabled(!flag);
//                        fireTreeNodesChanged(this, new Object[]{root}, null, null);

            if (!flag)
            {
//                            layer.setRefreshNeeded(true);
              layer.retrieve(true);
            }
            ActiveLayerEvent ale = new ActiveLayerEvent();
            ale.setLayer(value);
            CismapBroker.getInstance().fireLayerVisibilityChanged(ale);
            table.repaint();
          }
        }
      }
    });

    informationBox = new JCheckBox();
    informationBox.setHorizontalAlignment(JCheckBox.CENTER);
    cbbStyleChooser = new JComboBox();
    cbbStyleChooser.setEditable(false);
    cbbStyleChooser.setRenderer(new StyleChooserCellRenderer());

    informationBox.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        WMSLayer l = null;
        if (value instanceof WMSLayer)
        {
          l = ((WMSLayer) value);
        } else if (value instanceof WMSServiceLayer && ((WMSServiceLayer) value).getWMSLayers().size() == 1)
        {
          l = ((WMSLayer) ((WMSServiceLayer) value).getWMSLayers().get(0));
        }
        l.setQuerySelected(informationBox.isSelected());
        ActiveLayerEvent ale = new ActiveLayerEvent();
        ale.setLayer(l.getParentServiceLayer());
        CismapBroker.getInstance().fireLayerInformationStatusChanged(ale);
      }
    });

    cbbStyleChooser.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {

        try
        {
          WMSLayer l = null;
          if (value instanceof WMSLayer)
          {
            l = ((WMSLayer) value);
          } else if (value instanceof WMSServiceLayer && ((WMSServiceLayer) value).getWMSLayers().size() == 1)
          {
            l = ((WMSLayer) ((WMSServiceLayer) value).getWMSLayers().get(0));
          }
          if (!(l.getSelectedStyle().equals((Style) cbbStyleChooser.getSelectedItem())))
          {
            ActiveLayerEvent ale = new ActiveLayerEvent();
            ale.setLayer(l.getParentServiceLayer());
            //CismapBroker.getInstance().fireLayerRemoved(ale);
            l.setSelectedStyle((Style) cbbStyleChooser.getSelectedItem());
            ((de.cismet.cismap.commons.retrieval.RetrievalService) value).retrieve(true);
            //CismapBroker.getInstance().fireLayerAdded(ale);
            CismapBroker.getInstance().fireLayerInformationStatusChanged(ale);
          }
          l.setSelectedStyle((Style) cbbStyleChooser.getSelectedItem());
          ActiveLayerEvent ale = new ActiveLayerEvent();
          ale.setLayer(l.getParentServiceLayer());
          ((de.cismet.cismap.commons.retrieval.RetrievalService) value).retrieve(true);
          // ((RetrievalService)value).retrieve();
          CismapBroker.getInstance().fireLayerRemoved(ale);
          CismapBroker.getInstance().fireLayerAdded(ale);
        } catch (Exception ex)
        {
          logger.error("Error while changing the style", ex);//NOI18N
        }

      }
    });

    moreButton.setFocusPainted(false);
    moreButton.setEnabled(false);
    moreButton.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        if(DEBUG)logger.debug("invoke FeatureService-QueryEditorDialog");//NOI18N
        QueryEditorDialog dia = new QueryEditorDialog(StaticSwingTools.getParentFrame(moreButton), true,
                ((WebFeatureService) value).getQuery());
        dia.setVisible(true);
        if (dia.getReturnStatus() == QueryEditorDialog.RET_OK)
        {
          ((WebFeatureService) value).setQuery(dia.getQueryString());
        }
      }
    });

    wfsStyleButton.setFocusPainted(false);
    wfsStyleButton.setEnabled(true);
    wfsStyleButton.setBorderPainted(false);
    wfsStyleButton.setContentAreaFilled(false);
    wfsStyleButton.setIconTextGap(0);
    wfsStyleButton.addMouseListener(new MouseAdapter()
    {
      // creates and shows the StyleDialog on doubleclick

      @Override
      public void mouseClicked(MouseEvent e)
      {
        //Event Dispatch Thread TERROR:
        //FIXME: ACHTUNG alle Exceptions die in dieser Operation auftreten und
        //nicht explizit gefangen werden, werden nicht auf der Console ausgegeben?!
        if (e.getClickCount() == 2)
        {
          final AbstractFeatureService selectedService = (AbstractFeatureService) value;

          try
          {
            if(DEBUG)logger.debug("invoke FeatureService-StyleDialog");//NOI18N
            // only create one instance of the styledialog
            if (styleDialog == null)
            {
              Frame parentFrame = StaticSwingTools.getParentFrame(wfsStyleButton);
              if(DEBUG)logger.debug("creating new StyleDialog '" + parentFrame.getTitle() + "'");//NOI18N
              styleDialog = new StyleDialog(parentFrame, true);
            }

            // configure dialog, adding attributes to the tab and
            // set style from the layer properties
            if(DEBUG)logger.debug("configure dialog");//NOI18N
            styleDialog.configureDialog(selectedService.getLayerProperties(), selectedService.getFeatureServiceAttributes(), selectedService.getQuery());

            if(DEBUG)logger.debug("set dialog visible");//NOI18N
            styleDialog.setVisible(true);


          } catch (Throwable t)
          {
            logger.error("could not configure StyleDialog: " + t.getMessage(), t);//NOI18N
          }
          // check returnstatus
          if (styleDialog != null && styleDialog.isAccepted())
          {
            Runnable r = new Runnable()
            {
              @Override
              public void run()
              {
                try
                {
                  boolean forceUpdate = false;
                  if (selectedService instanceof WebFeatureService)
                  {
                    if(styleDialog.isGeoAttributeChanged() || styleDialog.isAttributeSelectionChanged())
                    {
                      if(DEBUG)logger.debug("Attributes changed, updating the QUERY Element");//NOI18N
                      Element query = ((WebFeatureService) selectedService).getQueryElement();
                      WebFeatureService service = ((WebFeatureService) selectedService);
                      WFSFacade.setGeometry(query, styleDialog.getSelectedGeoAttribute(), service.getVersion());
                      WFSFacade.changePropertyNames(query, styleDialog.getSelectedAttributes(), service.getVersion());

                      service.setQueryElement(query);
                      forceUpdate = true;
                    }

                    if (styleDialog.isQueryStringChanged())
                    {
                      int i = JOptionPane.showConfirmDialog(StaticSwingTools.getParentFrame(wfsStyleButton), org.openide.util.NbBundle.getMessage(ActiveLayerTableCellEditor.class, "ActiveLayerTableCellEditor.mouseClicked(MouseEvent).showConfirmDialog.message"), //NOI18N
                              org.openide.util.NbBundle.getMessage(ActiveLayerTableCellEditor.class, "ActiveLayerTableCellEditor.mouseClicked(MouseEvent).showConfirmDialog.title"), //NOI18N
                              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                      if (i == JOptionPane.YES_OPTION)
                      {
                         if(DEBUG)logger.debug("Query String changed, updating the QUERY String");//NOI18N
                         selectedService.setQuery(styleDialog.getQueryString());
                         forceUpdate = true;
                      }
                    }
                  }

                  // this causes a refresh of the last created features and fires a retrieval event
                  selectedService.setFeatureServiceAttributes(styleDialog.getFeatureServiceAttributes());
                  
                  if(forceUpdate)
                  {
                    ((WebFeatureService) selectedService).setLayerPropertiesWithoutUpdate(styleDialog.getLayerProperties());
                    selectedService.retrieve(forceUpdate);
                  }
                  else
                  {
                    selectedService.setLayerProperties(styleDialog.getLayerProperties());
                  }
                } catch (Throwable t)
                {
                  logger.error(t.getMessage(), t);
                }
              }
            };
            CismetThreadPool.execute(r);
          } else
          {
            if(DEBUG)logger.debug("Style Dialog canceled");//NOI18N
          }
        }
      }
    });

    informationCellEditor =
            new DefaultCellEditor(informationBox);

    stylesCellEditor =
            new DefaultCellEditor(cbbStyleChooser);

    progress.setLayout(
            new BorderLayout(2, 2));
    progress.add(slider, BorderLayout.CENTER);
    slider.setOpaque(false);
    slider.setValueIsAdjusting(true);
    slider.addChangeListener(
            new ChangeListener()
            {
              @Override
              public void stateChanged(ChangeEvent e)
              {
                JSlider slider = (JSlider) e.getSource();

                if (value instanceof RetrievalServiceLayer)
                {
                  float f = (float) (slider.getValue() * 0.01);
                  ((RetrievalServiceLayer) value).setTranslucency(f);
                  PNode pi = ((RetrievalServiceLayer) value).getPNode();
                  if (pi != null)
                  {
                    pi.setTransparency(f);
                    pi.repaint();
                  }

                }

                if (!slider.getValueIsAdjusting())
                {

                }
              }
            });
   // _WTF? ....................................................................
    slider.addMouseListener(
            new MouseAdapter()
            {

              @Override
              public void mouseClicked(MouseEvent e)
              {
                if(DEBUG)logger.debug("Click" + e);//NOI18N
              }

      @Override
              public void mouseReleased(MouseEvent e)
              {
                  // übergibt die Darstellung wieder an den Renderer
                  ActiveLayerTableCellEditor.this.stopCellEditing();
              }
            });

            slider.dispatchEvent(
            new MouseEvent(slider, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
            MouseEvent.BUTTON1_MASK, 3, 2, 1, false, MouseEvent.BUTTON1));
    // _WTF? ....................................................................
  }

  /**
   * Sets an initial <I>value</I> for the editor.  This will cause
   * the editor to stopEditing and lose any partially edited value
   * if the editor is editing when this method is called. <p>
   *
   * Returns the component that should be added to the client's
   * Component hierarchy.  Once installed in the client's hierarchy
   * this component will then be able to draw and receive user input.
   *
   * @param	tree		the JTree that is asking the editor to edit;
   * 				this parameter can be null
   * @param	value		the value of the cell to be edited
   * @param	isSelected	true is the cell is to be renderer with
   * 				selection highlighting
   * @param	expanded	true if the node is expanded
   * @param	leaf		true if the node is a leaf node
   * @param	row		the row index of the node being edited
   * @return	the component for editing
   */
  @Override
  public Component getTreeCellEditorComponent(
          JTree tree, Object value, boolean isSelected,
          boolean expanded, boolean leaf, int row)
  {
    if(DEBUG)logger.debug("TreeCellEditor requested");//NOI18N
    JTextField treeEditorTextField = new JTextField();
    treeEditorTextField.setEditable(true);
    DefaultCellEditor treeEditor = new DefaultCellEditor(treeEditorTextField);
    return treeEditor.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
  }

  /**
   *  Sets an initial <code>value</code> for the editor.  This will cause
   *  the editor to <code>stopEditing</code> and lose any partially
   *  edited value if the editor is editing when this method is called. <p>
   *
   *  Returns the component that should be added to the client's
   *  <code>Component</code> hierarchy.  Once installed in the client's
   *  hierarchy this component will then be able to draw and receive
   *  user input.
   *
   * @param	table		the <code>JTable</code> that is asking the
   * 				editor to edit; can be <code>null</code>
   * @param	value		the value of the cell to be edited; it is
   * 				up to the specific editor to interpret
   * 				and draw the value.  For example, if value is
   * 				the string "true", it could be rendered as a
   * 				string or it could be rendered as a check
   * 				box that is checked.  <code>null</code>
   * 				is a valid value
   * @param	isSelected	true if the cell is to be rendered with
   * 				highlighting
   * @param	row     	the row of the cell being edited
   * @param	column  	the column of the cell being edited
   * @return	the component for editing
   */
  @Override
  public Component getTableCellEditorComponent(
          final JTable table,
          final Object value,
          boolean isSelected, int row, int column)
  {
    int realColumn = table.convertColumnIndexToModel(column);
    this.value = value;
    this.table = table;
    WMSLayer wmsLayer = null;
    if (value instanceof WMSLayer)
    {
      wmsLayer = ((WMSLayer) value);
    } else if (value instanceof WMSServiceLayer)
    {
      wmsLayer = (WMSLayer) ((WMSServiceLayer) value).getWMSLayers().get(0);
    }

    if (realColumn == 0)
    {
      TableCellRenderer renderer = table.getCellRenderer(row, column);
      visibilityLabel.setIcon(((JLabel) renderer.getTableCellRendererComponent(table, value, isSelected, isSelected, row, column)).getIcon());
      return visibilityLabel;
    } else if (realColumn == 2)
    {
      if(DEBUG)logger.debug("Editor column=" + realColumn);//NOI18N
      if (value instanceof WMSServiceLayer)
      {
        try
        {
          if(DEBUG)logger.debug("Combo");//NOI18N
          DefaultComboBoxModel model = new DefaultComboBoxModel(wmsLayer.getOgcCapabilitiesLayer().getStyles());
          cbbStyleChooser.setModel(model);
          cbbStyleChooser.setSelectedItem(wmsLayer.getSelectedStyle());
          return stylesCellEditor.getTableCellEditorComponent(table, wmsLayer.getSelectedStyle(), isSelected, row, column);
        } catch (Exception e)
        {
          logger.warn("Error while setting the StyleEditor", e);//NOI18N
          return null;
        }

      } else
      {
        if(DEBUG)logger.debug("StyleButton");//NOI18N
        return wfsStyleButton;
      }

    } else if (realColumn == 3)
    {
      return informationCellEditor.getTableCellEditorComponent(table, new Boolean(wmsLayer.isQuerySelected()), isSelected, row, column);
    } else if (realColumn == 4)
    {
      if (wmsServiceLayerThatFiresPropertyChangeEvents != null)
      {
        wmsServiceLayerThatFiresPropertyChangeEvents.removePropertyChangeListener(this);
      }

      ((RetrievalServiceLayer) value).addPropertyChangeListener(this);
      wmsServiceLayerThatFiresPropertyChangeEvents =
              ((RetrievalServiceLayer) value);
      slider.setValue((int) (((RetrievalServiceLayer) value).getTranslucency() * 100));
      slider.requestFocus();
      if (((RetrievalServiceLayer) value).getProgress() == -1)
      {
        progress.setIndeterminate(true);
      } else
      {
        progress.setIndeterminate(false);
      }

      progress.setValue(((RetrievalServiceLayer) value).getProgress());
      slider.updateUI();

      return progress;
    } else if (realColumn == 5)
    {
      if (value instanceof WMSServiceLayer)
      {
        moreButton.setEnabled(false);
      } else if (value instanceof WebFeatureService)
      {
        moreButton.setEnabled(true);
      }

      return moreButton;
    } else
    {
      return null;
    }

  }

  /**
   * Returns the value contained in the editor.
   * @return the value contained in the editor
   */
  @Override
  public Object getCellEditorValue()
  {
    return value;
  }

  /**
   * This method gets called when a bound property is changed.
   * @param evt A PropertyChangeEvent object describing the event source
   *   	and the property that has changed.
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    if(DEBUG)logger.debug("Progressvalue in Editor changed");//NOI18N
    if (evt.getSource() instanceof RetrievalServiceLayer && evt.getPropertyName().equals("progress"))//NOI18N
    {
      int newValue = ((Integer) (evt.getNewValue())).intValue();
      if (newValue == 0)
      {
        progress.setIndeterminate(true);
      } else
      {
        progress.setIndeterminate(false);
        progress.setValue(newValue);
      }

    }
  }

  /**
   * Calls <code>fireEditingCanceled</code>.
   */
  @Override
  public void cancelCellEditing()
  {
    if (wmsServiceLayerThatFiresPropertyChangeEvents != null)
    {
      wmsServiceLayerThatFiresPropertyChangeEvents.removePropertyChangeListener(this);
    }

    super.cancelCellEditing();
  }

  /**
   * Calls <code>fireEditingStopped</code> and returns true.
   * @return true
   */
  @Override
  public boolean stopCellEditing()
  {
    if (wmsServiceLayerThatFiresPropertyChangeEvents != null)
    {
      wmsServiceLayerThatFiresPropertyChangeEvents.removePropertyChangeListener(this);
    }

    boolean retValue;
    try
    {
      retValue = super.stopCellEditing();
    } finally
    {
    }
    return retValue;
  }
}

class StyleChooserCellRenderer extends DefaultListCellRenderer
{

  private ImageIcon styleIcon;

  public StyleChooserCellRenderer()
  {
    styleIcon = new ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/style.png"));//NOI18N
  }

  @Override
  public Component getListCellRendererComponent(JList list, Object listValue, int index, boolean isSelected, boolean cellHasFocus)
  {
    JLabel retValue;
    retValue = (JLabel) super.getListCellRendererComponent(list, listValue, index, isSelected, cellHasFocus);
    retValue.setText(((Style) listValue).getTitle());
    retValue.setIcon(styleIcon);
    retValue.setIconTextGap(4);
    return retValue;
  }
}
