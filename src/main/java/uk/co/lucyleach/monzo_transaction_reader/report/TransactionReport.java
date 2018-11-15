package uk.co.lucyleach.monzo_transaction_reader.report;

import uk.co.lucyleach.monzo_transaction_reader.utils.Pair;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * User: Lucy
 * Date: 16/09/2018
 * Time: 10:37
 */
public class TransactionReport {
  private final Map<String, MonthlyTransactionReport> monthlyReportsByLabel;
  private final List<IgnoredTransactionsReport> ignoredTransactionsReports;

  public TransactionReport(Map<String, MonthlyTransactionReport> monthlyReportsByLabel, List<IgnoredTransactionsReport> ignoredTransactionsReports) {
    this.monthlyReportsByLabel = monthlyReportsByLabel;
    this.ignoredTransactionsReports = List.copyOf(ignoredTransactionsReports);
  }

  public List<Pair<String, String>> getAllTagsSortedWithClassification() {
    return monthlyReportsByLabel.values().stream()
        .map(MonthlyTransactionReport::getTagReports)
        .flatMap(Collection::stream)
        .map(r -> new Pair<>(r.getTag(), r.getTagClassification()))
        .distinct()
        .sorted(Comparator.comparing(Pair::getA))
        .collect(toList());
  }

  public Map<String, MonthlyTransactionReport> getMonthlyReportsByLabel() {
    return monthlyReportsByLabel;
  }

  public List<IgnoredTransactionsReport> getIgnoredTransactionsReports() {
    return ignoredTransactionsReports;
  }

  public List<CategoryReport> getCategoryReports() {
    var allCategories = monthlyReportsByLabel.values().stream()
        .flatMap(sr -> sr.getTagReports().stream())
        .map(TagLevelReport::getTagClassification)
        .filter(Objects::nonNull)
        .distinct()
        .sorted()
        .collect(toList());
    var categoryReports = new ArrayList<CategoryReport>();
    for(var category: allCategories) {
      var amountBySplit = new ArrayList<Double>();
      for(var monthlyReport: monthlyReportsByLabel.values()) {
        var amount = monthlyReport.getTagReports().stream()
            .filter(tr -> category.equals(tr.getTagClassification()))
            .mapToDouble(tr -> tr.getTotalAmount().getAmountInPounds().doubleValue())
            .sum();
        amountBySplit.add(amount);
      }
      categoryReports.add(new CategoryReport(category, amountBySplit));
    }
    return categoryReports;
  }
}
