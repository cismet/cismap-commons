/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package de.cismet.cismap.commons;

import org.jdom.Element;

/**
 * Defines operations to serialize (and deserialize) an arbitrary object to a DOM Element
 * Which properties of the object are serialized depends on the
 * implementation.
 *
 * @author Throsten Hell
 * @author Pascal Dihé
 */
public interface ConvertableToXML<T>
{

  /**
   * Name of the mandatory type attribute, value shall be the canonical class name.
   */
  public final static String TYPE_ATTRIBUTE = "type";
/**
 * Serializes the object that implements the ConvertableToXML interface to a
 * DOM Element. Although no further assumption is made about the XML Structure of the
 * serialized object implementation classes shall at least provide an attribute
 * <i>type</i> must contain the class name ob the object.
 *
 * @return XML representation of the object
 */
  public Element toElement();


  /**
   * Initialies an object that implements the ConvertableToXML interface from
   * a DOM Element.
   *
   * @param element the elemen to be reconstructed
   * @throws Exception if the object could not be initialized
   */
  public void initFromElement(Element element) throws Exception;
}
