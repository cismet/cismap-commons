/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons;

import org.jdom.Element;

/**
 * Defines operations to serialize (and deserialize) an arbitrary object to a DOM Element Which properties of the object
 * are serialized depends on the implementation.
 *
 * @author   Throsten Hell
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public interface ConvertableToXML<T> {

    //~ Instance fields --------------------------------------------------------

    /** Name of the mandatory type attribute, value shall be the canonical class name. */
    String TYPE_ATTRIBUTE = "type";                                                   // NOI18N

    //~ Methods ----------------------------------------------------------------

    /**
     * Serializes the object that implements the ConvertableToXML interface to a DOM Element. Although no further
     * assumption is made about the XML Structure of the serialized object implementation classes shall at least provide
     * an attribute <i>type</i> must contain the class name ob the object.
     *
     * @return  XML representation of the object
     */
    Element toElement();

    /**
     * Initialies an object that implements the ConvertableToXML interface from a DOM Element.
     *
     * @param   element  the elemen to be reconstructed
     *
     * @throws  Exception  if the object could not be initialized
     */
    void initFromElement(Element element) throws Exception;
}
