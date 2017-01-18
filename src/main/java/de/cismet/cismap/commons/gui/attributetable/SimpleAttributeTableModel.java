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
package de.cismet.cismap.commons.gui.attributetable;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;

import org.jdesktop.swingx.JXTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.tools.FeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SimpleAttributeTableModel implements TableModel {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SimpleAttributeTableModel.class);

    //~ Instance fields --------------------------------------------------------

    protected String[] attributeAlias;
    protected String[] attributeNames;
    protected Map<String, FeatureServiceAttribute> featureServiceAttributes;
    protected List<String> orderedFeatureServiceAttributes;
    protected List<FeatureServiceFeature> featureList;
    protected List<TableModelListener> listener = new ArrayList<TableModelListener>();
    protected AttributeTableRuleSet tableRuleSet;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CustomTableModel object.
     *
     * @param  orderedFeatureServiceAttributes  DOCUMENT ME!
     * @param  featureServiceAttributes         DOCUMENT ME!
     * @param  propertyContainer                DOCUMENT ME!
     * @param  tableRuleSet                     DOCUMENT ME!
     */
    public SimpleAttributeTableModel(final List<String> orderedFeatureServiceAttributes,
            final Map<String, FeatureServiceAttribute> featureServiceAttributes,
            final List<FeatureServiceFeature> propertyContainer,
            final AttributeTableRuleSet tableRuleSet) {
        this.featureServiceAttributes = featureServiceAttributes;
        this.orderedFeatureServiceAttributes = orderedFeatureServiceAttributes;
        this.featureList = propertyContainer;
        this.tableRuleSet = tableRuleSet;

        fillHeaderArrays();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * The feature must be of the same type as the other features in the model.
     *
     * @param  feature  DOCUMENT ME!
     */
    public void addFeature(final FeatureServiceFeature feature) {
        featureList.add(feature);
        fireContentsChanged();
    }

    /**
     * DOCUMENT ME!
     */
    private void fillHeaderArrays() {
        int index = 0;
        attributeNames = new String[attributeCount()];
        attributeAlias = new String[attributeCount()];

        if (orderedFeatureServiceAttributes != null) {
            for (final String attributeName : orderedFeatureServiceAttributes) {
                final FeatureServiceAttribute fsa = featureServiceAttributes.get(attributeName);

                if ((fsa == null) || fsa.isVisible()) {
                    attributeNames[index] = attributeName;
                    String aliasName = attributeName;

                    if ((fsa != null) && !fsa.getAlias().equals("")) {
                        final String alias = fsa.getAlias();

                        if (alias != null) {
                            aliasName = alias;
                        }
                    }

                    if (aliasName.startsWith("app:")) {
                        attributeAlias[index++] = aliasName.substring(4);
                    } else {
                        attributeAlias[index++] = aliasName;
                    }
                }
            }
        }
        fireTableStructureChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
        fireContentsChanged();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int attributeCount() {
        int count = 0;

        if (orderedFeatureServiceAttributes != null) {
            for (final String key : orderedFeatureServiceAttributes) {
                final FeatureServiceAttribute fsa = featureServiceAttributes.get(key);
                if (fsa.isVisible()) {
                    ++count;
                }
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  propertyContainer  DOCUMENT ME!
     */
    public void setNewFeatureList(final List<FeatureServiceFeature> propertyContainer) {
        this.featureList = propertyContainer;
        fireContentsChanged();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getRowCount() {
        if (featureList == null) {
            return 0;
        } else {
            return featureList.size();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getColumnCount() {
        if (attributeAlias == null) {
            return 0;
        } else {
            return attributeAlias.length;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  index  DOCUMENT ME!
     */
    public void moveRowUp(final int index) {
        final FeatureServiceFeature propToMove = featureList.get(index);

        for (int i = index; i > 0; --i) {
            featureList.set(i, featureList.get(i - 1));
        }

        featureList.set(0, propToMove);

        fireContentsChanged();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   row  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry getGeometryFromRow(final int row) {
        // The geometries from the attributes has no crs. At least, if they come from a shape file final List<String>
        // geometryColumns = new ArrayList<String>(); Geometry resultGeom = null;
        //
        // for (final String key : featureServiceAttributes.keySet()) { final FeatureServiceAttribute attr =
        // featureServiceAttributes.get(key);
        //
        // if (attr.isGeometry()) { geometryColumns.add(attr.getName()); } }
        //
        // for (final String name : geometryColumns) { final Object value = featureList.get(row).getProperty(name);
        // Geometry geo = null;
        //
        // if (value instanceof Geometry) { geo = ((Geometry)value); } else if (value instanceof
        // org.deegree.model.spatialschema.Geometry) { final org.deegree.model.spatialschema.Geometry geom =
        // ((org.deegree.model.spatialschema.Geometry) value); try { geo = JTSAdapter.export(geom); } catch
        // (GeometryException e) { LOG.error("Error while transforming deegree geometry to jts geometry.", e); } }
        //
        // if (geo != null) { if (resultGeom == null) { resultGeom = geo; } else { resultGeom = resultGeom.union(geo); }
        // } }
        //
        // resultGeom.setSRID(featureList.get(row).getGeometry().getSRID());

        // the same geometry, that is shown on the map, should be returned
        return featureList.get(row).getGeometry();
    }

    /**
     * Checks, if the given column has a numeric type.
     *
     * @param   col  the column to check
     *
     * @return  True, if the given column has a numeric type
     */
    public boolean isNumeric(final int col) {
        final String key = attributeNames[col];
        final FeatureServiceAttribute attr = featureServiceAttributes.get(key);

        return ((attr != null)
                        && (attr.getType().equals(String.valueOf(Types.INTEGER))
                            || attr.getType().equals(String.valueOf(Types.BIGINT))
                            || attr.getType().equals(String.valueOf(Types.SMALLINT))
                            || attr.getType().equals(String.valueOf(Types.TINYINT))
                            || attr.getType().equals(String.valueOf(Types.NUMERIC))
                            || attr.getType().equals(String.valueOf(Types.DOUBLE))
                            || attr.getType().equals(String.valueOf(Types.FLOAT))
                            || attr.getType().equals(String.valueOf(Types.DECIMAL))
                            || attr.getType().equals("xsd:float")
                            || attr.getType().equals("xsd:decimal")
                            || attr.getType().equals("xsd:double")
                            || attr.getType().equals("xsd:integer")));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   row  DOCUMENT ME!
     *
     * @return  The feature that is represented by the given row
     */
    public FeatureServiceFeature getFeatureServiceFeature(final int row) {
        return featureList.get(row);
    }

    /**
     * Removes the given feature from the model.
     *
     * @param  feature  the feature to remove
     */
    public void removeFeatureServiceFeature(final FeatureServiceFeature feature) {
        featureList.remove(feature);
        fireContentsChanged();
    }

    /**
     * Returns all features of this model.
     *
     * @return  A list with all features, which are contained in this model
     */
    public List<FeatureServiceFeature> getFeatureServiceFeatures() {
        return featureList;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return attributeAlias[columnIndex];
    }

    /**
     * Returns the attribute name of the given column. This is not the name of the column. The name of the column is the
     * alias name of the attribute.
     *
     * @param   columnIndex  the attribute name of this column will be returned
     *
     * @return  The attribute name of the given column
     */
    public String getColumnAttributeName(final int columnIndex) {
        return attributeNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        final String key = attributeNames[columnIndex];
        final FeatureServiceAttribute attr = featureServiceAttributes.get(key);

        return FeatureTools.getClass(attr);
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        Object value = featureList.get(rowIndex).getProperty(attributeNames[columnIndex]);

        if (value instanceof Geometry) {
            value = ((Geometry)value).getGeometryType();
        } else if (value instanceof org.deegree.model.spatialschema.Geometry) {
            final org.deegree.model.spatialschema.Geometry geom = ((org.deegree.model.spatialschema.Geometry)value);
            try {
                value = JTSAdapter.export(geom).getGeometryType();
            } catch (GeometryException e) {
                LOG.error("Error while transforming deegree geometry to jts geometry.", e);
            }
        }

        return value;
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        // do nothing, because isCellEditable returns always false If the table should be writable, this model should be
        // extended
    }

    @Override
    public void addTableModelListener(final TableModelListener l) {
        listener.add(l);
    }

    @Override
    public void removeTableModelListener(final TableModelListener l) {
        listener.remove(l);
    }

    /**
     * Hides the given column.
     *
     * @param  col  the column to hide
     */
    public void hideColumn(final int col) {
        // todo fuer virtuelle Spalten
        this.attributeNames = remove(this.attributeNames, col);
        this.attributeAlias = remove(this.attributeAlias, col);
        fireTableStructureChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
//        fireContentsChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }

    /**
     * Shows all column. After an invocation of this method, there are no hidden column anymore.
     */
    public void showColumns() {
        fillHeaderArrays();
        fireTableStructureChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
//        fireContentsChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }

    /**
     * Set the name of the given column.
     *
     * @param  col   the column number
     * @param  name  the new name to set
     */
    public void setColumnName(final int col, final String name) {
        if ((col >= 0) && (col < attributeAlias.length)) {
            attributeAlias[col] = name;
            fireContentsChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   array  DOCUMENT ME!
     * @param   index  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String[] remove(final String[] array, final int index) {
        if ((index >= 0) && (index < array.length)) {
            final String[] resultArray = new String[array.length - 1];
            int indexResArray = 0;

            for (int i = 0; i < array.length; ++i) {
                if (i != index) {
                    resultArray[indexResArray++] = array[i];
                }
            }

            return resultArray;
        } else {
            return array;
        }
    }

    /**
     * DOCUMENT ME!
     */
    protected void fireContentsChanged() {
        final TableModelEvent e = new TableModelEvent(this);

        for (final TableModelListener tmp : listener) {
            tmp.tableChanged(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    protected void fireContentsChanged(final TableModelEvent e) {
        for (final TableModelListener tmp : listener) {
            tmp.tableChanged(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    protected void fireTableStructureChanged(final TableModelEvent e) {
        for (final TableModelListener tmp : listener) {
            if (tmp instanceof JXTable) {
                tmp.tableChanged(e);
            }
        }
    }
}
