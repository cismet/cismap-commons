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

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * rte represents route - an ordered list of waypoints representing a series of turn points leading to a destination.
 *
 * <p>Java-Klasse für rteType complex type.</p>
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 *
 * <pre>
   &lt;complexType name="rteType">
     &lt;complexContent>
       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         &lt;sequence>
           &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
           &lt;element name="cmt" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
           &lt;element name="desc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
           &lt;element name="src" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
           &lt;element name="link" type="{http://www.topografix.com/GPX/1/1}linkType" maxOccurs="unbounded" minOccurs="0"/>
           &lt;element name="number" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
           &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
           &lt;element name="extensions" type="{http://www.topografix.com/GPX/1/1}extensionsType" minOccurs="0"/>
           &lt;element name="rtept" type="{http://www.topografix.com/GPX/1/1}wptType" maxOccurs="unbounded" minOccurs="0"/>
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
    name = "rteType",
    propOrder = {
            "name",
            "cmt",
            "desc",
            "src",
            "link",
            "number",
            "type",
            "extensions",
            "rtept"
        }
)
public class RteType {

    //~ Instance fields --------------------------------------------------------

    protected String name;
    protected String cmt;
    protected String desc;
    protected String src;
    protected List<LinkType> link;
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger number;
    protected String type;
    protected ExtensionsType extensions;
    protected List<WptType> rtept;

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
     * Ruft den Wert der cmt-Eigenschaft ab.
     *
     * @return  possible object is {@link String }
     */
    public String getCmt() {
        return cmt;
    }

    /**
     * Legt den Wert der cmt-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setCmt(final String value) {
        this.cmt = value;
    }

    /**
     * Ruft den Wert der desc-Eigenschaft ab.
     *
     * @return  possible object is {@link String }
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Legt den Wert der desc-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setDesc(final String value) {
        this.desc = value;
    }

    /**
     * Ruft den Wert der src-Eigenschaft ab.
     *
     * @return  possible object is {@link String }
     */
    public String getSrc() {
        return src;
    }

    /**
     * Legt den Wert der src-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setSrc(final String value) {
        this.src = value;
    }

    /**
     * Gets the value of the link property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the link property.</p>
     *
     * <p>For example, to add a new item, do as follows:</p>
     *
     * <pre>
          getLink().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link LinkType }</p>
     *
     * @return  DOCUMENT ME!
     */
    public List<LinkType> getLink() {
        if (link == null) {
            link = new ArrayList<LinkType>();
        }
        return this.link;
    }

    /**
     * Ruft den Wert der number-Eigenschaft ab.
     *
     * @return  possible object is {@link BigInteger }
     */
    public BigInteger getNumber() {
        return number;
    }

    /**
     * Legt den Wert der number-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link BigInteger }
     */
    public void setNumber(final BigInteger value) {
        this.number = value;
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
     * Ruft den Wert der extensions-Eigenschaft ab.
     *
     * @return  possible object is {@link ExtensionsType }
     */
    public ExtensionsType getExtensions() {
        return extensions;
    }

    /**
     * Legt den Wert der extensions-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link ExtensionsType }
     */
    public void setExtensions(final ExtensionsType value) {
        this.extensions = value;
    }

    /**
     * Gets the value of the rtept property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the rtept property.</p>
     *
     * <p>For example, to add a new item, do as follows:</p>
     *
     * <pre>
          getRtept().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link WptType }</p>
     *
     * @return  DOCUMENT ME!
     */
    public List<WptType> getRtept() {
        if (rtept == null) {
            rtept = new ArrayList<WptType>();
        }
        return this.rtept;
    }
}
