package de.cismet.cismap.commons.gui.attributetable;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRStyledText;
import net.sf.jasperreports.swing.JRViewer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pd
 */
public class AttributeTableReportBuilderTest {

    public AttributeTableReportBuilderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        System.setProperty(JRStyledText.PROPERTY_AWT_IGNORE_MISSING_FONT, "true");
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test010AttributeTableReportBuilder() throws Exception {
        final JRDataSource tableDataSource = new AttributeTableReportBuilderTest.TableDataSource(new AttributeTableReportBuilderTest.CustomTableModel());
        final Map<String, Object> generatedParams = new HashMap<String, Object>();
        generatedParams.put("table_data", tableDataSource);
        final DynamicReport dynamicReport = new AttributeTableReportBuilder().buildReport(
                "Titel",
                new JTable(new AttributeTableReportBuilderTest.CustomTableModel()));
        assertNotNull(dynamicReport);

        final JasperReport jasperReport = DynamicJasperHelper.generateJasperReport(dynamicReport,
                new ClassicLayoutManager(),
                generatedParams);
        assertNotNull(jasperReport);

        for (final String key : generatedParams.keySet()) {
            final Object o = generatedParams.get(key);

            if (o instanceof JasperReport) {
                final JasperReport jr = (JasperReport) o;
                String reportFile = System.getProperty("user.dir")
                        + "/target/reports/"
                        + key
                        + ".jrxml";

                DynamicJasperHelper.generateJRXML(
                        jr,
                        "UTF-8",
                        reportFile);

                final JasperReport restoredReport = JasperCompileManager.compileReport(reportFile);
                assertNotNull(restoredReport);

                assertEquals(jr.getColumnCount(), restoredReport.getColumnCount());
                assertEquals(jr.getBottomMargin(), restoredReport.getBottomMargin());
                assertEquals(jr.getName(), restoredReport.getName());
                assertEquals(jr.getLanguage(), restoredReport.getLanguage());
                assertEquals(jr.getMainDataset().getName(), restoredReport.getMainDataset().getName());
            }
        }

        final String reportFile = System.getProperty("user.dir") + "/target/reports/table_report.jrxml";
        DynamicJasperHelper.generateJRXML(
                jasperReport,
                "UTF-8", reportFile);

        final JasperReport restoredReport = JasperCompileManager.compileReport(reportFile);
        assertNotNull(restoredReport);

        assertEquals(jasperReport.getColumnCount(), restoredReport.getColumnCount());
        assertEquals(jasperReport.getBottomMargin(), restoredReport.getBottomMargin());
        assertEquals(jasperReport.getName(), restoredReport.getName());
        assertEquals(jasperReport.getLanguage(), restoredReport.getLanguage());
        assertEquals(jasperReport.getMainDataset().getName(), restoredReport.getMainDataset().getName());

        final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, generatedParams, tableDataSource);
        assertNotNull(jasperPrint);

        final JRViewer aViewer = new JRViewer(jasperPrint);

        assertNotNull(aViewer);
    }

    private static class TableDataSource implements JRDataSource {

        //~ Instance fields ----------------------------------------------------
        private int index = -1;
        private final TableModel model;

        //~ Constructors -------------------------------------------------------
        /**
         * Creates a new TableDataSource object.
         *
         * @param model DOCUMENT ME!
         */
        public TableDataSource(final TableModel model) {
            this.model = model;
        }

        //~ Methods ------------------------------------------------------------
        @Override
        public boolean next() throws JRException {
            final boolean ret = ++index < model.getRowCount();

            if (!ret) {
                index = -1;
            }

            return ret;
        }

        @Override
        public Object getFieldValue(final JRField jrField) throws JRException {
            int col = 0;

            try {
                col = Integer.parseInt(jrField.getName());
            } catch (NumberFormatException e) {
                //e.printStackTrace();
            }

            final Object result = model.getValueAt(index, col);

            if (result != null) {
                return String.valueOf(result);
            } else {
                return null;
            }
        }
    }

    /**
     * Only for test purposes.
     *
     * @version $Revision$, $Date$
     */
    private static class CustomTableModel implements TableModel {

        //~ Instance fields ----------------------------------------------------
        String[] cols = {"ab", "cd", "ef", "gh"};
        String[][] attr = {
            {"a", "b", "c", "d"},
            {"a1", "b1", "c1", "d1"},
            {"a2", "b2", "c2", "d2"},
            {"a3", "b3", "c3", "d3"},
            {"a4", "4b", "c4", "4d"}
        };

        //~ Methods ------------------------------------------------------------
        @Override
        public int getRowCount() {
            return 5;
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(final int columnIndex) {
            return cols[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            return attr[rowIndex][columnIndex];
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addTableModelListener(final TableModelListener l) {
        }

        @Override
        public void removeTableModelListener(final TableModelListener l) {
        }
    }
}
