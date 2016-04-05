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

import org.jdom.Element;

import java.io.File;

import java.net.URI;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.gui.attributetable.LockFromSameUserAlreadyExistsException;

import static de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory.createLockTableIfNotExist;

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
        super(name, databasePath, tableName, attributes);
        this.shapeFile = shapeFile;
        this.features = features;
        this.orderedAttributeNames = orderedAttributeNames;
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
            f = new H2FeatureServiceFactory(
                    name,
                    databasePath,
                    tableName,
                    shapeFile,
                    layerInitWorker,
                    parseSLD(getSLDDefiniton()));
            checkTable();
            geometryType = f.getGeometryType();
        }

        if (getLayerProperties() != null) {
            setTheFactorySpecificLayerProperties((DefaultLayerProperties)getLayerProperties(), f);
        }

        return f;
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
        final H2FeatureServiceFactory f = (H2FeatureServiceFactory)featureFactory;
        ((DefaultLayerProperties)properties).setAttributeTableRuleSet(new H2AttributeTableRuleSet(
                f.getLinRefList(),
                f.getGeometryType()));
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
     * @param  fromField       The field with the from station
     * @param  tillField       The field with the till station
     * @param  routeField      The field with the route name
     * @param  routeJoinField  the field of the route service, that is used for the join with this service
     * @param  routeService    The service that conains the routes
     * @param  layerName       The name of the route layer
     * @param  domain          the domain of the route layer
     */
    public void setLinearReferencingInformation(final String fromField,
            final String tillField,
            final String routeField,
            final String routeJoinField,
            final AbstractFeatureService routeService,
            final String layerName,
            final String domain) {
        ((H2FeatureServiceFactory)getFeatureFactory()).setLinearReferencingInformation(
            fromField,
            tillField,
            routeField,
            routeJoinField,
            routeService,
            layerName,
            domain);
    }

    /**
     * Adds point geometries to the service.
     *
     * @param  xField  The field with the x value
     * @param  yField  The field with the y value
     */
    public void setPointGeometryInformation(final String xField,
            final String yField) {
        ((H2FeatureServiceFactory)getFeatureFactory()).setPointGeometryInformation(
            xField,
            yField);
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
            conn = (ConnectionWrapper)SFSUtilities.wrapConnection(DriverManager.getConnection(
                        "jdbc:h2:"
                                + H2FeatureServiceFactory.DB_NAME));
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
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close connection", ex);
                }
            }
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
            conn = (ConnectionWrapper)SFSUtilities.wrapConnection(DriverManager.getConnection(
                        "jdbc:h2:"
                                + H2FeatureServiceFactory.DB_NAME));
            st = conn.createStatement();

            if (id == null) {
                rs = st.executeQuery(String.format(CHECK_LOCKED_FEATURE_TABLE, tableName));
            } else {
                rs = st.executeQuery(String.format(CHECK_LOCKED_FEATURE, id, tableName));
            }
            final boolean lockExists = rs.next();

            if (lockExists) {
                throw new LockFromSameUserAlreadyExistsException("The lock does already exists", "local user");
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
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close connection", ex);
                }
            }
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
            conn = (ConnectionWrapper)SFSUtilities.wrapConnection(DriverManager.getConnection(
                        "jdbc:h2:"
                                + H2FeatureServiceFactory.DB_NAME));
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
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close connection", ex);
                }
            }
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
            conn = (ConnectionWrapper)SFSUtilities.wrapConnection(DriverManager.getConnection(
                        "jdbc:h2:"
                                + H2FeatureServiceFactory.DB_NAME));
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
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close connection", ex);
                }
            }
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
            conn = (ConnectionWrapper)SFSUtilities.wrapConnection(DriverManager.getConnection(
                        "jdbc:h2:"
                                + H2FeatureServiceFactory.DB_NAME));
            rs = conn.getMetaData().getTables(null, null, tableName, null);
            tableExists = rs.next();

            if (tableExists) {
                final Statement st = conn.createStatement();
                st.execute("DROP TABLE \"" + tableName + "\";");
                st.execute("DROP SEQUENCE IF EXISTS \"" + tableName + "_seq\";");
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
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close connection", ex);
                }
            }
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
            conn = (ConnectionWrapper)SFSUtilities.wrapConnection(DriverManager.getConnection(
                        "jdbc:h2:"
                                + dbName));
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
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    LOG.warn("Cannot close connection", ex);
                }
            }
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
