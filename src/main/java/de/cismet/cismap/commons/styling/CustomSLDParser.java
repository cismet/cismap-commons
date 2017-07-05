/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.styling;

import org.deegree.style.persistence.sld.SLDParser;
import org.deegree.style.se.parser.SymbologyParser;
import org.deegree.style.se.unevaluated.Style;

import org.openide.util.Exceptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CustomSLDParser extends SLDParser {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   input  in DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  XMLStreamException  DOCUMENT ME!
     */
    public static Map<String, LinkedList<Style>> getCustomStyles(final Reader input) throws XMLStreamException {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final Map<String, LinkedList<Style>> map = getStyles(factory.createXMLStreamReader(input));
        try {
            input.reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        final XMLStreamReader in = factory.createXMLStreamReader(input);

        int index = 0;
        while (!in.isStartElement() || (in.getLocalName() == null)
                    || !(in.getLocalName().equals("NamedLayer") || in.getLocalName().equals("UserLayer"))) {
            in.nextTag();
        }

        while ((in.hasNext() && (in.getLocalName().equals("NamedLayer") && !in.isEndElement()))
                    || in.getLocalName().equals("UserLayer")) {
            final LinkedList<Style> styles = new LinkedList<Style>();

            in.nextTag();

            in.require(START_ELEMENT, null, "Name");
            final String name = in.getElementText();

            in.nextTag();

            // skip description
            if (in.getLocalName().equals("Description")) {
                skipElement(in);
            }

            if (in.getLocalName().equals("LayerFeatureConstraints")) {
                skipElement(in);
            }

            if (in.getLocalName().equals("NamedStyle")) {
                // does not make sense to reference a named style when configuring it...
                skipElement(in);
            }

            String styleName = null;

            while (in.hasNext() && in.getLocalName().equals("UserStyle")) {
                while (in.hasNext() && !(in.isEndElement() && in.getLocalName().equals("UserStyle"))) {
                    in.nextTag();

                    if (in.getLocalName().equals("Name")) {
                        styleName = in.getElementText();
                    }

                    // TODO skipped
                    if (in.getLocalName().equals("Description")) {
                        skipElement(in);
                    }

                    // TODO skipped
                    if (in.getLocalName().equals("Title")) {
                        in.getElementText();
                    }

                    // TODO skipped
                    if (in.getLocalName().equals("Abstract")) {
                        in.getElementText();
                    }

                    if (in.getLocalName().equals("IsDefault")) {
                        final String def = in.getElementText();
                        if ((styleName == null) && def.equalsIgnoreCase("true")) {
                            styleName = "default";
                        }
                    }

                    while (in.getLocalName().equals("FeatureTypeStyle")
                                || in.getLocalName().equals("CoverageStyle")
                                || in.getLocalName().equals("OnlineResource")) {
                        while (!(in.isEndElement()
                                        && (in.getLocalName().equals("FeatureTypeStyle")
                                            || in.getLocalName().equals("CoverageStyle")))) {
                            in.nextTag();
                            if (in.getLocalName().equals("VendorOption")) {
                                Style s = map.get(name).get(index);

                                if (!(s instanceof CustomStyle)) {
                                    s = new CustomStyle(s);
                                    map.get(name).remove(index);
                                    map.get(name).add(index, s);
                                }

                                final CustomStyle cs = (CustomStyle)s;
                                final String vendorSpecAttrName = in.getAttributeValue("", "name");

                                if (vendorSpecAttrName.equals("endCap")) {
                                    final String capName = in.getAttributeValue("", "cap-name");

                                    cs.addEndPointStyle(new EndPointStyleDescription(capName));
                                }
                                skipElement(in);
                            } else {
                                skipElement(in);
                            }
                        }
                        ++index;

                        in.nextTag();
                    }
                }
                in.nextTag();
            }
            in.nextTag();
//            map.put(name, styles);
        }

        return map;
    }

//    /**
//     * DOCUMENT ME!
//     *
//     * @param   in  DOCUMENT ME!
//     *
//     * @return  DOCUMENT ME!
//     *
//     * @throws  XMLStreamException  DOCUMENT ME!
//     */
//    public static Map<String, LinkedList<Style>> getStyles(final XMLStreamReader in) throws XMLStreamException {
//        final Map<String, LinkedList<Style>> map = new HashMap<String, LinkedList<Style>>();
//
//        while (!in.isStartElement() || (in.getLocalName() == null)
//                    || !(in.getLocalName().equals("NamedLayer") || in.getLocalName().equals("UserLayer"))) {
//            in.nextTag();
//        }
//
//        while ((in.hasNext() && (in.getLocalName().equals("NamedLayer") && !in.isEndElement()))
//                    || in.getLocalName().equals("UserLayer")) {
//            final LinkedList<Style> styles = new LinkedList<Style>();
//
//            in.nextTag();
//
//            in.require(START_ELEMENT, null, "Name");
//            final String name = in.getElementText();
//
//            in.nextTag();
//
//            // skip description
//            if (in.getLocalName().equals("Description")) {
//                skipElement(in);
//            }
//
//            if (in.getLocalName().equals("LayerFeatureConstraints")) {
//                skipElement(in);
//            }
//
//            if (in.getLocalName().equals("NamedStyle")) {
//                // does not make sense to reference a named style when configuring it...
//                skipElement(in);
//            }
//
//            String styleName = null;
//
//            while (in.hasNext() && in.getLocalName().equals("UserStyle")) {
//                while (in.hasNext() && !(in.isEndElement() && in.getLocalName().equals("UserStyle"))) {
//                    in.nextTag();
//
//                    if (in.getLocalName().equals("Name")) {
//                        styleName = in.getElementText();
//                    }
//
//                    // TODO skipped
//                    if (in.getLocalName().equals("Description")) {
//                        skipElement(in);
//                    }
//
//                    // TODO skipped
//                    if (in.getLocalName().equals("Title")) {
//                        in.getElementText();
//                    }
//
//                    // TODO skipped
//                    if (in.getLocalName().equals("Abstract")) {
//                        in.getElementText();
//                    }
//
//                    if (in.getLocalName().equals("IsDefault")) {
//                        final String def = in.getElementText();
//                        if ((styleName == null) && def.equalsIgnoreCase("true")) {
//                            styleName = "default";
//                        }
//                    }
//
//                    if (in.getLocalName().equals("VendorOption")) {
//                        System.out.println("vendor Option");
//                    }
//
//                    while (in.getLocalName().equals("FeatureTypeStyle")
//                                || in.getLocalName().equals("CoverageStyle")
//                                || in.getLocalName().equals("OnlineResource")) {
//                        final Style style = SymbologyParser.INSTANCE.parseFeatureTypeOrCoverageStyle(in);
//                        style.setName(styleName);
//                        styles.add(style);
//                        in.nextTag();
//                    }
//                }
//                in.nextTag();
//            }
//            in.nextTag();
//            map.put(name, styles);
//        }
//
//        return map;
//    }
}
