/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cismet.cismap.commons.featureservice.style;

import de.cismet.cismap.commons.ConvertableToXML;

/**
 *
 * @author haffkeatcismet
 */
public interface Style extends ConvertableToXML, Comparable, Cloneable {
    // JDOM-elementconstants
    public static final String STYLE_ELEMENT = "StyleHistoryElement";
    public static final String FILL = "Fill";
    public static final String LINE = "Line";
    public static final String LABEL = "Label";
    public static final String POINTSYMBOL = "Pointsymbol";
    public static final String NO_POINTSYMBOL = "kein";
    
    // JDOM-attributes
    public static final String NAME = "name";
    public static final String PAINT = "paint";
    public static final String COLOR = "color";
    public static final String WIDTH = "width";
    public static final String ALPHA = "alpha";
    public static final String HIGHLIGHT = "highlight";
    public static final String SIZE = "size";
    public static final String FAMILY = "family";
    public static final String STYLE = "style";
    public static final String ATTRIBUTE = "attribute";
    public static final String ALIGNMENT = "alignment";
    public static final String MIN_SCALE = "minscale";
    public static final String MAX_SCALE = "maxscale";
    public static final String MULTIPLIER = "multiplier";
    public static final String AUTOSCALE = "autoscale";
    
    public static final int MIN_POINTSYMBOLSIZE = 10;
}
