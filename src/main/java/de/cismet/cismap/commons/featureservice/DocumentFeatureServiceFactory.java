/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.featureservice;

import de.cismet.cismap.commons.exceptions.UnknownDocumentException;
import de.cismet.cismap.commons.exceptions.FileExtensionContentMissmatchException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import org.deegree.io.shpapi.FileHeader;

/**
 *
 * @author spuhl
 */
public class DocumentFeatureServiceFactory
{
  private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DocumentFeatureServiceFactory.class);
  public static final String XML_FILE_EXTENSION = ".xml";
  public static final String GML_FILE_EXTENSION = ".gml";
  public static final String SHP_FILE_EXTENSION = ".shp";
  public static final String SHP_DBF_FILE_EXTENSION = ".dbf";
  public static final String SHP_INDEX_FILE_EXTENSION = ".shx";
  public static final String XML_IDENTIFICATION_STRING = "<?xml";
  public static final String GML_IDENTIFICATION_STRING = "xmlns:gml";

//    public static DocumentFeatureService createDocumentFeatureService(File documentFile) throws Exception {
//        if (documentFile != null) {
//            return createDocumentFeatureService(documentFile);
//        } else {
//            log.error("URI ist null es kann kein FeatureService angelegt werden");
//            throw new Exception("Pfad ist ungültig es kann kein FeatureService angelegt werden");
//        }
//    }
//
//    public static DocumentFeatureService createDocumentFeatureService(Element element) throws Exception {
//
//
//        //File test = new File(new URI(layerConf.getChildText("documentURI").trim()));
//
//        if (layerConf != null) {
//            return createDocumentFeatureService(element);
//        } else {
//            log.error("URI ist null es kann kein FeatureService angelegt werden");
//            throw new Exception("Pfad ist ungültig es kann kein FeatureService angelegt werden");
//        }
//    }
//     try {
//                if (test.getName().endsWith(XML_FILE_EXTENSION) || test.getName().endsWith(GML_FILE_EXTENSION)) {
//                    log.debug("File extension ist xml/gml");
//                    if (isGMLDocument(test)) {
//                        return new GMLFeatureService(layerConf);
//                    } else {
//                        throw new FileExtensionContentMissmatchException("File extension ist xml/gml aber kein gültiges xml/gml Dokument");
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
   * @param configurationObject file or JDOM-element (at the moment)
   * @return a new DocumentFeatureService
   * @throws java.lang.Exception
   */
  public static DocumentFeatureService createDocumentFeatureService(File documentFile) throws Exception
  {
//    Element xmlConfig = null;
//    if (configurationObject instanceof Element)
//    {
//      xmlConfig = (Element) configurationObject;
//      if (!xmlConfig.getName().contains("FeatureServiceLayer"))
//      {
//        xmlConfig = xmlConfig.getChild("DocumentFeatureServiceLayer");
//      }
//      documentFile = new File(new URI(xmlConfig.getChildText("documentURI").trim()));
//    } else if (configurationObject instanceof File)
//    {
//      documentFile = (File) configurationObject;
//    } else
//    {
//      log.error("Konfigurationsobjekt nicht bekannt oder null " + configurationObject);
//      throw new Exception("Konfigurationsobjekt nicht bekannt oder null");
//    }

    if (documentFile == null)
    {
      log.error("URI ist null es kann kein FeatureService angelegt werden");
      throw new Exception("Pfad ist ungültig es kann kein FeatureService angelegt werden");
    }

    long documentSize = documentFile.length();

    try
    {
      if (documentFile.getName().endsWith(XML_FILE_EXTENSION) || documentFile.getName().endsWith(GML_FILE_EXTENSION))
      {
        log.debug("File extension ist xml/gml");
        if (isGMLDocument(documentFile))
        {
          return new GMLFeatureService(documentFile.getName(), documentFile.toURI(), documentSize, null);
//          if (xmlConfig != null)
//          {
//            return new GMLFeatureService(xmlConfig);
//          } else
//          {
//            return new GMLFeatureService(documentFile.getName(), documentFile.toURI(), null);
//          }
        } else
        {
          throw new FileExtensionContentMissmatchException("File extension ist xml/gml aber kein gültiges xml/gml Dokument");
        }
      } else if (documentFile.getPath().endsWith(SHP_FILE_EXTENSION))
      {
        log.debug("File extension ist shp");
        if (isShapeFile(documentFile))
        {
          return new ShapeFileFeatureService(documentFile.getName(), documentFile.toURI(), documentSize, null);

//          if (xmlConfig != null)
//          {
//            return new ShapeFileFeatureService(xmlConfig);
//          } else
//          {
//            return new ShapeFileFeatureService(documentFile.getName(), documentFile.toURI(), null);
//          }
        } else
        {
          throw new FileExtensionContentMissmatchException("File extension ist shp aber kein gültiges shp Dokument");
        }
      } else
      {
        throw new UnknownDocumentException("Endung des Dokumentes ist nicht bekannt");
      }
    } catch (Exception ex)
    {
      if (ex instanceof UnknownDocumentException || ex instanceof FileExtensionContentMissmatchException)
      {
        log.error("Fehler beim erstellen eines DocumentFeaturelayers anhand eines Dokumentes --> versuche Inhalt automatisch zu bestimmen", ex);
        if (isGMLDocument(documentFile))
        {
          return new GMLFeatureService(documentFile.getName(), documentFile.toURI(), documentSize,  null);
//          if (xmlConfig != null)
//          {
//            return new GMLFeatureService(xmlConfig);
//          } else
//          {
//            return new GMLFeatureService(documentFile.getName(), documentFile.toURI(), null);
//          }
        } else if (isShapeFile(documentFile))
        {
          return new ShapeFileFeatureService(documentFile.getName(), documentFile.toURI(), documentSize, null);
//          if (xmlConfig != null)
//          {
//            return new ShapeFileFeatureService(xmlConfig);
//          } else
//          {
//            return new ShapeFileFeatureService(documentFile.getName(), documentFile.toURI(), null);
//          }
        } else
        {
          throw new Exception("Inhalt des Dokumentes ist nicht bekannt und kann nicht verarbeitet werden");
        }
      } else
      {
        log.error("Fehler beim anlegen eines DocumentFeatureServices", ex);
        throw ex;
      }
    }
  }

  /**
   * Checks if the delivered File-object is a GML-file.
   * Currently only checks if the first 100 lines contain "xmlns:gml".
   * @param documentFile File-object to test
   * @return true if the document is a GML-file, else false
   * @throws java.lang.Exception
   */
  //TODO Primitiv check --> sollte geändert werden
  public static boolean isGMLDocument(File documentFile) throws Exception
  {
    log.debug("Prüfe ob Document ein GML Dokument ist");
    if (documentFile != null)
    {
      BufferedReader bf = new BufferedReader(new FileReader(documentFile));
      String currentLine = null;
      int counter = 1;
      while ((currentLine = bf.readLine()) != null || counter < 100)
      {
        if (counter == 2)
        {
          log.debug("Erste Zeile des Dokuments: ");
          if (!currentLine.startsWith(XML_IDENTIFICATION_STRING))
          {
            log.info("XML File fängt nicht mit " + XML_IDENTIFICATION_STRING + " an.");
            return false;
          }
        }
        if (currentLine.contains(GML_IDENTIFICATION_STRING))
        {
          return true;
        }
      }
      log.info("Im ganzen Dokument konnte keine Zeile mit: " + GML_IDENTIFICATION_STRING + " gefunden werden");
      return false;
    } else
    {
      log.warn("Achtung documentFile war null");
      return false;
    }
  }

  /**
   * Checks if the delivered File-object is an ESRI-Shapefile.
   * @param documentFile File-object to test
   * @return true if the document is a shapefile, else false
   */
  public static boolean isShapeFile(File documentFile)
  {
    log.debug("Prüfe ob Document ein ShapeFile ist");
    try
    {
      if (documentFile != null)
      {
        RandomAccessFile raf = new RandomAccessFile(documentFile, "r");
        FileHeader fh = new FileHeader(raf);
        return true;
      } else
      {
        log.warn("Achtung documentFile war null");
        return false;
      }
    } catch (Exception ex)
    {
      log.warn("Document ist wahrscheinlich kein Shapefile");
      return false;
    }
  }
}
