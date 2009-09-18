/*
 * SimplePostgisFeatureService.java
 * Copyright (C) 2005 by:
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

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.ConvertableToXML;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.ServiceLayer;
import de.cismet.cismap.commons.jtsgeometryfactories.PostGisGeometryFactory;
import de.cismet.cismap.commons.rasterservice.FeatureMapService;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.AbstractRetrievalService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.tools.CismetThreadPool;
import de.cismet.tools.ConnectionInfo;
import edu.umd.cs.piccolo.PNode;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Element;
import org.postgis.PGgeometry;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class SimplePostgisFeatureService extends AbstractRetrievalService implements MapService,ServiceLayer,FeatureMapService,RetrievalServiceLayer,ConvertableToXML{
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public static final String QUERY_CANCELED="57014";
    boolean enabled=true;
    BoundingBox bb=null;
    Connection connection=null;
    private SimpleFeatureServiceSqlStatement sqlStatement;
    private FeatureRetrieval fr;
    Vector retrievedResults=new Vector();
    private int layerPosition;
    private String name;
    private float translucency=0.2f;
    private Color lineColor=new Color(0.6f,0.6f,0.6f,0.7f);
    private Color fillingColor=new Color(0.2f,0.2f,0.2f,0.7f);
    private ConnectionInfo ci=null;
    private PNode pNode;
    private boolean visible=true;
    private SimpleFeatureServiceSqlStatement sfsStatement;
    
    /** Creates a new instance of SimplePostgisFeatureService */
    public SimplePostgisFeatureService() {
    }
    
    public SimplePostgisFeatureService(Element object) {
        //layer ="1" enabled="true" translucency="0.2" lineColor="0,0,0" fillingColor="100,100,100"
        ci=new ConnectionInfo(object.getChild("dbConnectionInfo"));
        setConnection(ci);
        Attribute layerPositionAttr=object.getAttribute("layerPosition");
        if (layerPositionAttr!=null) {
            try {
                layerPosition=layerPositionAttr.getIntValue();
            } catch (Exception e) {
                log.warn("Fehler beim Auslesen der Preferences(LayerPosition)", e);
            }
        }
        Attribute enabledAttr=object.getAttribute("enabled");
        if (enabledAttr!=null){
            try {
                enabled=enabledAttr.getBooleanValue();
            } catch (Exception e) {
                log.warn("Fehler beim Auslesen der Preferences(enabled)", e);
            }
        }
        Attribute nameAttr=object.getAttribute("name");
        if (nameAttr!=null){
            try {
                name=nameAttr.getValue();
            } catch (Exception e) {
                log.warn("Fehler beim Auslesen der Preferences(name)", e);
            }
        }
        Attribute translucencyAttr=object.getAttribute("translucency");
        if (translucencyAttr!=null){
            try {
                translucency=translucencyAttr.getFloatValue();
            } catch (Exception e) {
                log.warn("Fehler beim Auslesen der Preferences(translucency)", e);
            }
        }
        Attribute lineColorAttr=object.getAttribute("lineColor");
        if (lineColorAttr!=null){
            try {
                String lineColorString=lineColorAttr.getValue();
                String[] rgb=lineColorString.split(",");
                Color c=new Color(new Integer(rgb[0]).intValue(),
                        new Integer(rgb[1]).intValue(),
                        new Integer(rgb[2]).intValue(),
                        (int)(255*translucency));
                lineColor=c;
            } catch (Exception e) {
                log.warn("Fehler beim Auslesen der Preferences(lineColor)", e);
            }
        }
        Attribute fillingColorAttr=object.getAttribute("fillingColor");
        if (fillingColorAttr!=null){
            try {
                String fillingColorString=fillingColorAttr.getValue();
                String[] rgb=fillingColorString.split(",");
                Color c=new Color(new Integer(rgb[0]).intValue(),
                        new Integer(rgb[1]).intValue(),
                        new Integer(rgb[2]).intValue(),
                        (int)(255*translucency));
                fillingColor=c;
            } catch (Exception e) {
                log.warn("Fehler beim Auslesen der Preferences(fillingColor)", e);
            }
        }
        
        sfsStatement=
                new SimpleFeatureServiceSqlStatement(object.getChild("statement").getTextTrim());
        sfsStatement.setAllFields(object.getChild("allFields").getTextTrim());
        sfsStatement.setOrderBy(object.getChild("orderBy").getTextTrim());
        setSqlStatement(sfsStatement);
        
    }
    
    public Element getElement() {
        Element e=new Element("simplePostgisFeatureService");
        e.setAttribute("name",name);
        e.setAttribute("skip",new Boolean(false).toString());
        e.setAttribute("layerPosition",new Integer(layerPosition).toString());
        e.setAttribute("visible",new Boolean(visible).toString());
        e.setAttribute("enabled",new Boolean(enabled).toString());
        e.setAttribute("translucency",new Float(translucency).toString());
        e.setAttribute("lineColor",lineColor.getRed()+","+lineColor.getGreen()+","+lineColor.getBlue());
        e.setAttribute("fillingColor",fillingColor.getRed()+","+fillingColor.getGreen()+","+fillingColor.getBlue());
        e.addContent(ci.getElement());
        Element stmnt=new Element("statement");
        stmnt.addContent(new CDATA(sfsStatement.getSqlTemplate()));
        e.addContent(stmnt);
        Element allFields=new Element("allFields");
        allFields.addContent(new CDATA(sfsStatement.getAllFields()));
        e.addContent(allFields);
        Element orderBy=new Element("orderBy");
        orderBy.addContent(new CDATA(sfsStatement.getOrderBy()));
        e.addContent(orderBy);
        return e;
    }
    
    
    public void setConnection(ConnectionInfo connectionInfo) {
        try {
            Class.forName( connectionInfo.getDriver() );
            connection=DriverManager.getConnection(connectionInfo.getUrl(),connectionInfo.getUser(), connectionInfo.getPass());
            ((org.postgresql.PGConnection)connection).addDataType("geometry","org.postgis.PGgeometry");
            ((org.postgresql.PGConnection)connection).addDataType("box3d","org.postgis.PGbox3d");
        } catch (Exception e) {
            log.fatal("Fehler beim Herstellen der DB-Verbindung ("+connectionInfo+")", e);
        }
    }
    
    
    public synchronized void setBoundingBox(de.cismet.cismap.commons.BoundingBox bb) {
        this.bb=bb;
    }
    
    
    public void setEnabled(boolean enabled) {
        this.enabled=enabled;
    }
            
    
    public synchronized void retrieve(boolean forced) {
        //Hier wird eine neue Connection angelegt, weil die alte noch besch�ftigt ist
        // setConnection(ci);
        
        if (fr!=null&&fr.isAlive()) {
            fr.interrupt();
            //fr.stop(); //TODO: UGLY WINNING
            fireRetrievalAborted(new RetrievalEvent());
        }
        fr=new FeatureRetrieval();
        fr.setPriority(Thread.NORM_PRIORITY);
        CismetThreadPool.execute(fr);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public boolean canBeDisabled() {
        return true;
        
    }
    
    public DefaultFeatureServiceFeature getNewFeatureServiceFeature() {
        //log.debug("getNewFeatureServiceFeature");
        return new DefaultFeatureServiceFeature();
    }
    
    
    private class FeatureRetrieval extends Thread {
    
    public void run() {
            try {
                Statement stmnt=null;
                //connection.close();
                //setConnection(ci);
                long ctm=System.currentTimeMillis();
                RetrievalEvent r=new RetrievalEvent();
                r.setRequestIdentifier(ctm);
                fireRetrievalStarted(r);
                sqlStatement.setX1(bb.getX1());
                sqlStatement.setX2(bb.getX2());
                sqlStatement.setY1(bb.getY1());
                sqlStatement.setY2(bb.getY2());
                retrievedResults=new Vector();
                if (stmnt!=null) {
                    try {
                        //stmnt.cancel();
                        stmnt.cancel();
                        stmnt.close();
                        stmnt=null;
                    } catch (Exception ex) {
                        log.debug("stmnt war  noch nicht belegt",ex);
                    }
                }
                stmnt=connection.createStatement();                             
                log.debug("FeatureRetrievalSQLStatement (count): "+sqlStatement.getCountFeaturesStatement());
                
                ResultSet rsCount=stmnt.executeQuery(sqlStatement.getCountFeaturesStatement());                
                if (isInterrupted()) {
                    fireLoadingAborted();
                    return;
                }
                log.debug("Count(*) ist fertig");
                int count=-1;
                
                while (rsCount.next()&&!(isInterrupted())) {
                    count=rsCount.getInt(1);
                    log.info(count+" Polygone im gew�hlten Ausschnitt");
                    if (count>1000) {
                        RetrievalEvent re=new RetrievalEvent();
                        re.setHasErrors(true);
                        log.warn("Zu gro�");
                        re.setRetrievedObject("Mehr als 1000 Features k�nnen nicht dargestellt werden.");
                        re.setRequestIdentifier(ctm);
                        fireRetrievalError(re);
                        return;
                    }
                }
                if (!(isInterrupted())) {
                    log.debug("FeatureRetrievalSQLStatement: "+sqlStatement.getFeaturesStatement());
                    ResultSet rs=stmnt.executeQuery(sqlStatement.getFeaturesStatement());
                    int progress=0;
                    while (rs.next()&&!(isInterrupted())) {
                        String name="";
                        try {
                            name=rs.getObject("ObjectName").toString();
                        }
                        catch (Exception e) {
                            log.warn("Fehler in SimplePostgisFeatureService. Setze ObjectName=''",e);
                        }
                        String type="";
                        try {
                            type=rs.getObject("Type").toString();
                        }
                        catch (Exception e) {
                            log.warn("Fehler in SimplePostgisFeatureService. Setze Type=''",e);
                        }
                        String groupingKey="";
                        try {
                        groupingKey=rs.getObject("GroupingKey").toString();
                        }
                        catch (Exception e) {
                            log.warn("Fehler in SimplePostgisFeatureService. Setze groupingKey=''",e);
                        }
                        int id=-1;
                        try {
                            id=rs.getInt("Id");
                        }
                        catch (Exception skip){}
                        
                        PGgeometry postgresGeom=(PGgeometry)rs.getObject("Geom");
                        org.postgis.Geometry postgisGeom=postgresGeom.getGeometry();
                        DefaultFeatureServiceFeature sf=getNewFeatureServiceFeature();
                        sf.setId(id);
                        sf.setGeometry((PostGisGeometryFactory.createJtsGeometry(postgisGeom)));
                        sf.setFeatureService(SimplePostgisFeatureService.this);
                        sf.setFeatureType(type);
                        sf.setGroupingKey(groupingKey);
                        sf.setObjectName(name);
                        sf.setFillingPaint(fillingColor);
                        sf.setLinePaint(lineColor);
                        
                        retrievedResults.add(sf);
                        progress++;
                        RetrievalEvent re=new RetrievalEvent();
                        re.setIsComplete(false);
                        double percentage=((double)progress)/((double)count)*100.0;
                        re.setPercentageDone(percentage);
                        re.setRequestIdentifier(ctm);
                        fireRetrievalProgress(re);
                    }
                }
                if (!(isInterrupted())) {
                    RetrievalEvent re=new RetrievalEvent();
                    re.setIsComplete(true);
                    re.setHasErrors(false);
                    re.setRetrievedObject(retrievedResults);
                    re.setRequestIdentifier(ctm);
                    fireRetrievalComplete(re);
                } else {
                    stmnt.close();
                    stmnt=null;
                    RetrievalEvent re=new RetrievalEvent();
                    re.setIsComplete(false);
                    re.setRequestIdentifier(ctm);
                    fireRetrievalAborted(re);
                    
                }
                
                
            } catch (Exception e) {
                log.error("Exception in Featureretrieval",e);
                if (e instanceof SQLException) {
                    SQLException se=(SQLException)e;
                    try {
                    if (se.getSQLState().equals(QUERY_CANCELED)){
                        //Abbruch
                        return;
                    }
                    }
                    catch (Exception e2) {
                        log.error(e2,e2);
                    }
                }
                log.error("Fehler beim Laden der Features.("+sqlStatement.getFeaturesStatement()+")",e);
                
                RetrievalEvent re=new RetrievalEvent();
                re.setHasErrors(true);
                String message;
                if (!(e.getMessage()==null||e.getMessage().equalsIgnoreCase("null"))) {
                    try {
                        message=e.getCause().getMessage();
                    } catch (Throwable t) {
                        message="Nicht zuordnungsf\u00E4higer Fehler (e.getCause()==null";
                    }
                } else {
                    message=e.getMessage();
                }
                re.setRetrievedObject(message);
                
                fireRetrievalError(re);
            }
        }

//        public void interrupt() {
//            try {
////                stmnt.cancel();
//            } catch (SQLException ex) {
//                log.warn("cancel beim Statement ging schief",ex);
//            }
//            super.interrupt();
//        }
        
    }
    
    public SimpleFeatureServiceSqlStatement getSqlStatement() {
        return sqlStatement;
    }
    
    public void setSqlStatement(SimpleFeatureServiceSqlStatement sqlStatement) {
        this.sqlStatement = sqlStatement;
    }
    
    public int getLayerPosition() {
        return layerPosition;
    }
    
    public String getName() {
        return name;
    }
    
    public void setLayerPosition(int layerPosition) {
        this.layerPosition = layerPosition;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public float getTranslucency() {
        return translucency;
    }
    
    public void setTranslucency(float translucency) {
        this.translucency = translucency;
    }
    
    private void fireLoadingAborted(){
//        RetrievalEvent e=new RetrievalEvent();
//        fireRetrievalAborted(e);
    }
    
    public Object clone() {
        SimplePostgisFeatureService s =new SimplePostgisFeatureService();
        s.bb=bb;
        s.ci=ci;
        s.connection=connection;
        s.enabled=enabled;
        s.fillingColor=fillingColor;
        s.fr=fr;
        s.layerPosition=layerPosition;
        s.lineColor=lineColor;
        s.listeners=new Vector(listeners);
        s.name=name;
        s.retrievedResults=new Vector(retrievedResults);
        s.sqlStatement=sqlStatement;
        //s.stmnt=stmnt;
        s.translucency=translucency;
        return s;
    }
    private Vector<PropertyChangeListener> propertyChangeSupportListener=new Vector<PropertyChangeListener>();
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupportListener.remove(l);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupportListener.add(l);
    }

    public PNode getPNode() {
        return pNode;
    }

    public void setPNode(PNode pNode) {
        this.pNode = pNode;
    }

    public boolean  isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSize(int height, int width) {
       
    }
    
    public String toString() {
        return getName();
    }
    
    
}


