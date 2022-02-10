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
 * An ordered sequence of points. (for polygons or polylines, e.g.)
 *
 * <p>Java-Klasse für ptsegType complex type.</p>
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 *
 * <pre>
   &lt;complexType name="ptsegType">
     &lt;complexContent>
       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         &lt;sequence>
           &lt;element name="pt" type="{http://www.topografix.com/GPX/1/1}ptType" maxOccurs="unbounded" minOccurs="0"/>
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
    name = "ptsegType",
    propOrder = { "pt" }
)
public class PtsegType {

    //~ Instance fields --------------------------------------------------------

    protected List<PtType> pt;

    //~ Methods ----------------------------------------------------------------

    /**
     * Gets the value of the pt property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the pt property.</p>
     *
     * <p>For example, to add a new item, do as follows:</p>
     *
     * <pre>
          getPt().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link PtType }</p>
     *
     * @return  DOCUMENT ME!
     */
    public List<PtType> getPt() {
        if (pt == null) {
            pt = new ArrayList<PtType>();
        }
        return this.pt;
    }
}
