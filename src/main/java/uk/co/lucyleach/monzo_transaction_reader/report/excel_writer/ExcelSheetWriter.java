package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import org.apache.poi.ss.usermodel.Sheet;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport;

import java.util.List;
import java.util.function.Consumer;

/**
 * User: Lucy
 * Date: 14/09/2018
 * Time: 21:19
 */
public interface ExcelSheetWriter<R> {
  String getSheetName();

  String[] getTitles();

  List<R> getObjects(TransactionReport report);

  Consumer<R> objectWriter(Sheet sheet);
}
