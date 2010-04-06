/*
 * SimpleFeatureServiceSqlStatement.java
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
 * Created on 13. Juli 2005, 13:44
 *
 */

package de.cismet.cismap.commons.featureservice;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class SimpleFeatureServiceSqlStatement {
    
    /** Creates a new instance of SimpleFeatureServiceSqlStatement */
    
    String sqlTemplate;
    String allFieldsToken;
    String x1Token;
    String y1Token; 
    String x2Token; 
    String y2Token;
    
    public final static String ALL_FIELDS_TOKEN="<cismap::AllFields>";//NOI18N
    public final static String X1_TOKEN="<cismap::x1>";//NOI18N
    public final static String Y1_TOKEN="<cismap::y1>";//NOI18N
    public final static String X2_TOKEN="<cismap::x2>";//NOI18N
    public final static String Y2_TOKEN="<cismap::y2>";//NOI18N
    
    private String allFields="";//NOI18N
    private double x1=0;
    private double y1=0;
    private double x2=0;
    private double y2=0;
    private String orderBy="";//NOI18N
    
    public SimpleFeatureServiceSqlStatement(String sqlTemplate,String allFieldsToken,String x1Token, String y1Token, String x2Token, String y2Token) {
        this.sqlTemplate=sqlTemplate;
        this.allFieldsToken=allFieldsToken;
        this.x1Token=x1Token;
        this.y1Token=y1Token;
        this.x2Token=x2Token;
        this.y2Token=y2Token;
    }
    
    public SimpleFeatureServiceSqlStatement(String sqlTemplate) {
        this.sqlTemplate=sqlTemplate;
        this.allFieldsToken=ALL_FIELDS_TOKEN;
        this.x1Token=X1_TOKEN;
        this.y1Token=Y1_TOKEN;
        this.x2Token=X2_TOKEN;
        this.y2Token=Y2_TOKEN;
    }

    public double getX1() {
        return x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getY1() {
        return y1;
    }

    public void setY1(double y1) {
        this.y1 = y1;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public double getY2() {
        return y2;
    }

    public void setY2(double y2) {
        this.y2 = y2;
    }
    
    public String getFeaturesStatement() {
        String ret=sqlTemplate;
        ret=sqlTemplate.replaceAll(allFieldsToken, allFields);
        ret=ret.replaceAll(x1Token, x1+"");//NOI18N
        ret=ret.replaceAll(y1Token, y1+"");//NOI18N
        ret=ret.replaceAll(x2Token, x2+"");//NOI18N
        ret=ret.replaceAll(y2Token, y2+"");//NOI18N
        return ret+" "+orderBy;//NOI18N
    }
    
    public String getCountFeaturesStatement() {
        String ret=sqlTemplate;
        ret=ret.replaceAll(allFieldsToken, "count(*)");//NOI18N
        ret=ret.replaceAll(x1Token, x1+"");//NOI18N
        ret=ret.replaceAll(y1Token, y1+"");//NOI18N
        ret=ret.replaceAll(x2Token, x2+"");//NOI18N
        ret=ret.replaceAll(y2Token, y2+"");//NOI18N
        return ret;
    }

    public String getAllFields() {
        return allFields;
    }

    public void setAllFields(String allFields) {
        this.allFields = allFields;
    }

    public String getSqlTemplate() {
        return sqlTemplate;
    }
    
    
    public static void main (String[] args) {
        SimpleFeatureServiceSqlStatement sqlStatement= new SimpleFeatureServiceSqlStatement("select <cismap::AllFields> from geom, cs_all_attr_mapping where geom.id=attr_object_id and attr_class_id=0 and class_id=11 and geom.geo_field && GeomFromText('BOX3D(<cismap::x1> <cismap::y1>,<cismap::x2> <cismap::y2>)',-1)", //NOI18N
                                                                                        "<cismap::AllFields>", "<cismap::x1>","<cismap::y1>", "<cismap::x2>", "<cismap::y2>");//NOI18N
        sqlStatement.setAllFields("'Kassenzeichen' as Type,object_id as GroupingKey, object_id as ObjectName,geo_field as Geom");//NOI18N
        sqlStatement.setX1(1);
        sqlStatement.setY1(2);
        sqlStatement.setX2(3);
        sqlStatement.setY2(4);
        
        //System.out.println(sqlStatement.getCountFeaturesStatement());
        //System.out.println(sqlStatement.getFeaturesStatement());
        
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
    
}
