package uk.co.lucyleach.monzo_transaction_reader.report;

import java.time.Month;
import java.util.*;

import static java.util.stream.Collectors.toList;

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

  public List<String> getAllTagsSorted() {
    return splitReports.stream()
        .map(SplitTransactionReport::getTagReports)
        .flatMap(Collection::stream)
        .map(TagLevelReport::getTag)
        .distinct()
        .sorted()
        .collect(toList());
  }

  public List<IgnoredTransactionsReport> getIgnoredTransactionsReports() {
    return ignoredTransactionsReports;
  }

  public Map<String, SplitTransactionReport> getSplitReportsByLabel(boolean removeInitialStub) {
    var splitReportsToLabel = removeInitialStub ? reportsWithoutInitialStub() : splitReports;

    //Try to label by month, but if the previous label was the month then add an extra number for uniqueness
    Month previousMonth = null;
    int extraNumLabel = 0;
    var splitReportsByLabel = new LinkedHashMap<String, SplitTransactionReport>();
    for(SplitTransactionReport splitReport: splitReportsToLabel) {
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

  private List<SplitTransactionReport> reportsWithoutInitialStub() {
    if(splitReports.size() > 1 && !startsWithIncomeTransaction(splitReports.get(0))) {
      var mutableList = new ArrayList<>(splitReports);
      mutableList.remove(0);
      return mutableList;
    } else {
      return splitReports;
    }
  }

  private boolean startsWithIncomeTransaction(SplitTransactionReport splitReport) {
    return splitReport.getTransactions().get(0).getTag().equalsIgnoreCase("ignore");
  }
}
