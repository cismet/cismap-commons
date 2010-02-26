/*
 * DefaultMappingModel.java
 * Copyright (C) 2005 by:
 *
 *----------------------------
 * cismet GmbH
 * Goebenstrasse 40
 * 66117 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *----------------------------
 * Author:
 * thorsten.hell@cismet.de
 *----------------------------
 *
 * Created on 23. Juni 2005, 12:50
 *
 */
package de.cismet.cismap.commons;

import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.SimpleUpdateablePostgisFeatureService;
import de.cismet.cismap.commons.featureservice.WebFeatureService;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 *
 * @author thorsten.hell@cismet.de
 */
@Deprecated
public class DefaultMappingModel implements MappingModel, Configurable {

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public final static int UP = -1;
    public final static int DOWN = 1;
    TreeMap mapServices = new TreeMap();
    TreeMap featureServices = new TreeMap();
    FeatureCollection featureCollection;
    Vector mappingModelListeners = new Vector();
    BoundingBox initialBoundingBox;

    /** Creates a new instance of DefaultMappingModel */
    public DefaultMappingModel() {
    }

    

    public int moveRasterServiceUp(MapService rs) {
        return moveRasterService(rs, UP);
    }

    public int moveRasterServiceDown(MapService rs) {
        return moveRasterService(rs, DOWN);
    }

    public int moveRasterService(MapService rs, int step) {
        return moveObjectInTreeMap(mapServices, rs, step);
    }

    private int moveObjectInTreeMap(TreeMap tm, Object o, int step) {
        try {
            log.debug("moveObjectInTreeMap");
            Vector v = new Vector(tm.values());
            int currentPosition = v.indexOf(o);
            int newPosition = currentPosition + step;
            Object objectToBeShifted = v.get(newPosition);
            v.set(newPosition, o);
            v.set(currentPosition, objectToBeShifted);
            tm.clear();
            for (int i = 0; i < v.size(); ++i) {
                tm.put(i, v.get(i));
            }
            return newPosition;
        } catch (Exception e) {
            log.warn("No moving", e);
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
    public void addMappingModelListener(MappingModelListener mml) {
        if (!(mappingModelListeners.contains(mml))) {
            mappingModelListeners.add(mml);
        }
    }

//    public void removeFeatureService(de.cismet.cismap.commons.featureservice.FeatureService featureService) {
//    }

  @Override
    public void removeMappingModelListener(MappingModelListener mml) {
        mappingModelListeners.remove(mml);
    }

//    public void setFeatureCollection(de.cismet.cismap.commons.features.FeatureCollection featureCollection) {
//        this.featureCollection=featureCollection;
//       featureCollection.addFeatureCollectionListener(this);
//    }
    public void setInitialBoundingBox(BoundingBox bb) {
        initialBoundingBox = bb;
    }

    public void removeMapService(de.cismet.cismap.commons.rasterservice.MapService rasterService) {
        mapServices.remove(rasterService);
    }

    public void putMapService(int position, de.cismet.cismap.commons.rasterservice.MapService rasterService) {
        mapServices.put(new Integer(position), rasterService);
    }

    @Deprecated
    public de.cismet.cismap.commons.features.FeatureCollection getFeatureCollection() {
        return featureCollection;
    }


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
    //TODO
  @Override
    public void addLayer(RetrievalServiceLayer layer) {
    }

  @Override
    public void removeLayer(RetrievalServiceLayer layer) {
    }

  @Override
    public void configure(Element parent) {
        Element prefs = parent.getChild("cismapMappingPreferences");

        XBoundingBox xBox = null;

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
    public void masterConfigure(Element parent) {
        log.debug("masterConfigure im DefaultmappingModel:" + parent);

        Element prefs = parent.getChild("cismapMappingPreferences");

        Iterator<Element> it = prefs.getChildren("home").iterator();
        XBoundingBox xBox = null;

        while (it.hasNext()) {
            Element elem = it.next();
            String srs = elem.getAttribute("srs").getValue();
            boolean metric = false;
            try {
                metric = elem.getAttribute("metric").getBooleanValue();
            } catch (DataConversionException dce) {
                log.warn("Metric hat falschen Syntax", dce);
            }
            boolean defaultVal = false;
            try {
                defaultVal = elem.getAttribute("default").getBooleanValue();
            } catch (DataConversionException dce) {
                log.warn("default hat falschen Syntax", dce);
            }
            if (defaultVal) {
                try {
                    xBox = new XBoundingBox(elem, srs, metric);
                    setInitialBoundingBox(xBox);
                } catch (Throwable t) {
                    log.fatal("Die Home-BoundingBox konnte nicht gesetzt werden. Das wird wahrscheinlich schiefgehen :-7", t);
                }
            }
        }
        //failure because there could be several boundingBoxes

        getInitialBoundingBox();
        //SimpleRasterServices
        prefs = parent.getChild("cismapActiveLayerConfiguration");
        List simpleWmsList = prefs.getChild("rasterLayers").getChildren("simpleWms");
        it = simpleWmsList.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Element) {
                Element el = (Element) o;
                try {
                    boolean skip = false;
                    try {
                        skip = el.getAttribute("skip").getBooleanValue();
                    } catch (Exception skipException) {
                    }
                    if (!skip) {
                        SimpleWMS swms = new SimpleWMS(el);
                        mapServices.put(new Integer(swms.getLayerPosition()), swms);
                        log.debug("Rasterservice added:" + swms + "(" + swms.getLayerPosition() + ")");
                    }
                } catch (Exception ex) {
                    log.warn("Preferences Auslesen. Fehler. SimpleWMS erzeugen  ", ex);
                }
            }
        }

        //SimplePostgisServices
        List simplePostgisFeatureServiceList = prefs.getChild("featureLayers").getChildren("simplePostgisFeatureService");
        it = simplePostgisFeatureServiceList.iterator();
        while (it.hasNext()) {
            log.debug("new SimplePostgisService");
            Object o = it.next();
            if (o instanceof Element) {
                Element el = (Element) o;
                try {
                    log.debug("SimplePostgisFeatureService hinzugef�gt");
                    boolean skip = false;
                    boolean updateable = false;
                    try {
                        skip = el.getAttribute("skip").getBooleanValue();
                    } catch (Exception skipException) {
                    }
                    try {
                        updateable = el.getAttribute("updateable").getBooleanValue();
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
                    log.warn("Preferences Auslesen. Fehler. SimplePostgisFeatureService erzeugen  ", ex);
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
  public TreeMap getFeatureServices()
  {
    return this.featureServices;
  }
}
