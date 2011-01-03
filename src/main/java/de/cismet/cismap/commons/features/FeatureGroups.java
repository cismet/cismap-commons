/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Geometry;

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
        Geometry g = null;
        for (final Feature f : featureCollection) {
            final Geometry newGeom = f.getGeometry();
            if (newGeom != null) {
                if (g == null) {
                    g = newGeom;
                    if (FeatureGroups.SHOW_GROUPS_AS_ENVELOPES) {
                        g = g.getEnvelope();
                    }
                } else {
                    if (FeatureGroups.SHOW_GROUPS_AS_ENVELOPES) {
                        g = g.union(newGeom.getEnvelope()).getEnvelope();
                    } else {
                        g = g.union(newGeom);
                    }
                }
            }
        }
        return g;
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
