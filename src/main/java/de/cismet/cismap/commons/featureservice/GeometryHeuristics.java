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
package de.cismet.cismap.commons.featureservice;

import org.apache.log4j.Logger;

import org.deegree.model.feature.Feature;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GeometryPropertyType;
import org.deegree.model.spatialschema.Geometry;

import java.util.HashMap;

/**
 * DOCUMENT ME!
 *
 * @author   haffkeatcismet
 * @version  $Revision$, $Date$
 */
public class GeometryHeuristics {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger log = Logger.getLogger("de.cismet.cismap.commons.featureservice.GeometryHeuristics"); // NOI18N
    private static final String POINT = "Point";                                                                      // NOI18N
    private static final String MULTIPOINT = "MultiPoint";                                                            // NOI18N
    private static final String LINESTRING = "LineString";                                                            // NOI18N
    private static final String MULTILINESTRING = "MultiLineString";                                                  // NOI18N
    private static final String POLYGON = "Polygon";                                                                  // NOI18N
    private static final String MULTIPOLYGON = "MultiPolygon";                                                        // NOI18N
    private static final HashMap<String, Integer> geomOrder = new HashMap<String, Integer>();

    // TODO: geomOrder vervollstaendigen
    static {
        geomOrder.put(POINT, 1);
        geomOrder.put(MULTIPOINT, 2);
        geomOrder.put(LINESTRING, 3);
        geomOrder.put(MULTILINESTRING, 4);
        geomOrder.put(POLYGON, 5);
        geomOrder.put(MULTIPOLYGON, 6);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the name of the "best" geometry found to display. Compares all GeometryPropertyTypes and returns the name
     * of the property with the highest complexity.
     *
     * @param   f  Feature which geometry will be determined
     *
     * @return  Geometryproperty-name as String
     */
    public static String findBestGeometryName(final Feature f) {
        GeometryPropertyType best = null;
        if (f != null) {
            final FeatureType type = f.getFeatureType();
            final GeometryPropertyType[] geoProp = type.getGeometryProperties();
            final Geometry[] geo = f.getGeometryPropertyValues();
            if (geoProp.length == 1) {
                best = geoProp[0];
            } else {
                for (int i = 0; i < geoProp.length; i++) {
                    if (best == null) {
                        if (!isBoundingBox(geo[i])) {
                            best = geoProp[i];
                        }
                    } else {
                        final int bestValue = geomOrder.get(best.getTypeName().getLocalName());
                        final int geomValue = geomOrder.get(geoProp[i].getTypeName().getLocalName());
                        if ((geomValue > bestValue) && !isBoundingBox(geo[i])) {
                            best = geoProp[i];
                        }
                    }
                }
            }
        }
        return best.getName().getAsString();
    }

    /**
     * Returns the index of the "best" geometry found to display inside the Geometry-array of the given Feature.
     * Compares all GeometryPropertyTypes and returns the name of the property with the highest complexity.
     *
     * @param   f  geometries array of GeometryPropertyTypes
     *
     * @return  index of the Geometry in the Geometry-array
     */
    public static int findBestGeometryIndex(final Feature f) {
        int bestIndex = 0;
        GeometryPropertyType best = null;
        if (f != null) {
            final FeatureType type = f.getFeatureType();
            final GeometryPropertyType[] geoProp = type.getGeometryProperties();
            final Geometry[] geo = f.getGeometryPropertyValues();
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
                        final int bestValue = geomOrder.get(best.getTypeName().getLocalName());
                        final int geomValue = geomOrder.get(geoProp[i].getTypeName().getLocalName());
                        if ((geomValue > bestValue) && !isBoundingBox(geo[i])) {
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
     *
     * @param   g  Geometry that will be checked
     *
     * @return  true if the Geometry is a BoundingBox, else false
     */
    private static boolean isBoundingBox(final Geometry g) {
        return g.isEmpty();
    }
}
