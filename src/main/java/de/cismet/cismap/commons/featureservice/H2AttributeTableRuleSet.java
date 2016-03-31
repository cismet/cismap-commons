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
package de.cismet.cismap.commons.featureservice;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.lang.reflect.Constructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.cismet.cismap.commons.gui.attributetable.DefaultAttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.PrimitiveGeometryCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.cismap.linearreferencing.tools.StationTableCellEditorInterface;

/**
 * This is the default AttributeTableRouleSet for h2 services. This is required to support linear referencing
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class H2AttributeTableRuleSet extends DefaultAttributeTableRuleSet {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(H2AttributeTableRuleSet.class);

    //~ Instance fields --------------------------------------------------------

    private final List<LinearReferencingInfo> refInfos;
    private Map<String, LinearReferencingInfo> refInfoMap;
    private final String geometryType;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new H2AttributeTableRuleSet object.
     *
     * @param  refInfos      DOCUMENT ME!
     * @param  geometryType  DOCUMENT ME!
     */
    public H2AttributeTableRuleSet(final List<LinearReferencingInfo> refInfos, final String geometryType) {
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

    //~ Methods ----------------------------------------------------------------

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
    public List<LinearReferencingInfo> getAllLinRefInfos() {
        return refInfos;
    }
}
