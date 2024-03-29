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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Information about the GPX file, author, and copyright restrictions goes in the metadata section. Providing rich,
 * meaningful information about your GPX files allows others to search for and use your GPS data.
 *
 * <p>Java-Klasse für metadataType complex type.</p>
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 *
 * <pre>
   &lt;complexType name="metadataType">
     &lt;complexContent>
       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         &lt;sequence>
           &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
           &lt;element name="desc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
           &lt;element name="author" type="{http://www.topografix.com/GPX/1/1}personType" minOccurs="0"/>
           &lt;element name="copyright" type="{http://www.topografix.com/GPX/1/1}copyrightType" minOccurs="0"/>
           &lt;element name="link" type="{http://www.topografix.com/GPX/1/1}linkType" maxOccurs="unbounded" minOccurs="0"/>
           &lt;element name="time" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
           &lt;element name="keywords" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
           &lt;element name="bounds" type="{http://www.topografix.com/GPX/1/1}boundsType" minOccurs="0"/>
           &lt;element name="extensions" type="{http://www.topografix.com/GPX/1/1}extensionsType" minOccurs="0"/>
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
    name = "metadataType",
    propOrder = {
            "name",
            "desc",
            "author",
            "copyright",
            "link",
            "time",
            "keywords",
            "bounds",
            "extensions"
        }
)
public class MetadataType {

    //~ Instance fields --------------------------------------------------------

    protected String name;
    protected String desc;
    protected PersonType author;
    protected CopyrightType copyright;
    protected List<LinkType> link;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar time;
    protected String keywords;
    protected BoundsType bounds;
    protected ExtensionsType extensions;

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
     * Ruft den Wert der author-Eigenschaft ab.
     *
     * @return  possible object is {@link PersonType }
     */
    public PersonType getAuthor() {
        return author;
    }

    /**
     * Legt den Wert der author-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link PersonType }
     */
    public void setAuthor(final PersonType value) {
        this.author = value;
    }

    /**
     * Ruft den Wert der copyright-Eigenschaft ab.
     *
     * @return  possible object is {@link CopyrightType }
     */
    public CopyrightType getCopyright() {
        return copyright;
    }

    /**
     * Legt den Wert der copyright-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link CopyrightType }
     */
    public void setCopyright(final CopyrightType value) {
        this.copyright = value;
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
     * Ruft den Wert der keywords-Eigenschaft ab.
     *
     * @return  possible object is {@link String }
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Legt den Wert der keywords-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setKeywords(final String value) {
        this.keywords = value;
    }

    /**
     * Ruft den Wert der bounds-Eigenschaft ab.
     *
     * @return  possible object is {@link BoundsType }
     */
    public BoundsType getBounds() {
        return bounds;
    }

    /**
     * Legt den Wert der bounds-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link BoundsType }
     */
    public void setBounds(final BoundsType value) {
        this.bounds = value;
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
}
