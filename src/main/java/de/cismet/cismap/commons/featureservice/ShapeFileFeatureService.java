/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.awt.event.ActionEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import java.net.URI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.exceptions.ShapeFileImportAborted;
import de.cismet.cismap.commons.features.ShapeFeature;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.ShapeFeatureFactory;
import de.cismet.cismap.commons.featureservice.style.BasicStyle;
import de.cismet.cismap.commons.featureservice.style.Style;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.interaction.DefaultQueryButtonAction;

import static de.cismet.cismap.commons.featureservice.AbstractFeatureService.SQL_QUERY_BUTTONS;

/**
 * DOCUMENT ME!
 *
 * @author   spuhl
 * @version  $Revision$, $Date$
 */
//Todo optimieren wann welche Features geladen werden z.B. bei 150 MB file
public class ShapeFileFeatureService extends DocumentFeatureService<ShapeFeature, String> {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ShapeFileFeatureService.class);
    public static final Map<Integer, Icon> layerIcons = new HashMap<Integer, Icon>();
    public static final String SHAPE_FEATURELAYER_TYPE = "ShapeFeatureServiceLayer"; // NOI18N
    public static final List<DefaultQueryButtonAction> SQL_QUERY_BUTTONS = new ArrayList<DefaultQueryButtonAction>();

    static {
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("=="));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("!="));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction(">"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction(">="));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("&&", "And"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("<"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("<="));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("||", "Or"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("()") {

                {
                    posCorrection = -1;
                }

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (queryTextArea.getSelectionEnd() == 0) {
                        super.actionPerformed(e);
                    } else {
                        final int start = queryTextArea.getSelectionStart();
                        final int end = queryTextArea.getSelectionEnd();
                        queryTextArea.insert("(", start);
                        queryTextArea.insert(")", end + 1);
                        // jTextArea1.setCaretPosition(end + 2);
                        if (start == end) {
                            CorrectCarret(posCorrection);
                        } else {
                            CorrectCarret((short)2);
                        }
                    }
                }
            });
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("!", "Not"));
        SQL_QUERY_BUTTONS.add(new DefaultQueryButtonAction("null"));
        layerIcons.put(
            LAYER_ENABLED_VISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerShape.png")));                   // NOI18N
        layerIcons.put(
            LAYER_ENABLED_INVISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerShapeInvisible.png")));          // NOI18N
        layerIcons.put(
            LAYER_DISABLED_VISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerShape.png")));          // NOI18N
        layerIcons.put(
            LAYER_DISABLED_INVISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerShapeInvisible.png"))); // NOI18N
    }

    //~ Instance fields --------------------------------------------------------

    protected List<DefaultQueryButtonAction> queryButtons = new ArrayList<DefaultQueryButtonAction>(SQL_QUERY_BUTTONS);

    private boolean noGeometryRecognised = false;
    private boolean errorInGeometryFound = false;
    private boolean fileNotFound = false;
    private String geometryType = UNKNOWN;
    private String shapeCrs;
    private Crs crs;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeFileFeatureService object.
     *
     * @param   e  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ShapeFileFeatureService(final Element e) throws Exception {
        super(e);
        checkFile();
    }

    /**
     * Creates a new ShapeFileFeatureService object.
     *
     * @param   name          DOCUMENT ME!
     * @param   documentURI   DOCUMENT ME!
     * @param   documentSize  DOCUMENT ME!
     * @param   attributes    DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ShapeFileFeatureService(final String name,
            final URI documentURI,
            final long documentSize,
            final List<FeatureServiceAttribute> attributes) throws Exception {
        super(name, documentURI, documentSize, attributes);
        this.maxFeatureCount = Integer.MAX_VALUE;
        checkFile();
    }

    /**
     * Creates a new ShapeFileFeatureService object.
     *
     * @param  sfs  DOCUMENT ME!
     */
    protected ShapeFileFeatureService(final ShapeFileFeatureService sfs) {
        super(sfs);
        this.geometryType = sfs.geometryType;
        checkFile();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void checkFile() {
        final File file = new File(documentURI);

        if (!file.exists()) {
            fileNotFound = true;
        } else {
            fileNotFound = false;
        }
    }

    @Override
    public Icon getLayerIcon(final int type) {
        return layerIcons.get(type);
    }

    @Override
    protected LayerProperties createLayerProperties() {
        final DefaultLayerProperties defaultLayerProperties = new DefaultLayerProperties();
        // defaultLayerProperties.setIdExpression("app:ID", LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
        // IDs of documents can be autogenerated (faster)!
        defaultLayerProperties.setIdExpression(null, LayerProperties.EXPRESSIONTYPE_UNDEFINED);
        defaultLayerProperties.setFeatureService(this);
        return defaultLayerProperties;
    }

    @Override
    protected FeatureFactory createFeatureFactory() throws Exception {
        try {
            final ShapeFeatureFactory sff = new ShapeFeatureFactory(this.getLayerProperties(),
                    this.getDocumentURI(),
                    this.maxSupportedFeatureCount,
                    this.layerInitWorker,
                    parseSLD(getSLDDefiniton()),
                    shapeCrs);
            noGeometryRecognised = sff.isNoGeometryRecognised();
            errorInGeometryFound = sff.isErrorInGeometryFound();
            geometryType = sff.getGeometryType();
            if (crs != null) {
                sff.setCrs(crs);
            }

            final String sldString = sff.getSldDefinition();

            if ((sldString != null) && !sldString.isEmpty()) {
                if (sldString.contains(Style.STYLE_ELEMENT)) {
                    final SAXBuilder saxBuilder = new SAXBuilder(false);
                    final StringReader stringReader = new StringReader(sldString);
                    final Document document = saxBuilder.build(stringReader);
                    final BasicStyle style = new BasicStyle(document.getRootElement());
                    if (getLayerProperties() != null) {
                        getLayerProperties().setStyle(style);
                    }
                } else {
                    sldDefinition = sldString;
                    final Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles = parseSLD(
                            new StringReader(
                                sldString));

                    if ((styles != null) && !styles.isEmpty()) {
                        sff.setSLDStyle(styles);
                    }
                }
            }

            return sff;
        } catch (ShapeFileImportAborted e) {
            CismapBroker.getInstance().getMappingComponent().getMappingModel().removeLayer(this);
            throw e;
        }
    }

    @Override
    public String getQuery() {
        // LOG.warn("unexpected call to getQuery, not supported by this service");
        return null;
    }

    @Override
    public void setQuery(final String query) {
        LOG.warn("unexpected call to setQuery, not supported by this service:\n" + query); // NOI18N
    }

    @Override
    protected void initConcreteInstance() throws Exception {
        // nothing to do here
    }

    @Override
    public void setDocumentURI(final URI documentURI) {
        super.setDocumentURI(documentURI);
        if (this.getFeatureFactory() != null) {
            ((ShapeFeatureFactory)this.getFeatureFactory()).setDocumentURI(documentURI);
        }
        checkFile();
    }

    @Override
    protected String getFeatureLayerType() {
        return SHAPE_FEATURELAYER_TYPE;
    }

    @Override
    public Object clone() {
        LOG.info("cloning service " + this.getName()); // NOI18N
        return new ShapeFileFeatureService(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the noGeometryRecognised
     */
    public boolean isNoGeometryRecognised() {
        return noGeometryRecognised;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the errorInGeometryFound
     */
    public boolean isErrorInGeometryFound() {
        return errorInGeometryFound;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the fileNotFound
     */
    public boolean isFileNotFound() {
        return fileNotFound;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  crs  DOCUMENT ME!
     */
    public void setCrs(final Crs crs) {
        if (featureFactory != null) {
            ((ShapeFeatureFactory)featureFactory).setCrs(crs);
        } else {
            this.crs = crs;
        }
    }

    @Override
    public void setLayerProperties(final LayerProperties layerProperties, final boolean refreshFeatures) {
        super.setLayerProperties(layerProperties, refreshFeatures);

        if (((sldDefinition == null) || sldDefinition.isEmpty()) && (layerProperties != null)
                    && (layerProperties.getStyle() != null)) {
            final XMLOutputter out = new XMLOutputter(Format.getPrettyFormat()); // NOI18N
            final StringWriter writer = new StringWriter();
            try {
                final Element e = layerProperties.getStyle().toElement();
                out.output(e, writer);
                writeStyleFile(writer.toString());
            } catch (IOException e) {
                LOG.error("Error while saving style", e);
            } finally {
                try {
                    writer.close();
                } catch (IOException ex) {
                    LOG.error("Error while closing string writer", ex);
                }
            }
        }
    }

    /**
     * The query buttons, which should be used by the query search.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public List<DefaultQueryButtonAction> getQueryButtons() {
        return queryButtons;
    }

    @Override
    public void setSLDInputStream(final String inputStream) {
        super.setSLDInputStream(inputStream);

        // save the sld in a separate file with the ending sld
        if ((inputStream != null) && !inputStream.isEmpty()) {
            writeStyleFile(inputStream);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  content  DOCUMENT ME!
     */
    private void writeStyleFile(final String content) {
        String filename = null;

        if (this.documentURI.getPath().toLowerCase().endsWith(".shp")) {
            filename = this.documentURI.getPath().substring(0, this.documentURI.getPath().length() - 4);
            filename = filename + ".sld";
        } else if (!this.documentURI.getPath().contains(".")) {
            filename = this.documentURI.getPath() + ".sld";
        }

        if (filename != null) {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(filename));
                writer.write(content);
                writer.close();
            } catch (Exception e) {
                LOG.error("Error while writing sld file", e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ex) {
                        LOG.error("Error while closing sld file", ex);
                    }
                }
            }
        }
    }

    @Override
    public boolean isEditable() {
        return true;
    }

// breaks DocumentFeatureServiceFactory
//  @Override
//  protected String getFeatureLayerType()
//  {
//    return SHAPE_FEATURELAYER_TYPE;
//  }

    @Override
    public String getGeometryType() {
        return geometryType; // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initFromElement(final Element element) throws Exception {
        super.initFromElement(element);

        if (element.getAttributeValue("shapeCrs") != null) {
            final String crs = element.getAttributeValue("shapeCrs");
            shapeCrs = CismapBroker.getInstance().crsFromCode(crs).getCode();
        }
    }

    @Override
    public Element toElement() {
        final Element e = super.toElement();
        String crs = null;

        if (getFeatureFactory() != null) {
            crs = ((ShapeFeatureFactory)getFeatureFactory()).getShapeCrs();
        } else if (shapeCrs != null) {
            crs = shapeCrs;
        }

        if (crs != null) {
            e.setAttribute("shapeCrs", crs); // NOI18N
        }

        return e;
    }
}
