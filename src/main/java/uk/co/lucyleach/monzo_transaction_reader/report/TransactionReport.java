package uk.co.lucyleach.monzo_transaction_reader.report;

import uk.co.lucyleach.monzo_transaction_reader.utils.Pair;

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

  public List<Pair<String, String>> getAllTagsSortedWithClassification() {
    return splitReports.stream()
        .map(SplitTransactionReport::getTagReports)
        .flatMap(Collection::stream)
        .map(r -> new Pair<String, String>(r.getTag(), r.getTagClassification()))
        .distinct()
        .sorted(Comparator.comparing(Pair::getA))
        .collect(toList());
  }

  public List<IgnoredTransactionsReport> getIgnoredTransactionsReports() {
    return ignoredTransactionsReports;
  }

  public Map<String, SplitTransactionReport> getSplitReportsByLabel(boolean removeInitialStub) {
    var splitReportsToLabel = removeInitialStub ? reportsWithoutInitialStub() : splitReports;

    //Try to label by month, but if the previous label was the month then add an extra number for uniqueness
    Month previousMonth = null;
    var extraNumLabel = 0;
    var splitReportsByLabel = new LinkedHashMap<String, SplitTransactionReport>();
    for(var splitReport: splitReportsToLabel) {
      var thisReportMonth = splitReport.getEarliestTransaction().plusDays(5).getMonth();
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

  public List<CategoryReport> getCategoryReports() {
    var splitReportsByLabel = getSplitReportsByLabel(true);
    var allCategories = splitReports.stream()
        .flatMap(sr -> sr.getTagReports().stream())
        .map(TagLevelReport::getTagClassification)
        .filter(Objects::nonNull)
        .distinct()
        .sorted()
        .collect(toList());
    var categoryReports = new ArrayList<CategoryReport>();
    for(var category: allCategories) {
      var amountBySplit = new ArrayList<Double>();
      for(var splitReport: splitReportsByLabel.values()) {
        var amount = splitReport.getTagReports().stream()
            .filter(tr -> category.equals(tr.getTagClassification()))
            .mapToDouble(tr -> tr.getTotalAmount().getAmountInPounds().doubleValue())
            .sum();
        amountBySplit.add(amount);
      }
      categoryReports.add(new CategoryReport(category, amountBySplit));
    }
    return categoryReports;
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
