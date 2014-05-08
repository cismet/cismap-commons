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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import edu.umd.cs.piccolo.util.PObjectOutputStream;

import org.apache.log4j.Logger;

import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.wrapper.ConnectionWrapper;
import org.h2gis.utilities.wrapper.StatementWrapper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.ObjectInputStream;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingWorker;

import javax.xml.bind.DatatypeConverter;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.features.JDBCFeatureInfo;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.features.ShapeInfo;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class H2FeatureServiceFactory extends JDBCFeatureFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static Logger LOG = Logger.getLogger(H2FeatureServiceFactory.class);
    public static final String DB_NAME = "~/cismap/internal";

    //~ Instance fields --------------------------------------------------------

    protected Vector<FeatureServiceAttribute> featureServiceAttributes;
    private String geometryField;
    private String idField = "id";
    private ConnectionWrapper conn;
    private JDBCFeatureInfo info;
    private String name;

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
     * @param  shapeFile     DOCUMENT ME!
     * @param  workerThread  DOCUMENT ME!
     */
    public H2FeatureServiceFactory(final String name,
        final String databasePath,
        final String tableName,
        final File shapeFile,
        final SwingWorker workerThread,
        final Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles) {
        super(databasePath, tableName);
        this.name = name;
//        this.styles = styles;
        initConnection();

        if (shapeFile != null) {
            importShapeFile(workerThread, shapeFile);
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
    private void importShapeFile(final SwingWorker workerThread, final File file) {
        try {
            ResultSet rs = conn.getMetaData().getTables(null, null, "GEOMETRY_COLUMNS", null);
            final StatementWrapper st = createStatement();

            if (!rs.next()) {
                st.execute(
                    "CREATE ALIAS IF NOT EXISTS SPATIAL_INIT FOR  \"org.h2gis.h2spatialext.CreateSpatialExtension.initSpatialExtension\";");
                st.execute("CALL SPATIAL_INIT();");
            }
            rs.close();

            rs = conn.getMetaData().getTables(null, null, tableName, null);

            if (!rs.next()) {
                if (workerThread != null) {
                    workerThread.firePropertyChange("progress", 5, -1);
                }
                final String tmpTableReference = tableName + "_temp_reference";
                st.execute("CALL FILE_TABLE('" + file.getAbsolutePath() + "', '" + tmpTableReference + "');");
                st.execute("create table \"" + tableName + "\" as select * from " + tmpTableReference);
                st.execute("drop table " + tmpTableReference + ";");

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

                        st.execute("CREATE SPATIAL INDEX " + indexName + " ON \"" + tableName + "\"(" + colName + ");");
                        st.execute("UPDATE \"" + tableName + "\" set " + colName + " = st_setsrid(" + colName + ", "
                                    + srid
                                    + ")");
                    }
                }
                rs.close();

                if (!hasIdField) {
                    final String seqName = tableName + "_seq";

                    st.execute("CREATE SEQUENCE " + seqName + ";");
                    st.execute("ALTER TABLE \"" + tableName + "\" ADD COLUMN id int default " + seqName + ".nextval;");
                }
                final String indexName = tableName + "PIndex";
                st.execute("ALTER TABLE \"" + tableName + "\" ALTER COLUMN id SET NOT NULL;");
                st.execute("CREATE PRIMARY KEY " + indexName + " ON \"" + tableName + "\"(id);");

                // add pfeature

// st.execute("alter table \"" + tableName + "\" ADD COLUMN pfeature ARRAY;");
            }
            st.close();
        } catch (SQLException e) {
            logger.error("Error while creating new shape table", e);
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
                    }
                }

                if (featureServiceAttributes.isEmpty() && !upperCaseTryed) {
                    rs.close();
                    rs = conn.getMetaData().getColumns(null, null, tableName.toUpperCase(), "%");
                    upperCaseTryed = true;
                }
            } while (rep < 2);
            rs.close();

            final StatementWrapper st = createStatement();
            final ResultSet envelopeSet = st.executeQuery("SELECT ST_Extent(" + geometryField + "), (SELECT st_srid("
                            + geometryField + ") from \"" + tableName + "\" limit 1) from \"" + tableName + "\";");

            if (envelopeSet.next()) {
                final Envelope e = ((Envelope)envelopeSet.getObject(1));
                final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                        envelopeSet.getInt(2));
                envelope = gf.toGeometry(e);

                envelopeSet.close();
            } else {
                logger.error("cannot determine H2 layer envelope");
            }

            st.close();
        } catch (Exception e) {
            LOG.error("Error while reading meta information of table " + databasePath + "." + tableName, e);
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

        if ((query != null) && !query.equals("")) {
            sb.append(" and ").append(query);
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
            List style = getStyle(name);

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
//                updateLastCreatedFeatures(selectedFeatures, boundingBox.getGeometry(srid), query);
                updateLastCreatedFeatures(selectedFeatures);
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
        sb.append(tableName)
                .append("\" WHERE ")
                .append(geometryField)
                .append(" && '")
                .append(bb.getGeometry(srid))
                .append("'");
        final String query = sb.toString();
        int result = 0;

        try {
            final StatementWrapper st = createStatement();
            final ResultSet rs = st.executeQuery(query);

            if (rs.next()) {
                result = rs.getInt(1);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            LOG.error("Error while determining the feature count. Query: " + query, e);
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
}
