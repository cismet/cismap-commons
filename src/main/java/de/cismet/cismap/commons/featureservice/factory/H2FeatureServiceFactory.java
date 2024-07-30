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
package de.cismet.cismap.commons.featureservice.factory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import edu.umd.cs.piccolo.util.PObjectOutputStream;

import org.apache.log4j.Logger;

import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.spatialschema.JTSAdapter;

import org.h2.jdbc.JdbcSQLException;

import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.wrapper.ConnectionWrapper;
import org.h2gis.utilities.wrapper.StatementWrapper;

import org.jfree.util.Log;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import java.math.BigDecimal;

import java.net.URI;

import java.nio.charset.Charset;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.features.JDBCFeatureInfo;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.DefaultLayerProperties;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.LinearReferencingInfo;
import de.cismet.cismap.commons.gui.attributetable.H2AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import de.cismet.cismap.commons.gui.options.CapabilityWidgetOptionsPanel;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.FeatureTools;
import de.cismet.cismap.commons.util.CrsDeterminer;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class H2FeatureServiceFactory extends JDBCFeatureFactory {

    //~ Static fields/initializers ---------------------------------------------

    public static final String DEFAULT_DBF_CHARSET = "ISO8859-1";
    public static final String LR_META_TABLE_NAME = "linear_referencing_meta";
    public static final String META_TABLE_NAME = "table_meta";
    public static final String META_TABLE_ATTRIBUTES_NAME = "table_attributes_meta";
    public static final String SLD_TABLE_NAME = "sld_meta_data";
    public static final String SORT_TABLE_NAME = "sort_meta_data";
    public static final String LOCK_TABLE_NAME = "cs_lock";
    public static final int STATION = 1;
    public static final int STATION_LINE = 2;
    private static final String INSERT_LR_META_DATA = "INSERT INTO \"" + LR_META_TABLE_NAME
                + "\" (table, lin_ref_reference, domain, src_join_field, targ_join_field, lin_ref_geom, kind, from_value, till_value) VALUES ('%s','%s','%s','%s','%s','%s',%s,'%s','%s');";
    private static final String DELETE_LR_META_DATA = "DELETE FROM \"" + LR_META_TABLE_NAME
                + "\" where table = '%s';";
    private static final String INSERT_META_DATA = "INSERT INTO \"" + META_TABLE_NAME
                + "\" (table, format) VALUES ('%s','%s');";
    private static final String INSERT_ATTRIBUTE_META_DATA = "INSERT INTO \"" + META_TABLE_ATTRIBUTES_NAME
                + "\" (table, attributes) VALUES (?,?);";
    private static final String UPDATE_ATTRIBUTE_META_DATA = "UPDATE \"" + META_TABLE_ATTRIBUTES_NAME
                + "\" set attributes = ? where table = ?;";
    private static final String SELECT_ATTRIBUTE_META_DATA = "SELECT attributes from \"" + META_TABLE_ATTRIBUTES_NAME
                + "\" where table = '%s';";

    private static Logger LOG = Logger.getLogger(H2FeatureServiceFactory.class);
    public static final String DB_NAME = "~/cismap/internalH2";
    private static final String CREATE_SPATIAL_INDEX = "CREATE SPATIAL INDEX \"%s\" ON \"%s\" (\"%s\");";
    private static final String UPDATE_SRID =
        "UPDATE \"%1$s\" set \"%2$s\" = st_setsrid(\"%2$s\", %3$s) where \"%2$s\" is not null";
    private static final String CREATE_TABLE_FROM_CSV =
        "CREATE TABLE \"%s\" as select %s from CSVREAD('%s', null, '%s');";
    private static final String CREATE_TABLE_FROM_DBF = "CALL DBFRead('%s', '\"%s\"', '%s');";
    private static final String CREATE_TABLE_FROM_SHP = "CALL SHPRead('%s', '\"%s\"', '%s');";
    private static final String CREATE_TABLE_FROM_FILE = "CALL FILE_TABLE('%s', '\"%s\"');";
    private static final String COPY_TABLE_FROM_DBF = "create table \"%s\" as select * from \"%s\"";
    private static final String DROP_TABLE_REFERENCE = "drop table \"%s\";";
    private static final String SELECT_COLUMN = "select \"%s\", \"%s\" from \"%s\"";
    private static final String UPDATE_COLUMN = "update \"%s\" set \"%s\" = ? where \"%s\" = ?";
    private static final String CREATE_TABLE_TEMPLATE = "create table \"%s\" (%s)";
    private static final String INSERT_TEMPLATE = "INSERT INTO \"%s\" (%s) VALUES (%s)";
    private static final String SPATIAL_INIT = "CALL SPATIAL_INIT();";
    private static final String CREATE_SPATIAL_INIT_ALIAS =
        "CREATE ALIAS IF NOT EXISTS SPATIAL_INIT FOR  \"org.h2gis.h2spatialext.CreateSpatialExtension.initSpatialExtension\";";
    private static final String CREATE_SEQUENCE = "CREATE SEQUENCE \"%s\";";
    private static final String ADD_SEQUENCE = "ALTER TABLE \"%s\" ADD COLUMN \"%s\" int default \"%s\".nextval;";
    private static final String ADD_NOT_NULL_ID = "ALTER TABLE \"%s\" ALTER COLUMN \"%s\" SET NOT NULL;";
    private static final String CREATE_PRIMARY_KEY = "CREATE PRIMARY KEY \"%s\" ON \"%s\"(\"%s\");";
    private static final String CREATE_LR_META_TABLE =
        "create table \"%s\" (id serial, table varchar, lin_ref_reference varchar, domain varchar, src_join_field varchar, targ_join_field varchar, lin_ref_geom varchar, kind int, from_value varchar, till_value varchar);";
    private static final String CREATE_SORT_META_TABLE =
        "create table \"%s\" (id serial, folder varchar, table varchar, position integer);";
    private static final String CREATE_SLD_META_TABLE =
        "create table \"%s\" (\"id\" serial, \"table\" varchar, \"sld\" varchar);";
    private static final String CREATE_META_TABLE = "create table \"%s\" (id serial, table varchar, format varchar);";
    private static final String CREATE_ATTRIBUTES_META_TABLE =
        "create table \"%s\" (id serial, table varchar, attributes array);";
    private static final String CREATE_LOCK_TABLE = "create table IF NOT EXISTS \"" + LOCK_TABLE_NAME
                + "\" (\"id\" integer, \"table\" varchar, \"lock_time\" timestamp);";
    private static final String CREATE_LOCK_TABLE_INDEX = "CREATE INDEX IF NOT EXISTS cs_locks_ind ON \""
                + LOCK_TABLE_NAME + "\" (\"id\", \"table\");";
    private static String[] knownShpBundleEndings = { "dbf", "atx", "sbn", "prj", "shx", "sbx", "aih", "ain", "cpg" };
    private static final Map<String, ConnectionWrapper> DB_CONNECTIONS = Collections.synchronizedMap(
            new HashMap<String, ConnectionWrapper>());
    private static Map<String, String> CS_MAPPING = new HashMap<String, String>();

    static {
        try {
            final ResourceBundle csMapping = ResourceBundle.getBundle("cismapCsMapping", Locale.getDefault());
            final Enumeration<String> en = csMapping.getKeys();

            while (en.hasMoreElements()) {
                final String key = en.nextElement();
                final String value = csMapping.getString(key);
                if (value != null) {
                    CS_MAPPING.put(key, value);
                }
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cannot open the resource bundle for the cs mapping.", e);
            }
        }
    }

    //~ Instance fields --------------------------------------------------------

    protected Vector<FeatureServiceAttribute> featureServiceAttributes;
    // the geometry field in the database or null, if the data were imported from a
    // dbf and does not contain geometries
    private String geometryField;
    private String idField = "id";
    private ConnectionWrapper conn;
    private JDBCFeatureInfo info;
    private String name;
    private List<LinearReferencingInfo> linRefList;
    private int srid = CismapBroker.getInstance().getDefaultCrsAlias();
    private String geometryType = AbstractFeatureService.UNKNOWN;
    private int lastFreeId = 0;
    private boolean removeServiceFromTree = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new H2FeatureServiceFactory object.
     *
     * @param  hff  DOCUMENT ME!
     */
    public H2FeatureServiceFactory(final H2FeatureServiceFactory hff) {
        super(hff);
        this.featureServiceAttributes = (Vector<FeatureServiceAttribute>)hff.featureServiceAttributes.clone();
        this.geometryField = hff.geometryField;
        this.idField = hff.idField;
        this.name = hff.name;
        initConnection();
    }

    /**
     * Creates a new H2FeatureServiceFactory object.
     *
     * @param   name          DOCUMENT ME!
     * @param   databasePath  DOCUMENT ME!
     * @param   tableName     DOCUMENT ME!
     * @param   file          supported file formats are shp, dbf and csv (really comma separated)
     * @param   workerThread  DOCUMENT ME!
     * @param   styles        DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public H2FeatureServiceFactory(final String name,
            final String databasePath,
            final String tableName,
            final File file,
            final SwingWorker workerThread,
            final Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles) throws Exception {
        super(databasePath, tableName);
        this.name = name;
        this.layerName = name;
        this.styles = styles;
        initConnection();

        if (file != null) {
            importFile(workerThread, file);
        }
        initFactory();
    }

    /**
     * Creates a new H2FeatureServiceFactory object.
     *
     * @param  name          DOCUMENT ME!
     * @param  databasePath  DOCUMENT ME!
     * @param  tableName     DOCUMENT ME!
     * @param  featureList   DOCUMENT ME!
     * @param  workerThread  DOCUMENT ME!
     * @param  styles        DOCUMENT ME!
     */
    public H2FeatureServiceFactory(final String name,
            final String databasePath,
            final String tableName,
            final List<FeatureServiceFeature> featureList,
            final SwingWorker workerThread,
            final Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles) {
        this(name, databasePath, tableName, featureList, null, workerThread, styles);
    }

    /**
     * Creates a new H2FeatureServiceFactory object.
     *
     * @param  name                  DOCUMENT ME!
     * @param  databasePath          DOCUMENT ME!
     * @param  tableName             DOCUMENT ME!
     * @param  featureList           DOCUMENT ME!
     * @param  orderedAttributeList  DOCUMENT ME!
     * @param  workerThread          DOCUMENT ME!
     * @param  styles                DOCUMENT ME!
     */
    public H2FeatureServiceFactory(final String name,
            final String databasePath,
            final String tableName,
            final List<FeatureServiceFeature> featureList,
            final List<String> orderedAttributeList,
            final SwingWorker workerThread,
            final Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles) {
        super(databasePath, tableName);
        this.name = name;
        this.layerName = name;
        this.styles = styles;
        initConnection();

        if (featureList != null) {
            importFeatures(workerThread, featureList, orderedAttributeList);
        }
        initFactory();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Imports the given file and assign it to the layer.
     *
     * @param  file  DOCUMENT ME!
     */
    public void setFile(final File file) {
        if (file != null) {
            try {
                importFile(null, file);
            } catch (Exception ex) {
                LOG.error("Error while assigning file", ex);
            }
        }
        initFactory();
    }

    /**
     * Import the given file into the db.
     *
     * @param   workerThread  the thread, that is used to handle the current progress
     * @param   file          the file to import
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void importFile(final SwingWorker workerThread, final File file) throws Exception {
        StatementWrapper st = null;

        try {
            st = createStatement(conn);
            initDatabase(conn);
            ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null);
            createSldMetaTableIfNotExist();

            if (!rs.next()) {
                rs.close();
                if (workerThread != null) {
                    workerThread.firePropertyChange("progress", 5, -1);
                }
                createMetaTableIfNotExist();
                createLockTableIfNotExist();

                if (file.getAbsolutePath().toLowerCase().endsWith("csv")) {
                    createTableFromCsv(file, st);
                } else {
                    Class.forName("org.h2.Driver");
                    ConnectionWrapper connection = null;
                    StatementWrapper sta = null;

                    try {
                        connection = (ConnectionWrapper)SFSUtilities.wrapConnection(DriverManager.getConnection(
                                    "jdbc:h2:"
                                            + databasePath));
                        sta = createStatement(connection);
                        createTableFromSHP(file, sta);
                    } finally {
                        if (sta != null) {
                            sta.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                    }
                }
                final String format = file.getAbsolutePath()
                            .toLowerCase()
                            .substring(file.getAbsolutePath().length() - 3);
                st.execute(String.format(INSERT_META_DATA, tableName, format));

                rs = conn.getMetaData().getColumns(null, null, tableName, "%");
                boolean hasIdField = false;
                String geoCol = null;

                while (rs.next()) {
                    if (rs.getString("COLUMN_NAME").equalsIgnoreCase("id")) {
                        hasIdField = true;
                        idField = rs.getString("COLUMN_NAME");
                    }

                    if (rs.getString("TYPE_NAME").toUpperCase().endsWith("GEOMETRY")) {
                        geoCol = rs.getString("COLUMN_NAME");

                        createSpatialIndex(geoCol, tableName);
                    }
                }
                rs.close();

                if (!createPrimaryKey(hasIdField, tableName)) {
                    removeServiceFromTree = true;
                    H2FeatureService.removeTableIfExists(tableName);
                }

                if (geoCol != null) {
                    // prepare geo field
                    final String crs = determineShapeCrs(file.toURI());

                    if (crs != null) {
                        final PreparedStatement ps = conn.prepareStatement("update \"" + tableName + "\" set \""
                                        + geoCol
                                        + "\" = ? where \"" + idField + "\" = ?");
                        final ResultSet res = st.executeQuery("select \"" + idField + "\", \"" + geoCol + "\" from \""
                                        + tableName + "\"");

                        while (res.next()) {
                            final int id = res.getInt(1);
                            final Geometry geom = (Geometry)res.getObject(2);
                            if (crs.equalsIgnoreCase("EPSG:5650")) {
                                final double minX = geom.getEnvelopeInternal().getMinX();
                                final double maxX = geom.getEnvelopeInternal().getMaxX();

                                if ((minX < 33000000) || (maxX > 33999999)) {
                                    JOptionPane.showMessageDialog(
                                        CismapBroker.getInstance().getMappingComponent(),
                                        "Die Shapedatei enthält Rechtwerte, die kleiner 33000000 oder größer 33999999 sind",
                                        org.openide.util.NbBundle.getMessage(
                                            H2FeatureServiceFactory.class,
                                            "H2FeatureServiceFactory.importFile().title"), // NOI18N
                                        JOptionPane.ERROR_MESSAGE);
                                }
                            }
                            geom.setSRID(CrsTransformer.extractSridFromCrs(crs));
                            final Geometry crsTransformed = CrsTransformer.transformToGivenCrs(
                                    geom,
                                    CismapBroker.getInstance().getDefaultCrs());
                            ps.setObject(1, crsTransformed);
                            ps.setInt(2, id);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
            }

            final CapabilityWidget cap = CapabilityWidgetOptionsPanel.getCapabilityWidget();

            if (cap != null) {
                cap.refreshJdbcTrees();
            }
        } catch (Exception e) {
            logger.error("Error while creating new shape table", e);
            String errorMessage = e.getMessage();

            if (errorMessage.contains("\n") && !errorMessage.startsWith("Shape kann")) {
                errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n"));
            }

            if (file.getAbsolutePath().toLowerCase().endsWith("shp") && (e instanceof JdbcSQLException)) {
                final JdbcSQLException jdbcException = (JdbcSQLException)e;
                if (jdbcException.getCause() instanceof IOException) {
                    final IOException ioEx = (IOException)jdbcException.getCause();
                    if ((ioEx.getMessage() != null) && ioEx.getMessage().contains("Unknown logical value")) {
                        errorMessage = NbBundle.getMessage(
                                H2FeatureServiceFactory.class,
                                "H2FeatureServiceFactory.importFile().dbfError");
                    }
                }
                if (jdbcException.getCause() instanceof IndexOutOfBoundsException) {
                    errorMessage = NbBundle.getMessage(
                            H2FeatureServiceFactory.class,
                            "H2FeatureServiceFactory.importFile().dbfError");
                }
                JOptionPane.showMessageDialog(
                    CismapBroker.getInstance().getMappingComponent(),
                    errorMessage,
                    org.openide.util.NbBundle.getMessage(
                        H2FeatureServiceFactory.class,
                        "H2FeatureServiceFactory.importFile().title"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);

                throw e;
            }

            JOptionPane.showMessageDialog(
                CismapBroker.getInstance().getMappingComponent(),
                errorMessage,
                org.openide.util.NbBundle.getMessage(
                    H2FeatureServiceFactory.class,
                    "H2FeatureServiceFactory.importFile().title"), // NOI18N
                JOptionPane.ERROR_MESSAGE);

            if (errorMessage.equals(
                            org.openide.util.NbBundle.getMessage(
                                H2FeatureServiceFactory.class,
                                "H2FeatureServiceFactory.importFile().emptyFile"))) {
                // remove the layer from the tree
                throw e;
            }
        } finally {
            if (st != null) {
                st.close();
            }
            // the following gc() invocation removes all file descriptors from the imported shp/dbf/csv files

            final Thread t = new Thread("cleanup") {

                    @Override
                    public void run() {
                        System.gc();
                    }
                };

            t.start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   list  DOCUMENT ME!
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int indexOfString(final String[] list, final String name) {
        for (int i = 0; i < list.length; ++i) {
            if (list[i].equals(name)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  tableFormat  DOCUMENT ME!
     */
    public void setTableFormat(final String tableFormat) {
        if (tableFormat != null) {
            try {
                getConnection().createStatement().execute(String.format(INSERT_META_DATA, tableName, tableFormat));
            } catch (Exception e) {
                LOG.error("Error while changing table format", e);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  index  DOCUMENT ME!
     * @param  name   DOCUMENT ME!
     */
    private void addCustomIdFieldToAttributes(final int index, final String name) {
        final String[] attributes = loadOrderedAttributeArray(tableName);

        if (attributes != null) {
            final ArrayList<String> orderedAttributes = new ArrayList<String>(Arrays.asList(attributes));
            orderedAttributes.add(index, name);
            saveAttributeOrder(tableName, orderedAttributes.toArray(new String[orderedAttributes.size()]));
        }
    }

    /**
     * Saves the order of the feature attributes in the corresponding meta table.
     *
     * @param  table       DOCUMENT ME!
     * @param  attributes  DOCUMENT ME!
     */
    private void saveAttributeOrder(final String table, final String[] attributes) {
        PreparedStatement st = null;

        try {
            if (attributes[1].equals("id")) {
                final String tmp = attributes[0];
                attributes[0] = attributes[1];
                attributes[1] = tmp;
            }

            if (existsOrderedAttributeArray(tableName)) {
                st = conn.prepareStatement(UPDATE_ATTRIBUTE_META_DATA);

                st.setObject(1, attributes);
                st.setString(2, table);

                st.executeUpdate();
            } else {
                st = conn.prepareStatement(INSERT_ATTRIBUTE_META_DATA);

                st.setString(1, table);
                st.setObject(2, attributes);

                st.executeUpdate();
            }
        } catch (Exception e) {
            LOG.error("Cannot insert attributes", e);
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (Exception e) {
                LOG.error("Cannot close statement", e);
            }
        }
    }

    /**
     * Loads the order of the feature attributes from the corresponding meta table.
     */
    private void loadAttributeOrder() {
        final String[] attributes = loadOrderedAttributeArray(tableName);

        if (attributes != null) {
            Collections.sort(featureServiceAttributes, new Comparator<FeatureServiceAttribute>() {

                    private int indexOfAttributes(final String name) {
                        for (int i = 0; i < attributes.length; ++i) {
                            if (attributes[i].equals(name)) {
                                return i;
                            }
                        }

                        return -1;
                    }

                    @Override
                    public int compare(final FeatureServiceAttribute o1, final FeatureServiceAttribute o2) {
                        final String name1 = o1.getName();
                        final String name2 = o2.getName();

                        return (int)Math.signum(indexOfAttributes(name1) - indexOfAttributes(name2));
                    }
                });
        }
    }

    /**
     * Loads the order of the feature attributes from the corresponding meta table.
     *
     * @param   table  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String[] loadOrderedAttributeArray(final String table) {
        ResultSet rs = null;
        Statement st = null;

        try {
            st = conn.createStatement();
            rs = st.executeQuery(String.format(SELECT_ATTRIBUTE_META_DATA, table));

            if (rs.next()) {
                final Object o = rs.getObject(1);

                if (o instanceof Object[]) {
                    final String[] attributes = new String[((Object[])o).length];

                    for (int i = 0; i < ((Object[])o).length; ++i) {
                        attributes[i] = (String)((Object[])o)[i];
                    }

                    return attributes;
                }
            }
        } catch (Exception e) {
            LOG.error("Cannot insert attributes", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
            } catch (Exception e) {
                LOG.error("Cannot close statement", e);
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     *
     * @return  true, iff a dataset for this table exists
     */
    private boolean existsOrderedAttributeArray(final String table) {
        ResultSet rs = null;
        Statement st = null;

        try {
            st = conn.createStatement();
            rs = st.executeQuery(String.format(SELECT_ATTRIBUTE_META_DATA, table));

            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            LOG.error("Cannot insert attributes", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
            } catch (Exception e) {
                LOG.error("Cannot close statement", e);
            }
        }

        return false;
    }

    /**
     * Creates the a new table from the given shp/dbf file.
     *
     * @param   file  the file with the source data
     * @param   st    a Statement
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void createTableFromSHP(final File file, final Statement st) throws Exception {
        final String tmpTableReference = tableName + "_temp_reference";
        ResultSet rs;
        boolean isDbf = false;

        if (file.getAbsolutePath().toLowerCase().endsWith(".shp")) {
            if (!checkForFileNames(file)) {
                removeServiceFromTree = true;
                throw new Exception(org.openide.util.NbBundle.getMessage(
                        H2FeatureServiceFactory.class,
                        "H2FeatureServiceFactory.importFile().invalidFileName"));
            }
        } else {
            isDbf = true;
        }

        try {
            final Charset charset = getCharsetDefinition(file.getAbsolutePath());

            if (charset == null) {
                st.execute(String.format(CREATE_TABLE_FROM_FILE, file.getAbsolutePath(), tmpTableReference));
            } else {
                if (isDbf) {
                    st.execute(String.format(
                            CREATE_TABLE_FROM_DBF,
                            file.getAbsolutePath(),
                            tmpTableReference,
                            charset));
                } else {
                    st.execute(String.format(
                            CREATE_TABLE_FROM_SHP,
                            file.getAbsolutePath(),
                            tmpTableReference,
                            charset));
                }
            }

            // test, if file is empty
            rs = st.executeQuery("select * from \"" + tmpTableReference + "\" limit 1");

            if (!rs.next()) {
                throw new Exception(org.openide.util.NbBundle.getMessage(
                        H2FeatureServiceFactory.class,
                        "H2FeatureServiceFactory.importFile().emptyFile"));
            }
            rs.close();

            // st.execute(String.format(COPY_TABLE_FROM_DBF, tableName, tmpTableReference));
            final StringBuilder attsAndTypes = new StringBuilder();
            final StringBuilder atts = new StringBuilder();
            final StringBuilder attsRef = new StringBuilder();
            final List<String> attributeNames = new ArrayList<String>();
            final Map<String, String> typeMap = new HashMap<String, String>();

            rs = st.executeQuery("select * from \"" + tmpTableReference + "\" limit 1");
            for (int i = 2; i <= rs.getMetaData().getColumnCount(); ++i) {
                if (!attsAndTypes.toString().equals("")) {
                    attsAndTypes.append(",");
                    atts.append(",");
                    attsRef.append(",");
                }
                String columnName = rs.getMetaData().getColumnName(i).toLowerCase();

                if (columnName.equals("the_geom")) {
                    columnName = "geom";
                }
                attributeNames.add(columnName);
                attsAndTypes.append("\"")
                        .append(columnName)
                        .append("\" ")
                        .append(rs.getMetaData().getColumnTypeName(i));
                typeMap.put(rs.getMetaData().getColumnName(i), rs.getMetaData().getColumnTypeName(i));
                atts.append("\"").append(columnName).append("\"");
                attsRef.append("\"").append(rs.getMetaData().getColumnName(i)).append("\"");
            }
            rs.close();
            st.execute(String.format(CREATE_TABLE_TEMPLATE, tableName, attsAndTypes.toString()));
            try {
                st.execute(String.format(
                        "INSERT INTO \"%s\" (%s) (select %s from \"%s\")",
                        tableName,
                        atts,
                        attsRef,
                        tmpTableReference));
            } catch (JdbcSQLException e) {
                // This can happen, when the shp file has less data sets as the dbf file
                // Check if this was the reason for NPE
                if (e.getOriginalCause() instanceof NullPointerException) {
                    if (file.getPath().endsWith(".shp")) {
                        final ShapeFile shapeFile = new ShapeFile(file.getPath().substring(
                                    0,
                                    file.getPath().length()
                                            - 4));
                        final int records = shapeFile.getRecordNum();
                        boolean hasNullGeometries = false;
                        int nullGeometries = 0;

                        for (int i = 1; i <= records; ++i) {
                            if (shapeFile.getGeometryByRecNo(i) == null) {
                                hasNullGeometries = true;
                                ++nullGeometries;
                            }
                        }

                        if (hasNullGeometries) {
                            final String[] attr = attsRef.toString().split("\",\"");
                            attr[0] = attr[0].substring(1);
                            attr[attr.length - 1] = attr[attr.length - 1].substring(
                                    0,
                                    attr[attr.length
                                                - 1].length()
                                            - 1);

                            for (int i = 1; i <= records; ++i) {
                                StringBuffer attrValues = null;
                                final org.deegree.model.feature.Feature f = shapeFile.getFeatureByRecNo(i);
                                final Map<String, Object> container = new HashMap<String, Object>();
                                final org.deegree.model.spatialschema.Geometry geometry = shapeFile.getGeometryByRecNo(
                                        i);
                                final FeatureProperty[] featureProperties = f.getProperties();

                                if (geometry == null) {
                                    continue;
                                }

                                for (final FeatureProperty fp : featureProperties) {
                                    container.put(fp.getName().getLocalName(), fp.getValue());
                                }

                                for (final String attribute : attr) {
                                    final String type = typeMap.get(attribute);

                                    if (attrValues == null) {
                                        attrValues = new StringBuffer();
                                    } else {
                                        attrValues.append(",");
                                    }

                                    if (type.equalsIgnoreCase("geometry")) {
                                        final Geometry g = JTSAdapter.export(geometry);
                                        attrValues.append("'").append(g.toText()).append("'");
                                    } else {
                                        if (container.get(attribute) == null) {
                                            attrValues.append("null");
                                        } else {
                                            if (type.equalsIgnoreCase("char")) {
                                                attrValues.append("'").append(container.get(attribute)).append("'");
                                            } else {
                                                attrValues.append(container.get(attribute));
                                            }
                                        }
                                    }
                                }

                                st.execute(String.format(
                                        "INSERT INTO \"%s\" (%s) values (%s)",
                                        tableName,
                                        atts,
                                        attrValues));
                            }

                            JOptionPane.showMessageDialog(CismapBroker.getInstance().getMappingComponent(),
                                NbBundle.getMessage(
                                    H2FeatureServiceFactory.class,
                                    "H2FeatureServiceFactory.createTableFromSHP.nullGeom.message",
                                    new Object[] { nullGeometries }),
                                NbBundle.getMessage(
                                    H2FeatureServiceFactory.class,
                                    "H2FeatureServiceFactory.createTableFromSHP.nullGeom.title"),
                                JOptionPane.WARNING_MESSAGE);
                        } else {
                            throw e;
                        }
                    }
                } else {
                    throw e;
                }
            }

            saveAttributeOrder(tableName, attributeNames.toArray(new String[0]));

            if (!isDbf) {
                ResultSet set = st.executeQuery(String.format(
                            "SELECT distinct ST_GeometryType(\"geom\") from \"%s\"",
                            tableName));

                if (set.next()) {
                    if ((set.getString(1) != null) && set.getString(1).toUpperCase().startsWith("MULTI")) {
                        set.close();
                        set = st.executeQuery(String.format(
                                    "SELECT max(ST_NumGeometries(\"geom\")) from \"%s\"",
                                    tableName));

                        if (set.next() && (set.getInt(1) == 1)) {
                            st.execute(String.format(
                                    "UPDATE \"%s\" set \"geom\" = ST_GeometryN(\"geom\", 1)",
                                    tableName));
                        }
                        set.close();
                    }
                }
            }
            st.execute(String.format(DROP_TABLE_REFERENCE, tmpTableReference));
        } finally {
            try {
                st.execute(String.format(DROP_TABLE_REFERENCE, tmpTableReference));
            } catch (Exception ex) {
                // nothing to do
            }
        }
    }

    /**
     * Creates the a new table from the given csv file.
     *
     * @param   file  the file with the source data
     * @param   st    a Statement
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void createTableFromCsv(final File file, final Statement st) throws Exception {
        final CsvDialog dialog = new CsvDialog(null, true);
        final String separatorChar = determineSeparator(file);

        dialog.setSeparatorChar(separatorChar);
        dialog.setSize(225, 200);
        StaticSwingTools.centerWindowOnScreen(dialog);

        final StatementWrapper checkStatement = createStatement(conn);
        ResultSet csvRs = null;
        try {
            ResultSet checkResultSet = null;
            final String options = "charset=" + dialog.getCharactersetName() + " fieldDelimiter="
                        + dialog.getTextSep() + " fieldSeparator=" + dialog.getSeparatorChar();
            csvRs = st.executeQuery(String.format(
                        "select * from CSVREAD('%s', null, '%s') limit 1;",
                        file.getAbsolutePath(),
                        options));
            final int colCount = csvRs.getMetaData().getColumnCount();
            StringBuilder select = null;
            final List<String> attributeNames = new ArrayList<String>();

            for (int i = 1; i <= colCount; ++i) {
                boolean isInteger = false;
                final String colName = csvRs.getMetaData().getColumnName(i);
                String columnText = "\"" + colName + "\"";

                try {
                    checkResultSet = checkStatement.executeQuery(String.format(
                                "select distinct \""
                                        + colName
                                        + "\"::integer = \""
                                        + colName
                                        + "\"::double from CSVREAD('%s', null, '%s');",
                                file.getAbsolutePath(),
                                options));

                    if (checkResultSet.next()) {
                        if (checkResultSet.getBoolean(1)) {
                            columnText += "::integer";
                            isInteger = true;
                        }
                    }
                } catch (Exception e) {
                    // nothing to do. column data type is no integer
                } finally {
                    if (checkResultSet != null) {
                        checkResultSet.close();
                    }
                }

                if (!isInteger) {
                    try {
                        checkResultSet = checkStatement.executeQuery(String.format(
                                    "select \""
                                            + colName
                                            + "\"::double from CSVREAD('%s', null, '%s');",
                                    file.getAbsolutePath(),
                                    options));
                        columnText += "::double";
                    } catch (Exception e) {
                        try {
                            checkResultSet = checkStatement.executeQuery(String.format(
                                        "select replace(\""
                                                + colName
                                                + "\",',','.')::double from CSVREAD('%s', null, '%s');",
                                        file.getAbsolutePath(),
                                        options));
                            columnText = "replace(" + columnText + ",',','.')::double";
                        } catch (Exception ex) {
                            // nothing to do. column data type is no double
                        }
                    } finally {
                        if (checkResultSet != null) {
                            checkResultSet.close();
                        }
                    }
                }

                if (select == null) {
                    select = new StringBuilder(columnText);
                } else {
                    select.append(", ").append(columnText);
                }

                select.append(" as \"").append(colName).append("\"");
                attributeNames.add(colName);
            }

            st.execute(String.format(
                    CREATE_TABLE_FROM_CSV,
                    tableName,
                    ((select != null) ? select.toString() : " * "),
                    file.getAbsolutePath(),
                    options));

            saveAttributeOrder(tableName, attributeNames.toArray(new String[0]));
        } finally {
            if (csvRs != null) {
                try {
                    csvRs.close();
                } catch (Exception e) {
                    LOG.warn("Error while closing result set", e);
                }
            }
            checkStatement.close();
        }
    }

    /**
     * Checks, if the upper and lower case of all files of the shp bundle is the same.
     *
     * @param   shpFile  the shape file
     *
     * @return  false, if the upper and lower case of a file is different to the shape file
     */
    private boolean checkForFileNames(final File shpFile) {
        final File parentFolder = shpFile.getParentFile();
        final String shpStem = shpFile.getName().substring(0, shpFile.getName().lastIndexOf("."));

        for (final File possibleShpSiebling : parentFolder.listFiles()) {
            if (possibleShpSiebling.isFile()) {
                if (possibleShpSiebling.getName().contains(("."))) {
                    final String ending = possibleShpSiebling.getName()
                                .substring(possibleShpSiebling.getName().lastIndexOf(".") + 1);

                    for (final String possibleEnding : knownShpBundleEndings) {
                        if (possibleEnding.equalsIgnoreCase(ending)) {
                            final String fileStem = possibleShpSiebling.getName()
                                        .substring(0, possibleShpSiebling.getName().lastIndexOf("."));

                            if (shpStem.equalsIgnoreCase(fileStem) && !shpStem.equals(fileStem)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   csvFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String determineSeparator(final File csvFile) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(csvFile));
            final String line = br.readLine();

            if (line.contains(",")) {
                return ",";
            } else {
                return ";";
            }
        } catch (Exception e) {
            LOG.error("Cannot read csv file", e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                // nothing to do
            }
        }

        return ",";
    }

    /**
     * Determines the crs of the corresponding prj file.
     *
     * @param   documentURI  DOCUMENT ME!
     *
     * @return  the crs of the corresponding prj file or null, if the initialisation of the shape file should be
     *          cancelled.
     */
    private String determineShapeCrs(final URI documentURI) {
        String prjFilename;
        File prjFile;

        if (documentURI.getPath().endsWith(".shp")) {
            prjFilename = documentURI.getPath().substring(0, documentURI.getPath().length() - 4);
        } else {
            prjFilename = documentURI.getPath();
        }

        prjFile = new File(prjFilename + ".prj");
        if (!prjFile.exists()) {
            prjFile = new File(prjFilename + ".PRJ");
        }

        try {
            if (prjFile.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(prjFile));
                final String crsDefinition = br.readLine();
                br.close();

                if (crsDefinition != null) {
                    final String epsg = CrsDeterminer.getEpsgCode(crsDefinition);

                    if (epsg != null) {
                        return epsg;
                    }
                } else {
                    logger.warn("The prj file is empty.");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No prj file found.");
                }
            }
        } catch (IOException e) {
            logger.error("Error while reading the prj file.", e);
        }

        return null;
    }

    /**
     * Imports the given features into the db.
     *
     * @param  workerThread          the thread, that is used to handle the current progress
     * @param  features              the features to import
     * @param  orderedAttributeList  DOCUMENT ME!
     */
    private void importFeatures(final SwingWorker workerThread,
            final List<FeatureServiceFeature> features,
            final List<String> orderedAttributeList) {
        try {
            final StatementWrapper st = createStatement(conn);

            initDatabase(conn);
            ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null);
            createSldMetaTableIfNotExist();

            if (!rs.next()) {
                if (workerThread != null) {
                    workerThread.firePropertyChange("progress", 5, -1);
                }
                try {
                    createMetaTableIfNotExist();
                    createLockTableIfNotExist();
                } catch (Exception e) {
                    LOG.error("Cannot create meta table.", e);
                }

                final Map<String, FeatureServiceAttribute> attributeMap = features.get(0)
                            .getLayerProperties()
                            .getFeatureService()
                            .getFeatureServiceAttributes();
                final StringBuilder tableAttributesWithType = new StringBuilder();
                final StringBuilder tableAttributesWithoutType = new StringBuilder();
                final StringBuilder placeholder = new StringBuilder();
                List<String> attributeList;
                boolean firstAttr = true;
                boolean hasIdField = false;

                if (orderedAttributeList != null) {
                    attributeList = orderedAttributeList;
                } else {
                    final List<String> orderedList = features.get(0)
                                .getLayerProperties()
                                .getFeatureService()
                                .getOrderedFeatureServiceAttributes();

                    if (orderedList != null) {
                        attributeList = orderedList;
                    } else {
                        attributeList = new ArrayList<String>(attributeMap.keySet());
                    }
                }

                for (final String attrKey : attributeList) {
                    final FeatureServiceAttribute attr = attributeMap.get(attrKey);
                    if (attr == null) {
                        continue;
                    }
                    if (!firstAttr) {
                        tableAttributesWithType.append(",");
                        tableAttributesWithoutType.append(",");
                        placeholder.append(",");
                    } else {
                        firstAttr = false;
                    }

                    if (attr.getName().equalsIgnoreCase("id")) {
                        hasIdField = true;
                        idField = attr.getName();
                    }

                    tableAttributesWithType.append("\"").append(attr.getName()).append("\" ");
                    tableAttributesWithoutType.append("\"").append(attr.getName()).append("\"");
                    placeholder.append("?");
                    tableAttributesWithType.append(FeatureTools.getH2DataType(attr));
                }

                st.execute(String.format(CREATE_TABLE_TEMPLATE, tableName, tableAttributesWithType.toString()));

                rs = conn.getMetaData().getColumns(null, null, tableName, "%");

                while (rs.next()) {
                    if (rs.getString("TYPE_NAME").toUpperCase().endsWith("GEOMETRY")) {
                        final String colName = rs.getString("COLUMN_NAME");

                        createSpatialIndex(colName, tableName);
                    }
                }
                rs.close();
                final PreparedStatement prepStat = conn.prepareStatement(String.format(
                            INSERT_TEMPLATE,
                            tableName,
                            tableAttributesWithoutType,
                            placeholder));
                int id = 0;
                final String manuallySetId = getManuallyToChangePrimaryKey(attributeList, features);

                for (final FeatureServiceFeature f : features) {
                    int index = 0;
                    for (final String attrKey : attributeList) {
                        final FeatureServiceAttribute attr = attributeMap.get(attrKey);
                        if (attr == null) {
                            continue;
                        }
                        if ((manuallySetId != null) && manuallySetId.equals(attrKey)) {
                            prepStat.setObject(++index, ++id);
                        } else {
                            Object value = f.getProperty(attrKey);

                            if ((value != null) && !(value instanceof Serializable)) {
                                value = value.toString();
                            }
                            prepStat.setObject(++index, value);
                        }
                    }
                    prepStat.execute();
                }

                createPrimaryKey(hasIdField, tableName);
                saveAttributeOrder(tableName, attributeList.toArray(new String[0]));
            }
            st.close();

            final CapabilityWidget cap = CapabilityWidgetOptionsPanel.getCapabilityWidget();

            if (cap != null) {
                cap.refreshJdbcTrees();
            }
        } catch (SQLException e) {
            logger.error("Error while creating a new table from existing features", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   indexName  tableName DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String removeSpecialCharacterFromIndexName(final String indexName) {
        String indexNameWithoutSpecialCharacters = indexName.replace("-", "SMI");
        indexNameWithoutSpecialCharacters = indexNameWithoutSpecialCharacters.replace("/", "SSL");
        indexNameWithoutSpecialCharacters = indexNameWithoutSpecialCharacters.replace(" ", "SSP");
        indexNameWithoutSpecialCharacters = indexNameWithoutSpecialCharacters.replace(",", "SCOM");
        indexNameWithoutSpecialCharacters = indexNameWithoutSpecialCharacters.replace(":", "SCOL");
        indexNameWithoutSpecialCharacters = indexNameWithoutSpecialCharacters.replace("<", "SLE");
        indexNameWithoutSpecialCharacters = indexNameWithoutSpecialCharacters.replace(">", "SGR");
        indexNameWithoutSpecialCharacters = indexNameWithoutSpecialCharacters.replace("(", "KLO");

        return indexNameWithoutSpecialCharacters.replace(")", "KLG");
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attributeList  DOCUMENT ME!
     * @param   features       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getManuallyToChangePrimaryKey(final List<String> attributeList,
            final List<FeatureServiceFeature> features) {
        for (final String attrName : attributeList) {
            if (attrName.equalsIgnoreCase("id")) {
                final TreeSet idSet = new TreeSet();

                for (final FeatureServiceFeature fsf : features) {
                    final Object idProperty = fsf.getProperty(attrName);

                    if ((idProperty == null) || idSet.contains(idProperty)) {
                        return attrName;
                    } else {
                        idSet.add(idProperty);
                    }
                }

                break;
            }
        }

        return null;
    }

    /**
     * Initialises the database, if this was not already happen.
     *
     * @param   conn  the connection to the database
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public static void initDatabase(final ConnectionWrapper conn) throws SQLException {
        final ResultSet rs = conn.getMetaData().getTables(null, null, "GEOMETRY_COLUMNS", null);

        if (!rs.next()) {
            final StatementWrapper st = createStatement(conn);
            st.execute(
                CREATE_SPATIAL_INIT_ALIAS);
            st.execute(SPATIAL_INIT);
            st.close();
        }
        rs.close();
    }

    /**
     * Adds a primary key constraint to the database table.
     *
     * @param   hasIdField  creates a id field, for the primary key, if this parameter is false
     * @param   tableName   DOCUMENT ME!
     *
     * @return  false, iff the primary key was not created
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private boolean createPrimaryKey(final boolean hasIdField, final String tableName) throws SQLException {
        final StatementWrapper st = createStatement(conn);

        if (!hasIdField) {
            final String seqName = tableName + "_seq";

            st.execute(String.format(CREATE_SEQUENCE, seqName));
            st.execute(String.format(ADD_SEQUENCE, tableName, idField, seqName));
        }

        try {
            final String indexName = removeSpecialCharacterFromIndexName(tableName) + "PIndex"
                        + System.currentTimeMillis();
            st.execute(String.format(ADD_NOT_NULL_ID, tableName, idField));
            st.execute(String.format(
                    CREATE_PRIMARY_KEY,
                    removeSpecialCharacterFromIndexName(indexName),
                    tableName,
                    idField));
        } catch (SQLException e) {
            if (hasIdField) {
                int index = 0;
                final String nameBase = "id___";
                String newName = nameBase + fillWithZeros(index);

                while (indexOfString(loadOrderedAttributeArray(tableName), newName) != -1) {
                    newName = nameBase + fillWithZeros(++index);
                }

                final int ans = JOptionPane.showConfirmDialog(CismapBroker.getInstance().getMappingComponent(),
                        NbBundle.getMessage(
                            H2FeatureServiceFactory.class,
                            "H2FeatureServiceFactory.createPrimaryKey",
                            newName),
                        NbBundle.getMessage(
                            H2FeatureServiceFactory.class,
                            "H2FeatureServiceFactory.createPrimaryKey.title"),
                        JOptionPane.OK_CANCEL_OPTION);

                if (ans == JOptionPane.OK_OPTION) {
                    st.execute(String.format(
                            "alter table \"%s\" alter column \"%s\" rename to \"%s\"",
                            tableName,
                            idField,
                            newName));
                    addCustomIdFieldToAttributes(1, newName);
                    createPrimaryKey(false, tableName);
                } else {
                    return false;
                }
            }
        } finally {
            st.close();
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   number  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String fillWithZeros(final int number) {
        if (number < 10) {
            return "00" + number;
        } else if (number < 100) {
            return "0" + number;
        } else {
            return String.valueOf(number);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geoField   DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private void createSpatialIndex(final String geoField, final String tableName) throws SQLException {
        final StatementWrapper st = createStatement(conn);
        final String indexName = removeSpecialCharacterFromIndexName(geoField + tableName
                        + "SpatialIndex") + System.currentTimeMillis();
        final int srid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode());
        st.execute(String.format(CREATE_SPATIAL_INDEX, indexName, tableName, geoField));
        st.execute(String.format(UPDATE_SRID, tableName, geoField, srid));
        st.close();
    }

    /**
     * Creates the meta table for linear referencing, if it does not exist.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void createMetaLinRefTablesIfNotExist() throws Exception {
        final ResultSet rs = conn.getMetaData().getTables(null, null, LR_META_TABLE_NAME, null);

        if (!rs.next()) {
            // meta table does not exist and should be created
            final Statement st = createStatement(conn);
            st.execute(String.format(CREATE_LR_META_TABLE, LR_META_TABLE_NAME));
            st.close();
        }

        rs.close();
    }

    /**
     * Creates the meta table for linear referencing, if it does not exist.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void createSortMetaTableIfNotExist() throws Exception {
        ConnectionWrapper conn = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            Class.forName("org.h2.Driver");
            conn = getDBConnection(DB_NAME);

            rs = conn.getMetaData().getTables(null, null, SORT_TABLE_NAME, null);

            if (!rs.next()) {
                st = conn.createStatement();
                st.execute(String.format(CREATE_SORT_META_TABLE, SORT_TABLE_NAME));
                st.close();
            }
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
        }
    }

    /**
     * Creates the meta table for linear referencing, if it does not exist.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void createLockTableIfNotExist() throws Exception {
        ConnectionWrapper conn = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            Class.forName("org.h2.Driver");
            conn = getDBConnection(DB_NAME);

            rs = conn.getMetaData().getTables(null, null, LOCK_TABLE_NAME, null);

            if (!rs.next()) {
                st = conn.createStatement();
                // lock table does not exist and should be created
                st.execute(CREATE_LOCK_TABLE);
                st.execute(CREATE_LOCK_TABLE_INDEX);
                st.close();
            }
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
     * Creates the meta table, if it does not exist.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void createMetaTableIfNotExist() throws Exception {
        ResultSet rs = conn.getMetaData().getTables(null, null, META_TABLE_NAME, null);

        if (!rs.next()) {
            // meta table does not exist and should be created
            final Statement st = createStatement(conn);
            st.execute(String.format(CREATE_META_TABLE, META_TABLE_NAME));
            st.close();
        }

        rs.close();

        rs = conn.getMetaData().getTables(null, null, META_TABLE_ATTRIBUTES_NAME, null);

        if (!rs.next()) {
            // meta table does not exist and should be created
            final Statement st = createStatement(conn);
            st.execute(String.format(CREATE_ATTRIBUTES_META_TABLE, META_TABLE_ATTRIBUTES_NAME));
            st.close();
        }

        rs.close();
    }

    /**
     * Creates the meta table, if it does not exist.
     */
    private void createSldMetaTableIfNotExist() {
        try {
            final ResultSet rs = conn.getMetaData().getTables(null, null, SLD_TABLE_NAME, null);

            if (!rs.next()) {
                // meta table does not exist and should be created
                final Statement st = createStatement(conn);
                st.execute(String.format(CREATE_SLD_META_TABLE, SLD_TABLE_NAME));
                st.close();
            }

            rs.close();
        } catch (Exception e) {
            LOG.error("Cannot create sld meta table", e);
        }
    }

    /**
     * Determines the charset of a shape file, if a cpg file exists.
     *
     * @param   filename  the name of a shape or dbf file
     *
     * @return  DOCUMENT ME!
     */
    private Charset getCharsetDefinition(final String filename) {
        Charset cs = null;
        String cpgFilename;
        final File cpgFile;

        if (filename.endsWith(".shp") || filename.endsWith(".dbf")) {
            cpgFilename = filename.substring(0, filename.length() - 4);
        } else {
            cpgFilename = filename;
        }

        cpgFile = new File(cpgFilename + ".cpg");

        try {
            if (cpgFile.exists()) {
                final BufferedReader br = new BufferedReader(new FileReader(cpgFile));
                String csName = br.readLine();

                csName = CS_MAPPING.getOrDefault(csName, csName);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("cpg file with charset " + csName + " found");
                }
                if ((csName != null) && Charset.isSupported(csName)) {
                    cs = Charset.forName(csName);
                } else {
                    LOG.warn("The given charset is not supported. Charset: " + csName);
                }
                br.close();
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No cpg file found.");
                }
            }
        } catch (IOException e) {
            LOG.error("Error while reading the cpg file.");
        }

        return cs;
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
     * @param  newTableName    DOCUMENT ME!
     */
    public void createLinearReferencingLayer(final String fromField,
            final String tillField,
            final String routeField,
            final String routeJoinField,
            final AbstractFeatureService routeService,
            final String layerName,
            final String domain,
            final String newTableName) {
        StatementWrapper st = null;
        ResultSet rs = null;
        PreparedStatement linRefGeomUpdate = null;
        PreparedStatement removeLine = null;
        int creationSuccessfully = 0;
        int errorCode = 0;
        int errorStation = 0;
        int onlyParts = 0;
        int dsCount = 0;

        try {
            if (!routeService.isInitialized()) {
                routeService.initAndWait();
            }
            final List<Feature> featureList = routeService.retrieveFeatures(null, 0, 0, name);
            final Map<Object, Geometry> routeGeometries = new HashMap<Object, Geometry>();

            for (final Feature f : featureList) {
                if (f instanceof FeatureServiceFeature) {
                    final FeatureServiceFeature feature = (FeatureServiceFeature)f;
                    routeGeometries.put(feature.getProperty(routeJoinField), feature.getGeometry());
                }
            }
            String geoCol = layerName;
            if (geoCol.indexOf(".") != -1) {
                geoCol = geoCol.substring(geoCol.lastIndexOf(".") + 1);
            }
            st = createStatement(conn);
            st.execute("create table \"" + newTableName + "\" as select * from \"" + this.tableName + "\"");

            if (geometryField != null) {
                st.execute("alter table \"" + newTableName + "\" drop column \"" + geometryField + "\"");
            }

            final String newGeometryField = "geom";
            st.execute("alter table \"" + newTableName + "\" add column \"" + newGeometryField + "\" Geometry");
            String additionalFields = "\"" + fromField + "\",\"" + routeField + "\"";
            linRefGeomUpdate = conn.prepareStatement("UPDATE \"" + newTableName + "\" set \""
                            + newGeometryField + "\" = ? WHERE \"" + idField + "\" = ?");
            removeLine = conn.prepareStatement("DELETE FROM \"" + newTableName + "\"  WHERE \"" + idField + "\" = ?");

            final String[] attributes = loadOrderedAttributeArray(this.tableName);
            final List<String> attributeList = new ArrayList<String>(Arrays.asList(attributes));
            attributeList.add(0, newGeometryField);
            saveAttributeOrder(newTableName, attributeList.toArray(new String[attributeList.size()]));

            if (tillField != null) {
                additionalFields += ",\"" + tillField + "\"";
            }

            rs = st.executeQuery("select \"" + idField + "\"," + additionalFields + " from \""
                            + newTableName + "\"");

            while (rs.next()) {
                boolean fromError = false;
                final int id = rs.getInt(1);
                double from = rs.getDouble(2);
                final Object routeId = rs.getObject(3);
                Geometry geom = null;
                Geometry routeGeom = routeGeometries.get(routeId);
                dsCount++;

                if ((routeGeom == null) && (routeId instanceof Long)) {
                    routeGeom = routeGeometries.get(new BigDecimal((Long)routeId));
                }
                if (routeGeom == null) {
                    LOG.warn("No geometry found for route " + routeId);
                    errorCode++;
                    removeLine.setInt(1, id);
                    removeLine.execute();
                    continue;
                }
                final double routeLength = routeGeom.getLength();
                final LengthIndexedLine line = new LengthIndexedLine(routeGeom);

                creationSuccessfully++;

                if (from > routeLength) {
                    errorStation++;
                    fromError = true;
                    creationSuccessfully--;
                }

                if (tillField != null) {
                    final double till = rs.getDouble(4);

                    if (till > routeLength) {
                        errorStation++;

                        if (!fromError) {
                            ++onlyParts;
                            creationSuccessfully--;
                        }
                    }

                    if ((till > routeLength) && (from > routeLength)) {
                        from = routeLength - 1;

                        if (from < 0) {
                            from = 0;
                        }
                    }
                    geom = line.extractLine(from, till);
                } else {
                    final Coordinate coords = line.extractPoint(from);
                    geom = routeGeom.getFactory().createPoint(coords);
                }

                linRefGeomUpdate.setObject(1, geom);
                linRefGeomUpdate.setInt(2, id);
                linRefGeomUpdate.execute();
            }

            createMetaLinRefTablesIfNotExist();
            final String tillInfo = ((tillField == null) ? "" : tillField);
            final int kind = ((tillField == null) ? STATION : STATION_LINE);

//            st.execute(String.format(
//                    DELETE_LR_META_DATA,
//                    tableName));
            st.execute(String.format(
                    INSERT_LR_META_DATA,
                    newTableName,
                    layerName,
                    domain,
                    routeField,
                    routeJoinField,
                    newGeometryField,
                    new Integer(kind),
                    fromField,
                    tillInfo));
            st.execute(String.format(INSERT_META_DATA, newTableName, "r"));
            createPrimaryKey(true, newTableName);
            createSpatialIndex(newGeometryField, newTableName);

            if (creationSuccessfully == 0) {
                JOptionPane.showMessageDialog(
                    CismapBroker.getInstance().getMappingComponent(),
                    org.openide.util.NbBundle.getMessage(
                        H2FeatureServiceFactory.class,
                        "H2FeatureServiceFactory.createLinearReferencingLayer().noresult",
                        errorCode,
                        errorStation),
                    org.openide.util.NbBundle.getMessage(
                        H2FeatureServiceFactory.class,
                        "H2FeatureServiceFactory.createLinearReferencingLayer().title"), // NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
                H2FeatureService.removeTableIfExists(newTableName);
            } else if ((errorCode == 0) && (errorStation == 0)) {
            } else {
                if (tillField == null) {
                    JOptionPane.showMessageDialog(
                        CismapBroker.getInstance().getMappingComponent(),
                        org.openide.util.NbBundle.getMessage(
                            H2FeatureServiceFactory.class,
                            "H2FeatureServiceFactory.createLinearReferencingLayer().points",
                            creationSuccessfully,
                            dsCount,
                            errorCode,
                            errorStation),
                        org.openide.util.NbBundle.getMessage(
                            H2FeatureServiceFactory.class,
                            "H2FeatureServiceFactory.createLinearReferencingLayer().title"), // NOI18N
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(
                        CismapBroker.getInstance().getMappingComponent(),
                        org.openide.util.NbBundle.getMessage(
                            H2FeatureServiceFactory.class,
                            "H2FeatureServiceFactory.createLinearReferencingLayer()",
                            creationSuccessfully,
                            dsCount,
                            onlyParts,
                            errorCode,
                            errorStation),
                        org.openide.util.NbBundle.getMessage(
                            H2FeatureServiceFactory.class,
                            "H2FeatureServiceFactory.createLinearReferencingLayer().title"), // NOI18N
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            try {
                if (st != null) {
                    st.execute("drop table \"" + newTableName + "\"");
                }
            } catch (SQLException ex) {
                LOG.error("Cannot drop table", ex);
            }
            LOG.error("Error while joining linear referenced table.", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing result set", ex);
                }
            }
            if (linRefGeomUpdate != null) {
                try {
                    linRefGeomUpdate.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing prepared statement", ex);
                }
            }
            if (removeLine != null) {
                try {
                    removeLine.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing prepared statement", ex);
                }
            }
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing statement", ex);
                }
            }
        }
    }

    /**
     * Adds point geometries to the service.
     *
     * @param   xField        The field with the x value
     * @param   yField        The field with the y value
     * @param   newTableName  DOCUMENT ME!
     *
     * @throws  NegativeValueException  DOCUMENT ME!
     */
    public void createPointGeometryLayer(final String xField, final String yField, final String newTableName)
            throws NegativeValueException {
        StatementWrapper st = null;
        PreparedStatement linRefGeomUpdate = null;
        ResultSet rs = null;

        try {
            st = createStatement(conn);
            st.execute("create table \"" + newTableName + "\" as select * from \"" + this.tableName + "\"");
            final String newGeometryField = "geo_xy";

            if (geometryField != null) {
                st.execute("alter table \"" + newTableName + "\" drop column \"" + geometryField + "\"");
            }
            st.execute("alter table \"" + newTableName + "\" add column \"" + newGeometryField + "\" Geometry");

            final String additionalFields = "\"" + xField + "\",\"" + yField + "\"";
            linRefGeomUpdate = conn.prepareStatement("UPDATE \"" + newTableName + "\" set \""
                            + newGeometryField + "\" = ? WHERE \"" + idField + "\" = ?");
            rs = st.executeQuery("select \"" + idField + "\"," + additionalFields + " from \""
                            + newTableName + "\"");
            final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                    CrsTransformer.getCurrentSrid());
            boolean xNegative = false;
            boolean yNegative = false;

            while (rs.next()) {
                final int id = rs.getInt(1);
                final double x = rs.getDouble(2);
                final double y = rs.getDouble(3);

                if (!xNegative && (x < 0)) {
                    xNegative = true;
                }

                if (!yNegative && (y < 0)) {
                    yNegative = true;
                }

                if (!xNegative && !yNegative) {
                    final Geometry geom = gf.createPoint(new Coordinate(x, y));

                    linRefGeomUpdate.setObject(1, geom);
                    linRefGeomUpdate.setInt(2, id);
                    linRefGeomUpdate.addBatch();
                }
            }

            if (xNegative || yNegative) {
                final NegativeValueException e = new NegativeValueException(xNegative && yNegative,
                        "Negative values found");

                if (xNegative && !yNegative) {
                    e.setAttributeName(xField);
                }

                if (!xNegative && yNegative) {
                    e.setAttributeName(yField);
                }

                throw e;
            }

            linRefGeomUpdate.executeBatch();
            st.execute(String.format(INSERT_META_DATA, newTableName, "xy"));
            createPrimaryKey(true, newTableName);
            createSpatialIndex(newGeometryField, newTableName);
        } catch (NegativeValueException e) {
            try {
                if (st != null) {
                    st.execute("drop table \"" + newTableName + "\"");
                }
            } catch (SQLException ex) {
                LOG.error("Cannot drop table", ex);
            }
            throw e;
        } catch (Exception e) {
            try {
                if (st != null) {
                    st.execute("drop table \"" + newTableName + "\"");
                }
            } catch (SQLException ex) {
                LOG.error("Cannot drop table", ex);
            }
            LOG.error("Error while adding point geometries.", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    LOG.error("Cannot close result set", ex);
                }
            }
            if (linRefGeomUpdate != null) {
                try {
                    linRefGeomUpdate.close();
                } catch (SQLException ex) {
                    LOG.error("Cannot close prepared statement", ex);
                }
            }
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    LOG.error("Cannot close statement", ex);
                }
            }
        }
    }

    /**
     * Initialises the featureServiceAttributes and the geometryField.
     */
    private synchronized void initFactory() {
        featureServiceAttributes = new Vector<FeatureServiceAttribute>();
        try {
            ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, "%");
            boolean upperCaseTryed = false;
            int rep = 0;

            do {
                ++rep;
                while (rs.next()) {
                    if (rs.getString("COLUMN_NAME").equalsIgnoreCase("id")) {
                        idField = rs.getString("COLUMN_NAME");
                    }
                    featureServiceAttributes.add(new FeatureServiceAttribute(
                            rs.getString("COLUMN_NAME"),
                            String.valueOf(rs.getInt("DATA_TYPE")),
                            true));
                    if (rs.getString("TYPE_NAME").toUpperCase().endsWith("GEOMETRY")) {
                        geometryField = rs.getString("COLUMN_NAME");
                        final FeatureServiceAttribute attr = featureServiceAttributes.get(
                                featureServiceAttributes.size()
                                        - 1);
                        attr.setGeometry(true);
                        attr.setType(rs.getString("TYPE_NAME"));
                    }
                }

                if (featureServiceAttributes.isEmpty() && !upperCaseTryed) {
                    rs.close();
                    rs = conn.getMetaData().getColumns(null, null, tableName.toUpperCase(), "%");
                    upperCaseTryed = true;
                }
            } while (rep < 2);
            rs.close();
            loadAttributeOrder();
            createMetaLinRefTablesIfNotExist();
            final StatementWrapper st = createStatement(conn);
            final ResultSet lrMeta = st.executeQuery(
                    "SELECT lin_ref_reference, domain, src_join_field, targ_join_field, lin_ref_geom, kind, from_value, till_value FROM \""
                            + LR_META_TABLE_NAME
                            + "\" where table = '"
                            + tableName
                            + "'");
            linRefList = new ArrayList<LinearReferencingInfo>();

            while (lrMeta.next()) {
                final LinearReferencingInfo refInfo = new LinearReferencingInfo();
                refInfo.setLinRefReferenceName(lrMeta.getString(1));
                refInfo.setDomain(lrMeta.getString(2));
                refInfo.setSrcLinRefJoinField(lrMeta.getString(3));
                refInfo.setTrgLinRefJoinField(lrMeta.getString(4));
                refInfo.setGeomField(lrMeta.getString(5));
                refInfo.setFromField(lrMeta.getString(7));
                refInfo.setTillField(lrMeta.getString(8));
                linRefList.add(refInfo);
            }

            if (layerProperties instanceof DefaultLayerProperties) {
                ((DefaultLayerProperties)layerProperties).setAttributeTableRuleSet(createH2AttributeTableRuleSet());
            }

            if (geometryField != null) {
                final ResultSet envelopeSet = st.executeQuery("SELECT ST_Extent(\"" + geometryField
                                + "\"), (SELECT st_srid(\""
                                + geometryField + "\"::Geometry) from \"" + tableName + "\" limit 1) from \""
                                + tableName
                                + "\" where \"" + geometryField + "\" is not null;");

                if (envelopeSet.next()) {
                    final Object geomObject = envelopeSet.getObject(1);

                    if (geomObject instanceof Envelope) {
                        final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                                envelopeSet.getInt(2));
                        envelope = gf.toGeometry((Envelope)geomObject);
                    } else if (geomObject instanceof Polygon) {
                        envelope = (Polygon)geomObject;
                        envelope.setSRID(envelopeSet.getInt(2));
                    } else if (geomObject instanceof Geometry) {
                        final Geometry g = ((Geometry)geomObject);
                        if (!g.isEmpty()) {
                            envelope = g.getEnvelope().buffer(50);
                            envelope.setSRID(envelopeSet.getInt(2));
                        }
                    } else {
                        envelope = null;
                    }

                    envelopeSet.close();
                } else {
                    logger.error("cannot determine H2 layer envelope");
                }

                final ResultSet geometryTypeRs = st.executeQuery("SELECT distinct st_geometryType(\"" + geometryField
                                + "\"), (select \"" + geometryField + "\" from \"" + tableName + "\" where \""
                                + geometryField
                                + "\" is not null limit 1) from \"" + tableName + "\" where \"" + geometryField
                                + "\" is not null limit 1;");

                if (geometryTypeRs.next()) {
                    geometryType = geometryTypeRs.getString(1);
                    final Object o = geometryTypeRs.getObject(2);

                    if (geometryTypeRs.next()) {
                        geometryType = AbstractFeatureService.UNKNOWN;
                    } else {
                        if (o instanceof Geometry) {
                            geometryType = ((Geometry)o).getGeometryType();
                        }
                    }

                    if (getLayerProperties() instanceof DefaultLayerProperties) {
                        ((DefaultLayerProperties)getLayerProperties()).setAttributeTableRuleSet(
                            createH2AttributeTableRuleSet());
                    }
                }

                geometryTypeRs.close();
            } else {
                geometryType = AbstractFeatureService.NONE;

                if (getLayerProperties() instanceof DefaultLayerProperties) {
                    ((DefaultLayerProperties)getLayerProperties()).setAttributeTableRuleSet(
                        createH2AttributeTableRuleSet());
                }
            }

            st.close();

            if (info == null) {
                info = new JDBCFeatureInfo(conn, srid, geometryField, tableName, idField);
            }
        } catch (Exception e) {
            LOG.error("Error while reading meta information of table " + databasePath + "." + tableName, e);
        }
    }

    /**
     * The properties linRefList, geometryType and featureServiceAttributes must be filled, when this method is called.
     *
     * @return  the default H2AttributeTableRuleSet
     */
    public H2AttributeTableRuleSet createH2AttributeTableRuleSet() {
        final Collection<? extends H2AttributeTableRuleSet> attributeRuleSet = Lookup.getDefault()
                    .lookupAll(H2AttributeTableRuleSet.class);

        if ((attributeRuleSet != null) && (attributeRuleSet.size() > 0)) {
            final H2AttributeTableRuleSet ruleSet =
                attributeRuleSet.toArray(new H2AttributeTableRuleSet[attributeRuleSet.size()])[0].clone();

            ruleSet.init(linRefList, geometryType, featureServiceAttributes, tableName);

            return ruleSet;
        }

        return null;
    }

    @Override
    public void setLayerProperties(final LayerProperties layerProperties) {
        super.setLayerProperties(layerProperties);
        if (layerProperties instanceof DefaultLayerProperties) {
            if (removeServiceFromTree && (layerProperties.getFeatureService() != null)) {
                CismapBroker.getInstance()
                        .getMappingComponent()
                        .getMappingModel()
                        .removeLayer(layerProperties.getFeatureService());
            }
            ((DefaultLayerProperties)layerProperties).setAttributeTableRuleSet(createH2AttributeTableRuleSet());
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void updatePFeatures() {
        try {
            final PreparedStatement ps = conn.prepareStatement("update \"" + tableName
                            + "\" set pfeature = ? where \"" + idField + "\" = ?;");
            final StatementWrapper upState = (StatementWrapper)conn.createStatement();
            final ResultSet resultSet = upState.executeQuery("select \"" + idField + "\", " + geometryField + " from \""
                            + tableName
                            + "\"");

            try {
                int i = 0;
                while (resultSet.next()) {
                    final int id = resultSet.getInt(1);
                    final Geometry geom = (Geometry)resultSet.getObject(2);
                    final PFeature feature = new PFeature(new DefaultFeatureServiceFeature(
                                id,
                                geom,
                                this.getLayerProperties()),
                            CismapBroker.getInstance().getMappingComponent());
                    ps.setObject(1, PObjectOutputStream.toByteArray(feature));
                    ps.setInt(2, id);
                    ps.addBatch();
                    if (((++i) % 1000) == 0) {
                        ps.executeBatch();
                    }
                }
                ps.executeBatch();
            } catch (Exception e) {
                LOG.error("Error while creating pfeatures.", e);
            }
        } catch (Exception e) {
            logger.error("Error while creating pfeatures", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
//        final H2FeatureServiceFactory h2 = new H2FeatureServiceFactory(DB_NAME, "poly");
    }

    /**
     * DOCUMENT ME!
     */
    private synchronized void initConnection() {
        conn = getDBConnection(databasePath);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private ConnectionWrapper getConnection() {
        return getDBConnection(databasePath);
    }

    /**
     * Creates a connection to the internal database.
     *
     * @param   databasePath  the path to the database. if null, {@link DB_NAME} will be used
     *
     * @return  a connection to the internal database
     */
    public static ConnectionWrapper getDBConnection(final String databasePath) {
        final String path = ((databasePath == null) ? DB_NAME : databasePath);

        ConnectionWrapper connection = DB_CONNECTIONS.get(path);
        boolean isConnectionClosed = false;

        try {
            if (connection != null) {
                isConnectionClosed = connection.isClosed() || !connection.isValid(100);
            }
        } catch (Exception e) {
            isConnectionClosed = true;
        }

        if ((connection == null) || isConnectionClosed) {
            try {
                if (connection != null) {
                    connection.close();
                }
                Class.forName("org.h2.Driver");
                connection = (ConnectionWrapper)SFSUtilities.wrapConnection(DriverManager.getConnection(
                            "jdbc:h2:"
                                    + path));
                DB_CONNECTIONS.put(path, connection);
            } catch (ClassNotFoundException e) {
                LOG.error("Error while creating database connection.", e);
                return null;
            } catch (SQLException e) {
                LOG.error("Error while creating database connection.", e);

                return null;
            }
        }

        return connection;
    }

    /**
     * Tries to create a connection to the internal database.
     *
     * @param   databasePath  the path to the database. if null, {@link DB_NAME} will be used
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public static void tryDBConnection(final String databasePath) throws SQLException {
        final String path = ((databasePath == null) ? DB_NAME : databasePath);

        ConnectionWrapper connection = DB_CONNECTIONS.get(path);
        boolean isConnectionClosed = false;

        try {
            if (connection != null) {
                isConnectionClosed = connection.isClosed() || !connection.isValid(100);
            }
        } catch (Exception e) {
            isConnectionClosed = true;
        }

        if ((connection == null) || isConnectionClosed) {
            try {
                if (connection != null) {
                    connection.close();
                }
                Class.forName("org.h2.Driver");
                connection = (ConnectionWrapper)SFSUtilities.wrapConnection(DriverManager.getConnection(
                            "jdbc:h2:"
                                    + path));
                DB_CONNECTIONS.put(path, connection);
            } catch (ClassNotFoundException e) {
                LOG.error("Error while creating database connection.", e);
            } catch (SQLException e) {
                LOG.error("Error while creating database connection.", e);

                throw e;
            }
        }
    }

    /**
     * Creates a new statement on the givven connection.
     *
     * @param   conn  the connection, the statement should be created on
     *
     * @return  the new statement
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public static synchronized StatementWrapper createStatement(final ConnectionWrapper conn) throws SQLException {
        return (StatementWrapper)conn.createStatement();
    }

    @Override
    protected boolean isGenerateIds() {
        return true;
    }

    @Override
    public AbstractFeatureFactory clone() {
        return new H2FeatureServiceFactory(this);
    }

    @Override
    public List createFeatures(final Object query, final BoundingBox boundingBox, final SwingWorker workerThread)
            throws FeatureFactory.TooManyFeaturesException, Exception {
        return createFeaturesInternal(query, boundingBox, workerThread, 0, 80000, null, true);
    }

    @Override
    public List createFeatures(final Object query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread,
            final int offset,
            final int limit,
            final FeatureServiceAttribute[] orderBy) throws FeatureFactory.TooManyFeaturesException, Exception {
        return createFeaturesInternal(query, boundingBox, workerThread, offset, limit, orderBy, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  tableName  DOCUMENT ME!
     */
    @Override
    public void setTableName(final String tableName) {
        this.tableName = tableName;
        if (info != null) {
            info.setTableName(tableName);
        }
    }

    @Override
    public synchronized FeatureServiceFeature createNewFeature() {
        final JDBCFeature feature = new JDBCFeature(info, getStyle(name));
        feature.setId(getFreeId());
        feature.setLayerProperties(this.getLayerProperties());

        return feature;
    }

    /**
     * Returns the name of the field, that contains the id.
     *
     * @return  the name of the field, that contains the id
     */
    public String getIdField() {
        return idField;
    }

    /**
     * The next free id or -1 if the id cannot be determined.
     *
     * @return  the next free id or -1 if the id cannot be determined.
     */
    private int getFreeId() {
        if (lastFreeId > 0) {
            return ++lastFreeId;
        } else {
            int freeId = -1;
            Statement st = null;

            try {
                st = createStatement(conn);
                final String maxId = "SELECT max(\"%1s\") + 1 from \"%2s\";";

                final String query = String.format(maxId, idField, tableName);
                final ResultSet rs = st.executeQuery(query);

                if ((rs != null) && rs.next()) {
                    freeId = rs.getInt(1);
                    lastFreeId = freeId;
                }

                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                Log.error("cannot determine free id", e);
            } finally {
                if (st != null) {
                    try {
                        st.close();
                    } catch (SQLException ex) {
                        LOG.error("Cannot close statement", ex);
                    }
                }
            }

            return freeId;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void determineEnvelope() throws Exception {
        StatementWrapper st = null;
        try {
            st = createStatement(conn);
            final ResultSet envelopeSet = st.executeQuery("SELECT ST_Extent(\"" + geometryField
                            + "\"), (SELECT st_srid(\""
                            + geometryField + "\"::Geometry) from \"" + tableName + "\" limit 1) from \""
                            + tableName
                            + "\" where \"" + geometryField + "\" is not null;");

            if (envelopeSet.next()) {
                final Object geomObject = envelopeSet.getObject(1);

                if (geomObject instanceof Envelope) {
                    final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                            envelopeSet.getInt(2));
                    envelope = gf.toGeometry((Envelope)geomObject);
                } else if (geomObject instanceof Polygon) {
                    envelope = (Polygon)geomObject;
                    envelope.setSRID(envelopeSet.getInt(2));
                } else {
                    envelope = null;
                }

                envelopeSet.close();
            } else {
                logger.error("cannot determine H2 layer envelope");
            }
        } catch (Exception e) {
            LOG.error("Error while determining envelope", e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {
                    LOG.error("Error while closing statement", e);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void determineIdField() {
        String id = "id";
        ResultSet rs = null;

        try {
            rs = conn.getMetaData().getColumns(null, null, tableName, "%");
            ;

            for (int i = 0; i < rs.getMetaData().getColumnCount(); ++i) {
                if (rs.getString("COLUMN_NAME").equalsIgnoreCase("id")) {
                    id = rs.getMetaData().getColumnName(i);
                    break;
                }
            }
        } catch (Exception e) {
            LOG.error("Cannot determine the id field", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    // nothing to do
                }
            }
        }
        idField = id;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   query              DOCUMENT ME!
     * @param   boundingBox        DOCUMENT ME!
     * @param   workerThread       DOCUMENT ME!
     * @param   offset             DOCUMENT ME!
     * @param   limit              DOCUMENT ME!
     * @param   orderBy            DOCUMENT ME!
     * @param   saveAsLastCreated  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  FeatureFactory.TooManyFeaturesException  DOCUMENT ME!
     * @throws  Exception                                DOCUMENT ME!
     */
    private List createFeaturesInternal(final Object query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread,
            final int offset,
            final int limit,
            final FeatureServiceAttribute[] orderBy,
            final boolean saveAsLastCreated) throws FeatureFactory.TooManyFeaturesException, Exception {
        final StringBuilder sb = new StringBuilder("select \"" + idField + "\" from \"");
        final int srid = CrsTransformer.extractSridFromCrs(crs.getCode());

        if ((boundingBox != null) && (geometryField == null)) {
            return new ArrayList<JDBCFeature>();
        }

        if (boundingBox != null) {
            sb.append(tableName)
                    .append("\" WHERE \"")
                    .append(geometryField)
                    .append("\" && '")
                    .append(boundingBox.getGeometry(srid))
                    .append("'");
        } else {
            sb.append(tableName).append("\"");
        }

        if ((query != null) && !query.equals("")) {
            if (boundingBox != null) {
                sb.append(" and ");
            } else {
                sb.append(" WHERE ");
            }
            sb.append(query);
        }
        if (limit != 0) {
            sb.append(" limit ").append(limit);
        }
        if (offset != 0) {
            sb.append(" offset ").append(offset);
        }

        if ((orderBy != null) && (orderBy.length > 0)) {
            boolean firstAttr = true;
            sb.append(" order by ");

            for (final FeatureServiceAttribute attr : orderBy) {
                if (!firstAttr) {
                    sb.append(", ");
                } else {
                    firstAttr = false;
                }
                sb.append(attr.getName());
            }
        }
        final String select = sb.toString();

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("db query: " + select);
            }
            setInterruptedNotAllowed();
            final StatementWrapper st = createStatement(conn);
            final ResultSet rs = st.executeQuery(select);
            final List<JDBCFeature> selectedFeatures = new ArrayList<JDBCFeature>();

            if (info == null) {
                info = new JDBCFeatureInfo(conn, srid, geometryField, tableName, idField);
            }
            final List style = getStyle(name);
            Geometry boundingBoxGeom = null;

            if (boundingBox != null) {
                boundingBoxGeom = boundingBox.getGeometry(srid);
            }

            while (rs.next()) {
                if ((workerThread != null) && workerThread.isCancelled()) {
                    return null;
                }
                final JDBCFeature feature = new JDBCFeature(info, style);
                feature.setId(rs.getInt(idField));
                feature.setLayerProperties(this.getLayerProperties());
                final Geometry featureGeometry = feature.getGeometry();

                if (!saveAsLastCreated && (boundingBoxGeom != null) && (featureGeometry != null)
                            && !boundingBoxGeom.intersects(featureGeometry)) {
                    continue;
                }

                selectedFeatures.add(feature);
            }
//            updatePFeatures();
            rs.close();
            st.close();
            if (saveAsLastCreated) {
                updateLastCreatedFeatures(selectedFeatures, boundingBox.getGeometry(srid), query);
            }
            return selectedFeatures;
        } catch (SQLException e) {
            LOG.error("Error during the createFeatures operation. Query: " + query, e);
        } finally {
            setInterruptedAllowed();
        }

        return new ArrayList<JDBCFeature>();
    }

    @Override
    public synchronized List createAttributes(final SwingWorker workerThread)
            throws FeatureFactory.TooManyFeaturesException, UnsupportedOperationException, Exception {
        if ((featureServiceAttributes == null) || featureServiceAttributes.isEmpty()) {
            initFactory();
        }

        if ((featureServiceAttributes == null) || featureServiceAttributes.isEmpty()) {
            logger.error("SW[" + workerThread + "]: no attributes could be found in h2 database table");
            throw new Exception("no attributes could be found in the database table '" + this.databasePath + "."
                        + this.tableName + "'");
        }

        return featureServiceAttributes;
    }

    @Override
    public int getFeatureCount(final Object query, final BoundingBox bb) {
        final StringBuilder sb = new StringBuilder("select count(*) from \"");
        final int srid = CrsTransformer.extractSridFromCrs(crs.getCode());

        if (bb != null) {
            sb.append(tableName)
                    .append("\" WHERE ")
                    .append(geometryField)
                    .append(" && '")
                    .append(bb.getGeometry(srid))
                    .append("'");
        } else {
            sb.append(tableName).append("\"");
        }

        if ((query != null) && !query.equals("")) {
            if (bb != null) {
                sb.append(" and ");
            } else {
                sb.append(" WHERE ");
            }
            sb.append(query);
        }

        final String sqlQuery = sb.toString();
        int result = 0;

        StatementWrapper st = null;
        ResultSet rs = null;

        try {
            st = createStatement(conn);
            rs = st.executeQuery(sqlQuery);
            if (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOG.error("Error while determining the feature count. Query: " + sqlQuery, e);
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
        }

        return result;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        closeConnection();
    }

    /**
     * DOCUMENT ME!
     */
    public void closeConnection() {
//        try {
//            if (conn != null) {
//                conn.close();
//            }
//        } catch (Exception e) {
//            // nothing to do
//        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the linRefList
     */
    public List<LinearReferencingInfo> getLinRefList() {
        return linRefList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the geometryType
     */
    public String getGeometryType() {
        return geometryType;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class NegativeValueException extends Exception {

        //~ Instance fields ----------------------------------------------------

        private final boolean both;
        private String attributeName;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new NegativeValueException object.
         *
         * @param  both     DOCUMENT ME!
         * @param  message  DOCUMENT ME!
         */
        public NegativeValueException(final boolean both, final String message) {
            super(message);
            this.both = both;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the both
         */
        public boolean isBoth() {
            return both;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the attributeName
         */
        public String getAttributeName() {
            return attributeName;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  attributeName  the attributeName to set
         */
        public void setAttributeName(final String attributeName) {
            this.attributeName = attributeName;
        }
    }
}
