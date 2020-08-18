/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.featureservice;

import org.apache.log4j.Logger;

import org.jdom.CDATA;
import org.jdom.Element;

import java.awt.Color;

import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.cismet.cismap.commons.LayerInfoProvider;
import de.cismet.cismap.commons.features.PostgisFeature;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.PostgisFeatureFactory;

import de.cismet.commons.wms.capabilities.Layer;

import de.cismet.tools.ConnectionInfo;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class SimplePostgisFeatureService
        extends AbstractFeatureService<PostgisFeature, SimpleFeatureServiceSqlStatement> implements LayerInfoProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(SimplePostgisFeatureService.class);

    public static final String POSTGIS_FEATURELAYER_TYPE = "simplePostgisFeatureService"; // NOI18N
    public static final HashMap<Integer, Icon> layerIcons = new HashMap<Integer, Icon>();

    static {
        layerIcons.put(
            LAYER_ENABLED_VISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerPostgis.png")));                   // NOI18N
        layerIcons.put(
            LAYER_ENABLED_INVISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerPostgisInvisible.png")));          // NOI18N
        layerIcons.put(
            LAYER_DISABLED_VISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerPostgis.png")));          // NOI18N
        layerIcons.put(
            LAYER_DISABLED_INVISIBLE,
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerPostgisInvisible.png"))); // NOI18N
    }

    //~ Instance fields --------------------------------------------------------

    private SimpleFeatureServiceSqlStatement sqlStatement;
    private ConnectionInfo connectionInfo;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of SimplePostgisFeatureService.
     *
     * @param  spfs  DOCUMENT ME!
     */
    public SimplePostgisFeatureService(final SimplePostgisFeatureService spfs) {
        super(spfs);
        this.connectionInfo = spfs.getConnectionInfo();
        this.sqlStatement = spfs.getQuery();
    }

    /**
     * Creates a new SimplePostgisFeatureService object.
     *
     * @param   element  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public SimplePostgisFeatureService(final Element element) throws Exception {
        super(element);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   element  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public void initFromElement(final Element element) throws Exception {
        super.initFromElement(element);

        if (element.getChild("dbConnectionInfo") != null)                                                      // NOI18N
        {
            final ConnectionInfo newConnectionInfo = new ConnectionInfo(element.getChild("dbConnectionInfo")); // NOI18N
            this.setConnectionInfo(newConnectionInfo);
            if (LOG.isDebugEnabled()) {
                LOG.debug("SimplePostgisFeatureService initialised with connection: \n"
                            + this.getConnectionInfo().getUrl() + ", " + this.getConnectionInfo().getDriver() + ", "
                            + this.getConnectionInfo().getUser());                                             // NOI18N
            }
        } else {
            LOG.error("missing element 'dbConnectionInfo' in xml configuration");                              // NOI18N
        }

        // TODO: SimpleFeatureServiceSqlStatement should implement ConvertableToXML
        this.sqlStatement = new SimpleFeatureServiceSqlStatement(element.getChild("statement").getTextTrim()); // NOI18N
        this.sqlStatement.setAllFields(element.getChild("allFields").getTextTrim());                           // NOI18N
        this.sqlStatement.setOrderBy(element.getChild("orderBy").getTextTrim());                               // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Element toElement() {
        final Element e = super.toElement();

        if (this.sqlStatement != null) {
            final Element stmnt = new Element("statement");        // NOI18N
            stmnt.addContent(new CDATA(sqlStatement.getSqlTemplate()));
            e.addContent(stmnt);
            final Element allFields = new Element("allFields");    // NOI18N
            allFields.addContent(new CDATA(sqlStatement.getAllFields()));
            e.addContent(allFields);
            final Element orderBy = new Element("orderBy");        // NOI18N
            orderBy.addContent(new CDATA(sqlStatement.getOrderBy()));
            e.addContent(orderBy);
        } else {
            LOG.warn("sql statement is null and cannot be saved"); // NOI18N
        }

        if (this.connectionInfo != null) {
            // TODO: ConnectionInfo should implement ConvertableToXML
            final Element connectionElement = new Element("dbConnectionInfo");                                         // NOI18N
            connectionElement.addContent(new Element("driverClass").addContent(this.getConnectionInfo().getDriver())); // NOI18N
            connectionElement.addContent(new Element("dbUrl").addContent(this.getConnectionInfo().getUrl()));          // NOI18N
            connectionElement.addContent(new Element("user").addContent(this.getConnectionInfo().getUser()));          // NOI18N
            connectionElement.addContent(new Element("pass").addContent(this.getConnectionInfo().getPass()));          // NOI18N
            e.addContent(connectionElement);
        } else {
            LOG.warn("connection info is null and cannot be saved");                                                   // NOI18N
        }

        return e;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    protected LayerProperties createLayerProperties() {
        final DefaultLayerProperties defaultLayerProperties = new DefaultLayerProperties();
        defaultLayerProperties.getStyle().setLineColor(new Color(0.6f, 0.6f, 0.6f, 0.7f));
        defaultLayerProperties.getStyle().setFillColor(new Color(0.2f, 0.2f, 0.2f, 0.7f));
        defaultLayerProperties.setQueryType(LayerProperties.QUERYTYPE_UNDEFINED);
        defaultLayerProperties.setFeatureService(this);

        return defaultLayerProperties;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    protected FeatureFactory createFeatureFactory() throws Exception {
        return new PostgisFeatureFactory(this.getLayerProperties(), this.getConnectionInfo(), null, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public SimpleFeatureServiceSqlStatement getQuery() {
        return this.sqlStatement;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sqlStatement  DOCUMENT ME!
     */
    @Override
    public void setQuery(final SimpleFeatureServiceSqlStatement sqlStatement) {
        this.sqlStatement = sqlStatement;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    protected void initConcreteInstance() throws Exception {
        // nothing to do here
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    protected String getFeatureLayerType() {
        return POSTGIS_FEATURELAYER_TYPE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Icon getLayerIcon(final int type) {
        return layerIcons.get(type);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  connectionInfo  DOCUMENT ME!
     */
    public void setConnectionInfo(final ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ConnectionInfo getConnectionInfo() {
        return this.connectionInfo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public SimplePostgisFeatureService clone() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cloning SimplePostgisFeatureService " + this.getName()); // NOI18N
        }
        return new SimplePostgisFeatureService(this);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getLayerURI() {
        return sqlStatement.getSqlTemplate();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getServerURI() {
        return connectionInfo.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isLayerQuerySelected() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  selected  DOCUMENT ME!
     */
    @Override
    public void setLayerQuerySelected(final boolean selected) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isQueryable() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Layer getLayerInformation() {
        return null;
    }
}
