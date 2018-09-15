package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;
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
    var dateStyle = getDateStyle(workbook);

    createSheet(report, workbook, new ExcelSheetWriter_TagReport());
    createSheet(report, workbook, new ExcelSheetWriter_DetailedTagReport(dateStyle));
    createSheet(report, workbook, new ExcelSheetWriter_ByDate(dateStyle));
    createSheet(report, workbook, new ExcelSheetWriter_IgnoredTransactions());

    autoSizeAllColumnsOnAllSheets(workbook);

    try(OutputStream out = new FileOutputStream(outputDir + createFileName())) {
      workbook.write(out);
    }
  }

  private static <R> void createSheet(TransactionReport report, Workbook workbook, ExcelSheetWriter<R> writer) {
    var sheet = workbook.createSheet(writer.getSheetName());
    createTitleCells(getTitleStyle(workbook), sheet, writer::getTitles);
    writer.getObjects(report).stream().forEachOrdered(writer.objectWriter(sheet));
  }

  private static void createTitleCells(CellStyle titleStyle, Sheet sheet, Supplier<String[]> titles) {
    var row = sheet.createRow(0);
    var cellContents = titles.get();
    IntStream.range(0, cellContents.length).forEach(i -> {
      var content = cellContents[i];
      var cell = row.createCell(i);
      cell.setCellValue(content);
      cell.setCellStyle(titleStyle);
    });
  }

  private static CellStyle getTitleStyle(Workbook workbook) {
    var titleStyle = workbook.createCellStyle();
    var titleFont = workbook.createFont();
    titleFont.setBold(true);
    titleStyle.setFont(titleFont);
    return titleStyle;
  }

  private static CellStyle getDateStyle(Workbook workbook) {
    var cellStyle = workbook.createCellStyle();
    var creationHelper = workbook.getCreationHelper();
    cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd-mmm-yy"));
    return cellStyle;
  }

  private static void autoSizeAllColumnsOnAllSheets(Workbook workbook) {
    IntStream.range(0, workbook.getNumberOfSheets()).forEach(i -> {
      var sheet = workbook.getSheetAt(i);
      var numColumns = sheet.getRow(0).getPhysicalNumberOfCells();
      IntStream.range(0, numColumns).forEach(sheet::autoSizeColumn);
    });
  }

  private static String createFileName() {
    var dtFormatter= DateTimeFormatter.ofPattern("yyMMdd_HHmmss");
    var dateTimeString = LocalDateTime.now().format(dtFormatter);
    return "Report_" + dateTimeString + ".xlsx";
  }

}
