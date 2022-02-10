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
import javax.xml.bind.annotation.XmlType;

/**
 * An email address. Broken into two parts (id and domain) to help prevent email harvesting.
 *
 * <p>Java-Klasse für emailType complex type.</p>
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 *
 * <pre>
   &lt;complexType name="emailType">
     &lt;complexContent>
       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         &lt;attribute name="domain" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
       &lt;/restriction>
     &lt;/complexContent>
   &lt;/complexType>
 * </pre>
 *
 * @version  $Revision$, $Date$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "emailType")
public class EmailType {

    //~ Instance fields --------------------------------------------------------

    @XmlAttribute(
        name = "id",
        required = true
    )
    protected String id;
    @XmlAttribute(
        name = "domain",
        required = true
    )
    protected String domain;

    //~ Methods ----------------------------------------------------------------

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     *
     * @return  possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der domain-Eigenschaft ab.
     *
     * @return  possible object is {@link String }
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Legt den Wert der domain-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setDomain(final String value) {
        this.domain = value;
    }
}
