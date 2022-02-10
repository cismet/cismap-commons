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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * A link to an external resource (Web page, digital photo, video clip, etc) with additional information.
 *
 * <p>Java-Klasse für linkType complex type.</p>
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 *
 * <pre>
   &lt;complexType name="linkType">
     &lt;complexContent>
       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         &lt;sequence>
           &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
           &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         &lt;/sequence>
         &lt;attribute name="href" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
       &lt;/restriction>
     &lt;/complexContent>
   &lt;/complexType>
 * </pre>
 *
 * @version  $Revision$, $Date$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "linkType",
    propOrder = {
            "text",
            "type"
        }
)
public class LinkType {

    //~ Instance fields --------------------------------------------------------

    protected String text;
    protected String type;
    @XmlAttribute(
        name = "href",
        required = true
    )
    @XmlSchemaType(name = "anyURI")
    protected String href;

    //~ Methods ----------------------------------------------------------------

    /**
     * Ruft den Wert der text-Eigenschaft ab.
     *
     * @return  possible object is {@link String }
     */
    public String getText() {
        return text;
    }

    /**
     * Legt den Wert der text-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setText(final String value) {
        this.text = value;
    }

    /**
     * Ruft den Wert der type-Eigenschaft ab.
     *
     * @return  possible object is {@link String }
     */
    public String getType() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setType(final String value) {
        this.type = value;
    }

    /**
     * Ruft den Wert der href-Eigenschaft ab.
     *
     * @return  possible object is {@link String }
     */
    public String getHref() {
        return href;
    }

    /**
     * Legt den Wert der href-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setHref(final String value) {
        this.href = value;
    }
}
