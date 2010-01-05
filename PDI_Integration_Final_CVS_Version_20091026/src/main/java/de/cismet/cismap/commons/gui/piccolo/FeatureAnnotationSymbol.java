/*
 * FeatureAnnotationSymbol.java
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
 * Created on 11. Mai 2006, 17:44
 *
 */
package de.cismet.cismap.commons.gui.piccolo;

import java.awt.Image;
import java.net.URL;

/**
 *
 * @author thorsten.hell@cismet.de
 */
public class FeatureAnnotationSymbol extends FixedPImage implements ParentNodeIsAPFeature {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FeatureAnnotationSymbol.class);
    private Image selectedFeatureAnnotationSymbol = null;

    /** Creates a new instance of FeatureAnnotationSymbol */
    public FeatureAnnotationSymbol() {
        super();
    }

    public FeatureAnnotationSymbol(Image newImage) {
        super(newImage);
    }

    public FeatureAnnotationSymbol(String fileName) {
        super(fileName);
    }

    public FeatureAnnotationSymbol(URL url) {
        super(url);
    }

    public Image getSelectedFeatureAnnotationSymbol() {
        return selectedFeatureAnnotationSymbol;
    }

    public void setSelectedFeatureAnnotationSymbol(Image selectedFeatureAnnotationSymbol) {
        this.selectedFeatureAnnotationSymbol = selectedFeatureAnnotationSymbol;
    }

    public static FeatureAnnotationSymbol newCenteredFeatureAnnotationSymbol(Image unselected, Image selected) {
        final FeatureAnnotationSymbol tmpSymbol = new FeatureAnnotationSymbol(unselected);
        tmpSymbol.setSelectedFeatureAnnotationSymbol(selected);
        tmpSymbol.setSweetSpotX(0.5d);
        tmpSymbol.setSweetSpotY(0.5d);
        return tmpSymbol;
    }

    public static FeatureAnnotationSymbol newCustomSweetSpotFeatureAnnotationSymbol(Image unselected, Image selected,
            double sweetSpotX, double sweetSpotY) {
        final FeatureAnnotationSymbol tmpSymbol = new FeatureAnnotationSymbol(unselected);
        tmpSymbol.setSelectedFeatureAnnotationSymbol(selected);
        tmpSymbol.setSweetSpotX(sweetSpotX);
        tmpSymbol.setSweetSpotY(sweetSpotY);
        return tmpSymbol;
    }
}
