/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice.factory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import com.ysystems.ycad.lib.ydxf.YdxfGet;
import com.ysystems.ycad.lib.ydxf.YdxfGetBuffer;
import com.ysystems.ycad.lib.yxxf.Yxxf;
import com.ysystems.ycad.lib.yxxf.YxxfDictionary;
import com.ysystems.ycad.lib.yxxf.YxxfEnt;
import com.ysystems.ycad.lib.yxxf.YxxfEntArc;
import com.ysystems.ycad.lib.yxxf.YxxfEntCircle;
import com.ysystems.ycad.lib.yxxf.YxxfEntHeader;
import com.ysystems.ycad.lib.yxxf.YxxfEntInsert;
import com.ysystems.ycad.lib.yxxf.YxxfEntLine;
import com.ysystems.ycad.lib.yxxf.YxxfEntLwpolyline;
import com.ysystems.ycad.lib.yxxf.YxxfEntMtext;
import com.ysystems.ycad.lib.yxxf.YxxfEntPoint;
import com.ysystems.ycad.lib.yxxf.YxxfEntPolyline;
import com.ysystems.ycad.lib.yxxf.YxxfEntText;
import com.ysystems.ycad.lib.yxxf.YxxfEntVertex;
import com.ysystems.ycad.lib.yxxf.YxxfGfxPointW;
import com.ysystems.ycad.lib.yxxf.YxxfObject;
import com.ysystems.ycad.lib.yxxf.YxxfXRecord;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.DefaultLayerProperties;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.util.CrsDeterminer;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DxfReader {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DxfReader.class);

    //~ Instance fields --------------------------------------------------------

    private List<FeatureServiceAttribute> featureServiceAttributes;
    private List<FeatureServiceFeature> annotationFeatures;
    private List<FeatureServiceFeature> pointFeatures;
    private List<FeatureServiceFeature> polygonFeatures;
    private List<FeatureServiceFeature> linestringFeatures;
    private LayerProperties properties;
    private int lastGeneratedId = 0;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DxfReader object.
     *
     * @param  filename  DOCUMENT ME!
     */
    public DxfReader(final String filename) {
        annotationFeatures = new ArrayList<FeatureServiceFeature>();
        pointFeatures = new ArrayList<FeatureServiceFeature>();
        polygonFeatures = new ArrayList<FeatureServiceFeature>();
        linestringFeatures = new ArrayList<FeatureServiceFeature>();
        final File f = new File(filename);

        // read file using ycad lib:
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            Yxxf drawing = new Yxxf();
            final YdxfGetBuffer buffer = new YdxfGetBuffer();
            final int type = YdxfGetBuffer.GET_TYPE_MAIN;
            buffer.setInput(type, in, drawing);
            YdxfGet.get(buffer);
            drawing = buffer.getDrawing();

            final int srid = CrsTransformer.extractSridFromCrs(getCrs(buffer));
            printHoleDictionary(buffer);
            final String originFeatureType = getOriginFeatureType(buffer);
            final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);
            int nextToDraw;

            for (nextToDraw = 0;;) {
                final YxxfEnt ent = (YxxfEnt)drawing.secEntities.insMSpace.block.nextEntity(nextToDraw);
                if (!(ent instanceof YxxfEntHeader)) {
                    break;
                }
                if (ent instanceof YxxfEntText) {
                    final YxxfEntText text = (YxxfEntText)ent;
                    final YxxfGfxPointW point = text.inspnt;
                    final Geometry geom = factory.createPoint(new Coordinate(point.x, point.y, point.z));
                    final Map<String, Object> featureAttr = getFeatureAttributes(buffer, text, true);
                    featureAttr.put("annotation", text.text);
                    annotationFeatures.add(toFeature(geom, featureAttr));
                } else if (ent instanceof YxxfEntMtext) {
                    final YxxfEntMtext text = (YxxfEntMtext)ent;
                    final YxxfGfxPointW point = text.inspnt;
                    final Geometry geom = factory.createPoint(new Coordinate(point.x, point.y, point.z));
                    final Map<String, Object> featureAttr = getFeatureAttributes(buffer, text, true);
                    featureAttr.put("annotation", text.text);
                    annotationFeatures.add(toFeature(geom, featureAttr));
                } else if (ent instanceof YxxfEntInsert) {
                    final YxxfEntInsert ins = (YxxfEntInsert)ent;
                    final YxxfGfxPointW point = ins.inspnt;
                    final Geometry geom = factory.createPoint(new Coordinate(point.x, point.y, point.z));
                    final Map<String, Object> featureAttr = getFeatureAttributes(buffer, ins, true);
                    pointFeatures.add(toFeature(geom, featureAttr));
                } else if (ent instanceof YxxfEntPoint) {
                    final YxxfEntPoint point = (YxxfEntPoint)ent;
                    final Geometry geom = factory.createPoint(new Coordinate(point.pnt.x, point.pnt.y, point.pnt.z));
                    final Map<String, Object> featureAttr = getFeatureAttributes(buffer, point, true);
                    pointFeatures.add(toFeature(geom, featureAttr));
                } else if (ent instanceof YxxfEntPolyline) {
                    final YxxfEntPolyline line = (YxxfEntPolyline)ent;
                    final Geometry geom = readPolyLine(line, factory, originFeatureType);
                    final Map<String, Object> featureAttr = getFeatureAttributes(buffer, line, true);

                    if ((originFeatureType != null) && originFeatureType.toLowerCase().equals("polygon")) {
                        polygonFeatures.add(toFeature(geom, featureAttr));
                    } else {
                        linestringFeatures.add(toFeature(geom, featureAttr));
                    }
                } else if (ent instanceof YxxfEntLine) {
                    final YxxfEntLine line = (YxxfEntLine)ent;
                    final Coordinate[] coordArray = new Coordinate[2];

                    Coordinate coord = new Coordinate(line.begpnt.x, line.begpnt.y, line.begpnt.z);
                    coordArray[0] = coord;
                    coord = new Coordinate(line.endpnt.x, line.endpnt.y, line.endpnt.z);
                    coordArray[1] = coord;

                    final Geometry geom = factory.createLineString(coordArray);
                    final Map<String, Object> featureAttr = getFeatureAttributes(buffer, line, true);
                    linestringFeatures.add(toFeature(geom, featureAttr));
                } else if (ent instanceof YxxfEntLwpolyline) {
                    final YxxfEntLwpolyline line = (YxxfEntLwpolyline)ent;
                    final Geometry geom = readPolyLine(line.pline, factory, originFeatureType);
                    final Map<String, Object> featureAttr = getFeatureAttributes(buffer, line, true);
                    if ((originFeatureType != null) && originFeatureType.toLowerCase().equals("polygon")) {
                        polygonFeatures.add(toFeature(geom, featureAttr));
                    } else {
                        linestringFeatures.add(toFeature(geom, featureAttr));
                    }
                } else if (ent instanceof YxxfEntCircle) {
                    final YxxfEntCircle circle = (YxxfEntCircle)ent;
                    final YxxfGfxPointW point = circle.center;
                    final Geometry center = factory.createPoint(new Coordinate(point.x, point.y, point.z));
                    final Geometry geom = center.buffer(circle.radius);
                    final Map<String, Object> featureAttr = getFeatureAttributes(buffer, circle, true);
                    polygonFeatures.add(toFeature(geom, featureAttr));
                } else if (ent instanceof YxxfEntArc) {
                    final YxxfEntArc arc = (YxxfEntArc)ent;
                    final YxxfGfxPointW point = arc.center;

                    final GeometricShapeFactory gsf = new GeometricShapeFactory();
                    gsf.setCentre(new Coordinate(point.x, point.y, point.z));
                    gsf.setSize(arc.radius * 2);
                    final Geometry geom = gsf.createArcPolygon(arc.entbegang, arc.entendang);

                    final Map<String, Object> featureAttr = getFeatureAttributes(buffer, arc, true);
                    polygonFeatures.add(toFeature(geom, featureAttr));
                } else {
                    LOG.warn("Unknown type: " + ent.getClass().getName());
                }
                nextToDraw++;
            }
        } catch (Exception ex) {
            LOG.error("While while reading dxf file.", ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    // nothing to do
                }
            }
            if (properties != null) {
                try {
                    properties.setFeatureService(new H2FeatureService(
                            "dummy",
                            "dummy",
                            null,
                            featureServiceAttributes));
                } catch (Exception e) {
                    LOG.error("Cannot create dummy H2FeatureService.", e);
                }
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
//        String filename = "/home/therter/Downloads/beispieldaten-dxf-dwg/dxf-2010-polyff-ohne-proj-wsg-daten3.dxf";
//        String filename = "/home/therter/Downloads/beispieldaten-dxf-dwg/dxf-2010-poly-mit-proj-ezg-mv-3.dxf";
//        String filename = "/home/therter/Downloads/beispieldaten-dxf-dwg/dxf-2010-poly-ohne-proj-wsg-daten1.dxf";
        final String filename =
            "/home/therter/Downloads/beispieldaten-dxf-dwg/dxf-2010-poly-mit-proj-wsg-daten2/2013-08-01-wsg-eurawasser-hro.dxf";

        new DxfReader(filename);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<FeatureServiceFeature> getPointFeatures() {
        return pointFeatures;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<FeatureServiceFeature> getAnnotationFeatures() {
        return annotationFeatures;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<FeatureServiceFeature> getPolygonFeatures() {
        return polygonFeatures;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<FeatureServiceFeature> getLinestringFeatures() {
        return linestringFeatures;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<FeatureServiceAttribute> getFeatureServiceAttributes() {
        return featureServiceAttributes;
    }

    /**
     * Determines all feature attributes.
     *
     * @param   buffer      DOCUMENT ME!
     * @param   header      DOCUMENT ME!
     * @param   annotation  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private Map<String, Object> getFeatureAttributes(final YdxfGetBuffer buffer,
            final YxxfEntHeader header,
            final boolean annotation) throws Exception {
        final Map<String, Object> attributeMap = new HashMap<String, Object>();
        final String esriAttributesHandle = getDictionaryHandle(buffer, header, "ESRI_Attributes");
        final boolean createFeatureAttirbutes = featureServiceAttributes == null;

        if (esriAttributesHandle != null) {
            final YxxfObject attributeHeader = buffer.currObjectsBlock.getObjectByHandle(esriAttributesHandle);

            if (attributeHeader instanceof YxxfDictionary) {
                final YxxfDictionary dict = (YxxfDictionary)attributeHeader;

                for (final String attrName : dict.nameSoftOwnerIdMap.keySet()) {
                    final String valueHandle = dict.nameSoftOwnerIdMap.get(attrName);
                    final YxxfObject value = buffer.currObjectsBlock.getObjectByHandle(valueHandle);

                    if (value instanceof YxxfXRecord) {
                        final YxxfXRecord val = (YxxfXRecord)value;
                        if (!val.values.isEmpty()) {
                            // take the first element
                            final Integer key = val.values.keySet().iterator().next();
                            attributeMap.put(attrName, getConvertToDbType(val.values.get(key), key));

                            if (createFeatureAttirbutes) {
                                if (featureServiceAttributes == null) {
                                    featureServiceAttributes = new ArrayList<FeatureServiceAttribute>();
                                    properties = new DefaultLayerProperties();
                                    featureServiceAttributes.add(new FeatureServiceAttribute(
                                            "geom",
                                            String.valueOf(Types.GEOMETRY),
                                            true));
                                    if (annotation) {
                                        featureServiceAttributes.add(new FeatureServiceAttribute(
                                                "annotation",
                                                String.valueOf(Types.VARCHAR),
                                                true));
                                    }
                                }
                                final FeatureServiceAttribute attr = new FeatureServiceAttribute(
                                        attrName,
                                        getTypeByEsriCode(key),
                                        true);
                                featureServiceAttributes.add(attr);
                            }
                        }
                    }
                }
            }
        } else {
            if (createFeatureAttirbutes) {
                featureServiceAttributes = new ArrayList<FeatureServiceAttribute>();
                properties = new DefaultLayerProperties();
                featureServiceAttributes.add(new FeatureServiceAttribute("geom", String.valueOf(Types.GEOMETRY), true));
                if (annotation) {
                    featureServiceAttributes.add(new FeatureServiceAttribute(
                            "annotation",
                            String.valueOf(Types.VARCHAR),
                            true));
                }
            }
        }

        return attributeMap;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   buffer  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getOriginFeatureType(final YdxfGetBuffer buffer) {
        final List<YxxfObject> objects = buffer.currObjectsBlock.getObjectsWichPointTo0();

        for (final YxxfObject obj : objects) {
            if (obj instanceof YxxfDictionary) {
                final YxxfDictionary dic = (YxxfDictionary)obj;
                final String featureTypeHandle = dic.nameSoftOwnerIdMap.get("FeatureType");

                if (featureTypeHandle != null) {
                    final YxxfObject geomType = buffer.currObjectsBlock.getObjectByHandle(featureTypeHandle);

                    if (geomType instanceof YxxfXRecord) {
                        final YxxfXRecord rec = (YxxfXRecord)geomType;

                        return rec.values.get(1);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Determines the data type by the esri code (see esri Mapping_Specification_for_DWG).
     *
     * @param   esriCode  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getTypeByEsriCode(final Integer esriCode) {
        if (esriCode == 1) {
            return String.valueOf(Types.VARCHAR);
        } else if ((esriCode == 70) || (esriCode == 90)) {
            // 70 = int16, 90 = int32
            return String.valueOf(Types.INTEGER);
        } else if (esriCode == 40) {
            // 40 = real
            return String.valueOf(Types.DOUBLE);
        } else {
            return String.valueOf(Types.VARCHAR);
        }
    }

    /**
     * Determines the correct db type by the esri code and converts the given value to this type.
     *
     * @param   value     the value to convert
     * @param   esriCode  the esri data type code
     *
     * @return  the converted value
     */
    private Object getConvertToDbType(final String value, final Integer esriCode) {
        if ((esriCode == 70) || (esriCode == 90)) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                LOG.warn("Cannot convert " + value + " to integer");
                return null;
            }
        } else if (esriCode == 40) {
            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                LOG.warn("Cannot convert " + value + " to double");
                return null;
            }
        } else {
            // one byte was converted to one char. This is not corect for umlaute. So convert it back to an byte array
            // and then parse the hole string.
            final byte[] asByte = new byte[value.length()];

            for (int i = 0; i < value.length(); ++i) {
                asByte[i] = (byte)value.charAt(i);
            }

            return new String(asByte);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   buffer  DOCUMENT ME!
     * @param   header  DOCUMENT ME!
     * @param   name    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getDictionaryHandle(final YdxfGetBuffer buffer, final YxxfEntHeader header, final String name) {
        final List<YxxfObject> objects = buffer.currObjectsBlock.getObjectsOfEntity(header);

        for (final YxxfObject obj : objects) {
            if (obj instanceof YxxfDictionary) {
                final YxxfDictionary dic = (YxxfDictionary)obj;
                final String handle = dic.nameSoftOwnerIdMap.get(name);

                if (handle != null) {
                    return handle;
                }
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   buffer  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getCrs(final YdxfGetBuffer buffer) {
        final List<YxxfObject> objects = buffer.currObjectsBlock.getObjectsWichPointTo0();
        String crsDefinition = null;

        for (final YxxfObject obj : objects) {
            if (obj instanceof YxxfDictionary) {
                final YxxfDictionary dic = (YxxfDictionary)obj;
                final String prjPnt = dic.nameSoftOwnerIdMap.get("ESRI_PRJ");

                if (prjPnt != null) {
                    final YxxfObject prjObj = buffer.currObjectsBlock.getObjectByHandle(prjPnt);

                    if (prjObj instanceof YxxfXRecord) {
                        final YxxfXRecord rec = (YxxfXRecord)prjObj;

                        crsDefinition = rec.values.get(1);
                    }
                }
            }
        }

        return determineCrs(crsDefinition);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  buffer  DOCUMENT ME!
     */
    private void printHoleDictionary(final YdxfGetBuffer buffer) {
        final List<YxxfObject> objects = buffer.currObjectsBlock.getObjectsWichPointTo0();

        for (final YxxfObject obj : objects) {
            if (obj instanceof YxxfDictionary) {
                final YxxfDictionary dic = (YxxfDictionary)obj;

                printDictionary(buffer, dic);
//                for (final String key : dic.nameSoftOwnerIdMap.keySet()) {
//                    final String handle = dic.nameSoftOwnerIdMap.get(key);
//                    final YxxfObject prjObj = buffer.currObjectsBlock.getObjectByHandle(handle);
//                    System.out.println("key: " + key);
//
//                    if (prjObj instanceof YxxfXRecord) {
//                        final YxxfXRecord rec = (YxxfXRecord)prjObj;
//
//                        for (final Integer intKey : rec.values.keySet()) {
//                            System.out.println(intKey + ": " + rec.values.get(intKey));
//                        }
//                    } else {
//                        System.out.println("class: " + ((prjObj == null) ? "null" : prjObj.getClass().getName()));
//                    }
//                }
            } else {
                System.out.println("class: " + obj.getClass().getName());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  buffer  DOCUMENT ME!
     * @param  dic     DOCUMENT ME!
     */
    private void printDictionary(final YdxfGetBuffer buffer, final YxxfDictionary dic) {
        for (final String key : dic.nameSoftOwnerIdMap.keySet()) {
            final String handle = dic.nameSoftOwnerIdMap.get(key);
            final YxxfObject prjObj = buffer.currObjectsBlock.getObjectByHandle(handle);
            System.out.println("key: " + key);

            if (prjObj instanceof YxxfXRecord) {
                final YxxfXRecord rec = (YxxfXRecord)prjObj;

                for (final Integer intKey : rec.values.keySet()) {
                    System.out.println(intKey + ": " + rec.values.get(intKey));
                }
            } else if (prjObj instanceof YxxfDictionary) {
                printDictionary(buffer, (YxxfDictionary)prjObj);
            } else {
                System.out.println("class: " + ((prjObj == null) ? "null" : prjObj.getClass().getName()));
            }
        }
    }

    /**
     * Determines the crs of the corresponding prj file.
     *
     * @param   crsDefinition  DOCUMENT ME!
     *
     * @return  the crs of the corresponding prj file or null, if the initialisation of the shape file should be
     *          cancelled.
     */
    private String determineCrs(final String crsDefinition) {
        if (crsDefinition != null) {
            final String epsg = CrsDeterminer.getEpsgCode(crsDefinition);

            if (epsg != null) {
                return epsg;
            }
        } else {
            LOG.warn("No crs found. Assume that the default crs is used");
        }

        return CismapBroker.getInstance().getSrs().getCode();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  objects  DOCUMENT ME!
     */
    private void printObjects(final List<YxxfObject> objects) {
        for (final YxxfObject o : objects) {
            System.out.println(o.toString());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   line               DOCUMENT ME!
     * @param   factory            DOCUMENT ME!
     * @param   originFeatureType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Geometry readPolyLine(final YxxfEntPolyline line,
            final GeometryFactory factory,
            final String originFeatureType) {
        final List<YxxfEntVertex> vertexList = (List<YxxfEntVertex>)line.vtxEntities;
        final Coordinate[] coordArray = new Coordinate[vertexList.size()];
        int index = -1;

        for (final YxxfEntVertex vertex : vertexList) {
            final Coordinate coord = new Coordinate(vertex.pnt.x, vertex.pnt.y, vertex.pnt.z);
            coordArray[++index] = coord;
        }

        if ((originFeatureType != null) && originFeatureType.toLowerCase().equals("polygon")) {
            return factory.createPolygon(coordArray);
        } else {
            return factory.createLineString(coordArray);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geom        DOCUMENT ME!
     * @param   attributes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private FeatureServiceFeature toFeature(final Geometry geom, final Map<String, Object> attributes) {
        Object id = attributes.get("FID");

        if (!(id instanceof Integer)) {
            id = attributes.get("id");

            if (!(id instanceof Integer)) {
                id = generateId();
            }
        }

        final DefaultFeatureServiceFeature feature = new DefaultFeatureServiceFeature((Integer)id, geom, properties);
        feature.addProperties(attributes);
        feature.addProperty("geom", geom);

        return feature;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Integer generateId() {
        return ++lastGeneratedId;
    }
}
