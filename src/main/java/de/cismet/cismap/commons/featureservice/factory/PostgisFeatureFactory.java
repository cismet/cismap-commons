package de.cismet.cismap.commons.featureservice.factory;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.features.InputEventAwareFeature;
import de.cismet.cismap.commons.features.PostgisFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.SimpleFeatureServiceSqlStatement;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.jtsgeometryfactories.PostGisGeometryFactory;
import de.cismet.cismap.commons.retrieval.RetrievalService;
import de.cismet.tools.ConnectionInfo;
import edu.umd.cs.piccolo.event.PInputEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.util.logging.Level;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.postgis.Geometry;
import org.postgis.PGgeometry;
import org.postgresql.PGConnection;

public class PostgisFeatureFactory extends AbstractFeatureFactory<PostgisFeature, SimpleFeatureServiceSqlStatement>
{
  public final static String ID_TOKEN = "<cismap::update::id>";//NOI18N
  public static final String QUERY_CANCELED = "57014";//NOI18N
  private Connection connection;
  protected final ConnectionInfo connectionInfo;
  protected final PostgisAction postgisAction;
  protected final RetrievalService parentService;

  protected PostgisFeatureFactory(PostgisFeatureFactory pff)
  {
    super(pff);
    this.connectionInfo = pff.connectionInfo;
    this.postgisAction = pff.postgisAction;
    this.parentService = pff.parentService;
    try
    {
      this.connection = createConnection(connectionInfo);
    }
    catch(Throwable t)
    {
      logger.error("could not create connection: "+t.getMessage(),t);//NOI18N
    }
  }

  public PostgisFeatureFactory(LayerProperties layerProperties, ConnectionInfo connectionInfo, PostgisAction postgisAction, RetrievalService parentService)
          throws Exception
  {
    //this.setLayerProperties(layerProperties);
    this.layerProperties = layerProperties;
    this.connectionInfo = connectionInfo;
    this.postgisAction = postgisAction;
    this.parentService = parentService;
    this.connection = createConnection(connectionInfo);
  }

  protected Connection createConnection(ConnectionInfo connectionInfo) throws Exception
  {
    try
    {
      this.logger.info("creating new PostgisFeatureFactory instance with connection: connection: \n" + connectionInfo.getUrl() + ", " + connectionInfo.getDriver() + ", " + connectionInfo.getUser());//NOI18N
      Class.forName(connectionInfo.getDriver());
      Connection theConnection = DriverManager.getConnection(connectionInfo.getUrl(), connectionInfo.getUser(), connectionInfo.getPass());
      ((PGConnection) theConnection).addDataType("geometry", "org.postgis.PGgeometry");//NOI18N
      ((PGConnection) theConnection).addDataType("box3d", "org.postgis.PGbox3d");//NOI18N
      return theConnection;
    } catch (Throwable t)
    {
      this.logger.fatal("could not create database connection (" + connectionInfo + "):\n " + t.getMessage(), t);//NOI18N
      throw new Exception("could not create database connection (" + connectionInfo + "):\n " + t.getMessage(), t);//NOI18N
    }
  }

  @Override
  protected boolean isGenerateIds()
  {
    return false;
  }

  @Override
  public synchronized Vector<PostgisFeature> createFeatures(SimpleFeatureServiceSqlStatement sqlStatement, BoundingBox boundingBox, SwingWorker workerThread)
          throws FeatureFactory.TooManyFeaturesException, Exception
  {
    if (checkCancelled(workerThread, "createFeatures()"))//NOI18N
    {
      return null;
    }

    Statement statement = null;
    Vector postgisFeatures = null;
    long start = System.currentTimeMillis();
    try
    {
      if ((this.connection == null) || (this.connection.isClosed()))
      {
        this.logger.error("FRW[" + workerThread + "]: Connection to database lost or not correctly initialised");//NOI18N
        this.connection = createConnection(this.connectionInfo);
      }

      sqlStatement.setX1(boundingBox.getX1());
      sqlStatement.setX2(boundingBox.getX2());
      sqlStatement.setY1(boundingBox.getY1());
      sqlStatement.setY2(boundingBox.getY2());

      if (checkCancelled(workerThread, "initialising sql statement()"))//NOI18N
      {
        cleanup(statement);
        return null;
      }
      statement = this.connection.createStatement();
      this.logger.debug("FRW[" + workerThread + "]: executing count features statement: " + sqlStatement.getCountFeaturesStatement());//NOI18N
      ResultSet resultSet = statement.executeQuery(sqlStatement.getCountFeaturesStatement());

      if (checkCancelled(workerThread, "initialising sql statement()"))//NOI18N
      {
        cleanup(statement);
        return null;
      }

      int count = 0;
      if (resultSet.next())
      {
        count = resultSet.getInt(1);
      }

      resultSet.close();
      this.logger.debug("FRW[" + workerThread + "]: " + count + " matching features in selected bounding box");//NOI18N
      if (count > getMaxFeatureCount())
      {
        throw new FeatureFactory.TooManyFeaturesException("FRW[" + workerThread + "]: feature in feature document " + count + " exceeds max feature count " + getMaxFeatureCount());//NOI18N
      }
      if (count == 0)
      {
        this.logger.warn("FRW[" + workerThread + "]: no features found in selected bounding ");//NOI18N
        return null;
      }
      this.logger.info("FRW[" + workerThread + "]: "+count+" postgis features found by sql statement");//NOI18N

      if (checkCancelled(workerThread, " counting postgis features"))//NOI18N
      {
        cleanup(statement);
        return null;
      }
      this.logger.debug("FRW[" + workerThread + "]: executing select features statement: " + sqlStatement.getFeaturesStatement());//NOI18N
      resultSet = statement.executeQuery(sqlStatement.getFeaturesStatement());

      postgisFeatures = new Vector(count);
      int j = 0;
      while (resultSet.next())
      {
        if (checkCancelled(workerThread, " processing postgis feature #" + count))//NOI18N
        {
          cleanup(statement);
          return null;
        }
        String name = "";//NOI18N
        try
        {
          name = resultSet.getObject(PostgisFeature.OBJECT_NAME_PROPERTY).toString();
        } catch (Exception e)
        {
          if (DEBUG)
          {
            logger.warn("FRW[" + workerThread + "]: name is null");//NOI18N
          }
        }

        String type = "";//NOI18N
        try
        {

          type = resultSet.getObject(PostgisFeature.FEATURE_TYPE_PROPERTY).toString();
        } catch (Exception e)
        {
          if (DEBUG)
          {
            logger.warn("FRW[" + workerThread + "]: type is null");//NOI18N
          }
        }

        String groupingKey = "";//NOI18N
        try
        {
          groupingKey = resultSet.getObject(PostgisFeature.GROUPING_KEY_PROPERTY).toString();
        } catch (Exception e)
        {
          if (DEBUG)
          {
            logger.warn("FRW[" + workerThread + "]: GroupingKey is null");//NOI18N
          }
        }

        int id = -1;
        try
        {
          id = resultSet.getInt(PostgisFeature.ID_PROPERTY);
        } catch (Exception e)
        {
           logger.warn("FRW[" + workerThread + "]: Id is null",e);//NOI18N

           if (DEBUG)
          {
            logger.warn("FRW[" + workerThread + "]: Id is null");//NOI18N
          }
        }

        PGgeometry postgresGeom = (PGgeometry) resultSet.getObject(PostgisFeature.GEO_PROPERTY);
        Geometry postgisGeom = postgresGeom.getGeometry();

        PostgisFeature postgisFeature;
        
        if(this.postgisAction != null)
        {
          postgisFeature = new UpdateablePostgisFeature();
        }
        else
        {
          postgisFeature = new PostgisFeature();
        }

        postgisFeature.setId(id);
        postgisFeature.setGeometry(PostGisGeometryFactory.createJtsGeometry(postgisGeom));
        postgisFeature.setFeatureType(type);
        postgisFeature.setGroupingKey(groupingKey);
        postgisFeature.setObjectName(name);
        postgisFeature.setLayerProperties(getLayerProperties());

        evaluateExpressions(postgisFeature, j);
        postgisFeatures.add(postgisFeature);
        ++j;
      }
    } catch (Exception e)
    {
      SQLException se;
      this.logger.error("FRW[" + workerThread + "]: Exception during Postgis Featureretrieval: \n" + e.getMessage(), e);//NOI18N
      if (e instanceof SQLException)
      {
        se = (SQLException) e;
      }

      throw e;
    } finally
    {
      cleanup(statement);
    }

    this.logger.info("FRW[" + workerThread + "]: Postgis request took " + (System.currentTimeMillis() - start) + " ms");//NOI18N
    updateLastCreatedFeatures(postgisFeatures);
    return postgisFeatures;
  }

  @Override
  public Vector createAttributes(SwingWorker workerThread)
          throws FeatureFactory.TooManyFeaturesException, Exception
  {
    Vector featureServiceAttributes = new Vector(4);
    featureServiceAttributes.add(new FeatureServiceAttribute(PostgisFeature.GEO_PROPERTY, "gml:GeometryPropertyType", true));//NOI18N
    featureServiceAttributes.add(new FeatureServiceAttribute(PostgisFeature.ID_PROPERTY, "1", true));//NOI18N
    featureServiceAttributes.add(new FeatureServiceAttribute(PostgisFeature.FEATURE_TYPE_PROPERTY, "2", true));//NOI18N
    featureServiceAttributes.add(new FeatureServiceAttribute(PostgisFeature.GROUPING_KEY_PROPERTY, "2", true));//NOI18N
    featureServiceAttributes.add(new FeatureServiceAttribute(PostgisFeature.OBJECT_NAME_PROPERTY, "2", true));//NOI18N
    return featureServiceAttributes;
  }

  protected void cleanup(Statement statement)
  {
    if (statement == null)
    {
      return;
    }
    try
    {
      statement.cancel();
      statement.close();
      statement = null;
    } catch (Exception ex)
    {
    }
  }

  protected void doAction(int id) throws Exception
  {
    if (this.postgisAction.getAction() != null && this.postgisAction.getAction().length() > 0)
    {
      if (this.connection == null || this.connection.isClosed())
      {
        this.logger.error("Connection to database lost or not correctly initialised");//NOI18N
        this.connection = createConnection(this.connectionInfo);
      }

      java.sql.Statement statement = connection.createStatement();
      String sql = this.postgisAction.getAction().replaceAll(ID_TOKEN, String.valueOf(id));
      if(DEBUG)logger.debug("performing action on feature #"+id+": \n"+sql);//NOI18N
      statement.execute(sql);
      statement.close();

    } else
    {
      logger.warn("Feature Service not yet correclty initialised, ignoring action");//NOI18N
      throw new Exception("Feature Service not yet correclty initialised, ignoring action");//NOI18N
    }
  }

  private class UpdateablePostgisFeature extends PostgisFeature implements InputEventAwareFeature
  {

    @Override
    public boolean noFurtherEventProcessing(PInputEvent event)
    {
      return true;
    }

    @Override
    public void mouseClicked(PInputEvent event)
    {
    }

    @Override
    public void mouseEntered(PInputEvent event)
    {
    }

    @Override
    public void mouseExited(PInputEvent event)
    {
    }

    @Override
    public void mousePressed(PInputEvent event)
    {
      final MappingComponent mappingComponent = (MappingComponent) event.getComponent();
      if (!mappingComponent.isReadOnly() && event.getModifiers() == InputEvent.BUTTON3_MASK)
      {
        if(DEBUG)logger.debug("showing menu on feature #"+this.getId());//NOI18N
        JPopupMenu pop = new JPopupMenu();
        JMenuItem mni = new JMenuItem(PostgisFeatureFactory.this.postgisAction.getActionText(), PostgisFeatureFactory.this.postgisAction.getIcon());
        mni.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            try
            {
              PostgisFeatureFactory.this.doAction(UpdateablePostgisFeature.this.getId());
              PostgisFeatureFactory.this.parentService.retrieve(true);
            } catch (Exception ex)
            {
              logger.error("Error during doAction(): " + ex.getMessage(), ex);//NOI18N
              ErrorInfo ei = new ErrorInfo(org.openide.util.NbBundle.getMessage(PostgisFeatureFactory.class, "PostgisFeatureFactory.mousePressed.ErrorInfo.title") , org.openide.util.NbBundle.getMessage(PostgisFeatureFactory.class, "PostgisFeatureFactory.mousePressed.ErrorInfo.basicErrorMessage") , null, null, ex, Level.ALL, null);//NOI18N
              JXErrorPane.showDialog(mappingComponent, ei);
            }
          }
        });
        pop.add(mni);
        pop.show(mappingComponent, (int) event.getCanvasPosition().getX(), (int) event.getCanvasPosition().getY());
      }
    }

    @Override
    public void mouseWheelRotated(PInputEvent event)
    {
    }

    @Override
    public void mouseReleased(PInputEvent event)
    {
    }

    @Override
    public void mouseMoved(PInputEvent event)
    {
    }

    @Override
    public void mouseDragged(PInputEvent event)
    {
    }
  }

  @Override
  public PostgisFeatureFactory clone()
  {
    return new PostgisFeatureFactory(this);
  }
}
