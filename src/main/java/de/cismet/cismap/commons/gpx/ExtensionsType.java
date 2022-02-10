/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2022.01.28 um 04:02:30 PM CET
// mit jdk1.8.0_311/bin/xjc  -d src -p de.cismet.cismap.commons.gpx gpx.xsd
//
package de.cismet.cismap.commons.gpx;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlType;

/**
 * You can add extend GPX by adding your own elements from another schema here.
 *
 * <p>Java-Klasse für extensionsType complex type.</p>
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 *
 * <pre>
   &lt;complexType name="extensionsType">
     &lt;complexContent>
       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         &lt;sequence>
           &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
         &lt;/sequence>
       &lt;/restriction>
     &lt;/complexContent>
   &lt;/complexType>
 * </pre>
 *
 * @version  $Revision$, $Date$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "extensionsType",
    propOrder = { "any" }
)
public class ExtensionsType {

    //~ Instance fields --------------------------------------------------------

    @XmlAnyElement(lax = true)
    protected List<Object> any;

    //~ Methods ----------------------------------------------------------------

    /**
     * Gets the value of the any property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the any property.</p>
     *
     * <p>For example, to add a new item, do as follows:</p>
     *
     * <pre>
          getAny().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link Element } {@link Object }</p>
     *
     * @return  DOCUMENT ME!
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }
}
