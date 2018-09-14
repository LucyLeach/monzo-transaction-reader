package uk.co.lucyleach.monzo_transaction_reader.report;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

/**
 * User: Lucy
 * Date: 14/09/2018
 * Time: 13:47
 */
public class ReportWriter_Excel {
  private final String outputDir;

  public ReportWriter_Excel(String outputDir) {
    this.outputDir = outputDir;
  }

  public void write(TransactionReport report) throws IOException {
    Workbook workbook = new XSSFWorkbook();

    var titleStyle = workbook.createCellStyle();
    var titleFont = workbook.createFont();
    titleFont.setBold(true);
    titleStyle.setFont(titleFont);

    var tagSheet = workbook.createSheet("Tag Reports");
    var titleRow = tagSheet.createRow(0);
    createTagTitleRow(titleStyle, titleRow);
    var numTagReports = report.getTagReports().size();
    IntStream.rangeClosed(1, numTagReports).forEachOrdered(i -> createTagRow(tagSheet.createRow(i), report.getTagReports().get(i-1)));

    var numColumns = titleRow.getPhysicalNumberOfCells();
    IntStream.range(0, numColumns).forEach(tagSheet::autoSizeColumn);

    var dtFormatter= DateTimeFormatter.ofPattern("yyMMdd_HHmmss");
    var dateTimeString = LocalDateTime.now().format(dtFormatter);
    try(OutputStream out = new FileOutputStream(outputDir + "Report_" + dateTimeString + ".xlsx")) {
      workbook.write(out);
    }
  }

  private static void createTitleCells(CellStyle titleStyle, Row row, String... cellContents) {
    for(var i = 0; i < cellContents.length; i++) {
      var content = cellContents[i];
      var cell = row.createCell(i);
      cell.setCellValue(content);
      cell.setCellStyle(titleStyle);
    }
  }

  private static void createTagTitleRow(CellStyle titleStyle, Row row) {
    createTitleCells(titleStyle, row, "Tag Name", "Total In", "Total Out", "Num Transactions");
  }

  private static void createTagRow(Row row, TagLevelReport tagReport) {
    row.createCell(0).setCellValue(tagReport.getTag());
    row.createCell(1).setCellValue(tagReport.getTotalAmountIn().getAmountInPounds().doubleValue());
    row.createCell(2).setCellValue(tagReport.getTotalAmountOut().getAmountInPounds().doubleValue());
    row.createCell(3).setCellValue(tagReport.getNumTransactions());
  }
}
