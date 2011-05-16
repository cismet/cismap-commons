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
 * The objects of this class represent a topic extracted from the configuration file. It comprises of a name, the query
 * and the URL of the corresponding server..
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class ExportWFS implements Comparable {

    //~ Instance fields --------------------------------------------------------

    private String topic;
    private String file;
    private String query;
    private URL url;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportWFS object.
     *
     * @param  topic  The topic.
     * @param  file   The file name.
     * @param  query  The query.
     * @param  url    The URL of the server.
     */
    public ExportWFS(final String topic, final String file, final String query, final URL url) {
        this.topic = topic;
        this.file = file;
        this.query = query;
        this.url = url;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the topic of this export.
     *
     * @return  The topic.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Returns the file of this export.
     *
     * @return  The file.
     */
    public String getFile() {
        return file;
    }

    /**
     * Returns the query of this export.
     *
     * @return  The query.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query of this export.
     *
     * @param  query  The query.
     */
    public void setQuery(final String query) {
        this.query = query;
    }

    /**
     * Returns the URL of the server used by this export.
     *
     * @return  The URL of the server.
     */
    public URL getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return topic;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ExportWFS)) {
            return false;
        }

        final ExportWFS other = (ExportWFS)obj;

        boolean result = true;

        if ((this.topic == null) ? (other.topic != null) : (!this.topic.equals(other.topic))) {
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

        result = (43 * result) + ((this.topic != null) ? this.topic.hashCode() : 0);
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

        return topic.compareTo(other.topic);
    }
}
