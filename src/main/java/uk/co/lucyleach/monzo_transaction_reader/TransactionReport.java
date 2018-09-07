package uk.co.lucyleach.monzo_transaction_reader;

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
  private final SortedMap<LocalDate, Money> amountByDate;
  //TODO Reasons for ignored transactions

  public TransactionReport(Money totalAmountIn, Money totalAmountOut, List<TagLevelReport> tagReports, SortedMap<LocalDate, Money> amountByDate) {
    this.totalAmountIn = totalAmountIn;
    this.totalAmountOut = totalAmountOut;
    this.tagReports = List.copyOf(tagReports);
    this.amountByDate = new TreeMap<>(amountByDate);
  }

  public String produceReport() {
    var startString = "Total amount in: " + totalAmountIn + lineSeparator() +
        "Total amount out: " + totalAmountOut + lineSeparator() +
        "Tag level summary: ";
    var tagReportString = "\t" + tagReports.stream().map(TagLevelReport::toString).collect(joining(lineSeparator() + "\t"));
    var middleString = "Day by day summary:";
    var dayReportString = amountByDate.entrySet().stream()
        .map(e -> "\t" + e.getKey().toString() + ": " + e.getValue().toString())
        .collect(joining(lineSeparator()));

    return Stream.of(startString, tagReportString, middleString, dayReportString).collect(joining(lineSeparator()));
  }
}
