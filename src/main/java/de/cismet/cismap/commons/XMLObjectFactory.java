/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons;

import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * The XMLObjectFactory factory recreates arbitrary objects from xml documents.<br/>
 *
 * @author Pascal Dihé
 */
public class XMLObjectFactory
{
  //private final static Logger logger = Logger.getLogger(XMLObjectFactory.class);

  public static Object restoreObjectfromElement(Element element) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, Exception
  {
    String type = element.getAttributeValue(ConvertableToXML.TYPE_ATTRIBUTE);
    if (type == null)
    {
      //logger.error("unsupported xml element, type attribute is missing");
      throw new Exception("unsupported xml element, type attribute is missing");//NOI18N
    }

    //logger.info("creating new instance of '" + type + "' class");
    Class objectType = Class.forName(type);

    Constructor objectConstructor;
    try
    {
      objectConstructor = objectType.getConstructor(Element.class);
      //logger.debug("constructing '" + objectType.getSimpleName() + "' using supported constructor");
      return objectConstructor.newInstance(element);
    } catch (NoSuchMethodException ex)
    {
      //logger.debug("constructing '" + objectType.getSimpleName() + "' using empty constructor");
      Object object = objectType.newInstance();
      ((ConvertableToXML) object).initFromElement(element);
      return object;
    }
  }

  public static Object restoreObjectfromXml(String xmlString) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, Exception
  {
    SAXBuilder saxBuilder = new SAXBuilder(false);
    StringReader stringReader = new StringReader(xmlString);
    Document document = saxBuilder.build(stringReader);
    return XMLObjectFactory.restoreObjectfromElement(document.getRootElement());
  }
}
