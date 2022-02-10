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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the
 * de.cismet.cismap.commons.gpx package.
 *
 * <p>An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content.
 * The Java representation of XML content can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory methods for each of these are provided in
 * this class.</p>
 *
 * @version  $Revision$, $Date$
 */
@XmlRegistry
public class ObjectFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static final QName _Gpx_QNAME = new QName("http://www.topografix.com/GPX/1/1", "gpx");

    //~ Constructors -----------------------------------------------------------

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
     * de.cismet.cismap.commons.gpx
     */
    public ObjectFactory() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Create an instance of {@link GpxType }.
     *
     * @return  DOCUMENT ME!
     */
    public GpxType createGpxType() {
        return new GpxType();
    }

    /**
     * Create an instance of {@link TrkType }.
     *
     * @return  DOCUMENT ME!
     */
    public TrkType createTrkType() {
        return new TrkType();
    }

    /**
     * Create an instance of {@link BoundsType }.
     *
     * @return  DOCUMENT ME!
     */
    public BoundsType createBoundsType() {
        return new BoundsType();
    }

    /**
     * Create an instance of {@link CopyrightType }.
     *
     * @return  DOCUMENT ME!
     */
    public CopyrightType createCopyrightType() {
        return new CopyrightType();
    }

    /**
     * Create an instance of {@link RteType }.
     *
     * @return  DOCUMENT ME!
     */
    public RteType createRteType() {
        return new RteType();
    }

    /**
     * Create an instance of {@link TrksegType }.
     *
     * @return  DOCUMENT ME!
     */
    public TrksegType createTrksegType() {
        return new TrksegType();
    }

    /**
     * Create an instance of {@link EmailType }.
     *
     * @return  DOCUMENT ME!
     */
    public EmailType createEmailType() {
        return new EmailType();
    }

    /**
     * Create an instance of {@link PtType }.
     *
     * @return  DOCUMENT ME!
     */
    public PtType createPtType() {
        return new PtType();
    }

    /**
     * Create an instance of {@link PtsegType }.
     *
     * @return  DOCUMENT ME!
     */
    public PtsegType createPtsegType() {
        return new PtsegType();
    }

    /**
     * Create an instance of {@link LinkType }.
     *
     * @return  DOCUMENT ME!
     */
    public LinkType createLinkType() {
        return new LinkType();
    }

    /**
     * Create an instance of {@link WptType }.
     *
     * @return  DOCUMENT ME!
     */
    public WptType createWptType() {
        return new WptType();
    }

    /**
     * Create an instance of {@link ExtensionsType }.
     *
     * @return  DOCUMENT ME!
     */
    public ExtensionsType createExtensionsType() {
        return new ExtensionsType();
    }

    /**
     * Create an instance of {@link PersonType }.
     *
     * @return  DOCUMENT ME!
     */
    public PersonType createPersonType() {
        return new PersonType();
    }

    /**
     * Create an instance of {@link MetadataType }.
     *
     * @return  DOCUMENT ME!
     */
    public MetadataType createMetadataType() {
        return new MetadataType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GpxType }{@code >}}.
     *
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @XmlElementDecl(
        namespace = "http://www.topografix.com/GPX/1/1",
        name = "gpx"
    )
    public JAXBElement<GpxType> createGpx(final GpxType value) {
        return new JAXBElement<GpxType>(_Gpx_QNAME, GpxType.class, null, value);
    }
}
