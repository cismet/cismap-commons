/*
 * PrintingToolTip.java
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
 * Created on 17. November 2006, 11:09
 *
 */

package de.cismet.cismap.commons.gui.printing;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.RoundRectangle2D;

/**
 *de.cismet.cismap.commons.gui.printing.PrintingToolTip.PrintingToolTip().
 * @author thorsten.hell@cismet.de
 */
public class PrintingToolTip extends PNode{

    /** Creates a new instance of PrintingToolTip */
    public PrintingToolTip(Color backgroundColor) {
        PImage image=new PImage(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/frameprint64.png")).getImage());//NOI18N
        image.setOffset(10,10);
        PText t1=new PText(org.openide.util.NbBundle.getMessage(PrintingToolTip.class,"PrintingToolTip.PrintingToolTip().t1"));
        
        Font defaultFont=t1.getFont();
        Font boldDefaultFont=new Font(defaultFont.getName(),defaultFont.getStyle()+Font.BOLD,defaultFont.getSize());
        t1.setFont(boldDefaultFont);
        PText t2=new PText(org.openide.util.NbBundle.getMessage(PrintingToolTip.class,"PrintingToolTip.PrintingToolTip().t2"));
        PText t3=new PText(org.openide.util.NbBundle.getMessage(PrintingToolTip.class,"PrintingToolTip.PrintingToolTip().t3"));
        PText t4=new PText(org.openide.util.NbBundle.getMessage(PrintingToolTip.class,"PrintingToolTip.PrintingToolTip().t4"));
        

        double textHeight=t1.getHeight()+5+t2.getHeight()+5+t3.getHeight()+5+t4.getHeight();
        double textWidth=Math.max(Math.max(t1.getWidth(),t2.getWidth()),Math.max(t3.getWidth(),t4.getWidth()));
        
        double backgroundHeight=Math.max(textHeight,image.getHeight());
        
        PPath background=new PPath(new RoundRectangle2D.Double(0,0,10+image.getWidth()+textWidth+10,5+backgroundHeight+5,10,10));
        background.setPaint(backgroundColor);
        background.addChild(image);
        background.addChild(t1);
        background.addChild(t2);
        background.addChild(t3);
        background.addChild(t4);
        t1.setOffset(image.getWidth()+5+10,5);
        t2.setOffset(t1.getOffset().getX(),t1.getOffset().getY()+5+t1.getHeight());
        t3.setOffset(t1.getOffset().getX(),t2.getOffset().getY()+5+t2.getHeight());
        t4.setOffset(t1.getOffset().getX(),t3.getOffset().getY()+5+t3.getHeight());
        setTransparency(0.85f);
        addChild(background);
        
    }
    
}
