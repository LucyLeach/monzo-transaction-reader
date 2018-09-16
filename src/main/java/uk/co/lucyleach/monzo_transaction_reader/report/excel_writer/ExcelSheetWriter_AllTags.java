package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * User: Lucy
 * Date: 16/09/2018
 * Time: 14:14
 */
public class ExcelSheetWriter_AllTags implements ExcelSheetWriter<String> {
  @Override
  public Map<String, List<String>> getObjectsToWritePerSheet(TransactionReport report) {
    return Map.of("All_Tags", report.getAllTagsSorted());
  }

  @Override
  public String[] getTitles() {
    return new String[]{"Tags"};
  }

  @Override
  public Consumer<String> objectWriter(Sheet sheet) {
    return string -> {
      var row = sheet.createRow(sheet.getPhysicalNumberOfRows());
      row.createCell(0).setCellValue(string);
    };
  }
}
