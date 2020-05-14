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

import org.jdom.Element;

import java.util.List;

import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.factory.JDBCFeatureFactory;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class JDBCFeatureService<FT extends FeatureServiceFeature> extends AbstractFeatureService<FT, String> {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(JDBCFeatureService.class);

    //~ Instance fields --------------------------------------------------------

    protected String databasePath;
    protected String tableName;
    protected String query;
//    protected String encryptedDatabasePassword;
//    protected String databaseUser;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JDBCFeatureService object.
     *
     * @param  jfs  DOCUMENT ME!
     */
    public JDBCFeatureService(final JDBCFeatureService jfs) {
        super(jfs);
        this.databasePath = jfs.databasePath;
        this.tableName = jfs.tableName;
        this.query = jfs.query;
    }

    /**
     * Creates a new JDBCFeatureService object.
     *
     * @param   e  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public JDBCFeatureService(final Element e) throws Exception {
        super(e);
    }

    /**
     * Creates a new DocumentFeatureService object.
     *
     * @param   name          DOCUMENT ME!
     * @param   databasePath  documentURI DOCUMENT ME!
     * @param   tableName     documentSize DOCUMENT ME!
     * @param   attributes    DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public JDBCFeatureService(final String name,
            final String databasePath,
            final String tableName,
            final List<FeatureServiceAttribute> attributes) throws Exception {
        super(name, attributes);
        this.databasePath = databasePath;
        substituteHome();
        this.tableName = tableName;
        if (LOG.isInfoEnabled()) {
            LOG.info("creating new JDBCFeatureService from path: " + this.databasePath); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void substituteHome() {
        final String home = System.getProperty("user.home");         // NOI18N
        final String fileSep = System.getProperty("file.separator"); // NOI18N
        databasePath = databasePath.replace("~", home);
    }

    @Override
    protected LayerProperties createLayerProperties() {
        final DefaultLayerProperties defaultLayerProperties = new DefaultLayerProperties();
        defaultLayerProperties.setIdExpression(null, LayerProperties.EXPRESSIONTYPE_UNDEFINED);
        defaultLayerProperties.setFeatureService(this);

        return defaultLayerProperties;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Element toElement() {
        final Element parentElement = super.toElement();
        final Element daPath = new Element("databasePath"); // NOI18N
        daPath.setText(databasePath);
        final Element tabName = new Element("tableName");   // NOI18N
        tabName.setText(tableName);
        final Element queryElement = new Element("query");  // NOI18N
        queryElement.setText(query);
        parentElement.addContent(queryElement);
        parentElement.addContent(daPath);
        parentElement.addContent(tabName);
        return parentElement;
    }

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
        this.setDatabasePath(element.getChildText("databasePath").trim()); // NOI18N
        this.setTableName(element.getChildText("tableName").trim());       // NOI18N
        this.setQuery(element.getChildText("query").trim());               // NOI18N
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public void setQuery(final String query) {
        this.query = query;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the databasePath
     */
    public String getDatabasePath() {
        return databasePath;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  databasePath  the databasePath to set
     */
    public void setDatabasePath(final String databasePath) {
        this.databasePath = databasePath;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  tableName  the tableName to set
     */
    public void setTableName(final String tableName) {
        this.tableName = tableName;
        if (featureFactory != null) {
            ((JDBCFeatureFactory)featureFactory).setTableName(tableName);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  crs  DOCUMENT ME!
     */
    public void setCrs(final Crs crs) {
        ((JDBCFeatureFactory)featureFactory).setCrs(crs);
    }
}
