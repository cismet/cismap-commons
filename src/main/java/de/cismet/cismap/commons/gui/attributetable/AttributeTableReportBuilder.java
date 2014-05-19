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

import ar.com.fdvs.dj.core.DJConstants;
import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.AutoText;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.builders.SubReportBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.entities.Subreport;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.swing.JRViewer;

import java.awt.Color;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Generates a jasper report for the attribute table.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AttributeTableReportBuilder {

    //~ Static fields/initializers ---------------------------------------------

    public static final String DATASOURCE_NAME = "table_data";
    private static final int PAGE_WIDTH = 560;

    //~ Instance fields --------------------------------------------------------

    private final Style titleStyle = new Style("HeaderStyle");
    private final Style columnHeaderStyle = new Style("TableHeaderStyle");
    private final Style columnDetailStyle = new Style("DetailStyle");
    private final Style oddRowStyle = new Style();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AttributeTableReportBuilder object.
     */
    public AttributeTableReportBuilder() {
        oddRowStyle.setBorder(Border.NO_BORDER);
        final Color lightGrey = new Color(230, 230, 230);
        oddRowStyle.setBackgroundColor(lightGrey);
        oddRowStyle.setTransparency(Transparency.OPAQUE);
        columnHeaderStyle.setBorderBottom(Border.PEN_2_POINT);
        columnHeaderStyle.setBorderColor(Color.BLACK);
        columnHeaderStyle.setPaddingBottom(2);
        columnDetailStyle.setTransparency(Transparency.OPAQUE);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   title  The title of the report
     * @param   table  the table, that should be drawn
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public DynamicReport buildReport(final String title, final JTable table) throws Exception {
        final DynamicReportBuilder drb = new DynamicReportBuilder();
        int totalWidth = 0;
        int lastCol = 0;
        int col;
        boolean firstPart = true;

        drb.setTitle(title)
                .setUseFullPageWidth(true)
                .setTemplateFile("de/cismet/cismap/commons/gui/attributetable/AttributeTableTemplate.jrxml")
                .setPrintBackgroundOnOddRows(true)
                .setOddRowBackgroundStyle(oddRowStyle)
                .setDefaultStyles(titleStyle, titleStyle, columnHeaderStyle, columnDetailStyle);

        for (col = 0; col < table.getColumnCount(); ++col) {
            final int width = table.getColumn(table.getColumnName(col)).getWidth();
            totalWidth += width;

            if (totalWidth > PAGE_WIDTH) {
                final Subreport subreport = new SubReportBuilder().setStartInNewPage(!firstPart)
                            .setDataSource(
                                    DJConstants.DATA_SOURCE_ORIGIN_PARAMETER,
                                    DJConstants.DATA_SOURCE_TYPE_JRDATASOURCE,
                                    AttributeTableReportBuilder.DATASOURCE_NAME)
                            .setDynamicReport(createSubreport(table, lastCol, col), new ClassicLayoutManager())
                            .build();
                drb.addConcatenatedReport(subreport);
                firstPart = false;
                lastCol = col;
                totalWidth = width;
            }
        }

        final Subreport subreport = new SubReportBuilder().setStartInNewPage(!firstPart)
                    .setDataSource(
                            DJConstants.DATA_SOURCE_ORIGIN_PARAMETER,
                            DJConstants.DATA_SOURCE_TYPE_JRDATASOURCE,
                            AttributeTableReportBuilder.DATASOURCE_NAME)
                    .setDynamicReport(createSubreport(table, lastCol, col), new ClassicLayoutManager())
                    .build();
        drb.addConcatenatedReport(subreport);

        drb.addAutoText(AutoText.AUTOTEXT_PAGE_X_OF_Y, AutoText.POSITION_FOOTER, AutoText.ALIGMENT_CENTER);

        return drb.build();
    }

    /**
     * Create a sub report with a table from the fromCol column to the untilCol column.
     *
     * @param   table     The table, that should be drawn
     * @param   fromCol   the first col, that should be drawn
     * @param   untilCol  the last col, that should be drawn (exclusive)
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private DynamicReport createSubreport(final JTable table,
            final int fromCol,
            final int untilCol) throws Exception {
        final TableModel model = table.getModel();
        final DynamicReportBuilder drb = new DynamicReportBuilder();
        final AbstractColumn[] abstractColumns = new AbstractColumn[model.getColumnCount()];

        for (int col = fromCol; col < untilCol; ++col) {
            final int width = table.getColumn(table.getColumnName(col)).getWidth();
            final int modelCol = table.convertColumnIndexToModel(col);

            abstractColumns[col] = ColumnBuilder.getNew()
                        .setColumnProperty(String.valueOf(modelCol), String.class.getName())
                        .setTitle(model.getColumnName(modelCol))
                        .setWidth(width)
                        .build();
            drb.addColumn(abstractColumns[col]);
        }

        drb.setMargins(20, 20, 20, 20)
                .setDefaultStyles(titleStyle, titleStyle, columnHeaderStyle, columnDetailStyle)
                .setPrintBackgroundOnOddRows(true)
                .setOddRowBackgroundStyle(oddRowStyle);

        return drb.build();
    }

    /**
     * Only for test purposes.
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final JFrame frame = new JFrame();
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        try {
            final JRDataSource ds = new TableDataSource(new CustomTableModel());
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put("table_data", ds);
            final DynamicReport report = new AttributeTableReportBuilder().buildReport(
                    "Titel",
                    new JTable(new CustomTableModel()));
            final JasperReport jasperReport = DynamicJasperHelper.generateJasperReport(
                    report,
                    new ClassicLayoutManager(),
                    map);

            for (final Object key : map.keySet()) {
                final Object o = map.get(key);

                if (o instanceof JasperReport) {
                    final JasperReport jr = (JasperReport)o;
                    DynamicJasperHelper.generateJRXML(
                        jr,
                        "UTF-8",
                        System.getProperty("user.dir")
                                + "/target/reports/"
                                + key
                                + ".jrxml");
                }
            }

            DynamicJasperHelper.generateJRXML(
                jasperReport,
                "UTF-8",
                System.getProperty("user.dir")
                        + "/target/reports/table_report.jrxml");
            final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, ds);

            final JRViewer aViewer = new JRViewer(jasperPrint);
            final JFrame aFrame = new JFrame(org.openide.util.NbBundle.getMessage(
                        AttributeTable.class,
                        "AttributeTable.butPrintPreviewActionPerformed.aFrame.title")); // NOI18N
            aFrame.getContentPane().add(aViewer);
            final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            aFrame.setSize(screenSize.width / 2, screenSize.height / 2);
            final java.awt.Insets insets = aFrame.getInsets();
            aFrame.setSize(aFrame.getWidth() + insets.left + insets.right,
                aFrame.getHeight()
                        + insets.top
                        + insets.bottom
                        + 20);
            aFrame.setLocationRelativeTo(frame);
            aFrame.setVisible(true);
        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Only for test purposes.
     *
     * @version  $Revision$, $Date$
     */
    private static class TableDataSource implements JRDataSource {

        //~ Instance fields ----------------------------------------------------

        private int index = -1;
        private TableModel model;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new TableDataSource object.
         *
         * @param  model  DOCUMENT ME!
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
                e.printStackTrace();
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
     * @version  $Revision$, $Date$
     */
    private static class CustomTableModel implements TableModel {

        //~ Instance fields ----------------------------------------------------

        String[] cols = { "ab", "cd", "ef", "gh" };
        String[][] attr = {
                { "a", "b", "c", "d" },
                { "a1", "b1", "c1", "d1" },
                { "a2", "b2", "c2", "d2" },
                { "a3", "b3", "c3", "d3" },
                { "a4", "4b", "c4", "4d" }
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
