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

import org.geotools.referencing.wkt.Parser;

import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.text.ParseException;

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
            final CoordinateReferenceSystem crs,
            final CoordinateReferenceSystem otherCrs) {
        final String definitionWithoutName = crs.toWKT().substring(crs.toWKT().indexOf("\n") + 1);
        final String otherDefinitionWithoutName = otherCrs.toWKT().substring(otherCrs.toWKT().indexOf("\n") + 1);

        if ((crs.getCoordinateSystem() != null) && (crs.getCoordinateSystem().getIdentifiers() != null)
                    && !crs.getCoordinateSystem().getIdentifiers().isEmpty()) {
            for (final ReferenceIdentifier ri : crs.getCoordinateSystem().getIdentifiers()) {
                if (ri.toString().equals(authority)) {
                    return true;
                }
            }
        }

        return definitionWithoutName.equals(otherDefinitionWithoutName);
    }

    /**
     * Reads all crs definitions from the cismapPrjMapping properties file.
     *
     * @return  all crs definitions from the cismapPrjMapping properties file. The key of the map is the epsg code of
     *          the crs.
     */
    public static Map<Crs, CoordinateReferenceSystem> getKnownCrsMappings() {
        final Map<Crs, CoordinateReferenceSystem> prjMap = new HashMap<Crs, CoordinateReferenceSystem>();

        final List<Crs> crsList = CismapBroker.getInstance().getMappingComponent().getCrsList();

        if (crsList != null) {
            final Parser parser = new Parser();

            for (final Crs crs : crsList) {
                if (crs.hasEsriDefinition()) {
                    try {
                        prjMap.put(
                            crs,
                            parser.parseCoordinateReferenceSystem(crsDefinitionAdjustments(crs.getEsriDefinition())));
                    } catch (ParseException e) {
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
