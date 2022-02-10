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
import javax.xml.bind.annotation.XmlType;

/**
 * Two lat/lon pairs defining the extent of an element.
 *
 * <p>Java-Klasse für boundsType complex type.</p>
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 *
 * <pre>
   &lt;complexType name="boundsType">
     &lt;complexContent>
       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         &lt;attribute name="minlat" use="required" type="{http://www.topografix.com/GPX/1/1}latitudeType" />
         &lt;attribute name="minlon" use="required" type="{http://www.topografix.com/GPX/1/1}longitudeType" />
         &lt;attribute name="maxlat" use="required" type="{http://www.topografix.com/GPX/1/1}latitudeType" />
         &lt;attribute name="maxlon" use="required" type="{http://www.topografix.com/GPX/1/1}longitudeType" />
       &lt;/restriction>
     &lt;/complexContent>
   &lt;/complexType>
 * </pre>
 *
 * @version  $Revision$, $Date$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "boundsType")
public class BoundsType {

    //~ Instance fields --------------------------------------------------------

    @XmlAttribute(
        name = "minlat",
        required = true
    )
    protected BigDecimal minlat;
    @XmlAttribute(
        name = "minlon",
        required = true
    )
    protected BigDecimal minlon;
    @XmlAttribute(
        name = "maxlat",
        required = true
    )
    protected BigDecimal maxlat;
    @XmlAttribute(
        name = "maxlon",
        required = true
    )
    protected BigDecimal maxlon;

    //~ Methods ----------------------------------------------------------------

    /**
     * Ruft den Wert der minlat-Eigenschaft ab.
     *
     * @return  possible object is {@link BigDecimal }
     */
    public BigDecimal getMinlat() {
        return minlat;
    }

    /**
     * Legt den Wert der minlat-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link BigDecimal }
     */
    public void setMinlat(final BigDecimal value) {
        this.minlat = value;
    }

    /**
     * Ruft den Wert der minlon-Eigenschaft ab.
     *
     * @return  possible object is {@link BigDecimal }
     */
    public BigDecimal getMinlon() {
        return minlon;
    }

    /**
     * Legt den Wert der minlon-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link BigDecimal }
     */
    public void setMinlon(final BigDecimal value) {
        this.minlon = value;
    }

    /**
     * Ruft den Wert der maxlat-Eigenschaft ab.
     *
     * @return  possible object is {@link BigDecimal }
     */
    public BigDecimal getMaxlat() {
        return maxlat;
    }

    /**
     * Legt den Wert der maxlat-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link BigDecimal }
     */
    public void setMaxlat(final BigDecimal value) {
        this.maxlat = value;
    }

    /**
     * Ruft den Wert der maxlon-Eigenschaft ab.
     *
     * @return  possible object is {@link BigDecimal }
     */
    public BigDecimal getMaxlon() {
        return maxlon;
    }

    /**
     * Legt den Wert der maxlon-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link BigDecimal }
     */
    public void setMaxlon(final BigDecimal value) {
        this.maxlon = value;
    }
}
