/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.commons.raster.tms.tmscapability;

import de.cismet.cismap.commons.BoundingBox;
import java.util.ArrayList;
import org.deegree.services.wms.capabilities.WMSCapabilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author cschmidt
 */
public class TMSCapabilitiesFactory {

    private final org.apache.log4j.Logger log =
            org.apache.log4j.Logger.getLogger(this.getClass());

    public static TMSCapabilities parseTMSCapabilities(WMSCapabilities caps) {

        final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TMSCapabilitiesFactory.class);
        String srs = "";//NOI18N
        String layers = "";//NOI18N
        String format = "";//NOI18N
        String style = "";//NOI18N
        String host = "";//NOI18N
        String capVersion = "";//NOI18N
        double minx, miny, maxx, maxy;
        BoundingBox bb = new BoundingBox();
        ArrayList<TileSet> list = new ArrayList<TileSet>();
        Double[] resolutions = null;
        int width = 0, height = 0;

        capVersion = caps.getVersion();
        host = caps.getService().getOnlineResource().toString();
        if (host.endsWith("?")) {//NOI18N
            host = host.substring(0, host.length() - 1);
        }


        Document doc = caps.getCapability().getVendorSpecificCapabilities();
        NodeList tileSetNodeList = doc.getDocumentElement().getElementsByTagName("TileSet");//NOI18N

        for (int i = 0; i < tileSetNodeList.getLength(); i++) {
            Node tileSetNode = tileSetNodeList.item(i);
            NodeList tileSetChilds = tileSetNode.getChildNodes();
            for (int j = 0; j < tileSetChilds.getLength(); j++) {
                final Node childNode = tileSetChilds.item(j);
                if (childNode.getNodeName().equalsIgnoreCase("SRS")) {//NOI18N
                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        srs = childNode.getFirstChild().getNodeValue();
                        continue;
                    }
                } else if (childNode.getNodeName().equalsIgnoreCase("BoundingBox")) {//NOI18N
                    minx = Double.parseDouble(childNode.getAttributes().getNamedItem("minx").getNodeValue());//NOI18N
                    miny = Double.parseDouble(childNode.getAttributes().getNamedItem("miny").getNodeValue());//NOI18N
                    maxx = Double.parseDouble(childNode.getAttributes().getNamedItem("maxx").getNodeValue());//NOI18N
                    maxy = Double.parseDouble(childNode.getAttributes().getNamedItem("maxy").getNodeValue());//NOI18N
                    bb = new BoundingBox(minx, miny, maxx, maxy);
                    continue;

                } else if (childNode.getNodeName().equalsIgnoreCase("resolutions")) {//NOI18N
                    String res = childNode.getFirstChild().getNodeValue();
                    String[] resolutionStrings = res.split(" ");//NOI18N
                    resolutions = new Double[resolutionStrings.length];
                    for (int currentPos = 0; currentPos < resolutionStrings.length; currentPos++) {
                        resolutions[currentPos] = Double.parseDouble(resolutionStrings[currentPos]);
                    }
                } else if (childNode.getNodeName().equalsIgnoreCase("width")) {//NOI18N
                    width = Integer.parseInt(childNode.getFirstChild().getNodeValue());

                } else if (childNode.getNodeName().equalsIgnoreCase("height")) {//NOI18N
                    height = Integer.parseInt(childNode.getFirstChild().getNodeValue());
                } else if (childNode.getNodeName().equalsIgnoreCase("format")) {//NOI18N
                    format = childNode.getFirstChild().getNodeValue();
                    if (format.contains("image/")) {//NOI18N
                        format = format.substring(format.indexOf("/") + 1);//NOI18N
                    }
                } else if (childNode.getNodeName().equalsIgnoreCase("layers")) {//NOI18N
                    layers = childNode.getFirstChild().getNodeValue();
                } else if (childNode.getNodeName().equalsIgnoreCase("styles")) {//NOI18N
                    if (childNode.getFirstChild() != null) {
                        style = childNode.getFirstChild().getNodeValue();
                    }
                } else {
                }

            }
            TileSet t = new TileSet(capVersion, host, srs, bb, resolutions, width, height, format, layers, style);

            list.add(t);
        }
        return new TMSCapabilities(list, caps);
    }
}
