package uk.co.lucyleach.monzo_transaction_reader.report;

import java.util.List;

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
}
