/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.tools;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.io.dbaseapi.DBaseFile;
import org.deegree.io.dbaseapi.FieldDescriptor;
import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.feature.schema.SimplePropertyType;

import java.io.ByteArrayInputStream;

import java.util.ArrayList;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DBaseFileHelper {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DBaseFileHelper.class);

    //~ Methods ----------------------------------------------------------------

//
// /**
// * DOCUMENT ME!
// *
// * @param  url     DOCUMENT ME!
// * @param  source  DOCUMENT ME!
// */
// private static void createDBaseFile(final String url, final FieldDescriptor[] fd, DefaultFeatureServiceFeature[] features) {
//        try {
//
//            final DBaseFile file = new DBaseFile(url, fd);
//
//            for (int i = 0; i < features.length; ++i) {
//                file.setRecord(i, getRecData(features[i]));
//            }
//        } catch (Exception e) {
//            LOG.error("Error while creating dbf index file.", e);
//        }
//    }
//

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     * @param   n        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static FeatureProperty[] getFeatureProperties(final Feature feature, final int n) {
        final PropertyType[] ftp = feature.getFeatureType().getProperties();
        final FeatureProperty[] fp = new FeatureProperty[ftp.length + 1];
        final FeatureProperty[] fp_ = feature.getProperties();

        fp[0] = org.deegree.model.feature.FeatureFactory.createFeatureProperty(new QualifiedName("unique_gid"), n);

        for (int i = 0; i < ftp.length; i++) {
            fp[i + 1] = org.deegree.model.feature.FeatureFactory.createFeatureProperty(
                    ftp[i].getName(),
                    fp_[i].getValue());
        }

        return fp;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   f  DOCUMENT ME!
     * @param   i  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static ArrayList getRecData(final Feature f, final int i) {
        final PropertyType[] ftp = f.getFeatureType().getProperties();
        final ArrayList<Object> list = new ArrayList<Object>(ftp.length + 1);
        list.add(i);

        for (int j = 0; j < ftp.length; j++) {
            if (ftp[j].getType() == Types.GEOMETRY) {
                continue;
            }
            final FeatureProperty fp = f.getDefaultProperty(ftp[j].getName());
            Object obj = null;
            if (fp != null) {
                obj = fp.getValue();
            }

            if (obj instanceof Object[]) {
                obj = ((Object[])obj)[0];
            }

            if ((ftp[j].getType() == Types.INTEGER) || (ftp[j].getType() == Types.BIGINT)
                        || (ftp[j].getType() == Types.SMALLINT)
                        || (ftp[j].getType() == Types.CHAR)
                        || (ftp[j].getType() == Types.FLOAT)
                        || (ftp[j].getType() == Types.DOUBLE)
                        || (ftp[j].getType() == Types.NUMERIC)
                        || (ftp[j].getType() == Types.VARCHAR)
                        || (ftp[j].getType() == Types.DATE)) {
                list.add(obj);
            }
        }

        return list;
    }
}
