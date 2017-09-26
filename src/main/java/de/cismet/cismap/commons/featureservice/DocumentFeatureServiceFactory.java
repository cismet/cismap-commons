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
package de.cismet.cismap.commons.featureservice;

import org.deegree.io.shpapi.FileHeader;

import java.awt.Component;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.RandomAccessFile;

import java.security.MessageDigest;

import java.util.List;
import java.util.StringTokenizer;

import de.cismet.cismap.commons.exceptions.FileExtensionContentMissmatchException;
import de.cismet.cismap.commons.exceptions.UnknownDocumentException;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.factory.DxfReader;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.LayerCollection;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   spuhl
 * @version  $Revision$, $Date$
 */
public class DocumentFeatureServiceFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            DocumentFeatureServiceFactory.class);
    public static final String XML_FILE_EXTENSION = ".xml";
    public static final String GML_FILE_EXTENSION = ".gml";
    public static final String SHP_FILE_EXTENSION = ".shp";
    public static final String CSV_FILE_EXTENSION = ".csv";
    public static final String DXF_FILE_EXTENSION = ".dxf";
    public static final String SHP_DBF_FILE_EXTENSION = ".dbf";
    public static final String SHP_INDEX_FILE_EXTENSION = ".shx";
    public static final String XML_IDENTIFICATION_STRING = "<?xml";
    public static final String GML_IDENTIFICATION_STRING = "xmlns:gml";

    //~ Methods ----------------------------------------------------------------

// public static DocumentFeatureService createDocumentFeatureService(File documentFile) throws Exception {
// if (documentFile != null) {
// return createDocumentFeatureService(documentFile);
// } else {
// log.error("URI ist null es kann kein FeatureService angelegt werden");
// throw new Exception("Pfad ist ungültig es kann kein FeatureService angelegt werden");
// }
// }
//
// public static DocumentFeatureService createDocumentFeatureService(Element element) throws Exception {
//
//
// //File test = new File(new URI(layerConf.getChildText("documentURI").trim()));
//
// if (layerConf != null) {
// return createDocumentFeatureService(element);
// } else {
// log.error("URI ist null es kann kein FeatureService angelegt werden");
// throw new Exception("Pfad ist ungültig es kann kein FeatureService angelegt werden");
// }
// }
// try {
// if (test.getName().endsWith(XML_FILE_EXTENSION) || test.getName().endsWith(GML_FILE_EXTENSION)) {
// log.debug("File extension ist xml/gml");
// if (isGMLDocument(test)) {
// return new GMLFeatureService(layerConf);
// } else {
// throw new FileExtensionContentMissmatchException("File extension ist xml/gml aber kein gültiges xml/gml Dokument");
//                    }
//                } else if (test.getPath().endsWith(SHP_FILE_EXTENSION)) {
//                    log.debug("File extension ist shp");
//                    if (isShapeFile(test)) {
//                        return new ShapeFileFeatureService(layerConf);
//                    } else {
//                        throw new FileExtensionContentMissmatchException("File extension ist shp aber kein gültiges shp Dokument");
//                    }
//                } else {
//                    throw new UnknownDocumentException("Endung des Dokumentes ist nicht bekannt");
//                }
//            } catch (Exception ex) {
//                if (ex instanceof UnknownDocumentException || ex instanceof FileExtensionContentMissmatchException) {
//                    log.error("Fehler beim erstellen eines DocumentFeaturelayers anhand eines Dokumentes --> versuche Inhalt automatisch zu bestimmen", ex);
//                    if (isGMLDocument(test)) {
//                        return new GMLFeatureService(layerConf);
//                    } else if (isShapeFile(test)) {
//                        return new ShapeFileFeatureService(layerConf);
//                    } else {
//                        throw new Exception("Inhalt des Dokumentes ist nicht bekannt und kann nicht verarbeitet werden");
//                    }
//                } else {
//                    log.error("Fehler beim anlegen eines DocumentFeatureServices", ex);
//                    throw ex;
//                }
//            }
    /**
     * Creates a new DocumentFeatureService depending on the type of the delivered object.
     *
     * @param   documentFile  configurationObject file or JDOM-element (at the moment)
     *
     * @return  a new DocumentFeatureService
     *
     * @throws  Exception  java.lang.Exception
     */
    public static AbstractFeatureService createDocumentFeatureService(final File documentFile) throws Exception {
        if (documentFile == null) {
            log.error("URI ist null es kann kein FeatureService angelegt werden");
            throw new Exception("Pfad ist ungültig es kann kein FeatureService angelegt werden");
        }

        final long documentSize = documentFile.length();

        try {
            if (documentFile.getName().endsWith(XML_FILE_EXTENSION)
                        || documentFile.getName().endsWith(GML_FILE_EXTENSION)) {
                if (log.isDebugEnabled()) {
                    log.debug("File extension ist xml/gml");
                }
                if (isGMLDocument(documentFile)) {
                    return new GMLFeatureService(documentFile.getName(), documentFile.toURI(), documentSize, null);
//          if (xmlConfig != null)
//          {
//            return new GMLFeatureService(xmlConfig);
//          } else
//          {
//            return new GMLFeatureService(documentFile.getName(), documentFile.toURI(), null);
//          }
                } else {
                    throw new FileExtensionContentMissmatchException(
                        "File extension ist xml/gml aber kein gültiges xml/gml Dokument");
                }
            } else if (documentFile.getPath().endsWith(SHP_FILE_EXTENSION)
                        || documentFile.getPath().endsWith(SHP_DBF_FILE_EXTENSION)
                        || documentFile.getPath().endsWith(CSV_FILE_EXTENSION)) {
                if (log.isDebugEnabled()) {
                    log.debug("File extension ist shp/dbf/csv");
                }
                if (((documentFile.getPath().endsWith(SHP_DBF_FILE_EXTENSION)
                                    || documentFile.getPath().endsWith(CSV_FILE_EXTENSION))
                                && CismapBroker.getInstance().isUseInternalDb()) || isShapeFile(documentFile)) {
                    // dbf and csv will only be supported, if the internal db is used
                    if (CismapBroker.getInstance().isUseInternalDb()) {
                        final String hexString = calcMd5FromFile(documentFile);

                        String fileName = documentFile.getName();
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));
                        final String tableName = fileName + "_" + hexString;
                        return new H2FeatureService(
                                fileName,
                                H2FeatureServiceFactory.DB_NAME,
                                tableName,
                                null,
                                documentFile);
                    } else {
                        return new ShapeFileFeatureService(documentFile.getName(),
                                documentFile.toURI(),
                                documentSize,
                                null);
                    }

//          if (xmlConfig != null)
//          {
//            return new ShapeFileFeatureService(xmlConfig);
//          } else
//          {
//            return new ShapeFileFeatureService(documentFile.getName(), documentFile.toURI(), null);
//          }
                } else {
                    throw new FileExtensionContentMissmatchException(
                        "File extension ist shp aber kein gültiges shp Dokument");
                }
            } else if (documentFile.getPath().toLowerCase().endsWith(DXF_FILE_EXTENSION)) {
                final DxfReader reader = new DxfReader(documentFile.getPath());
                final List<FeatureServiceAttribute> attributes = reader.getFeatureServiceAttributes();
                final String hexString = calcMd5FromFile(documentFile);
                String fileName = documentFile.getName();
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                final String folderName = fileName;

                final List<FeatureServiceFeature> pointFeatures = reader.getPointFeatures();
                final List<FeatureServiceFeature> linestringFeatures = reader.getLinestringFeatures();
                final List<FeatureServiceFeature> polygonFeatures = reader.getPolygonFeatures();
                final List<FeatureServiceFeature> annotationFeatures = reader.getAnnotationFeatures();

                if ((pointFeatures != null) && !pointFeatures.isEmpty()) {
                    final String serviceName = fileName + "_point";
                    final String tableName = folderName + "->" + fileName + "_point" + "_" + hexString;
                    final H2FeatureService service = new H2FeatureService(
                            serviceName,
                            H2FeatureServiceFactory.DB_NAME,
                            tableName,
                            attributes,
                            pointFeatures);
                    service.initAndWait();
                    showService(service, folderName);
                }
                if ((linestringFeatures != null) && !linestringFeatures.isEmpty()) {
                    final String serviceName = fileName + "_linestring";
                    final String tableName = folderName + "->" + fileName + "_linestring" + "_" + hexString;
                    final H2FeatureService service = new H2FeatureService(
                            serviceName,
                            H2FeatureServiceFactory.DB_NAME,
                            tableName,
                            attributes,
                            linestringFeatures);
                    service.initAndWait();
                    showService(service, folderName);
                }
                if ((polygonFeatures != null) && !polygonFeatures.isEmpty()) {
                    final String serviceName = fileName + "_polygon";
                    final String tableName = folderName + "->" + fileName + "_polygon" + "_" + hexString;
                    final H2FeatureService service = new H2FeatureService(
                            serviceName,
                            H2FeatureServiceFactory.DB_NAME,
                            tableName,
                            attributes,
                            polygonFeatures);
                    service.initAndWait();
                    showService(service, folderName);
                }

                if ((annotationFeatures != null) && !annotationFeatures.isEmpty()) {
                    final String serviceName = fileName + "_annotation";
                    final String tableName = folderName + "->" + fileName + "_annotation" + "_" + hexString;
                    final H2FeatureService service = new H2FeatureService(
                            serviceName,
                            H2FeatureServiceFactory.DB_NAME,
                            tableName,
                            attributes,
                            annotationFeatures);
                    service.initAndWait();
                    showService(service, folderName);
                }

                throw new LayerAlreadyAddedException();
            } else {
                throw new UnknownDocumentException("Endung des Dokumentes ist nicht bekannt");
            }
        } catch (Exception ex) {
            if ((ex instanceof UnknownDocumentException) || (ex instanceof FileExtensionContentMissmatchException)) {
                log.error(
                    "Fehler beim erstellen eines DocumentFeaturelayers anhand eines Dokumentes --> versuche Inhalt automatisch zu bestimmen",
                    ex);
                if (isGMLDocument(documentFile)) {
                    return new GMLFeatureService(documentFile.getName(), documentFile.toURI(), documentSize, null);
//          if (xmlConfig != null)
//          {
//            return new GMLFeatureService(xmlConfig);
//          } else
//          {
//            return new GMLFeatureService(documentFile.getName(), documentFile.toURI(), null);
//          }
                } else if (isShapeFile(documentFile)) {
                    return new ShapeFileFeatureService(documentFile.getName(),
                            documentFile.toURI(),
                            documentSize,
                            null);
//          if (xmlConfig != null)
//          {
//            return new ShapeFileFeatureService(xmlConfig);
//          } else
//          {
//            return new ShapeFileFeatureService(documentFile.getName(), documentFile.toURI(), null);
//          }
                } else {
                    throw new Exception("Inhalt des Dokumentes ist nicht bekannt und kann nicht verarbeitet werden");
                }
            } else {
                log.error("Fehler beim anlegen eines DocumentFeatureServices", ex);
                throw ex;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   documentFile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static String calcMd5FromFile(final File documentFile) throws Exception {
        final MessageDigest md5 = MessageDigest.getInstance("MD5");
        final BufferedInputStream is = new BufferedInputStream(new FileInputStream(documentFile));
        final byte[] inputArray = new byte[256];
        int byteCount = 0;

        while ((byteCount = is.read(inputArray)) != -1) {
            md5.update(inputArray, 0, byteCount);
        }
        final byte[] hashValue = md5.digest();
        final StringBuffer hexString = new StringBuffer();

        for (final byte b : hashValue) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }

    /**
     * Shows the service in the ThemeLayerWidget and refreshs the Capabilities tab of the internal db.
     *
     * @param  service  DOCUMENT ME!
     * @param  folder   DOCUMENT ME!
     */
    public static void showService(final H2FeatureService service, final String folder) {
        final ActiveLayerModel model = (ActiveLayerModel)CismapBroker.getInstance().getMappingComponent()
                    .getMappingModel();
        LayerCollection layerCollection = null;

        if (folder != null) {
            final String folderWithDem = folder.replaceAll("->", "\\");
            final StringTokenizer st = new StringTokenizer(folderWithDem, "\\");

            while (st.hasMoreTokens()) {
                final String subFolder = st.nextToken();

                if (layerCollection != null) {
                    boolean found = false;

                    for (int i = 0; i < layerCollection.size(); ++i) {
                        final Object tmp = layerCollection.get(i);

                        if ((tmp instanceof LayerCollection) && ((LayerCollection)tmp).getName().equals(subFolder)) {
                            layerCollection = (LayerCollection)tmp;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        final LayerCollection newLayerCollection = new LayerCollection();
                        newLayerCollection.setName(subFolder);
                        layerCollection.add(newLayerCollection);
                        layerCollection = newLayerCollection;
                    }
                } else {
                    boolean found = false;

                    for (int i = 0; i < model.getChildCount(model.getRoot()); ++i) {
                        final Object tmp = model.getChild(model.getRoot(), i);

                        if ((tmp instanceof LayerCollection) && ((LayerCollection)tmp).getName().equals(subFolder)) {
                            layerCollection = (LayerCollection)tmp;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        layerCollection = new LayerCollection();
                        layerCollection.setName(subFolder);
                        model.addEmptyLayerCollection(layerCollection);
                    }
                }
            }
        }

        if (layerCollection != null) {
            model.registerRetrievalServiceLayer(service);
            layerCollection.add(service);
        } else {
            model.addLayer(service);
        }
    }

    /**
     * Checks if the delivered File-object is a GML-file. Currently only checks if the first 100 lines contain
     * "xmlns:gml".
     *
     * @param   documentFile  File-object to test
     *
     * @return  true if the document is a GML-file, else false
     *
     * @throws  Exception  java.lang.Exception
     */
    // TODO Primitiv check --> sollte geändert werden
    public static boolean isGMLDocument(final File documentFile) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Prüfe ob Document ein GML Dokument ist");
        }
        if (documentFile != null) {
            final BufferedReader bf = new BufferedReader(new FileReader(documentFile));
            String currentLine = null;
            final int counter = 1;
            while (((currentLine = bf.readLine()) != null) || (counter < 100)) {
                if (counter == 2) {
                    if (log.isDebugEnabled()) {
                        log.debug("Erste Zeile des Dokuments: ");
                    }
                    if (!currentLine.startsWith(XML_IDENTIFICATION_STRING)) {
                        log.info("XML File fängt nicht mit " + XML_IDENTIFICATION_STRING + " an.");
                        return false;
                    }
                }
                if (currentLine.contains(GML_IDENTIFICATION_STRING)) {
                    return true;
                }
            }
            log.info("Im ganzen Dokument konnte keine Zeile mit: " + GML_IDENTIFICATION_STRING + " gefunden werden");
            return false;
        } else {
            log.warn("Achtung documentFile war null");
            return false;
        }
    }

    /**
     * Checks if the delivered File-object is an ESRI-Shapefile.
     *
     * @param   documentFile  File-object to test
     *
     * @return  true if the document is a shapefile, else false
     */
    public static boolean isShapeFile(final File documentFile) {
        if (log.isDebugEnabled()) {
            log.debug("Prüfe ob Document ein ShapeFile ist");
        }
        try {
            if (documentFile != null) {
                final RandomAccessFile raf = new RandomAccessFile(documentFile, "r");
                final FileHeader fh = new FileHeader(raf);
                return true;
            } else {
                log.warn("Achtung documentFile war null");
                return false;
            }
        } catch (Exception ex) {
            log.warn("Document ist wahrscheinlich kein Shapefile");
            return false;
        }
    }
}
