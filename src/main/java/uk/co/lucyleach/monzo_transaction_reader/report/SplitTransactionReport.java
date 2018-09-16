package uk.co.lucyleach.monzo_transaction_reader.report;

import uk.co.lucyleach.monzo_transaction_reader.output_model.Money;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * User: Lucy
 * Date: 16/09/2018
 * Time: 10:37
 */
public class SplitTransactionReport {
  private final ZonedDateTime earliestTransaction;
  private final ZonedDateTime latestTransaction;
  private final Money totalAmountIn;
  private final Money totalAmountOut;
  private final List<TagLevelReport> tagReports;
  private final SortedMap<LocalDate, Money> expenditureByDate;

  public SplitTransactionReport(ZonedDateTime earliestTransaction, ZonedDateTime latestTransaction, Money totalAmountIn,
                                Money totalAmountOut, List<TagLevelReport> tagReports, SortedMap<LocalDate, Money> expenditureByDate) {
    this.earliestTransaction = earliestTransaction;
    this.latestTransaction = latestTransaction;
    this.totalAmountIn = totalAmountIn;
    this.totalAmountOut = totalAmountOut;
    this.tagReports = List.copyOf(tagReports);
    this.expenditureByDate = new TreeMap<>(expenditureByDate);
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
    return new TreeMap<>(expenditureByDate);
  }
}
