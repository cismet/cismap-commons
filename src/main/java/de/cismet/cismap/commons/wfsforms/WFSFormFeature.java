/*
 * WFSFormFeature.java
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
 * Created on 27. Juli 2006, 14:13
 *
 */

package de.cismet.cismap.commons.wfsforms;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.deegree2.datatypes.QualifiedName;
import org.deegree2.model.feature.Feature;
import org.deegree2.model.feature.FeatureProperty;
import org.deegree2.model.spatialschema.GeometryException;
import org.deegree2.model.spatialschema.JTSAdapter;



/**
 *
 * @author thorsten.hell@cismet.de
 */
public class WFSFormFeature {
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private Feature feature;
    private WFSFormQuery query;
    /** Creates a new instance of WFSFormFeature */
    public WFSFormFeature(Feature feature, WFSFormQuery query) {
        
        this.feature=feature;
        this.query=query;
        
    }
    
    public String getIdentifier() {
        try {
//return feature.getAttribute(query.getIdProperty()).toString();
            //return feature.getProperty(query.getIdProperty()).toString();
            //return feature.getProperties( new QualifiedName(query.getIdProperty().toString()) );
            //return
            return feature.getProperties(new QualifiedName(query.getPropertyPrefix().toString(),query.getIdProperty().toString(),new URI(query.getPropertyNamespace().toString())))[0].getValue().toString();
        } catch (Exception e) {
            log.error("Fehler in toIdentifier()",e);
            return null;
        }
    }
    
    
    public FeatureProperty[] getRawFeatureArray(String prefix,String identifier,String namespace) throws Exception{
        return feature.getProperties(new QualifiedName(prefix,identifier,new URI(namespace)));
    }
    
    
    
    public String toString() {
        try {//return feature.getAttribute(query.getDisplayTextProperty()).toString();
            //return feature.getProperty(query.getDisplayTextProperty()).toString();
            if (query.getPropertyPrefix()!=null) {
                String s= feature.getProperties(new QualifiedName(query.getPropertyPrefix().toString(),query.getDisplayTextProperty().toString(),new URI(query.getPropertyNamespace().toString())))[0].getValue().toString();
                return s;
            } else {
                String s=feature.getProperties(new QualifiedName(query.getDisplayTextProperty().toString()))[0].getValue().toString();
                return s;
                
            }
            
//        ByteBuffer bb = ByteBuffer.wrap(ret.getBytes());
//        return Charset.forName("utf-8").decode(bb).toString();
            
            
//        try {
//            return new String (feature.getProperties(new QualifiedName(query.getDisplayTextProperty().toString()))[0].getValue().toString().getBytes(),"ISO-8859-1");
//        }
//        catch (Exception skip) {
//            return "";
//        }
        } catch (Exception e) {
//            try {
//                String ret =feature.getProperties(new QualifiedName("app",query.getDisplayTextProperty().toString(),new URI("http://www.deegree.org/app")))[0].getValue().toString();
//                return ret;
//            } catch (Exception ex) {
            try {
                log.error("Fehler in toString() angefragt wurde: "+new QualifiedName(query.getPropertyPrefix().toString(),query.getDisplayTextProperty().toString(),new URI(query.getPropertyNamespace().toString())).toString(),e);
            } catch (Exception never) {
                log.error("Fehler in toString()",e);
            }
            for (FeatureProperty fp:feature.getProperties()) {
                log.fatal(fp.getName().getPrefix()+"."+fp.getName().getLocalName()+"."+fp.getName().getNamespace()+"->"+fp.getValue());
            }
            return null;
//            }
            
        }
    }
    
    public Feature getFeature() {
        return feature;
    }
    
    public void setFeature(Feature feature) {
        this.feature = feature;
    }
    
    public WFSFormQuery getQuery() {
        return query;
    }
    
    
    public Geometry getJTSGeometry(){
        try {
            return JTSAdapter.export(feature.getDefaultGeometryPropertyValue());
        } catch (GeometryException ex) {
            log.error("Fehler in getJTSGeometry()",ex);
        }
        return null;
    }
    
    
    public Point getPosition() {
        try {
            FeatureProperty[] fp=feature.getProperties(new QualifiedName(query.getPropertyPrefix().toString(),query.getPositionProperty().toString(),new URI(query.getPropertyNamespace().toString())));
            Point p=(Point)(JTSAdapter.export((org.deegree2.model.spatialschema.Geometry)(fp[0].getValue())));
            log.debug("POSITION="+p);
            return p;
        } catch (Exception ex) {
            log.debug("Feature hat keine POSITION. Berechne den Mittelpunkt aus getJTSGeometry() ",ex) ;
            Point p=getJTSGeometry().getCentroid();
            return p;
        }
    }
    public void setQuery(WFSFormQuery query) {
        this.query = query;
    }
    
    
    
    
    
}
