/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.*;

import java.util.Arrays;
import java.util.Collection;

import de.cismet.tools.collections.TypeSafeCollections;

/**
 * DOCUMENT ME!
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public final class FeatureGroups {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FeatureGroups.class);
    public static final boolean SHOW_GROUPING_ENABLED = false;
    public static final boolean SHOW_GROUPS_AS_ENVELOPES = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FeatureGroups object.
     *
     * @throws  AssertionError  DOCUMENT ME!
     */
    private FeatureGroups() {
        throw new AssertionError();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   fg  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean hasSubFeatures(final FeatureGroup fg) {
        final Collection<? extends Feature> subFeatures = fg.getFeatures();
        if ((subFeatures == null) || (subFeatures.size() == 0)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   featureGroup   DOCUMENT ME!
     * @param   includeGroups  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Collection<? extends Feature> expand(final FeatureGroup featureGroup, final boolean includeGroups) {
        final Collection<Feature> result = TypeSafeCollections.newArrayList();
        final Collection<? extends Feature> subFeatures = featureGroup.getFeatures();
        if (subFeatures != null) {
            for (final Feature f : featureGroup) {
                if (f instanceof FeatureGroup) {
                    result.addAll(expand((FeatureGroup)f, includeGroups));
                } else {
                    result.add(f);
                }
            }
        }
        if (includeGroups || (result.size() == 0)) {
            result.add(featureGroup);
        }
        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fg  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Collection<? extends Feature> expandAll(final FeatureGroup fg) {
        return expand(fg, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fg  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Collection<? extends Feature> expandToLeafs(final FeatureGroup fg) {
        return expand(fg, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   group  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry getEnclosingGeometry(final FeatureGroup group) {
        return getEnclosingGeometry(group.getFeatures());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   featureCollection  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry getEnclosingGeometry(final Collection<? extends Feature> featureCollection) {
        boolean hasLinestringOrPoints = false;
        Geometry union = null;
        final Geometry[] array = new Geometry[featureCollection.size()];
        int i = 0;

        try {
            for (final Feature f : featureCollection) {
                final Geometry newGeom = f.getGeometry();

                if (!hasLinestringOrPoints && !((newGeom instanceof Polygon) || (newGeom instanceof MultiPolygon))) {
                    hasLinestringOrPoints = true;
                }

                if (newGeom != null) {
                    if (FeatureGroups.SHOW_GROUPS_AS_ENVELOPES) {
                        array[i++] = newGeom.getEnvelope();
                    } else {
                        array[i++] = newGeom;
                    }
                }
            }
            int srid = 0;

            if (array.length > 0) {
                srid = array[0].getSRID();
            }
            final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);

            if (!hasLinestringOrPoints) {
                // The following two lines are more efficient then the union method.
                // See http://www.vividsolutions.com/JTS/bin/JTS%20Developer%20Guide.pdf
                // But buffer(0) handles LineStrings and Points as empty polygons, so it can only be used,
                // if only polygons should be unioned.
                final GeometryCollection collection = factory.createGeometryCollection(array);
                union = collection.buffer(0);
            } else {
                union = factory.buildGeometry(Arrays.asList(array));

                if (union instanceof GeometryCollection) {
                    union = ((GeometryCollection)union).union();
                }
            }

            return union;
        } catch (Exception e) {
            log.error("Error during creation of enclosing geom", e);
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   hierarchyMember  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static FeatureGroup getRootFeature(FeatureGroup hierarchyMember) {
        while (hierarchyMember.getParentFeature() != null) {
            hierarchyMember = hierarchyMember.getParentFeature();
        }
        return hierarchyMember;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   hierarchyMember  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Feature getRootFeature(SubFeature hierarchyMember) {
        while (hierarchyMember.getParentFeature() != null) {
            hierarchyMember = hierarchyMember.getParentFeature();
        }
        return hierarchyMember;
    }
}
