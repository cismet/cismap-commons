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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.apache.log4j.Logger;

import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.wrapper.ConnectionWrapper;
import org.h2gis.utilities.wrapper.StatementWrapper;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingWorker;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.features.ShapeInfo;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.interaction.CismapBroker;
import java.io.File;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class H2FeatureServiceFactory extends JDBCFeatureFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static Logger LOG = Logger.getLogger(H2FeatureServiceFactory.class);

    //~ Instance fields --------------------------------------------------------

    protected Vector<FeatureServiceAttribute> featureServiceAttributes;
    private String databasePath;
    private String tableName;
    private String geometryField;
    private String idField = "id";
    private ConnectionWrapper conn;
    private StatementWrapper st;

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
     * @param  databasePath  DOCUMENT ME!
     * @param  tableName     DOCUMENT ME!
     */
    public H2FeatureServiceFactory(final String databasePath, final String tableName) {
        this(databasePath, tableName, null);
    }
    
    /**
     * Creates a new H2FeatureServiceFactory object.
     *
     * @param  databasePath  DOCUMENT ME!
     * @param  tableName     DOCUMENT ME!
     */
    public H2FeatureServiceFactory(final String databasePath, final String tableName, final File shapeFile) {
        this.databasePath = databasePath;
        this.tableName = tableName;
        initConnection();

        if (shapeFile != null) {
            importShapeFile(shapeFile);
        }
        initFactory();
    }

    //~ Methods ----------------------------------------------------------------

    private void importShapeFile(final File file) {
        try {
            ResultSet rs = conn.getMetaData().getTables(null, null, "GEOMETRY_COLUMNS", null);
            
            if (!rs.next()) {
                st.execute("CREATE ALIAS IF NOT EXISTS SPATIAL_INIT FOR  \"org.h2gis.h2spatialext.CreateSpatialExtension.initSpatialExtension\";");
                st.execute("CALL SPATIAL_INIT();");            
            }
            rs.close();
            
            rs = conn.getMetaData().getTables(null, null, tableName, null);
            
            if (!rs.next()) {
                String tmpTableReference = tableName + "_temp_reference";
                st.execute("CALL FILE_TABLE('" + file.getAbsolutePath() + "', '" + tmpTableReference + "');");
                st.execute("create table " + tableName + " as select * from " + tmpTableReference );
                
                rs = conn.getMetaData().getColumns(null, null, tableName.toUpperCase(), "%");
                boolean hasIdField = false;

                while (rs.next()) {
                    if (rs.getString("COLUMN_NAME").toUpperCase().equals("ID")) {
                        hasIdField = true;
                    }
                    
                    if (rs.getString("TYPE_NAME").toUpperCase().endsWith("GEOMETRY")) {
                        String colName = rs.getString("COLUMN_NAME");
                        String indexName = colName + tableName + "SpatialIndex";
                        int srid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getSrs().getCode());
                        
                        st.execute("CREATE SPATIAL INDEX " + indexName + " ON " + tableName + "(" + colName + ");");
                        st.execute("UPDATE " + tableName + " set " + colName + " = st_setsrid(" + colName + ", " + srid + ")");
                    }
                }
                rs.close();
                
                if (!hasIdField) {
                    String seqName = tableName + "_seq";

                    st.execute("CREATE SEQUENCE " + seqName + ";");
                    st.execute("ALTER TABLE " + tableName + " ADD COLUMN id int default " + seqName + ".nextval;");
//                    st.execute("ALTER TABLE " + tableName + " ADD COLUMN id int;");
//                    st.execute("CREATE INDEX " + tableName + "primaryIndex ON " + tableName + "(id);");
                }
                String indexName = tableName + "PIndex";
                st.execute("ALTER TABLE " + tableName + " ALTER COLUMN id SET NOT NULL;");
                st.execute("CREATE PRIMARY KEY " + indexName + " ON " + tableName + "(id);");
            }
        } catch (SQLException e) {
            logger.error("Error while creating new shape table");
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
            
            ResultSet envelopeSet = st.executeQuery("SELECT ST_Extent(" + geometryField + "), (SELECT st_srid(" + geometryField + ") from " + tableName + " limit 1) from " + tableName + ";");
            
            if (envelopeSet.next()) {
                Envelope e = ((Envelope)envelopeSet.getObject(1));
                GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), envelopeSet.getInt(2));
                envelope = gf.toGeometry(e);
                
                envelopeSet.close();
            } else {
                logger.error("cannot determine H2 layer envelope");
            }
            st.executeQuery("select " + geometryField + " from " + tableName);
        } catch (Exception e) {
            LOG.error("Error while reading meta information of table " + databasePath + "." + tableName, e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final H2FeatureServiceFactory h2 = new H2FeatureServiceFactory("~/db/test2", "poly");
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
            st = (StatementWrapper)conn.createStatement();
        } catch (Exception e) {
            LOG.error("Error while creating database connection.", e);
        }
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
        return createFeatures(query, boundingBox, workerThread, 0, 0, null);
    }

    @Override
    public List createFeatures(final Object query,
            final BoundingBox boundingBox,
            final SwingWorker workerThread,
            final int offset,
            final int limit,
            final FeatureServiceAttribute[] orderBy) throws TooManyFeaturesException, Exception {
        final long start = System.currentTimeMillis();
//        final StringBuilder sb = new StringBuilder("select id, the_geom from ");
        final StringBuilder sb = new StringBuilder("select id from ");
        final int srid = CrsTransformer.extractSridFromCrs(crs.getCode());
        sb.append(tableName)
                .append(" WHERE ")
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
                LOG.error("db query: " + select);
            }
            setInterruptedNotAllowed();
            ResultSet rs = st.executeQuery(select);
            setInterruptedAllowed();
            final List<JDBCFeature> selectedFeatures = new ArrayList<JDBCFeature>();
            LOG.error("H2:  " + (System.currentTimeMillis() - start));
            ShapeInfo info = new ShapeInfo(tableName, st, 35833, geometryField, tableName);

            while (rs.next()) {
                if (workerThread.isCancelled()) {
                    return null;
                }
                final JDBCFeature feature = new JDBCFeature(info);
//                for (final FeatureServiceAttribute attr : featureServiceAttributes) {
//                    feature.addProperty(attr.getName(), rs.getObject(attr.getName()));
//                }
                feature.setId(rs.getInt(idField));
//                feature.setGeometry((Geometry)rs.getObject(geometryField));
//                feature.getGeometry().setSRID(35833);
                feature.setLayerProperties(this.getLayerProperties());
                selectedFeatures.add(feature);
            }

            rs.close();
            updateLastCreatedFeatures(selectedFeatures);
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
        final StringBuilder sb = new StringBuilder("select count(*) from ");
        sb.append(tableName)
                .append(" WHERE ")
                .append(geometryField)
                .append(" intersects ")
                .append(bb.getGeometryFromTextCompatibleString());
        final String query = sb.toString();
        int result = 0;

        try {
            final ResultSet rs = st.executeQuery(query);

            if (rs.next()) {
                result = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            LOG.error("Error while determining the feature count. Query: " + query, e);
        }

        return result;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
    
    private void closeConnection() {
        try {
            if (st != null) {
                st.close();
            }
            
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            //nothing to do
        }
    }
}
