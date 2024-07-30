/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.util;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.Crs;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * Provides some helper methods to parse crs wkt.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CrsDeterminer {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CrsDeterminer.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * Compares the given crs.
     *
     * @param   authority  DOCUMENT ME!
     * @param   crs        DOCUMENT ME!
     * @param   otherCrs   DOCUMENT ME!
     *
     * @return  true, if the given crs are equal
     */
    public static boolean isCrsEqual(final String authority,
            final WKTCrs crs,
            final WKTCrs otherCrs) {
        if ((crs == null) || (otherCrs == null)) {
            return false;
        }

        if (((otherCrs.getIdentifier() != null) && (authority != null)
                        && otherCrs.getIdentifier().equalsIgnoreCase(authority))
                    || otherCrs.equals(crs)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Reads all crs definitions from the cismapPrjMapping properties file.
     *
     * @return  all crs definitions from the cismapPrjMapping properties file. The key of the map is the epsg code of
     *          the crs.
     */
    public static Map<Crs, WKTCrs> getKnownCrsMappings() {
        final Map<Crs, WKTCrs> prjMap = new HashMap<>();

        final List<Crs> crsList = CismapBroker.getInstance().getMappingComponent().getCrsList();

        if (crsList != null) {
            for (final Crs crs : crsList) {
                if (crs.hasEsriDefinition()) {
                    try {
                        prjMap.put(
                            crs,
                            new WKTCrs(crs.getEsriDefinition()));
                    } catch (Exception e) {
                        LOG.error("Cannot parse the crs definition for " + crs.getCode() + ":\n"
                                    + crs.getEsriDefinition(),
                            e);
                    }
                }
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No crs definition found");
            }
        }

        return prjMap;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   crsDefinition  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getEpsgCode(String crsDefinition) {
        final Map<Crs, WKTCrs> prjMapping = CrsDeterminer.getKnownCrsMappings();

        if ((prjMapping != null) && !prjMapping.isEmpty()) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("prj file with definition: " + crsDefinition + " found");
                }

                crsDefinition = CrsDeterminer.crsDefinitionAdjustments(crsDefinition);
                final WKTCrs crsFromShape = new WKTCrs(crsDefinition);

                for (final Crs key : prjMapping.keySet()) {
                    if (CrsDeterminer.isCrsEqual(key.getCode(), prjMapping.get(key), crsFromShape)) {
                        return key.getCode();
                    }
                }
            } catch (Exception e) {
                LOG.error("Error while parsing crs " + crsDefinition, e);
            }
        }

        return null;
    }

    /**
     * Adjusts the crs definition that the WKT parser can parse it.
     *
     * @param   definition  Dthe definition to adjust
     *
     * @return  the modified definition
     */
    public static String crsDefinitionAdjustments(final String definition) {
        final String invalidProjection = "projection[\"mercator_auxiliary_sphere\"]";
        final String invalidParameter = "parameter[\"auxiliary_sphere_type\"";
        String tmp = definition;

        if (tmp.toLowerCase().contains(invalidProjection)) {
            // replace mercator_auxiliary_sphere with mercator_2sp, because
            // geotools does not know the mercator_auxiliary_sphere projection
            final String firstPart = tmp.substring(0, tmp.toLowerCase().indexOf(invalidProjection));
            final String secondPart = tmp.substring(tmp.toLowerCase().indexOf(invalidProjection)
                            + invalidProjection.length(),
                    tmp.length());
            tmp = firstPart + "PROJECTION[\"Mercator_2SP\"]" + secondPart;
        }

        if (tmp.toLowerCase().contains(invalidParameter)) {
            // replace the Auxiliary_Sphere_Type parameter, because
            // geotools does not know the Auxiliary_Sphere_Type parameter
            final String firstPart = tmp.substring(0, tmp.toLowerCase().indexOf(invalidParameter));
            String withoutParameterStart = tmp.substring(tmp.toLowerCase().indexOf(invalidParameter)
                            + invalidParameter.length(),
                    tmp.length());
            withoutParameterStart = withoutParameterStart.substring(withoutParameterStart.indexOf("]") + 1,
                    withoutParameterStart.length());
            final String secondPart = withoutParameterStart.substring(withoutParameterStart.indexOf(",") + 1,
                    withoutParameterStart.length());
            tmp = firstPart + secondPart;
        }

        return tmp;
    }
}
