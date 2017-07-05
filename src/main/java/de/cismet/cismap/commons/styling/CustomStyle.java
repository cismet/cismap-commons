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
package de.cismet.cismap.commons.styling;

import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.feature.Feature;
import org.deegree.filter.XPathEvaluator;
import org.deegree.geometry.Geometry;
import org.deegree.style.se.unevaluated.Continuation;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.style.se.unevaluated.Symbolizer;
import org.deegree.style.styling.Styling;

import java.io.File;

import java.net.URL;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * Wraps a Style object. But it can be used as the Style class extends the Style class to prevent a large refactoring
 * process.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CustomStyle extends Style {

    //~ Instance fields --------------------------------------------------------

    private List<EndPointStyleDescription> endPointStyles = new ArrayList<EndPointStyleDescription>();
    private final Style style;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CustomStyle object.
     *
     * @param  style  DOCUMENT ME!
     */
    public CustomStyle(final Style style) {
        this.style = style;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  style  DOCUMENT ME!
     */
    public void addEndPointStyle(final EndPointStyleDescription style) {
        endPointStyles.add(style);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  style  DOCUMENT ME!
     */
    public void removeEndPointStyle(final EndPointStyleDescription style) {
        endPointStyles.remove(style);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<EndPointStyleDescription> getEndPointStyles() {
        return endPointStyles;
    }

    @Override
    public Style copy() {
        final CustomStyle cs = new CustomStyle(style);
        cs.endPointStyles = endPointStyles;

        return cs;
    }

    @Override
    public LinkedList<Triple<Styling, LinkedList<Geometry>, String>> evaluate(final Feature f,
            final XPathEvaluator<Feature> evaluator) {
        return style.evaluate(f, evaluator);
    }

    @Override
    public Style filter(final double scale) {
        final Style filtered = style.filter(scale);

        if (filtered != null) {
            final CustomStyle cs = new CustomStyle(filtered);
            cs.endPointStyles = endPointStyles;

            return cs;
        } else {
            return null;
        }
    }

    @Override
    public ArrayList<LinkedList<Styling>> getBases() {
        return style.getBases();
    }

    @Override
    public LinkedList<Triple<LinkedList<Styling>, DoublePair, LinkedList<String>>> getBasesWithScales() {
        return style.getBasesWithScales();
    }

    @Override
    public QName getFeatureType() {
        return style.getFeatureType();
    }

    @Override
    public File getLegendFile() {
        return style.getLegendFile();
    }

    @Override
    public URL getLegendURL() {
        return style.getLegendURL();
    }

    @Override
    public String getName() {
        return style.getName();
    }

    @Override
    public LinkedList<String> getRuleTitles() {
        return style.getRuleTitles();
    }

    @Override
    public LinkedList<Class<?>> getRuleTypes() {
        return style.getRuleTypes();
    }

    @Override
    public LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> getRules() {
        return style.getRules();
    }

    @Override
    public boolean isDefault() {
        return style.isDefault();
    }

    @Override
    public boolean isSimple() {
        return style.isSimple();
    }

    @Override
    public boolean prefersGetLegendGraphicUrl() {
        return style.prefersGetLegendGraphicUrl();
    }

    @Override
    public void setLegendFile(final File file) {
        style.setLegendFile(file);
    }

    @Override
    public void setLegendURL(final URL url) {
        style.setLegendURL(url);
    }

    @Override
    public void setName(final String name) {
        style.setName(name);
    }

    @Override
    public void setPrefersGetLegendGraphicUrl(final boolean prefers) {
        style.setPrefersGetLegendGraphicUrl(prefers);
    }
}
