package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport2;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * User: Lucy
 * Date: 14/09/2018
 * Time: 21:19
 */
public interface ExcelSheetWriter<R> {
  Map<String, List<R>> getObjectsToWritePerSheet(TransactionReport2 report);

  String[] getTitles();

  Consumer<R> objectWriter(Sheet sheet);
}
