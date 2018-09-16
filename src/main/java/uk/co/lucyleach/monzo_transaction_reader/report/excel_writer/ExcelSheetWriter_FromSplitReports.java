package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import uk.co.lucyleach.monzo_transaction_reader.report.SplitTransactionReport;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * User: Lucy
 * Date: 16/09/2018
 * Time: 13:39
 */
public abstract class ExcelSheetWriter_FromSplitReports<R> implements ExcelSheetWriter<R> {
  @Override
  public Map<String, List<R>> getObjectsToWritePerSheet(TransactionReport report) {
    return report.getSplitReportsByLabel().entrySet().stream()
        .collect(toMap(e -> getSheetName() + "_" + e.getKey(), e -> getObjectsFromSplitReport(e.getValue()), (r1,r2) -> r2, LinkedHashMap::new));
  }

  abstract String getSheetName();

  abstract List<R> getObjectsFromSplitReport(SplitTransactionReport splitReport);
}
