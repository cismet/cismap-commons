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
import javax.xml.bind.annotation.XmlType;

/**
 * A person or organization.
 *
 * <p>Java-Klasse für personType complex type.</p>
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 *
 * <pre>
   &lt;complexType name="personType">
     &lt;complexContent>
       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         &lt;sequence>
           &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
           &lt;element name="email" type="{http://www.topografix.com/GPX/1/1}emailType" minOccurs="0"/>
           &lt;element name="link" type="{http://www.topografix.com/GPX/1/1}linkType" minOccurs="0"/>
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
    name = "personType",
    propOrder = {
            "name",
            "email",
            "link"
        }
)
public class PersonType {

    //~ Instance fields --------------------------------------------------------

    protected String name;
    protected EmailType email;
    protected LinkType link;

    //~ Methods ----------------------------------------------------------------

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     *
     * @return  possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Ruft den Wert der email-Eigenschaft ab.
     *
     * @return  possible object is {@link EmailType }
     */
    public EmailType getEmail() {
        return email;
    }

    /**
     * Legt den Wert der email-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link EmailType }
     */
    public void setEmail(final EmailType value) {
        this.email = value;
    }

    /**
     * Ruft den Wert der link-Eigenschaft ab.
     *
     * @return  possible object is {@link LinkType }
     */
    public LinkType getLink() {
        return link;
    }

    /**
     * Legt den Wert der link-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link LinkType }
     */
    public void setLink(final LinkType value) {
        this.link = value;
    }
}
