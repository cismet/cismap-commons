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
package de.cismet.cismap.commons.features;

import com.vividsolutions.jts.geom.Geometry;

import org.deegree.feature.Feature;
import org.deegree.feature.types.FeatureType;
import org.deegree.io.shpapi.shape_new.ShapeFile;
import org.deegree.io.shpapi.shape_new.ShapeFileWriter;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.spatialschema.JTSAdapter;

import java.io.File;
import java.io.FileFilter;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.ShapeFileFeatureService;
import de.cismet.cismap.commons.featureservice.factory.ShapeFeatureFactory;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.SimpleFeatureCollection;
import de.cismet.cismap.commons.util.SelectionManager;

/**
 * Features read from a SHP File.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class ShapeFeature extends DefaultFeatureServiceFeature implements ModifiableFeature {

    //~ Static fields/initializers ---------------------------------------------

    // caches the last feature properties
    private static final Object sync = new Object();

    //~ Instance fields --------------------------------------------------------

    private final ShapeInfo shapeInfo;
    private Geometry geom;
    private boolean isChanged;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShapeFeature object.
     *
     * @param  shapeInfo  typename DOCUMENT ME!
     */
    public ShapeFeature(final ShapeInfo shapeInfo) {
        this.shapeInfo = shapeInfo;
    }

    /**
     * Creates a new ShapeFeature object.
     *
     * @param  shapeInfo  typename DOCUMENT ME!
     * @param  styles     DOCUMENT ME!
     */
    public ShapeFeature(final ShapeInfo shapeInfo, final List<org.deegree.style.se.unevaluated.Style> styles) {
        setSLDStyles(styles); // super.style = styles;
        this.shapeInfo = shapeInfo;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setEditable(final boolean editable) {
        final boolean oldEditableStatus = isEditable();
        super.setEditable(editable);

        if (oldEditableStatus != editable) {
            isChanged = false;

            CismapBroker.getInstance().getMappingComponent().getFeatureCollection().unholdFeature(this);
            CismapBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeature(this);

            if (editable) {
                CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addFeature(this);
                CismapBroker.getInstance().getMappingComponent().getFeatureCollection().holdFeature(this);
//                SelectionManager.getInstance().addSelectedFeatures(Collections.nCopies(1, this));
//                setBackgroundColor(new Color(255, 91, 0));
            }
        }
    }

    /**
     * /** * Creates a new ShapeFeature object. * * @param typename DOCUMENT ME! * @param styles DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public HashMap getProperties() {
        LinkedHashMap<String, Object> container = null;
        final int id = getId();
        if (existProperties()) {
            return super.getProperties();
        }

        if (shapeInfo.getFc() == null) {
            try {
                container = shapeInfo.getPropertiesFromCache(id);

                if (container != null) {
                    return container;
                } else {
                    container = new LinkedHashMap<String, Object>();
                }

                org.deegree.model.feature.Feature degreeFeature = null;
                synchronized (sync) {
                    // getFeatureByRecNo is not threadsafe
                    degreeFeature = shapeInfo.getFile().getFeatureByRecNo(id);
                }
                final FeatureProperty[] featureProperties = degreeFeature.getProperties();

                for (final FeatureProperty fp : featureProperties) {
                    container.put(fp.getName().getAsString(), fp.getValue());
                }

                shapeInfo.addPropertiesToCache(id, container);
            } catch (final Exception e) {
                logger.error("Cannot read properties from file.", e);
            }
        } else {
            container = new LinkedHashMap<String, Object>();

            try {
                final org.deegree.model.feature.Feature degreeFeature = shapeInfo.getFc().getFeature(id);
                final FeatureProperty[] featureProperties = degreeFeature.getProperties();

                for (final FeatureProperty fp : featureProperties) {
                    container.put(fp.getName().getAsString(), fp.getValue());
                }
            } catch (final Exception e) {
                logger.error("Cannot read properties from file.", e);
            }
        }

        return container;
    }

    @Override
    public Object getProperty(final String propertyName) {
        return getProperties().get(propertyName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  propertyName   DOCUMENT ME!
     * @param  propertyValue  DOCUMENT ME!
     */
    @Override
    public void setProperty(final String propertyName, final Object propertyValue) {
        if (!existProperties()) {
            super.setProperties(getProperties());
        }
        isChanged = true;
        super.addProperty(propertyName, propertyValue);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean existProperties() {
        return !super.getProperties().isEmpty();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public FeatureServiceFeature saveChanges() throws Exception {
        final AbstractFeatureService featureService = getLayerProperties().getFeatureService();
        final List<FeatureServiceFeature> features = featureService.retrieveFeatures(null, 0, 0, null);
        features.remove(this);
        features.add(this);
        Collections.sort(features, new Comparator<FeatureServiceFeature>() {

                @Override
                public int compare(final FeatureServiceFeature o1, final FeatureServiceFeature o2) {
                    return Integer.compare(o1.getId(), o2.getId());
                }
            });

        final org.deegree.model.feature.FeatureCollection fc = new SimpleFeatureCollection(
                String.valueOf(System.currentTimeMillis()),
                features.toArray(new FeatureServiceFeature[features.size()]),
                null);
        String filename = ((ShapeFileFeatureService)featureService).getDocumentURI().getPath();
        final File shapeFile = new File(filename);

        if (shapeFile.exists()) {
            String file = shapeFile.getName();
            if (file.contains(".")) {
                file = file.substring(0, file.lastIndexOf("."));
            }
            final String nameStem = file;

            final File[] files = shapeFile.getParentFile().listFiles(new FileFilter() {

                        @Override
                        public boolean accept(final File pathname) {
                            return pathname.getName().substring(0, nameStem.length()).equals(nameStem);
                        }
                    });

            for (final File f : files) {
                if (f.getName().endsWith(".sbx") || f.getName().endsWith(".rti")) {
                    f.delete();
                }
            }
        }
        if (filename.contains(".")) {
            filename = filename.substring(0, filename.lastIndexOf("."));
        }

        final ShapeFile shape = new ShapeFile(
                fc,
                filename);
        final ShapeFileWriter writer = new ShapeFileWriter(shape);
        writer.write();
//        org.deegree.model.feature.Feature deegreeFeature = null;

//        synchronized (sync) {
//            deegreeFeature = shapeInfo.getFile().getFeatureByRecNo(getId());
//        }
//
//        final Map<String, Object> map = super.getProperties();
//
//        final FeatureProperty[] featureProperties = deegreeFeature.getProperties();
//        for (final FeatureProperty fp : featureProperties) {
//            fp.setValue(map.get(fp.getName().getAsString()));
//        }
//
//        shapeInfo.getFile().writeShape(null);
        ((ShapeFeatureFactory)featureService.getFeatureFactory()).refreshData();
        super.getProperties().clear();
        geom = null;
        isChanged = false;
        return this;
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void undoAll() {
        super.getProperties().clear();
        geom = null;
    }

    @Override
    public void addProperty(final String propertyName, final Object property) {
        // nothing to do
    }

    /**
     * DOCUMENT ME!
     *
     * @param  map  DOCUMENT ME!
     */
    @Override
    public void addProperties(final Map<String, Object> map) {
        // nothing to do
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Geometry getGeometry() {
        if (geom != null) {
            return geom;
        }
        Geometry g = null;
        if (shapeInfo.getFc() == null) {
            g = shapeInfo.getGeometryFromCache(getId());

            if (g != null) {
                return g;
            }

            try {
                g = JTSAdapter.export(shapeInfo.getFile().getGeometryByRecNo(getId()));
                g.setSRID(shapeInfo.getSrid());
            } catch (final Exception e) {
                logger.error("Cannot read geometry from shape file.", e);
            }

            shapeInfo.addGeometryToCache(getId(), g);
        } else {
            try {
                g = JTSAdapter.export(shapeInfo.getFc().getFeature(getId()).getDefaultGeometryPropertyValue());
                g.setSRID(shapeInfo.getSrid());
            } catch (final Exception e) {
                logger.error("Cannot read geometry from shape file.", e);
            }
        }

        return g;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  geom  DOCUMENT ME!
     */
    @Override
    public void setGeometry(final Geometry geom) {
        this.geom = geom;
        isChanged = true;
    }

    @Override
    protected Feature getDeegreeFeature() {
        return new ShapeFileLayerDeegreeFeature();
    }

    @Override
    public void saveChangesWithoutReload() throws Exception {
        saveChanges();
    }

    @Override
    public void delete() throws Exception {
    }

    @Override
    public void restore() throws Exception {
    }

    @Override
    public boolean isFeatureChanged() {
        return isChanged;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected class ShapeFileLayerDeegreeFeature extends DefaultFeatureServiceFeature.DeegreeFeature {

        //~ Methods ------------------------------------------------------------

        @Override
        public FeatureType getType() {
            return new DefaultFeatureServiceFeature.DeegreeFeatureType() {

                    @Override
                    public QName getName() {
                        return new QName("Feature"); // for demo only
                    }
                };
        }
    }
}
