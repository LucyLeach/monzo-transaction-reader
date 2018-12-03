package uk.co.lucyleach.monzo_transaction_reader.report;

import uk.co.lucyleach.monzo_transaction_reader.utils.Pair;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * User: Lucy
 * Date: 16/09/2018
 * Time: 10:37
 */
public class TransactionReport {
  private final Map<LocalDate, MonthlyTransactionReport> monthlyReportsByFirstOfMonth;
  private final List<Pair<String, String>> sortedTagsWithCategories;
  private final List<CategoryReport> categoryReports;

  private final List<IgnoredTransactionsReport> ignoredTransactionsReports;

  public TransactionReport(Map<LocalDate, MonthlyTransactionReport> monthlyReportsByFirstOfMonth, List<Pair<String, String>> sortedTagsWithCategories,
                           List<CategoryReport> categoryReports, List<IgnoredTransactionsReport> ignoredTransactionsReports) {
    this.monthlyReportsByFirstOfMonth = monthlyReportsByFirstOfMonth;
    this.sortedTagsWithCategories = List.copyOf(sortedTagsWithCategories);
    this.categoryReports = List.copyOf(categoryReports);
    this.ignoredTransactionsReports = List.copyOf(ignoredTransactionsReports);
  }

  public List<Pair<String, String>> getAllTagsSortedWithCategories() {
    return sortedTagsWithCategories;
  }

  public Map<LocalDate, MonthlyTransactionReport> getMonthlyReportsByFirstOfMonth() {
    return monthlyReportsByFirstOfMonth;
  }

  public List<IgnoredTransactionsReport> getIgnoredTransactionsReports() {
    return ignoredTransactionsReports;
  }

  public List<CategoryReport> getCategoryReports() {
    return categoryReports;
  }
}
