package uk.co.lucyleach.monzo_transaction_reader.report;

import uk.co.lucyleach.monzo_transaction_reader.utils.Pair;

import java.util.List;
import java.util.Map;

/**
 * User: Lucy
 * Date: 16/09/2018
 * Time: 10:37
 */
public class TransactionReport {
  private final Map<String, MonthlyTransactionReport> monthlyReportsByLabel;
  private final List<Pair<String, String>> sortedTagsWithCategories;
  private final List<CategoryReport> categoryReports;

  private final List<IgnoredTransactionsReport> ignoredTransactionsReports;

  public TransactionReport(Map<String, MonthlyTransactionReport> monthlyReportsByLabel, List<Pair<String, String>> sortedTagsWithCategories,
                           List<CategoryReport> categoryReports, List<IgnoredTransactionsReport> ignoredTransactionsReports) {
    this.monthlyReportsByLabel = monthlyReportsByLabel;
    this.sortedTagsWithCategories = List.copyOf(sortedTagsWithCategories);
    this.categoryReports = List.copyOf(categoryReports);
    this.ignoredTransactionsReports = List.copyOf(ignoredTransactionsReports);
  }

  public List<Pair<String, String>> getAllTagsSortedWithCategories() {
    return sortedTagsWithCategories;
  }

  public Map<String, MonthlyTransactionReport> getMonthlyReportsByLabel() {
    return monthlyReportsByLabel;
  }

  public List<IgnoredTransactionsReport> getIgnoredTransactionsReports() {
    return ignoredTransactionsReports;
  }

  public List<CategoryReport> getCategoryReports() {
    return categoryReports;
  }
}
