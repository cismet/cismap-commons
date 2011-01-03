/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons.featureservice;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
public class SimpleFeatureServiceSqlStatement {

    //~ Static fields/initializers ---------------------------------------------

    public static final String ALL_FIELDS_TOKEN = "<cismap::AllFields>"; // NOI18N
    public static final String X1_TOKEN = "<cismap::x1>";          // NOI18N
    public static final String Y1_TOKEN = "<cismap::y1>";          // NOI18N
    public static final String X2_TOKEN = "<cismap::x2>";          // NOI18N
    public static final String Y2_TOKEN = "<cismap::y2>";          // NOI18N

    //~ Instance fields --------------------------------------------------------

    /** Creates a new instance of SimpleFeatureServiceSqlStatement. */

    String sqlTemplate;
    String allFieldsToken;
    String x1Token;
    String y1Token;
    String x2Token;
    String y2Token;

    private String allFields = ""; // NOI18N
    private double x1 = 0;
    private double y1 = 0;
    private double x2 = 0;
    private double y2 = 0;
    private String orderBy = "";   // NOI18N

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SimpleFeatureServiceSqlStatement object.
     *
     * @param  sqlTemplate  DOCUMENT ME!
     */
    public SimpleFeatureServiceSqlStatement(final String sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
        this.allFieldsToken = ALL_FIELDS_TOKEN;
        this.x1Token = X1_TOKEN;
        this.y1Token = Y1_TOKEN;
        this.x2Token = X2_TOKEN;
        this.y2Token = Y2_TOKEN;
    }

    /**
     * Creates a new SimpleFeatureServiceSqlStatement object.
     *
     * @param  sqlTemplate     DOCUMENT ME!
     * @param  allFieldsToken  DOCUMENT ME!
     * @param  x1Token         DOCUMENT ME!
     * @param  y1Token         DOCUMENT ME!
     * @param  x2Token         DOCUMENT ME!
     * @param  y2Token         DOCUMENT ME!
     */
    public SimpleFeatureServiceSqlStatement(final String sqlTemplate,
            final String allFieldsToken,
            final String x1Token,
            final String y1Token,
            final String x2Token,
            final String y2Token) {
        this.sqlTemplate = sqlTemplate;
        this.allFieldsToken = allFieldsToken;
        this.x1Token = x1Token;
        this.y1Token = y1Token;
        this.x2Token = x2Token;
        this.y2Token = y2Token;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getX1() {
        return x1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  x1  DOCUMENT ME!
     */
    public void setX1(final double x1) {
        this.x1 = x1;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getY1() {
        return y1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  y1  DOCUMENT ME!
     */
    public void setY1(final double y1) {
        this.y1 = y1;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getX2() {
        return x2;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  x2  DOCUMENT ME!
     */
    public void setX2(final double x2) {
        this.x2 = x2;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getY2() {
        return y2;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  y2  DOCUMENT ME!
     */
    public void setY2(final double y2) {
        this.y2 = y2;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getFeaturesStatement() {
        String ret = sqlTemplate;
        ret = sqlTemplate.replaceAll(allFieldsToken, allFields);
        ret = ret.replaceAll(x1Token, x1 + ""); // NOI18N
        ret = ret.replaceAll(y1Token, y1 + ""); // NOI18N
        ret = ret.replaceAll(x2Token, x2 + ""); // NOI18N
        ret = ret.replaceAll(y2Token, y2 + ""); // NOI18N
        return ret + " " + orderBy;             // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCountFeaturesStatement() {
        String ret = sqlTemplate;
        ret = ret.replaceAll(allFieldsToken, "count(*)"); // NOI18N
        ret = ret.replaceAll(x1Token, x1 + "");           // NOI18N
        ret = ret.replaceAll(y1Token, y1 + "");           // NOI18N
        ret = ret.replaceAll(x2Token, x2 + "");           // NOI18N
        ret = ret.replaceAll(y2Token, y2 + "");           // NOI18N
        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getAllFields() {
        return allFields;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  allFields  DOCUMENT ME!
     */
    public void setAllFields(final String allFields) {
        this.allFields = allFields;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getSqlTemplate() {
        return sqlTemplate;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final SimpleFeatureServiceSqlStatement sqlStatement = new SimpleFeatureServiceSqlStatement(
                "select <cismap::AllFields> from geom, cs_all_attr_mapping where geom.id=attr_object_id and attr_class_id=0 and class_id=11 and geom.geo_field && GeomFromText('BOX3D(<cismap::x1> <cismap::y1>,<cismap::x2> <cismap::y2>)',-1)", // NOI18N
                "<cismap::AllFields>",
                "<cismap::x1>",
                "<cismap::y1>",
                "<cismap::x2>",
                "<cismap::y2>"); // NOI18N
        sqlStatement.setAllFields(
            "'Kassenzeichen' as Type,object_id as GroupingKey, object_id as ObjectName,geo_field as Geom"); // NOI18N
        sqlStatement.setX1(1);
        sqlStatement.setY1(2);
        sqlStatement.setX2(3);
        sqlStatement.setY2(4);

        // System.out.println(sqlStatement.getCountFeaturesStatement());
        // System.out.println(sqlStatement.getFeaturesStatement());

    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  orderBy  DOCUMENT ME!
     */
    public void setOrderBy(final String orderBy) {
        this.orderBy = orderBy;
    }
}
