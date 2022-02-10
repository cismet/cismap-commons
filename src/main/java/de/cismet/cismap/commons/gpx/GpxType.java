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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * GPX documents contain a metadata header, followed by waypoints, routes, and tracks. You can add your own elements to
 * the extensions section of the GPX document.
 *
 * <p>Java-Klasse für gpxType complex type.</p>
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 *
 * <pre>
   &lt;complexType name="gpxType">
     &lt;complexContent>
       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         &lt;sequence>
           &lt;element name="metadata" type="{http://www.topografix.com/GPX/1/1}metadataType" minOccurs="0"/>
           &lt;element name="wpt" type="{http://www.topografix.com/GPX/1/1}wptType" maxOccurs="unbounded" minOccurs="0"/>
           &lt;element name="rte" type="{http://www.topografix.com/GPX/1/1}rteType" maxOccurs="unbounded" minOccurs="0"/>
           &lt;element name="trk" type="{http://www.topografix.com/GPX/1/1}trkType" maxOccurs="unbounded" minOccurs="0"/>
           &lt;element name="extensions" type="{http://www.topografix.com/GPX/1/1}extensionsType" minOccurs="0"/>
         &lt;/sequence>
         &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="1.1" />
         &lt;attribute name="creator" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
       &lt;/restriction>
     &lt;/complexContent>
   &lt;/complexType>
 * </pre>
 *
 * @version  $Revision$, $Date$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "gpxType",
    propOrder = {
            "metadata",
            "wpt",
            "rte",
            "trk",
            "extensions"
        }
)
public class GpxType {

    //~ Instance fields --------------------------------------------------------

    protected MetadataType metadata;
    protected List<WptType> wpt;
    protected List<RteType> rte;
    protected List<TrkType> trk;
    protected ExtensionsType extensions;
    @XmlAttribute(
        name = "version",
        required = true
    )
    protected String version;
    @XmlAttribute(
        name = "creator",
        required = true
    )
    protected String creator;

    //~ Methods ----------------------------------------------------------------

    /**
     * Ruft den Wert der metadata-Eigenschaft ab.
     *
     * @return  possible object is {@link MetadataType }
     */
    public MetadataType getMetadata() {
        return metadata;
    }

    /**
     * Legt den Wert der metadata-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link MetadataType }
     */
    public void setMetadata(final MetadataType value) {
        this.metadata = value;
    }

    /**
     * Gets the value of the wpt property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the wpt property.</p>
     *
     * <p>For example, to add a new item, do as follows:</p>
     *
     * <pre>
          getWpt().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link WptType }</p>
     *
     * @return  DOCUMENT ME!
     */
    public List<WptType> getWpt() {
        if (wpt == null) {
            wpt = new ArrayList<WptType>();
        }
        return this.wpt;
    }

    /**
     * Gets the value of the rte property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the rte property.</p>
     *
     * <p>For example, to add a new item, do as follows:</p>
     *
     * <pre>
          getRte().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link RteType }</p>
     *
     * @return  DOCUMENT ME!
     */
    public List<RteType> getRte() {
        if (rte == null) {
            rte = new ArrayList<RteType>();
        }
        return this.rte;
    }

    /**
     * Gets the value of the trk property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the trk property.</p>
     *
     * <p>For example, to add a new item, do as follows:</p>
     *
     * <pre>
          getTrk().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link TrkType }</p>
     *
     * @return  DOCUMENT ME!
     */
    public List<TrkType> getTrk() {
        if (trk == null) {
            trk = new ArrayList<TrkType>();
        }
        return this.trk;
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
     * Ruft den Wert der version-Eigenschaft ab.
     *
     * @return  possible object is {@link String }
     */
    public String getVersion() {
        if (version == null) {
            return "1.1";
        } else {
            return version;
        }
    }

    /**
     * Legt den Wert der version-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setVersion(final String value) {
        this.version = value;
    }

    /**
     * Ruft den Wert der creator-Eigenschaft ab.
     *
     * @return  possible object is {@link String }
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Legt den Wert der creator-Eigenschaft fest.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setCreator(final String value) {
        this.creator = value;
    }
}
