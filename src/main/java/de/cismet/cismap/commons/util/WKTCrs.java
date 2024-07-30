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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WKTCrs {

    //~ Instance fields --------------------------------------------------------

    private final String wkt;
    private String proj;
    private String geogcs;
    private String datum;
    private String speroid;
    private String primem;
    private String unit;
    private String projection;
    private final Map<String, String> parameters = new HashMap<>();
    private String unit2;
    private String authority;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WKTCrs object.
     *
     * @param  wkt  DOCUMENT ME!
     */
    public WKTCrs(String wkt) {
        this.wkt = wkt;

        wkt = wkt.replace("PROJCS[", "");
        wkt = wkt.substring(0, wkt.length());

        if (wkt.contains(",")) {
            proj = wkt.substring(0, wkt.indexOf(","));
            proj = proj.replace("\"", "");
            wkt = wkt.substring(wkt.indexOf(",") + 1);

            wkt = wkt.replace("GEOGCS[", "");

            if (wkt.contains(",")) {
                geogcs = wkt.substring(0, wkt.indexOf(","));
                geogcs = geogcs.replace("\"", "");
                wkt = wkt.substring(wkt.indexOf(",") + 1);
                wkt = wkt.replace("DATUM[", "");

                if (wkt.contains(",")) {
                    datum = wkt.substring(0, wkt.indexOf(","));
                    datum = geogcs.replace("\"", "");
                    wkt = wkt.substring(wkt.indexOf(",") + 1);
                    wkt = wkt.replace("SPHEROID[", "");

                    if (wkt.contains("]")) {
                        speroid = wkt.substring(0, wkt.indexOf("]"));
                        wkt = wkt.substring(wkt.indexOf("]") + 3);
                        wkt = wkt.replace("PRIMEM[", "");

                        if (wkt.contains("]")) {
                            primem = wkt.substring(0, wkt.indexOf("]"));
                            wkt = wkt.substring(wkt.indexOf("]") + 2);
                            wkt = wkt.replace("UNIT[", "");

                            if (wkt.contains("]")) {
                                unit = wkt.substring(0, wkt.indexOf("]"));
                                wkt = wkt.substring(wkt.indexOf("]") + 3);
                                wkt = wkt.replace("PROJECTION[", "");

                                if (wkt.contains("]")) {
                                    projection = wkt.substring(0, wkt.indexOf("]"));
                                    projection = projection.replace("\"", "");
                                    wkt = wkt.substring(wkt.indexOf("]") + 2);

                                    while (wkt.trim().startsWith("PARAMETER")) {
                                        wkt = wkt.substring(wkt.indexOf("PARAMETER[") + "PARAMETER[".length());
                                        final String par = wkt.substring(0, wkt.indexOf("]"));

                                        if (par.contains(",")) {
                                            String key = par.substring(0, par.indexOf(","));
                                            final String value = par.substring(par.indexOf(",") + 1);
                                            key = key.replace("\"", "");
                                            parameters.put(key, value);
                                        }

                                        wkt = wkt.substring(wkt.indexOf("]") + 2);
                                    }

                                    if (wkt.trim().startsWith("UNIT") && wkt.contains("]")) {
                                        wkt = wkt.replace("UNIT[", "");
                                        unit2 = wkt.substring(0, wkt.indexOf("]"));
                                        wkt = wkt.substring(wkt.indexOf("]") + 2);
                                    }

                                    if (wkt.trim().contains("AUTHORITY") && wkt.contains("]")) {
                                        wkt = wkt.substring(wkt.indexOf("\"AUTHORITY[\"") + "AUTHORITY[".length());
                                        authority = wkt.substring(0, wkt.indexOf("]"));

                                        if (authority.contains(",")) {
                                            authority = authority.substring(authority.indexOf(",") + 1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        return String.valueOf(wkt);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof WKTCrs) {
            final WKTCrs other = (WKTCrs)obj;

            if ((other.authority != null) && (this.authority != null)) {
                return other.authority.equalsIgnoreCase(this.authority);
            } else if ((other.proj != null) && (this.proj != null)) {
                return other.proj.equalsIgnoreCase(this.proj);
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (59 * hash) + Objects.hashCode(this.wkt);
        return hash;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getIdentifier() {
        return authority;
    }
}
