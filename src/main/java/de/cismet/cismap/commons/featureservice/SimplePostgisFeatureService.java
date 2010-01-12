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
  public static String POSTGIS_FEATURELAYER_TYPE = "simplePostgisFeatureService";
  private SimpleFeatureServiceSqlStatement sqlStatement;
  private ConnectionInfo connectionInfo;

  public static final HashMap<Integer, Icon> layerIcons = new HashMap<Integer, Icon>();
  static
  {
    layerIcons.put(LAYER_ENABLED_VISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerPostgis.png")));
    layerIcons.put(LAYER_ENABLED_INVISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/layerPostgisInvisible.png")));
    layerIcons.put(LAYER_DISABLED_VISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerPostgis.png")));
    layerIcons.put(LAYER_DISABLED_INVISIBLE, new ImageIcon(AbstractFeatureService.class.getResource("/de/cismet/cismap/commons/gui/layerwidget/res/disabled/layerPostgisInvisible.png")));
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

    if(element.getChild("dbConnectionInfo") != null)
    {
      ConnectionInfo newConnectionInfo = new ConnectionInfo(element.getChild("dbConnectionInfo"));
      this.setConnectionInfo(newConnectionInfo);
      logger.debug("SimplePostgisFeatureService initialised with connection: \n" + this.getConnectionInfo().getUrl() + ", " + this.getConnectionInfo().getDriver() + ", " + this.getConnectionInfo().getUser());
    }
    else
    {
      logger.error("missing element 'dbConnectionInfo' in xml configuration");
    }

    // TODO: SimpleFeatureServiceSqlStatement should implement ConvertableToXML
    this.sqlStatement = new SimpleFeatureServiceSqlStatement(element.getChild("statement").getTextTrim());
    this.sqlStatement.setAllFields(element.getChild("allFields").getTextTrim());
    this.sqlStatement.setOrderBy(element.getChild("orderBy").getTextTrim());
  }

  @Override
  public Element toElement()
  {
    Element e = super.toElement();

    if (this.sqlStatement != null)
    {
      Element stmnt = new Element("statement");
      stmnt.addContent(new CDATA(sqlStatement.getSqlTemplate()));
      e.addContent(stmnt);
      Element allFields = new Element("allFields");
      allFields.addContent(new CDATA(sqlStatement.getAllFields()));
      e.addContent(allFields);
      Element orderBy = new Element("orderBy");
      orderBy.addContent(new CDATA(sqlStatement.getOrderBy()));
      e.addContent(orderBy);
    } else
    {
      logger.warn("sql statement is null and cannot be saved");
    }

    if (this.connectionInfo != null)
    {
      // TODO: ConnectionInfo should implement ConvertableToXML
      Element connectionElement = new Element("dbConnectionInfo");
      connectionElement.addContent(new Element("driverClass").addContent(this.getConnectionInfo().getDriver()));
      connectionElement.addContent(new Element("dbUrl").addContent(this.getConnectionInfo().getUrl()));
      connectionElement.addContent(new Element("user").addContent(this.getConnectionInfo().getUser()));
      connectionElement.addContent(new Element("pass").addContent(this.getConnectionInfo().getPass()));
      e.addContent(connectionElement);
    } else
    {
      logger.warn("connection info is null and cannot be saved");
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
    logger.debug("cloning SimplePostgisFeatureService " + this.getName());
    return new SimplePostgisFeatureService(this);
  }
}

