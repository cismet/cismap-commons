/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import java.util.HashMap;
import org.apache.log4j.Logger;
import org.deegree2.model.feature.Feature;
import org.deegree2.model.feature.schema.FeatureType;
import org.deegree2.model.feature.schema.GeometryPropertyType;
import org.deegree2.model.spatialschema.Geometry;

/**
 * 
 * @author haffkeatcismet
 */
public class GeometryHeuristics {
    private static final Logger log = Logger.getLogger("de.cismet.cismap.commons.featureservice.GeometryHeuristics");//NOI18N
    private static final String POINT = "Point";//NOI18N
    private static final String LINESTRING = "LineString";//NOI18N
    private static final String POLYGON = "Polygon";//NOI18N
    private static final String MULTIPOLYGON = "MultiPolygon";//NOI18N
    private static final HashMap<String, Integer> geomOrder = new HashMap<String, Integer>();
    static {
        geomOrder.put(POINT, 1);
        geomOrder.put(LINESTRING, 2);
        geomOrder.put(POLYGON, 3);
        geomOrder.put(MULTIPOLYGON, 4);
    }

    /**
     * Returns the name of the "best" geometry found to display. Compares all GeometryPropertyTypes
     * and returns the name of the property with the highest complexity.
     * @param f Feature which geometry will be determined
     * @return Geometryproperty-name as String
     */
    public static String findBestGeometryName(Feature f) {
        GeometryPropertyType best = null;
        if (f != null) {
            FeatureType type = f.getFeatureType();
            GeometryPropertyType[] geoProp = type.getGeometryProperties();
            Geometry[] geo = f.getGeometryPropertyValues();
            if (geoProp.length == 1) {
                best = geoProp[0];
            } else {
                for (int i = 0; i < geoProp.length; i++) {
                    if (best == null) {
                        if (!isBoundingBox(geo[i])) {
                            best = geoProp[i];
                        }
                    } else {
                        int bestValue = geomOrder.get(best.getTypeName().getLocalName());
                        int geomValue = geomOrder.get(geoProp[i].getTypeName().getLocalName());
                        if (geomValue > bestValue && !isBoundingBox(geo[i])) {
                            best = geoProp[i];
                        }
                    }
                }
            }
        }
        return best.getName().getAsString();
    }

    /**
     * Returns the index of the "best" geometry found to display inside the Geometry-array
     * of the given Feature. Compares all GeometryPropertyTypes and returns the name of
     * the property with the highest complexity.
     * @param geometries array of GeometryPropertyTypes
     * @return index of the Geometry in the Geometry-array
     */
    public static int findBestGeometryIndex(Feature f) {
        int bestIndex = 0;
        GeometryPropertyType best = null;
        if (f != null) {
            FeatureType type = f.getFeatureType();
            GeometryPropertyType[] geoProp = type.getGeometryProperties();
            Geometry[] geo = f.getGeometryPropertyValues();
            if (geoProp.length == 1) {
                bestIndex = 0;
            } else {
                for (int i = 0; i < geoProp.length; i++) {
                    if (best == null) {
                        if (!isBoundingBox(geo[i])) {
                            best = geoProp[i];
                            bestIndex = i;
                        }
                    } else {
                        int bestValue = geomOrder.get(best.getTypeName().getLocalName());
                        int geomValue = geomOrder.get(geoProp[i].getTypeName().getLocalName());
                        if (geomValue > bestValue && !isBoundingBox(geo[i])) {
                            best = geoProp[i];
                            bestIndex = i;
                        }
                    }
                }
            }
        }
        return bestIndex;
    }

//    /**
//     * The heuristic-method. Compares all GeometryPropertyTypes and returns the name of
//     * the property with the highest complexity.
//     * @param geometries array of GeometryPropertyTypes
//     * @return Propertyname as String
//     */
//    public static Geometry findBestGeometry(Feature f) {
//        Geometry bestGeom = null;
//        GeometryPropertyType best = null;
//        if (f != null) {
//            FeatureType type = f.getFeatureType();
//            GeometryPropertyType[] geoProp = type.getGeometryProperties();
//            Geometry[] geo = f.getGeometryPropertyValues();
//            if (geoProp.length == 1) {
//                best = geoProp[0];
//                bestGeom = geo[0];
//            } else {
//                for (int i = 0; i < geoProp.length; i++) {
//                    if (best == null) {
//                        if (!isBoundingBox(geo[i])) {
//                            best = geoProp[i];
//                            bestGeom = geo[i];
//                        }
//                    } else {
//                        int bestValue = geomOrder.get(best.getTypeName().getLocalName());
//                        int geomValue = geomOrder.get(geoProp[i].getTypeName().getLocalName());
//                        if (geomValue > bestValue && !isBoundingBox(geo[i])) {
//                            best = geoProp[i];
//                            bestGeom = geo[i];
//                        }
//                    }
//                }
//            }
//        }
//        return bestGeom;
//    }

    /**
     * Checks if the given Geometry-object is a BoundingBox.
     * @param g Geometry that will be checked   
     * @return true if the Geometry is a BoundingBox, else false
     */
    private static boolean isBoundingBox(Geometry g) {
        return g.isEmpty();
    }
}
