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
    protected String[] additionalAttributes = new String[0];
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
     * DOCUMENT ME!
     */
    private void fillHeaderArrays() {
        int index = 0;
        attributeNames = new String[attributeCount()];
        attributeAlias = new String[attributeCount()];

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

        if (tableRuleSet != null) {
            final String[] fields = tableRuleSet.getAdditionalFieldNames();

            if (fields != null) {
                additionalAttributes = fields;
            }
        }

        fireContentsChanged();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int attributeCount() {
        int count = 0;

        for (final String key : orderedFeatureServiceAttributes) {
            final FeatureServiceAttribute fsa = featureServiceAttributes.get(key);
            if (fsa.isVisible()) {
                ++count;
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
            return attributeAlias.length + additionalAttributes.length;
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
     * DOCUMENT ME!
     *
     * @param   col  row DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isNumeric(final int col) {
        final String key = attributeNames[col];
        final FeatureServiceAttribute attr = featureServiceAttributes.get(key);

        if ((attr != null)
                    && (attr.getType().equals(String.valueOf(Types.INTEGER))
                        || attr.getType().equals(String.valueOf(Types.BIGINT))
                        || attr.getType().equals(String.valueOf(Types.SMALLINT))
                        || attr.getType().equals(String.valueOf(Types.TINYINT))
                        || attr.getType().equals(String.valueOf(Types.DOUBLE))
                        || attr.getType().equals(String.valueOf(Types.FLOAT))
                        || attr.getType().equals(String.valueOf(Types.DECIMAL))
                        || attr.getType().equals("xsd:float")
                        || attr.getType().equals("xsd:decimal")
                        || attr.getType().equals("xsd:double")
                        || attr.getType().equals("xsd:integer"))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   row  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureServiceFeature getFeatureServiceFeature(final int row) {
        return featureList.get(row);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  row DOCUMENT ME!
     */
    public void removeFeatureServiceFeature(final FeatureServiceFeature feature) {
        featureList.remove(feature);
        fireContentsChanged();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<FeatureServiceFeature> getFeatureServiceFeatures() {
        return featureList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnIndex  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getColumnName(final int columnIndex) {
        if (columnIndex < attributeAlias.length) {
            return attributeAlias[columnIndex];
        } else {
            return additionalAttributes[columnIndex - attributeAlias.length];
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnIndex  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getColumnAttributeName(final int columnIndex) {
        if (columnIndex < attributeAlias.length) {
            return attributeNames[columnIndex];
        } else {
            return additionalAttributes[columnIndex - attributeAlias.length];
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   columnIndex  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        if (columnIndex < attributeAlias.length) {
            final String key = attributeNames[columnIndex];
            final FeatureServiceAttribute attr = featureServiceAttributes.get(key);

            return FeatureTools.getClass(attr);
        } else {
            return tableRuleSet.getAdditionalFieldClass(columnIndex - attributeAlias.length);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   rowIndex     DOCUMENT ME!
     * @param   columnIndex  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   rowIndex     DOCUMENT ME!
     * @param   columnIndex  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (columnIndex < attributeAlias.length) {
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
        } else {
            return tableRuleSet.getAdditionalFieldValue(columnIndex - attributeAlias.length,
                    featureList.get(rowIndex));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  aValue       DOCUMENT ME!
     * @param  rowIndex     DOCUMENT ME!
     * @param  columnIndex  DOCUMENT ME!
     */
    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        // do nothing, because isCellEditable returns always false If the table should be writable, this model should be
        // extended
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    @Override
    public void addTableModelListener(final TableModelListener l) {
        listener.add(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    @Override
    public void removeTableModelListener(final TableModelListener l) {
        listener.remove(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  col  DOCUMENT ME!
     */
    public void hideColumn(final int col) {
        // todo fuer virtuelle Spalten
        this.attributeNames = remove(this.attributeNames, col);
        this.attributeAlias = remove(this.attributeAlias, col);
        fireContentsChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }

    /**
     * DOCUMENT ME!
     */
    public void showColumns() {
        fillHeaderArrays();
        fireContentsChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }

    /**
     * DOCUMENT ME!
     *
     * @param  row   DOCUMENT ME!
     * @param  name  DOCUMENT ME!
     */
    public void setColumnName(final int row, final String name) {
        if ((row >= 0) && (row < attributeAlias.length)) {
            attributeAlias[row] = name;
            fireContentsChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
        } else if ((row - attributeAlias.length) >= 0) {
            additionalAttributes[row - attributeAlias.length] = name;
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
}
