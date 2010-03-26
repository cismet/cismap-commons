/*
 * SimplePostgisFeatureService.java
 * Copyright (C) 2009 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 11. Juli 2005, 17:34
 *
 */
package de.cismet.cismap.commons.featureservice;

import de.cismet.cismap.commons.features.PostgisFeature;
import de.cismet.cismap.commons.featureservice.factory.FeatureFactory;
import de.cismet.cismap.commons.featureservice.factory.PostgisFeatureFactory;
import de.cismet.tools.ConnectionInfo;
import java.awt.Color;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.jdom.CDATA;
import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author Pascal Dihé
 */
public class SimplePostgisFeatureService extends AbstractFeatureService<PostgisFeature, SimpleFeatureServiceSqlStatement>
{
  public static final String POSTGIS_FEATURELAYER_TYPE = "simplePostgisFeatureService";//NOI18N
  private SimpleFeatureServiceSqlStatement sqlStatement;
  private ConnectionInfo connectionInfo;

  public static final HashMap<Integer, Icon> layerIcons = new HashMap<Integer, Icon>();
  static
  {
    layerIcons.put(LAYER_ENABLED_VISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerPostgis.png")));//NOI18N
    layerIcons.put(LAYER_ENABLED_INVISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerPostgisInvisible.png")));//NOI18N
    layerIcons.put(LAYER_DISABLED_VISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerPostgis.png")));//NOI18N
    layerIcons.put(LAYER_DISABLED_INVISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerPostgisInvisible.png")));//NOI18N
  }

  /** Creates a new instance of SimplePostgisFeatureService */
  public SimplePostgisFeatureService(SimplePostgisFeatureService spfs)
  {
    super(spfs);
    this.setConnectionInfo(spfs.getConnectionInfo());
    this.setQuery(spfs.getQuery());
  }

  public SimplePostgisFeatureService(Element element) throws Exception
  {
    super(element);
  }

  @Override
  public void initFromElement(Element element) throws Exception
  {
    super.initFromElement(element);

    if(element.getChild("dbConnectionInfo") != null)//NOI18N
    {
      ConnectionInfo newConnectionInfo = new ConnectionInfo(element.getChild("dbConnectionInfo"));//NOI18N
      this.setConnectionInfo(newConnectionInfo);
      logger.debug("SimplePostgisFeatureService initialised with connection: \n" + this.getConnectionInfo().getUrl() + ", " + this.getConnectionInfo().getDriver() + ", " + this.getConnectionInfo().getUser());//NOI18N
    }
    else
    {
      logger.error("missing element 'dbConnectionInfo' in xml configuration");//NOI18N
    }

    // TODO: SimpleFeatureServiceSqlStatement should implement ConvertableToXML
    this.sqlStatement = new SimpleFeatureServiceSqlStatement(element.getChild("statement").getTextTrim());//NOI18N
    this.sqlStatement.setAllFields(element.getChild("allFields").getTextTrim());//NOI18N
    this.sqlStatement.setOrderBy(element.getChild("orderBy").getTextTrim());//NOI18N
  }

  @Override
  public Element toElement()
  {
    Element e = super.toElement();

    if (this.sqlStatement != null)
    {
      Element stmnt = new Element("statement");//NOI18N
      stmnt.addContent(new CDATA(sqlStatement.getSqlTemplate()));
      e.addContent(stmnt);
      Element allFields = new Element("allFields");//NOI18N
      allFields.addContent(new CDATA(sqlStatement.getAllFields()));
      e.addContent(allFields);
      Element orderBy = new Element("orderBy");//NOI18N
      orderBy.addContent(new CDATA(sqlStatement.getOrderBy()));
      e.addContent(orderBy);
    } else
    {
      logger.warn("sql statement is null and cannot be saved");//NOI18N
    }

    if (this.connectionInfo != null)
    {
      // TODO: ConnectionInfo should implement ConvertableToXML
      Element connectionElement = new Element("dbConnectionInfo");//NOI18N
      connectionElement.addContent(new Element("driverClass").addContent(this.getConnectionInfo().getDriver()));//NOI18N
      connectionElement.addContent(new Element("dbUrl").addContent(this.getConnectionInfo().getUrl()));//NOI18N
      connectionElement.addContent(new Element("user").addContent(this.getConnectionInfo().getUser()));//NOI18N
      connectionElement.addContent(new Element("pass").addContent(this.getConnectionInfo().getPass()));//NOI18N
      e.addContent(connectionElement);
    } else
    {
      logger.warn("connection info is null and cannot be saved");//NOI18N
    }

    return e;
  }

  @Override
  protected LayerProperties createLayerProperties()
  {
    DefaultLayerProperties defaultLayerProperties = new DefaultLayerProperties();
    defaultLayerProperties.getStyle().setLineColor(new Color(0.6f, 0.6f, 0.6f, 0.7f));
    defaultLayerProperties.getStyle().setFillColor(new Color(0.2f, 0.2f, 0.2f, 0.7f));
    defaultLayerProperties.setQueryType(LayerProperties.QUERYTYPE_UNDEFINED);

    return defaultLayerProperties;
  }

  @Override
  protected FeatureFactory createFeatureFactory() throws Exception
  {
    return new PostgisFeatureFactory(this.getLayerProperties(), this.getConnectionInfo(), null, this);
  }

  @Override
  public SimpleFeatureServiceSqlStatement getQuery()
  {
    return this.sqlStatement;
  }

  @Override
  public void setQuery(SimpleFeatureServiceSqlStatement sqlStatement)
  {
    this.sqlStatement = sqlStatement;
  }

  @Override
  protected void initConcreteInstance() throws Exception
  {
    //nothing to do here
  }

  @Override
  protected String getFeatureLayerType()
  {
    return POSTGIS_FEATURELAYER_TYPE;
  }

  @Override
  public Icon getLayerIcon(int type)
  {
    return layerIcons.get(type);
  }

  public void setConnectionInfo(ConnectionInfo connectionInfo)
  {
    this.connectionInfo = connectionInfo;
  }

  public ConnectionInfo getConnectionInfo()
  {
    return this.connectionInfo;
  }

  @Override
  public SimplePostgisFeatureService clone()
  {
    logger.debug("cloning SimplePostgisFeatureService " + this.getName());//NOI18N
    return new SimplePostgisFeatureService(this);
  }
}

