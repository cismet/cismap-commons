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
package de.cismet.cismap.commons.tools;

import org.deegree.datatypes.Types;

import java.util.Date;

import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FeatureTools {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   attr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Class<?> getClass(final FeatureServiceAttribute attr) {
        if (attr.isGeometry()) {
            return String.class;
        } else if (attr.getType().equals(String.valueOf(Types.CHAR))
                    || attr.getType().equals(String.valueOf(Types.VARCHAR))
                    || attr.getType().equals(String.valueOf(Types.LONGVARCHAR))) {
            return String.class;
        } else if (attr.getType().equals(String.valueOf(Types.INTEGER))
                    || attr.getType().equals(String.valueOf(Types.SMALLINT))
                    || attr.getType().equals(String.valueOf(Types.TINYINT))
                    || attr.getType().equals("xsd:integer")) {
            return Integer.class;
        } else if (attr.getType().equals(String.valueOf(Types.BIGINT))
                    || attr.getType().equals("xsd:long")) {
            return Long.class;
        } else if (attr.getType().equals(String.valueOf(Types.DOUBLE))
                    || attr.getType().equals(String.valueOf(Types.FLOAT))
                    || attr.getType().equals(String.valueOf(Types.DECIMAL))
                    || attr.getType().equals("xsd:float")
                    || attr.getType().equals("xsd:decimal")
                    || attr.getType().equals("xsd:double")) {
            return Double.class;
        } else if (attr.getType().equals(String.valueOf(Types.DATE))
                    || attr.getType().equals(String.valueOf(Types.TIME))
                    || attr.getType().equals(String.valueOf(Types.TIMESTAMP))) {
            return Date.class;
        } else if (attr.getType().equals(String.valueOf(Types.BOOLEAN))
                    || attr.getType().equals("xsd:boolean")) {
            return Boolean.class;
        } else {
            return String.class;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getH2DataType(final FeatureServiceAttribute attr) {
        if (attr.isGeometry()) {
            return "Geometry";
        } else if (attr.getType().equals(String.valueOf(Types.CHAR))
                    || attr.getType().equals(String.valueOf(Types.VARCHAR))
                    || attr.getType().equals(String.valueOf(Types.LONGVARCHAR))) {
            return "VARCHAR";
        } else if (attr.getType().equals(String.valueOf(Types.INTEGER))
                    || attr.getType().equals(String.valueOf(Types.SMALLINT))
                    || attr.getType().equals(String.valueOf(Types.TINYINT))
                    || attr.getType().equals("xsd:integer")) {
            return "Integer";
        } else if (attr.getType().equals(String.valueOf(Types.BIGINT))
                    || attr.getType().equals("xsd:long")) {
            return "BIGINT";
        } else if (attr.getType().equals(String.valueOf(Types.DOUBLE))
                    || attr.getType().equals(String.valueOf(Types.FLOAT))
                    || attr.getType().equals(String.valueOf(Types.DECIMAL))
                    || attr.getType().equals("xsd:float")
                    || attr.getType().equals("xsd:decimal")
                    || attr.getType().equals("xsd:double")) {
            return "Double";
        } else if (attr.getType().equals(String.valueOf(Types.DATE))
                    || attr.getType().equals(String.valueOf(Types.TIME))
                    || attr.getType().equals(String.valueOf(Types.TIMESTAMP))) {
            return "Timestamp";
        } else if (attr.getType().equals(String.valueOf(Types.BOOLEAN))
                    || attr.getType().equals("xsd:boolean")) {
            return "Boolean";
        } else if (attr.getType().equalsIgnoreCase("geometry")) {
            return "Geometry";
        } else {
            return attr.getType();
        }
    }
}
