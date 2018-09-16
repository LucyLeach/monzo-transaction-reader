package uk.co.lucyleach.monzo_transaction_reader.report;

import java.time.Month;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Lucy
 * Date: 16/09/2018
 * Time: 10:37
 */
public class TransactionReport {
  private final List<SplitTransactionReport> splitReports;
  private final List<IgnoredTransactionsReport> ignoredTransactionsReports;

  public TransactionReport(List<SplitTransactionReport> splitReports, List<IgnoredTransactionsReport> ignoredTransactionsReports) {
    this.splitReports = List.copyOf(splitReports);
    this.ignoredTransactionsReports = List.copyOf(ignoredTransactionsReports);
  }

  public List<IgnoredTransactionsReport> getIgnoredTransactionsReports() {
    return ignoredTransactionsReports;
  }

  public Map<String, SplitTransactionReport> getSplitReportsByLabel() {
    Month previousMonth = null;
    int extraNumLabel = 0;
    var splitReportsByLabel = new LinkedHashMap<String, SplitTransactionReport>();
    for(SplitTransactionReport splitReport: splitReports) {
      Month thisReportMonth = splitReport.getEarliestTransaction().plusDays(5).getMonth();
      if(thisReportMonth.equals(previousMonth)) {
        extraNumLabel += 1;
        var label = thisReportMonth.name().substring(0, 3) + "_" + extraNumLabel;
        splitReportsByLabel.put(label, splitReport);
      } else {
        var label = thisReportMonth.name().substring(0, 3);
        splitReportsByLabel.put(label, splitReport);
        extraNumLabel = 0;
        previousMonth = thisReportMonth;
      }
    }

    return splitReportsByLabel;
  }
}
