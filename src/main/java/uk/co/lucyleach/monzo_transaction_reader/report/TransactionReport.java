package uk.co.lucyleach.monzo_transaction_reader.report;

import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;

import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

/**
 * User: Lucy
 * Date: 07/09/2018
 * Time: 20:56
 */
public class TransactionReport {
  private final Money totalAmountIn;
  private final Money totalAmountOut;
  private final List<TagLevelReport> tagReports;
  private final SortedMap<LocalDate, Money> expenditureByDate;
  private final List<IgnoredTransactionsReport> ignoredTransactionsReports;

  public TransactionReport(Money totalAmountIn, Money totalAmountOut, List<TagLevelReport> tagReports, SortedMap<LocalDate, Money> expenditureByDate,
                           List<IgnoredTransactionsReport> ignoredTransactionsReports) {
    this.totalAmountIn = totalAmountIn;
    this.totalAmountOut = totalAmountOut;
    this.tagReports = List.copyOf(tagReports);
    this.expenditureByDate = new TreeMap<>(expenditureByDate);
    this.ignoredTransactionsReports = ignoredTransactionsReports;
  }

  public Money getTotalAmountIn() {
    return totalAmountIn;
  }

  public Money getTotalAmountOut() {
    return totalAmountOut;
  }

  public List<TagLevelReport> getTagReports() {
    return tagReports;
  }

  public SortedMap<LocalDate, Money> getExpenditureByDate() {
    return expenditureByDate;
  }

  public List<IgnoredTransactionsReport> getIgnoredTransactionsReports() {
    return ignoredTransactionsReports;
  }

  public String produceReport() {
    var startString = "Total amount in: " + totalAmountIn + lineSeparator() +
        "Total amount out: " + totalAmountOut + lineSeparator() +
        "Tag level summary: ";
    var tagReportString = "\t" + tagReports.stream().map(TagLevelReport::toString).collect(joining(lineSeparator() + "\t"));
    var middleString = "Ignored transactions by reason:";
    var ignoredReportString = "\t" + ignoredTransactionsReports.stream().map(IgnoredTransactionsReport::toString).collect(joining(lineSeparator() + "\t"));

    return Stream.of(startString, tagReportString, middleString, ignoredReportString).collect(joining(lineSeparator()));
  }
}
