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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;

import de.cismet.tools.gui.downloadmanager.AbstractCancellableDownload;
import de.cismet.tools.gui.downloadmanager.Download;

/**
 * Every ExportDownload class needs a public constructor without arguments.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class ExportDownload extends AbstractCancellableDownload {

    //~ Instance fields --------------------------------------------------------

    protected FeatureServiceFeature[] features;
    protected AbstractFeatureService service;
    protected List<String[]> aliasAttributeList;
    protected String extension;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  filename            DOCUMENT ME!
     * @param  extension           DOCUMENT ME!
     * @param  features            DOCUMENT ME!
     * @param  service             DOCUMENT ME!
     * @param  aliasAttributeList  attributeNames A list with string arrays. Every array should have 2 elements. The
     *                             first element is the alias of the column and the second element is the name of the
     *                             attribute, that should be shown in the column
     */
    public void init(final String filename,
            final String extension,
            final FeatureServiceFeature[] features,
            final AbstractFeatureService service,
            final List<String[]> aliasAttributeList) {
        this.features = features;
        this.service = service;
        this.aliasAttributeList = aliasAttributeList;
        this.title = "Export " + features.length + " Features";
        this.extension = extension;

        if (aliasAttributeList == null) {
            if ((features != null) && (features.length > 0)) {
                this.aliasAttributeList = getAttributeNames(features[0]);
            }
        }

        status = Download.State.WAITING;
        String filenameWithoutExt = filename;

        if (filename.charAt(filename.length() - 4) == '.') {
            filenameWithoutExt = filename.substring(0, filename.length() - 4);
        }
        determineDestinationFile(filenameWithoutExt, extension);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract String getDefaultExtension();

    /**
     * DOCUMENT ME!
     *
     * @param   f  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<String[]> getAttributeNames(final FeatureServiceFeature f) {
        final List<String[]> attrNames = new ArrayList<String[]>();
        final Map<String, Object> hm = (Map<String, Object>)f.getProperties();

        for (final String attrName : hm.keySet()) {
            final String[] aliasName = new String[2];
            aliasName[0] = aliasName[1] = attrName;
            attrNames.add(aliasName);
        }

        return attrNames;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aliasAttributeList  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected List<String> toAliasList(final List<String[]> aliasAttributeList) {
        return toAliasOrAttributeList(aliasAttributeList, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aliasAttributeList  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected List<String> toAttributeList(final List<String[]> aliasAttributeList) {
        return toAliasOrAttributeList(aliasAttributeList, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aliasAttributeList  DOCUMENT ME!
     * @param   aliasList           DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<String> toAliasOrAttributeList(final List<String[]> aliasAttributeList, final boolean aliasList) {
        final List<String> attrList = new ArrayList<String>(aliasAttributeList.size());

        for (final String[] aliasAttr : aliasAttributeList) {
            attrList.add((aliasList ? aliasAttr[0] : aliasAttr[1]));
        }

        return attrList;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Download) {
            if ((fileToSaveTo == null) && (((Download)obj).getFileToSaveTo() == null)) {
                return (obj.getClass().getName().equals(this.getClass().getName()));
            }
        }

        return super.equals(obj);
    }
}
