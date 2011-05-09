/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2011 jweintraut
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
package de.cismet.cismap.commons.gui.shapeexport;

import java.net.URL;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class ExportWFS implements Comparable {

    //~ Instance fields --------------------------------------------------------

    private String title;
    private String query;
    private URL url;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportWFS object.
     *
     * @param  title  DOCUMENT ME!
     * @param  query  DOCUMENT ME!
     * @param  url    DOCUMENT ME!
     */
    public ExportWFS(final String title, final String query, final URL url) {
        this.title = title;
        this.query = query;
        this.url = url;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getTitle() {
        return title;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  title  DOCUMENT ME!
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getQuery() {
        return query;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  query  url DOCUMENT ME!
     */
    public void setQuery(final String query) {
        this.query = query;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public URL getUrl() {
        return url;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  url  DOCUMENT ME!
     */
    public void setUrl(final URL url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ExportWFS)) {
            return false;
        }

        final ExportWFS other = (ExportWFS)obj;

        boolean result = true;

        if ((this.title == null) ? (other.title != null) : (!this.title.equals(other.title))) {
            result &= false;
        }

        if ((this.url != other.url) && ((this.url == null) || !this.url.equals(other.url))) {
            result &= false;
        }

        if ((this.query == null) ? (other.query != null) : (!this.query.equals(other.query))) {
            result &= false;
        }

        return result;
    }

    @Override
    public int hashCode() {
        int result = 7;

        result = (43 * result) + ((this.title != null) ? this.title.hashCode() : 0);
        result = (43 * result) + ((this.url != null) ? this.url.hashCode() : 0);
        result = (43 * result) + ((this.query != null) ? this.query.hashCode() : 0);

        return result;
    }

    @Override
    public int compareTo(final Object o) {
        if (!(o instanceof ExportWFS)) {
            return 1;
        }

        final ExportWFS other = (ExportWFS)o;

        return title.compareTo(other.title);
    }
}
