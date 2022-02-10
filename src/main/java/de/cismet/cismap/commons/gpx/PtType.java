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

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * A geographic point with optional elevation and time. Available for use by other schemas.
 *
 * <p>Java-Klasse für ptType complex type.</p>
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 *
 * <pre>
   &lt;complexType name="ptType">
     &lt;complexContent>
       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         &lt;sequence>
           &lt;element name="ele" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
           &lt;element name="time" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
         &lt;/sequence>
         &lt;attribute name="lat" use="required" type="{http://www.topografix.com/GPX/1/1}latitudeType" />
         &lt;attribute name="lon" use="required" type="{http://www.topografix.com/GPX/1/1}longitudeType" />
       &lt;/restriction>
     &lt;/complexContent>
   &lt;/complexType>
 * </pre>
 *
 * @version  $Revision$, $Date$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "ptType",
    propOrder = {
            "ele",
            "time"
        }
)
public class PtType {

    //~ Instance fields --------------------------------------------------------

    protected BigDecimal ele;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar time;
    @XmlAttribute(
        name = "lat",
        required = true
    )
    protected BigDecimal lat;
    @XmlAttribute(
        name = "lon",
        required = true
    )
    protected BigDecimal lon;

    //~ Methods ----------------------------------------------------------------

    /**
     * Ruft den Wert der ele-Eigenschaft ab.
     *
     * @return  possible object is {@link BigDecimal }
     */
    public BigDecimal getEle() {
        return ele;
    }

    /**
     * Legt den Wert der ele-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link BigDecimal }
     */
    public void setEle(final BigDecimal value) {
        this.ele = value;
    }

    /**
     * Ruft den Wert der time-Eigenschaft ab.
     *
     * @return  possible object is {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getTime() {
        return time;
    }

    /**
     * Legt den Wert der time-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link XMLGregorianCalendar }
     */
    public void setTime(final XMLGregorianCalendar value) {
        this.time = value;
    }

    /**
     * Ruft den Wert der lat-Eigenschaft ab.
     *
     * @return  possible object is {@link BigDecimal }
     */
    public BigDecimal getLat() {
        return lat;
    }

    /**
     * Legt den Wert der lat-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link BigDecimal }
     */
    public void setLat(final BigDecimal value) {
        this.lat = value;
    }

    /**
     * Ruft den Wert der lon-Eigenschaft ab.
     *
     * @return  possible object is {@link BigDecimal }
     */
    public BigDecimal getLon() {
        return lon;
    }

    /**
     * Legt den Wert der lon-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link BigDecimal }
     */
    public void setLon(final BigDecimal value) {
        this.lon = value;
    }
}
