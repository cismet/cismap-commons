/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2010 thorsten
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cismap.commons.gui.featureinfowidget;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class FeatureInfoDisplayKey {

    //~ Static fields/initializers ---------------------------------------------

    public static final String ANY = null;

    //~ Instance fields --------------------------------------------------------

    private String javaclass;
    private String server;
    private String layer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FeatureInfoDisplayKey object.
     *
     * @param  javaclass  DOCUMENT ME!
     * @param  server     DOCUMENT ME!
     * @param  layer      DOCUMENT ME!
     */
    public FeatureInfoDisplayKey(final String javaclass, final String server, final String layer) {
        this.javaclass = javaclass;
        this.server = server;
        this.layer = layer;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getJavaclass() {
        return javaclass;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getLayer() {
        return layer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getServer() {
        return server;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FeatureInfoDisplayKey other = (FeatureInfoDisplayKey)obj;
        if ((this.javaclass == null) ? (other.javaclass != null) : (!this.javaclass.equals(other.javaclass))) {
            return false;
        }
        if ((this.server == null) ? (other.server != null) : (!this.server.equals(other.server))) {
            return false;
        }
        if ((this.layer == null) ? (other.layer != null) : (!this.layer.equals(other.layer))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = (59 * hash) + ((this.javaclass != null) ? this.javaclass.hashCode() : 0);
        hash = (59 * hash) + ((this.server != null) ? this.server.hashCode() : 0);
        hash = (59 * hash) + ((this.layer != null) ? this.layer.hashCode() : 0);
        return hash;
    }
}
