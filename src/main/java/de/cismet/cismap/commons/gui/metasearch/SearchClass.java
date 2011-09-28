/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.metasearch;

import org.apache.log4j.Logger;

/**
 * DOCUMENT ME!
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class SearchClass implements Comparable<SearchClass> {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SearchClass.class);

    //~ Instance fields --------------------------------------------------------

    private String cidsDomain;
    private String cidsClass;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SearchClass object.
     *
     * @param  cidsDomain  DOCUMENT ME!
     * @param  cidsClass   DOCUMENT ME!
     */
    public SearchClass(final String cidsDomain, final String cidsClass) {
        this.cidsDomain = cidsDomain;
        this.cidsClass = cidsClass;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCidsClass() {
        return cidsClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCidsDomain() {
        return cidsDomain;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof SearchClass)) {
            return false;
        }

        final SearchClass other = (SearchClass)obj;

        if ((this.cidsDomain == null) ? (other.cidsDomain != null) : (!this.cidsDomain.equals(other.cidsDomain))) {
            return false;
        }
        if ((this.cidsClass == null) ? (other.cidsClass != null) : (!this.cidsClass.equals(other.cidsClass))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = (61 * hash) + ((this.cidsDomain != null) ? this.cidsDomain.hashCode() : 0);
        hash = (61 * hash) + ((this.cidsClass != null) ? this.cidsClass.hashCode() : 0);

        return hash;
    }

    @Override
    public int compareTo(final SearchClass o) {
        if (o == null) {
            return 1;
        }

        return cidsClass.compareTo(o.cidsClass);
    }

    @Override
    public String toString() {
        return cidsClass + "@" + cidsDomain;
    }
}
