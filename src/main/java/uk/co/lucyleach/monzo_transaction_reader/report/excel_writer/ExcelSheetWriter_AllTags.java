package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport;
import uk.co.lucyleach.monzo_transaction_reader.utils.Pair;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * User: Lucy
 * Date: 16/09/2018
 * Time: 14:14
 */
public class ExcelSheetWriter_AllTags implements ExcelSheetWriter<Pair<String, String>> {
  @Override
  public Map<String, List<Pair<String, String>>> getObjectsToWritePerSheet(TransactionReport report) {
    return Map.of("All_Tags", report.getAllTagsSortedWithClassification());
  }

  @Override
  public String[] getTitles(TransactionReport report) {
    return new String[]{"Tag", "Classification"};
  }

  @Override
  public Consumer<Pair<String, String>> objectWriter(Sheet sheet) {
    return stringPair -> {
      var row = sheet.createRow(sheet.getPhysicalNumberOfRows());
      row.createCell(0).setCellValue(stringPair.getA());
      row.createCell(1).setCellValue(stringPair.getB());
    };
  }
}
