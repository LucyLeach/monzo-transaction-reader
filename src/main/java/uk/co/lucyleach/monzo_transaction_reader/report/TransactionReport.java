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
  private final Map<String, MonthlyTransactionReport> monthlyReportsByLabel;
  private final List<IgnoredTransactionsReport> ignoredTransactionsReports;

  public TransactionReport(List<MonthlyTransactionReport> splitReports, List<IgnoredTransactionsReport> ignoredTransactionsReports) {
    this.monthlyReportsByLabel = labelMonthlyReports(List.copyOf(splitReports));
    this.ignoredTransactionsReports = List.copyOf(ignoredTransactionsReports);
  }

  private Map<String, MonthlyTransactionReport> labelMonthlyReports(List<MonthlyTransactionReport> originalMonthlyReports) {
    var monthlyReportsToLabel = reportsWithoutInitialStub(originalMonthlyReports);

    //Try to label by month, but if the previous label was the month then add an extra number for uniqueness
    Month previousMonth = null;
    var extraNumLabel = 0;
    var monthlyReportsByLabel = new LinkedHashMap<String, MonthlyTransactionReport>();
    for(var splitReport: monthlyReportsToLabel) {
      var thisReportMonth = splitReport.getEarliestTransaction().plusDays(5).getMonth();
      if(thisReportMonth.equals(previousMonth)) {
        extraNumLabel += 1;
        var label = thisReportMonth.name().substring(0, 3) + "_" + extraNumLabel;
        monthlyReportsByLabel.put(label, splitReport);
      } else {
        var label = thisReportMonth.name().substring(0, 3);
        monthlyReportsByLabel.put(label, splitReport);
        extraNumLabel = 0;
        previousMonth = thisReportMonth;
      }
    }

    return monthlyReportsByLabel;
  }

  public List<Pair<String, String>> getAllTagsSortedWithClassification() {
    return monthlyReportsByLabel.values().stream()
        .map(MonthlyTransactionReport::getTagReports)
        .flatMap(Collection::stream)
        .map(r -> new Pair<String, String>(r.getTag(), r.getTagClassification()))
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
      for(var splitReport: monthlyReportsByLabel.values()) {
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

  private List<MonthlyTransactionReport> reportsWithoutInitialStub(List<MonthlyTransactionReport> monthlyReports) {
    if(monthlyReports.size() > 1 && !startsWithIncomeTransaction(monthlyReports.get(0))) {
      var mutableList = new ArrayList<>(monthlyReports);
      mutableList.remove(0);
      return mutableList;
    } else {
      return monthlyReports;
    }
  }

  private boolean startsWithIncomeTransaction(MonthlyTransactionReport splitReport) {
    return splitReport.getTransactions().get(0).getTag().equalsIgnoreCase("income");
  }
}
