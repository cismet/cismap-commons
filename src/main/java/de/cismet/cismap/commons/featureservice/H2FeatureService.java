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

import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.wrapper.ConnectionWrapper;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import org.openide.util.NbBundle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import java.net.URI;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.featureservice.style.BasicStyle;
import de.cismet.cismap.commons.featureservice.style.Style;
import de.cismet.cismap.commons.gui.attributetable.LockFromSameUserAlreadyExistsException;
import de.cismet.cismap.commons.interaction.CismapBroker;

import static de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory.LR_META_TABLE_NAME;

/**
 * A service, that uses the internal db as data source.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class H2FeatureService extends JDBCFeatureService<JDBCFeature> {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(H2FeatureService.class);
    public static final Map<String, Icon> layerIcons = new HashMap<String, Icon>();
    public static final String H2_FEATURELAYER_TYPE = "H2FeatureServiceLayer"; // NOI18N
    private static final String LOCK_FEATURE = "INSERT INTO \"" + H2FeatureServiceFactory.LOCK_TABLE_NAME
                + "\" (\"id\", \"table\", \"lock_time\") VALUES(%s, '%s', now())";
    private static final String UPDATE_SLD = "UPDATE \"" + H2FeatureServiceFactory.SLD_TABLE_NAME
                + "\" set \"sld\" = '%s' where \"table\" = '%s'";
    private static final String INSERT_SLD = "INSERT INTO \"" + H2FeatureServiceFactory.SLD_TABLE_NAME
                + "\" (\"table\", \"sld\") VALUES('%s', '%s')";
    private static final String READ_SLD = "SELECT \"sld\" from \"" + H2FeatureServiceFactory.SLD_TABLE_NAME
                + "\" where \"table\" = '%s'";
    private static final String CHECK_LOCKED_FEATURE = "SELECT \"lock_time\" FROM \""
                + H2FeatureServiceFactory.LOCK_TABLE_NAME
                + "\" where (\"id\" = %s OR \"id\" is null) and \"table\" = '%s'";
    private static final String CHECK_LOCKED_FEATURE_TABLE = "SELECT \"lock_time\" FROM \""
                + H2FeatureServiceFactory.LOCK_TABLE_NAME + "\" where \"table\" = '%s'";
    private static final String CLEAR_LOCKS = "DELETE FROM \"" + H2FeatureServiceFactory.LOCK_TABLE_NAME + "\"";
    private static final String UNLOCK = "DELETE FROM \"" + H2FeatureServiceFactory.LOCK_TABLE_NAME
                + "\" where \"id\" = %s and \"table\" = '%s'";
    private static final String UNLOCK_TABLE = "DELETE FROM \"" + H2FeatureServiceFactory.LOCK_TABLE_NAME
                + "\" where \"table\" = '%s'";
    private static final String DELETE_FROM_TABLE = "DELETE FROM \"%s\" where table = '%s';";
    private static final String DELETE_FROM_LOCK_TABLE = "DELETE FROM \"%s\" where \"table\" = '%s';";
    private static final String DROP_TABLE = "DROP TABLE \"%s\";";
    private static final String DROP_SEQUENCE = "DROP SEQUENCE IF EXISTS \"%s_seq\";";

    static {
        layerIcons.put(
            String.valueOf(LAYER_ENABLED_VISIBLE),
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerShape.png")));                   // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_ENABLED_INVISIBLE),
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerShapeInvisible.png")));          // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_DISABLED_VISIBLE),
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerShape.png")));          // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_DISABLED_INVISIBLE),
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerShapeInvisible.png"))); // NOI18N

        layerIcons.put(
            String.valueOf(LAYER_ENABLED_VISIBLE)
                    + ";shp",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerShape.png")));                   // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_ENABLED_INVISIBLE)
                    + ";shp",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerShapeInvisible.png")));          // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_DISABLED_VISIBLE)
                    + ";shp",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerShape.png")));          // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_DISABLED_INVISIBLE)
                    + ";shp",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerShapeInvisible.png"))); // NOI18N

        layerIcons.put(
            String.valueOf(LAYER_ENABLED_VISIBLE)
                    + ";dbf",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerDbf.png")));                   // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_ENABLED_INVISIBLE)
                    + ";dbf",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerDbfInvisible.png")));          // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_DISABLED_VISIBLE)
                    + ";dbf",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerDbf.png")));          // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_DISABLED_INVISIBLE)
                    + ";dbf",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerDbfInvisible.png"))); // NOI18N

        layerIcons.put(
            String.valueOf(LAYER_ENABLED_VISIBLE)
                    + ";xy",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerXy.png")));                   // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_ENABLED_INVISIBLE)
                    + ";xy",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerXyInvisible.png")));          // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_DISABLED_VISIBLE)
                    + ";xy",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerXy.png")));          // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_DISABLED_INVISIBLE)
                    + ";xy",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerXyInvisible.png"))); // NOI18N

        layerIcons.put(
            String.valueOf(LAYER_ENABLED_VISIBLE)
                    + ";r",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerR.png")));                     // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_ENABLED_INVISIBLE)
                    + ";r",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerRInvisible.png")));            // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_DISABLED_VISIBLE)
                    + ";r",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerR.png")));            // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_DISABLED_INVISIBLE)
                    + ";r",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerRInvisible.png")));   // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_ENABLED_VISIBLE)
                    + ";csv",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerCsv.png")));                   // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_ENABLED_INVISIBLE)
                    + ";csv",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerCsvInvisible.png")));          // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_DISABLED_VISIBLE)
                    + ";csv",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerCsv.png")));          // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_DISABLED_INVISIBLE)
                    + ";csv",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerCsvInvisible.png"))); // NOI18N

        layerIcons.put(
            String.valueOf(LAYER_ENABLED_VISIBLE)
                    + ";dxf",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerDxf.png")));                   // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_ENABLED_INVISIBLE)
                    + ";dxf",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/layerDxfInvisible.png")));          // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_DISABLED_VISIBLE)
                    + ";dxf",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerDxf.png")));          // NOI18N
        layerIcons.put(
            String.valueOf(LAYER_DISABLED_INVISIBLE)
                    + ";dxf",
            new ImageIcon(
                AbstractFeatureService.class.getResource(
                    "/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerDxfInvisible.png"))); // NOI18N
    }

    //~ Instance fields --------------------------------------------------------

    private List<FeatureServiceFeature> features;
    private File shapeFile;
    private boolean initialised = true;
    private boolean tableNotFound = false;
    private List<String> orderedAttributeNames = null;
    private String geometryType = UNKNOWN;
    private String tableFormat = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new H2FeatureService object.
     *
     * @param   e  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public H2FeatureService(final Element e) throws Exception {
        super(e);
        checkTable();
    }

    /**
     * Creates a new H2FeatureService object.
     *
     * @param   name          DOCUMENT ME!
     * @param   databasePath  DOCUMENT ME!
     * @param   tableName     DOCUMENT ME!
     * @param   attributes    DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public H2FeatureService(final String name,
            final String databasePath,
            final String tableName,
            final List<FeatureServiceAttribute> attributes) throws Exception {
        this(name, databasePath, tableName, attributes, null, null);
    }

    /**
     * Creates a new H2FeatureService object.
     *
     * @param   name          DOCUMENT ME!
     * @param   databasePath  DOCUMENT ME!
     * @param   tableName     DOCUMENT ME!
     * @param   attributes    DOCUMENT ME!
     * @param   shapeFile     DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public H2FeatureService(final String name,
            final String databasePath,
            final String tableName,
            final List<FeatureServiceAttribute> attributes,
            final File shapeFile) throws Exception {
        this(name, databasePath, tableName, attributes, shapeFile, null);
    }

    /**
     * Creates a new H2FeatureService object.
     *
     * @param   name          DOCUMENT ME!
     * @param   databasePath  DOCUMENT ME!
     * @param   tableName     DOCUMENT ME!
     * @param   attributes    DOCUMENT ME!
     * @param   features      DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public H2FeatureService(final String name,
            final String databasePath,
            final String tableName,
            final List<FeatureServiceAttribute> attributes,
            final List<FeatureServiceFeature> features) throws Exception {
        this(name, databasePath, tableName, attributes, null, features);
    }

    /**
     * Creates a new H2FeatureService object.
     *
     * @param   name          The name of the service
     * @param   databasePath  the database path of the service
     * @param   tableName     the name of the service table
     * @param   attributes    the feature service attributes of the service
     * @param   shapeFile     the shape file to import
     * @param   features      the features to import
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public H2FeatureService(final String name,
            final String databasePath,
            final String tableName,
            final List<FeatureServiceAttribute> attributes,
            final File shapeFile,
            final List<FeatureServiceFeature> features) throws Exception {
        this(name, databasePath, tableName, attributes, shapeFile, features, null);
    }

    /**
     * Creates a new H2FeatureService object.
     *
     * @param   name                   The name of the service
     * @param   databasePath           the database path of the service
     * @param   tableName              the name of the service table
     * @param   attributes             the feature service attributes of the service
     * @param   shapeFile              the shape file to import
     * @param   features               the features to import
     * @param   orderedAttributeNames  the order of the service attributes. Can be null for an arbitrary attribute
     *                                 order.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public H2FeatureService(final String name,
            final String databasePath,
            final String tableName,
            final List<FeatureServiceAttribute> attributes,
            final File shapeFile,
            final List<FeatureServiceFeature> features,
            final List<String> orderedAttributeNames) throws Exception {
        this(name, databasePath, tableName, attributes, shapeFile, features, null, null);
    }

    /**
     * Creates a new H2FeatureService object.
     *
     * @param   name                   The name of the service
     * @param   databasePath           the database path of the service
     * @param   tableName              the name of the service table
     * @param   attributes             the feature service attributes of the service
     * @param   shapeFile              the shape file to import
     * @param   features               the features to import
     * @param   orderedAttributeNames  the order of the service attributes. Can be null for an arbitrary attribute
     *                                 order.
     * @param   format                 DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public H2FeatureService(final String name,
            final String databasePath,
            final String tableName,
            final List<FeatureServiceAttribute> attributes,
            final File shapeFile,
            final List<FeatureServiceFeature> features,
            final List<String> orderedAttributeNames,
            final String format) throws Exception {
        super(name, databasePath, tableName, attributes);
        this.shapeFile = shapeFile;
        this.features = features;
        this.orderedAttributeNames = orderedAttributeNames;
        this.tableFormat = format;
    }

    /**
     * Creates a new ShapeFileFeatureService object.
     *
     * @param  hfs  DOCUMENT ME!
     */
    protected H2FeatureService(final H2FeatureService hfs) {
        super(hfs);
        checkTable();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Checks, if the table of the layer exists within the db and writes the result to the tableNotFound variable.
     */
    private void checkTable() {
        tableNotFound = !tableAlreadyExists(tableName);
    }

    /**
     * Import the given shape file to the database and assign it to this layer.
     *
     * @param  shapeFileUri  the uri to the shape file
     */
    public void createFromShapeFile(final URI shapeFileUri) {
        this.shapeFile = new File(shapeFileUri);

        if (this.getFeatureFactory() != null) {
            ((H2FeatureServiceFactory)this.getFeatureFactory()).setFile(this.shapeFile);
        } else {
            try {
                initAndWait();
            } catch (Exception e) {
                LOG.error("Error while initialising the H2FeatureService with a new shape file", e);
            }
        }
        checkTable();
    }

    @Override
    public void setSLDInputStream(final String inputStream) {
        super.setSLDInputStream(inputStream);

        // save the sld in a separate file with the ending sld
        if ((inputStream != null) && !inputStream.isEmpty()) {
            saveStyleFile(inputStream);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getTableNameStemFromTableName(final String tableName) {
        String tableNameStem = tableName;

        if (tableNameStem.contains("_")) {
            tableNameStem = tableNameStem.substring(0, tableNameStem.lastIndexOf("_"));
        }

        return tableNameStem;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  content  DOCUMENT ME!
     */
    private void saveStyleFile(final String content) {
        ConnectionWrapper conn = null;
        Statement st = null;
        final String tableNameStem = getTableNameStemFromTableName(tableName);

        try {
            Class.forName("org.h2.Driver");
            conn = H2FeatureServiceFactory.getDBConnection(H2FeatureServiceFactory.DB_NAME);
            st = conn.createStatement();

            final int rowsAffected = st.executeUpdate(String.format(UPDATE_SLD, content, tableNameStem));

            if (rowsAffected == 0) {
                st.executeUpdate(String.format(INSERT_SLD, tableNameStem, content));
            }
        } catch (Exception e) {
            LOG.error("Error while saving style definition", e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close statement", ex);
                }
            }
//            if (conn != null) {
//                try {
//                    conn.close();
//                } catch (SQLException ex) {
//                    LOG.warn("Cannot close connection", ex);
//                }
//            }
        }
    }

    @Override
    protected FeatureFactory createFeatureFactory() throws Exception {
        H2FeatureServiceFactory f;
        if (features != null) {
            f = new H2FeatureServiceFactory(
                    name,
                    databasePath,
                    tableName,
                    features,
                    orderedAttributeNames,
                    layerInitWorker,
                    parseSLD(getSLDDefiniton()));
            checkTable();
            geometryType = f.getGeometryType();
        } else {
            try {
                f = new H2FeatureServiceFactory(
                        name,
                        databasePath,
                        tableName,
                        shapeFile,
                        layerInitWorker,
                        parseSLD(getSLDDefiniton()));
            } catch (Exception e) {
                CismapBroker.getInstance().getMappingComponent().getMappingModel().removeLayer(this);
                removeTableIfExists(tableName);
                throw e;
            }
            checkTable();
            geometryType = f.getGeometryType();
        }

        if (tableFormat != null) {
            f.setTableFormat(tableFormat);
        }

        if (getLayerProperties() != null) {
            setTheFactorySpecificLayerProperties((DefaultLayerProperties)getLayerProperties(), f);
        }
        tableFormat = getTableFormat(tableName, H2FeatureServiceFactory.DB_NAME);

        final String sldString = getSldDefinition();

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
                    f.setSLDStyle(styles);
                }
            }
        }

        return f;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getSldDefinition() {
        ConnectionWrapper conn = null;
        Statement st = null;
        ResultSet rs = null;
        final String tableNameStem = getTableNameStemFromTableName(tableName);

        try {
            Class.forName("org.h2.Driver");
            conn = H2FeatureServiceFactory.getDBConnection(H2FeatureServiceFactory.DB_NAME);
            st = conn.createStatement();

            rs = st.executeQuery(String.format(READ_SLD, tableNameStem));

            if (rs.next()) {
                final String s = rs.getString(1);

                return s;
            }
        } catch (Exception e) {
            LOG.error("Error while loading style definition", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close result set", ex);
                }
            }
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close statement", ex);
                }
            }
//            if (conn != null) {
//                try {
//                    conn.close();
//                } catch (SQLException ex) {
//                    LOG.warn("Cannot close connection", ex);
//                }
//            }
        }

        return null;
    }

    @Override
    protected void initConcreteInstance() throws Exception {
    }

    @Override
    protected String getFeatureLayerType() {
        return H2_FEATURELAYER_TYPE;
    }

    @Override
    protected LayerProperties createLayerProperties() {
        final LayerProperties properties = super.createLayerProperties();

        if (featureFactory != null) {
            setTheFactorySpecificLayerProperties((DefaultLayerProperties)properties,
                (H2FeatureServiceFactory)featureFactory);
        }

        return properties;
    }

    /**
     * Set all layer properties, which depends on the feature factory.
     *
     * @param  properties      The layer properties to set
     * @param  featureFactory  the feature factory to set the layer properties
     */
    private void setTheFactorySpecificLayerProperties(final DefaultLayerProperties properties,
            final H2FeatureServiceFactory featureFactory) {
        ((DefaultLayerProperties)properties).setAttributeTableRuleSet(featureFactory.createH2AttributeTableRuleSet());

        properties.setIdExpression(((H2FeatureServiceFactory)featureFactory).getIdField(),
            LayerProperties.EXPRESSIONTYPE_PROPERTYNAME);
    }

    @Override
    public Icon getLayerIcon(final int type) {
        try {
            if (tableFormat == null) {
                tableFormat = getTableFormat(tableName, H2FeatureServiceFactory.DB_NAME);
            }

            if (tableFormat == null) {
                if (shapeFile != null) {
                    tableFormat = shapeFile.getAbsolutePath().toLowerCase()
                                .substring(shapeFile.getAbsolutePath().length() - 3);
                }
            }

            final Icon layerIcon = layerIcons.get(type + ";" + tableFormat);

            if (layerIcon != null) {
                return layerIcon;
            } else {
                return layerIcons.get(String.valueOf(type));
            }
        } catch (Exception e) {
            return layerIcons.get(String.valueOf(type));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   type       DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     * @param   dbName     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Icon getLayerIcon(final int type, final String tableName, final String dbName) {
        final String format = getTableFormat(tableName, dbName);

        final Icon layerIcon = layerIcons.get(type + ";" + format);

        if (layerIcon != null) {
            return layerIcon;
        } else {
            return layerIcons.get(String.valueOf(type));
        }
    }

    @Override
    public Object clone() {
        return new H2FeatureService(this);
    }

    @Override
    public String getGeometryType() {
        return geometryType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the initialised
     */
    public boolean isInitialised() {
        return initialised;
    }

    /**
     * Adds the geometries for linear referenced values to the service.
     *
     * @param   fromField       The field with the from station
     * @param   tillField       The field with the till station
     * @param   routeField      The field with the route name
     * @param   routeJoinField  the field of the route service, that is used for the join with this service
     * @param   routeService    The service that conains the routes
     * @param   layerName       The name of the route layer
     * @param   domain          the domain of the route layer
     * @param   newTableName    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public H2FeatureService createLinearReferencingLayer(final String fromField,
            final String tillField,
            final String routeField,
            final String routeJoinField,
            final AbstractFeatureService routeService,
            final String layerName,
            final String domain,
            final String newTableName) throws Exception {
        ((H2FeatureServiceFactory)getFeatureFactory()).createLinearReferencingLayer(
            fromField,
            tillField,
            routeField,
            routeJoinField,
            routeService,
            layerName,
            domain,
            newTableName);

        final H2FeatureService service = new H2FeatureService(newTableName, databasePath, newTableName, null);
        service.initAndWait();

        return service;
    }

    /**
     * Adds point geometries to the service.
     *
     * @param   xField        The field with the x value
     * @param   yField        The field with the y value
     * @param   newTableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  H2FeatureServiceFactory.NegativeValueException  DOCUMENT ME!
     * @throws  Exception                                       DOCUMENT ME!
     */
    public H2FeatureService createPointGeometryInformation(final String xField,
            final String yField,
            final String newTableName) throws H2FeatureServiceFactory.NegativeValueException, Exception {
        ((H2FeatureServiceFactory)getFeatureFactory()).createPointGeometryLayer(
            xField,
            yField,
            newTableName);

        final H2FeatureService service = new H2FeatureService(newTableName, databasePath, newTableName, null);
        service.initAndWait();

        return service;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof H2FeatureService) {
            final H2FeatureService service = (H2FeatureService)obj;

            if ((tableName != null) && tableName.equals(service.tableName) && (databasePath != null)
                        && databasePath.equals(service.databasePath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (89 * hash) + ((this.tableName != null) ? this.tableName.hashCode() : 0);
        hash = (89 * hash) + ((this.databasePath != null) ? this.databasePath.hashCode() : 0);
        return hash;
    }

    @Override
    public String decoratePropertyName(final String name) {
        return "\"" + name + "\"";
    }

    /**
     * Checks, if the given table exists in the db.
     *
     * @param   tableName  the name of the table to check
     *
     * @return  True, if the given db table exists
     */
    public static boolean tableAlreadyExists(final String tableName) {
        ConnectionWrapper conn = null;
        ResultSet rs = null;
        boolean tableExists = false;

        try {
            Class.forName("org.h2.Driver");
            conn = H2FeatureServiceFactory.getDBConnection(H2FeatureServiceFactory.DB_NAME);
            rs = conn.getMetaData().getTables(null, null, tableName, null);
            tableExists = rs.next();

            return tableExists;
        } catch (Exception e) {
            LOG.error("Cannot connect to database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close result set", ex);
                }
            }
//            if (conn != null) {
//                try {
//                    conn.close();
//                } catch (SQLException ex) {
//                    LOG.warn("Cannot close connection", ex);
//                }
//            }
        }

        return tableExists;
    }

    /**
     * Lock the given features.
     *
     * @param   id         the id of the feature to lock or null to lock the hole table
     * @param   tableName  The table name of the feature to lock
     *
     * @throws  Exception                               DOCUMENT ME!
     * @throws  LockFromSameUserAlreadyExistsException  DOCUMENT ME!
     */
    public static void lockFeature(final Integer id, final String tableName) throws Exception {
        ConnectionWrapper conn = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            Class.forName("org.h2.Driver");
            conn = H2FeatureServiceFactory.getDBConnection(H2FeatureServiceFactory.DB_NAME);
            st = conn.createStatement();

            if (id == null) {
                rs = st.executeQuery(String.format(CHECK_LOCKED_FEATURE_TABLE, tableName));
            } else {
                rs = st.executeQuery(String.format(CHECK_LOCKED_FEATURE, id, tableName));
            }
            final boolean lockExists = rs.next();

            if (lockExists) {
                throw new LockFromSameUserAlreadyExistsException(
                    "The lock does already exists",
                    NbBundle.getMessage(H2FeatureService.class, "H2FeatureService.lockFeature.localUser"));
            }

            st.execute(String.format(LOCK_FEATURE, ((id == null) ? "null" : id.toString()), tableName));
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close result set", ex);
                }
            }
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close statement", ex);
                }
            }
//            if (conn != null) {
//                try {
//                    conn.close();
//                } catch (SQLException ex) {
//                    LOG.warn("Cannot close connection", ex);
//                }
//            }
        }
    }

    /**
     * Clear all locks.
     */
    public static void clearLocks() {
        ConnectionWrapper conn = null;
        Statement st = null;

        try {
            H2FeatureServiceFactory.createLockTableIfNotExist();
            Class.forName("org.h2.Driver");
            conn = H2FeatureServiceFactory.getDBConnection(H2FeatureServiceFactory.DB_NAME);
            st = conn.createStatement();

            st.execute(String.format(CLEAR_LOCKS));
        } catch (Exception e) {
            LOG.error("Cannot connect to database", e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close statement", ex);
                }
            }
//            if (conn != null) {
//                try {
//                    conn.close();
//                } catch (SQLException ex) {
//                    LOG.warn("Cannot close connection", ex);
//                }
//            }
        }
    }

    /**
     * Unlock the features, which are described by the given values.
     *
     * @param  id         the id of the feature to unlock or null to unlock all features of the given table
     * @param  tableName  the name of the table to unlock
     */
    public static void unlockFeature(final Integer id, final String tableName) {
        ConnectionWrapper conn = null;
        Statement st = null;

        try {
            Class.forName("org.h2.Driver");
            conn = H2FeatureServiceFactory.getDBConnection(H2FeatureServiceFactory.DB_NAME);
            st = conn.createStatement();

            if (id == null) {
                st.execute(String.format(UNLOCK_TABLE, tableName));
            } else {
                st.execute(String.format(UNLOCK, id, tableName));
            }
        } catch (Exception e) {
            LOG.error("Cannot connect to database", e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close statement", ex);
                }
            }
//            if (conn != null) {
//                try {
//                    conn.close();
//                } catch (SQLException ex) {
//                    LOG.warn("Cannot close connection", ex);
//                }
//            }
        }
    }

    /**
     * Checks, if the given table exists in the db.
     *
     * @param   tableName  the name of the table to check
     *
     * @return  True, if the given was removed
     */
    public static boolean removeTableIfExists(final String tableName) {
        ConnectionWrapper conn = null;
        ResultSet rs = null;
        boolean tableExists = false;

        try {
            conn = H2FeatureServiceFactory.getDBConnection(H2FeatureServiceFactory.DB_NAME);
            rs = conn.getMetaData().getTables(null, null, tableName, null);
            tableExists = rs.next();

            if (tableExists) {
                final Statement st = conn.createStatement();
                st.execute(String.format(DROP_TABLE, tableName));
                st.execute(String.format(DROP_SEQUENCE, tableName));
                st.execute(String.format(DELETE_FROM_TABLE, H2FeatureServiceFactory.LR_META_TABLE_NAME, tableName));
                st.execute(String.format(DELETE_FROM_TABLE, H2FeatureServiceFactory.META_TABLE_NAME, tableName));
                st.execute(String.format(
                        DELETE_FROM_TABLE,
                        H2FeatureServiceFactory.META_TABLE_ATTRIBUTES_NAME,
                        tableName));
                st.execute(String.format(DELETE_FROM_LOCK_TABLE, H2FeatureServiceFactory.LOCK_TABLE_NAME, tableName));
                st.close();
                return true;
            }
        } catch (SQLException e) {
            LOG.error("Cannot connect to database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close result set", ex);
                }
            }
//            if (conn != null) {
//                try {
//                    conn.close();
//                } catch (SQLException ex) {
//                    LOG.warn("Cannot close connection", ex);
//                }
//            }
        }

        return tableExists;
    }

    /**
     * Checks, if the given table exists in the db.
     *
     * @param   tableName  DOCUMENT ME!
     * @param   dbName     DOCUMENT ME!
     *
     * @return  True, if the given was removed
     */
    public static String getTableFormat(final String tableName, final String dbName) {
        ConnectionWrapper conn = null;
        ResultSet rs = null;
        Statement st = null;
        String format = "shp"; // the default type is shp

        try {
            conn = H2FeatureServiceFactory.getDBConnection(H2FeatureServiceFactory.DB_NAME);
            st = conn.createStatement();

            rs = st.executeQuery("SELECT format from \"" + H2FeatureServiceFactory.META_TABLE_NAME
                            + "\" WHERE table = '" + tableName + "';");
            if (rs.next()) {
                format = rs.getString(1);
            }
        } catch (SQLException e) {
            LOG.error("Cannot connect to database", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close result set", ex);
                }
            }
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close connection", ex);
                }
            }
//            if (conn != null) {
//                try {
//                    conn.close();
//                } catch (SQLException ex) {
//                    LOG.warn("Cannot close connection", ex);
//                }
//            }
        }

        return format;
    }

    /**
     * Checks, if the table of the layer does exist.
     *
     * @return  true, if the table of the layer does not exist
     */
    public boolean isTableNotFound() {
        return tableNotFound;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  tableNotFound  the tableNotFound to set
     */
    public void setTableNotFound(final boolean tableNotFound) {
        this.tableNotFound = tableNotFound;
    }
}
