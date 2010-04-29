/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.util;

import java.awt.datatransfer.DataFlavor;

/**
 *
 * @author spuhl
 */
public class DnDUtils {

    
    //TODO Best position for this code snippet ?
    public static DataFlavor URI_LIST_FLAVOR;
    static {
        try {
            URI_LIST_FLAVOR = new DataFlavor("text/uri-list;class=java.lang.String");//NOI18N
        } catch (ClassNotFoundException e) { // can't happen
            e.printStackTrace();
        }
    }
    
    public static java.util.List textURIListToFileList(String data) {
        java.util.List list = new java.util.ArrayList(1);
        for (java.util.StringTokenizer st = new java.util.StringTokenizer(data, "\r\n");//NOI18N
                st.hasMoreTokens();) {
            String s = st.nextToken();
            if (s.startsWith("#")) {//NOI18N
                // the line is a comment (as per the RFC 2483)
                continue;
            }
            try {
                java.net.URI uri = new java.net.URI(s);
                java.io.File file = new java.io.File(uri);
                list.add(file);
            } catch (java.net.URISyntaxException e) {
                // malformed URI
            } catch (IllegalArgumentException e) {
                // the URI is not a valid 'file:' URI
            }
        }
        return list;
    }
    
}
