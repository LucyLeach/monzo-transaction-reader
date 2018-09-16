package uk.co.lucyleach.monzo_transaction_reader.report;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: Lucy
 * Date: 16/09/2018
 * Time: 10:37
 */
public class TransactionReport2 {
  private final List<SplitTransactionReport> splitReports;
  private final List<IgnoredTransactionsReport> ignoredTransactionsReports;

  public TransactionReport2(List<SplitTransactionReport> splitReports, List<IgnoredTransactionsReport> ignoredTransactionsReports) {
    this.splitReports = List.copyOf(splitReports);
    this.ignoredTransactionsReports = List.copyOf(ignoredTransactionsReports);
  }

  public List<SplitTransactionReport> getSplitReports() {
    return splitReports;
  }

  public List<IgnoredTransactionsReport> getIgnoredTransactionsReports() {
    return ignoredTransactionsReports;
  }

  public Map<String, SplitTransactionReport> getSplitReportsByLabel() {
    return splitReports.stream().collect(Collectors.toMap(r -> Integer.toString(r.hashCode()).substring(0,5), r -> r, (r1,r2) -> r2, LinkedHashMap::new));
  }
}
