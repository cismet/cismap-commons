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
package de.cismet.cismap.commons.gui.attributetable;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.lang.reflect.Constructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.LinearReferencingInfo;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.cismap.linearreferencing.tools.StationTableCellEditorInterface;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = H2AttributeTableRuleSet.class)
public class DefaultH2AttributeTableRuleSet extends DefaultAttributeTableRuleSet implements H2AttributeTableRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DefaultH2AttributeTableRuleSet.class);

    //~ Instance fields --------------------------------------------------------

    private List<LinearReferencingInfo> refInfos = null;
    private Map<String, LinearReferencingInfo> refInfoMap = null;
    private String geometryType = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DefaultH2AttributeTableRuleSet object.
     */
    public DefaultH2AttributeTableRuleSet() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Creates a new H2AttributeTableRuleSet object.
     *
     * @param  refInfos      DOCUMENT ME!
     * @param  geometryType  DOCUMENT ME!
     * @param  attributes    DOCUMENT ME!
     */
    @Override
    public void init(final List<LinearReferencingInfo> refInfos,
            final String geometryType,
            final List<FeatureServiceAttribute> attributes) {
        this.refInfos = refInfos;
        this.geometryType = geometryType;

        if (refInfos != null) {
            refInfoMap = new HashMap<String, LinearReferencingInfo>();

            for (final LinearReferencingInfo info : refInfos) {
                refInfoMap.put(info.getFromField(), info);

                if (info.getTillField() != null) {
                    refInfoMap.put(info.getTillField(), info);
                }
            }
        }
    }

    @Override
    public TableCellEditor getCellEditor(final String columnName) {
        final LinearReferencingInfo refInfo = getInfoForColumn(columnName);

        if (refInfo != null) {
            final Collection<? extends StationTableCellEditorInterface> cellEditor = Lookup.getDefault()
                        .lookupAll(StationTableCellEditorInterface.class);

            if ((cellEditor != null) && (cellEditor.size() > 0)) {
                final StationTableCellEditorInterface editor =
                    cellEditor.toArray(new StationTableCellEditorInterface[1])[0];

                final StationTableCellEditorInterface editorCopy = createNewInstance(editor);

                if (editorCopy != null) {
                    editorCopy.setLinRefInfos(refInfos);
                    editorCopy.setColumnName(columnName);
                    return editorCopy;
                }
            }
        }

        return super.getCellEditor(columnName);
    }

    @Override
    public FeatureCreator getFeatureCreator() {
        if ((refInfos == null) || refInfos.isEmpty()) {
            // primitive type
            if (geometryType.equalsIgnoreCase("Point")) {
                return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POINT, false);
            } else if (geometryType.equalsIgnoreCase("MultiPoint")) {
                return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POINT, true);
            } else if (geometryType.equalsIgnoreCase("LineString")) {
                return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.LINESTRING, false);
            } else if (geometryType.equalsIgnoreCase("MultiLineString")) {
                return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.LINESTRING, true);
            } else if (geometryType.equalsIgnoreCase("Polygon")) {
                return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, false);
            } else if (geometryType.equalsIgnoreCase("MultiPolygon")) {
                return new PrimitiveGeometryCreator(CreateGeometryListenerInterface.POLYGON, true);
            }
        }

        return super.getFeatureCreator();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   editor  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private StationTableCellEditorInterface createNewInstance(final StationTableCellEditorInterface editor) {
        try {
            final Constructor<? extends StationTableCellEditorInterface> c = editor.getClass().getConstructor();
            return c.newInstance();
        } catch (Exception e) {
            LOG.error("Cannot create a new instance of class " + editor.getClass().getName(), e);
        }

        return null;
    }

    @Override
    public TableCellRenderer getCellRenderer(final String columnName) {
        return super.getCellRenderer(columnName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public LinearReferencingInfo getInfoForColumn(final String columnName) {
        if (refInfoMap == null) {
            return null;
        }
        return refInfoMap.get(columnName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public List<LinearReferencingInfo> getAllLinRefInfos() {
        return refInfos;
    }
}
