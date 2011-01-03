/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.commons;

import org.jdom.DataConversionException;
import org.jdom.Element;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.SimpleUpdateablePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.rasterservice.MapService;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @version  $Revision$, $Date$
 */
@Deprecated
public class DefaultMappingModel implements MappingModel, Configurable {

    //~ Static fields/initializers ---------------------------------------------

    public static final int UP = -1;
    public static final int DOWN = 1;

    //~ Instance fields --------------------------------------------------------

    TreeMap mapServices = new TreeMap();
    TreeMap featureServices = new TreeMap();
    FeatureCollection featureCollection;
    Vector mappingModelListeners = new Vector();
    BoundingBox initialBoundingBox;

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of DefaultMappingModel.
     */
    public DefaultMappingModel() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   rs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int moveRasterServiceUp(final MapService rs) {
        return moveRasterService(rs, UP);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   rs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int moveRasterServiceDown(final MapService rs) {
        return moveRasterService(rs, DOWN);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   rs    DOCUMENT ME!
     * @param   step  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int moveRasterService(final MapService rs, final int step) {
        return moveObjectInTreeMap(mapServices, rs, step);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   tm    DOCUMENT ME!
     * @param   o     DOCUMENT ME!
     * @param   step  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int moveObjectInTreeMap(final TreeMap tm, final Object o, final int step) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("moveObjectInTreeMap"); // NOI18N
            }
            final Vector v = new Vector(tm.values());
            final int currentPosition = v.indexOf(o);
            final int newPosition = currentPosition + step;
            final Object objectToBeShifted = v.get(newPosition);
            v.set(newPosition, o);
            v.set(currentPosition, objectToBeShifted);
            tm.clear();
            for (int i = 0; i < v.size(); ++i) {
                tm.put(i, v.get(i));
            }
            return newPosition;
        } catch (Exception e) {
            log.warn("No moving", e);             // NOI18N
            return -1;
        }
    }

//    public static void main(String[] args) {
//        DefaultMappingModel dm = new DefaultMappingModel();
//        TreeMap tm = new TreeMap();
//        tm.put(3, "C");
//        tm.put(4, "D");
//        tm.put(5, "E");
//        tm.put(1, "A");
//        tm.put(2, "B");
//        System.out.println(tm);
//
//        dm.moveObjectInTreeMap(tm, "A", DOWN);
//        System.out.println(tm);
//    }

//    public void putFeatureService(int position, de.cismet.cismap.commons.featureservice.FeatureService featureService) {
//        featureServices.put(new Integer(position), featureService);
//    }

    @Override
    public void addMappingModelListener(final MappingModelListener mml) {
        if (!(mappingModelListeners.contains(mml))) {
            mappingModelListeners.add(mml);
        }
    }

//    public void removeFeatureService(de.cismet.cismap.commons.featureservice.FeatureService featureService) {
//    }

    @Override
    public void removeMappingModelListener(final MappingModelListener mml) {
        mappingModelListeners.remove(mml);
    }
    /**
     * public void setFeatureCollection(de.cismet.cismap.commons.features.FeatureCollection featureCollection) {
     * this.featureCollection=featureCollection; featureCollection.addFeatureCollectionListener(this); }.
     *
     * @param  bb  DOCUMENT ME!
     */
    public void setInitialBoundingBox(final BoundingBox bb) {
        initialBoundingBox = bb;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  rasterService  DOCUMENT ME!
     */
    public void removeMapService(final de.cismet.cismap.commons.rasterservice.MapService rasterService) {
        mapServices.remove(rasterService);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  position       DOCUMENT ME!
     * @param  rasterService  DOCUMENT ME!
     */
    public void putMapService(final int position,
            final de.cismet.cismap.commons.rasterservice.MapService rasterService) {
        mapServices.put(new Integer(position), rasterService);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Deprecated
    public de.cismet.cismap.commons.features.FeatureCollection getFeatureCollection() {
        return featureCollection;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public TreeMap getMapServices() {
        return featureServices;
    }

    @Override
    public BoundingBox getInitialBoundingBox() {
        return initialBoundingBox;
    }

    @Override
    public TreeMap getRasterServices() {
        return mapServices;
    }

//    public void selectionChanged(MappingModelEvent mme) {
//        Iterator it=mappingModelListeners.iterator();
//        while (it.hasNext()) {
//            Object o=it.next();
//            if (o instanceof MappingModelListener) {
//                ((MappingModelListener)o).selectionChanged(mme);
//            }
//            else {
//                //TODO Loggerausgabe
//            }
//        }
//    }
//
//    public void featureCollectionChanged(MappingModelEvent mme) {
//        log.debug("DefaultMappingModel:featureCollectionChanged()");
//        Iterator it=mappingModelListeners.iterator();
//        while (it.hasNext()) {
//            Object o=it.next();
//            if (o instanceof MappingModelListener) {
//                log.debug("MappingModelListener:featureCollectionChanged()");
//                ((MappingModelListener)o).featureCollectionChanged(mme);
//            }
//            else {
//                //TODO Loggerausgabe
//            }
//        }
//    }
    // TODO
    @Override
    public void addLayer(final RetrievalServiceLayer layer) {
    }

    @Override
    public void removeLayer(final RetrievalServiceLayer layer) {
    }

    @Override
    public void configure(final Element parent) {
        final Element prefs = parent.getChild("cismapMappingPreferences"); // NOI18N

        final XBoundingBox xBox = null;

//
//
//        while (it.hasNext()) {
//
//            //SimpleRasterServices
//            prefs = parent.getChild("cismapActiveLayerConfiguration");
//            List simpleWmsList = prefs.getChild("rasterLayers").getChildren("simpleWms");
//            it = simpleWmsList.iterator();
//            while (it.hasNext()) {
//                Object o = it.next();
//                if (o instanceof Element) {
//                    Element el = (Element) o;
//
//                }
//            }
//
//            //SimplePostgisServices
//            List simplePostgisFeatureServiceList = prefs.getChild("featureLayers").getChildren("simplePostgisFeatureService");
//            it = simplePostgisFeatureServiceList.iterator();
//            while (it.hasNext()) {
//                Object o = it.next();
//                if (o instanceof Element) {
//                    Element el = (Element) o;
//
//                }
//            }
//
//            //SimpleWebFeatureServices
//            List simpleWFSFeatureServiceList = prefs.getChild("featureLayers").getChildren("simpleWebFeatureService");
//            it = simpleWFSFeatureServiceList.iterator();
//            while (it.hasNext()) {
//                Object o = it.next();
//                if (o instanceof Element) {
//
//                }
//            }
//        }
    }

    @Override
    public void masterConfigure(final Element parent) {
        if (log.isDebugEnabled()) {
            log.debug("masterConfigure im DefaultmappingModel:" + parent); // NOI18N
        }

        Element prefs = parent.getChild("cismapMappingPreferences"); // NOI18N

        Iterator<Element> it = prefs.getChildren("home").iterator(); // NOI18N
        XBoundingBox xBox = null;

        while (it.hasNext()) {
            final Element elem = it.next();
            final String srs = elem.getAttribute("srs").getValue();                                   // NOI18N
            boolean metric = false;
            try {
                metric = elem.getAttribute("metric").getBooleanValue();                               // NOI18N
            } catch (DataConversionException dce) {
                log.warn("Metric has invalid syntax", dce);                                           // NOI18N
            }
            boolean defaultVal = false;
            try {
                defaultVal = elem.getAttribute("default").getBooleanValue();                          // NOI18N
            } catch (DataConversionException dce) {
                log.warn("defaulthas invalid syntax", dce);                                           // NOI18N
            }
            if (defaultVal) {
                try {
                    xBox = new XBoundingBox(elem, srs, metric);
                    setInitialBoundingBox(xBox);
                } catch (Throwable t) {
                    log.fatal("The home bounding box cannot be set. This will probably fail :-7", t); // NOI18N
                }
            }
        }
        // failure because there could be several boundingBoxes

        getInitialBoundingBox();
        // SimpleRasterServices
        prefs = parent.getChild("cismapActiveLayerConfiguration");                                          // NOI18N
        final List simpleWmsList = prefs.getChild("rasterLayers").getChildren("simpleWms");                 // NOI18N
        it = simpleWmsList.iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if (o instanceof Element) {
                final Element el = (Element)o;
                try {
                    boolean skip = false;
                    try {
                        skip = el.getAttribute("skip").getBooleanValue();                                   // NOI18N
                    } catch (Exception skipException) {
                    }
                    if (!skip) {
                        final SimpleWMS swms = new SimpleWMS(el);
                        mapServices.put(new Integer(swms.getLayerPosition()), swms);
                        if (log.isDebugEnabled()) {
                            log.debug("Rasterservice added:" + swms + "(" + swms.getLayerPosition() + ")"); // NOI18N
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Read Preferences. Error. create SimpleWMS  ", ex);                            // NOI18N
                }
            }
        }

        // SimplePostgisServices
        final List simplePostgisFeatureServiceList = prefs.getChild("featureLayers")
                    .getChildren("simplePostgisFeatureService");                      // NOI18N
        it = simplePostgisFeatureServiceList.iterator();
        while (it.hasNext()) {
            if (log.isDebugEnabled()) {
                log.debug("new SimplePostgisService");                                // NOI18N
            }
            final Object o = it.next();
            if (o instanceof Element) {
                final Element el = (Element)o;
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("SimplePostgisFeatureService added");               // NOI18N
                    }
                    boolean skip = false;
                    boolean updateable = false;
                    try {
                        skip = el.getAttribute("skip").getBooleanValue();             // NOI18N
                    } catch (Exception skipException) {
                    }
                    try {
                        updateable = el.getAttribute("updateable").getBooleanValue(); // NOI18N
                    } catch (Exception skipException) {
                    }
                    if (!skip) {
                        SimplePostgisFeatureService spfs = null;
                        if (updateable) {
                            spfs = new SimpleUpdateablePostgisFeatureService(el);
                        } else {
                            spfs = new SimplePostgisFeatureService(el);
                        }

                        featureServices.put(new Integer(spfs.getLayerPosition()), spfs);
                    }
                } catch (Exception ex) {
                    log.warn("Read Preferences. Error. Create SimplePostgisFeatureService ", ex); // NOI18N
                }
            }
        }

//        //SimpleWebFeatureServices
//        List simpleWFSFeatureServiceList = prefs.getChild("featureLayers").getChildren("simpleWebFeatureService");
//        it = simpleWFSFeatureServiceList.iterator();
//        while (it.hasNext()) {
//            Object o = it.next();
//            if (o instanceof Element) {
//                try {
//                    boolean skip = false;
//                    boolean updateable = false;
//                    try {
//                        skip = ((Element) o).getAttribute("skip").getBooleanValue();
//                    } catch (Exception skipException) {
//                    }
//                    try {
//                        updateable = ((Element) o).getAttribute("updateable").getBooleanValue();
//                    } catch (Exception skipException) {
//                    }
//                    if (!skip) {
//                        SimpleWebFeatureService swfs = null;
//                        if (updateable) {
//                        //TODO IMPLEMENT ?
//                        //spfs = new SimpleUpdateablePostgisFeatureService(el);
//                        } else {
//                            swfs = new SimpleWebFeatureService(((Element) o));
//                            //TODO failure static position
//                            putFeatureService(swfs.getLayerPosition(), swfs);
//                        }
//                        //featureServices.put(new Integer(swfs.getLayerPosition()),swfs);
//                        log.debug("SimpleWebFeatureService hinzugef\u00FCgt");
//                    }
//                } catch (Exception ex) {
//                    log.warn("Preferences Auslesen. Fehler. SimpleWebFeatureService erzeugen  ", ex);
//                }
//            }
//        }

    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        return null;
    }

    @Override
    public TreeMap getFeatureServices() {
        return this.featureServices;
    }
}
