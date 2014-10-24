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

import org.openide.util.Exceptions;

import java.io.File;

import java.net.URI;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;

/**
 * A service, that uses the internal db as data source.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class H2FeatureService extends JDBCFeatureService<JDBCFeature> {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(H2FeatureService.class);
    public static final Map<Integer, Icon> layerIcons = new HashMap<Integer, Icon>();
    public static final String H2_FEATURELAYER_TYPE = "H2FeatureServiceLayer"; // NOI18N

    static {
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

    private List<FeatureServiceFeature> features;
    private File shapeFile;
    private boolean initialised = true;
    private boolean tableNotFound = false;

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
     * @param   name          DOCUMENT ME!
     * @param   databasePath  DOCUMENT ME!
     * @param   tableName     DOCUMENT ME!
     * @param   attributes    DOCUMENT ME!
     * @param   shapeFile     DOCUMENT ME!
     * @param   features      DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public H2FeatureService(final String name,
            final String databasePath,
            final String tableName,
            final List<FeatureServiceAttribute> attributes,
            final File shapeFile,
            final List<FeatureServiceFeature> features) throws Exception {
        super(name, databasePath, tableName, attributes);
        this.shapeFile = shapeFile;
        this.features = features;
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
        if (features != null) {
            final FeatureFactory f = new H2FeatureServiceFactory(
                    name,
                    databasePath,
                    tableName,
                    features,
                    layerInitWorker,
                    parseSLD(getSLDDefiniton()));
            checkTable();

            return f;
        } else {
            final FeatureFactory f = new H2FeatureServiceFactory(
                    name,
                    databasePath,
                    tableName,
                    shapeFile,
                    layerInitWorker,
                    parseSLD(getSLDDefiniton()));
            checkTable();

            return f;
        }
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
            final H2FeatureServiceFactory f = (H2FeatureServiceFactory)featureFactory;
            ((DefaultLayerProperties)properties).setAttributeTableRuleSet(new H2AttributeTableRuleSet(
                    f.getLinRefList()));
        }

        return properties;
    }

    @Override
    public Icon getLayerIcon(final int type) {
        return layerIcons.get(type);
    }

    @Override
    public Object clone() {
        return new H2FeatureService(this);
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
            conn = (ConnectionWrapper)SFSUtilities.wrapConnection(DriverManager.getConnection(
                        "jdbc:h2:"
                                + H2FeatureServiceFactory.DB_NAME));
            rs = conn.getMetaData().getTables(null, null, tableName, null);
            tableExists = rs.next();
            rs.close();
            conn.close();
            return tableExists;
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
