/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.gui.featureinfowidget;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class FeatureInfoDisplayKey {

    //~ Static fields/initializers ---------------------------------------------

    public static final Class ANY_CLASS = null;
    public static final String ANY_SERVER = null;
    public static final String ANY_LAYER = null;

    //~ Instance fields --------------------------------------------------------

    private Class javaclass;
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
    public FeatureInfoDisplayKey(final Class javaclass, final String server, final String layer) {
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
    public Class getJavaclass() {
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
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        final FeatureInfoDisplayKey other = (FeatureInfoDisplayKey)obj;
        if ((this.javaclass == null) ? (other.javaclass != null) : (!this.javaclass.equals(other.javaclass))) {
            return false;
        } else if ((this.server == null) ? (other.server != null) : (!this.server.equals(other.server))) {
            return false;
        } else if ((this.layer == null) ? (other.layer != null) : (!this.layer.equals(other.layer))) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = (59 * hash) + ((this.javaclass != null) ? this.javaclass.hashCode() : 0);
        hash = (59 * hash) + ((this.server != null) ? this.server.hashCode() : 0);
        hash = (59 * hash) + ((this.layer != null) ? this.layer.hashCode() : 0);

        return hash;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());

        sb.append(" [javaclass=").append(javaclass); // NOI18N
        sb.append(", server=").append(server);       // NOI18N
        sb.append(", layer=").append(layer);         // NOI18N
        sb.append("]");                              // NOI18N

        return sb.toString();
    }
}
