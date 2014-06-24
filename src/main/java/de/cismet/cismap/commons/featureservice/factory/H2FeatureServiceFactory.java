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
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import edu.umd.cs.piccolo.util.PObjectOutputStream;

import org.apache.log4j.Logger;

import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.wrapper.ConnectionWrapper;
import org.h2gis.utilities.wrapper.StatementWrapper;

import org.jfree.util.Log;

import org.openide.util.NbBundle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.nio.charset.Charset;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import de.cismet.cismap.commons.featureservice.H2AttributeTableRuleSet;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.LinearReferencingInfo;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import de.cismet.cismap.commons.gui.options.CapabilityWidgetOptionsPanel;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.FeatureTools;

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
    public static final int STATION = 1;
    public static final int STATION_LINE = 2;
    private static final String INSERT_LR_META_DATA = "INSERT INTO \"" + LR_META_TABLE_NAME
                + "\" (table, lin_ref_reference, domain, src_join_field, targ_join_field, lin_ref_geom, kind, from_value, till_value) VALUES ('%s','%s','%s','%s','%s','%s',%s,'%s','%s');";
    private static final String INSERT_META_DATA = "INSERT INTO \"" + META_TABLE_NAME
                + "\" (table, format) VALUES ('%s','%s');";

    private static Logger LOG = Logger.getLogger(H2FeatureServiceFactory.class);
    public static final String DB_NAME = "~/cismap/internal";
    private static final String CREATE_SPATIAL_INDEX = "CREATE SPATIAL INDEX %s ON \"%s\"(%s);";
    private static final String UPDATE_SRID = "UPDATE \"%1$s\" set %2$s = st_setsrid(%2$s, %3$s)";
    private static final String CREATE_TABLE_FROM_CSV = "CREATE TABLE \"%s\" as select * from CSVREAD('%s');";
    private static final String CREATE_TABLE_FROM_DBF = "CALL FILE_TABLE('%s', '%s');";
    private static final String COPY_TABLE_FROM_DBF = "create table \"%s\" as select * from %s";
    private static final String DROP_TABLE_REFERENCE = "drop table %s;";
    private static final String SELECT_COLUMN = "select %s, id from \"%s\"";
    private static final String UPDATE_COLUMN = "update \"%s\" set %s = ? where id = ?";
    private static final String CREATE_TABLE_TEMPLATE = "create table \"%s\" (%s)";
    private static final String INSERT_TEMPLATE = "INSERT INTO \"%s\" (%s) VALUES (%s)";
    private static final String SPATIAL_INIT = "CALL SPATIAL_INIT();";
    private static final String CREATE_SPATIAL_INIT_ALIAS =
        "CREATE ALIAS IF NOT EXISTS SPATIAL_INIT FOR  \"org.h2gis.h2spatialext.CreateSpatialExtension.initSpatialExtension\";";
    private static final String CREATE_SEQUENCE = "CREATE SEQUENCE %s;";
    private static final String ADD_SEQUENCE = "ALTER TABLE \"%s\" ADD COLUMN id int default %s.nextval;";
    private static final String ADD_NOT_NULL_ID = "ALTER TABLE \"%s\" ALTER COLUMN id SET NOT NULL;";
    private static final String CREATE_PRIMARY_KEY = "CREATE PRIMARY KEY %s ON \"%s\"(id);";
    private static final String CREATE_LR_META_TABLE =
        "create table \"%s\" (id serial, table varchar, lin_ref_reference varchar, domain varchar, src_join_field varchar, targ_join_field varchar, lin_ref_geom varchar, kind int, from_value varchar, till_value varchar);";
    private static final String CREATE_META_TABLE = "create table \"%s\" (id serial, table varchar, format varchar);";

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
        initConnection();
    }

    /**
     * Creates a new H2FeatureServiceFactory object.
     *
     * @param  name          DOCUMENT ME!
     * @param  databasePath  DOCUMENT ME!
     * @param  tableName     DOCUMENT ME!
     * @param  file          supported file formats are shp, dbf and csv (really comma separated)
     * @param  workerThread  DOCUMENT ME!
     * @param  styles        DOCUMENT ME!
     */
    public H2FeatureServiceFactory(final String name,
            final String databasePath,
            final String tableName,
            final File file,
            final SwingWorker workerThread) {
        super(databasePath, tableName);
        this.name = name;
//        this.styles = styles;
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
     */
    public H2FeatureServiceFactory(final String name,
            final String databasePath,
            final String tableName,
            final List<FeatureServiceFeature> featureList,
            final SwingWorker workerThread) {
        super(databasePath, tableName);
        this.name = name;
//        this.styles = styles;
        initConnection();

        if (featureList != null) {
            importFeatures(workerThread, featureList);
        }
        initFactory();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  workerThread  DOCUMENT ME!
     * @param  file          DOCUMENT ME!
     */
    private void importFile(final SwingWorker workerThread, final File file) {
        try {
            final StatementWrapper st = createStatement();
            initDatabase();
            ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null);

            if (!rs.next()) {
                rs.close();
                if (workerThread != null) {
                    workerThread.firePropertyChange("progress", 5, -1);
                }
                createMetaTableIfNotExist();
                final String tmpTableReference = tableName + "_temp_reference";

                if (file.getAbsolutePath().toLowerCase().endsWith("csv")) {
                    st.execute(String.format(CREATE_TABLE_FROM_CSV, tableName, file.getAbsolutePath()));
                } else {
                    st.execute(String.format(CREATE_TABLE_FROM_DBF, file.getAbsolutePath(), tmpTableReference));
                    st.execute(String.format(COPY_TABLE_FROM_DBF, tableName, tmpTableReference));
                    st.execute(String.format(DROP_TABLE_REFERENCE, tmpTableReference));
                }
                final String format = file.getAbsolutePath()
                            .toLowerCase()
                            .substring(file.getAbsolutePath().length() - 3);
                st.execute(String.format(INSERT_META_DATA, tableName, format));

                rs = conn.getMetaData().getColumns(null, null, tableName, "%");
                boolean hasIdField = false;

                while (rs.next()) {
                    if (rs.getString("COLUMN_NAME").toUpperCase().equals("ID")) {
                        hasIdField = true;
                    }

                    if (rs.getString("TYPE_NAME").toUpperCase().endsWith("GEOMETRY")) {
                        final String colName = rs.getString("COLUMN_NAME");
                        final String indexName = colName + tableName + "SpatialIndex";
                        final int srid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs()
                                        .getCode());

                        st.execute(String.format(CREATE_SPATIAL_INDEX, indexName, tableName, colName));
                        st.execute(String.format(UPDATE_SRID, tableName, colName, srid));
                    }
                }
                rs.close();

                createPrimaryKey(hasIdField);

                if (!file.getAbsolutePath().toLowerCase().endsWith("csv")) {
                    final Charset charset = getCharsetDefinition(file.getAbsolutePath());

                    if ((charset != null) && !charset.name().equals(DEFAULT_DBF_CHARSET)) {
                        // check and correct encoding
                        rs = conn.getMetaData().getColumns(null, null, tableName, "%");
                        // ISO-8859-1 is the default charset of dbf files. If the file has an other encoding,
                        // all text fields must be converted
                        while (rs.next()) {
                            if ((rs.getInt("DATA_TYPE") == java.sql.Types.VARCHAR)
                                        || (rs.getInt("DATA_TYPE") == java.sql.Types.CHAR)
                                        || (rs.getInt("DATA_TYPE") == java.sql.Types.NCHAR)
                                        || (rs.getInt("DATA_TYPE") == java.sql.Types.NVARCHAR)) {
                                final ResultSet dataRs = st.executeQuery(String.format(
                                            SELECT_COLUMN,
                                            rs.getString("COLUMN_NAME"),
                                            tableName));
                                final PreparedStatement updateSt = conn.prepareStatement(String.format(
                                            UPDATE_COLUMN,
                                            tableName,
                                            rs.getString("COLUMN_NAME")));

                                while (dataRs.next()) {
                                    updateSt.setString(
                                        1,
                                        new String(dataRs.getString(1).getBytes(DEFAULT_DBF_CHARSET), charset));
                                    updateSt.setInt(2, dataRs.getInt(2));
                                    updateSt.execute();
                                }

                                dataRs.close();
                            }
                        }
                        rs.close();
                    }
                }
            }
            st.close();

            final CapabilityWidget cap = CapabilityWidgetOptionsPanel.getCapabilityWidget();

            if (cap != null) {
                cap.refreshJdbcTrees();
            }
        } catch (Exception e) {
            logger.error("Error while creating new shape table", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  workerThread  DOCUMENT ME!
     * @param  features      file DOCUMENT ME!
     */
    private void importFeatures(final SwingWorker workerThread, final List<FeatureServiceFeature> features) {
        try {
            final StatementWrapper st = createStatement();

            initDatabase();
            ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null);

            if (!rs.next()) {
                if (workerThread != null) {
                    workerThread.firePropertyChange("progress", 5, -1);
                }

                final Map<String, FeatureServiceAttribute> attributeMap = features.get(0)
                            .getLayerProperties()
                            .getFeatureService()
                            .getFeatureServiceAttributes();
                final StringBuilder tableAttributesWithType = new StringBuilder();
                final StringBuilder tableAttributesWithoutType = new StringBuilder();
                final StringBuilder placeholder = new StringBuilder();
                boolean firstAttr = true;
                boolean hasIdField = false;

                for (final String attrKey : attributeMap.keySet()) {
                    if (!firstAttr) {
                        tableAttributesWithType.append(",");
                        tableAttributesWithoutType.append(",");
                        placeholder.append(",");
                    } else {
                        firstAttr = false;
                    }

                    final FeatureServiceAttribute attr = attributeMap.get(attrKey);
                    if (attr.getName().equalsIgnoreCase("id")) {
                        hasIdField = true;
                    }

                    tableAttributesWithType.append(attr.getName()).append(" ");
                    tableAttributesWithoutType.append(attr.getName());
                    placeholder.append("?");
                    tableAttributesWithType.append(FeatureTools.getH2DataType(attr));
                }

                st.execute(String.format(CREATE_TABLE_TEMPLATE, tableName, tableAttributesWithType.toString()));

                rs = conn.getMetaData().getColumns(null, null, tableName, "%");

                while (rs.next()) {
                    if (rs.getString("TYPE_NAME").toUpperCase().endsWith("GEOMETRY")) {
                        final String colName = rs.getString("COLUMN_NAME");
                        final String indexName = colName + tableName + "SpatialIndex";
                        final int srid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs()
                                        .getCode());
                        st.execute(String.format(CREATE_SPATIAL_INDEX, indexName, tableName, colName));
                        st.execute(String.format(UPDATE_SRID, tableName, colName, srid));
                    }
                }
                rs.close();
                final PreparedStatement prepStat = conn.prepareStatement(String.format(
                            INSERT_TEMPLATE,
                            tableName,
                            tableAttributesWithoutType,
                            placeholder));

                for (final FeatureServiceFeature f : features) {
                    int index = 0;
                    for (final String attrKey : attributeMap.keySet()) {
                        prepStat.setObject(++index, f.getProperty(attrKey));
                    }
                    prepStat.execute();
                }

                createPrimaryKey(hasIdField);
            }
            st.close();
        } catch (SQLException e) {
            logger.error("Error while creating a new table from existing features", e);
        }
    }

    /**
     * Initialises the database, if this was not already happen.
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private void initDatabase() throws SQLException {
        final ResultSet rs = conn.getMetaData().getTables(null, null, "GEOMETRY_COLUMNS", null);

        if (!rs.next()) {
            final StatementWrapper st = createStatement();
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
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private void createPrimaryKey(final boolean hasIdField) throws SQLException {
        final StatementWrapper st = createStatement();

        if (!hasIdField) {
            final String seqName = tableName + "_seq";

            st.execute(String.format(CREATE_SEQUENCE, seqName));
            st.execute(String.format(ADD_SEQUENCE, tableName, seqName));
        }
        final String indexName = tableName + "PIndex";
        st.execute(String.format(ADD_NOT_NULL_ID, tableName));
        st.execute(String.format(CREATE_PRIMARY_KEY, indexName, tableName));
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
            final Statement st = createStatement();
            st.execute(String.format(CREATE_LR_META_TABLE, LR_META_TABLE_NAME));
            st.close();
        }

        rs.close();
    }

    /**
     * Creates the meta table, if it does not exist.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void createMetaTableIfNotExist() throws Exception {
        final ResultSet rs = conn.getMetaData().getTables(null, null, META_TABLE_NAME, null);

        if (!rs.next()) {
            // meta table does not exist and should be created
            final Statement st = createStatement();
            st.execute(String.format(CREATE_META_TABLE, META_TABLE_NAME));
            st.close();
        }

        rs.close();
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
                final String csName = br.readLine();
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
     * @param  fromField       DOCUMENT ME!
     * @param  tillField       DOCUMENT ME!
     * @param  routeField      DOCUMENT ME!
     * @param  routeJoinField  DOCUMENT ME!
     * @param  routeService    DOCUMENT ME!
     * @param  layerName       DOCUMENT ME!
     * @param  domain          DOCUMENT ME!
     */
    public void setLinearReferencingInformation(final String fromField,
            final String tillField,
            final String routeField,
            final String routeJoinField,
            final AbstractFeatureService routeService,
            final String layerName,
            final String domain) {
        if (geometryField != null) {
            JOptionPane.showMessageDialog(CismapBroker.getInstance().getMappingComponent(),
                NbBundle.getMessage(
                    H2FeatureServiceFactory.class,
                    "H2FeatureServiceFactory.setLinearReferencingInformation.geometryAlreadyExists",
                    tableName),
                NbBundle.getMessage(
                    H2FeatureServiceFactory.class,
                    "H2FeatureServiceFactory.setLinearReferencingInformation.geometryAlreadyExists.title"),
                JOptionPane.ERROR_MESSAGE);
            return;
        }

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
            geometryField = "geo_" + geoCol;
            final StatementWrapper st = createStatement();
            st.execute("alter table \"" + tableName + "\" add column " + geometryField + " Geometry");
            String additionalFields = fromField + "," + routeField;
            final PreparedStatement linRefGeomUpdate = conn.prepareStatement("UPDATE \"" + tableName + "\" set "
                            + geometryField + " = ? WHERE id = ?");

            if (tillField != null) {
                additionalFields += "," + tillField;
            }

            final ResultSet rs = st.executeQuery("select id," + additionalFields + " from \"" + tableName + "\"");

            while (rs.next()) {
                final int id = rs.getInt(1);
                final double from = rs.getDouble(2);
                final Object routeId = rs.getObject(3);
                Geometry geom = null;
                final Geometry routeGeom = routeGeometries.get(routeId);
                if (routeGeom == null) {
                    LOG.warn("No geometry found for route " + routeId);
                    continue;
                }
                final LengthIndexedLine line = new LengthIndexedLine(routeGeom);

                if (tillField != null) {
                    final double till = rs.getDouble(4);

                    geom = line.extractLine(from, till);
                } else {
                    final Coordinate coords = line.extractPoint(from);
                    geom = routeGeom.getFactory().createPoint(coords);
                }

                linRefGeomUpdate.setObject(1, geom);
                linRefGeomUpdate.setInt(2, id);
                linRefGeomUpdate.execute();
            }

            rs.close();

            createMetaLinRefTablesIfNotExist();
            final String tillInfo = ((tillField == null) ? "" : tillField);
            final int kind = ((tillField == null) ? STATION : STATION_LINE);

            st.execute(String.format(
                    INSERT_LR_META_DATA,
                    tableName,
                    layerName,
                    domain,
                    routeField,
                    routeJoinField,
                    geometryField,
                    new Integer(kind),
                    fromField,
                    tillInfo));
            st.close();
        } catch (Exception e) {
            LOG.error("Error while joining linear referenced table.", e);
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

            createMetaLinRefTablesIfNotExist();
            final StatementWrapper st = createStatement();
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
                ((DefaultLayerProperties)layerProperties).setAttributeTableRuleSet(new H2AttributeTableRuleSet(
                        linRefList));
            }

            if (geometryField != null) {
                final ResultSet envelopeSet = st.executeQuery("SELECT ST_Extent(" + geometryField
                                + "), (SELECT st_srid("
                                + geometryField + "::Geometry) from \"" + tableName + "\" limit 1) from \"" + tableName
                                + "\" where " + geometryField + " is not null;");

                if (envelopeSet.next()) {
                    final Envelope e = ((Envelope)envelopeSet.getObject(1));
                    final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                            envelopeSet.getInt(2));
                    envelope = gf.toGeometry(e);

                    envelopeSet.close();
                } else {
                    logger.error("cannot determine H2 layer envelope");
                }
            }
            st.close();
        } catch (Exception e) {
            LOG.error("Error while reading meta information of table " + databasePath + "." + tableName, e);
        }
    }

    @Override
    public void setLayerProperties(final LayerProperties layerProperties) {
        super.setLayerProperties(layerProperties);
        if (layerProperties instanceof DefaultLayerProperties) {
            ((DefaultLayerProperties)layerProperties).setAttributeTableRuleSet(new H2AttributeTableRuleSet(linRefList));
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void updatePFeatures() {
        try {
            final PreparedStatement ps = conn.prepareStatement("update \"" + tableName
                            + "\" set pfeature = ? where id = ?;");
            final StatementWrapper upState = (StatementWrapper)conn.createStatement();
            final ResultSet resultSet = upState.executeQuery("select id, " + geometryField + " from \"" + tableName
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
        try {
            Class.forName("org.h2.Driver");
            conn = (ConnectionWrapper)SFSUtilities.wrapConnection(DriverManager.getConnection(
                        "jdbc:h2:"
                                + databasePath));
        } catch (Exception e) {
            LOG.error("Error while creating database connection.", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private synchronized StatementWrapper createStatement() throws SQLException {
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
            throws TooManyFeaturesException, Exception {
        return createFeaturesInternal(query, boundingBox, workerThread, 0, 80000, null, true);
    }

    @Override
    public List createFeatures(final Object query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread,
            final int offset,
            final int limit,
            final FeatureServiceAttribute[] orderBy) throws TooManyFeaturesException, Exception {
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
        final JDBCFeature feature = new JDBCFeature(info);
        feature.setId(getFreeId());
        feature.setLayerProperties(this.getLayerProperties());

        return feature;
    }

    /**
     * The next free id or -1 if the id cannot be determined.
     *
     * @return  the next free id or -1 if the id cannot be determined.
     */
    private int getFreeId() {
        int freeId = -1;
        Statement st = null;

        try {
            st = conn.createStatement();
            final String maxId = "SELECT max(%1s) from \"%2s\";";

            final String query = String.format(maxId, idField, tableName);
            final ResultSet rs = st.executeQuery(query);

            if ((rs != null) && rs.next()) {
                freeId = rs.getInt(1);
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
     * @throws  TooManyFeaturesException  DOCUMENT ME!
     * @throws  Exception                 DOCUMENT ME!
     */
    private List createFeaturesInternal(final Object query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread,
            final int offset,
            final int limit,
            final FeatureServiceAttribute[] orderBy,
            final boolean saveAsLastCreated) throws TooManyFeaturesException, Exception {
//        final StringBuilder sb = new StringBuilder("select id, the_geom from ");
        final StringBuilder sb = new StringBuilder("select id from \"");
        final int srid = CrsTransformer.extractSridFromCrs(crs.getCode());

        if (boundingBox != null) {
            sb.append(tableName)
                    .append("\" WHERE ")
                    .append(geometryField)
                    .append(" && '")
                    .append(boundingBox.getGeometry(srid))
                    .append("'");
//                .append("' and ")
//                .append("intersects(")
//                .append(geometryField)
//                .append(", '")
//                .append(boundingBox.getGeometry(srid))
//                .append("')");
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
            final StatementWrapper st = createStatement();
            final ResultSet rs = st.executeQuery(select);
            setInterruptedAllowed();
            final List<JDBCFeature> selectedFeatures = new ArrayList<JDBCFeature>();

            if (info == null) {
                info = new JDBCFeatureInfo(conn, 35833, geometryField, tableName);
            }
            final List style = getStyle(name);

            while (rs.next()) {
                if ((workerThread != null) && workerThread.isCancelled()) {
                    return null;
                }
                final JDBCFeature feature = new JDBCFeature(info, style);
                feature.setId(rs.getInt(idField));
                feature.setLayerProperties(this.getLayerProperties());
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
        }

        return new ArrayList<JDBCFeature>();
    }

    @Override
    public synchronized List createAttributes(final SwingWorker workerThread) throws TooManyFeaturesException,
        UnsupportedOperationException,
        Exception {
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
    public int getFeatureCount(final BoundingBox bb) {
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
        final String query = sb.toString();
        int result = 0;

        StatementWrapper st = null;
        ResultSet rs = null;

        try {
            st = createStatement();
            rs = st.executeQuery(query);
            if (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOG.error("Error while determining the feature count. Query: " + query, e);
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
    private void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            // nothing to do
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the linRefList
     */
    public List<LinearReferencingInfo> getLinRefList() {
        return linRefList;
    }
}
