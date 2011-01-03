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

import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.StringReader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.logging.Level;

/**
 * The XMLObjectFactory factory recreates arbitrary objects from xml documents.<br/>
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class XMLObjectFactory {

    //~ Methods ----------------------------------------------------------------

    // private final static Logger logger = Logger.getLogger(XMLObjectFactory.class);

    /**
     * DOCUMENT ME!
     *
     * @param   element  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ClassNotFoundException     DOCUMENT ME!
     * @throws  InstantiationException     DOCUMENT ME!
     * @throws  IllegalAccessException     DOCUMENT ME!
     * @throws  IllegalArgumentException   DOCUMENT ME!
     * @throws  InvocationTargetException  DOCUMENT ME!
     * @throws  Exception                  DOCUMENT ME!
     */
    public static Object restoreObjectfromElement(final Element element) throws ClassNotFoundException,
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        Exception {
        final String type = element.getAttributeValue(ConvertableToXML.TYPE_ATTRIBUTE);
        if (type == null) {
            // logger.error("unsupported xml element, type attribute is missing");
            throw new Exception("unsupported xml element, type attribute is missing");
        }

        // logger.info("creating new instance of '" + type + "' class");
        final Class objectType = Class.forName(type);

        final Constructor objectConstructor;
        try {
            objectConstructor = objectType.getConstructor(Element.class);
            // logger.debug("constructing '" + objectType.getSimpleName() + "' using supported constructor");
            return objectConstructor.newInstance(element);
        } catch (NoSuchMethodException ex) {
            // logger.debug("constructing '" + objectType.getSimpleName() + "' using empty constructor");
            final Object object = objectType.newInstance();
            ((ConvertableToXML)object).initFromElement(element);
            return object;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   xmlString  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ClassNotFoundException     DOCUMENT ME!
     * @throws  InstantiationException     DOCUMENT ME!
     * @throws  IllegalAccessException     DOCUMENT ME!
     * @throws  IllegalArgumentException   DOCUMENT ME!
     * @throws  InvocationTargetException  DOCUMENT ME!
     * @throws  Exception                  DOCUMENT ME!
     */
    public static Object restoreObjectfromXml(final String xmlString) throws ClassNotFoundException,
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException,
        Exception {
        final SAXBuilder saxBuilder = new SAXBuilder(false);
        final StringReader stringReader = new StringReader(xmlString);
        final Document document = saxBuilder.build(stringReader);
        return XMLObjectFactory.restoreObjectfromElement(document.getRootElement());
    }
}
