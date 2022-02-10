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
import javax.xml.bind.annotation.XmlType;

/**
 * A Track Segment holds a list of Track Points which are logically connected in order. To represent a single GPS track
 * where GPS reception was lost, or the GPS receiver was turned off, start a new Track Segment for each continuous span
 * of track data.
 *
 * <p>Java-Klasse für trksegType complex type.</p>
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 *
 * <pre>
   &lt;complexType name="trksegType">
     &lt;complexContent>
       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         &lt;sequence>
           &lt;element name="trkpt" type="{http://www.topografix.com/GPX/1/1}wptType" maxOccurs="unbounded" minOccurs="0"/>
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
    name = "trksegType",
    propOrder = {
            "trkpt",
            "extensions"
        }
)
public class TrksegType {

    //~ Instance fields --------------------------------------------------------

    protected List<WptType> trkpt;
    protected ExtensionsType extensions;

    //~ Methods ----------------------------------------------------------------

    /**
     * Gets the value of the trkpt property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the trkpt property.</p>
     *
     * <p>For example, to add a new item, do as follows:</p>
     *
     * <pre>
          getTrkpt().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link WptType }</p>
     *
     * @return  DOCUMENT ME!
     */
    public List<WptType> getTrkpt() {
        if (trkpt == null) {
            trkpt = new ArrayList<WptType>();
        }
        return this.trkpt;
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
