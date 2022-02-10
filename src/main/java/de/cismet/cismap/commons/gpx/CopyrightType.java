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
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Information about the copyright holder and any license governing use of this file. By linking to an appropriate
 * license, you may place your data into the public domain or grant additional usage rights.
 *
 * <p>Java-Klasse für copyrightType complex type.</p>
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 *
 * <pre>
   &lt;complexType name="copyrightType">
     &lt;complexContent>
       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         &lt;sequence>
           &lt;element name="year" type="{http://www.w3.org/2001/XMLSchema}gYear" minOccurs="0"/>
           &lt;element name="license" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
         &lt;/sequence>
         &lt;attribute name="author" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
       &lt;/restriction>
     &lt;/complexContent>
   &lt;/complexType>
 * </pre>
 *
 * @version  $Revision$, $Date$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "copyrightType",
    propOrder = {
            "year",
            "license"
        }
)
public class CopyrightType {

    //~ Instance fields --------------------------------------------------------

    @XmlSchemaType(name = "gYear")
    protected XMLGregorianCalendar year;
    @XmlSchemaType(name = "anyURI")
    protected String license;
    @XmlAttribute(
        name = "author",
        required = true
    )
    protected String author;

    //~ Methods ----------------------------------------------------------------

    /**
     * Ruft den Wert der year-Eigenschaft ab.
     *
     * @return  possible object is {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getYear() {
        return year;
    }

    /**
     * Legt den Wert der year-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link XMLGregorianCalendar }
     */
    public void setYear(final XMLGregorianCalendar value) {
        this.year = value;
    }

    /**
     * Ruft den Wert der license-Eigenschaft ab.
     *
     * @return  possible object is {@link String }
     */
    public String getLicense() {
        return license;
    }

    /**
     * Legt den Wert der license-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setLicense(final String value) {
        this.license = value;
    }

    /**
     * Ruft den Wert der author-Eigenschaft ab.
     *
     * @return  possible object is {@link String }
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Legt den Wert der author-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setAuthor(final String value) {
        this.author = value;
    }
}
