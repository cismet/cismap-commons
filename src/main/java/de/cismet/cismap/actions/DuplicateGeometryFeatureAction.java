/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2011 thorsten
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cismap.actions;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import de.cismet.cismap.commons.features.CommonFeatureAction;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.StaticSwingTools;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = CommonFeatureAction.class)
public class DuplicateGeometryFeatureAction extends AbstractAction implements CommonFeatureAction {

    //~ Instance fields --------------------------------------------------------

    Feature f = null;

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DuplicateGeometryFeatureAction object.
     */
    public DuplicateGeometryFeatureAction() {
        super(NbBundle.getMessage(
                DuplicateGeometryFeatureAction.class,
                "DuplicateGeometryFeatureAction.DuplicateGeometryFeatureAction()"));
        super.putValue(
            Action.SMALL_ICON,
            new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/actions/raisePoly.png")));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int getSorter() {
        return 1;
    }

    @Override
    public Feature getSourceFeature() {
        return f;
    }

    @Override
    public boolean isActive() {
        return !(f instanceof PureNewFeature);
    }

    @Override
    public void setSourceFeature(final Feature source) {
        f = source;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final WaitDialog wd = new WaitDialog();
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    StaticSwingTools.showDialog(wd);
                }
            });
        de.cismet.tools.CismetThreadPool.execute(new javax.swing.SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    Thread.currentThread().setName("DuplicateGeometryFeatureAction");

                    final Geometry geom = (Geometry)f.getGeometry().clone();
                    final PureNewFeature pnf = new PureNewFeature(geom);

                    if ((geom instanceof LineString) || (geom instanceof MultiLineString)) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.LINESTRING);
                    } else if (geom instanceof Polygon) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.POLYGON);
                    } else if (geom instanceof MultiPolygon) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.MULTIPOLYGON);
                    } else if ((geom instanceof Point) || (geom instanceof MultiPoint)) {
                        pnf.setGeometryType(PureNewFeature.geomTypes.POINT);
                    } else {
                        pnf.setGeometryType(PureNewFeature.geomTypes.UNKNOWN);
                    }

                    pnf.setEditable(true);
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().addFeature(pnf);
                    CismapBroker.getInstance().getMappingComponent().getFeatureCollection().holdFeature(pnf);
                    return null;
                }

                @Override
                protected void done() {
                    wd.setVisible(false);
                    wd.dispose();
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final WaitDialog w = new WaitDialog();
        w.setVisible(true);
    }
}
/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class WaitDialog extends JDialog {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new WaitDialog object.
     */
    public WaitDialog() {
        super(StaticSwingTools.getParentFrame(CismapBroker.getInstance().getMappingComponent()), true);
        setLayout(new FlowLayout());
        getContentPane().add(new JLabel(
                new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/cismap/actions/raiseProgress.png"))));
        final JProgressBar prb = new JProgressBar();
        prb.setForeground(new Color(51, 153, 204));
        prb.setBorderPainted(false);
        prb.setIndeterminate(true);
        getContentPane().add(prb);
        setUndecorated(true);
        final JComponent c = CismapBroker.getInstance().getMappingComponent();
        pack();
        setLocationRelativeTo(c);
    }
}
