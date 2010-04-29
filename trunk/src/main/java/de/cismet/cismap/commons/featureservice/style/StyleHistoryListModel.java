/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.style;

import de.cismet.cismap.commons.ConvertableToXML;
import java.io.File;
import java.util.Vector;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * The StyleHistoryListModel is a ListModel that contains a list of Style-objects.
 * @author nh
 */
public class StyleHistoryListModel implements ListModel, ConvertableToXML
{

  private final Logger logger = Logger.getLogger(StyleHistoryListModel.class);
  /* Maximum number of styles stored in the listmodel */
  private static final int MAX_STYLES = 15;
  /* Name of the root-element of the listmodel as JDOM-element */
  public static final String STYLE_ROOT = "StyleHistory";//NOI18N
  /* vector that contains the styles */
  private Vector<Style> styleList;
  /* JDOM-parser */
  private SAXBuilder builder = new SAXBuilder();

  /**
   * Constructor that creates a list of styles from the delivered XML-file.
   * @param doc JDOM-Document das die History beinhält
   */
  public StyleHistoryListModel(File f) throws Exception
  {
    this();

    try
    {
      Document doc = builder.build(f);
      Element root = doc.getRootElement();
      if (root != null && root.getName().equals(STYLE_ROOT))
      {
        this.initFromElement(root);
      } else
      {
        logger.error("file '" + f.getName() + "' contains wrong xml content:\n" + doc);//NOI18N
      }
    } catch (Exception ex)
    {
      logger.error("Could not load style history from '" + f.getName() + "'", ex);//NOI18N
    }
  }

  /**
   * Constructor with empty stylelist.
   */
  public StyleHistoryListModel()
  {
    this.styleList = new Vector<Style>();
    this.styleList.ensureCapacity(MAX_STYLES);
  }

  /**
   * Adds a Style to the listmodel. If the model contains more than the allowed
   * maximum the oldest style will be dropped.
   * @param e the style to add
   */
  public void addStyle(Style newStyle)
  {
    if (!elementEquals(newStyle))
    {
      styleList.add(newStyle);
      if (styleList.size() > MAX_STYLES)
      {
        styleList.remove(0);
      }
    }
  }

  /**
   * Returns the style from a specific position inside the stylelist.
   * @param index position of the desired style
   * @return object (instanceof Style)
   */
  @Override
  public Object getElementAt(int index)
  {
    return styleList.get(index);
  }

  /**
   * Returns the current count of saved styles.
   */
  @Override
  public int getSize()
  {
    return styleList.size();
  }

  /**
   * Deletes all saved styles.
   */
  public void clear()
  {
    styleList.clear();
  }

  /**
   * Checks if there's already an equal style inside the stylelist.
   * @param compare Style that should be compared with the list
   * @return true if there's already an equal style, else false
   */
  public boolean elementEquals(Style compare)
  {
    if (styleList.isEmpty())
    {
      return false;
    } else
    {
      boolean returnValue = false;
      for (Style s : styleList)
      {
        if (s.compareTo(compare) == 0)
        {
          returnValue = true;
          break;
        }
      }
      return returnValue;
    }
  }

  /**
   * Returns this model as JDOM-element with all styles as children.
   */
  @Override
  public Element toElement()
  {
    Element e = new Element(STYLE_ROOT);
    for (Style s : styleList)
    {
      e.addContent(s.toElement());
    }
    return e;
  }

  @Override
  public void addListDataListener(ListDataListener l)
  {
  }

  @Override
  public void removeListDataListener(ListDataListener l)
  {
  }

  @Override
  public void initFromElement(Element element) throws Exception
  {
    this.styleList.clear();
    this.styleList.ensureCapacity(element.getChildren(Style.STYLE_ELEMENT).size());
    for (Object o : element.getChildren(Style.STYLE_ELEMENT))
    {
      if (o instanceof Element)
      {
        Style newStyle = new BasicStyle((Element) o);
        if (newStyle != null)
        {
          this.styleList.add(newStyle);
        }
      }
    }
  }
}
