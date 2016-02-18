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

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import org.openide.util.NbBundle;

import java.math.BigDecimal;

import java.sql.Timestamp;

import java.util.Date;

import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;

/**
 * Some useful method for features.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FeatureTools {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FeatureTools.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * Determines the java class that can represent the type of the given feature attribute.
     *
     * @param   attr  DOCUMENT ME!
     *
     * @return  The java class that can represent the type of the given feature attribute
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
        } else if (attr.getType().equals(String.valueOf(Types.NUMERIC))) {
            return BigDecimal.class;
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
     * Converts the given java data type to its corresponding feature data type.
     *
     * @param   cl  a java class
     *
     * @return  the service data type
     */
    public static String getType(final Class cl) {
        if (Geometry.class.isAssignableFrom(cl)) {
            return String.valueOf(Types.GEOMETRY);
        } else if (cl.getName().endsWith("String")) {
            return String.valueOf(Types.VARCHAR);
        } else if (cl.getName().endsWith("Integer") || cl.getName().equals("int")) {
            return String.valueOf(Types.INTEGER);
        } else if (cl.getName().endsWith("Long") || cl.getName().equals("long")) {
            return String.valueOf(Types.BIGINT);
        } else if (cl.getName().endsWith("Double") || cl.getName().equals("double") || cl.getName().endsWith("Float")
                    || cl.getName().equals("float")) {
            return String.valueOf(Types.DOUBLE);
        } else if (cl.getName().endsWith("Date")) {
            return String.valueOf(Types.DATE);
        } else if (cl.getName().endsWith("Timestamp")) {
            return String.valueOf(Types.TIMESTAMP);
        } else if (cl.getName().endsWith("Boolean") || cl.getName().equals("boolean")) {
            return String.valueOf(Types.BOOLEAN);
        } else if (cl.getName().endsWith("BigDecimal")) {
            return String.valueOf(Types.NUMERIC);
        } else {
            return String.valueOf(Types.VARCHAR);
        }
    }

    /**
     * Determines the H2 data type that can represent the type of the given feature attribute.
     *
     * @param   attr  DOCUMENT ME!
     *
     * @return  The H2 data type that can represent the type of the given feature attribute
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
        } else if (attr.getType().equals(String.valueOf(Types.NUMERIC))) {
            return "NUMERIC";
        } else if (attr.getType().equals(String.valueOf(Types.BOOLEAN))
                    || attr.getType().equals("xsd:boolean")) {
            return "Boolean";
        } else if (attr.getType().equalsIgnoreCase("geometry")) {
            return "Geometry";
        } else {
            return attr.getType();
        }
    }

    /**
     * Converts the given object to the given class.
     *
     * @param   object  the object to convert
     * @param   cl      the class, the object should be converted to
     *
     * @return  the converted object
     *
     * @throws  Exception  If the conversion is not possible
     */
    public static Object convertObjectToClass(final Object object, final Class cl) throws Exception {
        final String objectAsString = String.valueOf(object);
        if (object == null) {
            return object;
        }

        try {
            if (cl.equals(String.class)) {
                return objectAsString;
            } else if (cl.equals(Double.class)) {
                return Double.parseDouble(objectAsString);
            } else if (cl.equals(Float.class)) {
                return Float.parseFloat(objectAsString);
            } else if (cl.equals(Integer.class)) {
                try {
                    return Integer.parseInt(objectAsString);
                } catch (NumberFormatException e) {
                    final Double d = Double.parseDouble(objectAsString);

                    return d.intValue();
                }
            } else if (cl.equals(Boolean.class)) {
                return Boolean.parseBoolean(objectAsString);
            } else if (cl.equals(Long.class)) {
                try {
                    return Long.parseLong(objectAsString);
                } catch (NumberFormatException e) {
                    final Double d = Double.parseDouble(objectAsString);

                    return d.longValue();
                }
            } else if (cl.equals(Date.class)) {
                return Timestamp.valueOf(objectAsString);
            } else {
                return object;
            }
        } catch (Exception e) {
            LOG.error("Wrong data type: " + objectAsString + " expected type: " + cl.toString(), e);
            throw new Exception(NbBundle.getMessage(
                    FeatureTools.class,
                    "FeatureTools.convertObjectToClass.message",
                    objectAsString,
                    cl.getName()));
        }
    }
}
