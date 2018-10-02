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

    createSheets(report, workbook, new ExcelSheetWriter_CategoryComparison());
    createSheets(report, workbook, new ExcelSheetWriter_TagReport());
    createSheets(report, workbook, new ExcelSheetWriter_DetailedTagReport(dateStyle));
    createSheets(report, workbook, new ExcelSheetWriter_ByDate(dateStyle));
    createSheets(report, workbook, new ExcelSheetWriter_IgnoredTransactions());
    createSheets(report, workbook, new ExcelSheetWriter_AllTags());

    autoSizeAllColumnsOnAllSheets(workbook);

    try(OutputStream out = new FileOutputStream(outputDir + createFileName())) {
      workbook.write(out);
    }
  }

  private static <R> void createSheets(TransactionReport report, Workbook workbook, ExcelSheetWriter<R> writer) {
    writer.getObjectsToWritePerSheet(report).entrySet().stream().forEachOrdered(e -> {
      var sheet = workbook.createSheet(e.getKey());
      createTitleCells(getTitleStyle(workbook), sheet, writer.getTitles(report));
      e.getValue().stream().forEachOrdered(writer.objectWriter(sheet));
    });
  }

  private static void createTitleCells(CellStyle titleStyle, Sheet sheet, String[] titles) {
    var row = sheet.createRow(0);
    IntStream.range(0, titles.length).forEach(i -> {
      var content = titles[i];
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
