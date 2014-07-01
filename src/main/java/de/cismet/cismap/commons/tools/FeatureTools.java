/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.tools;

import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import java.util.Date;
import org.deegree.datatypes.Types;

/**
 *
 * @author therter
 */
public class FeatureTools {
    
    public static Class<?> getClass(FeatureServiceAttribute attr) {
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
}
