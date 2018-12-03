package uk.co.lucyleach.monzo_transaction_reader.report.excel_writer;

import uk.co.lucyleach.monzo_transaction_reader.report.MonthlyTransactionReport;
import uk.co.lucyleach.monzo_transaction_reader.report.TransactionReport;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * User: Lucy
 * Date: 16/09/2018
 * Time: 13:39
 */
public abstract class ExcelSheetWriter_FromMonthlyReports<R> implements ExcelSheetWriter<R> {
  @Override
  public Map<String, List<R>> getObjectsToWritePerSheet(TransactionReport report) {
    return report.getMonthlyReportsByFirstOfMonth().entrySet().stream()
        .collect(toMap(e -> getMonthlySheetName(e.getKey()), e -> getObjectsFromMonthlyReport(e.getValue()), (r1, r2) -> r2, LinkedHashMap::new));
  }

  private String getMonthlySheetName(LocalDate firstOfMonth) {
    return getSheetName() + "_" + firstOfMonth.getMonth().toString().substring(0, 3).toUpperCase() + "_" + firstOfMonth.getYear();
  }

  abstract String getSheetName();

  abstract List<R> getObjectsFromMonthlyReport(MonthlyTransactionReport monthlyReport);
}
